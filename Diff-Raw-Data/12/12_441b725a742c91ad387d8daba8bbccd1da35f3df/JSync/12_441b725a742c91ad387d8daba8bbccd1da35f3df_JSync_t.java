 /*
  * Funambol is a mobile platform developed by Funambol, Inc. 
  * Copyright (C) 2009 Funambol, Inc.
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Affero General Public License version 3 as published by
  * the Free Software Foundation with the addition of the following permission 
  * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
  * WORK IN WHICH THE COPYRIGHT IS OWNED BY FUNAMBOL, FUNAMBOL DISCLAIMS THE 
  * WARRANTY OF NON INFRINGEMENT  OF THIRD PARTY RIGHTS.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Affero General Public License 
  * along with this program; if not, see http://www.gnu.org/licenses or write to
  * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301 USA.
  * 
  * You can contact Funambol, Inc. headquarters at 643 Bair Island Road, Suite 
  * 305, Redwood City, CA 94063, USA, or at email address info@funambol.com.
  * 
  * The interactive user interfaces in modified source and object code versions
  * of this program must display Appropriate Legal Notices, as required under
  * Section 5 of the GNU Affero General Public License version 3.
  * 
  * In accordance with Section 7(b) of the GNU Affero General Public License
  * version 3, these Appropriate Legal Notices must retain the display of the
  * "Powered by Funambol" logo. If the display of the logo is not reasonably 
  * feasible for technical reasons, the Appropriate Legal Notices must display
  * the words "Powered by Funambol". 
  */
 
 package com.funambol.jsync;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.DataInputStream;
 import java.io.OutputStream;
 import java.io.DataOutputStream;
 import java.io.FileInputStream;
 import java.io.File;
 import java.util.Properties;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Set;
 
 import com.funambol.sync.SyncConfig;
 import com.funambol.sync.SyncReport;
 import com.funambol.sync.SourceConfig;
 import com.funambol.sync.SyncSource;
 import com.funambol.sync.SyncAnchor;
 import com.funambol.sync.SyncManagerI;
 import com.funambol.sync.client.CacheTracker;
 import com.funambol.sync.client.RawFileSyncSource;
 import com.funambol.syncml.client.FileSyncSource;
 import com.funambol.syncml.spds.SyncMLAnchor;
 import com.funambol.syncml.spds.SyncManager;
 import com.funambol.syncml.spds.DeviceConfig;
 import com.funambol.syncml.protocol.SyncML;
 import com.funambol.sapisync.SapiSyncManager;
 import com.funambol.sapisync.SapiSyncAnchor;
 import com.funambol.storage.StringKeyValueStore;
 import com.funambol.storage.StringKeyValueMemoryStore;
 import com.funambol.storage.StringKeyValueFileStore;
 import com.funambol.platform.FileAdapter;
 import com.funambol.util.Log;
 import com.funambol.util.StringUtil;
 import com.funambol.util.ConsoleAppender;
 
 public class JSync {
 
     private static final String TAG_LOG = "JSync";
 
     private String args[];
     private String username  = null;
     private String password  = null;
     private String url       = null;
     private int    logLevel  = Log.DISABLED;
     private int    syncMode  = SyncSource.INCREMENTAL_SYNC;
     private String remoteUri = "briefcase";
     private boolean raw      = false;
     private int customMsgSize = 16*1024;
     private String customDeviceId = null;
     private String sourceType = null;
     private String sourceEncoding = null;
     private boolean md5 = false;
     private boolean wbxml = false;
     private boolean mediaEngine = false;
     private String  dir;
 
     public JSync(String args[]) {
         this.args = args;
     }
 
     public void run() {
         // Load the config file if any
         loadConfigFile();
         // Parse the arguments
         parseArgs();
 
         ConsoleAppender appender = new ConsoleAppender();
         Log.initLog(appender);
         Log.setLogLevel(logLevel);
 
         SyncConfig  config  = new SyncConfig();
         // Apply customized device config
         DeviceConfig dc = new DeviceConfig();
         if (customDeviceId != null) {
             dc.setDevID(customDeviceId);
         }
         dc.setMaxMsgSize(customMsgSize);
         dc.setWBXML(wbxml);
         // Set credentials
         config.syncUrl  = url;
         config.userName = username;
         config.password = password;
 
         if (md5) {
             config.preferredAuthType = SyncConfig.AUTH_TYPE_MD5;
         }
 
         String serverName = StringUtil.extractAddressFromUrl(url, StringUtil.getProtocolFromUrl(url));
         String clientConfigDir = "config" + File.separator + serverName;
 
         config.compress = false;
         SyncManagerI manager;
         
         if (mediaEngine) {
             manager = new SapiSyncManager(config, dc);
         } else {
             manager = new SyncManager(config, dc);
         }
         JSyncSourceConfig sc = new JSyncSourceConfig(SourceConfig.BRIEFCASE, SourceConfig.FILE_OBJECT_TYPE, remoteUri);
 
         // Define the source config file
         String sourceConfigFile = clientConfigDir + File.separator + remoteUri + File.separator + "config.properties";
         String cacheFile        = clientConfigDir + File.separator + remoteUri + File.separator + "cache.txt";
 
         // Create the dir tree if needed
         try {
             mkdir(clientConfigDir + File.separator + remoteUri);
             if (dir == null) {
                 dir = clientConfigDir + File.separator + remoteUri + File.separator + "db" + File.separator;
                 mkdir(dir);
             }
         } catch (IOException ioe) {
             System.err.println("Cannot create configuration directory tree");
             System.exit(1);
         }
 
         SyncAnchor anchor;
         if (mediaEngine) {
             anchor = new SapiSyncAnchor();
         } else {
             anchor = new SyncMLAnchor(); 
         }
         sc.setSyncAnchor(anchor);
 
 
         // If a configuration exists, then load it
         try {
             FileAdapter ssConfig = new FileAdapter(sourceConfigFile);
             if (ssConfig.exists()) {
                 sc.load(sourceConfigFile);
                 ssConfig.close();
             }
         } catch (IOException ioe) {
             System.err.println("Cannot load configuration");
         }
 
         // --raw is a utility flag when syncing files. It defines type and
         // encoding. Its effect can be overwritten by specifying type and
         // encoding
         if (!raw) {
             sc.setType(SourceConfig.FILE_OBJECT_TYPE);
             sc.setEncoding(SyncSource.ENCODING_NONE);
         } else {
             sc.setType(SourceConfig.BRIEFCASE_TYPE);
             // Disabled because our server does not support binary data
             //if (!wbxml) {
             //    sc.setEncoding(SyncSource.ENCODING_B64);
             //}
         }
 
         // Set custom source type and encoding
         if (sourceType != null) {
             sc.setType(sourceType);
         }
         if (sourceEncoding != null) {
             sc.setEncoding(sourceEncoding);
         }
 
         sc.setRemoteUri(remoteUri);
 
         StringKeyValueFileStore ts = new StringKeyValueFileStore(cacheFile);
         CacheTracker ct = new CacheTracker(ts);
         sc.setSyncMode(syncMode);
 
        SyncSource ss;

        if (mediaEngine) {
            ss = new com.funambol.sapisync.source.FileSyncSource(sc, ct, dir, dir, 0, 0);
        } else {
            ss = new FileSyncSource(sc, ct, dir);
        }
 
         try {
            manager.sync(ss);
             // Save the configuration
             sc.save(sourceConfigFile);
         } catch (Exception e) {
             Log.error(TAG_LOG, "Exception while synchronizing", e);
         }
 
     }
 
     public static void main(String args[]) {
         JSync jsync = new JSync(args);
         jsync.run();
     }
 
     private void parseLogLevel(String log) {
         if (log.equals("none")) {
             logLevel = Log.DISABLED;
         } else if (log.equals("info")) {
             logLevel = Log.INFO;
         } else if (log.equals("debug")) {
             logLevel = Log.DEBUG;
         } else if (log.equals("trace")) {
             logLevel = Log.TRACE;
         } else {
             System.err.println("Unknown log level " + log);
             usage();
             System.exit(1);
         }
     }
 
     private void parseSyncMode(String mode) {
         if (mode.equals("fast")) {
             syncMode = SyncML.ALERT_CODE_FAST;
         } else if (mode.equals("slow")) {
             syncMode = SyncML.ALERT_CODE_SLOW;
         } else if (mode.equals("refresh_from_server")) {
             syncMode = SyncML.ALERT_CODE_REFRESH_FROM_SERVER;
         } else if (mode.equals("refresh_from_client")) {
             syncMode = SyncML.ALERT_CODE_REFRESH_FROM_CLIENT;
         } else if (mode.equals("one_way_from_server")) {
             syncMode = SyncML.ALERT_CODE_ONE_WAY_FROM_SERVER;
         } else if (mode.equals("one_way_from_client")) {
             syncMode = SyncML.ALERT_CODE_ONE_WAY_FROM_CLIENT;
         } else {
             System.err.println("Unknown sync mode " + mode);
             usage();
             System.exit(1);
         }
     }
 
     private void loadConfigFile() {
         int i = 0;
         String configFile = null;
         while(i<args.length) {
             String arg = args[i];
             if ("--config".equals(arg)) {
                 if (i+1 <args.length) {
                     configFile = args[++i];
                     break;
                 } else {
                     System.err.println("Missing configuration file");
                     usage();
                     System.exit(1);
                 }
             }
             ++i;
         }
 
         // If we have a config file we shall load it now
         if (configFile != null) {
             Properties props = new Properties();
 
             //try retrieve data from file
             try {
                 props.load(new FileInputStream(configFile));
             } catch (IOException ioe) {
                 System.err.println("Cannot read configuration file");
                 ioe.printStackTrace();
                 System.exit(2);
             }
 
             // Now add to the command arguments, so that the corresponding
             // options will be set during the parseArgs
             ArrayList<String> newArgs = new ArrayList<String>();
             Set keySet = props.keySet();
             for(Object key : keySet) {
                 String value = (String)props.get(key);
                 newArgs.add("--" + (String)key);
                 if (value != null && value.length() > 0) {
                     newArgs.add(value);
                 }
             }
             String args2[] = new String[newArgs.size() + args.length];
             i = 0;
             for(String arg : newArgs) {
                 args2[i++] = arg;
             }
             for(String arg : args) {
                 args2[i++] = arg;
             }
             args = args2;
         }
     }
 
     private void parseArgs() {
         int i = 0;
         while(i<args.length) {
             String arg = args[i];
             if (arg.equals("--user")) {
                 if (i+1 < args.length) {
                     username = args[++i];
                 } else {
                     System.err.println("Missing username");
                     usage();
                     System.exit(1);
                 }
             } else if (arg.equals("--pwd")) {
                 if (i+1 < args.length) {
                     password = args[++i];
                 } else {
                     System.err.println("Missing password");
                     usage();
                     System.exit(1);
                 }
             } else if (arg.equals("--url")) {
                 if (i+1 < args.length) {
                     url = args[++i];
                 } else {
                     System.err.println("Missing url");
                     usage();
                     System.exit(1);
                 }
             } else if (arg.equals("--log")) {
                 if (i+1 < args.length) {
                     parseLogLevel(args[++i]);
                 } else {
                     System.err.println("Missing log level");
                     usage();
                     System.exit(1);
                 }
             } else if (arg.equals("--mode")) {
                 if (i+1 < args.length) {
                     parseSyncMode(args[++i]);
                 } else {
                     System.err.println("Missing sync mode");
                     usage();
                     System.exit(1);
                 }
             } else if (arg.equals("--uri")) {
                 if (i+1 < args.length) {
                     remoteUri = args[++i];
                 } else {
                     System.err.println("Missing remote uri");
                     usage();
                     System.exit(1);
                 }
             } else if (arg.equals("--raw")) {
                 raw = true;
             } else if (arg.equals("--type")) {
                 if (i+1 < args.length) {
                     sourceType = args[++i];
                 } else {
                     System.err.println("Missing type");
                     usage();
                     System.exit(1);
                 }
             } else if (arg.equals("--encoding")) {
                 if (i+1 < args.length) {
                     sourceEncoding = args[++i];
                 } else {
                     System.err.println("Missing encoding");
                     usage();
                     System.exit(1);
                 }
             } else if (arg.equals("--devid")) {
                 if (i+1 < args.length) {
                     customDeviceId = args[++i];
                 } else {
                     System.err.println("Missing device id");
                     usage();
                     System.exit(1);
                 }
             } else if (arg.equals("--maxmsgsize")) {
                 if (i+1 < args.length) {
                     customMsgSize = Integer.parseInt(args[++i]);
                 } else {
                     System.err.println("Missing max msg size");
                     usage();
                     System.exit(1);
                 }
             } else if (arg.equals("--md5")) {
                 md5 = true;
             } else if (arg.equals("--wbxml")) {
                 wbxml = true;
             } else if (arg.equals("--config")) {
                 // Ignore the next param (file name)
                 ++i;
             } else if (arg.equals("--dir")) {
                 if (i+1 < args.length) {
                     dir = args[++i];
                 } else {
                     System.err.println("Missing directory");
                     usage();
                     System.exit(1);
                 }
             } else if (arg.equals("--media")) {
                 mediaEngine = true;
             } else {
                 System.err.println("Invalid option: " + arg);
                 usage();
                 System.exit(1);
             }
             ++i;
         }
 
         if (username == null || password == null || url == null) {
             usage();
             System.exit(2);
         }
     }
 
     private void mkdir(String dir) throws IOException {
         String dirs[] = StringUtil.split(dir, File.separator);
         StringBuffer dirBuf = new StringBuffer();
         for(String d : dirs) {
             if (dirBuf.length() > 0) {
                 dirBuf.append(File.separator);
             }
             dirBuf.append(d);
             File f = new File(dirBuf.toString());
             if (!f.exists()) {
                 f.mkdir();
             }
         }
     }
 
     private void usage() {
         System.out.println("JSync options");
         StringBuffer options = new StringBuffer();
         
         options.append("--user <username>\n")
                .append("--pwd <password>\n")
                .append("--url <url>\n")
                .append("--log <trace|debug|info|error|none>\n")
                .append("--mode <fast|slow|refresh_from_client|refresh_from_server|one_way_from_client|one_way_from_server>\n")
                .append("--uri remote_uri\n")
                .append("--raw\n")
                .append("--type <source_mime_type>\n")
                .append("--encoding <none|b64>\n")
                .append("--md5 tries md5 authentication first\n")
                .append("--wbxml use WBXML encoding\n")
                .append("--dir <dirname> the directory to sync\n")
                .append("--config <file> read configuration properties from file\n")
                .append("--media use the sapi sync engine for media sources");
 
         System.out.println(options);
     }
 
 
 }
 
