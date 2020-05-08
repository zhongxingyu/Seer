 /*
  *  Copyright (C) 2008  The Concord Consortium, Inc.,
  *  25 Love Lane, Concord, MA 01742
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
 
 
 package org.concord.otrunk.view;
 
 import java.net.URL;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.concord.framework.otrunk.OTObject;
 import org.concord.framework.otrunk.OTObjectList;
 import org.concord.framework.otrunk.OTObjectService;
 import org.concord.framework.otrunk.OTrunk;
 import org.concord.framework.otrunk.view.AbstractOTView;
 import org.concord.framework.otrunk.view.OTXHTMLView;
 import org.concord.otrunk.OTIncludeRootObject;
 import org.concord.otrunk.OTrunkImpl;
 import org.concord.otrunk.overlay.CompositeDatabase;
 import org.concord.otrunk.overlay.OTOverlay;
 import org.concord.otrunk.overlay.OTUserOverlayManager;
 import org.concord.otrunk.overlay.OverlayImpl;
 import org.concord.otrunk.user.OTUserObject;
 import org.concord.otrunk.xml.XMLDatabase;
 
 
 public class OTMultiUserRootView extends AbstractOTView implements OTXHTMLView 
 {
 	private Logger logger = Logger.getLogger(this.getClass().getName());
 	private boolean firstRun = true;
 	private OTrunkImpl otrunk;
 	OTUserOverlayManager overlayManager;
 	
 	public String getXHTMLText(OTObject otObject) {
 		//System.out.println("ENTER: OTMultiUserRootView#getXHTMLText()");
 	    OTMultiUserRoot root = (OTMultiUserRoot) otObject;
 	    otrunk = (OTrunkImpl) getViewService(OTrunk.class);
 	    
 		if (firstRun) { //why is this method called twice?
 			overlayManager = new OTUserOverlayManager();
 			viewContext.addViewService(OTUserOverlayManager.class, overlayManager);
 			loadUserDatabases(root);
 			loadGlobalOverlay(root);
 			firstRun = false;
 		}
 	    OTObject reportTemplate = root.getReportTemplate();
 	    String result = "<object refid=\"" + reportTemplate.otExternalId() + "\" ";
 	    if(root.getReportTemplateViewEntry() != null){
 	    	result += "viewid=\"" + root.getReportTemplateViewEntry().otExternalId() + "\" ";
 	    }
 	    result += "/>";
 	    return result;
     }
 	
 	protected void loadUserDatabases(OTMultiUserRoot root) {
 		OTUserList userList = null;
 		OTObject userListOrig = root.getUserList();
 		if(userListOrig instanceof OTIncludeRootObject){
 			userList = (OTUserList) ((OTIncludeRootObject)userListOrig).getReference();
 		} else {
 			userList = (OTUserList) userListOrig;
 		}
 		
 	    OTObjectList userDatabases = userList.getUserDatabases();
 	    
 	    for (int i = 0; i < userDatabases.size(); ++i) {
 	    	OTUserDatabaseRef ref = (OTUserDatabaseRef) userDatabases.get(i);
 	    	URL url = ref.getUrl();
 	    	URL overlayURL = ref.getOverlayURL();
 	    	OTUserObject userObject = null;
 	    	try {
 	    		// set up the user session and register it
 	    		OTMLUserSession userSession = new OTMLUserSession(url, null);
 	    		otrunk.registerUserSession(userSession);
 	    		userObject = userSession.getUserObject();
 	    	}
 	    	catch (Exception e) {
 	    		logger.log(Level.SEVERE, "Couldn't initialize user session", e);
 	    	}
 	    	
     		// set up the overlay, if it exists
     		if (overlayURL != null && userObject != null) {
     			try {
     				// get the OTOverlay
     				OTOverlay overlay = (OTOverlay) otrunk.getExternalObject(overlayURL, ref.getOTObjectService());
     				
     				if (overlay == null) {
     					// create a blank one
     					XMLDatabase xmldb = new XMLDatabase();
     					overlay = (OTOverlay) root.getOTObjectService().createObject(OTOverlay.class);
     					xmldb.setRoot(overlay.getGlobalId());
     					otrunk.remoteSaveData(xmldb, overlayURL, OTViewer.HTTP_PUT);
     					overlay = (OTOverlay) otrunk.getExternalObject(overlayURL, ref.getOTObjectService());
     				}
     				
     			  	OTObjectService objService = createObjectService(overlay);
     			  	
     				// map the object service/overlay to the user
     			  	overlayManager.add(overlay, objService, userObject);
     			} catch (Exception e) {
     				logger.log(Level.SEVERE, "Couldn't initialize user overlay", e);
     			}
     		}
 	    }
 	}
 	
 	protected void loadGlobalOverlay(OTMultiUserRoot root) {
 		// get the list of overlays
 		try {
 			OTOverlay overlay = (OTOverlay) otrunk.getExternalObject(root.getGroupOverlayURL(), root.getOTObjectService());
 	        if (overlay != null) {
     	        // set up the objectService for it
     	        OTObjectService objService = createObjectService(overlay);
     	        // associate it with the 'null' userobject in otrunk
     	        overlayManager.add(overlay, objService, null);
 	        }
 		} catch (Exception e) {
 	        logger.log(Level.WARNING, "Couldn't set up the group-wide overlay", e);
         }
 	}
 	
 	protected OTObjectService createObjectService(OTOverlay overlay) {
 		// create an object service for the overlay
 		OverlayImpl myOverlay = new OverlayImpl(overlay);
 		CompositeDatabase db = new CompositeDatabase(otrunk.getDataObjectFinder(), myOverlay);
 	  	OTObjectService objService = otrunk.createObjectService(db);
 	  	return objService;
 	}
 
 	public boolean getEmbedXHTMLView()
     {
 	    return true;
     }
 }
