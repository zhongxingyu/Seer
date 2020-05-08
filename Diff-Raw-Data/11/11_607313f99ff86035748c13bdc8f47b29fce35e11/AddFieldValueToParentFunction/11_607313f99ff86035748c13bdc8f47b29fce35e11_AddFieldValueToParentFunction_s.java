 package com.innovalog.jmwe.plugins.functions;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import webwork.dispatcher.ActionResult;
 
 import com.atlassian.core.ofbiz.CoreFactory;
 import com.atlassian.core.util.map.EasyMap;
 import com.atlassian.jira.action.ActionNames;
 import com.atlassian.jira.issue.MutableIssue;
 import com.atlassian.jira.issue.fields.Field;
 import com.atlassian.jira.issue.util.DefaultIssueChangeHolder;
 import com.atlassian.jira.issue.util.IssueChangeHolder;
 import com.atlassian.jira.util.ImportUtils;
 import com.innovalog.googlecode.jsu.util.WorkflowUtils;
 import com.opensymphony.module.propertyset.PropertySet;
 import com.opensymphony.workflow.WorkflowException;
 
 public class AddFieldValueToParentFunction extends AbstractPreserveChangesPostFunction {
 	private Logger log = Logger.getLogger(AddFieldValueToParentFunction.class);
 	private static final String FIELD = "field";
 
 	public void executeFunction(Map transientVars, Map args, PropertySet ps, IssueChangeHolder holder)
 			throws WorkflowException {
 		String fieldKey = (String) args.get(FIELD);
 		Field field = (Field) WorkflowUtils.getFieldFromKey(fieldKey);
 		if (field == null) {
 			log.warn("Error while executing function : field [" + fieldKey
 					+ "] not found");
 			return;
 		}
 
 		boolean indexingPreviouslyEnabled = ImportUtils.isIndexIssues();
 
 		try {
 			MutableIssue issue = getIssue(transientVars);
 			Object sourceValue = WorkflowUtils.getFieldValueFromIssue(issue,
 					field);
 			if (sourceValue != null && sourceValue instanceof Collection) {
 				// get the parent issue
 				MutableIssue parentIssue = (MutableIssue)issue.getParentObject();
 				if (parentIssue != null)
 				{
 					//get parent issue's field value
 					Object parentValue = WorkflowUtils.getFieldValueFromIssue(parentIssue,field);
 					if (parentValue == null)
 						parentValue = new ArrayList();
 					if (parentValue instanceof Collection)
 					{
 						if (!indexingPreviouslyEnabled)
 							ImportUtils.setIndexIssues(true);
 						((Collection)parentValue).addAll((Collection)sourceValue);
 						WorkflowUtils.setFieldValue(parentIssue, field, parentValue, new DefaultIssueChangeHolder());
 						
 						//trigger an edit on the issue
 						Map actionParams = EasyMap.build("issue", parentIssue.getGenericValue(), "issueObject", parentIssue, "remoteUser", this.getCaller(transientVars, args));
 						actionParams.put("comment", "Added "+field.getName()+" from sub-task "+issue.getKey());
 						actionParams.put("commentLevel", null);
 						ActionResult aResult = CoreFactory.getActionDispatcher().execute(ActionNames.ISSUE_UPDATE, actionParams);
 						if (aResult.getResult() != null && !aResult.getResult().equals("success"))
 							log.error(aResult.getResult());
 					}
 				}
 			}
 		} catch (Exception e) {
 			log.warn("Error while executing function : " + e, e);
 		} finally {
 			if (!indexingPreviouslyEnabled)
 				ImportUtils.setIndexIssues(false);
 		}
 	}
 }
