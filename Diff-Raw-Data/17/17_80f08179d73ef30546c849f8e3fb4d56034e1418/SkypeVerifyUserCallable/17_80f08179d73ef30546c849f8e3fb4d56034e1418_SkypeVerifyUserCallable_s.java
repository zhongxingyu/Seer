 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package hudson.plugins.skype.im.transport.callables;
 
 import com.skype.SkypeException;
 import com.skype.SkypeImpl;
 import com.skype.User;
 import com.skype.User.BuddyStatus;
 import hudson.plugins.skype.im.transport.SkypeIMException;
 import hudson.remoting.Callable;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author jbh
  */
 public class SkypeVerifyUserCallable implements Callable<String, SkypeIMException> {
 
     private String skypeNames = null;
 
     public SkypeVerifyUserCallable(String names) {
         this.skypeNames = names;
     }
 
     public String call() throws SkypeIMException {
         String result = null;
 
         User usr = SkypeImpl.getUser(skypeNames);
 
         try {
             if (usr == null || usr.getFullName() == null || usr.getFullName().trim().length() <= 0) {
                 usr = null;
                 User[] users = SkypeImpl.searchUsers(skypeNames);

                for (User user : users) {
                    if (user.getId().equals(skypeNames)) {
                        usr = user;
                        break;
                    } else if (skypeNames.contains("@")) {
                         //EMail, so this must be ok.
                        usr = user;
                        break;
                     }
                 }
             }
             if (usr != null) {
                 BuddyStatus bdyStatus = usr.getBuddyStatus();
                 if (!usr.isAuthorized()) {
                     usr.setAuthorized(true);
                 }
                 System.out.println("BDY (" + usr.getDisplayName() + "):'" + bdyStatus + "' :'" + BuddyStatus.ADDED + "'");
                 if (!usr.getBuddyStatus().equals(BuddyStatus.ADDED)) {
                     try {
                         SkypeImpl.getContactList().addFriend(usr, "The Skype Service on " + InetAddress.getLocalHost().getHostName() + " wants to notify you");
                     } catch (UnknownHostException ex) {
                         Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                         throw new SkypeIMException(ex);
                     }
                     result = usr.getId();
                 }
             }
         } catch (SkypeException ex) {
             throw new SkypeIMException(ex);
         }
 
 
 
         return result;
     }
 }
