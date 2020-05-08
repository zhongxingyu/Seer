 package yuuki.ui;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import javax.swing.JPanel;
 
 import yuuki.graphic.ImageFactory;
 import yuuki.util.ElementGrid;
 import yuuki.util.Grid;
 import yuuki.world.Locatable;
 import yuuki.world.Tile;
 import yuuki.world.TileFactory;
 
 /**
  * Displays the overworld graphically.
  */
 @SuppressWarnings("serial")
 public class WorldViewer extends JPanel {
 	
 	/**
 	 * The size of a tile, in pixels.
 	 */
 	public static final int TILE_SIZE = 32;
 	
 	/**
 	 * Generates tile graphics.
 	 */
 	private ImageFactory images;
 	
 	/**
 	 * The grid of tiles that make up the land being displayed.
 	 */
 	private Grid<Tile> land;
 	
 	/**
 	 * The section of the land that is shown in the view port.
 	 */
 	private Grid<Tile> landView;
 	
 	/**
 	 * The Locatables on the screen. They are arranged in layers, which specify
 	 * the z-ordering of the locatables. Higher layers are drawn above lower
 	 * layers.
 	 */
 	private Map<Integer, Set<Locatable>> locatables;
 	
 	/**
 	 * The exact locatables being displayed. This mirrors the land view. Key
 	 * iteration must be guaranteed to be ordered so that painting routines may
 	 * correctly render lower layers before higher layers.
 	 */
 	private SortedMap<Integer, Grid<Image>> resBuffers;
 	
 	/**
 	 * The sections of the locatables that contain the drawn residents.
 	 */
 	private Map<Integer, Grid<Image>> resBufferViews;
 	
 	/**
 	 * The exact images being displayed. This mirrors the land view.
 	 */
 	private Grid<Image> tileBuffer;
 	
 	/**
 	 * The section of the tile buffer that contains the drawn map.
 	 */
 	private Grid<Image> tileBufferView;
 	
 	/**
 	 * The height of this viewer, in tiles.
 	 */
 	private int tileHeight;
 	
 	/**
 	 * The width of this viewer, in tiles.
 	 */
 	private int tileWidth;
 	
 	/**
 	 * Creates a new WorldViewer that can display the specified number of
 	 * tiles.
 	 * 
 	 * @param width The width of this WorldViewer in tiles.
 	 * @param height The height of this WorldViewer in tiles.
 	 */
 	public WorldViewer(int width, int height) {
 		tileWidth = width;
 		tileHeight = height;
 		Dimension d = new Dimension(width, height);
 		tileBuffer = new ElementGrid<Image>(d);
 		locatables = new HashMap<Integer, Set<Locatable>>();
 		resBuffers = new TreeMap<Integer, Grid<Image>>();
 		setLayout(null);
 		Dimension size = new Dimension(width * TILE_SIZE, height * TILE_SIZE);
 		setPreferredSize(size);
 	}
 	
 	/**
 	 * Adds a resident to the current display.
 	 * 
 	 * @param l The resident to add.
 	 * @param zIndex The Z-index of the layer to add the resident to.
 	 */
 	public void addLocatable(Locatable l, int zIndex) {
 		if (!layerExists(zIndex)) {
 			createLayer(zIndex);
 		}
 		Set<Locatable> layer = getLayer(zIndex);
 		layer.add(l);
 	}
 	
 	/**
 	 * Removes all Locatables from the current display.
 	 */
 	public void clearLocatables() {
 		locatables.clear();
 	}
 	
 	/**
 	 * Gets the number of layers of Locatable objects. The layers with Z-index
 	 * 0 up to but not including the return value of this method are guaranteed
 	 * to exist.
 	 * 
 	 * @return The number of layers.
 	 */
 	public int getLayerCount() {
 		return locatables.size();
 	}
 	
 	/**
 	 * Gets the Locatables that fall within a certain rectangle.
 	 * 
 	 * @param box The Rectangle from within the Locatables should be drawn.
 	 * @param zIndex The Z-index of the layer of Locatables to search in.
 	 * 
 	 * @return The Locatables that currently fall within the bounding box.
 	 */
 	public ArrayList<Locatable> getLocatablesInBox(Rectangle box, int zIndex) {
 		ArrayList<Locatable> desired = new ArrayList<Locatable>();
 		Set<Locatable> layer = getLayer(zIndex);
 		for (Locatable l : layer) {
 			if (box.contains(l.getLocation())) {
 				desired.add(l);
 			}
 		}
 		return desired;
 	}
 	
 	/**
 	 * Sets the image factory for tile graphics.
 	 * 
 	 * @param imageFactory The ImageFactory to use.
 	 */
 	public void setImageFactory(ImageFactory imageFactory) {
 		images = imageFactory;
 	}
 	
 	/**
 	 * Sets the the reference to the tiles of the Land being displayed.
 	 * 
 	 * @param view The view to show.
 	 */
 	public void setLand(Grid<Tile> view) {
 		this.land = view;
 	}
 	
 	/**
 	 * Updates this WorldViewer to show a new area.
 	 * 
 	 * @param center The center of the area to show.
 	 */
 	public void updateDisplay(Point center) {
 		clearBuffers();
 		Point requested = setLandView(center);
 		setTileBufferView(requested);
 		setResBufferViews(requested);
 		drawTiles();
 		drawLocatables();
 		repaint();
 	}
 	
 	/**
 	 * Sets all tiles in the tile buffer to be empty.
 	 */
 	private void clearBuffers() {
 		clearTileBuffer();
 		clearResBuffers();
 	}
 	
 	/**
 	 * Clears the resident buffers of all content.
 	 */
 	private void clearResBuffers() {
 		for (int i : resBuffers.keySet()) {
 			resBuffers.get(i).clear();
 		}
 	}
 	
 	/**
 	 * Sets all tiles in the tile buffer to be void.
 	 */
 	private void clearTileBuffer() {
 		Image i = images.createImage(TileFactory.VOID_PATH);
 		Point p = new Point();
 		for (p.y = 0; p.y < tileHeight; p.y++) {
 			for (p.x = 0; p.x < tileWidth; p.x++) {
 				tileBuffer.set(p, i);
 			}
 		}
 	}
 	
 	/**
 	 * Sets a sub view of a buffer as the section that contains land data.
 	 * 
 	 * @param request The requested upper-left corner.
 	 * @param buffer The buffer that the sub view is being set up on.
 	 */
 	private Grid<Image> createBufferView(Point request, Grid<Image> buffer) {
 		Rectangle landBox, bufBox, bufViewBox;
 		landBox = new Rectangle(landView.getLocation(), landView.getSize());
 		bufBox = new Rectangle(buffer.getLocation(), buffer.getSize());
 		bufViewBox = new Rectangle(buffer.getLocation(), buffer.getSize());
 		if (landBox.height < bufBox.height) {
 			int shiftAmount = landBox.y - request.y;
 			bufViewBox.y += shiftAmount;
 			bufViewBox.height -= (bufBox.height - landBox.height);
 		}
 		if (landBox.width < bufBox.width) {
 			int shiftAmount = landBox.x - request.x;
 			bufViewBox.x += shiftAmount;
 			bufViewBox.width -= (bufBox.width - landBox.width);
 		}
 		Grid<Image> bufferView = buffer.getSubGrid(bufViewBox);
 		return bufferView;
 	}
 	
 	/**
 	 * Creates an empty Locatable layer with the given Z-index. If there is
 	 * already a layer with the given Z-index, it is replaced with an empty
 	 * layer.
 	 * 
 	 * @param zIndex The Z-index of the layer to create.
 	 */
 	private void createLayer(int zIndex) {
 		Dimension d = new Dimension(tileWidth, tileHeight);
 		locatables.put(zIndex, new HashSet<Locatable>());
 		resBuffers.put(zIndex, new ElementGrid<Image>(d));
 	}
 	
 	/**
 	 * Draws the locatables on the screen.
 	 */
 	private void drawLocatables() {
 		Rectangle box;
 		box = new Rectangle(landView.getLocation(), landView.getSize());
 		for (int i : locatables.keySet()) {
 			ArrayList<Locatable> ls = getLocatablesInBox(box, i);
 			for (Locatable l : ls) {
 				Point p = new Point(l.getLocation());
 				p.x -= box.x;
 				p.y -= box.y;
 				String imgIndex = l.getDisplayable().getOverworldImage();
 				Image img = images.createImage(imgIndex);
 				Grid<Image> bufferView = resBufferViews.get(i);
 				bufferView.set(p, img);
 			}
 		}
 	}
 	
 	/**
 	 * Draws the tiles in the land view onto the tile buffer.
 	 */
 	private void drawTiles() {
 		Point p = new Point(0, 0);
 		Dimension size = tileBufferView.getSize();
 		for (p.x = 0; p.x < size.width; p.x++) {
 			for (p.y = 0; p.y < size.height; p.y++) {
 				String imgIndex = landView.itemAt(p).getOverworldImage();
 				Image img = images.createImage(imgIndex);
 				tileBufferView.set(p, img);
 			}
 		}
 	}
 	
 	/**
 	 * Gets one layer of the Locatables to be drawn on the screen.
 	 * 
 	 * @param zIndex The Z-index of the layer to get.
 	 * 
 	 * @return The layer.
 	 */
 	private Set<Locatable> getLayer(int zIndex) {
 		return locatables.get(zIndex);
 	}
 	
 	/**
 	 * Checks whether a Locatable with a given Z-index exists.
 	 * 
 	 * @param zIndex The Z-index of the layer to check.
 	 * 
 	 * @return True if the layer exists; otherwise, false.
 	 */
 	private boolean layerExists(int zIndex) {
 		return (locatables.containsKey(zIndex));
 	}
 	
 	/**
 	 * Paints the elements in a buffer on to a graphical context.
 	 * 
 	 * @param g The graphical context to paint the elements on to.
 	 * @param buffer The buffer whose elements are to be painted.
 	 */
 	private void paintElements(Graphics2D g, Grid<Image> buffer) {
 		final int w = TILE_SIZE;
 		final int h = TILE_SIZE;
 		Point p = new Point(0, 0);
 		for (p.y = 0; p.y < tileHeight; p.y++) {
 			for (p.x = 0; p.x < tileWidth; p.x++) {
 				Image img = buffer.itemAt(p);
 				if (img != null) {
 					g.drawImage(img, p.x * w, p.y * h, w, h, this);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Paints the Locatables in this world viewer.
 	 * 
 	 * @param g The Graphics2D context to paint the Locatables on.
 	 */
 	private void paintLocatables(Graphics2D g) {
 		for (int i : resBuffers.keySet()) {
 			Grid<Image> buffer = resBuffers.get(i);
 			paintElements(g, buffer);
 		}
 	}
 	
 	/**
 	 * Paints the land tiles in this world viewer.
 	 * 
 	 * @param g The Graphics2D context to paint the tile images on.
 	 */
 	private void paintTiles(Graphics2D g) {
 		paintElements(g, tileBuffer);
 	}
 	
 	/**
 	 * Gets the proper sub view centered about a point.
 	 * 
 	 * @param center The center of the view to set as the sub view.
 	 * 
 	 * @return The position of the requested upper-left corner.
 	 */
 	private Point setLandView(Point center) {
 		Dimension size = tileBuffer.getSize();
 		Point actualLocation = new Point(center);
 		actualLocation.translate(-(size.width / 2), -(size.height / 2));
 		Rectangle subBox = new Rectangle(actualLocation, size);
 		landView = land.getSubGrid(subBox);
 		return subBox.getLocation();
 	}
 	
 	/**
 	 * Sets the resident buffer views as the sections of the resident buffers
 	 * that contain land tiles.
 	 * 
 	 * @param request The upper-left corner of the requested buffer view.
 	 */
 	private void setResBufferViews(Point request) {
 		for (int i : resBuffers.keySet()) {
 			Grid<Image> buffer = resBuffers.get(i);
 			Grid<Image> view = createBufferView(request, buffer);
 			resBufferViews.put(i, view);
 		}
 	}
 	
 	/**
 	 * Sets the tile buffer view as the section of the tile buffer that
 	 * contains land tiles.
 	 * 
 	 * @param request The requested upper-left corner.
 	 */
 	private void setTileBufferView(Point request) {
 		tileBufferView = createBufferView(request, tileBuffer);
 	}
 	
 	@Override
 	protected void paintComponent(Graphics g) {
 		Graphics2D g2 = (Graphics2D) g;
 		paintTiles(g2);
 		paintLocatables(g2);
 	}
 	
 }
