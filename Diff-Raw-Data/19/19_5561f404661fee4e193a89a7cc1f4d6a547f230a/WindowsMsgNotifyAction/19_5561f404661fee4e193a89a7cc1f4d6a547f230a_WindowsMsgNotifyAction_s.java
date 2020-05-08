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
  * @author Tania Machet (tmachet)
  * @date 26th February 2010
  *
  * Changelog:
  * - 26/02/2010 - tmachet - Initial file creation.
  */
 package au.edu.uts.eng.remotelabs.rigclient.action.notify;
 
 import java.util.Arrays;
 import java.util.List;
 
 import au.edu.uts.eng.remotelabs.rigclient.rig.INotifyAction;
 import au.edu.uts.eng.remotelabs.rigclient.util.ILogger;
 import au.edu.uts.eng.remotelabs.rigclient.util.LoggerFactory;
 
 /**
  * This class allows notification to be sent to users for Windows Users.
  * The notification is sent using the <code>msg</code> command.
  * The implementation assumes that the messenger service is enabled on the computer.
  * <p>
  * @author tmachet
  */
 public class WindowsMsgNotifyAction implements INotifyAction
 {
     /** Default command for changing user groups for windows */
     public static final String DEFAULT_COMMAND = "msg";
 
     /** Default user group for remote desktop access. */
     private String failureReason;
 
     /** Logger. */
     protected ILogger logger;
 
     /**
      * Constructor.
      */
     public WindowsMsgNotifyAction()
     {
         this.logger = LoggerFactory.getLoggerInstance();
 
         /* Windows notification only valid for windows and not for Vista */
         if (System.getProperty("os.name").startsWith("Windows"))
         {
             this.logger.debug("Preparing to send notification.");
         }
         else
         {
             this.logger.error("Unable to instantiate the Windows Message Notify Action (" + this.getClass().getName() 
                     + ") becuase the detected platform is not Windows. Detected platform is '" + 
                     System.getProperty("os.name") + "'.");
             throw new IllegalStateException("Windows Message Notify Action is only valid for a WINDOWS platforms not " 
                     + System.getProperty("os.name") + '.');
         }
     }
 
     @Override
     public boolean notify(String message, String[] users)
     {
         synchronized (this)
         {
             ProcessBuilder builder = new ProcessBuilder();
             List<String> command = builder.command();
             command.add(WindowsMsgNotifyAction.DEFAULT_COMMAND);
 
             try
             {
                 for (String us : users)
                 {
                     command.add(us);
                     command.add(message);
                     
                     this.logger.debug("Command to be executed is " + command.toString());
                     int result = builder.start().waitFor();
                     
                     if (result != 0)
                     {
                         this.logger.debug("Attempt to send notification message " + message + " to the user " 
                                 + us + " failed with error code " + result + '.');
                     }
 
                     command.remove(message);
                     command.remove(us);
                 }
                 return true;
             }
             catch (final Exception e)
             {
                 this.failureReason = "Unable to send Windows notification " + message + " to all users";
                 this.logger.info("Attempt to send notification meassage " + message + " to the users " 
                         + Arrays.toString(users) + " failed with error " + e.getMessage() + '.');
                 return false;
             }
         }
     }
 
     @Override
     public String getActionType()
     {
         return "Windows Message Notification Action";
     }
 
     @Override
     public String getFailureReason()
     {
         return this.failureReason;
     }
 
 }
