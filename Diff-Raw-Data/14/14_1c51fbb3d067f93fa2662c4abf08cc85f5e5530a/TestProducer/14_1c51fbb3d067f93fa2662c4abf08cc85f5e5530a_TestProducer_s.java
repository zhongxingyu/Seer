 /*
  * Created on 8 Jan 2008
  */
 package uk.org.ponder.rsf.test.rvb;
 
 import java.util.List;
 
 import uk.org.ponder.arrayutil.ListUtil;
 import uk.org.ponder.beanutil.entity.EntityID;
 import uk.org.ponder.rsf.bare.RequestLauncher;
 import uk.org.ponder.rsf.components.UICommand;
 import uk.org.ponder.rsf.components.UIContainer;
 import uk.org.ponder.rsf.components.UIForm;
 import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
 import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
 import uk.org.ponder.rsf.util.RSFUtil;
 import uk.org.ponder.rsf.view.ComponentChecker;
 import uk.org.ponder.rsf.view.ViewComponentProducer;
 import uk.org.ponder.rsf.viewstate.EntityCentredViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 
 public class TestProducer implements ViewComponentProducer, NavigationCaseReporter {
 
   public String getViewID() {
     return RequestLauncher.TEST_VIEW;
   }
 
   public void fillComponents(UIContainer tofill, ViewParameters viewparams,
       ComponentChecker checker) {
     UIForm form = UIForm.make(tofill, "form");
     // This binding will ensure that after the form submission is processed, the
     // id which appears at the EL path "idholder.id" in IDHolder will be assigned
     // to the outgoing ViewParameters state (which for convenience, points back at this
     // view) - #{entity.ID} is an EL path into the EntityCentredViewParameters which is 
     // the outgoing VP state reported from our navigationCases for this action cycle.
    RSFUtil.addResultingViewBinding(form, "entity.ID", "idholder.id");
     // Assigns the method binding "idholder.act" which invokes the IDHolder.act() method
     // as part of the submission. This triggers the id to appear at the path
     // "#{idholder.id}", in a similar way to a persistent save operation.
     UICommand.make(form, "mycommand", "idholder.act");
   }
 
   public List reportNavigationCases() {
     return ListUtil.instance(new NavigationCase(
         new EntityCentredViewParameters("null", 
             new EntityID("testentity", null))));
   }
 }
