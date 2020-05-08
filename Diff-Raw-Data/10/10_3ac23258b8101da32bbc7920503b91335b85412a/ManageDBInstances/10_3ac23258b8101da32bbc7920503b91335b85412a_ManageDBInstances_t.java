 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edacc.manageDB;
 
 import com.mysql.jdbc.Blob;
 import edacc.EDACCAddInstanceToInstanceClass;
 import edacc.EDACCAddNewInstanceSelectClassDialog;
 import edacc.EDACCApp;
 import edacc.EDACCCreateInstanceClassDialog;
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
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.security.NoSuchAlgorithmException;
 import java.sql.SQLException;
 import java.util.Vector;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTable;
 import javax.swing.RowFilter;
 import javax.swing.RowFilter.ComparisonType;
 
 /**
  *
  * @author rretz
  */
 public class ManageDBInstances {
 
     EDACCManageDBMode main;
     JPanel panelManageDBInstances;
     JFileChooser jFileChooserManageDBInstance;
     JFileChooser jFileChooserManageDBExportInstance;
 
     public ManageDBInstances(EDACCManageDBMode main, JPanel panelManageDBInstances, 
             JFileChooser jFileChooserManageDBInstance,  JFileChooser jFileChooserManageDBExportInstance ) {
         this.main = main;
         this.panelManageDBInstances = panelManageDBInstances;
         this.jFileChooserManageDBInstance = jFileChooserManageDBInstance;
         this.jFileChooserManageDBExportInstance = jFileChooserManageDBExportInstance;
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
 
     public void addInstances() {
         try {
             //Starts the dialog at which the user has to choose a instance source class or the autogeneration.
             if(main.addInstanceDialog == null){
                 JFrame mainFrame = EDACCApp.getApplication().getMainFrame();
                 main.addInstanceDialog = new EDACCAddNewInstanceSelectClassDialog(mainFrame, true);
                 main.addInstanceDialog.setLocationRelativeTo(mainFrame);
             }
             EDACCApp.getApplication().show(main.addInstanceDialog);
 
             InstanceClass input = main.addInstanceDialog.getInput();
             main.addInstanceDialog.dispose();
             //if the user doesn't cancel the dialog above, the fileChooser is shown.
             if(input != null){
                 
                 //When the user choos autogenerate only directorys can be choosen, else files and directorys.
                 if(input.getName().equals("")) jFileChooserManageDBInstance.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                 else jFileChooserManageDBInstance.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
 
                 int returnVal = jFileChooserManageDBInstance.showOpenDialog(panelManageDBInstances);
                 File ret = jFileChooserManageDBInstance.getSelectedFile();
                 RecursiveFileScanner InstanceScanner = new RecursiveFileScanner("cnf");
                 Vector<File> instanceFiles = InstanceScanner.searchFileExtension(ret);
                 if (instanceFiles.isEmpty()){
                     JOptionPane.showMessageDialog(panelManageDBInstances,
                             "No Instances have been found.",
                             "Error",
                             JOptionPane.WARNING_MESSAGE);
                 }
 
                 if(input.getName().equals("")){
                     Vector<Instance> instances;
  
                         instances = buildInstancesAutogenerateClass(instanceFiles, ret);
 
                     main.instanceTableModel.addInstances(instances);
                    loadInstanceClasses();
                 }else{
                     Vector<Instance> instances = buildInstancesGivenClass(instanceFiles, (InstanceClass)input);
                     main.instanceTableModel.addInstances(instances);
                     loadInstanceClasses();
                 }
                 main.instanceClassTableModel.changeInstanceTable();
             }
         } catch (NullPointerException ex) {
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
         } catch (NoSuchAlgorithmException ex) {
             JOptionPane.showMessageDialog(panelManageDBInstances,
                     "A problem with the MD5-algorithm has occured: " + ex.getMessage(),
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
         } catch (InstanceException ex) {
             JOptionPane.showMessageDialog(panelManageDBInstances,
                     ex.getMessage(),
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
         } catch (FileNotFoundException ex) {
             JOptionPane.showMessageDialog(panelManageDBInstances,
                     "Chosen file or directory not found: " + ex.getMessage(),
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
         } catch (IOException ex) {
             JOptionPane.showMessageDialog(panelManageDBInstances,
                     "Error reading chosen file or directory: " + ex.getMessage(),
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
         }
 
     }
 
     /**
      * Remove the given rows from the instanceTableModel
      * @param rows the rows which have to be deleted
      */
     public void removeInstances(int[] rows) throws NoConnectionToDBException, SQLException {
         Vector<Instance> rem = new Vector<Instance>();
         Vector<Instance> notRem = new Vector<Instance>();
         for (int i = 0; i < rows.length; i++) {
             Instance ins = (Instance) main.instanceTableModel.getValueAt(rows[i], 5);
             try {
                 InstanceDAO.delete(ins);
                 rem.add(ins);
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
 
     /*private Vector<Instance> buildInstances(Vector<File> instanceFiles)
             throws InstanceException, FileNotFoundException, NullPointerException, IOException, NoSuchAlgorithmException, NoConnectionToDBException, SQLException {
         Vector<Instance> instances = new Vector<Instance>();
         String duplicatesDB = "";
         for (int i = 0; i < instanceFiles.size(); i++) {
             try {
                 String md5 = calculateMD5(instanceFiles.get(i));
                 InstanceParser tempInstance = new InstanceParser(instanceFiles.get(i).getAbsolutePath());
                 Instance temp = InstanceDAO.createInstance(instanceFiles.get(i), tempInstance.name, tempInstance.n,
                         tempInstance.m, tempInstance.r, tempInstance.k, md5);{
                 instances.add(temp);
                 InstanceDAO.save(temp);
                 }
             } catch (InstanceAlreadyInDBException ex) {
                 duplicatesDB += "\n " + instanceFiles.get(i).getAbsolutePath();
             }
             
         }
         if(!duplicatesDB.equals("")){
              JOptionPane.showMessageDialog(panelManageDBInstances,
                     "The following instances are already in the database: " + duplicatesDB,
                     "Error",
                     JOptionPane.ERROR_MESSAGE);
         }
         return instances;
     }*/
 
 /**
  * Creates and add a andFilter with the given values to the sorter of EDACCManageDBMode
  * @param name
  * @param numOfAtomsMin
  * @param numOfAtomsMax
  * @param numOfClausesMin
  * @param numOfClausesMax
  * @param ratioMin
  * @param ratioMax
  * @param maxClauseLengthMin
  * @param maxClauseLengthMax
  */
     public void newFilter(String name, String numOfAtomsMin, String numOfAtomsMax, String numOfClausesMin,
             String numOfClausesMax, String ratioMin, String ratioMax, String maxClauseLengthMin,
             String maxClauseLengthMax) {
         Vector<RowFilter<Object, Object>> filters = new Vector<RowFilter<Object, Object>>();
         filters.add(RowFilter.regexFilter(name, 0));
         if(!numOfAtomsMin.isEmpty())filters.add(RowFilter.numberFilter(ComparisonType.AFTER, Integer.parseInt(numOfAtomsMin), 1));
         if(!numOfAtomsMax.isEmpty()) filters.add(RowFilter.numberFilter(ComparisonType.BEFORE, Integer.parseInt(numOfAtomsMax), 1));
         if(!numOfClausesMin.isEmpty())filters.add(RowFilter.numberFilter(ComparisonType.AFTER, Integer.parseInt(numOfClausesMin), 2));
         if(!numOfClausesMax.isEmpty())filters.add(RowFilter.numberFilter(ComparisonType.BEFORE, Integer.parseInt(numOfClausesMax), 2));
         if(!ratioMin.isEmpty())filters.add(RowFilter.numberFilter(ComparisonType.AFTER, Float.parseFloat(ratioMin), 3));
         if(!ratioMax.isEmpty())filters.add(RowFilter.numberFilter(ComparisonType.BEFORE, Float.parseFloat(ratioMax), 3));
         if(!maxClauseLengthMin.isEmpty())filters.add(RowFilter.numberFilter(ComparisonType.AFTER, Integer.parseInt(maxClauseLengthMin), 4));
         if(!maxClauseLengthMax.isEmpty())filters.add(RowFilter.numberFilter(ComparisonType.BEFORE, Integer.parseInt(maxClauseLengthMax), 4));
         RowFilter<Object, Object> filter = RowFilter.andFilter(filters);
         main.sorter.setRowFilter(filter);
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
     public void exportInstances(int[] rows) throws IOException, NoConnectionToDBException, SQLException, 
             InstaceNotInDBException, FileNotFoundException, MD5CheckFailedException,
             NoSuchAlgorithmException{
 
         int returnVal = jFileChooserManageDBExportInstance.showOpenDialog(panelManageDBInstances);
         String path = jFileChooserManageDBExportInstance.getSelectedFile().getAbsolutePath();
         Instance temp;
         for(int i = 0; i < rows.length; i++){
            temp =    (Instance) main.instanceTableModel.getValueAt(rows[i], 5);
            File f = new File(path + System.getProperty("file.separator") + temp.getName());
            InstanceDAO.getBinaryFileOfInstance(temp, f);
            String md5File = Util.calculateMD5(f);
            if (!md5File.equals(temp.getMd5()))
                 throw new MD5CheckFailedException("The exported solver binary of solver \"" + temp.getName() + "\" seems to be corrupt!");         
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
      * Removes the selected instance classes.
      * @param selectedRows The instance classes to remove.
      * @throws SQLException
      * @throws NoConnectionToDBException
      * @throws InstanceSourceClassHasInstance if one of the selected classes are a source class which has a refernce to an Instance.
      */
     public void RemoveInstanceClass(int[] selectedRows) throws SQLException, NoConnectionToDBException, InstanceSourceClassHasInstance {
         InstanceClass instanceClass;
         Boolean fail = false;
         for(int i = 0; i < selectedRows.length; i++){
            instanceClass = (InstanceClass) main.instanceClassTableModel.getValueAt(selectedRows[i], 4);
             try {
                 InstanceClassDAO.delete(instanceClass);
                 main.instanceClassTableModel.classes.remove(instanceClass);
                 main.instanceClassTableModel.classSelect.remove(i);
             } catch (InstanceSourceClassHasInstance ex) {
                 fail = true;
             }
         }
         if(fail) throw new InstanceSourceClassHasInstance();
     }
 
     /**
      * Opens a EDACCCreateInstanceClassDialog to create a new instance class.
      */
     public void addInstanceClasses() {
         if(main.createInstanceClassDialog == null){
             JFrame mainFrame = EDACCApp.getApplication().getMainFrame();
             main.createInstanceClassDialog = new EDACCCreateInstanceClassDialog(mainFrame, true, main.instanceClassTableModel);
             main.createInstanceClassDialog.setLocationRelativeTo(mainFrame);
         }
         EDACCApp.getApplication().show(main.createInstanceClassDialog);
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
     private Vector<Instance> buildInstancesGivenClass(Vector<File> instanceFiles, InstanceClass instanceClass)
             throws FileNotFoundException, NullPointerException, NullPointerException, IOException,
             InstanceException, NoSuchAlgorithmException, SQLException {
 
         Vector<Instance> instances = new Vector<Instance>();
         String duplicatesDB = "";
 
         for (int i = 0; i < instanceFiles.size(); i++) {
 
             try {
                 String md5 = calculateMD5(instanceFiles.get(i));
                 InstanceParser tempInstance = new InstanceParser(instanceFiles.get(i).getAbsolutePath());
                 Instance temp = InstanceDAO.createInstance(instanceFiles.get(i), tempInstance.name, tempInstance.n,
                         tempInstance.m, tempInstance.r, tempInstance.k, md5, instanceClass);{
                 instances.add(temp);
                 InstanceDAO.save(temp);
                 }
             } catch (InstanceAlreadyInDBException ex) {
                 duplicatesDB += "\n " + instanceFiles.get(i).getAbsolutePath();
             }
 
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
     private Vector<Instance> buildInstancesAutogenerateClass(Vector<File> instanceFiles, File ret) throws FileNotFoundException, IOException, NoSuchAlgorithmException, NullPointerException, InstanceException, SQLException {
         
         Vector<Instance> instances = new Vector<Instance>();
         String duplicatesDB = "";
         InstanceClass instanceClass;
 
         for (int i = 0; i < instanceFiles.size(); i++) {
             try {
                 String name = autogenerateInstanceClassName(ret.getParent(), instanceFiles.get(i));
                 try {
                     instanceClass = InstanceClassDAO.createInstanceClass(name, "Autogernerated instance source class", true);
                 } catch (InstanceClassAlreadyInDBException ex) {
                     instanceClass = InstanceClassDAO.getByName(name);
                 }
                 String md5 = calculateMD5(instanceFiles.get(i));
                 InstanceParser tempInstance = new InstanceParser(instanceFiles.get(i).getAbsolutePath());
                 Instance temp = InstanceDAO.createInstance(instanceFiles.get(i), tempInstance.name, tempInstance.n, tempInstance.m, tempInstance.r, tempInstance.k, md5, instanceClass);
                 {
                     instances.add(temp);
                     InstanceDAO.save(temp);
                 }
             } catch (InstanceAlreadyInDBException ex) {
                 Logger.getLogger(ManageDBInstances.class.getName()).log(Level.SEVERE, null, ex);
             }
 
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
     private String autogenerateInstanceClassName(String root, File instanceFile) {
         return instanceFile.getAbsolutePath().substring(root.length()+1 , instanceFile.getParent().length());
     }
 
     /**
      * Adds the selected instances to a new instance user class or changes their instance source class.
      * This is decided by the user in the EDACCAddInstanceToInstanceClass Dialog.
      * @param selectedRows The rows of the selected instances
      */
     public void addInstancesToClass(int[] selectedRows){
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
 
     /**
      * Removes the choosen instances from the selected instance classes in the instanceClasstable.
      * If one of the selected instance classes is a source class a error message will occur.
      * @param selectedRowsInstance
      */
     public void RemoveInstanceFromInstanceClass(int[] selectedRowsInstance){
         Vector<InstanceClass> instanceClass = main.instanceClassTableModel.getAllChoosen();
         Boolean isSource = false;
         try {
             for(int i = 0; i < selectedRowsInstance.length; i++){
                 Instance tempInstance = (Instance) main.instanceTableModel.getValueAt(i, 5);
                 for(int j = 0; j < instanceClass.size(); j++){
                     InstanceClass tempInstanceClass = instanceClass.get(j);
                     if(tempInstanceClass.isSource()){
                        isSource = true;
                     }else{
                             InstanceHasInstanceClass rem = InstanceHasInstanceClassDAO.getInstanceHasInstanceClass(tempInstanceClass, tempInstance);
                             if (rem != null) {
                                 InstanceHasInstanceClassDAO.removeInstanceHasInstanceClass(rem);
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
