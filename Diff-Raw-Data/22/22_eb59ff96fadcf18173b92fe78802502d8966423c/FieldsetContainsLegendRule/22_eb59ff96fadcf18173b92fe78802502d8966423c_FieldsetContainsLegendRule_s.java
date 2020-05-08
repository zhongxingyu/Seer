 package de.codescape.bitvunit.rule.forms;
 
 import com.gargoylesoftware.htmlunit.html.HtmlFieldSet;
 import com.gargoylesoftware.htmlunit.html.HtmlLegend;
 import de.codescape.bitvunit.model.Page;
 import de.codescape.bitvunit.rule.AbstractRule;
 import de.codescape.bitvunit.rule.Violations;
 
 /**
 * FieldsetContainsLegendRule ensures that every <code>%lt;fieldset /&gt;</code> within the given HTML document contains
  * a <code>&lt;legend /&gt;</code> tag as the first child element to describe the elements grouped by this fieldset.
  *
  * @author Stefan Glase
  * @since 0.5
  */
 public class FieldsetContainsLegendRule extends AbstractRule {
 
     private static final String RULE_NAME = "FieldsetContainsLegend";
     private static final String RULE_MESSAGE = "Every fieldset should contain a legend as the first child element to describe the group of elements within the fieldset.";
 
     @Override
     public String getName() {
         return RULE_NAME;
     }
 
     @Override
     protected void applyTo(Page page, Violations violations) {
         for (HtmlFieldSet fieldSet : page.findAllFieldsetTags()) {
             if (!(fieldSet.getFirstChild() instanceof HtmlLegend)) {
                 violations.add(createViolation(fieldSet, RULE_MESSAGE));
             }
         }
     }
 
 }
