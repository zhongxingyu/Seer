 package org.concord.otrunk.overlay;
 
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Set;
 import java.util.Map.Entry;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.concord.framework.otrunk.OTID;
 import org.concord.framework.otrunk.OTObject;
 import org.concord.framework.otrunk.OTObjectService;
 import org.concord.otrunk.OTObjectServiceImpl;
 import org.concord.otrunk.OTrunkImpl;
 import org.concord.otrunk.datamodel.OTDatabase;
 import org.concord.otrunk.user.OTUserObject;
 import org.concord.otrunk.util.StandardPasswordAuthenticator;
 import org.concord.otrunk.view.OTViewer;
 import org.concord.otrunk.xml.XMLDataObject;
 import org.concord.otrunk.xml.XMLDatabase;
 
 public class OTUserOverlayManager
 {
 	Logger logger = Logger.getLogger(this.getClass().getName());
 	HashMap<OTOverlay, OTObjectService> overlayToObjectServiceMap =
 		new HashMap<OTOverlay, OTObjectService>();
 	HashMap<OTUserObject, OTOverlay> userToOverlayMap = new HashMap<OTUserObject, OTOverlay>();
 	ArrayList<OTDatabase> overlayDatabases = new ArrayList<OTDatabase>();
 	OTrunkImpl otrunk;
 	ArrayList<OverlayImpl> globalOverlays = new ArrayList<OverlayImpl>();
 	private final StandardPasswordAuthenticator authenticator = new StandardPasswordAuthenticator();
 
 	public OTUserOverlayManager(OTrunkImpl otrunk) {
 		this.otrunk = otrunk;
 	}
 
 	/**
 	 * Add an overlay to the UserOverlayManager. This can be used when you have a URL to an otml snippet which contains an OTOverlay object
 	 * and you don't want to fetch the object yourself.
 	 * @param overlayURL
 	 * @param contextObject
 	 * @param userObject
 	 * @param isGlobal
 	 * @throws Exception
 	 */
 	public void add(URL overlayURL, OTUserObject userObject, boolean isGlobal) throws Exception {
 		// get the OTOverlay OTObject from the otml at the URL specified
 		OTOverlay overlay = null;
 		try {
 			overlay = (OTOverlay) otrunk.getExternalObject(overlayURL, otrunk.getRootObjectService(), true);
 		} catch (Exception e) {
 			// some error occurred...
 			logger.warning("Couldn't get overlay for user\n" + overlayURL + "\n" + e.getMessage());
 		}
 
 		// if there isn't an overlay object, and it's not supposed to be a global one, go ahead and try to make a default one
 		if (overlay == null && isGlobal == false) {
 			// create a blank one
 			try {
 				logger.info("Creating empty overlay database on the fly...");
     			XMLDatabase xmldb = new XMLDatabase();
     			overlay = otrunk.getRootObjectService().createObject(OTOverlay.class);
 
     			// FIXME this approach bypasses the normal way of adding a data object to the
     			// database.  It seems like it should be changed to create an object service for
     			// the newly created database and then make the overlay in that object service.
     			// that should get around the code below
     			xmldb.getDataObjects().put(overlay.getGlobalId(),
     				(XMLDataObject) otrunk.getDataObjectFinder().findDataObject(overlay.getGlobalId()));
     			xmldb.setRoot(overlay.getGlobalId());
     			otrunk.remoteSaveData(xmldb, overlayURL, OTViewer.HTTP_PUT, new StandardPasswordAuthenticator());
 
     			overlay = (OTOverlay) otrunk.getExternalObject(overlayURL, otrunk.getRootObjectService());
 			} catch (Exception e) {
 				// still an error. skip the overlay for this user/url
 				logger.warning("Couldn't create a default overlay for user\n" + overlayURL + "\n" + e.getMessage());
 			}
 		}
 
 		// if the overlay exists, the create an objectservice for it and register it
 		if (overlay != null) {
 			OTObjectService objService = createObjectService(overlay, isGlobal);
 
 			// map the object service/overlay to the user
 			add(overlay, objService, userObject);
 		}
 	}
 
 	/**
 	 * Creates an OTObjectService object for an OTOverlay
 	 * @param overlay
 	 * @param isGlobal
 	 * @return
 	 */
 	private OTObjectService createObjectService(OTOverlay overlay, boolean isGlobal) {
 		// initialize an OverlayImpl with the OTOverlay
 		OverlayImpl myOverlay = new OverlayImpl(overlay);
 		if(isGlobal){
 			globalOverlays.add(myOverlay);
 		}
 		// set up the CompositeDatabase
 		CompositeDatabase db = new CompositeDatabase(otrunk.getDataObjectFinder(), myOverlay);
 
 		// if it's not a global overlay, add all the global overlays to its stack of overlays
 		if(!isGlobal){
 			ArrayList<Overlay> overlays = new ArrayList<Overlay>();
 			if (globalOverlays.size() > 0) {
 				overlays.addAll(globalOverlays);
 			}
 			db.setOverlays(overlays);
 		}
 		// create the OTObjectService and return it
 	  	OTObjectService objService = otrunk.createObjectService(db);
 	  	return objService;
 	}
 
 	public void add(OTOverlay otOverlay, OTObjectService objService, OTUserObject userObject) {
 		userObject = getAuthoredObject(userObject);
 		overlayToObjectServiceMap.put(otOverlay, objService);
 		userToOverlayMap.put(userObject, otOverlay);
 
 		if (objService instanceof OTObjectServiceImpl) {
 			overlayDatabases.add(getDatabase(otOverlay));
 		}
 	}
 
 	public OTObjectService getObjectService(OTOverlay overlay) {
 		return overlayToObjectServiceMap.get(overlay);
 	}
 
 	public OTObjectService getObjectService(OTUserObject userObj) {
 		userObj = getAuthoredObject(userObj);
 		return overlayToObjectServiceMap.get(userToOverlayMap.get(userObj));
 	}
 
 	public OTOverlay getOverlay(OTUserObject userObj) {
 		userObj = getAuthoredObject(userObj);
 		return userToOverlayMap.get(userObj);
 	}
 
 	public OTOverlay getOverlay(OTObjectService objService) {
 		for (Entry<OTOverlay,OTObjectService> entry : overlayToObjectServiceMap.entrySet()) {
 			if(entry.getValue() == objService){
 				return entry.getKey();
 			}
         }
 		return null;
 	}
 
 	public OTUserObject getUserObject(OTOverlay overlay) {
 		for (Entry<OTUserObject, OTOverlay> entry : userToOverlayMap.entrySet()) {
 			if(entry.getValue() == overlay){
 				return entry.getKey();
 			}
         }
 		return null;
 	}
 
 	public OTUserObject getUserObject(OTObjectService objService) {
 		OTOverlay overlay = getOverlay(objService);
 		return getUserObject(overlay);
 	}
 
 	public ArrayList<OTDatabase> getOverlayDatabases() {
 		return this.overlayDatabases;
 	}
 
 	public Set<OTOverlay> getOverlays() {
 		return this.overlayToObjectServiceMap.keySet();
 	}
 	
 	public OTObject getOTObject(OTUserObject userObject, OTObject object) throws Exception {
 		userObject = getAuthoredObject(userObject);
 		object = getAuthoredObject(object);
 		return getOTObject(userObject, object.getGlobalId());
 	}
 
 	public OTObject getOTObject(OTUserObject userObject, OTID id) throws Exception {
 		userObject = getAuthoredObject(userObject);
 		return getOTObject(getOverlay(userObject), id);
 	}
 
 	public OTObject getOTObject(OTOverlay overlay, OTID id) throws Exception {
 		OTObjectService objService = getObjectService(overlay);
 		if (objService == null) {
 			return null;
 		}
 		return objService.getOTObject(id);
 	}
 
 	public OTDatabase getDatabase(OTOverlay overlay) {
 		OTObjectServiceImpl objService = (OTObjectServiceImpl) getObjectService(overlay);
 		if (objService != null) {
 			return objService.getCreationDb();
 		}
 		return null;
 	}
 
 	public XMLDatabase getXMLDatabase(OTOverlay overlay) {
     	OTDatabase db = getDatabase(overlay);
     	if (db instanceof XMLDatabase) {
     		return (XMLDatabase) db;
     	} else if (db instanceof CompositeDatabase) {
     		return (XMLDatabase) ((CompositeDatabase) db).getActiveOverlayDb();
     	}
     	return null;
     }
 
 	public void remove(OTOverlay overlay) {
 		OTUserObject userObject = getUserObject(overlay);
 		OTObjectService objService = getObjectService(overlay);
 
 		remove(userObject, overlay, objService);
     }
 
 	public void remove(OTUserObject userObject) {
 		userObject = getAuthoredObject(userObject);
 		OTOverlay otOverlay = getOverlay(userObject);
 		OTObjectService objService = getObjectService(otOverlay);
 
 		remove(userObject, otOverlay, objService);
     }
 
 	private void remove(OTUserObject userObject, OTOverlay otOverlay, OTObjectService objService) {
 		otrunk.removeObjectService((OTObjectServiceImpl) objService);
 		overlayToObjectServiceMap.remove(otOverlay);
 		userToOverlayMap.remove(userObject);
 
 		if (objService instanceof OTObjectServiceImpl) {
 			overlayDatabases.add(getDatabase(otOverlay));
 		}
 	}
 	
 	public void reload(OTUserObject userObject) throws Exception {
 		userObject = getAuthoredObject(userObject);
 		// check the last modified of the URL and the existing db, if they're different, remove and add the db again
 		XMLDatabase xmlDb = getXMLDatabase(getOverlay(userObject));
 		long existingTime = xmlDb.getUrlLastModifiedTime();
 		
 		URLConnection conn = xmlDb.getSourceURL().openConnection();
 		if (conn instanceof HttpURLConnection) {
 			((HttpURLConnection) conn).setRequestMethod(OTViewer.HTTP_HEAD);
 		}
 		
 		long serverTime = conn.getLastModified();
 		
 		if (existingTime != 0 && serverTime != 0 && existingTime == serverTime) {
 			// no reload needed
 			logger.info("Not reloading overlay as modified time is the same as the currently loaded version");
 		} else {
 			logger.info("Modified times indicated reload needed. current: " + existingTime + ", server: " + serverTime);
 			remove(userObject);
 			add(xmlDb.getSourceURL(), userObject, false);
 		}
 	}
 
 	public void remoteSave(OTOverlay overlay) throws Exception {
 		if (otrunk.isSailSavingDisabled()) {
 			logger.info("Not saving overlay because SAIL saving is disabled");
 		} else {
 			otrunk.remoteSaveData(getXMLDatabase(overlay), OTViewer.HTTP_PUT, authenticator);
 		}
 	}
 	
 	public void remoteSave(OTUserObject user) throws Exception {
 		user = getAuthoredObject(user);
 		remoteSave(getOverlay(user));
 	}
 	
 	private <T extends OTObject> T getAuthoredObject(T object) {
		if (object == null) {
			return null;
		}
 		try {
 			object = otrunk.getRuntimeAuthoredObject(object);
 		} catch (Exception e) {
 			logger.log(Level.WARNING, "Couldn't get authored version of user object!", e);
 		}
 		return object;
 	}
 }
