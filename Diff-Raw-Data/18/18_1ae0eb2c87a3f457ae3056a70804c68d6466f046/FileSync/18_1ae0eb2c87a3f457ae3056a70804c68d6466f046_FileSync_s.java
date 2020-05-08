 package com.frugs.filesync;
 
 import com.frugs.filesync.domain.Diff;
 import com.frugs.filesync.local.FileUpdateFacade;
 import com.frugs.filesync.local.LocalFileUpdatePollingService;
 import com.frugs.filesync.local.LocalFileUpdater;
 import com.frugs.filesync.local.LockedDiff;
 import com.frugs.filesync.local.system.FileWriter;
 import com.frugs.filesync.local.system.SystemCommandExecutor;
 import com.frugs.filesync.local.system.SystemCommandFacade;
 import com.frugs.filesync.remote.RemoteFileUpdateReceiver;
 import com.frugs.filesync.remote.RemoteFileUpdateSender;
 import com.frugs.filesync.task.PollLocalUpdatesTask;
 
 import java.net.InetAddress;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.logging.Logger;
 
 import static java.lang.Integer.parseInt;
 import static java.net.InetAddress.getLocalHost;
 import static java.util.concurrent.TimeUnit.MILLISECONDS;
 
 public class FileSync {
 
     //TODO deleteme - lol...
     static class CustomLogger extends Logger {
         public CustomLogger() {
             super(null, null);
         }
 
         @Override public void info(String message) {
            System.out.println(message);
         }
     }
 
     public static void main(String[] args) {
 
         //TODO fix this up a bit later, just trying to see whether it works
         try {
             InetAddress remoteAddress = getLocalHost();
             int localPort = parseInt(args[0]);
             int remotePort = parseInt(args[1]);
 
             Logger logger = new CustomLogger();
 
             SystemCommandExecutor systemCommandExecutor = new SystemCommandExecutor(logger);
             FileWriter fileWriter = new FileWriter();
             SystemCommandFacade systemCommandFacade = new SystemCommandFacade(systemCommandExecutor, fileWriter);
 
             FileUpdateFacade fileUpdateFacade = new FileUpdateFacade(systemCommandFacade);
             LockedDiff lockedDiff = new LockedDiff(new ReentrantLock(), Diff.fromInputStream(systemCommandExecutor.gitDiffHead()));
 
             LocalFileUpdater localFileUpdater = new LocalFileUpdater(lockedDiff, fileUpdateFacade, logger);
             RemoteFileUpdateSender remoteFileUpdateSender = new RemoteFileUpdateSender(remoteAddress, remotePort, logger);
 
             LocalFileUpdatePollingService localFileUpdatePollingService = new LocalFileUpdatePollingService(lockedDiff, fileUpdateFacade, remoteFileUpdateSender, logger);
             RemoteFileUpdateReceiver remoteFileUpdateReceiver = new RemoteFileUpdateReceiver(localPort, localFileUpdater, logger);
 
             Runnable pollLocalUpdatesTask = new PollLocalUpdatesTask(localFileUpdatePollingService);
             startInSingleRepeatingThread(pollLocalUpdatesTask, 500, MILLISECONDS);
 
             while (true) {
                 remoteFileUpdateReceiver.acceptUpdates();
             }
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     private static ScheduledFuture startInSingleRepeatingThread(Runnable task, int repeating, TimeUnit unit) {
         return Executors.newScheduledThreadPool(1).scheduleAtFixedRate(task, 5000, repeating, unit);
     }
 }
