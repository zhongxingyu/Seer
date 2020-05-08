 package au.edu.uq.cmm.mirage.grabber;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.log4j.Logger;
 import org.ini4j.Ini;
 import org.ini4j.InvalidFileFormatException;
 
 /**
  * An UNC path mapper for use on a Samba fileserver.  This reads the Samba
  * configuration file to figure out what the locally hosted shares are.
  * 
  * @author scrawley
  */
 public class SambaUncPathameMapper implements UncPathnameMapper {
     private static final Logger LOG = Logger.getLogger(SambaUncPathameMapper.class);
     private static final Pattern UNC_PATTERN = 
            Pattern.compile("//([^/]+)/([^/]+)(?:/+(.*)?)");
     private Map<String, File> shareMap = new HashMap<String, File>();
     private Set<String> hostNames;
     
     public SambaUncPathameMapper(String smbConfFileName) 
             throws InvalidFileFormatException, IOException {
         initShareMap(smbConfFileName);
         initHostnames();
     }
 
     /**
      * Configure the set of Strings that we'll recognize as our hostname
      * an UNC pathname.
      * 
      * @throws UnknownHostException
      */
     private void initHostnames() throws UnknownHostException {
         // Note: this will only give us the primary hostname & IP address.  
         // DNS aliases and secondary IP addresses won't show up.  (If 
         // this is a problem we may need to configure the 'hostnames' 
         // set manually.)
         LOG.debug("Figuring out our hostnames");
         hostNames = new HashSet<String>();
         InetAddress host = InetAddress.getLocalHost();
         String hostAddr = host.getHostAddress();
         String hostName = host.getHostName();
         String canonicalHostName = host.getCanonicalHostName();
         hostNames.add(hostAddr);
         if (hostNames.add(hostName)) {
             int firstDot = hostName.indexOf(".");
             if (firstDot > 0) {
                 hostNames.add(hostName.substring(0, firstDot));
             }
         }
         if (hostNames.add(canonicalHostName)) {
             int firstDot = canonicalHostName.indexOf(".");
             if (firstDot > 0) {
                 hostNames.add(canonicalHostName.substring(0, firstDot));
             }
         }
         LOG.info("The following hostnames will be recognized " +
         		"as 'us' by the share mapper: " + hostNames);
     }
 
     /**
      * Load the SMB share mapping from the Samba config file.
      * 
      * @param smbConfFileName the pathname of the config file.
      * @throws FileNotFoundException
      * @throws IOException
      * @throws InvalidFileFormatException
      */
     private void initShareMap(String smbConfFileName)
             throws FileNotFoundException, IOException,
             InvalidFileFormatException {
         FileInputStream is = new FileInputStream(smbConfFileName);
         try {
             LOG.debug("Loading share map from Samba config file - " + smbConfFileName);
             Ini ini = new Ini();
             ini.load(is);
             for (String section : ini.keySet()) {
                 if (section.equals("global")) {
                     continue;
                 }
                 String path = ini.get(section, "path");
                 if (path == null) {
                     continue;
                 }
                 File dir = new File(path);
                 if (!dir.exists() || !dir.isDirectory()) {
                     LOG.info("Ignoring share '" + section + 
                             "' because the mapped object '" + dir +
                             "' is not an existing directory");
                     continue;
                 }
                 shareMap.put(section, dir);
             }
         } finally {
             is.close();
         }
     }
     
 
     @Override
     public File mapUncPathname(String uncPathname) {
         String canonicalUncPathname = uncPathname.replace('\\', '/');
         Matcher matcher = UNC_PATTERN.matcher(canonicalUncPathname);
         if (!matcher.matches()) {
             LOG.info("Invalid UNC path: '" + canonicalUncPathname + "'");
             return null;
         }
         if (!hostNames.contains(matcher.group(1))) {
             LOG.info("UNC path '" + canonicalUncPathname + "'s hostname is not us");
             return null;
         }
         File sharePath = shareMap.get(matcher.group(2));
         if (sharePath == null) {
             LOG.info("UNC path '" + canonicalUncPathname + "'s share is not known");
             return null;
         }
         if (matcher.group(3) == null) {
             return sharePath;
         } else {
             return new File(sharePath, matcher.group(3));
         }
     }
 
 }
