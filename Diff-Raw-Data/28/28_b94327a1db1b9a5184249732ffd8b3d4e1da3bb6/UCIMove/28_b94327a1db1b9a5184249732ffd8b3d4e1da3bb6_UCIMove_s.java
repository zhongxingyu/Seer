 package uci;
 
 public class UCIMove {
 	public enum MoveType{
 		Normal,
 		Promotion,
 		Null;
 	}
 	public enum PromotionType{
		Knight,
		Queen,
		Bishop,
		Rook;
 	}
 	
 	final int[] move = new int[2];
 	MoveType type;
 	PromotionType ptype;
 }
