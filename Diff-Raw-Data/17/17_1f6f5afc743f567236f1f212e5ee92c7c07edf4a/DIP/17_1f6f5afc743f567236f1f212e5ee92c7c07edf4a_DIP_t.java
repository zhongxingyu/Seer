 package com.zylman.protein;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class DIP {
 	
 	private static final Proteins sequences = new Proteins("fasta20101010.seq");
 	
 	private List<PositiveInteraction> interactions = new ArrayList<PositiveInteraction>();
 	
 	DIP(String filePath) {
 		BufferedReader br;
 		
 		try {
 			br = new BufferedReader(new FileReader(filePath));
 			String line;
 			try {
 				line = br.readLine(); // Remove column headings
 				while ((line = br.readLine()) != null) {
 					String[] fields = line.split("\t");
					int split1 = fields[0].indexOf('|');
					int split2 = fields[1].indexOf('|');
					String id1 = split1 != -1 ? fields[0].substring(0, fields[0].indexOf('|')) : fields[0];
					String id2 = split2 != -1 ? fields[1].substring(0, fields[1].indexOf('|')) : fields[1];
					String sequence1 = sequences.get(id1);
					String sequence2 = sequences.get(id2);
					interactions.add(new PositiveInteraction(sequence1, sequence2));
 				}
 			} catch (IOException ex) {
 				System.out.println("IO exception reading DIP: " + ex.getMessage());
 			} 
 		} catch (FileNotFoundException ex) {
 			System.out.println("FNF exception reading DIP: " + ex.getMessage());
 		}
 	}
 	
 	List<PositiveInteraction> getInteractions() {
 		return interactions;
 	}
 }
