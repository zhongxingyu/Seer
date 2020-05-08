 package pleocmd;
 
 import static org.junit.Assert.assertArrayEquals;
 import static org.junit.Assert.assertEquals;
 
 import java.io.IOException;
 import java.util.Random;
 
 import org.junit.Test;
 
 import pleocmd.itfc.gui.MainFrame;
 
 public final class StandardInputTest {
 
 	private static final int BUF_SIZE = 16 * 1024;
 	private static final long RAND_SEED = 2921756112651594520L;
 
 	@Test(timeout = 60000)
 	public void testPutAndRead() throws IOException {
 		// prepare
 		final byte buf0[] = new byte[BUF_SIZE];
 		final byte buf1[] = new byte[BUF_SIZE];
 		new Random(RAND_SEED).nextBytes(buf0);
 
 		// ensure we are in GUI mode
 		MainFrame.the();
 		// no need for detailed output
 		Log.setLogDetailed(false);
 
 		// execute twice
 		for (int i = 0; i < 2; ++i) {
 			System.out.println("Starting test loop for StandardInput");
 			StandardInput.the().put(buf0);
 			System.out.println("Put bytes into StandardInput");
			assertEquals(buf0.length, StandardInput.the().available());
 			System.out.println("Checked available bytes");
 			StandardInput.the().read(buf1);
 			System.out.println("Read bytes from StandardInput");
 			assertArrayEquals(buf0, buf1);
 
 			StandardInput.the().close();
 			System.out.println("Closed StandardInput");
 
 			// now available() should no longer block ...
			assertEquals(0, StandardInput.the().available());
 
 			// ... and read should fail
			assertEquals(-1, StandardInput.the().read());
 
 			StandardInput.the().resetCache();
 			System.out.println("Reset StandardInput");
 		}
 		System.out.println("Done testing StandardInput");
 	}
 
 }
