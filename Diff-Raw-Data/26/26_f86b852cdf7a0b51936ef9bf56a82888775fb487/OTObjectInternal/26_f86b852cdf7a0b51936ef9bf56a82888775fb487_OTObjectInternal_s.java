 package org.concord.otrunk;
 
 import java.lang.ref.WeakReference;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.concord.framework.otrunk.OTChangeEvent;
 import org.concord.framework.otrunk.OTChangeListener;
 import org.concord.framework.otrunk.OTID;
 import org.concord.framework.otrunk.OTObject;
 import org.concord.framework.otrunk.OTObjectCollection;
 import org.concord.framework.otrunk.OTObjectInterface;
 import org.concord.framework.otrunk.OTObjectList;
 import org.concord.framework.otrunk.OTObjectMap;
 import org.concord.framework.otrunk.OTObjectService;
 import org.concord.framework.otrunk.OTResourceList;
 import org.concord.framework.otrunk.OTResourceMap;
 import org.concord.framework.otrunk.otcore.OTClass;
 import org.concord.framework.otrunk.otcore.OTClassProperty;
 import org.concord.framework.otrunk.otcore.OTType;
 import org.concord.otrunk.datamodel.BlobResource;
 import org.concord.otrunk.datamodel.OTDataCollection;
 import org.concord.otrunk.datamodel.OTDataList;
 import org.concord.otrunk.datamodel.OTDataMap;
 import org.concord.otrunk.datamodel.OTDataObject;
 import org.concord.otrunk.overlay.CompositeDataObject;
 import org.concord.otrunk.view.OTConfig;
 
 public class OTObjectInternal implements OTObjectInterface
 {
 	public final static boolean traceListeners = OTConfig.getBooleanProp(
             OTConfig.TRACE_LISTENERS_PROP, false);
 
 	public final static String LISTENER_THROWABLE_MESSAGE =
 		"Throwable thrown by an OTObjecListener, other listeners might not be notified";
 	
 	protected OTObjectServiceImpl objectService;
 
 	/**
 	 * This is null unless there are actually listeners added this saves some 
 	 * memory.
 	 */
     ArrayList<WeakReference<OTChangeListener>> changeListeners = null;
 
     /**
      * This is for debugging purposes it contains a mapping from
      * the weak reference object to the toString of the listener it
      * referenced.  This way when the listener is gc'd we can printout
      * its "label" (toString value).
      */
     Map<WeakReference<OTChangeListener>, String> changeListenerLabels;
     
     /**
      * This can be used by a user of an object to turn off the listening
      * 
      */
     protected boolean doNotifyListeners = true;
     
     protected OTObject changeEventSource;
 
 	protected OTDataObject dataObject;
 	protected OTClass otClass;
 	
 	private String changeEventSourceInstanceID;
 
 	private int hashCode;
 
 	private HashMap<String, Object> referencedObjects;
 	
 	public OTObjectInternal()
 	{		
 	}
 	
 	public OTObjectInternal(OTDataObject dataObject, OTObjectServiceImpl objectService, OTClass otClass)
     {
 		setup(dataObject, objectService, otClass);
     }	
 
 	public void setup(OTObjectInternal objInternal)
 	{
 		setup(objInternal.dataObject, objInternal.objectService, objInternal.otClass);
 	}
 	
 	public void setup(OTDataObject dataObject, OTObjectServiceImpl objectService, OTClass otClass)
 	{
 		this.objectService = objectService;
     	this.dataObject = dataObject;
     	
 		String str = getOTClassName() + "@" +  getGlobalId().hashCode();
 		hashCode = str.hashCode();
 
 		if(otClass == null){
 			throw new IllegalStateException("otClass cannot be null");
 		}
 		this.otClass = otClass;		
 	}
 	
 	/* (non-Javadoc)
      * @see org.concord.otrunk.OTObjectInternal#getOTObjectService()
      */
     public OTObjectService getOTObjectService()
     {
         return objectService;    	
     }
     
 	/* (non-Javadoc)
      * @see org.concord.otrunk.OTObjectInternal#setEventSource(org.concord.framework.otrunk.OTObject)
      */
     public void setEventSource(OTObject src)
     {
     	changeEventSource = src;
     	changeEventSourceInstanceID = Integer.toHexString(System.identityHashCode(changeEventSource));
     }
 
 	/* (non-Javadoc)
      * @see org.concord.otrunk.OTObjectInternal#setDoNotifyListeners(boolean)
      */
 	public void setDoNotifyChangeListeners(boolean doNotify)
 	{
 	    doNotifyListeners = doNotify;
 	}
 	
     /* (non-Javadoc)
      * @see org.concord.otrunk.OTObjectInternal#notifyOTChange(java.lang.String, java.lang.String, java.lang.Object)
      */
     public void notifyOTChange(String property, String operation, 
     	Object value, Object previousValue)
     {
     	if(!doNotifyListeners || changeListeners == null){
     		return;
     	}
 
     	ArrayList<WeakReference<OTChangeListener>> toBeRemoved = null;
 
     	OTChangeEvent changeEvent = new OTChangeEvent(changeEventSource);
 
     	changeEvent.setProperty(property);
     	changeEvent.setOperation(operation);
     	changeEvent.setValue(value);
     	changeEvent.setPreviousValue(previousValue);
     	
     	try {
 
     		for(int i=0;i<changeListeners.size(); i++){
    			WeakReference<OTChangeListener> ref = changeListeners.get(i);
     			OTChangeListener listener = ref.get();
     			if(traceListeners && !(listener instanceof InternalListener)){
     				System.out.println("sending stateChanged " + changeEvent.getDescription() +
     						" to " + listener);
     			}
     			if(listener != null) {
     				listener.stateChanged(changeEvent);
     			} else {
     				// the listener was gc'd so lets mark it to be removed
     				if(toBeRemoved == null) {
     					toBeRemoved = new ArrayList<WeakReference<OTChangeListener>>();
     				}
     				if(traceListeners){
     					System.out.println("otChangeListener garbage collected:" +
     							changeListenerLabels.get(ref));
     				}
     				toBeRemoved.add(ref);
     			}
     		}
 
     	// The Errors and runtime exceptions are printed out here because the code which caused
     	// this change event might not expect an exception and might be silently catching exceptions
     	// for its own reasons. 
     	} catch (Error err) {
     		System.err.println(LISTENER_THROWABLE_MESSAGE);
     		err.printStackTrace();
     		throw(err);
     	} catch (RuntimeException exp) {
     		System.err.println(LISTENER_THROWABLE_MESSAGE);
     		exp.printStackTrace();
     		throw(exp);
     	}
     		    	
     	if(toBeRemoved != null) {
     		for(int i=0; i<toBeRemoved.size(); i++) {
     			changeListeners.remove(toBeRemoved.get(i));
     		}
     	}
		if(changeListeners.size() == 0){
			changeListeners = null;
		}
     }
 
 	/* (non-Javadoc)
      * @see org.concord.otrunk.OTObjectInternal#addOTChangeListener(org.concord.framework.otrunk.OTChangeListener)
      */
 	public void addOTChangeListener(OTChangeListener changeListener)
     {
 		if(changeListener == null){
 			throw new IllegalArgumentException("changeListener cannot be null");
 		}
 
 		if(changeListeners == null){
 			changeListeners = new ArrayList<WeakReference<OTChangeListener>>();
 		}
 		
 		// Check if the changeListener has already been added 
 	    for(int i=0; i<changeListeners.size(); i++) {
 	        WeakReference<OTChangeListener> ref = changeListeners.get(i);
 	        if(changeListener == ref.get()) {
 	        	// this changeListener has already been added 
 	        	return;
 	        }
 	    }		
 		
 	    WeakReference<OTChangeListener> listenerRef = new WeakReference<OTChangeListener>(changeListener);
 	    changeListeners.add(listenerRef);
 	    
 	    // debugging instrumentation
 	    // ignore instances of the tracelistener
 	    if(traceListeners &&
 	    		!(changeListener instanceof InternalListener)){
 	    	System.out.println("addOTChangeListener(obj:" + changeEventSource + ","); 
 	    	System.out.println("   listener:" + changeListener+")");
 
 	    	if(changeListenerLabels == null){
 	    		changeListenerLabels = new HashMap<WeakReference<OTChangeListener>, String>();
 	    		// the ("" + obj) is used in case the changeListener is null 
 	    		changeListenerLabels.put(listenerRef, "" + changeListener);
 	    	}
 	    }
     }
 
 	/* (non-Javadoc)
      * @see org.concord.otrunk.OTObjectInternal#removeOTChangeListener(org.concord.framework.otrunk.OTChangeListener)
      */
 	public void removeOTChangeListener(OTChangeListener changeListener)
 	{
 	    if(traceListeners){
 	    	System.out.println("removeOTChangeListener(obj:" + changeEventSource + ",");
 	    	System.out.println("   listener:" + changeListener);
 	    }
 
 	    if(changeListeners == null){
 	    	System.err.println("Warning: trying to remove a listener that hasn't been added: " + 
 	    			changeListener);
 	    	return;
 	    }
 	    
 	    // param OTChangeListener listener
 	    for(int i=0; i<changeListeners.size(); i++) {
 	        WeakReference<OTChangeListener> ref = changeListeners.get(i);
 	        if(changeListener == ref.get()) {
 	            changeListeners.remove(i);
 	            // if we don't break right away then the "i" variable will skip over the 
 	            // the next value of the list. 
 	            break;
 	        }
 	    }
	    if(changeListeners.size() == 0) {
	    	changeListeners = null;
	    }
 	}
 
     public OTObject getOTObject(OTID childID) throws Exception
     {
     	return objectService.getOTObject(childID);
     }
 
     /* (non-Javadoc)
      * @see org.concord.otrunk.OTObjectInternal#setResource(java.lang.String, java.lang.Object)
      */
     public boolean setResource(String name, Object value)
 	{
     	Object dataValue = value;
 		if(value instanceof OTObject) {
 			OTObject child = (OTObject)value;
 			OTID childId = child.getGlobalId();
 			
 			saveReference(name, child);
 			dataValue = childId;
 		} else if(value instanceof byte[]) {
 			dataValue = new BlobResource((byte[])value);
 		} else if(value instanceof URL){
 			dataValue = new BlobResource((URL)value);
 		} 
 		
 		// if the value is a BlobResource itself then it will pass right though to here
 		
 		// Check to see if it is equal before we go further
 	    Object oldValue = getResourceValue(name);
 	    if(oldValue != null && oldValue.equals(dataValue)){
 	        return false;
 	    }
 
 		// setResource should only return true if the dataObject was 
 		// actually changed with this call
 		if(setResourceInternal(name, dataValue)){
 			if(oldValue instanceof OTID){
 				// Handle the case where someone set a object reference to null
 				if(value == null){
 					saveReference(name, null);
 				}
 
 				try {
 	                oldValue = getOTObject((OTID) oldValue);
                 } catch (Exception e) {
 	                e.printStackTrace();	               
                 }
 			}			
 			notifyOTChange(name, OTChangeEvent.OP_SET, value, oldValue);			
 		}
 		
 		return true;
 	}
    
     public OTID getGlobalId()
     {
     	return dataObject.getGlobalId();
     }
 
 	public boolean isResourceSet(String resourceName)
     {		
 		OTClassProperty property = otClass().getProperty(resourceName);
 		if(property == null){
 			throw new IllegalStateException("Property: " + resourceName + " doesn't exist on OTClass: " + otClass().getName());
 		}
 		return otIsSet(property);
     }
 
 	public Object getResourceChecked(String resourceName, Class<?> returnType)
 	{
 		try {
 	        return getResource(resourceName, returnType);
         } catch (Exception e) {
         	throw new RuntimeException(e);
         }
 	}
 	
 	public Object getResource(String resourceName, Class<?> returnType)
 		throws Exception
 	{
 		OTClassProperty classProperty = otClass().getProperty(resourceName);
 		return getResource(classProperty, returnType);
 	}
 	
 	public Object getResource(String resourceName)
 		throws Exception
 	{
 			return getResource(resourceName, null);
 	}
 	
 	public Object getResourceChecked(OTClassProperty property, Class<?> returnType)	
 	{
 		try {
 	        return getResource(property, returnType);
         } catch (Exception e) {
         	throw new RuntimeException(e);
         }
 	}
 	
 	public Object getResource(OTClassProperty property, Class<?> returnType)
 		throws Exception
 	{
 		Object value = getResourceInternal(property, returnType);
 		
 		// If the resource value is an OTObject save a reference to it
 		// if this is is an overridden property then don't save it 
 		if(!property.isOverriddenProperty() && (value instanceof OTObject || value instanceof OTObjectCollection)){
 			String propertyName = property.getName();
 			saveReference(propertyName, value);
 		}
 		return value;
 	}
 	
 	public Object getResourceInternal(OTClassProperty property, Class<?> returnType)
 		throws Exception
 	{
 		String resourceName = property.getName();
 		OTType type = property.getType();
 		if(returnType == null){
 			returnType = type.getInstanceClass();
 		}
 
 		
         // If this class is the one handling the overlays then this call
         // would be substituted by one that goes through all of the overlayed data objects.
 	    Object resourceValue = null;
 	    Object overriddenValue = null;
 	    if(property.isOverriddenProperty()){
 	        if(dataObject instanceof CompositeDataObject) {        	
 	            Object nonActiveDeltaResource = 
 	            	((CompositeDataObject)dataObject).getNonActiveDeltaResource(resourceName);
 
 	            resourceValue = nonActiveDeltaResource;
 	            overriddenValue = resourceValue;
 	        } else {
 	        	System.err.println("Warning: this object isn't from an Overlay");
 		    	resourceValue = getResourceValue(resourceName);
 	        }    	    
 	    } else {
 	    	resourceValue = getResourceValue(resourceName);
 	    }
 	    
 	    // we can't rely on the returnType here because it could be an
 	    // interface that isn't in the ot package
 	    if(resourceValue instanceof OTID){
 	        OTObject object;
 	        try {
 	            if(resourceValue == null) {
 	                return null;
 	            }
 	            OTID objId = (OTID)resourceValue;
 	            
 	            object = getOTObject(objId);
 	            
 	            if(object != null){
 	            	if(!returnType.isAssignableFrom(object.getClass())){
 	            		System.err.println("Error: Type Mismatch");
 	            		System.err.println("  value: " + object);
 	            		System.err.println("  parentObject: " + otClass.getName());
 	            		System.err.println("  resourceName: " + resourceName);
 	        	        System.err.println("  expected type is: " + returnType);
 	            		return null;
 	            	}
 	            }
 	            
 	            return object;
 	        } catch (Exception e)
 	        {
 	            e.printStackTrace();
 	        }		
 	        
 	        return null;
 	        
 	    } else if(OTResourceMap.class.isAssignableFrom(returnType)) {
 	        try {
 	        	OTDataMap list = getResourceCollection(resourceName, OTDataMap.class, 
 	        		overriddenValue);
 	            return new OTResourceMapImpl(resourceName, list, this);
 	        } catch (Exception e) {
 	            e.printStackTrace();
 	        }
 	        
 	        return null;
 	    } else if(OTObjectMap.class.isAssignableFrom(returnType)) {
 	        try {
 	        	OTDataMap list = getResourceCollection(resourceName, OTDataMap.class, 
 	        		overriddenValue);
 	            return new OTObjectMapImpl(resourceName, list, this);
 	        } catch (Exception e) {
 	            e.printStackTrace();
 	        }
 	        
 	        return null;				
 	    } else if(OTResourceList.class.isAssignableFrom(returnType)) {
 	        try {
 	        	OTDataList list = getResourceCollection(resourceName, OTDataList.class, 
 	        		overriddenValue);
 	            return new OTResourceListImpl(resourceName, list, this);
 	        } catch (Exception e) {
 	            e.printStackTrace();
 	        }
 	        
 	        return null;				
 	    } else if(OTObjectList.class.isAssignableFrom(returnType)) {
 	        try {					
 	        	OTDataList list = getResourceCollection(resourceName, OTDataList.class, 
 	        		overriddenValue);
 	            return new OTObjectListImpl(resourceName, list, this);
 	        } catch (Exception e) {
 	            e.printStackTrace();
 	        }
 	        
 	        return null;	
 	    } else if(resourceValue instanceof BlobResource) {
 	    	BlobResource blob = (BlobResource)resourceValue;
 	    	if(returnType == byte[].class){
 	    		return blob.getBytes();
 	    	} else if(returnType == URL.class){
 	    		return blob.getBlobURL();
 	    	} else {
 	    		return blob;
 	    	}
 	    } else if(resourceValue == null && 
 	    		(returnType == String.class || returnType.isPrimitive() ||
 	    				returnType == Boolean.class || returnType == Short.class ||
 	    				returnType == Character.class || returnType == Integer.class ||
 	    				returnType == Float.class || returnType == Double.class ||
 	    				returnType == Long.class || Enum.class.isAssignableFrom(returnType))) {
 	    	Object defaultValue = property.getDefault();
 	    	if(defaultValue == null && 
 	    			returnType != String.class && 
 	    			!Enum.class.isAssignableFrom(returnType)){
 	    		throw new RuntimeException("No default value set for \"" + resourceName + "\" " +
 	    				"in class: " + otClass.getName());	        		
 	    	}
 	    	
 	    	return defaultValue;
 	    }
 	    
 	    if(resourceValue == null) return null;
 	    
 	    if(!returnType.isInstance(resourceValue) &&
 	            !returnType.isPrimitive()){
 	        System.err.println("invalid resource value for: " + resourceName);
 	        System.err.println("  object type: " + otClass.getName());
 	        System.err.println("  resourceValue is: " + resourceValue.getClass());
 	        System.err.println("  expected type is: " + returnType);
 	        return null;
 	    }
 	    
 	    return resourceValue;	    
 	}
 
 	public Object getResourceValue(String resourceName)
     {
     	return dataObject.getResource(resourceName);
     }
 
 	@SuppressWarnings("unchecked")
     public <T extends OTDataCollection> T getResourceCollection(String resourceName, 
 		Class<T> collectionClass, Object overriddenValue)
 	{
     	if(overriddenValue != null){
     		return (T)overriddenValue;
     	} else {
     		return dataObject.getResourceCollection(resourceName, collectionClass);
     	}		
 	}
 	
 	public OTResourceMap getResourceMap(String resourceName)
     {
         OTDataMap map = (OTDataMap)dataObject.getResourceCollection(
                 resourceName, OTDataMap.class);
         return new OTResourceMapImpl(resourceName, map, this);
     }
 
 	public OTObjectMap getObjectMap(String resourceName)
     {
     	OTDataMap map = (OTDataMap)dataObject.getResourceCollection(
     			resourceName, OTDataMap.class);
     	return new OTObjectMapImpl(resourceName, map, this);
     }
 
 	public OTResourceList getResourceList(String resourceName)
     {
         OTDataList list = (OTDataList)dataObject.getResourceCollection(
                 resourceName, OTDataList.class);
         return new OTResourceListImpl(resourceName, list, this);
     }
 
 	public OTObjectList getObjectList(String resourceName, Object overriddenValue)
     {		
     	OTDataList list = 
     		(OTDataList) getResourceCollection(resourceName, OTDataList.class, overriddenValue);
         return new OTObjectListImpl(resourceName, list, this);
     }
 
 	public boolean setResourceInternal(String name, Object value)
     {
     	return dataObject.setResource(name, value);
     }
 
 	public String getOTClassName()
     {
     	return OTrunkImpl.getClassName(dataObject);
     }
 
 	public String internalToString()
 	{
 		String otObjectIDStr = "";
 		if(changeEventSourceInstanceID != null){
 			otObjectIDStr = "@" + changeEventSourceInstanceID;
 		}
 		return getOTClassName() + "#" +  getGlobalId() + otObjectIDStr;
 	}
 	
 	public int internalHashCode()
 	{
 		return hashCode;
 	}
 	
 	public boolean internalEquals(Object other)
 	{
 		if(!(other instanceof OTObject)){
 			return false;
 		}
 		
 		if(changeEventSource == other) {
 			return true;
 		}
 		
 		if(((OTObject)other).getGlobalId().equals(getGlobalId())) {
 			System.err.println("compared two ot objects with the same ID but different instances");
 			return true;
 		}
 		return false;
 	}
 	
 	protected void saveReference(String key, Object value)
 	{
 		if(referencedObjects == null){
 			referencedObjects = new HashMap<String, Object>();
 		}
 		referencedObjects.put(key, value);
 	}
 	
 	protected void finalize()
 	throws Throwable
 	{
 		if(OTConfig.isTrace()){
 			System.out.println("finalizing object: " + internalToString());
 		}
 		if(traceListeners){
 			if(changeListeners != null){
 				// Check for the case where there is just the TraceListener
 				if(changeListeners.size() == 1){
 					WeakReference<OTChangeListener> ref = changeListeners.get(0);
 					OTChangeListener listener = ref.get();
 					if(listener instanceof InternalListener){
 						// don't print anything here
 						return;
 					}
 				}
 
 				System.out.println("listeners on finalized object: " + internalToString());
 				for(int i=0; i<changeListeners.size(); i++){
 					WeakReference<OTChangeListener> ref = changeListeners.get(i);
 					OTChangeListener listener = ref.get();
 					if(listener instanceof InternalListener){
 						// skip the trace listener
 						continue;
 					}
 					System.out.println("  " + listener);
 				}
 			} 
 		}
 	}
 
 	public OTClass otClass()
     {
 		return otClass;
     }
 
 	public String getName()
     {
 		return (String) getResourceValue("name");
     }
 
 	public void setName(String name)
     {
 		setResource("name", name);
     }
 	public void init()
     {
 		// do nothing on init.
     }
 
 	public String getLocalId()
     {		
 		throw new UnsupportedOperationException("should not be called");
     }
 
 	public Object otGet(OTClassProperty property)
     {		
 		try {
 	        return getResource(property, null);
         } catch (Exception e) {
 	        // TODO Auto-generated catch block
 	        e.printStackTrace();
         }
         return null;
     }
 
 	public boolean otIsSet(OTClassProperty property)
     {    	
 		if(property.isOnlyInOverlayProperty()){
 			if(dataObject instanceof CompositeDataObject) {     	
 				return ((CompositeDataObject)dataObject).hasOverrideInTopOverlay(property.getName());
 			} else {
 				System.err.println("Warning: this object isn't from an Overlay");
 			}
 		}
 		
         Object resourceValue = dataObject.getResource(property.getName());
         return resourceValue != null;
     }
 
 	public void otSet(OTClassProperty property, Object newValue)
     {
 		// FIXME should probably do some type checking here
 		setResource(property.getName(), newValue);
     }
 
 	public void otUnSet(OTClassProperty property)
     {
 		/*
 		boolean isOnlyInOverlayProperty();
 		
 		public boolean isOverridenProperty();
 		
 		public OTClassProperty getOnlyInOverlayProperty();
 		
 		public OTClassProperty getOverridenProperty();
 		*/
 
 		if(property.isOnlyInOverlayProperty()){	    	
 	        if(dataObject instanceof CompositeDataObject) {	        	
 	            ((CompositeDataObject)dataObject).removeOverrideInTopOverlay(property.getName());
 	            return;
 	        } else {
 	        	System.err.println("Warning: this object isn't from an Overlay");
 	        }    		
 		}
 		
 		setResource(property.getName(), null);
     }
 
 	public String otExternalId()
     {
 		return objectService.getExternalID(changeEventSource);
     }
 
 	public ArrayList<WeakReference<OTChangeListener>> getOTChangeListeners()
     {
 	    return changeListeners;
     }
 }
