 package com.ctb.pilot.gamification.model;
 
 import java.util.List;
 
 public class Leaderboard {
 
 	private List<LeaderboardEntry> entries;
 
 	public Leaderboard(List<LeaderboardEntry> entries) {
 		this.entries = entries;
 	}
 
 	public List<LeaderboardEntry> getEntries() {
 		return entries;
 	}
 
 	@Override
 	public String toString() {
 		return "Leaderboard [entries=" + entries + "]";
 	}
 
 }
