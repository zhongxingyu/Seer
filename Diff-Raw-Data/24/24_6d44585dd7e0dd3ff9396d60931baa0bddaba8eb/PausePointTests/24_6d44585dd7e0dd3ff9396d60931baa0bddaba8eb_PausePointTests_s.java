 package scrollsai;
 
import static org.junit.Assert.*;
 import org.junit.Test;
 
 import com.gmail.ellendar.scrollsai.ui.PausePoint;
 
 public class PausePointTests {
 
 	@Test
 	public void testIsPaused() throws InterruptedException {
 		Thread t1 = new Thread(new Runnable() {
 			public void run() {
 				assertTrue(PausePoint.isPaused());
 			}
 		});
 		
 		PausePoint.pause();
 		t1.start();
 		t1.join();
 	}
 
 	@Test
 	public void testCheckPause() throws InterruptedException {
 		final Thread t1 = new Thread(new Runnable() {
 			public void run() {
 				try {
 					long start = System.currentTimeMillis();
 					PausePoint.check();
 					long end = System.currentTimeMillis();
 					
 					assertTrue(end - start >= 1000);
 					
 				} catch (InterruptedException e) {
					throw new AssertionError("pause point interrupted", e);
 				}
 			}
 		});
 		
 		final Thread t2 = new Thread(new Runnable() {
 			public void run() {
 				
 				try {
 					synchronized(this) {
 						wait(1000);
 					}
 				} catch (InterruptedException e) {
					throw new AssertionError("wait for resume interrupted", e);
 				}
 				
 				PausePoint.resume();
 			}
 		});
 		
 		PausePoint.pause();
 		
 		t1.start();
 		t2.start();
 		
 		t1.join();
 	}
 }
