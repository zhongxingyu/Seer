 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.SocketException;
 import java.util.HashMap;
 import java.util.Vector;
 
 
 
 public class ContactAddRemove implements Runnable {
 	private Machine m;
 
 	
 	public ContactAddRemove(Machine machine) {
 		m=machine;
 	}
 
 	
 	public void sendMemberListToIncoming(DatagramSocket s, String ip_addr) {
 		byte[] mList = null;
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 	    try {
 	    	ObjectOutputStream oos = new ObjectOutputStream(baos);
 	    	oos.writeObject(m.memberList);
 	    	oos.flush();
 	    	//TODO - need to decide whether we need to send length also in first packet and then actual packet
 	    	// get the byte array of the object
 	    	mList = baos.toByteArray();
 	    } catch(IOException e) {
 	    	e.printStackTrace();
 	    }
 		m.sendMsg(s, ip_addr, mList, Machine.MEMBERSHIP_PORT);
 	}
 	
 	
 	
 	public void run(){
 	
 		while(true){
 			String recvMsg;
 			
 				try {
 					recvMsg = m.recvStrMsg();
 					
 					Vector<String> memberList = m.getMemberList();
 					WriteLog.printList2Log("Contact", memberList);
 					
 					
 					//need to take decision based on the recvMsg opcode
 					if (recvMsg.charAt(0) == 'R')
 					{
 						String ip = recvMsg.substring(1).trim();
 						System.out.println(ip);
 						
 						WriteLog.writelog("Contact" ,"received incoming socket, remove " + ip );
 						
 						if (memberList.contains(ip)) {
 							
 							
 							//String prevIP = memberList.get((index - 1 + memberList.size()) % memberList.size());
 							//m.sendMsg(m.membership_sock, prevIP, recvMsg, Machine.MEMBERSHIP_PORT);
 							
 							//String prevprevIP = memberList.get((index - 1 + memberList.size()) % memberList.size());
 							//m.sendMsg(m.membership_sock, prevprevIP, recvMsg, Machine.MEMBERSHIP_PORT);
 							String newMaster = null;
 							if (ip == m.masterName)
 							{
 								int mindex = memberList.indexOf(m.masterName);
 								newMaster = memberList.get((mindex + 1) % memberList.size());
 								m.masterName = newMaster;
 							}
 							
 							memberList.remove(ip);
 							//int index = memberList.indexOf(m.myName);
 							//String prevIP = memberList.get((index - 1 + memberList.size()) % memberList.size());
 							try {
 								WriteLog.printList2Log(m.myName, memberList);
 								
 							} catch (IOException e) {
 								// TODO Auto-generated catch block
 								e.printStackTrace();
 							}
 							//code for checking if now I am a new master, if yes, i need to run some code
 							//change the mode
 							//contact all the nodes in the memberlist for file replication info
 							
 							if(m.myName == newMaster)
 							{
 								m.master = true;
 								m.file_node_map = new HashMap<String, Vector<String>>();
 								m.node_file_map = new HashMap<String, Vector<String>>();
 								m.FileReplicator.reformFileInfo();
 							}
 						}
 						//memberList.remove(recvMsg.trim());
 						WriteLog.printList2Log("Contact", memberList);
 						
 						if (m.master)
 						{
 							
 							if(m.node_file_map.containsKey(ip))
 								m.node_file_map.remove(ip);
 							for (String tkey : m.file_node_map.keySet())
 							{
 								Vector<String> tvalue = m.file_node_map.get(tkey);
 								if (tvalue.contains(ip))
 								{
 									// TODO - can it work?? will this affect file_node_map?
 									tvalue.remove(ip);
 									// TODO - trigger file balancing thread here
 								}
 							}
 							//call to filereplicator for balancing maps
 							m.FileReplicator.balanceFiles();
 						}
 					}
 					else if (recvMsg.charAt(0) == 'A')
 					{
 						// received an incoming socket, append ip address to memberlist
 						//String incomingIP = new String(incoming.getData());
 						String ip = (recvMsg.substring(1)).trim();
 														
 						System.out.println(ip);
 						
 						WriteLog.writelog("Contact" ,"received incoming socket, append " + ip);
 						
 						//System.out.println(incomingIP.length());
 						
 						
 						if (!memberList.contains(ip)) {
 							//System.out.println("999999999999!!!!!!!");
 							try {
 								WriteLog.writelog(m.myName, "adddddddddddddd " + ip);
 							} catch (IOException e1) {
 								// TODO Auto-generated catch block
 								e1.printStackTrace();
 							}
 							memberList.add(ip);
 								
 							//int index = m.memberList.indexOf(m.myIP);
 							//String nextIP = m.memberList.get((index + 1) % memberList.size());
 							//m.sendMsg(m.membership_sock, nextIP, recvMsg, Machine.MEMBERSHIP_PORT);
 							
 							//need to review
 							//String nextnextIP = memberList.get((index + 2) % memberList.size());
 							//m.sendMsg(m.membership_sock, nextnextIP, recvMsg, Machine.MEMBERSHIP_PORT);				
 							
 							try {
 								WriteLog.printList2Log(m.myName, memberList);
 								//WriteLog.writelog(m.myIP, "send to "+nextIP+" msg is " + recvMsg);
 							} catch (IOException e) {
 								// TODO Auto-generated catch block
 								e.printStackTrace();
 							}
 						}
 						
 						
 						WriteLog.printList2Log("Contact", memberList);
 						
 						//m.membership_sock.close();
 						
 						//if(memberList.size()>1){
 						//sendAddMsg();
 						//}
 						
 					}
 					else if (m.master)
 					{
 						if (recvMsg.charAt(0) == 'J')
 						{
 							String ip = (recvMsg.substring(1)).trim();
 							WriteLog.writelog("Contact" ,"received incoming socket, join " + ip );
 						
 							if (!memberList.contains(ip)) {
 								//TODO
 								//need to review..some more code needs to be added here
 								memberList.add(ip);
 								sendMemberListToIncoming(m.membership_sock, ip);
 								m.sendMsgToAllNodes(ip, "ADD");
 							} else {
 								//TODO
 								//need to review..some map related processing will be different in these scenarios
 								sendMemberListToIncoming(m.membership_sock, ip);
 							}
 					
 							if(!m.node_file_map.containsKey(ip))
							{
 								//TODO - need to decide whether we are going to save the replicated file info while dying 
 								// and then sending it in J message to the master
								Vector<String> emptyList = new Vector<String>();
								m.node_file_map.put(ip, emptyList);
							
								//TODO - need to run balancing algorithm here
 								m.FileReplicator.balanceFiles();
								// get the id's of the files written to this node and then update the file_node_map accordingly
								// or we could update it inside balanceFiles itself
								//TODO - need to send out the ADD message to all the nodes
								//m.sendMsgToAllNodes(ip, "ADD");
							}
 						}
 					}
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				
 		}	
 	}
 }
