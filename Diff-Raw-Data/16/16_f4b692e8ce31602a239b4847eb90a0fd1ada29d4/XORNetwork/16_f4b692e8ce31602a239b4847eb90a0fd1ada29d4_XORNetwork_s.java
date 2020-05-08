 package NeurophExamples;
 
 import static neural.test.Assert.assertEqualWithin;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 
 import neural.Network;
 import neural.ScriptParser;
 import neural.networks.neuroph.FeedForwardNetwork;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.Before;
 import org.junit.Test;
 
 public class XORNetwork { 
 
 	private ScriptParser parser;
 	
 	private String getNetworkScript(String activation) {
 		return "network basic_net is feedforward {\n" + "    layer {\n" + "        activation input\n"
 				+ "        size 2\n" + "        biased\n" + "    }\n	 " + "    layer {\n" + "        activation "
 				+ activation + "\n" + "        size 3\n" + "        biased \n" + "    }\n" + "    layer {\n"
 				+ "        activation sigmoid\n" + "        size 1\n" + "    }\n" + "}\n";
 	}
 
 	private String training_script = "training {\n" + "  type resilient_propagation\n" + " restart 5 error 0.1%\n"
 			+ "  input {\n" + "    0.0 0.0,\n" + "    0.0 1.0,\n" + "    1.0 0.0,\n" + "    1.0 1.0\n" + "  }\n"
 			+ "  output {\n" + "    0.0,\n" + "    1.0,\n" + "    1.0,\n" + "    0.0,\n" + "  }\n" + "  \n" + "}";
 
 	@Before
 	public void initParser() {  
 		parser = new ScriptParser();
 		parser.setUnderlyingLibrary("neural.networks.NeurophNetworkFactory");
 	}
 	
 	@Test
 	public void createNetwork() {
 		Network network = parser.parseScript(getNetworkScript("sigmoid") + training_script);
 		assertNotNull(network);
 		assertEquals("feedforward", network.getType()); 
 		assertEquals("basic_net", network.getName());
 	}
 
 	@Test
 	public void testActivation() {
 		trainNetwork("sigmoid");
		//trainNetwork("tanh");
 	}
 	
 	
 	public void testSeperateTraining() {
 		trainNetworkSeperately("sigmoid");
 	}
 
 	private void trainNetwork(String activation) {
 
 
 		Network network = parser.parseScript(getNetworkScript(activation) + training_script);
 		network.train();
 		testXor(network);
 	}
 
 	private void testXor(Network network) {
 		double[][] input = new double[][] { { 0.0, 0.0 }, { 1.0, 0.0 }, { 0.0, 1.0 }, { 1.0, 1.0 } };
 		double[][] output = new double[][] { { 0.0 }, { 1.0 }, { 1.0 }, { 0.0 } };
 		double[] result = new double[1];
 		for (int i = 0; i < 4; i++) {
 			network.compute(input[i], result);
 			System.out.println("v" + i + " input: " + Arrays.toString(input[i]) + " output:" + Arrays.toString(result));
			assertEqualWithin(0.2, output[i], result);
 		}
 	}
 	
 	private void trainNetworkSeperately(String activation) {
 
 		double[][] input = new double[][] { { 0.0, 0.0 }, { 1.0, 0.0 }, { 0.0, 1.0 }, { 1.0, 1.0 } };
 		double[][] output = new double[][] { { 0.0 }, { 1.0 }, { 1.0 }, { 0.0 } };
 		int epochs = 1000;
 		Network network = parser.parseScript(getNetworkScript(activation) + training_script);
 		for (int e=0; e<epochs; e++) {
 		
 			for (int i=0; i<4; i++) {
 				network.train(input[i], output[i]);
 			}
 		
 		}
 		double[] result = new double[1];
 		for (int i = 0; i < 4; i++) {
 			network.compute(input[i], result);
 			System.out.println("v" + i + " input: " + Arrays.toString(input[i]) + " output:" + Arrays.toString(result));
 			//assertEqualWithin(0.1, output[i], result);
 		}
 	}
 	
 	@Test
 	public void testUsingScript() throws IOException {
 		trainUsingScript("scripts/xor-1.neural");
 		trainUsingScript("scripts/xor-2.neural");
 	}
 	
 	private void trainUsingScript(String name) throws IOException {
 		String script = FileUtils.readFileToString(new File(name));
 		Network network = parser.parseScript(script);
 		network.train();
 		testXor(network);
 	}
 
 	@SuppressWarnings("unused")
 	@Before
 	public void infiniTestFix() {
 		FeedForwardNetwork n = new FeedForwardNetwork(); 
 	}
 
 }
