 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.HashMap;
 import java.util.Set;
 
 public class Controller extends Module {
 	
 	private Configuration configInstance;
 	private Logger logInstance;
 	private String peerID;
 	private Server serverInstance;
 	private List<Peer> neighborPeers;
 	private boolean isShuttingDown;
         private OptimisticNeighborManager optimisticNeighborManager;
         private PreferredNeighborManager preferredNeighborManager;
 
 	public Controller(String peerID)
 	{
 		this.peerID = peerID;
 	}
 	
 	@Override
 	public void initialConfiguration() {
 			if(configInstance == null)
 			{
 				configInstance = (Configuration) ModuleFactory.createConfigMod();
 				
 			}
 			
 			if(logInstance == null)
 			{
 					logInstance = (Logger) ModuleFactory.createLogMod(peerID);
 			}
 
 			if(serverInstance == null)
 			{
 					serverInstance = (Server) ModuleFactory.createServerMod(peerID, (Controller) this);
 			}
 			
 			if(neighborPeers == null)
 			{
 				neighborPeers = new ArrayList<Peer>();
 			}
 			
 			isShuttingDown = false;
 
 	}
 	
 	public void execute()
 	{
 			try {
 				createServers();	
 				createClients();
 			} catch (UnknownHostException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 	}
 	
 	public void setPeerID(String peerID)
 	{
 		this.peerID = peerID;
 	}
 	
 	public String getPeerID()
 	{
 		return peerID;
 	}
 	
 	public boolean isShuttingDown()
 	{
 		return isShuttingDown;
 	}
 
 	public void createServers()
 	{
 		new Thread(serverInstance).start();
 	}
 	
 	public void createClients() throws UnknownHostException, IOException
 	{
 		HashMap<String, Configuration.PeerInfo> map = configInstance.getPeerList();
 
 
 		
 		Set<String> peerKeys = map.keySet();
 		
 		for(String peerKey : peerKeys)
 		{
 		
 				if(Integer.parseInt(peerID) > Integer.parseInt(peerKey))
 				{
					Socket socket = new Socket(map.get(peerKey).getHostName(), map.get(peerKey).getPortNumber());
 					
 					Peer clientPeer = (Peer) ModuleFactory.createPeer(socket, this);
 					
 					new Thread(clientPeer).start();
 				}
 		}
 		
 	}
 	
 	public int getNumberOfConnectedPeers(String peerID)
 	{
 		int numOfPeers = 0;
 		
 		HashMap<String, Configuration.PeerInfo> peers = configInstance.getPeerList();
 		
 		Set<String> peerKeys = peers.keySet();
 		
 		for(String peerKey : peerKeys)
 		{
 			if(Integer.parseInt(peerID) < Integer.parseInt(peerKey))
 			{
 				numOfPeers++;
 			}
 		}
 		
 		return numOfPeers;	
 		
 	}
 	
 	public void addNeighbors(Peer peer)
 	{
 		neighborPeers.add(peer);
 	}
 	
 	public List<Peer> getNeighborsList()
 	{
 		return neighborPeers;
 	}
         
         public Configuration getConfiguration()
         {
             return configInstance;
         }
 	
 	public Module getLogger()
 	{
 		return logInstance;
 	}
 
 }
 
