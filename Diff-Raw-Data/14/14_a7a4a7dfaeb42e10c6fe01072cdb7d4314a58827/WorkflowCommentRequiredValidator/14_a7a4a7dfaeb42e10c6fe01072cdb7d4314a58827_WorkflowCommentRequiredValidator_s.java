 /**
  * 
  */
 package com.innovalog.jmwe.plugins.validators;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory;
 import com.atlassian.jira.plugin.workflow.WorkflowPluginValidatorFactory;
 import com.atlassian.jira.security.groups.GroupManager;
 import com.innovalog.googlecode.jsu.util.WorkflowUtils;
 import com.opensymphony.workflow.loader.AbstractDescriptor;
 import com.opensymphony.workflow.loader.ValidatorDescriptor;
 
 /**
  * @author fischerd
  *
  */
 public class WorkflowCommentRequiredValidator extends
 		AbstractWorkflowPluginFactory implements WorkflowPluginValidatorFactory {
     private final WorkflowUtils workflowUtils;
     private final GroupManager groupManager;
 
     public WorkflowCommentRequiredValidator(WorkflowUtils workflowUtils, GroupManager groupManager) {
         this.workflowUtils = workflowUtils;
         this.groupManager = groupManager;
     }
 
     /* (non-Javadoc)
       * @see com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory#getVelocityParamsForEdit(java.util.Map, com.opensymphony.workflow.loader.AbstractDescriptor)
       */
 	@Override
 	protected void getVelocityParamsForEdit(Map velocityParams,
 			AbstractDescriptor descriptor) {
 		getVelocityParamsForInput(velocityParams);
 		getVelocityParamsForView(velocityParams, descriptor);
 
 		ValidatorDescriptor validatorDescriptor = (ValidatorDescriptor) descriptor;
 		Map args = validatorDescriptor.getArgs();
 		
 		velocityParams.remove("val-groupsList");
 		
 		String strGroupsSelected = (String)args.get("hidGroupsList");
 		Collection groupsSelected = workflowUtils.getGroups(strGroupsSelected, WorkflowUtils.SPLITTER);
 		
 		Collection groups = groupManager.getAllGroups();
 		groups.removeAll(groupsSelected);
 		
 		velocityParams.put("val-hidGroupsList", workflowUtils.getStringGroup(groupsSelected, WorkflowUtils.SPLITTER));
 		velocityParams.put("val-groupsList", Collections.unmodifiableCollection(groups));
 }
 
 	/* (non-Javadoc)
 	 * @see com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory#getVelocityParamsForInput(java.util.Map)
 	 */
 	@Override
 	protected void getVelocityParamsForInput(Map velocityParams) {
 		Collection groups = groupManager.getAllGroups();
 		velocityParams.put("val-groupsList", Collections.unmodifiableCollection(groups));
 		velocityParams.put("val-splitter", WorkflowUtils.SPLITTER);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.atlassian.jira.plugin.workflow.AbstractWorkflowPluginFactory#getVelocityParamsForView(java.util.Map, com.opensymphony.workflow.loader.AbstractDescriptor)
 	 */
 	@Override
 	protected void getVelocityParamsForView(Map velocityParams,
 			AbstractDescriptor descriptor) {
 		ValidatorDescriptor validatorDescriptor = (ValidatorDescriptor) descriptor;
 		Map args = validatorDescriptor.getArgs();
 		velocityParams.put("errorMessage", args.get("errorMessage"));
 
 		String strGroupsSelected = (String)args.get("hidGroupsList");
 		Collection groupsSelected = workflowUtils.getGroups(strGroupsSelected, WorkflowUtils.SPLITTER);
 		
 		velocityParams.put("val-groupsListSelected", Collections.unmodifiableCollection(groupsSelected));
 }
 
 	/* (non-Javadoc)
 	 * @see com.atlassian.jira.plugin.workflow.WorkflowPluginFactory#getDescriptorParams(java.util.Map)
 	 */
 	public Map getDescriptorParams(Map formParams) {
 		Map<String, String> params = new HashMap<String, String>();
 		String strErrorMessage = extractSingleParam(formParams, "errorMessage");
 		
		if ("".equals(strErrorMessage)) {
			throw new IllegalArgumentException("An error message must be provided.");
		}
		
 		params.put("errorMessage", strErrorMessage);
 		
 		try{
 			String strGroupsSelected = extractSingleParam(formParams, "hidGroupsList");
 			params.put("hidGroupsList", strGroupsSelected);
 			
 		}catch(IllegalArgumentException iae){
 			// Aggregate so that Transitions can be added.
 		}
 		
 		return params;
 	}
 
 }
