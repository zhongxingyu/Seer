 package ReactorEE.test;
 
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 import java.io.StreamCorruptedException;
 
 import org.junit.Test;
 
 import ReactorEE.Networking.SocketUtil;
 import ReactorEE.model.Plant;
 import ReactorEE.simulator.PlantController;
 import ReactorEE.simulator.ReactorUtils;
 
 public class SocketUtilTest {
 
 	@Test
 	public void test() throws StreamCorruptedException, ClassNotFoundException, IOException {
 		ReactorUtils r = new ReactorUtils();
 		PlantController pc = new PlantController(r);
 		Plant p = pc.getPlant();
 		
 		byte[] ba = SocketUtil.toBypeArray(p);
 		
 		Plant p2 = (Plant) SocketUtil.fromByteArray(ba);
 		
		boolean b = p.equals(p2);
 		assertEquals(p, p2);
 		
 		
 	}
 
 }
