 package es.testingserver.atlassian.epicstats;
 
 import com.atlassian.crowd.embedded.api.User;
 import com.atlassian.jira.bc.issue.IssueService;
 import com.atlassian.jira.bc.issue.search.SearchService;
 import com.atlassian.jira.bc.project.ProjectService;
 import com.atlassian.jira.component.ComponentAccessor;
 import com.atlassian.jira.issue.CustomFieldManager;
 import com.atlassian.jira.issue.Issue;
 import com.atlassian.jira.issue.fields.CustomField;
 import com.atlassian.jira.issue.label.Label;
 import com.atlassian.jira.issue.search.SearchException;
 import com.atlassian.jira.jql.builder.JqlClauseBuilder;
 import com.atlassian.jira.jql.builder.JqlQueryBuilder;
 import com.atlassian.jira.web.bean.PagerFilter;
 import com.atlassian.sal.api.user.UserManager;
 import com.atlassian.templaterenderer.TemplateRenderer;
 import com.google.common.collect.Maps;
 import es.testingserver.atlassian.entities.Epic;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 public class EpicStats extends HttpServlet{
     private static final Logger log = LoggerFactory.getLogger(EpicStats.class);
     private IssueService issueService;
     private ProjectService projectService;
     private SearchService searchService;
     private UserManager userManager;
     private TemplateRenderer templateRenderer;
     private com.atlassian.jira.user.util.UserManager jiraUserManager;
     private JqlClauseBuilder jqlClauseBuilder = null;
     private CustomField epicField = null;
     private CustomField storyPoints = null;
     private double globalTotalStoryPoints = 0;
     private double globalBurnedStoryPoints = 0;
     private String filterLabel = null;
     private String project = null;
     private String epicIssueType = null;
     private String storyIssueType = null;
     private String storyPointsField = null;
     private String epicRelatedField = null;
     private String doneStatus = null;
     private String filtered = null;
     private static final String LIST_BROWSER_TEMPLATE = "/templates/list.vm";
 
     public EpicStats(IssueService issueService, ProjectService projectService,
                      SearchService searchService, UserManager userManager,
                      com.atlassian.jira.user.util.UserManager jiraUserManager,
                      TemplateRenderer templateRenderer) {
         this.issueService = issueService;
         this.projectService = projectService;
         this.searchService = searchService;
         this.userManager = userManager;
         this.templateRenderer = templateRenderer;
         this.jiraUserManager = jiraUserManager;
         this.jqlClauseBuilder = null;
     }
 
 
     private User getCurrentUser(HttpServletRequest req)
 	{
 
         return jiraUserManager.getUser(userManager.getRemoteUsername(req));
     }
 
     private List<Issue> getEpics(HttpServletRequest req) {
         // User is required to carry out a search
         User user = getCurrentUser(req);
 
         // The search interface requires JQL clause... so let's build one
         jqlClauseBuilder = JqlQueryBuilder.newClauseBuilder();
 
         com.atlassian.query.Query query = null;
         jqlClauseBuilder = jqlClauseBuilder.project( this.project ).
                 and().issueTypeIsStandard().
                 and().issueType().in( this.epicIssueType );
 
        if ( this.filtered != null )
         {
             // JQL Filter Clause:
             jqlClauseBuilder = jqlClauseBuilder.and().labels(this.filterLabel);
         }
 
         query = jqlClauseBuilder.buildQuery();
 
         // A page filter is used to provide pagination. Let's use an unlimited filter to
         // to bypass pagination.
         PagerFilter pagerFilter = PagerFilter.getUnlimitedFilter();
         com.atlassian.jira.issue.search.SearchResults searchResults = null;
 
         try
 		{
             // Perform search results
             searchResults = searchService.search(user, query, pagerFilter);
         }
 		catch (SearchException e)
 		{
             e.printStackTrace();
         }
 
         return searchResults.getIssues();
     }
 
     private List<Epic> processEpics( List<Issue> issues, HttpServletRequest req )
     {
         User user = getCurrentUser(req);
         List<Epic> processedIssues = new ArrayList<Epic>();
         this.globalTotalStoryPoints = 0;
         this.globalBurnedStoryPoints = 0;
         for( Issue item : issues )
         {
             Object epicValue = item.getCustomFieldValue( epicField );
             if ( epicValue != null )
             {
                 List<Label> epics = new ArrayList<Label>(
                         (Collection<Label>) epicValue
                 );
 
                 String epicLabel = epics.get(0).getLabel();
 
                 // JQL Clause:
                 jqlClauseBuilder = JqlQueryBuilder.newClauseBuilder();
 
 
                 // JQL Clause:
                 com.atlassian.query.Query query = jqlClauseBuilder.project( this.project ).
                         and().issueTypeIsStandard().
                         and().issueType().in(this.storyIssueType).
                         and().customField(epicField.getIdAsLong()).eq(epicLabel).
                         buildQuery();
 
                 PagerFilter pagerFilter = PagerFilter.getUnlimitedFilter();
                 com.atlassian.jira.issue.search.SearchResults searchResults = null;
 
                 try
                 {
                     // Perform search results
                     searchResults = searchService.search(user, query, pagerFilter);
                 }
                 catch (SearchException e)
                 {
                     e.printStackTrace();
                 }
 
                 double totalStoryPoints = 0;
                 double burnedStoryPoints = 0;
 
                 List<Issue> stories = searchResults.getIssues();
                 for ( Issue story : stories )
                 {
                     Double spValue = (Double) story.getCustomFieldValue( storyPoints );
                     if ( spValue != null )
                     {
                         totalStoryPoints += spValue;
                         if ( story.getStatusObject().getName().equals( this.doneStatus ) )
                         {
                             burnedStoryPoints += spValue;
                         }
                     }
                 }
 
                 Epic temp = new Epic();
                 temp.setKey(item.getKey());
                 temp.setSummary(item.getSummary());
                 temp.setTotalStoryPoints(totalStoryPoints);
                 temp.setBurnedStoryPoints(burnedStoryPoints);
                 processedIssues.add( temp );
 
                 // Sum all the epics:
                 globalTotalStoryPoints += totalStoryPoints;
                 globalBurnedStoryPoints += burnedStoryPoints;
             }
         }
 
         return processedIssues;
     }
 
     @Override
     protected void doGet(
             HttpServletRequest req,
             HttpServletResponse resp
     ) throws ServletException, IOException
     {
 
         // Configuration read from Admin Plugin Section in Jira:
         this.readPluginConfiguration( req );
 
         // Get Epics Info:
         List<Issue> issues = getEpics(req);
         List<Epic> processedEpics = this.processEpics( issues, req );
 
         // Set template context:
         Map<String, Object> context = Maps.newHashMap();
         context.put( "cfEpic", epicField.getIdAsLong() );
         context.put( "issues", processedEpics );
         context.put( "totalStoryPoints", globalTotalStoryPoints );
         context.put( "burnedStoryPoints", globalBurnedStoryPoints );
         resp.setContentType("text/html;charset=utf-8");
 
         // Pass in the list of issues as the context
         templateRenderer.render(
                 LIST_BROWSER_TEMPLATE,
                 context,
                 resp.getWriter()
         );
     }
 
     private void readPluginConfiguration( HttpServletRequest req )
     {
         CustomFieldManager customFieldManager =
                 ComponentAccessor.getCustomFieldManager();
 
         this.filtered = req.getParameter("filtered");
         this.project = "Web";
         this.epicIssueType = "Epic";
         this.storyIssueType = "Story";
         this.storyPointsField = "Story Points";
         this.epicRelatedField = "Epic/Theme";
         this.doneStatus = "Closed";
         this.filterLabel = "Roadmap";
 
         this.epicField =
                 customFieldManager.getCustomFieldObjectByName( this.epicRelatedField );
         this.storyPoints =
                 customFieldManager.getCustomFieldObjectByName( this.storyPointsField );
     }
 }
