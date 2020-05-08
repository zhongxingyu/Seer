 package org.ethelred.mymailtool2;
 
 import com.google.common.annotations.VisibleForTesting;
 import com.google.common.base.Joiner;
 import com.google.common.base.Strings;
 import com.google.common.collect.Maps;
 import java.io.Console;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.logging.*;
 import javax.mail.Authenticator;
 import javax.mail.Folder;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.PasswordAuthentication;
 import javax.mail.Session;
 import javax.mail.Store;
 
 import com.google.common.collect.Sets;
 import org.joda.time.DateTime;
 import org.joda.time.Days;
 import org.joda.time.Period;
 import org.joda.time.ReadablePeriod;
 import org.joda.time.format.PeriodFormat;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 
 /**
  *
  * @author edward
  */
 public class Main implements MailToolContext
 {   
 
     @VisibleForTesting
     MailToolConfiguration config;
 
     private Store store;
     
     private int opCount = 0;
     
     protected Map<String, Folder> folderCache;
     
     protected static final ReadablePeriod DEFAULT_MIN_AGE = Days.days(30);
     private DateTime ageCompare;
     private long startTime;
     private long timeLimit = -1;
     private volatile boolean shutdown = false;
     private int operationLimit = -1;
 
     private void init(String[] args) {
         Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHook()));
         CommandLineConfiguration clc = new CommandLineConfiguration();
         CmdLineParser parser = new CmdLineParser(clc);
         try {
 
             parser.parseArgument(args);
 
 
             if (clc.isShowUsage()) {
                 parser.printUsage(System.err);
                 System.exit(1);
             }
             
             SystemPropertiesConfiguration spc = new SystemPropertiesConfiguration();
             
             DefaultConfiguration dc = new DefaultConfiguration();
             
             CompositeConfiguration temp = new CompositeConfiguration(clc, spc, dc);
             
             for(String fileLocation: temp.getFileLocations())
             {
                 FileConfigurationHelper.loadFileConfiguration(temp, fileLocation);
             }
 
             validateRequiredConfiguration(temp);
             
             config = temp;
             
         /*} catch (IOException ex) {
             Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
 */
         } catch (CmdLineException ex) {
             Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
             parser.printUsage(System.err);
             System.exit(1);
         }
     }
 
     private void validateRequiredConfiguration(MailToolConfiguration configuration) throws CmdLineException
     {
         Map<String, String> mailProperties = configuration.getMailProperties();
         if(mailProperties == null || mailProperties.isEmpty())
         {
             throw new CmdLineException("Missing mail properties - have you set up a config file?");
         }
         Set<String> missingProperties = Sets.newHashSet();
         for(String key: MailToolConfiguration.ALL_MAIL_PROPERTIES)
         {
             if(!mailProperties.containsKey(key))
             {
                 missingProperties.add(key);
             }
         }
         if(!missingProperties.isEmpty())
         {
             throw new CmdLineException("Missing mail properties " + Joiner.on(',').join(missingProperties));
         }
     }
 
     private void run() {
         try {
 
             connect();
             
             Task t = config.getTask();
             t.init(this);
             t.run();
         }
         catch(OperationLimitException e)
         {
             Logger.getLogger(Main.class.getName()).log(Level.INFO, "Reached Operation Limit");
         }
         catch(Exception e)
         {
             Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, e);
             
         } finally {
             disconnect();
         }
 
     }
        
     private synchronized void connect() {
         try {
             Session session = Session.getDefaultInstance(mapAsProperties(config.getMailProperties()), new MyAuthenticator());
             store = session.getStore();
             store.connect();

            startTime = System.currentTimeMillis();
             
             folderCache = Maps.newHashMap();
             System.out.printf("Connected to %s%n", config.getMailProperties().get(MailToolConfiguration.HOST));
 
         /*} catch (NoSuchProviderException ex) {
             Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);*/
         } catch (MessagingException ex) {
             Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
 
         }
     }
            
            
     private synchronized void disconnect() {
         if (store != null) {
             try {
                 store.close();
                 store = null;
                 System.out.printf("Disconnected%n");
             } catch (MessagingException ex) {
                 Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 
     private Properties mapAsProperties(Map<String, String> mailProperties)
     {
         Properties p = new Properties();
         for(Map.Entry<String, String> e: mailProperties.entrySet())
         {
             System.out.printf("Mail property: %s => %s%n", e.getKey(), e.getValue());
             p.setProperty(e.getKey(), e.getValue());
         }
         return p;
     }
 
     @Override
     public void countOperation()
     {
         if(++opCount > getOperationLimit())
         {
             throw new OperationLimitException();
         }
 
         if(getTimeLimit() > 0 && startTime > 0 && (System.currentTimeMillis() - startTime) > getTimeLimit())
         {
             throw new OperationLimitException();
         }
 
         if(shutdown)
         {
             throw new RuntimeException("Shutdown");
         }
 
     }
 
     private int getOperationLimit()
     {
         if(operationLimit > -1)
         {
             return operationLimit;
         }
 
        System.out.printf("getOperationLimit%n");
         int result = config.getOperationLimit();
         System.out.printf("Operation limit %s%n", result);
         operationLimit = result;
         return operationLimit;
     }
 
     private long getTimeLimit()
     {
         if(timeLimit > -1)
         {
             return timeLimit;
         }
         String timeLimitSpec = config.getTimeLimit();
         long newTimeLimit = 0;
         if(!(Strings.isNullOrEmpty(timeLimitSpec)))
         {
             Period p = PeriodFormat.getDefault().parsePeriod(timeLimitSpec);
             newTimeLimit = p.toStandardDuration().getMillis();
         }
        System.out.printf("Time limit %sms%n", newTimeLimit);
         timeLimit = newTimeLimit;
         return timeLimit;
     }
 
     @Override
     public boolean isOldEnough(Message m) throws MessagingException {
         DateTime received = new DateTime(m.getReceivedDate());
         return received.isBefore(getAgeCompare());
     }
 
     @VisibleForTesting
     DateTime getAgeCompare()
     {
         if(ageCompare != null)
         {
             return ageCompare;
         }
         
         try {
             Period p = PeriodFormat.getDefault().parsePeriod(config.getMinAge());
             ageCompare = new DateTime().minus(p);
         } catch (Exception ex) {
             Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
             ageCompare = new DateTime().minus(DEFAULT_MIN_AGE);
         }
         
         return ageCompare;
     }
 
     private class MyAuthenticator extends Authenticator {
 
         @Override
         protected PasswordAuthentication getPasswordAuthentication() {
             String password = config.getPassword();
             if (password == null) {
                 password = readPassword();
             }
             return new PasswordAuthentication(config.getUser(), password);
         }
 
         private String readPassword() {
             Console cons;
             if ((cons = System.console()) != null) {
                 return new String(cons.readPassword("Please enter your password for %s at %s%n", config.getUser(), this.getRequestingSite()));
             }
             return null;
         }
     }
     
     @Override
     public Folder getFolder(String folderName) {
         try {
             Folder result;
             //try cache
             result = folderCache.get(folderName);
             if (result != null) {
                 return result;
             }
             //now store
             result = store.getFolder(folderName);
             if (result != null) {
                 if (!result.exists()) {
                     result.create(Folder.HOLDS_FOLDERS | Folder.HOLDS_MESSAGES);
                 }
                 folderCache.put(folderName, result);
                 return result;
             }
         } catch (MessagingException ex) {
             Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
         }
         return null;
     }
     
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         Logger root = Logger.getLogger("");
         root.setLevel(Level.ALL);
         Main app = new Main();
         app.init(args);
         app.run();
 
     }
 
     private class ShutdownHook implements Runnable
     {
         @Override
         public void run()
         {
             disconnect();
             shutdown = true;
         }
     }
 }
