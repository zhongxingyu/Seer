 package pleocmd.pipe.data;
 
 import static org.junit.Assert.assertArrayEquals;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertSame;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.junit.Test;
 
 import pleocmd.Log;
 import pleocmd.pipe.val.Value;
 
 public final class DataQueueTest {
 
 	private static final int BUF_SIZE = 1024;
 	private static final long RAND_SEED = 928215371469291872L;
 
 	@Test(timeout = 60000)
 	public void testPutAndRead() throws IOException, InterruptedException {
 		// no need for detailed output
 		Log.setLogDetailed(false);
 
 		// prepare
 		final Data buf0[] = new Data[BUF_SIZE];
 		final Data buf1[] = new Data[BUF_SIZE];
 		for (int i = 0; i < BUF_SIZE; ++i)
 			buf0[i] = new Data(new ArrayList<Value>(0), null);
 		final DataQueue queue = new DataQueue();
 
 		// execute twice
 		for (int i = 0; i < 2; ++i) {
 			System.out.println("Starting test loop for DataQueue");
 			for (final Data data : buf0)
 				queue.put(data);
 			System.out.println("Put Data into DataQueue");
 			for (int j = 0; j < BUF_SIZE; ++j)
 				buf1[j] = queue.get();
 			System.out.println("Read Data from DataQueue");
 			assertArrayEquals(buf0, buf1);
 
 			queue.close();
 			System.out.println("Closed DataQueue");
 
 			// now get should fail
 			assertNull(queue.get());
 
 			queue.resetCache();
 			System.out.println("Reset DataQueue");
 		}
 
 		// now again with some higher and lower priority objects
 		for (final Data data : buf0)
 			queue.put(data);
 		Data d = createData(1);
 		queue.put(d);
		assertSame(d, queue.get());
 		System.out.println("Tested High-Priority Data in DataQueue");
 
 		queue.resetCache();
 		System.out.println("Reset DataQueue");
 
 		// now again with some higher and lower priority objects
 		for (final Data data : buf0)
 			queue.put(data);
 		d = createData(-1);
 		queue.put(d);
		assertSame(buf0[0], queue.get());
 		System.out.println("Tested Low-Priority Data in DataQueue");
 
 		System.out.println("Done testing DataQueue");
 	}
 
 	private Data createData(final int prioDelta) {
 		return new Data(new ArrayList<Value>(0), null,
 				(byte) (Data.PRIO_DEFAULT + prioDelta), Data.TIME_NOTIME);
 	}
 
 }
