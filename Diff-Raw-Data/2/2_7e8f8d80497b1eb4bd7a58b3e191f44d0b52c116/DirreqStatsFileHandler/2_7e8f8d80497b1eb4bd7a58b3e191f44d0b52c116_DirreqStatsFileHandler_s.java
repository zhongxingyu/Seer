 /* Copyright 2010 The Tor Project
  * See LICENSE for licensing information */
 package org.torproject.ernie.db;
 
 import java.io.*;
 import java.sql.*;
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
    * Two-letter country codes of known countries.
    */
   private SortedSet<String> countries;
 
   /**
    * Results file containing v3 directory requests by country.
    */
   private File dirreqStatsFile;
 
   /**
    * Directory requests by directory and date. Map keys are directory and
    * date written as "directory,statsend,seconds", map values are
    * country-user maps.
    */
   private SortedMap<String, Map<String, String>> dirreqs;
 
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
 
   /* Database connection string. */
   private String connectionURL = null;
 
   /* Format for parsing dirreq-stats-end timestamps. */
   private SimpleDateFormat dateTimeFormat = null;
 
   /**
    * Initializes this class, including reading in previous results from
    * <code>stats/dirreq-stats</code>.
    */
   public DirreqStatsFileHandler(String connectionURL) {
 
     /* Initialize set of known countries. */
     this.countries = new TreeSet<String>();
     this.countries.add("zy");
 
     /* Initialize local data structure to hold observations received from
      * RelayDescriptorParser. */
     this.dirreqs = new TreeMap<String, Map<String, String>>();
 
     /* Initialize file name for observations file. */
     this.dirreqStatsFile = new File("stats/dirreq-stats");
 
     /* Initialize database connection string. */
     this.connectionURL = connectionURL;
 
     /* Initialize format to parse dirreq-stats-end lines. */
     this.dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
     this.dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
 
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
           if (!line.startsWith("directory,statsend,seconds,")) {
             this.logger.warning("Incorrect first line '" + line + "' in "
                 + this.dirreqStatsFile.getAbsolutePath() + "! This line "
                 + "should contain headers! Aborting to read in this "
                 + "file!");
           } else {
             String[] headers = line.split(",");
             for (int i = 3; i < headers.length; i++) {
               if (!headers[i].equals("all")) {
                 this.countries.add(headers[i]);
               }
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
               String statsEnd = parts[1];
               long seconds = Long.parseLong(parts[2]);
               Map<String, String> obs = new HashMap<String, String>();
               for (int i = 3; i < parts.length; i++) {
                 if (parts[i].equals("NA")) {
                   continue;
                 }
                 if (headers[i].equals("all")) {
                   obs.put("zy", parts[i]);
                 } else {
                   obs.put(headers[i], parts[i]);
                 }
               }
               this.addObs(directory, statsEnd, seconds, obs);
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
    * seen on a directory at a given date.
    */
   public void addObs(String directory, String statsEnd, long seconds,
       Map<String, String> obs) {
     for (String country : obs.keySet()) {
       this.countries.add(country);
     }
     String key = directory + "," + statsEnd + "," + seconds;
     if (!this.dirreqs.containsKey(key)) {
       this.logger.finer("Adding new directory request numbers: " + key);
       this.dirreqs.put(key, obs);
       this.dirreqsModified = true;
       this.addedResults++;
     } else {
       this.logger.fine("The directory request numbers we were just "
           + "given for " + key + " may be different from what we learned "
           + "before. Overwriting!");
       this.dirreqs.put(key, obs);
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
         bw.append("directory,statsend,seconds");
         for (String country : this.countries) {
           if (country.equals("zy")) {
             bw.append(",all");
           } else {
             bw.append("," + country);
           }
         }
         bw.append("\n");
         /* Write observations. */
         for (Map.Entry<String, Map<String, String>> e :
             this.dirreqs.entrySet()) {
           String key = e.getKey();
           Map<String, String> obs = e.getValue();
           StringBuilder sb = new StringBuilder(key);
           for (String c : this.countries) {
             sb.append("," + (obs.containsKey(c) ? obs.get(c) : "NA"));
           }
           String line = sb.toString();
           bw.append(line + "\n");
         }
         bw.close();
         this.logger.fine("Finished writing file "
             + this.dirreqStatsFile.getAbsolutePath() + ".");
       } catch (IOException e) {
         this.logger.log(Level.WARNING, "Failed to write file "
             + this.dirreqStatsFile.getAbsolutePath() + "!", e);
       }
     } else {
       this.logger.fine("Not writing file "
           + this.dirreqStatsFile.getAbsolutePath() + ", because "
           + "nothing has changed.");
     }
 
     /* Add directory requests by country to database. */
     if (connectionURL != null) {
       try {
         List<String> countryList = new ArrayList<String>();
         for (String c : this.countries) {
           countryList.add(c);
         }
         Map<String, String> insertRows = new HashMap<String, String>(),
             updateRows = new HashMap<String, String>();
         for (Map.Entry<String, Map<String, String>> e :
             this.dirreqs.entrySet()) {
           String[] parts = e.getKey().split(",");
           String source = parts[0];
           String statsEnd = parts[1];
           String seconds = parts[2];
           Map<String, String> obs = e.getValue();
           int i = 0;
           for (String country : this.countries) {
             if (obs.containsKey(country)) {
               String key = source + "," + statsEnd + "," + seconds + ","
                   + country;
               String requests = "" + obs.get(country);
               insertRows.put(key, requests);
             }
           }
         }
         Connection conn = DriverManager.getConnection(connectionURL);
         conn.setAutoCommit(false);
         Statement statement = conn.createStatement();
         ResultSet rs = statement.executeQuery(
             "SELECT source, statsend, seconds, country, requests "
             + "FROM dirreq_stats");
         while (rs.next()) {
           String source = rs.getString(1);
           String statsEnd = this.dateTimeFormat.format(
               rs.getTimestamp(2).getTime());
           long seconds = rs.getLong(3);
           String country = rs.getString(4);
           String key = source + "," + statsEnd + "," + seconds + ","
               + country;
           if (insertRows.containsKey(key)) {
             String insertRow = insertRows.remove(key);
            long oldUsers = rs.getLong(4);
             long newUsers = Long.parseLong(insertRow.split(",")[0]);
             if (oldUsers != newUsers) {
               updateRows.put(key, insertRow);
             }
           }
         }
         rs.close();
         PreparedStatement psU = conn.prepareStatement(
             "UPDATE dirreq_stats SET requests = ? "
             + "WHERE source = ? AND statsend = ? AND seconds = ? "
             + "AND country = ?");
         Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
         for (Map.Entry<String, String> e : updateRows.entrySet()) {
           String[] keyParts = e.getKey().split(",");
           String source = keyParts[0];
           Timestamp statsEnd = new Timestamp(this.dateTimeFormat.parse(
               keyParts[1]).getTime());
           long seconds = Long.parseLong(keyParts[2]);
           String country = keyParts[3];
           long requests = Long.parseLong(e.getValue());
           psU.clearParameters();
           psU.setLong(1, requests);
           psU.setString(2, source);
           psU.setTimestamp(3, statsEnd, cal);
           psU.setLong(4, seconds);
           psU.setString(5, country);
           psU.executeUpdate();
         }
         PreparedStatement psI = conn.prepareStatement(
             "INSERT INTO dirreq_stats (requests, source, statsend, "
             + "seconds, country) VALUES (?, ?, ?, ?, ?)");
         for (Map.Entry<String, String> e : insertRows.entrySet()) {
           String[] keyParts = e.getKey().split(",");
           String source = keyParts[0];
           Timestamp statsEnd = new Timestamp(this.dateTimeFormat.parse(
               keyParts[1]).getTime());
           long seconds = Long.parseLong(keyParts[2]);
           String country = keyParts[3];
           long requests = Long.parseLong(e.getValue());
           psI.clearParameters();
           psI.setLong(1, requests);
           psI.setString(2, source);
           psI.setTimestamp(3, statsEnd, cal);
           psI.setLong(4, seconds);
           psI.setString(5, country);
           psI.executeUpdate();
         }
         conn.commit();
         conn.close();
       } catch (SQLException e) {
         logger.log(Level.WARNING, "Failed to add directory requests by "
             + "country to database.", e);
       } catch (ParseException e) {
         logger.log(Level.WARNING, "Failed to add directory requests by "
             + "country to database.", e);
       }
     }
 
     /* Set modification flag to false again. */
     this.dirreqsModified = false;
 
     /* Write stats. */
     StringBuilder dumpStats = new StringBuilder("Finished writing "
         + "statistics on directory requests by country.\nAdded "
         + this.addedResults + " new observations in this execution.\n"
         + "Last known observations by directory are:");
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
 
