 /*
 * $Id: SLCSClient.java,v 1.2 2006/11/03 14:08:04 vtschopp Exp $
  * 
  * Created on Aug 8, 2006 by tschopp
  *
  * Copyright (c) Members of the EGEE Collaboration. 2004.
  * See http://eu-egee.org/partners/ for details on the copyright holders.
  * For license conditions see the license file or http://eu-egee.org/license.html
  */
 package org.glite.slcs;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.security.GeneralSecurityException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.glite.slcs.config.SLCSClientConfiguration;
 import org.glite.slcs.httpclient.ssl.ExtendedProtocolSocketFactory;
 import org.glite.slcs.pki.Certificate;
 import org.glite.slcs.pki.CertificateExtension;
 import org.glite.slcs.pki.CertificateExtensionFactory;
 import org.glite.slcs.pki.CertificateKeys;
 import org.glite.slcs.pki.CertificateRequest;
 import org.glite.slcs.shibclient.ShibbolethClient;
 import org.glite.slcs.shibclient.ShibbolethCredentials;
 import org.glite.slcs.shibclient.metadata.ShibbolethClientMetadata;
 import org.glite.slcs.util.PasswordReader;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.protocol.Protocol;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import au.id.jericho.lib.html.Element;
 import au.id.jericho.lib.html.Source;
 
 /**
  * 
  * SLCSClient
  * 
  * @author Valery Tschopp <tschopp@switch.ch>
  * @version $Version$
  */
 public class SLCSClient {
 
     /** Logging */
     private static Log LOG= LogFactory.getLog(SLCSClient.class);
 
     /** Default XML config filename in CLASSPATH */
     static private String DEFAULT_CONFIGURATION_FILE= "slcs-init.xml";
 
     /** Configuration */
     private SLCSClientConfiguration configuration_= null;
 
     /** Shibboleth client */
     private ShibbolethClient shibClient_= null;
 
     /** Shibboleth credentials */
     private ShibbolethCredentials shibCredentials_= null;
 
     /** Shibboleth client metadata */
     private ShibbolethClientMetadata shibMetadata_= null;
 
     /** Absolute pathname of directory to store user key and cert */
     private String storeDirectory_= null;
 
     /** Filename for the user cert */
     private String userCertFilename_= null;
 
     /** Filename for the user key */
     private String userKeyFilename_= null;
     
     /** Private key size */
     private int keySize_= -1;
 
     /** optional user file prefix for cert and key files */
     private String userPrefix_= null;
 
     /** Authorization Token */
     private String authorizationToken_= null;
 
     /** URL to post certificate requests */
     private String certificateRequestUrl_= null;
 
     /** Certificate subject */
     private String certificateSubject_= null;
 
     /** List of required certificate extensions */
     private List certificateExtensions_= null;
 
     /** Private and public keys */
     private CertificateKeys certificateKeys_= null;
 
     /** Certificate request */
     private CertificateRequest certificateRequest_= null;
 
     /** X.509 certificate */
     private Certificate certificate_= null;
 
     /**
      * 
      * @param configuration
      * @param credentials
      * @throws SLCSException
      */
     public SLCSClient(SLCSClientConfiguration configuration,
             ShibbolethCredentials credentials) throws SLCSException {
         this.configuration_= configuration;
         
         // read default params from config
         this.storeDirectory_= createDefaultStoreDirectory(configuration_);
         this.userCertFilename_= getDefaultUserCertFile(configuration_);
         this.userKeyFilename_= getDefaultUserKeyFile(configuration_);
         this.keySize_= getDefaultUserKeySize(configuration_);
         
         // create the embedded HTTP Shibboleth agent
         HttpClient httpClient= createHttpClient(configuration_);
         this.shibMetadata_= new ShibbolethClientMetadata(configuration_);
         this.shibCredentials_= credentials;
         this.shibClient_= new ShibbolethClient(httpClient,
                                                shibMetadata_,
                                                shibCredentials_);
 
     }
 
     /**
      * Creates the HttpClient based on the SLCS client config.
      * 
      * @param configuration
      *            SLCS client configuration.
      * @return a new HttpClient object.
      * @throws SLCSConfigurationException
      * @throws SLCSException
      */
     static private HttpClient createHttpClient(
             SLCSClientConfiguration configuration)
             throws SLCSConfigurationException, SLCSException {
         String truststore= configuration.getString("HttpClient.TrustStoreFile");
         LOG.info("TrustStoreFile=" + truststore);
         try {
             ExtendedProtocolSocketFactory protocolSocketFactory= new ExtendedProtocolSocketFactory(truststore);
             Protocol https= new Protocol("https", protocolSocketFactory, 443);
             Protocol.registerProtocol("https", https);
         } catch (IOException e) {
             LOG.error(e);
             throw new SLCSException("Failed to create ExtendedProtocolSocketFactory",
                                     e);
         }
         return new HttpClient();
     }
 
     /**
      * 
      * @param configuration
      *            SLCS client configuration.
      * @return
      * @throws SLCSConfigurationException
      */
     static private String createDefaultStoreDirectory(
             SLCSClientConfiguration configuration)
             throws SLCSConfigurationException {
         String storeDirectory= configuration.getString("StoreDirectory");
         LOG.debug("StoreDirectory=" + storeDirectory);
         // check java properties variable and expand
         int start= storeDirectory.indexOf("${");
         if (0 <= start) {
             int stop= storeDirectory.indexOf("}");
             if (start <= stop) {
                 String propertyName= storeDirectory.substring(start + 2, stop);
                 String propertyValue= System.getProperty(propertyName);
                 if (propertyValue != null) {
                     LOG.debug("replace ${" + propertyName + "} with: "
                             + propertyValue);
                     String replace= "\\$\\{" + propertyName + "\\}";
                     storeDirectory= storeDirectory.replaceAll(replace,
                                                               propertyValue);
                 }
                 else {
                     LOG.error("StoreDirectory contains invalid ${"
                             + propertyName + "} java property");
                     throw new SLCSConfigurationException("StoreDirectory contains invalid ${"
                             + propertyName + "} java property");
                 }
             }
             else {
                 // ERROR
                 LOG.error("StoreDirectory contains invalid ${...} java property");
                 throw new SLCSConfigurationException("StoreDirectory contains invalid ${...} java property");
             }
         }
         LOG.info("StoreDirectory=" + storeDirectory);
         // create directory
         File directory= new File(storeDirectory);
         if (!directory.exists()) {
             LOG.debug("create directory: " + storeDirectory);
             directory.mkdirs();
         }
         if (!directory.isDirectory()) {
             LOG.error(directory.getAbsolutePath() + " is not a directory");
         }
 
         return directory.getAbsolutePath();
     }
 
     /**
      * Returns the default UserCertFile from the config.
      * 
      * @param configuration
      *            the XML config obj.
      * @return The usercert filename.
      * @throws SLCSConfigurationException
      */
     static private String getDefaultUserCertFile(
             SLCSClientConfiguration configuration)
             throws SLCSConfigurationException {
         String userCertFile= configuration.getString("UserCertFile");
         LOG.info("UserCertFile=" + userCertFile);
         return userCertFile;
     }
 
     /**
      * Returns the default UserKeyFile from the config.
      * 
      * @param configuration
      *            the XML config obj.
      * @return The userkey filename.
      * @throws SLCSConfigurationException
      */
     static private String getDefaultUserKeyFile(
             SLCSClientConfiguration configuration)
             throws SLCSConfigurationException {
         String userKeyFile= configuration.getString("UserKeyFile");
         LOG.info("UserKeyFile=" + userKeyFile);
         return userKeyFile;
     }
 
     
     /**
      * Returns the default private KeySize from the config.
      * 
      * @param configuration
      *            the XML config obj.
      * @return the key size (bits).
      * @throws SLCSConfigurationException
      */
     static private int getDefaultUserKeySize(
             SLCSClientConfiguration configuration)
             throws SLCSConfigurationException {
         int keySize= configuration.getInt("UserKeySize");
         // check valid key size
         if (!validKeySize(keySize)) {
             LOG.error("Invalid UserKeySize: " + keySize);
             throw new SLCSConfigurationException("Invalid UserKeySize: " + keySize);
         }
         LOG.info("KeySize=" + keySize);
         return keySize;
     }
     
     /**
      * Checks that the size is 512, 1024 or 2048
      * @param size the key size
      * @return <code>true</code> if the size valid.
      */
     static private boolean validKeySize(int size) {
         boolean valid= false;
         if (size == 1024 || size == 2048 || size == 512) {
             valid= true;
         }
         return valid;
     }
     
     /**
      * @param args
      */
     public static void main(String[] args) {
         LOG.info("Start...");
         // parse command line
         CommandLineParser parser= new PosixParser();
         CommandLine cmd= null;
         boolean error= false;
         Options options= createCommandLineOptions();
         try {
             cmd= parser.parse(options, args);
         } catch (ParseException e) {
             System.err.println("ERROR: " + e.getMessage());
             error= true;
         }
         
        // help? or error
         if (error || cmd.hasOption('h')) {
            System.out.println("slcs-init: " + SLCSClient.class.getName()
                    + " - " + SLCSClientVersion.COPYRIGHT);
            System.out.println("Version: " + SLCSClientVersion.VERSION);
             HelpFormatter help= new HelpFormatter();
             help.printHelp("slcs-init --idp <providerId> [options]", options);
             System.exit(1);
         }
         
         // version?
         if (cmd.hasOption('V')) {
             System.out.println("slcs-init: " + SLCSClient.class.getName()
                     + " - " + SLCSClientVersion.COPYRIGHT);
             System.out.println("Version: " + SLCSClientVersion.VERSION);
             System.exit(0);
         }
         
         // verbose?
         boolean verbose= false;
         if (cmd.hasOption('v')) {
             verbose= true;
         }
         
         // config
         String config= null;
         if (cmd.hasOption('c')) {
             config= cmd.getOptionValue('c');
             if (config == null) {
                 System.err.println("ERROR: --config: empty config filename");
                 System.exit(1);
             }
             File configFile= new File(config);
             if (!configFile.exists()) {
                 System.err.println("ERROR: config file: " + config
                         + " doesn't exist");
                 System.exit(1);
             }
         }
         else {
             config= DEFAULT_CONFIGURATION_FILE;
         }
         if (verbose) {
             System.out.println("Config: " + config);
         }
         
         // store directory
         String storeDirectory= null;
         if (cmd.hasOption('D')) {
             storeDirectory= cmd.getOptionValue('D');
             if (storeDirectory == null) {
                 System.err.println("ERROR: --storedir: empty store dirname");
                 System.exit(1);
             }
             if (verbose) {
                 System.out.println("Store Directory: " + storeDirectory);
             }
         }
 
         // user prefix
         String userPrefix= null;
         if (cmd.hasOption('P')) {
             userPrefix= cmd.getOptionValue('P');
             if (userPrefix == null) {
                 System.err.println("ERROR: --prefix: empty prefix");
                 System.exit(1);                
             }
             if (verbose) {
                 System.out.println("Prefix: " + userPrefix);
             }
         }
 
         // IdP providerId
         String idpProviderId= null;
         if (cmd.hasOption('i')) {
             idpProviderId= cmd.getOptionValue('i');
             if (idpProviderId == null) {
                 System.err.println("ERROR: --idp: empty IdP ProviderId");
                 System.exit(1);
             }
         }
         else {
            System.err.println("ERROR: option --idp <providerId> is missing");
            HelpFormatter help= new HelpFormatter();
            help.printHelp("slcs-init --idp <providerId> [options]", options);
             System.exit(1);
 
         }
         LOG.info("IdentityProvider: " + idpProviderId);
         if (verbose) {
             System.out.println("IdentityProvider: " + idpProviderId);
         }
         
         // username
         String username= null;
         if (cmd.hasOption('u')) {
             username= cmd.getOptionValue('u');
             if (username == null) {
                 System.err.println("ERROR: --user: empty username");
                 System.exit(1);
             }
         }
         else {
             // get current username
             username= System.getProperty("user.name");
         }
         LOG.info("Username: " + username);
         if (verbose) {
             System.out.println("Username: " + username);
         }
         
         // password
         char[] password= null;
         if (cmd.hasOption('p')) {
             password= cmd.getOptionValue('p').toCharArray();
         }
         else {
             // read from console
             try {
                 password= PasswordReader.getPassword(System.in,
                                                      "Shibboleth Password: ");
             } catch (IOException e) {
                 // ignored?
                 LOG.error(e);
             }
         }
         if (password == null) {
             System.err.println("ERROR: empty password");
             System.exit(1);
         }
         // private key size and password
         int keySize= -1;
         if (cmd.hasOption('s')) {
             keySize= Integer.parseInt(cmd.getOptionValue('s'));
         }
         char[] keyPassword= null;
         if (cmd.hasOption('k')) {
             keyPassword= cmd.getOptionValue('k').toCharArray();
         }
         else {
             // read from console
             try {
                 keyPassword= PasswordReader.getPassword(System.in,
                                                         "Key Password: ");
             } catch (IOException e) {
                 // ignored?
                 LOG.error(e);
             }
         }
         if (keyPassword == null) {
             System.out.println("Key password is empty, using Shibboleth password.");
             keyPassword= password;
         }
 
         // create client
         SLCSClient client= null;
         try {
             LOG.debug("load SLCS client configuration...");
             SLCSClientConfiguration configuration= SLCSClientConfiguration.getInstance(config);
             ShibbolethCredentials credentials= new ShibbolethCredentials(username,
                                                                          password,
                                                                          idpProviderId);
             LOG.debug("create SLCS client...");
             client= new SLCSClient(configuration, credentials);
             if (storeDirectory != null) {
                 LOG.debug("overwrite store directory: " + storeDirectory);
                 client.setStoreDirectory(storeDirectory);
             }
             if (userPrefix != null) {
                 LOG.debug("set prefix: " + userPrefix);
                 client.setUserPrefix(userPrefix);
             }
         } catch (SLCSException e) {
             LOG.fatal("SLCS client creation error", e);
             System.err.println("ERROR: Failed to create SLCS client: " + e);
             System.exit(1);
         }
 
         // client shibboleth login
         try {
             LOG.info("Shibboleth login...");
             if (verbose) {
                 System.out.println("Shibboleth login...");
             }
             client.shibbolethLogin();
         } catch (SLCSException e) {
             LOG.fatal("SLCSClient Shibboleth login error", e);
             System.err.println("ERROR: " + e);
             System.exit(1);
         }
 
         // SLCS login request, get DN and authToken
         try {
             LOG.info("SLCS login request...");
             if (verbose) {
                 System.out.println("SLCS login request...");
             }
             client.slcsLogin();
         } catch (SLCSException e) {
             LOG.fatal("SLCS login request error", e);
             System.err.println("ERROR: " + e);
             System.exit(1);
         }
 
         // generate key and CSR
         try {
             if (keySize != -1) {
                 client.setKeySize(keySize);
             }
             keySize= client.getKeySize();
             LOG.info("Generate keys and certificate request...");
             if (verbose) {
                 System.out.println("Generate private key (" + keySize
                         + " bits)...");
             }
             client.generateCertificateKeys(keySize, keyPassword);
             if (verbose) {
                 System.out.println("Generate certificate request...");
             }
             client.generateCertificateRequest();
         } catch (GeneralSecurityException e) {
             LOG.fatal("SLCSClient failed to generate key and certificate request",
                       e);
             System.err.println("ERROR: " + e);
             System.exit(1);
         }
 
         // submit CSR
         try {
             LOG.info("SLCS certificate request...");
             if (verbose) {
                 System.out.println("SLCS certificate request...");
             }
             client.slcsCertificateRequest();
         } catch (SLCSException e) {
             LOG.fatal("SLCS certificate request error", e);
             System.err.println("ERROR: " + e);
             System.exit(1);
         }
 
         // store key + cert
         try {
             if (verbose) {
                 String userkey= client.getStoreDirectory() + File.separator
                         + client.getUserKeyFilename();
                 System.out.println("Store private key [" + userkey + "]...");
             }
             client.storePrivateKey();
             if (verbose) {
                 String usercert= client.getStoreDirectory() + File.separator
                         + client.getUserCertFilename();
                 System.out.println("Store SLCS certificate [" + usercert
                         + "]...");
             }
             client.storeCertificate();
         } catch (IOException e) {
             LOG.fatal("SLCS failed to store key or certificate", e);
             System.err.println("ERROR: " + e);
             System.exit(1);
         }
 
         if (verbose) {
             System.out.println("Done.");
         }
 
     }
 
     /**
      * Login with Shibboleth
      * 
      * @throws SLCSException
      */
     private void shibbolethLogin() throws SLCSException {
         LOG.debug("Shibboleth authentication...");
         boolean authenticated= shibClient_.authenticate();
         if (!authenticated) {
             LOG.error("Shibboleth authentication failed");
             throw new AuthException("Shibboleth authentication failed");
         }
     }
 
     private void slcsLogin() throws SLCSException {
         String slcsLoginURL= shibMetadata_.getSLCS().getUrl();
         GetMethod getLoginMethod= new GetMethod(slcsLoginURL);
         try {
             LOG.info("GET login: " + slcsLoginURL);
             int status= shibClient_.executeMethod(getLoginMethod);
             LOG.debug(getLoginMethod.getStatusLine());
             // check status: 401
             if (status != 200) {
                 LOG.error("SLCS login failed: "
                         + getLoginMethod.getStatusLine());
                 throw new AuthException("SLCS login failed: "
                         + getLoginMethod.getStatusLine());
             }
 
             // read response
             InputStream is= getLoginMethod.getResponseBodyAsStream();
             Source source= new Source(is);
             checkSLCSResponse(source, "SLCSLoginResponse");
             parseSLCSLoginResponse(source);
         } catch (IOException e) {
             LOG.error("Failed to request DN", e);
             throw new SLCSException("Failed to request DN", e);
         } finally {
             getLoginMethod.releaseConnection();
         }
     }
 
     private void slcsCertificateRequest() throws SLCSException {
         PostMethod postCertificateRequestMethod= new PostMethod(certificateRequestUrl_);
         postCertificateRequestMethod.addParameter("AuthorizationToken",
                                                   authorizationToken_);
         postCertificateRequestMethod.addParameter("CertificateSigningRequest",
                                                   certificateRequest_.getPEMEncoded());
         try {
             LOG.info("POST CSR: " + certificateRequestUrl_);
             int status= shibClient_.executeMethod(postCertificateRequestMethod);
             LOG.debug(postCertificateRequestMethod.getStatusLine());
             // check status
             if (status != 200) {
                 LOG.error("SLCS certificate request failed: "
                         + postCertificateRequestMethod.getStatusLine());
                 throw new ServiceException("SLCS certificate request failed: "
                         + postCertificateRequestMethod.getStatusLine());
             }
             // read response
             InputStream is= postCertificateRequestMethod.getResponseBodyAsStream();
             Source source= new Source(is);
             checkSLCSResponse(source, "SLCSCertificateResponse");
             parseSLCSCertificateResponse(source);
 
         } catch (IOException e) {
             LOG.error("Failed to request the certificate", e);
             throw new SLCSException("Failed to request the certificate", e);
         } finally {
             postCertificateRequestMethod.releaseConnection();
         }
     }
 
     private void checkSLCSResponse(Source source, String name)
             throws IOException, SLCSException {
         // optimization !?!
         source.fullSequentialParse();
 
         int pos= 0;
         Element reponseElement= source.findNextElement(pos, name);
         if (reponseElement == null || reponseElement.isEmpty()) {
             LOG.error(name + " element not found");
             throw new ServiceException(name
                     + " element not found in SLCS response");
         }
         // read status
         Element statusElement= source.findNextElement(pos, "status");
         if (statusElement == null || statusElement.isEmpty()) {
             LOG.error("Status element not found");
             throw new ServiceException("Status element not found in SLCS response");
         }
         String status= statusElement.getContent().toString();
         LOG.info("Status=" + status);
         if (status != null && status.equalsIgnoreCase("Error")) {
             pos= statusElement.getEnd();
             Element errorElement= source.findNextElement(pos, "error");
             if (errorElement == null || errorElement.isEmpty()) {
                 LOG.error("Error element not found");
                 throw new SLCSException("Error element not found in SLCS error response");
             }
             String error= errorElement.getContent().toString();
             // is there a stack trace?
             pos= errorElement.getEnd();
             Element traceElement= source.findNextElement(pos, "stacktrace");
             if (traceElement != null && !traceElement.isEmpty()) {
                 String stackTrace= traceElement.getContent().toString();
                 throw new ServiceException(error + "\nRemote error:\n"
                         + stackTrace);
             }
             throw new ServiceException(error);
         }
         else if (status == null || !status.equalsIgnoreCase("Success")) {
             LOG.error("Unknown Status: " + status);
             throw new ServiceException("Unknown Status:" + status);
         }
     }
 
     private void parseSLCSLoginResponse(Source source) throws SLCSException {
         // get AuthorizationToken
         int pos= 0;
         Element tokenElement= source.findNextElement(pos, "AuthorizationToken");
         if (tokenElement == null || tokenElement.isEmpty()) {
             LOG.error("AuthorizationToken element not found");
             throw new SLCSException("AuthorizationToken element not found in SLCS response");
         }
         authorizationToken_= tokenElement.getContent().toString();
         LOG.info("AuthorizationToken=" + authorizationToken_);
         // get the certificate request URL
         pos= tokenElement.getEnd();
         Element certificateRequestElement= source.findNextElement(pos,
                                                                   "CertificateRequest");
         if (certificateRequestElement == null
                 || certificateRequestElement.isEmpty()) {
             LOG.error("CertificateRequest element not found");
             throw new SLCSException("CertificateRequest element not found in SLCS response");
         }
         certificateRequestUrl_= certificateRequestElement.getAttributeValue("url");
         if (certificateRequestUrl_ == null) {
             LOG.error("CertificateRequest url attribute not found");
             throw new SLCSException("CertificateRequest url attribute not found in SLCS response");
         }
         LOG.info("CertificateRequest url=" + certificateRequestUrl_);
 
         // get certificate subject
         Element subjectElement= source.findNextElement(pos, "Subject");
         if (subjectElement == null || subjectElement.isEmpty()) {
             LOG.error("Subject element not found");
             throw new SLCSException("Subject element not found in SLCS response");
         }
         certificateSubject_= subjectElement.getContent().toString();
         LOG.info("CertificateRequest.Subject=" + certificateSubject_);
         // any certificate extensions?
         certificateExtensions_= new ArrayList();
         pos= subjectElement.getEnd();
         Element extensionElement= null;
         while ((extensionElement= source.findNextElement(pos,
                                                          "certificateextension")) != null) {
             pos= extensionElement.getEnd();
             String extensionName= extensionElement.getAttributeValue("name");
             String extensionValues= extensionElement.getContent().toString();
             LOG.info("CertificateRequest.CertificateExtension: "
                     + extensionName + "=" + extensionValues);
             CertificateExtension extension= CertificateExtensionFactory.createCertificateExtension(extensionName,
                                                                                                    extensionValues);
             if (extension != null) {
                 certificateExtensions_.add(extension);
             }
         }
     }
 
     private void parseSLCSCertificateResponse(Source source)
             throws SLCSException, IOException {
         int pos= 0;
         Element certificateElement= source.findNextElement(pos, "Certificate");
         if (certificateElement == null || certificateElement.isEmpty()) {
             LOG.error("Certificate element not found");
             throw new SLCSException("Certificate element not found in SLCS response");
         }
         String pemCertificate= certificateElement.getContent().toString();
         LOG.info("Certificate=" + pemCertificate);
         StringReader reader= new StringReader(pemCertificate);
         try {
             certificate_= Certificate.readPEM(reader);
         } catch (GeneralSecurityException e) {
             LOG.error("Failed to reconstitute the certificate: " + e);
             throw new SLCSException("Failed to reconstitute the certificate", e);
         }
     }
 
     /**
      * Creates the CLI options.
      * 
      * @return The CLI Options
      */
     private static Options createCommandLineOptions() {
         Option help= new Option("h", "help", false, "this help");
         Option username= new Option("u", "user", true, "Shibboleth username");
         username.setArgName("username");
         Option idp= new Option("i", "idp", true, "Shibboleth IdP providerId");
         idp.setArgName("providerId");
         Option config= new Option("c",
                                   "conf",
                                   true,
                                   "SLCS client XML configuration file");
         config.setArgName("filename");
         Option verbose= new Option("v", "verbose", false, "verbose");
         Option version= new Option("V", "version", false, "shows the version");
         Option password= new Option("p",
                                     "password",
                                     true,
                                     "Shibboleth password");
         password.setArgName("password");
         Option keysize= new Option("s",
                                    "keysize",
                                    true,
                                    "private key size (default: 1024)");
         keysize.setArgName("size");
         Option keypassword= new Option("k",
                                        "keypass",
                                        true,
                                        "private key password (default: same as Shibboleth password)");
         keypassword.setArgName("password");
         Option prefix= new Option("P",
                                   "prefix",
                                   true,
                                   "optional usercert.pem and userkey.pem filename prefix");
         prefix.setArgName("prefix");
         Option storedir= new Option("D",
                                     "storedir",
                                     true,
                                     "absolute pathname to the store directory (default: $HOME/.globus)");
         storedir.setArgName("directory");
         // Option list= OptionBuilder.withLongOpt("list").withDescription("list
         // the known Shibboleth IdP").create("l");
         Options options= new Options();
         options.addOption(help);
         options.addOption(username);
         options.addOption(password);
         options.addOption(idp);
         options.addOption(config);
         options.addOption(verbose);
         options.addOption(version);
         options.addOption(keysize);
         options.addOption(keypassword);
         options.addOption(prefix);
         options.addOption(storedir);
         return options;
     }
 
     /**
      * Creates the certificate keys.
      * 
      * @param size
      *            The private key size.
      * @param password
      *            The private key password.
      * @throws GeneralSecurityException
      *             If an
      */
     private void generateCertificateKeys(int size, char[] password)
             throws GeneralSecurityException {
         LOG.debug("generate keys...");
         certificateKeys_= new CertificateKeys(size, password);
     }
 
     /**
      * Store the private key (userkey.pem) in the store directory.
      * 
      * @throws IOException
      *             If an error occurs while writing the userkey.pem file.
      */
     private void storePrivateKey() throws IOException {
         String filename= getStoreDirectory() + File.separator
                 + getUserKeyFilename();
         LOG.info("Store private key: " + filename);
         File file= new File(filename);
         certificateKeys_.storePEMPrivate(file);
     }
 
     /**
      * Stores the X509 certificate with its chain (usercert.pem) in the store
      * directory.
      * 
      * @throws IOException
      *             If an error occurs while writing the usercert.pem file.
      */
     private void storeCertificate() throws IOException {
         String filename= getStoreDirectory() + File.separator
                 + getUserCertFilename();
         LOG.info("Store certificate: " + filename);
         File file= new File(filename);
         certificate_.storePEM(file);
     }
 
     /**
      * Creates the CeriticateRequest object.
      * 
      * @throws GeneralSecurityException
      *             If an error occurs while creating the object.
      */
     private void generateCertificateRequest() throws GeneralSecurityException {
         LOG.debug("generate CSR: " + certificateSubject_);
         certificateRequest_= new CertificateRequest(certificateKeys_,
                                                     certificateSubject_,
                                                     certificateExtensions_);
     }
 
     /**
      * @return The absolute directory name to store the usercert.pem and
      *         userkey.pem files.
      */
     private String getStoreDirectory() {
         return storeDirectory_;
     }
 
     /**
      * Sets the absolute pathname to the store directory.
      * 
      * @param directory
      *            The absolute pathname of the store directory.
      * @return <code>true</code> iff the absolute dirname is an existing writable directory
      */
     private boolean setStoreDirectory(String directory) {
         boolean valid= false;
         if (directory== null) {
             return false;
         }
         File dir= new File(directory);
         if (dir.isDirectory() && dir.canWrite()) {
             storeDirectory_= dir.getAbsolutePath();
             valid= true;
         }
         else {
             LOG.warn("Not a valid store directory: " + directory);
         }
         return valid;
     }
 
     /**
      * Sets the usercert and userkey filename prefix.
      * 
      * @param prefix
      */
     private void setUserPrefix(String prefix) {
         userPrefix_= prefix;
     }
 
     /**
      * @return The prefixed (if any) usercert filename.
      */
     private String getUserCertFilename() {
         String usercert= null;
         if (userPrefix_ == null) {
             usercert= userCertFilename_;
         }
         else {
             usercert= userPrefix_ + userCertFilename_;
         }
         return usercert;
     }
 
     /**
      * @return The prefixed (if any) userkey filename.
      */
     private String getUserKeyFilename() {
         String userkey= null;
         if (userPrefix_ == null) {
             userkey= userKeyFilename_;
         }
         else {
             userkey= userPrefix_ + userKeyFilename_;
         }
         return userkey;
     }
     
     private int getKeySize() {
         return keySize_;
     }
     
     private void setKeySize(int size) {
         //TODO check valid size
         keySize_= size;
     }
 
 }
