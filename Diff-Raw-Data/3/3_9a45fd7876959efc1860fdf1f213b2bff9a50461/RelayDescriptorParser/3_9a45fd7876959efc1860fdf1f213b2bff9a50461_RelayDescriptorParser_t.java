 package org.torproject.ernie.db;
 
 import java.io.*;
 import java.text.*;
 import java.util.*;
 import java.util.logging.*;
 import org.apache.commons.codec.digest.*;
 import org.apache.commons.codec.binary.*;
 
 /**
  * Parses relay descriptors including network status consensuses and
  * votes, server and extra-info descriptors, and passes the results to the
  * stats handlers, to the archive writer, or to the relay descriptor
  * downloader.
  */
 public class RelayDescriptorParser {
 
   /**
    * Stats file handler that accepts parse results for directory request
    * statistics.
    */
   private DirreqStatsFileHandler dsfh;
 
   /**
    * Stats file handler that accepts parse results for consensus
    * statistics.
    */
   private ConsensusStatsFileHandler csfh;
 
   /**
    * Stats file handler that accepts parse results for bridge statistics.
    */
   private BridgeStatsFileHandler bsfh;
 
   /**
    * Stats file handler that accepts parse results for server descriptor
    * statistics.
    */
   private ServerDescriptorStatsFileHandler sdsfh;
 
   /**
    * File writer that writes descriptor contents to files in a
    * directory-archive directory structure.
    */
   private ArchiveWriter aw;
 
   /**
    * Missing descriptor downloader that uses the parse results to learn
    * which descriptors we are missing and want to download.
    */
   private RelayDescriptorDownloader rdd;
 
   /**
    * Relay descriptor database importer that stores relay descriptor
    * contents for later evaluation.
    */
   private RelayDescriptorDatabaseImporter rddi;
 
   private ConsensusHealthChecker chc;
 
   /**
    * Countries that we care about for directory request and bridge
    * statistics.
    */
   private SortedSet<String> countries;
 
   /**
    * Directories that we care about for directory request statistics.
    */
   private SortedSet<String> directories;
 
   /**
    * Logger for this class.
    */
   private Logger logger;
 
   /**
    * Initializes this class.
    */
   public RelayDescriptorParser(ConsensusStatsFileHandler csfh,
       BridgeStatsFileHandler bsfh, DirreqStatsFileHandler dsfh,
       ServerDescriptorStatsFileHandler sdsfh, ArchiveWriter aw,
       RelayDescriptorDatabaseImporter rddi, ConsensusHealthChecker chc,
       SortedSet<String> countries, SortedSet<String> directories) {
     this.csfh = csfh;
     this.bsfh = bsfh;
     this.dsfh = dsfh;
     this.sdsfh = sdsfh;
     this.aw = aw;
     this.rddi = rddi;
     this.chc = chc;
     this.countries = countries;
     this.directories = directories;
 
     /* Initialize logger. */
     this.logger = Logger.getLogger(RelayDescriptorParser.class.getName());
   }
 
   public void setRelayDescriptorDownloader(
       RelayDescriptorDownloader rdd) {
     this.rdd = rdd;
   }
 
   public void parse(byte[] data) {
     try {
       /* Convert descriptor to ASCII for parsing. This means we'll lose
        * the non-ASCII chars, but we don't care about them for parsing
        * anyway. */
       BufferedReader br = new BufferedReader(new StringReader(new String(
           data, "US-ASCII")));
       String line = br.readLine();
       if (line == null) {
         this.logger.fine("We were given an empty descriptor for "
             + "parsing. Ignoring.");
         return;
       }
       SimpleDateFormat parseFormat =
           new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
       parseFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
       if (line.equals("network-status-version 3")) {
         // TODO when parsing the current consensus, check the fresh-until
         // time to see when we switch from hourly to half-hourly
         // consensuses
         boolean isConsensus = true;
         int exit = 0, fast = 0, guard = 0, running = 0, stable = 0;
         String validAfterTime = null, descriptorIdentity = null,
             serverDesc = null;
         StringBuilder descriptorIdentities = new StringBuilder();
         String fingerprint = null, dirSource = null;
         long validAfter = -1L;
         SortedSet<String> dirSources = new TreeSet<String>();
         SortedSet<String> serverDescriptors = new TreeSet<String>();
         SortedSet<String> hashedRelayIdentities = new TreeSet<String>();
         while ((line = br.readLine()) != null) {
           if (line.equals("vote-status vote")) {
             isConsensus = false;
           } else if (line.startsWith("valid-after ")) {
             validAfterTime = line.substring("valid-after ".length());
             validAfter = parseFormat.parse(validAfterTime).getTime();
           } else if (line.startsWith("dir-source ")) {
             dirSource = line.split(" ")[2];
           } else if (line.startsWith("vote-digest ")) {
             dirSources.add(dirSource);
           } else if (line.startsWith("fingerprint ")) {
             fingerprint = line.split(" ")[1];
           } else if (line.startsWith("r ")) {
             String publishedTime = line.split(" ")[4] + " "
                 + line.split(" ")[5];
             String relayIdentity = Hex.encodeHexString(
                 Base64.decodeBase64(line.split(" ")[2] + "=")).
                 toLowerCase();
             serverDesc = Hex.encodeHexString(Base64.decodeBase64(
                 line.split(" ")[3] + "=")).toLowerCase();
             serverDescriptors.add(publishedTime + "," + relayIdentity
                 + "," + serverDesc);
             hashedRelayIdentities.add(DigestUtils.shaHex(
                Base64.decodeBase64(line.split(" ")[2] + "=")).
                toUpperCase());
             descriptorIdentity = line.split(" ")[3];
           } else if (line.startsWith("s ")) {
             if (line.contains(" Running")) {
               exit += line.contains(" Exit") ? 1 : 0;
               fast += line.contains(" Fast") ? 1 : 0;
               guard += line.contains(" Guard") ? 1 : 0;
               stable += line.contains(" Stable") ? 1 : 0;
               running++;
               descriptorIdentities.append("," + descriptorIdentity);
             }
             if (this.rddi != null) {
               SortedSet<String> flags = new TreeSet<String>();
               if (line.length() > 2) {
                 for (String flag : line.substring(2).split(" ")) {
                   flags.add(flag);
                 }
               }
               this.rddi.addStatusEntry(validAfter, serverDesc, flags);
             }
           }
         }
         if (isConsensus) {
           if (this.bsfh != null) {
             for (String hashedRelayIdentity : hashedRelayIdentities) {
               this.bsfh.addHashedRelay(hashedRelayIdentity);
             }    
           }
           if (this.csfh != null) {
             this.csfh.addConsensusResults(validAfterTime, exit, fast,
                 guard, running, stable);
           }
           if (this.sdsfh != null) {
             this.sdsfh.addConsensus(validAfterTime,
                 descriptorIdentities.toString().substring(1));
           }
           if (this.rdd != null) {
             this.rdd.haveParsedConsensus(validAfterTime, dirSources,
                 serverDescriptors);
           }
           if (this.aw != null) {
             this.aw.storeConsensus(data, validAfter);
           }
           if (this.chc != null) {
             this.chc.processConsensus(validAfterTime, data);
           }
         } else {
           if (this.rdd != null) {
             this.rdd.haveParsedVote(validAfterTime, fingerprint,
                 serverDescriptors);
           }
           if (this.aw != null) {
             String ascii = new String(data, "US-ASCII");
             String startToken = "network-status-version ";
             String sigToken = "directory-signature ";
             int start = ascii.indexOf(startToken);
             int sig = ascii.indexOf(sigToken);
             if (start >= 0 && sig >= 0 && sig > start) {
               sig += sigToken.length();
               byte[] forDigest = new byte[sig - start];
               System.arraycopy(data, start, forDigest, 0, sig - start);
               String digest = DigestUtils.shaHex(forDigest).toUpperCase();
               if (this.aw != null) {
                 this.aw.storeVote(data, validAfter, dirSource, digest);
               }
             }
           }
           if (this.chc != null) {
             this.chc.processVote(validAfterTime, dirSource, data);
           }
         }
       } else if (line.startsWith("router ")) {
         String platformLine = null, publishedLine = null,
             publishedTime = null, bandwidthLine = null,
             extraInfoDigest = null, relayIdentifier = null;
         String[] parts = line.split(" ");
         String address = parts[2];
         int orPort = Integer.parseInt(parts[3]);
         int dirPort = Integer.parseInt(parts[4]);
         long published = -1L, uptime = -1L;
         while ((line = br.readLine()) != null) {
           if (line.startsWith("platform ")) {
             platformLine = line;
           } else if (line.startsWith("published ")) {
             publishedLine = line;
             publishedTime = line.substring("published ".length());
             published = parseFormat.parse(publishedTime).getTime();
           } else if (line.startsWith("opt fingerprint") ||
               line.startsWith("fingerprint")) {
             relayIdentifier = line.substring(line.startsWith("opt ") ?
                 "opt fingerprint".length() : "fingerprint".length()).
                 replaceAll(" ", "").toLowerCase();
           } else if (line.startsWith("bandwidth ")) {
             bandwidthLine = line;
           } else if (line.startsWith("opt extra-info-digest ") ||
               line.startsWith("extra-info-digest ")) {
             extraInfoDigest = line.startsWith("opt ") ?
                 line.split(" ")[2].toLowerCase() :
                 line.split(" ")[1].toLowerCase();
           } else if (line.startsWith("uptime ")) {
             uptime = Long.parseLong(line.substring("uptime ".length()));
           }
         }
         String ascii = new String(data, "US-ASCII");
         String startToken = "router ";
         String sigToken = "\nrouter-signature\n";
         int start = ascii.indexOf(startToken);
         int sig = ascii.indexOf(sigToken) + sigToken.length();
         String digest = null, descriptorIdentity = null;
         if (start >= 0 || sig >= 0 || sig > start) {
           byte[] forDigest = new byte[sig - start];
           System.arraycopy(data, start, forDigest, 0, sig - start);
           descriptorIdentity = Base64.encodeBase64String(
               DigestUtils.sha(forDigest)).substring(0, 27);
           digest = DigestUtils.shaHex(forDigest);
         }
         if (this.aw != null && digest != null) {
           this.aw.storeServerDescriptor(data, digest, published);
         }
         if (this.rdd != null && digest != null) {
           this.rdd.haveParsedServerDescriptor(publishedTime,
               relayIdentifier, digest, extraInfoDigest);
         }
         if (this.sdsfh != null && descriptorIdentity != null) {
           this.sdsfh.addServerDescriptor(descriptorIdentity, platformLine,
               publishedLine, bandwidthLine);
         }
         if (this.rddi != null && digest != null) {
           String[] bwParts = bandwidthLine.split(" ");
           long bandwidthAvg = Long.parseLong(bwParts[1]);
           long bandwidthBurst = Long.parseLong(bwParts[2]);
           long bandwidthObserved = Long.parseLong(bwParts[3]);
           String platform = platformLine.substring("platform ".length());
           this.rddi.addServerDescriptor(digest, address, orPort, dirPort,
               bandwidthAvg, bandwidthBurst, bandwidthObserved, platform,
               published, uptime);
         }
       } else if (line.startsWith("extra-info ")) {
         String publishedTime = null, relayIdentifier = line.split(" ")[2];
         long published = -1L;
         String dir = line.split(" ")[2];
         String date = null, v3Reqs = null;
         boolean skip = false;
         while ((line = br.readLine()) != null) {
           if (line.startsWith("published ")) {
             publishedTime = line.substring("published ".length());
             published = parseFormat.parse(publishedTime).getTime();
           } else if (line.startsWith("dirreq-stats-end ")) {
             date = line.split(" ")[1];
             // trusted had very strange dirreq-v3-shares here...
             // TODO don't check that here, but in DirreqStatsFileHandler
             skip = dir.equals("8522EB98C91496E80EC238E732594D1509158E77")
                 && (date.equals("2009-09-10") ||
                     date.equals("2009-09-11"));
           } else if (line.startsWith("dirreq-v3-reqs ")
               && line.length() > "dirreq-v3-reqs ".length()) {
             v3Reqs = line.split(" ")[1];
           } else if (line.startsWith("dirreq-v3-share ")
               && v3Reqs != null && !skip) {
             Map<String, String> obs = new HashMap<String, String>();
             String[] parts = v3Reqs.split(",");
             for (String p : parts) {
               for (String c : this.countries) {
                 if (p.startsWith(c)) {
                   obs.put(c, p.substring(3));
                 }
               }
             }
             String share = line.substring("dirreq-v3-share ".length(),
                 line.length() - 1);
             if (this.dsfh != null &&
                 directories.contains(relayIdentifier)) {
               this.dsfh.addObs(dir, date, obs, share);
             }
           }
         }
         String ascii = new String(data, "US-ASCII");
         String startToken = "extra-info ";
         String sigToken = "\nrouter-signature\n";
         String digest = null;
         int start = ascii.indexOf(startToken);
         int sig = ascii.indexOf(sigToken) + sigToken.length();
         if (start >= 0 || sig >= 0 || sig > start) {
           byte[] forDigest = new byte[sig - start];
           System.arraycopy(data, start, forDigest, 0, sig - start);
           digest = DigestUtils.shaHex(forDigest);
         }
         if (this.aw != null && digest != null) {
           this.aw.storeExtraInfoDescriptor(data, digest, published);
         }
         if (this.rdd != null && digest != null) {
           this.rdd.haveParsedExtraInfoDescriptor(publishedTime,
               relayIdentifier.toLowerCase(), digest);
         }
       }
     } catch (IOException e) {
       this.logger.log(Level.WARNING, "Could not parse descriptor. "
           + "Skipping.", e);
     } catch (ParseException e) {
       this.logger.log(Level.WARNING, "Could not parse descriptor. "
           + "Skipping.", e);
     }
   }
 }
