 package com.github.mailerific.server;
 
 import java.util.Date;
 import java.util.List;
 
 import com.github.mailerific.client.Mail;
 import com.github.mailerific.client.NotLoggedInException;
 import com.github.mailerific.client.UserAccount;
 import com.github.mailerific.client.UserAccountService;
 import com.github.mailerific.client.UsernameNotUniqueException;
 import com.google.appengine.api.users.User;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 public class UserAccountServiceImpl extends RemoteServiceServlet implements
         UserAccountService {
 
     private void checkLoggedIn() throws NotLoggedInException {
         User user = UserServiceFactory.getUserService().getCurrentUser();
         if (user == null) {
             throw new NotLoggedInException("Not logged in.");
         }
     }
 
     @Override
     public UserAccount authenticate(final String uri) {
         UserService userService = UserServiceFactory.getUserService();
         User user = userService.getCurrentUser();
         UserAccount userAccount;
         if (user != null) {
             // has google account, now check for Mailerific account
             userAccount = Users.getUserByEmail(user.getEmail());
             if (userAccount == null) {
                 // if no Mailerific account, sign him up
                 userAccount = signUp(user);
             }
             userAccount.setNickname(user.getNickname());
             userAccount.setLoggedIn(true);
             userAccount.setLogoutUrl(userService.createLogoutURL(uri));
         } else {
             userAccount = new UserAccount();
             // not logged in to google, redirect to login page
             userAccount.setLoggedIn(false);
             userAccount.setLoginUrl(userService.createLoginURL(uri));
         }
         return userAccount;
     }
 
     private UserAccount signUp(final User user) {
         UserAccount account = new UserAccount();
         account.setFirstTimeUser(true);
         account.setEmail(user.getEmail());
         return Users.save(account);
     }
 
     @Override
     public void saveInSettings(final Long id, final String subject)
             throws NotLoggedInException {
         checkLoggedIn();
         UserAccount account = Users.getUserById(id);
         account.setInSubject(subject);
     }
 
     @Override
     public void saveOutSettings(final Long id, final String subject,
             final boolean includeSig, final String signature)
             throws NotLoggedInException {
         checkLoggedIn();
         UserAccount account = Users.getUserById(id);
         account.setOutSubject(subject);
         account.setOutIncludeSig(includeSig);
         account.setOutSignature(signature);
     }
 
     @Override
     public void savePublicSettings(final Long id, final boolean enable,
             final String subject, final String username)
             throws NotLoggedInException, UsernameNotUniqueException {
         checkLoggedIn();
         if (!isUsernameUnique(id, username)) {
             throw new UsernameNotUniqueException();
         }
         UserAccount account = Users.getUserById(id);
         account.setPublicSubject(subject);
         account.setEnablePublic(enable);
         account.setUsername(username);
     }
 
     @Override
     public void unmarkFirstTime(final Long id) throws NotLoggedInException {
         checkLoggedIn();
         UserAccount account = Users.getUserById(id);
         account.setFirstTimeUser(false);
     }
 
     @Override
     public Boolean isUsernameUnique(final Long id, final String username) {
         UserAccount account = Users.getUserById(id);
         if (username.equals(account.getUsername())) {
             // not unique, but user owns the previous one
             return true;
         }
         return null == Users.getUserByUsername(username);
     }
 
     public List<Mail> listIncoming(final String email, final Long upperBound)
             throws NotLoggedInException {
         checkLoggedIn();
         List<Mail> mails = Mails.getByOwner(email, upperBound);
         for (Mail mail : mails) {
             mail.setDatePast(PastCalculator.diff(mail.getReceivedDate()));
         }
         return mails;
     }
 
     public List<Mail> listIncoming(final String email)
             throws NotLoggedInException {
         checkLoggedIn();
         List<Mail> mails = Mails.getByOwner(email);
         for (Mail mail : mails) {
             mail.setDatePast(PastCalculator.diff(mail.getReceivedDate()));
         }
         return mails;
     }
 
     public List<Mail> listOutgoing(final String email, final Long upperBound)
             throws NotLoggedInException {
         checkLoggedIn();
         List<Mail> mails = Mails.getBySender(email, upperBound);
         for (Mail mail : mails) {
             mail.setDatePast(PastCalculator.diff(mail.getReceivedDate()));
         }
         return mails;
     }
 
     public List<Mail> listOutgoing(final String email)
             throws NotLoggedInException {
         checkLoggedIn();
         List<Mail> mails = Mails.getBySender(email);
         for (Mail mail : mails) {
             mail.setDatePast(PastCalculator.diff(mail.getReceivedDate()));
         }
         return mails;
     }
 
     @Override
     public void removeUser(final Long id) throws NotLoggedInException {
         checkLoggedIn();
         UserAccount account = Users.getUserById(id);
         Users.remove(account);
     }
 
     private static class PastCalculator {
         // conversion to milliseconds
         private static final int SECONDS = 1000;
         private static final int MINUTES = 60 * SECONDS;
         private static final int HOURS = 60 * MINUTES;
         private static final int DAY = 24 * 60 * 60 * 1000;
 
         private static String diff(final Date from, final Date date) {
             long milis1 = from.getTime();
             long milis2 = date.getTime();
             long diff = milis2 - milis1;
 
             long diffDays = diff / DAY;
             if (diffDays > 0) {
                 return diffString(diffDays, "day");
             }
             long diffHours = diff / HOURS;
             if (diffHours > 0) {
                 return diffString(diffHours, "hour");
             }
             long diffSeconds = diff / SECONDS;
             if (diffSeconds > 0) {
                 return diffString(diffSeconds, "second");
             }
             return "1 second ago"; // if we reached this, it was very recent
         }
 
         private static String diffString(final long duration, final String unit) {
             return duration + " " + (duration > 1 ? unit + "s" : unit) + " ago";
         }
 
         private static String diff(final Date from) {
             return diff(from, new Date());
         }
     }
 }
