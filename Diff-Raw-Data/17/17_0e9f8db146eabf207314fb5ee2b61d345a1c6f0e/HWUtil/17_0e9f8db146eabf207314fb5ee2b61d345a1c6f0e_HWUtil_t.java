 /**
 	@authors: Alex Clavelle, Kelsey Cameron
 Description: Creates a K-ary heap where K is the maximum number of children for a non-leaf node.
  */
 import java.util.*;
 import java.io.*;
 
 public class HWUtil {
 	List<String> input;
 
 	public static void main(String[] args) {
		//SUPPLY A FILEPATH TO THE INPUT ON THE COMMAND LINE!!!!!
 		String filepath = args[0];
 		HWUtil inp = new HWUtil();
 		inp.loadfile(filepath);
 		for (int i = 2; i <= 10; i += 2) {
 			System.out.println("For k=" + i + " children");
 			KHeap k = new KHeap(i);
 			double start = System.nanoTime();
 			for (String s : inp.input) {
 				inp.opRunner(s, k);
 			}
 			double end = System.nanoTime();
 			double elapsed = (end-start)/1000.0;
 			System.out.println(elapsed + " micro-sec \n");
 		}
 
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
 
 	public void opRunner(String s, KHeap k) {
 		String[] op = s.split(" ");
 		if (op[0].compareTo("IN") == 0) {
 			k.insert(Integer.parseInt(op[1]));
 		}
 		if (op[0].compareTo("EX") == 0) {
 			Node n = k.extractMin();
 			System.out.println(n);
 		}
 	}
 
 }
