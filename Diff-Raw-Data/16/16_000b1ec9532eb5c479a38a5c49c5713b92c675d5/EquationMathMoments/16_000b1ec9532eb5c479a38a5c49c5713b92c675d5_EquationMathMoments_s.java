 /*
  * EquationMoments.java
  *
  * Created on July 26, 2007, 1:10 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 package edu.gatech.statics.modes.equation.worksheet;
 
 import edu.gatech.statics.modes.equation.*;
 import edu.gatech.statics.objects.SimulationObject;
 import edu.gatech.statics.application.StaticsApplication;
 import edu.gatech.statics.math.Vector;
 import edu.gatech.statics.math.Vector3bd;
 import edu.gatech.statics.objects.Force;
 import edu.gatech.statics.objects.Moment;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class EquationMathMoments extends EquationMath {
 
     private boolean observationPointSet = false;
     private Vector3bd observationPoint = new Vector3bd();
 
     public Vector3bd getObservationPoint() {
         return observationPoint;
     }
 
     public void setObservationPoint(Vector3bd point) {
         this.observationPoint = point;
         observationPointSet = true;
     }
 
     public boolean getObservationPointSet() {
         return observationPointSet;
     }
 
     @Override
     public String getName() {
         return "M[P]";
     }
 
     @Override
     public Term createTerm(Vector source) {
         return new MomentTerm(source, this);
     }
 
     /** Creates a new instance of EquationMoments */
     public EquationMathMoments(EquationDiagram world) {
         super(world);
     }
 
     @Override
     public boolean check() {
 
         if (!observationPointSet) {
             Logger.getLogger("Statics").info("check: Moment point is not set!");
             Logger.getLogger("Statics").info("check: FAILED");
 
             StaticsApplication.getApp().setAdviceKey("equation_feedback_check_fail_moment_point_not_set");
             return false;
         }
 
         // first, make sure all of the necessary terms are added to the equation.
         List<Force> allForces = new ArrayList();
         List<Moment> allMoments = new ArrayList();
 
         for (SimulationObject obj : getWorld().allObjects()) {
             if (obj instanceof Force && !obj.isDisplayGrayed()) // should not be grayed anyway, but just in case.
             {
                 allForces.add((Force) obj);
             }
             if (obj instanceof Moment && !obj.isDisplayGrayed()) {
                 allMoments.add((Moment) obj);
             }
         }
 
         for (Force force : allForces) {
             Term term = getTerm(force.getVector());
 
             // clear off things that would not add via cross product
             float contribution = (float) force.getVectorValue().cross(force.getAnchor().getPosition().subtract(getObservationPoint())).length();
             contribution *= force.getVector().doubleValue();
 
             if (Math.abs(contribution) < TEST_ACCURACY) {
                 if (term != null) {
                     Logger.getLogger("Statics").info("check: equation has unnecessary term: " + term.getSource());
                     Logger.getLogger("Statics").info("check: FAILED");
 
                     StaticsApplication.getApp().setAdviceKey("equation_feedback_check_fail_unnecessary", term.getSource().getSymbolName());
                     return false;
                 } else {
                     continue;
                 }
             }
 
             if (term == null) {
                 Logger.getLogger("Statics").info("check: equation has not added all terms: " + force.getVector());
                 Logger.getLogger("Statics").info("check: FAILED");
 
                 StaticsApplication.getApp().setAdviceKey("equation_feedback_check_fail_missing_moments");
                 return false;
             }
         }
 
         for (Moment moment : allMoments) {
 
             Term term = getTerm(moment.getVector());
             if (term == null) {
                 Logger.getLogger("Statics").info("check: equation has not added all terms: " + moment.getVector());
                 Logger.getLogger("Statics").info("check: FAILED");
 
                 StaticsApplication.getApp().setAdviceKey("equation_feedback_check_fail_couples");
                 return false;
             }
         }
 
 
         for (Term term : allTerms()) {
             if (!term.check()) {
 
                 Logger.getLogger("Statics").info("check: term does not evaluate correctly: " + term.getCoefficient());
 
                 switch (term.error) {
                     case none:
                         // ??? should not be here
                         Logger.getLogger("Statics").info("check: unknown error?");
                         Logger.getLogger("Statics").info("check: FAILED");
 
                         StaticsApplication.getApp().setAdviceKey("equation_feedback_check_fail_unknown");
                         return false;
                     case badCoefficient:
                         Logger.getLogger("Statics").info("check: bad coefficient");
                         Logger.getLogger("Statics").info("check: FAILED");
 
                         StaticsApplication.getApp().setAdviceKey("equation_feedback_check_fail_momentCoefficient");
                         return false;
                     case parse:
                         Logger.getLogger("Statics").info("check: for " + term.getSource());//.getLabelText());
                         Logger.getLogger("Statics").info("check: parse error");
                         Logger.getLogger("Statics").info("check: FAILED");
 
                         StaticsApplication.getApp().setAdviceKey("equation_feedback_check_fail_parse", term.getCoefficient(), term.getSource().getSymbolName());
                         //"Note: I can't understand your coefficient: \""+term.getCoefficient()+"\"");
                         return false;
                     case incorrect:
                         Logger.getLogger("Statics").info("check: for " + term.getSource());//.getLabelText());
                         Logger.getLogger("Statics").info("check: incorrect value: " + term.coefficientValue);
                         Logger.getLogger("Statics").info("check: should be: " + term.targetValue);
                         Logger.getLogger("Statics").info("check: FAILED");
 
                        StaticsApplication.getApp().setAdviceKey("equation_feedback_check_fail_coefficient", term.getSource().toString());
                         //"Note: Your coefficient is not correct for "+term.getVector().getLabelText());
                         return false;
                 }
             }
         }
 
         setLocked(true);
 
         Logger.getLogger("Statics").info("check: PASSED!");
         StaticsApplication.getApp().setAdviceKey("equation_feedback_check_success");
         return true;
     }
 }
