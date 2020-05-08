 /*
  * Copyright 2008 Google Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package com.google.jstestdriver;
 
 import static java.lang.String.format;
 
 import org.joda.time.Instant;
 
 import java.util.Collection;
 import java.util.LinkedHashSet;
 import java.util.Set;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.TimeUnit;
 
 /**
  * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
  */
 public class SlaveBrowser {
 
   public static final long TIMEOUT = 15000; // 15 seconds
   private static final int POLL_RESPONSE_TIMEOUT = 2;
 
   private final Time time;
   private final String id;
   private final BrowserInfo browserInfo;
   private final BlockingQueue<Command> commandsToRun = new LinkedBlockingQueue<Command>();
   private long dequeueTimeout = 10;
   private TimeUnit timeUnit = TimeUnit.SECONDS;
   private volatile Instant lastHeartBeat;
   private Set<FileInfo> fileSet = new LinkedHashSet<FileInfo>();
   private final BlockingQueue<CommandResponse> responses =
     new LinkedBlockingQueue<CommandResponse>();
   private Command commandRunning = null;
   private Command lastCommandDequeued;
   private final long timeout;
 
   public SlaveBrowser(Time time, String id, BrowserInfo browserInfo, long timeout) {
     this.time = time;
     this.timeout = timeout;
     lastHeartBeat = time.now();
     this.id = id;
     this.browserInfo = browserInfo;
   }
 
   public String getId() {
     return id;
   }
 
   public static class CommandResponse {
     private Response response;
     private final boolean last;
 
     public CommandResponse(Response response, boolean last) {
       this.response = response;
       this.last = last;
     }
 
     /**
      * @return the response
      */
     public Response getResponse() {
       return response;
     }
 
     /**
      * @return the isLast
      */
     public boolean isLast() {
       return last;
     }
 
     public void setResponse(Response response) {
       this.response = response;
     }
   }
 
   public void createCommand(String data) {
     try {
       commandsToRun.put(new Command(data));
     } catch (InterruptedException e) {
       throw new RuntimeException(e);
     }
   }
 
   public synchronized Command dequeueCommand() {
     try {
       Command command = commandsToRun.poll(dequeueTimeout, timeUnit);
 
       if (command != null) {
         commandRunning = command;
         lastCommandDequeued = command;
       }
       return command;
     } catch (InterruptedException e) {
       // The server was killed
     }
     return null;
   }
 
   public Command getLastDequeuedCommand() {
     return lastCommandDequeued;
   }
   
   public BrowserInfo getBrowserInfo() {
     return browserInfo;
   }
 
   public void setDequeueTimeout(long dequeueTimeout, TimeUnit timeUnit) {
     this.dequeueTimeout = dequeueTimeout;
     this.timeUnit = timeUnit;
   }
 
   public void heartBeat() {
     lastHeartBeat = time.now();
   }
 
   public Instant getLastHeartBeat() {
     return lastHeartBeat;
   }
 
   public void addFiles(Collection<FileInfo> fileSet) {
     this.fileSet.removeAll(fileSet);
     this.fileSet.addAll(fileSet);
   }
 
   public Set<FileInfo> getFileSet() {
     return fileSet;
   }
 
   public synchronized void resetFileSet() {
     fileSet.clear();
   }
 
   public CommandResponse getResponse() {
     try {
       return responses.poll(POLL_RESPONSE_TIMEOUT, TimeUnit.SECONDS);
     } catch (InterruptedException e) {
       return null;
     }
   }
 
   public synchronized void addResponse(Response response, boolean isLast) {
     if (isLast) {
       commandRunning = null;
     }
     responses.offer(new CommandResponse(response, isLast));
   }
 
   public void clearResponseQueue() {
     responses.clear();
   }
 
   public boolean isCommandRunning() {
     return commandRunning != null;
   }
 
   public synchronized Command getCommandRunning() {
     return commandRunning;
   }
 
   public synchronized void removeFiles(Collection<FileSource> errorFiles) {
     Set<FileInfo> filesInfoToRemove = new LinkedHashSet<FileInfo>();
 
     for (FileSource f : errorFiles) {
       for (FileInfo info : fileSet) {
         if (info.getFileName().equals(f.getBasePath())) {
           filesInfoToRemove.add(info);
           break;
         }
       }
     }
     fileSet.removeAll(filesInfoToRemove);
   }
 
   public Command peekCommand() {
     return commandsToRun.peek();
   }
 
   public void clearCommandRunning() {
     if (commandRunning != null) {
       commandRunning = null;
       commandsToRun.clear();
       responses.clear();
     }
   }
 
   public boolean isAlive() {
     return time.now().getMillis() - lastHeartBeat.getMillis() < timeout;
   }
 
   @Override
   public String toString() {
    return format("SlaveBrowser(browserInfo=%s,\nid=%s,\nsinceLastCheck=%s)",
         browserInfo, id, time.now().getMillis() - lastHeartBeat.getMillis());
   }  
 }
