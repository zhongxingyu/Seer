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
 package org.patientview.quartz;
 
 import org.patientview.patientview.FindXmlFiles;
 import org.patientview.utils.LegacySpringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Value;
 
 import java.io.File;
 
 /**
  * Quartz XmlImportJobQuartzScheduler Job
  */
 public class XmlImportJobQuartzScheduler {
 
     @Value("${run.xml.import}")
     private String runXMLImport;
 
     @Value("${xml.directory}")
     private String xmlDirectory;
 
     private String[] fileEndings = {".xml", };
 
     private static final Logger LOGGER = LoggerFactory.getLogger(XmlImportJobQuartzScheduler.class);
 
     public void execute() {
         try {
             updateXmlFiles();
         } catch (Exception e) {
             LOGGER.error(e.getMessage());
             LOGGER.debug(e.getMessage(), e);
         }
     }
 
     private void updateXmlFiles() {
         int numFilesProcessed = 0;
         if ((runXMLImport == null) || !"false".equals(runXMLImport)) {
             File[] xmlFiles = FindXmlFiles.findXmlFiles(xmlDirectory, fileEndings);
             if (xmlFiles != null && xmlFiles.length > 0) {
                 LOGGER.debug("Starting updateXmlFiles for {} files", xmlFiles.length);
                 for (File xmlFile : xmlFiles) {
                     LOGGER.debug("Starting updateXmlFiles for {} file", xmlFile.getAbsolutePath());
                     LegacySpringUtils.getImportManager().update(xmlFile);
                     numFilesProcessed++;
                 }
             }
             LOGGER.info("Patient data importer finished for {} files", numFilesProcessed);
         }
     }
 
     public String getRunXMLImport() {
         return runXMLImport;
     }
 
     public void setRunXMLImport(String runXMLImport) {
         this.runXMLImport = runXMLImport;
     }
 
     public String getXmlDirectory() {
         return xmlDirectory;
     }
 
     public void setXmlDirectory(String xmlDirectory) {
         this.xmlDirectory = xmlDirectory;
     }
 }
