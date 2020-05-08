 package com.openske.model.security;
 
 import java.util.List;
 
 import com.openske.model.assets.AssetAccessor;
import com.openske.model.hardware.Host;
 
 public class UserGroup implements AssetAccessor {
 
     protected String name;
    protected Host host;
     protected List<UserAccount> accounts;
 
    public UserGroup(Host host) {
        this.host = host;
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

    public Host getHost() {
        return host;
    }

    public void setHost(Host host) {
        this.host = host;
    }
 }
