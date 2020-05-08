 package com.github.kpacha.jkata.tennis;
 
 public class Tennis {
 
     private int playerOneScored = 0;
     private int playerTwoScored = 0;
 
     public String getScore() {
 	if (isDeuce())
 	    return "Deuce";
	if (playerOneScored == 4 && playerTwoScored == 3
		|| playerOneScored == 5 && playerTwoScored == 4)
 	    return "Advantage Player 1";
 	if (playerOneScored == 4)
 	    return "Player 1 wins";
 	return (15 * playerOneScored) + " - " + (15 * playerTwoScored);
     }
 
     private boolean isDeuce() {
 	return playerOneScored == playerTwoScored && playerTwoScored > 2;
     }
 
     public void playerOneScores() {
 	playerOneScored++;
     }
 
     public void playerTwoScores() {
 	playerTwoScored++;
     }
 }
