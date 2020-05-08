 /**
  * Copyright (C) 2013 SINTEF <franck.fleurey@sintef.no>
  *
  * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, Version 3, 29 June 2007;
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * 	http://www.gnu.org/licenses/lgpl-3.0.txt
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.thingml.sensgui.desktop;
 
 import java.io.File;
 import java.lang.reflect.Field;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.prefs.Preferences;
 import javax.swing.JFileChooser;
 import org.thingml.sensgui.adapters.ChestBeltAdapter;
 import org.thingml.sensgui.adapters.DummySensGUIAdapter;
 import org.thingml.sensgui.adapters.EMGPrototypeAdapter;
 import org.thingml.sensgui.adapters.IsensUAdapter;
 import org.thingml.sensgui.adapters.SensGUIAdapter;
 
 /**
  *
  * @author ffl
  */
 public class SensGUIMainFrame extends javax.swing.JFrame {
 
     protected ArrayList<SensorPanel> sensors = new ArrayList<SensorPanel>();
     
     
     JFileChooser chooser = new JFileChooser();
     Preferences prefs = Preferences.userRoot().node(this.getClass().getName());
     
     /**
      * Creates new form SensGUIMainFrame
      */
     public SensGUIMainFrame() {
         initComponents();
         chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY);
         chooser.setMultiSelectionEnabled(false);
         
         jMenuItemStopLog.setEnabled(false);
     }
     
     public String createSessionName() {
         SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
         return timestampFormat.format( Calendar.getInstance().getTime());
     }
     
     public void startLogging() {
 
         try {
             
             File f = new File(prefs.get("LogFolder", ""));
             if (f.exists() && f.isDirectory()) chooser.setCurrentDirectory(f);
         
             do {
                 int result = chooser.showDialog(this, "Start Logging");
                 if (result != JFileChooser.APPROVE_OPTION) return;
             }
             while (!chooser.getSelectedFile().exists() || !chooser.getSelectedFile().isDirectory());
             
             prefs.put("LogFolder", chooser.getSelectedFile().getAbsolutePath());
             
             String sName = createSessionName();
             File base_folder = new File(chooser.getSelectedFile(), sName);
 
             // To avoid overwriting an exiting folder (in case several logs are created at the same time)
            int i=1;
            while (base_folder.exists()) {
                base_folder = new File(chooser.getSelectedFile(), sName + "-" + i);
                i++;
            }
 
            base_folder.mkdir();
            
            
 
             for (SensorPanel s : sensors) {
                 if (s.includeInLog()) s.startLogging(base_folder);
             }
             jMenuItemStartLog.setEnabled(false);
             jMenuItemStopLog.setEnabled(true);
         }
         catch(Exception e) {
             e.printStackTrace();
         }
     }
     
     public void stopLogging() {
         for (SensorPanel s : sensors) {
             if (s.includeInLog()) s.stopLogging();
         }
         
         jMenuItemStartLog.setEnabled(true);
         jMenuItemStopLog.setEnabled(false);
     }
     
     public void refreshList() {
         jPanelSensorList.removeAll();
         //jPanelSensorList.revalidate();
          for (SensorPanel s : sensors) {
             s.refresh();
             jPanelSensorList.add(s);
         }
         jPanelSensorList.revalidate();
         jPanelSensorList.repaint();
     }
     
     public void removeDisconnected() {
         ArrayList<SensorPanel> disc = new ArrayList<SensorPanel>();
         for (SensorPanel s : sensors) {
             if (s.getSensor() == null || !s.getSensor().isConnected()) {
                 disc.add(s);
             }
         }
         sensors.removeAll(disc);
         refreshList();
     }
     
     public void disconnectAll() {
         for (SensorPanel s : sensors) {
             s.do_disconnect();
         }
     }
     
     public void addSensor(SensGUIAdapter adapter) {
        adapter.connect();
        if (adapter.isConnected()) {
            SensorPanel panel = new SensorPanel(adapter);
            adapter.addListener(panel);
            sensors.add(panel);
            refreshList();
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
 
         jScrollPane2 = new javax.swing.JScrollPane();
         jPanelSensorList = new javax.swing.JPanel();
         jMenuBar1 = new javax.swing.JMenuBar();
         jMenuFile = new javax.swing.JMenu();
         jMenuItemExit = new javax.swing.JMenuItem();
         jMenuConnect = new javax.swing.JMenu();
         jMenuItemConnISenseU = new javax.swing.JMenuItem();
         jMenuItemConnChestBelt = new javax.swing.JMenuItem();
         jMenuItem1 = new javax.swing.JMenuItem();
         jMenuItemConnectDummy = new javax.swing.JMenuItem();
         jSeparator1 = new javax.swing.JPopupMenu.Separator();
         jMenuItemDisconnectAll = new javax.swing.JMenuItem();
         jMenuView = new javax.swing.JMenu();
         jMenuItemRefresh = new javax.swing.JMenuItem();
         jMenuItemRMDisconnected = new javax.swing.JMenuItem();
         jMenuLog = new javax.swing.JMenu();
         jMenuItemStartLog = new javax.swing.JMenuItem();
         jMenuItemStopLog = new javax.swing.JMenuItem();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
 
         jPanelSensorList.setLayout(new javax.swing.BoxLayout(jPanelSensorList, javax.swing.BoxLayout.PAGE_AXIS));
         jScrollPane2.setViewportView(jPanelSensorList);
 
         jMenuFile.setText("File");
 
         jMenuItemExit.setText("Exit");
         jMenuItemExit.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItemExitActionPerformed(evt);
             }
         });
         jMenuFile.add(jMenuItemExit);
 
         jMenuBar1.add(jMenuFile);
 
         jMenuConnect.setText("Connect");
 
         jMenuItemConnISenseU.setText("ISenseU...");
         jMenuItemConnISenseU.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItemConnISenseUActionPerformed(evt);
             }
         });
         jMenuConnect.add(jMenuItemConnISenseU);
 
         jMenuItemConnChestBelt.setText("ChestBelt...");
         jMenuItemConnChestBelt.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItemConnChestBeltActionPerformed(evt);
             }
         });
         jMenuConnect.add(jMenuItemConnChestBelt);
 
        jMenuItem1.setText("EMG Proptotype...");
         jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItem1ActionPerformed(evt);
             }
         });
         jMenuConnect.add(jMenuItem1);
 
         jMenuItemConnectDummy.setText("Dummy sensor...");
         jMenuItemConnectDummy.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItemConnectDummyActionPerformed(evt);
             }
         });
         jMenuConnect.add(jMenuItemConnectDummy);
         jMenuConnect.add(jSeparator1);
 
         jMenuItemDisconnectAll.setText("Disconnect All");
         jMenuItemDisconnectAll.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItemDisconnectAllActionPerformed(evt);
             }
         });
         jMenuConnect.add(jMenuItemDisconnectAll);
 
         jMenuBar1.add(jMenuConnect);
 
         jMenuView.setText("View");
 
         jMenuItemRefresh.setText("Refresh");
         jMenuItemRefresh.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItemRefreshActionPerformed(evt);
             }
         });
         jMenuView.add(jMenuItemRefresh);
 
         jMenuItemRMDisconnected.setText("Remove Disconnected");
         jMenuItemRMDisconnected.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItemRMDisconnectedActionPerformed(evt);
             }
         });
         jMenuView.add(jMenuItemRMDisconnected);
 
         jMenuBar1.add(jMenuView);
 
         jMenuLog.setText("Log");
 
         jMenuItemStartLog.setText("Start Logging");
         jMenuItemStartLog.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItemStartLogActionPerformed(evt);
             }
         });
         jMenuLog.add(jMenuItemStartLog);
 
         jMenuItemStopLog.setText("Stop Logging");
         jMenuItemStopLog.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItemStopLogActionPerformed(evt);
             }
         });
         jMenuLog.add(jMenuItemStopLog);
 
         jMenuBar1.add(jMenuLog);
 
         setJMenuBar(jMenuBar1);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 813, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 465, Short.MAX_VALUE)
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void jMenuItemExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemExitActionPerformed
         disconnectAll();
         System.exit(0);
     }//GEN-LAST:event_jMenuItemExitActionPerformed
 
     private void jMenuItemDisconnectAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemDisconnectAllActionPerformed
         disconnectAll();
     }//GEN-LAST:event_jMenuItemDisconnectAllActionPerformed
 
     private void jMenuItemConnectDummyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemConnectDummyActionPerformed
        DummySensGUIAdapter adapter = new DummySensGUIAdapter();
        addSensor(adapter);
        
     }//GEN-LAST:event_jMenuItemConnectDummyActionPerformed
 
     private void jMenuItemRMDisconnectedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemRMDisconnectedActionPerformed
         removeDisconnected();
     }//GEN-LAST:event_jMenuItemRMDisconnectedActionPerformed
 
     private void jMenuItemConnISenseUActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemConnISenseUActionPerformed
         addSensor(new IsensUAdapter());
     }//GEN-LAST:event_jMenuItemConnISenseUActionPerformed
 
     private void jMenuItemConnChestBeltActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemConnChestBeltActionPerformed
         addSensor(new ChestBeltAdapter());
     }//GEN-LAST:event_jMenuItemConnChestBeltActionPerformed
 
     private void jMenuItemRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemRefreshActionPerformed
         refreshList();
     }//GEN-LAST:event_jMenuItemRefreshActionPerformed
 
     private void jMenuItemStartLogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemStartLogActionPerformed
         startLogging();
     }//GEN-LAST:event_jMenuItemStartLogActionPerformed
 
     private void jMenuItemStopLogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemStopLogActionPerformed
         stopLogging();
     }//GEN-LAST:event_jMenuItemStopLogActionPerformed
 
     private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
         addSensor(new EMGPrototypeAdapter());
     }//GEN-LAST:event_jMenuItem1ActionPerformed
 
     /**
      * @param args the command line arguments
      */
     public static void main(String args[]) {
         
         
         try {
             // This is a very dirty hack to try and set the java.library.path dynamically.
             System.setProperty("java.library.path", ".");
             Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
             fieldSysPath.setAccessible(true);
             fieldSysPath.set(null, null);
         } catch (Exception e) {
             e.printStackTrace();
         }
         
         /* Set the Nimbus look and feel */
         //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
         /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
          * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
          */
        
         //</editor-fold>
 
         /* Create and display the form */
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 new SensGUIMainFrame().setVisible(true);
             }
         });
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JMenuBar jMenuBar1;
     private javax.swing.JMenu jMenuConnect;
     private javax.swing.JMenu jMenuFile;
     private javax.swing.JMenuItem jMenuItem1;
     private javax.swing.JMenuItem jMenuItemConnChestBelt;
     private javax.swing.JMenuItem jMenuItemConnISenseU;
     private javax.swing.JMenuItem jMenuItemConnectDummy;
     private javax.swing.JMenuItem jMenuItemDisconnectAll;
     private javax.swing.JMenuItem jMenuItemExit;
     private javax.swing.JMenuItem jMenuItemRMDisconnected;
     private javax.swing.JMenuItem jMenuItemRefresh;
     private javax.swing.JMenuItem jMenuItemStartLog;
     private javax.swing.JMenuItem jMenuItemStopLog;
     private javax.swing.JMenu jMenuLog;
     private javax.swing.JMenu jMenuView;
     private javax.swing.JPanel jPanelSensorList;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JPopupMenu.Separator jSeparator1;
     // End of variables declaration//GEN-END:variables
 }
