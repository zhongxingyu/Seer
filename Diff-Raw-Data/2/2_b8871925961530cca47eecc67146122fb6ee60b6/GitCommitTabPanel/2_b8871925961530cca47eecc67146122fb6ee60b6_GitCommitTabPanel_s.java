 /*
  * Created by IntelliJ IDEA.
  * User: Mike
  * Date: Sep 16, 2004
  * Time: 1:57:17 PM
  */
 package jiracommitviewer.issuetabpanels;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import jiracommitviewer.domain.GitCommitKey;
 import jiracommitviewer.domain.GitRepository;
 import jiracommitviewer.domain.LogEntry;
 import jiracommitviewer.index.GitCommitIndexer;
 import jiracommitviewer.index.exception.IndexException;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang3.Validate;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import webwork.action.ActionContext;
 
 import com.atlassian.crowd.embedded.api.User;
 import com.atlassian.jira.issue.Issue;
 import com.atlassian.jira.issue.tabpanels.GenericMessageAction;
 import com.atlassian.jira.plugin.issuetabpanel.AbstractIssueTabPanel;
 import com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelModuleDescriptor;
 import com.atlassian.jira.security.PermissionManager;
 import com.atlassian.jira.security.Permissions;
 import com.atlassian.jira.util.EasyList;
 import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
 import com.atlassian.plugin.webresource.WebResourceManager;
 
 /**
  * The "Git" tab panel for an issue in view.
  * 
  * @author mark
  */
 @SuppressWarnings("deprecation")
 public class GitCommitTabPanel extends AbstractIssueTabPanel {
 	
 	/** The number of commits to show in the tab initially. 100 should be good enough for most issues. */
     public static final int NUMBER_OF_REVISIONS = 100;
 	
     private final static Logger logger = LoggerFactory.getLogger(GitCommitTabPanel.class);
 
     @Autowired
     private PermissionManager permissionManager;
     @Autowired
     private WebResourceManager webResourceManager;
     @Autowired
     private VelocityRequestContextFactory velocityRequestContextFactory;
     @Autowired
     private GitCommitIndexer gitCommitIndexer;
 
     /**
      * Gets a list of actions for the tab panel with each action being a commit against this issue.
      */
     @SuppressWarnings({ "unchecked", "rawtypes" })
 	public List getActions(final Issue issue, final User remoteUser) {
     	Validate.notNull(issue, "issue must not be null");
     	Validate.notNull(remoteUser, "remoteUser must not be null");
     	
         webResourceManager.requireResource("jiracommitviewer.jiracommitviewer:git-resource-js");
 
         try {
             // SVN-392 - Temporary setting to descending by default until JRA-30220 is fixed
             final boolean sortAscending = false;
             final int pageSize = getPageSizeRequestParameter();
 
             final List<LogEntry<GitRepository, GitCommitKey>> logEntries = 
             		gitCommitIndexer.getAllLogEntriesByIssue(issue, getPageRequestParameter(), pageSize);
 
             if (logEntries.isEmpty()) {
                final GenericMessageAction action = new GenericMessageAction(getText("no.log.entries.message"));
                 return EasyList.build(action);
             } else {
                 List<GitCommitAction> actions = new ArrayList<GitCommitAction>();
                 for (final LogEntry<GitRepository, GitCommitKey> logEntry : logEntries) {
                 	actions.add(createGitRevisionAction(logEntry));
                 }
 
                 if (!sortAscending) {
                     Collections.reverse(actions);
                 }
 
                 /*
                  * Hack! If we have more than a page of actions, that means we should show the 'More' button.
                  */
                 if (!actions.isEmpty() && actions.size() > pageSize) {
                     /**
                      * ViewIssue will reverse the list of actions if the action sort order is descending, so we
                      * need to sublist based on the order.
                      */
                     actions = sortAscending ? actions.subList(0, pageSize) : actions.subList(1, actions.size());
 
                     final int lastActionIndex = sortAscending ? actions.size() - 1 : 0;
                     GitCommitAction lastAction = actions.get(lastActionIndex);
 
                     /**
                      * The last action should have specialized class name so that we can use it to tell us when
                      * to render the more button.
                      */
                     actions.set(lastActionIndex, createLastGitRevisionActionInPage(lastAction.getLogEntry()));
                 }
 
                 return actions;
             }
         } catch (final IndexException ie) {
             logger.error("There's a problem with the Git index.", ie);
         }
 
         return Collections.emptyList();
     }
 
     /**
      * Gets the page number being requested.
      * 
      * @return the requested page number where the first page is 0
      */
     private int getPageRequestParameter() {
         final HttpServletRequest req = ActionContext.getRequest();
 
         if (req != null) {
             final String pageIndexString = req.getParameter("pageIndex");
             return StringUtils.isBlank(pageIndexString) ? 0 : Integer.parseInt(pageIndexString);
         }
 
         return 0;
     }
 
     /**
      * Gets the page size being requested.
      * 
      * @return the page size or the default value of 100 if no page size requested
      */
     private int getPageSizeRequestParameter() {
         final HttpServletRequest req = ActionContext.getRequest();
 
         if (req != null) {
             final String pageIndexString = req.getParameter("pageSize");
             return StringUtils.isBlank(pageIndexString) ? NUMBER_OF_REVISIONS : Integer.parseInt(pageIndexString);
         }
 
         return NUMBER_OF_REVISIONS;
     }
 
     /**
      * Creates an action for the specified {@code logEntry.
      * 
      * @param logEntry the log entry to create the action for. Must not be {@code null}
      * @return the action. Never {@code null}
      */
     private GitCommitAction createGitRevisionAction(final LogEntry<GitRepository, GitCommitKey> logEntry) {
     	assert logEntry != null : "logEntry must not be null";
     	
         return new GitCommitAction(logEntry, descriptor);
     }
 
     /**
      * Creates the special action for showing the last commit on the panel.
      * 
      * @param logEntry the log entry of the last commit to show. Must not be {@code null}
      * @return the last action. Never {@code null}
      */
     private GitCommitAction createLastGitRevisionActionInPage(final LogEntry<GitRepository, GitCommitKey> logEntry) {
     	assert logEntry != null : "logEntry must not be null";
     	
         return new LastGitCommitActionInPage(logEntry, descriptor);
     }
 
     /**
      * Gets internationalised text for the specified {@code key}.
      * 
      * @param key the key whose label to get. Must not be {@code null}
      * @return the text. Never {@code null}
      */
     private String getText(final String key) {
     	assert key != null : "key must not be null";
     	
         return descriptor.getI18nBean().getText(key);
     }
 
     /**
      * Gets whether to show the panel at all. The panel will be display if the user has permissions to view version control.
      */
 	@Override
     public boolean showPanel(final Issue issue, final User remoteUser) {
     	Validate.notNull(issue, "issue must not be null");
     	Validate.notNull(remoteUser, "remoteUser must not be null");
     	
         return permissionManager.hasPermission(Permissions.VIEW_VERSION_CONTROL, issue, remoteUser);
     }
 
     /**
      * A class specifically created for its unique name so that the action view VMs know that
      * the action it is processing is the last one and render a 'More' button.
      */
     private class LastGitCommitActionInPage extends GitCommitAction {
         public LastGitCommitActionInPage(final LogEntry<GitRepository, GitCommitKey> logEntry, 
         		final IssueTabPanelModuleDescriptor descriptor) {
             super(logEntry, descriptor);
         }
     }
 }
