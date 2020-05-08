 package ttProject.view.dialogs;
 
 import ttProject.controller.measurements.MeasurementsManager;
 import ttProject.view.listeners.OkButtonCombinedSListener;
 import ttProject.view.main.MyMainFrame;
 
 import javax.swing.*;
 import java.awt.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Michal
  * Date: 4.6.12
  * Time: 16:13
  * To change this template use File | Settings | File Templates.
  */
 public class CombinedPresetDialog extends JDialog {
 
     private MyMainFrame mainFrame;
     private JPanel jPanelCenter;
     private JButton okButton;
     private JTextField jTextFieldSwTimes;
     private JRadioButton jRadioButtonHarmonic;
     private JRadioButton jRadioButtonRandom;
 
 
     public CombinedPresetDialog(MyMainFrame mainFrame) {
         super(mainFrame);
         this.mainFrame = mainFrame;
         init();
     }
 
     private void init() {
         this.setModal(true);
         this.setLocation(400, 150);
         this.setSize(400, 150);
 
         this.jPanelCenter = new JPanel();
         this.add(jPanelCenter, BorderLayout.CENTER);
         this.add(new JLabel(" Set time of signals switching:"), BorderLayout.NORTH);
 
         jPanelCenter.setLayout(new GridBagLayout());
         GridBagConstraints constraints = new GridBagConstraints();
         buildComponents(constraints);
         addOKButton();
         okButton.addActionListener(new OkButtonCombinedSListener(this));
 
     }
 
     private void buildComponents(GridBagConstraints constraints) {
 
         buildSingleLabel(constraints, 0, 0, new JLabel("switching time (s):"));
         this.jTextFieldSwTimes = new JTextField(getTextFieldValues());
         buildSingleTextField(constraints,1,0,jTextFieldSwTimes);
 
         this.jRadioButtonHarmonic = new JRadioButton("Harmonic Signal",MeasurementsManager.getMeasurementManager().getCrossoverSignalWrapper().isHarmonicFirst());
        buildSingleRadioButton(constraints, 0, 1, jRadioButtonHarmonic);
 
         this.jRadioButtonRandom = new JRadioButton("Random Signal",MeasurementsManager.getMeasurementManager().getCrossoverSignalWrapper().isRandomFirst());
        buildSingleRadioButton(constraints,1,1,jRadioButtonRandom);
         ButtonGroup buttonGroup = new ButtonGroup();
         buttonGroup.add(jRadioButtonHarmonic);
         buttonGroup.add(jRadioButtonRandom);
     }
 
     private void buildSingleLabel(GridBagConstraints constraints, int gridx, int gridy, JLabel label) {
 
         constraints.fill = GridBagConstraints.HORIZONTAL;
         constraints.gridx = gridx;
         constraints.insets = new Insets(0, 5, 0, 0);
         constraints.gridy = gridy;
         jPanelCenter.add(label, constraints);
 
     }
 
     private void buildSingleTextField(GridBagConstraints constraints, int gridx, int gridy, JTextField jTextField) {
 
         constraints.fill = GridBagConstraints.HORIZONTAL;
         constraints.gridx = gridx;
         constraints.gridy = gridy;
         constraints.weightx = 0.5;
         constraints.gridwidth = 2;
         constraints.insets = new Insets(0, 2, 0, 0);
         jPanelCenter.add(jTextField, constraints);
 
     }
 
     private void buildSingleRadioButton(GridBagConstraints constraints, int gridx, int gridy, JRadioButton jRadioButton) {
 
         constraints.fill = GridBagConstraints.HORIZONTAL;
         constraints.gridx = gridx;
         constraints.gridy = gridy;
         constraints.weightx = 0;
         constraints.gridwidth = 1;
        constraints.insets = new Insets(2, 2, 2, 0);
         jPanelCenter.add(jRadioButton, constraints);
 
     }
 
     private void addOKButton() {
         JPanel bottomPanel = new JPanel();
         FlowLayout temp = new FlowLayout();
         temp.setAlignment(FlowLayout.LEFT);
         bottomPanel.setLayout(temp);
         this.okButton = new JButton("OK");
         bottomPanel.add(okButton);
         this.add(bottomPanel, BorderLayout.SOUTH);
     }
 
     private String getTextFieldValues() {
 
         String s = "";
 
         for (Integer integer : MeasurementsManager.getMeasurementManager().getCrossoverSignalWrapper().getSwitchingList()) {
             s = s + integer + ", ";
         }
         s = s.substring(0,s.length()-2);
 
         return s;
     }
 
     public JRadioButton getjRadioButtonRandom() {
         return jRadioButtonRandom;
     }
 
     public JRadioButton getjRadioButtonHarmonic() {
         return jRadioButtonHarmonic;
     }
 
     public JTextField getjTextFieldSwTimes() {
         return jTextFieldSwTimes;
     }
 }
