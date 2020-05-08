 /*
  * $Id$
  *
  * Copyright 2012 Valentyn Kolesnikov
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.github.searchfilebytemplate;
 
 import java.beans.XMLDecoder;
 import java.beans.XMLEncoder;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.io.Writer;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.DefaultListModel;
 import javax.swing.JCheckBox;
 import javax.swing.JFileChooser;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 public class SearchFileByTemplate extends javax.swing.JFrame {
     private final class TemplateFilenameFilter implements FilenameFilter {
         public boolean accept(File dir, String name) {
             File file = new File(dir + "/" + name);
             boolean nameCheck = name.toLowerCase().matches(
                     jTextField2.getText().toLowerCase()
                         .replaceAll("\\.", "\\.").replaceAll("\\?", ".")
                         .replaceAll("\\*", ".*?")
                     );
             long fileLength = file.length();
             boolean minCheck = true;
             if (jCheckBox2.isSelected() && jTextField4.getText().matches("\\d+")) {
                 minCheck = fileLength >= Integer.valueOf(jTextField4.getText());
             }
             boolean maxCheck = true;
             if (jCheckBox1.isSelected() && jTextField5.getText().matches("\\d+")) {
                 maxCheck = fileLength <= Integer.valueOf(jTextField5.getText());
             }
             boolean equalCheck = true;
             if (jCheckBox3.isSelected() && jTextField6.getText().matches("\\d+")) {
                 equalCheck = fileLength == Integer.valueOf(jTextField6.getText());
             }
             boolean systemCheck = true;
             if (jCheckBox6.isSelected()) {
                 systemCheck = !file.canWrite();
             }
             return file.isDirectory() || (nameCheck && minCheck && maxCheck && equalCheck && systemCheck);
         }
     }
     private static final int BUFFER_SIZE = 100000;
 
     private List<DefaultListModel> models = new ArrayList<DefaultListModel>();
     private Date timeStart;
     private Thread searchThread;
 
     /** Creates new form Find. */
     public SearchFileByTemplate() {
         setLookAndFeel();
         initComponents();
         try {
             XMLDecoder xmlDecoder = new XMLDecoder(new FileInputStream("SearchFileByTemplate.xml"));
             jTextField1.setText((String) xmlDecoder.readObject());
             jButton1.setEnabled(!jTextField1.getText().isEmpty());
             jTextField2.setText((String) xmlDecoder.readObject());
             jTextField3.setText((String) xmlDecoder.readObject());
             jTextField4.setText((String) xmlDecoder.readObject());
             jTextField5.setText((String) xmlDecoder.readObject());
             jTextField6.setText((String) xmlDecoder.readObject());
             jCheckBox1.setSelected((Boolean) xmlDecoder.readObject());
             jTextField5.setEnabled(jCheckBox1.isSelected());
             jCheckBox2.setSelected((Boolean) xmlDecoder.readObject());
             jTextField4.setEnabled(jCheckBox2.isSelected());
             jCheckBox3.setSelected((Boolean) xmlDecoder.readObject());
             jTextField6.setEnabled(jCheckBox3.isSelected());
             jCheckBox4.setSelected((Boolean) xmlDecoder.readObject());
             jCheckBox6.setSelected((Boolean) xmlDecoder.readObject());
             xmlDecoder.close();
         } catch (FileNotFoundException ex) {
             ex.getMessage();
         } catch (ClassCastException ex) {
             ex.getMessage();
         }
     }
 
     private static void setLookAndFeel() {
         javax.swing.UIManager.LookAndFeelInfo[] infos = UIManager.getInstalledLookAndFeels();
         String firstFoundClass = null;
         for (javax.swing.UIManager.LookAndFeelInfo info : infos) {
             String foundClass = info.getClassName();
            if ("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel".equals(foundClass)) {
                 firstFoundClass = foundClass;
                 break;
             }
             if (null == firstFoundClass) {
                 firstFoundClass = foundClass;
             }
         }
 
         if (null == firstFoundClass) {
             throw new IllegalArgumentException("No suitable Swing looks and feels");
         } else {
             try {
                 UIManager.setLookAndFeel(firstFoundClass);
             } catch (ClassNotFoundException ex) {
                 Logger.getLogger(SearchFileByTemplate.class.getName()).log(Level.SEVERE, null, ex);
             } catch (InstantiationException ex) {
                 Logger.getLogger(SearchFileByTemplate.class.getName()).log(Level.SEVERE, null, ex);
             } catch (IllegalAccessException ex) {
                 Logger.getLogger(SearchFileByTemplate.class.getName()).log(Level.SEVERE, null, ex);
             } catch (UnsupportedLookAndFeelException ex) {
                 Logger.getLogger(SearchFileByTemplate.class.getName()).log(Level.SEVERE, null, ex);
             }
             return;
         }
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jLabel1 = new javax.swing.JLabel();
         jTextField1 = new javax.swing.JTextField();
         jCheckBox4 = new javax.swing.JCheckBox();
         jButton1 = new javax.swing.JButton();
         jButton2 = new javax.swing.JButton();
         jLabel2 = new javax.swing.JLabel();
         jTextField2 = new javax.swing.JTextField();
         jLabel3 = new javax.swing.JLabel();
         jTextField3 = new javax.swing.JTextField();
         jPanel1 = new javax.swing.JPanel();
         jCheckBox1 = new javax.swing.JCheckBox();
         jCheckBox2 = new javax.swing.JCheckBox();
         jCheckBox3 = new javax.swing.JCheckBox();
         jTextField4 = new javax.swing.JTextField();
         jTextField5 = new javax.swing.JTextField();
         jTextField6 = new javax.swing.JTextField();
         jPanel2 = new javax.swing.JPanel();
         jCheckBox5 = new javax.swing.JCheckBox();
         jCheckBox6 = new javax.swing.JCheckBox();
         jCheckBox7 = new javax.swing.JCheckBox();
         jLabel4 = new javax.swing.JLabel();
         jLabel5 = new javax.swing.JLabel();
         jLabel6 = new javax.swing.JLabel();
         jLabel7 = new javax.swing.JLabel();
         jButton3 = new javax.swing.JButton();
         jButton4 = new javax.swing.JButton();
         jLabel8 = new javax.swing.JLabel();
         jLabel9 = new javax.swing.JLabel();
         jTabbedPane1 = new javax.swing.JTabbedPane();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle("SearchFileByTemplate, 1.01");
 
         jLabel1.setText("Start directory:");
 
         jTextField1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jTextField1ActionPerformed(evt);
             }
         });
         jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyTyped(java.awt.event.KeyEvent evt) {
                 jTextField1KeyTyped(evt);
             }
         });
 
         jCheckBox4.setText("Search in subdirectories");
         jCheckBox4.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
         jCheckBox4.setMargin(new java.awt.Insets(0, 0, 0, 0));
 
         jButton1.setText("Find");
         jButton1.setEnabled(false);
         jButton1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton1ActionPerformed(evt);
             }
         });
 
         jButton2.setText("Cancel");
         jButton2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton2ActionPerformed(evt);
             }
         });
 
         jLabel2.setText("File mask:");
 
         jTextField2.setText("*.*");
 
         jLabel3.setText("Search text:");
 
         jPanel1.setBackground(new java.awt.Color(239, 239, 239));
         jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Size conditions"));
 
         jCheckBox1.setText("Max");
         jCheckBox1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
         jCheckBox1.setMargin(new java.awt.Insets(0, 0, 0, 0));
         jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jCheckBox1ActionPerformed(evt);
             }
         });
 
         jCheckBox2.setText("Min");
         jCheckBox2.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
         jCheckBox2.setMargin(new java.awt.Insets(0, 0, 0, 0));
         jCheckBox2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jCheckBox2ActionPerformed(evt);
             }
         });
 
         jCheckBox3.setText("Exact");
         jCheckBox3.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
         jCheckBox3.setMargin(new java.awt.Insets(0, 0, 0, 0));
         jCheckBox3.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jCheckBox3ActionPerformed(evt);
             }
         });
 
         jTextField4.setEnabled(false);
         jTextField4.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jTextField4ActionPerformed(evt);
             }
         });
 
         jTextField5.setEnabled(false);
         jTextField5.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jTextField5ActionPerformed(evt);
             }
         });
 
         jTextField6.setEnabled(false);
         jTextField6.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jTextField6ActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(jCheckBox1)
                     .add(jCheckBox2)
                     .add(jCheckBox3))
                 .add(18, 18, 18)
                 .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(jTextField6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                     .add(jTextField4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                     .add(jTextField5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE))
                 .addContainerGap())
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel1Layout.createSequentialGroup()
                 .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jTextField4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(jCheckBox2))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jCheckBox1)
                     .add(jTextField5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jCheckBox3)
                     .add(jTextField6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(11, Short.MAX_VALUE))
         );
 
         jPanel2.setBackground(new java.awt.Color(239, 239, 239));
         jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("File attributies"));
 
         jCheckBox5.setText("Archive");
         jCheckBox5.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
         jCheckBox5.setEnabled(false);
         jCheckBox5.setMargin(new java.awt.Insets(0, 0, 0, 0));
         jCheckBox5.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jCheckBox5ActionPerformed(evt);
             }
         });
 
         jCheckBox6.setText("System");
         jCheckBox6.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
         jCheckBox6.setMargin(new java.awt.Insets(0, 0, 0, 0));
 
         jCheckBox7.setText("Hidden");
         jCheckBox7.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
         jCheckBox7.setEnabled(false);
         jCheckBox7.setMargin(new java.awt.Insets(0, 0, 0, 0));
 
         org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
         jPanel2.setLayout(jPanel2Layout);
         jPanel2Layout.setHorizontalGroup(
             jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel2Layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(jCheckBox6)
                     .add(jCheckBox5)
                     .add(jCheckBox7))
                 .addContainerGap(285, Short.MAX_VALUE))
         );
         jPanel2Layout.setVerticalGroup(
             jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel2Layout.createSequentialGroup()
                 .add(jCheckBox6)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(jCheckBox5)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(jCheckBox7)
                 .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         jLabel4.setText("Files found:");
 
         jLabel5.setText("0");
 
         jLabel6.setText("Search time:");
 
         jLabel7.setText("00:00");
 
         jButton3.setText("Save search results");
         jButton3.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton3ActionPerformed(evt);
             }
         });
 
         jButton4.setText("Save settings");
         jButton4.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton4ActionPerformed(evt);
             }
         });
 
         jLabel8.setText("Current file:");
 
         jLabel9.setText(" ");
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(layout.createSequentialGroup()
                         .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 851, Short.MAX_VALUE)
                         .addContainerGap())
                     .add(layout.createSequentialGroup()
                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                             .add(layout.createSequentialGroup()
                                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                     .add(jLabel1)
                                     .add(jLabel2))
                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                     .add(layout.createSequentialGroup()
                                         .add(jTextField1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 543, Short.MAX_VALUE)
                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                         .add(jCheckBox4)
                                         .add(12, 12, 12))
                                     .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                         .add(jButton3)
                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                         .add(jButton4)
                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 290, Short.MAX_VALUE)
                                         .add(jLabel4)
                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                         .add(jLabel5)
                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                         .add(jLabel6)
                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                         .add(jLabel7))
                                     .add(org.jdesktop.layout.GroupLayout.LEADING, jTextField3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 692, Short.MAX_VALUE)
                                     .add(org.jdesktop.layout.GroupLayout.LEADING, jTextField2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 692, Short.MAX_VALUE)
                                     .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                         .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                         .add(jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                             .add(jLabel3))
                         .add(16, 16, 16)
                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                             .add(jButton1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .add(jButton2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                         .addContainerGap())
                     .add(layout.createSequentialGroup()
                         .add(jLabel8)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(jLabel9, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 706, Short.MAX_VALUE)
                         .add(91, 91, 91))))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabel1)
                     .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(jCheckBox4)
                     .add(jButton1))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabel2)
                     .add(jTextField2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(jButton2))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabel3)
                     .add(jTextField3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .add(18, 18, 18)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                         .add(jButton3)
                         .add(jButton4))
                     .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                         .add(jLabel4)
                         .add(jLabel5)
                         .add(jLabel6)
                         .add(jLabel7)))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabel9)
                     .add(jLabel8))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
         jTextField5.setEnabled(((JCheckBox) evt.getSource()).isSelected());
     }//GEN-LAST:event_jCheckBox1ActionPerformed
 
     private void jTextField4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField4ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_jTextField4ActionPerformed
 
     private void jTextField5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField5ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_jTextField5ActionPerformed
 
     private void jTextField6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField6ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_jTextField6ActionPerformed
 
     private void jCheckBox5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox5ActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_jCheckBox5ActionPerformed
 
     private void jCheckBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox2ActionPerformed
        jTextField4.setEnabled(((JCheckBox) evt.getSource()).isSelected());
     }//GEN-LAST:event_jCheckBox2ActionPerformed
 
     private void jCheckBox3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox3ActionPerformed
         jTextField6.setEnabled(((JCheckBox) evt.getSource()).isSelected());
     }//GEN-LAST:event_jCheckBox3ActionPerformed
 
     private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
         if (jButton1.getText().equals("Stop")) {
             searchThread.interrupt();
             jButton1.setText("Find");
         } else {
             models.add(new DefaultListModel());
             javax.swing.JList jList = new javax.swing.JList();
             jList.setModel(models.get(models.size() - 1));
             JScrollPane jScrollPane = new javax.swing.JScrollPane();
             jScrollPane.setViewportView(jList);
             String tabHeader = "Search " + models.size() + " ("
                     + jTextField1.getText() + ", "
                     + jTextField2.getText() + ", "
                     + jTextField3.getText() + ")";
             jTabbedPane1.addTab(tabHeader, jScrollPane);
             jTabbedPane1.setSelectedIndex(models.size() - 1);
             models.get(models.size() - 1).clear();
             jLabel5.setText("0");
             jLabel9.setText("");
             timeStart = new Date();
             jButton1.setText("Stop");
             searchThread = new Thread() {
                 @Override
                 public void run() {
                     findFiles(new File(jTextField1.getText()));
                     jButton1.setText("Find");
                 }
             };
         searchThread.start();
         }
     }//GEN-LAST:event_jButton1ActionPerformed
 
     private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
         
     }//GEN-LAST:event_jTextField1ActionPerformed
 
     private void jTextField1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyTyped
         jButton1.setEnabled(!((JTextField) evt.getSource()).getText().isEmpty());
     }//GEN-LAST:event_jTextField1KeyTyped
 
     private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
         JFileChooser chooser1 = new JFileChooser();
         int result = chooser1.showSaveDialog(this);
         if (result == JFileChooser.APPROVE_OPTION) {
             writeDataFile(chooser1.getSelectedFile());
         }
     }//GEN-LAST:event_jButton3ActionPerformed
 
     private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
         try {
             XMLEncoder xmlEncoder = new XMLEncoder(new FileOutputStream("SearchFileByTemplate.xml"));
             xmlEncoder.writeObject(jTextField1.getText());
             xmlEncoder.writeObject(jTextField2.getText());
             xmlEncoder.writeObject(jTextField3.getText());
             xmlEncoder.writeObject(jTextField4.getText());
             xmlEncoder.writeObject(jTextField5.getText());
             xmlEncoder.writeObject(jTextField6.getText());
             xmlEncoder.writeObject(jCheckBox1.isSelected());
             xmlEncoder.writeObject(jCheckBox2.isSelected());
             xmlEncoder.writeObject(jCheckBox3.isSelected());
             xmlEncoder.writeObject(jCheckBox4.isSelected());
             xmlEncoder.writeObject(jCheckBox6.isSelected());
             xmlEncoder.close();
         } catch (FileNotFoundException ex) {
             Logger.getLogger(SearchFileByTemplate.class.getName()).log(Level.SEVERE, null, ex);
         }
     }//GEN-LAST:event_jButton4ActionPerformed
 
     private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
         System.exit(0);
     }//GEN-LAST:event_jButton2ActionPerformed
 
     private void writeDataFile(File file) {
         Writer writer = null;
         try {
             writer = new BufferedWriter(new FileWriter(file));
             if (!models.get(jTabbedPane1.getSelectedIndex()).isEmpty()) {
                 writer.write(models.get(jTabbedPane1.getSelectedIndex()).get(0).toString());
             }
             for (int index = 1; index < models.get(jTabbedPane1.getSelectedIndex()).size(); index += 1) {
                 writer.write("\n");
                 writer.write(models.get(jTabbedPane1.getSelectedIndex()).get(index).toString());
             }
             writer.close();
         } catch (IOException ex) {
             Logger.getLogger(SearchFileByTemplate.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
             if (writer != null) {
                 try {
                     writer.close();
                 } catch (IOException ex) {
                     Logger.getLogger(SearchFileByTemplate.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         }
     }
     private void findFiles(File dir) {
         File[] files = dir.listFiles(new TemplateFilenameFilter());
         if (files == null) {
             Logger.getLogger(SearchFileByTemplate.class.getName()).log(
                     Level.SEVERE, "Can't read " + dir.getAbsolutePath());
             return;
         }
         for (final File file : files) {
             if  (Thread.currentThread().isInterrupted()) {
                 return;
             }
             if (!file.isDirectory()) {
                 if (searchText(file)) {
                     SwingUtilities.invokeLater(new Runnable() {
                         public void run() {
                             models.get(models.size() - 1).add(0, file.getAbsolutePath());
                         }
                     });
                     jLabel5.setText(String.valueOf(
                             Integer.valueOf(jLabel5.getText()) + 1));
                 }
             } else {
                 if (jCheckBox4.isSelected()) {
                     findFiles(file);
                 }
             }
         }
     }
 
     private boolean searchText(File file) {
         jLabel9.setText(file.getAbsolutePath());
         long timeDiff = new Date().getTime() - timeStart.getTime();
         jLabel7.setText(new DecimalFormat("00").format(
                 timeDiff / 1000 / 60) + ":"
                 + new DecimalFormat("00").format((timeDiff / 1000) % 60));
         if (jTextField3.getText().isEmpty()) {
             return true;
         }
         RandomAccessFile accessFile = null;
         try {
             accessFile = new RandomAccessFile(file, "r");
             byte[] fileContent = new byte[BUFFER_SIZE];
             for (long position = 0; position < file.length();
                     position += BUFFER_SIZE - jTextField3.getText().length()) {
                 accessFile.seek(position);
                 accessFile.read(fileContent);
                 if (new String(fileContent).indexOf(jTextField3.getText()) >= 0) {
                         return true;
                 }
             }
         } catch (IOException ex) {
             Logger.getLogger(SearchFileByTemplate.class.getName()).log(Level.SEVERE, null, ex);
         }
         finally {
             try {
                 if (accessFile != null) {
                     accessFile.close();
                 }
             } catch (IOException ex) {
                 Logger.getLogger(SearchFileByTemplate.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         return false;
     }
 
     /**
      * main.
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 new SearchFileByTemplate().setVisible(true);
             }
         });
     }
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton jButton1;
     private javax.swing.JButton jButton2;
     private javax.swing.JButton jButton3;
     private javax.swing.JButton jButton4;
     private javax.swing.JCheckBox jCheckBox1;
     private javax.swing.JCheckBox jCheckBox2;
     private javax.swing.JCheckBox jCheckBox3;
     private javax.swing.JCheckBox jCheckBox4;
     private javax.swing.JCheckBox jCheckBox5;
     private javax.swing.JCheckBox jCheckBox6;
     private javax.swing.JCheckBox jCheckBox7;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JLabel jLabel8;
     private javax.swing.JLabel jLabel9;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JTabbedPane jTabbedPane1;
     private javax.swing.JTextField jTextField1;
     private javax.swing.JTextField jTextField2;
     private javax.swing.JTextField jTextField3;
     private javax.swing.JTextField jTextField4;
     private javax.swing.JTextField jTextField5;
     private javax.swing.JTextField jTextField6;
     // End of variables declaration//GEN-END:variables
 
 }
