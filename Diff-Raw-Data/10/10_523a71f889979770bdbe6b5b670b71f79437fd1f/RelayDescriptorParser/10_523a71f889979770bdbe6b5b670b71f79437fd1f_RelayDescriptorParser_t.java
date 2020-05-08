 import java.io.*;
 import java.text.*;
 import java.util.*;
 import java.util.logging.*;
 import org.apache.commons.codec.digest.*;
 import org.apache.commons.codec.binary.*;
 
 /**
  * Parse the contents of a network status consensus and pass on the
  * relevant contents to the stats file handlers.
  */
 public class RelayDescriptorParser {
   private File relayDescriptorParseHistoryFile;
   private SortedMap<String, String> lastParsedExtraInfos;
   private String lastParsedConsensus;
   private boolean relayDescriptorParseHistoryModified = false;
   private DirreqStatsFileHandler dsfh;
   private ConsensusStatsFileHandler csfh;
   private BridgeStatsFileHandler bsfh;
   private ServerDescriptorStatsFileHandler sdsfh;
   private SortedSet<String> countries;
   private SortedSet<String> directories;
   private Logger logger;
   public RelayDescriptorParser(ConsensusStatsFileHandler csfh,
       BridgeStatsFileHandler bsfh, DirreqStatsFileHandler dsfh,
       ServerDescriptorStatsFileHandler sdsfh, SortedSet<String> countries,
       SortedSet<String> directories) {
     this.relayDescriptorParseHistoryFile = new File(
         "stats/relay-descriptor-parse-history");
     this.csfh = csfh;
     this.bsfh = bsfh;
     this.dsfh = dsfh;
     this.sdsfh = sdsfh;
     this.countries = countries;
     this.directories = directories;
     this.logger = Logger.getLogger(RelayDescriptorParser.class.getName());
     this.lastParsedConsensus = null;
     this.lastParsedExtraInfos = new TreeMap<String, String>();
     if (this.relayDescriptorParseHistoryFile.exists()) {
       this.logger.fine("Reading file "
           + this.relayDescriptorParseHistoryFile.getAbsolutePath()
           + "...");
       try {
         BufferedReader br = new BufferedReader(new FileReader(
             this.relayDescriptorParseHistoryFile));
         String line = null;
         while ((line = br.readLine()) != null) {
           if (line.startsWith("consensus")) {
             this.lastParsedConsensus = line.split(",")[2];
           } else if (line.startsWith("extrainfo")) {
             this.lastParsedExtraInfos.put(line.split(",")[1],
                 line.split(",")[2]);
           }
         }
         br.close();
         this.logger.fine("Finished reading file "
             + this.relayDescriptorParseHistoryFile.getAbsolutePath()
             + ".");
       } catch (IOException e) {
         this.logger.log(Level.WARNING, "Failed reading file "
             + this.relayDescriptorParseHistoryFile.getAbsolutePath()
             + "!", e);
       }
     }
   }
   public void parse(byte[] data) throws IOException {
     BufferedReader br = new BufferedReader(new StringReader(new String(
         data, "US-ASCII")));
     String line = br.readLine();
     if (line == null) {
       this.logger.warning("Parsing empty file?");
       return;
     }
     if (line.equals("network-status-version 3")) {
       int exit = 0, fast = 0, guard = 0, running = 0, stable = 0;
       String validAfter = null, rLine = null;
       StringBuilder descriptorIdentities = new StringBuilder();
       while ((line = br.readLine()) != null) {
         if (line.startsWith("valid-after ")) {
           validAfter = line.substring("valid-after ".length());
           if (this.lastParsedConsensus == null ||
               validAfter.compareTo(this.lastParsedConsensus) > 0) {
             this.lastParsedConsensus = validAfter;
             relayDescriptorParseHistoryModified = true;
           }
         } else if (line.equals("vote-status vote")) {
           return;
        } else if (line.startsWith("r ")) {
          if (this.bsfh != null) {
            String hashedRelay = DigestUtils.shaHex(Base64.decodeBase64(
                line.split(" ")[2] + "=")).toUpperCase();
            this.bsfh.addHashedRelay(hashedRelay);
          }
           rLine = line;
         } else if (line.startsWith("s ")) {
           if (line.contains(" Running")) {
             exit += line.contains(" Exit") ? 1 : 0;
             fast += line.contains(" Fast") ? 1 : 0;
             guard += line.contains(" Guard") ? 1 : 0;
             stable += line.contains(" Stable") ? 1 : 0;
             running++;
             descriptorIdentities.append("," + rLine.split(" ")[3]);
           }
         }
       }
       if (this.csfh != null) {
         this.csfh.addConsensusResults(validAfter, exit, fast, guard,
           running, stable);
       }
       if (this.sdsfh != null) {
         this.sdsfh.addConsensus(validAfter,
             descriptorIdentities.toString().substring(1));
       }
     } else if (line.startsWith("router ")) {
       String platformLine = null, publishedLine = null,
           bandwidthLine = null;
       while ((line = br.readLine()) != null) {
         if (line.startsWith("platform ")) {
           platformLine = line;
         } else if (line.startsWith("published ")) {
           publishedLine = line;
         } else if (line.startsWith("bandwidth ")) {
           bandwidthLine = line;
         }
       }
       String ascii = new String(data, "US-ASCII");
       String startToken = "router ";
       String sigToken = "\nrouter-signature\n";
       int start = ascii.indexOf(startToken);
       int sig = ascii.indexOf(sigToken) + sigToken.length();
       if (start < 0 || sig < 0 || sig < start) {
         this.logger.warning("Cannot determine descriptor digest! "
             + "Skipping.");
         return;
       }
       byte[] forDigest = new byte[sig - start];
       System.arraycopy(data, start, forDigest, 0, sig - start);
       String descriptorIdentity = Base64.encodeBase64String(
           DigestUtils.sha(forDigest)).substring(0, 27);
       if (this.sdsfh != null) {
         this.sdsfh.addServerDescriptor(descriptorIdentity, platformLine,
             publishedLine, bandwidthLine);
       }
     } else if (line.startsWith("extra-info ") && this.dsfh != null &&
         directories.contains(line.split(" ")[2])) {
       String dir = line.split(" ")[2];
       String statsEnd = null, date = null, v3ips = null;
       boolean skip = false;
       while ((line = br.readLine()) != null) {
         if (line.startsWith("dirreq-stats-end ")) {
           statsEnd = line.split(" ")[1] + " " + line.split(" ")[2];
           date = line.split(" ")[1];
           // trusted had very strange dirreq-v3-shares here...
           skip = dir.equals("8522EB98C91496E80EC238E732594D1509158E77")
               && (date.equals("2009-09-10") || date.equals("2009-09-11"));
         } else if (line.startsWith("dirreq-v3-reqs ")
             && line.length() > "dirreq-v3-reqs ".length()) {
           v3ips = line.split(" ")[1];
         } else if (line.startsWith("dirreq-v3-share ")
             && v3ips != null && !skip) {
           Map<String, String> obs = new HashMap<String, String>();
           String[] parts = v3ips.split(",");
           for (String p : parts) {
             for (String c : this.countries) {
               if (p.startsWith(c)) {
                 obs.put(c, p.substring(3));
               }
             }
           }
           String share = line.substring("dirreq-v3-share ".length(),
               line.length() - 1);
           this.dsfh.addObs(dir, date, obs, share);
           if (!this.lastParsedExtraInfos.containsKey(dir) ||
               statsEnd.compareTo(
               this.lastParsedExtraInfos.get(dir)) > 0) {
             this.lastParsedExtraInfos.put(dir, statsEnd);
             relayDescriptorParseHistoryModified = true;
           }
         }
       }
     }
   }
   /**
    * Returns the URLs of current descriptors that we are missing,
    * including the current consensus and a few extra-info descriptors.
    */
   public Set<String> getMissingDescriptorUrls() {
     Set<String> urls = new HashSet<String>();
     // We might be missing the current consensus for either consensus
     // stats or bridge stats; we remember ourselves which consensus we
     // parsed before (most likely from parsing cached-consensus) and can
     // decide whether we want a more current one
     SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH");
     format.setTimeZone(TimeZone.getTimeZone("UTC"));
     String currentConsensus = format.format(new Date())
         + ":00:00";
     if (currentConsensus.equals(this.lastParsedConsensus)) {
       urls.add("/tor/status-vote/current/consensus");
     }
     // We might be missing extra-info descriptors for dirreq stats for
     // the directories we care about; we are happy with previous dirreq
     // stats until they are more than 36 hours old (24 hours for the
     // next stats period to end plus 12 hours for publishing a new
     // descriptor)
     format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
     format.setTimeZone(TimeZone.getTimeZone("UTC"));
     long now = System.currentTimeMillis();
     for (String directory : this.directories) {
       if (!this.lastParsedExtraInfos.containsKey(directory)) {
         urls.add("/tor/extra/fp/" + directory);
       } else {
         try {
           long statsEnd = format.parse(this.lastParsedExtraInfos.get(
               directory)).getTime();
           if (statsEnd + 36L * 60L * 60L * 1000L < now) {
             urls.add("/tor/extra/fp/" + directory);
           }
         } catch (ParseException e) {
           this.logger.log(Level.WARNING, "Failed parsing timestamp in "
               + this.relayDescriptorParseHistoryFile.getAbsolutePath()
               + "!", e);
         }
       }
     }
     return urls;
   }
   public void writeFile() {
     if (this.relayDescriptorParseHistoryModified) {
       try {
         this.logger.fine("Writing file "
             + this.relayDescriptorParseHistoryFile.getAbsolutePath()
             + "...");
         this.relayDescriptorParseHistoryFile.getParentFile().mkdirs();
         BufferedWriter bw = new BufferedWriter(new FileWriter(
             this.relayDescriptorParseHistoryFile));
         bw.write("type,source,published\n");
         if (this.lastParsedConsensus != null) {
           bw.write("consensus,NA," + this.lastParsedConsensus + "\n");
         }
         for (Map.Entry<String, String> e :
             this.lastParsedExtraInfos.entrySet()) {
           bw.write("extrainfo," + e.getKey() + "," + e.getValue()
               + "\n");
         }
         bw.close();
         this.logger.fine("Finished writing file "
             + this.relayDescriptorParseHistoryFile.getAbsolutePath()
             + ".");
       } catch (IOException e) {
         this.logger.log(Level.WARNING, "Failed writing "
             + this.relayDescriptorParseHistoryFile.getAbsolutePath()
             + "!", e);
       }
     }
   }
 }
 
