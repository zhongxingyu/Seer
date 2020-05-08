 package de.codescape.bitvunit.rule.lists;
 
 import com.gargoylesoftware.htmlunit.html.HtmlElement;
 import com.gargoylesoftware.htmlunit.html.HtmlListItem;
 import com.gargoylesoftware.htmlunit.html.HtmlUnorderedList;
 import de.codescape.bitvunit.model.Page;
 import de.codescape.bitvunit.rule.AbstractRule;
 import de.codescape.bitvunit.rule.Violations;
 
 import java.util.List;
 
 /**
  * UnorderedListContainsListItemsRule ensures that every ordered list <code>&lt;ul /&gt;</code> within the given HTML
  * document contains one or more list items <code>&lt;li /&gt;</code>.
  *
  * @since 0.3
  */
 public class UnorderedListContainsListItemsRule extends AbstractRule {
 
     private static final String RULE_NAME = "UnorderedListContainsListItems";
     private static final String RULE_MESSAGE = "Unordered lists should always have one or more list items.";
 
     @Override
     public String getName() {
         return RULE_NAME;
     }
 
     @Override
     protected void applyTo(Page page, Violations violations) {
         List<HtmlUnorderedList> unorderedLists = page.findAllUnorderedLists();
         for (HtmlUnorderedList unorderedList : unorderedLists) {
             if (!listHasOneOrMoreListItems(unorderedList)) {
                 violations.add(createViolation(unorderedList, RULE_MESSAGE));
             }
         }
     }
 
     private boolean listHasOneOrMoreListItems(HtmlUnorderedList list) {
         for (HtmlElement childElement : list.getChildElements()) {
             if (HtmlListItem.TAG_NAME.equals(childElement.getTagName())) {
                 return true;
             }
         }
         return false;
     }
 
 }
