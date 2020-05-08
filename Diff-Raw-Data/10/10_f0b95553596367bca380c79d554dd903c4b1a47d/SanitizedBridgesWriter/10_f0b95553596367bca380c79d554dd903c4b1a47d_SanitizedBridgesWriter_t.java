 import java.io.*;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.logging.*;
 
 import org.apache.commons.codec.DecoderException;
 import org.apache.commons.codec.digest.*;
 import org.apache.commons.codec.binary.*;
 
 /**
  * Sanitizes bridge descriptors, i.e., removes all possibly sensitive
  * information from them, and writes them to a local directory structure.
  * During the sanitizing process, all information about the bridge
  * identity or IP address are removed or replaced. The goal is to keep the
  * sanitized bridge descriptors useful for statistical analysis while not
  * making it easier for an adversary to enumerate bridges.
  *
  * There are three types of bridge descriptors: bridge network statuses
  * (lists of all bridges at a given time), server descriptors (published
  * by the bridge to advertise their capabilities), and extra-info
  * descriptors (published by the bridge, mainly for statistical analysis).
  *
  * Network statuses, server descriptors, and extra-info descriptors are
  * linked via descriptor digests: extra-info descriptors are referenced
  * from server descriptors, and server descriptors are referenced from
  * network statuses. These references need to be changed during the
  * sanitizing process, because descriptor contents change and so do the
  * descriptor digests. Furthermore, extra-info descriptors require either
  * the network status or server descriptor to be parsed first to learn the
  * bridge's country code that is part of its new nickname.
  *
  * As a result, there is no possible order in which bridge descriptors can
  * be parsed without having to update a previously written bridge
  * descriptor. The approach taken here is to sanitize bridge descriptors
  * even with incomplete knowledge about references or country codes and to
  * update them as soon as these information get known. We are keeping a
  * persistent data structure, the bridge descriptor mapping, to hold
  * information about every single descriptor. The idea is that every
  * descriptor is (a) referenced from a network status and consists of
  * (b) a server descriptor and (c) an extra-info descriptor, both of which
  * are published at the same time. Using this data structure, we can
  * repair references as soon as we learn more about the descriptor and
  * regardless of the order of incoming bridge descriptors.
  *
  * The process of sanitizing a bridge descriptor is as follows, depending
  * on the type of descriptor:
  *
  * Network statuses are processed by sanitizing every r line separately
  * and looking up whether the descriptor mapping contains a bridge with
  * given identity hash and descriptor publication time. If either server
  * descriptor or extra-info descriptor have been published before and if
  * the GeoIP lookup of the bridge's IP address reveals a new country code
  * for this bridge, extra-info descriptor and server descriptor are
  * re-written.
  *
  * Server descriptors are processed by looking up their bridge identity
  * hash and publication time in the descriptor mapping. If the GeoIP
  * lookup reveals a new country code and if the extra-info descriptor was
  * parsed before, the extra-info descriptor is re-written. After
  * sanitizing a server descriptor, its publication time is noted down, so
  * that all network statuses that might be referencing this server
  * descriptor can be re-written at the end of the sanitizing procedure.
  *
  * Extra-info descriptors are also processed by looking up their bridge
  * identity hash and publication time in the descriptor mapping. If the
  * corresponding server descriptor was sanitized before, it is re-written
  * to include the new extra-info descriptor digest. The publication time
  * is noted down, too, so that all network statuses possibly referencing
  * this extra-info descriptor and its corresponding server descriptor can
  * be re-written at the end of the sanitizing procedure.
  *
  * After sanitizing all bridge descriptors, the network statuses that
  * might be referencing server descriptors which have been (re-)written
  * during this execution are re-written, too. This may be necessary in
  * order to update previously broken references to server descriptors.
  */
 public class SanitizedBridgesWriter {
 
   /**
    * Hex representation of null reference that is written to bridge
    * descriptors if we don't have the real reference, yet.
    */
   private static final String NULL_REFERENCE =
       "0000000000000000000000000000000000000000";
 
   /**
    * Mapping between a descriptor as referenced from a network status to
    * a country code and the digests of server descriptor and extra-info
    * descriptor.
    */
   private static class DescriptorMapping {
 
     /**
      * Creates a new mapping from comma-separated values as read from the
      * persistent mapping file.
      */
     private DescriptorMapping(String commaSeparatedValues) {
       String[] parts = commaSeparatedValues.split(",");
       this.hashedBridgeIdentity = parts[0];
       this.published = parts[1];
       this.countryCode = parts[2];
       this.serverDescriptorIdentifier = parts[3];
       this.extraInfoDescriptorIdentifier = parts[4];
     }
 
     /**
      * Creates a new mapping for a given identity hash and descriptor
      * publication time that has ZZ as country code and all 0's as
      * descriptor digests.
      */
     private DescriptorMapping(String hashedBridgeIdentity,
         String published) {
       this.hashedBridgeIdentity = hashedBridgeIdentity;
       this.published = published;
       this.countryCode = "ZZ";
       this.serverDescriptorIdentifier = NULL_REFERENCE;
       this.extraInfoDescriptorIdentifier = NULL_REFERENCE;
     }
     private String hashedBridgeIdentity;
     private String published;
     private String countryCode;
     private String serverDescriptorIdentifier;
     private String extraInfoDescriptorIdentifier;
 
     /**
      * Returns a string representation of this descriptor mapping that can
      * be written to the persistent mapping file.
      */
     public String toString() {
       return this.hashedBridgeIdentity + "," + this.published + ","
       + this.countryCode + "," + this.serverDescriptorIdentifier + ","
       + this.extraInfoDescriptorIdentifier;
     }
   }
 
   /**
    * File containing the mapping between network status entries, server
    * descriptors, and extra-info descriptors.
    */
   private File bridgeDescriptorMappingsFile;
 
   /**
    * Mapping between status entries, server descriptors, and extra-info
    * descriptors. This mapping is required to re-establish the references
    * from status entries to server descriptors and from server descriptors
    * to extra-info descriptors. The original references are broken when
    * sanitizing, because descriptor contents change and so do the
    * descriptor digests that are used for referencing. Map key contains
    * hashed bridge identity and descriptor publication time, map value
    * contains map key plus country code, new server descriptor identifier,
    * and new extra-info descriptor identifier.
    */
   private SortedMap<String, DescriptorMapping> bridgeDescriptorMappings;
 
   /**
    * GeoIP database used for resolving bridge IP addresses to two-letter
    * country codes.
    */
   private GeoIPDatabaseManager gd;
 
   /**
    * Logger for this class.
    */
   private Logger logger;
 
   /**
    * Publication times of server descriptors and extra-info descriptors
    * parsed in the current execution. These times are used to determine
    * which statuses need to be rewritten at the end of the execution.
    */
   private SortedSet<String> descriptorPublicationTimes;
 
   /**
    * Output directory for writing sanitized bridge descriptors.
    */
   private String sanitizedBridgesDir;
 
   /**
    * Initializes this class, including reading in the known descriptor
    * mapping.
    */
   public SanitizedBridgesWriter(GeoIPDatabaseManager gd, String dir) {
 
     /* Memorize argument values. */
     this.gd = gd;
     this.sanitizedBridgesDir = dir;
 
     /* Initialize logger. */
     this.logger = Logger.getLogger(
         SanitizedBridgesWriter.class.getName());
 
     /* Initialize data structure. */
     this.bridgeDescriptorMappings = new TreeMap<String,
         DescriptorMapping>();
     this.descriptorPublicationTimes = new TreeSet<String>();
 
     /* Read known descriptor mappings from disk. */
     this.bridgeDescriptorMappingsFile = new File(
         "stats/bridge-descriptor-mappings");
     if (this.bridgeDescriptorMappingsFile.exists()) {
       try {
         BufferedReader br = new BufferedReader(new FileReader(
             this.bridgeDescriptorMappingsFile));
         String line = null;
         while ((line = br.readLine()) != null) {
           if (line.split(",").length == 5) {
             String[] parts = line.split(",");
             DescriptorMapping dm = new DescriptorMapping(line);
             dm.hashedBridgeIdentity = parts[0];
             dm.published = parts[1];
             dm.countryCode = parts[2];
             dm.serverDescriptorIdentifier = parts[3];
             dm.extraInfoDescriptorIdentifier = parts[4];
             this.bridgeDescriptorMappings.put(line.split(",")[0] + ","
                 + line.split(",")[1], dm);
           } else {
             this.logger.warning("Corrupt line '" + line + "' in "
                 + this.bridgeDescriptorMappingsFile.getAbsolutePath()
                 + ". Skipping.");
             continue;
           }
         }
         br.close();
       } catch (IOException e) {
         this.logger.log(Level.WARNING, "Could not read in "
             + this.bridgeDescriptorMappingsFile.getAbsolutePath()
             + ".");
         return;
       }
     }
   }
 
   /**
    * Sanitizes a network status and writes it to disk. Processes every r
    * line separately and looks up whether the descriptor mapping contains
    * a bridge with given identity hash and descriptor publication time. If
    * either server descriptor or extra-info descriptor have been published
    * before and if the GeoIP lookup of the bridge's IP address reveals a
    * new country code for this bridge, extra-info descriptor and server
    * descriptor are re-written.
    */
   public void sanitizeAndStoreNetworkStatus(byte[] data,
       String publicationTime) {
 
     /* Parse the given network status line by line. */
     StringBuilder scrubbed = new StringBuilder();
     try {
       BufferedReader br = new BufferedReader(new StringReader(new String(
           data, "US-ASCII")));
       String line = null;
       while ((line = br.readLine()) != null) {
 
         /* r lines contain sensitive information that needs to be removed
          * or replaced. */
         if (line.startsWith("r ")) {
 
           /* Parse the relevant parts of this r line. */
           String[] parts = line.split(" ");
           String bridgeIdentity = parts[2];
           String descPublicationTime = parts[4] + " " + parts[5];
           String ipAddress = parts[6];
           String orPort = parts[7];
           String dirPort = parts[8];
 
           /* Look up the descriptor in the descriptor mapping, or add a
            * new mapping entry if there is none. */
           String hashedBridgeIdentityHex = Hex.encodeHexString(
               DigestUtils.sha(Base64.decodeBase64(bridgeIdentity
               + "=="))).toLowerCase();
           String mappingKey = hashedBridgeIdentityHex + ","
               + descPublicationTime;
           DescriptorMapping mapping = null;
           if (this.bridgeDescriptorMappings.containsKey(mappingKey)) {
             mapping = this.bridgeDescriptorMappings.get(mappingKey);
           } else {
             mapping = new DescriptorMapping(hashedBridgeIdentityHex.
                 toLowerCase(), descPublicationTime);
             this.bridgeDescriptorMappings.put(mappingKey, mapping);
           }
 
           /* Look up the bridge's IP address in the GeoIP database. */
           String newCountryCode = this.gd.getCountryForIPOneWeek(
               ipAddress, descPublicationTime);
 
           /* If we just learned a new IP address, we might have to
            * re-write the (indirectly) referenced extra-info descriptor
            * that has UnnamedZZ as its nickname and the corresponding
            * server descriptor that gets an updated extra-info-digest
            * line. */
           if (!newCountryCode.equals(mapping.countryCode)) {
             mapping.countryCode = newCountryCode;
             if (!mapping.extraInfoDescriptorIdentifier.equals(
                 NULL_REFERENCE)) {
               this.rewriteExtraInfoDescriptor(mapping);
             }
             if (!mapping.serverDescriptorIdentifier.equals(
                 NULL_REFERENCE)) {
               this.rewriteServerDescriptor(mapping);
             }
           }
 
           /* Write scrubbed r line to buffer. */
           String nickname = "Unnamed" + mapping.countryCode;
           String hashedBridgeIdentityBase64 = Base64.encodeBase64String(
               DigestUtils.sha(Base64.decodeBase64(bridgeIdentity
               + "=="))).substring(0, 27);
           String sdi = Base64.encodeBase64String(Hex.decodeHex(
                 mapping.serverDescriptorIdentifier.toCharArray())).
                 substring(0, 27);
           scrubbed.append("r " + nickname + " "
               + hashedBridgeIdentityBase64 + " " + sdi + " "
               + descPublicationTime + " 127.0.0.1 " + orPort + " "
               + dirPort + "\n");
 
         /* Nothing special about s lines; just copy them. */
         } else if (line.startsWith("s ")) {
           scrubbed.append(line + "\n");
 
         /* There should be nothing else but r and s lines in the network
          * status. If there is, we should probably learn before writing
          * anything to the sanitized descriptors. */
         } else {
           this.logger.warning("Unknown line '" + line + "' in bridge "
               + "network status. Not writing to disk!");
           return;
         }
       }
       br.close();
 
     } catch (IOException e) {
       this.logger.log(Level.WARNING, "Could not parse bridge network "
           + "status.", e);
       return;
     } catch (DecoderException e) {
       this.logger.log(Level.WARNING, "Could not parse bridge network "
           + "status.", e);
       return;
     }
 
     /* Write the sanitized network status to disk. */
     try {
 
       /* Determine file name. */
       String syear = publicationTime.substring(0, 4);
       String smonth = publicationTime.substring(5, 7);
       String sday = publicationTime.substring(8, 10);
       String stime = publicationTime.substring(11, 13)
           + publicationTime.substring(14, 16)
           + publicationTime.substring(17, 19);
       File statusFile = new File(this.sanitizedBridgesDir + "/" + syear
           + "/" + smonth + "/statuses/" + sday + "/" + syear + smonth
           + sday + "-" + stime + "-"
           + "4A0CCD2DDC7995083D73F5D667100C8A5831F16D");
 
       /* Create all parent directories to write this network status. */
       statusFile.getParentFile().mkdirs();
 
       /* Write sanitized network status to disk. */
       BufferedWriter bw = new BufferedWriter(new FileWriter(statusFile));
       bw.write(scrubbed.toString());
       bw.close();
 
     } catch (IOException e) {
       this.logger.log(Level.WARNING, "Could not write sanitized bridge "
           + "network status to disk.", e);
       return;
     }
   }
 
   /**
    * Sanitizes a bridge server descriptor and writes it to disk. Looks up
    * up bridge identity hash and publication time in the descriptor
    * mapping. If the GeoIP lookup reveals a new country code and if the
    * corresponding extra-info descriptor was parsed before, the extra-info
    * descriptor is re-written. After sanitizing a server descriptor, its
    * publication time is noted down, so that all network statuses that
    * might be referencing this server descriptor can be re-written at the
    * end of the sanitizing procedure.
    */
   public void sanitizeAndStoreServerDescriptor(byte[] data) {
 
     /* Parse descriptor to generate a sanitized version and to look it up
      * in the descriptor mapping. */
     String scrubbedDesc = null;
     DescriptorMapping mapping = null;
     try {
       BufferedReader br = new BufferedReader(new StringReader(
           new String(data, "US-ASCII")));
       StringBuilder scrubbed = new StringBuilder();
       String line = null, ipAddress = null, hashedBridgeIdentity = null,
           published = null;
       boolean skipCrypto = false, contactWritten = false;
       while ((line = br.readLine()) != null) {
 
         /* When we have parsed both published and fingerprint line, look
          * up descriptor in the descriptor mapping or create a new one if
          * there is none. */
         if (mapping == null && published != null &&
             hashedBridgeIdentity != null) {
           String mappingKey = hashedBridgeIdentity + "," + published;
           if (this.bridgeDescriptorMappings.containsKey(mappingKey)) {
             mapping = this.bridgeDescriptorMappings.get(mappingKey);
           } else {
             mapping = new DescriptorMapping(hashedBridgeIdentity,
                 published);
             this.bridgeDescriptorMappings.put(mappingKey, mapping);
           }
 
           /* Look up IP address in the GeoIP database. If our knowledge
            * about the bridge's country code has changed, we might have to
            * re-write the extra-info descriptor corresponding to this
            * server descriptor. */
           String newCountryCode = this.gd.getCountryForIPOneWeek(ipAddress,
               published);
           if (!newCountryCode.equals(mapping.countryCode)) {
             mapping.countryCode = newCountryCode;
             if (!mapping.extraInfoDescriptorIdentifier.equals(
                 NULL_REFERENCE)) {
               this.rewriteExtraInfoDescriptor(mapping);
             }
           }
         }
 
         /* Skip all crypto parts that might be used to derive the bridge's
          * identity fingerprint. */
         if (skipCrypto && !line.startsWith("-----END ")) {
           continue;
 
         /* Parse the original IP address for looking it up in the GeoIP
          * database and replace it with 127.0.0.1 in the scrubbed
          * version. */
         } else if (line.startsWith("router ")) {
           ipAddress = line.split(" ")[2];
           scrubbed = new StringBuilder("127.0.0.1 " + line.split(" ")[3]
               + " " + line.split(" ")[4] + " " + line.split(" ")[5]
               + "\n");
 
         /* Parse the publication time and add it to the list of descriptor
          * publication times to re-write network statuses at the end of
          * the sanitizing procedure. */
         } else if (line.startsWith("published ")) {
           published = line.substring("published ".length());
           this.descriptorPublicationTimes.add(published);
           scrubbed.append(line + "\n");
 
         /* Parse the fingerprint to determine the hashed bridge
          * identity. */
         } else if (line.startsWith("opt fingerprint ")) {
           String fingerprint = line.substring(line.startsWith("opt ") ?
               "opt fingerprint".length() : "fingerprint".length()).
               replaceAll(" ", "").toLowerCase();
           hashedBridgeIdentity = DigestUtils.shaHex(Hex.decodeHex(
               fingerprint.toCharArray())).toLowerCase();
           scrubbed.append("opt fingerprint");
           for (int i = 0; i < hashedBridgeIdentity.length() / 4; i++)
             scrubbed.append(" " + hashedBridgeIdentity.substring(4 * i,
                 4 * (i + 1)).toUpperCase());
           scrubbed.append("\n");
 
         /* Replace the contact line (if present) with a generic line that
          * contains the bridge's country code as last two characters. */
         } else if (line.startsWith("contact ")) {
           scrubbed.append("contact somebody at example dot "
               + mapping.countryCode.toLowerCase() + "\n");
           contactWritten = true;
 
         /* When we reach the signature, we're done. Write the sanitized
          * descriptor to disk below. */
         } else if (line.startsWith("router-signature")) {
           scrubbedDesc = "router Unnamed"
               + mapping.countryCode.toUpperCase() + " "
               + scrubbed.toString();
           break;
 
         /* Replace extra-info digest with the one we know from our
          * descriptor mapping (which might be all 0's if we didn't parse
          * the extra-info descriptor before). */
         } else if (line.startsWith("opt extra-info-digest ")) {
           scrubbed.append("opt extra-info-digest "
               + mapping.extraInfoDescriptorIdentifier.toUpperCase()
               + "\n");
 
         /* Before writing the exit policy, check if we wrote a contact
          * line before. If not, there was no contact line in the original
          * descriptor. In that case, add a generic contact line with the
          * bridge's country code as last two characters. */
         } else if (line.startsWith("reject ")
             || line.startsWith("accept ")) {
           if (!contactWritten) {
             scrubbed.append("contact nobody at example dot "
                 + mapping.countryCode.toLowerCase() + "\n");
             contactWritten = true;
           }
           scrubbed.append(line + "\n");
 
         /* Write the following lines unmodified to the sanitized
          * descriptor. */
         } else if (line.startsWith("platform ")
             || line.startsWith("opt protocols ")
             || line.startsWith("uptime ")
             || line.startsWith("bandwidth ")
             || line.startsWith("opt hibernating ")
             || line.equals("opt hidden-service-dir")
             || line.equals("opt caches-extra-info")
             || line.equals("opt allow-single-hop-exits")) {
           scrubbed.append(line + "\n");
 
         /* Replace node fingerprints in the family line with their hashes
          * and nicknames with Unnamed. */
         } else if (line.startsWith("family ")) {
           StringBuilder familyLine = new StringBuilder("family");
           for (String s : line.substring(7).split(" ")) {
             if (s.startsWith("$")) {
               familyLine.append(" $" + DigestUtils.shaHex(Hex.decodeHex(
                   s.substring(1).toCharArray())).toUpperCase());
             } else {
               familyLine.append(" Unnamed");
             }
           }
           scrubbed.append(familyLine.toString() + "\n");
 
         /* Skip the purpose line that the bridge authority adds to its
          * cached-descriptors file. */
         } else if (line.startsWith("@purpose ")) {
           continue;
 
         /* Skip all crypto parts that might leak the bridge's identity
          * fingerprint. */
         } else if (line.startsWith("-----BEGIN ")
             || line.equals("onion-key") || line.equals("signing-key")) {
           skipCrypto = true;
 
         /* Stop skipping lines when the crypto parts are over. */
         } else if (line.startsWith("-----END ")) {
           skipCrypto = false;
 
         /* If we encounter an unrecognized line, stop parsing and print
          * out a warning. We might have overlooked sensitive information
          * that we need to remove or replace for the sanitized descriptor
          * version. */
         } else {
           this.logger.warning("Unrecognized line '" + line
               + "'. Skipping.");
           return;
         }
       }
       br.close();
     } catch (Exception e) {
       this.logger.log(Level.WARNING, "Could not parse server "
           + "descriptor.", e);
       return;
     }
 
     /* Determine new descriptor digest and write it to descriptor
      * mapping. */
     String scrubbedHash = DigestUtils.shaHex(scrubbedDesc);
     mapping.serverDescriptorIdentifier = scrubbedHash;
 
     /* Determine filename of sanitized server descriptor. */
     String dyear = mapping.published.substring(0, 4);
     String dmonth = mapping.published.substring(5, 7);
     String dday = mapping.published.substring(8, 10);
     File newFile = new File(this.sanitizedBridgesDir + "/"
         + dyear + "/" + dmonth + "/server-descriptors/" + dday
         + "/" + scrubbedHash.charAt(0) + "/"
         + scrubbedHash.charAt(1) + "/"
         + scrubbedHash);
 
     /* Write sanitized server descriptor to disk, including all its parent
      * directories. */
     try {
       newFile.getParentFile().mkdirs();
       BufferedWriter bw = new BufferedWriter(new FileWriter(newFile));
       bw.write(scrubbedDesc);
       bw.close();
     } catch (IOException e) {
       this.logger.log(Level.WARNING, "Could not write sanitized server "
           + "descriptor to disk.", e);
       return;
     }
   }
 
   /**
    * Sanitizes an extra-info descriptor and writes it to disk. Looks up
    * the bridge identity hash and publication time in the descriptor
    * mapping. If the corresponding server descriptor was sanitized before,
    * it is re-written to include the new extra-info descriptor digest.
    * The publication time is noted down, too, so that all network statuses
    * possibly referencing this extra-info descriptor and its corresponding
    * server descriptor can be re-written at the end of the sanitizing
    * procedure.
    */
   public void sanitizeAndStoreExtraInfoDescriptor(byte[] data) {
 
     /* Parse descriptor to generate a sanitized version and to look it up
      * in the descriptor mapping. */
     String scrubbedDesc = null;
     DescriptorMapping mapping = null;
     try {
       BufferedReader br = new BufferedReader(new StringReader(new String(
           data, "US-ASCII")));
       String line = null;
       StringBuilder scrubbed = null;
       String hashedBridgeIdentity = null, published = null;
       while ((line = br.readLine()) != null) {
 
         /* When we have parsed both published and fingerprint line, look
          * up descriptor in the descriptor mapping or create a new one if
          * there is none. */
         if (mapping == null && published != null &&
             hashedBridgeIdentity != null) {
           String mappingKey = hashedBridgeIdentity + "," + published;
           if (this.bridgeDescriptorMappings.containsKey(mappingKey)) {
             mapping = this.bridgeDescriptorMappings.get(mappingKey);
           } else {
             mapping = new DescriptorMapping(hashedBridgeIdentity,
                 published);
             this.bridgeDescriptorMappings.put(mappingKey, mapping);
           }
         }
 
         /* Parse bridge identity from extra-info line and replace it with
          * its hash in the sanitized descriptor. */
         if (line.startsWith("extra-info ")) {
           hashedBridgeIdentity = DigestUtils.shaHex(Hex.decodeHex(
               line.split(" ")[2].toCharArray())).toLowerCase();
           scrubbed = new StringBuilder(hashedBridgeIdentity.toUpperCase()
               + "\n");
 
         /* Parse the publication time and add it to the list of descriptor
          * publication times to re-write network statuses at the end of
          * the sanitizing procedure. */
         } else if (line.startsWith("published ")) {
           scrubbed.append(line + "\n");
           published = line.substring("published ".length());
           this.descriptorPublicationTimes.add(published);
 
         /* Write the following lines unmodified to the sanitized
          * descriptor. */
         } else if (line.startsWith("write-history ")
             || line.startsWith("read-history ")
             || line.startsWith("geoip-start-time ")
             || line.startsWith("geoip-client-origins ")
             || line.startsWith("bridge-stats-end ") 
             || line.startsWith("bridge-ips ")) {
           scrubbed.append(line + "\n");
 
         /* When we reach the signature, we're done. Write the sanitized
          * descriptor to disk below. */
         } else if (line.startsWith("router-signature")) {
           scrubbedDesc = "extra-info Unnamed"
               + mapping.countryCode + " " + scrubbed.toString();
           break;
         /* Don't include statistics that should only be contained in relay
          * extra-info descriptors. */
         } else if (line.startsWith("dirreq-") || line.startsWith("cell-")
             || line.startsWith("exit-")) {
           continue;
 
         /* If we encounter an unrecognized line, stop parsing and print
          * out a warning. We might have overlooked sensitive information
          * that we need to remove or replace for the sanitized descriptor
          * version. */
         } else {
           this.logger.warning("Unrecognized line '" + line
               + "'. Skipping");
           return;
         }
       }
       br.close();
     } catch (IOException e) {
       this.logger.log(Level.WARNING, "Could not parse extra-info "
           + "descriptor.", e);
       return;
     } catch (DecoderException e) {
       this.logger.log(Level.WARNING, "Could not parse extra-info "
           + "descriptor.", e);
       return;
     }
 
     /* Determine new descriptor digest and check if write it to descriptor
      * mapping. */
     String scrubbedDescHash = DigestUtils.shaHex(scrubbedDesc);
     boolean extraInfoDescriptorIdentifierHasChanged =
         !scrubbedDescHash.equals(mapping.extraInfoDescriptorIdentifier);
     mapping.extraInfoDescriptorIdentifier = scrubbedDescHash;
     if (extraInfoDescriptorIdentifierHasChanged &&
         !mapping.serverDescriptorIdentifier.equals(NULL_REFERENCE)) {
       this.rewriteServerDescriptor(mapping);
     }
 
     /* Determine filename of sanitized server descriptor. */
     String dyear = mapping.published.substring(0, 4);
     String dmonth = mapping.published.substring(5, 7);
     String dday = mapping.published.substring(8, 10);
     File newFile = new File(this.sanitizedBridgesDir + "/"
         + dyear + "/" + dmonth + "/extra-infos/" + dday
         + "/" + scrubbedDescHash.charAt(0) + "/"
         + scrubbedDescHash.charAt(1) + "/"
         + scrubbedDescHash);
 
     /* Write sanitized server descriptor to disk, including all its parent
      * directories. */
     try {
       newFile.getParentFile().mkdirs();
       BufferedWriter bw = new BufferedWriter(new FileWriter(newFile));
       bw.write(scrubbedDesc);
       bw.close();
     } catch (Exception e) {
       this.logger.log(Level.WARNING, "Could not write sanitized "
           + "extra-info descriptor to disk.", e);
     }
   }
 
   public void storeSanitizedNetworkStatus(byte[] data, String published) {
     String scrubbed = null;
     try {
       String ascii = new String(data, "US-ASCII");
       BufferedReader br2 = new BufferedReader(new StringReader(ascii));
       StringBuilder sb = new StringBuilder();
       String line = null;
       while ((line = br2.readLine()) != null) {
         if (line.startsWith("r ")) {
           String readCountryCode = line.split(" ")[1].substring(
               "Unnamed".length());
           String hashedBridgeIdentity = Hex.encodeHexString(
               Base64.decodeBase64(line.split(" ")[2] + "==")).
               toLowerCase();
           String hashedBridgeIdentityBase64 =
               Base64.encodeBase64String(DigestUtils.sha(
               Base64.decodeBase64(line.split(" ")[2] + "=="))).
               substring(0, 27);
           String readServerDescId = Hex.encodeHexString(
               Base64.decodeBase64(line.split(" ")[3] + "==")).
               toLowerCase();
           String descPublished = line.split(" ")[4] + " "
               + line.split(" ")[5];
           String mappingKey = (hashedBridgeIdentity + ","
               + descPublished).toLowerCase();
           DescriptorMapping mapping = null;
           if (this.bridgeDescriptorMappings.containsKey(mappingKey)) {
             mapping = this.bridgeDescriptorMappings.get(mappingKey);
           } else {
             mapping = new DescriptorMapping(hashedBridgeIdentity.
                 toLowerCase(), descPublished);
             mapping.countryCode = readCountryCode;
              mapping.serverDescriptorIdentifier = readServerDescId;
             this.bridgeDescriptorMappings.put(mappingKey, mapping);
           }
           String nickname = "Unnamed" + mapping.countryCode;
           String sdi = Base64.encodeBase64String(Hex.decodeHex(
               mapping.serverDescriptorIdentifier.toCharArray())).
               substring(0, 27);
           String orPort = line.split(" ")[7];
           String dirPort = line.split(" ")[8];
           sb.append("r " + nickname + " "
               + hashedBridgeIdentityBase64 + " " + sdi + " "
               + descPublished + " 127.0.0.1 " + orPort + " "
               + dirPort + "\n");
         } else {
           sb.append(line + "\n");
         }
       }
       scrubbed = sb.toString();
       br2.close();
     } catch (DecoderException e) {
       this.logger.log(Level.WARNING, "Could not parse server descriptor "
           + "identifier. This must be a bug.", e);
       return;
     } catch (IOException e) {
       this.logger.log(Level.WARNING, "Could not parse previously "
           + "sanitized network status.", e);
       return;
     }
 
     try {
       /* Determine file name. */
       String syear = published.substring(0, 4);
       String smonth = published.substring(5, 7);
       String sday = published.substring(8, 10);
       String stime = published.substring(11, 13)
           + published.substring(14, 16)
           + published.substring(17, 19);
       File statusFile = new File(this.sanitizedBridgesDir + "/" + syear
           + "/" + smonth + "/statuses/" + sday + "/" + syear + smonth
           + sday + "-" + stime + "-"
           + "4A0CCD2DDC7995083D73F5D667100C8A5831F16D");
 
       /* Create all parent directories to write this network status. */
       statusFile.getParentFile().mkdirs();
 
       /* Write sanitized network status to disk. */
       BufferedWriter bw = new BufferedWriter(new FileWriter(statusFile));
       bw.write(scrubbed);
       bw.close();
     } catch (IOException e) {
       this.logger.log(Level.WARNING, "Could not write previously "
           + "sanitized network status.", e);
       return;
     }
   } 
 
   public void storeSanitizedServerDescriptor(byte[] data) {
     try {
       String ascii = new String(data, "US-ASCII");
       BufferedReader br2 = new BufferedReader(new StringReader(ascii));
       StringBuilder sb = new StringBuilder();
       String line2 = null, published = null;
       String hashedBridgeIdentity = null;
       DescriptorMapping mapping = null;
       while ((line2 = br2.readLine()) != null) {
         if (mapping == null && published != null &&
             hashedBridgeIdentity != null) {
           String mappingKey = (hashedBridgeIdentity + "," + published).
               toLowerCase();
           if (this.bridgeDescriptorMappings.containsKey(mappingKey)) {
             mapping = this.bridgeDescriptorMappings.get(mappingKey);
           } else {
             mapping = new DescriptorMapping(hashedBridgeIdentity.
                 toLowerCase(), published);
             this.bridgeDescriptorMappings.put(mappingKey, mapping);
           }
         }
         if (line2.startsWith("router ")) {
           sb.append(" 127.0.0.1 " + line2.split(" ")[3] + " "
               + line2.split(" ")[4] + " " + line2.split(" ")[5]
               + "\n");
         } else if (line2.startsWith("published ")) {
           published = line2.substring("published ".length());
           sb.append(line2 + "\n");
           this.descriptorPublicationTimes.add(published);
         } else if (line2.startsWith("opt fingerprint ")) {
           hashedBridgeIdentity = line2.substring("opt fingerprint".
               length()).replaceAll(" ", "").toLowerCase();
           sb.append(line2 + "\n");
         } else if (line2.startsWith("opt extra-info-digest ")) {
           sb.append("opt extra-info-digest "
               + mapping.extraInfoDescriptorIdentifier.toUpperCase()
               + "\n");
         } else {
           sb.append(line2 + "\n");
         }
       }
       br2.close();
       String scrubbedDesc = "router Unnamed" + mapping.countryCode
           + sb.toString();
       String scrubbedHash = DigestUtils.shaHex(scrubbedDesc);
 
       mapping.serverDescriptorIdentifier = scrubbedHash;
       String dyear = published.substring(0, 4);
       String dmonth = published.substring(5, 7);
       String dday = published.substring(8, 10);
       File newFile = new File(this.sanitizedBridgesDir + "/"
           + dyear + "/" + dmonth + "/server-descriptors/" + dday
           + "/" + scrubbedHash.substring(0, 1) + "/"
           + scrubbedHash.substring(1, 2) + "/"
           + scrubbedHash);
       this.logger.finer("Storing server descriptor "
           + newFile.getAbsolutePath());
       newFile.getParentFile().mkdirs();
       BufferedWriter bw = new BufferedWriter(new FileWriter(
           newFile));
       bw.write(scrubbedDesc);
       bw.close();
     } catch (IOException e) {
       this.logger.log(Level.WARNING, "Could not store unsanitized server "
           + "descriptor.", e);
     }
   }
 
   public void storeSanitizedExtraInfoDescriptor(byte[] data) {
     try {
       String ascii = new String(data, "US-ASCII");
       BufferedReader br2 = new BufferedReader(new StringReader(ascii));
       StringBuilder sb = new StringBuilder();
       String line2 = null, published = null;
       String hashedBridgeIdentity = null;
       DescriptorMapping mapping = null;
       while ((line2 = br2.readLine()) != null) {
         if (mapping == null && published != null &&
             hashedBridgeIdentity != null) {
           String mappingKey = (hashedBridgeIdentity + "," + published).
               toLowerCase();
           if (this.bridgeDescriptorMappings.containsKey(mappingKey)) {
             mapping = this.bridgeDescriptorMappings.get(mappingKey);
           } else {
             mapping = new DescriptorMapping(hashedBridgeIdentity.
                 toLowerCase(), published);
             this.bridgeDescriptorMappings.put(mappingKey, mapping);
           }
         }
         if (line2.startsWith("extra-info ")) {
           hashedBridgeIdentity = line2.split(" ")[2];
           sb.append(hashedBridgeIdentity + "\n");
         } else if (line2.startsWith("published ")) {
           sb.append(line2 + "\n");
           published = line2.substring("published ".length());
           this.descriptorPublicationTimes.add(published);
         } else if (line2.startsWith(
             "contact somebody at example dot ") ||
             line2.startsWith("contact nobody at example dot ")) {
           sb.append(line2.substring(0, line2.indexOf("dot ")
               + "dot ".length()) + mapping.countryCode.toLowerCase()
               + "\n");
         } else {
           sb.append(line2 + "\n");
         }
       }
       br2.close();
       String scrubbedDesc = "extra-info Unnamed"
           + mapping.countryCode.toUpperCase() + " " + sb.toString();
       String scrubbedHash = DigestUtils.shaHex(scrubbedDesc);
       mapping.extraInfoDescriptorIdentifier = scrubbedHash;
       String dyear = published.substring(0, 4);
       String dmonth = published.substring(5, 7);
       String dday = published.substring(8, 10);
       File newFile = new File(this.sanitizedBridgesDir + "/"
           + dyear + "/" + dmonth + "/extra-infos/" + dday + "/"
           + scrubbedHash.substring(0, 1) + "/"
           + scrubbedHash.substring(1, 2) + "/"
           + scrubbedHash);
       this.logger.finer("Storing extra-info descriptor "
           + newFile.getAbsolutePath());
       newFile.getParentFile().mkdirs();
       BufferedWriter bw = new BufferedWriter(new FileWriter(
           newFile));
       bw.write(scrubbedDesc);
       bw.close();
     } catch (IOException e) {
       this.logger.log(Level.WARNING, "Could not store sanitized "
           + "extra-info descriptor.", e);
     }
   }
 
   private void rewriteNetworkStatus(File status, String published) {
     try {
       FileInputStream fis = new FileInputStream(status);
       BufferedInputStream bis = new BufferedInputStream(fis);
       ByteArrayOutputStream baos = new ByteArrayOutputStream();
       int len;
       byte[] data2 = new byte[1024];
       while ((len = bis.read(data2, 0, 1024)) >= 0) {
         baos.write(data2, 0, len);
       }
       fis.close();
       byte[] allData = baos.toByteArray();
       this.storeSanitizedNetworkStatus(allData, published);
     } catch (IOException e) {
       this.logger.log(Level.WARNING, "Could not rewrite network "
           + "status.", e);
     }
   }
 
   private void rewriteServerDescriptor(DescriptorMapping mapping) {
     try {
       String dyear = mapping.published.substring(0, 4);
       String dmonth = mapping.published.substring(5, 7);
       String dday = mapping.published.substring(8, 10);
       File serverDescriptorFile = new File(
           this.sanitizedBridgesDir + "/"
           + dyear + "/" + dmonth + "/server-descriptors/" + dday
           + "/" + mapping.serverDescriptorIdentifier.substring(0, 1) + "/"
           + mapping.serverDescriptorIdentifier.substring(1, 2) + "/"
           + mapping.serverDescriptorIdentifier);
       FileInputStream fis = new FileInputStream(serverDescriptorFile);
       BufferedInputStream bis = new BufferedInputStream(fis);
       ByteArrayOutputStream baos = new ByteArrayOutputStream();
       int len;
       byte[] data2 = new byte[1024];
       while ((len = bis.read(data2, 0, 1024)) >= 0) {
         baos.write(data2, 0, len);
       }
       fis.close();
       byte[] allData = baos.toByteArray();
       this.storeSanitizedServerDescriptor(allData);
       serverDescriptorFile.delete();
       this.logger.finer("Deleting server descriptor "
           + serverDescriptorFile.getAbsolutePath());
     } catch (IOException e) {
       this.logger.log(Level.WARNING, "Could not rewrite server "
           + "descriptor.", e);
     }
   }
 
   private void rewriteExtraInfoDescriptor(DescriptorMapping mapping) {
     try {
       String dyear = mapping.published.substring(0, 4);
       String dmonth = mapping.published.substring(5, 7);
       String dday = mapping.published.substring(8, 10);
       File extraInfoDescriptorFile = new File(
           this.sanitizedBridgesDir + "/"
           + dyear + "/" + dmonth + "/extra-infos/" + dday + "/"
           + mapping.extraInfoDescriptorIdentifier.substring(0, 1) + "/"
           + mapping.extraInfoDescriptorIdentifier.substring(1, 2) + "/"
           + mapping.extraInfoDescriptorIdentifier);
       FileInputStream fis = new FileInputStream(extraInfoDescriptorFile);
       BufferedInputStream bis = new BufferedInputStream(fis);
       ByteArrayOutputStream baos = new ByteArrayOutputStream();
       int len;
       byte[] data2 = new byte[1024];
       while ((len = bis.read(data2, 0, 1024)) >= 0) {
         baos.write(data2, 0, len);
       }
       fis.close();
       byte[] allData = baos.toByteArray();
       this.storeSanitizedExtraInfoDescriptor(allData);
       extraInfoDescriptorFile.delete();
       this.logger.finer("Deleting extra-info descriptor "
           + extraInfoDescriptorFile.getAbsolutePath());
     } catch (IOException e) {
       e.printStackTrace();
     }
   }
 
   /**
    * Rewrite all network statuses that might contain references to server
    * descriptors we added or updated in this execution. This applies to
    * all statuses that have been published up to 24 hours after any added
    * or updated server descriptor.
    */
   public void finishWriting() {
 
     /* Prepare parsing and formatting timestamps. */
     SimpleDateFormat dateTimeFormat =
          new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
     dateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
     SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
     dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
     SimpleDateFormat statusFileFormat =
         new SimpleDateFormat("yyyyMMdd-HHmmss");
     statusFileFormat.setTimeZone(TimeZone.getTimeZone("UTC"));    
 
     /* Iterate over publication timestamps of previously sanitized
      * descriptors. For every publication timestamp, we want to re-write
      * the network statuses that we published up to 24 hours after that
      * descriptor. We keep the timestamp of the last re-written network
      * status in order to make sure we re-writing any network status at
      * most once. */
    String lastDescriptorPublishedPlus24Hours = "1970-01-01 00:00:00";
     for (String published : this.descriptorPublicationTimes) {
      if (published.compareTo(lastDescriptorPublishedPlus24Hours) <= 0) {
         continue;
       }
       // find statuses 24 hours after published
       SortedSet<File> statusesToRewrite = new TreeSet<File>();
       long publishedTime;
       try {
         publishedTime = dateTimeFormat.parse(published).getTime();
       } catch (ParseException e) {
         this.logger.log(Level.WARNING, "Could not parse publication "
             + "timestamp '" + published + "'. Skipping.", e);
         continue;
       }
       String[] dayOne = dateFormat.format(publishedTime).split("-");
 
       File publishedDayOne = new File(this.sanitizedBridgesDir + "/"
           + dayOne[0] + "/" + dayOne[1] + "/statuses/" + dayOne[2]);
       if (publishedDayOne.exists()) {
         statusesToRewrite.addAll(Arrays.asList(publishedDayOne.
             listFiles()));
       }
       long plus24Hours = publishedTime + 24L * 60L * 60L * 1000L;
      lastDescriptorPublishedPlus24Hours = dateFormat.format(plus24Hours);
       String[] dayTwo = dateFormat.format(plus24Hours).split("-");
       File publishedDayTwo = new File(this.sanitizedBridgesDir + "/"
           + dayTwo[0] + "/" + dayTwo[1] + "/statuses/" + dayTwo[2]);
       if (publishedDayTwo.exists()) {
         statusesToRewrite.addAll(Arrays.asList(publishedDayTwo.
             listFiles()));
       }
       for (File status : statusesToRewrite) {
         String statusPublished = status.getName().substring(0, 15);
         long statusTime;
         try {
           statusTime = statusFileFormat.parse(statusPublished).getTime();
         } catch (ParseException e) {
           this.logger.log(Level.WARNING, "Could not parse network "
               + "status publication timestamp '" + published
               + "'. Skipping.", e);
           continue;
         }
         if (statusTime < publishedTime || statusTime > plus24Hours) {
           continue;
         }
         this.rewriteNetworkStatus(status,
             dateTimeFormat.format(statusTime));
       }
     }
 
     /* Write descriptor mappings to disk. */
     try {
       BufferedWriter bw = new BufferedWriter(new FileWriter(
           this.bridgeDescriptorMappingsFile));
       for (DescriptorMapping mapping :
           this.bridgeDescriptorMappings.values()) {
         bw.write(mapping.toString() + "\n");
       }
       bw.close();
     } catch (IOException e) {
       this.logger.log(Level.WARNING, "Could not write descriptor "
           + "mappings to disk.", e);
     }
   }
 }
 
