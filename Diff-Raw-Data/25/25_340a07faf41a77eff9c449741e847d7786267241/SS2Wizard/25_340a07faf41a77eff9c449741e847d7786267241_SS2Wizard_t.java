 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package littlesmarttool2.GUI;
 
 import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;
 import gnu.io.NoSuchPortException;
 import gnu.io.PortInUseException;
 import gnu.io.UnsupportedCommOperationException;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.io.IOException;
 import java.lang.reflect.*;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.concurrent.TimeoutException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.*;
 import javax.swing.plaf.metal.MetalLookAndFeel;
 import littlesmarttool2.GUI.components.DotsListener;
 import littlesmarttool2.LittleSmartTool2;
 import littlesmarttool2.comm.ConnectionListener;
 import littlesmarttool2.comm.ResponseListener;
 import littlesmarttool2.comm.SerialCommand;
 import littlesmarttool2.comm.SerialController;
 import littlesmarttool2.model.Configuration;
 import littlesmarttool2.util.UpdateUtil;
 
 /**
  *
  * @author marcher89
  */
 public class SS2Wizard extends javax.swing.JFrame implements ActionListener{
 
     private static final String SELECT_PORT_MESSAGE = "Select port:";
     
     private StepPanel[] stepPanels;
     private int currentStep = -1;
     private boolean hasUploaded = false;
     private Configuration configuration;
     private SerialController controller;
     private Timer servoPullerTimer = new Timer(50, null);
     private ArrayList<ResponseListener> servoReadingListeners = new ArrayList<>();
     private boolean connecting = false;
     private boolean updatingFirmware = false;
     private boolean skipFirmwareCheck = false;
     private boolean waitingForServoResponse = false;
     private int servoRequestTimeoutCount = 0;
     private static final int MAX_SERVO_REQUEST_TIMEOUT_COUNT = 10;
     
     /**
      * Creates new form SS2Wizard
      */
     public SS2Wizard() {
         
         try {
             UIManager.setLookAndFeel(new MetalLookAndFeel());
         } catch (Exception ex) {}
         try {
             UIManager.setLookAndFeel(new WindowsLookAndFeel());
         } catch (Exception ex) {}
         initComponents();
         try {
             Class util = Class.forName("com.apple.eawt.Application");
             Method getApplication = util.getMethod("getApplication", new Class[0]);
             Object application = getApplication.invoke(util);
             Class params[] = new Class[1];
             params[0] = Image.class;
             Method setDockIconImage = util.getMethod("setDockIconImage", params);
             Image image = Toolkit.getDefaultToolkit().getImage("img/rocket_gloss.png");
             setDockIconImage.invoke(application, image);
         } catch (Exception e) {
             // never mind, then
         }
         
         configuration = new Configuration();
         controller = new SerialController();
         setTitle("LittleSmartTool 2 v" + LittleSmartTool2.Version);
         
         //Initialize port chooser
         refreshPortList();
         
         //Initialize step panels
         stepPanels = new StepPanel[]{new Step1Panel(this), new Step2Panel(this), new Step3Panel(this), new Step4Panel(this)};
         
         for (StepPanel step : stepPanels) {
             cardPanel.add(step, step.getName());
         }
         servoReadingListeners.add((ResponseListener)stepPanels[1]);
         servoReadingListeners.add((ResponseListener)stepPanels[2]);
         servoReadingListeners.add((ResponseListener)stepPanels[3]);
 
         controller.addConnectionListener(new ConnectionListener() {
             @Override
             public void connectionStateChanged(boolean connected) {
                 if (!connected && !connecting)
                 {
                     connectedLabel.setText("Not connected");
                     connectedLabel.setForeground(new Color(0x660000));
                     portChooser.setSelectedIndex(0);
                 }
             }
         });
         
         servoPullerTimer.addActionListener(this);
         goToStep(0);
     }
     
     public void stopAutoServoPulling()
     {
         servoPullerTimer.stop();
     }
     
     public void startAutoServoPulling()
     {
         servoPullerTimer.start();
     }
     
     public Configuration getConfiguration(){
         return configuration;
     }
     
     public SerialController getSerialController(){
         return controller;
     }
     
     public void setHasUploaded(boolean hasUploaded){
         this.hasUploaded = hasUploaded;
     }
     
     public boolean getHasUploaded(){
         return hasUploaded;
     }
     
     public void setBackEnabled(boolean val) { backButton.setEnabled(val);}
     
     public void setNextEnabled(boolean val) { nextButton.setEnabled(val);}
     
     public void goToNextStep() {
         if(currentStep < stepPanels.length-1)
             goToStep(currentStep+1);
     }
     
     public void goToPreviousStep() {
         if(currentStep > 0)
             goToStep(currentStep-1);
     }
     
     private void goToStep(int index) {
         backButton.setEnabled((index <= 0) ? false : true);
         nextButton.setEnabled(true);
         
         nextButton.setText((index >= stepPanels.length-1) ? "Close" : "Next");
         if(currentStep > -1) stepPanels[currentStep].onHide();
         ((CardLayout)cardPanel.getLayout()).show(cardPanel, stepPanels[index].getName());
         stepPanels[index].onDisplay();
         
         headline.setText(stepPanels[index].getName());
         currentStep = index;
     }
     
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         contentPanel = new javax.swing.JPanel();
         buttonPanel = new javax.swing.JPanel();
         backButton = new javax.swing.JButton();
         nextButton = new javax.swing.JButton();
         cardPanel = new javax.swing.JPanel();
         upperPanel = new javax.swing.JPanel();
         headline = new javax.swing.JLabel();
         portPanel = new javax.swing.JPanel();
         portLabel = new javax.swing.JLabel();
         portChooser = new javax.swing.JComboBox();
         refreshPortListButton = new javax.swing.JButton();
         connectedLabel = new javax.swing.JLabel();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle("StratoSnapper 2");
         setIconImage(new ImageIcon("./img/rocket_gloss.png").getImage());
         setLocationByPlatform(true);
         setMinimumSize(new java.awt.Dimension(800, 570));
        setPreferredSize(new java.awt.Dimension(100, 570));
 
         contentPanel.setPreferredSize(new java.awt.Dimension(100, 700));
         contentPanel.setLayout(new java.awt.BorderLayout());
 
         buttonPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 10));
         buttonPanel.setLayout(new java.awt.BorderLayout());
 
         backButton.setText("Back");
         backButton.setActionCommand("");
         backButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 backButtonActionPerformed(evt);
             }
         });
         buttonPanel.add(backButton, java.awt.BorderLayout.WEST);
 
         nextButton.setText("Next");
         nextButton.setActionCommand("");
         nextButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 nextButtonActionPerformed(evt);
             }
         });
         buttonPanel.add(nextButton, java.awt.BorderLayout.EAST);
 
         contentPanel.add(buttonPanel, java.awt.BorderLayout.SOUTH);
 
         cardPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
         cardPanel.setLayout(new java.awt.CardLayout());
         contentPanel.add(cardPanel, java.awt.BorderLayout.CENTER);
 
         upperPanel.setLayout(new java.awt.BorderLayout());
 
         headline.setFont(new java.awt.Font("Lucida Grande", 0, 18)); // NOI18N
         headline.setText("StratoSnapper 2");
         headline.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
         upperPanel.add(headline, java.awt.BorderLayout.CENTER);
 
         portPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 10));
         portPanel.setLayout(new javax.swing.BoxLayout(portPanel, javax.swing.BoxLayout.LINE_AXIS));
 
         portLabel.setText("Choose port:");
         portLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 5));
         portPanel.add(portLabel);
 
         portChooser.setMaximumSize(new java.awt.Dimension(32767, 21));
         portChooser.setPreferredSize(new java.awt.Dimension(150, 20));
         portChooser.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 portChooserItemStateChanged(evt);
             }
         });
         portPanel.add(portChooser);
 
         refreshPortListButton.setText("Refresh");
         refreshPortListButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 refreshPortListButtonActionPerformed(evt);
             }
         });
         portPanel.add(refreshPortListButton);
 
         connectedLabel.setForeground(new java.awt.Color(102, 0, 0));
         connectedLabel.setText("Not connected");
         connectedLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 5, 0, 0));
         connectedLabel.setMaximumSize(new java.awt.Dimension(150, 14));
         connectedLabel.setMinimumSize(new java.awt.Dimension(140, 14));
         connectedLabel.setPreferredSize(new java.awt.Dimension(140, 14));
         connectedLabel.setRequestFocusEnabled(false);
         portPanel.add(connectedLabel);
 
         upperPanel.add(portPanel, java.awt.BorderLayout.EAST);
 
         contentPanel.add(upperPanel, java.awt.BorderLayout.NORTH);
 
         getContentPane().add(contentPanel, java.awt.BorderLayout.CENTER);
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void backButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backButtonActionPerformed
         goToPreviousStep();
     }//GEN-LAST:event_backButtonActionPerformed
 
     private void nextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextButtonActionPerformed
         if(currentStep < stepPanels.length-1)
             goToNextStep();
         else{
             if(hasUploaded || JOptionPane.showConfirmDialog(this, "Your configuration has not been uploaded to the StratoSnapper2.\nAre you sure, you want to quit?", "Really quit?", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.YES_OPTION)
             System.exit(0);
         }
     }//GEN-LAST:event_nextButtonActionPerformed
 
     private class SerialConnector implements Runnable
     {
         String port;
         SS2Wizard wizard;
         Timer dotsTimer;
         public SerialConnector(String port, SS2Wizard wizard, Timer dotsTimer)
         {
             this.port = port;
             this.wizard = wizard;
             this.dotsTimer = dotsTimer;
         }
         @Override
         public void run() {
             boolean error = false;
             try {
                 updatingFirmware = false;
                 SerialCommand conResult = controller.connect(port,15000);
                 dotsTimer.stop();
                 //Disable output
                 if(!controller.send("N;1", 1000).equals("N;1"))//------------------------"N" Disable output
                     throw new IOException("The StratoSnapper2 returned an unexpected value, while trying to disable output.");
                 //Get space information
                 String[] space = controller.send("W", 1000).split(";");
                 try {
                     configuration.setMaxRanges(Integer.parseInt(space[1]));//Range
                     configuration.setMaxTriggers(Integer.parseInt(space[2]));//Trigger
                     configuration.setMaxLANC(Integer.parseInt(space[3]));//LANC
                     configuration.setMaxIR(Integer.parseInt(space[4]) - 1);//IR (room for one less since one space is reserved)
                     configuration.setMaxIRPulse(Integer.parseInt(space[5]));//IR Puls
                 } catch (NumberFormatException ex) {
                     throw new IOException("Unable to get maximum values.");
                 }
                 //Check version info
                 int[] ssfwVersionInfo = conResult.convertArgsToInt();
                 int mainV = ssfwVersionInfo[1], subV = ssfwVersionInfo[2];
                 if (!skipFirmwareCheck && (UpdateUtil.FirmwareMain != mainV || UpdateUtil.FirmwareSub != subV))
                 {
                     String message =  "The connected Stratosnapper has firmware version " + mainV + "." + subV
                             + "\r\nPress OK to update to the latest version!";
                     JOptionPane.showMessageDialog(wizard, message, "Firmware update!", JOptionPane.INFORMATION_MESSAGE);
                 
                     //Update message
                     dotsTimer.stop();
                     dotsTimer = new Timer(500,new DotsListener(connectedLabel, "Updating"));
                     dotsTimer.start();
                     
                     //Update firmware
                     updatingFirmware = true;
                     controller.disconnect();
                     boolean success = UpdateUtil.UpdateFirmware(port);
                     if (!success)
                     {
                         int retry = JOptionPane.showConfirmDialog(wizard, "Firmware update failed.\r\nRetry update?", "Update failed", JOptionPane.YES_NO_OPTION);
                         if (retry == JOptionPane.NO_OPTION)
                             skipFirmwareCheck = true;
                     }
                     
                     //Reconnect
                     dotsTimer.stop();
                     dotsTimer = new Timer(500,new DotsListener(connectedLabel, "Validating"));
                     dotsTimer.start();
                     SerialConnector sc2 = new SerialConnector(port, wizard, dotsTimer);
                     Thread t = new Thread(sc2);
                     t.start();
                     return;
                 }
                 
                 //Finish
                 connectedLabel.setForeground(new Color(0x006600));
                 connectedLabel.setText("Connected (v. " + mainV + "." + subV + ")");
                 servoPullerTimer.start();
             } catch (NoSuchPortException ex) {
                 Logger.getLogger("ss2logger").log(Level.SEVERE, null, ex);
                 JOptionPane.showMessageDialog(wizard, "The selected port is invalid.\r\nEnsure that you selected the right port or try to connect the Stratosnapper to another port","Invalid port", JOptionPane.ERROR_MESSAGE);
                 refreshPortList();
                 error = true;
             } catch (PortInUseException ex) {
                 Logger.getLogger("ss2logger").log(Level.SEVERE, null, ex);
                 JOptionPane.showMessageDialog(wizard, "The selected port is in use.\r\nEnsure that you selected the right port, and that no other software is using it.\r\nOn Mac computers, this can happen as a result of fault in the serial driver. To solve this, run the following commands:\r\nmkdir /var/lock\r\nchmod 777 /var/lock","Port in use", JOptionPane.ERROR_MESSAGE);
                 error = true;
             } catch (UnsupportedCommOperationException | IOException ex) {
                 Logger.getLogger("ss2logger").log(Level.SEVERE, null, ex);
                 JOptionPane.showMessageDialog(wizard, "An error occured while connecting to the Stratosnapper.\r\nPlease try again or use another port if the error persists\r\nMessage from system: \"" + ex.getMessage() + "\"","Connection error", JOptionPane.ERROR_MESSAGE);
                 error = true;
             } catch (TimeoutException ex) {
                 Logger.getLogger("ss2logger").log(Level.SEVERE, null, ex);
                 JOptionPane.showMessageDialog(wizard, "Unable to connect to StratoSnapper.\r\nEnsure that you selected the right port.\r\nIf this error persists, try turning your RC receiver off,\r\nthen try connecting to the Stratosnapper again,\r\nand finally turn your RC receiver back on.","Connection timed out", JOptionPane.ERROR_MESSAGE);
                 error = true;
             }
             finally
             {
                 if (updatingFirmware) return; //Do nothing if updating (avrdude running)
                 portChooser.setEnabled(true);
                 refreshPortListButton.setEnabled(true);
                 connecting = false;
                 dotsTimer.stop();
             }
             if (error)
             {
                 connectedLabel.setForeground(new Color(0x660000));
                 connectedLabel.setText("Connection error");
                 portChooser.setSelectedIndex(0);
             }
             System.out.println("Max ranges: "+configuration.getMaxRanges());
             System.out.println("Max triggers: "+configuration.getMaxTriggers());
             System.out.println("Max LANC: "+configuration.getMaxLANC());
             System.out.println("Max IR: "+configuration.getMaxIR());
             System.out.println("Max IR pulse: "+configuration.getMaxIRPulse());
         }    
     }
     
     private void portChooserItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_portChooserItemStateChanged
         if (evt.getStateChange() == ItemEvent.DESELECTED)
         {
             controller.disconnect();
             connectedLabel.setForeground(new Color(0x660000));
             connectedLabel.setText("Not connected");
             return;
         }
         if (evt.getItem().equals(SELECT_PORT_MESSAGE)) return;
         if (connecting) return; //TODO: Handle this somehow?
         portChooser.setEnabled(false);
         refreshPortListButton.setEnabled(false);
         connecting = true;
         connectedLabel.setText("Connecting");
         connectedLabel.setForeground(Color.orange);
         Timer dotsTimer = new Timer(500,new DotsListener(connectedLabel, "Connecting"));
         dotsTimer.start();
         new Thread(new SerialConnector(portChooser.getSelectedItem().toString(), this, dotsTimer)).start();
     }//GEN-LAST:event_portChooserItemStateChanged
 
     private void refreshPortList()
     {
         controller.disconnect();
         ArrayList<String> portNames = SerialController.getPortNames();
         portChooser.removeAllItems();
         portNames.add(0, SELECT_PORT_MESSAGE);
         portChooser.setModel(new DefaultComboBoxModel(portNames.toArray()));
     }
     
     private void refreshPortListButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshPortListButtonActionPerformed
         refreshPortList();
     }//GEN-LAST:event_refreshPortListButtonActionPerformed
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton backButton;
     private javax.swing.JPanel buttonPanel;
     private javax.swing.JPanel cardPanel;
     private javax.swing.JLabel connectedLabel;
     private javax.swing.JPanel contentPanel;
     private javax.swing.JLabel headline;
     private javax.swing.JButton nextButton;
     private javax.swing.JComboBox portChooser;
     private javax.swing.JLabel portLabel;
     private javax.swing.JPanel portPanel;
     private javax.swing.JButton refreshPortListButton;
     private javax.swing.JPanel upperPanel;
     // End of variables declaration//GEN-END:variables
 
     @Override
     public void actionPerformed(ActionEvent e) {
         if (waitingForServoResponse) return;
         try {
             waitingForServoResponse = true;
             //Servo puller timer tick
             String reading = controller.send("S", 1000);
             SerialCommand cmd = SerialCommand.fromMessage(reading);
             if (cmd.getCommand() != 'S') throw new IOException("Unexpected answer from device");
             servoRequestTimeoutCount = 0;
             for (ResponseListener l : servoReadingListeners)
                 if (l != null)
                     l.receiveResponse(cmd.getCommand(), cmd.getArgs());
         } catch (IOException ex) {
             servoPullerTimer.stop();
             controller.disconnect();
             connectedLabel.setText("Not connected");
             connectedLabel.setForeground(new Color(0x660000));
             portChooser.setSelectedIndex(0);
         } catch (TimeoutException ex) {
             //TODO: Count timeouts (if more than x, stop)
             servoRequestTimeoutCount++;
             if (servoRequestTimeoutCount > MAX_SERVO_REQUEST_TIMEOUT_COUNT)
             {
                 servoPullerTimer.stop();
                 controller.disconnect();
                 connectedLabel.setText("Not connected");
                 connectedLabel.setForeground(new Color(0x660000));
                 portChooser.setSelectedIndex(0);
                 Logger.getLogger("ss2logger").log(Level.SEVERE, "Servo reading request timed out " + servoRequestTimeoutCount + " times in a row. Disconnecting.", ex);
                 JOptionPane.showMessageDialog(this, "The connected Stratosnapper is not responding.\r\nMake sure that it is properly connected, and that the right port is selected.", "No response from Stratosnapper", JOptionPane.ERROR_MESSAGE);
             }
         }
         finally
         {
             waitingForServoResponse = false;
         }
     }
 }
