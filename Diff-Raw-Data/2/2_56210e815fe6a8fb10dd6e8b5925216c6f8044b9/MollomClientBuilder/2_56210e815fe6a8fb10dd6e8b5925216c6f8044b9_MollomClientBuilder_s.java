 package com.mollom.client;
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.filter.ClientFilter;
 import com.sun.jersey.core.util.MultivaluedMapImpl;
 import com.sun.jersey.oauth.client.OAuthClientFilter;
 import com.sun.jersey.oauth.signature.OAuthParameters;
 import com.sun.jersey.oauth.signature.OAuthSecrets;
 
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.MultivaluedMap;
 
 /**
 * Builds a new mollomClient instance to interact with the Mollom service.
  *
  * The initial entry point for the Mollom API library.
  */
 public class MollomClientBuilder {
 
   private static final String PRODUCTION_ENDPOINT = "http://rest.mollom.com/";
   private static final String TESTING_ENDPOINT = "http://dev.mollom.com/";
 
   private static final String DEFAULT_API_VERSION = "v1";
   private static final int DEFAULT_RETRIES = 1;
   private static final int DEFAULT_CONNECTION_TIMEOUT = 1500;
   private static final int DEFAULT_READ_TIMEOUT = 1500;
 
   private static final String DEFAULT_CLIENT_NAME = "MollomJava";
   private static final String DEFAULT_CLIENT_VERSION = "2.0-SNAPSHOT";
 
   // Client behavior settings.
   private boolean testing;
 
   private String apiVersion;
   private int retries;
   private int connectionTimeout;
   private int readTimeout;
 
   // Client information sent to Mollom for support and statistics.
   private String platformName;
   private String platformVersion;
   private String clientName;
   private String clientVersion;
 
   public static MollomClientBuilder create() {
     return new MollomClientBuilder();
   }
 
   MollomClientBuilder() {
     testing = false;
     apiVersion = DEFAULT_API_VERSION;
     retries = DEFAULT_RETRIES;
     connectionTimeout = DEFAULT_CONNECTION_TIMEOUT;
     readTimeout = DEFAULT_READ_TIMEOUT;
     clientName = DEFAULT_CLIENT_NAME;
     clientVersion = DEFAULT_CLIENT_VERSION;
   }
 
   /**
    * Enables usage of the dev.mollom.com endpoint to test your integration.
    *
    * Optional. Default value: false
    *
    * Testing mode differences:
    * - checkContent() only reacts to the literal strings "spam", "ham", and
    *   "unsure" in the postTitle and postBody parameters.
    * - If none of the literal strings is contained, and no blacklist/whitelist
    *   entries matched, the final spamClassification will be "unsure".
    * - checkCaptcha() only accepts "correct" for image CAPTCHAs and "demo" for
    *   audio CAPTCHAs as the correct solution.
    */
   public MollomClientBuilder withTesting(boolean testing) {
     this.testing = testing;
     return this;
   }
 
   /**
    * Sets the version of the Mollom API to use.
    *
    * Optional. Default value: v1
    *
    * There is only one API version at this point, but included for completeness.
    */
   public MollomClientBuilder withApiVersion(String apiVersion) {
     // TODO: Add additional checks when we support multiple API versions
     if (!"v1".equals(apiVersion)) {
       throw new MollomConfigurationException("Property `apiVersion` must be one of ('v1')");
     }
 
     this.apiVersion = apiVersion;
     return this;
   }
 
   /**
    * Sets the maximum number of times a Mollom API request will be retried.
    *
    * Optional. Default value: 1
    */
   public MollomClientBuilder withRetries(int retries) {
     if (retries < 0) {
       throw new MollomConfigurationException("Property `retries` must be greater than or equal to 0.");
     }
 
     this.retries = retries;
     return this;
   }
 
   /**
    * Sets the timeout (ms) for establishing a connection to the Mollom API.
    *
    * Optional. Default value: 1500 (ms)
    *
    * A value of 0 means no timeout (infinite).
    */
   public MollomClientBuilder withConnectionTimeout(int connectionTimeout) {
     if (connectionTimeout < 0) {
       throw new MollomConfigurationException("Property `connectionTimeout` must be greater than or equal to 0.");
     }
 
     this.connectionTimeout = connectionTimeout;
     return this;
   }
 
   /**
    * Sets the timeout (ms) for waiting for and reading a Mollom API response.
    *
    * Optional. Default value: 1500 (ms)
    *
    * A value of 0 means no timeout (infinite).
    */
   public MollomClientBuilder withReadTimeout(int readTimeout) {
     if (readTimeout < 0) {
       throw new MollomConfigurationException("Property `readTimeout` must be greater than or equal to 0.");
     }
 
     this.readTimeout = readTimeout;
     return this;
   }
 
   /**
    * Sets the Mollom client name.
    *
    * Optional. Default value: MollomJava
    *
    * Change this value if you are using a modified version of this client library.
    *
    * Used to speed up support requests and technical inquiries. The data may
    * also be aggregated to help the Mollom staff to make decisions on new
    * features or the necessity of back-porting improved functionality to older
    * versions.
    */
   public MollomClientBuilder withClientName(String clientName) {
     this.clientName = clientName;
     return this;
   }
 
   /**
    * Sets the Mollom client version.
    *
    * Optional. Default value: {current library version}
    *
    * Change this value if you are using a modified version of this client library.
    *
    * Used to speed up support requests and technical inquiries. The data may
    * also be aggregated to help the Mollom staff to make decisions on new
    * features or the necessity of back-porting improved functionality to older
    * versions.
    */
   public MollomClientBuilder withClientVersion(String clientVersion) {
     this.clientVersion = clientVersion;
     return this;
   }
 
   /**
    * Sets the platform/framework/CMS name.
    *
    * Optional. Default value: n/a
    *
    * Only specify a publicly available web application framework here.
    * For example:
    * - Spring
    * - DaliCore
    *
    * If you are not using a public web application framework or CMS project,
    * leave this property empty.
    *
    * Used to speed up support requests and technical inquiries. The data may
    * also be aggregated to help the Mollom staff to make decisions on new
    * features or the necessity of back-porting improved functionality to older
    * versions.
    */
   public MollomClientBuilder withPlatformName(String platformName) {
     this.platformName = platformName;
     return this;
   }
 
   /**
    * Sets the platform/framework version.
    *
    * Optional. Default value: n/a
    *
    * Specify the version of the platform/framework; e.g., 3.1.0
    *
    * Used to speed up support requests and technical inquiries. The data may
    * also be aggregated to help the Mollom staff to make decisions on new
    * features or the necessity of back-porting improved functionality to older
    * versions.
    */
   public MollomClientBuilder withPlatformVersion(String platformVersion) {
     this.platformVersion = platformVersion;
     return this;
   }
 
   /**
    * Builds the MollomClient object as configured.
    *
    * @throws MollomConfigurationException If could not authenticate with the Mollom service.
    */
   public MollomClient build(String publicKey, String privateKey) {
     Client client = new Client();
     client.setConnectTimeout(connectionTimeout);
     client.setReadTimeout(readTimeout);
 
     OAuthParameters oauthParams = new OAuthParameters()
       .signatureMethod("HMAC-SHA1")
       .consumerKey(publicKey)
       .version("1.0");
     OAuthSecrets oauthSecrets = new OAuthSecrets()
       .consumerSecret(privateKey);
     ClientFilter oauthFilter = new OAuthClientFilter(client.getProviders(), oauthParams, oauthSecrets);
     client.addFilter(oauthFilter);
 
     String rootUrl = testing ? TESTING_ENDPOINT : PRODUCTION_ENDPOINT;
     WebResource rootResource = client
       .resource(rootUrl)
       .path(apiVersion);
 
     // Verify that API keys exist.
     if (publicKey == null || publicKey.equals("")) {
       throw new MollomConfigurationException("The property `publicKey` must be configured.");
     }
     if (privateKey == null || privateKey.equals("")) {
       throw new MollomConfigurationException("The property `privateKey` must be configured.");
     }
     MultivaluedMap<String, String> postParams = new MultivaluedMapImpl();
     postParams.putSingle("platformName", platformName);
     postParams.putSingle("platformVersion", platformVersion);
     postParams.putSingle("clientName", clientName);
     postParams.putSingle("clientVersion", clientVersion);
     ClientResponse response = rootResource
       .path("site")
       .path(publicKey)
       .accept(MediaType.APPLICATION_XML).type(MediaType.APPLICATION_FORM_URLENCODED)
       .post(ClientResponse.class, postParams);
     if (response.getStatus() != 200) {
       throw new MollomConfigurationException("Invalid public/private key.");
     }
 
     // Initialize the resources.
     WebResource contentResource = rootResource.path("content");
     WebResource captchaResource = rootResource.path("captcha");
     WebResource feedbackResource = rootResource.path("feedback");
     WebResource blacklistResource = rootResource.path("blacklist").path(publicKey);
     WebResource whitelistResource = rootResource.path("whitelist").path(publicKey);
 
     MollomClient mollomClient = new MollomClient(client, contentResource, captchaResource, feedbackResource, blacklistResource, whitelistResource, retries);
     return mollomClient;
   }
 }
