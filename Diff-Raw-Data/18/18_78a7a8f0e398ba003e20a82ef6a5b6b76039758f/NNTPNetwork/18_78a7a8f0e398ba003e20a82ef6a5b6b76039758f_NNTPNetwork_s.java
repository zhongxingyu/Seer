 import java.util.*;
 import java.io.*;
 import java.net.*;
 
 
 public class NNTPNetwork extends ArrayList<NNTPConnection>  implements Serializable {
 	
 	private static NNTPNetwork instance = null;
 
 
 	public static NNTPNetwork getInstance() {
 		if (instance == null) {
 			instance = new NNTPNetwork();
 		}
 		return instance;
 	}
 	
 	/*
 	public NNTPNetwork() { super(); }
 	*/
 	
 	public Iterator getIterator() {
 		return super.iterator();
 	}
 	
 	public void connectAll() throws IOException, NNTPConnectionResponseException {
 		for (NNTPConnection n  : this)
 			n.connect();
 	}
 	
 	public void disconnectAll() throws IOException, NNTPUnexpectedResponseException {
 		for (NNTPConnection n  : this)
 			n.disconnect();
 	}
 	
 	public void add (String s, int i) throws UnknownHostException, IOException {
 		NNTPConnection n = new NNTPConnection(s,i);
 		super.add(n);
 	}
 	
 	public boolean connectionActive() {
 		boolean avail = false;
 		synchronized (this) {
 			for (NNTPConnection n  : this)
 				avail = avail | n.isLocked();
 		}
 		return avail;
 	}
 	
 	public boolean connectionAvailable() {
 		boolean avail = false;
 		synchronized (this) {
 			for (NNTPConnection n  : this)
 				avail = avail | !n.isLocked();
 		}
 		return avail;
 	}
 	
 	public NNTPConnection getAvailableConnection() {
 	
 				synchronized (this) {
 				for (NNTPConnection n  : this)
 					if (!n.isLocked()) {
 						// lock connection
 						n.lock();
 						return n;
 					}
 				}
 					return null;
 	
 	}
 	
	public int getNNTParticle (String a , File f) throws IOException, NNTPNoSuchArticleException {
 		// find available connection
		int bytes=-1;
 		
 		while (bytes == -1) {
 			
 			synchronized (this) {
 				for (NNTPConnection n  : this)
 					if (!n.isLocked()) {
 						// lock connection
 						n.lock();
 						try {
 							long ctime = System.currentTimeMillis();
							n.setArticleName(a);
 							//n.setOutputFile(f);
 							bytes = n.writeArticleToFile(f);
 							long etime = System.currentTimeMillis();
 							if (((etime - ctime)/1000)>0) 
 								System.out.println(a + " BPS: " + bytes / ((etime - ctime)/1000));
 							// unlock connection
 							n.unlock();
 						} catch (IOException e) {
 							n.unlock();
 							throw e;
 						} catch (NNTPNoSuchArticleException e) {
 							n.unlock();
 							throw e;
 						}
 					}
 			}
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException ie) { 
 				// like anything going to interrupt us?
 				;
 			}
 		}
 		return bytes;
 	}
 	
 	
 }
