 package plotter;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.Point;
 import java.awt.Stroke;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionListener;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 @SuppressWarnings("serial")
 public class Plotter extends JPanel {
 
 	private static final Color B_GROUND_COLOR = Color.WHITE;
 	private static final Color LINE_COLOR = Color.BLUE;
 	private static final Color POINT_COLOR = Color.BLACK;
 	private static final Color RULE_COLOR = Color.LIGHT_GRAY;
 
 	public static int VIEWPORT_MAX = 10;
 	public static int VIEWPORT_MIN = -10;
 	public static int Y_MAX = 10;
 	public static int Y_MIN = -10;
 	public static int X_MAX = VIEWPORT_MAX;
 	public static int X_MIN = VIEWPORT_MIN;
 	public static double X_RULE = 1;
 	public static double Y_RULE = 1;
 	private static int WIDTH = X_MAX - X_MIN;
 	private static int HEIGHT = Y_MAX - Y_MIN;
 
 	private static final int POINT_DIAMETER = 4;
 	private static final double SLOPE_TOLLERANCE = Double.MAX_VALUE;
 
 	private Point mouseLoc;
 
 	private List<Point2D.Double> points;
 	private boolean connectedMode;
 	private boolean onTop = true;
 
 	private JFrame plotHolder;
 	private InstrumentPanel instruments;
 
 	public Plotter() {
 		this(true);
 	}
 
 	public Plotter(boolean connectedModeP) {
 		points = new ArrayList<Point2D.Double>();
 		setPreferredSize(new Dimension(400, 400));
 		setBackground(B_GROUND_COLOR);
 		addMouseMotionListener(mouseMove);
 		connectedMode = connectedModeP;
 	}
 
 	public void plotPoint(Point2D.Double p) {
 		points.add(p);
 		repaint();
 	}
 
 	public void addPoint(final double x, final double y) {
 		Runnable r = new Runnable() {
 			@Override
 			public void run() {
 				plotPoint(new Point2D.Double(x, y));
 			}
 		};
 		SwingUtilities.invokeLater(r);
 	}
 
 	public void clear() {
 		Runnable r = new Runnable() {
 			@Override
 			public void run() {
 				points.clear();
 				repaint();
 			}
 		};
 		SwingUtilities.invokeLater(r);
 
 	}
 
 	public void startPlotter() {
 		Runnable r = new Runnable() {
 			@Override
 			public void run() {
 				createAndRun();
 			}
 		};
 		SwingUtilities.invokeLater(r);
 	}
 
 	private void createAndRun() {
 		plotHolder = new JFrame("JPlotter");
 		plotHolder.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		plotHolder.add(this);
 		plotHolder.pack();
 		plotHolder.setAlwaysOnTop(onTop);
 		plotHolder.setVisible(true);
 		instruments = new InstrumentPanel();
 		instruments.setVisible(true);
 		addMouseMotionListener(mouseMove);
 	};
 
 	public void stopPlotter() {
 		Runnable r = new Runnable() {
 			@Override
 			public void run() {
 				points.clear();
 				plotHolder.dispose();
 			}
 		};
 		SwingUtilities.invokeLater(r);
 	}
 
 	public void zoomOut(int factor) {
 		X_RULE *= factor;
 		Y_RULE *= factor;
 		changeViewport(X_MAX * factor, X_MIN * factor, Y_MAX * factor, Y_MIN * factor);
 	}
 
 	public void zoomIn(int factor) {
 		X_RULE /= factor;
 		Y_RULE /= factor;
 		changeViewport(X_MAX / factor, X_MIN / factor, Y_MAX / factor, Y_MIN / factor);
 	}
 
 	public void widenView(int factor) {
 		X_RULE *= factor;
 		changeViewport(X_MAX * factor, X_MIN * factor, Y_MAX, Y_MIN);
 	}
 
 	public void thinView(int factor) {
 		X_RULE /= factor;
 		changeViewport(X_MAX / factor, X_MIN / factor, Y_MAX, Y_MIN);
 	}
 
 	public void growViewHeight(int factor) {
 		Y_RULE *= factor;
 		changeViewport(X_MAX, X_MIN, Y_MAX * factor, Y_MIN * factor);
 	}
 
 	public void shrinkViewHeight(int factor) {
 		Y_RULE /= factor;
 		changeViewport(X_MAX, X_MIN, Y_MAX / factor, Y_MIN / factor);
 	}
 
 	public void setRules(int xRule, int yRule) {
 		X_RULE = xRule;
 		Y_RULE = yRule;
 		repaint();
 	}
 
 	public void setXRule(int rule) {
 		X_RULE = rule;
 		repaint();
 	}
 
 	public void setYRule(int rule) {
 		Y_RULE = rule;
 		repaint();
 	}
 
 	private void changeViewport(int xMax, int xMin, int yMax, int yMin) {
 		if (xMax < 1)
 			return;
 		if (yMax < 1)
 			return;
 		X_MAX = xMax;
 		X_MIN = xMin;
 		Y_MAX = yMax;
 		Y_MIN = yMin;
 		VIEWPORT_MAX = X_MAX;
 		VIEWPORT_MIN = X_MIN;
 		WIDTH = X_MAX - X_MIN;
 		HEIGHT = Y_MAX - Y_MIN;
 		repaint();
 	}
 
 	@Override
 	protected void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		Graphics2D g2 = (Graphics2D) g;
 		paintGrid(g2);
 		paintPoints(g2);
 		if (mouseLoc != null)
 			paintMouseLocation(g2);
 	}
 
 	private void paintPoints(Graphics2D g) {
 		boolean paintHugeSlope = true;
 		Color originalColor = g.getColor();
 		Stroke originalStroke = g.getStroke();
 		g.setColor(POINT_COLOR);
 		g.setStroke(new BasicStroke(2));
 		Point oldJavaPoint = null;
 		Point2D.Double oldRegPoint = null;
 		for (Point2D.Double point : points) {
 			if (point.x > X_MAX || point.x < X_MIN) {
 				oldJavaPoint = null;
 				oldRegPoint = null;
 				continue;
 			}
 			if (point.y > Y_MAX || point.y < Y_MIN) {
 				oldJavaPoint = null;
 				oldRegPoint = null;
 				continue;
 			}
 			Point javaPoint = shiftPointToJavaCoord(point, getHeight(), getWidth());
 			if (connectedMode && oldRegPoint != null) {
 				double slope;
 				if (oldRegPoint.x - point.x != 0) {
 					slope = (oldRegPoint.y - point.y) / (double) (oldRegPoint.x - point.x);
 				} else {
 					slope = 0;
 				}
 				// For tan graphs we want to exclude the lines between points that shouldn't be
 				// there
 				if (paintHugeSlope || Math.abs(slope) < SLOPE_TOLLERANCE) {
 					g.setColor(LINE_COLOR);
 					g.drawLine(oldJavaPoint.x, oldJavaPoint.y, javaPoint.x, javaPoint.y);
 					g.setColor(POINT_COLOR);
 				}
 			} else {
 				g.fillOval(javaPoint.x - POINT_DIAMETER / 2, javaPoint.y - POINT_DIAMETER / 2,
 						POINT_DIAMETER, POINT_DIAMETER);
 			}
 			oldJavaPoint = javaPoint;
 			oldRegPoint = point;
 		}
 
 		g.setStroke(originalStroke);
 		g.setColor(originalColor);
 	}
 
 	private static Point shiftPointToJavaCoord(Point2D.Double cartesianCoord,
 			int componentHeight, int componentWidth) {
 		// this assumes the view is always centered on the origin
 		double relativeX = cartesianCoord.x + X_MAX;
 		double relativeY = cartesianCoord.y + Y_MAX;
 
 		int javaCoordX = (int) ((relativeX / WIDTH) * componentWidth);
 		int javaCoordY = componentHeight - (int) ((relativeY / HEIGHT) * componentHeight);
 		return new Point(javaCoordX, javaCoordY);
 	}
 
 	private static Point2D.Double shiftJavaPointToCartesianCoord(Point javaPoint,
 			int componentHeight, int componentWidth) {
 
 		double xPercent = javaPoint.x / (double) componentWidth;
 		double yPercent = javaPoint.y / (double) componentHeight;
 
 		double relativeX = xPercent * WIDTH;
 		double relativeY = yPercent * HEIGHT;
 
 		double absX = relativeX - X_MAX;
 		double absY = relativeY - Y_MAX;
 		return new Point2D.Double(absX, -absY);
 	}
 
 	private void paintMouseLocation(Graphics2D g) {
 		Color originalColor = g.getColor();
 		Stroke originalStroke = g.getStroke();
 		final int RECT_HEIGHT = 15;
 		final int RECT_WIDTH = 100;
 		g.setColor(B_GROUND_COLOR);
 		g.fillRect(0, 0, RECT_WIDTH, RECT_HEIGHT);
 		g.setColor(Color.LIGHT_GRAY);
 		g.drawRect(0, 0, RECT_WIDTH, RECT_HEIGHT);
 		g.setColor(Color.BLACK);
 		Point2D.Double cartPoint = shiftJavaPointToCartesianCoord(mouseLoc, getHeight(),
 				getWidth());
 		String toDraw = String.format("X:%.2f, Y:%.2f", cartPoint.x, cartPoint.y);
 		g.drawString(toDraw, 1, 12);
 
 		g.setStroke(originalStroke);
 		g.setColor(originalColor);
 	}
 
 	private void paintGrid(Graphics2D g) {
 		// save some things before changing them
 		Color originalColor = g.getColor();
 		Stroke originalStroke = g.getStroke();
 
 		g.setColor(RULE_COLOR);
 
 		final int MINI_LINE_LENGTH = 10;
 
 		// Main lines
 		g.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
 		g.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight());
 		// Mini lines
		for (double i = WIDTH / 2; i < WIDTH; i += X_RULE) {
 			// the ones on the x axis
 			g.drawLine((int) (i * getWidth() / WIDTH), getHeight() / 2 - MINI_LINE_LENGTH / 2,
 					(int) (i * getWidth() / WIDTH), getHeight() / 2 + MINI_LINE_LENGTH / 2);
			g.drawLine((int) ((WIDTH - i) * getWidth() / WIDTH), getHeight() / 2 - MINI_LINE_LENGTH / 2,
					(int) ((WIDTH - i) * getWidth() / WIDTH), getHeight() / 2 + MINI_LINE_LENGTH / 2);
 		}// Mini lines
		for (double i = WIDTH / 2; i < WIDTH; i += Y_RULE) {
 			// the ones on the y axis
 			g.drawLine(getWidth() / 2 - MINI_LINE_LENGTH / 2,
 					(int) (i * getHeight()) / HEIGHT, getWidth() / 2 + MINI_LINE_LENGTH / 2,
 					(int) (i * getHeight() / HEIGHT));
			g.drawLine(getWidth() / 2 - MINI_LINE_LENGTH / 2,
					(int) ((HEIGHT -i) * getHeight()) / HEIGHT, getWidth() / 2 + MINI_LINE_LENGTH / 2,
					(int) ((HEIGHT - i) * getHeight() / HEIGHT));
 		}
 
 		g.setStroke(originalStroke);
 		g.setColor(originalColor);
 	}
 
 	private MouseMotionListener mouseMove = new MouseMotionListener() {
 		@Override
 		public void mouseDragged(MouseEvent e) {
 		}
 
 		@Override
 		public void mouseMoved(MouseEvent e) {
 			final int TOP_BAR_OFFSET = 0;
 			mouseLoc = e.getPoint();
 			mouseLoc.y -= TOP_BAR_OFFSET;
 			Plotter.this.repaint(true);
 		}
 	};
 
 	private void repaint(boolean paintOnlyMouseCoord) {
 		Graphics2D g = (Graphics2D) getGraphics();
 		paintMouseLocation(g);
 	}
 
 	@SuppressWarnings("unused")
 	private class InstrumentPanel extends JFrame {
 		private JButton zoomInButton;
 		private JButton zoomOutButton;
 		private JSlider xRuleSlider;
 		private JSlider yRuleSlider;
 
 		public InstrumentPanel() {
 			super("Plotter Controls");
 			setDefaultCloseOperation(EXIT_ON_CLOSE);
 			zoomInButton = new JButton("Zoom In");
 			zoomOutButton = new JButton("Zoom Out");
 			xRuleSlider = new JSlider(1, X_MAX / 2);
 			yRuleSlider = new JSlider(1, Y_MAX / 2);
 			xRuleSlider.setPaintLabels(true);
 			xRuleSlider.setPaintTicks(true);
 			xRuleSlider.setMajorTickSpacing(1);
 			xRuleSlider.setSnapToTicks(true);
			xRuleSlider.setValue(1);
 			yRuleSlider.setPaintLabels(true);
 			yRuleSlider.setPaintTicks(true);
 			yRuleSlider.setMajorTickSpacing(1);
 			yRuleSlider.setSnapToTicks(true);
			yRuleSlider.setValue(1);
 			xRuleSlider.addChangeListener(sliderChange);
 			yRuleSlider.addChangeListener(sliderChange);
 			JPanel mainPane = new JPanel(new GridBagLayout());
 			GridBagConstraints gc = new GridBagConstraints();
 			gc.gridy = 0;
 			mainPane.add(zoomInButton, gc);
 			mainPane.add(zoomOutButton, gc);
 			gc.gridy++;
 			mainPane.add(xRuleSlider, gc);
 			mainPane.add(yRuleSlider, gc);
 			add(mainPane);
 			zoomInButton.addActionListener(zoomInAction);
 			zoomOutButton.addActionListener(zoomOutAction);
 			pack();
 		}
 
 		private ActionListener zoomInAction = new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				zoomIn(2);
 			}
 		};
 		private ActionListener zoomOutAction = new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				zoomOut(2);
 			}
 		};
 
 		private ChangeListener sliderChange = new ChangeListener() {
 			@Override
 			public void stateChanged(ChangeEvent e) {
 				setRules(xRuleSlider.getValue(), yRuleSlider.getValue());
 			}
 		};
 	}
 }
