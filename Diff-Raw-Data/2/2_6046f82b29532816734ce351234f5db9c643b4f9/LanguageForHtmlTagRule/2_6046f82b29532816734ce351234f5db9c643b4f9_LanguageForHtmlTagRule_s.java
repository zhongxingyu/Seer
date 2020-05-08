 package de.codescape.bitvunit.rule.page;
 
 import com.gargoylesoftware.htmlunit.html.HtmlHtml;
 import de.codescape.bitvunit.model.Page;
 import de.codescape.bitvunit.rule.AbstractRule;
 import de.codescape.bitvunit.rule.Violations;
 
 import static de.codescape.bitvunit.util.html.HtmlElementUtil.hasNonEmptyAttribute;
 
 /**
  * LanguageForHtmlTagRule ensures that the given HTML document provides its language information through the
  * <code>lang</code> attribute on the <code>&lt;html/&gt;</code> tag.
  * <p/>
  * A valid example of a document in German language would look like this:
 * <pre><code>&lt;html lang="de"&gt;...&lt;/html&gt</code></pre>
  *
  * @author Stefan Glase
  * @since 0.2
  */
 public class LanguageForHtmlTagRule extends AbstractRule {
 
     private static final String RULE_NAME = "LanguageForHtmlTag";
     private static final String RULE_MESSAGE = "Every <html /> tag should communicate the main language of that page with its lang attribute.";
     private static final String LANG_ATTRIBUTE = "lang";
 
     @Override
     public String getName() {
         return RULE_NAME;
     }
 
     @Override
     protected void applyTo(Page page, Violations violations) {
         for (HtmlHtml html : page.findAllHtmlTags()) {
             if (!hasNonEmptyAttribute(html, LANG_ATTRIBUTE)) {
                 violations.add(createViolation(html, RULE_MESSAGE));
             }
         }
     }
 
 }
