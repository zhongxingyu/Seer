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
 
 import static com.google.jstestdriver.runner.RunnerType.BROWSER;
 import static com.google.jstestdriver.runner.RunnerType.CLIENT;
 import static com.google.jstestdriver.runner.RunnerType.STANDALONE;
 import static com.google.jstestdriver.server.handlers.CaptureHandler.RUNNER_TYPE;
 import static java.lang.String.format;
 
 import com.google.jstestdriver.Response.ResponseType;
 import com.google.jstestdriver.commands.NoopCommand;
 import com.google.jstestdriver.model.HandlerPathPrefix;
 import com.google.jstestdriver.runner.RunnerType;
 import com.google.jstestdriver.server.handlers.pages.PageType;
 import com.google.jstestdriver.server.handlers.pages.SlavePageRequest;
 
 import org.joda.time.Instant;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.Collection;
 import java.util.LinkedHashSet;
 import java.util.Set;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicReference;
 
 /**
  * Represents a captured browser, and brokers the interaction between the client
  * and browser.
  * 
  * @author jeremiele@google.com (Jeremie Lenfant-Engelmann)
  */
 public class SlaveBrowser {
 
   public static enum BrowserState {
     CAPTURED, READY, HEARTBEAT, DEAD
   }
 
   private static final LoadedFiles EMPTY_LOADED_FILES = new LoadedFiles();
 
   private static final String CLIENT_CONSOLE_RUNNER = "/slave/id/%s/page/CONSOLE/mode/%s/"
       + SlavePageRequest.TIMEOUT + "/%s/"
       + SlavePageRequest.UPLOAD_SIZE + "/%s/"
       + RUNNER_TYPE + "/" + CLIENT;
 
   private static final String STANDALONE_CONSOLE_RUNNER = "/runner/id/%s/page/RUNNER/mode/%s/"
       + SlavePageRequest.TIMEOUT + "/%s/"
       + SlavePageRequest.UPLOAD_SIZE + "/%s/"
       + RUNNER_TYPE + "/" + STANDALONE;
 
   private static final String BROWSER_CONTROLLED_RUNNER = "/bcr/id/%s/page/" + PageType.VISUAL_STANDALONE_RUNNER + "/mode/%s/"
       + SlavePageRequest.TIMEOUT + "/%s/"
       + SlavePageRequest.UPLOAD_SIZE + "/%s/"
       + RUNNER_TYPE + "/" + BROWSER;
 
 
   private static final Logger LOGGER = LoggerFactory.getLogger(SlaveBrowser.class);
 
   public static final long TIMEOUT = 30000; // 30 seconds
   private static final int POLL_RESPONSE_TIMEOUT = 2;
 
   private final Time time;
   private final String id;
   private final BrowserInfo browserInfo;
   private final BlockingQueue<Command> commandsToRun = new LinkedBlockingQueue<Command>();
   private long dequeueTimeout = 10;
   private TimeUnit timeUnit = TimeUnit.SECONDS;
   private AtomicReference<Instant> lastHeartbeat;
   private Set<FileInfo> fileSet = new LinkedHashSet<FileInfo>();
   private final BlockingQueue<StreamMessage> responses = new LinkedBlockingQueue<StreamMessage>();
   private AtomicReference<Command> commandRunning = new AtomicReference<Command>(null);
   private AtomicReference<Command> lastCommandDequeued = new AtomicReference<Command>(null);
   private final long timeout;
   private final Lock lock = new Lock();
 
   private final HandlerPathPrefix prefix;
 
   private final String mode;
 
   private final RunnerType type;
 
   private AtomicReference<LoadedFiles> lastloadedFiles =
     new AtomicReference<LoadedFiles>(EMPTY_LOADED_FILES);
 
   private AtomicReference<BrowserState> state;
 
   public SlaveBrowser(Time time, String id, BrowserInfo browserInfo, long timeout,
       HandlerPathPrefix prefix, String mode, RunnerType type, BrowserState state) {
     this.time = time;
     this.timeout = timeout;
     this.id = id;
     this.browserInfo = browserInfo;
     this.prefix = prefix;
     this.mode = mode;
     this.type = type;
     this.state = new AtomicReference<BrowserState>(state);
     lastHeartbeat = new AtomicReference<Instant>(new Instant(0));
   }
 
   public String getId() {
     return id;
   }
 
   public Lock getLock() {
     return lock;
   }
 
   public void createCommand(String data) {
     try {
       commandsToRun.put(new Command(data));
     } catch (InterruptedException e) {
       throw new RuntimeException(e);
     }
   }
 
   public Command dequeueCommand() {
     try {
       Command command = commandsToRun.poll(dequeueTimeout, timeUnit);
       LOGGER.debug("dequeue {}", command);
 
       synchronized (this) {
         if (command != null) {
           commandRunning.set(command);
           lastCommandDequeued.set(command);
           return command;
         }
       }
     } catch (InterruptedException e) {
       // The server was killed
     }
     return new NoopCommand();
   }
 
   public Command getLastDequeuedCommand() {
     return lastCommandDequeued.get();
   }
 
   public BrowserInfo getBrowserInfo() {
     return browserInfo;
   }
 
   public void setDequeueTimeout(long dequeueTimeout, TimeUnit timeUnit) {
     this.dequeueTimeout = dequeueTimeout;
     this.timeUnit = timeUnit;
   }
 
   
   
   public synchronized void heartBeat() {
     lastHeartbeat.set(time.now());
     state.set(BrowserState.HEARTBEAT);
 
     browserInfo.setServerReceivedHeartbeat(true);
   }
 
   public Instant getLastHeartbeat() {
     return lastHeartbeat.get();
   }
 
   /** @return true if at least one heartbeat was received from the browser. */
   public boolean receivedHeartbeat() {
     return lastHeartbeat.get().getMillis() != 0;
   }
   
   /** Changes the BrowserState to ready */
   public synchronized void ready() {
     state.set(BrowserState.READY);
     browserInfo.setReady(true);
   }
 
   /**
    * @return the number of seconds since the last heartbeat, or -1 if no
    *         heartbeat has been received.
    */
   public double getSecondsSinceLastHeartbeat() {
     return !receivedHeartbeat() ? -1
         : ((time.now().getMillis() - lastHeartbeat.get().getMillis()) / 1000.0);
   }
 
   public synchronized void addFiles(Collection<FileInfo> fileSet, LoadedFiles loadedFiles) {
     this.fileSet.removeAll(fileSet);
     this.fileSet.addAll(fileSet);
     this.lastloadedFiles.set(loadedFiles);
   }
 
   public Set<FileInfo> getFileSet() {
     return fileSet;
   }
 
   public void resetFileSet() {
     LOGGER.debug("Resetting fileSet for {}", this);
     synchronized (this) {
       fileSet.clear();
       lastloadedFiles.set(EMPTY_LOADED_FILES);
     }
   }
 
   public StreamMessage getResponse() {
     try {
       StreamMessage message = responses.poll(POLL_RESPONSE_TIMEOUT, TimeUnit.SECONDS);
       if (message == null) {
         LOGGER.trace("responses size {}", responses.size());
         message = new StreamMessage(false, new Response(ResponseType.UNKNOWN.name(), "{}", browserInfo, "", 0l));
       } else {
         LOGGER.trace("returning type {}", message.getResponse().getResponseType());
       }
       return message;
     } catch (InterruptedException e) {
       LOGGER.error("Exception during poll {}", e);
       return new StreamMessage(false, new Response(ResponseType.UNKNOWN.name(), "{}", browserInfo, "", 0l));
     }
   }
 
   public void addResponse(Response response, boolean isLast) {
     if (isLast) {
       commandRunning.set(null);
     }
    LOGGER.debug("adding response type {} done: {}", response.getResponseType(), isLast);
     responses.offer(new StreamMessage(isLast, response));
   }
 
   public void clearResponseQueue() {
     responses.clear();
   }
 
   public boolean isCommandRunning() {
     return commandRunning.get() != null;
   }
 
   public Command getCommandRunning() {
     return commandRunning.get();
   }
 
   public Command peekCommand() {
     return commandsToRun.peek();
   }
 
   public void clearCommandRunning() {
     if (commandRunning != null) {
       commandRunning.set(null);
       commandsToRun.clear();
       responses.clear();
     }
   }
 
   public boolean isAlive() {
     boolean alive = receivedHeartbeat()
         && ((time.now().getMillis() - lastHeartbeat.get().getMillis() < timeout) || timeout == -1);
     if (!alive) {
       state.set(BrowserState.DEAD);
       LOGGER.debug("Browser dead: {}", toString());
     }
     return alive;
   }
 
   public String getCaptureUrl() {
     // TODO(corysmith): refactor the capture path building to a proper builder. Doing it to many places.
     switch (type) {
       case CLIENT:
         return prefix.prefixPath(String.format(CLIENT_CONSOLE_RUNNER, id, mode, timeout, browserInfo.getUploadSize()));
       case STANDALONE:
         return prefix.prefixPath(String.format(STANDALONE_CONSOLE_RUNNER, id, mode, timeout, browserInfo.getUploadSize()));
       case BROWSER:
         return prefix.prefixPath(String.format(BROWSER_CONTROLLED_RUNNER, id, mode, timeout, browserInfo.getUploadSize()));
     }
     throw new UnsupportedOperationException("Unsupported Runner type: " + type);
   }
 
   @Override
   public String toString() {
     return format("SlaveBrowser(browserInfo=%s,\n\tid=%s,\n\tsinceLastCheck=%ss,\n\ttimeout=%s)",
         browserInfo, id, getSecondsSinceLastHeartbeat(), timeout);
   }
 
   public LoadedFiles getLastLoadedFiles() {
     return lastloadedFiles.get();
   }
 }
