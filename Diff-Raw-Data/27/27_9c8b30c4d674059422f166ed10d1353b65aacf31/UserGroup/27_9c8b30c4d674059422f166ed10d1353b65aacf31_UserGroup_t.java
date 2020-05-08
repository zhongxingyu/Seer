 package com.openske.model.security;
 
 import java.util.List;
 
 import com.openske.model.assets.AssetAccessor;
import com.openske.model.software.Software;
 
 public class UserGroup implements AssetAccessor {
 
     protected String name;
    protected Software software;
     protected List<UserAccount> accounts;
 
    public UserGroup(Software software) {
        this.software = software;
     }
 
     public List<UserAccount> getAccounts() {
         return accounts;
     }
 
     public void setAccounts(List<UserAccount> accounts) {
         this.accounts = accounts;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 }
