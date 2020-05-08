 package network;
 
 import interfaces.MediatorNetwork;
 import interfaces.NetworkMediator;
 import interfaces.NetworkService;
 import interfaces.NetworkTransfer;
 
 import java.net.InetSocketAddress;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.SocketChannel;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.apache.log4j.Logger;
 
 import data.Message;
 import data.Service;
 import data.Service.Status;
 import data.UserProfile;
 
 /**
  * Network module implementation.
  * 
  * @author Paul Vlase <vlase.paul@gmail.com>
  */
 public class NetworkImpl implements NetworkMediator, NetworkTransfer, NetworkService {
 	private static Logger logger = Logger.getLogger(NetworkImpl.class);
 
 	private MediatorNetwork									mediator;
 
 	private NetworkReceiveEvents							receiveEvents;
 	private NetworkSendEvents								sendEvents;
 
 	private ConcurrentHashMap<String, SocketChannel>		userChanelMap;
 	private ConcurrentHashMap<String, ArrayList<Message>>	userUnsentMessages;
 
 	private NetworkDriver									driver;
 
 	public NetworkImpl(MediatorNetwork med) {
 		//TODO: logger.setLevel(Level.OFF);
 
 		this.mediator = med;
 
 		med.registerNetwork(this);
 
 		userChanelMap = new ConcurrentHashMap<String, SocketChannel>();
 		userUnsentMessages = new ConcurrentHashMap<String, ArrayList<Message>>();
 
 		driver = new NetworkDriver(this);
 	}
 
 	public InetSocketAddress getAddress() {
 		return driver.getAddress();
 	}
 
 	public NetworkReceiveEvents getEventsTask() {
 		return receiveEvents;
 	}
 
 	public NetworkDriver getDriver() {
 		return driver;
 	}
 
 	public void setDriver(NetworkDriver driver) {
 		this.driver = driver;
 	}
 
 	@Override
 	public boolean startTransfer(Service service) {
 		return true;
 	}
 	
 	@Override
 	public void stopTransfer(Service service) {
 	}
 
 	@Override
 	public UserProfile getUserProfile() {
 		return mediator.getUserProfile();
 	}
 
 	public void registerConnection(String username, SocketChannel chanel) {
 		userChanelMap.put(username, chanel);
 	}
 
 	public Service getService(String serviceName) {
 		return mediator.getOffer(serviceName);
 	}
 
 	public ConcurrentHashMap<String, ArrayList<Message>> getUserUnsentMessages() {
 		return userUnsentMessages;
 	}
 
 	public void setUserUnsentMessages(ConcurrentHashMap<String, ArrayList<Message>> userUnsentMessages) {
 		this.userUnsentMessages = userUnsentMessages;
 	}
 
 	public NetworkReceiveEvents getReceiveEvents() {
 		return receiveEvents;
 	}
 
 	public void setReceiveEvents(NetworkReceiveEvents receiveEvents) {
 		this.receiveEvents = receiveEvents;
 	}
 
 	public NetworkSendEvents getSendEvents() {
 		return sendEvents;
 	}
 
 	public void setSendEvents(NetworkSendEvents sendEvents) {
 		this.sendEvents = sendEvents;
 	}
 
 	public ConcurrentHashMap<String, SocketChannel> getUserChanelMap() {
 		return userChanelMap;
 	}
 
 	public void setUserChanelMap(ConcurrentHashMap<String, SocketChannel> userChanelMap) {
 		this.userChanelMap = userChanelMap;
 	}
 
 	public MediatorNetwork getMediator() {
 		return mediator;
 	}
 
 	public void setMediator(MediatorNetwork mediator) {
 		this.mediator = mediator;
 	}
 
 	public void changeServiceNotify(Service service) {
 		mediator.changeServiceNotify(service);
 	}
 
 	public void logIn() {
 		logger.debug("Begin");
 		receiveEvents = new NetworkReceiveEvents(this);
 		sendEvents = new NetworkSendEvents(this.getDriver());
 
 		driver.startRunning();
 		receiveEvents.start();
 		sendEvents.start();
 
 		logger.debug("End");
 	}
 
 	/**
 	 * Stop receiving or sending messages
 	 */
 	public void logOut() {
 		logger.debug("Begin");
 		try {
 			driver.stopRunning();
 			receiveEvents.stopRunning();
 			sendEvents.stopRunning();
 
 			driver = new NetworkDriver(this);
 			receiveEvents = null;
 			sendEvents = null;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		logger.debug("End");
 	}
 
 	@Override
 	public void publishService(Service service) {
 		logger.debug("Begin");
 		logger.debug("Service : " + service);

 		ArrayList<Message> messages = service.asMessages(this);
 		logger.debug("Messages : " + messages);
 
 		for (int i = 0; i < messages.size(); i++) {
 			Message message = messages.get(i);
 			driver.sendData(message, message.getDestination(), service.getUsers().get(i).getAddress());
 		}
		
		service.executeNet(this);
 		logger.debug("End");
 	}
 
 	@Override
 	public void publishServices(ArrayList<Service> services) {
 		// TODO : Process send messages
 	}
 }
