 /*
  * #%L
  * Bitrepository Protocol
  * 
  * $Id$
  * $HeadURL$
  * %%
  * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as 
  * published by the Free Software Foundation, either version 2.1 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Lesser Public License for more details.
  * 
  * You should have received a copy of the GNU General Lesser Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/lgpl-2.1.html>.
  * #L%
  */
 package org.bitrepository.common.settings;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.bitrepository.common.JaxbHelper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xml.sax.SAXException;
 
 /**
  * Loads settings as xml from the classpath.
  *
  */
 public class XMLFileSettingsLoader implements SettingsLoader {
     /** The log for this class. */
     private final Logger log = LoggerFactory.getLogger(getClass());
 
     private String pathToSettingsFiles;
 
     /** Directory seperator constant */
     private static final String DIRECTORY_SEPERATOR = "/";
     /** xml file extension constant */
     private static final String XML_FILE_EXTENSION = ".xml";
     /** xsd file extension constant */
     private static final String XSD_FILE_EXTENSION = ".xsd";
     /** xsd schema directory constant */
     private static final String XSD_SCHEMA_DIR = "xsd/";
 
     /**
      * Creates a new loader
      * @param pathToSettingsFiles The location of the files to load. The settings xml files are assume to be placed as
      * ${CONFIGURATION_CLASS}.xml under this directory.
      *
      */
     public XMLFileSettingsLoader(String pathToSettingsFiles) {
         this.pathToSettingsFiles = pathToSettingsFiles;
     }
 
     /**
      * Loads the indicated settings for the specified collection.
      * @param settingsClass The settings class identifying the type of settings requested.
      * @param <T> The type of settings to return
      * @return The loaded settings.
      */
     public <T> T loadSettings(Class<T> settingsClass) {
         StringBuilder fileLocationBuilder = new StringBuilder();
        if (pathToSettingsFiles != null && !pathToSettingsFiles.equals("")) {
             fileLocationBuilder.append(pathToSettingsFiles + DIRECTORY_SEPERATOR);
         }
         fileLocationBuilder.append(settingsClass.getSimpleName() + XML_FILE_EXTENSION);
         String fileLocation = fileLocationBuilder.toString();
         String schemaLocation = settingsClass.getSimpleName() + XSD_FILE_EXTENSION;
         JaxbHelper jaxbHelper = new JaxbHelper(XSD_SCHEMA_DIR, schemaLocation);
         InputStream configStreamLoad = null;
         InputStream configStreamValidate = null;
         try {
             configStreamLoad = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileLocation);
             configStreamValidate = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileLocation);
             if (configStreamLoad == null) {
                 try {
                     configStreamLoad = new FileInputStream(fileLocation);
                     configStreamValidate = new FileInputStream(fileLocation);
                 } catch (FileNotFoundException e) {
                     throw new RuntimeException("Unable to load settings from " + fileLocation, e);
                 }
             }
             log.debug("Loading the settings file '" + fileLocation + "'.");
             try {
                 jaxbHelper.validate(configStreamValidate);
                 return (T) jaxbHelper.loadXml(settingsClass, configStreamLoad);
             } catch (SAXException e) {
                 throw new RuntimeException("Unable to validate settings from " +
                         Thread.currentThread().getContextClassLoader().getResource(fileLocation), e);
             } catch (Exception e) {
                 throw new RuntimeException("Unable to load settings from " + fileLocation, e);
             }
         } finally {
             try {
                 if (configStreamLoad != null) configStreamLoad.close();
                 if (configStreamValidate != null) configStreamValidate.close();
             } catch (IOException e) {
                 throw new RuntimeException("Unable to close inputstream", e);
             }
         }
     }
 }
