package model;

 import java.lang.*;
import java.util.concurrent.ConcurrentSkipListSet;
 
 public class TestPrimerHighlighter {
 
 	private static ConcurrentSkipListSet<Integer> matchSet; 	// to be initialised in main
 													// thread at start of loop
 
 	private class PrimerFinder implements Runnable {
 		
 		private String primer;
 		private String strand;
 		
 		public PrimerFinder(String p, char x) {
 			primer = p;
 			if (x == 'o') strand = oStrand;
 			if (x == 'c') strand = cStrand;
 			else strand = null;
 		}
 		
 		public void run() {
 			for (int i = 0; i < strand.length(); i++) {
 				if (strand.substring(i, (i+primer.length()-1)) == primer)
 					matchSet.add(i);
 				
 				else if (matchSet.contains(i))
 					matchSet.remove(i);
 			}
 		}
 	}
 
 	public static void main(String[] args) {
 		
 		boolean stop = false;
 		String fPrimer = "";
 		String bPrimer = "";
 		matchSet = new ConcurrentSkipListSet<Integer>();
 		int fLast = 0; int bLast = 0; Thread t;
 		while (!stop) {
 			int fCurrent = fPrimer.length();
 			int bCurrent = bPrimer.length();
 			// REMINDER: NEED TO MAKE SURE IT IGNORES NON ATGCs. USE PARSER METHOD?
 			if (fLast != fCurrent) {
				t = new Thread(new PrimerFinder(fPrimer, 'o'));
 				t.start();
 			}
 			if (bLast != bCurrent) {
				t = new Thread(new PrimerFinder(bPrimer, 'c'));
 				t.start();
 			}
 		}
 	}
}
