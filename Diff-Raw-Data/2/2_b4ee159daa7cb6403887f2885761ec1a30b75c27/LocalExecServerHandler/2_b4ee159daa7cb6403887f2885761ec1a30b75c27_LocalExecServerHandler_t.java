 /**
  * Copyright 2009, Frederic Bregier, and individual contributors by the @author
  * tags. See the COPYRIGHT.txt in the distribution for a full listing of
  * individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it under the
  * terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 3.0 of the License, or (at your option)
  * any later version.
  *
  * This software is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this software; if not, write to the Free Software Foundation,
  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
  * site: http://www.fsf.org.
  */
 package goldengate.commandexec.server;
 
 import goldengate.commandexec.utils.LocalExecDefaultResult;
 import goldengate.common.logging.GgInternalLogger;
 import goldengate.common.logging.GgInternalLoggerFactory;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.nio.channels.CancelledKeyException;
 import java.nio.channels.ClosedChannelException;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.RejectedExecutionException;
 
 import org.apache.commons.exec.CommandLine;
 import org.apache.commons.exec.DefaultExecutor;
 import org.apache.commons.exec.ExecuteException;
 import org.apache.commons.exec.ExecuteWatchdog;
 import org.apache.commons.exec.PumpStreamHandler;
 
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ChannelStateEvent;
 import org.jboss.netty.channel.Channels;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
 
 /**
  * Handles a server-side channel for LocalExec.
  *
  *
  */
 public class LocalExecServerHandler extends SimpleChannelUpstreamHandler {
     // Fixed delay, but could change if necessary at construction
     private long delay = LocalExecDefaultResult.MAXWAITPROCESS;
     protected LocalExecServerPipelineFactory factory = null;
     static protected boolean isShutdown = false;
 
     /**
      * Internal Logger
      */
     private static final GgInternalLogger logger = GgInternalLoggerFactory
             .getLogger(LocalExecServerHandler.class);
 
     protected boolean answered = false;
 
     /**
      * Is the Local Exec Server going Shutdown
      * @param channel associated channel
      * @return True if in Shutdown
      */
     public static boolean isShutdown(Channel channel) {
         if (isShutdown) {
             channel.write(LocalExecDefaultResult.ConnectionRefused.result);
             channel.write(LocalExecDefaultResult.ENDOFCOMMAND).awaitUninterruptibly();
             Channels.close(channel);
             return true;
         }
         return false;
     }
     /**
      * Print stack trace
      * @param thread
      * @param stacks
      */
     static private void printStackTrace(Thread thread, StackTraceElement[] stacks) {
         System.err.print(thread.toString() + " : ");
         for (int i = 0; i < stacks.length-1; i++) {
             System.err.print(stacks[i].toString()+" ");
         }
         System.err.println(stacks[stacks.length-1].toString());
     }
     /**
      * Shutdown thread
      * @author Frederic Bregier
      *
      */
     private static class GGLEThreadShutdown extends Thread {
         long delay = 3000;
         LocalExecServerPipelineFactory factory;
         public GGLEThreadShutdown(LocalExecServerPipelineFactory factory) {
             this.factory = factory;
         }
         /* (non-Javadoc)
          * @see java.lang.Thread#run()
          */
         @Override
         public void run() {
             Timer timer = null;
             timer = new Timer(true);
             GGLETimerTask ggleTimerTask = new GGLETimerTask();
             timer.schedule(ggleTimerTask, delay);
             factory.releaseResources();
             System.exit(0);
         }
 
     }
     /**
      * TimerTask to terminate the server
      * @author Frederic Bregier
      *
      */
     private static class GGLETimerTask extends TimerTask {
         /**
          * Internal Logger
          */
         private static final GgInternalLogger logger = GgInternalLoggerFactory
                 .getLogger(GGLETimerTask.class);
         /*
          * (non-Javadoc)
          *
          * @see java.util.TimerTask#run()
          */
         @Override
         public void run() {
             logger.error("System will force EXIT");
             Map<Thread, StackTraceElement[]> map = Thread
                     .getAllStackTraces();
             for (Thread thread: map.keySet()) {
                 printStackTrace(thread, map.get(thread));
             }
             System.exit(0);
         }
     }
     /**
      * Constructor with a specific delay
      * @param newdelay
      */
     public LocalExecServerHandler(LocalExecServerPipelineFactory factory, long newdelay) {
         this.factory = factory;
         delay = newdelay;
     }
 
     /* (non-Javadoc)
      * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#channelConnected(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChannelStateEvent)
      */
     @Override
     public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
             throws Exception {
         if (isShutdown(ctx.getChannel())) {
             answered = true;
             return;
         }
         answered = false;
         factory.addChannel(ctx.getChannel());
     }
     /* (non-Javadoc)
      * @see org.jboss.netty.channel.SimpleChannelUpstreamHandler#channelDisconnected(org.jboss.netty.channel.ChannelHandlerContext, org.jboss.netty.channel.ChannelStateEvent)
      */
     @Override
     public void channelDisconnected(ChannelHandlerContext ctx,
             ChannelStateEvent e) throws Exception {
         this.factory.removeChannel(e.getChannel());
     }
     /**
      * Change the delay to the specific value. Need to be called before any receive message.
      * @param newdelay
      */
     public void setNewDelay(long newdelay) {
         delay = newdelay;
     }
 
     @Override
     public void messageReceived(ChannelHandlerContext ctx, MessageEvent evt) {
         answered = false;
         // Cast to a String first.
         // We know it is a String because we put some codec in
         // LocalExecPipelineFactory.
         String request = (String) evt.getMessage();
         
         // Generate and write a response.
         String response;
         response = LocalExecDefaultResult.NoStatus.status+" "+
             LocalExecDefaultResult.NoStatus.result;
         boolean isLocallyShutdown = false;
         ExecuteWatchdog watchdog = null;
         try {
             if (request.length() == 0) {
                 // No command
                 response = LocalExecDefaultResult.NoCommand.status+" "+
                     LocalExecDefaultResult.NoCommand.result;
             } else {
                 String[] args = request.split(" ");
                 int cpt = 0;
                 long tempDelay;
                 try {
                     tempDelay = Long.parseLong(args[0]);
                     cpt++;
                 } catch (NumberFormatException e) {
                     tempDelay = delay;
                 }
                 if (tempDelay < 0) {
                     // Shutdown Order
                     isShutdown = true;
                     logger.warn("Shutdown order received");
                     isLocallyShutdown = isShutdown(evt.getChannel());
                     // Wait the specified time
                     try {
                         Thread.sleep(-tempDelay);
                     } catch (InterruptedException e) {
                     }
                     Thread thread = new GGLEThreadShutdown(factory);
                     thread.start();
                     return;
                 }
                 String binary = args[cpt++];
                 File exec = new File(binary);
                 if (exec.isAbsolute()) {
                     // If true file, is it executable
                     if (! exec.canExecute()) {
                         logger.error("Exec command is not executable: " + request);
                         response = LocalExecDefaultResult.NotExecutable.status+" "+
                             LocalExecDefaultResult.NotExecutable.result;
                         return;
                     }
                 }
                 // Create command with parameters
                 CommandLine commandLine = new CommandLine(binary);
                 for (; cpt < args.length; cpt ++) {
                     commandLine.addArgument(args[cpt]);
                 }
                 DefaultExecutor defaultExecutor = new DefaultExecutor();
                 ByteArrayOutputStream outputStream;
                 outputStream = new ByteArrayOutputStream();
                 PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(outputStream);
                 defaultExecutor.setStreamHandler(pumpStreamHandler);
                 int[] correctValues = { 0, 1 };
                 defaultExecutor.setExitValues(correctValues);
                 if (tempDelay > 0) {
                     // If delay (max time), then setup Watchdog
                     watchdog = new ExecuteWatchdog(tempDelay);
                     defaultExecutor.setWatchdog(watchdog);
                 }
                 int status = -1;
                 try {
                     // Execute the command
                     status = defaultExecutor.execute(commandLine);
                 } catch (ExecuteException e) {
                     if (e.getExitValue() == -559038737) {
                         // Cannot run immediately so retry once
                         try {
                             Thread.sleep(LocalExecDefaultResult.RETRYINMS);
                         } catch (InterruptedException e1) {
                         }
                         try {
                             status = defaultExecutor.execute(commandLine);
                         } catch (ExecuteException e1) {
                             pumpStreamHandler.stop();
                             logger.error("Exception: " + e.getMessage() +
                                     " Exec in error with " + commandLine.toString());
                             response = LocalExecDefaultResult.BadExecution.status+" "+
                                 LocalExecDefaultResult.BadExecution.result;
                             try {
                                 outputStream.close();
                             } catch (IOException e2) {
                             }
                             return;
                         } catch (IOException e1) {
                             pumpStreamHandler.stop();
                             logger.error("Exception: " + e.getMessage() +
                                     " Exec in error with " + commandLine.toString());
                             response = LocalExecDefaultResult.BadExecution.status+" "+
                                 LocalExecDefaultResult.BadExecution.result;
                             try {
                                 outputStream.close();
                             } catch (IOException e2) {
                             }
                             return;
                         }
                     } else {
                         pumpStreamHandler.stop();
                         logger.error("Exception: " + e.getMessage() +
                                 " Exec in error with " + commandLine.toString());
                         response = LocalExecDefaultResult.BadExecution.status+" "+
                             LocalExecDefaultResult.BadExecution.result;
                         try {
                             outputStream.close();
                         } catch (IOException e2) {
                         }
                         return;
                     }
                 } catch (IOException e) {
                     pumpStreamHandler.stop();
                     logger.error("Exception: " + e.getMessage() +
                             " Exec in error with " + commandLine.toString());
                     response = LocalExecDefaultResult.BadExecution.status+" "+
                         LocalExecDefaultResult.BadExecution.result;
                     try {
                         outputStream.close();
                     } catch (IOException e2) {
                     }
                     return;
                 }
                 pumpStreamHandler.stop();
                 if (defaultExecutor.isFailure(status) && watchdog != null &&
                         watchdog.killedProcess()) {
                     // kill by the watchdoc (time out)
                     logger.error("Exec is in Time Out");
                     response = LocalExecDefaultResult.TimeOutExecution.status+" "+
                         LocalExecDefaultResult.TimeOutExecution.result;
                     try {
                         outputStream.close();
                     } catch (IOException e2) {
                     }
                 } else {
                     response = status+" "+outputStream.toString();
                     try {
                         outputStream.close();
                     } catch (IOException e2) {
                     }
                 }
             }
         } finally {
             if (isLocallyShutdown) {
                 return;
             }
             // We do not need to write a ChannelBuffer here.
             // We know the encoder inserted at LocalExecPipelineFactory will do the
             // conversion.
             evt.getChannel().write(response+"\n");
             answered = true;
             if (watchdog != null) {
                 watchdog.stop();
             }
            logger.info("End of Command: "+request+" : "+response);
             evt.getChannel().write(LocalExecDefaultResult.ENDOFCOMMAND);
         }
     }
 
     @Override
     public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
         if (answered) {
             logger.debug("Exception while answered: ",e.getCause());
         } else {
             logger.error("Unexpected exception from downstream while not answered.", e
                 .getCause());
         }
         Throwable e1 = e.getCause();
         // Look if Nothing to do since execution will stop later on and
         // an error will occur on client side
         // since no message arrived before close (or partially)
         if (e1 instanceof CancelledKeyException) {
         } else if (e1 instanceof ClosedChannelException) {
         } else if (e1 instanceof NullPointerException) {
             if (e.getChannel().isConnected()) {
                 e.getChannel().close();
             }
         } else if (e1 instanceof IOException) {
             if (e.getChannel().isConnected()) {
                 e.getChannel().close();
             }
         } else if (e1 instanceof RejectedExecutionException) {
             if (e.getChannel().isConnected()) {
                 e.getChannel().close();
             }
         }
     }
 }
