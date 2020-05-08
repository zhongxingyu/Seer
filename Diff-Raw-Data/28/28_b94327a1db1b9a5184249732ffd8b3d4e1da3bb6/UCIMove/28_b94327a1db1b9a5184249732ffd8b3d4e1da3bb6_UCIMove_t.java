 package uci;
 
import state4.State4;

 public class UCIMove {
 	public enum MoveType{
 		Normal,
 		Promotion,
 		Null;
 	}
 	public enum PromotionType{
		Knight(State4.PROMOTE_KNIGHT),
		Queen(State4.PROMOTE_QUEEN),
		Bishop(State4.PROMOTE_BISHOP),
		Rook(State4.PROMOTE_ROOK);
		
		private final int code;
		PromotionType(int code){
			this.code = code;
		}
		
		/** get the correspoding {@link State4} promotion code*/
		int getCode(){
			return code;
		}
 	}
 	
 	final int[] move = new int[2];
 	MoveType type;
 	PromotionType ptype;
 }
