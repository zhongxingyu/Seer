 package final_project.model;
 
 import java.util.*;
 
 public class DERound implements IRound {
     private List<Integer> _seeding;  /*!< The seeding of fencers based on pool results. */
     private double _cut;         /*!< The percentage of the fencers to be cut before the round starts */
     private Result[] _matches;  /*!< Array of all bouts in this DE round. */
     private int _bracketSize;    /*!< The bracket size (How many slots there are in the first round of DEs) (must be a power of 2) */
     private int _currentBracket; /*!< The number of slots in the current stage of the DE round (must be a power of 2 and <= _bracketSize*/
     private static int POINTS_TO_WIN;
 
     public DERound() {
         _seeding = new ArrayList<Integer>();
         POINTS_TO_WIN =15;
     }
 
     public DERound(List<Integer> seeding) {
         _seeding = seeding;
         POINTS_TO_WIN = 15;
     }
 
     public List<Integer> getSeeding() {
         return _seeding;
     }
 
     public void setSeeding(ArrayList<Integer> seeding) {
         _seeding = seeding;
     }
 
     public void setPointsToWin(int newPTW) {
         POINTS_TO_WIN = newPTW;
     }
 
     /**
      * Sets up the DE round by cutting the bottom _cut percentage of the competitors and filling the bracket with the remaining competitors.
      */
     public void setupRound() {
         makeCut();
         calcBracketSize();
         populateBracket();
     }
 
     /**
      * Cuts the bottom _cut percentage of fencers from _seeding.
      */
     public void makeCut() {
         int newEnd = (int) Math.ceil(_seeding.size() * (1 - _cut));
         _seeding = _seeding.subList(0, newEnd);
     }
 
     /**
      * Calculates the proper bracket size using the number of fencers in the round.
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
 
     /**
      * Populates the bracket with all of the seed number for the first round.
      * @throws IllegalStateException
      */
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
      * Helper function for populateBracket() that takes the values that represent the seeding of the competitors
      * in the current IncompleteResults and replaces them with the int id of the actual competitor of that seed.
      */
     public void switchSeedsForCompetitors(){
         for(int i = _matches.length - _bracketSize / 2; i < _matches.length; i++){
             IncompleteResult temp;
             temp = (IncompleteResult) _matches[i];
             if(temp.getPlayer1() > _seeding.size()) { // If player2 got a bye
                 _matches[i] = new CompleteResult(new PlayerResult(temp.getPlayer2(), 0),  // Sets player2 to the winner of the match
                                                  new PlayerResult(-1, 0));
                 _matches[getNextMatchIndex(i)] = new IncompleteResult(temp.getPlayer2(), -1, POINTS_TO_WIN);
             }
             else if(temp.getPlayer2() > _seeding.size()) { // If player1 got a bye
                 _matches[i] = new CompleteResult(new PlayerResult(temp.getPlayer1(), 0),  // Sets player1 to the winner of the match
                                                  new PlayerResult(-1, 0));
                 _matches[getNextMatchIndex(i)] = new IncompleteResult(temp.getPlayer1(), -1, POINTS_TO_WIN);
             }
             else {
                 _matches[i] = new IncompleteResult(_seeding.get(temp.getPlayer1() -1),
                                                    _seeding.get(temp.getPlayer2() -1),
                                                    POINTS_TO_WIN);
             }
         }
     }
 
     /**
      * @param index The index in _matches of the current match.
      * @return int the index of the match that the winner of the index match will continue on to.
      */
     private int getNextMatchIndex(int index) {
         int curRoundSize = 2;
         int headIndex = 0;
         int prevHeadIndex = 0;
         while(headIndex + curRoundSize < index) {  // Sets headIndex to the head of the round that contains index
             prevHeadIndex = headIndex;             // and prevHeadIndex to the head of the previous index
             headIndex += curRoundSize;
             curRoundSize *= 2;
         }
         int curIndex = headIndex;
         int boutsDown = 0;
         while(curIndex < index) {
             curIndex++;
             boutsDown++;
         }
         if(boutsDown %2 != 0) {  // If boutsDown is odd
             boutsDown++;
         }
         return (boutsDown/2) + prevHeadIndex;
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
 
     /**
      * Gets the next match to be fenced.
      * @return IncompleteResult The next match to be fenced.
      * @throws NoSuchMatchException If there is no next bout.
      */
     public IncompleteResult getNextMatch() throws NoSuchMatchException{
         for(Result result : _matches) {
             if(result instanceof IncompleteResult) {
                 return (IncompleteResult) result;
             }
         }
         throw new NoSuchMatchException("No such bout exists in this DERound");
     }
 
     /**
      * Adds its argument as a completed match of the DE
      * @param newResult The completed result to be added.
      * @throws NoSuchMatchException If there is no such bout in the DE bracket.
      */
     public void addCompleteResult(CompleteResult newResult) throws NoSuchMatchException {
         Result tempResult;
        for(int i = 0; i < _matches.length; i++) {
             tempResult = _matches[i];
             if(tempResult.getPlayer1() == newResult.getPlayer1() &&
                tempResult.getPlayer2() == newResult.getPlayer2()
                ||
                tempResult.getPlayer1() == newResult.getPlayer2() &&
                tempResult.getPlayer2() == newResult.getPlayer1()) {
                 if(tempResult instanceof CompleteResult) {
                     throw new NoSuchMatchException("This bout has already been completed");
                 }
                 tempResult = newResult;
                 IncompleteResult nextResult = (IncompleteResult) _matches[getNextMatchIndex(i)];
                 if(nextResult == null) {
                     nextResult = new IncompleteResult(newResult.getWinner(), -1, POINTS_TO_WIN);
                 }
                 else if(nextResult.getPlayer2() == -1 ) {
                     nextResult.setPlayer2(newResult.getWinner());
                 }
                 else if(nextResult.getPlayer1() == -1) {
                     // Should never actually happen because only player1 should be set to -1
                     nextResult.setPlayer1(newResult.getWinner());
                 }
                 return;
             }
         }
     }
 
     //TODO: why are we getting this warning?
     public class NoSuchMatchException extends Exception {
         public NoSuchMatchException(String message) {
             super(message);
         }
     }
 
 
     @Override
 	public List<Integer> getTopNPlayers(int num) {
         // TODO Auto-generated method stub
         return null;
     }
 }
