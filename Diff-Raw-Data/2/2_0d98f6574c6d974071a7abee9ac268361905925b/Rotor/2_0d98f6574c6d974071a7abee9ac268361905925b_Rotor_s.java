 package simulator;
 
 /**
  * Simulates a single rotor on a rotor machine
  * 
  * @author	Michael Billington <michael.billington@gmail.com>
  * @since	2012-09-08
  */
 public class Rotor {
 	String name;
 	Alphabet alphabet;
 	
 	char[][] encipherTable;	/* Table of character substitutions */
 	char[][] decipherTable;	/* Inverse table of above to save on lookups */
 	int position;			/* Current position of the rotor */
 	int numPositions;		/* Number of possible positions this rotor can take (=length of the alphabet) */
 
 	/**
 	 * @param alphabet 	The alphabet to use. Should be the same for all rotors on the machine
 	 * @param pair 		Array of 2-character strings representing the substitutions that this rotor makes, eg {"AB", "BA"} 
 	 */
 	public Rotor(Alphabet alphabet, String[] pair) {
 		this(alphabet);
 		setWiring(pair);
 	}
 
 	public Rotor(Alphabet alphabet) {
 		/* Cannot override more characters than the length of the alphabet */
 		this.alphabet = alphabet;
 		numPositions = alphabet.length();
 		encipherTable = new char[numPositions][numPositions];
 		decipherTable = new char[numPositions][numPositions];
 
 		int i;
 		char c;
 
 		/* Propagate A-A, B-B, etc, so that this rotor does nothing by default */
 		for(i = 0; i < numPositions; i++) {
 			c = alphabet.getCharFromIndex(i);
 			propagate(encipherTable, c, c);
 			propagate(decipherTable, c, c);
 		}
 	}
 	
 	public boolean setWiring(String[] pair) {
 		assert(pair.length <= alphabet.length());
 		int i;
 		
 		/* Apply defined substitutions */
 		char[] pairChar;
 		for(i = 0; i < pair.length; i++) {
 			pairChar = pair[i].toUpperCase().toCharArray();
 			assert(pairChar.length == 2); /* 1 char to 1 char, other lengths are no good */
 
 			/* Propagate item in table (reciprocal wiring not assumed) */
 			if(pairChar[1] == '_' || pairChar[0] == '_') {
 				/* One-way relations. We use these to mark un-encipherable positions */
 				if(pairChar[1] == '_') {
 					propagate(decipherTable, pairChar[0], pairChar[1]);
 				} else {
 					propagate(encipherTable, pairChar[1], pairChar[0]);
 				}
 			} else {
 				/* We have these characters */
 				propagate(encipherTable,	pairChar[1], pairChar[0]);
 				propagate(decipherTable,	pairChar[0], pairChar[1]);
 			}
 		}
 		return true;
 	}
 	
 	/**
 	 * Add a given wire, calculating what it does for each possible rotor position
 	 * 
 	 * @param in	Input character (left of rotor)
 	 * @param out	Output character (right of rotor)
 	 */
 	private void propagate(char[][]substTable, char in, char out) {
 		/* Add this character to the table */
 		int inID;
 		for(int pos = 0; pos < numPositions; pos++) {
 			inID = alphabet.getIndexFromChar(in);
 			substTable[pos][inID] = out;
 
 			/* Rotate the rotor and fill in its next translation */
 			in = alphabet.prev(in);
			if(out != '?') {
 				out = alphabet.prev(out);
 			}
 		}
 	}
 
 	/**
 	 * Spin this rotor to the given position
 	 * 
 	 * @param newPosChar	Character to set the rotor to
 	 * @return				True if the rotor was rotated, false if the character is not in the alphabet
 	 */
 	public boolean setTo(char newPosChar) {
 		/* Set the rotor to this position */
 		if(alphabet.hasChar(newPosChar)) {
 			position = alphabet.getIndexFromChar(newPosChar);
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Spin the rotor in the default direction
 	 */
 	public void rotate() {
 		rotate(1);
 	}
 
 	/**
 	 * Spin the rotor a given number of positions
 	 * 
 	 * @param offset	Number of positions to move (can be positive or negative)
 	 */
 	public void rotate(int offset) {
 		position += offset + numPositions;
 		position %= numPositions;
 	}	
 
 
 	/**
 	 * Encipher one character
 	 * 
 	 * @param c	Character to encipher
 	 * @return	The result of putting this character through the rotor
 	 */
 	public char encipherChar(char c) {
 		if(!alphabet.hasChar(c)) {
 			/* Simple catch for nonsense chars */
 			return c;
 		}
 		
 		int inID = alphabet.getIndexFromChar(c);
 		return encipherTable[position][inID];
 	}
 
 	public char decipherChar(char c) {
 		if(!alphabet.hasChar(c)) {
 			/* Simple catch for nonsense chars */
 			return c;
 		}
 		int inID = alphabet.getIndexFromChar(c);
 		return decipherTable[position][inID];
 	}
 
 	public boolean setName(String name) {
 		if(name != null && !name.equals("")) {
 			this.name = name;
 			return false;
 		}
 		return false;
 	}
 	
 	public char getPositionChar() {
 		return alphabet.getCharFromIndex(position);
 	}
 	
 	public String toString() {
 		/* Get current wiring */
 		String encTable = "";
 		String decTable = "";
 		String encRow, decRow;
 		
 		int x, y, l = alphabet.length();
 		for(y = 0; y < l; y++) {
 			encRow = decRow = "";
 			for(x = 0; x < l; x++) {
 				encRow += encipherTable[y][x];
 				decRow += decipherTable[y][x];
 			}
 			encTable += encRow + "\n";
 			decTable += decRow + "\n";
 		}
 
 		return "\tEncipher table:\n" + encTable + "\tDecipher table\n" + decTable + "Current position (row # being used 0-" + (l - 1) + "): " + position;
 	}
 }
