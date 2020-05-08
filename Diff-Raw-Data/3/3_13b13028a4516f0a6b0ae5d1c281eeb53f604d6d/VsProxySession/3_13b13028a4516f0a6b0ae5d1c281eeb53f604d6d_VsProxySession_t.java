 /**
  * Appia: Group communication and protocol composition framework library
  * Copyright 2006 University of Lisbon
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  *
  * Initial developer(s): Alexandre Pinto and Hugo Miranda.
  * Contributor(s): See Appia web page for a list of contributors.
  */
 package net.sf.appia.project.group.server;
 
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.UnknownHostException;
 import java.util.List;
 import java.util.concurrent.Semaphore;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.sf.appia.core.AppiaEventException;
 import net.sf.appia.core.AppiaException;
 import net.sf.appia.core.Channel;
 import net.sf.appia.core.Direction;
 import net.sf.appia.core.Event;
 import net.sf.appia.core.Session;
 import net.sf.appia.core.events.channel.ChannelInit;
 import net.sf.appia.core.events.channel.EchoEvent;
 import net.sf.appia.project.group.event.proxy.DecidedProxyEvent;
 import net.sf.appia.project.group.event.proxy.GroupSendableProxyEvent;
 import net.sf.appia.project.group.event.proxy.LeaveClientProxyEvent;
 import net.sf.appia.project.group.event.proxy.NewClientProxyEvent;
 import net.sf.appia.project.group.event.proxy.ProxyEvent;
 import net.sf.appia.project.group.event.proxy.BlockOkProxyEvent;
 import net.sf.appia.project.group.event.proxy.UpdateDecideProxyEvent;
 import net.sf.appia.project.group.event.proxy.UpdateProxyEvent;
 import net.sf.appia.project.group.event.stub.BlockOkStubEvent;
 import net.sf.appia.project.group.event.stub.GroupInitStubEvent;
 import net.sf.appia.project.group.event.stub.GroupSendableStubEvent;
 import net.sf.appia.project.group.event.stub.LeaveStubEvent;
 import net.sf.appia.project.group.event.stub.PongEvent;
 import net.sf.appia.project.group.event.stub.ShutUpStubEvent;
 import net.sf.appia.project.group.event.stub.StubEvent;
 import net.sf.appia.project.group.event.stub.ViewStubEvent;
 import net.sf.appia.protocols.common.RegisterSocketEvent;
 import net.sf.appia.protocols.group.AppiaGroupException;
 import net.sf.appia.protocols.group.Endpt;
 import net.sf.appia.protocols.group.Group;
 import net.sf.appia.protocols.group.ViewID;
 import net.sf.appia.protocols.group.ViewState;
 import net.sf.appia.protocols.group.events.GroupInit;
 import net.sf.appia.protocols.group.events.GroupSendableEvent;
 import net.sf.appia.protocols.group.intra.View;
 import net.sf.appia.protocols.group.sync.BlockOk;
 import net.sf.appia.xml.interfaces.InitializableSession;
 import net.sf.appia.xml.utils.SessionProperties;
 
 
 /**
  * This class defines a Proxy for View Synchrony
  * 
  * It has to channels to receive messages. One to receive messages from clients
  * and the other two comunicate with a Virtual Synchrony Qos with the other servers
  * 
  * @author Joao Trindade
  * @version 1.0
  */
 public class VsProxySession extends Session implements InitializableSession{
 
 	//To discover what of the two channels we are dealing with
 	private final Pattern textPattern = Pattern.compile(".*listen_.*");
 	private final Pattern drawPattern = Pattern.compile(".*_vs_.*");
 
 	//The addresses of the two channels of the proxy session
 	private int localport = -1; //The localport used for clients to connect to
 	private InetSocketAddress listenAddress;
 	private InetSocketAddress vsAddress;
 
 	//The gossip servers addresses 
 	private InetSocketAddress[] gossipServers = null;
 
 	//Our two channels
 	private Channel listenChannel; //Which will talk with clients
 	private Channel vsChannel; //Which will talk with the other servers
 
 	//Our server name - ONLY FOR DEBUG PORPOSES - No real usage
 	private String serverName;
 
 	//To store info for our View Synchrony Channel
 	private Endpt myEndpt;
 	private ViewState vs;
 
 	boolean flushMode = true;
 	private BlockOk pendingBlockOk;
 
 	private final Semaphore available = new Semaphore(1, true);
 
 	/*
 	 * Inits the paremeters for this session
 	 * 
 	 * @see net.sf.appia.xml.interfaces.InitializableSession#init(net.sf.appia.xml.utils.SessionProperties)
 	 */
 	public void init(SessionProperties params) {
 		localport = Integer.parseInt(params.getProperty("localport"));
 		serverName = params.getProperty("servername");
 
 		String gossipHost = params.getProperty("gossiphost");
 		int gossipPort = Integer.parseInt(params.getProperty("gossipport"));
 
 		try {
 			gossipServers = new InetSocketAddress[1];
 			gossipServers[0] = new InetSocketAddress(InetAddress.getByName(gossipHost),gossipPort);
 			System.out.println("Gossip: "+gossipServers[0]);
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/*
 	 * Constructs a VsProxy Session
 	 */
 	public VsProxySession(VsProxyLayer l) {
 		super(l);
 	}
 
 	/**************************** METHODS TO HANDLE EVENTS ********************************/
 
 	/**
 	 * Main event handler.
 	 * @param ev the event to handle.
 	 * 
 	 * @see net.sf.appia.core.Session#handle(net.sf.appia.core.Event)
 	 */
 	public void handle(Event ev) {
 
 //		synchronized (this) {
 		//		System.out.println("VsProxySession - "+ ev.getDir() 
 //		+" - Received an event type - " + ev.getClass());
 
 		try {
 			/********** Normal Events **********/
 			if (ev instanceof ChannelInit)
 				handleChannelInit((ChannelInit) ev);
 			else if (ev instanceof RegisterSocketEvent)
 				handleRegisterSocketEvent((RegisterSocketEvent) ev);
 			else if (ev instanceof BlockOk)
 				handleBlockOk((BlockOk) ev);
 			else if (ev instanceof View)
 				handleView((View) ev);
 
 			/********** Stub Events => Clients->Server **********/
 			else if (ev instanceof GroupInitStubEvent)
 				handleGroupInitStubEvent((GroupInitStubEvent) ev);
 			else if (ev instanceof LeaveStubEvent)
 				handleLeaveStubEvent((LeaveStubEvent) ev);
 			else if (ev instanceof GroupSendableStubEvent)
 				handleGroupSendableStubEvent((GroupSendableStubEvent) ev);	
 			else if (ev instanceof BlockOkStubEvent)
 				handleBlockOkStubEvent((BlockOkStubEvent) ev);	
 
 			/********** Stub Events => Server->Server **********/
 			else if (ev instanceof NewClientProxyEvent)
 				handleNewClientProxyEvent((NewClientProxyEvent) ev);
 			else if (ev instanceof LeaveClientProxyEvent)
 				handleLeaveClientProxyEvent((LeaveClientProxyEvent) ev);
 			else if (ev instanceof GroupSendableProxyEvent)
 				handleGroupSendableProxyEvent((GroupSendableProxyEvent) ev);
 			else if (ev instanceof BlockOkProxyEvent)
 				handleBlockOkProxyEvent((BlockOkProxyEvent) ev);
 			else if (ev instanceof DecidedProxyEvent)
 				handleDecidedProxyEvent((DecidedProxyEvent) ev);
 			else if (ev instanceof UpdateProxyEvent)
 				handleUpdateProxyEvent((UpdateProxyEvent) ev);		
 			else if (ev instanceof UpdateDecideProxyEvent)
 				handleUpdateDecideProxyEvent((UpdateDecideProxyEvent) ev);		
 
 			/********** Pong detecture - For failure detection ********/
 			else if (ev instanceof PongEvent){
 				handlePongEvent((PongEvent) ev);
 			}
 
 			/********* If we don't handle the event warn *******/
 			else{ 
 				System.out.println("VsProxySession: Event not treated - " + 
 						ev.getClass().toString());
 				ev.go();
 			}
 		} catch (AppiaException e) {
 			e.printStackTrace();
 		}
 	}
 
 
 	/************** START - Methods to handle stub events ************************
 	 * @throws AppiaEventException */
 
 	private void handleBlockOkStubEvent(BlockOkStubEvent event) throws AppiaEventException {
 		event.loadMessage();
 		System.out.println("Received BlockOkStubEvent from: "  + event.source);
 
 		//Get the parameters
 		String groupId = event.getGroupId();
 		Endpt clientEndpt = event.getEndpoint();
 		int viewVersion = VsClientManagement.getVsGroup(groupId).getCurrentVersion();
 
 		//Mute the client
 		VsClientManagement.muteClient(groupId, clientEndpt);
 
 		//Servers enconter themselfs in mute mode
 		if(this.flushMode == true){
 			//We need to check if EVERY client attached to me is quiet	
 			boolean allMuted = VsClientManagement.checkIfEveryAllAttachedToMeClientAreMute(listenAddress);
 
 			if(allMuted == true){
 				System.out.println("All my clients are muted");
 
 				//I may send the pending block ok
 				pendingBlockOk.go();
 
 			}
 		}
 
 		else if(this.flushMode == false){
 
 			//Let's see if all clients have said BlockOk
 			boolean allGroupMuted = VsClientManagement.checkIfAllInGroupAttachedToMeClientAreMute(groupId, listenAddress);
 
 			//If all clients in the group are muted
 			if(allGroupMuted){
 				System.out.println("Hurray every client in group: " + groupId +" is muted");
 
 				// We may send our leaveProxyEvent to the other servers	
 				BlockOkProxyEvent blockedOkProxy = new BlockOkProxyEvent(groupId, myEndpt, viewVersion);
 				sendToOtherServers(blockedOkProxy);
 
 				//And also send to myself
 				sendToMyself(blockedOkProxy);
 
 				return;
 			}
 
 			//In this case we still have to wait on other clients
 			else{
 				System.out.println("Still waiting on other clients");
 			}
 		}
 	}
 
 	/**
 	 * Handles the event received from a client that he wishes to join a group
 	 * 
 	 * @param event
 	 * @throws AppiaEventException
 	 */
 	private void handleGroupInitStubEvent(GroupInitStubEvent event) throws AppiaEventException {
 		event.loadMessage();
 
 		System.out.println("Received a new client connecting to me for group: " + event.getGroup().id);
 
 		//Let's get the arguments
 		Endpt clientEndpt = event.getEndpoint();
 
 
 		//Let's create the client
 		VsClient newClient = new VsClient(clientEndpt, 
 				event.getGroup(), (InetSocketAddress) event.source, listenAddress);
 
 		//Let's warn everybody that a client wishes to Join
 		NewClientProxyEvent newClientProxyEvent = new NewClientProxyEvent(newClient, clientEndpt);
 		sendToOtherServers(newClientProxyEvent);
 		sendToMyself(newClientProxyEvent);
 	}
 
 	/**
 	 * Receives from the clients a message to send to the other clients
 	 * 
 	 * @param stub
 	 * @throws AppiaEventException
 	 */
 	private void handleGroupSendableStubEvent(GroupSendableStubEvent stub) throws AppiaEventException {
 		stub.loadMessage();
 
 		//Get the contained event
 		GroupSendableEvent realEvent = stub.getEncapsulatedEvent();
 		String groupId = stub.getGroupId();
 
 		//Create a proxy for GroupSendableEvent
 		GroupSendableProxyEvent proxyEvent = new GroupSendableProxyEvent(groupId, realEvent);
 
 		//And we send to the other servers
 		sendToOtherServers(proxyEvent);
 		sendToMyself(proxyEvent);
 	}
 
 	/**
 	 * Receives the info that a client has left from a group
 	 * 
 	 * @param event
 	 * @throws AppiaEventException
 	 */
 	private void handleLeaveStubEvent(LeaveStubEvent event) throws AppiaEventException {
 		event.loadMessage();
 
 		//See if I can have now leave events
 		acquireClientsViewUpdateLock();
 
 		System.out.println("Removing client: " + event.getEndpoint() + " from group: " + event.getGroupId());
 
 		//Lets get the neccessary arguments
 		String groupId = event.getGroupId();
 		VsClient vsClient = VsClientManagement.getVsGroup(groupId).getVsClient(event.getEndpoint());
 
 		//Let's warn everybody that a client wishes to leave
 		LeaveClientProxyEvent leaveProxyEvent = new LeaveClientProxyEvent(vsClient, groupId);
 		sendToOtherServers(leaveProxyEvent);
 		sendToMyself(leaveProxyEvent);
 	}
 	
 	/**
 	 * Handles Pong events, by clearing the pong meter of the client
 	 * @param event
 	 */
 	private void handlePongEvent(PongEvent event) {		
 		event.loadMessage();
 		//System.out.println("Received a pong event from: " + event.getClientAddress());
 
 		VsClientManagement.resetPongTimer(event.getClientAddress(), this.listenAddress);
 	}
 	/************** END - Methods to handle stub events *************************/
 
 
 	/************** START - Methods to handle proxy events *************************/
 
 	/**
 	 * Receives the message sent from another server wich indicates that 
 	 * a client has left a view
 	 * 
 	 * @param event
 	 * @throws AppiaEventException
 	 */
 	private void handleLeaveClientProxyEvent(LeaveClientProxyEvent event) throws AppiaEventException {
 		event.loadMessage();
 		acquireClientsViewUpdateLock();
 
 		System.out.println("A server said me that clients:");
 		for(VsClient client : event.getFutureDeadClients()){
 			System.out.println(client.getClientAddress());
 		}
 		System.out.println("are leaving from group: " + event.getGroupId());
 
 		//Get the parameters
 		String groupId = event.getGroupId();
 		List<VsClient> futureDeadClients = event.getFutureDeadClients();
 
 		if(futureDeadClients.size() > 0){
 			//This deads will also be our deads
 			VsClientManagement.addFutureDead(futureDeadClients, groupId);
 
 			//Remove the client from our list	
 			VsClientManagement.removeClient(futureDeadClients, groupId);
 			VsClientManagement.printClients(groupId);
 		}
 
 		//I ask if I have some client in that group
 		if( VsClientManagement.hasSomeClientConnectedToServer(groupId, listenAddress)){
 			System.out.println("I have clients to ask to shut up");
 
 			//I need to ask my attached clients for them to Block	
 			VsGroup group = VsClientManagement.getVsGroup(groupId);
 			ShutUpStubEvent shutUpEvent = new ShutUpStubEvent(groupId);
 			sendStubEvent(shutUpEvent, group);
 		}
 
 		//If I don't have no clients in that group
 		else{
 			System.out.println("I have no clients for group: " + groupId + " so I can send the blockOk");
 
 			//Get my version for this group
 			int viewVersion = VsClientManagement.getVsGroup(groupId).getCurrentVersion();
 
 			//I may send directly the BlockOkProxy event to me and the other server
 			BlockOkProxyEvent blockedOkProxy = new BlockOkProxyEvent(groupId, myEndpt, viewVersion);
 			sendToOtherServers(blockedOkProxy);
 			sendToMyself(blockedOkProxy);
 		}
 	}
 
 	/**
 	 * Handles the event when a new view for a group has been decided by a leader
 	 * 
 	 * @param event
 	 * @throws AppiaEventException
 	 */
 	private void handleDecidedProxyEvent(DecidedProxyEvent event) throws AppiaEventException {
 		event.loadMessage();
 		System.out.println("I have received the information that someone has decided of the nextClienViews");
 
 		// Get the arguments
 		VsGroup decidedGroup = event.getDecidedVsGroup();
 		String decidedGroupId = decidedGroup.getGroupId();
 
 		//Update the present view
 		VsClientManagement.replaceAGroup(decidedGroup);
 
 		//Let's see what in our future's list can be removed
 		VsClientManagement.updateFutureDeadClientsBasedOnCurrentView(decidedGroupId);
 		VsClientManagement.updateFutureLiveClientsBasedOnCurrentView(decidedGroupId);
 
 		//Clean VSControl for the group
 		ControlManager.removeControl(decidedGroupId);
 
 		//Unmute all clients for the group
 		VsClientManagement.unmuteAllClients(decidedGroupId);
 
 		System.out.println(decidedGroup);
 
 		//Release the clientViewUpdateLock
 		releaseClientViewUpdate();
 
 		//Only now I may deliver the view to my clients
 		ViewStubEvent newView = new ViewStubEvent(decidedGroup);
 		sendStubEvent(newView, decidedGroup);
 	}
 
 	/**
 	 * Handles the event of another server saying that it is blocked
 	 * 
 	 * @param event
 	 * @throws AppiaEventException
 	 */
 	private void handleBlockOkProxyEvent(BlockOkProxyEvent event) throws AppiaEventException {
 		event.loadMessage();
 		System.out.println("Received a BlockOkProxyEvent from: " + event.getServerThatShutUpEndpt());
 
 		//Get the arguments
 		String groupId = event.getGroupId();
 		Endpt serverThatShupUp = event.getServerThatShutUpEndpt();
 		int remoteViewVersion = event.getViewVersion();
 
 		//Check if it is a blockOk from a view already decided
 		//This means the decide event has already come and the block oks were late to me
 		int localViewVersion = VsClientManagement.getVsGroup(groupId).getCurrentVersion();
 		if(remoteViewVersion < localViewVersion ){
 			return; //Discard this event
 		}
 
 		//Mark the server as already sent the block ok
 		ControlManager.addControlMessageReceived(groupId, serverThatShupUp);
 
 		//Ask if all the other servers are quiet
 		boolean allMuted = ControlManager.receivedFromAllLiveServers(groupId, vs.view);
 
 		//If all the servers present in the current view are blocked
 		if(allMuted){
 			System.out.println("All servers have responded to me that they are quiet");
 
 			//Check if I am the leader
 			if(amIleader()){
 				System.out.println("I am the leader so I will decide the view on group:" + groupId);
 				VsGroup group = VsClientManagement.getVsGroup(groupId);
 
 				//Lets increment the version
 				group.incrementGroupViewVersion();
 
 				//And some clients (if they exist)
 				group.insertFutureClientsIntoPresent();
 
 				//Send the Decide message to the other servers
 				DecidedProxyEvent decidedEvent = new DecidedProxyEvent(group);
 				sendToOtherServers(decidedEvent);
 
 				//Sent to me also
 				sendToMyself(decidedEvent);
 			}
 		}
 
 		else{
 			System.out.println("There are still server to whom I didn't receive no blockOK");
 		}
 	}
 
 
 	/**
 	 * 	Handles the encapsulated GroupSendable events that we will send to the
 	 * other servers so they distribute the event to their clients
 	 * 
 	 * @param groupProxyEvent
 	 * @throws AppiaEventException
 	 */
 	private void handleGroupSendableProxyEvent(GroupSendableProxyEvent groupProxyEvent) throws AppiaEventException {
 		groupProxyEvent.loadMessage();
 
 		//Get the arguments
 		String groupId = groupProxyEvent.getGroupId();
 		GroupSendableEvent sendableEvent = groupProxyEvent.getSendableEvent();
 
 		//Create the stub event to send to the users
 		GroupSendableStubEvent stubEvent = new GroupSendableStubEvent(groupId, sendableEvent);
 
 		//Send to my clients
 		VsGroup group = VsClientManagement.getVsGroup(groupProxyEvent.getGroupId());
 		sendStubEvent(stubEvent, group);
 	}
 
 	/**
 	 * Handles the event that we receive from another servers saying 
 	 * that a client has joined a group
 	 * 
 	 * @param event
 	 * @throws AppiaEventException
 	 */
 	private void handleNewClientProxyEvent(NewClientProxyEvent event) throws AppiaEventException {
 		event.loadMessage();
 		System.out.println("Received a new client from a server");
 
 		//Get the parameters
 		String groupId = event.getClient().getGroup().id;
 
 		//Add the client as a future client
 		VsClientManagement.addAsFutureClient(groupId, event.getClient());
 
 		//I ask if I have some client in that group
 		if( VsClientManagement.hasSomeClientConnectedToServer(groupId, listenAddress)){
 			System.out.println("I have clients to ask to shut up");
 
 			//I need to ask my attached clients for them to Block	
 			VsGroup group = VsClientManagement.getVsGroup(groupId);
 			ShutUpStubEvent shutUpEvent = new ShutUpStubEvent(groupId);
 			sendStubEvent(shutUpEvent, group);
 		}
 
 		//If I don't have no clients in that group
 		else{
 			System.out.println("I have no clients for group: " + groupId + " so I can send the blockOk");
 
 			//Get my version for this group
 			int viewVersion = VsClientManagement.getVsGroup(groupId).getCurrentVersion();
 
 			//I may send directly the BlockOkProxy event to me and the other server
 			BlockOkProxyEvent blockedOkProxy = new BlockOkProxyEvent(groupId, myEndpt, viewVersion);
 			sendToOtherServers(blockedOkProxy);
 			sendToMyself(blockedOkProxy);
 		}
 	}
 	/************** END - Methods to handle proxy events *************************/
 
 
 	/************** START - Methods to handle basic events *************************/
 
 	/**
 	 * Handles the event of receive a block OK 
 	 * 
 	 * @param ok
 	 * @throws AppiaEventException
 	 */
 	private void handleBlockOk(BlockOk ok) throws AppiaEventException {
 		//Get the necessary arguments
 		List<VsClient> allMyClients = VsClientManagement.getClientstAttachedTo(listenAddress);
 
 		//Enter flush mode
 		flushMode = true;
 		System.out.println("Received a BlockOk ( a normal one )");
 
 		//Lock clientsViewManagement here with a lock
 		acquireClientsViewUpdateLock(); //So no new clients/servers can join the group
 
 		//Clean the update controls I have received
 		UpdateManager.cleanUpdateViews();
 
 		//Send to all client in all groups a blockOk request
 		//I ask if I have some client in that group
 
 		//If I have at least one client to shut
 		if(allMyClients.size() >= 1){
 			//I will store the block ok event
 			pendingBlockOk = ok;
 			System.out.println("I have a pending blockOk to send. Wait for client to shut upo");
 
 			//And for each client, i'll ask them to shut up
 			for (VsClient client : allMyClients){
 				VsGroup group = VsClientManagement.getVsGroup(client.getGroup().id);
 				ShutUpStubEvent shutUpEvent = new ShutUpStubEvent(client.getGroup().id);
 				sendStubEvent(shutUpEvent, group);
 			}
 		}
 
 		//If I have no clients to warn
 		else{
 			//I may send right way the block OK
 			ok.go();
 			System.out.println("I have no clients, sending blockOk");
 		}
 	}
 
 	/**
 	 * Handles a Register Socket event. This could be to the channel to receive 
 	 * messages from clients or to the channel to speak with the other servers
 	 * 
 	 * @param event
 	 * @throws AppiaException
 	 */
 	private void handleRegisterSocketEvent(RegisterSocketEvent event) throws AppiaException {
 		if(event.getDir() == Direction.DOWN){
 			System.err.println("VsProxySession - No one should be above " +
 			"me sending a RSE Event");
 		}
 
 		//Create the matchers to find out what is the channel (For group or clients)
 		Matcher listenMatcher = textPattern.matcher(event.getChannel().getChannelID());
 		Matcher vsMatcher = drawPattern.matcher(event.getChannel().getChannelID());
 
 		if(listenMatcher.matches()){
 			listenAddress = (InetSocketAddress) event.getLocalSocketAddress();
 			System.out.println("listenAddress Registered at: " + listenAddress);
 
 			//We may now launch our pong manager
 			launchPongManagerThread();
 		}
 
 		else if(vsMatcher.matches()){
 			vsAddress = (InetSocketAddress) event.getLocalSocketAddress();
 			System.out.println("vsAddress Registered at: " + vsAddress);
 			//Create a Virtual Sinchrony channel for the servers
 			sendGroupInit();	
 		}
 
 		event.go();
 	}
 
 	/**
 	 * Handles the Channel Init Event. It creates two channels, one to receive messages
 	 * from clients and the other one two communicate with the other servers
 	 * 
 	 * @param init
 	 * @throws AppiaEventException
 	 */
 	private void handleChannelInit(ChannelInit init) throws AppiaEventException {
 
 		Matcher listenMatcher = textPattern.matcher(init.getChannel().getChannelID());
 		Matcher vsMatcher = drawPattern.matcher(init.getChannel().getChannelID());
 
 		System.out.println("Registering at : " + init.getChannel().getChannelID()
 				+ " port: " + localport);
 
 		//Create a socket for clients to connect to
 		if(listenMatcher.matches()){
 			listenChannel = init.getChannel();
 			new RegisterSocketEvent(listenChannel,Direction.DOWN,this,localport).go();
 		}
 
 		//Create a socket for the group communication occur
 		else if (vsMatcher.matches()){
 			vsChannel = init.getChannel();
 			new RegisterSocketEvent(vsChannel,Direction.DOWN,this,localport + 1).go();	
 		}
 
 		//Let the event go
 		init.go();
 	}
 
 	/**
 	 * We receive a new View when the server group has suffered some change		
 	 * @param view
 	 * @throws AppiaEventException 
 	 */
 	private void handleView(View view) throws AppiaEventException {
 		System.out.println("Received a new View");
 
 		//Get the parameters
 		VsGroup[] allGroups = VsClientManagement.getAllGroups();
 
 		//Update the view
 		vs = view.vs;		
 
 		//We start from scratch
 		UpdateManager.cleanUpdateViews();
 
 		//We seize the oportunity to put the future clients and future dead clients in the view
 		for(VsGroup group : allGroups){
			VsClientManagement.setFutureClientsIntoPresent(group.getGroupId());
			VsClientManagement.setFutureDeadsIntoPresent(group.getGroupId());

 			VsClientManagement.clearFutureClients(group.getGroupId());
 			VsClientManagement.clearFutureDead(group.getGroupId());
 		}
 
 		//I have received a new view, must send the other servers my views clients (and me)
 		VsGroup[] allGroupsWithOnlyMyClients = VsClientManagement.getAllGroupsWithOnlyMyClients(listenAddress);
 
 		UpdateProxyEvent updateProxy = new UpdateProxyEvent(allGroupsWithOnlyMyClients, myEndpt);
 		sendToOtherServers(updateProxy);
 		sendToMyself(updateProxy);		
 	}
 
 	private void handleUpdateProxyEvent(UpdateProxyEvent event) throws AppiaEventException {
 //		System.out.println("Received an update proxy event.");
 
 		event.loadMessage();
 
 		//Get the parameters
 		Endpt servertThatSentEndpt = event.getServerThatSentEndpt();
 		VsGroup[] passedGroups = event.getAllGroups();
 
 		//Say that the view was received (this also merges the temporary views)
 		UpdateManager.addUpdateMessageReceived(servertThatSentEndpt, passedGroups);
 
 		//If i have received from all live servers
 		if(UpdateManager.receivedUpdatesFromAllLiveServers(vs.view) && amIleader()){
 			System.out.println("Received an update proxy event from all alive and I am leader");
 
 			//Let go get our temporary view
 			VsGroup[] newGroupList= UpdateManager.getTemporaryUpdateList();
 
 			//Send the nre decided view to all
 			UpdateDecideProxyEvent updateDecideProxy = new UpdateDecideProxyEvent(newGroupList);
 			sendToOtherServers(updateDecideProxy);
 			sendToMyself(updateDecideProxy);		
 		}
 	}
 
 
 	private void handleUpdateDecideProxyEvent(UpdateDecideProxyEvent event) throws AppiaEventException {
 		event.loadMessage();
 
 		System.out.println("Received an update decide proxy event with groups: " + event.getAllGroups().length);
 
 		//Get the parameters
 		VsGroup[] allNewGroups = event.getAllGroups();
 
 		//Update the view
 		VsClientManagement.setNewAllGroups(allNewGroups);
 
 		//Clean the update views
 		UpdateManager.cleanUpdateViews();
 		VsGroup[] myGroupView = VsClientManagement.getAllGroups();
 
 		//And we may leave the flushMode
 		flushMode = false;
 
 		//Set this as the new group	
 		for (VsGroup group : myGroupView){
 			ViewStubEvent newView = new ViewStubEvent(group);
 			sendStubEvent(newView, group);
 		}
 
 		//Remove the global lock (I cannot accept new clients in this state!!!)
 		releaseClientViewUpdate();
 	}	
 
 	/************** END OF - Methods to handle basic events ***********************/
 
 
 	/************************** ******************* *******************************/
 	/************************** AUXILIARY FUNCTIONS *******************************/
 
 
 	/**
 	 * Sends the groupInit event to create the communication group for servers 
 	 * exchange events.
 	 */
 	private void sendGroupInit() {
 		try {
 			myEndpt=new Endpt(serverName+"@"+vsAddress.toString());
 
 			Endpt[] view=null;
 			InetSocketAddress[] addrs=null;
 
 			addrs=new InetSocketAddress[1];
 			addrs[0]=vsAddress;
 			view=new Endpt[1];
 			view[0]=myEndpt;
 
 			Group myGroup = new Group("DEFAULT_SERVERS");
 			vs = new ViewState("1", myGroup, new ViewID(0,view[0]), new ViewID[0], view, addrs);
 
 			GroupInit gi =
 				new GroupInit(vs,myEndpt,null,gossipServers,vsChannel,Direction.DOWN,this);
 			gi.go();
 		} catch (AppiaEventException ex) {
 			System.err.println("EventException while launching GroupInit");
 			ex.printStackTrace();
 		} catch (NullPointerException ex) {
 			System.err.println("EventException while launching GroupInit");
 			ex.printStackTrace();
 		} catch (AppiaGroupException ex) {
 			System.err.println("EventException while launching GroupInit");
 			ex.printStackTrace();
 		} 
 	}
 
 	/**
 	 * Sends a stub event to all clients present in the passed vsGroup.
 	 *  
 	 * @param stubEvent
 	 * @param vsGroup
 	 * @throws AppiaEventException
 	 */
 	private void sendStubEvent(StubEvent stubEvent, VsGroup vsGroup) throws AppiaEventException {
 		List<VsClient> vsClients = vsGroup.getClientsInGroup();
 
 		//For each one of the clients in the group, send the stub message 
 		for (VsClient client : vsClients){
 			//	But	only send to the clients attached to me
 			if(client.attachedTo(listenAddress)){
 				try {
 					StubEvent clone = (StubEvent) stubEvent.cloneEvent();
 					sendStubEvent(clone, client.getClientAddress());
 					System.out.println("Sending to client:" +client.getClientAddress() + " type: " + clone.getClass());
 				} catch (CloneNotSupportedException e) {
 					e.printStackTrace();
 					System.exit(1);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Sends a proxy event to the other servers
 	 * 
 	 * @param proxyEvent
 	 * @throws AppiaEventException
 	 */
 	private void sendToOtherServers(ProxyEvent proxyEvent) throws AppiaEventException {
 //		System.out.println("Sending to other servers: "+ proxyEvent);
 		proxyEvent.storeMessage();
 		proxyEvent.setDir(Direction.DOWN);
 		proxyEvent.setChannel(vsChannel);
 		proxyEvent.setSourceSession(this);
 		proxyEvent.init();
 		proxyEvent.go();
 
 	}
 
 	/**
 	 * Sends a stub event to a single client
 	 * 
 	 * @param stubEvent
 	 * @param destination
 	 * @throws AppiaEventException
 	 */
 	private void sendStubEvent(StubEvent stubEvent, InetSocketAddress destination) throws AppiaEventException {
 		stubEvent.storeMessage();
 		stubEvent.setDir(Direction.DOWN);
 		stubEvent.setChannel(listenChannel);
 		stubEvent.setSourceSession(this);
 		stubEvent.dest = destination;
 		stubEvent.init();
 		stubEvent.go();
 	}
 
 	/**
 	 * Sends a proxy event to myself
 	 * @param event
 	 * @throws AppiaEventException
 	 */
 	private void sendToMyself(ProxyEvent event) throws AppiaEventException {
 		try {
 			ProxyEvent clone = (ProxyEvent) event.cloneEvent();
 			clone.storeMessage();
 			clone.setDir(Direction.DOWN);
 			clone.setChannel(vsChannel);
 			clone.setSourceSession(this);
 			clone.init();
 
 			EchoEvent echo = new EchoEvent(clone, this.vsChannel, Direction.UP, this);
 			echo.init();
 			echo.go();
 		} catch (CloneNotSupportedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Launches the Thread responsible for checking if all clients
 	 * are sending pongs. If they are not, the thread declares them
 	 * dead
 	 */
 	private void launchPongManagerThread() {
 		PongManager pongManager = new PongManager(this, listenAddress);
 		final Thread t = this.listenChannel.getThreadFactory().newThread(pongManager);
 		t.setName("Pong Server Manager");
 		t.start();
 	}
 
 	/**
 	 * Considers the client dead, and start the leave procedure for him leaving the view
 	 * @param client
 	 */
 	protected void considerClientDead(VsClient client) {
 		System.out.println("Client: " + client.getEndpoint() + " was considered dead by " +
 		"not responding to pongs");		
 
 		//Send some event to consider the client dead
 		LeaveStubEvent leaveClient = new LeaveStubEvent(client.getGroup().id, client.getEndpoint());
 		leaveClient.storeMessage();
 
 		try {
 			leaveClient.asyncGo(this.vsChannel, Direction.DOWN);
 		} catch (AppiaEventException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Checks if this server is the leader of the view
 	 * 
 	 * @return True if this server is the leader of the View. Falste Otherwise
 	 */
 	private boolean amIleader() {
 		return vs.getRank(myEndpt) == 0;
 	}
 
 	/**
 	 * Acquires a lock
 	 */
 	private void acquireClientsViewUpdateLock() {
 		try {
 			available.acquire();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 
 	/**
 	 * Releases a lock
 	 */
 	private void releaseClientViewUpdate() {
 		available.release();
 	}
 }
