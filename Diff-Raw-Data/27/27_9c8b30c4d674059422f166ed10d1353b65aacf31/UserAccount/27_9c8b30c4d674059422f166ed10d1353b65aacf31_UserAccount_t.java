 package com.openske.model.security;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.openske.model.assets.Asset;
 import com.openske.model.assets.AssetAccessor;
 import com.openske.model.software.Software;
 
 public class UserAccount implements AssetAccessor {
 
     protected String username;
     protected String password;
    protected Software software;
     protected List<UserGroup> groups;
     protected UserAccountState state;
     protected List<Asset> assets;
 
     public UserAccount(String username, String password, Software software) {
         super();
         this.username = username;
         this.password = password;
         this.software = software;
         this.state = UserAccountState.ACTIVE;
         this.assets = new ArrayList<Asset>();
         this.groups = new ArrayList<UserGroup>();
         this.software.addAccount(this);
     }
 
     public List<UserGroup> getGroups() {
         return groups;
     }
 
     public void setGroups(List<UserGroup> groups) {
         this.groups = groups;
     }
 
     public UserAccountState getState() {
         return state;
     }
 
     public void setState(UserAccountState state) {
         this.state = state;
     }
 
     public String getUsername() {
         return username;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
 
     public List<Asset> getAssets() {
         return assets;
     }
 
     public void setAssets(List<Asset> assets) {
         this.assets = assets;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public Software getSoftware() {
         return software;
     }
 
     public void setSoftware(Software software) {
         this.software = software;
     }
 }
