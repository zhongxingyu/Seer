 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package seesaw;
 
 import edu.gatech.statics.exercise.ExerciseLogger;
 import edu.gatech.statics.exercise.ExerciseUtilities;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class SeeSawExerciseGraded extends SeeSawExercise {
 
     //private ExerciseLogger logger;
 
     public SeeSawExerciseGraded() {
         //logger = new ExerciseLogger("loggerPost.php");
         //logger.scheduleLogger();
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
