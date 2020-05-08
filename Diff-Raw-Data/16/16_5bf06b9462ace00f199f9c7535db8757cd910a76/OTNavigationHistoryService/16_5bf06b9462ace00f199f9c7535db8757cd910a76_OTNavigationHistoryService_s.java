 package org.concord.otrunk.navigation;
 
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import org.concord.framework.otrunk.DefaultOTObject;
 import org.concord.framework.otrunk.OTBundle;
 import org.concord.framework.otrunk.OTObject;
 import org.concord.framework.otrunk.OTObjectList;
 import org.concord.framework.otrunk.OTResourceSchema;
 import org.concord.framework.otrunk.OTServiceContext;
 import org.concord.framework.otrunk.OTUser;
 import org.concord.framework.otrunk.OTrunk;
 import org.concord.framework.otrunk.wrapper.OTObjectSet;
 import org.concord.otrunk.OTrunkImpl;
 
 public class OTNavigationHistoryService extends DefaultOTObject implements OTBundle
 {
 	private static final Logger logger = Logger.getLogger(OTNavigationHistoryService.class.getCanonicalName());
 	private OTrunkImpl otrunk;
 	private HashMap<OTUser, OTObjectSet> navigationHistories = new HashMap<OTUser, OTObjectSet>();
 		
 	public static interface ResourceSchema extends OTResourceSchema 
 	{
 		// sequence of OTNavigationEvents
 		OTObjectSet getNavigationHistory();
 		void setNavigationHistory(OTObjectSet history);
 	}
 	private ResourceSchema resources;
 
 	public OTNavigationHistoryService(ResourceSchema resources)
     {
 	    super(resources);
 	    this.resources = resources;
 	    this.otrunk = (OTrunkImpl) resources.getOTObjectService().getOTrunkService(OTrunk.class);
     }
 
 	public void initializeBundle(OTServiceContext serviceContext)
 	{
 
 	}
 
 	public void registerServices(OTServiceContext serviceContext)
 	{
 		serviceContext.addService(OTNavigationHistoryService.class, this);
 	}
 	
 	private OTObjectSet getUserNavigationHistory(OTUser user) throws Exception {
 		if (user == null) {
			user = otrunk.getUsers().get(0);
 		}
 		
 		if (navigationHistories.containsKey(user)) {
 			return navigationHistories.get(user);
 		}
		OTObjectSet set = resources.getNavigationHistory();
		if (set == null) {
			set = otrunk.createObject(OTObjectSet.class);
			resources.setNavigationHistory(set);
 		}
		OTObjectSet defaultUserNavigationHistory = (OTObjectSet) otrunk.getUserRuntimeObject(set, user);
 		navigationHistories.put(user, defaultUserNavigationHistory);
 		return defaultUserNavigationHistory;
 	}
 	
 	public void logNavigationEvent(String type, OTObject obj) throws Exception {
 		logNavigationEvent(type, obj, null);
 	}
 	
 	public void logNavigationEvent(String type, OTObject obj, OTUser user) throws Exception {
 		logger.fine((user == null ? "null user" : user.getName()) + " " + type + ": " + obj);
 		OTObjectSet history = getUserNavigationHistory(user);
 		OTNavigationEvent event = history.getOTObjectService().createObject(OTNavigationEvent.class);
 		event.setTimestamp(System.currentTimeMillis());
 		event.setObject(obj);
 		event.setType(type);
 		history.getObjects().add(event);
 	}
 	
 	public OTObjectList getNavigationHistory() throws Exception {
 		return getUserNavigationHistory(null).getObjects();
 	}
 	
 	public OTObjectList getNavigationHistory(OTUser user) throws Exception {
 		return getUserNavigationHistory(user).getObjects();
 	}
 }
