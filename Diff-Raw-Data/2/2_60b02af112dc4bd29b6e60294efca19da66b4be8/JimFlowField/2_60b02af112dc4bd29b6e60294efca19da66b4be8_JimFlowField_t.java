 package de.itagile.jimflowjira;
 
 import java.sql.Timestamp;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Map;
 
 import org.apache.commons.httpclient.URIException;
 import org.apache.commons.httpclient.util.URIUtil;
 
 import com.atlassian.jira.issue.Issue;
 import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
 import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
 import com.atlassian.jira.issue.fields.CustomField;
 import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
 
 public class JimFlowField extends
 		com.atlassian.jira.issue.customfields.impl.URLCFType {
 	private static final String DEFAULT_URI = "http://jimflow.jimdo.com/";
 	private final DateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
 
 	public JimFlowField(CustomFieldValuePersister customFieldValuePersister,
 			GenericConfigManager genericConfigManager) {
 		super(customFieldValuePersister, genericConfigManager);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public Map getVelocityParameters(Issue issue, CustomField field,
 			FieldLayoutItem fieldLayoutItem) {
 		Map params = super.getVelocityParameters(issue, field, fieldLayoutItem);
 		try {
 			Object actualValue = field.getValue(issue);
 			String link = DEFAULT_URI;
 			if (actualValue != null) {
 				link = actualValue.toString();
 			}
 			Long id = issue.getId();
 			String summary = issue.getSummary();
 			String reporterName = issue.getReporter().getName();
 			Timestamp created = issue.getCreated();
 			String createdString = formatDate(created);
 			String type = issue.getIssueTypeObject().getName();
 			link = link.replaceFirst("\\$id", String.valueOf(id))
 					.replaceFirst("\\$title", summary)
 					.replaceFirst("\\$created", createdString)
 					.replaceFirst("\\$reporter", reporterName)
 					.replaceFirst("\\$type", type);
 			try {
 				link = URIUtil.encodeQuery(link);
 			} catch (URIException e) {
 				link = DEFAULT_URI;
 			}
 			params.put("v", link);
 		} catch (Exception e) {
			params.put("v", "");
 		}
 		return params;
 	}
 
 	public String formatDate(Timestamp created) {
 		return formatter.format(created);
 	}
 }
