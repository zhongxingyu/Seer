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
  * @author Michael Diponio
  * @date 27th July 2010
  */
 package au.edu.labshare.rigclient.action.detect;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import au.edu.uts.eng.remotelabs.rigclient.rig.IActivityDetectorAction;
 import au.edu.uts.eng.remotelabs.rigclient.util.ILogger;
 import au.edu.uts.eng.remotelabs.rigclient.util.LoggerFactory;
 
 /**
  * Detects activity by invoking the Linux 'who' command to determine if any
 * user is logged in. It does not explicitly check whether the assigned 
  * user is logged in, so any user logged in will cause this test to pass.
  * This class assumes 'who' is in path.
  * <br />
  * This class is only works on Linux operating systems.
  */
 public class LinuxConsoleActivityDetector implements IActivityDetectorAction
 {
     /** The process build to invoke 'who --count'. */
     private final ProcessBuilder builder = new ProcessBuilder("who", "--count");
     
     /** Logger. */
     private ILogger logger;
     
     /**
      * Constructor.
      * 
      * @throws IllegalStateException if trying to instantiate this class on a \ 
      *      non-Linux operating system.
      */
     public LinuxConsoleActivityDetector()
     {
         this.logger = LoggerFactory.getLoggerInstance();
         
         if (!"Linux".equals(System.getProperty("os.name")))
         {
             this.logger.error("The '" + this.getActionType() + "' class can only be used on Linux ('" + 
                     System.getProperty("os.name") + "' detected).");
             throw new IllegalStateException(this.getActionType() + " only works on Linux.");
         }
     }
 
     @Override
     public boolean detectActivity()
     {
         try
         {
             Process p = this.builder.start();
             p.waitFor();
 
             String line;
             BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
 
             while ((line = reader.readLine()) != null)
             {
                 /* The expected format of the 'who' count line is # users=<n>. */
                 if (line.contains("users"))
                 {
                     String chunks[] = line.split("=", 2);
                     if (chunks.length != 2) continue;
                     
                     try
                     {
                         return Integer.parseInt(chunks[1]) >= 1;
                     }
                     catch (NumberFormatException ex)
                     {
                         continue;
                     }
                 }
             }
             
             return false;
         }
         catch (IOException e)
         {
             this.logger.warn("Failed to invoke command " + this.builder.toString() + " because of exception '" 
                     + e.getClass().getName() + "', message '" + e.getMessage() + "'.");
             return true;
         }
         catch (InterruptedException e)
         {
             Thread.currentThread().interrupt();
             return true;
         }
     }
     
     @Override
     public String getFailureReason()
     {
         return null;
     }
 
     @Override
     public String getActionType()
     {
         return "Linux console activity detector";
     }
 
 }
