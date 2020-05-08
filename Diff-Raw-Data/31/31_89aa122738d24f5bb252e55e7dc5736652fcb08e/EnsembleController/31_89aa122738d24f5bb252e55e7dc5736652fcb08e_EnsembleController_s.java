 package com.mbutlitsky.mk;
 
 import java.io.*;
 import java.nio.charset.Charset;
 import java.nio.file.FileSystems;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.*;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ThreadLocalRandom;
 
 import static java.lang.Thread.sleep;
 
 /**
  * Date: 02.03.13
  * Time: 19:07
  */
 public class EnsembleController implements IEnsembleController {
     /**
      * how often check child ensembles & display status on console
      */
    public static int REFRESH_DELAY = 7000;
 
     /**
      * may be overriden by CLI options, if zero – max(2, CPUs/2) used
      */
     public static int NUM_THREADS = 0;
 
     private static final int DRAW_STATUS_INT = 5;
     private volatile boolean running = true;
 
     private static final String CONFIG_FILE = "mk_config.ini";
     private final NumberFormat SHORT_FORMAT = new DecimalFormat(EOptions.SHORT_FORMAT_STR);
     private final NumberFormat FULL_FORMAT = new DecimalFormat(EOptions.SCIENTIFIC_FORMAT_STR);
 
     private final List<IEnsemble> ensembles = new ArrayList<IEnsemble>();
 
     // TODO: remove this rudiment
 //    private final List<EOptions> continueOpts = new ArrayList<EOptions>();
 
     //    private Map<IEnsemble, List> energies = new HashMap();
     private final Map<IEnsemble, Boolean> states = new HashMap<IEnsemble, Boolean>();
     private final Map<Integer, Map<IEnsemble, List<Double>>> enrgValues = new HashMap<Integer,
             Map<IEnsemble, List<Double>>>();
 
 
     private final ExecutorService pool;
 
     public EnsembleController() {
 
         int cores = Runtime.getRuntime().availableProcessors();
         System.out.println("\nRunning " + System.getProperty("os.name") + " with " + cores + " " +
                 "CPUs");
 
         if (NUM_THREADS == 0) {
             NUM_THREADS = (cores > 2) ? cores / 2 : cores;
         }
 
         System.out.println("Executor pool has " + NUM_THREADS + " workers");
         pool = Executors.newFixedThreadPool(NUM_THREADS);
 
         Set<EOptions> options = readConfig(CONFIG_FILE);
 
         if (options.isEmpty()) {
             System.out.println("No valid options found in " + CONFIG_FILE);
             System.exit(2);
         }
 
 
         // setting main ensembles Lists
         for (EOptions opt : options) {
             IEnsemble ens = new EnsemblePolochka(opt);
 //            IEnsemble ens = new EnsemblePseudoPotential(opt);
             ensembles.add(ens);
             // additional config for continues
             opt.setOld(true);
 
 //            continueOpts.add(opt); HZ
 
             // fill mapping T -> ensembles list
             if (enrgValues.get(opt.getT()) == null) {
                 enrgValues.put(opt.getT(), new LinkedHashMap<IEnsemble, List<Double>>());
             }
 
             enrgValues.get(opt.getT()).put(ens, new ArrayList<Double>(6));
         }
 
         System.out.println("\n");
     }
 
     @Override
     public void stop() {
         if (!running) return;
 
         running = false;
 
         System.out.println("Somebody stopping controller...");
 
         for (IEnsemble current : ensembles) {
             current.stop();
         }
 
         System.out.println("Controler with " + ensembles.size() + " threads stopped. Thank you.");
     }
 
     public void start() throws InterruptedException {
         if (!running) return;
 
         System.out.println("Starting " + ensembles.size() + " ensembles' threads... ");
         ThreadLocalRandom rnd = ThreadLocalRandom.current();
         running = true;
 
         for (IEnsemble current : ensembles) {
             sleep(rnd.nextInt(40, 120)); // some start time distribution to avoid clutter
             // thread pool filled
             pool.execute(current);
             states.put(current, new Boolean(true));
         }
 
         System.out.println("Ensembles started and running:");
 
         int i = 0;
         drawStatus();
 
         while (running) {
             sleep(REFRESH_DELAY);
 
             // heavy status reports (saving energies & plots)
             if (i % DRAW_STATUS_INT == 0) drawStatus();
             if (i % (DRAW_STATUS_INT * 7) == 0) saveEnergies();
 
             refreshStatus();
             i++;
         }
 
         saveEnergies();
         System.out.println("Controller (" + ensembles.size() + " points) finished.");
         pool.shutdown();
         System.out.println("Thread pool shutted down.");
     }
 
     private void refreshStatus() {
         // main ensembles refresh loop
         for (IEnsemble current : ensembles) {
 //            refreshEnergy(current);
             refreshStates(current);
         }
         stopIfFinished();
     }
 
     private void drawStatus() {
         for (IEnsemble current : ensembles) {
             refreshEnergy(current);
         }
 
         System.out.println("\n\n"); // clear screen
         System.out.println("Polochka: -" + EnsemblePolochka.EPSILON
                 + ", delta: " + Ensemble.DELTA_FACTOR + ", refresh: "
                 + EnsembleController.REFRESH_DELAY / 1000 + ", workers: "
                 + EnsembleController.NUM_THREADS);
         System.out.println("---------------------------------------------------------");
 
         // main ensembles refresh loop
         for (IEnsemble current : ensembles) {
             drawEnergy(current);
         }
     }
 
     /**
      * Save energy (for e ot gamma plot) for each temperature used
      */
     private void saveEnergies() {
         try {
             for (Integer enrg : enrgValues.keySet()) {
                 BufferedWriter writer = Files.newBufferedWriter(getPath(enrg + "K_energy.txt"),
                         Charset.defaultCharset());
 
                 Set<IEnsemble> iEnsembles = enrgValues.get(enrg).keySet();
                 writer.write("#" + enrg + "  " + iEnsembles);
                 writer.newLine();
 
                 for (IEnsemble ens : iEnsembles) {
                     final List enrgs = getEnergies(ens);
 
                     writer.write(FULL_FORMAT.format(enrgs.get(enrgs.size() - 1)));
                     writer.newLine();
                 }
                 writer.close();
             }
         } catch (IOException e) {
             e.printStackTrace();
             System.out.println("ERROR: can't save energy lists!");
         }
     }
 
 
     private void stopIfFinished() {
         for (Boolean state : states.values()) {
             if (state.booleanValue()) return;
         }
         running = false;
         System.out.println("All threads seems finished work.");
     }
 
     private void refreshStates(IEnsemble current) {
         states.put(current, new Boolean(!current.isFinished())); // true == running thread
     }
 
     private void drawEnergy(IEnsemble current) {
         boolean currentRunning = states.get(current).booleanValue();
         String energyes = getSimpleEnergiesString(current);
         System.out.println(current.getFolder() + "\t#" + current.getCurrStep() +
                 " (" + 100 * (current.getCurrStep() + 1) / current.getNumSteps() + "%)" +
                 "\t[" + energyes + "]\t" + (currentRunning ? "" : " finished"));
     }
 
     private void refreshEnergy(IEnsemble current) {
         List<Double> enrgy = getEnergies(current); // we got list of energies for given ensemble
         enrgy.add(new Double(current.getEnergy() / current.getNumPart()));
         if (enrgy.size() > 5) enrgy.remove(0);
     }
 
     private String getSimpleEnergiesString(IEnsemble current) {
         List enrgs = getEnergies(current);
         StringBuilder out = new StringBuilder();
         for (Object o : enrgs) {
             out.append(SHORT_FORMAT.format(o));
             out.append(",  ");
         }
         return out.toString();
     }
 
     private final List<Double> getEnergies(IEnsemble current) {
         return enrgValues.get(current.getT()).get(current);
     }
 
 //    public void saveContinueOptions() throws IOException {
 //        Files.write(getPath(CONFIG_FILE + "_"), continueOpts, Charset.defaultCharset());
 //    }
 
     public static final Path getPath(String path) {
         return FileSystems.getDefault().getPath(".", path);
     }
 
     // =------------------ PRIVATE section -----------------
     private Set<EOptions> readConfig(String configFile) {
         Set<EOptions> opts = new LinkedHashSet<EOptions>();
 
         try {
             BufferedReader bufRead = new BufferedReader(new FileReader(configFile));
 
             String line;    // String that holds current file line
             // Read through file one line at time.
             do {
                 line = bufRead.readLine();
                 if (line != null && !line.trim().startsWith("#") && !line.trim().equals(""))
                     opts.add(EOptions.fromLine(line.trim()));
 
             } while (line != null);
 
             bufRead.close();
 
         } catch (FileNotFoundException e) {
             System.out.println("File " + configFile + " not found");
             System.exit(1);
         } catch (IOException e) {
             System.out.println("Config file broken, i'm quit.");
             System.exit(1);
         }
         opts.remove(null); // just in case of blank EnsembleOptions were added
 
         return opts;
     }
 
 }
