 package orange;
 
 import java.util.LinkedList;
 
 import peersim.config.Configuration;
 import peersim.config.FastConfig;
 import peersim.core.CommonState;
 import peersim.edsim.*;
 import peersim.cdsim.*;
 import peersim.transport.*;
 import peersim.core.Node;
 
 /**
  * 
  *
  * @author Fatima De Villa, Janeri Ongcol, Bryan Tan
  * @version 1.0
  */ 
 
 //protocol
 
 public class Gcp2pProtocol implements Overlay, CDProtocol, EDProtocol{
 	
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 	public static final int maxLandmarkRTT = 70;
 	public static final int minLandmarkRTT = 30;
 	
 	/**
 	 * Node Tags
 	 */
 	public static final int CDNTag = 0;
 	public static final int SuperPeerTag = 1;
 	public static final int RegularTag = 2;
 	
 	/**
 	 * maximum number of bins inside a CDN group
 	 */
 	public static final int maxBins = 6;
 	// ------------------------------------------------------------------------
 	// Parameters  
 	// ------------------------------------------------------------------------
 	/**
 	 * String name of the parameter, assigned to pid
 	 */
 	private static final String PAR_PROT = "protocol";
 	/**
 	 * String name of the parameter, assigned to tid
 	 */
 	private static final String PAR_TRANS = "transport";
 	/**
 	 * String name of the parameter, assigned to rid
 	 */
 	private static final String PAR_NET = "network";
 	/**
 	 * String name of the parameter, assigned to maxClients
 	 */
 	private static final String PAR_MAXCLIENTS = "maxclients";
 	/**
 	 * String name of the parameter, assigned to category
 	 */
 	private static final String PAR_CATEGORY = "category";
 	// ------------------------------------------------------------------------
 	// Static Fields
 	// ------------------------------------------------------------------------
 	/**
 	 *  Protocol identifier, obtained from config property {@link #PAR_PROT}. 
 	 **/
 	private static int pid;	
 	/**
 	 *  Transport Protocol identifier, obtained from config property {@link #PAR_TRANS}. 
 	 **/
 	private static int tid;	
 	/**
 	 *  Network Protocol identifier, obtained from config property {@link #PAR_TRANS}. 
 	 **/
 	private static int nid;	
 	
 	/**
 	 * max number of possible clients for CDNs and SuperPeers, 
 	 * obtained from config property {@link #PAR_MAXCLIENTS}. 
 	 **/
 	public static int maxClients; 		
 	
 	/***
 	 * The nodes corresponding to the 3 CDNs in the set-up
 	 */
 	public static Node CDN1;
 	public static Node CDN2;
 	public static Node CDN3;	
 	
 	/**
 	*GLOBALS
 	*/
 	int nodeTag;			// Type of node: 0 - CDN, 1 - Superpeer, 2 - Regular
 	Node connectedCDN;		// the CDN node it is closest/connected to
 	int CID;				// Which CDN range [1, 3]
 	int cdnRTT;				// RTT of a client to its CDN; 
 	int landmark1RTT;		// RTT to landmark 1
 	int landmark2RTT;		// RTT to landmark 2
 	int landmark3RTT;		// RTT to landmark 3
 	int binID;				// which bin in the CDN group the node belongs to	
 	int uploadSpd;			// maximum upload capacity
 	int downloadSpd;		// maximum download capacity
 	int usedUploadSpd;		// used upload speed
 	int uploadSpdBuffer;	// reserved upload spd for peers requesting connection, to be alloted when the peer accepts the upload spd
 	int usedDownloadSpd;	// used download speed
 	int videoID;			// ID of the video it is streaming
 	int videoSize;			// size of the video it is watching
 	int streamedVideoSize;	// size already streamed
 	int categoryID;			// category of the video it is streaming
 	int maxBinSize;			// max number of peers inside a bin (same as maxClients) 
 	int numClients;			// number of clients
 	int[] videoList;			// list of videos
 	int[] clientRTT;		// RTT of clients
 	int[] bestRTT;			// least RTT
 	int category;			// number of categories
 	int[] clientWatching;	// video the client is watching
 	int SPreply = 0;			// number of SP that sent YOUR_PEERS
 	int[] streamingSameVidPerBin = new int[maxBins];
 	int highestStreamingSameVid = 0;
 	long startTime;			// Time the node was initialized
 	long elapsedTime;		// Time it took from initialization until completion of stream
 	int numCandidates;
 	int candidateReplies = 0;
 	int uploaded = 0;
 	boolean firstConnect = false;
 	boolean firstPlayback = false;
 	boolean startTimeLogged = false;
 	long firstPlay;
 	boolean binned = false;
 	int numConnectionsAttempted = 0;
 	int numConnectionsRejected = 0;
 	Node potentialSource = null;
 	int numPotentialPeers = 0;
 	int spSent = 0;
 	
 	int[][] indexPerCategory; // index of peers per category i.e. indexPerCategory[0][1] = 5, then clientList[5] watches a video with category 0
 	Node[] superPeerList;	// list of SuperPeers
 	Node[] clientList;		// applicable to CDN and SuperPeer
 	Node[] peerList;		// list of peers the node uploads to
 	int[] peerSpdAlloted;		// speed alloted to peers
 	int numPeers = 0;			// number of peers it contributes to
 	int maxConnections = 300;
 	int activeLeechers = 0;
 	int maxSpdPerVid = 400;
 	Node[] sourcePeerList;	// list of peers that contribute to the node
 	Node[] candidatePeers;	// sent by the SuperPeer to a regular peer
 	int numSource;			// number of source peers that contribute to the node
 	int binSize[]; //binSize[i] contains the number of peers inside bin i
 	Node binList[][]; //binList[i] returns the list peers inside bin i
 	int binWatchList[][]; //binWatchList[i][j] returns the what video peer j of bin i is watching
 	int binIndexPerCategory[][][]; // CDN's copy of indexPerCategory, binIndexPerCategory[0][1][2] = 5 means that binList[0][5] watches a video with category 1
 	boolean startedStreaming = false; // true if the node is already streaming
 	boolean doneStreaming = false;	// true if videoSize<= streamedVideoSize
 	Node[] otherSP;				// 5 other superPeers
 	boolean left = false;
 	int videoSpdAlloted[];
 	LinkedList<Node> peerPool = new LinkedList<Node>();
 	int essentialSize = 168;
 	int speedLimit = 1024 - essentialSize;
 	// ------------------------------------------------------------------------
 	// Constructor
 	// ------------------------------------------------------------------------
 	public Gcp2pProtocol(String prefix){
 		maxClients = Configuration.getInt(prefix + "." + PAR_MAXCLIENTS);
 		tid = Configuration.getPid(prefix + "." + PAR_TRANS);
 		nid = Configuration.getPid(prefix + "." + PAR_NET);
 		pid = Configuration.getPid(prefix + "." + PAR_PROT);
 		category = Configuration.getInt(prefix + "." + PAR_CATEGORY);
 	}
 	
 	public Object clone(){
 		Gcp2pProtocol prot = null;
 		try{
 			prot = (Gcp2pProtocol) super.clone();
 		}catch( CloneNotSupportedException e ) {} // never happens
 		return prot;
 	}
 		
 	//cycle chuchu, ewan ko kung gagawin natin, feeling ko hindi
 	
 	public void nextCycle( Node node, int pid ){
 
 		Gcp2pProtocol prot = (Gcp2pProtocol) node.getProtocol(pid);
 		//System.out.println(node.getIndex()+" numPeers " + numPeers +": Network.size = "+Network.size());
 		Router router = (Router)node.getProtocol(nid);
 		router.nextCycle(node, pid);
 		if(!left){
 			for(int i = 0; i < numPeers; i++){
 				// TODO check
 				int given = 0;
 				int toGive = 0;
 				while (given < peerSpdAlloted[i]){
 					if (peerSpdAlloted[i] - given > speedLimit){
 						toGive = essentialSize + speedLimit;
 					}
 					else {
 						toGive = essentialSize + peerSpdAlloted[i] - given;
 					}
 					prot.sendMsg(new OrangeMessage(OrangeMessage.UPLOAD, node, peerList[i], essentialSize + speedLimit, peerSpdAlloted[i]));
 					given += toGive;
 				}
 				//prot.sendMsg(new OrangeMessage(OrangeMessage.UPLOAD, node, peerList[i], 0, peerSpdAlloted[i]));
 				uploaded += peerSpdAlloted[i];
 				if(nodeTag != 0)
 				if(uploaded > 4*videoSize && nodeTag!=0){
 					startedStreaming = false;
 					left = true;
 					if(nodeTag == SuperPeerTag){
 						// 171 = 168 + 3(3 bits lang since 6 values)
 						prot.sendMsg(new OrangeMessage(OrangeMessage.GOODBYE_AS_SP, node, connectedCDN, 171, prot.binID));
 					}
 					else 
 						// 171 = 168 + 3(3 bits lang since 6 values)
 						prot.sendMsg(new OrangeMessage(OrangeMessage.GOODBYE_AS_NORMAL, node, connectedCDN, 171, prot.binID));
 					
 					for(int j = 0; j < numPeers; j++){
 						// 232 = 168 + 64 (int kasi)
 						prot.sendMsg(new OrangeMessage(OrangeMessage.GOODBYE_AS_SOURCE, node, peerList[j], 232, peerSpdAlloted[j]));
 					}
 					
 					break;
 				}				
 			}
 			//if(nodeTag == 0)
 				//System.out.println("nagbigay si cdn");
 			if(nodeTag != 0){
 				if(usedDownloadSpd < downloadSpd){
 					// 171 = 168 + 3(3 bits lang since 6 values)
 					prot.sendMsg(new OrangeMessage(OrangeMessage.GET_SUPERPEER_2, node, prot.connectedCDN, 171, binID));
 				}
 				
 			}
 		}
 		
 	}
 	
 	//eto yung magproprocess ng messages
 	
 	public void processEvent( Node node, int pid, Object event ) {
 		Gcp2pProtocol p = (Gcp2pProtocol) node.getProtocol(pid);
 		
 		OrangeMessage omsg = (OrangeMessage)event;
 		//System.out.println(aem.msgType);
 		//CDN messages
 		//System.out.println(aem.sender.getIndex());
 		if (nodeTag == 0){
 			/**
 			*	message received requesting superpeer
 			*/
 			
 			if (omsg.msgType == OrangeMessage.GET_SUPERPEER){		//new peer requests for its bin's SP. aem.data is the binID
 				Gcp2pProtocol prot = (Gcp2pProtocol) omsg.sender.getProtocol(pid);
 				
 				int tempRTT = prot.cdnRTT;
 				addClient(omsg.sender);
 				addToBin(omsg.data, omsg.sender);
 				Node sp;
 				if(tempRTT < bestRTT[omsg.data]){	// if the new peer's RTT is lower, make the SP_var to be sent null. this will force the new peer to send a GET_MY_CLIENT
 					sp = null;
 				}
 				else sp = superPeerList[omsg.data];
 				if(sp == null)						// update bestRTT
 					bestRTT[omsg.data] = tempRTT;
 
 				// 296 = 168 + IP(128)
 				prot.sendMsg(new OrangeMessage(OrangeMessage.YOUR_SUPERPEER, node, omsg.sender, 296, sp));
 			}
 			else if (omsg.msgType == OrangeMessage.GET_SUPERPEER_2){
 				// 296 = 168 + IP(128)
 				p.sendMsg(new OrangeMessage(OrangeMessage.YOUR_SUPERPEER, node, omsg.sender, 296, superPeerList[omsg.data]));		
 			}
 			else if (omsg.msgType == OrangeMessage.DO_YOU_HAVE_THIS){			// this won't happen
 				/**
 				*	message received asking if the CDN has the video
 				*/
 				int i;
 				int reply = OrangeMessage.I_DONT_HAVE_IT;
 				for(i = 0; i<videoList.length; i++){ // check if the requested video's id is in the list
 					if(omsg.data == videoList[i]){
 						reply = OrangeMessage.I_HAVE_IT;
 						break;
 						}
 				}
 				Node temp = null;
 				// 296 = 168 + IP(128) lagyan na lang rin kahit hindi nangyayari. haha
 				p.sendMsg(new OrangeMessage(reply, node, omsg.sender, 296, temp));		
 			}
 			else if (omsg.msgType == OrangeMessage.GET_MY_CLIENTS){		// a new SuperPeer requests for its clients. aem.data is the binID
 				/**
 				*	A peer asks for the list of clients in a certain bin
 				*	a peer will only request this when the YOUR_SUPERPEER message is null
 				*/
 				//System.out.println("NEW SP: "+ aem.data);
 				Node[] temp = binList[omsg.data];
 				int[][] tempIndex = binIndexPerCategory[omsg.data];
 				int [] tempWatching = binWatchList[omsg.data];
 				int tempSize = binSize[omsg.data];
 				// TODO size of the message
 				p.sendMsg(new OrangeMessage(OrangeMessage.YOUR_CLIENTS, node, omsg.sender, 0, temp, tempWatching, tempIndex, tempSize));		
 				
 				if(superPeerList[omsg.data]!=null)		// send this to notify the old SP that he is fired
 					// 296 = 168 + 128bits for the IP of the new SP
 					p.sendMsg(new OrangeMessage(OrangeMessage.FIRED, node, superPeerList[omsg.data], 296, omsg.sender));		
 						
 				for(int i = 0; i<5; i++){
 					if(superPeerList[i] != null)
 						// 424 = 168 + 128bits for IP of previous SP + 128 bits for the new SP
 						p.sendMsg(new OrangeMessage(OrangeMessage.UPDATE_SP, node, superPeerList[i], 424,superPeerList[omsg.data], omsg.sender));		
 				}
 				superPeerList[omsg.data] = omsg.sender;	// make the sender the SP				
 			}
 			else if (omsg.msgType == OrangeMessage.GOODBYE_AS_SP){
 				for (int i = 0; i < 6; i++){
 					//remove as client
 					for(int j = 0; j < maxClients; j++){
 						if(omsg.sender.equals(binList[i][j])){
 							binList[i][j] = null;
 							break;
 						}
 					}					
 				}
 				
 				//remove as SP and hire a new one
 				for(int i = 0; i < 6; i++){
 					if(omsg.sender.equals(superPeerList[i])){
 						superPeerList[i] = null;
 						bestRTT[i] = 1000;
 						Node currentBest = binList[i][0];
 						for(int j = 0; j < maxClients; j++){
 							if(binList[i][j] != null){
 								Gcp2pProtocol prot3 = (Gcp2pProtocol) binList[i][j].getProtocol(pid);
 								if(bestRTT[i] > prot3.cdnRTT){
 									currentBest = binList[i][j];
 									bestRTT[i] = prot3.cdnRTT;
 								}
 							}
 						}
 						superPeerList[i] = currentBest;
 						Node sp = null;
 						// 424 = 168 + 128 bits IP for SP + 128 bits for null IP
 						p.sendMsg(new OrangeMessage(OrangeMessage.YOUR_SUPERPEER, node, omsg.sender, 424, superPeerList[i], sp));		
 						break;
 					}
 					
 				}
 				
 			}
 			else if (omsg.msgType == OrangeMessage.GOODBYE_AS_NORMAL){
 				for (int i = 0; i < 6; i++){
 					//remove as client
 					for(int j = 0; j < maxClients; j++){
 						if(omsg.sender.equals(binList[i][j])){
 							binList[i][j] = null;							
 							break;
 						}
 					}				
 				}				
 			}			
 		}
 		else if (nodeTag == 1){
 			if (omsg.msgType == OrangeMessage.REQUEST_PEERS_FROM_THIS_BIN ){	// a Peer requests a SP for peers. aem.data0 - categoryID. aem.data - videoID
 					Gcp2pProtocol prot = (Gcp2pProtocol)omsg.sender.getProtocol(pid);
 					//System.out.println("REQUEST: Category = "+aem.data0 +": SuperPeer Index = "+ node.getIndex() + ": Sender Index = "+aem.sender.getIndex()+": SP numClients = "+numClients);
 					int temp[] = indexPerCategory[omsg.data0];		// get the list of indices of the peers watching a certain category
 					Node[] peers = new Node[maxClients];
 					//System.out.println("pumasok");
 					/*
 					int i = 0;
 					int j = 0;
 					while(i<numClients-1 && temp[i] >= 0 ){							// get the nodes watching the video requested
 						//System.out.println(temp[i]);
 						if(clientWatching[temp[i]] == omsg.data){
 							peers[j] = clientList[temp[i]];
 							j++;
 						}
 						i++;
 						//System.out.println("Why?");
 					}
 					// TODO size of the message
 					p.sendMsg(new OrangeMessage(OrangeMessage.YOUR_PEERS, node, omsg.sender, 0, peers, j));		
 					*/
 					LinkedList<Node> tempList = new LinkedList<Node>();
 					int num = 0;
 					for (int i = 0; i < numClients; i++){
 						if(temp[i]>=0)
 						if (clientWatching[temp[i]] == omsg.data){
 							tempList.add(clientList[temp[i]]);
 							num++;
 							if(num == 6){
 								// 936 = 168 + 128(IP size)*6
 								sendMsg(new OrangeMessage(OrangeMessage.INCOMPLETE_PEER_LIST, node, omsg.sender, 936, tempList));
 								num = 0;
 								// TODO
 								tempList.removeAll(tempList);
 							}
 						}
 					}
 					sendMsg(new OrangeMessage(OrangeMessage.COMPLETE_PEER_LIST, node, omsg.sender, 0, tempList));
 					if(prot.binID == binID && !prot.binned){
 						clientList[numClients] = omsg.sender;
 						clientWatching[numClients] = omsg.data;
 						for(int itr = 0; itr < maxClients; itr++)
 							if(indexPerCategory[prot.categoryID][itr] == -1){
 								indexPerCategory[prot.categoryID][itr] = numClients;
 								numClients++;
 								break;
 							}
 						prot.binned = true;
 						
 					}
 					
 				}
 				
 				else if (omsg.msgType == OrangeMessage.REQUEST_PEERS_FROM_OTHER_BINS){	// the peers returned in REQUEST_PEERS_FROM_THIS_BIN is empty
 					for(int i= 0; i<5; i++){							// send requests to other SP and define the new peer as the sender. This will make the other SP
 																		// to send their Reply to the new peer
 						if(otherSP[i] != null)
 							// 232 = 168 + 32 bits for category + 32 bits for videoID
 							//p.sendMsg(new OrangeMessage(OrangeMessage.REQUEST_PEERS_FROM_THIS_BIN, node, otherSP[i], 0, omsg.data0, omsg.data));		
 							sendMsg(new OrangeMessage(OrangeMessage.ANOTHER_BIN_PEER_REQUEST, omsg.sender, otherSP[i], 0, omsg.data0,omsg.data));
 						else 
 							// 232 = 168 + 64bits for number of peers even though laging zero yung binibigay dito
 							// e kailangan pa ring 64 bits para consistent yung data na natatanggap nung recepient
 							//p.sendMsg(new OrangeMessage(OrangeMessage.YOUR_PEERS, node, omsg.sender, 0, null, 0));		
 							sendMsg(new OrangeMessage(OrangeMessage.OTHER_BIN_NUMBER_OF_PEERS, node, omsg.sender, 0, 0));
 					}
 				}
 				else if (omsg.msgType == OrangeMessage.ANOTHER_BIN_PEER_REQUEST){
 					// bilangin ilang peers
 					// send OTHER_BIN_NUMBER_OF_PEERS
 					//System.out.println("pumasok");
 					int num = 0;
 					int temp[] = indexPerCategory[omsg.data0];		// get the list of indices of the peers watching a certain category
 					for (int i = 0; i < numClients; i++){
 						if(temp[i] >= 0)
 						if (clientWatching[temp[i]] == omsg.data)
 							num++;
 					}
 					// 232 = 168 + 64 bits for the number of peers
 					sendMsg(new OrangeMessage(OrangeMessage.OTHER_BIN_NUMBER_OF_PEERS, node, omsg.sender, 232, num));	
 					
 				}
 				else if (omsg.msgType == OrangeMessage.UPDATE_SP){
 					for(int i = 0; i<5; i++){
 						Node temp = omsg.node1;
 						if(otherSP[i]==(temp)){
 							otherSP[i] = omsg.node2;
 							//System.out.println("UPDATED: "+ node.getIndex());
 							break;
 						}
 					}
 					
 				}
 				else if (omsg.msgType == OrangeMessage.FIRED){									// the peer is not a SP anymore
 					nodeTag = 2;
 				}
 				
 
 		}
 		
 		// Node is a Regular Peer
 			if(omsg.msgType == OrangeMessage.UPLOAD){					// a chunk is delivered. aem.data is the chunk
 				//System.out.println("UPLOADED TO "+node.getIndex());
 				streamedVideoSize = streamedVideoSize + omsg.data;
 				if(!firstPlayback && streamedVideoSize >= 400) {
 					firstPlay = System.currentTimeMillis()-startTime - getTimeElapsed();
 					firstPlayback = true;
 				}
 				if(streamedVideoSize>= videoSize){		// check if done streaming. if yes, send GOODBYE messages
 					for(int i = 0; i< numSource; i++){
 						// 168 lang since message type lang kailangan
 						p.sendMsg(new OrangeMessage(OrangeMessage.GOODBYE, node, sourcePeerList[i], 168, 0));		
 					}
 					doneStreaming = true;
 				}
 			}
 			else if (omsg.msgType == OrangeMessage.YOUR_PEERS){			// The peerList was sent by the SP
 				int i = 0;
 					if(omsg.data == 0){						// if the list is empty, request from other bins
 						if(SPreply == 0){					// if SPreply = 0 means that it was sent from the peer's SP
 							// TODO size of the message
 							p.sendMsg(new OrangeMessage(OrangeMessage.REQUEST_PEERS_FROM_OTHER_BINS, node, omsg.sender, 0, categoryID, videoID));				
 						}
 						//System.out.println("Nangyari ba?");
 					}
 					else {									// the list is not empty
 						if(SPreply == 0){					// if SPreply = 0 then the peers is from it's bin
 							while(usedDownloadSpd < downloadSpd && i < omsg.data){
 								if(omsg.nodeList[i]!= null) {
 									// TODO size of the message
 									p.sendMsg(new OrangeMessage(OrangeMessage.CONNECT, node, omsg.nodeList[i], 0, downloadSpd - usedDownloadSpd));		
 									numConnectionsAttempted++;
 								}
 								i++;
 							}
 						}
 						else {								// if the peers is not from it's bin. check if the number of peers is higher than the current candidate
 							if(omsg.data > highestStreamingSameVid){
 								candidatePeers = omsg.nodeList;
 								highestStreamingSameVid = omsg.data;
 							}
 							
 						}
 					}
 					SPreply++;
 					i = 0;
 					if(SPreply == maxBins){						// means that all the bins have sent its peers
 						//System.out.println(highestStreamingSameVid);
 						while(usedDownloadSpd < downloadSpd && i < highestStreamingSameVid){	//send CONNECT messages to the bin with the highest number of peers
 							if(candidatePeers[i]!=(null))
 								// TODO size of the message
 								p.sendMsg(new OrangeMessage(OrangeMessage.CONNECT, node, candidatePeers[i], 0, downloadSpd - usedDownloadSpd));		
 								numConnectionsAttempted++;
 								i++;
 							}
 						if(highestStreamingSameVid == 0){
 							// TODO size of the message
 							p.sendMsg(new OrangeMessage(OrangeMessage.CONNECT, node, connectedCDN, 0, downloadSpd - usedDownloadSpd));		
 							numConnectionsAttempted++;				
 						}
 						//i++;
 						SPreply = 0;
 						highestStreamingSameVid = 0;
 					}
 			
 			}
 			else if (omsg.msgType == OrangeMessage.INCOMPLETE_PEER_LIST){
 				while (!omsg.linkedNodeList.isEmpty()){
 					peerPool.add(omsg.linkedNodeList.remove());
 				}
 			}
 			else if (omsg.msgType == OrangeMessage.COMPLETE_PEER_LIST){
 				boolean connectedAtleastOnce = false;
 				while (!omsg.linkedNodeList.isEmpty()){
 					if(omsg.linkedNodeList.peek()!=null)
 						peerPool.add(omsg.linkedNodeList.remove());
 					else omsg.linkedNodeList.remove();
 				}
 				Node[] list = new Node[peerPool.size()];
 				for(int i = 0; i < peerPool.size(); i++){
 					list[i] = peerPool.get(i);;
 				}
				peerPool.removeAll(peerPool);
 				int[] RTTs = randomRTTs(list);
 				for (int i = 0; i < list.length -1; i++)
 					for (int j = 0; j < list.length - 1; j++){
 						if(RTTs[j] > RTTs[j+1]){
 							int tempInt = RTTs[j];
 							RTTs[j] = RTTs[j+1];
 							RTTs[j+1] = tempInt;
 							Node tempNode = list[j];
 							list[j] = list[j+1];
 							list[j+1] = tempNode;
 						}
 					}
 				if (peerPool.isEmpty() && spSent == 5){
 					// 232 = 168 + 64bits for speed
 					sendMsg(new OrangeMessage(OrangeMessage.CONNECT, node, connectedCDN, 232, downloadSpd - usedDownloadSpd));
 					//System.out.println("wala");
 					numConnectionsAttempted++;
 					spSent = 0;
 				}
 				else if (peerPool.isEmpty() && spSent == 0){
 					//232 = 168 + 32 bits for category + 32bits for videoID
 					sendMsg(new OrangeMessage(OrangeMessage.REQUEST_PEERS_FROM_OTHER_BINS, node, omsg.sender, 232, categoryID, videoID));				
 				}
 				/*while(usedDownloadSpd < downloadSpd && !peerPool.isEmpty()){
 						// TODO size of the message
 					if(peerPool.getFirst() != null){
 						// 232 = 168 + 64 bits for speed
 						p.sendMsg(new OrangeMessage(OrangeMessage.CONNECT, node, peerPool.remove(), 232, downloadSpd - usedDownloadSpd));		
 						numConnectionsAttempted++;
 						connectedAtleastOnce = true;
 					}
 					else
 						peerPool.remove();
 				}
 				if (!connectedAtleastOnce){
 					if(spSent == 5){
 						//232 = 168 + 64bits for speed
 						sendMsg(new OrangeMessage(OrangeMessage.CONNECT, node, connectedCDN, 232, downloadSpd - usedDownloadSpd));
 						numConnectionsAttempted++;
 					}
 				}*/
 				int iterate = list.length;
 				if(iterate>100)
 					iterate = 100;
 				for (int i = 0; i < iterate && usedDownloadSpd < downloadSpd; i++){
 					if(list[i]!=null){
 						p.sendMsg(new OrangeMessage(OrangeMessage.CONNECT, node, list[i], 232, downloadSpd - usedDownloadSpd));		
 						numConnectionsAttempted++;
 						connectedAtleastOnce = true;
 					}
 				}
 				if (!connectedAtleastOnce){
 					if(spSent == 5){
 						//232 = 168 + 64bits for speed
 						sendMsg(new OrangeMessage(OrangeMessage.CONNECT, node, connectedCDN, 232, downloadSpd - usedDownloadSpd));
 						numConnectionsAttempted++;
 					}
 				}
 			}
 			else if (omsg.msgType == OrangeMessage.OTHER_BIN_NUMBER_OF_PEERS){
 				spSent++;
 				if(omsg.data > numPotentialPeers){
 					numPotentialPeers = omsg.data;
 					potentialSource = omsg.sender;
 				}
 				if(spSent == 5 ){
 					if(potentialSource!= null){
 						//232 = 168 + 32 bits for category + 32 bits for videoID
 						sendMsg(new OrangeMessage(OrangeMessage.REQUEST_PEERS_FROM_THIS_BIN, node, potentialSource, 232, categoryID, videoID));
 					}
 					else{
 						// 232 = 168 + 64bits for requested speed
 						sendMsg(new OrangeMessage(OrangeMessage.CONNECT, node, connectedCDN, 232, downloadSpd - usedDownloadSpd));
 					}
 					potentialSource = null;
 					spSent = 0;
 				}
 				
 			}
 			else if (omsg.msgType == OrangeMessage.UPLOAD_SPEED_THAT_CAN_BE_GIVEN){	// reply to the CONNECT request. aem.data is the maximum upload spd that can be given
 					int spdAvail = downloadSpd - usedDownloadSpd;
 					int tobeAccepted = 0;
 					if (spdAvail > 0){									// check if the available download spd is not yet maxed
 						if(!firstConnect) {
 							setTimeElapsed();
 							firstConnect = true;
 						}
 						
 						if(spdAvail >= omsg.data)						// if the available download speed is equal or greater than the proposed upload spd, get it all
 							tobeAccepted = omsg.data;
 						else tobeAccepted = spdAvail;					// if not, get only the available download spd
 						usedDownloadSpd = usedDownloadSpd + tobeAccepted;
 						// 296 = 168 + 64bits for requested speed + 64bits for speed to be accepted
 						p.sendMsg(new OrangeMessage(OrangeMessage.ACCEPT_SPEED, node, omsg.sender, 296, omsg.data, tobeAccepted));		
 						
 						//System.out.println(numSource);
 						sourcePeerList[numSource] = omsg.sender;
 						numSource++;
 						//startedStreaming = true;
 					}
 					else {												// if the download spd is maxed, send a REJECT message
 						// 232 = 168 + 64bits for rejected speed
 						p.sendMsg(new OrangeMessage(OrangeMessage.REJECT_SPEED, node, omsg.sender, 232, omsg.data));		
 					}
 					
 					
 			}
 			else if (omsg.msgType == OrangeMessage.ACCEPT_SPEED){						// reply to the proposed upload spd. aem.date0 is the proposed upload spd. aem.data is the acceoted sod
 				peerList[numPeers] = omsg.sender;
 				peerSpdAlloted[numPeers] = omsg.data;
 				if(nodeTag == 0){
 					Gcp2pProtocol prot = (Gcp2pProtocol)omsg.sender.getProtocol(pid);
 					videoSpdAlloted[prot.videoID] += omsg.data;
 				}
 				numPeers++;
 				activeLeechers++;
 				//System.out.println(node.getIndex() +" Updated numPeers " +numPeers);
 				uploadSpdBuffer = uploadSpdBuffer - omsg.data0;
 				usedUploadSpd = usedUploadSpd + omsg.data;
 				startedStreaming = true;
 				//System.out.println("naging true?");
 				//if(CID == 0)
 					//System.out.println("CDN CONNECTED");
 			}
 			else if (omsg.msgType == OrangeMessage.REJECT_SPEED){						// if the upload spd is rejected, remove the reserved uploadSpd in the uploadSpdBuffer
 				uploadSpdBuffer = uploadSpdBuffer - omsg.data;
 				startedStreaming = true;
 			}
 			else if (omsg.msgType == OrangeMessage.CONNECT){								// a peer is requesting for upload Spd
 				int spdAvail = uploadSpd - usedUploadSpd - uploadSpdBuffer;	// get the unused upload spd
 				if(nodeTag == 0){
 					Gcp2pProtocol prot = (Gcp2pProtocol) omsg.sender.getProtocol(pid);
 					spdAvail = maxSpdPerVid - videoSpdAlloted[prot.videoID];
 					
 					}
 				if(spdAvail>0){		
 					// if the spd available is not zeroed out. send the spd available
 					// 232 = 168 + 64bits for speed
 					p.sendMsg(new OrangeMessage(OrangeMessage.UPLOAD_SPEED_THAT_CAN_BE_GIVEN, node, omsg.sender, 232, spdAvail));		
 					uploadSpdBuffer+= spdAvail;
 
 				}
 				else {														// if there is no available upload spd. reject the CONNECT request
 					// 168, message type lang kailangan
 					p.sendMsg(new OrangeMessage(OrangeMessage.REJECT, node, omsg.sender, 168, 0));		
 				}
 				//startedStreaming = true;
 				//if(nodeTag != 0)
 					//System.out.println("connect");
 				
 			}
 			else if (omsg.msgType == OrangeMessage.REJECT){
 					//hindi ko pa alam ano mangyayari
 				numConnectionsRejected++;
 			}
 			else if (omsg.msgType == OrangeMessage.YOUR_CLIENTS){							// the CDN sent your clients
 				nodeTag = 1;
 				clientList = omsg.nodeList;
 				clientWatching = omsg.peerWatching;
 				indexPerCategory = omsg.index;
 				numClients = omsg.data;
 				otherSP = new Node[5];
 				//magsend ulit ng GET_SUPERPEER
 				Gcp2pProtocol prot = (Gcp2pProtocol) node.getProtocol(pid);
 				
 				// 171 = 168 + 3bits
 				p.sendMsg(new OrangeMessage(OrangeMessage.GET_SUPERPEER, node, prot.connectedCDN, 171, binID));		
 			}
 			else if (omsg.msgType == OrangeMessage.YOUR_SUPERPEER){						// not gonna happen
 				if(omsg.superPeer!=null){
 					// 232 = 168 + 32 bits for categoryID + 32bits for videoID
 					p.sendMsg(new OrangeMessage(OrangeMessage.REQUEST_PEERS_FROM_THIS_BIN, node, omsg.superPeer, 232, categoryID, videoID));		
 					if(!firstConnect && !startTimeLogged){
 						setStartTime();
 						startTimeLogged = true;
 					}
 				}
 				else{
 					// 171 = 168 + 3bits
 					p.sendMsg(new OrangeMessage(OrangeMessage.GET_MY_CLIENTS, node, omsg.sender, 171, binID));		
 				}
 			}
 			
 			else if (omsg.msgType == OrangeMessage.GOODBYE){
 				for(int i = 0; i < maxClients; i++){
 					if(peerList[i] == omsg.sender){
 						usedUploadSpd = usedUploadSpd -peerSpdAlloted[i];
 						activeLeechers--;
 						if(nodeTag == 0){
 							Gcp2pProtocol prot = (Gcp2pProtocol) omsg.sender.getProtocol(pid);
 							videoSpdAlloted[prot.videoID]-= peerSpdAlloted[i];							
 						}
 						break;
 					}					
 				}				
 			}
 			else if (omsg.msgType == OrangeMessage.GOODBYE_AS_SOURCE){
 				usedDownloadSpd -= omsg.data;
 				if (usedDownloadSpd < 0)
 					System.out.println("Negative Speed");
 			}
 	}
 	
 	public void sendMsg(OrangeMessage orangeMsg)
 	{
 		Node sender = orangeMsg.sender;
 		Node receiver = orangeMsg.receiver;
 		
 		Router router = (Router)sender.getProtocol(nid);
 		router.insertMsg(orangeMsg);		
 	}
 	
 	
 	public void start(Node node){
 		Gcp2pProtocol prot = (Gcp2pProtocol) node.getProtocol(pid);
 		// TODO size of the message
 		prot.sendMsg(new OrangeMessage(OrangeMessage.GET_SUPERPEER, node, prot.connectedCDN, 0, binID));
 				
 		//System.out.println(node.getIndex());
 		if(prot.connectedCDN == null)
 			System.out.println("up  " + node.getIndex());
 		if(sourcePeerList == null)
 			sourcePeerList = new Node[prot.maxClients];
 		if(peerList == null)
 			peerList = new Node[prot.maxClients];
 		if( peerSpdAlloted == null)
 			peerSpdAlloted = new int [prot.maxClients];
 	}
 	
 	/**
 	* binning technique based on position(RTT) wrt to landmarks	
 	*	COMPUTE THE BIN
 	*		binID values:
 	*		0 if L1>L2>L3
 	*		1 if L1>L3>L2
 	*		2 if L2>L1>L3
 	*		3 if L2>L3>L1
 	*		4 if L3>L1>L2
 	*		5 if L3>L2>L1
 	*/
 	
 	public void computeBin(){
 		if(landmark1RTT>=landmark2RTT && landmark2RTT >= landmark3RTT){
 			this.setBinID(0);
 		}
 		else if (landmark1RTT>=landmark3RTT && landmark3RTT >= landmark2RTT){
 			this.setBinID(1);
 		}
 		else if (landmark2RTT>=landmark1RTT && landmark1RTT >= landmark3RTT){
 			this.setBinID(2);
 		}
 		else if (landmark2RTT>=landmark3RTT && landmark3RTT >= landmark1RTT){
 			this.setBinID(3);
 		}
 		else if (landmark3RTT>=landmark1RTT && landmark1RTT >= landmark2RTT){
 			this.setBinID(4);
 		}
 		else if (landmark3RTT>=landmark2RTT && landmark2RTT >= landmark1RTT){
 			this.setBinID(5);
 		}
 	}
 	// huwag muna pansinin to :D
 	public void superpeerArrInit(){
 		//clientRTT = new int[maxClients];
 		clientList = new Node[maxClients];
 		clientWatching = new int[maxClients];
 		indexPerCategory = new int[category][maxClients];
 		for (int i = 0; i < maxClients; i ++){
 			clientList[i] = null;
 			clientWatching[i] = -1;
 		}
 		for (int i = 0; i < category; i++)
 			for (int j = 0; j < maxClients; j++)
 				indexPerCategory[i][j] = -1;
 		//binSize = new int[maxBins];
 		//binList = new Node[maxBins][maxClients];
 		//binWatchList = new int[maxBins][maxClients];
 		//peerList = new Node[maxClients];
 		//sourcePeerList = new Node[maxClients];
 		
 	}
 	
 	/**
 	 * Add node n to the list of peers in the bin
 	 * @param bin - the binID
 	 * @param n - the node to be added
 	 */
 	public void addToBin(int bin, Node n)
 	{
 		int size = binSize[bin];
 		binList[bin][size] = n;
 		binSize[bin]++;
 		Gcp2pProtocol prot = (Gcp2pProtocol) n.getProtocol(pid);
 		for(int i = 0; i < maxClients; i++)
 			if(binIndexPerCategory[bin][prot.categoryID][i] == -1){
 				binIndexPerCategory[bin][prot.categoryID][i] = size;
 				break;
 			}
 		//System.out.println(prot.CID+":"+bin+":"+binSize[bin]);
 	}
 	
 	/**
 	 * Add node n to client list of a CDN or a SuperPeer
 	 * @param n - node to be added
 	 */
 	public boolean addClient(Node n){
 		// TODO Auto-generated method stub
 		//QUESTON: Should check first if client is alive? Else return false.
 		//Check if SuperPeer, initialize, relevant lists
 		/*if(clientList == null && ){
 
 		}
 		 */		
 		clientList[numClients] = n;
 		
 		Gcp2pProtocol prot = (Gcp2pProtocol) n.getProtocol(pid);
 		clientRTT[numClients] = prot.getCDNRTT();
 		
 		numClients++;
 		EDSimulator.add(0, new NextCycleEvent(null), n, pid);
 		return true;
 	}
 	
 	
 	
 	/*
 	*	OVERLAY overridden METHODS
 	*/
 	
 	public void setNodeTag(int tag) {
 		this.nodeTag = tag;
 	}
 	
 	
 	public int getNodeTag() {
 		return this.nodeTag;
 	}
 		
 	
 	public void setConnectedCDN(int cdnID)
 	{
 		switch(cdnID)
 		{
 			case 0: connectedCDN = null; //the node itself is the CDN
 					break;
 			case 1: connectedCDN = Gcp2pProtocol.CDN1;		//connected to CDN1
 					CID = 1;
 					break;
 			case 2: connectedCDN = Gcp2pProtocol.CDN2;		//connected to CDN2
 					CID = 2;
 					break;
 			case 3: connectedCDN = Gcp2pProtocol.CDN3;		//connected to CDN3
 					CID = 3;
 					break;
 		}
 	}
 	
 	public Node getConnectedCDN()
 	{
 		return this.connectedCDN;
 	}
 	
 	public Node getCDN(int cdnID)
 	{
 		switch(cdnID)
 		{
 			case 1: return CDN1;
 			case 2: return CDN2;
 			case 3: return CDN3;
 			default: return null;
 		}
 	}
 	
 	public void setCDNRTT(int rtt) {
 		cdnRTT = rtt;
 	}
 	
 	public int getCDNRTT()
 	{
 		return this.cdnRTT;
 	}
 	
 	
 	public void setBinID (int binID){
 		this.binID = binID;
 	}
 	
 	
 	public int getbinID() {
 		return this.binID;
 	}
 	
 	
 	public void setSuperPeerSize(int size){
 		/*superPeerList = new Node[size];
 		for (i = 0; i < size; i++)
 			Node[i] = null;
 			
 		*/
 		// TODO Auto-generated method stub
 	}
 	
 	
 	public void setSuperPeer (Node peer, int binID){
 		superPeerList[binID] = peer;
 	}
 	
 	
 	public Node getSuperpeer (int binID){
 		return superPeerList[binID];
 	}
 	
 	
 	public Node [] getPeerList (){
 		Node[] toReturn = new Node[20];
 		//for ()
 		// TODO Auto-generated method stub
 			return toReturn;
 	}
 	
 	/**
 	 * Applicable to SuperPeers and CDN Servers
 	 */
 	public Node [] getClientList ()
 	{
 		return this.clientList;
 	}
 	
 	
 	/**
 	 * CDN = 100 Mbps
 	 * SuperPeer/Regular = 125 kbps
 	 */
 	public void setDownloadSpd (int bw){
 		this.downloadSpd = bw;
 	}
 	
 	
 	public void setUploadSpd (int bw){
 		this.uploadSpd = bw;
 	}
 	
 	
 	public void setUsedDownloadSpd (int bw){
 		this.usedDownloadSpd = bw;
 	}
 	
 	
 	public void setUsedUploadSpd (int bw){
 		this.usedUploadSpd = bw;
 	}
 	
 	
 	public int getDownloadSpd (){
 		return downloadSpd;
 	}
 	
 	
 	public int getUploadSpd (){
 		return uploadSpd;
 	}
 	
 	
 	public int getUsedDownloadSpd (){
 		return usedDownloadSpd;
 	}
 	
 	
 	public int getUsedUploadSpd (){
 		return usedUploadSpd;
 	}
 	
 	public void setLandmarkRTT (int landmark, int max, int min){	
 		switch(landmark) {
 			case 1: landmark1RTT = CommonState.r.nextInt((max-min)+1) + min;
 					break;
 			case 2: landmark2RTT = CommonState.r.nextInt((max-min)+1) + min;
 					break;
 			case 3: landmark3RTT = CommonState.r.nextInt((max-min)+1) + min;
 					break;
 			
 		}	
 		
 	}
 	
 	public int getLandmarkRTT (int landmark){
 		switch(landmark) {
 			case 1: return landmark1RTT;
 			case 2: return landmark2RTT;
 			case 3: return landmark3RTT;
 			default: return 0; 
 		}
 	}
 
 	/**
 	 * Get video list
 	 */
 	@Override
 	public int[] getVideoList() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	public void setStartTime () {
 		startTime = System.currentTimeMillis();
 	}
 	
 	public void setTimeElapsed () {
 		elapsedTime = System.currentTimeMillis() - startTime;
 	}
 	
 	public long getTimeElapsed () {
 		return elapsedTime;
 	}
 
 	public void setClientList(Node[] clientList){
 		this.clientList = new Node[clientList.length];
 		for(int i = 0; i<clientList.length;i++)
 			this.clientList[i] = clientList[i];
 		//this.clientList = clientList;
 	}
 	public void setIndexPerCategory(int[][] indexPerCategory){
 		//this.indexPerCategory = indexPerCategory;
 		this.indexPerCategory = new int[category][indexPerCategory[0].length];
 		for(int i = 0; i < category; i++)
 			for(int j = 0; j < indexPerCategory[i].length; j++)
 				this.indexPerCategory[i][j] = indexPerCategory[i][j];
 		//System.arraycopy(indexPerCategory[i], 0, this.indexPerCategory[i], 0, indexPerCategory[0].length);
 
 	}
 	public void setClientWatching(int[] clientWatching){
 		//this.clientWatching = clientWatching;
 		this.clientWatching = new int[clientWatching.length];
 		for(int i = 0; i<clientWatching.length;i++)
 			this.clientWatching[i] = clientWatching[i];
 	}
 	public int[] randomRTTs(Node[] list){
 		int length = list.length;
 		int[] RTTs = new int[length];
 		for (int i = 0; i < length; i++){
 			if (list[i]!= null){
 				Gcp2pProtocol prot = (Gcp2pProtocol) list[i].getProtocol(pid);
 				if(prot.CID == CID){
 					if(prot.getbinID() == binID){
 						RTTs[i] = CommonState.r.nextInt(480) + 2;
 					}
 					else{
 						RTTs[i] = CommonState.r.nextInt(980) + 2;
 					}
 				}
 				else{
 					RTTs[i] = CommonState.r.nextInt(500) + 1000;
 				}
 			}
 		}
 		return RTTs;
 	}
 }
