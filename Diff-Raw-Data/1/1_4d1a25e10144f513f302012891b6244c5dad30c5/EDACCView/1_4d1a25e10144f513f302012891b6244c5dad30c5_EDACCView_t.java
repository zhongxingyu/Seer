 /*
  * EDACCView.java
  */
 package edacc;
 
 import edacc.model.DatabaseConnector;
 import edacc.model.NoConnectionToDBException;
 import java.awt.Component;
 import java.sql.SQLException;
 import java.util.Observable;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.jdesktop.application.Action;
 import org.jdesktop.application.ResourceMap;
 import org.jdesktop.application.SingleFrameApplication;
 import org.jdesktop.application.FrameView;
 import org.jdesktop.application.TaskMonitor;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Observer;
 import javax.swing.Timer;
 import javax.swing.Icon;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 /**
  * The application's main frame.
  */
 public class EDACCView extends FrameView implements Observer {
 
     private EDACCExperimentMode experimentMode;
     private EDACCManageDBMode manageDBMode;
     private EDACCNoMode noMode;
     private Component mode;
     private javax.swing.GroupLayout mainPanelLayout;
 
     public EDACCView(SingleFrameApplication app) {
         super(app);
 
         initComponents();
 
         DatabaseConnector.getInstance().addObserver(this);
 
         // status bar initialization - message timeout, idle icon and busy animation, etc
         ResourceMap resourceMap = getResourceMap();
         int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
         messageTimer = new Timer(messageTimeout, new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 statusMessageLabel.setText("");
             }
         });
         messageTimer.setRepeats(false);
         int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
         for (int i = 0; i < busyIcons.length; i++) {
             busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
         }
         busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                 statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
             }
         });
         idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
         statusAnimationLabel.setIcon(idleIcon);
         progressBar.setVisible(false);
 
         // connecting action tasks to status bar via TaskMonitor
         TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
         taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
 
             public void propertyChange(java.beans.PropertyChangeEvent evt) {
                 String propertyName = evt.getPropertyName();
                 if ("started".equals(propertyName)) {
                     if (!busyIconTimer.isRunning()) {
                         statusAnimationLabel.setIcon(busyIcons[0]);
                         busyIconIndex = 0;
                         busyIconTimer.start();
                     }
                     progressBar.setVisible(true);
                     progressBar.setIndeterminate(true);
                 } else if ("done".equals(propertyName)) {
                     busyIconTimer.stop();
                     statusAnimationLabel.setIcon(idleIcon);
                     progressBar.setVisible(false);
                     progressBar.setValue(0);
                 } else if ("message".equals(propertyName)) {
                     String text = (String) (evt.getNewValue());
                     statusMessageLabel.setText((text == null) ? "" : text);
                     messageTimer.restart();
                 } else if ("progress".equals(propertyName)) {
                     int value = (Integer) (evt.getNewValue());
                     progressBar.setVisible(true);
                     progressBar.setIndeterminate(false);
                     progressBar.setValue(value);
                 }
             }
         });
         experimentMode = new EDACCExperimentMode();
         manageDBMode = new EDACCManageDBMode();
         noMode = new EDACCNoMode();
         mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
         mainPanel.setLayout(mainPanelLayout);
         mainPanelLayout.setHorizontalGroup(
                 mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(noMode, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
         mainPanelLayout.setVerticalGroup(
                 mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(noMode, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE));
 
         mode = noMode;
        updateConnectionStateView();
     }
 
     private void createDatabaseErrorMessage(SQLException e) {
         javax.swing.JOptionPane.showMessageDialog(null, "There was an error while communicating with the database: " +e, "Connection error", javax.swing.JOptionPane.ERROR_MESSAGE);
     }
 
     @Action
     public void showAboutBox() {
         if (aboutBox == null) {
             JFrame mainFrame = EDACCApp.getApplication().getMainFrame();
             aboutBox = new EDACCAboutBox(mainFrame);
             aboutBox.setLocationRelativeTo(mainFrame);
         }
         EDACCApp.getApplication().show(aboutBox);
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         mainPanel = new javax.swing.JPanel();
         menuBar = new javax.swing.JMenuBar();
         javax.swing.JMenu fileMenu = new javax.swing.JMenu();
         connectToDBMenuItem = new javax.swing.JMenuItem();
         disconnectMenuItem = new javax.swing.JMenuItem();
         generateDBMenuItem = new javax.swing.JMenuItem();
         javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
         gridMenu = new javax.swing.JMenu();
         settingsMenuItem = new javax.swing.JMenuItem();
         modusMenu = new javax.swing.JMenu();
         manageDBModeMenuItem = new javax.swing.JRadioButtonMenuItem();
         manageExperimentModeMenuItem = new javax.swing.JRadioButtonMenuItem();
         javax.swing.JMenu helpMenu = new javax.swing.JMenu();
         javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
         jMenuItem1 = new javax.swing.JMenuItem();
         statusPanel = new javax.swing.JPanel();
         javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
         statusMessageLabel = new javax.swing.JLabel();
         statusAnimationLabel = new javax.swing.JLabel();
         progressBar = new javax.swing.JProgressBar();
 
         mainPanel.setName("mainPanel"); // NOI18N
 
         org.jdesktop.layout.GroupLayout mainPanelLayout = new org.jdesktop.layout.GroupLayout(mainPanel);
         mainPanel.setLayout(mainPanelLayout);
         mainPanelLayout.setHorizontalGroup(
             mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 1006, Short.MAX_VALUE)
         );
         mainPanelLayout.setVerticalGroup(
             mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 639, Short.MAX_VALUE)
         );
 
         menuBar.setAutoscrolls(true);
         menuBar.setName("menuBar"); // NOI18N
 
         org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(edacc.EDACCApp.class).getContext().getResourceMap(EDACCView.class);
         fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
         fileMenu.setName("fileMenu"); // NOI18N
 
         javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(edacc.EDACCApp.class).getContext().getActionMap(EDACCView.class, this);
         connectToDBMenuItem.setAction(actionMap.get("btnConnectToDB")); // NOI18N
         connectToDBMenuItem.setText(resourceMap.getString("connectToDBMenuItem.text")); // NOI18N
         connectToDBMenuItem.setName("connectToDBMenuItem"); // NOI18N
         fileMenu.add(connectToDBMenuItem);
 
         disconnectMenuItem.setAction(actionMap.get("btnDisconnect")); // NOI18N
         disconnectMenuItem.setText(resourceMap.getString("disconnectMenuItem.text")); // NOI18N
         disconnectMenuItem.setName("disconnectMenuItem"); // NOI18N
         fileMenu.add(disconnectMenuItem);
 
         generateDBMenuItem.setAction(actionMap.get("btnGenerateTables")); // NOI18N
         generateDBMenuItem.setText(resourceMap.getString("generateDBMenuItem.text")); // NOI18N
         generateDBMenuItem.setName("generateDBMenuItem"); // NOI18N
         fileMenu.add(generateDBMenuItem);
 
         exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
         exitMenuItem.setName("exitMenuItem"); // NOI18N
         fileMenu.add(exitMenuItem);
 
         menuBar.add(fileMenu);
 
         gridMenu.setAction(actionMap.get("btnGridSettings")); // NOI18N
         gridMenu.setText(resourceMap.getString("gridMenu.text")); // NOI18N
         gridMenu.setName("gridMenu"); // NOI18N
 
         settingsMenuItem.setAction(actionMap.get("btnGridSettings")); // NOI18N
         settingsMenuItem.setText(resourceMap.getString("settingsMenuItem.text")); // NOI18N
         settingsMenuItem.setName("settingsMenuItem"); // NOI18N
         gridMenu.add(settingsMenuItem);
 
         menuBar.add(gridMenu);
 
         modusMenu.setText(resourceMap.getString("modusMenu.text")); // NOI18N
         modusMenu.setName("modusMenu"); // NOI18N
 
         manageDBModeMenuItem.setAction(actionMap.get("manageDBMode")); // NOI18N
         manageDBModeMenuItem.setText(resourceMap.getString("manageDBModeMenuItem.text")); // NOI18N
         manageDBModeMenuItem.setName("manageDBModeMenuItem"); // NOI18N
         modusMenu.add(manageDBModeMenuItem);
 
         manageExperimentModeMenuItem.setAction(actionMap.get("manageExperimentMode")); // NOI18N
         manageExperimentModeMenuItem.setText(resourceMap.getString("manageExperimentModeMenuItem.text")); // NOI18N
         manageExperimentModeMenuItem.setName("manageExperimentModeMenuItem"); // NOI18N
         modusMenu.add(manageExperimentModeMenuItem);
 
         menuBar.add(modusMenu);
 
         helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
         helpMenu.setName("helpMenu"); // NOI18N
 
         aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
         aboutMenuItem.setName("aboutMenuItem"); // NOI18N
         helpMenu.add(aboutMenuItem);
 
         jMenuItem1.setText(resourceMap.getString("jMenuItem1.text")); // NOI18N
         jMenuItem1.setName("jMenuItem1"); // NOI18N
         helpMenu.add(jMenuItem1);
 
         menuBar.add(helpMenu);
 
         statusPanel.setName("statusPanel"); // NOI18N
 
         statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N
 
         statusMessageLabel.setName("statusMessageLabel"); // NOI18N
 
         statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N
 
         progressBar.setName("progressBar"); // NOI18N
 
         org.jdesktop.layout.GroupLayout statusPanelLayout = new org.jdesktop.layout.GroupLayout(statusPanel);
         statusPanel.setLayout(statusPanelLayout);
         statusPanelLayout.setHorizontalGroup(
             statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(statusPanelSeparator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1006, Short.MAX_VALUE)
             .add(statusPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(statusMessageLabel)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 822, Short.MAX_VALUE)
                 .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(statusAnimationLabel)
                 .addContainerGap())
         );
         statusPanelLayout.setVerticalGroup(
             statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(statusPanelLayout.createSequentialGroup()
                 .add(statusPanelSeparator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(statusMessageLabel)
                     .add(statusAnimationLabel)
                     .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .add(3, 3, 3))
         );
 
         setComponent(mainPanel);
         setMenuBar(menuBar);
         setStatusBar(statusPanel);
     }// </editor-fold>//GEN-END:initComponents
 
     @Action
     public void btnConnectToDB() {
         if (databaseSettings == null) {
             JFrame mainFrame = EDACCApp.getApplication().getMainFrame();
             databaseSettings = new EDACCDatabaseSettingsView(mainFrame, true);
             databaseSettings.setLocationRelativeTo(mainFrame);
         }
         EDACCApp.getApplication().show(databaseSettings);
     }
 
     @Action
     public void btnDisconnect() {
         try {
             DatabaseConnector.getInstance().disconnect();
         } catch (SQLException ex) {
             JOptionPane.showMessageDialog(EDACCApp.getApplication().getMainFrame(), "An error occured while closing the database connection: \n" + ex.getMessage(), "Couldn't close database connection", JOptionPane.ERROR_MESSAGE);
         }
     }
 
     @Action
     public void btnGenerateTables() {
         if (JOptionPane.showConfirmDialog(mode,
                 "This will destroy the EDACC tables of your DB an create new ones. Do you wish to continue?",
                 "Warning!",
                 JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
             try {
                 // User clicked on "Yes"
                 DatabaseConnector.getInstance().createDBSchema();
             } catch (NoConnectionToDBException ex) {
                 JOptionPane.showMessageDialog(mode,
                         "Couldn't generate the EDACC tables: No connection to database. Please connect to a database first.",
                         "Error!", JOptionPane.ERROR_MESSAGE);
             } catch (SQLException ex) {
                 JOptionPane.showMessageDialog(mode,
                         "An error occured while trying to generate the EDACC tables: " + ex.getMessage(),
                         "Error!", JOptionPane.ERROR_MESSAGE);
             }
         }
     }
 
     public void noMode() {
         manageExperimentModeMenuItem.setSelected(false);
         manageDBModeMenuItem.setSelected(false);
         mainPanelLayout.replace(mode, noMode);
         mode = noMode;
     }
 
     @Action
     public void manageDBMode() {
         if (!manageDBModeMenuItem.isSelected()) {
             noMode();
             return;
         }
         if (manageExperimentModeMenuItem.isSelected()) {
             manageExperimentModeMenuItem.setSelected(false);
         }
         
         try {
             manageDBMode.initialize();
             mainPanelLayout.replace(mode, manageDBMode);
             mode = manageDBMode;
             manageDBModeMenuItem.setSelected(true);
         } catch (NoConnectionToDBException ex) {
             JOptionPane.showMessageDialog(this.getComponent(), "You have to connect to the database before switching modes", "No database connection", JOptionPane.ERROR_MESSAGE);
             noMode();
         }
         catch (SQLException ex) {
             createDatabaseErrorMessage(ex);
             noMode();
         }
     }
 
     @Action
     public void manageExperimentMode() {
         {
         }
         if (!manageExperimentModeMenuItem.isSelected()) {
             noMode();
             return;
         }
         if (manageDBModeMenuItem.isSelected()) {
             manageDBModeMenuItem.setSelected(false);
         }
         try {
             experimentMode.initialize();
             mainPanelLayout.replace(mode, experimentMode);
             mode = experimentMode;
             manageExperimentModeMenuItem.setSelected(true);
         } catch (NoConnectionToDBException ex) {
             JOptionPane.showMessageDialog(this.getComponent(), "You have to connect to the database before switching modes", "No database connection", JOptionPane.ERROR_MESSAGE);
             noMode();
         }
         catch (SQLException ex) {
             createDatabaseErrorMessage(ex);
             noMode();
         }
     }
 
     @Action
     public void btnGridSettings() {
         if (gridSettings == null) {
             JFrame mainFrame = EDACCApp.getApplication().getMainFrame();
             gridSettings = new EDACCGridSettingsView(mainFrame, true);
             gridSettings.setLocationRelativeTo(mainFrame);
         }
         try {
             gridSettings.loadSettings();
             EDACCApp.getApplication().show(gridSettings);
         }
         catch (NoConnectionToDBException e) {
             JOptionPane.showMessageDialog(this.getComponent(), "Couldn't load settings. No connection to database", "No database connection", JOptionPane.ERROR_MESSAGE);
         }
         catch (SQLException e) {
             JOptionPane.showMessageDialog(this.getComponent(), "Error while loading settings: \n" + e.getMessage(), "Error loading settings", JOptionPane.ERROR_MESSAGE);
         }
         
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JMenuItem connectToDBMenuItem;
     private javax.swing.JMenuItem disconnectMenuItem;
     private javax.swing.JMenuItem generateDBMenuItem;
     private javax.swing.JMenu gridMenu;
     private javax.swing.JMenuItem jMenuItem1;
     private javax.swing.JPanel mainPanel;
     private javax.swing.JRadioButtonMenuItem manageDBModeMenuItem;
     private javax.swing.JRadioButtonMenuItem manageExperimentModeMenuItem;
     private javax.swing.JMenuBar menuBar;
     private javax.swing.JMenu modusMenu;
     private javax.swing.JProgressBar progressBar;
     private javax.swing.JMenuItem settingsMenuItem;
     private javax.swing.JLabel statusAnimationLabel;
     private javax.swing.JLabel statusMessageLabel;
     private javax.swing.JPanel statusPanel;
     // End of variables declaration//GEN-END:variables
     private final Timer messageTimer;
     private final Timer busyIconTimer;
     private final Icon idleIcon;
     private final Icon[] busyIcons = new Icon[15];
     private int busyIconIndex = 0;
     private JDialog aboutBox;
     private JDialog databaseSettings;
     private EDACCGridSettingsView gridSettings;
 
     public void update(Observable o, Object arg) {
         // watch connection state
         updateConnectionStateView();
     }
 
     /**
      * Updates the GUI components which are sensitive on the DB connection state.
      */
     private void updateConnectionStateView() {
         boolean state = DatabaseConnector.getInstance().isConnected();
         connectToDBMenuItem.setEnabled(!state);
         disconnectMenuItem.setEnabled(state);
         generateDBMenuItem.setEnabled(state);
     }
 }
