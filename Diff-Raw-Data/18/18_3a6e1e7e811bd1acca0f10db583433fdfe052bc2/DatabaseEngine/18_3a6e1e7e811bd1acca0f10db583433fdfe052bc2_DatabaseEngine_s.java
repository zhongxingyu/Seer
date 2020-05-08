 package org.publicmain.sql;
 
 import java.util.AbstractMap;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import javax.swing.JComboBox;
 import javax.swing.JTable;
 import javax.xml.crypto.NodeSetData;
 
 import org.publicmain.chatengine.ChatEngine;
 import org.publicmain.common.MSG;
 import org.publicmain.common.Node;
 import org.publicmain.nodeengine.NodeEngine;
 
 public class DatabaseEngine {
 	
 	private LocalDBConnection localDB;
 	private BackupDBConnection backupDB;
 	
 	private BlockingQueue<MSG> msg2Store;
 	private BlockingQueue<Node> node2Store;
 	private BlockingQueue<Map.Entry<Long, Long>> routes2Store;
 	private BlockingQueue<String> groups2Store; 
 	
 	private HashSet<MSG> failed_msgs;
 	private List<Node> failed_node;
 	private BlockingQueue<Map.Entry<Long, Long>> failed_routes;
 //	private HashSet<Node> stored_nodes;
 //	private HashSet<Node> stored_routes;
  
 	private static DatabaseEngine me;
 	Thread transporter = new Thread(new DPTransportBot());
 	
 	private DatabaseEngine() {
 		me=this;
 		localDB = LocalDBConnection.getDBConnection(this);
 		backupDB = BackupDBConnection.getBackupDBConnection();
 		
 		msg2Store = new LinkedBlockingQueue<MSG>();
 		node2Store = new LinkedBlockingQueue<Node>();
 		routes2Store = new LinkedBlockingQueue<Map.Entry<Long,Long>>();
 		groups2Store = new LinkedBlockingQueue<String>();
 		failed_msgs = new HashSet<MSG>();
 		failed_node = new ArrayList<Node>();
 		failed_routes = new LinkedBlockingQueue<Map.Entry<Long,Long>>();
 		
 		transporter.start();
 	}
 	
 	public synchronized static DatabaseEngine getDatabaseEngine() {
 		if(me==null) new DatabaseEngine();
 		return me;
 	}
 	
 	public void put(MSG x){
 		msg2Store.offer(x);
 	}
 	
 	public void put(Node x){
 		node2Store.offer(x);
 	}
 
 	public void put(Collection<Node> x){
 		for (Node node : x) {
 			node2Store.offer(node);
 		}
 	}
 	
 	public void put(String group){
 		groups2Store.add(group);
 	}
 	
 
 	public void put(long target, long gateway){
 		routes2Store.offer(new AbstractMap.SimpleEntry(target, gateway));
 	}
 	
 	public void deleteLocalHistory() {
 		localDB.deleteAllMsgs();
 	}
 	
 	public void deleteBackup() {
 //		TODO:Tell BackupServer to Drop users data
 	}
 	
 	
 	public void push(){
 		//TODO:
 	}
 	
 	public void pull(){
 		//TODO: HILFE
 	}
 	
 	private final class DPTransportBot implements Runnable {
 		@Override
 		public void run() {
 			boolean locDBWasConnectetBefore = false;
 			while (true) {
 				if (!localDB.getStatus()) {
 					synchronized (transporter) {
 						try {
 							if (locDBWasConnectetBefore) localDB.reconnectToLocDBServer();
 							transporter.wait();
 							locDBWasConnectetBefore = true;
 						} catch (InterruptedException e) {
 						}
 					}
 				}
 				while (true) {
 					// kopiere msgs
 					List<MSG> tmp_msg = new ArrayList<MSG>(msg2Store);
 					msg2Store.removeAll(tmp_msg);
 					
 					// kopiere groups
 					List<String> tmp_groups = new ArrayList<String>(groups2Store);
 					groups2Store.removeAll(tmp_groups);
 					
 					// kopiere nodes
 					List<Node> tmp_nodes = new ArrayList<Node>(node2Store);
 					node2Store.removeAll(tmp_nodes);
 
 					// kopiere routen
 					List<Map.Entry<Long, Long>> tmp_routes = new ArrayList<Map.Entry<Long, Long>>(routes2Store);
 					routes2Store.removeAll(tmp_routes);
 					
 					// schreibe nodes
 					if(!localDB.writeAllUsersToDB(tmp_nodes)){
 						failed_node.addAll(tmp_nodes);
 						if(!localDB.getStatus()) break;
 					}
 					
 					// schreibe groups
 					localDB.writeAllGroupsToDB(tmp_groups);
 					
 					// schreibe msgs
 					boolean all_msgs_written=true;
 					for (MSG current : tmp_msg) {
 						if(!localDB.writeMsgToDB(current)){
 							all_msgs_written=false;
 							failed_msgs.add(current);
 						}
 					}
 					if(!all_msgs_written){
 						if(!localDB.getStatus()){
 							break;
 						}
 					}
 					
 					//schreibe alle routen
 					for (Entry<Long, Long> tmp_route : tmp_routes){
 						if(!localDB.writeRoutingTableToDB(tmp_route.getKey(),NodeEngine.getNE().getNode(tmp_route.getKey()).getHostname(), NodeEngine.getNE().getUIDforNID(tmp_route.getKey()), tmp_route.getValue())){
 							failed_routes.add(tmp_route);
 							break;
 						}
 					}
 					
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 					}
 				}
 			}
 		}
 	}
 	
	/**Queries the local Database for Messages of a selected user (<code>uid</code>) which have been send after the begin date but before the end date. 
 	 * Is either of the given dates a negative long the respective field will be ignored. Additionialy the Querry can be further narrowed by giving a search text.
 	 * Only messages where the written text contains the text (case insensitiv) will be returned. If the searchtext is an empty String it will be ignored.  
 	 * @param uid UserId of User whose messages should be retrieved
 	 * @param begin only messages after the given date (in long) will be considered. begin date will be ignored if negative 
 	 * @param end only messages before the given date (in long) will be considered. end date will be ignored if negative
 	 * @param text only messages containing this text will be shown. text will be ignored if empty string
 	 * @return a JTable listing all the messages fitting the given attributes (Maybe the Database Engine will update the Datamodel of the HistoryWindow later)
 	 */
 	public JTable selectMSGsByUser(long uid,long begin, long end,String text) {
 		return null;
 	}
 	
	/**Queries the local Database for Messages which have been send with the given <code>alias</code> if send after the begin date but before the end date. 
 	 * Is either of the given dates a negative long the respective field will be ignored. Additionialy the Querry can be further narrowed by providing a search <code>text</code>.
 	 * Only messages where the written text contains the text (case insensitiv) will be returned. If the searchtext is an empty String it will be ignored.  
 	 * @param alias used in message
 	 * @param begin only messages after the given date (in long) will be considered. begin date will be ignored if negative 
 	 * @param end only messages before the given date (in long) will be considered. end date will be ignored if negative
 	 * @param text only messages containing this text will be shown. text will be ignored if empty string
 	 * @return a JTable listing all the messages fitting the given attributes
 	 */	
 	public JTable selectMSGsByAlias(String alias,long begin, long end,String text) {
 		return null;
 	}
 
	/**Queries the local Database for Messages which have been send within the given <code>group</code> if send after the begin date but before the end date. 
 	 * Is either of the given dates a negative long the respective field will be ignored. Additionialy the Querry can be further narrowed by providing a search <code>text</code>.
 	 * Only messages where the written text contains the text (case insensitiv) will be returned. If the searchtext is an empty String it will be ignored.  
 	 * @param group the message was send to
 	 * @param begin only messages after the given date (in long) will be considered. begin date will be ignored if negative 
 	 * @param end only messages before the given date (in long) will be considered. end date will be ignored if negative
 	 * @param text only messages containing this text will be shown. text will be ignored if empty string
 	 * @return a JTable listing all the messages fitting the given attributes
 	 */	
 	public JTable selectMSGsByGroup(String group,long begin, long end,String text) {
 		return null;
 	}
 	
 	public JComboBox getUsers(){
 		return new JComboBox<UserIdentifier>();
 	}
 
 	public synchronized void go() {
 		synchronized (transporter) {
 			transporter.notify();
 		}
 	}
 	
 	class UserIdentifier{
 		public String alias;
 		public long uid;
 		public String username;
 	}
 
 	
 	
 	
 
 }
