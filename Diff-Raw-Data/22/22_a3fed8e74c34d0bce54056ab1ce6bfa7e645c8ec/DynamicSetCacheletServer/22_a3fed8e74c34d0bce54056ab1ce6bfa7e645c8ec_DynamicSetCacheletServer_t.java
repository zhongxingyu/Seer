 package nimbus.server;
 
 import java.util.Iterator;
 
 import org.apache.log4j.Logger;
 import nimbus.nativestructs.CSet;
 
 public class DynamicSetCacheletServer extends ICacheletServer implements
 		Iterable<String> {
 
 	private CSet set = new CSet();
 	private static final Logger LOG = Logger
 			.getLogger(DynamicSetCacheletServer.class);
 
 	@Override
 	protected ICacheletWorker getNewWorker() {
 		return new DynamicSetCacheletWorker(this);
 	}
 
 	private class StatusThread implements Runnable {
 
 		@Override
 		public void run() {
 			while (true) {
 				LOG.info("Set size\t" + set.size());
 				try {
 					Thread.sleep(5000);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	@Override
 	public void run() {
 		openServer();
 
 		Thread t = new Thread(new StatusThread());
 		t.start();
 
 		acceptConnections();
 	}
 
 	public DynamicSetCacheletServer(String cacheName, String cacheletName,
 			int port, CacheType type) {
 		super(cacheName, cacheletName, port, type);
 	}
 
	public synchronized boolean add(String element) {
 		return set.add(element);
 	}
 
 	public synchronized void clear() {
 		set.clear();
 	}
 
 	public synchronized boolean contains(String element) {
 		return set.contains(element);
 	}
 
 	public synchronized boolean isEmpty() {
 		return set.isEmpty();
 	}
 
 	@Override
 	public Iterator<String> iterator() {
 		return set.iterator();
 	}
 
 	public boolean remove(String element) {
 		return set.remove(element);
 	}
 
 	public synchronized int size() {
 		return set.size();
 	}
 }
