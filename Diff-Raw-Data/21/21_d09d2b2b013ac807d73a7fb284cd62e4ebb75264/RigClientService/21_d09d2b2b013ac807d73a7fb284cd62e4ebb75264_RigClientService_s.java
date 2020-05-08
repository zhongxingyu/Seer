 /**
  * SAHARA Rig Client
  * 
  * Software abstraction of physical rig to provide rig session control
  * and rig device control. Automatically tests rig hardware and reports
  * the rig status to ensure rig goodness.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2009, University of Technology, Sydney
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
  * @date 7th December 2009
  *
  * Changelog:
  * - 07/12/2009 - mdiponio - Initial file creation.
  */
 package au.edu.uts.eng.remotelabs.rigclient.protocol;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.AbortBatchControl;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.AbortBatchControlResponse;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.ActivityDetectableType;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.Allocate;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.AllocateResponse;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.AttributeRequestType;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.AttributeResponseType;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.AttributeResponseTypeChoice;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.BatchRequestType;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.BatchState;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.BatchStatusResponseType;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.ErrorType;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.GetAttribute;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.GetAttributeResponse;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.GetBatchControlStatus;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.GetBatchControlStatusResponse;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.GetStatus;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.GetStatusResponse;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.IsActivityDetectable;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.IsActivityDetectableResponse;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.MaintenanceRequestType;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.Notify;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.NotifyResponse;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.OperationResponseType;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.ParamType;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.PerformBatchControl;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.PerformBatchControlResponse;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.PerformPrimitiveControl;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.PerformPrimitiveControlResponse;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.PrimitiveControlRequestType;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.PrimitiveControlResponseType;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.Release;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.ReleaseResponse;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.SetMaintenance;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.SetMaintenanceResponse;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.SetTestInterval;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.SetTestIntervalResponse;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.SlaveAllocate;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.SlaveAllocateResponse;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.SlaveRelease;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.SlaveReleaseResponse;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.SlaveUserType;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.StatusResponseType;
 import au.edu.uts.eng.remotelabs.rigclient.protocol.types.TestIntervalRequestType;
 import au.edu.uts.eng.remotelabs.rigclient.rig.IRig;
 import au.edu.uts.eng.remotelabs.rigclient.rig.IRigControl;
 import au.edu.uts.eng.remotelabs.rigclient.rig.IRigControl.BatchResults;
 import au.edu.uts.eng.remotelabs.rigclient.rig.IRigControl.PrimitiveRequest;
 import au.edu.uts.eng.remotelabs.rigclient.rig.IRigControl.PrimitiveResponse;
 import au.edu.uts.eng.remotelabs.rigclient.rig.IRigSession.Session;
 import au.edu.uts.eng.remotelabs.rigclient.status.StatusUpdater;
 import au.edu.uts.eng.remotelabs.rigclient.type.RigFactory;
 import au.edu.uts.eng.remotelabs.rigclient.util.ConfigFactory;
 import au.edu.uts.eng.remotelabs.rigclient.util.IConfig;
 import au.edu.uts.eng.remotelabs.rigclient.util.ILogger;
 import au.edu.uts.eng.remotelabs.rigclient.util.LoggerFactory;
 
 /**
  * Rig client SOAP interface implementation.
  */
 public class RigClientService implements RigClientServiceSkeletonInterface
 {
     /** Rig type class. */
     private final IRig rig;
     
     /** Logger. */
     private final ILogger logger;
     
     /** Configuration. */
     private final IConfig config;
     
     /**
      * Constructor.
      */
     public RigClientService()
     {
         this.logger = LoggerFactory.getLoggerInstance();
         this.rig = RigFactory.getRigInstance();
         this.config = ConfigFactory.getInstance();
     }
 
     @Override
     public AllocateResponse allocate(final Allocate allocRequest)
     {   
         /* Request parameters. */
         final String user = allocRequest.getAllocate().getUser();
         this.logger.debug("Received allocate request with parameter user=" + user + ".");
         
         /* Response parameters. */
         final OperationResponseType operation = new OperationResponseType();
         final ErrorType error = new ErrorType();
         operation.setError(error);
         error.setOperation("Allocate rig to user " + user + ".");
         error.setReason("");
         final AllocateResponse response = new AllocateResponse();
         response.setAllocateResponse(operation);
 
         if (!this.isSourceAuthenticated(allocRequest.getAllocate().getIdentityToken()))
         {
             this.logger.warn("Failed allocating user " + user + " because of invalid permission.");
             operation.setSuccess(false);
             error.setCode(3);
             error.setReason("Not authorised to allocate a user.");
 
         }
         else if (this.rig.isSessionActive()) // Cannot allocate a rig who is already in-use
         {
             this.logger.warn("Failed allocating user " + user + " because there is an existing session.");
             operation.setSuccess(false);
             error.setCode(4);
             error.setReason("A session is already active.");
         }
         else if (!this.rig.isMonitorStatusGood()) // Cannot allocate a rig who is in bad state
         {
             this.logger.warn("Failed allocating user " + user + " because the rig is not in a operable state.");
             operation.setSuccess(false);
             error.setCode(7);
             error.setReason("Rig not operable.");
         }
         else if (this.rig.assign(user)) // Actual allocation 
         {
             this.logger.info("Successfully allocated user " + user + " to rig.");
             operation.setSuccess(true);
         }
         else // Something fraked up
         {   
             this.logger.warn("Failed allocating user " + user + " because of some unknown reason " +
             "(this may be a bug).");
             operation.setSuccess(false);
             error.setCode(16);
             error.setReason("Unknown.");
         }
 
         return response;
     }
 
     @Override
     public ReleaseResponse release(final Release relRequest)
     {
         /* Request parameters. */
         final String user = relRequest.getRelease().getUser();
         this.logger.debug("Received release request with parameter: user=" + user + ".");
         
         /* Response parameters. */
         final OperationResponseType operation = new OperationResponseType();
         final ErrorType error = new ErrorType();
         operation.setError(error);
         error.setCode(0);
         error.setReason("");
         error.setOperation("Release " + user + ".");
         final ReleaseResponse response = new ReleaseResponse();
         response.setReleaseResponse(operation);
         
         if (!this.isSourceAuthenticated(relRequest.getRelease().getIdentityToken()))
         {
             this.logger.warn("Failed releasing user " + user + " because of invalid permission.");
             operation.setSuccess(false);
             error.setCode(3);
             error.setReason("Not authorised to release a user.");
 
         }
         else if (!this.rig.isSessionActive())
         {
             this.logger.warn("Failed to release " + user + " as no session is currently running.");
             operation.setSuccess(false);
             error.setCode(6);
             error.setReason("Session not running.");
         }
         else if (!this.rig.hasPermission(user, Session.MASTER)) // If the user is indeed a master session user
         {
             this.logger.warn("Failed to release " + user + " as they are not the session master.");
             operation.setSuccess(false);
             error.setCode(5);
             error.setReason("User is not a master user.");
         }
         else if (this.rig.revoke()) // Actual revocation
         {
             this.logger.info("Released user " + user + ".");
             operation.setSuccess(true);
         }
         else  // Something fraked up
         {
             this.logger.warn("Failed to release " + user + " for some unknown reason (this may be a bug).");
             operation.setSuccess(false);
             error.setCode(16);
             error.setReason("Unknown.");
         }
         
         return response;
     }
 
     @Override
     public SlaveAllocateResponse slaveAllocate(final SlaveAllocate slaveRequest)
     {
         /* Request parameters. */
         final SlaveUserType slave = slaveRequest.getSlaveAllocate();
         final String user = slaveRequest.getSlaveAllocate().getUser();
         final String typeStr = slaveRequest.getSlaveAllocate().getType().getValue();
         Session type = Session.NOT_IN;
         if (typeStr.equalsIgnoreCase("Active"))
         {
             type = Session.SLAVE_ACTIVE;
         }
         else if (typeStr.equalsIgnoreCase("Passive"))
         {
             type = Session.SLAVE_PASSIVE; 
         }
         this.logger.debug("Received slave allocate request with parameters: user=" + user + ", slave type=" + 
                 typeStr + ".");
         
         /* Response parameters. */
         final OperationResponseType operation = new OperationResponseType();
         final ErrorType error = new ErrorType();
         error.setCode(0);
         error.setOperation("Slave allocate of type " + typeStr + " to " + user + ".");
         error.setReason("");
         operation.setError(error);
         final SlaveAllocateResponse response = new SlaveAllocateResponse();
         response.setSlaveAllocateResponse(operation);
         
         if (!(this.isSourceAuthenticated(slave.getIdentityToken()) ||
                 this.rig.hasPermission(slave.getRequestor(), Session.MASTER)))
         {
             this.logger.warn("Failed allocating slave user " + user + " because of invalid permission.");
             operation.setSuccess(false);
             error.setCode(3);
             error.setReason("Not authorised to allocate a slave user.");
         }
         else if (!(type == Session.SLAVE_ACTIVE || type == Session.SLAVE_PASSIVE)) // Only slave types
         {
             this.logger.warn("Failed allocating slave user " + user + " because of an invalid slave type " + 
                     typeStr + ".");
             operation.setSuccess(false);
             error.setCode(2);
             error.setReason("Invalid slave type parameter.");
         }
         else if (!this.rig.isSessionActive()) // Cannot slave assign when no session is running
         {
             this.logger.warn("Failed allocating slave user " + user + " because a session is not running.");
             operation.setSuccess(false);
             error.setCode(6);
             error.setReason("No session is currently running.");
         }
         else if (this.rig.hasPermission(slave.getUser(), Session.MASTER))
         {
             this.logger.warn("Trying to slave allocate a master user.");
             operation.setSuccess(false);
             error.setCode(2);
             error.setReason("User " + user + " is already a master user.");
         }
         else if (this.rig.addSlave(user, type == Session.SLAVE_ACTIVE ? false : true))
         {
             this.logger.info("Successfully added user " + user + " as slave user of type " + typeStr + ".");
             operation.setSuccess(true);
         }
         else
         {
             this.logger.warn("Failed allocating slave user " + user + ", the user may already have the requested " +
             		"slave permission.");
             operation.setSuccess(false);
             error.setCode(16);
             error.setReason("Unknown, user may already be a slave user.");
         }
 
         return response;
     }
 
     @Override
     public SlaveReleaseResponse slaveRelease(final SlaveRelease slaveRequest)
     {
         /* Request parameters. */
         final String slave = slaveRequest.getSlaveRelease().getUser();
         this.logger.debug("Received slave release request with parameters: user=" + slave + ".");
         
         /* Response parameters. */
         final SlaveReleaseResponse response = new SlaveReleaseResponse();
         final OperationResponseType operation = new OperationResponseType();
         response.setSlaveReleaseResponse(operation);
         final ErrorType error = new ErrorType();
         operation.setError(error);
         error.setCode(0);
         error.setOperation("Release slave " + slave + ".");
         error.setReason("");
         
         if (!(this.isSourceAuthenticated(slaveRequest.getSlaveRelease().getIdentityToken()) || 
                 this.rig.hasPermission(slaveRequest.getSlaveRelease().getRequestor(), Session.MASTER) ||
                 slave.equals(slaveRequest.getSlaveRelease().getRequestor()))) // The slave user remove themselves
         {
             this.logger.warn("Failed releasing slave user " + slave + " because of invalid permission.");
             operation.setSuccess(false);
             error.setCode(3);
             error.setReason("Not authorised to release slave user.");
         }
         else if (!(this.rig.isInSession(slave) == Session.SLAVE_ACTIVE || 
                    this.rig.isInSession(slave) == Session.SLAVE_PASSIVE))
         { 
             this.logger.warn("Failed releasing slave user " + slave + " because they are not a slave user.");
             operation.setSuccess(false);
             error.setCode(18);
             error.setReason("User " + slave + " not a slave user.");
         }
         else if (this.rig.revokeSlave(slave))
         {
             this.logger.info("Released slave user " + slave + ".");
             operation.setSuccess(true);
         }
         else // Something mother fraked up
         {
             this.logger.warn("Failed releasing slave user " + slave + " because of some unknown reason " +
             		"(possibly a bug).");
             error.setCode(16);
             error.setReason("Unknown reason.");
         }
         
         return response;
     }
 
     @Override
     public NotifyResponse notify(final Notify notify)
     {
         /* Request parameters. */
         final String message = notify.getNotify().getMessage();
         final String requestor = notify.getNotify().getRequestor();
         final String ident = notify.getNotify().getIdentityToken();
         this.logger.debug("Received notify request with parameter: message=" + message + ".");
         
         /* Response parameters. */
         final NotifyResponse response = new NotifyResponse();
         final OperationResponseType operation = new OperationResponseType();
         response.setNotifyResponse(operation);
         final ErrorType error = new ErrorType();
         error.setCode(0);
         error.setOperation("Notify with message " + message + ".");
         error.setReason("");
         operation.setError(error);
         
         if (!(this.isSourceAuthenticated(ident) || this.rig.hasPermission(requestor, Session.SLAVE_PASSIVE)))
         {
             this.logger.warn("Failed sending notification message " + message + " because of invalid permission.");
             operation.setSuccess(false);
             error.setCode(3);
             error.setReason("Invalid permission.");
         }
         else if (!this.rig.isSessionActive())
         {
             this.logger.warn("Failed notification because there are no sessions active.");
             operation.setSuccess(false);
             error.setCode(6);
             error.setReason("Not in session.");
         }
         else if (this.rig.notify(message))
         {
             this.logger.info("Successfully sent out notification of message " + message + ".");
             operation.setSuccess(true);
         }
         else 
         {
             this.logger.warn("Failed notification because of some unknown error (possibly a bug).");
             operation.setSuccess(false);
             error.setCode(16);
             error.setReason("Unknown error.");
         }
 
         return response;
     }
 
     @Override
     public PerformBatchControlResponse performBatchControl(final PerformBatchControl batchRequest)
     {
         /* Request parameters. */
         final BatchRequestType request = batchRequest.getPerformBatchControl();
         this.logger.debug("Received batch control invocation request.");
         
         /* Response parameters. */
         final PerformBatchControlResponse response = new PerformBatchControlResponse();
         final OperationResponseType operation = new OperationResponseType();
         response.setPerformBatchControlResponse(operation);
         final ErrorType error = new ErrorType();
         error.setOperation("Performing batch control.");
         error.setReason("");
         operation.setError(error);
         
         /* --------------------------------------------------------------------
          * ---- 1. First check the rig is in session.                        --
          * ------------------------------------------------------------------*/
         if (!this.rig.isSessionActive())
         {
             this.logger.warn("Unable to perform batch control because the rig is not in session.");
             operation.setSuccess(false);
             error.setCode(6);
             error.setReason("Rig not in session.");
             return response;
         }
         
         /* --------------------------------------------------------------------
          * ---- 2. Check the requestor is allowed to perform batch          --
          * ----    control. The following entities are allowed:              --
          * ----      * Scheduling server identified by the provided          --
          * ----        identity token request field.                         --
          * ----      * Master user identified by the requestor request       --
          * ----        field.                                                --
          * ----      * Slave active user identified by the requestor request --
          * ----        field.                                                --
          * ------------------------------------------------------------------*/
         if (!(this.isSourceAuthenticated(request.getIdentityToken()) || 
                 this.rig.hasPermission(request.getRequestor(), Session.SLAVE_ACTIVE)))
         {
             this.logger.warn("Unable to perform batch control because the requestor does not have permission.");
             operation.setSuccess(false);
             error.setCode(3);
             error.setReason("Invalid permission.");
             return response;
         }
         
         /* --------------------------------------------------------------------
          * ---- 3. Check the rig supports being controlled (i.e. it          --
          * ----    implements the rig control interface).                    --
          * ------------------------------------------------------------------*/
         if (!(this.rig instanceof IRigControl))
         {
             this.logger.warn("Unable to perform batch control because the rig does not support it.");
             operation.setSuccess(false);
             error.setCode(10);
             error.setReason("Batch not supported.");
             return response;
         }
         
         final IRigControl controlledRig = (IRigControl)this.rig;
         
         
         /* ----------------------------------------------------------------
          * ---- 4. Check batch is not already running.                   --
          * --------------------------------------------------------------*/
         if (controlledRig.isBatchRunning())
         {
             this.logger.info("Unable to perform batch control because there is an existing batch operation running.");
             operation.setSuccess(false);
             error.setCode(12);
             error.setReason("Existing batch operation.");
             return response;
         }
 
 
         /* --------------------------------------------------------------------
          * ---- 5. Download the batch instruction file.                      --
          * ------------------------------------------------------------------*/
         String fileName = this.config.getProperty("Batch_Download_Dir", System.getProperty("java.io.tmpdir")) + 
             System.getProperty("file.separator") + (System.currentTimeMillis() / 1000) + 
             '-' + (new File(request.getFileName()).getName());
         this.logger.debug("Uploading batch file to " + fileName + '.');
 
         FileOutputStream file = null;
         InputStream upload = null;
         try
         {
             file = new FileOutputStream(fileName);
             upload = request.getBatchFile().getInputStream();
             byte buf[] = new byte[1024];
             int read = 0;
             while ((read = upload.read(buf)) == buf.length)
             {
                 file.write(buf);
             }
             if (read > 0)
             {
                 file.write(buf, 0, read); // File remaining bytes.
             }
             file.flush();
             file.close();
 
             /* ----------------------------------------------------------------
              * ---- 6. Get the requesting user, either the requestor request --
              * ----    field, or the session master user.                    --
              * --------------------------------------------------------------*/
             String user = request.getRequestor();
             if (user == null)
             {
                 this.logger.debug("Requestor name not provided, using the master user name.");
                 Map<String, Session> sessionUsers = this.rig.getSessionUsers();
                 for (Entry<String, Session> sUser : sessionUsers.entrySet())
                 {
                     if (sUser.getValue() == Session.MASTER)
                     {
                         user = sUser.getKey();
                         break;
                     }
                 }
             }
             if (user == null)
             {
                 this.logger.error("Attempted to run batch control, with session active, but the master user" +
                 		"was not found. Please report a bug.");
                 operation.setSuccess(false);
                 error.setCode(16);
                 error.setReason("Master user was not found.");
                 return response;
             }
             
             /* ----------------------------------------------------------------
              * ---- 7. Run batch control!                                    --
              * --------------------------------------------------------------*/
             if (controlledRig.performBatch(fileName, user))
             {
                 operation.setSuccess(true);
             }
             else
             {
                 operation.setSuccess(true);
                 BatchResults res = controlledRig.getBatchResults();
                 error.setCode(res.getErrorCode());
                 String reason = res.getErrorReason();
                 error.setReason(reason == null ? "Unknown error." : reason);
             }
         }
         catch (FileNotFoundException e)
         {
             operation.setSuccess(false);
             error.setCode(16);
             error.setReason("Unable to upload batch instruction file.");
         }
         catch (IOException e)
         {
             operation.setSuccess(false);
             error.setCode(16);
             error.setReason("Unable to upload batch instruction file.");        
         }
         finally
         {
             if (file != null)
             {
                 try
                 {
                     file.close();
                 }
                 catch (IOException e)
                 {
                     this.logger.warn("Unable to close batch file output stream because of exception: " 
                             + e.getClass().getSimpleName() + " with message: " + e.getMessage() + '.');
                 }
             }
 
             if (upload != null)
             {
                 try
                 {
                     upload.close();
                 }
                 catch (IOException e)
                 {
                     this.logger.warn("Unable to close batch upload input stream because of exception: " 
                             + e.getClass().getSimpleName() + " with message: " + e.getMessage() + '.');
                 }
             }
         }
         
         return response;
     }
 
     @Override
     public AbortBatchControlResponse abortBatchControl(final AbortBatchControl abortRequest)
     {
         /* Request parameters. */
         final String requestor = abortRequest.getAbortBatchControl().getRequestor();
         final String identTok = abortRequest.getAbortBatchControl().getIdentityToken();
         this.logger.debug("Received abort batch control request with parameters: identity token=" + identTok + 
                 ", requestor=" + requestor + ".");
         
         /* Response parameters. */
         final AbortBatchControlResponse response = new AbortBatchControlResponse();
         final OperationResponseType operation = new OperationResponseType();
         response.setAbortBatchControlResponse(operation);
         final ErrorType error = new ErrorType();
         error.setCode(0);
         error.setOperation("Abort batch control.");
         error.setReason("");
         operation.setError(error);
         
         if (this.rig instanceof IRigControl)
         {
             final IRigControl controlRig = (IRigControl)this.rig;
             if (!(this.isSourceAuthenticated(identTok) ||
                     this.rig.hasPermission(requestor, Session.SLAVE_ACTIVE))) // Does the requestor have permission
             {
                 this.logger.warn("Requestor " + requestor + " does not have permission to abort batch control.");
                 operation.setSuccess(false);
                 error.setCode(3);
                 error.setReason("Invalid permission.");
             }
             else if (controlRig.abortBatch())
             {
                 this.logger.info("Requestor " + requestor + " successfully aborted a batch control invocation.");
                 operation.setSuccess(true);
             }
             else
             {
                 this.logger.warn("Failed aborting batch control invocation. Perhaps the batch " +
                 		"control abort wait timed out.");
                 operation.setSuccess(false);
                 error.setCode(16);
                 error.setReason("Unknown reason, perhaps timeout.");
             }
         }
         else
         {
             this.logger.warn("Attempting to abort a batch invocation on a rig that does not support batch control.");
             operation.setSuccess(false);
             error.setCode(10);
             error.setReason("Batch control not supported.");
         }
         return response;
     }
 
     @Override
     public GetBatchControlStatusResponse getBatchControlStatus(final GetBatchControlStatus statusRequest)
     {
         /* Request parameters. */
         final String requestor = statusRequest.getGetBatchControlStatus().getRequestor();
         final String identTok = statusRequest.getGetBatchControlStatus().getIdentityToken();
         this.logger.debug("Received batch status request with parameters: identity token=" + identTok + 
                 ", requestor=" + requestor + ".");
         
         /* Response parameters. */
         final GetBatchControlStatusResponse response = new GetBatchControlStatusResponse();
         final BatchStatusResponseType status = new BatchStatusResponseType();
         response.setGetBatchControlStatusResponse(status);
         
         if (!(this.rig instanceof IRigControl))
         {
             this.logger.info("Rig does not support batch control, so sending a not supported status..");
             status.setProgress("-1");
             status.setState(BatchState.NOT_SUPPORTED);
         }
         else if (!(this.isSourceAuthenticated(identTok) ||
                 this.rig.hasPermission(requestor, Session.SLAVE_PASSIVE)))
         {
             this.logger.info("Requestor " + requestor + " does not have permission to abort batch control.");
             status.setProgress("-1");
             status.setState(BatchState.NOT_SUPPORTED);
         }
         else
         {
             IRigControl control = (IRigControl)this.rig;
             status.setProgress(String.valueOf(control.getBatchProgress()));
             
             BatchResults res = control.getBatchResults();
             switch (control.getBatchState())
             {
                 case ABORTED:
                     /* Falls through. */
                 case COMPLETE:
                     status.setState(BatchState.COMPLETE);
                     status.setResultFilePath(res.getResultsFiles());
                     status.setExitCode(res.getExitCode());
                     if (res.getStandardOut() != null) status.setStdout(res.getStandardOut());
                     if (res.getStandardErr() != null) status.setStderr(res.getStandardErr());
                     break;
                 case CLEAR:
                     status.setState(BatchState.CLEAR);
                     break;
                 case IN_PROGRESS:
                     status.setState(BatchState.IN_PROGRESS);
                     if (res.getStandardOut() != null) status.setStdout(res.getStandardOut());
                     if (res.getStandardErr() != null) status.setStderr(res.getStandardErr());
                     break;
                 case FAILED:
                     status.setState(BatchState.FAILED);
                     status.setResultFilePath(res.getResultsFiles());
                     status.setExitCode(res.getExitCode());
                     if (res.getStandardOut() != null) status.setStdout(res.getStandardOut());
                     if (res.getStandardErr() != null) status.setStderr(res.getStandardErr());
                     break;
                 default:
                     status.setState(BatchState.NOT_SUPPORTED);
                     break;
             }
         }
         
         return response;
     }
 
     @Override
     public PerformPrimitiveControlResponse performPrimitiveControl(final PerformPrimitiveControl primRequest)
     {
         /* Request parameters. */
         final PrimitiveControlRequestType request = primRequest.getPerformPrimitiveControl();
         final String requestor = request.getRequestor();
         final StringBuilder builder = new StringBuilder();
         builder.append("Recevied primitive control request with params: requestor=");
         builder.append(requestor);
         
         final PrimitiveRequest primitiveRequest = new PrimitiveRequest();
         
         /* Controller - action. */
         builder.append(", controller=");
         builder.append(request.getController());
         primitiveRequest.setController(request.getController());
         builder.append(", action=");
         builder.append(request.getAction());
         primitiveRequest.setAction(request.getAction());
         
         /* Parameter list. */
         int pCount = 1;
         for (ParamType param : request.getParam())
         {
             builder.append(", param");
             builder.append(pCount);
             builder.append('=');
             builder.append(param.getName());
             builder.append(':');
             builder.append(param.getValue());
             primitiveRequest.addParameter(param.getName(), param.getValue());
             ++pCount;
         }
 
         /* Response parameters. */
         final PerformPrimitiveControlResponse response = new PerformPrimitiveControlResponse();
         final PrimitiveControlResponseType control = new PrimitiveControlResponseType();
         response.setPerformPrimitiveControlResponse(control);
         final ErrorType error = new ErrorType();
         error.setCode(0);
         error.setOperation("Primitive control on controller " + request.getController() + " and action " + 
                 request.getAction() + ".");
         error.setReason("");
         control.setError(error);
         control.setWasSuccessful("");
         
         if (!(this.isSourceAuthenticated(request.getIdentityToken()) || 
                 this.rig.hasPermission(requestor, Session.SLAVE_ACTIVE)))
         {
             /* Requestor does not have permission to request primitive control. */
             this.logger.warn("Requestor " + requestor + " does not have permission to request primitive control.");
             control.setSuccess(false);
             error.setCode(3);
             error.setReason("Invalid permission.");
         }
         else if (this.rig instanceof IRigControl)
         {
             final IRigControl controlledRig = (IRigControl)this.rig;
             final PrimitiveResponse primitiveResponse = controlledRig.performPrimitive(primitiveRequest);
             control.setSuccess(primitiveResponse.wasSuccessful());
             error.setCode(primitiveResponse.getErrorCode());
             error.setReason(primitiveResponse.getErrorReason() != null ? primitiveResponse.getErrorReason() : "");
             control.setWasSuccessful(String.valueOf(primitiveResponse.wasSuccessful()));
             
             /* Results. */            
             final List<ParamType> results = new ArrayList<ParamType>();
             ParamType resultParam;
             for (Entry<String, String> result : primitiveResponse.getResults().entrySet())
             {
                 resultParam = new ParamType();
                 resultParam.setName(result.getKey());
                 resultParam.setValue(result.getValue());
                 results.add(resultParam);
             }
             control.setResult(results.toArray(new ParamType[results.size()]));
         }
         else
         {
             /* Controlling a rig not supported. */
             this.logger.warn("Primitive control not supported on this rig type.");
             control.setSuccess(false);
             error.setCode(14);
             error.setReason("Primitive control not supported.");
             control.setWasSuccessful("FAILED - Not supported.");
         }
         
         return response;
     }
 
     @Override
     public GetAttributeResponse getAttribute(final GetAttribute attrRequest)
     {
         /* Request parameters. */
         final AttributeRequestType request = attrRequest.getGetAttribute();
         final String attrName = request.getAttribute();
         final String requestor = request.getRequestor();
         final String ident = request.getIdentityToken();
         this.logger.debug("Received get attribute request with parameters: requestor=" + requestor + ", attribute=" +
                 attrName + ".");
         
         /* Response parameters. */
         final GetAttributeResponse response = new GetAttributeResponse();
         final AttributeResponseType attrResponse = new AttributeResponseType();
         response.setGetAttributeResponse(attrResponse);
         attrResponse.setAttribute(attrName);
         final AttributeResponseTypeChoice choice = new AttributeResponseTypeChoice();
         attrResponse.setAttributeResponseTypeChoice(choice);
 
         
         String attrValue;
         final ErrorType error = new ErrorType();
         error.setOperation("Finding attribute " + attrName + '.');
         if (!(this.isSourceAuthenticated(ident) || this.rig.hasPermission(requestor, Session.SLAVE_PASSIVE)))
         {
             this.logger.warn("Unable to provide attribute value for " + attrName + " because of invalid permission.");
             
             choice.setError(error);
             error.setCode(3);
             error.setReason("Invalid permission.");
         }
         else if ((attrValue = this.rig.getRigAttribute(attrName)) == null) // Check if the attribute was found
         {
             this.logger.warn("Unable to provide attribute value for " + attrName + " because it is not found.");
             choice.setError(error);
             error.setCode(9);
             error.setReason("Attribute " + attrName + " not found.");
         }
         else // All good!
         {
             this.logger.info("Found attribute value " + attrValue + " for request attribute " + attrName + ".");
             choice.setValue(attrValue);
         }
                 
         return response;
     }
 
     @Override
     public GetStatusResponse getStatus(final GetStatus statusRequest)
     {
         this.logger.debug("Received get status request.");
         
         /* Response parameters. */
         final GetStatusResponse statusResponse = new GetStatusResponse();
         final StatusResponseType status = new StatusResponseType();
         statusResponse.setGetStatusResponse(status);
         
         /* Maintenance status. */
         final boolean isInMaintenance = !this.rig.isNotInMaintenance();
         status.setIsInMaintenance(isInMaintenance);
         if (isInMaintenance)
         {
             status.setMaintenanceReason(this.rig.getMaintenanceReason());
         }
         
         /* Monitor status. */
         final boolean isMonitorFailed = !this.rig.isMonitorStatusGood();
         status.setIsMonitorFailed(isMonitorFailed);
         if (isMonitorFailed)
         {
             status.setMonitorReason(this.rig.getMonitorReason());
         }
 
         /* Session status. */
         final boolean inSession = this.rig.isSessionActive();
         status.setIsInSession(inSession);
         if (inSession)
         {
             for (Entry<String, Session> user : this.rig.getSessionUsers().entrySet())
             {
                 if (user.getValue() == Session.MASTER)
                 {
                     status.setSessionUser(user.getKey());
                 }
                 else if (user.getValue() == Session.SLAVE_ACTIVE || user.getValue() == Session.SLAVE_PASSIVE)
                 {
                     status.addSlaveUsers(user.getKey());
                 }
             }
         }
         
         return statusResponse;
     }
 
     @Override
     public SetMaintenanceResponse setMaintenance(final SetMaintenance maintenRequest)
     {
         /* Request parameters. */
         final MaintenanceRequestType request = maintenRequest.getSetMaintenance();
         this.logger.debug("Received set maintenance request with parameters: run tests=" + request.getRunTests() + 
                 ", put offline=" + request.getPutOffline() + ".");
         
         /* Response parameters. */
         final SetMaintenanceResponse response = new SetMaintenanceResponse();
         final OperationResponseType operation = new OperationResponseType();
         response.setSetMaintenanceResponse(operation);
         final ErrorType error = new ErrorType();
         error.setOperation("Setting maintenance to "  + (request.getPutOffline() ? "offline" : "online") + ".");
         error.setReason("");
         operation.setError(error);
         
         if (!this.isSourceAuthenticated(request.getIdentityToken()))
         {
             this.logger.warn("Unable to put the rig offline because of invalid permission.");
             operation.setSuccess(false);
             error.setCode(3);
             error.setReason("Invalid permission.");
         }
         /* DODGY The reason why the rig is going into maintenance should be communicated at request.
          * However, I'm far too lazy to fix this now. */
         else if (this.rig.setMaintenance(request.getPutOffline(), "User request.", request.getRunTests()))
         {
             this.logger.info("Successfully put the rig to state " + (request.getPutOffline() ? "offline" : "online") +
                 ".");
             operation.setSuccess(true);
         }
         else
         {
             this.logger.warn("Setting maintenance failed miserably for some unknown reason (perhaps a bug).");
             operation.setSuccess(false);
             error.setCode(16);
             error.setReason("Unknown - check the source Luke.");
         }
         
         return response;
     }
 
     @Override
     public SetTestIntervalResponse setTestInterval(final SetTestInterval interRequest)
     {
         /* Request parameters. */
         final TestIntervalRequestType request = interRequest.getSetTestInterval();
         this.logger.debug("Set test interval request received with parameters: interval=" + request.getInterval() + 
                 ".");
         
         /* Response parameters. */
         final SetTestIntervalResponse response = new SetTestIntervalResponse();
         final OperationResponseType operation = new OperationResponseType();
         response.setSetTestIntervalResponse(operation);
         final ErrorType error = new ErrorType();
         error.setOperation("Changing test interval to " + request.getInterval() + ".");
         error.setReason("");
         operation.setError(error);
         
         if (!this.isSourceAuthenticated(request.getIdentityToken()))
         {
             this.logger.warn("Failed to set test interval because of invalid permission.");
             operation.setSuccess(false);
             error.setCode(3);
             error.setReason("Invalid permission.");
         }
         else if (this.rig.setInterval(request.getInterval()))
         {
            this.logger.info("Successfully set the test interval to " + request.getInterval() + ".");
             operation.setSuccess(true);
         }
         else
         {
             this.logger.warn("Failed to set the test interval because of some unknown error (possibly a bug).");
             operation.setSuccess(false);
             error.setCode(16);
             error.setReason("Unknown error.");
         }
         
         return response;
     }
 
     @Override
     public IsActivityDetectableResponse isActivityDetectable(final IsActivityDetectable detectRequest)
     {
         this.logger.debug("Received detect activity request received.");
         
         /* Response parameters. */
         final IsActivityDetectableResponse response = new IsActivityDetectableResponse();
         final ActivityDetectableType detect = new ActivityDetectableType();
         response.setIsActivityDetectableResponse(detect);
         
         detect.setActivity(this.rig.isActivityDetected());
         return response;
     }
 
     /**
      * If the source is the scheduling server that is currently registered.
      * 
      * @param identTok identity token of requestor
      * @return true if the token is that provided by the scheduling server
      */
     private boolean isSourceAuthenticated(final String identTok)
     {
         String toks[] = StatusUpdater.getServerIdentityTokens();
         
         /* The normal case of the scheduling server identification. */
         if ((toks[0] != null && toks[0].equals(identTok)) ||   // The currently registered identity token
                 (toks[1] != null && toks[1].equals(identTok))) // The token previously registered
         {
             return true;
         }
         
         /* If not registered, accept messages from all. */
         if (!StatusUpdater.isRegistered())
         {
             this.logger.info("Authenticated message because the rig client is not registered with a scheduling " +
             		"server. Be wary using this, it may leave the rig client in an inconsistent state when " +
             		"registered when the rig client does register.");
             return true;
         }
         
         return false;
     }
 }
