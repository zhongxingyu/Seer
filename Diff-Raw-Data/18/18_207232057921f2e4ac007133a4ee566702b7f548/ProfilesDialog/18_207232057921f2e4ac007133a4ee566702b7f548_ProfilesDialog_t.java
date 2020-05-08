 /*
  * Copyright 2012 adeleon.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package ard.piraso.ui.base;
 
 import ard.piraso.ui.api.NewContextMonitorModel;
 import ard.piraso.ui.api.ProfileModel;
 import ard.piraso.ui.base.manager.ModelManagers;
 import ard.piraso.ui.base.manager.ModelVisitor;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.openide.ErrorManager;
 import org.openide.windows.WindowManager;
 
 import javax.swing.*;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableColumn;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 
 /**
  *
  * @author adeleon
  */
 public class ProfilesDialog extends javax.swing.JDialog {
 
     private DefaultListModel listModel = new DefaultListModel();
 
     private DefaultTableModel tableModel = new DefaultTableModel(0, 3) {
         @Override
         public boolean isCellEditable(int row, int column) {
             return false;
         }
     };
 
     /**
      * Creates new form ProfilesDialog
      */
     public ProfilesDialog() {
         super(WindowManager.getDefault().getMainWindow(), true);
         setTitle("Manage Profiles");
 
         initComponents();
         initTable();
         addButtonRefreshListeners();
 
         lstProfiles.setModel(listModel);
         lstProfiles.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         lstProfiles.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
             @Override
             public void valueChanged(ListSelectionEvent e) {
                 if(e.getValueIsAdjusting()) return;
                 initSelection((String) lstProfiles.getSelectedValue());
             }
         });
 
         setLocationRelativeTo(getOwner());
         refresh();
     }
 
     private void initTable() {
         TableColumn nameColumn = jtable.getColumnModel().getColumn(0);
         TableColumn urlColumn = jtable.getColumnModel().getColumn(1);
         TableColumn watchColumn = jtable.getColumnModel().getColumn(2);
 
         nameColumn.setHeaderValue("Name");
         urlColumn.setPreferredWidth(210);
         nameColumn.setMaxWidth(235);
 
         urlColumn.setHeaderValue("Logging URL");
         urlColumn.setPreferredWidth(220);
         urlColumn.setMaxWidth(700);
 
         watchColumn.setHeaderValue("Watch Address");
         watchColumn.setMaxWidth(200);
 
         jtable.setShowHorizontalLines(false);
         jtable.setAutoscrolls(true);
         jtable.setColumnSelectionAllowed(false);
         jtable.getTableHeader().setReorderingAllowed(false);
     }
 
     private void addButtonRefreshListeners() {
         txtName.getDocument().addDocumentListener(new DocumentListener() {
             @Override
             public void insertUpdate(DocumentEvent e) {
                 refreshButtons();
             }
             @Override
             public void removeUpdate(DocumentEvent e) {
                 refreshButtons();
             }
             @Override
             public void changedUpdate(DocumentEvent e) {
                 refreshButtons();
             }
         });
 
         jtable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
             @Override
             public void valueChanged(ListSelectionEvent e) {
                 refreshButtons();
             }
         });
     }
 
     private void refreshButtons() {
         boolean enabled = checkEnabled();
 
         btnSave.setEnabled(enabled);
         if(enabled &&  ModelManagers.PROFILES.contains(txtName.getText())) {
             btnRemove.setEnabled(enabled);
         } else {
             btnRemove.setEnabled(false);
         }
 
         btnAssociate.setEnabled(tableModel.getRowCount() < ModelManagers.MONITORS.size());
         btnDisassociate.setEnabled(jtable.getSelectedRowCount() > 0);
     }
 
     private boolean checkEnabled() {
         if(StringUtils.isBlank(txtName.getText())) {
             return false;
         }
         if(tableModel.getRowCount() <= 0) {
             return false;
         }
 
         return true;
     }
 
     private void initSelection(String name) {
         ProfileModel model = ModelManagers.PROFILES.get(name);
 
         if(model == null) {
             // no selection
             return;
         }
 
         txtName.setText(model.getName());
         txtDesc.setText(model.getDesc());
 
         tableModel.setRowCount(0);
 
         for(String monitorName : model.getMonitors()) {
             addMonitor(ModelManagers.MONITORS.get(monitorName));
         }
 
         refreshButtons();
     }
 
     private void addMonitor(NewContextMonitorModel monitor) {
         if(monitor == null) {
             return;
         }
 
         tableModel.addRow(new Object[] {
                 monitor.getName(),
                 monitor.getLoggingUrl(),
                 monitor.getWatchedAddr() == null ? "SELF" : monitor.getWatchedAddr()
         });
     }
 
     public void refresh() {
         refresh(null);
     }
 
     public void refresh(String name) {
         listModel.clear();
 
         ModelManagers.PROFILES.visit(new ModelVisitor<ProfileModel>() {
             @Override
             public void visit(ProfileModel model) {
                 listModel.addElement(model.getName());
             }
         });
 
         if(name != null) {
             int index = listModel.indexOf(name);
 
             if(index > -1) {
                 lstProfiles.setSelectedIndex(index);
             }
 
             return;
         }
 
         // make the first index as selection
         lstProfiles.setSelectedIndex(0);
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jLabel1 = new javax.swing.JLabel();
         jScrollPane1 = new javax.swing.JScrollPane();
         lstProfiles = new javax.swing.JList();
         btnClose = new javax.swing.JButton();
         jPanel1 = new javax.swing.JPanel();
         btnAssociate = new javax.swing.JButton();
         btnDisassociate = new javax.swing.JButton();
         txtName = new javax.swing.JTextField();
         jLabel4 = new javax.swing.JLabel();
         jScrollPane2 = new javax.swing.JScrollPane();
         txtDesc = new javax.swing.JTextArea();
         jLabel5 = new javax.swing.JLabel();
         jSeparator1 = new javax.swing.JSeparator();
         jLabel2 = new javax.swing.JLabel();
         jScrollPane3 = new javax.swing.JScrollPane();
         jtable = new javax.swing.JTable();
         jPanel2 = new javax.swing.JPanel();
         btnSave = new javax.swing.JButton();
         btnRemove = new javax.swing.JButton();
         btnClear = new javax.swing.JButton();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
 
         jLabel1.setText(org.openide.util.NbBundle.getMessage(ProfilesDialog.class, "ProfilesDialog.jLabel1.text")); // NOI18N
 
         lstProfiles.setModel(new javax.swing.AbstractListModel() {
             String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
             public int getSize() { return strings.length; }
             public Object getElementAt(int i) { return strings[i]; }
         });
         jScrollPane1.setViewportView(lstProfiles);
 
         btnClose.setText(org.openide.util.NbBundle.getMessage(ProfilesDialog.class, "ProfilesDialog.btnClose.text")); // NOI18N
         btnClose.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnCloseActionPerformed(evt);
             }
         });
 
         btnAssociate.setText(org.openide.util.NbBundle.getMessage(ProfilesDialog.class, "ProfilesDialog.btnAssociate.text")); // NOI18N
         btnAssociate.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnAssociateActionPerformed(evt);
             }
         });
 
         btnDisassociate.setText(org.openide.util.NbBundle.getMessage(ProfilesDialog.class, "ProfilesDialog.btnDisassociate.text")); // NOI18N
         btnDisassociate.setEnabled(false);
         btnDisassociate.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnDisassociateActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(btnAssociate, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 106, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
             .add(btnDisassociate, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 106, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel1Layout.createSequentialGroup()
                 .add(btnAssociate)
                 .add(1, 1, 1)
                 .add(btnDisassociate)
                 .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         txtName.setText(org.openide.util.NbBundle.getMessage(ProfilesDialog.class, "ProfilesDialog.txtName.text")); // NOI18N
 
         jLabel4.setText(org.openide.util.NbBundle.getMessage(ProfilesDialog.class, "ProfilesDialog.jLabel4.text")); // NOI18N
 
         txtDesc.setColumns(20);
         txtDesc.setRows(5);
         jScrollPane2.setViewportView(txtDesc);
 
         jLabel5.setText(org.openide.util.NbBundle.getMessage(ProfilesDialog.class, "ProfilesDialog.jLabel5.text")); // NOI18N
 
         jLabel2.setText(org.openide.util.NbBundle.getMessage(ProfilesDialog.class, "ProfilesDialog.jLabel2.text")); // NOI18N
 
         jtable.setModel(tableModel);
         jScrollPane3.setViewportView(jtable);
 
         btnSave.setText(org.openide.util.NbBundle.getMessage(ProfilesDialog.class, "ProfilesDialog.btnSave.text")); // NOI18N
         btnSave.setEnabled(false);
         btnSave.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSaveActionPerformed(evt);
             }
         });
 
         btnRemove.setText(org.openide.util.NbBundle.getMessage(ProfilesDialog.class, "ProfilesDialog.btnRemove.text")); // NOI18N
         btnRemove.setEnabled(false);
         btnRemove.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnRemoveActionPerformed(evt);
             }
         });
 
         btnClear.setText(org.openide.util.NbBundle.getMessage(ProfilesDialog.class, "ProfilesDialog.btnClear.text")); // NOI18N
         btnClear.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnClearActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
         jPanel2.setLayout(jPanel2Layout);
         jPanel2Layout.setHorizontalGroup(
             jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(btnSave, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 106, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
             .add(btnRemove, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 106, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
             .add(btnClear, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 106, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
         );
         jPanel2Layout.setVerticalGroup(
             jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel2Layout.createSequentialGroup()
                 .add(btnSave)
                 .add(1, 1, 1)
                 .add(btnRemove)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .add(btnClear)
                 .addContainerGap())
         );
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .add(14, 14, 14)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(layout.createSequentialGroup()
                         .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 237, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                             .add(layout.createSequentialGroup()
                                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                     .add(jLabel4)
                                     .add(jLabel5))
                                 .add(18, 18, 18)
                                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                     .add(txtName, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 447, Short.MAX_VALUE)
                                     .add(jScrollPane2))
                                 .add(122, 122, 122))
                             .add(layout.createSequentialGroup()
                                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                     .add(jLabel2)
                                     .add(layout.createSequentialGroup()
                                         .add(jScrollPane3)
                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                         .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                 .add(11, 11, 11))))
                     .add(layout.createSequentialGroup()
                         .add(jLabel1)
                         .addContainerGap())))
             .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                     .add(layout.createSequentialGroup()
                         .add(257, 257, 257)
                         .add(jSeparator1))
                     .add(layout.createSequentialGroup()
                         .addContainerGap(828, Short.MAX_VALUE)
                         .add(btnClose)))
                 .add(14, 14, 14))
             .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                 .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                     .addContainerGap(799, Short.MAX_VALUE)
                     .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(16, 16, 16)))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jLabel1)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(layout.createSequentialGroup()
                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                             .add(jLabel4)
                             .add(txtName, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                             .add(jScrollPane2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                             .add(jLabel5))
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(jSeparator1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(jLabel2)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                             .add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                             .add(layout.createSequentialGroup()
                                 .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                 .add(0, 200, Short.MAX_VALUE))))
                     .add(jScrollPane1))
                 .add(22, 22, 22)
                 .add(btnClose)
                 .add(14, 14, 14))
             .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                 .add(layout.createSequentialGroup()
                     .add(38, 38, 38)
                     .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .addContainerGap(393, Short.MAX_VALUE)))
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
         dispose();
     }//GEN-LAST:event_btnCloseActionPerformed
 
     private void btnAssociateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAssociateActionPerformed
         MonitorSelectionDialog dialog = new MonitorSelectionDialog();
 
         List<String> excludes = new ArrayList<String>();
         for (int i = 0; i < tableModel.getRowCount(); i++) {
             excludes.add(String.valueOf(tableModel.getValueAt(i, 0)));
         }
 
         dialog.showDialog(excludes);
 
         if(CollectionUtils.isNotEmpty(dialog.getSelection())) {
             for(NewContextMonitorModel monitor : dialog.getSelection()) {
                 addMonitor(monitor);
             }
         }
 
         refreshButtons();
     }//GEN-LAST:event_btnAssociateActionPerformed
 
     private void btnDisassociateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDisassociateActionPerformed
         if(ArrayUtils.isNotEmpty(jtable.getSelectedRows())) {
            List<String> removedMonitors = new ArrayList<String>();
             for(int index : jtable.getSelectedRows()) {
                removedMonitors.add(String.valueOf(tableModel.getValueAt(index, 0)));
            }

            for(String monitorName : removedMonitors) {
                removeAssociatedMonitor(monitorName);
             }
         }
 
         refreshButtons();
     }//GEN-LAST:event_btnDisassociateActionPerformed
 
    private void removeAssociatedMonitor(String name) {
        for(int i = 0; i < tableModel.getRowCount(); i++) {
            if(StringUtils.equals(name, String.valueOf(tableModel.getValueAt(i, 0)))) {
                tableModel.removeRow(i);
                return;
            }
        }
    }

     private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
         ProfileModel model = new ProfileModel();
 
         model.setName(txtName.getText());
         model.setDesc(txtDesc.getText());
         model.setMonitors(new HashSet<String>());
 
         for (int i = 0; i < tableModel.getRowCount(); i++) {
             model.getMonitors().add(String.valueOf(tableModel.getValueAt(i, 0)));
         }
 
         try {
             ModelManagers.PROFILES.save(model);
             refresh();
         } catch (IOException e) {
             ErrorManager.getDefault().notify(e);
         }
     }//GEN-LAST:event_btnSaveActionPerformed
 
     private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed
         try {
             ModelManagers.PROFILES.remove(txtName.getText());
             refresh();
         } catch (IOException e) {
             ErrorManager.getDefault().notify(e);
         }
     }//GEN-LAST:event_btnRemoveActionPerformed
 
     private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
         txtName.setText("");
         txtDesc.setText("");
         tableModel.setRowCount(0);
 
         // ensure that there are no selected monitors on clear.
         lstProfiles.getSelectionModel().clearSelection();
         txtName.requestFocus();
     }//GEN-LAST:event_btnClearActionPerformed
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnAssociate;
     private javax.swing.JButton btnClear;
     private javax.swing.JButton btnClose;
     private javax.swing.JButton btnDisassociate;
     private javax.swing.JButton btnRemove;
     private javax.swing.JButton btnSave;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JScrollPane jScrollPane3;
     private javax.swing.JSeparator jSeparator1;
     private javax.swing.JTable jtable;
     private javax.swing.JList lstProfiles;
     private javax.swing.JTextArea txtDesc;
     private javax.swing.JTextField txtName;
     // End of variables declaration//GEN-END:variables
 }
