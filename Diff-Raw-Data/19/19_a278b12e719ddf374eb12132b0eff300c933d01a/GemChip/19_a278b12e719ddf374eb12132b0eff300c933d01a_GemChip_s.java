 package com.jaredpearson.puzzlestrike.chips;
 
 import com.jaredpearson.puzzlestrike.Chip;
 
 /**
  * Represents a Gem chip
  * @author jared.pearson
  */
 public class GemChip implements Chip {
 	private int gemValue;
 	
 	public GemChip(int gemValue) {
 		this.gemValue = gemValue;
 	}
 
 	/**
 	 * Gets the gem value for this gem.
 	 * @return
 	 */
 	public int getGemValue() {
 		return gemValue;
 	}
 	
	@Override
 	public String getName() {
 		return getGemValue() + " Chip";
 	}
 	
 }
