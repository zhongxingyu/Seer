 /**
  * Copyright (c) 2011 Team Apollo
  */
 package uk.ac.ucl.cs.clonedetector;
 
 
 /**
  * Class definition
  */
 public class Clone {
	
 	/**
 	 * Instance variable def
 	 */
 	private int iStart;
 	
 	/**
 	 * Instance variable def
 	 */
 	private int jStart;
 	
 	/**
 	 * Instance variable def
 	 */
 	private int length;
 	
 	/**
 	 * Constructs a <code>Clone</code> using the passed parameters.
 	 * 
 	 * @param iStart
 	 * @param jStart
 	 * @param length
 	 */
 	public Clone(int iStart, int jStart, int length) {
 		this.iStart = iStart;
 		this.jStart = jStart;
 		this.length = length;
 	}
 	
 	@Override
 	public String toString() {
		return String.format("%d-%d:%d-%d", iStart, iStart+length, jStart, jStart+length);
 	}
 }
