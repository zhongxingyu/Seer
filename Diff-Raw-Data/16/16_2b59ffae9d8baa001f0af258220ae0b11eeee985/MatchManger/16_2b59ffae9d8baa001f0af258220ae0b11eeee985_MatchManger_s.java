 package com.thenewprogramming.android.timetoswim;
 
 public class MatchManger {
 	
 	private Race[] Races;
 	private Racetype[] Racetypes;
 	private Match[] Matches;
 	
 	//The id to be given to the next new race
 	private int NextRaceId;
 	//The id to be given to the next new match
 	private int NextMatchId;
 	
 	
 	
 	public MatchManger(int nextmatchid, int nextraceid){
 		NextMatchId = nextmatchid;
 		NextRaceId = nextraceid;
 	}
 	
	public void addRace(Match match, int resulttime, Racetype type){
 		int oldLengthOfRaces = Races.length;
 		Race[] BackupOfRaces = Races.clone();
 		Races = new Race[oldLengthOfRaces+1];
 		
 		for(int i = 0; i < oldLengthOfRaces; i++){
 			Races[i] = BackupOfRaces[i];
 		}
		Races[oldLengthOfRaces] = new Race(NextMatchId, match, resulttime, type);
 		NextRaceId++;
 	}
 	
 	
	public void addMatch(int[] date,Race[] races){
 		int oldLengthOfMatches = Matches.length;
 		Match[] BackupOfMatches = Matches.clone();
 		Matches = new Match[oldLengthOfMatches+1];
 
 		for(int i = 0; i < oldLengthOfMatches; i++){
 			Matches[i] = BackupOfMatches[i];
 		}
		Matches[oldLengthOfMatches] = new Match(NextMatchId);
 		NextMatchId++;
 	}
 }
