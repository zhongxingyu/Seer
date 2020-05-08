 package de.codescape.bitvunit.rule.forms;
 
 import com.gargoylesoftware.htmlunit.html.HtmlLabel;
 import de.codescape.bitvunit.model.Page;
 import de.codescape.bitvunit.rule.AbstractRule;
 import de.codescape.bitvunit.rule.Violations;
 
 /**
 * LabelContainsTextRule ensures that every <code>%lt;label /&gt;</code> within the given HTML document contains a
  * textual representation for the given label.
  *
  * @author Stefan Glase
  * @since 0.1
  */
 public class LabelContainsTextRule extends AbstractRule {
 
     private static final String RULE_NAME = "LabelContainsText";
     private static final String RULE_MESSAGE = "Every label must contain text to describe the associated form field.";
 
     @Override
     public String getName() {
         return RULE_NAME;
     }
 
     @Override
     protected void applyTo(Page page, Violations violations) {
         for (HtmlLabel label : page.findAllLabelTags()) {
             validateLabel(label, violations);
         }
     }
 
     private void validateLabel(HtmlLabel label, Violations violations) {
         if (label.getTextContent() == null || label.getTextContent().isEmpty()) {
             violations.add(createViolation(label, RULE_MESSAGE));
         }
     }
 
 }
