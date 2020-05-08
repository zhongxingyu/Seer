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
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.lang.ref.Reference;
 import java.lang.ref.WeakReference;
 import java.net.Authenticator;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Vector;
 import java.util.Map.Entry;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.net.ssl.HttpsURLConnection;
 
 import org.concord.framework.otrunk.OTBundle;
 import org.concord.framework.otrunk.OTControllerRegistry;
 import org.concord.framework.otrunk.OTID;
 import org.concord.framework.otrunk.OTObject;
 import org.concord.framework.otrunk.OTObjectList;
 import org.concord.framework.otrunk.OTObjectService;
 import org.concord.framework.otrunk.OTPackage;
 import org.concord.framework.otrunk.OTServiceContext;
 import org.concord.framework.otrunk.OTUser;
 import org.concord.framework.otrunk.OTrunk;
 import org.concord.framework.otrunk.otcore.OTClass;
 import org.concord.otrunk.datamodel.OTDataObject;
 import org.concord.otrunk.datamodel.OTDataObjectFinder;
 import org.concord.otrunk.datamodel.OTDataObjectType;
 import org.concord.otrunk.datamodel.OTDataPropertyReference;
 import org.concord.otrunk.datamodel.OTDatabase;
 import org.concord.otrunk.datamodel.OTIDFactory;
 import org.concord.otrunk.datamodel.OTTransientMapID;
 import org.concord.otrunk.net.HTTPRequestException;
 import org.concord.otrunk.overlay.CompositeDataObject;
 import org.concord.otrunk.overlay.CompositeDatabase;
 import org.concord.otrunk.overlay.OTOverlay;
 import org.concord.otrunk.overlay.OTOverlayGroup;
 import org.concord.otrunk.overlay.Overlay;
 import org.concord.otrunk.overlay.OverlayImpl;
 import org.concord.otrunk.user.OTReferenceMap;
 import org.concord.otrunk.user.OTUserObject;
 import org.concord.otrunk.util.ConcordHostnameVerifier;
 import org.concord.otrunk.view.OTConfig;
 import org.concord.otrunk.view.OTUserSession;
 import org.concord.otrunk.view.OTViewer;
 import org.concord.otrunk.xml.ExporterJDOM;
 import org.concord.otrunk.xml.XMLDatabase;
 
 /**
  * @author scott
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Generation - Code and Comments
  */
 public class OTrunkImpl implements OTrunk
 {
 	private static final Logger logger = Logger.getLogger(OTrunkImpl.class.getName());
 	protected Hashtable<OTID, Reference<OTObject>> loadedObjects = 
 		new Hashtable<OTID, Reference<OTObject>>();
 	protected Hashtable<OTID, CompositeDatabase> compositeDatabases = 
 		new Hashtable<OTID, CompositeDatabase>();
     protected Hashtable<OTID, OTObjectService> userObjectServices = 
     	new Hashtable<OTID, OTObjectService>();
 
 	OTServiceContext serviceContext = new OTServiceContextImpl();	
 
 	protected OTDatabase rootDb;
 	protected OTDatabase systemDb;
 	
     protected OTObjectServiceImpl rootObjectService;
     
 	ArrayList<OTDatabase> databases = new ArrayList<OTDatabase>();
 	Vector<OTUser> users = new Vector<OTUser>();
 	
     ArrayList<OTObjectServiceImpl> objectServices = new ArrayList<OTObjectServiceImpl>();
 
 	private ArrayList<Class<? extends OTPackage>> registeredPackageClasses = 
 		new ArrayList<Class<? extends OTPackage>>();
 	private OTObjectServiceImpl systemObjectService;
 
 	private static HashMap<String, OTClass> otClassMap = new HashMap<String, OTClass>(); 
 	
 	private boolean sailSavingDisabled = false;
 	
 	OTDataObjectFinder dataObjectFinder = new OTDataObjectFinder()
 	{
 		public OTDataObject findDataObject(OTID id)
 			throws Exception
         {
 			OTDatabase db = getOTDatabase(id);
 			if(db == null){
 				return null;
 			}
 			return db.getOTDataObject(null, id);
         }    		
 	};
 
     public final static String getClassName(OTDataObject dataObject)
     {
     	OTDataObjectType type = dataObject.getType();
     	return type.getClassName();
     }
     
     public OTrunkImpl(OTDatabase db)
 	{
 		this((OTDatabase)null, db, (ArrayList<OTrunkServiceEntry<?>>)null);
 	}
 
 	public OTrunkImpl(OTDatabase db, ArrayList<OTrunkServiceEntry<?>> services)
 	{	
 		this(null, db, services);
 	}
 	
 	@SuppressWarnings("unchecked")
     public OTrunkImpl(OTDatabase systemDb, OTDatabase db, ArrayList<OTrunkServiceEntry<?>> services) 
 	{	
 		try {
 			ConcordHostnameVerifier verifier = new ConcordHostnameVerifier();
 			HttpsURLConnection.setDefaultHostnameVerifier(verifier);
 		} catch (Exception e) {
 			logger.warning("Couldn't initialize the Concord HostnameVerifier!");
 		}
 		
 		try {
 	        URL dummyURL = new URL("http://www.concord.org");
 	        URLConnection openConnection = dummyURL.openConnection();
 	        openConnection.setDefaultUseCaches(true);
         } catch (MalformedURLException e1) {
 	        // TODO Auto-generated catch block
 	        logger.log(Level.WARNING, "Malformed URL", e1);
         } catch (IOException e) {
 	        // TODO Auto-generated catch block
         	logger.log(Level.WARNING, "IO problem", e);
         }
 		
 		// Setup the services this has to be done before addDatabase
 		// because addDatabase initializes the OTPackages loaded by that
 		// database
         
         // Add ourself as a service, this is needed so 
         serviceContext.addService(OTrunk.class, this);
         
 		if(services != null) {
 			for(int i=0; i<services.size(); i++){
 				// there might be a better way to make this type safe.  
 				// it seems like the compiler should be able to figure out it is safe
 				// but instead the suppress warnings is added to remove the warning here
 				OTrunkServiceEntry entry = services.get(i);
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
 				logger.warning("No OTSystem object found");
 				return;
 			}
 			
 			// This is deprecated but we use it anyhow for backward compatibility
 			OTObjectList serviceList = otSystem.getServices(); 
 			
 			OTObjectList bundleList = otSystem.getBundles();
 			
 			if(serviceList.size() > 0 && bundleList.size() > 0){
 				logger.warning("Both OTSystem.services and OTSystem.bundles are being used.  OTSystem.services is deprecated");
 			}
 			
 			ArrayList<OTObject> combined = new ArrayList<OTObject>();
 			combined.addAll(serviceList.getVector());
 			combined.addAll(bundleList.getVector());
 			
 			for(int i=0; i<combined.size(); i++){
 				OTBundle bundle = (OTBundle)combined.get(i);
 				if(bundle != null){
 					bundle.registerServices(serviceContext);
 				}
 			}
 			
 			for(int i=0; i<combined.size(); i++){
 				OTBundle bundle = (OTBundle)combined.get(i);
 				if(bundle != null){
 					bundle.initializeBundle(serviceContext);
 				}
 			}			
 		} catch (Exception e) {
 			logger.log(Level.WARNING, "Error registering and initializing bundles and services", e);
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
 	public <T extends OTObject> T createObject(Class<T> objectClass)
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
 	    
 	    for (OTDatabase db : databases) {	     	   
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
 	
 	public <T> T getService(Class<T> serviceInterface)
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
     
     public OTObjectServiceImpl createObjectService(OTOverlay overlay) {
     	// create an object service for the overlay
 		OverlayImpl myOverlay = new OverlayImpl(overlay);
 		CompositeDatabase db = new CompositeDatabase(this.getDataObjectFinder(), myOverlay);
 	  	OTObjectServiceImpl objService = this.createObjectService(db);
 	  	return objService;
     }
 
     public void registerObjectService(OTObjectServiceImpl objService, String label)
     {
         objectServices.add(objService);
 
         if(OTConfig.isTrace()){
         	objService.addObjectServiceListener(new TraceListener(label));
         }    	
     }
     
     public void removeObjectService(OTObjectServiceImpl objService) {
     	objectServices.remove(objService);
     }
     
     public void registerUserSession(OTUserSession userSession) throws Exception
     {
     	userSession.setOTrunk(this);
     	userSession.load();
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
     public OTObject getExternalObject(URL url, OTObjectService parentObjectService) throws Exception
 	{
     	return getExternalObject(url, parentObjectService, false);
 	}
     
     /**
      * the parentObjectService needs to be passed in so the returned object
      * uses the correct layers based on the context in which this method is
      * called.
      * 
      * @param url
      * @param parentObjectService
      * @param reload
      * @return
      * @throws Exception
      */
     public OTObject getExternalObject(URL url, OTObjectService parentObjectService, boolean reload) 
     	throws Exception
     {
     	OTObjectServiceImpl externalObjectService = 
     		loadDatabase(url, reload);
 		
     	// get the root object either the real root or the system root
     	OTObject root = getRoot(externalObjectService);
     	
     	return parentObjectService.getOTObject(root.getGlobalId(), reload);
     }
 
     protected OTObjectServiceImpl getExistingObjectService(XMLDatabase includeDb)
     {
 		// we've already loaded this database.
 		// It is nice to print an error here because the equals method of the databases
 		// just use the database id.  So if 2 databases have the same id then it will
 		// seem like the database has already been loaded.
 		// FIXME this should check the url of the database, and only print this message
 		// if the urls are different.
     	for (OTObjectServiceImpl objectService : objectServices) {
 			OTDatabase db = objectService.getMainDb();
 			if(db.equals(includeDb)){
 				return objectService;
 			}
 		}
 		
 		logger.warning("Cannot find objectService for database: " + includeDb.getDatabaseId());
 		return null;			    	
     }
     
     protected OTObjectServiceImpl loadDatabase(URL url) throws Exception {
     	return loadDatabase(url, false);
     }
     
     protected OTObjectServiceImpl loadDatabase(URL url, boolean reload) 
     	throws Exception
     {    	
     	// first see if we have a database with the same context url
     	for (OTDatabase db : databases) {
     		if(!(db instanceof XMLDatabase)) continue;
     		
     		XMLDatabase xmlDatabase = (XMLDatabase) db;
     		URL contextURL = xmlDatabase.getContextURL();
     		if (contextURL == null) {
     			logger.info("Database without a context url! " + xmlDatabase.getDatabaseId());
     		}
     		else if (contextURL.equals(url)){
     			if (reload) {
     				// remove the current database, so we can reload it below
     				logger.info("Removing database so we can reload it.");
     				databases.remove(xmlDatabase);
     				removeObjectService(getExistingObjectService(xmlDatabase));
     				break;
     			} else {
     				return getExistingObjectService(xmlDatabase);
     			}
     		}
     	}
     	
 		XMLDatabase includeDb = new XMLDatabase(url);
 
 		// load the data base		
 		if(databases.contains(includeDb)){
 			logger.info("already loaded database with id: " + includeDb.getDatabaseId() + 
 				" database: " + includeDb.getContextURL().toExternalForm() + " will not be loaded again");
 		
 			return getExistingObjectService(includeDb);
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
 
     public OTObjectServiceImpl initObjectService(OTDatabase db, String logLabel) throws Exception 
     {
     	return initObjectService(db, logLabel, true);
     }
     
     public OTObjectServiceImpl initObjectService(OTDatabase db, String logLabel, boolean loadIncludes) 
     	throws Exception
     {
 		addDatabase(db);
 		OTObjectServiceImpl objectService = createObjectService(db);
         if(OTConfig.isTrace()) {
         	objectService.addObjectServiceListener(new TraceListener(logLabel + ": " + db));
         }
         
         if(loadIncludes){
         	loadIncludes(objectService);
         }
         
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
 			
 			try {
 				loadDatabase(hrefUrl);
 			} catch (Exception e) {
 				logger.log(Level.WARNING, "Error while loading database. Trying to continue.", e);
 				continue;
 			}
 		}
     }
     
     public void reloadOverlays(OTUserSession userSession) 
     	throws Exception
     {
     	OTUserObject userObject = userSession.getUserObject();
     	OTID userId = userObject.getUserId();
     	
     	OTReferenceMap refMap = userSession.getReferenceMap();
     		
     	// need to make a new composite database.
     	// the user database should remain the same.
         OTDatabase oldCompositeDB = compositeDatabases.remove(userId);	
         userObjectServices.remove(userId);
         
         databases.remove(oldCompositeDB);
 
         registerReferenceMap(refMap);
     	
     }
     
     public OTObjectService registerReferenceMap(OTReferenceMap userStateMap)
     throws Exception
     {
     	OTObjectServiceImpl userObjService;
     	OTUser user = userStateMap.getUser();
 
     	if(!users.contains(user)){
     		users.add(user);
     	}
     	
     	OTID userId = user.getUserId();
     	    	
         CompositeDatabase userDb = new CompositeDatabase(dataObjectFinder, userStateMap);
 
         addDatabase(userDb);
         userObjService = createObjectService(userDb);
         compositeDatabases.put(userId, userDb);	
         userObjectServices.put(userId, userObjService);
 
         // After the user database is completely setup, now we get the overlays.  
         // This way the user can change the overlays list and those changes will
         // affect the overlay list when they are loaded.
         
         // The overlay list is added to one overlay at a time.  This allows
         // overlays to also modify the list or its values.
         // OTOverlayGroup objects are useful so an overlay or user can insert
         // an overlay into the list without modifying the whole overlay list.
     	try {
         	ArrayList<Overlay> overlays = null;
 	        OTObjectList otOverlays = getSystemOverlays(user);
 	        if(otOverlays != null && otOverlays.size() > 0){
 	        	overlays = new ArrayList<Overlay>();
 		        userDb.setOverlays(overlays);
 	        	for(int i=0; i<otOverlays.size(); i++){
 	        		OTOverlay otOverlay;
 	        		OTObject otOverlayObj = otOverlays.get(i);
 	        		if (otOverlayObj instanceof OTIncludeRootObject) {
 	        			otOverlayObj = ((OTIncludeRootObject)otOverlayObj).getReference();
 	        		}
 
 	        		if(otOverlayObj instanceof OTOverlayGroup){
 	        			OTObjectList members = ((OTOverlayGroup)otOverlayObj).getOverlays();
 	        			for(int j=0; j<members.size(); j++){
 			        		otOverlay = (OTOverlay) members.get(j);
 			        		Overlay overlay = new OverlayImpl(otOverlay);
 			        		overlays.add(overlay);	        				        				
 	        			}
 	        		} else {
 		        		otOverlay = (OTOverlay) otOverlayObj;
 		        		Overlay overlay = new OverlayImpl(otOverlay);
 		        		overlays.add(overlay);	        			
 	        		}
 	        	}
 	        }
         } catch (Exception e) {
 	        // TODO Auto-generated catch block
 	        logger.log(Level.WARNING, "Error register overlays", e);
         }    	
     	
 
         return userObjService;
     }
     
     public Hashtable<OTID, CompositeDatabase> getCompositeDatabases() {
     	return compositeDatabases;
     }
     
     public Vector<OTUser> getUsers() {
     	return users;
     }
     
     public boolean hasUserModified(OTObject authoredObject, OTUser user) throws Exception
     {
     	OTObject userObject = getUserRuntimeObject(authoredObject, user);
     	return isModifiedInTopOverlay(userObject);
     }
 
     /**
      * Check to see if the object is modified in the top overlay. Note that this DOES NOT return true if only a child object is modified.
      * @param otObject
      * @return
      */
     public boolean isModifiedInTopOverlay(OTObject otObject)
     {
     	OTDataObject dataObject = OTObjectServiceImpl.getOTDataObject(otObject);
     	
         if(dataObject instanceof CompositeDataObject) {
             OTDataObject overlayModifications = ((CompositeDataObject)dataObject).getActiveDeltaObject();
             return overlayModifications != null;
         } else {
         	logger.finest("This object isn't from an Overlay");
         }
     	
     	return false;
     }
     
     public boolean isComposite(OTObject otObject)
     {
     	OTDataObject dataObject = OTObjectServiceImpl.getOTDataObject(otObject);
     	
         if(dataObject instanceof CompositeDataObject) {
         	return ((CompositeDataObject)dataObject).isComposite();
         } else {
         	return false;
         }    	
     }
     
     public boolean isModified(OTObject otObject, OTObjectService objService, boolean includingChildren) {
     	boolean isBaseModified = isModifiedInTopOverlay(otObject);
     	if (isBaseModified) {
     		return true;
     	}
     	if (includingChildren) {
     		OTID objId = otObject.getGlobalId();
     		if (objId instanceof OTTransientMapID) {
     			objId = ((OTTransientMapID) objId).getMappedId();
     		}
     		
     		ArrayList<ArrayList<OTDataPropertyReference>> references = getOutgoingReferences(objId, true);
     		for (ArrayList<OTDataPropertyReference> path : references) {
     			OTID id = path.get(path.size()-1).getDest();
     			try {
 	                OTObject obj = objService.getOTObject(id);
 	                if (obj == null) { continue; }
 	                if (isModifiedInTopOverlay(obj)) {
 	                	return true;
 	                }
                 } catch (Exception e) {
 	                // TODO Auto-generated catch block
 	                e.printStackTrace();
                 }
     		}
     	}
     	return false;
     }
         
     public void addDatabase(OTDatabase db)
     {
         if(!databases.contains(db)) {
             databases.add(db);
             
             ArrayList<Class<? extends OTPackage>> packageClasses = 
             	db.getPackageClasses();
             if(packageClasses != null){
             	for(int i=0; i<packageClasses.size(); i++){
             		registerPackageClass(packageClasses.get(i));
             	}
             }
         }
     }
     
 	/**
      * @param class1
      */
     public void registerPackageClass(Class<? extends OTPackage> packageClass)
     {
     	// check to see if this package has already been registered
     	if(registeredPackageClasses.contains(packageClass)){
     		return;
     	}
     	
     	registeredPackageClasses.add(packageClass);
     	OTPackage otPackage;
         try {
 	        otPackage = packageClass.newInstance();
 	        Class<? extends OTPackage> [] dependencies = otPackage.getPackageDependencies();
 	        if(dependencies != null){
 	        	for (Class<? extends OTPackage> dependency : dependencies) {
 	        		registerPackageClass(dependency);
 	        	}
 	        }
 	        
 	    	otPackage.initialize(this);
         } catch (InstantiationException e) {
 	        // TODO Auto-generated catch block
         	logger.log(Level.WARNING, "Error registering package dependencies", e);
         } catch (IllegalAccessException e) {
 	        // TODO Auto-generated catch block
         	logger.log(Level.WARNING, "Error registering package dependencies", e);
         }
 
     	return;
     }
 
 	public OTObject getUserRuntimeObject(OTObject authoredObject, OTUser user)
 		throws Exception
 	{
 		authoredObject = getRuntimeAuthoredObject(authoredObject);
 		
 		OTID authoredId = authoredObject.getGlobalId();
 		OTID userId = user.getUserId();
         OTObjectService objService = userObjectServices.get(userId);
 
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
 				
 		CompositeDatabase db = compositeDatabases.get(userId);
 				
 		//logger.finer("is relative");
 		Object objectMapToken = ((OTTransientMapID) objectId).getMapToken();
 		if(objectMapToken != null && objectMapToken == db.getDatabaseId()) {
 			//logger.finer("   equals to databaseid");
 			objectId = ((OTTransientMapID) objectId).getMappedId();
 			//logger.finer(": " + objectId.toString());
 
 			return getOTObject(objectId);		    			
 		}
 		
 		
 	    return userObject;
 	}
 	/**
 	 * Get the base "authored" object for a particular object, using the root object service to resolve the object
 	 * @param otObject
 	 * @return
 	 * @throws Exception
 	 */
 	public <T extends OTObject> T getRuntimeAuthoredObject(T otObject) throws Exception {
 		return getRuntimeAuthoredObject(otObject, getRootObjectService());
 	}
 	
 	/**
 	 * Get the base "authored" object for a particular object, using a particular object service to resolve the object
 	 * @param otObject
 	 * @param objService
 	 * @return
 	 * @throws Exception
 	 */
 	public <T extends OTObject> T getRuntimeAuthoredObject(T otObject, OTObjectService objService) throws Exception {
 		OTID id = otObject.getGlobalId();
 		if (id instanceof OTTransientMapID) {
 			OTID realID = ((OTTransientMapID) id).getMappedId();
 			return (T) objService.getOTObject(realID);
 		}
 		return otObject;
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
     	WeakReference<OTObject> objRef = new WeakReference<OTObject>(otObject);
     	loadedObjects.put(otId, objRef);    		
     }
     
     OTObject getLoadedObject(OTID otId, boolean reload)
     {
     	if (reload) {
     		loadedObjects.remove(otId);
     		return null;
     	}
     	
     	OTObject otObject = null;
     	Reference<OTObject> otObjectRef = loadedObjects.get(otId);
     	if(otObjectRef != null) {
     		otObject = otObjectRef.get();
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
     	return getOrphanOTObject(childID, oldService, false);
     }
     
     OTObject getOrphanOTObject(OTID childID, OTObjectServiceImpl oldService, boolean reload)
         throws Exception
     {
         for(int i=0; i<objectServices.size(); i++) {
             OTObjectServiceImpl objService = objectServices.get(i);
             // To avoid infinite loop, the objService must not equal to oldService
             if(objService.managesObject(childID) && objService != oldService) {
             	return objService.getOTObject(childID, reload);
             }
         }
         
         logger.warning("Data object is not found for: " + childID);
         return null;
     }
 
 	public OTObjectServiceImpl getRootObjectService() {
 		return rootObjectService;
 	}	
 	
 	public static OTClass getOTClass(String className)
 	{
 		return otClassMap.get(className);
 	}
 	
 	public static void putOTClass(String className, OTClass otClass)
 	{
 		otClassMap.put(className, otClass);
 	}
 
 	public OTDataObjectFinder getDataObjectFinder()
     {
     	return dataObjectFinder;
     }
 	
 	public void localSaveData(XMLDatabase xmldb) throws Exception {
 		if (xmldb.getSourceURL() == null) {
 			throw new MalformedURLException("Invalid source URL on XMLDatabase: " + xmldb.getDatabaseId().toString());
 		}
 		localSaveData(xmldb, xmldb.getSourceURL());
 	}
 	
 	public void localSaveData(OTDatabase db, File file) throws Exception {
 		if (db instanceof XMLDatabase) {
 			XMLDatabase xmldb = (XMLDatabase) db;
 			if (! xmldb.getSourceURL().equals(file.toURL())) {
 				xmldb.setSourceURL(file.toURL());
 			}
 		}
 		DataOutputStream urlDataOut = new DataOutputStream(new FileOutputStream(file));
     	ExporterJDOM.export(urlDataOut, db.getRoot(), db);
     	urlDataOut.flush();
     	urlDataOut.close();
     	if (db instanceof XMLDatabase) {
     		((XMLDatabase)db).setSourceVerified(true);
     	}
 	}
 	
 	public void localSaveData(OTDatabase db, URL url) throws Exception {
 		File f = new File(url.getFile());
 		localSaveData(db, f);
 	}
 	
 	public void remoteSaveData(XMLDatabase xmldb, String method) throws HTTPRequestException,Exception {
 		remoteSaveData(xmldb, method, null);
 	}
 	
 	public void remoteSaveData(XMLDatabase xmldb, String method, Authenticator auth) throws HTTPRequestException,Exception {
 		if (xmldb.getSourceURL() == null) {
 			throw new MalformedURLException("Invalid source URL on XMLDatabase: " + xmldb.getDatabaseId().toString());
 		}
 		remoteSaveData(xmldb, xmldb.getSourceURL(), method, auth);
 	}
 	
 	public void remoteSaveData(OTDatabase db, URL remoteURL, String method) throws HTTPRequestException,Exception {
 		remoteSaveData(db, remoteURL, method, null);
 	}
 	
 	public void remoteSaveData(OTDatabase db, URL remoteURL, String method, Authenticator auth)
     throws HTTPRequestException,Exception
     {
     	HttpURLConnection urlConn;
     	DataOutputStream urlDataOut;
     	BufferedReader urlDataIn;
     
     	// If method isn't "POST" or "PUT", throw an exception
     	if (!(method.compareTo(OTViewer.HTTP_POST) == 0 || method.compareTo(OTViewer.HTTP_PUT) == 0)) {
     		throw new Exception("Invalid HTTP Request method for data saving");
     	}
     	
     	if (db instanceof XMLDatabase) {
     		XMLDatabase xmldb = (XMLDatabase) db;
     		if (xmldb.getSourceURL() != null && ! xmldb.getSourceURL().equals(remoteURL)) {
     			((XMLDatabase)db).setSourceURL(remoteURL);
     		}
     	}
     	
     	if (auth != null) {
     		Authenticator.setDefault(auth);
     	}
     
     	urlConn = (HttpURLConnection) remoteURL.openConnection();
     	urlConn.setDoInput(true);
     	urlConn.setDoOutput(true);
     	urlConn.setUseCaches(false);
     	urlConn.setRequestMethod(method);
     	urlConn.setRequestProperty("Content-Type", "application/xml");
     
     	// Send POST output.
     	urlDataOut = new DataOutputStream(urlConn.getOutputStream());
     	ExporterJDOM.export(urlDataOut, db.getRoot(), db);
     	urlDataOut.flush();
     	urlDataOut.close();
     
     	// Get response data.
     	urlDataIn =
     	    new BufferedReader(new InputStreamReader(new DataInputStream(
     	        urlConn.getInputStream())));
     	String str;
     	String response = "";
     	while (null != ((str = urlDataIn.readLine()))) {
     		response += str + "\n";
     	}
     	urlDataIn.close();
     	// Need to trap non-HTTP 200/300 responses and throw an exception (if an
     	// exception isn't thrown already) and capture the exceptions upstream
     	int code = urlConn.getResponseCode();
     	if (code >= 400) {
     		throw new HTTPRequestException("HTTP Response: " + urlConn.getResponseMessage() + "\n\n"
     		        + response, urlConn.getResponseCode());
     	}
     	urlConn.disconnect();
     	if (db instanceof XMLDatabase) {
     		((XMLDatabase)db).setDirty(false);
     		((XMLDatabase)db).setSourceVerified(true);
     	}
     }
 
 	/**
      * @param sailSavingDisabled true if SAIL is set to not return learner data
      */
     public void setSailSavingDisabled(boolean sailSavingDisabled)
     {
 	    this.sailSavingDisabled = sailSavingDisabled;
 	    System.setProperty("sail.data.saving", Boolean.toString(! sailSavingDisabled));
     }
 
 	/**
      * @return true if SAIL is set to not return learner data
      */
     public boolean isSailSavingDisabled()
     {
 	    return sailSavingDisabled;
     }
     
     public <T extends OTObject> ArrayList<T> getAllObjects(Class<T> klass) {
     	return getAllObjects(klass, getRootObjectService());
     }
     
     public <T extends OTObject> ArrayList<T> getAllObjects(Class<T> klass, OTObjectService objService) {
     	logger.finest("Getting all objects for class: " + klass.getName());
     	ArrayList<T> allObjects = new ArrayList<T>();
     	logger.finest("Datases: " + databases.size());
     	for (OTDatabase db : databases) {
     		logger.finest("Searching db: " + db.getURI());
     		HashMap<OTID, ? extends OTDataObject> map = db.getDataObjects();
     		logger.finest("db has " + map.size() + " objects");
     		for (Entry<OTID, ? extends OTDataObject> entry : map.entrySet()) {
     			OTDataObject dataObj = entry.getValue();
     			OTID id = entry.getKey();
     			logger.finest("Data object class is: " + dataObj.getType().getClassName());
                     try {
 	                    Class<?> objClass = Class.forName(dataObj.getType().getClassName());
             			if (klass.isAssignableFrom(objClass)) {
             				logger.finest("It's a match! Adding it.");
             				allObjects.add((T) objService.getOTObject(id));
             			}
                     } catch (ClassNotFoundException e) {
                     	logger.log(Level.WARNING, "Couldn't instantiate class: " + dataObj.getType().getClassName(), e);
                     } catch (Exception e) {
      					logger.log(Level.WARNING, "Couldn't get OTObject for object: " + id.toExternalForm(), e);
      				}
     		}
     	}
     	return allObjects;
     }
     
     public ArrayList<ArrayList<OTDataPropertyReference>> getIncomingReferences(OTID objectID) {
     	return getIncomingReferences(objectID, null, false, null);
     }
     
     public ArrayList<ArrayList<OTDataPropertyReference>> getIncomingReferences(OTID objectID, boolean getIndirectReferences) {
     	return getIncomingReferences(objectID, null, getIndirectReferences, null);
     }
     
     public ArrayList<ArrayList<OTDataPropertyReference>> getIncomingReferences(OTID objectID, Class<?> filterClass, boolean getIndirectReferences, ArrayList<OTID> excludeIDs) {
     	ArrayList<OTDataPropertyReference> path = new ArrayList<OTDataPropertyReference>();
     	return getReferences(true, objectID, filterClass, getIndirectReferences, path, excludeIDs);
     }
     
     private ArrayList<ArrayList<OTDataPropertyReference>> getReferences(boolean incoming, OTID objectID, Class<?> filterClass, boolean getIndirectReferences, ArrayList<OTDataPropertyReference> currentPath, ArrayList<OTID> excludeIDs) {
     	ArrayList<ArrayList<OTDataPropertyReference>> allParents = new ArrayList<ArrayList<OTDataPropertyReference>>();
     	if (excludeIDs == null) {
     		excludeIDs = new ArrayList<OTID>();
     	}
     	// XXX Should we be searching all databases?
     	for (OTDatabase db : databases) {
         	try {
     	        ArrayList<OTDataPropertyReference> parents = null;
     	        if (incoming) {
     	        	parents = db.getIncomingReferences(objectID);
     	        } else {
     	        	parents = db.getOutgoingReferences(objectID);
     	        }
     	        if (parents != null) {
         	        logger.finest("Found " + parents.size() + " references");
         	        for (OTDataPropertyReference reference : parents) {
         	        	/* FIXME by skipping objects we've seen already, it's possible that we're not including all of the possible paths to an object.
         	        	 * For instance, if A indirectly references D through both B and C, only one of A -> B -> D or A -> C -> D will be returned.
         	        	 * So while this code *will* find all the correct endpoints, it won't necessarily reflect all of the possible paths between those endpoints.
         	        	 */
         	        	OTID pId;
         	        	if (incoming) {
         	        		pId= reference.getSource();
         	        	} else {
         	        		pId = reference.getDest();
         	        	}
         	        	if (! excludeIDs.contains(pId)) {
         	        		logger.finest("Found reference id: " + pId);
         	        		excludeIDs.add(pId);
         	        		
         	        		ArrayList<OTDataPropertyReference> pPath = (ArrayList<OTDataPropertyReference>) currentPath.clone();
         	        		pPath.add(reference);
         	        		
                 	        OTDataObject parentObj = db.getOTDataObject(null, pId);
                	        if (filterClass != null) {
                	        	logger.finest("Filter class: " + filterClass.getSimpleName() + ", parent class: " + parentObj.getType().getClassName());
                 	        }
                	        if (filterClass == null || filterClass.isAssignableFrom(Class.forName(parentObj.getType().getClassName()))) {
                	        	logger.finest("Found a matching parent: " + parentObj.getGlobalId());
                	        	allParents.add(pPath);
                	        }
            	        	if (getIndirectReferences) {
            	        		logger.finest("recursing");
            	        		allParents.addAll(getReferences(incoming, pId, filterClass, true, pPath, excludeIDs));
            	        		logger.finest("unrecursing");
            	        	}
         	        	} else {
         	        		logger.finest("Already seen this id: " + pId);
         	        	}
         	        }
     	        } else {
     	        	logger.finest("null parents");
     	        }
             } catch (Exception e) {
     	        // TODO Auto-generated catch block
             	logger.log(Level.WARNING, "Error finding parents", e);
             }
     	}
     	return allParents;
     }
     
     public ArrayList<ArrayList<OTDataPropertyReference>> getIncomingReferences(OTID objectID, Class<?> filterClass, boolean getIndirectReferences) {
     	logger.finer("Finding references for: " + objectID + " with class: " + (filterClass == null ? "null" : filterClass.getName()) + " and recursion: " + getIndirectReferences);
     	ArrayList<ArrayList<OTDataPropertyReference>> parents = getIncomingReferences(objectID, filterClass, getIndirectReferences, null);
     	logger.finer("found " + parents.size() + " matching parents");
     	return parents;
     }
     
     public ArrayList<ArrayList<OTDataPropertyReference>> getOutgoingReferences(OTID objectID) {
     	return getOutgoingReferences(objectID, null, false, null);
     }
     
     public ArrayList<ArrayList<OTDataPropertyReference>> getOutgoingReferences(OTID objectID, boolean getIndirectReferences) {
     	return getOutgoingReferences(objectID, null, getIndirectReferences, null);
     }
     
     public ArrayList<ArrayList<OTDataPropertyReference>> getOutgoingReferences(OTID objectID, Class<?> filterClass, boolean getIndirectReferences, ArrayList<OTID> excludeIDs) {
     	return getReferences(false, objectID, filterClass, getIndirectReferences, new ArrayList<OTDataPropertyReference>(), excludeIDs);
     }
     
     public ArrayList<ArrayList<OTDataPropertyReference>> getOutgoingReferences(OTID objectID, Class<?> filterClass, boolean getIndirectReferences) {
     	logger.finest("Finding references for: " + objectID + " with class: " + (filterClass == null ? "null" : filterClass.getName()) + " and recursion: " + getIndirectReferences);
     	ArrayList<ArrayList<OTDataPropertyReference>> parents = getOutgoingReferences(objectID, filterClass, getIndirectReferences, null);
     	logger.finest("found " + parents.size() + " matching parents");
     	return parents;
     }
 }
