 package final_project.model;
 
 import java.util.*;
 
 public class FencerPool extends Pool{	
 	HashMap<Integer, FencerRoundResults> _idToFencerResults;
 	
 	public FencerPool() {
 		super();
 		_idToFencerResults = new HashMap<Integer, FencerRoundResults>();
 	}
 	
 	@Override
 	public Collection<FencerSeed> getSeeds() {
 		Collection<FencerSeed> fencerSeeds = new LinkedList<FencerSeed>();
 		createIdToFencerResultsMap();
 		tallyResults();
 		populateSeedListFromResults(fencerSeeds);
 		
 		return fencerSeeds;
 	}
 
 	private void populateSeedListFromResults(Collection<FencerSeed> fencerSeeds) {
 		for (FencerRoundResults result : _idToFencerResults.values())
 			fencerSeeds.add(new FencerSeed(result));
 	}
 
 	private void createIdToFencerResultsMap() {
 		for (Integer i : _players)
 			_idToFencerResults.put(i, new FencerRoundResults(i));
 	}
 
 	private void tallyResults() {
		for (Result b : _results){
 			FencerRoundResults winner = _idToFencerResults.get(b.getWinner());
 			FencerRoundResults loser = _idToFencerResults.get(b.getLoser());
 			winner.addWin();
 			loser.addLoss();
 
 			winner.addTouchesScored(b.getWinnerScore());
 			loser.addTouchesScored(b.getLoserScore());
 
 			winner.addTouchesReceived(b.getLoserScore());
 			loser.addTouchesReceived(b.getWinnerScore());
 		}
 	}
 }
