 package stackiter.sim;
 
 import static stackiter.sim.Util.*;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.geom.*;
 import java.awt.image.*;
 import java.io.*;
 import java.util.*;
 
 import javax.swing.*;
 import javax.swing.Timer;
 
 @SuppressWarnings("serial")
 public class Stackiter extends JComponent implements ActionListener, Closeable, MouseListener, MouseMotionListener {
 
 	public static void main(String[] args) {
 		JFrame frame = new JFrame("Stackiter");
 		frame.setLayout(new BorderLayout());
 		final Stackiter stackiter = new Stackiter();
 		frame.add(stackiter, BorderLayout.CENTER);
 		frame.pack();
 		frame.addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent e) {
 				stackiter.close();
 			}
 		});
 		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
 		//frame.setResizable(false);
 		//frame.addNotify();
 		//frame.setExtendedState(Frame.MAXIMIZED_BOTH);
 		frame.setVisible(true);
 		stackiter.start();
 	}
 
 	private BufferedImage backdrop;
 
 	private double backdropScale;
 
 	private double edgeThickness = 2.5;
 
 	private Logger logger;
 
 	private boolean mouseDown;
 
 	private boolean mouseOver;
 
 	private Tool mouseTool;
 
 	private Point2D mousePoint;
 
 	private Robot robot;
 
 	private Timer timer;
 
 	private Tray tray;
 
 	/**
 	 * The outer bounds of what we are allowed to see.
 	 */
 	private Rectangle2D viewBounds;
 
 	/**
 	 * The current view we'd like to see, if we had the right aspect ratio. The
 	 * size of this rectangle shouldn't ever change.
 	 */
 	private Rectangle2D viewRect;
 
 	private World world;
 
 	public Stackiter() {
 		try {
 
 			robot = new Robot();
 
 			logger = new FilterLogger(new TextLogger());
 			world = new World();
 			world.setLogger(logger);
 			tray = world.getTray();
 			mouseTool = world.addTool();
 			// Log the tool to get it a lower ID.
 			logger.logTool(mouseTool);
 
 			//world.addAgent(new ClearerAgent(30));
 			//world.addAgent(new DropperAgent());
 
 			double groundDepth = -2 * world.getGround().getExtent().getY();
 			double groundWidth = 40; // 2 * world.getGround().getExtent().getX(); <-- Visual glitch here needs resolved.
 			viewBounds = new Rectangle2D.Double(-20, groundDepth, groundWidth, 101);
 			viewRect = new Rectangle2D.Double(-20, groundDepth, groundWidth, 30);
 			addComponentListener(new ComponentAdapter() {
 				@Override
 				public void componentResized(ComponentEvent event) {
 					Stackiter.this.componentResized(event);
 				}
 			});
 			addMouseListener(this);
 			addMouseMotionListener(this);
 			setPreferredSize(new Dimension(640, 480));
 			setSize(getPreferredSize());
 
 			timer = new Timer(10, this);
 
 			// Clear the cursor for the display here.
 			// We use a custom "cursor" that doesn't always follow the actual mouse cursor.
 			BufferedImage blankImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
 			Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(blankImage, new Point(0, 0), "blank");
 			setCursor(blankCursor);
 
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent event) {
 
 		logger.atomic(new Runnable() { @Override public void run() {
 
 			Point2D toolPoint = null;
 			Point2D oldToolPoint = copy(mouseTool.getPosition());
 			boolean hadGrasped = false;
 
 			if (mousePoint != null) {
 				// Recalculate each time for cases of scrolling.
 				// TODO Base instead on moving toolPoint explicitly when scrolling??? Could be risky.
 				toolPoint = appliedInv(worldToDisplayTransform(), mousePoint);
 
 				hadGrasped = world.getGraspedItem(mouseTool) != null;
 				if (hadGrasped) {
 					// Constrain the target location.
 					// This mattered once blocks could constrain tool location (carrying them off the screen).
 					Point2D goalToolPoint = copy(toolPoint);
 					toolPoint.setLocation(
 						Math.min(toolPoint.getX(), viewBounds.getMaxX()),
 						Math.min(toolPoint.getY(), viewBounds.getMaxY())
 					);
 					toolPoint.setLocation(
 						Math.max(toolPoint.getX(), viewBounds.getMinX()),
 						Math.max(toolPoint.getY(), viewBounds.getMinY())
 					);
 					// See if we changed anything.
 					if (!toolPoint.equals(goalToolPoint)) {
 						// If so, move the mouse to the contrained spot.
 						moveMouse(toolPoint);
 					}
 				}
 
 				// Communicate agent action.
 				// TODO Include flush and clear as other mutually exclusive action choices.
 				// TODO The user can't grasp and flush at the same time, so I'd like not to provide that to computer agents, either.
 				// TODO Of course, we're still not imposing view constraints on the agent yet.
 				mouseTool.setMode(mouseDown ? ToolMode.GRASP : ToolMode.INACTIVE);
 				mouseTool.setPosition(toolPoint);
 
 				if (mouseOver) {
 					// Make sure we get the entrance before the move, if both.
 					logger.logToolPresent(mouseTool, mouseOver);
 				}
 			}
 
 			// TODO Offload this to a separate thread? If so, still lock step to one update per frame?
 			// TODO Alternatively, change the delay based on how much time is left. Or is that auto?
 			world.update();
 
 			if (mouseOver) {
 				if (mousePoint != null) {
					// Without mouseOver check, I got upward scrolling when over title bar.
					// Further, scroll after world update, so we don't lose our tool point, which might have changed.
 					Item graspedItem = world.getGraspedItem(mouseTool);
 					if (graspedItem != null) {
 						// Constrain the mouse to the tool position if holding an object.
 						// Otherwise, the seeming position on the screen can be far off from the target.
 						Point2D delta = subtracted(toolPoint, mouseTool.getPosition());
 						double relativeDir = dot(delta, graspedItem.getLinearVelocity());
 						if (relativeDir < 0) {
 							// The block is not headed toward the tool, so move the mouse to limit spring effect.
 							// Technically, the user should be allowed to target any position.
 							// However, we either need to show that position, or let the visible tool reflect the actual location better.
 							// This is one option for the latter.
 							// Showing tool and target would be nice but complicated.
 							// The human already has vaguer info than the computer anyway. The knowledge of action is somewhat internal to the human.
 							Point2D snapPoint = added(mouseTool.getPosition(), scaled(0.95, delta));
 							moveMouse(snapPoint);
 						}
 					} else if (hadGrasped) {
 						mouseTool.setPosition(oldToolPoint);
 						moveMouse(oldToolPoint);
 					}
					// Handle scroll after finalizing state.
					handleScroll(mouseTool.getPosition());
 				}
 			} else {
 				// Make we get the move (in world update) before the departure, if both.
 				logger.logToolPresent(mouseTool, mouseOver);
 			}
 
 			// Update view settings now that we've finalized out mouse position and world state.
 			updateView();
 			logger.logDisplaySize(point(getWidth(), getHeight()));
 			logger.logView(viewRelWorld());
 
 		}});
 
 		// And queue the repaint.
 		repaint();
 
 	}
 
 	public void close() {
 		logger.close();
 	}
 
 	public void componentResized(ComponentEvent event) {
 		updateView();
 	}
 
 	private void handleScroll(Point2D toolPoint) {
 
 		double rateX = 0;
 		double rateY = 0;
 		Rectangle2D viewRelWorld = viewRelWorld();
 
 		if (tray.isFixedToDisplay() && world.getGraspedItem(mouseTool) == null && toolPoint.getX() < tray.getAnchor().getX() + tray.getWidth()) {
 			// No block and over the tray means we are probably thinking about the tray, not scrolling.
 			// TODO Generalize this notion for widgets.
 			return;
 		}
 
 		// Find distance past scroll boundaries.
 		if (toolPoint.getX() < viewRelWorld.getMinX() + edgeThickness) {
 			// Left.
 			rateX = toolPoint.getX() - (viewRelWorld.getMinX() + edgeThickness);
 		} else if (toolPoint.getX() > viewRelWorld.getMaxX() - edgeThickness) {
 			// Right.
 			rateX = toolPoint.getX() - (viewRelWorld.getMaxX() - edgeThickness);
 		}
 		if (toolPoint.getY() < viewRelWorld.getMinY() + edgeThickness) {
 			// Down.
 			rateY = toolPoint.getY() - (viewRelWorld.getMinY() + edgeThickness);
 		} else if (toolPoint.getY() > viewRelWorld.getMaxY() - edgeThickness) {
 			// Up.
 			rateY = toolPoint.getY() - (viewRelWorld.getMaxY() - edgeThickness);
 		}
 
 		// Scale by edge thickness and apply quadratic decay.
 		rateX = rateX / edgeThickness;
 		rateY = rateY / edgeThickness;
 		rateX = 0.5 * Math.pow(Math.abs(rateX), 2) * Math.signum(rateX);
 		rateY = 0.5 * Math.pow(Math.abs(rateY), 2) * Math.signum(rateY);
 
 		// Impose view bounds.
 		// TODO Do narrow bounds drive this crazy?
 		// TODO Vectorized math would sure be nice here. This repetition is silly.
 		//System.out.println(viewBounds);
 		if (viewRelWorld.getMaxX() + rateX > viewBounds.getMaxX()) {
 			rateX = viewBounds.getMaxX() - viewRelWorld.getMaxX();
 		}
 		if (viewRelWorld.getMinX() + rateX < viewBounds.getMinX()) {
 			rateX = viewBounds.getMinX() - viewRelWorld.getMinX();
 		}
 		if (viewRelWorld.getMaxY() + rateY > viewBounds.getMaxY()) {
 			rateY = viewBounds.getMaxY() - viewRelWorld.getMaxY();
 		}
 		if (viewRelWorld.getMinY() + rateY < viewBounds.getMinY()) {
 			rateY = viewBounds.getMinY() - viewRelWorld.getMinY();
 		}
 
 		// Apply scroll rate, leaving the size unchanged.
 		viewRect.setRect(viewRect.getX() + rateX, viewRect.getY() + rateY, viewRect.getWidth(), viewRect.getHeight());
 
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent event) {
 		// Nothing to do here.
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent event) {
 		mouseMoved(event);
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent event) {
 		mouseOver = true;
 	}
 
 	@Override
 	public void mouseExited(MouseEvent event) {
 		mouseOver = false;
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent event) {
 		if (mousePoint == null) {
 			mousePoint = new Point2D.Double();
 		}
 		mousePoint.setLocation(event.getX(), event.getY());
 		mouseOver = true;
 	}
 
 	@Override
 	public void mousePressed(final MouseEvent event) {
 		// Track latest mouse position.
 		mouseMoved(event);
 		// Defer action to world update.
 		mouseDown = true;
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent event) {
 		// Defer action to world update.
 		// Possibly reduces responsiveness to user, but it keeps things more consistent from a standard MDP perspective.
 		// And if the world is fast enough, things will be okay.
 		mouseDown = false;
 	}
 
 	private void moveMouse(Point2D point) {
 		worldToDisplayTransform().transform(point, mousePoint);
 		Point displayPoint = getLocationOnScreen();
 		robot.mouseMove(displayPoint.x + (int)mousePoint.getX(), displayPoint.y + (int)mousePoint.getY());
 	}
 
 	@Override
 	protected void paintComponent(Graphics graphics) {
 		Graphics2D g = copy(graphics);
 		try {
 			// Backdrop.
 			AffineTransform transform = worldToDisplayTransform();
 			Point2D backdropPoint = applied(transform, point(viewBounds.getMinX(), viewBounds.getMinY()));
 			g.drawImage(backdrop, (int)backdropPoint.getX(), (int)backdropPoint.getY() - backdrop.getHeight(), null);
 			// World contents
 			g.transform(transform);
 			world.paint(g);
 		} finally {
 			g.dispose();
 		}
 	}
 
 	private void start() {
 		timer.start();
 	}
 
 	private void updateBackdrop() {
 		double scale = worldToDisplayScale();
 		int width = (int)Math.ceil(viewBounds.getWidth() * scale);
 		int height = (int)Math.ceil(viewBounds.getHeight() * scale);
 		if (backdrop == null || backdropScale != scale) {
 			backdrop = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
 			backdropScale = scale;
 			Graphics2D g = backdrop.createGraphics();
 			try {
 				float sectionHeight;
 				// Black background (or can I assume this by default?).
 				g.setColor(Color.BLACK);
 				g.fill(getBounds());
 				// Stars.
 				// I've considered deferring stars to paint time, but this is actually fairly slow here.
 				Random random = new Random(0);
 				int starCount = (int)(0.001 * backdrop.getWidth() * backdrop.getHeight());
 				for (int i = 0; i < starCount; i++) {
 					if (i % (starCount / 20) == 0) {
 						// The stars are already in random places.
 						// Only change the brightness at intervals, for fewer objects and faster speed.
 						g.setColor(Color.getHSBColor(1, 0, i / (float)starCount));
 					}
 					int x = random.nextInt(backdrop.getWidth());
 					int y = random.nextInt(backdrop.getHeight());
 					g.fillRect(x, y, 2, 2);
 				}
 				// Blue base to white.
 				sectionHeight = (float)(20 * scale);
 				float sectionY = backdrop.getHeight() - sectionHeight;
 				g.setPaint(new GradientPaint(
 					0, backdrop.getHeight(), Color.getHSBColor((float)(0.7 * 2/3f + 0.3 * 1/3f), 0.3f, 1f),
 					0, sectionY, Color.WHITE
 				));
 				sectionY -= (float)(5 * scale);
				g.fillRect(0, (int)sectionY, backdrop.getWidth(), (int)(backdrop.getHeight() - sectionY));
 				// White to transparent (i.e., black starry space).
 				sectionHeight = (float)(30 * scale);
 				float sectionY2 = sectionY - sectionHeight;
 				g.setPaint(new GradientPaint(
 					0, sectionY, Color.WHITE,
 					0, sectionY2, new Color(0, 0, 0, 0)
 				));
 				g.fillRect(0, (int)sectionY2, backdrop.getWidth(), (int)sectionHeight);
 			} finally {
 				g.dispose();
 			}
 		}
 	}
 
 	private void updateTrayBounds() {
 		if (tray.isFixedToDisplay()) {
 			AffineTransform transform = worldToDisplayTransform();
 			invert(transform);
 			tray.setAnchor(applied(transform, point(0, getHeight())));
 			tray.setHeight(viewRelWorld().getHeight());
 		}
 	}
 
 	private void updateView() {
 		updateBackdrop();
 		updateTrayBounds();
 	}
 
 	private Rectangle2D viewRelWorld() {
 		AffineTransform transform = inverted(worldToDisplayTransform());
 		Path2D displayPath = new Path2D.Double(getBounds());
 		displayPath.transform(transform);
 		return displayPath.getBounds2D();
 	}
 
 	private double worldToDisplayScale() {
 		Dimension size = getSize();
 		double xScale = size.getWidth() / viewRect.getWidth();
 		if (!tray.isFixedToDisplay()) {
 			// Make sure they can see the tray, even if the display goes high.
 			// This still breaks down at some point, though.
 			return xScale;
 		}
 		double yScale = size.getHeight() / viewRect.getHeight();
 		// Go with the smaller of the two. Cut off rather than show extra.
 		double scale = xScale * viewRect.getHeight() < size.getHeight() ? yScale : xScale;
 		return scale;
 	}
 
 	private AffineTransform worldToDisplayTransform() {
 		AffineTransform transform = new AffineTransform();
 		transform.translate(0.5 * getWidth(), getHeight());
 		double scale = worldToDisplayScale();
 		transform.scale(scale, -scale);
 		transform.translate(-viewRect.getCenterX(), -viewRect.getMinY());
 		return transform;
 	}
 
 }
