 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package example01;
 
import edu.gatech.statics.exercise.ExerciseLogger;
 import edu.gatech.statics.exercise.ExerciseUtilities;
 import edu.gatech.statics.math.Unit;
 import java.math.BigDecimal;
 import java.util.Random;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class PurseExerciseGraded2 extends PurseExercise {
 
     //private ExerciseLogger logger;
 
     public PurseExerciseGraded2() {
         //logger = new ExerciseLogger("loggerPost.php");
         //logger.scheduleLogger();
 
         Random rand = new Random();
 
         handPoint = -17 + (float) rand.nextInt(20) / 10 - 1;
         tendonAnchorB = 13 + (float) rand.nextInt(20) / 10;
         tendonAnchorD = 13 + (float) rand.nextInt(20) / 10 - 1;
         shoulderHeight = 16 + -(float) rand.nextInt(10) / 10;
         forearmWeight = 9 + (float) rand.nextInt(20) / 10 - 1;
         purseWeight = 19.6f + (float) rand.nextInt(20) / 10 - 1;
         centerGravityOffset = (float) rand.nextInt(10) / 10 - .5f;
 
     }
 
     @Override
     public void initExercise() {
         super.initExercise();
 
         BigDecimal bdForearmWeight = new BigDecimal(forearmWeight).setScale(Unit.force.getDecimalPrecision(), BigDecimal.ROUND_HALF_UP);
         BigDecimal bdPurseWeight = new BigDecimal(purseWeight).setScale(Unit.force.getDecimalPrecision(), BigDecimal.ROUND_HALF_UP);
 
         setDescription(
                 "Here is a simplified model of the human arm. " +
                 "Please solve for the reactions at each of the points: B, C, and E. " +
                 "C and E are both pins, but there is a couple due to the shoulder exerting a moment at E. " +
                 "You can treat the bicep (BD) as a cable, but you do not need to build a diagram for it alone. " +
                 "The weight of the forearm is " + bdForearmWeight + " N at G, and the weight of the purse is " + bdPurseWeight + " N at A.");
     }
 
     @Override
     public void onSubmit() {
         trySubmission();
     }
 
     @Override
     public void finishExercise() {
         super.finishExercise();
         trySubmission();
     }
 
     private void trySubmission() {
         boolean navigate = ExerciseUtilities.showCompletionPopup();
         if (navigate) {
             //logger.terminate();
             //ExerciseUtilities.navigateToSubmission(studentName, "problemPost.php");
             ExerciseUtilities.navigateAway("exercises.html");
         }
     }
 }
