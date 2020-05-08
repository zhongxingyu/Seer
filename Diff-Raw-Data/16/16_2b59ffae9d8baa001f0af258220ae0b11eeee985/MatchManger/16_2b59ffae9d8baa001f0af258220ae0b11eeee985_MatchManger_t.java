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
 	
	public int addRace(Match match, int resulttime, Racetype type){
 		int oldLengthOfRaces = Races.length;
 		Race[] BackupOfRaces = Races.clone();
 		Races = new Race[oldLengthOfRaces+1];
 		
 		for(int i = 0; i < oldLengthOfRaces; i++){
 			Races[i] = BackupOfRaces[i];
 		}
		Races[oldLengthOfRaces] = new Race(NextRaceId, match, resulttime, type);
 		NextRaceId++;
		return Races[oldLengthOfRaces].getId();
 	}
 	
 	
	public int addMatch(int[] date, String name, int[] races){
 		int oldLengthOfMatches = Matches.length;
 		Match[] BackupOfMatches = Matches.clone();
 		Matches = new Match[oldLengthOfMatches+1];
 
 		for(int i = 0; i < oldLengthOfMatches; i++){
 			Matches[i] = BackupOfMatches[i];
 		}
		
		Matches[oldLengthOfMatches] = new Match(NextMatchId, date, name, races);
 		NextMatchId++;
		return Matches[oldLengthOfMatches].getId();
 	}
 }
