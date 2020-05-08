 import java.io.*;
 import java.text.*;
 import java.util.*;
 import java.util.logging.*;
 
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
   private SortedMap<String, String> csAggr =
       new TreeMap<String, String>();
   private SortedMap<String, String> bcsAggr =
       new TreeMap<String, String>();
   private boolean initialized;
   private boolean consensusResultsModified;
   private boolean bridgeConsensusResultsModified;
   private Logger logger;
   public ConsensusStatsFileHandler(String statsDir) {
     this.statsDir = statsDir;
     this.consensusResults = new TreeMap<String, String>();
     this.consensusStatsRawFile = new File(statsDir
         + "/consensus-stats-raw");
     this.bridgeConsensusResults = new TreeMap<String, String>();
     this.bridgeConsensusStatsRawFile = new File(statsDir
         + "/bridge-consensus-stats-raw");
     this.consensusStatsFile = new File(statsDir + "/consensus-stats");
     this.logger =
         Logger.getLogger(ConsensusStatsFileHandler.class.getName());
   }
   public void initialize() throws IOException {
     if (this.initialized) {
       return;
     }
     this.initialized = true;
     if (this.consensusStatsRawFile.exists()) {
       this.logger.info("Reading file " + statsDir
           + "/consensus-stats-raw...");
       BufferedReader br = new BufferedReader(new FileReader(
           this.consensusStatsRawFile));
       String line = null;
       while ((line = br.readLine()) != null) {
         this.consensusResults.put(line.split(",")[0], line);
       }
       br.close();
       this.logger.info("Finished reading file " + statsDir
           + "/consensus-stats-raw.");
     }
     if (this.bridgeConsensusStatsRawFile.exists()) {
       this.logger.info("Reading file " + statsDir
           + "/bridge-consensus-stats-raw...");
       BufferedReader br = new BufferedReader(new FileReader(
           this.bridgeConsensusStatsRawFile));
       String line = null;
       while ((line = br.readLine()) != null) {
         bridgeConsensusResults.put(line.split(",")[0], line);
       }
       br.close();
       this.logger.info("Finished reading file " + statsDir
           + "/bridge-consensus-stats-raw.");
     }
     if (this.consensusStatsFile.exists()) {
       this.logger.info("Reading file " + statsDir
           + "/consensus-stats...");
       BufferedReader br = new BufferedReader(new FileReader(
           this.consensusStatsFile));
       String line = br.readLine();
       while ((line = br.readLine()) != null) {
         String[] parts = line.split(",");
         String date = parts[0];
         boolean foundOneNotNA = false;
         for (int i = 1; i < parts.length - 1; i++) {
           if (!parts[i].equals("NA")) {
             foundOneNotNA = true;
             break;
           }
         }
         if (foundOneNotNA) {
           String relays = line.substring(0, line.lastIndexOf(","));
           String bridges = line.substring(line.lastIndexOf(",") + 1) + "\n";
           csAggr.put(date, relays);
           bcsAggr.put(date, bridges);
         }
       }
       br.close();
       this.logger.info("Finished reading file " + statsDir
           + "/consensus-stats.");
     }
   }
   public void addConsensusResults(String validAfter, int exit, int fast,
       int guard, int running, int stable) throws IOException {
     if (!this.initialized) {
       throw new RuntimeException("Not initialized!");
     }
     this.consensusResults.put(validAfter, validAfter + "," + exit + "," + fast
         + "," + guard + "," + running + "," + stable);
     this.consensusResultsModified = true;
   }
   public void addBridgeConsensusResults(String published, int running)
       throws IOException {
     if (!this.initialized) {
       throw new RuntimeException("Not initialized!");
     }
     bridgeConsensusResults.put(published, published + "," + running);
     this.bridgeConsensusResultsModified = true;
   }
   public void writeFile() {
     boolean writeConsensusStatsRaw = false;
     boolean writeBridgeConsensusStatsRaw = false;
     boolean writeConsensusStats = false;
     try {
       BufferedWriter bwConsensusStatsRaw = null;
       if (!this.consensusResults.isEmpty()) {
         if (this.consensusResultsModified) {
           this.logger.info("Writing file " + this.statsDir
               + "/consensus-stats-raw...");
           writeConsensusStatsRaw = true;
           new File(this.statsDir).mkdirs();
           bwConsensusStatsRaw = new BufferedWriter(
               new FileWriter(this.consensusStatsRawFile));
         }
         String tempDate = null;
         int exitDay = 0, fastDay = 0, guardDay = 0, runningDay = 0,
             stableDay = 0, consensusesDay = 0;
         Iterator<String> it = this.consensusResults.values().iterator();
         boolean haveWrittenFinalLine = false;
         while (it.hasNext() || !haveWrittenFinalLine) {
           String next = it.hasNext() ? it.next() : null;
           if (tempDate != null
               && (next == null
               || !next.substring(0, 10).equals(tempDate))) {
             if (consensusesDay > 11) {
               String line = tempDate + ","
                   + (exitDay / consensusesDay) + ","
                   + (fastDay / consensusesDay) + ","
                   + (guardDay / consensusesDay) + ","
                   + (runningDay / consensusesDay) + ","
                   + (stableDay / consensusesDay);
               if (!line.equals(this.csAggr.get(tempDate))) {
                 this.csAggr.put(tempDate, line);
                 writeConsensusStats = true;
               }
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
             if (writeConsensusStatsRaw) {
               bwConsensusStatsRaw.append(next + "\n");
             }
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
         if (writeConsensusStatsRaw) {
           bwConsensusStatsRaw.close();
           this.logger.info("Finished writing file " + this.statsDir
               + "/consensus-stats-raw.");
         }
       }
     } catch (IOException e) {
       this.logger.log(Level.WARNING, "Failed writing file "
           + this.statsDir + "/consensus-stats-raw!", e);
       return;
     }
     try {
       BufferedWriter bwBridgeConsensusStatsRaw = null;
       if (!this.bridgeConsensusResults.isEmpty()) {
         if (this.bridgeConsensusResultsModified) {
           this.logger.info("Writing file " + this.statsDir
               + "/bridge-consensus-stats-raw...");
           writeBridgeConsensusStatsRaw = true;
           new File(this.statsDir).mkdirs();
           bwBridgeConsensusStatsRaw = new BufferedWriter(
               new FileWriter(this.bridgeConsensusStatsRawFile));
         }
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
               String line = "" + (brunningDay / bridgeStatusesDay) + "\n";
               if (!line.equals(this.bcsAggr.get(tempDate))) {
                 this.bcsAggr.put(tempDate, line);
                 writeConsensusStats = true;
               }
             }
             brunningDay = 0;
             bridgeStatusesDay = 0;
             if (next == null) {
               haveWrittenFinalLine = true;
             }
           }
           if (next != null) {
             if (writeBridgeConsensusStatsRaw) {
               bwBridgeConsensusStatsRaw.append(next + "\n");
             }
             tempDate = next.substring(0, 10);
             bridgeStatusesDay++;
             brunningDay += Integer.parseInt(next.split(",")[1]);
           }
         }
         if (writeBridgeConsensusStatsRaw) {
           bwBridgeConsensusStatsRaw.close();
           this.logger.info("Finished writing file " + this.statsDir
               + "/bridge-consensus-stats-raw.");
         }
       }
     } catch (IOException e) {
       this.logger.log(Level.WARNING, "Failed writing file "
           + this.statsDir + "/bridge-consensus-stats-raw!", e);
     }
    if (writeConsensusStats &&
        !(this.csAggr.isEmpty() && this.bcsAggr.isEmpty())) {
       this.logger.info("Writing file " + this.statsDir
           + "/consensus-stats...");
       try {
         new File(this.statsDir).mkdirs();
         BufferedWriter bwConsensusStats = new BufferedWriter(
             new FileWriter(this.consensusStatsFile));
         bwConsensusStats.append("date,exit,fast,guard,running,stable,"
             + "brunning\n");
         SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
         format.setTimeZone(TimeZone.getTimeZone("UTC"));
        long firstDate = 0L, lastDate = 0L;
        if (this.csAggr.isEmpty()) {
          firstDate = format.parse(this.bcsAggr.firstKey()).getTime();
          lastDate = format.parse(this.bcsAggr.lastKey()).getTime();
        } else if (this.bcsAggr.isEmpty()) {
          firstDate = format.parse(this.csAggr.firstKey()).getTime();
          lastDate = format.parse(this.csAggr.lastKey()).getTime();
        } else {
          firstDate = Math.min(
              format.parse(this.csAggr.firstKey()).getTime(),
              format.parse(this.bcsAggr.firstKey()).getTime());
          lastDate = Math.max(
              format.parse(this.csAggr.lastKey()).getTime(),
              format.parse(this.bcsAggr.lastKey()).getTime());
        }
         long currentDate = firstDate;
         while (currentDate <= lastDate) {
           String date = format.format(new Date(currentDate));
           if (this.csAggr.containsKey(date)) {
             bwConsensusStats.append(this.csAggr.get(date));
           } else {
             bwConsensusStats.append(date + ",NA,NA,NA,NA,NA");
           }
           if (this.bcsAggr.containsKey(date)) {
             bwConsensusStats.append("," + this.bcsAggr.get(date));
           } else {
             bwConsensusStats.append(",NA\n");
           }
           currentDate += 86400000L;
         }
         bwConsensusStats.close();
         this.logger.info("Finished writing file " + this.statsDir
             + "/consensus-stats.");
       } catch (IOException e) {
         this.logger.log(Level.WARNING, "Failed writing file "
             + this.statsDir + "/consensus-stats!", e);
       } catch (ParseException e) {
         this.logger.log(Level.WARNING, "Failed writing file "
             + this.statsDir + "/consensus-stats!", e);
       }
     }
   }
 }
 
