 /*
  * Copyright (c) 2013, Kyle Mulleady
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * Neither the name of the organization nor the
  *       names of its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY KYLE MULLEADY ''AS IS'' AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL KYLE MULLEADY BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package ics311km;
 
 import java.io.FileReader;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.Scanner;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.Random;
 
 public class Driver implements Const {
 
     private static String[] names;
     private static Map<String, List<Long>> dict;
     private static DynamicSet[] dynamicSets; 
 
     public static void main(String[] args) {
         if (args.length != 1) {
             log("Usage: java Driver <inputfile>");
             System.exit(1);
         }
         // Load names into array.
         loadData(args[0]);
         // Create a map for organizing output.
         dict = new HashMap<String, List<Long>>();
         Scanner scan = new Scanner(System.in);
         dynamicSets = new DynamicSet[NUM_DATA_STRUCTURES];
         dynamicSets[0] = new DLLDynamicSet();
         dynamicSets[1] = new SkipListDynamicSet();
         dynamicSets[2] = new BSTDynamicSet();
         try {
             // App interface.
             while (true) {
                 log("\nAvailable commands: runtest insert search delete pred succ min max size p");
                 log("Press q <Enter> to quit.");
                 System.out.print("Enter a command: ");
                 // Execute command.
                 runCommand(scan.nextLine().trim());
             }
         }
         catch (NoSuchElementException e) {
         	if (scan != null)
         		scan.close();
             quit();
         }
     }
 
     private static void runCommand(String input) {
         Scanner scan = new Scanner(System.in);
         if (input.equals("q")) {
         	if (scan != null)
         		scan.close();
             quit();
         }
         else if (input.equals(RUNTEST)) {
             runtest();
         }
         else if (input.equals(INSERT)) {
             System.out.print("Enter key to be inserted: ");
             insert(scan.nextLine());
         }
         else if (input.equals(SEARCH)) {
             System.out.print("Enter key to be searched: ");
             search(scan.nextLine());
         }
         else if (input.equals(DELETE)) {
             System.out.print("Enter key to be deleted: ");
             delete(scan.nextLine());
         }
         else if (input.equals(PRED)) {
             System.out.print("Enter key to find predecessor of: ");
             pred(scan.nextLine());
         }
         else if (input.equals(SUCC)) {
             System.out.print("Enter key to find successor of: ");
             succ(scan.nextLine());
         }
         else if (input.equals(MIN)) {
             min();
         }
         else if (input.equals(MAX)) {
             max();
         }
         else if (input.equals("size")) {
             for (DynamicSet ds : dynamicSets)
                 log(ds.getClass() + " size: " + ds.size());
         }
         else if (input.equals("p")) {
             for (DynamicSet ds : dynamicSets)
                 log(ds.getClass() + ": " + ds);
         }
         else {
             log("Unknown command.");
         }
 
     }
 
     private static void runtest() {
         // Loop through each implementation of DynamicSet.
         for (int i = 0; i < dynamicSets.length; i++) {
             // List for storing returned keys.
             List<Object> keyTypes = new ArrayList<Object>();
             // long variables for timing method calls.
             long start, runtime;
             // List for storing runtimes.
             List<Long> insertTimes = new ArrayList<Long>();
             // First DynamicSet is the linked list implementation.
             DynamicSet ds = dynamicSets[i];
             // For loop for 'insert'.
             for (int j = 0; j < names.length; j++) {
                 KeyType k = new KeyType(names[j]);
                 start = System.nanoTime();
                 ds.insert(k, null);
                 runtime = System.nanoTime() - start;
                 insertTimes.add(runtime);
             }
             // Add 'insert' runtimes to map.
             dict.put(INSERT+i, insertTimes);
             Random rand = new Random();
             List<Long> searchTimes = new ArrayList<Long>();
             // For loop for 'search'.
             for (int j = 0; j < LIMIT; j++) {
                 int randomInt = rand.nextInt(names.length);
                 KeyType k = new KeyType(names[randomInt]);
                 start = System.nanoTime();
                 Object o = ds.search(k);
                 runtime = System.nanoTime() - start;
                 searchTimes.add(runtime);
                 keyTypes.add(o);
             }
             // Add 'search' runtimes to map.
             dict.put(SEARCH+i, searchTimes);
             List<Long> predTimes = new ArrayList<Long>();
             // For loop for 'pred'.
             for (int j = 0; j < keyTypes.size(); j++) {
                 KeyType k = (KeyType)keyTypes.get(j);
                 start = System.nanoTime();
                 ds.predecessor(k);
                 runtime = System.nanoTime() - start;
                 predTimes.add(runtime);
             }
             // Add 'pred' runtimes to map.
             dict.put(PRED+i, predTimes);
             List<Long> succTimes = new ArrayList<Long>();
             // For loop for 'succ'.
             for (int j = 0; j < keyTypes.size(); j++) {
                 KeyType k = (KeyType)keyTypes.get(j);
                 start = System.nanoTime();
                 ds.successor(k);
                 runtime = System.nanoTime() - start;
                 succTimes.add(runtime);
             }
             // Add 'succ' runtimes to map.
             dict.put(SUCC+i, succTimes);
             // Run min.
             List<Long> minTimes = new ArrayList<Long>();
             start = System.nanoTime();
             ds.minimum();
             runtime = System.nanoTime() - start;
             minTimes.add(runtime);
             dict.put(MIN+i, minTimes);
             // Run max.
             List<Long> maxTimes = new ArrayList<Long>();
             start = System.nanoTime();
             ds.maximum();
             runtime = System.nanoTime() - start;
             maxTimes.add(runtime);
             dict.put(MAX+i, maxTimes);
         }
         printResults();
     }
 
     private static void insert(String key) {
         for (int i = 0; i < dynamicSets.length; i++) {
             DynamicSet ds = dynamicSets[i];
             ds.insert(new KeyType(key), null);
         }
     }
 
     private static void search(String key) {
         for (DynamicSet ds : dynamicSets) {
             KeyType k = (KeyType)ds.search(new KeyType(key));
             String val = k != null ? k.getValue() : "null";
             log("'search' for " + ds.getClass() + " returned: " + val);
         }
     }
 
     private static void delete(String key) {
         for (DynamicSet ds : dynamicSets) {
             ds.delete(new KeyType(key));
         }
     }
 
     private static void pred(String key) {
         for (DynamicSet ds : dynamicSets) {
             KeyType k = (KeyType)ds.predecessor(new KeyType(key));
             String val = k != null ? k.getValue() : "null";
             log("'pred' for " + ds.getClass() + " returned: " + val);
         }
     }
 
     private static void succ(String key) {
         for (DynamicSet ds : dynamicSets) {
             KeyType k = (KeyType)ds.successor(new KeyType(key));
             String val = k != null ? k.getValue() : "null";
             log("'succ' for " + ds.getClass() + " returned: " + val);
         }
     }
 
     private static void min() {
         for (DynamicSet ds : dynamicSets) {
             KeyType k = (KeyType)ds.minimum();
             String val = k != null ? k.getValue() : "null";
             log("'min' for " + ds.getClass() + " returned: " + val);
         }
     }
 
     private static void max() {
         for (DynamicSet ds : dynamicSets) {
             KeyType k = (KeyType)ds.maximum();
             String val = k != null ? k.getValue() : "null";
             log("'max' for " + ds.getClass() + " returned: " + val);
         }
     }
 
     private static void quit() {
         log("\nBye.");
         System.exit(0);
     }
 
     private static void loadData(String filename) {
         try {
             FileReader fr = new FileReader(filename);
             BufferedReader br = new BufferedReader(fr);
             List<String> lines = new ArrayList<String>();
             String line;
             // Load input file into a list.
             while ((line = br.readLine()) != null)
                lines.add(line);
 
             // Load global 'names' array.
             names = new String[lines.size()];
             for (int i = 0; i < lines.size(); i++)
                 names[i] = lines.get(i);
 
             if (br != null) br.close();
             if (fr != null) fr.close();
         }
         catch (IOException e) {
             log("IO error.");
             System.exit(1);
         }
     }
 
     private static long[] computeResults(String functionName, boolean loopEntireArray) {
         // The hashmap contains the runtimes for each feature--insert, search, etc.
         List<Long> runTimes = dict.get(functionName);
         long min, max, avg = 0;
         min = max = (long)runTimes.get(0);
         // For insert we run through the entire list; for others we only want 10.
         int limit = loopEntireArray ? runTimes.size() : LIMIT;
         for (int i = 1; i < limit; i++) {
             long time = (long)runTimes.get(i);
             if (time < min)
                 min = time;
             if (time > max)
                 max = time;
             avg += time;
         }
         avg /= runTimes.size();
         // Bad practice here: put min, max, & avg in an array and return it.
         return new long[] { min, max, avg };
     }
 
     private static void printResults() {
         String insert = "";
         String search = "";
         String pred   = "";
         String succ   = "";
         String min    = "";
         String max    = "";
         for (int i = 0; i < dynamicSets.length; i++) {
             // Compute results.
             long[] insertResults = computeResults(INSERT+i, true);
             long[] searchResults = computeResults(SEARCH+i, false);
             long[] predResults   = computeResults(PRED+i, false);
             long[] succResults   = computeResults(SUCC+i, false);
 
             insert += insertResults[0] + "/" + insertResults[1]  + "/" + insertResults[2] + " | ";
             search += searchResults[0] + "/" + searchResults[1] + "/" + searchResults[2] + " | ";
             pred   += predResults[0]   + "/" + predResults[1]    + "/" + predResults[2] + " | ";
             succ   += succResults[0]   + "/" + succResults[1]    + "/" + succResults[2] + " | ";
             min    += dict.get(MIN+i).get(0).toString() + "             |   ";
             max    += dict.get(MAX+i).get(0).toString() + "             |   ";
         }
 
         log("Size: " + names.length);
         log("------------------------------------------------------------------------------------");
         log("            | LL               |   SK               |  BST               | RBT      |");
         log("------------------------------------------------------------------------------------");
         log("insert      | "+insert);
         log("------------------------------------------------------------------------------------");
         log("search      | "+search);
         log("------------------------------------------------------------------------------------");
         log("predecessor | "+pred);
         log("------------------------------------------------------------------------------------");
         log("successor   | "+succ);
         log("------------------------------------------------------------------------------------");
         log("minimum     | "+min);
         log("------------------------------------------------------------------------------------");
         log("maximum     | "+max);
         log("------------------------------------------------------------------------------------");
 
         // Clear the hashmap for the next test.
         dict.clear();
     }
 
     private static void log(Object o) {
         System.out.println(String.valueOf(o));
     }
 }
