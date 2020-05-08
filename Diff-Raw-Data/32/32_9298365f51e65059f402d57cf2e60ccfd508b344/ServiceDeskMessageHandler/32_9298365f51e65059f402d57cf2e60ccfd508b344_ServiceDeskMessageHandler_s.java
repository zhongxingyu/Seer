 package com.bics.jira.mail.handler;
 
 import com.atlassian.crowd.embedded.api.User;
 import com.atlassian.jira.bc.issue.IssueService;
 import com.atlassian.jira.bc.project.component.ProjectComponent;
 import com.atlassian.jira.event.user.UserEventType;
 import com.atlassian.jira.exception.CreateException;
 import com.atlassian.jira.exception.PermissionException;
 import com.atlassian.jira.issue.Issue;
 import com.atlassian.jira.issue.IssueInputParameters;
 import com.atlassian.jira.issue.MutableIssue;
 import com.atlassian.jira.issue.status.Status;
 import com.atlassian.jira.issue.watchers.WatcherManager;
 import com.atlassian.jira.project.DefaultAssigneeException;
 import com.atlassian.jira.project.ProjectManager;
 import com.atlassian.jira.security.JiraAuthenticationContext;
 import com.atlassian.jira.service.util.handler.MessageHandler;
 import com.atlassian.jira.service.util.handler.MessageHandlerContext;
 import com.atlassian.jira.service.util.handler.MessageHandlerErrorCollector;
 import com.atlassian.jira.service.util.handler.MessageHandlerExecutionMonitor;
 import com.atlassian.jira.user.util.UserManager;
 import com.atlassian.jira.util.ErrorCollection;
 import com.atlassian.jira.web.util.AttachmentException;
 import com.atlassian.jira.workflow.JiraWorkflow;
 import com.atlassian.jira.workflow.WorkflowManager;
 import com.atlassian.mail.MailUtils;
 import com.bics.jira.mail.CommentExtractor;
 import com.bics.jira.mail.IssueBuilder;
 import com.bics.jira.mail.IssueLocator;
 import com.bics.jira.mail.ModelValidator;
 import com.bics.jira.mail.model.Attachment;
 import com.bics.jira.mail.model.HandlerModel;
 import com.bics.jira.mail.model.MessageAdapter;
 import com.bics.jira.mail.model.ServiceModel;
 import com.opensymphony.workflow.loader.ActionDescriptor;
 import com.opensymphony.workflow.loader.StepDescriptor;
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.internet.InternetAddress;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 /**
  * JavaDoc here
  *
  * @author Victor Polischuk
  * @since 03.02.13 12:29
  */
 public class ServiceDeskMessageHandler implements MessageHandler {
     private static final Logger LOG = Logger.getLogger(ServiceDeskMessageHandler.class);
 
     private final JiraAuthenticationContext jiraAuthenticationContext;
     private final ModelValidator modelValidator;
     private final IssueLocator issueLocator;
     private final IssueBuilder issueBuilder;
     private final IssueService issueService;
     private final ProjectManager projectManager;
     private final WorkflowManager workflowManager;
     private final UserManager userManager;
     private final CommentExtractor commentExtractor;
     private final WatcherManager watcherManager;
 
     private final HandlerModel model = new HandlerModel();
     private boolean valid;
 
     public ServiceDeskMessageHandler(JiraAuthenticationContext jiraAuthenticationContext, ModelValidator modelValidator, IssueLocator issueLocator, IssueBuilder issueBuilder, IssueService issueService, ProjectManager projectManager, WorkflowManager workflowManager, UserManager userManager, CommentExtractor commentExtractor, WatcherManager watcherManager) {
         this.jiraAuthenticationContext = jiraAuthenticationContext;
         this.modelValidator = modelValidator;
         this.issueLocator = issueLocator;
         this.issueBuilder = issueBuilder;
         this.issueService = issueService;
         this.projectManager = projectManager;
         this.workflowManager = workflowManager;
         this.userManager = userManager;
         this.commentExtractor = commentExtractor;
         this.watcherManager = watcherManager;
     }
 
     @Override
     public void init(Map<String, String> params, MessageHandlerErrorCollector monitor) {
         ServiceModel serviceModel = new ServiceModel().fromServiceParams(params);
 
         valid = modelValidator.populateHandlerModel(model, serviceModel, monitor);
 
         if (!valid) {
             monitor.warning("ServiceModel did not pass the validation. Emergency exit.");
         }
     }
 
     @Override
     public boolean handleMessage(Message message, MessageHandlerContext context) throws MessagingException {
         if (!valid) {
             return false;
         }
 
         MessageAdapter adapter = new MessageAdapter(message);
 
         InternetAddress[] addresses = adapter.getFrom();
 
         User author = null;
 
         for (InternetAddress address : addresses) {
             author = ensureUser(address, context);
 
             if (author == null) {
                 author = model.getReporterUser();
             }
         }
 
         MessageHandlerExecutionMonitor monitor = context.getMonitor();
 
         if (author == null) {
             monitor.error("Message sender(s) '" + StringUtils.join(MailUtils.getSenders(message), ",")
                     + "' do not have corresponding users in JIRA. Message will be ignored");
             return false;
         }
 
         jiraAuthenticationContext.setLoggedInUser(author);
 
         Issue issue = issueLocator.find(model, adapter, monitor);
 
         if (issue != null) {
             String body = commentExtractor.extractComment(model, adapter);
 
             ActionDescriptor action = lookupAction(issue);
 
             if (action == null) {
                 context.createComment(issue, author, body, false);
             } else {

                 IssueInputParameters params = issueService.newIssueInputParameters();
 
                 params.setComment(body);
                 params.setAssigneeId(getDefaultAssignee());
                 params.setApplyDefaultValuesWhenParameterNotProvided(true);
                 params.setSkipScreenCheck(true);
 
                 IssueService.TransitionValidationResult validationResult = issueService.validateTransition(author, issue.getId(), action.getId(), params);
 
                 if (!validationResult.isValid()) {
                     populareErrorCollection(validationResult.getErrorCollection(), monitor);
 
                     return false;
                 }
 
                 IssueService.IssueResult result = issueService.transition(author, validationResult);
 
                 if (!result.isValid()) {
                     populareErrorCollection(result.getErrorCollection(), monitor);
 
                     return false;
                 }
             }
         } else {
             MutableIssue newIssue = issueBuilder.build(model, adapter, monitor);
 
             newIssue.setReporter(author);
 
             try {
                 issue = context.createIssue(author, newIssue);
             } catch (CreateException e) {
                 monitor.error(e.getMessage(), e);
 
                 return false;
             }
         }
 
         try {
             for (Attachment attachment : adapter.getAttachments()) {
                 context.createAttachment(attachment.getStoredFile(), attachment.getFileName(), attachment.getContentType().toString(), author, issue);
             }
         } catch (AttachmentException e) {
             monitor.error(e.getMessage(), e);
 
             return false;
         }
 
         for (InternetAddress recipient : adapter.getAllRecipients()) {
             User user = ensureUser(recipient, context);
 
             if (user != null && !watcherManager.isWatching(user, issue)) {
                 watcherManager.startWatching(user, issue);
             }
         }
 
         return true;
     }
 
     private String getDefaultAssignee() {
         ProjectComponent component = model.getProjectComponent();
         Collection<ProjectComponent> components = new ArrayList<ProjectComponent>(1);
 
         if (component != null) {
             components.add(component);
         }
 
         try {
             return projectManager.getDefaultAssignee(model.getProject(), components).getName();
         } catch (DefaultAssigneeException e) {
             return null;
         }
     }
 
     private User ensureUser(InternetAddress address, MessageHandlerContext context) {
         User user = userManager.getUser(address.getAddress());
 
         if (user == null) {
             try {
                 user = context.createUser(address.getAddress(), address.getAddress(), address.getAddress(), address.getPersonal(), UserEventType.USER_CREATED);
             } catch (PermissionException e) {
                 LOG.error("Permission problem: ", e);
             } catch (CreateException e) {
                 LOG.error("Creation problem: ", e);
             }
         }
 
         return user;
     }
 
     private ActionDescriptor lookupAction(Issue issue) {
         int[] transitions = model.getTransitions();
 
         if (transitions == null || transitions.length == 0) {
             return null;
         }
 
         Status status = issue.getStatusObject();
 
         JiraWorkflow workflow = workflowManager.getWorkflow(issue);
 
         if (workflow == null) {
             LOG.warn("The issue " + issue.getKey() + " does not have assigned workflow.");
             return null;
         }
 
         StepDescriptor step = workflow.getLinkedStep(status);
 
         if (step == null) {
             return null;
         }
 
         List<ActionDescriptor> actions = step.getActions();
 
         if (actions == null) {
             return null;
         }
 
         for (int code : model.getTransitions()) {
             for (ActionDescriptor action : actions) {
                 if (action.getId() == code) {
                     return action;
                 }
             }
         }
 
         return null;
     }
 
     private static void populareErrorCollection(ErrorCollection errorCollection, MessageHandlerExecutionMonitor monitor) {
         ErrorCollection errors = errorCollection;
 
         for (String error : errors.getErrorMessages()) {
             monitor.error(error);
         }
     }
 }
