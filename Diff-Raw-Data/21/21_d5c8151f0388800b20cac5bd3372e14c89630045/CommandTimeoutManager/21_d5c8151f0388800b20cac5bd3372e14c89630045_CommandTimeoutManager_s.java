 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.vtlabs.rtpproxy.command;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author mhack
  */
 public class CommandTimeoutManager {
 
     private Logger log = LoggerFactory.getLogger(CommandTimeoutManager.class);
     /**
      * Map to hold 'cookie' -> 'command' maps.
      */
     private HashMap<String, Command> pendingCommandMap;
     /**
      * Map to hold 'command' -> 'timeout task future object' maps.
      */
     private HashMap<Command, ScheduledFuture<CommandTimeoutTask>> timeoutFutureMap;
     private List<CommandListener> listeners;
     private ScheduledThreadPoolExecutor executor;
     private long commandTimeout;
 
     public CommandTimeoutManager(ScheduledThreadPoolExecutor executor,
             long commandTimeout) {
         this.executor = executor;
         this.commandTimeout = commandTimeout;
         pendingCommandMap = new HashMap<String, Command>();
         timeoutFutureMap = new HashMap<Command, ScheduledFuture<CommandTimeoutTask>>();
         listeners = new ArrayList<CommandListener>();
     }
 
     public void addPendingCommand(Command command) {
         if (log.isDebugEnabled()) {
             StringBuilder sb = new StringBuilder("Adding timeout for command ");
             sb.append(command);
             log.debug(sb.toString());
         }
 
         synchronized (pendingCommandMap) {
             pendingCommandMap.put(command.getCookie(), command);
         }
 
         CommandTimeoutTask timeoutTask = new CommandTimeoutTask(command, this);
         synchronized (timeoutFutureMap) {
            ScheduledFuture<CommandTimeoutTask> future = (ScheduledFuture<CommandTimeoutTask>) executor.schedule(timeoutTask,
                    commandTimeout,
                    TimeUnit.MILLISECONDS);
 
             timeoutFutureMap.put(command, future);
         }
     }
 
     public Command removePendingCommand(String cookie) {
         Command command;
         synchronized (pendingCommandMap) {
             command = pendingCommandMap.remove(cookie);
         }
 
         if (command != null) {
             if (log.isDebugEnabled()) {
                 StringBuilder sb = new StringBuilder("Removing timeout for ");
                 sb.append(command);
                 log.debug(sb.toString());
             }
 
             boolean wasCanceled = false;
            CommandTimeoutTask task = null;
 
             synchronized (timeoutFutureMap) {
                 ScheduledFuture<CommandTimeoutTask> future =
                         timeoutFutureMap.remove(command);
                 wasCanceled = future.cancel(false);
             }
 
            if (wasCanceled && log.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder("Timeout sucessful ");
                sb.append("canceled for command ").append(command);
                log.debug(sb.toString());
                 
             } else {
                 StringBuilder sb = new StringBuilder("Timeout couldn't be ");
                 sb.append("canceled for command").append(command);
                 log.warn(sb.toString());
             }
 
         } else {
             StringBuilder sb = new StringBuilder("Command not found for ");
             sb.append("cookie \'").append(cookie).append("\'");
             log.warn(sb.toString());
         }
 
         return command;
     }
 
     public void addListener(CommandListener listener) {
         synchronized (listeners) {
             listeners.add(listener);
         }
     }
 
     public void removeListener(CommandListener listener) {
         synchronized (listeners) {
             listeners.remove(listener);
         }
     }
 
     public void terminate() {
         synchronized(listeners) {
             listeners.clear();
         }
 
         synchronized(pendingCommandMap) {
             pendingCommandMap.clear();
         }
 
         synchronized(timeoutFutureMap) {
             timeoutFutureMap.clear();
         }
 
         executor = null;
     }
 }
