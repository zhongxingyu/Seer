 package di.kdd.smartmonitor.protocol;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import android.util.Log;
 
 import di.kdd.smartmonitor.ISampler;
 import di.kdd.smartmonitor.protocol.exceptions.ConnectException;
 import di.kdd.smartmonitor.protocol.exceptions.MasterException;
 
 public class DistributedSystem implements ISmartMonitor, IObservable {
 	private ISampler sampler;
 	private List<IObserver> observers = new ArrayList<IObserver>();
 
 	private DistributedSystemNode node;
 	
 	private long samplingStarted, samplingEnded;
 	
 	/* States */
 	
 	private boolean isConnected;
 	private boolean isSampling;
 	
 	private static final String TAG = "distributed system";
 	
 	/* Singleton implementation */
 
 	private static DistributedSystem ds;
 	
 	private DistributedSystem() {
 	}
 	
 	public static DistributedSystem getInstance() {
 		if(ds == null) {
 			ds = new DistributedSystem();
 		}
 		
 		return ds;
 	}
 
 	/* IObservable implementation */
 	
 	@Override
 	public void unsubscribe(IObserver observer) {
 		observers.remove(observer);
 	}
 
 	@Override
 	public void subscribe(IObserver observer) {
 		observers.add(observer);		
 	}
 	
 	@Override
 	public void notify(String message) {
 		for(IObserver observer : observers) {
 			observer.showToastNotification(message);
 		}
 	}
 
 	/* ISmartMonitor implementation */
 		
 	@Override
 	public void connect() {
 		Log.i(TAG, "Connecting");
 		
 		ConnectTask connectTask = new ConnectTask(this);
 		connectTask.execute();
 	}
 
 	
 	protected void failedToConnectAsPeer() {
 		notify("Failed to connect as Peer");
 	}
 	
 	/***
 	 * Handler to be called from the ConnectTask, if the node didn't get a JOIN response
 	 */
 	
 	protected void connectedAsMaster() {
 		Log.i(TAG, "Connected as Master");
 
 		node = new MasterNode();		
 		isConnected = true;
 		
 		notify("Connected as Master");		
 	}
 
 	/***
 	 * Handler to be called from the ConnectTask, if the node got response to JOIN message
 	 * @param socket The connected to the Master node socket
 	 */
 	
 	protected void connectedAsPeer(Socket socket) {
 		Log.i(TAG, "Connected as Peer");
 
 		node = new PeerNode(socket);
 		isConnected = true;
 		
 		notify("Connected as Peer");		
 	}
 
 	@Override
 	public void connectAsMaster() {
 		Log.i(TAG, "Connecting as Master");
 		
 		node = new MasterNode(); 
 		isConnected = true;
 		
 		notify("Connected as Master");			
 	}
 
 	@Override
 	public void connectAt(String ip) {
 		Log.i(TAG, "Connecting as Peer at " + ip);
 
 		ConnectTask connectTask = new ConnectTask(this, ip);
 		connectTask.execute();
 	}	
 	
 	@Override
 	public boolean isConnected() {
 		return isConnected;
 	}
 	
 	@Override
 	public void disconnect() {
 		Log.i(TAG, "Disconnecting");
 		
 		node.disconnect();
		node = null;
 		isConnected = false;
 		
 		notify("Disconnected");
 	}
 
 	@Override
 	public void setSampler(ISampler sampler) {
 		this.sampler = sampler;
 	}
 	
 	@Override
 	public void startSampling() throws MasterException, IOException, ConnectException {		
 		if(node == null) {
 			throw new ConnectException();
 		}
 
 		if(node.isMaster() == false) {
 			throw new MasterException();
 		}
 		
 		((MasterNode) node).startSampling();			
 		
 		sampler.startSamplingService();
 		samplingStarted = System.currentTimeMillis();		
 		isSampling = true;
 		
 		notify("Started sampling");
 	}
 
 	@Override
 	public void stopSampling() throws MasterException, IOException, ConnectException {
 		if(node == null) {
 			throw new ConnectException();
 		}
 
 		if(node.isMaster() == false) {
 			throw new MasterException();
 		}
 		
 		((MasterNode) node).stopSampling();			
 		
 		sampler.stopSamplingService();
 		samplingEnded = System.currentTimeMillis();		
 		isSampling = false;
 
 		notify("Stoped sampling");
 	}
 	
 	@Override
 	public boolean isSampling() {
 		return isSampling;
 	}
 		
 	@Override
 	public void computeModalFrequencies(Date from, Date to) throws MasterException, IOException, ConnectException {
 		if(node == null) {
 			throw new ConnectException();
 		}
 
 		if(node.isMaster() == false) {
 			throw new MasterException();
 		}
 		
 		((MasterNode) node).computeModalFrequencies(from, to);
 		
 		notify("Computed modal frequencies");
 	}
 
 	@Override
 	public boolean isMaster() {
 		return (node !=null && node.isMaster());
 	}
 
 	@Override
 	public String getMasterIP() {
 		return (node != null) ? "None" : node.getMasterIP();
 	}
 }
