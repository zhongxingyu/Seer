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
  * @date 6th April 2010
  */
 
 package au.edu.uts.eng.remotelabs.schedserver.session.intf;
 
 import java.util.Date;
 
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.dao.SessionDao;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.dao.UserDao;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.ResourcePermission;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.Rig;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.Session;
 import au.edu.uts.eng.remotelabs.schedserver.dataaccess.entities.User;
 import au.edu.uts.eng.remotelabs.schedserver.logger.Logger;
 import au.edu.uts.eng.remotelabs.schedserver.logger.LoggerActivator;
 import au.edu.uts.eng.remotelabs.schedserver.rigclientproxy.RigClientService;
 import au.edu.uts.eng.remotelabs.schedserver.rigclientproxy.intf.types.IsActivityDetectableResponse;
 import au.edu.uts.eng.remotelabs.schedserver.session.impl.RigReleaser;
 import au.edu.uts.eng.remotelabs.schedserver.session.intf.types.FinishSession;
 import au.edu.uts.eng.remotelabs.schedserver.session.intf.types.FinishSessionResponse;
 import au.edu.uts.eng.remotelabs.schedserver.session.intf.types.GetSessionInformation;
 import au.edu.uts.eng.remotelabs.schedserver.session.intf.types.GetSessionInformationResponse;
 import au.edu.uts.eng.remotelabs.schedserver.session.intf.types.InSessionType;
 import au.edu.uts.eng.remotelabs.schedserver.session.intf.types.OperationRequestType;
 import au.edu.uts.eng.remotelabs.schedserver.session.intf.types.ResourceIDType;
 import au.edu.uts.eng.remotelabs.schedserver.session.intf.types.SessionType;
 import au.edu.uts.eng.remotelabs.schedserver.session.intf.types.UserIDType;
 
 /**
  * Session SOAP interface implementation.
  */
 public class SessionInterface implements SessionSkeletonInterface
 {
     /** Logger. */
     private Logger logger;
     
     /** Flag for unit testing to disable rig client communication. */ 
     private boolean notTest = true;
     
     public SessionInterface()
     {
         this.logger = LoggerActivator.getLogger();
     }
     
     @Override
     public FinishSessionResponse finishSession(final FinishSession request)
     {
         /* Request parameters. */
         UserIDType uID = request.getFinishSession();
         this.logger.debug("Received finish session request for user with id=" + uID.getUserID() + ", namespace="
                 + uID.getUserNamespace() + ", name=" + uID.getUserName() + '.');
         
         /* Response parameters. */
         FinishSessionResponse resp = new FinishSessionResponse();
         InSessionType inSes = new InSessionType();
         resp.setFinishSessionResponse(inSes);
         inSes.setIsInSession(false);
         
         if (!this.checkPermission(uID))
         {
             this.logger.warn("Unable to finish session because of invalid permission.");
             return resp;
         }
         
         SessionDao dao = new SessionDao();
         Session ses;
         User user = this.getUserFromUserID(uID, new UserDao(dao.getSession()));
         if (user != null && (ses = dao.findActiveSession(user)) != null)
         {
             /* Finish the session. */
             ses.setActive(false);
             ses.setRemovalReason("User request.");
             ses.setRemovalTime(new Date());
             dao.flush();
             
             /* Call rig release. */
             // TODO terminate code assigned
             if (this.notTest) new RigReleaser().release(ses, dao.getSession());
         }
         
         dao.closeSession();
         return resp;
     }
 
     @Override
     public GetSessionInformationResponse getSessionInformation(final GetSessionInformation request)
     {
         /* Request parameters. */
         UserIDType uID = request.getGetSessionInformation();
         this.logger.debug("Received session information request for user with id=" + uID.getUserID() + ", namespace="
                 + uID.getUserNamespace() + ", name=" + uID.getUserName() + '.');
         
         /* Response parameters. */
         GetSessionInformationResponse resp = new GetSessionInformationResponse();
         SessionType info = new SessionType();
         resp.setGetSessionInformationResponse(info);
         info.setIsInSession(false);
         
         SessionDao dao = new SessionDao();
         Session ses;
         User user = this.getUserFromUserID(uID, new UserDao(dao.getSession()));
         if (user != null && (ses = dao.findActiveSession(user)) != null && ses.getRig() != null)
         {
             info.setIsInSession(true);
         
           
             info.setIsCodeAssigned(ses.getCodeReference() != null);
             
             /* Resource. */
             Rig rig = ses.getRig();
             ResourceIDType res = new ResourceIDType();
             info.setResource(res);
             res.setType(ResourcePermission.RIG_PERMISSION);
             res.setResourceID(rig.getId().intValue());
             res.setResourceName(rig.getName());
             info.setContactURL(rig.getContactUrl());
             info.setRigType(rig.getRigType().getName());
             
             /* Session time and remaining time. */
             ResourcePermission perm = ses.getResourcePermission();
             int time = Math.round((System.currentTimeMillis() - ses.getAssignmentTime().getTime()) / 1000);
             info.setTime(time);
             int remainingTime = perm.getSessionDuration() + (perm.getAllowedExtensions() - ses.getExtensions()) *  
                     perm.getExtensionDuration() - time;
             info.setTimeLeft(remainingTime);
             info.setExtensions(ses.getExtensions());
             
             /* DODGY This is a hack for some callbacks not correctly updating the ready flag. */
             info.setIsReady(ses.isReady() || time > 60);
             
             /* Warning messages. */
             if (ses.isInGrace())
             {
                 info.setWarningMessage(remainingTime > 0 ? 
                         "You will be removed from the rig in " + remainingTime + " seconds." :
                         "You are being removed from the rig.");
             }
             else
             {
                 /* Find out about activity. */
                 try
                 {
                     if (this.notTest)
                     {
                         IsActivityDetectableResponse detectResponse = new RigClientService(rig, dao.getSession()).isActivityDetectable();
                         if (!detectResponse.getIsActivityDetectableResponse().getActivity())
                         {
                             int rmTime = perm.getSessionActivityTimeout() -  
                                     (ses.getActivityLastUpdated().before(ses.getAssignmentTime()) ? time :
                                      Math.round((System.currentTimeMillis() - ses.getActivityLastUpdated().getTime()) / 1000));
                             info.setWarningMessage(rmTime > 0 ?
                                    "If you do not use the rig before " + rmTime + " seconds you will be removed from the rig." :
                                     "You are being removed from the rig.");
                         }
                         else
                         {
                             ses.setActivityLastUpdated(new Date());
                             dao.flush();
                         }
                     }
                 }
                 catch (Exception e)
                 {
                     this.logger.warn("Unable to call activity detection on rig client " + rig.getName() + " at " +
                             rig.getContactUrl() + ", error " + e.getMessage() + '.');
                 }
             }
         }
         
         dao.closeSession();
         return resp;
     }
     
     /**
      * Gets the user identified by the user id type. 
      * 
      * @param uID user identity 
      * @param ses database session
      * @return user or null if not found
      */
     private User getUserFromUserID(UserIDType uID, UserDao dao)
     {
         User user;
         
         long recordId = 0;
         try
         {
             if (uID.getUserID() != null)
             {
                 recordId = Long.parseLong(uID.getUserID());
             }
         }
         catch (NumberFormatException e) { /* Don't use user ID then. */ }
         
         String ns = uID.getUserNamespace(), nm = uID.getUserName();
         
         if (recordId > 0 && (user = dao.get(recordId)) != null)
         {
             return user;
         }
         else if (ns != null && nm != null && (user = dao.findByName(ns, nm)) != null)
         {
             return user;
         }
         
         return null;
     }
     
     /**
      * Checks whether the request has the specified permission. Currently this
      * is a stub and always return, irrespective of the provided user.
      * 
      * @return true if the request has the appropriate permission
      */
     private boolean checkPermission(OperationRequestType req)
     {
         // TODO Check request permissions for queuer
         return true;
     }
 
 }
