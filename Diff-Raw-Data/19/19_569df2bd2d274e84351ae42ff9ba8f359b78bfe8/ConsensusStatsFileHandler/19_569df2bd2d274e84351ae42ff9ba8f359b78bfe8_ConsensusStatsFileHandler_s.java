 import java.io.*;
 import java.util.*;
 
 /**
  *
  */
 public class ConsensusStatsFileHandler {
   private String statsDir;
   private File consensusStatsRawFile;
   private File bridgeConsensusStatsRawFile;
   private File consensusStatsFile;
   private SortedMap<String, String> consensusResults;
   private SortedMap<String, String> bridgeConsensusResults;
   private boolean initialized;
   private boolean modified;
   public ConsensusStatsFileHandler(String statsDir) {
     this.statsDir = statsDir;
     this.consensusResults = new TreeMap<String, String>();
     this.consensusStatsRawFile = new File(statsDir
         + "/consensus-stats-raw");
     this.bridgeConsensusResults = new TreeMap<String, String>();
     this.bridgeConsensusStatsRawFile = new File(statsDir
         + "/bridge-consensus-stats-raw");
     this.consensusStatsFile = new File(statsDir + "/consensus-stats");
   }
   public void initialize() throws IOException {
     if (this.initialized) {
       return;
     }
     if (this.consensusStatsRawFile.exists()) {
       System.out.print("Reading file " + statsDir
           + "/consensus-stats-raw... ");
       BufferedReader br = new BufferedReader(new FileReader(
           this.consensusStatsRawFile));
       String line = null;
       while ((line = br.readLine()) != null) {
         consensusResults.put(line.split(",")[0], line);
       }
       System.out.println("done");
       br.close();
     }
     if (this.bridgeConsensusStatsRawFile.exists()) {
       System.out.print("Reading file " + statsDir
           + "/bridge-consensus-stats-raw... ");
       BufferedReader br = new BufferedReader(new FileReader(
           this.bridgeConsensusStatsRawFile));
       String line = null;
       while ((line = br.readLine()) != null) {
         bridgeConsensusResults.put(line.split(",")[0], line);
       }
       System.out.println("done");
       br.close();
     }
    this.initialized = true;
   }
   public void addConsensusResults(String validAfter, int exit, int fast,
       int guard, int running, int stable) throws IOException {
     if (!this.initialized) {
       throw new RuntimeException("Not initialized!");
     }
     consensusResults.put(validAfter, validAfter + "," + exit + "," + fast
         + "," + guard + "," + running + "," + stable);
     this.modified = true;
   }
   public void addBridgeConsensusResults(String published, int running)
       throws IOException {
     if (!this.initialized) {
       throw new RuntimeException("Not initialized!");
     }
     bridgeConsensusResults.put(published, published + "," + running);
     this.modified = true;
   }
   public void writeFile() {
     if (!this.modified) {
       return;
     }
     SortedMap<String, String> csAggr = new TreeMap<String, String>();
     SortedMap<String, String> bcsAggr = new TreeMap<String, String>();
     try {
       if (!consensusResults.isEmpty()) {
         System.out.print("Writing file " + this.statsDir
             + "/consensus-stats-raw... ");
         new File(this.statsDir).mkdirs();
         BufferedWriter bwConsensusStatsRaw = new BufferedWriter(
             new FileWriter(this.consensusStatsRawFile));
         String tempDate = null;
         int exitDay = 0, fastDay = 0, guardDay = 0, runningDay = 0,
             stableDay = 0, consensusesDay = 0;
         Iterator<String> it = consensusResults.values().iterator();
         boolean haveWrittenFinalLine = false;
           while (it.hasNext() || !haveWrittenFinalLine) {
             String next = it.hasNext() ? it.next() : null;
             if (tempDate != null
                 && (next == null
                 || !next.substring(0, 10).equals(tempDate))) {
             if (consensusesDay > 11) {
               csAggr.put(tempDate, tempDate + ","
                   + (exitDay / consensusesDay) + ","
                   + (fastDay / consensusesDay) + ","
                   + (guardDay / consensusesDay) + ","
                   + (runningDay / consensusesDay) + ","
                   + (stableDay / consensusesDay));
             }
             exitDay = 0;
             fastDay = 0;
             guardDay = 0;
             runningDay = 0;
             stableDay = 0;
             consensusesDay = 0;
             if (next == null) {
               haveWrittenFinalLine = true;
             }
           }
           if (next != null) {
             bwConsensusStatsRaw.append(next + "\n");
             String[] parts = next.split(",");
             tempDate = next.substring(0, 10);
             consensusesDay++;
             exitDay += Integer.parseInt(parts[1]);
             fastDay += Integer.parseInt(parts[2]);
             guardDay += Integer.parseInt(parts[3]);
             runningDay += Integer.parseInt(parts[4]);
             stableDay += Integer.parseInt(parts[5]);
           }
         }
         bwConsensusStatsRaw.close();
         System.out.println("done");
       }
       if (!bridgeConsensusResults.isEmpty()) {
         System.out.print("Writing file " + this.statsDir
             + "/bridge-consensus-stats-raw... ");
         new File(this.statsDir).mkdirs();
         BufferedWriter bwBridgeConsensusStatsRaw = new BufferedWriter(
             new FileWriter(this.bridgeConsensusStatsRawFile));
         String tempDate = null;
         int brunningDay = 0, bridgeStatusesDay = 0;
         Iterator<String> it = bridgeConsensusResults.values().iterator();
         boolean haveWrittenFinalLine = false;
         while (it.hasNext() || !haveWrittenFinalLine) {
           String next = it.hasNext() ? it.next() : null;
           if (tempDate != null
               && (next == null
               || !next.substring(0, 10).equals(tempDate))) {
             if (bridgeStatusesDay > 23) {
               bcsAggr.put(tempDate, "" + (brunningDay / bridgeStatusesDay)
                   + "\n");
             }
             brunningDay = 0;
             bridgeStatusesDay = 0;
             if (next == null) {
               haveWrittenFinalLine = true;
             }
           }
           if (next != null) {
             bwBridgeConsensusStatsRaw.append(next + "\n");
             tempDate = next.substring(0, 10);
             bridgeStatusesDay++;
             brunningDay += Integer.parseInt(next.split(",")[1]);
           }
         }
         bwBridgeConsensusStatsRaw.close();
         System.out.println("done");
       }
       if (!csAggr.isEmpty() || !bcsAggr.isEmpty()) {
         System.out.print("Writing file " + this.statsDir
             + "/consensus-stats... ");
         new File(this.statsDir).mkdirs();
         BufferedWriter bwConsensusStats = new BufferedWriter(
             new FileWriter(this.consensusStatsFile));
         bwConsensusStats.append("date,exit,fast,guard,running,stable,"
             + "brunning\n");
         SortedSet<String> allDates = new TreeSet<String>();
         allDates.addAll(csAggr.keySet());
         allDates.addAll(bcsAggr.keySet());
         for (String date : allDates) {
           if (csAggr.containsKey(date)) {
             bwConsensusStats.append(csAggr.get(date));
           } else {
             bwConsensusStats.append(date + ",NA,NA,NA,NA,NA");
           }
           if (bcsAggr.containsKey(date)) {
             bwConsensusStats.append("," + bcsAggr.get(date));
           } else {
             bwConsensusStats.append(",NA\n");
           }
         }
         bwConsensusStats.close();
         System.out.println("done");
       }
     } catch (IOException e) {
       System.out.println("failed");
     }
   }
 }
 
