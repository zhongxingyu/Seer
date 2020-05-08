 package chat;
 
 import hypeerweb.HyPeerWebSegment;
 import hypeerweb.Node;
 import hypeerweb.NodeCache;
 import hypeerweb.visitors.SendVisitor;
 import java.io.Serializable;
import java.util.ArrayList;
 import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Objects;
 import java.util.Random;
 
 /**
  * Handles communications in the chat network
  */
 public class ChatServer{
 	private final HyPeerWebSegment<HyPeerWebSegment<Node>> segment;
 	private String networkName = "";
 	//Node cache for the entire HyPeerWeb
 	private NodeCache cache;
 	//Cached list of all users
 	private static final Random randomName = new Random();
 	private final HashMap<Integer, ChatUser> users = new HashMap();
 	//List of all users and their GUI/Clients that are leeching on this network
 	private final HashMap<Integer, Client> clients = new HashMap();
 	
 	public ChatServer() throws Exception{
 		segment = new HyPeerWebSegment("InceptionWeb.db", -1);
 		segment.setData("ChatServer", this);
 		/* TODO:
 			Join the network by creating another node in "segment"
 			Fetch the following from another segment in the InceptionWeb
 			- cache
 			- chatUsers
 		*/
 	}
 	
 	//GUI
 	/**
 	 * Register a GUI/Client with this server
 	 * @param nwl network name listener
 	 * @param nl node change listener
 	 * @param sl send chat message listener
 	 * @param ul user update listener
 	 * @return the ChatUser for this GUI/Client
 	 */
 	public ChatUser registerClient(NetworkNameListener nwl, NodeListener nl, SendListener sl, UserListener ul){
 		Client c = new Client(nwl, nl, sl, ul);
 		//Generate a userID that has not been taken already
 		int newUser;
 		do{
 			newUser = randomName.nextInt(9999);
 		} while (users.containsKey(newUser));
 		c.user = new ChatUser(newUser, "user"+newUser, segment.getWebId());
 		users.put(newUser, c.user);
 		clients.put(newUser, c);
 		//TODO, broadcast this user update to all segments & userListeners
 		//TODO, send the client the nodecache, userlist, etc, through the listeners
 		return c.user;
 	}
 	/**
 	 * Unregisters a GUI/Client from the server
 	 * @param userID the user ID associated with this client
 	 */
 	public void unregisterClient(int userID){
 		users.remove(userID);
 		clients.remove(userID);
 		//TODO, broadcast this user update to all other segments
 	}
 	/**
 	 * Client object; holds all listeners and a
 	 * reference to the client's ChatUser
 	 */
 	private class Client{
 		public NetworkNameListener networkListener;
 		public NodeListener nodeListener;
 		public SendListener sendListener;
 		public UserListener userListener;
 		public ChatUser user;
 		
 		public Client(NetworkNameListener nwl, NodeListener nl, SendListener sl, UserListener ul){
 			networkListener = nwl;
 			nodeListener = nl;
 			sendListener = sl;
 			userListener = ul;	
 		}
 	}
 	
 	//NETWORK
 	/**
 	 * Spawn a new server off of this one
 	 */
 	public void joinNetwork(){
 		//We may need to write our own communication thing
 		//instead of calling this method
 	}
 	/**
 	 * Leech off of this server
 	 */
 	public void watchNetwork(){
 		//We may need to write our own communication thing
 		//instead of calling this method
 	}
 	/**
 	 * Disconnect from the network
 	 */
 	public void disconnect(){
 		//this one looks tough
 		//I think this is the part where Dr. Woodfield said that if one segment
 		//wanted to quit, all of the segments would have to quit.  Now I can 
 		//see why.  Sending all of the nodes on this segment to live somewhere
 		//else would be difficult.
 	}
 	/**
 	 * Change the ChatServer's name
 	 * @param name 
 	 */
 	public void changeNetworkName(String name){
 		networkName = name;
 		//broadcast to all network name listeners
 	}
 	public static abstract class NetworkNameListener{
 		abstract void callback();
 	}
 	
 	//NODES
 	/**
 	 * Adds a node to the HyPeerWeb and tells the nodeListeners about it.
 	 */
 	public void addNode(){
 		segment.getFirstSegmentNode().addNode(new Node.Listener() {
 			@Override
 			public void callback(Node n) {
 				resyncCache(n, NodeCache.SyncType.ADD);
 			}
 		});
 	}
 	/**
 	 * Deletes a node from the HyPeerWeb and tells the nodeListeners about it.
 	 * @param webID the webID of the node to delete
 	 */
 	public void removeNode(int webID){
 		HyPeerWebSegment hws = segment.getFirstSegmentNode();
 		hws.removeNode(webID, new Node.Listener(){
 			@Override
 			public void callback(Node n) {
 				resyncCache(n, NodeCache.SyncType.REMOVE);
 			}
 		});
 	}
 	/**
 	 * Resyncs the node cache to the actual data in the HyPeerWeb
 	 * @param n the Node that changed
 	 * @param type the change type
 	 */
 	private void resyncCache(Node n, NodeCache.SyncType type){
 		//These are a list of dirty nodes in our cache
 		int[] dirty;
 		switch (type){
 			case ADD:
 				dirty = cache.addNode(n, true);
 				break;
 			case REMOVE:
 				dirty = cache.removeNode(n, true);
 				break;
 		}
 		//Retrieve all dirty nodes
 		NodeCache.Node clean[] = new NodeCache.Node[dirty.length];
 		
 		//Notify all listeners that the cache changed
 		for (NodeListener listener : nodeListeners)
 			listener.callback(node, false);
 	}
 	public static abstract class NodeListener{
 		abstract void callback(NodeCache.Node affectedNode, NodeCache.SyncType type, NodeCache.Node[] updatedNodes);
 	}
 	
 	//CHAT
 	/**
 	 * Sends a message to another ChatUser
 	 * @param senderID who sent this message
 	 * @param recipientID who should receive the message (-1 to give to everyone)
 	 * @param message the message
 	 */
 	public void sendMessage(int senderID, int recipientID, String message){
 		SendVisitor visitor = new SendVisitor(user.getWebId());
 		visitor.visit(segment);
 	}
 	public static abstract class SendListener{
 		abstract void callback(int senderID, int recipientID, String mess);
 	}
 	
 	//USERS
 	/**
 	 * Changes a user's name
 	 * @param userID the user's id we want to update
 	 * @param name new name for this user
 	 */
 	public void changeUserName(int userID, String name){
 		if (name != null && users.containsKey(userID)){
 			users.get(userID).name = name;
 			//TODO, broadcast name change
 		}
 	}
 	public static abstract class UserListener{
 		abstract void callback();
 	}
 	public static class ChatUser implements Serializable{
 		//Random color generator
 		private static final Random rand = new Random();
 		private static final int minRGB = 30, maxRGB = 180;
 		//User attributes
 		public String color, name;
 		public int id;
 		//Server that owns this user
 		public int networkID;
 		
 		/**
 		 * Create a new chat user
 		 * @param id a unique id for this user
 		 * @param name the user's name
 		 * @param networkID the network that contains this user
 		 */
 		public ChatUser(int id, String name, int networkID){
 			//Random username color
 			//RGB values between 100-250
 			int delta = maxRGB-minRGB;
 			color = String.format(
 				"#%02x%02x%02x",
 				rand.nextInt(delta)+minRGB,
 				rand.nextInt(delta)+minRGB,
 				rand.nextInt(delta)+minRGB
 			);
 			this.name = name;
 			this.id = id;
 			this.networkID = networkID;
 		}
 		@Override
 		public String toString(){
 			return name;
 		}
 	}
 }
