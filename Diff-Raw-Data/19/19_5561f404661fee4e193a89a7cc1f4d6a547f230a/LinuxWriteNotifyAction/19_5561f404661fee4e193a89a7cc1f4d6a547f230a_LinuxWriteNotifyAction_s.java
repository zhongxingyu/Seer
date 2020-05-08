 /**
  * SAHARA Rig Client
  * 
  * Software abstraction of physical rig to provide rig session control
  * and rig device control. Automatically tests rig hardware and reports
  * the rig status to ensure rig goodness.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2010, University of Technology, Sydney
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright 
  *    notice, this list of conditions and the following disclaimer in the 
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names 
  *    of its contributors may be used to endorse or promote products derived from 
  *    this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * @author Michael Diponio (mdiponio)
  * @date 2nd March 2010
  */
 package au.edu.uts.eng.remotelabs.rigclient.action.notify;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.List;
 
 import au.edu.uts.eng.remotelabs.rigclient.rig.INotifyAction;
 import au.edu.uts.eng.remotelabs.rigclient.util.ConfigFactory;
 import au.edu.uts.eng.remotelabs.rigclient.util.ILogger;
 import au.edu.uts.eng.remotelabs.rigclient.util.LoggerFactory;
 
 /**
  * Notification action which provides an operating system notification using 
  * the 'write' (1) command. 'write' prints a message to a specific users 
  * terminal. The KDE desktop environment provides a write daemon that provides
  * a desktop dialog for write written messages.
  * <br />
  * <strong>NOTE:</strong>The <em>Linux</em> write notification only runs on
  * the Linux operating system. If run on other platforms, an 
  * {@link IllegalStateException} exception thrown on construction.
  */
 public class LinuxWriteNotifyAction implements INotifyAction
 {
     /** Default 'write' command, which must be in path. */
     public static final String DEFAULT_WRITE_COMMAND = "write";
     
     /** 'write' command. */
     private final String writeCommand;
     
     /** Failure reason. */
     private String failureReason;
     
     /** Logger. */
     private final ILogger logger;
     
     /**
      * Constructor which checks the condition that this action may only run on
      * Linux.
      */
     public LinuxWriteNotifyAction()
     {
         this.logger = LoggerFactory.getLoggerInstance();
         if (!System.getProperty("os.name").startsWith("Linux"))
         {
             this.logger.error("The Linux 'write' notification action ('" + this.getClass().getName() + "') can only be " +
                     "used on a Linux Operating System. Detected operating system is " 
                     + System.getProperty("os.name") + '.');
             throw new IllegalStateException("Wrong operating system for Linux 'write' notification action.");
         }
         
         String tmp;
         if ((tmp = ConfigFactory.getInstance().getProperty("Linux_Write_Command")) != null)
         {
             this.writeCommand = tmp;
             this.logger.info("Using the configured write command '" + this.writeCommand + "'.");
         }
         else
         {
             this.writeCommand = LinuxWriteNotifyAction.DEFAULT_WRITE_COMMAND;
             this.logger.info("Using the default write command '" + LinuxWriteNotifyAction.DEFAULT_WRITE_COMMAND + "'.");
         }
     }
 
     @Override
     public boolean notify(final String message, String users[])
     {
         synchronized (this)
         {
             ProcessBuilder procBuilder = new ProcessBuilder(this.writeCommand);
             List<String> command = procBuilder.command();
             
             try
             {
                 for (String us : users)
                 {
                     command.add(us);
                     Process proc = procBuilder.start();
                     
                     /* Empirical wait which should give plenty of time for a program 
                      * to crash out. */
                     Thread.sleep(250);
                     
                     try
                     {
                         proc.exitValue();
                         /* Not throwing a exception means write crashed out, most likely 
                          * because the user doesn't exist as a console user.  */
                         this.logger.debug("Unable to send a console message to " + us + " because they don't have " +
                                 "a console session.");
                     }
                     catch (IllegalThreadStateException thrEx)
                     { 
                         /* This exception being thrown is what we want, it means write is waiting
                          * for a message to send. */
                         PrintWriter writeStdIn = new PrintWriter(proc.getOutputStream());
                         writeStdIn.write(message);
                         writeStdIn.write("\n\n");
 
                         writeStdIn.close();
                         proc.waitFor();
                     }                    
                     
                     command.remove(us);
                 }
                 
                 return true;
             }
             catch (InterruptedException ex)
             {
                 Thread.currentThread().interrupt();
                 return true;
             }
             catch (IOException ex)
             {
                 this.logger.error("Unable to invoke the 'write' command (using " + this.writeCommand + "). Exception " +
                         "message is '" + ex.getMessage() + "'.");
                 this.failureReason = "Failed to invoke write.";
                 return false;
             }
         }
     }
 
     @Override
     public String getFailureReason()
     {
         return this.failureReason;
     }
 
     @Override
 
     public String getActionType()
     {
         return "Linux write notify action";
     }
 }
