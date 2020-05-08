 package edacc.experiment;
 
 import edacc.EDACCApp;
 import edacc.EDACCExperimentMode;
 import edacc.EDACCSolverConfigEntry;
 import edacc.EDACCSolverConfigPanel;
 import edacc.gridqueues.GridQueuesController;
 import edacc.model.DatabaseConnector;
 import edacc.model.Experiment;
 import edacc.model.ExperimentDAO;
 import edacc.model.ExperimentHasGridQueue;
 import edacc.model.ExperimentHasGridQueueDAO;
 import edacc.model.ExperimentHasInstance;
 import edacc.model.ExperimentHasInstanceDAO;
 import edacc.model.ExperimentResult;
 import edacc.model.ExperimentResultDAO;
 import edacc.model.GridQueue;
 import edacc.model.GridQueueDAO;
 import edacc.model.Instance;
 import edacc.model.InstanceClass;
 import edacc.model.InstanceClassDAO;
 import edacc.model.InstanceDAO;
 import edacc.model.NoConnectionToDBException;
 import edacc.model.Solver;
 import edacc.model.SolverConfiguration;
 import edacc.model.SolverConfigurationDAO;
 import edacc.model.SolverDAO;
 import edacc.model.TaskCancelledException;
 import edacc.model.Tasks;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.lang.reflect.InvocationTargetException;
 import java.sql.Date;
 import java.sql.SQLException;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.LinkedList;
 import java.util.Random;
 import java.util.Vector;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 import javax.swing.SwingUtilities;
 
 /**
  * Experiment design more controller class, handles requests by the GUI
  * for creating, removing, loading, etc. experiments
  * @author daniel
  */
 public class ExperimentController {
 
     EDACCExperimentMode main;
     EDACCSolverConfigPanel solverConfigPanel;
     private Experiment activeExperiment;
     private Vector<Experiment> experiments;
     private static RandomNumberGenerator rnd = new JavaRandom();
 
     /**
      * Creates a new experiment Controller
      * @param experimentMode
      * @param solverConfigPanel
      */
     public ExperimentController(EDACCExperimentMode experimentMode, EDACCSolverConfigPanel solverConfigPanel) {
         this.main = experimentMode;
         this.solverConfigPanel = solverConfigPanel;
     }
 
     /**
      * Initializes the experiment controller. Loads the experiments and the instances classes.
      * @throws SQLException
      */
     public void initialize() throws SQLException {
         Vector<Experiment> v = new Vector<Experiment>();
         v.addAll(ExperimentDAO.getAll());
         experiments = v;
         main.expTableModel.setExperiments(experiments);
 
         Vector<InstanceClass> vic = new Vector<InstanceClass>();
         vic.addAll(InstanceClassDAO.getAll());
         main.instanceClassModel.setClasses(vic);
 
     }
 
     /**
      * Loads an experiment, the solvers and the solver configurations.
      * @param id
      * @throws SQLException
      */
     public void loadExperiment(int id, Tasks task) throws SQLException {
         main.solverConfigPanel.beginUpdate();
         solverConfigPanel.removeAll();
         SolverConfigurationDAO.clearCache();
         task.setStatus("Loading solvers..");
         activeExperiment = ExperimentDAO.getById(id);
         Vector<Solver> vs = new Vector<Solver>();
         vs.addAll(SolverDAO.getAll());
         main.solTableModel.setSolvers(vs);
         task.setTaskProgress(.33f);
 
         task.setStatus("Loading solver configurations..");
         Vector<SolverConfiguration> vss = SolverConfigurationDAO.getSolverConfigurationByExperimentId(id);
         for (int i = 0; i < vss.size(); i++) {
             main.solverConfigPanel.addSolverConfiguration(vss.get(i));
         }
         task.setTaskProgress(.66f);
         task.setStatus("Loading instances..");
         // cache instances
         InstanceDAO.getAllByExperimentId(id);
 
         // select instances for the experiment
         main.insTableModel.setExperimentHasInstances(ExperimentHasInstanceDAO.getExperimentHasInstanceByExperimentId(activeExperiment.getId()));
 
         if (main.insTableModel.getRowCount() > 0) {
             main.sorter.setRowFilter(main.rowFilter);
         }
         main.solverConfigPanel.endUpdate();
         Vector<GridQueue> queues = GridQueueDAO.getAllByExperiment(activeExperiment);
         if (queues.size() > 0) {
             GridQueuesController.getInstance().setChosenQueue(queues.get(0));
         }
         main.afterExperimentLoaded();
     }
 
     /**
      * Removes an experiment form the db.
      * @param id
      * @return
      * @throws SQLException
      */
     public void removeExperiment(int id) throws SQLException {
         Experiment e = ExperimentDAO.getById(id);
         if (e.equals(activeExperiment)) {
             unloadExperiment();
         }
         ExperimentDAO.removeExperiment(e);
         initialize();
     }
 
     /**
      * returns a reference to the currently loaded experiment or null, if none
      * @return active experiment reference
      */
     public Experiment getActiveExperiment() {
         return activeExperiment;
     }
 
     /**
      * unloads the currently loaded experiment, i.e. sets activeExperiment to null
      * and calls UI functions to disable the experiment design tabs
      */
     public void unloadExperiment() {
         activeExperiment = null;
         main.afterExperimentUnloaded();
     }
 
     /**
      * invoked by the UI to create a new experiment, also calls initialize to load
      * instances and solvers
      * @param name
      * @param date
      * @param description
      * @throws SQLException
      * @throws Exception
      */
     public void createExperiment(String name, String description) throws SQLException, Exception {
         java.util.Date d = new java.util.Date();
         ExperimentDAO.createExperiment(name, new Date(d.getTime()), description);
         initialize();
     }
 
     public void saveExperiment(Experiment exp) throws SQLException {
         ExperimentDAO.save(exp);
     }
 
     /**
      * Saves all solver configurations with parameter instances in the solver
      * config panel.
      * @throws SQLException
      */
     public void saveSolverConfigurations(Tasks task) throws SQLException, InterruptedException, InvocationTargetException {
 
         task.setStatus("Checking jobs..");
         Vector<SolverConfiguration> deletedSolverConfigurations = SolverConfigurationDAO.getAllDeleted();
         final Vector<ExperimentResult> deletedJobs = new Vector<ExperimentResult>();
         for (SolverConfiguration sc : deletedSolverConfigurations) {
             deletedJobs.addAll(ExperimentResultDAO.getAllBySolverConfiguration(sc));
         }
 
         if (deletedJobs.size() > 0) {
             int notDeletableJobsCount = 0;
             for (ExperimentResult job : deletedJobs) {
                 if (job.getStatus() != -1) {
                     notDeletableJobsCount++;
                 }
             }
             String msg = "";
             if (notDeletableJobsCount > 0) {
                 msg = "There are " + notDeletableJobsCount + " started jobs and " + (deletedJobs.size() - notDeletableJobsCount) + " jobs waiting in the database which would be deleted. Do you want to continue?";
             } else {
                 msg = "There are " + deletedJobs.size() + " jobs waiting in the database which would be deleted. Do you want to continue?";
             }
             int userInput = javax.swing.JOptionPane.showConfirmDialog(Tasks.getTaskView(), msg, "Jobs would be deleted", javax.swing.JOptionPane.YES_NO_OPTION);
             if (userInput == 1) {
                 return;
             } else {
                 task.setStatus("Deleting jobs..");
                 ExperimentResultDAO.deleteExperimentResults(deletedJobs);
             }
         }
         task.setStatus("Saving solver configurations..");
         boolean invalidSeedGroup = false;
         for (int i = 0; i < solverConfigPanel.getComponentCount(); i++) {
             EDACCSolverConfigEntry entry = (EDACCSolverConfigEntry) solverConfigPanel.getComponent(i);
             int seed_group = 0;
             try {
                 seed_group = Integer.valueOf(entry.getSeedGroup().getText());
             } catch (NumberFormatException e) {
                 seed_group = 0;
                 entry.getSeedGroup().setText("0");
                 invalidSeedGroup = true;
             }
             if (entry.getSolverConfiguration() == null) {
                 entry.setSolverConfiguration(SolverConfigurationDAO.createSolverConfiguration(entry.getSolverId(), activeExperiment.getId(), seed_group));
             } else {
                 entry.getSolverConfiguration().setSeed_group(seed_group);
                 entry.getSolverConfiguration().setModified();
             }
             entry.saveParameterInstances();
         }
         SolverConfigurationDAO.saveAll();
         if (invalidSeedGroup) {
             SwingUtilities.invokeAndWait(new Runnable() {
 
                 @Override
                 public void run() {
                     javax.swing.JOptionPane.showMessageDialog(Tasks.getTaskView(), "Seed groups have to be integers, defaulted to 0", "Expected integer for seed groups", javax.swing.JOptionPane.ERROR_MESSAGE);
                 }
             });
         }
     }
 
     public void undoSolverConfigurations(Tasks task) throws SQLException {
         main.solverConfigPanel.beginUpdate();
         main.solverConfigPanel.removeAll();
         Vector<SolverConfiguration> solverConfigurations = SolverConfigurationDAO.getAllCached();
         Collections.sort(solverConfigurations, new Comparator<SolverConfiguration>() {
 
             @Override
             public int compare(SolverConfiguration o1, SolverConfiguration o2) {
                 return o1.getId() - o2.getId();
             }
         });
         for (SolverConfiguration sc : solverConfigurations) {
             main.solverConfigPanel.addSolverConfiguration(sc);
             sc.setSaved();
         }
         main.solverConfigPanel.endUpdate();
         main.setTitles();
     }
 
     /**
      * saves the instances selection of the currently loaded experiment
      * @throws SQLException
      */
     public void saveExperimentHasInstances(Tasks task) throws SQLException, InterruptedException, InvocationTargetException {
         task.setStatus("Checking jobs..");
         Vector<ExperimentHasInstance> deletedInstances = main.insTableModel.getDeletedExperimentHasInstances();
         if (deletedInstances.size() > 0) {
             Vector<ExperimentResult> deletedJobs = new Vector<ExperimentResult>();
             for (ExperimentHasInstance ehi : deletedInstances) {
                 deletedJobs.addAll(ExperimentResultDAO.getAllByExperimentHasInstance(ehi));
             }
             int notDeletableJobsCount = 0;
             for (ExperimentResult job : deletedJobs) {
                 if (job.getStatus() != -1) {
                     notDeletableJobsCount++;
                 }
             }
             String msg = "";
             if (notDeletableJobsCount > 0) {
                 msg = "There are " + notDeletableJobsCount + " started jobs and " + (deletedJobs.size() - notDeletableJobsCount) + " jobs waiting in the database which would be deleted. Do you want to continue?";
             } else {
                 msg = "There are " + deletedJobs.size() + " jobs waiting in the database which would be deleted. Do you want to continue?";
             }
             int userInput = javax.swing.JOptionPane.showConfirmDialog(Tasks.getTaskView(), msg, "Jobs would be deleted", javax.swing.JOptionPane.YES_NO_OPTION);
             if (userInput == 1) {
                 return;
             } else {
                 task.setStatus("Deleting jobs..");
                 ExperimentResultDAO.deleteExperimentResults(deletedJobs);
             }
         }
         task.setStatus("Saving instances..");
         // First: add all new ExperimentHasInstance objects
         for (Integer instanceId : main.insTableModel.getNewInstanceIds()) {
             ExperimentHasInstanceDAO.createExperimentHasInstance(activeExperiment.getId(), instanceId);
         }
 
         // Then: remove all removed ExperimentHasInstance objects
         for (ExperimentHasInstance ehi : main.insTableModel.getDeletedExperimentHasInstances()) {
             ExperimentHasInstanceDAO.removeExperimentHasInstance(ehi);
         }
 
         main.insTableModel.setExperimentHasInstances(ExperimentHasInstanceDAO.getExperimentHasInstanceByExperimentId(activeExperiment.getId()));
     }
 
     /**
      * method used for auto seed generation, uses the random number generator
      * referenced by this.rnd
      * @return integer between 0 and max inclusively
      */
     private int generateSeed(int max) {
         return rnd.nextInt(max + 1);
     }
 
     /**
      * generates the ExperimentResults (jobs) in the database for the currently active experiment
      * This is the cartesian product of the set of solver configs and the set of the selected instances
      * Doesn't overwrite existing jobs
      * @throws SQLException
      * @param numRuns
      * @param timeout
      * @param generateSeeds
      * @param maxSeed
      * @return number of jobs added to the experiment results table
      * @throws SQLException
      */
     public int generateJobs(int numRuns, final Tasks task) throws SQLException, TaskCancelledException {
         PropertyChangeListener cancelExperimentResultDAOStatementListener = new PropertyChangeListener() {
 
             public void propertyChange(PropertyChangeEvent evt) {
                 if ("state".equals(evt.getPropertyName()) && task.isCancelled()) {
                     try {
                         ExperimentResultDAO.cancelStatement();
                     } catch (SQLException ex) {
                     }
                 }
             }
         };
         Tasks.getTaskView().setCancelable(true);
         task.setOperationName("Generating jobs for experiment " + activeExperiment.getName());
         // get instances of this experiment
         LinkedList<Instance> listInstances = InstanceDAO.getAllByExperimentId(activeExperiment.getId());
 
         // get solver configurations of this experiment
         Vector<SolverConfiguration> vsc = SolverConfigurationDAO.getSolverConfigurationByExperimentId(activeExperiment.getId());
 
         int experiments_added = 0;
         Hashtable<SeedGroup, Integer> linked_seeds = new Hashtable<SeedGroup, Integer>();
         Vector<ExperimentResult> experiment_results = new Vector<ExperimentResult>();
 
         int elements = listInstances.size() * vsc.size() * numRuns;
         ExperimentDAO.updateNumRuns(activeExperiment);
         if (numRuns < activeExperiment.getNumRuns()) {
             // We have to delete jobs
             int runsToDelete = activeExperiment.getNumRuns() - numRuns;
             task.setStatus("Preparing..");
             Vector<Integer> runs = ExperimentResultDAO.getAllRunsByExperimentId(activeExperiment.getId());
             Vector<Integer> deleteRuns = new Vector<Integer>();
             Random random = new Random();
             for (int i = 0; i < runsToDelete; i++) {
                 if (runs.size() == 0) {
                     break;
                 }
                 int index = random.nextInt(runs.size());
                 deleteRuns.add(runs.get(index));
                 runs.remove(index);
             }
             Vector<ExperimentResult> deletedJobs = new Vector<ExperimentResult>();
             for (int i = 0; i < deleteRuns.size(); i++) {
                 int run = deleteRuns.get(i);
                 if (task.isCancelled()) {
                     throw new TaskCancelledException();
                 }
                 deletedJobs.addAll(ExperimentResultDAO.getAllByExperimentIdAndRun(activeExperiment.getId(), run));
                 task.setTaskProgress((float) (i + 1) / (deleteRuns.size()));
             }
             String msg = "The number of runs specified is less than the number of runs in this experiment. There are " + deletedJobs.size() + " jobs which would be deleted. Do you want to continue?";
             int userInput = javax.swing.JOptionPane.showConfirmDialog(Tasks.getTaskView(), msg, "Jobs would be deleted", javax.swing.JOptionPane.YES_NO_OPTION);
             task.setTaskProgress(0.f);
             if (userInput == 1) {
                 return 0;
             } else {
                 task.setStatus("Deleting jobs..");
                 task.addPropertyChangeListener(cancelExperimentResultDAOStatementListener);
                try {
                     ExperimentResultDAO.setAutoCommit(false);
                     ExperimentResultDAO.deleteExperimentResults(deletedJobs);
                     task.setStatus("Updating existing jobs..");
                     Vector<ExperimentResult> updateJobs = new Vector<ExperimentResult>();
                     for (int i = 0; i < runs.size(); i++) {
                         if (task.isCancelled()) {
                             throw new TaskCancelledException();
                         }
                         Vector<ExperimentResult> tmp = ExperimentResultDAO.getAllByExperimentIdAndRun(activeExperiment.getId(), runs.get(i));
                         for (ExperimentResult er : tmp) {
                             er.setRun(i);
                         }
                         updateJobs.addAll(tmp);
                         task.setTaskProgress((float) (i + 1) / (runs.size()));
                     }
                     task.setTaskProgress(0.f);

 
                     ExperimentResultDAO.batchUpdateRun(updateJobs);
                 } catch (SQLException ex) {
                     if (ex.getMessage().contains("cancelled")) {
                         throw new TaskCancelledException();
                     }
                     throw ex;
                 } finally {
                     ExperimentResultDAO.setAutoCommit(true);
                 }
                 task.removePropertyChangeListener(cancelExperimentResultDAOStatementListener);
                 ExperimentDAO.updateNumRuns(activeExperiment);
             }
             Tasks.getTaskView().setCancelable(true);
         }
         Vector<ExperimentResult> res = ExperimentResultDAO.getAllByExperimentId(activeExperiment.getId());
         HashMap<ExperimentResult, ExperimentResult> existingResults = new HashMap<ExperimentResult, ExperimentResult>();
         for (ExperimentResult e : res) {
             existingResults.put(e, e);
         }
         ExperimentResult er = ExperimentResultDAO.createExperimentResult(0, 0, 0, 0, 0, 0, activeExperiment.getId(), 0);
         if (activeExperiment.isAutoGeneratedSeeds() && activeExperiment.isLinkSeeds()) {
             // first pass over already existing jobs to accumulate existing linked seeds
             for (Instance i : listInstances) {
                 for (SolverConfiguration c : vsc) {
                     for (int run = 0; run < numRuns; ++run) {
                         task.setStatus("Preparing job generation");
                         er.setRun(run);
                         er.setInstanceId(i.getId());
                         er.setSolverConfigId(c.getId());
                         if (existingResults.containsKey(er)) {
                             // use the already existing jobs to populate the seed group hash table so jobs of newly added solver configs use
                             // the same seeds as already existing jobs
                             int seed = ExperimentResultDAO.getSeedValue(run, c.getId(), i.getId(), activeExperiment.getId());
                             SeedGroup sg = new SeedGroup(c.getSeed_group(), i.getId(), run);
                             if (!linked_seeds.contains(sg)) {
                                 linked_seeds.put(sg, new Integer(seed));
                             }
                         }
                     }
                 }
             }
         }
         if (task.isCancelled()) {
             throw new TaskCancelledException();
         }
 
 
         int done = 1;
         // cartesian product
         for (Instance i : listInstances) {
             for (SolverConfiguration c : vsc) {
                 for (int run = 0; run < numRuns; ++run) {
                     task.setTaskProgress((float) done / (float) elements);
                     if (task.isCancelled()) {
                         throw new TaskCancelledException();
                     }
                     task.setStatus("Adding job " + done + " of " + elements);
                     // check if job already exists
                     er.setRun(run);
                     er.setInstanceId(i.getId());
                     er.setSolverConfigId(c.getId());
                     if (!existingResults.containsKey(er)) {
                         if (activeExperiment.isAutoGeneratedSeeds() && activeExperiment.isLinkSeeds()) {
                             Integer seed = linked_seeds.get(new SeedGroup(c.getSeed_group(), i.getId(), run));
                             if (seed != null) {
                                 experiment_results.add(ExperimentResultDAO.createExperimentResult(run, -1, seed.intValue(), 0, -1, c.getId(), activeExperiment.getId(), i.getId()));
                             } else {
                                 Integer new_seed = new Integer(generateSeed(activeExperiment.getMaxSeed()));
                                 linked_seeds.put(new SeedGroup(c.getSeed_group(), i.getId(), run), new_seed);
                                 experiment_results.add(ExperimentResultDAO.createExperimentResult(run, -1, new_seed.intValue(), 0, -1, c.getId(), activeExperiment.getId(), i.getId()));
                             }
                         } else if (activeExperiment.isAutoGeneratedSeeds() && !activeExperiment.isLinkSeeds()) {
                             experiment_results.add(ExperimentResultDAO.createExperimentResult(run, -1, generateSeed(activeExperiment.getMaxSeed()), 0, -1, c.getId(), activeExperiment.getId(), i.getId()));
                         } else {
                             experiment_results.add(ExperimentResultDAO.createExperimentResult(run, -1, 0, 0, -1, c.getId(), activeExperiment.getId(), i.getId()));
                         }
                         experiments_added++;
                     }
                     done++;
                 }
 
             }
         }
         task.setTaskProgress(0.f);
         task.addPropertyChangeListener(cancelExperimentResultDAOStatementListener);
         task.setStatus("Saving changes to database..");
         try {
             ExperimentResultDAO.batchSave(experiment_results);
         } catch (SQLException ex) {
             if (ex.getMessage().contains("cancelled")) {
                 throw new TaskCancelledException();
             }
             throw ex;
         }
         task.removePropertyChangeListener(cancelExperimentResultDAOStatementListener);
         ExperimentDAO.updateNumRuns(activeExperiment);
         return experiments_added;
     }
 
     public void saveExperimentParameters(Integer maxMem, Integer timeout, Integer maxSeed, boolean generateSeeds, boolean linkSeeds) throws SQLException {
         activeExperiment.setAutoGeneratedSeeds(generateSeeds);
         activeExperiment.setTimeOut(timeout);
         activeExperiment.setMemOut(maxMem);
         activeExperiment.setMaxSeed(maxSeed);
         activeExperiment.setLinkSeeds(linkSeeds);
         ExperimentDAO.save(activeExperiment);
         initialize();
     }
 
     /**
      * returns the number of jobs in the database for the given experiment
      * @return
      */
     public int getNumJobs() {
         try {
             return ExperimentResultDAO.getCountByExperimentId(activeExperiment.getId());
         } catch (Exception e) {
             return 0;
         }
     }
 
     /**
      * returns the number of instances shown in the instance selection tab
      * @return
      */
     public int getNumInstances() {
         return main.insTableModel.getRowCount();
     }
 
     public void loadJobs() {
         try {


             Vector<ExperimentResult> jobs = ExperimentResultDAO.getAllByExperimentId(activeExperiment.getId());
            final ExperimentResultsBrowserTableModel sync = main.jobsTableModel;
            synchronized (sync) {
                main.jobsTableModel.setJobs(jobs);
            }
             System.gc();
         } catch (Exception e) {
             // TODO: shouldn't happen but show message if it does
         }
 
     }
 
     /**
      * Generates a ZIP archive with the necessary files for the grid.
      */
     public void generatePackage(File zipFile, Tasks task) throws FileNotFoundException, IOException, NoConnectionToDBException, SQLException, ClientBinaryNotFoundException {
         if (zipFile.exists()) {
             zipFile.delete();
         }
         ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
         ZipEntry entry;
 
         Tasks.getTaskView().setCancelable(true);
         task.setOperationName("Generating Package");
 
         Vector<Solver> solvers = ExperimentDAO.getSolversInExperiment(activeExperiment);
         LinkedList<Instance> instances = InstanceDAO.getAllByExperimentId(activeExperiment.getId());
 
         int total = solvers.size() + instances.size();
         int done = 0;
 
         boolean foundSolverWithSameName = false;
         HashSet<String> tmp = new HashSet<String>();
         HashSet<String> solvernameMap = new HashSet<String>();
         for (Solver s : solvers) {
             if (tmp.contains(s.getBinaryName())) {
                 solvernameMap.add(s.getBinaryName());
                 foundSolverWithSameName = true;
             } else {
                 tmp.add(s.getBinaryName());
             }
         }
         // add solvers to zip file
         for (Solver s : solvers) {
             done++;
             task.setTaskProgress((float) done / (float) total);
             if (task.isCancelled()) {
                 task.setStatus("Cancelled");
                 break;
             }
             task.setStatus("Writing solver " + done + " of " + solvers.size());
             File bin = SolverDAO.getBinaryFileOfSolver(s);
             String filename;
             if (solvernameMap.contains(s.getBinaryName())) {
                 filename = s.getBinaryName() + "_" + s.getMd5().substring(0, 3);
             } else {
                 filename = s.getBinaryName();
             }
             entry = new ZipEntry("solvers" + System.getProperty("file.separator") + filename);
             addFileToZIP(bin, entry, zos);
         }
 
         // add instances to zip file
         for (Instance i : instances) {
             done++;
             task.setTaskProgress((float) done / (float) total);
             if (task.isCancelled()) {
                 task.setStatus("Cancelled");
                 break;
             }
             task.setStatus("Writing instance " + (done - solvers.size()) + " of " + instances.size());
             File f = InstanceDAO.getBinaryFileOfInstance(i);
             entry = new ZipEntry("instances" + System.getProperty("file.separator") + i.getName());
             addFileToZIP(f, entry, zos);
         }
 
         task.setStatus("Writing client");
 
         // add PBS script
         // TODO extend to multiple queue support
         Vector<ExperimentHasGridQueue> eqs = ExperimentHasGridQueueDAO.getExperimentHasGridQueueByExperiment(activeExperiment);
         ExperimentHasGridQueue eq = eqs.get(eqs.size() - 1);
         GridQueue q = GridQueueDAO.getById(eq.getIdGridQueue());
         File f = GridQueueDAO.getPBS(q);
         entry = new ZipEntry("start_client.pbs");
         addFileToZIP(f, entry, zos);
 
         // add configuration File
         addConfigurationFile(zos, activeExperiment, q);
 
         // add run script
         addRunScript(zos, q);
 
         // add client binary
         addClient(zos);
 
         // add empty result library
         entry = new ZipEntry("results" + System.getProperty("file.separator") + "~");
         zos.putNextEntry(entry);
 
         zos.close();
 
         // delete tmp directory
         deleteDirectory(new File("tmp"));
 
         if (foundSolverWithSameName) {
             javax.swing.JOptionPane.showMessageDialog(null, "The resulting package file contains solvers with same names.", "Information", javax.swing.JOptionPane.INFORMATION_MESSAGE);
         }
     }
 
     private boolean deleteDirectory(File dir) {
         if (dir.exists()) {
             File[] files = dir.listFiles();
             for (int i = 0; i < files.length; i++) {
                 if (files[i].isDirectory()) {
                     deleteDirectory(files[i]);
                 } else {
                     files[i].delete();
                 }
             }
         }
         return (dir.delete());
     }
 
     /**
      * Adds a file to an open zip file.
      * @param f the location of the file to be added.
      * @param entry the zip entry to be created.
      * @param zos the open ZIPOutputStream of the zip file.
      */
     private void addFileToZIP(File f, ZipEntry entry, ZipOutputStream zos) throws FileNotFoundException, IOException {
         FileInputStream in = new FileInputStream(f);
         zos.putNextEntry(entry);
 
         byte[] buf = new byte[256 * 1024];
         int len;
         while ((len = in.read(buf)) > -1) {
             zos.write(buf, 0, len);
         }
         zos.closeEntry();
         in.close();
     }
 
     /**
      * Assigns a gridQueue to the active experiment.
      * This means: It creates a new ExperimentHasGridQueue object and persists it in the db.
      * @param q
      * @throws SQLException
      */
     public void assignQueueToExperiment(GridQueue q) throws SQLException {
         // check if assignment already exists
         if (ExperimentHasGridQueueDAO.getByExpAndQueue(activeExperiment, q) != null) {
             return;
         }
         ExperimentHasGridQueue eq = ExperimentHasGridQueueDAO.createExperimentHasGridQueue(activeExperiment, q);
     }
 
     public void selectAllInstanceClasses() {
         main.instanceClassModel.beginUpdate();
         for (int i = 0; i < main.instanceClassModel.getRowCount(); i++) {
             main.instanceClassModel.setInstanceClassSelected(i);
         }
         main.instanceClassModel.endUpdate();
     }
 
     public void deselectAllInstanceClasses() {
         main.instanceClassModel.beginUpdate();
         for (int i = 0; i < main.instanceClassModel.getRowCount(); i++) {
             main.instanceClassModel.setInstanceClassDeselected(i);
         }
         main.instanceClassModel.endUpdate();
     }
 
     private void addConfigurationFile(ZipOutputStream zos, Experiment activeExperiment, GridQueue activeQueue) throws IOException {
         // generate content of config file
         String sConf = "host = $host\n" + "username = $user\n" + "password = $pwd\n" + "database = $db\n" + "experiment = $exp\n" + "gridqueue = $q\n";
         DatabaseConnector con = DatabaseConnector.getInstance();
         sConf = sConf.replace("$host", con.getHostname());
         sConf = sConf.replace("$user", con.getUsername());
         sConf = sConf.replace("$pwd", con.getPassword());
         sConf = sConf.replace("$db", con.getDatabase());
         sConf = sConf.replace("$exp", String.valueOf(activeExperiment.getId()));
         sConf = sConf.replace("$q", String.valueOf(activeQueue.getId()));
 
         // write file into zip archive
         ZipEntry entry = new ZipEntry("config");
         zos.putNextEntry(entry);
         zos.write(sConf.getBytes());
         zos.closeEntry();
     }
 
     private void addRunScript(ZipOutputStream zos, GridQueue q) throws IOException {
        String sRun = "#!/bin/bash\n" +
                 "chmod a-rwx client\n" +
                 "chmod u+rwx client\n" +
                 "chmod a-rwx config\n" +
                 "chmod u+rw config\n" +
                 "chmod a-rwx solvers/*\n" +
                 "chmod u+rwx solvers/*\n" +
                 "chmod a-rwx instances/*\n" +
                 "chmod u+wr instances/*\n" +
                 "for (( i = 0; i < " + q.getNumNodes() + "; i++ ))\n" +
                 "do\n" +
                 "    qsub start_client.pbs\n" +
                 "done\n";
 
         // write file into zip archive
         ZipEntry entry = new ZipEntry("run.sh");
         zos.putNextEntry(entry);
         zos.write(sRun.getBytes());
         zos.closeEntry();
     }
 
     private void addClient(ZipOutputStream zos) throws IOException, ClientBinaryNotFoundException {
         InputStream in = EDACCApp.class.getClassLoader().getResourceAsStream("edacc/resources/client");
         if (in == null) {
             throw new ClientBinaryNotFoundException();
         }
         ZipEntry entry = new ZipEntry("client");
         zos.putNextEntry(entry);
 
         int data;
 
         while ((data = in.read()) > -1) {
             zos.write(data);
         }
         zos.closeEntry();
         in.close();
     }
 
     /**
      * Exports all jobs and all columns currently visible to a CSV file.
      * @param file
      * @throws IOException
      */
     public void exportCSV(File file, Tasks task) throws IOException {
         Tasks.getTaskView().setCancelable(true);
         task.setOperationName("Exporting jobs to CSV file");
 
         if (file.exists()) {
             file.delete();
         }
 
         PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
         for (int i = 0; i < main.jobsTableModel.getColumnCount(); i++) {
             out.write("\"" + main.jobsTableModel.getColumnName(i) + "\"");
             if (i < main.jobsTableModel.getColumnCount() - 1) {
                 out.write(",");
             }
         }
         out.write('\n');
 
         int total = main.getTableJobs().getRowCount();
         int done = 0;
         for (int i = 0; i < main.jobsTableModel.getJobs().size(); i++) {
             int vis = main.getTableJobs().convertRowIndexToView(i);
             if (vis != -1) {
                 done++;
                 task.setTaskProgress((float) done / (float) total);
                 if (task.isCancelled()) {
                     task.setStatus("Cancelled");
                     break;
                 }
                 task.setStatus("Exporting row " + done + " of " + total);
                 for (int col = 0; col < main.jobsTableModel.getColumnCount(); col++) {
                     out.write("\"" + main.jobsTableModel.getValueAt(i, col).toString() + "\"");
                     if (col < main.jobsTableModel.getColumnCount() - 1) {
                         out.write(",");
                     }
                 }
                 out.write('\n');
             }
         }
 
         out.flush();
         out.close();
     }
 
     public Experiment getExperiment(String name) throws SQLException {
         return ExperimentDAO.getExperimentByName(name);
     }
 
     public double getMaxCalculationTimeForSolverConfiguration(SolverConfiguration sc, int status, int run) throws SQLException {
         return ExperimentResultDAO.getMaxCalculationTimeForSolverConfiguration(sc, status, run);
     }
 
     public boolean experimentResultsIsModified(int numRuns) {
         try {
             if (numRuns != activeExperiment.getNumRuns()) {
                 return true;
             }
             Vector<Integer> solverConfigIds = ExperimentResultDAO.getAllSolverConfigIdsByExperimentId(activeExperiment.getId());
             if (solverConfigIds.size() == 0) {
                 return false;
             }
             Vector<Integer> instanceIds = ExperimentResultDAO.getAllInstanceIdsByExperimentId(activeExperiment.getId());
             if (!SolverConfigurationDAO.getAllSolverConfigIdsByExperimentId(activeExperiment.getId()).equals(solverConfigIds)) {
                 return true;
             }
             if (!ExperimentHasInstanceDAO.getAllInstanceIdsByExperimentId(activeExperiment.getId()).equals(instanceIds)) {
                 return true;
             }
         } catch (SQLException _) {
         }
         return false;
     }

     public void checkForR() throws REngineInitializationException {
         try {
             System.loadLibrary("jri");
         } catch (Throwable e) {
             throw new REngineInitializationException(e.getMessage());
         }
     }
 }
