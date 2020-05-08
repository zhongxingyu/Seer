 package org.eclipse.jem.internal.beaninfo.vm;
 
 /*******************************************************************************
  * Copyright (c)  2001, 2003 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 /*
  *  $RCSfile: ModelingBeanInfo.java,v $
 *  $Revision: 1.4 $  $Date: 2005/02/09 21:10:09 $ 
  */
 
 import java.beans.*;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.lang.reflect.*;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.*;
 
 import org.eclipse.jem.beaninfo.vm.BaseBeanInfo;
 import org.eclipse.jem.internal.beaninfo.common.*;
 import org.eclipse.jem.internal.proxy.common.*;
 
 /**
  * This class is the beaninfo class that is created when beaninfo modeling introspects a bean. Its purpose is to gather together and analyze the
  * beaninfo. For example, it checks with the supertype beaninfo to see if all of the supertype's descriptors are included in this list. If they are,
  * then it knows that it does a straight inheritance of the supertype's descriptors, and those descriptors can be removed from the list. This makes it
  * easier on the model side so that there isn't a great proliferation of descriptors all describing the same properties. In that case they can be
  * merged from the supertype model. If some are not found, then that means this beaninfo is trying to hide some of them, and in that case this is the
  * definitive list and inheritance shouldn't be used on the model side. However, for those features which are essentially the inherited feature (i.e.
  * the BeanInfo simply filtered out some inherited but not all), they will be returnable (by name). The IDE side will take these that are "inherited"
  * and will return the actual structured feature from the inherited class.
  * 
  * The test for seeing if the super feature is included in the beaninfo is first to see if the the feature is in the beaninfo by name, if it is then
  * it is marked as included. Then a check is made on equality, if they are equal, then the feature is removed from the beaninfo list, but the merge
  * flag is still left on, and removed inherited feature is added to the list of inherited features. If all inherited features are found, this list is
  * cleared and flag is set which simply says merge all inherited. This allows merging to occur but it also allows overrides to occur.
  * 
  * Note: Test for identity is different between JDK 1.5 and above and below. 1.5 and above, we can use actual equals() because the same descriptor is
  * returned from inherited features. In 1.3, clones were always returned and equals() would answer false, so we need to create a special equality
  * descriptor which turns the equals() into one that can test clones for semantic equality. See Java Bug ID#4996673 The problem was supposed to be
  * fixed in 1.4 but it was not fixed. Originally a new clone was created each time a beaninfo was requested. That is no longer done in 1.4, but the
  * processing to create the original beaninfo does a clone accidently under the covers. Looking at 1.5 it looks this was fixed, but it hasn't been
  * tested here yet.
  */
 
 public abstract class ModelingBeanInfo implements ICallback {
 
 	private static boolean PRE15;
 	static {
 		String version = System.getProperty("java.version", ""); //$NON-NLS-1$ //$NON-NLS-2$
 		PRE15 = version.startsWith("1."); //$NON-NLS-1$
 		if (PRE15) {
 			// Continue to check, get the revision.
 			int revision = 0;
 			if (version.length() > 2) {
 				int revEnd = version.indexOf('.', 2);
 				revision = version.length() > 2 ? Integer.parseInt(revEnd != -1 ? version.substring(2, revEnd) : version.substring(2)) : 0;
 				PRE15 = revision < 5;
 			}
 		}
 	}
 
 	static class FeatureEqualitySet extends HashSet {
 
 		private FeatureDescriptorEquality workingKey;
 
 		public FeatureEqualitySet(List features) {
 			super(features.size());
 			// Get first feature to fiqure out type of working key. This will not be reentrant.
 			workingKey = FeatureDescriptorEquality.createEquality((FeatureDescriptor) features.get(0));
 			this.addAll(features);
 		}
 
 		/**
 		 * @see java.util.Collection#add(Object)
 		 */
 		public boolean add(Object o) {
 			return super.add(FeatureDescriptorEquality.createEquality((FeatureDescriptor) o));
 		}
 
 		/**
 		 * @see java.util.Collection#contains(Object)
 		 */
 		public boolean contains(Object o) {
 			workingKey.setFeature((FeatureDescriptor) o);
 			return super.contains(workingKey);
 		}
 
 	}
 
 	// The following fields indicate if the super info should be merged
 	// in on the model side. no merge means there were no inherited methods at all. So the
 	// beaninfo presented is definitive. If merge, then get...Descriptors will return just
 	// the ones for this level, and getSuper...Descriptors will return the inherited ones.
 	// Though in this case, if the returned super list is null, then that means use ALL of
 	// the inherited ones.
 	// The super list returns simply the names, don't need to have the full descriptors in that case.
 	protected boolean fMergeInheritedEvents = false, fMergeInheritedMethods = false, fMergeInheritedProperties = false;
 
 	protected final BeanInfo fTargetBeanInfo; // The beaninfo being modeled.
 
 	// Local descriptors
 	protected EventSetDescriptor[] fEventSets;
 
 	protected MethodDescriptor[] fMethods;
 
 	protected PropertyDescriptor[] fProperties;
 
 	// Not inherited descriptor names, will be null if no merge or if merge all. This is list of names to NOT merge. It is usually shorter than the list to merge from super.
 
 	// Methods are special. You can have duplicates, so name is not sufficient.
 	// So for methods,
 	// will use an array of Strings where:
 	//   For each one the full signature
 	//   will be in the list, e.g. "name:methodName(argtype,..." where argtype is the fullyqualified
 	//   classname (using "." notation for inner classes), and using format "java.lang.String[]" for
 	//   arrays.
 	//
 	// This is because even if there are no duplicates, it will take less time/space to simply create the entries
 	// then it would to create a set to see if there are duplicates and then create the final array.
 	protected String[] fNotInheritedEventSets;
 
 	protected String[] fNotInheritedMethods;
 
 	protected String[] fNotInheritedProperties;
 	
 	protected int doFlags;
 
 	/**
 	 * Method used to do introspection and create the appropriate ModelingBeanInfo
 	 * 
 	 * This will always introspect.
 	 */
 	public static ModelingBeanInfo introspect(Class introspectClass, int doFlags) throws IntrospectionException {
 		return introspect(introspectClass, true, doFlags);
 	}
 
 	/**
 	 * Method used to do introspection and create the appropriate ModelingBeanInfo
 	 * 
 	 * introspectIfNoBeanInfo: If this is true, then if no explicit beaninfo was found for this class, then introspection will be done anyway. The
 	 * Introspector will use reflection for local methods/events/properties of this class and then add in the results of the superclass introspection.
 	 * If this parameter is false, then if the explicit beaninfo is not found, then no introspection will be done and null will be returned.
 	 */
 	public static ModelingBeanInfo introspect(Class introspectClass, boolean introspectIfNoBeanInfo, int doFlags) throws IntrospectionException {
 		if (!introspectIfNoBeanInfo) {
 			// Need to check if the beaninfo is explicitly supplied.
 			// If not, then we return null.
 			// The checks will be the same that Introspector will use.
 
 			boolean found = false;
 			// 1. Is there a BeanInfo class in the same package
 			if (!classExists(introspectClass.getName() + "BeanInfo", introspectClass)) { //$NON-NLS-1$
 				// 2. Is this class a BeanInfo class for itself.
 				if (!(BeanInfo.class).isAssignableFrom(introspectClass)) {
 					// 3. Can this class be found in the Beaninfo searchpath.
 					String[] searchPath = Introspector.getBeanInfoSearchPath();
 					int startClassname = introspectClass.getName().lastIndexOf(".") + 1; //$NON-NLS-1$
 					String biName = "." + introspectClass.getName().substring(startClassname) + "BeanInfo"; //$NON-NLS-1$ //$NON-NLS-2$
 					for (int i = 0; i < searchPath.length; i++) {
 						if (classExists(searchPath[i] + biName, introspectClass)) {
 							found = true;
 							break;
 						}
 					}
 				} else
 					found = true;
 			} else
 				found = true;
 
 			if (!found)
 				return null;
 		}
 
 		BeanInfo bInfo = Introspector.getBeanInfo(introspectClass);
 		Class superClass = introspectClass.getSuperclass();
 
 		if (superClass == null)
 			return PRE15 ? (ModelingBeanInfo) new ModelingBeanInfoPre15(bInfo, doFlags) : new ModelingBeanInfo15(bInfo, doFlags);
 		else
 			return PRE15 ? (ModelingBeanInfo) new ModelingBeanInfoPre15(bInfo, Introspector.getBeanInfo(superClass), doFlags) : new ModelingBeanInfo15(bInfo,
 					Introspector.getBeanInfo(superClass), doFlags);
 	}
 
 	/**
 	 * See if this class exists, first in the class loader of the sent class, then in the system loader, then the bootstrap loader, and finally the
 	 * current thread context class loader.
 	 */
 	protected static boolean classExists(String className, Class fromClass) {
 		if (fromClass.getClassLoader() != null)
 			try {
 				fromClass.getClassLoader().loadClass(className);
 				return true;
 			} catch (ClassNotFoundException e) {
 			}
 		if (ClassLoader.getSystemClassLoader() != null)
 			try {
 				ClassLoader.getSystemClassLoader().loadClass(className);
 				return true;
 			} catch (ClassNotFoundException e) {
 			}
 		try {
 			Class.forName(className);
 			return true;
 		} catch (ClassNotFoundException e) {
 		}
 
 		try {
 			// Use the classloader from the current Thread.
 			ClassLoader cl = Thread.currentThread().getContextClassLoader();
 			cl.loadClass(className);
 			return true;
 		} catch (ClassNotFoundException e) {
 		}
 
 		return false;
 
 	}
 
 	/**
 	 * Used only for Object since that is the only bean that doesn't have a superclass. Superclass beaninfo required for all other classes. If this is
 	 * constructed then this means no merge and the list is definitive.
 	 */
 	protected ModelingBeanInfo(BeanInfo beanInfo, int doFlags) {
 		fTargetBeanInfo = beanInfo;
 		this.doFlags = doFlags;
 	}
 
 	protected ModelingBeanInfo(BeanInfo beanInfo, BeanInfo superBeanInfo, int doFlags) {
 		this(beanInfo, doFlags);
 
 		// Now go through the beaninfo to determine the merge state.
 		// The default is no merge.
 
 		if ((doFlags & IBeanInfoIntrospectionConstants.DO_EVENTS) != 0) {
 			List full = addAll(beanInfo.getEventSetDescriptors());
 			List inherited = addAll(superBeanInfo.getEventSetDescriptors());
 
 			fMergeInheritedEvents = stripList(full, inherited);
 			if (fMergeInheritedEvents) {
 				if (!full.isEmpty())
 					fEventSets = (EventSetDescriptor[]) full.toArray(new EventSetDescriptor[full.size()]);
 				if (!inherited.isEmpty())
 					createEventArray(inherited); // This is actually a list of those NOT inherited.
 			}
 		}
 
 		if ((doFlags & IBeanInfoIntrospectionConstants.DO_METHODS) != 0) {
 			List full = addAll(beanInfo.getMethodDescriptors());
 			List inherited = addAll(superBeanInfo.getMethodDescriptors());
 
 			fMergeInheritedMethods = stripList(full, inherited);
 			if (fMergeInheritedMethods) {
 				if (!full.isEmpty())
 					fMethods = (MethodDescriptor[]) full.toArray(new MethodDescriptor[full.size()]);
 				if (!inherited.isEmpty())
 					createMethodEntries(inherited); // This is actually a list of those NOT inherited.
 			}
 		}
 
 		if ((doFlags & IBeanInfoIntrospectionConstants.DO_PROPERTIES) != 0) {
 			List full = addAll(beanInfo.getPropertyDescriptors());
 			List inherited = addAll(superBeanInfo.getPropertyDescriptors());
 
 			fMergeInheritedProperties = stripList(full, inherited);
 			if (fMergeInheritedProperties) {
 				if (!full.isEmpty())
 					fProperties = (PropertyDescriptor[]) full.toArray(new PropertyDescriptor[full.size()]);
 				if (!inherited.isEmpty())
 					createPropertyArray(inherited); // This is actually a list of those NOT inherited.
 			}
 		}
 	}
 
 	protected void createEventArray(List features) {
 		fNotInheritedEventSets = createDescriptorNames(features);
 	}
 
 	protected void createMethodEntries(List features) {
 		int s = features.size();
 		fNotInheritedMethods = new String[s];
 		for (int i = 0; i < s; i++) {
 			fNotInheritedMethods[i] = longName((MethodDescriptor) features.get(i));
 		}
 	}
 
 	protected String longName(MethodDescriptor md) {
 		String n = md.getName();
 		StringBuffer sb = new StringBuffer(n.length() + 20);
 		sb.append(n);
 		sb.append(':');
 		Method m = md.getMethod();
 		sb.append(m.getName());
 		sb.append('(');
 		Class[] parms = m.getParameterTypes();
 		for (int j = 0; j < parms.length; j++) {
 			if (j > 0)
 				sb.append(',');
 			if (!parms[j].isArray())
 				sb.append(parms[j].getName().replace('$', '.'));
 			else {
 				Class finalType = parms[j].getComponentType();
 				int insrt = sb.length();
 				while (finalType.isArray()) {
 					sb.append("[]"); //$NON-NLS-1$
 					finalType = finalType.getComponentType();
 				}
 				sb.insert(insrt, finalType.getName().replace('$', '.'));
 			}
 		}
 		return sb.toString();
 	}
 
 	protected void createPropertyArray(List features) {
 		fNotInheritedProperties = createDescriptorNames(features);
 	}
 
 	protected String[] createDescriptorNames(List features) {
 		String[] result = new String[features.size()];
 		for (int i = 0; i < result.length; i++) {
 			result[i] = ((FeatureDescriptor) features.get(i)).getName();
 		}
 		return result;
 	}
 
 	protected List addAll(Object[] set) {
 		if (set != null) {
 			ArrayList l = new ArrayList(set.length);
 			for (int i = 0; i < set.length; i++) {
 				l.add(set[i]);
 			}
 			return l;
 		} else
 			return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * If this returns true, then all of the super class's events should be merged in. If it returns false, then the events returned are it, there are
 	 * no others.
 	 */
 	public boolean isMergeInheritedEvents() {
 		return fMergeInheritedEvents;
 	}
 
 	/**
 	 * If this returns true, then all of the super class's methods should be merged in. If it returns false, then the methods returned are it, there
 	 * are no others.
 	 */
 	public boolean isMergeInheritedMethods() {
 		return fMergeInheritedMethods;
 	}
 
 	/**
 	 * If this returns true, then all of the super class's properties should be merged in. If it returns false, then the properties returned are it,
 	 * there are no others.
 	 */
 	public boolean isMergeInheritedProperties() {
 		return fMergeInheritedProperties;
 	}
 
 	public BeanInfo[] getAdditionalBeanInfo() {
 		return fTargetBeanInfo.getAdditionalBeanInfo();
 	}
 
 	public BeanDescriptor getBeanDescriptor() {
 		return fTargetBeanInfo.getBeanDescriptor();
 	}
 
 	public EventSetDescriptor[] getEventSetDescriptors() {
 		return fMergeInheritedEvents ? fEventSets : fTargetBeanInfo.getEventSetDescriptors();
 	}
 
 	public java.awt.Image getIcon(int iconKind) {
 		return fTargetBeanInfo.getIcon(iconKind);
 	}
 
 	public MethodDescriptor[] getMethodDescriptors() {
 		return fMergeInheritedMethods ? fMethods : fTargetBeanInfo.getMethodDescriptors();
 	}
 
 	public PropertyDescriptor[] getPropertyDescriptors() {
 		return fMergeInheritedProperties ? fProperties : fTargetBeanInfo.getPropertyDescriptors();
 	}
 
 	public String[] getNotInheritedEventSetDescriptors() {
 		return fNotInheritedEventSets;
 	}
 
 	public String[] getNotInheritedMethodDescriptors() {
 		return fNotInheritedMethods;
 	}
 
 	public String[] getNotInheritedPropertyDescriptors() {
 		return fNotInheritedProperties;
 	}
 
 	protected String computeKey(FeatureDescriptor feature) {
 		return feature instanceof MethodDescriptor ? longName((MethodDescriptor) feature) : feature.getName();
 	}
 
 	/*
 	 * Strip the list down using the Equality objects.
 	 */
 	protected boolean stripList(List fullList, List inheritedList) {
 		// The process is to create a boolean list mirroring the inheritedList.
 		// This boolean list indicates if the corresponding (by index)
 		// entry from the inheritedList is to be retained in the final computed
 		// list.
 		//
 		// A Hashmap is created where the key is the computedKey from the inheritedList
 		// and the value is the index into the inheritedList. This is so that we can quickly determine if the
 		// entry is matching.
 		//
 		// Then the fullList will be stepped through and see if there is
 		// an entry in the Hashmap for it. If there is an entry, then
 		// the entry is checked to see if it is semantically equal.
 		// If it is, then the boolean list entry is marked so that
 		// the inheritedList entry will be retained, the fullList entry removed and the counter
 		// of the number of entries in the inheritedList copy is incremented.
 		// If they aren't semantically equal, then we know that this is
 		// an override. In that case, the fullList entry is kept, the inheritedList
 		// entry is not retained, but we don't prevent merge later.
 		//
 		// If the fullList entry is not found in the HashMap, then we know it is not
 		// from the inheritedList, so it will be retained in the fullList.
 		//
 		// If we get all of the way through, then we know that what is left
 		// in fullList is just this level.
 		//
 		// When we return we know that
 		//   a) fullList has only the features that are found at the local level
 		//   b) inheritedList if not empty contains the ones from super that SHOULD NOT be inherited.
 		//      If it is empty, then if this method returns true, then ALL should be inherited,
 		//      or if this method returns false, then it doesn't matter because we aren't merging any.
 		//
 		// All of this is based upon the assumption that the list can
 		// get quite large (which it does for javax.swing) and that
 		// a simple n-squared order search would be too slow.
 
 		if (fullList.isEmpty()) {
 			return false; // There are none in the full list, so there should be none, and don't merge.
 		} else if (inheritedList.isEmpty())
 			return false; // There are no inheritedList features, so treat as no merge.
 
 		// We have some features and some inheritedList features, so we need to go through the lists.
 
 		// Create a working copy of the FeatureDescriptorEquality for fullList and stripList and just reuse them
 		FeatureDescriptorEquality workingStrip = FeatureDescriptorEquality.createEquality((FeatureDescriptor) inheritedList.get(0));
 		FeatureDescriptorEquality workingFull = FeatureDescriptorEquality.createEquality((FeatureDescriptor) fullList.get(0));
 
 		int inheritedSize = inheritedList.size();
 		boolean[] copy = new boolean[inheritedSize];
 
 		HashMap inheritedMap = new HashMap(inheritedSize);
 		for (int i = 0; i < inheritedSize; i++) {
 			FeatureDescriptor f = (FeatureDescriptor) inheritedList.get(i);
 			String key = computeKey(f);
 			Object value = inheritedMap.get(key);
 			if (value == null)
 				inheritedMap.put(key, new Integer(i));
 			else {
 				// Shouldn't occur.
 			}
 
 		}
 
 		// When we are done with this processing, inheritedList will contain the super that should not be used, and full list will contain only the locals
 		// (those defined at this class level).;
 		int inheritedRetained = 0;
 		Iterator fullItr = fullList.iterator();
 		// Continue while we've retained less than the full super amount. If we've retained all of the inheritedList, there is no
 		// need to continue processing the fullList because there can't possibly be any inheritedList entries left to find.
 		while (inheritedRetained < inheritedSize && fullItr.hasNext()) {
 			FeatureDescriptor f = (FeatureDescriptor) fullItr.next();
 			boolean foundFull = false;
 			Object index = inheritedMap.get(computeKey(f));
 			if (index != null) {
 				workingFull.setFeature(f);
 				int ndx = ((Integer) index).intValue();
 				workingStrip.setFeature((FeatureDescriptor) inheritedList.get(ndx));
 				if (workingFull.equals(workingStrip)) {
 					// They are semantically identical, so retain in the inheritedList.
 					copy[ndx] = true;
 					foundFull = true;
 					inheritedRetained++;
 				}
 			}
 
 			if (foundFull) {
 				// We found the inheritedList entry semantically equal in the full list somewhere, so we need to remove the full entry.
 				fullItr.remove();
 			}
 		}
 
 		if (inheritedRetained == inheritedSize) {
 			inheritedList.clear(); // All were found in inheritedList, so just clear the inheritedList and return just what was left in the found.
 								   // Those in full found in super had been removed from full during the processing.
 			return true; // We merge with all inherited. 
 		} else	if (inheritedRetained != 0) {
 			// Some were retained, take out of the list those that were retained.
 			// When done the list will contain those that should be dropped from the inherited list.
 			// We start from end because the actual number of bytes moved overall will be less than if we started from the front.
 			for (ListIterator itr = inheritedList.listIterator(inheritedList.size()); itr.hasPrevious();) {
 				int i = itr.previousIndex();
 				itr.previous();	// To back up the itr so that remove can remove it. We actually don't care what the value is.
 				if (copy[i])
 					itr.remove();
 			}
 			return true;	// We merge, and the list is not empty but it did have some removed, so we leave the list alone. Those are not inherited.
 		} else
 			return false;	// All were removed (retained == 0). None were retained. So we just don't do a merge. The list will be ignored.
 	}
 
 	// The modeling beaninfo is also used to send itself back in serialized mode as a callback.
 
 	private IVMServer vmServer;
 
 	private int callbackID;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jem.internal.proxy.common.ICallback#initializeCallback(org.eclipse.jem.internal.proxy.common.IVMServer, int)
 	 */
 	public void initializeCallback(IVMServer vmServer, int callbackID) {
 		this.vmServer = vmServer;
 		this.callbackID = callbackID;
 	}
 	
 	public void send() throws IOException, CommandException {
 		if (doFlags != 0) {
 			ObjectOutputStream stream = new ObjectOutputStream(vmServer.requestStream(callbackID, 0));
 			if ((doFlags & IBeanInfoIntrospectionConstants.DO_BEAN_DECOR) != 0)
 				sendBeanDecorator(stream);
 			if ((doFlags & IBeanInfoIntrospectionConstants.DO_PROPERTIES) != 0)
 				sendPropertyDecorators(stream);
 			if ((doFlags & IBeanInfoIntrospectionConstants.DO_METHODS) != 0)
 				sendMethodDecorators(stream);
 			if ((doFlags & IBeanInfoIntrospectionConstants.DO_EVENTS) != 0)
 				sendEventDecorators(stream);
 			stream.writeInt(IBeanInfoIntrospectionConstants.DONE);
 			stream.close();
 		}
 	}
 
 	/**
 	 * Called by IDE to send the bean decorator information back through the callback.
 	 * @throws CommandException
 	 * @throws IOException
 	 * 
 	 * @since 1.1.0
 	 */
 	public void sendBeanDecorator(ObjectOutputStream stream) throws IOException, CommandException {
 		BeanRecord br = new BeanRecord();
 		BeanDescriptor bd = getBeanDescriptor();
 
 		if (bd != null) {
 			br.customizerClassName = getClassName(bd.getCustomizerClass());
 			br.mergeInheritedProperties = isMergeInheritedProperties();
 			br.mergeInheritedOperations = isMergeInheritedMethods();
 			br.mergeInheritedEvents = isMergeInheritedEvents();
 			br.notInheritedPropertyNames = getNotInheritedPropertyDescriptors();
 			br.notInheritedOperationNames = getNotInheritedMethodDescriptors();
 			br.notInheritedEventNames = getNotInheritedEventSetDescriptors();
 			fill(bd, br, BEAN_RECORD_TYPE);
 		}
 		stream.writeInt(IBeanInfoIntrospectionConstants.BEAN_DECORATOR_SENT);
 		stream.writeObject(br);
 	}
 
 	/**
 	 * Called by IDE to send the property decorators information back through the callback.
 	 * 
 	 * @throws CommandException
 	 * @throws IOException
 	 * @since 1.1.0
 	 */
 	public void sendPropertyDecorators(ObjectOutputStream stream) throws IOException, CommandException {
 		PropertyDescriptor[] properties = getPropertyDescriptors();
 		if (properties != null && properties.length > 0) {
 			// Now start writing the records.
 			stream.writeInt(IBeanInfoIntrospectionConstants.PROPERTY_DECORATORS_SENT);
 			stream.writeInt(properties.length);
 			for (int i = 0; i < properties.length; i++) {
 				PropertyDescriptor pd = properties[i];
 				// Much of the two types are common, so if indexed, fill in the index part and then pass on to property part.
 				PropertyRecord usepr = null;
 				int useType = 0;
 				if (pd.getClass() == IndexedPropertyDescriptor.class) {
 					IndexedPropertyDescriptor ipd = (IndexedPropertyDescriptor) pd;
 					IndexedPropertyRecord ipr = new IndexedPropertyRecord();
 					usepr = ipr;
 					useType = INDEXEDPROPERTY_RECORD_TYPE;
 					ipr.indexedReadMethod = getReflectedMethodRecord(ipd.getIndexedReadMethod());
 					ipr.indexedWriteMethod = getReflectedMethodRecord(ipd.getIndexedWriteMethod());
 					ipr.indexedPropertyTypeName = getClassName(ipd.getIndexedPropertyType());
 				} else {
 					usepr = new PropertyRecord();
 					useType = PROPERTY_RECORD_TYPE;
 				}
 				usepr.propertyEditorClassName = getClassName(pd.getPropertyEditorClass());
 				usepr.propertyTypeName = getClassName(pd.getPropertyType());
 				usepr.readMethod = getReflectedMethodRecord(pd.getReadMethod());
 				usepr.writeMethod = getReflectedMethodRecord((pd.getWriteMethod()));
 				usepr.bound = pd.isBound();
 				usepr.constrained = pd.isConstrained();
 				usepr.designTime = null;
 				usepr.field = null;
 				fill(pd, usepr, useType);
 				stream.writeObject(usepr);
 			}
 		}
 	}
 
 	/**
 	 * Called by IDE to send the method decorators information back through the callback.
 	 * 
 	 * @throws CommandException
 	 * @throws IOException
 	 * @since 1.1.0
 	 */
 	public void sendMethodDecorators(ObjectOutputStream stream) throws IOException, CommandException {
 		MethodDescriptor[] methods = getMethodDescriptors();
 		if (methods != null && methods.length > 0) {
 			// Now start writing the records.
			stream.writeInt(IBeanInfoIntrospectionConstants.METHOD_DECORATORS_SENT);
 			stream.writeInt(methods.length);
 			for (int i = 0; i < methods.length; i++) {
 				MethodRecord mr = new MethodRecord();
 				fill(mr, methods[i]);
 				stream.writeObject(mr);
 			}
 		}
 	}
 
 	/**
 	 * Fill in a MethodRecord from the MethodDescriptor.
 	 * @param mr
 	 * @param md
 	 * 
 	 * @since 1.1.0
 	 */
 	protected void fill(MethodRecord mr, MethodDescriptor md) {
 		mr.methodForDescriptor = getReflectedMethodRecord(md.getMethod());
 		ParameterDescriptor[] parms = md.getParameterDescriptors();
 		if (parms == null)
 			mr.parameters = null;
 		else {
 			mr.parameters = new ParameterRecord[parms.length];
 			for (int j = 0; j < parms.length; j++) {
 				ParameterRecord pr = new ParameterRecord();
 				fill(parms[j], pr, PARAMETER_RECORD_TYPE);
 				mr.parameters[j] = pr;
 			}
 		}
 		fill(md, mr, METHOD_RECORD_TYPE);
 	}
 
 	/**
 	 * Called by IDE to send the event set decorators information back through the callback.
 	 * 
 	 * @throws CommandException
 	 * @throws IOException
 	 * @since 1.1.0
 	 */
 	public void sendEventDecorators(ObjectOutputStream stream ) throws IOException, CommandException {
 		EventSetDescriptor[] events = getEventSetDescriptors();
 		if (events != null && events.length > 0) {
 			// Now start writing the records.
			stream.writeInt(IBeanInfoIntrospectionConstants.EVENT_DECORATORS_SENT);
 			stream.writeInt(events.length);
 			for (int i = 0; i < events.length; i++) {
 				EventSetDescriptor ed = events[i];
 				EventSetRecord er = new EventSetRecord();
 				er.addListenerMethod = getReflectedMethodRecord(ed.getAddListenerMethod());
 				MethodDescriptor[] mds = ed.getListenerMethodDescriptors();
 				if (mds == null)
 					er.listenerMethodDescriptors = null;
 				else {
 					er.listenerMethodDescriptors = new MethodRecord[mds.length];
 					for (int j = 0; j < mds.length; j++) {
 						fill(er.listenerMethodDescriptors[j] = new MethodRecord(), mds[j]);
 					}
 				}
 				er.listenerTypeName = getClassName(ed.getListenerType());
 				er.removeListenerMethod = getReflectedMethodRecord(ed.getRemoveListenerMethod());
 				er.inDefaultEventSet = ed.isInDefaultEventSet();
 				er.unicast = ed.isUnicast();
 				er.eventAdapterClassName = null;
 				fill(ed, er, EVENTSET_RECORD_TYPE);
 				stream.writeObject(er);
 			}
 		}
 	}
 
 	protected static final int BEAN_RECORD_TYPE = 0;
 
 	protected static final int PROPERTY_RECORD_TYPE = 1;
 
 	protected static final int INDEXEDPROPERTY_RECORD_TYPE = 2;
 	
 	protected static final int METHOD_RECORD_TYPE = 3;
 	
 	protected static final int PARAMETER_RECORD_TYPE = 4;
 	
 	protected static final int EVENTSET_RECORD_TYPE = 5;
 
 	/**
 	 * Fill in the special attr/values for the given record type. The default handles the standard ones.
 	 * 
 	 * @param record
 	 * @param descr
 	 * @param attributeName
 	 * @param recordType
 	 *            type of record ultimately being processed.
 	 * @return <code>true</code> if this attribute is a special one and processed, <code>false</code> if not special and should be added to
 	 *         attributes list transferred to IDE.
 	 * 
 	 * @see ModelingBeanInfo#PROPERTY_RECORD_TYPE
 	 * @since 1.1.0
 	 */
 	protected boolean fillFromAttributes(FeatureRecord record, FeatureDescriptor descr, String attributeName, int recordType) {
 		switch (recordType) {
 			case INDEXEDPROPERTY_RECORD_TYPE:
 			case PROPERTY_RECORD_TYPE:
 				if (BaseBeanInfo.DESIGNTIMEPROPERTY.equals(attributeName)) {
 					((PropertyRecord) record).designTime = (Boolean) descr.getValue(attributeName);
 					return true;
 				} else if (BaseBeanInfo.FIELDPROPERTY.equals(attributeName)) {
 					Field f = (Field) descr.getValue(attributeName);
 					// We have a field, set the property type to this since we couldn't correctly create this otherwise.
 					PropertyRecord pr = (PropertyRecord) record;
 					pr.propertyTypeName = getClassName(f.getType());
 					pr.field = getReflectedFieldRecord(f);
 					pr.readMethod = null;	// Need to wipe out our dummy.
 					pr.writeMethod = null;	// Or if it set, not valid for a field.
 					return true;
 				}
 				break;
 			case EVENTSET_RECORD_TYPE:
 				if (BaseBeanInfo.EVENTADAPTERCLASS.equals(attributeName)) {
 					((EventSetRecord) record).eventAdapterClassName = (String) descr.getValue(attributeName);
 					return true;
 				}
 				break;
 			default:
 				break; // Didn't handle it.
 		}
 		return false;
 	}
 
 	/**
 	 * Fill in the feature portion of the Descriptor into the record. We can be reusing some records (so we don't keep allocating when not needed), so
 	 * we will null out unset fields.
 	 * 
 	 * @param descr
 	 * @param record
 	 * @param recordType
 	 *            type of record ultimately being processed. Used for fillFromAttributes.
 	 * 
 	 * @see ModelingBeanInfo#PROPERTY_RECORD_TYPE
 	 * @since 1.1.0
 	 */
 	protected void fill(FeatureDescriptor descr, FeatureRecord record, int recordType) {
 		record.name = descr.getName();
 		String dn = descr.getDisplayName();
 		if (!record.name.equals(dn))
 			record.displayName = dn; // display name returns name if display name not set. We don't want to send it if identical. (Note some Beaninfos are setting displayname the same text but not same string).
 		else
 			record.displayName = null;
 		String shd = descr.getShortDescription();
 		if (!dn.equals(shd))
 			record.shortDescription = shd; // short description returns displayname if short description not set. We don't want to send it if
 										   // identical.
 		else
 			record.shortDescription = null;
 		record.expert = descr.isExpert();
 		record.hidden = descr.isHidden();
 		record.preferred = descr.isPreferred();
 		record.category = null; // Clear out in case not set.
 		Enumeration attrs = descr.attributeNames();
 		if (attrs.hasMoreElements()) {
 			// We don't have a way of knowing how many there are ahead of time, so we will build into lists and then turn into arrays at the end.
 			List names = new ArrayList();
 			List values = new ArrayList();
 			while (attrs.hasMoreElements()) {
 				String attrName = (String) attrs.nextElement();
 				if (attrName.equals(BaseBeanInfo.CATEGORY))
 					record.category = (String) descr.getValue(BaseBeanInfo.CATEGORY);
 				else if (attrName.equals(BaseBeanInfo.PREFERRED)) {
 					// A bug in Java 1.3, doing setPreferred was lost. So for those also stored it in attributes. So if set here, then use it.
 					record.preferred = ((Boolean) descr.getValue(BaseBeanInfo.PREFERRED)).booleanValue();
 				} else if (!fillFromAttributes(record, descr, attrName, recordType)) {
 					// Just copy accross. FillfromAttributes didn't handle it.
 					FeatureAttributeValue fv = new FeatureAttributeValue();
 					fv.setValue(descr.getValue(attrName));
 					names.add(attrName);
 					values.add(fv);
 				}
 			}
 			if (!names.isEmpty()) {
 				record.attributeNames = (String[]) names.toArray(new String[names.size()]);
 				record.attributeValues = (FeatureAttributeValue[]) values.toArray(new FeatureAttributeValue[values.size()]);
 			} else {
 				record.attributeNames = null;
 				record.attributeValues = null;
 			}
 		} else {
 			record.attributeNames = null;
 			record.attributeValues = null;
 		}
 
 	}
 
 	/*
 	 * Get the classname from the class. If classs is null, then this return null.
 	 */
 	private String getClassName(Class classs) {
 		return classs != null ? classs.getName() : null;
 	}
 
 	private ReflectMethodRecord getReflectedMethodRecord(Method method) {
 		if (method != null) {
 			ReflectMethodRecord rmr = new ReflectMethodRecord();
 			rmr.className = getClassName(method.getDeclaringClass());
 			rmr.methodName = method.getName();
 			Class[] parmTypes = method.getParameterTypes();
 			if (parmTypes.length > 0) {
 				rmr.parameterTypeNames = new String[parmTypes.length];
 				for (int i = 0; i < parmTypes.length; i++) {
 					rmr.parameterTypeNames[i] = getClassName(parmTypes[i]);
 				}
 			}
 			return rmr;
 		} else
 			return null;
 	}
 	
 	private ReflectFieldRecord getReflectedFieldRecord(Field field) {
 		if (field != null) {
 			ReflectFieldRecord rf = new ReflectFieldRecord();
 			rf.className = getClassName(field.getDeclaringClass());
 			rf.fieldName = field.getName();
 			rf.readOnly = Modifier.isFinal(field.getModifiers());
 			return rf;
 		} else
 			return null;
 	}
 }
