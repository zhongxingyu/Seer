 package com.github.kpacha.jkata.tennis;
 
 public class Tennis {
 
    private boolean playerOneScored = false;

     public String getScore() {
	if (playerOneScored)
	    return "15 - 0";
 	return "0 - 0";
     }

    public void playerOneScores() {
	playerOneScored = true;
    }
 }
