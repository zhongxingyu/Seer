 /*
  * PatientView
  *
  * Copyright (c) Worth Solutions Limited 2004-2013
  *
  * This file is part of PatientView.
  *
  * PatientView is free software: you can redistribute it and/or modify it under the terms of the
  * GNU General Public License as published by the Free Software Foundation, either version 3 of the License,
  * or (at your option) any later version.
  * PatientView is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
  * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License along with PatientView in a file
  * titled COPYING. If not, see <http://www.gnu.org/licenses/>.
  *
  * @package PatientView
  * @link http://www.patientview.org
  * @author PatientView <info@patientview.org>
  * @copyright Copyright (c) 2004-2013, Worth Solutions Limited
  * @license http://www.gnu.org/licenses/gpl-3.0.html The GNU General Public License V3.0
  */
 
 package org.patientview.patientview.parser;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import javax.servlet.ServletContext;
 import org.patientview.patientview.FindXmlFiles;
 import org.patientview.patientview.ParserThread;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class XmlParserThread implements Runnable, ParserThread {
 
     private String prebit;
     private String directory;
     private String archiveDirectory;
     private int minutesBetweenWait;
     private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
     private ServletContext servletContext;
     private String[] fileEndings;
 
     private static final Logger LOGGER = LoggerFactory.getLogger(XmlParserThread.class);
 
     private static final int SECONDS_IN_MINUTE = 60;
     private static final int MILLISECONDS = 1000;
 
     public XmlParserThread(String[] fileEndings) {
         this.fileEndings = fileEndings;
     }
 
     public XmlParserThread(ServletContext context, String xmlDirectory,
                            String xmlArchiveDirectory, int minutesBetweenWait) {
         this.directory = xmlDirectory;
         this.archiveDirectory = xmlArchiveDirectory;
         this.minutesBetweenWait = minutesBetweenWait;
         this.servletContext = context;
     }
 
     public void run() {
         try {
             while (true) {
                 File[] xmlFiles = FindXmlFiles.findXmlFiles(directory, fileEndings);
                 updateXmlFiles(xmlFiles);
                 Thread.sleep(MILLISECONDS * SECONDS_IN_MINUTE * minutesBetweenWait);
                 Date now = new Date(System.currentTimeMillis());
                LOGGER.debug("XmlParserThread " + dateFormat.format(now));
             }
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
     }
 
     private void updateXmlFiles(File[] xmlFiles) {
 
         if (xmlFiles != null && xmlFiles.length > 0) {
 
             LOGGER.info("Starting XmlParserThread for {} files", xmlFiles.length);
 
             for (int i = 0; i < xmlFiles.length; i++) {
                 LOGGER.debug("Starting XmlParserThread for {} file", xmlFiles[i].getAbsolutePath());
                 XmlParserUtils.updateXmlData(servletContext, xmlFiles[i]);
                 //xmlFiles[i].delete();
             }
         }
 
         LOGGER.info("Finished XmlParserThread");
     }
 
     public String getPrebit() {
         return prebit;
     }
 
     public void setPrebit(String prebit) {
         this.prebit = prebit;
     }
 
     public String getDirectory() {
         return directory;
     }
 
     public void setDirectory(String directory) {
         this.directory = directory;
     }
 
     public String getArchiveDirectory() {
         return archiveDirectory;
     }
 
     public void setArchiveDirectory(String archiveDirectory) {
         this.archiveDirectory = archiveDirectory;
     }
 
     public int getMinutesBetweenWait() {
         return minutesBetweenWait;
     }
 
     public void setMinutesBetweenWait(int minutesBetweenWait) {
         this.minutesBetweenWait = minutesBetweenWait;
     }
 
     public ServletContext getServletContext() {
         return servletContext;
     }
 
     public void setServletContext(ServletContext servletContext) {
         this.servletContext = servletContext;
     }
 }
