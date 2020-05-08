 /*
 Copyright 2008 Flaptor (flaptor.com) 
 
 Licensed under the Apache License, Version 2.0 (the "License"); 
 you may not use this file except in compliance with the License. 
 You may obtain a copy of the License at 
 
     http://www.apache.org/licenses/LICENSE-2.0 
 
 Unless required by applicable law or agreed to in writing, software 
 distributed under the License is distributed on an "AS IS" BASIS, 
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 See the License for the specific language governing permissions and 
 limitations under the License.
 */
 package com.flaptor.util;
 
 import java.io.File;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.regex.Pattern;
 
 import org.apache.log4j.Logger;
 import org.cyberneko.html.parsers.DOMParser;
 import org.dom4j.Attribute;
 import org.dom4j.Document;
 import org.dom4j.Element;
 import org.dom4j.Node;
 import org.dom4j.Text;
 import org.dom4j.dom.DOMDocumentFactory;
 import org.dom4j.io.DOMReader;
 import org.dom4j.tree.DefaultAttribute;
 
 import com.flaptor.util.Execute;
 import com.flaptor.util.FileUtil;
 import com.flaptor.util.Pair;
 
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ArrayBlockingQueue;
 
 /**
  * This class implements a parser for html documents.
  * @author Flaptor Development Team
  */
 public class HtmlParser {
 
     private static final Logger logger = Logger.getLogger(Execute.whoAmI());
     private static final String HTMLPARSER_CONTENT = "HTML_PARSER_CONTENT_FIELD";
     private BlockingQueue<DOMParser> parsers;
     private String xpathIgnore=null;
     // map field - xpath
     private Map<String,String> fieldDefinitions;
 
     // Add a SEPARATOR at the end of some tags
     // note that also a space is added after the separator.
     private static final String SEPARATOR=".";
 
     /**
      * Default separator tags
      */    
     private String[] SEPARATOR_TAGS= {}; // {"TD","TR","BR","P","LI","UL","DIV","A","H1","H2","H3","H4","H5"};
     
 
     /**
      * Some pages have '<html xmlns=...>' instead of '<html>, this regexp is used
      * to replace them
      *         // <html xmlns=...>  ==>  <html>        
      */
     private static final Pattern REGEXP_HTML= Pattern.compile("<html[^>]*>",
             Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
     
 
     /**
      * Defauly constructor.
      * Does not ignore any element.
      */
     public HtmlParser() {
         this("");
     }
     
     
     /**
      * calls {@link HtmlParser} with default SEPARATOR_TAGS
      * @param ignoreXPath
      */
     public HtmlParser(String ignoreXPath) {
         this(ignoreXPath, null);
     }
     
     public HtmlParser(String ignoreXPath, String[] separatorTags) {
         this(ignoreXPath, separatorTags, new HashMap<String,String>());
     }
 
     /**
      * @param ignoreXPath 
      * @param separatorTags In HTML usually phrases are not ended with '.' or '\n' 
      * because an html tag is used for that (ie: 'p' to define paragraphs). That 
      * might be a problem later (for the snippetSearcher) as might be needed to 
      * know where a phrase ends. Tags appearing in the separatorTags will be 
      * appended with a '.'. Note, usually this list elements must be UPPERCASE.
      * If null, the default SEPARATOR_TAGS are used.  
      */
     public HtmlParser(String ignoreXPath, String[] separatorTags, Map<String,String> fieldDefinitions) {
         int processors = Runtime.getRuntime().availableProcessors();
         logger.info("constructor: found " + processors + " processors. Creating the same number of parsers.");
         parsers = new ArrayBlockingQueue<DOMParser>(processors);
         for (int i = 0; i < processors; i++) {
             DOMParser parser = new org.cyberneko.html.parsers.DOMParser();
             try {
                parser.setFeature("http://cyberneko.org/html/features/scanner/ignore-specified-charset", false);
                 parser.setProperty("http://cyberneko.org/html/properties/default-encoding","UTF-8");
             } catch (Exception e) { 
                 logger.error("Setting nekohtml parser encoding options", e);
             }
             parsers.add(parser);
         }
 
         if (null != ignoreXPath && 0 < ignoreXPath.length()){
             xpathIgnore= ignoreXPath;
         }
         if(null != separatorTags && 0 < separatorTags.length){
             SEPARATOR_TAGS= separatorTags;
         }
         this.fieldDefinitions = fieldDefinitions;
         if (null == fieldDefinitions) this.fieldDefinitions = new HashMap<String,String>();
     }
 
     /**
      * Replace all whitespace with one space character and trim.
      * This runs more than 4 times faster than 
      * <code>str.replaceAll("\\s+"," ").trim()</code>
      */
     private String collapseWhiteSpace(String str) {
         StringBuffer buf = new StringBuffer();
         boolean inspace = false;
         for (int n=0; n<str.length(); n++) {
             char ch = str.charAt(n);
             if (ch==' ' || ch=='\t' || ch=='\n' || ch=='\f' || ch=='\r' || ch==10 || ch==160) {  // 160 is &nbsp; in utf
                 if (!inspace) {
                     buf.append(' ');
                     inspace = true;
                 }
             } else {
                 buf.append(ch);
                 inspace = false;
             }
         }
         return buf.toString().trim();
     }
 
 
     // This method tries to create an URI from a possibly malformed url.
     private static URI getURI(String url) throws URISyntaxException {
     	URI uri = null;
     	url = url.trim();
     	if (url.startsWith("file:") || url.startsWith("javascript:")) {
     		logger.debug("Can't handle url: "+url);
     	} else {
     		int p = url.indexOf('?');
     		if (p < 0) {
     			try {
     				uri = new URI(url.replace(" ", "%20"));
     			} catch (java.net.URISyntaxException e) {
     				logger.debug("Malformed URI: "+url);
     			}
     		} else {
     			String base, query;
     			int q = url.lastIndexOf('#');
     			if (q < 0) q = url.length();
     			if (p < q) { 
     				base = url.substring(0,p+1);
     				query = url.substring(p+1,q);
     			} else {
     				base = url.substring(0,q)+"?";
     				query = url.substring(p+1);
     			}            
     			// Encode any space in the url. Can't use a url encoder because it would encode stuff like '/' and ':'.
     			base = base.replace(" ", "%20");
     			try {
     				// Re-encode the query part, to handle partially encoded urls.
     				query = java.net.URLEncoder.encode(java.net.URLDecoder.decode(query,"UTF-8"),"UTF-8");
     				query = query.replace("%3D","=").replace("%26","&");
     			} catch (java.io.UnsupportedEncodingException e) {
     				logger.debug("encoding a url", e);
     			}
     			url = base + query;
     			uri = new URI(url);
     		}
     	}
         return uri;
     }
 
 
     /**
      * The result of the parser is stored in an object of this class.
      * It contains the extracted text, the title and the outlinks.
      */
     public class Output {
         //private StringBuffer buffer;
         private String text;
         private List<Pair<String,String>> links;
         private String title = "";
         private String url = null;
         private URI baseUri = null;
         // map field - content
         private Map<String,StringBuffer> fields;
 
         public Output(String url) throws URISyntaxException {
             this.url = url;
             if (url.length() > 0) {
                 baseUri = getURI(url);
             }
             links = new ArrayList<Pair<String,String>>();
             fields = new HashMap<String,StringBuffer>();
             fields.put(HTMLPARSER_CONTENT,new StringBuffer());
         }
 
         public void addFieldString(String field, String str) {
             // check that the str is valid.
             if (null == str || "".equals(str)) { 
                 logger.debug("field " + field + " is empty");
                 return;
             }
 
             // So, find field.
             StringBuffer buffer = fields.get(field);
             if (null == buffer) {
                 buffer = new StringBuffer();
                 fields.put(field,buffer);
             } 
             str = collapseWhiteSpace(str);
             if (str.length() > 0) {
                 if (buffer.length() > 0) buffer.append(' ');
                 buffer.append(str.trim());
             }
         }
 
         public void addString(String str) {
             addFieldString(HTMLPARSER_CONTENT,str);
         }
 
 
         public void addLink(String url, String anchor) throws URISyntaxException {
             URI target = getURI(url);
             if (null != target) {
             	if (null != baseUri) {
             		if (baseUri.getPath() == null || baseUri.getPath().length() == 0) {
             			baseUri = baseUri.resolve(URI.create("/"));
             		}
             		target = baseUri.resolve(target);
             	}
             	links.add(new Pair<String,String>(target.toString(),anchor.trim()));
             }
         }
 
         public void setTitle(String title) {
             this.title = title.trim();
         }
         
         public void setBaseUrl(String baseUrl) throws URISyntaxException {
             baseUri = getURI(baseUrl);
         }
 
         protected void close(){
             text = fields.get(HTMLPARSER_CONTENT).toString();
             text = text.replaceAll("(\\.\\s)+", ". ");
             text = text.replaceAll("\\s\\.", ". ");           
         }
         
         public String getText() {
             return text;
         }
 
         public String getUrl() {
             return url;
         }
         
         public List<Pair<String,String>> getLinks() {
             return links;
         }
 
         public String getTitle() {
             return title;
         }
        
         /**
          * Gets the content of the given fieldname.
          * 
          * @param fieldName
          * @return The String content of fieldName if present,
          *         null otherwise.
          */
         public String getField(String fieldName) {
             StringBuffer sb = fields.get(fieldName);
             if (null != sb) { 
                 return sb.toString();
             } else {
                 return null;
             }
         }
         
     }
 
 
     /**
      * Parse the given html document.
      * @param content the html document to parse.
      * @return the parsed string.
      */
     public Output parse(String url, String content) throws Exception {
         // <html xmlns=...>  ==>   <html>
         content= REGEXP_HTML.matcher(content).replaceFirst("<html>");
         // Parser keeps state, synchronize in case its used in a multi-threaded setting.
         Output out = new Output(url);
         DOMParser parser = parsers.take();
         try {
             try {
                 // use cyberneko to parse the html documents (even broken ones)
                 org.xml.sax.InputSource inputSource = new org.xml.sax.InputSource(new java.io.ByteArrayInputStream(content.getBytes("UTF-8")));
                 parser.parse(inputSource);
             } catch (Exception e) {
                 logger.warn("Exception while trying to parse "+url);
                 throw e;
             }
             DOMReader reader = new DOMReader();
             Document htmlDoc;
             try {
                 // get the doc that resulted from parsing the text                
                 org.w3c.dom.Document document = parser.getDocument();                
                 htmlDoc = reader.read(document);                
             } catch (java.lang.StackOverflowError e) {
                 logger.warn("Out of stack memory trying to parse "+url);
                 throw new Exception(e);
             }
             // this 2 must be before the ignoreXPath, else an ignoreXPath that
             // includes the //TITLE will imply that the title is not indexed
             // extract the links
             extractLinks(htmlDoc, out);
 
             // extact the title
             extractTitle(htmlDoc, out);
     
             ignoreXpath(htmlDoc);
 
             replaceSeparatorTags(htmlDoc);
             
             // extract the text from the html tags
             extractText(htmlDoc.getRootElement(), out,HTMLPARSER_CONTENT);
 
 
             // extract special fields
             extractFields(htmlDoc,out);
         } finally {
             parsers.add(parser);
         }
         out.close();
         return out;
     }
 
 
 
     private void extractTitle(Document htmlDoc, Output out){
         Node titleNode = htmlDoc.selectSingleNode("//TITLE|//title|//Title");
         if (null != titleNode) {
             out.setTitle(titleNode.getText());
         }
     }
     
     @SuppressWarnings("unchecked")
     private void extractLinks(Document htmlDoc, Output out) {
         try {
             Node baseNode = htmlDoc.selectSingleNode("//BASE|//Base|//base");
             if (null != baseNode) {
                 Attribute href = ((Element) baseNode).attribute("href");
                 if (null == href) {
                     href = ((Element) baseNode).attribute("HREF");
                     if (null == href) {
                         href = ((Element) baseNode).attribute("Href");
                     }
                 }
                 if (null != href) {
                     String base = href.getValue();
                     if (null != base) {
                         out.setBaseUrl(base);
                     }
                 }
             }
             List links = htmlDoc.selectNodes("//A|//a");
             for (Iterator iter = links.iterator(); iter.hasNext();) {
                 Element link = (Element) iter.next();
                 Attribute href = link.attribute("href");
                 if (null != href) {
                     try {
                         out.addLink(href.getValue(), link.getText());
                     } catch (URISyntaxException e) {
                         logger.debug("Exception occurred, ignoring link " +
                                      link.getText() + " at " + href.getValue(), e);
                     }
                 }
             }
         } catch (URISyntaxException e) {
             logger.debug("Exception occurred, ignoring links in "+out.getUrl(), e);
         }
     }
 
     private void extractFields(Document htmlDoc, Output out) {
         for (String field: fieldDefinitions.keySet()) {
             String xpath = fieldDefinitions.get(field);
             List elements = htmlDoc.selectNodes(xpath);
             logger.debug("found " + elements.size() + " elements for " + xpath);
             for ( Iterator iter = elements.iterator(); iter.hasNext();) {
                 Object next = iter.next();
                 if (next instanceof DefaultAttribute) {
                     DefaultAttribute attr = (DefaultAttribute) next;
                     out.addFieldString(field,attr.getValue());
                 } else if ( next instanceof Element) {
                     Element el = (Element)next;
                     extractText(el,out,field);
                 } else {
                     logger.debug("xpath " + xpath + " selected some nodes of unknown type (" + next.getClass().getName() + " )");
                 } 
             }
         
         }
     
     }
 
 
     /**
      * This parser deletes all the tags and returns only the text. However some
      * tags are used to separate dofferent phrases, so we just add a '.' after
      * some tags to make sure we will be able to distinguish one phrase from 
      * the other in the text
      * @param htmlDoc
      */
     @SuppressWarnings("unchecked")
     private void replaceSeparatorTags(Document htmlDoc){
         for (String tag: SEPARATOR_TAGS ){                
             List<Element> nodes = (List<Element>) htmlDoc.selectNodes("//" + tag.toUpperCase());
             for (Element node: nodes){
                 try {
                     // The 'separator' must be created each time inside the for,
                     // else there is a 'already have a parent' conflict
                     Node separator = DOMDocumentFactory.getInstance().createText(SEPARATOR);
                     node.add(separator);
                 } catch (Exception e) {
                     logger.debug("Ignoring exception, not appending at " + node.getPath());
                     continue;
                 }
             }
         }        
     }
     
     @SuppressWarnings("unchecked")
     private void ignoreXpath(Document htmlDoc){
         if (null == xpathIgnore){ 
             return;
         }
         List<Node> nodes = (List<Node>) htmlDoc.selectNodes(xpathIgnore.toString());
         for (Node node: nodes){
             try {
                 node.detach();
             }catch (Exception e) {
                 logger.debug("Ignoring exception", e);
             }
         }        
     }
     
     /**
      * Simple method to concatenate all readable text in the document and get the outlinks.
      * 
      * @param e
      *            the element in where to look for readable text and outlinks.
      * @param out
      *            the parse output so far. For any caller except the getText itself,
      *            should be empty. After return, it contains the readable text
      *            of the html and the outlinks.
      */
     protected void extractText(final Element e, final Output out, final String fieldName) {
         //String nodeName = e.getName();
         if (!(e.getNodeType() == Node.COMMENT_NODE)) {
             int size = e.nodeCount();
             for (int i = 0; i < size; i++) {
                 Node node = e.node(i);                
                 if (node instanceof Element) {
                     extractText((Element) node, out,fieldName);
                 } else if (node instanceof Text) {
                     String t = node.getText();
                     out.addFieldString(fieldName,t);
                 }
             }
         }
     }
 
     public void test(String base, String link) throws Exception {
         Output out = new Output(base);
         out.addLink(link,"");
         for (Pair<String,String> lnk : out.getLinks()) {
             System.out.println(lnk.first());
         }
     }
 
     public static void main(String[] arg) throws Exception {
         HtmlParser parser = new HtmlParser();
         //parser.test(arg[0],arg[1]);
         String str = FileUtil.readFile(new File(arg[0]));
         String url = "http://url.com";
         if (arg.length > 1) { url = arg[1]; }
         Output out = parser.parse(url, str);
         System.out.println("-------------------------------------------");
         System.out.println("TITLE: "+out.getTitle());
         for (Pair<String,String> link : out.getLinks()) {
             System.out.println("LINK: "+link.first()+"  ("+link.last()+")");
         }
         System.out.println("CONTENT: "+out.getText());
     }
 
 }
