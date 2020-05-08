 package entropia.clubmonitor;
 
 import java.util.concurrent.TimeUnit;
 
import org.junit.Assert;
 
 import org.junit.Test;
 
 public class SyncServiceTest {
 
     private static final long ONE_DAY = TimeUnit.DAYS.toMillis(1);
     private static final long TEN_SECONDS = TimeUnit.SECONDS.toMillis(10);
     private static final long ONE_SECOND = TimeUnit.SECONDS.toMillis(1);
     
     private static class Waiter extends Thread {
 	private final long timeout;
 	private final SyncService syncService;
 	transient boolean wasInterrupted = false;
 	
 	public Waiter(final long timeout, final SyncService syncService) {
 	    this.timeout = timeout;
 	    this.syncService = syncService;
 	}
 	
 	@Override
 	public void run() {
 	    try {
 		syncService.sleepUntilEvent(timeout);
 	    } catch (InterruptedException e) {
 		this.wasInterrupted = true;
 	    }
 	}
     }
     
     @Test
     public void testSyncService() throws InterruptedException {
 	final SyncService syncService = new SyncService();
 	final Waiter w = new Waiter(ONE_DAY, syncService);
 	long before = System.currentTimeMillis();
 	w.start();
 	syncService.forceUpdate();
 	w.join(TEN_SECONDS);
 	long after = System.currentTimeMillis();
 	Assert.assertFalse(w.wasInterrupted);
 	Assert.assertTrue(after - before < ONE_SECOND);
     }
     
     @Test
     public void testSyncService2() throws InterruptedException {
 	final SyncService syncService = new SyncService();
 	final Waiter w = new Waiter(ONE_SECOND, syncService);
 	long before = System.currentTimeMillis();
 	w.start();
 	w.join(TEN_SECONDS);
 	long after = System.currentTimeMillis();
 	Assert.assertFalse(w.wasInterrupted);
 	Assert.assertTrue(after - before >= ONE_SECOND);
     }
     
 }
