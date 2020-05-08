 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
 * Copyright 2008 Laszlo Systems, Inc.  All Rights Reserved.                   *
 * Use is subject to license terms.                                            *
 * J_LZ_COPYRIGHT_END *********************************************************/
 
 package org.openlaszlo.test.netsize;
 
 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 public class Netsize {
     /** can be changed with -u option, see usage */
     public static final String DEFAULT_URLBASE = "http://127.0.0.1:8080";
 
     private String urlBase = null;
     private String prefix = null;
     private boolean verbose = false;
     private String configname = null;
 
     public static void usage() {
         System.out.println(
 "    Usage:  java org.openlaszlo.test.netsize.Netsize [ -f configfile ] [ options ]\n\n" +
 "    Options:\n" +
 "    -f configfile   file that lists the urls by app\n" +
 "    -u url_base     the base part of the url, by default \n" +
 "                    \"" + DEFAULT_URLBASE + "\"\n" +
 "    -p prefix       prefix, often the subversion branch name\n" +
 "    -n newconfig    create a new configfile\n" +
 "    -i              ignore errors\n" +
 "    -v              turn verbose on\n");
     }
 
     public static void main(String[] args) {
         String urlBase = DEFAULT_URLBASE;
         String prefix = "";
         String config = null;
         boolean verbose = false;
         boolean ignoreErrors = false;
         String newconfig = null;
 
         for (int i=0; i<args.length; ) {
             String arg = args[i++];
             if (arg.equals("-p")) {
                 prefix = args[i++];
             }
             else if (arg.equals("-f")) {
                 config = args[i++];
             }
             else if (arg.equals("-n")) {
                 newconfig = args[i++];
             }
             else if (arg.equals("-u")) {
                 urlBase = args[i++];
             }
             else if (arg.equals("-i")) {
                 ignoreErrors = true;
             }
             else if (arg.equals("-v")) {
                 verbose = true;
             }
             else {
                 usage();
                 System.exit(1);
             }
         }
 
         if (config == null) {
             usage();
             System.exit(1);
         }
 
         Netsize nsz = new Netsize(urlBase, prefix, verbose);
         nsz.configFile(config, newconfig, ignoreErrors);
 
     }
 
     public Netsize(String urlBase, String prefix, boolean verbose) {
         this.urlBase = urlBase;
         this.prefix = prefix;
         this.verbose = verbose;
     }
 
     public void configFile(String filename, String newconfig, boolean ignoreErrors) {
         this.configname = filename;
         Properties props = new Properties();
         try {
             long totsize = getSizeProperty(props, "totalsize");
             TotalSizer totals = new TotalSizer(filename, totsize);
            props.load(new FileInputStream(filename));
             String applist = props.getProperty("apps");
             if (applist == null) {
                 throw new IOException(filename + ": expected 'apps' property");
             }
             StringTokenizer tokens = new StringTokenizer(applist, ",");
             while (tokens.hasMoreTokens()) {
                 String token = tokens.nextToken();
                 long appsize = getSizeProperty(props, token + ".size");
                 AppSizer app = new AppSizer(totals, token, appsize);
                 for (int i=1; ;i++) {
                     String pname = token + "." + i;
                     String path = props.getProperty(pname + ".path");
                     if (path == null)
                         break;
                     long usize = getSizeProperty(props, pname + ".size");
                     new UrlSizer(app, urlBase, prefix, path, verbose, usize, ignoreErrors);
                 }
             }
             totals.connect();
             totals.report();
             if (newconfig != null) {
                 BufferedWriter bw = new BufferedWriter(new FileWriter(newconfig));
                 totals.generatePropertiesFile(bw);
                 bw.close();
             }
         }
         catch (IOException ioe) {
             System.err.println("Exception: " + ioe);
             ioe.printStackTrace();
             System.exit(1);
         }
     }
 
     public long getSizeProperty(Properties props, String propname)
     {
         String szstr = props.getProperty(propname);
         long sz = -1;
         if (szstr != null) {
             try {
                 sz = Long.parseLong(szstr);
             }
             catch (NumberFormatException nfe) {
                 System.err.println(configname + ": property '" + propname + "' is not numeric");
                 System.exit(1);
             }
         }
         return sz;
     }
 }
