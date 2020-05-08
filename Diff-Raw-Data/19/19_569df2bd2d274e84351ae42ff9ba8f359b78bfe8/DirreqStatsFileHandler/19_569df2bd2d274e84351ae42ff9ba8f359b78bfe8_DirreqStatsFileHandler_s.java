 import java.io.*;
 import java.util.*;
 
 /**
  *
  */
 public class DirreqStatsFileHandler {
   private String statsDir;
   private SortedSet<String> countries;
   private File dirreqStatsFile;
   private SortedMap<String, String> observations;
   private boolean initialized;
   private boolean modified;
   public DirreqStatsFileHandler(String statsDir,
       SortedSet<String> countries) {
     this.statsDir = statsDir;
     this.countries = countries;
     this.dirreqStatsFile = new File(statsDir + "/dirreq-stats");
     this.observations = new TreeMap<String, String>();
   }
   public void initialize() throws IOException {
     if (this.initialized) {
       return;
     }
     if (this.dirreqStatsFile.exists()) {
       System.out.print("Reading file " + statsDir + "/dirreq-stats... ");
       BufferedReader br = new BufferedReader(new FileReader(
           this.dirreqStatsFile));
       String line = br.readLine();
       if (line != null) {
         String[] headers = line.split(",");
         for (int i = 2; i < headers.length - 1; i++) {
           this.countries.add(headers[i]);
         }
         while ((line = br.readLine()) != null) {
           String[] readData = line.split(",");
           String dirNickname = readData[0];
           String date = readData[1];
           Map<String, String> obs = new HashMap<String, String>();
           for (int i = 2; i < readData.length - 1; i++) {
             obs.put(headers[i], readData[i]);
           }
           String share = readData[readData.length - 1];
           this.addObs(dirNickname, date, obs, share);
         }
       }
       System.out.println("done");
       br.close();
     }
    this.initialized = true;
   }
   public void addObs(String dirNickname, String date,
       Map<String, String> obs, String share) throws IOException {
     if (!this.initialized) {
       throw new RuntimeException("Not initialized!");
     }
     String obsKey = dirNickname + "," + date;
     StringBuilder sb = new StringBuilder(obsKey);
     for (String c : this.countries) {
       sb.append("," + (obs.containsKey(c) ? obs.get(c) : "0"));
     }
     sb.append("," + share);
     this.observations.put(obsKey, sb.toString());
     this.modified = true;
   }
   public void writeFile() {
     if (!this.modified) {
       return;
     }
     try {
       if (!this.observations.isEmpty()) {
         System.out.print("Writing file " + this.statsDir
             + "/dirreq-stats... ");
         new File(this.statsDir).mkdirs();
         BufferedWriter bwDirreqStats = new BufferedWriter(
             new FileWriter(this.dirreqStatsFile));
         bwDirreqStats.append("directory,date");
         for (String country : this.countries) {
           bwDirreqStats.append("," + country);
         }
         bwDirreqStats.append(",share\n");
         for (String observation : this.observations.values()) {
           bwDirreqStats.append(observation + "\n");
         }
         bwDirreqStats.close();
         System.out.println("done");
       }
     } catch (IOException e) {
       System.out.println("failed");
     }
   }
 }
 
