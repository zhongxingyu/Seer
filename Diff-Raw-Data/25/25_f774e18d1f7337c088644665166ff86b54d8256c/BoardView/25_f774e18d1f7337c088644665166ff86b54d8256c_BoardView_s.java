 package board;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Stroke;
 
 import javax.swing.JComponent;
 
 import board.Board.NoPhysicsDataException;
 import board.Board.Physics;
 
 import agent.Agent;
 import agent.AgentView;
 
 /**
  * Visual representation of a Board.
  */
 public final class BoardView extends JComponent {
 	private static final long serialVersionUID = 1L;
 
 	private Board board = null;
 
 	/** [px/m] -- screen pixels for every real meter */
 	private static final double SCALE = 40;
 
 	private Dimension dimension = new Dimension(0, 0);
 
 	public void setBoard(Board board) {
 		if (this.board == board)
 			return;
 
 		this.board = board;
 
 		if (board == null)
 			dimension = new Dimension(0, 0);
 		else
 			dimension = new Dimension((int) Math.round(board.getDimension().x
 					* SCALE), (int) Math.round(board.getDimension().y * SCALE));
 	}
 
 	@Override
 	public Dimension getPreferredSize() {
 		return dimension;
 	}
 
 	@Override
 	public Dimension getMinimumSize() {
 		return dimension;
 	}
 
 	@Override
 	public Dimension getMaximumSize() {
 		return dimension;
 	}
 
 	public void paintComponent(Graphics g) {
 		if (board == null)
 			return;
 
 		paintBackground(g);
 		paintObstacles(g);
 		paintExits(g);
 		paintAgents(g);
 	}
 
 	/** Zamienia cigy Point na Board-zie na piksel.x na BoardView. */
 	public int translateX(Point p) {
 		return (int) Math.round(p.x * SCALE);
 	}
 
 	/** Zamienia cigy Point na Board-zie na piksel.y na BoardView. */
 	public int translateY(Point p) {
 		return (int) Math.round((board.getDimension().y - p.y) * SCALE);
 	}
 
 	private Color colorFromTemperature(double t) {
 		float blueC = 0.67f;
 		float blueT = 10.0f; // [*C]
 
 		float redC = 0.00f;
 		float redT = 100.0f; // [*C]
 
 		if (t > redT)
 			t = redT;
 		else if (t < blueT)
 			t = blueT;
 
 		float a = (blueC - redC) / (blueT - redT);
 		float b = blueC - blueT * a;
 
 		float c = a * (float) t + b;
 
 		return Color.getHSBColor(c, 1.0f, 2.0f);
 	}
 
 	private void paintBackground(Graphics g) {
 		Point p = new Point();
 		Color lightGray = new Color(0xEEEEEE);
 
 		for (p.x = 0.0; p.x < board.getDimension().x; p.x += board.dataCellDimension.x)
 			for (p.y = 0.0; p.y < board.getDimension().y; p.y += board.dataCellDimension.y) {
 				Color c = Color.WHITE;
 				try {
 					c = colorFromTemperature(board.getPhysics(p,
 							Physics.TEMPERATURE));
 				} catch (NoPhysicsDataException e) {
 				}
 
 				// cells are painted top-down, so... push(p.y)
 				double tmp = p.y;
 				p.y += board.dataCellDimension.y;
 
 				int x = translateX(p);
 				int y = translateY(p);
 				Point p2 = new Point(p.x + board.dataCellDimension.x, p.y
 						+ board.dataCellDimension.y);
 				int w = translateX(p2) - x;
 				int h = y - translateY(p2);
 
 				// pop(p.y)
 				p.y = tmp;
 
 				g.setColor(c);
 				g.fillRect(x, y, w, h);
 				g.setColor(lightGray);
 				g.drawRect(x, y, w, h);
 			}
 	}
 
 	private void paintObstacles(Graphics g) {
 		for (Board.Obstacle obstacle : board.obstacles) {
 			Point p1 = obstacle.getStartPoint();
 			Point p2 = obstacle.getEndPoint();
 
			int x = translateX(p1);
			int y = translateY(p1);
			int w = translateX(p2) - x;
			int h = y - translateY(p2);
 
 			g.setColor(Color.BLACK);
 			g.fillRect(x, y, w, h);
 		}
 	}
 
 	private void paintExits(Graphics g) {
 		Graphics2D g2 = (Graphics2D) g;
 		g2.setColor(Color.BLACK);
 		Stroke s = g2.getStroke();
 		g2.setStroke(new BasicStroke(5));
 		for (Board.Exit exit : board.exits) {
 			Point p1 = exit.getStartPoint();
 			Point p2 = exit.getEndPoint();
 
 			int x1 = translateX(p1);
 			int y1 = translateY(p1);
 			int x2 = translateX(p2);
 			int y2 = translateY(p2);
 
 			g2.drawLine(x1, y1, x2, y2);
 		}
 		g2.setStroke(s);
 	}
 
 	private void paintAgents(Graphics g) {
 		for (Agent agent : board.agents)
 			new AgentView(agent).paint(this, (Graphics2D) g, SCALE);
 	}
 
 	/**
 	 * Rysuje skierowany w gr wektor o zadanej dugoci.
 	 * 
 	 * @param forward
 	 */
 	public static void drawVector(Graphics2D g, int x, int y, int length,
 			boolean forward) {
 		g.fillOval(x - 2, y - 2, 4, 4);
 		g.drawLine(x, y, 0, -length);
 		// -length, not '+'! odwrotne wsprzdne na OY obrazka ni OY
 		// prawdziwym
 		g.drawLine(x, y - length, x - 2, y - length + (forward ? 4 : -4));
 		g.drawLine(x, y - length, x + 2, y - length + (forward ? 4 : -4));
 	}
 
 }
