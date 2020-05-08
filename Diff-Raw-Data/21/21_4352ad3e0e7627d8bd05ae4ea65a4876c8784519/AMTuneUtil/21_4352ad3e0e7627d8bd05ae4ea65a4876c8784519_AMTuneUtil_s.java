 /**
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright (c) 2008 Sun Microsystems, Inc. All Rights Reserved.
  *
  * The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: AMTuneUtil.java,v 1.1 2008-07-02 18:57:37 kanduls Exp $
  */
 
 package com.sun.identity.tune.util;
 
 import com.sun.identity.tune.common.FileHandler;
 import com.sun.identity.tune.common.MessageWriter;
 import com.sun.identity.tune.common.OutputReaderThread;
 import com.sun.identity.tune.common.AMTuneException;
 import com.sun.identity.tune.common.AMTuneLogger;
 import com.sun.identity.tune.config.WS7ContainerConfigInfo;
 import com.sun.identity.tune.constants.DSConstants;
 import com.sun.identity.tune.constants.AMTuneConstants;
 import com.sun.identity.tune.constants.WebContainerConstants;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import java.util.logging.Level;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * This class contains all the utility functions for Tuning.
  */
 
   public class AMTuneUtil implements AMTuneConstants {
     private static AMTuneLogger pLogger;
     private static MessageWriter mWriter;
     private static boolean isWindows2003;
     private static boolean isSunOs;
     private static boolean isLinux;
     private static String tempFile;
     private static Map sysInfoMap;
     private static boolean utilInit = true;
     private static String osArch;
     private static boolean isNiagara = false;
     private static String date;
     public static String TMP_DIR;
     /**
      * Initializes utils
      *
      * @return Returns <code>true<\code> isf initialization is successfull.
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     public static boolean initializeUtil()
     throws AMTuneException {
         date = new SimpleDateFormat(
                   "MM/dd/yyyy hh:mm:ss:SSS a zzz").format(new Date());
         pLogger = AMTuneLogger.getLoggerInst();
         mWriter = MessageWriter.getInstance();
         checkSystemEnv();
         setTmpDir();
         tempFile = AMTuneUtil.TMP_DIR + "perftune-temp.txt";
         sysInfoMap = new HashMap();
         try {
             if (isWindows2003()) {
                 getWinSystemInfo();
                 //remove the temp file
                 File temp = new File(tempFile);
                 if (temp.isFile()) {
                     temp.delete();
                 }
             } else if (isSunOs()) {
                 checkRootUser();
                 getSunOSSystemInfo();
             } else if (isLinux()) {
                 checkRootUser();
                 getLinuxSystemInfo();
             } else {
                 utilInit = false;
                 throw new AMTuneException("Unsupported OS.");
             }
             pLogger.log(Level.FINEST, "initializeUtil", 
                     "System configuration : " + sysInfoMap.toString());
         } catch (Exception ex) {
             pLogger.logException("perftuneutilinit", ex);
             utilInit = false;
             throw new AMTuneException(ex.getMessage());
         }
         return utilInit;
     }
     
     /**
      * Checks local box environment 
      */
     private static void checkSystemEnv() 
     throws AMTuneException {
         mWriter.writelnLocaleMsg("pt-checking-system-env");
         osArch = System.getProperty("os.arch");
         String osName = System.getProperty("os.name");
         String osVersion = System.getProperty("os.version");
         pLogger.log(Level.INFO, "checkSystemEnv", "OS name :" + osName);
         pLogger.log(Level.INFO, "checkSystemEnv", "OS Version :" + osVersion);
         pLogger.log(Level.INFO, "checkSystemEnv", "OS Arch :" + osArch);
         if (osName.equalsIgnoreCase(SUN_OS)){
             isSunOs = true;
             if (osArch.contains("sparc") || osArch.contains("86")) {
                 int solVersion = Integer.parseInt(osVersion.replace("5.", ""));
                 if (solVersion > 8) {
                     return;
                 } else {
                     mWriter.writelnLocaleMsg("pt-unsupported-sol");
                     throw new AMTuneException("Unsupported Solaris Version");
                 }
             } else {
                 mWriter.writelnLocaleMsg("pt-unsupported-arch");
                 throw new AMTuneException("Unsupported Architecture");
             }
         } else if (osName.equalsIgnoreCase(WINDOWS_2003)) {
             isWindows2003 = true;
         } else if (osName.equalsIgnoreCase(LINUX)) {
             isLinux = true;
             String rVersionFile = "/etc/redhat-release";
             try {
                 FileHandler fh = new FileHandler(rVersionFile);
                 String reqLine = fh.getLine("Red Hat");
                if (reqLine == null ) {
                     mWriter.writelnLocaleMsg("pt-unsupported-lnx");
                     throw new AMTuneException("Unsupported Linux ");
                 }
                 int relidx = reqLine.indexOf("release");
                 String ver = reqLine.substring(relidx + 8, relidx + 10 );
                 pLogger.log(Level.INFO, "checkSystemEnv", "Red Hat version " +
                         ver);
                 int version = Integer.parseInt(ver.trim()); 
                 if (version < 4) {
                     mWriter.writelnLocaleMsg("pt-unsupported-lnx");
                     throw new AMTuneException("Unsupported Red Hat Linux " +
                             version);
                 }
             } catch (FileNotFoundException fe) {
                 throw new AMTuneException("File not found " + rVersionFile);
             } catch (IOException ioe) {
                 throw new AMTuneException("Error reading file " + 
                         ioe.getMessage());
             } 
             
         } else {
             mWriter.writelnLocaleMsg("pt-unsupported-os");
             throw new AMTuneException("Unsupported Operating system.");
         }
     }
     /**
      * This method finds host name, domain, cpus and memory size by executing
      * native commands.
      */
     private static void getSunOSSystemInfo() 
     throws AMTuneException {
         try {
             getCommonNXSystemInfo();
             String nCmd = "/bin/uname -i";
             StringBuffer rBuf = new StringBuffer();
             int extVal = executeCommand(nCmd, rBuf);
             if (extVal == -1) {
                 mWriter.writeLocaleMsg("pt-unable-hw-pt");
                 mWriter.writelnLocaleMsg("pt-cannot-proceed");
                 throw new AMTuneException("Error finding hardware platform");
             } 
             String hwPlatform = rBuf.toString();
             if (hwPlatform != null && hwPlatform.trim().length() > 0) {
                 if (hwPlatform.length() > 15) {
                     hwPlatform = hwPlatform.substring(5, 15);
                 }
                 sysInfoMap.put(HWPLATFORM, hwPlatform.trim());
             } else {
                 mWriter.writeLocaleMsg("pt-unable-hw-pt");
                 mWriter.writelnLocaleMsg("pt-cannot-proceed");
                 throw new AMTuneException("Hardware platform is null.");
             }
             nCmd = "/usr/sbin/psrinfo";
             rBuf.setLength(0);
             extVal = executeCommand(nCmd, rBuf);
             if (extVal == -1) {
                 mWriter.writeLocaleMsg("pt-unable-no-cpu");
                 mWriter.writelnLocaleMsg("pt-cannot-proceed");
                 throw new AMTuneException("psrinfo command error.");
             }
             int noCpus = getWordCount(rBuf.toString(), "on-line");
             if (noCpus == 0) {
                 mWriter.writeLocaleMsg("pt-unable-no-cpu");
                 mWriter.writelnLocaleMsg("pt-cannot-proceed");
                 throw new AMTuneException("No of CPUs 0.");
             } else {
                 if (hwPlatform != null && hwPlatform.indexOf(NIAGARABOX) >= 0) {
                     isNiagara = true;
                     pLogger.log(Level.INFO, "getSunOSSystemInfo", 
                             "Tuning Niagarabox");
                     if (noCpus >= DIV_NUM_CPU) {
                         noCpus = noCpus / DIV_NUM_CPU;
                     } else {
                         noCpus = MIN_NUM_CPU;
                     }
                 }
                 sysInfoMap.put(PROCESSERS_LINE, Integer.toString(noCpus));
             }
             rBuf.setLength(0);
             nCmd = "/usr/sbin/prtconf";
             extVal = executeCommand(nCmd, rBuf);
             if (extVal == -1) {
                 mWriter.writeLocaleMsg("pt-unable-avmem");
                 mWriter.writelnLocaleMsg("pt-cannot-proceed");
                 throw new AMTuneException("prtconf command error.");
             } else {
                 StringTokenizer st = new StringTokenizer(rBuf.toString(), "\n");
                 while (st.hasMoreTokens()) {
                     String reqLine = st.nextToken();
                     if (reqLine.indexOf("Memory size: ") >= 0) {
                         String size = reqLine.replace("Memory size: ", "").
                                 replace(" Megabytes", "");
                         sysInfoMap.put(MEMORY_LINE, size);        
                     }
                 }
             }
         } catch (Exception ex) {
             pLogger.log(Level.SEVERE, "getSunOSSystemInfo", "Error finding " +
                     "SUNOS system information ");
             throw new AMTuneException(ex.getMessage());
         }
     }
     
     /**
      * Finds hostname and domain name in *unix OS Machines.
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private static void getCommonNXSystemInfo() 
     throws AMTuneException { 
         try {
             String nCmd = "/bin/domainname";
             StringBuffer rBuf = new StringBuffer();
             int extVal = executeCommand(nCmd, rBuf);
             if (extVal == -1) {
                 mWriter.writeLocaleMsg("pt-unable-domainname");
                 mWriter.writelnLocaleMsg("pt-cannot-proceed");
                 throw new AMTuneException("Domainname command error.");
             }
             String domainName = rBuf.toString();
             if ((domainName != null) && (domainName.length() > 1)) {
                 sysInfoMap.put(DOMAIN_NAME_LINE, domainName.trim());
             } else {
                 mWriter.writeLocaleMsg("pt-unable-domainname");
                 mWriter.writelnLocaleMsg("pt-cannot-proceed");
                 throw new AMTuneException("Domain name is null");
             }
             nCmd = "/bin/hostname";
             rBuf.setLength(0);
             extVal = executeCommand(nCmd, rBuf);
             if (extVal == -1) {
                 mWriter.writeLocaleMsg("pt-unable-hostname");
                 mWriter.writelnLocaleMsg("pt-cannot-proceed");
                 throw new AMTuneException("Hostname command error.");
             }
             String hostName = rBuf.toString();
             if ((hostName != null) && (hostName.trim().length() > 1)) {
                 if (hostName.indexOf(".") != -1) {
                     hostName = hostName.substring(0, hostName.indexOf("."));
                 }
                 sysInfoMap.put(HOST_NAME_LINE, hostName.trim() + "." +
                         domainName.trim());
             } else {
                 mWriter.writeLocaleMsg("pt-unable-hostname");
                 mWriter.writelnLocaleMsg("pt-cannot-proceed");
                 throw new AMTuneException("Host name is null");
             }
         } catch (Exception ex) {
             pLogger.log(Level.SEVERE, "getCommonNXSystemInfo", 
                     "Error finding hostname or domain name.");
             throw new AMTuneException(ex.getMessage());
         }
     }
     
     private static void getLinuxSystemInfo() 
     throws AMTuneException {
         try {
             getCommonNXSystemInfo();
             FileHandler fh = new FileHandler("/proc/meminfo");
             String memSize = fh.getLine("MemTotal:").replace("MemTotal:", "");
             memSize = memSize.replace("kB", "");
             memSize = memSize.trim();
             if (memSize != null ) {
                 int size = Integer.parseInt(memSize) / 1024;
                 sysInfoMap.put(MEMORY_LINE, Integer.toString(size)); 
             } else {
                 mWriter.writeLocaleMsg("pt-unable-avmem");
                 mWriter.writelnLocaleMsg("pt-cannot-proceed");
                 throw new AMTuneException("Couldn't find memory size.");
             }
             FileHandler fh2 = new FileHandler("/proc/cpuinfo");
             String[] lines = fh2.getMattchingLines("processor", false);
             if (lines.length >= 0) {
                 sysInfoMap.put(PROCESSERS_LINE, Integer.toString(lines.length));
             } else {
                 mWriter.writeLocaleMsg("pt-unable-no-cpu");
                 mWriter.writelnLocaleMsg("pt-cannot-proceed");
                 throw new AMTuneException("Couldn't find no of cpus.");
             }
         } catch (Exception ex) {
             pLogger.log(Level.SEVERE, "getLinuxSystemInfo", "Error finding" +
                     " LINUX System information");
             throw new AMTuneException(ex.getMessage());
         }
     }
     /**
      * Checks if the user is root or not.
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     private static void checkRootUser() 
     throws AMTuneException {
         String userName = System.getProperty("user.name");
         mWriter.writelnLocaleMsg("pt-check-user");
         if (userName.indexOf("root") < 0) {
             mWriter.writelnLocaleMsg("pt-should-be-root-user");
             throw new AMTuneException("Not root user.");
         }
     }
     /**
      * This method uses "systeminfo.exe" command for getting the system
      * information and constructs configuration map.
      *
      * @throws java.lang.Exception
      */
 
     private static void getWinSystemInfo()
     throws AMTuneException {
         try {
             String hostNameCmd = "cmd /C systeminfo > " + tempFile;
             StringBuffer rBuf = new StringBuffer();
             int extVal = executeCommand(hostNameCmd, rBuf);
             if (extVal == -1) {
                 throw new AMTuneException("Couldn't get System information " +
                         "tuning will not be done.");
             }
 
             FileHandler fh = new FileHandler(tempFile);
             String domainName;
             String reqLine = fh.getLine(DOMAIN_NAME_LINE);
             if ((reqLine != null) && (reqLine.length() > 1)) {
                 int startIdx = reqLine.lastIndexOf(":");
                 domainName = reqLine.substring(startIdx + 1).trim();
             } else {
                 mWriter.writeLocaleMsg("pt-unable-domainname");
                 mWriter.writelnLocaleMsg("pt-cannot-proceed");
                 throw new AMTuneException("Domain name is null");
             }
             String hostName;
             reqLine = fh.getLine(HOST_NAME_LINE);
             if ((reqLine != null) && (reqLine.length() > 1)) {
                 int startIdx = reqLine.lastIndexOf(":");
                 hostName = reqLine.substring(startIdx + 1).trim();
                 domainName = domainName.replace(hostName + ".", "");
                 sysInfoMap.put(HOST_NAME_LINE, hostName + "." + 
                         domainName.trim());
                 sysInfoMap.put(DOMAIN_NAME_LINE, domainName);
             } else {
                 mWriter.writeLocaleMsg("pt-unable-hostname");
                 mWriter.writelnLocaleMsg("pt-cannot-proceed");
                 throw new AMTuneException("Host name is null");
             }
             String numProcessors;
             reqLine = fh.getLine(PROCESSERS_LINE);
             if ((reqLine != null) && (reqLine.length() > 1)) {
                 int startIdx = reqLine.lastIndexOf(":");
                 numProcessors = reqLine.substring(startIdx + 1).trim();
                 numProcessors = numProcessors.substring(0, 2).trim();
                 sysInfoMap.put(PROCESSERS_LINE, numProcessors);
             } else {
                 mWriter.writeLocaleMsg("pt-unable-no-cpu");
                 mWriter.writelnLocaleMsg("pt-cannot-proceed");
                 throw new AMTuneException("Couldn't find number of " +
                         "processors.");
             }
             String memSize;
             reqLine = fh.getLine(MEMORY_LINE);
             if ((reqLine != null) && (reqLine.length() > 1)) {
                 int startIdx = reqLine.lastIndexOf(":");
                 memSize = reqLine.substring(startIdx + 1).trim();
                 StringTokenizer st = new StringTokenizer(memSize, " ");
                 st.hasMoreTokens();
                 memSize = st.nextToken();
                 memSize = memSize.replace(",", "");
                 sysInfoMap.put(MEMORY_LINE, memSize);
             } else {
                 mWriter.writeLocaleMsg("pt-unable-avmem");
                 mWriter.writelnLocaleMsg("pt-cannot-proceed");
                 throw new AMTuneException("Couldn't find number of " +
                         "processors.");
             }
             if (hostName != null && domainName != null) {
                 domainName = domainName.replace(hostName + ".", "");
                 sysInfoMap.put(HOST_NAME_LINE, hostName + "." + domainName);
             }
             fh.close();
         } catch (Exception ex) {
             pLogger.log(Level.SEVERE, "getWinSystemInfo", "Error finding " +
                     "system information ");
             throw new AMTuneException(ex.getMessage());
         }
     }
 
      /**
      * Returns FQDN
      *
      * @return Returns FQDN
      */
     public static String getHostName() {
        return (String)sysInfoMap.get(HOST_NAME_LINE) + "." + getDomainName();
     }
 
     /**
      * Returns Domain Name
      *
      * @return Returns Domain Name
      */
     public static String getDomainName() {
         return (String)sysInfoMap.get(DOMAIN_NAME_LINE);
     }
 
     /**
      * Returns Number of CPU's in the System
      *
      * @return Returns Number of CPU's in the System
      */
     public static String getNumberOfCPUS() {
         return (String)sysInfoMap.get(PROCESSERS_LINE);
     }
 
     /**
      * Returns current RAM size in KB
      *
      * @return Returns current RAM size in KB
      */
     public static String getSystemMemory() {
         return (String)sysInfoMap.get(MEMORY_LINE);
     }
     
     /**
      * Return Sun OS platform.
      */
     public static String getOSPlatform() {
         return osArch;
     }
     
     /**
      * Return Hardware Platform
      */
     public static String getHardWarePlatform() {
         return (String)sysInfoMap.get(HWPLATFORM);
     }
     /**
      * Executes the command and appends the result in the result buffer
      *
      * @param command Command string to be executed
      * @param resultBuffer Buffer containing the output of the command
      * @return exitValue Positive Integer value for success and -1 if any error.
      */
     public static int executeCommand(String command,
             StringBuffer resultBuffer) {
         pLogger.log(Level.FINEST, "executeCommand", "Executing command : " +
                     command);
         try {
             Process execProcess = null;
             execProcess = Runtime.getRuntime().exec(command);
             if (resultBuffer != null) {
                 resultBuffer.setLength(0);
             }
             OutputReaderThread outReaderThread =
                     new OutputReaderThread(execProcess.getInputStream());
             OutputReaderThread errorReaderThread =
                     new OutputReaderThread(execProcess.getErrorStream());
             outReaderThread.start();
             errorReaderThread.start();
             execProcess.waitFor();
             int exitValue = execProcess.exitValue();
             outReaderThread.join(3000);
             errorReaderThread.join(3000);
             execProcess.destroy();
             outReaderThread.interrupt();
             errorReaderThread.interrupt();
             boolean errorOccured = false;
             if (resultBuffer != null) {
                 StringBuffer outBuffer = outReaderThread.getBuffer();
                 StringBuffer errorBuffer = errorReaderThread.getBuffer();
                 if (outBuffer != null && outBuffer.length() != 0) {
                     pLogger.log(Level.FINEST, "executeCommand", 
                             "Out buffer content : " + outBuffer.toString());
                     resultBuffer.append(outBuffer.toString());
                 }
                 if (errorBuffer!=null && errorBuffer.length() != 0) {
                     pLogger.log(Level.FINEST, "executeCommand", 
                             "Error buffer content : " + errorBuffer.toString());
                     resultBuffer.append(errorBuffer.toString());
                     errorOccured = true;
                 }
             }
             pLogger.log(Level.INFO, "executeCommand", "Command exit value " +
                     exitValue);
             if (exitValue != 0 && errorOccured){
                 throw new AMTuneException("Error executing command. ");
             }
             return(exitValue);
         } catch (Exception ex) {
             pLogger.log(Level.SEVERE, "executeCommand", "Executing command " +
                     command + " failed.");
             pLogger.log(Level.SEVERE, "executeCommand", "Error is " + 
                     resultBuffer.toString());
             pLogger.logException("executeCommand", ex);
             resultBuffer.insert(0, ex.getMessage());
             return (-1);
         }
     }
     
     public static int executeScriptCmd(String command, 
             StringBuffer resultBuffer)
     throws AMTuneException {
         int extVal = -1;
         String tempF = AMTuneUtil.TMP_DIR + "amtunecmdhelper.sh";
         try {
             if (!AMTuneUtil.isWindows2003()) {
                 pLogger.log(Level.FINE, "executeScriptCmd",
                         "Command in the file :" + command);
                 //write the command to file and then execute the file
                 //workaround as famadm is not working directly from
                 //runtime in *unix if any option contains space character.
                 // -m "Sun DS with AM Schema"
                 File tempSh = new File(tempF);
                 BufferedWriter br =
                         new BufferedWriter(new FileWriter(tempSh));
                 br.write(command);
                 br.close();
                 extVal = executeCommand("chmod 777 " + tempF,
                         resultBuffer);
                 extVal = executeCommand(tempF, resultBuffer);
                 tempSh.delete();
             }
         } catch (Exception ex) {
             throw new AMTuneException (ex.getMessage());
         } finally {
             File tempSh = new File(tempF);
             if (tempSh.isFile()) {
                 tempSh.delete();
             }
         }
         return extVal;
       }
 
       /**
      *  Evaluates the expression in the form a/b
      *
      * @param divExp Expression to be evaluated
      * @return value of the Division.
      * @throws java.lang.NumberFormatException
      * @throws java.lang.NullPointerException
      */
     public static double evaluteDivExp(String divExp)
     throws NumberFormatException, NullPointerException {
         StringTokenizer st = new StringTokenizer(divExp, "/");
         st.hasMoreTokens();
         double operand1 = Double.parseDouble(st.nextToken().trim());
         st.hasMoreTokens();
         double operand2 = Double.parseDouble(st.nextToken().trim());
         return (double)operand1 / operand2;
     }
 
     /**
      *  Returns he directory size in KB
      *
      * @param directory Absolute path of the directory.
      * @return size Size of the directory in KB.
      */
     public static long getDirSize(String directory) {
         long size = 0;
         File instanceDir = new File(directory);
         String[] list = instanceDir.list();
         for (int i=0; i<list.length; i++) {
             File tFile = new File(directory + FILE_SEP + list[i]);
             if (tFile.isDirectory()) {
                 size += getDirSize(directory + FILE_SEP + list[i]);
             } else {
                 size += tFile.length();
             }
         }
         return size;
     }
 
     /**
      *  Replaces token in the buffer with the given value.
      *
      * @param buf Buffer in which token need to be replaced.
      * @param key Token that need to be replaced.
      * @param value Value.
      */
     public static void replaceToken(StringBuffer buf,
             String key,
             String value) {
         if (key == null || value == null || buf == null) {
             return;
         }
         int loc = 0, keyLen = key.length(), valLen = value.length();
         while ((loc = buf.toString().indexOf(key, loc)) != -1) {
             buf.replace(loc, loc + keyLen, value);
             loc = loc + valLen;
         }
     }
 
     /**
      * Returns random string.
      *
      * @return Random String.
      */
     public static String getRandomStr() {
 	String DATE_FORMAT = "yyyyMMdd";
 	DateFormat df = new SimpleDateFormat(DATE_FORMAT);
         String ranStr = Double.toString(Math.random()).substring(5);
         String rand = df.format(new Date(System.currentTimeMillis()))+ "-" +
                 ranStr;
         return rand;
     }
 
     /**
      * Copies source file to destination file
      *
      * @param source File to be copied
      * @param dest Destination File name
      * @throws java.lang.Exception if any errors occurs.
      */
     static public void CopyFile(File source, File dest)
     throws Exception {
         if (source == null || dest == null) {
             throw new IllegalArgumentException();
         }
 
         FileInputStream fis = new FileInputStream(source);
         FileOutputStream fos = new FileOutputStream(dest);
         byte[] buf = new byte[1024];
         int i = 0;
         while ((i = fis.read(buf)) != -1) {
             fos.write(buf, 0, i);
         }
         fis.close();
         fos.close();
     }
 
     /**
      * Returns <code>true</code> if OS is Windows 2003.
      *
      * @return <code>true</code> if OS is Windows 2003.
      */
     public static boolean isWindows2003() {
         return isWindows2003;
     }
     
     /**
      * Returns <code>true</code> if SUNOS.
      *
      * @return <code>true</code> if SUNOS.
      */
     public static boolean isSunOs() {
         return isSunOs;
     }
     
     /**
      * Return <code>true</code> if Linux.
      * 
      * @return <code>true</code> if Linux
      */
     public static boolean isLinux() {
         return isLinux;
     }
     
     /**
      * Return true if under laying hardware is Niagara box.
      * 
      */
     public static boolean isNiagara() {
         return isNiagara;
     }
     /**
      *  Returns last token in the string.
      *
      * @param stream String from which last token is required.
      * @param delim Delimiter to be used for creating the tokens.
      * @return val Last Token.
      */
     public static String getLastToken(String stream, String delim) {
         String val = " ";
         if (stream != null && stream.trim().length() > 0) {
             StringTokenizer st = new StringTokenizer(stream, delim);
             while (st.hasMoreTokens()) {
                 val = st.nextToken().trim();
             }
         }
         return val;
     }
 
     /**
      * This method copies the configuration file to directory specified by
      * second parameter under current directory.  
      * If the backup directory is not present it creates new directory
      * under current directory.
      *
      * @param confFile Configuration file name.
      * @param backupDir Directory name where config File need to be copied.
      *      This should be directory name only path should not be given.
      * @throws com.sun.identity.tune.common.AMTuneException
      */
     public static void backupConfigFile(String confFile, String backupDir)
     throws AMTuneException {
         try {
             File confF = new File(confFile);
             if (!confF.isFile()) {
                     mWriter.writelnLocaleMsg("pt-conf-file-missing");
                     throw new AMTuneException("Config file " + confFile +
                             " is missing." );
             }
             File bkDir = new File (getCurDir() + "../.." + FILE_SEP +
                     backupDir);
             if (!bkDir.isDirectory()) {
                 bkDir.mkdirs();
             }
             String baseFileName = confF.getName();
             String bkFileName = bkDir + FILE_SEP + baseFileName +
                     "-orig" + getRandomStr();
             mWriter.writeLocaleMsg("pt-bk-file");
             mWriter.writeln(" " + confFile + " to " + bkFileName);
             CopyFile(new File(confFile), new File(bkFileName));
         } catch (Exception ex) {
             pLogger.log(Level.SEVERE, "backupConfigFile",
                     "Couldn't backup file.");
             throw new AMTuneException(ex.getMessage());
         }
     }
     
     public static void backupConfigFile(String confFile)
     throws AMTuneException {
         try {
             File confF = new File(confFile);
             if (!confF.isFile()) {
                     mWriter.writelnLocaleMsg("pt-conf-file-missing");
                     throw new AMTuneException("Config file " + confFile +
                             " is missing." );
             }
             String bkDir = confF.getParent();
             String baseFileName = confF.getName();
             String bkFileName = bkDir + FILE_SEP + baseFileName +
                     "-orig" + getRandomStr();
             mWriter.writeLocaleMsg("pt-bk-file");
             mWriter.writeln(" " + confFile + " to " + bkFileName);
             CopyFile(new File(confFile), new File(bkFileName));
         } catch (Exception ex) {
             pLogger.log(Level.SEVERE, "backupConfigFile",
                     "Couldn't backup file.");
             throw new AMTuneException(ex.getMessage());
         }
     }
     
    /**
     * Returns current directory name
     *
     * @return flePath Absolute path of the current Directory.
     */
 
     public static String getCurDir() {
         File tempF = new File("tempdbk");
         String filePath = tempF.getAbsolutePath();
         filePath = filePath.replace("tempdbk", "");
         return filePath;
     }
 
     /**
      * This method is used to write buffer content into file.
      *
      * @param buf Content to be written into file
      * @param tFile temporary file name.
      * @throws com.sun.identity.tune.common.AMTuneException if any error
      * occurs while writing the content into file
      */
 
     public static void writeResultBufferToTempFile(StringBuffer buf,
             String tFile)
     throws AMTuneException {
         try {
             File f = new File(tFile);
             FileWriter fw = new FileWriter(new File(tFile));
             fw.write(buf.toString());
             fw.flush();
             fw.close();
 
         } catch (Exception ex) {
             pLogger.log(Level.SEVERE, "writeResultBufferToTempFile",
                     "Couldn't write buffer to file.");
             throw new AMTuneException(ex.getMessage());
         }
     }
     
     public static String getASJVMOption(List curOptList, String reqFlag, 
             boolean lastToken) {
         Iterator optItr = curOptList.iterator();
         while (optItr.hasNext()) {
             String curFlag = (String) optItr.next();
             if (curFlag.indexOf(reqFlag) != -1) {
                 if (lastToken) {
                     return getLastToken(curFlag, PARAM_VAL_DELIM);
                 } else {
                     return curFlag;
                 }
             }
         }
         return NO_VAL_SET;
     }
     
     /**
      * Count number of times a word has repeated
      */
     public static int getWordCount(String str, String reqWord) {
         StringTokenizer st = new StringTokenizer(str, "\n");
         pLogger.log(Level.FINEST, "getWordCount", "Source string :" + str);
         pLogger.log(Level.FINEST, "getWordCount", "Word to find :" + reqWord);
         int count = 0;
         while (st.hasMoreTokens()) {
             String reqStr = st.nextToken();
             if (reqStr.contains(reqWord)) {
                 ++count;
             }
         }
         return count;
     }
     
     /**
      * Returns matched pattern lines.
      */
       public static String[] getMatchedLines(String[] lines, String pattern) {
           List matList = new ArrayList();
           Pattern p = Pattern.compile(pattern);
           int size = lines.length;
           int i = 0;
           for (i = 0; i < size; i++) {
               Matcher m = p.matcher(lines[i]);
               if (m.find()) {
                   matList.add(lines[i]);
               }
           }
           String[] arr = new String[matList.size()];
           for (i = 0; i < matList.size(); i++) {
               arr[i] = new String(matList.get(i).toString());
           }
           return arr;
       }
       
       /**
        * Return matching line.
        */
       public static String getMatchedLine(List lines, String pattern) {
           Iterator itr = lines.iterator();
           while (itr.hasNext()) {
               String curLine = itr.next().toString();
               if (curLine.indexOf(pattern) != -1) {
                   return curLine;
               }
           }
           return null;
       }
       /**
        * Return current date
        */
       public static String getTodayDateStr() {
           return date;
       }
       
       /**
      * Print error message.
      * @param propertyName
      */
     public static void printErrorMsg(String propertyName) {
         mWriter.writelnLocaleMsg("pt-cannot-proceed");
         mWriter.writeLocaleMsg("pt-conf-parm-cust-msg");
         mWriter.writeln(propertyName);
     }
     
     /**
      * Return tokens in a string
      * 
      */
     public static List getTokensList(String line, String delim) {
         StringTokenizer str = new StringTokenizer(line, delim);
         List tokens = new ArrayList();
         while (str.hasMoreTokens()) {
             tokens.add(str.nextToken());
         }
         return tokens;
     }
     
     /**
      * This method restarts the Web Server 7 using wadm tool.
      */
     public static void reStartWS7Serv(WS7ContainerConfigInfo wsConfigInfo) {
         try {
             pLogger.log(Level.INFO, "reStartServ", "Deploying configuration.");
             StringBuffer restartCmd = 
                     new StringBuffer(wsConfigInfo.getWSAdminCmd());
             restartCmd.append(WebContainerConstants.WADM_RESTART_SUB_CMD);
             restartCmd.append(wsConfigInfo.getWSAdminCommonParams());
             StringBuffer resultBuffer = new StringBuffer();
             int retVal = AMTuneUtil.executeCommand(restartCmd.toString(),
                     resultBuffer);
             if (retVal == -1) {
                 pLogger.log(Level.SEVERE, "reStartServ",
                         "Error executing command " + restartCmd);
             }
         } catch (Exception ex) {
             pLogger.log(Level.SEVERE, "reStartServ",
                     "Restart failed. " + ex.getMessage());
         }
     }
     /**
      * Return true if tuning is supported for web container
      * @param webContainer Short form of the web container
      * @return
      */
     public static boolean isSupportedWebContainer(String webContainer) {
         if (webContainer.indexOf(WebContainerConstants.WS7_CONTAINER) != -1 ||
                 webContainer.indexOf(
                 WebContainerConstants.AS91_CONTAINER) != -1) {
             return true;
         } else {
             return false;
         }
             
     }
     
     /**
      * Return true if the DS version is supported for storing FAM Service data.
      * 
      */
     public static boolean isSupportedSMDSVersion(String dsVersion) {
         if ((dsVersion.indexOf(DSConstants.DS63_VERSION) != -1 ||
                 dsVersion.indexOf(DSConstants.DS5_VERSION)!= -1 ||
                 dsVersion.indexOf(DSConstants.OPEN_DS) != -1) && 
                 !dsVersion.equalsIgnoreCase(DSConstants.DS62_VERSION)) {
             return true;
         } else {
             return false;
         }
     }
     
     /**
      * Return true if the DS version is supported for storing User information.
      * 
      */
     public static boolean isSupportedUMDSVersion(String dsVersion) {
         if ((dsVersion.indexOf(DSConstants.DS63_VERSION) != -1 ||
                 dsVersion.indexOf(DSConstants.DS5_VERSION)!= -1) && 
                 !dsVersion.equalsIgnoreCase(DSConstants.DS62_VERSION)) {
             return true;
         } else {
             return false;
         }
     }
     
     /**
      * Set temp Directory based on the platform
      */
     private static void setTmpDir() {
         if (isSunOs() || isLinux()) {
             TMP_DIR = "/tmp" + FILE_SEP;
         } else {
             TMP_DIR = System.getProperty("java.io.tmpdir");
         }
     }
 }
