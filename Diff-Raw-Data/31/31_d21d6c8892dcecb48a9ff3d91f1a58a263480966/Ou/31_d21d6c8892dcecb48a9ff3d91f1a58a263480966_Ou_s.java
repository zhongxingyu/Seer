 package onshogi;
 
 import java.awt.Point;
 import java.util.HashSet;
 
 /**
  * 王の駒クラス．
  * @author nisshy
  */
 public class Ou extends Piece {
 	
 	/**
 	 * このインスタンスが一番初めに生成されたとき,
 	 * この駒の移動できる座標をmovePatternに格納する.
 	 */
 	private static HashSet<Point> movePattern;
 	static {
 		movePattern = new HashSet<Point>();
 		movePattern.add(new Point(0,-1));
 		movePattern.add(new Point(0,1));
 		movePattern.add(new Point(1,-1));
 		movePattern.add(new Point(1,0));
 		movePattern.add(new Point(1,1));
 		movePattern.add(new Point(-1,-1));
 		movePattern.add(new Point(-1,0));
 		movePattern.add(new Point(-1,1));
 		//System.out.println("static init test");
 	}
 	
 	
 	public Ou(boolean isBlackPiece) {
 		super(isBlackPiece);
 	}
 
 	@Override
 	HashSet<Point> getMovePattern() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
 	}
 
 	@Override
 	boolean hasPromoted() {
 		return false;
 	}
 
 	@Override
 	public Piece getPromoted() {
 		return null;
 	}
 
 	@Override
 	public Piece getOriginal() {
 		return this;
 	}
 
 	@Override
 	public String toString() {
 		return this.isBlackPiece ? " 王" : "v王";
 	}
 }
