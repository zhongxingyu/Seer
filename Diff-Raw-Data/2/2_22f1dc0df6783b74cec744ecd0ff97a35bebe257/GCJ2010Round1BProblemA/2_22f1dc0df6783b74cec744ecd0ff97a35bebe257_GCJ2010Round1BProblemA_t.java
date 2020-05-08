 import java.util.*;
 import static java.lang.System.out;
 
 /**
  * File Fix-it
  * NOTE: Good design practices are mostly ignored in programming contests.
  * @author DavidJennings
  *
  */
 public class GCJ2010Round1BProblemA {
	private Scanner in = new Scanner(System.in);
 	
 	public static void main(String args[]) {
 		try {
 			(new GCJ2010Round1BProblemA()).goGo();
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void goGo() {
 		int T = in.nextInt();
 		for (int i = 0; i < T; i++) {
 			int N = in.nextInt();
 			int M = in.nextInt();
 			List<String> existingList = new ArrayList<String>();
 			List<String> createList = new ArrayList<String>();
 			for (int j = 0; j < N; j++) {
 				String dir = in.next();
 				existingList.add(dir);
 			}
 			for (int j = 0; j < M; j++) {
 				String dir = in.next();
 				createList.add(dir);
 			}
 			int count = countMkdir(existingList, createList);
 			out.println("Case #" + (i + 1) + ": " + count);
 		}
 	}
 	
 	private int countMkdir(List<String> existingList, List<String> createList) {
 		Node root = new Node();
 		// load the initial directory tree
 		for (String e : existingList) {
 			List<String> dirList = new ArrayList<String>();
 			String[] sArr = e.substring(1).split("/");
 			for (int i = 0; i < sArr.length; i++) {
 				dirList.add(sArr[i]);
 			}
 			root.addNode(dirList);
 		}
 		
 		int totalAdds = 0;
 		// count the created directories from create list
 		for (String e : createList) {
 			List<String> dirList = new ArrayList<String>();
 			String[] sArr = e.substring(1).split("/");
 			for (int i = 0; i < sArr.length; i++) {
 				dirList.add(sArr[i]);
 			}
 			totalAdds += root.addNode(dirList);
 		}
 		return totalAdds;
 	}
 	
 	private class Node {
 		Map<String, Node> map = new HashMap<String, Node>();
 		
 		public int addNode(List<String> dirList) {
 			if (!dirList.isEmpty()) {
 				String firstDir = dirList.remove(0);
 				Node node;
 				int val = 0;
 				if (!map.containsKey(firstDir)) {
 					node = new Node();
 					map.put(firstDir, node);
 					val = 1; // count the entry
 				} else {
 					node = map.get(firstDir);
 				}
 				return val + node.addNode(dirList); // recursively add
 			}
 			return 0;
 		}
 	}
 }
