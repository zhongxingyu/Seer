 /**
  * This file is part of FileControl application (check README).
  * Copyright (C) 2013  Stanislav Nepochatov
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 **/
 
 package filecontrol;
 
 /**
  * Main window main class;
  * @author Stanislav Nepochatov
  */
 public class ControlFrame extends javax.swing.JFrame {
 
     /**
      * Table model for <code>controlTable</code>;
      */
     public javax.swing.table.DefaultTableModel controlModel;
     
     /**
      * Normal icon for window;
      */
     public java.awt.Image appIconNormal = java.awt.Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/app_icon_normal.png"));
     
     /**
      * Error condition icon for window;
      */
     public java.awt.Image appIconError = java.awt.Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/app_icon_error.png"));
     
     /**
      * State button enabled icon;
      */
     private javax.swing.ImageIcon scanStateEnabled = new javax.swing.ImageIcon(getClass().getResource("/icons/scan_resume.png"));
     
     /**
      * State buutton disabled icon;
      */
     private javax.swing.ImageIcon scanStateDisabled = new javax.swing.ImageIcon(getClass().getResource("/icons/scan_pause.png"));
     
     /**
      * Control cell render class
      */
     private class ControlCellRender extends javax.swing.JLabel implements javax.swing.table.TableCellRenderer {
 
         @Override
         public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
             if (value != null) setText(value.toString());
             if (column == 0) {
                 if (isSelected) {
                     setBackground(ControlQuene.getEntry(row).EntryColor.brighter());
                 } else {
                     setBackground(ControlQuene.getEntry(row).EntryColor);
                 }
             } else {
                 if(isSelected) {
                     setBackground(table.getSelectionBackground());
                     setForeground(table.getSelectionForeground());
                 } else {
                     setBackground(table.getBackground());
                     setForeground(table.getForeground());
                 }
             }
             setOpaque(true);
             return this;
         }
     }
     
     /**
      * Creates new form ControlFrame
      */
     public ControlFrame() {
         initComponents();
         this.controlTable.getSelectionModel().addListSelectionListener(new javax.swing.event.ListSelectionListener() {
             @Override
             public void valueChanged(javax.swing.event.ListSelectionEvent e) {
                 if (e.getFirstIndex() != -1) {
                     FileControl.MainWindow.editBut.setEnabled(true);
                     FileControl.MainWindow.removeBut.setEnabled(true);
                 } else {
                     FileControl.MainWindow.editBut.setEnabled(false);
                     FileControl.MainWindow.removeBut.setEnabled(false);
                 }
             }
         });
     }
     
     /**
      * Set window properties before appearing;
      */
     public void postInit() {
         setTimerIndex(Integer.parseInt(FileControl.MainProperties.getProperty("scan_timer_index")));
         setLocation(Integer.parseInt(FileControl.MainProperties.getProperty("window_pos_x")), Integer.parseInt(FileControl.MainProperties.getProperty("window_pos_y")));
         FileControl.MainWindow.buildControlTable();
     }
     
     /**
      * Get value of timer combobox
      * @return Integer value of timer;
      */
     public Integer getTimerValue() {
         return Integer.parseInt(timerBox.getSelectedItem().toString());
     }
     
     /**
      * Set value of timer box;
      * @param givenIndex index from properties;
      */
     public void setTimerIndex(Integer givenIndex) {
         this.timerBox.setSelectedIndex(givenIndex);
     }
     
     /**
      * Add message to gui list
      * @param message massage's string
      */
     public synchronized void addToLog(String message) {
         this.logViewPane.setText(
                 this.logViewPane.getText()  + message + "\n");
         this.logViewPane.setCaretPosition(this.logViewPane.getText().length());
     }
     
     /**
      * Notify user with sound alarm and icon changing;
      */
     public void userNotify() {
         if (FileControl.notifyFlag) {
             if (FileControl.MainProperties.getProperty("err_play_sound").equals("1")) {
                 try {
                     javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
                    clip.open(javax.sound.sampled.AudioSystem.getAudioInputStream(getClass().getResourceAsStream("/sounds/Error.wav")));
                     clip.start();
                 } catch (Exception ex) {
                     ex.printStackTrace(System.out);
                 }
             }
             this.setIconImage(appIconError);
         } else {
             this.setIconImage(appIconNormal);
         }
     }
     
     /**
      * Build control table with current control states;
      */
     public void buildControlTable() {
         controlModel = new javax.swing.table.DefaultTableModel(
                 new Object [][] {},
                 new String [] {"Имя записи", "Путь", "Маска", "Статус"}
             ) {
                 Class[] types = new Class [] {
                     java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
                 };
                 boolean[] canEdit = new boolean [] {
                     false, false, false, false
                 };
 
                 @Override
                 public Class getColumnClass(int columnIndex) {
                     return types [columnIndex];
                 }
 
                 @Override
                 public boolean isCellEditable(int rowIndex, int columnIndex) {
                     return canEdit [columnIndex];
                 }
             };
         ControlQuene.fillControlTable(controlModel);
         this.controlTable.setModel(controlModel);
         controlTable.getColumnModel().getColumn(0).setCellRenderer(new ControlCellRender());
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
         jTable1 = new javax.swing.JTable();
         jInternalFrame1 = new javax.swing.JInternalFrame();
         addBut = new javax.swing.JButton();
         editBut = new javax.swing.JButton();
         removeBut = new javax.swing.JButton();
         timerLabel = new javax.swing.JLabel();
         timerBox = new javax.swing.JComboBox();
         refreshBut = new javax.swing.JButton();
         jScrollPane3 = new javax.swing.JScrollPane();
         controlTable = new javax.swing.JTable();
         scanState = new javax.swing.JToggleButton();
         jScrollPane1 = new javax.swing.JScrollPane();
         logViewPane = new javax.swing.JTextPane();
         jButton1 = new javax.swing.JButton();
 
         jTable1.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
         jScrollPane2.setViewportView(jTable1);
 
         jInternalFrame1.setVisible(true);
 
         javax.swing.GroupLayout jInternalFrame1Layout = new javax.swing.GroupLayout(jInternalFrame1.getContentPane());
         jInternalFrame1.getContentPane().setLayout(jInternalFrame1Layout);
         jInternalFrame1Layout.setHorizontalGroup(
             jInternalFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 0, Short.MAX_VALUE)
         );
         jInternalFrame1Layout.setVerticalGroup(
             jInternalFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 0, Short.MAX_VALUE)
         );
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle("Контроль файлов");
         setIconImage(appIconNormal);
         addWindowListener(new java.awt.event.WindowAdapter() {
             public void windowClosing(java.awt.event.WindowEvent evt) {
                 formWindowClosing(evt);
             }
         });
 
         addBut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/entry_add.png"))); // NOI18N
         addBut.setText("Добавить");
         addBut.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 addButActionPerformed(evt);
             }
         });
 
         editBut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/entry_edit.png"))); // NOI18N
         editBut.setText("Редактировать");
         editBut.setEnabled(false);
         editBut.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 editButActionPerformed(evt);
             }
         });
 
         removeBut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/entry_remove.png"))); // NOI18N
         removeBut.setText("Удалить");
         removeBut.setEnabled(false);
         removeBut.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 removeButActionPerformed(evt);
             }
         });
 
         timerLabel.setText("Интервал (мин.)");
 
         timerBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1", "3", "5", "10", "15", "45" }));
         timerBox.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 timerBoxItemStateChanged(evt);
             }
         });
 
         refreshBut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/refresh.png"))); // NOI18N
         refreshBut.setText("Обновить");
         refreshBut.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 refreshButActionPerformed(evt);
             }
         });
 
         jScrollPane3.setViewportView(controlTable);
 
         scanState.setIcon(this.scanStateDisabled);
         scanState.setSelected(true);
         scanState.setText("Остановить");
         scanState.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 scanStateActionPerformed(evt);
             }
         });
 
         logViewPane.setEditable(false);
         jScrollPane1.setViewportView(logViewPane);
 
         jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/settings.png"))); // NOI18N
         jButton1.setText("Настройки");
         jButton1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton1ActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane1)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 506, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                             .addComponent(editBut, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(addBut, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(removeBut, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(timerLabel)
                             .addComponent(timerBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(refreshBut, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(scanState, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(jButton1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(addBut)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(editBut)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(removeBut)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(timerLabel)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(timerBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(refreshBut)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(scanState)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(jButton1))
                     .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 142, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void refreshButActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButActionPerformed
         ControlQuene.refreshScan();
     }//GEN-LAST:event_refreshButActionPerformed
 
     private void addButActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButActionPerformed
         EntryDialog addDialog = new EntryDialog(this, true, null);
         addDialog.setVisible(true);
     }//GEN-LAST:event_addButActionPerformed
 
     private void editButActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButActionPerformed
         EntryDialog editDialog = new EntryDialog(this, true, ControlQuene.getEntry(this.controlTable.getSelectedRow()));
         editDialog.setVisible(true);
     }//GEN-LAST:event_editButActionPerformed
 
     private void timerBoxItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_timerBoxItemStateChanged
         ControlQuene.setScanTimer();
     }//GEN-LAST:event_timerBoxItemStateChanged
 
     private void scanStateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scanStateActionPerformed
         if (this.scanState.isSelected()) {
             this.scanState.setText("Остановить");
             this.scanState.setIcon(scanStateDisabled);
             ControlQuene.renewScan();
         } else {
             this.scanState.setText("Запустить");
             this.scanState.setIcon(scanStateEnabled);
             ControlQuene.stopScan();
         }
     }//GEN-LAST:event_scanStateActionPerformed
 
     private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
         SettingsDialog scanDialog = new SettingsDialog(this, true);
         scanDialog.setVisible(true);
     }//GEN-LAST:event_jButton1ActionPerformed
 
     private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
         FileControl.log(2, "Завершение работы программы.");
         FileControl.MainProperties.setProperty("window_pos_x", String.valueOf(this.getX()));
         FileControl.MainProperties.setProperty("window_pos_y", String.valueOf(this.getY()));
         FileControl.MainProperties.setProperty("scan_timer_index", String.valueOf(this.timerBox.getSelectedIndex()));
         FileControl.storeProperties();
     }//GEN-LAST:event_formWindowClosing
 
     private void removeButActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButActionPerformed
         Object[] options = {"Да", "Нет"};
         Integer result = javax.swing.JOptionPane.showOptionDialog(this,
             "Удалить данную запись '" + ControlQuene.getEntry(controlTable.getSelectedRow()).EntryName + "' из списка проверок?",
             "Вопрос",
             javax.swing.JOptionPane.YES_NO_CANCEL_OPTION,
             javax.swing.JOptionPane.QUESTION_MESSAGE,
             null,
             options,
             options[1]);
         if (result == 0) {
             ControlQuene.removeFromQuene(controlTable.getSelectedRow());
             buildControlTable();
         }
     }//GEN-LAST:event_removeButActionPerformed
 
     /**
      * @param args the command line arguments
      */
     public static void main(String args[]) {
         /*
          * Set the Nimbus look and feel
          */
         //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
         /*
          * If Nimbus (introduced in Java SE 6) is not available, stay with the
          * default look and feel. For details see
          * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
          */
         try {
             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(ControlFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(ControlFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(ControlFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(ControlFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
 
         /*
          * Create and display the form
          */
         java.awt.EventQueue.invokeLater(new Runnable() {
 
             public void run() {
                 new ControlFrame().setVisible(true);
             }
         });
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton addBut;
     public javax.swing.JTable controlTable;
     public javax.swing.JButton editBut;
     private javax.swing.JButton jButton1;
     private javax.swing.JInternalFrame jInternalFrame1;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JScrollPane jScrollPane3;
     private javax.swing.JTable jTable1;
     private javax.swing.JTextPane logViewPane;
     private javax.swing.JButton refreshBut;
     public javax.swing.JButton removeBut;
     private javax.swing.JToggleButton scanState;
     private javax.swing.JComboBox timerBox;
     private javax.swing.JLabel timerLabel;
     // End of variables declaration//GEN-END:variables
 }
