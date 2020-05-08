 package bioGUI;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import bioGUI.trie.Tree;
 
 public class lab4 {
 
 	public static void main(String[] args) {
 
 		trie x = new trie();
 		bioGUI.trie.Tree bob = x.new Tree();
 
 		/*     bob.StringToTree("BANANA$");
 
         System.out.println("Added String");
 
         for(int i = 0; i < bob.leafCount; i++){
         System.out.println(bob.GetSubString(i));
         }
 
         bob.StringToTree("ATTCATT$");
 
         System.out.println("Added String");
 
         for(int i = 0; i < bob.leafCount; i++){
         System.out.println(bob.GetSubString(i));
         }*/
 
 
 		//		repeatDetect.getRepeats("BANANA$");
 
 		//		repeatDetect.getRepeats("ATTCATT$");
 		//        repeatDetect.getRepeats("ATAGAT$");
 		//        repeatDetect.getRepeats("ATTATT$");
 		//		System.out.println(bob.PrintTree());
 
 
 
 	}
 
 	public static class Option1 {
 
 		int repeatLength;
 		int occurences;
 		String sequence;
 		int[] start;
 		int[] stop;
 		double greater;
 	}
 
 	public static class Option2 {
 
 		int occurences;
 		int[] start;
 		int[] stop;
 		double greater;
 	}
 
 	public static ArrayList<Option1> compOp1(ArrayList<String> mFasta, ArrayList<String> mGFF, Integer low, Integer high,
 			Integer foldDifference, Integer upstream) throws Exception {
 		ArrayList<Option1> op1 = new ArrayList<Option1>();
 		int numFiles;
 		Scanner fastaScanner;
 		StringBuilder fastaText;
 		if (mFasta.size() != mGFF.size()) {
 			throw new Exception();
 		} else {
 			numFiles = mFasta.size();
 		}
 		
 		if(high == 0){
 			high = 1000000000;
 		}
 		
 		for (int i = 0; i < numFiles; i++) {
 			System.out.println("i: "+i);
 			//build output for a particular gff/fasta file combination
 			fastaScanner = new Scanner(new File(mFasta.get(i)));
 			fastaText = new StringBuilder();
 			ArrayList<Repeat> rep = new ArrayList<Repeat>();
 
 			String nextLine = "";
 
 			try {
 				fastaScanner.nextLine(); // throw out first line
 				while (fastaScanner.hasNextLine()) {
 					nextLine = fastaScanner.nextLine();
 					fastaText.append(nextLine);
 				}
 			} finally {
 				fastaScanner.close();
 			}
 
 
 			String fastaStr = fastaText.toString();
 
 
 			if (upstream > 0) {
 				Scanner gffScanner = new Scanner(new File(mGFF.get(i)));
 				StringBuilder sb = new StringBuilder();
 
 				while (gffScanner.hasNext()) {
 					Scanner lineS = new Scanner(gffScanner.nextLine());
 
 					String name = lineS.next();
 					String u = lineS.next();
 					String type = lineS.next();
 					Integer start = lineS.nextInt();
 					Integer end = lineS.nextInt();
 					String v = lineS.next();
 					String dir = lineS.next();
 
 
 					if (type.equals("mRNA")) {
 						if (dir.equals("-")) {
 							int newstart = start - upstream - 1;
 							if (newstart < 0) {
 								newstart = 0;
 							}
 							int newend = start - 1;
 
 
 							rep.addAll(repeatDetect.getRepeats(fastaStr.subSequence(newstart, newend).toString()));
 						} else {
 							int newstart = start + 1;
 							int newend = start + upstream;
 
 							rep.addAll(repeatDetect.getRepeats(fastaStr.subSequence(newstart, newend).toString()));
 						}
 					}
 				}
 
 			} else {
 				rep.addAll(repeatDetect.getRepeats(fastaStr));
 			}
 
 
 			rep = repeatDetect.magicSoup(rep, fastaStr, foldDifference);
 			for (Repeat r : rep) {
 				if(r.getText().length() >= low && r.getText().length() <= high){
 					Option1 o1 = new Option1();
 					o1.occurences = r.getNumRepeat();
 					o1.repeatLength = r.getText().length();
 					o1.sequence = r.getText();
 					o1.start = r.getStart();
 					o1.stop = r.getStop();
 					o1.greater = r.getGreater();
 					op1.add(o1);
 				}
 			}
 		}
 		return op1;
 	}
 
 	//
 	public static ArrayList<Option2> compOp2(ArrayList<String> mFasta, ArrayList<String> mGFF, String sequence,
 			Integer foldDifference, Integer upstream) throws Exception {
 		ArrayList<Option2> op2 = new ArrayList<Option2>();
 
 		int numFiles;
 		Scanner fastaScanner;
 		StringBuilder fastaText;
 		if (mFasta.size() != mGFF.size()) {
 			throw new Exception();
 		} else {
 			numFiles = mFasta.size();
 		}
 		for (int i = 0; i < numFiles; i++) {
 			//build output for a particular gff/fasta file combination
 			fastaScanner = new Scanner(new File(mFasta.get(i)));
 			fastaText = new StringBuilder();
 			ArrayList<Repeat> rep = new ArrayList<Repeat>();
 
 			String nextLine = "";
 
 			try {
 				fastaScanner.nextLine(); // throw out first line
 				while (fastaScanner.hasNextLine()) {
 					nextLine = fastaScanner.nextLine();
 					fastaText.append(nextLine);
 				}
 			} finally {
 				fastaScanner.close();
 			}
 
 
 
 			String fastaStr = fastaText.toString();
 
 
 			if (upstream > 0) {
 				Scanner gffScanner = new Scanner(new File(mGFF.get(i)));
 				StringBuilder sb = new StringBuilder();
 
 				while (gffScanner.hasNext()) {
 					Scanner lineS = new Scanner(gffScanner.nextLine());
 
 					String name = lineS.next();
 					String u = lineS.next();
 					String type = lineS.next();
 					Integer start = lineS.nextInt();
 					Integer end = lineS.nextInt();
 					String v = lineS.next();
 					String dir = lineS.next();
 
 
 					if (type.equals("mRNA")) {
 						if (dir.equals("-")) {
 							int newstart = start - upstream - 1;
 							if (newstart < 0) {
 								newstart = 0;
 							}
 							int newend = start - 1;
 
 
							rep.add(repeatDetect.getSingleRepeat(sequence, fastaStr.subSequence(newstart, newend).toString()));
 						} else {
 							int newstart = start + 1;
 							int newend = start + upstream;
 
							rep.add(repeatDetect.getSingleRepeat(sequence, fastaStr.subSequence(newstart, newend).toString()));
 						}
 					}
 				}
 
 			} else {
				rep.add(repeatDetect.getSingleRepeat(sequence, fastaStr));
 			}
 
 
 			rep = repeatDetect.magicSoup(rep, fastaStr, foldDifference);
 			for (Repeat r : rep) {
 				Option2 o2 = new Option2();
 				o2.greater = 0;
 				o2.start = r.getStart();
 				o2.stop = r.getStop();
 				o2.occurences = r.getNumRepeat();
 				o2.greater = r.getGreater();
 				op2.add(o2);
 			}
 
 
 
 		}
 
 
 		return op2;
 	}
 }
