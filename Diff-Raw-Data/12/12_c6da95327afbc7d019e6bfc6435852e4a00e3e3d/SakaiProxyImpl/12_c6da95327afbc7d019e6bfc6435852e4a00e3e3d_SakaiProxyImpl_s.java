 package org.sakaiproject.yaft.impl;
 
 import java.net.URLEncoder;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 
 import org.apache.log4j.Logger;
 import org.sakaiproject.api.app.profile.Profile;
 import org.sakaiproject.api.app.profile.ProfileManager;
 import org.sakaiproject.authz.api.AuthzGroup;
 import org.sakaiproject.authz.api.AuthzGroupService;
 import org.sakaiproject.authz.api.FunctionManager;
 import org.sakaiproject.authz.api.Role;
 import org.sakaiproject.authz.api.SecurityService;
 import org.sakaiproject.authz.api.SecurityAdvisor;
 import org.sakaiproject.calendar.api.Calendar;
 import org.sakaiproject.calendar.api.CalendarEvent;
 import org.sakaiproject.calendar.api.CalendarEventEdit;
 import org.sakaiproject.calendar.api.CalendarService;
 import org.sakaiproject.component.api.ServerConfigurationService;
 import org.sakaiproject.component.api.ComponentManager;
 import org.sakaiproject.content.api.ContentHostingService;
 import org.sakaiproject.content.api.ContentResource;
 import org.sakaiproject.content.api.ContentResourceEdit;
 import org.sakaiproject.db.api.SqlService;
 import org.sakaiproject.email.api.DigestService;
 import org.sakaiproject.email.api.EmailService;
 import org.sakaiproject.entity.api.EntityManager;
 import org.sakaiproject.entity.api.EntityProducer;
 import org.sakaiproject.entity.api.ResourceProperties;
 import org.sakaiproject.event.api.EventTrackingService;
 import org.sakaiproject.event.api.UsageSession;
 import org.sakaiproject.event.api.UsageSessionService;
 import org.sakaiproject.event.cover.NotificationService;
 import org.sakaiproject.exception.IdUnusedException;
 import org.sakaiproject.exception.IdUsedException;
 import org.sakaiproject.site.api.Site;
 import org.sakaiproject.site.api.SiteService;
 import org.sakaiproject.site.api.ToolConfiguration;
 import org.sakaiproject.time.api.TimeRange;
 import org.sakaiproject.time.api.TimeService;
 import org.sakaiproject.tool.api.Placement;
 import org.sakaiproject.tool.api.ToolManager;
 import org.sakaiproject.user.api.User;
 import org.sakaiproject.user.api.UserDirectoryService;
 import org.sakaiproject.util.BaseResourceProperties;
 import org.sakaiproject.yaft.api.Attachment;
 import org.sakaiproject.yaft.api.SakaiProxy;
 import org.sakaiproject.yaft.api.YaftForumService;
 import org.sakaiproject.yaft.api.YaftFunctions;
 import org.sakaiproject.yaft.api.YaftPermissions;
 
 /**
  * All Sakai API calls go in here. If Sakai changes all we have to do if mod this file.
  * 
  * @author Adrian Fish (a.fish@lancaster.ac.uk)
  */
 public class SakaiProxyImpl implements SakaiProxy
 {
 	private Logger logger = Logger.getLogger(SakaiProxyImpl.class);
 
 	private ServerConfigurationService serverConfigurationService = null;
 
 	private UserDirectoryService userDirectoryService = null;
 
 	private SqlService sqlService = null;
 
 	private SiteService siteService = null;
 
 	private ToolManager toolManager;
 
 	private ProfileManager profileManager;
 
 	private FunctionManager functionManager;
 
 	private AuthzGroupService authzGroupService;
 
 	private EmailService emailService;
 
 	private DigestService digestService;
 
 	private ContentHostingService contentHostingService;
 
 	private SecurityService securityService;
 
 	private EntityManager entityManager;
 
 	private EventTrackingService eventTrackingService;
 
 	private CalendarService calendarService;
 
 	private TimeService timeService;
 
 	private UsageSessionService usageSessionService;
 
 	public SakaiProxyImpl()
 	{
 		if (logger.isDebugEnabled())
 			logger.debug("SakaiProxy()");
 
 		ComponentManager componentManager = org.sakaiproject.component.cover.ComponentManager.getInstance();
 		serverConfigurationService = (ServerConfigurationService) componentManager.get(ServerConfigurationService.class);
 		userDirectoryService = (UserDirectoryService) componentManager.get(UserDirectoryService.class);
 		sqlService = (SqlService) componentManager.get(SqlService.class);
 		siteService = (SiteService) componentManager.get(SiteService.class);
 		toolManager = (ToolManager) componentManager.get(ToolManager.class);
 		profileManager = (ProfileManager) componentManager.get(ProfileManager.class);
 		functionManager = (FunctionManager) componentManager.get(FunctionManager.class);
 		authzGroupService = (AuthzGroupService) componentManager.get(AuthzGroupService.class);
 		emailService = (EmailService) componentManager.get(EmailService.class);
 		digestService = (DigestService) componentManager.get(DigestService.class);
 		securityService = (SecurityService) componentManager.get(SecurityService.class);
 		contentHostingService = (ContentHostingService) componentManager.get(ContentHostingService.class);
 		eventTrackingService = (EventTrackingService) componentManager.get(EventTrackingService.class);
 		timeService = (TimeService) componentManager.get(TimeService.class);
 		calendarService = (CalendarService) componentManager.get(CalendarService.class);
 		entityManager = (EntityManager) componentManager.get(EntityManager.class);
 		usageSessionService = (UsageSessionService) componentManager.get(UsageSessionService.class);
 	}
 
 	public boolean isAutoDDL()
 	{
 		if (logger.isDebugEnabled())
 			logger.debug("isAutoDDL()");
 
 		String autoDDL = serverConfigurationService.getString("auto.ddl");
 		return autoDDL.equals("true");
 	}
 
 	public String getDbVendor()
 	{
 		if (logger.isDebugEnabled())
 			logger.debug("getDbVendor()");
 
 		return sqlService.getVendor();
 	}
 
 	public Site getCurrentSite()
 	{
 		try
 		{
 			return siteService.getSite(getCurrentSiteId());
 		}
 		catch (Exception e)
 		{
 			logger.error("Failed to get current site.", e);
 			return null;
 		}
 	}
 
 	public String getCurrentSiteId()
 	{
 		Placement placement = toolManager.getCurrentPlacement();
 		if (placement == null)
 		{
 			logger.warn("Current tool placement is null.");
 			return null;
 		}
 
 		return placement.getContext();
 	}
 
 	public Connection borrowConnection() throws SQLException
 	{
 		if (logger.isDebugEnabled())
 			logger.debug("borrowConnection()");
 		return sqlService.borrowConnection();
 	}
 
 	public void returnConnection(Connection connection)
 	{
 		if (logger.isDebugEnabled())
 			logger.debug("returnConnection()");
 		sqlService.returnConnection(connection);
 	}
 
 	public String getDisplayNameForUser(String creatorId)
 	{
 		try
 		{
 			User sakaiUser = userDirectoryService.getUser(creatorId);
 			return sakaiUser.getDisplayName();
 		}
 		catch (Exception e)
 		{
 			return creatorId; // this can happen if the user does not longer exist in the system
 		}
 	}
 
 	public void registerFunction(String function)
 	{
 		List functions = functionManager.getRegisteredFunctions("yaft.");
 
 		if (!functions.contains(function))
 			functionManager.registerFunction(function);
 	}
 
 	public String getSakaiHomePath()
 	{
 		return serverConfigurationService.getSakaiHomePath();
 	}
 
 	public Profile getProfile(String userId)
 	{
 		return profileManager.getUserProfileById(userId);
 	}
 
 	public boolean addCalendarEntry(String title, String description, String type, long startDate, long endDate)
 	{
 		try
 		{
 			Calendar cal = calendarService.getCalendar("/calendar/calendar/" + getCurrentSiteId() + "/main");
 			CalendarEventEdit edit = cal.addEvent();
 			TimeRange range = timeService.newTimeRange(startDate, endDate - startDate);
 			edit.setRange(range);
 			edit.setDescriptionFormatted(description);
 			edit.setDisplayName(title);
 			edit.setType(type);
 			cal.commitEvent(edit);
 
 			return true;
 		}
 		catch (Exception e)
 		{
 			logger.error("Failed to add calendar entry. Returning false ...", e);
 			return false;
 		}
 	}
 
 	public boolean removeCalendarEntry(String title, String description)
 	{
 		try
 		{
 			Calendar cal = calendarService.getCalendar("/calendar/calendar/" + getCurrentSiteId() + "/main");
 			List<CalendarEvent> events = cal.getEvents(null, null);
 			for (CalendarEvent event : events)
 			{
 				if (event.getDisplayName().equals(title) && event.getDescription().equals(description))
 				{
 					CalendarEventEdit edit = cal.getEditEvent(event.getId(), CalendarService.SECURE_REMOVE);
 					cal.removeEvent(edit);
 					return true;
 				}
 			}
 
 			return true;
 
 		}
 		catch (Exception e)
 		{
 			logger.error("Failed to add calendar entry. Returning false ...", e);
 			return false;
 		}
 	}
 
 	public String getServerUrl()
 	{
 		return serverConfigurationService.getServerUrl();
 	}
 
 	public List<YaftPermissions> getPermissions(String siteId)
 	{
 		try
 		{
 			Site site = null;
 
 			if (siteId != null)
 				site = siteService.getSite(siteId);
 			else
 				site = siteService.getSite(getCurrentSiteId());
 
 			AuthzGroup realm = authzGroupService.getAuthzGroup(site.getReference());
 			Set<Role> roles = realm.getRoles();
 
 			List<YaftPermissions> list = new ArrayList<YaftPermissions>(roles.size());
 
 			for (Role role : roles)
 			{
 				YaftPermissions permissions = new YaftPermissions();
 				permissions.setRole(role.getId());
 				permissions.setForumCreate(role.isAllowed(YaftFunctions.YAFT_FORUM_CREATE));
 				permissions.setForumDeleteOwn(role.isAllowed(YaftFunctions.YAFT_FORUM_DELETE_OWN));
 				permissions.setForumDeleteAny(role.isAllowed(YaftFunctions.YAFT_FORUM_DELETE_ANY));
 				permissions.setDiscussionCreate(role.isAllowed(YaftFunctions.YAFT_DISCUSSION_CREATE));
 				permissions.setDiscussionDeleteOwn(role.isAllowed(YaftFunctions.YAFT_DISCUSSION_DELETE_OWN));
 				permissions.setDiscussionDeleteAny(role.isAllowed(YaftFunctions.YAFT_DISCUSSION_DELETE_ANY));
 				permissions.setMessageCreate(role.isAllowed(YaftFunctions.YAFT_MESSAGE_CREATE));
 				permissions.setMessageCensor(role.isAllowed(YaftFunctions.YAFT_MESSAGE_CENSOR));
 				permissions.setMessageDeleteOwn(role.isAllowed(YaftFunctions.YAFT_MESSAGE_DELETE_OWN));
 				permissions.setMessageDeleteAny(role.isAllowed(YaftFunctions.YAFT_MESSAGE_DELETE_ANY));
 				permissions.setViewInvisible(role.isAllowed(YaftFunctions.YAFT_VIEW_INVISIBLE));
 				permissions.setCanModifyPermissions(role.isAllowed(YaftFunctions.YAFT_MODIFY_PERMISSIONS));
 
 				list.add(permissions);
 			}
 
 			return list;
 		}
 		catch (Exception e)
 		{
 			logger.error("Caught exception whilst building permissions list", e);
 		}
 
 		return null;
 	}
 
 	public YaftPermissions getPermissionsForCurrentUser(String siteId)
 	{
 		try
 		{
 			String userId = userDirectoryService.getCurrentUser().getId();
 
 			YaftPermissions permissions = new YaftPermissions();
 
 			if (UserDirectoryService.ADMIN_ID.equals(userId))
 			{
 				// Special case for the super admin
 				permissions.setAll();
 			}
 			else
 			{
 				Site site = null;
 
 				if (siteId != null)
 					site = siteService.getSite(siteId);
 				else
 					site = siteService.getSite(getCurrentSiteId());
 
 				AuthzGroup realm = authzGroupService.getAuthzGroup(site.getReference());
 
 				AuthzGroup siteHelperRealm = authzGroupService.getAuthzGroup("!site.helper");
 				
 				Role role = realm.getUserRole(userId);
 				
 				if(role == null)
 				{
 					return permissions;
 				}
 				
 				permissions.setRole(role.getId());
 				
 				Role siteHelperRole = siteHelperRealm.getRole(role.getId());
 
 				if (siteHelperRole != null)
 				{
 					permissions.setForumCreate(role.isAllowed(YaftFunctions.YAFT_FORUM_CREATE) || siteHelperRole.isAllowed(YaftFunctions.YAFT_FORUM_CREATE));
 					permissions.setForumDeleteOwn(role.isAllowed(YaftFunctions.YAFT_FORUM_DELETE_OWN) || siteHelperRole.isAllowed(YaftFunctions.YAFT_FORUM_DELETE_OWN));
 					permissions.setForumDeleteAny(role.isAllowed(YaftFunctions.YAFT_FORUM_DELETE_ANY) || siteHelperRole.isAllowed(YaftFunctions.YAFT_FORUM_DELETE_ANY));
 					permissions.setDiscussionCreate(role.isAllowed(YaftFunctions.YAFT_DISCUSSION_CREATE) || siteHelperRole.isAllowed(YaftFunctions.YAFT_DISCUSSION_CREATE));
 					permissions.setDiscussionDeleteOwn(role.isAllowed(YaftFunctions.YAFT_DISCUSSION_DELETE_OWN) || siteHelperRole.isAllowed(YaftFunctions.YAFT_DISCUSSION_DELETE_OWN));
 					permissions.setDiscussionDeleteAny(role.isAllowed(YaftFunctions.YAFT_DISCUSSION_DELETE_ANY) || siteHelperRole.isAllowed(YaftFunctions.YAFT_DISCUSSION_DELETE_ANY));
 					permissions.setMessageCreate(role.isAllowed(YaftFunctions.YAFT_MESSAGE_CREATE) || siteHelperRole.isAllowed(YaftFunctions.YAFT_MESSAGE_CREATE));
 					permissions.setMessageDeleteOwn(role.isAllowed(YaftFunctions.YAFT_MESSAGE_DELETE_OWN) || siteHelperRole.isAllowed(YaftFunctions.YAFT_MESSAGE_DELETE_OWN));
 					permissions.setMessageDeleteAny(role.isAllowed(YaftFunctions.YAFT_MESSAGE_DELETE_ANY) || siteHelperRole.isAllowed(YaftFunctions.YAFT_MESSAGE_DELETE_ANY));
 					permissions.setMessageCensor(role.isAllowed(YaftFunctions.YAFT_MESSAGE_CENSOR) || siteHelperRole.isAllowed(YaftFunctions.YAFT_MESSAGE_CENSOR));
 					permissions.setViewInvisible(role.isAllowed(YaftFunctions.YAFT_VIEW_INVISIBLE) || siteHelperRole.isAllowed(YaftFunctions.YAFT_VIEW_INVISIBLE));
 					permissions.setCanModifyPermissions(role.isAllowed(YaftFunctions.YAFT_MODIFY_PERMISSIONS) || siteHelperRole.isAllowed(YaftFunctions.YAFT_MODIFY_PERMISSIONS));
 				}
 				else
 				{
 					permissions.setForumCreate(role.isAllowed(YaftFunctions.YAFT_FORUM_CREATE));
 					permissions.setForumDeleteOwn(role.isAllowed(YaftFunctions.YAFT_FORUM_DELETE_OWN));
 					permissions.setForumDeleteAny(role.isAllowed(YaftFunctions.YAFT_FORUM_DELETE_ANY));
 					permissions.setDiscussionCreate(role.isAllowed(YaftFunctions.YAFT_DISCUSSION_CREATE));
 					permissions.setDiscussionDeleteOwn(role.isAllowed(YaftFunctions.YAFT_DISCUSSION_DELETE_OWN));
 					permissions.setDiscussionDeleteAny(role.isAllowed(YaftFunctions.YAFT_DISCUSSION_DELETE_ANY));
 					permissions.setMessageCreate(role.isAllowed(YaftFunctions.YAFT_MESSAGE_CREATE));
 					permissions.setMessageDeleteOwn(role.isAllowed(YaftFunctions.YAFT_MESSAGE_DELETE_OWN));
 					permissions.setMessageDeleteAny(role.isAllowed(YaftFunctions.YAFT_MESSAGE_DELETE_ANY));
 					permissions.setMessageCensor(role.isAllowed(YaftFunctions.YAFT_MESSAGE_CENSOR));
 					permissions.setViewInvisible(role.isAllowed(YaftFunctions.YAFT_VIEW_INVISIBLE));
 					permissions.setCanModifyPermissions(role.isAllowed(YaftFunctions.YAFT_MODIFY_PERMISSIONS));
 				}
 			}
 
 			return permissions;
 		}
 		catch (Exception e)
 		{
 			logger.error("Caught exception whilst building permissions list", e);
 		}
 
 		return null;
 	}
 
 	public void savePermissions(String siteId, Map<String, YaftPermissions> permissionMap) throws Exception
 	{
 		Site site = siteService.getSite(siteId);
 		AuthzGroup realm = authzGroupService.getAuthzGroup(site.getReference());
 
 		Set roles = realm.getRoles();
 
 		for (Iterator i = roles.iterator(); i.hasNext();)
 		{
 			Role role = (Role) i.next();
 
 			if (!permissionMap.containsKey(role.getId()))
 				permissionMap.put(role.getId(), new YaftPermissions());
 		}
 
 		for (String roleName : permissionMap.keySet())
 		{
 			YaftPermissions permissions = permissionMap.get(roleName);
 
 			Role role = realm.getRole(roleName);
 
 			if (role == null)
 				throw new Exception("Role '" + roleName + "' has not been setup for this site");
 
 			if (permissions.isForumCreate())
 				role.allowFunction(YaftFunctions.YAFT_FORUM_CREATE);
 			else
 				role.disallowFunction(YaftFunctions.YAFT_FORUM_CREATE);
 
 			if (permissions.isForumDeleteAny())
 				role.allowFunction(YaftFunctions.YAFT_FORUM_DELETE_ANY);
 			else
 				role.disallowFunction(YaftFunctions.YAFT_FORUM_DELETE_ANY);
 
 			if (permissions.isForumDeleteOwn())
 				role.allowFunction(YaftFunctions.YAFT_FORUM_DELETE_OWN);
 			else
 				role.disallowFunction(YaftFunctions.YAFT_FORUM_DELETE_OWN);
 
 			if (permissions.isDiscussionCreate())
 				role.allowFunction(YaftFunctions.YAFT_DISCUSSION_CREATE);
 			else
 				role.disallowFunction(YaftFunctions.YAFT_DISCUSSION_CREATE);
 
 			if (permissions.isDiscussionDeleteOwn())
 				role.allowFunction(YaftFunctions.YAFT_DISCUSSION_DELETE_OWN);
 			else
 				role.disallowFunction(YaftFunctions.YAFT_DISCUSSION_DELETE_OWN);
 
 			if (permissions.isDiscussionDeleteAny())
 				role.allowFunction(YaftFunctions.YAFT_DISCUSSION_DELETE_ANY);
 			else
 				role.disallowFunction(YaftFunctions.YAFT_DISCUSSION_DELETE_ANY);
 
 			if (permissions.isMessageCreate())
 				role.allowFunction(YaftFunctions.YAFT_MESSAGE_CREATE);
 			else
 				role.disallowFunction(YaftFunctions.YAFT_MESSAGE_CREATE);
 
 			if (permissions.isMessageCensor())
 				role.allowFunction(YaftFunctions.YAFT_MESSAGE_CENSOR);
 			else
 				role.disallowFunction(YaftFunctions.YAFT_MESSAGE_CENSOR);
 
 			if (permissions.isMessageDeleteOwn())
 				role.allowFunction(YaftFunctions.YAFT_MESSAGE_DELETE_OWN);
 			else
 				role.disallowFunction(YaftFunctions.YAFT_MESSAGE_DELETE_OWN);
 
 			if (permissions.isMessageDeleteAny())
 				role.allowFunction(YaftFunctions.YAFT_MESSAGE_DELETE_ANY);
 			else
 				role.disallowFunction(YaftFunctions.YAFT_MESSAGE_DELETE_ANY);
 
 			if (permissions.isViewInvisible())
 				role.allowFunction(YaftFunctions.YAFT_VIEW_INVISIBLE);
 			else
 				role.disallowFunction(YaftFunctions.YAFT_VIEW_INVISIBLE);
 			
 			if (permissions.isCanModifyPermissions())
 				role.allowFunction(YaftFunctions.YAFT_MODIFY_PERMISSIONS);
 			else
 				role.disallowFunction(YaftFunctions.YAFT_MODIFY_PERMISSIONS);
 		}
 
 		authzGroupService.save(realm);
 	}
 
 	private String getEmailForTheUser(String userId)
 	{
 		try
 		{
 			User sakaiUser = userDirectoryService.getUser(userId);
 			return sakaiUser.getEmail();
 		}
 		catch (Exception e)
 		{
 			return ""; // this can happen if the user does not longer exist in the system
 		}
 	}
 
 	public void sendEmailMessage(String subject, String body, String user)
 	{
 		try
 		{
 			List<String> additionalHeader = new ArrayList<String>();
 			additionalHeader.add("Content-Type: text/html; charset=ISO-8859-1");
 
 			String emailParticipant = getEmailForTheUser(user);
 			try
 			{
 				String sender = "sakai-forum@" + serverConfigurationService.getServerName();
 				emailService.send(sender, emailParticipant, subject, body, emailParticipant, sender, additionalHeader);
 			}
 			catch (Exception e)
 			{
 				System.out.println("Failed to send email to '" + user + "'. Message: " + e.getMessage());
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	public void addDigestMessage(String user, String subject, String body)
 	{
 		try
 		{
 			try
 			{
 				digestService.digest(user, subject, body);
 			}
 			catch (Exception e)
 			{
 				System.out.println("Failed to add message to digest. Message: " + e.getMessage());
 			}
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	public Set<String> getSiteUsers()
 	{
 		try
 		{
 			Site site = siteService.getSite(getCurrentSiteId());
 			return site.getUsers();
 		}
 		catch (Exception e)
 		{
 			return null;
 		}
 	}
 
 	public User getCurrentUser()
 	{
 		try
 		{
 			return userDirectoryService.getCurrentUser();
 		}
 		catch (Throwable t)
 		{
 			logger.error("Exception caught whilst getting current user.", t);
 			if (logger.isDebugEnabled())
 				logger.debug("Returning null ...");
 			return null;
 		}
 	}
 
 	public String getPortalUrl()
 	{
 		return serverConfigurationService.getServerUrl() + "/portal";
 	}
 
 	public String getCurrentPageId()
 	{
 		Placement placement = toolManager.getCurrentPlacement();
 
 		if (placement instanceof ToolConfiguration)
 			return ((ToolConfiguration) placement).getPageId();
 
 		return null;
 	}
 
 	public String getCurrentToolId()
 	{
 		return toolManager.getCurrentPlacement().getId();
 	}
 
 	public String getDirectUrl(String string)
 	{
 		String portalUrl = getPortalUrl();
 		String pageId = getCurrentPageId();
 		String siteId = getCurrentSiteId();
 		String toolId = getCurrentToolId();
 
 		try
 		{
 			String url = portalUrl + "/site/" + siteId + "/page/" + pageId + "?toolstate-" + toolId + "=" + URLEncoder.encode(string, "UTF-8");
 
 			return url;
 		}
 		catch (Exception e)
 		{
 			logger.error("Caught exception whilst building direct URL.", e);
 			return null;
 		}
 	}
 
 	private void enableSecurityAdvisor()
 	{
 		securityService.pushAdvisor(new SecurityAdvisor()
 		{
 			public SecurityAdvice isAllowed(String userId, String function, String reference)
 			{
 				return SecurityAdvice.ALLOWED;
 			}
 		});
 	}
 
 	/**
 	 * Saves the file to Sakai's content hosting
 	 */
 	public String saveFile(String creatorId, String name, String mimeType, byte[] fileData) throws Exception
 	{
 		if (logger.isDebugEnabled())
 			logger.debug("saveFile(" + name + "," + mimeType + ",[BINARY FILE DATA])");
 
 		if (name == null | name.length() == 0)
 			throw new IllegalArgumentException("The name argument must be populated.");
 
 		if (name.endsWith(".doc"))
 			mimeType = "application/msword";
 		else if (name.endsWith(".xls"))
 			mimeType = "application/excel";
 
 		// String uuid = UUID.randomUUID().toString();
 
 		String id = "/group/" + getCurrentSiteId() + "/yaft-files/" + name;
 
 		try
 		{
 			enableSecurityAdvisor();
 
 			ContentResourceEdit resource = contentHostingService.addResource(id);
 			resource.setContentType(mimeType);
 			resource.setContent(fileData);
 			ResourceProperties props = new BaseResourceProperties();
 			props.addProperty(ResourceProperties.PROP_CONTENT_TYPE, mimeType);
 			props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, name);
 			props.addProperty(ResourceProperties.PROP_CREATOR, creatorId);
 			props.addProperty(ResourceProperties.PROP_ORIGINAL_FILENAME, name);
 			resource.getPropertiesEdit().set(props);
 			contentHostingService.commitResource(resource, NotificationService.NOTI_NONE);
 
 			// return resource.getId();
 			return name;
 		}
 		catch (IdUsedException e)
 		{
 			if (logger.isInfoEnabled())
 				logger.info("A resource with id '" + id + "' exists already. Returning id without recreating ...");
 			return name;
 		}
 	}
 
 	public void getAttachment(Attachment attachment)
 	{
 		try
 		{
 			enableSecurityAdvisor();
 			String id = "/group/" + getCurrentSiteId() + "/yaft-files/" + attachment.getResourceId();
 			// ContentResource resource = contentHostingService.getResource(attachment.getResourceId());
 			ContentResource resource = contentHostingService.getResource(id);
 			ResourceProperties properties = resource.getProperties();
 			attachment.setMimeType(properties.getProperty(ResourceProperties.PROP_CONTENT_TYPE));
 			attachment.setName(properties.getProperty(ResourceProperties.PROP_DISPLAY_NAME));
 			attachment.setUrl(resource.getUrl());
 		}
 		catch (Exception e)
 		{
 			if (logger.isDebugEnabled())
 				e.printStackTrace();
 
 			logger.error("Caught an exception with message '" + e.getMessage() + "'");
 		}
 	}
 
 	public void deleteFile(String resourceId) throws Exception
 	{
 		enableSecurityAdvisor();
 		String id = "/group/" + getCurrentSiteId() + "/yaft-files/" + resourceId;
 		contentHostingService.removeResource(id);
 	}
 
 	public String getUserBio(String id)
 	{
		Profile profile = profileManager.getUserProfileById(id);
		if (profile != null)
			return profile.getOtherInformation();
 
 		return "";
 	}
 
 	public User getUser(String userId) throws Exception
 	{
 		return userDirectoryService.getUser(userId);
 	}
 
 	public List<Site> getAllSites()
 	{
 		return siteService.getSites(SiteService.SelectionType.NON_USER, null, null, null, null, null);
 	}
 
 	public ToolConfiguration getFirstInstanceOfTool(String siteId, String toolId)
 	{
 		try
 		{
 			return siteService.getSite(siteId).getToolForCommonId(toolId);
 		}
 		catch (IdUnusedException e)
 		{
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public String[] getSiteIds()
 	{
 		return null;
 	}
 
 	public void registerEntityProducer(EntityProducer entityProducer)
 	{
 		entityManager.registerEntityProducer(entityProducer, YaftForumService.ENTITY_PREFIX);
 	}
 
 	public void postEvent(String event, String reference, boolean modify)
 	{
 		eventTrackingService.post(eventTrackingService.newEvent(event, reference, modify));
 	}
 
 	public byte[] getResourceBytes(String resourceId)
 	{
 		try
 		{
 			enableSecurityAdvisor();
 			ContentResource resource = contentHostingService.getResource(resourceId);
 			return resource.getContent();
 		}
 		catch (Exception e)
 		{
 			logger.error("Caught an exception with message '" + e.getMessage() + "'");
 		}
 
 		return null;
 	}
 
 	public List<String> getOfflineYaftUserIds(String siteId) throws IdUnusedException
 	{
 		List<String> yaftUserIds = new ArrayList<String>();
 
 		List<Site> allSites = siteService.getSites(SiteService.SelectionType.ANY, null, null, null, SiteService.SortType.NONE, null);
 		Site site = siteService.getSite(siteId);
 
 		if (site.getToolForCommonId("sakai.yaft") != null)
 		{
 			// This site contains yaft. Get it's user ids.
 			Set<String> userIds = site.getUsers();
 			yaftUserIds.addAll(userIds);
 		}
 
 		List<UsageSession> openSessions = usageSessionService.getOpenSessions();
 
 		List<String> offlineYaftUserIds = new ArrayList<String>();
 
 		for (String yaftUserId : yaftUserIds)
 		{
 			boolean offline = true;
 
 			for (UsageSession session : openSessions)
 			{
 				if (session.getUserId().equals(yaftUserId))
 					offline = false;
 			}
 
 			if (offline)
 				offlineYaftUserIds.add(yaftUserId);
 		}
 
 		return offlineYaftUserIds;
 	}
 }
