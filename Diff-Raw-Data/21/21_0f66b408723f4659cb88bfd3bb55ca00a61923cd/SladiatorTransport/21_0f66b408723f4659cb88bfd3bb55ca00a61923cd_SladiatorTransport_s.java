 package lv.ebit.jira.plugins;
 
 import java.io.IOException;
 import java.net.URI;
 import java.sql.Timestamp;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.methods.PutMethod;
 import org.apache.commons.httpclient.methods.StringRequestEntity;
 import org.ofbiz.core.entity.GenericValue;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.atlassian.core.ofbiz.CoreFactory;
 import com.atlassian.crowd.embedded.api.User;
 import com.atlassian.jira.ComponentManager;
 import com.atlassian.jira.avatar.Avatar;
 import com.atlassian.jira.avatar.AvatarService;
 import com.atlassian.jira.bc.project.component.ProjectComponent;
 import com.atlassian.jira.component.ComponentAccessor;
 import com.atlassian.jira.event.type.EventType;
 import com.atlassian.jira.issue.CustomFieldManager;
 import com.atlassian.jira.issue.Issue;
 import com.atlassian.jira.issue.fields.CustomField;
 import com.atlassian.jira.issue.label.Label;
 import com.atlassian.jira.issue.link.IssueLink;
 import com.atlassian.jira.ofbiz.DefaultOfBizDelegator;
 import com.atlassian.jira.ofbiz.OfBizDelegator;
 import com.atlassian.jira.util.collect.MapBuilder;
 import com.atlassian.jira.util.json.JSONException;
 import com.atlassian.jira.util.json.JSONObject;
 
 public class SladiatorTransport implements Runnable {
 	public static final Logger log = LoggerFactory.getLogger(SladiatorTransport.class);
 	private SladiatorConfigModel config;
 	private SimpleDateFormat dateFormat;
 	private String jiraUrl;
 	private Long eventTypeId;
 	private Issue issue;
 	private AvatarService avatarService;
 
 	public SladiatorTransport(SladiatorConfigModel config, String jiraUrl, Long eventTypeId, Issue issue, AvatarService avatarService) {
 		this.config = config;
 		this.jiraUrl = jiraUrl;
 		this.eventTypeId = eventTypeId;
 		this.issue = issue;
 		this.avatarService = avatarService;
 		this.dateFormat = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss Z");
 //		this.dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
 	}
 	
 	public SladiatorTransport(SladiatorConfigModel config) {
 		this.config = config;
 	}
 	@Override
 	public void run() {
 		SladiatorIssueListener.removeFailedIssue(issue.getProjectObject().getId().toString(),issue.getKey());
 		JSONObject json;
 		try {
 			json = collectIssueInfo(issue, this.config.isSendAssignee());
 			if (sendData(json.toString())) {
 				log.info("Issue {} was sent", issue.getKey());
 			} else {
 				SladiatorIssueListener.addFailedIssue(issue.getProjectObject().getId().toString(),issue.getKey());
 				log.error("Issue {} was not sent", issue.getKey());
 			}
 		} catch (JSONException e) {
 			log.error("Issue {} was not collected", issue.getKey());
 		}
 	}
 	
 	public JSONObject collectIssueInfo(Issue issue, boolean collectAssignee) throws JSONException {
 		JSONObject json = new JSONObject();
 		json.putOpt("source", SladiatorIssueListener.getSource());
 		json.putOpt("key", issue.getKey());
 		json.putOpt("issue_created_at", this.dateFormat.format(issue.getCreated()));
 		json.putOpt("issue_updated_at", this.dateFormat.format(issue.getUpdated()));
 		if (issue.getDueDate() != null) {
 			json.putOpt("due_date", this.dateFormat.format(issue.getDueDate().getTime() + ((24 * 60 * 60) - 1) * 1000));
 //			json.putOpt("due_date", issue.getDueDate());
 		} else {
 			json.putOpt("due_date", JSONObject.NULL);
 		}
 		if (issue.getPriorityObject() != null) {
 			json.putOpt("priority", issue.getPriorityObject().getName());
 		}
 		json.putOpt("issue_type", issue.getIssueTypeObject().getName());
 		json.putOpt("status", issue.getStatusObject().getName());
 		json.putOpt("project", issue.getProjectObject().getKey());
 		json.putOpt("url", jiraUrl+"/browse/" + issue.getKey());
 		List<HashMap<String,String>> links = new ArrayList<HashMap<String,String>>();
 		
 		Iterator<IssueLink> link = ComponentManager.getInstance().getIssueLinkManager().getInwardLinks(issue.getId()).iterator();
 		while (link.hasNext()) {
 			IssueLink currentLink = link.next();
 			HashMap<String, String> map = new HashMap<String, String>();
 			map.put("key",currentLink.getSourceObject().getKey());
 			map.put("link",currentLink.getIssueLinkType().getInward());
 			links.add(map);
 		}
 		link = ComponentManager.getInstance().getIssueLinkManager().getOutwardLinks(issue.getId()).iterator();
 		while (link.hasNext()) {
 			IssueLink currentLink = link.next();
 			HashMap<String, String> map = new HashMap<String, String>();
 			map.put("key",currentLink.getDestinationObject().getKey());
 			map.put("link", currentLink.getIssueLinkType().getOutward());
 			links.add(map);
 		}
 		json.putOpt("links", links);
 		
 		Iterator<Label> label = issue.getLabels().iterator();
 		List<String> labels = new ArrayList<String>();
 		while (label.hasNext()) {
 			labels.add(label.next().toString());
 		}
 		json.putOpt("labels", labels);
 		
 		Iterator<ProjectComponent> component = issue.getComponentObjects().iterator();
 		List<String> components = new ArrayList<String>();
 		while (component.hasNext()) {
 			components.add(component.next().getName());
 		}
 		json.putOpt("components", components);
 		
 		if (collectAssignee) {
 			User assignee = issue.getAssigneeUser();
 			if (assignee != null) {
 				json.putOpt("assignee", assignee.getDisplayName());
 				json.putOpt("assignee_email", assignee.getEmailAddress());
 				URI jira_uri = URI.create(jiraUrl);
 				json.putOpt("assignee_avatar_url", jira_uri.resolve(this.avatarService.getAvatarURL(assignee, assignee.getName(), Avatar.Size.LARGE)));
 			}
 		}
 
 		if (issue.getResolutionObject() != null) {
 			json.putOpt("resolution", issue.getResolutionObject().getName());
 			json.putOpt("resolution_date", this.dateFormat.format(issue.getResolutionDate()));
 //			json.putOpt("resolution_date", issue.getResolutionDate());
 		} else {
 			json.putOpt("resolution", JSONObject.NULL);
 			json.putOpt("resolution_date", JSONObject.NULL);
 		}
 		json.putOpt("transitions", getTransitions(issue));
 		
 		CustomFieldManager customFieldManager = ComponentManager.getInstance().getCustomFieldManager();
 		
 		if (!this.config.getCustomField1().isEmpty()) {
 			CustomField field = customFieldManager.getCustomFieldObject(this.config.getCustomField1());
 			
 			String value = SladiatorCustomFieldNormalizer.getValue(field.getCustomFieldType(), field, issue);
 			if (value != null) {
 				json.putOpt("custom_field1", value);
 			} else {
 				json.putOpt("custom_field1", JSONObject.NULL);
 			}
 			
 		} else {
 			json.putOpt("custom_field1", JSONObject.NULL);
 		}
 		if (!this.config.getCustomField2().isEmpty()) {
 			CustomField field = customFieldManager.getCustomFieldObject(this.config.getCustomField2());
 			String value = SladiatorCustomFieldNormalizer.getValue(field.getCustomFieldType(), field, issue);
 			if (value != null) {
 				json.putOpt("custom_field2", value);
 			} else {
 				json.putOpt("custom_field2", JSONObject.NULL);
 			}
 		} else {
 			json.putOpt("custom_field2", JSONObject.NULL);
 		}
 		if (!this.config.getCustomField3().isEmpty()) {
 			CustomField field = customFieldManager.getCustomFieldObject(this.config.getCustomField3());
 			String value = SladiatorCustomFieldNormalizer.getValue(field.getCustomFieldType(), field, issue);
 			if (value != null) {
 				json.putOpt("custom_field3", value);
 			} else {
 				json.putOpt("custom_field3", JSONObject.NULL);
 			}
 		} else {
 			json.putOpt("custom_field3", JSONObject.NULL);
 		}
 		return json;
 	}
 	public List<JSONObject> getTransitions(Issue issue) throws JSONException {
 		OfBizDelegator delegator = new DefaultOfBizDelegator(CoreFactory.getGenericDelegator());
 		Map<String, Long> params = MapBuilder.build("issue", issue.getId());
 		List<GenericValue> changeGroups = delegator.findByAnd("ChangeGroup", params);

		List<JSONObject> retList = new ArrayList<JSONObject>();
 		
 		Timestamp entered_at = issue.getCreated();
 		for (GenericValue changeGroup : changeGroups) {
 			// starting from 4.4
 			// Map<String, ? extends Object> paramsItem =
 			// MapBuilder.build("group", changeGroup.getLong("id"),"field",
 			// "status", "fieldtype", "jira");
 			Map<String, Object> paramsItem = new HashMap<String, Object>();
 			paramsItem.put("group", changeGroup.getLong("id"));
 			paramsItem.put("field", "status");
 			paramsItem.put("fieldtype", "jira");
 
 			List<GenericValue> changeItems = delegator.findByAnd("ChangeItem", paramsItem);
 			for (GenericValue changeItem : changeItems) {
 				JSONObject json = new JSONObject();
 				json.put("entered_at", this.dateFormat.format(entered_at));
 //				json.put("entered_at", entered_at);
 				json.put("exited_at", this.dateFormat.format(changeGroup.getTimestamp("created")));
 //				json.put("exited_at", changeGroup.getTimestamp("created"));
 				json.put("status_to", ComponentAccessor.getConstantsManager().getStatusObject(changeItem.getString("newvalue")).getName());
 				json.put("status", ComponentAccessor.getConstantsManager().getStatusObject(changeItem.getString("oldvalue")).getName());
 				retList.add(json);
 				entered_at = changeGroup.getTimestamp("created");
 				// Deprecated. Use ComponentAccessor instead. Since v4.4.
 				// ManagerFactory.getConstantsManager().getStatusObject(changeItem.getString("newvalue"));
 			}
 
 		}
 		// add current state
 		JSONObject json = new JSONObject();
 		json.put("entered_at", this.dateFormat.format(entered_at));
 //		json.put("entered_at", entered_at);
 		json.put("status", issue.getStatusObject().getName());
 		retList.add(json);
 		
 		return retList;
 	}
 	public static String ping(String url, String token) {
 		HttpClient client = new HttpClient();
 		int statusCode = 0;
 		String status = "";
 		GetMethod httpMethod = new GetMethod(url+"/api/ping");
 		httpMethod.setRequestHeader("SLA_TOKEN", token);
 		try {
 			statusCode = client.executeMethod(httpMethod);
 		} catch (Exception e) {
 			status = e.getMessage()+". Please contact your system administrator";
 		}
 		if (statusCode == 401) {
 			status = "SLA token is not valid";
 		} else if (statusCode == 404) {
 			status = "SLAdiator service URL is not valid. Please contact your system administrator";
 		} else if (statusCode == 500) {
 			status = "SLAdiator is currently not available, please try later";
 		}
 		
 		return status;
 	}
 	public boolean sendData(String body) {
 		HttpClient client = new HttpClient();
 		int statusCode = 0;
 //		log.error("sending content "+body);
 		try {
 			if (eventTypeId.equals(EventType.ISSUE_CREATED_ID)) {
 				PostMethod httpMethod = new PostMethod(SladiatorIssueListener.getServiceUrl() + "/api/tickets/");
 				httpMethod.setRequestHeader("Content-Type", "application/json");
 				httpMethod.setRequestHeader("SLA_TOKEN", this.config.getSlaToken());
 
 				httpMethod.setRequestEntity(new StringRequestEntity(body, "application/json", null));
 				statusCode = client.executeMethod(httpMethod);
 			} else {
 				PutMethod httpMethod = new PutMethod(SladiatorIssueListener.getServiceUrl() + "/api/tickets/" + issue.getKey());
 				httpMethod.setRequestHeader("Content-Type", "application/json");
 				httpMethod.setRequestHeader("SLA_TOKEN", this.config.getSlaToken());
 
 				httpMethod.setRequestEntity(new StringRequestEntity(body, "application/json", null));
 				statusCode = client.executeMethod(httpMethod);
 			}
 		} catch (HttpException e) {
 			log.error("HttpException:"+e.getMessage());
 			log.error(e.getStackTrace().toString());
 		} catch (IOException e) {
 			log.error("IOException"+e.getMessage());
 		} catch (Exception e) {
 			log.error("Exception"+e.getMessage());
 		}
 		if (statusCode != 200) {
 			log.error("statusCode was " + statusCode);
 			return false;
 		}
 		return true;
 	}
 }
