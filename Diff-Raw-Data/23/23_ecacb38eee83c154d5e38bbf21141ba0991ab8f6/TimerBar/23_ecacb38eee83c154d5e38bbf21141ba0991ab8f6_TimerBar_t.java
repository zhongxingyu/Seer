 package optitask.ui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 import javax.sound.sampled.LineEvent;
 import javax.sound.sampled.LineListener;
 import javax.swing.JProgressBar;
 import javax.swing.SwingConstants;
 import javax.swing.Timer;
 
 import optitask.store.AppPersistence;
 
 /**
  * TimerBar.java <br />
  * Purpose: Provides a controllable timer in a form of a {@link JProgressBar}.
  * @author Jerome Rodrigo
  * @since 0.8
  */
 
 public class TimerBar extends JProgressBar implements ActionListener {
 
     /**
      * The serial version UID.
      */
     private static final long serialVersionUID = -2250979200884046826L;
 
     /**
      * The application persistence module.
      * @see AppPersistence
      */
     private AppPersistence model;
 
     /**
      * The pomodoro time.
      * The timer will begin counting down, starting at this value.
      */
     private static long pomodoroTime = 0;
 
     /**
      * The break time.
      * The timer will begin counting up to this value.
      */
     private static long breakTime = 0;
 
     /**
      * The elapsed pomodoro time.
      */
     private static long ePomodoroTime = 0;
 
     /**
      * The elapsed break time.
      */
     private static long eBreakTime = 0;
 
     /**
      * This variable determines how many times the timer has iterated,
      * or completed its cycle.
      */
     private int currentCycle = 0;
 
     /**
      * Flag whether the current cycle is a long break cycle.
      */
     private static boolean isLongBreakNow = false;
 
     /**
      * The swing timer.
      */
     private Timer timer;
 
     /**
      * The current task panel.
      */
     private CurrentTaskPanel taskPanel;
 
     /**
      * The constant for number of seconds in 1 hour.
      */
     private static final int HOUR_MULT = 3600;
 
     /**
      * The constant for number of seconds in 1 minute.
      */
     private static final int MIN_MULT = 60;
 
     /**
      * The constant for number of milliseconds in 1 second.
      */
     private static final int MILLI_MULT = 1000;
 
     /**
      * Default constructor.
      * <B>Not used</B>.
      */
 
     public TimerBar() {
     }
 
     /**
      * Creates the progress bar.
      * @param mdl     the persistence module
      * @param tskPnl the current task panel
      */
 
     public TimerBar(final AppPersistence mdl,
             final CurrentTaskPanel tskPnl) {
         super(SwingConstants.HORIZONTAL, 0, 0);
         taskPanel = tskPnl;
         model = mdl;
         timer = new Timer(MILLI_MULT, this);
     }
 
     /**
      * Starts the timer.
      */
 
     public final void start() {
         resetTimer();
         timer.start();
         setStringPainted(true);
         playSound();
     }
 
     /**
      * Stops the timer.
      */
 
     public final void stop() {
         setStringPainted(false);
         timer.stop();
         resetTimer();
         taskPanel.setStatus(CurrentTaskPanel.NULL);
     }
 
     /**
      * Resets the current cycle to zero.
      */
 
     public final void resetCycle() {
         currentCycle = 0;
     }
 
     /**
      * Method to reset the timer.
      * Refreshes all the timer constants from the data file.
      * @see #start()
      * @see #stop()
      */
 
     private void resetTimer() {
         resetBreaks();
 
         setValue((int) pomodoroTime);
         ePomodoroTime = pomodoroTime; // reset time
         eBreakTime = 0;
         setMaximum((int) pomodoroTime);
     }
 
     /**
      * Refreshes the {@link #breakTime} value.
      */
 
     private void resetBreaks() {
         TimerBar.pomodoroTime = model.getSettings()
                 .getPomodoroTime() / MILLI_MULT;
 
         if (isLongBreakNow) {
             initLongBreak();
         } else {
             initShortBreak();
         }
     }
 
     /**
      * Sets the {@link #breakTime} with the long break value.
      */
 
     private void initLongBreak() {
         TimerBar.breakTime = model.getSettings()
                 .getLongBreak() / MILLI_MULT;
     }
 
     /**
      * Sets the {@link #breakTime} with the short break value.
      */
 
     private void initShortBreak() {
         TimerBar.breakTime = model.getSettings()
                 .getShortBreak() / MILLI_MULT;
     }
 
     /**
      * Method to get a formatted time stamp.
      * @param elapsedTime the value of time in seconds
      * @return a {@link String} representation of the time
      */
 
     private String getElapsedTime(final long elapsedTime) {
         String format = String.format("%%0%dd", 2);
         return String.format(format, elapsedTime / HOUR_MULT) + ":"
         + String.format(format, (elapsedTime % HOUR_MULT) / MIN_MULT) + ":"
         + String.format(format, elapsedTime % MIN_MULT);
     }
 
     @Override
     public final void actionPerformed(final ActionEvent e) {
        
         if (ePomodoroTime == pomodoroTime) {
            ePomodoroTime--; // To ensure progress is not 100% at start
             if (isLongBreakNow) {
                 taskPanel.setStatus(CurrentTaskPanel.WORKING_THEN_LBRK);
             } else {
                 taskPanel.setStatus(CurrentTaskPanel.WORKING_THEN_SBRK);
             }
         }
 
         if (eBreakTime == 0) {
             setString(getElapsedTime(ePomodoroTime));
            setValue((int) ePomodoroTime);
             ePomodoroTime--;
         }
 
        /*
         * Counts down to -1 to give an extra second so that 00:00:01 can
         * be displayed.
         */
        if (ePomodoroTime == -1) {
             setString(getElapsedTime(eBreakTime));
             setValue((int) eBreakTime);
             eBreakTime++;
         }
 
         if (eBreakTime == 1) {
             playSound();
             setMaximum((int) breakTime);
             if (breakTime == model.getSettings().getLongBreak() / MILLI_MULT) {
                 taskPanel.setStatus(CurrentTaskPanel.LONG_BREAK);
             } else {
                 taskPanel.setStatus(CurrentTaskPanel.SHORT_BREAK);
             }
         }
 
        if (eBreakTime > breakTime) {
             timer.stop();
             playSound();
             setString("Done!");
 
             // Increments the current pomodoro value
             taskPanel.incrementPomodoro();
 
             taskPanel.setStatus(CurrentTaskPanel.NULL);
 
             // Enables a long break after an interval
             if (model.getSettings().isWillLongBreak()) {
                 if (currentCycle >= model.getSettings()
                         .getIncrementInterval() - 1) {
                     isLongBreakNow = true;
                     currentCycle = 0; // Reset the cycle
                 } else {
                     isLongBreakNow = false;
                     currentCycle++;
                 }
             }
         }
     }
 
     /**
      * Plays a sound file.
      */
 
     private static void playSound() {
         new SoundPlayer().start();
     }
 
     /**
      * SoundPlayer. <br />
      * Purpose: Plays a sound file (.wav) at the specified URL string
      * @author Jerome
      * @version 0.8.2
      * @since 0.8.2
      */
 
     private static final class SoundPlayer extends Thread implements Runnable {
 
         /**
          * The sound file path.
          */
         private static String notifyUrl;
 
         /**
          * The sound clip.
          */
         private static Clip clip;
 
         /**
          * Creates the SoundPlayer with the default sound.
          */
 
         public SoundPlayer() {
             this("/optitask/assests/notify.wav");
         }
 
         /**
          * Creates the SoundPlayer with the specified sound file path.
          * @param url the path to the sound file
          */
 
         public SoundPlayer(final String url) {
             notifyUrl = url;
         }
 
         @Override
         public void run() {
             try {
                 clip = AudioSystem.getClip();
                 AudioInputStream inputStream = AudioSystem
                         .getAudioInputStream(TimerBar.class
                                 .getResourceAsStream(notifyUrl));
                 clip.open(inputStream);
                 clip.start();
                 clip.addLineListener(new LineListener() {
 
                     @Override
                     public void update(final LineEvent e) {
                         if (e.getType() == LineEvent.Type.STOP) {
                             e.getLine().close();
                         }
                     }
 
                 });
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
 
     }
 }
