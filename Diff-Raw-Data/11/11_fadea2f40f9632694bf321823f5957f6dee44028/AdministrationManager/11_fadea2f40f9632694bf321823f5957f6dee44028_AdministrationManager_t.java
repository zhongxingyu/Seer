 package polly.core.remote;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 import java.util.regex.Pattern;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.spi.LoggingEvent;
 
 
 import de.skuzzle.polly.sdk.AbstractDisposable;
 import de.skuzzle.polly.sdk.UserManager;
 import de.skuzzle.polly.sdk.exceptions.DisposingException;
 import de.skuzzle.polly.sdk.model.User;
 
 import polly.network.Connection;
 import polly.network.protocol.Constants;
 import polly.network.protocol.Constants.ResponseType;
 import polly.network.protocol.Response;
 import polly.network.util.SerializableFile;
 import polly.core.remote.tcp.ServerConnection;
 
 
 public class AdministrationManager extends AbstractDisposable {
     
     public enum LoginResult {
         SUCCESS, INSUFICCIENT_RIGHTS, UNKNOWN_USER, INVALID_PASSWORD;
     }
     
     private final static File LOG_DIR = new File("./logs");
     private final static Pattern LOG_PATTERN = Pattern.compile(".+\\.log(\\.\\d+)?$");
     private final static Logger logger = Logger.getLogger(
             AdministrationManager.class.getName());
     
     private Set<Connection> liveLogList;
     private Set<Connection> ircForwards;
     private ExecutorService sender;
     private CachedLogAppender logAppender;
     private UserManager userManager;
     
     
     
     public AdministrationManager(UserManager userManager) {
         this.userManager = userManager;
         this.sender = Executors.newFixedThreadPool(1);
         this.liveLogList = new HashSet<Connection>();
         this.ircForwards = new HashSet<Connection>();
         
        this.logAppender = new CachedLogAppender(this, 5);
         Logger.getRootLogger().addAppender(this.logAppender);
     }
     
     
     
     public LoginResult login(Connection connection, String userName, String password) {
         User user = this.userManager.getUser(userName);
         
         if (user == null) {
             return LoginResult.UNKNOWN_USER;
         } else if (!user.checkPassword(password)) {
             return LoginResult.INVALID_PASSWORD;
         } else if (user.getUserLevel() < UserManager.ADMIN) {
             return LoginResult.INSUFICCIENT_RIGHTS;
         } else {
             ((ServerConnection) connection).setUser(user);
             return LoginResult.SUCCESS;
         }
         
     }
     
     
     
     public void logout(Connection connection) {
         ((polly.core.remote.tcp.ServerConnection) connection).setUser(null);
     }
     
     
     
     public void enableIrcForward(Connection connection) {
         synchronized (this.ircForwards) {
             this.ircForwards.add(connection);
             logger.info("IRC forward enabled for " + connection);
         }
     }
     
     
     
     public void disableIrcForward(Connection connection) {
         synchronized (this.ircForwards) {
             this.ircForwards.remove(connection);
             logger.info("IRC forward disabled for " + connection);
         }
     }
     
     
     
     public void enableLiveLog(Connection connection) {
         synchronized (this.liveLogList) {
             this.liveLogList.add(connection);
             this.logAppender.setEnabled(!this.liveLogList.isEmpty());
             logger.info("Live-Log enabled for " + connection);
         }
     }
     
     
     
     public void disableLiveLog(Connection connection) {
         synchronized (this.liveLogList) {
             this.liveLogList.remove(connection);
             this.logAppender.setEnabled(!this.liveLogList.isEmpty());
            logger.info("Live-Log disabled for " + connection);
         }
     }
     
     
     
     public void sendLogs(Connection connection) {
         Response response = new Response(ResponseType.FILE);
         response.getPayload().put(Constants.LOG_LIST, this.listLogFiles());
         
         connection.send(response);
     }
     
     
     
     private List<SerializableFile> listLogFiles() {
         File[] logs = LOG_DIR.listFiles(new FileFilter() {
             
             @Override
             public boolean accept(File file) {
                 String name = file.getName();
                 return LOG_PATTERN.matcher(name).matches(); 
             }
         });
         List<SerializableFile> result = new ArrayList<SerializableFile>(logs.length);
         for (File file : logs) {
             result.add(new SerializableFile(file));
         }
         return result;
     }
     
     
     
     public void processLogCache(List<LoggingEvent> cache) {
         final List<LoggingEvent> copy; 
         synchronized (cache) {
             copy = new ArrayList<LoggingEvent>(cache);
             cache.clear();
         }
         
         this.sender.execute(new Runnable() {
             
             @Override
             public void run() {
                 Response response = new Response(ResponseType.LOG_ITEM);
                 response.getPayload().put(Constants.LOG_LIST, copy);
                 
                 synchronized (liveLogList) {
                     for (Connection connection : liveLogList) {
                         connection.send(response);
                     }
                 }
             }
         });
         
     }
 
 
 
     @Override
     protected void actualDispose() throws DisposingException {
         this.logAppender.setEnabled(false);
         this.logAppender.processLogCache();
         Logger.getRootLogger().removeAppender(this.logAppender);
         this.sender.shutdown();
         try {
             this.sender.awaitTermination(1000, TimeUnit.MILLISECONDS);
         } catch (InterruptedException ignore) {
             ignore.printStackTrace();
         }
         this.liveLogList.clear();
     }
 }
