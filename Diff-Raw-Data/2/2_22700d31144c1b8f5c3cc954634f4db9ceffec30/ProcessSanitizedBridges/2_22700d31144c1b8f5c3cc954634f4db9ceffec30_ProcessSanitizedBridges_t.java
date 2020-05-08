 import java.io.*;
 import java.text.*;
 import java.util.*;
 import org.apache.commons.codec.binary.*;
 
 public class ProcessSanitizedBridges {
   public static void main(String[] args) throws IOException,
       ParseException {
 
     /* Validate command-line arguments. */
     if (args.length != 1 || !new File(args[0]).exists()) {
       System.out.println("Usage: java ProcessSanitizedBridges <dir>");
       System.exit(1);
     }
 
     /* Find all files that we should parse. Somewhat fragile, but should
      * work. */
     System.out.println("Creating list of files we should parse.");
     SortedMap<String, File> statuses = new TreeMap<String, File>();
     SortedMap<String, File> serverDescriptors =
         new TreeMap<String, File>();
     SortedMap<String, File> extraInfoDescriptors =
         new TreeMap<String, File>();
     Stack<File> files = new Stack<File>();
     files.add(new File(args[0]));
     while (!files.isEmpty()) {
       File file = files.pop();
       String path = file.getAbsolutePath();
       String filename = file.getName();
       if (file.isDirectory()) {
         files.addAll(Arrays.asList(file.listFiles()));
       } else if (path.contains("statuses")) {
         statuses.put(filename, file);
       } else if (path.contains("server-descriptors")) {
         serverDescriptors.put(filename, file);
       } else if (path.contains("extra-infos")) {
         extraInfoDescriptors.put(filename, file);
       }
     }
     System.out.println("We found\n  " + statuses.size() + " statuses,\n  "
         + serverDescriptors.size() + " server descriptors, and\n  "
         + extraInfoDescriptors.size() + " extra-info descriptors.");
 
     /* Parse statuses. */
     if (!statuses.isEmpty()) {
       System.out.println("Parsing statuses.");
       List<String> knownFlags = new ArrayList<String>(Arrays.asList(
           ("Authority,BadExit,BadDirectory,Exit,Fast,Guard,Named,Stable,"
           + "Running,Valid,V2Dir").split(",")));
       BufferedWriter bw = new BufferedWriter(new FileWriter(
           "statuses.csv"));
       bw.write("status,fingerprint,descriptor,published,address,orport,"
           + "dirport");
       for (String knownFlag : knownFlags) {
         bw.write("," + knownFlag.toLowerCase());
       }
       bw.write("\n");
       int parsedStatuses = 0, totalStatuses = statuses.size(),
           writtenOutputLines = 1;
       long started = System.currentTimeMillis();
       for (File file : statuses.values()) {
         String filename = file.getName();
         if (filename.length() != ("20110101-000703-"
             + "4A0CCD2DDC7995083D73F5D667100C8A5831F16D").length()) {
           System.out.println("Status filename has wrong length: '"
               + filename + "' Please check. Exiting.");
           System.exit(1);
         }
         String statusDateTime = filename.substring(0, 4) + "-"
             + filename.substring(4, 6) + "-" + filename.substring(6, 8)
             + " " + filename.substring(9, 11) + ":"
             + filename.substring(11, 13) + ":"
             + filename.substring(13, 15);
         BufferedReader br = new BufferedReader(new FileReader(file));
         String line;
         while ((line = br.readLine()) != null) {
           if (line.startsWith("r ")) {
             String[] parts = line.split(" ");
             if (parts.length != 9) {
               System.out.println("r line doesn't have the correct number "
                   + "of entries: '" + line + "'. Please check. Exiting.");
               System.exit(1);
             }
             String fingerprint = Hex.encodeHexString(Base64.decodeBase64(
                 parts[2] + "="));
             String descriptor = Hex.encodeHexString(Base64.decodeBase64(
                parts[3] + "="));
             String published = parts[4] + " " + parts[5];
             String address = parts[6];
             String orPort = parts[7];
             String dirPort = parts[8];
             bw.write(statusDateTime + "," + fingerprint + "," + descriptor
                 + "," + published + "," + address + "," + orPort + ","
                 + dirPort);
           } else if (line.equals("s") || line.startsWith("s ")) {
             String flags = line.substring(1);
             for (String flag : knownFlags) {
               if (flags.contains(" " + flag)) {
                 bw.write(",TRUE");
               } else {
                 bw.write(",FALSE");
               }
             }
             bw.write("\n");
             writtenOutputLines++;
           }
         }
         br.close();
         parsedStatuses++;
         if (parsedStatuses % (totalStatuses / 10) == 0) {
           double fractionDone = (double) (parsedStatuses) /
               (double) totalStatuses;
           double fractionLeft = 1.0D - fractionDone;
           long now = System.currentTimeMillis();
           double millisLeft = ((double) (now - started)) * fractionLeft /
               fractionDone;
           long secondsLeft = (long) millisLeft / 1000L;
           System.out.println("  " + (parsedStatuses / (totalStatuses
               / 10)) + "0% done, " + secondsLeft + " seconds left.");
         }
       }
       bw.close();
       System.out.println("Parsed " + parsedStatuses + " statuses and "
           + "wrote " + writtenOutputLines + " lines to statuses.csv.");
     }
 
     /* Parse server descriptors and extra-info descriptors. */
     if (!serverDescriptors.isEmpty()) {
       System.out.println("Parsing server descriptors and extra-info "
           + "descriptors.");
       List<String> knownCountries = new ArrayList<String>(Arrays.asList(
           ("?? A1 A2 AD AE AF AG AI AL AM AN AO AP AQ AR AS AT AU AW AX "
           + "AZ BA BB BD BE BF BG BH BI BJ BM BN BO BR BS BT BV BW BY BZ "
           + "CA CD CF CG CH CI CK CL CM CN CO CR CS CU CV CY CZ DE DJ DK "
           + "DM DO DZ EC EE EG ER ES ET EU FI FJ FK FM FO FR GA GB GD GE "
           + "GF GG GH GI GL GM GN GP GQ GR GT GU GW GY HK HN HR HT HU ID "
           + "IE IL IM IN IO IQ IR IS IT JE JM JO JP KE KG KH KI KM KN KP "
           + "KR KW KY KZ LA LB LC LI LK LR LS LT LU LV LY MA MC MD ME MF "
           + "MG MH MK ML MM MN MO MP MQ MR MS MT MU MV MW MX MY MZ NA NC "
           + "NE NF NG NI NL NO NP NR NU NZ OM PA PE PF PG PH PK PL PM PR "
           + "PS PT PW PY QA RE RO RS RU RW SA SB SC SD SE SG SH SI SJ SK "
           + "SL SM SN SO SR ST SV SY SZ TC TD TG TH TJ TK TL TM TN TO TR "
           + "TT TV TW TZ UA UG UM US UY UZ VA VC VE VG VI VN VU WF WS YE "
           + "YT ZA ZM ZW").toLowerCase().split(" ")));
       BufferedWriter bw = new BufferedWriter(new FileWriter(
           "descriptors.csv"));
       bw.write("descriptor,fingerprint,published,address,orport,dirport,"
           + "version,platform,uptime,bridgestatsend,bridgestatsseconds");
       for (String country : knownCountries) {
         bw.write("," + country);
       }
       bw.write(",bridgestatscountries,bridgestatstotal\n");
       int parsedServerDescriptors = 0, parsedExtraInfoDescriptors = 0,
           parsedGeoipStats = 0, skippedGeoipStats = 0,
           parsedBridgeStats = 0,
           totalServerDescriptors = serverDescriptors.size(),
           writtenOutputLines = 1;
       SimpleDateFormat timeFormat = new SimpleDateFormat(
           "yyyy-MM-dd HH:mm:ss");
       timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
       long started = System.currentTimeMillis();
       for (File file : serverDescriptors.values()) {
         String filename = file.getName();
         BufferedReader br = new BufferedReader(new FileReader(file));
         String line, fingerprint = null, published = null, address = null,
             orPort = null, dirPort = null, version = null,
             platform = null, uptime = null, extraInfoDigest = null,
             bridgeStatsEnd = null, bridgeStatsSeconds = null;
         SortedMap<String, String> bridgeStatsIps =
             new TreeMap<String, String>();
         long bridgeStatsTotal = 0L;
         while ((line = br.readLine()) != null) {
           if (line.startsWith("opt ")) {
             line = line.substring(4);
           }
           if (line.startsWith("router ")) {
             String[] parts = line.split(" ");
             address = parts[2];
             orPort = parts[3];
             dirPort = parts[4];
           } else if (line.startsWith("platform ")) {
             version = line.split(" ")[2];
             platform = line.substring(line.indexOf("on ")
                 + "on ".length());
             if (platform.contains("Windows")) {
               platform = "Windows";
             } else if (platform.contains("Linux")) {
               platform = "Linux";
             } else if (platform.contains("Darwin")) {
               platform = "Mac OS X";
             } else if (platform.contains("BSD")) {
               platform = "*BSD";
             } else {
               platform = "Other";
             }
           } else if (line.startsWith("published ")) {
             String[] parts = line.split(" ");
             published = parts[1] + " " + parts[2];
           } else if (line.startsWith("fingerprint ")) {
             fingerprint = line.substring("fingerprint".length()).
                 replaceAll(" ", "").toLowerCase();
           } else if (line.startsWith("uptime ")) {
             uptime = line.split(" ")[1];
           } else if (line.startsWith("extra-info-digest ")) {
             extraInfoDigest = line.substring("extra-info-digest ".
                 length()).toLowerCase();
             if (extraInfoDescriptors.containsKey(extraInfoDigest)) {
               parsedExtraInfoDescriptors++;
               BufferedReader br2 = new BufferedReader(new FileReader(
                   extraInfoDescriptors.get(extraInfoDigest)));
               String geoipStartTime = null, bridgeStatsEndLine = null;
               while ((line = br2.readLine()) != null) {
                 if (line.startsWith("geoip-start-time ")) {
                   geoipStartTime = line.substring("geoip-start-time ".
                       length());
                 } else if (line.startsWith("geoip-client-origins ") &&
                     line.split(" ").length > 1 && published != null &&
                     geoipStartTime != null) {
                   if (version.startsWith("0.2.2.")) {
                     skippedGeoipStats++;
                   } else {
                     parsedGeoipStats++;
                     bridgeStatsEnd = published;
                     bridgeStatsSeconds = "" +
                         + (timeFormat.parse(published).getTime()
                         - timeFormat.parse(geoipStartTime).getTime())
                         / 1000L;
                     for (String pair : line.split(" ")[1].split(",")) {
                       String country = pair.substring(0, 2);
                       String ips = pair.substring(3);
                       bridgeStatsIps.put(country, ips);
                       bridgeStatsTotal += Long.parseLong(ips);
                     }
                   }
                 } else if (line.startsWith("bridge-stats-end ")) {
                   bridgeStatsEndLine = line;
                 } else if (line.startsWith("bridge-ips ") &&
                     line.length() > "bridge-ips ".length() &&
                     bridgeStatsEndLine != null) {
                   parsedBridgeStats++;
                   String[] parts = bridgeStatsEndLine.split(" ");
                   bridgeStatsEnd = parts[1] + " " + parts[2];
                   bridgeStatsSeconds = parts[3].substring(1);
                   for (String pair : line.split(" ")[1].split(",")) {
                     String country = pair.substring(0, 2);
                     String ips = pair.substring(3);
                     bridgeStatsIps.put(country, ips);
                     bridgeStatsTotal += Long.parseLong(ips);
                   }
                 }
               }
               br2.close();
             }
           }
         }
         br.close();
         if (fingerprint == null || published == null || address == null ||
             orPort == null || dirPort == null || version == null ||
             platform == null || uptime == null) {
           System.out.println("Server descriptor " + filename + " is "
               + "missing critical information. Please check. Exiting.");
           System.exit(1);
         }
         bw.write(filename + "," + fingerprint + "," + published + ","
             + address + "," + orPort + "," + dirPort + "," + version + ","
             + platform + "," + uptime);
         if (bridgeStatsEnd != null) {
           bw.write("," + bridgeStatsEnd + "," + bridgeStatsSeconds);
           int bridgeStatsCountries = bridgeStatsIps.size();
           for (String country : knownCountries) {
             if (bridgeStatsIps.containsKey(country)) {
               bw.write("," + bridgeStatsIps.remove(country));
             } else {
               bw.write(",0");
             }
           }
           if (!bridgeStatsIps.isEmpty()) {
             StringBuilder message = new StringBuilder();
             for (String country : bridgeStatsIps.keySet()) {
               message.append(", " + country);
             }
             System.out.println("Unknown " + (bridgeStatsIps.size() == 1 ?
                 "country" : "countries") + " " + message.toString().
                 substring(2) + " in extra-info descriptor "
                 + extraInfoDigest + ". Please check. Exiting.");
             System.exit(1);
           }
           bw.write("," + bridgeStatsCountries + "," + bridgeStatsTotal
               + "\n");
         } else {
           bw.write(",NA,NA");
           for (String country : knownCountries) {
             bw.write(",NA");
           }
           bw.write(",NA,NA\n");
         }
         writtenOutputLines++;
         parsedServerDescriptors++;
         if (parsedServerDescriptors % (totalServerDescriptors / 100)
             == 0) {
           double fractionDone = (double) (parsedServerDescriptors) /
               (double) totalServerDescriptors;
           double fractionLeft = 1.0D - fractionDone;
           long now = System.currentTimeMillis();
           double millisLeft = ((double) (now - started)) * fractionLeft /
               fractionDone;
           long secondsLeft = (long) millisLeft / 1000L;
           System.out.println("  " + (parsedServerDescriptors /
               (totalServerDescriptors / 100)) + "% done, " + secondsLeft
               + " seconds left.");
         }
       }
       bw.close();
       System.out.println("Parsed " + parsedServerDescriptors + " server "
            + "descriptors and " + parsedExtraInfoDescriptors
            + " extra-info descriptors.\nParsed " + parsedGeoipStats
            + " geoip-stats and " + parsedBridgeStats + " bridge-stats.\n"
            + "Skipped " + skippedGeoipStats + " broken geoip-stats of "
            + "0.2.2.x bridges.\nWrote " + writtenOutputLines + " to "
            + "descriptors.csv.");
     }
 
     /* This is it. */
     System.out.println("Terminating.");
   }
 }
 
