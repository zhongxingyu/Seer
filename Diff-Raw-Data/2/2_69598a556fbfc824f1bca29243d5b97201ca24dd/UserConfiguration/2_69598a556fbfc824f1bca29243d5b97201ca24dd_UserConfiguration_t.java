 package com.cloudbees.sdk;
 
 import com.cloudbees.api.AccountInfo;
 import com.cloudbees.api.AccountKeysResponse;
 import com.cloudbees.api.AccountListResponse;
 import com.cloudbees.api.BeesClient;
 import com.cloudbees.api.BeesClientConfiguration;
 import com.cloudbees.api.BeesClientException;
 import com.cloudbees.sdk.cli.DirectoryStructure;
 import com.cloudbees.sdk.cli.Verbose;
 import com.cloudbees.sdk.utils.Helper;
 import com.cloudbees.sdk.utils.PasswordHelper;
 
 import javax.inject.Inject;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 /**
  * Injectable component that encapsulates the user configuration
  * and its persistence to {@code ~/.bees/bees.config}
  *
  * <h2>Parameters vs Config properties</h2>
  * <p>
  * For historical reasons, the code distinguishes "config properties" and "parameters."
  * Both are string->string key/value pairs, and both captures various aspects of how
  * we talk to CloudBees backend, but for reasons beyond me, they use different key names
  * to represent the same thing. For example, "config properties" would have "bees.api.key" for
  * API key, whereas "parameters" would use "key".
  *
  * <p>
  * "config properties" are tied to the persisted {@ccode ~/.bees/bees.config} whereas
  * parameters appear to be transient within one invocation.
  *
  * <p>
  * There's no constants defined for any of those keys.
  *
  * <p>
  * Most likely this is unintended technical debt over time, but for the time being
  * I'm not touching it. I recommend unifying them to consistently use "config properties" key
  * names.
  *
  * @author Kohsuke Kawaguchi
  */
 public class UserConfiguration {
     @Inject
     DirectoryStructure directoryStructure;
     
     @Inject
     Verbose verbose;
 
     /**
      * Loads the configuration, by creating it if necessary.
      *
      * @param parameters
      *      used only if we are to create configuration.
      */
     public Properties load(int credentialType, Map<String,String> parameters) {
         File userConfigFile = getConfigFile();
         
         Properties properties = new Properties();
         properties.setProperty("bees.api.url.us", "https://api.cloudbees.com/api");
         properties.setProperty("bees.api.url.eu", "https://api-eu.cloudbees.com/api");
 
         if (!Helper.loadProperties(userConfigFile, properties)) {
             properties = create(credentialType, parameters);
         }
 
         return properties;
     }
 
     public File getConfigFile() {
         return new File(directoryStructure.localRepository, "bees.config");
     }
 
     /**
      * Creates a new configuration file.
      */
     public Properties create(int credentialType, Map<String, String> paramaters) {
         Properties properties = new Properties();
        properties.setProperty("bees.api.url.us", "https://api.cloudbees.com/api");
        properties.setProperty("bees.api.url.eu", "https://api-eu.cloudbees.com/api");
         System.out.println();
         System.out.println("You have not created a CloudBees configuration profile, let's create one now...");
 
         try {
             String endPoint = paramaters.get("endPoint");
 /*
                 while (endPoint == null || endPoint.equalsIgnoreCase("us") || endPoint.equalsIgnoreCase("eu")) {
                     endPoint = Helper.promptFor("Enter your default CloudBees API end point [us | eu]: ", true);
                 }
 */
             if (endPoint == null) endPoint = "us";
 
             String server = paramaters.get("server");
             if (server == null) server = properties.getProperty("bees.api.url." + endPoint);
 
             properties.setProperty("bees.api.url", server);
             String key = paramaters.get("key");
             String secret = paramaters.get("secret");
             String domain = paramaters.get("domain");
             if (key == null || secret == null) {
                 if (credentialType == KEYS_CREDENTIALS) {
                     System.out.println("Go to https://grandcentral.cloudbees.com/user/keys to retrieve your API key");
                     System.out.println();
                 } else if (credentialType == EMAIL_CREDENTIALS) {
                     String email = paramaters.get("email");
                     if (email == null)
                         email = Helper.promptFor("Enter your CloudBees account email address: ", true);
                     String password = paramaters.get("password");
                     if (password == null) {
                         password = PasswordHelper.prompt("Enter your CloudBees account password: ");
                     }
 
                     // Get the API key & secret
                     BeesClientConfiguration beesClientConfiguration = new BeesClientConfiguration(server, "1", "0", "xml", "1.0");
                     // Set proxy information
                     beesClientConfiguration.setProxyHost(paramaters.get("proxy.host"));
                     if (paramaters.get("proxy.port") != null)
                         beesClientConfiguration.setProxyPort(Integer.parseInt(paramaters.get("proxy.port")));
                     beesClientConfiguration.setProxyUser(paramaters.get("proxy.user"));
                     beesClientConfiguration.setProxyPassword(paramaters.get("proxy.password"));
 
                     BeesClient staxClient = new BeesClient(beesClientConfiguration);
                     staxClient.setVerbose(verbose.isVerbose());
                     AccountKeysResponse response = staxClient.accountKeys(domain, email, password);
                     key = response.getKey();
                     secret = response.getSecret();
 
                     // Get the default account name
                     beesClientConfiguration.setApiKey(key);
                     beesClientConfiguration.setSecret(secret);
                     staxClient = new BeesClient(beesClientConfiguration);
                     staxClient.setVerbose(verbose.isVerbose());
                     AccountListResponse listResponse = staxClient.accountList();
                     List<AccountInfo> accounts = listResponse.getAccounts();
                     if (accounts.size() == 1) {
                         domain = accounts.get(0).getName();
                     } else {
                         String accountsString = null;
                         for (AccountInfo info: accounts) {
                             if (accountsString == null)
                                 accountsString = info.getName();
                             else
                                 accountsString += "," + info.getName();
                         }
                         System.out.println("You have several accounts: " + accountsString);
                         domain = Helper.promptFor("Enter your default CloudBees account name : ", true);
                     }
                 }
             }
 
             if (key == null) key = Helper.promptFor("Enter your CloudBees API key: ", true);
             if (secret == null) secret = Helper.promptFor("Enter your CloudBees secret: ", true);
             if (domain == null) domain = Helper.promptFor("Enter your default CloudBees account name: ", true);
 
             properties.setProperty("bees.api.key", key);
             properties.setProperty("bees.api.secret", secret);
             properties.setProperty("bees.project.app.domain", domain);
             if (paramaters.get("proxy.host") != null)
                 properties.setProperty("bees.api.proxy.host", paramaters.get("proxy.host"));
             if (paramaters.get("proxy.port") != null)
                 properties.setProperty("bees.api.proxy.port", paramaters.get("proxy.port"));
             if (paramaters.get("proxy.user") != null)
                 properties.setProperty("bees.api.proxy.user", paramaters.get("proxy.user"));
             if (paramaters.get("proxy.password") != null)
                 properties.setProperty("bees.api.proxy.password", paramaters.get("proxy.password"));
 
             getConfigFile().getParentFile().mkdirs();
 
             FileOutputStream fos = new FileOutputStream(getConfigFile());
             properties.store(fos, "CloudBees SDK config");
             fos.close();
 
             return properties;
         } catch (BeesClientException e) {
             String errCode = e.getError().getErrorCode();
             if (errCode != null && errCode.equals("AuthFailure"))
                 throw new BeesSecurityException("Authentication failure, please check credentials!", e);
             else
                 throw new RuntimeException(e.getMessage(), e);
         } catch (Exception e) {
             throw new RuntimeException("Cannot create configuration", e);
         }
     }
 
     public static int EMAIL_CREDENTIALS = 0;
     public static int KEYS_CREDENTIALS = 1;
 }
