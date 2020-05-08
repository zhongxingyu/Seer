 /*
  * Copyright 2012 Markus Geiss.
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
 package com.github.mgeiss.xls2ora.presentation.view;
 
 import com.github.mgeiss.xls2ora.presentation.control.WorkflowController;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.util.logging.Level;
 import javax.swing.*;
 import javax.swing.filechooser.FileFilter;
 import org.jdesktop.swingx.JXErrorPane;
 import org.jdesktop.swingx.error.ErrorInfo;
 
 /**
  *
  * @author Markus Geiss
  * @version 1.0
  */
 public class SelectSourcePanel extends WizardPanel implements ActionListener {
 
     private SelectDatabasePanel databasePanel;
     private JTextField fileField;
     private JTextField sheetField;
 
     public SelectSourcePanel(WorkflowController workflowController) {
         super(workflowController, "Quelldatei", "Wählen sie eine Excel-Datei und geben das Tabellenblatt an.", new ImageIcon(ClassLoader.getSystemResource("icons/spreadsheet.png")));
         this.init();
     }
 
     private void init() {
         JPanel content = new JPanel(new GridBagLayout());
 
         content.add(new JLabel("Excel-Datei:"),
                 new GridBagConstraints(0, 0, 1, 1, 0.00D, 0.00D, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 10, 0, 5), 0, 0));
 
         this.fileField = new JTextField();
         this.fileField.setPreferredSize(new Dimension(180, 23));
         this.fileField.setEditable(false);
         content.add(this.fileField,
                 new GridBagConstraints(1, 0, 1, 1, 0.00D, 0.00D, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 5, 0, 0), 0, 0));
 
         JButton fileChooserButton = new JButton(new ImageIcon(ClassLoader.getSystemResource("icons/filefind.png")));
         fileChooserButton.setPreferredSize(new Dimension(23, 23));
         fileChooserButton.setMargin(new Insets(3, 3, 3, 3));
         fileChooserButton.addActionListener(this);
         content.add(fileChooserButton,
                 new GridBagConstraints(2, 0, 1, 1, 0.00D, 0.00D, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 5, 0, 5), 0, 0));
 
         content.add(new JLabel("Tabelle:"),
                 new GridBagConstraints(0, 1, 1, 1, 0.00D, 0.00D, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 10, 5, 5), 0, 0));
 
         this.sheetField = new JTextField();
         this.sheetField.setPreferredSize(new Dimension(180, 23));
         content.add(this.sheetField,
                 new GridBagConstraints(1, 1, 2, 1, 0.00D, 0.00D, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
 
         content.add(new JLabel(),
                 new GridBagConstraints(3, 2, 1, 1, 1.00D, 1.00D, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
 
 
         super.addContent(content);
     }
 
     @Override
     public WizardPanel getPredecessorPanel() {
         return null;
     }
 
     @Override
     public WizardPanel getSuccesorPanel() {
         if (this.databasePanel == null) {
             this.databasePanel = new SelectDatabasePanel(super.workflowController, this);
         }
         return databasePanel;
     }
 
     @Override
     public void prepare() {
     }
 
     @Override
     public void proceed() {
         if (this.fileField.getText().length() == 0 || this.sheetField.getText().length() == 0) {
             JOptionPane.showMessageDialog(this, "Bitte überprüfen sie ihre Angaben zur Quelldatei!", "Datei konnte nicht geladen werden!", JOptionPane.ERROR_MESSAGE);
             throw new IllegalStateException();
         }
         try {
             Class.forName("com.googlecode.sqlsheet.Driver");
             Connection connection = DriverManager.getConnection("jdbc:xls:file:" + this.fileField.getText());
             super.workflowController.setAttribute("sheet", this.sheetField.getText());
             super.workflowController.setAttribute("excelConnection", connection);
         } catch (ClassNotFoundException | SQLException ex) {
             JXErrorPane.showDialog(null, new ErrorInfo("Datei konnte nicht geladen werden!", "Bitte überprüfen sie ihre Angaben zur Quelldatei!", null, null, ex, Level.SEVERE, System.getenv()));
         }
     }
 
     @Override
     public void actionPerformed(ActionEvent evt) {
         JFileChooser fileChooser = new JFileChooser(System.getProperty("user.home"));
         fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
         fileChooser.setFileFilter(new FileFilter() {
 
             @Override
             public boolean accept(File file) {
                if (file.isDirectory() 
                        || file.getName().toLowerCase().endsWith(".xls")
                        || file.getName().toLowerCase().endsWith(".xlsx")) {
                     return true;
                 }
                 return false;
             }
 
             @Override
             public String getDescription() {
                 return "Excel-Dateien (*.xls)";
             }
         });
 
         if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
             File excelFile = fileChooser.getSelectedFile();
             this.fileField.setText(excelFile.getAbsolutePath());
         }
     }
 }
