 package final_project.model;
 
 import java.util.*;
 
 
 public abstract class Pool {
 	protected Collection<Integer> _players;
 	protected Collection<Integer> _refs;
 	protected Collection<CompleteResult> _results;
 	protected List<IncompleteResult> _incompleteResults;

 	public Pool(){
 		_players = new HashSet<Integer>();
 		_refs = new HashSet<Integer>();
 		_incompleteResults = new LinkedList<IncompleteResult>();
 	}

 	public Collection<CompleteResult> getResults() {
 		return _results;
 	}
 
 	public void createIncompleteResults(){

 	}

 	public Collection<Integer> getPlayers() {
 		return _players;
 	}
 
 	public Collection<Integer> getRefs() {
 		return _refs;
 	}
 
 	public void addPlayer(int id){
 		_players.add(id);
 	}
 
 	public IncompleteResult getNextResult() {
 		return _incompleteResults.get(0);
 	}

 	public abstract Collection<? extends PlayerSeed> getSeeds();
 
 	public void addCompletedResult(CompleteResult completeResult) throws IllegalArgumentException{
 		if (isPrematureResult(completeResult)) {
 			throw new IllegalArgumentException("Attempted to add result for bout that should not have been fenced now.");
 		}
 		else {
 			_results.add(completeResult);
 			_incompleteResults.remove(0);
 		}
 	}
 
 	private boolean isPrematureResult(CompleteResult completeResult) {
		return !((completeResult.getWinner() == _incompleteResults.get(0).getPlayer1() &&
 				completeResult.getLoser() == _incompleteResults.get(0).getPlayer2())) ||
 				(completeResult.getWinner() == _incompleteResults.get(0).getPlayer2() &&
 	    		completeResult.getLoser() == _incompleteResults.get(0).getPlayer1());
 	}
 }
