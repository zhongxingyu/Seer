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
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.InetAddress;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.UnknownHostException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.Statement;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import ognl.Ognl;
 import ognl.OgnlException;
 
 import org.acegisecurity.AuthenticationManager;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.log4j.Appender;
 import org.apache.log4j.FileAppender;
 import org.apache.log4j.HTMLLayout;
 import org.apache.log4j.Layout;
 import org.apache.log4j.Level;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PatternLayout;
 import org.apache.log4j.PropertyConfigurator;
 import org.apache.tapestry.ApplicationRuntimeException;
 import org.apache.tapestry.IRequestCycle;
 import org.apache.tapestry.multipart.DefaultMultipartDecoder;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.taskdefs.Delete;
 import org.apache.tools.ant.taskdefs.Mkdir;
 import org.apache.tools.ant.taskdefs.Move;
 import org.apache.tools.ant.taskdefs.Touch;
 import org.apache.tools.ant.types.FileSet;
 import org.apache.xerces.parsers.DOMParser;
 import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
 import org.springframework.web.context.WebApplicationContext;
 import org.springframework.web.context.support.XmlWebApplicationContext;
 import org.xml.sax.SAXException;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.luntsys.luntbuild.ant.Commandline;
 import com.luntsys.luntbuild.builders.AntBuilder;
 import com.luntsys.luntbuild.builders.CommandBuilder;
 import com.luntsys.luntbuild.builders.Maven2Builder;
 import com.luntsys.luntbuild.builders.MavenBuilder;
 import com.luntsys.luntbuild.builders.RakeBuilder;
 import com.luntsys.luntbuild.dao.Dao;
 import com.luntsys.luntbuild.db.User;
 import com.luntsys.luntbuild.facades.Constants;
 import com.luntsys.luntbuild.facades.ILuntbuild;
 import com.luntsys.luntbuild.listeners.Listener;
 import com.luntsys.luntbuild.notifiers.BlogNotifier;
 import com.luntsys.luntbuild.notifiers.EmailNotifier;
 import com.luntsys.luntbuild.notifiers.JabberNotifier;
 import com.luntsys.luntbuild.notifiers.MsnNotifier;
 import com.luntsys.luntbuild.notifiers.SametimeNotifier;
 import com.luntsys.luntbuild.reports.Report;
 import com.luntsys.luntbuild.security.SecurityHelper;
 import com.luntsys.luntbuild.services.IScheduler;
 import com.luntsys.luntbuild.vcs.AccurevAdaptor;
 import com.luntsys.luntbuild.vcs.BaseClearcaseAdaptor;
 import com.luntsys.luntbuild.vcs.CvsAdaptor;
 import com.luntsys.luntbuild.vcs.DynamicClearcaseAdaptor;
 import com.luntsys.luntbuild.vcs.FileSystemAdaptor;
 import com.luntsys.luntbuild.vcs.MksAdaptor;
 import com.luntsys.luntbuild.vcs.PerforceAdaptor;
 import com.luntsys.luntbuild.vcs.StarteamAdaptor;
 import com.luntsys.luntbuild.vcs.SvnAdaptor;
 import com.luntsys.luntbuild.vcs.SvnExeAdaptor;
 import com.luntsys.luntbuild.vcs.UCMClearcaseAdaptor;
 import com.luntsys.luntbuild.vcs.Vcs;
 import com.luntsys.luntbuild.vcs.VssAdaptor;
 
 /**
  * Luntbuild utility class to provide easy access for some commonly used objects and functions.
  *
  * @author robin shine
  */
 public class Luntbuild {
 	private static final String JDBC_DRIVER_PROPERTY = "jdbc.driverClassName";
 	private static final String HSQLDB_DRIVER = "org.hsqldb.jdbcDriver";
 
     /** Location of Spring config file */
     public static final String SPRING_CONFIG_LOCATION = "/WEB-INF/applicationContext.xml";
 
     /** Trigger name separator */
     public static final String TRIGGER_NAME_SEPERATOR = "$";
     private static Log logger = LogFactory.getLog(Luntbuild.class);
 
     // Luntbuild system level properties
     private static Map properties;
 
     /** Default page referesh interval, in seconds */
     public static final int DEFAULT_PAGE_REFRESH_INTERVAL = 15;
     /** Default mail port */
     public static final int DEFAULT_MAIL_PORT = 25;
     /** Block size when operating files */
     public static final int FILE_BLOCK_SIZE = 25000;
 
     /** Date format for the web interface */
     public static final SynchronizedFormatter DATE_DISPLAY_FORMAT =
             new SynchronizedFormatter("yyyy-MM-dd HH:mm");
 
     /** ISO date format, used by web feeds */
     public static final SynchronizedFormatter DATE_DISPLAY_FORMAT_ISO =
             new SynchronizedFormatter("yyyy-MM-dd'T'HH:mm:ssZ"); 
 
     /** A synchronized wrapper around SimpleDateFormat */
     public static class SynchronizedFormatter {
         SimpleDateFormat delegate;
         
         public SynchronizedFormatter(String format) {
             delegate = new SimpleDateFormat(format);
         }
         
         public String format(Date date) {
             return delegate.format(date);
         }
     }
 
     /** Log4j system log - HTML */
     public static final String log4jFileName = "luntbuild_log.html";
 
     /** Log4j system log - Text */
     public static final String log4jFileNameTxt = "luntbuild_log.txt";
 
     /** Extensions configuration file */
     public static final String extensionsConfigFile = "luntbuild_config.xml";
 
     /** Application wide context for use in Spring framework */
     public static XmlWebApplicationContext appContext;
 
     /** Installation directory of Luntbuild */
     public static String installDir;
 
     /** Build information for this Luntbuild distribution */
     public static Properties buildInfos;
 
     /** List of VCS adaptor classes found in the system */
     public static List vcsAdaptors;
 
     /** List of notifier classes found in the system */
     public static List notifiers;
 
     /** List of builder classes found in the system */
     public static List builders;
 
     /** List of listener classes found in the system */
     public static Hashtable listeners;
 
     /** List of report classes found in the system */
     public static Hashtable reports;
 
     /** List of extension classes found in the system */
     public static Hashtable extensions;
 
     /** Page referesh interval, in seconds */
     public static int pageRefreshInterval;
 
     /** Server name */
     public static String serverName = "localhost";
     /** Server port */
     public static int serverPort = 8080;
     
     /**
      * Gets the data access object.
      *
      * @return the data access object
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
      * Gets the scheduling service.
      *
      * @return the scheduling service
      * @throws RuntimeException if the scheduler could not be found
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
 
     /**
      * Gets the authentication manager.
      * 
      * @return the authentication manager
      * @throws RuntimeException if the authentication manager could not be found
      */
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
      * Gets the Luntbuild service.
      *
      * @return the Luntbuild service
      * @throws RuntimeException if the Luntbuild service could not be found
      */
     public static ILuntbuild getLuntbuildService() {
         String message;
         if (appContext == null) {
             message = "Application context not initialized!";
             logger.error(message);
             throw new RuntimeException(message);
         }
         ILuntbuild luntbuildService = (ILuntbuild) appContext.getBean("luntbuildService");
         if (luntbuildService == null) {
             message = "Failed to find bean \"luntbuildService\" in application context!";
             logger.error(message);
             throw new RuntimeException(message);
         }
         return luntbuildService;
     }
 
     /**
      * Gets all messages from any throwable object.
      * 
      * @param throwable the throwable object
      * @return the messages
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
      * Checks if the specified local directory exists.
      *
      * @param directory the directory to check
      * @return <code>true</code> if the directory exists
      */
     public static boolean existsDir(String directory) {
     	try {
     		return new File(directory).exists();
     	} catch (Exception e) {
     		return false;
     	}
     }
 
     /**
      * Deletes a local directory and all the contents inside of it.
      *
      * @param directory the directory to delete
      */
     public static void deleteDir(String directory) {
         deleteDirWithExclude(directory, null, false);
     }
 
     /**
      * Deletes a local directory and all the contents inside of it.
      * 
      * @param directory
      *            the directory to delete
      * @param excludes
      *            a glob pattern of files that will not be deleted
      * @param leaveTopLevelFolder
      *            if true then only the contents of the folder are deleted, if
      *            false then the top level folder will be deleted too (provided
      *            no files are excluded)
      */
     public static void deleteDirWithExclude(String directory, String excludes,
             boolean leaveTopLevelFolder) {
         try {
             Delete deleteTask = new Delete();
             deleteTask.setProject(new org.apache.tools.ant.Project());
             deleteTask.getProject().init();
             deleteTask.setDir(new File(directory));
             deleteTask.setFailOnError(false);
             
             // pass the excludes pattern, if we have one
             if (excludes != null && excludes.length() > 0) {
                 deleteTask.setExcludes(excludes);
             }
             if (leaveTopLevelFolder) {
                 deleteTask.setIncludes("**/*");
             }
             deleteTask.execute();
         } catch (Exception e) {
             logger.error("IOException during deleteDir: ", e);
         }
     }
 
     /**
      * Renames a local directory.
      * 
      * @param from the current directory name
      * @param to the new directory name
      * @throws RuntimeException from {@link File#getCanonicalFile()}
      */
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
      * Deletes all contents inside a local directory.
      *
      * @param directory the directory to clean
      * @throws org.apache.tools.ant.BuildException when cleanup fails
      */
     public static void cleanupDir(String directory) {
         deleteDir(directory);
         createDir(directory);
     }
 
     /**
      * Deletes a local file.
      *
      * @param file the file to delete
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
      * Creates a new local directory.
      *
      * @param directory the directory to create
      * @throws org.apache.tools.ant.BuildException
      */
     public static void createDir(String directory) {
         Mkdir mkdirTask = new Mkdir();
         mkdirTask.setProject(new org.apache.tools.ant.Project());
         mkdirTask.getProject().init();
         mkdirTask.setDir(new File(directory));
         mkdirTask.execute();
     }
 
     /**
      * Touches a file to update its modification time, if the file does not exist it will be created.
      * 
      * @param file the file to touch
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
 
     /**
      * Sends the contents of the specified file to the specified request cycle.
      * 
      * @param cycle the request cycle
      * @param filePath the file to send
      */
     public static void sendFile(IRequestCycle cycle, String filePath) {
         sendFile(cycle.getRequestContext().getRequest(), cycle.getRequestContext().getResponse(),
                 filePath);
     }
 
     /**
      * Sends the contents of the specified file to the specified HTTP request.
      * 
      * @param request the HTTP request
      * @param response the HTTP response
      * @param filePath the file to send
      * @throws ApplicationRuntimeException if the file could not be read
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
      * Sends the contents of the specified URL to the specified HTTP request.
      * 
      * @param request the HTTP request
      * @param response the HTTP response 
      * @param url the URL to send
      * @throws ApplicationRuntimeException if the URL could not be read
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
 
     /**
      * Gets the EOL setting to use for the specified HTTP request.
      *  
      * @param request the HTTP request
      * @return the EOL to use
      */
     public static String getEol(HttpServletRequest request) {
         String userAgent = request.getHeader("user-agent");
         if (userAgent == null)
             return "\n";
         if (userAgent.matches(".*Windows.*"))
             return "\r\n";
         return "\n";
     }
 
     /**
      * Calculates the actual date by specifying a time in the format "hh:mm" of today.
      *
      * @param hhmm the hours and minutes in "hh:mm" format
      * @return the actual date
      * @throws RuntimeException if <code>hhmm</code> is formatted wrong
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
      * Converts a build version to a label that is safe for various version control systems.
      *
      * @param version the build version
      * @return the safe label
      */
     public static String getLabelByVersion(String version) {
         String label = version.trim().replace('.', '_').replaceAll("[\\s]", "-");
         if (!label.matches("^[a-zA-Z].*"))
             label = "v" + label;
         return label;
     }
 
     /**
      * Concatenates two path into one path. The main functionality is to detect the
      * trailing "/" of <code>path1</code>, and leading "/" of <code>path2</code>, and
      * forms only one "/" in the joined path.
      *
      * @param path1 the beginning path
      * @param path2 the ending path
      * @return the combined path
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
                 path = trimmedPath1 + "/" + trimmedPath2;
         }
         return path;
     }
 
     /**
      * Removes the leading '/' or '\' character from a path.
      *
      * @param path the path
      * @return the trimmed path
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
      * Removes the trailing '/' or '\' character from a path.
      *
      * @param path the path
      * @return the trimmed path
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
      * Gets the Luntbuild logger for specified ant project, or <code>null</code> if
      * it does not contain a Luntbuild logger.
      *
      * @param antProject the ant project
      * @return the Luntbuild logger, or <code>null</code> if no Luntbuild logger exists
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
      * Determines if the specified string is empty (<code>null</code> or blank).
      *
      * @param aString the string
      * @return <code>true</code> if the string is empty
      */
     public static boolean isEmpty(String aString) {
         if (aString == null || aString.trim().equals("") || aString.trim().equals("\"\""))
             return true;
         else
             return false;
     }
 
     /**
      * Creates a cloned copy of the specified module.
      * 
      * @param vcs the VCS the module is for
      * @param module the module
      * @return the clone of the module
      * @throws RuntimeException if modules are not supported for this VCS
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
 
     /**
      * Gets the URL to access the Luntbuild servlet.
      * 
      * @return the servlet URL
      */
     public static String getServletUrl() {
         String servletUrl = (String) properties.get("servletUrl");
         if (isEmpty(servletUrl))
             return "http://" + serverName + ":" + serverPort + "/luntbuild/app.do";
         else
             return servletUrl;
     }
 
     /**
      * Gets the root of the Luntbuild servlet URL.
      * 
      * @return the servlet root URL
      */
     public static String getServletRootUrl() {
         String servletUrl = (String) properties.get("servletUrl");
         if (isEmpty(servletUrl))
             return "http://" + serverName + ":" + serverPort + "/luntbuild";
         else
             return servletUrl.replaceAll("/app\\.do","");
     }
 
     /**
      * Gets the host name of this build server.
      * 
      * @return the host name
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
      * Gets the IP address of this build server.
      * 
      * @return the IP address
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
      * Determines if the specified shorter file recursively contains the specified longer file.
      * <code>shorter</code> will recursively contain <code>longer</code> when the any of the
      * following conditions are true:
      * <ul>
      * <li><code>shorter</code> is a directory and <code>longer</code> is a sub-directory</li>
      * <li><code>shorter</code> is a directory and <code>longer</code> is recursively under <code>shorter</code></li>
      * </ul>
      * 
      * @param shorter the shorter file
      * @param longer the longer file
      * @return the path of <code>longer</code> relative to <code>shorter</code>, or <code>null</code> if
      *         <code>longer</code> is not relative to <code>shorter</code>, or "" if
      *         <code>shorter</code> is a directory and is the same as <code>longer</code>
      * @throws RuntimeException from {@link File#getCanonicalFile()}
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
      * Filters the given list of notifier class names to get a list of existing notifier classes.
      * 
      * @param notifierClassNames the list of notifier class names, should not be <code>null</code>
      * @return the list of existing notifier classes
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
      * Converts a list of notifier classes to a list of notifier instances.
      * 
      * @param notifierClasses the list of notifier classes, should not be <code>null</code>
      * @return the list of notifier instances
      * @throws RuntimeException if unable to create an instance of a notifier
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
      * Gets the URL to access the Luntbuild system log.
      * 
      * @return the Luntbuild system log URL
      * @throws RuntimeException if the servlet URL is invalid
      */
     public static String getSystemLogUrl() {
         String servletUrl = getServletUrl();
         if (!servletUrl.endsWith("app.do"))
             throw new RuntimeException("Invalid servlet url: " + servletUrl);
         return servletUrl.substring(0, servletUrl.length() - 6) + "logs/" + log4jFileName;
     }
 
     /**
      * Creates a commandline object from a command line string.
      * 
      * <p>This function parses the input command line string to get rid of special characters such as
      * quotation, end of line, tab, etc. It also extracts characters inside a pair of quotation marks to form
      * a single argument.</p>
      *
      * @param input the input command line string
      * @return the commandline object
      */
     public static Commandline parseCmdLine(String input) {
         Commandline cmdLine = new Commandline();
         char inputChars[] = input.toCharArray();
         boolean quoted = false;
         String field = "";
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
                             field = "";
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
      * Converts the specified log level defined in {@link Constants} to the equivalent ant log level.
      *
      * @param logLevel the log level
      * @return the ant log level
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
      * Validates that the specified parameter can be a valid path element.
      *
      * @param pathElement the potential path element
      * @throws ValidationException if the parameter can not be a path element
      */
     public static void validatePathElement(String pathElement) {
         if (isEmpty(pathElement))
             throw new ValidationException("Should not be empty!");
         if (pathElement.matches(".*[/\\\\:*?\"<>|].*"))
             throw new ValidationException("Should not contain characters: /\\:*?\"<>|");
     }
 
     /**
      * Increases a build version as a string. The version string increase strategy is simple:
      * increase last found digits in the supplied version string, examples:
      * <ul>
      * <li>"v1.0" will be increased to "v1.1"</li>
      * <li>"v1.9" will be increased to "v1.10"</li>
      * <li>"v1.5(1000) will be increased to "v1.5(1001)"</li>
      * </ul>
      *
      * @param currentVersion the version to increase
      * @return the new increased version
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
 
     /**
      * Determines if an OGNL variable reference is contained in the specified OGNL expresion.
      * 
      * @param expression the OGNL expression
      * @return <code>true</code> if a variable reference is contained
      */
     public static boolean isVariablesContained(String expression) {
         Pattern pattern = Pattern.compile("\\$\\{[^$]*\\}");
 
         Matcher matcher = pattern.matcher(expression);
         if (matcher.find())
             return true;
         else
             return false;
     }
 
     /**
      * Evaluates the value of an OGNL expression. During the evaluation, substring contained in ${...} will be treated
      * as an OGNL expression and will be evaluated against the specified <code>ognlRoot</code> object. For example,
      * the string "testcvs-${year}" can evaluate to be "testcvs-2004".
      * 
      * @param ognlRoot the root object
      * @param expression the OGNL expression
      * @return the evaluated string
      * @throws OgnlException from {@link Ognl#parseExpression(java.lang.String)}
      *    or {@link Ognl#getValue(java.lang.Object, java.util.Map, java.lang.Object, java.lang.Class)}
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
             else if (ognlValue.equals(expression))
                 ognlValue = "";
             value = matcher.replaceFirst(ognlValue);
         }
         return value;
     }
 
     /**
      * Validates the specified OGNL expression.
      * 
      * @param expression the OGNL expression
      * @return the OGNL expression with variable references removed
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
 
     /**
      * Loads and configures log4j properties to specify the HTML and TEXT log files.
      * 
      * @throws IOException from {@link #setLuntbuildHtmlLog(String)} or {@link #setLuntbuildTextLog(String)}
      */
     private static final void setLuntbuildLogs() throws IOException {
         setLuntbuildHtmlLog(Luntbuild.installDir);
         setLuntbuildTextLog(Luntbuild.installDir);
     }
 
     /**
      * Loads and configures log4j properties to specify the HTML and TEXT log files, with an additional config.
      * 
      * @param installDir the Luntbuild installation directory
      * @param config the log4j config
      * @throws IOException from {@link #setLuntbuildHtmlLog(String)} or {@link #setLuntbuildTextLog(String)}
      */
     public static final void setLuntbuildLogs(String installDir, String config) throws IOException {
         PropertyConfigurator.configure(config);
         setLuntbuildHtmlLog(installDir);
         setLuntbuildTextLog(installDir);
     }
 
     /**
      * Sets the Luntbuild HTML log.
      * 
      * @param installDir the luntbuild installation directory
      * @throws IOException from {@link FileAppender#FileAppender(org.apache.log4j.Layout, java.lang.String, boolean)}
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
 
     /**
      * Sets the Luntbuild text log.
      * 
      * @param installDir the luntbuild installation directory
      * @throws IOException from {@link FileAppender#FileAppender(org.apache.log4j.Layout, java.lang.String, boolean)}
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
 
     /**
      * Initializes the Luntbuild system.
      * 
      * <p>Luntbuild lifecycle management moved here from servlet.</p>
      * 
      * @param context the servlet context
      */
     public static void initApplication(ServletContext context) {
         try {
             try {
                 installDir = context.getInitParameter("installDir");
 
                 if (isEmpty(installDir))
                     throw new RuntimeException("Missing parameter \"installDir\" for Luntbuild servlet");
 
                 // load build informations
                 buildInfos = new Properties();
                 FileInputStream is = null;
                 try {
                 	is = new FileInputStream(installDir + "/buildInfo.properties");
                 	buildInfos.load(is);
                 } catch (Exception e) {
 					logger.error("Failed to load buildInfo.properties");
 	            } finally {
 	            	if (is != null) try{is.close();} catch (Exception e) {}
 	            }
 
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
     	        	try {
     	        		props.load(jdbcIs);
     	        	} catch (Exception e) {
     					logger.error("Failed to load jdbc properties");
     	            } finally {
     	            	if (jdbcIs != null) try{jdbcIs.close();} catch (Exception e) {}
     				}
                 }
                 
     	        InputStream ldapIs = context.getResourceAsStream("/WEB-INF/ldap.properties");
     	        if (ldapIs != null) {
     	        	try {
     	        		props.load(ldapIs);
     	        	} catch (Exception e) {
     					logger.error("Failed to load ldap properties");
     	            } finally {
     	            	if (ldapIs != null) try{ldapIs.close();} catch (Exception e) {}
     				}
     	        } else {
     	        	logger.error("Failed to load ldap properties");
     	        }
 
                 setEmbeddedDbUrls(props);
 
                 PropertyPlaceholderConfigurer cfg = new PropertyPlaceholderConfigurer();
                 cfg.setProperties(props);
                 xwac.addBeanFactoryPostProcessor(cfg);
                 xwac.refresh();
 
                 context.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, xwac);
                 appContext = xwac;
                 setProperties(getDao().loadProperties());
 
                 loadVcsAdaptors(context);
                 loadNotifiers(context);
                 loadBuilders(context);
                 loadExtensions();
 
                 SecurityHelper.runAsSiteAdmin();
 
                 // check pre-defined users
                 if (!getDao().isUserExist(User.CHECKIN_USER_NAME_RECENT)
                         || !getDao().isUserExist(User.CHECKIN_USER_NAME_ALL))
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
                 getSchedService().scheduleSystemBackup();
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
 
     public static String resolveProperty(Properties props, String propName) {
     	if (!propName.startsWith("${")) return "";
     	propName = propName.substring(2);
     	if (propName.endsWith("}")) propName = propName.substring(0, propName.length() - 1);
     	return props.getProperty(propName, "");
     }
     
     public static void setEmbeddedDbUrls(Properties props) {
         // Set dataset DB for HSQLDB in process DB
         String hsqlUrl = props.getProperty("hsqlUrl");
         if (hsqlUrl == null || hsqlUrl.trim().length() == 0 ||
                 hsqlUrl.trim().startsWith("${")) {
             String dataset = new File(installDir + "/db/luntbuild").getAbsolutePath();
             props.setProperty("hsqlUrl", "jdbc:hsqldb:" + dataset);
             String hsqlUsername = props.getProperty("jdbcUsername");
             if (hsqlUsername == null || hsqlUsername.trim().length() == 0 || hsqlUsername.trim().startsWith("${"))
             	props.setProperty("jdbcUsername", "sa");                    
             String hsqlPassword = props.getProperty("jdbcPassword");
             if (hsqlPassword == null || hsqlPassword.trim().length() == 0 || hsqlPassword.trim().startsWith("${"))
             	props.setProperty("jdbcPassword", "");                    
         }
         
         // Set dataset DB for Derby in process DB
         String derbyUrl = props.getProperty("derbyUrl");
         if (derbyUrl == null || derbyUrl.trim().length() == 0 ||
                 derbyUrl.trim().startsWith("${")) {
             String dataset = new File(installDir + "/db/luntbuild-derby-data").getAbsolutePath();
             props.setProperty("derbyUrl", "jdbc:derby:" + dataset);
             String derbyUsername = props.getProperty("jdbcUsername");
             if (derbyUsername == null || derbyUsername.trim().length() == 0 || derbyUsername.trim().startsWith("${"))
             	props.setProperty("jdbcUsername", "sa");                    
             String derbyPassword = props.getProperty("jdbcPassword");
             if (derbyPassword == null || derbyPassword.trim().length() == 0 || derbyPassword.trim().startsWith("${"))
             	props.setProperty("jdbcPassword", "");                    
         }
         // Set dataset DB for H2 in process DB
         String h2Url = props.getProperty("h2Url");
         if (h2Url == null || h2Url.trim().length() == 0 ||
                 h2Url.trim().startsWith("${")) {
             String dataset = new File(installDir + "/db/luntbuild-h2-data").getAbsolutePath();
             props.setProperty("h2Url", "jdbc:h2:file:" + dataset);
             String h2Username = props.getProperty("jdbcUsername");
             if (h2Username == null || h2Username.trim().length() == 0 || h2Username.trim().startsWith("${"))
             	props.setProperty("jdbcUsername", "sa");                    
             String h2Password = props.getProperty("jdbcPassword");
             if (h2Password == null || h2Password.trim().length() == 0 || h2Password.trim().startsWith("${"))
             	props.setProperty("jdbcPassword", "");                    
         }
     }
     
     /**
      * Gets the path to the servlet classes.
      * 
      * <p>Need to build path and make sure path separator is at the end of the real context
      * path before appending WEB-INF/classes (Jetty does not).<p>
      * 
      * @param context the servlet context
      * @return the path to the servlet classes
      * @throws RuntimeException if {@link ServletContext#getRealPath(java.lang.String)} fails
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
         builders = new ArrayList();
         builders.add(AntBuilder.class);
         builders.add(MavenBuilder.class);
         builders.add(Maven2Builder.class);
         builders.add(CommandBuilder.class);
         builders.add(RakeBuilder.class);
     }
 
     private static void loadVcsAdaptors(ServletContext context) {
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
         notifiers = new ArrayList();
         notifiers.add(EmailNotifier.class);
         notifiers.add(MsnNotifier.class);
         notifiers.add(JabberNotifier.class);
         notifiers.add(SametimeNotifier.class);
         notifiers.add(BlogNotifier.class);
 
         List customNotifiers = (List)appContext.getBean("customNotifiers");
 
         if (customNotifiers != null) {
             notifiers.addAll(customNotifiers);
         }
 
     }
 
     /**
      * Loads extensions, listeners and reports.  Reads from the XML configuration file "luntbuild_extensions.xml".
      * 
      * @see #extensions
      * @see #listeners
      * @see #reports
      */
     private static void loadExtensions() {
         File config = new File(installDir + File.separatorChar + extensionsConfigFile);
         if (!config.exists()) return;
         
         DOMParser parser = null;
         extensions = new Hashtable();
         listeners = new Hashtable();
         reports = new Hashtable();
 
         // Parse config file
         try {
             parser = new DOMParser();
             parser.parse(config.getAbsolutePath());
         } catch (IOException e) {
             logger.error("Loading of \"" + extensionsConfigFile + "\" failed", e);
             return;
         } catch (SAXException  e) {
             logger.error("\"" + extensionsConfigFile + "\" is not properly formatted", e);
             return;
         } catch (Exception e) {
             logger.error("Exception occured while reading \"" + extensionsConfigFile + "\"", e);
             return;
         }
 
         // Load extensions
         try {
             NodeList extension_elements = parser.getDocument().getElementsByTagName("extensions").item(0).getChildNodes();
             for (int ex = 0; ex < extension_elements.getLength(); ex++) {
                 Node extension_element = extension_elements.item(ex);
 
                 if (extension_element.getNodeName().equals("extension")) {
                     String extensionName = extension_element.getAttributes().getNamedItem("name").getNodeValue();
                     String className = extension_element.getAttributes().getNamedItem("class").getNodeValue();
 
                     if (extensionName == null || extensionName.trim().equals("")) {
                         logger.error("Extension name is missing or empty in \"" + extensionsConfigFile + "\"");
                         continue;
                     }
                     if (className == null || className.trim().equals("")) {
                         logger.error("Extension class name is missing or empty in \"" + extensionsConfigFile + "\"");
                         continue;
                     }
 
                     Object extension = null;
 
                     try {
                         Class extensionClass = Class.forName(className);
                         extension = extensionClass.newInstance();
                     } catch (ClassNotFoundException e) {
                         logger.error("Extension class " + className + " for luntbuild extension \"" + extensionName + "\" not found", e);
                         continue;
                     } catch (InstantiationException e) {
                         logger.error("Extension occured while instantiating extension class " + className  + " for luntbuild extension \"" + extensionName + "\"", e);
                         continue;
                     } catch (IllegalAccessException e) {
                         logger.error("Extension class for " + className + " luntbuild extension \"" + extensionName + "\" doesn't have a public constructor", e);
                         continue;
                     }
 
                     extensions.put(extensionName, extension);
                 }
             }
         } catch (Exception e) {
             logger.error("Exception occured while loading Luntbuild extensions", e);
         }
 
         // Load listeners
         try {
             NodeList listener_elements = parser.getDocument().getElementsByTagName("listeners").item(0).getChildNodes();
             for (int l = 0; l < listener_elements.getLength(); l++) {
                 Node listener_element = listener_elements.item(l);
 
                 if (listener_element.getNodeName().equals("listener")) {
                     String listenerName = listener_element.getAttributes().getNamedItem("name").getNodeValue();
                     String className = listener_element.getAttributes().getNamedItem("class").getNodeValue();
 
                     if (listenerName == null || listenerName.trim().equals("")) {
                         logger.error("Listener name is missing or empty in \"" + extensionsConfigFile + "\"");
                         continue;
                     }
                     if (className == null || className.trim().equals("")) {
                         logger.error("Listener class name is missing or empty in \"" + extensionsConfigFile + "\"");
                         continue;
                     }
 
                     Object listener = null;
 
                     try {
                         Class extensionClass = Class.forName(className);
                         listener = extensionClass.newInstance();
                     } catch (ClassNotFoundException e) {
                         logger.error("Listener class " + className + " for luntbuild listener \"" + listenerName + "\" not found", e);
                         continue;
                     } catch (InstantiationException e) {
                         logger.error("Listener occured while instantiating listener class " + className  + " for luntbuild listener \"" + listenerName + "\"", e);
                         continue;
                     } catch (IllegalAccessException e) {
                         logger.error("Listener class for " + className + " luntbuild listener \"" + listenerName + "\" doesn't have a public constructor", e);
                         continue;
                     }
 
                     // Make sure that the class implements the correct interface
                     try {
                         Listener test = (Listener) listener;
                         test.equals(test);
                     } catch (Exception e) {
                         logger.error("Listener class " + className + " does not implement " + Listener.class.toString());
                         continue;
                     }
 
                     listeners.put(listenerName, listener);
                 }
             }
         } catch (Exception e) {
             logger.error("Exception occured while loading Luntbuild listeners", e);
         }
 
         // Load reports
         try {
             NodeList report_elements = parser.getDocument().getElementsByTagName("reports").item(0).getChildNodes();
             for (int r = 0; r < report_elements.getLength(); r++) {
                 Node report_element = report_elements.item(r);
 
                 if (report_element.getNodeName().equals("report")) {
                     String reportName = report_element.getAttributes().getNamedItem("name").getNodeValue();
                     String className = report_element.getAttributes().getNamedItem("class").getNodeValue();
 
                     if (reportName == null || reportName.trim().equals("")) {
                         logger.error("Report name is missing or empty in \"" + extensionsConfigFile + "\"");
                         continue;
                     }
                     if (className == null || className.trim().equals("")) {
                         logger.error("Report class name is missing or empty in \"" + extensionsConfigFile + "\"");
                         continue;
                     }
 
                     Object report = null;
 
                     try {
                         Class extensionClass = Class.forName(className);
                         report = extensionClass.newInstance();
                     } catch (ClassNotFoundException e) {
                         logger.error("Report class " + className + " for luntbuild report \"" + reportName + "\" not found", e);
                         continue;
                     } catch (InstantiationException e) {
                         logger.error("Report occured while instantiating report class " + className  + " for luntbuild report \"" + reportName + "\"", e);
                         continue;
                     } catch (IllegalAccessException e) {
                         logger.error("Report class for " + className + " luntbuild report \"" + reportName + "\" doesn't have a public constructor", e);
                         continue;
                     }
 
                     // Make sure that the class implements the correct interface
                     try {
                         Report test = (Report) report;
                         test.equals(test);
                     } catch (Exception e) {
                         logger.error("Report class " + className + " does not implement " + Report.class.toString());
                         continue;
                     }
 
                     reports.put(reportName, report);
                 }
             }
         } catch (Exception e) {
             logger.error("Exception occured while loading Luntbuild reports", e);
         }
     }
 
     /**
      * Shuts down the Luntbuild system.
      * 
      * <p>Does some cleanup works, such as cleanup the schedule thread, etc.</p>
      * 
      * @param context the servlet context
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
 				Statement stmt = null;
 				try {
 					Class.forName(HSQLDB_DRIVER).newInstance();
 					Connection connection = DriverManager.getConnection("jdbc:hsqldb:" +
 							new File(installDir + "/db/luntbuild").getAbsolutePath(),
 							props.getProperty("jdbc.username"), props.getProperty("jdbc.password"));
 					stmt = connection.createStatement();
 					stmt.executeUpdate("SHUTDOWN");
 					connection.close();
 				} catch (Exception e) {
 					e.printStackTrace();
 				} finally {
 					if (stmt != null) try {stmt.close();} catch (Exception e) {}
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
 
     /**
      * Removes the special "checkin user" from a list of users if it exists.
      * 
      * @param users the list of users
      * @return the list of users without the special "checkin user"
      */
     public static List removeCheckinUser(List users) {
         Iterator it = users.iterator();
         while (it.hasNext()) {
             User user = (User) it.next();
             if (user.getName().equals(User.CHECKIN_USER_NAME_RECENT)
                     || user.getName().equals(User.CHECKIN_USER_NAME_ALL)) {
                 it.remove();
             }
         }
         return users;
     }
 
     /**
      * Creates and initializes a blank ant project.
      * 
      * @return the ant project
      */
     public static Project createAntProject() {
         Project antProject = new Project();
         antProject.init();
         return antProject;
     }
 
     /**
      * Gets the name of a property or assignment from a string (example: "name=value").
      * 
      * @param assignment the assignment string
      * @return the name of the property or assignment
      */
     public static String getAssignmentName(String assignment) {
         int index = assignment.indexOf('=');
         if (index == -1) {
             return assignment.trim();
         } else {
             return assignment.substring(0, index).trim();
         }
     }
 
     /**
      * Gets the value of a property or assignment from a string (example: "name=value").
      * 
      * @param assignment the assignment string
      * @return the value of the property or assignment
      */
     public static String getAssignmentValue(String assignment) {
         int index = assignment.indexOf('=');
         if (index == -1) {
             return "";
         } else {
             return assignment.substring(index + 1).trim();
         }
     }
 
     /**
      * Encrypts a password.
      * 
      * @param passwd the unencrypted password
      * @return the encrypted password
      * @throws RuntimeException if an {@link StringEncrypter.EncryptionException} occurs
      * @see StringEncrypter
      */
     public static String encryptPassword(String passwd) {
         if (passwd == null || passwd.trim().length() == 0) return passwd;
 
         try {
             StringEncrypter encrypter = new StringEncrypter(StringEncrypter.DESEDE_ENCRYPTION_SCHEME);
             return encrypter.encrypt(passwd);
         } catch (StringEncrypter.EncryptionException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * Decrypts a password.
      * 
      * @param passwd the encrypted password
      * @return the decrypted password
      * @throws RuntimeException if an {@link StringEncrypter.EncryptionException} occurs
      * @see StringEncrypter
      */
     public static String decryptPassword(String passwd) {
         if (passwd == null || passwd.trim().length() == 0) return passwd;
 
         try {
             StringEncrypter encrypter = new StringEncrypter(StringEncrypter.DESEDE_ENCRYPTION_SCHEME);
             return encrypter.decrypt(passwd);
         } catch (StringEncrypter.EncryptionException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * Gets the page refresh interval.
      * 
      * @return the page refresh interval
      */
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
 
     /**
      * Gets the system properties map.
      * 
      * @return the system properties
      */
     public static Map getProperties() {
         return properties;
     }
 
     /**
      * Sets the system properties map.
      * 
      * @param properties the system properties
      */
     public static void setProperties(Map properties) {
         Luntbuild.properties = Collections.synchronizedMap(properties);
     }
 
     /**
      * Used for Java 1.4 compatability.
      * Replace with Node.getTextContent() for Java 1.5.
      * 
      * @param node the node
      * @return the text content
      */
     public static String getTextContent(Node node) {
        // TODO: Replace with Node.getTextContent() for Java 1.5
        NodeList nodeList= node.getChildNodes();
         String textContent= null;
           for (int j = 0; j < nodeList.getLength(); j++) {
               Node k = nodeList.item(j);
               textContent = k.getNodeValue();
               if (StringUtils.isNotEmpty(textContent))
                   return textContent;
           }
           return "";
     }
 
     /**
      * Sends the contents of the specified asset to the specified HTTP request.
      * 
      * @param request the HTTP request
      * @param response the HTTP response 
      * @param assetLocation the location of the asset
      * @throws ApplicationRuntimeException if the asset could not be found or read
      */
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
      * Encodes a character for XML.
      * 
      * @param ch the character
      * @return the encoded character, or <code>null</code> if the character does not need to be encoded
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
 
     /**
      * Checks if a character needs to be encoded for XML.
      * 
      * @param ch the character
      * @return <code>true</code> if the character needs to be encoded
      */
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
      * Substitutes all XML special characters to enable XML parsing.
      *
      * @param str the input string
      * @return the string with the substituted characters
      */
     public static String xmlEncodeEntities(String str) {
        if (str == null)
            return str;
         StringBuffer buf = new StringBuffer();
         int len = str.length();
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
