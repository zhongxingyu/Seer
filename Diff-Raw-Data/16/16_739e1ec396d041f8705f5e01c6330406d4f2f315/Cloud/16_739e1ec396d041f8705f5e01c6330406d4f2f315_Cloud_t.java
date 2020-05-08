package tk.bnbm.clockdrive4j.model;
 
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * 「背景の雲」を司るクラス。
  *
  * @author kazuhito_m
  */
 public class Cloud {
 
 	// TODO 四角形を扱うのであれば、概念的に「座標」より「矩形」のほうが良いのでは？
 
 	/** 背景サイズ */
 	private Point2D.Double backgroundSize;
 
 	/** 雲の座標情報群 */
 	private List<Point2D.Double> positions;
 
 	/**
 	 * コンストラクタ。<br>
 	 * 背景サイズや、雲の数など、基本的に生成したい情報を渡す。
 	 *
 	 * @param width 背景の横長。
 	 * @param height 背景の縦長。
 	 * @param count 雲の数。
 	 */
 	public Cloud(int width, int height, int count) {
 		// 資源の初期化。
 		backgroundSize = new Point2D.Double(width, height);
 		positions = new ArrayList<Point2D.Double>();
 		// 指定数分の雲を作成。
 		for (int i = 0; i < count; i++) {
 			double x = getRundum(width);
 			double y = height * 2 / count * i;
 			positions.add(new Point2D.Double(x, y));
 		}
 	}
 
 	/**
 	 * 引数に与えた上限から、その領域内でランダムな位置を返す。
 	 *
 	 * @param limit 上限座標。
 	 * @return 計算後ランダムな座標。
 	 */
 	protected float getRundum(double limit) {
 		return (float) (Math.random() * limit * 2 - limit * 0.5);
 	}
 
 	/**
 	 * 指定した経過時間で保持している雲座標情報を移動させる。
 	 *
 	 * @param elapsed 経過時間。
 	 */
 	public void move(double elapsed) {
 		for (Point2D.Double p : positions) {
 
 			double newX = p.getX() - elapsed;
 			double newY = p.getY() - elapsed;
 
 			double x = backgroundSize.getX();
 			double y = backgroundSize.getY();
 
 			if (newX < -x * 0.5) {
 				newX += x * 2;
 			}
 			if (newY < -y * 0.5) {
 				newY += y * 2;
 				newX = getRundum(x);
 			}
 			// 新しい座標に移動(インスタンスはそのまま)
 			p.setLocation(newX, newY);
 		}
 	}
 
 	// プロパティ群
 
 	/**
 	 * 雲の座標情報群を返す。
 	 *
 	 * @return 座標情報のリスト。
 	 */
 	public List<Point2D.Double> getPositions() {
 		return this.positions;
 	}
 }
