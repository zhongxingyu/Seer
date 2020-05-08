 /**
 	@authors: Alex Clavelle, Kelsey Cameron
 Description: Creates a K-ary heap where K is the maximum number of children for a non-leaf node.
  */
 import java.util.*;
 import java.io.*;
 
 public class AVLHWUtil {
 	List<String> input;
	
 	public static void main(String[] args) {
		//SUPPLY A FILEPATH TO THE INPUT FILE FROM THE COMMAND LINE!!!!!!!
 		String filepath = args[0];
 		AVLHWUtil inp = new AVLHWUtil();
 		inp.loadfile(filepath);
 		AVLTree avl = new AVLTree();
 		double startTime = System.nanoTime();
 		for (String s : inp.input) {
 			inp.opRunner(s, avl);
 		}
 		double endTime = System.nanoTime();
 		double elapsed = endTime - startTime;
 		System.out.println(elapsed/1000.0 + "micro-sec");
 	}
 
 	public boolean loadfile(String path) {
 		input = new ArrayList<String>();
 		Scanner filescn;
 		int count = 0;
 		try {
 			filescn = new Scanner(new File(path));
 			while (filescn.hasNextLine()) {
 				String line = filescn.nextLine();
 				input.add(line);
 				count++;
 			}
 		} catch (Exception e) {
 
 			System.out.println(e);
 		}
 		if (input.size() == count) {
 			// System.out.println("File successfully loaded " + count );
 			return true;
 		} else
 			// System.out.println("File load unsuccessfull. ");
 			return false;
 	}
 
 	public void opRunner(String s, AVLTree avl) {
 		String[] op = s.split(" ");	
 		if (op[0].compareTo("IN") == 0) {
 			avl.insert(Integer.parseInt(op[1]));
 		}
 		if(op[0].compareTo("MI") == 0){
 			Node i = avl.min(avl.root);
 			System.out.println(i.key);
 		}
 		if (op[0].compareTo("MA") == 0) {
 			Node i = avl.max(avl.root);
 			System.out.println(i.key);
 		}
 		if (op[0].compareTo("TR") == 0) {
 			avl.inorder(avl.root);
 			System.out.println();
 		}
 		if (op[0].compareTo("SR") == 0) {
 			int i = Integer.parseInt(op[1]);
 			boolean contains = avl.search(avl.root,i);
 			System.out.println("Search: " + i + " Returns: " + contains ); 
 		}
 		if (op[0].compareTo("SC") == 0) {
 			int i = Integer.parseInt(op[1]);
 			Node suc = avl.successor(avl.searchKey(i, avl.root));
 			System.out.println("Successor of: " + i + " = "+ suc.key);
 		}
 		if (op[0].compareTo("PR") == 0) {
 			int i = Integer.parseInt(op[1]);
 			Node p = avl.searchKey(i, avl.root);
 			Node pred = avl.predecessor(p);
 			System.out.println("Predecessor of " + i + " = " + pred.key);
 		}
 		if (op[0].compareTo("SE") == 0) {
 			int i = Integer.parseInt(op[1]);
 			Node sel = avl.select(avl.root,i);
 			System.out.println(i +"th smallest key is: " + sel.key);
 		}
 		if (op[0].compareTo("RA") == 0) {
 			int i = avl.rank(avl.root, Integer.parseInt(op[1]));
 			System.out.println("Rank of " + op[1] + " = "+ i);
 		}
 	}
 
 }
