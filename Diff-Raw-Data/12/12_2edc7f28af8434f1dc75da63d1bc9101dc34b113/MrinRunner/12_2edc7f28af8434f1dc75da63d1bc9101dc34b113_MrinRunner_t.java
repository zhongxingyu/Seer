 /*
  * Created by Andrey Markelov 29-08-2012.
  * Copyright Mail.Ru Group 2012. All rights reserved.
  */
 package ru.mail.jira.plugins.mrimsender;
 
 import java.util.ArrayList;
 import java.util.List;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import ru.mail.jira.plugins.mrimsender.mrim.MrimException;
 import ru.mail.jira.plugins.mrimsender.mrim.MrimWorker;
 
 /**
  * This is process that sends MRIM data.
  * 
  * @author Andrey Markelov
  */
 public class MrinRunner
     implements Runnable
 {
     /*
      * Logger.
      */
     private static Log log = LogFactory.getLog(MrinRunner.class);
 
     /**
      * Authorized users.
      */
     private List<String> authorizedUsers;
 
     /*
      * Statuses.
      */
     public static final int NODEF = 0;
     public static final int RUN = 1;
     public static final int FAIL = 2;
 
     /**
      * Singleton instance.
      */
     private static MrinRunner runner = new MrinRunner();
 
     /**
      * Get singleton instance.
      */
     public static MrinRunner getInstance()
     {
         return runner;
     }
 
     /**
      * MRIM worker.
      */
     private MrimWorker ms;
 
     /**
      * Process status.
      */
     private volatile int status;
 
     /**
      * Thread.
      */
     private Thread thread;
 
     /**
      * Local IP address.
      */
     private int localIpAddress;
 
     /**
      * Local port.
      */
     private int port;
 
     /**
      * Private constructor.
      */
     private MrinRunner()
     {
         this.status = NODEF;
         this.authorizedUsers = new ArrayList<String>();
     }
 
     /**
      * Close MRIM sender socket.
      */
     private void CloseMs()
     {
         if (ms == null) return;
 
         try
         {
             ms.close();
         }
         catch (MrimException e)
         {
             log.error("MrinRunner::run", e);
         }
     }
 
     /**
      * Get IP address.
      */
     public int getLocalIpAddress()
     {
         return localIpAddress;
     }
 
     /**
      * Get port.
      */
     public int getPort()
     {
         return port;
     }
 
     /**
      * Check if user authorized.
      */
     public boolean isAuthorized(String user)
     {
         return authorizedUsers.contains(user);
     }
 
     /**
      * Add an authorized user.
      */
     public void addAuthorizedUser(String user)
     {
         authorizedUsers.add(user);
     }
 
     /**
      * Get seq.
      */
     public int getSeq()
     {
         return ms.getMessageSeq();
     }
 
     /**
      * Get process status.
      */
     public int getStatus()
     {
         return status;
     }
 
     /**
      * Initialize new MRIM connection.
      */
     public void init(
         String login,
         String password)
     throws MrimException
     {
         CloseMs();
         authorizedUsers.clear();
         ms = MrimWorker.createMrimSender();
         ms.login(login, password);
 
         localIpAddress = ms.getIpAddress();
         port = ms.getPort();
 
         thread = new Thread(this, "MRIM");
         thread.start();
     }
 
     /**
      * Interrupt MRIM process.
      */
     public void interrupt()
     {
         thread.interrupt();
     }
 
     @Override
     public void run()
     {
         status = RUN;
 
         DataKeeper dk = null;
         try
         {
             while (true)
             {
                 if (thread.isInterrupted())
                 {
                     status = FAIL;
                     break;
                 }
 
                 dk = QueueSingleton.getInstance().poll();
                 if (dk != null)
                 {
                     if (dk.getOperation() == DataKeeper.ADD_CONTACT)
                     {
                         ms.sleep(12000);
                     }
                     else if (dk.getOperation() == DataKeeper.PING)
                     {
                         //--> nothing to do
                     }
                     else
                     {
                        ms.sleep(500);
                     }
                     ms.write(dk.getData());
                 }
                 else
                 {
                     ms.ping();
                 }
 
                 dk = null;
             }
         }
         catch (Exception ex)
         {
             log.warn("MrinRunner::run", ex);
 
             status = FAIL;
 
             if (dk != null && dk.getOperation() != DataKeeper.PING)
             {
                 QueueSingleton.getInstance().put(dk);
             }
         }
     }
 }
