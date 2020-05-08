package mvnp2versionmatch.main;
import mvnp2versionmatch.main.*;

 import junit.framework.TestCase;
 
 public class CommandTest extends TestCase{
 	public CommandTest(String name){
 		super(name);
 	}

 	public void testCommands() throws Exception {
		mvnp2versionmatch.main.Command ca = Command.findByStr("add");
		assertEquals(ca, Command.ADD);
//		assertEquals("add", Command.findByStr("add"));
//		assertEquals("find", Command.findByStr("find"));
//		assertEquals("update", Command.findByStr("update"));
//		assertEquals(null, Command.findByStr("notacommand"));
 	}
 
 }
