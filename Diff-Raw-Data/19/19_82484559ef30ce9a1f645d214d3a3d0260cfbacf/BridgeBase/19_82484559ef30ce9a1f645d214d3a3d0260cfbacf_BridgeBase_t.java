 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package bridge;
 
 import edu.gatech.statics.exercise.BodySubset;
 import edu.gatech.statics.modes.description.Description;
import edu.gatech.statics.modes.truss.zfm.ZeroForceMember;
import edu.gatech.statics.objects.Body;
import edu.gatech.statics.objects.bodies.Background;
 import edu.gatech.statics.objects.bodies.TwoForceMember;
 import edu.gatech.statics.tasks.Solve2FMTask;
 import edu.gatech.statics.tasks.SolveFBDTask;
 import edu.gatech.statics.tasks.SolveZFMTask;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 /**
  *
  * @author Calvin Ashmore
  */
 abstract public class BridgeBase extends BridgeExercise {
 
     @Override
     public Description getDescription() {
         Description description = super.getDescription();
 
         List<String> toSolve = new ArrayList((Set<String>) getState().getParameter("toSolve"));
 
         String forceQualifier = null;
         if (toSolve.size() == 3) {
             forceQualifier = "three forces ";
         } else if (toSolve.size() == 4) {
             forceQualifier = "four forces ";
         } else {
             forceQualifier = "forces ";
         }
 
         String forces = "";
         for (int i = 0; i < toSolve.size(); i++) {
             if (i > 0) {
                 forces += ", ";
             }
             forces += "<font color=\"red\"><b>" + toSolve.get(i) + "</b></font>";
         }
 
 //        description.setGoals(
 //                "Identify the zero force members in the entire structure.  " +
 //                "Create a FBD of the whole truss and solve for the support forces. " +
 //                "Then solve for the three forces __, __, and __ by the method of joints or sections.");
 
         description.setGoals(
                 "Identify the zero force members in the entire structure.  " +
                 "Create a FBD of the whole truss and solve for the support forces. " +
                 "Then solve for the " + forceQualifier + forces + " by the method of joints or sections.");
 
         return description;
     }
 
     @Override
     public void initParameters() {
 
         Set<String> toSolve = selectToSolve();
         getState().setParameter("toSolve", toSolve);
     }
 
     @Override
     public void applyParameters() {
         getTasks().clear();
         
         addTask(new SolveZFMTask("Solve zfms"));
 
        List<Body> allBodies = new ArrayList<Body>();
        for (Body body : getSchematic().allBodies()) {
            if(body instanceof ZeroForceMember || body instanceof Background)
                continue;
            allBodies.add(body);
        }
        BodySubset subset = new BodySubset(allBodies);
         subset.setSpecialName("Whole Truss");
         addTask(new SolveFBDTask("Solve whole truss", subset));
 
 
         Set<String> toSolve = (Set<String>) getState().getParameter("toSolve");
 
         for (String twoForceMemberName : toSolve) {
 
             TwoForceMember twoForceMember = (TwoForceMember) getSchematic().getAllObjectsByName().get("Bar " + twoForceMemberName);
             String taskName = "Solve " + twoForceMember.getName();
             addTask(new Solve2FMTask(taskName, twoForceMember, twoForceMember.getConnector1()));
         }
 
     }
 
     abstract protected Set<String> selectToSolve();
 }
