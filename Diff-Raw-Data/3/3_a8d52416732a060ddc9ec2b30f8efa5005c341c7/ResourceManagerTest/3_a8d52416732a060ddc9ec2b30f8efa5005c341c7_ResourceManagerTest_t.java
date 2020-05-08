 package ch17.ex04;
 
 import static org.junit.Assert.*;
 
 
 import org.junit.Test;
 
 public class ResourceManagerTest {
 	@Test
 	public void successTest() throws InterruptedException {
 		ResourceManager obj = new ResourceManager();
 		Key[] keys = new Key[10];
 		for (int i = 0; i < 10; i++) {
 			keys[i] = new Key(i);
 		}
 		
 		for (int i = 0; i < 10; i++) {
 			Resource res = obj.getResource(keys[i]);
 			res.use(keys[i], new Integer(i));
 			keys[i] = null;
 			res.release();
			Thread.sleep(100); //Sleepを挿入しないと残ってしまう。。
 		}
 		
 		Runtime.getRuntime().gc();
 		obj.shutdown();
 		assertEquals(obj.refs.size(), 0);
 	}
 }
