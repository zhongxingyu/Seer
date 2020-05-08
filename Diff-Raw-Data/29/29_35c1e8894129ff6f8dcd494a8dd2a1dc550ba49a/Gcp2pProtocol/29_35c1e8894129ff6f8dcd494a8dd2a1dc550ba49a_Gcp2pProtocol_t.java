 package caviar;
 
 import peersim.config.Configuration;
 import peersim.config.FastConfig;
 import peersim.core.Control;
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
 	private static final int maxLandmarkRTT = 70;
 	private static final int minLandmarkRTT = 30;
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
 	
 	/**
 	*GLOBALS
 	*/
 	int nodeTag;			// Type of node: 0 - CDN, 1 - Superpeer, 2 - Regular
 	int connectedCDN;		// "ID" of the CDN it is closest/connected to
 	int cdnRTT;				// RTT of a client to its CDN; 
 	int landmark1RTT;		// RTT to landmark 1
 	int landmark2RTT;		// RTT to landmark 2
 	int landmark3RTT;		// RTT to landmark 3
 	int binID;				// which bin in the CDN group the node belongs to	
 	int uploadSpd;			// maximum upload capacity
 	int downloadSpd;		// maximum download capacity
 	int usedUploadSpd;		// used upload speed
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
 	int bestRTT;			// least RTT
 	int category;			// number of categories
 	int[] clientWatching;	// video the client is watching
 	
 	int[][] indexPerCategory; // index of peers per category i.e. indexPerCategory[0][1] = 5, then clientList[5] watches a video with category 0
 	Node[] superPeerList;	// list of SuperPeers
 	Node[] clientList;		// applicable to CDN and SuperPeer
 	Node[] peerList;		// list of peers the node uploads to
 	Node[] sourcePeerList;	// list of peers that contribute to the node
 	int binSize[]; //binSize[i] contains the number of peers inside bin i
 	Node binList[][]; //binList[i] returns the list peers inside bin i
 	int binWatchList[][]; //binWatchList[i][j] returns the what video peer j of bin i is watching
	int binIndexPerCategory[][][]; // CDN's copy of indexPerCategory, binIndexPerCategory[0][1][2] = 5 means that binList[0][5] watches a video with category 1
 	boolean streaming = false; // true if the node is already streaming
 	
 	
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
			prot = super.clone();
		}catch( CloneNotSupportedException e ) {} // never happens
		return prot;
	}
 		
 	//cycle chuchu, ewan ko kung gagawin natin, feeling ko hindi
 	public void nextCycle( Node node, int pid ){
 	
 	
 		
 	}
 	
 	//eto yung magproprocess ng messages
 	public void processEvent( Node node, int pid, Object event ) {
 		ArrivedMessage aem = (ArrivedMessage)event;
 		
 		*	CDN messages
 		
 		if (nodeTag == 0){
 			/**
 			*	message received requesting superpeer
 			*/
 			if (aem.msgType == GET_SUPERPEER){		
 				gcp2pProtocol prot = (gcp2pProtocol) aem.sender.getProtocol(pid);
 				switch(prot.connectedCDN){
 					case 0: 
 						int tempRTT = prot.CDN1RTT;
 						break;
 					case 1:
 						int tempRTT = prot.CDN2RTT;
 						break;
 					case 2:
 						int tempRTT = prot.CDN3RTT;
 				}
 				
 				if(superPeerList[aem.data] != null && tempRTT >= bestRTT)
 					((Transport)node.getProtocol(FastConfig.getTransport(pid))).
 								send(
 									node,
 									aem.sender,
 									new ArrivedMessage(YOUR_SUPERPEER, node, superPeerList[aem.data]),
 									pid);
 					
 			}
 			else if (aem.msgType == DO_YOU_HAVE_THIS){
 				/**
 				*	message received asking if the CDN has the video
 				*/
 				int i;
 				int reply = I_DONT_HAVE_IT;
 				for(i = 0; i<videoList.size(); i++){ // check if the requested video's id is in the list
 					if(aem.data == videoList[i]){
 						reply = I_HAVE_IT;
 						break;
 						}
 				}
 				
 				((Transport)node.getProtocol(FastConfig.getTransport(pid))).
 							send(
 								node,
 								aem.sender,
 								new ArrivedMessage(reply, node, null),
 								pid);
 					
 			}
 			else if (aem.msgType == GET_MY_CLIENTS){
 				/**
 				*	A peer asks for the list of clients in a certain bin
 				*	a peer will only request this when the YOUR_SUPERPEER message is null
 				*/
 				Node[] temp = binList[aem.data];
				int[][] tempIndex = binIndexPerCategory;
 				int [] tempWatching = binWatchList[aem.data];
 				 ((Transport)node.getProtocol(FastConfig.getTransport(pid))).
 							send(
 								node,
 								aem.sender,
								new ArrivedMessage(YOUR_CLIENTS, node, temp, tempWatching, tempIndex),
 								pid);
 			}
 			
 			
 			
 			
 		}
 		else if (nodeTag == 1){
 				if (aem.msgType == REQUEST_PEERS_FROM_THIS_BIN ){
 					int temp[] = indexPerCategory[aem.data0];
 					Node[] peers = new Node[20];
 					for(i = 0; i< 20; i++)
 						peers[i] = clientList[temp[i]];
 					((Transport)node.getProtocol(FastConfig.getTransport(pid))).
 							send(
 								node,
 								aem.sender,
 								new ArrivedMessage(YOUR_PEERS, node, peers),
 								pid);
 					
 				}
 
 		}
 
 		else {
 			
 		}
 
 
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
 	
 	public boolean setSuperPeers()
 	{
 		// TODO Auto-generated method stub
 		// which one to use clientRTT or binList?
 		int binsize;
 		for(int i = 0; i < 6; i ++)
 		{
 			binsize = binSize[i];
 			
 			for(int j = 0; j < binsize; j++)
 			{
 				//Choose the one with the best RTT
 			}//endinnerfor
 			
 			//call setSuperPeer
 		}//endfor
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
 		this.connectedCDN = cdnID;
 	}
 	
 	public int getConnectedCDN()
 	{
 		return this.connectedCDN;
 	}
 	
 	public Node getCDN(int cdnID)
 	{
 		switch(cdnID)
 		{
 			case 1: return CDN1;
 					break;
 			case 2: return CDN2;
 					break;
 			case 3: return CDN3;
 					break;
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
 	
 	public int setLandmarkRTT (int which){	
 		switch(which) {
 			case 1: landmark1RTT = CommonState.r.nextInt((maxLandmarkRTT-minLandmarkRTT)+1) + minLandmarkRTT;
 					break;
 			case 2: landmark2RTT = CommonState.r.nextInt((maxLandmarkRTT-minLandmarkRTT)+1) + minLandmarkRTT;
 					break;
 			case 3: landmark3RTT = CommonState.r.nextInt((maxLandmarkRTT-minLandmarkRTT)+1) + minLandmarkRTT;
 					break;
 			
 		}	
 		
 	}
 	
 	public int getLandmarkRTT (int whichLandmark){
 		switch(landmark) {
 			case 1: return landmark1RTT;
 			case 2: return landmark2RTT;
 			case 3: return landmark3RTT;
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
