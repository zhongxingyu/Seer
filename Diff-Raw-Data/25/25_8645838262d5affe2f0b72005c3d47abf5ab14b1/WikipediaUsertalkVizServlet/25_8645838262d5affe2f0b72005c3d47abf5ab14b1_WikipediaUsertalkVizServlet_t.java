 package edu.mit.cci.visualize.wiki.vizservlet;
 
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import com.google.common.collect.Lists;
 
 import edu.mit.cci.visualize.wiki.collector.ArticleContributions;
 import edu.mit.cci.visualize.wiki.collector.Revisions;
 import edu.mit.cci.visualize.wiki.fetcher.PageRevisionFetcher;
 import edu.mit.cci.visualize.wiki.util.MapSorter;
 
 public class WikipediaUsertalkVizServlet {
 
     private final String nodeLimit;
     private final String lang;
     private final String pageTitle;
 
     public WikipediaUsertalkVizServlet(final HttpServletRequest request) {
        nodeLimit = readStringParameter("nodeLimit", "20", request);
        lang = readStringParameter("lang", "en", request);
        pageTitle = readStringParameter("name", "", request);
    }

    public WikipediaUsertalkVizServlet(final String pageTitle) {
        this.pageTitle = pageTitle;
        this.nodeLimit = "20";
        this.lang = "en";
     }
 
     public List<ArticleContributions> getContributions() {
         Revisions revisionData = new PageRevisionFetcher(lang, pageTitle).getArticleRevisions();
         return new MapSorter().generateTopAuthorRanking(revisionData, Integer.parseInt(nodeLimit));
     }
 
     public List<String> prepareUserIDs(final List<ArticleContributions> nodes) {
         List<String> userIDs = Lists.newArrayList();
         for (ArticleContributions node : nodes) {
             userIDs.add(node.getUserID());
         }
         return userIDs;
     }
 
    private String readStringParameter(final String paramName, final String defaultValue, final HttpServletRequest request) {
         if (request.getParameter(paramName) != null) {
             return request.getParameter(paramName);
         }
         return defaultValue;
     }
 
 }
