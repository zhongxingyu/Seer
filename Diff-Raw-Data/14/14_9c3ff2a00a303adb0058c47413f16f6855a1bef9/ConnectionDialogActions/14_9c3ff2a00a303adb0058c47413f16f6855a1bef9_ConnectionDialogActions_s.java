 package com.intelix.digihdmi.app.actions;
 
 import com.intelix.digihdmi.app.DigiHdmiApp;
 import com.intelix.digihdmi.app.views.DeviceConnectionDlg;
 import com.intelix.digihdmi.model.Device;
 import com.intelix.net.Connection;
 import com.intelix.net.IPConnection;
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
 
     protected boolean alterDevice() {
         String dlgIA = dlg.getIpAddr();
         int dlgPt = dlg.getPort();
 
         boolean altered = false;
 
         Connection c = device.getConnection();
         if (c instanceof IPConnection)
         {
             IPConnection ipc = (IPConnection) c;
 
             if (! ipc.getIpAddr().equals(dlgIA))
             {
                 ipc.setIpAddr(dlgIA);
                 altered = true;
             }
             if (ipc.getPort() != dlgPt)
             {
                 ipc.setPort(dlgPt);
                 altered = true;
             }
         }
 
         return altered;
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
         Connection oldC = device.getConnection();
         IPConnection newC = new IPConnection();
         newC.setIpAddr(dlg.getIpAddr());
         newC.setPort(dlg.getPort());
         boolean prevConnected = oldC.isConnected();
         boolean fail = true;
         try {
             device.setConnection(newC);
             device.connect();
             device.disconnect();
             fail = false;
         } catch (IOException ex)
         {
            JOptionPane.showMessageDialog(dlg, "Connection failed!\n" + ex.getMessage(), "Fail!", JOptionPane.ERROR_MESSAGE);
         }
 
         if (! fail)
             JOptionPane.showMessageDialog(dlg, "Success!",
                     "Win!", JOptionPane.INFORMATION_MESSAGE);
 
         try {
             device.setConnection(oldC);
             if (prevConnected)
                 device.connect();
         } catch (Exception ex) {
             JOptionPane.showMessageDialog(dlg, "Error reconnecting to previously set connection!\n"
                    + "Remaining in disconnected state."
                     + ex.getMessage(),
                     "Fail!", JOptionPane.ERROR_MESSAGE);
         }
         // after connection, report via dialog
     }
 
     @Action
     public void onConnect()
     {
         boolean altered = alterDevice();
 
         if (altered)
         {
             try {
                 device.connect();
             } catch (Exception ex) {
                 JOptionPane.showMessageDialog(dlg, "Error connecting!\n"
                        + "Remaining in disconnected state."
                         + ex.getMessage(),
                         "Fail!", JOptionPane.ERROR_MESSAGE);
             }
         }
     }
 }
