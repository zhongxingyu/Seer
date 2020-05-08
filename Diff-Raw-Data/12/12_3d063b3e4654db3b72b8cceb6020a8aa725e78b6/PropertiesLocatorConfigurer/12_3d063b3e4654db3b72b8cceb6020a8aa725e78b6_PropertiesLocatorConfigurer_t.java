 /*
  * This is a utility project for wide range of applications
  * 
  * Copyright (C) 8  Imran M Yousuf (imyousuf@smartitengineering.com)
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  10-1  USA
  */
 package com.smartitengineering.util.spring;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Properties;
 import org.apache.commons.lang.StringUtils;
 import org.springframework.beans.factory.BeanFactoryAware;
 import org.springframework.beans.factory.BeanNameAware;
 import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
 import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
 import org.springframework.core.PriorityOrdered;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.core.io.FileSystemResource;
 import org.springframework.core.io.Resource;
 import org.springframework.util.DefaultPropertiesPersister;
 import org.springframework.util.PropertiesPersister;
 
 /**
  * This class will mainly search for a designated properties file at locations
  * predefined by system (that is this module) and user through app context. <p />
  * Preconfigured locations according to ascending priority is -
  * <ul>
  *  <li>System properties (if enabled)</li>
  *  <li>Classpath for default resource (defaultResourceSuffix appended to the path)</li>
  *  <li>Current working directory</li>
  *  <li>Home directory</li>
  *  <li>User specified directory in order specified</li>
  * </ul>
  * 
  * @author imyousuf
  */
 public class PropertiesLocatorConfigurer
     extends PropertyPlaceholderConfigurer
     implements BeanFactoryPostProcessor,
                PriorityOrdered,
                BeanNameAware,
                BeanFactoryAware {
 
     public static final String DEFAULT_RESOURCE_SUFFIX = ".template";
     private String defaultResourceSuffix =
         PropertiesLocatorConfigurer.DEFAULT_RESOURCE_SUFFIX;
     private String[] smartLocations;
     private PropertiesPersister myPropertiesPersister =
         new DefaultPropertiesPersister();
     private boolean ignoreResourceNotFound = false;
     private String fileEncoding = null;
     private boolean defaultSearchEnabled = true;
     private boolean classpathSearchEnabled = true;
     private boolean currentDirSearchEnabled = true;
     private boolean userHomeSearchEnabled = true;
     private String resourceContext;
     private String[] searchLocations;
 
     /**
      * Loads properties file from locations as it is supposed.
      * @param props The properties object that is filled.
      * @throws java.io.IOException If failed to load the properties or error
      *                              reading a resoure
      */
     @Override
     protected void loadProperties(Properties props)
         throws IOException {
         if (this.smartLocations != null) {
             for (int i = 0; i < this.smartLocations.length; i++) {
                 String location = this.smartLocations[i];
                 if (logger.isInfoEnabled()) {
                     logger.info("Loading properties file from " + location);
                 }
                 InputStream is = null;
                 String fileName =
                     new StringBuilder(getResourceContext()).append(location).
                     toString();
                 if (fileName == null) {
                     continue;
                 }
                 try {
                     boolean resourceFound = false;
                     Resource resource;
                     if (isDefaultSearchEnabled()) {
                         String resourceName = new StringBuilder(fileName).append(
                             getDefaultResourceSuffix()).toString();
                         resource =
                             new ClassPathResource(resourceName);
                         is = attemptToLoadResource(props, resource);
                         resourceFound = closeInputStream(is);
                     }
                     if (isClasspathSearchEnabled()) {
                         resource =
                             new ClassPathResource(fileName);
                         is = attemptToLoadResource(props, resource);
                         resourceFound = closeInputStream(is) || resourceFound;
                     }
                     if (isCurrentDirSearchEnabled()) {
                         String parent = System.getProperty("user.dir");
                         resourceFound = attempToReadRsrcFromFile(parent,
                             fileName, resourceFound, props);
                     }
                     if (isUserHomeSearchEnabled()) {
                         String parent = System.getProperty("user.home");
                         resourceFound = attempToReadRsrcFromFile(parent,
                             fileName, resourceFound, props);
                     }
                     if (searchLocations != null) {
                         for (String searchLocation : searchLocations) {
                             if (StringUtils.isNotEmpty(StringUtils.trim(
                                 searchLocation))) {
                                 resourceFound = attempToReadRsrcFromFile(
                                     searchLocation, fileName, resourceFound,
                                     props);
                             }
                         }
                     }
                     if (!resourceFound) {
                         throw new RuntimeException(fileName + " not found!");
                     }
                 }
                 catch (Exception ex) {
                     if (this.ignoreResourceNotFound) {
                         if (logger.isWarnEnabled()) {
                             logger.warn("Could not load properties from " +
                                 location + ": " + ex.getMessage());
                         }
                     }
                     else {
                         throw new IOException(ex);
                     }
                 }
                 finally {
                     closeInputStream(is);
                 }
             }
         }
     }
 
     /**
      * 
      * @param parent The parent folder of the fileName
      * @param fileName The file to read
      * @param resourceFound True if resource was earlier found
      * @param props The properties object to fill the properties with
      * @return True if either resourceFound is true or resource was read from
      *         fileName
      * @throws java.io.IOException If error in reading the file
      */
     protected boolean attempToReadRsrcFromFile(String parent,
                                                String fileName,
                                                boolean resourceFound,
                                                Properties props)
         throws IOException {
         File resourceFile = new File(parent, fileName);
         Resource resource =
             new FileSystemResource(resourceFile);
         InputStream is = attemptToLoadResource(props, resource);
         return closeInputStream(is) || resourceFound;
     }
 
     /**
      * Return suffix for default resource file.
      * @return Suffix for the classpath default resource
      */
     protected String getDefaultResourceSuffix() {
         return defaultResourceSuffix;
     }
 
     /**
      * Set the suffix for the default resource file
      * @param defaultResourceSuffix The suffix of he default resource
      */
     public void setDefaultResourceSuffix(String defaultResourceSuffix) {
         this.defaultResourceSuffix = defaultResourceSuffix;
     }
 
     /**
      * Retrieves the context of the search. The context will be added before the
      * for every resource search. It is primarily useful if you multiple config
      * group for single application.
      * @return The context for current configurer
      */
     protected String getResourceContext() {
         if (resourceContext == null) {
             return "";
         }
         return resourceContext;
     }
 
     /**
      * Sets the context for the resource context for this config group lookup.
      * @param resourceContext The context to search the current configs.
      */
     public void setResourceContext(String resourceContext) {
         this.resourceContext = resourceContext;
     }
 
     /**
      * Load the current resource into the provided properties file. It respects
      * type of properties and encoding if set.
      * @param props Properties file to fill
      * @param resource Resource to load if present
      * @return The input stream of the resource.
      */
     protected InputStream attemptToLoadResource(Properties props,
                                                 Resource resource) {
         InputStream is = null;
         try {
             is = resource.getInputStream();
             if (resource.getFilename().endsWith(XML_FILE_EXTENSION)) {
                 this.myPropertiesPersister.loadFromXml(props, is);
             }
             else {
                 if (this.fileEncoding != null) {
                     this.myPropertiesPersister.load(props,
                         new InputStreamReader(is, this.fileEncoding));
                 }
                 else {
                     this.myPropertiesPersister.load(props, is);
                 }
             }
         }
         catch (IOException ex) {
         }
         return is;
     }
 
     private boolean closeInputStream(InputStream is)
         throws IOException {
         if (is != null) {
             is.close();
             return true;
         }
         return false;
     }
 
     /**
      * This operation is restricted from this configurer.
      * @param location
      */
     @Override
     public void setLocation(Resource location) {
         throw new UnsupportedOperationException();
     }
 
     /**
      * This operation is restricted from this configurer.
      * @param locations
      */
     @Override
     public void setLocations(Resource[] locations) {
         throw new UnsupportedOperationException();
     }
 
     /**
      * Set the encoding of the resource file to read in. It basically delegates
      * through to parents method, but also sets the value in current method to
      * used for reading the input stream.
      * @param encoding Encoding of the resource
      */
     @Override
     public void setFileEncoding(String encoding) {
         this.fileEncoding = encoding;
         super.setFileEncoding(encoding);
     }
 
     /**
      * Set the single custom resource to search at.
      * @param smartLocation The custom resource
      */
     public void setSmartLocation(String smartLocation) {
         this.smartLocations = new String[]{smartLocation};
         super.setLocation(new ClassPathResource(smartLocation));
     }
 
     /**
      * The custom resources as CSV. Its main intended use would be to supply
      * custom resources through another properties file to keep the resources
      * dynamic.
      * @param smartLocationsAsCsv The resources as comma separated values (csv)
      */
     public void setSmartLocationsAsCsv(String smartLocationsAsCsv) {
         setSearchLocations(smartLocationsAsCsv.split(","));
     }
 
     /**
      * The custom resources as array, its main intended use case would be from
      * an application context XML file.
      * @param smartLocations The resources as an array
      */
     public void setSmartLocations(String[] smartLocations) {
         this.smartLocations = smartLocations;
         Resource[] resources = new Resource[smartLocations.length];
         for (int i = 0; i < smartLocations.length; ++i) {
             String smartLocation = StringUtils.trim(smartLocations[i]);
             if (StringUtils.isNotEmpty(smartLocation)) {
                 resources[i] = new ClassPathResource(smartLocation);
             }
         }
         super.setLocations(resources);
     }
 
     /**
      * Retrieves whether search in classpath is enabled or not.
      * @return True if search is enabled in classpath
      */
     protected boolean isClasspathSearchEnabled() {
         return classpathSearchEnabled;
     }
 
     /**
      * Sets whether search in classpath is enabled or not
      * @param classpathSearchEnabled True if search is enabled for classpath
      */
     public void setClasspathSearchEnabled(boolean classpathSearchEnabled) {
         this.classpathSearchEnabled = classpathSearchEnabled;
     }
 
     /**
      * Retrieves whether search in current directory is enabled or not.
      * @return True if search is enabled in current directory
      */
     protected boolean isCurrentDirSearchEnabled() {
         return currentDirSearchEnabled;
     }
 
     /**
      * Sets whether search in current directory is enabled or not
      * @param currentDirSearchEnabled True if search is enabled for current dir
      */
     public void setCurrentDirSearchEnabled(boolean currentDirSearchEnabled) {
         this.currentDirSearchEnabled = currentDirSearchEnabled;
     }
 
     /**
      * Retrieves whether search in classpath for default is enabled or not.
      * @return True if search is enabled for enabled
      */
     protected boolean isDefaultSearchEnabled() {
         return defaultSearchEnabled;
     }
 
     /**
      * Sets whether search in default classpath is enabled or not
      * @param defaultSearchEnabled True if search is enabled for default cp
      */
     public void setDefaultSearchEnabled(boolean defaultSearchEnabled) {
         this.defaultSearchEnabled = defaultSearchEnabled;
     }
 
     /**
      * Retrieves whether search in user home directory is enabled or not.
      * @return True if search is enabled in user home directory
      */
     protected boolean isUserHomeSearchEnabled() {
         return userHomeSearchEnabled;
     }
 
     /**
      * Sets whether search in user home directory is enabled or not
      * @param userHomeSearchEnabled True if search is enabled for user home dir
      */
     public void setUserHomeSearchEnabled(boolean userHomeSearchEnabled) {
         this.userHomeSearchEnabled = userHomeSearchEnabled;
     }
 
     /**
      * Get configured custom search locations
      * @return Custom search locations
      */
     protected String[] getSearchLocations() {
         return searchLocations;
     }
 
     /**
      * The custom search location for the current configurer.
      * @param searchLocation The custom search location
      */
     public void setSearchLocation(String searchLocation) {
         if (StringUtils.isNotEmpty(searchLocation)) {
             setSearchLocations(new String[]{searchLocation});
         }
     }
 
     /**
      * The custom search locations as comma separated values (csv). It will
      * primarily split the search locations by ',' and its intended use case is
      * to inject the search locations via another properties configurer.
      * @param searchLocationAsCsv The search locations as CSV
      */
     public void setSearchLocationsAsCsv(String searchLocationAsCsv) {

         if (StringUtils.isNotEmpty(searchLocationAsCsv)) {
             setSearchLocations(searchLocationAsCsv.split(","));
         }
     }
 
     /**
      * The custom search locations intended to be mainly used via application
      * context XML.
      * @param searchLocations The search locations
      */
     public void setSearchLocations(String[] searchLocations) {
         this.searchLocations = searchLocations;
     }

    /**
     * Set whether to ignore if resource is not found
     * @param ignoreResourceNotFound Flag to note whether to ignore missing rsrc
     */
    @Override
    public void setIgnoreResourceNotFound(boolean ignoreResourceNotFound) {
        this.ignoreResourceNotFound = ignoreResourceNotFound;
        super.setIgnoreResourceNotFound(ignoreResourceNotFound);
    }
 }
