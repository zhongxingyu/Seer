 package Peppy;
 import java.util.ArrayList;
 
 public class DNA_DigestionThread implements Runnable {
 	
 	ArrayList<Peptide> peptides = new ArrayList<Peptide>();
 	DNA_Sequence nucleotideSequence;
 	byte frame;
 	boolean forwards;
 	int startIndex;
 	int stopIndex;
 	boolean reverse;
 
 	public void run() {
 		//go through each character in the line, skipping ahead "frame" characters
 		if (forwards) {
 			digest(startIndex + frame, stopIndex);
 		} else {
 			digest(stopIndex - frame - 1, startIndex - 1);	
 		}
 	}
 	
 
 	/**
 	 * @param nucleotideSequence
 	 * @param frame
 	 * @param forwards
 	 */
 	public DNA_DigestionThread(DNA_Sequence nucleotideSequence,
 			byte frame, boolean forwards, int startIndex, int stopIndex, boolean reverse) {
 		if (startIndex < 0) startIndex = 0;
 		this.nucleotideSequence = nucleotideSequence;
 		this.frame = frame;
 		this.forwards = forwards;
 		this.startIndex = startIndex;
 		this.stopIndex = stopIndex;
 		this.reverse = reverse;
 	}
 	
 	
 	public ArrayList<Peptide> getPeptides( ) {
 		return peptides;
 	}
 	
 	
 	public ArrayList<Peptide> digest(int startIndex, int stopIndex) {
 		ArrayList<Protein> proteins = translateToProteins(startIndex, stopIndex);
		return ProteinDigestion.getPeptidesFromListOfProteins(proteins);
 	}
 	
 
 	/**
 	 * creates an ArrayList of Proteins where STOPs mark the end
 	 * of each protein
 	 * @param startPosition
 	 * @param stopPosition
 	 */
 	private ArrayList<Protein> translateToProteins(int startPosition, int stopPosition) {
 		ArrayList<Protein> proteins = new ArrayList<Protein>();
 		char [] codon = new char[3];
 		char aminoAcid;
 		int mod = 0;
 		StringBuffer buildingProtein = new StringBuffer();
 		Sequence sequence = nucleotideSequence.getParentSequence();
 		String name = sequence.getSequenceFile().getName();
 		int increment = 1;
 		if (!forwards) increment = -1;
 		int index;
 		int proteinStart = startPosition;
 		for (index = startPosition; index != stopPosition; index += increment) {
 			codon[mod] = nucleotideSequence.getSequence().charAt(index);
 			if (mod == 2) {
 				aminoAcid = Definitions.aminoAcidList[indexForCodonArray(codon, forwards)];
 				buildingProtein.append(aminoAcid);
 				if (aminoAcid == '.') {
 					if (buildingProtein.length() > 3) {
 						if (reverse) buildingProtein.reverse();
 						proteins.add(new Protein(name, proteinStart, buildingProtein.toString(), false, -1, -1, forwards, sequence));
 					}
 					buildingProtein = new StringBuffer();
 					proteinStart = index + 1;
 				}
 			}
 			if (mod == 2) {
 				mod = 0;
 			} else {
 				mod++;
 			}
 		}
 		
 		if (buildingProtein.length() > 3) {
 			proteins.add(new Protein(name, index, buildingProtein.toString(), false, -1, -1, forwards, sequence));
 		}
 			
 	
 		return proteins;
 	}
 
 
 	public static int indexForCodonArray(char [] codon, boolean forwards) {
 		int out = indexForCodonArray(codon);
 		if (out == -1) return 56; //if unknown, return STOP
 		if (forwards) {
 			return indexForCodonArray(codon);
 		} else {
 			return 63 - indexForCodonArray(codon);
 		}
 	}
 	
 	/*
 	 * TODO this method needs to make sure that if any unknown characters are found then 
 	 * STOP (56) is returned.  Right now that only happens with the last character.
 	 */
 	/**
 	 * Same as other, but assumes that the direction is "forwards"
 	 * @param codon
 	 * @return
 	 */
 	public static int indexForCodonArray(char [] codon) {
 		if (codon[0] == 'A') {
 			if (codon[1] == 'A') {
 				if (codon[2] == 'A') {
 					return 0;
 				} else if (codon[2] == 'C') {
 					return 1;
 				} else if (codon[2] == 'G') {
 					return 2;
 				} else {
 					return 3;
 				}
 			} else if (codon[1] == 'C') {
 				if (codon[2] == 'A') {
 					return 4;
 				} else if (codon[2] == 'C') {
 					return 5;
 				} else if (codon[2] == 'G') {
 					return 6;
 				} else {
 					return 7;
 				}
 			} else if (codon[1] == 'G') {
 				if (codon[2] == 'A') {
 					return 8;
 				} else if (codon[2] == 'C') {
 					return 9;
 				} else if (codon[2] == 'G') {
 					return 10;
 				} else {
 					return 11;
 				}
 			} else {
 				if (codon[2] == 'A') {
 					return 12;
 				} else if (codon[2] == 'C') {
 					return 13;
 				} else if (codon[2] == 'G') {
 					return 14;
 				} else {
 					return 15;
 				}
 			}
 		} else if (codon[0] == 'C') {
 			if (codon[1] == 'A') {
 				if (codon[2] == 'A') {
 					return 16;
 				} else if (codon[2] == 'C') {
 					return 17;
 				} else if (codon[2] == 'G') {
 					return 18;
 				} else {
 					return 19;
 				}
 			} else if (codon[1] == 'C') {
 				if (codon[2] == 'A') {
 					return 20;
 				} else if (codon[2] == 'C') {
 					return 21;
 				} else if (codon[2] == 'G') {
 					return 22;
 				} else {
 					return 23;
 				}
 			} else if (codon[1] == 'G') {
 				if (codon[2] == 'A') {
 					return 24;
 				} else if (codon[2] == 'C') {
 					return 25;
 				} else if (codon[2] == 'G') {
 					return 26;
 				} else {
 					return 27;
 				}
 			} else {
 				if (codon[2] == 'A') {
 					return 28;
 				} else if (codon[2] == 'C') {
 					return 29;
 				} else if (codon[2] == 'G') {
 					return 30;
 				} else {
 					return 31;
 				}
 			}
 		} else if (codon[0] == 'G') {
 			if (codon[1] == 'A') {
 				if (codon[2] == 'A') {
 					return 32;
 				} else if (codon[2] == 'C') {
 					return 33;
 				} else if (codon[2] == 'G') {
 					return 34;
 				} else {
 					return 35;
 				}
 			} else if (codon[1] == 'C') {
 				if (codon[2] == 'A') {
 					return 36;
 				} else if (codon[2] == 'C') {
 					return 37;
 				} else if (codon[2] == 'G') {
 					return 38;
 				} else {
 					return 39;
 				}
 			} else if (codon[1] == 'G') {
 				if (codon[2] == 'A') {
 					return 40;
 				} else if (codon[2] == 'C') {
 					return 41;
 				} else if (codon[2] == 'G') {
 					return 42;
 				} else {
 					return 43;
 				}
 			} else {
 				if (codon[2] == 'A') {
 					return 44;
 				} else if (codon[2] == 'C') {
 					return 45;
 				} else if (codon[2] == 'G') {
 					return 46;
 				} else {
 					return 47;
 				}
 			}
 		} else if (codon[0] == 'T') {
 			if (codon[1] == 'A') {
 				if (codon[2] == 'A') {
 					return 48;
 				} else if (codon[2] == 'C') {
 					return 49;
 				} else if (codon[2] == 'G') {
 					return 50;
 				} else {
 					return 51;
 				}
 			} else if (codon[1] == 'C') {
 				if (codon[2] == 'A') {
 					return 52;
 				} else if (codon[2] == 'C') {
 					return 53;
 				} else if (codon[2] == 'G') {
 					return 54;
 				} else {
 					return 55;
 				}
 			} else if (codon[1] == 'G') {
 				if (codon[2] == 'A') {
 					return 56;
 				} else if (codon[2] == 'C') {
 					return 57;
 				} else if (codon[2] == 'G') {
 					return 58;
 				} else {
 					return 59;
 				}
 			} else {
 				if (codon[2] == 'A') {
 					return 60;
 				} else if (codon[2] == 'C') {
 					return 61;
 				} else if (codon[2] == 'G') {
 					return 62;
 				} else {
 					return 63;
 				}
 			}
 		} else {
 			return -1; //return STOP
 		}
 	}
 
 }
 
 
