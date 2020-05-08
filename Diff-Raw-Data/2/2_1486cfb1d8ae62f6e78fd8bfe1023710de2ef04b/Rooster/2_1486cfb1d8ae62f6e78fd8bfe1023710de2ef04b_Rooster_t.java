 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /*
  * Rooster.java
  *
  * Created on 21-jan-2011, 11:32:49
  */
 package view;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.util.Calendar;
 import java.util.Locale;
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableModel;
 import model.Employee;
 import model.WorkHours;
 import roosterprogramma.*;
 
 /**
  *
  * @author Dark
  */
 public class Rooster extends javax.swing.JPanel {
 
     private DefaultTableModel model;
     private Calendar calendar = Calendar.getInstance();
     private ActionListener changeListener = new ActionListener() {
 
         @Override
         public void actionPerformed(ActionEvent e) {
             int selectedYear = Integer.parseInt(cmbYear.getSelectedItem().toString());
             int selectedMonth = Integer.parseInt(cmbMonth.getSelectedItem().toString());
             handleTime(selectedYear, selectedMonth);
         }
     };
 
     /**
      * Creates new form Rooster
      */
     public Rooster() {
         calendar.setTimeInMillis(System.currentTimeMillis());
         initComponents();
         refreshTable();
         fillBoxes();
         process();
     }
 
     private void fillBoxes() {
         for (int i = -5; i <= 5; i++) {
             cmbYear.addItem(calendar.get(Calendar.YEAR) + i);
         }
         for (int j = 1; j <= 12; j++) {
             cmbMonth.addItem(j);
         }
         cmbYear.setSelectedItem(calendar.get(Calendar.YEAR));
         cmbMonth.setSelectedItem(calendar.get(Calendar.MONTH) + 1);
         cmbYear.addActionListener(changeListener);
         cmbMonth.addActionListener(changeListener);
     }
 
     private void process() {
         refreshTable();
         tblSchedule.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
         model = (DefaultTableModel) tblSchedule.getModel();
         model.addColumn("Nr.");
         model.addColumn("Naam");
         model.addColumn("Contracturen");
         int daysOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
         for (int i = 1; i <= daysOfMonth; i++) {
             calendar.set(Calendar.DAY_OF_MONTH, i);
             model.addColumn(i + " - " + Translater.Translate(calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH)).substring(0, 2));
         }
         fill(daysOfMonth);
         tblSchedule.getColumnModel().getColumn(0).setPreferredWidth(30);
         tblSchedule.getColumnModel().getColumn(1).setPreferredWidth(100);
         tblSchedule.getColumnModel().getColumn(2).setPreferredWidth(75);
         for (int j = 0; j < tblSchedule.getColumnCount(); j++) {
             tblSchedule.getColumnModel().getColumn(j).setCellRenderer(new WhiteRenderer());
         }
     }
 
     private void fill(int daysOfMonth) {
         removeRows();
         for (Employee employee : RoosterProgramma.getInstance().getEmployees()) {
             if ((chkClerk.isSelected() && employee.isClerk())
                     || (chkMuseumEducator.isSelected() && employee.isMuseumEducator())
                     || (chkCallWorker.isSelected() && employee.isCallWorker())) {
                 insertEmployeeIntoTable(employee, daysOfMonth);
             }
         }
         for (int i = 1; i <= daysOfMonth; i++) {
             tblSchedule.getColumnModel().getColumn(i + 2).setPreferredWidth(50);
         }
     }
 
     private String getDate(Calendar calendar) {
         return calendar.get(Calendar.YEAR) + "-" + getMonth() + "-" + (calendar.get(Calendar.DAY_OF_MONTH) < 10 ? "0" + calendar.get(Calendar.DAY_OF_MONTH) : calendar.get(Calendar.DAY_OF_MONTH));
     }
 
     private String getMonth() {
         int month = calendar.get(Calendar.MONTH) + 1;
         return month < 10 ? "0" + Integer.toString(month) : Integer.toString(month);
     }
 
     private void refreshTable() {
         tblSchedule.setModel(new javax.swing.table.DefaultTableModel(
                 new Object[][]{},
                 new String[]{}) {
 
             @Override
             public Class<?> getColumnClass(int columnIndex) {
                 if (columnIndex == 0 || columnIndex == 2) {
                     return Integer.class;
                 }
                 return String.class;
             }
 
             @Override
             public boolean isCellEditable(int rowIndex, int colIndex) {
                 return colIndex > 2;
             }
 
             @Override
             public void fireTableCellUpdated(int row, int column) {
                 Object tmpValue = model.getValueAt(row, column);
                 if (tmpValue != null) {
                     String value = tmpValue.toString();
                     if (value.equalsIgnoreCase("x1")) {
                         model.setValueAt(RoosterProgramma.getInstance().getSettings().getX1(), row, column);
                     } else if (value.equalsIgnoreCase("x2")) {
                         model.setValueAt(RoosterProgramma.getInstance().getSettings().getX2(), row, column);
                     } else if (value.equalsIgnoreCase("x3")) {
                         model.setValueAt(RoosterProgramma.getInstance().getSettings().getX3(), row, column);
                     }
                 }
                 super.fireTableCellUpdated(row, column);
             }
         });
     }
 
     private void insertEmployeeIntoTable(Employee employee, int daysOfMonth) {
         if (employee.isCallWorker() || employee.isClerk() || employee.isMuseumEducator()) {
             Object[] fields = new Object[daysOfMonth + 3];
             fields[0] = employee.getEmployeeNumber();
             fields[1] = employee.getFullName();
             fields[2] = employee.getContractHours();
             for (int i = 1; i <= daysOfMonth; i++) {
                 calendar.set(Calendar.DAY_OF_MONTH, i);
                 WorkHours hour = RoosterProgramma.getQueryManager().getWorkHours(employee, getDate(calendar));
                 fields[i + 2] = hour.getShouldWork();
             }
             model.addRow(fields);
         }
     }
 
     private void searchTable() {
         removeRows();
         if (!tfPersoneelsnummer.getText().isEmpty()) {
             Employee employee = RoosterProgramma.getInstance().getEmployee(Integer.parseInt(tfPersoneelsnummer.getText()));
             if (employee != null) {
                 insertEmployeeIntoTable(employee, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
             }
         } else {
             if (!tfVoornaam.getText().isEmpty() || !tfAchternaam.getText().isEmpty()) {
                 String voornaam = tfVoornaam.getText().equals("Voornaam") ? "" : tfVoornaam.getText();
                 String achternaam = tfAchternaam.getText().equals("Achternaam") ? "" : tfAchternaam.getText();
                 for (Employee employee : RoosterProgramma.getInstance().searchEmployee(voornaam, achternaam)) {
                     insertEmployeeIntoTable(employee, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                 }
             } else {
                 process();
             }
         }
     }
 
     private boolean isValidWorkHour(String shouldWork) {
         return (shouldWork.equalsIgnoreCase("z")
                 || shouldWork.equalsIgnoreCase("v")
                 || shouldWork.equalsIgnoreCase("c")
                 || shouldWork.equalsIgnoreCase("k")
                 || shouldWork.equals("*")
                 || shouldWork.matches("[0-2]\\d{3}-[0-2]\\d{3}"));
     }
 
     private void handleTime(int year, int month) {
         calendar.setTimeInMillis(System.currentTimeMillis());
         calendar.set(Calendar.YEAR, year);
         calendar.set(Calendar.MONTH, month - 1);
         process();
     }
 
     private void removeRows() {
         while (model.getRowCount() != 0) {
             model.removeRow(0);
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
 
         btnSave = new javax.swing.JButton();
         btnBack = new javax.swing.JButton();
         pnlControls = new javax.swing.JPanel();
         chkCallWorker = new javax.swing.JCheckBox();
         chkClerk = new javax.swing.JCheckBox();
         chkMuseumEducator = new javax.swing.JCheckBox();
         cmbYear = new javax.swing.JComboBox<Integer>();
         cmbMonth = new javax.swing.JComboBox<Integer>();
         btnExcelExport = new javax.swing.JButton();
         jspSchedule = new javax.swing.JScrollPane();
         tblSchedule = new javax.swing.JTable();
         tfVoornaam = new javax.swing.JTextField();
         tfAchternaam = new javax.swing.JTextField();
         jLabel1 = new javax.swing.JLabel();
         tfPersoneelsnummer = new javax.swing.JFormattedTextField();
 
         setBackground(new java.awt.Color(153, 204, 255));
 
         btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/btnSave.png"))); // NOI18N
         btnSave.setToolTipText("Opslaan");
         btnSave.setContentAreaFilled(false);
         btnSave.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSaveActionPerformed(evt);
             }
         });
 
         btnBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/btnPrevious.png"))); // NOI18N
         btnBack.setToolTipText("Vorige");
         btnBack.setContentAreaFilled(false);
         btnBack.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnBackActionPerformed(evt);
             }
         });
 
         pnlControls.setBackground(new java.awt.Color(153, 204, 255));
 
         chkCallWorker.setBackground(new java.awt.Color(153, 204, 255));
         chkCallWorker.setSelected(true);
         chkCallWorker.setText("Oproepkracht");
         chkCallWorker.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 chkCallWorkerItemStateChanged(evt);
             }
         });
         pnlControls.add(chkCallWorker);
 
         chkClerk.setBackground(new java.awt.Color(153, 204, 255));
         chkClerk.setSelected(true);
         chkClerk.setText("Baliemedewerker");
         chkClerk.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 chkClerkItemStateChanged(evt);
             }
         });
         pnlControls.add(chkClerk);
 
         chkMuseumEducator.setBackground(new java.awt.Color(153, 204, 255));
         chkMuseumEducator.setSelected(true);
         chkMuseumEducator.setText("Museumdocent");
         chkMuseumEducator.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 chkMuseumEducatorItemStateChanged(evt);
             }
         });
         pnlControls.add(chkMuseumEducator);
 
         pnlControls.add(cmbYear);
 
         pnlControls.add(cmbMonth);
 
         btnExcelExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/btnExport.png"))); // NOI18N
         btnExcelExport.setToolTipText("Exporteer naar excel");
         btnExcelExport.setContentAreaFilled(false);
         btnExcelExport.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnExcelExportActionPerformed(evt);
             }
         });
 
         jspSchedule.setBackground(new java.awt.Color(255, 255, 255));
         jspSchedule.setToolTipText("");
         jspSchedule.setPreferredSize(new java.awt.Dimension(452, 200));
 
         tblSchedule.setAutoCreateRowSorter(true);
         tblSchedule.setBackground(new java.awt.Color(153, 204, 255));
         tblSchedule.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
 
             }
         ));
         tblSchedule.setToolTipText("Mogelijke invoer: Z, V, C, K, *, X1, X2, X3, 0000-0000");
         tblSchedule.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
         tblSchedule.setFillsViewportHeight(true);
         tblSchedule.getTableHeader().setReorderingAllowed(false);
         jspSchedule.setViewportView(tblSchedule);
 
         tfVoornaam.setText("Voornaam");
         tfVoornaam.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 tfVoornaamFocusGained(evt);
             }
         });
         tfVoornaam.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 tfVoornaamKeyReleased(evt);
             }
         });
 
         tfAchternaam.setText("Achternaam");
         tfAchternaam.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 tfAchternaamFocusGained(evt);
             }
         });
         tfAchternaam.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 tfAchternaamKeyReleased(evt);
             }
         });
 
         jLabel1.setText("of personeelsnummer:");
 
         tfPersoneelsnummer.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
         tfPersoneelsnummer.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 tfPersoneelsnummerKeyReleased(evt);
             }
         });
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(pnlControls, javax.swing.GroupLayout.DEFAULT_SIZE, 913, Short.MAX_VALUE)
                     .addComponent(jspSchedule, javax.swing.GroupLayout.DEFAULT_SIZE, 913, Short.MAX_VALUE)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                         .addComponent(btnBack)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 604, Short.MAX_VALUE)
                         .addComponent(btnExcelExport)
                         .addGap(18, 18, 18)
                         .addComponent(btnSave))
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(tfVoornaam, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(tfAchternaam, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(18, 18, 18)
                         .addComponent(jLabel1)
                         .addGap(18, 18, 18)
                         .addComponent(tfPersoneelsnummer, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(tfVoornaam)
                     .addComponent(tfAchternaam)
                     .addComponent(jLabel1)
                     .addComponent(tfPersoneelsnummer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jspSchedule, javax.swing.GroupLayout.DEFAULT_SIZE, 467, Short.MAX_VALUE)
                 .addGap(18, 18, 18)
                 .addComponent(pnlControls, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(btnSave)
                         .addComponent(btnBack))
                     .addComponent(btnExcelExport))
                 .addContainerGap())
         );
     }// </editor-fold>//GEN-END:initComponents
 
     private void btnBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBackActionPerformed
         RoosterProgramma.getInstance().showPanel(new MainMenu());
     }//GEN-LAST:event_btnBackActionPerformed
 
     private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
         if (tblSchedule.getCellEditor() != null) {
             tblSchedule.getCellEditor().stopCellEditing();
         }
         for (int i = 0; i < model.getRowCount(); i++) {
             Employee employee = RoosterProgramma.getInstance().getEmployee(Integer.parseInt(model.getValueAt(tblSchedule.convertRowIndexToModel(i), 0).toString().split(" - ")[0]));
             for (int j = 3; j < model.getColumnCount(); j++) {
                 String date = calendar.get(Calendar.YEAR) + "-" + getMonth() + "-" + model.getColumnName(j).split(" - ")[0];
                 String shouldWork = model.getValueAt(tblSchedule.convertRowIndexToModel(i), j).toString();
                 if (isValidWorkHour(shouldWork) || shouldWork.isEmpty()) {
                     WorkHours hour = RoosterProgramma.getQueryManager().getWorkHours(employee, date);
                    if (hour.getEmployeeNumber() == -1 && !shouldWork.isEmpty()) {
                         hour = new WorkHours(employee.getEmployeeNumber(), date);
                         hour.setShouldWork(shouldWork);
                         if (!RoosterProgramma.getQueryManager().insertWorkHours(hour)) {
                             return;
                         }
                     } else if (!hour.getShouldWork().equals(shouldWork)) {
                         if (shouldWork.isEmpty()) {
                             if (!RoosterProgramma.getQueryManager().deleteWorkHours(employee.getEmployeeNumber(), date)) {
                                 return;
                             }
                         } else {
                             hour.setShouldWork(shouldWork);
                             if (!RoosterProgramma.getQueryManager().updateWorkHours(hour)) {
                                 return;
                             }
                         }
                     }
                 } else {
                     Utils.showMessage("De waarde ingevuld voor " + employee.getFullName() + " op "
                             + date + " is incorrect.", "Incorrecte veldwaarde!", "",
                             false);
                     return;
                 }
             }
         }
         Utils.showMessage("Succesvol opgeslagen.", "Opslaan gelukt!", null, false);
     }//GEN-LAST:event_btnSaveActionPerformed
 
     private void btnExcelExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExcelExportActionPerformed
         String input = Utils.showFileChooser("Opslaan");
         if (!input.isEmpty()) {
             boolean inverted = Utils.promptQuestion("Wilt u de tabel omgedraaid of precies zoals hierboven?", false, "Zoals hierboven", "Omgedraaid");
             ExcelExporter.Export(tblSchedule, new File(input.contains(".xls") ? input : input + ".xls"), inverted);
         }
     }//GEN-LAST:event_btnExcelExportActionPerformed
 
     private void tfVoornaamKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfVoornaamKeyReleased
         searchTable();
     }//GEN-LAST:event_tfVoornaamKeyReleased
 
     private void tfAchternaamKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfAchternaamKeyReleased
         searchTable();
     }//GEN-LAST:event_tfAchternaamKeyReleased
 
     private void tfPersoneelsnummerKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfPersoneelsnummerKeyReleased
         searchTable();
     }//GEN-LAST:event_tfPersoneelsnummerKeyReleased
 
     private void tfVoornaamFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfVoornaamFocusGained
         if (tfVoornaam.getText().equals("Voornaam")) {
             tfVoornaam.setText("");
         }
     }//GEN-LAST:event_tfVoornaamFocusGained
 
     private void tfAchternaamFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_tfAchternaamFocusGained
         if (tfAchternaam.getText().equals("Achternaam")) {
             tfAchternaam.setText("");
         }
     }//GEN-LAST:event_tfAchternaamFocusGained
 
     private void chkMuseumEducatorItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_chkMuseumEducatorItemStateChanged
         fill(calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
     }//GEN-LAST:event_chkMuseumEducatorItemStateChanged
 
     private void chkCallWorkerItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_chkCallWorkerItemStateChanged
         fill(calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
     }//GEN-LAST:event_chkCallWorkerItemStateChanged
 
     private void chkClerkItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_chkClerkItemStateChanged
         fill(calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
     }//GEN-LAST:event_chkClerkItemStateChanged
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnBack;
     private javax.swing.JButton btnExcelExport;
     private javax.swing.JButton btnSave;
     private javax.swing.JCheckBox chkCallWorker;
     private javax.swing.JCheckBox chkClerk;
     private javax.swing.JCheckBox chkMuseumEducator;
     private javax.swing.JComboBox<Integer> cmbMonth;
     private javax.swing.JComboBox<Integer> cmbYear;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JScrollPane jspSchedule;
     private javax.swing.JPanel pnlControls;
     private javax.swing.JTable tblSchedule;
     private javax.swing.JTextField tfAchternaam;
     private javax.swing.JFormattedTextField tfPersoneelsnummer;
     private javax.swing.JTextField tfVoornaam;
     // End of variables declaration//GEN-END:variables
 }
