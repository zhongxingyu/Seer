 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.gatech.statics.modes.fbd;
 
 import com.jme.math.Vector3f;
 import edu.gatech.statics.application.StaticsApplication;
 import edu.gatech.statics.math.Unit;
 import edu.gatech.statics.math.Vector;
 import edu.gatech.statics.objects.Body;
 import edu.gatech.statics.objects.Force;
 import edu.gatech.statics.objects.Joint;
 import edu.gatech.statics.objects.Load;
 import edu.gatech.statics.objects.Moment;
 import edu.gatech.statics.objects.SimulationObject;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class FBDChecker {
 
     private FreeBodyDiagram diagram;
 
     public FBDChecker(FreeBodyDiagram diagram) {
         this.diagram = diagram;
     }
 
     private List<Load> getAddedForces() {
         List<Load> addedForces = new ArrayList<Load>();
         for (SimulationObject obj : diagram.allObjects()) {
             if (!(obj instanceof Load)) {
                 continue;
             }
             //if (obj.isGiven()) {
             //    continue;
             //}
 
             // this force has been added, and is not a given that could have been selected.
             addedForces.add((Load) obj);
         }
         return addedForces;
     }
 
     private List<Load> getExternalForces() {
         List<Load> externalForces = new ArrayList<Load>();
         for (Body body : FreeBodyDiagram.getSchematic().allBodies()) {
             if (diagram.getBodySubset().getBodies().contains(body)) {
                 for (SimulationObject obj : body.getAttachedObjects()) {
                     if (obj instanceof Load) {
                         externalForces.add((Load) obj);
                     }
                 }
             }
         }
         return externalForces;
     }
 
     public boolean checkDiagram() {
 
         // step 1: assemble a list of all the forces the user has added.
         List<Load> addedForces = getAddedForces();
         Logger.getLogger("Statics").info("check: user added forces: " + addedForces);
 
         // make list for weights, as we will need these later.
         Map<Load, Body> weights = new HashMap<Load, Body>();
 
         List<Load> externalForces = getExternalForces();
 
         // step 2: for vectors that we can click on and add, ie, external added forces,
         // make sure that the user has added all of them.
         for (Load external : externalForces) {
 
             boolean success = false;
             if (external.isSymbol()) {
                 Load addedExternal = null;
                 for (Load candidate : addedForces) {
                     if (external.equalsSymbolic(candidate)) {
                         addedExternal = candidate;
                         break;
                     }
                 }
                 if (addedExternal != null) {
                     success = addedForces.remove(addedExternal);
                 }
             } else {
                 success = addedForces.remove(external);
             }
 
             if (!success) {
                 Logger.getLogger("Statics").info("check: diagram does not contain external force " + external);
                 Logger.getLogger("Statics").info("check: FAILED");
 
                 StaticsApplication.getApp().setAdviceKey("fbd_feedback_check_fail_external");
                 return false;
             }
         }
 
         // step 3: Make sure weights exist, and remove them from our addedForces.
         for (Body body : diagram.getBodySubset().getBodies()) {
             if (body.getWeight().doubleValue() != 0) {
                 
                 Load weight = new Force(
                         body.getCenterOfMassPoint(),
                         new Vector3f(0,-1,0),
                         new BigDecimal(body.getWeight().doubleValue()));
                 weights.put(weight, body);
                 if (addedForces.contains(weight)) {
                     // still using units here....
                     // this should be changed so the test is independent of magnitude.
                     Logger.getLogger("Statics").info("check: removing weight for " + body);
                     addedForces.remove(weight);
                 } else {
                     // weight does not exist in system.
                     Logger.getLogger("Statics").info("check: diagram does not contain weight for " + body);
                     Logger.getLogger("Statics").info("check: weight is: " + weight);
                     Logger.getLogger("Statics").info("check: FAILED");
 
                     StaticsApplication.getApp().setAdviceKey("fbd_feedback_check_fail_weight");
                     return false;
                 }
             }
         }
 
         // Step 4: go through all the border joints connecting this FBD to the external world,
         // and check each force implied by the joint.
         //List<Pair<Joint, Body>> jointsAndBodies = new ArrayList(); // joints between system and external.
         for (SimulationObject obj : diagram.allObjects()) {
             if (!(obj instanceof Joint)) {
                 continue;
             }
 
             Joint joint = (Joint) obj;
 
             Body body = null;
             if (diagram.getBodySubset().getBodies().contains(joint.getBody1())) {
                 body = joint.getBody1();
             }
             if (diagram.getBodySubset().getBodies().contains(joint.getBody2())) {
                 body = joint.getBody2();
             }
 
             // ^ is java's XOR operator
             // we want the joint IF it connects a body in the body list
             // to a body that is not in the body list. This means xor.
             if (!(diagram.getBodySubset().getBodies().contains(joint.getBody1()) ^
                     diagram.getBodySubset().getBodies().contains(joint.getBody2()))) {
                 continue;
             }
 
             // actually, this is not done in the current implementation, so don't remove them otherwise chaos ensues.
             //if (joint.isSolved()) {
             // this particular joint has been solved for in another FBD, its influence should be here already,
             // so just pass it by for now.
             //    addedForces.removeAll(joint.getReactions(body));
             //    continue;
             //}
 
             //jointsAndBodies.add(new Pair(joint, body));
             List<Load> reactions = getReactions(joint, joint.getReactions(body));
 
             Logger.getLogger("Statics").info("check: testing joint: " + joint);
             for (Load reaction : reactions) {
 
                 if (joint.isForceDirectionNegatable()) {
                     if (!testReaction(reaction, addedForces) &&
                             !testReaction(negate(reaction), addedForces)) {
                         Logger.getLogger("Statics").info("check: diagram missing reaction force: " + reaction);
                         Logger.getLogger("Statics").info("check:               or negated force: " + negate(reaction));
                         Logger.getLogger("Statics").info("check: note: reaction is negatable");
                         Logger.getLogger("Statics").info("check: FAILED");
 
                         StaticsApplication.getApp().setAdviceKey("fbd_feedback_check_fail_reaction");
                         return false;
                     }
                 } else {
                     if (!testReaction(reaction, addedForces)) {
                         Logger.getLogger("Statics").info("check: diagram missing reaction force: " + reaction);
                         Logger.getLogger("Statics").info("check: FAILED");
 
                         StaticsApplication.getApp().setAdviceKey("fbd_feedback_check_fail_reaction");
                         return false;
                     }
                 }
 
             }
         }
 
         // Step 5: Make sure we've used all the user added forces.
         if (!addedForces.isEmpty()) {
             Logger.getLogger("Statics").info("check: user added more forces than necessary: " + addedForces);
             Logger.getLogger("Statics").info("check: FAILED");
 
             StaticsApplication.getApp().setAdviceKey("fbd_feedback_check_fail_additional");
             return false;
         }
 
         // Step 6: Verify labels
         // verify that all unknowns are symbols
         // these are reaction forces and moments
         // knowns should not be symbols: externals, weights
         // symbols must also not be repeated, unless this is valid somehow? (not yet)
 
         addedForces = getAddedForces();
         List<String> names = new ArrayList<String>();
 
         // go through each force that the user has added
         for (Load force : addedForces) {
             if (force.isSymbol()) {
 
                 // ignore solved forces
                 if (force.isKnown()) {
                     continue;
                 }
 
                 // should force be a symbol?
                 // the only forces that should not be symbols now are weights.
                 // we probably need some sort of means for identifying this later on...
 
                 if (weights.values().contains(force)) {
                     Logger.getLogger("Statics").info("check: force should not be symbol: " + force);
                     Logger.getLogger("Statics").info("check: FAILED");
 
                     StaticsApplication.getApp().setAdviceKey("fbd_feedback_check_fail_symbol");
                     return false;
                 }
 
                 // check for duplication
                 String name = force.getSymbolName();
                 if (names.contains(name)) {
                     Logger.getLogger("Statics").info("check: user duplicated name for force: " + name);
                     Logger.getLogger("Statics").info("check: FAILED");
 
                     StaticsApplication.getApp().setAdviceKey("fbd_feedback_check_fail_duplicate");
                     return false;
                 }
                 names.add(name);
 
             } else {
 
                 // force is numeric.
                 // the only forces that WOULD be numeric are weights right now.
                 // make sure the value given is equal to the weight of the object.
 
                 if (weights.containsKey(force)) {
 
                     /*
                     // check each body because lazy
                     //boolean checked = false;
                     Body body = weights.get(force);
 
                     //for(Body body : getObservedBodies()) {
 
                     //if(force.getAnchor() == body.getCenterOfMassPoint()) {
                     //    checked = true;
 
                     // will this ever happen, given that the weight Load has a magnitude now?
                     double weight = body.getWeight().doubleValue();
                     //if (force.doubleValue() != weight) {
                     if(!body.getWeight().equals(force.getVector().getQuantity())) {
                         Logger.getLogger("Statics").info("check: weight value incorrect: " + force.doubleValue() + " != " + weight);
                         Logger.getLogger("Statics").info("check: FAILED");
 
                         StaticsApplication.getApp().setAdviceKey("fbd_feedback_check_fail_wrongWeight");
                         return false;
                     }*/
                 //}
                 //}
 
                 /*if(!checked) {
                 Logger.getLogger("Statics").info("check: unknown error, weight winks out of existence: "+force);
                 Logger.getLogger("Statics").info("check: FAILED");
                 StaticsApplication.getApp().setAdvice(
                 java.util.ResourceBundle.getBundle("rsrc/Strings").getString("fbd_feedback_check_fail_unknown"));
                 return false;
                 }*/
 
                 } else if (externalForces.contains(force)) {
                 // OK, do nothing
 
                 } else {
                     Logger.getLogger("Statics").info("check: force should not be numeric: " + force);
                     Logger.getLogger("Statics").info("check: FAILED");
 
                     StaticsApplication.getApp().setAdviceKey("fbd_feedback_check_fail_numeric");
                     return false;
                 }
             }
         }
 
         // Yay, we've passed the test!
         Logger.getLogger("Statics").info("check: PASSED!");
         return true;
     }
 
     private List<Load> getReactions(Joint joint, List<Vector> reactions) {
         List<Load> loads = new ArrayList<Load>();
         for (Vector vector : reactions) {
             if (vector.getUnit() == Unit.force) {
                 loads.add(new Force(joint.getPoint(), vector));
             } else if (vector.getUnit() == Unit.moment) {
                 loads.add(new Moment(joint.getPoint(), vector));
             }
         }
         return loads;
     }
 
     private Load negate(Load reaction) {
        return new Load(reaction.getAnchor(), reaction.getVector().negate());
     }
 
     private boolean testReaction(Load reaction, List<Load> addedForces) {
 
         // if the reaction is given to the user, leave it be.
         //if(externalForces.contains(reaction))
         //    return true;
 
         // test and remove reaction in the addedForces
         if (addedForces.contains(reaction)) {
             addedForces.remove(reaction);
             return true;
         }
 
         return false;
     }
 }
