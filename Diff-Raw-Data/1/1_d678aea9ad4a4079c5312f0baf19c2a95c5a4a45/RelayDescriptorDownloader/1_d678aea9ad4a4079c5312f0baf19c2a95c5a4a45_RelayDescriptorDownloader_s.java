 import java.io.*;
 import java.net.*;
 import java.text.*;
 import java.util.*;
 import java.util.logging.*;
 
 /**
  * Downloads missing relay descriptors from the directories via HTTP.
  * Keeps a list of missing descriptors that gets updated by parse results
  * from <code>RelayDescriptorParser</code>. Only descriptors on that
  * missing list that we think might be available on the directories are
  * downloaded.
  */
 public class RelayDescriptorDownloader {
 
   /**
    * Text file containing the descriptors that we are missing and that we
    * want to download in <code>downloadMissingDescriptors</code>.
    * Lines are formatted as:
    * - "consensus,<validafter>,<parsed>",
    * - "vote,<validafter>,<fingerprint>,<parsed>",
    * - "server,<published>,<relayid>,<descid>,<parsed>", or
    * - "extra,<published>,<relayid>,<descid><parsed>".
    */
   private File missingDescriptorsFile;
 
   /**
    * Relay descriptors that we are missing and that we want to download
    * either in this execution or write to disk and try next time. Map keys
    * contain comma-separated values as in the missing descriptors files
    * without the parsed column. Map values contain the parsed column.
    */
   private SortedMap<String, String> missingDescriptors;
 
   /**
    * <code>RelayDescriptorParser</code> that we will hand over the
    * downloaded descriptors for parsing.
    */
   private RelayDescriptorParser rdp;
 
   /**
    * Directories that we will try to download missing descriptors from.
    */
   private List<String> dirSources;
 
   /**
    * Should we try to download the current consensus if we don't have it?
    */
   private boolean downloadCurrentConsensus;
 
   /**
    * Should we try to download current votes if we don't have them?
    */
   private boolean downloadCurrentVotes;
 
   /**
    * Should we try to download all missing server descriptors that have
    * been published within the past 24 hours?
    */
   private boolean downloadAllServerDescriptors;
 
   /**
    * Should we try to download all missing extra-info descriptors that
    * have been published within the past 24 hours?
    */
   private boolean downloadAllExtraInfos;
 
   /**
    * Should we try to download missing server and extra-info descriptors
    * of certain relays that have been published within the past 24 hours?
    */
   private Set<String> downloadDescriptorsForRelays;
 
   /**
    * valid-after time that we expect the current consensus and votes to
    * have, formatted "yyyy-MM-dd HH:mm:ss". We only expect to find
    * consensuses and votes with this valid-after time on the directories.
    * This time is initialized as the beginning of the current hour.
    */
   private String currentValidAfter;
 
   /**
    * Cut-off time for missing server and extra-info descriptors, formatted
    * "yyyy-MM-dd HH:mm:ss". This time is initialized as the current system
    * time minus 24 hours.
    */
   private String descriptorCutOff;
 
   /**
    * Current timestamp that is written to the missing list for descriptors
    * that we parsed in this execution. This timestamp is most useful for
    * debugging purposes when looking at the missing list. For execution it
    * only matters whether the parsed time is "NA" or has some other value.
    */
   private String parsedTimestampString;
 
   /**
    * Logger for this class.
    */
   private Logger logger;
 
   private StringBuilder dumpStats;
   private int newMissingConsensuses = 0, newMissingVotes = 0,
       newMissingServerDescriptors = 0, newMissingExtraInfoDescriptors = 0,
       triedConsensuses = 0, triedVotes = 0, triedServerDescriptors = 0,
       triedExtraInfoDescriptors = 0, downloadedConsensuses = 0,
       downloadedVotes = 0, downloadedServerDescriptors = 0,
       downloadedExtraInfoDescriptors = 0;
   /**
    * Initializes this class, including reading in missing descriptors from
    * <code>stats/missing-relay-descriptors</code>.
    */
   public RelayDescriptorDownloader(RelayDescriptorParser rdp,
       List<String> dirSources, boolean downloadCurrentConsensus,
       boolean downloadCurrentVotes, boolean downloadAllServerDescriptors,
       boolean downloadAllExtraInfos,
       Set<String> downloadDescriptorsForRelays) {
 
     /* Memorize argument values. */
     this.rdp = rdp;
     this.dirSources = dirSources;
     this.downloadCurrentConsensus = downloadCurrentConsensus;
     this.downloadCurrentVotes = downloadCurrentVotes;
     this.downloadAllServerDescriptors = downloadAllServerDescriptors;
     this.downloadAllExtraInfos = downloadAllExtraInfos;
     this.downloadDescriptorsForRelays = downloadDescriptorsForRelays;
 
     /* Initialize logger. */
     this.logger = Logger.getLogger(RelayDescriptorParser.class.getName());
 
     /* Prepare cut-off times and timestamp for missing descriptors
      * list. */
     SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
     format.setTimeZone(TimeZone.getTimeZone("UTC"));
     long now = System.currentTimeMillis();
     this.currentValidAfter = format.format((now / (60L * 60L * 1000L)) *
         (60L * 60L * 1000L));
     this.descriptorCutOff = format.format(now - 24L * 60L * 60L * 1000L);
     this.parsedTimestampString = format.format(now);
 
     /* Initialize missing list and put current consensus on it if we want
      * it. */
     this.missingDescriptors = new TreeMap<String, String>();
     if (this.downloadCurrentConsensus) {
       this.missingDescriptors.put("consensus," + this.currentValidAfter,
           "NA");
     }
 
     /* Read list of missing descriptors from disk and memorize those that
      * we are interested in and that are likely to be found on the
      * directory servers. */
     this.missingDescriptorsFile = new File(
         "stats/missing-relay-descriptors");
     int missingConsensuses = 0, missingVotes = 0,
         missingServerDescriptors = 0, missingExtraInfoDescriptors = 0;
     if (this.missingDescriptorsFile.exists()) {
       try {
         this.logger.fine("Reading file "
             + this.missingDescriptorsFile.getAbsolutePath() + "...");
         BufferedReader br = new BufferedReader(new FileReader(
             this.missingDescriptorsFile));
         String line = null;
         while ((line = br.readLine()) != null) {
           if (line.split(",").length > 2) {
             String published = line.split(",")[1];
             if (((line.startsWith("consensus,") ||
                 line.startsWith("vote,")) &&
                 this.currentValidAfter.equals(published)) ||
                 ((line.startsWith("server,") ||
                 line.startsWith("extra,")) &&
                 this.descriptorCutOff.compareTo(published) <= 0)) {
               if (line.startsWith("consensus,")) {
                 missingConsensuses++;
               } else if (line.startsWith("vote,")) {
                 missingVotes++;
               } else if (line.startsWith("server,")) {
                 missingServerDescriptors++;
               } else if (line.startsWith("extra,")) {
                 missingExtraInfoDescriptors++;
               }
               int separateAt = line.lastIndexOf(",");
               this.missingDescriptors.put(line.substring(0,
                   separateAt), line.substring(separateAt + 1));
             }
           } else {
             this.logger.fine("Invalid line '" + line + "' in "
                 + this.missingDescriptorsFile.getAbsolutePath()
                 + ". Ignoring.");
           }
         }
         br.close();
         this.logger.fine("Finished reading file "
             + this.missingDescriptorsFile.getAbsolutePath() + ".");
       } catch (IOException e) {
         this.logger.log(Level.WARNING, "Failed to read file "
             + this.missingDescriptorsFile.getAbsolutePath()
             + "! This means that we might forget to dowload relay "
             + "descriptors we are missing.", e);
       }
 
       dumpStats.append("Finished downloading relay descriptors from the "
         + "directory authorities:\nAt the beginning of this execution, "
         + "we were missing " + missingConsensuses + " consensuses, "
         + missingVotes + " votes, " + missingServerDescriptors
         + " server descriptors, and " + missingExtraInfoDescriptors
         + " extra-info descriptors.");
     }
   }
 
   /**
    * We have parsed a consensus. Take this consensus off the missing list
    * and add the votes created by the given <code>dirSources</code> and
    * the <code>serverDescriptors</code> in the format
    * "<published>,<relayid>,<descid>" to that list.
    */
   public void haveParsedConsensus(String validAfter,
       Set<String> dirSources, Set<String> serverDescriptors) {
 
     /* Mark consensus as parsed. */
     if (this.currentValidAfter.equals(validAfter)) {
       String consensusKey = "consensus," + validAfter;
       this.missingDescriptors.put(consensusKey,
           this.parsedTimestampString);
 
       /* Add votes to missing list. */
       if (this.downloadCurrentVotes) {
         for (String dirSource : dirSources) {
           String voteKey = "vote," + validAfter + "," + dirSource;
           if (!this.missingDescriptors.containsKey(voteKey)) {
             this.missingDescriptors.put(voteKey, "NA");
             this.newMissingVotes++;
           }
         }
       }
     }
 
     /* Add server descriptors to missing list. */
     if (this.downloadAllServerDescriptors ||
         this.downloadDescriptorsForRelays != null) {
       for (String serverDescriptor : serverDescriptors) {
         String published = serverDescriptor.split(",")[0];
         if (this.descriptorCutOff.compareTo(published) <= 0) {
           if (this.downloadAllServerDescriptors ||
               (this.downloadDescriptorsForRelays != null &&
               this.downloadDescriptorsForRelays.contains(
               serverDescriptor.split(",")[1].toUpperCase()))) {
             String serverDescriptorKey = "server," + serverDescriptor;
             if (!this.missingDescriptors.containsKey(
                 serverDescriptorKey)) {
               this.missingDescriptors.put(serverDescriptorKey, "NA");
               this.newMissingServerDescriptors++;
             }
           }
         }
       }
     }
   }
 
   /**
    * We have parsed a vote. Take this vote off the missing list.
    */
   public void haveParsedVote(String validAfter, String fingerprint,
       Set<String> serverDescriptors) {
 
     /* Mark consensus as parsed. */
     if (this.currentValidAfter.equals(validAfter)) {
       String voteKey = "vote," + validAfter + "," + fingerprint;
       this.missingDescriptors.put(voteKey, this.parsedTimestampString);
     }
 
     /* Add server descriptors to missing list. */
     if (this.downloadAllServerDescriptors ||
         this.downloadDescriptorsForRelays != null) {
       for (String serverDescriptor : serverDescriptors) {
         String published = serverDescriptor.split(",")[0];
         if (this.descriptorCutOff.compareTo(published) < 0) {
           if (this.downloadDescriptorsForRelays == null ||
               this.downloadDescriptorsForRelays.contains(
               serverDescriptor.split(",")[1].toUpperCase())) {
             String serverDescriptorKey = "server," + serverDescriptor;
             if (!this.missingDescriptors.containsKey(
                 serverDescriptorKey)) {
               this.missingDescriptors.put(serverDescriptorKey, "NA");
               this.newMissingServerDescriptors++;
             }
           }
         }
       }
     }
   }
 
   /**
    * We have parsed a server descriptor. Take this server descriptor off
    * the missing list and put the extra-info descriptor digest on that
    * list.
    */
   public void haveParsedServerDescriptor(String published,
       String relayIdentity, String serverDescriptorDigest,
       String extraInfoDigest) {
 
     /* Mark server descriptor as parsed. */
     if (this.descriptorCutOff.compareTo(published) <= 0) {
       String serverDescriptorKey = "server," + published + ","
           + relayIdentity + "," + serverDescriptorDigest;
       this.missingDescriptors.put(serverDescriptorKey,
           this.parsedTimestampString);
 
       /* Add extra-info descriptor to missing list. */
       if (extraInfoDigest != null && (this.downloadAllExtraInfos ||
           (this.downloadDescriptorsForRelays != null &&
           this.downloadDescriptorsForRelays.contains(relayIdentity.
           toUpperCase())))) {
         String extraInfoKey = "extra," + published + ","
             + relayIdentity + "," + extraInfoDigest;
         if (!this.missingDescriptors.containsKey(extraInfoKey)) {
           this.missingDescriptors.put(extraInfoKey, "NA");
           this.newMissingExtraInfoDescriptors++;
         }
       }
     }
   }
 
   /**
    * We have parsed an extra-info descriptor. Take it off the missing
    * list.
    */
   public void haveParsedExtraInfoDescriptor(String published,
       String relayIdentity, String extraInfoDigest) {
     if (this.descriptorCutOff.compareTo(published) <= 0) {
       String extraInfoKey = "extra," + published + ","
           + relayIdentity + "," + extraInfoDigest;
       this.missingDescriptors.put(extraInfoKey,
           this.parsedTimestampString);
     }
   }
 
   /**
    * Downloads missing descriptors that we think might still be available
    * on the directories.
    */
   public void downloadMissingDescriptors() {
 
     /* Update cut-off times to reflect that execution so far might have
      * taken a few minutes and that some descriptors aren't available on
      * the directories anymore. */
     SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
     format.setTimeZone(TimeZone.getTimeZone("UTC"));
     long now = System.currentTimeMillis();
     this.currentValidAfter = format.format((now / (60L * 60L * 1000L)) *
         (60L * 60L * 1000L));
     this.descriptorCutOff = format.format(now - 24L * 60L * 60L * 1000L);
 
     /* Remember which directories remain as source for downloading
      * descriptors. */
     List<String> remainingDirSources =
         new ArrayList<String>(this.dirSources);
 
     /* URLs of descriptors we want to download. */
     SortedSet<String> urls = new TreeSet<String>();
 
     /* URLs of descriptors we have downloaded or at least tried to
      * download. */
     SortedSet<String> downloaded = new TreeSet<String>();
 
     /* We might need more than one iteration for downloading descriptors,
      * because we might learn about descriptors while parsing those that
      * we got. In every iteration, compile a new list of URLs, remove
      * those that we tried before, and download the remaining ones. Stop
      * when there are no new URLs anymore. */
     do {
 
       /* Compile list of URLs to download in this iteration. */
       urls.clear();
       for (Map.Entry<String, String> e :
           this.missingDescriptors.entrySet()) {
         if (e.getValue().equals("NA")) {
           String[] parts = e.getKey().split(",");
           if (parts[0].equals("consensus") &&
               this.downloadCurrentConsensus &&
               this.currentValidAfter.equals(parts[1])) {
             urls.add("/tor/status-vote/current/consensus");
             this.triedConsensuses++;
           } else if (parts[0].equals("vote") &&
               this.downloadCurrentVotes &&
               this.currentValidAfter.equals(parts[1])) {
             urls.add("/tor/status-vote/current/" + parts[2]);
             this.triedVotes++;
           } else if (parts[0].equals("server") &&
               (this.downloadAllServerDescriptors ||
               (this.downloadDescriptorsForRelays != null &&
               this.downloadDescriptorsForRelays.contains(parts[2].
               toUpperCase()))) &&
               this.descriptorCutOff.compareTo(parts[1]) <= 0) {
             urls.add("/tor/server/d/" + parts[3]);
             this.triedServerDescriptors++;
           } else if (parts[0].equals("extra") &&
               (this.downloadAllExtraInfos ||
               (this.downloadDescriptorsForRelays != null &&
               this.downloadDescriptorsForRelays.contains(parts[2].
               toUpperCase()))) &&
               this.descriptorCutOff.compareTo(parts[1]) <= 0) {
             urls.add("/tor/extra/d/" + parts[3]);
             this.triedExtraInfoDescriptors++;
           }
         }
       }
       urls.removeAll(downloaded);
 
       /* Log what we're downloading. */
       StringBuilder sb = new StringBuilder("Downloading " + urls.size()
           + " descriptors:");
       for (String url : urls) {
         sb.append(url + "\n");
       }
       this.logger.fine(sb.toString());
 
       /* We are trying to download these descriptors from each directory
        * source one after the other until we got it from one. For each
        * directory source we are removing the URLs from urls and putting
        * those the we want to retry into retryUrls. Once we are done, we
        * move the URLs back to urls and try the next directory source. */
       SortedSet<String> currentDirSources =
           new TreeSet<String>(remainingDirSources);
       SortedSet<String> retryUrls = new TreeSet<String>();
       while (!currentDirSources.isEmpty() && !urls.isEmpty()) {
         String authority = currentDirSources.first();
         String url = urls.first();
         try {
           URL u = new URL("http://" + authority + url);
           HttpURLConnection huc =
               (HttpURLConnection) u.openConnection();
           huc.setRequestMethod("GET");
           huc.connect();
           int response = huc.getResponseCode();
           logger.finer("Downloading http://" + authority + url + " -> "
               + response);
           if (response == 200) {
             BufferedInputStream in = new BufferedInputStream(
                 huc.getInputStream());
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             int len;
             byte[] data = new byte[1024];
             while ((len = in.read(data, 0, 1024)) >= 0) {
               baos.write(data, 0, len);
             }
             in.close();
             byte[] allData = baos.toByteArray();
             rdp.parse(allData);
             if (url.endsWith("consensus")) {
               this.downloadedConsensuses++;
             } else if (url.contains("status-vote")) {
               this.downloadedVotes++;
             } else if (url.contains("server")) {
               this.downloadedServerDescriptors++;
             } else if (url.contains("extra")) {
               this.downloadedExtraInfoDescriptors++;
             }
           } else {
             retryUrls.add(url);
           }
           urls.remove(url);
           if (urls.isEmpty()) {
             currentDirSources.remove(authority);
             urls.addAll(retryUrls);
             retryUrls.clear();
           }
         } catch (IOException e) {
           remainingDirSources.remove(authority);
           currentDirSources.remove(authority);
           if (!remainingDirSources.isEmpty()) {
             logger.log(Level.FINE, "Failed downloading from "
                 + authority + "!", e);
           } else {
             logger.log(Level.WARNING, "Failed downloading from "
                 + authority + "! We have no authorities left to download "
                 + "from!", e);
           }
         }
       }
       downloaded.addAll(urls);
     } while (!urls.isEmpty());
   }
 
   public void writeFile() {
     int missingConsensuses = 0, missingVotes = 0,
         missingServerDescriptors = 0, missingExtraInfoDescriptors = 0;
     try {
       this.logger.fine("Writing file "
           + this.missingDescriptorsFile.getAbsolutePath() + "...");
       this.missingDescriptorsFile.getParentFile().mkdirs();
       BufferedWriter bw = new BufferedWriter(new FileWriter(
           this.missingDescriptorsFile));
       for (Map.Entry<String, String> e :
           this.missingDescriptors.entrySet()) {
         String key = e.getKey();
         if (key.startsWith("consensus,")) {
           missingConsensuses++;
         } else if (key.startsWith("vote,")) {
           missingVotes++;
         } else if (key.startsWith("server,")) {
           missingServerDescriptors++;
         } else if (key.startsWith("extra,")) {
           missingExtraInfoDescriptors++;
         }
         bw.write(e.getKey() + "," + e.getValue() + "\n");
       }
       bw.close();
       this.logger.fine("Finished writing file "
           + this.missingDescriptorsFile.getAbsolutePath() + ".");
     } catch (IOException e) {
       this.logger.log(Level.WARNING, "Failed writing "
           + this.missingDescriptorsFile.getAbsolutePath() + "!", e);
     }
 
     dumpStats.append("During this execution, we added "
         + this.newMissingConsensuses + " consensuses, "
         + this.newMissingVotes + " votes, "
         + this.newMissingServerDescriptors + " server descriptors, and "
         + this.newMissingExtraInfoDescriptors + " extra-info descriptors "
         + "to the missing list.\n");
     dumpStats.append("We attempted to download " + this.triedConsensuses
         + " consensuses, " + this.triedVotes + " votes, "
         + this.triedServerDescriptors + " server descriptors, and "
         + this.triedExtraInfoDescriptors + " extra-info descriptors from "
         + "the directory authorities.\n");
     dumpStats.append("We successfully downloaded "
         + this.downloadedConsensuses + " consensuses, "
         + this.downloadedVotes + " votes, "
         + this.downloadedServerDescriptors + " server descriptors, and "
         + this.downloadedExtraInfoDescriptors + " extra-info descriptors "
         + "from the directory authorities.\n");
     dumpStats.append("At the end of this execution, "
       + "we are missing " + missingConsensuses + " consensuses, "
       + missingVotes + " votes, " + missingServerDescriptors
       + " server descriptors, and " + missingExtraInfoDescriptors
       + " extra-info descriptors, some of which we may try in the next "
       + "execution.");
     this.logger.info(dumpStats.toString());
   }
 }
