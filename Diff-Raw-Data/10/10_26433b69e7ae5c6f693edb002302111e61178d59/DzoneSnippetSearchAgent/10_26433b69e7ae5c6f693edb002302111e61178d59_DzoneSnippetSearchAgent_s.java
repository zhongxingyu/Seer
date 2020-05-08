 package org.mvnsearch.snippet.impl.dzone;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.StringEscapeUtils;
 import org.htmlparser.Node;
 import org.htmlparser.Parser;
 import org.htmlparser.filters.HasAttributeFilter;
 import org.htmlparser.tags.Div;
 import org.htmlparser.tags.LinkTag;
 import org.htmlparser.util.NodeList;
 import org.mvnsearch.snippet.Category;
 import org.mvnsearch.snippet.Comment;
 import org.mvnsearch.snippet.Snippet;
 import org.mvnsearch.snippet.SnippetSearchAgent;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 /**
  * dzone snippet search agent
  *
  * @author linux_china@hotmail.com
  */
 public class DzoneSnippetSearchAgent extends SnippetSearchAgent {
     /**
      * agent id
      *
      * @return agent id
      */
     public String getId() {
         return "dzone";
     }
 
     /**
      * Just a convenient method to display the source of the search in the results tree of iSnippet.
      *
      * @return brief text about the source from where the results were found
      */
     public String getDisplayName() {
         return "DZone Snippets";
     }
 
     /**
      * The actual web URL
      *
      * @return url from where the results were found
      */
     public String getHomePageURL() {
         return "http://snippets.dzone.com";
     }
 
     /**
      * Detailed description of the site providing these snippets.
      *
      * @return agent description
      */
     public String getDescription() {
         return "Snippets is a public source code repository. Easily build up your personal collection of code snippets, categorize them with tags / keywords, and share them with the world ";
     }
 
     /**
      * Performs the search for given keywords
      *
      * @param keywords - Array of keywords
      * @return Matching araaylist of the ISnippets
      */
     public List<Snippet> query(String[] keywords) {
         List<Snippet> snippets = new ArrayList<Snippet>();
         try {
             String searchUrl = "http://snippets.dzone.com/search/get_results?q=" + StringUtils.join(keywords, "+");
             Parser parser = new Parser();
             parser.setURL(searchUrl);
             NodeList nodeList = parser.extractAllNodesThatMatch(new HasAttributeFilter("class", "post"));
             for (Node node : nodeList.toNodeArray()) {
                 if (node instanceof Div) {
                     Div postTag = (Div) node;
                     Snippet snippet = constructSnippetFromDivTag(postTag);
                     if (snippet != null)
                         snippets.add(snippet);
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return snippets;
     }
 
     /**
      * construct snippet from post div tag
      *
      * @param postTag post div tag
      * @return snippet object
      */
     private Snippet constructSnippetFromDivTag(Div postTag) {
         Snippet snippet = new Snippet();
         LinkTag titleTag = (LinkTag) postTag.getChild(1).getChildren().elementAt(0);
         snippet.setTitle(titleTag.getLinkText());
         snippet.setId(titleTag.getLink().substring(titleTag.getLink().lastIndexOf("/") + 1));
         Div postBodyTag = (Div) postTag.getChild(3);
        int dawnStart = postBodyTag.getChildrenHTML().indexOf("<pre class=\"dawn\">");
         //if no code present, ignore it
         if (dawnStart == -1) return null;
         String description = postBodyTag.getChildrenHTML().substring(0, dawnStart).trim();
         if (description.startsWith("//")) {
             description = description.substring(2);
         }
         snippet.setDescription(description);
         int dawnEnd = postBodyTag.getChildrenHTML().indexOf("</pre>", dawnStart);
        String code = postBodyTag.getChildrenHTML().substring(dawnStart + 18, dawnEnd);
         //clean line number
         code = code.replaceAll("<span class=\"line-numbers\">.*?</span>", "");
         //clean html tag
         code = code.replaceAll("</?\\w+((\\s+\\w+(\\s*=\\s*(?:\".*?\"|'.*?'|[^'\">\\s]+))?)+\\s*|\\s*)/?>", "");
         code = StringEscapeUtils.unescapeHtml(code);
         snippet.setCode(code);
         Div postMetaTag = (Div) postTag.getChild(5);
         int authorStart = postMetaTag.getChildrenHTML().indexOf("<a href=\"/user/") + 15;
         String author = postMetaTag.getChildrenHTML().substring(authorStart, postMetaTag.getChildrenHTML().indexOf("\"", authorStart));
         snippet.setAuthor(author);
         snippet.setDetailUrl("http://snippets.dzone.com/posts/show/"+snippet.getId());
         return snippet;
     }
 
     /**
      * find snippets according to snippets
      *
      * @param categoryId category id
      * @return snippet list
      */
     public List<Snippet> findSnippetsByCategory(String categoryId) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     /**
      * find recent added snippets for RSS
      *
      * @return snippets
      */
     public List<Snippet> findRecentAddedSnippets() {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     /**
      * find snippet by id
      *
      * @param snippetId snippet id
      * @return snippet object
      */
     public Snippet findById(String snippetId) {
         try {
             String searchUrl = "http://snippets.dzone.com/posts/show/" + snippetId;
             Parser parser = new Parser();
             parser.setURL(searchUrl);
             NodeList nodeList = parser.extractAllNodesThatMatch(new HasAttributeFilter("class", "post"));
             for (Node node : nodeList.toNodeArray()) {
                 if (node instanceof Div) {
                     Div postTag = (Div) node;
                     Snippet snippet = constructSnippetFromDivTag(postTag);
                     if (snippet != null) return snippet;
                 }
             }
         } catch (Exception e) {
         }
         return null;
     }
 
     /**
      * update snippet category
      *
      * @param snippet snippet object
      * @return result
      */
     public boolean updateSnippet(Snippet snippet) {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     /**
      * find snippet comment
      *
      * @param snippetId snippet id
      * @return comment list
      */
     public List<Comment> findComments(String snippetId) {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     /**
      * add comment
      *
      * @param snippetId snippet id
      * @param comment   comment
      * @return result
      */
     public boolean addSnippetComment(String snippetId, Comment comment) {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     /**
      * get category list
      *
      * @return category list
      */
     public Collection<Category> findRootCategories() {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     /**
      * get content type for code     text/plain, text/html
      *
      * @return content type for code
      */
     public String getCodeContentType() {
         return "text/palin";
     }
 }
