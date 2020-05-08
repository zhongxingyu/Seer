 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.SocketException;
 import java.util.*;
 
 
 
 /*
  * FILEREPLICATOR INSTANCE OF MACHINE CLASS
  * WILL TAKE CARE OF ALL THE FILE SYSTEM RELATED TASKS
  * FILE DISTRIBUTION BALANCING
  * FILE GET, PUT, DELETE
  */
 public class FileReplication implements Runnable {
 	private Machine m;
 	public FileTransferClient FileClient;
 	public FileTransferServer FileServer;
 	public int min_rep=2;
 	private boolean rep_info_reformed = false;
 	HashMap<String, Integer> checkReplies = new HashMap<String, Integer>();
 
 	public FileReplication(Machine machine)
 	{
 		m=machine;
 		FileServer = new FileTransferServer();
 	}
 
 	private void sendListMsg(Vector<String> msgList, String nodeName)
 	{
 		byte[] mList = null;
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		try {
 			ObjectOutputStream oos = new ObjectOutputStream(baos);
 			oos.writeObject(msgList);
 			oos.flush();
 			mList = baos.toByteArray();
 		} catch(IOException e) {
 			e.printStackTrace();
 		}
 		m.sendMsg(m.filerep_sock, nodeName, mList, Machine.FILE_OPERATIONS_PORT);
 
 		try {
 			WriteLog.writelog(m.myName, "sent "+msgList.toString() + " to "+ nodeName);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 
 	private Vector<String> recvListMsg()
 	{
 		DatagramPacket recvPacket;
 		byte[] recvData = new byte[1024];
 		Vector<String> returnList=new Vector<String>();
 		try {
 			recvPacket = new DatagramPacket(recvData,recvData.length);
 			m.filerep_sock.receive(recvPacket);
 			//TODO - need to decide whether we need to define this length or not!!
 			ByteArrayInputStream bais = new ByteArrayInputStream(recvData);
 
 			ObjectInputStream ois = new ObjectInputStream(bais);
 			returnList = (Vector<String>)ois.readObject();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();			
 		}
 		try {
 			WriteLog.writelog(m.myName, "received "+returnList.toString() +" through "+ Integer.toString(Machine.FILE_OPERATIONS_PORT));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return(returnList);
 	}
 
 
 	private void sendCOPYMessage(String tNodeID, String rFile, String lFile, String nodeID) {
 		Vector<String> msgList=new Vector<String>();
 		msgList.add("C");
 		msgList.add(rFile);
 		msgList.add(lFile);
 		msgList.add(nodeID);
 
 		sendListMsg(msgList, tNodeID);
 	}
 
 
 	private void sendCOPYMessage(String tNodeID, String rFile, String lFile, String nodeID, boolean append) {
 		Vector<String> msgList=new Vector<String>();
 		msgList.add("C");
 		msgList.add(rFile);
 		msgList.add(lFile);
 		msgList.add(nodeID);
 		if(append) {
 			msgList.add("append");
 		}
 
 		sendListMsg(msgList, tNodeID);
 	}
 
 	
 	
 	
 	public void sendSDFSGetMessage(String FileID) {
 		//SDFC_GET packet - Opcode + SDFS_FileID + SourceNodeID
 		String opcode = "SDFS_GET";
 		Vector<String> getPacket = new Vector<String>();
 
 		getPacket.add(opcode);
 		getPacket.add(FileID);
 		getPacket.add(m.myName);
 
 		//TODO - update myFileList, return only when file transfer completes
 		try {
 			WriteLog.writelog(m.myName, "SDFSGet Message sent to master for "+FileID);
 		} catch (IOException e3) {
 			// TODO Auto-generated catch block
 			e3.printStackTrace();
 		}
 		sendListMsg(getPacket, m.masterName);
 		
 		//crucial change...will keep waiting here till the myFileList gets populated with the fileID
 		while(!m.myFileList.contains(FileID));
 	
 		try {
 			WriteLog.writelog(m.myName, "SDFSGet completed for "+FileID);
 		} catch (IOException e3) {
 			// TODO Auto-generated catch block
 			e3.printStackTrace();
 		}
 	}
 
 
 	public void sendSDFSPutMessage(String lFileID, String rFileID) {
 		//SDFS_PUT packet - Opcode + Local_FileID + SDFS_FileID + SourceNodeID
 		Vector<String> putMsg = new Vector<String>();
 		putMsg.add("P");
 		putMsg.add(lFileID);
 		putMsg.add(rFileID);
 		putMsg.add(m.myName);
 		
 		sendListMsg(putMsg, m.masterName);
 	}
 	
 	
 	public void sendSDFSPutMessage(String lFileID, String rFileID, boolean append) {
 		//SDFS_PUT packet - Opcode + Local_FileID + SDFS_FileID + SourceNodeID
 		Vector<String> putMsg = new Vector<String>();
 		putMsg.add("P");
 		putMsg.add(lFileID);
 		putMsg.add(rFileID);
 		putMsg.add(m.myName);
 		if(append) {
 			putMsg.add("append");
 		}
 
 		sendListMsg(putMsg, m.masterName);
 	}
 
 
 	private Vector<String> sort_node_file_map()
 	{
 		Vector<String> keys = new Vector<String>(m.node_file_map.keySet());
 		final HashMap<String, Vector<String>> temp_file_node = m.node_file_map;
 		//List<String> tempKeys = (List<String>)keys;
 		//List<String> tempKeys = new ArrayList<String>(keys);
 
 		Collections.sort(keys, new Comparator<String>(){
 			public int compare(String firstkey, String secondkey){
 				//String firstkey = (String)first;
 				//String secondkey = (String)second;
 				Vector<String> firstValue = temp_file_node.get(firstkey);
 				Vector<String> secondValue = temp_file_node.get(secondkey);
 				int firstLength = firstValue.size();
 				int secondLength = secondValue.size();
 
 				return (firstLength - secondLength);
 			}
 		});
 		/*try {
 			WriteLog.writelog(m.myName, "sorted node_file_map - "+ keys.toString());
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}*/
 		return keys;  //TODO need to verify that sorting on tempkeys actually affects also keys
 
 	}
 
 	private Vector<String> sort_file_node_map()
 	{
 		Vector<String> keys = new Vector<String>(m.file_node_map.keySet());
 		final HashMap<String, Vector<String>> temp_file_node = m.file_node_map;
 		//List<String> tempKeys = (List<String>)keys;
 		//List<String> tempKeys = new ArrayList<String>(keys);
 
 		Collections.sort(keys, new Comparator<String>(){
 			public int compare(String firstkey, String secondkey){
 				//String firstkey = (String)first;
 				//String secondkey = (String)second;
 				Vector<String> firstValue = temp_file_node.get(firstkey);
 				Vector<String> secondValue = temp_file_node.get(secondkey);
 				int firstLength = firstValue.size();
 				int secondLength = secondValue.size();
 
 				return (firstLength - secondLength);
 			}
 		});
 		/*try {
 			WriteLog.writelog(m.myName, "sorted file_node_map - "+ keys.toString());
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}*/
 		return keys;  //TODO need to verify that sorting on tempkeys actually affects also keys
 	}
 
 
 	private int getAvgFilesPerNode()
 	{
 		float actAvg = 0.0f;
 		int intAvg = 0;
 		int cumCnt = 0;
 
 		for (String key: m.node_file_map.keySet())
 		{
 			cumCnt = cumCnt + m.node_file_map.get(key).size();
 		}
 
 		actAvg = (float)cumCnt/(float)m.node_file_map.keySet().size();
 
 		intAvg = (int)actAvg;
 
 		try {
 			WriteLog.writelog(m.myName, "average number of files per server - "+ Integer.toString(intAvg));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return(intAvg);
 	}
 
 
 
 	public void balanceFiles()
 	{
 		// TODO - called from ContactAddRemove when any node adds or leaves the network, used only by master
 		// will first sort the map keys according to the length of value field
 		//
 		Vector<String> node_file_keys, file_node_keys;
 
 		node_file_keys = sort_node_file_map();
 
 		file_node_keys = sort_file_node_map();
 
 		try {
 			WriteLog.writelog(m.myName, "balancefiles called ");
 			//WriteLog.writelog(m.myName, "initial node_file_keys - "+node_file_keys.toString());
 			//WriteLog.writelog(m.myName, "initial file_node_keys - "+file_node_keys.toString());
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		for(String tempKey: file_node_keys)
 		{
 			if (m.file_node_map.get(tempKey).size() < min_rep)
 			{
 				while (m.file_node_map.get(tempKey).size() < min_rep)
 				{
 					Vector<String> nodeList = m.file_node_map.get(tempKey);
 					String targetNode = null;
 					boolean goodNode=false;
 
 					while(!goodNode)
 					{	
 						for(String tmpIndex: node_file_keys)
 						{
 							if(!nodeList.contains(tmpIndex))
 							{
 								goodNode=true;
 								targetNode = tmpIndex;
 								break;
 							}
 						}
 					}
 					if(targetNode == null) {
 						try {
 							WriteLog.writelog(m.myName, "Couldn't find the GoodNode for re-replicating the file, hence moving to the next file!!");
 						} catch (IOException e1) {
 							// TODO Auto-generated catch block
 							e1.printStackTrace();
 						}
 						break;
 					}
 					/*Vector<String> msgList=new Vector<String>();
 					msgList.add("C");
 					msgList.add(tempKey);
 					msgList.add(tempKey);
 					msgList.add(nodeList.firstElement());
 
 					sendListMsg(msgList, targetNode);
 					 */
 					sendCOPYMessage(targetNode, tempKey, tempKey, nodeList.firstElement());
 					try {
 						Thread.sleep(100);
 					} catch(InterruptedException e) {
 						e.printStackTrace();
 					}
 					
 					m.file_node_map.get(tempKey).add(targetNode);
 					m.node_file_map.get(targetNode).add(tempKey);
 
 					node_file_keys = sort_node_file_map();
 				}
 			} else
 				break;
 		}
 
 		try {
 			WriteLog.writelog(m.myName, "balancefiles called, stage one done");
			//WriteLog.writelog(m.myName, "final file_node_keys - "+file_node_keys.toString());
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		int lowAvgFiles = 0;
 		lowAvgFiles = getAvgFilesPerNode();
 		int highAvgFiles = lowAvgFiles + 1;
 
 		int firstIndex=0, lastIndex=0;
 		lastIndex = node_file_keys.size()-1;
 
 		String firstKey = node_file_keys.get(firstIndex);
 		String lastKey = node_file_keys.get(lastIndex);  //TODO need to be sure about the maximum index value
 
 
 		
 		while(m.node_file_map.get(firstKey).size() < lowAvgFiles)
 		{
 			//have removed this 'm.node_file_map.get(lastIndex).size() > highAvgFiles' from the while condition
 
 			Vector<String> filesAtLastNode = m.node_file_map.get(node_file_keys.get(lastIndex));
 			String filetoCopy=null;
 			String nodetoCopyFrom=null;
 			for(String file: filesAtLastNode)
 			{
 				if(m.file_node_map.get(file).contains(firstKey))
 					continue;
 				filetoCopy = file;
 				Vector<String> nodeList = m.file_node_map.get(file);
 				for(String node: nodeList)
 				{
 					if(node != lastKey) {
 						nodetoCopyFrom = node;
 						break;
 					}
 				}
 				break;
 			}
 			if (filetoCopy == null)
 			{
 				System.out.println("Couldn't find a file which can be replicated");
 				break;
 			}
 			if (nodetoCopyFrom == null) {
 				System.out.println("Couldn't find a node from which file can be re-replicated/balanced");
 				break;
 			}
 			/*
 			Vector<String> cpmsgList=new Vector<String>();
 			cpmsgList.add("C");
 			cpmsgList.add(filetoCopy);
 			cpmsgList.add(filetoCopy);
 			cpmsgList.add(nodetoCopyFrom);
 
 			sendListMsg(cpmsgList, firstKey);
 			 */
 			sendCOPYMessage(firstKey, filetoCopy, filetoCopy, nodetoCopyFrom);
 
 			Vector<String> rmmsgList=new Vector<String>();
 			rmmsgList.add("R");
 			rmmsgList.add(filetoCopy);
 
 			sendListMsg(rmmsgList, lastKey);
 
 			m.file_node_map.get(filetoCopy).add(firstKey);
 			m.file_node_map.get(filetoCopy).remove(lastKey);
 			m.node_file_map.get(firstKey).add(filetoCopy);
 			m.node_file_map.get(lastKey).remove(filetoCopy);
 
 
 			if(m.node_file_map.get(firstKey).size() >= lowAvgFiles)
 			{
 				firstIndex=firstIndex+1;
 				if(firstIndex>=lastIndex)
 					break;
 				firstKey = node_file_keys.get(firstIndex); //TODO - does mod operation need to be done here?
 				if(m.node_file_map.get(lastKey).size() <= highAvgFiles)
 				{
 					lastIndex=lastIndex-1;
 					lastKey = node_file_keys.get(lastIndex); //same as TODO above
 				}
 				continue;
 			}
 			if(m.node_file_map.get(lastKey).size() <= highAvgFiles)
 			{
 				lastIndex=lastIndex-1;
 				lastKey = node_file_keys.get(lastIndex); //same as TODO above
 				continue;
 			}
 		}	
 		try {
 			WriteLog.writelog(m.myName, "balancefiles called, stage two done ");
 			//WriteLog.writelog(m.myName, "final node_file_keys - "+node_file_keys.toString());
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	
 	
 	private Vector<String> sort_checkReplies()
 	{
 		Vector<String> keys = new Vector<String>(checkReplies.keySet());
 		final HashMap<String, Integer> temp_checkReplies = checkReplies;
 		//List<String> tempKeys = (List<String>)keys;
 		//List<String> tempKeys = new ArrayList<String>(keys);
 
 		Collections.sort(keys, new Comparator<String>(){
 			public int compare(String firstkey, String secondkey){
 				//String firstkey = (String)first;
 				//String secondkey = (String)second;
 
 				return (temp_checkReplies.get(firstkey)-temp_checkReplies.get(secondkey));
 			}
 		});
 
 		return keys;  //TODO need to verify that sorting on tempkeys actually affects also keys
 	}
 
 
 
 	public void reformFileInfo()
 	{
 		// TODO - called when a node recieves R message and now i am the new master
 		// hence i need to reform the file replication info in the maps
 		// check rep_info_reformed after sending the req to all nodes
 		try {
 			WriteLog.writelog(m.myName,"I am the new Master now hence, will reform the ReplicationInfo- ");
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		Vector<String> repMsg = new Vector<String>();
 
 		repMsg.add("Q");
 		repMsg.add(m.myName);
 
 		for(String member: m.memberList)
 		{
 			sendListMsg(repMsg, member);
 			checkReplies.put(member, 0);
 		}
 		rep_info_reformed = false;
 		while(!rep_info_reformed)
 		{
 			Vector<String> sortedKeys = sort_checkReplies();
 			if (checkReplies.get(sortedKeys.firstElement()) == 0)
 				continue;
 			else
 				rep_info_reformed = true;
 		}
 
 
 	}
 
 	public void start()
 	{
 		FileServer.start();
 		//FileClient.start(); //need not start client here..will be started whenever required
 		Thread fr_thread = new Thread(this);
 		fr_thread.start();
 	}
 
 
 	/**
 	 * @param args
 	 */
 	public void run(){
 		// TODO Auto-generated method stub
 		/* start udp socket for listening to control messages
 		 * while loop for listening to udp socket
 		 * listen to messages based on whether you are a master
 		 * start transfer thread based on the control messages
 		 */
 		Vector<String> recvList=new Vector<String>();
 		try {
 			m.filerep_sock = new DatagramSocket(Machine.FILE_OPERATIONS_PORT);
 		} catch (SocketException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		while(true){
 			try {
 				//receive the list of strings from the udp socket
 				recvList = recvListMsg();
 				//System.out.println(recvList);
 
 				//need to review - instead of using local memberlist we can think of using central memberlist and using locks to synchronize
 				Vector<String> memberList = m.getMemberList();
 				WriteLog.printList2Log(m.myName, memberList);
 
 				if (m.master)
 				{
 					//need to take decision based on the recvMsg opcode
 					//System.out.println("filereplication: mastermode");
 					if (recvList.firstElement().equals("P"))
 					{
 						//System.out.println("filereplication PUT received"); 
 						//master receiving PUT
 						// send copy msg to primary machine and backup machine
 						if(m.file_node_map.containsKey(recvList.elementAt(2)) && recvList.lastElement().equalsIgnoreCase("append")){
 							//System.out.println("file already exists");
 							WriteLog.writelog(m.myName, "File already present, but PUT called in APPEND mode hence sending this to the nodes that already have this file");
 							Vector<String> tNodes = m.file_node_map.get(recvList.elementAt(2));
 							
 							for(String tNode : tNodes){
 								//sendCOPYMessage for append mode
 								sendCOPYMessage(tNode, recvList.elementAt(1), recvList.elementAt(2), recvList.elementAt(3), true);
 								//WriteLog.writelog(m.myName, "Copy msg: Src:" + recvList.elementAt(3) + " Dest: " 
 											//+ tNode + " rfile: " + recvList.elementAt(1) + "lfile: " + recvList.elementAt(2));
 							}
 							
 						} else if(m.file_node_map.containsKey(recvList.elementAt(2))) {
 							WriteLog.writelog(m.myName, "PUT command received for "+recvList.elementAt(2)+ "File, but file already exists in the file system!! hence ignoring PUT");
 						} else {
 							WriteLog.writelog(m.myName, "PUT command received for - lfile: "+recvList.elementAt(1)+", rfile: "+recvList.elementAt(2));
 							Vector<String> sortedKey = sort_node_file_map();
 								
 							//m.sendMsg(m.filerep_sock, sortedKey.firstElement(), , portN)
 							Vector<String> cpnodes = new Vector<String>();
 							/*Vector<String> cpMsg = new Vector<String>();
 								cpMsg.add("C");
 								cpMsg.add(recvList.elementAt(1));
 								cpMsg.add(recvList.elementAt(2));
 								cpMsg.add(recvList.elementAt(3));
 								sendListMsg(cpMsg, sortedKey.elementAt(0));
 							 */
 							sendCOPYMessage(sortedKey.elementAt(0), recvList.elementAt(1), recvList.elementAt(2), recvList.elementAt(3));
 							cpnodes.add(sortedKey.elementAt(0));
 							m.node_file_map.get(sortedKey.elementAt(0)).add(recvList.elementAt(2));
 							if(sortedKey.size()>1) 
 							{
 								//sendListMsg(cpMsg, sortedKey.elementAt(1));
 								sendCOPYMessage(sortedKey.elementAt(1), recvList.elementAt(1), recvList.elementAt(2), recvList.elementAt(3));
 								cpnodes.add(sortedKey.elementAt(1));
 								m.node_file_map.get(sortedKey.elementAt(1)).add(recvList.elementAt(2));
 							}
 
 							m.file_node_map.put(recvList.elementAt(2), cpnodes);
 
 							/*try {
 								WriteLog.writelog(m.myName, "node_file_map after PUT - "+m.node_file_map.toString());
 								WriteLog.writelog(m.myName, "file_node_map after PUT - "+m.file_node_map.toString());
 							} catch (IOException e) {
 								// TODO Auto-generated catch block
 								e.printStackTrace();
 							}*/
 						}
 
 					}
 					else if (recvList.firstElement().equals("G"))
 					{
 						// master receiving GET
 						// GET sdfc_file client_name
 						// find primary machine
 						// send setSource msg to primary machine
 						if(!m.file_node_map.containsKey(recvList.elementAt(1))){
 							System.out.println("file doesn.t exist");
 						} else {
 							//String primaryM = m.file_node_map.get(recvList.elementAt(1)).elementAt(0);
 							String FileID = recvList.elementAt(1);
 							Vector<String> ListNodes = m.file_node_map.get(FileID);
 							String RandomNode = ListNodes.elementAt((int)(Math.random() * ListNodes.size()));
 
 							
 							Vector<String> cpMsg = new Vector<String>();
 							cpMsg.add(RandomNode);
 							sendListMsg(cpMsg, recvList.elementAt(2));	
 						}
 					}
 					else if (recvList.firstElement().equals("D"))
 					{
 						// master receiving DELETE
 						// find primary machine
 						// send remove msg to primary machine and backup machine
 						Vector<String> storeMs = m.file_node_map.get(recvList.elementAt(1));
 
 						Vector<String> dMsg = new Vector<String>();
 						dMsg.add("R");
 						dMsg.add(recvList.elementAt(1));
 
 						for(String key : storeMs)
 						{
 							sendListMsg(dMsg, key);
 							m.node_file_map.get(key).remove(recvList.elementAt(1));
 						}
 						m.file_node_map.remove(recvList.elementAt(1));
 						/*try {
 							WriteLog.writelog(m.myName, "node_file_map after DELETE - "+m.node_file_map.toString());
 							WriteLog.writelog(m.myName, "file_node_map after DELETE - "+m.file_node_map.toString());
 						} catch (IOException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}*/
 					}
 					else if (recvList.firstElement().equals("I"))
 					{
 						// master receives answer to replication query	
 						try {
 							WriteLog.writelog(m.myName, "received QUERY_INFO from - "+recvList.elementAt(1));
 						} catch (IOException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 						Vector<String> repInfo = new Vector<String>();
 						for(Integer i=2; i<recvList.size(); i++)
 						{
 							if(!m.file_node_map.containsKey(recvList.get(i)))
 							{
 								Vector<String> tempNodeLst = new Vector<String>();
 								tempNodeLst.add(recvList.elementAt(1));
 								m.file_node_map.put(recvList.elementAt(i), tempNodeLst);
 							}
 							else
 								m.file_node_map.get(recvList.elementAt(i)).add(recvList.elementAt(1));
 						}
 						recvList.remove(0);
 						String nodeid = recvList.remove(0);
 						m.node_file_map.put(nodeid, recvList);
 
 						checkReplies.put(nodeid, 1);
 						try {
 							WriteLog.writelog(m.myName, "node_file_map after INFO_QUERY_RESP - "+m.node_file_map.toString());
 							WriteLog.writelog(m.myName, "file_node_map after INFO_QUERY_RESP - "+m.file_node_map.toString());
 						} catch (IOException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 
 					else if (recvList.firstElement().equals("SDFS_GET"))
 					{
 						// master receives answer to replication query
 						//SDFC_GET packet - Opcode + SDFS_FileID + SourceNodeID
 						String FileID=null;
 						try {
 							WriteLog.writelog(m.myName, "received SDFS_GET from - "+recvList.elementAt(1));
 						} catch (IOException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 
 						FileID = recvList.elementAt(1);
 						Vector<String> ListNodes = m.file_node_map.get(FileID);
 						String RandomNode = ListNodes.elementAt((int)(Math.random() * ListNodes.size()));
 
 						if(!m.file_node_map.get(FileID).contains(recvList.elementAt(2))) {
 							sendCOPYMessage(recvList.elementAt(2), FileID, FileID, RandomNode);
 
 							m.file_node_map.get(FileID).add(RandomNode);
 							m.node_file_map.get(RandomNode).add(FileID);
 
 
 							/*try {
 								WriteLog.writelog(m.myName, "node_file_map after SDFS_GET - "+m.node_file_map.toString());
 								WriteLog.writelog(m.myName, "file_node_map after SDFS_GET - "+m.file_node_map.toString());
 							} catch (IOException e) {
 								// TODO Auto-generated catch block
 								e.printStackTrace();
 							}*/
 						}
 					}
 
 					else if (recvList.firstElement().equalsIgnoreCase("MAPLE"))
 					{
 						/*
 						 * Maple sdfs_exe sdfs_prefix sdfs_files 
 						 */
 
 						String sdfs_exe = recvList.elementAt(1);
 						String sdfs_prefix = recvList.elementAt(2);
 						int length = recvList.size();
 						Vector<String> sdfs_files = new Vector<String>(recvList.subList(3, length));
 						//sdfs_files.add(recvList.elementAt(length));
 
 						//make sure that all the sdfc_files are already existing in file_system
 						for (String tfile: sdfs_files) {
 							if(!m.file_node_map.containsKey(tfile)) {
 								WriteLog.writelog(m.myName, "file_node_map doesn't contain "+ tfile);
 								System.exit(-1);
 							}
 						}
 
 						WriteLog.writelog(m.myName, "Received MAPLE commnd from client");
 
 						
 						//TODO
 						//m.mapleJuiceListener.processMapleCommand(sdfs_exe, sdfs_files, sdfs_prefix);
 						
 						Runnable mapleWorker = new MapleJuiceWorker(m, sdfs_exe, sdfs_files, sdfs_prefix);
 						Thread mapleWorkerThread = new Thread(mapleWorker);
 						mapleWorkerThread.start();
 					}
 
 					else if (recvList.firstElement().equalsIgnoreCase("JUICE"))
 					{
 						/*
 						 * JUICE sdfs_exe num_juices sdfs_prefix sdfs_file 
 						 */
 
 						String sdfs_exe = recvList.elementAt(1);
 						String no_juices = recvList.elementAt(2);
 						String sdfs_prefix = recvList.elementAt(3);
 						String sdfs_file = recvList.elementAt(4);
 
 						Integer no_reduce = Integer.valueOf(no_juices);
 
 						WriteLog.writelog(m.myName, "Received JUICE commnd from client");
 						//make sure that all the sdfc_files are already existing in file_system
 
 						
 						//TODO
 						//m.mapleJuiceListener.processJuiceCommand(sdfs_exe, no_reduce, sdfs_file, sdfs_prefix);
 
 						Runnable juiceWorker = new MapleJuiceWorker(m, sdfs_exe, no_reduce, sdfs_file, sdfs_prefix);
 						Thread juiceWorkerThread = new Thread(juiceWorker);
 						juiceWorkerThread.start();
 					}
 				}
 
 
 				if (recvList.firstElement().equals("C"))
 				{
 					//machine receiving COPY
 					//COPY file_name sdfs_file node_id 
 					String copyFN = recvList.elementAt(2);
 					String serverIP = recvList.elementAt(3);//TODO get client ip
 
 					if(recvList.lastElement().equalsIgnoreCase("append")) {
 						try {
 							WriteLog.writelog(m.myName, "received COPY, for "+copyFN+"with the APPEND mode set");
 						} catch (IOException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 						Runnable runnableClient = new FileTransferClient(copyFN, recvList.elementAt(1), serverIP, true);
 						Thread thread = new Thread(runnableClient);
 						thread.start();
 	
 					}
 					else {
 						/*try {
 							//WriteLog.writelog(m.myName, "received COPY, will now start FileTransferClient");
 							WriteLog.writelog(m.myName, "myFileList before COPY - "+m.myFileList.toString());
 						} catch (IOException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}*/
 
 						Runnable runnableClient = new FileTransferClient(m, copyFN, recvList.elementAt(1), serverIP);
 						Thread thread = new Thread(runnableClient);
 						thread.start();
 
 						/*try {
 						thread.wait();
 					} catch (InterruptedException e1) {
 						// TODO Auto-generated catch block
 						e1.printStackTrace();
 					}*/
 						/*try {
 							WriteLog.writelog(m.myName, "Received COPY message, copy started for "+copyFN);
 						} catch (IOException e3) {
 							// TODO Auto-generated catch block
 							e3.printStackTrace();
 						}*/
 						//synchronized(runnableClient) {
 						//	m.myFileList.add(copyFN);
 						//}
 						/*try {
 							WriteLog.writelog(m.myName, "COPY completed for "+copyFN);
 						} catch (IOException e3) {
 							// TODO Auto-generated catch block
 							e3.printStackTrace();
 						}*/
 						//crucial change...will keep waiting here till the myFileList gets populated with the fileID
 						//while(!m.myFileList.contains(copyFN));
 					}
 				}
 				else if (recvList.firstElement().equals("R"))
 				{
 					// machine receiving REM
 					String removeF = recvList.elementAt(1);//TODO get remove file name 
 					File f = new File(removeF);
 					f.delete();
 					m.myFileList.remove(removeF);
 					try {
 						WriteLog.writelog(m.myName, "myFileList after REMOVE - "+m.myFileList.toString());
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 
 				else if (recvList.firstElement().equals("Q"))
 				{
 					// machine receiving REPLICATION_INFO_QUERY
 					try {
 						WriteLog.writelog(m.myName, "received QUERY from new Master - "+recvList.elementAt(1));
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					Vector<String> qMsg = new Vector<String>();
 					qMsg.add("I");
 					qMsg.add(m.myName);
 					for(String f: m.myFileList)
 						qMsg.add(f);
 
 					sendListMsg(qMsg, recvList.elementAt(1));
 				}
 
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}				
 	}
 }
