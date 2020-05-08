 import java.io.*;
 import java.util.*;
 import java.net.*;
 import java.nio.ByteBuffer;
 
 import utils.TorrentInfo;
 import utils.Bencoder2;
 import utils.Helpers;
 import utils.ToolKit;
 
 import peers.Peer;
 
 public class CannonClient {
   
   public static byte[] PEER_ID = new byte[20];
   public static byte[] INFO_HASH = new byte[20];
   public static TorrentInfo TORRENT_INFO;
 	
 	public static void main(String[] args) {
     
     String torrentFile = args[0];
     String savedFile = args[1];
     setPeerId();
 
     //set up the torrent info
     try{
       TORRENT_INFO = new TorrentInfo(readTorrent(torrentFile));
       TORRENT_INFO.info_hash.get(INFO_HASH, 0, INFO_HASH.length);
     } catch (Exception e){
     	e.printStackTrace();
       System.out.println("Torrent could not be loaded.");
     }
     
     //query tracker
     //TODO: write a loop that cycles through ports
     for (int i=6881; i<=6889;){
       try{
         String url = constructQuery(i, 0, 0, TORRENT_INFO.file_length); 
         byte[] response = getURL(url);
         ArrayList<Peer> peerList = getPeers(response);
         for(Peer peer : peerList){
           if (peer.isValid()){
             System.out.println("Peer Found");
             peer.createSocket(peer.ip_, peer.port_);
             peer.establishStreams();
             peer.sendHandshake(PEER_ID, INFO_HASH);
           }
         }
         
         break;
       } catch (Exception e){
       	e.printStackTrace();
         System.out.println("Port " + i + " failed."); 
         i++;
       }
     }
 	}
 	
 	public static ArrayList<Peer> getPeers(byte[] response){
 	  ArrayList<Peer> peerList = new ArrayList<Peer>();
 
 	  try{
       //decode the response and slap it in an array for perusal
       Object decodedResponse = Bencoder2.decode(response);
       // ToolKit.print(decodedResponse, 1);
       
       Map<ByteBuffer, Object> responseMap = (Map<ByteBuffer, Object>)decodedResponse;
       Object[] responseArray = responseMap.values().toArray();
       
       //the interval returned by the tracker
       int interval = (Integer)responseArray[0];
       
       //the list of peers returned by the tracker
       Object peers = responseArray[1];
       
       ArrayList<Object> peerArray = (ArrayList<Object>)peers;
       
       //iterate through the list
       for (Object peer : peerArray){
         String ip_ = "";
         String peer_id_ = "";
         int port_ = 0;
         
         Map<ByteBuffer, Object> peerMap = (Map<ByteBuffer, Object>)peer;
         
         //get all the properties
         for (Map.Entry<ByteBuffer, Object> entry : peerMap.entrySet()){          
            String key = Helpers.bufferToString(entry.getKey());
            Object value = entry.getValue();
            if (key.compareTo("ip") == 0){
              ip_ = Helpers.bufferToString((ByteBuffer)value);
            }
            if (key.compareTo("peer id") == 0){
              peer_id_ = Helpers.bufferToString((ByteBuffer)value);
            }
            if (key.compareTo("port") == 0){
              //TODO: this sometimes throws an error, for god knows why: java.nio.HeapByteBuffer cannot be cast to java.lang.Integer
              port_ = (Integer)value;
           }
         }
         //add the fleshed out peer to the peerList
         Peer newPeer = new Peer(peer_id_, ip_, port_);
         peerList.add(newPeer);
       }
     } catch (Exception e){
       System.out.print(e);
     }
 
     return peerList;
 	}
 	
 	//construct the url for initially querying the tracker
 	public static String constructQuery(int port, int uploaded, int downloaded, int left){
 	  String url_string = "";
 	  try{
 	    
       String escaped_hash = Helpers.toURLHex(INFO_HASH);
       String ip = "128.6.5.130";
       url_string =  TORRENT_INFO.announce_url.toString()
                     + "?port=" + port
                     + "&peer_id=" + PEER_ID
                     + "&info_hash=" + escaped_hash 
                     + "&uploaded=" + uploaded
                     + "&downloaded=" + downloaded
                     + "&left=" + left
                     + "&ip=" +  ip;
 
     } catch (Exception e){
       System.out.println(e);
     }
     
     return url_string;
 	}
 
	
 	public static byte[] readTorrent(String torrentFile) {
    StringBuffer buffer = new StringBuffer();
     try {
        FileInputStream fis = new FileInputStream(torrentFile);
        InputStreamReader isr = new InputStreamReader(fis);
        Reader in = new BufferedReader(isr);
        int ch;
        while ((ch = in.read()) > -1) {
                buffer.append((char)ch);
        }
        in.close();
        return buffer.toString().getBytes();
     } catch (IOException e) {
         e.printStackTrace();
         return null;
     }
   }
   
   //sets the peer id every time the program is run
   public static void setPeerId(){
     Random ran = new Random();
     int rand_id = ran.nextInt(5555555 - 1000000 + 1) + 1000000;
     String peer_id_string = "GROUP4AREL33t" + rand_id;
     PEER_ID = peer_id_string.getBytes();
   }
   
   //returns a byte[] consisting of the contents at the given url
   public static byte[] getURL(String string_url) {
     ByteArrayOutputStream bais = new ByteArrayOutputStream();
     InputStream is = null;
     try {
       URL url = new URL(string_url);
       is = url.openStream();
       byte[] byteChunk = new byte[4096];
       int n;
 
       while ( (n = is.read(byteChunk)) > 0 ) {
         bais.write(byteChunk, 0, n);
       }
       
       is.close();
     }
     catch (IOException e) {
     }
     
     return bais.toByteArray();
   }
 }
 
