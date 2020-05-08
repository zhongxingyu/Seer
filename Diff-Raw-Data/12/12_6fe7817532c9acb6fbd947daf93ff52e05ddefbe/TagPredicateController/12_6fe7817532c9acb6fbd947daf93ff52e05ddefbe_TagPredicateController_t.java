 package org.jetbrains.plugins.xml.searchandreplace.ui.controller.search;
 
 import com.intellij.psi.xml.XmlTag;
 import org.jetbrains.plugins.xml.searchandreplace.search.predicates.MatchesXmlTextPredicate;
 import org.jetbrains.plugins.xml.searchandreplace.search.predicates.TagPredicate;
 import org.jetbrains.plugins.xml.searchandreplace.search.predicates.XmlElementPredicate;
 import org.jetbrains.plugins.xml.searchandreplace.ui.ConstraintType;
 import org.jetbrains.plugins.xml.searchandreplace.ui.PredicateTypeRegistry;
 import org.jetbrains.plugins.xml.searchandreplace.ui.controller.replace.Capture;
 import org.jetbrains.plugins.xml.searchandreplace.ui.controller.search.captures.TagNameCapture;
 import org.jetbrains.plugins.xml.searchandreplace.ui.constraintTypes.Inside;
 import org.jetbrains.plugins.xml.searchandreplace.ui.constraintTypes.NotContains;
 import org.jetbrains.plugins.xml.searchandreplace.ui.constraintTypes.NotInside;
 import org.jetbrains.plugins.xml.searchandreplace.ui.view.TagPredicatePanel;
 
 import javax.swing.*;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 
 public class TagPredicateController extends ConstraintTypeController implements TagPredicatePanel.Delegate {
   private TagPredicatePanel myView;
 
   @Override
   public JPanel getView() {
     return myView;
   }
 
   public TagPredicateController(ConstraintType constraintType, boolean strictlyTag) {
     this(constraintType, null, strictlyTag);
   }
 
   public TagPredicateController(ConstraintType constraintType, Params p, boolean strictlyTag) {
     super(p, constraintType);
     myView = new TagPredicatePanel(strictlyTag);
     myView.setDelengate(this);
   }
 
  public boolean isConstraintOnText() {
    return myView.selectedCard().equals(TagPredicatePanel.TEXT);
  }

   @Override
   public XmlElementPredicate buildPredicate() {
     if (myView.selectedCard().equals(TagPredicatePanel.TAG)) {
       final String tagName = myView.getTagName();
       if (tagName.isEmpty()) {
         return null;
       }
       return decorateWithNotIfNeccessary(new TagPredicate() {
 
         @Override
         public String toString() {
           return tagName;
         }
 
         @Override
         public boolean applyToTag(XmlTag tag) {
           return tag.getName().matches(tagName);
         }
 
       });
     } else {
       final String text = myView.getText();
       if (text.isEmpty()) {
         return null;
       }
       return decorateWithNotIfNeccessary(new MatchesXmlTextPredicate(text));
     }
   }
 
   @Override
   public Collection<Capture> provideCaptures(ConstraintController constraintController) {
     if (myView.selectedCard().equals(TagPredicatePanel.TAG)) {
       if (p != Params.NOT) {
         ArrayList<Capture> captures = new ArrayList<Capture>();
         captures.add(new TagNameCapture(constraintController));
         return captures;
       }
     }
     return super.provideCaptures(constraintController);
   }
 
   @Override
   public void stateChanged(TagPredicatePanel tpp) {
     if (getDelegate() != null) {
       getDelegate().updateCaptures(this);
     }
   }
 
   @Override
   public List<ConstraintType> getAllowedChildrenTypes() {
     if (myView.selectedCard().equals(TagPredicatePanel.TAG)) {
       if (getConstraintType() instanceof NotInside || getConstraintType() instanceof NotContains) {
         return new ArrayList<ConstraintType>();
       } else {
         return super.getAllowedChildrenTypes();
       }
     } else {
       return Arrays.asList(PredicateTypeRegistry.getInstance().byClass(Inside.class),
               PredicateTypeRegistry.getInstance().byClass(NotInside.class));
     }
   }
 }
