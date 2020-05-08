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
 import com.jmex.bui.event.ActionEvent;
 import com.jmex.bui.event.ActionListener;
 import com.jmex.bui.layout.BorderLayout;
 import com.jmex.bui.layout.GroupLayout;
 import com.jmex.bui.util.Dimension;
 import edu.gatech.statics.application.StaticsApplet;
import edu.gatech.statics.math.Unit;
 import edu.gatech.statics.modes.equation.EquationDiagram;
 import edu.gatech.statics.tasks.SolveJointTask;
 import edu.gatech.statics.ui.components.ModalPopupWindow;
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.List;
 import java.util.Random;
 import java.util.logging.Logger;
 import java.util.logging.SimpleFormatter;
 import java.util.logging.StreamHandler;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class PurseExerciseGraded extends PurseExercise {
 
     private String studentName = "No Name";
     //private StreamHandler streamHandler;
     ByteArrayOutputStream bout;
     StreamHandler streamHandler;
     private int instance; // used for uniqueness of logger data
 
     public PurseExerciseGraded() {
 
         Random rand = new Random();
 
         handPoint = -17 + (float) rand.nextInt(20) / 10 - 1;
         tendonAnchorB = 13 + (float) rand.nextInt(20) / 10;
         tendonAnchorD = 13 + (float) rand.nextInt(20) / 10 - 1;
         shoulderHeight = 16 + -(float) rand.nextInt(10) / 10;
         forearmWeight = 9 + (float) rand.nextInt(20) / 10 - 1;
         purseWeight = 19.6f + (float) rand.nextInt(20) / 10 - 1;
         centerGravityOffset = (float) rand.nextInt(10) / 10 - .5f;
 
         instance = rand.nextInt();
 
         bout = new ByteArrayOutputStream();
         streamHandler = new StreamHandler(bout, new SimpleFormatter());
         Logger.getLogger("").addHandler(streamHandler);
 
     }
 
     @Override
     public void initExercise() {
         super.initExercise();
 
        BigDecimal bdForearmWeight = new BigDecimal(forearmWeight).setScale(Unit.force.getDecimalPrecision(),BigDecimal.ROUND_HALF_UP);
        BigDecimal bdPurseWeight = new BigDecimal(purseWeight).setScale(Unit.force.getDecimalPrecision(),BigDecimal.ROUND_HALF_UP);
        
         setDescription(
                 "Here is a simplified model of the human arm. " +
                "Please solve for the reactions at each of the points: B, C, and E. " +
                "C and E are both pins, but there is a couple due to the shoulder exerting a moment at E. " +
                "You can treat the bicep (BD) as a cable, but you do not need to build a diagram for it alone. " +
                "The weight of the forearm is " + bdForearmWeight + " N at G, and the weight of the purse is " + bdPurseWeight + " N at A.");
     }
 
     @Override
     public void loadExercise() {
         super.loadExercise();
 
         addTask(new SolveJointTask(jointB));
         addTask(new SolveJointTask(jointC));
         addTask(new SolveJointTask(jointE));
     }
 
     @Override
     public void onSubmit() {
         showCompletionPopup();
     }
 
     @Override
     public void postLoadExercise() {
         showNamePopup();
     //showSubmitButton();
     }
 
     private void showNamePopup() {
 
         final ModalPopupWindow popup = new ModalPopupWindow(new BorderLayout());
         popup.setStyleClass("application_popup");
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
 
         content.add(new BLabel("Please give your full name so you can be given credit for this exercise."));
 
         BContainer nameRow = new BContainer(GroupLayout.makeHoriz(GroupLayout.LEFT));
         content.add(nameRow);
         nameRow.add(new BLabel("Name: "));
         final BTextField nameField = new BTextField();
         nameField.setPreferredWidth(200);
         nameRow.add(nameField);
 
         ActionListener actionListener = new ActionListener() {
 
             public void actionPerformed(ActionEvent event) {
                 String name = nameField.getText();
                 if (name.trim().equals("")) {
                     return;
                 }
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
 
     @Override
     public void finishExercise() {
         super.finishExercise();
         showCompletionPopup();
     }
 
     private void showCompletionPopup() {
 
         final ModalPopupWindow popup = new ModalPopupWindow(
                 new BorderLayout(5, 5));
         popup.setStyleClass("application_popup");
         popup.setModal(true);
 
         popup.setPreferredSize(300, -1);
 
         BContainer title = new BContainer(new BorderLayout());
         BLabel titleLabel = new BLabel("Submit Assignment", "title_container");
         title.add(titleLabel, BorderLayout.CENTER);
         popup.add(title, BorderLayout.NORTH);
 
         BLabel textLabel;
 
         if (isExerciseFinished()) {
             textLabel = new BLabel("CONGRATULATIONS! You have solved for all of the unknown joints in this exercise. " +
                     "Please click the button below to submit your work.");
         } else if(isHalfSolved()){
             textLabel = new BLabel("You have solved one of the diagrams, so you can submit your exercise for half credit now, " +
                     "But you have not finished the exercise. Are you sure you want to submit?");
         } else {
             textLabel = new BLabel("You have solved any of the diagrams yet, so you will not get any credit. Are you sure you " +
                     "want to submit?");
         }
 
         BContainer textContainer = new BContainer(new BorderLayout());
         textContainer.setStyleClass("padded_container");
 
         textContainer.add(textLabel, BorderLayout.CENTER);
 
         popup.add(textContainer, BorderLayout.CENTER);
 
         ActionListener actionListener = new ActionListener() {
 
             public void actionPerformed(ActionEvent event) {
                 if (event.getAction().equals("submit")) {
                     navigateAway();
                 } else {
                     popup.dismiss();
                 }
             }
         };
 
         BContainer buttonContainer = new BContainer(new BorderLayout());
 
         BButton submitButton = new BButton("Submit", actionListener, "submit");
         buttonContainer.add(submitButton, BorderLayout.CENTER);
 
         BButton returnButton = new BButton("Go Back", actionListener, "return");
         buttonContainer.add(returnButton, BorderLayout.WEST);
 
         popup.add(buttonContainer, BorderLayout.SOUTH);
 
         Dimension dim = new Dimension(300, 0);
         popup.popup((DisplaySystem.getDisplaySystem().getWidth() - dim.width) / 2, (DisplaySystem.getDisplaySystem().getHeight() - dim.height) / 2, true);
     }
 
     private boolean isHalfSolved() {
         // enumerate all diagrams and find out whether they are solved.
         List<EquationDiagram> equationDiagrams = getEquationDiagrams();
         for (EquationDiagram diagram : equationDiagrams) {
             if (diagram.getWorksheet().isSolved()) {
                 return true;
             }
         }
         return false;
     }
 
     // NOTE: this is being used in place of an update() method
     // this is bad style and should be fixed. Maybe we should have a general
     // facility for logger posting?
     @Override
     public void testTasks() {
         super.testTasks();
         
         my_frame++;
         if(my_frame >= frames_until_post) {
             my_frame = 0;
             sendLoggerData();
         }
     }
     private int my_frame = 0;
     private static final int frames_until_post = 100;
     
     private void sendLoggerData() {
         new Thread(new Runnable() {
             public void run() {
                 sendLoggerDataImpl();
             }
         }).start();
     }
     
     private void sendLoggerDataImpl() {
         
         URL documentBase = null;
         if (StaticsApplet.getInstance() == null) {
             try {
                 documentBase = new URL("http://intel.gatech.edu/assignment.php");
             } catch (MalformedURLException ex) {
                 // ???
                 //Logger.getLogger(PurseExerciseGraded.class.getName()).log(Level.SEVERE, null, ex);
             }
         } else {
             documentBase = StaticsApplet.getInstance().getDocumentBase();
         }
 
         // first, attempt to gather the logger data
         streamHandler.flush();
         byte[] loggerData = bout.toByteArray();
         String loggerString = new String(loggerData);
 
         System.out.println("*** Size: " + loggerData.length);
 
         String postData = null;
         try {
             postData = URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(studentName, "UTF-8");
             postData += "&" + URLEncoder.encode("instance", "UTF-8") + "=" + URLEncoder.encode("" + instance, "UTF-8");
             postData += "&" + URLEncoder.encode("loggerData", "UTF-8") + "=" + URLEncoder.encode(loggerString, "UTF-8");
         } catch (UnsupportedEncodingException ex) {
         }
 
         // ***
         // first send out our log messages
         try {
             String targetPage = "loggerPost.php";
             URL url = new URL(documentBase, targetPage);
 
             URLConnection connection = url.openConnection();
             connection.setDoOutput(true);
             OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
             writer.write(postData);
             writer.flush();
             
             BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
             String line;
             while((line = reader.readLine()) != null) {
                 System.out.println("response: "+line);
             }
 
             writer.close();
         } catch (MalformedURLException ex) {
         } catch (IOException ex) {
         }
     }
 
     private void navigateAway() {
 
         // this only works if we have an applet...
         if (StaticsApplet.getInstance() == null) {
             return;
         }
 
         MessageDigest md5;
         try {
             md5 = MessageDigest.getInstance("MD5");
         } catch (NoSuchAlgorithmException ex) {
             // if this fails, we're in a bit of a snit.
             // should popup a dialog with some message on it??
             return;
         }
 
         String exerciseName = getName();
         if (isExerciseFinished()) {
             // if exercise is solved, give full credit
             exerciseName += " [Solved]";
         } else if (isHalfSolved()) {
             exerciseName += " [1PartSolved]";
         }
         String finalString = studentName + "@" + exerciseName;
         byte[] resultBytes = md5.digest(finalString.getBytes());
 
         String finalCode = String.format("%02x%02x%02x", resultBytes[0], resultBytes[1], resultBytes[2]);
 
         // first send out our logger data
         sendLoggerData();
 
         // next send out the actual submission
         try {
             // format resulting URL:
             String postPage = "problemPost.php";
 
             String formattedName = URLEncoder.encode(studentName, "UTF-8");
             String formattedExercise = URLEncoder.encode(exerciseName, "UTF-8");
 
             String targetPage = postPage + "?name=" + formattedName + "&exercise=" + formattedExercise + "&code=" + finalCode;
 
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
