 package info.aki017.OpenRitsPen;
 
 import java.awt.Color;
 import java.util.LinkedList;
 
 public class OpenRitsPen {
 	private Frame mainframe;
 	private LinkedList<Line> lines = new LinkedList<Line>();
 	private LinkedList<Line> linesLog = new LinkedList<Line>();
 	private Pen pen;
 	
 	public OpenRitsPen(){
 		//初期化
 		mainframe = new Frame(this);
 		pen = new Pen();
 		SocketListener socketListener = new SocketListener(this);
 		socketListener.start();
 	}
 	
 	/**
 	 * Undo
 	 * @return 成功したかどうか
 	 */
 	public boolean undo() {
		if (!lines.isEmpty()) {
 			linesLog.add(lines.pollLast());
 			mainframe.repaint();
 			System.out.println("Undo");
 			return true;
 		}
 		System.err.println("Undo失敗");
 		return false;
 	}
 	
 	/**
 	 * Redo
 	 * @return 成功したかどうか
 	 */
 	public boolean redo() {
 		if (linesLog.size() > 0) {
 			lines.add(linesLog.pollLast());
 			mainframe.repaint();
 			System.out.println("Redo");
 			return true;
 		}
 		System.err.println("Redo失敗");
 		return false;
 	}
 
 	/**
 	 * リセット
 	 * @return 成功したかどうか
 	 */
 	public boolean reset() {
 		pen.reset();
 		lines.clear();
 		linesLog.clear();
 		mainframe.repaint();
 		return true;
 	}
 
 	/**
 	 * 描画する線を取得する
 	 * @return LinkedList<Line>
 	 */
 	public LinkedList<Line> getLines() {
 		LinkedList<Line> tmp = new LinkedList<Line>();
 		tmp.addAll(lines);
 		return tmp;
 	}
 
 	/**
 	 * ペンの角度を取得する
 	 * @return 角度
 	 */
 	public double getAngle() {
 		return pen.getAngle();
 	}
 
 	/**
 	 * 線を引く
 	 * @param len 長さ
 	 * @param angle 角度
 	 */
 	public void usePen(int len, int angle) {
 		double[] start = { pen.getX(), pen.getY() };
 		pen.addAngle(angle);
 		pen.forward(len);
 		Line line = new Line(start[0],start[1],pen.getX(),pen.getY(),pen.getColor(),pen.getSize());
 		lines.add(line);
 		try {
 			Thread.sleep(pen.getSpeed());
 		} catch (Exception e) {
 			System.err.println("Sleep Error");
 		}
 		System.out.println(">> draw line : length = " + len + "," + "angle = " + angle);
 		mainframe.repaint();
 	}
 
 	/**
 	 * RGB指定で色を変える
 	 * @param color Color
 	 */
 	public void changeColor(Color color) {
 		pen.setColor(color);
 	}
 	
 	/**
 	 * インデックス指定で色を変える
 	 * @param index インデックス
 	 */
 	public void changeColor(int index) {
 		pen.setColor(index);
 	}
 
 	/**
 	 * ペンの大きさを変える
 	 * @param size 大きさ
 	 */
 	public void changeSize(int size) {
 		pen.setSize(size);
 	}
 	
 	/**
 	 * ペンの描画スピードを変える
 	 * @param speed スピード
 	 */
 	public void changeSpeed(int speed) {
 		pen.setSpeed(speed);
 	}
 }
