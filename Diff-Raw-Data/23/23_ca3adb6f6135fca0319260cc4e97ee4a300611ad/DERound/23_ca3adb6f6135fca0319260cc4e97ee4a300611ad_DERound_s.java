 package final_project.model;
 
 import java.util.*;
 
 public class DERound implements IRound {
 	private List<Integer> _seeding;  /*!< The seeding of fencers based on pool results. */
 	private double _cut;         /*!< The percentage of the fencers to be cut before the round starts */
 	private Result[] _matches;  /*!< Array of all bouts in this DE round. */
 	private int _bracketSize;    /*!< The bracket size (How many slots there are in the first round of DEs) (must be a power of 2) */
 	private int _currentBracket; /*!< The number of slots in the current stage of the DE round (must be a power of 2 and <= _bracketSize*/
 	private static int POINTS_TO_WIN;
 
 	/**
 	 * Sets up the DE round by cutting the bottom _cut percentage of the competitors and filling the bracket with the remaining competitors.
 	 */
 	public void setupRound() {
 		makeCut();
 		calcBracketSize();
 		populateBracket();
 	}
 
 	/**
 	 * Cuts the bottom _cut percentage of fencers from _seeding
 	 */
 	public void makeCut() {
 		int newEnd = (int) Math.ceil(_seeding.size() * (1 - _cut));
 		_seeding = _seeding.subList(0, newEnd);
 	}
 
 	/**
 	 * Calculates the proper bracket size using the number of fencers in the round
 	 */
 	private void calcBracketSize() throws IllegalArgumentException{
 		if(_seeding.size() < 2)
 			throw new IllegalArgumentException("Attempted to build a bracket for less than 2 competitors.");
 		int curSize = 2;
 		int totalSize = 2;
 		while(curSize < _seeding.size()) {
 			curSize *= 2;
 			totalSize += curSize;
 		}
 		_bracketSize = curSize;
 		_matches = new Result[totalSize];
 	}
 
 	public void populateBracket() throws IllegalStateException{
 		if(_matches == null){
 			throw new IllegalStateException("_matches not yet instantiated.");
 		}
 		populateBracketHelper(0, 2, 1);
 		for(int i = 0; i < _matches.length - _bracketSize / 2 - 1; i++){
 			_matches[i] = null;
 		}
 	}
 
 	/**
 	 *Helper function for populateBracket() that takes the values that represent the seeding of the competitors
 	 *in the current IncompleteResults and replaces them with the int id of the actual competitor of that seed.
 	 */
 	public void switchSeedsForCompetitors(){
 		for(int i = _matches.length - _bracketSize / 2; i < _matches.length; i++){
 
 		}
 	}
 
 	private void populateBracketHelper(int index, int currentBracketSize, int currentSeed){
 		if(index < 0)
 			throw new IllegalArgumentException("Index cannot be negative.");
 		if(index >= _matches.length)
 			return;
 		_matches[index] = new IncompleteResult(	currentSeed,
 												currentBracketSize - currentSeed + 1,
 												POINTS_TO_WIN);
 		populateBracketHelper(	2 * index + 1,
 								currentBracketSize * 2,
 								currentSeed);
 		populateBracketHelper(	2 * index + 2,
 								currentBracketSize * 2,
 								currentBracketSize - currentSeed + 1);
 	}
 
 	public void setCut(double newCut){
 		_cut = newCut;
 	}
 
 	public double getCut() {
 		return _cut;
 	}
 
 	public int getCurrentBracketSize() {
 		return _currentBracket;
 	}
 
 	public IncompleteResult getNextBout() throws NoSuchBoutException{
 		for(Result result : _matches) {
 			if(result instanceof IncompleteResult) {
 				return (IncompleteResult) result;
 			}
 		}
 		throw new NoSuchBoutException("No such bout exists in this DERound");
 	}
 
 	public void addCompleteResult(CompleteResult newResult) throws NoSuchBoutException {
 		Result tempResult;
 		for(int i = 0; i < _bracketSize /2; i++) {
 			tempResult = _matches[i];
 			if(tempResult.getPlayer1() == newResult.getPlayer1() &&
 			   tempResult.getPlayer2() == newResult.getPlayer2()
 			   ||
 			   tempResult.getPlayer1() == newResult.getPlayer2() &&
 			   tempResult.getPlayer2() == newResult.getPlayer1()) {
 				if(tempResult instanceof IncompleteResult) {
 					throw new NoSuchBoutException("This bout has already been completed");
 				}
 			}
 		}
 	}
 
 	public class NoSuchBoutException extends Exception {
 		public NoSuchBoutException(String message) {
 			super(message);
 		}
 	}
 
 
 	@Override
 	public List<Integer> getTopNPlayers(int num) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 }
