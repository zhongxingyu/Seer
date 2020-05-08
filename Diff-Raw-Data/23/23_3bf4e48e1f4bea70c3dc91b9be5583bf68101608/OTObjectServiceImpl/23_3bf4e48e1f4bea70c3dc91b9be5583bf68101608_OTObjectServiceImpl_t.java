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
 * $Revision: 1.24 $
 * $Date: 2007-09-29 04:30:29 $
  * $Author: scytacki $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.otrunk;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Proxy;
 import java.util.Vector;
 
 import org.concord.framework.otrunk.OTControllerRegistry;
 import org.concord.framework.otrunk.OTControllerService;
 import org.concord.framework.otrunk.OTID;
 import org.concord.framework.otrunk.OTObject;
 import org.concord.framework.otrunk.OTObjectList;
 import org.concord.framework.otrunk.OTObjectService;
 import org.concord.framework.otrunk.OTResourceSchema;
 import org.concord.framework.otrunk.otcore.OTClass;
 import org.concord.otrunk.datamodel.DataObjectUtil;
 import org.concord.otrunk.datamodel.OTDataList;
 import org.concord.otrunk.datamodel.OTDataObject;
 import org.concord.otrunk.datamodel.OTDataObjectType;
 import org.concord.otrunk.datamodel.OTDatabase;
 import org.concord.otrunk.datamodel.OTTransientMapID;
 import org.concord.otrunk.otcore.impl.ReflectiveOTClassFactory;
 import org.concord.otrunk.overlay.CompositeDatabase;
 
 public class OTObjectServiceImpl
     implements OTObjectService
 {
     protected OTrunkImpl otrunk;
     protected OTDatabase creationDb;
     protected OTDatabase mainDb;
     protected Vector listeners = new Vector();
 
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
     
     public OTObject createObject(Class objectClass) 
         throws Exception
     {
     	OTObjectInternal otObjectImpl = createOTObjectInternal(objectClass);
         OTObject newObject = loadOTObject(otObjectImpl, objectClass);
 
         return newObject;
     }
 
     protected OTObjectInternal createOTObjectInternal(Class objectClass)
     	throws Exception
     {
     	String className = objectClass.getName();
     	OTDataObjectType type = new OTDataObjectType(objectClass.getName());
         OTDataObject dataObject = createDataObject(type); 
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
     	OTObjectInternal otObjectImpl = 
     		new OTObjectInternal(dataObject, this, otClass);
     	return otObjectImpl;
     }
     
     public OTObject getOTObject(OTID childID) throws Exception
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
         	return otrunk.getOrphanOTObject(childID, this);
         }
 
         // Look for our object to see it is already setup in the otrunk list of loaded objects
         // it might be better to have each object service maintain its own list of loaded objects
         OTObject otObject = otrunk.getLoadedObject(childDataObject.getGlobalId());
         if(otObject != null) {
             return otObject;
         }
 
     	String otObjectClassStr = OTrunkImpl.getClassName(childDataObject);
         if(otObjectClassStr == null) {
             return null;
         }            
         Class otObjectClass = Class.forName(otObjectClassStr);
         
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
     		(OTControllerRegistry) otrunk.getService(OTControllerRegistry.class);
     	return new OTControllerServiceImpl(this, registry);
     }
     
     public OTObject loadOTObject(OTObjectInternal otObjectImpl, Class otObjectClass)
     throws  Exception
     {
         OTObject otObject = null;
         
         if(otObjectClass.isInterface()) {
             OTBasicObjectHandler handler = new OTBasicObjectHandler(otObjectImpl, otrunk, otObjectClass);
 
             try {
             	otObject = (OTObject)Proxy.newProxyInstance(otObjectClass.getClassLoader(),
             			new Class[] { otObjectClass }, handler);
             	handler.setOTObject(otObject);
             } catch (ClassCastException e){
             	throw new RuntimeException("The OTClass: " + otObjectClass + 
             			" does not extend OTObject or OTObjectInterface", e);
             }
 
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
 			((OTObjectServiceListener)listeners.get(i)).objectLoaded(otObject);
 		}
 	}
 
 	/**
      * Track down the objects schema by looking at the type
      * of class of the argument to setResources method
      * 
      * @param dataObject
      * @param otObject
      */
     public OTObject setResourcesFromSchema(OTObjectInternal otObjectImpl, Class otObjectClass)
     {
         Constructor [] memberConstructors = otObjectClass.getConstructors();
         Constructor resourceConstructor = memberConstructors[0]; 
         Class [] params = resourceConstructor.getParameterTypes();
         
         if(memberConstructors.length > 1) {
             System.err.println("OTObjects should only have 1 constructor");
             return null;
         }
         
         if(params == null | params.length == 0) {
             try {
                 return (OTObject)otObjectClass.newInstance();
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
             Class schemaClass = params[0];
                 
             handler = new OTResourceSchemaHandler(otObjectImpl, otrunk, schemaClass);
 
             Class [] interfaceList = new Class[] { schemaClass };
             
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
         
         OTObject otObject = null;
         try {
             otObject = (OTObject)resourceConstructor.newInstance(constructorParams);
             
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
             return mapToken == creationDb.getDatabaseId();            
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
     private OTDataObject getOTDataObject(OTID childID)
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
 		
 		OTObjectServiceImpl originalObjectService = (OTObjectServiceImpl) original.getOTObjectService();
 		
 		OTDataObject originalDataObject = 
 			originalObjectService.getOTDataObject(original.getGlobalId());				
 		
 		// Assume the object list is our object list impl
 		OTDataList orphanDataList = 
 			((OTObjectListImpl)orphanObjectList).getDataList();
 		
 		OTDataObject copyDataObject = 
 			DataObjectUtil.copy(originalDataObject, creationDb, 
 					orphanDataList, maxDepth);
 
 		return getOTObject(copyDataObject.getGlobalId());		
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
     public void registerPackageClass(Class packageClass)
     {
     	otrunk.registerPackageClass(packageClass);	    
     }
 
 	/* (non-Javadoc)
      * @see org.concord.framework.otrunk.OTObjectService#getOTrunkService(java.lang.Class)
      */
     public Object getOTrunkService(Class serviceInterface)
     {
     	return otrunk.getService(serviceInterface);
     }
 
 	public String getExternalID(OTObject object)
     {
 		OTID globalId = object.getGlobalId();
 		if(mainDb instanceof CompositeDatabase){
 			return ((CompositeDatabase)mainDb).resolveID(globalId).toString();
 		}
 		
 		return globalId.toString();
     }
 
 }
