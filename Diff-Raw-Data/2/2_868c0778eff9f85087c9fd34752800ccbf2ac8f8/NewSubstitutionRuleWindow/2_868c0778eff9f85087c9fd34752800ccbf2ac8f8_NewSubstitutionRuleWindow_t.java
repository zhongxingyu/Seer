 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.pearson.Interface.Windows;
 
 import com.pearson.Database.MySQL.MySQLDataType;
 import com.pearson.Interface.Interfaces.EnumInterface;
 import com.pearson.Interface.Interfaces.XMLInterface;
 import com.pearson.Rules.SubstitutionTypes.DateSubstitutionTypes;
 import com.pearson.Rules.SubstitutionTypes.NumericSubstitutionTypes;
 import com.pearson.Rules.SubstitutionTypes.StringSubstitutionTypes;
 import com.pearson.SQL.Database;
 import com.pearson.SQL.Column;
 import noNamespace.*;
 import com.pearson.SQL.MySQLTable;
 
 import java.io.File;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import noNamespace.RulesDocument.Rules;
 
 import javax.swing.*;
 
 
 /**
  * @author : Ruslan Kiselev
  */
 public class NewSubstitutionRuleWindow extends JDialog {
 
     /**
      * Creates new form NewSubstitutionRule
      */
     Database database;
     ArrayList<String> tableNames = new ArrayList<>();
     boolean isTriggersIsolated = true;
     boolean isTableSelected = false;
     String tableSelected;
     Column columnSelected;
     File setFromFile;
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JLabel NewSubstitutionRuleLabel;
     private javax.swing.JButton browseButton;
     private javax.swing.ButtonGroup buttonGroup1;
     private javax.swing.JPanel buttonsPanel;
     private javax.swing.JButton cancelButton;
     private javax.swing.JLabel columnLabel;
     private javax.swing.JComboBox columnsComboBox;
     private javax.swing.JButton createSubstitutionRule;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JSeparator jSeparator1;
     private java.awt.MenuBar menuBar1;
     private javax.swing.JPanel selectOptionsPanel;
     private javax.swing.JLabel selectedColumnLabel;
     private javax.swing.JLabel selectedTypeOfSubstitutionLabel;
     private javax.swing.JLabel selectedValueLabel;
     private javax.swing.JScrollPane tableScrollPane;
     private javax.swing.JTable tablesSelectedTable;
     private javax.swing.JComboBox typeOfSubstitutionComboBox;
     private javax.swing.JLabel typeOfSubstitutionLabel;
     private javax.swing.JLabel valueLabel;
     private javax.swing.JLabel valueOrBrowseLabel;
     private javax.swing.JTextField valueTextField;
 
     /**
      * Similar to new shuffle rule window, this method throws an exception,
      * by the time when users open the new substitution rule window,
      * database information has been passed over. *
      */
 
     public NewSubstitutionRuleWindow() throws SQLException {
         database = new Database(com.pearson.Interface.UIManager.getDefaultSchema(), com.pearson.Interface.UIManager.getUsername(),
                 com.pearson.Interface.UIManager.getPassword(), "jdbc:mysql://" + com.pearson.Interface.UIManager.getUrl()
                 + ":" + com.pearson.Interface.UIManager.getPort());
 
         // end of preparing database structure
 
         for (MySQLTable table : database.tables.values()) {
             tableNames.add(table.getTableName());
         }
 
         initComponents();
         // set the third step be invisible for the user
         valueOrBrowseLabel.setVisible(false);
         valueTextField.setVisible(false);
         browseButton.setVisible(false);
         typeOfSubstitutionComboBox.setEnabled(false);
         columnsComboBox.setEnabled(false);
         tablesSelectedTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 
         this.setModalityType(ModalityType.APPLICATION_MODAL);
 
     }
 
     /**
      * @param args the command line arguments
      */
     public static void main(String args[]) {
         /* Set the Nimbus look and feel */
         //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
         /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
          * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
          */
         try {
             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(NewSubstitutionRuleWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(NewSubstitutionRuleWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(NewSubstitutionRuleWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(NewSubstitutionRuleWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
 
         /* Create and display the form */
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 try {
                     new NewSubstitutionRuleWindow().setVisible(true);
 
                 } catch (SQLException ex) {
                     Logger.getLogger(NewShuffleRuleWindow.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         });
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         menuBar1 = new java.awt.MenuBar();
         buttonGroup1 = new javax.swing.ButtonGroup();
         selectOptionsPanel = new javax.swing.JPanel();
         tableScrollPane = new javax.swing.JScrollPane();
         tablesSelectedTable = new javax.swing.JTable();
         buttonsPanel = new javax.swing.JPanel();
         cancelButton = new javax.swing.JButton();
         createSubstitutionRule = new javax.swing.JButton();
         columnsComboBox = new javax.swing.JComboBox();
         jLabel1 = new javax.swing.JLabel();
         jLabel3 = new javax.swing.JLabel();
         typeOfSubstitutionComboBox = new javax.swing.JComboBox();
         jPanel1 = new javax.swing.JPanel();
         valueTextField = new javax.swing.JTextField();
         browseButton = new javax.swing.JButton();
         valueOrBrowseLabel = new javax.swing.JLabel();
         jSeparator1 = new javax.swing.JSeparator();
         NewSubstitutionRuleLabel = new javax.swing.JLabel();
         columnLabel = new javax.swing.JLabel();
         typeOfSubstitutionLabel = new javax.swing.JLabel();
         valueLabel = new javax.swing.JLabel();
         selectedColumnLabel = new javax.swing.JLabel();
         selectedTypeOfSubstitutionLabel = new javax.swing.JLabel();
         selectedValueLabel = new javax.swing.JLabel();
 
         setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 
         selectOptionsPanel.setLayout(new java.awt.GridBagLayout());
 
         tablesSelectedTable.setModel(new com.pearson.Interface.Models.ItemsSelectedTableModel(tableNames));
         tablesSelectedTable.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 tablesSelectedTableMouseClicked(evt);
             }
         });
         tablesSelectedTable.addComponentListener(new java.awt.event.ComponentAdapter() {
             public void componentShown(java.awt.event.ComponentEvent evt) {
                 tablesSelectedTableComponentShown(evt);
             }
         });
         tableScrollPane.setViewportView(tablesSelectedTable);
 
         cancelButton.setText("Cancel");
         cancelButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cancelButtonActionPerformed(evt);
             }
         });
 
         createSubstitutionRule.setText("Create Substitution Rule");
         createSubstitutionRule.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 createSubstitutionRuleActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout buttonsPanelLayout = new javax.swing.GroupLayout(buttonsPanel);
         buttonsPanel.setLayout(buttonsPanelLayout);
         buttonsPanelLayout.setHorizontalGroup(
                 buttonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, buttonsPanelLayout.createSequentialGroup()
                                 .addContainerGap()
                                 .addComponent(createSubstitutionRule)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 79, Short.MAX_VALUE)
                                 .addComponent(cancelButton)
                                 .addContainerGap())
         );
         buttonsPanelLayout.setVerticalGroup(
                 buttonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                         .addGroup(buttonsPanelLayout.createSequentialGroup()
                                 .addGap(5, 5, 5)
                                 .addGroup(buttonsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                         .addComponent(cancelButton)
                                         .addComponent(createSubstitutionRule)))
         );
 
         columnsComboBox.setModel(new javax.swing.DefaultComboBoxModel());
         columnsComboBox.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 columnsComboBoxActionPerformed(evt);
             }
         });
 
         jLabel1.setText("2) Please Select Type Of Substitution");
 
         jLabel3.setText("1) Please Select A Column");
 
         typeOfSubstitutionComboBox.setModel(new javax.swing.DefaultComboBoxModel());
         typeOfSubstitutionComboBox.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 typeOfSubstitutionComboBoxActionPerformed(evt);
             }
         });
 
         jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.LINE_AXIS));
 
         valueTextField.setText("Value");
         valueTextField.setPreferredSize(columnsComboBox.getPreferredSize());
         valueTextField.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 valueTextFieldActionPerformed(evt);
             }
         });
         jPanel1.add(valueTextField);
         valueTextField.setVisible(true);
 
         browseButton.setText("Browse...");
         browseButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 browseButtonActionPerformed(evt);
             }
         });
         jPanel1.add(browseButton);
         valueTextField.setVisible(true);
 
         valueOrBrowseLabel.setText("ThirdStepLabel(Should be Hidden)");
 
         NewSubstitutionRuleLabel.setText("New Substitution Rule");
 
         columnLabel.setText("Column: ");
 
         typeOfSubstitutionLabel.setText("Type Of Substitution: ");
 
         valueLabel.setText("Value: ");
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
                 layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                         .addGroup(layout.createSequentialGroup()
                                 .addContainerGap()
                                 .addComponent(tableScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                         .addComponent(buttonsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                         .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                 .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                 .addComponent(selectOptionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                         .addComponent(jSeparator1)
                                         .addComponent(typeOfSubstitutionComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                         .addGroup(layout.createSequentialGroup()
                                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                         .addComponent(jLabel1)
                                                         .addComponent(valueOrBrowseLabel)
                                                         .addComponent(NewSubstitutionRuleLabel)
                                                         .addGroup(layout.createSequentialGroup()
                                                                 .addGap(25, 25, 25)
                                                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                         .addGroup(layout.createSequentialGroup()
                                                                                 .addComponent(typeOfSubstitutionLabel)
                                                                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                 .addComponent(selectedTypeOfSubstitutionLabel))
                                                                         .addGroup(layout.createSequentialGroup()
                                                                                 .addComponent(valueLabel)
                                                                                 .addGap(83, 83, 83)
                                                                                 .addComponent(selectedValueLabel))
                                                                         .addGroup(layout.createSequentialGroup()
                                                                                 .addComponent(columnLabel)
                                                                                 .addGap(74, 74, 74)
                                                                                 .addComponent(selectedColumnLabel))))
                                                         .addComponent(jLabel3))
                                                 .addGap(0, 0, Short.MAX_VALUE))
                                         .addComponent(columnsComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                 .addContainerGap())
         );
         layout.setVerticalGroup(
                 layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                         .addGroup(layout.createSequentialGroup()
                                 .addContainerGap()
                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                         .addGroup(layout.createSequentialGroup()
                                                 .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                 .addGap(18, 18, 18)
                                                 .addComponent(columnsComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                 .addGap(9, 9, 9)
                                                 .addComponent(jLabel1)
                                                 .addGap(18, 18, 18)
                                                 .addComponent(typeOfSubstitutionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                 .addComponent(valueOrBrowseLabel)
                                                 .addGap(18, 18, 18)
                                                 .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                 .addGap(23, 23, 23)
                                                 .addComponent(selectOptionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                 .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                 .addComponent(NewSubstitutionRuleLabel)
                                                 .addGap(18, 18, 18)
                                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                         .addComponent(columnLabel)
                                                         .addComponent(selectedColumnLabel))
                                                 .addGap(18, 18, 18)
                                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                         .addComponent(typeOfSubstitutionLabel)
                                                         .addComponent(selectedTypeOfSubstitutionLabel))
                                                 .addGap(18, 18, 18)
                                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                         .addComponent(valueLabel)
                                                         .addComponent(selectedValueLabel))
                                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                 .addComponent(buttonsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                         .addComponent(tableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 441, Short.MAX_VALUE))
                                 .addContainerGap())
         );
 
         valueTextField.setVisible(true);
 
         pack();
         setLocationRelativeTo(null);
     }// </editor-fold>//GEN-END:initComponents
 
     private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
         // Exit
         dispose();
     }//GEN-LAST:event_cancelButtonActionPerformed
 
     private void tablesSelectedTableComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_tablesSelectedTableComponentShown
         // TODO add your handling code here:
 
     }//GEN-LAST:event_tablesSelectedTableComponentShown
 
     private void tablesSelectedTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablesSelectedTableMouseClicked
         // select table from the window
         isTriggersIsolated = true;
         isTableSelected = true;
         typeOfSubstitutionComboBox.setEnabled(false);
 
         columnsComboBox.removeAllItems();
         typeOfSubstitutionComboBox.removeAllItems();
 
         int row = tablesSelectedTable.rowAtPoint(evt.getPoint());
         tableSelected = tableNames.get(row);
         for (Column column : database.tables.get(tableSelected).columns.values()) {
             columnsComboBox.addItem(column.name + "(" + column.getType() + ")");
         }
         isTriggersIsolated = false;
         columnsComboBox.setEnabled(true);
     }//GEN-LAST:event_tablesSelectedTableMouseClicked
 
     private void createSubstitutionRuleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createSubstitutionRuleActionPerformed
 
         Rules rulesInSet = XMLInterface.getSetDocument().getMaskingSet().getRules();
 
         // build the rule according to information from this window
         // parentRule would be passed by the mainWindow in case it is a dependent rule
         Rule parentRule = com.pearson.Interface.UIManager.getParentRule();
         Rule newRule;
 
         // depending on whether is an independent rule or dependent
         if (parentRule == null) {
             newRule = rulesInSet.addNewRule();
             newRule.setId(rulesInSet.getRuleArray().length + "");
         } else {
             newRule = parentRule.getDependencies().addNewRule();
             newRule.setId(parentRule.getId().concat("-" + parentRule.getDependencies().getRuleArray().length) + "");
         }
 
         addRuleInformation(newRule);
         // let other windows know that masking set has change
 
         com.pearson.Interface.UIManager.update();
 
         dispose();
     }//GEN-LAST:event_createSubstitutionRuleActionPerformed
 
     private void columnsComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_columnsComboBoxActionPerformed
 
         if (isTriggersIsolated) return;
 
         isTriggersIsolated = true;
         String columnSelectedString = columnsComboBox.getSelectedItem().toString();
         typeOfSubstitutionComboBox.removeAllItems();
 
         columnSelected = database.tables.get(tableSelected).columns.get(
                 columnSelectedString.substring(0, columnSelectedString.indexOf("(")));
 
         if (MySQLDataType.isNumericType(columnSelected.getType())) addNumericToSubstitutionType();
         else if (MySQLDataType.isDateType(columnSelected.getType())) addDateToSubstitutionType();
         else if (MySQLDataType.isStringType(columnSelected.getType())) addStringToSubstitutionType();
 
         // let the user know the selected column
         selectedColumnLabel.setText(columnSelectedString);
         typeOfSubstitutionComboBox.setEnabled(true);
         isTriggersIsolated = false;
     }//GEN-LAST:event_columnsComboBoxActionPerformed
 
     private void addDateToSubstitutionType() {
 
         for (DateSubstitutionTypes type : DateSubstitutionTypes.values())
             typeOfSubstitutionComboBox.addItem(type.toString());
     }
 
     private void addNumericToSubstitutionType() {
 
         for (NumericSubstitutionTypes type : NumericSubstitutionTypes.values())
             typeOfSubstitutionComboBox.addItem(type.toString());
     }
 
     private void addStringToSubstitutionType() {
 
         for (StringSubstitutionTypes type : StringSubstitutionTypes.values())
             typeOfSubstitutionComboBox.addItem(type.toString());
     }
 
     private void addRuleInformation(Rule newRule) {
 
         SubstitutionRule newRuleSubstitution = newRule.addNewSubstitute();
         // add new column
         String columnString = columnsComboBox.getSelectedItem().toString().substring(0, columnsComboBox.getSelectedItem().toString().indexOf("("));
         newRuleSubstitution.setColumn(columnString);
 
         String targetTable = tableNames.get(tablesSelectedTable.getSelectedRow());
         newRule.setTarget(targetTable);
         newRule.setRuleType(RuleType.SUBSTITUTION);
 
         String substitutionType = typeOfSubstitutionComboBox.getSelectedItem().toString();
         newRule.getSubstitute().setSubstitutionActionType(EnumInterface.getSubstitutionActionType(substitutionType));
         newRule.getSubstitute().setSubstitutionDataType(EnumInterface.getSubstitutionDataType(columnSelected.getType()));
 
         setValueInformation(EnumInterface.getSubstitutionActionType(substitutionType), EnumInterface.getSubstitutionDataType(columnSelected.getType()), newRule);
 
     }
 
     private void setValueInformation(SubstitutionActionType.Enum actionType, SubstitutionDataType.Enum dateType, Rule newRule) {
 
         String value = valueTextField.getText();
 
         if (dateType == SubstitutionDataType.DATE) {
             if (actionType == SubstitutionActionType.SET_TO_VALUE) {
                 newRule.getSubstitute().setDateValue1(BigInteger.valueOf(Integer.parseInt(value)));
             }
         } else if (dateType == SubstitutionDataType.NUMERIC) {
             if (actionType == SubstitutionActionType.SET_TO_VALUE) {
                 newRule.getSubstitute().setNumericValue(BigDecimal.valueOf(Integer.parseInt(value)));
             }
         } else if (dateType == SubstitutionDataType.STRING) {
             if (actionType == SubstitutionActionType.SET_TO_RANDOM) {
                 newRule.getSubstitute().setNumericValue(BigDecimal.valueOf(Integer.parseInt(value)));
             } else if (actionType == SubstitutionActionType.SET_TO_VALUE) {
                 newRule.getSubstitute().setStringValue1(value);
             } else if (actionType == SubstitutionActionType.SET_FROM_FILE) {
                 // since we displayed the text inside the label we can use it
                 newRule.getSubstitute().setStringValue1(selectedValueLabel.getText());
             }
         }
     }
 
     private void typeOfSubstitutionComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_typeOfSubstitutionComboBoxActionPerformed
 
         if (isTriggersIsolated) return;
 
         selectedValueLabel.setText("");
 
         isTriggersIsolated = true;
         String selectedType = typeOfSubstitutionComboBox.getSelectedItem().toString();
         if (selectedType.equals(StringSubstitutionTypes.SET_TO_VALUE.toString()) ||
                 selectedType.equals(DateSubstitutionTypes.SET_TO_VALUE.toString()) ||
                 selectedType.equals(NumericSubstitutionTypes.SET_TO_VALUE.toString())) {
             valueOrBrowseLabel.setText("Please Enter Value You Want To Set To");
             valueOrBrowseLabel.setVisible(true);
             valueTextField.setVisible(true);
         } else if (selectedType.equals(StringSubstitutionTypes.FROM_A_LIST.toString())) {
             valueOrBrowseLabel.setText("Please Select A File");
             valueOrBrowseLabel.setVisible(true);
             browseButton.setVisible(true);
         } else if (selectedType.equals(DateSubstitutionTypes.SET_TO_VALUE.toString())) {
             valueOrBrowseLabel.setText("Please Enter Value In Millis since Jan 1 1970");
             valueOrBrowseLabel.setVisible(true);
             valueTextField.setVisible(true);
         } else if (selectedType.equals(StringSubstitutionTypes.RANDOM_STRING.toString())) {
             valueOrBrowseLabel.setText("Please Enter Max String Size");
             valueOrBrowseLabel.setVisible(true);
             valueTextField.setVisible(true);
         } else {
             valueTextField.setVisible(false);
             valueOrBrowseLabel.setVisible(false);
             browseButton.setVisible(false);
         }
 
         selectedTypeOfSubstitutionLabel.setText(selectedType);
         isTriggersIsolated = false;
 
         // todo version 2.0: add random within range specified by the user
     }//GEN-LAST:event_typeOfSubstitutionComboBoxActionPerformed
 
     private void valueTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_valueTextFieldActionPerformed
 
         selectedValueLabel.setText(valueTextField.getText());
     }//GEN-LAST:event_valueTextFieldActionPerformed
 
     private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
 
         // create a file chooser
         JFileChooser fc = new JFileChooser();
         int returnVal = fc.showOpenDialog(browseButton);
         File file = null;
 
         if (evt.getSource() == browseButton) {
             //handle open button action
 
             if (returnVal == JFileChooser.APPROVE_OPTION) {
                 file = fc.getSelectedFile();
 
             }
         }
 
         setFromFile = file;
         selectedValueLabel.setText("File: " + file.getAbsolutePath());
     }//GEN-LAST:event_browseButtonActionPerformed
     // End of variables declaration//GEN-END:variables
 
 
 }
