 package org.bahmni.feed.openerp;
 
 import org.bahmni.openerp.web.OpenERPProperties;
 
 public class OpenERPPropertiesStub implements OpenERPProperties {
     @Override
     public String getHost() {
         return "localhost";
     }
 
     @Override
     public int getPort() {
         return 8069;
     }
 
     @Override
     public String getDatabase() {
         return "openerp";
     }
 
     @Override
     public String getUser() {
         return "admin";
     }
 
     @Override
     public String getPassword() {
         return "password";
     }

    @Override
    public int getConnectionTimeoutInMilliseconds() {
        return -1;
    }

    @Override
    public int getReplyTimeoutInMilliseconds() {
        return -1;
    }
 }
