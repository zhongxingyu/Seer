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
 
 import java.io.IOException;
 import java.lang.ref.Reference;
 import java.lang.ref.WeakReference;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Vector;
 import java.util.WeakHashMap;
 
 import org.concord.framework.otrunk.OTBundle;
 import org.concord.framework.otrunk.OTControllerRegistry;
 import org.concord.framework.otrunk.OTID;
 import org.concord.framework.otrunk.OTObject;
 import org.concord.framework.otrunk.OTObjectList;
 import org.concord.framework.otrunk.OTObjectMap;
 import org.concord.framework.otrunk.OTObjectService;
 import org.concord.framework.otrunk.OTPackage;
 import org.concord.framework.otrunk.OTServiceContext;
 import org.concord.framework.otrunk.OTUser;
 import org.concord.framework.otrunk.OTrunk;
 import org.concord.framework.otrunk.otcore.OTClass;
 import org.concord.otrunk.datamodel.OTDataObject;
 import org.concord.otrunk.datamodel.OTDataObjectFinder;
 import org.concord.otrunk.datamodel.OTDataObjectType;
 import org.concord.otrunk.datamodel.OTDatabase;
 import org.concord.otrunk.datamodel.OTIDFactory;
 import org.concord.otrunk.datamodel.OTTransientMapID;
 import org.concord.otrunk.overlay.CompositeDataObject;
 import org.concord.otrunk.overlay.CompositeDatabase;
 import org.concord.otrunk.overlay.OTOverlay;
 import org.concord.otrunk.overlay.Overlay;
 import org.concord.otrunk.overlay.OverlayImpl;
 import org.concord.otrunk.user.OTReferenceMap;
 import org.concord.otrunk.user.OTUserObject;
 import org.concord.otrunk.view.OTConfig;
 import org.concord.otrunk.xml.XMLDatabase;
 
 /**
  * @author scott
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Generation - Code and Comments
  */
 public class OTrunkImpl implements OTrunk
 {
 	protected Hashtable loadedObjects = new Hashtable();
 	protected Hashtable compositeDatabases = new Hashtable();
     protected Hashtable userObjectServices = new Hashtable();
 	protected Hashtable userDataObjects = new Hashtable();
 	protected WeakHashMap objectWrappers = new WeakHashMap();
 
 	OTServiceContext serviceContext = new OTServiceContextImpl();	
 
 	protected OTDatabase rootDb;
 	protected OTDatabase systemDb;
 	
     protected OTObjectServiceImpl rootObjectService;
     
 	Vector databases = new Vector();
 	Vector users = new Vector();
 	
     Vector objectServices = new Vector();
 
 	private Vector registeredPackageClasses = new Vector();
 	private OTObjectServiceImpl systemObjectService;;
 
 	private static HashMap otClassMap = new HashMap(); 
 	
     public final static String getClassName(OTDataObject dataObject)
     {
     	OTDataObjectType type = dataObject.getType();
     	return type.getClassName();
     }
     
     public OTrunkImpl(OTDatabase db)
 	{
		this((OTDatabase)null, db, (ArrayList)null);
 	}
 
 	public OTrunkImpl(OTDatabase db, ArrayList services)
 	{	
 		this(null, db, services);
 	}
 	
 	public OTrunkImpl(OTDatabase systemDb, OTDatabase db, ArrayList services) 
 	{	
 		try {
 	        URL dummyURL = new URL("http://www.concord.org");
 	        URLConnection openConnection = dummyURL.openConnection();
 	        openConnection.setDefaultUseCaches(true);
         } catch (MalformedURLException e1) {
 	        // TODO Auto-generated catch block
 	        e1.printStackTrace();
         } catch (IOException e) {
 	        // TODO Auto-generated catch block
 	        e.printStackTrace();
         }
 		
 		// Setup the services this has to be done before addDatabase
 		// because addDatabase initializes the OTPackages loaded by that
 		// database
         
         // Add ourself as a service, this is needed so 
         serviceContext.addService(OTrunk.class, this);
         
 		if(services != null) {
 			for(int i=0; i<services.size(); i++){
 				OTrunkServiceEntry entry = (OTrunkServiceEntry) services.get(i);
 				serviceContext.addService(entry.serviceInterface, entry.service);
 			}
 		}
 		
 		serviceContext.addService(OTControllerRegistry.class, 
 				new OTControllerRegistryImpl());
 
 		// We should look up if there are any sevices.
 		try {
 			if(systemDb != null){
 				this.systemDb = systemDb;
 				systemObjectService = initObjectService(systemDb, "system");
 			} 
 			
 			this.rootDb = db;
 			rootObjectService = initObjectService(rootDb, "root");        
 
 			if(systemObjectService == null){
 				// there is no real system db so just use the main db
 				this.systemDb = db;
 				systemObjectService = rootObjectService;			
 			}
 					
 			OTSystem otSystem = getSystem();
 			if(otSystem == null){
 				System.err.println("Warning: No OTSystem object found");
 				return;
 			}
 			
 			// This is deprecated but we use it anyhow for backward compatibility
 			OTObjectList serviceList = otSystem.getServices(); 
 			
 			OTObjectList bundleList = otSystem.getBundles();
 			
 			Vector combined = new Vector();
 			combined.addAll(serviceList.getVector());
 			combined.addAll(bundleList.getVector());
 			
 			for(int i=0; i<combined.size(); i++){
 				OTBundle bundle = (OTBundle)combined.get(i);
 				bundle.registerServices(serviceContext);
 			}
 			
 			for(int i=0; i<combined.size(); i++){
 				OTBundle bundle = (OTBundle)combined.get(i);
 				bundle.initializeBundle(serviceContext);
 			}			
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
 	
 	public OTSystem getSystem() throws Exception
 	{
 		OTDataObject systemDO = systemDb.getRoot();
 		if (systemDO == null) {
 			return null;
 		}
 		OTID systemID = systemDO.getGlobalId();
 		OTObject systemObject = systemObjectService.getOTObject(systemID);
 		if(systemObject instanceof OTSystem){
 			return (OTSystem) systemObject;
 		}
 		return null;
 	}
 	
 	public OTObject getRealRoot() throws Exception
 	{
 		OTDataObject rootDO = getRootDataObject();
 		if(rootDO == null) {
 			return null;
 		}
 		return rootObjectService.getOTObject(rootDO.getGlobalId());
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
 	public OTDatabase getOTDatabase(OTID id)
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
 	
 	public Object getService(Class serviceInterface)
 	{
 		return serviceContext.getService(serviceInterface);
 	}
 	
     public OTObjectServiceImpl createObjectService(OTDatabase db)
     {
         OTObjectServiceImpl objService = new OTObjectServiceImpl(this);
         objService.setCreationDb(db);
         objService.setMainDb(db);
      
         registerObjectService(objService, "" + db);
         
         return objService;
     }
 
     public void registerObjectService(OTObjectServiceImpl objService, String label)
     {
         objectServices.add(objService);
 
         if(OTConfig.isTrace()){
         	objService.addObjectServiceListener(new TraceListener(label));
         }    	
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
     	addDatabase(userDataDb);
         
     	OTReferenceMap refMap = getReferenceMapFromUserDb(userDataDb);
 
     	OTUser user = refMap.getUser();
         OTUserObject aUser = (OTUserObject)user;
         if(name != null){
         	aUser.setName(name);
         }
         users.add(aUser);
 
         setupUserDatabase(user, refMap);
 
         return aUser;
     }
 
     /**
      * the parentObjectService needs to be passed in so the returned object
      * uses the correct layers based on the context in which this method is
      * called.
      * 
      * @param url
      * @param parentObjectService
      * @return
      * @throws Exception
      */
     public OTObject getExternalObject(URL url, OTObjectService parentObjectService) 
     	throws Exception
     {
     	OTObjectServiceImpl externalObjectService = 
     		loadDatabase(url);
 		
     	// get the root object either the real root or the system root
     	OTObject root = getRoot(externalObjectService);
     	
     	return parentObjectService.getOTObject(root.getGlobalId());
     }
     
     protected OTObjectServiceImpl loadDatabase(URL url) 
     	throws Exception
     {
     	// load the data base
 		XMLDatabase includeDb = new XMLDatabase(url);
 
 		if(databases.contains(includeDb)){
 			// we've already loaded this database.
 			// It is nice to print an error here because the equals method of the databases
 			// just use the database id.  So if 2 databases have the same id then it will
 			// seem like the database has already been loaded.
 			// FIXME this should check the url of the database, and only print this message
 			// if the urls are different.
 			System.err.println("already loaded database with id: " + includeDb.getDatabaseId() + 
 					" database: " + url.toExternalForm() + " will not be loaded again");
 			
 			for(int i=0; i<objectServices.size(); i++){
 				OTObjectServiceImpl objectService = (OTObjectServiceImpl) objectServices.get(i);
 				OTDatabase db = objectService.getMainDb();
 				if(db.equals(includeDb)){
 					return objectService;
 				}
 			}
 			
 			System.err.println("Cannot find objectService for database: " + includeDb.getDatabaseId());
 			return null;			
 		}
 		
     	// register it with the OTrunkImpl
 		// well track the resource info to be safe here.
 		includeDb.setTrackResourceInfo(true);
 		includeDb.loadObjects();
     	
 		return initObjectService(includeDb, url.toExternalForm());
     }
     
     protected OTObject getRoot(OTObjectServiceImpl objectService) 
     	throws Exception
     {
     	OTDatabase db = objectService.getMainDb();
 
     	OTDataObject rootDO = db.getRoot();
 
     	OTObject root = objectService.getOTObject(rootDO.getGlobalId());
     	
 		if(root instanceof OTSystem) {
 			return ((OTSystem)root).getRoot();
 		}
 		
 		return root;
     }
     
     protected OTObjectServiceImpl initObjectService(OTDatabase db, String logLabel) 
     	throws Exception
     {
 		addDatabase(db);
 		OTObjectServiceImpl objectService = createObjectService(db);
         if(OTConfig.isTrace()) {
         	objectService.addObjectServiceListener(new TraceListener(logLabel + ": " + db));
         }
         loadIncludes(objectService);
         
         return objectService;
     }
     
     protected void loadIncludes(OTObjectServiceImpl objectService) 
     	throws Exception
     {
     	OTDatabase db = objectService.getMainDb();    	
 		OTDataObject rootDO = db.getRoot();
 		if (rootDO == null) {
 			return;
 		}
 		OTID rootID = rootDO.getGlobalId();
 		OTObject otRoot = objectService.getOTObject(rootID);
 		if(!(otRoot instanceof OTSystem)){
 			return;
 		}
 				
 		OTSystem otSystem = (OTSystem) otRoot;
 		
 		OTObjectList includes = otSystem.getIncludes();
 		
 		for(int i=0; i<includes.size(); i++){
 			OTInclude include = (OTInclude) includes.get(i);
 			
 			URL hrefUrl = include.getHref();
 
 			loadDatabase(hrefUrl);			
 		}
     }
     
     protected OTReferenceMap getReferenceMapFromUserDb(OTDatabase userDataDb) 
     	throws Exception
     {
         OTObjectService objService = createObjectService(userDataDb);
 
         OTDataObject rootDO = userDataDb.getRoot();
         
         OTStateRoot stateRoot = 
             (OTStateRoot)objService.getOTObject(rootDO.getGlobalId());
 
         OTObjectMap userMap = stateRoot.getUserMap();
         
         // find the user from this database.
         // this currently is the first user in the userMap
         Vector keys = userMap.getObjectKeys();
         OTReferenceMap refMap = (OTReferenceMap)userMap.getObject((String)keys.get(0));        
     	
         return refMap;
     }
     
     public void reloadOverlays(OTUserObject user, OTDatabase userDataDb) 
     	throws Exception
     {
     	OTID userId = user.getUserId();
     	
     	OTReferenceMap refMap = getReferenceMapFromUserDb(userDataDb);
     		
     	// need to make a new composite database.
     	// the user database should remain the same.
         OTDatabase oldCompositeDB = (OTDatabase) compositeDatabases.remove(userId);	
         userObjectServices.remove(userId);
         
         databases.remove(oldCompositeDB);
 
         setupUserDatabase(user, refMap);
     }
     
     protected OTObjectService setupUserDatabase(OTUser user, OTReferenceMap userStateMap)
     {
     	OTObjectServiceImpl userObjService;
     	OTID userId = user.getUserId();
     	
     	OTDataObjectFinder objectFinder = new OTDataObjectFinder()
     	{
 			public OTDataObject findDataObject(OTID id)
 				throws Exception
             {
 				OTDatabase db = getOTDatabase(id);
 				if (db == null)
 					System.out.println("********** "+id);
 				return db.getOTDataObject(null, id);
             }    		
     	};
     	
         CompositeDatabase userDb = new CompositeDatabase(objectFinder, userStateMap);
 
         addDatabase(userDb);
         userObjService = createObjectService(userDb);
         compositeDatabases.put(userId, userDb);	
         userObjectServices.put(userId, userObjService);
 
         // After the user database is complete setup, now we get the overlays.  This way 
         // the user can change the overlays list.
     	try {
         	ArrayList overlays = null;
 	        OTObjectList otOverlays = getSystemOverlays(user);
 	        if(otOverlays != null && otOverlays.size() > 0){
 	        	overlays = new ArrayList();
 	        	for(int i=0; i<otOverlays.size(); i++){
 	        		OTOverlay otOverlay = (OTOverlay) otOverlays.get(i);
 	        		Overlay overlay = new OverlayImpl(otOverlay);
 	        		if (overlay != null)
 	        			overlays.add(overlay);
 	        	}
 	        }
 	        userDb.setOverlays(overlays);
         } catch (Exception e) {
 	        // TODO Auto-generated catch block
 	        e.printStackTrace();
         }    	
     	
 
         return userObjService;
     }
     
     public Hashtable getCompositeDatabases() {
     	return compositeDatabases;
     }
     
     public Vector getUsers() {
     	return users;
     }
     
     public boolean hasUserModified(OTObject authoredObject, OTUser user) throws Exception
     {
         OTID authoredId = authoredObject.getGlobalId();
         OTID userId = user.getUserId();
         CompositeDatabase db = (CompositeDatabase)compositeDatabases.get(userId);
         
         if(db == null) {
             // FIXME this should throw an exception
             return false;
         } 
 
         OTDataObject userDataObject = db.getOTDataObject(null, authoredId);
 
         if(userDataObject instanceof CompositeDataObject) {
             OTDataObject userModifications = ((CompositeDataObject)userDataObject).getActiveDeltaObject();
             return userModifications != null;
         }
 
         return false;
     }
 
     public OTObjectService initUserObjectService(OTObjectServiceImpl objService, OTUser user, OTStateRoot stateRoot)
     throws Exception
     {
         OTID userId = user.getUserId();
         
         // this should probably look for the user object service instead
         // of the template database
         OTObjectService userObjService = 
             (OTObjectService)userObjectServices.get(userId);
         
         if(userObjService != null){
         	return userObjService;
         }
                 
         OTObjectMap userStateMapMap = stateRoot.getUserMap();
 
         OTReferenceMap userStateMap = (OTReferenceMap)userStateMapMap.getObject(userId.toExternalForm());
         if(userStateMap == null) {
         	// this is inferring that the createObject method will
         	// create the object in the correct database.  
         	userStateMap = 
         		(OTReferenceMap)objService.createObject(OTReferenceMap.class);
         	userStateMapMap.putObject(userId.toExternalForm(), userStateMap);
         	userStateMap.setUser((OTUserObject)user);
         }
 
         userObjService = setupUserDatabase(user, userStateMap);
     	return userObjService;
         
     }
     
     protected void addDatabase(OTDatabase db)
     {
         if(!databases.contains(db)) {
             databases.add(db);
             
             Vector packageClasses = db.getPackageClasses();
             if(packageClasses != null){
             	for(int i=0; i<packageClasses.size(); i++){
             		registerPackageClass((Class)packageClasses.get(i));
             	}
             }
         }
     }
     
 	/**
      * @param class1
      */
     public void registerPackageClass(Class packageClass)
     {
     	// check to see if this package has already been registered
     	if(registeredPackageClasses.contains(packageClass)){
     		return;
     	}
     	
     	registeredPackageClasses.add(packageClass);
     	OTPackage otPackage;
         try {
 	        otPackage = (OTPackage)packageClass.newInstance();
 	        Class [] dependencies = otPackage.getPackageDependencies();
 	        if(dependencies != null){
 	        	for(int i=0; i<dependencies.length; i++){
 	        		registerPackageClass(dependencies[i]);
 	        	}
 	        }
 	        
 	    	otPackage.initialize(this);
         } catch (InstantiationException e) {
 	        // TODO Auto-generated catch block
 	        e.printStackTrace();
         } catch (IllegalAccessException e) {
 	        // TODO Auto-generated catch block
 	        e.printStackTrace();
         }
 
     	return;
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
 
 	}
 	
 	/**
 	 * This method is a legacy method.  It should be generalized now that "user objects" and 
 	 * "author objects" are just specific versions of "overlay delta objects" and "base objects"
 	 * 
 	 * @param userObject
 	 * @param user
 	 * @return
 	 * @throws Exception
 	 */
 	public OTObject getRuntimeAuthoredObject(OTObject userObject, OTUser user)
 	    throws Exception 
     {
 		OTID objectId = userObject.getGlobalId();
 		OTID userId = user.getUserId();
 		
 		if(!(objectId instanceof OTTransientMapID)){
 			return userObject;
 		}
 				
 		CompositeDatabase db = (CompositeDatabase)compositeDatabases.get(userId);
 				
 		//System.out.println("is relative");
 		Object objectMapToken = ((OTTransientMapID) objectId).getMapToken();
 		if(objectMapToken != null && objectMapToken == db.getDatabaseId()) {
 			//System.out.print("   equals to databaseid");
 			objectId = ((OTTransientMapID) objectId).getMappedId();
 			//System.out.println(": " + objectId.toString());
 
 			return getOTObject(objectId);		    			
 		}
 		
 		
 	    return userObject;
 	}
 
 	public OTObject getRootObject(OTDatabase db)
 		throws Exception
 	{
 	    OTDataObject rootDO = db.getRoot();
 	    OTObject rootObject = rootObjectService.getOTObject(rootDO.getGlobalId());
 
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
 
 	public OTObjectList getSystemOverlays(OTUser user)
 		throws Exception
 	{
 		OTObject root = getRealRoot();
 		if(!(root instanceof OTSystem)) {
 			return null;
 		}
 		
 		OTSystem userRoot = (OTSystem) getUserRuntimeObject(root, user);
 		
 		return userRoot.getOverlays();		
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
 
     void putLoadedObject(OTObject otObject, OTID otId)
     {
     	WeakReference objRef = new WeakReference(otObject);
     	loadedObjects.put(otId, objRef);    		
     }
     
     OTObject getLoadedObject(OTID otId)
     {
     	OTObject otObject = null;
     	Reference otObjectRef = (Reference)loadedObjects.get(otId);
     	if(otObjectRef != null) {
     		otObject = (OTObject)otObjectRef.get();
     	}
 
     	if(otObject != null) {
     		return otObject;
     	}
 
     	loadedObjects.remove(otId);
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
 
 	public OTObjectServiceImpl getRootObjectService() {
 		return rootObjectService;
 	}	
 	
 	public static OTClass getOTClass(String className)
 	{
 		return (OTClass) otClassMap.get(className);
 	}
 	
 	public static void putOTClass(String className, OTClass otClass)
 	{
 		otClassMap.put(className, otClass);
 	}
 }
