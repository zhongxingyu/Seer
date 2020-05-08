 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edacc.manageDB;
 
 import edacc.EDACCAddInstanceToInstanceClass;
 import edacc.EDACCApp;
 import edacc.EDACCCreateEditInstanceClassDialog;
 import edacc.EDACCExtendedWarning;
 import edacc.EDACCManageDBMode;
 import edacc.experiment.ExperimentTableModel;
 import edacc.model.ComputationMethodDoesNotExistException;
 import edacc.model.DatabaseConnector;
 import edacc.model.Experiment;
 import edacc.model.ExperimentHasInstanceDAO;
 import edacc.model.InstanceClassAlreadyInDBException;
 import edacc.model.InstanceNotInDBException;
 import edacc.model.Instance;
 import edacc.model.InstanceAlreadyInDBException;
 import edacc.model.InstanceClass;
 import edacc.model.InstanceClassDAO;
 
 import edacc.model.InstanceDAO;
 import edacc.model.InstanceHasInstanceClass;
 import edacc.model.InstanceHasInstanceClassDAO;
 import edacc.model.InstanceIsInExperimentException;
 import edacc.model.InstanceSourceClassHasInstance;
 import edacc.model.MD5CheckFailedException;
 import edacc.model.NoConnectionToDBException;
 import edacc.model.Property;
 import edacc.model.PropertyDAO;
 import edacc.model.PropertyNotInDBException;
 import edacc.model.TaskCancelledException;
 import edacc.model.Tasks;
 import edacc.properties.PropertyComputationController;
 import edacc.properties.PropertyTypeNotExistException;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.security.NoSuchAlgorithmException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Vector;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTable;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.TreePath;
 
 /**
  *
  * @author rretz
  */
 public class ManageDBInstances implements Observer {
 
     EDACCManageDBMode main;
     JPanel panelManageDBInstances;
     JFileChooser jFileChooserManageDBInstance;
     JFileChooser jFileChooserManageDBExportInstance;
     JTable tableInstances;
     Lock lock = new ReentrantLock();
     Condition condition;
     private Vector<Instance> tmp;
 
     public void setTmp(Vector<Instance> tmp) {
         this.tmp = tmp;
     }
 
     public Vector<Instance> getTmp() {
         return tmp;
     }
 
     public ManageDBInstances(EDACCManageDBMode main, JPanel panelManageDBInstances,
             JFileChooser jFileChooserManageDBInstance, JFileChooser jFileChooserManageDBExportInstance,
             JTable tableInstances) {
         this.main = main;
         this.panelManageDBInstances = panelManageDBInstances;
         this.jFileChooserManageDBInstance = jFileChooserManageDBInstance;
         this.jFileChooserManageDBExportInstance = jFileChooserManageDBExportInstance;
         this.tableInstances = tableInstances;
         DatabaseConnector.getInstance().addObserver(this);
         this.tmp = new Vector<Instance>();
     }
 
     /**
      * Load all instances from the database into the instancetable
      * @throws NoConnectionToDBException
      * @throws SQLException
      */
     public void loadInstances() {
         try {
             main.instanceTableModel.instances.clear();
             main.instanceTableModel.initInstances(new Vector<Instance>(InstanceDAO.getAll()));
         } catch (SQLException ex) {
             Logger.getLogger(ManageDBInstances.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(ManageDBInstances.class.getName()).log(Level.SEVERE, null, ex);
         } catch (PropertyNotInDBException ex) {
             Logger.getLogger(ManageDBInstances.class.getName()).log(Level.SEVERE, null, ex);
         } catch (PropertyTypeNotExistException ex) {
             Logger.getLogger(ManageDBInstances.class.getName()).log(Level.SEVERE, null, ex);
         } catch (ComputationMethodDoesNotExistException ex) {
             Logger.getLogger(ManageDBInstances.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public void loadInstanceClasses() throws SQLException {
         DefaultMutableTreeNode root = (DefaultMutableTreeNode) InstanceClassDAO.getAllAsTreeFast();
         main.instanceClassTreeModel.setRoot(root);
         main.getInstanceClassTree().setRootVisible(false);
         //main.instanceClassTreeModel.nodesWereInserted(((DefaultMutableTreeNode)main.instanceClassTreeModel.getRoot()), new int[] {((DefaultMutableTreeNode)main.instanceClassTreeModel.getRoot()).getIndex(child)});
 
         /* main.instanceClassTableModel.classes.clear();
         main.instanceClassTableModel.classSelect.clear();
         main.instanceClassTableModel.addClasses(new Vector<InstanceClass>(InstanceClassDAO.getAll()));*/
     }
 
     /**
      * Will open a jFilechooser to select a file or directory to add all containing
      * instance files into the "instance table" of the MangeDBMode.
      */
     public void addInstances(InstanceClass input, File ret, Tasks task, String fileExtension, Boolean compress, Boolean autoClass) throws InstanceException, TaskCancelledException {
         try {
             Tasks.getTaskView().setCancelable(true);
             RecursiveFileScanner InstanceScanner = new RecursiveFileScanner(fileExtension);
             Vector<File> instanceFiles = InstanceScanner.searchFileExtension(ret);
             task.setOperationName("Adding Instances");
             if (input.getName().equals("")) {
                 Vector<Instance> instances;
                 instances = buildInstancesAutogenerateClass(instanceFiles, ret, task, compress);
                 main.instanceTableModel.addNewInstances(instances);
             } else {
                 Vector<Instance> instances;
                 if (autoClass) {
                     instances = buildInstancesGivenClassAutogenerate(instanceFiles, (InstanceClass) input, ret, task, compress);
                 } else {
                     instances = buildInstancesGivenClass(instanceFiles, (InstanceClass) input, task, compress);
                 }
 
 
                 main.instanceTableModel.addNewInstances(instances);
                 updateInstanceClasses();
                 main.updateInstanceTable();
                 Tasks.getTaskView().setCancelable(false);
             }
         } catch (FileNotFoundException ex) {
             Logger.getLogger(ManageDBInstances.class.getName()).log(Level.SEVERE, null, ex);
         } catch (InstanceClassAlreadyInDBException ex) {
             Logger.getLogger(ManageDBInstances.class.getName()).log(Level.SEVERE, null, ex);
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
 
     /**
      * Removes all instances from the instancetable
      */
     public void removeAllInstancesFromTable() {
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
      * @throws InstanceNotInDBException
      */
     public void exportInstances(int[] rows, String path, Tasks task) throws IOException, NoConnectionToDBException, SQLException,
             InstanceNotInDBException, FileNotFoundException, MD5CheckFailedException,
             NoSuchAlgorithmException, TaskCancelledException {
         task.setOperationName("Exporting instances");
         Tasks.getTaskView().setCancelable(true);
         Instance temp;
         Vector<Instance> md5Error = new Vector<Instance>();
         for (int i = 0; i < rows.length; i++) {
             if (task.isCancelled()) {
                 throw new TaskCancelledException();
             }
             temp = (Instance) main.instanceTableModel.getInstance(tableInstances.convertRowIndexToModel(rows[i]));
 
             File f = new File(path + System.getProperty("file.separator") + temp.getName());
             if (!f.exists()) {
                 InstanceDAO.getBinaryFileOfInstance(temp, f);
             }
             String md5File = Util.calculateMD5(f);
             task.setStatus(i + " of " + rows.length + " instances are exported");
             task.setTaskProgress((float) i / (float) rows.length);
             if (!md5File.equals(temp.getMd5())) {
                 md5Error.add(temp);
                 f.delete();
             }
         }
         Tasks.getTaskView().setCancelable(false);
 
         if (!md5Error.isEmpty()) {
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
     public void selectAllInstanceClass(Tasks task) {
         /*   for(int i = 0; i < main.instanceClassTableModel.getRowCount(); i++){
         task.setStatus(i + " of " + main.instanceClassTableModel.getRowCount() + " instance classes are loaded.");
         task.setTaskProgress((float)i/(float)main.instanceClassTableModel.getRowCount());
         main.instanceClassTableModel.seSelected(i);
         }
         try {
         main.instanceClassTableModel.changeInstanceTable();
         } catch (NoConnectionToDBException ex) {
         Logger.getLogger(ManageDBInstances.class.getName()).log(Level.SEVERE, null, ex);
         } catch (SQLException ex) {
         Logger.getLogger(ManageDBInstances.class.getName()).log(Level.SEVERE, null, ex);
         }*/
     }
 
     public void deselectAllInstanceClass() {
         /* main.instanceClassTableModel.DeselectAll();
         main.instanceTableModel.clearTable();
         main.instanceClassTableModel.fireTableDataChanged();*/
     }
 
     /**
      * Removes the given instance classes.
      * @param choosen The instance classes to remove.
      * @throws SQLException
      * @throws NoConnectionToDBException
      * @throws InstanceSourceClassHasInstance if one of the selected classes are a source class which has a refernce to an Instance.
      */
     public void removeInstanceClass(DefaultMutableTreeNode node) throws SQLException, NoConnectionToDBException, InstanceSourceClassHasInstance, InstanceIsInExperimentException {
         Vector<InstanceClass> toRemove = new Vector<InstanceClass>();
         toRemove = getAllToEnd(node);
         AddInstanceInstanceClassTableModel tableModel = new AddInstanceInstanceClassTableModel();
         tableModel.addClasses(new Vector<InstanceClass>(toRemove));
         if (EDACCExtendedWarning.showMessageDialog(EDACCExtendedWarning.OK_CANCEL_OPTIONS,
                 EDACCApp.getApplication().getMainFrame(),
                 "Do you really won't to remove the listed instance classes?",
                 new JTable(tableModel))
                 == EDACCExtendedWarning.RET_OK_OPTION) {
             Vector<Instance> lastRelated = InstanceClassDAO.checkIfEmpty(toRemove);
 
             /* Are the instances, related to the classes to remove, related to other class? If not, ask if the instances have to be deleted
              * else delte the classes
              */
             if (lastRelated.isEmpty()) {
                 Vector<InstanceClass> errors = new Vector<InstanceClass>();
                 for (int i = 0; i < toRemove.size(); i++) {
                     try {
                         InstanceClassDAO.delete(toRemove.get(i));
                     } catch (InstanceSourceClassHasInstance ex) {
                         errors.add(toRemove.get(i));
                     }
                 }
                 //ReloadInstanceClasses();
                 if (!errors.isEmpty()) {
                     tableModel = new AddInstanceInstanceClassTableModel();
                     tableModel.addClasses(errors);
                     EDACCExtendedWarning.showMessageDialog(EDACCExtendedWarning.OK_OPTIONS,
                             EDACCApp.getApplication().getMainFrame(),
                             "A Problem occured by removing the following instance classes.  \n ",
                             new JTable(tableModel));
                 }
             } else {
                 // List the Experiments to which the instances, which are dangered related to and ask to remove these isntances.
                 ExperimentTableModel expTableModel = new ExperimentTableModel(true);
                 ArrayList<Experiment> inExp = ExperimentHasInstanceDAO.getAllExperimentsByInstances(lastRelated);
                 if (!inExp.isEmpty()) {
                     expTableModel.setExperiments(inExp);
                     if (EDACCExtendedWarning.showMessageDialog(EDACCExtendedWarning.OK_CANCEL_OPTIONS,
                             EDACCApp.getApplication().getMainFrame(),
                             "If you remove the instance classes, instances used in den following experiments will be deleted too. \n"
                             + "Do you really want to remove the selected instance classes?",
                             new JTable(expTableModel))
                             == EDACCExtendedWarning.RET_OK_OPTION) {
                         deleteInstanceClasssAndInstances(lastRelated, toRemove);
                     }
                 } else {
                     deleteInstanceClasssAndInstances(lastRelated, toRemove);
                 }
 
                 return;
             }
         }
     }
 
     /**
      * Opens a EDACCCreateEditInstanceClassDialog to create a new instance class.
      */
     public void addInstanceClasses() {
         JFrame mainFrame = EDACCApp.getApplication().getMainFrame();
         EDACCCreateEditInstanceClassDialog dialog = new EDACCCreateEditInstanceClassDialog(mainFrame, true, main.getInstanceClassTree());
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
     public Vector<Instance> buildInstancesGivenClass(Vector<File> instanceFiles, InstanceClass instanceClass, Tasks task, boolean compressBinary)
             throws FileNotFoundException, NullPointerException, NullPointerException, IOException,
             NoSuchAlgorithmException, SQLException, TaskCancelledException {
 
         Vector<Instance> instances = new Vector<Instance>();
         String duplicatesDB = "";
         Vector<String> errorsDB = new Vector<String>();
         Vector<String> errorsAdd = new Vector<String>();
         StringBuilder instanceErrors = new StringBuilder("");
         Vector<String> errorsDBInstances = new Vector<String>();
         int errCount = 0;
         int done = 0;
 
         task.setTaskProgress((float) 0 / (float) instanceFiles.size());
         String md5 = "";
         for (int i = 0; i < instanceFiles.size(); i++) {
             if (task.isCancelled()) {
                 throw new TaskCancelledException();
             }
             try {
                 md5 = calculateMD5(instanceFiles.get(i));
                 String fileName = instanceFiles.get(i).getName();
                 Instance temp = InstanceDAO.createInstance(instanceFiles.get(i), fileName, md5);
                 instances.add(temp);
                 InstanceDAO.save(temp, compressBinary, instanceClass);
                 this.tmp.add(temp);
             } catch (InstanceAlreadyInDBException ex) {
                 errorsDB.add(instanceFiles.get(i).getAbsolutePath());
                 errorsDBInstances.add(md5);
             }
             task.setTaskProgress((float) i / (float) instanceFiles.size());
             task.setStatus("Added " + i + " instances of " + instanceFiles.size());
         }
 
         setTmp(new Vector<Instance>());
         String instanceErrs = instanceErrors.toString();
         if (!errorsAdd.isEmpty()) {
             FileNameTableModel tmp = new FileNameTableModel();
             tmp.setAll(errorsAdd);
             EDACCExtendedWarning.showMessageDialog(
                     EDACCExtendedWarning.OK_OPTIONS, EDACCApp.getApplication().getMainFrame(),
                     "By adding the following instances an error occured.",
                     new JTable(tmp));
         }
 
         if (!errorsDB.isEmpty()) {
             FileNameTableModel tmp = new FileNameTableModel();
             tmp.setAll(errorsDB);
             if (EDACCExtendedWarning.showMessageDialog(
                     EDACCExtendedWarning.OK_CANCEL_OPTIONS, EDACCApp.getApplication().getMainFrame(),
                     "The following instances are already in the database. (Equal name or md5 hash). \\n"
                     + "Do you want to add the instances to the selected/autobuilded classes?",
                     new JTable(tmp)) == EDACCExtendedWarning.RET_OK_OPTION) {
                 for (int i = 0; i < errorsDBInstances.size(); i++) {
                     InstanceHasInstanceClassDAO.createInstanceHasInstance(InstanceDAO.getByMd5(errorsDBInstances.get(i)), instanceClass);
                 }
             }
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
     public Vector<Instance> buildInstancesAutogenerateClass(Vector<File> instanceFiles, File ret, Tasks task, boolean compressBinary) throws FileNotFoundException, IOException, NoSuchAlgorithmException, NullPointerException, SQLException, InstanceClassAlreadyInDBException, InstanceException, TaskCancelledException {
 
         if (instanceFiles.isEmpty()) {
             throw new InstanceException();
         }
         Vector<Instance> instances = new Vector<Instance>();
         Vector<String> errorsDB = new Vector<String>();
         Vector<String> errorsAdd = new Vector<String>();
         Vector<File> errorsDBInstances = new Vector<File>();
         InstanceClass instanceClass;
         task.setTaskProgress((float) 0 / (float) instanceFiles.size());
 
         //Creates all InstanceClasses in the directory and structures them as a tree.
         DefaultMutableTreeNode nodes = InstanceClassDAO.createInstanceClassFromDirectory(ret);
 
         //Create the instances and assigne them to their InstanceClass
         for (int i = 0; i < instanceFiles.size(); i++) {
             if (task.isCancelled()) {
                 throw new TaskCancelledException();
             }
             try {
                 String md5 = calculateMD5(instanceFiles.get(i));
 
                 //Get all InstanceClasses of the Instance and assigne it to the corresponding InstanceClass
                 String rawPath = instanceFiles.get(i).getAbsolutePath().substring(ret.getParent().length() + 1, instanceFiles.get(i).getParent().length());
                 String[] possibleInstanceClasses = rawPath.split("\\\\|/");
                 if (possibleInstanceClasses.length != 1) {
                     instanceClass = getInstanceClassFromTree(possibleInstanceClasses, nodes, 0);
                 } else {
                     instanceClass = (InstanceClass) ((DefaultMutableTreeNode) nodes.getRoot()).getUserObject();
                 }
                 Instance temp = InstanceDAO.createInstance(instanceFiles.get(i), instanceFiles.get(i).getName(), md5);
                 instances.add(temp);
                 InstanceDAO.save(temp, compressBinary, instanceClass);
                 this.tmp.add(temp);
 
             } catch (InstanceAlreadyInDBException ex) {
                 errorsDB.add(instanceFiles.get(i).getAbsolutePath());
                 errorsDBInstances.add(instanceFiles.get(i));
             }
             task.setTaskProgress((float) i / (float) instanceFiles.size());
             task.setStatus("Added " + i + " instances of " + instanceFiles.size());
         }
         setTmp(new Vector<Instance>());
         if (!errorsAdd.isEmpty()) {
             FileNameTableModel tmp = new FileNameTableModel();
             tmp.setAll(errorsAdd);
             EDACCExtendedWarning.showMessageDialog(
                     EDACCExtendedWarning.OK_OPTIONS, EDACCApp.getApplication().getMainFrame(),
                     "By adding the following instances an error occured.",
                     new JTable(tmp));
         }
 
         if (!errorsDB.isEmpty()) {
             FileNameTableModel tmp = new FileNameTableModel();
             tmp.setAll(errorsDB);
             task.cancel(true);
             if (EDACCExtendedWarning.showMessageDialog(
                     EDACCExtendedWarning.OK_CANCEL_OPTIONS, EDACCApp.getApplication().getMainFrame(),
                     "The following instances are already in the database. (Equal name or md5 hash). \n"
                     + "Do you want to add the instances to the selected/autobuilded classes?",
                     new JTable(tmp)) == EDACCExtendedWarning.RET_OK_OPTION) {
                 addInstancesToAutoBuildedInstances(errorsDBInstances, nodes, ret);
             }
 
         }
         return instances;
 
     }
 
     /**
      * 
      * @param instanceFiles
      * @param input
      * @param ret
      * @param task
      * @param compressBinary
      * @return
      * @throws FileNotFoundException
      * @throws IOException
      * @throws NoSuchAlgorithmException
      * @throws NullPointerException
      * @throws SQLException
      * @throws InstanceClassAlreadyInDBException
      * @throws InstanceException
      * @throws TaskCancelledException 
      */
     public Vector<Instance> buildInstancesGivenClassAutogenerate(Vector<File> instanceFiles, InstanceClass input, File ret, Tasks task, boolean compressBinary) throws FileNotFoundException, IOException, NoSuchAlgorithmException, NullPointerException, SQLException, InstanceClassAlreadyInDBException, InstanceException, TaskCancelledException {
 
         if (instanceFiles.isEmpty()) {
             throw new InstanceException();
         }
 
         Vector<Instance> instances = new Vector<Instance>();
         Vector<String> errorsDB = new Vector<String>();
         Vector<String> errorsAdd = new Vector<String>();
         InstanceClass instanceClass;
         Vector<File> errorsDBInstances = new Vector<File>();
         task.setTaskProgress((float) 0 / (float) instanceFiles.size());
 
         //Creates all InstanceClasses in the directory and structures them as a tree. The given InstanceClass is the root of this tree.
         DefaultMutableTreeNode nodes = InstanceClassDAO.createInstanceClassFromDirectory(ret, input);
 
         //Create the instances and assigne them to their InstanceClass
         for (int i = 0; i < instanceFiles.size(); i++) {
             if (task.isCancelled()) {
                 throw new TaskCancelledException();
             }
 
             String md5 = calculateMD5(instanceFiles.get(i));
             try {
                 //Get all InstanceClasses of the Instance and assigne it to the corresponding InstanceClass    
                 String rawPath = instanceFiles.get(i).getAbsolutePath().substring(ret.getParent().length() + 1, instanceFiles.get(i).getParent().length());
                 String[] possibleInstanceClasses = rawPath.split("\\\\|/");
                 if (possibleInstanceClasses.length != 1) {
                     instanceClass = getInstanceClassFromTree(possibleInstanceClasses, nodes, 0);
                 } else {
                     instanceClass = (InstanceClass) ((DefaultMutableTreeNode) nodes.getRoot()).getUserObject();
                 }
                 Instance temp = InstanceDAO.createInstance(instanceFiles.get(i), instanceFiles.get(i).getName(), md5);
                 instances.add(temp);
                 InstanceDAO.save(temp, compressBinary, instanceClass);
                 this.tmp.add(temp);
 
             } catch (InstanceAlreadyInDBException ex) {
                 errorsDB.add(instanceFiles.get(i).getAbsolutePath());
                 errorsDBInstances.add(instanceFiles.get(i));
             }
             task.setTaskProgress((float) i / (float) instanceFiles.size());
             task.setStatus("Added " + i + " instances of " + instanceFiles.size());
         }
 
         setTmp(new Vector<Instance>());
 
         if (!errorsAdd.isEmpty()) {
             FileNameTableModel tmp = new FileNameTableModel();
             tmp.setAll(errorsAdd);
             EDACCExtendedWarning.showMessageDialog(
                     EDACCExtendedWarning.OK_OPTIONS, EDACCApp.getApplication().getMainFrame(),
                     "By adding the following instances an error occured.",
                     new JTable(tmp));
         }
 
         if (!errorsDB.isEmpty()) {
             FileNameTableModel tmp = new FileNameTableModel();
             tmp.setAll(errorsDB);
             task.cancel(true);
             if (EDACCExtendedWarning.showMessageDialog(
                     EDACCExtendedWarning.OK_CANCEL_OPTIONS, EDACCApp.getApplication().getMainFrame(),
                     "The following instances are already in the database. (Equal name or md5 hash). \n"
                     + "Do you want to add the instances to the selected/autobuilded classes?",
                     new JTable(tmp)) == EDACCExtendedWarning.RET_OK_OPTION) {
                 addInstancesToAutoBuildedInstances(errorsDBInstances, nodes, ret);
             }
         }
         return instances;
     }
 
     private InstanceClass getInstanceClassFromTree(String[] possibleInstanceClasses, DefaultMutableTreeNode node, int i) throws SQLException, InstanceClassAlreadyInDBException {
         i++;
         if (i == possibleInstanceClasses.length - 1) {
 
             for (int j = 0; j < node.getChildCount(); j++) {
                 InstanceClass tmp = (InstanceClass) ((DefaultMutableTreeNode) node.getChildAt(j)).getUserObject();
                 if (tmp.getName().equals(possibleInstanceClasses[i])) {
                     return tmp;
                 }
             }
             DefaultMutableTreeNode tmp = new DefaultMutableTreeNode(InstanceClassDAO.createInstanceClass(possibleInstanceClasses[(i)], "Autogenerated instance class", (InstanceClass) node.getUserObject()));
             node.add(tmp);
             return (InstanceClass) tmp.getUserObject();
 
 
         } else {
             for (int j = 0; j < node.getChildCount(); j++) {
                 InstanceClass tmp = (InstanceClass) ((DefaultMutableTreeNode) node.getChildAt(j)).getUserObject();
                 if (tmp.getName().equals(possibleInstanceClasses[i])) {
                     return getInstanceClassFromTree(possibleInstanceClasses, (DefaultMutableTreeNode) node.getChildAt(j), i);
                 }
             }
             DefaultMutableTreeNode tmp = new DefaultMutableTreeNode(InstanceClassDAO.createInstanceClass(possibleInstanceClasses[(i)], "Autogenerated instance class", (InstanceClass) node.getUserObject()));
             node.add(tmp);
             return getInstanceClassFromTree(possibleInstanceClasses, (DefaultMutableTreeNode) tmp, i);
 
         }
     }
 
     private DefaultMutableTreeNode autoGenerateInstanceClasses(Vector<File> files, File root) throws SQLException, InstanceClassAlreadyInDBException {
         DefaultMutableTreeNode node = null;
         for (int i = 0; i < files.size(); i++) {
             String rawPath = files.get(i).getAbsolutePath().substring(root.getParent().length() + 1, files.get(i).getParent().length());
             String[] possibleInstanceClasses = rawPath.split("\\\\|/");
             if (node == null) {
                 node = new DefaultMutableTreeNode(InstanceClassDAO.createInstanceClass(possibleInstanceClasses[i], "Autogenerated instance source class", null));
             }
             for (int j = 1; j < possibleInstanceClasses.length; j++) {
                 addInstanceClassToTree(node, j, possibleInstanceClasses[j]);
             }
         }
         return node;
     }
 
     private void addInstanceClassToTree(DefaultMutableTreeNode node, int j, String name) throws SQLException, InstanceClassAlreadyInDBException {
         j--;
         if (j == 0) {
             for (int i = 0; i < node.getChildCount(); i++) {
                 DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
                 if (((InstanceClass) child.getUserObject()).getName().equals(name)) {
                     return;
                 }
             }
             node.add(new DefaultMutableTreeNode(InstanceClassDAO.createInstanceClass(name, "Autogenerated instance source class", (InstanceClass) node.getUserObject())));
         } else {
             for (int i = 0; i < node.getChildCount(); i++) {
                 addInstanceClassToTree((DefaultMutableTreeNode) node.getChildAt(i), j, name);
             }
         }
     }
 
     /**
      * Adds the selected instances to a new instance user class or changes their instance source class.
      * This is decided by the user in the EDACCAddInstanceToInstanceClass Dialog.
      * @param selectedRows The rows of the selected instances
      */
     public void addInstancesToClass(int[] selectedRows) throws IOException {
         if (tableInstances.getSelectedRows().length == 0) {
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
                 Vector<Instance> toChange = new Vector<Instance>();
                 for (int i = 0; i < selectedRows.length; i++) {
                     toChange.add((Instance) main.instanceTableModel.getInstance(selectedRows[i]));
                 }
                 if (input != null) {
                     for (int i = 0; i < selectedRows.length; i++) {
                         InstanceHasInstanceClassDAO.createInstanceHasInstance(toChange.get(i), input);
                     }
                 }
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
             updateInstanceClasses();
             main.updateInstanceTable();
         }
     }
 
     public void onTaskStart(String methodName) {
     }
 
     public void onTaskFailed(String methodName, Throwable e) {
     }
 
     public void onTaskSuccessful(String methodName, Object result) {
         if (methodName.equals("TryToRemoveInstances")) {
             main.instanceTableModel.fireTableDataChanged();
             updateInstanceClasses();
             restoreExpandedState();
         }
     }
 
     /**
      * Shows the dialog to edit an instance class of the selected instance class
      * @param instanceClassTableModel Table model of the instance classes of the ManageDBMode
      * @param convertRowIndexToModel the row of the selected instance class
      */
     public void editInstanceClass() {
         JFrame mainFrame = EDACCApp.getApplication().getMainFrame();
         EDACCCreateEditInstanceClassDialog dialog = new EDACCCreateEditInstanceClassDialog(mainFrame, true, main.getInstanceClassTree());
         dialog.setLocationRelativeTo(mainFrame);
         EDACCApp.getApplication().show(dialog);
     }
 
     public void update(Observable o, Object arg) {
         /* this.main.instanceTableModel.clearTable();
         this.main.instanceClassTableModel.clearTable();*/
     }
 
     public void showInstanceClassButtons(boolean enable) {
         main.showInstanceClassButtons(enable);
     }
 
     void showInstanceButtons(boolean enable) {
         main.showInstanceButtons(enable);
     }
 
     /**
      *
      * @param parent String which represents the path of root
      * @param searchDepth int which represents the depth to which the root path has to be cut
      * @return path cut down to the given search depth or the maximum search depth (depth of the layer of the instance file).
      */
     private String cutToSearchDepth(String parent, File file, int searchDepth) {
         String tmpString = file.getAbsolutePath().substring(parent.length() + 1);
         char[] tmp = tmpString.toCharArray();
         int count = 0;
         for (int i = 0; i < tmp.length; i++) {
             if (tmp[i] == System.getProperty("file.separator").toCharArray()[0]) {
                 count++;
                 String tmpS = tmpString.substring(0, i);
                 if (searchDepth == count || !tmpS.contains("" + System.getProperty("file.separator").toCharArray()[0])) {
                     return tmpS;
                 }
             }
         }
         return tmpString;
     }
 
     /**
      *
      * @param selectedRows
      */
     public void showInstanceInfoDialog(int[] selectedRows) {
         try {
             // Get the intersection of the instance classes of the selected Instances
             Vector<Instance> instances = new Vector<Instance>();
             for (int i = 0; i < selectedRows.length; i++) {
                 instances.add((Instance) ((InstanceTableModel) tableInstances.getModel()).getInstance(tableInstances.convertRowIndexToModel(selectedRows[i])));
             }
             Vector<InstanceClass> instanceClasses = InstanceHasInstanceClassDAO.getIntersectionOfInstances(instances);
             AddInstanceInstanceClassTableModel tableModel = new AddInstanceInstanceClassTableModel();
             tableModel.addClasses(instanceClasses);
             EDACCExtendedWarning.showMessageDialog(EDACCExtendedWarning.OK_OPTIONS, EDACCApp.getApplication().getMainFrame(), "The selected instances belong to the following common instance classes.", new JTable(tableModel));
 
         } catch (NoConnectionToDBException ex) {
             Logger.getLogger(ManageDBInstances.class.getName()).log(Level.SEVERE, null, ex);
         } catch (SQLException ex) {
             Logger.getLogger(ManageDBInstances.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public void computeProperties(Vector<Instance> instances, Vector<Property> properties, Tasks task) throws ProblemOccuredDuringPropertyComputation {
         System.out.println(instances.size() + " instances, " + properties.size() + " properties.");
 
         lock.lock();
         Condition condition = lock.newCondition();
         PropertyComputationController comCon = new PropertyComputationController(instances, properties, task, lock);
         Thread compute = new Thread(comCon);
         try {
             compute.start();
             condition.await();
         } catch (InterruptedException e) {
         } finally {
             lock.unlock();
         }
         if (!comCon.getExceptionCollector().isEmpty()) {
             throw new ProblemOccuredDuringPropertyComputation(comCon.getExceptionCollector());
         }
         task.cancel(true);
     }
 
     public void changeInstanceTable() {
         main.updateInstanceTable();
         /*    ((InstanceTableModel) tableInstances.getModel()).clearTable();
         TreePath[] selected = main.getInstanceClassTree().getSelectionPaths();
         Vector<InstanceClass> choosen = new Vector<InstanceClass>();
         if (selected == null) {
         return;
         }
         for (int i = 0; i < selected.length; i++) {
         choosen.addAll(getAllToEnd((DefaultMutableTreeNode) (selected[i].getLastPathComponent())));
         /*  DefaultMutableTreeNode[] nodes = getPath()
         
         Enumeration<DefaultMutableTreeNode> tmp = nodes.children();
         while(tmp.hasMoreElements()){
         choosen.add((InstanceClass) tmp.nextElement().getUserObject());
         }
         choosen.add((InstanceClass) nodes.getUserObject());
         }
         if (!choosen.isEmpty()) {
         try {
         LinkedList<Instance> test = InstanceDAO.getAllByInstanceClasses(choosen);
         ((InstanceTableModel) tableInstances.getModel()).addInstances(new Vector<Instance>(test));
         } catch (NoConnectionToDBException ex) {
         Logger.getLogger(ManageDBInstances.class.getName()).log(Level.SEVERE, null, ex);
         } catch (SQLException ex) {
         Logger.getLogger(ManageDBInstances.class.getName()).log(Level.SEVERE, null, ex);
         }
         }
         main.updateInstanceTable();
         ((InstanceTableModel) tableInstances.getModel()).fireTableDataChanged();*/
     }
 
     private Vector<InstanceClass> getAllToEnd(DefaultMutableTreeNode root) {
         Vector<InstanceClass> ret = new Vector<InstanceClass>();
         for (int i = 0; i < root.getChildCount(); i++) {
             ret.addAll(getAllToEnd((DefaultMutableTreeNode) root.getChildAt(i)));
         }
         ret.add((InstanceClass) root.getUserObject());
         return ret;
     }
 
     public void exportInstanceClass(DefaultMutableTreeNode selected, String path, Tasks task) throws NoConnectionToDBException, SQLException, FileNotFoundException, IOException, NoSuchAlgorithmException, InstanceNotInDBException, TaskCancelledException {
         Tasks.getTaskView().setCancelable(true);
         task.setOperationName("Exporting instance classes");
         InstanceClass root = (InstanceClass) selected.getUserObject();
         Vector<InstanceClass> tmp = new Vector<InstanceClass>();
         Vector<Instance> md5Error = new Vector<Instance>();
         tmp.add(root);
         Vector<Instance> toExport = new Vector<Instance>(InstanceDAO.getAllByInstanceClasses(tmp));
 
         File dir = new File(path + System.getProperty("file.separator") + root.getName());
         if (!dir.isDirectory()) {
             dir.mkdir();
         }
 
         //Creates all files of the Instances related to the the InstanceClass
         for (int i = 0; i < toExport.size(); i++) {
             if (task.isCancelled()) {
                 throw new TaskCancelledException();
             }
             task.setStatus(i + " of " + toExport.size() + " instances from the class " + root.getName());
             task.setTaskProgress((float) i / (float) toExport.size());
             File f = new File(dir.getAbsolutePath() + System.getProperty("file.separator")
                     + toExport.get(i).getName());
             if (!f.exists()) {
                 InstanceDAO.getBinaryFileOfInstance(toExport.get(i), f);
             }
             String md5File = Util.calculateMD5(f);
             // If the file is corrupted delete it
             if (!md5File.equals(toExport.get(i).getMd5())) {
                 md5Error.add(toExport.get(i));
                 f.delete();
             }
         }
         task.setStatus(toExport.size() + " of " + toExport.size() + " instances from the class " + root.getName());
         task.setTaskProgress(1);
         Tasks.getTaskView().setCancelable(true);
         //Creates all files of the Instances related to the the childs of the InstanceClass
         for (int i = 0; i < selected.getChildCount(); i++) {
             md5Error.addAll(exportInstanceClasses((DefaultMutableTreeNode) selected.getChildAt(i), dir.getAbsolutePath(), task));
         }
 
         if (!md5Error.isEmpty()) {
             InstanceTableModel tableModel = new InstanceTableModel();
             tableModel.addInstances(md5Error);
             EDACCExtendedWarning.showMessageDialog(EDACCExtendedWarning.OK_OPTIONS, EDACCApp.getApplication().getMainFrame(), "Following instances couldn't be written. Because the MD5checksum wasn't valid.", new JTable(tableModel));
         }
     }
 
     private Vector<Instance> exportInstanceClasses(DefaultMutableTreeNode selected, String path, Tasks task) throws NoConnectionToDBException, SQLException, FileNotFoundException, IOException, NoSuchAlgorithmException, InstanceNotInDBException {
         task.setOperationName("Exporting instance classes");
         InstanceClass root = (InstanceClass) selected.getUserObject();
         Vector<InstanceClass> tmp = new Vector<InstanceClass>();
         Vector<Instance> md5Error = new Vector<Instance>();
         tmp.add(root);
         Vector<Instance> toExport = new Vector<Instance>(InstanceDAO.getAllByInstanceClasses(tmp));
 
         File dir = new File(path + System.getProperty("file.separator") + root.getName());
         if (!dir.isDirectory()) {
             dir.mkdir();
         }
 
         //Creates all files of the Instances related to the the InstanceClass
         for (int i = 0; i < toExport.size(); i++) {
             task.setStatus(i + " of " + toExport.size() + " instances from the class " + root.getName());
             task.setTaskProgress((float) i / (float) toExport.size());
             File f = new File(dir.getAbsolutePath() + System.getProperty("file.separator")
                     + toExport.get(i).getName());
             if (!f.exists()) {
                 InstanceDAO.getBinaryFileOfInstance(toExport.get(i), f);
             }
             String md5File = Util.calculateMD5(f);
             // If the file is corrupted delete it
             if (!md5File.equals(toExport.get(i).getMd5())) {
                 md5Error.add(toExport.get(i));
                 f.delete();
             }
         }
 
         task.setStatus(toExport.size() + " of " + toExport.size() + " instances from the class " + root.getName());
         task.setTaskProgress(1);
         //Creates all files of the Instances related to the the childs of the InstanceClass
         for (int i = 0; i < selected.getChildCount(); i++) {
             md5Error.addAll(exportInstanceClasses((DefaultMutableTreeNode) selected.getChildAt(i), dir.getAbsolutePath(), task));
         }
         return md5Error;
     }
 
     /**
      * Starts the deletion the given Instances and InstanceClasses
      * @param lastRelated
      * @param toRemove
      * @throws SQLException
      * @throws NoConnectionToDBException
      * @throws InstanceIsInExperimentException 
      */
     private void deleteInstanceClasssAndInstances(Vector<Instance> lastRelated, Vector<InstanceClass> toRemove) throws SQLException, NoConnectionToDBException, InstanceIsInExperimentException {
         AddInstanceInstanceClassTableModel tableModel = new AddInstanceInstanceClassTableModel();
         //Delete the related instances and instance classes
         InstanceDAO.deleteAll(lastRelated);
 
         Vector<InstanceClass> errors = new Vector<InstanceClass>();
         for (int i = 0; i < toRemove.size(); i++) {
             try {
                 InstanceClassDAO.delete(toRemove.get(i));
             } catch (InstanceSourceClassHasInstance ex) {
                 errors.add(toRemove.get(i));
             }
         }
         loadInstanceClasses();
         if (!errors.isEmpty()) {
             tableModel = new AddInstanceInstanceClassTableModel();
             tableModel.addClasses(errors);
             EDACCExtendedWarning.showMessageDialog(EDACCExtendedWarning.OK_OPTIONS,
                     EDACCApp.getApplication().getMainFrame(),
                     "A Problem occured by removing the following instance classes.  \n ",
                     new JTable(tableModel));
         }
     }
 
     /** 
      * The given files include instances which are already in the Database. They will be added to the 
      * given autobuilded InstanceClasses. 
      * @param errorsDBInstances
      * @param nodes 
      */
     private void addInstancesToAutoBuildedInstances(Vector<File> errorsDBInstances, DefaultMutableTreeNode nodes, File ret) throws FileNotFoundException, IOException, NoSuchAlgorithmException, NullPointerException, InstanceException, SQLException, InstanceClassAlreadyInDBException {
         InstanceClass instanceClass;
         for (int i = 0; i < errorsDBInstances.size(); i++) {
 
             String md5 = calculateMD5(errorsDBInstances.get(i));
             //Get all InstanceClasses of the Instance and assigne it to the corresponding InstanceClass
             String rawPath = errorsDBInstances.get(i).getAbsolutePath().substring(ret.getParent().length() + 1, errorsDBInstances.get(i).getParent().length());
             String[] possibleInstanceClasses = rawPath.split("\\\\|/");
             if (possibleInstanceClasses.length != 1) {
                 instanceClass = getInstanceClassFromTree(possibleInstanceClasses, nodes, 0);
             } else {
                 instanceClass = (InstanceClass) ((DefaultMutableTreeNode) nodes.getRoot()).getUserObject();
             }
             Instance temp = InstanceDAO.getByMd5(md5);
             InstanceHasInstanceClassDAO.createInstanceHasInstance(temp, instanceClass);
         }
     }
 
     public void updateInstanceClasses() {
         InstanceClassDAO.addTmpTreeBranchToTreeCache();
         DefaultMutableTreeNode root = (DefaultMutableTreeNode) InstanceClassDAO.getTreeCache();
         main.instanceClassTreeModel.setRoot(root);
         main.getInstanceClassTree().setRootVisible(false);
         main.instanceClassTreeModel.reload();
     }
 
     private void reloadInstanceClasses() {
         DefaultMutableTreeNode root = (DefaultMutableTreeNode) InstanceClassDAO.getTreeCache();
         main.instanceClassTreeModel.setRoot(root);
         main.getInstanceClassTree().setRootVisible(false);
         main.instanceClassTreeModel.reload();
     }
 
     public void restoreExpandedState() {
         main.restoreExpandedState();
     }
 
     public void loadProperties() {
         try {
             PropertyDAO.init();
         } catch (SQLException ex) {
             Logger.getLogger(ManageDBInstances.class.getName()).log(Level.SEVERE, null, ex);
         } catch (PropertyTypeNotExistException ex) {
             Logger.getLogger(ManageDBInstances.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(ManageDBInstances.class.getName()).log(Level.SEVERE, null, ex);
         } catch (ComputationMethodDoesNotExistException ex) {
             Logger.getLogger(ManageDBInstances.class.getName()).log(Level.SEVERE, null, ex);
         } catch (PropertyNotInDBException ex) {
             Logger.getLogger(ManageDBInstances.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     /**
      * Removes the relation of the given instances (the selected rows of the instanceTable) 
      * with the given instanceClasses (the selected instanceClasses from the instanceClassTree).
      * If the last relation is deleted, the instance is finally removed from the database and cache,
      * with the affirmation of the user.
      * @param selectionPaths
      * @param selectedRows
      * @throws SQLException 
      */
     public void removeInstances(TreePath[] selectionPaths, int[] selectedRows) throws SQLException {
         HashMap<Integer, Instance> selectedToRemove = new HashMap<Integer, Instance>();
 
         for (int i = 0; i < selectedRows.length; i++) {
             Instance tmp = (Instance) main.instanceTableModel.getInstance(tableInstances.convertRowIndexToModel(selectedRows[i]));
             selectedToRemove.put(tmp.getId(), tmp);
         }
 
         ArrayList<InstanceClass> classes = new ArrayList<InstanceClass>();
         for (int j = 0; j < selectionPaths.length; j++) {
             classes.addAll(getAllToEnd((DefaultMutableTreeNode) (selectionPaths[j].getLastPathComponent())));
         }
 
 
         InstanceTableModel tableModel = new InstanceTableModel();
         tableModel.addInstances(new Vector<Instance>(selectedToRemove.values()));
 
         //Check if user really wants to delete the selected instances
         if (EDACCExtendedWarning.showMessageDialog(EDACCExtendedWarning.OK_CANCEL_OPTIONS,
                 EDACCApp.getApplication().getMainFrame(),
                 "Do you really want to remove the listed instances from the selected instance classes?",
                 new JTable(tableModel))
                 == EDACCExtendedWarning.RET_OK_OPTION) {
 
             //Check if one of the selected instances is dangered being deleted complety from the database and inform and ask user
 
             ArrayList<Instance> lastOccurrence = InstanceHasInstanceClassDAO.checkIfLastOccurrence(new ArrayList<Instance>(selectedToRemove.values()), classes);
             if (!lastOccurrence.isEmpty()) {
                 for (Instance inst : lastOccurrence) {
                     selectedToRemove.remove(inst.getId());
                 }
                 tableModel.addInstances(new Vector<Instance>(lastOccurrence));
                 if (EDACCExtendedWarning.showMessageDialog(EDACCExtendedWarning.OK_CANCEL_OPTIONS,
                         EDACCApp.getApplication().getMainFrame(),
                         "The listed instances will be deleted complete from your edacc database. \n "
                         + "Do you really want to remove them?",
                         new JTable(tableModel))
                         != EDACCExtendedWarning.RET_OK_OPTION) {
                     lastOccurrence.clear();
                 }
 
             }
 
             //Get the selected instance classes
 
             ArrayList<Instance> toRemove = new ArrayList<Instance>(selectedToRemove.values());
             Tasks.startTask("tryToRemoveInstances", new Class[]{ArrayList.class, ArrayList.class, ArrayList.class, edacc.model.Tasks.class}, new Object[]{toRemove, lastOccurrence, classes, null}, this, this.main);
         }
     }
 
     /**
      * Remove the given rows from the instanceTableModel
      * @param rows the rows which have to be deleted
      */
     public void tryToRemoveInstances(ArrayList<Instance> toRemove, ArrayList<Instance> finallyRemove, ArrayList<InstanceClass> classes, Tasks task) throws NoConnectionToDBException, SQLException, TaskCancelledException {
         Tasks.getTaskView().setCancelable(true);
         task.setOperationName("Removing instances");
         int count = 0;
         int all = toRemove.size() + finallyRemove.size();
 
         for (int i = 0; i < toRemove.size(); i++) {
             for (int j = 0; j < classes.size(); j++) {
                 InstanceClass tempInstanceClass = classes.get(j);
                 InstanceHasInstanceClass rem = InstanceHasInstanceClassDAO.getInstanceHasInstanceClass(tempInstanceClass, toRemove.get(i));
                 if (rem != null) {
                     InstanceHasInstanceClassDAO.removeInstanceHasInstanceClass(rem);
                 }
             }
             task.setStatus(count + " of " + all + " instances removed");
             task.setTaskProgress((float) count / (float) all);
             count++;
         }
         main.instanceTableModel.removeInstances(new Vector<Instance>(toRemove));
         main.instanceTableModel.addInstances(new Vector<Instance>(toRemove));
 
         Vector<Instance> rem = new Vector<Instance>();
         Vector<Instance> notRem = new Vector<Instance>();
 
         for (Instance remove : finallyRemove) {
             try {
                 InstanceDAO.delete(remove);
                 rem.add(remove);
                 tmp.add(remove);
                 main.instanceTableModel.remove(remove);
                 count++;
                 task.setStatus(count + " of " + all + " instances removed");
                 task.setTaskProgress((float) count / (float) all);
             } catch (InstanceIsInExperimentException ex) {
                 notRem.add(remove);
             }
         }
 
         main.instanceTableModel.removeInstances(rem);
         if (!notRem.isEmpty()) {
             ExperimentTableModel expTableModel = new ExperimentTableModel(true);
             ArrayList<Experiment> inExp = ExperimentHasInstanceDAO.getAllExperimentsByInstances(notRem);
             expTableModel.setExperiments(inExp);
             if (EDACCExtendedWarning.showMessageDialog(EDACCExtendedWarning.OK_CANCEL_OPTIONS,
                     EDACCApp.getApplication().getMainFrame(),
                     "If you remove the instance classes, instances used in den following experiments will be deleted too. \n"
                     + "Do you really want to remove the selected instance?",
                     new JTable(expTableModel))
                     == EDACCExtendedWarning.RET_OK_OPTION) {
                 InstanceDAO.deleteAll(notRem);
                 main.instanceTableModel.removeInstances(notRem);
             }
 
         }
         updateInstanceClasses();
         main.instanceTableModel.fireTableDataChanged();
         Tasks.getTaskView().setCancelable(false);
     }
 }
