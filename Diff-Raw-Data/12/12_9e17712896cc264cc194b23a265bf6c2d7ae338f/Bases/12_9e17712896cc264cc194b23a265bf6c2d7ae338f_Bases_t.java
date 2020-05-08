 package glt;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 public class Bases {
 
 	private List<Base> bases = new ArrayList<Base>();
 	
 	public Bases() { }
 	
 	public Bases(Bases bases) {
 		this.add(bases);
 	}
 	
 	/**
 	 * Creates a new object based on the letters 
 	 * @param letters The letters to use as the bases
 	 */
 	public Bases(String letters) {
 		this.add(letters);
 	}
 
 	/**
 	 * Returns the number of the bases.
 	 * @return the number of the bases.
 	 */
 	public int length() {
 		return this.bases.size();
 	}
 	
 	/**
 	 * Adds a single Base to the strand.
 	 * @param base A single Base which is added to the Strand
 	 * @return This object
 	 */
 	public Bases add(Base base) {
 		this.bases.add(base);
 		return this;
 	}
 
 	/**
 	 * Adds a single Base to the strand.
 	 * @param base A single Base which is added to the Strand
 	 * @return This object
 	 */
 	public Bases add(Bases bases) {
 		this.bases.addAll(bases.bases);
 		return this;
 	}
 
 	/**
 	 * Adds one or more letters to the end of the current bases
 	 * @param letters One or more letters
 	 * @return This object
 	 */
 	public Bases add(String letters) {
 		int length = letters.length();
 		for (int i = 0; i < length; i++) {
 			this.bases.add(Base.forLetter(letters.substring(i, i + 1)));
 		}
 		return this;
 	}
 	
 	/**
 	 * Returns a new Bases object with the bases in reversed order
 	 * @return a new Bases object with the bases in reversed order
 	 */
 	public Bases reverse() {
 		Bases bases = new Bases(this);
 		Collections.reverse(bases.bases);
 		return bases;
 	}
 
 	/**
 	 * Returns a new Bases object with complemented bases
 	 * @return a new Bases object with complemented bases
 	 */
 	public Bases complement() {
		
		Bases complement = new Bases();
		
		// get the complement for each individual base
		for (Base base : this.bases) {
			complement.add(base.getComplement());
		}
		
		return complement;
 	}
 
 	/**
 	 * Convenience function which reverses and complements the bases in one go
 	 * @return A new Bases object
 	 */
 	public Bases reverseComplement() {
 		return this.reverse().complement();
 	}
 	
 	/**
 	 * Returns all bases as letters
 	 * @return all bases as letters
 	 */
 	public String getLetters() {
 		
 		StringBuffer sb = new StringBuffer();
 		
 		for (Base base : this.bases) {
 			sb.append(base.getLetter());
 		}
 		
 		return sb.toString();
 	}
 }
