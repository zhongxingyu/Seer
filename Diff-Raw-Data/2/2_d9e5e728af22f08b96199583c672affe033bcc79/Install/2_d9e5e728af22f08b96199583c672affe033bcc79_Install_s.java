 /*
  * Copyright (C) 2010-2013 Netcetera Switzerland (info@netcetera.com)
  * 
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 3 of the License, or (at your option)
  * any later version.
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
  * more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, see http://www.gnu.org/licenses/
  *
  * @(#) $Id: $
  */
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.math.BigInteger;
 import java.net.URL;
 import java.nio.channels.Channels;
 import java.nio.channels.ReadableByteChannel;
 import java.security.DigestInputStream;
 import java.security.MessageDigest;
 import java.util.Enumeration;
 import java.util.Scanner;
 import java.util.regex.Matcher;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 public class Install {
   private static final String TYPE_BIN         = "bin";
   private static final String TYPE_MODULES     = "modules";
   private static final String TYPE_AUX         = "aux";
   private static final String DEFAULT_REPO     = "ftp://ftp.netcetera.ch/pub/";
   private static final String DEFAULT_MANIFEST = DEFAULT_REPO + "/3DVegLab.manifest";
   
   public static void die(String msg) {System.err.println(msg); System.exit(1);}
   public static void fetch(String urlName, String targetName) throws Exception {
       URL url                 = new URL(urlName);
       ReadableByteChannel rbc = Channels.newChannel(url.openStream());
       FileOutputStream fos    = new FileOutputStream(targetName);
       fos.getChannel().transferFrom(rbc, 0, 1 << 24);
       fos.close();
   }
   public static String md5sum(String fName) throws Exception {
     MessageDigest md = MessageDigest.getInstance("MD5");
     InputStream   is = new FileInputStream(fName);
     try {
       byte[] buf = new byte[64 * 1024];
       is = new DigestInputStream(is, md); while (is.read(buf) != -1);
     }
     finally {is.close();}
     return String.format("%1$032x", new BigInteger(1,md.digest()));
   }
   public static void unzip(String baseDir, String zipPath) throws Exception {
     Enumeration<?> entries; ZipFile zipFile;
     zipFile = new ZipFile(zipPath); entries = zipFile.entries();
     while (entries.hasMoreElements()) {
       ZipEntry ent = (ZipEntry) entries.nextElement();
       if (ent.isDirectory()) {
         (new File(baseDir, ent.getName())).mkdir();
       } else {
         File newFile = new File(baseDir, ent.getName());
         InputStream   in = zipFile.getInputStream(ent);
         OutputStream out = new BufferedOutputStream(new FileOutputStream(newFile));
         byte[] buffer = new byte[1024]; int len;
         while ((len = in.read(buffer)) >= 0) out.write(buffer, 0, len);
         in.close(); out.close();
       }
     }
     zipFile.close();
   }
 
   private static File createDummyInput() throws Exception {
     File dummyInput = new File(System.getProperty("java.io.tmpdir"), "dummy.xml");
     BufferedWriter out = new BufferedWriter(new FileWriter(dummyInput));
     for (String line : new String[] {
         "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>",
         "<RequestList>",
         "   <Request type=\"VLAB\">",
         "     <Parameter name=\"3dScene\" value=\"Default RAMI\"/>",
         "     <OutputProduct file=\"vlab_out.dim\" format=\"BEAM-DIMAP\"/>",
         "   </Request>",
         "</RequestList>"}) {
       out.write(line + "\n");
     }
     out.close();
     return dummyInput;
   }
   private static File createRunScript(String bindir) throws Exception {
     System.out.println("Creating 3DVegLabProcessor script...");
     String fileName = null;
     String[] lines = null;
     if (System.getProperty("os.name").startsWith("Windows")) {
       fileName = "3DVegLab.bat";
       lines = new String[] { 
         "@echo off",
         "",
         "set BEAM4_HOME=" + new File(bindir, "..").getCanonicalPath(),
         "",
         "\"%BEAM4_HOME%\\jre\\bin\\java.exe\" ^",
         "    -Xmx1024M ^",
         "    -Dceres.context=beam ^",
         "    \"-Dbeam.mainClass=org.esa.beam.framework.processor.ProcessorRunner\" ^",
         "    \"-Dbeam.processorClass=com.netcetera.vlab.VLabProcessor\" ^",
         "    \"-Dbeam.home=%BEAM4_HOME%\" ^",
         "    \"-Dncsa.hdf.hdflib.HDFLibrary.hdflib=%BEAM4_HOME%\\modules\\lib-hdf-2.7\\lib\\jhdf.dll\" ^",
         "    \"-Dncsa.hdf.hdf5lib.H5.hdf5lib=%BEAM4_HOME%\\modules\\lib-hdf-2.7\\lib\\jhdf5.dll\" ^",
         "    -jar \"%BEAM4_HOME%\\bin\\ceres-launcher.jar\" %*",
         "",
         "exit /B %ERRORLEVEL%"
       };
     } else {
       fileName = "3DVegLab.sh";
       lines = new String[] { 
         "#! /bin/sh",
                                 "",
         "export BEAM4_HOME=" + new File(bindir, "..").getCanonicalPath(),
         "",
         ". \"$BEAM4_HOME/bin/detect_java.sh\"",
         "",
         "\"$app_java_home/bin/java\" \\",
         "    -Xmx1024M \\",
         "    -Dceres.context=beam \\",
         "    \"-Dbeam.mainClass=org.esa.beam.framework.processor.ProcessorRunner\" \\",
         "    \"-Dbeam.processorClass=com.netcetera.vlab.VLabProcessor\" \\",
         "    \"-Dbeam.home=$BEAM4_HOME\" \\",
         "    \"-Dncsa.hdf.hdflib.HDFLibrary.hdflib=$BEAM4_HOME/modules/lib-hdf-2.7/lib/libjhdf.so\" \\",
         "    \"-Dncsa.hdf.hdf5lib.H5.hdf5lib=$BEAM4_HOME/modules/lib-hdf-2.7/lib/libjhdf5.so\" \\",
         "    -jar \"$BEAM4_HOME/bin/ceres-launcher.jar\" \"$@\"",
         "",
         "exit $?"
       };
     }
     File runScript = new File(bindir, fileName);
     BufferedWriter out = new BufferedWriter(new FileWriter(runScript));
     for (String line : lines) {
       out.write(line + "\n");
     }
     out.close();
     runScript.setExecutable(true);
     return runScript;
   }
   private static String join(String[] args, String jstr) {
     String result = "";
     for (String s: args) {
       if (result == "") {
         result = s;
       } else {
         result = result + jstr + s;
       }
     }
     return result;
   }
   private static int run3DVegLabProcessor(File inputFile, File scriptFile) throws Exception {
     System.out.println("Running 3DVegLabProcessor...");
     String[] cmd;
     if (System.getProperty("os.name").startsWith("Windows")) {
      cmd = new String[] {"cmd", "/c", scriptFile.getCanonicalPath(), inputFile.getCanonicalPath(), "2>&1"};
     } else {
       cmd = new String[] {"sh", "-c", scriptFile.getCanonicalPath() + " " + inputFile.getCanonicalPath()  + " 2>&1"};
     }
     System.out.println("Executing: " + join(cmd, " "));
     ProcessBuilder pb = new ProcessBuilder(cmd);
     Process proc = pb.start();   
     // hack - collect but ignore output
     new Scanner( proc.getInputStream() ).useDelimiter("\\Z").next();
     return proc.waitFor();
   }
   private static boolean recursiveDelete(File path) {
     if (path.exists()) {
       File[] files = path.listFiles();
       for (int i = 0; i < files.length; i++) {
         if (files[i].isDirectory()) {
           recursiveDelete(files[i]);
         } else {
           files[i].delete();
         }
       }
     }
     return (path.delete());
   }
   
   private static void install(String repoURL, String manifestUrl) throws Exception {
     String cwd = new File(".").getCanonicalPath();
     String endPath = "beam-4.10.3:bin".replaceAll(":", Matcher.quoteReplacement(File.separator));
     if (!cwd.endsWith(endPath)) {
       die("Run me from inside a directory ending with: " + endPath + " (not: " + cwd + ")");
     }
     String bindir = cwd;
     String moddir = new File(cwd, ".." + File.separator + TYPE_MODULES).getAbsolutePath();
     String auxsuffix = ".beam:beam-vlab:auxdata".replaceAll(":", Matcher.quoteReplacement(File.separator));
     String auxdir = null;
     if (System.getProperty("os.name").startsWith("Windows")) {
       auxdir = new File(System.getenv("HOMEDRIVE")+System.getenv("HOMEPATH"), auxsuffix).getCanonicalPath();
     } else {
       auxdir = new File(System.getenv("HOME"), auxsuffix).getCanonicalPath();
     }
     File vlabaux = new File(auxdir, "..").getCanonicalFile();
     System.out.println("Clearing existing 3DVegLab auxdata: " + vlabaux);
     System.out.println("Succeeded? -> " + recursiveDelete(vlabaux));
     System.out.println("Fetching " + manifestUrl);
     Scanner sc = new Scanner(new URL(manifestUrl).openStream());
     String text = sc.useDelimiter("\\Z").next(); sc.close();
     System.out.println("Processing " + manifestUrl);
     for (String line : text.split("\n")) {
       String[] triple = line.split(":");
       String targetName = null;
       if (TYPE_BIN.equals(triple[1])) {
         targetName = bindir + File.separator + triple[2];
       } else if (TYPE_AUX.equals(triple[1])) {
         targetName = auxdir + File.separator + triple[2];
       } else if (TYPE_MODULES.equals(triple[1])) {
         for (File toDelete : new File(moddir).listFiles(new FilenameFilter() {
           public boolean accept(File directory, String fileName) {
               return fileName.startsWith("beam-3dveglab-vlab");
           }})) {
           System.out.println("Deleting " + toDelete.getCanonicalPath());
           toDelete.delete();
         }
         targetName = moddir + File.separator + triple[2];
       } else {
         die("unknown file locator: " + triple[1]);
       }
       System.out.println("Fetching " + triple[2] + " for " + triple[1]);
       fetch(repoURL + triple[2], targetName);
       System.out.println("Checking md5sum for " + triple[2]);
       String cksum = md5sum(targetName);
       if (!cksum.equals(triple[0])) {
         die("md5sum mismatch: expected=" + triple[0] + " actual=" + cksum);
       }
       if (TYPE_MODULES.equals(triple[1])) {
         File script = createRunScript(bindir);
         File dummy  = createDummyInput();
         run3DVegLabProcessor(dummy, script);
         if (! new File(auxdir).isDirectory()) {
           die("no auxdir - running 3DVegLab must have failed");
         }
       } else if (targetName.endsWith("zip")) {
         System.out.println("Unpacking " + targetName);
         unzip(new File(targetName, "..").getCanonicalPath(), targetName);
         System.out.println("Deleting " + targetName);
         new File(targetName).delete();
         String oldPath = targetName.substring(0, targetName.length()-4);
         String newPath = deriveName(oldPath);
         System.out.println("Renaming " + oldPath + " to " + newPath);
         new File(oldPath).renameTo(new File(newPath));
       } else if (targetName.endsWith(".tar.gz")) {
         if (! System.getProperty("os.name").startsWith("Windows")) {
           System.out.println("Unpacking " + targetName);
           String [] cmd = new String[] {"sh", "-c", "tar -C " + new File(targetName, "..").getCanonicalPath() + " -xzvf " + targetName};
           System.out.println("Running " + join(cmd, " "));
           ProcessBuilder pb = new ProcessBuilder(cmd);
           Process proc = pb.start();
           // hack - collect but ignore output
           new Scanner( proc.getInputStream() ).useDelimiter("\\Z").next();
           proc.waitFor();
           System.out.println("Deleting " + targetName);
           new File(targetName).delete();
           String oldPath = targetName.substring(0, targetName.length()-7);
           String newPath = deriveName(oldPath);
           System.out.println("Renaming " + oldPath + " to " + newPath);
           new File(oldPath).renameTo(new File(newPath));
         }
       }
     }
     System.out.println("Successfully completed.");
   }
   private static String deriveName(String oldName) throws Exception {
     String newName = "";
     String[] comps = oldName.split(Matcher.quoteReplacement(File.separator));
     String basename = comps[comps.length-1];
     String newBaseName = basename.split("[-_]")[0];
     if (oldName.contains("inux")) {
       newName = new File(oldName, "..").getCanonicalPath() + File.separator + newBaseName + "_" + "linux";
     } else if (oldName.contains("indows")) {
       newName = new File(oldName, "..").getCanonicalPath() + File.separator + newBaseName + "_" + "windows";
     }
     return newName;
   }
   public static void main(String[] args) throws Exception {
     switch (args.length){
       case 0:
         install(DEFAULT_REPO, DEFAULT_MANIFEST);
         break;
       case 2:
       case 3:
         if ("fetch".compareTo(args[0]) == 0) {
           String urlName         = args[1];
           String targetName      = null;
           String[] parts         = urlName.split(";type=");
           String[] components    = parts[0].split("/");
           targetName             = components[components.length - 1];
           if (args.length == 3) { targetName = args[2]; }
           fetch(urlName, targetName);
         } else if ("unzip".compareTo(args[0]) == 0) {
           if(args.length != 2) { die("unzip expects unzipPath"); }
           unzip(new File(".").getCanonicalPath(), args[1]);
         } else if ("repo".compareTo(args[0]) == 0) {
           if(args.length != 3) { die("repo expects repoURL manifestURL"); }
           install(args[1], args[2]);
         } else {
           die("unknown subcommand: " + args[0]); 
         }
         break;
       default:
         die("Invalid arguments");
         break;
     }
   }
 }
