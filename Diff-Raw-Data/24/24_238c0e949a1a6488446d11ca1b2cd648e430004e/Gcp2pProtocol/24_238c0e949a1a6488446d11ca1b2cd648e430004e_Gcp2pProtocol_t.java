 package caviar;
 
 import peersim.config.Configuration;
 import peersim.config.FastConfig;
 import peersim.core.Control;
 import peersim.core.CommonState;
 import peersim.core.Network;
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
 	int highestStreamingSameVid;
 	long startTime;			// Time the node was initialized
 	long elapsedTime;		// Time it took from initialization until completion of stream
 	
 	int[][] indexPerCategory; // index of peers per category i.e. indexPerCategory[0][1] = 5, then clientList[5] watches a video with category 0
 	Node[] superPeerList;	// list of SuperPeers
 	Node[] clientList;		// applicable to CDN and SuperPeer
 	Node[] peerList;		// list of peers the node uploads to
 	int[] peerSpdAlloted;		// speed alloted to peers
 	int numPeers;			// number of peers it contributes to
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
 	
 	// ------------------------------------------------------------------------
 	// Constructor
 	// ------------------------------------------------------------------------
 	public Gcp2pProtocol(String prefix){
 		maxClients = Configuration.getInt(prefix + "." + PAR_MAXCLIENTS);
 		tid = Configuration.getPid(prefix + "." + PAR_TRANS);
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
 		//System.out.println("next");
 		if(startedStreaming){
 			for(int i = 0; i < numPeers; i++){
 				((Transport)node.getProtocol(tid)).
 								send(
 									node,
 									peerList[i],
 									new ArrivedMessage(ArrivedMessage.UPLOAD, node, peerSpdAlloted[i]),
 									pid);
 				
 			}
 		}
 		
 		
 	}
 	
 	//eto yung magproprocess ng messages
 	
 	public void processEvent( Node node, int pid, Object event ) {
 		ArrivedMessage aem = (ArrivedMessage)event;
 		//System.out.println("ye");
 		//CDN messages
 		//System.out.println(aem.sender.getIndex());
 		if (nodeTag == 0){
 			/**
 			*	message received requesting superpeer
 			*/
 			
 			if (aem.msgType == ArrivedMessage.GET_SUPERPEER){		//new peer requests for its bin's SP. aem.data is the binID
 				Gcp2pProtocol prot = (Gcp2pProtocol) aem.sender.getProtocol(pid);
 				/*switch(prot.CID){			// get the new peer's RTT to compare with the current SP
 					case 0: 
 						int tempRTT = prot.CDN1RTT;
 						break;
 					case 1:
 						int tempRTT = prot.CDN2RTT;
 						break;
 					case 2:
 						int tempRTT = prot.CDN3RTT;
 				}*/
 				int tempRTT = prot.cdnRTT;
 				addToBin(aem.data, aem.sender);
 				Node sp;
 				if(tempRTT < bestRTT[aem.data]){	// if the new peer's RTT is lower, make the SP_var to be sent null. this will force the new peer to send a GET_MY_CLIENT
 					sp = null;
 				}
 				else sp = superPeerList[aem.data];
 				if(sp == null)						// update bestRTT
 					bestRTT[aem.data] = tempRTT;
 				((Transport)node.getProtocol(tid)).
 							send(
 								node,
 								aem.sender,
 								new ArrivedMessage(ArrivedMessage.YOUR_SUPERPEER, node, sp),
 								pid);
 				/*else {
 					((Transport)node.getProtocol(tid)).
 								send(
 									node,
 									aem.sender,
 									new ArrivedMessage(YOU_ARE_SUPERPEER, node, 0),
 									pid);
 					
 				}*/
 					
 			}
 			else if (aem.msgType == ArrivedMessage.DO_YOU_HAVE_THIS){			// this won't happen
 				/**
 				*	message received asking if the CDN has the video
 				*/
 				int i;
 				int reply = ArrivedMessage.I_DONT_HAVE_IT;
 				for(i = 0; i<videoList.length; i++){ // check if the requested video's id is in the list
 					if(aem.data == videoList[i]){
 						reply = ArrivedMessage.I_HAVE_IT;
 						break;
 						}
 				}
 				Node temp = null;
 				((Transport)node.getProtocol(tid)).
 							send(
 								node,
 								aem.sender,
 								new ArrivedMessage(reply, node, temp),
 								pid);
 					
 			}
 			else if (aem.msgType == ArrivedMessage.GET_MY_CLIENTS){		// a new SuperPeer requests for its clients. aem.data is the binID
 				/**
 				*	A peer asks for the list of clients in a certain bin
 				*	a peer will only request this when the YOUR_SUPERPEER message is null
 				*/
 				
 				Node[] temp = binList[aem.data];
 				int[][] tempIndex = binIndexPerCategory[aem.data];
 				int [] tempWatching = binWatchList[aem.data];
 				int tempSize = binSize[aem.data];
 				((Transport)node.getProtocol(tid)).
 							send(
 								node,
 								aem.sender,
 								new ArrivedMessage(ArrivedMessage.YOUR_CLIENTS, node, temp, tempWatching, tempIndex, tempSize),
 								pid);
 				if(superPeerList[aem.data]!=null)		// send this to notify the old SP that he is fired
 						((Transport)node.getProtocol(tid)).
 								send(
 									node,
 									superPeerList[aem.data],
 									new ArrivedMessage(ArrivedMessage.FIRED, node, aem.sender),
 									pid);
 				
 				for(int i = 0; i<5; i++){
 					((Transport)node.getProtocol(tid)).
 									send(
 										node,
 										superPeerList[aem.data],
 										new ArrivedMessage(ArrivedMessage.UPDATE_SP, node, superPeerList[aem.data], aem.sender),
 										pid);
 				}
 				superPeerList[aem.data] = aem.sender;	// make the sender the SP
 			}
 			
 			
 			
 			
 		}
 		else if (nodeTag == 1){
 				if (aem.msgType == ArrivedMessage.REQUEST_PEERS_FROM_THIS_BIN ){	// a Peer requests a SP for peers. aem.data0 - categoryID. aem.data - videoID
					Gcp2pProtocol prot = (Gcp2pProtocol)aem.sender.getProtocol(pid);
					Gcp2pProtocol prot2 = (Gcp2pProtocol)node.getProtocol(pid);
					System.out.println(aem.data0 +"   "+ node.getIndex() + "   "+aem.sender.getIndex()+"   "+numClients);
 					int temp[] = indexPerCategory[aem.data0];		// get the list of indices of the peers watching a certain category
 					Node[] peers = new Node[1000];
 					if(prot.binID == binID){
 						clientList[numClients] = aem.sender;
 						for(int i = 0; i < maxClients; i++)
 							if(binIndexPerCategory[prot.binID][prot.categoryID][i] == -1){
 								binIndexPerCategory[prot.binID][prot.categoryID][i] = numClients;
 							}
 						numClients++;
 						
 					}
 					int i = 0;
 					int j = 0;
 					while(temp[i] >= 0){							// get the nodes watching the video requested
 						if(clientWatching[temp[i]] == aem.data){
 							peers[j] = clientList[temp[i]];
 							j++;
 						}
 					}
 					((Transport)node.getProtocol(tid)).
 							send(
 								node,
 								aem.sender,
 								new ArrivedMessage(ArrivedMessage.YOUR_PEERS, node, peers, j),
 								pid);
 					
 				}
 				
 				else if (aem.msgType == ArrivedMessage.REQUEST_PEERS_FROM_OTHER_BINS){	// the peers returned in REQUEST_PEERS_FROM_THIS_BIN is empty
 					for(int i= 0; i<5; i++){							// send requests to other SP and define the new peer as the sender. This will make the other SP
 																		// to send their Reply to the new peer
 						if(otherSP[i] != null)
 							((Transport)node.getProtocol(tid)).
 								send(
 									node,
 									otherSP[i],
 									new ArrivedMessage(ArrivedMessage.REQUEST_PEERS_FROM_THIS_BIN, aem.sender, aem.data0, aem.data),
 									pid);
 						else 
 							((Transport)node.getProtocol(tid)).
 								send(
 									node,
 									aem.sender,
 									new ArrivedMessage(ArrivedMessage.YOUR_PEERS, node, null, 0),
 									pid);
 					}
 				}
 				else if (aem.msgType == ArrivedMessage.UPDATE_SP){
 					for(int i = 0; i<5; i++){
 						if(otherSP[i].equals(aem.node1)){
 							otherSP[i] = aem.node2;
 							break;
 						}
 					}
 					
 				}
 
 		}
 		
 		// Node is a Regular Peer
 		else {
 			if(aem.msgType == ArrivedMessage.UPLOAD){					// a chunk is delivered. aem.data is the chunk
 				streamedVideoSize = streamedVideoSize + aem.data;
 				if(streamedVideoSize>= videoSize){		// check if done streaming. if yes, send GOODBYE messages
 					for(int i = 0; i< numSource; i++){
 						((Transport)node.getProtocol(tid)).
 								send(
 									node,
 									sourcePeerList[i],
 									new ArrivedMessage(ArrivedMessage.GOODBYE, node, 0),
 									pid);
 					}
 					doneStreaming = true;
 				}
 			}
 			else if (aem.msgType == ArrivedMessage.YOUR_PEERS){			// The peerList was sent by the SP
 				int i = 0;
 					if(aem.data == 0){						// if the list is empty, request from other bins
 						if(SPreply == 0){					// if SPreply = 0 means that it was sent from the peer's SP
 							((Transport)node.getProtocol(tid)).
 								send(
 									node,
 									aem.sender,
 									new ArrivedMessage(ArrivedMessage.REQUEST_PEERS_FROM_OTHER_BINS, node, categoryID, videoID),
 									pid);
 						}
 					}
 					else {									// the list is not empty
 						if(SPreply == 0){					// if SPreply = 0 then the peers is from it's bin
 							while(usedDownloadSpd < downloadSpd && i < aem.data){
 								((Transport)node.getProtocol(tid)).
 											send(
 												node,
 												aem.nodeList[i],
 												new ArrivedMessage(ArrivedMessage.CONNECT, node, downloadSpd - usedDownloadSpd),
 												pid);
 							}
 						}
 						else {								// if the peers is not from it's bin. check if the number of peers is higher than the current candidate
 							if(aem.data > highestStreamingSameVid){
 								candidatePeers = aem.nodeList;
 								highestStreamingSameVid = aem.data;
 							}
 						}
 					}
 					SPreply++;
 					if(SPreply == maxBins){						// means that all the bins have sent its peers
 						while(usedDownloadSpd < downloadSpd && i < highestStreamingSameVid){	//send CONNECT messages to the bin with the highest number of peers
 								((Transport)node.getProtocol(tid)).
 											send(
 												node,
 												candidatePeers[i],
 												new ArrivedMessage(ArrivedMessage.CONNECT, node, downloadSpd - usedDownloadSpd),
 												pid);
 							}
 						if(highestStreamingSameVid == 0){
 							((Transport)node.getProtocol(tid)).
 											send(
 												node,
 												connectedCDN,
 												new ArrivedMessage(ArrivedMessage.CONNECT, node, downloadSpd - usedDownloadSpd),
 												pid);
 						}
 					}
 			
 			}
 			else if (aem.msgType == ArrivedMessage.UPLOAD_SPEED_THAT_CAN_BE_GIVEN){	// reply to the CONNECT request. aem.data is the maximum upload spd that can be given
 					int spdAvail = downloadSpd - usedDownloadSpd;
 					int tobeAccepted;
 					if (spdAvail > 0){									// check if the available download spd is not yet maxed
 						if(spdAvail >= aem.data)						// if the available download speed is equal or greater than the proposed upload spd, get it all
 							tobeAccepted = aem.data;
 						else tobeAccepted = spdAvail;					// if not, get only the available download spd
 						usedDownloadSpd = usedDownloadSpd + aem.data;
 						((Transport)node.getProtocol(tid)).
 									send(
 										node,
 										aem.sender,
 										new ArrivedMessage(ArrivedMessage.ACCEPT_SPEED, node, aem.data, tobeAccepted),
 										pid);
 						sourcePeerList[numSource] = aem.sender;
 						numSource++;
 						startedStreaming = true;
 					}
 					else {												// if the download spd is maxed, send a REJECT message
 						((Transport)node.getProtocol(tid)).
 									send(
 										node,
 										aem.sender,
 										new ArrivedMessage(ArrivedMessage.REJECT_SPEED, node, aem.data),
 										pid);
 					}
 					
 			}
 			else if (aem.msgType == ArrivedMessage.ACCEPT_SPEED){						// reply to the proposed upload spd. aem.date0 is the proposed upload spd. aem.data is the acceoted sod
 				peerList[numPeers] = aem.sender;
 				peerSpdAlloted[numPeers] = aem.data;
 				numPeers++;
 				uploadSpdBuffer = uploadSpdBuffer - aem.data0;
 				usedUploadSpd = usedUploadSpd + aem.data;
 				
 			}
 			else if (aem.msgType == ArrivedMessage.REJECT_SPEED){						// if the upload spd is rejected, remove the reserved uploadSpd in the uploadSpdBuffer
 				uploadSpdBuffer = uploadSpdBuffer - aem.data;
 				
 			}
 			else if (aem.msgType == ArrivedMessage.CONNECT){								// a peer is requesting for upload Spd
 				int spdAvail = uploadSpd - usedUploadSpd - uploadSpdBuffer;	// get the unused upload spd
 				if(spdAvail>0){												// if the spd available is not zeroed out. send the spd available
 					((Transport)node.getProtocol(tid)).
 									send(
 										node,
 										aem.sender,
 										new ArrivedMessage(ArrivedMessage.UPLOAD_SPEED_THAT_CAN_BE_GIVEN, node, spdAvail),
 										pid);
 				}
 				else {														// if there is no available upload spd. reject the CONNECT request
 					((Transport)node.getProtocol(tid)).
 									send(
 										node,
 										aem.sender,
 										new ArrivedMessage(ArrivedMessage.REJECT, node, 0),
 										pid);
 				}
 			}
 			else if (aem.msgType == ArrivedMessage.REJECT){
 					//hindi ko pa alam ano mangyayari
 			}
 			else if (aem.msgType == ArrivedMessage.YOUR_CLIENTS){							// the CDN sent your clients
 				nodeTag = 1;
 				clientList = aem.nodeList;
 				clientWatching = aem.peerWatching;
 				indexPerCategory = aem.index;
 				numClients = aem.data;
 				//magsend ulit ng GET_SUPERPEER
 			}
 			else if (aem.msgType == ArrivedMessage.FIRED){									// the peer is not a SP anymore
 				nodeTag = 2;
 			}
 			else if (aem.msgType == ArrivedMessage.YOUR_SUPERPEER){						// not gonna happen
 				if(aem.superPeer!=null){
 					((Transport)node.getProtocol(tid)).
 							send(
 								node,
 								aem.superPeer,
 								new ArrivedMessage(ArrivedMessage.REQUEST_PEERS_FROM_THIS_BIN, node, categoryID, videoID),
 								pid);
 				}
 				else{
 					((Transport)node.getProtocol(tid)).
 							send(
 								node,
 								aem.sender,
 								new ArrivedMessage(ArrivedMessage.GET_MY_CLIENTS, node, binID),
 								pid);
 				}
 			}
 			
 		}
 
 
 	}
 	
 	public void start(Node node){
 		Gcp2pProtocol prot = (Gcp2pProtocol) node.getProtocol(pid);
 		((Transport)node.getProtocol(FastConfig.getTransport(pid))).
 							send(
 								node,
 								prot.connectedCDN,
 								new ArrivedMessage(ArrivedMessage.GET_SUPERPEER, node, binID),
 								pid);
 		//System.out.println(node.getIndex());
 		if(prot.connectedCDN .isUp())
 			System.out.println("up  " + node.getIndex());
 
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
 		clientRTT = new int[maxClients];
 		clientWatching = new int[maxClients];
 		indexPerCategory = new int[category][maxClients];
 		binSize = new int[maxBins];
 		binList = new Node[maxBins][maxClients];
 		binWatchList = new int[maxBins][maxClients];
 		peerList = new Node[maxClients];
 		sourcePeerList = new Node[maxClients];
 		
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
 		elapsedTime = startTime - System.currentTimeMillis();
 	}
 
 }
