 /* Copyright 2010 The Tor Project
  * See LICENSE for licensing information */
 package org.torproject.ernie.db;
 
 import java.io.*;
 import java.sql.*;
 import java.text.*;
 import java.util.*;
 import java.util.logging.*;
 import org.postgresql.util.*;
 
 /**
  * Parse directory data.
  */
 
 public final class RelayDescriptorDatabaseImporter {
 
   /**
    * How many records to commit with each database transaction.
    */
   private final long autoCommitCount = 500;
 
   /**
    * Keep track of the number of records committed before each transaction
    */
   private int rdsCount = 0;
   private int resCount = 0;
   private int rrsCount = 0;
 
   /**
    * Relay descriptor database connection.
    */
   private Connection conn;
 
   /**
    * Prepared statement to check whether a given network status consensus
    * entry has been imported into the database before.
    */
   private PreparedStatement psRs;
 
   /**
    * Prepared statement to check whether a given extra-info descriptor has
    * been imported into the database before.
    */
   private PreparedStatement psEs;
 
   /**
    * Prepared statement to check whether a given server descriptor has
    * been imported into the database before.
    */
   private PreparedStatement psDs;
 
   /**
    * Prepared statement to insert a network status consensus entry into
    * the database.
    */
   private PreparedStatement psR;
 
   /**
    * Prepared statement to insert a server descriptor into the database.
    */
   private PreparedStatement psD;
 
   /**
    * Prepared statement to insert an extra-info descriptor into the
    * database.
    */
   private PreparedStatement psE;
 
   /**
    * Logger for this class.
    */
   private Logger logger;
 
   private BufferedWriter statusentryOut;
   private BufferedWriter descriptorOut;
   private BufferedWriter extrainfoOut;
 
   /**
    * Initialize database importer by connecting to the database and
    * preparing statements.
    */
   public RelayDescriptorDatabaseImporter(String connectionURL,
       String rawFilesDirectory) {
 
     /* Initialize logger. */
     this.logger = Logger.getLogger(
         RelayDescriptorDatabaseImporter.class.getName());
 
     if (connectionURL != null) {
       try {
         /* Connect to database. */
         this.conn = DriverManager.getConnection(connectionURL);
 
         /* Turn autocommit off */
         this.conn.setAutoCommit(false);
 
         /* Prepare statements. */
         this.psRs = conn.prepareStatement("SELECT COUNT(*) "
             + "FROM statusentry WHERE validafter = ? AND descriptor = ?");
         this.psDs = conn.prepareStatement("SELECT COUNT(*) "
             + "FROM descriptor WHERE descriptor = ?");
         this.psEs = conn.prepareStatement("SELECT COUNT(*) "
             + "FROM extrainfo WHERE extrainfo = ?");
         this.psR = conn.prepareStatement("INSERT INTO statusentry "
             + "(validafter, nickname, fingerprint, descriptor, "
             + "published, address, orport, dirport, isauthority, "
             + "isbadexit, isbaddirectory, isexit, isfast, isguard, "
             + "ishsdir, isnamed, isstable, isrunning, isunnamed, "
             + "isvalid, isv2dir, isv3dir, version, bandwidth, ports, "
             + "rawdesc) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
             + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
         this.psD = conn.prepareStatement("INSERT INTO descriptor "
             + "(descriptor, nickname, address, orport, dirport, "
             + "bandwidthavg, bandwidthburst, bandwidthobserved, "
             + "platform, published, uptime, extrainfo, rawdesc) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
         this.psE = conn.prepareStatement("INSERT INTO extrainfo "
             + "(extrainfo, nickname, fingerprint, published, rawdesc) "
             + "VALUES (?, ?, ?, ?, ?)");
       } catch (SQLException e) {
         this.logger.log(Level.WARNING, "Could not connect to database or "
             + "prepare statements.", e);
       }
     }
 
     if (rawFilesDirectory != null) {
       try {
         new File(rawFilesDirectory).mkdirs();
         this.statusentryOut = new BufferedWriter(new FileWriter(
             rawFilesDirectory + "/statusentry.sql"));
         this.descriptorOut = new BufferedWriter(new FileWriter(
             rawFilesDirectory + "/descriptor.sql"));
         this.extrainfoOut = new BufferedWriter(new FileWriter(
             rawFilesDirectory + "/extrainfo.sql"));
       } catch (IOException e) {
         this.logger.log(Level.WARNING, "Could not open raw database "
             + "import files.", e);
       }
     }
   }
 
   /**
    * Insert network status consensus entry into database.
    */
   public void addStatusEntry(long validAfter, String nickname,
       String fingerprint, String descriptor, long published,
       String address, long orPort, long dirPort,
       SortedSet<String> flags, String version, long bandwidth,
       String ports, byte[] rawDescriptor) {
     try {
       if (this.psRs != null && this.psR != null) {
         Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
         Timestamp validAfterTimestamp = new Timestamp(validAfter);
         this.psRs.setTimestamp(1, validAfterTimestamp, cal);
         this.psRs.setString(2, descriptor);
         ResultSet rs = psRs.executeQuery();
         rs.next();
         if (rs.getInt(1) == 0) {
           this.psR.clearParameters();
           this.psR.setTimestamp(1, validAfterTimestamp, cal);
           this.psR.setString(2, nickname);
           this.psR.setString(3, fingerprint);
           this.psR.setString(4, descriptor);
           this.psR.setTimestamp(5, new Timestamp(published), cal);
           this.psR.setString(6, address);
           this.psR.setLong(7, orPort);
           this.psR.setLong(8, dirPort);
           this.psR.setBoolean(9, flags.contains("Authority"));
           this.psR.setBoolean(10, flags.contains("BadExit"));
           this.psR.setBoolean(11, flags.contains("BadDirectory"));
           this.psR.setBoolean(12, flags.contains("Exit"));
           this.psR.setBoolean(13, flags.contains("Fast"));
           this.psR.setBoolean(14, flags.contains("Guard"));
           this.psR.setBoolean(15, flags.contains("HSDir"));
           this.psR.setBoolean(16, flags.contains("Named"));
           this.psR.setBoolean(17, flags.contains("Stable"));
           this.psR.setBoolean(18, flags.contains("Running"));
           this.psR.setBoolean(19, flags.contains("Unnamed"));
           this.psR.setBoolean(20, flags.contains("Valid"));
           this.psR.setBoolean(21, flags.contains("V2Dir"));
           this.psR.setBoolean(22, flags.contains("V3Dir"));
           this.psR.setString(23, version);
           this.psR.setLong(24, bandwidth);
           this.psR.setString(25, ports);
           this.psR.setBytes(26, rawDescriptor);
           this.psR.executeUpdate();
           rrsCount++;
           if (rrsCount % autoCommitCount == 0)  {
             this.conn.commit();
             rrsCount = 0;
           }
         }
       }
       if (this.statusentryOut != null) {
         SimpleDateFormat dateTimeFormat =
              new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
         this.statusentryOut.write(
             dateTimeFormat.format(validAfter) + "\t" + nickname
             + "\t" + fingerprint.toLowerCase() + "\t"
             + descriptor.toLowerCase() + "\t"
             + dateTimeFormat.format(published) + "\t" + address + "\t"
             + orPort + "\t" + dirPort + "\t"
             + (flags.contains("Authority") ? "t" : "f") + "\t"
             + (flags.contains("BadExit") ? "t" : "f") + "\t"
             + (flags.contains("BadDirectory") ? "t" : "f") + "\t"
             + (flags.contains("Exit") ? "t" : "f") + "\t"
             + (flags.contains("Fast") ? "t" : "f") + "\t"
             + (flags.contains("Guard") ? "t" : "f") + "\t"
             + (flags.contains("HSDir") ? "t" : "f") + "\t"
             + (flags.contains("Named") ? "t" : "f") + "\t"
             + (flags.contains("Stable") ? "t" : "f") + "\t"
             + (flags.contains("Running") ? "t" : "f") + "\t"
             + (flags.contains("Unnamed") ? "t" : "f") + "\t"
             + (flags.contains("Valid") ? "t" : "f") + "\t"
             + (flags.contains("V2Dir") ? "t" : "f") + "\t"
             + (flags.contains("V3Dir") ? "t" : "f") + "\t"
             + (version != null ? version : "\\N") + "\t"
             + (bandwidth >= 0 ? bandwidth : "\\N") + "\t"
             + (ports != null ? ports : "\\N") + "\t");
         this.statusentryOut.write(PGbytea.toPGString(rawDescriptor).
             replaceAll("\\\\\\\\", "\\\\\\\\\\\\\\\\") + "\n");
       }
     } catch (SQLException e) {
       this.logger.log(Level.WARNING, "Could not add network status "
           + "consensus entry.", e);
     } catch (IOException e) {
       this.logger.log(Level.WARNING, "Could not write network status "
           + "consensus entry to raw database import file.", e);
     }
   }
 
   /**
    * Insert server descriptor into database.
    */
   public void addServerDescriptor(String descriptor, String nickname,
       String address, int orPort, int dirPort, long bandwidthAvg,
       long bandwidthBurst, long bandwidthObserved, String platform,
       long published, long uptime, String extraInfoDigest,
       byte[] rawDescriptor) {
     try {
       if (this.psDs != null && this.psD != null) {
         Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
         this.psDs.setString(1, descriptor);
         ResultSet rs = psDs.executeQuery();
         rs.next();
         if (rs.getInt(1) == 0) {
           this.psD.clearParameters();
           this.psD.setString(1, descriptor);
           this.psD.setString(2, nickname);
           this.psD.setString(3, address);
           this.psD.setInt(4, orPort);
           this.psD.setInt(5, dirPort);
           this.psD.setLong(6, bandwidthAvg);
           this.psD.setLong(7, bandwidthBurst);
           this.psD.setLong(8, bandwidthObserved);
           this.psD.setString(9, new String(platform.getBytes(),
               "US-ASCII"));
           this.psD.setTimestamp(10, new Timestamp(published), cal);
           this.psD.setLong(11, uptime);
           this.psD.setString(12, extraInfoDigest);
           this.psD.setBytes(13, rawDescriptor);
           this.psD.executeUpdate();
           rdsCount++;
           if (rdsCount % autoCommitCount == 0)  {
             this.conn.commit();
             rdsCount = 0;
           }
         }
       }
       if (this.descriptorOut != null) {
         SimpleDateFormat dateTimeFormat =
              new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
         this.descriptorOut.write(descriptor.toLowerCase() + "\t"
             + nickname + "\t" + address + "\t" + orPort + "\t" + dirPort
             + "\t" + bandwidthAvg + "\t" + bandwidthBurst + "\t"
             + bandwidthObserved + "\t"
             + (platform != null && platform.length() > 0
             ? new String(platform.getBytes(), "US-ASCII") : "\\N") + "\t"
             + dateTimeFormat.format(published) + "\t"
             + (uptime >= 0 ? uptime : "\\N") + "\t"
             + (extraInfoDigest != null ? extraInfoDigest : "\\N") + "\t");
         this.descriptorOut.write(PGbytea.toPGString(rawDescriptor).
             replaceAll("\\\\\\\\", "\\\\\\\\\\\\\\\\") + "\n");
       }
     } catch (UnsupportedEncodingException e) {
       this.logger.log(Level.WARNING, "Could not add server descriptor.",
           e);
     } catch (SQLException e) {
       this.logger.log(Level.WARNING, "Could not add server descriptor.",
           e);
     } catch (IOException e) {
       this.logger.log(Level.WARNING, "Could not write server descriptor "
           + "to raw database import file.", e);
     }
   }
 
   /**
    * Insert extra-info descriptor into database.
    */
   public void addExtraInfoDescriptor(String extraInfoDigest,
       String nickname, String fingerprint, long published,
       byte[] rawDescriptor) {
     try {
       if (this.psEs != null && this.psE != null) {
         Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
         this.psEs.setString(1, extraInfoDigest);
         ResultSet rs = psEs.executeQuery();
         rs.next();
         if (rs.getInt(1) == 0) {
           this.psE.clearParameters();
           this.psE.setString(1, extraInfoDigest);
           this.psE.setString(2, nickname);
           this.psE.setString(3, fingerprint);
           this.psE.setTimestamp(4, new Timestamp(published), cal);
           this.psE.setBytes(5, rawDescriptor);
           this.psE.executeUpdate();
           resCount++;
           if (resCount % autoCommitCount == 0)  {
             this.conn.commit();
             resCount = 0;
           }
         }
       }
       if (this.extrainfoOut != null) {
         SimpleDateFormat dateTimeFormat =
              new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
         this.extrainfoOut.write(extraInfoDigest.toLowerCase() + "\t"
             + nickname + "\t" + fingerprint.toLowerCase() + "\t"
             + dateTimeFormat.format(published) + "\t");
         this.extrainfoOut.write(PGbytea.toPGString(rawDescriptor).
             replaceAll("\\\\\\\\", "\\\\\\\\\\\\\\\\") + "\n");
       }
     } catch (SQLException e) {
       this.logger.log(Level.WARNING, "Could not add extra-info "
           + "descriptor.", e);
     } catch (IOException e) {
       this.logger.log(Level.WARNING, "Could not write extra-info "
           + "descriptor to raw database import file.", e);
     }
   }
 
   /**
    * Close the relay descriptor database connection.
    */
   public void closeConnection() {
     /* commit any stragglers before closing */
     if (this.conn != null) {
       try {
         this.conn.commit();
       } catch (SQLException e)  {
         this.logger.log(Level.WARNING, "Could not commit final records to "
             + "database", e);
       }
       try {
         this.conn.close();
       } catch (SQLException e) {
         this.logger.log(Level.WARNING, "Could not close database "
             + "connection.", e);
       }
     }
     /* Close raw import files. */
     if (this.statusentryOut != null) {
       try {
         this.statusentryOut.close();
       } catch (IOException e) {
         this.logger.log(Level.WARNING, "Could not close raw database "
             + "import file.", e);
       }
     }
     if (this.descriptorOut != null) {
       try {
         this.descriptorOut.close();
       } catch (IOException e) {
         this.logger.log(Level.WARNING, "Could not close raw database "
             + "import file.", e);
       }
     }
     if (this.extrainfoOut != null) {
       try {
         this.extrainfoOut.close();
       } catch (IOException e) {
         this.logger.log(Level.WARNING, "Could not close raw database "
             + "import file.", e);
       }
     }
   }
 }
 
