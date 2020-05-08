 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edacc.manageDB;
 
 import edacc.EDACCAddInstanceToInstanceClass;
 import edacc.EDACCApp;
 import edacc.EDACCCreateEditInstanceClassDialog;
 import edacc.EDACCExtWarningErrorDialog;
 import edacc.EDACCExtendedWarning;
 import edacc.EDACCManageDBInstanceFilter;
 import edacc.manageDB.InstanceParser.*;
 import edacc.EDACCManageDBMode;
 import edacc.model.InstaceNotInDBException;
 import edacc.model.Instance;
 import edacc.model.InstanceAlreadyInDBException;
 import edacc.model.InstanceClass;
 import edacc.model.InstanceClassAlreadyInDBException;
 import edacc.model.InstanceClassDAO;
 import edacc.model.InstanceClassMustBeSourceException;
 
 import edacc.model.InstanceDAO;
 import edacc.model.InstanceHasInstanceClass;
 import edacc.model.InstanceHasInstanceClassDAO;
 import edacc.model.InstanceIsInExperimentException;
 import edacc.model.InstanceSourceClassHasInstance;
 import edacc.model.MD5CheckFailedException;
 import edacc.model.NoConnectionToDBException;
 import edacc.model.Tasks;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.security.NoSuchAlgorithmException;
 import java.sql.SQLException;
 import java.util.Vector;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.RowFilter;
 
 /**
  *
  * @author rretz
  */
 public class ManageDBInstances{
 
     EDACCManageDBMode main;
     JPanel panelManageDBInstances;
     JFileChooser jFileChooserManageDBInstance;
     JFileChooser jFileChooserManageDBExportInstance;
     JTable tableInstances;
 
     public ManageDBInstances(EDACCManageDBMode main, JPanel panelManageDBInstances, 
             JFileChooser jFileChooserManageDBInstance,  JFileChooser jFileChooserManageDBExportInstance,
             JTable tableInstances) {
         this.main = main;
         this.panelManageDBInstances = panelManageDBInstances;
         this.jFileChooserManageDBInstance = jFileChooserManageDBInstance;
         this.jFileChooserManageDBExportInstance = jFileChooserManageDBExportInstance;
         this.tableInstances = tableInstances;
     }
     /**
      * Load all instances from the database into the instancetable
      * @throws NoConnectionToDBException
      * @throws SQLException
      */
     public void loadInstances() throws NoConnectionToDBException, SQLException, InstanceClassMustBeSourceException {
         main.instanceTableModel.instances.clear();
         main.instanceTableModel.addInstances(new Vector<Instance>(InstanceDAO.getAll()));
         main.instanceTableModel.fireTableDataChanged();
     }
 
     public void loadInstanceClasses() throws SQLException{
         main.instanceClassTableModel.classes.clear();
         main.instanceClassTableModel.classSelect.clear();
         main.instanceClassTableModel.addClasses(new Vector<InstanceClass>(InstanceClassDAO.getAll()));
     }
 
     /**
      * Will open a jFilechooser to select a file or directory to add all containing
      * instance files into the "instance table" of the MangeDBMode.
      */
 
     public void addInstances(InstanceClass input, File ret,  Tasks task){
         try {
         
             RecursiveFileScanner InstanceScanner = new RecursiveFileScanner("cnf");
             Vector<File> instanceFiles = InstanceScanner.searchFileExtension(ret);
             if (instanceFiles.isEmpty()) {
                 JOptionPane.showMessageDialog(panelManageDBInstances, "No Instances have been found.", "Error", JOptionPane.WARNING_MESSAGE);
             }
             task.setOperationName("Adding Instances");
             if (input.getName().equals("")) {
                 Vector<Instance> instances;
                 instances = buildInstancesAutogenerateClass(instanceFiles, ret, task);
                 main.instanceTableModel.addInstances(instances);
                 loadInstanceClasses();
             } else {
                 Vector<Instance> instances = buildInstancesGivenClass(instanceFiles, (InstanceClass) input, task);
                 main.instanceTableModel.addInstances(instances);
                 loadInstanceClasses();
             }
         } catch (NoSuchAlgorithmException ex) {
             Logger.getLogger(ManageDBInstances.class.getName()).log(Level.SEVERE, null, ex);
         } catch (NullPointerException ex) {
             Logger.getLogger(ManageDBInstances.class.getName()).log(Level.SEVERE, null, ex);
         } catch (SQLException ex) {
             Logger.getLogger(ManageDBInstances.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(ManageDBInstances.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public void removeInstances(int[] rows) throws NoConnectionToDBException, SQLException {
         Vector<Instance> toRemove = new Vector<Instance>();
         
         for (int i = 0; i < rows.length; i++) {
             toRemove.add((Instance )main.instanceTableModel.getValueAt(tableInstances.convertRowIndexToModel(rows[i]), 5));
         }
         InstanceTableModel tableModel = new InstanceTableModel();
         tableModel.addInstances(toRemove);
        JFrame mainFrame = EDACCApp.getApplication().getMainFrame();
        EDACCExtendedWarning.showMessageDialog(EDACCExtendedWarning.OK_CANCEL_OPTIONS,mainFrame, "Do you really won't to remove the listed instances?", new JTable(tableModel));
        
        //EDACCExtendedWarning.sho

         //EDACCExtWarningErrorDialog removeInstances = new EDACCExtWarningErrorDialog(mainFrame, true, true, tableModel,
         //        "Do you really won't to remove the listed instances?");
         //removeInstances.setLocationRelativeTo(mainFrame);
         //EDACCApp.getApplication().show(removeInstances);
         //if(EDACCExtendedWarning.){
 //            Tasks.startTask("TryToRemoveInstances", new Class[]{Vector.class, edacc.model.Tasks.class}, new Object[]{toRemove,  null}, this, this.main);
   //      }
         /*
          * Vector<Instance> toRemove = new Vector<Instance>();
 
         for (int i = 0; i < rows.length; i++) {
             toRemove.add((Instance )main.instanceTableModel.getValueAt(tableInstances.convertRowIndexToModel(rows[i]), 5));
         }
         InstanceTableModel tableModel = new InstanceTableModel();
         tableModel.addInstances(toRemove);
         JFrame mainFrame = EDACCApp.getApplication().getMainFrame();
         EDACCExtWarningErrorDialog removeInstances = new EDACCExtWarningErrorDialog(mainFrame, true, true, tableModel,
                 "Do you really won't to remove the listed instances?");
         removeInstances.setLocationRelativeTo(mainFrame);
        EDACCApp.getApplication().show(removeInstances);
        if(removeInstances.isAccept()){
             Tasks.startTask("TryToRemoveInstances", new Class[]{Vector.class, edacc.model.Tasks.class}, new Object[]{toRemove,  null}, this, this.main);
         }
         */
     }
 
     /**
      * Remove the given rows from the instanceTableModel
      * @param rows the rows which have to be deleted
      */
        public void TryToRemoveInstances(Vector<Instance> toRemove, Tasks task) throws NoConnectionToDBException, SQLException {
        task.setOperationName("Removing instances");
         Vector<Instance> rem = new Vector<Instance>();
         Vector<Instance> notRem = new Vector<Instance>();
         for (int i = 0; i < toRemove.size(); i++) {
             Instance ins = toRemove.get(i);
             try {
                 InstanceDAO.delete(ins);
                 rem.add(ins);
                 task.setStatus(i + " of " + toRemove.size() + " instances removed");
                 task.setTaskProgress((float)i/(float) toRemove.size());
             } catch (InstanceIsInExperimentException ex) {
                 notRem.add(ins);
             }
         }
         main.instanceTableModel.removeInstances(rem);
         if(!notRem.isEmpty()){
             JOptionPane.showMessageDialog(panelManageDBInstances,
              "Some of the selected instances couldn't be removed, because they are containing to a experiment.",
             "Error",
             JOptionPane.ERROR_MESSAGE);
         }
     }
     
     /**
      * Removes all instances from the instancetable
      */
     public void removeAllInstancesFromTable(){
         main.instanceTableModel.clearTable();
 
     }
 
     /**
      * Removes the Filter of the given JTable
      */
     public void removeFilter(JTable table) {
         table.setRowSorter(null);
     }
 
     /**
      *
      * @param file
      * @return md5sum of the given File
      * @throws FileNotFoundException
      * @throws IOException
      * @throws NoSuchAlgorithmException
      */
     public String calculateMD5(File file) throws FileNotFoundException, IOException, NoSuchAlgorithmException {
         return Util.calculateMD5(file);
     }
     /**
      * Writes the selected instances binarys into the choosen Directory.
      * @param rows rows of the selected instances
      * @throws IOException
      * @throws NoConnectionToDBException
      * @throws SQLException
      * @throws InstaceNotInDBException
      */
     public void exportInstances(int[] rows, String path, Tasks task) throws IOException, NoConnectionToDBException, SQLException,
             InstaceNotInDBException, FileNotFoundException, MD5CheckFailedException,
             NoSuchAlgorithmException{
         task.setOperationName("Exporting instances");
 
         Instance temp;
         Vector<Instance> md5Error = new Vector<Instance>();
         for(int i = 0; i < rows.length; i++){
            temp =    (Instance) main.instanceTableModel.getValueAt(tableInstances.convertRowIndexToModel(rows[i]), 5);
            
            File f = new File(path + System.getProperty("file.separator") + temp.getName());
            if(!f.exists())
                  InstanceDAO.getBinaryFileOfInstance(temp, f);
            String md5File = Util.calculateMD5(f);
            task.setStatus(i + " of " + rows.length + " instances are exported");
            task.setTaskProgress((float)i/(float)rows.length);
            if (!md5File.equals(temp.getMd5()))
                 md5Error.add(temp);
                 f.delete();
         }
 
         if(!md5Error.isEmpty()){
             InstanceTableModel tableModel = new InstanceTableModel();
             tableModel.addInstances(md5Error);
             EDACCExtendedWarning.showMessageDialog(EDACCExtendedWarning.OK_OPTIONS, EDACCApp.getApplication().getMainFrame(), "Following instances couldn't be written. Because the MD5checksum wasn't valid.", new JTable(tableModel));
 
 //            JFrame mainFrame = EDACCApp.getApplication().getMainFrame();
 //            EDACCExtWarningErrorDialog removeInstances = new EDACCExtWarningErrorDialog(mainFrame, true, false, tableModel,
 //                    "Following instances couldn't be written. Because the MD5checksum wasn't valid.");
 //            removeInstances.setLocationRelativeTo(mainFrame);
 //            EDACCApp.getApplication().show(removeInstances);
         }
     }
     
     /**
      * Sets all checkboxes of the instanceclass table true.
      */
     public void SelectAllInstanceClass() {
         for(int i = 0; i < main.instanceClassTableModel.getRowCount(); i++){
             main.instanceClassTableModel.setInstanceClassSelected(i);
         }
         main.instanceClassTableModel.setAll();
     }
 
     /**
      * Removes the given instance classes.
      * @param choosen The instance classes to remove.
      * @throws SQLException
      * @throws NoConnectionToDBException
      * @throws InstanceSourceClassHasInstance if one of the selected classes are a source class which has a refernce to an Instance.
      */
     public void RemoveInstanceClass(Vector<InstanceClass> choosen) throws SQLException, NoConnectionToDBException, InstanceSourceClassHasInstance {
         Boolean fail = false;
         for(int i = 0; i < choosen.size(); i++){
             try {
                 InstanceClassDAO.delete(choosen.get(i));
                 main.instanceClassTableModel.removeClass(choosen.get(i));
             } catch (InstanceSourceClassHasInstance ex) {
                 fail = true;
             }
         }
         if(fail) throw new InstanceSourceClassHasInstance();
     }
 
     /**
      * Opens a EDACCCreateEditInstanceClassDialog to create a new instance class.
      */
     public void addInstanceClasses() {
         JFrame mainFrame = EDACCApp.getApplication().getMainFrame();
         EDACCCreateEditInstanceClassDialog dialog = new EDACCCreateEditInstanceClassDialog(mainFrame, true, main.instanceClassTableModel, -1);
         dialog.setLocationRelativeTo(mainFrame);
         EDACCApp.getApplication().show(dialog);
     }
 
     /**
      * Builds the instances of the given files with the instance source class which was choosen by the
      * User.
      * @param instanceFiles
      * @param instanceClass
      * @return Vector<Instance> of the instances of the from files.
      * @throws FileNotFoundException
      * @throws NullPointerException
      * @throws NullPointerException
      * @throws IOException
      * @throws InstanceException
      * @throws NoSuchAlgorithmException
      * @throws SQLException
      */
     public Vector<Instance> buildInstancesGivenClass(Vector<File> instanceFiles, InstanceClass instanceClass, Tasks task)
             throws FileNotFoundException, NullPointerException, NullPointerException, IOException,
             NoSuchAlgorithmException, SQLException {
 
         Vector<Instance> instances = new Vector<Instance>();
         String duplicatesDB = "";
         StringBuilder instanceErrors = new StringBuilder("");
         int errCount = 0;
         int done = 0;
 
          task.setTaskProgress((float)0 / (float)instanceFiles.size());
         for (int i = 0; i < instanceFiles.size(); i++) {
             try {
                 String md5 = calculateMD5(instanceFiles.get(i));
                 try {
                     InstanceParser tempInstance = new InstanceParser(instanceFiles.get(i).getAbsolutePath());
                     Instance temp = InstanceDAO.createInstance(instanceFiles.get(i), tempInstance.name, tempInstance.n,
                             tempInstance.m, tempInstance.r, tempInstance.k, md5, instanceClass);
                     instances.add(temp);
                     InstanceDAO.save(temp);
                 } catch (InstanceException e) {
                     if (++errCount <= 20) { // show only first 20 errors
                         instanceErrors.append(instanceFiles.get(i).getName() + ": " + e.getMessage() + '\n');
                     }
                 }
             } catch (InstanceAlreadyInDBException ex) {
                 duplicatesDB += "\n " + instanceFiles.get(i).getAbsolutePath();
             }
              task.setTaskProgress((float)i / (float)instanceFiles.size());
              task.setStatus("Added " + i +" instances of " + instanceFiles.size() );
         }
 
         String instanceErrs = instanceErrors.toString();
         if (!"".equals(instanceErrs)) {
             String err = "The following errors occured while adding the instances. Added only instances without errors:\n\n" + instanceErrs;
             if (errCount > 20) {
                 err += "\n... and " + (errCount - 20) + " more errors";
             }
              JOptionPane.showMessageDialog(panelManageDBInstances,
                     err,
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
         }
 
         if(!duplicatesDB.equals("")){
              JOptionPane.showMessageDialog(panelManageDBInstances,
                     "The following instances are already in the database: " + duplicatesDB,
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
         }
         
         return instances;
     }
 
     /**
      * Builds the instances of the given files and autogenerates the instance source classes.
      * If a autogenerated instance source class already exists, the existing is used.
      * @param instanceFiles
      * @param ret
      * @return Vector<Instance> with all generated instances.
      * @throws FileNotFoundException
      * @throws IOException
      * @throws NoSuchAlgorithmException
      * @throws NullPointerException
      * @throws InstanceException
      * @throws SQLException
      */
     public Vector<Instance> buildInstancesAutogenerateClass(Vector<File> instanceFiles, File ret, Tasks task) throws FileNotFoundException, IOException, NoSuchAlgorithmException, NullPointerException, SQLException {
         
         Vector<Instance> instances = new Vector<Instance>();
         String duplicatesDB = "";
         StringBuilder instanceErrors = new StringBuilder("");
         InstanceClass instanceClass;
         int errCount = 0;
         task.setTaskProgress((float)0 / (float)instanceFiles.size());
         for (int i = 0; i < instanceFiles.size(); i++) {
             try {
                 String name = autogenerateInstanceClassName(ret.getParent(), instanceFiles.get(i));
                 try {
                     instanceClass = InstanceClassDAO.createInstanceClass(name, "Autogenerated instance source class", true);
                 } catch (InstanceClassAlreadyInDBException ex) {
                     instanceClass = InstanceClassDAO.getByName(name);
                 }
                 String md5 = calculateMD5(instanceFiles.get(i));
                 try {
                     InstanceParser tempInstance = new InstanceParser(instanceFiles.get(i).getAbsolutePath());
                     Instance temp = InstanceDAO.createInstance(instanceFiles.get(i), tempInstance.name, tempInstance.n, tempInstance.m, tempInstance.r, tempInstance.k, md5, instanceClass);
                     instances.add(temp);
                     InstanceDAO.save(temp);
                 }
                 catch (InstanceException e) {
                     if (++errCount <= 20) { // show only first 20 errors
                         instanceErrors.append(instanceFiles.get(i).getName() + ": " + e.getMessage() + '\n');
                     }
                 }
 
             } catch (InstanceAlreadyInDBException ex) {
                 duplicatesDB += "\n " + instanceFiles.get(i).getAbsolutePath();
             }
             task.setTaskProgress((float)i / (float)instanceFiles.size());
             task.setStatus("Added " + i +" instances of " + instanceFiles.size() );
         }
 
         String instanceErrs = instanceErrors.toString();
         if (!"".equals(instanceErrs)) {
             String err = "The following errors occured while adding the instances. Added only instances without errors:\n\n" + instanceErrs;
             if (errCount > 20) {
                 err += "\n... and " + (errCount - 20) + " more errors";
             }
              JOptionPane.showMessageDialog(panelManageDBInstances,
                     err,
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
         }
 
         if(!duplicatesDB.equals("")){
              JOptionPane.showMessageDialog(panelManageDBInstances,
                     "The following instances are already in the database: " + duplicatesDB,
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
         }
 
         return instances;
      
     }
 
     /**
      * Autogenerates the instance source class name. The name is generated by using the path of the file from
      * the choosenDirectory down to the parent directory of the instance as the name.
      * @param root
      * @param instanceFile
      * @return
      */
     public String autogenerateInstanceClassName(String root, File instanceFile) {
         return instanceFile.getAbsolutePath().substring(root.length()+1 , instanceFile.getParent().length());
     }
 
     /**
      * Adds the selected instances to a new instance user class or changes their instance source class.
      * This is decided by the user in the EDACCAddInstanceToInstanceClass Dialog.
      * @param selectedRows The rows of the selected instances
      */
     public void addInstancesToClass(int[] selectedRows){
          if(tableInstances.getSelectedRows().length == 0){
              JOptionPane.showMessageDialog(panelManageDBInstances,
                 "No instances selected.",
                 "Warning",
                 JOptionPane.WARNING_MESSAGE);
         } else {
 
             try {
                 JFrame mainFrame = EDACCApp.getApplication().getMainFrame();
                 EDACCAddInstanceToInstanceClass addInstanceToClass = new EDACCAddInstanceToInstanceClass(mainFrame, true);
                 addInstanceToClass.setLocationRelativeTo(mainFrame);
                 EDACCApp.getApplication().show(addInstanceToClass);
                 InstanceClass input = addInstanceToClass.getInput();
                 if (input != null) {
                     if (input.isSource()) {
                         for (int i = 0; i < selectedRows.length; i++) {
                             Instance temp = (Instance) main.instanceTableModel.getValueAt(selectedRows[i], 5);
                             temp.setInstanceClass(input);
                             temp.setModified();
                             InstanceDAO.save(temp);
                         }
                     } else {
                         for (int i = 0; i < selectedRows.length; i++) {
                             Instance temp = (Instance) main.instanceTableModel.getValueAt(selectedRows[i], 5);
                             InstanceHasInstanceClassDAO.createInstanceHasInstance(temp, input);
                         }
                     }
                     main.instanceClassTableModel.changeInstanceTable();
                 }
             } catch (FileNotFoundException ex) {
                 Logger.getLogger(ManageDBInstances.class.getName()).log(Level.SEVERE, null, ex);
             } catch (NoConnectionToDBException ex) {
                 JOptionPane.showMessageDialog(panelManageDBInstances,
                         ex.getMessage(),
                         "Error",
                         JOptionPane.ERROR_MESSAGE);
             } catch (SQLException ex) {
                 JOptionPane.showMessageDialog(panelManageDBInstances,
                         "There is a Problem with the database: " + ex.getMessage(),
                         "Error",
                         JOptionPane.ERROR_MESSAGE);
             }
         }
     }
 
     /**
      * Removes the choosen instances from the selected instance classes in the instanceClasstable.
      * If one of the selected instance classes is a source class a error message will occur.
      * @param selectedRowsInstance the instances to remove
      * @param selectedRowsInstanceClass rows of the InstanceClasses from which the selected instances have to be removed
      */
     public void RemoveInstanceFromInstanceClass(int[] selectedRowsInstance, int[] selectedRowsInstanceClass){
        // check if a instance is selected, if not notify the user
         if(tableInstances.getSelectedRows().length == 0){
              JOptionPane.showMessageDialog(panelManageDBInstances,
                 "No instances selected.",
                 "Warning",
                 JOptionPane.WARNING_MESSAGE);
         }else{
             Boolean isSource = false;
          
             try {
                 Vector<Instance> toRemove = new Vector<Instance>();
                 for(int i = 0; i < selectedRowsInstance.length; i++){
                     toRemove.add((Instance) main.instanceTableModel.getValueAt(tableInstances.convertRowIndexToModel(selectedRowsInstance[i]), 5));
                 }
                 // check if the user really want to remove the instances from the instace classes
                 InstanceTableModel tableModel = new InstanceTableModel();
                 tableModel.addInstances(toRemove);
 
 //                JFrame mainFrame = EDACCApp.getApplication().getMainFrame();
 //                EDACCExtWarningErrorDialog removeInstances = new EDACCExtWarningErrorDialog(mainFrame, true, true, tableModel,
 //                    "Do you really won't to remove the the listed instances from the selected instance classes?");
 //                removeInstances.setLocationRelativeTo(mainFrame);
 //                EDACCApp.getApplication().show(removeInstances);
 
                 // remove the instances from the instace classes
                 //if(removeInstances.isAccept()){
                 if(EDACCExtendedWarning.showMessageDialog(EDACCExtendedWarning.OK_CANCEL_OPTIONS,
                         EDACCApp.getApplication().getMainFrame(),
                         "Do you really won't to remove  the listed instances from the selected instance classes?",
                         new JTable(tableModel))==EDACCExtendedWarning.RET_OK_OPTION){
                    for(int i = 0; i < toRemove.size(); i++){
                        for(int j = 0; j < selectedRowsInstanceClass.length; j++){
                             InstanceClass tempInstanceClass = (InstanceClass) main.instanceClassTableModel.getValueAt(selectedRowsInstanceClass[j], 4);
                             if(tempInstanceClass.isSource()){
                                isSource = true;
                             }else{
                                     InstanceHasInstanceClass rem = InstanceHasInstanceClassDAO.getInstanceHasInstanceClass(tempInstanceClass, toRemove.get(i));
                                     if (rem != null) {
                                         InstanceHasInstanceClassDAO.removeInstanceHasInstanceClass(rem);
                                     }
                             }
                       }
                    }
                 }
 
                 if(isSource){
                     JOptionPane.showMessageDialog(panelManageDBInstances,
                         "Some of the choosen instance classes are source classes, " +
                         "the selected instances couldn't removed from them.",
                         "Error",
                         JOptionPane.ERROR_MESSAGE);
                 }
                 main.instanceClassTableModel.changeInstanceTable();
 
             } catch (NoConnectionToDBException ex) {
                 JOptionPane.showMessageDialog(panelManageDBInstances,
                     ex.getMessage(),
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                 JOptionPane.showMessageDialog(panelManageDBInstances,
                     "There is a Problem with the database: " + ex.getMessage(),
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
            }
         }
 
     }
 
     /**
      * Shows the dialog to configure a filter and add it to the instance table. If less than 2 instances are in the
      * instance table, no filter can be added. Selects the first entry in the instance table after adding the filter.
      */
     public void addFilter() {
 
         if(tableInstances.getRowCount() <= 1){
             JOptionPane.showMessageDialog(panelManageDBInstances,
                 "At least more than one instance is required in the instance table to add a filter.",
                 "Warning",
                 JOptionPane.WARNING_MESSAGE);
         }else{
             if(main.instanceFilter == null){
                 JFrame mainFrame = EDACCApp.getApplication().getMainFrame();
                 main.instanceFilter = new EDACCManageDBInstanceFilter(mainFrame,true);
                 main.instanceFilter.setLocationRelativeTo(mainFrame);
             }
             EDACCApp.getApplication().show(main.instanceFilter);
 
             Vector<RowFilter<Object, Object>> filters  = main.instanceFilter.getFilter();
             if(filters.isEmpty()){
                 removeFilter(tableInstances);
                 main.setFilterStatus("");
                 if(tableInstances.getRowCount() != 0)
                     tableInstances.addRowSelectionInterval(0, 0);
             }
             else{
                 main.sorter.setRowFilter(RowFilter.andFilter(filters));
                 tableInstances.setRowSorter(main.sorter);
                 main.instanceTableModel.fireTableDataChanged();
                 if(tableInstances.getRowCount() != 0)
                     tableInstances.addRowSelectionInterval(0, 0);
                 main.setFilterStatus("This list of instances has filters applied to it. Use the filter button below to modify.");
             }
         }
        
     }
 
     public void onTaskStart(String methodName) {
     }
 
     public void onTaskFailed(String methodName, Throwable e) {
     }
 
     public void onTaskSuccessful(String methodName, Object result) {
         if(methodName.equals("TryToRemoveInstances")){
             main.instanceTableModel.fireTableDataChanged();
         }
     }
 
     public void EditInstanceClass(InstanceClassTableModel instanceClassTableModel, int convertRowIndexToModel) {
         JFrame mainFrame = EDACCApp.getApplication().getMainFrame();
         EDACCCreateEditInstanceClassDialog dialog = new EDACCCreateEditInstanceClassDialog(mainFrame, true, instanceClassTableModel, convertRowIndexToModel);
         dialog.setLocationRelativeTo(mainFrame);
         EDACCApp.getApplication().show(dialog);
     }
 
 }
