 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.gatech.statics.modes.equation.worksheet;
 
 import edu.gatech.statics.application.StaticsApplication;
 import edu.gatech.statics.math.AffineQuantity;
 import edu.gatech.statics.math.AnchoredVector;
 import edu.gatech.statics.math.Unit;
 import edu.gatech.statics.math.Vector3bd;
 import edu.gatech.statics.math.expressionparser.Parser;
 import edu.gatech.statics.modes.equation.EquationDiagram;
 import java.math.BigDecimal;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class EquationMathForces extends EquationMath {
 
     public EquationMathForces(String name, Vector3bd observationDirection, EquationDiagram world) {
         super(name, observationDirection, world);
     }
 
     @Override
     protected TermError checkTerm(AnchoredVector load, String coefficient) {
 
         // get this case out of the way.
         // if this happens, then the user has added a coefficient for a moment 
         // in the force equation.
         if (load.getUnit() == Unit.moment && coefficient != null) {
             return TermError.doesNotBelong;
         }
 
         // check the alignment...
         if (!isLoadAligned(load)) {
             if (coefficient != null) {
                 // the load is not aligned.
                 // complain if the user has added a coefficient
                 return TermError.doesNotBelong;
             } else {
                 // otherwise we should return ok
                 return TermError.none;
             }
         }
 
         // if we get here, then the user should have a coefficient.
         // if this is not the case, then complain.
         if (coefficient == null) {
             return TermError.missedALoad;
         }
 
         // get the coefficient value as an AffineQuantity
         AffineQuantity affineCoefficient = Parser.evaluateSymbol(coefficient);
         if (affineCoefficient == null) {
             return TermError.parse;
         }
 
         // if the coefficient is symbolic, complain
         if (affineCoefficient.isSymbolic()) {
             return TermError.shouldNotBeSymbolic;
         }
 
         // what is our expected value?
         BigDecimal targetValue = load.getVectorValue().dot(getObservationDirection());
         BigDecimal userValue = affineCoefficient.getConstant();
 
         return compareValues(userValue, targetValue);
     }
 
     @Override
     protected float valueComparePrecision() {
        return 0.22f;
     }
 
     @Override
     protected void reportError(TermError error, AnchoredVector load, String coefficient) {
 
         if (error == TermError.doesNotBelong && load.getUnit() == Unit.moment) {
 
             Logger.getLogger("Statics").info("check: equation has unnecessary moment term: " + load);
             Logger.getLogger("Statics").info("check: FAILED");
 
             StaticsApplication.getApp().setAdviceKey("equation_feedback_check_fail_unnecessaryMoment", load.getVector().getPrettyName());
             return;
         }
 
         super.reportError(error, load, coefficient);
     }
 }
