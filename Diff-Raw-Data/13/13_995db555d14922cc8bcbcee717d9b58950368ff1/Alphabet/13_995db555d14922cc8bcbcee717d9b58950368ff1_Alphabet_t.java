 package io.seq;
 
 import util.Utils;
 
 public interface Alphabet {
 	
 	boolean isValidChar( char ch );
 	
 	char[] letters();
 	
 	
 	
 	/**
 	 * Amino Acid alphabet as defined http://www.ncbi.nlm.nih.gov/blast/fasta.shtml 
 	 * 
 	 * @author Paolo Di Tommaso
 	 *
 	 */
 	class AminoAcid implements Alphabet {
 
 		public static Alphabet INSTANCE = new AminoAcid();
 		
 		static final char[] aminos = { 
 			'a', 'A',	// alanine 
 			'b', 'B',	// aspartate or asparagine
 			'c', 'C',	// cystine
 			'd', 'D',	// aspartate
 			'e', 'E',	// glutamate
 			'f', 'F',	// phenylalanine
 			'g', 'G',	// glycine
 			'h', 'H',	// histidine
 			'i', 'I',	// isoleucine
 			'k', 'K',	// lysine
 			'l', 'L',	// leucine
 			'm', 'M',	// methionine
 			'n', 'N',	// asparagine
 			'p', 'P',	// proline
 			'q', 'Q',	// glutamine
 			'r', 'R',	// arginine
 			's', 'S',	// serine
 			't', 'T',	// threonine
 			'u', 'U',	// selenocysteine
 			'v', 'V',	// valine	
 			'w', 'W',	// tryptophan
 			'y', 'Y',	// tyrosine
 			'z', 'Z',	// glutamate or glutamine
 			'x', 'X',	// any   
 			'*',	// translation stop
 			'-'		// gap of indeterminate length
 		};
 		
 		
 		public boolean isValidChar(char ch) {
 			return Utils.contains(aminos, ch);
 		}
 
 		public char[] letters() {
 			return aminos;
 		} 
 		
 		@Override
 		public String toString() { 
 			return "amino-acid";
 		}
 		
 	}
 	
 	/**
 	 * Nucleic acid alphabet 
 	 * 
 	 */
 	class NucleicAcid implements Alphabet { 
 		
 		public static Alphabet INSTANCE = new NucleicAcid();
 		
 		
 		static final char[] letters = { 
 			'a', 'A',	// adenosine
 			'c', 'C',	// cytosine
 			'g', 'G',	// guanine
 			'u', 'U',	// uracil
 			't', 'T',	// thymidine
 			'x', 'X',	// any   
 			'-'		// gap of indeterminate length
 		};
 		
 		
 		public boolean isValidChar(char ch) {
 			return Utils.contains(letters, ch);
 		}
 
 		public char[] letters() {
 			return letters;
 		} 
 
 		@Override
 		public String toString() { 
 			return "nucleic-acid";
 		}
 		
 	}
 	
 	/**
 	 * DNA alphabet 
 	 *
 	 */
 	class Dna implements Alphabet { 
 
 		public static Alphabet INSTANCE = new Dna();
 		
 		static final char[] letters = { 
 			'a', 'A',	// adenosine
 			't', 'T',	// thymidine
 			'c', 'C',	// cytosine
 			'g', 'G',	// guanine
			'n', 'N',	// any   
 			'-'		// gap of indeterminate length
 		};
 		
 		
 		public boolean isValidChar(char ch) {
 			return Utils.contains(letters, ch);
 		}
 
 		public char[] letters() {
 			return letters;
 		} 
 		
 		@Override
 		public String toString() { 
 			return "dna-acid";
 		}
 		
 	}
 
 	/**
 	 * RNA alphabet 
 	 * 
 	 */
 	class Rna implements Alphabet { 
 
 		public static Alphabet INSTANCE = new Rna();
 		
 		static final char[] letters = { 
 			'a', 'A',	// adenosine
 			'u', 'U',	// uracil
 			'c', 'C',	// cytosine
 			'g', 'G',	// guanine
			'n', 'N',	// any   
 			'-'		// gap of indeterminate length
 		};
 		
 		
 		public boolean isValidChar(char ch) {
 			return Utils.contains(letters, ch);
 		}
 
 		public char[] letters() {
 			return letters;
 		} 
 		
 		@Override
 		public String toString() { 
 			return "rna-acid";
 		}
 		
 	}
 		
 }
