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
 
  private static final long TIMEOUT = 15000; // 15 seconds
   private static final int POLL_RESPONSE_TIMEOUT = 2;
 
   private final Time time;
   private final String id;
   private final BrowserInfo browserInfo;
   private final BlockingQueue<Command> commandsToRun = new LinkedBlockingQueue<Command>();
   private long timeout = 10;
   private TimeUnit timeUnit = TimeUnit.SECONDS;
   private Instant lastHeartBeat;
   private Set<FileInfo> fileSet = new LinkedHashSet<FileInfo>();
   private final BlockingQueue<CommandResponse> responses =
     new LinkedBlockingQueue<CommandResponse>();
   private Command commandRunning = null;
   private Command lastCommandDequeued;
 
   public SlaveBrowser(Time time, String id, BrowserInfo browserInfo) {
     this.time = time;
     lastHeartBeat = time.now();
     this.id = id;
     this.browserInfo = browserInfo;
   }
 
   public String getId() {
     return id;
   }
 
   public static class CommandResponse {
     private final String response;
     private final boolean last;
 
     public CommandResponse(String response, boolean last) {
       this.response = response;
       this.last = last;
     }
 
     /**
      * @return the response
      */
     public String getResponse() {
       return response;
     }
 
     /**
      * @return the isLast
      */
     public boolean isLast() {
       return last;
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
       Command command = commandsToRun.poll(timeout, timeUnit);
 
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
 
   public void setDequeueTimeout(long timeout, TimeUnit timeUnit) {
     this.timeout = timeout;
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
 
   public void resetFileSet() {
     fileSet.clear();
   }
 
   public CommandResponse getResponse() {
     try {
       return responses.poll(POLL_RESPONSE_TIMEOUT, TimeUnit.SECONDS);
     } catch (InterruptedException e) {
       return null;
     }
   }
 
   public void addResponse(String response, boolean isLast) {
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
 
   public void removeFiles(Collection<FileSource> errorFiles) {
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
     return time.now().getMillis() - lastHeartBeat.getMillis() < TIMEOUT;
   }
 
   @Override
   public String toString() {
     return "SlaveBrowser(browserInfo=" + browserInfo + ", id=" + id + ")";
   }  
 }
