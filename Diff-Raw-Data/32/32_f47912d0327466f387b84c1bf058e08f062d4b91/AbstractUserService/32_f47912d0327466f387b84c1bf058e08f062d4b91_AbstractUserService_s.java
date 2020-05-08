 /*
  * Created on 2003-9-29
  *
  * To change the template for this generated file go to
  * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
  */
 package com.cyclops.tornado.services.user;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Iterator;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.commons.lang.StringUtils;
 
 import com.cyclops.tornado.services.BaseService;
 /** Default implementation of UserService
  * @author joeblack
  * @since 2003-9-29 17:21:32
  *
  * Class
  */
 public abstract class AbstractUserService
     extends BaseService
     implements UserService {
     private final class UserEntry {
         private long latestAccess;
         private User user;
         private UserEntry(User usr) {
             this.user = usr;
             latestAccess = System.currentTimeMillis();
         }
         private boolean isExpired() {
             return (!user.isAnonymous())
                 && System.currentTimeMillis() - latestAccess > userTimeout;
         }
     }
     private final class CheckingThread implements Runnable {
         public void run() {
             long checkInteval =
                 configuration.getLong("user.inteval", DEFAULT_USER_INTEVAL);
             boolean running = true;
             while (running) {
                 try {
                     ArrayList tobeRemoved = new ArrayList();
                     Iterator i = userRepo.keySet().iterator();
                     while (i.hasNext()) {
                         String key = (String) i.next();
                         UserEntry entry = (UserEntry) userRepo.get(key);
                         if (entry.isExpired()) {
                             tobeRemoved.add(key);
                         }
                     }
                     for (i = tobeRemoved.iterator(); i.hasNext();) {
                         String key = (String) i.next();
                         userRepo.remove(key);
                     }
                     Thread.sleep(checkInteval);
                 } catch (InterruptedException e) {
                     running = false;
                 }
             }
         }
     }
     private static final long DEFAULT_USER_TIMEOUT = 1800000L;
     private static final long DEFAULT_USER_INTEVAL = 1000L;
     private User anonymousUser;
     private Hashtable userRepo = new Hashtable();
     private long userTimeout;
     private Thread checkingThread;
     /** Method getActiveUser()
      * @see com.cyclops.tornado.services.user.UserService#getActiveUser(java.lang.String)
      */
     public User getActiveUser(String key) {
         if (userRepo.containsKey(key)) {
             UserEntry entry = (UserEntry) userRepo.get(key);
             entry.latestAccess = System.currentTimeMillis();
             return entry.user;
         } else {
             return getAnonymousUser();
         }
     }
     /** Method getActiveUsers()
      * @see com.cyclops.tornado.services.user.UserService#getActiveUsers()
      */
     public User[] getActiveUsers() {
         ArrayList users = new ArrayList();
         for (Iterator i = userRepo.values().iterator(); i.hasNext();) {
             UserEntry entry = (UserEntry) i.next();
             users.add(entry.user);
         }
         return (User[]) users.toArray(User.EMPTY_ARRAY);
     }
     /** Method getAnonymousUser()
      * @see com.cyclops.tornado.services.user.UserService#getAnonymousUser()
      */
     public User getAnonymousUser() {
         return anonymousUser;
     }
     /** Method initialize()
      * @see com.cyclops.tornado.services.BaseService#initialize(org.apache.commons.configuration.Configuration)
      */
     protected void initialize(Configuration conf) throws Exception {
         String anonymousUserName = conf.getString("user.anonymous", "guest");
         userTimeout = conf.getLong("user.timeout", DEFAULT_USER_TIMEOUT);
         anonymousUser = loadUser(anonymousUserName, true);
         userRepo.put(anonymousUser.getName(), new UserEntry(anonymousUser));
         checkingThread = new Thread(new CheckingThread());
         checkingThread.setDaemon(true);
         checkingThread.setPriority(Thread.MIN_PRIORITY);
         checkingThread.start();
     }
     /** In default implementation, it load user object from database.
      * New implementation of this class can override this method to make different authorization way.
      * @param userName Name of the user
      * @param isAnonymous If this user is anonymous user
      * @return User object, null if loading failed
      * @throws Exception DB exception
      */
     protected User loadUser(String userName, boolean isAnonymous)
         throws Exception {
         String userClassName =
             configuration.getString(
                "user.classname",
                 DefaultUser.class.getName());
         User du = (User) Class.forName(userClassName).newInstance();
         du.setName(userName);
         du.setAnonymous(isAnonymous);
         return du;
     }
     /** Method shutdown()
      * @see org.apache.fulcrum.Service#shutdown()
      */
     public void shutdown() {
         checkingThread.stop();
     }
     /** Method singin()
      * @see com.cyclops.tornado.services.user.UserService#singin(java.lang.String, java.lang.String)
      */
     public void singin(String key, String userName) {
         boolean existed = false;
         for (Iterator i = userRepo.values().iterator(); i.hasNext();) {
             UserEntry entry = (UserEntry) i.next();
             if (StringUtils.equals(userName, entry.user.getName())) {
                 existed = true;
                 userRepo.put(key, entry);
                 entry.latestAccess = System.currentTimeMillis();
                 break;
             }
         }
         if (!existed) {
             try {
                 User user = loadUser(userName, false);
                 UserEntry entry = new UserEntry(user);
                 userRepo.put(key, entry);
             } catch (Exception e) {
                 logger.error("Loading User Error", e);
             }
         }
     }
     /** Method singout()
      * @see com.cyclops.tornado.services.user.UserService#singout(java.lang.String)
      */
     public void singout(String key) {
         userRepo.remove(key);
     }
     /** Method checkUser()
      * @see com.cyclops.tornado.services.user.UserService#checkUser(java.lang.String, java.lang.String)
      */
     public abstract int checkUser(String userName, String password);
 }
