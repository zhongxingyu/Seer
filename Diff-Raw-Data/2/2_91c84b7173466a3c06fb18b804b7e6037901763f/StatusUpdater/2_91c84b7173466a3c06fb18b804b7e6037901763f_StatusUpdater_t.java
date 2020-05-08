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
  * @date 23rd February 2010
  */
 package au.edu.uts.eng.remotelabs.rigclient.status;
 
 import java.net.ConnectException;
 import java.net.UnknownHostException;
 import java.rmi.RemoteException;
 import java.util.Arrays;
 import java.util.Calendar;
 
 import org.apache.axis2.AxisFault;
 import org.apache.axis2.databinding.types.URI;
 
 import au.edu.uts.eng.remotelabs.rigclient.rig.IRig;
 import au.edu.uts.eng.remotelabs.rigclient.status.types.ProviderResponse;
 import au.edu.uts.eng.remotelabs.rigclient.status.types.RegisterRig;
 import au.edu.uts.eng.remotelabs.rigclient.status.types.RegisterRigResponse;
 import au.edu.uts.eng.remotelabs.rigclient.status.types.RegisterRigType;
 import au.edu.uts.eng.remotelabs.rigclient.status.types.RemoveRig;
 import au.edu.uts.eng.remotelabs.rigclient.status.types.RemoveRigResponse;
 import au.edu.uts.eng.remotelabs.rigclient.status.types.RemoveRigType;
 import au.edu.uts.eng.remotelabs.rigclient.status.types.StatusType;
 import au.edu.uts.eng.remotelabs.rigclient.status.types.UpdateRigStatus;
 import au.edu.uts.eng.remotelabs.rigclient.status.types.UpdateRigStatusResponse;
 import au.edu.uts.eng.remotelabs.rigclient.status.types.UpdateRigType;
 import au.edu.uts.eng.remotelabs.rigclient.type.RigFactory;
 import au.edu.uts.eng.remotelabs.rigclient.util.ConfigFactory;
 import au.edu.uts.eng.remotelabs.rigclient.util.IConfig;
 import au.edu.uts.eng.remotelabs.rigclient.util.ILogger;
 import au.edu.uts.eng.remotelabs.rigclient.util.LoggerFactory;
 
 /**
  * Registers the rig client with a scheduling server, then periodically 
  * updates the scheduling server with the rig clients status.
  * <br />
  * Interrupting the status updater will cause the status updater to 
  * shutdown by removing the scheduling server's rig client registration.
  */
 public class StatusUpdater implements Runnable
 {
     /** Suffix for the Scheduling Server Rig Provider SOAP interface 
      *  end point. */
     public static final String SS_URL_SUFFIX = "/SchedulingServer-RigProvider/services/RigProvider";
     
     /** Suffix for the Scheduling Server v2.0 Local Rig Provider SOAP 
      * interface. */
     public static final String SSv2_URL_SUFFIX = "/SchedulingServer-LocalRigProvider/services/LocalRigProvider";
     
     /** Default status update interval. */
     public static final int DEFAULT_UPDATE_PERIOD = 30;
     
     /** Scheduling server SOAP stub. */
     private SchedulingServerProviderStub schedServerStub;
     
     /** Scheduling server end point. */
     private String endPoint;
     
     /** The actual rig type class. */
     private final IRig rig;
     
     /** The address the rig client is listening on. */
     private final URI rigClientAddress;
     
     /** The period to provide status updates. */
     private int updatePeriod;
     
     /** The identity tokens from registration. The 0th value is current 
      *  identity token, the 1st identity token is the identity token 
      *  directly before the current identity token. */
     private final static String identToks[] = new String[2];
                         
     /** Whether the rig client is registered. */
     private static boolean isRegistered;
     
     /** Flag to specify the status updater should 
     
     /** Logger. */
     private final ILogger logger;
     
     public StatusUpdater(String addr) throws Exception
     {
         this.logger = LoggerFactory.getLoggerInstance();
         
         this.rig = RigFactory.getRigInstance();
         this.rigClientAddress = new URI(addr);
         
         /* Load the configuration properties. */
         IConfig config = ConfigFactory.getInstance();
         
         /* Generate the end point URL. */
         StringBuilder ep = new StringBuilder();
         ep.append("http://");
         String tmp = config.getProperty("Scheduling_Server_Address");
         if (tmp == null || tmp.length() < 1)
         {
             this.logger.fatal("Unable to load the Scheduling Server address. Ensure the property " +
             		"'Scheduling_Server_Address' is set with a valid host name or IP address.");
             throw new Exception("Unable to load scheduling server address.");
         }
         this.logger.debug("Loaded scheduling server address ('Scheduling_Server_Address') property as " + tmp + '.');
         ep.append(tmp);
         
         tmp = config.getProperty("Scheduling_Server_Port", "8080");
         try
         {
             ep.append(':');
             ep.append(Integer.parseInt(tmp)); // Check to ensure the port number is valid
         }
         catch (NumberFormatException ex)
         {
             this.logger.fatal("Invalid Scheduling Server port number loaded. Ensure the property " +
             		"'Scheduling_Server_Port' is either set to a valid port number or not set (defaults to 8080).");
             throw new Exception("Invalid port number for scheduling server.");
         }
         
         ep.append(StatusUpdater.SS_URL_SUFFIX);
         this.endPoint = ep.toString();
         this.logger.info("Scheduling server end point address is " + this.endPoint + '.');
         
         tmp = config.getProperty("Scheduling_Server_Update_Period", String.valueOf(StatusUpdater.DEFAULT_UPDATE_PERIOD));
         try
         {
             this.updatePeriod = Integer.parseInt(tmp);
             this.logger.info("Going to update the scheduling server every " + this.updatePeriod + " seconds.");
         }
         catch (NumberFormatException ex)
         {
             this.logger.warn("Invalid scheduling server status update period set ('Scheduling_Server_Update_Period'), " +
                     "using the default of every " + StatusUpdater.DEFAULT_UPDATE_PERIOD + " seconds.");
             this.updatePeriod = StatusUpdater.DEFAULT_UPDATE_PERIOD;
         }
     }
     
     @Override
     public void run()
     {
         while (!Thread.interrupted())
         {
             try
             {
                 if (this.schedServerStub == null)
                 {
                     this.schedServerStub = new SchedulingServerProviderStub(this.endPoint);
                 }
                 
                 ProviderResponse provResp;
                 if (StatusUpdater.isRegistered)
                 {
                     /* --------------------------------------------------------
                      * -- Registered - providing status update.              --
                      * ------------------------------------------------------*/
                     /* 1) Set up message. */
                     UpdateRigStatus request = new UpdateRigStatus();
                     UpdateRigType updateType = new UpdateRigType();
                     request.setUpdateRigStatus(updateType);
                     updateType.setName(this.rig.getName());
                     
                     StatusType status = new StatusType();
                     updateType.setStatus(status);
                     status.setIsOnline(this.rig.isMonitorStatusGood());
                     if (!this.rig.isMonitorStatusGood()) status.setOfflineReason(this.rig.getMonitorReason());
                     
                     /* 2) Send message. */
                     UpdateRigStatusResponse response = this.schedServerStub.updateRigStatus(request);
                     provResp = response.getUpdateRigStatusResponse();
                 }
                 else
                 {
                     /* --------------------------------------------------------
                      * -- Not registered - attempt to register.              --
                      * ------------------------------------------------------*/
                     /* 1) Set up message. */
                     RegisterRig request = new RegisterRig();
                     RegisterRigType registerType = new RegisterRigType();
                     request.setRegisterRig(registerType);
                     registerType.setName(this.rig.getName());
                     registerType.setType(this.rig.getType());
                     
                     String caps[] = this.rig.getCapabilities();
                     StringBuilder capBuilder = new StringBuilder();
                     for (int i = 0; i < caps.length; i++)
                     {
                         capBuilder.append(caps[i]);
                         if ((i + 1) != caps.length) capBuilder.append(',');
                     }
                     registerType.setCapabilities(capBuilder.toString());
                     registerType.setContactUrl(this.rigClientAddress);
                     
                     StatusType status = new StatusType();
                     registerType.setStatus(status);
                     status.setIsOnline(this.rig.isMonitorStatusGood());
                     status.setOfflineReason(this.rig.getMonitorReason());
                     
                     /* 2) Send message. */
                     RegisterRigResponse response = this.schedServerStub.registerRig(request);
                     provResp = response.getRegisterRigResponse();
                 }
                 
                 /* 3) Check response. */
                 if (provResp.getSuccessful())
                 {
                     this.logger.debug("Successfully communicated with the scheduling server to " +
                             (StatusUpdater.isRegistered ? "update the rig status." : "register the rig."));
                     StatusUpdater.isRegistered = true;
                     String identTok = provResp.getIdentityToken();
                     if (identTok != null && !identTok.equals(identToks[0]))
                     {
                         synchronized (StatusUpdater.class)
                         {
                             StatusUpdater.identToks[1] = StatusUpdater.identToks[0];
                             StatusUpdater.identToks[0] = identTok;
                         }
                         this.logger.info("Obtained new identity token with value '" + StatusUpdater.identToks[0] + "'.");
                         StatusUpdater.isRegistered = true;
                     }
                 }
                 else
                 {
                     /* Assuming the Scheduling Server does not have the rig client registered. */
                     synchronized (StatusUpdater.class)
                     {
                         StatusUpdater.identToks[1] = null;
                         StatusUpdater.identToks[0] = null;
                     }
                     this.logger.error("Failed to " + (StatusUpdater.isRegistered ? "update" : "register") + " the " +
                     		"Scheduling Server. The provided reason for failing is '" + 
                     		this.getNiceErrorMessage(provResp.getErrorReason()) + "'.");
                     StatusUpdater.isRegistered = false;
                 }
             }
             catch (AxisFault ex)
             {   
                 synchronized (StatusUpdater.class)
                 {
                     StatusUpdater.identToks[1] = null;
                     StatusUpdater.identToks[0] = null;
                 }
                 StatusUpdater.isRegistered = false;
                 this.schedServerStub = null;
                 
                 if (ex.getCause() instanceof ConnectException)
                 {
                     this.logger.error("Unable to" + (StatusUpdater.isRegistered ? " update the rigs status" : 
                            " register the rig") + ". Error reason is connection error: " + ex.getMessage() +
                             ". Ensure the Scheduling Server is running and listening on the configured port number.");
                 }
                 else if (ex.getCause() instanceof UnknownHostException)
                 {
                     this.logger.error("Unable to" + (StatusUpdater.isRegistered ? " update the rig's status" : 
                             " register the rig") + ". Error reason is unknown host " + ex.getMessage() + ". " +
                             "Configure the scheduling server host as a valid host name.");
                 }
                 else if (ex.getReason() != null && ex.getReason().contains("404"))
                 {
                     /* May be an earlier Scheduling Server version. */
                     this.logger.error("Unable to" + (StatusUpdater.isRegistered ? " update the rig's status" : 
                             " register the rig") + ". Error reason is '404 Not Found' which may mean a different " +
                             " version of the Scheduling Server is running.");
                     if (this.endPoint.endsWith(SS_URL_SUFFIX))
                     {
                         this.endPoint = this.endPoint.replace(SS_URL_SUFFIX, SSv2_URL_SUFFIX);
                         this.logger.error("Tried Scheduling Server v3+ update address which failed, going to try Scheduling " +
                         		"Server v2 address '" + this.endPoint + "'.");
                     }
                     else
                     {
                         this.endPoint = this.endPoint.replace(SSv2_URL_SUFFIX, SS_URL_SUFFIX);
                         this.logger.error("Tried Scheduling Server v2 update address which failed, going to try Scheduling " +
                                 "Server v3+ address '" + this.endPoint + "'.");
                     }
                 }
                 else
                 {
                     this.logger.error("Unable to" + (StatusUpdater.isRegistered ? " update the rig's status" : 
                     " register the rig") + ". Error reason is '" + ex.getReason() + "'.");
                 }
                 
                 if (this.rig.isSessionActive())
                 {
                     this.logger.error("Terminating an in progress session because of error updating the rig's status.");
                     this.rig.revoke();
                 }
             }
             catch (RemoteException ex)
             {
                 synchronized (StatusUpdater.class)
                 {
                     StatusUpdater.identToks[1] = null;
                     StatusUpdater.identToks[0] = null;
                 }
                 StatusUpdater.isRegistered = false;
                 this.schedServerStub = null;
                 
                 this.logger.error("Remote exception when trying to" + (StatusUpdater.isRegistered ? " update the rigs " +
                 		"status" : " register the rig") + ". Exception message is '" + ex.getMessage() + "'.");
                 
                 if (this.rig.isSessionActive())
                 {
                     this.logger.error("Terminating an in progress session because of error updating the rig's status.");
                     this.rig.revoke();
                 }
             }
             
             try
             {
                 Thread.sleep(this.updatePeriod * 1000);
             }
             catch (InterruptedException e)
             {
                 Thread.currentThread().interrupt();
             }
         }
         
         /* --------------------------------------------------------------------
          * -- 3) Unregister the rig.                                         --
          * ------------------------------------------------------------------*/
         if (!StatusUpdater.isRegistered) return;
         this.logger.debug("Received interrupt for the status updater, removing the rig client's registration.");
         
         /* 1) Set up message. */
         RemoveRig request = new RemoveRig();
         RemoveRigType removeType = new RemoveRigType();
         request.setRemoveRig(removeType);
         removeType.setName(this.rig.getName());
         Calendar cal = Calendar.getInstance();
         removeType.setRemovalReason("Shutting down at time " + cal.get(Calendar.HOUR_OF_DAY) + ':' + 
                 cal.get(Calendar.MINUTE) + ':' + cal.get(Calendar.SECOND) + " on " + cal.get(Calendar.DAY_OF_MONTH) +
                 '/' + (cal.get(Calendar.MONTH) + 1) + '/' + cal.get(Calendar.YEAR) + '.');
         
         /* 2) Send message. */
         try
         {
             RemoveRigResponse response = this.schedServerStub.removeRig(request);
             if (response.getRemoveRigResponse().getSuccessful())
             {
                 this.logger.info("Successfully removed the rig client's scheduling server registration.");
             }
             else
             {
                 this.logger.error("Failed to remove the rig client's scheduling server registration with provided " +
                 		"error '" + this.getNiceErrorMessage(response.getRemoveRigResponse().getErrorReason()) + "'.");
             }
         }
         catch (RemoteException e)
         {
             this.logger.error("Failed to remove the scheduling servers registration because of remote exception " + 
                     " with error message '" + e.getMessage() + "'.");
         }
         
         StatusUpdater.isRegistered = false;
     }
     
     /**
      * Returns the current identity token and the identity token directly 
      * before the current identity token. The 0th value is the current
      * identity token and the 1st is the one directly before. 
      * <br />
      * If the rig client is not registered or recently registered, the 0th or
      * 1st values may be <code>null</code>.
      * 
      * @return current and last identity token
      */
     public static String[] getServerIdentityTokens()
     {
         synchronized (StatusUpdater.class)
         {
             return Arrays.copyOf(StatusUpdater.identToks, 2);
         }
     }
     
     /**
      * Returns <tt>true</tt> if the rig client is registered with a scheduling
      * server.
      * 
      * @return true if registered
      */
     public static boolean isRegistered()
     {
         return StatusUpdater.isRegistered;
     }
     
     /**
      * Returns a more descriptive error than is provided from the scheduling 
      * server, provided the error is a known type.
      * 
      * @param msg message from server
      * @return detailed error or provided message
      */
     private String getNiceErrorMessage(String msg)
     {
         if ("Exists".equals(msg))
         {
             return "a rig with the same name already exists";
         }
         else if ("Not registered".equals(msg))
         {
             return "the rig is not registered";
         }
         
         return msg;
     }
 }
