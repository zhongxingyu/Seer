 import java.util.*;
 
 public class AdvanceMatch implements Tournament {
 	MatchData data;
 	Tournament feeder1;
 	Tournament feeder2;
 	
 	AdvanceMatch(MatchData data, Tournament feeder1, Tournament feeder2){
 		this.data = data;
 		this.feeder1 = feeder1;
 		this.feeder2 = feeder2;
 	}
 	
 	public MatchData getData(){
 		return data;
 	}
 	
 	public boolean allScoresValid(){
 		return this.data.score.isValid() &&
 				this.feeder1.allScoresValid() &&
 				this.feeder2.allScoresValid();
 	}
 	
 	// determines whether this tournament is valid by checking:
 	// 1) if the tournament is ranked, whether each member of the top half
 	//    is initially paired with a member from the bottom half
 	// 2) that each contestant in an advanced match came from one 
 	//    of its feeder matches
 	public boolean tourValid(){
 		InitialMatch[] inits = this.getLeaves();
 		
 		// get a list of the contestants
 		ArrayList<Contestant> contsList = new ArrayList<Contestant>();
 		for (int i = 0; i < inits.length; i++){
 			contsList.add(inits[i].data.contestant1);
 			contsList.add(inits[i].data.contestant2);
 		}
 		Contestant[] conts = contsList.toArray(new Contestant[0]);
 		// need to make sure that all contestants are playing the same sport
 		// thus, they must all be objects of the same class
 		Class<? extends Contestant> sportType = conts[0].getClass();
 		for(int i = 1; i < conts.length; i++){
 			if(!conts[i].getClass().equals(sportType))
 				return false;
 		}
 		// only get here if they are all playing the same sport
 		
 		// if they are not ranked, we only need to know if the players always advanced
 		if(!(conts[0] instanceof Ranked))
			return this.playersAlwaysAdvanced();
 		// now we have to deal with rankings,
 		// start by letting ranksValid be true
 		boolean ranksValid = true;
 		
 		// now we know we have all the same contestants, and they are ranked
 		ArrayList<Ranked> rankedContsList = new ArrayList<Ranked>();
 		for(int i = 0; i < conts.length; i++){
 			rankedContsList.add((Ranked) conts[i]);
 		}
 		Ranked[] ranked = rankedContsList.toArray(new Ranked[0]);
 		ArrayList<Integer> ranksList = new ArrayList<Integer>();
 		for(int i = 0; i < ranked.length; i++){
 			ranksList.add(ranked[i].getRanking());
 		}
 		Integer[] ranks = ranksList.toArray(new Integer[0]);
 		Arrays.sort(ranks);
 		// now there is a sorted list of the ranks of all the players, and we know that 
 		// its length must be even because it was taken from a series of matches,
 		// each with 2 contestants
 		int lowCutoff = ranks[(ranks.length / 2) - 1];
 		int highCutoff = ranks[(ranks.length / 2)];
 		// make sure one contestant is ranked below cutoff, and one is ranked above
 		for(int i = 0; i < inits.length; i++){
 			InitialMatch in = inits[i];
 			Ranked r1 = (Ranked) in.data.contestant1;
 			Ranked r2 = (Ranked) in.data.contestant2;
 			if(r1.hasBetterRanking(r2)){
 				if(r1.getRanking() > lowCutoff ||
 						r2.getRanking() < highCutoff){
 					ranksValid = false;
 					break;
 				}
 			}
 			else 
 				if(r1.getRanking() < highCutoff ||
 						r2.getRanking() > lowCutoff){
 					ranksValid = false;
 					break;
 				}
 		}
 		// now ranksValid is true only if every initial pairing consists of
 		// 2 ranked contestants with one in the upper half and one in the lower half
 		
 		return this.allScoresValid() && ranksValid && this.playersAlwaysAdvanced();
 	}
 	
 	// determines whether both contestants in this match came from the previous round,
 	// each from one of the feeder matches
 	public boolean playersAlwaysAdvanced(){
 		Contestant c1 = data.contestant1, c2 = data.contestant2;
 		Contestant[] previousRound = new Contestant[]{
 				feeder1.getData().contestant1,
 				feeder1.getData().contestant2,
 				feeder2.getData().contestant1,
 				feeder2.getData().contestant2
 		};
 		// either c1 came from feeder1 and c2 came from feeder2, or
 		//   c1 came from feeder2 and c2 came from feeder1
 		// (We are assuming that they cannot both come from the same feeder match.)
 		boolean isValid =
 				((c1.equals(previousRound[0]) || c1.equals(previousRound[1])) &&
 				 (c2.equals(previousRound[2]) || c2.equals(previousRound[3])))   
 																				||
 				((c2.equals(previousRound[0]) || c2.equals(previousRound[1])) &&
 			     (c1.equals(previousRound[2]) || c1.equals(previousRound[3])));
 		// also make sure lower down matches are all valid
 		return isValid &&
 				feeder1.playersAlwaysAdvanced() &&
 				feeder2.playersAlwaysAdvanced();
 	}
 	
 	//get all leaves (initial matches) in this tournament match tree
 	public InitialMatch[] getLeaves(){
 		ArrayList<InitialMatch> leaves = new ArrayList<InitialMatch>();
 		InitialMatch[] matches = feeder1.getLeaves();
 		for(int i = 0; i < matches.length; i++){
 			leaves.add(matches[i]);
 		}
 		Collections.addAll(leaves, feeder2.getLeaves());
 		return leaves.toArray(new InitialMatch[0]);
 	}
 	
 	public int matchesPlayed(String name){
 		int matches = 0;
 		if(this.data.contestant1.getName().equals(name) ||
 			this.data.contestant2.getName().equals(name))
 				matches = 1;
 		return matches +
 				feeder1.matchesPlayed(name) +
 				feeder2.matchesPlayed(name);
 	}
 }
