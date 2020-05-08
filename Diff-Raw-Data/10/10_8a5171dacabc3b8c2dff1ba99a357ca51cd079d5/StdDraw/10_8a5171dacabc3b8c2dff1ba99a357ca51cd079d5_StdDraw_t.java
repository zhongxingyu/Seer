 package draw;
 
 /*************************************************************************
  * @OriginalAuthor: 	Robert Sedgewick and Kevin Wayne
  * @author:				Devin Barry
  * @location:			introcs.cs.princeton.edu
  * @Date:				???
  * @ModifiedBy:			Devin Barry
  * @FirstModified:		09.10.2012
  * @LastModified:		09.01.2013
  * 
  * Shape drawing methods in the library have been substantially modified by
  * Devin. At a certain point the methods became clumsy inside this massive
  * class and I moved them into their own (also static) class called
  * ShapeAssist. Now all the shape drawing methods from StdDraw call matching
  * methods in ShapeAssist to generate the polygons to be drawn.
  * 
  * The modifications allow the drawing of several new shapes and add
  * additional method signatures for old shapes too.
  * 
  * Devin's major modifications to this library include adding all the methods
  * that draw shapes or text using using Point2D.Double rather than a set of
  * x and y co-ordinates.
  * 
  * Added the diamond and filledDiamond methods
  * 
  * Added the triangle and filledTriangle methods
  * 
  * Adding the angledRectangle and two filledAngledRectangle methods using
  * the AffineTransform.
  * 
  * Changed the polygon method from using the legacy GeneralPath class to
  * using the newer Path2D.Double class, which implements the Shape interface.
  * 
  * Removed static colour references to their own static class draw.Colors
  * 
  * Additional comments and formatting. Changing of spelling to New Zealand
  * English. i.e. centre and centred
  * 
  * ************************************************************************
  * 
  *  Compilation:  javac StdDraw.java
  *  Execution:    java StdDraw
  *
  *  Standard drawing library. This class provides a basic capability for
  *  creating drawings with your programs. It uses a simple graphics model that
  *  allows you to create drawings consisting of points, lines, and curves
  *  in a window on your computer and to save the drawings to a file.
  *
  *  Todo
  *  ----
  *    -  Add support for gradient fill, etc.
  *
  *  Remarks
  *  -------
  *    -  don't use AffineTransform for rescaling since it inverts
  *       images and strings
  *    -  careful using setFont in inner loop within an animation -
  *       it can cause flicker
  *
  *************************************************************************/
 import draw.Colors;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.FileDialog;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.MediaTracker;
 import java.awt.RenderingHints;
 import java.awt.Toolkit;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.KeyListener;
 import java.awt.event.KeyEvent;
 
 import java.awt.geom.*;
 import java.awt.image.*;
 import java.io.*;
 import java.net.*;
 import java.util.LinkedList;
 import java.util.TreeSet;
 import javax.imageio.ImageIO;
 import javax.swing.*;
 
 /**
  * <i>Standard draw</i>. This class provides a basic capability for creating
  * drawings with your programs. It uses a simple graphics model that allows you
  * to create drawings consisting of points, lines, and curves in a window on
  * your computer and to save the drawings to a file.
  * <p>
  * For additional documentation, see <a
  * href="http://introcs.cs.princeton.edu/15inout">Section 1.5</a> of
  * <i>Introduction to Programming in Java: An Interdisciplinary Approach</i> by
  * Robert Sedgewick and Kevin Wayne.
  */
 public final class StdDraw implements ActionListener, MouseListener, MouseMotionListener, KeyListener {
 
 	// default colours
 	private static final Color DEFAULT_PEN_COLOR = Colors.BLACK;
 	private static final Color DEFAULT_CLEAR_COLOR = Colors.WHITE;
 
 	// current pen colour
 	private static Color penColor;
 
 	// default canvas size is DEFAULT_SIZE-by-DEFAULT_SIZE
 	private static final int DEFAULT_SIZE = 512;
 	private static int width = DEFAULT_SIZE;
 	private static int height = DEFAULT_SIZE;
 
 	// default pen radius
 	private static final double DEFAULT_PEN_RADIUS = 0.002;
 
 	// current pen radius
 	private static double penRadius;
 
 	// show we draw immediately or wait until next show?
 	private static boolean defer = false;
 
 	// boundary of drawing canvas, 5% border
 	private static final double BORDER = 0.05;
 	private static final double DEFAULT_XMIN = 0.0;
 	private static final double DEFAULT_XMAX = 1.0;
 	private static final double DEFAULT_YMIN = 0.0;
 	private static final double DEFAULT_YMAX = 1.0;
 	private static double xmin, ymin, xmax, ymax;
 
 	// for synchronisation
 	private static Object mouseLock = new Object();
 	private static Object keyLock = new Object();
 
 	// default font
	private static final Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN, 16);
 
 	// current font
 	private static Font font;
 
 	// double buffered graphics
 	private static BufferedImage offscreenImage, onscreenImage;
 	private static Graphics2D offscreen, onscreen;
 
 	// singleton for callbacks: avoids generation of extra .class files
 	private static StdDraw std = new StdDraw();
 
 	// the frame for drawing to the screen
 	private static JFrame frame;
 
 	// mouse state
 	private static boolean mousePressed = false;
 	private static double mouseX = 0;
 	private static double mouseY = 0;
 
 	// queue of typed key characters
 	private static LinkedList<Character> keysTyped = new LinkedList<Character>();
 
 	// set of key codes currently pressed down
 	private static TreeSet<Integer> keysDown = new TreeSet<Integer>();
 
 	// singleton pattern: client can't instantiate
 	private StdDraw() {
 	}
 
 	// static initialiser
 	static {
 		init();
 	}
 
 	/**
 	 * Set the window size to the default size 512-by-512 pixels.
 	 */
 	public static void setCanvasSize() {
 		setCanvasSize(DEFAULT_SIZE, DEFAULT_SIZE);
 	}
 
 	/**
 	 * Set the window size to w-by-h pixels.
 	 * 
 	 * @param w
 	 *            the width as a number of pixels
 	 * @param h
 	 *            the height as a number of pixels
 	 * @throws a
 	 *             RunTimeException if the width or height is 0 or negative
 	 */
 	public static void setCanvasSize(int w, int h) {
 		if (w < 1 || h < 1)
 			throw new RuntimeException("width and height must be positive");
 		width = w;
 		height = h;
 		init();
 	}
 
 	// init
 	private static void init() {
 		if (frame != null) frame.setVisible(false);
 		
 		frame = new JFrame();
 		offscreenImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
 		onscreenImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
 		offscreen = offscreenImage.createGraphics();
 		onscreen = onscreenImage.createGraphics();
 		setXscale();
 		setYscale();
 		offscreen.setColor(DEFAULT_CLEAR_COLOR);
 		offscreen.fillRect(0, 0, width, height);
 		setPenColor();
 		setPenRadius();
 		setFont();
 		clear();
 
 		// add anti-aliasing
 		RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
 		offscreen.addRenderingHints(hints);
 
 		// frame stuff
 		ImageIcon icon = new ImageIcon(onscreenImage);
 		JLabel draw = new JLabel(icon);
 
 		draw.addMouseListener(std);
 		draw.addMouseMotionListener(std);
 
 		frame.setContentPane(draw);
 		frame.addKeyListener(std); // JLabel cannot get keyboard focus
 		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // closes all windows
		// frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // closes only current window
 		frame.setTitle("Standard Draw");
 		frame.setJMenuBar(createMenuBar());
 		frame.pack();
 		frame.requestFocusInWindow();
 		frame.setVisible(true);
 	}
 
 	// create the menu bar (changed to private)
 	private static JMenuBar createMenuBar() {
 		JMenuBar menuBar = new JMenuBar();
 		JMenu menu = new JMenu("File");
 		menuBar.add(menu);
 		JMenuItem saveItem = new JMenuItem(" Save...   ");
 		saveItem.addActionListener(std);
 		saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 		menu.add(saveItem);
 		return menuBar;
 	}
 
 	/*************************************************************************
 	 * User and screen coordinate systems
 	 *************************************************************************/
 
 	/**
 	 * Set the x-scale to be the default (between 0.0 and 1.0).
 	 */
 	public static void setXscale() {
 		setXscale(DEFAULT_XMIN, DEFAULT_XMAX);
 	}
 
 	/**
 	 * Set the y-scale to be the default (between 0.0 and 1.0).
 	 */
 	public static void setYscale() {
 		setYscale(DEFAULT_YMIN, DEFAULT_YMAX);
 	}
 
 	/**
 	 * Set the x-scale (a 10% border is added to the values)
 	 * 
 	 * @param min
 	 *            the minimum value of the x-scale
 	 * @param max
 	 *            the maximum value of the x-scale
 	 */
 	public static void setXscale(double min, double max) {
 		double size = max - min;
 		synchronized (mouseLock) {
 			xmin = min - BORDER * size;
 			xmax = max + BORDER * size;
 		}
 	}
 
 	/**
 	 * Set the y-scale (a 10% border is added to the values).
 	 * 
 	 * @param min
 	 *            the minimum value of the y-scale
 	 * @param max
 	 *            the maximum value of the y-scale
 	 */
 	public static void setYscale(double min, double max) {
 		double size = max - min;
 		synchronized (mouseLock) {
 			ymin = min - BORDER * size;
 			ymax = max + BORDER * size;
 		}
 	}
 
 	/**
 	 * Set the x-scale and y-scale (a 10% border is added to the values)
 	 * 
 	 * @param min
 	 *            the minimum value of the x- and y-scales
 	 * @param max
 	 *            the maximum value of the x- and y-scales
 	 */
 	public static void setScale(double min, double max) {
 		setXscale(min, max);
 		setYscale(min, max);
 	}
 
 	// helper functions that scale from user coordinates to screen coordinates
 	// and back
 	public static double scaleX(double x) {
 		return width * (x - xmin) / (xmax - xmin);
 	}
 
 	public static double scaleY(double y) {
 		return height * (ymax - y) / (ymax - ymin);
 	}
 
 	public static double factorX(double w) {
 		return w * width / Math.abs(xmax - xmin);
 	}
 
 	public static double factorY(double h) {
 		return h * height / Math.abs(ymax - ymin);
 	}
 
 	private static double userX(double x) {
 		return xmin + x * (xmax - xmin) / width;
 	}
 
 	private static double userY(double y) {
 		return ymax - y * (ymax - ymin) / height;
 	}
 
 	/**
 	 * Clear the screen to the default colour (white).
 	 */
 	public static void clear() {
 		clear(DEFAULT_CLEAR_COLOR);
 	}
 
 	/**
 	 * Clear the screen to the given colour.
 	 * 
 	 * @param color
 	 *            the Color to make the background
 	 */
 	public static void clear(Color color) {
 		offscreen.setColor(color);
 		offscreen.fillRect(0, 0, width, height);
 		offscreen.setColor(penColor);
 		draw();
 	}
 
 	/**
 	 * Get the current pen radius.
 	 */
 	public static double getPenRadius() {
 		return penRadius;
 	}
 
 	/**
 	 * Set the pen size to the default (.002).
 	 */
 	public static void setPenRadius() {
 		setPenRadius(DEFAULT_PEN_RADIUS);
 	}
 
 	/**
 	 * Set the radius of the pen to the given size.
 	 * 
 	 * @param r
 	 *            the radius of the pen
 	 * @throws RuntimeException
 	 *             if r is negative
 	 */
 	public static void setPenRadius(double r) {
 		if (r < 0)
 			throw new RuntimeException("pen radius must be positive");
 		penRadius = r;
 		float scaledPenRadius = (float) (r * DEFAULT_SIZE);
 		BasicStroke stroke = new BasicStroke(scaledPenRadius, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
 		// BasicStroke stroke = new BasicStroke(scaledPenRadius);
 		offscreen.setStroke(stroke);
 	}
 
 	/**
 	 * Get the current pen colour.
 	 */
 	public static Color getPenColor() {
 		return penColor;
 	}
 
 	/**
 	 * Set the pen colour to the default colour (black).
 	 */
 	public static void setPenColor() {
 		setPenColor(DEFAULT_PEN_COLOR);
 	}
 
 	/**
 	 * Set the pen colour to the given colour. The available pen colours are BLACK,
 	 * BLUE, CYAN, DARK_GRAY, GRAY, GREEN, LIGHT_GRAY, MAGENTA, ORANGE, PINK,
 	 * RED, WHITE, and YELLOW.
 	 * 
 	 * @param color
 	 *            the Color to make the pen
 	 */
 	public static void setPenColor(Color color) {
 		penColor = color;
 		offscreen.setColor(penColor);
 	}
 
 	/**
 	 * Get the current font.
 	 */
 	public static Font getFont() {
 		return font;
 	}
 
 	/**
 	 * Set the font to the default font (sans serif, 16 point).
 	 */
 	public static void setFont() {
 		setFont(DEFAULT_FONT);
 	}
 
 	/**
 	 * Set the font to the given value.
 	 * 
 	 * @param f
 	 *            the font to make text
 	 */
 	public static void setFont(Font f) {
 		font = f;
 	}
 
 	/*************************************************************************
 	 * Drawing geometric shapes.
 	 *************************************************************************/
 
 	/**
 	 * Draw a line from (x0, y0) to (x1, y1).
 	 * 
 	 * @param x0
 	 *            the x-coordinate of the starting point
 	 * @param y0
 	 *            the y-coordinate of the starting point
 	 * @param x1
 	 *            the x-coordinate of the destination point
 	 * @param y1
 	 *            the y-coordinate of the destination point
 	 */
 	public static void line(double x0, double y0, double x1, double y1) {
 		offscreen.draw(new Line2D.Double(scaleX(x0), scaleY(y0), scaleX(x1),
 				scaleY(y1)));
 		draw();
 	}
 
 	/**
 	 * Draw one pixel at (x, y).
 	 * 
 	 * @param x
 	 *            the x-coordinate of the pixel
 	 * @param y
 	 *            the y-coordinate of the pixel
 	 */
 	private static void pixel(double x, double y) {
 		offscreen.fillRect((int) Math.round(scaleX(x)),
 				(int) Math.round(scaleY(y)), 1, 1);
 	}
 
 	/**
 	 * Draw a point at (x, y).
 	 * 
 	 * @param x
 	 *            the x-coordinate of the point
 	 * @param y
 	 *            the y-coordinate of the point
 	 */
 	public static void point(double x, double y) {
 		double r = penRadius; // r is the pen radius
 
 		// double ws = factorX(2*r);
 		// double hs = factorY(2*r);
 		// if (ws <= 1 && hs <= 1) pixel(x, y);
 		if (r <= 1)
 			pixel(x, y);
 		else
 			offscreen.fill(ShapeAssist.point(x, y, r));
 		draw();
 	}
 
 	/**
 	 * Draw a circle of radius r, centred on (x, y).
 	 * 
 	 * @param x
 	 *            the x-coordinate of the centre of the circle
 	 * @param y
 	 *            the y-coordinate of the centre of the circle
 	 * @param r
 	 *            the radius of the circle
 	 * @throws RuntimeException
 	 *             if the radius of the circle is negative
 	 */
 	public static void circle(double x, double y, double r) {
 		offscreen.draw(ShapeAssist.circle(x, y, r));
 		draw();
 	}
 
 	/**
 	 * Draw a circle of radius r, centred at <location>.
 	 * 
 	 * @param location
 	 *            the Point at the centre of the circle
 	 * @param r
 	 *            the radius of the circle
 	 * @throws RuntimeException
 	 *             if the radius of the circle is negative
 	 */
 	public static void circle(Point2D.Double location, double r) {
 		circle(location.getX(), location.getY(), r);
 	}
 
 	/**
 	 * Draw filled circle of radius r, centred on (x, y).
 	 * 
 	 * @param x
 	 *            the x-coordinate of the centre of the circle
 	 * @param y
 	 *            the y-coordinate of the centre of the circle
 	 * @param r
 	 *            the radius of the circle
 	 * @throws RuntimeException
 	 *             if the radius of the circle is negative
 	 */
 	public static void filledCircle(double x, double y, double r) {
 		offscreen.fill(ShapeAssist.circle(x, y, r));
 		draw();
 	}
 
 	/**
 	 * Draw filled circle of radius r, centred at <location>.
 	 * 
 	 * @param location
 	 *            the Point at the centre of the circle
 	 * @param r
 	 *            the radius of the circle
 	 * @throws RuntimeException
 	 *             if the radius of the circle is negative
 	 */
 	public static void filledCircle(Point2D.Double location, double r) {
 		filledCircle(location.getX(), location.getY(), r);
 	}
 
 	/**
 	 * Draw an ellipse with given semimajor and semiminor axes, centered on (x,
 	 * y).
 	 * 
 	 * @param x
 	 *            the x-coordinate of the centre of the ellipse
 	 * @param y
 	 *            the y-coordinate of the centre of the ellipse
 	 * @param semiMajorAxis
 	 *            is the semimajor axis of the ellipse
 	 * @param semiMinorAxis
 	 *            is the semiminor axis of the ellipse
 	 * @throws RuntimeException
 	 *             if either of the axes are negative
 	 */
 	public static void ellipse(double x, double y, double semiMajorAxis,
 			double semiMinorAxis) {
 		
 		offscreen.draw(ShapeAssist.ellipse(x, y, semiMajorAxis, semiMinorAxis));
 		draw();
 	}
 
 	/**
 	 * Draw a filled ellipse with given semimajor and semiminor axes,
 	 * centred on (x, y).
 	 * 
 	 * @param x
 	 *            the x-coordinate of the centre of the ellipse
 	 * @param y
 	 *            the y-coordinate of the centre of the ellipse
 	 * @param semiMajorAxis
 	 *            is the semimajor axis of the ellipse
 	 * @param semiMinorAxis
 	 *            is the semiminor axis of the ellipse
 	 * @throws RuntimeException
 	 *             if either of the axes are negative
 	 */
 	public static void filledEllipse(double x, double y, double semiMajorAxis,
 			double semiMinorAxis) {
 		
 		offscreen.fill(ShapeAssist.ellipse(x, y, semiMajorAxis, semiMinorAxis));
 		draw();
 	}
 
 	/**
 	 * Draw an arc of radius r, centred on (x, y), from angle1 to angle2 (in
 	 * degrees).
 	 * 
 	 * @param x
 	 *            the x-coordinate of the centre of the circle
 	 * @param y
 	 *            the y-coordinate of the centre of the circle
 	 * @param r
 	 *            the radius of the circle
 	 * @param angle1
 	 *            the starting angle. 0 would mean an arc beginning at 3
 	 *            o'clock.
 	 * @param angle2
 	 *            the angle at the end of the arc. For example, if you want a 90
 	 *            degree arc, then angle2 should be angle1 + 90.
 	 * @throws RuntimeException
 	 *             if the radius of the circle is negative
 	 */
 	public static void arc(double x, double y, double r, double angle1,
 			double angle2) {
 		
 		offscreen.draw(ShapeAssist.arc(x, y, r, angle1, angle2));
 		draw();
 	}
 
 	/**
 	 * Draw a square of side length 2r, centred on (x, y).
 	 * 
 	 * @param x
 	 *            the x-coordinate of the centre of the square
 	 * @param y
 	 *            the y-coordinate of the centre of the square
 	 * @param r
 	 *            radius is half the length of any side of the square
 	 * @throws RuntimeException
 	 *             if r is negative
 	 */
 	public static void square(double x, double y, double r) {
 		offscreen.draw(ShapeAssist.square(x, y, r));
 		draw();
 	}
 
 	/**
 	 * Draw a filled square of side length 2r, centred on (x, y).
 	 * 
 	 * @param x
 	 *            the x-coordinate of the centre of the square
 	 * @param y
 	 *            the y-coordinate of the centre of the square
 	 * @param r
 	 *            radius is half the length of any side of the square
 	 * @throws RuntimeException
 	 *             if r is negative
 	 */
 	public static void filledSquare(double x, double y, double r) {
 		offscreen.fill(ShapeAssist.square(x, y, r));
 		draw();
 	}
 
 	/**
 	 * Draw a filled square of side length 2r, centred at <location>.
 	 * 
 	 * @param location
 	 *            the Point at the centre of the square
 	 * @param r
 	 *            radius is half the length of any side of the square
 	 * @throws RuntimeException
 	 *             if r is negative
 	 */
 	public static void filledSquare(Point2D.Double location, double r) {
 		filledSquare(location.getX(), location.getY(), r);
 	}
 	
 	/**
 	 * Draw a diamond of side length 2r, centred on (x, y).
 	 * 
 	* @param x
 	 *            the x-coordinate of the centre of the diamond
 	 * @param y
 	 *            the y-coordinate of the centre of the diamond
 	 * @param r
 	 *            radius is half the length of any side of the diamond
 	 * @throws RuntimeException
 	 *             if r is negative
 	 */
 	public static void diamond(double x, double y, double r) {
 		offscreen.draw(ShapeAssist.diamond(x,y,r));
 		draw();
 	}
 	
 	/**
 	 * Draw a filled diamond of side length 2r, centred on (x, y).
 	 * 
 	* @param x
 	 *            the x-coordinate of the centre of the diamond
 	 * @param y
 	 *            the y-coordinate of the centre of the diamond
 	 * @param r
 	 *            radius is half the length of any side of the diamond
 	 * @throws RuntimeException
 	 *             if r is negative
 	 */
 	public static void filledDiamond(double x, double y, double r) {
 		offscreen.fill(ShapeAssist.diamond(x,y,r));
 		draw();
 	}
 
 	/**
 	 * Draw a filled diamond of side length 2r, centred at <location>.
 	 * 
 	 * @param location
 	 *            the Point at the centre of the square
 	 * @param r
 	 *            radius is half the length of any side of the diamond
 	 * @throws RuntimeException
 	 *             if r is negative
 	 */
 	public static void filledDiamond(Point2D.Double location, double r) {
 		filledDiamond(location.getX(), location.getY(), r);
 	}
 	
 	/**
 	 * Draw an equilateral triangle of radius r, centred at <location>.
 	 * 
 	 * @param location
 	 *            the Point at the centre of the triangle
 	 * @param r
 	 *            radius is the length from the centre of the triangle to
 	 *            any of its corners.
 	 * @throws RuntimeException
 	 *             if r is negative
 	 */
 	public static void triangle(double x, double y, double r) {
 		offscreen.draw(ShapeAssist.triangle(x,y,r));
 		draw();
 	}
 	
 	/**
 	 * Draw a filled equilateral triangle of radius r, centred at <location>.
 	 * 
 	 * @param location
 	 *            the Point at the centre of the triangle
 	 * @param r
 	 *            radius is the length from the centre of the triangle to
 	 *            any of its corners.
 	 * @throws RuntimeException
 	 *             if r is negative
 	 */
 	public static void filledTriangle(double x, double y, double r) {
 		offscreen.fill(ShapeAssist.triangle(x,y,r));
 		draw();
 	}
 
 	/**
 	 * Draw a rectangle of given half width and half height, centered on (x, y).
 	 * 
 	 * @param x
 	 *            the x-coordinate of the center of the rectangle
 	 * @param y
 	 *            the y-coordinate of the center of the rectangle
 	 * @param halfWidth
 	 *            is half the width of the rectangle
 	 * @param halfHeight
 	 *            is half the height of the rectangle
 	 * @throws RuntimeException
 	 *             if halfWidth or halfHeight is negative
 	 */
 	public static void rectangle(double x, double y, double halfWidth,
 			double halfHeight) {
 		
 		offscreen.draw(ShapeAssist.rectangle(x, y, halfWidth, halfHeight));
 		draw();
 	}
 
 	/**
 	 * Draw a filled rectangle of given half width and half height, centred on
 	 * (x, y).
 	 * 
 	 * @param x
 	 *            the x-coordinate of the centre of the rectangle
 	 * @param y
 	 *            the y-coordinate of the centre of the rectangle
 	 * @param halfWidth
 	 *            is half the width of the rectangle
 	 * @param halfHeight
 	 *            is half the height of the rectangle
 	 * @throws RuntimeException
 	 *             if halfWidth or halfHeight is negative
 	 */
 	public static void filledRectangle(double x, double y, double halfWidth,
 			double halfHeight) {
 		
 		offscreen.fill(ShapeAssist.rectangle(x, y, halfWidth, halfHeight));
 		draw();
 	}
 
 	/**
 	 * Draw a filled rectangle of given half width and half height, centred at
 	 * <location>.
 	 * 
 	 * @param location
 	 *            the Point at the centre of the rectangle
 	 * @param halfWidth
 	 *            is half the width of the rectangle
 	 * @param halfHeight
 	 *            is half the height of the rectangle
 	 * @throws RuntimeException
 	 *             if halfWidth or halfHeight is negative
 	 */
 	public static void filledRectangle(Point2D.Double location,
 			double halfWidth, double halfHeight) {
 		
 		filledRectangle(location.getX(), location.getY(), halfWidth, halfHeight);
 	}
 	
 	/**
 	 * Draw a rectangle of given half width and half height, top left corner
 	 * on (x, y) and rotated by d degrees.
 	 * 
 	 * @param x
 	 *            the x-coordinate of the top left of the rectangle
 	 * @param y
 	 *            the y-coordinate of the top left of the rectangle
 	 * @param halfWidth
 	 *            is half the width of the rectangle
 	 * @param halfHeight
 	 *            is half the height of the rectangle
 	 * @param degrees
 	 *            is the rotation of the rectangle with 3 o'clock being 0
 	 *            degrees and 12 o'clock being 270 degrees
 	 * @throws RuntimeException
 	 *             if halfWidth or halfHeight is negative
 	 */
 	public static void angledRectangle(double x, double y, double halfWidth,
 			double halfHeight, double degrees) {
 		
 		offscreen.draw(ShapeAssist.angledRectangle(x, y, halfWidth, halfHeight, degrees));
 		draw();
 	}
 
 	/**
 	 * Draw a filled rectangle of given half width and half height, centred on
 	 * (x, y) and rotated by d degrees.
 	 * 
 	 * @param x
 	 *            the x-coordinate of the centre of the rectangle
 	 * @param y
 	 *            the y-coordinate of the centre of the rectangle
 	 * @param halfWidth
 	 *            is half the width of the rectangle
 	 * @param halfHeight
 	 *            is half the height of the rectangle
 	 * @param degrees
 	 *            is the rotation of the rectangle with 3 o'clock being 0
 	 *            degrees and 12 o'clock being 270 degrees
 	 * @throws RuntimeException
 	 *             if halfWidth or halfHeight is negative
 	 */
 	public static void filledAngledRectangle(double x, double y, double halfWidth,
 			double halfHeight, double degrees) {
 		
 		offscreen.fill(ShapeAssist.angledRectangle(x, y, halfWidth, halfHeight, degrees));
 		draw();
 	}
 	
 	/**
 	 * Draw a filled rectangle of given half width and half height, centred at
 	 * <location> and rotated by d degrees.
 	 * 
 	 * @param location
 	 *            the Point at the centre of the rectangle
 	 * @param halfWidth
 	 *            is half the width of the rectangle
 	 * @param halfHeight
 	 *            is half the height of the rectangle
 	 * @param degrees
 	 *            is the rotation of the rectangle with 3 o'clock being 0
 	 *            degrees and 12 o'clock being 270 degrees
 	 * @throws RuntimeException
 	 *             if halfWidth or halfHeight is negative
 	 */
 	public static void filledAngledRectangle(Point2D.Double location,
 			double halfWidth, double halfHeight, double degrees) {
 		
 		filledAngledRectangle(location.getX(), location.getY(), halfWidth, halfHeight, degrees);
 	}
 
 	/**
 	 * Draw a polygon with the given (x[i], y[i]) coordinates.
 	 * 
 	 * @param x
 	 *            an array of all the x-coordindates of the polygon
 	 * @param y
 	 *            an array of all the y-coordindates of the polygon
 	 */
 	public static void polygon(double[] x, double[] y) {
 		offscreen.draw(ShapeAssist.polygon(x, y));
 		draw();
 	}
 
 	/**
 	 * Draw a filled polygon with the given (x[i], y[i]) coordinates.
 	 * 
 	 * @param x
 	 *            an array of all the x-coordindates of the polygon
 	 * @param y
 	 *            an array of all the y-coordindates of the polygon
 	 */
 	public static void filledPolygon(double[] x, double[] y) {
 		offscreen.fill(ShapeAssist.polygon(x, y));
 		draw();
 	}
 
 	/*************************************************************************
 	 * Drawing images.
 	 *************************************************************************/
 
 	// get an image from the given filename
 	private static Image getImage(String filename) {
 
 		// to read from file
 		ImageIcon icon = new ImageIcon(filename);
 
 		// try to read from URL
 		if ((icon == null)
 				|| (icon.getImageLoadStatus() != MediaTracker.COMPLETE)) {
 			try {
 				URL url = new URL(filename);
 				icon = new ImageIcon(url);
 			} catch (Exception e) { /* not a url */
 			}
 		}
 
 		// in case file is inside a .jar
 		if ((icon == null)
 				|| (icon.getImageLoadStatus() != MediaTracker.COMPLETE)) {
 			URL url = StdDraw.class.getResource(filename);
 			if (url == null)
 				throw new RuntimeException("image " + filename + " not found");
 			icon = new ImageIcon(url);
 		}
 
 		return icon.getImage();
 	}
 
 	/**
 	 * Draw picture (gif, jpg, or png) centered on (x, y).
 	 * 
 	 * @param x
 	 *            the center x-coordinate of the image
 	 * @param y
 	 *            the center y-coordinate of the image
 	 * @param s
 	 *            the name of the image/picture, e.g., "ball.gif"
 	 * @throws RuntimeException
 	 *             if the image is corrupt
 	 */
 	public static void picture(double x, double y, String s) {
 		Image image = getImage(s);
 		double xs = scaleX(x);
 		double ys = scaleY(y);
 		int ws = image.getWidth(null);
 		int hs = image.getHeight(null);
 		if (ws < 0 || hs < 0)
 			throw new RuntimeException("image " + s + " is corrupt");
 
 		offscreen.drawImage(image, (int) Math.round(xs - ws / 2.0),
 				(int) Math.round(ys - hs / 2.0), null);
 		draw();
 	}
 
 	/**
 	 * Draw picture (gif, jpg, or png) centered on (x, y), rotated given number
 	 * of degrees
 	 * 
 	 * @param x
 	 *            the center x-coordinate of the image
 	 * @param y
 	 *            the center y-coordinate of the image
 	 * @param s
 	 *            the name of the image/picture, e.g., "ball.gif"
 	 * @param degrees
 	 *            is the number of degrees to rotate counterclockwise
 	 * @throws RuntimeException
 	 *             if the image is corrupt
 	 */
 	public static void picture(double x, double y, String s, double degrees) {
 		Image image = getImage(s);
 		double xs = scaleX(x);
 		double ys = scaleY(y);
 		int ws = image.getWidth(null);
 		int hs = image.getHeight(null);
 		if (ws < 0 || hs < 0)
 			throw new RuntimeException("image " + s + " is corrupt");
 
 		offscreen.rotate(Math.toRadians(-degrees), xs, ys);
 		offscreen.drawImage(image, (int) Math.round(xs - ws / 2.0),
 				(int) Math.round(ys - hs / 2.0), null);
 		offscreen.rotate(Math.toRadians(+degrees), xs, ys);
 
 		draw();
 	}
 
 	/**
 	 * Draw picture (gif, jpg, or png) centered on (x, y), rescaled to w-by-h.
 	 * 
 	 * @param x
 	 *            the center x coordinate of the image
 	 * @param y
 	 *            the center y coordinate of the image
 	 * @param s
 	 *            the name of the image/picture, e.g., "ball.gif"
 	 * @param w
 	 *            the width of the image
 	 * @param h
 	 *            the height of the image
 	 * @throws RuntimeException
 	 *             if the width height are negative
 	 * @throws RuntimeException
 	 *             if the image is corrupt
 	 */
 	public static void picture(double x, double y, String s, double w, double h) {
 		Image image = getImage(s);
 		double xs = scaleX(x);
 		double ys = scaleY(y);
 		if (w < 0)
 			throw new RuntimeException("width is negative: " + w);
 		if (h < 0)
 			throw new RuntimeException("height is negative: " + h);
 		double ws = factorX(w);
 		double hs = factorY(h);
 		if (ws < 0 || hs < 0)
 			throw new RuntimeException("image " + s + " is corrupt");
 		if (ws <= 1 && hs <= 1)
 			pixel(x, y);
 		else {
 			offscreen.drawImage(image, (int) Math.round(xs - ws / 2.0),
 					(int) Math.round(ys - hs / 2.0), (int) Math.round(ws),
 					(int) Math.round(hs), null);
 		}
 		draw();
 	}
 
 	/**
 	 * Draw picture (gif, jpg, or png) centered on (x, y), rotated given number
 	 * of degrees, rescaled to w-by-h.
 	 * 
 	 * @param x
 	 *            the center x-coordinate of the image
 	 * @param y
 	 *            the center y-coordinate of the image
 	 * @param s
 	 *            the name of the image/picture, e.g., "ball.gif"
 	 * @param w
 	 *            the width of the image
 	 * @param h
 	 *            the height of the image
 	 * @param degrees
 	 *            is the number of degrees to rotate counterclockwise
 	 * @throws RuntimeException
 	 *             if the image is corrupt
 	 */
 	public static void picture(double x, double y, String s, double w,
 			double h, double degrees) {
 		Image image = getImage(s);
 		double xs = scaleX(x);
 		double ys = scaleY(y);
 		double ws = factorX(w);
 		double hs = factorY(h);
 		if (ws < 0 || hs < 0)
 			throw new RuntimeException("image " + s + " is corrupt");
 		if (ws <= 1 && hs <= 1)
 			pixel(x, y);
 
 		offscreen.rotate(Math.toRadians(-degrees), xs, ys);
 		offscreen.drawImage(image, (int) Math.round(xs - ws / 2.0),
 				(int) Math.round(ys - hs / 2.0), (int) Math.round(ws),
 				(int) Math.round(hs), null);
 		offscreen.rotate(Math.toRadians(+degrees), xs, ys);
 
 		draw();
 	}
 
 	/*************************************************************************
 	 * Drawing text.
 	 *************************************************************************/
 
 	/**
 	 * Write the given text string in the current font, centered on (x, y).
 	 * 
 	 * @param x
 	 *            the center x-coordinate of the text
 	 * @param y
 	 *            the center y-coordinate of the text
 	 * @param s
 	 *            the text
 	 */
 	public static void text(double x, double y, String s) {
 		offscreen.setFont(font);
 		FontMetrics metrics = offscreen.getFontMetrics();
 		double xs = scaleX(x);
 		double ys = scaleY(y);
 		int ws = metrics.stringWidth(s);
 		int hs = metrics.getDescent();
 		offscreen.drawString(s, (float) (xs - ws / 2.0), (float) (ys + hs));
 		draw();
 	}
 
 	/**
 	 * Write the given text string in the current font, centered at <location>.
 	 * 
 	 * @param location
 	 *            the Point at the center of the text
 	 * @param s
 	 *            the text
 	 */
 	public static void text(Point2D.Double location, String s) {
 		text(location.getX(), location.getY(), s);
 	}
 
 	/**
 	 * Write the given text string in the current font, centered on (x, y) and
 	 * rotated by the specified number of degrees
 	 * 
 	 * @param x
 	 *            the center x-coordinate of the text
 	 * @param y
 	 *            the center y-coordinate of the text
 	 * @param s
 	 *            the text
 	 * @param degrees
 	 *            is the number of degrees to rotate counterclockwise
 	 */
 	public static void text(double x, double y, String s, double degrees) {
 		double xs = scaleX(x);
 		double ys = scaleY(y);
 		offscreen.rotate(Math.toRadians(-degrees), xs, ys);
 		text(x, y, s);
 		offscreen.rotate(Math.toRadians(+degrees), xs, ys);
 	}
 
 	/**
 	 * Write the given text string in the current font, left-aligned at (x, y).
 	 * 
 	 * @param x
 	 *            the x-coordinate of the text
 	 * @param y
 	 *            the y-coordinate of the text
 	 * @param s
 	 *            the text
 	 */
 	public static void textLeft(double x, double y, String s) {
 		offscreen.setFont(font);
 		FontMetrics metrics = offscreen.getFontMetrics();
 		double xs = scaleX(x);
 		double ys = scaleY(y);
 		int hs = metrics.getDescent();
 		offscreen.drawString(s, (float) (xs), (float) (ys + hs));
 		draw();
 	}
 
 	/**
 	 * Write the given text string in the current font, right-aligned at (x, y).
 	 * 
 	 * @param x
 	 *            the x-coordinate of the text
 	 * @param y
 	 *            the y-coordinate of the text
 	 * @param s
 	 *            the text
 	 */
 	public static void textRight(double x, double y, String s) {
 		offscreen.setFont(font);
 		FontMetrics metrics = offscreen.getFontMetrics();
 		double xs = scaleX(x);
 		double ys = scaleY(y);
 		int ws = metrics.stringWidth(s);
 		int hs = metrics.getDescent();
 		offscreen.drawString(s, (float) (xs - ws), (float) (ys + hs));
 		draw();
 	}
 
 	/**
 	 * Display on screen, pause for t milliseconds, and turn on
 	 * <em>animation mode</em>: subsequent calls to drawing methods such as
 	 * <tt>line()</tt>, <tt>circle()</tt>, and <tt>square()</tt> will not be
 	 * displayed on screen until the next call to <tt>show()</tt>. This is
 	 * useful for producing animations (clear the screen, draw a bunch of
 	 * shapes, display on screen for a fixed amount of time, and repeat). It
 	 * also speeds up drawing a huge number of shapes (call <tt>show(0)</tt> to
 	 * defer drawing on screen, draw the shapes, and call <tt>show(0)</tt> to
 	 * display them all on screen at once).
 	 * 
 	 * @param t
 	 *            number of milliseconds
 	 */
 	public static void show(int t) {
 		defer = false;
 		draw();
 		try {
 			// Thread.currentThread().sleep(t);
 			Thread.sleep(t);
 		} catch (InterruptedException e) {
 			System.out.println("Error sleeping");
 		}
 		defer = true;
 	}
 
 	/**
 	 * Display on-screen and turn off animation mode: subsequent calls to
 	 * drawing methods such as <tt>line()</tt>, <tt>circle()</tt>, and
 	 * <tt>square()</tt> will be displayed on screen when called. This is the
 	 * default.
 	 */
 	public static void show() {
 		defer = false;
 		draw();
 	}
 
 	// draw onscreen if defer is false
 	private static void draw() {
 		if (defer)
 			return;
 		onscreen.drawImage(offscreenImage, 0, 0, null);
 		frame.repaint();
 	}
 
 	/*************************************************************************
 	 * Save drawing to a file.
 	 *************************************************************************/
 
 	/**
 	 * Save onscreen image to file - suffix must be png, jpg, or gif.
 	 * 
 	 * @param filename
 	 *            the name of the file with one of the required suffixes
 	 */
 	public static void save(String filename) {
 		File file = new File(filename);
 		String suffix = filename.substring(filename.lastIndexOf('.') + 1);
 
 		// png files
 		if (suffix.toLowerCase().equals("png")) {
 			try {
 				ImageIO.write(onscreenImage, suffix, file);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 		// need to change from ARGB to RGB for jpeg
 		// reference:
 		// http://archives.java.sun.com/cgi-bin/wa?A2=ind0404&L=java2d-interest&D=0&P=2727
 		else if (suffix.toLowerCase().equals("jpg")) {
 			WritableRaster raster = onscreenImage.getRaster();
 			WritableRaster newRaster;
 			newRaster = raster.createWritableChild(0, 0, width, height, 0, 0,
 					new int[] { 0, 1, 2 });
 			DirectColorModel cm = (DirectColorModel) onscreenImage
 					.getColorModel();
 			DirectColorModel newCM = new DirectColorModel(cm.getPixelSize(),
 					cm.getRedMask(), cm.getGreenMask(), cm.getBlueMask());
 			BufferedImage rgbBuffer = new BufferedImage(newCM, newRaster,
 					false, null);
 			try {
 				ImageIO.write(rgbBuffer, suffix, file);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 		else {
 			System.out.println("Invalid image file type: " + suffix);
 		}
 	}
 
 	/**
 	 * This method cannot be called directly.
 	 */
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		FileDialog chooser = new FileDialog(StdDraw.frame,
 				"Use a .png or .jpg extension", FileDialog.SAVE);
 		chooser.setVisible(true);
 		String filename = chooser.getFile();
 		if (filename != null) {
 			StdDraw.save(chooser.getDirectory() + File.separator
 					+ chooser.getFile());
 		}
 	}
 
 	/*************************************************************************
 	 * Mouse interactions.
 	 *************************************************************************/
 
 	/**
 	 * Is the mouse being pressed?
 	 * 
 	 * @return true or false
 	 */
 	public static boolean mousePressed() {
 		synchronized (mouseLock) {
 			return mousePressed;
 		}
 	}
 
 	/**
 	 * What is the x-coordinate of the mouse?
 	 * 
 	 * @return the value of the x-coordinate of the mouse
 	 */
 	public static double mouseX() {
 		synchronized (mouseLock) {
 			return mouseX;
 		}
 	}
 
 	/**
 	 * What is the y-coordinate of the mouse?
 	 * 
 	 * @return the value of the y-coordinate of the mouse
 	 */
 	public static double mouseY() {
 		synchronized (mouseLock) {
 			return mouseY;
 		}
 	}
 
 	/**
 	 * This method cannot be called directly.
 	 */
 	@Override
 	public void mouseClicked(MouseEvent e) {
 	}
 
 	/**
 	 * This method cannot be called directly.
 	 */
 	@Override
 	public void mouseEntered(MouseEvent e) {
 	}
 
 	/**
 	 * This method cannot be called directly.
 	 */
 	@Override
 	public void mouseExited(MouseEvent e) {
 	}
 
 	/**
 	 * This method cannot be called directly.
 	 */
 	@Override
 	public void mousePressed(MouseEvent e) {
 		synchronized (mouseLock) {
 			mouseX = StdDraw.userX(e.getX());
 			mouseY = StdDraw.userY(e.getY());
 			mousePressed = true;
 		}
 	}
 
 	/**
 	 * This method cannot be called directly.
 	 */
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		synchronized (mouseLock) {
 			mousePressed = false;
 		}
 	}
 
 	/**
 	 * This method cannot be called directly.
 	 */
 	@Override
 	public void mouseDragged(MouseEvent e) {
 		synchronized (mouseLock) {
 			mouseX = StdDraw.userX(e.getX());
 			mouseY = StdDraw.userY(e.getY());
 		}
 	}
 
 	/**
 	 * This method cannot be called directly.
 	 */
 	@Override
 	public void mouseMoved(MouseEvent e) {
 		synchronized (mouseLock) {
 			mouseX = StdDraw.userX(e.getX());
 			mouseY = StdDraw.userY(e.getY());
 		}
 	}
 
 	/*************************************************************************
 	 * Keyboard interactions.
 	 *************************************************************************/
 
 	/**
 	 * Has the user typed a key?
 	 * 
 	 * @return true if the user has typed a key, false otherwise
 	 */
 	public static boolean hasNextKeyTyped() {
 		synchronized (keyLock) {
 			return !keysTyped.isEmpty();
 		}
 	}
 
 	/**
 	 * What is the next key that was typed by the user? This method returns a
 	 * Unicode character corresponding to the key typed (such as 'a' or 'A'). It
 	 * cannot identify action keys (such as F1 and arrow keys) or modifier keys
 	 * (such as control).
 	 * 
 	 * @return the next Unicode key typed
 	 */
 	public static char nextKeyTyped() {
 		synchronized (keyLock) {
 			return keysTyped.removeLast();
 		}
 	}
 
 	/**
 	 * Is the keycode currently being pressed? This method takes as an argument
 	 * the keycode (corresponding to a physical key). It can handle action keys
 	 * (such as F1 and arrow keys) and modifier keys (such as shift and
 	 * control). See <a href =
 	 * "http://download.oracle.com/javase/6/docs/api/java/awt/event/KeyEvent.html"
 	 * >KeyEvent.java</a> for a description of key codes.
 	 * 
 	 * @return true if keycode is currently being pressed, false otherwise
 	 */
 	public static boolean isKeyPressed(int keycode) {
 		return keysDown.contains(keycode);
 	}
 
 	/**
 	 * This method cannot be called directly.
 	 */
 	@Override
 	public void keyTyped(KeyEvent e) {
 		synchronized (keyLock) {
 			keysTyped.addFirst(e.getKeyChar());
 		}
 	}
 
 	/**
 	 * This method cannot be called directly.
 	 */
 	@Override
 	public void keyPressed(KeyEvent e) {
 		keysDown.add(e.getKeyCode());
 	}
 
 	/**
 	 * This method cannot be called directly.
 	 */
 	@Override
 	public void keyReleased(KeyEvent e) {
 		keysDown.remove(e.getKeyCode());
 	}
 
 	/**
 	 * Test client.
 	 */
 	public static void main(String[] args) {
 		//final int WINDOW_LENGTH = 600;
 		//final int WINDOW_HEIGHT = 1200;
 		//StdDraw.setCanvasSize(WINDOW_HEIGHT, WINDOW_LENGTH); //pixel size of window
 		StdDraw.setXscale(0, 1.2);
 		StdDraw.setYscale(0, 1.2);
 		
 		
 		StdDraw.square(.2, .8, .1);
 		StdDraw.filledSquare(.8, .8, .2);
 		StdDraw.circle(.8, .2, .2);
 		
 		//draw a blue diamond, radius 0.1 (old method)
 		StdDraw.setPenRadius(); //default radius
 		StdDraw.setPenColor(Colors.BOOK_BLUE);
 		double[] x = { .1, .2, .3, .2 };
 		double[] y = { .2, .3, .2, .1 };
 		StdDraw.filledPolygon(x, y);
 		
 		//draw a cyandiamond, radius 0.1 (new method)
 		StdDraw.setPenColor(Colors.CYAN);
 		StdDraw.filledDiamond(.45, .2, 0.1);
 		
 		//draw a magenta triangle, radius 0.1
 		StdDraw.setPenColor(Colors.MAGENTA);
 		StdDraw.filledTriangle(.45, 0.4, 0.1);
 		
 		//Green filled rectangle, rotated x degrees
 		StdDraw.setPenColor(Colors.GREEN);
 		StdDraw.filledAngledRectangle(.45, .8, .05, .2, 10);
 		//StdDraw.filledAngledRectangle(0.2, 0.2, 0.2, 0.05, 90);
 		//StdDraw.filledAngledRectangle(0, 0, 0.2, 0.05, 0);
 
 		StdDraw.setPenColor(Colors.BOOK_RED);
 		StdDraw.setPenRadius(.02);
 		StdDraw.arc(.8, .2, .1, 120, 60);
 		StdDraw.line(0.8, 0.2, 0.8, 0.3);
 
 		// text
 		StdDraw.setPenRadius();
 		StdDraw.setPenColor(Colors.BLACK);
 		StdDraw.text(0.2, 0.5, "black text");
 		StdDraw.setPenColor(Colors.WHITE);
 		StdDraw.text(0.8, 0.8, "white text");
 		
 		//Edge testing
 		StdDraw.setPenColor(Colors.GRAY);
 		//StdDraw.circle(0, 0, .2);
 		StdDraw.line(0, 0, 0, 1);
 	}
 	
 	/**
 	 * Added by Shafqat to get the Frame being drawn
 	 * @return
 	 */
 	public static JFrame getFrame() {
 		return frame;
 	}
 
 }
