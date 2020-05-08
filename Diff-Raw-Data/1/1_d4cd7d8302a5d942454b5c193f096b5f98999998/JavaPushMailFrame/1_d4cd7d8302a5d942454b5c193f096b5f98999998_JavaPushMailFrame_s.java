 package com.mofirouz.javapushmail.app.ui;
 
 import com.mofirouz.javapushmail.JavaPushMailAccount;
 import com.mofirouz.javapushmail.app.JavaPushMailAccountsManager;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Image;
 import java.awt.MenuItem;
 import java.awt.PopupMenu;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import javax.mail.MessagingException;
 import javax.swing.*;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.DefaultTableModel;
 
 /**
  *
  * @author Mo Firouz
  * @since 2/10/11
  */
 public class JavaPushMailFrame {
     protected JavaPushMailAccountsManager manager;
     protected JFrame frame;
     protected JTabbedPane tabbedPanel;
     protected JTable accountsTable;
     protected JMenuBar menu;
     protected Image dockIcon;
     protected boolean waitingState = false;
     private TablePopup tablePopup;
     private JavaPushMailAccountSettingsPanel settingsPanel;
     private NewAccountDialog accountDialog;
 
     public JavaPushMailFrame() {
         dockIcon = Toolkit.getDefaultToolkit().createImage(getClass().getResource("dock.png"));//(new ImageIcon(this.getClass().getResource("dock.png"))).getImage();
     }
 
     public void init(JavaPushMailAccountsManager manager) {
         this.manager = manager;
         buildTrayPopup();
         frame = new JFrame("Push Mail Configuration Wizard");
         buildPanels();
         buildPopup();
         buildTabs();
         buildFrame();
         loadPreferences();
         initKeyboardHook();
     }
 
     private void buildFrame() {
         frame.add(tabbedPanel);
         frame.setResizable(false);
         frame.setIconImage(dockIcon);
         frame.getRootPane().setDefaultButton(settingsPanel.getHideButton());
         frame.addWindowListener(new WindowListener() {
             public void windowOpened(WindowEvent e) {
                 tablePopup.setVisible(false);
             }
 
             public void windowClosing(WindowEvent e) {
                 tablePopup.setVisible(false);
             }
 
             public void windowClosed(WindowEvent e) {
                 tablePopup.setVisible(false);
             }
 
             public void windowIconified(WindowEvent e) {
                 tablePopup.setVisible(false);
             }
 
             public void windowDeiconified(WindowEvent e) {
                 tablePopup.setVisible(false);
             }
 
             public void windowActivated(WindowEvent e) {
                 tablePopup.setVisible(false);
             }
 
             public void windowDeactivated(WindowEvent e) {
                 tablePopup.setVisible(false);
             }
         });
         fixMenuBar();
         frame.pack();
     }
 
     private void buildTabs() {
         tabbedPanel = new JTabbedPane();
         tabbedPanel.add(settingsPanel);
     }
 
     private void buildPanels() {
         settingsPanel = new JavaPushMailAccountSettingsPanel();
         settingsPanel.getNewAccountButton().addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent ae) {
                 showAddNewAccountDialog();
             }
         });
         settingsPanel.getQuitButton().addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent ae) {
                 quitApplication(true);
             }
         });
         settingsPanel.getHideButton().addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent ae) {
                 tablePopup.setVisible(false);
                 manager.saveAccounts();
                 frame.dispose();
             }
         });
         accountsTable = settingsPanel.getAccountTable();
         configTable();
     }
 
     private void configTable() {
         for (int column = 0; column < accountsTable.getColumnCount(); column++) {
             accountsTable.getColumnModel().getColumn(column).setCellRenderer(new AccountCellRenderer());
         }
         accountsTable.getModel().addTableModelListener(new TableModelListener() {
             public void tableChanged(TableModelEvent tme) {
                 updateModel(tme.getFirstRow(), tme.getColumn());
             }
         });
     }
 
     private void buildPopup() {
         tablePopup = new TablePopup();
 
         final JMenuItem deleteItem = new JMenuItem("Remove");
         deleteItem.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent ae) {
                 tablePopup.setVisible(false);
 
                 setWaitingState(true);
                 SwingWorker worker = new SwingWorker<String, Object>() {
                     @Override
                     public String doInBackground() {
                         manager.removeAccount(tablePopup.row);
                         return "";
                     }
 
                     @Override
                     protected void done() {
                         setWaitingState(false);
                     }
                 };
                 worker.execute();
             }
         });
 
         final JMenuItem dis_connectItem = new JMenuItem("Connect");
         dis_connectItem.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent ae) {
                 tablePopup.setVisible(false);
                 setWaitingState(true);
 
                 SwingWorker worker = new SwingWorker<String, Object>() {
                     @Override
                     public String doInBackground() {
                         if (manager.getAccount(tablePopup.row).isConnected()) {
                             manager.getAccount(tablePopup.row).disconnect();
                         } else {
                             manager.getAccount(tablePopup.row).connect();
                         }
                         return "";
                     }
 
                     @Override
                     protected void done() {
                     }
                 };
                 worker.execute();
             }
         });
 
         tablePopup.add(dis_connectItem);
         tablePopup.addSeparator();
         tablePopup.add(deleteItem);
 
         accountsTable.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent me) {
                 if (!accountsTable.isEnabled())
                     return;
 
                 if (tablePopup.isVisible())
                     tablePopup.setVisible(false);
 
                 if (me.getButton() == MouseEvent.BUTTON3) {
                     tablePopup.setLocation(me.getLocationOnScreen());
                     if (accountsTable.rowAtPoint(me.getPoint()) != -1) {
                         tablePopup.setSelectedRow(accountsTable.rowAtPoint(me.getPoint()));
 
                         dis_connectItem.setText("Connect");
                         if (manager.getAccount(tablePopup.row).isConnected()) {
                             dis_connectItem.setText("Disconnect");
                         }
 
                         tablePopup.setVisible(true);
                     } else {
                         tablePopup.setVisible(false);
                     }
                 } else {
                     tablePopup.setVisible(false);
                 }
             }
         });
     }
 
     private void buildTrayPopup() {
         MenuItem exit = new MenuItem("Exit Notifier");
         exit.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 quitApplication(true);
             }
         });
         MenuItem settings = new MenuItem("Settings");
         settings.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 frame.setVisible(true);
             }
         });
 
         PopupMenu popmenu = new PopupMenu();
         popmenu.add(settings);
         popmenu.add(exit);
 
         manager.getSystemNotification().setPopupMenu(popmenu);
     }
 
     protected void fixMenuBar() {
     }
 
     private void initKeyboardHook() {
         //TODO:
     }
 
     private void updateModel(int row, int col) {
         tablePopup.setVisible(false);
         if (col == -1)
             return;
 
         final JavaPushMailAccount mail = manager.getAccount(row);
 
         boolean requiresDisconnect = true;
 
         switch (col) {
             case 0:
                 mail.setAccountName((String) accountsTable.getModel().getValueAt(row, 0));
                 requiresDisconnect = false;
                 break;
             case 1:
                 mail.setServerAddress((String) accountsTable.getModel().getValueAt(row, 1));
                 break;
             case 2:
                 mail.setServerPort((Integer) accountsTable.getModel().getValueAt(row, 2));
                 break;
             case 3:
                 mail.setUseSSL((Boolean) accountsTable.getModel().getValueAt(row, 3));
                 break;
             case 4:
                 mail.setUsername((String) accountsTable.getModel().getValueAt(row, 4));
                 break;
             case 5:
                 mail.setPassword((String) accountsTable.getModel().getValueAt(row, 5));
                 break;
         }
 
         if (requiresDisconnect) {
             setWaitingState(true);
             SwingWorker worker = new SwingWorker<String, Object>() {
                 @Override
                 public String doInBackground() {
                     if (mail.isConnected())
                         mail.disconnect();
 
                     return "";
                 }
 
                 @Override
                 protected void done() {
                 }
             };
             worker.execute();
         }
     }
 
     private void showAddNewAccountDialog() {
         tablePopup.setVisible(false);
         accountDialog = new NewAccountDialog(this, manager);
         accountDialog.setVisible(true);
     }
 
     public void setWaitingState(boolean show) {
         waitingState = show;
         accountsTable.setEnabled(!show);
         settingsPanel.getNewAccountButton().setEnabled(!show);
         accountsTable.clearSelection();
         settingsPanel.getWorkingLabel().setVisible(show);
     }
 
     public boolean isInWaitingState() {
         return waitingState;
     }
 
     public void showMe(boolean go) {
         frame.setLocationRelativeTo(null);
         frame.pack();
         frame.setVisible(go);
     }
 
     public void quitApplication(boolean save) {
         tablePopup.setVisible(false);
         frame.dispose();
 
         manager.disconnectAllAccounts();
         while (true) {
             if (manager.allDisconnected()) {
                 if (save) {
                     manager.saveAccounts();
                     savePreferences();
                 }
                 break;
             }
         }
         System.exit(0);
     }
 
     public void onErrorCallback(Exception ex) {
         refreshTable();
         if (ex instanceof MessagingException) {
             String error = "";
             error += "You have been disconnected. Please reconnect manually.\n";
             error += "\t\tError: " + ex.toString();
             error += "\nPlease check the followings:";
             error += "\n- You have Internet Connectivity";
             error += "\n- You have entered the details correctly";
             error += "\n- Your mail server is operational";
             error += "\n- Your mail server supports IDLE and Push notifications";
             JOptionPane.showMessageDialog(frame, error);
         }
         ex.printStackTrace();
         setWaitingState(false);
     }
 
     public synchronized void updateOnModelChange() {
         refreshTable();
     }
 
     public synchronized void updateOnStateChange() {
         refreshTable();
         setWaitingState(false);
     }
 
     public synchronized void refreshTable() {
         if (accountsTable == null)
             return;
 
         DefaultTableModel model = (DefaultTableModel) accountsTable.getModel();
         model.setRowCount(0);
         for (int i = 0; i < manager.countAccounts(); i++) {
             model.addRow(manager.getAccount(i).getVectorData());
         }
     }
 
     // should only be called upon pressing Save button on the frame,
     // or when the app is closing by the main frame, not the system tray.
     protected boolean savePreferences() {
         return false;
     }
 
     // should be called automatically at startup, if perferences exists.
     protected boolean loadPreferences() {
         return false;
     }
 
     public boolean isUsingPerferences() {
         // checks whether perefences exits, and if so, does not show the initial frame at start up.
         boolean res = false;
 
         return res;
     }
 
     private class AccountCellRenderer extends DefaultTableCellRenderer {
         @Override
         public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
             setHorizontalAlignment(SwingConstants.CENTER);
 
             if (manager.getAccount(row).isConnected())
                 setBackground(Color.WHITE);
             else
                 setBackground(Color.RED);
 
             Object newvalue = value;
             if (column == 5)
                 newvalue = JavaPushMailAccountSettingsPanel.PASSWORD_FIELD;
 
             return super.getTableCellRendererComponent(table, newvalue, isSelected, hasFocus, row, column);
         }
     }
 
     private class TablePopup extends JPopupMenu {
         int row;
 
         public void setSelectedRow(int i) {
             row = i;
         }
     }
 }
