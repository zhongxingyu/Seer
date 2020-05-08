 package com.intelix.digihdmi.app.actions;
 
 import com.intelix.digihdmi.app.DigiHdmiApp;
 import com.intelix.digihdmi.app.views.dialogs.DeviceConnectionDlg;
 import com.intelix.digihdmi.model.Device;
 import com.intelix.digihdmi.util.Selectable;
 import com.intelix.net.Connection;
 import com.intelix.net.IPConnection;
 import java.awt.event.ActionEvent;
 import java.io.IOException;
 import javax.swing.JOptionPane;
 import org.jdesktop.application.Action;
 import org.jdesktop.application.Application;
 
 /**
  *
  * @author Michael Caron <michael.r.caron@gmail.com>
  */
 public class ConnectionDialogActions {
     Device device;
     DigiHdmiApp app;
     DeviceConnectionDlg dlg;
 
     public ConnectionDialogActions() {
         app = (DigiHdmiApp)Application.getInstance();
         device = app.getDevice();
         dlg = app.getConnectionDlg();
     }
 
     @Action
     public void onOk()
     {
         alterDevice();
         dlg.setVisible(false);
     }
 
     protected Connection[] alterDevice() {
         Connection oldC = device.getConnection();
         IPConnection newC = new IPConnection();
         newC.setIpAddr(dlg.getIpAddr());
         newC.setPort(dlg.getPort());
        try {
            device.setConnection(newC);
        } catch (IOException ex)
         {
            JOptionPane.showMessageDialog(dlg, "Connection failed!\n\nDetails:\n\t" + ex.getMessage(), "Fail!", JOptionPane.ERROR_MESSAGE);
         }
         return new Connection[] {oldC,newC};
     }
 
     @Action
     public void onCancel()
     {
         dlg.setVisible(false);
     }
 
     @Action
     public void onTest()
     {
         // connect to the device & show a glass pane
         Connection[] cxns = alterDevice();
         boolean prevConnected = cxns[0].isConnected();
         boolean fail = true;
         try {
             if (prevConnected) { device.disconnect(); Thread.sleep(10);}
             //device.setConnection(cxns[1]);
             Thread.sleep(10);
             device.connect();
             device.disconnect();
             fail = false;
         } catch (IOException ex)
         {
             JOptionPane.showMessageDialog(dlg, "Connection failed!\n\nDetails:\n\t" + ex.getMessage(), "Fail!", JOptionPane.ERROR_MESSAGE);
         } catch (InterruptedException ex)
         {
             // Ignore
         }
 
         if (! fail)
             JOptionPane.showMessageDialog(dlg, "Success!",
                     "Win!", JOptionPane.INFORMATION_MESSAGE);
 
         try {
             device.setConnection(cxns[0]);
             if (prevConnected)
                 device.connect();
         } catch (Exception ex) {
             JOptionPane.showMessageDialog(dlg, "Error reconnecting to previously set connection!\n"
                     + "Remaining in disconnected state.\n\nDetails:\n\t"
                     + ex.getMessage(),
                     "Fail!", JOptionPane.ERROR_MESSAGE);
         }
         // after connection, report via dialog
     }
 
     @Action
     public void onConnect(ActionEvent e)
     {
         // This code is virtually duplicated in DeviceActions.toggleConnect
         if (device.isConnected())
             try {
                 device.disconnect();
             } catch (Exception ex) {
                 JOptionPane.showMessageDialog(dlg, "Error disconnecting!\n\nDetails:\n\t"
                     + ex.getMessage(),
                     "Fail!", JOptionPane.ERROR_MESSAGE);
             }
         else
         {
             alterDevice();
             try {
                 device.connect();
                 app.showSyncDlg();
             } catch (Exception ex) {
                 JOptionPane.showMessageDialog(dlg, "Error connecting!\n"
                     + "Remaining in disconnected state.\n\nDetails:\n\t"
                     + ex.getMessage(),
                     "Fail!", JOptionPane.ERROR_MESSAGE);
                 if (e.getSource() instanceof Selectable)
                     ((Selectable)e.getSource()).setSelected(false);
             }
         }
     }
 }
