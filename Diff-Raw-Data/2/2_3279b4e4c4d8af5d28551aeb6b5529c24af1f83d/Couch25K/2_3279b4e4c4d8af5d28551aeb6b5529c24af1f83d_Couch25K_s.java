 import java.io.IOException;
 import java.io.InterruptedIOException;
 import java.util.Hashtable;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.microedition.io.Connector;
 import javax.microedition.lcdui.Choice;
 import javax.microedition.lcdui.Command;
 import javax.microedition.lcdui.CommandListener;
 import javax.microedition.lcdui.Display;
 import javax.microedition.lcdui.Displayable;
 import javax.microedition.lcdui.Font;
 import javax.microedition.lcdui.Form;
 import javax.microedition.lcdui.Gauge;
 import javax.microedition.lcdui.Image;
 import javax.microedition.lcdui.ImageItem;
 import javax.microedition.lcdui.Item;
 import javax.microedition.lcdui.ItemCommandListener;
 import javax.microedition.lcdui.List;
 import javax.microedition.lcdui.StringItem;
 import javax.microedition.lcdui.TextBox;
 import javax.microedition.lcdui.TextField;
 import javax.microedition.midlet.MIDlet;
 import javax.microedition.midlet.MIDletStateChangeException;
 import javax.wireless.messaging.MessageConnection;
 import javax.wireless.messaging.TextMessage;
 
 /**
  * Tracks jogging and walking intervals for the Couch-to-5k running program.
  */
 public class Couch25K extends MIDlet implements CommandListener, ItemCommandListener {
     static final int STATE_TITLE_SCREEN = 1;
     static final int STATE_OPTIONS_SCREEN = 2;
     static final int STATE_EDITING_TWEET_TEMPLATE = 3;
     static final int STATE_SELECT_WEEK = 4;
     static final int STATE_SELECT_WORKOUT = 5;
     static final int STATE_WORKOUT_SELECTED = 6;
     static final int STATE_WORKOUT = 7;
     static final int STATE_WORKOUT_PAUSED = 8;
     static final int STATE_WORKOUT_COMPLETE = 9;
 
     static final String CONFIG_TWITTER_SMS = "twitterSMS";
     static final String CONFIG_TWEET_TEMPLATE = "tweetTemplate";
 
     // MIDlet state ------------------------------------------------------------
 
     /** Current app state. */
     int state;
     /** Workout configuration and completion status. */
     Week[] weeks;
     /** Index of selected week on the Select Week screen. */
     int selectedWeek;
     /** Has the week changed since the Select Workout screen was last shown? */
     boolean weekChanged;
     /** Workout configuration for the selected week. */
     Week week;
     /** Index of selected workout on the Select Workout screen. */
     int selectedWorkout;
     /** Active Workout configuration. */
     Workout workout;
     /** Configuration options. */
     Hashtable config;
     /** State persistence. */
     WorkoutStore workoutStore;
 
     void initialiseState() {
         weeks = Workouts.getWorkouts();
         workoutStore = new WorkoutStore();
         workoutStore.setCompletion(weeks);
         config = workoutStore.loadConfig();
         if (config.size() == 0) {
             config = new Hashtable();
             config.put(CONFIG_TWITTER_SMS, "86444");
             config.put(CONFIG_TWEET_TEMPLATE,
                        "Completed $1 of #couchto5k");
         }
     }
 
     // Workout tracking --------------------------------------------------------
 
     /** Workout timer reference, used for stopping the timer. */
     Timer workoutTimer;
 
     /** Creates a Timer which calls updateWorkoutState() each second. */
     void trackWorkoutState(final Couch25K midlet) {
         workoutTimer = new Timer();
         workoutTimer.scheduleAtFixedRate(new TimerTask() {
             public void run() {
                 midlet.updateWorkoutState();
             }
         }, 0, 1000);
     }
 
     /** Index of the current workout step. */
     int currentStep;
     /** Configuration for the current workout step. */
     WorkoutStep step;
     /** Number of seconds elapsed during the current workout step. */
     int stepCounter;
     /** Number of seconds elapsed during the current workout. */
     int workoutCounter;
 
     /** Prepares to start tracking the currently-selected workout. */
     void resetWorkoutState() {
         currentStep = 0;
         stepCounter = 0;
         workoutCounter = 0;
         step = workout.steps[0];
         updateStepDisplay();
         updateProgressDisplay();
     }
 
     /**
      * Increments step and workout counters and updates display - it's expected
      * that this method will be called once a second while a Workour is in
      * progress.
      */
     public void updateWorkoutState() {
         workoutCounter++;
         stepCounter++;
         if (stepCounter >= step.duration) {
             if (currentStep == workout.steps.length - 1) {
                 // Completed the last step
                 finishWorkout();
                 return;
             }
 
             // Advance to the next step
             currentStep++;
             step = workout.steps[currentStep];
             stepCounter = 0;
             updateStepDisplay();
         }
         updateProgressDisplay();
     }
 
     /**
      * Updates step display and resets the progress gauge for the current step.
      */
     void updateStepDisplay() {
         action.setText(step.action + " for " + Utils.secToDuration(step.duration));
         stepCount.setText((currentStep + 1) + " of " + workout.steps.length);
         stepProgress.setValue(0);
         stepProgress.setMaxValue(step.duration);
         if (currentStep > 0) {
             Utils.playSound(step.action.toLowerCase());
             display.vibrate(1000);
         }
     }
 
     /** Updates step and workout progress display. */
     void updateProgressDisplay() {
         stepProgress.setValue(stepCounter);
         stepTime.setText(Utils.secToTime(stepCounter));
         workoutProgress.setValue(workoutCounter);
         workoutTime.setText(Utils.secToTime(workoutCounter));
     }
 
     // MIDlet UI ---------------------------------------------------------------
 
     Display display;
     Image tickImage;
     Font boldUnderlinedFont, bigBoldFont, mediumBoldFont, smallFont;
     Form titleScreen;
     StringItem title, quickStartMenu, selectWorkoutMenu, optionsMenu, exitMenu;
     Form optionsScreen;
     TextField twitterSMS;
     StringItem tweetTemplate;
     StringItem editTweetMenu;
     TextBox editTweetTemplate;
     List selectWeekScreen;
     List selectWorkoutScreen;
     Form workoutSummaryScreen;
     ImageItem completedIcon;
     StringItem completedAt, intervalsLabel;
     Form workoutScreen;
     StringItem action;
     Gauge stepProgress, workoutProgress;
     StringItem stepCount,stepTime, workoutTime;
     Form workoutCompleteScreen;
 
     // Commands
     Command backCommand = new Command("Back", Command.BACK, 1);
     Command selectCommand = new Command("Select", Command.ITEM, 1);
     Command quickStartCommand = new Command("Quick Start", Command.SCREEN, 1);
     Command optionsCommand = new Command("Options", Command.ITEM, 1);
     Command editCommand = new Command("Edit", Command.ITEM, 1);
     Command okCommand = new Command("OK", Command.OK, 1);
     Command saveCommand = new Command("Save", Command.SCREEN, 1);
     Command cancelCommand = new Command("Cancel", Command.SCREEN, 1);
     Command startCommand = new Command("Start", Command.SCREEN, 1);
     Command markCompleteCommand = new Command("Mark Complete", Command.SCREEN, 2);
     Command pauseCommand = new Command("Pause", Command.SCREEN, 1);
     Command resumeCommand = new Command("Resume", Command.SCREEN, 1);
     Command exitCommand = new Command("Exit", Command.EXIT, 1);
     Command tweetCommand = new Command("Tweet", Command.SCREEN, 1);
 
     void initialiseUI() {
         display = Display.getDisplay(this);
         boldUnderlinedFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_MEDIUM);
         mediumBoldFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_MEDIUM);
         bigBoldFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_LARGE);
         smallFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
         tickImage = Utils.loadImage("tick");
 
         // Title screen
         titleScreen = new Form("couch25k");
         title = new StringItem(null, "\ncouch25k\n\n");
         title.setFont(bigBoldFont);
         title.setLayout(Item.LAYOUT_CENTER);
         quickStartMenu = new StringItem(null, "Quick Start\n", StringItem.HYPERLINK);
         quickStartMenu.setLayout(Item.LAYOUT_CENTER);
         quickStartMenu.addCommand(quickStartCommand);
         quickStartMenu.setItemCommandListener(this);
         selectWorkoutMenu = new StringItem(null, "Select Workout\n", StringItem.HYPERLINK);
         selectWorkoutMenu.setLayout(Item.LAYOUT_CENTER);
         selectWorkoutMenu.addCommand(selectCommand);
         selectWorkoutMenu.setItemCommandListener(this);
         optionsMenu = new StringItem(null, "Options\n", StringItem.HYPERLINK);
         optionsMenu.setLayout(Item.LAYOUT_CENTER);
         optionsMenu.addCommand(optionsCommand);
         optionsMenu.setItemCommandListener(this);
         exitMenu = new StringItem(null, "Exit", StringItem.HYPERLINK);
         exitMenu.setLayout(Item.LAYOUT_CENTER);
         exitMenu.addCommand(exitCommand);
         exitMenu.setItemCommandListener(this);
         titleScreen.append(title);
         titleScreen.append(quickStartMenu);
         titleScreen.append(selectWorkoutMenu);
         titleScreen.append(optionsMenu);
         titleScreen.append(exitMenu);
 
         // Options screen
         optionsScreen = new Form("couch25k Options");
         twitterSMS = new TextField("Twitter SMS Number",
                                    (String)config.get(CONFIG_TWITTER_SMS),
                                    11, TextField.NUMERIC);
         tweetTemplate = new StringItem("Tweet Template\n",
                                        (String)config.get(CONFIG_TWEET_TEMPLATE));
         editTweetMenu = new StringItem(null, "Edit\n", Item.BUTTON);
         editTweetMenu.setLayout(Item.LAYOUT_RIGHT);
         editTweetMenu.addCommand(editCommand);
         editTweetMenu.setItemCommandListener(this);
         StringItem tweetTemplateHint = new StringItem(null, "$1: Week X - Workout Y");
         tweetTemplateHint.setFont(smallFont);
         tweetTemplateHint.setLayout(Item.LAYOUT_LEFT);
         optionsScreen.append(twitterSMS);
         optionsScreen.append(tweetTemplate);
         optionsScreen.append(editTweetMenu);
         optionsScreen.append(tweetTemplateHint);
         optionsScreen.addCommand(cancelCommand);
         optionsScreen.addCommand(saveCommand);
         optionsScreen.setCommandListener(this);
 
         // Tweet template screen
         editTweetTemplate = new TextBox("Tweet Template",
                 (String)config.get(CONFIG_TWEET_TEMPLATE),
                 140, TextField.ANY);
         editTweetTemplate.addCommand(okCommand);
         editTweetTemplate.setCommandListener(this);
 
         // Week selection screen
         selectWeekScreen = new List("couch25k - Select Week", Choice.IMPLICIT);
         selectWeekScreen.setSelectCommand(selectCommand);
         selectWeekScreen.addCommand(backCommand);
         selectWeekScreen.setCommandListener(this);
 
         // Workout selection screen
         selectWorkoutScreen = new List("", Choice.IMPLICIT);
         selectWorkoutScreen.setSelectCommand(selectCommand);
         selectWorkoutScreen.addCommand(markCompleteCommand);
         selectWorkoutScreen.addCommand(backCommand);
         selectWorkoutScreen.setCommandListener(this);
 
         // Workout summary screen
         workoutSummaryScreen = new Form("");
        completedIcon = new ImageItem("Completed", tickImage, Item.LAYOUT_CENTER, null);
         completedAt = new StringItem(null, "");
         completedAt.setLayout(Item.LAYOUT_CENTER);
         intervalsLabel = new StringItem(null, "");
         intervalsLabel.setLayout(Item.LAYOUT_LEFT);
         intervalsLabel.setFont(boldUnderlinedFont);
         workoutSummaryScreen.addCommand(startCommand);
         workoutSummaryScreen.addCommand(backCommand);
         workoutSummaryScreen.setCommandListener(this);
 
         // Workout screen
         workoutScreen = new Form("");
         action = new StringItem(null, "");
         action.setFont(bigBoldFont);
         stepProgress = new Gauge(null, false, Gauge.INDEFINITE, 0);
         stepProgress.setLayout(Item.LAYOUT_EXPAND);
         stepCount = new StringItem(null, "");
         stepCount.setFont(bigBoldFont);
         stepCount.setLayout(Item.LAYOUT_EXPAND);
         stepTime = new StringItem(null, "");
         stepTime.setFont(bigBoldFont);
         workoutProgress = new Gauge("Workout", false, Gauge.INDEFINITE, 0);
         workoutProgress.setLayout(Item.LAYOUT_EXPAND);
         workoutTime = new StringItem(null, "");
         workoutTime.setLayout(Item.LAYOUT_RIGHT);
         workoutScreen.append(action);
         workoutScreen.append(stepProgress);
         workoutScreen.append(stepCount);
         workoutScreen.append(stepTime);
         workoutScreen.append(workoutProgress);
         workoutScreen.append(workoutTime);
         workoutScreen.addCommand(pauseCommand);
         workoutScreen.setCommandListener(this);
 
         // Workout completion screen
         workoutCompleteScreen = new Form("Workout Complete");
         workoutCompleteScreen.addCommand(backCommand);
         workoutCompleteScreen.addCommand(tweetCommand);
         workoutCompleteScreen.setCommandListener(this);
     }
 
     // State transitions -------------------------------------------------------
 
     void showTitleScreen() {
         display.setCurrent(titleScreen);
         state = STATE_TITLE_SCREEN;
     }
 
     /** Opens the workout summary screen with the first incomplete workout. */
     void quickStart() {
         for (int i = 0; i < weeks.length; i++) {
             if (!weeks[i].isCompleted()) {
                 weekChanged = (selectedWeek != i);
                 selectedWeek = i;
                 week = weeks[i];
                 selectedWorkout = week.firstIncompleteIndex();
                 workout = week.workouts[selectedWorkout];
                 showWorkoutSummaryScreen();
                 return;
             }
         }
     }
 
     void showOptionsScreen() {
         display.setCurrent(optionsScreen);
         state = STATE_OPTIONS_SCREEN;
     }
 
     void editTweetTemplate() {
         display.setCurrent(editTweetTemplate);
         state = STATE_EDITING_TWEET_TEMPLATE;
     }
 
     void saveOptions() {
         config.put(CONFIG_TWITTER_SMS, twitterSMS.getString());
         config.put(CONFIG_TWEET_TEMPLATE, editTweetTemplate.getString());
         workoutStore.saveConfig(config);
         showTitleScreen();
     }
 
     void cancelOptions() {
         twitterSMS.delete(0, twitterSMS.size());
         twitterSMS.insert((String)config.get(CONFIG_TWITTER_SMS), 0);
         editTweetTemplate.delete(0, editTweetTemplate.size());
         editTweetTemplate.insert((String)config.get(CONFIG_TWEET_TEMPLATE), 0);
         tweetTemplate.setText((String)config.get(CONFIG_TWEET_TEMPLATE));
         showTitleScreen();
     }
 
     void showSelectWeekScreen() {
         boolean allCompleted = true;
         selectWeekScreen.deleteAll();
         for (int i = 0; i < weeks.length; i++) {
             boolean weekCompleted = weeks[i].isCompleted();
             selectWeekScreen.append(
                 "Week " + (i + 1),
                 weekCompleted ? tickImage : null);
             if (!weekCompleted && allCompleted) {
                 allCompleted = false;
             }
         }
         // Restore any previously-selected week
         selectWeekScreen.setSelectedIndex(selectedWeek, true);
         if (allCompleted) {
             selectWeekScreen.removeCommand(quickStartCommand);
         }
         display.setCurrent(selectWeekScreen);
         state = STATE_SELECT_WEEK;
     }
 
     void selectWeek() {
         weekChanged = (selectedWeek != selectWeekScreen.getSelectedIndex());
         selectedWeek = selectWeekScreen.getSelectedIndex();
         week = weeks[selectedWeek];
         showSelectWorkoutScreen();
     }
 
     void showSelectWorkoutScreen() {
         selectWorkoutScreen.setTitle("Week " + (selectedWeek + 1) +
                                      " - Select Workout");
         selectWorkoutScreen.deleteAll();
         for (int i = 0; i < week.workouts.length; i++) {
             selectWorkoutScreen.append(
                 "Workout " + (i + 1) + " - " + Utils.secToTime(week.workouts[i].totalDuration),
                 (week.completedAt[i] != null ? tickImage : null));
         }
         if (!weekChanged) {
             selectWorkoutScreen.setSelectedIndex(selectedWorkout, true);
         }
         display.setCurrent(selectWorkoutScreen);
         state = STATE_SELECT_WORKOUT;
     }
 
     void selectWorkout() {
         selectedWorkout = selectWorkoutScreen.getSelectedIndex();
         workout = week.workouts[selectedWorkout];
         showWorkoutSummaryScreen();
     }
 
     void showWorkoutSummaryScreen() {
         workoutSummaryScreen.setTitle("Week " + (selectedWeek + 1) +
                                       " - Workout " + (selectedWorkout + 1));
         workoutSummaryScreen.deleteAll();
         // Completion details
         if (week.completedAt[selectedWorkout] != null) {
             workoutSummaryScreen.append(completedIcon);
             completedAt.setText(Utils.formatDate(week.completedAt[selectedWorkout]));
             workoutSummaryScreen.append(completedAt);
         }
         // Workout steps
         intervalsLabel.setText(workout.steps.length + " Intervals\n");
         workoutSummaryScreen.append(intervalsLabel);
         for (int i = 0; i < workout.steps.length; i++) {
             StringItem stepDesc = new StringItem(null,
                 workout.steps[i].action + " for " +
                 Utils.secToDuration(workout.steps[i].duration) + "\n");
             workoutSummaryScreen.append(stepDesc);
         }
         display.setCurrent(workoutSummaryScreen);
         state = STATE_WORKOUT_SELECTED;
     }
 
     void startWorkout() {
         resetWorkoutState();
 
         workoutScreen.setTitle("Week " + (selectedWeek + 1) +
                                " - Workout " + (selectedWorkout + 1));
         workoutProgress.setMaxValue(workout.totalDuration);
         workoutProgress.setValue(0);
         display.setCurrent(workoutScreen);
         trackWorkoutState(this);
         state = STATE_WORKOUT;
     }
 
     void pauseWorkout() {
         workoutTimer.cancel();
 
         workoutScreen.removeCommand(pauseCommand);
         workoutScreen.addCommand(resumeCommand);
         state = STATE_WORKOUT_PAUSED;
     }
 
     void resumeWorkout() {
         workoutScreen.removeCommand(resumeCommand);
         workoutScreen.addCommand(pauseCommand);
         trackWorkoutState(this);
         state = STATE_WORKOUT;
     }
 
     void finishWorkout() {
         workoutTimer.cancel();
         week.completedAt[selectedWorkout] =
             workoutStore.completeWorkout(selectedWeek, selectedWorkout);
 
         display.setCurrent(workoutCompleteScreen);
         Utils.playSound("finished");
         state = STATE_WORKOUT_COMPLETE;
     }
 
     void tweetCompletion() {
         try {
             String addr = "sms://" + (String)config.get(CONFIG_TWITTER_SMS);
             MessageConnection conn = (MessageConnection)Connector.open(addr);
             TextMessage msg =
                 (TextMessage)conn.newMessage(MessageConnection.TEXT_MESSAGE);
             msg.setPayloadText(Utils.format((String)config.get(CONFIG_TWEET_TEMPLATE),
                                             new String[] { workoutTitle() }));
             conn.send(msg);
             workoutCompleteScreen.removeCommand(tweetCommand);
         } catch (InterruptedIOException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     void exit() {
         try {
             destroyApp(false);
         } catch (MIDletStateChangeException e) {
             e.printStackTrace();
         }
         notifyDestroyed();
     }
 
     // Utilities ---------------------------------------------------------------
 
     String workoutTitle() {
         return "Week " + (selectedWeek + 1) + " - Workout " + (selectedWorkout + 1);
     }
 
     // MIDlet API --------------------------------------------------------------
 
     /** Initialises or resumes the applcation as approriate. */
     protected void startApp() throws MIDletStateChangeException {
         if (display == null) {
             initialiseState();
             initialiseUI();
         }
 
         if (state == STATE_WORKOUT_PAUSED) {
             resumeWorkout();
         } else {
             showTitleScreen();
         }
     }
 
     /** Pauses workout tracking if it is currently active. */
     protected void pauseApp() {
         if (state == STATE_WORKOUT) {
             pauseWorkout();
         }
     }
 
     protected void destroyApp(boolean unconditional)
         throws MIDletStateChangeException {
         workoutStore.close();
     }
 
     // CommandListener / ItemCommandListener API -------------------------------
 
     /**
      * Calls the appropriate transition method based on the current state and
      * the command which was given.
      */
     public void commandAction(Command c, Displayable d) {
         switch (state) {
         case STATE_OPTIONS_SCREEN:
             if (c == saveCommand) saveOptions();
             if (c == cancelCommand) cancelOptions();
             break;
         case STATE_EDITING_TWEET_TEMPLATE:
             if (c == okCommand) {
                 tweetTemplate.setText(editTweetTemplate.getString());
                 showOptionsScreen();
             }
             break;
         case STATE_SELECT_WEEK:
             if (c == selectCommand) selectWeek();
             if (c == backCommand) showTitleScreen();
             break;
         case STATE_SELECT_WORKOUT:
             if (c == selectCommand) selectWorkout();
             if (c == backCommand) showSelectWeekScreen();
             if (c == markCompleteCommand) {
                 int selectedWorkout = selectWorkoutScreen.getSelectedIndex();
                 week.completedAt[selectedWorkout] =
                     workoutStore.completeWorkout(selectedWeek, selectedWorkout);
                 // Redraw lazily
                 showSelectWorkoutScreen();
                 selectWorkoutScreen.setSelectedIndex(selectedWorkout, true);
             }
             break;
         case STATE_WORKOUT_SELECTED:
             if (c == startCommand) startWorkout();
             if (c == backCommand) showSelectWorkoutScreen();
             break;
         case STATE_WORKOUT:
             if (c == pauseCommand) pauseWorkout();
             break;
         case STATE_WORKOUT_PAUSED:
             if (c == resumeCommand) resumeWorkout();
             break;
         case STATE_WORKOUT_COMPLETE:
             if (c == backCommand) showTitleScreen();
             if (c == tweetCommand) tweetCompletion();
             break;
         }
     }
 
     /**
      * Calls the appropriate transition method based on a specific item which
      * an action was performed on.
      */
     public void commandAction(Command c, Item item) {
         if (item == quickStartMenu) quickStart();
         if (item == selectWorkoutMenu) showSelectWeekScreen();
         if (item == optionsMenu) showOptionsScreen();
         if (item == editTweetMenu) editTweetTemplate();
         if (item == exitMenu) exit();
     }
 }
