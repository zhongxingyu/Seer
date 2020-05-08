 /*
  * PreferencePane.java
  *
  * Created on Jun 29, 2009, 5:22:54 PM
 
  * This file is a part of Shoddy Battle.
  * Copyright (C) 2009  Catherine Fitzpatrick and Benjamin Gwin
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Affero General Public License
  * as published by the Free Software Foundation; either version 3
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program; if not, visit the Free Software Foundation, Inc.
  * online at http://gnu.org.
  */
 
 package shoddybattleclient;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.ListModel;
 import shoddybattleclient.Preference.HealthDisplay;
 import shoddybattleclient.Preference.LogOption;
 import shoddybattleclient.utils.SwingWorker;
 import shoddybattleclient.utils.Text;
 import shoddybattleclient.utils.bzip2.CBZip2InputStream;
 import shoddybattleclient.utils.tar.TarEntry;
 import shoddybattleclient.utils.tar.TarInputStream;
 
 /**
  *
  * @author ben
  */
 public class PreferencePane extends javax.swing.JFrame {
 
     private class SpritePackage {
         private String m_name;
         private String m_dir;
         private int m_order = 999;
         public SpritePackage(String name, String dir) {
             m_name = name;
             m_dir = dir;
         }
         public String getDir() { return m_dir; }
         @Override
         public String toString() { return m_name; }
     }
 
     enum SpriteLink {
         NA ("N/A", ""),
         DP ("DP", "http://shoddybattle.com/sprites/dp.tar.bz2"),
         PLATINUM ("Platinum", "http://shoddybattle.com/sprites/platinum.tar.bz2");
         private String m_str;
         private String m_url;
         SpriteLink(String str, String url) {
             m_str = str;
             m_url = url;
         }
         @Override
         public String toString() {
             return m_str;
         }
     }
 
     private class SpriteDownloader extends SwingWorker<Void, Integer> {
         private InputStream m_is;
         private int m_max;
         private int m_total = 0;
         public SpriteDownloader(InputStream is, int max) {
             m_is = is;
             m_max = max;
         }
 
         @Override
         protected Void doInBackground() {
             try {
                 try {
                     //discard first two bytes to make the bzip library work
                     m_is.read();
                     m_is.read();
                     TarInputStream tar = new TarInputStream(new CBZip2InputStream(m_is));
                     TarEntry entry;
                     while ((entry = tar.getNextEntry()) != null) {
                         File file = new File(new File(Preference.getSpriteLocation()), entry.getName());
                         if (file.exists()) {
                             JOptionPane.showMessageDialog(PreferencePane.this,
                                     "A package with this name is already installed.");
                             m_is.close();
                             tar.close();
                             return null;
                         }
                         if (entry.isDirectory()) {
                             file.mkdirs();
                         } else {
                             file.createNewFile();
                             FileOutputStream out = new FileOutputStream(file);
                             byte[] bytes = new byte[512];
                             int length;
                             while ((length = tar.read(bytes)) != -1) {
                                 out.write(bytes, 0, length);
                                 m_total += length;
                                 int progress = (int)(100.0 * m_total / m_max);
                                 if (progress > 100) progress = 100;
                                 setProgress(progress);
                             }
                             out.flush();
                             out.close();
                         }
                     }
                     tar.close();
                 } finally {
                     m_is.close();
                 }
             } catch (IOException e) {
                 e.printStackTrace();
             }
             return null;
         }
 
         @Override
         public void done() {
             PreferencePane.this.initSpritePanel();
         }
 
     }
 
     /** Creates new form PreferencePane */
     public PreferencePane() {
         initComponents();
         initChatPanel();
         initBattlePanel();
         initSpritePanel();
     }
 
     private void initChatPanel() {
         chkTimestamps.setSelected(Preference.timeStampsEnabled());
         txtTimestampFormat.setText(Preference.getTimeStampFormat());
         txtTimestampFormat.setEnabled(chkTimestamps.isSelected());
 
         txtIgnored.setText(Preference.getIgnoredUsersStr());
 
         txtLogDir.setText(Preference.getLogDirectory());
         chkSaveChatLogs.setSelected(Preference.getAutosaveChatLogs());
     }
 
     private void initBattlePanel() {
         chkAnimateHealth.setSelected(Preference.animateHealthBars());
 
         cmbUserHealth.setModel(new DefaultComboBoxModel(Preference.HealthDisplay.values()));
         cmbUserHealth.setSelectedItem(Preference.getHealthDisplay(true));
         cmbOppHealth.setModel(new DefaultComboBoxModel(Preference.HealthDisplay.values()));
         cmbOppHealth.setSelectedItem(Preference.getHealthDisplay(false));
 
         cmbBattleLogOption.setModel(new DefaultComboBoxModel(LogOption.values()));
         cmbBattleLogOption.setSelectedItem(Preference.getBattleLogOption());
     }
 
     private void initSpritePanel() {
         File path = new File(Preference.getSpriteLocation());
         path.mkdirs();
         List<SpritePackage> packages = new ArrayList<SpritePackage>();
         File[] dirs = path.listFiles();
         for (int i = 0; i < dirs.length; i++) {
             File dir = dirs[i];
             if (!dir.isDirectory()) continue;
             String d = dir.getName();
             String name = d;
             File f = new File(dir, "info.txt");
             if (f.exists()) {
                 try {
                     BufferedReader reader = new BufferedReader(new FileReader(f));
                     String line;
                     while ((line = reader.readLine()) != null) {
                         line = line.trim();
                         if (line.startsWith("name")) {
                             name = line.split("=")[1].trim();
                         }
                     }
                    reader.close();
                 } catch (Exception e) {
 
                 }
             }
             SpritePackage pack = new SpritePackage(name, d);
             packages.add(pack);
         }
         String[] packs = Preference.getSpriteDirectories();
         for (int i = 0; i < packs.length; i++) {
             for (SpritePackage p : packages) {
                 if (p.getDir().equals(packs[i])) p.m_order = i;
             }
         }
         Collections.sort(packages, new Comparator<SpritePackage>() {
             public int compare(SpritePackage o1, SpritePackage o2) {
                 return new Integer(o1.m_order).compareTo(new Integer(o2.m_order));
             }
         });
         lstPackages.setModel(
                 new DefaultComboBoxModel(packages.toArray(new SpritePackage[packages.size()])));
         cmbSpriteDefaults.setModel(new DefaultComboBoxModel(SpriteLink.values()));
     }
 
     private void saveSpriteDirectories() {
         ListModel m = lstPackages.getModel();
         String[] dirs = new String[m.getSize()];
         for (int i = 0; i < m.getSize(); i++) {
             dirs[i] = ((SpritePackage)m.getElementAt(i)).getDir();
         }
         Preference.setSpriteDirectories(dirs);
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jTabbedPane1 = new javax.swing.JTabbedPane();
         jPanel1 = new javax.swing.JPanel();
         chkTimestamps = new javax.swing.JCheckBox();
         jLabel1 = new javax.swing.JLabel();
         txtTimestampFormat = new javax.swing.JTextField();
         lblTimestampInfo = new javax.swing.JLabel();
         jLabel2 = new javax.swing.JLabel();
         txtIgnored = new javax.swing.JTextField();
         jLabel3 = new javax.swing.JLabel();
         jLabel10 = new javax.swing.JLabel();
         txtLogDir = new javax.swing.JTextField();
         btnBrowseLogDir = new javax.swing.JButton();
         chkSaveChatLogs = new javax.swing.JCheckBox();
         jPanel2 = new javax.swing.JPanel();
         jLabel4 = new javax.swing.JLabel();
         jLabel5 = new javax.swing.JLabel();
         cmbUserHealth = new javax.swing.JComboBox();
         cmbOppHealth = new javax.swing.JComboBox();
         lblUserHealth = new javax.swing.JLabel();
         lblEnemyHealth = new javax.swing.JLabel();
         chkAnimateHealth = new javax.swing.JCheckBox();
         jLabel11 = new javax.swing.JLabel();
         cmbBattleLogOption = new javax.swing.JComboBox();
         jPanel3 = new javax.swing.JPanel();
         jScrollPane1 = new javax.swing.JScrollPane();
         lstPackages = new javax.swing.JList();
         jLabel6 = new javax.swing.JLabel();
         btnUp = new javax.swing.JButton();
         btnDown = new javax.swing.JButton();
         btnDelete = new javax.swing.JButton();
         jPanel4 = new javax.swing.JPanel();
         jLabel7 = new javax.swing.JLabel();
         cmbSpriteDefaults = new javax.swing.JComboBox();
         jLabel8 = new javax.swing.JLabel();
         jLabel9 = new javax.swing.JLabel();
         txtURL = new javax.swing.JTextField();
         btnAdd = new javax.swing.JButton();
         txtProgress = new javax.swing.JLabel();
         progress = new javax.swing.JProgressBar();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
         setTitle("Preferences");
 
         jPanel1.setOpaque(false);
 
         chkTimestamps.setText("Enable Timestamps");
         chkTimestamps.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 chkTimestampsActionPerformed(evt);
             }
         });
 
         jLabel1.setText("Timestamp Format:");
 
         txtTimestampFormat.setText("[h:m:s]");
         txtTimestampFormat.addCaretListener(new javax.swing.event.CaretListener() {
             public void caretUpdate(javax.swing.event.CaretEvent evt) {
                 txtTimestampFormatCaretUpdate(evt);
             }
         });
 
         lblTimestampInfo.setFont(new java.awt.Font("Lucida Grande", 1, 10));
         lblTimestampInfo.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
 
         jLabel2.setText("Ignored Users:");
 
         txtIgnored.addCaretListener(new javax.swing.event.CaretListener() {
             public void caretUpdate(javax.swing.event.CaretEvent evt) {
                 txtIgnoredCaretUpdate(evt);
             }
         });
 
         jLabel3.setFont(new java.awt.Font("Lucida Grande", 1, 10));
         jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
         jLabel3.setText("Type user names separated by commas");
 
         jLabel10.setText("Log Directory:");
 
         txtLogDir.addCaretListener(new javax.swing.event.CaretListener() {
             public void caretUpdate(javax.swing.event.CaretEvent evt) {
                 txtLogDirCaretUpdate(evt);
             }
         });
 
         btnBrowseLogDir.setText("Browse");
         btnBrowseLogDir.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnBrowseLogDirActionPerformed(evt);
             }
         });
 
         chkSaveChatLogs.setText("Automatically save chat logs?");
         chkSaveChatLogs.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 chkSaveChatLogsActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel1Layout.createSequentialGroup()
                 .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                         .add(6, 6, 6)
                         .add(lblTimestampInfo, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 477, Short.MAX_VALUE))
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel1Layout.createSequentialGroup()
                         .add(chkTimestamps)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(jLabel1)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(txtTimestampFormat, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 270, Short.MAX_VALUE))
                     .add(jPanel1Layout.createSequentialGroup()
                         .addContainerGap()
                         .add(jLabel2)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(txtIgnored, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE))
                     .add(jPanel1Layout.createSequentialGroup()
                         .addContainerGap()
                         .add(jLabel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 473, Short.MAX_VALUE))
                     .add(jPanel1Layout.createSequentialGroup()
                         .addContainerGap()
                         .add(jLabel10)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(txtLogDir, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(btnBrowseLogDir))
                     .add(jPanel1Layout.createSequentialGroup()
                         .addContainerGap()
                         .add(chkSaveChatLogs)))
                 .addContainerGap())
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel1Layout.createSequentialGroup()
                 .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(chkTimestamps)
                     .add(jLabel1)
                     .add(txtTimestampFormat, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(lblTimestampInfo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabel2)
                     .add(txtIgnored, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jLabel3)
                 .add(26, 26, 26)
                 .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabel10)
                     .add(txtLogDir, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(btnBrowseLogDir))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(chkSaveChatLogs)
                 .addContainerGap(106, Short.MAX_VALUE))
         );
 
         jTabbedPane1.addTab("Chat", jPanel1);
 
         jPanel2.setOpaque(false);
 
         jLabel4.setText("Show my health changes as:");
 
         jLabel5.setText("Show enemy health changes as:");
 
         cmbUserHealth.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 cmbUserHealthItemStateChanged(evt);
             }
         });
 
         cmbOppHealth.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 cmbOppHealthItemStateChanged(evt);
             }
         });
 
         chkAnimateHealth.setText("Animate health bars?");
         chkAnimateHealth.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 chkAnimateHealthActionPerformed(evt);
             }
         });
 
         jLabel11.setText("Battle logs:");
 
         cmbBattleLogOption.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmbBattleLogOptionActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
         jPanel2.setLayout(jPanel2Layout);
         jPanel2Layout.setHorizontalGroup(
             jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel2Layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(jPanel2Layout.createSequentialGroup()
                         .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                             .add(jLabel4)
                             .add(jLabel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                             .add(cmbOppHealth, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .add(cmbUserHealth, 0, 93, Short.MAX_VALUE))
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                             .add(lblEnemyHealth, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE)
                             .add(lblUserHealth, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 217, Short.MAX_VALUE)))
                     .add(chkAnimateHealth)
                     .add(jPanel2Layout.createSequentialGroup()
                         .add(jLabel11)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(cmbBattleLogOption, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 264, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap())
         );
         jPanel2Layout.setVerticalGroup(
             jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel2Layout.createSequentialGroup()
                 .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabel4)
                     .add(cmbUserHealth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(lblUserHealth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 27, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(cmbOppHealth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(lblEnemyHealth, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 27, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(jLabel5))
                 .add(26, 26, 26)
                 .add(chkAnimateHealth)
                 .add(18, 18, 18)
                 .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabel11)
                     .add(cmbBattleLogOption, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(131, Short.MAX_VALUE))
         );
 
         jTabbedPane1.addTab("Battle", jPanel2);
 
         jPanel3.setOpaque(false);
 
         lstPackages.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
         lstPackages.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
             public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                 lstPackagesValueChanged(evt);
             }
         });
         jScrollPane1.setViewportView(lstPackages);
 
         jLabel6.setFont(new java.awt.Font("Lucida Grande", 1, 13));
         jLabel6.setText("Installed Packages:");
 
         btnUp.setText("^");
         btnUp.setEnabled(false);
         btnUp.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnUpActionPerformed(evt);
             }
         });
 
         btnDown.setText("v");
         btnDown.setEnabled(false);
         btnDown.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnDownActionPerformed(evt);
             }
         });
 
         btnDelete.setText("Delete");
         btnDelete.setEnabled(false);
         btnDelete.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnDeleteActionPerformed(evt);
             }
         });
 
         jPanel4.setOpaque(false);
 
         jLabel7.setFont(new java.awt.Font("Lucida Grande", 1, 13));
         jLabel7.setText("Add New:");
 
         jLabel8.setText("Choose a default:");
 
         jLabel9.setText("Or provide a URL:");
 
         txtURL.setText("http://");
 
         btnAdd.setText("Add...");
         btnAdd.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnAddActionPerformed(evt);
             }
         });
 
         txtProgress.setFont(new java.awt.Font("Lucida Grande", 0, 10));
         txtProgress.setText("0% complete");
 
         org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
         jPanel4.setLayout(jPanel4Layout);
         jPanel4Layout.setHorizontalGroup(
             jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel4Layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                         .add(jPanel4Layout.createSequentialGroup()
                             .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                 .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4Layout.createSequentialGroup()
                                     .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                         .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel7)
                                         .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel8)
                                         .add(org.jdesktop.layout.GroupLayout.LEADING, cmbSpriteDefaults, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 139, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                         .add(org.jdesktop.layout.GroupLayout.LEADING, jLabel9)
                                         .add(org.jdesktop.layout.GroupLayout.LEADING, txtURL, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 264, Short.MAX_VALUE))
                                     .add(6, 6, 6))
                                 .add(txtProgress))
                             .addContainerGap())
                         .add(jPanel4Layout.createSequentialGroup()
                             .add(progress, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
                             .add(12, 12, 12)))
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4Layout.createSequentialGroup()
                         .add(btnAdd, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 84, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addContainerGap())))
         );
         jPanel4Layout.setVerticalGroup(
             jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel4Layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jLabel7)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jLabel8)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(cmbSpriteDefaults, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jLabel9)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(txtURL, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(35, 35, 35)
                 .add(txtProgress)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(progress, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(btnAdd)
                 .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
         jPanel3.setLayout(jPanel3Layout);
         jPanel3Layout.setHorizontalGroup(
             jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel3Layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(jLabel6)
                     .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 128, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(btnDelete, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                         .add(btnDown, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .add(btnUp, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         jPanel3Layout.linkSize(new java.awt.Component[] {btnDelete, btnDown, btnUp}, org.jdesktop.layout.GroupLayout.HORIZONTAL);
 
         jPanel3Layout.setVerticalGroup(
             jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel3Layout.createSequentialGroup()
                 .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                         .add(jLabel6)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                             .add(jPanel3Layout.createSequentialGroup()
                                 .add(btnUp, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                 .add(btnDown)
                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 162, Short.MAX_VALUE)
                                 .add(btnDelete))
                             .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE)))
                     .add(jPanel3Layout.createSequentialGroup()
                         .add(6, 6, 6)
                         .add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap())
         );
 
         jTabbedPane1.addTab("Sprites", jPanel3);
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 498, Short.MAX_VALUE)
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jTabbedPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void chkTimestampsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkTimestampsActionPerformed
         boolean selected = chkTimestamps.isSelected();
         Preference.setTimeStampsEnabled(selected);
         txtTimestampFormat.setEnabled(selected);
     }//GEN-LAST:event_chkTimestampsActionPerformed
 
     private void txtTimestampFormatCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_txtTimestampFormatCaretUpdate
         String format = txtTimestampFormat.getText();
         Date d = new Date();
         String date = "Invalid";
         try {
             SimpleDateFormat f = new SimpleDateFormat(format);
             date = f.format(d);
         } catch (Exception e) {
 
         }
         lblTimestampInfo.setText("H: 0-24 h: 0-12 m: 0-60 s: 0-60 ' ': escape    Current: " + date);
         Preference.setTimeStampFormat(txtTimestampFormat.getText());
     }//GEN-LAST:event_txtTimestampFormatCaretUpdate
 
     private void cmbUserHealthItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbUserHealthItemStateChanged
         HealthDisplay disp = (HealthDisplay)cmbUserHealth.getSelectedItem();
         lblUserHealth.setText("<html>" + Text.formatHealthChange(40, 368, disp) + "</html>");
         Preference.setUserHealthDisplay(disp);
     }//GEN-LAST:event_cmbUserHealthItemStateChanged
 
     private void cmbOppHealthItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbOppHealthItemStateChanged
         HealthDisplay disp = (HealthDisplay)cmbOppHealth.getSelectedItem();
         lblEnemyHealth.setText("<html>" + Text.formatHealthChange(4, 48, disp) + "</html>");
         Preference.setOpponentHealthDisplay(disp);
     }//GEN-LAST:event_cmbOppHealthItemStateChanged
 
     private void txtIgnoredCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_txtIgnoredCaretUpdate
         Preference.setIgnoredUsers(txtIgnored.getText());
     }//GEN-LAST:event_txtIgnoredCaretUpdate
 
     private void chkAnimateHealthActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkAnimateHealthActionPerformed
         Preference.setAnimateHealthBars(chkAnimateHealth.isSelected());
     }//GEN-LAST:event_chkAnimateHealthActionPerformed
 
     private void lstPackagesValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_lstPackagesValueChanged
         boolean on = lstPackages.getSelectedIndex() != -1;
         btnUp.setEnabled(on);
         btnDown.setEnabled(on);
         btnDelete.setEnabled(on);
     }//GEN-LAST:event_lstPackagesValueChanged
 
     private void btnUpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpActionPerformed
         int idx = lstPackages.getSelectedIndex();
         if (idx <= 0) return;
         DefaultComboBoxModel model = (DefaultComboBoxModel)lstPackages.getModel();
         Object obj = lstPackages.getSelectedValue();
         model.removeElementAt(idx);
         model.insertElementAt(obj, idx - 1);
         lstPackages.setSelectedValue(obj, true);
         saveSpriteDirectories();
     }//GEN-LAST:event_btnUpActionPerformed
 
     private void btnDownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDownActionPerformed
         DefaultComboBoxModel model = (DefaultComboBoxModel)lstPackages.getModel();
         int idx = lstPackages.getSelectedIndex();
         if ((idx == -1) || (idx == model.getSize() - 1)) return;
         Object obj = lstPackages.getSelectedValue();
         model.removeElementAt(idx);
         model.insertElementAt(obj, idx + 1);
         lstPackages.setSelectedValue(obj, true);
         saveSpriteDirectories();
     }//GEN-LAST:event_btnDownActionPerformed
 
     private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
         int idx = lstPackages.getSelectedIndex();
         if (idx == -1) return;
         int result = JOptionPane.showConfirmDialog(this, "Are you sure you wish " +
                 "to delete this package?", "Deleting Sprite Package", JOptionPane.YES_NO_OPTION);
         if (result == JOptionPane.YES_OPTION) {
             String dir = ((SpritePackage)lstPackages.getSelectedValue()).getDir();
             File f = new File(new File(Preference.getSpriteLocation()), dir);
             if (f.exists()) {
                 deleteDirectory(f);
                 ((DefaultComboBoxModel)lstPackages.getModel()).removeElementAt(idx);
             }
             saveSpriteDirectories();
         }
     }//GEN-LAST:event_btnDeleteActionPerformed
 
     private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
         SpriteLink link = (SpriteLink)cmbSpriteDefaults.getSelectedItem();
         String s;
         if ((link == null) || !"".equals(link.m_url)) {
             s = link.m_url;
         } else {
             s = txtURL.getText();
         }
         URL url = null;
         URLConnection conn = null;
         int length = -1;
         try {
             url = new URL(s);
             conn = url.openConnection();
             length = conn.getContentLength();
         } catch (Exception e) {
             JOptionPane.showMessageDialog(this, "Invalid URL");
             return;
         }
 
         final InputStream input;
         try {
             input = conn.getInputStream();
         } catch (Exception e) {
             JOptionPane.showMessageDialog(this, "Failed to open input stream");
             return;
         }
 
         SpriteDownloader task = new SpriteDownloader(input, length);
         task.addPropertyChangeListener(new PropertyChangeListener() {
             public void propertyChange(PropertyChangeEvent evt) {
                 Object val = evt.getNewValue();
                 if ("progress".equals(evt.getPropertyName())) {
                     int intVal = (Integer)val;
                     progress.setValue(intVal);
                     txtProgress.setText(intVal + "% complete");
                 }
             }
         });
         task.execute();
 
     }//GEN-LAST:event_btnAddActionPerformed
 
     private void chkSaveChatLogsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkSaveChatLogsActionPerformed
         Preference.setAutosaveChatLogs(chkSaveChatLogs.isSelected());
     }//GEN-LAST:event_chkSaveChatLogsActionPerformed
 
     private void txtLogDirCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_txtLogDirCaretUpdate
         Preference.setLogDirectory(txtLogDir.getText());
     }//GEN-LAST:event_txtLogDirCaretUpdate
 
     private void btnBrowseLogDirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBrowseLogDirActionPerformed
         JFileChooser chooser = new JFileChooser();
         chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
         if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
             return;
         }
         txtLogDir.setText(chooser.getSelectedFile().toString());
         txtLogDirCaretUpdate(null);
     }//GEN-LAST:event_btnBrowseLogDirActionPerformed
 
     private void cmbBattleLogOptionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbBattleLogOptionActionPerformed
         Preference.setBattleLogOption(cmbBattleLogOption.getSelectedIndex());
     }//GEN-LAST:event_cmbBattleLogOptionActionPerformed
 
 
     private void deleteDirectory(File f) {
         File[] files = f.listFiles();
         for (int i = 0; i < files.length; i++) {
             File file = files[i];
             if (file.isDirectory()) {
                 deleteDirectory(file);
             }
             file.delete();
         }
         f.delete();
     }
 
     /**
     * @param args the command line arguments
     */
     public static void main(String args[]) throws IOException {
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 new PreferencePane().setVisible(true);
             }
         });
     }
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnAdd;
     private javax.swing.JButton btnBrowseLogDir;
     private javax.swing.JButton btnDelete;
     private javax.swing.JButton btnDown;
     private javax.swing.JButton btnUp;
     private javax.swing.JCheckBox chkAnimateHealth;
     private javax.swing.JCheckBox chkSaveChatLogs;
     private javax.swing.JCheckBox chkTimestamps;
     private javax.swing.JComboBox cmbBattleLogOption;
     private javax.swing.JComboBox cmbOppHealth;
     private javax.swing.JComboBox cmbSpriteDefaults;
     private javax.swing.JComboBox cmbUserHealth;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel10;
     private javax.swing.JLabel jLabel11;
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
     private javax.swing.JPanel jPanel3;
     private javax.swing.JPanel jPanel4;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JTabbedPane jTabbedPane1;
     private javax.swing.JLabel lblEnemyHealth;
     private javax.swing.JLabel lblTimestampInfo;
     private javax.swing.JLabel lblUserHealth;
     private javax.swing.JList lstPackages;
     private javax.swing.JProgressBar progress;
     private javax.swing.JTextField txtIgnored;
     private javax.swing.JTextField txtLogDir;
     private javax.swing.JLabel txtProgress;
     private javax.swing.JTextField txtTimestampFormat;
     private javax.swing.JTextField txtURL;
     // End of variables declaration//GEN-END:variables
 
 }
