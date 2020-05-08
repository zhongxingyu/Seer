 package org.atlasapi.feeds.radioplayer.upload;
 
 import java.io.File;
 import java.util.List;
 
 import org.apache.ftpserver.ftplet.Authority;
 import org.apache.ftpserver.ftplet.AuthorizationRequest;
 import org.apache.ftpserver.ftplet.User;
 import org.apache.ftpserver.usermanager.impl.WritePermission;
 
 import com.google.common.collect.ImmutableList;
 
 public class TestUser implements User {
 
     private final String TEST_USERNAME;
     private final String TEST_PASSWORD;
     private final File homeDir;
    
    public TestUser() {
        TEST_USERNAME = "";
        TEST_PASSWORD = "";
        homeDir = null;
    }
 
     public TestUser(String name, String password, File homeDir) {
         this.TEST_USERNAME = name;
         this.TEST_PASSWORD = password;
         this.homeDir = homeDir;
     }
     
     @Override
     public String getName() {
         return TEST_USERNAME;
     }
 
     @Override
     public String getPassword() {
         return TEST_PASSWORD;
     }
 
     @Override
     public List<Authority> getAuthorities() {
         return ImmutableList.<Authority> of(new WritePermission());
     }
 
     @Override
     public List<Authority> getAuthorities(Class<? extends Authority> clazz) {
         if (clazz.equals(WritePermission.class)) {
             return ImmutableList.<Authority> of(new WritePermission());
         }
         return ImmutableList.<Authority> of();
     }
 
     @Override
     public AuthorizationRequest authorize(AuthorizationRequest request) {
         return new WritePermission().authorize(request);
     }
 
     @Override
     public int getMaxIdleTime() {
         return 0;
     }
 
     @Override
     public boolean getEnabled() {
         return true;
     }
 
     @Override
     public String getHomeDirectory() {
         return homeDir.getAbsolutePath();
     }
 };
