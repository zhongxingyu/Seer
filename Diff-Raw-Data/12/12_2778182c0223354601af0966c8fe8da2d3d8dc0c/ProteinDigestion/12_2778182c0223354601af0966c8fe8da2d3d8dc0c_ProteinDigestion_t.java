 package Peppy;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import Utilities.U;
 
 /**
  * The contract here is that all lists of peptides must be returned
  * sorted by mass, from least to greatest.
  * @author Brian Risk
  *
  */
 public class ProteinDigestion {
 	
 	public static ArrayList<Peptide> getPeptidesFromDatabase(File proteinFile) {
 		return getPeptidesFromDatabase(proteinFile, false);
 	}
 	
 	/**
 	 * This method, right here, it's for reverse database searches for things like
 	 * False Positive Rates and the like.  So, only used for stats purposes, not
 	 * "real" searches
 	 * @param proteinFile
 	 * @return
 	 */
 	public static ArrayList<Peptide> getPeptidesFromReverseDatabase(File proteinFile) {
 		return getPeptidesFromDatabase(proteinFile, true);
 	}
 	
 	/**
 	 * Depending on the file suffix of the protein file it chooses how to extract the proteins
 	 * @param proteinFile a FASTA or DAT formatted file
 	 * @param isReverse
 	 * @return
 	 */
 	private static ArrayList<Protein> getProteinsFromDatabase(File proteinFile, boolean isReverse) {
 		if (proteinFile.getName().toLowerCase().endsWith("fa")) return getProteinsFromFASTA(proteinFile, isReverse);
 		if (proteinFile.getName().toLowerCase().endsWith("fsa")) return getProteinsFromFASTA(proteinFile, isReverse);
 		if (proteinFile.getName().toLowerCase().endsWith("fasta")) return getProteinsFromFASTA(proteinFile, isReverse);
 		if (proteinFile.getName().toLowerCase().endsWith("dat")) return getProteinsFromUniprotDAT(proteinFile, isReverse);
 		return null;
 	}
 	
 	public static ArrayList<Protein> getProteinsFromDatabase(File proteinFile) {
 		return getProteinsFromDatabase(proteinFile, false);
 	}
 	
 	public static ArrayList<Peptide> getPeptidesFromDatabase(File proteinFile, boolean isReverse) {
 		ArrayList<Protein> proteins = getProteinsFromDatabase(proteinFile, isReverse);
 		return getPeptidesFromListOfProteins(proteins);	
 	}
 	
 	public static ArrayList<Peptide> getPeptidesFromListOfProteins(ArrayList<Protein> proteins) {
 		ArrayList<Peptide> peptides = new ArrayList<Peptide>();
 		for (Protein protein: proteins) {
 			peptides.addAll(protein.digest());
 		}
 		Collections.sort(peptides);
 		return peptides;
 	}
 	
 	private static ArrayList<Protein> getProteinsFromFASTA(File proteinFile, boolean isReverse) {
 		ArrayList<Protein> proteins = new ArrayList<Protein>();
 		try {
 			BufferedReader br = new BufferedReader(new FileReader(proteinFile));
 			String line = br.readLine();
 			StringBuffer buffy = new StringBuffer();
 			String proteinName = "";
 			int proteinIndex = 0;
 			while (line != null) {
 				//this symbol means we've reached the beginning of a new protein and
 				//the one we've been working on has ended
 				if (line.startsWith(">")) {
					//make a new protein if we've been building one
					if (buffy.length() > 0) {
						if (isReverse) buffy.reverse();
						proteins.add(new Protein(proteinName, buffy.toString()));
					}
 
 					//this is the name of the next protein
 					proteinName = line.substring(1).trim();
					
 					//try to get the accession number from UniProt databases
 					if (proteinName.startsWith("sp|") || proteinName.startsWith("tr|")) {
						String [] proteinWords = proteinName.split("\\|");
 						proteinName = proteinWords[1];
 					}
 					
 					buffy = new StringBuffer(); 
 					proteinIndex++;
 				} else {
 					buffy.append(line);
 				}
 				line = br.readLine();
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return proteins;
 	}
 	
 	/**
 	 * Uniprot has its own format!  Yes!
 	 * @param proteinFile
 	 * @return
 	 */
 	private static ArrayList<Protein> getProteinsFromUniprotDAT(File proteinFile, boolean reverse) {
 		ArrayList<Protein> proteins = new ArrayList<Protein>();
 		try {
 			BufferedReader br = new BufferedReader(new FileReader(proteinFile));
 			String line = br.readLine();
 			StringBuffer buffy = new StringBuffer();
 			String proteinName = "";
 			boolean inSequence = false;
 			while (line != null) {
 				if (line.startsWith("ID")) {
 					proteinName = "NOT DEFINED";
 					buffy = new StringBuffer(); 
 				}
 				//DR   UCSC; uc002hjd.1; human.
 				if (line.startsWith("AC")) {
 					if (proteinName.equals("NOT DEFINED")) {
 						String [] chunks = line.split(";");
 						proteinName = chunks[0].trim();
 						proteinName = proteinName.substring(5);
 					}
 				}
 				if (line.startsWith("SQ")) {
 					inSequence = true;
 				}
 				//starts with five spaces
 				if (line.startsWith("     ")) {
 					if (inSequence) {
 						char theChar;
 						for (int i = 0; i < line.length(); i++) {
 							theChar = line.charAt(i);
 							if (theChar != ' ') buffy.append(theChar);
 						}
 					}
 				}
 				if (line.startsWith("//")) {
 					inSequence = false;
 					if (reverse) buffy.reverse();
 					proteins.add(new Protein(proteinName, buffy.toString()));
 				}
 				//read a new line
 				line = br.readLine();
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return proteins;
 	}
 	
 	//TODO
 	/**
 	 * DELETE LATER
 	 * this is a quick converter from DAT to FASTA
 	 * @param proteinFile
 	 * @return
 	 */
 	public static void convertDATtoFASTA(File proteinFile) {
 		U.p("loading protein file: " + proteinFile.getName());
 		try {
 			BufferedReader br = new BufferedReader(new FileReader(proteinFile));
 			PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(new File("uniprot_human.fasta"))));
 			String line = br.readLine();
 			StringBuffer buffy = new StringBuffer();
 			String proteinName = "";
 			boolean inSequence = false;
 			while (line != null) {
 				if (line.startsWith("ID")) {
 					proteinName = "NOT DEFINED";
 					buffy = new StringBuffer(); 
 				}
 				//DR   UCSC; uc002hjd.1; human.
 				if (line.startsWith("AC")) {
 					if (proteinName.equals("NOT DEFINED")) {
 						String [] chunks = line.split(";");
 						proteinName = chunks[0].trim();
 						proteinName = proteinName.substring(5);
 					}
 				}
 				if (line.startsWith("SQ")) {
 					inSequence = true;
 				}
 				//starts with five spaces
 				if (line.startsWith("     ")) {
 					if (inSequence) {
 						char theChar;
 						for (int i = 0; i < line.length(); i++) {
 							theChar = line.charAt(i);
 							if (theChar != ' ') buffy.append(theChar);
 						}
 					}
 				}
 				if (line.startsWith("//")) {
 					inSequence = false;
 					pw.println("> " + proteinName);
 					String protein = buffy.toString();
 					for (int i = 0; i < protein.length(); i++) {
 						pw.print(protein.charAt(i));
 						if (((i + 1) % 60 == 0) && i > 1) {
 							pw.print("\r");
 						}
 					}
 					pw.print("\r");
 					
 				}
 				//read a new line
 				line = br.readLine();
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	
 	
 
 }
