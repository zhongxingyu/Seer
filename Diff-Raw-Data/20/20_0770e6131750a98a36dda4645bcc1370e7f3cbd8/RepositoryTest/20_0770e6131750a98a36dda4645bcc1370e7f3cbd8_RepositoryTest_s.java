 /* SVN Header
    $Id: RepositoryTest.java 86 2007-09-09 22:44:55Z sm00sm $
    $URL: https://source.uhi.ac.uk/svn/ctrep/trunk/testapp/testsrc/fedora/webservices/client/api/RepositoryTest.java $
 */
 
 package fedora.webservices.client.api;
 
 import org.apache.axis2.transport.http.HttpTransportProperties;
 import org.bouncycastle.jce.provider.BouncyCastleProvider;
 import org.guanxi.common.EntityConnection;
 import org.guanxi.common.definitions.Guanxi;
 import org.guanxi.common.security.SecUtils;
 import org.junit.AfterClass;
 import static org.junit.Assert.fail;
 import org.junit.BeforeClass;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.File;
 import java.security.KeyStore;
 import java.security.Provider;
 import java.security.Security;
 import java.security.cert.X509Certificate;
 import java.util.ResourceBundle;
 
 /**
  * This is the base class that all Fedora web services test classes must extend. It provides
  * common functionality
  */
 public abstract class RepositoryTest {
   /** The format and version of ingest messages */
   protected static final String DIGITAL_OBJECT_FORMAT_FOXML = "foxml1.0";
   
   /** Definitions for keys in repository.properties */
   protected static final String PROPS_KEY_TRUSTSTORE_LOCATION = "truststore.location";
   protected static final String PROPS_KEY_TRUSTSTORE_PASSWORD = "truststore.password";
   protected static final String PROPS_KEY_KEYSTORE_LOCATION = "keystore.location";
   protected static final String PROPS_KEY_KEYSTORE_PASSWORD = "keystore.password";
   protected static final String PROPS_KEY_KEYSTORE_CERT_CN = "keystore.self.signed.cert.cn";
   protected static final String PROPS_KEY_KEYSTORE_CERT_ALIAS = "keystore.self.signed.cert.alias";
   protected static final String PROPS_KEY_API_A_ENDPOINT = "fedora.api.a.ws.endpoint";
   protected static final String PROPS_KEY_API_M_ENDPOINT = "fedora.api.m.ws.endpoint";
   protected static final String PROPS_KEY_FEDORA_ADMIN_USERNAME = "fedora.admin.username";
   protected static final String PROPS_KEY_FEDORA_ADMIN_PASSWORD = "fedora.admin.password";
   protected static final String PROPS_KEY_FEDORA_VERSION = "fedora.version";
   
   protected static final String PROPS_KEY_TEST_INGEST_FILE = "test.file";
   protected static final String PROPS_KEY_TEST_INGEST_FILE_PID = "test.file.pid";
   protected static final String PROPS_KEY_TEST_INGEST_FILE_MIME_TYPE = "test.file.mime.type";
   protected static final String PROPS_KEY_TEST_INGEST_FILE_LABEL = "test.file.label";
   protected static final String PROPS_KEY_TEST_INGEST_FILE_CONTENT_MODEL = "test.file.content.model";
   protected static final String PROPS_KEY_TEST_INGEST_FILE_DATASTREAM_ID = "test.file.datastream.id";
   protected static final String PROPS_KEY_TEST_INGEST_FILE_DATASTREAM_VERSION_ID = "test.file.datastream.version.id";
   protected static final String PROPS_KEY_TEST_INGEST_FILE_TITLE = "test.file.title";
   protected static final String PROPS_KEY_TEST_INGEST_FILE_CREATOR = "test.file.creator";
   protected static final String PROPS_KEY_TEST_INGEST_FILE_SUBJECT = "test.file.subject";
   protected static final String PROPS_KEY_TEST_INGEST_FILE_DESCRIPTION = "test.file.description";
   protected static final String PROPS_KEY_TEST_INGEST_FILE_PUBLISHER = "test.file.publisher";
   protected static final String PROPS_KEY_TEST_INGEST_FILE_IDENTIFIER = "test.file.identifier";
 
   protected static final String PROPS_KEY_TEST_RELSEXT_MIME_TYPE = "relsext.mime.type";
   protected static final String PROPS_KEY_TEST_RELSEXT_VERSION_ID = "relsext.version.id";
   protected static final String PROPS_KEY_TEST_RELSEXT_LABEL = "relsext.label";
   protected static final String PROPS_KEY_TEST_RELSEXT_OWNER = "relsext.owner";
 
   protected static final String PROPS_KEY_XAL_WS_FACTORY_CLASS = "ws.factory.class";
 
   protected static final String PROPS_KEY_DUMP_INGEST_XML = "ingest.dump";
   protected static final String PROPS_KEY_DUMP_INGEST_XML_FILE = "ingest.dump.file";
   protected static final String PROPS_KEY_INGEST_LOG_MESSAGE = "ingest.log.message";
   protected static final String PROPS_KEY_PURGE_LOG_MESSAGE = "purge.log.message";
 
   protected static ResourceBundle repositoryProperties = null;
   protected static HttpTransportProperties.Authenticator authenticator = null;
 
   private static boolean okToUnloadBCProvider = false;
 
   @BeforeClass
   public static void init() {
     repositoryProperties = ResourceBundle.getBundle("fedora.webservices.client.api.repository");
 
     // Set up the truststore. We need this to verify the Fedora server's certificate as we are connecting via HTTPS
     //System.setProperty("javax.net.ssl.trustStore", repositoryProperties.getString(PROPS_KEY_TRUSTSTORE_LOCATION));
     //System.setProperty("javax.net.ssl.trustStorePassword", repositoryProperties.getString(PROPS_KEY_TRUSTSTORE_PASSWORD));
 
     // Prepare the authentication details for a web service request, i.e. fedora admin username and password
     authenticator = new HttpTransportProperties.Authenticator();
     authenticator.setUsername(repositoryProperties.getString(PROPS_KEY_FEDORA_ADMIN_USERNAME));
     authenticator.setPassword(repositoryProperties.getString(PROPS_KEY_FEDORA_ADMIN_PASSWORD));
 
     // This is essential as otherwise the auth creds are not sent with the initial request
     authenticator.setPreemptiveAuthentication(true);
 
     loadBCProvider();
     createKeystore();
     createTruststore();
     getServerCert();
   }
 
   /**
    * Loads the BouncyCastle security provider. We need this to create a self signed certificate
    * for use with the keystore. If the Fedora server required client authentication then we
    * must use a keystore to put our client certificate on the wire.
    */
   private static void loadBCProvider() {
     if ((Security.addProvider(new BouncyCastleProvider())) != -1) {
       // We've loaded it, so we should unload it
       okToUnloadBCProvider = true;
     }
   }
 
   @AfterClass
   public static void unloadBCProvider() {
     if (okToUnloadBCProvider) {
       Provider[] providers = Security.getProviders();
 
       /* Although addProvider() returns the ID of the newly installed provider,
        * we can't rely on this. If another webapp removes a provider from the list of
        * installed providers, all the other providers shuffle up the list by one, thus
        * invalidating the ID we got from addProvider().
        */
       try {
         for (int i=0; i < providers.length; i++) {
           if (providers[i].getName().equalsIgnoreCase(Guanxi.BOUNCY_CASTLE_PROVIDER_NAME)) {
             Security.removeProvider(Guanxi.BOUNCY_CASTLE_PROVIDER_NAME);
           }
         }
       }
       catch(SecurityException se) {
         /* We'll end up here if a security manager is installed and it refuses us
          * permission to remove the BouncyCastle provider
          */
       }
     }
   }
 
   /**
    * Create a keystore with a self signed certificate in it in case Fedora wants client
    * authentication.
    */
   private static void createKeystore() {
     try {
       // xmlsec bouncycastle
       SecUtils secUtils = SecUtils.getInstance();
 
       secUtils.createSelfSignedKeystore(repositoryProperties.getString(PROPS_KEY_KEYSTORE_CERT_CN),
                                         repositoryProperties.getString(PROPS_KEY_KEYSTORE_LOCATION),
                                         repositoryProperties.getString(PROPS_KEY_KEYSTORE_PASSWORD),
                                         repositoryProperties.getString(PROPS_KEY_KEYSTORE_PASSWORD),
                                         repositoryProperties.getString(PROPS_KEY_KEYSTORE_CERT_ALIAS));
     }
     catch(Exception ge) {
       fail(ge.getMessage());
     }
   }
 
   /**
    * Create an empty truststore. This will be used by the Axis2 web services clients to verify the SSL
    * exchange with Fedora servers. One truststore will hold the server certificates of multiple Fedoras.
    * The truststore will be populated by using the Guanxi SSL probing functionality.
    */
   private static void createTruststore() {
     try {
    KeyStore ks = KeyStore.getInstance("JKS");

    // Does the keystore exist?
    File keyStore = new File(repositoryProperties.getString(PROPS_KEY_KEYSTORE_LOCATION));
    if (keyStore.exists())
      ks.load(new FileInputStream(repositoryProperties.getString(PROPS_KEY_KEYSTORE_LOCATION)), repositoryProperties.getString(PROPS_KEY_KEYSTORE_PASSWORD).toCharArray());
    else
      ks.load(null, null);
     }
     catch(Exception e) {
       fail(e.getMessage());
     }
   }
 
   /**
    * Uses the Guanxi SSL Layer probing functionality to probe a Fedora server for its X509 certificate. This is
    * then added to the truststore to allow the Axis2 web services clients to trust that Fedora.
    */
   private static void getServerCert() {
     try {
       EntityConnection connection = new EntityConnection(repositoryProperties.getString(PROPS_KEY_API_M_ENDPOINT),
                                                          repositoryProperties.getString(PROPS_KEY_KEYSTORE_CERT_ALIAS),
                                                         repositoryProperties.getString(PROPS_KEY_KEYSTORE_PASSWORD),
                                                          repositoryProperties.getString(PROPS_KEY_KEYSTORE_PASSWORD),
                                                          repositoryProperties.getString(PROPS_KEY_TRUSTSTORE_LOCATION),
                                                          repositoryProperties.getString(PROPS_KEY_TRUSTSTORE_PASSWORD),
                                                          EntityConnection.PROBING_ON);
       X509Certificate fedoraX509 = connection.getServerCertificate();
       KeyStore engineTrustStore = KeyStore.getInstance("jks");
       engineTrustStore.load(new FileInputStream(repositoryProperties.getString(PROPS_KEY_TRUSTSTORE_LOCATION)),
                             repositoryProperties.getString(PROPS_KEY_TRUSTSTORE_PASSWORD).toCharArray());
       // ...under it's Subject DN as an alias...
       engineTrustStore.setCertificateEntry(fedoraX509.getSubjectDN().toString(), fedoraX509);
       // ...and rewrite the trust store
       engineTrustStore.store(new FileOutputStream(repositoryProperties.getString(PROPS_KEY_TRUSTSTORE_LOCATION)),
                              repositoryProperties.getString(PROPS_KEY_TRUSTSTORE_PASSWORD).toCharArray());
     }
     catch(Exception e) {
       fail(e.getMessage());
     }
   }
 }
