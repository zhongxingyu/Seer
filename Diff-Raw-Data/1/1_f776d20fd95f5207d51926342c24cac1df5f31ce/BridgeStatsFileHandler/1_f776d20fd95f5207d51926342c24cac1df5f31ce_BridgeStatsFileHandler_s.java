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
   public BridgeStatsFileHandler(String statsDir,
       SortedSet<String> countries) throws IOException {
     this.statsDir = statsDir;
     this.countries = countries;
     this.bridgeStatsFile = new File(statsDir + "/bridge-stats");
     this.observations = new TreeMap<String, String>();
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
     this.hashedRelayIdentitiesFile = new File(statsDir
         + "/hashed-relay-identities");
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
   public void addHashedRelay(String hashedRelayIdentity) {
     this.hashedRelays.add(hashedRelayIdentity);
   }
   public boolean isKnownRelay(String hashedBridgeIdentity) {
     return this.hashedRelays.contains(hashedBridgeIdentity);
   }
   public void addObs(String hashedIdentity, String date,
       String time, Map<String, String> obs) {
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
   }
 
   public void writeFile() throws IOException {
     System.out.print("Writing file " + this.statsDir
         + "/hashed-relay-identities... ");
     BufferedWriter bwRelayIdentities = new BufferedWriter(
         new FileWriter(this.hashedRelayIdentitiesFile));
     for (String hashedRelay : this.hashedRelays) {
       bwRelayIdentities.append(hashedRelay + "\n");
     }
     bwRelayIdentities.close();
     System.out.print("done\nWriting file " + this.statsDir
         + "/bridge-stats...");
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
   }
 }
 
