 package edacc.experiment;
 
 import edacc.EDACCApp;
 import edacc.EDACCExperimentMode;
 import edacc.EDACCSolverConfigEntry;
 import edacc.EDACCSolverConfigPanel;
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
 import edacc.model.Tasks;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Date;
 import java.sql.SQLException;
 import java.util.Hashtable;
 import java.util.LinkedList;
 import java.util.Vector;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipOutputStream;
 import javax.swing.JFileChooser;
 
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
     private Vector<Instance> instances;
     private Vector<Solver> solvers;
     private Vector<InstanceClass> instanceClasses;
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
 
         instances = new Vector<Instance>();
 
         Vector<InstanceClass> vic = new Vector<InstanceClass>();
         vic.addAll(InstanceClassDAO.getAll());
         instanceClasses = vic;
         main.instanceClassModel.addClasses(instanceClasses);
 
     }
 
     /**
      * Loads an experiment, the solvers and the solver configurations.
      * @param id
      * @throws SQLException
      */
     public void loadExperiment(int id) throws SQLException {
         if (activeExperiment != null) {
             // TODO! messagedlg,..
             solverConfigPanel.removeAll();
         }
         activeExperiment = ExperimentDAO.getById(id);
         Vector<Solver> vs = new Vector<Solver>();
         vs.addAll(SolverDAO.getAll());
         solvers = vs;
         main.solTableModel.setSolvers(solvers);
 
         Vector<SolverConfiguration> vss = SolverConfigurationDAO.getSolverConfigurationByExperimentId(id);
         main.solverConfigPanel.beginUpdate();
         for (int i = 0; i < vss.size(); i++) {
             main.solverConfigPanel.addSolverConfiguration(vss.get(i));
             for (int k = 0; k < main.solTableModel.getRowCount(); k++) {
                 if (((Solver) main.solTableModel.getValueAt(k, 5)).getId() == vss.get(i).getSolver_id()) {
                     main.solTableModel.setValueAt(true, k, 4);
                 }
             }
         }
         main.solverConfigPanel.endUpdate();
         main.insTableModel.setExperimentHasInstances(ExperimentHasInstanceDAO.getExperimentHasInstanceByExperimentId(activeExperiment.getId()));
 
         if (instances.size() > 0) {
             main.sorter.setRowFilter(main.rowFilter);
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
      */
     public void createExperiment(String name, String description) throws SQLException {
         java.util.Date d = new java.util.Date();
         ExperimentDAO.createExperiment(name, new Date(d.getTime()), description);
         initialize();
     }
 
     /**
      * Saves all solver configurations with parameter instances in the solver
      * config panel.
      * @throws SQLException
      */
     public void saveSolverConfigurations() throws SQLException {
         for (int i = 0; i < solverConfigPanel.getComponentCount(); i++) {
             EDACCSolverConfigEntry entry = (EDACCSolverConfigEntry) solverConfigPanel.getComponent(i);
             int seed_group = 0;
             try {
                 seed_group = Integer.valueOf(entry.getSeedGroup().getText());
             } catch (NumberFormatException e) {
                 seed_group = 0;
                 entry.getSeedGroup().setText("0");
                 javax.swing.JOptionPane.showMessageDialog(null, "Seed groups have to be integers, defaulted to 0", "Expected integer for seed groups", javax.swing.JOptionPane.ERROR_MESSAGE);
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
     }
 
     /**
      * saves the instances selection of the currently loaded experiment
      * @throws SQLException
      */
     public void saveExperimentHasInstances() throws SQLException {
         for (int i = 0; i < main.insTableModel.getRowCount(); i++) {
             if ((Boolean) main.insTableModel.getValueAt(i, 5)) {
                 if ((ExperimentHasInstance) main.insTableModel.getValueAt(i, 6) == null) {
                     main.insTableModel.setExperimentHasInstance(ExperimentHasInstanceDAO.createExperimentHasInstance(activeExperiment.getId(), ((Instance) main.insTableModel.getValueAt(i, 7)).getId()), i);
                 }
             } else {
                 ExperimentHasInstance ei = (ExperimentHasInstance) main.insTableModel.getValueAt(i, 6);
                 if (ei != null) {
                     ExperimentHasInstanceDAO.removeExperimentHasInstance(ei);
                     main.insTableModel.setExperimentHasInstance(null, i);
                 }
             }
         }
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
     public int generateJobs(int numRuns, int timeout, boolean generateSeeds, int maxSeed, boolean linkSeeds, Tasks task) throws SQLException {
         activeExperiment.setAutoGeneratedSeeds(generateSeeds);
         activeExperiment.setNumRuns(numRuns);
         activeExperiment.setTimeOut(timeout);
         ExperimentDAO.setModified(activeExperiment);
         ExperimentDAO.save(activeExperiment);
         // get instances of this experiment
         LinkedList<Instance> listInstances = InstanceDAO.getAllByExperimentId(activeExperiment.getId());
 
         // get solver configurations of this experiment
         Vector<SolverConfiguration> vsc = SolverConfigurationDAO.getSolverConfigurationByExperimentId(activeExperiment.getId());
 
         int experiments_added = 0;
         Hashtable<SeedGroup, Integer> linked_seeds = new Hashtable<SeedGroup, Integer>();
         Vector<ExperimentResult> experiment_results = new Vector<ExperimentResult>();
 
 
 
         if (generateSeeds && linkSeeds) {
             // first pass over already existing jobs to accumulate existing linked seeds
             for (Instance i : listInstances) {
                 for (SolverConfiguration c : vsc) {
                     for (int run = 0; run < numRuns; ++run) {
                         task.setStatus("Preparing job generation");
                         if (ExperimentResultDAO.jobExists(run, c.getId(), i.getId(), activeExperiment.getId())) {
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
 
 
         int elements = listInstances.size() * vsc.size() * numRuns;
         int done = 1;
         // cartesian product
         for (Instance i : listInstances) {
             for (SolverConfiguration c : vsc) {
                 for (int run = 0; run < numRuns; ++run) {
                     task.setTaskProgress((float) done / (float) elements);
                     task.setStatus("Adding Job " + done + " of " + elements);
 
                     // check if job already exists
                     if (ExperimentResultDAO.jobExists(run, c.getId(), i.getId(), activeExperiment.getId()) == false) {
                         if (generateSeeds && linkSeeds) {
                             Integer seed = linked_seeds.get(new SeedGroup(c.getSeed_group(), i.getId(), run));
                             if (seed != null) {
                                 experiment_results.add(ExperimentResultDAO.createExperimentResult(run, -1, seed.intValue(), "", 0, -1, c.getId(), activeExperiment.getId(), i.getId()));
                             } else {
                                 Integer new_seed = new Integer(generateSeed(maxSeed));
                                 linked_seeds.put(new SeedGroup(c.getSeed_group(), i.getId(), run), new_seed);
                                 experiment_results.add(ExperimentResultDAO.createExperimentResult(run, -1, new_seed.intValue(), "", 0, -1, c.getId(), activeExperiment.getId(), i.getId()));
                             }
                         } else if (generateSeeds && !linkSeeds) {
                             experiment_results.add(ExperimentResultDAO.createExperimentResult(run, -1, generateSeed(maxSeed), "", 0, -1, c.getId(), activeExperiment.getId(), i.getId()));
                         } else {
                             experiment_results.add(ExperimentResultDAO.createExperimentResult(run, -1, 0, "", 0, -1, c.getId(), activeExperiment.getId(), i.getId()));
                         }
                         experiments_added++;
                     }
                     done++;
                 }
 
             }
         }
         ExperimentResultDAO.batchSave(experiment_results);
 
         return experiments_added;
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
         return instances.size();
     }
 
     public void loadJobs() {
         try {
             main.jobsTableModel.jobs = ExperimentResultDAO.getAllByExperimentId(activeExperiment.getId());
             main.jobsTableModel.fireTableDataChanged();
         } catch (Exception e) {
             // TODO: shouldn't happen but show message if it does
         }
 
     }
 
     private JFileChooser packageFileChooser;
     /**
      * Generates a ZIP archive with the necessary files for the grid.
      */
     public void generatePackage() throws FileNotFoundException, IOException, NoConnectionToDBException, SQLException, ClientBinaryNotFoundException {
         final String fileSep = System.getProperty("file.separator");
         if (packageFileChooser == null) {
             packageFileChooser = new JFileChooser();
             packageFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
         }
         if (packageFileChooser.showDialog(main, "Select Package Location") != JFileChooser.APPROVE_OPTION)
             return;
         File zipFile = new File(packageFileChooser.getSelectedFile().getAbsolutePath() + fileSep + activeExperiment.getDate().toString() + " - " + activeExperiment.getName() + ".zip");
         if (zipFile.exists()) {
             zipFile.delete();
         }
         ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile));
         ZipEntry entry;
 
         // add solvers to zip file
         Vector<Solver> solvers = ExperimentDAO.getSolversInExperiment(activeExperiment);
         for (Solver s : solvers) {
             File bin = SolverDAO.getBinaryFileOfSolver(s);
             entry = new ZipEntry("solvers" + fileSep + s.getBinaryName());
             addFileToZIP(bin, entry, zos);
         }
 
         // add instances to zip file
         LinkedList<Instance> instances = InstanceDAO.getAllByExperimentId(activeExperiment.getId());
         for (Instance i : instances) {
             File f = InstanceDAO.getBinaryFileOfInstance(i);
             entry = new ZipEntry("instances" + fileSep + i.getId() + "_" + i.getName());
             addFileToZIP(f, entry, zos);
         }
 
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
         entry = new ZipEntry("result" + fileSep + "~");
         zos.putNextEntry(entry);
 
         zos.close();
 
         // delete tmp directory
         deleteDirectory(new File("tmp"));
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
 
         int data;
 
         while ((data = in.read()) > -1) {
             zos.write(data);
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
 
     void addInstancesToVector(Vector<Instance> instances) {
         this.instances.addAll(instances);
     }
 
     void removeAllInstancesFromVector() {
         this.instances.clear();
     }
 
     public void selectAllInstanceClasses() {
         for (int i = 0; i
                 < main.instanceClassModel.getRowCount(); i++) {
             main.instanceClassModel.setInstanceClassSelected(i);
         }
     }
 
     public void deselectAllInstanceClasses() {
         for (int i = 0; i
                 < main.instanceClassModel.getRowCount(); i++) {
             main.instanceClassModel.setInstanceClassDeselected(i);
         }
     }
 
     private void addConfigurationFile(ZipOutputStream zos, Experiment activeExperiment, GridQueue activeQueue) throws IOException {
         // generate content of config file
         String sConf = "host = $host\n"
                 + "username = $user\n"
                 + "password = $pwd\n"
                 + "database = $db\n"
                 + "experiment = $exp\n"
                + "gridqueue = $q\n";
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
         String sRun = "#!/bin/bash\n"
                 + "for (( i = 1; i < " + q.getNumNodes() + "; i++ ))\n"
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
         InputStream in = EDACCApp.class.getClassLoader().getResourceAsStream("client");
         if (in == null)
             throw new ClientBinaryNotFoundException();
         ZipEntry entry = new ZipEntry("client");
         zos.putNextEntry(entry);
 
         int data;
 
         while ((data = in.read()) > -1) {
             zos.write(data);
         }
         zos.closeEntry();
         in.close();
     }
 }
