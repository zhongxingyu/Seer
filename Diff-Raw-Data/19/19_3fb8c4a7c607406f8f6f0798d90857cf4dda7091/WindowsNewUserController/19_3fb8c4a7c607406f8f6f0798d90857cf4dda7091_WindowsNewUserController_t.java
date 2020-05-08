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
  * @date 25th April 2010
  */
 package au.edu.uts.eng.remotelabs.rigclient.action.access;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 
 import au.edu.uts.eng.remotelabs.rigclient.rig.IRigControl.PrimitiveRequest;
 import au.edu.uts.eng.remotelabs.rigclient.rig.IRigControl.PrimitiveResponse;
 import au.edu.uts.eng.remotelabs.rigclient.rig.primitive.IPrimitiveController;
 
 /**
  * Obtains the user name and password of the user created by the 
  * {@link WindowsNewUserAccessAction}.
  */
@Deprecated
 public class WindowsNewUserController implements IPrimitiveController
 {
     @Override
     public boolean initController()
     {
         return true;
     }
     
     @Override
     public boolean preRoute()
     {
         return true;
     }
     
     /**
      * Returns the user name and password of the user created by the 
      * {@link WindowsNewUserAccessAction} access action.
      * 
      * @param request request
      * @return response
      */
     public PrimitiveResponse getCredentialsAction(PrimitiveRequest request)
     {
         String name = WindowsNewUserAccessAction.getUserName();
         String pass = WindowsNewUserAccessAction.getUserPassword();
         
         PrimitiveResponse resp = new PrimitiveResponse();
         
         if (name == null)
         {
             resp.setSuccessful(false);
             resp.setErrorCode(1);
             resp.setErrorReason("No user name is set.");
         }
         else if (pass == null)
         {
             resp.setSuccessful(false);
             resp.setErrorCode(2);
             resp.setErrorReason("No password is set.");
         }
         else
         {
             resp.setSuccessful(true);
             resp.addResult("username", name);
             resp.addResult("password", pass);
         }
         return resp;
     }
     
     /**
      * Forces logoff of the user who was created by the {@link WindowsNewUserAccessAction}
      * access action.
      * 
      * @param request 
      * @return response
      */
     public PrimitiveResponse forceLogoffAction(PrimitiveRequest request)
     {
         String name = WindowsNewUserAccessAction.getUserName();
         
         PrimitiveResponse response = new PrimitiveResponse();
         response.setSuccessful(true);
         
         
         if (name == null)
         {
             /* No user was created by WindowsNewUserAccessAction. */
             response.setErrorCode(1);
             response.setSuccessful(false);
             response.setErrorReason("No user created by WindowsNewUserAccessAction.");
             return response;
         }
         
         try
         {
             /* 1) Find in progress sessions. */
             Process proc = new ProcessBuilder("qwinsta", name).start();
             proc.waitFor();
             BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
 
             String line = null;
             while (br.ready() && (line = br.readLine()) != null)
             {
                 if (line.contains(name))
                 {
                     final String qwinstaSplit[] = line.split("\\s+");
                     String logoffID = null;
 
                     for (int i = 0; i < qwinstaSplit.length - 1; i++)
                     {
                         if (qwinstaSplit[i].trim().equals(name))
                         {
                             logoffID = qwinstaSplit[i + 1]; 
                         }
                     }
                     if (logoffID == null) continue;
 
                     /* Run logoff for the detected user. */
                     new ProcessBuilder("logoff", logoffID).start().waitFor();
                 }
             }
             br.close();
         }
         catch (Exception ex)
         {
             response.setSuccessful(false);
             response.setErrorCode(2);
             response.setErrorReason("Exception: '" + ex.getClass().getName() + "', message: '" + ex.getMessage() + "'.");
         }
 
         return response;
     }
     
     @Override
     public boolean postRoute()
     {
        return true;
     }
     
     @Override
     public void cleanup()
     {
         /* Nothing to clean up. */
     }
 }
