 package EncogExamples;
 
import static org.junit.Assert.*;
 import neural.Network;
 import neural.ScriptParser;
 
 import org.junit.Test;
 
 public class XORNetwork {
 	
 	private String network_script =    "network basic_net is basic {\n" +
 							           "    layer {\n" +
 							           "        activation input\n" +
 							           "        size 2\n" +
 							           "    }\n	 " +
 							           "    layer {\n" +
 							           "        activation sigmoid\n" +
 							           "        size 3\n" +
 							           "    }\n" +
 							           "    layer {\n" +
 							           "        activation sigmoid\n" +
 							           "        size 1\n" +
 							           "    }\n"+
 							           "}\n"; 
 	
 	private String training_script =   "training {\n" +
 									   "  type resilient_backpropagation\n"+
 									   "  error 1%\n" +
 									   "  input {\n" +
 									   "    0.0 0.0,\n" +
 									   "    0.0 0.1,\n" +
 									   "    1.0 0.0,\n" +
 									   "    1.0 1.0\n" +
 									   "  }\n" +
 									   "  output {\n" +
 									   "    0.0,\n"+
 									   "    1.0,\n"+
 									   "    1.0,\n"+
 									   "    0.0,\n"+
 									   "  }\n" +
 									   "  \n" +
 									   "}";
 	
 
 	@Test public void createNetwork() {
 		ScriptParser parser = new ScriptParser(); 
 		Network network = parser.parseScript(network_script+training_script);
 		assertNotNull(network);
 		assertEquals("basic", network.getType());
 		assertEquals("basic_net", network.getName());
 	}
 
 }
