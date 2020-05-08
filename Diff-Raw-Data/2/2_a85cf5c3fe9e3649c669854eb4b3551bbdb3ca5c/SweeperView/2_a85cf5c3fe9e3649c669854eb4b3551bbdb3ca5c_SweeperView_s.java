 package demos.sweeper;
 
 import vitro.*;
 import vitro.grid.*;
 import vitro.util.*;
 import java.awt.*;
 import java.awt.geom.*;
 import java.awt.image.*;
 import java.io.*;
 import javax.imageio.*;
 import java.util.*;
 
 public class SweeperView implements View {
 
 	protected final Sweeper     model;
 	protected final Controller  controller;
 	protected final ColorScheme colors;
 
 	protected final int buffer;
 	protected final int width;
 	protected final int height;
 
 	protected final Color background = new Color(198, 196, 198);
 	protected final Color darker     = new Color(132, 130, 132);
 	protected final Color brighter   = new Color(255, 255, 255);
 
 	protected final Component outer;
 	protected final Component score;
 	protected final Component board;
 	protected final Component mines;
 	protected final Component ticks;
 	protected final Component smile;
 	protected final java.util.List<Cell> cells;
 
 	protected final Color[] mineColors = {
 		background,
 		new Color(   0,   0, 255),
 		new Color(   0, 130,   0),
 		new Color( 255,   0,   0),
 		new Color(   0,   0, 132),
 		new Color( 132,   0,   0),
 		new Color(   0, 132, 132),
 		new Color( 132,   0, 132),
 		new Color(   0,   0,   0)
 	};
 
 	protected State state;
 
 	public SweeperView(Sweeper model, Controller controller) {
 		this.model      = model;
 		this.controller = controller;
 		this.colors     = new ColorScheme(Color.BLACK, Color.GRAY, Color.WHITE);
 
 		this.buffer   = 10;
 		this.width    = model.width  * (Cell.SIZE + 1) + 2 * buffer +  9;
 		this.height   = model.height * (Cell.SIZE + 1) + 3 * buffer + 51;
 
 		this.outer = new Component(
 			new Rectangle(
 				1, 1, 
 				width - 2, height - 2
 			), 
 			2, false
 		);
 
 		this.score = new Component(
 			new Rectangle(
 				outer.panel.x + buffer, outer.panel.y + buffer,
 				outer.panel.width - 2 * buffer, 41
 			),
 			2, true
 		);
 
 		this.board = new Component(
 			new Rectangle(
 				outer.panel.x + buffer, score.bound.y + score.bound.height + buffer,
 				outer.panel.width - 2 * buffer, outer.panel.width - 2 * buffer
 			),
 			2, true
 		);
 
 		this.mines = new MinesComponent(
 			new Rectangle(
 				score.panel.x + 6, score.panel.y + (score.panel.height / 2) - 12,
 				40, 24
 			),
 			1, true
 		);
 
 		this.ticks = new TicksComponent(
 			new Rectangle(
 				score.panel.x + score.panel.width - 40 - 6, score.panel.y + (score.panel.height / 2) - 12,
 				40, 24
 			),
 			1, true
 		);
 
 		this.smile = new SmileComponent(
 			new Rectangle(
 				score.panel.x + (score.panel.width / 2) - 13, score.panel.y + (score.panel.height / 2) - 13,
 				27, 27
 			),
 			3, false
 		);
 
 		this.cells = new ArrayList<Cell>(model.width * model.height);
 		for(int y = 0; y < model.width; y++) {
 			for(int x = 0; x < model.height; x++) {
 				cells.add(new Cell(new Location(model, x, y)));
 			}
 		}
 
 		state = updateState();
 	}
 	
 	public Controller  controller()  { return controller; }
 	public ColorScheme colorScheme() { return colors;     }
 	public int         width()       { return width;      }
 	public int         height()      { return height;     }
 	
 	private double sofar = 0;
 	public void tick(double time) {
 		sofar += time;
 		if (sofar > .2) {
 			controller.next();
 			flush();
 			sofar = 0;
 		}
 	}
 	public void flush() {
 		synchronized(model) {
 			state = updateState();
 		}
 	}
 
 	
 	public void draw(Graphics gt) {
 		Graphics2D g = (Graphics2D)gt;
 
 		synchronized(model) {
 
 			outer.draw(g);
 			score.draw(g);
 			board.draw(g);
 			mines.draw(g);
 			ticks.draw(g);
 			smile.draw(g);
 
 			for(Cell cell : cells) {
 				cell.draw(g);
 			}
 		}
 	}
 
 	protected class State {
 		public final boolean    completed;
 		public final boolean    success;
 		public final Location   selection;
 		public final Annotation annotation;
 		public final int        numMines;
 		public final int        numFlags;
 		public final int        numTicks;
 
 		public State(boolean completed, boolean success, Location selection, Annotation annotation, int numMines, int numFlags, int numTicks) {
 			this.completed  = completed;
 			this.success    = success;
 			this.selection  = selection;
 			this.annotation = annotation;
 			this.numMines   = numMines;
 			this.numFlags   = numFlags;
 			this.numTicks   = numTicks;
 		}
 	}
 
 	private State updateState() {
 		Action action     = Groups.firstOfType(Sweeper.FlipAction.class, controller.previousActions());
 
 		Location   previous   = action != null ? ((Sweeper.FlipAction)action).location : null;
 		Annotation annotation = Groups.firstOfType(GridAnnotation.class, controller.annotations().keySet());
 
 		int mines = Groups.ofType(Sweeper.Mine.class, model.actors).size();
 		int flags = annotation != null ? ((GridAnnotation)annotation).coloring.keySet().size() : 0;
 		int ticks = 0;
 
 		return new State(model.done(), model.success(), previous, annotation, mines, flags, ticks);
 	}
 
 	protected enum Smiles {
 		WHOO (0), GASP (1), DEAD (2), COOL (3);
 
 		private final int index;
 		private Smiles(int index) {
 			this.index = index;
 		}
 	}
 
 	private static BufferedImage faces;
 	{
 		try {
 			ClassLoader loader = SweeperView.class.getClassLoader();
 			faces = ImageIO.read(loader.getResource("demos/sweeper/faces.png"));
 		}
 		catch(IOException e) {
 			throw new Error("Unable to load image resource (faces).");
 		}
 	}
 
 	private static BufferedImage numbers;
 	{
 		try {
 			ClassLoader loader = SweeperView.class.getClassLoader();
 			numbers = ImageIO.read(loader.getResource("demos/sweeper/numbers.png"));
 		}
 		catch(IOException e) {
 			throw new Error("Unable to load image resource (numbers).");
 		}
 	}
 
 	protected static Image smileImage(Smiles smile) {
 		return faces.getSubimage(17 * smile.index, 0, 17, 17);
 	}
 
 	protected static Image numberImage(int value) {
 		return numbers.getSubimage(13 * value, 0, 13, 23);
 	}
 
 	protected class Component {
 		public final Rectangle bound;
 		public final Rectangle panel;
 
 		public final Color ulBezel;
 		public final Color drBezel;
 		public final Color fill;
 
 		public Component(Rectangle bound, int depth, boolean depressed) {
 			this.bound = bound;
 			this.panel = new Rectangle(
 				bound.x + depth, bound.y + depth,
 				bound.width - 2 * depth, bound.height - 2 * depth
 			);
 
 			if(depressed) {
 				ulBezel = darker;
 				drBezel = brighter;
 			}
 			else {
 				ulBezel = brighter;
 				drBezel = darker;
 			}
 			fill = background;
 		}
 
 		public void draw(Graphics2D g) {
 			Drawing.drawBezelRect(g, bound, panel.x - bound.x, ulBezel, drBezel, fill);
 		}
 	}
 
 	protected class MinesComponent extends Component {
 		public MinesComponent(Rectangle bound, int depth, boolean depressed) {
 			super(bound, depth, depressed);
 		}
 
 		public void draw(Graphics2D g) {
 			int counter = Math.max((state.numMines - state.numFlags) % 1000, 0);
 
 			super.draw(g);
 			for(int n = 0; n < 3; n++, counter /= 10) {
 				g.drawImage(
 					numberImage(counter % 10),
 					panel.x + 13 * (3 - n - 1), panel.y, 13, 23,
 					null
 				);
 			}
 		}
 	}
 
 	protected class TicksComponent extends Component {
 		public TicksComponent(Rectangle bound, int depth, boolean depressed) {
 			super(bound, depth, depressed);
 		}
 
 		public void draw(Graphics2D g) {
 			int counter = state.numTicks % 1000;
 
 			super.draw(g);
 			for(int n = 0; n < 3; n++, counter /= 10) {
 				g.drawImage(
 					numberImage(counter % 10),
 					panel.x + 13 * (3 - n - 1), panel.y, 13, 23,
 					null
 				);
 			}
 		}
 	}
 
 	protected class SmileComponent extends Component {
 		public SmileComponent(Rectangle bound, int depth, boolean depressed) {
 			super(bound, depth, depressed);
 		}
 
 		public void draw(Graphics2D g) {
 			Smiles smile = Smiles.WHOO;
 			if(state.completed && !state.success) { smile = Smiles.DEAD; }
 			if(state.completed &&  state.success) { smile = Smiles.COOL; }
 
 			super.draw(g);
 			g.drawImage(
 				smileImage(smile),
 				panel.x + 2, panel.y + 2, 17, 17,
 				null
 			);
 		}
 	}
 
 	protected class Cell {
 		public static final int SIZE = 16;
 
 		public final Location     location;
 		public final Rectangle    area;
 		public final Sweeper.Mine mine;
 
 		public Cell(Location location) {
 			this.location = location;
 			this.area = new Rectangle(
 				board.panel.x + location.x * (SIZE + 1), board.panel.y + location.y * (SIZE + 1), 
 				SIZE, SIZE
 			);
 
 			Actor actor = Groups.firstOfType(Sweeper.Mine.class, model.actorsAt(location));
 			this.mine = actor != null ? (Sweeper.Mine)actor : null;
 		}
 
 		public void drawCell(Graphics2D g) {
 			Drawing.drawBezelRect(g, area, 2, brighter, darker, background);
 		}
 
 		public void drawMine(Graphics2D g) {
 			int tx = board.panel.x + location.x * (SIZE + 1) + (SIZE + 1) / 2;
 			int ty = board.panel.y + location.y * (SIZE + 1) + (SIZE + 1) / 2;
 
 			Drawing.drawCircleCentered(g, tx, ty, 3, Color.BLACK, Color.BLACK);
 			g.drawLine(tx    , ty - 4, tx    , ty + 4);
 			g.drawLine(tx - 4, ty    , tx + 4, ty    );
 			g.drawLine(tx - 4, ty - 4, tx + 4, ty + 4);
 			g.drawLine(tx - 4, ty + 4, tx + 4, ty - 4);
 		}
 
 		public void drawFlag(Graphics2D g) {
 			int bx = board.panel.x + location.x * (SIZE + 1);
 			int by = board.panel.y + location.y * (SIZE + 1);
 
 			g.setColor(colors.outline);
 			g.fillRect(bx +  6, by + 13, 8, 2);
 			g.fillRect(bx +  8, by + 12, 4, 1);
 			g.fillRect(bx + 10, by +  9, 1, 3);
 
 			g.setColor(((GridAnnotation)state.annotation).coloring.get(new Point(location.x, location.y)));
 			g.fillPolygon(
 				new int[] { bx + 4, bx + 11, bx + 10 },
 				new int[] { by + 6, by +  4, by + 10 },
 				3
 			);
 		}
 
 		public void drawFail(Graphics2D g) {
 
 		}
 
 		public void drawCount(Graphics2D g) {
 			int count = model.count(location);
 			if(count < 0) { return; }
 		
 			g.setFont(g.getFont().deriveFont(Font.BOLD, 12f));
 			g.setColor(mineColors[count]);
 		
 			int tx = board.panel.x + location.x * (SIZE + 1) + (SIZE + 1) / 2 - 1;
 			int ty = board.panel.y + location.y * (SIZE + 1) + (SIZE + 1) / 2 + 1;
 		
 			Drawing.drawStringCentered(g, "" + count, tx    , ty    );
 			Drawing.drawStringCentered(g, "" + count, tx + 1, ty    );
 			Drawing.drawStringCentered(g, "" + count, tx    , ty + 1);
 			Drawing.drawStringCentered(g, "" + count, tx + 1, ty + 1);
 		}
 
 		public void drawStandard(Graphics2D g) {
 			g.setColor(darker);
 			g.draw(area);
 
 			if(model.hidden.contains(location)) {
 				drawCell(g);
 
 				if(state.annotation != null && ((GridAnnotation)state.annotation).coloring.keySet().contains(new Point(location.x, location.y))) {
 					drawFlag(g);
 				}
 			}
 			else {
 				drawCount(g);
 			}
 		}
 
 		public void drawSuccess(Graphics2D g) {
 			g.setColor(darker);
 			g.draw(area);
 
 			if(mine != null) {
 				drawCell(g);
 				drawFlag(g);
 			}
 			else {
 				drawCount(g);
 			}
 		}
 
 		public void drawFailure(Graphics2D g) {
 			if(state.selection.equals(location)) {
 				g.setColor(Color.RED);
 				g.fill(area);
 			}
 
 			g.setColor(darker);
 			g.draw(area);
 
 			if(state.annotation != null && ((GridAnnotation)state.annotation).coloring.keySet().contains(new Point(location.x, location.y))) {
 				drawCell(g);
 
 				if(state.selection.equals(location)) {
 					// WTF are you doing?!
 					if(state.selection.equals(location)) {
 						g.setColor(Color.RED);
 						g.fill(area);
 					}
 
 					g.setColor(darker);
 					g.draw(area);
 
					drawMine();
 				}
 				else {
 					if(mine == null) { drawFail(g); }
 					else             { drawFlag(g); }
 				}
 			}
 			else {
 				if(mine == null) {
 					if(model.hidden.contains(location)) {
 						drawCell(g);
 					}
 					else {
 						drawCount(g);
 					}
 				}
 				else {
 					drawMine(g);
 				}
 			}
 		}
 
 		public void draw(Graphics2D g) {
 			if(!state.completed) {
 				drawStandard(g);
 			}
 			else {
 				if(state.success) { drawSuccess(g); }
 				else              { drawFailure(g); }
 			}
 		}
 	}
 }
