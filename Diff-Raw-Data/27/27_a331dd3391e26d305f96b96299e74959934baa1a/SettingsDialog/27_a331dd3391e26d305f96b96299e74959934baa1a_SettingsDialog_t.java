 package optitask.ui;
 
 import java.awt.Color;
 import java.awt.Toolkit;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JDialog;
 import javax.swing.JFormattedTextField;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSeparator;
 import javax.swing.JSpinner;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.UIManager;
 import javax.swing.WindowConstants;
 import javax.swing.border.TitledBorder;
 
 import net.miginfocom.swing.MigLayout;
 import optitask.AppController;
 import optitask.store.AppPersistence;
 import optitask.util.Settings;
 
 /**
  * SettingsDialog.java <br />
  * Purpose: Displays a dialog for users to edit the application settings.
  * @author Jerome
  * @version 0.8.2
  * @since 0.8
  */
 
 public class SettingsDialog extends JDialog {
 
     /**
      * The serial version UID.
      */
     private static final long serialVersionUID = 8299983443943142835L;
 
     /**
      * Stores a reference to the persistence module.
      * @see AppPersistence
      */
     private AppPersistence model;
 
     /**
      * Stores a reference to the application controller.
      * @see AppController
      */
     private AppController controller;
 
     /**
      * Allows the user to input the short break time.
      */
     private JSpinner shortBreakTimeSpinner;
 
     /**
      * Allows the user to input the interval until a long break.
      */
     private JSpinner longBreakAfterSpinner;
 
     /**
      * Allows the user to input the pomodoro duration.
      */
     private JSpinner pomodoroTimeSpinner;
 
     /**
      * Allows the user to input the long break time.
      */
     private JSpinner longBreakTimeSpinner;
 
     /**
      * Allows the user to input if a long break should occur
      * using a JCheckBox.
      */
     private JCheckBox willLongBreakCheckBox;
 
     /**
      * Displays the label for the long break.
      * @see #setEnabledIncrementSettings(boolean)
      */
     private JLabel lblLongBreak;
 
     /**
      * Displays the label for the long break after value.
      * @see #setEnabledIncrementSettings(boolean)
      */
     private JLabel lblLongBreakAfter;
 
     /**
      * Constant multiplier for conversion between milliseconds.
      */
     private static final int MILLI_MULT = 1000;
 
     /**
      * Constant for number of seconds in 1 minute.
      */
     private static final int MINUTE_IN_SECONDS = 60;
 
     /**
      * Creates the dialog.
      * <B>Not used</B>.
      */
 
     public SettingsDialog() {
         initialize();
     }
 
     /**
      * Creates and initialises the dialog.
      * @param mdl      the persistence module / model
      * @param cntrller the application controller
      */
 
     public SettingsDialog(final AppPersistence mdl,
             final AppController cntrller) {
         controller = cntrller;
         model = mdl;
         initialize();
         if (!mdl.getSettings().isWillLongBreak()) {
             setEnabledIncrementSettings(false);
         }
     }
 
     /**
      * Creates the user interface components.
      */
 
     private void initialize() {
         setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
         setIconImage(Toolkit.getDefaultToolkit().getImage(
                 SettingsDialog.class
                 .getResource("/optitask/assests/settings.gif")));
         setSize(320, 300);
         setModal(true);
         setResizable(false);
         setTitle("Settings");
         getContentPane().setLayout(new MigLayout("",
                 "[135px][53px][50px][64px]",
                 "[26px][26px][151px][2px][39px]"));
 
         JLabel lblShortBreak = new JLabel("Short Break Time:");
         getContentPane().add(lblShortBreak, "cell 0 0,grow");
 
         shortBreakTimeSpinner = new JSpinner();
         lblShortBreak.setLabelFor(shortBreakTimeSpinner);
         shortBreakTimeSpinner.setModel(new SpinnerNumberModel(1, 1, 30, 1));
         preventKeyboardInputJSpinner(shortBreakTimeSpinner);
         getContentPane().add(shortBreakTimeSpinner, "cell 2 0,grow");
 
         JPanel longBreakTimePanel = new JPanel();
         longBreakTimePanel.setBorder(new TitledBorder(UIManager
                 .getBorder("TitledBorder.border"), "Long Break Settings",
                 TitledBorder.LEADING, TitledBorder.TOP, null, null));
         getContentPane().add(longBreakTimePanel, "cell 0 2 4 1,grow");
         longBreakTimePanel.setLayout(new MigLayout("",
                 "[137.00px][50px][69px]",
                 "[26px][26px][25px]"));
 
         lblLongBreakAfter = new JLabel("Long Break After:");
         longBreakTimePanel.add(lblLongBreakAfter, "cell 0 0,grow");
         lblLongBreakAfter.setLabelFor(longBreakAfterSpinner);
 
         longBreakAfterSpinner = new JSpinner();
         longBreakTimePanel.add(longBreakAfterSpinner, "cell 1 0,grow");
         longBreakAfterSpinner.setModel(new SpinnerNumberModel(1, 1, 10, 1));
         preventKeyboardInputJSpinner(longBreakAfterSpinner);
 
         lblLongBreak = new JLabel("Long Break Time:");
         longBreakTimePanel.add(lblLongBreak, "cell 0 1,grow");
         lblLongBreak.setLabelFor(longBreakTimeSpinner);
 
         longBreakTimeSpinner = new JSpinner();
         longBreakTimeSpinner.setModel(new SpinnerNumberModel(1, 1, 60, 1));
         longBreakTimePanel.add(longBreakTimeSpinner, "cell 1 1,grow");
         preventKeyboardInputJSpinner(longBreakTimeSpinner);
 
         JLabel lblToggleLongBreak = new JLabel("Toggle Long Break:");
         longBreakTimePanel.add(lblToggleLongBreak, "cell 0 2,grow");
 
         willLongBreakCheckBox = new JCheckBox("");
         willLongBreakCheckBox.setActionCommand("Toggle Increment");
         willLongBreakCheckBox.addActionListener(controller);
         longBreakTimePanel.add(willLongBreakCheckBox,
                 "cell 2 2,growx,aligny bottom");
 
         JLabel lblMinutes3 = new JLabel("minutes");
         longBreakTimePanel.add(lblMinutes3,
                 "cell 2 1,alignx left,aligny center");
 
         JLabel lblCycles = new JLabel("pomodoros");
         longBreakTimePanel.add(lblCycles, "cell 2 0,growx,aligny center");
 
         JLabel lblTaskDuration = new JLabel("Pomodoro Time:");
         getContentPane().add(lblTaskDuration, "cell 0 1,grow");
 
         pomodoroTimeSpinner = new JSpinner();
         lblTaskDuration.setLabelFor(pomodoroTimeSpinner);
         pomodoroTimeSpinner.setModel(new SpinnerNumberModel(1, 1, 60, 1));
         preventKeyboardInputJSpinner(pomodoroTimeSpinner);
         getContentPane().add(pomodoroTimeSpinner, "cell 2 1,grow");
 
         JButton btnDefault = new JButton("Default");
         btnDefault.setActionCommand("Default Settings");
         btnDefault.addActionListener(controller);
         getContentPane().add(btnDefault, "cell 2 4,alignx center,growy");
 
         JButton btnSave = new JButton("Save");
         btnSave.setActionCommand("Save Settings");
         btnSave.addActionListener(controller);
         getContentPane().add(btnSave, "cell 3 4,alignx center,growy");
 
         JSeparator separator = new JSeparator();
         getContentPane().add(separator, "cell 0 3 4 1,growx,aligny top");
 
         JLabel lblMinutes1 = new JLabel("minutes");
         getContentPane().add(lblMinutes1, "cell 3 0,growx,aligny center");
 
         JLabel lblMinutes2 = new JLabel("minutes");
         getContentPane().add(lblMinutes2, "cell 3 1,growx,aligny center");
 
         setSettings(model.getSettings());
     }
 
     /**
      * Gets the settings from the user interface components.
      * @return the {@link Settings} object
      */
 
     public final Settings getSettings() {
         Settings set = new Settings();
         set.setShortBreak(minutesToMilliseconds((Integer) shortBreakTimeSpinner
                 .getValue()));
         set.setIncrementInterval((Integer) longBreakAfterSpinner.getValue());
         set.setLongBreak((minutesToMilliseconds((Integer) longBreakTimeSpinner
                 .getValue())));
         set.setPomodoroTime(minutesToMilliseconds((Integer) pomodoroTimeSpinner
                 .getValue()));
         set.setWillLongBreak(willLongBreakCheckBox.isSelected());

        // Long break value cannot be equal or less than short break
        if (set.getLongBreak() > set.getShortBreak()) {
            return set;
        } else {
            return null;
        }
     }
 
     /**
      * Sets the settings to be displayed.
      * @param settings the {@link Settings} to be set
      */
 
     public final void setSettings(final Settings settings) {
         shortBreakTimeSpinner.setValue(millisecondsToMinutes(settings
                 .getShortBreak()));
         longBreakAfterSpinner.setValue(settings.getIncrementInterval());
         pomodoroTimeSpinner.setValue(millisecondsToMinutes(settings
                 .getPomodoroTime()));
         longBreakTimeSpinner.setValue(millisecondsToMinutes(settings
                 .getLongBreak()));
         willLongBreakCheckBox.setSelected(settings.isWillLongBreak());
     }
 
     /**
      * Sets the state whether the user interface components
      * related to the long break should be enabled or disabled.
      * @param bEnabled the flag to enabled/disable the components
      */
 
     public final void setEnabledIncrementSettings(final boolean bEnabled) {
         longBreakAfterSpinner.setEnabled(bEnabled);
         longBreakTimeSpinner.setEnabled(bEnabled);
         lblLongBreak.setEnabled(bEnabled);
         lblLongBreakAfter.setEnabled(bEnabled);
     }
 
     /**
      * Gets the state whether the long break settings are enabled or disabled.
      * @return <code>true</code> if the settings are enabled;
      *         <code>false</code> otherwise.
      */
 
     public final boolean isEnabledIncrementSettings() {
         return longBreakAfterSpinner.isEnabled()
                 && longBreakTimeSpinner.isEnabled();
     }
 
     /**
      * Converts milliseconds to minutes.
      * @param milliseconds the value of time in milliseconds
      * @return value of time in minutes
      */
 
     private int millisecondsToMinutes(final long milliseconds) {
         return (int) (milliseconds / MILLI_MULT) / MINUTE_IN_SECONDS;
     }
 
     /**
      * Converts minutes to milliseconds.
      * @param minutes the value of time in minutes.
      * @return value of time in milliseconds
      */
 
     private long minutesToMilliseconds(final int minutes) {
         return (long) minutes * MILLI_MULT * MINUTE_IN_SECONDS;
     }
 
     /**
      * Prevents keyboard input for a {@link JSpinner}.
      * @param spinner the JSpinner
      */
 
     private void preventKeyboardInputJSpinner(final JSpinner spinner) {
         JFormattedTextField tf = ((JSpinner.DefaultEditor) spinner.getEditor())
                 .getTextField();
         tf.setEditable(false);
         tf.setBackground(Color.WHITE);
     }
 }
