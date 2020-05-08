 import java.io.*;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import org.apache.commons.codec.binary.*;
 
 /*
  * TODO Possible extensions:
  * - Include consensus signatures and tell by which Tor versions the
  *   consensus will be accepted (and by which not)
  */
 public class ConsensusHealthChecker {
 
   private String mostRecentValidAfterTime = null;
 
   private byte[] mostRecentConsensus = null;
 
   private SortedMap<String, byte[]> mostRecentVotes =
         new TreeMap<String, byte[]>();
 
   public void processConsensus(String validAfterTime, byte[] data) {
     if (this.mostRecentValidAfterTime == null ||
         this.mostRecentValidAfterTime.compareTo(validAfterTime) < 0) {
       this.mostRecentValidAfterTime = validAfterTime;
       this.mostRecentVotes.clear();
       this.mostRecentConsensus = data;
     }
   }
 
   public void processVote(String validAfterTime, String dirSource,
       byte[] data) {
     if (this.mostRecentValidAfterTime == null ||
         this.mostRecentValidAfterTime.compareTo(validAfterTime) < 0) {
       this.mostRecentValidAfterTime = validAfterTime;
       this.mostRecentVotes.clear();
       this.mostRecentConsensus = null;
     }
     if (this.mostRecentValidAfterTime.equals(validAfterTime)) {
       this.mostRecentVotes.put(dirSource, data);
     }
   }
 
   public void writeStatusWebsite() {
 
     /* If we don't have any consensus, we cannot write useful consensus
      * health information to the website. Do not overwrite existing page
      * with a warning, because we might just not have learned about a new
      * consensus in this execution. */
     if (this.mostRecentConsensus == null) {
       return;
     }
 
     /* Prepare parsing dates. */
     SimpleDateFormat dateTimeFormat =
         new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
     dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
     StringBuilder knownFlagsResults = new StringBuilder();
     StringBuilder numRelaysVotesResults = new StringBuilder();
     StringBuilder consensusMethodsResults = new StringBuilder();
     StringBuilder versionsResults = new StringBuilder();
     StringBuilder paramsResults = new StringBuilder();
     StringBuilder authorityKeysResults = new StringBuilder();
     StringBuilder bandwidthScannersResults = new StringBuilder();
     SortedSet<String> allKnownFlags = new TreeSet<String>();
     SortedSet<String> allKnownVotes = new TreeSet<String>();
     SortedMap<String, String> consensusAssignedFlags =
         new TreeMap<String, String>();
     SortedMap<String, SortedSet<String>> votesAssignedFlags =
         new TreeMap<String, SortedSet<String>>();
     SortedMap<String, String> votesKnownFlags =
         new TreeMap<String, String>();
     SortedMap<String, SortedMap<String, Integer>> flagsAgree =
         new TreeMap<String, SortedMap<String, Integer>>();
     SortedMap<String, SortedMap<String, Integer>> flagsLost =
         new TreeMap<String, SortedMap<String, Integer>>();
     SortedMap<String, SortedMap<String, Integer>> flagsMissing =
         new TreeMap<String, SortedMap<String, Integer>>();
 
 
     /* Read consensus and parse all information that we want to compare to
      * votes. */
     String consensusConsensusMethod = null, consensusKnownFlags = null,
         consensusClientVersions = null, consensusServerVersions = null,
         consensusParams = null, rLineTemp = null;
     int consensusTotalRelays = 0, consensusRunningRelays = 0;
     try {
       BufferedReader br = new BufferedReader(new StringReader(new String(
           this.mostRecentConsensus)));
       String line = null;
       while ((line = br.readLine()) != null) {
         if (line.startsWith("consensus-method ")) {
           consensusConsensusMethod = line;
         } else if (line.startsWith("client-versions ")) {
           consensusClientVersions = line;
         } else if (line.startsWith("server-versions ")) {
           consensusServerVersions = line;
         } else if (line.startsWith("known-flags ")) {
           consensusKnownFlags = line;
         } else if (line.startsWith("params ")) {
           consensusParams = line;
         } else if (line.startsWith("r ")) {
           rLineTemp = line;
         } else if (line.startsWith("s ")) {
           consensusTotalRelays++;
           if (line.contains(" Running")) {
             consensusRunningRelays++;
           }
           consensusAssignedFlags.put(Hex.encodeHexString(
               Base64.decodeBase64(rLineTemp.split(" ")[2] + "=")).
               toUpperCase() + " " + rLineTemp.split(" ")[1], line);
         }
       }
       br.close();
     } catch (IOException e) {
       /* There should be no I/O taking place when reading a String. */
     }
 
     /* Read votes and parse all information to compare with the
      * consensus. */
     for (byte[] voteBytes : this.mostRecentVotes.values()) {
       String voteConsensusMethods = null, voteKnownFlags = null,
           voteClientVersions = null, voteServerVersions = null,
           voteParams = null, dirSource = null, voteDirKeyExpires = null;
       int voteTotalRelays = 0, voteRunningRelays = 0,
           voteContainsBandwidthWeights = 0;
       try {
         BufferedReader br = new BufferedReader(new StringReader(
             new String(voteBytes)));
         String line = null;
         while ((line = br.readLine()) != null) {
           if (line.startsWith("consensus-methods ")) {
             voteConsensusMethods = line;
           } else if (line.startsWith("client-versions ")) {
             voteClientVersions = line;
           } else if (line.startsWith("server-versions ")) {
             voteServerVersions = line;
           } else if (line.startsWith("known-flags ")) {
             voteKnownFlags = line;
           } else if (line.startsWith("params ")) {
             voteParams = line;
           } else if (line.startsWith("dir-source ")) {
             dirSource = line.split(" ")[1];
             allKnownVotes.add(dirSource);
           } else if (line.startsWith("dir-key-expires ")) {
             voteDirKeyExpires = line;
           } else if (line.startsWith("r ")) {
             rLineTemp = line;
           } else if (line.startsWith("s ")) {
             voteTotalRelays++;
             if (line.contains(" Running")) {
               voteRunningRelays++;
             }
             String relayKey = Hex.encodeHexString(Base64.decodeBase64(
                 rLineTemp.split(" ")[2] + "=")).toUpperCase() + " "
                 + rLineTemp.split(" ")[1];
             SortedSet<String> sLines = null;
             if (votesAssignedFlags.containsKey(relayKey)) {
               sLines = votesAssignedFlags.get(relayKey);
             } else {
               sLines = new TreeSet<String>();
               votesAssignedFlags.put(relayKey, sLines);
             }
             sLines.add(dirSource + " " + line);
           } else if (line.startsWith("w ")) {
             if (line.contains(" Measured")) {
               voteContainsBandwidthWeights++;
             }
           }
         }
         br.close();
       } catch (IOException e) {
         /* There should be no I/O taking place when reading a String. */
       }
 
       /* Write known flags. */
       knownFlagsResults.append("          <tr>\n"
           + "            <td>" + dirSource + "</td>\n"
           + "            <td>" + voteKnownFlags + "</td>\n"
           + "          </tr>\n");
       votesKnownFlags.put(dirSource, voteKnownFlags);
       for (String flag : voteKnownFlags.substring(
           "known-flags ".length()).split(" ")) {
         allKnownFlags.add(flag);
       }
 
       /* Write number of relays voted about. */
       numRelaysVotesResults.append("          <tr>\n"
           + "            <td>" + dirSource + "</td>\n"
           + "            <td>" + voteTotalRelays + " total</td>\n"
           + "            <td>" + voteRunningRelays + " Running</td>\n"
           + "          </tr>\n");
 
       /* Write supported consensus methods. */
       if (!voteConsensusMethods.contains(consensusConsensusMethod.
           split(" ")[1])) {
         consensusMethodsResults.append("          <tr>\n"
             + "            <td><font color=\"red\">" + dirSource
               + "</font></td>\n"
             + "            <td><font color=\"red\">"
               + voteConsensusMethods + "</font></td>\n"
             + "          </tr>\n");
       } else {
         consensusMethodsResults.append("          <tr>\n"
                + "            <td>" + dirSource + "</td>\n"
                + "            <td>" + voteConsensusMethods + "</td>\n"
                + "          </tr>\n");
       }
 
       /* Write recommended versions. */
       if (voteClientVersions == null) {
         /* Not a versioning authority. */
       } else if (!voteClientVersions.equals(consensusClientVersions)) {
         versionsResults.append("          <tr>\n"
             + "            <td><font color=\"red\">" + dirSource
               + "</font></td>\n"
             + "            <td><font color=\"red\">"
               + voteClientVersions + "</font></td>\n"
             + "          </tr>\n");
       } else {
         versionsResults.append("          <tr>\n"
             + "            <td>" + dirSource + "</td>\n"
             + "            <td>" + voteClientVersions + "</td>\n"
             + "          </tr>\n");
       }
       if (voteServerVersions == null) {
         /* Not a versioning authority. */
       } else if (!voteServerVersions.equals(consensusServerVersions)) {
         versionsResults.append("          <tr>\n"
             + "            <td/>\n"
             + "            <td><font color=\"red\">"
               + voteServerVersions + "</font></td>\n"
             + "          </tr>\n");
       } else {
         versionsResults.append("          <tr>\n"
             + "            <td/>\n"
             + "            <td>" + voteServerVersions + "</td>\n"
             + "          </tr>\n");
       }
 
       /* Write consensus parameters. */
       if (voteParams == null) {
         /* Authority doesn't set consensus parameters. */
       } else if (!voteParams.equals(consensusParams)) {
         paramsResults.append("          <tr>\n"
             + "            <td><font color=\"red\">" + dirSource
               + "</font></td>\n"
             + "            <td><font color=\"red\">"
               + voteParams + "</font></td>\n"
             + "          </tr>\n");
       } else {
         paramsResults.append("          <tr>\n"
             + "            <td>" + dirSource + "</td>\n"
             + "            <td>" + voteParams + "</td>\n"
             + "          </tr>\n");
       }
 
       /* Write authority key expiration date. */
       if (voteDirKeyExpires != null) {
         boolean expiresIn14Days = false;
         try {
           expiresIn14Days = (System.currentTimeMillis()
               + 14L * 24L * 60L * 60L * 1000L >
               dateTimeFormat.parse(voteDirKeyExpires.substring(
               "dir-key-expires ".length())).getTime());
         } catch (ParseException e) {
           /* Can't parse the timestamp? Whatever. */
         }
         if (expiresIn14Days) {
           authorityKeysResults.append("          <tr>\n"
               + "            <td><font color=\"red\">" + dirSource
                 + "</font></td>\n"
               + "            <td><font color=\"red\">"
                 + voteDirKeyExpires + "</font></td>\n"
               + "          </tr>\n");
         } else {
           authorityKeysResults.append("          <tr>\n"
               + "            <td>" + dirSource + "</td>\n"
               + "            <td>" + voteDirKeyExpires + "</td>\n"
               + "          </tr>\n");
         }
       }
 
       /* Write results for bandwidth scanner status. */
       if (voteContainsBandwidthWeights > 0) {
         bandwidthScannersResults.append("          <tr>\n"
             + "            <td>" + dirSource + "</td>\n"
             + "            <td>" + voteContainsBandwidthWeights
               + " Measured values in w lines<td/>\n"
             + "          </tr>\n");
       }
     }
 
     try {
 
       /* Keep the past two consensus health statuses. */
       File file0 = new File("website/consensus-health.html");
       File file1 = new File("website/consensus-health-1.html");
       File file2 = new File("website/consensus-health-2.html");
       if (file2.exists()) {
         file2.delete();
       }
       if (file1.exists()) {
         file1.renameTo(file2);
       }
       if (file0.exists()) {
         file0.renameTo(file1);
       }
 
       /* Start writing web page. */
       BufferedWriter bw = new BufferedWriter(
           new FileWriter("website/consensus-health.html"));
       bw.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 "
             + "Transitional//EN\">\n"
           + "<html>\n"
           + "  <head>\n"
           + "    <title>Tor Metrics Portal: Consensus health</title>\n"
           + "    <meta http-equiv=Content-Type content=\"text/html; "
             + "charset=iso-8859-1\">\n"
           + "    <link href=\"http://www.torproject.org/stylesheet-"
           + "ltr.css\" type=text/css rel=stylesheet>\n"
           + "    <link href=\"http://www.torproject.org/favicon.ico\""
             + " type=image/x-icon rel=\"shortcut icon\">\n"
           + "  </head>\n"
           + "  <body>\n"
           + "    <div class=\"center\">\n"
           + "      <table class=\"banner\" border=\"0\" "
             + "cellpadding=\"0\" cellspacing=\"0\" summary=\"\">\n"
           + "        <tr>\n"
           + "          <td class=\"banner-left\"><a href=\"https://"
             + "www.torproject.org/\"><img src=\"http://www.torproject"
             + ".org/images/top-left.png\" alt=\"Click to go to home "
             + "page\" width=\"193\" height=\"79\"></a></td>\n"
           + "          <td class=\"banner-middle\">\n"
           + "            <a href=\"/\">Home</a>\n"
           + "            <a href=\"graphs.html\">Graphs</a>\n"
           + "            <a href=\"papers.html\">Papers</a>\n"
           + "            <a href=\"data.html\">Data</a>\n"
           + "            <a href=\"tools.html\">Tools</a>\n"
           + "            <br/>\n"
           + "            <font size=\"2\">\n"
           + "              <a href=\"ernie-howto.html\">ERNIE Howto</a>\n"
           + "              <a href=\"log.html\">Last log</a>\n"
          + "              <a href=\"consensus-health.html\">Consensus health</a>\n"
           + "            </font>\n"
           + "          </td>\n"
           + "          <td class=\"banner-right\"></td>\n"
           + "        </tr>\n"
           + "      </table>\n"
           + "      <div class=\"main-column\">\n"
           + "        <h2>Tor Metrics Portal: Consensus Health</h2>\n"
           + "        <br/>\n"
           + "        <p>This page shows statistics about the current "
             + "consensus and votes to facilitate debugging of the "
             + "directory consensus process.</p>\n");
 
       /* Write valid-after time. */
       bw.write("        <br/>\n"
           + "        <h3>Valid-after time</h3>\n"
           + "        <br/>\n"
           + "        <p>Consensus was published ");
       boolean consensusIsStale = false;
       try {
         consensusIsStale = System.currentTimeMillis()
             - 3L * 60L * 60L * 1000L >
             dateTimeFormat.parse(this.mostRecentValidAfterTime).getTime();
       } catch (ParseException e) {
         /* Can't parse the timestamp? Whatever. */
       }
       if (consensusIsStale) {
         bw.write("<font color=\"red\">" + this.mostRecentValidAfterTime
             + "</font>");
       } else {
         bw.write(this.mostRecentValidAfterTime);
       }
       bw.write(". <i>Note that it takes "
             + "15 to 30 minutes for the metrics portal to learn about "
             + "new consensus and votes and process them.</i></p>\n");
 
       /* Write known flags. */
       bw.write("        <br/>\n"
           + "        <h3>Known flags</h3>\n"
           + "        <br/>\n"
           + "        <table border=\"0\" cellpadding=\"4\" "
           + "cellspacing=\"0\" summary=\"\">\n"
           + "          <colgroup>\n"
           + "            <col width=\"160\">\n"
           + "            <col width=\"640\">\n"
           + "          </colgroup>\n");
       if (knownFlagsResults.length() < 1) {
         bw.write("          <tr><td>(No votes.)</td><td/></tr>\n");
       } else {
         bw.write(knownFlagsResults.toString());
       }
       bw.write("          <tr>\n"
           + "            <td><font color=\"blue\">consensus</font>"
             + "</td>\n"
           + "            <td><font color=\"blue\">"
             + consensusKnownFlags + "</font></td>\n"
           + "          </tr>\n");
       bw.write("        </table>\n");
 
       /* Write number of relays voted about. */
       bw.write("        <br/>\n"
           + "        <h3>Number of relays voted about</h3>\n"
           + "        <br/>\n"
           + "        <table border=\"0\" cellpadding=\"4\" "
           + "cellspacing=\"0\" summary=\"\">\n"
           + "          <colgroup>\n"
           + "            <col width=\"160\">\n"
           + "            <col width=\"320\">\n"
           + "            <col width=\"320\">\n"
           + "          </colgroup>\n");
       if (numRelaysVotesResults.length() < 1) {
         bw.write("          <tr><td>(No votes.)</td><td/><td/></tr>\n");
       } else {
         bw.write(numRelaysVotesResults.toString());
       }
       bw.write("          <tr>\n"
           + "            <td><font color=\"blue\">consensus</font>"
             + "</td>\n"
           + "            <td><font color=\"blue\">"
             + consensusTotalRelays + " total</font></td>\n"
           + "            <td><font color=\"blue\">"
             + consensusRunningRelays + " Running</font></td>\n"
           + "          </tr>\n");
       bw.write("        </table>\n");
 
       /* Write consensus methods. */
       bw.write("        <br/>\n"
           + "        <h3>Consensus methods</h3>\n"
           + "        <br/>\n"
           + "        <table border=\"0\" cellpadding=\"4\" "
           + "cellspacing=\"0\" summary=\"\">\n"
           + "          <colgroup>\n"
           + "            <col width=\"160\">\n"
           + "            <col width=\"640\">\n"
           + "          </colgroup>\n");
       if (consensusMethodsResults.length() < 1) {
         bw.write("          <tr><td>(No votes.)</td><td/></tr>\n");
       } else {
         bw.write(consensusMethodsResults.toString());
       }
       bw.write("          <tr>\n"
           + "            <td><font color=\"blue\">consensus</font>"
             + "</td>\n"
           + "            <td><font color=\"blue\">"
             + consensusConsensusMethod + "</font></td>\n"
           + "          </tr>\n");
       bw.write("        </table>\n");
 
       /* Write recommended versions. */
       bw.write("        <br/>\n"
           + "        <h3>Recommended versions</h3>\n"
           + "        <br/>\n"
           + "        <table border=\"0\" cellpadding=\"4\" "
           + "cellspacing=\"0\" summary=\"\">\n"
           + "          <colgroup>\n"
           + "            <col width=\"160\">\n"
           + "            <col width=\"640\">\n"
           + "          </colgroup>\n");
       if (versionsResults.length() < 1) {
         bw.write("          <tr><td>(No votes.)</td><td/></tr>\n");
       } else {
         bw.write(versionsResults.toString());
       }
       bw.write("          <tr>\n"
           + "            <td><font color=\"blue\">consensus</font>"
           + "</td>\n"
           + "            <td><font color=\"blue\">"
             + consensusClientVersions + "</font></td>\n"
           + "          </tr>\n");
       bw.write("          <td/>\n"
           + "            <td><font color=\"blue\">"
           + consensusServerVersions + "</font></td>\n"
         + "          </tr>\n");
       bw.write("        </table>\n");
 
       /* Write consensus parameters. */
       bw.write("        <br/>\n"
           + "        <h3>Consensus parameters</h3>\n"
           + "        <br/>\n"
           + "        <table border=\"0\" cellpadding=\"4\" "
           + "cellspacing=\"0\" summary=\"\">\n"
           + "          <colgroup>\n"
           + "            <col width=\"160\">\n"
           + "            <col width=\"640\">\n"
           + "          </colgroup>\n");
       if (paramsResults.length() < 1) {
         bw.write("          <tr><td>(No votes.)</td><td/></tr>\n");
       } else {
         bw.write(paramsResults.toString());
       }
       bw.write("          <td><font color=\"blue\">consensus</font>"
           + "</td>\n"
         + "            <td><font color=\"blue\">"
           + consensusParams + "</font></td>\n"
         + "          </tr>\n");
       bw.write("        </table>\n");
 
       /* Write authority keys. */
       bw.write("        <br/>\n"
           + "        <h3>Authority keys</h3>\n"
           + "        <br/>\n"
           + "        <table border=\"0\" cellpadding=\"4\" "
           + "cellspacing=\"0\" summary=\"\">\n"
           + "          <colgroup>\n"
           + "            <col width=\"160\">\n"
           + "            <col width=\"640\">\n"
           + "          </colgroup>\n");
       if (authorityKeysResults.length() < 1) {
         bw.write("          <tr><td>(No votes.)</td><td/></tr>\n");
       } else {
         bw.write(authorityKeysResults.toString());
       }
       bw.write("        </table>\n"
           + "        <br/>\n"
           + "        <p><i>Note that expiration dates of legacy keys are "
             + "not included in votes and therefore not listed here!</i>"
             + "</p>\n");
 
       /* Write bandwidth scanner status. */
       bw.write("        <br/>\n"
            + "        <h3>Bandwidth scanner status</h3>\n"
           + "        <br/>\n"
           + "        <table border=\"0\" cellpadding=\"4\" "
           + "cellspacing=\"0\" summary=\"\">\n"
           + "          <colgroup>\n"
           + "            <col width=\"160\">\n"
           + "            <col width=\"640\">\n"
           + "          </colgroup>\n");
       if (bandwidthScannersResults.length() < 1) {
         bw.write("          <tr><td>(No votes.)</td><td/></tr>\n");
       } else {
         bw.write(bandwidthScannersResults.toString());
       }
       bw.write("        </table>\n");
 
       /* Write (huge) table with all flags. */
       bw.write("        <br/>\n"
           + "        <h3>Relay flags</h3>\n"
           + "        <br/>\n"
           + "        <p>The semantics of flags written in the table is "
             + "as follows:</p>\n"
           + "        <ul>\n"
           + "          <li><b>In vote and consensus:</b> Flag in vote "
             + "matches flag in consensus, or relay is not listed in "
             + "consensus (because it doesn't have the Running "
             + "flag)</li>\n"
           + "          <li><b><font color=\"red\">Only in "
             + "vote:</font></b> Flag in vote, but missing in the "
             + "consensus, because there was no majority for the flag or "
             + "the flag was invalidated (e.g., Named gets invalidated by "
             + "Unnamed)</li>\n"
           + "          <li><b><font color=\"gray\"><s>Only in "
             + "consensus:</s></font></b> Flag in consensus, but missing "
             + "in a vote of a directory authority voting on this "
             + "flag</li>\n"
           + "          <li><b><font color=\"blue\">In "
             + "consensus:</font></b> Flag in consensus</li>\n"
           + "        </ul>\n"
           + "        <br/>\n"
           + "        <p>See also the summary below the table.</p>\n"
           + "        <table border=\"0\" cellpadding=\"4\" "
           + "cellspacing=\"0\" summary=\"\">\n"
           + "          <colgroup>\n"
           + "            <col width=\"120\">\n"
           + "            <col width=\"80\">\n");
       for (int i = 0; i < allKnownVotes.size(); i++) {
         bw.write("            <col width=\""
             + (640 / allKnownVotes.size()) + "\">\n");
       }
       bw.write("          </colgroup>\n");
       int linesWritten = 0;
       for (Map.Entry<String, SortedSet<String>> e :
           votesAssignedFlags.entrySet()) {
         if (linesWritten++ % 10 == 0) {
           bw.write("          <tr><td/><td/>\n");
           for (String dir : allKnownVotes) {
             String shortDirName = dir.length() > 6 ?
                 dir.substring(0, 5) + "." : dir;
             bw.write("<td><br/><b>" + shortDirName + "</b></td>");
           }
           bw.write("<td><br/><b>consensus</b></td></tr>\n");
         }
         String relayKey = e.getKey();
         SortedSet<String> votes = e.getValue();
         String fingerprint = relayKey.split(" ")[0].substring(0, 8);
         String nickname = relayKey.split(" ")[1];
         bw.write("          <tr>\n"
             + "            <td>" + fingerprint + "</td>\n"
             + "            <td>" + nickname + "</td>\n");
         SortedSet<String> relevantFlags = new TreeSet<String>();
         for (String vote : votes) {
           String[] parts = vote.split(" ");
           for (int j = 2; j < parts.length; j++) {
             relevantFlags.add(parts[j]);
           }
         }
         String consensusFlags = null;
         if (consensusAssignedFlags.containsKey(relayKey)) {
           consensusFlags = consensusAssignedFlags.get(relayKey);
           String[] parts = consensusFlags.split(" ");
           for (int j = 1; j < parts.length; j++) {
             relevantFlags.add(parts[j]);
           }
         }
         for (String dir : allKnownVotes) {
           String flags = null;
           for (String vote : votes) {
             if (vote.startsWith(dir)) {
               flags = vote;
               break;
             }
           }
           if (flags != null) {
             votes.remove(flags);
             bw.write("            <td>");
             int flagsWritten = 0;
             for (String flag : relevantFlags) {
               bw.write(flagsWritten++ > 0 ? "<br/>" : "");
               SortedMap<String, SortedMap<String, Integer>> sums = null;
               if (flags.contains(" " + flag)) {
                 if (consensusFlags == null ||
                   consensusFlags.contains(" " + flag)) {
                   bw.write(flag);
                   sums = flagsAgree;
                 } else {
                   bw.write("<font color=\"red\">" + flag + "</font>");
                   sums = flagsLost;
                 }
               } else if (consensusFlags != null &&
                   votesKnownFlags.get(dir).contains(" " + flag) &&
                   consensusFlags.contains(" " + flag)) {
                 bw.write("<font color=\"gray\"><s>" + flag
                     + "</s></font>");
                 sums = flagsMissing;
               }
               if (sums != null) {
                 SortedMap<String, Integer> sum = null;
                 if (sums.containsKey(dir)) {
                   sum = sums.get(dir);
                 } else {
                   sum = new TreeMap<String, Integer>();
                   sums.put(dir, sum);
                 }
                 sum.put(flag, sum.containsKey(flag) ?
                     sum.get(flag) + 1 : 1);
               }
             }
             bw.write("</td>\n");
           } else {
             bw.write("            <td/>\n");
           }
         }
         if (consensusFlags != null) {
           bw.write("            <td>");
           int flagsWritten = 0;
           for (String flag : relevantFlags) {
             bw.write(flagsWritten++ > 0 ? "<br/>" : "");
             if (consensusFlags.contains(" " + flag)) {
               bw.write("<font color=\"blue\">" + flag + "</font>");
             }
           }
           bw.write("</td>\n");
         } else {
           bw.write("            <td/>\n");
         }
         bw.write("          </tr>\n");
       }
       bw.write("        </table>\n");
 
       /* Write summary of overlap between votes and consensus. */
       bw.write("        <br/>\n"
            + "        <h3>Overlap between votes and consensus</h3>\n"
           + "        <br/>\n"
           + "        <p>The semantics of columns is similar to the "
             + "table above:</p>\n"
           + "        <ul>\n"
           + "          <li><b>In vote and consensus:</b> Flag in vote "
             + "matches flag in consensus, or relay is not listed in "
             + "consensus (because it doesn't have the Running "
             + "flag)</li>\n"
           + "          <li><b><font color=\"red\">Only in "
             + "vote:</font></b> Flag in vote, but missing in the "
             + "consensus, because there was no majority for the flag or "
             + "the flag was invalidated (e.g., Named gets invalidated by "
             + "Unnamed)</li>\n"
           + "          <li><b><font color=\"gray\"><s>Only in "
             + "consensus:</s></font></b> Flag in consensus, but missing "
             + "in a vote of a directory authority voting on this "
             + "flag</li>\n"
           + "        </ul>\n"
           + "        <br/>\n"
           + "        <table border=\"0\" cellpadding=\"4\" "
           + "cellspacing=\"0\" summary=\"\">\n"
           + "          <colgroup>\n"
           + "            <col width=\"160\">\n"
           + "            <col width=\"210\">\n"
           + "            <col width=\"210\">\n"
           + "            <col width=\"210\">\n"
           + "          </colgroup>\n");
       bw.write("          <tr><td/><td><b>Only in vote</b></td>"
             + "<td><b>In vote and consensus</b></td>"
             + "<td><b>Only in consensus</b></td>\n");
       for (String dir : allKnownVotes) {
         boolean firstFlagWritten = false;
         String[] flags = votesKnownFlags.get(dir).substring(
             "known-flags ".length()).split(" ");
         for (String flag : flags) {
           bw.write("          <tr>\n");
           if (firstFlagWritten) {
             bw.write("            <td/>\n");
           } else {
             bw.write("            <td>" + dir + "</td>\n");
             firstFlagWritten = true;
           }
           if (flagsLost.containsKey(dir) &&
               flagsLost.get(dir).containsKey(flag)) {
             bw.write("            <td><font color=\"red\"> "
                   + flagsLost.get(dir).get(flag) + " " + flag
                   + "</font></td>\n");
           } else {
             bw.write("            <td/>\n");
           }
           if (flagsAgree.containsKey(dir) &&
               flagsAgree.get(dir).containsKey(flag)) {
             bw.write("            <td>" + flagsAgree.get(dir).get(flag)
                   + " " + flag + "</td>\n");
           } else {
             bw.write("            <td/>\n");
           }
           if (flagsMissing.containsKey(dir) &&
               flagsMissing.get(dir).containsKey(flag)) {
             bw.write("            <td><font color=\"gray\"><s>"
                   + flagsMissing.get(dir).get(flag) + " " + flag
                   + "</s></font></td>\n");
           } else {
             bw.write("            <td/>\n");
           }
           bw.write("          </tr>\n");
         }
       }
       bw.write("        </table>\n");
 
       /* Finish writing. */
       bw.write("      </div>\n"
           + "    </div>\n"
           + "    <div class=\"bottom\" id=\"bottom\">\n"
           + "      <p>\"Tor\" and the \"Onion Logo\" are <a "
             + "href=\"https://www.torproject.org/trademark-faq.html"
             + ".en\">"
           + "registered trademarks</a> of The Tor Project, "
             + "Inc.</p>\n"
           + "    </div>\n"
           + "  </body>\n"
           + "</html>");
       bw.close();
 
     } catch (IOException e) {
     }
   }
 }
