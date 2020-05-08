 /*
  * PurseExerciseGraded.java
  *
  * Created on Oct 23, 2007, 4:00:47 PM
  *
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package example01;
 
 import com.jme.system.DisplaySystem;
 import com.jmex.bui.BButton;
 import com.jmex.bui.BContainer;
 import com.jmex.bui.BLabel;
 import com.jmex.bui.BTextField;
 import com.jmex.bui.BWindow;
 import com.jmex.bui.event.ActionEvent;
 import com.jmex.bui.event.ActionListener;
 import com.jmex.bui.layout.BorderLayout;
 import com.jmex.bui.layout.GroupLayout;
 import com.jmex.bui.util.Dimension;
 import edu.gatech.statics.application.StaticsApplet;
 import edu.gatech.statics.application.StaticsApplication;
 import edu.gatech.statics.application.ui.AppWindow;
 import edu.gatech.statics.application.ui.ModalPopupWindow;
 import edu.gatech.statics.tasks.SolveJointTask;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Random;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class PurseExerciseGraded extends PurseExercise {
 
     private String studentName;
 
     public PurseExerciseGraded() {
 
         Random rand = new Random();
         
         handPoint = -17 + (float)rand.nextInt(20)/10 - 1;
         tendonAnchorB = 13 + (float)rand.nextInt(20)/10;
         tendonAnchorD = 13 + (float)rand.nextInt(20)/10 - 1;
         shoulderHeight = 16 + -(float)rand.nextInt(10)/10;
         forearmWeight = 9 + (float)rand.nextInt(20)/10 - 1;
         purseWeight = 19.6f + (float)rand.nextInt(20)/10 - 1;
         
     }
     
     public void initExercise() {
         super.initExercise();
         
         setDescription(
                "Here is a simplified version of the human arm. " +
                 "Please solve for the reactions at each of the points: B, C, and E." +
                 "The weight of the forearm is "+forearmWeight+" N, and the weight of the purse is "+purseWeight+" N."
                 );
     }
     
     public void loadExercise() {
         super.loadExercise();
         
         addTask(new SolveJointTask(jointB));
         addTask(new SolveJointTask(jointC));
         addTask(new SolveJointTask(jointE));
     }
 
     public void postLoadExercise() {
         showNamePopup();
         showSubmitButton();
     }
     
     private void showSubmitButton() {
         BWindow submitWindow = new AppWindow(new BorderLayout());
         submitWindow.add(new BButton("Submit", new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                 showCompletionPopup();
             }
         }, "submit"), BorderLayout.CENTER);
         StaticsApplication.getApp().getRootInterface().getBuiNode().addWindow(submitWindow);
         submitWindow.pack();
         submitWindow.setLocation(
                 DisplaySystem.getDisplaySystem().getWidth()-submitWindow.getWidth(),
                 DisplaySystem.getDisplaySystem().getHeight()-submitWindow.getHeight());
     }
     
     private void showNamePopup() {
     
         final ModalPopupWindow popup = new ModalPopupWindow(StaticsApplication.getApp().getCurrentInterface().getToolbar(), new BorderLayout());
         popup.setStyleClass("info_window_opaque");
         popup.setModal(true);
 
         popup.setPreferredSize(300, -1);
 
         BContainer title = new BContainer(new BorderLayout());
         BLabel titleLabel = new BLabel("Before we start...", "title_container");
         title.add(titleLabel, BorderLayout.CENTER);
         popup.add(title, BorderLayout.NORTH);
 
         GroupLayout contentLayout = GroupLayout.makeVert(GroupLayout.TOP);
         contentLayout.setOffAxisJustification(GroupLayout.LEFT);
 
         BContainer content = new BContainer(contentLayout);
         content.setStyleClass("content_container");
         popup.add(content, BorderLayout.CENTER);
 
        content.add(new BLabel("Please give your name so you can be given credit for this exercise."));
 
         BContainer nameRow = new BContainer(GroupLayout.makeHoriz(GroupLayout.LEFT));
         content.add(nameRow);
         nameRow.add(new BLabel("Name: "));
         final BTextField nameField = new BTextField();
         nameField.setPreferredWidth(200);
         nameRow.add(nameField);
 
         ActionListener actionListener = new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                 String name = nameField.getText();
                 if(name.trim().equals(""))
                     return;
                 studentName = name;
                 popup.dismiss();
             }
         };
         
         BButton okButton = new BButton("OK", actionListener, "ok");
         content.add(okButton);
         nameField.addListener(actionListener);
         nameField.requestFocus();
         
         Dimension dim = new Dimension(300, 0);
         popup.popup(0, 0, true);
         popup.center();
     }
 
     /*public boolean isExerciseSolved() {
         for (SimulationObject obj : getWorld().allObjects())
             if (obj instanceof Joint && !((Joint) obj).isSolved() && !((Joint) obj).getPoint().getName().equals("D"))
                 return false;
         return true;
     }*/
 
     public void finishExercise() {
         super.finishExercise();
         showCompletionPopup();
     }
     
     private void showCompletionPopup() {
         
         final ModalPopupWindow popup = new ModalPopupWindow(
                 StaticsApplication.getApp().getCurrentInterface().getToolbar(),
                 new BorderLayout(5, 5));
         popup.setStyleClass("info_window_opaque");
         popup.setModal(true);
 
         popup.setPreferredSize(300, -1);
 
         BContainer title = new BContainer(new BorderLayout());
         BLabel titleLabel = new BLabel("Submit Assignment", "title_container");
         title.add(titleLabel, BorderLayout.CENTER);
         popup.add(title, BorderLayout.NORTH);
         
         BLabel textLabel;
         
         if(isExerciseFinished()) {
             textLabel = new BLabel("CONGRATULATIONS! You have solved for all of the unknown joints in this exercise. " +
                     "Please click the button below to submit your work.");
         } else {
             textLabel = new BLabel("You can submit your exercise for full extra credit now, " +
                     "But you have not finished the exercise. Are you sure you want to submit?");
         }
         
         BContainer textContainer = new BContainer(new BorderLayout());
         textContainer.setStyleClass("padded_container");
         
         textContainer.add(textLabel, BorderLayout.CENTER);
         popup.add(textContainer, BorderLayout.CENTER);
         
         ActionListener actionListener = new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                if(event.getAction().equals("submit"))
                     navigateAway();
                else
                     popup.dismiss();
             }
         };
         
         BContainer buttonContainer = new BContainer(new BorderLayout());
         
         BButton submitButton = new BButton("Submit",actionListener,"submit");
         buttonContainer.add(submitButton, BorderLayout.CENTER);
         
         BButton returnButton = new BButton("Go Back",actionListener,"return");
         buttonContainer.add(returnButton, BorderLayout.WEST);
         
         popup.add(buttonContainer, BorderLayout.SOUTH);
         
         Dimension dim = new Dimension(300, 0);
         popup.popup((DisplaySystem.getDisplaySystem().getWidth() - dim.width) / 2, (DisplaySystem.getDisplaySystem().getHeight() - dim.height) / 2, true);
     }
     
     private void navigateAway() {
         
         // this only works if we have an applet...
         if(StaticsApplet.getInstance() == null)
             return;
         
         MessageDigest md5;
         try {
              md5 = MessageDigest.getInstance("MD5");
         } catch (NoSuchAlgorithmException ex) {
             // if this fails, we're in a bit of a snit.
             // should popup a dialog with some message on it??
             return;
         }
         
         String exerciseName = getName();
         if(isExerciseFinished())
             exerciseName += " [Solved]";
         String finalString = studentName + "@" + exerciseName;
         byte[] resultBytes = md5.digest(finalString.getBytes());
         
        String finalCode = String.format("%x%x%x", resultBytes[0], resultBytes[1], resultBytes[2]);
         
         try {
             // format resulting URL:
             String postPage = "problemPost.php";
 
             String formattedName = URLEncoder.encode(studentName, "UTF-8");
             String formattedExercise = URLEncoder.encode(exerciseName, "UTF-8");
 
             String targetPage = postPage+"?name="+formattedName+"&exercise="+formattedExercise+"&code="+finalCode;
             
             // navigate away if we are in an applet now.
             URL url = new URL(StaticsApplet.getInstance().getDocumentBase(), targetPage);
             StaticsApplet.getInstance().getAppletContext().showDocument(url);
             
         } catch (MalformedURLException ex) {
             // shouldn't wind up here, but...
         } catch (UnsupportedEncodingException ex) {
             // we're also in trouble if we wind up here.
             // should do our popup again.
         }
     }
 }
