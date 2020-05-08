 /**
  * SAHARA Scheduling Server
  *
  * Schedules and assigns local laboratory rigs.
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
  * @date 4th January 2010
  */
 package au.edu.uts.eng.remotelabs.schedserver.rigprovider.intf;
 
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.Rig;
 import au.edu.uts.eng.remotelabs.schedserver.logger.Logger;
 import au.edu.uts.eng.remotelabs.schedserver.logger.LoggerActivator;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.identok.impl.IdentityTokenRegister;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.impl.RegisterLocalRig;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.impl.RemoveLocalRig;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.impl.UpdateLocalRigStatus;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.intf.types.ProviderResponse;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.intf.types.RegisterRig;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.intf.types.RegisterRigResponse;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.intf.types.RegisterRigType;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.intf.types.RemoveRig;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.intf.types.RemoveRigResponse;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.intf.types.RemoveRigType;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.intf.types.StatusType;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.intf.types.UpdateRigStatus;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.intf.types.UpdateRigStatusResponse;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.intf.types.UpdateRigType;
 
 /**
  * Local rig provider SOAP interface operation implementations.
  */
 public class LocalRigProvider implements LocalRigProviderSkeletonInterface
 {
     /** Logger. */
     private Logger logger;
     
     public LocalRigProvider()
     {
         this.logger = LoggerActivator.getLogger();
     }
     
     @Override
     public RegisterRigResponse registerRig(RegisterRig request)
     {
         /* Request parameters. */
         RegisterRigType rig = request.getRegisterRig();
         StatusType status = rig.getStatus();
         
         this.logger.debug("Received " + this.getClass().getName() + "#registerRig with parameters: name=" + rig.getName()
                 + ", type=" + rig.getType() + ", capabilities=" + rig.getCapabilities() + 
                 ", contact URL=" + rig.getContactUrl().toString() + ", isOnline=" + 
                 status.getIsOnline() + ", offlineReason=" + status.getOfflineReason() + ".");
         
         /* Response parameters. */
         RegisterRigResponse response = new RegisterRigResponse();
         ProviderResponse providerResp = new ProviderResponse();
         response.setRegisterRigResponse(providerResp);
 
         RegisterLocalRig register = new RegisterLocalRig();
         if (register.registerRig(rig.getName(), rig.getType(), rig.getCapabilities(), rig.getContactUrl().toString()))
         {
             Rig registeredRig = register.getRegisteredRig();
             
             /* Rig register so update its status. */
             UpdateLocalRigStatus updater = new UpdateLocalRigStatus(register.getSession());
             if (updater.updateStatus(registeredRig.getName(), status.getIsOnline(), status.getOfflineReason()))
             {
                 providerResp.setSuccessful(true);
                providerResp.setIdentityToken(IdentityTokenRegister.getInstance().generateIdentityToken(
                         registeredRig.getName()));
             }
             else
             {
                 providerResp.setSuccessful(false);
                 providerResp.setErrorReason(updater.getFailedReason());
             }
         }
         else
         {
             providerResp.setSuccessful(false);
             providerResp.setErrorReason(register.getFailedReason());
         }
         
         register.getSession().close();
         return response;
     }
 
     @Override
     public RemoveRigResponse removeRig(RemoveRig request)
     {
         /* Request parameters. */
         RemoveRigType remRig = request.getRemoveRig();
         this.logger.debug("Received " + this.getClass().getName() + "#removeRig with parameters: name=" + remRig.getName() 
                 + ", removal " + "reason=" + remRig.getRemovalReason() + '.');
         
         /* Response parameters. */
         RemoveRigResponse response = new RemoveRigResponse();
         ProviderResponse providerResp = new ProviderResponse();
         response.setRemoveRigResponse(providerResp);
         
         RemoveLocalRig remover = new RemoveLocalRig();
         if (remover.removeRig(remRig.getName(), remRig.getRemovalReason()))
         {
             providerResp.setSuccessful(true);
         }
         else
         {
             providerResp.setSuccessful(false);
             providerResp.setErrorReason(remover.getFailedReason());
         }
         
         remover.getSession().close();
         return response;
     }
 
     @Override
     public UpdateRigStatusResponse updateRigStatus(UpdateRigStatus request)
     {
         /* Request parameters. */
         UpdateRigType upRig = request.getUpdateRigStatus();
         StatusType status = upRig.getStatus();
         this.logger.debug("Received " + this.getClass().getName() + "#updateRigStatus with parameters: name=" + upRig.getName()
                 + ", isOnline=" + status.getIsOnline() + ", offlineReason=" + status.getOfflineReason() + '.');
         
         /* Response parameters. */
         UpdateRigStatusResponse response = new UpdateRigStatusResponse();
         ProviderResponse providerResp = new ProviderResponse();
         response.setUpdateRigStatusResponse(providerResp);
         
         UpdateLocalRigStatus updater = new UpdateLocalRigStatus();
         if (updater.updateStatus(upRig.getName(), status.getIsOnline(), status.getOfflineReason()))
         {
             providerResp.setSuccessful(true);
             providerResp.setIdentityToken(IdentityTokenRegister.getInstance().getOrGenerateIdentityToken(
                     upRig.getName()));
         }
         else
         {
             providerResp.setSuccessful(false);
             providerResp.setErrorReason(updater.getFailedReason());
         }
         
         updater.getSession().close();
         return response;
     }
 
 }
