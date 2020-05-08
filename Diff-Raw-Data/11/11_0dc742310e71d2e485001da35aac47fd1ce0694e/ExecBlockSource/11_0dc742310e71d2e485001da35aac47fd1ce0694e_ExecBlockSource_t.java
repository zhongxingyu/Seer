 package org.n3r.flume.source.exec;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.flume.Context;
 import org.apache.flume.Event;
 import org.apache.flume.EventDrivenSource;
 import org.apache.flume.SystemClock;
 import org.apache.flume.channel.ChannelProcessor;
 import org.apache.flume.conf.Configurable;
 import org.apache.flume.event.EventBuilder;
 import org.apache.flume.instrumentation.SourceCounter;
 import org.apache.flume.source.AbstractSource;
 import org.apache.flume.source.ExecSourceConfigurationConstants;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Preconditions;
 import com.google.common.util.concurrent.ThreadFactoryBuilder;
 
 public class ExecBlockSource extends AbstractSource implements EventDrivenSource, Configurable {
 
     private static final Logger logger = LoggerFactory.getLogger(ExecBlockSource.class);
 
     private String shell;
     private String command;
     private SourceCounter sourceCounter;
     private ExecutorService executor;
     private Future<?> runnerFuture;
     private long restartThrottle;
     private boolean restart;
     private boolean logStderr;
     private Integer bufferCount;
     private long bufferTimeout;
     private Pattern boundaryRegex;
     private ExecRunnable runner;
     private Charset charset;
    private static String newline = System.getProperty("line.separator");
 
     @Override
     public void start() {
         logger.info("Execblock source starting with command:{}", command);
 
         executor = Executors.newSingleThreadExecutor();
 
         runner = new ExecRunnable(shell, command, getChannelProcessor(), sourceCounter,
                 restart, restartThrottle, logStderr, bufferCount, bufferTimeout, boundaryRegex, charset);
 
         // FIXME: Use a callback-like executor / future to signal us upon
         // failure.
         runnerFuture = executor.submit(runner);
 
         /*
          * NB: This comes at the end rather than the beginning of the method
          * because it sets our state to running. We want to make sure the
          * executor is alive and well first.
          */
         sourceCounter.start();
         super.start();
 
         logger.debug("Execblock source started");
     }
 
     @Override
     public void stop() {
         logger.info("Stopping execblock source with command:{}", command);
         if (runner != null) {
             runner.setRestart(false);
             runner.kill();
         }
 
         if (runnerFuture != null) {
             logger.debug("Stopping execblock runner");
             runnerFuture.cancel(true);
             logger.debug("Execblock runner stopped");
         }
         executor.shutdown();
 
         while (!executor.isTerminated()) {
             logger.debug("Waiting for execblock executor service to stop");
             try {
                 executor.awaitTermination(500, TimeUnit.MILLISECONDS);
             } catch (InterruptedException e) {
                 logger.debug("Interrupted while waiting for execblock executor service "
                         + "to stop. Just exiting.");
                 Thread.currentThread().interrupt();
             }
         }
 
         sourceCounter.stop();
         super.stop();
 
         logger.debug("Execblock source with command:{} stopped. Metrics:{}", command,
                 sourceCounter);
     }
 
     @Override
     public void configure(Context context) {
         command = context.getString("command");
 
         Preconditions.checkState(command != null,
                 "The parameter command must be specified");
 
         restartThrottle = context.getLong(ExecSourceConfigurationConstants.CONFIG_RESTART_THROTTLE,
                 ExecSourceConfigurationConstants.DEFAULT_RESTART_THROTTLE);
 
         restart = context.getBoolean(ExecSourceConfigurationConstants.CONFIG_RESTART,
                 ExecSourceConfigurationConstants.DEFAULT_RESTART);
 
         logStderr = context.getBoolean(ExecSourceConfigurationConstants.CONFIG_LOG_STDERR,
                 ExecSourceConfigurationConstants.DEFAULT_LOG_STDERR);
 
         bufferCount = context.getInteger(ExecSourceConfigurationConstants.CONFIG_BATCH_SIZE,
                 ExecSourceConfigurationConstants.DEFAULT_BATCH_SIZE);
 
         bufferTimeout = context.getLong(ExecSourceConfigurationConstants.CONFIG_BATCH_TIME_OUT,
                 ExecSourceConfigurationConstants.DEFAULT_BATCH_TIME_OUT);
 
         String regex = context.getString("boundaryRegex");
         boundaryRegex = StringUtils.isEmpty(regex) ? null : Pattern.compile(regex);
 
         charset = Charset.forName(context.getString(ExecSourceConfigurationConstants.CHARSET,
                 ExecSourceConfigurationConstants.DEFAULT_CHARSET));
 
         shell = context.getString(ExecSourceConfigurationConstants.CONFIG_SHELL, null);
 
         if (sourceCounter == null)
             sourceCounter = new SourceCounter(getName());
 
         logger.info("Configuration : restartThrottle=" + restartThrottle + ", restart=" + restart +
                 ", logStderr=" + logStderr + ", bufferCount=" + bufferCount + ", bufferTimeout=" +
                 bufferTimeout + ", boundaryRegex=" + regex + ", charset" + charset + ", shell" + shell);
     }
 
     private static class ExecRunnable implements Runnable {
 
         public ExecRunnable(String shell, String command, ChannelProcessor channelProcessor,
                 SourceCounter sourceCounter, boolean restart, long restartThrottle,
                 boolean logStderr, int bufferCount, long bufferTimeout, Pattern boundaryRegex, Charset charset) {
             this.command = command;
             this.channelProcessor = channelProcessor;
             this.sourceCounter = sourceCounter;
             this.restartThrottle = restartThrottle;
             this.bufferCount = bufferCount;
             this.bufferTimeout = bufferTimeout;
             this.restart = restart;
             this.logStderr = logStderr;
             this.boundaryRegex = boundaryRegex;
             this.charset = charset;
             this.shell = shell;
         }
 
         private final String shell;
         private final String command;
         private final ChannelProcessor channelProcessor;
         private final SourceCounter sourceCounter;
         private volatile boolean restart;
         private final long restartThrottle;
         private final int bufferCount;
         private long bufferTimeout;
         private final boolean logStderr;
         private Pattern boundaryRegex;
         private final Charset charset;
         private Process process = null;
         private SystemClock systemClock = new SystemClock();
         private Long lastPushToChannel = systemClock.currentTimeMillis();
         private ScheduledExecutorService timedFlushService;
         private ScheduledFuture<?> future;
         private List<Event> eventList = new ArrayList<Event>();
         private StringBuilder block = new StringBuilder();
 
         @Override
         public void run() {
             do {
                 String exitCode = "unknown";
                 BufferedReader reader = null;
 
                 try {
                     if (shell != null) {
                         String[] commandArgs = formulateShellCommand(shell, command);
                         process = Runtime.getRuntime().exec(commandArgs);
                     } else {
                         String[] commandArgs = command.split("\\s+");
                         process = new ProcessBuilder(commandArgs).start();
                     }
                     reader = new BufferedReader(
                             new InputStreamReader(process.getInputStream(), charset));
 
                     // StderrLogger dies as soon as the input stream is invalid
                     StderrReader stderrReader = new StderrReader(new BufferedReader(
                             new InputStreamReader(process.getErrorStream(), charset)), logStderr);
                     stderrReader.setName("StderrReader-[" + command + "]");
                     stderrReader.setDaemon(true);
                     stderrReader.start();
 
                     // Start flush thread
                     startTimedFlushService();
 
                     String line = null;
                     while ((line = reader.readLine()) != null) {
                         synchronized (eventList) {
                             processLine(line);
                             if (eventList.size() >= bufferCount || timeout())
                                 flushEventBatch(eventList);
                         }
                     }
 
                     synchronized (eventList) {
                         if (!eventList.isEmpty())
                             flushEventBatch(eventList);
                     }
                 } catch (Exception e) {
                     logger.error("Failed while running command: " + command, e);
                     if (e instanceof InterruptedException)
                         Thread.currentThread().interrupt();
                 } finally {
                     if (reader != null) {
                         try {
                             reader.close();
                         } catch (IOException ex) {
                             logger.error("Failed to close reader for execblock source", ex);
                         }
                     }
                     exitCode = String.valueOf(kill());
                 }
                 if (restart) {
                     logger.info("Restarting in {}ms, exit code {}", restartThrottle,
                             exitCode);
                     try {
                         Thread.sleep(restartThrottle);
                     } catch (InterruptedException e) {
                         Thread.currentThread().interrupt();
                     }
                 } else {
                     logger.info("Command [" + command + "] exited with " + exitCode);
                 }
             } while (restart);
         }
 
         private void startTimedFlushService() {
             timedFlushService = Executors.newSingleThreadScheduledExecutor(
                     new ThreadFactoryBuilder().setNameFormat(
                             "timedFlushExecService" + Thread.currentThread().getId() + "-%d").build());
 
             future = timedFlushService.scheduleWithFixedDelay(new Runnable() {
                 @Override
                 public void run() {
                     try {
                         synchronized (eventList) {
                             if (StringUtils.isNotEmpty(block.toString()))
                                 flushBlock();
 
                             if (!eventList.isEmpty() && timeout()) {
                                 logger.info("Timeout : eventList size is [{}]", eventList.size());
                                 flushEventBatch(eventList);
                             }
                         }
                     } catch (Exception e) {
                         logger.error("Exception occured when flushing event batch", e);
                         if (e instanceof InterruptedException)
                             Thread.currentThread().interrupt();
                     }
                 }
             }, bufferTimeout, bufferTimeout, TimeUnit.MILLISECONDS);
         }
 
         private void processLine(String line) {
             // Line break
             if (null == boundaryRegex) {
                 logger.info("Line : [{}]", line);
                 sourceCounter.incrementEventReceivedCount();
                 eventList.add(EventBuilder.withBody(line, charset));
                 return;
             }
 
             // Block break
             Matcher matcher = boundaryRegex.matcher(line);
             int start = 0;
             while (matcher.find()) {
                 // Incr counter
                 sourceCounter.incrementEventReceivedCount();
                 // Append the tail to last block before flushing it
                 if (matcher.start() > 0) {
                     String tail = line.substring(start, matcher.start());
                     synchronized (block) {
                        if (start == 0 && block.length() > 0) block.append(newline);
                         block.append(tail);
                     }
                     // Reset the index of new block
                     start = matcher.start();
                 }
                 // Flush last block
                 if (StringUtils.isNotEmpty(block.toString()))
                     flushBlock();
             }
             synchronized (block) {
                if (block.length() > 0) block.append(newline);
                 block.append(line.substring(start, line.length()));
             }
         }
 
         private void flushBlock() {
             synchronized (block) {
                 logger.info("Block : [{}]", block.toString());
                 eventList.add(EventBuilder.withBody(block.toString(), charset));
                 block.delete(0, block.length());
             }
         }
 
         private void flushEventBatch(List<Event> eventList) {
             channelProcessor.processEventBatch(eventList);
             sourceCounter.addToEventAcceptedCount(eventList.size());
             eventList.clear();
             lastPushToChannel = systemClock.currentTimeMillis();
         }
 
         private boolean timeout() {
             return (systemClock.currentTimeMillis() - lastPushToChannel) >= bufferTimeout;
         }
 
         private static String[] formulateShellCommand(String shell, String command) {
             String[] shellArgs = shell.split("\\s+");
             String[] result = new String[shellArgs.length + 1];
             System.arraycopy(shellArgs, 0, result, 0, shellArgs.length);
             result[shellArgs.length] = command;
             return result;
         }
 
         public int kill() {
             if (process == null)
                 return Integer.MIN_VALUE / 2;
 
             synchronized (process) {
                 process.destroy();
                 try {
                     int exitValue = process.waitFor();
 
                     // Stop the Thread that flushes periodically
                     if (future != null)
                         future.cancel(true);
 
                     if (timedFlushService != null) {
                         timedFlushService.shutdown();
                         while (!timedFlushService.isTerminated()) {
                             try {
                                 timedFlushService.awaitTermination(500, TimeUnit.MILLISECONDS);
                             } catch (InterruptedException e) {
                                 logger.debug("Interrupted while waiting for execblock executor service "
                                         + "to stop. Just exiting.");
                                 Thread.currentThread().interrupt();
                             }
                         }
                     }
                     return exitValue;
                 } catch (InterruptedException ex) {
                     Thread.currentThread().interrupt();
                 }
             }
             return Integer.MIN_VALUE;
         }
 
         public void setRestart(boolean restart) {
             this.restart = restart;
         }
     }
 
     private static class StderrReader extends Thread {
         private BufferedReader input;
         private boolean logStderr;
 
         protected StderrReader(BufferedReader input, boolean logStderr) {
             this.input = input;
             this.logStderr = logStderr;
         }
 
         @Override
         public void run() {
             try {
                 int i = 0;
                 String line = null;
                 while ((line = input.readLine()) != null) {
                     if (logStderr)
                         // There is no need to read 'line' with a charset
                         // as we do not to propagate it.
                         // It is in UTF-16 and would be printed in UTF-8 format.
                         logger.info("StderrLogger[{}] = '{}'", ++i, line);
                 }
             } catch (IOException e) {
                 logger.info("StderrLogger exiting", e);
             } finally {
                 try {
                     if (input != null)
                         input.close();
                 } catch (IOException ex) {
                     logger.error("Failed to close stderr reader for execblock source", ex);
                 }
             }
         }
     }
 }
