 import java.io.*;
 import java.util.*;
 
 public class Classifier {
 	Trainer known;
 

	
 	public static int getClass(Trainer t, File f) {
 		return Utils.maxInd(scoreClass(t, f));
 	}
 	
 	private static List<String> getTokens(File f) {
 		List<String> tokens = new ArrayList<String>();
 		
 		try {
 	        Scanner s = new Scanner(f);
 	        
 	        while(s.hasNextLine()) {
 	        	for(String token : s.nextLine().split(" ")) {
 	        		tokens.add(token);
 	        	}
 	        }
 		} catch(FileNotFoundException fe) {
 			fe.printStackTrace();
 		}
         return tokens;
 	}
 	
 	private static Double[] scoreClass(Trainer t, File f) {
		List<String> tokens = getTokens(f);
 		
 		Double[] score = new Double[t.prior.length];
 		for(int i = 0; i < t.prior.length; i++)
 			score[i] = Math.log(t.prior[i]);
 		
		for(String token : tokens) {
			for(int i = 0; i < t.CLASSES.length; i++) {
 				if(t.vocab.containsKey(token)) { 
					score[i] += Math.log(t.vocab.get(token)[i]);
 				}
 			}
 		}
 		return score;
 	}
 }
