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
 
 import java.util.Date;
 
 import javax.activation.DataHandler;
 
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.DataAccessActivator;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.dao.RigDao;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.dao.RigLogDao;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.Rig;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.Session;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.SessionFile;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.listener.RigEventListener.RigStateChangeEvent;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.listener.SessionEventListener.SessionEvent;
 import au.edu.uts.eng.remotelabs.schedserver.logger.Logger;
 import au.edu.uts.eng.remotelabs.schedserver.logger.LoggerActivator;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.RigProviderActivator;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.identok.impl.IdentityTokenRegister;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.impl.RegisterLocalRig;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.impl.RemoveLocalRig;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.impl.UpdateLocalRigStatus;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.intf.types.AddSessionFiles;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.intf.types.AddSessionFilesResponse;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.intf.types.AllocateCallback;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.intf.types.AllocateCallbackResponse;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.intf.types.CallbackRequestType;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.intf.types.ErrorType;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.intf.types.ProviderResponse;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.intf.types.RegisterRig;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.intf.types.RegisterRigResponse;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.intf.types.RegisterRigType;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.intf.types.ReleaseCallback;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.intf.types.ReleaseCallbackResponse;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.intf.types.RemoveRig;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.intf.types.RemoveRigResponse;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.intf.types.RemoveRigType;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.intf.types.SessionFiles;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.intf.types.StatusType;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.intf.types.UpdateRigStatus;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.intf.types.UpdateRigStatusResponse;
 import au.edu.uts.eng.remotelabs.schedserver.rigprovider.intf.types.UpdateRigType;
 
 /**
  * Rig provider SOAP interface operation implementations.
  */
 public class RigProviderSOAPImpl implements RigProvider
 {
     /** Logger. */
     private Logger logger;
     
     public RigProviderSOAPImpl()
     {
         this.logger = LoggerActivator.getLogger();
     }
     
     @Override
     public RegisterRigResponse registerRig(RegisterRig request)
     {
         RegisterLocalRig register = null;
         
         try
         {
             /* Request parameters. */
             RegisterRigType rig = request.getRegisterRig();
             StatusType status = rig.getStatus();
             
             this.logger.debug("Received " + this.getClass().getSimpleName() + "#registerRig with parameters: name=" + rig.getName()
                     + ", type=" + rig.getType() + ", capabilities=" + rig.getCapabilities() + 
                     ", contact URL=" + rig.getContactUrl().toString() + ", isOnline=" + 
                     status.getIsOnline() + ", offlineReason=" + status.getOfflineReason() + ".");
             
             /* Response parameters. */
             RegisterRigResponse response = new RegisterRigResponse();
             ProviderResponse providerResp = new ProviderResponse();
             response.setRegisterRigResponse(providerResp);
     
             register = new RegisterLocalRig();
             if (register.registerRig(rig.getName(), rig.getType(), rig.getCapabilities(), rig.getContactUrl().toString()))
             {
                 Rig registeredRig = register.getRegisteredRig();
                 
                 /* Rig register so update its status. */
                 UpdateLocalRigStatus updater = new UpdateLocalRigStatus(register.getSession());
                 if (updater.updateStatus(registeredRig.getName(), status.getIsOnline(), status.getOfflineReason()))
                 {
                     providerResp.setSuccessful(true);
                     providerResp.setIdentityToken(IdentityTokenRegister.getInstance().getIdentityToken(
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
             
             return response;
         }
         finally 
         {
             if (register != null) register.getSession().close();
         }
     }
 
     @Override
     public RemoveRigResponse removeRig(RemoveRig request)
     {
         RemoveLocalRig remover = null;
         
         try
         {
             /* Request parameters. */
             RemoveRigType remRig = request.getRemoveRig();
             this.logger.debug("Received " + this.getClass().getSimpleName() + "#removeRig with parameters: name=" + remRig.getName() 
                     + ", removal " + "reason=" + remRig.getRemovalReason() + '.');
             
             /* Response parameters. */
             RemoveRigResponse response = new RemoveRigResponse();
             ProviderResponse providerResp = new ProviderResponse();
             response.setRemoveRigResponse(providerResp);
             
             remover = new RemoveLocalRig();
             if (remover.removeRig(remRig.getName(), remRig.getRemovalReason()))
             {
                 providerResp.setSuccessful(true);
             }
             else
             {
                 providerResp.setSuccessful(false);
                 providerResp.setErrorReason(remover.getFailedReason());
             }
             
             return response;
         }
         finally
         {
             if (remover != null) remover.getSession().close();
         }
     }
 
     @Override
     public UpdateRigStatusResponse updateRigStatus(UpdateRigStatus request)
     {
         UpdateLocalRigStatus updater = null;
         
         try
         {
             /* Request parameters. */
             UpdateRigType upRig = request.getUpdateRigStatus();
             StatusType status = upRig.getStatus();
             this.logger.debug("Received " + this.getClass().getSimpleName() + "#updateRigStatus with parameters: name=" + upRig.getName()
                     + ", isOnline=" + status.getIsOnline() + ", offlineReason=" + status.getOfflineReason() + '.');
             
             /* Response parameters. */
             UpdateRigStatusResponse response = new UpdateRigStatusResponse();
             ProviderResponse providerResp = new ProviderResponse();
             response.setUpdateRigStatusResponse(providerResp);
             
             updater = new UpdateLocalRigStatus();
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
             
             return response;
         }
         finally
         {
             if (updater != null) updater.getSession().close();
         }
     }
 
     @Override
     public AllocateCallbackResponse allocateCallback(AllocateCallback allocateCallback)
     {
         RigDao dao = null;
         
         try
         {
             CallbackRequestType request = allocateCallback.getAllocateCallback();
             this.logger.debug("Received " + this.getClass().getSimpleName() + "#allocateCallback with params: rigname=" + request.getName() + ", success=" +
                     request.getSuccess() + '.');
             
             AllocateCallbackResponse response = new AllocateCallbackResponse();
             ProviderResponse status = new ProviderResponse();
             response.setAllocateCallbackResponse(status);
             
             /* Load session from rig. */
             dao = new RigDao();
             Rig rig = dao.findByName(request.getName());
             Session ses = null;
             if (rig == null)
             {
                 /* If the rig wasn't found, something is seriously wrong. */
                 this.logger.error("Received allocate callback for rig '" + request.getName() + "' that doesn't exist.");
                 status.setSuccessful(false);
             }
             else if ((ses = rig.getSession()) == null)
             {
                 this.logger.warn("Received allocate callback for session that doesn't exist. Rig who sent callback " +
                 		"response was '" + request.getName() + "'.");
                 status.setSuccessful(false);
                 
                 /* Make sure the rig is no marked as in session. */
                 rig.setInSession(false);
                 rig.setLastUpdateTimestamp(new Date());
             }
             else if (request.getSuccess())
             {
                /* If the response from allocate is successful, put the session to ready. */
                ses.setReady(true);
                status.setSuccessful(true);
                rig.setLastUpdateTimestamp(new Date());
                
                RigProviderActivator.notifySessionEvent(SessionEvent.READY, ses, dao.getSession());
             }
             else
             {
                 ErrorType err = request.getError();
                 this.logger.error("Received allocate response for " + ses.getUserNamespace() + ':' + 
                         ses.getUserName() + ", allocation not successful. Error reason is '" + err.getReason() + "'.");
                 
                 /* Allocation failed so end the session and take the rig offline depending on error. */
                 ses.setActive(false);
                 ses.setReady(false);
                 ses.setRemovalReason("Allocation failure with reason: " + err.getReason());
                 ses.setRemovalTime(new Date());
                 
                 RigProviderActivator.notifySessionEvent(SessionEvent.FINISHED, ses, dao.getSession());
             
                 if (err.getCode() == 4) // Error code 4 is an existing session exists
                 {
                     this.logger.error("Allocation failure reason was caused by an existing session, so not putting rig offline " +
                             "because a session already has it.");
                 }
                 else
                 {
                     rig.setInSession(false);
                     rig.setOnline(false);
                     rig.setOfflineReason("Allocation failed with reason: " + err.getReason());
                     rig.setSession(null);
                     
                     /* Log the rig going offline. */
                     RigLogDao logDao = new RigLogDao(dao.getSession());
                     logDao.addOfflineLog(rig, "Allocation failed with reason: " + err.getReason() + "");
                     
                     RigProviderActivator.notifyRigEvent(RigStateChangeEvent.OFFLINE, rig, dao.getSession());
                 }
                 
                 rig.setLastUpdateTimestamp(new Date());
                 
                 /* Whilst allocation was not successful, the process was clean. */
                 status.setSuccessful(true);
             }
 
             dao.flush();
             return response;
         }
         finally
         {
             if (dao != null) dao.closeSession();
         }
     }
 
     @Override
     public ReleaseCallbackResponse releaseCallback(ReleaseCallback releaseCallback)
     {
         RigDao dao = null;
         try
         {
             CallbackRequestType request = releaseCallback.getReleaseCallback();
             this.logger.debug("Received " + this.getClass().getSimpleName() + "#releaseCallback with params: rigname=" + 
                     request.getName() + ", success=" + request.getSuccess());
     
             ReleaseCallbackResponse response = new ReleaseCallbackResponse();
             ProviderResponse status = new ProviderResponse();
             response.setReleaseCallbackResponse(status);
             
             /* Load rig information. */
             dao = new RigDao();
             Rig rig = dao.findByName(request.getName());
             if (rig == null)
             {
                 /* If the rig wasn't found something is seriously wrong. */
                 this.logger.error("Received release notification from rig '" + request.getName() + "' which does not " +
                 		"exist.");
                 status.setSuccessful(false);
             }
             else if (request.getSuccess())
             {
                 status.setSuccessful(true);
                 
                 /* Release was successful so provide the rig back to the queue. */
                 this.logger.debug("Release of rig '" + request.getName() + "' successful, going to requeue rig.");
                 rig.setInSession(false);
                 rig.setSession(null);
                 rig.setLastUpdateTimestamp(new Date());
                 dao.flush();
                 
                 /* Provide notification a new rig is free. */
                 RigProviderActivator.notifyRigEvent(RigStateChangeEvent.ONLINE, rig, dao.getSession());
             }
             else
             {
                 status.setSuccessful(true);
                 
                 /* Allocation failed so take the rig off line. */
                 rig.setInSession(false);
                 rig.setSession(null);
                 rig.setLastUpdateTimestamp(new Date());
                 rig.setOnline(false);
                 
                 RigLogDao logDao = new RigLogDao(dao.getSession());
                 
                 ErrorType err = request.getError();
                 if (err == null)
                 {
                     this.logger.warn("Taking rig '" + request.getName() + "' offline because release failed.");
                     rig.setOfflineReason("Release failed.");
                     logDao.addOfflineLog(rig, "Release failed.");
                 }
                 else
                 {
                     this.logger.warn("Taking rig '" + request.getName() + "' offline because release failed with reason '" +
                             err.getReason() + "'.");
                     rig.setOfflineReason("Release failed with reason: " + err.getReason());
                     logDao.addOfflineLog(rig, "Release failed with reason: " + err.getReason());
                 }
                 
                 /* Provide notification a new rig is offline. */
                 RigProviderActivator.notifyRigEvent(RigStateChangeEvent.OFFLINE, rig, dao.getSession());
             }
             
             dao.flush();
             return response;
         }
         finally
         {
             if (dao != null) dao.closeSession();
         }
     }
 
     @Override
     public AddSessionFilesResponse addSessionFiles(AddSessionFiles sessionFiles)
     {
         org.hibernate.Session db = null;
         try
         {
             /* Request parameters. */
             SessionFiles files = sessionFiles.getAddSessionFiles();                    
             this.logger.debug("Received " + this.getClass().getSimpleName() + "#addSessionFiles with parameters name=" +
                     files.getName() + ", user=" + files.getUser() + ", number of files=" + files.getFiles().length);
             
             /* Response parameters. */
             AddSessionFilesResponse response = new AddSessionFilesResponse();
             ProviderResponse status = new ProviderResponse();
             response.setAddSessionFilesResponse(status);
             
             
             db = DataAccessActivator.getNewSession();
             
             /* Load the session that generated the session file. This is the last 
              * session the user was on the rig. */
             Session session = (Session) db.createCriteria(Session.class)
                             .createAlias("rig", "rig")
                             .createAlias("user", "user")
                             .add(Restrictions.eq("rig.name", files.getName()))
                             .add(Restrictions.eq("user.name", files.getUser()))
                             .addOrder(Order.desc("activityLastUpdated"))
                             .setMaxResults(1)
                             .uniqueResult();
             
             if (session == null)
             {
                 this.logger.warn("Unable to store session files for user '" + files.getUser() + "' because their " +
                 		"session on rig '" + files.getName() + "' was not found.");
                 status.setSuccessful(false);
                 status.setErrorReason("Session not found");
                 return response;
             }
             
             for (au.edu.uts.eng.remotelabs.schedserver.rigprovider.intf.types.SessionFile file : files.getFiles())
             {
                 SessionFile sf = new SessionFile();
                 sf.setSession(session);
                 sf.setName(file.getName());
                 sf.setPath(file.getPath());
                 sf.setTimestamp(file.getTimestamp().getTime());
                 sf.setTransferMethod(file.getTransfer().toString());
                 
                 if (SessionFile.ATTACHMENT_TRANSFER.equals(sf.getTransferMethod()))
                 {
                     /* The file needs to be download. */
                     DataHandler data = file.getFile();
                     if (data == null)
                     {
                        this.logger.warn("Cannot save sessionf ile with name '" + sf.getName() + "' because it no " +
                         		"attached file even though the transfer method is 'ATTACHMENT'.");
                         continue;
                     }
                     
                     // TODO The actual downloading of a file should be handled elsewhere
                 }
                 else if (SessionFile.FILESYSTEM_TRANSFER.equals(sf.getTransferMethod()))
                 {
                     /* As the file is on a shared directory, it should already 
                      * exist. */
                     // FIXME There should be some form of verification here
                     sf.setTransferred(true);
                 }
                 else if (SessionFile.WEBDAV_TRANSFER.equals(sf.getTransferMethod()))
                 {
                     /* The file will be coming later. */
                     sf.setTransferred(false);
                 }
                 else
                 {
                     this.logger.warn("Cannot save session file with name '" + sf.getName() + "' because its transfer " +
                     		"method '" + sf.getTransferMethod() + "' is unknown.");
                     continue;
                 }
                 
                 /* Store the new session file record. */
                 db.beginTransaction();
                 db.persist(sf);
                 db.getTransaction().commit();
             }
             
             status.setSuccessful(true);
             return response;
         }
         finally
         {
             if (db != null) db.close();
         }
     }
 }
