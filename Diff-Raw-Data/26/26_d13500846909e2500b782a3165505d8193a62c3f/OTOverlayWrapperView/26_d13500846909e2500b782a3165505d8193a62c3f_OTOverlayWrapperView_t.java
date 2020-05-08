 package org.concord.otrunk.overlay;
 
import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.swing.JComponent;
 
 import org.concord.framework.otrunk.OTID;
 import org.concord.framework.otrunk.OTObject;
 import org.concord.framework.otrunk.OTrunk;
 import org.concord.otrunk.OTrunkImpl;
 import org.concord.otrunk.datamodel.OTTransientMapID;
 import org.concord.otrunk.user.OTUserObject;
 import org.concord.otrunk.util.StandardPasswordAuthenticator;
 import org.concord.otrunk.view.AbstractOTJComponentContainerView;
 import org.concord.otrunk.view.OTClassListManager;
 import org.concord.otrunk.view.OTViewer;
 
 public class OTOverlayWrapperView extends AbstractOTJComponentContainerView
 {
 	private Logger logger = Logger.getLogger(this.getClass().getName());
 	private OTOverlay overlay;
 	private OTObject wrappedObject;
 	private JComponent subview;
 	private OTOverlayWrapper wrapper;
 	private OTClassListManager classListManager;
 	private OTUserOverlayManager overlayManager;
 	private OTrunkImpl otrunk;
 	
 	public JComponent getComponent(OTObject otObject)
 	{
		// logger.info("Initializing overlay wrapper");
 		// FIXME Changes to the object will ONLY be save in the overlay! We should probably mirror
 		// the changes to the current user's session overlay, too!
 		// logger.info("Initializing");
 		wrapper = (OTOverlayWrapper) otObject;
 		otrunk = (OTrunkImpl) wrapper.getOTObjectService().getOTrunkService(OTrunk.class);
 		overlay = wrapper.getOverlay();
 		classListManager = (OTClassListManager) wrapper.getOTObjectService().getOTrunkService(OTClassListManager.class);
 		overlayManager = (OTUserOverlayManager) wrapper.getOTObjectService().getOTrunkService(OTUserOverlayManager.class);
 		wrappedObject = wrapper.getWrappedObject();
 		
 		/* String msg = "\nwrapped object is: " + wrappedObject;
 		msg += "\noverlay object is: " + overlay;
 		msg += "\nclass list manager is: " + classListManager;
 		msg += "\noverlay manager is: " + overlayManager;
 		logger.info(msg);
 		*/
 		if (overlay == null && classListManager != null && overlayManager != null) {
			// logger.info("Using class list manager/overlay manager");
 			OTUserObject currentClassMember = classListManager.getCurrentClassMemberUserObject();
 			overlay = overlayManager.getOverlay(currentClassMember);
 		}
 
 		// logger.info("overlay object is: " + overlay);
 		
 		if (overlay != null) {
			// logger.info("had an overlay");
     		try {
     			OTID id = wrappedObject.getGlobalId();
     			if (id instanceof OTTransientMapID) {
     				id = ((OTTransientMapID) id).getMappedId();
     			}
     	        wrappedObject = overlayManager.getOTObject(overlay, id);
     	        
             } catch (Exception e) {
    	        logger.log(Level.SEVERE, "Couldn't get the object from the overlay!", e);
             }
 		}
 		
 		// logger.info("wrapped object is: " + wrappedObject);
 		subview = createSubViewComponent(wrappedObject);
 		
 		return subview;
 	}
 	
 	public void viewClosed() {
		// logger.info("Closing the wrapped object's views");
 		removeAllSubViews();
 		// save everything
 		try {
 	        otrunk.remoteSaveData(overlayManager.getXMLDatabase(overlay), OTViewer.HTTP_PUT, new StandardPasswordAuthenticator());
         } catch (Exception e) {
        	logger.log(Level.SEVERE, "Couldn't save the user's overlay!", e);
         }
 		super.viewClosed();
 	}
 
 }
