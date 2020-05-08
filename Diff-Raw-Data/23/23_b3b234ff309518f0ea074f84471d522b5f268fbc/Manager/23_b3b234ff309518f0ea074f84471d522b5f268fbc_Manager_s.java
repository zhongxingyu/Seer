 package utils;
 
 import java.nio.ByteBuffer;
 import java.io.*;
 import java.util.*;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.atomic.AtomicIntegerArray;
 
 import peers.*;
 
 public class Manager {
 
 	public static byte[] peer_id                 = new byte[20];
 	public static byte[] info_hash		    	     = new byte[20];
 	public static TorrentInfo torrent_info	     = null;
 	public static ConcurrentLinkedQueue<Block> q = null;
 	public static AtomicIntegerArray have_piece  = null;
 	public static RandomAccessFile file  	       = null;
 	public static int port			        		     = 0;
 	public static int interval                   = 0;
 	public static int numPieces   			         = 0;
 	public static int numLeft                    = 0;
	public static int numBlocks		    	         = 0;
 	public static boolean ready                  = false;
 
 	public static final int block_length = 16384;
 	public static final String STARTED   = "started";
 	public static final String COMPLETED = "completed";
 	public static final String STOPPED   = "stopped";
 	public static final String EMPTY     = "";
 
 	public static ArrayList<Peer> peerList_  = null;
 	
 	public Manager(){
 	}
 
 	public Manager(String torrentFile, String fileName){
 		setInfo(torrentFile,fileName);
 
     int leftoverBytes = torrent_info.file_length % block_length; 
     numPieces = leftoverBytes == 0 ?
                    torrent_info.file_length / torrent_info.piece_length :
                    torrent_info.file_length / torrent_info.piece_length + 1;
    numBlocks = torrent_info.piece_length / block_length;
 		
 		have_piece = new AtomicIntegerArray(numPieces);
 		System.out.println(leftoverBytes);
 		System.out.println(numPieces);
		System.out.println(numBlocks);
 
     //set up the reference queue
     q = new ConcurrentLinkedQueue<Block>();
	  for(int j = 0; j < numPieces; j++){
	    for(int k = 0; k < numBlocks; k++){
 				byte[] data = null;
         
				if(j == numPieces - 1 && k == numBlocks - 1){
 					data = new byte[leftoverBytes];
 				}
 				else{
 					data = new byte[block_length];
 				}
 				
         Block b = new Block(j, k, data);
         q.add(b);
 	    }
 	  }
 	}
 
 	public boolean download(){
 		byte[] response = null;
 		for(Peer peer : peerList_){
 			DownloadThread p = new DownloadThread(peer);
 			Thread a = new Thread(p);
       a.start();
 		}
 
     // response = Helpers.getURL(constructQuery(port, 0, torrent_info.file_length, 0, STOPPED));
     // System.out.println("\nFile finished.");
 		return false;
 	}
 
 	public static void setInfo(String torrentFile, String savedFile){
 		try{
 			torrent_info = new TorrentInfo(Helpers.readTorrent(torrentFile));
 			torrent_info.info_hash.get(info_hash, 0, info_hash.length);
 			file = new RandomAccessFile(savedFile,"rws");
 		} catch (Exception e){
 			System.out.println(e);
 			System.out.println("Torrent file could not be loaded.");
 			System.exit(1);
 		}
 	}
 
 	public static byte[] getPeerId() {
 		return peer_id;
 	}
 
 	public static void setPeerId(byte[] peer_id) {
 		Manager.peer_id = peer_id;
 	}
 
 	public static byte[] getInfoHash() {
 		return info_hash;
 	}
 
 	public static void setInfoHash(byte[] info_hash) {
 		Manager.info_hash = info_hash;
 	}
 
 	public static TorrentInfo getTorrentInfo() {
 		return torrent_info;
 	}
 
 	public static void setTorrentInfo(TorrentInfo torrent_info) {
 		Manager.torrent_info = torrent_info;
 	}
 
 	public static RandomAccessFile getFile() {
 		return file;
 	}
 
 	public static void setFile(RandomAccessFile file) {
 		Manager.file = file;
 	}
 
 	public static int getPort() {
 		return port;
 	}
 
 	public static void setPort(int port) {
 		Manager.port = port;
 	}
 	
 	public static void setPeerList (ArrayList<Peer> _peerList){
 		Manager.peerList_ = _peerList;
 		Manager.ready = true;
 	}
 
 	public static ArrayList<Peer> getPeerList(){
 		return peerList_;
 	}
 
 	public static int getNumPieces() {
 		return numPieces;
 	}
 
 	public static void setNumPieces(int numPieces) {
 		Manager.numPieces = numPieces;
 	}
 
 	/**	
 	 * Downloads the pieces from the peer and stores them in the appropriate file
 	 * @param peer the peer object to be downloading from
 	 */
 
   /**
    * Generates our random PeerID
    */
   public static void setPeerId(){
   	Random ran = new Random();
   	int rand_id = ran.nextInt(5555555 - 1000000 + 1) + 1000000;
   	String peer_id_string = "GROUP4AREL33t" + rand_id;
   	peer_id = peer_id_string.getBytes();
   }
 
 	/**
 	 * Construct the url for tracker querying
 	 * @param port  port to be used
 	 * @param uploaded  amount uploaded
 	 * @param downloaded  amount downloaded
 	 * @param left amount left
 	 * @param event event type
 	 * @return String to be sent as query
 	 */
 	public static String constructQuery(int port, int uploaded, int downloaded, int left, String event){
 		String url_string = "";
 		try{
 			String escaped_hash = Helpers.toURLHex(info_hash);
 			String escaped_id = Helpers.toURLHex(peer_id);
 			String ip = "128.6.5.130";
 			url_string =  torrent_info.announce_url.toString()
 			+ "?port=" + port
 			+ "&peer_id=" + escaped_id
 			+ "&info_hash=" + escaped_hash 
 			+ "&uploaded=" + uploaded
 			+ "&downloaded=" + downloaded
 			+ "&left=" + left
 			+ "&ip=" +  ip
 			+ "&event=" + event;
 
 		} catch (Exception e){
 			System.out.println(e);
 		}
 
 		return url_string;
 	}
 	
 	//query the tracker and get the initial list of peers
 	public static void queryTracker(){
 		byte[] response = null;
 		int i = 0;
 		for (i=6881; i<=6889;){
 			try{
 				response = Helpers.getURL(constructQuery(i, 0, 0, torrent_info.file_length, ""));
 				setPort(i);
 				break;
 			} catch (Exception e){
 				System.out.println("Port " + i + " failed");
 				i++;
 				continue;
 			}
 		}
 
 		setPeerList(response);
 	}
 	
 	public static final ByteBuffer intervalKey = ByteBuffer.wrap(new byte[]{'i','n','t','e','r','v','a','l'});
 	public static final ByteBuffer peersKey = ByteBuffer.wrap(new byte[]{'p','e','e','r','s'});
 	public static final ByteBuffer minIntervalKey = ByteBuffer.wrap(new byte[]{'m','i','n',' ','i','n','t','e','r','v','a','l'});
 	public static final ByteBuffer downloadedKey = ByteBuffer.wrap(new byte[]{'d','o','w','n','l','o','a','d','e','d'});
 	public static final ByteBuffer completeKey = ByteBuffer.wrap(new byte[]{'c','o','m','p','l','e','t','e'});
 	public static final ByteBuffer ipKey = ByteBuffer.wrap(new byte[]{'i','p'});
 	public static final ByteBuffer peerIdKey = ByteBuffer.wrap(new byte[]{'p','e','e','r',' ','i','d'});
 	public static final ByteBuffer portKey = ByteBuffer.wrap(new byte[]{'p','o','r','t'});
 
 	/**
 	 * Gets the peer list from a response from the tracker
 	 * @param response	byte array response from 
 	 * @return			    returns the array list of peers
 	 */
 	public static void setPeerList(byte[] response){	  
 		ArrayList<Peer> peerList = new ArrayList<Peer>();
 		try{
 			Object decodedResponse = Bencoder2.decode(response);
       // ToolKit.print(decodedResponse, 1);
 
 			Map<ByteBuffer, Object> responseMap = (Map<ByteBuffer, Object>)decodedResponse;
 			interval = (Integer)responseMap.get(intervalKey);
 
 			ArrayList<Object> peerArray = (ArrayList<Object>)responseMap.get(peersKey);
 
 			for (int i = 0;i<peerArray.size();i++){
 				Object peer = peerArray.get(i);
 				String ip_ = "";
 				String peer_id_ = "";
 				int port_ = 0;
 
 				Map<ByteBuffer, Object> peerMap = (Map<ByteBuffer, Object>)peer;
 				ip_ = Helpers.bufferToString((ByteBuffer)peerMap.get(ipKey));
 				peer_id_ = Helpers.bufferToString((ByteBuffer)peerMap.get(peerIdKey));
 				port_ = (Integer)peerMap.get(portKey);
 				// System.out.println(ip_ +" " +  peer_id_ +" " +  port_);
 				Peer newPeer = new Peer(peer_id_, ip_, port_);
 
 				if(newPeer.isValid()){
 					peerList.add(newPeer);
 				}
 			}
 		} catch (Exception e){
 			e.printStackTrace();
 		}
 
 		peerList_ = peerList;
 		
 		//ready to start! THIS IS TERRIBLE CODE
 		ready = true;
 	}
 }
 
