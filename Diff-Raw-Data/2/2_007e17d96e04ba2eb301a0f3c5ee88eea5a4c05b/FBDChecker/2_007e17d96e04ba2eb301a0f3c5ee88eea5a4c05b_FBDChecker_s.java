 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.gatech.statics.modes.fbd;
 
 import edu.gatech.statics.application.StaticsApplication;
 import edu.gatech.statics.exercise.Diagram;
 import edu.gatech.statics.exercise.Exercise;
 import edu.gatech.statics.math.AnchoredVector;
 import edu.gatech.statics.math.Unit;
 import edu.gatech.statics.math.Vector;
 import edu.gatech.statics.math.Vector3bd;
 import edu.gatech.statics.objects.Body;
 import edu.gatech.statics.objects.Connector;
 import edu.gatech.statics.objects.Load;
 import edu.gatech.statics.objects.Measurement;
 import edu.gatech.statics.objects.Point;
 import edu.gatech.statics.objects.SimulationObject;
 import edu.gatech.statics.objects.bodies.Cable;
 import edu.gatech.statics.objects.bodies.TwoForceMember;
 import edu.gatech.statics.objects.connectors.Connector2ForceMember2d;
 import edu.gatech.statics.objects.connectors.Fix2d;
 import edu.gatech.statics.objects.connectors.Pin2d;
 import edu.gatech.statics.util.Pair;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class FBDChecker {
 
     private FreeBodyDiagram diagram;
     //private Joint nextJoint;
     //private boolean done = false;
     private boolean verbose = true;
 
     protected FreeBodyDiagram getDiagram() {
         return diagram;
     }
 
     public FBDChecker(FreeBodyDiagram diagram) {
         this.diagram = diagram;
     }
 
     /**
      * Get all of the symbolic measurements in the schematic, for making sure their names
      * do are not being used for AnchoredVectors.
      * @return
      */
     private List<Measurement> getSymbolicMeasurements() {
         List<Measurement> m = new ArrayList<Measurement>();
         for (SimulationObject obj : FreeBodyDiagram.getSchematic().allObjects()) {
             if (obj instanceof Measurement && ((Measurement) obj).isSymbol()) {
                 m.add((Measurement) obj);
             }
         }
         return m;
     }
 
     /**
      * The verbose flag lets the checker know whether to report information on failure.
      * Verbose output will report both information to the logger and to the advice box.
      * @param enable
      */
     public void setVerbose(boolean enable) {
         verbose = enable;
     }
 
     /**
      * Get all the points in the schematic, to check against for force names.
      * @return
      */
     private List<Point> getAllPoints() {
         List<Point> m = new ArrayList<Point>();
         for (SimulationObject obj : FreeBodyDiagram.getSchematic().allObjects()) {
             if (obj instanceof Point) {
                 m.add((Point) obj);
             }
         }
         return m;
     }
 
     /**
      * Get the given AnchoredVectors that are present in the diagram.
      * The givens are AnchoredVectors present in the schematic, and should be added to the diagram
      * by the user in the FBD. Givens are first looked up in the symbol manager to 
      * see if a stored symbol has been used.
      * @return
      */
     private List<AnchoredVector> getGivenLoads() {
 
         List<AnchoredVector> givenLoads = new ArrayList<AnchoredVector>();
         // look through everything in the schematic, we want to pick out the loads
         // on the correct bodies.
         for (Body body : FreeBodyDiagram.getSchematic().allBodies()) {
             if (diagram.getBodySubset().getBodies().contains(body)) {
                 for (SimulationObject obj : body.getAttachedObjects()) {
                     if (obj instanceof Load) {
 
                         Load given = (Load) obj;
 
                         // attempt to find an equivalent that might have been stored in the symbol manager.
                         AnchoredVector symbolEquivalent = Exercise.getExercise().getSymbolManager().getLoad(given.getAnchoredVector());
                         if (symbolEquivalent != null) {
                             givenLoads.add(symbolEquivalent);
                         } else {
                             givenLoads.add(given.getAnchoredVector());
                         }
                     }
                 }
             }
         }
         return givenLoads;
     }
 
     private void logInfo(String info) {
         if (verbose) {
             Logger.getLogger("Statics").info(info);
         }
     }
 
     private void setAdviceKey(String key, Object... parameters) {
         if (verbose) {
             StaticsApplication.getApp().setAdviceKey(key, parameters);
         }
     }
 
     public boolean checkDiagram() {
 
         //done = false;
 
         // step 1: assemble a list of all the forces the user has added.
         List<AnchoredVector> addedLoads = new ArrayList<AnchoredVector>(diagram.getCurrentState().getAddedLoads());
 
         logInfo("check: user added AnchoredVectors: " + addedLoads);
 
         if (addedLoads.size() <= 0) {
             logInfo("check: diagram does not contain any AnchoredVectors");
             logInfo("check: FAILED");
 
             setAdviceKey("fbd_feedback_check_fail_add");
             return false;
         }
 
         // step 2: for vectors that we can click on and add, ie, given added forces,
         // make sure that the user has added all of them.
         for (AnchoredVector given : getGivenLoads()) {
             boolean ok = performGivenCheck(addedLoads, given);
             if (!ok) {
                 return false;
             }
         }
 
         // step 3: Make sure weights exist, and remove them from our addedForces.
         for (Body body : diagram.getBodySubset().getBodies()) {
             if (body.getWeight().getDiagramValue().floatValue() == 0) {
                 continue;
             }
             AnchoredVector weight = new AnchoredVector(
                     body.getCenterOfMassPoint(),
                     new Vector(Unit.force, Vector3bd.UNIT_Y.negate(),
                     new BigDecimal(body.getWeight().doubleValue())));
 
             boolean ok = performWeightCheck(addedLoads, weight, body);
             if (!ok) {
                 return false;
             }
         }
 
         // Step 4: go through all the border connectors connecting this FBD to the external world,
         // and check each AnchoredVector implied by the connector.
         for (int i = 0; i < diagram.allObjects().size(); i++) {
             SimulationObject obj = diagram.allObjects().get(i);
             if (!(obj instanceof Connector)) {
                 continue;
             }
 
             Connector connector = (Connector) obj;
 
             // find the body in this diagram to which the connector is attached.
             Body body = null;
             if (diagram.allBodies().contains(connector.getBody1())) {
                 body = connector.getBody1();
             }
             if (diagram.allBodies().contains(connector.getBody2())) {
                 body = connector.getBody2();
             }
 
             // ^ is java's XOR operator
             // we want the joint IF it connects a body in the body list
             // to a body that is not in the body list. This means xor.
             if (!(diagram.getBodySubset().getBodies().contains(connector.getBody1()) ^
                     diagram.getBodySubset().getBodies().contains(connector.getBody2()))) {
                 continue;
             }
 
             // build a list of the AnchoredVectors at this point
             List<AnchoredVector> userAnchoredVectorsAtConnector = new ArrayList<AnchoredVector>();
             for (AnchoredVector AnchoredVector : addedLoads) {
                 if (AnchoredVector.getAnchor().equals(connector.getAnchor())) {
                     userAnchoredVectorsAtConnector.add(AnchoredVector);
                 }
             }
 
             logInfo("check: testing connector: " + connector);
 
             // special case, userAnchoredVectorsAtConnector is empty:
             if (userAnchoredVectorsAtConnector.isEmpty()) {
                 logInfo("check: have any forces been added");
                 logInfo("check: FAILED");
                 setAdviceKey("fbd_feedback_check_fail_joint_reaction", connector.connectorName(), connector.getAnchor().getLabelText());
                 return false;
             }
 
 //            //this is trying to make sure two force members have the same values at either end
 //            if (body instanceof TwoForceMember) {
 //                List<AnchoredVector> userAnchoredVectorsAtOtherConnector = new ArrayList<AnchoredVector>();
 //                Connector con;
 //                if (((TwoForceMember) body).getConnector1() == connector) {
 //                    con = ((TwoForceMember) body).getConnector2();
 //                } else {
 //                    con = ((TwoForceMember) body).getConnector1();
 //                }
 //                for (AnchoredVector AnchoredVector : addedAnchoredVectors) {
 //                    if (AnchoredVector.getAnchor().equals(con.getAnchor())) {
 //                        userAnchoredVectorsAtOtherConnector.add(AnchoredVector);
 //                    }
 //                }
 //                if (!userAnchoredVectorsAtConnector.get(0).getLabelText().equalsIgnoreCase(userAnchoredVectorsAtOtherConnector.get(0).getLabelText())) {
 //                    logInfo("check: the user has given a 2ForceMember's AnchoredVectors different values");
 //                    logInfo("check: FAILED");
 //                    setAdviceKey("fbd_feedback_check_fail_2force_not_same");
 //                    return false;                
 //                }
 //            }
 
             ConnectorCheckResult connectorResult = checkConnector(userAnchoredVectorsAtConnector, connector, body);
 
             switch (connectorResult) {
                 case passed:
                     // okay, the check passed without complaint. 
                     // The AnchoredVectors may still not be correct, but that will be tested afterwards.
                     // for now, continue normally.
                     break;
                 case inappropriateDirection:
                     // check for special case of 2FM:
                     logInfo("check: User added AnchoredVectors at " + connector.getAnchor().getName() + ": " + userAnchoredVectorsAtConnector);
                     logInfo("check: Was expecting: " + getReactionAnchoredVectors(connector, connector.getReactions(body)));
 
                     if (connector instanceof Connector2ForceMember2d) {
                         Connector2ForceMember2d connector2fm = (Connector2ForceMember2d) connector;
                         if (connector2fm.getMember() instanceof Cable) {
                             // special message for cables:
                             logInfo("check: user created a cable in compression at point " + connector.getAnchor().getName());
                             logInfo("check: FAILED");
                             setAdviceKey("fbd_feedback_check_fail_joint_cable",
                                     connector.getAnchor().getName(),
                                     connector2fm.getMember());
                             return false;
                         }
                     } else {
                         // one of the directions is the wrong way, and it's not a cable this time
                         // it is probably a roller or something.
                         logInfo("check: AnchoredVectors have wrong direction at point " + connector.getAnchor().getName());
                         logInfo("check: FAILED");
                         setAdviceKey("fbd_feedback_check_fail_some_reverse", connector.getAnchor().getName());
                         return false;
                     }
                 case somethingExtra:
                     // this particular check could be fine
                     // in some problems there are multiple connectors at one point (notably in frame problems)
                     // and this means that extra AnchoredVectors are okay. We check to see if multiple connectors are present,
                     // and if so, continue gracefully, as inapporpriate extra things will be checked at the end
                     // otherwise the check will continue to the next step, "missingSomething" where other conditions
                     // will be tested.
 
                     if (diagram.getConnectorsAtPoint(connector.getAnchor()).size() > 1) {
                         // continue on.
                         break;
                     }
                 case missingSomething:
                     // okay, if we are here then either something is missing, or something is extra.
                     // check against pins or rollers and see what happens.
 
                     logInfo("check: User added AnchoredVectors at " + connector.getAnchor().getName() + ": " + userAnchoredVectorsAtConnector);
                     logInfo("check: Was expecting: " + getReactionAnchoredVectors(connector, connector.getReactions(body)));
 
                     // check if this is mistaken for a pin
                     if (!connector.connectorName().equals("pin")) {
                         Pin2d testPin = new Pin2d(connector.getAnchor());
                         if (checkConnector(userAnchoredVectorsAtConnector, testPin, null) == ConnectorCheckResult.passed) {
                             logInfo("check: user wrongly created a pin at point " + connector.getAnchor().getLabelText());
                             logInfo("check: FAILED");
                             setAdviceKey("fbd_feedback_check_fail_joint_wrong_type", connector.getAnchor().getLabelText(), "pin", connector.connectorName());
                             return false;
                         }
                     }
 
                     // check if this is mistaken for a fix
                     if (!connector.connectorName().equals("fix")) {
                         Fix2d testFix = new Fix2d(connector.getAnchor());
                         if (checkConnector(userAnchoredVectorsAtConnector, testFix, null) == ConnectorCheckResult.passed) {
                             logInfo("check: user wrongly created a fix at point " + connector.getAnchor().getLabelText());
                             logInfo("check: FAILED");
                             setAdviceKey("fbd_feedback_check_fail_joint_wrong_type", connector.getAnchor().getLabelText(), "fix", connector.connectorName());
                             return false;
                         }
                     }
 
                     // otherwise, the user did something strange.
                     logInfo("check: user simply added reactions to a joint that don't make sense to point " + connector.getAnchor().getLabelText());
                     logInfo("check: FAILED");
                     setAdviceKey("fbd_feedback_check_fail_joint_wrong", connector.connectorName(), connector.getAnchor().getLabelText());
                     return false;
             }
 
             // okay, now the connector test has passed.
             // We know now that the AnchoredVectors present in the diagram satisfy the reactions for the connector.
             // All reactions AnchoredVectors are necessarily symbolic, and thus will either be new symbols, or
             // they will be present in the symbol manager.
 
             List<AnchoredVector> expectedReactions = getReactionAnchoredVectors(connector, connector.getReactions(body));
             for (AnchoredVector reaction : expectedReactions) {
                 // get a AnchoredVector and result corresponding to this check.
                 AnchoredVector loadFromSymbolManager = Exercise.getExercise().getSymbolManager().getLoad(reaction);
 
                 if (loadFromSymbolManager != null) {
                     // make sure the directions are pointing the correct way:
                     if (reaction.getVectorValue().equals(loadFromSymbolManager.getVectorValue().negate())) {
                         loadFromSymbolManager = new AnchoredVector(loadFromSymbolManager);
                        loadFromSymbolManager.getVectorValue().negateLocal();
                     }
 
                     // of the user AnchoredVectors, only check those which point in maybe the right direction
                     List<AnchoredVector> userAnchoredVectorsAtConnectorInDirection = new ArrayList<AnchoredVector>();
                     for (AnchoredVector AnchoredVector : userAnchoredVectorsAtConnector) {
                         if (AnchoredVector.getVectorValue().equals(reaction.getVectorValue()) ||
                                 AnchoredVector.getVectorValue().equals(reaction.getVectorValue().negate())) {
                             userAnchoredVectorsAtConnectorInDirection.add(AnchoredVector);
                         }
                     }
 
                     Pair<AnchoredVector, AnchoredVectorCheckResult> result = checkAllCandidatesAgainstTarget(
                             userAnchoredVectorsAtConnectorInDirection, loadFromSymbolManager);
                     AnchoredVector candidate = result.getLeft();
 
                     // this AnchoredVector has been solved for already. Now we can check against it.
                     if (result.getRight() == AnchoredVectorCheckResult.passed) {
                         // check is OK, we can remove the AnchoredVector from our addedAnchoredVectors.
                         addedLoads.remove(candidate);
                     } else {
                         complainAboutAnchoredVectorCheck(result.getRight(), candidate);
                         return false;
                     }
 
                 } else {
                     // this AnchoredVector is new, so it requires a name check.
 
                     // let's find a AnchoredVector that seems to match the expected reaction.
                     AnchoredVector candidate = null;
                     for (AnchoredVector possibleCandidate : userAnchoredVectorsAtConnector) {
                         // we know that these all are at the right anchor, so only test direction.
                         // direction may also be negated, since these are new symbols.
                         if (possibleCandidate.getVectorValue().equals(reaction.getVectorValue()) ||
                                 possibleCandidate.getVectorValue().equals(reaction.getVectorValue().negate())) {
                             candidate = possibleCandidate;
                         }
                     }
 
                     // candidate should not be null at this point since the main test passed.
 
                     NameCheckResult nameResult;
                     if (connector instanceof Connector2ForceMember2d) {
                         nameResult = checkAnchoredVectorName2FM(candidate, (Connector2ForceMember2d) connector);
                     } else {
                         nameResult = checkLoadName(candidate);
                     }
                     if (nameResult == NameCheckResult.passed) {
                         // we're okay!!
                         addedLoads.remove(candidate);
                     } else {
                         complainAboutName(nameResult, candidate);
                         return false;
                     }
                 }
             }
         }
 
         // Step 5: Make sure we've used all the user added forces.
         if (!addedLoads.isEmpty()) {
             logInfo("check: user added more forces than necessary: " + addedLoads);
             logInfo("check: FAILED");
 
             setAdviceKey("fbd_feedback_check_fail_additional", addedLoads.get(0).getAnchor().getName());
             return false;
         }
 
         // Step 6: Verify labels
         // verify that all unknowns are symbols
         // these are reaction forces and moments
         // knowns should not be symbols: externals, weights
         // symbols must also not be repeated, unless this is valid somehow? (not yet)
 
         // Yay, we've passed the test!
         logInfo("check: PASSED!");
         return true;
     }
 
     /**
      * Checks against a given AnchoredVector.
      * The check removes the candidate from addedAnchoredVectors if the check passes.
      * @param addedAnchoredVectors
      * @param given
      * @return
      */
     protected boolean performGivenCheck(List<AnchoredVector> addedAnchoredVectors, AnchoredVector given) {
         List<AnchoredVector> candidates = getCandidates(addedAnchoredVectors, given, given.isSymbol() && !given.isKnown());
 
         // try all candidates
         // realistically there should only be one, but this check tries to be secure.
         Pair<AnchoredVector, AnchoredVectorCheckResult> result = checkAllCandidatesAgainstTarget(candidates, given);
 
         // we have no candidates, so terminate.
         if (result.getRight() == null) {
             //user has forgotten to add a given AnchoredVector
             logInfo("check: diagram does not contain given AnchoredVector " + given);
             logInfo("check: FAILED");
             setAdviceKey("fbd_feedback_check_fail_given", given.getAnchor().getLabelText());
             return false;
         }
 
         AnchoredVector candidate = result.getLeft();
 
         // report failures
         switch (result.getRight()) {
             case passed:
                 // Our test has passed, we can continue.
                 addedAnchoredVectors.remove(candidate);
                 break;
             case shouldNotBeNumeric:
                 //A given value that should be symbolic has been added as numeric
                 logInfo("check: external value should be a symbol at point" + given.getAnchor().getName());
                 logInfo("check: FAILED");
                 setAdviceKey("fbd_feedback_check_fail_given_symbol", candidate.getQuantity().toString(), candidate.getAnchor().getLabelText());
                 return false;
             case shouldNotBeSymbol:
                 //A given value that should be numeric has been added as symbolic
                 logInfo("check: external value should be a numeric at point" + given.getAnchor().getLabelText());
                 logInfo("check: FAILED");
                 setAdviceKey("fbd_feedback_check_fail_given_number", candidate.getQuantity().toString(), candidate.getAnchor().getLabelText());
                 return false;
             case wrongSymbol:
                 // user has given a symbol that does not match the symbol of the given AnchoredVector.
                 // this is generally okay, but we want there to be consistency if the user has already put a name down.
                 if (Exercise.getExercise().getSymbolManager().getLoad(candidate) == null) {
                     // we're okay
                     addedAnchoredVectors.remove(candidate);
                     break;
                 }
             default:
                 complainAboutAnchoredVectorCheck(result.getRight(), candidate);
                 return false;
         }
 
         // user candidate is a symbolic value
         if (candidate.isSymbol() && Exercise.getExercise().getSymbolManager().getLoad(candidate) == null) {
             NameCheckResult nameResult = checkLoadName(candidate);
             if (nameResult != NameCheckResult.passed) {
                 complainAboutName(nameResult, candidate);
                 return false;
             }
         }
         return true;
     }
 
     /**
      * Checks against a weight. This method is very similar to the Given check, 
      * but uses different log and feedback messages. A good way to do the check might be to abstract them out,
      * but the difference is kind of immaterial at this point.
      * The check removes the candidate from addedAnchoredVectors if the check passes.
      * @param addedAnchoredVectors
      * @param given
      * @return
      */
     protected boolean performWeightCheck(List<AnchoredVector> addedAnchoredVectors, AnchoredVector weight, Body body) {
         List<AnchoredVector> candidates = getCandidates(addedAnchoredVectors, weight, weight.isSymbol() && !weight.isKnown());
 
         // try all candidates
         // realistically there should only be one, but this check tries to be secure.
         Pair<AnchoredVector, AnchoredVectorCheckResult> result = checkAllCandidatesAgainstTarget(candidates, weight);
 
         // we have no candidates, so terminate.
         if (result.getRight() == null) {
             // weight does not exist in system.
             logInfo("check: diagram does not contain weight for " + body);
             logInfo("check: weight is: " + weight);
             logInfo("check: FAILED");
 
             setAdviceKey("fbd_feedback_check_fail_weight", body.getName());
             return false;
         }
 
         AnchoredVector candidate = result.getLeft();
 
         // report failures
         switch (result.getRight()) {
             case passed:
                 // Our test has passed, we can continue.
                 addedAnchoredVectors.remove(candidate);
                 break;
             case shouldNotBeNumeric:
                 //A given value that should be symbolic has been added as numeric
                 logInfo("check: weight should be a symbol at point" + weight.getAnchor().getName());
                 logInfo("check: FAILED");
                 setAdviceKey("fbd_feedback_check_fail_weight_symbol", body.getName());
                 return false;
             case shouldNotBeSymbol:
                 //A given value that should be numeric has been added as symbolic
                 logInfo("check: weight should be numeric at point" + weight.getAnchor().getName());
                 logInfo("check: FAILED");
                 setAdviceKey("fbd_feedback_check_fail_weight_number", body.getName());
                 return false;
             case wrongNumericValue:
                 // wrong numeric value
                 logInfo("check: diagram contains incorrect weight " + weight);
                 logInfo("check: FAILED");
                 setAdviceKey("fbd_feedback_check_fail_weight_value", body.getName());
                 return false;
             default:
                 complainAboutAnchoredVectorCheck(result.getRight(), candidate);
                 return false;
         }
 
         // user candidate is a symbolic value
         if (candidate.isSymbol() && Exercise.getExercise().getSymbolManager().getLoad(candidate) == null) {
             NameCheckResult nameResult = checkLoadName(candidate);
             if (nameResult != NameCheckResult.passed) {
                 complainAboutName(nameResult, candidate);
                 return false;
             }
         }
         return true;
     }
 
     private void complainAboutAnchoredVectorCheck(AnchoredVectorCheckResult result, AnchoredVector candidate) {
         switch (result) {
             case shouldNotBeNumeric:
                 logInfo("check: force should not be numeric: " + candidate);
                 logInfo("check: FAILED");
                 setAdviceKey("fbd_feedback_check_fail_numeric", candidate.getUnit().toString(), candidate.getQuantity().toString(), candidate.getAnchor().getName());
                 return;
             case shouldNotBeSymbol:
                 logInfo("check: force should not be symbol: " + candidate);
                 logInfo("check: FAILED");
                 setAdviceKey("fbd_feedback_check_fail_symbol", candidate.getUnit().toString(), candidate.getAnchor().getLabelText(), candidate.getAnchor().getName());
                 return;
             case wrongNumericValue:
                 logInfo("check: numeric values do not match: " + candidate);
                 logInfo("check: FAILED");
                 setAdviceKey("fbd_feedback_check_fail_not_same_number", candidate.getUnit().toString(), candidate.getQuantity().toString(), candidate.getAnchor().getName());
                 return;
             case wrongDirection:
                 logInfo("check: AnchoredVector is pointing the wrong direction: " + candidate);
                 logInfo("check: FAILED");
                 setAdviceKey("fbd_feedback_check_fail_reverse", candidate.getUnit().toString(), candidate.getQuantity().toString(), candidate.getAnchor().getName());
                 return;
             case wrongSymbol:
                 //the student has created a AnchoredVector with a name that doesn't match its opposing force
                 logInfo("check: AnchoredVector should equal its opposite: " + candidate);
                 logInfo("check: FAILED");
                 setAdviceKey("fbd_feedback_check_fail_not_same_symbol", candidate.getUnit().toString(), candidate.getQuantity().toString(), candidate.getAnchor().getName());
                 return;
         }
     }
 
     private void complainAboutName(NameCheckResult result, AnchoredVector candidate) {
         switch (result) {
             case duplicateInThisDiagram:
             case matchesSymbolElsewhere:
                 logInfo("check: forces and moments should not have the same name as any other force or moment: " + candidate);
                 logInfo("check: FAILED");
                 setAdviceKey("fbd_feedback_check_fail_duplicate", candidate.getUnit().toString(), candidate.getAnchor().getLabelText());
                 return;
             case matchesMeasurementSymbol:
                 logInfo("check: force or moment should not share the same name with an unknown measurement ");
                 logInfo("check: FAILED");
                 setAdviceKey("fbd_feedback_check_fail_duplicate_measurement", candidate.getUnit().toString(), candidate.getAnchor().getLabelText());
                 return;
             case matchesPointName:
                 logInfo("check: anchors and added force/moments should not share names");
                 logInfo("check: FAILED");
                 setAdviceKey("fbd_feedback_check_fail_duplicate_anchor", candidate.getUnit().toString(), candidate.getAnchor().getLabelText());
                 return;
             case shouldMatch2FM:
                 //the student has created a 2FM with non matching forces
                 logInfo("check: forces on a 2FM need to have the same name: " + candidate);
                 logInfo("check: FAILED");
                 setAdviceKey("fbd_feedback_check_fail_2force_not_same");
                 return;
         }
     }
 
     protected enum NameCheckResult {
 
         passed, // passed, no conficts
         matchesSymbolElsewhere, // same as a symbolic AnchoredVector from another diagram
         matchesPointName, // same as the name for a point
         matchesMeasurementSymbol, // same as a symbol used in an unknown measurement
         duplicateInThisDiagram, // two AnchoredVectors incorrectly have the same name in this diagram
         shouldMatch2FM // the opposing forces of a 2FM should match
     }
 
     /**
      * This check makes sure this AnchoredVector has a suitable name. The given candidate
      * must be a symbolic AnchoredVector.
      * This check is intended for AnchoredVectors which have *not yet* been added to the symbol manager.
      * This check will go through and make sure the name does not coincide with that of a point or
      * a different AnchoredVector in this diagram or in other diagrams. A special case must be 
      * made with Two Force Members, and for these it is necessary to use checkAnchoredVectorName2FM().
      * @param candidate
      * @return
      */
     protected NameCheckResult checkLoadName(AnchoredVector candidate) {
         String name = candidate.getSymbolName();
 
         for (SimulationObject obj : Diagram.getSchematic().allObjects()) {
             // look through simulation objects to find name conflicts
             // first look at measurements
             if (obj instanceof Measurement) {
                 Measurement measure = (Measurement) obj;
                 if (measure.isSymbol() && measure.getSymbolName().equalsIgnoreCase(name)) {
                     return NameCheckResult.matchesMeasurementSymbol;
                 }
             }
             // then points
             if (obj instanceof Point) {
                 if (name.equalsIgnoreCase(obj.getName())) {
                     return NameCheckResult.matchesPointName;
                 }
             }
         }
 
         // look through other symbols stored in the symbol manager
         if (Exercise.getExercise().getSymbolManager().getSymbols().contains(name)) {
             //the name exists elsewhere in the fbd
             return NameCheckResult.matchesSymbolElsewhere;
         }
 
         // now look through other AnchoredVectors in this diagram.
         for (AnchoredVector load : diagram.getCurrentState().getAddedLoads()) {
             if (load.equals(candidate)) {
                 continue;
             }
             if (candidate.getSymbolName().equalsIgnoreCase(load.getSymbolName())) {
                 return NameCheckResult.duplicateInThisDiagram;
             }
         }
 
         return NameCheckResult.passed;
     }
 
     /**
      * This is an extension of the checkAnchoredVectorName() method, which applies specifically
      * to AnchoredVectors which are reactions to Two Force Members. This method assumes that the candidate provided is 
      * actually the reaction force to the 2fm.
      * @param candidate
      * @param connector
      * @return
      */
     protected NameCheckResult checkAnchoredVectorName2FM(AnchoredVector candidate, Connector2ForceMember2d connector) {
         NameCheckResult result = checkLoadName(candidate);
 
         //if we passed, which we usually want, this means that the AnchoredVectors' labels
         //do not match, which is bad
         if (result == NameCheckResult.passed) {
             for (SimulationObject obj : connector.getMember().getAttachedObjects()) {
                 if (!(obj instanceof Load)) {
                     continue;
                 }
                 if (connector.getMember().containsPoints(candidate.getAnchor(), ((Load) obj).getAnchor())) {
                     return NameCheckResult.shouldMatch2FM;
                 }
             }
         }
 
         // if the result of the standard check is anything but "there is a duplicate in this diagram"
         // then we can return that result. We are only interested in the case where there
         // might be a second AnchoredVector with the same name, which implies a duplicate.
         if (result != NameCheckResult.duplicateInThisDiagram) {
             return result;
         }
 
         TwoForceMember member = connector.getMember();
         if (!diagram.allBodies().contains(member)) {
             return result;
         }
 
         Connector2ForceMember2d otherConnector;
         if (member.getConnector1() == connector) {
             otherConnector = member.getConnector2();
         } else if (member.getConnector2() == connector) {
             otherConnector = member.getConnector1();
         } else {
             // shouldn't get here, but fail gracefully
             return result;
         }
 
         // get the other AnchoredVector that satisfies the reactions of the otherConnector
         List<AnchoredVector> AnchoredVectorsAtOtherReaction = getLoadsAtPoint(otherConnector.getAnchor());
         List<AnchoredVector> otherConnectorReactions = getReactionAnchoredVectors(connector, otherConnector.getReactions());
         AnchoredVector otherReactionTarget = otherConnectorReactions.get(0);
         AnchoredVector otherReaction = null;
         // iterate through the list and look for the one that should do it.
         // we want to find something that could be a reaction on the other end of the 2fm,
         // and we want it to match the name of our current AnchoredVector.
         for (AnchoredVector otherAnchoredVector : AnchoredVectorsAtOtherReaction) {
             if (otherAnchoredVector.getVectorValue().equals(otherReactionTarget.getVectorValue()) ||
                     otherAnchoredVector.getVectorValue().equals(otherReactionTarget.getVectorValue().negate())) {
                 if (candidate.getSymbolName().equalsIgnoreCase(otherAnchoredVector.getSymbolName())) {
                     otherReaction = otherAnchoredVector;
                 }
             }
         }
 
         // okay, we found it. That means that this AnchoredVector should have an appropriate name.
         // in the case that there is some case that is invalid and escapes the above, it should
         // be caught by other parts of the check (for instance, if the user was especially
         // difficult and put two reactions at one end of a 2fm or something like that)
         if (otherReaction != null) {
             return NameCheckResult.passed;
         }
         return result;
     }
 
     /**
      * A convenience method to get all of the loads at a given point. This goes through
      * all of the loads in the current diagram and checks against them.
      * @param point
      * @return
      */
     protected List<AnchoredVector> getLoadsAtPoint(Point point) {
         List<AnchoredVector> loads = new ArrayList<AnchoredVector>();
         //for (SimulationObject obj : allObjects) {
         for (AnchoredVector load : diagram.getCurrentState().getAddedLoads()) {
             if (load.getAnchor().equals(point)) {
                 loads.add(load);
             }
         }
         return loads;
     }
 
     /**
      * Attempts to find a AnchoredVector from a pool of possibilities which might match the target.
      * The search will search for AnchoredVectors that are the same type, are at the same point, and
      * point in the same direction as the target, or the opposite direction, if the 
      * testOpposites flag is checked.
      * @param searchPool 
      * @param target
      * @param testOpposites 
      * @return
      */
     protected List<AnchoredVector> getCandidates(List<AnchoredVector> searchPool, AnchoredVector target, boolean testOpposites) {
         List<AnchoredVector> candidates = new ArrayList<AnchoredVector>();
         for (AnchoredVector AnchoredVector : searchPool) {
             // make sure types are the same
             if (AnchoredVector.getClass() != target.getClass()) {
                 continue;
             }
             // make sure the anchor is the same
             if (!AnchoredVector.getAnchor().equals(target.getAnchor())) {
                 continue;
             }
             // add if direction is the same, or is opposite and the testOpposites flag is set
             if (AnchoredVector.getVectorValue().equals(target.getVectorValue()) ||
                     (testOpposites && AnchoredVector.getVectorValue().negate().equals(target.getVectorValue()))) {
                 candidates.add(AnchoredVector);
             }
         }
         return candidates;
     }
 
     /**
      * This is a result that is returned when a AnchoredVector is checked against some stored version.
      * This works when checking against a given, a weight, or a stored symbolic AnchoredVector.
      */
     protected enum AnchoredVectorCheckResult {
 
         passed, //check passes
         wrongDirection, // occurs when solved value is in wrong direction
         wrongSymbol, // symbol is stored, this should change its symbol
         wrongNumericValue, // number is stored, user put in wrong value
         shouldNotBeSymbol, // store is numeric
         shouldNotBeNumeric // store is symbolic
     }
 
     /**
      * Like checkAnchoredVectorAgainstTarget() this method aims to check whether a AnchoredVector matches the 
      * target. However, this method checks against a collection of candidates, rather than just one.
      * @param candidates
      * @param target
      * @return
      */
     protected Pair<AnchoredVector, AnchoredVectorCheckResult> checkAllCandidatesAgainstTarget(List<AnchoredVector> candidates, AnchoredVector target) {
         AnchoredVectorCheckResult result = null;
         AnchoredVector lastCandidate = null;
         for (AnchoredVector candidate : candidates) {
             lastCandidate = candidate;
             result = checkAnchoredVectorAgainstTarget(candidate, target);
             if (result == AnchoredVectorCheckResult.passed) {
                 return new Pair<AnchoredVector, AnchoredVectorCheckResult>(candidate, result);
             }
         }
         return new Pair<AnchoredVector, AnchoredVectorCheckResult>(lastCandidate, result);
     }
 
     /**
      * Returns a result indicating whether the candidate sufficiently matches the target provided.
      * Target can be a stored symbolic AnchoredVector, a given, or a weight. The target could be known, symbolic, numeric,
      * or what-have-you. The goal of this check is to abstract out some of the detailed checks making sure
      * that candidates are named or valud appropriately and pointing the right direction given
      * other information that might be known about other diagrams.
      * @param candidate
      * @param target
      * @return
      */
     protected AnchoredVectorCheckResult checkAnchoredVectorAgainstTarget(AnchoredVector candidate, AnchoredVector target) {
         if (target.isKnown()) {
             // target is a known AnchoredVector
             // the numeric value must be correct, and the direction must be correct.
             if (!candidate.isKnown()) {
                 // candidate is not known, so complain.
                 return AnchoredVectorCheckResult.shouldNotBeSymbol;
             }
             if (!candidate.getDiagramValue().equals(target.getDiagramValue())) {
                 // the numeric values are off.
                 return AnchoredVectorCheckResult.wrongNumericValue;
             }
             if (!candidate.getVectorValue().equals(target.getVectorValue())) {
                 // pointing the wrong way
                 return AnchoredVectorCheckResult.wrongDirection;
             }
             // this is sufficient for the AnchoredVector to be correct
             return AnchoredVectorCheckResult.passed;
         } else {
             // target is unknown, it must be symbolic
             // the symbol must be correct
             if (candidate.isKnown()) {
                 // candidate is not symbolic, so complain
                 return AnchoredVectorCheckResult.shouldNotBeNumeric;
             }
             if (!candidate.getSymbolName().equalsIgnoreCase(target.getSymbolName())) {
                 // candidate has the wrong symbol name
                 return AnchoredVectorCheckResult.wrongSymbol;
             }
             // we should be okay now.
             return AnchoredVectorCheckResult.passed;
         }
     }
 
     /**
      * This is a result of a check of a connector. This returns *no* information about
      * whether the AnchoredVectors are appropriately valued or named, it merely returns information regarding whether
      * the AnchoredVectors provided could work for the connector.
      */
     protected enum ConnectorCheckResult {
 
         passed, // ok
         missingSomething, // some reaction is missing from the candidates
         somethingExtra, // the candidates have an extra force that is not necessary
         inappropriateDirection,  // one or more of the candidates is the wrong direction for the connector
         // (ie, in a 2 force member, or in a roller)
     }
 
     /**
      * Checks to see whether candidateAnchoredVectors, the list of AnchoredVectors provided, is a suitable match for
      * the reactions of the given connector. This does not check if the AnchoredVectors are named or have
      * the correct names or symbols. This check assumes that all candidateAnchoredVectors are on the connector's anchor.
      * The method will return ConnectorCheckResult.somethingExtra if everything is okay except for there being more
      * AnchoredVectors than expected. Sometimes, this is okay, for instance if there are more than one connector at a point.
      * The check method itself is responsible for identifying these situations and handling them appropriately.
      * @param cadidateAnchoredVectors
      * @param connector
      * @param localBody 
      * @return
      */
     protected ConnectorCheckResult checkConnector(List<AnchoredVector> candidateAnchoredVectors, Connector connector, Body localBody) {
         List<Vector> reactions;
         if (localBody == null) {
             reactions = connector.getReactions();
         } else {
             reactions = connector.getReactions(localBody);
         }
         List<AnchoredVector> reactionAnchoredVectors = getReactionAnchoredVectors(connector, reactions);
 
         boolean negatable = connector.isForceDirectionNegatable();
         for (AnchoredVector reaction : reactionAnchoredVectors) {
             // check each reaction AnchoredVector to make sure it is present and proper
             List<AnchoredVector> candidates = getCandidates(candidateAnchoredVectors, reaction, negatable);
             if (candidates.isEmpty()) {
                 // okay, this one is missing, which is bad.
                 if (!negatable && !getCandidates(candidateAnchoredVectors, reaction, true).isEmpty()) {
                     // candidates allowing negation is not empty, meaning that user is adding 
                     // a AnchoredVector in the wrong direction
                     return ConnectorCheckResult.inappropriateDirection;
                 }
                 return ConnectorCheckResult.missingSomething;
             }
         }
 
         // okay, all reactions are accounted for.
         // if our list of candidateAnchoredVectors is larger, then there may be a problem
         if (candidateAnchoredVectors.size() > reactionAnchoredVectors.size()) {
             return ConnectorCheckResult.somethingExtra;
         }
         // otherwise, we're okay.
         return ConnectorCheckResult.passed;
     }
 
     /**
      * Returns the reactions present from a connector as AnchoredVectors instead of Vectors.
      * This returns a fresh new list, so it does no harm to remove AnchoredVectors from it.
      * @param joint
      * @param reactions
      * @return
      */
     private List<AnchoredVector> getReactionAnchoredVectors(Connector connector, List<Vector> reactions) {
         List<AnchoredVector> loads = new ArrayList<AnchoredVector>();
         for (Vector vector : reactions) {
             loads.add(new AnchoredVector(connector.getAnchor(), vector));
             /*if (vector.getUnit() == Unit.force) {
                 AnchoredVectors.add(new Force(joint.getAnchor(), vector));
             } else if (vector.getUnit() == Unit.moment) {
                 AnchoredVectors.add(new Moment(joint.getAnchor(), vector));
             }*/
         }
         return loads;
     }
 }
