 package examples;
 
 import types.FlowData;
 import engine.Engine;
 
 public class mainTester {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		Engine engine = new Engine("src\\generated\\flow.xml");
 		FlowData data = engine.run();
 	} 
 }
