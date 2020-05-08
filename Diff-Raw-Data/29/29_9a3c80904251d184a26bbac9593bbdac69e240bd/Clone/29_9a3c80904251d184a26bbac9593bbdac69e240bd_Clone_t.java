 /**
  * Copyright (c) 2011 Team Apollo
  */
 package uk.ac.ucl.cs.clonedetector;
 
 /**
  * <code>Clone</code> stores information about clones (starting position, and
  * the length of the clone).
  */
 public class Clone implements Comparable<Clone> {
 
 	private Reference iStart;
 	private Reference iEnd;
 	private Reference jStart;
 	private Reference jEnd;
 
 	/**
 	 * Constructs a <code>Clone</code> using the passed parameters.
 	 * 
 	 * @param iStart
 	 *            start of the first block that collides
 	 * @param jStart
 	 *            start of the second block that collides
 	 * @param length
 	 *            length is the length of the collision
 	 */
 	public Clone(Reference iStart, Reference iEnd, Reference jStart, Reference jEnd) {
 		if (iStart.getLine() > jStart.getLine()) {
 			Reference tempStart = iStart.clone();
 			Reference tempEnd = iEnd.clone();
 			iStart = jStart;
 			iEnd = jEnd;
 			jStart = tempStart;
 			jEnd = tempEnd;
 		}
 		
 		this.iStart = iStart;
 		this.iEnd = iEnd;
 		this.jStart = jStart;
 		this.jEnd = jEnd;
 	}
 	
 
 	public String toString() {
		if (CloneDetector.options.getFilenames().size() > 1) // if more than 1 filename, output filenames too
			return String.format("(%s)%d-%d:(%s)%d-%d", this.iStart.getFilename(), this.iStart.getLine(), this.iEnd.getLine(), this.jStart.getFilename(), this.jStart.getLine(), this.jEnd.getLine());
		else // otherwise, just output line numbers
			return String.format("%d-%d:%d-%d", this.iStart.getLine(), this.iEnd.getLine(), this.jStart.getLine(), this.jEnd.getLine());
 	}
 	
 	public Reference getIStart() {
 		return iStart;
 	}
 	
 	public int getLength() {
 		return (iEnd.getLine() - iStart.getLine()) + 1;
 	}
 	
 	/**
 	 * Determines whether this clone encompasses another clone. There are two types of encompassing,
 	 * internal and external:
 	 * 
 	 * If there are two clones A:B and a:b then A:B <bold>externally</bold> encompasses
 	 * a:b if a is a subset of A and b is a subset of B.
 	 * 
 	 * If there are two clones A:B and a:b then A:B <bold>internally</bold> encompasses
 	 * a:b if (a and b are subsets of A) or (a and b are subsets of B).
 	 * 
 	 * @param other The clone to comare with.
 	 * @return True if this clone encompasses other, else false.
 	 */
 	public boolean encompasses(Clone other) {
 		if (this.iStart.getFilename().equals(other.iStart.getFilename())) {
 			boolean external = (this.iStart.getLine() <= other.iStart.getLine() && this.iEnd.getLine() >= other.iEnd.getLine() && this.jStart.getLine() <= other.jStart.getLine() && this.jEnd.getLine() >= other.jEnd.getLine());	
 			boolean internal = (this.iStart.getLine() <= other.iStart.getLine() && this.iEnd.getLine() > other.iEnd.getLine() && this.iStart.getLine() <= other.jStart.getLine() && this.iEnd.getLine() > other.jEnd.getLine() ||
 					this.jStart.getLine() <= other.iStart.getLine() && this.jEnd.getLine() > other.iEnd.getLine() && this.jStart.getLine() <= other.jStart.getLine() && this.jEnd.getLine() > other.jEnd.getLine());	
 			
 			return (external || internal);
 		}
 		return false;
 	}
 	
 	public boolean overlapsItself() {
		if (!iStart.getFilename().equals(jStart.getFilename())) return false;
 		return (jStart.getLine() <= iEnd.getLine() && jEnd.getLine() >= iEnd.getLine() ||
 				iStart.getLine() <= jEnd.getLine() && iEnd.getLine() >= jEnd.getLine());
 	}
 	
 	public int compareTo(Clone other) {
 		return iStart.compareTo(other.iStart);
 	}
 }
