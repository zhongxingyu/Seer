 package com.atlassian.sal.jira.search;
 
 import java.io.IOException;
 import java.util.*;
 
 import javax.servlet.http.HttpUtils;
 
 import org.apache.log4j.Logger;
 import org.apache.commons.lang.StringUtils;
 
 import com.atlassian.jira.issue.Issue;
 import com.atlassian.jira.issue.IssueManager;
 import com.atlassian.jira.issue.search.*;
 import com.atlassian.jira.issue.search.managers.IssueSearcherManager;
 import com.atlassian.jira.issue.search.searchers.IssueSearcher;
 import com.atlassian.jira.issue.search.util.QueryCreator;
 import com.atlassian.jira.issue.transport.FieldValuesHolder;
 import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
 import com.atlassian.jira.issue.transport.impl.IssueNavigatorActionParams;
 import com.atlassian.jira.project.Project;
 import com.atlassian.jira.project.ProjectManager;
 import com.atlassian.jira.util.ErrorCollection;
 import com.atlassian.jira.util.SimpleErrorCollection;
 import com.atlassian.jira.util.JiraKeyUtils;
 import com.atlassian.jira.util.searchers.ThreadLocalSearcherCache;
 import com.atlassian.jira.web.bean.I18nBean;
 import com.atlassian.jira.web.bean.PagerFilter;
 import com.atlassian.jira.exception.DataAccessException;
 import com.atlassian.sal.api.ApplicationProperties;
 import com.atlassian.sal.api.component.ComponentLocator;
 import com.atlassian.sal.api.message.Message;
 import com.atlassian.sal.api.search.SearchMatch;
 import com.atlassian.sal.api.search.SearchResults;
 import com.atlassian.sal.api.search.parameter.SearchParameter;
 import com.atlassian.sal.api.search.query.SearchQuery;
 import com.atlassian.sal.api.search.query.SearchQueryParser;
 import com.atlassian.sal.core.message.DefaultMessage;
 import com.atlassian.sal.core.search.BasicSearchMatch;
 import com.atlassian.sal.core.search.BasicResourceType;
 import com.opensymphony.user.EntityNotFoundException;
 import com.opensymphony.user.User;
 import com.opensymphony.user.UserManager;
 
 /**
  *
  */
 public class JiraSearchProvider implements com.atlassian.sal.api.search.SearchProvider
 {
     private static final Logger log = Logger.getLogger(JiraSearchProvider.class);
     private final IssueSearcherManager issueSearcherManager;
     private final QueryCreator queryCreator;
    private final SearchRequestManager searchRequestManager;
     private final SearchProvider searchProvider;
     private final UserManager userManager;
     private final ProjectManager projectManager;
     private final IssueManager issueManager;
     private final SearchRequestFactory searchRequestFactory;
 
 
     public JiraSearchProvider(IssueSearcherManager issueSearcherManager,
        QueryCreator queryCreator, SearchRequestManager searchRequestManager,
         com.atlassian.jira.issue.search.SearchProvider searchProvider,
         UserManager userManager, ProjectManager projectManager, IssueManager issueManager,
         SearchRequestFactory searchRequestFactory)
     {
         this.issueSearcherManager = issueSearcherManager;
         this.queryCreator = queryCreator;
        this.searchRequestManager = searchRequestManager;
         this.searchProvider = searchProvider;
         this.userManager = userManager;
         this.projectManager = projectManager;
         this.issueManager = issueManager;
         this.searchRequestFactory = searchRequestFactory;
     }
 
     public SearchResults search(String username, String searchString)
     {
         SearchQueryParser queryParser = ComponentLocator.getComponent(SearchQueryParser.class);
         final SearchQuery searchQuery = queryParser.parse(searchString);
         int maxHits = searchQuery.getParameter(SearchParameter.MAXHITS, Integer.MAX_VALUE);
         
         final User remoteUser = getUser(username);
         if (remoteUser == null)
         {
             log.info("User '" + username + "' not found. Running anonymous search...");
         }
         final ErrorCollection errors = new SimpleErrorCollection();
         // See if the search String contains a JIRA issue
         Collection<Issue> issues = getIssuesFromQuery(searchQuery.getSearchString());
 
         SearchRequest searchRequest = createSearchRequest(searchQuery, errors, remoteUser);
         if (errors.hasAnyErrors())
         {
             final List<Message> errorMessages = new ArrayList<Message>();
             for (Iterator iterator = errors.getErrors().keySet().iterator(); iterator.hasNext();)
             {
                 String key = (String) iterator.next();
                 errorMessages.add(new DefaultMessage((String) errors.getErrors().get(key)));
             }
             return new SearchResults(errorMessages);
         }
 
         return performSearch(maxHits, searchRequest, issues, remoteUser);
     }
 
     private SearchResults performSearch(int maxHits, SearchRequest searchRequest, Collection<Issue> issues,
        User remoteUser)
     {
         try
         {
             long startTime = System.currentTimeMillis();
             final PagerFilter pagerFilter = new PagerFilter();
 
             pagerFilter.setMax(maxHits);
             com.atlassian.jira.issue.search.SearchResults searchResults =
                 searchProvider.search(searchRequest, remoteUser, pagerFilter);
             issues.addAll(searchResults.getIssues());
             int numResults = searchResults.getTotal() - searchResults.getIssues().size()+issues.size();
             
             List<Issue> trimedResults = new ArrayList<Issue>(issues);
             if (trimedResults.size() > maxHits)
             	trimedResults = trimedResults.subList(0, maxHits);
             	
 			return new SearchResults(transformResults(trimedResults), numResults,
                 System.currentTimeMillis() - startTime);
         }
         catch (SearchException e)
         {
             log.error("Error executing search", e);
             ArrayList<Message> errors = new ArrayList<Message>();
             errors.add(new DefaultMessage(e.getMessage()));
             return new SearchResults(errors);
         }
         finally
         {
             try
             {
                 ThreadLocalSearcherCache.resetSearchers();
             }
             catch (IOException e)
             {
                 log.error("Error closing searchers", e);
             }
         }
     }
 
     private List<SearchMatch> transformResults(final Collection<Issue> issues)
     {
         final List<SearchMatch> matches = new ArrayList<SearchMatch>();
         final ApplicationProperties applicationProperties = getWebProperties();
         for (Iterator iterator = issues.iterator(); iterator.hasNext();)
         {
             final Issue issue = (Issue) iterator.next();
             matches.add(new BasicSearchMatch(applicationProperties.getBaseUrl() + "/browse/" + issue.getKey(),
                 "[" + issue.getKey() + "] " + issue.getSummary(), issue.getDescription(),
                 new BasicResourceType(applicationProperties, issue.getIssueTypeObject().getId())));
         }
         return matches;
     }
 
     private SearchRequest createSearchRequest(SearchQuery query, ErrorCollection errors, User remoteUser)
     {
         final String queryString = queryCreator.createQuery(query.getSearchString());
         //TODO: Perhaps should use a non-deprecated utility here.
         Hashtable queryParams = HttpUtils.parseQueryString(queryString);
 
         addProjectParam(query, queryParams);
 
         IssueNavigatorActionParams issueNavigatorActionParams = new IssueNavigatorActionParams(queryParams);
         FieldValuesHolderImpl holder = new FieldValuesHolderImpl();
         populateAndValidate(issueNavigatorActionParams, holder, errors, remoteUser);
         if (errors.hasAnyErrors())
         {
             return null;
         }
 
        SearchRequest searchRequest = searchRequestFactory.create(null, remoteUser, holder, getSearchContext());
        return searchRequestManager.create(searchRequest);
     }
 
     private Collection<Issue> getIssuesFromQuery(String query)
     {
         Collection<String> issueKeys = JiraKeyUtils.getIssueKeysFromString(query);
         // Need to ensure issue order is maintained, while also ensuring uniqueness, hence LinkedHashSet
         Collection<Issue> issues = new LinkedHashSet<Issue>();
         for (String issueKey : issueKeys)
         {
             Issue issue = getIssueByKey(issueKey);
             if (issue != null)
             {
                 issues.add(issue);
             }
         }
         return issues;
     }
 
     private Issue getIssueByKey(String issueKey)
     {
         try
         {
             return issueManager.getIssueObject(issueKey);
         }
         catch (DataAccessException dae)
         {
             // Not found
             return null;
         }
     }
 
     private void addProjectParam(SearchQuery query, Hashtable queryParams)
     {
         final String projectKey = query.getParameter(SearchParameter.PROJECT);
         if (projectKey != null)
         {
             final Project project = projectManager.getProjectObjByKey(projectKey);
             if (project != null)
             {
                 queryParams.put("pid", new String[]{project.getId().toString()});
             }
         }
     }
 
     void populateAndValidate(IssueNavigatorActionParams actionParams, final FieldValuesHolder fieldValuesHolder,
         ErrorCollection errors, User remoteUser)
     {
         final Collection searchers = issueSearcherManager.getAllSearchers();
         SearchContext searchContext = getSearchContext();
         for (Iterator iterator = searchers.iterator(); iterator.hasNext();)
         {
             IssueSearcher searcher = (IssueSearcher) iterator.next();
             searcher.populateFromParams(fieldValuesHolder, actionParams);
             searcher.validateParams(searchContext, fieldValuesHolder, new I18nBean(remoteUser), errors);
         }
     }
 
     SearchContext getSearchContext()
     {
         return new SearchContextImpl();
     }
 
     ApplicationProperties getWebProperties()
     {
         return ComponentLocator.getComponent(ApplicationProperties.class);
     }
 
     User getUser(String username)
     {
         if (StringUtils.isEmpty(username))
         {
             return null;
         }
         try
         {
             return userManager.getUser(username);
         }
         catch (EntityNotFoundException e)
         {
             return null;
         }
     }
 }
