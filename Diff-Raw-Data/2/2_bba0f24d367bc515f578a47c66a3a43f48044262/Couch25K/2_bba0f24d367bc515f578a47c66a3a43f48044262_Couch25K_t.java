 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.microedition.lcdui.Choice;
 import javax.microedition.lcdui.Command;
 import javax.microedition.lcdui.CommandListener;
 import javax.microedition.lcdui.Display;
 import javax.microedition.lcdui.Displayable;
 import javax.microedition.lcdui.Font;
 import javax.microedition.lcdui.Form;
 import javax.microedition.lcdui.Gauge;
 import javax.microedition.lcdui.Item;
 import javax.microedition.lcdui.List;
 import javax.microedition.lcdui.StringItem;
 import javax.microedition.media.Manager;
 import javax.microedition.media.MediaException;
 import javax.microedition.media.Player;
 import javax.microedition.media.PlayerListener;
 import javax.microedition.midlet.MIDlet;
 import javax.microedition.midlet.MIDletStateChangeException;
 
 public class Couch25K extends MIDlet implements CommandListener, PlayerListener {
     private static final int STATE_SELECT_WEEK = 1;
     private static final int STATE_SELECT_WORKOUT = 2;
     private static final int STATE_WORKOUT_SELECTED = 3;
     private static final int STATE_WORKOUT = 4;
     private static final int STATE_WORKOUT_PAUSED = 5;
     private static final int STATE_WORKOUT_COMPLETE = 6;
 
     // State
     private int state;
     private int selectedWeek;
     private int selectedWorkout;
     private Workout workout;
     private WorkoutState workoutState;
 
     // UI
     private Display display;
     private Font bigBoldFont;
     private List selectWeekScreen;
     private List selectWorkoutScreen;
     private Form workoutSummaryScreen;
     private Form workoutScreen;
     private StringItem action;
     private Gauge stepProgress;
     private StringItem stepCount;
     private StringItem stepTime;
     private Gauge workoutProgress;
     private StringItem workoutTime;
     private Form workoutCompleteScreen;
 
     // Commands
     private Command backCommand = new Command("Back", Command.BACK, 1);
     private Command selectCommand = new Command("Select", Command.ITEM, 1);
     private Command startCommand = new Command("Start", Command.SCREEN, 1);
     private Command pauseCommand = new Command("Pause", Command.SCREEN, 2);
     private Command resumeCommand = new Command("Resume", Command.SCREEN, 3);
 
     // Timing
     private Timer workoutTimer;
 
     // MIDlet API --------------------------------------------------------------
 
     protected void startApp() throws MIDletStateChangeException {
         if (display == null) {
             display = Display.getDisplay(this);
             bigBoldFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_LARGE);
 
             // Week selection screen setup
             selectWeekScreen = new List("Select Week", Choice.IMPLICIT,
                 new String[] {
                     "Week 1", "Week 2", "Week 3", "Week 4", "Week 5", "Week 6",
                     "Week 7", "Week 8", "Week 9"
                 }, null);
             selectWeekScreen.addCommand(selectCommand);
             selectWeekScreen.setCommandListener(this);
 
             // Workout selection screen setup
             selectWorkoutScreen = new List("Select Workout", Choice.IMPLICIT,
                 new String[] {
                     "Workout 1", "Workout 2", "Workout 3"
                 }, null);
             selectWorkoutScreen.addCommand(selectCommand);
             selectWorkoutScreen.addCommand(backCommand);
             selectWorkoutScreen.setCommandListener(this);
 
             // Workout summary screen setup
             workoutSummaryScreen = new Form("");
             workoutSummaryScreen.addCommand(startCommand);
             workoutSummaryScreen.addCommand(backCommand);
             workoutSummaryScreen.setCommandListener(this);
 
             // Workout screen setup
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
 
             workoutCompleteScreen = new Form("Workout Complete");
             workoutCompleteScreen.setCommandListener(this);
         }
 
         if (state == STATE_WORKOUT_PAUSED) {
            resumeWorkout();
         } else {
             init();
         }
     }
 
     protected void pauseApp() {
         if (state == STATE_WORKOUT) {
             pauseWorkout();
         }
     }
 
     protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
     }
 
     // CommandListener API -----------------------------------------------------
 
     public void commandAction(Command c, Displayable d) {
         switch (state) {
         case STATE_SELECT_WEEK:
             if (c == selectCommand) selectWeek();
             break;
         case STATE_SELECT_WORKOUT:
             if (c == selectCommand) selectWorkout();
             else if (c == backCommand) init();
             break;
         case STATE_WORKOUT_SELECTED:
             if (c == startCommand) startWorkout();
             else if (c == backCommand) selectWeek();
             break;
         case STATE_WORKOUT:
             if (c == pauseCommand) pauseWorkout();
             break;
         case STATE_WORKOUT_PAUSED:
             if (c == resumeCommand) resumeWorkout();
             break;
         default:
             throw new IllegalStateException("Command " + c +
                                             " not expected in state " + state);
         }
     }
 
     // PlayerListener API ------------------------------------------------------
 
     public void playerUpdate(Player player, String event, Object eventData) {
         if (event == PlayerListener.END_OF_MEDIA) {
             player.close();
         }
     }
 
     // State transitions -------------------------------------------------------
 
     public void init() {
         display.setCurrent(selectWeekScreen);
         state = STATE_SELECT_WEEK;
     }
 
     public void selectWeek() {
         selectedWeek = selectWeekScreen.getSelectedIndex() + 1;
 
         display.setCurrent(selectWorkoutScreen);
         state = STATE_SELECT_WORKOUT;
     }
 
     public void selectWorkout() {
         selectedWorkout = selectWorkoutScreen.getSelectedIndex() + 1;
         workout = Workouts.getWorkout(selectedWeek, selectedWorkout);
 
         workoutSummaryScreen.setTitle("Week " + selectedWeek +
                                       " - Workout " + selectedWorkout);
         workoutSummaryScreen.deleteAll();
         for (int i = 0; i < workout.steps.length; i++) {
             workoutSummaryScreen.append(new StringItem(null,
                 workout.steps[i].action + " for " +
                 displayDuration(workout.steps[i].duration) + "\n"
             ));
         }
         display.setCurrent(workoutSummaryScreen);
         state = STATE_WORKOUT_SELECTED;
     }
 
     public void startWorkout() {
         workoutState = new WorkoutState(this, workout);
 
         workoutScreen.setTitle("Week " + selectedWeek +
                                " - Workout " + selectedWorkout);
         workoutProgress.setMaxValue(workout.totalDuration);
         workoutProgress.setValue(0);
         display.setCurrent(workoutScreen);
         trackWorkoutState(workoutState);
         state = STATE_WORKOUT;
     }
 
     public void pauseWorkout() {
         workoutTimer.cancel();
 
         workoutScreen.removeCommand(pauseCommand);
         workoutScreen.addCommand(resumeCommand);
         state = STATE_WORKOUT_PAUSED;
     }
 
     public void resumeWorkout() {
         workoutScreen.removeCommand(resumeCommand);
         workoutScreen.addCommand(pauseCommand);
         trackWorkoutState(workoutState);
         state = STATE_WORKOUT;
     }
 
     public void finishWorkout() {
         workoutTimer.cancel();
 
         display.setCurrent(workoutCompleteScreen);
         playSound("finished");
         state = STATE_WORKOUT_COMPLETE;
     }
 
     // Status update API -------------------------------------------------------
 
     public void updateStep(int stepNum, WorkoutStep step) {
         action.setText(step.action + " for " + displayDuration(step.duration));
         stepCount.setText(stepNum + " of " + workout.steps.length);
         stepProgress.setValue(0);
         stepProgress.setMaxValue(step.duration);
         if (stepNum > 1) {
             playSound(step.action.toLowerCase());
         }
     }
 
     public void updateProgress(int progress, int totalTime) {
         stepProgress.setValue(progress);
         stepTime.setText(displayTime(progress));
         workoutProgress.setValue(totalTime);
         workoutTime.setText(displayTime(totalTime));
     }
 
     // Utilities ---------------------------------------------------------------
 
     private void trackWorkoutState(final WorkoutState workoutState) {
         workoutTimer = new Timer();
         workoutTimer.scheduleAtFixedRate(new TimerTask() {
             public void run() {
                 workoutState.increment();
             }
         }, 0, 1000);
     }
 
     private void playSound(String action) {
         try {
             InputStream in = getClass().getResourceAsStream("/" + action + ".mp3");
             Player player = Manager.createPlayer(in, "audio/mpeg");
             player.addPlayerListener(this);
             player.start();
         } catch (MediaException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     private String displayDuration(int n) {
         if (n <= 90) {
             return n + " seconds";
         }
         int min = n / 60;
         int sec = n % 60;
         if (sec == 0) {
           return min + " minutes";
         }
         return (min + (float)sec/60) + " minutes";
     }
 
     private String displayTime(int n) {
         int minutes = n / 60;
         int seconds = n % 60;
         return pad(minutes) + ":" + pad(seconds);
     }
 
     private String pad(int n) {
         if (n < 10) {
             return "0" + n;
         }
         return "" + n;
     }
 }
