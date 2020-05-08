 package com.globalsight.tools;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 public class Profiles implements UserDataComponent {
 
     private Map<String, Profile> profiles = 
                 new HashMap<String, Profile>();
     
     public Profiles(Map<String, String> props) {
         Map<String, Map<String, String>> m = 
             new HashMap<String, Map<String, String>>();
         for (Map.Entry<String, String> e : props.entrySet()) {
            String[] parts = e.getKey().split(".");
             if (parts.length != 2) {
                 continue; // TODO: warn
             }
             Map<String, String> profileValues = m.get(parts[0]);
             if (profileValues == null) {
                 profileValues = new HashMap<String, String>();
                 m.put(parts[0], profileValues);
             }
             profileValues.put(parts[1], e.getValue());
         }
         for (Map.Entry<String, Map<String, String>> e : m.entrySet()) {
             profiles.put(e.getKey(), new Profile(e.getKey(), e.getValue()));
         }
     }
     
     @Override
     public Map<String, String> getValues() {
         Map<String, String> props = new HashMap<String, String>();
         for (Profile p : profiles.values()) {
             props.putAll(p.getValues());
         }
         return props;
     }
 
     public void addProfile(Profile profile) {
         profiles.put(profile.getProfileName(), profile);
     }
     
     public Profile getProfile(String profileName) {
         return profiles.get(profileName);
     }
     
     public Collection<Profile> getProfiles() {
         return profiles.values();
     }
 }
