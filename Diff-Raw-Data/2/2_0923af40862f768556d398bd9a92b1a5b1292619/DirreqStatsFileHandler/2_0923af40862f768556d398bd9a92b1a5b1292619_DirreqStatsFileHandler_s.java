 import java.io.*;
 import java.text.*;
 import java.util.*;
 import java.util.logging.*;
 
 /**
  * Extracts statistics on v3 directory requests by country from extra-info
  * descriptors and writes them to a CSV file that is easily parsable by R.
  * Parse results come from <code>RelayDescriptorParser</code> and are
  * written to <code>stats/dirreq-stats</code>.
  */
 public class DirreqStatsFileHandler {
 
   /**
    * Two-letter country codes of countries that we care about.
    */
   private SortedSet<String> countries;
 
   /**
    * Results file containing v3 directory requests by country.
    */
   private File dirreqStatsFile;
 
   /**
    * Directory requests by directory and date. Map keys are directory and
    * date written as "directory,date", map values are lines as read from
    * <code>stats/dirreq-stats</code>.
    */
   private SortedMap<String, String> dirreqs;
 
   /**
    * Modification flag for directory requests stored in memory. This flag
    * is used to decide whether the contents of <code>dirreqs</code> need
    * to be written to disk during <code>writeFile</code>.
    */
   private boolean dirreqsModified;
 
   /**
    * Logger for this class.
    */
   private Logger logger;
 
   private int addedResults = 0;
 
   /**
    * Initializes this class, including reading in previous results from
    * <code>stats/dirreq-stats</code>.
    */
   public DirreqStatsFileHandler(SortedSet<String> countries) {
 
     /* Memorize the set of countries we care about. */
     this.countries = countries;
 
     /* Initialize local data structure to hold observations received from
      * RelayDescriptorParser. */
     this.dirreqs = new TreeMap<String, String>();
 
     /* Initialize file name for observations file. */
     this.dirreqStatsFile = new File("stats/dirreq-stats");
 
     /* Initialize logger. */
     this.logger = Logger.getLogger(
         DirreqStatsFileHandler.class.getName());
 
     /* Read in previously stored results. */
     if (this.dirreqStatsFile.exists()) {
       try {
         this.logger.fine("Reading file "
             + this.dirreqStatsFile.getAbsolutePath() + "...");
         BufferedReader br = new BufferedReader(new FileReader(
             this.dirreqStatsFile));
         String line = br.readLine();
         if (line != null) {
           /* The first line should contain headers that we need to parse
            * in order to learn what countries we were interested in when
            * writing this file. */
           if (!line.startsWith("directory,date,")) {
             this.logger.warning("Incorrect first line '" + line + "' in "
                 + this.dirreqStatsFile.getAbsolutePath() + "! This line "
                 + "should contain headers! Aborting to read in this "
                 + "file!");
           } else {
             String[] headers = line.split(",");
             for (int i = 2; i < headers.length - 1; i++) {
               this.countries.add(headers[i]);
             }
             /* Read in the rest of the file. */
             while ((line = br.readLine()) != null) {
               String[] parts = line.split(",");
               if (parts.length != headers.length) {
                 this.logger.warning("Corrupt line '" + line + "' in file "
                     + this.dirreqStatsFile.getAbsolutePath() + "! This "
                     + "line has either fewer or more columns than the "
                     + "file has column headers! Aborting to read this "
                     + "file!");
                 break;
               }
               String directory = parts[0];
               String date = parts[1];
               /* If the share column contains NA, all the other columns do.
                * We only want to read in non-NA lines here. */
               if (!parts[parts.length - 1].equals("NA")) {
                 Map<String, String> obs = new HashMap<String, String>();
                 for (int i = 2; i < parts.length - 1; i++) {
                   obs.put(headers[i], parts[i]);
                 }
                 String share = parts[parts.length - 1];
                 this.addObs(directory, date, obs, share);
               }
             }
           }
         }
         br.close();
         this.logger.fine("Finished reading file "
             + this.dirreqStatsFile.getAbsolutePath() + ".");
       } catch (IOException e) {
         this.logger.log(Level.WARNING, "Failed to read file "
             + this.dirreqStatsFile.getAbsolutePath() + "!", e);
       }
     }
 
     /* Set modification flag to false and counter for stats to zero. */
     this.dirreqsModified = false;
     this.addedResults = 0;
   }
 
   /**
    * Adds observations on the number of directory requests by country as
    * seen on a directory at a given data that expected to see the given
    * share of all directory requests in the network.
    */
   public void addObs(String directory, String date,
       Map<String, String> obs, String share) {
     String key = directory + "," + date;
     StringBuilder sb = new StringBuilder(key);
     for (String c : this.countries) {
       sb.append("," + (obs.containsKey(c) ? obs.get(c) : "0"));
     }
     sb.append("," + share);
     String value = sb.toString();
     if (!this.dirreqs.containsKey(key)) {
       this.logger.finer("Adding new directory request numbers: " + value);
       this.dirreqs.put(key, value);
       this.dirreqsModified = true;
       this.addedResults++;
     } else if (value.compareTo(this.dirreqs.get(key)) > 0) {
       this.logger.warning("The directory request numbers we were just "
           + "given (" + value + ") are different from what we learned "
           + "before (" + this.dirreqs.get(key) + "! Overwriting!");
       this.dirreqs.put(key, value);
       this.dirreqsModified = true;
     }   
   }
 
   /**
    * Writes the v3 directory request numbers from memory to
    * <code>stats/dirreq-stats</code> if they have changed.
    */
   public void writeFile() {
 
     /* Only write file if we learned something new. */
     if (this.dirreqsModified) {
       try {
         this.logger.fine("Writing file "
             + this.dirreqStatsFile.getAbsolutePath() + "...");
         this.dirreqStatsFile.getParentFile().mkdirs();
         BufferedWriter bw = new BufferedWriter(new FileWriter(
             this.dirreqStatsFile));
         /* Write header. */
         bw.append("directory,date");
         for (String country : this.countries) {
           bw.append("," + country);
         }
         bw.append(",share\n");
         /* Memorize last written date and directory to fill missing dates
          * with NA's. */
         long lastDateMillis = 0L;
         String lastDirectory = null;
         SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
         dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
         for (String line : this.dirreqs.values()) {
           /* Fill missing dates with NA's. */
           String[] parts = line.split(",");
           String currentDirectory = parts[0];
           long currentDateMillis = dateFormat.parse(parts[1]).getTime();
           while (currentDirectory.equals(lastDirectory) &&
               currentDateMillis - 24L * 60L * 60L * 1000L
               > lastDateMillis) {
             lastDateMillis += 24L * 60L * 60L * 1000L;
             bw.append(currentDirectory + ","
                 + dateFormat.format(new Date(lastDateMillis)));
             for (int i = 0; i < this.countries.size(); i++) {
               bw.append(",NA");
             }
             bw.append(",NA\n");
           }
           lastDateMillis = currentDateMillis;
           lastDirectory = currentDirectory;
           /* Write current observation. */
           bw.append(line + "\n");
         }
         bw.close();
         this.logger.fine("Finished writing file "
             + this.dirreqStatsFile.getAbsolutePath() + ".");
       } catch (IOException e) {
         this.logger.log(Level.WARNING, "Failed to write file "
             + this.dirreqStatsFile.getAbsolutePath() + "!", e);
       } catch (ParseException e) {
         this.logger.log(Level.WARNING, "Failed to write file "
             + this.dirreqStatsFile.getAbsolutePath() + "!", e);
       }
     } else {
       this.logger.fine("Not writing file "
           + this.dirreqStatsFile.getAbsolutePath() + ", because "
           + "nothing has changed.");
     }
 
     /* Set modification flag to false again. */
     this.dirreqsModified = false;
 
     /* Write stats. */
     StringBuilder dumpStats = new StringBuilder("Finished writing "
         + "statistics on directory requests by country.\nAdded "
         + this.addedResults + " new observations in this execution.\n"
        + "Last known obserations by directory are:");
     String lastDir = null;
     String lastDate = null;
     for (String line : this.dirreqs.keySet()) {
       String[] parts = line.split(",");
       if (lastDir == null) {
         lastDir = parts[0];
       } else if (!parts[0].equals(lastDir)) {
         dumpStats.append("\n" + lastDir.substring(0, 8) + " " + lastDate);
         lastDir = parts[0];
       }
       lastDate = parts[1];
     }
     if (lastDir != null) {
       dumpStats.append("\n" + lastDir.substring(0, 8) + " " + lastDate);
     }
     logger.info(dumpStats.toString());
   }
 }
 
