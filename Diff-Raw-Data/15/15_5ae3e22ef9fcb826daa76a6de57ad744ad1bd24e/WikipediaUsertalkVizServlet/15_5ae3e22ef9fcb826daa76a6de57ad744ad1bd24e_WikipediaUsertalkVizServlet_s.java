 package edu.mit.cci.visualize.wiki.vizservlet;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.List;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 
 import com.google.common.collect.Lists;
 
 import edu.mit.cci.visualize.wiki.collector.ArticleContributions;
 import edu.mit.cci.visualize.wiki.collector.Revisions;
 import edu.mit.cci.visualize.wiki.collector.UsertalkEdge;
 import edu.mit.cci.visualize.wiki.fetcher.PageRevisionFetcher;
 import edu.mit.cci.visualize.wiki.fetcher.UsertalkNetworkFetcher;
 import edu.mit.cci.visualize.wiki.util.MapSorter;
 import edu.mit.cci.visualize.wiki.util.Processing;
 
 public class WikipediaUsertalkVizServlet {
 
     private static final Logger LOG = Logger.getLogger(WikipediaUsertalkVizServlet.class.getName());
     private static final String EOL = "\n";
 
     private final ServletContext context;
     private final HttpServletRequest request;
 
     public WikipediaUsertalkVizServlet(final HttpServletRequest request, final ServletContext context) {
         this.request = request;
         this.context = context;
     }
 
     public String magicMethod() throws IOException, ServletException, NumberFormatException,
             ParseException {
         long currentTimeMillis = System.currentTimeMillis();
         String responseStr = "";
         String pageTitle = readStringParameter("name", ""); // WikiPedia article title
 
         if (pageTitle.isEmpty()) {
             String errorResp = "";
             errorResp += "Please set arguments.<br>" + EOL;
             errorResp += "name: Page_name of a Wikipedia article." + EOL;
             return errorResp;
         }
 
         // # of nodes
         String nodeLimit = readStringParameter("nodeLimit", "10");
         String canvasSize = readStringParameter("size", "300");
 
         // Language
         String lang = readStringParameter("lang", "en");
 
         Revisions revisionData = new PageRevisionFetcher(lang, pageTitle).getArticleRevisions();
 
         // Sort data, with second parameter: getting Top N editors
         List<ArticleContributions> editRanking = new MapSorter().generateTopAuthorRanking(revisionData, Integer.parseInt(nodeLimit));
 
         responseStr += writeProcessingCode(canvasSize, lang, editRanking);
         responseStr += writeAuthorsTable(editRanking);
         long currentTimeMillisAfterDl = System.currentTimeMillis();
         LOG.info("TIME: " + (currentTimeMillisAfterDl - currentTimeMillis));
         return responseStr;
     }
 
     private String prepareProcessingCode(final String canvasSize,
                                          final String lang,
                                          final List<ArticleContributions> nodes) {
         List<String> userIDs = prepareUserIDs(nodes);
         List<UsertalkEdge> edges = new UsertalkNetworkFetcher(lang, userIDs).getNetwork();
         if (edges.size() > 0) {
             LOG.info(nodes.toString());
             LOG.info(edges.toString());
            String path = context.getRealPath("/skelton/skelton_spring.js");
             return new Processing().processingCode(nodes, edges, path, canvasSize);
         }
         return "";
     }
 
     private List<String> prepareUserIDs(final List<ArticleContributions> nodes) {
         List<String> userIDs = Lists.newArrayList();
         for (ArticleContributions node : nodes) {
             userIDs.add(node.getUserID());
         }
         return userIDs;
     }
 
     private String writeProcessingCode(final String canvasSize,
                                        final String lang,
                                        final List<ArticleContributions> nodes) {
         String responseStr = "";
         responseStr += "<script id=\"processing-code\" type=\"application/processing\">" + EOL;
         responseStr += prepareProcessingCode(canvasSize, lang, nodes);
         responseStr += "</script>" + EOL;
         responseStr += "<div><canvas width=\"" + canvasSize + "\" height=\"" + canvasSize + "\"></canvas></div>" + EOL;
         return responseStr;
     }
 
     private String writeAuthorsTable(final List<ArticleContributions> editRanking) {
         String tableContents = "";
         tableContents += "<table>" + EOL;
         tableContents += "<tbody>" + EOL;
         int sumNum = 0;
         String userName_editSize = "";
         for (int i = 0; i < editRanking.size(); i++) {
             String name = editRanking.get(i).getUserID(); // Editor name
             int num = editRanking.get(i).getNumberOfChanges();// # of edits
             int editSize = editRanking.get(i).getEditSize(); // total byte change by this user
 
             userName_editSize += name + "\t" + editSize + "\n";
             sumNum += num;
             tableContents += "<tr>" + EOL;
             tableContents += "<td>" + name + "</td>" + EOL;
             tableContents += "<td>" + num + "</td>" + EOL;
             tableContents += "</tr>" + EOL;
         }
 
         tableContents += "</tbody>" + EOL;
         tableContents += "</table>" + EOL;
         tableContents += "<p id =\"map\"></p>" + EOL;
         return tableContents;
     }
 
     private String readStringParameter(final String paramName, final String defaultValue) {
         if (request.getParameter(paramName) != null) {
             return request.getParameter(paramName);
         }
         return defaultValue;
     }
 
 }
