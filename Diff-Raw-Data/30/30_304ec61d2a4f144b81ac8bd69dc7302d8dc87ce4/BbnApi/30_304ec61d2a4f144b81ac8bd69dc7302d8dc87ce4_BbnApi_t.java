 package bbn.api.sms;
 
 import bbn.api.sms.domain.Result;
 import bbn.api.sms.domain.SMS;
 import bbn.api.sms.domain.SmsGroup;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Victor Igbokwe (vicsstar@yahoo.com)
  * @created Sep 23, 2012 at 1:19:42 AM
  */
 public final class BbnApi {
 
   // A singleton for the BbnApi.
   private static BbnApi api;
   // The configuration options available inside the config file or added manually.
   private final Map<String, Object> config = new HashMap<>();
 
   /**
    *
    * @param configFile
    * @throws IOException if an I/O error occurs.
    */
   private BbnApi() throws IOException {
 
     // Load the default configuration file.
     loadConfig(BBN.DEFAULT_CONFIG_FILE);
     // Load the user's configuration file.
     loadConfig(BBN.USER_CONFIG_FILE);
   }
 
   /**
    * Retrieves the single instance of the BbnApi class.
    *
    * @return BbnApi
    * @throws IOException if an I/O error occurs.
    */
   public static BbnApi getInstance() throws IOException {
     if (api == null) {
       api = new BbnApi();
     }
     return api;
   }
 
   /**
    * Returns the default instance for this application.
    *
    * @return
    */
   public static BbnApi defaultInstance() {
     return api;
   }
 
   /*
    * Loads the configuration files specified in the constructor.
    */
   private void loadConfig(final String configFile) throws IOException {
     Properties properties = new Properties();
 
     try {
       // check if the config file is available on the file system.
       if (new File(configFile).isAbsolute()) {
         // try loading the config file from the file system.
         properties.load(new FileInputStream(configFile));
       } else {
         // load the config file from the classpath.
         properties.load(
                 ClassLoader.getSystemClassLoader().getResourceAsStream(configFile));
       }
     } catch (NullPointerException e) {
       Logger.getLogger(BbnApi.class.getName()).log(Level.WARNING, null, e);
     }
 
     // put/add each entry into the config.
     for (Object key : properties.keySet()) {
       if ("bbn.api.config.include".equals(key.toString())) {
         loadConfig(properties.getProperty(key.toString()));
       } else {
         addConfig(key.toString(), properties.getProperty(key.toString()));
       }
     }
   }
 
   /**
    * Adds a new configuration key into the config or updates an existing one.
    *
    * @param key the key to add to the config.
    * @param value the value to be associated with the key in the config.
    */
   public void addConfig(final String key, final String value) {
     if ((key == null) || (value == null)) {
       throw new NullPointerException("The config's key or value cannot be null.");
     }
     config.put(key, value);
   }
 
   /**
    * Returns the value associated with a particular configuration key as a
    * string.
    *
    * @param key The key whose value is to be retrieved.
    * @return Boolean the value of the key as a String.
    */
   public String getConfig(String key) {
     return (key != null) ? String.valueOf(config.get(key)) : null;
   }
 
   /**
    * Attempts converting the value of the key specified as a Boolean.
    *
    * @param key The key whose value is to be retrieved.
    * @return Boolean the value of the key as a Boolean.
    */
   public Boolean getConfigAsBoolean(String key) {
     return Boolean.parseBoolean(getConfig((key)));
   }
 
   /**
    * Changes the secure state of the BBN API when making requests.
    *
    * @param secure When set to true, API requests will be made over HTTPS,
    * otherwise over plain HTTP.
    */
   public void setSecure(final boolean secure) {
     addConfig("bbn.api.secure", secure ? "true" : "false");
   }
 
   /**
    * Checks if subsequent API requests using the BBN API will be secure.
    *
    * @return boolean The secure state of subsequent API requests.
    */
   public boolean isSecure() {
     return getConfigAsBoolean("bbn.api.secure");
   }
 
   /**
    * Change the username used to make API calls to the BBN SMS network.
    *
    * @param username The username to use for subsequent requests.
    */
   public void setUsername(final String username) {
     if (username != null) {
       addConfig("username", username);
     }
   }
 
   /**
    * Change the password used to make API calls to the BBN SMS network.
    *
    * @param password The password to use for subsequent requests.
    */
   public void setPassword(final String password) {
     if (password != null) {
       addConfig("password", password);
     }
   }
 
   /**
    * Login with a username and password different from that available in the
    * config file.
    *
    * @param username the BBN username to login with.
    * @param password the BBN password to login with.
    */
   public Result<Boolean> login(
           final String username,
           final String password) throws IOException {
     Http requestHttp = new Http(BBN.Resource.AUTH_URL());
     requestHttp.addParam(BBN.Params.USERNAME, username);
     requestHttp.addParam(BBN.Params.PASSWORD, password);
 
     // Make the HTTP GET request.
     final String response = requestHttp.get();
     final boolean valid = Boolean.parseBoolean(
             response != null ? response.trim() : "false");
     return new Result(valid).setMessage(response).setError(!valid);
   }
 
   /**
    * Login using the default username and password provided in the config file.
    */
   public Result<Boolean> login() throws MalformedURLException, IOException {
     return login(getConfig(BBN.Params.USERNAME),
             getConfig(BBN.Params.PASSWORD));
   }
 
   /**
    * Checks and returns the amount of credit available in a user's account on
    * the BBN network.
    *
    * @param username The username to use in making the request.
    * @param password The matching password to use in making the request.
    * @return Float the amount of credit remaining.
    */
   public Result<Float> checkBalance(
           final String username,
           final String password) throws IOException {
     Http requestHttp = new Http(BBN.Resource.ACCOUNT_BALANCE_URL());
     requestHttp.addParam(BBN.Params.USERNAME, username);
     requestHttp.addParam(BBN.Params.PASSWORD, password);
     final String result = requestHttp.get();
 
     try {
       return new Result(Float.parseFloat(result)).setError(false);
     } catch (NumberFormatException e) {
       return new Result().setMessage(result);
     }
   }
 
   /**
    * Checks and returns the amount of credit available in a user's account on
    * the BBN network using the default configuration options
    * (username/password).
    *
    * @return Float
    * @throws IOException
    */
   public Result<Float> checkBalance() throws IOException {
     return checkBalance(getConfig(BBN.Params.USERNAME),
             getConfig(BBN.Params.PASSWORD));
   }
 
   /**
    * Sends an SMS to a recipient.
    *
    * @param sms The SMS to send.
    * @param username The username to use in authentication.
    * @param password The associated password to use in authentication.
    * @return Result<String> The result of the operation.
    * @throws MalformedURLException
    * @throws IOException If a network or I/O error occurs.
    */
   public Result<String> sendMessage(
           final SMS sms,
           final String username,
           final String password) throws MalformedURLException, IOException {
     Http requestHttp = new Http(BBN.Resource.SEND_SMS_URL());
     requestHttp.addParam(BBN.Params.USERNAME, username);
     requestHttp.addParam(BBN.Params.PASSWORD, password);
     requestHttp.addParam(BBN.Params.SENDER, sms.getSender());
     requestHttp.addParam(BBN.Params.MESSAGE, sms.getMessage());
     requestHttp.addParam(BBN.Params.MOBILE, sms.getMobile());
 
     // We use POST requests to help the user prevent failure due to too long URLs.
     final String result = requestHttp.post();
 
     return new Result(result != null ? result.trim() : "").setError(
             !result.contains(String.valueOf(BBN.ResponseCode.MESSAGE_SENT.getCode())));
   }
 
   /**
    * Sends an SMS to a recipient.
    *
    * @param sms The SMS to send.
    * @return Result<String> The result of the operation.
    * @throws MalformedURLException
    * @throws IOException
    */
   public Result<String> sendMessage(SMS sms)
           throws MalformedURLException, IOException {
     // Delegates to the method that actually sends the SMS.
     return sendMessage(sms, getConfig(BBN.Params.USERNAME),
             getConfig(BBN.Params.PASSWORD));
   }
 
   /**
    * Sends an SMS to a list recipients.
    *
    * @param smsGroup The group to send the SMS to.
    * @param username The username to use in authentication.
    * @param password The associated password to use in authentication.
    * @return Result<String> The result of the operation.
    * @throws MalformedURLException
    * @throws IOException If a network or I/O error occurs.
    */
   public Result<String> sendBatchMessage(
           final SmsGroup smsGroup,
           final String username,
           final String password) throws MalformedURLException, IOException {
     Http requestHttp = new Http(BBN.Resource.SEND_SMS_URL());
     requestHttp.addParam(BBN.Params.USERNAME, username);
     requestHttp.addParam(BBN.Params.PASSWORD, password);
     requestHttp.addParam(BBN.Params.SENDER, smsGroup.getSender());
     requestHttp.addParam(BBN.Params.MESSAGE, smsGroup.getMessage());
     requestHttp.addParam(BBN.Params.MOBILE, smsGroup.getMobileListCommaSeparated());
 
     // We use POST requests to help the user prevent failure due to too long URLs.
     final String result = requestHttp.post();
     return new Result(result != null ? result.trim() : "").setError(
             !result.contains(String.valueOf(BBN.ResponseCode.MESSAGE_SENT.getCode())));
   }
 
   /**
    * Sends an SMS to a list recipients.
    *
    * @param smsGroup The group to send the SMS to.
    * @return Result<String> The result of the operation.
    * @throws MalformedURLException
    * @throws IOException If a network or I/O error occurs.
    */
   public Result<String> sendBatchMessage(
           final SmsGroup smsGroup) throws MalformedURLException, IOException {
     // Delegates to the method that actually sends the group SMS.
     return sendBatchMessage(smsGroup, getConfig(BBN.Params.USERNAME),
             getConfig(BBN.Params.PASSWORD));
   }
 
   /**
    * Schedules SMS to be sent to a list of recipients.
    *
    * @param smsGroup The group of SMS to be sent.
    * @param scheduleName The name of the schedule.
    * @param username The username for authentication.
    * @param password The associated password for authentication.
    * @return Result<String>
    * @throws MalformedURLException
    * @throws IOException
    */
   public Result<String> scheduleMessage(
           final SmsGroup smsGroup,
           final String scheduleName,
          final boolean notify,
           final String username,
           final String password) throws MalformedURLException, IOException {
     Http requestHttp = new Http(BBN.Resource.SCHEDULE_SMS_URL());
     requestHttp.addParam(BBN.Params.USERNAME, username);
     requestHttp.addParam(BBN.Params.PASSWORD, password);
     requestHttp.addParam(BBN.Params.SENDER, smsGroup.getSender());
     requestHttp.addParam(BBN.Params.MESSAGE, smsGroup.getMessage());
     requestHttp.addParam(BBN.Params.MOBILE, smsGroup.getMobileListCommaSeparated());
     requestHttp.addParam(BBN.Params.SCHEDULE, "1");
    requestHttp.addParam(BBN.Params.SCHEDULE_NOTIFY, notify ? "1" : "0");
     requestHttp.addParam(BBN.Params.BROADCAST_TIME, String.valueOf(new Date().getTime()));
 
     if (scheduleName != null) {
       requestHttp.addParam(BBN.Params.SCHEDULE_NAME, scheduleName);
     }
 
     // We use POST requests to help the user prevent failure due to too long URLs.
     final String result = requestHttp.post();
     return new Result(result != null ? result.trim() : "").setError(
             !result.contains(String.valueOf(BBN.ResponseCode.SCHEDULE_SAVED.getCode())));
   }
 
   /**
    * Schedules SMS to be sent to a list of recipients.
    *
    * @param smsGroup The list of recipients to send SMS to.
    * @param scheduleName The name of the schedule.
    * @return Result<String>
    * @throws MalformedURLException
    * @throws IOException
    */
   public Result<String> scheduleMessage(
           final SmsGroup smsGroup,
          final String scheduleName,
          final boolean notify) throws MalformedURLException, IOException {
    return scheduleMessage(
            smsGroup, scheduleName, notify,
             getConfig(BBN.Params.USERNAME),
             getConfig(BBN.Params.PASSWORD));
   }
 }
