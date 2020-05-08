 package final_project.model;
 
 import java.util.*;
 import final_project.control.*;
 
 public abstract class Pool {
 	protected List<Integer> _players;
 	protected Collection<Integer> _refs;
 	protected Collection<CompleteResult> _results;
 	protected List<IncompleteResult> _incompleteResults;
 	protected Collection<Integer> _strips;
     protected Collection<PoolObserver> _observers;
 
 	public Pool(){
 		_players = new ArrayList<Integer>();
 		_refs = new HashSet<Integer>();
 		_incompleteResults = new LinkedList<IncompleteResult>();
 		_results = new HashSet<CompleteResult>();
         _observers = new LinkedList<PoolObserver>();
         _strips = new LinkedList<Integer>();
 	}
 
     public void addObserver(PoolObserver observer) {
         _observers.add(observer);
     }
 
     public void removeObserver(PoolObserver observer) {
         _observers.remove(observer);
     }
 
 	//currently for testing only
 	public int numPlayers(){
 		return _players.size();
 	}
 
 	public void shufflePlayers() {
 		Collections.shuffle(_players);
 	}
 	/**
 	 * Returns the Collection of all completed matches.
 	 * @return Collection<CompletResult> All matches currently completed.
 	 */
 	public Collection<CompleteResult> getResults() {
 		return _results;
 	}
 
 	/**
 	 * Returns the List of player IDs in the pool.
 	 * @return List<Integer> The list of player IDs in the pool.
 	 */
 	public List<Integer> getPlayers() {
 		return _players;
 	}
 
 	public Collection<Integer> getStrips() {
 		return _strips;
 	}
 
 	public void addStrip(Integer toAdd) {
 		System.out.println("Add strips called in pool");
 		_strips.add(toAdd);
 	}
 
 	public void clearStrips() {
 		_strips.clear();
 	}
 
 	public Collection<Integer> getRefs() {
 		return _refs;
 	}
 
 	public void addPlayer(int id){
 		_players.add(id);
 	}
 
 	public void addRef(int id){
 		System.out.println("add ref called in pool");
 		_refs.add(id);
 	}
 	public void clearRefs(){
 		System.out.println("clear refs called");
 		_refs.clear();
 	}
 
 	/**
 	 * Gets the next match to be fenced.
 	 * @return IncompleteResult The next match to be fenced.
 	 */
 	public IncompleteResult getNextResult() {
         if(_incompleteResults.isEmpty())
             return null;
 		return _incompleteResults.get(0);
 	}
 	
 	/**
 	 * @return IncompleteResult The on deck match (the second element of _incompleteResult
 	 * because the first one should already be in progress).
 	 */
 	public IncompleteResult getOnDeckResult() {
		if(_incompleteResults.size() > 1)
 			return null;
 		return
 			_incompleteResults.get(1);
 	}
 	
 	/**
 	 * @return IncompleteResult The match that is in the hole (the third element of _incompleteResults
 	 * because the first one should already be in progress.
 	 */
 	public IncompleteResult getInHoleBout() {
 		if(_incompleteResults.size() > 2)
 			return null;
 		return
 			_incompleteResults.get(2);
 	}
 
 	//currently for testing only
 	public List<IncompleteResult> getIncompleteResults(){
 		return _incompleteResults;
 	}
 
     public boolean isDone() {
         return _incompleteResults.isEmpty();
     }
 
 	public abstract List<? extends PlayerSeed> getSeeds();
 
 	/**
 	 * Adds its argument to the list of completed results if it matches the result the pool is expecting next.
 	 * @param completeResult The result to be added to the pools collection of completed results.
 	 * @throws IllegalArgumentException
 	 * @return a boolean true if all of this pool's matches have been completed.
 	 */
 	public boolean addCompletedResult(CompleteResult completeResult) throws IllegalArgumentException{
 		System.out.println("Player 1: " + _incompleteResults.get(0).getPlayer1() + " 2: " + _incompleteResults.get(0).getPlayer2());
 		if (isPrematureResult(completeResult))
 			throw new IllegalArgumentException("Attempted to add result for bout that should not have been fenced now.");
 		else {
 			_results.add(completeResult);
 			_incompleteResults.remove(0);
             for (PoolObserver obs : _observers)
                 obs.addCompleteResult(completeResult);
 			return _incompleteResults.isEmpty();
 		}
 	}
 
 	private boolean isPrematureResult(CompleteResult completeResult) {
 		return !((completeResult.getWinner() == _incompleteResults.get(0).getPlayer1() &&
                   completeResult.getLoser() == _incompleteResults.get(0).getPlayer2()) ||
                  (completeResult.getWinner() == _incompleteResults.get(0).getPlayer2() &&
                   completeResult.getLoser() == _incompleteResults.get(0).getPlayer1()));
 	}
 }
