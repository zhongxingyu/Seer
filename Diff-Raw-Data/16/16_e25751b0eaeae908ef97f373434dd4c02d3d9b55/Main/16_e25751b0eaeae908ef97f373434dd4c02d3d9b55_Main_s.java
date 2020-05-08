 package com.zylman.protein;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 public class Main {
 	public static void main(String[] args) {
 		
 		try {
 			ProteinDatabase pDb = new ProteinDatabase("localhost", "protein", "root", "qwerty");
 			
 			if (pDb.neededToCreateTables()) {
 				long startTime = new Date().getTime() / 1000;
 				
 				Proteins sequences = new Proteins("fasta20101010.seq");
 				
 				DIP dip = new DIP("dip20111027.txt", sequences);
 				
 				long endTime = new Date().getTime() / 1000;
 				
 				System.out.println("Found " + dip.getInteractions().size() + " interactions comprising "
 						+ sequences.getProteins().size() + " proteins in " + (endTime - startTime) + " seconds"); 
 				
				List<Protein> proteinList = new ArrayList<Protein>(sequences.getProteins());
				
 				startTime = new Date().getTime() / 1000;
				//pDb.addProteinList(sequences.getProteins());
				pDb.addProtein(proteinList.get(0));
 				endTime = new Date().getTime() / 1000;
 				System.out.println("Took " + (endTime + startTime) + " seconds to add the proteins to the database");
 				
 				startTime = new Date().getTime() / 1000;
 				pDb.addInteractionList(dip.getInteractions());
 				endTime = new Date().getTime() / 1000;
 				System.out.println("Took " + (endTime + startTime) + " seconds to add the interactions to the database");
 			}
 			
 		} catch (ProteinException ex) {
 			System.out.println("Protein exception: " + ex.getMessage());
 		}
 	}
 }
