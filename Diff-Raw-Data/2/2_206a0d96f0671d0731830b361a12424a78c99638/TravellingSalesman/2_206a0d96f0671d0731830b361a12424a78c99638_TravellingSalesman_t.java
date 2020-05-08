 package evolution.tsp;
 
 import evolution.*;
 import evolution.individuals.IntegerIndividual;
 import evolution.operators.SwappingMutationOperator;
 import evolution.selectors.TournamentSelector;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Properties;
 import java.util.Vector;
 
 public class TravellingSalesman {
 
     static int maxGen;
     static int popSize;
     static String logFilePrefix;
     static int repeats;
     static Vector<Coordinates> coords;
     static String bestPrefix;
     static double eliteSize;
     static double xoverProb;
     static double mutProb;
     static double mutProbPerBit;
     static String enableDetailsLog;
     static String outputDirectory;
     static String objectiveFilePrefix;
     static String objectiveStatsFile;
     static String fitnessFilePrefix;
     static String fitnessStatsFile;
     static String detailsLogPrefix;
 
     public static void main(String[] args) {
 
         Properties prop = new Properties();
         try {
             InputStream propIn = new FileInputStream("properties/ga-tsp.properties");
             prop.load(propIn);
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         maxGen = Integer.parseInt(prop.getProperty("ea.maxGenerations", "20"));
         popSize = Integer.parseInt(prop.getProperty("ea.popSize", "30"));
         xoverProb = Double.parseDouble(prop.getProperty("ea.xoverProb", "0.8"));
         mutProb = Double.parseDouble(prop.getProperty("ea.mutProb", "0.05"));
         mutProbPerBit = Double.parseDouble(prop.getProperty("ea.mutProbPerBit", "0.04"));
         eliteSize = Double.parseDouble(prop.getProperty("ea.eliteSize", "0.1"));
 
         String inputFile = prop.getProperty("prob.inputFile", "resources/tsp_evropa.in");
 
         repeats = Integer.parseInt(prop.getProperty("xset.repeats", "10"));
         enableDetailsLog = prop.getProperty("xlog.detailsLog", "enabled");
 
         if (!enableDetailsLog.equals("enabled")) {
             DetailsLogger.disableLog();
         }
 
         outputDirectory = prop.getProperty("xlog.outputDirectory", "tsp");
         logFilePrefix = prop.getProperty("xlog.filePrefix", "tsp");
         String path = outputDirectory + System.getProperty("file.separator") + logFilePrefix;
         objectiveFilePrefix = path + ".objective";
         objectiveStatsFile = path + ".objective_stats";
         bestPrefix = path + ".best";
         fitnessFilePrefix = path + ".fitness";
         fitnessStatsFile = path + ".fitness_stats";
         detailsLogPrefix = path + ".details";
 
         File output = new File(outputDirectory);
         output.mkdirs();
 
         coords = new Vector<Coordinates>();
         try {
             BufferedReader in = new BufferedReader(new FileReader(inputFile));
             String line;
             while ((line = in.readLine()) != null) {
                 String[] c = line.split(" ");
                 coords.add(new Coordinates(Double.parseDouble(c[0]), Double.parseDouble(c[1])));
             }
         } catch (IOException e) {
             e.printStackTrace();
         } catch (NumberFormatException e) {
             e.printStackTrace();
         }
 
 
         for (int i = 0; i < repeats; i++) {
             run(i);
         }
 
         StatsLogger.processResults(fitnessFilePrefix, fitnessStatsFile, repeats, maxGen, popSize);
         StatsLogger.processResults(objectiveFilePrefix, objectiveStatsFile, repeats, maxGen, popSize);
 
     }
 
     static void run(int number) {
 
         RandomNumberGenerator.getInstance().reseed(number);
 
         try {
 
             IntegerIndividual sampleIndividual = new IntegerIndividual(coords.size(), 0, coords.size());
 
             Population pop = new Population();
             pop.setSampleIndividual(sampleIndividual);
             pop.setPopulationSize(popSize);
 
             EvolutionaryAlgorithm ea = new EvolutionaryAlgorithm();
             ea.setFitnessFunction(new TSPFitness(coords));
             ea.addOperator(new SwappingMutationOperator(mutProb, mutProbPerBit));
             ea.addEnvironmentalSelector(new TournamentSelector());
             ea.setElite(0.05);
 
             pop.createRandomInitialPopulation();
 
             //ensure all the individuals represent permutation
             for (int i = 0; i < pop.getPopulationSize(); i++) {
 
                 ArrayList rand = new ArrayList();
 
                 for (int j = 0; j < coords.size(); j++) {
                     rand.add(j);
                 }
 
                Collections.shuffle(rand, RandomNumberGenerator.getInstance().getRandom());
                 IntegerIndividual tmp = (IntegerIndividual) pop.get(i);
                 for (int j = 0; j < tmp.length(); j++) {
                     tmp.set(j, rand.get(j));
                 }
 
             }
 
             OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(fitnessFilePrefix + "." + number));
             OutputStreamWriter progOut = new OutputStreamWriter(new FileOutputStream(objectiveFilePrefix + "." + number));
 
             for (int i = 0; i < maxGen; i++) {
                 ea.evolve(pop);
 
                 IntegerIndividual ch = (IntegerIndividual) pop.getSortedIndividuals().get(0);
 
                 //check whether each city is used only once
                 boolean[] used = new boolean[ch.length()];
                 for (int g : ch.toIntArray()) {
                     if (used[g]) {
                         throw new RuntimeException("The city with id " + g + " found multiple times");
                     }
                     used[g] = true;
                 }
 
                 Double diff = (Double) pop.getSortedIndividuals().get(0).getObjectiveValue();
                 System.out.println("Generation " + i + ": " + diff);
 
                 StatsLogger.logFitness(pop, out);
                 StatsLogger.logObjective(pop, progOut);
 
             }
 
             OutputStreamWriter bestOut = new OutputStreamWriter(new FileOutputStream(bestPrefix + "." + number + ".kml"));
 
             BufferedReader rin = new BufferedReader(new FileReader("resources/kmlstart"));
 
             String line = "";
 
             while ((line = rin.readLine()) != null) {
                 bestOut.write(line + System.getProperty("line.separator"));
             }
 
             rin.close();
 
             IntegerIndividual bestInd = (IntegerIndividual) pop.getSortedIndividuals().get(0);
 
             for (int i = 0; i < bestInd.length(); i++) {
                 bestOut.write(" " + coords.get((Integer) bestInd.get(i)).toString() + System.getProperty("line.separator"));
             }
 
             bestOut.write(" " + coords.get((Integer) bestInd.get(0)).toString() + System.getProperty("line.separator"));
 
             rin = new BufferedReader(new FileReader("resources/kmlend"));
 
             line = "";
 
             while ((line = rin.readLine()) != null) {
                 bestOut.write(line);
             }
 
             rin.close();
             out.close();
             progOut.close();
             bestOut.close();
 
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 }
