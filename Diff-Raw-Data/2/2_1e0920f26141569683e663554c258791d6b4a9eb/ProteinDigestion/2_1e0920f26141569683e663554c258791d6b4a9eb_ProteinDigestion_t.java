 package Peppy;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Collections;
 
 import Utilities.U;
 
 
 public class ProteinDigestion {
 	
 	public static ArrayList<Peptide> getPeptidesFromDatabase(File proteinFile) {
 		if (proteinFile.getName().toLowerCase().endsWith("fa")) return getPeptidesFromFASTA(proteinFile);
 		if (proteinFile.getName().toLowerCase().endsWith("fsa")) return getPeptidesFromFASTA(proteinFile);
 		if (proteinFile.getName().toLowerCase().endsWith("fasta")) return getPeptidesFromFASTA(proteinFile);
 		if (proteinFile.getName().toLowerCase().endsWith("dat")) return getPeptidesFromUniprotDAT(proteinFile);
 		return null;
 	}
 	
 	public static ArrayList<Peptide> getPeptidesFromFASTA(File proteinFile) {
 		ArrayList<Peptide> out = new ArrayList<Peptide>();
 		try {
 			BufferedReader br = new BufferedReader(new FileReader(proteinFile));
 			String line = br.readLine();
 			StringBuffer buffy = new StringBuffer();
 			String proteinName = "";
 			while (line != null) {
 				if (line.startsWith(">")) {
 					out.addAll(getPeptidesFromProteinString(buffy.toString(), proteinName));
					proteinName = line.substring(1).trim();
 					buffy = new StringBuffer(); 
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
 		Collections.sort(out);
 		return out;
 	}
 	
 	/**
 	 * Uniprot has its own format!  Yes!
 	 * @param proteinFile
 	 * @return
 	 */
 	public static ArrayList<Peptide> getPeptidesFromUniprotDAT(File proteinFile) {
 		ArrayList<Peptide> out = new ArrayList<Peptide>();
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
 					out.addAll(getPeptidesFromProteinString(buffy.toString(), proteinName));
 				}
 				//read a new line
 				line = br.readLine();
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		Collections.sort(out);
 		return out;
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
 	
 	public static ArrayList<Peptide> getReversePeptidesFromFASTA(File proteinFile) {
 		ArrayList<Peptide> out = new ArrayList<Peptide>();
 		try {
 			BufferedReader br = new BufferedReader(new FileReader(proteinFile));
 			String line = br.readLine();
 			StringBuffer buffy = new StringBuffer();
 			String proteinName = "";
 			while (line != null) {
 				if (line.startsWith(">")) {
 					String forwards = buffy.toString();
 					StringBuffer reverseBuffer = new StringBuffer();
 					for (int i = forwards.length() - 1; i >=0; i--) {
 						reverseBuffer.append(forwards.charAt(i));
 					}
 					out.addAll(getPeptidesFromProteinString(reverseBuffer.toString(), proteinName));
 					buffy = new StringBuffer(); 
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
 		Collections.sort(out);
 		return out;
 	}
 	
 	
 	//TODO this code needs to reflect DNA digestion e.g. have peptides that both do and don't start with M
 	public static ArrayList<Peptide> getPeptidesFromProteinString(String proteinString, String proteinName) {
 		ArrayList<Peptide> out = new ArrayList<Peptide>();
 		if (proteinString.length() == 0) return out;
 		ArrayList<Peptide> fragments = new ArrayList<Peptide>();
 		StringBuffer buffy = new StringBuffer();
 		boolean cleavage;
 		//starting with the second amino acid
 		for (int i = 1; i < proteinString.length(); i++) {
 			buffy.append(proteinString.charAt(i - 1));
 			cleavage = false;
 			if (proteinString.charAt(i - 1) == 'K') cleavage = true;
 			if (proteinString.charAt(i - 1) == 'R') cleavage = true;
 			if (proteinString.charAt(i) == 'P') cleavage = false;
 			if (proteinString.charAt(i) == 'X') cleavage = true;
 			if (cleavage) {
 				Peptide peptide = new Peptide(buffy.toString(), proteinName);
 				fragments.add(peptide);
 				buffy = new StringBuffer();
 			}
 			if (proteinString.charAt(i) == 'X') {
 				while (proteinString.charAt(i) == 'X') {
 					i++;
 					if (i ==  proteinString.length()) break;
 				}
 				i++;
 			}
 		}
 		
 		//get in the last peptide
 		buffy.append(proteinString.charAt(proteinString.length() - 1));
 		fragments.add(new Peptide(buffy.toString(), proteinName));
 		
 		//add big enough fragments to out
 		for (int i = 0; i < fragments.size(); i++) {
 			Peptide peptide = fragments.get(i);
 			if (peptide.getMass() >= Properties.peptideMassThreshold) out.add(peptide);
 		}
 		
 		//getting all missed cleavages
 		for (int numberOfMissedCleavages = 1; numberOfMissedCleavages <= Properties.numberOfMissedCleavages; numberOfMissedCleavages++){
 			for (int i = 0; i < fragments.size() - numberOfMissedCleavages; i++) {
 				StringBuffer peptideString = new StringBuffer(fragments.get(i).getAcidSequence());
 				for (int j = 1; j <= numberOfMissedCleavages; j++) {
 					peptideString.append(fragments.get(i + j).getAcidSequence());
 				}
 				Peptide peptide = new Peptide(peptideString.toString(),  proteinName);
 				if (peptide.getMass() >= Properties.peptideMassThreshold) out.add(peptide);
 			}
 		}
 		return out;
 	}
 	
 	public static ArrayList<Peptide> digestProtein(
 			String proteinString,
 			String proteinName
 			) {
 		return digestProtein(proteinString, false, null, proteinName, new int[proteinString.length()], -1, -1, true);
 	}
 	
 	public static ArrayList<Peptide> digestProtein(
 			String proteinString,
 			Sequence sequence,
 			int startIndex,
 			boolean isForward) {
 		int [] indicies = new int [proteinString.length()];
 		int index = startIndex;
 		for (int i = 0; i < proteinString.length(); i++) {
 			indicies[i] = index;
 			index += 3;
 		}
 		return digestProtein(proteinString, true, sequence, null, indicies, -1, -1, isForward);
 	}
 	
 	
 	public static ArrayList<Peptide> digestProtein(
 			String proteinString, 
 			boolean isFromSequence, 
 			Sequence geneSequence,
 			String proteinName,
 			int [] indicies,
 			int intronStartIndex,
 			int intronStopIndex,
 			boolean isForward
 			) {
 		ArrayList<Peptide> peptides = new ArrayList<Peptide>();
 		
 		int proteinLength = proteinString.length();
 		int proteinLengthMinusOne = proteinLength - 1;
 		char aminoAcid = proteinString.charAt(0);
 		char previousAminoAcid = aminoAcid;
 		int acidIndex = -1;
 		
 		
 		//Where we store all of our forming peptides
 		ArrayList<PeptideUnderConstruction> peptidesUnderConstruction = new ArrayList<PeptideUnderConstruction>();
 		//start the first peptide
 		peptidesUnderConstruction.add(new PeptideUnderConstruction(indicies[0], aminoAcid));
 		
 		//keeping track of open reading frames within this sequence
 		boolean inORF = false;
 		
 		//start 1 out so we have a previous amino acid
 		for (int i = 1; i < proteinLength; i++) {
 			aminoAcid = proteinString.charAt(i);
 			acidIndex = indicies[i];
 			for (PeptideUnderConstruction puc: peptidesUnderConstruction) {
 				puc.addAminoAcid(aminoAcid);
 			}
 			if ((isStart(aminoAcid)) ||  // start a new peptide at M
 				(isStart(previousAminoAcid) && !isStart(aminoAcid)) || // handle possible N-terminal methionine truncation products
 				(isBreak(previousAminoAcid) && !isStart(aminoAcid)))  // Create new peptides after a break, but only if we wouldn't have created a new one with M already
 			{		
 				peptidesUnderConstruction.add(new PeptideUnderConstruction(acidIndex, aminoAcid));
 			}
 			
 			//adding peptides to the grand list
 			if (isStart(aminoAcid)) {
 				inORF = true;
 			} else {
 				if (isBreak(aminoAcid)) {
 					for (PeptideUnderConstruction puc: peptidesUnderConstruction) {
 						evaluateNewPeptide(
 								puc,
 								intronStartIndex,
 								intronStopIndex,
 								acidIndex,
 								isFromSequence,
 								isForward,
 								geneSequence,
 								proteinName,
 								inORF,
 								peptides);
 					}
 				}
 				if (isStop(aminoAcid)) inORF = false;
 			}
 			
 			//if stop, then clear out
 			if (isStop(aminoAcid)) {
 				peptidesUnderConstruction = new ArrayList<PeptideUnderConstruction>();
 			}
 			
 			//remove all peptide under construction that have reached their maximum break count
 			int size = peptidesUnderConstruction.size();
 			for (int pucIndex = 0; pucIndex < size; pucIndex++) {
 				PeptideUnderConstruction puc = peptidesUnderConstruction.get(pucIndex);
 				if (puc.getBreakCount() > Properties.numberOfMissedCleavages) {
 					peptidesUnderConstruction.remove(pucIndex);
 					pucIndex--;
 					size--;
 				}
 			}
 			
 			//skip X sequences
 			if (proteinString.charAt(i) == 'X') {
 				while (proteinString.charAt(i) == 'X') {
 					i++;
 					if (i ==  proteinString.length()) break;
 				}
 				i++;
 			}
 		}
 		
 		//adding all the remaining peptides under construction
 		for (PeptideUnderConstruction puc: peptidesUnderConstruction) {
 			evaluateNewPeptide(
 					puc,
 					intronStartIndex,
 					intronStopIndex,
 					acidIndex,
 					isFromSequence,
 					isForward,
 					geneSequence,
 					proteinName,
 					inORF,
 					peptides);
 		}
 		return peptides;
 	}
 	
 	/**
 	 * This first creates a peptide from the peptide under construction.
 	 * This is mildly complicated as the peptide has different constructors
 	 * depending on if it comes form a protein database or nucleotide sequence
 	 * @param puc
 	 * @param intronStartIndex
 	 * @param intronStopIndex
 	 * @param acidIndex
 	 * @param isFromSequence
 	 * @param isForward
 	 * @param geneSequence
 	 * @param proteinName
 	 * @param inORF
 	 * @param peptides
 	 */
 	private static void evaluateNewPeptide(
 			PeptideUnderConstruction puc,
 			int intronStartIndex,
 			int intronStopIndex,
 			int acidIndex,
 			boolean isFromSequence,
 			boolean isForward,
 			Sequence geneSequence,
 			String proteinName,
 			boolean inORF,
 			ArrayList<Peptide> peptides
 			) {
 		boolean isSpliced = false;
 		int peptideIntronStartIndex;
 		int peptideIntronStopIndex;
 		Peptide peptide;
 		
 		//splice related
 		isSpliced =  (puc.getStartIndex() < intronStartIndex && acidIndex > intronStopIndex);
 		if (isSpliced) {
 			peptideIntronStartIndex = intronStartIndex;
 			peptideIntronStopIndex = intronStopIndex;
 		} else {
 			peptideIntronStartIndex = -1;
 			peptideIntronStopIndex = -1;
 		}
 		//If this is coming from DNA or RNA, there is a different peptide constructor
 		if (isFromSequence) {
 			peptide = new Peptide(
 					puc.getSequence(),
 					puc.getStartIndex(),
 					acidIndex,
 					peptideIntronStartIndex,
 					peptideIntronStopIndex,
 					isForward,
 					geneSequence,
 					isSpliced);
 		} else {
 			peptide = new Peptide(
 					puc.getSequence(),
 					proteinName);
 		}
 		//add peptide if it meets certain criteria
 		if (peptide.getMass() >= Properties.peptideMassThreshold) {
 			if (Properties.onlyUsePeptidesInOpenReadingFrames) {
 				if (inORF) {
 					peptides.add(peptide);
 				}
 			} else {
 				peptides.add(peptide);
 			}
 		}
 	}
 	
 	private static boolean isStart(char aminoAcid) {
 		return (aminoAcid == 'M');
 	}
 	
 	private static boolean isStop(char aminoAcid) {
 		return (aminoAcid == '.');
 	}
 	
 	private static boolean isBreak(char aminoAcid) {
 		return (aminoAcid == '.' || aminoAcid == 'K' || aminoAcid == 'R' || aminoAcid == 'X');
 	}
 	
 	
 
 }
