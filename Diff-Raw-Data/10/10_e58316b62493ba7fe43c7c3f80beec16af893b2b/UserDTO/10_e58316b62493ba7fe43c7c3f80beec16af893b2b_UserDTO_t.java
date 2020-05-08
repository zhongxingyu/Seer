 package edu.chip.carranet.client.data;
 
 import edu.chip.carranet.auth.backend.UserPermissions;
 import edu.chip.carranet.auth.backend.UserUtil;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 
 public class UserDTO implements Serializable {
 
     private String userName;
     private List<String> homeSites = new ArrayList<String>();
     private List<String> studies = new ArrayList<String>();
     private List<GWTUserPermissions> roles = new ArrayList<GWTUserPermissions>();
 
 
     public UserDTO() {
 
     }
 
     public void setAssertionStrings(Collection<String> assertionStrings) {
         roles.clear();
         homeSites.clear();
         studies.clear();
 
         for (String s : assertionStrings) {
             String[] tokens = s.split(":");
             if (tokens.length == 2) {
                 if (tokens[0].equals(UserUtil.HOMESITE_PREFIX)) {
                     homeSites.add(tokens[1]);
                 }
                 if (tokens[0].equals(UserUtil.STUDY_PREFIX)) {
                     studies.add(tokens[1]);
                 }
                 if (tokens[0].equals(UserUtil.ROLE_PREFIX)) {
                     roles.add(GWTUserPermissions.permissionFromString(tokens[1]));
 
                 }
             }
         }
 
 
     }
 
     public ArrayList<String> getAssertionStrings() {
         ArrayList<String> assertions = new ArrayList<String>();
 
         for (String s : homeSites) {
             if (s != null) {
                assertions.add("homesite:" + s);
 
             }
         }
 
         for (String s : studies) {
             if (s != null) {
                 assertions.add("study:" + s);
             }
         }
 
         for (GWTUserPermissions s : roles) {
             if (s != null) {
                 assertions.add(s.toAssertionString());
             }
         }
         return assertions;
     }
 
 
     public String getUserName() {
         return userName;
     }
 
     public void setUserName(String userName) {
         this.userName = userName;
     }
 
     public List<String> getStudies() {
         return studies;
     }
 
     public void setStudies(List<String> studies) {
         this.studies = studies;
     }
 
     public List<GWTUserPermissions> getRoles() {
         return roles;
     }
 
     public void setRoles(List<GWTUserPermissions> roles) {
         this.roles = roles;
     }
 
     public List<String> getHomeSites() {
         return homeSites;
     }
 
     public void setHomeSites(List<String> homeSites) {
         this.homeSites = homeSites;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         UserDTO userDTO = (UserDTO) o;
 
         if (homeSites != null ? !homeSites.equals(userDTO.homeSites) : userDTO.homeSites != null) return false;
         if (roles != null ? !roles.equals(userDTO.roles) : userDTO.roles != null) return false;
         if (studies != null ? !studies.equals(userDTO.studies) : userDTO.studies != null) return false;
         if (userName != null ? !userName.equals(userDTO.userName) : userDTO.userName != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = userName != null ? userName.hashCode() : 0;
         result = 31 * result + (homeSites != null ? homeSites.hashCode() : 0);
         result = 31 * result + (studies != null ? studies.hashCode() : 0);
         result = 31 * result + (roles != null ? roles.hashCode() : 0);
         return result;
     }
 }
 
