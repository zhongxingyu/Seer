 /**
  * Rig Client Commons.
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
  * @date 18th July 2010
  */
 
 package au.edu.labshare.rigclient.action.access;
 
import java.io.BufferedReader;
import java.io.InputStreamReader;

 import au.edu.uts.eng.remotelabs.rigclient.rig.IAccessAction;
 import au.edu.uts.eng.remotelabs.rigclient.util.ILogger;
 import au.edu.uts.eng.remotelabs.rigclient.util.LoggerFactory;
 
 /**
  * Kills the processes of a user by invoking the command
  * <tt>pkill -u &lt;name&gt;</tt>.
  */
 public class UNIXLogoffAccessAction implements IAccessAction
 {
     /** Failure reason. */
     private String failureReason;
     
     /** Logger. */
     private ILogger logger;
     
     public UNIXLogoffAccessAction()
     {
         this.logger = LoggerFactory.getLoggerInstance();
     }
     
     @Override
     public boolean revoke(String name)
     {
         try
         {
            ProcessBuilder builder = new ProcessBuilder("id", "-u", name);
             Process proc = builder.start();
            String uid = new BufferedReader(new InputStreamReader(proc.getInputStream())).readLine();
            proc.waitFor();
            
            builder = new ProcessBuilder("pkill", "-u", uid);
            proc = builder.start();
             proc.waitFor();
             this.logger.debug("Forced logoff by killing " + name + "'s processes.");
             return true;
         }
         catch (Exception e)
         {
             this.logger.error("Failed to kill " + name + " processes because of invocation exception. Type: " + 
                     e.getClass().getName() +", message:" + e.getMessage() + ".");
             this.failureReason = "Invocation exception, type: " + e.getClass().getName() +", message:" 
                     + e.getMessage() + ".";
             return false;
         }
     }
     
     @Override
     public String getFailureReason()
     {
         return this.failureReason;
     }
 
     @Override
     public boolean assign(String name)
     {
         return true;
     }
 
     @Override
     public String getActionType()
     {
         return "UNIX logoff access action.";
     }
 }
