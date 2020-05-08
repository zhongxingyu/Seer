 /*
  *  Copyright (C) 2004  The Concord Consortium, Inc.,
  *  10 Concord Crossing, Concord, MA 01742
  *
  *  Web Site: http://www.concord.org
  *  Email: info@concord.org
  *
  *  This library is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public
  *  License as published by the Free Software Foundation; either
  *  version 2.1 of the License, or (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this library; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * END LICENSE */
 
 /*
  * Last modification information:
  * $Revision: 1.27 $
  * $Date: 2007-10-22 01:50:37 $
  * $Author: scytacki $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.otrunk;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Proxy;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.concord.framework.otrunk.OTControllerRegistry;
 import org.concord.framework.otrunk.OTControllerService;
 import org.concord.framework.otrunk.OTID;
 import org.concord.framework.otrunk.OTObject;
 import org.concord.framework.otrunk.OTObjectList;
 import org.concord.framework.otrunk.OTObjectService;
 import org.concord.framework.otrunk.OTPackage;
 import org.concord.framework.otrunk.OTResourceSchema;
 import org.concord.framework.otrunk.otcore.OTClass;
 import org.concord.otrunk.asm.GeneratedClassLoader;
 import org.concord.otrunk.datamodel.DataObjectUtil;
 import org.concord.otrunk.datamodel.OTDataList;
 import org.concord.otrunk.datamodel.OTDataObject;
 import org.concord.otrunk.datamodel.OTDataObjectType;
 import org.concord.otrunk.datamodel.OTDatabase;
 import org.concord.otrunk.datamodel.OTExternalIDProvider;
 import org.concord.otrunk.datamodel.OTTransientMapID;
 import org.concord.otrunk.datamodel.OTUUID;
 import org.concord.otrunk.otcore.impl.ReflectiveOTClassFactory;
 import org.concord.otrunk.overlay.CompositeDatabase;
 import org.concord.otrunk.view.OTConfig;
 import org.concord.otrunk.xml.XMLDataObject;
 
 public class OTObjectServiceImpl
     implements OTObjectService, OTExternalIDProvider
 {
 	public final static Logger logger = 
 		Logger.getLogger(OTObjectServiceImpl.class.getCanonicalName());
 	
     protected OTrunkImpl otrunk;
     protected OTDatabase creationDb;
     protected OTDatabase mainDb;
     protected ArrayList<OTObjectServiceListener> listeners = 
     	new ArrayList<OTObjectServiceListener>();
 
     public OTObjectServiceImpl(OTrunkImpl otrunk)
     {
         this.otrunk = otrunk;
     }
     
     public void setCreationDb(OTDatabase creationDb)
     {
         this.creationDb = creationDb;
     }
     
     public OTDatabase getCreationDb()
     {
         return creationDb;
     }
     
     public void setMainDb(OTDatabase mainDb)
     {
         this.mainDb = mainDb;
     }
     
     public OTDatabase getMainDb()
     {
     	return mainDb;
     }
     
     public <T extends OTObject> T createObject(Class<T> objectClass) 
         throws Exception
     {
     	OTObjectInternal otObjectImpl = createOTObjectInternal(objectClass);
         T newObject = loadOTObject(otObjectImpl, objectClass);
 
         return newObject;
     }
 
     protected OTObjectInternal createOTObjectInternal(
     	Class<? extends OTObject> objectClass)
     	throws Exception
     {
     	String className = objectClass.getName();
         OTClass otClass = OTrunkImpl.getOTClass(className);
         if(otClass == null){
         	// Can't find existing otClass for this class try to make one
         	otClass = ReflectiveOTClassFactory.singleton.registerClass(objectClass);
         	if(otClass == null){
         		// Java class isn't a valid OTObject
             	throw new IllegalStateException("Invalid OTClass definition: " + className);
         	}
         	
         	// This will add the properties for this new class plus any dependencies that
         	// were registered at the same time.
         	ReflectiveOTClassFactory.singleton.processAllNewlyRegisteredClasses();
         }
     	OTDataObjectType type = new OTDataObjectType(objectClass.getName());
         OTDataObject dataObject = createDataObject(type); 
     	OTObjectInternal otObjectImpl = 
     		new OTObjectInternal(dataObject, this, otClass);
     	return otObjectImpl;
     }
     
     public OTObject getOTObject(OTID childID) throws Exception
     {
     	return getOTObject(childID, false);
     }
     
     @SuppressWarnings("unchecked")
     public OTObject getOTObject(OTID childID, boolean reload) throws Exception
     {
         // sanity check
         if(childID == null) {
             throw new Exception("Null child id");
         }
         
         OTDataObject childDataObject = getOTDataObject(childID);
  
         if(childDataObject == null) {
             // we have a null internal object that means the child doesn't 
             // exist in our database/databases.
             // 
             // This will happen with the aggregate views which display different overlays
         	// of the same object.  Each overlay is going to come from a different objectService
         	// So if we can't find this object then we go out to OTrunk to see if it can find
         	// the object.  The way that this happens needs to be more clear so the ramifcations
         	// are clear.
         	return otrunk.getOrphanOTObject(childID, this, reload);
         }
 
         // Look for our object to see it is already setup in the otrunk list of loaded objects
         // it might be better to have each object service maintain its own list of loaded objects
         OTObject otObject = otrunk.getLoadedObject(childDataObject.getGlobalId(), reload);
         if(otObject != null) {
             return otObject;
         }
 
     	String otObjectClassStr = OTrunkImpl.getClassName(childDataObject);
         if(otObjectClassStr == null) {
             return null;
         }            
         Class<? extends OTObject> otObjectClass = 
         	(Class<? extends OTObject>) Class.forName(otObjectClassStr);
         
     	OTObjectInternal otObjectInternal = 
     		new OTObjectInternal(childDataObject, this, OTrunkImpl.getOTClass(otObjectClassStr));
     
         return loadOTObject(otObjectInternal, otObjectClass);        
     }
 
     public OTID getOTID(String otidStr)
     {
         return otrunk.getOTID(otidStr);
     }
 
     public OTControllerService createControllerService() {
     	OTControllerRegistry registry = 
     		otrunk.getService(OTControllerRegistry.class);
     	return new OTControllerServiceImpl(this, registry);
     }
     
     // This will work as long as all of the object classes are loaded by the same classloader
     // if the OTClass classes are loaded by different classloaders there will probably have
     // to be multiple GeneratedClassLoader
     GeneratedClassLoader asmClassLoader = null;
 
 	private HashMap<String, String> preserveUUIDCallers = new HashMap<String, String>(); 
     
     public GeneratedClassLoader getASMClassLoader()
     {
     	if(asmClassLoader != null){
     		return asmClassLoader;
     	}
     	asmClassLoader = new GeneratedClassLoader(OTObjectServiceImpl.class.getClassLoader());
     	return asmClassLoader;
     	
     }
     
     @SuppressWarnings("unchecked")
     public <T extends OTObject> T loadOTObject(OTObjectInternal otObjectImpl, Class<T> otObjectClass)
     throws  Exception
     {
         T otObject = null;
         
         if(otObjectClass.isInterface()) {
         	if(OTConfig.getBooleanProp(OTConfig.USE_ASM, false)){
         		Class<? extends AbstractOTObject> generatedClass = 
         			getASMClassLoader().generateClass(otObjectClass, otObjectImpl.otClass());
         		OTObjectInternal internalObj = generatedClass.newInstance();
         		otObject = (T)internalObj;
         		internalObj.setup(otObjectImpl);
         		internalObj.setEventSource(internalObj);
         	} else {        	
 
         		OTBasicObjectHandler handler = new OTBasicObjectHandler(otObjectImpl, otrunk, otObjectClass);
 
         		try {
         			otObject = (T)Proxy.newProxyInstance(otObjectClass.getClassLoader(),
         				new Class[] { otObjectClass }, handler);
         			handler.setOTObject(otObject);
         		} catch (ClassCastException e){
         			throw new RuntimeException("The OTClass: " + otObjectClass + 
         				" does not extend OTObject or OTObjectInterface", e);
         		}
         	}
         } else if(AbstractOTObject.class.isAssignableFrom(otObjectClass)){
     		Class<? extends AbstractOTObject> generatedClass = 
     			getASMClassLoader().generateClass(otObjectClass, otObjectImpl.otClass());
     		OTObjectInternal internalObj = generatedClass.newInstance();
     		otObject = (T)internalObj;
     		internalObj.setup(otObjectImpl);
     		internalObj.setEventSource(internalObj);        	
         } else {
             otObject = setResourcesFromSchema(otObjectImpl, otObjectClass);
         }
 
         
         notifyLoaded(otObject);
         
         otObject.init();
         
         otrunk.putLoadedObject(otObject, otObjectImpl.getGlobalId());
          
         return otObject;        
     }
     
     /**
 	 * @param otObject
 	 */
 	protected void notifyLoaded(OTObject otObject) 
 	{
 		for(int i=0; i < listeners.size(); i++) {
 			(listeners.get(i)).objectLoaded(otObject);
 		}
 	}
 
 	/**
      * Track down the objects schema by looking at the type
      * of class of the argument to setResources method
      * 
      * @param dataObject
      * @param otObject
      */
     @SuppressWarnings("unchecked")
     public <T extends OTObject> T setResourcesFromSchema(OTObjectInternal otObjectImpl, Class<T> otObjectClass)
     {
        Constructor<T> [] memberConstructors = (Constructor<T> [])otObjectClass.getConstructors();
         Constructor<T> resourceConstructor = memberConstructors[0]; 
         Class<?> [] params = resourceConstructor.getParameterTypes();
         
         if(memberConstructors.length > 1) {
             System.err.println("OTObjects should only have 1 constructor");
             return null;
         }
         
         if(params == null | params.length == 0) {
             try {
                 return otObjectClass.newInstance();
             } catch (Exception e) {
                 e.printStackTrace();
                 return null;
             }
         }
         
         OTResourceSchemaHandler handler = null;
         
         Object constructorParams [] = new Object [params.length];
         int nextParam = 0;
         if(params[0].isInterface() && 
                 OTResourceSchema.class.isAssignableFrom(params[0])){
             Class<? extends OTResourceSchema> schemaClass = (Class<? extends OTResourceSchema>)params[0];
                 
             handler = new OTResourceSchemaHandler(otObjectImpl, otrunk, 
             	 schemaClass);
 
             Class<?> [] interfaceList = new Class[] { schemaClass };
             
             Object resources = 
                 Proxy.newProxyInstance(schemaClass.getClassLoader(),
                     interfaceList, handler);
             
             constructorParams[0] = resources;
             nextParam++;
         }
         
         for(int i=nextParam; i<params.length; i++) {
             // look for a service in the services list to can 
             // be used for this param
             constructorParams[i] = otrunk.getService(params[i]);
             
             if(constructorParams[i] == null) {
                 System.err.println("No service could be found to handle the\n" +
                         " requirement of: " + otObjectClass + "\n" +
                         " for: " + params[i]);              
                 return null;                
             }
         }
         
         T otObject = null;
         try {
             otObject = resourceConstructor.newInstance(constructorParams);
             
             // now we need to pass the otObject to the schema handler so it can
             // set that as the source of OTChangeEvents
             handler.setEventSource(otObject);
             
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         }       
         
         return otObject;
     }
 
     boolean managesObject(OTID id)
     {    	
         if(id instanceof OTTransientMapID) {
             Object mapToken = ((OTTransientMapID)id).getMapToken();
             if (creationDb.getDatabaseId() == null) {
             	System.err.println("Database with a null id!");
             	return false;
             }
             return creationDb.getDatabaseId().equals(mapToken);            
         }
 
         // Check our mainDb to see if it contains the object
         // FIXME there is an issue here about what the difference between the 
         // mainDb and the creationDb.
         return mainDb.contains(id);
     }
     
     private OTDataObject createDataObject(OTDataObjectType type)
         throws Exception
     {
         return creationDb.createDataObject(type);
     }
     
     /**
      *  
      * @param dataParent
      * @param childID
      * @return
      * @throws Exception
      */
     OTDataObject getOTDataObject(OTID childID)
         throws Exception
     {
         // sanity check
         if(childID == null) {
             throw new Exception("Null child Id");
         }
 
         OTDataObject childDataObject = mainDb.getOTDataObject(null, childID);
 
         return childDataObject;
     }
 
 	/* (non-Javadoc)
 	 * @see org.concord.framework.otrunk.OTObjectService#copyObject(org.concord.framework.otrunk.OTObject, int)
 	 */
 	public OTObject copyObject(OTObject original, int maxDepth) 
 	throws Exception	
 	{
 		OTObjectList orphanObjectList = null;
 		
 		OTDataObject rootDO = otrunk.getRootDataObject();		
 		OTObject root = getOTObject(rootDO.getGlobalId());
 		if(root instanceof OTSystem) {
 			orphanObjectList = ((OTSystem)root).getLibrary();
 		}
 
 		return copyObject(original, orphanObjectList, maxDepth);
 	}
 	
 	public OTObject copyObject(OTObject original, OTObjectList orphanObjectList, 
 	                           int maxDepth) 
 		throws Exception	
 		{
 		// make a copy of the original objects data object
 		// it is easier to copy data objects than the actual objects
 		
 		OTDataObject originalDataObject = getOTDataObject(original);
 		
 		// Assume the object list is our object list impl
 		OTDataList orphanDataList = 
 			((OTObjectListImpl)orphanObjectList).getDataList();
 		
 		OTDataObject copyDataObject = 
 			DataObjectUtil.copy(originalDataObject, creationDb, 
 					orphanDataList, maxDepth, this, otrunk.getDataObjectFinder(), false);
 
 		return getOTObject(copyDataObject.getGlobalId());		
 	}
 	
 	public void copyInto(OTObject source, OTObject destination, int maxDepth, boolean onlyModifications) throws Exception {
 		OTObjectList orphanObjectList = null;
 		
 		OTDataObject rootDO = otrunk.getRootDataObject();		
 		OTObject root = getOTObject(rootDO.getGlobalId());
 		if(root instanceof OTSystem) {
 			orphanObjectList = ((OTSystem)root).getLibrary();
 		}
 		
 		// make a copy of the original objects data object
 		// it is easier to copy data objects than the actual objects
 		
 		OTDataObject sourceDO = getOTDataObject(source);
 		OTDataObject destDO = getOTDataObject(destination);
 		
 		// Assume the object list is our object list impl
 		OTDataList orphanDataList = null;
 		if (orphanObjectList != null) {
 			orphanDataList = ((OTObjectListImpl)orphanObjectList).getDataList();
 		}
 		
 		// OTDataObject copyDataObject = DataObjectUtil.copy(originalDataObject, creationDb, orphanDataList, maxDepth, this, otrunk.getDataObjectFinder());
 		ArrayList<OTObjectService> idProviders = new ArrayList<OTObjectService>();
 		idProviders.add(source.getOTObjectService());
 		idProviders.add(destination.getOTObjectService());
 		DataObjectUtil.copyInto(sourceDO, destDO, orphanDataList, maxDepth, this, otrunk.getDataObjectFinder(), onlyModifications);
 	}
 
 	public void addObjectServiceListener(OTObjectServiceListener listener)
 	{
 		if(listeners.contains(listener)) {
 			return;
 		}
 		listeners.add(listener);
 	}
 
 	public void removeObjectServiceListener(OTObjectServiceListener listener)
 	{
 		listeners.remove(listener);
 	}
 
 	/* (non-Javadoc)
      * @see org.concord.framework.otrunk.OTObjectService#registerPackageClass(java.lang.Class)
      */
     public void registerPackageClass(Class<? extends OTPackage> packageClass)
     {
     	otrunk.registerPackageClass(packageClass);	    
     }
 
 	/* (non-Javadoc)
      * @see org.concord.framework.otrunk.OTObjectService#getOTrunkService(java.lang.Class)
      */
     public <T> T getOTrunkService(Class<T> serviceInterface)
     {
     	return otrunk.getService(serviceInterface);
     }
 
 	public String getExternalID(OTObject object)
     {
 		OTID globalId = object.getGlobalId();
 		return getExternalID(globalId);
     }
 	
 	public String getExternalID(OTID otid)
     {
 		if(mainDb instanceof CompositeDatabase){
 			return ((CompositeDatabase)mainDb).resolveID(otid).toExternalForm();
 		}
 		
 		if(otid instanceof OTTransientMapID){
 			throw new RuntimeException("Cannot get an external id for " + otid +
 					" using this object service with mainDb: " + mainDb);
 		}
 		return otid.toExternalForm();
     }
 
 	static OTDataObject getOTDataObject(OTObject otObject)
 	{
 		if(otObject instanceof OTObjectInternal){
 			return ((OTObjectInternal)otObject).dataObject;
 		}
 		
 		return OTInvocationHandler.getOTDataObject(otObject);
 	}
 	
 	public URL getCodebase(OTObject otObject)
     {
 		try {
 	        OTDataObject dataObject = getOTDataObject(otObject); 
 	        return dataObject.getCodebase();
         } catch (Exception e) {
 	        e.printStackTrace();
         }
 		
 	    return null;
     }
 
 	public void preserveUUID(OTObject otObject)
     {
 		OTID id = otObject.getGlobalId();
 
 		if(!(id instanceof OTUUID)){
 			logPreserveUUIDError("object does not have a UUID " + otObject);
 			return;
 		}
 				
 		try {
 	        OTDataObject dataObject = getOTDataObject(id);
 	        if(!(dataObject instanceof XMLDataObject)){
 				logPreserveUUIDError("object is not backed by a XMLDatabase " + otObject);
 				return;
 	        }
 	        
 	        ((XMLDataObject)dataObject).setPreserveUUID(true);
         } catch (Exception e) {
 	        e.printStackTrace();
         }
     }
 
 	private void logPreserveUUIDError(String string)
     {
 		logger.warning(string);
 
 		Throwable throwable = 
 			new IllegalArgumentException(string);
 
 	    StackTraceElement stackTraceElement = throwable.getStackTrace()[2];
 	    String callerStr = stackTraceElement.getClassName() + "." + 
 	    	stackTraceElement.getMethodName();
 	    Object value = preserveUUIDCallers.get(callerStr);
 	    if(value != null){
 	    	return;
 	    }
 	    preserveUUIDCallers.put(callerStr, callerStr);
 	    logger.log(Level.FINE, "first call from method which caused bad preserveUUID call",
 	    	throwable);
     }
 
 }
