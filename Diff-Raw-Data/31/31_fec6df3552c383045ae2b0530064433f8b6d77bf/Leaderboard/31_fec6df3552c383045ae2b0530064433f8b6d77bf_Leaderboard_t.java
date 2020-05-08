 package com.ctb.pilot.gamification.model;
 
 import java.util.List;
 
 public class Leaderboard {
 
 	private List<LeaderboardEntry> entries;
 
 	public Leaderboard(List<LeaderboardEntry> entries) {
 		this.entries = entries;

		// Handle same scores.
		normalize(entries);
	}

	static void normalize(List<LeaderboardEntry> entries) {
		int previousRank = 1;
		int previousScore = 0;
		for (LeaderboardEntry entry : entries) {
			int point = entry.getPoint();
			if (point == previousScore) {
				entry.setRank(previousRank);
			}
			previousScore = point;
			previousRank = entry.getRank();
		}

 	}
 
 	public List<LeaderboardEntry> getEntries() {
 		return entries;
 	}
 
 	@Override
 	public String toString() {
 		return "Leaderboard [entries=" + entries + "]";
 	}
 
 }
