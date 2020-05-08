 package org.atlasapi.feeds.radioplayer.upload;
 
 import org.apache.ftpserver.ftplet.Authentication;
 import org.apache.ftpserver.ftplet.AuthenticationFailedException;
 import org.apache.ftpserver.ftplet.FtpException;
 import org.apache.ftpserver.ftplet.User;
 import org.apache.ftpserver.ftplet.UserManager;
 import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
 
 public class TestUserManager implements UserManager {
     
     private final String TEST_USERNAME;
     private final User testUser;
 
     public TestUserManager(User user) {
         this.TEST_USERNAME = user.getName();
         this.testUser = user;
     }
 
     @Override
     public User getUserByName(String username) throws FtpException {
         if (username == TEST_USERNAME) {
             return testUser;
         }
         return null;
     }
 
     @Override
     public String[] getAllUserNames() throws FtpException {
         return new String[] { TEST_USERNAME };
     }
 
     @Override
     public void delete(String username) throws FtpException {
         // no-op
     }
 
     @Override
     public void save(User user) throws FtpException {
     }
 
     @Override
     public boolean doesExist(String username) throws FtpException {
         return username.equals(TEST_USERNAME);
     }
 
     @Override
     public User authenticate(Authentication authentication) throws AuthenticationFailedException {
         if (authentication instanceof UsernamePasswordAuthentication) {
             UsernamePasswordAuthentication upauth = (UsernamePasswordAuthentication) authentication;
             if (upauth.getUsername().equals(TEST_USERNAME)) {
                 return testUser;
             }
         }
         throw new AuthenticationFailedException();
     }
 
     @Override
     public String getAdminName() throws FtpException {
         return "admin";
     }
 
     @Override
     public boolean isAdmin(String username) throws FtpException {
         return username.equals("admin");
     }
 
 }
