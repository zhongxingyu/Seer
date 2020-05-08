 package com.bics.jira.mail.helper;
 
 import com.atlassian.crowd.embedded.api.User;
 import com.atlassian.jira.bc.ServiceResult;
 import com.atlassian.jira.bc.issue.IssueService;
 import com.atlassian.jira.bc.issue.search.SearchService;
 import com.atlassian.jira.bc.project.component.ProjectComponent;
 import com.atlassian.jira.config.ConstantsManager;
 import com.atlassian.jira.exception.CreateException;
 import com.atlassian.jira.exception.PermissionException;
 import com.atlassian.jira.issue.AttachmentManager;
 import com.atlassian.jira.issue.CustomFieldManager;
 import com.atlassian.jira.issue.Issue;
 import com.atlassian.jira.issue.IssueFactory;
 import com.atlassian.jira.issue.IssueFieldConstants;
 import com.atlassian.jira.issue.IssueInputParameters;
 import com.atlassian.jira.issue.IssueManager;
 import com.atlassian.jira.issue.MutableIssue;
 import com.atlassian.jira.issue.comments.CommentManager;
 import com.atlassian.jira.issue.fields.CustomField;
 import com.atlassian.jira.issue.issuetype.IssueType;
 import com.atlassian.jira.issue.priority.Priority;
 import com.atlassian.jira.issue.search.SearchException;
 import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
 import com.atlassian.jira.issue.status.Status;
 import com.atlassian.jira.issue.watchers.WatcherManager;
 import com.atlassian.jira.jql.builder.JqlQueryBuilder;
 import com.atlassian.jira.project.Project;
 import com.atlassian.jira.service.util.handler.MessageHandlerErrorCollector;
 import com.atlassian.jira.util.ErrorCollection;
 import com.atlassian.jira.web.FieldVisibilityManager;
 import com.atlassian.jira.web.bean.PagerFilter;
 import com.atlassian.jira.web.util.AttachmentException;
 import com.atlassian.jira.workflow.JiraWorkflow;
 import com.atlassian.jira.workflow.WorkflowManager;
 import com.atlassian.query.Query;
 import com.bics.jira.mail.IssueHelper;
 import com.bics.jira.mail.MailHelper;
 import com.bics.jira.mail.UserHelper;
 import com.bics.jira.mail.model.Attachment;
 import com.bics.jira.mail.model.HandlerModel;
 import com.bics.jira.mail.model.MessageAdapter;
 import com.opensymphony.workflow.loader.ActionDescriptor;
 import com.opensymphony.workflow.loader.StepDescriptor;
 
 import javax.mail.MessagingException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.regex.Pattern;
 
 /**
  * JavaDoc here
  *
  * @author Victor Polischuk
  * @since 04.02.13 21:38
  */
 public class IssueHelperImpl implements IssueHelper {
    private static final Pattern PREPARER = Pattern.compile("\\W+");
     private static final long RESOLUTION_DELTA = 1000L * 60 * 60 * 24 * 30;
 
     private final IssueFactory issueFactory;
     private final IssueService issueService;
     private final IssueManager issueManager;
     private final AttachmentManager attachmentManager;
     private final WatcherManager watcherManager;
     private final WorkflowManager workflowManager;
     private final SearchService searchService;
     private final CommentManager commentManager;
     private final IssueSecurityLevelManager issueSecurityLevelManager;
     private final CustomFieldManager customFieldManager;
     private final ConstantsManager constantsManager;
     private final UserHelper userHelper;
     private final MailHelper mailHelper;
     private final FieldVisibilityManager fieldVisibilityManager;
 
     public IssueHelperImpl(IssueFactory issueFactory, IssueService issueService, IssueManager issueManager, AttachmentManager attachmentManager, WatcherManager watcherManager, WorkflowManager workflowManager, SearchService searchService, CommentManager commentManager, IssueSecurityLevelManager issueSecurityLevelManager, CustomFieldManager customFieldManager, ConstantsManager constantsManager, UserHelper userHelper, MailHelper mailHelper, FieldVisibilityManager fieldVisibilityManager) {
         this.issueFactory = issueFactory;
         this.issueService = issueService;
         this.issueManager = issueManager;
         this.attachmentManager = attachmentManager;
         this.watcherManager = watcherManager;
         this.workflowManager = workflowManager;
         this.searchService = searchService;
         this.commentManager = commentManager;
         this.issueSecurityLevelManager = issueSecurityLevelManager;
         this.customFieldManager = customFieldManager;
         this.constantsManager = constantsManager;
         this.userHelper = userHelper;
         this.mailHelper = mailHelper;
         this.fieldVisibilityManager = fieldVisibilityManager;
     }
 
     @Override
     public Issue process(User author, HandlerModel model, MessageAdapter message, MessageHandlerErrorCollector monitor) throws MessagingException, CreateException, AttachmentException, PermissionException {
         Project project = model.getProject();
 
         MutableIssue issue = find(author, project, message.getSubject(), monitor);
         Collection<User> users = userHelper.ensure(message.getAllRecipients(), model.isCreateUsers(), model.isNotifyUsers(), monitor);
 
         User assignee = getAssignee(users, model);
 
         if (issue == null && !userHelper.canCreateIssue(author, project)) {
             throw new PermissionException("User " + author.getName() + " cannot create issues in the project " + project.getKey() + ".");
         }
 
         if (issue != null && !userHelper.canCommentIssue(author, project)) {
             throw new PermissionException("User " + author.getName() + " cannot comment issues in the project " + project.getKey() + ".");
         }
 
         if (issue == null) {
             issue = create(author, assignee, model, message, monitor);
         } else {
             comment(author, assignee, issue, model, message, monitor);
         }
 
         if (userHelper.canCreateAttachment(author, project)) {
             attach(author, issue, message.getAttachments());
         } else {
             monitor.warning("User " + author.getName() + " cannot create attachments in the project " + project.getKey() + ". Ignoring.");
         }
 
         if (userHelper.canManageWatchList(author, project)) {
             watch(issue, users);
         } else {
             monitor.warning("User " + author.getName() + " cannot manage watch list in the project " + project.getKey() + ". Ignoring.");
         }
 
         return issue;
     }
 
     protected MutableIssue find(User author, Project project, String subject, MessageHandlerErrorCollector monitor) {
         subject = prepareSummary(subject);
 
         try {
             Query unresolvedQuery = JqlQueryBuilder.newClauseBuilder()
                     .project(project.getId()).and().summary(subject).and()
                     .unresolved().buildQuery();
 
             Issue issue = findIssue(author, unresolvedQuery, subject);
 
             if (issue == null) {
                 Query recentlyResolvedQuery = JqlQueryBuilder.newClauseBuilder()
                         .project(project.getId()).and().summary(subject).and()
                         .resolutionDateBetween(new Date(System.currentTimeMillis() - RESOLUTION_DELTA), new Date())
                         .buildQuery();
 
                 issue = findIssue(author, recentlyResolvedQuery, subject);
             }
 
             if (issue != null) {
                 return issueManager.getIssueObject(issue.getId());
             }
         } catch (SearchException e) {
             monitor.error("Cannot search for an issue.", e);
         }
 
         return null;
     }
 
     protected MutableIssue create(User author, User assignee, HandlerModel model, MessageAdapter message, MessageHandlerErrorCollector monitor) throws CreateException, MessagingException {
         monitor.info("Creating new issue for an author: " + author.getName());
 
         Project project = model.getProject();
         ProjectComponent component = model.getProjectComponent();
         IssueType issueType = model.getIssueType();
 
         Long levelId = issueSecurityLevelManager.getDefaultSecurityLevel(project);
         MutableIssue issue = issueFactory.getIssue();
 
         issue.setProjectObject(project);
         issue.setSummary(message.getSubject());
         issue.setIssueTypeId(issueType.getId());
         issue.setReporter(author);
 
         if (assignee != null) {
             issue.setAssignee(assignee);
         }
 
         if (levelId != null) {
             issue.setSecurityLevelId(levelId);
         }
 
         if (component != null) {
             issue.setComponentObjects(Collections.singleton(component));
         }
 
         if (isVisibleField(project, IssueFieldConstants.PRIORITY, issueType)) {
             setPriority(issue, message);
         }
 
         if (isVisibleField(project, IssueFieldConstants.DESCRIPTION, issueType)) {
             String body = mailHelper.extractBody(model, message);
 
             issue.setDescription(body);
         }
 
         for (CustomField customField : customFieldManager.getCustomFieldObjects(issue)) {
             issue.setCustomFieldValue(customField, customField.getDefaultValue(issue));
         }
 
         Issue issueObject = issueManager.createIssueObject(author, issue);
 
         monitor.info("New issue " + issueObject.getKey() + " has been successfully created");
 
         return issueManager.getIssueObject(issueObject.getId());
     }
 
     protected void comment(User author, User assignee, Issue issue, HandlerModel model, MessageAdapter message, MessageHandlerErrorCollector monitor) throws MessagingException, CreateException {
         String body = mailHelper.extractComment(model, message);
 
         ActionDescriptor action = lookupAction(issue, model.getTransitions(), monitor);
 
         if (action == null) {
             commentManager.create(issue, author.getName(), body, true);
 
             return;
         }
 
         IssueInputParameters params = issueService.newIssueInputParameters();
 
         params.setComment(body);
         params.setAssigneeId(assignee == null ? issue.getAssigneeId() : assignee.getName());
         params.setApplyDefaultValuesWhenParameterNotProvided(true);
         params.setSkipScreenCheck(true);
 
         IssueService.TransitionValidationResult validationResult = issueService.validateTransition(author, issue.getId(), action.getId(), params);
 
         verifyResult(validationResult, monitor);
 
         IssueService.IssueResult result = issueService.transition(author, validationResult);
 
         verifyResult(result, monitor);
     }
 
     protected void attach(User author, MutableIssue issue, Collection<Attachment> attachments) throws AttachmentException {
         for (Attachment attachment : attachments) {
             attachmentManager.createAttachment(attachment.getStoredFile(), attachment.getFileName(), attachment.getContentType().toString(), author, issue);
         }
     }
 
     protected void watch(Issue issue, Collection<User> users) {
         for (User user : users) {
             if (!watcherManager.isWatching(user, issue)) {
                 watcherManager.startWatching(user, issue);
             }
         }
     }
 
     private Issue findIssue(User user, Query query, String subjectMatch) throws SearchException {
         List<Issue> issues = searchService.search(user, query, PagerFilter.getUnlimitedFilter()).getIssues();
 
         if (issues == null || issues.isEmpty()) {
             return null;
         }
 
         for (Issue issue : issues) {
             String summary = prepareSummary(issue.getSummary());
 
             if (subjectMatch.equals(summary)) {
                 return issue;
             }
         }
 
         return null;
     }
 
     private boolean isVisibleField(Project project, String fieldName, IssueType issueType) {
         return !fieldVisibilityManager.isFieldHiddenInAllSchemes(project.getId(), fieldName, Collections.singletonList(issueType.getId()));
     }
 
     private User getAssignee(Collection<User> users, HandlerModel model) {
         if (users != null && !users.isEmpty() && model.isCcAssignee()) {
             for (User user : users) {
                 if (userHelper.canAssignTo(user, model.getProject())) {
                     return user;
                 }
             }
         }
 
         return userHelper.getDefaultAssignee(model.getProject(), model.getProjectComponent());
     }
 
     private void setPriority(MutableIssue issue, MessageAdapter message) {
         List<Priority> priorities = new ArrayList<Priority>(constantsManager.getPriorityObjects());
 
         Collections.sort(priorities);
 
         int index = message.getPriority(priorities.size());
 
         issue.setPriorityObject(priorities.get(index));
     }
 
     private ActionDescriptor lookupAction(Issue issue, int[] transitions, MessageHandlerErrorCollector monitor) {
         if (transitions == null || transitions.length == 0) {
             return null;
         }
 
         Status status = issue.getStatusObject();
 
         JiraWorkflow workflow = workflowManager.getWorkflow(issue);
 
         if (workflow == null) {
             monitor.warning("The issue " + issue.getKey() + " does not have assigned workflow.");
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
 
         for (int code : transitions) {
             for (ActionDescriptor action : actions) {
                 if (action.getId() == code) {
                     return action;
                 }
             }
         }
 
         return null;
     }
 
     private static void verifyResult(ServiceResult result, MessageHandlerErrorCollector monitor) throws CreateException {
         if (!result.isValid()) {
             ErrorCollection errors = result.getErrorCollection();
 
             for (String error : errors.getErrorMessages()) {
                 monitor.error(error);
             }
 
             throw new CreateException("Cannot transit issue from one state to another");
         }
     }
 
     private static String prepareSummary(String subject) {
        return PREPARER.matcher(subject).replaceAll(" ").trim();
     }
 }
