 package com.innovalog.jmwe.plugins.functions;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import com.atlassian.jira.ComponentManager;
 import com.atlassian.jira.issue.Issue;
 import com.atlassian.jira.issue.MutableIssue;
 import com.atlassian.jira.issue.util.IssueChangeHolder;
 import com.atlassian.jira.util.ErrorCollection;
 import com.atlassian.jira.util.ImportUtils;
 import com.atlassian.jira.util.JiraUtils;
 import com.atlassian.jira.workflow.JiraWorkflow;
 import com.atlassian.jira.workflow.WorkflowManager;
 import com.atlassian.jira.workflow.WorkflowTransitionUtil;
 import com.atlassian.jira.workflow.WorkflowTransitionUtilImpl;
 import com.opensymphony.module.propertyset.PropertySet;
 import com.opensymphony.workflow.WorkflowException;
 import com.opensymphony.workflow.loader.ActionDescriptor;
 
 public class TransitionParentIssueFunction extends AbstractPreserveChangesPostFunction {
 	private Logger log = Logger.getLogger(TransitionParentIssueFunction.class);
 	public static final String TRANSITION = "transition";
 
 	private static JiraWorkflow getWorkflow(Issue issue) {
 		// get current workflow
 		WorkflowManager wm = ComponentManager.getInstance().getWorkflowManager();
 		return wm.getWorkflow(issue);
 	}
 
 	private static ActionDescriptor transitionFromName(Issue issue,String name) {
 		Collection<ActionDescriptor> actions = getWorkflow(issue).getAllActions();
 		for (ActionDescriptor ad : actions)
 			if (ad.getName().equals(name))
 				return ad;
 		return null;
 	}
 
 	private void printAnyErrors(Issue subtask, ErrorCollection errorCollection) {
 		if (errorCollection.hasAnyErrors()) {
			log.warn("Field validation error auto-transitioning parent issue " + subtask.getKey() + ":");
 			Iterator iter = errorCollection.getErrorMessages().iterator();
 			while (iter.hasNext()) {
 				String errMsg = (String) iter.next();
 				log.warn("\t" + errMsg);
 			}
 			iter = errorCollection.getErrors().keySet().iterator();
 			while (iter.hasNext()) {
 				String fieldName = (String) iter.next();
 				log.warn("\tField " + fieldName + ": " + errorCollection.getErrors().get(fieldName));
 			}
 		}
 
 	}
 
 	public void executeFunction(Map transientVars, Map args, PropertySet ps, IssueChangeHolder holder) throws WorkflowException {
 		MutableIssue issue = getIssue(transientVars);
 		String transitionName = (String) args.get(TRANSITION);
 		boolean indexingPreviouslyEnabled = ImportUtils.isIndexIssues();
 
 		try {
 			MutableIssue parentIssue = (MutableIssue) issue.getParentObject();
 			if (parentIssue != null) {
 				ActionDescriptor transition = transitionFromName(parentIssue,transitionName);
 				if (transition == null) {
 					log.warn("Error while executing function : transition [" + transitionName + "] not found");
 					return;
 				}
 
 				if (!indexingPreviouslyEnabled)
 					ImportUtils.setIndexIssues(true);
 
 				WorkflowTransitionUtil workflowTransitionUtil = (WorkflowTransitionUtil) JiraUtils.loadComponent(WorkflowTransitionUtilImpl.class);
 				workflowTransitionUtil.setIssue(parentIssue);
 				workflowTransitionUtil.setUsername(this.getCaller(transientVars, args).getName());
 				workflowTransitionUtil.setAction(transition.getId());
 
 				// validate and transition issue
 				ErrorCollection errorCollection = workflowTransitionUtil.validate();
 				printAnyErrors(parentIssue, errorCollection);
 
 				if (!errorCollection.hasAnyErrors())
 				{
 					workflowTransitionUtil.progress();
 					ComponentManager.getInstance().getIndexManager().reIndex(parentIssue);
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
