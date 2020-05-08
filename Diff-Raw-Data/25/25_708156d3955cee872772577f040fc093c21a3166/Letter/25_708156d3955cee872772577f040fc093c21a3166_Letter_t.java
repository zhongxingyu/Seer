 package com.WordFinder;
 
 // -------------------------------------------------------------------------
  /**
  *  Contains the letter in a grid cell, along with
  *  a table of all adjacent letters.
  *
  *  @author John Mooring (jmooring)
  *  @version Oct 30, 2011
  */
 public class Letter {
     private char character;
     private boolean[] adjacent = new boolean[26];
 
     /**
      * Constructs a letter object, takes the center character and an array of adjacent characters
      * @param letter character in the cell
      * @param adjacent an array of all adjacent characters
      */
     public Letter(char letter, boolean[] adjacent) {
 	character = letter;
 	this.adjacent = adjacent;
     }
 
     /**
      * Get the contained character
      * @return the contained char
      */
     public char getLetter() {
 	return character;
     }
 
     /**
      * Checks to see if the passed character is adjacent to this letter
      * @param letter the letter to check for adjacency
      * @return boolean whether or not the passed character is adjacent
      */
     public boolean hasAdjascent(char letter) {
 	return adjacent[letter - 'a'];
     }
 }
