 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.jackl.gui;
 
 import gnu.io.SerialPort;
 import java.awt.Color;
 import java.io.PrintStream;
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.util.LinkedList;
 import java.util.Locale;
 import javax.swing.JOptionPane;
 import javax.swing.JRadioButtonMenuItem;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.text.DefaultCaret;
 import org.jackl.Settings;
 import org.jackl.serial.*;
 
 /**
  *
  * @author Fabian
  */
 public class GUI extends javax.swing.JFrame {
 
     private DecimalFormat f;
     private DecimalFormat s;
     private boolean voltageChanged;
     private SerialCommunicator serial;
     private double[] lastCurrentValues;
     private PrintStream systemOutput;
     private PrintStream consoleOutput;
 
     public GUI() {
         try {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
             System.out.println(ex);
         }
         initAttributes();
         initComponents();
         Color[] colors = {Color.GREEN, Color.YELLOW, Color.ORANGE, Color.RED};
         float[] distrib = {0.5f, 0.75f, 0.825f, 1.0f};
         loadBar.setUI(new GradientPalletProgressBarUI(colors, distrib));
         refreshCOMPorts();
         enableComponents(false);
         DefaultCaret caret = (DefaultCaret) consoleArea.getCaret();
         caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
         this.setLocationRelativeTo(null);
     }
 
     private void initAttributes() {
         DecimalFormatSymbols decimalSymbol = new DecimalFormatSymbols(Locale.getDefault());
         decimalSymbol.setDecimalSeparator('.');
         decimalSymbol.setGroupingSeparator(',');
         f = new DecimalFormat("#0.00", decimalSymbol);
         s = new DecimalFormat("#00.00", decimalSymbol);
         voltageChanged = true;
         serial = new SerialCommunicator(this);
         lastCurrentValues = new double[Settings.getHowManyValues()];
         for (int i = 0; i < lastCurrentValues.length; i++) {
             lastCurrentValues[i] = 0;
         }
         systemOutput = System.out;
         consoleOutput = new PrintStream(System.out) {
             @Override
             public void println(String s) {
                 consoleArea.append(s + "\n");
             }
         };
     }
 
     public void setLastCurrentValues(double[] lcv) {
         lastCurrentValues = lcv;
     }
 
     private int setVoltageSlider(double volt) {
         if (volt >= 0 && volt <= 12) //Filter valid Values
         {
             int value = (int) (volt * 100) / 5;
             voltageSlider.setValue(value);
             voltageChanged = true;
             return value;
         }
         return -1;
     }
 
     public boolean hasVoltageChanged() {
         if (voltageChanged) {
             voltageChanged = false;
             return true;
         } else {
             return false;
         }
     }
 
     public void setVoltage(double volt) {
         if (volt >= 0 && volt <= 12) {
             if (!voltageSlider.isFocusOwner()) {
                 setVoltageSlider(volt);
             }
             voltageTextField.setText(f.format(volt));
             voltageChanged = true;
         }
     }
 
     public double getVoltage() {
         if (voltageSlider.getValue() * 5 / 100 == Double.parseDouble(voltageTextField.getText())) {
             return voltageSlider.getValue() * 5 / 100;
         }
         return -1;
     }
 
     public void setCurrent(double current) {
         if (Settings.displayAverage()) {
             for (int i = lastCurrentValues.length - 1; i >= 1; i--) {
                 lastCurrentValues[i] = lastCurrentValues[i - 1];
             }
             lastCurrentValues[0] = current;
             current = 0;
             for (double d : lastCurrentValues) {
                 current += d;
             }
             current = current / lastCurrentValues.length;
         }
         currentTextField.setText(f.format(current));
         loadBar.setValue((int) (current * 100));
     }
 
     public void setOutput(boolean on) {
         if (on) {
             jTextField1.setBackground(Color.green);
         } else {
             jTextField1.setBackground(Color.red);
         }
     }
 
     private void refreshCOMPorts() {
         LinkedList<String> coms = serial.getSerialPorts();
         while (comButtonGroup.getElements().hasMoreElements()) {
             javax.swing.AbstractButton b = comButtonGroup.getElements().nextElement();
             comButtonGroup.remove(b);
             comSelectMenu.remove(b);
         }
         comButtonGroup.clearSelection();
         if (coms != null && !coms.isEmpty()) {
             for (String c : coms) {
                 JRadioButtonMenuItem tmp = new JRadioButtonMenuItem(c);
                 tmp.setActionCommand(c);
                 comButtonGroup.add(tmp);
                 comSelectMenu.add(tmp);
             }
         } else {
             JRadioButtonMenuItem tmp = new JRadioButtonMenuItem("No COM Port available");
             tmp.setActionCommand("null");
             comButtonGroup.add(tmp);
             comSelectMenu.add(tmp);
         }
     }
 
     private boolean connect() {
         if (comButtonGroup.getSelection() != null) {
             System.out.println(comButtonGroup.getSelection().getActionCommand());
             boolean out = serial.openSerialPort(comButtonGroup.getSelection().getActionCommand());
             return out;
         }
         JOptionPane.showMessageDialog(this, "Can't connect because no COM Port is selected", "ERROR", JOptionPane.ERROR_MESSAGE);
         return false;
     }
 
     private void enableComponents(boolean enable) {
         onOffToggleButton.setEnabled(enable);
         voltageSlider.setEnabled(enable);
     }
 
     private void setSettings() {
         Settings.setRefreshDelay((int) refreshDelaySpinner.getValue());
         switch (baudrateComboBox.getSelectedIndex()) {
             case 0:
                 Settings.setBaudrate(9600);
                 break;
             case 1:
                 Settings.setBaudrate(19200);
                 break;
             case 2:
                 Settings.setBaudrate(38400);
                 break;
             case 3:
                 Settings.setBaudrate(57600);
                 break;
             case 4:
                 Settings.setBaudrate(115200);
                 break;
             default:
                 Settings.setBaudrate(9600);
                 break;
         }
         switch (dataBitsComboBox.getSelectedIndex()) {
             case 0:
                 Settings.setDataBits(SerialPort.DATABITS_5);
                 break;
             case 1:
                 Settings.setDataBits(SerialPort.DATABITS_6);
                 break;
             case 2:
                 Settings.setDataBits(SerialPort.DATABITS_7);
                 break;
             case 3:
                 Settings.setDataBits(SerialPort.DATABITS_8);
                 break;
             default:
                 Settings.setDataBits(SerialPort.DATABITS_8);
                 break;
         }
         switch (stopBitsComboBox.getSelectedIndex()) {
             case 0:
                 Settings.setStopBits(0);
                 break;
             case 1:
                 Settings.setStopBits(SerialPort.STOPBITS_1);
                 break;
             case 2:
                 Settings.setStopBits(SerialPort.STOPBITS_1_5);
                 break;
             case 3:
                 Settings.setStopBits(SerialPort.STOPBITS_2);
                 break;
             default:
                 Settings.setStopBits(SerialPort.STOPBITS_1);
                 break;
         }
         switch (parityComboBox.getSelectedIndex()) {
             case 0:
                 Settings.setParity(SerialPort.PARITY_NONE);
                 break;
             case 1:
                 Settings.setParity(SerialPort.PARITY_ODD);
                 break;
             case 2:
                 Settings.setParity(SerialPort.PARITY_EVEN);
                 break;
             case 3:
                 Settings.setParity(SerialPort.PARITY_MARK);
                 break;
             case 4:
                 Settings.setParity(SerialPort.PARITY_SPACE);
                 break;
             default:
                 Settings.setParity(SerialPort.PARITY_ODD);
                 break;
         }
         switch (outputIndexComboBox.getSelectedIndex()) {
             case 0:
                 Settings.setOutputIndex(1);
                 break;
             case 1:
                 Settings.setOutputIndex(2);
                 break;
             case 2:
                 Settings.setOutputIndex(3);
                 break;
             case 3:
                 Settings.setOutputIndex(4);
                 break;
             default:
                 Settings.setOutputIndex(1);
                 break;
         }
         Settings.setHowManyValues(this, (int) howManyValuesSpinner.getValue());
     }
 
     private void openSettings() {
         // set values
         syncSettings();
         settingsDialog.setVisible(true);
         settingsDialog.setLocationRelativeTo(null);
     }
 
     private void syncSettings() {
         refreshDelaySpinner.setValue((Object) Settings.getRefreshDelay());
         switch (Settings.getBaudrate()) {
             case 9600:
                 baudrateComboBox.setSelectedIndex(0);
                 break;
             case 19200:
                 baudrateComboBox.setSelectedIndex(1);
                 break;
             case 38400:
                 baudrateComboBox.setSelectedIndex(2);
                 break;
             case 57600:
                 baudrateComboBox.setSelectedIndex(3);
                 break;
             case 115200:
                 baudrateComboBox.setSelectedIndex(4);
                 break;
             default:
                 baudrateComboBox.setSelectedIndex(0);
                 break;
         }
         switch (Settings.getDataBits()) {
             case 5:
                 break;
             case 6:
                 break;
             case 7:
                 break;
             case 8:
                 break;
             default:
                 break;
         }
         switch (Settings.getStopBits()) {
             case SerialPort.STOPBITS_1:
                 break;
             case SerialPort.STOPBITS_1_5:
                 break;
             case SerialPort.STOPBITS_2:
                 break;
             default:
                 break;
         }
         switch (Settings.getParity()) {
             case SerialPort.PARITY_NONE:
                 break;
             case SerialPort.PARITY_ODD:
                 break;
             case SerialPort.PARITY_EVEN:
                 break;
             case SerialPort.PARITY_MARK:
                 break;
             case SerialPort.PARITY_SPACE:
                 break;
             default:
                 break;
         }
         switch (Settings.getOutputIndex()) {
             case 1:
                 break;
             case 2:
                 break;
             case 3:
                 break;
             case 4:
                 break;
             default:
                 break;
         }
         howManyValuesSpinner.setValue((Object) Settings.getHowManyValues());
     }
 
     private void redirectOutput(boolean redirect) {
         if (redirect) {
             System.out.println("Redirecting Output to Console Window");
             System.setOut(consoleOutput);
             System.out.println("Output redirected to Console Window");
         } else {
             System.out.println("Redirecting Output to System Output");
             System.setOut(systemOutput);
             System.out.println("Output redirected to System Output");
         }
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         comButtonGroup = new javax.swing.ButtonGroup();
         settingsDialog = new javax.swing.JDialog();
         settingsApplyButton = new javax.swing.JButton();
         settingsCancelButton = new javax.swing.JButton();
         settingsOkayButton = new javax.swing.JButton();
         settingsPanel = new javax.swing.JPanel();
         howManyValuesLabel = new javax.swing.JLabel();
         baudrateLabel = new javax.swing.JLabel();
         refreshDelayLabel = new javax.swing.JLabel();
         baudrateComboBox = new javax.swing.JComboBox();
         dataBitsComboBox = new javax.swing.JComboBox();
         parityLabel = new javax.swing.JLabel();
         stopBitsComboBox = new javax.swing.JComboBox();
         outputIndexLabel = new javax.swing.JLabel();
         parityComboBox = new javax.swing.JComboBox();
         dataBitsLabel = new javax.swing.JLabel();
         outputIndexComboBox = new javax.swing.JComboBox();
         stopBitsLabel = new javax.swing.JLabel();
         defaultRefresh = new javax.swing.JLabel();
         defaultBaud = new javax.swing.JLabel();
         defaultDataBits = new javax.swing.JLabel();
         defaultStopBits = new javax.swing.JLabel();
         defaultParity = new javax.swing.JLabel();
         defaultOutputIndex = new javax.swing.JLabel();
         defaultHowManyValues = new javax.swing.JLabel();
         defaultLabel = new javax.swing.JLabel();
         howManyValuesSpinner = new javax.swing.JSpinner();
         refreshDelaySpinner = new javax.swing.JSpinner();
         consoleDialog = new javax.swing.JDialog();
         jScrollPane1 = new javax.swing.JScrollPane();
         consoleArea = new javax.swing.JTextArea();
         voltageTextField = new javax.swing.JTextField();
         currentTextField = new javax.swing.JTextField();
         aLabel = new javax.swing.JLabel();
         voltLabel = new javax.swing.JLabel();
         voltageLabel = new javax.swing.JLabel();
         currentLabel = new javax.swing.JLabel();
         voltageSlider = new javax.swing.JSlider();
         onOffToggleButton = new javax.swing.JToggleButton();
         jTextField1 = new javax.swing.JTextField();
         loadBar = new javax.swing.JProgressBar();
         menuBar = new javax.swing.JMenuBar();
         settingsMenu = new javax.swing.JMenu();
         connectMenuItem = new javax.swing.JMenuItem();
         comSelectMenu = new javax.swing.JMenu();
         refreshCOMPortsMenuItem = new javax.swing.JMenuItem();
         saveCOMPortMenuItem = new javax.swing.JMenuItem();
         comSelectSeparator = new javax.swing.JPopupMenu.Separator();
         jRadioButtonMenuItem4 = new javax.swing.JRadioButtonMenuItem();
         jRadioButtonMenuItem5 = new javax.swing.JRadioButtonMenuItem();
         jRadioButtonMenuItem6 = new javax.swing.JRadioButtonMenuItem();
         comSettingsSeparator = new javax.swing.JPopupMenu.Separator();
         averageCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
         advancedSettingsMenuItem = new javax.swing.JMenuItem();
         aboutMenu = new javax.swing.JMenu();
         infoMenuItem = new javax.swing.JMenuItem();
         jMenuItem1 = new javax.swing.JMenuItem();
 
         settingsDialog.setModal(true);
 
         settingsApplyButton.setText("Apply");
         settingsApplyButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 settingsApplyButtonActionPerformed(evt);
             }
         });
 
         settingsCancelButton.setText("Cancel");
         settingsCancelButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 settingsCancelButtonActionPerformed(evt);
             }
         });
 
         settingsOkayButton.setText("OK");
         settingsOkayButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 settingsOkayButtonActionPerformed(evt);
             }
         });
 
         settingsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
 
         howManyValuesLabel.setLabelFor(howManyValuesSpinner);
         howManyValuesLabel.setText("howManyValues");
 
         baudrateLabel.setLabelFor(baudrateComboBox);
         baudrateLabel.setText("baudrate");
 
         refreshDelayLabel.setLabelFor(refreshDelaySpinner);
         refreshDelayLabel.setText("refreshDelay");
 
         baudrateComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "9600", "19200", "38400", "57600", "115200" }));
         baudrateComboBox.setToolTipText("");
 
         dataBitsComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "5", "6", "7", "8" }));
         dataBitsComboBox.setSelectedIndex(3);
         dataBitsComboBox.setToolTipText("");
 
         parityLabel.setLabelFor(parityComboBox);
         parityLabel.setText("parity");
 
         stopBitsComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "1.5", "2" }));
         stopBitsComboBox.setSelectedIndex(1);
         stopBitsComboBox.setToolTipText("");
 
         outputIndexLabel.setLabelFor(outputIndexComboBox);
         outputIndexLabel.setText("outputIndex");
 
         parityComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "none", "odd", "even", "mark", "space" }));
         parityComboBox.setSelectedIndex(1);
         parityComboBox.setToolTipText("");
 
         dataBitsLabel.setLabelFor(dataBitsComboBox);
         dataBitsLabel.setText("dataBits");
 
         outputIndexComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "2", "3", "4" }));
 
         stopBitsLabel.setLabelFor(stopBitsComboBox);
         stopBitsLabel.setText("stopBits");
 
         defaultRefresh.setText("100ms");
 
         defaultBaud.setText("9600");
 
         defaultDataBits.setText("8");
 
         defaultStopBits.setText("1");
 
         defaultParity.setText("odd");
 
         defaultOutputIndex.setText("1");
 
         defaultHowManyValues.setText("10");
 
         defaultLabel.setText("Default");
 
         refreshDelaySpinner.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 refreshDelaySpinnerStateChanged(evt);
             }
         });
 
         javax.swing.GroupLayout settingsPanelLayout = new javax.swing.GroupLayout(settingsPanel);
         settingsPanel.setLayout(settingsPanelLayout);
         settingsPanelLayout.setHorizontalGroup(
             settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(settingsPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(settingsPanelLayout.createSequentialGroup()
                         .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(howManyValuesLabel)
                             .addComponent(outputIndexLabel))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                             .addComponent(outputIndexComboBox, 0, 61, Short.MAX_VALUE)
                             .addComponent(howManyValuesSpinner))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(defaultOutputIndex)
                             .addComponent(defaultHowManyValues)))
                     .addGroup(settingsPanelLayout.createSequentialGroup()
                         .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(refreshDelayLabel)
                             .addComponent(baudrateLabel)
                             .addComponent(dataBitsLabel)
                             .addComponent(stopBitsLabel)
                             .addComponent(parityLabel))
                         .addGap(25, 25, 25)
                         .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                             .addComponent(refreshDelaySpinner)
                             .addComponent(parityComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(stopBitsComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(baudrateComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(dataBitsComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                         .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(settingsPanelLayout.createSequentialGroup()
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(defaultBaud)
                                     .addComponent(defaultDataBits)
                                     .addComponent(defaultStopBits)
                                     .addComponent(defaultParity)))
                             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, settingsPanelLayout.createSequentialGroup()
                                 .addGap(11, 11, 11)
                                 .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(defaultLabel)
                                     .addComponent(defaultRefresh))))))
                 .addContainerGap(29, Short.MAX_VALUE))
         );
         settingsPanelLayout.setVerticalGroup(
             settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(settingsPanelLayout.createSequentialGroup()
                 .addGap(6, 6, 6)
                 .addComponent(defaultLabel)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(refreshDelayLabel)
                     .addComponent(defaultRefresh)
                     .addComponent(refreshDelaySpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(baudrateLabel)
                     .addComponent(baudrateComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(defaultBaud))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(dataBitsLabel)
                     .addComponent(dataBitsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(defaultDataBits))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(stopBitsLabel)
                     .addComponent(stopBitsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(defaultStopBits))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(parityLabel)
                     .addComponent(parityComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(defaultParity))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(outputIndexLabel)
                     .addComponent(outputIndexComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(defaultOutputIndex))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(howManyValuesLabel)
                     .addComponent(defaultHowManyValues)
                     .addComponent(howManyValuesSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(14, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout settingsDialogLayout = new javax.swing.GroupLayout(settingsDialog.getContentPane());
         settingsDialog.getContentPane().setLayout(settingsDialogLayout);
         settingsDialogLayout.setHorizontalGroup(
             settingsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(settingsDialogLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(settingsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(settingsDialogLayout.createSequentialGroup()
                         .addGap(0, 54, Short.MAX_VALUE)
                         .addComponent(settingsOkayButton)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(settingsCancelButton)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(settingsApplyButton))
                     .addComponent(settingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         settingsDialogLayout.setVerticalGroup(
             settingsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, settingsDialogLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(settingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addGroup(settingsDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(settingsApplyButton)
                     .addComponent(settingsCancelButton)
                     .addComponent(settingsOkayButton))
                 .addContainerGap())
         );
 
         settingsDialog.pack();
 
         consoleDialog.setTitle("Console");
         consoleDialog.setAlwaysOnTop(true);
         consoleDialog.setBackground(new java.awt.Color(0, 0, 0));
         consoleDialog.setType(java.awt.Window.Type.UTILITY);
         consoleDialog.addWindowListener(new java.awt.event.WindowAdapter() {
             public void windowOpened(java.awt.event.WindowEvent evt) {
                 consoleDialogWindowOpened(evt);
             }
             public void windowClosing(java.awt.event.WindowEvent evt) {
                 consoleDialogWindowClosing(evt);
             }
         });
 
         consoleArea.setEditable(false);
         consoleArea.setBackground(new java.awt.Color(0, 0, 0));
         consoleArea.setColumns(20);
         consoleArea.setForeground(new java.awt.Color(255, 255, 255));
         consoleArea.setRows(10);
         consoleArea.setDragEnabled(true);
         jScrollPane1.setViewportView(consoleArea);
 
         javax.swing.GroupLayout consoleDialogLayout = new javax.swing.GroupLayout(consoleDialog.getContentPane());
         consoleDialog.getContentPane().setLayout(consoleDialogLayout);
         consoleDialogLayout.setHorizontalGroup(
             consoleDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
         );
         consoleDialogLayout.setVerticalGroup(
             consoleDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE)
         );
 
         consoleDialog.pack();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle("PICSU GUI");
         setMaximizedBounds(new java.awt.Rectangle(0, 0, 0, 150));
         setMinimumSize(new java.awt.Dimension(326, 150));
 
         voltageTextField.setEditable(false);
         voltageTextField.setBackground(new java.awt.Color(255, 255, 153));
         voltageTextField.setColumns(4);
         voltageTextField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
         voltageTextField.setText("6.00");
         voltageTextField.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 voltageTextFieldActionPerformed(evt);
             }
         });
 
         currentTextField.setEditable(false);
         currentTextField.setBackground(new java.awt.Color(255, 255, 153));
         currentTextField.setColumns(4);
         currentTextField.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
         currentTextField.setText("2.00");
 
         aLabel.setLabelFor(currentTextField);
         aLabel.setText("A");
 
         voltLabel.setLabelFor(voltageTextField);
         voltLabel.setText("V");
 
         voltageLabel.setLabelFor(voltageTextField);
         voltageLabel.setText("Voltage");
 
         currentLabel.setLabelFor(currentTextField);
         currentLabel.setText("Current");
 
         voltageSlider.setMajorTickSpacing(20);
         voltageSlider.setMaximum(240);
         voltageSlider.setMinimum(20);
         voltageSlider.setMinorTickSpacing(5);
         voltageSlider.setPaintTicks(true);
         voltageSlider.setToolTipText("");
         voltageSlider.setValue(120);
         voltageSlider.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 voltageSliderStateChanged(evt);
             }
         });
 
         onOffToggleButton.setBackground(new java.awt.Color(255, 255, 255));
         onOffToggleButton.setText("Off");
         onOffToggleButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 onOffToggleButtonActionPerformed(evt);
             }
         });
 
         jTextField1.setEditable(false);
         jTextField1.setBackground(new java.awt.Color(255, 0, 0));
         jTextField1.setColumns(2);
         jTextField1.setToolTipText("");
 
         loadBar.setMaximum(200);
         loadBar.setOrientation(1);
         loadBar.setToolTipText("");
         loadBar.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
 
         settingsMenu.setText("Settings");
         settingsMenu.setName(""); // NOI18N
 
         connectMenuItem.setText("Connect");
         connectMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 connectMenuItemActionPerformed(evt);
             }
         });
         settingsMenu.add(connectMenuItem);
 
         comSelectMenu.setText("Select COM");
 
         refreshCOMPortsMenuItem.setText("Refresh Ports");
         refreshCOMPortsMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 refreshCOMPortsMenuItemActionPerformed(evt);
             }
         });
         comSelectMenu.add(refreshCOMPortsMenuItem);
 
         saveCOMPortMenuItem.setText("Save Default Port");
         saveCOMPortMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 saveCOMPortMenuItemActionPerformed(evt);
             }
         });
         comSelectMenu.add(saveCOMPortMenuItem);
         comSelectMenu.add(comSelectSeparator);
 
         comButtonGroup.add(jRadioButtonMenuItem4);
         jRadioButtonMenuItem4.setSelected(true);
         jRadioButtonMenuItem4.setText("COM2808");
         jRadioButtonMenuItem4.setToolTipText("");
         jRadioButtonMenuItem4.setName(""); // NOI18N
         comSelectMenu.add(jRadioButtonMenuItem4);
 
         comButtonGroup.add(jRadioButtonMenuItem5);
         jRadioButtonMenuItem5.setText("COM0893");
         comSelectMenu.add(jRadioButtonMenuItem5);
 
         comButtonGroup.add(jRadioButtonMenuItem6);
         jRadioButtonMenuItem6.setText("COM1993");
         comSelectMenu.add(jRadioButtonMenuItem6);
 
         settingsMenu.add(comSelectMenu);
         settingsMenu.add(comSettingsSeparator);
 
         averageCheckBoxMenuItem.setText("Display Average");
         averageCheckBoxMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 averageCheckBoxMenuItemActionPerformed(evt);
             }
         });
         settingsMenu.add(averageCheckBoxMenuItem);
 
         advancedSettingsMenuItem.setText("Advanced Settings");
         advancedSettingsMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 advancedSettingsMenuItemActionPerformed(evt);
             }
         });
         settingsMenu.add(advancedSettingsMenuItem);
 
         menuBar.add(settingsMenu);
 
         aboutMenu.setText("About");
 
         infoMenuItem.setText("Info");
         infoMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 infoMenuItemActionPerformed(evt);
             }
         });
         aboutMenu.add(infoMenuItem);
 
         jMenuItem1.setText("Console");
         jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItem1ActionPerformed(evt);
             }
         });
         aboutMenu.add(jMenuItem1);
 
         menuBar.add(aboutMenu);
 
         setJMenuBar(menuBar);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(voltageSlider, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 276, Short.MAX_VALUE)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(onOffToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 65, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addGroup(layout.createSequentialGroup()
                                 .addComponent(currentLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 43, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(currentTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addGroup(layout.createSequentialGroup()
                                 .addComponent(voltageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(voltageTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(aLabel)
                             .addComponent(voltLabel))))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(loadBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(7, 7, 7))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                     .addComponent(loadBar, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(layout.createSequentialGroup()
                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                     .addComponent(voltageTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addComponent(voltLabel)
                                     .addComponent(voltageLabel))
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                     .addComponent(currentTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addComponent(aLabel)
                                     .addComponent(currentLabel)))
                             .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                 .addComponent(onOffToggleButton, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                         .addGap(18, 18, 18)
                         .addComponent(voltageSlider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void voltageSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_voltageSliderStateChanged
         double value = (voltageSlider.getValue() * 5) / 100.;
         voltageTextField.setText(f.format(value));
         voltageChanged = true;
         if (!voltageSlider.getValueIsAdjusting()) {
             serial.send("[v:" + Settings.getOutputIndex() + ":" + s.format(value) + "]\r");
         }
     }//GEN-LAST:event_voltageSliderStateChanged
 
     private void onOffToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_onOffToggleButtonActionPerformed
         if (onOffToggleButton.isSelected()) {
             onOffToggleButton.setText("On ");
             serial.send("[r:" + Settings.getOutputIndex() + "]\r");
             serial.send("[v:" + Settings.getOutputIndex() + ":" + s.format((voltageSlider.getValue() * 5) / 100.) + "]\r");
         } else {
             onOffToggleButton.setText("Off");
             serial.send("[s:" + Settings.getOutputIndex() + "]\r");
         }
     }//GEN-LAST:event_onOffToggleButtonActionPerformed
 
     private void voltageTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_voltageTextFieldActionPerformed
         String text = voltageTextField.getText().trim().replace(",", "."); // Replace , with . to fix compatibility with EU Comma
         if (text.contains(".")) {
             if (text.indexOf(".") + 1 <= text.length() - 3) {
                 text = text.substring(0, text.length() - 1); //Trim Text if it's too long (eg. 6.255)
             }
         }
         voltageTextField.setText(f.format(setVoltageSlider(Float.parseFloat(text)) * 0.05));
         voltageChanged = true;
     }//GEN-LAST:event_voltageTextFieldActionPerformed
 
     private void refreshCOMPortsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshCOMPortsMenuItemActionPerformed
         refreshCOMPorts();
     }//GEN-LAST:event_refreshCOMPortsMenuItemActionPerformed
 
     private void infoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_infoMenuItemActionPerformed
         JOptionPane.showMessageDialog(this, Settings.aboutMessage, "About", JOptionPane.INFORMATION_MESSAGE);
     }//GEN-LAST:event_infoMenuItemActionPerformed
 
     private void connectMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connectMenuItemActionPerformed
         if (!serial.isConnected()) {
             if (connect()) {
                 connectMenuItem.setText("Disconnect");
                 enableComponents(true);
                 serial.send("*\r");
             }
         } else {
             serial.send("*\r");
             serial.closeSerialPort();
             connectMenuItem.setText("Connect");
             enableComponents(false);
         }
     }//GEN-LAST:event_connectMenuItemActionPerformed
 
     private void averageCheckBoxMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_averageCheckBoxMenuItemActionPerformed
         Settings.displayAverage(averageCheckBoxMenuItem.getState());
     }//GEN-LAST:event_averageCheckBoxMenuItemActionPerformed
 
     private void advancedSettingsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_advancedSettingsMenuItemActionPerformed
         openSettings();
     }//GEN-LAST:event_advancedSettingsMenuItemActionPerformed
 
     private void settingsOkayButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsOkayButtonActionPerformed
         settingsDialog.setVisible(false);
         setSettings();
     }//GEN-LAST:event_settingsOkayButtonActionPerformed
 
     private void settingsCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsCancelButtonActionPerformed
         settingsDialog.setVisible(false);
     }//GEN-LAST:event_settingsCancelButtonActionPerformed
 
     private void settingsApplyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_settingsApplyButtonActionPerformed
         setSettings();
     }//GEN-LAST:event_settingsApplyButtonActionPerformed
 
     private void consoleDialogWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_consoleDialogWindowClosing
         redirectOutput(false);
     }//GEN-LAST:event_consoleDialogWindowClosing
 
     private void consoleDialogWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_consoleDialogWindowOpened
         redirectOutput(true);
     }//GEN-LAST:event_consoleDialogWindowOpened
 
     private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
         consoleDialog.setLocationRelativeTo(null);
         consoleDialog.setVisible(true);
     }//GEN-LAST:event_jMenuItem1ActionPerformed
 
     private void refreshDelaySpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_refreshDelaySpinnerStateChanged
         System.out.println((int) refreshDelaySpinner.getValue());
     }//GEN-LAST:event_refreshDelaySpinnerStateChanged
 
     private void saveCOMPortMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveCOMPortMenuItemActionPerformed
         JOptionPane.showMessageDialog(this, "Nothing to see here yet\nFunction not implemented yet!", "Info", JOptionPane.INFORMATION_MESSAGE);
     }//GEN-LAST:event_saveCOMPortMenuItemActionPerformed
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JLabel aLabel;
     private javax.swing.JMenu aboutMenu;
     private javax.swing.JMenuItem advancedSettingsMenuItem;
     private javax.swing.JCheckBoxMenuItem averageCheckBoxMenuItem;
     private javax.swing.JComboBox baudrateComboBox;
     private javax.swing.JLabel baudrateLabel;
     private javax.swing.ButtonGroup comButtonGroup;
     private javax.swing.JMenu comSelectMenu;
     private javax.swing.JPopupMenu.Separator comSelectSeparator;
     private javax.swing.JPopupMenu.Separator comSettingsSeparator;
     private javax.swing.JMenuItem connectMenuItem;
     private javax.swing.JTextArea consoleArea;
     private javax.swing.JDialog consoleDialog;
     private javax.swing.JLabel currentLabel;
     private javax.swing.JTextField currentTextField;
     private javax.swing.JComboBox dataBitsComboBox;
     private javax.swing.JLabel dataBitsLabel;
     private javax.swing.JLabel defaultBaud;
     private javax.swing.JLabel defaultDataBits;
     private javax.swing.JLabel defaultHowManyValues;
     private javax.swing.JLabel defaultLabel;
     private javax.swing.JLabel defaultOutputIndex;
     private javax.swing.JLabel defaultParity;
     private javax.swing.JLabel defaultRefresh;
     private javax.swing.JLabel defaultStopBits;
     private javax.swing.JLabel howManyValuesLabel;
     private javax.swing.JSpinner howManyValuesSpinner;
     private javax.swing.JMenuItem infoMenuItem;
     private javax.swing.JMenuItem jMenuItem1;
     private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem4;
     private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem5;
     private javax.swing.JRadioButtonMenuItem jRadioButtonMenuItem6;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JTextField jTextField1;
     private javax.swing.JProgressBar loadBar;
     private javax.swing.JMenuBar menuBar;
     private javax.swing.JToggleButton onOffToggleButton;
     private javax.swing.JComboBox outputIndexComboBox;
     private javax.swing.JLabel outputIndexLabel;
     private javax.swing.JComboBox parityComboBox;
     private javax.swing.JLabel parityLabel;
     private javax.swing.JMenuItem refreshCOMPortsMenuItem;
     private javax.swing.JLabel refreshDelayLabel;
     private javax.swing.JSpinner refreshDelaySpinner;
     private javax.swing.JMenuItem saveCOMPortMenuItem;
     private javax.swing.JButton settingsApplyButton;
     private javax.swing.JButton settingsCancelButton;
     private javax.swing.JDialog settingsDialog;
     private javax.swing.JMenu settingsMenu;
     private javax.swing.JButton settingsOkayButton;
     private javax.swing.JPanel settingsPanel;
     private javax.swing.JComboBox stopBitsComboBox;
     private javax.swing.JLabel stopBitsLabel;
     private javax.swing.JLabel voltLabel;
     private javax.swing.JLabel voltageLabel;
     private javax.swing.JSlider voltageSlider;
     private javax.swing.JTextField voltageTextField;
     // End of variables declaration//GEN-END:variables
 }
