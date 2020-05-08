 package ch9k.plugins.liteanalyzer;
 
 import ch9k.core.I18n;
 import ch9k.core.settings.Settings;
 import ch9k.core.settings.SettingsChangeEvent;
 import ch9k.core.settings.SettingsChangeListener;
 import javax.swing.GroupLayout;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSpinner;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 /**
  * Preference pane for a LiteTextAnalyzer.
  */
 public class LiteTextAnalyzerPreferencePane
         extends JPanel implements SettingsChangeListener, ChangeListener {
     /**
      * Maximum number of subjects to search.
      */
     public static final String MAX_SUBJECTS = "maxSubjects";
 
     /**
      * Maximum number of messages to analyze.
      */
     public static final String MAX_MESSAGES = "maxMessages";
 
     /**
      * Settings to manipulate.
      */
     private final Settings settings;
 
     /**
      * NUM_SUBJECTS spinner.
      */
     private final JSpinner maxSubjectsSpinner;
 
     /**
      * MAX_MESSAGES spinner.
      */
     private final JSpinner maxMessagesSpinner;
 
     /**
      * Constructor.
      * @param settings Settings to manipulate.
      */
     public LiteTextAnalyzerPreferencePane(final Settings settings) {
         this.settings = settings;
 
         GroupLayout layout = new GroupLayout(this);
 
         layout.setAutoCreateGaps(true);
         layout.setAutoCreateContainerGaps(true);
 
         JLabel maxSubjectsLabel = new JLabel(
                 I18n.get("ch9k.plugins.liteanalyzer", "max_subjects"));
         JLabel maxMessagesLabel = new JLabel(
                 I18n.get("ch9k.plugins.liteanalyzer", "max_messages"));
         maxSubjectsSpinner = new JSpinner(new SpinnerNumberModel(
                 settings.getInt(MAX_SUBJECTS),
                 1, 5, 1));
         maxMessagesSpinner = new JSpinner(new SpinnerNumberModel(
                 settings.getInt(MAX_MESSAGES),
                 1, 30, 1));
 
         layout.setHorizontalGroup(layout.createSequentialGroup()
             .addGroup(layout.createParallelGroup()
                 .addComponent(maxSubjectsLabel)
                 .addComponent(maxMessagesLabel))
             .addGroup(layout.createParallelGroup()
                 .addComponent(maxSubjectsSpinner)
                 .addComponent(maxMessagesSpinner)));
 
         layout.setVerticalGroup(layout.createSequentialGroup()
             .addGroup(layout.createParallelGroup()
                 .addComponent(maxSubjectsLabel)
                .addComponent(maxSubjectsSpinner, GroupLayout.PREFERRED_SIZE,
                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
             .addGroup(layout.createParallelGroup()
                 .addComponent(maxMessagesLabel)
                .addComponent(maxMessagesSpinner, GroupLayout.PREFERRED_SIZE,
                        GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)));
 
         setLayout(layout);
 
         maxSubjectsSpinner.addChangeListener(this);
         maxMessagesSpinner.addChangeListener(this);
         settings.addSettingsListener(this);
     }
 
     @Override
     public void settingsChanged(SettingsChangeEvent event) {
         if(MAX_SUBJECTS.equals(event.getKey())) {
             maxSubjectsSpinner.setValue(settings.getInt(MAX_SUBJECTS));
         }
 
         if(MAX_MESSAGES.equals(event.getKey())) {
             maxMessagesSpinner.setValue(settings.getInt(MAX_MESSAGES));
         }
     }
 
     @Override
     public void stateChanged(ChangeEvent event) {
         if(event.getSource() == maxSubjectsSpinner) {
             settings.setInt(MAX_SUBJECTS,
                     (Integer) maxSubjectsSpinner.getValue());
         }
 
         if(event.getSource() == maxMessagesSpinner) {
             settings.setInt(MAX_MESSAGES,
                     (Integer) maxMessagesSpinner.getValue());
         }
     }
 }
