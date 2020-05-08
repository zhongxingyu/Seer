 package mitzi;
 
 import java.util.Locale;
 
 public class Move implements IMove {
 
 	private int src;
 
 	private int dest;
 
 	private int promotion;
 
 	/**
 	 * Move constructor
 	 * 
 	 * @param src
 	 *            Source
 	 * @param dest
 	 *            Destination
 	 * @param promotion
 	 *            Promotion (if no, then omit)
 	 */
 	public Move(int src, int dest, int promotion) {
 		this.src = src;
 		this.dest = dest;
 		this.promotion = promotion;
 	}
 
 	public Move(int src, int dest) {
 		this(src, dest, 0);
 	}
 
 	public Move(String notation) {
 		String[] squares = new String[2];
 
 		squares[0] = notation.substring(0, 2);
 		squares[1] = notation.substring(2, 4);
 
 		src = SquareHelper.fromString(squares[0]);
		dest = SquareHelper.fromString(squares[1]);
 
 		if (notation.length() > 4) {
 			String promo_string = notation.substring(4, 5).toLowerCase(
 					Locale.ENGLISH);
 			if (promo_string.equals("q")) {
 				promotion = PieceHelper.QUEEN;
 			} else if (promo_string.equals("r")) {
 				promotion = PieceHelper.ROOK;
 			} else if (promo_string.equals("n")) {
 				promotion = PieceHelper.KNIGHT;
 			} else if (promo_string.equals("b")) {
 				promotion = PieceHelper.BISHOP;
 			}
 		} else {
 			promotion = 0;
 		}
 	}
 
 	@Override
 	public int getFromSquare() {
 		return src;
 	}
 
 	@Override
 	public int getToSquare() {
 		return dest;
 	}
 
 	@Override
 	public int getPromotion() {
 		return promotion;
 	}
 
 	@Override
 	public String toString() {
 		String promote_to;
 		if (getPromotion() != 0) {
 			promote_to = PieceHelper.toString(getPromotion());
 		} else {
 			promote_to = "";
 		}
 		return SquareHelper.toString(getFromSquare())
 				+ SquareHelper.toString(getToSquare()) + promote_to;
 	}
 
 }
