 /*
  * Copyright luntsys (c) 2004-2005,
  * Date: 2004-5-10
  * Time: 9:54:34
  *
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met: 1.
  * Redistributions of source code must retain the above copyright notice, this
  * list of conditions and the following disclaimer. 2. Redistributions in
  * binary form must reproduce the above copyright notice, this list of
  * conditions and the following disclaimer in the documentation and/or other
  * materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  */
 package com.luntsys.luntbuild.utility;
 
 import com.luntsys.luntbuild.ant.Commandline;
 import com.luntsys.luntbuild.builders.AntBuilder;
 import com.luntsys.luntbuild.builders.MavenBuilder;
 import com.luntsys.luntbuild.builders.Maven2Builder;
 import com.luntsys.luntbuild.builders.CommandBuilder;
 import com.luntsys.luntbuild.builders.RakeBuilder;
 import com.luntsys.luntbuild.dao.Dao;
 import com.luntsys.luntbuild.db.User;
 import com.luntsys.luntbuild.facades.Constants;
 import com.luntsys.luntbuild.facades.ILuntbuild;
 import com.luntsys.luntbuild.listeners.ListenerSample;
 import com.luntsys.luntbuild.notifiers.BlogNotifier;
 import com.luntsys.luntbuild.notifiers.EmailNotifier;
 import com.luntsys.luntbuild.notifiers.MsnNotifier;
 import com.luntsys.luntbuild.notifiers.JabberNotifier;
 import com.luntsys.luntbuild.notifiers.SametimeNotifier;
 import com.luntsys.luntbuild.security.SecurityHelper;
 import com.luntsys.luntbuild.services.IScheduler;
 import com.luntsys.luntbuild.vcs.*;
 import org.acegisecurity.AuthenticationManager;
 import ognl.Ognl;
 import ognl.OgnlException;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.log4j.*;
 import org.apache.tapestry.ApplicationRuntimeException;
 import org.apache.tapestry.IRequestCycle;
 import org.apache.tapestry.multipart.DefaultMultipartDecoder;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.taskdefs.Delete;
 import org.apache.tools.ant.taskdefs.Mkdir;
 import org.apache.tools.ant.taskdefs.Move;
 import org.apache.tools.ant.taskdefs.Touch;
 import org.apache.tools.ant.types.FileSet;
 import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.web.context.support.XmlWebApplicationContext;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.*;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.Statement;
 
 /**
  * This is a utility class to provides easy access for some commonly used
  * objects and functions
  *
  * @author robin shine
  */
 public class Luntbuild {
 	private static final String JDBC_DRIVER_PROPERTY = "jdbc.driverClassName";
 	private static final String HSQLDB_DRIVER = "org.hsqldb.jdbcDriver";
 
     private static final String VCS_PACKAGE_NAME = "com.luntsys.luntbuild.vcs";
     private static final String NOTIFIER_PACKAGE_NAME = "com.luntsys.luntbuild.notifiers";
     private static final String LISTENER_PACKAGE_NAME = "com.luntsys.luntbuild.listeners";
     private static final String BUILDER_PACKAGE_NAME = "com.luntsys.luntbuild.builders";
 
     public static final String SPRING_CONFIG_LOCATION = "/WEB-INF/applicationContext.xml";
 
     public static final String TRIGGER_NAME_SEPERATOR = "$";
     private static Log logger = LogFactory.getLog(Luntbuild.class);
 
     // Luntbuild system level properties
     private static Map properties;
 
     public static final int DEFAULT_PAGE_REFRESH_INTERVAL = 15;
     public static final int DEFAULT_MAIL_PORT = 25;
     /**
      * The block size when operating files
      */
     public static final int FILE_BLOCK_SIZE = 25000;
 
     /**
      * The date display format at luntbuild web interface
      */
     public static final SimpleDateFormat DATE_DISPLAY_FORMAT =
             new SimpleDateFormat("yyyy-MM-dd HH:mm");
 
     /**
      * Log4j system log - HTML
      */
     public static final String log4jFileName = "luntbuild_log.html";
 
     /**
      * Log4j system log - Text
      */
     public static final String log4jFileNameTxt = "luntbuild_log.txt";
 
     /**
      * The application wide context for use in spring framework
      */
     public static XmlWebApplicationContext appContext;
 
     /**
      * The installation directory for luntbuild
      */
     public static String installDir;
 
     public static Properties buildInfos;
 
     /**
      * List of vcs adaptor classes found in the system
      */
     public static List vcsAdaptors;
 
     /**
      * List of notifier classes found in the system
      */
     public static List notifiers;
 
     /**
      * List of listener classes found in the system
      */
     public static List listeners;
 
     /**
      * List of builders classes found in the system
      */
     public static List builders;
 
     public static int pageRefreshInterval;
 
     /**
      * Provides easy access to data access object
      *
      * @return
      */
     public static Dao getDao() {
         String message;
         if (appContext == null) {
             message = "Application context not initialized!";
             logger.error(message);
             throw new RuntimeException(message);
         }
         Dao dao = (Dao) appContext.getBean("dao");
         if (dao == null) {
             message = "Failed to find bean \"dao\" in application context!";
             logger.error(message);
             throw new RuntimeException(message);
         }
         return dao;
     }
 
     /**
      * Provides easy access to sched service object
      *
      * @return
      * @throws RuntimeException
      */
     public static IScheduler getSchedService() {
         String message;
         if (appContext == null) {
             message = "Application context not initialized!";
             logger.error(message);
             throw new RuntimeException(message);
         }
         IScheduler scheduler = (IScheduler) appContext.getBean("scheduler");
         if (scheduler == null) {
             message = "Failed to find bean \"scheduler\" in application context!";
             logger.error(message);
             throw new RuntimeException(message);
         }
         return scheduler;
     }
 
     public static AuthenticationManager getAuthenticationManager() {
         String message;
         if (appContext == null) {
             message = "Application context not initialized!";
             logger.error(message);
             throw new RuntimeException(message);
         }
         AuthenticationManager authManager = (AuthenticationManager) Luntbuild.appContext.getBean("authenticationManager");
         if (authManager == null) {
             message = "Failed to find bean \"authenticationManager\" in application context!";
             logger.error(message);
             throw new RuntimeException(message);
         }
         return authManager;
     }
 
     /**
      * Provides easy access to luntbuild service object
      *
      * @return
      * @throws RuntimeException
      */
     public static com.luntsys.luntbuild.facades.ILuntbuild getLuntbuildService() {
         String message;
         if (appContext == null) {
             message = "Application context not initialized!";
             logger.error(message);
             throw new RuntimeException(message);
         }
         ILuntbuild luntbuildService = (com.luntsys.luntbuild.facades.ILuntbuild) appContext.getBean("luntbuildService");
         if (luntbuildService == null) {
             message = "Failed to find bean \"luntbuildService\" in application context!";
             logger.error(message);
             throw new RuntimeException(message);
         }
         return luntbuildService;
     }
 
     /**
      * This class will dig all messages for a throwable object
      *
      * @param throwable
      * @return
      */
     public static String getExceptionMessage(Throwable throwable) {
         String message;
         if (throwable instanceof OgnlException) {
             OgnlException ognlException = (OgnlException) throwable;
             message = ognlException.getClass().getName();
             if (ognlException.getMessage() != null)
                 message += "(" + ognlException.getMessage() + ")";
             if (ognlException.getReason() != null) {
                 message += "\nReason: " + ognlException.getReason().getClass().getName();
                 if (ognlException.getReason().getMessage() != null)
                     message += "(" + ognlException.getReason().getMessage() + ")";
             }
             return message;
         } else {
             message = throwable.getClass().getName();
             if (throwable.getMessage() != null)
                 message += "(" + throwable.getMessage() + ")";
             Throwable cause = throwable.getCause();
             while (cause != null) {
                 message += "\nCause: " + cause.getClass().getName();
                 if (cause.getMessage() != null)
                     message += "(" + cause.getMessage() + ")";
                 cause = cause.getCause();
             }
             return message;
         }
     }
 
     /**
      * This method use an ant task to delete a directory and all the contents inside of it
      *
      * @param directory
      * @throws org.apache.tools.ant.BuildException
      *          when delete fails
      */
     public static void deleteDir(String directory) {
         try {
             Delete deleteTask = new Delete();
             deleteTask.setProject(new org.apache.tools.ant.Project());
             deleteTask.getProject().init();
             deleteTask.setDir(new File(directory));
             deleteTask.setFailOnError(false);
             deleteTask.execute();
         } catch (Exception e) {
             logger.error("IOException during deleteDir: ", e);
         }
     }
 
     public static void renameDir(String from, String to) {
         try {
             File normalizedFromFile = new File(from).getCanonicalFile();
             File normalizedToFile = new File(to).getCanonicalFile();
             if (normalizedFromFile.compareTo(normalizedToFile) == 0 || !normalizedFromFile.exists())
                 return;
             Move moveTask = new Move();
             moveTask.setProject(new org.apache.tools.ant.Project());
             moveTask.getProject().init();
             moveTask.setPreserveLastModified(true);
             deleteDir(normalizedToFile.getAbsolutePath());
             moveTask.setTodir(normalizedToFile);
             FileSet fileSet = new FileSet();
             fileSet.setDir(normalizedFromFile);
             fileSet.setDefaultexcludes(false);
             moveTask.addFileset(fileSet);
             moveTask.execute();
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * Delete all contents inside a directory
      *
      * @param directory
      * @throws org.apache.tools.ant.BuildException
      *          when cleanup fails
      */
     public static void cleanupDir(String directory) {
         deleteDir(directory);
         createDir(directory);
     }
 
     /**
      * This method use ant task to delete a file
      *
      * @param file
      * @throws org.apache.tools.ant.BuildException
      *
      */
     public static void deleteFile(String file) {
         try {
             Delete deleteTask = new Delete();
             deleteTask.setProject(new org.apache.tools.ant.Project());
             deleteTask.getProject().init();
             deleteTask.setFile(new File(file));
             deleteTask.setFailOnError(false);
             deleteTask.execute();
         } catch (Exception e) {
             logger.error("IOException during deleteFile: ", e);
         }
     }
 
     /**
      * This method use ant task to creates a new directory
      *
      * @param directory
      * @throws org.apache.tools.ant.BuildException
      *
      */
     public static void createDir(String directory) {
         Mkdir mkdirTask = new Mkdir();
         mkdirTask.setProject(new org.apache.tools.ant.Project());
         mkdirTask.getProject().init();
         mkdirTask.setDir(new File(directory));
         mkdirTask.execute();
     }
 
     /**
      * Touch a file to update its modification time, if this file is not exist, it will be
      * created
      *
      * @param file
      * @throws org.apache.tools.ant.BuildException
      *
      */
     public static void touchFile(String file) {
         Touch touchTask = new Touch();
         touchTask.setProject(new org.apache.tools.ant.Project());
         touchTask.getProject().init();
         touchTask.setFile(new File(file));
         touchTask.execute();
     }
 
     public static void sendFile(IRequestCycle cycle, String filePath) {
         sendFile(cycle.getRequestContext().getRequest(), cycle.getRequestContext().getResponse(),
                 filePath);
     }
 
     /**
      * Sends the content of the specified file to the web browser
      *
      * @param filePath using the header user-agent
      */
     public static void sendFile(HttpServletRequest request, HttpServletResponse response, String filePath) {
         response.reset();
         BufferedReader reader = null;
         FileInputStream in = null;
         File file = new File(filePath);
         try {
             if (filePath.endsWith(".txt")) {
                 // convert EOL flag of log text file to the EOL flag of requesting host
                 response.setHeader("Content-disposition", "inline;filename=" + file.getName());
                 response.setContentType("text/plain");
                 reader = new BufferedReader(new FileReader(file));
                 ServletOutputStream out = response.getOutputStream();
                 String line;
                 int length = 0;
                 String eol = getEol(request);
                 List lines = new ArrayList();
                 while ((line = reader.readLine()) != null) {
                     length += line.getBytes().length;
                     length += eol.getBytes().length;
                     lines.add(line + eol);
                 }
                 response.setContentLength(length);
 
                 Iterator it = lines.iterator();
                 while (it.hasNext()) {
                     String s = (String) it.next();
                     out.write(s.getBytes());
                 }
             } else {
                 response.setHeader("Content-disposition", "filename=" + file.getName());
                 String contentType = request.getSession().
                         getServletContext().getMimeType(filePath);
                 if (Luntbuild.isEmpty(contentType))
                     response.setContentType("application/octet-stream");
                 else
                     response.setContentType(contentType);
                 in = new FileInputStream(filePath);
                 byte[] data = new byte[FILE_BLOCK_SIZE];
                 response.setContentLength(new Long(file.length()).intValue());
                 ServletOutputStream out = response.getOutputStream();
                 while (true) {
                     int bytesRead = in.read(data);
                     if (bytesRead < 0)
                         break;
                     out.write(data, 0, bytesRead);
                 }
             }
             response.flushBuffer();
         } catch (IOException e) {
             logger.error("IOException during sendFile: ", e);
             throw new ApplicationRuntimeException(e);
         } finally {
             try {
                 if (in != null)
                     in.close();
                 if (reader != null)
                     reader.close();
             } catch (IOException e) {
                 logger.error("Failed to close file: " + filePath, e);
             }
         }
     }
 
     /**
      * Sends the content of the specified URL to the web browser
      *
      * @param filePath using the header user-agent
      */
     public static void sendFile(HttpServletRequest request, HttpServletResponse response, URL url) {
         response.reset();
         URLConnection connection = null;
         InputStream in = null;
 
         try {
             connection = url.openConnection();
             File file = new File(url.getFile());
             response.setHeader("Content-disposition", "filename=" + file.getName());
             String contentType = request.getSession().
                     getServletContext().getMimeType(file.getName());
             if (Luntbuild.isEmpty(contentType))
                 response.setContentType("application/octet-stream");
             else
                 response.setContentType(contentType);
             in = connection.getInputStream();
             byte[] data = new byte[FILE_BLOCK_SIZE];
             response.setContentLength(connection.getContentLength());
             ServletOutputStream out = response.getOutputStream();
             int read = 0;
             while ( (read = in.read(data, 0, FILE_BLOCK_SIZE)) > -1) {
                 out.write(data, 0, read);
             }
             response.flushBuffer();
         } catch (IOException e) {
             logger.error("IOException during sendFile: ", e);
             throw new ApplicationRuntimeException(e);
         } finally {
             try {
                 if (in != null)
                     in.close();
             } catch (IOException e) {
                 logger.error("Failed to close URL: " + url.toExternalForm(), e);
             }
         }
     }
 
     public static String getEol(HttpServletRequest request) {
         String userAgent = request.getHeader("user-agent");
         if (userAgent == null)
             return "\n";
         if (userAgent.matches(".*Windows.*"))
             return "\r\n";
         return "\n";
     }
 
     /**
      * Calculate the actual date time by specifying a time of the format hh:mm of today
      *
      * @param hhmm
      * @return
      */
     public static Date getDateByHHMM(String hhmm) {
         try {
             SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
             Date timeDate = sdf.parse(hhmm);
             Calendar timeCal = Calendar.getInstance(); // only time part of this calendar will be used
             timeCal.setTime(timeDate);
             Calendar dateCal = Calendar.getInstance(); // only date part of this calendar will be used
             dateCal.setTimeInMillis(System.currentTimeMillis());
             dateCal.set(Calendar.HOUR_OF_DAY, timeCal.get(Calendar.HOUR_OF_DAY));
             dateCal.set(Calendar.MINUTE, timeCal.get(Calendar.MINUTE));
             dateCal.set(Calendar.SECOND, 0);
             dateCal.set(Calendar.MILLISECOND, 0);
             return dateCal.getTime();
         } catch (ParseException ex) {
             throw new RuntimeException(ex);
         }
     }
 
     /**
      * Convert the version to a label that propers to applied to various version control system.
      *
      * @param version
      * @return
      */
     public static String getLabelByVersion(String version) {
         String label = version.trim().replace('.', '_').replaceAll("[\\s]", "-");
         if (!label.matches("^[a-zA-Z].*"))
             label = "v" + label;
         return label;
     }
 
     /**
      * Concatenate two path into one path, the main functionality is to detect the
      * trailing "/" of path1, and leading "/" of path2, and forms only one "/" in the joined
      * path
      *
      * @param path1
      * @param path2
      * @return
      */
     public static String concatPath(String path1, String path2) {
         if (isEmpty(path1)) {
             if (path2 == null)
                 return "";
             else
                 return path2.trim();
         }
         if (isEmpty(path2))
             return path1.trim();
 
         String trimmedPath1 = path1.trim();
         String trimmedPath2 = path2.trim();
         String path;
         if (trimmedPath1.charAt(trimmedPath1.length() - 1) == '/' ||
                 trimmedPath1.charAt(trimmedPath1.length() - 1) == '\\') {
             if (trimmedPath2.charAt(0) == '/' || trimmedPath2.charAt(0) == '\\') {
                 if (trimmedPath1.length() == 1)
                     path = trimmedPath2;
                 else {
                     path = trimmedPath1.substring(0, trimmedPath1.length() - 2);
                     path += trimmedPath2;
                 }
             } else
                 path = trimmedPath1 + trimmedPath2;
         } else {
             if (trimmedPath2.charAt(0) == '/' || trimmedPath2.charAt(0) == '\\')
                 path = trimmedPath1 + trimmedPath2;
             else
                 path = trimmedPath1 + '/' + trimmedPath2;
         }
         return path;
     }
 
     /**
      * Removes the leading '/' or '\' character from the path
      *
      * @param path
      * @return
      */
     public static String removeLeadingSlash(String path) {
         if (path == null || path.trim().equals(""))
             return "";
         String trimmedPath = path.trim();
         if (trimmedPath.charAt(0) == '/' || trimmedPath.charAt(0) == '\\') {
             if (trimmedPath.length() == 1)
                 return "";
             else
                 return trimmedPath.substring(1);
         } else
             return trimmedPath;
     }
 
     /**
      * Removes the trailing '/' or '\' character from the path
      *
      * @param path
      * @return
      */
     public static String removeTrailingSlash(String path) {
         if (isEmpty(path))
             return "";
         String trimmedPath = path.trim();
         if (trimmedPath.charAt(trimmedPath.length() - 1) == '/' ||
                 trimmedPath.charAt(trimmedPath.length() - 1) == '\\') {
             if (trimmedPath.length() == 1)
                 return "";
             else
                 return trimmedPath.substring(0, trimmedPath.length() - 2);
         } else
             return trimmedPath;
     }
 
     /**
      * Return the lunt build logger for specified antProject, or return null if
      * it does not contain a lunt build logger
      *
      * @param antProject
      * @return
      */
     public static LuntbuildLogger getLuntBuildLogger(Project antProject) {
         Iterator itListener = antProject.getBuildListeners().iterator();
         while (itListener.hasNext()) {
             Object o = itListener.next();
             if (o instanceof LuntbuildLogger)
                 return (LuntbuildLogger) o;
         }
         return null;
     }
 
     /**
      * Determines if the specified string is empty
      *
      * @param aString
      * @return
      */
     public static boolean isEmpty(String aString) {
         if (aString == null || aString.trim().equals(""))
             return true;
         else
             return false;
     }
 
     /**
      * Create a cloned copy of specified module
      *
      * @param module
      * @return
      */
     public static Vcs.Module cloneModule(Vcs vcs, Vcs.Module module) {
         Vcs.Module clone = vcs.createNewModule();
         if (clone == null)
             throw new RuntimeException("Module clone operation failed because " +
                     "supplied vcs object does not support module!");
 
         List srcProperties = module.getProperties();
         List dstProperties = clone.getProperties();
         for (int i = 0; i < srcProperties.size(); i++) {
             DisplayProperty srcProperty = (DisplayProperty) srcProperties.get(i);
             DisplayProperty dstProperty = (DisplayProperty) dstProperties.get(i);
             dstProperty.setValue(srcProperty.getValue());
         }
 
         return clone;
     }
 
     public static String getServletUrl() {
         String servletUrl = (String) properties.get("servletUrl");
         if (isEmpty(servletUrl))
             return "http://" + getIpAddress() + ":8080/luntbuild/app.do";
         else
             return servletUrl;
     }
 
     /**
      * Get the host name of the build server
      *
      * @return
      */
     public static String getHostName() {
         try {
             InetAddress addr = InetAddress.getLocalHost();
             return addr.getHostName();
         } catch (UnknownHostException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * Get the ip address of the build server
      *
      * @return
      */
     public static String getIpAddress() {
         try {
             InetAddress addr = InetAddress.getLocalHost();
             return addr.getHostAddress();
         } catch (UnknownHostException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * Determines if specified shorter file recursively contains specified longer file.
      * Dir will recursively contains longer file when the following conditions meet at same time:
      * <i> specified shorter is a directory
      * <i> specified longer is a sub-directory or longer recursively under shorter
      *
      * @param shorter
      * @param longer
      * @return path of longer relative to shorter, null if longer is not relative to shorter, or "" if
      *         shorter is a directory and is the same as longer
      * @throws RuntimeException
      */
     public static String parseRelativePath(File shorter, File longer) {
         try {
             if (!shorter.isDirectory())
                 return null;
 
             String shorterPath = removeTrailingSlash(shorter.getCanonicalFile().getAbsolutePath());
             String longerPath = removeTrailingSlash(longer.getCanonicalFile().getAbsolutePath());
 
             if (longerPath.startsWith(shorterPath))
                 return longerPath.substring(shorterPath.length());
             else
                 return null;
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * Filter given list of notifier class names to get a list of existing notifier classes
      *
      * @param notifierClassNames list of notifier class names, should not be null
      * @return list of existing notifier classes
      */
     public static List getNotifierClasses(List notifierClassNames) {
         List notifierClasses = new ArrayList();
         Iterator itClassName = notifierClassNames.iterator();
         while (itClassName.hasNext()) {
             String notifierClassName = (String) itClassName.next();
             Iterator itClass = notifiers.iterator();
             while (itClass.hasNext()) {
                 Class notifierClass = (Class) itClass.next();
                 if (notifierClass.getName().equals(notifierClassName)) {
                     notifierClasses.add(notifierClass);
                     break;
                 }
             }
         }
         return notifierClasses;
     }
 
     /**
      * Convert list of notifier classes to list of notifier instances
      *
      * @param notifierClasses should not be null
      * @return
      */
     public static List getNotifierInstances(List notifierClasses) {
         List notifierInstances = new ArrayList();
         Iterator it = notifierClasses.iterator();
         while (it.hasNext()) {
             Class notifierClass = (Class) it.next();
             try {
                 notifierInstances.add(notifierClass.newInstance());
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
         }
         return notifierInstances;
     }
 
     /**
      * Convert list of listener classes to list of listener instances
      *
      * @param listenerClasses should not be null
      * @return
      */
     public static List getListenerInstances(List listenerClasses) {
         List listenerInstances = new ArrayList();
         Iterator it = listenerClasses.iterator();
         while (it.hasNext()) {
             Class listenerClass = (Class) it.next();
             try {
                 listenerInstances.add(listenerClass.newInstance());
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
         }
         return listenerInstances;
     }
 
     public static String getSystemLogUrl() {
         String servletUrl = getServletUrl();
         if (!servletUrl.endsWith("app.do"))
             throw new RuntimeException("Invalid servlet url: " + servletUrl);
         return servletUrl.substring(0, servletUrl.length() - 6) + "logs/" + log4jFileName;
     }
 
     /**
      * This function parses the input command line string to get rid of special characters such as
      * quotation, end of line, tab, etc. It also extracts characters inside a pair of quotation to form
      * a single argument
      *
      * @param input
      * @return
      */
     public static Commandline parseCmdLine(String input) {
         Commandline cmdLine = new Commandline();
         char inputChars[] = input.toCharArray();
         boolean quoted = false;
         String field = new String("");
         for (int i = 0; i < inputChars.length; i++) {
             char inputChar = inputChars[i];
             switch (inputChar) {
                 case '"':
                     if (!quoted) {
                         quoted = true;
                     } else {
                         quoted = false;
                     }
                     break;
                 case '\n':
                 case '\r':
                 case ' ':
                 case '\t':
                     if (quoted) {
                         field += ' ';
                     } else {
                         field = field.trim();
                         if (!field.equals("")) {
                             if (cmdLine.getExecutable() == null)
                                 cmdLine.setExecutable(field);
                             else
                                 cmdLine.createArgument().setValue(field);
                             field = new String("");
                         }
                     }
                     break;
                 default:
                     field += inputChar;
                     break;
             }
         }
         field = field.trim();
         if (!field.equals("")) {
             if (cmdLine.getExecutable() == null)
                 cmdLine.setExecutable(field);
             else
                 cmdLine.createArgument().setValue(field);
         }
         return cmdLine;
     }
 
     /**
      * Convert specified logLevel defined in {@link com.luntsys.luntbuild.facades.Constants} to proper ant log level
      *
      * @param logLevel
      * @return
      */
     public static int convertToAntLogLevel(int logLevel) {
         if (logLevel == com.luntsys.luntbuild.facades.Constants.LOG_LEVEL_BRIEF)
             return Project.MSG_WARN;
         else if (logLevel == com.luntsys.luntbuild.facades.Constants.LOG_LEVEL_NORMAL)
             return Project.MSG_INFO;
         else
             return Project.MSG_VERBOSE;
     }
 
     /**
      * Validates if the parameter can be a valid path element
      *
      * @param pathElement
      * @throws ValidationException
      */
     public static void validatePathElement(String pathElement) {
         if (isEmpty(pathElement))
             throw new ValidationException("Should not be empty!");
         if (pathElement.matches(".*[/\\\\:*?\"<>|].*"))
             throw new ValidationException("Should not contain characters: /\\:*?\"<>|");
     }
 
     /**
      * This method increases a version string. The version string increase strategy is simple:
      * increase last found digits in the supplied version string, for eg:
      * <i> "v1.0" will be increased to "v1.1"
      * <i> "v1.9" will be increased to "v1.10"
      * <i> "v1.5(1000) will be increased to "v1.5(1001)"
      *
      * @param currentVersion
      * @return
      */
     public static String increaseBuildVersion(String currentVersion) {
         currentVersion = StringUtils.reverse(currentVersion);
         Pattern pattern = Pattern.compile("\\d+");
         Matcher matcher = pattern.matcher(currentVersion);
         if (!matcher.find()) {
             currentVersion = StringUtils.reverse(currentVersion);
             return currentVersion;
         } else {
             String digits = matcher.group();
             digits = StringUtils.reverse(digits);
             String newDigits = String.valueOf(new Long(digits).longValue() + 1);
             newDigits = StringUtils.reverse(newDigits);
             String nextVersion = matcher.replaceFirst(newDigits);
             currentVersion = StringUtils.reverse(currentVersion);
             nextVersion = StringUtils.reverse(nextVersion);
             return nextVersion;
         }
     }
 
     public static boolean isVariablesContained(String expression) {
         Pattern pattern = Pattern.compile("\\$\\{[^$]*\\}");
 
         Matcher matcher = pattern.matcher(expression);
         if (matcher.find())
             return true;
         else
             return false;
     }
 
     /**
      * Evaluate value of a expression. During the evaluation, substring contained in ${...} will be treated
      * as an ognl expression and will be evaluated against the passed ognlRoot parameter. For example,
      * string "testcvs-${year}" can evaluate to be "testcvs-2004".
      *
      * @param ognlRoot
      * @param expression
      * @return
      * @throws OgnlException
      */
     public static String evaluateExpression(Object ognlRoot, String expression) throws OgnlException {
         Pattern pattern = Pattern.compile("\\$\\{([^$]*)\\}");
 
         String value = expression;
         Matcher matcher;
         while ((matcher = pattern.matcher(value)).find()) {
             String ognlValue = (String) Ognl.getValue(Ognl.parseExpression(matcher.group(1)),
                     Ognl.createDefaultContext(ognlRoot), ognlRoot, String.class);
             if (ognlValue == null)
                 ognlValue = "";
             value = matcher.replaceFirst(ognlValue);
         }
         return value;
     }
 
     /**
      * Validates supplied expression
      *
      * @param expression
      * @return
      * @throws ValidationException
      */
     public static String validateExpression(String expression) {
         Pattern pattern = Pattern.compile("\\$\\{([^$]*)\\}");
 
         String value = expression;
         Matcher matcher;
         while ((matcher = pattern.matcher(value)).find()) {
             try {
                 Ognl.parseExpression(matcher.group(1));
             } catch (OgnlException e) {
                 throw new ValidationException("Invalid ognl expression: " + matcher.group(1));
             }
             value = matcher.replaceFirst("");
         }
         return value;
     }
 
     /** Load and configure log4j properties to specify the HTML, TEXT log file
      * @throws IOException
      */
     private static final void setLuntbuildLogs() throws IOException {
         setLuntbuildHtmlLog(Luntbuild.installDir);
         setLuntbuildTextLog(Luntbuild.installDir);
     }
 
     /** Load and configure log4j properties to specify the HTML, TEXT log file
      * @param installDir
      * @throws IOException
      */
     public static final void setLuntbuildLogs(String installDir, String config) throws IOException {
         PropertyConfigurator.configure(config);
         setLuntbuildHtmlLog(installDir);
         setLuntbuildTextLog(installDir);
     }
 
     /** Set Luntbuild html log
      * @throws IOException
      */
     private static final void setLuntbuildHtmlLog(String installDir) throws IOException {
         Appender app = LogManager.getRootLogger().getAppender("luntbuild_logfile");
         if (app != null) {
             ((FileAppender) app).setFile(new File(installDir + "/logs/" +
                     Luntbuild.log4jFileName).getAbsolutePath());
             ((FileAppender) app).activateOptions();
         } else {
             logger.warn("Can not find luntbuild_logfile appender, creating...");
             HTMLLayout layout = new HTMLLayout();
             layout.setTitle("Luntbuild System Log");
             layout.setLocationInfo(true);
             app = new FileAppender(layout, new File(installDir + "/logs/" +
                     Luntbuild.log4jFileName).getAbsolutePath(), true);
             ((FileAppender) app).setAppend(false);
             Logger log = LogManager.getLogger("com.luntsys.luntbuild");
             log.setLevel(Level.INFO);
             log.addAppender(app);
             ((FileAppender) app).activateOptions();
         }
     }
 
     /** Set Luntbuild text log
      * @throws IOException
      */
     private static final void setLuntbuildTextLog(String installDir) throws IOException {
         Appender app = LogManager.getRootLogger().getAppender("luntbuild_txt_logfile");
         if (app != null) {
             ((FileAppender) app).setFile(new File(installDir + "/logs/" +
                     Luntbuild.log4jFileNameTxt).getAbsolutePath());
             ((FileAppender) app).activateOptions();
         } else {
             logger.warn("Can not find luntbuild_logfile appender, creating...");
             Layout layout = new PatternLayout("%5p [%t] (%F:%L) - %m%n");
             app = new FileAppender(layout, new File(installDir + "/logs/" +
                     Luntbuild.log4jFileNameTxt).getAbsolutePath(), true);
             Logger log = LogManager.getLogger("com.luntsys.luntbuild");
             log.setLevel(Level.INFO);
             log.addAppender(app);
             ((FileAppender) app).activateOptions();
         }
     }
 
     /*
      * luntbuild lifecycle management moved here from servlet
      */
     public static void initApplication(ServletContext context) {
         try {
             try {
                 installDir = context.getInitParameter("installDir");
 
                 if (isEmpty(installDir))
                     throw new RuntimeException("Missing parameter \"installDir\" for Luntbuild servlet");
 
                 // load build informations
                 buildInfos = new Properties();
                 buildInfos.load(new FileInputStream(installDir + "/buildInfo.properties"));
 
                 // load and configure log4j properties to specify the HTML, TEXT log file
                 setLuntbuildLogs();
 
                 // initialize spring application context
                 XmlWebApplicationContext xwac = new XmlWebApplicationContext();
                 xwac.setServletContext(context);
                 xwac.setParent(null);
                 xwac.setConfigLocations(new String[]{SPRING_CONFIG_LOCATION});
 
                 Properties props = new Properties();
                 InputStream jdbcIs = context.getResourceAsStream("/WEB-INF/jdbc.properties");
                 if (jdbcIs != null) {
                     props.load(jdbcIs);
                 }
                 // Set dataset DB for HSQLDB in process DB
                 String hsqlUrl = props.getProperty("hsqlUrl");
                 if (hsqlUrl == null || hsqlUrl.trim().length() == 0 ||
                         hsqlUrl.trim().startsWith("${")) {
                     String dataset = new File(installDir + "/db/luntbuild").getAbsolutePath();
                     props.setProperty("hsqlUrl", "jdbc:hsqldb:" + dataset);
                 }
                 // Set dataset DB for Derby in process DB
                 String derbyUrl = props.getProperty("derbyUrl");
                 if (derbyUrl == null || derbyUrl.trim().length() == 0 ||
                         derbyUrl.trim().startsWith("${")) {
                     String dataset = new File(installDir + "/db/luntbuild-derby-data").getAbsolutePath();
                     props.setProperty("derbyUrl", "jdbc:derby:" + dataset);
                 }
                 // Set dataset DB for H2 in process DB
                 String h2Url = props.getProperty("h2Url");
                 if (h2Url == null || h2Url.trim().length() == 0 ||
                         h2Url.trim().startsWith("${")) {
                     String dataset = new File(installDir + "/db/luntbuild-h2-data").getAbsolutePath();
                     props.setProperty("h2Url", "jdbc:h2:file:" + dataset);
                 }
 
                 PropertyPlaceholderConfigurer cfg = new PropertyPlaceholderConfigurer();
                 cfg.setProperties(props);
                 xwac.addBeanFactoryPostProcessor(cfg);
                 xwac.refresh();
 
                 context.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, xwac);
                 appContext = xwac;
                 setProperties(getDao().loadProperties());
 
                 loadVcsAdaptors(context);
                 loadNotifiers(context);
                 loadListeners(context);
                 loadBuilders(context);
 
                 SecurityHelper.runAsSiteAdmin();
 
                 // check pre-defined users
                 if (!getDao().isUserExist(User.CHECKIN_USER_NAME))
                     getDao().initialize();
 
                 // mark unfinished builds as failed
                 getDao().processUnfinishedBuilds();
                 // mark unfinished schedule executions as failed
                 getDao().processUnfinishedSchedules();
 
                 // cleanup temp directory
                 cleanupDir(installDir + "/tmp");
 
                 // setup upload parameters
                 DefaultMultipartDecoder.getSharedInstance().setRepositoryPath(installDir + "/tmp");
                 DefaultMultipartDecoder.getSharedInstance().setMaxSize(-1); // set no limit on max upload size
                 DefaultMultipartDecoder.getSharedInstance().setThresholdSize(FILE_BLOCK_SIZE);
 
                 getSchedService().startup();
                 getSchedService().scheduleSystemCare();
                 getSchedService().rescheduleBuilds();
 
                 logger.info("Leaving application initialization");
 
             } catch (Throwable throwable) {
                 logger.error("Exception catched in LuntbuildServlet.init()", throwable);
             }
         } catch (Throwable throwable) {
             logger.error("Exception catched in application initialization", throwable);
         }
     }
 
     /* Need to build path and make sure path separator is at the end of the real context
      * path before appending WEB-INF/classes (Jetty does not).
      */
     private static String getPath(ServletContext context) {
         String path = context.getRealPath("/");
         if (path == null) {
             throw new RuntimeException("ServletContext.getRealPath() failed: please deploy Luntbuild web application as exploded directory instead of only in war file!");
         }
         if (!(path.endsWith("/") || path.endsWith("\\") || path.endsWith(File.pathSeparator))) {
             path += "/";
         }
         path += "WEB-INF/classes";
         return path;
     }
 
     private static void loadBuilders(ServletContext context) {
 /*
         ClassPackageExplorer pkgExplorer = new SimpleClassPackageExplorer(getPath(context));
         String[] classNames = pkgExplorer.listPackage(BUILDER_PACKAGE_NAME);
         Luntbuild.builders = new ArrayList();
         for (int i = 0; i < classNames.length; i++) {
             String aClassName = classNames[i];
             try {
                 Class aClass = Class.forName(aClassName);
                 if (Builder.class.isAssignableFrom(aClass) && !Modifier.isAbstract(aClass.getModifiers())) {
                     Luntbuild.builders.add(aClass);
                     logger.info("Builder \"" + aClassName + "\" found");
                 }
             } catch (Exception e) {
                 logger.fatal("Failed to load class: " + aClassName, e);
                 throw new ApplicationRuntimeException(e);
             }
         }
         if (Luntbuild.vcsAdaptors.size() == 0) {
             logger.fatal("No builders found in package: \"" + BUILDER_PACKAGE_NAME + "\"");
             throw new ApplicationRuntimeException("No builders found!");
         }
 */
         builders = new ArrayList();
         builders.add(AntBuilder.class);
         builders.add(MavenBuilder.class);
         builders.add(Maven2Builder.class);
         builders.add(CommandBuilder.class);
         builders.add(RakeBuilder.class);
     }
 
     private static void loadVcsAdaptors(ServletContext context) {
 /*
         ClassPackageExplorer pkgExplorer = new SimpleClassPackageExplorer(getPath(context));
         String[] classNames = pkgExplorer.listPackage(VCS_PACKAGE_NAME);
         Luntbuild.vcsAdaptors = new ArrayList();
         for (int i = 0; i < classNames.length; i++) {
             String aClassName = classNames[i];
             try {
                 Class aClass = Class.forName(aClassName);
                 if (Vcs.class.isAssignableFrom(aClass) && !Modifier.isAbstract(aClass.getModifiers())) {
                     Luntbuild.vcsAdaptors.add(aClass);
                     logger.info("Vcs adaptor \"" + aClassName + "\" found");
                 }
             } catch (Exception e) {
                 logger.fatal("Failed to load class: " + aClassName, e);
                 throw new ApplicationRuntimeException(e);
             }
         }
         if (Luntbuild.vcsAdaptors.size() == 0) {
             logger.fatal("No Version Control System adaptor found in package: \"" + VCS_PACKAGE_NAME + "\"");
             throw new ApplicationRuntimeException("No Version Control System adaptor found!");
         }
 */
         vcsAdaptors = new ArrayList();
         vcsAdaptors.add(AccurevAdaptor.class);
         vcsAdaptors.add(BaseClearcaseAdaptor.class);
         vcsAdaptors.add(DynamicClearcaseAdaptor.class);
         vcsAdaptors.add(CvsAdaptor.class);
         vcsAdaptors.add(FileSystemAdaptor.class);
         vcsAdaptors.add(MksAdaptor.class);
         vcsAdaptors.add(PerforceAdaptor.class);
         vcsAdaptors.add(StarteamAdaptor.class);
         vcsAdaptors.add(SvnAdaptor.class);
         vcsAdaptors.add(SvnExeAdaptor.class);
         vcsAdaptors.add(UCMClearcaseAdaptor.class);
         vcsAdaptors.add(VssAdaptor.class);
     }
 
     private static void loadNotifiers(ServletContext context) {
 /*
         ClassPackageExplorer pkgExplorer = new SimpleClassPackageExplorer(getPath(context));
         String[] classNames = pkgExplorer.listPackage(NOTIFIER_PACKAGE_NAME);
         Luntbuild.notifiers = new ArrayList();
         for (int i = 0; i < classNames.length; i++) {
             String aClassName = classNames[i];
             try {
                 Class aClass = Class.forName(aClassName);
                 if (Notifier.class.isAssignableFrom(aClass) && !Modifier.isAbstract(aClass.getModifiers())) {
                     Luntbuild.notifiers.add(aClass);
                     logger.info("Notifier \"" + aClassName + "\" found");
                 }
             } catch (Exception e) {
                 logger.fatal("Failed to load class: " + aClassName, e);
                 throw new ApplicationRuntimeException(e);
             }
         }
 */
         notifiers = new ArrayList();
         notifiers.add(EmailNotifier.class);
         notifiers.add(MsnNotifier.class);
         notifiers.add(JabberNotifier.class);
         notifiers.add(SametimeNotifier.class);
         notifiers.add(BlogNotifier.class);
     }
 
     private static void loadListeners(ServletContext context) {
 /*
         ClassPackageExplorer pkgExplorer = new SimpleClassPackageExplorer(getPath(context));
         String[] classNames = pkgExplorer.listPackage(LISTENER_PACKAGE_NAME);
         Luntbuild.listeners = new ArrayList();
         for (int i = 0; i < classNames.length; i++) {
             String aClassName = classNames[i];
             try {
                 Class aClass = Class.forName(aClassName);
                 if (Listener.class.isAssignableFrom(aClass) && !Modifier.isAbstract(aClass.getModifiers())) {
                     Luntbuild.listeners.add(aClass);
                     logger.info("Listener \"" + aClassName + "\" found");
                 }
             } catch (Exception e) {
                 logger.fatal("Failed to load class: " + aClassName, e);
                 throw new ApplicationRuntimeException(e);
             }
         }
 */
         listeners = new ArrayList();
         listeners.add(ListenerSample.class);
     }
 
     /**
      * Do some cleanup works, such as cleanup the schedule thread, etc.
      */
     public static void destroyApplication(ServletContext context) {
         logger.info("Enter application shutdown");
         SecurityHelper.runAsSiteAdmin();
         getSchedService().shutdown();
         appContext.close();
 
 		InputStream jdbcIs = null;
 		try {
 			Properties props = new Properties();
 			jdbcIs = context.getResourceAsStream("/WEB-INF/jdbc.properties");
 			if (jdbcIs != null)
 				props.load(jdbcIs);
 			else
 				throw new RuntimeException("Failed to load /WEB-INF/jdbc.properties");
 			if (props.getProperty(JDBC_DRIVER_PROPERTY).equals(HSQLDB_DRIVER)) {
 				logger.info("Shutdown database.");
 				try {
 					Class.forName(HSQLDB_DRIVER).newInstance();
 					Connection connection = DriverManager.getConnection("jdbc:hsqldb:" +
 							new File(installDir + "/db/luntbuild").getAbsolutePath(),
 							props.getProperty("jdbc.username"), props.getProperty("jdbc.password"));
 					Statement stmt = connection.createStatement();
 					stmt.executeUpdate("SHUTDOWN");
 					connection.close();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			if (jdbcIs != null)
 				try {
 					jdbcIs.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 		}
 
         logger.info("application shutdown complete");
     }
 
     public static List removeCheckinUser(List users) {
         Iterator it = users.iterator();
         while (it.hasNext()) {
             User user = (User) it.next();
             if (user.getName().equals(User.CHECKIN_USER_NAME)) {
                 it.remove();
                 break;
             }
         }
         return users;
     }
 
     public static Project createAntProject() {
         Project antProject = new Project();
         antProject.init();
         return antProject;
     }
 
     public static String getAssignmentName(String assignment) {
         int index = assignment.indexOf('=');
         if (index == -1) {
             return assignment.trim();
         } else {
             return assignment.substring(0, index).trim();
         }
     }
 
     public static String getAssignmentValue(String assignment) {
         int index = assignment.indexOf('=');
         if (index == -1) {
             return "";
         } else {
             return assignment.substring(index + 1).trim();
         }
     }
 
     public static String encryptPassword(String passwd) {
         if (passwd == null || passwd.trim().length() == 0) return passwd;
 
         try {
             StringEncrypter encrypter = new StringEncrypter(StringEncrypter.DESEDE_ENCRYPTION_SCHEME);
             return encrypter.encrypt(passwd);
         } catch (StringEncrypter.EncryptionException e) {
             throw new RuntimeException(e);
         }
     }
 
     public static String decryptPassword(String passwd) {
         if (passwd == null || passwd.trim().length() == 0) return passwd;
 
         try {
             StringEncrypter encrypter = new StringEncrypter(StringEncrypter.DESEDE_ENCRYPTION_SCHEME);
             return encrypter.decrypt(passwd);
         } catch (StringEncrypter.EncryptionException e) {
             throw new RuntimeException(e);
         }
     }
 
     public static int getPageRefreshInterval() {
         String pageRefreshIntervalText = (String) properties.get(Constants.PAGE_REFRESH_INTERVAL);
         try {
             int pageRefreshInterval = new Integer(pageRefreshIntervalText).intValue();
             if (pageRefreshInterval <= 0)
                 return DEFAULT_PAGE_REFRESH_INTERVAL;
             else
                 return pageRefreshInterval;
         } catch (NumberFormatException e) {
             return DEFAULT_PAGE_REFRESH_INTERVAL;
         }
     }
 
     public static Map getProperties() {
         return properties;
     }
 
     public static void setProperties(Map properties) {
         Luntbuild.properties = Collections.synchronizedMap(properties);
     }
 
     public static void sendAsset(HttpServletRequest request, HttpServletResponse response,
                                  String assetLocation) {
         InputStream in = null;
         try {
             URL url = Thread.currentThread().getContextClassLoader().getResource(assetLocation);
             URLConnection connection = url.openConnection();
             int period = assetLocation.lastIndexOf(".");
             if (period < 0)
                 throw new ApplicationRuntimeException("Invalid asset location: " + assetLocation + "(No file extension found)");
             String extension = assetLocation.substring(period + 1);
             if (extension.length() < 1)
                 throw new ApplicationRuntimeException("Invalid asset location: " + assetLocation + "(No file extension found)");
             String contentType = SimpleMimeType.getMimeType(extension);
             if (Luntbuild.isEmpty(contentType))
                 response.setContentType("application/octet-stream");
             else
                 response.setContentType(contentType);
             int contentLength = connection.getContentLength();
             if (contentLength > 0)
                 response.setContentLength(contentLength);
             byte[] data = new byte[FILE_BLOCK_SIZE];
             ServletOutputStream out = response.getOutputStream();
             in = connection.getInputStream();
             while (true) {
                 int bytesRead = in.read(data);
                 if (bytesRead < 0)
                     break;
                 out.write(data, 0, bytesRead);
             }
         } catch (IOException e) {
             logger.error("IOException during sendStream: ", e);
             throw new ApplicationRuntimeException(e);
         } finally {
             if (in != null)
                 try {
                     in.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
         }
     }
 
     /**
      * Encodes a character for xml
      */
     private static String xmlEncode(char ch){
         switch (ch){
             case '\"': return "&quot;";
             case '&': return "&amp;";
             case '<': return "&lt;";
             case '>': return "&gt;";
             case '\'': return "&apos;";
             default: return null;
         }
     }
 
     private static boolean isXmlEncodeChar(char ch){
         switch (ch){
             case '\"': return true;
             case '&': return true;
             case '<': return true;
             case '>': return true;
             case '\'': return true;
             default: return false;
         }
     }
 
     /**
      * Substitutes all XML special characters to enable XML parsing
      *
      * @param str String input text
      * @return String with the substituted characters.
      */
     public static String xmlEncodeEntities(String str) {
         StringBuffer buf = new StringBuffer();
         int len =str.length();
         for (int i = 0; i < len; i++){
             char c = str.charAt(i);
             if (isXmlEncodeChar(c)) {
                 if (c == '&') {
                     String subs;
                     if (i + 3 < len && str.charAt(i + 1) == '#') {
                         int idx = str.indexOf(';', i + 3);
                         if (idx > -1 && idx - i < 10) {
                             buf.append(str.substring(i, idx + 1));
                             i = idx;
                         }
                         continue;
                     } else if (i + 5 < len && (subs = str.substring(i, i + 5)).equals("&amp;")) {
                         buf.append(subs);
                         i += 4;
                         continue;
                     } else if (i + 6 < len && (subs = str.substring(i, i + 6)).equals("&quot;")) {
                         buf.append(subs);
                         i += 5;
                         continue;
                     } else if (i + 6 < len && (subs = str.substring(i, i + 6)).equals("&apos;")) {
                         buf.append(subs);
                         i += 5;
                         continue;
                     } else if (i + 4 < len && (subs = str.substring(i, i + 4)).equals("&lt;")) {
                         buf.append(subs);
                         i += 3;
                         continue;
                     } else if (i + 4 < len && (subs = str.substring(i, i + 4)).equals("&gt;")) {
                         buf.append(subs);
                         i += 3;
                         continue;
                     } else {
                         buf.append("&amp;");
                     }
                 } else {
                     String encode = xmlEncode(c);
                     if (encode != null)
                         buf.append(xmlEncode(c));
                     else {
                         buf.append(c);
                     }
                 }
             } else
                 buf.append(c);
         }
 
         return buf.toString();
     }
 
 
 }
