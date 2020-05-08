 package org.CMCSToolsSet.TestProcesses;
 
 import java.util.ArrayList;
 
 import org.CMCSToolsSet.testAl.CircleHashSpace;
import org.CMCSToolsSet.testAl.NodeInfo;
 
 public class ConsistentHashTest {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		/**
 		 * test case start
 		 */
 		String[] magics = new String[] { "123", "322", "1203", "565", "456",
 				"1000", "4565" };
 
 		String[] magic2s = new String[] { "776", "676", "453", "908", "111" };
 
 		int userMagic = 7000;
		/**
		 * 二维
		 */
		ArrayList<ArrayList<NodeInfo>> values = new ArrayList<ArrayList<NodeInfo>>();
		
		ArrayList<NodeInfo> No1 = new ArrayList<NodeInfo>();
		No1.add(new NodeInfo(0, "123", 2, "127.0.0.1", 1023, "dior", 0));
		No1.add(new NodeInfo(1, "123", 2, "127.0.0.1", 1023, "dior", 0));
 
 		/**
 		 * test case end
 		 */
 		/*
 		 * for(int i = 0 ;i < magics.length; i ++){
 		 * CircleHashSpace.add(magics[i]);
 		 * 
 		 * ArrayList<String> magicArr = new ArrayList<String>();
 		 * 
 		 * for (int i = 0; i < magics.length; i++) { magicArr.add(magics[i]); }
 		 */
 		CircleHashSpace.setMagicArray(magics);
 
 		CircleHashSpace.setMagicArray(magic2s);
 
 		// CircleHashSpace.del("123");
 
 		/*
 		 * for (int i = 0; i < CircleHashSpace.getMagicArray().size(); i++) {
 		 * System.err.println(CircleHashSpace.getMagicArray().get(i)); }
 		 */
 
 		// int userMagic = 200;
 		/*
 		 * System.err.println("I got it! It is " +
 		 * CircleHashSpace.Search4Magic(userMagic));
 		 */
 		/*
 		 * CircleHashSpace.modify("123", "001");
 		 * 
 		 * for(int i = 0; i < CircleHashSpace.getMagicArray().size(); i ++){
 		 * System.err.println(CircleHashSpace.getMagicArray().get(i)); }
 		 */
 	}
 
 }
