 package edacc.experiment;
 
 import edacc.EDACCExperimentMode;
 import edacc.EDACCSolverConfigEntry;
 import edacc.EDACCSolverConfigPanel;
 import edacc.EDACCSolverConfigPanelSolver;
 import edacc.model.ComputationMethodDoesNotExistException;
 import edacc.model.DatabaseConnector;
 import edacc.model.ExpResultHasSolvPropertyNotInDBException;
 import edacc.model.Experiment;
 import edacc.model.ExperimentDAO;
 import edacc.model.ExperimentHasGridQueue;
 import edacc.model.ExperimentHasGridQueueDAO;
 import edacc.model.ExperimentHasInstance;
 import edacc.model.ExperimentHasInstanceDAO;
 import edacc.model.ExperimentResult;
 import edacc.model.ExperimentResultDAO;
 import edacc.model.ExperimentResultDAO.IdValue;
 import edacc.model.ExperimentResultHasProperty;
 import edacc.model.ExperimentResultNotInDBException;
 import edacc.model.ResultCode;
 import edacc.model.ResultCodeNotInDBException;
 import edacc.model.StatusCode;
 import edacc.model.StatusCodeDAO;
 import edacc.model.GridQueue;
 import edacc.model.GridQueueDAO;
 import edacc.model.Instance;
 import edacc.model.InstanceClassDAO;
 import edacc.model.InstanceClassMustBeSourceException;
 import edacc.model.InstanceDAO;
 import edacc.model.InstanceHasProperty;
 import edacc.model.InstanceNotInDBException;
 import edacc.model.NoConnectionToDBException;
 import edacc.model.Property;
 import edacc.model.PropertyDAO;
 import edacc.model.Solver;
 import edacc.model.SolverConfiguration;
 import edacc.model.SolverConfigurationDAO;
 import edacc.model.SolverDAO;
 import edacc.model.PropertyNotInDBException;
 import edacc.model.ResultCodeDAO;
 import edacc.model.StatusCodeNotInDBException;
 import edacc.model.TaskCancelledException;
 import edacc.model.Tasks;
 import edacc.properties.PropertyTypeNotExistException;
 import edacc.satinstances.ConvertException;
 import edacc.satinstances.PropertyValueType;
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
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Random;
 import java.util.Vector;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 import javax.swing.SwingUtilities;
 import javax.swing.tree.DefaultMutableTreeNode;
 
 /**
  * Experiment design more controller class, handles requests by the GUI
  * for creating, removing, loading, etc. experiments.
  * @author daniel, simon
  */
 public class ExperimentController {
 
     EDACCExperimentMode main;
     EDACCSolverConfigPanel solverConfigPanel;
     private Experiment activeExperiment;
     private ArrayList<Experiment> experiments;
     private static RandomNumberGenerator rnd = new JavaRandom();
     // caching experiments
     private ExperimentResultCache experimentResultCache;
     public static Property PROP_CPUTIME;
 
     /**
      * Creates a new experiment Controller
      * @param experimentMode
      * @param solverConfigPanel
      */
     public ExperimentController(EDACCExperimentMode experimentMode, EDACCSolverConfigPanel solverConfigPanel) {
         this.main = experimentMode;
         this.solverConfigPanel = solverConfigPanel;
         PROP_CPUTIME = new Property();
         PROP_CPUTIME.setName("CPU-Time (s)");
 
     }
 
     /**
      * Initializes the experiment controller. Loads the experiments and the instances classes.
      * @throws SQLException
      */
     public void initialize() throws SQLException, InstanceClassMustBeSourceException, IOException, NoConnectionToDBException, PropertyNotInDBException, PropertyTypeNotExistException, ComputationMethodDoesNotExistException {
         InstanceDAO.clearCache();
         StatusCodeDAO.initialize();
         ResultCodeDAO.initialize();
         ArrayList<Experiment> v = new ArrayList<Experiment>();
         v.addAll(ExperimentDAO.getAll());
         experiments = v;
         main.expTableModel.setExperiments(experiments);
         try {
             DefaultMutableTreeNode root = (DefaultMutableTreeNode) InstanceClassDAO.getAllAsTreeFast();
             main.instanceClassTreeModel.setRoot(root);
             ArrayList<Instance> instances = new ArrayList<Instance>();
             instances.addAll(InstanceDAO.getAll());
             main.insTableModel.setInstances(instances);
         } catch (Exception e) {
             e.printStackTrace();
         }
 
     }
 
     /**
      * Loads an experiment, the solvers and the solver configurations.
      * @param exp
      * @param task
      * @throws SQLException
      */
     public void loadExperiment(Experiment exp, Tasks task) throws SQLException, Exception {
         main.reinitializeSolvers();
         activeExperiment = exp;
         main.solverConfigPanel.beginUpdate();
         solverConfigPanel.removeAll();
         SolverConfigurationDAO.clearCache();
         ArrayList<Solver> vs = new ArrayList<Solver>();
         ArrayList<SolverConfiguration> vss = new ArrayList<SolverConfiguration>();
         Vector<ExperimentHasInstance> ehi = new Vector<ExperimentHasInstance>();
         task.setStatus("Loading solvers..");
         vs.addAll(SolverDAO.getAll());
         main.solTableModel.setSolvers(vs);
         task.setTaskProgress(.25f);
         task.setStatus("Loading solver configurations..");
         vss.addAll(SolverConfigurationDAO.getSolverConfigurationByExperimentId(exp.getId()));
         for (int i = 0; i < vss.size(); i++) {
             main.solverConfigPanel.addSolverConfiguration(vss.get(i));
         }
         main.solverConfigPanel.endUpdate();
         task.setTaskProgress(.5f);
         task.setStatus("Loading instances..");
         // select instances for the experiment
         ehi.addAll(ExperimentHasInstanceDAO.getExperimentHasInstanceByExperimentId(activeExperiment.getId()));
         main.insTableModel.setExperimentHasInstances(ehi);
 
         task.setTaskProgress(.75f);
         task.setStatus("Loading experiment results..");
         experimentResultCache = new ExperimentResultCache(activeExperiment);
         experimentResultCache.updateExperimentResults();
 
         main.generateJobsTableModel.updateNumRuns();
         Util.updateTableColumnWidth(main.tblGenerateJobs);
 
         main.afterExperimentLoaded();
     }
 
     /**
      * Removes an experiment form the db.
      * @param id
      * @throws SQLException
      */
     public void removeExperiment(int id) throws SQLException, InstanceClassMustBeSourceException, IOException, NoConnectionToDBException, PropertyNotInDBException, PropertyTypeNotExistException, ComputationMethodDoesNotExistException {
         Experiment e = ExperimentDAO.getById(id);
         if (e.equals(activeExperiment)) {
             unloadExperiment();
         }
         ExperimentDAO.removeExperiment(e);
         initialize();
     }
 
     public ExperimentResultCache getExperimentResults() {
         return experimentResultCache;
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
         experimentResultCache = null;
         main.afterExperimentUnloaded();
     }
 
     /**
      * invoked by the UI to create a new experiment, also calls initialize to load
      * instances and solvers
      * @param name
      * @param description
      * @throws SQLException
      * @throws InstanceClassMustBeSourceException
      * @throws IOException
      * @throws NoConnectionToDBException
      * @throws PropertyNotInDBException
      * @throws PropertyTypeNotExistException
      * @throws ComputationMethodDoesNotExistException
      */
     public Experiment createExperiment(String name, String description) throws SQLException, InstanceClassMustBeSourceException, IOException, NoConnectionToDBException, PropertyNotInDBException, PropertyTypeNotExistException, ComputationMethodDoesNotExistException {
         java.util.Date d = new java.util.Date();
         Experiment res = ExperimentDAO.createExperiment(name, new Date(d.getTime()), description);
         initialize();
         return res;
     }
 
     /**
      * This will save the settings for the given experiment.
      * @param exp the experiment
      * @throws SQLException
      */
     public void saveExperiment(Experiment exp) throws SQLException {
         ExperimentDAO.save(exp);
     }
 
     /**
      * Saves all solver configurations with parameter instances in the solver
      * config panel.
      * @throws SQLException
      */
    public void saveSolverConfigurations(Tasks task) throws SQLException, InterruptedException, InvocationTargetException, PropertyNotInDBException, PropertyTypeNotExistException, IOException, NoConnectionToDBException, ComputationMethodDoesNotExistException, ExpResultHasSolvPropertyNotInDBException, ExperimentResultNotInDBException, StatusCodeNotInDBException, ResultCodeNotInDBException, Exception {
         // TODO: there are some points where this task should never be canceled (by an application crash or lost db connection)... fix them!
         task.setStatus("Checking jobs..");
 
         // check for deleted solver configurations (jobs have to be deleted)
         ArrayList<SolverConfiguration> deletedSolverConfigurations = SolverConfigurationDAO.getAllDeleted();
         final ArrayList<ExperimentResult> deletedJobs = new ArrayList<ExperimentResult>();
         for (SolverConfiguration sc : deletedSolverConfigurations) {
             deletedJobs.addAll(ExperimentResultDAO.getAllBySolverConfiguration(sc));
         }
 
         // check for modified solver configurations (jobs have to be deleted)
         // this will append (to deletedJobs) all modified solver configurations, i.e. the solver configurations are not marked as deleted and
         //  * the seed group of the solver configurations has been changed or
         //  * some parameter instances have been modified/deleted or added
         ArrayList<SolverConfiguration> modifiedSolverConfigurations = solverConfigPanel.getModifiedSolverConfigurations();
         for (SolverConfiguration sc : modifiedSolverConfigurations) {
             deletedJobs.addAll(ExperimentResultDAO.getAllBySolverConfiguration(sc));
         }
         if (deletedJobs.size() > 0) {
             int notDeletableJobsCount = 0;
             for (ExperimentResult job : deletedJobs) {
                 if (job.getStatus() != StatusCode.NOT_STARTED) {
                     notDeletableJobsCount++;
                 }
             }
             String msg = "";
             if (notDeletableJobsCount > 0) {
                 msg = "There are " + notDeletableJobsCount + " started jobs and " + (deletedJobs.size() - notDeletableJobsCount) + " jobs waiting in the database which would be deleted. ";
             } else {
                 msg = "There are " + deletedJobs.size() + " jobs waiting in the database which would be deleted. ";
             }
             msg += "Do you want to continue?";
             int userInput = javax.swing.JOptionPane.showConfirmDialog(Tasks.getTaskView(), msg, "Jobs would be changed", javax.swing.JOptionPane.YES_NO_OPTION);
             if (userInput == 1) {
                 return;
             } else {
                 task.setStatus("Deleting jobs..");
                 ExperimentResultDAO.deleteExperimentResults(deletedJobs);
                 activeExperiment.invalidateNumJobs();
             }
         }
         task.setStatus("Saving solver configurations..");
         boolean invalidSeedGroup = false;
 
         for (EDACCSolverConfigPanelSolver solPanel : solverConfigPanel.getAllSolverConfigSolverPanels()) {
             // iterate over solvers
             int idx = 0;
             for (EDACCSolverConfigEntry entry : solPanel.getAllSolverConfigEntries()) {
                 // iterate over solver configs
                 int seed_group = 0;
                 try {
                     seed_group = Integer.valueOf(entry.getSeedGroup().getText());
                 } catch (NumberFormatException e) {
                     seed_group = 0;
                     entry.getSeedGroup().setText("0");
                     invalidSeedGroup = true;
                 }
                 if (entry.getSolverConfiguration() == null) {
                     // TODO: what if there are no solver binaries?!!
                     entry.setSolverConfiguration(SolverConfigurationDAO.createSolverConfiguration(entry.getSolverBinary(), activeExperiment.getId(), seed_group, entry.getTitle(), idx));
                 } else {
                     entry.getSolverConfiguration().setName(entry.getTitle());
                     entry.getSolverConfiguration().setSeed_group(seed_group);
                     entry.getSolverConfiguration().setIdx(idx);
 
                 }
                 entry.saveParameterInstances();
                 idx++;
             }
         }
         SolverConfigurationDAO.saveAll();
         main.generateJobsTableModel.updateNumRuns();
         Util.updateTableColumnWidth(main.tblGenerateJobs);
         if (invalidSeedGroup) {
             SwingUtilities.invokeAndWait(new Runnable() {
 
                 @Override
                 public void run() {
                     javax.swing.JOptionPane.showMessageDialog(Tasks.getTaskView(), "Seed groups have to be integers, defaulted to 0", "Expected integer for seed groups", javax.swing.JOptionPane.ERROR_MESSAGE);
                 }
             });
         }
     }
 
     /**
      * This will clean up all changed data for the solver configurations.
      * @param task
      * @throws SQLException
      */
     public void undoSolverConfigurations(Tasks task) throws SQLException {
         main.solverConfigPanel.beginUpdate();
         main.solverConfigPanel.removeAll();
         ArrayList<SolverConfiguration> solverConfigurations = SolverConfigurationDAO.getAllCached();
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
     public void saveExperimentHasInstances(Tasks task) throws SQLException, InterruptedException, InvocationTargetException, PropertyNotInDBException, PropertyTypeNotExistException, IOException, NoConnectionToDBException, ComputationMethodDoesNotExistException, ExpResultHasSolvPropertyNotInDBException, ExperimentResultNotInDBException, StatusCodeNotInDBException, ResultCodeNotInDBException {
         task.setStatus("Checking jobs..");
         ArrayList<ExperimentHasInstance> deletedInstances = main.insTableModel.getDeletedExperimentHasInstances();
         if (deletedInstances.size() > 0) {
             ArrayList<ExperimentResult> deletedJobs = new ArrayList<ExperimentResult>();
             for (ExperimentHasInstance ehi : deletedInstances) {
                 deletedJobs.addAll(ExperimentResultDAO.getAllByExperimentHasInstance(ehi));
             }
             int notDeletableJobsCount = 0;
             for (ExperimentResult job : deletedJobs) {
                 if (job.getStatus() != StatusCode.NOT_STARTED) {
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
                 activeExperiment.invalidateNumJobs();
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
         main.generateJobsTableModel.updateNumRuns();
         Util.updateTableColumnWidth(main.tblGenerateJobs);
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
      * Doesn't overwrite existing jobs
      * @throws SQLException
      * @param numRuns
      * @return number of jobs added to the experiment results table
      * @throws SQLException
      */
     public synchronized int generateJobs(final Tasks task, int cpuTimeLimit, int memoryLimit, int wallClockTimeLimit, int stackSizeLimit, int outputSizeLimit) throws SQLException, TaskCancelledException, IOException, PropertyTypeNotExistException, PropertyNotInDBException, NoConnectionToDBException, ComputationMethodDoesNotExistException, ExpResultHasSolvPropertyNotInDBException, ExperimentResultNotInDBException, StatusCodeNotInDBException, ResultCodeNotInDBException {
         PropertyChangeListener cancelExperimentResultDAOStatementListener = new PropertyChangeListener() {
 
             @Override
             public void propertyChange(PropertyChangeEvent evt) {
                 if ("state".equals(evt.getPropertyName()) && task.isCancelled()) {
                     try {
                         ExperimentResultDAO.cancelStatement();
                     } catch (SQLException ex) {
                     }
                 }
             }
         };
 
         task.setOperationName("Generating jobs for experiment " + activeExperiment.getName());
         task.setStatus("Loading data from database");
         experimentResultCache.updateExperimentResults();
         // get instances of this experiment
         LinkedList<Instance> listInstances = InstanceDAO.getAllByExperimentId(activeExperiment.getId());
 
         // get solver configurations of this experiment
         ArrayList<SolverConfiguration> vsc = SolverConfigurationDAO.getSolverConfigurationByExperimentId(activeExperiment.getId());
 
         int experiments_added = 0;
         HashMap<SeedGroup, Integer> linked_seeds = new HashMap<SeedGroup, Integer>();
         ArrayList<ExperimentResult> experiment_results = new ArrayList<ExperimentResult>();
 
         ArrayList<ExperimentResult> deleteJobs = new ArrayList<ExperimentResult>();
         ArrayList<IdValue<Integer>> updateJobs = new ArrayList<IdValue<Integer>>();
         task.setStatus("Preparing..");
         int elements = 0; // # jobs when finished
         for (Instance i : listInstances) {
             for (SolverConfiguration sc : vsc) {
                 int numRuns = main.generateJobsTableModel.getNumRuns(i, sc);
                 elements += numRuns;
                 int currentNumRuns = experimentResultCache.getResults(sc.getId(), i.getId()).size();
                 if (currentNumRuns > numRuns) {
                     // we have to delete jobs
                     ArrayList<Integer> runs = new ArrayList<Integer>();
                     for (int run = 0; run < currentNumRuns; run++) {
                         runs.add(run);
                     }
                     int runsToDelete = currentNumRuns - numRuns;
                     Random random = new Random();
                     for (int k = 0; k < runsToDelete; k++) {
                         if (runs.isEmpty()) {
                             break;
                         }
                         int index = random.nextInt(runs.size());
                         deleteJobs.add(experimentResultCache.getResult(sc.getId(), i.getId(), runs.get(index)));
                         runs.remove(index);
                     }
 
                     for (int k = 0; k < runs.size(); k++) {
                         updateJobs.add(new IdValue<Integer>(experimentResultCache.getResult(sc.getId(), i.getId(), runs.get(k)).getId(), k));
                     }
                 }
             }
         }
 
         if (!deleteJobs.isEmpty()) {
             // We have to delete jobs
             String msg = "The number of runs specified is less than the number of runs in this experiment. There are " + deleteJobs.size() + " jobs which would be deleted. Do you want to continue?";
             int userInput = javax.swing.JOptionPane.showConfirmDialog(Tasks.getTaskView(), msg, "Jobs would be deleted", javax.swing.JOptionPane.YES_NO_OPTION);
             Tasks.getTaskView().setCancelable(true);
             task.setTaskProgress(0.f);
             if (userInput == 1) {
                 return 0;
             } else {
                 task.setStatus("Deleting jobs..");
                 task.addPropertyChangeListener(cancelExperimentResultDAOStatementListener);
                 try {
                     ExperimentResultDAO.setAutoCommit(false);
                     ExperimentResultDAO.deleteExperimentResults(deleteJobs);
                     task.setStatus("Updating existing jobs..");
                     ExperimentResultDAO.batchUpdateRun(updateJobs);
                 } catch (Exception ex) {
                     DatabaseConnector.getInstance().getConn().rollback();
                     if (ex.getMessage().contains("cancelled")) {
                         throw new TaskCancelledException();
                     }
                     if (ex instanceof SQLException) {
                         throw (SQLException) ex;
                     } else if (ex instanceof TaskCancelledException) {
                         throw (TaskCancelledException) ex;
                     }
                 } finally {
                     ExperimentResultDAO.setAutoCommit(true);
                 }
                 task.removePropertyChangeListener(cancelExperimentResultDAOStatementListener);
             }
         }
 
         Tasks.getTaskView().setCancelable(false);
         task.setStatus("Updating local cache..");
         experimentResultCache.updateExperimentResults();
         Tasks.getTaskView().setCancelable(true);
 
         task.setStatus("Preparing job generation");
 
         if (activeExperiment.isAutoGeneratedSeeds() && activeExperiment.isLinkSeeds()) {
             // first pass over already existing jobs to accumulate existing linked seeds
             for (Instance i : listInstances) {
                 for (SolverConfiguration c : vsc) {
                     ArrayList<ExperimentResult> jobs = experimentResultCache.getResults(c.getId(), i.getId());
                     for (ExperimentResult job : jobs) {
                         // use the already existing jobs to populate the seed group hash table so jobs of newly added solver configs use
                         // the same seeds as already existing jobs
                         int seed = job.getSeed();
                         SeedGroup sg = new SeedGroup(c.getSeed_group(), i.getId(), job.getRun());
                         if (!linked_seeds.containsKey(sg)) {
                             linked_seeds.put(sg, new Integer(seed));
                         }
                     }
                 }
             }
         }
         if (task.isCancelled()) {
             throw new TaskCancelledException();
         }
 
 
         int done = 1;
 
         for (Instance i : listInstances) {
             for (SolverConfiguration c : vsc) {
                 for (int run = 0; run < main.generateJobsTableModel.getNumRuns(i, c); ++run) {
                     task.setTaskProgress((float) done / (float) elements);
                     if (task.isCancelled()) {
                         throw new TaskCancelledException();
                     }
                     task.setStatus("Adding job " + done + " of " + elements);
                     // check if job already exists
                     if (!experimentResultCache.contains(c.getId(), i.getId(), run)) {
                         if (activeExperiment.isAutoGeneratedSeeds() && activeExperiment.isLinkSeeds()) {
                             Integer seed = linked_seeds.get(new SeedGroup(c.getSeed_group(), i.getId(), run));
                             if (seed != null) {
                                 experiment_results.add(ExperimentResultDAO.createExperimentResult(run, 0, 0, StatusCode.NOT_STARTED, seed.intValue(), ResultCode.UNKNOWN, 0, c.getId(), activeExperiment.getId(), i.getId(), null, cpuTimeLimit, memoryLimit, wallClockTimeLimit, stackSizeLimit, outputSizeLimit));
                             } else {
                                 Integer new_seed = new Integer(generateSeed(activeExperiment.getMaxSeed()));
                                 linked_seeds.put(new SeedGroup(c.getSeed_group(), i.getId(), run), new_seed);
                                 experiment_results.add(ExperimentResultDAO.createExperimentResult(run, 0, 0, StatusCode.NOT_STARTED, new_seed.intValue(), ResultCode.UNKNOWN, 0, c.getId(), activeExperiment.getId(), i.getId(), null, cpuTimeLimit, memoryLimit, wallClockTimeLimit, stackSizeLimit, outputSizeLimit));
                             }
                         } else if (activeExperiment.isAutoGeneratedSeeds() && !activeExperiment.isLinkSeeds()) {
                             experiment_results.add(ExperimentResultDAO.createExperimentResult(run, 0, 0, StatusCode.NOT_STARTED, generateSeed(activeExperiment.getMaxSeed()), ResultCode.UNKNOWN, 0, c.getId(), activeExperiment.getId(), i.getId(), null, cpuTimeLimit, memoryLimit, wallClockTimeLimit, stackSizeLimit, outputSizeLimit));
                         } else {
                             experiment_results.add(ExperimentResultDAO.createExperimentResult(run, 0, 0, StatusCode.NOT_STARTED, 0, ResultCode.UNKNOWN, 0, c.getId(), activeExperiment.getId(), i.getId(), null, cpuTimeLimit, memoryLimit, wallClockTimeLimit, stackSizeLimit, outputSizeLimit));
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
         experimentResultCache.updateExperimentResults();
         activeExperiment.invalidateNumJobs();
         main.generateJobsTableModel.updateNumRuns();
         SwingUtilities.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 Util.updateTableColumnWidth(main.tblGenerateJobs);
             }
         });
         return experiments_added;
 
     }
 
     /**
      * Updates the given parameters in the current experiment and saves the changes to the db.
      * @param maxSeed
      * @param generateSeeds
      * @param linkSeeds
      * @param active
      * @param priority
      * @throws SQLException
      * @throws InstanceClassMustBeSourceException
      * @throws IOException
      * @throws NoConnectionToDBException
      * @throws PropertyNotInDBException
      * @throws PropertyTypeNotExistException
      * @throws ComputationMethodDoesNotExistException
      */
     public void saveExperimentParameters(Integer maxSeed, boolean generateSeeds, boolean linkSeeds, boolean active, int priority) throws SQLException, InstanceClassMustBeSourceException, IOException, NoConnectionToDBException, PropertyNotInDBException, PropertyTypeNotExistException, ComputationMethodDoesNotExistException {
         activeExperiment.setAutoGeneratedSeeds(generateSeeds);
         activeExperiment.setMaxSeed(maxSeed);
         activeExperiment.setLinkSeeds(linkSeeds);
         activeExperiment.setActive(active);
         activeExperiment.setPriority(priority);
         ExperimentDAO.save(activeExperiment);
         initialize();
     }
 
     /**
      * returns the number maximum run of the jobs in the database for the given experiment
      * @return the number of jobs in the db
      */
     public int getMaxRun() {
         try {
             return ExperimentResultDAO.getMaximumRun(activeExperiment);
         } catch (Exception e) {
             return 0;
         }
     }
 
     /**
      * Updates the job browser table
      */
     public synchronized void loadJobs() {
         try {
             experimentResultCache.updateExperimentResults();
             final ExperimentResultsBrowserTableModel sync = main.jobsTableModel;
             synchronized (sync) {
 
                 ArrayList<ExperimentResult> results = main.jobsTableModel.getJobs();
                 boolean[] updateRows = null;
                 if (results != null) {
                     updateRows = new boolean[results.size()];
                     if (results.size() != experimentResultCache.size()) {
                         results = null;
                     } else {
                         for (int i = 0; i < results.size(); i++) {
                             ExperimentResult er = results.get(i);
                             ExperimentResult tmp = experimentResultCache.getResult(er.getSolverConfigId(), er.getInstanceId(), er.getRun());
                             if (tmp == null) {
                                 results = null;
                                 break;
                             } else if (!er.getDatemodified().equals(tmp.getDatemodified())) {
                                 results.set(i, tmp);
                                 updateRows[i] = true;
                             }
                         }
                         if (results != null && results.size() != experimentResultCache.size()) {
                             results = null;
                         }
                     }
                 }
                 if (results == null) {
                     results = new ArrayList<ExperimentResult>();
                     results.addAll(experimentResultCache.values());
                     main.jobsTableModel.setJobs(results);
                     main.resultBrowserRowFilter.updateFilterTypes();
                     main.jobsTableModel.fireTableDataChanged();
                 } else {
                     // repaint the table: updates the currently visible rectangle, otherwise there might be duplicates of rows.
                     // this has to be done in the EDT.
                     final boolean[] urows = updateRows;
                     SwingUtilities.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             int beg = -1;
                             for (int i = 0; i < urows.length; i++) {
                                 if (urows[i]) {
                                     if (beg == -1) {
                                         beg = i;
                                     }
                                 } else {
                                     if (beg != -1) {
                                         main.jobsTableModel.fireTableRowsUpdated(beg, i);
                                         beg = -1;
                                     }
                                 }
                             }
                             main.tableJobs.invalidate();
                             main.tableJobs.revalidate();
                             main.tableJobs.repaint();
                         }
                     });
                 }
                 SwingUtilities.invokeLater(new Runnable() {
 
                     @Override
                     public void run() {
                         main.updateJobsFilterStatus();
                     }
                 });
             }
         } catch (Exception e) {
             e.printStackTrace();
             // TODO: shouldn't happen but show message if it does
         }
 
     }
 
     /**
      * Deletes all illegal characters of filename and returns the result.
      * @param filename
      * @return
      */
     private String getFilename(String filename) {
         final char[] ILLEGAL_CHARACTERS = {'/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':', ' '};
         String res = "";
         for (char c : filename.toCharArray()) {
             boolean illegal = false;
             for (char i : ILLEGAL_CHARACTERS) {
                 if (c == i) {
                     illegal = true;
                 }
             }
             if (illegal) {
                 res += "_";
             } else {
                 res += c;
             }
         }
         return res;
     }
 
     /**
      * Generates a ZIP archive with the necessary files for the grid.
      */
     public void generatePackage(String location, boolean exportInstances, boolean exportSolvers, Tasks task) throws FileNotFoundException, IOException, NoConnectionToDBException, SQLException, ClientBinaryNotFoundException, InstanceNotInDBException, TaskCancelledException {
         boolean foundSolverWithSameName = false;
         File tmpDir = new File("tmp");
         tmpDir.mkdir();
         Tasks.getTaskView().setCancelable(true);
         Calendar cal = Calendar.getInstance();
         String dateStr = cal.get(Calendar.YEAR) + "" + (cal.get(Calendar.MONTH) < 9 ? "0" + (cal.get(Calendar.MONTH) + 1) : (cal.get(Calendar.MONTH) + 1)) + "" + (cal.get(Calendar.DATE) < 10 ? "0" + cal.get(Calendar.DATE) : cal.get(Calendar.DATE));
         ArrayList<ExperimentHasGridQueue> eqs = ExperimentHasGridQueueDAO.getExperimentHasGridQueueByExperiment(activeExperiment);
         int count = 0;
         for (ExperimentHasGridQueue eq : eqs) {
             GridQueue queue = GridQueueDAO.getById(eq.getIdGridQueue());
 
             File zipFile = new File(location + getFilename(activeExperiment.getName() + "_" + queue.getName() + "_" + dateStr + ".zip"));
             if (zipFile.exists()) {
                 zipFile.delete();
             }
             ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
             ZipEntry entry;
 
 
             task.setOperationName("Generating Package " + (++count) + " of " + eqs.size());
             ArrayList<Solver> solvers;
             if (exportSolvers) {
                 solvers = ExperimentDAO.getSolversInExperiment(activeExperiment);
             } else {
                 solvers = new ArrayList<Solver>();
             }
 
             LinkedList<Instance> instances;
             if (exportInstances) {
                 instances = InstanceDAO.getAllByExperimentId(activeExperiment.getId());
             } else {
                 instances = new LinkedList<Instance>();
             }
 
             int total = solvers.size() + instances.size();
             int done = 0;
 
 
            /* TODO !!!! HashSet<String> tmp = new HashSet<String>();
             HashSet<String> solvernameMap = new HashSet<String>();
             for (Solver s : solvers) {
                 if (tmp.contains(s.getBinaryName() + "_" + s.getVersion())) {
                     solvernameMap.add(s.getBinaryName() + "_" + s.getVersion());
                     foundSolverWithSameName = true;
                 } else {
                     tmp.add(s.getBinaryName() + "_" + s.getVersion());
                 }
             }*/
 
             if (!task.isCancelled() && exportSolvers) {
                 // add solvers to zip file
                 for (Solver s : solvers) {
                     done++;
                     task.setTaskProgress((float) done / (float) total);
                     if (task.isCancelled()) {
                         task.setStatus("Cancelled");
                         break;
                     }
                     task.setStatus("Writing solver " + done + " of " + solvers.size());
               /* TODO     File bin = SolverDAO.getBinaryFileOfSolver(s);
                     String filename;
                     if (solvernameMap.contains(s.getBinaryName())) {
                         filename = s.getBinaryName() + "_" + s.getVersion() + "_" + s.getMd5().substring(0, 3);
                     } else {
                         filename = s.getBinaryName() + "_" + s.getVersion();
                     }
                     entry = new ZipEntry("solvers" + System.getProperty("file.separator") + filename);
                     addFileToZIP(bin, entry, zos);*/
                 }
             }
 
             if (!task.isCancelled() && exportInstances) {
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
             }
 
             if (!task.isCancelled()) {
                 task.setStatus("Writing client");
 
                 // add configuration File
                 addConfigurationFile(zos, activeExperiment, queue);
 
                 // add run script
                 addRunScript(zos, exportInstances, exportSolvers, queue);
 
                 // add client binary
                 addClient(zos);
 
                 // add runsolver
                 addRunsolver(zos);
                 // add empty result library
                 entry = new ZipEntry("results" + System.getProperty("file.separator") + "~");
                 zos.putNextEntry(entry);
             }
             zos.close();
 
             // delete tmp directory
             deleteDirectory(new File("tmp"));
 
             if (task.isCancelled()) {
                 throw new TaskCancelledException("Cancelled");
             }
         }
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
      * Assigns all gridQueues to the active experiment.
      * This means: It creates a new ExperimentHasGridQueue object for each queue
      * and deletes all ExperimentHasGridQueue objects which are not in queues vektor
      * and persists it in the db
      * @param queues
      * @throws SQLException
      */
     public void assignQueuesToExperiment(ArrayList<GridQueue> queues) throws SQLException {
         boolean autoCommit = DatabaseConnector.getInstance().getConn().getAutoCommit();
         try {
             DatabaseConnector.getInstance().getConn().setAutoCommit(false);
             // create all ExperimentHasGridQueue objects which do not exist
             for (GridQueue q : queues) {
                 // check if assignment already exists
                 if (ExperimentHasGridQueueDAO.getByExpAndQueue(activeExperiment, q) != null) {
                     continue;
                 }
                 ExperimentHasGridQueueDAO.createExperimentHasGridQueue(activeExperiment, q);
             }
 
             // remove all ExperimentHasGridQueue objects which are not in the queues vektor
             ArrayList<ExperimentHasGridQueue> ehgqs = ExperimentHasGridQueueDAO.getExperimentHasGridQueueByExperiment(activeExperiment);
             for (ExperimentHasGridQueue egq : ehgqs) {
                 boolean found = false;
                 for (GridQueue q : queues) {
                     if (egq.getIdGridQueue() == q.getId()) {
                         found = true;
                         break;
                     }
                 }
                 if (!found) {
                     ExperimentHasGridQueueDAO.removeExperimentHasGridQueue(egq);
                 }
             }
         } catch (SQLException e) {
             DatabaseConnector.getInstance().getConn().rollback();
             throw e;
         } finally {
             DatabaseConnector.getInstance().getConn().setAutoCommit(autoCommit);
         }
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
 
     private void addRunScript(ZipOutputStream zos, boolean hasInstances, boolean hasSolvers, GridQueue q) throws IOException {
         String sRun = "#!/bin/bash\n"
                 + "chmod a-rwx client\n"
                 + "chmod u+rwx client\n"
                 + "chmod a-rwx config\n"
                 + "chmod u+rw config\n"
                 + (hasSolvers ? "chmod a-rwx solvers/*\n" : "")
                 + (hasSolvers ? "chmod u+rwx solvers/*\n" : "")
                 + (hasInstances ? "chmod a-rwx instances/*\n" : "")
                 + (hasInstances ? "chmod u+rw instances/*\n" : "")
                 + "for (( i = 0; i < " + q.getNumCPUs() + "; i++ ))\n"
                 + "do\n"
                 + "    qsub start_client.pbs\n"
                 + "done\n";
 
         // write file into zip archive
         ZipEntry entry = new ZipEntry("run.sh");
         zos.putNextEntry(entry);
         zos.write(sRun.getBytes());
         zos.closeEntry();
     }
 
     private void addClient(ZipOutputStream zos) throws IOException, ClientBinaryNotFoundException {
         InputStream in = new FileInputStream(new File(Util.getPath() + System.getProperty("file.separator") + "bin" + System.getProperty("file.separator") + "client"));
         if (in == null) {
             throw new ClientBinaryNotFoundException();
         }
         ZipEntry entry = new ZipEntry("client");
         zos.putNextEntry(entry);
 
         byte[] buf = new byte[1024];
         int data;
 
         while ((data = in.read(buf)) > -1) {
             zos.write(buf, 0, data);
         }
         zos.closeEntry();
         in.close();
     }
 
     private void addRunsolver(ZipOutputStream zos) throws IOException, ClientBinaryNotFoundException {
         InputStream in = new FileInputStream(new File(Util.getPath() + System.getProperty("file.separator") + "bin" + System.getProperty("file.separator") + "runsolver"));
         if (in == null) {
             throw new ClientBinaryNotFoundException();
         }
         ZipEntry entry = new ZipEntry("runsolver");
         zos.putNextEntry(entry);
 
         byte[] buf = new byte[1024];
         int data;
 
         while ((data = in.read(buf)) > -1) {
             zos.write(buf, 0, data);
         }
         zos.closeEntry();
         in.close();
 
         in = new FileInputStream(new File(Util.getPath() + System.getProperty("file.separator") + "bin" + System.getProperty("file.separator") + "runsolver_copyright.txt"));
         if (in == null) {
             throw new ClientBinaryNotFoundException();
         }
         entry = new ZipEntry("runsolver_copyright.txt");
         zos.putNextEntry(entry);
 
         while ((data = in.read(buf)) > -1) {
             zos.write(buf, 0, data);
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
             int vis_col = main.tableJobs.convertColumnIndexToModel(i);
             out.write("\"" + main.jobsTableModel.getColumnName(vis_col) + "\"");
             if (i < main.jobsTableModel.getColumnCount() - 1) {
                 out.write(",");
             }
         }
         out.write('\n');
 
         int total = main.getTableJobs().getRowCount();
         int done = 0;
         for (int i = 0; i < main.tableJobs.getRowCount(); i++) {
             int vis = main.getTableJobs().convertRowIndexToModel(i);
             done++;
             task.setTaskProgress((float) done / (float) total);
             if (task.isCancelled()) {
                 task.setStatus("Cancelled");
                 break;
             }
             task.setStatus("Exporting row " + done + " of " + total);
             for (int col = 0; col < main.jobsTableModel.getColumnCount(); col++) {
                 int vis_col = main.tableJobs.convertColumnIndexToModel(col);
                 if (main.jobsTableModel.getValueAt(vis, vis_col) == null) {
                     out.write("\"-\"");
                 } else {
                     out.write("\"" + main.jobsTableModel.getValueAt(vis, vis_col).toString() + "\"");
                 }
                 if (col < main.jobsTableModel.getColumnCount() - 1) {
                     out.write(",");
                 }
             }
             out.write('\n');
         }
 
         out.flush();
         out.close();
     }
 
     /**
      * Exports all jobs and all columns currently visible to a TeX file.
      * @param file
      * @throws IOException
      */
     public void exportTeX(File file, Tasks task) throws IOException {
         Tasks.getTaskView().setCancelable(true);
         task.setOperationName("Exporting jobs to TeX file");
 
         if (file.exists()) {
             file.delete();
         }
 
         PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
 
         String format = "|";
         String columns = "";
         for (int i = 0; i < main.jobsTableModel.getColumnCount(); i++) {
             int vis_col = main.tableJobs.convertColumnIndexToModel(i);
             columns += main.jobsTableModel.getColumnName(vis_col);
             if (main.jobsTableModel.getColumnClass(vis_col) == Integer.class
                     || main.jobsTableModel.getColumnClass(vis_col) == Double.class
                     || main.jobsTableModel.getColumnClass(vis_col) == Float.class
                     || main.jobsTableModel.getColumnClass(vis_col) == int.class
                     || main.jobsTableModel.getColumnClass(vis_col) == double.class
                     || main.jobsTableModel.getColumnClass(vis_col) == float.class) {
                 format += "r|";
             } else {
                 format += "l|";
             }
             if (i < main.jobsTableModel.getColumnCount() - 1) {
                 columns += "&";
             }
         }
         out.write("\\documentclass[a4paper]{report}\n");
         out.write("\\title{Results of " + activeExperiment.getName() + "}\n");
         out.write("\\begin{document}\n");
         out.write("\\begin{tabular}[h]{" + format + "}\n");
         out.write("\\hline\n");
         out.write(columns + " \\\\\n");
         out.write("\\hline\n");
         int total = main.getTableJobs().getRowCount();
         int done = 0;
         for (int i = 0; i < main.tableJobs.getRowCount(); i++) {
             int vis = main.getTableJobs().convertRowIndexToModel(i);
             done++;
             task.setTaskProgress((float) done / (float) total);
             if (task.isCancelled()) {
                 task.setStatus("Cancelled");
                 break;
             }
             task.setStatus("Exporting row " + done + " of " + total);
             for (int col = 0; col < main.jobsTableModel.getColumnCount(); col++) {
                 int vis_col = main.tableJobs.convertColumnIndexToModel(col);
                 if (main.jobsTableModel.getValueAt(vis, vis_col) == null) {
                     out.write("-");
                 } else {
                     out.write(main.jobsTableModel.getValueAt(vis, vis_col).toString());
                 }
                 if (col < main.jobsTableModel.getColumnCount() - 1) {
                     out.write(" & ");
                 }
             }
             out.write("\\\\\n");
         }
         out.write("\\hline\n");
         out.write("\\end{tabular}\n");
         out.write("\\end{document}\n");
         out.flush();
         out.close();
     }
 
     /**
      * Sets the priority of the currently visible jobs in the job browser to <code>priority</code>,
      * updates the local cached experiment results and updates the gui.
      * @param priority the new priority for the jobs
      * @throws SQLException
      * @throws IOException
      * @throws PropertyTypeNotExistException
      * @throws PropertyNotInDBException
      * @throws NoConnectionToDBException
      * @throws ComputationMethodDoesNotExistException
      * @throws ExpResultHasSolvPropertyNotInDBException
      * @throws ExperimentResultNotInDBException
      */
     public void setPriority(int priority) throws SQLException, IOException, PropertyTypeNotExistException, PropertyNotInDBException, NoConnectionToDBException, ComputationMethodDoesNotExistException, ExpResultHasSolvPropertyNotInDBException, ExperimentResultNotInDBException {
         ArrayList<ExperimentResult> jobs = main.jobsTableModel.getJobs();
         ArrayList<IdValue<Integer>> values = new ArrayList<IdValue<Integer>>();
         for (int i = 0; i < main.tableJobs.getRowCount(); i++) {
             int vis = main.getTableJobs().convertRowIndexToModel(i);
             if (jobs.get(vis).getPriority() != priority) {
                 values.add(new IdValue<Integer>(jobs.get(vis).getId(), priority));
             }
         }
         ExperimentResultDAO.batchUpdatePriority(values);
         this.loadJobs();
     }
 
     /**
      * Sets the status of all currently visible jobs in the job browser to <code>status</code>,
      * updates the local cached experiment results and updates the gui.
      * @param status the new status for the jobs
      * @throws SQLException
      */
     public void setStatus(StatusCode status) throws SQLException {
         ArrayList<ExperimentResult> jobs = main.jobsTableModel.getJobs();
         ArrayList<ExperimentResult> updatedJobs = new ArrayList<ExperimentResult>();
         for (int i = 0; i < main.tableJobs.getRowCount(); i++) {
             int vis = main.getTableJobs().convertRowIndexToModel(i);
             if (!jobs.get(vis).getStatus().equals(status)) {
                 updatedJobs.add(jobs.get(vis));
             }
         }
         ExperimentResultDAO.batchUpdateStatus(updatedJobs, status);
         this.loadJobs();
     }
 
     public ArrayList<Experiment> getExperiments() {
         return experiments;
     }
 
     /**
      * Returns the experiment with the given name.
      * @param name
      * @return the experiment
      * @throws SQLException
      */
     public Experiment getExperiment(String name) throws SQLException {
         return ExperimentDAO.getExperimentByName(name);
     }
 
     public String getExperimentResultOutput(int type, ExperimentResult er) throws SQLException, NoConnectionToDBException, IOException {
         return ExperimentResultDAO.getOutputText(type, er);
     }
 
     /**
      * returns a hashmap containing the maximum limits of the experiment results for the currently loaded experiment. 
      * <br/>
      * <br/>
      * possible keys are: cpuTimeLimit, memoryLimit, wallClockTimeLimit, stackSizeLimit, outputSizeLimit
      * @return
      */
     public HashMap<String, Integer> getMaxLimits() throws SQLException, Exception {
         HashMap<String, Integer> res = new HashMap<String, Integer>();
         int cpuTimeLimit = -1;
         int memoryLimit = -1;
         int wallClockTimeLimit = -1;
         int stackSizeLimit = -1;
         int outputSizeLimit = -1;
 
         experimentResultCache.updateExperimentResults();
         for (ExperimentResult er : experimentResultCache.values()) {
             if (er.getCPUTimeLimit() > cpuTimeLimit) {
                 cpuTimeLimit = er.getCPUTimeLimit();
             }
             if (er.getMemoryLimit() > memoryLimit) {
                 memoryLimit = er.getMemoryLimit();
             }
             if (er.getWallClockTimeLimit() > wallClockTimeLimit) {
                 wallClockTimeLimit = er.getWallClockTimeLimit();
             }
             if (er.getStackSizeLimit() > stackSizeLimit) {
                 stackSizeLimit = er.getStackSizeLimit();
             }
             if (er.getOutputSizeLimit() > outputSizeLimit) {
                 outputSizeLimit = er.getOutputSizeLimit();
             }
         }
 
         res.put("cpuTimeLimit", cpuTimeLimit);
         res.put("memoryLimit", memoryLimit);
         res.put("wallClockTimeLimit", wallClockTimeLimit);
         res.put("stackSizeLimit", stackSizeLimit);
         res.put("outputSizeLimit", outputSizeLimit);
         return res;
     }
 
     /**
      * Imports data from the specified solver configurations to the currently loaded experiment. <br/>
      * <br/>
      * <br/>
      * Let e_i be the experiments to import data from and e the currently loaded experiment then 
      * the following statements are valid for the merged experiment: <br/>
      *  * instances(e) = UNION{instances(e_i)}
      *  * sc(e) = UNION{sc(e_i)}, sc_1 == sc_2 iff they are equal in their semantics (same solver, same parameters)
      *  * ... tbd
      * @param task
      * @param solverConfigIds
      * @param duplicate
      * @throws SQLException
      * @throws Exception
      */
     public void importDataFromSolverConfigurations(Tasks task, ArrayList<Integer> solverConfigIds, ArrayList<Boolean> duplicate) throws SQLException, Exception {
         /*  if (solverConfigIds.size() != duplicate.size()) {
         throw new IllegalArgumentException("solverConfigIds.size() != duplicate.size()");
         }
         final boolean autocommit = DatabaseConnector.getInstance().getConn().getAutoCommit();
         DatabaseConnector.getInstance().getConn().setAutoCommit(false);
         try {
         HashSet<Integer> experimentIds = new HashSet<Integer>();
         for (int scId : solverConfigIds) {
         experimentIds.add(SolverConfigurationDAO.getSolverConfigurationById(scId).getExperiment_id());
         }
         boolean first = true;
         boolean useNotFinishedJobs = true;
         for (int expId : experimentIds) {
         Experiment exp = ExperimentDAO.getById(expId);
         if (first) {
         first = false;
         activeExperiment.setAutoGeneratedSeeds(exp.isAutoGeneratedSeeds());
         activeExperiment.setCPUTimeLimit(exp.getCPUTimeLimit());
         activeExperiment.setLinkSeeds(exp.isLinkSeeds());
         activeExperiment.setMaxSeed(exp.getMaxSeed());
         activeExperiment.setMemoryLimit(exp.getMemoryLimit());
         activeExperiment.setOutputSizeLimit(exp.getOutputSizeLimit());
         activeExperiment.setStackSizeLimit(exp.getStackSizeLimit());
         activeExperiment.setWallClockTimeLimit(exp.getWallClockTimeLimit());
         } else {
         activeExperiment.setAutoGeneratedSeeds(activeExperiment.isAutoGeneratedSeeds() | exp.isAutoGeneratedSeeds());
         activeExperiment.setLinkSeeds(activeExperiment.isLinkSeeds() | exp.isLinkSeeds());
         if (activeExperiment.getMaxSeed() < exp.getMaxSeed()) {
         activeExperiment.setMaxSeed(exp.getMaxSeed());
         }
         if (activeExperiment.getCPUTimeLimit() != exp.getCPUTimeLimit()) {
         useNotFinishedJobs = false;
         if (activeExperiment.getCPUTimeLimit() < exp.getCPUTimeLimit() && activeExperiment.getCPUTimeLimit() != -1) {
         activeExperiment.setCPUTimeLimit(exp.getCPUTimeLimit());
         }
         }
         if (activeExperiment.getMemoryLimit() != exp.getMemoryLimit()) {
         useNotFinishedJobs = false;
         if (activeExperiment.getMemoryLimit() < exp.getMemoryLimit() && activeExperiment.getMemoryLimit() != -1) {
         activeExperiment.setMemoryLimit(exp.getMemoryLimit());
         }
         }
         if (activeExperiment.getOutputSizeLimit() != exp.getOutputSizeLimit()) {
         useNotFinishedJobs = false;
         if (activeExperiment.getOutputSizeLimit() < exp.getOutputSizeLimit() && activeExperiment.getOutputSizeLimit() != -1) {
         activeExperiment.setOutputSizeLimit(exp.getOutputSizeLimit());
         }
         }
         if (activeExperiment.getStackSizeLimit() != exp.getStackSizeLimit()) {
         useNotFinishedJobs = false;
         if (activeExperiment.getStackSizeLimit() < exp.getStackSizeLimit() && activeExperiment.getStackSizeLimit() != -1) {
         activeExperiment.setStackSizeLimit(exp.getStackSizeLimit());
         }
         }
         if (activeExperiment.getWallClockTimeLimit() != exp.getWallClockTimeLimit()) {
         useNotFinishedJobs = false;
         if (activeExperiment.getWallClockTimeLimit() < exp.getWallClockTimeLimit() && activeExperiment.getWallClockTimeLimit() != -1) {
         activeExperiment.setWallClockTimeLimit(exp.getWallClockTimeLimit());
         }
         }
         }
         }
 
         boolean haveToDuplicate = false;
         //activeExperiment.setAutoGeneratedSeeds(true);
         int numRuns = 0;
         ArrayList<Integer> newIds = new ArrayList<Integer>();
         for (int i = 0; i < solverConfigIds.size(); i++) {
         haveToDuplicate |= duplicate.get(i);
         int scId = solverConfigIds.get(i);
         SolverConfiguration sc = SolverConfigurationDAO.getSolverConfigurationById(scId);
         task.setOperationName("Processing data from solver configuration " + sc.getName() + " (" + (i + 1) + " / " + solverConfigIds.size() + "):");
         task.setTaskProgress((float) (i + 1) / solverConfigIds.size());
         RunCountSCId tmp = this.importDataFromSolverConfiguration(task, sc, useNotFinishedJobs);
         if (tmp.runcount > numRuns) {
         numRuns = tmp.runcount;
         }
         newIds.add(tmp.scid);
         }
         if (haveToDuplicate) {
         task.setStatus("Duplicating results..");
         LinkedList<Instance> instances = InstanceDAO.getAllByExperimentId(activeExperiment.getId());
         updateExperimentResults();
         Random random = new Random();
         ArrayList<ExperimentResult> newResults = new ArrayList<ExperimentResult>();
         ArrayList<ExperimentResult> oldResults = new ArrayList<ExperimentResult>();
         for (int k = 0; k < solverConfigIds.size(); k++) {
         for (Instance instance : instances) {
         if (task.isCancelled()) {
         throw new TaskCancelledException();
         }
         if (duplicate.get(k)) {
         int runCount = this.getResults(newIds.get(k), instance.getId()).size();
         // int runCount = ExperimentDAO.getRunCountInExperimentForSolverConfigurationAndInstance(activeExperiment, newIds.get(k), instance.getId());
         ArrayList<ExperimentResult> results = getResults(newIds.get(k), instance.getId());
         if (results.size() > 0) {
         for (int t = runCount; t < numRuns; t++) {
         ExperimentResult er = results.get(random.nextInt(results.size()));
         newResults.add(ExperimentResultDAO.createExperimentResult(t, er.getPriority(), er.getComputeQueue(), er.getStatus().getValue(), er.getSeed(), er.getResultCode(), er.getResultTime(), er.getSolverConfigId(), activeExperiment.getId(), er.getInstanceId(), er.getStartTime()));
         oldResults.add(er);
         }
         }
         }
         }
         }
         if (newResults.size() > 0) {
         task.setStatus("Saving results..");
         ExperimentResultDAO.batchSave(newResults);
         if (task.isCancelled()) {
         throw new TaskCancelledException();
         }
         task.setStatus("Saving outputs..");
         ExperimentResultDAO.batchCopyOutputs(oldResults, newResults);
         }
         if (task.isCancelled()) {
         throw new TaskCancelledException();
         }
         }
         generateJobs(numRuns, task);
         task.setStatus("Finalizing..");
         ExperimentDAO.save(activeExperiment);
         updateExperimentResults();
         } catch (Exception e) {
         DatabaseConnector.getInstance().getConn().rollback();
         throw e;
         } finally {
         DatabaseConnector.getInstance().getConn().setAutoCommit(autocommit);
         }*/
     }
 
     private class RunCountSCId {
 
         public int runcount;
         public int scid;
 
         public RunCountSCId(int runcount, int scid) {
             this.runcount = runcount;
             this.scid = scid;
         }
     }
 
     private RunCountSCId importDataFromSolverConfiguration(Tasks task, SolverConfiguration solverConfig, boolean useNotFinishedJobs) throws SQLException, Exception {
         // TODO: implement
         return null;
         /*  Tasks.getTaskView().setCancelable(true);
         final boolean autocommit = DatabaseConnector.getInstance().getConn().getAutoCommit();
         DatabaseConnector.getInstance().getConn().setAutoCommit(false);
         try {
         task.setStatus("Loading data..");
         Vector<ExperimentHasInstance> ehi = ExperimentHasInstanceDAO.getExperimentHasInstanceByExperimentId(activeExperiment.getId());
         HashSet<Integer> instanceIds = new HashSet<Integer>();
         for (ExperimentHasInstance e : ehi) {
         instanceIds.add(e.getInstances_id());
         }
         Vector<ExperimentHasInstance> ehi_toAdd = ExperimentHasInstanceDAO.getExperimentHasInstanceByExperimentId(solverConfig.getExperiment_id());
         task.setStatus("Saving instances..");
         for (ExperimentHasInstance e : ehi_toAdd) {
         if (task.isCancelled()) {
         throw new TaskCancelledException();
         }
         if (!instanceIds.contains(e.getInstances_id())) {
         ExperimentHasInstanceDAO.createExperimentHasInstance(activeExperiment.getId(), e.getInstances_id());
         }
         }
 
         task.setStatus("Saving solver configuration..");
         ArrayList<SolverConfiguration> solverConfigs = SolverConfigurationDAO.getSolverConfigurationByExperimentId(activeExperiment.getId());
         Integer equalId = null;
         for (SolverConfiguration sc : solverConfigs) {
         if (task.isCancelled()) {
         throw new TaskCancelledException();
         }
         boolean equal = true;
         if (sc.getSolver_id() == solverConfig.getSolver_id()) {
         ArrayList<ParameterInstance> paramInstances = ParameterInstanceDAO.getBySolverConfigId(sc.getId());
         for (ParameterInstance paramInstanceToAdd : ParameterInstanceDAO.getBySolverConfigId(solverConfig.getId())) {
         boolean found = false;
         for (ParameterInstance paramInstance : paramInstances) {
         if (paramInstance.getParameter_id() == paramInstanceToAdd.getParameter_id()
         && (paramInstance.getValue() == null && paramInstanceToAdd.getValue() == null || paramInstance.getValue().equals(paramInstanceToAdd.getValue()))) {
         found = true;
         break;
         }
         }
         if (!found) {
         equal = false;
         break;
         }
         }
         } else {
         equal = false;
         }
         if (equal) {
         equalId = sc.getId();
         break;
         }
         }
         int newId;
         if (equalId == null) {
         // didn't find solver config -> add one
         SolverConfiguration newSc = SolverConfigurationDAO.createSolverConfiguration(solverConfig.getSolver_id(), activeExperiment.getId(), 0, solverConfig.getName(), SolverConfigurationDAO.getSolverConfigurationCount(activeExperiment.getId(), solverConfig.getSolver_id()));
         SolverConfigurationDAO.saveAll();
         for (ParameterInstance pi : ParameterInstanceDAO.getBySolverConfigId(solverConfig.getId())) {
         ParameterInstanceDAO.createParameterInstance(pi.getParameter_id(), newSc.getId(), pi.getValue());
         }
         // now an id is assigned to newSc
         newId = newSc.getId();
         } else {
         // found solver config -> map id
         newId = equalId;
         }
 
         // At this point: we have the union of all instances and the union of all solver configs saved in the db
 
         task.setStatus("Saving results..");
         LinkedList<Instance> instances = InstanceDAO.getAllByExperimentId(solverConfig.getExperiment_id());
 
         ArrayList<ExperimentResult> results = ExperimentResultDAO.getAllByExperimentId(solverConfig.getExperiment_id());
         ArrayList<ExperimentResult> newResults = new ArrayList<ExperimentResult>();
         ArrayList<ExperimentResult> oldResults = new ArrayList<ExperimentResult>();
         int numRuns = activeExperiment.getNumRuns();
         this.updateExperimentResults();
         for (Instance i : instances) {
         if (task.isCancelled()) {
         throw new TaskCancelledException();
         }
         int runCount = ExperimentDAO.getRunCountInExperimentForSolverConfigurationAndInstance(activeExperiment, newId, i.getId());
         for (ExperimentResult er : results) {
         if (er.getSolverConfigId() == solverConfig.getId() && er.getInstanceId() == i.getId() && (useNotFinishedJobs || er.getStatus() == ExperimentResultStatus.SUCCESSFUL)) {
         newResults.add(ExperimentResultDAO.createExperimentResult(runCount++, er.getPriority(), er.getComputeQueue(), er.getStatus().getValue(), er.getSeed(), er.getResultCode(), er.getResultTime(), newId, activeExperiment.getId(), er.getInstanceId(), er.getStartTime()));
         oldResults.add(er);
         }
         }
         if (runCount > numRuns) {
         numRuns = runCount;
         }
         }
         ExperimentResultDAO.batchSave(newResults);
         if (task.isCancelled()) {
         throw new TaskCancelledException();
         }
         task.setStatus("Saving outputs..");
         ExperimentResultDAO.batchCopyOutputs(oldResults, newResults);
         if (task.isCancelled()) {
         throw new TaskCancelledException();
         }
         return new RunCountSCId(numRuns, newId);
         } catch (Exception e) {
         DatabaseConnector.getInstance().getConn().rollback();
         throw e;
         } finally {
         DatabaseConnector.getInstance().getConn().setAutoCommit(autocommit);
         }*/
     }
 
     /**
      * Checks if data in the experiment design tabs is modified.
      * @param numRuns
      * @return true, iff some data is modified
      */
     public boolean experimentResultsIsModified() {
         try {
             experimentResultCache.updateExperimentResults();
             ArrayList<SolverConfiguration> solverConfigs = SolverConfigurationDAO.getSolverConfigurationByExperimentId(activeExperiment.getId());
             LinkedList<Instance> instances = InstanceDAO.getAllByExperimentId(activeExperiment.getId());
             for (SolverConfiguration sc : solverConfigs) {
                 for (Instance i : instances) {
                     if (main.generateJobsTableModel.getNumRuns(i, sc) != experimentResultCache.getNumRuns(sc.getId(), i.getId())) {
                         return true;
                     }
                 }
             }
         } catch (Exception _) {
         }
         return false;
     }
 
     /**
      * Returns a double value for the given property value type and the string value
      * @param type
      * @param value
      * @return null, iff the property value type doesn't represent Integer, Float or Double or an error occurred.
      */
     private Double transformPropertyValueTypeToDouble(PropertyValueType type, String value) {
         Double res = null;
         try {
             if (type.getJavaType() == Integer.class) {
                 res = new Double((Integer) type.getJavaTypeRepresentation(value));
             } else if (type.getJavaType() == Float.class) {
                 res = new Double((Float) type.getJavaTypeRepresentation(value));
             } else if (type.getJavaType() == Double.class) {
                 res = (Double) type.getJavaTypeRepresentation(value);
             }
         } catch (ConvertException ex) {
             return null;
         }
         return res;
     }
 
     /**
      * Returns the value for the given property and the given experiment result.
      * @param result
      * @param property
      * @return null, iff the experiment result`s status is RUNNING, NOTSTARTED, LAUNCHERCRASH, VERIFIERCRASH, WATCHERCRASH or the property isn't calculated or the result isn't successfully verified. In case of the cpu-time property CPUTimeLmit can be returned.
      */
     public Double getValue(ExperimentResult result, Property property) {
         return getValue(result, property, true);
     }
 
     /**
      * Returns the value for the given property and the given experiment result.
      * @param result
      * @param property
      * @param useTimeOutForCPUProp
      * @return null, iff the experiment result`s status is RUNNING, NOTSTARTED, LAUNCHERCRASH, VERIFIERCRASH, WATCHERCRASH or the property isn't calculated or the result isn't successfully verified. In case of the cpu-time property CPUTimeLmit can be returned.
      */
     public Double getValue(ExperimentResult result, Property property, boolean useTimeOutForCPUProp) {
 
         if (result.getStatus() == StatusCode.RUNNING
                 || result.getStatus() == StatusCode.NOT_STARTED
                 || result.getStatus() == StatusCode.LAUNCHERCRASH
                 || result.getStatus() == StatusCode.VERIFIERCRASH
                 || result.getStatus() == StatusCode.WATCHERCRASH) {
             return null;
         }
         if (property == PROP_CPUTIME) {
             if (!String.valueOf(result.getResultCode().getResultCode()).startsWith("1")) {
                 if (useTimeOutForCPUProp) {
                     return new Double(result.getCPUTimeLimit());
                 } else {
                     return null;
                 }
             }
             return Double.valueOf(result.getResultTime());
         } else {
             if (!String.valueOf(result.getResultCode().getResultCode()).startsWith("1")) {
                 return null;
             }
             ExperimentResultHasProperty erhsp = result.getPropertyValues().get(property.getId());
             if (erhsp == null || erhsp.getValue().isEmpty()) {
                 return null;
             }
             return transformPropertyValueTypeToDouble(property.getPropertyValueType(), erhsp.getValue().get(0));
         }
     }
 
     /**
      * Returns the value of the property for this instance
      * @param instance the instance
      * @param property the property
      * @return null, iff the property isn't calculated or some error occurred.
      */
     public Double getValue(Instance instance, Property property) {
         InstanceHasProperty ihip = instance.getPropertyValues().get(property.getId());
         if (ihip == null) {
             return null;
         }
         return transformPropertyValueTypeToDouble(property.getPropertyValueType(), ihip.getValue());
     }
 
     /**
      * Returns all instances.
      * @return arraylist of the instances
      * @throws SQLException
      * @throws InstanceClassMustBeSourceException
      * @throws IOException
      * @throws NoConnectionToDBException
      * @throws PropertyNotInDBException
      * @throws PropertyTypeNotExistException
      * @throws ComputationMethodDoesNotExistException
      */
     public ArrayList<Instance> getInstances() throws SQLException, InstanceClassMustBeSourceException, IOException, NoConnectionToDBException, PropertyNotInDBException, PropertyTypeNotExistException, ComputationMethodDoesNotExistException {
         if (activeExperiment == null) {
             return null;
         }
         ArrayList<Instance> res = new ArrayList<Instance>();
         res.addAll(InstanceDAO.getAllByExperimentId(activeExperiment.getId()));
         return res;
     }
 
     /**
      * Returns all solver configurations.
      * @return arraylist of the solver configurations
      * @throws SQLException
      */
     public ArrayList<SolverConfiguration> getSolverConfigurations() throws SQLException {
         if (activeExperiment == null) {
             return null;
         }
         return SolverConfigurationDAO.getSolverConfigurationByExperimentId(activeExperiment.getId());
     }
 
     /**
      * Returns all result properties; adds CPUTime as result property.
      * @return arraylist of the result properties
      * @throws Exception
      */
     public ArrayList<Property> getResultProperties() throws Exception {
         ArrayList<Property> res = new ArrayList<Property>();
         res.add(ExperimentController.PROP_CPUTIME);
         res.addAll(PropertyDAO.getAllResultProperties());
         return res;
     }
 
     /**
      * Returns all instance properties.
      * @return arraylist of the instance properties
      * @throws Exception
      */
     public ArrayList<Property> getInstanceProperties() throws Exception {
         ArrayList<Property> res = new ArrayList<Property>();
         res.addAll(PropertyDAO.getAllInstanceProperties());
         return res;
     }
 }
