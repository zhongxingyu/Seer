 package com.globalsight.tools;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class Profile implements UserDataComponent {
     private String profileName;
     private String url;
     private String username;
     private String password;
     
     public Profile(String profileName, String url, String username, 
                    String password) {
         this.profileName = profileName;
         this.url = url;
         this.username = username;
         this.password = password;
     }
     
     // XXX The asymmetry between this and getValues() is pretty bogus
     public Profile(String profileName, Map<String, String> props) {
         this.profileName = profileName;
        this.username = props.get("user");
         this.password = props.get("password");
         this.url = props.get("url");
     }
     
     @Override
     public Map<String, String> getValues() {
         Map<String, String> v = new HashMap<String, String>();
        v.put(profileName + ".user", username);
         v.put(profileName + ".password", password);
         v.put(profileName + ".url", url);
         return v;
     }
 
     public String getProfileName() {
         return profileName;
     }
     
     public String getUsername() {
         return username;
     }
 
     public String getPassword() {
         return password;
     }
     
     public String getUrl() {
         return url;
     }
 
     public void setProfileName(String profileName) {
         this.profileName = profileName;
     }
     public void setUrl(String url) {
         this.url = url;
     }
     public void setUsername(String name) {
         this.username = name;
     }
     public void setPassword(String password) {
         this.password = password;
     }
 
 }
