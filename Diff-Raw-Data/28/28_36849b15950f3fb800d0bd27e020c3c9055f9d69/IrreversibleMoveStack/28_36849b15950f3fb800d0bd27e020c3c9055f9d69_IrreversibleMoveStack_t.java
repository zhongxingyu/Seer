 package mitzi;
 
 import java.util.LinkedList;
 
 /**
  * This class represents a stack, storing the information, which cannot be
  * reverted only with a given move. It is implemented as a LinkedList containing
  * a class which stores the half move clock, the castling, the en passant target
  * and the captured piece. (en passant captures does not count as capture). The
  * elements should be accessed via irr_move_info.removeLast();
  */
 public class IrreversibleMoveStack {
 
 	static public class MoveInfo {
 
 		int half_move_clock;
 		int[] castling = new int[4];
 		int en_passant_square;
 		Piece capture;
		Boolean is_check;
 
 	}
 
 	/**
 	 * the stack containing the information
 	 */
 	static public LinkedList<MoveInfo> irr_move_info = new LinkedList<MoveInfo>();
 
 	private IrreversibleMoveStack() {
 	}
 
 	/**
 	 * add a new entry.
 	 * 
 	 * @param half_move_clock
 	 *            the old half move clock
 	 * @param castling
 	 *            the castling array
 	 * @param en_passant_square
 	 *            the en passant target square
 	 * @param capture
 	 *            the piece, which got captured (null if no capture)
 	 */
 	static public void addInfo(int half_move_clock, int[] castling,
			int en_passant_square, Piece capture, Boolean is_check) {
 
 		MoveInfo inf = new MoveInfo();
 		System.arraycopy(castling, 0, inf.castling, 0, 4);
 		inf.en_passant_square = en_passant_square;
 		inf.half_move_clock = half_move_clock;
 		inf.capture = capture;
		inf.is_check = is_check;
 
 		irr_move_info.addLast(inf);
 	}
 
 }
