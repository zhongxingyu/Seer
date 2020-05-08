 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edacc.manageDB;
 
 import edacc.EDACCApp;
 import edacc.model.SolverIsInExperimentException;
 import edacc.EDACCManageDBMode;
 import edacc.EDACCSolverBinaryDlg;
 import edacc.model.DatabaseConnector;
 import edacc.model.NoConnectionToDBException;
 import edacc.model.Parameter;
 import edacc.model.Solver;
 import edacc.model.SolverBinaries;
 import edacc.model.SolverBinariesDAO;
 import edacc.model.SolverDAO;
 import edacc.model.SolverNotInDBException;
 import edacc.model.TaskRunnable;
 import edacc.model.Tasks;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.SequenceInputStream;
 import java.security.NoSuchAlgorithmException;
 import java.sql.SQLException;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.NoSuchElementException;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Vector;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 import java.util.zip.ZipOutputStream;
 import javax.imageio.stream.FileImageInputStream;
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 import javax.xml.bind.JAXBException;
 
 /**
  *
  * @author dgall
  */
 public class ManageDBSolvers implements Observer {
 
     private EDACCManageDBMode gui;
     private SolverTableModel solverTableModel;
     private Solver currentSolver;
     private ManageDBParameters manageDBParameters;
     private SolverBinariesTableModel solverBinariesTableModel;
 
     public ManageDBSolvers(EDACCManageDBMode gui, SolverTableModel solverTableModel, ManageDBParameters manageDBParameters, SolverBinariesTableModel solverBinariesTableModel) {
         this.gui = gui;
         this.solverTableModel = solverTableModel;
         this.manageDBParameters = manageDBParameters;
         this.solverBinariesTableModel = solverBinariesTableModel;
         DatabaseConnector.getInstance().addObserver(this);
     }
 
     public Solver getCurrentSolver() {
         return currentSolver;
     }
 
     /**
      * Loads all solvers from the DB and adds it to the Solver table.
      * @throws NoConnectionToDBException
      * @throws SQLException
      */
     public void loadSolvers() throws NoConnectionToDBException, SQLException {
         solverTableModel.clear();
         SolverDAO.clearCache();
         for (Solver s : SolverDAO.getAll()) {
             solverTableModel.addSolver(s);
         }
     }
 
     /**
      * Applies the name and the description of a solver.
      * @param name
      * @param description
      * @return <code>true</code> if the solver changed
      */
     public boolean applySolver(String name, String description, String author, String version) {
         if (currentSolver != null) {
             if (currentSolver.getName() != null && currentSolver.getName().equals(name)
                     && currentSolver.getDescription().equals(description)
                     && currentSolver.getAuthors().equals(author)
                     && currentSolver.getVersion().equals(version)) {
                 return false;
             }
             currentSolver.setName(name);
             currentSolver.setDescription(description);
             currentSolver.setAuthor(author);
             currentSolver.setVersion(version);
             return true;
         }
         return false;
     }
 
     public void saveSolvers() {
         Tasks.startTask(new TaskRunnable() {
 
             @Override
             public void run(Tasks task) {
                 try {
                     saveSolvers(task);
                 } catch (final SQLException ex) {
                     SwingUtilities.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             JOptionPane.showMessageDialog(gui,
                                     "Solvers cannot be saved. There is a problem with the Database: " + ex.getMessage(),
                                     "Error",
                                     JOptionPane.ERROR_MESSAGE);
                         }
                     });
 
                 } catch (final FileNotFoundException ex) {
                     SwingUtilities.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             JOptionPane.showMessageDialog(gui,
                                     "Solvers cannot be saved because a file couldn't be found: " + ex.getMessage(),
                                     "Error",
                                     JOptionPane.ERROR_MESSAGE);
                         }
                     });
                 } catch (final NoSolverBinarySpecifiedException ex) {
                     SwingUtilities.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             JOptionPane.showMessageDialog(gui,
                                     ex.getMessage(),
                                     "Error",
                                     JOptionPane.ERROR_MESSAGE);
                         }
                     });
                 } catch (final NoSolverNameSpecifiedException ex) {
                     SwingUtilities.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             JOptionPane.showMessageDialog(gui,
                                     ex.getMessage(),
                                     "Error",
                                     JOptionPane.ERROR_MESSAGE);
                         }
                     });
                 } catch (final IOException ex) {
                     SwingUtilities.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             JOptionPane.showMessageDialog(gui,
                                     "IO exception while reading solver data from the filesystem" + ex.getMessage(),
                                     "Error",
                                     JOptionPane.ERROR_MESSAGE);
                         }
                     });
                 } catch (final NoSuchAlgorithmException ex) {
                     SwingUtilities.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             JOptionPane.showMessageDialog(gui,
                                     ex.getMessage(),
                                     "Error",
                                     JOptionPane.ERROR_MESSAGE);
                         }
                     });
                 } catch (final JAXBException ex) {
                     SwingUtilities.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             JOptionPane.showMessageDialog(gui,
                                     ex.getMessage(),
                                     "Error",
                                     JOptionPane.ERROR_MESSAGE);
                         }
                     });
                 }
             }
         }, true);
     }
 
     /**
      * Tries to save all solvers in the solver table to DB.
      * If a solver is already saved in the DB, it will update its data in the DB.
      * @throws SQLException
      * @throws FileNotFoundException
      */
     public void saveSolvers(final Tasks task) throws SQLException, FileNotFoundException, NoSolverBinarySpecifiedException, NoSolverNameSpecifiedException, IOException, NoSuchAlgorithmException, NoConnectionToDBException, JAXBException {
         task.setOperationName("Saving solvers...");
         int countSolvers = solverTableModel.getSolvers().size();
         for (int i = 0; i < countSolvers; i++) {
             Solver s = solverTableModel.getSolver(i);
             task.setTaskProgress((float) i / (float) countSolvers);
             task.setStatus("Saving solver " + s.getName() + " (" + i + " of " + countSolvers + ")");
             Vector<Parameter> params = manageDBParameters.getParametersOfSolver(s);
             SolverDAO.save(s);
             manageDBParameters.rehash(s, params);
         }
         // save parameters
         for (Solver s : solverTableModel.getSolvers()) {
             manageDBParameters.saveParameters(s);
         }
     }
 
     public void newSolver() {
         Solver s = new Solver();
         solverTableModel.addSolver(s);
         solverTableModel.fireTableDataChanged();
         manageDBParameters.addDefaultParameters(s);
     }
 
     /**
      * Shows the sovler with the specified index, which means: All
      * buttons for the solver are activated and its details are shown.
      * If the index is invalid, no solver will be shown and the solver
      * specific buttons are deactivated.
      * @param index
      */
     public void showSolver(int index) {
         currentSolver = solverTableModel.getSolver(index); // will be null if no solver selected!
         gui.showSolverDetails(currentSolver);
         gui.showSolverBinariesDetails(currentSolver == null ? null : currentSolver.getSolverBinaries());
         gui.showCostBinaryDetails(currentSolver == null ? null : currentSolver.getCostBinaries());
     }
 
     public void addSolverBinary(File[] binary) throws FileNotFoundException, IOException, NoSuchAlgorithmException, NoConnectionToDBException, SQLException, SolverAlreadyInDBException {
         if (binary.length == 0) {
             return;
         }
         Arrays.sort(binary);
         SolverBinaries b = new SolverBinaries(currentSolver);
         b.setBinaryArchive(binary);
         b.setBinaryName(binary[0].getName());
         try {
             FileInputStreamList is = new FileInputStreamList(binary);
             SequenceInputStream seq = new SequenceInputStream(is);
             String md5 = Util.calculateMD5(seq);
             if (hasDuplicates(md5)) {
                 if (JOptionPane.showConfirmDialog(gui,
                         "There already exists a solver binary with the same "
                         + "checksum. Do you want to add this binary anyway?",
                         "Duplicate solver binary",
                         JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
                         == JOptionPane.NO_OPTION) {
                     return;
                 }
             }
             b.setMd5(md5);
             Util.removeCommonPrefix(b);
             new EDACCSolverBinaryDlg(EDACCApp.getApplication().getMainFrame(), b, this, EDACCSolverBinaryDlg.DialogMode.CREATE_MODE).setVisible(true);
             gui.showSolverBinariesDetails(currentSolver.getSolverBinaries());
         } catch (NoSuchElementException e) {
             JOptionPane.showMessageDialog(gui, "You have to choose some files!", "No files chosen!", JOptionPane.ERROR_MESSAGE);
         }
     }
 
     private boolean hasDuplicates(String md5) {
         for (SolverBinaries b : currentSolver.getSolverBinaries()) {
             if (md5.equals(b.getMd5())) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Edits a solver binary with all details and changes also the file archive.
      * @param binary
      * @param solverBin
      * @throws IOException
      * @throws NoSuchAlgorithmException 
      */
     public void editSolverBinary(File[] binary, SolverBinaries solverBin) throws IOException, NoSuchAlgorithmException {
         if (binary.length == 0) {
             return;
         }
         Arrays.sort(binary);
         solverBin.setBinaryArchive(binary);
         FileInputStreamList is = new FileInputStreamList(binary);
         SequenceInputStream seq = new SequenceInputStream(is);
         String md5 = Util.calculateMD5(seq);
         if (hasDuplicates(md5)) {
             if (JOptionPane.showConfirmDialog(gui,
                     "There already exists a solver binary with the same "
                     + "checksum. Do you want to add this binary anyway?",
                     "Duplicate solver binary",
                     JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)
                     == JOptionPane.NO_OPTION) {
                 return;
             }
         }
         solverBin.setMd5(md5);
         Util.removeCommonPrefix(solverBin);
         new EDACCSolverBinaryDlg(EDACCApp.getApplication().getMainFrame(), solverBin, this, EDACCSolverBinaryDlg.DialogMode.EDIT_MODE).setVisible(true);
         gui.showSolverBinariesDetails(currentSolver.getSolverBinaries());
     }
 
     /**
      * Edits a solver binary but doesn't change the archive. Only the details
      * like name, run command or run path are changed.
      * @param solverBin
      * @throws IOException
      * @throws NoSuchAlgorithmException 
      */
     public void editSolverBinaryDetails(SolverBinaries solverBin) throws IOException, NoSuchAlgorithmException, SQLException {
         // create file list of binary
         setFileArrayOfSolverBinary(solverBin);
        Util.removeCommonPrefix(solverBin);
         new EDACCSolverBinaryDlg(EDACCApp.getApplication().getMainFrame(), solverBin, this, EDACCSolverBinaryDlg.DialogMode.EDIT_MODE).setVisible(true);
         // reset binary archive
         solverBin.setBinaryArchive(null);
         // refresh gui
         gui.showSolverBinariesDetails(currentSolver.getSolverBinaries());
     }
 
     private void setFileArrayOfSolverBinary(SolverBinaries solverBin) throws SQLException, IOException {
         // make temporary directory
         File tmpDir = new File(System.getProperty("java.io.tmpdir") + System.getProperty("file.separator") + "edacctmpdir");
         tmpDir.mkdirs();
         ZipInputStream zis = new ZipInputStream(SolverBinariesDAO.getZippedBinaryFile(solverBin));
         LinkedList<File> bins = new LinkedList<File>(); // the binary files in the zip 
         ZipEntry entry;
 
         while ((entry = zis.getNextEntry()) != null) {
             bins.add(new File(tmpDir.getAbsolutePath() + System.getProperty("file.separator") + entry.getName()));
         }
         solverBin.setBinaryArchive(bins.toArray(new File[bins.size()]));
         solverBin.setRootDir(tmpDir.getAbsolutePath());
     }
 
     public void addSolverBinary(SolverBinaries solverBin) throws SQLException, NoSolverBinarySpecifiedException, FileNotFoundException, IOException {
         currentSolver.addSolverBinary(solverBin);
     }
 
     public void addSolverCode(File[] code) throws FileNotFoundException {
         for (File c : code) {
             if (!c.exists()) {
                 throw new FileNotFoundException("Couldn't find file \"" + c.getName() + "\".");
             }
         }
         currentSolver.setCodeFile(code);
     }
 
     /**
      * Removes the current solver from the solver table model.
      * If it is persisted in the db, it will also remove it from the db.
      * @throws SolverIsInExperimentException if the solver is used in an experiment.
      * @throws SQLException if an SQL error occurs while deleting the solver.
      */
     public void removeSolver() throws SolverIsInExperimentException, SQLException, NoSolverBinarySpecifiedException, FileNotFoundException, IOException {
         removeSolver(currentSolver);
 
         solverTableModel.removeSolver(currentSolver);
     }
 
     /**
      * Removes the specified solver from the solver table model.
      * If it is persisted in the db, it will also remove it from the db.
      * @throws SolverIsInExperimentException if the solver is used in an experiment.
      * @throws SQLException if an SQL error occurs while deleting the solver.
      */
     public void removeSolver(Solver s) throws SolverIsInExperimentException, SQLException, NoSolverBinarySpecifiedException, FileNotFoundException, IOException {
         try {
             SolverDAO.removeSolver(s);
         } catch (SolverNotInDBException ex) {
             // if the solver isn't in the db, just remove it from the table model
         }
         solverTableModel.removeSolver(s);
     }
 
     public void removeSolverBinary(SolverBinaries b) throws SQLException, NoSolverBinarySpecifiedException, FileNotFoundException, IOException, NoSuchAlgorithmException {
         Solver s = SolverDAO.getById(b.getIdSolver());
         if (s.getSolverBinaries().size() <= 1) {
             throw new NoSolverBinarySpecifiedException("There must be at least one binary remaining for solver " + s.getName() + "!");
         }
         b.setDeleted();
         SolverBinariesDAO.save(b);
         solverBinariesTableModel.setSolverBinaries(currentSolver.getSolverBinaries());
     }
 
     /**
      * Exports the binary of a solver to the file system.
      * @param s The solver to be exported
      * @param f The location where the binary shall be stored. If it is a directory,
      * the solverName field of the solver will be used as filename.
      */
     public void exportSolver(final Solver s, final File f) {
         Tasks.startTask(new TaskRunnable() {
 
             @Override
             public void run(Tasks task) {
                 try {
                     startExportSolverTask(s, f, task);
                 } catch (final Exception e) {
                     SwingUtilities.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             JOptionPane.showMessageDialog(gui, "An error occured while exporting solver binaries: \n" + e.getMessage(), "Error while exporting solver binaries", JOptionPane.ERROR_MESSAGE);
                         }
                     });
                 }
             }
         });
 
     }
 
     private void startExportSolverTask(Solver s, File f, Tasks task) throws FileNotFoundException, SQLException, IOException {
         task.setOperationName("Exporting solver binaries...");
 
         FileOutputStream fos;
         if (f.isDirectory()) {
             fos = new FileOutputStream(f.getAbsolutePath() + System.getProperty("file.separator") + s.getName() + ".zip");
         } else {
             fos = new FileOutputStream(f);
         }
         ZipOutputStream zos = new ZipOutputStream(fos);
         Vector<SolverBinaries> bins = s.getSolverBinaries();
         for (int i = 0; i < bins.size(); i++) {
             task.setTaskProgress((float) (i + 1) / (float) bins.size());
             SolverBinaries b = bins.get(i);
             InputStream binStream = SolverBinariesDAO.getZippedBinaryFile(b);
             ZipInputStream zis = new ZipInputStream(binStream);
             ZipEntry entry;
             while ((entry = zis.getNextEntry()) != null) {
                 ZipEntry newEntry = new ZipEntry(b.getBinaryName() + "_" + b.getVersion() + "/" + entry.getName());
                 zos.putNextEntry(newEntry);
                 for (int c = zis.read(); c != -1; c = zis.read()) {
                     zos.write(c);
                 }
                 zos.closeEntry();
                 zis.closeEntry();
             }
             zis.close();
             binStream.close();
         }
         zos.close();
         fos.close();
     }
 
     /** Exports the code of a solver.
      * Creates a subdirectory in the directory specified by f named
      * SolverName_code
      * @param s solver, which code is to be exported
      * @param f File specifiying the directory the code should be exported to
      */
     public void exportSolverCode(Solver s, File f) throws NoConnectionToDBException, SQLException, FileNotFoundException, IOException {
         if (f.isDirectory()) {
             f = new File(f.getAbsolutePath() + System.getProperty("file.separator") + s.getName() + "_code");
         } else {
             return;
         }
         SolverDAO.exportSolverCode(s, f);
     }
 
     @Override
     public void update(Observable o, Object arg) {
         solverTableModel.clear();
     }
 
     public void removeSolverBinaries(Solver s) throws SQLException {
         SolverBinariesDAO.removeBinariesOfSolver(s);
     }
 
     void selectSolverBinary(boolean selected) {
         gui.enableSolverBinaryButtons(selected);
     }
 }
