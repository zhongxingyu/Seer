 /*
  * AppletLauncher.java
  *
  * Created on August 11, 2007, 10:07 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 package edu.gatech.statics.application;
 
 import edu.gatech.statics.exercise.Exercise;
 import edu.gatech.statics.exercise.persistence.StateIO;
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.logging.Level;
 import javax.swing.JOptionPane;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class AppletLauncher extends StaticsApplet {
 
     /** Creates a new instance of AppletLauncher */
     public AppletLauncher() {
     }
 
     @Override
     public void init() {
 
         try {
             getApplication().setApplet(true);
 
             String exerciseName = getExercise();
             Class exerciseClass = Class.forName(exerciseName);
             final Exercise exercise = (Exercise) exerciseClass.newInstance();
 
             configureAppletData(exercise);
 
             StaticsApplication.logger.info("Exercise loaded!");
 
             super.init();
             getApplication().setExercise(exercise);
 
         } catch (NullPointerException ex) {
             StaticsApplication.logger.log(Level.SEVERE, "Could not load exercise", ex);
             showError();
         } catch (ClassNotFoundException ex) {
             StaticsApplication.logger.log(Level.SEVERE, "Could not load exercise", ex);
             showError();
         } catch (ClassCastException ex) {
             StaticsApplication.logger.log(Level.SEVERE, "Could not load exercise", ex);
             showError();
         } catch (IllegalAccessException ex) {
             StaticsApplication.logger.log(Level.SEVERE, "Could not load exercise", ex);
             showError();
         } catch (InstantiationException ex) {
             StaticsApplication.logger.log(Level.SEVERE, "Could not load exercise", ex);
             showError();
         }
     }
 
     private void showError() {
         JOptionPane.showMessageDialog(this,
                 "An error occurred in loading the exercise. Please refresh the page and try again.",
                 "Could not load exercise", JOptionPane.ERROR_MESSAGE);
     }
 
     /**
      * Attempts to load the state specified by the parameters of the applet. If the
      * state does not exist, then the applet continues normally. If there is an error in loading the state,
      * the applet will continue normally as well and report an exception in the log.
      */
     @Override
     protected void setupState() {
         super.setupState();
 
         String exerciseState = getParameter("exerciseState");
         if (exerciseState != null && !exerciseState.trim().equals("")) {
             StaticsApplication.logger.info("Loading state...");
             try {
                 StateIO.loadState(exerciseState);
                 StaticsApplication.logger.info("Finished loading state!");
             } catch (Exception ex) {
                 StaticsApplication.logger.log(Level.SEVERE, "Loading state failed. Continuing...", ex);
             }
         }
     }
 
     private void configureAppletData(Exercise exercise) {
 
         String userIDString = getParameter("userID");
         String assignmentIDString = getParameter("assignmentID");
         String problemIDString = getParameter("problemID");
         String problemName = getParameter("problemName");
 
 
         // by default, have graded mode be true.
         // if testing mode is set, let graded be false.
         // if the testing mode is set, then the user id is set to -1.
        if (!"-1".equals(userIDString) && !"0".equals(userIDString)) {
             getApplication().setGraded(true);
         }
 
         if (userIDString == null || assignmentIDString == null) {
             StaticsApplication.logger.info("Applet loaded, but user ID not recorded. Continuing anonymously.");
             return;
         }
         StaticsApplication.logger.info("Applet loaded, user ID recorded. Continuing for credit.");
 
         if (!checkVerifierKey() && getApplication().isGraded()) {
             // Only complain about the verifier failure if the problem is graded.
             StaticsApplication.logger.severe("Verifier key does not check!! This is a problem. Continuing anonymously.");
 
             JOptionPane.showMessageDialog(this,
                     "There has been an error in decoding the state for the problem. The exercise will continue anonymously. "
                     + "Please contact support if this is in error!",
                     "Uh oh", JOptionPane.ERROR_MESSAGE);
 
         } else {
 
             int exerciseID = Integer.valueOf(assignmentIDString);
             int userID = Integer.valueOf(userIDString);
             int problemID = Integer.valueOf(problemIDString);
 
             exercise.setAppletExerciseName(problemName);
             exercise.setProblemID(problemID);
             exercise.getState().setAssignmentID(exerciseID);
             exercise.getState().setUserID(userID);
         }
     }
 
     private boolean checkVerifierKey() {
         String userIDString = getParameter("userID");
         //String assignmentIDString = getParameter("assignmentID");
         String problemIDString = getParameter("problemID");
         String problemName = getParameter("problemName");
         String exerciseState = getParameter("exerciseState");
         String givenVerifierKey = getParameter("verifierKey");
 
         // change state to empty if it is null.
         exerciseState = exerciseState == null ? "" : exerciseState;
 
         String preHash = userIDString + ":" + problemIDString + /*":" + assignmentIDString +*/ ":" + problemName + ":" + exerciseState.replaceAll("\\s", "");
 
         // use MD5 to generate our key
         MessageDigest md5;
         try {
             md5 = MessageDigest.getInstance("MD5");
         } catch (NoSuchAlgorithmException ex) {
             // if this fails, we're in a bit of a snit.
             StaticsApplication.logger.log(Level.SEVERE, "cannot find an MD5 digest!", ex);
             return false;
         }
 
         byte[] digestBytes;
         try {
             digestBytes = md5.digest(preHash.getBytes("UTF-8"));
         } catch (UnsupportedEncodingException ex) {
             StaticsApplication.logger.log(Level.SEVERE, "cannot encode into UTF-8!", ex);
             return false;
         }
 
         String verifierKey = String.format("%02x%02x%02x%02x",
                 digestBytes[0], digestBytes[1], digestBytes[2], digestBytes[3]);
 
         return verifierKey.equals(givenVerifierKey);
     }
 }
