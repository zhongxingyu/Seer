 import java.io.*;
 import java.util.*;
 
 /**
  *
  */
 public class BridgeStatsFileHandler {
   private String statsDir;
   private File bridgeStatsFile;
   private File hashedRelayIdentitiesFile;
   private SortedSet<String> countries;
   private SortedSet<String> hashedRelays = new TreeSet<String>();
   private SortedMap<String, String> observations;
   private boolean initialized;
   private boolean modified;
   public BridgeStatsFileHandler(String statsDir,
       SortedSet<String> countries) {
     this.statsDir = statsDir;
     this.countries = countries;
     this.bridgeStatsFile = new File(statsDir + "/bridge-stats");
     this.observations = new TreeMap<String, String>();
     this.hashedRelayIdentitiesFile = new File(statsDir
         + "/hashed-relay-identities");
   }
   public void initialize() throws IOException {
     if (this.initialized) {
       return;
     }
    this.initialized = true;
     if (this.bridgeStatsFile.exists()) {
       System.out.print("Reading file " + statsDir + "/bridge-stats... ");
       BufferedReader br = new BufferedReader(new FileReader(
           this.bridgeStatsFile));
       String line = br.readLine();
       if (line != null) {
         String[] headers = line.split(",");
         for (int i = 3; i < headers.length; i++) {
           this.countries.add(headers[i]);
         }
         while ((line = br.readLine()) != null) {
           String[] readData = line.split(",");
           String hashedBridgeIdentity = readData[0];
           String date = readData[1];
           String time = readData[2];
           SortedMap<String, String> obs = new TreeMap<String, String>();
           for (int i = 3; i < readData.length; i++) {
             obs.put(headers[i], readData[i]);
           }
           this.addObs(hashedBridgeIdentity, date, time, obs);
         }
       }
       System.out.println("done");
       br.close();
     }
     if (this.hashedRelayIdentitiesFile.exists()) {
       System.out.print("Reading file " + statsDir
           + "/hashed-relay-identities... ");
       BufferedReader br = new BufferedReader(new FileReader(
           this.hashedRelayIdentitiesFile));
       String line = null;
       while ((line = br.readLine()) != null) {
         this.hashedRelays.add(line);
       }
       br.close();
       System.out.println("done");
     }
   }
   public void addHashedRelay(String hashedRelayIdentity)
       throws IOException {
     if (!this.initialized) {
       throw new RuntimeException("Not initialized!");
     }
     this.hashedRelays.add(hashedRelayIdentity);
     this.modified = true;
   }
   public boolean isKnownRelay(String hashedBridgeIdentity)
       throws IOException {
     if (!this.initialized) {
       throw new RuntimeException("Not initialized!");
     }
     return this.hashedRelays.contains(hashedBridgeIdentity);
   }
   public void addObs(String hashedIdentity, String date,
       String time, Map<String, String> obs) throws IOException {
     if (!this.initialized) {
       throw new RuntimeException("Not initialized!");
     }
     String key = hashedIdentity + "," + date;
     StringBuilder sb = new StringBuilder(key + "," + time);
     for (String c : countries) {
       sb.append("," + (obs.containsKey(c) ? obs.get(c) : "0.0"));
     }
     String value = sb.toString();
     if (!this.observations.containsKey(key)
         || value.compareTo(this.observations.get(key)) > 0) {
       this.observations.put(key, value);
     }
     this.modified = true;
   }
 
   public void writeFile() {
     if (!this.modified) {
       return;
     }
     try {
       if (!this.hashedRelays.isEmpty()) {
         System.out.print("Writing file " + this.statsDir
             + "/hashed-relay-identities... ");
         new File(this.statsDir).mkdirs();
         BufferedWriter bwRelayIdentities = new BufferedWriter(
             new FileWriter(this.hashedRelayIdentitiesFile));
         for (String hashedRelay : this.hashedRelays) {
           bwRelayIdentities.append(hashedRelay + "\n");
         }
         bwRelayIdentities.close();
         System.out.println("done");
       }
       if (!this.observations.isEmpty()) {
         System.out.print("Writing file " + this.statsDir
             + "/bridge-stats...");
         new File(this.statsDir).mkdirs();
         BufferedWriter bwBridgeStats = new BufferedWriter(
             new FileWriter(this.bridgeStatsFile));
         bwBridgeStats.append("bridge,date,time");
         for (String c : this.countries) {
           bwBridgeStats.append("," + c);
         }
         bwBridgeStats.append("\n");
         for (String observation : this.observations.values()) {
           String hashedBridgeIdentity = observation.split(",")[0];
           if (!this.hashedRelays.contains(hashedBridgeIdentity)) {
             bwBridgeStats.append(observation + "\n");
           }
         }
         bwBridgeStats.close();
         System.out.println("done");
       }
     } catch (IOException e) {
       System.out.println("failed");
     }
   }
 }
 
