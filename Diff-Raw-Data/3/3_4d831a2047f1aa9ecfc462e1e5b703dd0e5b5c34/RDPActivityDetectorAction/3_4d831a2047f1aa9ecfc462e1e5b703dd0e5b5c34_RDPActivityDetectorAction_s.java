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
  * @date 1st March 2010
  *
  * Changelog:
  * - 01/03/2010 - tmachet - Initial file creation.
  */
 package au.edu.labshare.rigclient.action.detect;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import au.edu.uts.eng.remotelabs.rigclient.rig.IActivityDetectorAction;
 import au.edu.uts.eng.remotelabs.rigclient.util.ILogger;
 import au.edu.uts.eng.remotelabs.rigclient.util.LoggerFactory;
 
 /**
  * This class checks for activity for a rig that uses Remote Desktop for access.  If a user has
  * a session active on the Remote Desktop machine, the rig is considered to be in use. If there is 
  * no active session, the rig is considered to be unused.
  * <p>
  * An active session is determined using the <code>qwinsta</Code> command.  Any session with a 
  * STATE = "Active" is considered valid for determining a rig is in use.
  * <p>
  * This command is only valid for Windows systems
  * <p>
  */
 public class RDPActivityDetectorAction implements IActivityDetectorAction
 {
     /** String to indicate a session is "Active" from qwinsta output */
     public static final String ACTIVE_STATE = "Active";
 
     /** Default command for querying session in Windows */
     public static final String DEFAULT_COMMAND = "qwinsta";
 
     /** Default user group for remote desktop access. */
     private String failureReason;
     
     /** Logger. */
     protected ILogger logger;
     
     public RDPActivityDetectorAction()
     {
         this.logger = LoggerFactory.getLoggerInstance();
 
         /* Windows notification only valid for windows and not for Vista */
         if (System.getProperty("os.name").startsWith("Windows"))
         {
             this.logger.debug("Preparing to detect RDP activity.");
         }
         else
         {
             this.logger.error("Unable to instantiate the Remote Desktop Activity Detector Action (" 
                     + this.getClass().getName() + ") becuase the detected platform is not Windows. Detected platform is '"
                     + System.getProperty("os.name") + "'.");
             throw new IllegalStateException("RDP Activity Detector Action is only valid for a WINDOWS platforms not " 
                     + System.getProperty("os.name") + '.');
         }
     }
 
     @Override
     public boolean detectActivity()
     {
         boolean isActive = false;
         try{
             ProcessBuilder builder = new ProcessBuilder(RDPActivityDetectorAction.DEFAULT_COMMAND);
             Process proc = builder.start();
     
             if (proc.waitFor() != 0)
             {
                 this.failureReason = "RDP Activity Detector Action failed";
                 this.logger.warn("The RDP Activity Detector Action has failed with the following error: " 
                         + proc.exitValue() + ". Activity Detection cannot be determined.");
                 return false;
             }
             
             InputStream is = proc.getInputStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(is));
             String line = null;
     
             while (br.ready() && (line = br.readLine()) != null)
             {
                 if (line.contains(RDPActivityDetectorAction.ACTIVE_STATE))
                 {
                     isActive = true;
                     this.logger.debug("The RDP Activity Detector Action has found an active session");
                 }
             }
             br.close();
             return isActive;    
         }
         catch (Exception e)
         {
             this.failureReason = "RDP Activity Detector could not check status";
            this.logger.error("The RDP Activity Detector failed with exception " + e.getMessage() + '.');
             return isActive;
         }
     }
 
     @Override
     public String getActionType()
     {
         return "RDP Activity Detector Action";
     }
 
     @Override
     public String getFailureReason()
     {
         return this.failureReason;
     }
 
 }
