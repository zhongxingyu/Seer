 package com.github.kpacha.jkata.tennis;
 
 public class Tennis {
 
     private int playerOneScored = 0;
     private int playerTwoScored = 0;
 
     public String getScore() {
 	if (isDeuce())
 	    return "Deuce";
 	if (isAdvantagePlayerOne())
 	    return "Advantage Player 1";
 	if (isAdvantagePlayerTwo())
 	    return "Advantage Player 2";
	if (playerOneScored == 4)
 	    return "Player 1 wins";
 	return (15 * playerOneScored) + " - " + (15 * playerTwoScored);
     }
 
     private boolean isAdvantagePlayerOne() {
 	return playerOneScored == playerTwoScored + 1 && playerOneScored > 3;
     }
 
     private boolean isAdvantagePlayerTwo() {
 	return playerOneScored + 1 == playerTwoScored && playerTwoScored > 3;
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
