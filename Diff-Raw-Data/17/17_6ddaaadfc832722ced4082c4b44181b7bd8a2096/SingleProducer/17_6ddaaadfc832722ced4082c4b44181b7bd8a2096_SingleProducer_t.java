 /*
  * Created on 7 Mar 2007
  */
 package uk.ac.cam.caret.rsf.testcomponents.producers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import uk.org.ponder.rsf.components.UICommand;
 import uk.org.ponder.rsf.components.UIContainer;
 import uk.org.ponder.rsf.components.UIForm;
 import uk.org.ponder.rsf.components.UIInputMany;
 import uk.org.ponder.rsf.components.UIOutput;
 import uk.org.ponder.rsf.evolvers.DynamicListInputEvolver;
 import uk.org.ponder.rsf.flow.ARIResult;
 import uk.org.ponder.rsf.flow.jsfnav.NavigationCase;
 import uk.org.ponder.rsf.flow.jsfnav.NavigationCaseReporter;
 import uk.org.ponder.rsf.view.ComponentChecker;
 import uk.org.ponder.rsf.view.ViewComponentProducer;
 import uk.org.ponder.rsf.viewstate.SimpleViewParameters;
 import uk.org.ponder.rsf.viewstate.ViewParameters;
 
/** A view for testing those components which have just a single 
 * implementation in the provided components package.
 * @author Antranig Basman (amb26@ponder.org.uk)
 *
 */

 public class SingleProducer implements ViewComponentProducer,  NavigationCaseReporter{
   public static final String VIEW_ID = "test-components-single";
   public String getViewID() {
     return VIEW_ID;
   }
   private DynamicListInputEvolver dynamicListInputEvolver;
 
   public void setDynamicListInputEvolver(
       DynamicListInputEvolver dynamicListInputEvolver) {
     this.dynamicListInputEvolver = dynamicListInputEvolver;
   }
   
   public void fillComponents(UIContainer tofill, ViewParameters viewparams, 
       ComponentChecker checker) {
     UIForm form = UIForm.make(tofill, "components-form");
     String[] list = new String[] {"Initial marmosets", "Inchoate marsupials"};
     UIInputMany input = UIInputMany.make(form, "list-input:", "dataBean.selection", list);
     dynamicListInputEvolver.setLabels(UIOutput.make("Remove"), UIOutput.make("Add marsupial"));
     dynamicListInputEvolver.evolve(input);
     UICommand.make(form, "submit");
   }
 
   public List reportNavigationCases() {
     List togo = new ArrayList();
     togo.add(new NavigationCase(
         new SimpleViewParameters(ResultsProducer.VIEW_ID), ARIResult.FLOW_ONESTEP));
     return togo;
   }
 
 }
