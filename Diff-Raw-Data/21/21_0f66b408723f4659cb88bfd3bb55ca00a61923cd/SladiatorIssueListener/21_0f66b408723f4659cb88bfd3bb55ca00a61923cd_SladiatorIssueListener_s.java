 package lv.ebit.jira.plugins;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.ofbiz.core.entity.GenericValue;
 import org.springframework.beans.factory.DisposableBean;
 import org.springframework.beans.factory.InitializingBean;
 
 import com.atlassian.core.ofbiz.CoreFactory;
 import com.atlassian.event.api.EventListener;
 import com.atlassian.event.api.EventPublisher;
 import com.atlassian.jira.avatar.AvatarService;
 import com.atlassian.jira.event.issue.IssueEvent;
 import com.atlassian.jira.event.type.EventType;
 import com.atlassian.jira.issue.Issue;
 import com.atlassian.jira.ofbiz.DefaultOfBizDelegator;
 import com.atlassian.jira.ofbiz.OfBizDelegator;
 import com.atlassian.sal.api.ApplicationProperties;
 import com.atlassian.sal.api.pluginsettings.PluginSettings;
 import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
 import com.google.common.base.Joiner;
 
 public class SladiatorIssueListener implements InitializingBean, DisposableBean {
 	private final EventPublisher eventPublisher;
 //	private List<Long> validEventsList;
 	private static PluginSettingsFactory pluginSettingsFactory;
 	private static ApplicationProperties applicationProperties;
 	private final AvatarService avatarService;
 	private String jiraUrl;
 	
 	@EventListener
 	public void onIssueEvent(IssueEvent issueEvent) {
 		//FIXME
 		//"Issue linked" is not calling event :(
 		//https://jira.atlassian.com/browse/JRA-8505
 		Long eventTypeId = issueEvent.getEventTypeId();
 		Issue issue = issueEvent.getIssue();
 		
 		PluginSettings pluginSettings = pluginSettingsFactory.createSettingsForKey(SladiatorConfigModel.KEY);
 		
 		if (eventTypeId == EventType.ISSUE_MOVED_ID) {
 			OfBizDelegator delegator = new DefaultOfBizDelegator(CoreFactory.getGenericDelegator());
 			String oldProjectId = "";
 			
 			Map<String, Object> paramsItem = new HashMap<String, Object>();
 			paramsItem.put("group", issueEvent.getChangeLog().getLong("id"));
 			paramsItem.put("field", "Project");
 			paramsItem.put("fieldtype", "jira");
 			
 			List<GenericValue> changeItems = delegator.findByAnd("ChangeItem", paramsItem);
 			for (GenericValue changeItem : changeItems) {
 				oldProjectId = changeItem.getString("oldvalue");
 			}
 			if (!oldProjectId.isEmpty()){
 				SladiatorConfigModel config = new SladiatorConfigModel(pluginSettings.get(oldProjectId));
 				if (config.sendToSladiator()) {
 					String oldKey = "";
 					paramsItem.put("field", "Key");
 					changeItems = delegator.findByAnd("ChangeItem", paramsItem);
 					for (GenericValue changeItem : changeItems) {
 						oldKey = changeItem.getString("oldstring");
 					}
 					Runnable transportDelete = new SladiatorTransportDelete(config,oldKey);
 					new Thread(transportDelete).start();
 				}
 			}
 		}
 
 		
 		SladiatorConfigModel configuration = new SladiatorConfigModel(pluginSettings.get(issue.getProjectObject().getId().toString()));
 		if (configuration.sendToSladiator()) {
 			if (eventTypeId == EventType.ISSUE_DELETED_ID) {
 				Runnable transport = new SladiatorTransportDelete(configuration,issue.getKey());
 				new Thread(transport).start();
 			} else {
 				Runnable transport = new SladiatorTransport(configuration, this.jiraUrl,eventTypeId ,issue, this.avatarService);
 				new Thread(transport).start();
 			}
 			
 		}
 	}
 	public static void addFailedIssue(String project, String key) {
 		PluginSettings pluginSettings = pluginSettingsFactory.createSettingsForKey(SladiatorConfigModel.KEY);
 		synchronized(pluginSettings) {
 			List<String> errors = new ArrayList<String>(); 
 	        if (pluginSettings.get("errors"+project) != null) {
 	        	errors = new ArrayList<String>(Arrays.asList(pluginSettings.get("errors"+project).toString().split(",")));
 	        	errors.remove("");
 	        }
 	        errors.remove(key);
 	        errors.add(key);
 			pluginSettings.put("errors"+project,Joiner.on(",").skipNulls().join(errors));
 		}
 	}
 	public static void removeFailedIssue(String project, String key) {
 		PluginSettings pluginSettings = pluginSettingsFactory.createSettingsForKey(SladiatorConfigModel.KEY);
 		synchronized(pluginSettings) {
 			List<String> errors = new ArrayList<String>(); 
 	        if (pluginSettings.get("errors"+project) != null) {
 	        	errors = new ArrayList<String>(Arrays.asList(pluginSettings.get("errors"+project).toString().split(",")));
 	        	errors.remove("");
 	        }
 			errors.remove(key);
 			pluginSettings.put("errors"+project,Joiner.on(",").skipNulls().join(errors));
 		}
 	}
 	public static ArrayList<String> getFailedIssues(String project) {
 		PluginSettings pluginSettings = pluginSettingsFactory.createSettingsForKey(SladiatorConfigModel.KEY);
 		return new ArrayList<String>(Arrays.asList(pluginSettings.get("errors"+project).toString().split(",")));
 	}
 	
 	public static String getServiceUrl() {
 		PluginSettings pluginSettings = pluginSettingsFactory.createSettingsForKey(SladiatorConfigModel.KEY);
 		if (pluginSettings.get("service_url") == null) {
 			return "https://sladiator.ebit.lv";
 		} else {
 			return pluginSettings.get("service_url").toString();
 		}
 	}
 	public static void setServiceUrl(String url) {
 		PluginSettings pluginSettings = pluginSettingsFactory.createSettingsForKey(SladiatorConfigModel.KEY);
 		pluginSettings.put("service_url",url);
 	}
 	public static String getSource() {
		return "JIRA "+applicationProperties.getVersion()+"; jira-plugin v1.1.3";
 	}
 	/**
 	 * Constructor.
 	 * 
 	 * @param eventPublisher
 	 *            injected {@code EventPublisher} implementation.
 	 */
 	public SladiatorIssueListener(EventPublisher eventPublisher, PluginSettingsFactory pluginSettingsFactory, ApplicationProperties applicationProperties, AvatarService avatarService) {
 		this.jiraUrl = applicationProperties.getBaseUrl();
 		this.avatarService = avatarService;
 		this.eventPublisher = eventPublisher;
 		SladiatorIssueListener.pluginSettingsFactory = pluginSettingsFactory;
 		SladiatorIssueListener.applicationProperties = applicationProperties; 
 //		this.validEventsList = new ArrayList<Long>();
 //		this.validEventsList.add(EventType.ISSUE_CREATED_ID);
 //		this.validEventsList.add(EventType.ISSUE_UPDATED_ID);
 //		this.validEventsList.add(EventType.ISSUE_ASSIGNED_ID);
 //		this.validEventsList.add(EventType.ISSUE_RESOLVED_ID);
 //		this.validEventsList.add(EventType.ISSUE_CLOSED_ID);
 //		this.validEventsList.add(EventType.ISSUE_COMMENTED_ID);
 //		this.validEventsList.add(EventType.ISSUE_REOPENED_ID);
 //		this.validEventsList.add(EventType.ISSUE_DELETED_ID);
 //		this.validEventsList.add(EventType.ISSUE_MOVED_ID);
 //		this.validEventsList.add(EventType.ISSUE_WORKLOGGED_ID);
 //		this.validEventsList.add(EventType.ISSUE_WORKSTARTED_ID);
 //		this.validEventsList.add(EventType.ISSUE_WORKSTOPPED_ID);
 //		this.validEventsList.add(EventType.ISSUE_GENERICEVENT_ID);
 //		this.validEventsList.add(EventType.ISSUE_COMMENT_EDITED_ID);
 //		this.validEventsList.add(EventType.ISSUE_WORKLOG_UPDATED_ID);
 //		this.validEventsList.add(EventType.ISSUE_WORKLOG_DELETED_ID);
 		
 	}
 
 	/**
 	 * Called when the plugin has been enabled.
 	 * 
 	 * @throws Exception
 	 */
 	@Override
 	public void afterPropertiesSet() throws Exception {
 		// register ourselves with the EventPublisher
 		eventPublisher.register(this);
 	}
 
 	/**
 	 * Called when the plugin is being disabled or removed.
 	 * 
 	 * @throws Exception
 	 */
 	@Override
 	public void destroy() throws Exception {
 		// unregister ourselves with the EventPublisher
 		eventPublisher.unregister(this);
 	}
 }
