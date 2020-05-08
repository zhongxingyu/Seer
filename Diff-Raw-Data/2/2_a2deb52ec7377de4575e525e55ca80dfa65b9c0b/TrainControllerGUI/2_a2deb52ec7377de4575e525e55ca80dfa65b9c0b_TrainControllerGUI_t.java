 package TLTTC;
 
 import javax.swing.*;
 import javax.swing.table.*;
 import java.util.*;
 import java.awt.*;
 
 public class TrainControllerGUI extends JFrame {
     TrainModel tm;
     TrainController tc;
     TrainControllerModule mod;
     Integer[] trainIDs;
     boolean noTrains = true;
     javax.swing.table.DefaultTableModel model;
     
     public TrainControllerGUI(TrainControllerModule m) {
         mod = m;
         while (noTrains){ // Don't open GUI until a train is created
             createDropdownModel(); // Creates dropdown menu
         }
         trainContDropdown.setSelectedItem(trainIDs[0]); // Set dropdown to first train in list
         tc = mod.getTrainController(trainIDs[0]); // Sets first displayed data to first train in list
         tm = tc.getTrain();
         initComponents();
         open();
     }
 
 
         public void open(){
         try {
             for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(TrainControllerGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(TrainControllerGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(TrainControllerGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(TrainControllerGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
 
         this.setVisible(true);
         /*final TrainControllerGUI t = this;
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 t.setVisible(true);
             }
         });*/
         
         (new Thread(new UpdateGUI())).start();
     }
     
     
     private void refreshUI(){
         createDropdownModel();
         if (!noTrains){
             doorControlButton.setText(tm.getDoors() == true ? "Close" : "Open");
             lightControlButton.setText(tm.getLights() == true ? "Turn Off" : "Turn On");
 
             currentTempText.setText(Double.toString(tm.getTemperature()));
             nextStationText.setText(tc.getNextStation());
             velocityText.setText(Double.toString(tm.getVelocity()));
             authorityText.setText(Double.toString(tc.getAuthority()));
 
             engineFailureText.setBackground(tc.getEngineFail() == true ? Color.RED : Color.GRAY);
             pickupFailureText.setBackground(tc.getSignalPickupFail() == true ? Color.RED : Color.GRAY);
             brakeFailureText.setBackground(tc.getBrakeFail() == true ? Color.RED : Color.GRAY);
             model = (javax.swing.table.DefaultTableModel) powerTable.getModel();
             model.addRow(new Object[]{"time", tc.getVelocity(), tc.getVelocitySetpoint(), tc.getPower()});
         }
     }
     
     private void createDropdownModel(){ // Creates Integer array of train IDs
         trainIDs = new Integer[60];
         Enumeration<Integer> list = mod.getTrainList(); // List of train IDs
         DefaultComboBoxModel<Integer> model = new DefaultComboBoxModel<Integer>(); // Combo Box Model
         int i = 0;
         for (i = 0; list.hasMoreElements(); i++){
             trainIDs[i] = list.nextElement();
             model.addElement(list.nextElement()); // Adds each ID to the model
         }
         if (i == 0){
            // trainContDropdown.setModel(model); // Sets new model (null model in this case)
             noTrains = true;
         }
         else{
             trainContDropdown.setModel(model); // Sets new model
             noTrains = false;
         }
     }
     
     
     @SuppressWarnings("serial")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">
     private void initComponents() {
 
         trainContDropdown = new javax.swing.JComboBox<Integer>();
         doorControlPanel = new javax.swing.JPanel();
         doorControlButton = new javax.swing.JButton();
         lightControlPanel = new javax.swing.JPanel();
         lightControlButton = new javax.swing.JButton();
         temperatureControlPanel = new javax.swing.JPanel();
         tempSetpointSetButton = new javax.swing.JButton();
         currentTempText = new javax.swing.JTextField();
         currentTempLabel = new javax.swing.JLabel();
         degreesSymbolText1 = new javax.swing.JLabel();
         tempSetpointText = new javax.swing.JTextField();
         tempSetpointLabel = new javax.swing.JLabel();
         degreesSymbolText2 = new javax.swing.JLabel();
         powerControlPanel = new javax.swing.JPanel();
         authorityLabel = new javax.swing.JLabel();
         authorityText = new javax.swing.JTextField();
         velocitySetter = new javax.swing.JSpinner();
         accelerateButton = new javax.swing.JButton();
         brakeButton = new javax.swing.JButton();
         metersText = new javax.swing.JLabel();
         mphText2 = new javax.swing.JLabel();
         emergencyBrakeButton = new javax.swing.JButton();
         velocityLabel = new javax.swing.JLabel();
         velocityText = new javax.swing.JTextField();
         mphText = new javax.swing.JLabel();
         gpsPanel = new javax.swing.JPanel();
         gpsConnectButton = new javax.swing.JButton();
         nextStationPanel = new javax.swing.JPanel();
         nextStationAnnounceButton2 = new javax.swing.JButton();
         nextStationText = new javax.swing.JTextField();
         faultsPanel = new javax.swing.JPanel();
         engineFailureText = new javax.swing.JTextField();
         pickupFailureText = new javax.swing.JTextField();
         brakeFailureText = new javax.swing.JTextField();
         jScrollPane1 = new javax.swing.JScrollPane();
         powerTable = new javax.swing.JTable();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
 
         trainContDropdown.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 trainContDropdownActionPerformed(evt);
             }
         });
 
         doorControlPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Door Control"));
 
         doorControlButton.setText("No Train!");
         doorControlButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 doorControlButtonActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout doorControlPanelLayout = new javax.swing.GroupLayout(doorControlPanel);
         doorControlPanel.setLayout(doorControlPanelLayout);
         doorControlPanelLayout.setHorizontalGroup(
             doorControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(doorControlPanelLayout.createSequentialGroup()
                 .addGap(23, 23, 23)
                 .addComponent(doorControlButton)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         doorControlPanelLayout.setVerticalGroup(
             doorControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(doorControlPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(doorControlButton)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         lightControlPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Light Control"));
 
         lightControlButton.setText("No Train!");
         lightControlButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 lightControlButtonActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout lightControlPanelLayout = new javax.swing.GroupLayout(lightControlPanel);
         lightControlPanel.setLayout(lightControlPanelLayout);
         lightControlPanelLayout.setHorizontalGroup(
             lightControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(lightControlPanelLayout.createSequentialGroup()
                 .addGap(23, 23, 23)
                 .addComponent(lightControlButton)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         lightControlPanelLayout.setVerticalGroup(
             lightControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(lightControlPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(lightControlButton)
                 .addContainerGap(22, Short.MAX_VALUE))
         );
 
         temperatureControlPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Temperature Control"));
 
         tempSetpointSetButton.setText("Set");
         tempSetpointSetButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 tempSetpointSetButtonActionPerformed(evt);
             }
         });
 
         currentTempText.setEditable(false);
         currentTempText.setText("-");
         currentTempText.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 currentTempTextActionPerformed(evt);
             }
         });
 
         currentTempLabel.setText("Temperature");
 
         degreesSymbolText1.setText("°F");
 
         tempSetpointText.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 tempSetpointTextActionPerformed(evt);
             }
         });
 
         tempSetpointLabel.setText("Setpoint");
 
         degreesSymbolText2.setText("°F");
 
         javax.swing.GroupLayout temperatureControlPanelLayout = new javax.swing.GroupLayout(temperatureControlPanel);
         temperatureControlPanel.setLayout(temperatureControlPanelLayout);
         temperatureControlPanelLayout.setHorizontalGroup(
             temperatureControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(temperatureControlPanelLayout.createSequentialGroup()
                 .addGroup(temperatureControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(currentTempLabel)
                     .addGroup(temperatureControlPanelLayout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(currentTempText, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(degreesSymbolText2)))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(temperatureControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(temperatureControlPanelLayout.createSequentialGroup()
                         .addGroup(temperatureControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addGroup(temperatureControlPanelLayout.createSequentialGroup()
                                 .addGap(0, 6, Short.MAX_VALUE)
                                 .addComponent(tempSetpointLabel))
                             .addComponent(tempSetpointText))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(degreesSymbolText1)
                         .addGap(8, 8, 8))
                     .addGroup(temperatureControlPanelLayout.createSequentialGroup()
                         .addComponent(tempSetpointSetButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addContainerGap())))
         );
         temperatureControlPanelLayout.setVerticalGroup(
             temperatureControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(temperatureControlPanelLayout.createSequentialGroup()
                 .addGroup(temperatureControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(currentTempLabel)
                     .addComponent(tempSetpointLabel))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(temperatureControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(currentTempText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(tempSetpointText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(degreesSymbolText1)
                     .addComponent(degreesSymbolText2))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(tempSetpointSetButton))
         );
 
         powerControlPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Power Control"));
 
         authorityLabel.setText("Authority");
 
         authorityText.setEditable(false);
         authorityText.setText("-");
         authorityText.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 authorityTextActionPerformed(evt);
             }
         });
 
         velocitySetter.setModel(new javax.swing.SpinnerNumberModel(0.0d, 0.0d, 43.0d, 1.0d));
 
         accelerateButton.setText("Accelerate");
         accelerateButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 accelerateButtonActionPerformed(evt);
             }
         });
 
         brakeButton.setText("Brake");
         brakeButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 brakeButtonActionPerformed(evt);
             }
         });
 
         metersText.setText("m");
 
         mphText2.setText("mph");
 
         emergencyBrakeButton.setText("E-Brake");
         emergencyBrakeButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 emergencyBrakeButtonActionPerformed(evt);
             }
         });
 
         velocityLabel.setText("Velocity");
 
         velocityText.setEditable(false);
         velocityText.setText("-");
         velocityText.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 velocityTextActionPerformed(evt);
             }
         });
 
         mphText.setText("mph");
 
         javax.swing.GroupLayout powerControlPanelLayout = new javax.swing.GroupLayout(powerControlPanel);
         powerControlPanel.setLayout(powerControlPanelLayout);
         powerControlPanelLayout.setHorizontalGroup(
             powerControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, powerControlPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(velocityText, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(powerControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(powerControlPanelLayout.createSequentialGroup()
                         .addGroup(powerControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(velocityLabel)
                             .addComponent(mphText))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(velocitySetter, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(mphText2))
                     .addGroup(powerControlPanelLayout.createSequentialGroup()
                         .addGroup(powerControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(powerControlPanelLayout.createSequentialGroup()
                                 .addComponent(authorityText, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(metersText))
                             .addComponent(authorityLabel))
                         .addGap(0, 0, Short.MAX_VALUE)))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(powerControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(accelerateButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(brakeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(emergencyBrakeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(13, 13, 13))
         );
         powerControlPanelLayout.setVerticalGroup(
             powerControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(powerControlPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(powerControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(powerControlPanelLayout.createSequentialGroup()
                         .addComponent(accelerateButton)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(brakeButton)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(emergencyBrakeButton))
                     .addGroup(powerControlPanelLayout.createSequentialGroup()
                         .addComponent(velocityLabel)
                         .addGap(3, 3, 3)
                         .addGroup(powerControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(velocityText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(mphText)
                             .addComponent(velocitySetter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(mphText2))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(authorityLabel)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(powerControlPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(authorityText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(metersText))))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         gpsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("GPS"));
 
         gpsConnectButton.setText("Connect");
         gpsConnectButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 gpsConnectButtonActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout gpsPanelLayout = new javax.swing.GroupLayout(gpsPanel);
         gpsPanel.setLayout(gpsPanelLayout);
         gpsPanelLayout.setHorizontalGroup(
             gpsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(gpsPanelLayout.createSequentialGroup()
                 .addGap(38, 38, 38)
                 .addComponent(gpsConnectButton)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         gpsPanelLayout.setVerticalGroup(
             gpsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(gpsPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(gpsConnectButton)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         nextStationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Next Station"));
 
         nextStationAnnounceButton2.setText("Announce");
         nextStationAnnounceButton2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 nextStationAnnounceButton2ActionPerformed(evt);
             }
         });
 
         nextStationText.setEditable(false);
         nextStationText.setText("-");
 
         javax.swing.GroupLayout nextStationPanelLayout = new javax.swing.GroupLayout(nextStationPanel);
         nextStationPanel.setLayout(nextStationPanelLayout);
         nextStationPanelLayout.setHorizontalGroup(
             nextStationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, nextStationPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(nextStationText)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(nextStationAnnounceButton2)
                 .addContainerGap())
         );
         nextStationPanelLayout.setVerticalGroup(
             nextStationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, nextStationPanelLayout.createSequentialGroup()
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addGroup(nextStationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(nextStationAnnounceButton2)
                     .addComponent(nextStationText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap())
         );
 
         faultsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Faults"));
 
         engineFailureText.setEditable(false);
         engineFailureText.setHorizontalAlignment(javax.swing.JTextField.CENTER);
         engineFailureText.setText("Train Engine Failure");
         engineFailureText.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 engineFailureTextActionPerformed(evt);
             }
         });
 
         pickupFailureText.setEditable(false);
         pickupFailureText.setHorizontalAlignment(javax.swing.JTextField.CENTER);
         pickupFailureText.setText("Signal Pickup Failure");
 
         brakeFailureText.setEditable(false);
         brakeFailureText.setHorizontalAlignment(javax.swing.JTextField.CENTER);
         brakeFailureText.setText("Brake Failiure");
 
         javax.swing.GroupLayout faultsPanelLayout = new javax.swing.GroupLayout(faultsPanel);
         faultsPanel.setLayout(faultsPanelLayout);
         faultsPanelLayout.setHorizontalGroup(
             faultsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, faultsPanelLayout.createSequentialGroup()
                 .addContainerGap(20, Short.MAX_VALUE)
                 .addGroup(faultsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(pickupFailureText)
                     .addComponent(brakeFailureText)
                     .addComponent(engineFailureText, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18))
         );
         faultsPanelLayout.setVerticalGroup(
             faultsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(faultsPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(engineFailureText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(pickupFailureText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(brakeFailureText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         powerTable.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Time", "Velocity (mph)", "Setpoint (mph)", "Power (W)"
             }
         ) {
             Class[] types = new Class [] {
                 java.lang.Object.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class
             };
             boolean[] canEdit = new boolean [] {
                 false, false, false, false
             };
 
             public Class getColumnClass(int columnIndex) {
                 return types [columnIndex];
             }
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         powerTable.setName("");
         jScrollPane1.setViewportView(powerTable);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                     .addComponent(doorControlPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(trainContDropdown, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(temperatureControlPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(lightControlPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addGap(18, 18, 18)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addGroup(layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                             .addComponent(nextStationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(powerControlPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                         .addGap(18, 18, 18)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                             .addComponent(faultsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(gpsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                     .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 460, Short.MAX_VALUE))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addGap(7, 7, 7)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(trainContDropdown, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(doorControlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(gpsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                     .addComponent(nextStationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                             .addComponent(powerControlPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(faultsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                         .addGap(22, 22, 22)
                         .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 205, Short.MAX_VALUE))
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(lightControlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(temperatureControlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(0, 0, Short.MAX_VALUE)))
                 .addContainerGap())
         );
 
         pack();
     }// </editor-fold>
     
     private void trainContDropdownActionPerformed(java.awt.event.ActionEvent evt) {                                                  
         if (!noTrains){
             tc = mod.getTrainController((Integer)(trainContDropdown.getSelectedItem()));
             tm = tc.getTrain();
             refreshUI();
         }
     }                                                 
 
     
     private void currentTempTextActionPerformed(java.awt.event.ActionEvent evt) {                                                
         // TODO add your handling code here:
     }                                               
 
     private void tempSetpointSetButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                      
         if (!noTrains && Integer.parseInt(tempSetpointText.getText()) >= 55 && Integer.parseInt(tempSetpointText.getText()) <= 80){ // If temperature is safe, set it
             tm.setTemperature(Double.parseDouble(tempSetpointText.getText()));
         }
     }                                                     
 
     private void lightControlButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                   
         if (!noTrains){
             tc.setLights();
         }
     }                                                  
 
     private void authorityTextActionPerformed(java.awt.event.ActionEvent evt) {                                              
         // TODO add your handling code here:
     }                                             
 
     private void doorControlButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                  
         if (!noTrains){
            tc.setDoors();
         }
     }                                                 
 
     private void tempSetpointTextActionPerformed(java.awt.event.ActionEvent evt) {                                                 
         // TODO add your handling code here:
     }                                                
 
     private void velocityTextActionPerformed(java.awt.event.ActionEvent evt) {                                             
         // TODO add your handling code here:
     }                                            
 
     private void accelerateButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                 
         if (!noTrains){
             double v = new Double(velocitySetter.getValue().toString());
             tc.setTrainOperatorVelocity(v*0.44704);
         }
     }                                                
 
     private void brakeButtonActionPerformed(java.awt.event.ActionEvent evt) {                                            
         if (!noTrains){
             tm.setPower(0);
         }
     }                                           
 
     private void emergencyBrakeButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                     
         if (!noTrains){
             tm.setEmergencyBrake(true);
         }
     }                                                    
 
     private void nextStationAnnounceButton2ActionPerformed(java.awt.event.ActionEvent evt) {                                                           
         if (!noTrains){
             tc.announceStation(false);
         }
     }                                                          
 
     private void engineFailureTextActionPerformed(java.awt.event.ActionEvent evt) {                                                  
         // TODO add your handling code here:
     }                                                 
 
     private void gpsConnectButtonActionPerformed(java.awt.event.ActionEvent evt) {                                                 
         if (!noTrains){
             if (!tc.getGpsConnected()){
                 // connect to GPS
             }
         }
     }                                                
 
     // Variables declaration - do not modify
     private javax.swing.JButton accelerateButton;
     private javax.swing.JLabel authorityLabel;
     private javax.swing.JTextField authorityText;
     private javax.swing.JButton brakeButton;
     private javax.swing.JTextField brakeFailureText;
     private javax.swing.JLabel currentTempLabel;
     private javax.swing.JTextField currentTempText;
     private javax.swing.JLabel degreesSymbolText1;
     private javax.swing.JLabel degreesSymbolText2;
     private javax.swing.JButton doorControlButton;
     private javax.swing.JPanel doorControlPanel;
     private javax.swing.JButton emergencyBrakeButton;
     private javax.swing.JTextField engineFailureText;
     private javax.swing.JPanel faultsPanel;
     private javax.swing.JButton gpsConnectButton;
     private javax.swing.JPanel gpsPanel;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JButton lightControlButton;
     private javax.swing.JPanel lightControlPanel;
     private javax.swing.JLabel metersText;
     private javax.swing.JLabel mphText;
     private javax.swing.JLabel mphText2;
     private javax.swing.JButton nextStationAnnounceButton2;
     private javax.swing.JPanel nextStationPanel;
     private javax.swing.JTextField nextStationText;
     private javax.swing.JTextField pickupFailureText;
     private javax.swing.JPanel powerControlPanel;
     private javax.swing.JTable powerTable;
     private javax.swing.JLabel tempSetpointLabel;
     private javax.swing.JButton tempSetpointSetButton;
     private javax.swing.JTextField tempSetpointText;
     private javax.swing.JPanel temperatureControlPanel;
     private javax.swing.JComboBox<Integer> trainContDropdown;
     private javax.swing.JLabel velocityLabel;
     private javax.swing.JSpinner velocitySetter;
     private javax.swing.JTextField velocityText;
     // End of variables declaration
 
 private class UpdateGUI implements Runnable {
     public void run() {
         while (true){ // Refreshes UI
             if (System.currentTimeMillis() % 500 == 0){
                 refreshUI(); // Refresh UI every half second
             }
         }
     }
 }
 
 }
