 package com.directi.train.tweetapp.services;
 
import com.directi.train.tweetapp.services.Auxillary.PasswordStore;
 import org.junit.Test;
 
 /**
  * Created with IntelliJ IDEA.
  * User: sitesh
  * Date: 6/8/12
  * Time: 9:21 PM
  * To change this template use File | Settings | File Templates.
  */
 public class TestPasswordStore {
 

     @Test
     public void testSendPassword() {
         String to = "siteshshrivastava@gmail.com";
         String password = "bitch";
 
        new PasswordStore().sendPassword(to, password);
     }
 }
