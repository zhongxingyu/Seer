 /**
  * 
  */
 package com.innovalog.jmwe.plugins.validators;
 
 import java.util.Collection;
 import java.util.Iterator;
 
 import com.atlassian.crowd.embedded.api.Group;
 import com.atlassian.crowd.embedded.api.User;
 import com.atlassian.jira.ManagerFactory;
 import com.atlassian.jira.issue.IssueFieldConstants;
 import com.atlassian.jira.issue.fields.Field;
 import com.atlassian.jira.security.groups.GroupManager;
 import com.atlassian.jira.user.util.UserManager;
 import com.atlassian.jira.workflow.WorkflowTransitionUtil;
 import com.innovalog.googlecode.jsu.annotation.Argument;
 import com.innovalog.googlecode.jsu.annotation.TransientVariable;
 import com.innovalog.googlecode.jsu.util.FieldCollectionsUtils;
 import com.innovalog.googlecode.jsu.util.WorkflowUtils;
 import com.opensymphony.util.TextUtils;
 import com.opensymphony.workflow.InvalidInputException;
 import com.opensymphony.workflow.WorkflowContext;
 import com.opensymphony.workflow.WorkflowException;
 
 /**
  * @author fischerd
  *
  */
 public class CommentRequiredValidator extends GenericValidator {
 
 	@TransientVariable(WorkflowTransitionUtil.FIELD_COMMENT)
 	private String strComment;
 	
 	@Argument("errorMessage")
 	private String errorMsg;
 	
 	@Argument("hidGroupsList")
 	private String strGroupsSelected;
 	
 	@TransientVariable("context")
 	private WorkflowContext context;
 
     private final UserManager userManager;
     private final GroupManager groupManager;
 
     public CommentRequiredValidator(WorkflowUtils workflowUtils, FieldCollectionsUtils fieldCollectionsUtils, UserManager userManager, GroupManager groupManager) {
         super(workflowUtils, fieldCollectionsUtils);
         this.userManager = userManager;
         this.groupManager = groupManager;
     }
 
     /* (non-Javadoc)
       * @see com.innovalog.jmwe.plugins.validators.GenericValidator#validate()
       */
 	@Override
 	protected void validate() throws InvalidInputException, WorkflowException {
 		//JMWE-10
 		//check whether we were invoked through the SOAP API call progressWorkflowAction.
 		//If so, skip validation because comments cannot be passed this way
 		//NOTA: this is an ugly hack but for now it'll have to do!
 		try
 		{
 			throw new Exception();
 		}
 		catch(Exception e)
 		{
 			StackTraceElement[] stack = e.getStackTrace();
 			for (StackTraceElement entry : stack)
 			{
 				if (entry.getClassName().equals("com.atlassian.jira.rpc.soap.JiraSoapServiceImpl") && entry.getMethodName().equals("progressWorkflowAction"))
 					return;
 			}
 		}
         if (!TextUtils.stringSet(strComment))
         {
         	//bypass validator for certain users
             // Obtain the current user.
             User userLogged = userManager.getUser(context.getCaller());
 
             // If there aren't groups selected, hidGroupsList is equal to "".
             // And groupsSelected will be an empty collection.
             Collection groupsSelected = workflowUtils.getGroups(strGroupsSelected, WorkflowUtils.SPLITTER);
 
             Iterator it = groupsSelected.iterator();
             while(it.hasNext()){
                 if(groupManager.isUserInGroup(userLogged,(Group) it.next())) {
                     return;
                 }
             }
 
         	//find Comment field
         	Field field = ManagerFactory.getFieldManager().getField(IssueFieldConstants.COMMENT);
            if ("".equals(errorMsg))
                this.setExceptionMessage(field, "You must provide a Comment", "A Comment is required but cannot be input. Please report this error to your administrator.");
            else
        	    this.setExceptionMessage(field, errorMsg, "A Comment is required but cannot be input. Please report this error to your administrator.");
         }
 	}
 
 }
