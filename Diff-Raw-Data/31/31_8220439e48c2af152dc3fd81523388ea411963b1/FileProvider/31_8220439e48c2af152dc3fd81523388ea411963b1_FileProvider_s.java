 /*
  * CDDL HEADER START
  *
  * The contents of this file are subject to the terms of the Common Development and Distribution License, Version 1.0
  * only (the "License"). You may not use this file except in compliance with the License.
  *
  * You can obtain a copy of the license at license/ESCIDOC.LICENSE or http://www.escidoc.de/license. See the License for
  * the specific language governing permissions and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL HEADER in each file and include the License file at
  * license/ESCIDOC.LICENSE. If applicable, add the following below this CDDL HEADER, with the fields enclosed by
  * brackets "[]" replaced with your own identifying information: Portions Copyright [yyyy] [name of copyright owner]
  *
  * CDDL HEADER END
  *
  * Copyright 2006-2011 Fachinformationszentrum Karlsruhe Gesellschaft fuer wissenschaftlich-technische Information mbH
  * and Max-Planck-Gesellschaft zur Foerderung der Wissenschaft e.V. All rights reserved. Use is subject to license
  * terms.
  */
 
 package de.escidoc.core.common.axis;
 
 import de.escidoc.core.common.util.IOUtils;
 import de.escidoc.core.common.util.configuration.EscidocConfiguration;
 import org.slf4j.Logger; import org.slf4j.LoggerFactory;
 import de.escidoc.core.common.util.xml.XmlUtility;
 import org.apache.axis.AxisEngine;
 import org.apache.axis.ConfigurationException;
 import org.apache.axis.Handler;
 import org.apache.axis.WSDDEngineConfiguration;
 import org.apache.axis.deployment.wsdd.WSDDDeployment;
 import org.apache.axis.deployment.wsdd.WSDDDocument;
 import org.apache.axis.deployment.wsdd.WSDDGlobalConfiguration;
 import org.apache.axis.encoding.TypeMappingRegistry;
 import org.apache.axis.handlers.soap.SOAPService;
 import org.apache.axis.utils.Admin;
 import org.apache.axis.utils.ClassUtils;
 import org.apache.axis.utils.Messages;
 import org.apache.axis.utils.XMLUtils;
 import org.w3c.dom.Document;
 
 import javax.xml.namespace.QName;
 import java.io.BufferedWriter;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.io.Writer;
 import java.nio.charset.Charset;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * A simple ConfigurationProvider that uses the Admin class to read + write XML
  * files.
  * 
  * @author Michael Hoppe
  */
 public class FileProvider implements WSDDEngineConfiguration {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(FileProvider.class);
 
     private WSDDDeployment deployment;
 
     private File configFile;
 
     private InputStream myInputStream;
 
     private boolean readOnly = true;
 
     // Should we search the classpath for the file if we don't find it in
     // the specified location?
     private boolean searchClasspath = true;
 
     private static String defaultJNDIUrl;
 
     /**
      * Constructor which accesses a file in the current directory of the engine
      * or at an absolute path.
      * 
      * @param filename
      *            name of the config-file
      */
     public FileProvider(final String filename) {
         this.configFile = new File(filename);
         if (defaultJNDIUrl == null) {
             try {
                 defaultJNDIUrl =
                     EscidocConfiguration.getInstance().get(
                         EscidocConfiguration.ESCIDOC_CORE_DEFAULT_JNDI_URL);
             }
             catch (final IOException e) {
                 throw new RuntimeException(
                     "Default jndi url not found in properties! No value for key '"
                         + EscidocConfiguration.ESCIDOC_CORE_DEFAULT_JNDI_URL
                         + "'.", e);
             }
         }
         check();
     }
 
     /**
      * Constructor which accesses a file relative to a specific base path.
      * 
      * @param basepath
      *            base-path of the file
      * @param filename
      *            name of the config-file
      * @throws ConfigurationException
      *             e
      */
     public FileProvider(final String basepath, final String filename)
         throws ConfigurationException {
 
         final File dir = new File(basepath);
 
         /*
          * If the basepath is not a readable directory, throw an internal
          * exception to make it easier to debug setup problems.
          */
         if (!dir.exists() || !dir.isDirectory() || !dir.canRead()) {
             throw new ConfigurationException(Messages.getMessage(
                 "invalidConfigFilePath", basepath));
         }
 
         this.configFile = new File(basepath, filename);
         check();
     }
 
     /**
      * Check the configuration file attributes and remember whether or not the
      * file is read-only.
      */
     private void check() {
         try {
             this.readOnly = !configFile.canWrite();
         }
         catch (final SecurityException se) {
             if(LOGGER.isWarnEnabled()) {
                 LOGGER.warn("Error on checking the configuration file.");
             }
             if(LOGGER.isDebugEnabled()) {
                 LOGGER.debug("Error on checking the configuration file.", se);
             }
             this.readOnly = true;
         }
 
         /*
          * If file is read-only, log informational message as configuration
          * changes will not persist.
          */
         if (this.readOnly) {
             LOGGER.info(Messages.getMessage("readOnlyConfigFile"));
         }
     }
 
     /**
      * Constructor which takes an input stream directly. Note: The configuration
      * will be read-only in this case!
      * 
      * @param is
      *            the input stream
      */
     public FileProvider(final InputStream is) {
         setInputStream(is);
     }
 
     /**
      * sets the InputStream.
      * 
      * @param is
      *            the input stream
      */
     public final void setInputStream(final InputStream is) {
 
         this.myInputStream = is;
         if (is != null) {
             this.myInputStream = replaceVariables(this.myInputStream);
         }
     }
 
     /**
      * returns the InputStream.
      * 
      * @return InputStream input stream
      */
     private InputStream getInputStream() {
         return this.myInputStream;
     }
 
     /**
      * returns the deployment.
      * 
      * @return WSDDDeployment the deployment
      */
     @Override
     public WSDDDeployment getDeployment() {
         return this.deployment;
     }
 
     /**
      * sets the deployment.
      * 
      * @param deployment
      *            the deployment
      */
     public void setDeployment(final WSDDDeployment deployment) {
         this.deployment = deployment;
     }
 
     /**
      * Determine whether or not we will look for a "*-config.wsdd" file on the
      * classpath if we don't find it in the specified location.
      * 
      * @param searchClasspath
      *            true if we should search the classpath
      */
     public void setSearchClasspath(final boolean searchClasspath) {
         this.searchClasspath = searchClasspath;
     }
 
     /**
      * Replace parts matching ${...} with parameter from EscidocConfiguration.
      * 
      * @param in
      *            InputStream
      * @return InputStream replaced InputStream
      */
     private InputStream replaceVariables(final InputStream in) {
        String xmlString = null;
         try {
            xmlString = IOUtils.readStringFromStream(in);
             xmlString = insertSystemProperties(xmlString);
         } catch (final IOException e) {
             LOGGER.error("Error on inserting system properties.", e);
         }
         finally {
             IOUtils.closeStream(in);
         }
        return new ByteArrayInputStream(xmlString.getBytes(Charset.forName(XmlUtility.CHARACTER_ENCODING)));
     }
 
     /**
      * Replace parts matching ${...} with parameter from EscidocConfiguration.
      * 
      * @param propertyValue
      *            propertyValue
      * @return String replaced String
      */
     private String insertSystemProperties(final String propertyValue) {
         String result = propertyValue;
         while (result.contains("${")) {
             if (LOGGER.isDebugEnabled()) {
                 LOGGER.debug("propertyValue=" + result);
             }
             result = insertSystemProperty(result);
             if (LOGGER.isDebugEnabled()) {
                 LOGGER.debug("propertyValue=" + result);
             }
         }
         return result;
     }
 
     /**
      * Replace one part matching ${...} with parameter from
      * EscidocConfiguration.
      * 
      * @param propertyValue
      *            propertyValue
      * @return String replaced String
      */
     private static String insertSystemProperty(final String propertyValue) {
         String result = propertyValue;
         final int i = result.indexOf("${");
         if (i > -1) {
             final int j = result.indexOf('}');
             if (j > -1) {
                 final String confProperty = result.substring(i + 2, j);
                 String confPropertyValue = null;
                 try {
                     confPropertyValue =
                         EscidocConfiguration.getInstance().get(confProperty);
                 } catch (final IOException e) {
                     if(LOGGER.isWarnEnabled()) {
                         LOGGER.warn("Error on getting configuration property.");
                     }
                     if(LOGGER.isDebugEnabled()) {
                         LOGGER.debug("Error on getting configuration property.", e);
                     }
                 }
                 if (confPropertyValue == null) {
                     confPropertyValue = defaultJNDIUrl;
                 }
                 result =
                     result.substring(0, i) + confPropertyValue
                         + result.substring(j + 1);
             }
         }
         return result;
     }
 
     /**
      * Configures engine with values from configFile.
      * 
      * @param engine
      *            axisEngine
      * @throws ConfigurationException
      *             e
      */
     @Override
     public void configureEngine(final AxisEngine engine)
         throws ConfigurationException {
         try {
             if (getInputStream() == null) {
                 try {
                     setInputStream(new FileInputStream(this.configFile));
                 } catch (final Exception e) {
                     if(LOGGER.isWarnEnabled()) {
                         LOGGER.warn("Error on creating input stream.");
                     }
                     if(LOGGER.isDebugEnabled()) {
                         LOGGER.debug("Error on creating input stream.", e);
                     }
                     if (this.searchClasspath) {
                         setInputStream(ClassUtils.getResourceAsStream(engine
                             .getClass(), configFile.getName(), true));
                     }
                 }
             }
 
             if (getInputStream() == null) {
                 throw new ConfigurationException(Messages
                     .getMessage("noConfigFile"));
             }
 
             final WSDDDocument doc =
                 new WSDDDocument(XMLUtils.newDocument(getInputStream()));
             this.deployment = doc.getDeployment();
 
             deployment.configureEngine(engine);
             engine.refreshGlobalOptions();
 
             setInputStream(null);
         }
         catch (final Exception e) {
             throw new ConfigurationException(e);
         }
     }
 
     /**
      * Save the engine configuration. In case there's a problem, we write it to
      * a string before saving it out to the actual file so we don't screw up the
      * file.
      * 
      * @param engine
      *            axisEngine
      * @throws ConfigurationException
      *             e
      */
     @Override
     public void writeEngineConfig(final AxisEngine engine)
         throws ConfigurationException {
         if (! this.readOnly) {
             PrintWriter writer = null;
             try {
                 final Document doc = Admin.listConfig(engine);
                 final Writer osWriter = new OutputStreamWriter(
                         new FileOutputStream(this.configFile), XMLUtils.getEncoding());
                 writer = new PrintWriter(new BufferedWriter(osWriter));
                 XMLUtils.DocumentToWriter(doc, writer);
                 writer.println();
             } catch (final Exception e) {
                 throw new ConfigurationException(e);
             } finally {
                 IOUtils.closeWriter(writer);
             }
         }
     }
 
     /**
      * retrieve an instance of the named handler.
      * 
      * @param qname
      *            qname
      * @return Handler
      * @throws ConfigurationException
      *             e
      */
     @Override
     public Handler getHandler(final QName qname) throws ConfigurationException {
         return deployment.getHandler(qname);
     }
 
     /**
      * retrieve an instance of the named service.
      * 
      * @param qname
      *            qname
      * @return SOAPService
      * @throws ConfigurationException
      *             e
      */
     @Override
     public SOAPService getService(final QName qname)
         throws ConfigurationException {
         final SOAPService service = deployment.getService(qname);
         if (service == null) {
             throw new ConfigurationException(Messages.getMessage("noService10",
                 qname.toString()));
         }
         return service;
     }
 
     /**
      * Get a service which has been mapped to a particular namespace.
      * 
      * @param namespace
      *            a namespace URI
      * @return an instance of the appropriate Service, or null
      * @throws ConfigurationException
      *             e
      */
     @Override
     public SOAPService getServiceByNamespaceURI(final String namespace)
         throws ConfigurationException {
         return deployment.getServiceByNamespaceURI(namespace);
     }
 
     /**
      * retrieve an instance of the named transport.
      * 
      * @param qname
      *            qname
      * @return Handler
      * @throws ConfigurationException
      *             e
      */
     @Override
     public Handler getTransport(final QName qname)
         throws ConfigurationException {
         return deployment.getTransport(qname);
     }
 
     /**
      * Returns a TypeMappingRegistry.
      * 
      * @return TypeMappingRegistry
      * @throws ConfigurationException
      *             e
      */
     @Override
     public TypeMappingRegistry getTypeMappingRegistry()
         throws ConfigurationException {
         return deployment.getTypeMappingRegistry();
     }
 
     /**
      * Returns a global request handler.
      * 
      * @return Handler
      * @throws ConfigurationException
      *             e
      */
     @Override
     public Handler getGlobalRequest() throws ConfigurationException {
         return deployment.getGlobalRequest();
     }
 
     /**
      * Returns a global response handler.
      * 
      * @return Handler
      * @throws ConfigurationException
      *             e
      */
     @Override
     public Handler getGlobalResponse() throws ConfigurationException {
         return deployment.getGlobalResponse();
     }
 
     /**
      * Returns the global configuration options.
      * 
      * @return Hashtable
      * @throws ConfigurationException
      *             e
      */
     // Needs to be a HashTable
     // Interface org.apache.axis.EngineConfiguration
     @Override
     public Hashtable getGlobalOptions() throws ConfigurationException {
         final WSDDGlobalConfiguration globalConfig =
             deployment.getGlobalConfiguration();
 
         if (globalConfig != null) {
             return globalConfig.getParametersTable();
         }
 
         return null;
     }
 
     /**
      * Get an enumeration of the services deployed to this engine.
      * 
      * @return Iterator
      * @throws ConfigurationException
      *             e
      */
     @Override
     public Iterator getDeployedServices() throws ConfigurationException {
         return deployment.getDeployedServices();
     }
 
     /**
      * Get a list of roles that this engine plays globally. Services within the
      * engine configuration may also add additional roles.
      * 
      * @return a <code>List</code> of the roles for this engine
      */
     @Override
     public List getRoles() {
         return deployment.getRoles();
     }
 }
