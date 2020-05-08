 package com.blackjack;
 
 import com.blackjack.gamecontrollers.Drill;
 import com.blackjack.strategy.Strategy;
 
 public final class ConfigLoader {
 	// Load configuration file
	//
	// For now, just return some "default" values
 	
 	public static int getDeckCount() {
 		return 1;
 	}
 
 	public static Strategy getStrategy() {
 		return Strategy.BASIC;
 	}
 
 	public static Drill getDrill() {
 		return Drill.PAIRS;
 	}
 
 }
