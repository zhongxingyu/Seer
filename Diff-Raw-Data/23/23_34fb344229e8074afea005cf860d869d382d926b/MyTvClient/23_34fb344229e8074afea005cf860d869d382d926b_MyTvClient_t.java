 /**
  * Copyright (c) 2011, SOCIETIES Consortium (WATERFORD INSTITUTE OF TECHNOLOGY (TSSG), HERIOT-WATT UNIVERSITY (HWU), SOLUTA.NET 
  * (SN), GERMAN AEROSPACE CENTRE (Deutsches Zentrum fuer Luft- und Raumfahrt e.V.) (DLR), Zavod za varnostne tehnologije
  * informacijske družbe in elektronsko poslovanje (SETCCE), INSTITUTE OF COMMUNICATION AND COMPUTER SYSTEMS (ICCS), LAKE
  * COMMUNICATIONS (LAKE), INTEL PERFORMANCE LEARNING SOLUTIONS LTD (INTEL), PORTUGAL TELECOM INOVAÇÃO, SA (PTIN), IBM Corp., 
  * INSTITUT TELECOM (ITSUD), AMITEC DIACHYTI EFYIA PLIROFORIKI KAI EPIKINONIES ETERIA PERIORISMENIS EFTHINIS (AMITEC), TELECOM 
  * ITALIA S.p.a.(TI),  TRIALOG (TRIALOG), Stiftelsen SINTEF (SINTEF), NEC EUROPE LTD (NEC))
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
  * conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *    disclaimer in the documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
  * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT 
  * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package ac.hw.mytv;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.societies.api.comm.xmpp.interfaces.ICommManager;
 import org.societies.api.css.devicemgmt.display.IDisplayDriver;
 import org.societies.api.css.devicemgmt.display.IDisplayableService;
 import org.societies.api.identity.IIdentity;
 import org.societies.api.identity.Requestor;
import org.societies.api.identity.RequestorService;
 import org.societies.api.osgi.event.CSSEvent;
 import org.societies.api.osgi.event.CSSEventConstants;
 import org.societies.api.osgi.event.EventListener;
 import org.societies.api.osgi.event.EventTypes;
 import org.societies.api.osgi.event.IEventMgr;
 import org.societies.api.osgi.event.InternalEvent;
 import org.societies.api.personalisation.mgmt.IPersonalisationManager;
 import org.societies.api.personalisation.model.Action;
 import org.societies.api.personalisation.model.IAction;
 import org.societies.api.personalisation.model.IActionConsumer;
 import org.societies.api.schema.servicelifecycle.model.ServiceResourceIdentifier;
 import org.societies.api.services.IServices;
 import org.societies.api.useragent.monitoring.IUserActionMonitor;
 
 public class MyTvClient extends EventListener implements IDisplayableService, IActionConsumer, IMyTv{
 
 	SocketClient socketClient;
 	SocketServer socketServer;
 	CommandHandler commandHandler;
 	IUserActionMonitor uam;
 	IPersonalisationManager persoMgr;
 	IIdentity userID;
 	IServices serviceMgmt;
 	IEventMgr eventMgr;
 	ICommManager commsMgr;
 	IDisplayDriver displayDriver;
 	ServiceResourceIdentifier myServiceID;
 	String myServiceName;
 	String myServiceType;
 	URL myUIExeLocation;
 	List<String> myServiceTypes;
 	Logger LOG = LoggerFactory.getLogger(MyTvClient.class);
 	
 	//personalisable parameters
 	int currentChannel;
 	boolean mutedState;
 
 	public MyTvClient(){
 	}
 
 	public void initialiseMyTvClient(){
 		//set service descriptors
 		myServiceName = "MyTv";
 		myServiceType = "media";
 		myServiceTypes = new ArrayList<String>();
 		myServiceTypes.add(myServiceType);
 
 		//initialise settings
 		currentChannel = 0;
 		mutedState = true;
 
 		//register as a displayable service
 		try {
 			myUIExeLocation = new URL("http://www.macs.hw.ac.uk/~ceesmm1/societies/mytv/MyTvUI.exe");
 			displayDriver.registerDisplayableService(
 					this, 
 					myServiceName, 
 					myUIExeLocation, 
 					true);
 		} catch (MalformedURLException e) {
 			LOG.error("Could not register as displayable service with display driver");
 			e.printStackTrace();
 		}
 
 		//register for portal started events
 		registerForDisplayEvents();
 
 		//start server listening for connections from GUI
 		commandHandler = new CommandHandler();
 		socketServer = new SocketServer(commandHandler);
 		socketServer.start();
 	}
 
 	/*
 	 * Register for display events from portal
 	 */
 	private void registerForDisplayEvents(){
 		String eventFilter = "(&" + 
 				"(" + CSSEventConstants.EVENT_NAME + "=displayUpdate)" +
 				"(" + CSSEventConstants.EVENT_SOURCE + "=org/societies/css/device)" +
 				")";
 		this.eventMgr.subscribeInternalEvent(this, new String[]{EventTypes.DISPLAY_EVENT}, eventFilter);
 		LOG.debug("Subscribed to "+EventTypes.DISPLAY_EVENT+" events");
 	}
 
 
 
 	/*
 	 * These methods handle events from the Portal
 	 */
 	@Override
 	public void handleExternalEvent(CSSEvent event) {
 		LOG.debug("Received external display event from portal: "+event.geteventName());
 	}
 
 	@Override
 	public void handleInternalEvent(InternalEvent event) {
 		LOG.debug("Received internal display event from portal: "+event.geteventName());
 		
 		//get user ID
 		userID = commsMgr.getIdManager().getThisNetworkNode();
 		LOG.debug("userID = "+userID.toString());
 
 		//get service ID
 		myServiceID = serviceMgmt.getMyServiceId(MyTvClient.class);
 		LOG.debug("client serviceID = "+myServiceID.toString());
 	}
 
 
 
 	/*
 	 * These methods are called by the Portal when my GUI is displayed/hidden
 	 */
 	@Override
 	public void serviceStarted(String guiIpAddress){
 		//service is started
 	}
 
 	@Override
 	public void serviceStopped(String guiIpAddress){
 		//service is stopped
 	}
 
 
 	/*
 	 * These methods are called by PersonalisationManager and User Agent
 	 * (non-Javadoc)
 	 * @see org.societies.api.personalisation.model.IActionConsumer#getServiceIdentifier()
 	 */
 	@Override
 	public ServiceResourceIdentifier getServiceIdentifier() {
 		return myServiceID;
 	}
 
 	@Override
 	public String getServiceType() {
 		return myServiceType;
 	}
 
 	@Override
 	public List<String> getServiceTypes() {
 		return myServiceTypes;
 	}
 
 	@Override
 	public boolean setIAction(IIdentity identity, IAction action) {
 
 		return false;
 	}
 
 	/*private void setChannel(int channel){
 
 	}
 
 	private void setMuted(String muted){
 
 	}*/
 
 	public void setUam(IUserActionMonitor uam){
 		this.uam = uam;
 	}
 
 	public void setPersoMgr(IPersonalisationManager persoMgr){
 		this.persoMgr = persoMgr;
 	}
 
 	public void setServiceMgmt(IServices serviceMgmt){
 		this.serviceMgmt = serviceMgmt;
 	}
 
 	public void setEventMgr(IEventMgr eventMgr){
 		this.eventMgr = eventMgr;
 	}
 
 	public void setCommsMgr(ICommManager commsMgr){
 		this.commsMgr = commsMgr;
 	}
 
 	public void setDisplayDriver(IDisplayDriver displayDriver){
 		this.displayDriver = displayDriver;
 	}
 
 	public static void main(String[] args) throws IOException{
 		new MyTvClient();
 	}
 
 
 
 
 	/*
 	 * Handle commands from GUI
 	 */
 	public class CommandHandler{
 		public void connectToGUI(String gui_ip){
			LOG.debug("Connecting to service GUI on IP address: "+gui_ip);
 			//disconnect any existing connections
 			if(socketClient != null){
 				if(socketClient.isConnected()){
 					socketClient.disconnect();
 				}
 			}
 			//create new connection
 			socketClient = new SocketClient(gui_ip);
 			if(socketClient.connect()){
 				if(socketClient.sendMessage(
 						"START_MSG\n" +
 						"USER_SESSION_STARTED\n" +
 						"END_MSG")){
 					LOG.debug("Handshake complete:  ServiceClient -> GUI");
 				}else{
 					LOG.error("Handshake failed: ServiceClient -> GUI");
 				}
 			}else{
 				LOG.error("Could not connect to service GUI");
 			}
 		}
 
 		public void disconnectFromGUI(){
 			socketClient.disconnect();
 		}
 
 		public void processUserAction(String parameterName, String value){
			LOG.debug("Processing user action: "+parameterName+" = "+value);
 
 			if(parameterName.equalsIgnoreCase("channel")){
 				currentChannel = new Integer(value).intValue();
 			}else if(parameterName.equalsIgnoreCase("muted")){
 				mutedState = new Boolean(value).booleanValue();
 			}
 
 			//create action object and send to uam
 			IAction action = new Action(myServiceID, myServiceType, parameterName, value);
 			uam.monitor(userID, action);
 		}
 
 		public String getChannelPreference(){
 			LOG.debug("Getting channel preference from personalisation manager");
 			String result = "PREFERENCE-ERROR";
 			try {
				RequestorService requestor = new RequestorService(userID, myServiceID);
				IAction outcome = persoMgr.getPreference(requestor, userID, myServiceType, myServiceID, "channel").get();
 				if(outcome!=null){
 					LOG.debug("Successfully retrieved channel preference outcome: "+outcome.getvalue());
 					result = outcome.getvalue();
 				}else{
 					LOG.debug("No channel preference was found");
 				}
 			} catch (InterruptedException e) {
				LOG.debug("Error retrieving preference");
 			} catch (ExecutionException e) {
				LOG.debug("Error retrieving preference");
 			}
 			LOG.debug("Preference request result = "+result);
 			return result;
 		}
 
 		public String getMutedPreference(){
 			String result = "PREFERENCE-ERROR";
 			try {
 				IAction action = persoMgr.getPreference((Requestor)userID, userID, myServiceType, myServiceID, "muted").get();
 				if(action!=null){
 					result = action.getvalue();
 				}
 			} catch (InterruptedException e) {
				LOG.debug("Error retrieving preference");
 			} catch (ExecutionException e) {
				LOG.debug("Error retrieving preference");
 			}
 			return result;
 		}
 	}
 }
