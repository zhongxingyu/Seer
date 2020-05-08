 /*===========================================================================
   Copyright (C) 2008-2010 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.persistence;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.observer.BaseObservable;
 import net.sf.okapi.common.observer.IObservable;
 import net.sf.okapi.common.observer.IObserver;
 
 public abstract class PersistenceSession implements IPersistenceSession, IObservable {
 
 	private static final String ITEM_LABEL = "item"; //$NON-NLS-1$
 	
 	protected abstract void writeBean(IPersistenceBean<?> bean, String name);	
 	protected abstract String writeBeanToString(IPersistenceBean<?> bean);
 	protected abstract <T extends IPersistenceBean<?>> T readBean(Class<T> beanClass, String name);
 	protected abstract <T extends IPersistenceBean<?>> T readBeanFromString(String content, Class<T> beanClass);
 	
 	protected abstract void startWriting(OutputStream outStream);
 	protected abstract void endWriting(OutputStream outStream);
 	
 	protected abstract void startReading(InputStream inStream);
 	protected abstract void endReading(InputStream inStream);
 	
 	protected abstract String getDefItemLabel();
 	protected abstract Class<?> getDefItemClass();
 	protected abstract String getDefVersionId();
 	
 	private String itemLabel = ITEM_LABEL;
 	private SessionState state = SessionState.IDLE;
 	private ReferenceResolver refResolver = new ReferenceResolver(this);
 	private BeanMapper beanMapper = new BeanMapper(this); 
 	private int itemCounter = 0;
 	private Class<?> prevClass;
 	private Class<? extends IPersistenceBean<?>> beanClass;
 	private OutputStream outStream;
 	private InputStream inStream;
 	private String description;
 	private Class<?> itemClass;
 	private LinkedList<IPersistenceBean<?>> queue = new LinkedList<IPersistenceBean<?>>();
 	private boolean readingDone = false;
 	private IVersionDriver versionDriver = null;
 
 	public PersistenceSession() {
 		super();
 		registerVersions();
 		setItemLabel(getDefItemLabel());
 		setItemClass(getDefItemClass());
 		setVersion(getDefVersionId()); // sets versionDriver
 	}
 	
 	@Override
 	public void cacheBean(Object obj, IPersistenceBean<?> bean) {
 		refResolver.cacheBean(obj, bean);
 	}
 	
 	@Override
 	public <T> IPersistenceBean<T> createBean(Class<T> classRef) {
 		return refResolver.createBean(classRef);
 	}
 
 	@Override
 	public <T> T deserialize(Class<T> classRef) {
 		return deserialize(classRef, itemLabel);
 	}
 
 	@SuppressWarnings("unchecked")
 	private <T> IPersistenceBean<T> nextBean(Class<T> classRef, String name) {
 		if (readingDone) return null;
 		// Update bean class if core class has changed
 		if (classRef != prevClass) { 
 			beanClass = beanMapper.getBeanClass(classRef);
 			prevClass = classRef;
 		}
 		IPersistenceBean<T> bean = (IPersistenceBean<T>) readBean(beanClass, name);
 		notifyObservers(bean);
 		readingDone = bean == null;
 		return bean;		
 	}
 	
 	private <T> T deserialize(Class<T> classRef, String name) {
 		if (state != SessionState.READING) return null;
 			
 		while (true) {			
 			if (queue.size() == 0) { // Empty queue
 				
 				if (readingDone) { // The stream is read to the end, no object to return as queue is empty					
 					end();
 					return null;
 				}
 				else { // Read one bean from the stream
 					IPersistenceBean<?> bean = nextBean(classRef, name);										
 					if (readingDone) continue;
 					
 					refResolver.cacheBean(bean);
 					queue.add(bean);
 				}
 			}
 			else { // Something in the queue
 				
 				// We are interested in the head bean
 				IPersistenceBean<?> bean = queue.peek();
 				long refId = bean.getRefId();
 				if (refId < 0) refId = -refId; // anti-bean
 				Object obj = getObject(refId);
 				
 				if (obj != null) { // The bean has been resolved, its object found in the cache
 					queue.poll();
 					refResolver.releaseObject(obj);
 					return classRef.cast(obj);
 				}
 				else { // The bean is not yet resolved, which means more beans are required to be read, the frame is resolved all at once
 					Set<Long> frame = refResolver.getFrame(refId);
 					if (frame != null) { // The bean is part of a frame
 						do {
 							bean = nextBean(classRef, name);
 							if (readingDone) break;
 							
 							refResolver.cacheBean(bean);							
 							queue.add(bean);
 						} while (!refResolver.isFrameAvailable(frame));
 						
 						// Ref resolution
 						for (Long rid : frame) {
 							bean = refResolver.uncacheBean(rid); // removes from bean cache
 							if (bean == null)
 								throw new RuntimeException(String.format("PersistenceSession: bean %d not found in cache", rid));							
 							if (rid < 0) continue;
 							
 							refResolver.setRootId(rid);
 							obj = classRef.cast(bean.get(classRef, this));
 							refResolver.setRefIdForObject(obj, rid); // for getObject()
 						}
 						refResolver.removeFrame(frame);
 						continue;
 					}
 					else { // The bean is stand-alone
 						bean = queue.poll();
 						obj = bean.get(classRef, this);
 						refResolver.releaseObject(obj);
 						return classRef.cast(obj); 
 					}
 				}
 			}			
 		}
 	}
 	
 	@Override
 	public void end() {
 		switch (state) {
 		case IDLE:
 			return;
 			
 		case READING:
 			readingDone = true;
 			if (inStream != null)
 				endReading(inStream);
 			// !!! Do not close external inStream
 			break;
 			
 		case WRITING:
 			if (outStream != null) {
 				refResolver.updateFrames();
 				endWriting(outStream);
 			}
 			// !!! Do not close external outStream
 		}		
 		inStream = null;
 		outStream = null;
 		prevClass = null;
 		beanClass = null;
 		refResolver.reset();	
 		state = SessionState.IDLE;
 		setVersion(getDefVersionId());
 	}
 	
 	@Override
 	public String getDescription() {		
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	@Override
 	public long getRefIdForObject(Object obj) {
 		return refResolver.getRefIdForObject(obj);
 	}
 
 	@Override
 	public void setRefIdForObject(Object obj, long refId) {
 		refResolver.setRefIdForObject(obj, refId);
 	}
 
 	@Override
 	public void setReference(long parentRefId, long childRefId) {
 		refResolver.setReference(parentRefId, childRefId);
 	}
 
 	@Override
 	public String getItemClass() {
 		return (itemClass == null) ? "" : itemClass.getName();
 	}
 
 	@Override
 	public void serialize(Object obj) {
 		if (obj == null)
 			throw new IllegalArgumentException("PersistenceSession: cannot serialize a null object");
 		serialize(obj, String.format("%s%d", itemLabel, ++itemCounter));
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void serialize(Object obj, String name) {
 		if (state != SessionState.WRITING) return;
 		if (obj == null)
 			throw new IllegalArgumentException("PersistenceSession: cannot serialize a null object");
 
 		long rid = refResolver.getRefIdForObject(obj);
 		IPersistenceBean<Object> bean = null;
 		
 		if (refResolver.isSerialized(obj)) { // The object has been serialized by another bean (as a FactoryBean field)
 			//if (bean != null)
 				bean = (IPersistenceBean<Object>) refResolver.createAntiBean(obj.getClass(), rid);
 				//refResolver.uncacheBean(obj); // to not leave in cache 
 //			else
 //				throw new RuntimeException(String.format("PersistenceSession: bean for %s (%s) not found in cache, " +
 //						"though marked as already serialized", name, obj.getClass()));
 		}
 		else {
 			bean = (IPersistenceBean<Object>) refResolver.uncacheBean(obj); // get a bean created earlier by ReferenceBean
 			if (bean == null) {
 				bean = (IPersistenceBean<Object>) refResolver.createBean(obj.getClass());
 				if (bean == null) return;
 				
 				//refResolver.cacheBean(obj, bean);			
 			}
 					
 			refResolver.setRootId(bean.getRefId());
 			refResolver.setRefIdForObject(obj, bean.getRefId());
 			bean.set(obj, this);
 		}
 		
 		notifyObservers(bean);
 		writeBean(bean, name);
 	}
 
 	@Override
 	public void start(OutputStream outStream) {
 		if (outStream == null)
 			throw new IllegalArgumentException("PersistenceSession: output stream cannot be null");
 		
 		end();
 		refResolver.reset();
 		setVersion(getDefVersionId());
 		
 		this.outStream = outStream;		
 		itemCounter = 0;
 		
 		if (Util.isEmpty(itemLabel))
 			this.itemLabel = ITEM_LABEL;
 		
 		state = SessionState.WRITING;
 		startWriting(outStream);
 	}
 
 	@Override
 	public void start(InputStream inStream) {
 		if (inStream == null)
 			throw new IllegalArgumentException("PersistenceSession: input stream cannot be null");
 		
 		end();
 		refResolver.reset();
 		setVersion(getDefVersionId());
 		
 		this.inStream = inStream;				
 		readingDone = false;
 		state = SessionState.READING;
 		startReading(inStream);
 	}
 
 	@Override
 	public IPersistenceBean<?> uncacheBean(Object obj) {
 		return refResolver.uncacheBean(obj);
 	}
 
 	@Override
 	public SessionState getState() {
 		return state;
 	}
 	
 	public String getItemLabel() {
 		return itemLabel;
 	}
 	
 	public void setItemLabel(String itemLabel) {
 		this.itemLabel = itemLabel;
 	}
 
 	protected List<List<Long>> getFrames() {
 		return refResolver.getFrames();
 	}
 	
 	protected void setFrames(List<List<?>> frames) {
 		refResolver.setFrames(frames);
 	}
 	
 	public void setItemClass(Class<?> itemClass) {
 		this.itemClass = itemClass;
 	}
 	
 	@Override
 	public Object getObject(long refId) {		
 		return refResolver.getObject(refId);
 	}
 	
 	@Override
 	public void setSerialized(Object obj) {
 		refResolver.setSerialized(obj);
 	}
 	
 	//
 	// implements IObservable interface
 	//
 
 	/**
 	 * Implements multiple inheritance via delegate pattern to an inner class
 	 * 
 	 * @see IObservable
 	 * @see BaseObservable
 	 */
 	private IObservable delegatedObservable = new BaseObservable(this);
 
 	@Override
 	public void addObserver(IObserver observer) {
 		delegatedObservable.addObserver(observer);
 	}
 
 	public int countObservers() {
 		return delegatedObservable.countObservers();
 	}
 
 	public void deleteObserver(IObserver observer) {
 		delegatedObservable.deleteObserver(observer);
 	}
 
 	public void notifyObservers() {
 		delegatedObservable.notifyObservers();
 	}
 
 	public void notifyObservers(Object arg) {
 		delegatedObservable.notifyObservers(arg);
 	}
 
 	public void deleteObservers() {
 		delegatedObservable.deleteObservers();
 	}
 
 	public List<IObserver> getObservers() {
 		return delegatedObservable.getObservers();
 	}
 	
 	public Class<?> getClass(String objClassName) {
 		return beanMapper.getClass(objClassName);
 	}
 	
 	public Class<?> getObjectClass(Class<? extends IPersistenceBean<?>> beanClassRef) {
 		return beanMapper.getObjectClass(beanClassRef);
 	}
 	
 	public <T> Class<IPersistenceBean<T>> getBeanClass(Class<T> classRef) {
 		return beanMapper.getBeanClass(classRef);
 	}
 	
 	public Class<? extends IPersistenceBean<?>> getBeanClass(String className) {
 		return beanMapper.getBeanClass(className);
 	}
 	
 	public IPersistenceBean<?> getProxy(String objClassName) {
 		return beanMapper.getProxy(objClassName);
 	}
 	
 	public IPersistenceBean<?> getProxy(Class<?> objClassRef) {
 		return beanMapper.getProxy(objClassRef);
 	}
 	
 	public void registerBean(
 			Class<?> classRef, 
 			Class<? extends IPersistenceBean<?>> beanClassRef) {
 		beanMapper.registerBean(classRef, beanClassRef);
 	}
 	
 	@Override
 	public String getVersion() {
 		return versionDriver == null ? "" : versionDriver.getVersionId();
 	}
 	
 	protected void setVersion(String versionId) {
 		if (Util.isEmpty(versionId))
 			throw new IllegalArgumentException(String.format("PersistenceSession: version id cannot be empty"));
 		
 		if (versionDriver != null && 
 				versionId.equalsIgnoreCase(versionDriver.getVersionId())) return; // already set
 		
 		versionDriver = VersionMapper.getDriver(versionId);
 		if (versionDriver == null)
 			throw new RuntimeException(String.format("PersistenceSession: the version %s is not supported", versionId));
 		beanMapper.reset();
 		versionDriver.registerBeans(beanMapper);
 	}
 	
 	@Override
 	public <T> T readObject(String content, Class<T> classRef) {
 		beanClass = beanMapper.getBeanClass(classRef);		
 		if (beanClass == null)
 			throw new RuntimeException("PersistenceSession: no bean class found");
 		
 		IPersistenceBean<?> bean = readBeanFromString(content, beanClass);
 		notifyObservers(bean);
 		
 		return classRef.cast(bean.get(classRef, this));		
 	}
 	
 	@Override
 	@SuppressWarnings("unchecked")
 	public String writeObject(Object obj) {
 		if (obj == null)
 			throw new IllegalArgumentException("PersistenceSession: cannot write a null object");
 
 		// Throws an exception if fails
 		IPersistenceBean<Object> bean = (IPersistenceBean<Object>) refResolver.createBean(obj.getClass());
 
 		bean.set(obj, this);
 	
 		notifyObservers(bean);
 		return writeBeanToString(bean);
 	}
 }
