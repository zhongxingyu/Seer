 package org.mvnsearch.snippet.impl.regex;
 
 import org.apache.commons.lang.StringUtils;
 import org.htmlparser.Node;
 import org.htmlparser.Parser;
 import org.htmlparser.filters.HasAttributeFilter;
 import org.htmlparser.tags.Div;
 import org.htmlparser.tags.LinkTag;
 import org.htmlparser.tags.TableRow;
 import org.htmlparser.tags.TableTag;
 import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserUtils;
import org.mvnsearch.snippet.*;
 
 import java.util.*;
 
 /**
  * regexlib search agent
  *
  * @author linux-china@hotmail.com
  */
 public class RegexlibSearchAgent extends SnippetSearchAgent {
     public Map<String, Category> categories = new HashMap<String, Category>();
 
     /**
      * initialize category
      */
     public RegexlibSearchAgent() {
         categories.put("1", new Category("1", "Email"));
         categories.put("2", new Category("2", "Uri"));
         categories.put("3", new Category("3", "Numbers"));
         categories.put("4", new Category("4", "Strings"));
         categories.put("5", new Category("5", "Dates and Times"));
         categories.put("6", new Category("6", "Misc"));
         categories.put("7", new Category("7", "Address/Phone"));
         categories.put("8", new Category("8", "Markup/Code"));
     }
 
     /**
      * agent id
      *
      * @return agent id
      */
     public String getId() {
         return "regexlib";
     }
 
     /**
      * Just a convenient method to display the source of the search in the results tree of iSnippet.
      *
      * @return brief text about the source from where the results were found
      */
     public String getDisplayName() {
         return "Regular Expression Library Search";
     }
 
     /**
      * The actual web URL
      *
      * @return url from where the results were found
      */
     public String getHomePageURL() {
         return "http://regexlib.com";
     }
 
     /**
      * Detailed description of the site providing these snippets.
      *
      * @return agent description
      */
     public String getDescription() {
         return "Regular Expression Library Search Agent";
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
             String searchUrl = "http://regexlib.com/Search.aspx?c=-1&m=-1&ps=20&k=" + StringUtils.join(keywords, "+");
            Parser parser = ParserUtils.createParserParsingAnInputString(HttpClientManager.fetchUrlContent(searchUrl));
             NodeList nodeList = parser.extractAllNodesThatMatch(new HasAttributeFilter("class", "searchResultsTable"));
             for (Node node : nodeList.toNodeArray()) {
                 TableTag tableTag = (TableTag) node;
                 snippets.add(constructSnippetFromTableTag(tableTag));
             }
         } catch (Exception e) {
         }
         return snippets;
     }
 
     /**
      * construct snippet from table tag
      *
      * @param tableTag table tag
      * @return snippet object
      */
     private Snippet constructSnippetFromTableTag(TableTag tableTag) {
         Snippet snippet = new Snippet();
         TableRow titleRow = tableTag.getRow(0);
         LinkTag titleLink = (LinkTag) titleRow.getColumns()[0].getChild(5);
         snippet.setTitle(titleLink.getLinkText().trim());
         String href = titleLink.getLink();
         snippet.setId(href.substring(href.lastIndexOf("=") + 1));
         TableRow expressionRow = tableTag.getRow(1);
         Div codeDiv = (Div) expressionRow.getColumns()[0].getChild(0);
         snippet.setCode(codeDiv.getChildrenHTML());
         TableRow authorRow = tableTag.getRow(5);
         LinkTag authorLink = (LinkTag) authorRow.getColumns()[0].getChild(3);
         snippet.setAuthor(authorLink.getLinkText().trim());
         snippet.setDescription(tableTag.toHtml());
        snippet.setDescription(snippet.getDescription().replace("src=\"App_Themes", "src=\"http://regexlib.com/App_Themes"));
        snippet.setDescription(snippet.getDescription().replace("<a href='RETester.aspx?regexp_id=" + snippet.getId() + "' class=\"buttonSmall\">Test</a>", ""));
        snippet.setDescription(snippet.getDescription().replace("<a href='REDetails.aspx?regexp_id=" + snippet.getId() + "' class=\"buttonSmall\">Details</a>", ""));
        snippet.setDetailUrl("http://regexlib.com/REDetails.aspx?regexp_id=" + snippet.getId());
         return snippet;
     }
 
     /**
      * find snippets according to snippets
      *
      * @param categoryId category id
      * @return snippet list
      */
     public List<Snippet> findSnippetsByCategory(String categoryId) {
         return null;
     }
 
     /**
      * find recent added snippets for RSS
      *
      * @return snippets
      */
     public List<Snippet> findRecentAddedSnippets() {
         return null;
     }
 
     /**
      * find snippet by id
      *
      * @param snippetId snippet id
      * @return snippet object
      */
     public Snippet findById(String snippetId) {
         try {
             String searchUrl = "http://regexlib.com/REDetails.aspx?regexp_id=" + snippetId;
             Parser parser = new Parser();
             parser.setURL(searchUrl);
             NodeList nodeList = parser.extractAllNodesThatMatch(new HasAttributeFilter("class", "searchResultsTable"));
             TableTag tableTag = (TableTag) nodeList.elementAt(0);
             return constructSnippetFromTableTag(tableTag);
         } catch (Exception e) {
             return null;
         }
     }
 
     /**
      * update snippet category
      *
      * @param snippet snippet object
      * @return result
      */
     public boolean updateSnippet(Snippet snippet) {
         return false;
     }
 
     /**
      * find snippet comment
      *
      * @param snippetId snippet id
      * @return comment list
      */
     public List<Comment> findComments(String snippetId) {
         return null;
     }
 
     /**
      * add comment
      *
      * @param snippetId snippet id
      * @param comment   comment
      * @return result
      */
     public boolean addSnippetComment(String snippetId, Comment comment) {
         return false;
     }
 
     /**
      * get category list
      *
      * @return category list
      */
     public Collection<Category> findRootCategories() {
         return categories.values();
     }
 
     /**
      * get content type for code     text/plain, text/html
      *
      * @return content type for code
      */
     public String getCodeContentType() {
         return "text/plain";
     }
 
 }
