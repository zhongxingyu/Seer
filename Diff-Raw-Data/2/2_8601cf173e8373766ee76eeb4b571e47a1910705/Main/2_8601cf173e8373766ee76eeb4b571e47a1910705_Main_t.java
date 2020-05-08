 package evolution.sga;
 
 import evolution.*;
 import evolution.individuals.BooleanIndividual;
 import evolution.individuals.Individual;
 import evolution.operators.BitFlipMutation;
 import evolution.operators.OnePtXOver;
 import evolution.selectors.RouletteWheelSelector;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Properties;
 
 /**
  * @author Martin Pilat
  */
 public class Main {
 
     static int maxGen;
     static int popSize;
     static int dimension;
     static double xoverProb;
     static double mutProb;
     static double mutProbPerBit;
     static String fitnessFilePrefix;
     static String fitnessStatsFile;
     static String objectiveFilePrefix;
     static String objectiveStatsFile;
     static String bestPrefix;
     static String outputDirectory;
     static String logFilePrefix;
     static String detailsLogPrefix;
     static int repeats;
     static Properties prop;
 
     public static void main(String[] args) {
 
         prop = new Properties();
         try {
             InputStream propIn = new FileInputStream("properties/ga-sga.properties");
             prop.load(propIn);
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         //Read to properties from the file.
 
         maxGen = Integer.parseInt(prop.getProperty("ea.maxGenerations", "20"));
         popSize = Integer.parseInt(prop.getProperty("ea.popSize", "30"));
         xoverProb = Double.parseDouble(prop.getProperty("ea.xoverProb", "0.8"));
         mutProb = Double.parseDouble(prop.getProperty("ea.mutProb", "0.05"));
         mutProbPerBit = Double.parseDouble(prop.getProperty("ea.mutProbPerBit", "0.04"));
 
         dimension = Integer.parseInt(prop.getProperty("prob.dimension", "25"));
 
         repeats = Integer.parseInt(prop.getProperty("xset.repeats", "10"));
 
         outputDirectory = prop.getProperty("xlog.outputDirectory", "sga");
         logFilePrefix = prop.getProperty("xlog.filePrefix", "log");
         String path = outputDirectory + System.getProperty("file.separator") + logFilePrefix;
         objectiveFilePrefix = path + ".objective";
         objectiveStatsFile = path + ".objective_stats";
         bestPrefix = path + ".best";
         fitnessFilePrefix = path + ".fitness";
         fitnessStatsFile = path + ".fitness_stats";
         detailsLogPrefix = path + ".details";
 
         File outDir = new File(outputDirectory);
         if (!outDir.exists()) {
             outDir.mkdirs();
         }
 
         //Run the algorithm
 
         for (int i = 0; i < repeats; i++) {
             run(i);
         }
 
         StatsLogger.processResults(fitnessFilePrefix, fitnessStatsFile, repeats, maxGen, popSize);
         StatsLogger.processResults(objectiveFilePrefix, objectiveStatsFile, repeats, maxGen, popSize);
 
     }
 
     /**
      * This is the main method, which executes one run of the evolutionary algorithm.
      *
      * @param number The number of this run, also used as seed for the random number generator.
      */
 
 
     public static void run(int number) {
 
         //Initialize logging of the run
 
         DetailsLogger.startNewLog(detailsLogPrefix + "." + number + ".xml");
         DetailsLogger.logParams(prop);
 
         //Set the rng seed
 
         RandomNumberGenerator.getInstance().reseed(number);
 
         //Create new population
         Population pop = new Population();
         pop.setPopulationSize(popSize);
         pop.setSampleIndividual(new BooleanIndividual(dimension));
         pop.createRandomInitialPopulation();
 
 
         //Set the options for the evolutionary algorithm
         EvolutionaryAlgorithm ea = new EvolutionaryAlgorithm();
         ea.setFitnessFunction(new ExampleFitnessFunction());
         ea.addMatingSelector(new RouletteWheelSelector());
         ea.addOperator(new OnePtXOver(xoverProb));
         ea.addOperator(new BitFlipMutation(mutProb, mutProbPerBit));
 
 
        //Run the algorithm
 
         try {
             OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(fitnessFilePrefix + "." + number));
             OutputStreamWriter progOut = new OutputStreamWriter(new FileOutputStream(objectiveFilePrefix + "." + number));
 
             for (int i = 0; i < maxGen; i++) {
 
                 //Make one generation
                 ea.evolve(pop);
                 ArrayList<Individual> sorted = pop.getSortedIndividuals();
                 //Log the best individual to console.
                 System.out.println("fitness: " + sorted.get(0).getFitnessValue() + " " + sorted.get(0));
 
                 //Add population statistics to the logs
                 StatsLogger.logFitness(pop, out);
                 StatsLogger.logObjective(pop, progOut);
             }
 
             OutputStreamWriter bestOut = new OutputStreamWriter(new FileOutputStream(bestPrefix + "." + number));
 
             Individual bestInd = pop.getSortedIndividuals().get(0);
             bestOut.write(bestInd.toString());
 
             out.close();
             progOut.close();
             bestOut.close();
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         DetailsLogger.writeLog();
 
     }
 }
