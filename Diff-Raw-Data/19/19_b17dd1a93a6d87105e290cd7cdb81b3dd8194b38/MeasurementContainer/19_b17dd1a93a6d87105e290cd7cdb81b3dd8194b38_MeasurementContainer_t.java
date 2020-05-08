 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package acs.fluffy.restinterface.domain;
 
 import acs.fluffy.domain.Fingerprint;
 import acs.fluffy.form.LocalizationType;
 import java.util.List;
 
 /**
  *
  * @author tonykova
  */
 public class MeasurementContainer {
     private String username;
     private String password;
     private LocalizationType type;
     
     private List<Fingerprint> fingerprints;
     
    public MeasurementContainer() {};
    
     public MeasurementContainer(String username, String password, List<Fingerprint> fingerprints) {
         this.username = username;
         this.password = password;
         this.fingerprints = fingerprints;
         this.type = LocalizationType.ByErrorAverage;
     }
     
     public MeasurementContainer(String username, String password, List<Fingerprint> fingerprints, LocalizationType type) {
         this(username, password, fingerprints);
         this.type = type;
     }
 
     public List<Fingerprint> getFingerprints() {
         return fingerprints;
     }
 
     public void setFingerprints(List<Fingerprint> fingerprints) {
         this.fingerprints = fingerprints;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public LocalizationType getType() {
         return type;
     }
 
     public void setType(LocalizationType type) {
         this.type = type;
     }
 
     public String getUsername() {
         return username;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
     
     @Override
     public String toString() {
         String str = "";
         
         for (Fingerprint f : fingerprints) {
             str += f.toString() + "\n";
         }
         
         return str;
     }
 }
