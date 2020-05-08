 package com.github.kpacha.jkata.tennis;
 
 public class Tennis {
 
     private int playerOneScored = 0;
 
     public String getScore() {
 	if (playerOneScored == 2)
 	    return "30 - 0";
 	if (playerOneScored == 1)
 	    return "15 - 0";
 	return "0 - 0";
     }
 
     public void playerOneScores() {
 	playerOneScored++;
     }
 }
