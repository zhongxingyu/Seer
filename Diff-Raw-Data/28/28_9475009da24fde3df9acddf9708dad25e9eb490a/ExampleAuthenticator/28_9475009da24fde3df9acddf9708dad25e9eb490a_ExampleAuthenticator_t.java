 package com.syncup.service.auth;
 
 /**
  * Created with IntelliJ IDEA.
  * User: aditya
  * Date: 4/20/12
  * Time: 5:56 AM
  * To change this template use File | Settings | File Templates.
  */
 import com.syncup.service.core.User;
 import com.google.common.base.Optional;
 import com.yammer.dropwizard.auth.AuthenticationException;
 import com.yammer.dropwizard.auth.Authenticator;
 import com.yammer.dropwizard.auth.basic.BasicCredentials;
 
 public class ExampleAuthenticator implements Authenticator<BasicCredentials, User> {
     @Override
     public Optional<User> authenticate(BasicCredentials credentials) throws AuthenticationException {
         if ("secret".equals(credentials.getPassword())) {
            return Optional.absent();
         }
         return Optional.absent();
     }
 }
