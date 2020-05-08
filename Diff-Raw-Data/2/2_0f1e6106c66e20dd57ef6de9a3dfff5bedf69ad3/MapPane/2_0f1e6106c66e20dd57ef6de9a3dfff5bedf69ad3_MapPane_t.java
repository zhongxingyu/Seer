 package jzi.view;
 
 import java.awt.AlphaComposite;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Composite;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.Stroke;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.NoninvertibleTransformException;
 import java.awt.geom.Point2D;
 import java.awt.image.AffineTransformOp;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 
 import javax.imageio.ImageIO;
 import javax.swing.JPanel;
 
 import jzi.model.IGame;
 import jzi.model.IPlayer;
 import jzi.model.IZombie;
 import jzi.model.SuperZombie;
 import jzi.model.map.Coordinates;
 import jzi.model.map.Field;
 import jzi.model.map.ICoordinates;
 import jzi.model.map.IField;
 import jzi.model.map.ITile;
 import jzi.model.map.Tile;
 
 /**
  * Renders the game map.
  * 
  * @author Tobias Groth, Buddy Jonte
  * 
  */
 public class MapPane extends JPanel {
 	/**
 	 * Starting scale of the map.
 	 */
 	private static final double START_SCALE = 0.5;
 	/**
 	 * Generated serialVersionUID.
 	 */
 	private static final long serialVersionUID = 4607860473640965464L;
 	/**
 	 * Affine transform used to translate and scale the map.
 	 */
 	private AffineTransform at;
 	/**
 	 * Game handle.
 	 */
 	private IGame game;
 	/**
 	 * Current x translation.
 	 */
 	private double translateX;
 	/**
 	 * Current y translation.
 	 */
 	private double translateY;
 	/**
 	 * Current scale.
 	 */
 	private double scale;
 	/**
 	 * Image used for ammunition.
 	 */
 	private BufferedImage ammoImage;
 	/**
 	 * Image used for life points.
 	 */
 	private BufferedImage lifeImage;
 	/**
 	 * Image used for zombies.
 	 */
 	private BufferedImage zombieImage;
 	/**
 	 * Image user for super zombies.
 	 */
 	private BufferedImage superZombieImage;
 	/**
 	 * Clicked point transformed to image coordinates.
 	 */
 	private Point2D transformedPoint;
 	/**
 	 * Last clicked point.
 	 */
 	private Point2D lastClicked;
 	/**
 	 * Field coordinates that the mouse is currently hovering over.
 	 */
 	private ICoordinates mouseCoords;
 
 	/**
 	 * Constructor with game instance, initializes transform attributes and
 	 * loads images.
 	 * 
 	 * @param game
 	 *            game instance
 	 * @param zombieImage2
 	 * @param ammoImage2
 	 * @param lifeImage
 	 */
 	public MapPane(IGame game, BufferedImage lifeImage,
 			BufferedImage ammoImage, BufferedImage zombieImage) {
 		super();
 		this.game = game;
 		translateX = 0;
 		translateY = 0;
 		scale = START_SCALE;
 		this.ammoImage = ammoImage;
 		this.lifeImage = lifeImage;
 		this.zombieImage = zombieImage;
 
 		try {
 			AffineTransform scaleTransform = new AffineTransform();
 			AffineTransformOp scaleOp;
 
 			scaleTransform
 					.scale(Tile.TILE_SIZE / 800.0, Tile.TILE_SIZE / 800.0);
 
 			scaleOp = new AffineTransformOp(scaleTransform,
 					AffineTransformOp.TYPE_BILINEAR);
 
 			superZombieImage = ImageIO.read(new File(
 					"./resource/gameObjects/SuperZombie.png"));
 
 			ammoImage = scaleOp.filter(ammoImage, null);
 			lifeImage = scaleOp.filter(lifeImage, null);
 			zombieImage = scaleOp.filter(zombieImage, null);
 			superZombieImage = scaleOp.filter(superZombieImage, null);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		PanningHandler panning = new PanningHandler();
 
 		addMouseListener(panning);
 		addMouseMotionListener(panning);
 		addMouseWheelListener(new ScaleHandler());
 	}
 
 	/**
 	 * Handles painting of the map.
 	 * 
 	 * @param g
 	 *            graphics object that handles the actual painting
 	 */
 	@Override
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g);
 
 		// get the graphics object and save the current transform
 		Graphics2D graphics = (Graphics2D) g;
 		AffineTransform transformBackup = graphics.getTransform();
 
 		// paint the background
 		graphics.setColor(Color.BLACK);
 		graphics.fillRect(0, 0, getWidth(), getHeight());
 
 		// create a new affine transform
 		at = new AffineTransform(transformBackup);
 
 		// and apply our transformations
 		at.translate(getWidth() / 2, getHeight() / 2);
 		at.scale(scale, scale);
 		at.translate(-getWidth() / 2, -getHeight() / 2);
 
 		at.translate(translateX, translateY);
 
 		// set our new transform
 		graphics.setTransform(at);
 
 		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
 				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
 		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 				RenderingHints.VALUE_ANTIALIAS_ON);
 
 		// paint all tiles
 		for (ITile tile : game.getMap().getTiles()) {
 			paintTile(graphics, tile);
 		}
 
 		// paint empty spots
 		if (game.getCurrentTile() != null) {
 			paintEmptyTiles(graphics);
 			paintCurrentTile(graphics);
 		}
 
 		// paint all zombies
 		paintZombies(graphics);
 
 		// paint all players
 		paintPlayers(graphics);
 
 		// and restore the old transform
 		graphics.setTransform(transformBackup);
 	}
 
 	/**
 	 * Paints one tile onto the map.
 	 * 
 	 * @param graphics
 	 *            graphics object that handles the actual painting
 	 * @param tile
 	 *            tile to be painted
 	 */
 	private void paintTile(Graphics2D graphics, ITile tile) {
 		ICoordinates coords = tile.getCoordinates();
 		BufferedImage image = TileGraphic.getRenderImage(tile);
 
 		// draw the tile image
 		graphics.drawImage(image, coords.getX() * Tile.TILE_SIZE, coords.getY()
 				* Tile.TILE_SIZE, null);
 
 		// draw objects on each field
 		for (int x = 0; x < Tile.WIDTH_FIELDS; x++) {
 			for (int y = 0; y < Tile.HEIGHT_FIELDS; y++) {
 				IField field = tile.getField(y, x);
 
 				// draw ammunition
 				// multiply tile coordinates by tile size, and field coordinates
 				// by field size
 				if (field.hasAmmo()) {
 					graphics.drawImage(ammoImage, coords.getX()
 							* Tile.TILE_SIZE + x * Field.FIELD_SIZE,
 							coords.getY() * Tile.TILE_SIZE + y
 									* Field.FIELD_SIZE, null);
 				}
 
 				// draw life points
 				if (field.hasLife()) {
 					graphics.drawImage(lifeImage, coords.getX()
 							* Tile.TILE_SIZE + x * Field.FIELD_SIZE,
 							coords.getY() * Tile.TILE_SIZE + y
 									* Field.FIELD_SIZE, null);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Paints all empty spots where a tile may be placed.
 	 * 
 	 * @param graphics
 	 *            Graphics object to paint onto
 	 */
 	private void paintEmptyTiles(Graphics2D graphics) {
 		Composite comp = graphics.getComposite();
 		ITile tile = game.getCurrentTile();
 
 		graphics.setComposite(AlphaComposite.getInstance(
				AlphaComposite.SRC_OVER, 0.2f));
 
 		for (ICoordinates coords : game.getMap().getEmptyTiles()) {
 			if (coords.equals(mouseCoords)) {
 				continue;
 			}
 
 			if (game.getMap().checkTileRotations(coords, tile)) {
 				graphics.setColor(Color.GREEN);
 			} else {
 				graphics.setColor(Color.RED);
 			}
 
 			graphics.fillRect(coords.getX() * Tile.TILE_SIZE, coords.getY()
 					* Tile.TILE_SIZE, Tile.TILE_SIZE, Tile.TILE_SIZE);
 		}
 
 		graphics.setComposite(comp);
 	}
 
 	private void paintCurrentTile(Graphics2D graphics) {
 		ITile tile = game.getCurrentTile();
 		Composite comp = graphics.getComposite();
 
 		if (mouseCoords == null
 				|| !game.getMap().getEmptyTiles().contains(mouseCoords)) {
 			return;
 		}
 
 		if (game.getMap().checkTile(mouseCoords, tile)) {
 			graphics.setComposite(AlphaComposite.getInstance(
 					AlphaComposite.SRC_OVER, 0.6f));
 		} else {
 			graphics.setComposite(AlphaComposite.getInstance(
 					AlphaComposite.SRC_OVER, 0.25f));
 		}
 
 		graphics.drawImage(TileGraphic.getRenderImage(tile), mouseCoords.getX()
 				* Tile.TILE_SIZE, mouseCoords.getY() * Tile.TILE_SIZE, null);
 
 		graphics.setComposite(comp);
 	}
 
 	/**
 	 * Paints all zombies on the map.
 	 * 
 	 * @param graphics
 	 *            graphics object that handles the painting
 	 */
 	private void paintZombies(Graphics2D graphics) {
 		// iterate through zombies
 		for (IZombie zombie : game.getZombies()) {
 			ICoordinates field = zombie.getCoordinates().toRelativeField();
 			ICoordinates tile = zombie.getCoordinates().toTile();
 
 			// check type of zombie and draw according image
 			if (zombie instanceof SuperZombie) {
 				graphics.drawImage(superZombieImage, tile.getX()
 						* Tile.TILE_SIZE + field.getX() * Field.FIELD_SIZE,
 						tile.getY() * Tile.TILE_SIZE + field.getY()
 								* Field.FIELD_SIZE, null);
 			} else {
 				graphics.drawImage(zombieImage, tile.getX() * Tile.TILE_SIZE
 						+ field.getX() * Field.FIELD_SIZE, tile.getY()
 						* Tile.TILE_SIZE + field.getY() * Field.FIELD_SIZE,
 						null);
 			}
 		}
 
 		// draw a border around the current zombie if there is one
 		if (game.getCurrentZombie() != null) {
 			ICoordinates coords = game.getCurrentZombie().getCoordinates();
 			ICoordinates tile = coords.toTile();
 			ICoordinates field = coords.toRelativeField();
 			Stroke stroke = graphics.getStroke();
 
 			// draw a red border around the current zombie
 			graphics.setColor(Color.GREEN);
 			graphics.setStroke(new BasicStroke(5));
 			graphics.drawRect(tile.getX() * Tile.TILE_SIZE + field.getX()
 					* Field.FIELD_SIZE,
 					tile.getY() * Tile.TILE_SIZE + field.getY()
 							* Field.FIELD_SIZE, Field.FIELD_SIZE,
 					Field.FIELD_SIZE);
 			graphics.setStroke(stroke);
 		}
 	}
 
 	/**
 	 * Paints all the players onto the map.
 	 * 
 	 * @param graphics
 	 *            graphics object that handles the actual painting
 	 */
 	private void paintPlayers(Graphics2D graphics) {
 		HashMap<ICoordinates, LinkedList<Color>> colorMap = new HashMap<>();
 
 		// iterate through players and add their color to their coordinates
 		for (IPlayer player : game.getPlayers()) {
 			ICoordinates coords = player.getCoordinates();
 
 			if (colorMap.get(coords) == null) {
 				colorMap.put(coords, new LinkedList<Color>());
 			}
 
 			colorMap.get(coords).add(player.getColor());
 		}
 
 		// for each point that has at least one player, paint their circles or
 		// arcs
 		for (Map.Entry<ICoordinates, LinkedList<Color>> entry : colorMap
 				.entrySet()) {
 			ICoordinates coords = entry.getKey();
 			// defines the position of the player on the arc
 			int i = 0;
 			// defines the arc size for one player at these coordinates
 			int size = 360 / entry.getValue().size();
 			int x = coords.toTile().getX() * Tile.TILE_SIZE
 					+ coords.toRelativeField().getX() * Field.FIELD_SIZE + 40;
 			int y = coords.toTile().getY() * Tile.TILE_SIZE
 					+ coords.toRelativeField().getY() * Field.FIELD_SIZE + 40;
 
 			// draw the arc for each player (each color)
 			for (Color color : entry.getValue()) {
 				graphics.setColor(color);
 				graphics.fillArc(x, y, Field.FIELD_SIZE - 80,
 						Field.FIELD_SIZE - 80, i * size, size);
 				i++;
 			}
 		}
 	}
 
 	/**
 	 * Defines this component's preferred size.
 	 * 
 	 * @return preferred size
 	 */
 	@Override
 	public Dimension getPreferredSize() {
 		return new Dimension(2000, 2000);
 	}
 
 	/**
 	 * Gets the last point clicked on the map.
 	 * 
 	 * @return last clicked point
 	 */
 	public Point2D getLastPoint() {
 		return lastClicked;
 	}
 
 	/**
 	 * Sets the last clicked point for testing purposes.
 	 * 
 	 * @param last
 	 *            new point
 	 */
 	public void setLastPoint(Point2D last) {
 		lastClicked = last;
 	}
 
 	/**
 	 * Handles panning of the map view.
 	 * 
 	 * @author Buddy Jonte
 	 * 
 	 */
 	private class PanningHandler implements MouseListener, MouseMotionListener {
 		/**
 		 * Reference x coordinate; defines where the user initiated the panning
 		 * action.
 		 */
 		private double referenceX;
 		/**
 		 * Reference y coordinate; defines where the user initiated the panning
 		 * action.
 		 */
 		private double referenceY;
 		/**
 		 * Transform that was valid at the beginning of the panning.
 		 */
 		private AffineTransform initialTransform;
 
 		/**
 		 * Invoked when a mouse button has been pressed on the map.
 		 * 
 		 * @param e
 		 *            Mouse event for this action
 		 */
 		@Override
 		public void mousePressed(MouseEvent e) {
 			// transform the clicked point into map coordinates
 			try {
 				transformedPoint = at.inverseTransform(e.getPoint(), null);
 			} catch (NoninvertibleTransformException te) {
 				return;
 			}
 
 			// save point as last clicked point
 			lastClicked = transformedPoint;
 			// save reference coordinates for panning
 			referenceX = transformedPoint.getX();
 			referenceY = transformedPoint.getY();
 			// save transform for later use
 			initialTransform = at;
 		}
 
 		/**
 		 * Invoked when a mouse button is pressed on the component and the mouse
 		 * is dragged.
 		 * 
 		 * @param e
 		 *            Mouse event for this action
 		 */
 		@Override
 		public void mouseDragged(MouseEvent e) {
 			// transform new point into map coordinates
 			try {
 				transformedPoint = initialTransform.inverseTransform(
 						e.getPoint(), null);
 			} catch (NoninvertibleTransformException te) {
 				return;
 			}
 
 			// calculate the dragged distance along each axis
 			double deltaX = transformedPoint.getX() - referenceX;
 			double deltaY = transformedPoint.getY() - referenceY;
 
 			// update the reference point
 			referenceX = transformedPoint.getX();
 			referenceY = transformedPoint.getY();
 
 			// translate according to movement
 			translateX += deltaX;
 			translateY += deltaY;
 
 			// and repaint
 			repaint();
 		}
 
 		/**
 		 * Invoked after the mouse is released; unused in this class.
 		 * 
 		 * @param e
 		 *            corresponding mouse event
 		 */
 		public void mouseClicked(MouseEvent e) {
 		}
 
 		/**
 		 * Invoked when the mouse enters the component; unused in this class.
 		 * 
 		 * @param e
 		 *            corresponding mouse event
 		 */
 		public void mouseEntered(MouseEvent e) {
 		}
 
 		/**
 		 * Invoked when the mouse exits the component; unused in this class.
 		 * 
 		 * @param e
 		 *            corresponding mouse event
 		 */
 		public void mouseExited(MouseEvent e) {
 			mouseCoords = null;
 			repaint();
 		}
 
 		/**
 		 * Invoked when the mouse is moved inside the component; unused in this
 		 * class.
 		 * 
 		 * @param e
 		 *            corresponding mouse event
 		 */
 		public void mouseMoved(MouseEvent e) {
 			// transform new point into map coordinates
 			try {
 				mouseCoords = Coordinates.tileFromPoint(at.inverseTransform(
 						e.getPoint(), null));
 			} catch (NoninvertibleTransformException te) {
 				return;
 			}
 
 			repaint();
 		}
 
 		/**
 		 * Invoked when the mouse is released; unused in this class.
 		 * 
 		 * @param e
 		 *            corresponding mouse event
 		 */
 		public void mouseReleased(MouseEvent e) {
 		}
 	}
 
 	/**
 	 * Handles scaling of the map view.
 	 * 
 	 * @author Buddy Jonte
 	 * 
 	 */
 	private class ScaleHandler implements MouseWheelListener {
 		/**
 		 * Maximum scale of the map.
 		 */
 		private static final double MAX_SCALE = 2.0;
 		/**
 		 * Minimum scale of the map.
 		 */
 		private static final double MIN_SCALE = 0.1;
 		/**
 		 * Factor by which the map is scaled after a mouse wheel movement.
 		 */
 		private static final double SCALE_FACTOR = 1.1;
 
 		/**
 		 * Invoked when the mouse wheel is moved over the component. Adjusts the
 		 * map scale accordingly.
 		 * 
 		 * @param e
 		 *            corresponding mouse event
 		 */
 		@Override
 		public void mouseWheelMoved(MouseWheelEvent e) {
 			double newScale;
 
 			// check the mouse wheel direction
 			if (e.getWheelRotation() > 0) {
 				newScale = scale / SCALE_FACTOR;
 			} else {
 				newScale = scale * SCALE_FACTOR;
 			}
 
 			// set the new scale
 			scale = newScale;
 			// and make sure it is within bounds
 			scale = Math.max(MIN_SCALE, scale);
 			scale = Math.min(MAX_SCALE, scale);
 
 			// then repaint the map
 			repaint();
 		}
 	}
 }
