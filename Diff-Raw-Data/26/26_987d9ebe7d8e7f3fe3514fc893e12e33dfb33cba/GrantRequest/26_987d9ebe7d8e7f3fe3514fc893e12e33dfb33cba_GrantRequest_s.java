 package com.janrain.backplane2.server.provision;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author Johnny Bufu
  */
 public class GrantRequest extends AdminRequest {
 
     // - PUBLIC
 
     public Map<String, String> getGrants() {
         return grants;
     }
 
     public void setGrants(Map<String, String> grants) {
         this.grants = new HashMap<String, String>();
         this.grants.putAll(grants);
     }
 
     // - PRIVATE
 
     // client_id -> space separated buses
     private Map<String,String> grants;
 }
