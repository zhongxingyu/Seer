 package player.gamer.statemachine.eggplant;
 
 import util.statemachine.Move;
 
 class ValuedMove{
 	public int value;
 	public Move move;
 	public boolean terminal;
 	public ValuedMove(int value, Move move, boolean terminal){
 		this.value=value;
 		this.move = move;
 		this.terminal = terminal;
 	}
 	public ValuedMove(int value, Move move) {
 		this(value, move, true);
 	}
 	
 	@Override
 	public String toString() {
	  return this.move.toString() + " @ " + this.value;
 	}
 }
 
 class CacheValue {
 	public ValuedMove valuedMove;
 	public int alpha;
 	public int beta;
 	
 	public CacheValue(ValuedMove valuedMove, int alpha, int beta) {
 		super();
 		this.valuedMove = valuedMove;
 		this.alpha = alpha;
 		this.beta = beta;
 	}
 	
 	@Override
     public String toString() {
      return this.valuedMove.toString() + " : (" + this.alpha + ", " + this.beta + ")";
     }
 }
 
 @SuppressWarnings("serial")
 class TimeUpException extends Exception { }
