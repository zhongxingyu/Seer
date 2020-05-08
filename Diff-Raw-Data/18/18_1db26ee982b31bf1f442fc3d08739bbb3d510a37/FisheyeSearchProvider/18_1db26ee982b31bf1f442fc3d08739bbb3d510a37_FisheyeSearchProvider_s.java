 package com.atlassian.sal.fisheye.search;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import com.atlassian.sal.api.ApplicationProperties;
 import com.atlassian.sal.api.component.ComponentLocator;
 import com.atlassian.sal.api.message.DefaultMessage;
 import com.atlassian.sal.api.message.Message;
 import com.atlassian.sal.api.search.BasicResourceType;
 import com.atlassian.sal.api.search.BasicSearchMatch;
 import com.atlassian.sal.api.search.SearchMatch;
 import com.atlassian.sal.api.search.SearchProvider;
 import com.atlassian.sal.api.search.SearchResults;
 import com.atlassian.sal.api.search.parameter.SearchParameter;
 import com.atlassian.sal.api.search.query.SearchQuery;
 import com.atlassian.sal.api.search.query.SearchQueryParser;
 import com.cenqua.crucible.filters.CrucibleFilter;
 import com.cenqua.fisheye.AppConfig;
 import com.cenqua.fisheye.LicensePolicyException;
 import com.cenqua.fisheye.cvsrep.search.SearchManager;
 import com.cenqua.fisheye.cvsrep.search.query.FishQuery;
 import com.cenqua.fisheye.rep.DbException;
 import com.cenqua.fisheye.rep.FileRevision;
 import com.cenqua.fisheye.rep.RepositoryEngine;
 import com.cenqua.fisheye.rep.RepositoryHandle;
 import com.cenqua.fisheye.user.UserLogin;
 import com.cenqua.fisheye.user.UserManager;
 import com.cenqua.fisheye.web.FishEyePathInfo;
 import com.cenqua.fisheye.web.SearchResultsExplorer;
 import com.cenqua.fisheye.web.UrlHelper;
 
 /**
  * A FishEye specific search provider.  Groups search results by change sets.  Only repositories that the user
  * has permission for will be returned in the search.  An optional PATH parameter can be specified to limit the search
  * to a specific repository.
  */
 public class FisheyeSearchProvider implements SearchProvider
 {
     private static final Logger log = Logger.getLogger(FisheyeSearchProvider.class);
 
     private static final int MAX_FILES = 5;
 
     public SearchResults search(String username, String searchString)
     {
         SearchQueryParser queryParser = ComponentLocator.getComponent(SearchQueryParser.class);
         final SearchQuery searchQuery = queryParser.parse(searchString);
 
 		int maxHits = searchQuery.getParameter(SearchParameter.MAXHITS, Integer.MAX_VALUE);
 
 		final List<Message> errors = validateQuery(searchQuery, username);
         if (!errors.isEmpty())
         {
             return new SearchResults(errors);
         }
 
 
         return doFisheyeSearch(searchQuery.getSearchString(), maxHits, searchQuery.getParameter(SearchParameter.PROJECT), username);
     }
 
     private List<Message> validateQuery(SearchQuery searchQuery, String username)
     {
         final List<Message> errors = new ArrayList<Message>();
         //FishEye also may have a projectParameter...
         final String projectParameter = searchQuery.getParameter(SearchParameter.PROJECT);
         if (projectParameter != null)
         {
             final FishEyePathInfo pathInfo = new FishEyePathInfo(projectParameter);
             final String repositoryName = pathInfo.getRepname();
             if (repositoryName == null)
             {
                 errors.add(new DefaultMessage("studio.search.errors.no.repository.found", projectParameter));
             }
             else
             {
                 getRepositoryEngine(repositoryName, errors, username);
             }
         }
         return errors;
     }
 
 
     private SearchResults doFisheyeSearch(String searchQuery, int maxHits, String projectParameter, String username)
     {
         //if no path was specified, try to search all repositories!
         if (projectParameter == null)
         {
             long startTime = System.currentTimeMillis();
             List<SearchMatch> matches = new ArrayList<SearchMatch>();
             final List<Message> errors = new ArrayList<Message>();
 
             int combinedTotal = 0;
             final Set<String> projectKeys = getLocalProjectKeys();
             for (String projectKey : projectKeys)
             {
                 final SearchResults results = doFisheyeRepositorySearch(searchQuery, maxHits, projectKey, username);
                 matches.addAll(results.getMatches());
                 errors.addAll(results.getErrors());
                 combinedTotal += results.getTotalResults();
             }
             if (errors.isEmpty())
             {
                 //need to ensure we don't return more than maxHits results!
                 if (matches.size() > maxHits)
                 {
                     matches = new ArrayList<SearchMatch>(matches.subList(0, maxHits));
                 }
                 return new SearchResults(matches, combinedTotal, System.currentTimeMillis() - startTime);
             }
             else
             {
                 return new SearchResults(errors);
             }
 
         }
         return doFisheyeRepositorySearch(searchQuery, maxHits, projectParameter, username);
     }
 
     private SearchResults doFisheyeRepositorySearch(String searchQuery, int maxHits,
                                                     String pathParameter, String username)
     {
         final List<Message> errors = new ArrayList<Message>();
         final FishEyePathInfo pathInfo = new FishEyePathInfo(pathParameter);
         final String repositoryName = pathInfo.getRepname();
         final RepositoryEngine engine = getRepositoryEngine(repositoryName, errors, username);
         final SearchManager search = engine.getSearchManager();
 
         if (!errors.isEmpty())
         {
             return new SearchResults(errors);
         }
 
         String query = buildEyeQL(searchQuery);
 
         final List errorStrings = new ArrayList();
         FishQuery q = FishQuery.parse(query, errorStrings);
         if (q == null)
         {
             for (Iterator iterator = errorStrings.iterator(); iterator.hasNext();)
             {
                 String errorString = (String) iterator.next();
                 errors.add(new DefaultMessage(errorString));
             }
             return new SearchResults(errors);
         }
 
         try
         {
             long startTime = System.currentTimeMillis();
 
             com.cenqua.fisheye.cvsrep.search.SearchResults collator = search.runQuery(q, true);
             SearchResultsExplorer results = getSearchResultsExplorer(repositoryName, collator);
             final List<SearchMatch> matches = transformFisheyeResults(maxHits, repositoryName, results);
 
             return new SearchResults(matches, results.getGroups().size(), System.currentTimeMillis() - startTime);
         }
         catch (Exception e)
         {
             errors.add(new DefaultMessage(e.getMessage()));
             return new SearchResults(errors);
         }
     }
 
     private String buildEyeQL(String searchQuery)
     {
         StringBuilder query = new StringBuilder();
         query.append("select revisions where ");
         final String[] queryStrings = searchQuery.split(" ");
         for (int i = 0; i < queryStrings.length; i++)
         {
             String queryString = queryStrings[i];
             query.append("path like ").append(queryString).append(" or ").
                     append("author = ").append(queryString).append(" or ").
                     append("comment matches ").append(queryString).append(" ");
             if(i+1 < queryStrings.length)
             {
                 query.append("or ");
             }
         }
         query.append("group by changeset");
         return query.toString();
     }
 
     private List<SearchMatch> transformFisheyeResults(int maxHits, String repositoryName, SearchResultsExplorer results)
             throws Exception
     {
         final ApplicationProperties applicationProperties = ComponentLocator.getComponent(ApplicationProperties.class);
         final String baseUrl = applicationProperties.getBaseUrl();
 
         final List groups = results.getGroups();
         final List<SearchMatch> matches = new ArrayList<SearchMatch>();
         int count = 0;
         for (Iterator iterator = groups.iterator(); iterator.hasNext() && count < maxHits; count++)
         {
             SearchResultsExplorer.GroupItem groupItem = (SearchResultsExplorer.GroupItem) iterator.next();
             final FileRevision firstRevision = groupItem.getFirst();
 
             final List<FileRevision> revisionList = groupItem.getItems();
             String excerpt = buildExcerpt(firstRevision, revisionList);
             matches.add(new BasicSearchMatch(baseUrl + "/changelog/" + repositoryName + "/" + firstRevision.getPath() + "/?cs=" + firstRevision.getChangeSetId(),
                     firstRevision.getChangeSetId() + " by " + firstRevision.getAuthor(), excerpt, new BasicResourceType(applicationProperties, "changeset")));
         }
         return matches;
     }
 
     private String buildExcerpt(FileRevision firstRevision, List<FileRevision> revisionList)
     {
         StringBuffer excerpt = new StringBuffer();
         excerpt.append(firstRevision.getComment()).append("\n");
         int count = 0;
         for (FileRevision fileRevision : revisionList)
         {
             count++;
             excerpt.append(fileRevision.getPath()).append("\n");
             if (count > MAX_FILES)
             {
                 excerpt.append("...");
                 break;
             }
         }
         return excerpt.toString();
     }
 
     private RepositoryEngine getRepositoryEngine(String repositoryName, List<Message> errors, String username)
     {
         try
         {
             RepositoryHandle handle = AppConfig.getsConfig().getRepositoryManager().getRepository(repositoryName);
 
             if (handle == null)
             {
                 errors.add(new DefaultMessage("studio.search.errors.no.repository.found", repositoryName));
                 return null;
             }
 
             if (!hasPermissionToView(username, handle))
             {
                errors.add(new DefaultMessage("studio.search.errors.access.not.allowed.for.repository", repositoryName));
                 return null;
             }
 
             return handle.acquireEngine();
         }
         catch (RepositoryHandle.StateException e)
         {
             errors.add(new DefaultMessage("studio.search.errors.opening.repository", repositoryName));
             return null;
         }
     }
 
     private boolean hasPermissionToView(String username, RepositoryHandle handle)
     {
         final UserManager um = AppConfig.getsConfig().getUserManager();
         UserLogin userLogin;
         if (username == null)
         {
             userLogin = null;
         }
         else
         {
             try
             {
                 userLogin = um.createTrustedUserLogin(username);
             }
             catch (DbException e)
             {
                 log.warn("Invalid user '" + username + "' tried to view review. Access denied.");
                 return false;
             }
             catch (LicensePolicyException e)
             {
                 log.warn("License exception when user '" + username + "' tried to view review. Access denied.");
                 return false;
             }
         }
         return um.hasPermissionToAccess(userLogin, handle);
     }
 
     private Set<String> getLocalProjectKeys()
     {
         Set<String> repoData = new HashSet<String>();
         for (RepositoryHandle h : AppConfig.getsConfig().getRepositoryManager().getHandles())
         {
             repoData.add(h.getName());
         }
         return repoData;
     }
 
 
     private SearchResultsExplorer getSearchResultsExplorer(String repositoryName, com.cenqua.fisheye.cvsrep.search.SearchResults collator)
     {
         final ApplicationProperties applicationProperties = ComponentLocator.getComponent(ApplicationProperties.class);
         final String baseUrl = applicationProperties.getBaseUrl();
         final UrlHelper searchUrl = new UrlHelper();
         searchUrl.setUrl(baseUrl + "/search/" + repositoryName + "/");
         SearchResultsExplorer results = new SearchResultsExplorer(collator);
         results.init(CrucibleFilter.getRequest(), searchUrl);
         return results;
     }
 }
