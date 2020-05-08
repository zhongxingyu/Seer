 package de.codescape.bitvunit.rule.images;
 
 import com.gargoylesoftware.htmlunit.html.HtmlImage;
 import de.codescape.bitvunit.model.Page;
 import de.codescape.bitvunit.rule.AbstractRule;
 import de.codescape.bitvunit.rule.Violations;
 
 import java.util.List;
 
 import static de.codescape.bitvunit.util.html.HtmlElementUtil.hasNonEmptyAttribute;
 
 /**
  * AlternativeTextForLinkedImageRule ensures that every image created by the <code>&lt;img/&gt;</code> tag within the
  * given HTML document that is part of a hyperlink created by the <code>&lt;a/&gt;</code> tag provides a non empty
  * alternative text through it's <code>alt</code> attribute.
  * <p/>
  * A good example for a valid linked image should look like this:
  * <pre><code>&lt;a href="contact.html"&gt;&lt;img src="image.gif" alt="Contact me"/&gt;&lt;/a&gt;</code></pre>
  *
  * @author Stefan Glase
  * @since 0.6
  */
 public class AlternativeTextForLinkedImageRule extends AbstractRule {
 
     private static final String RULE_NAME = "AlternativeTextForLinkedImage";
    private static final String RULE_MESSAGE = "Linked images should always provide an alternative text through it's alt attribute.";
 
     @Override
     public String getName() {
         return RULE_NAME;
     }
 
     @Override
     protected void applyTo(Page page, Violations violations) {
         List<HtmlImage> images = page.findAllImageTags();
         for (HtmlImage image : images) {
             if (isLinkedImage(image) && !hasNonEmptyAttribute(image, "alt")) {
                 violations.add(createViolation(image, RULE_MESSAGE));
             }
         }
     }
 
     private boolean isLinkedImage(HtmlImage image) {
         return image.getEnclosingElement("A") != null;
     }
 
 }
