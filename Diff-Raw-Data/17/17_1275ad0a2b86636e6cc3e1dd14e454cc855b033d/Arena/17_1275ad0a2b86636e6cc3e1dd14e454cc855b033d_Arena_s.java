 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.image.BufferedImage;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.HashSet;
 
 import javax.swing.JComponent;
 import javax.swing.Timer;
 
 @SuppressWarnings("serial")
 public class Arena extends JComponent {
 	private BufferedImage background; // view
 	private Graphics2D bg_graphics;
 	private ArenaObject[][] arena; // model
 
 	private ScoreKeeper score;
 	private boolean paused;
 
 	private LightCycle user;
 	private Set<LightCycle> players;
 
 	private int interval = 35; // Milliseconds between updates.
 	private Timer timer; // Each time timer fires we animate one step.
 
 	public Arena(ScoreKeeper score) {
 		setFocusable(true);
 
 		this.score = score;
 		paused = true;
 
 		timer = new Timer(interval, new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				tick();
 			}
 		});
 		timer.start();
 
 		// Keyboard control
 		// Note: going backwards is not allowed
 		addKeyListener(new KeyAdapter() {
 			public void keyPressed(KeyEvent e) {
 				if (e.getKeyCode() == KeyEvent.VK_P)
 					togglePause();
 				if (e.getKeyCode() == KeyEvent.VK_LEFT
 						&& user.getDirection() != LightCycle.Direction.WEST)
 					user.setDirection(LightCycle.Direction.EAST);
 				else if (e.getKeyCode() == KeyEvent.VK_RIGHT
 						&& user.getDirection() != LightCycle.Direction.EAST)
 					user.setDirection(LightCycle.Direction.WEST);
 				else if (e.getKeyCode() == KeyEvent.VK_UP
 						&& user.getDirection() != LightCycle.Direction.SOUTH)
 					user.setDirection(LightCycle.Direction.NORTH);
 				else if (e.getKeyCode() == KeyEvent.VK_DOWN
 						&& user.getDirection() != LightCycle.Direction.NORTH)
 					user.setDirection(LightCycle.Direction.SOUTH);
 				else if (e.getKeyCode() == KeyEvent.VK_N)
 					reset();
 			}
 		});
 	}
 
 	/**
 	 * Given a coordinate pair that may refer to a point out of the image,
 	 * convert it to one in the image by "wrapping" it around.
 	 * 
 	 * @param x
 	 *            X Coordinate to be converted
 	 * @param y
 	 *            Y Coordinate to be converted
 	 * @return An int array of size 2 such that the first element is the
 	 *         converted x coordinate and the second element is the converted y
 	 *         coordinate.
 	 */
 	private Point wrapCoordinates(Point p) {
 		Point ans = new Point(p);
 		if (p.y < 0)
 			ans.y = getHeight() + p.y;
 		else if (p.y >= getHeight())
 			ans.y = p.y - getHeight();
 		if (p.x < 0)
 			ans.x = getWidth() + p.x;
 		else if (p.x >= getWidth())
 			ans.x = p.x - getWidth();
 		return ans;
 	}
 
 	/**
 	 * Given a coordinate pair that that may refer to a point out of the game
 	 * data, wrap the coordinates to fit it within the game data bounds and
 	 * return the <code>ArenaObject</code> at that point.
 	 * 
 	 * @param x
 	 *            X Coordinate of point
 	 * @param y
 	 *            Y Coordinate of point
 	 * @return The <code>ArenaObject</code> at the given point; null if no
 	 *         <code>ArenaObject</code> exists at the given point.
 	 */
 	public ArenaObject getAtPosition(Point p) {
 		Point wrapped = wrapCoordinates(p);
 		return arena[wrapped.x][wrapped.y];
 	}
 
 	/**
 	 * Given a coordinate pair that that may refer to a point out of the game
 	 * data, wrap the coordinates to fit it within the game data bounds and add
 	 * the given <code>ArenaObject</code> to that location.
 	 * 
 	 * @param x
 	 *            X Coordinate of point
 	 * @param y
 	 *            Y Coordinate of point
 	 * @param a
 	 *            The <code>ArenaObject</code> to add to the given location.
 	 */
 	public void setAtPosition(Point p, ArenaObject a) {
 		Point wrapped = wrapCoordinates(p);
 		arena[wrapped.x][wrapped.y] = a;
 	}
 
 	/**
 	 * Set the state of the state of the game to its initial value and prepare
 	 * the game for keyboard input.
 	 */
 	public void reset() {
 		background = null;
 		arena = new ArenaObject[getWidth()][getHeight()];
 		score.reset();
		user = new LightCycle(Color.BLUE);
		LightCycle ai1 = new AILightCycle(Color.RED);
 		players = new HashSet<LightCycle>();
 		players.add(user);
		players.add(ai1);
 		user.setPosition(new Point(getWidth() / 2, getHeight() / 2));
		ai1.setPosition(new Point(getWidth() / 4, getHeight() / 2));
 		BonusObject.placeAt(new Point(400, 50), user, score, 20, this);
 		repaint();
 		requestFocusInWindow();
 	}
 
 	/**
 	 * For a given <code>LightCycle</code>, returns the set of
 	 * <code>ArenaObject</code>s "under" it (null references excluded). Used for
 	 * detecting if the <code>LightCycle</code> is colliding with a path.
 	 * 
 	 * @param l
 	 *            The <code>LightCycle</code> to look under.
 	 * @return The set of <code>ArenaObject</code>s that are under
 	 *         <code>l</code>
 	 */
 	public Set<ArenaObject> spacesUnderLightCycle(LightCycle l) {
 		Set<ArenaObject> ans = new HashSet<ArenaObject>();
 		int startX, endX, startY, endY;
 		startY = Math.min(l.getRearCoords().y, l.getFrontCoords().y);
 		endY = Math.max(l.getRearCoords().y, l.getFrontCoords().y);
 		startX = Math.min(l.getRearCoords().x, l.getFrontCoords().x);
 		endX = Math.max(l.getRearCoords().x, l.getFrontCoords().x);
 
 		for (Point curr = new Point(startX, startY); curr.x <= endX; curr.x++)
 			for (curr.y = startY; curr.y <= endY; curr.y++)
 				if (getAtPosition(curr) != null)
 					ans.add(getAtPosition(curr));
 
 		return ans;
 	}
 
 	/** Update the game one timestep. */
 	void tick() {
 		if (paused)
 			return;
 		score.increment();
 
 		// Randomly place a BonusObject once in a while
 		if (Math.random() < .005) {
 			Point p = new Point();
 			double x = Math.random() * getWidth();
 			double y = Math.random() * getHeight();
 			p.setLocation(x, y);
 			BonusObject.placeAt(p, user, score, 20, this);
 		}
 
 		// Use an iterator so that we can remove elements during the loop
 		Iterator<LightCycle> it = players.iterator();
 		while (it.hasNext()) {
 			LightCycle l = it.next();
 
 			l.move();
 
 			// wrap if l has reached an edge
 			Point coords = wrapCoordinates(l.getRearCoords());
 			l.setPosition(coords);
 
 			for (ArenaObject o : spacesUnderLightCycle(l)) {
 				o.actOn(l);
 			}
 			if (l.isAlive()) {
 				if (l == user)
 					setAtPosition(l.getRearCoords(),
 							new UserTrail(l, l.getRearCoords(), score));
 				else
 					setAtPosition(l.getRearCoords(),
 							new Trail(l, l.getRearCoords()));
 			} else {
 				if (l == user)
 					System.out.println("You lose");
 				else if (players.size() == 2)
 					System.out.println("You win");
 				it.remove();
 				System.out.println(l + " is dead.");
 			}
 		}
 		repaint(); // Repaint indirectly calls paintComponent.
 	}
 
 	@Override
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g); // Paint background, border
 		if (background == null) {
 			background = new BufferedImage(getWidth(), getHeight(),
 					BufferedImage.TYPE_INT_RGB);
 			bg_graphics = background.createGraphics();
 			bg_graphics.setColor(Color.BLACK);
 			bg_graphics.drawRect(0, 0, getWidth(), getHeight());
 		}
 		g.drawImage(background, 0, 0, null);
 		for (ArenaObject[] a : arena)
 			for (ArenaObject b : a)
 				if (b != null)
 					b.draw(g);
 		for (LightCycle l : players)
 			l.draw(g);
 	}
 
 	public void togglePause() {
 		paused = !paused;
 		requestFocusInWindow();
 	}
 
 	@Override
 	public Dimension getPreferredSize() {
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		return new Dimension(screenSize.width, screenSize.height);
 	}
 }
