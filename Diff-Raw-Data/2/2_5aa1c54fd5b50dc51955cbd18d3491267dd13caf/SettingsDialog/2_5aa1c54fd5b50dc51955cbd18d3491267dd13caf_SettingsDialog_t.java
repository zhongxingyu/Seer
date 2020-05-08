 package de.aidger.view;
 
 import static de.aidger.utils.Translation._;
 
 import java.awt.Frame;
 import java.util.List;
 
 import javax.swing.JDialog;
 
 import de.aidger.controller.ActionNotFoundException;
 import de.aidger.controller.ActionRegistry;
 import de.aidger.controller.actions.DialogAbortAction;
 import de.aidger.controller.actions.SettingsSaveAction;
 import de.aidger.controller.actions.SettingsBrowseAction;
 import de.aidger.model.Runtime;
 import de.aidger.view.utils.InputPatternFilter;
 import de.unistuttgart.iste.se.adohive.util.tuple.Pair;
 import javax.swing.ButtonModel;
 
 public class SettingsDialog extends JDialog {
 
     private static final long serialVersionUID = 1L;
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JSpinner activitiesSpinner;
     private javax.swing.JTextField anonTextField;
     private javax.swing.JButton browseButton;
     private javax.swing.ButtonGroup calculationGroup;
     private javax.swing.JTextField historicTextField;
     private javax.swing.JButton jButton1;
     private javax.swing.JButton jButton2;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JLabel jLabel8;
     private javax.swing.JLabel jLabel9;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JRadioButton jRadioButton1;
     private javax.swing.JRadioButton jRadioButton2;
     private javax.swing.JTabbedPane jTabbedPane1;
     private javax.swing.JComboBox langComboBox;
     private javax.swing.JTextField nameTextField;
     private javax.swing.JCheckBox openCheckBox;
     private javax.swing.JTextField pdfTextField;
     private javax.swing.JTextField pessimisticTextField;
     private javax.swing.JCheckBox saveCheckBox;
     private javax.swing.JTextField toleranceTextField;
     // End of variables declaration//GEN-END:variables
 
     /**
      * @param owner
      */
     public SettingsDialog(Frame owner) {
         super(owner, true);
         initComponents();
 
         String lang = Runtime.getInstance().getOption("language");
 
         try {
             jButton1.setAction(ActionRegistry.getInstance().get(
                     DialogAbortAction.class.getName()));
             jButton2.setAction(ActionRegistry.getInstance().get(
                     SettingsSaveAction.class.getName()));
             browseButton.setAction(ActionRegistry.getInstance().get(
                     SettingsBrowseAction.class.getName()));
         } catch (ActionNotFoundException e) {
             UI.displayError(e.getMessage());
         }
 
         saveCheckBox.setSelected(Boolean.parseBoolean(
                 Runtime.getInstance().getOption("auto-save")));
         openCheckBox.setSelected(Boolean.parseBoolean(
                 Runtime.getInstance().getOption("auto-open")));
         nameTextField.setText(Runtime.getInstance().getOption("name"));
         InputPatternFilter.addFilter(nameTextField, "[a-zA-Z]{0,2}");
 
         pdfTextField.setText(Runtime.getInstance().getOption("pdf-viewer"));
         pessimisticTextField.setText(Runtime.getInstance().getOption(
                 "pessimistic-factor"));
         historicTextField.setText(Runtime.getInstance().getOption(
                 "historic-factor"));
         anonTextField.setText(Runtime.getInstance().getOption("anonymize-time"));
         toleranceTextField.setText(Runtime.getInstance().getOption("tolerance"));
         activitiesSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer
             .valueOf(Runtime.getInstance().getOption("activities")), null,
             null, Integer.valueOf(1)));
 
         ButtonModel model = Runtime.getInstance().getOption("calc-method")
                 .equals("1") ? jRadioButton1.getModel() : jRadioButton2.getModel();
         calculationGroup.setSelected(model, true);
 
         /* Add all languages to the combobox and select the correct one */
         List<Pair<String, String>> langs = Runtime.getInstance().getLanguages();
         String[] longLangs = new String[langs.size()];
         int selected = -1;
 
         for (int i = 0; i < langs.size(); ++i) {
             longLangs[i] = langs.get(i).snd();
             if (langs.get(i).fst().equals(lang)) {
                 selected = i;
             }
         }
 
         langComboBox.setModel(new javax.swing.DefaultComboBoxModel(longLangs));
         langComboBox.setSelectedIndex(selected);
 
         setLocationRelativeTo(null);
     }
 
     /**
      * Get the currently entered username.
      * 
      * @return The username
      */
     public String getUsername() {
         return nameTextField.getText();
     }
 
     /**
      * Get the currently entered pdf viewer.
      * 
      * @return The pdf viewer
      */
     public String getPDFViewer() {
         return pdfTextField.getText();
     }
 
     /**
      * Get the pessimistic factor currently entered.
      *
      * @return The pessimistic factor
      */
     public String getPessimisticFactor() {
         return pessimisticTextField.getText();
     }
 
     /**
      * Get the historical factor currently entered.
      *
      * @return The historical factor
      */
     public String getHistoricFactor() {
         return historicTextField.getText();
     }
 
     /**
      * Get the time until assistants get anonymized.
      *
      * @return The anonymization time
      */
     public String getAnonymizationTime() {
         return anonTextField.getText();
     }
 
     /**
      * Get the tolerance for controlling currently entered.
      *
      * @return The tolerance
      */
     public String getTolerance() {
         return toleranceTextField.getText();
     }
 
     /**
      * Get the number of activities currently entered
      * 
      * @return The number of activities
      */
     public int getNumOfActivities() {
         return (Integer) activitiesSpinner.getValue();
     }
 
     /**
      * Get the selected calculation method
      *
      * @return 1 is pessimistic, 0 is historical
      */
     public int getSelectedMethod() {
         return calculationGroup.getSelection().equals(jRadioButton1.getModel()) ?
             1 : 0;
     }
 
     /**
      * Get whether the save checkbox is selected
      * 
      * @return True if the checkbox is selected
      */
     public boolean isSaveSelected() {
         return saveCheckBox.isSelected();
     }
 
     /**
      * Get whether the open checkbox is selected
      * 
      * @return True if the checkbox is selected
      */
     public boolean isOpenSelected() {
         return openCheckBox.isSelected();
     }
 
     /**
      * Get the index of the selected language.
      * 
      * @return The index of the selected language
      */
     public int getSelectedLanguage() {
         return langComboBox.getSelectedIndex();
     }
 
     /**
      * Set the path of the pdf viewer.
      *
      * @param path
      *            The new pdf viewer path
      */
     public void setPdfPath(String path) {
         pdfTextField.setText(path);
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         calculationGroup = new javax.swing.ButtonGroup();
         jTabbedPane1 = new javax.swing.JTabbedPane();
         jPanel1 = new javax.swing.JPanel();
         langComboBox = new javax.swing.JComboBox();
         jLabel4 = new javax.swing.JLabel();
         activitiesSpinner = new javax.swing.JSpinner();
         jLabel3 = new javax.swing.JLabel();
         jLabel1 = new javax.swing.JLabel();
         nameTextField = new javax.swing.JTextField();
         jLabel2 = new javax.swing.JLabel();
         pdfTextField = new javax.swing.JTextField();
         openCheckBox = new javax.swing.JCheckBox();
         saveCheckBox = new javax.swing.JCheckBox();
         browseButton = new javax.swing.JButton();
         jPanel2 = new javax.swing.JPanel();
         jLabel5 = new javax.swing.JLabel();
         jLabel6 = new javax.swing.JLabel();
         pessimisticTextField = new javax.swing.JTextField();
         historicTextField = new javax.swing.JTextField();
         jLabel7 = new javax.swing.JLabel();
         anonTextField = new javax.swing.JTextField();
         jLabel8 = new javax.swing.JLabel();
         toleranceTextField = new javax.swing.JTextField();
         jLabel9 = new javax.swing.JLabel();
         jRadioButton1 = new javax.swing.JRadioButton();
         jRadioButton2 = new javax.swing.JRadioButton();
         jButton2 = new javax.swing.JButton();
         jButton1 = new javax.swing.JButton();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
         setTitle(_("Settings"));
 
         jLabel4.setText(_("Language:"));
 
         jLabel3.setText(_("Amount of activities:"));
         jLabel3.setToolTipText(_("The amount of activities shown in detailviews."));
 
         jLabel1.setText(_("Initials:"));
         jLabel1.setToolTipText(_("The initials used in activities."));
 
         nameTextField.setToolTipText(_("The initials used in activities."));
 
         jLabel2.setText(_("PDF Viewer:"));
 
         pdfTextField.setEditable(false);
 
        openCheckBox.setText(_("Open reports automatically after exporting"));
 
         saveCheckBox.setText(_("Save Tabs when quitting"));
 
         browseButton.setText(_("Browse"));
 
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel1Layout.createSequentialGroup()
                         .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(jLabel2)
                                     .addComponent(jLabel1)
                                     .addComponent(jLabel4))
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 90, Short.MAX_VALUE)
                                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                     .addComponent(langComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                     .addGroup(jPanel1Layout.createSequentialGroup()
                                         .addComponent(pdfTextField)
                                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                         .addComponent(browseButton))
                                     .addComponent(nameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 200, Short.MAX_VALUE)))
                             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                 .addComponent(jLabel3)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 230, Short.MAX_VALUE)
                                 .addComponent(activitiesSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)))
                         .addContainerGap())
                     .addGroup(jPanel1Layout.createSequentialGroup()
                         .addComponent(openCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
                         .addContainerGap())
                     .addGroup(jPanel1Layout.createSequentialGroup()
                         .addComponent(saveCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addGap(278, 278, 278))))
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel1))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(pdfTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel2)
                     .addComponent(browseButton))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(langComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel4))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(activitiesSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel3))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(openCheckBox)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(saveCheckBox)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         jTabbedPane1.addTab(_("General"), jPanel1);
 
         jLabel5.setText(_("Pessimistic Factor:"));
 
         jLabel6.setText(_("Historical Factor:"));
 
         jLabel7.setText(_("Time until anonymization in days:"));
 
         jLabel8.setText(_("Tolerance of Controlling:"));
 
         jLabel9.setText(_("Calculation method:"));
 
         calculationGroup.add(jRadioButton1);
         jRadioButton1.setText(_("Pessimistic"));
 
         calculationGroup.add(jRadioButton2);
         jRadioButton2.setText(_("Historical"));
 
         javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
         jPanel2.setLayout(jPanel2Layout);
         jPanel2Layout.setHorizontalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel2Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanel2Layout.createSequentialGroup()
                         .addComponent(jLabel7)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 90, Short.MAX_VALUE)
                         .addComponent(anonTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                         .addComponent(jLabel8)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 90, Short.MAX_VALUE)
                         .addComponent(toleranceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                         .addComponent(jLabel5)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 90, Short.MAX_VALUE)
                         .addComponent(pessimisticTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                         .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jLabel6)
                             .addComponent(jLabel9))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 90, Short.MAX_VALUE)
                         .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jRadioButton1)
                             .addComponent(historicTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jRadioButton2))))
                 .addContainerGap())
         );
         jPanel2Layout.setVerticalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel2Layout.createSequentialGroup()
                 .addGap(15, 15, 15)
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel7)
                     .addComponent(anonTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(toleranceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel8))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(pessimisticTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel5))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(historicTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel6))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel9)
                     .addComponent(jRadioButton1))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jRadioButton2)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         jTabbedPane1.addTab(_("Reports"), jPanel2);
 
         jButton2.setText("Speichern");
 
         jButton1.setText("Abbrechen");
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addContainerGap(232, Short.MAX_VALUE)
                 .addComponent(jButton2)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jButton1)
                 .addContainerGap())
             .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 411, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 254, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jButton1)
                     .addComponent(jButton2))
                 .addGap(15, 15, 15))
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
 }
