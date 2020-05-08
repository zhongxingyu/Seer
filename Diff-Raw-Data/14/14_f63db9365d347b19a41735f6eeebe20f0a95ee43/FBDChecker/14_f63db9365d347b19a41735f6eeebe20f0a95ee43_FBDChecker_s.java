 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.gatech.statics.modes.fbd;
 
 import edu.gatech.statics.application.StaticsApplication;
 import edu.gatech.statics.math.Unit;
 import edu.gatech.statics.math.Vector;
 import edu.gatech.statics.math.Vector3bd;
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
 
         if (addedForces.size() <= 0) {
             Logger.getLogger("Statics").info("check: diagram does not contain any forces");
             Logger.getLogger("Statics").info("check: FAILED");
 
             StaticsApplication.getApp().setAdviceKey("fbd_feedback_check_fail_add");
             return false;
         }
 
         // make list for weights, as we will need these later.
         Map<Load, Body> weights = new HashMap<Load, Body>();
 
         List<Load> externalForces = getExternalForces();
 
         // step 2: for vectors that we can click on and add, ie, external added forces,
         // make sure that the user has added all of them.
         for (Load external : externalForces) {
 
             boolean success = false;
             if (external.isSymbol()) {
                success = testReaction(external, addedForces);
                /*Load addedExternal = null;
                for (Load candidate : addedForces) {
                    if (external.equalsSymbolic(candidate) || external.equalsSymbolic(negate(candidate))) {
                        addedExternal = candidate;
                        break;
                    }
                }
                if (addedExternal != null) {
                    success = addedForces.remove(addedExternal);
                }*/
             } else {
                 success = addedForces.remove(external);
             }
 
             if (!success) {
                 //if the code reaches this point there is a problem. this checks
                 //to see if the falure is related to a force added as an external that
                 //was wrong (numerically or symbolically)
                 for (Load addedExt : addedForces) {
                     if (addedExt.getAnchor() == external.getAnchor()) {
                         //An external value that should be symbolic has been added as numeric
                         if (external.isSymbol() && !addedExt.isSymbol()) {
                             Logger.getLogger("Statics").info("check: external value should be a symbol at point" + external.getAnchor().getLabelText());
                             Logger.getLogger("Statics").info("check: FAILED");
 
                             StaticsApplication.getApp().setAdviceKey("fbd_feedback_check_fail_external_symbol", addedExt.getAnchor().getLabelText());
                             return false;
                         }
                         //An external value that should be numeric has been added as symbolic
                         else if (!external.isSymbol() && addedExt.isSymbol()) {
                             Logger.getLogger("Statics").info("check: external value should be a numeric at point" + external.getAnchor().getLabelText());
                             Logger.getLogger("Statics").info("check: FAILED");
 
                             StaticsApplication.getApp().setAdviceKey("fbd_feedback_check_fail_external_number", addedExt.getAnchor().getLabelText());
                             return false;
                         }
                         //An external value was added as numeric, correctly, but was the wrong number
                         else {
                             Logger.getLogger("Statics").info("check: diagram contains incorrect external force " + external);
                             Logger.getLogger("Statics").info("check: FAILED");
 
                             StaticsApplication.getApp().setAdviceKey("fbd_feedback_check_fail_external_value", addedExt.getAnchor().getLabelText());
                             return false;
                         }
                     }
                 }
 
                 //if the above is not the error it is assumed to be because the
                 //user has forgotten to add an external force
                 Logger.getLogger("Statics").info("check: diagram does not contain external force " + external);
                 Logger.getLogger("Statics").info("check: FAILED");
 
                 StaticsApplication.getApp().setAdviceKey("fbd_feedback_check_fail_external", external.getAnchor().getLabelText());
                 return false;
             }
         }
 
         // step 3: Make sure weights exist, and remove them from our addedForces.
         for (Body body : diagram.getBodySubset().getBodies()) {
             if (body.getWeight().getDiagramValue().floatValue() != 0) {
 
                 Load weight = new Force(
                         body.getCenterOfMassPoint(),
                         Vector3bd.UNIT_Y.negate(),
                         new BigDecimal(body.getWeight().doubleValue()));
                 weights.put(weight, body);
                 if (addedForces.contains(weight)) {
                     // still using units here....
                     // this should be changed so the test is independent of magnitude.
                     Logger.getLogger("Statics").info("check: removing weight for " + body);
                     addedForces.remove(weight);
                 } else {
                     //if the code reaches this point there is a problem. this checks
                     //to see if the falure is related to a force added as a weight that
                     //was wrong (numerically or symbolically)
                     for (Load addedWeight : addedForces) {
                         if (addedWeight.getAnchor() == weight.getAnchor()) {
                             //A weight that should be symbolic has been added as numeric
                             if (weight.isSymbol() && !addedWeight.isSymbol()) {
                                 Logger.getLogger("Statics").info("check: weight should be a symbol at point" + weight.getAnchor().getLabelText());
                                 Logger.getLogger("Statics").info("check: FAILED");
 
                                 StaticsApplication.getApp().setAdviceKey("fbd_feedback_check_fail_weight_symbol", addedWeight.getAnchor().getLabelText());
                                 return false;
                             }
                             //A weight that should be numeric has been added as symbolic
                             else if (!weight.isSymbol() && addedWeight.isSymbol()) {
                                 Logger.getLogger("Statics").info("check: weight should be numeric at point" + weight.getAnchor().getLabelText());
                                 Logger.getLogger("Statics").info("check: FAILED");
 
                                 StaticsApplication.getApp().setAdviceKey("fbd_feedback_check_fail_weight_number", addedWeight.getAnchor().getLabelText());
                                 return false;
                             }
                             //A weight was added as numeric, correctly, but was the wrong number
                             else {
                                 Logger.getLogger("Statics").info("check: diagram contains incorrect weight " + weight);
                                 Logger.getLogger("Statics").info("check: FAILED");
 
                                 StaticsApplication.getApp().setAdviceKey("fbd_feedback_check_fail_weight_value", addedWeight.getAnchor().getLabelText());
                                 return false;
                             }
                         }
                     }
                     // weight does not exist in system.
                     Logger.getLogger("Statics").info("check: diagram does not contain weight for " + body);
                     Logger.getLogger("Statics").info("check: weight is: " + weight);
                     Logger.getLogger("Statics").info("check: FAILED");
 
                     StaticsApplication.getApp().setAdviceKey("fbd_feedback_check_fail_weight", weight.getAnchor().getLabelText());
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
 
             StaticsApplication.getApp().setAdviceKey("fbd_feedback_check_fail_additional", addedForces.get(0).getAnchor().getLabelText());
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
 
                     StaticsApplication.getApp().setAdviceKey("fbd_feedback_check_fail_symbol", force.getAnchor().getLabelText());
                     return false;
                 }
 
                 // check for duplication
                 String name = force.getSymbolName();
                 if (names.contains(name)) {
                     Logger.getLogger("Statics").info("check: user duplicated name for force: " + name);
                     Logger.getLogger("Statics").info("check: FAILED");
 
                     StaticsApplication.getApp().setAdviceKey("fbd_feedback_check_fail_duplicate", name);
                     return false;
                 }
                 names.add(name);
 
             } else {
 
                 // force is numeric.
                 // the only forces that WOULD be numeric are weights right now.
                 // make sure the value given is equal to the weight of the object.
 
                 if (weights.containsKey(force)) {
 
                 } else if (externalForces.contains(force)) {
                 // OK, do nothing
 
                 } else {
                     Logger.getLogger("Statics").info("check: force should not be numeric: " + force);
                     Logger.getLogger("Statics").info("check: FAILED");
 
                     StaticsApplication.getApp().setAdviceKey("fbd_feedback_check_fail_numeric", force.getAnchor().getLabelText());
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
         if (reaction instanceof Force) {
             return new Force(reaction.getAnchor(), reaction.getVector().negate());
         } else if (reaction instanceof Moment) {
             return new Moment(reaction.getAnchor(), reaction.getVector().negate());
         } else {
             // ignore this case
             return null;
         }
     }
 
     private boolean testReaction(Load reaction, List<Load> addedForces) {
 
         // the equality check on Load requires that we ignore the symbol name
         
         Load equivalent = null;
         for(Load candidate : addedForces) {
             if(candidate.equalsSymbolic(reaction))
                 equivalent = candidate;
         }
         
         if(equivalent != null) {
             addedForces.remove(equivalent);
             return true;
         }
 
         return false;
     }
 }
