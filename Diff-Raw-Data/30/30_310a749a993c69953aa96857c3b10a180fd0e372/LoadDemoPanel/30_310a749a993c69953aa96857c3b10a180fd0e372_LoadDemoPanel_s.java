 package dit.panels;
 
 import dit.DEIDGUI;
 import dit.*;
 import dit.DemographicTableModel;
 import dit.DeidData;
 import dit.XlsFile;
 import dit.XlsxFile;
 import dit.EditDemoDataFrame;
 import java.io.*;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.ArrayList;
 import java.util.Scanner;
 import java.awt.event.*;
 import jxl.*;
 import jxl.write.*;
 import javax.swing.*;
 import java.util.regex.*;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.table.JTableHeader;
 import javax.swing.table.TableColumnModel;
 import jxl.Workbook;
 import jxl.write.Label;
 import jxl.write.WritableSheet;
 import jxl.write.WritableWorkbook;
 import jxl.write.WriteException;
 
 
 /**
  *
  * @author christianprescott && angelo
  */
 public class LoadDemoPanel extends javax.swing.JPanel implements WizardPanel {
     private EditDemoDataFrame Editdemo;
      final JTextField textField = new JTextField();
     /**
      * Creates new form LoadDemoPanel
      */
     public LoadDemoPanel() {
         initComponents();
         DEIDGUI.title = "Load Demographic Data";
         DEIDGUI.helpButton.setEnabled(true);
         
         jButton2.setVisible(false);
         // TODO: remove this auto-load line
         //        ReadDemographicFile(new File("/Users/christianprescott/Desktop/dataset/my_demo_data.txt"));
         jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
         jTable1.setColumnSelectionAllowed(true);
         jTable1.setRowSelectionAllowed(false);
         jTable1.setRowHeight(26);
         
         Action action = new AbstractAction()
         {
             public void actionPerformed(ActionEvent e)
             {
                 
                 TableCellListener tcl = (TableCellListener)e.getSource();
                 int i=tcl.getRow();
                 int j=tcl.getColumn();
                 String oldValue=tcl.getOldValue().toString();
                 String newValue=tcl.getNewValue().toString();
                 if(!oldValue.equals(newValue))
                 {
                     final int choice;
                     choice= JOptionPane.showConfirmDialog(LoadDemoPanel.this, "Make sure this change?", "Confirm", JOptionPane.YES_NO_CANCEL_OPTION);
                     if(choice== JOptionPane.YES_OPTION)
                     {
                         DeidData.demographicData.setValueAt(newValue, i, j);
                         DeidData.demoFileModified=true;
                         //correctDemoModel.setValueAt(textField.getText(), i, j);
                         jTable1.setValueAt(newValue, i, j);
                         // System.out.println("hit one"+i+" and " + j+textField.getText());
                         jTable1.setDefaultRenderer(Object.class, new missingValueRenderer());
                         jTable1.clearSelection();
                     }
                     else
                     {
                         DeidData.demographicData.setValueAt(oldValue, i, j);
                         //correctDemoModel.setValueAt(textField.getText(), i, j);
                         jTable1.setValueAt(oldValue, i, j);
                         // System.out.println("hit one"+i+" and " + j+textField.getText());
                         jTable1.setDefaultRenderer(Object.class, new missingValueRenderer());
                         jTable1.clearSelection();
                     }
                 }
               
             }
         };
         TableCellListener tcl = new TableCellListener(jTable1, action);
       //  jTable1.addPropertyChangeListener(tcl);
         
         if(DeidData.demographicData!=null)
         {
             jTable1.setModel(DeidData.demographicData);
         }
         
         jTable1.getTableHeader().addMouseListener(new MouseAdapter(){
             @Override
             public void mouseClicked(MouseEvent e) {
                 
                 int columnIndex = jTable1.getTableHeader().columnAtPoint(e.getPoint());
                 DeidData.IdColumn = columnIndex;
                 ArrayList rowList = new ArrayList(Arrays.asList(DeidData.demographicData.getData()));
                 Collections.sort(rowList, new DemoRowComparator());
                 Object[][] rows = new Object[rowList.size()][];
                 rowList.toArray(rows);
                 DeidData.demographicData = new DemographicTableModel(DeidData.demographicData.getDataFieldNames(), rows);
                 jTable1.getTableHeader().removeMouseListener(this);
                 jTable1.setModel(DeidData.demographicData);
                 jTable1.setColumnSelectionInterval( DeidData.IdColumn,  DeidData.IdColumn);
                 jTable1.getTableHeader().addMouseListener(this);
                 // jTable1.setRowSelectionAllowed(true);
                 // System.out.println("click");
                 
             }
             
         });
         
         if (DeidData.demographicData != null) {
             jTable1.setModel(DeidData.demographicData);
             jTable1.setColumnSelectionInterval(DeidData.IdColumn, DeidData.IdColumn);
             jTable1.setDefaultRenderer(Object.class, new missingValueRenderer());
             DEIDGUI.continueButton.setEnabled(true);
             DEIDGUI.log("Loaded existing demographic table data");
         } else {
             // DeidData.demographicData= DemographicTableModel.dummyModel;
             DEIDGUI.continueButton.setEnabled(false);
         }
         
         
         final JTextField textField = new JTextField();
         for(int i=0; i< jTable1.getColumnCount(); i++)
         {
             jTable1.getColumnModel().getColumn(i).setCellEditor(new DefaultCellEditor(textField));
         }
         jTable1.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
         textField.setEditable(true);
         textField.getDocument().addDocumentListener(new DocumentListener(){
             @Override
             public void changedUpdate(DocumentEvent e) {
                 int i = jTable1.getSelectedRow();
                 int j = jTable1.getSelectedColumn();
                 String oldText=(String)jTable1.getValueAt(i, j);
                 String newText=textField.getText();
                
                 if(!oldText.equals(newText))
                 {
                     System.out.println("Not equal");
                     final int choice;
                     choice= JOptionPane.showConfirmDialog(LoadDemoPanel.this, "Make sure this change?", "Confirm", JOptionPane.YES_NO_CANCEL_OPTION);
                     if(choice== JOptionPane.YES_OPTION)
                     {
                         DeidData.demographicData.setValueAt(textField.getText(), i, j);
                         //correctDemoModel.setValueAt(textField.getText(), i, j);
                         jTable1.setValueAt(textField.getText(), i, j);
                         //  System.out.println("hit one"+i+" and " + j+textField.getText());
                         jTable1.clearSelection();
                     }
                 }
             }
             @Override
             public void removeUpdate(DocumentEvent e) {
                 System.out.println("End read");
                 if (textField.isShowing() && textField.getText().trim().equals("")){
                     JOptionPane.showMessageDialog(null,
                             "WARNING: This cell may need to be non-empty!", "Warning Massage",
                             JOptionPane.WARNING_MESSAGE);
                 }
             }
             @Override
             public void insertUpdate(DocumentEvent e) {
                 
                 /*  int i = jTable1.getSelectedRow();
                  * int j = jTable1.getSelectedColumn();
                  * DeidData.demographicData.setValueAt(textField.getText(), i, j);
                  * correctDemoModel.setValueAt(textField.getText(), i, j);
                  * jTable1.setValueAt(textField.getText(), i, j);
                  * System.out.println("hit one"+i+" and " + j+textField.getText());
                  * if (textField.getText().trim().equals("")){
                  * JOptionPane.showMessageDialog(null,
                  * "Error: This cell can not be empty!", "Error Massage",
                  * JOptionPane.ERROR_MESSAGE);
                  * }*/
             }
             
             
             
         });
         
         DEIDGUI.log("LoadDemoPanel initialized");
     }
     
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jLabel3 = new javax.swing.JLabel();
         jButtonLoadDemo = new javax.swing.JButton();
         jScrollPane1 = new javax.swing.JScrollPane();
         jTable1 = new javax.swing.JTable();
         jButton1 = new javax.swing.JButton();
         jButton2 = new javax.swing.JButton();
         btnWriteBackFile = new javax.swing.JButton();
         btnCancleChange = new javax.swing.JButton();
 
         jLabel3.setText("<html><p>Select a data file, then click the column that will be used to match the images.</p><p>&nbsp;</p></html>");
 
         jButtonLoadDemo.setText("Choose data file...");
         jButtonLoadDemo.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButtonLoadDemoActionPerformed(evt);
             }
         });
 
         jTable1.setModel(new DemographicTableModel(new String[]{"No data"}, new Object[1][1]));
         jScrollPane1.setViewportView(jTable1);
 
         jButton1.setText("Dummy file");
         jButton1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton1ActionPerformed(evt);
             }
         });
 
         jButton2.setText("Refresh");
         jButton2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton2ActionPerformed(evt);
             }
         });
 
         btnWriteBackFile.setText("Save new data file");
         btnWriteBackFile.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnWriteBackFileActionPerformed(evt);
             }
         });
 
         btnCancleChange.setText("Revert changes");
         btnCancleChange.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnCancleChangeActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(jScrollPane1)
                     .add(layout.createSequentialGroup()
                         .add(jButtonLoadDemo)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(jButton1)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(jButton2)
                         .add(0, 0, Short.MAX_VALUE))
                     .add(jLabel3)
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                         .add(0, 0, Short.MAX_VALUE)
                         .add(btnCancleChange)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                         .add(btnWriteBackFile)))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jLabel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jButtonLoadDemo)
                     .add(jButton1)
                     .add(jButton2))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(btnWriteBackFile)
                     .add(btnCancleChange)))
         );
     }// </editor-fold>//GEN-END:initComponents
     
     private void jButtonLoadDemoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLoadDemoActionPerformed
         final javax.swing.JFileChooser fc = new JFileChooser();
         fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
         fc.setAcceptAllFileFilterUsed(false);
         fc.addChoosableFileFilter(new DemoFilter());
         String dirrec;
         File filename = new File("/tmp/textpath.txt");
         try{
             FileReader fr = new FileReader(filename);
             BufferedReader br = new BufferedReader(fr);
             dirrec = br.readLine();
             // System.out.println(dirrec);
             if (dirrec!= null)
             {
                 fc.setCurrentDirectory(new File(dirrec));
             }
             
         }catch(IOException e)
         {
             fc.setCurrentDirectory(null);
         }
         
         int returnVal = fc.showOpenDialog(this);
         if (returnVal == JFileChooser.APPROVE_OPTION) {
             File file = fc.getSelectedFile();
             if (file.canRead()) {
                 ReadDemographicFile(file);
             } else {
                 JOptionPane.showMessageDialog(this, "This file could not "
                         + "be opened.", "Invalid Demographic File",
                         JOptionPane.ERROR_MESSAGE);
             }
             String dir = fc.getSelectedFile().getParent();
             //System.out.println(dir);
             
             
             if (!filename.exists()){
                 try{
                     filename.createNewFile();
                 }
                 catch (IOException e) {
                     DEIDGUI.log("Fail to create file!" );
                 }
                 
             }
             try
             {
                 
                 RandomAccessFile  pathfile = new RandomAccessFile (filename,"rw");
                 pathfile.writeBytes(dir);
             }catch(IOException e)
             {
                 DEIDGUI.log("No Parent Directory Found!" );
             }
         }
          
     }//GEN-LAST:event_jButtonLoadDemoActionPerformed
     
     private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
         // TODO add your handling code here:
         if (DeidData.demographicData != null){
             DeidData.demographicData = new DemographicTableModel(
                     DeidData.demographicData.getDataFieldNames(), DeidData.demographicData.getData());
             jTable1.setModel(DeidData.demographicData);
         }
         else {
             JOptionPane.showMessageDialog(this, "No Demographic File selected.Please Select Demographic File first. "
                     , "Error",
                     JOptionPane.ERROR_MESSAGE);
         }
     }//GEN-LAST:event_jButton2ActionPerformed
     
     private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
         File dummyDemographic=new File(DeidData.outputPath+"/dummy.txt");
         if(!dummyDemographic.exists())
         {
             try {
                 dummyDemographic.createNewFile();
                 FileWriter fw=new FileWriter(dummyDemographic.getAbsoluteFile());
                 BufferedWriter bw=new BufferedWriter(fw);
                 String content="TID\n1";
                 bw.write(content);
                 bw.close();
             } catch (IOException ex) {
                 Logger.getLogger(LoadDemoPanel.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         ReadDemographicFile(dummyDemographic);
         
         
     }//GEN-LAST:event_jButton1ActionPerformed
 
     private void btnCancleChangeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancleChangeActionPerformed
         
         if(DeidData.demoSourceFile != null)
         {
             int choice = JOptionPane.showConfirmDialog(this, "Would you discard all the change?",
                     "Revert", JOptionPane.OK_CANCEL_OPTION);
             if(choice== JOptionPane.OK_OPTION)
                 ReadDemographicFile(DeidData.demoSourceFile);
         }
         else
         {
             JOptionPane.showMessageDialog(this, "No previous demographic file!","Error", JOptionPane.ERROR_MESSAGE);
         }
     }//GEN-LAST:event_btnCancleChangeActionPerformed
 
     private void btnWriteBackFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnWriteBackFileActionPerformed
         File destFile=null;
         if(DeidData.demoFileModified)
         {
             JFileChooser fc=new JFileChooser(DeidData.demoSourceFile);
             fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
             fc.setFileFilter(new DemoFilter());
             int returnVal = fc.showOpenDialog(this);
             if (returnVal == JFileChooser.APPROVE_OPTION) {
                 File file = fc.getSelectedFile();
                 if(file.exists())
                 {
                     int choice= JOptionPane.showConfirmDialog(this, "File already existed, would you want overwrite it?","Confirm",
                             JOptionPane.INFORMATION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
                     if(choice== JOptionPane.OK_OPTION)
                         destFile=file;
                 }
                 else
                 {
                     int choice= JOptionPane.showConfirmDialog(this, "File does not existed, would you want create it?","Confirm",
                             JOptionPane.INFORMATION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
                     if(choice== JOptionPane.OK_OPTION)
                     {
                         try {
                             file.createNewFile();
                         } catch (IOException ex) {
                         }
                         destFile=file;
                     }
                 }                
             }
         }
         else
         {
             JOptionPane.showMessageDialog(this, "Demographic file is not change.", "Information", JOptionPane.INFORMATION_MESSAGE);
         }
         if(destFile!=null)
             try {
                 writeBack(destFile);
             }catch (WriteException ex) {
                 Logger.getLogger(LoadDemoPanel.class.getName()).log(Level.SEVERE, null, ex);
             }
             catch (IOException ex) {
                 Logger.getLogger(LoadDemoPanel.class.getName()).log(Level.SEVERE, null, ex);
             } 
     }//GEN-LAST:event_btnWriteBackFileActionPerformed
     
     private void writeBack(File dest) throws IOException, WriteException
     {
         String extension= FileUtils.getExtension(dest);
         FileWriter fw=new FileWriter(dest);
         if(extension.equals("txt"))
         {
             StringBuilder header=new StringBuilder();
             TableColumnModel headerColumn=jTable1.getTableHeader().getColumnModel();
             for(int i=0;i<headerColumn.getColumnCount();i++)
             {
                header.append(headerColumn.getColumn(i).getHeaderValue());
                if(i!=headerColumn.getColumnCount()-1)
                    header.append("\t");
             }
             header.append(System.getProperty("line.separator"));
             fw.write(header.toString());
             for(int i=0;i<jTable1.getRowCount();i++)
             {
                 StringBuilder line=new StringBuilder();
                 for(int j=0;j<jTable1.getColumnCount();j++)
                 {
                    line.append((String)jTable1.getValueAt(i, j));
                    if(j!=jTable1.getColumnCount()-1)
                        line.append("\t");
                 }
                 if(i!=jTable1.getRowCount()-1)
                     line.append(System.getProperty("line.separator"));
                 fw.write(line.toString());
             }
             fw.close();
         }
         else if(extension.equals("xls"))
         {
             WritableWorkbook workbook = Workbook.createWorkbook(dest);
             WritableSheet sheet = workbook.createSheet("First Sheet", 0);
             TableColumnModel headerColumn=jTable1.getTableHeader().getColumnModel();
             for(int i=0;i<headerColumn.getColumnCount();i++)
             {
                 Label label = new Label(i, 0, headerColumn.getColumn(i).getHeaderValue().toString());
                 sheet.addCell(label);
             }
             for(int i=0;i<jTable1.getRowCount();i++)
             {
                 for(int j=0;j<jTable1.getColumnCount();j++)
                 {
                     Label label = new Label(j, i+1, (String)jTable1.getValueAt(i, j));
                     sheet.addCell(label);
                 }
             }
             workbook.write();
             workbook.close();
         }
         else if(extension.equals("xlsx"))
        {}
         System.out.println("Write to:"+dest.getAbsolutePath());
         JOptionPane.showMessageDialog(this, "New File has been saved.","Congratulation", JOptionPane.INFORMATION_MESSAGE);
     }
     
     private void ReadDemographicFile(File demoFile) {
         DeidData.demoSourceFile=demoFile;
         ArrayList<Object[]> rowList = new ArrayList<Object[]>();
         String[] fields = null;
         if (demoFile.getName().endsWith(".xlsx")){
             
             XlsxFile xlsx = new XlsxFile();
             xlsx.setInputFile(demoFile.getAbsolutePath());
             try{
                 fields = xlsx.readHeadings();
                 rowList = xlsx.read();
             }
             catch(Exception e){
                 JOptionPane.showMessageDialog(this, "This file could not "
                         + "be opened.", "Invalid Demographic File",
                         JOptionPane.ERROR_MESSAGE);
             }
             
         }
         else
             if (demoFile.getName().endsWith(".xls")){
                 XlsFile xls = new XlsFile();
                 xls.setInputFile(demoFile.getAbsolutePath());
                 try{
                     fields = xls.readHeadings();
                     rowList = xls.read();
                 }
                 catch(Exception e){
                     /* JOptionPane.showMessageDialog(this, "This file could not "
                      * + "be opened.", "Invalid Demographic File",
                      * JOptionPane.ERROR_MESSAGE);*/
                 }
             }
             else {
                 
                 Scanner inputStream = null;
                 try {
                     inputStream = new Scanner(demoFile);
                 } catch (FileNotFoundException ex) {
                     JOptionPane.showMessageDialog(this, "This file could not "
                             + "be opened.", "Invalid Demographic File",
                             JOptionPane.ERROR_MESSAGE);
                     return;
                 }
                 
                 //String[] fields;
                 if (inputStream.hasNextLine()) {
                     // Read data field headings from first line
                     fields = inputStream.nextLine().split("\t");
                     //DITGUI.log("Demographic header row: " + StringUtils.join(fields, ','));
                 } else {
                     // File is empty
                     inputStream.close();
                     JOptionPane.showMessageDialog(this, "This file does not contain "
                             + "any demographic data.", "Invalid Demographic File",
                             JOptionPane.ERROR_MESSAGE);
                     return;
                 }
                 
                 // Read the data and fill a 2D array
                 //ArrayList<Object[]> rowList = new ArrayList<Object[]>();
                 int lineIndex = 1;
                 while (inputStream.hasNextLine()) {
                     lineIndex++;
                     String line = inputStream.nextLine().trim();
                     if(!line.isEmpty()){
                         Object[] rowData = line.split("\t");
                         Object[] rowDataMatch;
                         rowDataMatch = new Object[fields.length];
                         for(int i = 0; i< rowData.length; i++){
                             if (StringFilter(rowData[i].toString()).equals("")){
                                 
                                 rowData[i] = "misV";
                                 DeidData.errorlog.addElement("Missing value in column " + fields[i] +" in line " + (lineIndex - 1));
                                 //  DEIDGUI.log("Missing value in column " + fields[i] +" in line " + (lineIndex - 1),DEIDGUI.LOG_LEVEL.WARNING);
                                 
                             }
                         }
                       
                         if (rowData.length > fields.length) {
                             System.arraycopy(rowData, 0, rowDataMatch, 0, fields.length);
                             DeidData.errorlog.addElement("Mismatched data in demographic file line " +(lineIndex - 1) + " (" + rowData.length + "/" +fields.length + "), some data " + "may be unnecessary. Please correct your original file.");
                             /*DEIDGUI.log("Mismatched data in demographic file line " +
                              * (lineIndex - 1) + " (" + rowData.length + "/" +
                              * fields.length + "), some data " + "may be unnecessary. Please correct your original file.",
                              * DEIDGUI.LOG_LEVEL.WARNING);*/
                         }
                         if (rowData.length < fields.length) {
                             for(int k = 0; k< fields.length; k++){
                                 if (k < rowData.length)
                                     rowDataMatch[k] = rowData[k];
                                 else rowDataMatch[k] = "misV";
                             }
                             DeidData.errorlog.addElement("Mismatched data in demographic file line " +
                                     (lineIndex - 1) + " (" + rowData.length + "/" +
                                     fields.length + "), some data " + "may be missing. Please correct your original file. ");
                             /*DEIDGUI.log("Mismatched data in demographic file line " +
                              * (lineIndex - 1) + " (" + rowData.length + "/" +
                              * fields.length + "), some data " + "may be missing. Please correct your original file. ",
                              * DEIDGUI.LOG_LEVEL.WARNING);*/
                         }
                         if (rowData.length != fields.length) {
                             
                             rowList.add(rowDataMatch);
                         } else {
                             rowList.add(rowData);
                         }
                     }
                 }
                 inputStream.close();
             }
         
         if (rowList.isEmpty()) {
             JOptionPane.showMessageDialog(this, "This file does not contain "
                     + "any demographic data.", "Invalid Demographic File",
                     JOptionPane.ERROR_MESSAGE);
             return;
         }
      
         // Sort the data by ID
         // The data may be alphanumeric, and the user may change the ID data later.
         // Collections.sort(rowList, new DemoRowComparator());
         
         Object[][] rows = new Object[rowList.size()][];
         rowList.toArray(rows);
         DeidData.demographicData = new DemographicTableModel(fields, rows);
         jTable1.setModel(DeidData.demographicData);
         
         
         
        
         for(int i=0; i< jTable1.getColumnCount(); i++)
         {
             jTable1.getColumnModel().getColumn(i).setCellEditor(new DefaultCellEditor(textField));
         }
         jTable1.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
         textField.setEditable(true);
         
         textField.getDocument().addDocumentListener(new DocumentListener(){
             @Override
             public void changedUpdate(DocumentEvent e) {
                 int i = jTable1.getSelectedRow();
                 int j = jTable1.getSelectedColumn();
                 
                 String oldText=(String)jTable1.getValueAt(i, j);
                 String newText=textField.getText();
                  System.out.println("End read");
                 if(!oldText.equals(newText))
                 {
                     System.out.println("Not equal");
                     final int choice;
                     choice= JOptionPane.showConfirmDialog(LoadDemoPanel.this, "Make sure this change?", "Confirm", JOptionPane.YES_NO_CANCEL_OPTION);
                     if(choice== JOptionPane.YES_OPTION)
                     {
                         DeidData.demographicData.setValueAt(textField.getText(), i, j);
                         //correctDemoModel.setValueAt(textField.getText(), i, j);
                         jTable1.setValueAt(textField.getText(), i, j);
                         // System.out.println("hit one"+i+" and " + j+textField.getText());
                         jTable1.setDefaultRenderer(Object.class, new missingValueRenderer());
                         jTable1.clearSelection();
                     }
                 }
             }
             @Override
             public void removeUpdate(DocumentEvent e) {
                  
                 if (textField.isShowing() && textField.getText().trim().equals("")){
                     JOptionPane.showMessageDialog(null,
                             "WARNING: This cell may not be empty!", "Warning Massage",
                             JOptionPane.WARNING_MESSAGE);
                 }
             }
             @Override
             public void insertUpdate(DocumentEvent e) {
                 
                 /*  int i = jTable1.getSelectedRow();
                  * int j = jTable1.getSelectedColumn();
                  * DeidData.demographicData.setValueAt(textField.getText(), i, j);
                  * correctDemoModel.setValueAt(textField.getText(), i, j);
                  * jTable1.setValueAt(textField.getText(), i, j);
                  * System.out.println("hit one"+i+" and " + j+textField.getText());
                  * if (textField.getText().trim().equals("")){
                  * JOptionPane.showMessageDialog(null,
                  * "Error: This cell can not be empty!", "Error Massage",
                  * JOptionPane.ERROR_MESSAGE);
                  * }*/
             }
             
             
             
         });
         
         // System.out.println(jTable1.getColumnClass(0));
         DEIDGUI.continueButton.setEnabled(true);
         int idColumn = DeidData.demographicData.getColumnNdx("id");
         if (idColumn < 0) {
             idColumn = 0;
         }
         DeidData.IdColumn = idColumn;
         jTable1.setColumnSelectionInterval(idColumn, idColumn);
         //jTable1.setCellSelectionEnabled(true);
         jTable1.setDefaultRenderer(Object.class, new missingValueRenderer());
         DeidData.selectedIdentifyingFields = null;
         DeidData.deselectedIdentifyingFields = null;
         
     }
     
     private   static   String StringFilter(String   str)   throws   PatternSyntaxException   {
         
         String regEx="[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}‘”“’]";
         java.util.regex.Pattern   p   =   java.util.regex.Pattern.compile(regEx);
         Matcher   m   =   p.matcher(str);
         return   m.replaceAll("").trim();
     }  
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnCancleChange;
     private javax.swing.JButton btnWriteBackFile;
     private javax.swing.JButton jButton1;
     private javax.swing.JButton jButton2;
     private javax.swing.JButton jButtonLoadDemo;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JTable jTable1;
     // End of variables declaration//GEN-END:variables
     
     @Override
     public WizardPanel getNextPanel() {
         if(DeidData.demographicData== DemographicTableModel.dummyModel)
         {
             return new DeidentifyProgressPanel(true);
         }
         return new MatchDataPanel();
     }
     
     @Override
     public WizardPanel getPreviousPanel() {
         return new LoadImagesPanel();
     }
 }
