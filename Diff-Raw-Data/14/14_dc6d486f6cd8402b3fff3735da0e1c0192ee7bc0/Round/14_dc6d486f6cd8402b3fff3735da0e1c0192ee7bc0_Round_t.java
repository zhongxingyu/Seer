 package model;
 
 public class Round {
 	private Player[]	teamA;
 	private Player[]	teamB;
 	// This is weird before the round ends, so don't fuck around with it until
 	// it does :D
 	private boolean		teamAWon;
 	
 	public Round(Player[] teamA, Player[] teamB) {
 		this.teamA = teamA;
 		this.teamB = teamB;
 	}
 	
 	public Player[] getTeamA() {
 		return teamA;
 	}
 	
 	public Player[] getTeamB() {
 		return teamB;
 	}
 	
 	public void setTeamAWin() {
 		teamAWon = true;
 	}
 	
 	public boolean teamAWon() {
		return teamAWon;
 	}
 }
