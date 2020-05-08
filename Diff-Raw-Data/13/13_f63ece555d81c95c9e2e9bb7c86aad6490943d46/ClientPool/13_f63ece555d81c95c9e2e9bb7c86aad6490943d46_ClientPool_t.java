 package stv6.http.pools;
 
 import java.util.LinkedList;
 import java.util.concurrent.Semaphore;
 
 import stv6.http.Client;
 
 /**
  * A client pool should allow for multiple, threaded
  * 	clients, without the risk of over-running Java's
  * 	thread limit. Because, really, we probably don't
  * 	need a separate thread for each client
  * 
  * For now, we'll be lazy
  * 
  * @author dhleong
  *
  */
 public class ClientPool implements ClientList {
     
     
 	private final class ClientThread implements Runnable {
 
 	    private final boolean mRunning = true;
 
         @Override
         public void run() {
             while (mRunning) {
                 try {
                     mClientsWaiting.acquire();
                 } catch (final InterruptedException e) {
                     // we shouldn't typically get interrupted,
                     //  so probably we just want to stop
                     break;
                 }
                 
                 Client current = mClients.removeFirst();
                 
                 // process 
                 current.process();
             }
         }
 
     }
 
     /**
 	 * Max # of kids before reorganizing
 	 */
	public static final int DEFAULT_THREADS = 8;
     private final ClientThread[] mThreads;
     
     private final Semaphore mClientsWaiting = new Semaphore(0);
     private final LinkedList<Client> mClients = new LinkedList<Client>();
 	
 	public ClientPool() {
 	    this(DEFAULT_THREADS);
 	}
 	
 	public ClientPool(int threadCount) {
 	    mThreads = new ClientThread[threadCount];
 
         for (int i=0; i<threadCount; i++) {
             mThreads[i] = new ClientThread();
             // go ahead and start the thread
             final Thread t = new Thread(mThreads[i], "ClientPoolThread-" + i);
             t.setDaemon(true); // daemon thread please
             t.start(); // gogogo
         }
 	}
 	
 	@Override
     public boolean add(Client c) {
 	    mClients.addFirst(c);
 
         // v-up
         mClientsWaiting.release();
         return true;
 	}
 
 }
