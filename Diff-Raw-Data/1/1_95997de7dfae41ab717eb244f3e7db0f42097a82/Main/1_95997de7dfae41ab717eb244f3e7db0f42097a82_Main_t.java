 
 import java.util.*;
 
 /**
  * Dummy class for testing out your code
  */
 public class Main {
 
 	public static void main(String[] args) {
 		int total_errs = 0;
 		try {
 			System.out.println("BEGIN DATABASE CLASS TESTING:\n");
 			//Make sure database is working
 			Database db = Database.getInstance();
 			total_errs =
 				testSurrogates(db) +
 				testNodeAttributes(db) +
 				testAddNode1(db) +	//line added by Guy
                                 testAddNode2(db) +
                                testRemoveNode(db) +
 				testNeighbors(db);
 		} catch (Exception ex) {
 			System.out.println("DB creation failed:\t" + ex.getMessage());
 		}
 		System.out.println("\nTOTAL ERRS = " + total_errs);
 	}
 
 	private static int testSurrogates(Database db) {
 		System.out.println("---- TEST surrogates ----");
 		int errs = 0;
 		try {
 			db.clearDB();
 			if (!db.getSurrogateNeighbors(1).isEmpty()) {
 				System.out.println("Found Surrogate Neighbors when database "
 						+ "was supposed to be empty");
 				errs++;
 			}
 			if (!db.getInverseSurrogateNeighbors(1).isEmpty()) {
 				System.out.println("Found Inverse Surrogate Neighbors when "
 						+ "database was supposed to be empty");
 				errs++;
 			}
 
 			db.addSurrogateNeighbor(4, 1);
 			db.addSurrogateNeighbor(4, 2);
 			db.addSurrogateNeighbor(7, 1);
 			ArrayList<Integer> SurrList = db.getInverseSurrogateNeighbors(1);
 			if (SurrList.size() != 2) {
 				System.out.println("Expected Surrogate Neighbors: 2"
 						+ "\nFound Surrogate Neighbors: " + SurrList.size());
 				errs++;
 			}
 			SurrList = db.getSurrogateNeighbors(4);
 			if (SurrList.size() != 2) {
 				System.out.println("Expected Surrogate Neighbors: 2"
 						+ "\nFound Surrogate Neighbors: " + SurrList.size());
 				errs++;
 			}
 			db.removeSurrogateNeighbor(7, 1);
 			if (SurrList.size() != 2) {
 				System.out.println("Expected Surrogate Neighbors: 1"
 						+ "\nFound Surrogate Neighbors: " + SurrList.size());
 				errs++;
 			}
 		} catch (Exception e) {
 			System.out.println("Exception encountered: " + e.getMessage());
 			errs++;
 		}
 		System.out.println(errs > 0 ? "errors = " + errs : "passed");
 		return errs;
 	}
 
 	private static int testNodeAttributes(Database db) {
 		System.out.println("---- TEST node attibutes ----");
 		int errs = 0, temp;
 		try {
 			db.clearDB();
 			db.addNode(new Node(5));
 			//Height
 			if (!db.setHeight(5, 10)) {
 				errs++;
 			}
 			if (db.getHeight(5) != 10) {
 				System.out.println("Failed to set node height");
 				errs++;
 			}
 			//Fold
 			db.addNode(new Node(6));
 			if (!db.setFold(5, 6)) {
 				errs++;
 			}
 			if (db.getFold(5) != 6) {
 				System.out.println("Failed to set node fold");
 				errs++;
 			}
 			//Surrogate fold
 			if (!db.setSurrogateFold(5, 6)) {
 				errs++;
 			}
 			if (db.getSurrogateFold(5) != 6) {
 				System.out.println("Failed to set surrogate fold");
 				errs++;
 			}
 			if (db.getInverseSurrogateFold(6) != 5) {
 				System.out.println("Failed to set inverse surrogate fold");
 				errs++;
 			}
 		} catch (Exception e) {
 			System.out.println("!! getColumn exception encountered, could not complete tests !!");
 			errs++;
 		}
 		System.out.println(errs > 0 ? "errors = " + errs : "passed");
 		return errs;
 	}
 
 	//@author guy
 	private static int testAddNode1(Database db) {
 		System.out.println("---- TEST add node #1 ----");
 		int errs = 0;
 		try {
 			//fill Node with attributes
 			db.clearDB();
 			Node node = new Node(5);
 			node.setHeight(1);
 			node.setFold(2);
 			node.setSurrogateFold(3);
 			node.setInverseSurrogateFold(4);
 
 			ArrayList<Integer> list1 = new ArrayList();
 			list1.add(6);
 			list1.add(7);
 			list1.add(8);
 
 			ArrayList<Integer> list2 = new ArrayList();
 			list2.add(9);
 			list2.add(10);
 			list2.add(11);
 
 			ArrayList<Integer> list3 = new ArrayList();
 			list3.add(12);
 			list3.add(13);
 			list3.add(14);
 
 			node.setNeighbors(list1);
 			node.setSurrogateNeighbors(list2);
 			node.setInverseSurrogateNeighbors(list3);
 
 			db.addNode(node);
 			//Height
 			if (db.getHeight(5) != 1) {
 				System.out.println("Failed to set node height");
 				errs++;
 			}
 			//Fold
 			if (db.getFold(5) != 2) {
 				System.out.println("Failed to set node fold");
 				errs++;
 			}
 			//Surrogate fold
 			if (db.getSurrogateFold(5) != 3) {
 				System.out.println("Failed to set surrogate fold");
 				errs++;
 			}
 			//Inverse Surrogate fold
 			if (db.getInverseSurrogateFold(5) != 4) {
 				System.out.println("Failed to set inverse surrogate fold");
 				errs++;
 			}
 			List<Integer> list;
 			//Neighbors
 			list = db.getNeighbors(5);
 			for (int i = 0; i < list.size(); i++) {
 				if (list.get(i) != i + 6) {
 					System.out.println("Neighbor list is wrong");
 					errs++;
 				}
 			}
 
 			//Surrogate Neighbors
 			list = db.getSurrogateNeighbors(5);
 			for (int i = 0; i < list.size(); i++) {
 				if (list.get(i) != i + 9) {
 					System.out.println("Surrogate Neighbor list is wrong");
 					errs++;
 				}
 			}
 
 			//Inverse Surrogate Neighbors
 			list = db.getInverseSurrogateNeighbors(5);
 			for (int i = 0; i < list.size(); i++) {
 				if (list.get(i) != i + 12) {
 					System.out.println("Inverse Surrogate Neighbor list is wrong");
 					errs++;
 				}
 			}
 
 		} catch (Exception e) {
 			System.out.println(e);
 			errs++;
 		}
 		System.out.println(errs > 0 ? "errors = " + errs : "passed");
 		return errs;
 	}
 
         //@author Brian
         private static int testAddNode2(Database db) {
 		System.out.println("---- TEST add node #2 ----");
 		int errs = 0;
 		try {
 			//fill Node with attributes
 			db.clearDB();
 			int webID = 9;
                         int height = 1;
                         int fold = 6;
                         int surrogateFold = 2;
                         
 			db.addNode(webID, height, fold, surrogateFold);
 			//Height
 			if (db.getHeight(9) != 1) {
 				System.err.println("Failed to set node height");
 				errs++;
 			}
 			//Fold
 			if (db.getFold(9) != 6) {
 				System.err.println("Failed to set node fold");
 				errs++;
 			}
 			//Surrogate fold
 			if (db.getSurrogateFold(9) != 2) {
 				System.err.println("Failed to set surrogate fold");
 				errs++;
 			}
 			//Inverse Surrogate fold
 			if (db.getInverseSurrogateFold(9) != 9) {
 				System.err.println("Failed to set inverse surrogate fold");
 				errs++;
 			}
                     } catch (Exception e) {
 			System.err.println(e);
 			errs++;
 		}
 		System.out.println(errs > 0 ? "errors = " + errs : "passed");
 		return errs;
 	}
         
         private static int testRemoveNode(Database db){
             System.out.println("---- TEST remove node ----");
 		int errs = 0;
 		try {
 			//fill Node with attributes
 			db.clearDB();
 			int webID = 9;
                         int height = 1;
                         int fold = 6;
                         int surrogateFold = 2;
                         
 			db.addNode(webID, height, fold, surrogateFold);
                         
                         if (!db.removeNode(webID)){
                             System.err.println("Failed to remove node");
                         }
                         
 			//Height
                         try{
                             int fail = db.getHeight(webID);
                             System.err.println("Failed to remove height");
                             errs++;
                         }
                         catch (Exception e){}
 			
 			//Fold
                         try{
                             int fail = db.getFold(webID);
                             System.err.println("Failed to remove height");
                             errs++;
                         }
                         catch (Exception e){}
 			
 			//Surrogate fold
                         try{
                             int fail = db.getSurrogateFold(webID);
                             System.err.println("Failed to remove height");
                             errs++;
                         }
                         catch (Exception e){}
 			
 			//Inverse Surrogate fold
                         try{
                             int fail = db.getInverseSurrogateFold(webID);
                             System.err.println("Failed to remove height");
                             errs++;
                         }
                         catch (Exception e){}
 			
                     } catch (Exception e) {
 			System.err.println(e);
 			errs++;
 		}
 		System.out.println(errs > 0 ? "errors = " + errs : "passed");
 		return errs;
         }
 
 	private static int testNeighbors(Database db) {
 
 		System.out.println("---- TEST neighbors ----");
 		int errs = 0;
 
 		try {
 
 			db.clearDB();
 			db.addNode(new Node(0));
 			db.addNode(new Node(1));
 			db.addNode(new Node(2));
 			db.addNode(new Node(3));
 			db.addNode(new Node(4));
 
 			db.addNeighbor(0, 1);
 			db.addNeighbor(0, 2);
 			db.addNeighbor(1, 3);
 			db.addNeighbor(3, 2);
 
 			if (!db.getNeighbors(0).contains(1) || !db.getNeighbors(0).contains(2) || db.getNeighbors(0).size() != 2) {
 				System.out.println("Doesn't add nodes correctly");
 				errs++;
 			}
 
 			if (!db.getNeighbors(1).contains(0) || !db.getNeighbors(1).contains(3) || db.getNeighbors(1).size() != 2) {
 				System.out.println("Doesn't add nodes correctly");
 				errs++;
 			}
 
 			if (!db.getNeighbors(2).contains(0) || !db.getNeighbors(2).contains(3) || db.getNeighbors(2).size() != 2) {
 				System.out.println("Doesn't add nodes correctly");
 				errs++;
 			}
 
 			if (!db.getNeighbors(3).contains(1) || !db.getNeighbors(3).contains(2) || db.getNeighbors(3).size() != 2) {
 				System.out.println("Doesn't add nodes correctly");
 				errs++;
 			}
 
 			db.removeNeighbor(0, 1);
 			db.removeNeighbor(3, 1);
 
 			if (db.getNeighbors(0).contains(1) || db.getNeighbors(1).contains(0) || db.getNeighbors(0).size() != 1) {
 				System.out.println("Doesn't remove nodes correctly");
 				errs++;
 			}
 
 			if (!db.getNeighbors(1).isEmpty()) {
 				System.out.println("Doesn't remove nodes correctly");
 				errs++;
 			}
 		} catch (Exception e) {
 			System.out.println(e);
 			errs++;
 		}
 
 		System.out.println(errs > 0 ? "errors = " + errs : "passed");
 		return errs;
 	}
 }
