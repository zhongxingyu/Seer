 package onshogi;
 
 import java.awt.Point;
 import java.util.HashSet;
 
 /**
  * 将棋の駒を表すPieceクラス
  * @author nisshy
  */
 abstract public class Piece {
 	/**
 	 * 先手番の駒か否か．
 	 */
 	protected boolean isBlackPiece;
 	
 	/**
 	 * Komaインスタンスを生成する．
 	 * @param isBlackPiece 先手番の駒か否か．
 	 */
 	public Piece(boolean isBlackPiece) {
 		this.isBlackPiece = isBlackPiece; 
 	}
 	
 	/**
 	 * この駒の移動バターンを応答する．
 	 * @return 移動パターンを表すPointのHashSet
 	 */
 	abstract HashSet<Point> getMovePattern();
 
 	/**
 	 * この駒が成っているか否かを応答する．
 	 * @return この駒が成っているか否か．
 	 */
 	abstract boolean hasPromoted();
 
 	/**
 	 * この駒が先手の駒か否かを応答する．
 	 * @return この駒が，先手の駒か否か．
 	 */
 	public boolean isBlackPiece() {
 		return this.isBlackPiece;
 	}
 	
 	/**
 	 * この駒が，指定された手番から見て，相手番の駒かを応答する．
 	 * @param isBlack 先手番か否か
 	 * @return 指定された手番から見て，相手の駒が否か
 	 */
 	public boolean isEnemyPiece(boolean isBlack) {
 		return isBlack ? !this.isBlackPiece : this.isBlackPiece;
 	}
 	
 	/**
 	 * この駒が，指定された手番の駒かを応答する．
 	 * @param isBlack 先手番か否か
 	 * @return 指定された手番の駒が否か
 	 */
 	public boolean isSelfPiece(boolean isBlack) {
 		return isBlack ? this.isBlackPiece : !this.isBlackPiece;
 	}
 
 	/**
 	 * この駒が成駒であれば，成る前の駒を返す．
 	 * そうでなければ，thisを返す．
 	 * @return 
 	 */
 	abstract public Piece getOriginal();
 
 	/**
 	 * この駒が成れる場合は，成駒のインスタンスを応答する．
 	 * そうでなければ，nullを応答する．
 	 * @return この駒が，成った後の駒のインスタンス
 	 */
 	abstract public Piece getPromoted();
 }
