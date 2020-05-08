 /*
  * Copyright (c) 2010-2012 Matthias Klass, Johannes Leimer,
  *               Rico Lieback, Sebastian Gabriel, Lothar Gesslein,
  *               Alexander Rampp, Kai Weidner
  *
  * This file is part of the Physalix Enrollment System
  *
  * Foobar is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Foobar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package hsa.awp.common.naming;
 
 import hsa.awp.common.exception.ConfigurationException;
 import hsa.awp.common.exception.NoMatchingElementException;
 import hsa.awp.common.exception.ProgrammingErrorException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.naming.NamingEnumeration;
 import javax.naming.NamingException;
 import javax.naming.directory.Attributes;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Properties;
 import java.util.Set;
 
 /**
  * Provides code to lookup user details. This class is a singleton
  *
  * @author alex
  */
 public final class Directory implements IAbstractDirectory {
   /**
    * Holds the directory instance.
    */
   private static Directory instance;
 
   /**
    * Logger used to log useful informations.
    */
   private Logger logger = LoggerFactory.getLogger(getClass());
 
   /**
    * The adapter to do the user details lookup.
    */
   private IDirectoryAdapter adapter;
 
   /**
    * Provides a mapping from directory field names to abstract field names used in this programm (==> so that we are independent
    * from a ldap shema).
    */
   private Properties fieldMapping;
 
   /**
    * Returns an the instance of the directory. If the instance does not exist, it will be created.
    *
    * @return An instance of Directory.
    */
   public static Directory getInstance() {
 
     if (instance == null) {
       instance = new Directory();
     }
     return instance;
   }
 
   /**
    * This is a singleton - so the constructor is private.
    */
   private Directory() {
 
   }
 
   @Override
   public Properties getUserProperties(String username) {
 
     Attributes attrs;
     try {
       attrs = adapter.getAttributes(username);
       logger.trace("received attributes: {}", attrs);
     } catch (NullPointerException e) {
       throw new ConfigurationException("DirectoryAdapter not set.", e);
     } catch (NamingException e) {
       throw new NoMatchingElementException("Username " + username + " not found.", e);
     }
 
     return createProperties(attrs);
   }
 
   @Override
   public Properties getUserProperties(Long uuid) {
 
     if (uuid == null) {
       throw new IllegalArgumentException("uuid must not be null");
     }
     Attributes attrs;
     try {
       attrs = adapter.getAttributes(uuid);
     } catch (NullPointerException e) {
       throw new ConfigurationException("DirectoryAdapter not set.", e);
     } catch (NamingException e) {
       throw new NoMatchingElementException("User with id " + uuid + " not found.", e);
     }
     return createProperties(attrs);
   }
 
   @Override
   public void setDirectoryAdapter(IDirectoryAdapter adapter) {
 
     this.adapter = adapter;
   }
 
   @Override
   public void setFieldMappingFile(String propertyFile) {
 
     setupFieldMapping(propertyFile);
   }
 
   @Override
   public List<Properties> searchForUser(String searchString) {
 
     List<Properties> properties = new ArrayList<Properties>();
 
     for (Attributes attributes : adapter.searchDirectory(searchString)) {
       properties.add(createProperties(attributes));
     }
 
     return properties;
   }
 
   @Override
   public Set<String> readAllStudyCourses() {
     return adapter.getAllStudyCourses();
   }
 
   /**
    * Converts given attributes to properties.
    *
    * @param attrs - The attributes to convert.
    * @return Converted attributes as properties
    */
   private Properties createProperties(Attributes attrs) {
 
     Properties result = new Properties();
     NamingEnumeration<String> ids = attrs.getIDs();
 
     while (ids.hasMoreElements()) {
       try {
         String currentId = ids.next();
         String abstractId = getAbstractFieldName(currentId);
         if (abstractId == null) {
           continue;
         }
         String value = (String) attrs.get(currentId).get();
 
         // Semester field has the format 0079$6 for example we are only
         // interested in the chars after the $
         logger.trace("inspecting field {}=>{} : value = '{}'", new Object[]{currentId, abstractId, value});
         if (abstractId.equals(IAbstractDirectory.TERM)) {
           // FIXME this should be done in a higher abstraction layer (alex)
           logger.trace("field need additional convertion");
           value = value.split("\\$")[1];
           logger.trace("value is now '{}'", value);
         }
 
         result.put(abstractId, value);
       } catch (NamingException e) {
         throw new ProgrammingErrorException(e);
         // this should never occur
       }
     }
 
     return result;
   }
 
   @Override
   public String getLowLevelFieldName(String abstractFieldName) {
    abstractFieldName = abstractFieldName.toUpperCase();
    for (Entry<Object, Object> mappingEntry : fieldMapping.entrySet()) {
      if (mappingEntry.getValue().equals(abstractFieldName)) {
        return (String) mappingEntry.getKey();
      }
    }
    return null;
   }
 
   @Override
   public String getAbstractFieldName(String lowLevelName) {
 
     String fieldConstant = fieldMapping.getProperty(lowLevelName);
     if (fieldConstant == null) {
       fieldConstant = fieldMapping.getProperty(lowLevelName.toLowerCase());
       if (fieldConstant == null) {
         return null;
       }
     }
     try {
       logger.trace("searching for fieldConstant '{}'", fieldConstant);
       return (String) this.getClass().getField(fieldConstant).get(new String());
     } catch (IllegalArgumentException e) {
       throw new ProgrammingErrorException("This was a programming error, please call developers for support.", e);
     } catch (SecurityException e) {
       throw new ProgrammingErrorException("This was a programming error, please call developers for support.", e);
     } catch (IllegalAccessException e) {
       throw new ProgrammingErrorException("This was a programming error, please call developers for support.", e);
     } catch (NoSuchFieldException e) {
       throw new ConfigurationException("Configuration error, the mapping in the config file is not right.", e);
     }
   }
 
   public void setFieldMapping(Properties mapping) {
     // now we reverse the properties
     fieldMapping = new Properties();
     for (Entry<Object, Object> currentEntry : mapping.entrySet()) {
       fieldMapping.put(currentEntry.getValue(), currentEntry.getKey());
     }
   }
 
   /**
    * This procedure reads <code>fieldMapping</code> from the properties file and reverses it. This is needed because the
    * properties file is in the format [fieldname]=[ldapname] (more convenient to configure) but we need them in format
    * [ldapname]=[fieldname].
    *
    * @param propertyFile - The properties file to read from.
    */
   private void setupFieldMapping(String propertyFile) {
     // loading the properties file in a temporary object
     Properties tmpProps = new Properties();
     try {
       InputStream in = ClassLoader.getSystemResourceAsStream(propertyFile);
       if (in == null) {
         throw new ConfigurationException("PropertyFile " + propertyFile + " is not in CLASSPATH");
       }
       tmpProps.load(in);
     } catch (IOException e) {
       throw new ConfigurationException("Could not read configuration file: " + propertyFile, e);
     }
 
     // now we reverse the properties
     fieldMapping = new Properties();
     for (Entry<Object, Object> currentEntry : tmpProps.entrySet()) {
       fieldMapping.put(currentEntry.getValue(), currentEntry.getKey());
     }
   }
 }
