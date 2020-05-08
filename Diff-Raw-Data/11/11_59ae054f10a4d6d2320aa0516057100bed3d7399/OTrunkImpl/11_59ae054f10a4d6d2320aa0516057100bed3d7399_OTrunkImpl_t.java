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
  * Created on Aug 16, 2004
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Generation - Code and Comments
  */
 package org.concord.otrunk;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.lang.ref.Reference;
 import java.lang.ref.WeakReference;
 import java.util.Collection;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.Vector;
 import java.util.WeakHashMap;
 
 import org.concord.framework.otrunk.OTID;
 import org.concord.framework.otrunk.OTObject;
 import org.concord.framework.otrunk.OTObjectList;
 import org.concord.framework.otrunk.OTObjectMap;
 import org.concord.framework.otrunk.OTObjectService;
 import org.concord.framework.otrunk.OTResourceList;
 import org.concord.framework.otrunk.OTResourceMap;
 import org.concord.framework.otrunk.OTUser;
 import org.concord.framework.otrunk.OTrunk;
 import org.concord.otrunk.datamodel.OTDataObject;
 import org.concord.otrunk.datamodel.OTDatabase;
 import org.concord.otrunk.datamodel.OTIDFactory;
 import org.concord.otrunk.datamodel.OTRelativeID;
 import org.concord.otrunk.user.OTReferenceMap;
 import org.concord.otrunk.user.OTTemplateDatabase;
 import org.concord.otrunk.user.OTUserDataObject;
 import org.concord.otrunk.user.OTUserObject;
 import org.concord.otrunk.view.document.OTCompoundDoc;
 import org.concord.otrunk.xml.XMLDataObject;
 import org.concord.otrunk.xml.XMLDatabase;
 
 /**
  * @author scott
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Generation - Code and Comments
  */
 public class OTrunkImpl implements OTrunk
 {
 	public static final String RES_CLASS_NAME = "otObjectClass";
 
 	protected Hashtable loadedObjects = new Hashtable();
 	protected Hashtable userTemplateDatabases = new Hashtable();
     protected Hashtable userObjectServices = new Hashtable();
 	protected Hashtable userDataObjects = new Hashtable();
 	protected WeakHashMap objectWrappers = new WeakHashMap();
 	protected Vector services = null;
 
 	protected OTDatabase rootDb;
 	
     protected OTObjectServiceImpl rootObjectService;
     
 	Vector databases = new Vector();
 	Vector users = new Vector();
 	
     Vector objectServices = new Vector();
     
 	//private transient PrintWriter pw;
 	
 	public OTrunkImpl(OTDatabase db)
 	{
 		this(db, null);
 	}
 
 	public OTrunkImpl(OTDatabase db, Object [] services)
 	{		
 		this.rootDb = db;
 		databases.add(db);
 		if(services != null) {
 			this.services = new Vector();
 			for(int i=0; i<services.length; i++) {
 				this.services.add(services[i]);
 			}
 		}
 		
         rootObjectService = new OTObjectServiceImpl(this);
         rootObjectService.setCreationDb(rootDb);
         rootObjectService.setMainDb(rootDb);
         
 		// We should look up if there are any sevices.
 		try {
 			OTObject root = getRealRoot();
 			if(!(root instanceof OTSystem)) {
 				return;
 			}
 			
 			OTObjectList serviceList = ((OTSystem)root).getServices();
 			
 			if(this.services == null) {
 				this.services = new Vector();
 			}
 			this.services.addAll(serviceList.getVector());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 *
      * @see org.concord.framework.otrunk.OTrunk#getOTID(java.lang.String)
      */
     public OTID getOTID(String otidStr)
     {
         return OTIDFactory.createOTID(otidStr);        
     }
 	
     /* (non-Javadoc)
 	 */
 	public OTObject createObject(Class objectClass)
 		throws Exception
 	{
         return rootObjectService.createObject(objectClass);
 	}
 
 	public void setRoot(OTObject obj) throws Exception
 	{
 		// FIXME this doesn't do a good job if there
 		// is an OTSystem
 		OTID id = obj.getGlobalId();
 		rootDb.setRoot(id);
 	}
 		
 	protected OTObject getRealRoot() throws Exception
 	{
 		OTDataObject rootDO = getRootDataObject();
 		if(rootDO == null) {
 			return null;
 		}
 		return rootObjectService.getOTObject(rootDO);
 	}
 	
 	public OTObject getRoot() throws Exception
 	{
 		OTObject root = getRealRoot();
 		if(root instanceof OTSystem) {
 			return ((OTSystem)root).getRoot();
 		}
 		
 		return root;
 	}
 	
 	/**
 	 * return the database that is serving this id
 	 * currently there is only one database, so this is
 	 * easy
 	 * 
 	 * @param id
 	 * @return
 	 */
 	protected OTDatabase getOTDatabase(OTID id)
 	{
 	    // look for the database that contains this id
 	    if(id == null) {
 	        return null;
 	    }
 	    
 	    for(int i=0; i<databases.size(); i++) {
 	        OTDatabase db = (OTDatabase)databases.get(i);
 	        if(db.contains(id)) {
 	            return db;
 	        }
 	    }
 	    return null;
 	}
 	
 	public void close()
 	{
 		rootDb.close();
 	}
 	
 	/**
 	 * Warning: this is method should only be used when you don't know
 	 * which object is requesting the new OTObject.  The requestion object
 	 * is currently used to keep the context of user mode or authoring mode
 	 * @param childID
 	 * @return
 	 * @throws Exception
 	 */
 	public OTObject getOTObject(OTID childID)
 		throws Exception
 	{
 		return rootObjectService.getOTObject(childID); 
 	}
 	
 	/**
 	 * This method is only used internally. Once a data object has
 	 * been tracked down then method is used to get the OTObject
 	 * it checks the cache of loadedObjects before making a new one
 	 * @param childDataObject
 	 * @return
 	 * @throws Exception
 	 */
 	private OTObject getOTObject(OTDataObject childDataObject)
 		throws Exception
 	{
         return rootObjectService.getOTObject(childDataObject);
 	}
 	
 	public Object getService(Class serviceInterface)
 	{
         if(services == null) {
             return null;
         }
         
 		for(int j=0; j<services.size(); j++) {
 			Object service = services.get(j);
 			if(serviceInterface.isInstance(service)) {
 				return service;
 			}
 		}
 		
 		return null;
 	}
 	
     public OTObjectService createObjectService(OTDatabase db)
     {
         OTObjectServiceImpl objService = new OTObjectServiceImpl(this);
         objService.setCreationDb(db);
         objService.setMainDb(db);
         
         objectServices.add(objService);
         
         return objService;
     }
     
     /**
      * This is a temporary method.  It works for files that
      * represent a single user.  This method finds that user and registers
      * them.  It could be modified to register all the users referenced in
      * the passed in database and in which case it should check if the user
      * is already registered with another database.  
      * 
      * @param userDataDb
      * @throws Exception
      */
     public OTUserObject registerUserDataDatabase(OTDatabase userDataDb, String name)
         throws Exception
     {
         // add this database as one of our databases
         if(!databases.contains(userDataDb)) {
             databases.add(userDataDb);
         }
         
         OTObjectService objService = createObjectService(userDataDb);
         OTDataObject rootDO = userDataDb.getRoot();
         
         OTStateRoot stateRoot = 
             (OTStateRoot)objService.getOTObject(rootDO.getGlobalId());
 
         OTObjectMap userMap = stateRoot.getUserMap();
         
         // find the user from this database.
         // this currently is the first user in the userMap
         Vector keys = userMap.getObjectKeys();
         OTReferenceMap refMap = (OTReferenceMap)userMap.getObject((String)keys.get(0));        
         OTUser user = refMap.getUser();
         OTUserObject aUser = (OTUserObject)user;
         if(name != null){
         	aUser.setName(name);
         }
         users.add(aUser);
 
         // create a template database that links this userDataDb with the rootDb
         // and uses the refMap to store the links between the two
         OTTemplateDatabase db = new OTTemplateDatabase(rootDb, userDataDb, refMap);          
 
         databases.add(db);
         
         // save this data base so getUserRuntimeObject can track down
         // objects related to this user
         userTemplateDatabases.put(user.getUserId(), db);        
         
         OTObjectService userObjService = createObjectService(db);
         userObjectServices.put(user.getUserId(), userObjService);
         
         return aUser;
     }
     
     public Hashtable getUserTemplateDatabases() {
     	return userTemplateDatabases;
     }
     
     public Vector getUsers() {
     	return users;
     }
     
     public boolean hasUserModified(OTObject authoredObject, OTUser user) throws Exception
     {
         OTID authoredId = authoredObject.getGlobalId();
         OTID userId = user.getUserId();
         OTTemplateDatabase db = (OTTemplateDatabase)userTemplateDatabases.get(userId);
         
         if(db == null) {
             // FIXME this should throw an exception
             return false;
         } 
 
         OTDataObject userDataObject = db.getOTDataObject(null, authoredId);

         boolean modified = false;
         if(userDataObject instanceof OTUserDataObject) {
             OTDataObject userModifications = ((OTUserDataObject)userDataObject).getExistingUserObject();
             modified = (userModifications != null);
             //return  userModifications != null;
         }
         //if(modified) System.out.println(userDataObject.getGlobalId().toString() + " is modified: " + modified);
         //return false;
         return modified;
     }
 
     public OTObjectService initUserObjectService(OTObjectServiceImpl objService, OTUser user, OTStateRoot stateRoot)
     throws Exception
     {
         OTID userId = user.getUserId();
         
         // this should probably look for the user object service instead
         // of the template database
         OTTemplateDatabase db = (OTTemplateDatabase)userTemplateDatabases.get(userId);
         OTObjectService userObjService = 
             (OTObjectService)userObjectServices.get(userId);
         
         if(userObjService == null) {            
             OTObjectMap userStateMapMap = stateRoot.getUserMap();
             
             OTReferenceMap userStateMap = (OTReferenceMap)userStateMapMap.getObject(userId.toString());
             if(userStateMap == null) {
                 // this is inferring that the createObject method will
                 // create the object in the correct database.  
                 userStateMap = 
                     (OTReferenceMap)objService.createObject(OTReferenceMap.class);
                 userStateMapMap.putObject(userId.toString(), userStateMap);
                 userStateMap.setUser((OTUserObject)user);
             }
             
             
             db = new OTTemplateDatabase(rootDb, objService.getCreationDb(), userStateMap);          
             databases.add(db);
             userObjService = createObjectService(db);
             userTemplateDatabases.put(userId, db);
             userObjectServices.put(userId,userObjService);
         }
         
         return userObjService;
     }
     
     
     
 	public OTObject getUserRuntimeObject(OTObject authoredObject, OTUser user)
 		throws Exception
 	{
 		//authoredObject = getRuntimeAuthoredObject(authoredObject, user);
 		
 		OTID authoredId = authoredObject.getGlobalId();
 		OTID userId = user.getUserId();
         OTObjectService objService = 
             (OTObjectService)userObjectServices.get(userId);
 
         // the objService should be non null if not this is coding error
         // that needs to be fixed so we will just let it throw an null pointer
         // exception
         return objService.getOTObject(authoredId);
 
         /*
 		OTTemplateDatabase db = (OTTemplateDatabase)userTemplateDatabases.get(userId);
 		
 		if(db == null) {
 		    OTDataObject stateRootDO = creationDb.getRoot();
             if(stateRootDO == null) {
                 throw new RuntimeException("user database root is null");
             }
 		    OTStateRoot stateRoot = (OTStateRoot)getOTObject(stateRootDO);
 		    OTObjectMap userStateMapMap = stateRoot.getUserMap();
 
 		    OTReferenceMap userStateMap = (OTReferenceMap)userStateMapMap.getObject(userId.toString());
 		    if(userStateMap == null) {
 		        // this is inferring that the createObject method will
 		        // create the object in the correct database.  
 		        userStateMap = (OTReferenceMap)createObject(OTReferenceMap.class);
 		        userStateMapMap.putObject(userId.toString(), userStateMap);
 		        userStateMap.setUser((OTUserObject)user);
 		    }
 		    
 		    
 		    db = new OTTemplateDatabase(rootDb, creationDb, userStateMap);		    
 		    databases.add(db);
 		}
 				
 		OTDataObject userDataObject = db.getOTDataObject(null, authoredId);
 		
 		return getOTObject(userDataObject);
         */		
 	}
 	
 	public OTObject getRuntimeAuthoredObject(OTObject userObject, OTUser user)
 	    throws Exception 
     {
 		OTID objectId = userObject.getGlobalId();
 		OTID userId = user.getUserId();
 		OTTemplateDatabase db = (OTTemplateDatabase)userTemplateDatabases.get(userId);
 				
 	    if(objectId instanceof OTRelativeID) {
 	    	//System.out.println("is relative");
     		OTID childRootId = ((OTRelativeID)objectId).getRootId();
     		if(childRootId != null && childRootId.equals(db.getDatabaseId())) {
     			//System.out.print("   equals to databaseid");
     			objectId = ((OTRelativeID)objectId).getRelativeId();
     			//System.out.println(": " + objectId.toString());
 
     			return getOTObject(objectId);		    			
     		}
     	}
 
 	    return userObject;
 	}
 
 	public OTObject getRootObject(OTDatabase db)
 		throws Exception
 	{
 	    OTDataObject rootDO = db.getRoot();
 	    OTObject rootObject = getOTObject(rootDO);
 
 	    return rootObject;
 	}
 	
 	/**
 	 * @return
 	 */
 	public OTDataObject getRootDataObject()
 		throws Exception
 	{
 		return rootDb.getRoot();
 	}
 
     /**
      * @return
      */
     public OTObject getFirstObjectNoUserData()
     	throws Exception
     {
 		OTObject root = getRealRoot();
 		if(!(root instanceof OTSystem)) {
 			return null;
 		}
 		
 		return ((OTSystem)root).getFirstObjectNoUserData();
     }
     
     void putLoadedObject(OTObject otObject, OTDataObject dataObject)
     {
         WeakReference objRef = new WeakReference(otObject);
         loadedObjects.put(dataObject, objRef);
     }
     
     OTObject getLoadedObject(OTDataObject dataObject)
     {
         Reference otObjectRef = (Reference)loadedObjects.get(dataObject);
         if(otObjectRef != null) {
             OTObject otObject = (OTObject)otObjectRef.get();
             if(otObject != null) {
                 return otObject;
             }
             
             loadedObjects.remove(dataObject);
         }
 
         return null;
     }
     
     /**
      * This method is used by object services that can't handle a requested object
      * this happens in reports when a report object needs to access a user object.
      * 
      * It might be possible to clean this up by explicitly giving the object service
      * of the report access to the users objects. 
      * 
      * @param childID
      * @return
      * @throws Exception
      */
    OTObject getOrphanOTObject(OTID childID, OTObjectServiceImpl oldService)
         throws Exception
     {
         for(int i=0; i<objectServices.size(); i++) {
             OTObjectServiceImpl objService = (OTObjectServiceImpl)objectServices.get(i);
            // To avoid infinite loop, the objService must not equal to oldService
            if(objService.managesObject(childID) && objService != oldService) {
            	return objService.getOTObject(childID);
             }
         }
         
         System.err.println("Data object is not found for: " + childID);
         return null;
     }    
 }
