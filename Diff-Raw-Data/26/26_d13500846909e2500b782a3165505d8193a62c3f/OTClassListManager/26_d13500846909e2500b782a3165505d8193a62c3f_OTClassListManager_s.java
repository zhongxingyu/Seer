 package org.concord.otrunk.view;
 
 import java.net.URL;
 import java.util.Enumeration;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.concord.framework.otrunk.DefaultOTObject;
 import org.concord.framework.otrunk.OTBundle;
 import org.concord.framework.otrunk.OTObjectList;
 import org.concord.framework.otrunk.OTResourceSchema;
 import org.concord.framework.otrunk.OTServiceContext;
 import org.concord.framework.otrunk.OTrunk;
 import org.concord.framework.otrunk.wrapper.OTObjectSet;
 import org.concord.otrunk.OTrunkImpl;
 import org.concord.otrunk.overlay.OTUserOverlayManager;
 import org.concord.otrunk.user.OTUserObject;
 
 public class OTClassListManager extends DefaultOTObject
     implements OTBundle
 {
 	private Logger logger = Logger.getLogger(this.getClass().getName());
 	
 	public static interface ResourceSchema extends OTResourceSchema 
 	{
 		// list of users that are in this class. specify the URL or the list
 		URL getUserListURL();
 		OTObjectList getUserList();
 	}
 	private ResourceSchema resources;
 	
 	// the userListURL should point to an otrunk document with the main object being a OTObjectSet,
 	// which encapsulates an OTObjectList of OTClassMember objects
 	private URL userListURL;
 	
 	// userList is a list of OTClassMember objects
 	private OTObjectList userList;
 	private OTrunkImpl otrunk;
 	
 	private OTClassMember currentClassMember;
 
 	private OTUserOverlayManager overlayManager;
 	
 	public OTClassListManager(ResourceSchema resources)
     {
 	    super(resources);
 	    this.resources = resources;
 	    this.otrunk = (OTrunkImpl) resources.getOTObjectService().getOTrunkService(OTrunk.class);
     }
 
 	public void initializeBundle(OTServiceContext serviceContext)
 	{
 		userList = resources.getUserList();
 		
 		userListURL = resources.getUserListURL();
 		if (userListURL != null) {
 			initializeUserListFromURL();
 		}
 		
 		processUserList();
 	}
 
 	public void registerServices(OTServiceContext serviceContext)
 	{
 		serviceContext.addService(OTClassListManager.class, this);
 		overlayManager = (OTUserOverlayManager) serviceContext.getService(OTUserOverlayManager.class);
 		if (overlayManager == null) {
 			overlayManager = new OTUserOverlayManager(otrunk);
 			serviceContext.addService(OTUserOverlayManager.class, overlayManager);
 			// logger.info("Overlay manager registered as service");
 		}
 	}
 	
 	private void initializeUserListFromURL() {
 		try {
 	        OTObjectSet set = (OTObjectSet) otrunk.getExternalObject(userListURL, resources.getOTObjectService());
 	        
 	        // because OTObjectList.addAll() is unsupported, iterate and add them individually
 	        Enumeration objects = set.getObjects().getVector().elements();
 	        while (objects.hasMoreElements()) {
 	        	userList.add(objects.nextElement());
 	        }
         } catch (Exception e) {
 	        logger.log(Level.SEVERE, "Couldn't initialize class list from URL: " + userListURL, e);
         }
 	}
 
 	/**
      * @return OTObjectList - the userList
      */
     public OTObjectList getUserList()
     {
 	    return userList;
     }
     
     private void processUserList() {
     	// for each user
     	Enumeration classMemberList = userList.getVector().elements();
     	while (classMemberList.hasMoreElements()) {
     		OTClassMember classMember = (OTClassMember) classMemberList.nextElement();
     		
     	// if current user is set, make it the current user
     	if (classMember.getIsCurrentUser()) {
     		this.currentClassMember = classMember;
     	}
     	// if overlay url is set, load and register overlay
     	if (classMember.getOverlayURL() != null) {
     		try {
 	            overlayManager.add(classMember.getOverlayURL(), classMember, classMember.getUserObject(), false);
             } catch (Exception e) {
 	            logger.log(Level.WARNING, "Couldn't load overlay for user: " + classMember.getName(), e);
             }
     	}
     	}
     }
     
     public OTClassMember getCurrentClassMember() {
     	return this.currentClassMember;
     }
     
     public OTUserObject getCurrentClassMemberUserObject() {
     	return this.currentClassMember.getUserObject();
     }
     
     public void reloadAll() {
     	processUserList();
     }
 
 }
