 package org.codehaus.xsite.extractors;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.codehaus.xsite.PageExtractor;
 import org.codehaus.xsite.io.FileSystem;
 import org.codehaus.xsite.model.Page;
 
 import com.opensymphony.module.sitemesh.html.BasicRule;
 import com.opensymphony.module.sitemesh.html.CustomTag;
 import com.opensymphony.module.sitemesh.html.HTMLProcessor;
 import com.opensymphony.module.sitemesh.html.Tag;
 import com.opensymphony.module.sitemesh.html.rules.BodyTagRule;
 import com.opensymphony.module.sitemesh.html.rules.HeadExtractingRule;
 import com.opensymphony.module.sitemesh.html.rules.MetaTagRule;
 import com.opensymphony.module.sitemesh.html.rules.PageBuilder;
 import com.opensymphony.module.sitemesh.html.rules.RegexReplacementTextFilter;
 import com.opensymphony.module.sitemesh.html.rules.TitleExtractingRule;
 import com.opensymphony.module.sitemesh.html.util.CharArray;
 
 /**
  * PageExtractor which extract page information from an HTML file using the SiteMesh library.
  *
  * @author Joe Walnes
  */
 public class SiteMeshPageExtractor implements PageExtractor {
 
     private Properties properties;
     private String filename;
     private String head;
     private String body;
     private Collection links = new HashSet();
 
     public Page extractPage(File htmlFile) {
         try {
             filename = htmlFile.getName();
             FileSystem fileSystem = new FileSystem();
             char[] rawHTML = fileSystem.readFile(htmlFile);
             extractContentFromHTML(rawHTML);
             return new Page(filename, head, body, links, properties);
         } catch (IOException e) {
             throw new CannotParsePageException(e);
         }
     }
 
     public Page extractPage(String filename, String htmlContent) {
         try {
             this.filename = filename;
             extractContentFromHTML(htmlContent.toCharArray());
             return new Page(filename, head, body, links, properties);
         } catch (IOException e) {
             throw new CannotParsePageException(e);
         }
     }
 
     private void extractContentFromHTML(char[] rawHTML) throws IOException {
         // where to dump properties extracted from the page
         properties = new Properties();
         PageBuilder pageBuilder = new PageBuilder() {
             public void addProperty(String key, String value) {
                 properties.setProperty(key, value);
             }
         };
 
         // buffers to hold head and body content
         CharArray headBuffer = new CharArray(64);
         CharArray bodyBuffer = new CharArray(4096);
 
         // setup rules for html processor
         HTMLProcessor htmlProcessor = new HTMLProcessor(rawHTML, bodyBuffer);
         htmlProcessor.addRule(new BodyTagRule(pageBuilder, bodyBuffer));
         htmlProcessor.addRule(new HeadExtractingRule(headBuffer));
         htmlProcessor.addRule(new TitleExtractingRule(pageBuilder));
         htmlProcessor.addRule(new MetaTagRule(pageBuilder));
         htmlProcessor.addRule(new LinkExtractingRule());
         htmlProcessor.addRule(new AddFirstChildClassToHeader());
         // turn JIRA:XSTR-123 snippets into links
         htmlProcessor.addTextFilter(new RegexReplacementTextFilter("JIRA:(XSTR\\-[0-9]+)", "<a href=\"http://jira.codehaus.org/browse/$1\">$1</a>"));
 
         // go!
         htmlProcessor.process();
         this.head = headBuffer.toString();
         this.body = bodyBuffer.toString();
     }
 
     public static class CannotParsePageException extends RuntimeException {
         public CannotParsePageException(Throwable cause) {
             super(cause);
         }
     }
 
     /**
      * Rule for HTMLProcessor that records all <a href=""> links.
      */
     private class LinkExtractingRule extends BasicRule {
         public boolean shouldProcess(String tag) {
             return tag.equalsIgnoreCase("a");
         }
 
         public void process(Tag tag) {
             if (tag.hasAttribute("href", false)) {
                 links.add(tag.getAttributeValue("href", false));
             }
             tag.writeTo(currentBuffer());
         }
     }
 
     /**
      * Rule for HTMLProcessor that adds class=""FirstChild" to the first header of the body if it is the first element.
      */
     private class AddFirstChildClassToHeader extends BasicRule {
         private boolean firstChildIsHeader = true;
         private final Pattern pattern;
 
         private AddFirstChildClassToHeader() {
             pattern = Pattern.compile("^H[1-9]$", Pattern.CASE_INSENSITIVE);
         }
 
         public boolean shouldProcess(String tag) {
             final Matcher matcher = pattern.matcher(tag);
             return tag.equalsIgnoreCase("p") || matcher.matches();
         }
 
         public void process(Tag tag) {
             if (firstChildIsHeader) {
                 if (!tag.getName().equalsIgnoreCase("p")) {
                     final CustomTag customTag;
                     // http://jira.opensymphony.com/browse/SIM-202
                     if (tag.getAttributeCount() == 0) {
                         customTag = new CustomTag(tag.getName(), tag.getType());
                     } else {
                         customTag = new CustomTag(tag);
                     }
                    customTag.addAttribute("class", "FirstChild");
                     tag = customTag;
                 }
                 firstChildIsHeader = false;
             }
             tag.writeTo(currentBuffer());
         }
     }
 
 }
