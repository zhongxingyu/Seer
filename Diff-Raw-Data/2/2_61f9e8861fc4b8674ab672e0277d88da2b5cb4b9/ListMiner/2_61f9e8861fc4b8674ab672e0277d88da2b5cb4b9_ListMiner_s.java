 package com.parthtejani.listminer;
 
 import java.io.*;
 import java.util.*;
 import java.util.Map.Entry;
 
 import com.parthtejani.listminer.Graph.Result;
 
 /**
  * An implementation of an algorithm to mine periodic subgraphs in a dynamic network.
  */
 public class ListMiner {
 
     private static final int DEFAULT_MIN_SUPPORT = 3;
     private static final int DEFAULT_MIN_PERIOD = 1;
     private static final int DEFAULT_MAX_PERIOD = 10;
     private static final int DEFAULT_SMOOTH_STEP = 1;
     private static final boolean DEFAULT_PRINT_GRAPHS = false;
     private static final boolean DEFAULT_SUBSUMES = true;
 
     private final String inputFilePath;
     private final String outputFilePath;
     private final int minSupport;
     private final int minPeriod;
     private final int maxPeriod;
     private final int smoothStep;
     private final boolean printGraphs;
     private final boolean performSubsumation;
 
     private final List<Graph> graphs = new ArrayList<Graph>();
     private final List<ArrayList<Integer>> periodFactors = new ArrayList<ArrayList<Integer>>();
     private final TriangularTable<ArrayList<Descriptor>> descriptorTable = new TriangularTable<ArrayList<Descriptor>>();
     private final TriangularTable<ArrayList<Descriptor>> flushedDescriptorTable = new TriangularTable<ArrayList<Descriptor>>();
 
     private final Map<Integer,Map<Integer,Integer>> pseStats = new TreeMap<Integer, Map<Integer,Integer>>();
     private int pseCount = 0;
 
     public ListMiner(Map<Option, String> values) {
         inputFilePath = values.get(Option.INPUT_FILE);
         Validate.checkNull(inputFilePath, "Need to specify an input file");
 
         outputFilePath = values.get(Option.OUTPUT_FILE);
         Validate.checkNull(outputFilePath, "Need to specify an output file");
 
         minSupport = Validate.parseInt(values.get(Option.MIN_SUPPORT), DEFAULT_MIN_SUPPORT);
         Validate.isTrue(minSupport >= 2, "Min support must be at least 2");
 
         minPeriod = Validate.parseInt(values.get(Option.MIN_PERIOD), DEFAULT_MIN_PERIOD);
         Validate.isTrue(minPeriod >= 1, "Min period must be at least 1");
 
         maxPeriod =  Validate.parseInt(values.get(Option.MAX_PERIOD), DEFAULT_MAX_PERIOD);
         Validate.isTrue(maxPeriod >= minPeriod && maxPeriod <= Integer.MAX_VALUE,
                 "Max Period must be at least " + minPeriod + " and at most " + Integer.MAX_VALUE);
 
         smoothStep = Validate.parseInt(values.get(Option.SMOOTH_STEP), DEFAULT_SMOOTH_STEP);
         Validate.isTrue(smoothStep == 1, "Smoothing is currently not implemented");
 
         printGraphs = Validate.checkFlag(values.get(Option.PRINT_GRAPHS), DEFAULT_PRINT_GRAPHS);
         performSubsumation = Validate.checkFlag(values.get(Option.SUBSUME), DEFAULT_SUBSUMES);
     }
 
     public static void main(String[] args) throws Exception {
         new ListMinerFactory(args).build().run();
     }
 
     /**
      * Processes the input file and generates periodic subgraphs
      */
     public void run() throws Exception {
         //open input/output files and writer
         File inputFile = new File(inputFilePath);
         BufferedReader reader = new BufferedReader(new FileReader(inputFile));
         File outputFile = new File(outputFilePath);
         if (outputFile.exists()) {
             outputFile.delete();
         }
         outputFile.createNewFile();
         BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
 
         //initialize
         String line = null;
         int time = 1;
 
         //obtain start time for simple profiling
         long startTime = System.currentTimeMillis();
 
         //parse input file
         while ((line = reader.readLine()) != null) {
             Graph currentGraph = new Graph(line);
             graphs.add(currentGraph);
 
             //add a row representing a period to the descriptor table
             descriptorTable.addRow();
             if (performSubsumation) {
                 flushedDescriptorTable.addRow();
                 periodFactors.add(factorize(time-1));
             }
 
             //add corresponding initial descriptors representing the phase for
             //current row (period) of table
             for (int i = 0; i < time-1; i++) {
                 ArrayList<Descriptor> descriptors = new ArrayList<Descriptor>(1);
                descriptors.add(new Descriptor(graphs.get(i), 1, i));
                 descriptorTable.set(time-1, i, descriptors);
 
                 if (performSubsumation) {
                     flushedDescriptorTable.set(time-1, i, new ArrayList<Descriptor>(1));
                 }
             }
 
             updateDescriptors(time, currentGraph);
 
             time++;
         }
 
         //flush all the remaning descriptors in the table
         int limit = Math.min(maxPeriod, descriptorTable.numRows()-1);
         for (int i = 0; i <= limit; i++) {
             for (int j = 0; j < descriptorTable.numCols(i); j++) {
                 for (Descriptor g : descriptorTable.get(i, j)) {
                     flush(g, i, j);
                 }
             }
         }
 
         long endTime = System.currentTimeMillis();
 
         System.out.println("Completion Time: " + (endTime-startTime) + " ms");
         System.out.println("Number of PSEs:  " + pseCount);
 
         //write out the stats required for project grading
         for (Entry<Integer, Map<Integer, Integer>> e1 : pseStats.entrySet()) {
             int timeBucket = e1.getKey();
             for (Entry<Integer, Integer> e2 : e1.getValue().entrySet()) {
                 int sigma = e2.getKey();
                 int supportCount = e2.getValue();
                 writer.write(timeBucket*100 + "-" + sigma + "=" + supportCount + "\n");
             }
         }
         writer.flush();
         writer.close();
     }
 
     /**
      * Updates descriptors for all applicable phases/periods based on the current Graph
      */
     private void updateDescriptors(int time, Graph currentGraph) {
         for (int period = 1; period <= Math.min(time-1, maxPeriod); period++) {
             final int phase = (time-1) % period;
 
             //obtain current descriptors for period/phase, ordered chronologically descending
             ArrayList<Descriptor> descriptors = descriptorTable.get(period, phase);
 
             /*
              * Instead of shuffling around descriptors in the descriptors array, just add
              * them to new descriptors array if they are still continuing, and then
              * set the corresponding value in descriptors table to new descriptors array
              */
             ArrayList<Descriptor> newDescriptors = new ArrayList<Descriptor>(descriptors.size());
 
             Descriptor currentDescriptor = new Descriptor(currentGraph, 1, time);
 
             final int dSize = descriptors.size();
             for (int i = 0; i < dSize; i++) {
                 /*
                  * Perform maximal common subgraph computation
                  * 
                  * F = currentDescriptor.graph, G = descriptors.get(i).graph
                  * 
                  * 1) F AND G = NULL                flush g, and the rest of the descriptors out, add F, then exit
                  * 2) F AND G = F = G               update all the descriptors, do NOT add F, then exit
                  * 3) F AND G = G                   update all the descriptors, add F, then exit
                  * 4) F AND G = F                   flush g, do NOT add F, repeat MCS with other descriptors with intersected result
                  * 5) F AND G = H != (F OR G)       flush g, add F, repeat MCS with other descriptors with intersected result
                  */
 
                 Result result = Graph.compute(currentDescriptor.graph, descriptors.get(i).graph);
 
                 if (result.scenario == Graph.NO_INTERSECTION) {
                     newDescriptors.add(currentDescriptor);
                     for (int j = i; j < dSize; j++) {
                         flush(descriptors.get(j), period, phase);
                     }
                     break;
                 } else if (result.scenario == Graph.EQUAL_INTERSECTION) {
                     for (int j = i; j < dSize; j++) {
                         descriptors.get(j).incrementCount();
                         newDescriptors.add(descriptors.get(j));
                     }
                     break;
                 } else if (result.scenario == Graph.PARTIAL_INTERSECTION_EQUALS_G) {
                     newDescriptors.add(currentDescriptor);
                     for (int j = i; j < dSize; j++) {
                         descriptors.get(j).incrementCount();
                         newDescriptors.add(descriptors.get(j));
                     }
                     break;
                 } else if (result.scenario == Graph.PARTIAL_INTERSECTION_EQUALS_F) {
                     flush(descriptors.get(i), period, phase);
                     Graph intersection = new Graph(result.elements);
 
                     currentDescriptor = new Descriptor(intersection, descriptors.get(i));
 
                     //if last element, make sure to add the current descriptor
                     if (i == dSize-1) {
                         newDescriptors.add(currentDescriptor);
                     }
                 } else { //result.scenario == MaximumCommonSubgraph.PARTIAL_INTERSECTION_EQUALS_NONE
                     flush(descriptors.get(i), period, phase);
                     newDescriptors.add(currentDescriptor);
                     Graph intersection = new Graph(result.elements);
 
                     currentDescriptor = new Descriptor(intersection, descriptors.get(i));
 
                     //if last element, make sure to add the current descriptor
                     if (i == dSize-1) {
                         newDescriptors.add(currentDescriptor);
                     }
                 }
             }
             descriptorTable.set(period, phase, newDescriptors);
         }
     }
 
     /**
      * Returns factors for input number from 2 to n (excludes 1).
      */
     private static ArrayList<Integer> factorize(int number) {
         ArrayList<Integer> factors = new ArrayList<Integer>();
         //skip if number is 1
         if (number == 1) {
             return factors;
         }
         int max = (int) Math.sqrt(number);
         for (int factor = 2; factor <= max; factor++) {
             if (number % factor == 0) {
                 factors.add(factor);
                 if (factor != number/factor) {
                     factors.add(number/factor);
                 }
             }
         }
         //add the number itself
         factors.add(number);
         return factors;
     }
 
     /**
      * Flushes the descriptor for a given period/phase if it meets the requirements
      */
     private void flush(Descriptor descriptor, int period, int phase) {
         if (descriptor.getCount() >= minSupport) {
             /*
              * If performing subsumation, check against all possible descriptors
              * that may subsume the descriptor being flushed, and if subsumed,
              * do not flush the descriptor
              */
             if (performSubsumation) {
                 List<Integer> factors = periodFactors.get(period);
                 final int p1 = period;
                 final int m1 = phase;
                 for (Integer factor : factors) {
                     final int p2 = period/factor;
 
                     for (int m2 = 0; m2 < p2; m2++) {
                         boolean subsumes = checkSubsumation(flushedDescriptorTable.get(p2, m2), descriptor, p1, m1, p2, m2);
                         if (subsumes) {
                             return;
                         }
                         subsumes = checkSubsumation(descriptorTable.get(p2, m2), descriptor, p1, m1, p2, m2);
                         if (subsumes) {
                             return;
                         }
 
                     }
                 }
                 flushedDescriptorTable.get(period, phase).add(descriptor);
             }
 
             //check if printing each individual graph
             if (printGraphs) {
                 //print graph following the same format as PSEMiner
                 StringBuilder sb = new StringBuilder();
                 sb.append("start ").append(descriptor.startTime);
                 sb.append(" psup ").append(descriptor.getCount());
                 sb.append(" p ").append(period);
                 sb.append(" m ").append(phase);
                 sb.append(" ").append(descriptor.graph);
                 System.out.println(sb.toString());
             }
 
             updateStats(descriptor);
         }
     }
 
     /**
      * Update the total number of observed periodic subgraphs, and the count
      * for the number of subgraphs started between (floor(time/100)*100 to (floor(time/100))*100+100.
      * The second statistic is for school project grading.
      */
     private void updateStats(Descriptor descriptor) {
         pseCount++;
 
         int timeBucket = descriptor.startTime / 100;
         Map<Integer, Integer> supportCounts;
         if ((supportCounts = pseStats.get(timeBucket)) == null) {
             supportCounts = new TreeMap<Integer, Integer>();
             pseStats.put(timeBucket, supportCounts);
         }
 
         int occurrences = descriptor.getCount();
         Integer supportSize;
         if ((supportSize = supportCounts.get(occurrences)) == null) {
             supportCounts.put(occurrences, 1);
         } else {
             supportCounts.put(occurrences, supportSize+1);
         }
     }
 
     /**
      * Checks if any of the given descriptors subsume or include descriptor d1.
      */
     private boolean checkSubsumation(List<Descriptor> descriptors, Descriptor d1, int p1, int m1, int p2, int m2) {
         for (Descriptor d2 : descriptors) {
             //check if the start time is valid
             if (d2.startTime > d1.startTime) {
                 continue;
             }
             //check if the start times are in sync
             float cycles = (float)(d1.startTime - d2.startTime)/p2;
             if ((int) cycles != cycles) {
                 continue;
             }
 
             //check endtimes
             int end1 = d1.startTime + (d1.getCount()-1)*p1;
             int end2 = d2.startTime + (d2.getCount()-1)*p2;
             if (end1 > end2) {
                 continue;
             }
 
             if(d2.graph.contains(d1.graph)) {
                 return true;
             }
         }
         return false;
     }
 }
