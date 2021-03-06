 package org.jivesoftware;
 
 import javax.security.auth.login.AppConfigurationEntry;
 import javax.security.auth.login.Configuration;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Vector;
 
 
 public class GSSAPIConfiguration extends Configuration {
 
     Map<String, Vector<AppConfigurationEntry>> configs;
 
     GSSAPIConfiguration() {
         super();
         init(true);
     }
 
     GSSAPIConfiguration(boolean config_from_file) {
         super();
        init(confi_from_file);
     }
 
 
    private void init(boolean config_from_file) {
 
         configs = new HashMap<String, Vector<AppConfigurationEntry>>();
 
         //The structure of the options is not well documented in terms of
         //data types.  Since the file version of the Configuration object
         //puts things in quotes, String is assumed. But boolean options
         //do not have quotes, and my represent different types internally.
         HashMap<String, String> c_options = new HashMap<String, String>();
 
         //If Kerberos config is not from a file, it's not possible to (re-)read the config file.
         //So don't set refreshKrb5Config
         if (config_from_file) {
             c_options.put("refreshKrb5Config", "true");
         }
         c_options.put("doNotPrompt", "true");
         c_options.put("useTicketCache", "true");
         c_options.put("debug", "true");
 
 
         putAppConfigurationEntry("com.sun.security.jgss.initiate", "com.sun.security.auth.module.Krb5LoginModule", AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, c_options);
         putAppConfigurationEntry("com.sun.security.jgss.krb5.initiate", "com.sun.security.auth.module.Krb5LoginModule", AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, c_options);
 
     }
 
     public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
         AppConfigurationEntry[] a = new AppConfigurationEntry[1];
         if (configs.containsKey(name)) {
             Vector<AppConfigurationEntry> v = configs.get(name);
             a = v.toArray(a);
             return a;
         }
         else {
             return null;
         }
     }
 
     public boolean putAppConfigurationEntry(String name, String module, AppConfigurationEntry.LoginModuleControlFlag controlFlag, Map<String,String> options) {
         Vector<AppConfigurationEntry> v;
         if (configs.containsKey(name)) {
             v = configs.get(name);
         }
         else {
             v = new Vector<AppConfigurationEntry>();
             configs.put(name, v);
         }
 
         return v.add(new AppConfigurationEntry(module, controlFlag, options));
     }
 
 
     public void refresh() {
     }
 
     
 }
