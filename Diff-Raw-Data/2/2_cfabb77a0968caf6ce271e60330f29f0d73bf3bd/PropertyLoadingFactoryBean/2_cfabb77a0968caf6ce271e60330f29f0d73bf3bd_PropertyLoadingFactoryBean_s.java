 #set( $symbol_pound = '#' )
 #set( $symbol_dollar = '$' )
 #set( $symbol_escape = '\' )
 /*
  * Copyright 2007 The Kuali Foundation
  * 
  * Licensed under the Educational Community License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.opensource.org/licenses/ecl2.php
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package ${package}.sys.context;
 
 import sun.misc.BASE64Decoder;
 import sun.misc.BASE64Encoder;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 
 import java.security.KeyFactory;
 import java.security.KeyPair;
 import java.security.KeyStore;
 import java.security.PrivateKey;
 import java.security.PublicKey;
 import java.security.Security;
 import java.security.cert.Certificate;
 import java.security.cert.CertificateFactory;
 import java.security.spec.PKCS8EncodedKeySpec;
 import java.security.spec.X509EncodedKeySpec;
 
 import javax.security.auth.x500.X500Principal;
 import org.bouncycastle.jce.provider.BouncyCastleProvider;
 import org.bouncycastle.openssl.PEMReader;
 import org.bouncycastle.openssl.PEMWriter;
 import org.bouncycastle.x509.X509V1CertificateGenerator;
 import org.bouncycastle.x509.X509V3CertificateGenerator;
 
 import javax.crypto.Cipher;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.apache.commons.lang.StringUtils;
 import org.kuali.kfs.sys.KFSConstants;
 import org.kuali.rice.core.config.JAXBConfigImpl;
 import org.kuali.rice.core.util.ClassLoaderUtils;
 import org.springframework.beans.factory.FactoryBean;
 import org.springframework.core.io.DefaultResourceLoader;
 
 import static ${package}.logging.FormattedLogger.*;
 
 
 public class PropertyLoadingFactoryBean implements FactoryBean {
     private static final String PROPERTY_FILE_NAMES_KEY         = "property.files";
     private static final String PROPERTY_TEST_FILE_NAMES_KEY    = "property.test.files";
     private static final String SECURITY_PROPERTY_FILE_NAME_KEY = "security.property.file";
     private static final String CONFIGURATION_FILE_NAME         = "configuration";
     private static final Properties BASE_PROPERTIES             = new Properties();
     private static final String HTTP_URL_PROPERTY_NAME          = "http.url";
     private static final String KSB_REMOTING_URL_PROPERTY_NAME  = "ksb.remoting.url";
     private static final String REMOTING_URL_SUFFIX             = "/remoting";
     protected static final String ENCRYPTION_STRATEGY           = "RSA/ECB/PKCS1Padding";
     protected static final String KEYSTORE_TYPE                 = "JCEKS";
     protected static final String KEYSTORE_PASSWORD_PROPERTY    = "keystore.password";
     protected static final String KEYSTORE_LOCATION_PROPERTY    = "keystore.file";
     protected static final String ENCRYPTED_PROPERTY_EXTENSION  = ".encrypted";
     protected static final String PASSWORD_PROPERTY_EXTENSION   = ".password";
     protected static final String RICE_RSA_KEY_NAME             = "rice-rsa-key";
     
     static  {
         Security.addProvider(new BouncyCastleProvider());
     }
     
     private Properties props = new Properties();
     private boolean testMode;
     private boolean secureMode;
 
     /**
      * Main entry method.
      */
     public Object getObject() throws Exception {
         loadBaseProperties();
         props.putAll(BASE_PROPERTIES);
         if (secureMode) {
             loadPropertyList(props,SECURITY_PROPERTY_FILE_NAME_KEY);
         } else {
             loadPropertyList(props,PROPERTY_FILE_NAMES_KEY);
             if (testMode) {
                 loadPropertyList(props,PROPERTY_TEST_FILE_NAMES_KEY);
             }            
         }
         if (StringUtils.isBlank(System.getProperty(HTTP_URL_PROPERTY_NAME))) {
             props.put(KSB_REMOTING_URL_PROPERTY_NAME, props.getProperty(KFSConstants.APPLICATION_URL_KEY) + REMOTING_URL_SUFFIX);
         }
         else {
             props.put(KSB_REMOTING_URL_PROPERTY_NAME, new StringBuffer("http://").append(System.getProperty(HTTP_URL_PROPERTY_NAME)).append("/kfs-").append(props.getProperty(KFSConstants.ENVIRONMENT_KEY)).append(REMOTING_URL_SUFFIX).toString());
         }
         config("%s set to %s", KSB_REMOTING_URL_PROPERTY_NAME, props.getProperty(KSB_REMOTING_URL_PROPERTY_NAME));
 
         decryptProps(props);
 
         return props;
     }
 
     /**
      * Decrypts encrypted values in properties. Interprets that any property in the {@link Properties} instance
      * provided with a key ending with the {@code ENCRYPTED_PROPERTY_EXTENSION} is considered to be encrypted.
      * It is then decrypted and replaced with a key of the same name only using the {@code PASSWORD_PROPERTY_EXTENSION}
      * 
      * @param props the {@link Properties} to decrypt
      * @throws {@link Exception} if there's any problem decrypting/encrypting properties.
      */
     protected void decryptProps(final Properties props) throws Exception {
         final String keystore  = props.getProperty(KEYSTORE_LOCATION_PROPERTY);
         final String storepass = props.getProperty(KEYSTORE_PASSWORD_PROPERTY);
         final FileInputStream fs = new FileInputStream(keystore);
         final KeyStore jks = KeyStore.getInstance(KEYSTORE_TYPE);
         jks.load(fs, storepass.toCharArray());                
         fs.close();
 
         
         final Cipher cipher = Cipher.getInstance(ENCRYPTION_STRATEGY);
         cipher.init(Cipher.DECRYPT_MODE, (PrivateKey) jks.getKey(RICE_RSA_KEY_NAME, storepass.toCharArray()));
 
         for (final String key : props.stringPropertyNames()) {
             if (key.endsWith(ENCRYPTED_PROPERTY_EXTENSION)) {
                 final String prefix = key.substring(0, key.indexOf(ENCRYPTED_PROPERTY_EXTENSION));
                 final String encrypted_str = props.getProperty(key);
                 props.setProperty(prefix + PASSWORD_PROPERTY_EXTENSION,
                                   new String(cipher.doFinal(new BASE64Decoder().decodeBuffer(encrypted_str))));
             }
         }
         
     }
 
     public Class getObjectType() {
         return Properties.class;
     }
 
     public boolean isSingleton() {
         return true;
     }
 
     private static void loadPropertyList(Properties props, String listPropertyName) {
         entering();
         debug("Loading property %s", listPropertyName);
         for (String propertyFileName : getBaseListProperty(listPropertyName)) {
             loadProperties(props,propertyFileName);
         }
         exiting();
     }
 
     private static void loadProperties(Properties props, String propertyFileName) {
         entering();
         debug("Loading %s", propertyFileName);
         InputStream propertyFileInputStream = null;
         try {
             try {
                 propertyFileInputStream = new DefaultResourceLoader(ClassLoaderUtils.getDefaultClassLoader()).getResource(propertyFileName).getInputStream();
                 props.load(propertyFileInputStream);
             }
             finally {
                 if (propertyFileInputStream != null) {
                     propertyFileInputStream.close();
                 }
             }
         }
         catch (FileNotFoundException fnfe) {
             try {
                 try {
                     propertyFileInputStream = new FileInputStream(propertyFileName);
                     props.load(propertyFileInputStream);
                 }
                 finally {
                     if (propertyFileInputStream != null) {
                         propertyFileInputStream.close();
                     }
                 }
             }
             catch (Exception e) {
                 warn("Could not load property file %s", propertyFileName);
                 throwing(e);
             }                
 
         }
         catch (IOException e) {
             warn("PropertyLoadingFactoryBean unable to load property file: %s", propertyFileName);
         }
         finally {
             exiting();
         }
     }
 
     public static String getBaseProperty(String propertyName) {
         loadBaseProperties();
         return BASE_PROPERTIES.getProperty(propertyName);
     }
 
     protected static List<String> getBaseListProperty(String propertyName) {
         loadBaseProperties();
         try {
             if (BASE_PROPERTIES == null) {
                 error("BASE PROPERTIES IS NULL!!");
             }
             debug("Returning list of %s", BASE_PROPERTIES.getProperty(propertyName));
             return Arrays.asList(BASE_PROPERTIES.getProperty(propertyName).split(","));
         }
         catch (Exception e) {
             // NPE loading properties
             return new ArrayList<String>();
         }
     }
 
     protected static void loadBaseProperties() {
         if (BASE_PROPERTIES.isEmpty()) {
             List<String> riceXmlConfigurations = new ArrayList<String>();
             riceXmlConfigurations.add("classpath:META-INF/common-config-defaults.xml");
             JAXBConfigImpl riceXmlConfigurer = new JAXBConfigImpl(riceXmlConfigurations);
             try {
                 riceXmlConfigurer.parseConfig();
                 BASE_PROPERTIES.putAll(riceXmlConfigurer.getProperties());
                 BASE_PROPERTIES.list(System.out);
             }
             catch (Exception e) {
                 warn("Couldn't load the rice configs");
                 warn(e.getMessage());
             }
         }
 
         loadProperties(BASE_PROPERTIES, new StringBuffer("classpath:").append(CONFIGURATION_FILE_NAME).append(".properties").toString());
 
         final String additionalProps = BASE_PROPERTIES.getProperty("additional.config.locations");
         config("Adding props from %s", additionalProps);
 
         final JAXBConfigImpl additionalConfigurer = new JAXBConfigImpl(java.util.Arrays.asList(additionalProps.split(",")));
         try {
             additionalConfigurer.parseConfig();
             BASE_PROPERTIES.putAll(additionalConfigurer.getProperties());
         }
         catch (Exception e) {
             warn("Unable to load additional configs");
            warn("e.getMessage());
             // e.printStackTrace();
         }
     }
 
     public void setTestMode(boolean testMode) {
         this.testMode = testMode;
     }
 
     public void setSecureMode(boolean secureMode) {
         this.secureMode = secureMode;
     }
     
     public static void clear() {
         BASE_PROPERTIES.clear();
     }
 }
