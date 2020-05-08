 /*******************************************************************************
  * Copyright (c) 2001, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jem.internal.adapters.jdom;
 /*
  *  $RCSfile: JavaClassJDOMAdaptor.java,v $
 *  $Revision: 1.24 $  $Date: 2006/01/26 15:21:53 $ 
  */
 
 import java.util.*;
 import java.util.logging.Level;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.Notifier;
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.xmi.XMIResource;
 import org.eclipse.jdt.core.*;
 
 
 import org.eclipse.jem.internal.java.adapters.*;
 import org.eclipse.jem.internal.java.adapters.nls.ResourceHandler;
 import org.eclipse.jem.internal.plugin.JavaPlugin;
 import org.eclipse.jem.java.*;
 import org.eclipse.jem.java.internal.impl.JavaClassImpl;
 import org.eclipse.jem.util.TimerTests;
 import org.eclipse.jem.util.UIContextDetermination;
 import org.eclipse.jem.util.logger.proxy.Logger;
 
 
 public class JavaClassJDOMAdaptor extends JDOMAdaptor implements IJavaClassAdaptor {
 	private static final String OBJECT_TYPE_NAME = "java.lang.Object"; //$NON-NLS-1$
 
 	/*
 	 * Step ids used for TimerTests of performance testing.
 	 */
 	public static final String REFLECT_CLASS = "Reflect JDOM Class"; //$NON-NLS-1$
 	public static final String REFLECT_METHODS = "Reflect all JDOM methods for a class"; //$NON-NLS-1$
 	public static final String REFLECT_FIELDS = "Reflect all JDOM fields for a class"; //$NON-NLS-1$
 	
 	
 	protected IType sourceType = null;
 	protected JavaReflectionAdapterFactory adapterFactory;
 	private Map typeResolutionCache = new HashMap(25);
 	private boolean hasReflectedFields, isReflectingFields;
 	private boolean hasReflectedMethods, isReflectingMethods;
 	
 	public JavaClassJDOMAdaptor(Notifier target, IJavaProject workingProject, JavaReflectionAdapterFactory inFactory) {
 		super(target, workingProject);
 		setAdapterFactory(inFactory);
 	}
 	
 	private Map existingFields = new HashMap(); 
 	/*
 	 * addFields - reflect our fields
 	 */
 	protected boolean addFields() {
 
 		// The algorithm we will use is:
 		// 1) Pass through the IField's of this class
 		//    a) If it is in existingFields, then add to newExisting the entry from
 		//       oldExisting (deleting from oldExisting at the same time), and flush the field. This is so next we re-get any changed parts of it.
 		//    b) else not existing, then create new field and add to the new fields list.
 		// 2) Remove from the fields list any still left in oldExisting. These are ones that no longer exist.
 		// 3) Add all of the news ones to the fields.
 		//       
 		IField[] fields = null;
 		try {
 			fields = getSourceType().getFields();
 		} catch (JavaModelException e) {
 			Logger.getLogger().log(e, Level.WARNING);
 			return false;	
 		}
 		XMIResource resource = (XMIResource) getJavaClassTarget().eResource();		
 		Field field = null;
 		JavaFieldJDOMAdaptor adapter = null;
 		Map newExisting = new HashMap(fields.length);
 		List newFields = new ArrayList();
 		for (int i = 0; i < fields.length; i++) {
 			IField ifield = fields[i];
 			field = (Field) existingFields.remove(ifield);	// Get the existing field (which is the value) from the collection keyed by IField.
 			if (field != null) {
 				// It is an existing method. So just put over to newExisting. Then flush it.
 				newExisting.put(ifield, field);
 				// Since this is a new method, it is not attached to a resource, so we need to explicitly create the adapter.
 				adapter = (JavaFieldJDOMAdaptor) EcoreUtil.getExistingAdapter(field, ReadAdaptor.TYPE_KEY);
 				if (adapter == null)
 					adapter = (JavaFieldJDOMAdaptor) getAdapterFactory().adaptNew(field, ReadAdaptor.TYPE_KEY);
 				else
 					adapter.flushReflectedValuesIfNecessaryNoNotification(true);
 				adapter.setSourceField(ifield);	// Give it this new IField
 			} else {
 				// It is a new method. Create the new method, add to newExisting, and add to newMethods list.
 				field = createJavaField(ifield, resource);
 				newExisting.put(ifield, field);				
 				newFields.add(field);
 				adapter = (JavaFieldJDOMAdaptor) getAdapterFactory().adaptNew(field, ReadAdaptor.TYPE_KEY);
 				if (adapter != null)
 					adapter.setSourceField(ifield);
 			}
 		}
 		
 		BasicEList fieldsList = (BasicEList) getJavaClassTarget().getFieldsGen();
 		if (!existingFields.isEmpty()) {
 			// Now any still left in old existing are deleted. So we make them proxies and then remove them from fields list.			
 			URI baseURI = resource.getURI();
 			Collection toDelete = existingFields.values();
 			for (Iterator itr = toDelete.iterator(); itr.hasNext();) {
 				InternalEObject m = (InternalEObject) itr.next();
 				String id = resource.getID(m);
 				if (id != null)
 					m.eSetProxyURI(baseURI.appendFragment(id));
 			}
 			fieldsList.removeAll(toDelete);
 		}
 		
 		if (!newFields.isEmpty()) {
 			// Now add in the news ones
 			fieldsList.addAllUnique(newFields);
 		}
 		
 		// Finally set current existing to the new map we created.
 		existingFields = newExisting;
 		return true;			
 	}
 	
 	private Map existingMethods = new HashMap(); 
 	/*
 	 * addMethods - reflect our methods. Merge in with the previous.
 	 */
 	protected boolean addMethods() {
 		// The algorithm we will use is:
 		// 1) Pass through the IMethod's of this class
 		//    a) If it is in existingMethods, then add to newExisting the entry from
 		//       oldExisting (deleting from oldExisting at the same time), and flush the method. This is so next we re-get any changed parts of it.
 		//    b) else not existing, then create new method and add to the new methods list.
 		// 2) Remove from the methods list any still left in oldExisting. These are ones that no longer exist.
 		// 3) Add all of the news ones to the methods.
 		//       
 		IMethod[] methods = null;
 		try {
 			methods = getSourceType().getMethods();
 		} catch (JavaModelException e) {
 			Logger.getLogger().log(e, Level.WARNING);
 			return false;	
 		}
 		XMIResource resource = (XMIResource) getJavaClassTarget().eResource();		
 		Method method = null;
 		JavaMethodJDOMAdaptor adapter = null;
 		Map newExisting = new HashMap(methods.length);
 		List newMethods = new ArrayList();
 		for (int i = 0; i < methods.length; i++) {
 			IMethod im = methods[i];
 			method = (Method) existingMethods.remove(im);	// Get the existing method (which is the value) from the collection keyed by IMethod.
 			if (method != null) {
 				// It is an existing method. So just put over to newExisting. Then flush it.
 				newExisting.put(im, method);
 				adapter = (JavaMethodJDOMAdaptor) retrieveAdaptorFrom(method);
 				if (adapter != null) {
 					adapter.flushReflectedValuesIfNecessaryNoNotification(true);
 					adapter.setSourceMethod(im);	// Give it this new IMethod
 				}
 			} else {
 				// It is a new method. Create the new method, add to newExisting, and add to newMethods list.
 				method = createJavaMethod(im, resource);
 				newExisting.put(im, method);				
 				newMethods.add(method);
 				// Since this is a new method, it is not attached to a resource, so we need to explicitly create the adapter.
 				adapter = (JavaMethodJDOMAdaptor) getAdapterFactory().adaptNew(method, ReadAdaptor.TYPE_KEY);
 				if (adapter != null)
 					adapter.setSourceMethod(methods[i]);
 			}
 		}
 		
 		BasicEList methodsList = (BasicEList) getJavaClassTarget().getMethodsGen();
 		if (!existingMethods.isEmpty()) {
 			// Now any still left in old existing are deleted. So we make them proxies and then remove them from methods list.
 			URI baseURI = resource.getURI();
 			Collection toDelete = existingMethods.values();
 			for (Iterator itr = toDelete.iterator(); itr.hasNext();) {
 				InternalEObject m = (InternalEObject) itr.next();
 				String id = resource.getID(m);
 				if (id != null)
 					m.eSetProxyURI(baseURI.appendFragment(id));
 			}
 			methodsList.removeAll(toDelete);
 		}
 		
 		if (!newMethods.isEmpty()) {
 			// Now add in the news ones
 			methodsList.addAllUnique(newMethods);
 		}
 		
 		// Finally set current existing to the new map we created.
 		existingMethods = newExisting;
 		return true;
 	}
 	/**
 	 * Clear source Type ;
 	 */
 	protected void clearSource() {
 		sourceType = null;
 	}
 
 	/**
 	 * Clear the reflected fields list.
 	 */
 	protected boolean flushFields() {
 		// First turn them all into proxies so that any holders will re-resolve to maybe the new one if class comes back.
 		existingFields.clear();
 		XMIResource res = (XMIResource) getJavaClassTarget().eResource();
 		URI baseURI = res.getURI();
 		List fields = getJavaClassTarget().getFieldsGen();
 		int msize = fields.size();
 		for (int i = 0; i < msize; i++) {
 			InternalEObject f = (InternalEObject) fields.get(i);
 			String id = res.getID(f);
 			if (id != null)
 				f.eSetProxyURI(baseURI.appendFragment(id));
 		}
 		fields.clear();	// Now we can clear it.
 		return true;
 		}
 	/**
 	 * Clear the implements list.
 	 */
 	protected boolean flushImplements() {
 		getJavaClassTarget().getImplementsInterfacesGen().clear();
 		return true;
 	}
 	/**
 	 * Clear the reflected methods list.
 	 */
 	protected boolean flushMethods() {
 		// First turn them all into proxies so that any holders will re-resolve to maybe the new one if class comes back.
 		existingMethods.clear();
 		XMIResource res = (XMIResource) getJavaClassTarget().eResource();
 		URI baseURI = res.getURI();
 		List methods = getJavaClassTarget().getMethodsGen();
 		int msize = methods.size();
 		for (int i = 0; i < msize; i++) {
 			InternalEObject m = (InternalEObject) methods.get(i);
 			String id = res.getID(m);
 			if (id != null)
 				m.eSetProxyURI(baseURI.appendFragment(id));
 		}
 		methods.clear();	// Now we can clear it.
 		return true;
 	}
 	protected boolean flushModifiers() {
 		JavaClass javaClassTarget = (JavaClass) getTarget();
 		javaClassTarget.setAbstract(false);
 		javaClassTarget.setFinal(false);
 		javaClassTarget.setPublic(false);
 		javaClassTarget.setKind(TypeKind.UNDEFINED_LITERAL);
 		return true;
 	}
 	protected boolean flushInnerClasses() {
 		getJavaClassTarget().getDeclaredClassesGen().clear();
 		return true;
 	}
	
	protected boolean flushAndClearCachedModelObject;
	
 	/**
 	 * Clear the reflected values.
 	 */
 	protected boolean flushReflectedValues(boolean clearCachedModelObject) {
		flushAndClearCachedModelObject = clearCachedModelObject;
		return true;
	}
	
	/*
	 * This is called before a reflect if a real flush is needed.
	 */
	private void flushNow() {
		if (flushAndClearCachedModelObject)
 			setSourceType(null);
 		typeResolutionCache.clear();
 		flushModifiers();
 		flushSuper();
 		flushImplements();
		if (flushAndClearCachedModelObject) {
 			// Don't flush these yet. We will try to reuse them on the next reflush. If clear model too, then flush them. This usually means class has been deleted, so why keep them around.
 			flushMethods();
 			flushFields();
 		}
 		// Even if we didn't flush the fields/methods, we do need to mark as not reflected so on next usage we will merge in the changes.
 		hasReflectedMethods = false;
 		hasReflectedFields = false;
 		
 		flushInnerClasses();
		flushAndClearCachedModelObject = false;
 	}
 

 	/**
 	 * @see com.ibm.etools.java.adapters.JavaReflectionAdaptor#postFlushReflectedValuesIfNecessary()
 	 */
 	protected void postFlushReflectedValuesIfNecessary(boolean isExisting) {
 		getJavaClassTarget().setReflected(false);
 		super.postFlushReflectedValuesIfNecessary(isExisting);
 	}
 
 	/**
 	 * Set the supertype to be null.
 	 */
 	protected boolean flushSuper() {
 		List targetSupers = getJavaClassTarget().primGetESuperTypes();
 		targetSupers.clear();
 		return true;
 	}
 	protected JavaReflectionAdapterFactory getAdapterFactory() {
 		return adapterFactory;
 	}
 	/**
 	 * getBinaryType - return the IType which describes our existing Java class file
 	 */
 	protected IType getBinaryType() {
 		return this.getBinaryType(((JavaClass) getTarget()).getQualifiedName());
 	}
 	/**
 	 * Return the target typed to a JavaClass.
 	 */
 	protected JavaClassImpl getJavaClassTarget() {
 		return (JavaClassImpl) getTarget();
 	}
 	public Object getReflectionSource() {
 		return getSourceType();
 	}
 	
     /* (non-Javadoc)
      * @see org.eclipse.jem.internal.java.adapters.JavaReflectionAdaptor#hasReflectionSource()
      */
     public boolean hasCachedReflectionSource() {
         return sourceType != null;
     }
 	/**
 	 * getSourceType - return the IType which describes our existing Java class or source file
 	 */
 	public IType getSourceType() {
 		if (sourceType == null) {
 			JavaClassImpl javaClass = (JavaClassImpl) getTarget();
 			sourceType = JDOMSearchHelper.findType(javaClass.getJavaPackage().getName(), javaClass.primGetName(), getSourceProject());
 		}
 		return sourceType;
 	}
 	/**
 	 * getSourceType - return the IType which describes our existing Java class or source file
 	 */
 	protected IType getType() {
 		return getSourceType();
 	}
 	protected Map getTypeResolutionCache() {
 		return typeResolutionCache;
 	}
 	/**
 	 * getValueIn method comment.
 	 */
 	public Object getValueIn(EObject object, EObject attribute) {
 		// At this point, this adapter does not dynamically compute any values,
 		// all values are pushed back into the target on the initial call.
 		return super.getValueIn(object, attribute);
 	}
 	/**
 	 * Return true if the sourceType is null or if
 	 * it is a binary type.
 	 */
 	public boolean isSourceTypeFromBinary() {
 		if (getSourceType() == null)
 			return false; //must be new?
 		return getSourceType().isBinary();
 	}
 
 
 	protected JavaClass reflectJavaClass(String qualifiedName) {
 		IType type = JDOMSearchHelper.findType(qualifiedName, true, getSourceProject(), this);
 		if (type != null)
 			return reflectJavaClass(type);
 		else
 			return createJavaClassRef(qualifiedName);
 	}
 	protected JavaClass reflectJavaClass(IType aType) {
 		if (aType != null) {
 			JavaClass javaClass = (JavaClass) JavaRefFactory.eINSTANCE.reflectType(aType.getFullyQualifiedName(), (EObject) getTarget());
 			if (javaClass != null) {
 				JavaClassJDOMAdaptor adaptor = (JavaClassJDOMAdaptor) EcoreUtil.getAdapter(javaClass.eAdapters(), ReadAdaptor.TYPE_KEY);
 				if (adaptor != null)
 					adaptor.setSourceType(aType);
 			}
 			return javaClass;
 		}
 		return null;
 	}
 	/**
 	 * reflectValues - template method, subclasses override to pump values into target.
 	 * on entry: name, containing package (and qualified name), and document must be set.
 	 * Return true always and the JavaReflectionSynchronizer will flush with the type can
 	 * be found again.  In headless mode, return true only if the type is found.  This is
 	 * needed becauce most headless tasks are done under one operation and the JavaReflectionSynchronizer
 	 * may not have a chance to flush a bad reflection before the real type needs to be found and can be found.
 	 * JavaClass adaptor:
 	 * - set modifiers
 	 * - set name
 	 * - set reference to super
 	 * - create methods
 	 * - create fields
 	 * - add imports
 	 */
 	public boolean reflectValues() {
		if (hasFlushed) {
			// We flush sometime in the past since last reflect. So now do the actual flush.
			flushNow();
		}
 		super.reflectValues();
 		boolean isHeadless = UIContextDetermination.getCurrentContext() == UIContextDetermination.HEADLESS_CONTEXT;
 		if (canReflect()) {
 			TimerTests.basicTest.startCumulativeStep(REFLECT_CLASS);
 			try {
 				ICompilationUnit cu = getSourceType().getCompilationUnit();
 				boolean isWC = cu != null ? cu.isWorkingCopy() : false;
 				IResource res = isWC ? getSourceType().getResource() : null;
 				// We are only interested in physical classes. If still just in working copy and not yet put out to
 				// disk, we don't should treat as not exist. Anything else is considered existing because we got past
 				// getSourceType.exists. This will return the truth for non-wc. But for wc types it will return true,
 				// even though not physically on disk (such as just creating it and hadn't saved it yet). So for wc types
 				// we need to test the actual resource.
 				// Test is OK if not wc, or if wc, then there is a res. and it is accessible.
 				if (!isWC || (res != null && res.isAccessible())) {
 					setModifiers();
 					setNaming();
 					try {
 						setSuper();
 					} catch (InheritanceCycleException e) {
 						JavaPlugin.getDefault().getLogger().log(e);
 					}
 					setImplements();
 					reflectInnerClasses();
 					setDeclaringClass();
 					//addImports();
 					if (isHeadless) {
 						registerWithFactory();
 						return true;
 					}
 				}
 			} finally {
 				TimerTests.basicTest.stopCumulativeStep(REFLECT_CLASS);
 			}
 		}
 		if (isHeadless)
 			return false;
 		else {
 			registerWithFactory();
 			return true;
 		}
 	}
 	
 	protected void setDeclaringClass() {
 		IType declaringType = getSourceType().getDeclaringType();
 		if (declaringType != null) {
 			// Need to get it and reflect it so that the declared type of this target is set correctly. We can just
 			// set it ourselves directly because ECore would try to add it to the list of inner classes of the declaring type. This
 			// would cause it to be added twice, once from the reflection caused by the inverse setting, and once from our doing
 			// the inverse setting itself.
 			ResourceSet set = getTargetResource().getResourceSet();
 			String packageName = declaringType.getPackageFragment().getElementName();
 			JavaClassImpl declaringClass = (JavaClassImpl) JavaRefFactory.eINSTANCE.reflectType(packageName, declaringType.getTypeQualifiedName(), set);
 			declaringClass.getDeclaredClasses();	// This will cause it to put us into its list and also set our declaring class to this declaring type.
 		}
 	}
 	
 	
 	/**
 	 * @return
 	 */
 	private boolean canReflect() {
 		return isResourceLoaded() && getSourceProject() != null && getSourceType() != null && getSourceType().exists();
 	}
 	public synchronized boolean reflectFieldsIfNecessary() {
 		if (reflectValuesIfNecessary() && canReflect()) {
 			if (!hasReflectedFields && !isReflectingFields) {
 				isReflectingFields = true;
 				try {
 					TimerTests.basicTest.startCumulativeStep(REFLECT_FIELDS);
 					addFields();
 					hasReflectedFields = true;
 				} catch (Throwable e) {
 					hasReflectedFields = false;
 					Logger logger = Logger.getLogger();
 					if (logger.isLoggingLevel(Level.WARNING)) {
 						logger.log(ResourceHandler.getString("Failed_reflecting_values_ERROR_"), Level.WARNING); //$NON-NLS-1$ = "Failed reflecting values!!!"
 						logger.logWarning(e); 
 					}					
 				} finally {
 					isReflectingFields = false;
 					TimerTests.basicTest.stopCumulativeStep(REFLECT_FIELDS);
 				}
 			}
 			return hasReflectedFields;
 		} else
 			return false;	// Couldn't reflect the base values, so couldn't do fields either
 	}
 	public synchronized boolean reflectMethodsIfNecessary() {
 		if (reflectValuesIfNecessary() && canReflect()) {
 			if (!hasReflectedMethods && !isReflectingMethods) {
 				isReflectingMethods = true;
 				try {
 					TimerTests.basicTest.startCumulativeStep(REFLECT_METHODS);
 					hasReflectedMethods = addMethods();
 				} catch (Throwable e) {
 					hasReflectedMethods = false;
 					Logger logger = Logger.getLogger();
 					if (logger.isLoggingLevel(Level.WARNING)) {
 						logger.log(ResourceHandler.getString("Failed_reflecting_values_ERROR_"), Level.WARNING); //$NON-NLS-1$ = "Failed reflecting values!!!"
 						logger.logWarning(e); 
 					}					
 				} finally {
 					isReflectingMethods = false;
 					if (!hasReflected)
 						flushMethods();	// Something bad happened, so we will do a complete flush to be on safe side.
 					TimerTests.basicTest.stopCumulativeStep(REFLECT_METHODS);
 				}
 			}
 			return hasReflectedMethods;
 		} else
 			return false;	// Couldn't reflect the base values, so couldn't do fields either
 	}
 	
 	private void registerWithFactory() {
 		getAdapterFactory().registerReflection(getJavaClassTarget().getQualifiedNameForReflection(), this);
 	}
 
 	/**
 	 * @see com.ibm.etools.java.adapters.ReflectionAdaptor#notifyChanged(new ENotificationImpl((InternalEObject)Notifier, int,(EStructuralFeature) EObject, Object, Object, int))
 	 */
 	public void notifyChanged(Notification notification) {
 		if (notification.getEventType() == Notification.REMOVING_ADAPTER
 			&& notification.getOldValue() == this
 			&& notification.getNotifier() == getTarget())
 			getAdapterFactory().unregisterReflection(getJavaClassTarget().getQualifiedNameForReflection());
 
 	}
 
 	protected void setAdapterFactory(JavaReflectionAdapterFactory inFactory) {
 		adapterFactory = inFactory;
 	}
 	/**
 	 * setImplements - set our implemented/super interfaces here
 	 * For an interface, these are superclasses.
 	 * For a class, these are implemented interfaces.
 	 */
 	protected void setImplements() {
 		try {
 			String[] interfaceNames = getSourceType().getSuperInterfaceNames();
 			JavaClass ref;
 			// needs work, the names above will be simple names if we are relfecting from a source file
 			List list = getJavaClassTarget().getImplementsInterfacesGen();
 			for (int i = 0; i < interfaceNames.length; i++) {
 				ref = reflectJavaClass(interfaceNames[i]);
 				list.add(ref);
 			}
 		} catch (JavaModelException npe) {
 			// name stays null and we carry on
 		}
 	}
 	/**
 	 * setModifiers - set the attribute values related to modifiers here
 	 */
 	protected void setModifiers() {
 		JavaClass javaClassTarget = (JavaClass) getTarget();
 		try {
 			javaClassTarget.setAbstract(Flags.isAbstract(getSourceType().getFlags()));
 			javaClassTarget.setFinal(Flags.isFinal(getSourceType().getFlags()));
 			javaClassTarget.setPublic(Flags.isPublic(getSourceType().getFlags()));
 			// Set type to class or interface, not yet handling EXCEPTION
 			if (getSourceType().isClass())
 				javaClassTarget.setKind(TypeKind.CLASS_LITERAL);
 			else
 				javaClassTarget.setKind(TypeKind.INTERFACE_LITERAL);
 		} catch (JavaModelException npe) {
 			Logger logger = JavaPlugin.getDefault().getLogger();
 			if (logger.isLoggingLevel(Level.WARNING))
 				logger.log(ResourceHandler.getString("Error_Introspecting_Flags_ERROR_", new Object[] { javaClassTarget.getQualifiedName(), npe.getMessage()}), Level.WARNING); //$NON-NLS-1$ = "error introspecting flags on {0}"
 		}
 	}
 	/**
 	 * setNaming - set the naming values here
 	 * 	- qualified name (package name + name) must be set first, that is the path to the real Java class
 	 *	- ID - simple name, identity within a package document
 	 * 	- null UUID
 	 */
 	protected void setNaming() {
 		/* Naming has been provided by the JavaReflectionKey 
 		JavaClass javaClassTarget = (JavaClass) getTarget();
 		String packageName = getSourceType().getPackageFragment().getElementName();
 		javaClassTarget.refSetUUID((String)null);
 		((XMIResource)javaClassTarget.eResource()).setID(javaClassTarget,getSourceType().getElementName());
 		*/
 	}
 	protected void setSourceType(IType aType) {
 		sourceType = aType;
 	}
 	/**
 	 * setSuper - set our supertype here, implemented interface are handled separately
 	 */
 	protected void setSuper() throws InheritanceCycleException {
 		String superName = null;
 		IType superType = null;
 		try {
 			if (!getSourceType().isInterface()) {
 				superName = getSourceType().getSuperclassName();
 				// binary types will always have fully-qualified super names, so no need to do any searching.
 				if (!getSourceType().isBinary() && superName != null && isTargetInner()) {
 					IType declaringType = getSourceType().getDeclaringType();
 					if (declaringType != null) {
 						//Get all parent InnerTypes
 						IType[] inners = declaringType.getTypes();
 						for (int i = 0; i < inners.length; i++) {
 							IType type = inners[i];
 							if (superName.equals(type.getElementName())) {
 								superName = declaringType.getElementName() + '.' + superName;
 								reflectInnerClasses(declaringType);
 								superType = type;
 								break;
 							}
 						}
 					}
 				}
 				
 				//Source files return null if extends does not exist.
 				if (superName == null && !getSourceType().getFullyQualifiedName().equals(OBJECT_TYPE_NAME))
 					superName = OBJECT_TYPE_NAME;
 				if (superName != null) {
 					JavaClass javaClassTarget = (JavaClass) getTarget();
 					if (superType != null)
 						javaClassTarget.setSupertype(reflectJavaClass(superType));
 					else
 						javaClassTarget.setSupertype(reflectJavaClass(superName));
 				}
 			}
 		} catch (JavaModelException npe) {
 		}
 	}
 	private boolean isTargetInner() {
 		JavaClassImpl javaClass = (JavaClassImpl) getTarget();
 		return (javaClass.getName().indexOf('$') != -1);
 	}
 	/**
 	 * Return true if the sourceType can be found.
 	 */
 	public boolean sourceTypeExists() {
 		return getSourceType() != null;
 	}
 	protected void reflectInnerClasses() {
 		IType[] innerClasses = null;
 		try {
 			innerClasses = getSourceType().getTypes();
 		} catch (JavaModelException e) {
 		}
 		if (innerClasses != null && innerClasses.length != 0) {
 			List declaredClasses = getJavaClassTarget().getDeclaredClassesGen();
 			JavaClass inner;
 			ResourceSet set = getTargetResource().getResourceSet();
 			String packageName = getSourceType().getPackageFragment().getElementName();
 			for (int i = 0; i < innerClasses.length; i++) {
 				inner = (JavaClass) JavaRefFactory.eINSTANCE.reflectType(packageName, innerClasses[i].getTypeQualifiedName(), set);
 				declaredClasses.add(inner);
 			}
 		}
 	}
 	protected void reflectInnerClasses(IType aType) {
 		IType[] innerClasses = null;
 		try {
 			innerClasses = aType.getTypes();
 		} catch (JavaModelException e) {
 		}
 		if (innerClasses != null && innerClasses.length != 0) {
 			ResourceSet set = getTargetResource().getResourceSet();
 			String packageName = aType.getPackageFragment().getElementName();
 			JavaClassImpl parentType = (JavaClassImpl) JavaRefFactory.eINSTANCE.reflectType(packageName, aType.getTypeQualifiedName(), set);
 			List declaredClasses = parentType.getDeclaredClassesGen();
 			JavaClass inner;
 			
 			
 			for (int i = 0; i < innerClasses.length; i++) {
 				inner = (JavaClass) JavaRefFactory.eINSTANCE.reflectType(packageName, innerClasses[i].getTypeQualifiedName(), set);
 				declaredClasses.add(inner);
 			}
 		}
 	}
 }
