 package tjenkinson.asteriskLiveComs.program;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Set;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.asteriskjava.live.AsteriskChannel;
 import org.asteriskjava.live.AsteriskQueueEntry;
 import org.asteriskjava.live.AsteriskServer;
 import org.asteriskjava.live.AsteriskServerListener;
 import org.asteriskjava.live.DefaultAsteriskServer;
 import org.asteriskjava.live.Extension;
 import org.asteriskjava.live.ManagerCommunicationException;
 import org.asteriskjava.live.MeetMeUser;
 import org.asteriskjava.live.internal.AsteriskAgentImpl;
 import org.asteriskjava.manager.ManagerConnection;
 import org.asteriskjava.manager.ManagerConnectionFactory;
 import org.asteriskjava.manager.ManagerEventListener;
 import org.asteriskjava.manager.event.HangupEvent;
 import org.asteriskjava.manager.event.ManagerEvent;
 import org.asteriskjava.manager.event.NewExtenEvent;
 
 import tjenkinson.asteriskLiveComs.program.events.ChannelAddedEvent;
 import tjenkinson.asteriskLiveComs.program.events.ChannelRemovedEvent;
 import tjenkinson.asteriskLiveComs.program.events.ChannelToHoldingEvent;
 import tjenkinson.asteriskLiveComs.program.events.ChannelVerifiedEvent;
 import tjenkinson.asteriskLiveComs.program.events.ChannelsToRoomEvent;
 import tjenkinson.asteriskLiveComs.program.events.EventListener;
 import tjenkinson.asteriskLiveComs.program.events.LiveComsEvent;
 import tjenkinson.asteriskLiveComs.program.events.ServerResettingEvent;
 import tjenkinson.asteriskLiveComs.program.exceptions.ChannelNotVerifiedException;
 import tjenkinson.asteriskLiveComs.program.exceptions.InvalidChannelException;
 import tjenkinson.asteriskLiveComs.program.exceptions.MissingKeyException;
 import tjenkinson.asteriskLiveComs.program.exceptions.NoAsteriskConnectionException;
 import tjenkinson.asteriskLiveComs.program.exceptions.OnlyOneChannel;
 
 public class Program {
 
 	private String asteriskServerIP;
 	private int asteriskServerPort;
 	private String asteriskServerUser;
 	private String asteriskServerSecret;
 	
 	private ManagerConnection managerConnection;
 	private AsteriskServer asteriskServer;
 	private Hashtable<Integer,MyAsteriskChannel> registeredChannels = null;
 	private Hashtable<Integer,MyAsteriskChannel> activatedChannels = null;
 	private int lastChannelId = 0;
 	private Hashtable<Integer, Room> rooms;
 	private ArrayList<EventListener> listeners = new ArrayList<EventListener>();
 	private final ExecutorService eventsDispatcherExecutor;
 	private Object asteriskConnectionLock = new Object();
 	private Object connectionChangeStateLock = new Object();
 	private Object resetLock = new Object();
 	private Object channelActivationLock = new Object();
 	private HandleAsteriskServerEvents asteriskServerEventsHandler;
 	private HandleManagerEvents managerEventsHandler;
 	private boolean hasLoaded = false;
 	private boolean connectedToAsterisk = false;
 	
 	public Program(String ip, int port, String user, String password) throws NoAsteriskConnectionException
 	{
 		this.asteriskServerIP = ip;
 		this.asteriskServerPort = port;
 		this.asteriskServerUser = user;
 		this.asteriskServerSecret = password;
 		
 		asteriskServerEventsHandler = new HandleAsteriskServerEvents();
 		managerEventsHandler = new HandleManagerEvents();
 		
 		eventsDispatcherExecutor = Executors.newSingleThreadExecutor();
 		ManagerConnectionFactory factory = new ManagerConnectionFactory(asteriskServerIP, asteriskServerPort, asteriskServerUser, asteriskServerSecret);
 		log("Starting asterisk manager connection.");
 		managerConnection = factory.createManagerConnection();
 		log("Started asterisk manager connection.");
 		asteriskServer = new DefaultAsteriskServer(managerConnection);
 		log("Checking connectivity.");
 		try {
 			asteriskServer.getVersion();
 		}
 		catch (ManagerCommunicationException e) {
 			log("Cannot connect to asterisk server.");
 			throw (new NoAsteriskConnectionException());
 		}
 		connectedToAsterisk = true;
 		synchronized(resetLock) {
 			reset();
 			hasLoaded = true;
 		}
 		log("All systems running!");
 	}
 	
 	public void log(String msg) {
 		System.out.println(msg);
 	}
 	
 	public void reset()
 	{
 		synchronized(resetLock) {
 			log("Initialising.");
 			if (hasLoaded) {
 				dispatchEvent(new ServerResettingEvent());
 			}
 			synchronized(asteriskConnectionLock) {
 				asteriskServer.removeAsteriskServerListener(asteriskServerEventsHandler);
 				managerConnection.removeEventListener(managerEventsHandler);
 			}
 			
 			if (activatedChannels != null){
 				log("Broadcasting channel removed event for any channels currently registered before clearing.");
 				synchronized(activatedChannels) {
 					Set<Integer> keys = activatedChannels.keySet();
 					for(Integer id : keys) {
 						dispatchEvent(new ChannelRemovedEvent(activatedChannels.get(id).getId()));
 					}
 				}
 			}
 			
 			if (registeredChannels == null) {
 				registeredChannels = new Hashtable<Integer,MyAsteriskChannel>();
 			}
 			else {
 				synchronized (registeredChannels) {
 					registeredChannels = new Hashtable<Integer,MyAsteriskChannel>();
 				}
 			}
 			
 			if (activatedChannels == null) {
 				activatedChannels = new Hashtable<Integer,MyAsteriskChannel>();
 			}
 			else {
 				synchronized (activatedChannels) {
 					activatedChannels = new Hashtable<Integer,MyAsteriskChannel>();
 				}
 			}
 			if (rooms == null) {
 				rooms = new Hashtable<Integer, Room>();
 			}
 			else {
 				synchronized (rooms) {
 					rooms = new Hashtable<Integer, Room>();
 				}
 			}
 			
 			try {
 				for (AsteriskChannel asteriskChannel : asteriskServer.getChannels())
 		        {
 					log("Found channel: \""+asteriskChannel+"\".");
 					registerChannel(asteriskChannel, true);
 		        }
 			}
 			catch (ManagerCommunicationException e) {
 				// if it can't connect this will be called again immediately when it does. the library keeps retrying
 			}
 			synchronized(asteriskConnectionLock) {
 				try {
 					asteriskServer.addAsteriskServerListener(asteriskServerEventsHandler);
 				}
 				catch (ManagerCommunicationException e) {
 					// if it can't connect this will be called again immediately when it does. the library keeps retrying
 				}
 				managerConnection.addEventListener(managerEventsHandler);
 			}
 		}
 	}
 	
 	// register a channel that has just been received by asterisk
 	private void registerChannel(AsteriskChannel asteriskChannel) {
 		registerChannel(asteriskChannel, false);
 	}
 	private void registerChannel(AsteriskChannel asteriskChannel, boolean alsoActivate) {
 		MyAsteriskChannel chan = null;
 		
 		if (asteriskChannel.getName().matches("^.*?/pseudo.*$")) {
 			log("Not registering channel \""+asteriskChannel+"\" because it has a \"pseudo\" after a /.");
 			return;
 		}
 		
 		synchronized(registeredChannels) {
 			int id = ++lastChannelId;
 			log("Registering channel \""+asteriskChannel+"\" with id "+id+".");
 			chan = new MyAsteriskChannel(asteriskChannel, id);
 			registeredChannels.put(id, chan);
 			if (alsoActivate) {
 				// if the current context is start then it will be added when transferred to GrabChannel so leave it.
				if (!chan.getChannel().getCurrentExtension().getContext().equals("Start") && !chan.getChannel().getCurrentExtension().getContext().equals("FnDenyAccess")) {
 					activateChannel(chan.getChannel().getName());
 				}
 			}
 		}
 	}
 	
 	// activate a channel so that it can be used. (not related to grantAccess)
 	private void activateChannel(String chanName) {
 		synchronized(activatedChannels) {
 			synchronized(registeredChannels) {
 				Set<Integer> keys = registeredChannels.keySet();
 				MyAsteriskChannel channel = null;
 				for(Integer id : keys) {
 					if (registeredChannels.get(id).getChannel().getName().equals(chanName)) {
 						channel = registeredChannels.get(id);
 						break;
 					}
 				}
 				if (channel != null) {
 					log("Activating channel with id "+channel.getId()+".");
 					registeredChannels.remove(channel.getId());
 					activatedChannels.put(channel.getId(), channel);
 					sendToDPFn(channel, "WaitVerification", 1);
 					dispatchEvent(new ChannelAddedEvent(channel));
 				}
 			}
 		}
 	}
 	
 	private void sendToDPFn(MyAsteriskChannel chan, String fn, int priority) {
 		synchronized(activatedChannels) {
 			log("Sending channel with id "+chan.getId()+" to DPFn \""+fn+"\" at priority "+priority+".");
 			boolean success = false;
 			
 			// TODO: figure out what is actually going wrong here
 			// try this a maximum of 3 times before giving up
 			// if this is called to quickly after the channel is activated this is thrown for some reason.
 			// presuming it's a bug in the library as if you try the same again after a delay it works
 			// the library must already have the channel as it wouldn't end up here with the channel otherwise!
 			for(int i=0; i<3 && !success; i++) {
 				try {
 					chan.getChannel().redirect("Fn"+fn, "start", priority);
 					success = true;
 				}
 				catch(org.asteriskjava.live.NoSuchChannelException e) {
 					try {
 						log("Sending channel failed. Trying again.");
 						Thread.sleep(1000);
 					} catch (InterruptedException e1) {
 						e1.printStackTrace();
 					}
 				}
 			}
 		}
 		checkRoomCounts();
 	}
 	
 	public ArrayList<Hashtable<String,Object>> getChannels() {
 		log("Getting channels.");
 		ArrayList<Hashtable<String,Object>> data = new ArrayList<Hashtable<String,Object>>();
 		synchronized(activatedChannels) {
 			Set<Integer> keys = activatedChannels.keySet();
 			for(Integer id : keys) {
 				MyAsteriskChannel channel = activatedChannels.get(id);
 				data.add(channel.getInfo());
 			}
 		}
 		return data;
 	}
 	
 	public boolean grantAccess(int id, boolean enableHoldingMusic) throws InvalidChannelException {
 		synchronized(activatedChannels) {
 			MyAsteriskChannel channel = getChannelFromId(id);
 			if (channel.getVerified()) {
 				return false;
 			}
 			else {
 				log("Verifying channel with id "+id+".");
 				channel.setVerified();
 				channel.setPlayHoldMusic(enableHoldingMusic);
 				channel.getChannel().setVariable("EnableHoldingMusic", enableHoldingMusic ? "1":"0");
 				sendToDPFn(channel, "GrantAccess", 1);
 				dispatchEvent(new ChannelVerifiedEvent(channel.getId()));
 				return true;
 			}
 		}
 	}
 	
 	public void denyAccess(int id) throws InvalidChannelException {
 		synchronized(activatedChannels) {
 			MyAsteriskChannel channel = getChannelFromId(id);
 			log("Denying access for channel with id "+id+".");
 			sendToDPFn(channel, "DenyAccess", 1);
 		}
 	}
 	
 	private MyAsteriskChannel getChannelFromId(int id) throws InvalidChannelException {
 		synchronized(activatedChannels) {
 			MyAsteriskChannel channel = activatedChannels.get(id);
 			if (channel == null) {
 				throw(new InvalidChannelException());
 			}
 			return channel;
 		}
 	}
 	
 	public void createRoom(ArrayList<Hashtable<String, Object>> data) throws InvalidChannelException, ChannelNotVerifiedException, MissingKeyException, OnlyOneChannel {
 		log("Connecting channels.");
 		
 		ArrayList<MyAsteriskChannel> channels = new ArrayList<MyAsteriskChannel>();
 		
 		if (data.size() == 1) {
 			throw(new OnlyOneChannel());
 		}
 		
 		int roomNo = -1;
 		ArrayList<Integer> ids = new ArrayList<Integer>();
 		synchronized(channels) {
 			for(int i=0; i<data.size(); i++) {
 				if (data.get(i).get("id") == null || data.get(i).get("listenOnly") == null) {
 					throw(new MissingKeyException());
 				}
 				
 				if (!getChannelFromId((int)data.get(i).get("id")).getVerified()) {
 					throw (new ChannelNotVerifiedException());
 				}
 				channels.add(getChannelFromId((int)data.get(i).get("id")));
 			}
 			
 			// remove channels from any previous rooms
 			for(int i=0; i<channels.size(); i++) {
 				removeChannelFromRoom(channels.get(i), true); // true means do not check room counts. will do this later so that channels can seamlessly move rooms if they're already in one
 			}
 			
 			roomNo = 1; // rooms start at 1
 			Room room = null;
 			synchronized(rooms) {
 				while(rooms.containsKey(roomNo)) {
 					roomNo++;
 				}
 				room = new Room(roomNo, channels);
 				rooms.put(roomNo, room);
 			}
 			// this initialises the room in the library (even though don't use it for rooms)
 			asteriskServer.getMeetMeRoom(String.valueOf(roomNo));
 			
 			// set channel var for each channel so they know what room to join and then enter room
 			for(int i=0; i<channels.size(); i++) {
 				ids.add(channels.get(i).getId());
 				AsteriskChannel channel = channels.get(i).getChannel();
 				channel.setVariable("RoomToJoin", String.valueOf(roomNo));
 				channel.setVariable("RoomListenInParam", ((boolean)data.get(i).get("listenOnly"))?"m":"");
 				Extension extension = channel.getCurrentExtension();
 				if (extension != null && extension.getContext().equals("FnToMeeting")) {
 					// do nothing because hearing into message and will then enter correct room as just set variable
 				}
 				else {
 					String fnName = "StartMeeting";
 		            if (extension != null && !extension.getContext().equals("FnStartMeeting")) {
 		            	fnName = "ToMeeting";
 		            }
 		            sendToDPFn(channels.get(i), fnName, 1);
 				}
 			}
 		}
 		checkRoomCounts();
 		log("Channels sent to room "+roomNo+".");
 		dispatchEvent(new ChannelsToRoomEvent(ids));
 	}
 	
 	public void sendChannelsToHolding(ArrayList<Integer> chanIds) throws InvalidChannelException, ChannelNotVerifiedException {
 		log("Sending channels to holding.");
 		ArrayList<MyAsteriskChannel> sentChannels = new ArrayList<MyAsteriskChannel>();
 		synchronized(activatedChannels) {
 			for (int i=0; i<chanIds.size(); i++) {
 				if (!getChannelFromId(chanIds.get(i)).getVerified()) {
 					throw (new ChannelNotVerifiedException());
 				}
 				sentChannels.add(getChannelFromId(chanIds.get(i)));
 			}
 			for (int i=0; i<sentChannels.size(); i++) {
 				removeChannelFromRoom(sentChannels.get(i));
 				sendToHolding(sentChannels.get(i));
 			}
 		}
 	}
 	
 	// checks room counts and if any are 1 person then put them in holding. also remove any fully empty rooms
 	private void checkRoomCounts() {
 		log("Checking rooms contain more than 1 person.");
 		synchronized(rooms) {
 			ArrayList<Room> roomsToEmpty = new ArrayList<Room>();
 			Set<Integer> keys = rooms.keySet();
 			for(Integer roomNo : keys) {
 				Room room = rooms.get(roomNo);
 				if (room.getCount() <= 1) {
 					roomsToEmpty.add(room);
 				}
 			}
 			
 			for (int i=0; i<roomsToEmpty.size(); i++) {
 				log("Emptying room no "+roomsToEmpty.get(i).getNo()+" because it only contains one member.");
 				emptyRoom(roomsToEmpty.get(i));
 			}
 		}
 	}
 	
 	private void emptyRoom(Room room) {
 		log("Emptying room no "+room.getNo()+".");
 		synchronized(rooms) {
 			synchronized(activatedChannels) {
 				ArrayList<MyAsteriskChannel> roomChannels = room.getChannels();
 				for(int i=0; i<roomChannels.size(); i++) {
 					MyAsteriskChannel channel = roomChannels.get(i);
 					room.removeChannel(channel);
 					sendToHolding(channel);
 				}
 			}
 			rooms.remove(room.getNo());
 		}
 	}
 	
 	private void sendToHolding(MyAsteriskChannel chan) {
 		Extension extension = chan.getChannel().getCurrentExtension();
 		if (extension == null || !extension.getContext().equals("FnHolding")) {
 			sendToDPFn(chan, "ToHolding", 1);
 			dispatchEvent(new ChannelToHoldingEvent(chan.getId()));
 		}
 	}
 	
 	// removes a channel from any rooms they are in.
 	private void removeChannelFromRoom(MyAsteriskChannel channel)
 	{
 		removeChannelFromRoom(channel, false);
 	}
 	private void removeChannelFromRoom(MyAsteriskChannel channel, boolean doNotCheckCounts)
 	{
 		synchronized(rooms) {
 			Set<Integer> keys = rooms.keySet();
 			Room room = null;
 			for(Integer roomNo : keys) {
 				room = rooms.get(roomNo);
 				synchronized(activatedChannels) {
 					if (room.containsChannel(channel)) {
 						break;
 					}
 				}
 			}
 			if (room != null) {
 				log("Removing channel with id "+channel.getId()+" from room.");
 				room.removeChannel(channel);
 				if (!doNotCheckCounts) {
 					checkRoomCounts();
 				}
 			}
 		}
 	}
 	
 	public boolean isConnectedToAsterisk() {
 		return connectedToAsterisk;
 	}
 	
 	public void addEventListener(EventListener a) {
 		synchronized(listeners) {
 			listeners.add(a);
 		}
 	}
 	
 	public void removeEventListener(EventListener a) {
 		synchronized(listeners) {
 			listeners.remove(a);
 		}
 	}
 	
 	private void dispatchEvent(final LiveComsEvent e) {
 		synchronized (listeners) {
 			synchronized (eventsDispatcherExecutor) {
 				eventsDispatcherExecutor.execute(new Runnable()
 				{
 					public void run()
 					{
 						for(int i=0; i<listeners.size(); i++) {
 							listeners.get(i).onEvent(e);
 						}
 					}
 				});
 			}
 		}
 	}
 	
 	private class HandleHangup implements Runnable {
 
 		private HangupEvent e;
 		public HandleHangup(HangupEvent e) {
 			this.e = e;
 		}
 		@Override
 		public void run() {
 			synchronized(registeredChannels) {
 				synchronized(activatedChannels) {
 
 					String chanName = e.getChannel();
 					MyAsteriskChannel channel = null;
 					Set<Integer> keys = activatedChannels.keySet();
 					for(Integer id : keys) {
 						if (activatedChannels.get(id).getChannel().getName().equals(chanName)) {
 							channel = activatedChannels.get(id);
 							break;
 						}
 					}
 					if (channel != null) {
 						removeChannelFromRoom(channel);
 						int channelId = channel.getId();
 						activatedChannels.remove(channelId);
 						dispatchEvent(new ChannelRemovedEvent(channelId));
 						log("Removed channel with id "+channel.getId()+".");
 					}
 					
 					channel = null;
 					keys = registeredChannels.keySet();
 					for(Integer id : keys) {
 						if (registeredChannels.get(id).getChannel().getName().equals(chanName)) {
 							channel = registeredChannels.get(id);
 							break;
 						}
 					}
 					if (channel != null) {
 						int channelId = channel.getId();
 						registeredChannels.remove(channelId);
 						log("Removed unactivated channel with id "+channel.getId()+".");
 					}
 				}
 			}
 		}
 		
 	}
 	
 	private class HandleConnectionEvent implements Runnable {
 
 		private ManagerEvent e;
 		public HandleConnectionEvent(ManagerEvent e) {
 			this.e = e;
 		}
 		@Override
 		public void run() {
 			synchronized(connectionChangeStateLock) {
 				if (e.getClass().getSimpleName().equals("ConnectEvent") || e.getClass().getSimpleName().equals("FullyBootedEvent")) {
 					connectedToAsterisk = true;
 				}
 				else if (e.getClass().getSimpleName().equals("DisconnectEvent")) {
 					connectedToAsterisk = false;
 				}
 			}
 			log("Connection to server has been lost or reconnected so resetting.");
 			reset();
 		}
 		
 	}
 	
 	private class HandleChannelActivation implements Runnable {
 
 		private NewExtenEvent e;
 		public HandleChannelActivation(NewExtenEvent e) {
 			this.e = e;
 		}
 		@Override
 		public void run() {
 			synchronized(channelActivationLock) {
 				activateChannel(e.getChannel());
 			}
 		}
 		
 	}
 	
 	private class HandleManagerEvents implements ManagerEventListener {
 		@Override
 		public void onManagerEvent(ManagerEvent e) {
 			String eName = e.getClass().getSimpleName().toString();
 			if (eName.equals("NewExtenEvent")) {
 				if (((NewExtenEvent)e).getContext().equals("GrabChannel")) {
 					new Thread(new HandleChannelActivation((NewExtenEvent)e)).start();
 				}
 			}
 			else if (eName.equals("HangupEvent")) {
 				new Thread(new HandleHangup((HangupEvent)e)).start();
 			}
 			else if (eName.equals("FullyBootedEvent") || eName.equals("ConnectEvent") || eName.equals("DisconnectEvent")) {
 				new Thread(new HandleConnectionEvent(e)).start();
 			}
 		}
 	}
 	
 	private class HandleAsteriskServerEvents implements AsteriskServerListener {
 		@Override
 		public void onNewAsteriskChannel(AsteriskChannel channel) {
 			registerChannel(channel);
 		}
 
 		@Override
 		public void onNewMeetMeUser(MeetMeUser user) {}
 
 		@Override
 		public void onNewAgent(AsteriskAgentImpl agent) {}
 
 		@Override
 		public void onNewQueueEntry(AsteriskQueueEntry entry) {}
 	}
 
 }
