 package hudson.plugins.googlecode;
 
 import hudson.MarkupText;
 import hudson.MarkupText.SubText;
 import hudson.model.AbstractBuild;
 import hudson.scm.ChangeLogAnnotator;
 import hudson.scm.ChangeLogSet.Entry;
 
 import java.util.regex.Pattern;
 
 /**
  * Annotates <a href="http://code.google.com/p/support/wiki/WikiSyntax">WikiSyntax</a>
  * notation in changelog messages. 
  *
  * @author Kohsuke Kawaguchi
  * @author Erik Ramfelt
  */
 public class GoogleCodeLinkAnnotator extends ChangeLogAnnotator {
 
     private GoogleCodeProjectProperty.PropertyRetriever propertyRetriever;
         
     public GoogleCodeLinkAnnotator(GoogleCodeProjectProperty.PropertyRetriever propertyRetriever) {
         this.propertyRetriever = propertyRetriever;
     }
 
     @Override
     public void annotate(AbstractBuild<?,?> build, Entry change, MarkupText text) {
         GoogleCodeProjectProperty property = propertyRetriever.getProperty(build);
         if(property==null || property.googlecodeWebsite==null)
             return; // not configured
 
         String url = property.googlecodeWebsite;
         for (LinkMarkup markup : MARKUPS)
             markup.process(text, url);
     }
 
     static final class LinkMarkup {
         private final Pattern pattern;
         private final String href;
 
         LinkMarkup(String pattern, String href) {
             pattern = NUM_PATTERN.matcher(pattern).replaceAll("(\\\\d+)"); // \\\\d becomes \\d when in the expanded text.
             pattern = ANYWORD_PATTERN.matcher(pattern).replaceAll("((?:\\\\w|[._-])+)");
             this.pattern = Pattern.compile(pattern);
             this.href = href;
         }
 
         void process(MarkupText text, String url) {
             for(SubText st : text.findTokens(pattern)) {
                 st.surroundWith(
                     "<a href='"+url+href+"'>",
                     "</a>");
             }
         }
 
         private static final Pattern NUM_PATTERN = Pattern.compile("NUM");
         private static final Pattern ANYWORD_PATTERN = Pattern.compile("ANYWORD");
     }
 
     static final LinkMarkup[] MARKUPS = new LinkMarkup[] {
         new LinkMarkup(
            "(?:#|issue )NUM",
             "issues/detail?id=$1"),
         new LinkMarkup(
             "issue #?NUM:NUM",
             "issues/detail?id=$1#$2"),
         new LinkMarkup(
             "((?:[A-Z][a-z]+){2,})|wiki:ANYWORD",
             "wiki/$1$2")
     };
 }
