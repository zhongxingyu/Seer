 package aether.repl;
 
 import java.net.SocketException;
 
 import aether.cluster.ClusterMgr;
 import aether.cluster.ClusterTableRecord;
 import aether.io.Chunk;
 
 
 public class Replication {
 	private static Replication repl;
 	public static int HTBT_SND_PORT_NUMBER = 34444;
 	public static int HTBT_RCV_PORT_NUMBER = 34445;
 	public static int NUM_RETRIES = 4;
 	public static int TIMEOUT_BEFORE_DEAD = 10000;	//milliseconds
 	public static int REPL_PORT_LISTENER = 44444;	//port where all nodes listen for file chunk transfer	
 	public static int REPL_MAIN_LISTENER = 34343;	//port where all the read requests arrive from other cluster nodes and clients
 	
 	/*
 	 * 
 	 * The getinstance will be called from ClusterMgr code
 	 * and will start an instance of the Replication class. 
 	 */
 	public static synchronized Replication getInstance () throws SocketException {
 		if (repl == null) {
 			repl = new Replication ();
 		}
 		return repl;
 	}
 	public Replication () throws SocketException{
 		HtbtBuddyMap hbm = HtbtBuddyMap.getInstance();		
 		HtbtSender s = new HtbtSender (hbm);		
 		HtbtReceiver r = new HtbtReceiver ();
 		ReplicationListener rl = ReplicationListener.getInstance();
		ChunkSpaceMap csm = ChunkSpaceMap.getInstance();
 		ChunkManager cm = ChunkManager.getInstance();
 		FileChunkMetadata fcm = new FileChunkMetadata ();
 		new Thread(s).start();
 		new Thread(r).start();
 		new Thread(rl).start();
 		new Thread(cm).start();
 		new Thread(csm).start();
 	}
 	
 	
 	 
 	
 	
 
 	public void run () {
 		/*try {
 			DatagramSocket s = new DatagramSocket (Replication.REPL_MAIN_LISTENER, NetMgr.getLocalIp());
 			CD cd = CD.getInstance();
 			while (!Thread.currentThread().isInterrupted()) {
 				byte[] buffer = new byte[1024];
 				DatagramPacket dpr = new DatagramPacket (buffer, buffer.length);
 				NetMgr mgr = new NetMgr (29298);				
 				Message m = mgr.receive();
 				String fileName = m.toString();
 				ArrayList<Integer> chunkIds = cd.getChunkIDForFile(fileName);
 				byte[] sendbuf = new byte[2048];
 				//DatagramPacket dps = new DatagramPacket (chunkIds.toString());
 				//s.send(dps);
 			}
 		} catch (SocketException e) {
 
 			e.printStackTrace();
 		} catch (IOException e) {
 
 			e.printStackTrace();
 		}*/	
 	}
 	/*public Replication () {
 		Chunk[] chunks = new Chunk[4];
 		String test = "This is a test : Devesh and Heramb";
 		System.out.println("This is a test : Devesh and Heramb");
 		chunks[0] = new Chunk ("filename.txt", 5, test.getBytes());
 		chunks[1] = new Chunk ("filename.txt", 6, test.getBytes());
 		chunks[2] = new Chunk ("filename.txt", 8, test.getBytes());
 		chunks[3] = new Chunk ("filename.txt", 9, test.getBytes());
 		//this.WriteChunks(chunks);
 		FileChunkMetadata fcm = FileChunkMetadata.getInstance();
 		fcm.addChunk("filename.txt", new ChunkMetadata (chunks[0]));
 		fcm.addChunk("filename.txt", new ChunkMetadata (chunks[1]));
 		fcm.addChunk("filename.txt", new ChunkMetadata (chunks[2]));
 		fcm.addChunk("filename.txt", new ChunkMetadata (chunks[3]));
 		try {
 			FileOutputStream fos = new FileOutputStream (new File ("filename.txt5"));
 			ObjectOutputStream oos = new ObjectOutputStream (fos);
 			oos.writeObject(chunks[0]);
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}*/
 	
 	/*public static void main (String[] args) {
 		Replication r = new Replication ();
 		
 		Chunk[] chunks = new Chunk[4];
 		String test = "This is a test : Devesh and Heramb";
 		System.out.println("This is a test : Devesh and Heramb");
 		chunks[0] = new Chunk ("filename.txt", 5, test.getBytes());
 		chunks[1] = new Chunk ("filename.txt", 6, test.getBytes());
 		chunks[2] = new Chunk ("filename.txt", 8, test.getBytes());
 		chunks[3] = new Chunk ("filename.txt", 9, test.getBytes());
 		r.WriteChunks(chunks);
 		
 		//ChunkManager cm = ChunkManager.getInstance();
 		//new Thread(cm).start();
 		//r.getChunk("filename.txt", "filename.txt5");
 		 
 	}*/
 	 
      
       public Integer[] getChunkIds(String file){
     	  Integer[] chunks = null;
     	  FileChunkMetadata fcm = FileChunkMetadata.getInstance();
     	  chunks = fcm.getChunkIds(file);
     	  return chunks;
       }
       
       /*
 	   * This function performs the write functionality
 	   * It is responsible for the finding free space on the cluster nodes 
 	   * and writing the data chunks*/
       public void WriteChunks(Chunk[] chunks){
     	  ChunkManager cm = ChunkManager.getInstance();
     	  try {
 			cm.addToQueue(chunks);
 		} catch (InterruptedException e) {
 			System.out.println("Could not add the chunks from array to queue");
 			e.printStackTrace();
 		}
     	  
       }
       /*
        * This function provides an API to the caller who might 
        * want a chunk from a particular node 
        * */
       public Chunk getChunk (String f, String c) {
     	  Chunk chunk;
     	  FileChunkMetadata fcm = FileChunkMetadata.getInstance();
     	  chunk = fcm.retrieveChunk(f, c);
     	  return chunk;
       }
       
       
       /**
        * This function imports the entries from cluster manager 
        * into the buddy map. This is further used to maintain 
        * heartbeat from current node to each other node in 
        * the data structure. 
        * */
       public void importClusterRecs () {
     	  ClusterMgr cm;
 		try {
 			cm = ClusterMgr.getInstance();
 			ClusterTableRecord[] ctr = cm.getClusterTableRecords();
 			HtbtBuddyMap hbm = HtbtBuddyMap.getInstance();
 			for (int i = 0; i < ctr.length; i++) {
 				hbm.put(new Host (ctr[i].getNodeIp(), Replication.HTBT_SND_PORT_NUMBER), null);
 			}
 			
 		} catch (UnsupportedOperationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SocketException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     	  
       }
 	/**
 	 * @param args
 	 */
 	//public static void main(String[] args) {
 
 		/*HtbtBuddyMap hbm = new HtbtBuddyMap ();
 		try {
 			//hbm.put(new Host (InetAddress.getLocalHost(), Replication.HTBT_RCV_PORT_NUMBER), null);
 			hbm.put(new Host (InetAddress.getByName("192.168.0.10"), Replication.HTBT_RCV_PORT_NUMBER), null);
 			System.out.println(InetAddress.getLocalHost());
 		} catch (UnknownHostException e) {
 
 			e.printStackTrace();
 		}
 		
 		HtbtSender s = new HtbtSender (hbm);
 		new Thread(s).start();
 		
 		HtbtReceiver r = new HtbtReceiver();
 		new Thread(r).start();
 
 		*/
 		/*create the required data structures*/
 		
 	//}
 }
 
 
 
