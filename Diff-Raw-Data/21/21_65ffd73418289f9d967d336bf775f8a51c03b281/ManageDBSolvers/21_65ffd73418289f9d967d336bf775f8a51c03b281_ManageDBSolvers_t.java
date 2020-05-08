 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edacc.manageDB;
 
 import edacc.model.MD5CheckFailedException;
 import edacc.model.SolverIsInExperimentException;
 import edacc.EDACCManageDBMode;
 import edacc.model.NoConnectionToDBException;
 import edacc.model.Solver;
 import edacc.model.SolverDAO;
 import edacc.model.SolverNotInDBException;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.security.NoSuchAlgorithmException;
 import java.sql.SQLException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author dgall
  */
 public class ManageDBSolvers {
 
     private EDACCManageDBMode gui;
     private SolverTableModel solverTableModel;
     private Solver currentSolver;
 
     public ManageDBSolvers(EDACCManageDBMode gui, SolverTableModel solverTableModel) {
         this.gui = gui;
         this.solverTableModel = solverTableModel;
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
      */
     public void applySolver(String name, String description) {
         if (currentSolver != null) {
             currentSolver.setName(name);
             currentSolver.setDescription(description);
         }
     }
 
     /**
      * Tries to save all solvers in the solver table to DB.
      * If a solver is already saved in the DB, it will update its data in the DB.
      * @throws SQLException
      * @throws FileNotFoundException
      */
     public void saveSolvers() throws SQLException, FileNotFoundException, NoSolverBinarySpecifiedException, NoSolverNameSpecifiedException,IOException {
         for (Solver s : solverTableModel.getSolvers()) {
             SolverDAO.save(s);
         }
     }
 
     public void newSolver() {
         solverTableModel.addSolver(new Solver());
         solverTableModel.fireTableDataChanged();
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
     }
 
     public void addSolverBinary(File binary) throws FileNotFoundException, IOException, NoSuchAlgorithmException, NoConnectionToDBException, SQLException, SolverAlreadyInDBException {
         if (!binary.exists()) {
             throw new FileNotFoundException("Couldn't find file \"" + binary.getName() + "\".");
         }
         currentSolver.setBinaryFile(binary);
         currentSolver.setBinaryName(binary.getName());
         currentSolver.setMd5(Util.calculateMD5(binary));
         if (SolverDAO.solverAlreadyInDB(currentSolver) != null) {
             currentSolver.setBinaryFile(null);
             currentSolver.setBinaryName(null);
             currentSolver.setMd5(null);
             throw new SolverAlreadyInDBException();
         }
     }
 
     public void addSolverCode(File[] code) throws FileNotFoundException {
         for (File c : code)
             if (!c.exists())
                 throw new FileNotFoundException("Couldn't find file \"" + c.getName() + "\".");
         currentSolver.setCodeFile(code);
     }
 
     /**
      * Removes the current solver from the solver table model.
      * If it is persisted in the db, it will also remove it from the db.
      * @throws SolverIsInExperimentException if the solver is used in an experiment.
      * @throws SQLException if an SQL error occurs while deleting the solver.
      */
     public void removeSolver() throws SolverIsInExperimentException, SQLException {
         removeSolver(currentSolver);
 
         solverTableModel.removeSolver(currentSolver);
     }
 
     /**
      * Removes the specified solver from the solver table model.
      * If it is persisted in the db, it will also remove it from the db.
      * @throws SolverIsInExperimentException if the solver is used in an experiment.
      * @throws SQLException if an SQL error occurs while deleting the solver.
      */
     public void removeSolver(Solver s) throws SolverIsInExperimentException, SQLException {
         try {
             SolverDAO.removeSolver(s);
         } catch (SolverNotInDBException ex) {
             // if the solver isn't in the db, just remove it from the table model
         }
         solverTableModel.removeSolver(s);
     }
 
     /**
      * Exports the binary of a solver to the file system.
      * @param s The solver to be exported
      * @param f The location where the binary shall be stored. If it is a directory,
      * the binaryName field of the solver will be used as filename.
      */
     public void exportSolver(Solver s, File f) throws NoConnectionToDBException, SQLException, FileNotFoundException, IOException, NoSuchAlgorithmException, MD5CheckFailedException {
         if (f.isDirectory()) {
             f = new File(f.getAbsolutePath() + System.getProperty("file.separator") + s.getBinaryName());
         }
         SolverDAO.getBinaryFileOfSolver(s, f);
         String md5File = Util.calculateMD5(f);
         if (!md5File.equals(s.getMd5())) {
             throw new MD5CheckFailedException("The exported solver binary of solver \"" + s.getName() + "\" seems to be corrupt!");
         }
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
         }
         else return;
         SolverDAO.exportSolverCode(s, f);
     }
 }
