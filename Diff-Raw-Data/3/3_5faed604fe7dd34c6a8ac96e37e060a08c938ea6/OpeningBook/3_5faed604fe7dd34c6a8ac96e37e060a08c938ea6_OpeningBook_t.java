 package iago.history;
 
 import iago.Move;
 
 public class OpeningBook {
 	private NamedPosition initialPosition;
 	private NamedPosition currentOpening;
 	private boolean firstMove;
 	
 	public OpeningBook() {
 		initialPosition = NamedPosition.fromStream(OpeningBookLoader.class.getResourceAsStream("small_book"));
 		reset();
 	}
 	
 	//In between games we need to reset the opening book
 	public void reset(){
 		currentOpening = initialPosition;
 		firstMove = true;
 	}
 	
 	
 	/**
 	 * Returns an opening move if there is one, but gives up if there isn't
 	 * @throws UnexploredException If we have left the opening book
 	 */
 	public Move getNextOpeningMove(Move otherPlayersMove) throws UnexploredException {
 	    System.out.println("opponent move: " + otherPlayersMove);
 	    adjustPosition(otherPlayersMove);
 	    
 	    Move moveToPlay =  (Move) currentOpening.children.keySet().toArray()[0];
 	    currentOpening = currentOpening.getNextPosition(moveToPlay);
 	    System.out.println("Playing the opening: "+ currentOpening.name);
 	    return moveToPlay;
 	}
     
 	private void adjustPosition(Move m) throws UnexploredException {
 	    if (null == currentOpening) {
 	        throw new UnexploredException();
 	    }
 	    if (firstMove) {
 	        reset();
 	        firstMove = false;
 	        if (!m.equals(Move.NO_MOVE)) {
 	            // They've already played
 	            currentOpening = currentOpening.getNextPosition(m);
 	        }
 	    } else {
 	        currentOpening = currentOpening.getNextPosition(m);
 	    }
	    if (null == currentOpening) {
	        throw new UnexploredException();
	    }
 	    System.out.println("Current opening: " + currentOpening.name);
 	    boolean noReply = currentOpening.children.keySet().size() == 0;
 	    if (noReply) {
 	        throw new UnexploredException();
 	    }
 	}
 	
 	public NamedPosition getNextPosition(Move m) throws UnexploredException {
 	    adjustPosition(m);
 	    return currentOpening;
 	}
 }
