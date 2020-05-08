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
 import java.io.InputStream;
 
 import javax.xml.bind.JAXBException;
 
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
 
     /**
      * Creates a new loader
      * @param pathToSettingsFiles The location of the files to load. The settings xml files are assume to be placed as
      * ${COLLECTION_ID}/${CONFIGURATION_CLASS}.xml under this directory.
      * 
      * Note the the file is loaded from the class path so the path should only be different from the default "" if a 
      * need to 
      */
     public XMLFileSettingsLoader(String pathToSettingsFiles) {
         this.pathToSettingsFiles = pathToSettingsFiles;
     }
 
     /**
      * Loads the indicated settings for the specified collection.
      * @param <T> The type of settings to return
      * @param collectionID The collectionID to load the settings for.
      * @param settingsClass The settings class identifying the type of settings requested.
      * @return The loaded settings.
      */
     public <T> T loadSettings(String collectionID, Class<T> settingsClass) {
         String fileLocation = pathToSettingsFiles + "/" + collectionID + "/" + settingsClass.getSimpleName() + ".xml";
         String schemaLocation = "xsd/" + settingsClass.getSimpleName() + ".xsd";
         JaxbHelper jaxbHelper = new JaxbHelper(schemaLocation);
         InputStream configStreamLoad = ClassLoader.getSystemResourceAsStream(fileLocation);
         InputStream configStreamValidate = ClassLoader.getSystemResourceAsStream(fileLocation);
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
        } catch (JAXBException e) {
            throw new RuntimeException("Unable to load settings from " + fileLocation, e);
         } catch (SAXException e) {
        throw new RuntimeException("Unable to validate settings from " + fileLocation, e);
    	}
     }
 }
