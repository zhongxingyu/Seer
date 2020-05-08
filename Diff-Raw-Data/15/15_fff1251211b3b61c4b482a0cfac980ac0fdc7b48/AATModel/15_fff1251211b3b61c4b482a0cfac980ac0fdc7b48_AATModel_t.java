 /** This file is part of Approach Avoidance Task.
  *
  * Approach Avoidance Task is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Approach Avoidance Task is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Approach Avoidance Task.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package Model;
 
 import AAT.AatObject;
 import AAT.HighMemoryAAT;
 import DataStructures.AATImage;
 import DataStructures.ParticipantData;
 import DataStructures.TestData;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Observable;
 
 
 /**
  * Created by IntelliJ IDEA.
  * User: marcel
  * Date: 10/4/11
  * Time: 3:12 PM
  * This model does all the logic for the AAT. It uses the Model-View-Controller pattern. So it does the communication between
  * the controllers and the views. Controllers are the joystick and the user input from the main Frame. Views are the test view and
  * results view.
  * <p/>
  * When a picture has to be pulled or pushed all joystick movements are recorded with their corresponding reaction times.
  * All the data for all participants are stored in a file and selections from those results can be exported so it can be analyzed
  * with R or SPSS. A researcher can choose the way the data has to be prepared. E.g. include mistakes or not.
  * <p/>
  * When a user has done the test he can see his results with four boxplots. One for every condition.
  * <p/>
  * <p/>
  * <p/>
  */
 
 public class AATModel extends Observable {
 
 
     private AatObject newAAT;
 
     //Test status
     private final static int TEST_STOPPED = 0;
     private final static int TEST_WAIT_FOR_QUESTIONS = 1;
     private final static int PRACTICE_IMAGE_LOADED = 2;
     private final static int TEST_WAIT_FOR_TRIGGER = 3;
     private final static int IMAGE_LOADED = 4;
     private final static int TEST_SHOW_FINISHED = 5;
     private final static int TEST_SHOW_RESULTS = 6;
 
     //Test variables
     private int repeat;
     private int breakAfter;
     private int count; //Counts the number of images shown.
     private ArrayList<AATImage> testList; //Random list that contains the push or pull images.
 
     //progress variables
     private int run;
     private boolean practice;
 
     //Measurement
 
     private TestData testData;
     private ParticipantData newParticipant;
     private long startMeasure;
     private int resize;
 
     //Current image and current status of the test
     private AATImage current;
     private int testStatus;
 
     private int previousPos; //TTo keep track of the joystick position
 
     private int lastSize;
 
     //Constructor.
     public AATModel() {
         //   testStatus = AATModel.TEST_STOPPED;
     }
 
 //------------------------------initialise AAT --------------------------------------------------
 
     //Load a new AAT from file
     public void loadNewAAT(File configFile) throws AatObject.FalseConfigException {
         newAAT = new HighMemoryAAT(configFile);
         testData = new TestData(newAAT);
     }
 
     //Starts a new instance of the AAT. With no. times it has to repeat and when there will be a break.
     public void startTest() {
         this.repeat = newAAT.getRepeat();
         this.breakAfter = newAAT.getBreakAfter();
         this.previousPos = (newAAT.getDataSteps() + 1) / 2; //Set the previous position to the center position
 
         if (newAAT.hasPractice()) {        //If set in the config, first do a practice.
             practice = true; //Set the test to practice mode
             repeat++;     //Make these one higher, because of the practice
             breakAfter++;
             count = 0; //reset the counter
             testList = newAAT.createRandomPracticeList();
         } else {   //Test has no practice so create random list of images
             if (newAAT.hasColoredBorders()) {
                 testList = newAAT.createRandomListBorders(); //create a new Random list
             } else {
                 testList = newAAT.createRandomListNoBorders();
             }
         }
         count = 0;        //reset counters
         run = 0;
         int id = testData.getHighestID();
         id++;          //new higher id
         newParticipant = new ParticipantData(id);
 
         //     if (newAAT.getNoOfQuestions() > 0) {   //When there are extra question, show them
         if (newAAT.getDisplayQuestions().equals("Before")) {
             testStatus = AATModel.TEST_WAIT_FOR_QUESTIONS;
             this.setChanged();
             notifyObservers("Show questions");      //Notify the observer that a new test is started.
         } else {     //When there are no questions te begin with, just start the test
             this.setChanged();
             notifyObservers("Start");
             testStatus = AATModel.TEST_WAIT_FOR_TRIGGER;   //Start the test
         }
 
     }
 
     public AatObject getTest() {
         return newAAT;
     }
 
     public TestData getTestData() {
         return testData;
     }
 
 //---------------------------Test Progress---------------------------------------------------------
 
     /**
      * This method determines the next step to be taken in the test.
      * Based on testStatus and the current image shown to the participant
      * Makes sure that the correct images are shown(practice/real test). Puts the test on break
      * if that's necessary. And watches when the test is finished. Together with the joystick inputs this method
      * determines the progress of the AAT.
      */
     private void NextStep() {
 
         if (testStatus == AATModel.PRACTICE_IMAGE_LOADED) {
             if (count < testList.size()) {
                 showNextImage();
             } else {
                 run++;
                 practice = false; //Practice has ended
                 testStatus = AATModel.TEST_WAIT_FOR_TRIGGER;
                 count = 0; //reset the counter
                 if (newAAT.hasColoredBorders()) {
                     testList = newAAT.createRandomListBorders(); //create a new Random list
                 } else {
                     testList = newAAT.createRandomListNoBorders();
                 }
                 this.setChanged();
                 this.notifyObservers("Practice ended");
 
             }
         } else {
             if (count < testList.size()) {     //Just show the next image
                 showNextImage();
             } else {          //No more images in the list
                 run++;
                 count = 0;
                 if (run == breakAfter) {    //Test needs a break
                     if (newAAT.hasColoredBorders()) {
                         testList = newAAT.createRandomListBorders(); //create a new Random list
                     } else {
                         testList = newAAT.createRandomListNoBorders();
                     }
                     current = testList.get(0);
                     testStatus = AATModel.TEST_WAIT_FOR_TRIGGER;
                     this.setChanged();
                     notifyObservers("Break");      //Notify observer that there is a break.
 
                 } else if (run == repeat) {   //No more runs left, Test has ended
                     testStatus = AATModel.TEST_SHOW_FINISHED;    //Notify observers about it
                    if (!newAAT.getDisplayQuestions().equals("After")) {  //questionnaire has to be added at the end
                        testData.addParticipant(newParticipant);
                    }
                     this.setChanged();
                     notifyObservers("Show finished");   //First show black screen
                 } else {           //Continue with a new run
                     if (newAAT.hasColoredBorders()) {
                         testList = newAAT.createRandomListBorders(); //create a new Random list
                     } else {
                         testList = newAAT.createRandomListNoBorders();
                     }
                     count = 0;
                     showNextImage();
                 }
             }
         }
     }
 
     /**
      * Displays the next image. Will display from the practicelist or the normal testlist, depending on the status
      * of the test
      */
     private void showNextImage() {
         current = testList.get(count);    //change current to the next image
         count++;
         this.setChanged();
         notifyObservers("Show Image");      //Notify observers
         startMeasure(); //Start the measurement.
     }
 
     //Returns the next Image
     public BufferedImage getNextImage() {
         return current.getImage();
     }
 
 //---------------------Getter and Setter methods for the different Observers and Controllers -------------
 
     /**
      * @return an integer from 1 to stepSize, which determines how much larger or smaller the picture
      *         has to be shown on the screen
      */
     public int getPictureSize() {
         return resize;
     }
 
     /**
      * The AATImage object also stores the direction (push or pull) of an image. This method returns the
      * direction for the image that is currently shown on the screen
      *
      * @return Direction for the current image
      */
     public int getDirection() {
         return current.getDirection();
     }
 
 //-------------------- Measure the data and write them to file when finished ---------------
 
     /**
      * Every time when a new image is shown on the screen a new measure is started.
      */
     public void startMeasure() {
         newParticipant.newMeasure(run, current.toString(), current.getDirection(), current.getType()); //Begin met de metingen opslaan.
         startMeasure = System.currentTimeMillis();  //Begintijd
     }
 
     /**
      * This method is called every time when the joystick is pulled or pushed while a picture is shown on the
      * screen. It returns the time between the start of the measure and the current joystick movement
      *
      * @return ReactionTime in ms.
      */
     public long getMeasurement() {
         return System.currentTimeMillis() - startMeasure;
     }
 
 
 //------------------------------Get results from the test -------------------------------
 
     /**
      * This methode creates a hashmap with float arrays containing the results of a single participant.
      * This data is used as input for the boxplots shown at the end of the test
      *
      * @return Hashmap with results for the boxplot
      */
     public HashMap<String, float[]> getResultsPerCondition() {
         HashMap<String, float[]> results = new HashMap<String, float[]>();
         String pull = newAAT.getPullTag();
         String push = newAAT.getPushTag();
         String nDir = newAAT.getNeutralDir();
         String aDir = newAAT.getAffectiveDir();
         results.put(pull + " & " + nDir, newParticipant.getMeasures(AATImage.PULL, AATImage.NEUTRAL));
         results.put(pull + " & " + aDir, newParticipant.getMeasures(AATImage.PULL, AATImage.AFFECTIVE));
         results.put(push + " & " + nDir, newParticipant.getMeasures(AATImage.PUSH, AATImage.NEUTRAL));
         results.put(push + " & " + aDir, newParticipant.getMeasures(AATImage.PUSH, AATImage.AFFECTIVE));
         return results;
     }
 
 
     //--------------Input from the joystick controller-------------------------------------//
 
 
     /**
      * Gets changes in the movement from the y-axis of the joystick.
      * Test status has to be changed when the joystick reaches the maximum distance. It depends on the direction that belongs to
      * the current image. When the user performs the requested action, when finished the test status has to go to wait for trigger.
      *
      * @param value This value represents the position of the joystick. This information comes from the joystick controller
      */
     public void changeYaxis(int value, int displayValue) {
 
         if (value != previousPos) {
             previousPos = value;
 
             if (testStatus == AATModel.IMAGE_LOADED || testStatus == AATModel.PRACTICE_IMAGE_LOADED) { //Only listen when there is an image loaded
                 newParticipant.addResult(value, getMeasurement());     //add results to the other measurements
                 if (current.getDirection() == AATImage.PULL && value == newAAT.getDataSteps()) {   //check if the requested action has been performed
                     lastSize = newAAT.getStepRate();
                     removeImage();
                     return;
                 } else if (current.getDirection() == AATImage.PUSH && value == 1) {
                     lastSize = 1;
                     removeImage();
                     return;
                 }
             }
         }
 
         resize = displayValue;
         this.setChanged();
         notifyObservers("Y-as");
 
     }
 
     public int getLastSize() {
         return lastSize;
     }
 
     //clear the test
     public void clearAll() {
         newAAT.clearLists();
         System.gc();
     }
 
     /**
      * This method is called when a participant has pulled or pushed the image completely. It notifies the observers
      * that the current image has to be removed from the view.
      */
 
 
     private void removeImage() {
 
         this.setChanged();
         notifyObservers("Wait screen");
         testStatus = AATModel.TEST_WAIT_FOR_TRIGGER;
     }
 
     /**
      * When the test is waiting for the trigger, check if the trigger is pressed and then change the test status
      * to image loaded. then call nextstep so the model can determine the appropriate next action to take
      */
     public void triggerPressed() {
 
         switch (testStatus) {
             case AATModel.TEST_SHOW_RESULTS:
                 testStatus = AATModel.TEST_STOPPED;
                 this.setChanged();
                 if (newAAT.getDisplayQuestions().equals("After")) {
                     this.notifyObservers("Show questions");
                 } else {
                     this.notifyObservers("Finished");
                     clearAll();
                 }
                 break;
 
 
             case AATModel.TEST_SHOW_FINISHED:
                 this.setChanged();
                 if (newAAT.hasBoxPlot()) {
                     testStatus = AATModel.TEST_SHOW_RESULTS;
                     this.notifyObservers("Display results");
                 } else {
                     testStatus = AATModel.TEST_STOPPED;
                     if (newAAT.getDisplayQuestions().equals("After")) {
                         this.notifyObservers("Show questions");
                     } else {
                         this.notifyObservers("Finished");
                         clearAll();
                     }
                 }
                 break;
 
             case AATModel.TEST_WAIT_FOR_TRIGGER:
                 if (practice) {
                     testStatus = AATModel.PRACTICE_IMAGE_LOADED;
                 } else {
                     testStatus = AATModel.IMAGE_LOADED;
                 }
                 NextStep();    //Determine next step in the test.
                 break;
         }
     }
 
 //--------------------------User input, answers to the questions ----------------------------------------
 
     /**
      * This method is called when the participant has answered the optional questions. The answers are added to
      * the participants data file and the views are notified that the AAT can be started
      *
      * @param extraQuestions The answers to the extra questions. The hashmap contains the key and the answer
      */
     public void addExtraQuestions(HashMap<String, String> extraQuestions) {
         this.newParticipant.addQuestionData(extraQuestions);
         this.setChanged();
         if (newAAT.getDisplayQuestions().equals("After")) {
            testData.addParticipant(newParticipant);
             this.notifyObservers("Finished");
         } else {
             this.notifyObservers("Start");
             testStatus = AATModel.TEST_WAIT_FOR_TRIGGER;   //Test status is wait for the user to press the trigger button.
         }
     }
 }
