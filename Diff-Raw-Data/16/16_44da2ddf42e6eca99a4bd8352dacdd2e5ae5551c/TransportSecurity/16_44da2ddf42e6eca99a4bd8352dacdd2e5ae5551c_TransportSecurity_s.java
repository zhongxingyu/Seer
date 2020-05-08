 package edu.umn.msi.tropix.storage.service.security;
 
 import java.io.File;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.util.StringUtils;
 
 import edu.umn.msi.tropix.common.io.FileUtils;
 import edu.umn.msi.tropix.common.io.FileUtilsFactory;
 
 public class TransportSecurity {
   private static final Log LOG = LogFactory.getLog(TransportSecurity.class);
   private static final FileUtils FILE_UTILS = FileUtilsFactory.getInstance();
 
   private static final String DEFAULT_SERVICE_KEYSTORE_FILE_NAME = "service.jks";
   private static final String DEFAULT_CLIENT_KEYSTORE_FILE_NAME = "client.jks";
   private static final String DEFAULT_TRUSTSTORE_FILE_NAME = "truststore.jks";
 
   private static final String TEST_CLIENT_KEY_MANAGER = "wibble.jks";
   private static final String TEST_SERVER_KEY_MANAGER = "cherry.jks";
   private static final String TEST_TRUSTSTORE = "truststore.jks";
   
   public static String getServiceKeyManager(final String inputTruststoreFile, final String configDirectory) {
     return getKeyStorePath(inputTruststoreFile, configDirectory, DEFAULT_SERVICE_KEYSTORE_FILE_NAME, TEST_SERVER_KEY_MANAGER);
   }
   
   public static String getClientKeyManager(final String inputTruststoreFile, final String configDirectory) {
     return getKeyStorePath(inputTruststoreFile, configDirectory, DEFAULT_CLIENT_KEYSTORE_FILE_NAME, TEST_CLIENT_KEY_MANAGER);    
   }
 
   public static String getTrustManager(final String truststoreFile, final String configDirectory) {
     return getKeyStorePath(truststoreFile, configDirectory, DEFAULT_TRUSTSTORE_FILE_NAME, TEST_TRUSTSTORE);
   }
   
   private static String getKeyStorePath(final String inputTruststoreFile, 
                                         final String configDirectory, 
                                         final String defaultFileName,
                                         final String testKeyStore) {
     final String configuredKeyStoreFile = getConfigurePath(inputTruststoreFile, configDirectory, defaultFileName);
     final String keyStoreFile;
     if(new File(configuredKeyStoreFile).exists()) {
       keyStoreFile = configuredKeyStoreFile;
     } else {
       keyStoreFile = writeTestKeyStore(configuredKeyStoreFile, testKeyStore);
     }
     return keyStoreFile;
   }
   
   private static String writeTestKeyStore(final String truststoreFile, final String testKeyStore) {
     final String logMessage = "TransportSecurity: Key file " + truststoreFile + " does not exist, using test certificates - this is not secure.";
     System.out.println(logMessage);
     LOG.warn(logMessage);
     final File keyFile = FILE_UTILS.createTempFile("tmpkeystore", ".jks");
     FILE_UTILS.writeStreamToFile(keyFile, TransportSecurity.class.getResourceAsStream(testKeyStore));
     return keyFile.getAbsolutePath();
   }
 
   private static String getConfigurePath(final String inputTruststoreFile, final String configDirectory, final String defaultFileName) {
     String truststoreFile;
    if(!StringUtils.hasText(inputTruststoreFile)) {
       truststoreFile = inputTruststoreFile;
     } else {
       truststoreFile = new File(configDirectory, defaultFileName).getAbsolutePath();
     }
     return truststoreFile;
   }
 
 }
