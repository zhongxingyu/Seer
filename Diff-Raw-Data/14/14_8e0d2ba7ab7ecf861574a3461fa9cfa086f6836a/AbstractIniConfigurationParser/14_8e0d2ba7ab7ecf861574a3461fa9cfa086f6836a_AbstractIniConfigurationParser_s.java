 /*
  * Copyright 2008 EGEE Collaboration
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.glite.authz.common.config;
 
 import java.io.IOException;
 import java.util.StringTokenizer;
 
 import javax.net.ssl.KeyManager;
 import javax.net.ssl.X509KeyManager;
 
 import net.jcip.annotations.ThreadSafe;
 
 import org.glite.authz.common.obligation.AbstractObligationHandler;
 import org.glite.authz.common.obligation.IniOHConfigurationParser;
 import org.glite.authz.common.pip.IniPIPConfigurationParser;
 import org.glite.authz.common.pip.PolicyInformationPoint;
 import org.glite.authz.common.util.Files;
 import org.glite.authz.common.util.Strings;
 import org.glite.security.trustmanager.ContextWrapper;
 import org.glite.security.trustmanager.UpdatingKeyManager;
 import org.glite.security.util.CaseInsensitiveProperties;
 import org.glite.voms.PKIStore;
 import org.ini4j.Ini;
 import org.ini4j.Ini.Section;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Base class for configuration parsers that employ an INI file.
  * 
  * @param <ConfigurationType> the type of configuration produced by this parser
  */
 @ThreadSafe
 public abstract class AbstractIniConfigurationParser<ConfigurationType extends AbstractConfiguration> implements
         ConfigurationParser<ConfigurationType> {
 
     /** The name of the {@value} INI header which contains the property for configuring credential/trust information. */
     public static final String SECURITY_SECTION_HEADER = "SECURITY";
 
     /** The name of the {@value} which gives the path to the service's private key. */
     public static final String SERVICE_KEY_PROP = "servicePrivateKey";
 
     /** The name of the {@value} which gives the path to the service's certificate. */
     public static final String SERVICE_CERT_PROP = "serviceCertificate";
 
     /** The name of the {@value} which gives the path to directory of PEM-encoded trusted X.509 certificates. */
     public static final String TRUST_INFO_DIR_PROP = "trustInfoDir";
 
     /** The name of the {@value} which gives the refresh period, in minutes, for the trust information. */
     public static final String TRUST_INFO_REFRSH_PROP = "trustInfoRefresh";
 
     /** The name of the {@value} which gives the maximum number of simultaneous requests. */
     public static final String MAX_REQUESTS_PROP = "maximumRequests";
 
     /** The name of the {@value} which gives the connection timeout, in seconds. */
     public static final String CONN_TIMEOUT_PROP = "connectionTimeout";
 
     /** The name of the {@value} which gives the size of the receiving message buffer, in bytes. */
     public static final String REC_BUFF_SIZE_PROP = "receiveBufferSize";
 
     /** The name of the {@value} which gives the sending message buffer, in bytes. */
     public static final String SEND_BUFF_SIZE_PROP = "sendBufferSize";
 
     /** The name of the {@value} which gives the space-delimited lists of to-be-configured PIPs. */
     public static final String PIP_PROP = "pips";
 
     /** The name of the {@value} which gives the space-delimited lists of to-be-configured obligation handlers. */
     public static final String OH_PROP = "obligationHandlers";
 
     /** Default value of the {@value #TRUST_INFO_REFRSH_PROP} property, {@value} . */
     public static final int DEFAULT_TRUST_INFO_REFRESH = 60;
 
     /** Default value of the {@value #MAX_REQUESTS_PROP} property, {@value} . */
     public static final int DEFAULT_MAX_REQS = 50;
 
     /** Default value of the {@value #CONN_TIMEOUT_PROP} property, {@value} seconds. */
     public static final int DEFAULT_CONN_TIMEOUT = 30;
 
     /** Default value of the {@value #REC_BUFF_SIZE_PROP} property, {@value} kilobytes. */
     public static final int DEFAULT_REC_BUFF_SIZE = 4096;
 
     /** Default value of the {@value #SEND_BUFF_SIZE_PROP} property, {@value} kilobytes. */
     public static final int DEFAULT_SEND_BUFF_SIZE = 4096;
 
     /** Class logger. */
     private final Logger log = LoggerFactory.getLogger(AbstractIniConfigurationParser.class);
 
     /**
      * Processes each individual Obligation Handler configuration section.
      * 
      * @param ohConfig the obligation handler configuration section
      * @param configBuilder configuration builder currently being populated
      * 
      * @return the obligation handler configured with the information provided in the configuration section
      * 
      * @throws ConfigurationException throw if a obligation handler can not be instantiated
      */
     @SuppressWarnings("unchecked")
    private AbstractObligationHandler buildObligationHandler(Section ohConfig, AbstractConfigurationBuilder<?> configBuilder) throws ConfigurationException {
         String parserClassName = IniConfigUtil.getString(ohConfig, IniOHConfigurationParser.PARSER_CLASS_PROP);
         if (parserClassName == null) {
             String errorMsg = "Obligation configuration section " + ohConfig.getName() + " does not contain a valid "
                     + IniOHConfigurationParser.PARSER_CLASS_PROP + " configuration property.";
             log.error(errorMsg);
             throw new ConfigurationException(errorMsg);
         }
 
         try {
             Class<IniOHConfigurationParser> parserClass = (Class<IniOHConfigurationParser>) AbstractIniServiceConfigurationParser.class
                     .getClassLoader().loadClass(parserClassName);
             IniOHConfigurationParser parser = parserClass.getConstructor().newInstance();
             return parser.parse(ohConfig, configBuilder);
         } catch (Exception e) {
            throw new ConfigurationException("Unable to configure Obligation Handler " + ohConfig.getName(), e);
         }
     }
 
     /**
      * Processes each individual PIP configuration section.
      * 
      * @param pipConfig the PIP configuration section
      * @param configBuilder configuration builder currently being populated
      * 
      * @return the PIP configured with the information provided in the configuration section
      * 
      * @throws ConfigurationException throw if a PIP can not be instantiated
      */
     @SuppressWarnings("unchecked")
     private PolicyInformationPoint buildPolicyInformationPoint(Section pipConfig,
             AbstractConfigurationBuilder<?> configBuilder) throws ConfigurationException {
         String parserClassName = IniConfigUtil.getString(pipConfig, IniPIPConfigurationParser.PARSER_CLASS_PROP);
         if (parserClassName == null) {
             String errorMsg = "PIP configuration section " + pipConfig.getName() + " does not contain a valid "
                     + IniPIPConfigurationParser.PARSER_CLASS_PROP + " configuration property.";
             log.error(errorMsg);
             throw new ConfigurationException(errorMsg);
         }
 
         try {
             log.info("Loading PIP {}", pipConfig.getName());
             log.debug("Creating INI PIP parser class {}", parserClassName);
             Class<IniPIPConfigurationParser> parserClass = (Class<IniPIPConfigurationParser>) AbstractIniConfigurationParser.class
                     .getClassLoader().loadClass(parserClassName);
             IniPIPConfigurationParser parser = parserClass.getConstructor().newInstance();
             return parser.parse(pipConfig, configBuilder);
         } catch (Exception e) {
            throw new ConfigurationException("Unable to configure PIP " + pipConfig.getName(), e);
         }
     }
 
     /**
      * Gets the value of the {@value #CONN_TIMEOUT_PROP} property from the configuration section. If the property is not
      * present or is not valid the default value of {@value #DEFAULT_CONN_TIMEOUT} will be used.
      * 
      * @param configSection configuration section from which to extract the value
      * 
      * @return the value
      */
     protected int getConnectionTimeout(Section configSection) {
         int timeout = IniConfigUtil
                 .getInt(configSection, CONN_TIMEOUT_PROP, DEFAULT_CONN_TIMEOUT, 1, Integer.MAX_VALUE);
         return timeout * 1000;
     }
 
     /**
      * Gets the value of the {@value #MAX_REQUESTS_PROP} property from the configuration section. If the property is not
      * present or is not valid the default value of {@value #DEFAULT_MAX_REQS} will be used.
      * 
      * @param configSection configuration section from which to extract the value
      * 
      * @return the value
      */
     protected int getMaximumRequests(Section configSection) {
         return IniConfigUtil.getInt(configSection, MAX_REQUESTS_PROP, DEFAULT_MAX_REQS, 1, Integer.MAX_VALUE);
     }
 
     /**
      * Gets the value of the {@value #REC_BUFF_SIZE_PROP} property from the configuration section. If the property is
      * not present or is not valid the default value of {@value #DEFAULT_REC_BUFF_SIZE} will be used.
      * 
      * @param configSection configuration section from which to extract the value
      * 
      * @return the value
      */
     protected int getReceiveBufferSize(Section configSection) {
         return IniConfigUtil.getInt(configSection, REC_BUFF_SIZE_PROP, DEFAULT_REC_BUFF_SIZE, 1, Integer.MAX_VALUE);
     }
 
     /**
      * Gets the value of the {@value #SEND_BUFF_SIZE_PROP} property from the configuration section. If the property is
      * not present or is not valid the default value of {@value #DEFAULT_SEND_BUFF_SIZE} will be used.
      * 
      * @param configSection configuration section from which to extract the value
      * 
      * @return the value
      */
     protected int getSendBufferSize(Section configSection) {
         return IniConfigUtil.getInt(configSection, SEND_BUFF_SIZE_PROP, DEFAULT_SEND_BUFF_SIZE, 1, Integer.MAX_VALUE);
     }
 
     /**
      * Gets the value of the {@value #TRUST_INFO_REFRSH_PROP} property from the configuration section. If the property
      * is not present or is not valid the default value of {@value #DEFAULT_TRUST_INFO_REFRESH} will be used.
      * 
      * @param configSection configuration section from which to extract the value
      * 
      * @return the value
      */
     protected int getTrustMaterialRefreshInterval(Section configSection) {
         return IniConfigUtil.getInt(configSection, TRUST_INFO_REFRSH_PROP, DEFAULT_TRUST_INFO_REFRESH, 1,
                 Integer.MAX_VALUE);
     }
 
     /**
      * Creates a {@link KeyManager} from the {@value #SERVICE_KEY_PROP} and {@value #SERVICE_CERT_PROP} properties, if
      * they exist.
      * 
      * @param configSection current configuration section being processed
      * @param configBuilder current builder being constructed from the parser
      * 
      * @return the constructed key manager, or null if the required properties do not exist
      * 
      * @throws ConfigurationException thrown if there is a problem creating the key manager
      */
     protected X509KeyManager getX509KeyManager(Section configSection) throws ConfigurationException {
         if (configSection == null) {
             return null;
         }
 
         String privateKeyFilePath = IniConfigUtil.getString(configSection, SERVICE_KEY_PROP, null);
         if (privateKeyFilePath == null) {
             log.info("No service private key file provided, no service credential will be used.");
             return null;
         }
 
         String certificateFilePath = IniConfigUtil.getString(configSection, SERVICE_CERT_PROP, null);
         if (certificateFilePath == null) {
             log.info("No service certificate file provided, no service credential will be used.");
             return null;
         }
 
         log.info("Service credential will use private key {} and certificate {}", privateKeyFilePath,
                 certificateFilePath);
         CaseInsensitiveProperties keystoreProps = new CaseInsensitiveProperties();
         keystoreProps.setProperty(ContextWrapper.CREDENTIALS_KEY_FILE, privateKeyFilePath);
         keystoreProps.setProperty(ContextWrapper.CREDENTIALS_CERT_FILE, certificateFilePath);
 
         try {
             return new UpdatingKeyManager(keystoreProps, null);
         } catch (Exception e) {
             log.error("Unable to create service key manager", e);
             throw new ConfigurationException("Unable to read service credential information", e);
         }
     }
 
     /**
      * Creates a {@link PKIStore} from the {@value #TRUST_INFO_DIR_PROP} and {@value #CRLS_REQUIRED_PROP} properties, if
      * they exist. This store holds the material used to validate X.509 certificates.
      * 
      * @param configSection current configuration section being processed
      * @param configBuilder current builder being constructed from the parser
      * 
      * @return the constructed trust material store, or null if the required attribute did not exist
      * 
      * @throws ConfigurationException thrown if there is a problem creating the trust manager
      */
     protected PKIStore getX509TrustMaterialStore(Section configSection) throws ConfigurationException {
         if (configSection == null) {
             return null;
         }
 
         String trustStoreDir = IniConfigUtil.getString(configSection, TRUST_INFO_DIR_PROP, null);
         if (trustStoreDir == null) {
             log.info("No truststore directory given, no trust manager will be used");
             return null;
         }
 
         try {
             Files.getFile(trustStoreDir, false, true, true, false);
         } catch (IOException e) {
             log.error("Unable to read truststore directory " + trustStoreDir, e);
             throw new ConfigurationException(e.getMessage());
         }
         log.info("X.509 trusted information directory: {}", trustStoreDir);
        
         int refreshInterval = getTrustMaterialRefreshInterval(configSection) * 60 * 1000;
         log.info("trust information refresh interval: {}ms", refreshInterval);
 
         try {
             PKIStore trustMaterial = new PKIStore(trustStoreDir, PKIStore.TYPE_CADIR);
             trustMaterial.rescheduleRefresh(refreshInterval);
             return trustMaterial;
         } catch (Exception e) {
             log.error("Unable to create X.509 trust material store", e);
             throw new ConfigurationException("Unable to create X.509 trust material store", e);
         }
     }
 
     /**
      * Processing the {@value #OH_PROP} configuration property, if there is one.
      * 
      * @param iniFile INI configuration file being processed
      * @param configSection current configuration section being processed
      * @param configBuilder current builder being constructed from the parser
      * 
      * @throws ConfigurationException thrown if there is a problem building the obligations handlers
      */
     protected void processObligationHandlers(Ini iniFile, Section configSection,
             AbstractConfigurationBuilder<?> configBuilder) throws ConfigurationException {
         if (configSection.containsKey(OH_PROP)) {
             StringTokenizer obligationHandlers = new StringTokenizer(configSection.get(OH_PROP), " ");
             String obligationHandlerName;
             while (obligationHandlers.hasMoreTokens()) {
                 obligationHandlerName = Strings.safeTrimOrNullString(obligationHandlers.nextToken());
                 if (!iniFile.containsKey(obligationHandlerName)) {
                     String errorMsg = "INI configuration file does not contain a configuration section for obligation handler "
                             + obligationHandlerName;
                     log.error(errorMsg);
                     throw new ConfigurationException(errorMsg);
                 }
                 if (obligationHandlerName != null) {
                     configBuilder.getObligationService().addObligationhandler(
                             buildObligationHandler(iniFile.get(obligationHandlerName), configBuilder));
                     log.info("Added obligation handler: {}", obligationHandlerName);
                 }
             }
         }
     }
 
     /**
      * Processing the {@value #PIP_PROP} configuration property, if there is one.
      * 
      * @param iniFile INI configuration file being processed
      * @param configSection current configuration section being processed
      * @param configBuilder current builder being constructed from the parser
      * 
      * @throws ConfigurationException thrown if there is a problem building the policy information points
      */
     protected void processPolicyInformationPoints(Ini iniFile, Section configSection,
             AbstractConfigurationBuilder<?> configBuilder) throws ConfigurationException {
         if (configSection.containsKey(PIP_PROP)) {
             String pipName;
             StringTokenizer pipNames = new StringTokenizer(configSection.get(PIP_PROP), " ");
             while (pipNames.hasMoreTokens()) {
                 pipName = Strings.safeTrimOrNullString(pipNames.nextToken());
                 if (pipName != null) {
                     if (!iniFile.containsKey(pipName)) {
                         String errorMsg = "INI configuration file does not contain a configuration section for policy information point "
                                 + pipName;
                         log.error(errorMsg);
                         throw new ConfigurationException(errorMsg);
                     }
                     configBuilder.getPIPs().add(buildPolicyInformationPoint(iniFile.get(pipName), configBuilder));
                     log.debug("loadded policy information point: {}", pipName);
                 }
             }
         }
     }
 }
