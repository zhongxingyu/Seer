 package com.mbutlitsky.mk;
 
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.StandardOpenOption;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import static com.mbutlitsky.mk.EnsembleController.getPath;
 import static java.lang.Math.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: aristofun
  * Date: 03.03.13
  * Time: 13:04
  */
 public abstract class Ensemble implements IEnsemble {
    private static final int CALC_ENERGY_INT = 117; // make less for big num part
 //    private static final int CALC_ENERGY_INT = 419; // make less for big num part
    private static final int SAVE_CONFIG_INT = 239; // make less for big num part
 //    private static final int SAVE_CONFIG_INT = 1117; // make less for big num part
 
    private static final int SAVE_CORR_INT = 1979;
    private static final int CALC_CORR_INT = 489;
     public static double DELTA_FACTOR = 1.2;
     /**
      * overrides specific particle number in ini file if set in CLI options
      */
     public static int DEFAULT_NUM_PARTICLES = 0;
 
     private final NumberFormat FORMAT = new DecimalFormat(EOptions.SCIENTIFIC_FORMAT_STR);
     private final NumberFormat SHORT_FORMAT = new DecimalFormat(EOptions.SHORT_FORMAT_STR);
 
     // ----------  controllers  --------------
     private final EOptions opt;
     private final String myFolder;
     private final MersenneTwisterFast rnd;
     private Path myConfigPath;
     private Path myCorrPath;
     private volatile boolean finished = false;
     private final boolean saveLongTail;
     private BufferedWriter longTailWriter;
 
 
     // ------------ Monte Karlo --------------
     private final int numPart;
     private final int numSteps;
     private int currStep;
     private int which;
     private double xTrial;
     private double yTrial;
     private double zTrial;
 
     // ------------ mathematics --------------
     protected final int T;
     private final double boxSize;
     private final double halfBox;
     private final double maxDelta;
     private final double avgDistance;
 
     private final double corrNormirovka;
     private final double corrDr;
     private final double[][] corrArray = new double[3][];
     private int corrAverager = 0;
 
     private final double[] Xs;
     private final double[] Ys;
     private final double[] Zs;
     private double energy = 0;
     // averaging energies values
     private double[] energies = new double[5];
     private double avgEnergy = 0;
 
 
     public Ensemble(EOptions options) {
         rnd = new MersenneTwisterFast();
         opt = options;
         boxSize = pow(opt.getNumParticles() / (2 * opt.getDensity()), 0.3333333333333333) / BOHR;
         halfBox = boxSize / 2.0;
         avgDistance = pow(opt.getDensity(), -0.333333333333333333) / BOHR;
 
         // ignore specific delta factor if set to zero
         maxDelta = (opt.getMaxDelta() == 0.0) ? DELTA_FACTOR * avgDistance : opt.getMaxDelta();
 
         numPart = (DEFAULT_NUM_PARTICLES == 0) ? opt.getNumParticles() : DEFAULT_NUM_PARTICLES;
         numSteps = opt.getNumSteps();
         T = opt.getT();
         myFolder = opt.getFolder();
 
 //        double maxDistance = sqrt(3.0 * boxSize * boxSize) / 1.99;
 
         corrNormirovka = 4. * (boxSize * boxSize * boxSize) / (numPart * numPart);
         corrDr = sqrt(3.0 * boxSize * boxSize) / 1.99999999999 / (CORR_LENGTH);
 
         corrArray[0] = new double[CORR_LENGTH];
         corrArray[1] = new double[CORR_LENGTH];
         corrArray[2] = new double[CORR_LENGTH];
 
         // initiating particles configuration
         Xs = new double[numPart];
         Ys = new double[numPart];
         Zs = new double[numPart];
 
         // OPTIONS first bit == save longtail
         saveLongTail = ((options.getStrategy() & 1) == 1);
     }
 
     /**
      * Must be run by children after super constructor. Default strategy – set up initial state from file
      * or random if failed to read config from disk.
      */
     protected void initialize() {
         loadFromStateFile();
         initEnergy();
 
         applyAdditionalStrategies();
     }
 
     /**
      * sets average energy from file or resets it to zero
      */
     private void initEnergy() {
         resetEnergy();
         if (avgEnergy == 0) {
             avgEnergy = energy;
         }
 
         Arrays.fill(energies, avgEnergy);
     }
 
 
     private void applyAdditionalStrategies() {
         if (saveLongTail) {
             try {
                 longTailWriter = Files.newBufferedWriter(getPath(myFolder + "/" + LONGTAIL_FILE),
                         Charset.defaultCharset(), StandardOpenOption.CREATE,
                         opt.isOld() ? StandardOpenOption.APPEND : StandardOpenOption.TRUNCATE_EXISTING,
                         StandardOpenOption.WRITE);
             } catch (Exception e) {
                 System.out.println("ERROR: Can't create " + LONGTAIL_FILE + " for " + myFolder);
             }
         }
     }
 
     /**
      * reads config or generates new random if any errors ocurred
      */
     private void loadFromStateFile() {
         myConfigPath = getPath(myFolder + "/" + STATE_FILE);
         myCorrPath = getPath(myFolder + "/" + CORR_FILE);
 
         try {
             Files.createDirectories(getPath(myFolder));
         } catch (IOException e) {
             e.printStackTrace();
             System.out.println("ERROR: can't find " + myFolder + " directory, " +
                     "worker is dead");
             finished = true;
             return;
         }
 
         // reading old state
         if (opt.isOld()) {
             if (Files.exists(myConfigPath)) {
                 try {
                     loadArrays(Files.readAllLines(myConfigPath, Charset.defaultCharset()));
                 } catch (Exception e) {
                     System.out.println("WARNING: failed to read config for " + myFolder);
                     System.out.println(e.getLocalizedMessage());
                     opt.setOld(false);
                 }
             } else {
                 opt.setOld(false);
                 System.out.print("! from scratch: " + myFolder + "\t ");
             }
         }
 
         if (!opt.isOld()) initRandomState(); // initial distribution, reset counters
     }
 
     /**
      * fill random arrays of particles, resetting all counters
      */
     private void initRandomState() {
         currStep = 0;
 
         // random configuration.
         for (int j = 0; j < numPart; j++) {
             /* spreading the particles */
             Xs[j] = myRandom(boxSize);
             Ys[j] = myRandom(boxSize);
             Zs[j] = myRandom(boxSize);
         }
     }
 
     /**
      * calculates full system energy from scratch, based on current particles configuration
      */
     private final void averageEnergy() {
         energies[0] = energies[1];
         energies[1] = energies[2];
         energies[2] = energies[3];
         energies[3] = energies[4];
         energies[4] = energy;
 
         avgEnergy = (energies[0] + energies[1] + energies[2] + energies[3] + energies[4]) / 5;
     }
 
     private final void resetEnergy() {
         double newEn = 0;
 //        energy = 0;
         for (int i = 0; i < numPart; i++) {
             for (int j = i + 1; j < numPart; j++) {
                 newEn = newEn + getPotential(i, j);
             }
         }
 
 //        if (abs(newEn - energy) > abs(energy * 0.0000001))
 //            System.out.println("ACHTUNG! new - old == " + (newEn - energy) + ", step: " + currStep
 //                    + ", " + myFolder + ", energy: " + energy + ", newEn: " + newEn);
         energy = newEn;
     }
 
     /**
      * trialX/Y/Z must belong to j particle
      *
      * @return potential between two particles assuming j[trialX, trialY, trialZ] particle
      */
     private final double getPotential(int i, int j, double trialX, double trialY, double trialZ) {
         final double r = sqrt(dSquared(trialX - Xs[i], trialY - Ys[i], trialZ - Zs[i]));
 
         if (i != j) {
             if (j < (numPart / 2))   // First _num/2 are IONS
             {
                 if (i < (numPart / 2)) // ION-ION
                     return getPotential(r, false);
                 else              // ION - electron
                     return getPotential(r, true);
             } else                   // Last _num/2 are Electrons
             {
                 if (i < (numPart / 2)) // Electron - ION
                     return getPotential(r, true);
                 else              // Electron - Electron
                     return getPotential(r, false);
             }
         }
         return 0;
     }
 
     /**
      * returns current energy between to particles defined by i,j indexes
      * if i == j returns 0
      */
     private final double getPotential(int i, int j) {
         return getPotential(i, j, Xs[j], Ys[j], Zs[j]);
     }
 
     protected abstract double getPotential(double r, boolean attraction);
 
     public final double dSquared(double dx, double dy, double dz) {
         dx = dSquared(dx);
         dy = dSquared(dy);
         dz = dSquared(dz);
         return ((dx * dx) + (dy * dy) + (dz * dz));
     }
 
     private final double dSquared(double dx) {
         dx = abs(dx);
         return (dx > halfBox) ? (halfBox - dx % halfBox) : dx;
     }
 
     /**
      * random (-1.0; 1.0)
      */
     protected final double myRandom() {
         return ((double) rnd.nextInt() / Integer.MAX_VALUE);
 //        return ThreadLocalRandom.current().nextDouble(-1.,1.);
     }
 
     protected final int myRandom(int size) {
         return rnd.nextInt(size);
 //        return ThreadLocalRandom.current().nextInt(size);
     }
 
     /**
      * returns Mersenne Twister random (0; size) double value
      */
     protected final double myRandom(double size) {
         // faster on Core 2 Duo
         return size * ((double) rnd.nextInt() / Integer.MAX_VALUE + 1) / 2;
 //        return rnd.nextDouble() * size;
 //        return ThreadLocalRandom.current().nextDouble(size);
     }
 
     /**
      * throws IOException if no valid config could be read
      */
     private void loadArrays(List<String> strings) throws Exception {
         // first line: curr_step energy
         String step_en = strings.remove(0);
         // #step, average energy, ...
         currStep = Integer.parseInt(step_en.split("\\s+")[0]);
         avgEnergy = Double.parseDouble(step_en.split("\\s+")[1]);
 
         if (strings.size() != numPart) {
             throw new Exception("file size doesn't fit particles number");
         }
 
         for (int i = 0; i < strings.size(); i++) {
             String[] strs = strings.get(i).split("\\s");
             Xs[i] = Double.parseDouble(strs[0]);    //
             Ys[i] = Double.parseDouble(strs[1]);    //
             Zs[i] = Double.parseDouble(strs[2]);    //
         }
     }
 
     private final void saveCorrelation() {
         List<String> strings = new ArrayList<String>(CORR_LENGTH);
 
         if (corrAverager == 0) {
             System.out.println("WARNING: corrAverager == 0 for " + myFolder + ", " +
                     "skip saveCorrelation");
             return;
         }
 
         for (int i = 0; i < CORR_LENGTH; i++) {
             // radius in the middle of a sherical layer
             final double r = i * corrDr + 0.5 * corrDr;
 //                                              #    sherical layer volume     #
             final double norm = corrNormirovka / (4.0 * Math.PI * r * r * corrDr * corrAverager);
             // writing
             strings.add(FORMAT.format(r) + "\t"
                     + FORMAT.format(corrArray[0][i] * norm) + "\t"
                     + FORMAT.format(corrArray[1][i] * norm) + "\t"
                     + FORMAT.format(corrArray[2][i] * norm) + "\t"
             );
         }
 
         try {
             Files.write(myCorrPath, strings, Charset.defaultCharset());
         } catch (IOException e) {
             e.printStackTrace();
             System.out.println("ERROR: failed to save correlation for " + myFolder);
         }
     }
 
     public void saveState() {
         resetEnergy();
         averageEnergy();
         // create strings list from arrays
         try {
             BufferedWriter writer = Files.newBufferedWriter(myConfigPath, Charset.defaultCharset());
 
             writer.write("" + currStep + "\t"
                     + FORMAT.format(avgEnergy) + "\t"
                     + SHORT_FORMAT.format(avgEnergy / numPart) + "\t"
                     + SHORT_FORMAT.format(energy / numPart) + "\t"
                     + SHORT_FORMAT.format(opt.getGamma()));
 
             writer.newLine();
             writeStateTo(writer);
             writer.close();
         } catch (IOException e) {
             e.printStackTrace();
             System.out.println("ERROR: failed to save state for " + myFolder);
         }
     }
 
     private void writeStateTo(BufferedWriter writer) throws IOException {
         for (int i = 0; i < numPart; i++) {
             writer.write(FORMAT.format(Xs[i]) + "\t"
                     + FORMAT.format(Ys[i]) + "\t" + FORMAT.format(Zs[i]));
             writer.newLine();
         }
     }
 
     @Override
     public final int getCurrStep() {
         return currStep;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     /**
      * new blank thread is running this method until stopped or comes to the numSteps
      * It quietly stops on any exceptions with message to system out
      */
     @Override
     public void run() {
         System.out.println();
         if (currStep >= numSteps) {
             System.out.print(myFolder + " No run.\t");
             finished = true;
             return;
         }
 
         for (int i = currStep; i <= numSteps; i++) {
             if (finished) {
                 System.out.println("Exitting by stop " + myFolder + ", 'finished' found 'true'");
                 break;
             }
 
             play();
 
             if (i % CALC_CORR_INT == 0) calcCorrelation();
             if (i % SAVE_CORR_INT == 0) saveCorrelation();
             if (i % CALC_ENERGY_INT == 0) {
                 averageEnergy();
             }
 
             if (i % SAVE_CONFIG_INT == 0) {
                 saveState();
                 if (saveLongTail) saveLongTail();
             }
 
             currStep = i;
         }
 
         finished = true;
 
         saveState();
 
         saveLongTail();
         closeLongTail();
         System.out.println("Ensemble " + myFolder + " finished after " + currStep + " steps.");
     }
 
     private void closeLongTail() {
         if (saveLongTail) try {
             longTailWriter.flush();
             longTailWriter.close();
         } catch (IOException e1) {
             System.out.println("ERROR: can't close long tail writer for " + myFolder);
         }
     }
 
     private void saveLongTail() {
         try {
             if (saveLongTail) {
                 writeStateTo(longTailWriter);
                 longTailWriter.flush();
             }
         } catch (IOException e1) {
             System.out.println("ERROR: can't write " + myFolder + " long tail to writer!");
         }
     }
 
     private final void fillCorrAray(int i, int j, int corrIndex) {
         if (i != j) {
             // get the index of a radius in array
             final int idx =
                     (int) (sqrt(dSquared(Xs[i] - Xs[j], Ys[i] - Ys[j], Zs[i] - Zs[j])) / corrDr);
             // increment ION-ION array
             if (idx < CORR_LENGTH)
                 corrArray[corrIndex][idx]++;
             else
                 System.out.println("WARNING: " + myFolder + " correlation index out of bounds!");
         }
     }
 
     private final void calcCorrelation() {
         // get the simple corrArray for ION-ION
         for (int i = 0; i < (numPart / 2); i++) {
             for (int j = 0; j < (numPart / 2); j++) {
                 fillCorrAray(i, j, 0);
             }
         }
         // get the simple corrArray for ELECTRON-ELECTRON
         for (int i = (numPart / 2); i < numPart; i++) {
             for (int j = (numPart / 2); j < numPart; j++) {
                 fillCorrAray(i, j, 2);
             }
         }
 
         // get the simple corrArray for ELECTRON-ION
         for (int i = (numPart / 2); i < numPart; i++) {
             for (int j = 0; j < (numPart / 2); j++) {
                 fillCorrAray(i, j, 1);
             }
         }
         // increment the number of correlation array calculatings
         corrAverager++;
     }
 
     /**
      * performs one act of monte-karlo play randomly moves each particle and saves the state
      * with some probability
      */
     private final void play() {
         for (int j = 0; j < numPart; j++) {
             // move random particle
             final double deltaE = moveParticle();
             // transition probability checking
             // energy increased
             if (deltaE > 0) {
                 // compare the transition probability with random
                 // All energies are in kT
                 if (Math.exp(-deltaE) >= myRandom(1.0))   // accept movement
                     acceptTrial(deltaE);
 
                 // energy decreased, accept configuration
             } else {
                 acceptTrial(deltaE);
             }
         }
     }
 
     private final void acceptTrial(double deltaE) {
         Xs[which] = xTrial;
         Ys[which] = yTrial;
         Zs[which] = zTrial;
         energy = energy + deltaE;
     }
 
     /**
      * returns trial energy shift for moved particle
      */
     private final double moveParticle() {
         final double x = myRandom() * maxDelta;
         final double y = myRandom() * maxDelta;
         final double z = myRandom() * maxDelta;
 
         // setting new trial index and coordinates
         which = myRandom(numPart);
         xTrial = correctPosition(Xs[which] + x);
         yTrial = correctPosition(Ys[which] + y);
         zTrial = correctPosition(Zs[which] + z);
 
         // Calculating the energy shift
         double oldE = 0, newE = 0;
 
         // firstly, old Energy
         for (int i = 0; i < numPart; i++) {
             oldE = oldE + getPotential(i, which);
         }
 
         // then, new Energy
         for (int i = 0; i < numPart; i++) {
             newE = newE + getPotential(i, which, xTrial, yTrial, zTrial);
         }
         return newE - oldE;
     }
 
     /**
      * set right position inside the Box
      */
     private final double correctPosition(double coord) {
         return ((boxSize + coord % boxSize) % boxSize);
         // optimize it
         // int mod (int a, int b)
 //        {
 //            int ret = a % b;
 //            if(ret < 0)
 //                ret+=b;
 //            return ret;
 //        }
 // OLD code:   (coord - boxSize * (long) (coord * 2 / boxSize - 1));
     }
 
     @Override
     /**
      * Used only by external watchers! gives back averaged energy (by last 5 configurations).
      */
     public double getEnergy() {
         return avgEnergy;
     }
 
     @Override
     public double getPressure() {
         return 0;
     }
 
     @Override
     public final boolean isFinished() {
         return finished;
     }
 
 
     @Override
     public int getNumPart() {
         return numPart;
     }
 
     @Override
     public int getT() {
         return T;
     }
 
     @Override
     public String getFolder() {
         return myFolder;
     }
 
     @Override
     public int getNumSteps() {
         return numSteps;
     }
 
     @Override
     public final void stop() {
         finished = true;
     }
 
     public String toString() {
         return "ensmbl:" + myFolder;
     }
 }
