 package org.torproject.ernie.web;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.io.*;
 import java.math.*;
 import java.text.*;
 import java.util.*;
 import java.util.regex.*;
 
 import org.apache.commons.codec.binary.*;
 
 public class ExoneraTorServlet extends HttpServlet {
 
   private void writeHeader(PrintWriter out) throws IOException {
     out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 "
           + "Transitional//EN\"\n"
         + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n"
         + "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n"
         + "  <head>\n"
         + "    <meta content=\"text/html; charset=ISO-8859-1\"\n"
         + "          http-equiv=\"content-type\" />\n"
         + "    <title>ExoneraTor</title>\n"
         + "    <meta http-equiv=Content-Type content=\"text/html; "
           + "charset=iso-8859-1\">\n"
         + "    <link href=\"http://www.torproject.org/"
           + "stylesheet-ltr.css\" type=text/css rel=stylesheet>\n"
         + "    <link href=\"http://www.torproject.org/favicon.ico\" "
           + "type=image/x-icon rel=\"shortcut icon\">\n"
         + "  </head>\n"
         + "  <body>\n"
         + "    <div class=\"center\">\n"
         + "      <table class=\"banner\" border=\"0\" cellpadding=\"0\" "
           + "cellspacing=\"0\" summary=\"\">\n"
         + "        <tr>\n"
         + "          <td class=\"banner-left\"><a "
           + "href=\"https://www.torproject.org/\"><img "
           + "src=\"http://www.torproject.org/images/top-left.png\" "
           + "alt=\"Click to go to home page\" width=\"193\" "
           + "height=\"79\"></a></td>\n"
         + "          <td class=\"banner-middle\">\n"
           + "            <a href=\"/\">Home</a>\n"
           + "            <a href=\"graphs.html\">Graphs</a>\n"
           + "            <a href=\"research.html\">Research</a>\n"
           + "            <a href=\"status.html\">Status</a>\n"
           + "            <br/>\n"
           + "            <font size=\"2\">\n"
           + "              <a class=\"current\">ExoneraTor</a>\n"
          + "              <a href=\"relay-search.html\">Relay Search</a>\n"

           + "              <a href=\"consensus-health.html\">Consensus Health</a>\n"
           + "              <a href=\"log.html\">Last Log</a>\n"
           + "            </font>\n"
           + "          </td>\n"
         + "          <td class=\"banner-right\"></td>\n"
         + "        </tr>\n"
         + "      </table>\n"
         + "      <div class=\"main-column\" style=\"margin:5; "
           + "Padding:0;\">\n"
         + "        <h2>ExoneraTor</h2>\n"
         + "        <h3>or: a website that tells you whether some IP "
           + "address was a Tor relay</h3>\n"
         + "        <p>ExoneraTor tells you whether there was a Tor relay "
           + "running on a given IP address at a given time. ExoneraTor "
           + "can further find out whether this relay permitted exiting "
           + "to a given server and/or TCP port. ExoneraTor learns about "
           + "these facts from parsing the public relay lists and relay "
           + "descriptors that are collected from the Tor directory "
           + "authorities.\n"
         + "        <br/>\n"
         + "        <p><font color=\"red\"><b>Notice:</b> Note that the "
           + "information you are providing below may be leaked to anyone "
           + "who can read the network traffic between you and this web "
           + "server or who has access to this web server. If you need to "
           + "keep the IP addresses and incident times confidential, you "
           + "should download the <a href=\"https://svn.torproject.org/"
             + "svn/projects/archives/trunk/exonerator/\">Java or Python "
             + "version of ExoneraTor</a> and run it on your local "
             + "machine.</font></p>\n"
         + "        <br/>\n");
   }
 
   private void writeFooter(PrintWriter out) throws IOException {
     out.println("        <br/>\n"
         + "      </div>\n"
         + "    </div>\n"
         + "    <div class=\"bottom\" id=\"bottom\">\n"
         + "      <p>This material is supported in part by the National "
           + "Science Foundation under Grant No. CNS-0959138. Any "
           + "opinions, finding, and conclusions or recommendations "
           + "expressed in this material are those of the author(s) and "
           + "do not necessarily reflect the views of the National "
           + "Science Foundation.</p>\n"
         + "      <p>\"Tor\" and the \"Onion Logo\" are <a "
           + "href=\"https://www.torproject.org/trademark-faq.html.en\">"
           + "registered trademarks</a> of The Tor Project, Inc.</p>\n"
         + "      <p>Data on this site is freely available under a <a "
           + "href=\"http://creativecommons.org/publicdomain/zero/1.0/\">"
           + "CC0 no copyright declaration</a>: To the extent possible "
           + "under law, the Tor Project has waived all copyright and "
           + "related or neighboring rights in the data. Graphs are "
           + "licensed under a <a "
           + "href=\"http://creativecommons.org/licenses/by/3.0/us/\">"
           + "Creative Commons Attribution 3.0 United States "
           + "License</a>.</p>\n"
         + "    </div>\n"
         + "  </body>\n"
         + "</html>");
     out.close();
   }
 
   // TODO make this configurable!
   public final String CONSENSUS_DIRECTORY =
       "/srv/metrics.torproject.org/ernie/directory-archive/consensus";
   public final String SERVER_DESCRIPTOR_DIRECTORY =
       "/srv/metrics.torproject.org/ernie/directory-archive/"
       + "server-descriptor";
 
   private static final boolean TEST_MODE = false; // TODO take me out
 
   public void doGet(HttpServletRequest request,
       HttpServletResponse response) throws IOException,
       ServletException {
 
     /* Get print writer and start writing response. We're wrapping the
      * PrintWriter, because we want it to auto-flush as soon as we have
      * written a line. */
     //PrintWriter out = new PrintWriter(response.getWriter(), true);
     PrintWriter out = response.getWriter();
     writeHeader(out);
 
     /* Check if we have a descriptors directory. */
     File consensusDirectory = new File(CONSENSUS_DIRECTORY);
     SortedSet<File> consensusDirectories = new TreeSet<File>();
     if (consensusDirectory.exists() && consensusDirectory.isDirectory()) {
       for (File yearFile : consensusDirectory.listFiles()) {
         for (File monthFile : yearFile.listFiles()) {
           consensusDirectories.add(monthFile);
         }
       }
     }
 
     if (consensusDirectories.isEmpty()) {
       out.println("<p><font color=\"red\"><b>Warning: </b></font>This "
           + "server doesn't have any relay lists available. If this "
           + "problem persists, please "
           + "<a href=\"mailto:tor-assistants@freehaven.net\">let us "
           + "know</a>!</p>\n");
       writeFooter(out);
       return;
     }
     String firstConsensus = new TreeSet<File>(Arrays.asList(
         new TreeSet<File>(Arrays.asList(consensusDirectories.first().
         listFiles())).first().listFiles())).first().getName().substring(0,
         13);
     firstConsensus = firstConsensus.substring(0, 10) + " "
         + firstConsensus.substring(11, 13) + ":00";
     String lastConsensus = new TreeSet<File>(Arrays.asList(
         new TreeSet<File>(Arrays.asList(consensusDirectories.last().
         listFiles())).last().listFiles())).last().getName().substring(0,
         13);
     lastConsensus = lastConsensus.substring(0, 10) + " "
         + lastConsensus.substring(11, 13) + ":00";
 
     out.println("<a id=\"relay\"/><h3>Was there a Tor relay running on "
         + "this IP address?</h3>");
 
     /* Parse IP parameter. */
     Pattern ipAddressPattern = Pattern.compile(
         "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
         "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
         "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
         "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");
     String ipParameter = request.getParameter("ip");
     String relayIP = "", ipWarning = "";
     if (ipParameter != null && ipParameter.length() > 0) {
       Matcher ipParameterMatcher = ipAddressPattern.matcher(ipParameter);
       if (ipParameterMatcher.matches()) {
         String[] ipParts = ipParameter.split("\\.");
         relayIP = Integer.parseInt(ipParts[0]) + "."
             + Integer.parseInt(ipParts[1]) + "."
             + Integer.parseInt(ipParts[2]) + "."
             + Integer.parseInt(ipParts[3]);
       } else {
         ipWarning = "\"" + (ipParameter.length() > 20 ?
             ipParameter.substring(0, 20) + "[...]" :
             ipParameter) + "\" is not a valid IP address.";
       }
     }
 
     /* Parse timestamp parameter. */
     String timestampParameter = request.getParameter("timestamp");
     long timestamp = 0L;
     String timestampStr = "", timestampWarning = "";
     SimpleDateFormat parseTimeFormat = new SimpleDateFormat(
         "yyyy-MM-dd HH:mm");
     parseTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
     if (timestampParameter != null && timestampParameter.length() > 0) {
       try {
         Date parsedTimestamp = parseTimeFormat.parse(timestampParameter);
         if (timestampParameter.compareTo(firstConsensus) >= 0 &&
             timestampParameter.compareTo(lastConsensus) <= 0) {
           timestamp = parsedTimestamp.getTime();
           timestampStr = parseTimeFormat.format(timestamp);
         } else {
           timestampWarning = "Please pick a value between \""
               + firstConsensus + "\" and \"" + lastConsensus + "\".";
         }
       } catch (ParseException e) {
         /* We have no way to handle this exception, other than leaving
            timestampStr at "". */
         timestampWarning = "\"" + (timestampParameter.length() > 20 ?
             timestampParameter.substring(0, 20) + "[...]" :
             timestampParameter) + "\" is not a valid timestamp.";
       }
     }
 
     /* If either IP address or timestamp is provided, the other one must
      * be provided, too. */
     if (relayIP.length() < 1 && timestampStr.length() > 0 &&
         ipWarning.length() < 1) {
       ipWarning = "Please provide an IP address.";
     }
     if (relayIP.length() > 0 && timestampStr.length() < 1 &&
         timestampWarning.length() < 1) {
       timestampWarning = "Please provide a timestamp.";
     }
 
     /* Parse target IP parameter. */
     String targetIP = "", targetPort = "", target = "";
     String[] targetIPParts = null;
     String targetAddrParameter = request.getParameter("targetaddr");
     String targetAddrWarning = "";
     if (targetAddrParameter != null && targetAddrParameter.length() > 0) {
       Matcher targetAddrParameterMatcher =
           ipAddressPattern.matcher(targetAddrParameter);
       if (targetAddrParameterMatcher.matches()) {
         String[] targetAddrParts = targetAddrParameter.split("\\.");
         targetIP = Integer.parseInt(targetAddrParts[0]) + "."
             + Integer.parseInt(targetAddrParts[1]) + "."
             + Integer.parseInt(targetAddrParts[2]) + "."
             + Integer.parseInt(targetAddrParts[3]);
         target = targetIP;
         targetIPParts = targetIP.split("\\.");
       } else {
         targetAddrWarning = "\"" + (targetAddrParameter.length() > 20 ?
             timestampParameter.substring(0, 20) + "[...]" :
             timestampParameter) + "\" is not a valid IP address.";
       }
     }
 
     /* Parse target port parameter. */
     String targetPortParameter = request.getParameter("targetport");
     String targetPortWarning = "";
     if (targetPortParameter != null && targetPortParameter.length() > 0) {
       Pattern targetPortPattern = Pattern.compile("\\d+");
       if (targetPortParameter.length() < 5 &&
           targetPortPattern.matcher(targetPortParameter).matches() &&
           !targetPortParameter.equals("0") &&
           Integer.parseInt(targetPortParameter) < 65536) {
         targetPort = targetPortParameter;
         if (target != null) {
           target += ":" + targetPort;
         } else {
           target = targetPort;
         }
       } else {
         targetPortWarning = "\"" + (targetPortParameter.length() > 8 ?
             targetPortParameter.substring(0, 8) + "[...]" :
             targetPortParameter) + "\" is not a valid TCP port.";
       }
     }
 
     /* If target port is provided, a target address must be provided,
      * too. */
     if (targetPort.length() > 0 && targetIP.length() < 1 &&
         targetAddrWarning.length() < 1) {
       targetAddrWarning = "Please provide an IP address.";
     }
 
     /* Write form with IP address and timestamp. */
     out.println("        <form action=\"exonerator.html#relay\">\n"
         + "          <input type=\"hidden\" name=\"targetaddr\" "
         + (targetIP.length() > 0 ? " value=\"" + targetIP + "\"" : "")
         + "/>\n"
         + "          <input type=\"hidden\" name=\"targetPort\""
         + (targetPort.length() > 0 ? " value=\"" + targetPort + "\"" : "")
         + "/>\n"
         + "          <table>\n"
         + "            <tr>\n"
         + "              <td align=\"right\">IP address in question:</td>\n"
         + "              <td><input type=\"text\" name=\"ip\""
           + (relayIP.length() > 0 ? " value=\"" + relayIP + "\"" :
             (TEST_MODE ? " value=\"209.17.171.104\"" : ""))
           + "\"/>"
           + (ipWarning.length() > 0 ? "<br/><font color=\"red\">"
           + ipWarning + "</font>" : "")
         + "</td>\n"
         + "              <td><i>(Ex.: 1.2.3.4)</i></td>\n"
         + "            </tr>\n"
         + "            <tr>\n"
         + "              <td align=\"right\">Timestamp, in UTC:</td>\n"
         + "              <td><input type=\"text\" name=\"timestamp\""
           + (timestampStr.length() > 0 ? " value=\"" + timestampStr + "\"" :
              (TEST_MODE ? " value=\"2009-08-15 16:05\"" : ""))
           + "\"/>"
           + (timestampWarning.length() > 0 ? "<br/><font color=\"red\">"
               + timestampWarning + "</font>" : "")
         + "</td>\n"
         + "              <td><i>(Ex.: 2010-01-01 12:00)</i></td>\n"
         + "            </tr>\n"
         + "            <tr>\n"
         + "              <td/>\n"
         + "              <td>\n"
         + "                <input type=\"submit\">\n"
         + "                <input type=\"reset\">\n"
         + "              </td>\n"
         + "              <td/>\n"
         + "            </tr>\n"
         + "          </table>\n"
         + "        </form>\n");
 
     if (relayIP.length() < 1 || timestampStr.length() < 1) {
       writeFooter(out);
       return;
     }
 
     /* Look up relevant consensuses. */
     long timestampTooOld = timestamp - 15L * 60L * 60L * 1000L;
     long timestampFrom = timestamp - 3L * 60L * 60L * 1000L;
     long timestampTooNew = timestamp + 12L * 60L * 60L * 1000L;
     Calendar calTooOld = Calendar.getInstance(
         TimeZone.getTimeZone("UTC"));
     Calendar calFrom = Calendar.getInstance(
         TimeZone.getTimeZone("UTC"));
     Calendar calTooNew = Calendar.getInstance(
         TimeZone.getTimeZone("UTC"));
     calTooOld.setTimeInMillis(timestampTooOld);
     calFrom.setTimeInMillis(timestampFrom);
     calTooNew.setTimeInMillis(timestampTooNew);
     out.printf("<p>Looking up IP address %s in the relay lists published "
         + "between %tF %tR and %s. "
         + "Clients could have used any of these relay lists to "
         + "select relays for their paths and build circuits using them. "
         + "You may follow the links to relay lists and relay descriptors "
         + "to grep for the lines printed below and confirm that results "
         + "are correct.<br/>", relayIP, calFrom, calFrom, timestampStr);
     SimpleDateFormat consensusTimeFormat = new SimpleDateFormat(
         "yyyy-MM-dd-HH-mm-ss");
     consensusTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
     String fromTime = consensusTimeFormat.format(
         new Date(timestampTooOld));
     String fromDay = fromTime.substring(0, "yyyy-MM-dd".length());
     String fromMonth = fromDay.substring(0, "yyyy-MM".length());
     String toTime = consensusTimeFormat.format(new Date(timestampTooNew));
     String toDay = toTime.substring(0, "yyyy-MM-dd".length());
     String toMonth = toDay.substring(0, "yyyy-MM".length());
     SortedSet<File> tooOldConsensuses = new TreeSet<File>();
     SortedSet<File> relevantConsensuses = new TreeSet<File>();
     SortedSet<File> tooNewConsensuses = new TreeSet<File>();
     for (File consensusMonth : consensusDirectories) {
       String month = consensusMonth.getParentFile().getName() + "-"
           + consensusMonth.getName();
       if (month.compareTo(fromMonth) < 0 ||
           month.compareTo(toMonth) > 0) {
         continue;
       }
       for (File consensusDay : consensusMonth.listFiles()) {
         String day = month + "-" + consensusDay.getName();
         if (day.compareTo(fromDay) < 0 ||
             day.compareTo(toDay) > 0) {
           continue;
         }
         for (File consensusFile : consensusDay.listFiles()) {
           String time = consensusFile.getName().substring(0,
               "yyyy-MM-dd-HH-mm-ss".length());
           if (time.compareTo(fromTime) < 0 ||
               time.compareTo(toTime) > 0) {
             continue;
           }
           Date consensusDate = null;
           try {
             consensusDate = consensusTimeFormat.parse(time);
           } catch (ParseException e) {
             /* This should never happen. If it does, it's a bug. */
             throw new RuntimeException(e);
           }
           long consensusTime = consensusDate.getTime();
           if (consensusTime >= timestampTooOld &&
               consensusTime < timestampFrom)
             tooOldConsensuses.add(consensusFile);
           else if (consensusTime >= timestampFrom &&
                    consensusTime <= timestamp)
             relevantConsensuses.add(consensusFile);
           else if (consensusTime > timestamp &&
                    consensusTime <= timestampTooNew)
             tooNewConsensuses.add(consensusFile);
         }
       }
     }
     SortedSet<File> allConsensuses = new TreeSet<File>();
     allConsensuses.addAll(tooOldConsensuses);
     allConsensuses.addAll(relevantConsensuses);
     allConsensuses.addAll(tooNewConsensuses);
     if (allConsensuses.isEmpty()) {
       out.println("        <p>No relay lists found!</p>\n"
           + "        <p>Result is INDECISIVE!</p>\n"
           + "        <p>We cannot make any statement whether there was "
           + "a Tor relay running on IP address "
           + relayIP + " at " + timestampStr + "! We "
           + "did not find any relevant relay lists preceding the given "
           + "time. If you think this is an error on our side, please "
           + "<a href=\"mailto:tor-assistants@freehaven.net\">contact "
           + "us</a>!</p>\n");
       writeFooter(out);
       return;
     }
 
     // parse consensuses to find descriptors belonging to the IP address
     SortedSet<File> positiveConsensusesNoTarget = new TreeSet<File>();
     Set<String> addressesInSameNetwork = new HashSet<String>();
     SortedMap<String, Set<File>> relevantDescriptors =
         new TreeMap<String, Set<File>>();
     SimpleDateFormat validAfterTimeFormat = new SimpleDateFormat(
         "yyyy-MM-dd HH:mm:ss");
     validAfterTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
     for (File consensus : allConsensuses) {
       if (relevantConsensuses.contains(consensus)) {
         String validAfterString = consensus.getName().substring(0,
             "yyyy-MM-dd-HH-mm-ss".length());
         Date validAfterDate = null;
         try {
           validAfterDate = consensusTimeFormat.parse(validAfterString);
         } catch (ParseException e) {
           /* This should never happen. If it does, it's a bug. */
           throw new RuntimeException(e);
         }
         long validAfterTime = validAfterDate.getTime();
         String validAfterDatetime = validAfterTimeFormat.format(
             validAfterTime);
         out.println("        <br/><tt>valid-after <b>"
             + "<a href=\"consensus?valid-after="
             + validAfterString + "\" target=\"_blank\">"
             + validAfterDatetime + "</b></a></tt><br/>");
       }
       BufferedReader br = new BufferedReader(new FileReader(consensus));
       String line;
       while ((line = br.readLine()) != null) {
         if (!line.startsWith("r "))
           continue;
         String[] parts = line.split(" ");
         String address = parts[6];
         if (address.equals(relayIP)) {
           String hex = String.format("%040x", new BigInteger(1,
               Base64.decodeBase64(parts[3] + "==")));
           if (!relevantDescriptors.containsKey(hex))
             relevantDescriptors.put(hex, new HashSet<File>());
           relevantDescriptors.get(hex).add(consensus);
           positiveConsensusesNoTarget.add(consensus);
           if (relevantConsensuses.contains(consensus)) {
             out.println("    <tt>r " + parts[1] + " " + parts[2] + " "
                 + "<a href=\"serverdesc?desc-id=" + hex + "\" "
                 + "target=\"_blank\">" + parts[3] + "</a> " + parts[4]
                 + " " + parts[5] + " <b>" + parts[6] + "</b> " + parts[7]
                 + " " + parts[8] + "</tt><br/>");
           }
         } else {
           if (relayIP.startsWith(address.substring(0,
               address.lastIndexOf(".")))) {
             addressesInSameNetwork.add(address);
           }
         }
       }
       br.close();
     }
     if (relevantDescriptors.isEmpty()) {
       out.printf("        <p>None found!</p>\n"
           + "        <p>Result is NEGATIVE with moderate certainty!</p>\n"
           + "        <p>We did not find IP "
           + "address " + relayIP + " in any of the relay lists that were "
           + "published between %tF %tR and %tF %tR.\n\nA possible "
           + "reason for false negatives is that the relay is using a "
           + "different IP address when generating a descriptor than for "
           + "exiting to the Internet. We hope to provide better checks "
           + "for this case in the future.</p>\n", calTooOld, calTooOld,
           calTooNew, calTooNew);
       if (!addressesInSameNetwork.isEmpty()) {
         out.println("        <p>The following other IP addresses of Tor "
             + "relays were found in the mentioned relay lists that "
             + "are in the same /24 network and that could be related to "
             + "IP address " + relayIP + ":</p>\n");
         for (String s : addressesInSameNetwork) {
           out.println("        <p>" + s + "</p>\n");
         }
       }
       writeFooter(out);
       return;
     }
 
     // print out result
     Set<File> matches = positiveConsensusesNoTarget;
     if (matches.contains(relevantConsensuses.last())) {
       out.println("        <p>Result is POSITIVE with high certainty!"
             + "</p>\n"
           + "        <p>We found one or more relays on IP address "
           + relayIP
           + " in the most recent relay list preceding " + timestampStr
           + " that clients were likely to know.</p>\n");
     } else {
       boolean inOtherRelevantConsensus = false,
           inTooOldConsensuses = false,
           inTooNewConsensuses = false;
       for (File f : matches)
         if (relevantConsensuses.contains(f))
           inOtherRelevantConsensus = true;
         else if (tooOldConsensuses.contains(f))
           inTooOldConsensuses = true;
         else if (tooNewConsensuses.contains(f))
           inTooNewConsensuses = true;
       if (inOtherRelevantConsensus) {
         out.println("        <p>Result is POSITIVE "
             + "with moderate certainty!</p>\n");
         out.println("<p>We found one or more relays on IP address "
             + relayIP
             + ", but not in the relay list immediately preceding "
             + timestampStr + ". A possible reason for the relay being "
             + "missing in the last relay list preceding the given time might "
             + "be that some of the directory authorities had difficulties "
             + "connecting to the relay. However, clients might still have "
             + "used the relay.</p>\n");
       } else {
         out.println("        <p>Result is NEGATIVE "
             + "with high certainty!</p>\n");
         out.println("        <p>We did not find any relay on IP address "
             + relayIP
             + " in the relay lists 3 hours preceding " + timestampStr
             + ".</p>\n");
         if (inTooOldConsensuses || inTooNewConsensuses) {
           if (inTooOldConsensuses && !inTooNewConsensuses) {
             out.println("        <p>Note that we found a matching relay in "
                 + "relay lists that were published between 5 and 3 "
                 + "hours before " + timestampStr + ".</p>\n");
           } else if (!inTooOldConsensuses && inTooNewConsensuses) {
             out.println("        <p>Note that we found a matching relay in "
                 + "relay lists that were published up to 2 hours after "
                 + timestampStr + ".</p>\n");
           } else {
             out.println("        <p>Note that we found a matching relay in "
                 + "relay lists that were published between 5 and 3 "
                 + "hours before and in relay lists that were published up "
                 + "to 2 hours after " + timestampStr + ".</p>\n");
           }
           out.println("<p>Make sure that the timestamp you provided is "
               + "in the correct timezone: UTC (or GMT).</p>");
         }
         writeFooter(out);
         return;
       }
     }
 
     /* Second part: target */
     out.println("<br/><a id=\"exit\"/><h3>Was this relay configured to "
         + "permit exiting to a given target?</h3>");
 
     File serverDescriptorDirectory =
         new File(SERVER_DESCRIPTOR_DIRECTORY);
     SortedSet<File> serverDescriptorDirectories = new TreeSet<File>();
     if (serverDescriptorDirectory.exists() &&
         serverDescriptorDirectory.isDirectory()) {
       for (File yearFile : serverDescriptorDirectory.listFiles()) {
         for (File monthFile : yearFile.listFiles()) {
           serverDescriptorDirectories.add(monthFile);
         }
       }
     }
 
     if (serverDescriptorDirectories.isEmpty()) {
       out.println("<p><font color=\"red\"><b>Warning: </b></font>This "
           + "server doesn't have any relay descriptors available. If "
           + "this problem persists, please "
           + "<a href=\"mailto:tor-assistants@freehaven.net\">let us "
           + "know</a>!</p>\n");
       writeFooter(out);
       return;
     }
 
     out.println("        <form action=\"exonerator.html#exit\">\n"
         + "              <input type=\"hidden\" name=\"timestamp\"\n"
         + "                         value=\"" + timestampStr + "\"/>\n"
         + "              <input type=\"hidden\" name=\"ip\" "
           + "value=\"" + relayIP + "\"/>\n"
         + "          <table>\n"
         + "            <tr>\n"
         + "              <td align=\"right\">Target address:</td>\n"
         + "              <td><input type=\"text\" name=\"targetaddr\""
           + (targetIP.length() > 0 ? " value=\"" + targetIP + "\"" :
              (TEST_MODE ? " value=\"209.85.129.104\"" : ""))
           + "\"/>"
           + (targetAddrWarning.length() > 0 ? "<br/><font color=\"red\">"
               + targetAddrWarning + "</font>" : "")
         + "</td>\n"
         + "              <td><i>(Ex.: 4.3.2.1)</i></td>\n"
         + "            </tr>\n"
         + "            <tr>\n"
         + "              <td align=\"right\">Target port:</td>\n"
         + "              <td><input type=\"text\" name=\"targetport\""
           + (targetPort.length() > 0 ? " value=\"" + targetPort + "\"" :
              (TEST_MODE ? " value=\"80\"" : ""))
           + "/>"
           + (targetPortWarning.length() > 0 ? "<br/><font color=\"red\">"
               + targetPortWarning + "</font>" : "")
         + "</td>\n"
         + "              <td><i>(Ex.: 80)</i></td>\n"
         + "            </tr>\n"
         + "            <tr>\n"
         + "              <td/>\n"
         + "              <td>\n"
         + "                <input type=\"submit\">\n"
         + "                <input type=\"reset\">\n"
         + "              </td>\n"
         + "              <td/>\n"
         + "            </tr>\n"
         + "          </table>\n"
         + "        </form>\n");
 
     if (targetIP.length() < 1) {
       writeFooter(out);
       return;
     }
 
     // parse router descriptors to check exit policies
     out.println("<p>Searching the relay descriptors published by the relay "
         + "on IP address " + relayIP + " to find out whether this relay "
         + "permitted exiting to " + target + ". You may follow the links "
         + "above to the relay descriptors and grep them for the lines "
         + "printed below to confirm that results are correct.</p>");
     SortedSet<File> positiveConsensuses = new TreeSet<File>();
     Set<String> missingDescriptors = new HashSet<String>();
     Set<String> descriptors = relevantDescriptors.keySet();
     for (String descriptor : descriptors) {
       for (File directory : serverDescriptorDirectories) {
         File subDirectory = new File(directory.getAbsolutePath() + "/"
             + descriptor.substring(0, 1) + "/"
             + descriptor.substring(1, 2));
         if (subDirectory.exists()) {
           File descriptorFile = new File(subDirectory.getAbsolutePath()
               + "/" + descriptor);
           if (!descriptorFile.exists()) {
             continue;
           }
           missingDescriptors.remove(descriptor);
           BufferedReader br = new BufferedReader(
               new FileReader(descriptorFile));
           String line, routerLine = null, publishedLine = null;
           StringBuilder acceptRejectLines = new StringBuilder();
           boolean foundMatch = false;
           while ((line = br.readLine()) != null) {
             if (line.startsWith("router ")) {
               routerLine = line;
             } else if (line.startsWith("published ")) {
               publishedLine = line;
             } else if (line.startsWith("reject ") ||
                 line.startsWith("accept ")) {
               if (foundMatch) {
                 out.println("<tt> " + line + "</tt><br/>");
                 continue;
               }
               boolean ruleAccept = line.split(" ")[0].equals("accept");
               String ruleAddress = line.split(" ")[1].split(":")[0];
               if (!ruleAddress.equals("*")) {
                 if (!ruleAddress.contains("/") &&
                     !ruleAddress.equals(targetIP)) {
                   acceptRejectLines.append("<tt> " + line + "</tt><br/>\n");
                   continue; // IP address does not match
                 }
                 String[] ruleIPParts = ruleAddress.split("/")[0].
                     split("\\.");
                 int ruleNetwork = ruleAddress.contains("/") ?
                     Integer.parseInt(ruleAddress.split("/")[1]) : 32;
                 for (int i = 0; i < 4; i++) {
                   if (ruleNetwork == 0) {
                     break;
                   } else if (ruleNetwork >= 8) {
                     if (ruleIPParts[i].equals(targetIPParts[i])) {
                       ruleNetwork -= 8;
                     } else {
                       break;
                     }
                   } else {
                     int mask = 255 ^ 255 >>> ruleNetwork;
                     if ((Integer.parseInt(ruleIPParts[i]) & mask) ==
                         (Integer.parseInt(targetIPParts[i]) & mask)) {
                       ruleNetwork = 0;
                     }
                     break;
                   }
                 }
                 if (ruleNetwork > 0) {
                   acceptRejectLines.append("<tt> " + line + "</tt><br/>\n");
                   continue; // IP address does not match
                 }
               }
               String rulePort = line.split(" ")[1].split(":")[1];
               if (targetPort.length() < 1 && !ruleAccept &&
                   !rulePort.equals("*")) {
                 acceptRejectLines.append("<tt> " + line + "</tt><br/>\n");
                 continue; // with no port given, we only consider
                           // reject :* rules as matching
               }
               if (targetPort.length() > 0) {
                 if (!rulePort.equals("*") &&
                     !targetPort.equals(rulePort)) {
                   acceptRejectLines.append("<tt> " + line + "</tt><br/>\n");
                   continue; // ports do not match
                 }
               }
               boolean relevantMatch = false;
               for (File f : relevantDescriptors.get(descriptor))
                 if (relevantConsensuses.contains(f))
                   relevantMatch = true;
               if (relevantMatch) {
                 String[] routerParts = routerLine.split(" ");
                 out.println("<br/><tt>" + routerParts[0] + " "
                     + routerParts[1] + " <b>" + routerParts[2] + "</b> "
                     + routerParts[3] + " " + routerParts[4] + " "
                     + routerParts[5] + "</tt><br/>");
                 String[] publishedParts = publishedLine.split(" ");
                 out.println("<tt>" + publishedParts[0] + " <b>"
                     + publishedParts[1] + " " + publishedParts[2]
                     + "</b></tt><br/>");
                 out.println(acceptRejectLines.toString());
                 out.println("<tt><b>" + line + "</b></tt><br/>");
                 foundMatch = true;
               }
               if (ruleAccept) {
                 positiveConsensuses.addAll(
                     relevantDescriptors.get(descriptor));
               }
             }
           }
           br.close();
         }
       }
     }
 
     // print out result
 // TODO don't repeat results from above
     matches = positiveConsensuses;
     if (matches.contains(relevantConsensuses.last())) {
       out.println("        <p>Result is POSITIVE with high certainty!</p>\n"
           + "        <p>We found one or more relays on IP address "
           + relayIP + " permitting exit to " + target
           + " in the most recent relay list preceding " + timestampStr
           + " that clients were likely to know.</p>\n");
       writeFooter(out);
       return;
     }
     boolean resultIndecisive = target.length() > 0
         && !missingDescriptors.isEmpty();
     if (resultIndecisive) {
       out.println("        <p>Result is INDECISIVE!</p>\n"
           + "        <p>At least one referenced descriptor could not be found. This "
           + "is a rare case, but one that (apparently) happens. We cannot "
           + "make any good statement about exit relays without these "
           + "descriptors. The following descriptors are missing:</p>");
       for (String desc : missingDescriptors)
         out.println("        <p>" + desc + "</p>\n");
     }
     boolean inOtherRelevantConsensus = false, inTooOldConsensuses = false,
         inTooNewConsensuses = false;
     for (File f : matches)
       if (relevantConsensuses.contains(f))
         inOtherRelevantConsensus = true;
       else if (tooOldConsensuses.contains(f))
         inTooOldConsensuses = true;
       else if (tooNewConsensuses.contains(f))
         inTooNewConsensuses = true;
     if (inOtherRelevantConsensus) {
       if (!resultIndecisive) {
         out.println("        <p>Result is POSITIVE "
             + "with moderate certainty!</p>\n");
       }
       out.println("<p>We found one or more relays on IP address "
           + relayIP + " permitting exit to " + target
           + ", but not in the relay list immediately preceding "
           + timestampStr + ". A possible reason for the relay being "
           + "missing in the last relay list preceding the given time might "
           + "be that some of the directory authorities had difficulties "
           + "connecting to the relay. However, clients might still have "
           + "used the relay.</p>\n");
     } else {
       if (!resultIndecisive) {
         out.println("        <p>Result is NEGATIVE "
             + "with high certainty!</p>\n");
       }
       out.println("        <p>We did not find any relay on IP address "
           + relayIP + " permitting exit to " + target
           + " in the relay list 3 hours preceding " + timestampStr
           + ".</p>\n");
       if (inTooOldConsensuses || inTooNewConsensuses) {
         if (inTooOldConsensuses && !inTooNewConsensuses)
           out.println("        <p>Note that we found a matching relay in "
               + "relay lists that were published between 5 and 3 "
               + "hours before " + timestampStr + ".</p>\n");
         else if (!inTooOldConsensuses && inTooNewConsensuses)
           out.println("        <p>Note that we found a matching relay in "
               + "relay lists that were published up to 2 hours after "
               + timestampStr + ".</p>\n");
         else
           out.println("        <p>Note that we found a matching relay in "
               + "relay lists that were published between 5 and 3 "
               + "hours before and in relay lists that were published up "
               + "to 2 hours after " + timestampStr + ".</p>\n");
         out.println("<p>Make sure that the timestamp you provided is "
             + "in the correct timezone: UTC (or GMT).</p>");
       }
     }
     if (target != null) {
       if (positiveConsensuses.isEmpty() &&
           !positiveConsensusesNoTarget.isEmpty())
         out.println("        <p>Note that although the found relay(s) did "
             + "not permit exiting to " + target + ", there have been one "
             + "or more relays running at the given time.</p>");
     }
 
     /* Finish writing response. */
     writeFooter(out);
   }
 }
 
