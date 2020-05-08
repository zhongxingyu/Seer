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
 //protocol
 
 public class Gcp2pProtocol implements Overlay, CDProtocol, EDProtocol{
 	
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 	public static final int maxLandmarkRTT = 70;
 	public static final int minLandmarkRTT = 30;
 	// ------------------------------------------------------------------------
 	// Parameters  
 	// ------------------------------------------------------------------------
 	/**
 	 * String name of the parameter
 	 */
 	private static final String PAR_PROT = "protocol";
 	/**
 	 * String name of the parameter
 	 */
 	private static final String PAR_MAXCLIENTS = "maxclients";
 	
 	private static final String PAR_CATEGORY = "category";
 	// ------------------------------------------------------------------------
 	// Static Fields
 	// ------------------------------------------------------------------------
 	/**
 	 *  Protocol identifier, obtained from config property {@link #PAR_PROT}. 
 	 **/
 	private static int pid;	
 	/***
 	 * The nodes corresponding to the 3 CDNs in the set-up
 	 */
 	public static Node CDN1;
 	public static Node CDN2;
 	public static Node CDN3;	
 	/**
 	*	Message TYPES
 	*/
 	private static final int HELLO = 0;			//	HELLO MSG
 	private static final int GOODBYE = 1;		//	LEAVING MSG
 	private static final int UPLOAD = 2;			//	DATA TRANSFER
 	private static final int CONNECT = 3;		//	REQUEST FOR CONNECTION
 	private static final int REQUEST_PEERS_FROM_THIS_BIN = 4;	
 	private static final int NOONE_STREAMS_IN_THIS_BIN = 5;
 	private static final int REQUEST_PEERS_FROM_OTHER_BINS = 6;
 	private static final int GET_SUPERPEER = 7;
 	private static final int YOUR_SUPERPEER = 8;
 	private static final int DO_YOU_HAVE_THIS = 9;	// do you have this video? applicable message for CDN
 	private static final int I_HAVE_IT = 10;			// reply to DO_YOU_HAVE_THIS
 	private static final int I_DONT_HAVE_IT = 11;	// reply to DO_YOU_HAVE_THIS
 	private static final int REQUEST_MY_CLIENTS = 12;// new super peer asks for his clients
 	private static final int YOUR_CLIENTS = 13;		// reply to REQUEST_MY_CLIENTS
 	private static final int YOUR_PEERS = 14;
 	private static final int GOODBYE_LEECHER = 15;	// sent by a leaving node to its peer leechers
 	private static final int UPLOAD_SPEED_THAT_CAN_BE_GIVEN = 16;
 	private static final int ACCEPT_SPEED = 17;
 	private static final int REJECT_SPEED = 18;		// sent by the requesting peer when its capacity is maxed
 	private static final int FELLOW_SP_REQUEST_FOR_PEERS = 19;
 	private static final int FIRED = 20;	//sent by a CDN to a SP when a new peer is nearer
 	private static final int YOU_ARE_SUPERPEER = 21 ;
 	private static final int GET_MY_CLIENTS = 22;
 	private static final int REJECT = 23;
 	/**
 	*GLOBALS
 	*/
 	int nodeTag;			// Type of node: 0 - CDN, 1 - Superpeer, 2 - Regular
 	Node connectedCDN;		// the CDN node it is closest/connected to
 	int CID;
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
 	int maxClients; 		// max number of possible clients for CDNs and SuperPeers, obtained from config property {@link #PAR_MAXCLIENTS}. 
 	int maxBinSize;			// max number of peers inside a bin (same as maxClients) 
 	int numClients;			// number of clients
 	int[] videoList;			// list of videos
 	int[] clientRTT;		// RTT of clients
 	int[] bestRTT;			// least RTT
 	int category;			// number of categories
 	int[] clientWatching;	// video the client is watching
 	int SPreply = 0;			// number of SP that sent YOUR_PEERS
 	int[] streamingSameVidPerBin = new int[6];
 	int highestStreamingSameVid;
 	
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
 		maxClients = Configuration.getPid(prefix + "." + PAR_MAXCLIENTS);
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
 		for(int i = 0; i < numPeers l i++){
 			((Transport)node.getProtocol(FastConfig.getTransport(pid))).
 							send(
 								node,
 								peerList[i],
								new ArrivedMessage(UPLOAD, node, peerSpdAllocated[i]),
 								pid);
 			
 		}
 		
 		
 	}
 	
 	//eto yung magproprocess ng messages
 	public void processEvent( Node node, int pid, Object event ) {
 		ArrivedMessage aem = (ArrivedMessage)event;
 		
 		//CDN messages
 		
 		if (nodeTag == 0){
 			/**
 			*	message received requesting superpeer
 			*/
 			if (aem.msgType == GET_SUPERPEER){		//new peer requests for its bin's SP. aem.data is the binID
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
 				Node sp;
 				if(tempRTT < bestRTT[aem.data]){	// if the new peer's RTT is lower, make the SP_var to be sent null. this will force the new peer to send a GET_MY_CLIENT
 					sp = null;
 				}
 				else sp = superPeerList[aem.data];
 				if(sp == null)						// update bestRTT
 					bestRTT[aem.data] = tempRTT;
 				((Transport)node.getProtocol(FastConfig.getTransport(pid))).
 							send(
 								node,
 								aem.sender,
 								new ArrivedMessage(YOUR_SUPERPEER, node, sp),
 								pid);
 				/*else {
 					((Transport)node.getProtocol(FastConfig.getTransport(pid))).
 								send(
 									node,
 									aem.sender,
 									new ArrivedMessage(YOU_ARE_SUPERPEER, node, 0),
 									pid);
 					
 				}*/
 					
 			}
 			else if (aem.msgType == DO_YOU_HAVE_THIS){			// this won't happen
 				/**
 				*	message received asking if the CDN has the video
 				*/
 				int i;
 				int reply = I_DONT_HAVE_IT;
 				for(i = 0; i<videoList.length; i++){ // check if the requested video's id is in the list
 					if(aem.data == videoList[i]){
 						reply = I_HAVE_IT;
 						break;
 						}
 				}
 				Node temp = null;
 				((Transport)node.getProtocol(FastConfig.getTransport(pid))).
 							send(
 								node,
 								aem.sender,
 								new ArrivedMessage(reply, node, temp),
 								pid);
 					
 			}
 			else if (aem.msgType == GET_MY_CLIENTS){		// a new SuperPeer requests for its clients. aem.data is the binID
 				/**
 				*	A peer asks for the list of clients in a certain bin
 				*	a peer will only request this when the YOUR_SUPERPEER message is null
 				*/
 				Node[] temp = binList[aem.data];
 				int[][] tempIndex = binIndexPerCategory[aem.data];
 				int [] tempWatching = binWatchList[aem.data];
 				((Transport)node.getProtocol(FastConfig.getTransport(pid))).
 							send(
 								node,
 								aem.sender,
 								new ArrivedMessage(YOUR_CLIENTS, node, temp, tempWatching, tempIndex),
 								pid);
 				if(superPeerList[aem.data]!=null)		// send this to notify the old SP that he is fired
 						((Transport)node.getProtocol(FastConfig.getTransport(pid))).
 								send(
 									node,
 									superPeerList[aem.data],
 									new ArrivedMessage(FIRED, node, 0),
 									pid);
 				superPeerList[aem.data] = aem.sender;	// make the sender the SP
 				
 			}
 			
 			
 			
 			
 		}
 		else if (nodeTag == 1){
 				if (aem.msgType == REQUEST_PEERS_FROM_THIS_BIN ){	// a Peer requests a SP for peers. aem.data0 - categoryID. aem.data - videoID
 					int temp[] = indexPerCategory[aem.data0];		// get the list of indices of the peers watching a certain category
 					Node[] peers = new Node[1000];
 					int i = 0;
 					int j = 0;
 					while(temp[i] >= 0){							// get the nodes watching the video requested
 						if(clientWatching[temp[i]] == aem.data){
 							peers[j] = clientList[temp[i]];
 							j++;
 						}
 					}
 					((Transport)node.getProtocol(FastConfig.getTransport(pid))).
 							send(
 								node,
 								aem.sender,
 								new ArrivedMessage(YOUR_PEERS, node, peers, j),
 								pid);
 					
 				}
 				
 				else if (aem.msgType == REQUEST_PEERS_FROM_OTHER_BINS){	// the peers returned in REQUEST_PEERS_FROM_THIS_BIN is empty
 					for(int i= 0; i<5; i++){							// send requests to other SP and define the new peer as the sender. This will make the other SP
 																		// to send their Reply to the new peer
 						if(otherSP[i] != null)
 							((Transport)node.getProtocol(FastConfig.getTransport(pid))).
 								send(
 									node,
 									otherSP[i],
 									new ArrivedMessage(REQUEST_PEERS_FROM_THIS_BIN, aem.sender, aem.data0, aem.data),
 									pid);
 						else 
 							((Transport)node.getProtocol(FastConfig.getTransport(pid))).
 								send(
 									node,
 									aem.sender,
 									new ArrivedMessage(YOUR_PEERS, node, null, 0),
 									pid);
 					}
 				}
 
 		}
 
 		else {
 			if(aem.msgType == UPLOAD){					// a chunk is delivered. aem.data is the chunk
 				streamedVideoSize = streamedVideoSize + aem.data;
 				if(streamedVideoSize>= videoSize){		// check if done streaming. if yes, send GOODBYE messages
 					for(int i = 0; i< numSource; i++){
 						((Transport)node.getProtocol(FastConfig.getTransport(pid))).
 								send(
 									node,
 									sourcePeerList[i],
 									new ArrivedMessage(GOODBYE, node, 0),
 									pid);
 					}
 					doneStreaming = true;
 				}
 			}
 			else if (aem.msgType == YOUR_PEERS){			// The peerList was sent by the SP
 				int i = 0;
 					if(aem.data == 0){						// if the list is empty, request from other bins
 						if(SPreply == 0){					// if SPreply = 0 means that it was sent from the peer's SP
 							((Transport)node.getProtocol(FastConfig.getTransport(pid))).
 								send(
 									node,
 									aem.sender,
 									new ArrivedMessage(REQUEST_PEERS_FROM_OTHER_BINS, node, categoryID, videoID),
 									pid);
 						}
 					}
 					else {									// the list is not empty
 						if(SPreply == 0){					// if SPreply = 0 then the peers is from it's bin
 							while(usedDownloadSpd < downloadSpd && i < aem.data){
 								((Transport)node.getProtocol(FastConfig.getTransport(pid))).
 											send(
 												node,
 												aem.nodeList[i],
 												new ArrivedMessage(CONNECT, node, downloadSpd - usedDownloadSpd),
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
 					if(SPreply == 6){						// means that all the bins have sent its peers
 						while(usedDownloadSpd < downloadSpd && i < highestStreamingSameVid){	//send CONNECT messages to the bin with the highest number of peers
 								((Transport)node.getProtocol(FastConfig.getTransport(pid))).
 											send(
 												node,
 												candidatePeers[i],
 												new ArrivedMessage(CONNECT, node, downloadSpd - usedDownloadSpd),
 												pid);
 							}
 						if(highestStreamingSameVid == 0){
 							((Transport)node.getProtocol(FastConfig.getTransport(pid))).
 											send(
 												node,
 												connectedCDN,
 												new ArrivedMessage(CONNECT, node, downloadSpd - usedDownloadSpd),
 												pid);
 						}
 					}
 			
 			}
 			else if (aem.msgType == UPLOAD_SPEED_THAT_CAN_BE_GIVEN){	// reply to the CONNECT request. aem.data is the maximum upload spd that can be given
 					int spdAvail = downloadSpd - usedDownloadSpd;
 					int tobeAccepted;
 					if (spdAvail > 0){									// check if the available download spd is not yet maxed
 						if(spdAvail >= aem.data)						// if the available download speed is equal or greater than the proposed upload spd, get it all
 							tobeAccepted = aem.data;
 						else tobeAccepted = spdAvail;					// if not, get only the available download spd
 						usedDownloadSpd = usedDownloadSpd + aem.data;
 						((Transport)node.getProtocol(FastConfig.getTransport(pid))).
 									send(
 										node,
 										aem.sender,
 										new ArrivedMessage(ACCEPT_SPEED, node, aem.data, tobeAccepted),
 										pid);
 					}
 					else {												// if the download spd is maxed, send a REJECT message
 						((Transport)node.getProtocol(FastConfig.getTransport(pid))).
 									send(
 										node,
 										aem.sender,
 										new ArrivedMessage(REJECT_SPEED, node, aem.data),
 										pid);
 					}
 					
 			}
 			else if (aem.msgType == ACCEPT_SPEED){						// reply to the proposed upload spd. aem.date0 is the proposed upload spd. aem.data is the acceoted sod
 				peerList[numPeers] = aem.sender;
 				peerSpdAlloted[numPeers] = aem.data;
 				numPeers++;
 				uploadSpdBuffer = uploadSpdBuffer - aem.data0;
 				usedUploadSpd = usedUploadSpd + aem.data;
 				
 			}
 			else if (aem.msgType == REJECT_SPEED){						// if the upload spd is rejected, remove the reserved uploadSpd in the uploadSpdBuffer
 				uploadSpdBuffer = uploadSpdBuffer - aem.data;
 				
 			}
 			else if (aem.msgType == CONNECT){								// a peer is requesting for upload Spd
 				int spdAvail = uploadSpd - usedUploadSpd - uploadSpdBuffer;	// get the unused upload spd
 				if(spdAvail>0){												// if the spd available is not zeroed out. send the spd available
 					((Transport)node.getProtocol(FastConfig.getTransport(pid))).
 									send(
 										node,
 										aem.sender,
 										new ArrivedMessage(UPLOAD_SPEED_THAT_CAN_BE_GIVEN, node, spdAvail),
 										pid);
 				}
 				else {														// if there is no available upload spd. reject the CONNECT request
 					((Transport)node.getProtocol(FastConfig.getTransport(pid))).
 									send(
 										node,
 										aem.sender,
 										new ArrivedMessage(REJECT, node, 0),
 										pid);
 				}
 			}
 			else if (aem.msgType == REJECT){
 					//hindi ko pa alam ano mangyayari
 			}
 			else if (aem.msgType == YOUR_CLIENTS){							// the CDN sent your clients
 				nodeTag = 1;
 				clientList = aem.nodeList;
 				clientWatching = aem.peerWatching;
 				indexPerCategory = aem.index;
 				//magsend ulit ng GET_SUPERPEER
 			}
 			else if (aem.msgType == FIRED){									// the peer is not a SP anymore
 				nodeTag = 2;
 			}
 			else if (aem.msgType == YOUR_SUPERPEER){						// not gonna happen
 				if(aem.superPeer!=null){
 					((Transport)node.getProtocol(FastConfig.getTransport(pid))).
 							send(
 								node,
 								aem.superPeer,
 								new ArrivedMessage(REQUEST_PEERS_FROM_THIS_BIN, node, categoryID, videoID),
 								pid);
 				}
 				else{
 					((Transport)node.getProtocol(FastConfig.getTransport(pid))).
 							send(
 								node,
 								aem.sender,
 								new ArrivedMessage(GET_MY_CLIENTS, node, binID),
 								pid);
 				}
 			}
 			
 		}
 
 
 	}
 	
 	public void start(Node node){
 		((Transport)node.getProtocol(FastConfig.getTransport(pid))).
 							send(
 								node,
 								connectedCDN,
 								new ArrivedMessage(GET_SUPERPEER, node, binID),
 								pid);
 		
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
 		binSize = new int[6];
 		binList = new Node[6][maxClients];
 		binWatchList = new int[6][maxClients];
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
 	}
 	
 	/**
 	 * Add node n to client list of a CDN or a SuperPeer
 	 * @param n - node to be added
 	 */
 	public boolean addClient(Node n){
 		
 		//QUESTON: Should check first if client is alive? Else return false.
 		
 		if(clientList == null){
 			clientList = new Node[maxClients];
 		}
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
 		
 	public void setAsCDN(int cdnID, Node n) {
 		switch(cdnID)
 		{
 				case 1: Gcp2pProtocol.CDN1 = n;
 						break;
 				case 2: Gcp2pProtocol.CDN2 = n;
 						break;
 				case 3: Gcp2pProtocol.CDN3 = n;
 						break;
 		}
 		
 	}
 	
 	public void setConnectedCDN(int cdnID)
 	{
 		switch(cdnID)
 		{
 			case 0: connectedCDN = null; //the node itself is the CDN
 					break;
 			case 1: connectedCDN = Gcp2pProtocol.CDN1;		//connected to CDN1
 					break;
 			case 2: connectedCDN = Gcp2pProtocol.CDN2;		//connected to CDN2
 					break;
 			case 3: connectedCDN = Gcp2pProtocol.CDN3;		//connected to CDN3
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
 	
 	public void setLandmarkRTT (int landmark){	
 		switch(landmark) {
 			case 1: landmark1RTT = CommonState.r.nextInt((maxLandmarkRTT-minLandmarkRTT)+1) + minLandmarkRTT;
 					break;
 			case 2: landmark2RTT = CommonState.r.nextInt((maxLandmarkRTT-minLandmarkRTT)+1) + minLandmarkRTT;
 					break;
 			case 3: landmark3RTT = CommonState.r.nextInt((maxLandmarkRTT-minLandmarkRTT)+1) + minLandmarkRTT;
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
 
 }
