 package ui.isometric;
 
 import game.Level;
 import game.Location;
 
 import java.awt.Color;
 import java.awt.Graphics;
import java.awt.Graphics2D;
 import java.awt.Point;
import java.awt.RenderingHints;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.JPanel;
 
 import util.Direction;
 import util.Position;
 
 
 /**
  * 
  * This canvas will render a section of the world, using the given datasource.
  * 
  * @author melby
  *
  */
 public class IsoCanvas extends JPanel implements MouseMotionListener, MouseListener {
 	private static final long serialVersionUID = 1L;
 	
 	public static final int TILE_X = 64;
 	public static final int TILE_Y = 44;
 	
 	private IsoDataSource dataSource;
 	
 	private Direction viewDirection = Direction.NORTH;
 	
 	private Point mouse;
 	private Point origin = new Point(0, 0);
 	
 	private boolean selectionRender = false;
 	private IsoImage selectedImage = null;
 	private Position selectedSquarePosition = null;
 	private Point selectionPoint = new Point(0, 0);
 	
 	private static final double fps = 20;
 	
 	private List<UILayerRenderer> extraRenderers = new ArrayList<UILayerRenderer>();
 	
 	/**
 	 * An interface for rendering extra content on top of the IsoCanvas
 	 * 
 	 * @author melby
 	 *
 	 */
 	public static interface UILayerRenderer {
 		/**
 		 * The level this renderer should renderer at
 		 * @return
 		 */
 		public int level();
 		
 		/**
 		 * Do the rendering into the specified Graphics context
 		 * @param g
 		 * @param into
 		 */
 		public void render(Graphics g, IsoCanvas into);
 	}
 		
 	/**
 	 * An interface for objects that wish to be added to the set of objects to be notified when a selection is made
 	 * 
 	 * @author melby
 	 *
 	 */
 	public interface SelectionCallback {
 		/**
 		 * A specific image and location that was selected
 		 * @param image - the image selected
 		 * @param loc - the location under the mouse
 		 * @param event
 		 */
 		public void selected(IsoImage image, Location loc, MouseEvent event);
 	}
 	
 	private Set<SelectionCallback> selectionCallback = new HashSet<SelectionCallback>();
 	
 	/**
 	 * Create a new IsoCanvas with a given interface and datasource
 	 * @param inter
 	 * @param dataSource
 	 */
 	public IsoCanvas(IsoDataSource dataSource) {
 		this.dataSource = dataSource;
 		this.addMouseListener(this);
 		this.addMouseMotionListener(this);
 		
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				while(true) {
 					repaint();
 					try {
 						Thread.sleep((long) (1000/fps));
 					} catch (InterruptedException e) { }
 				}
 			}
 		}).start();
 		this.setFocusable(true);
 	}
 	
 	@Override
 	public void paint(Graphics g) {
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 		dataSource.setViewableRect((int)origin.getX(), (int)origin.getY(), this.getWidth(), this.getHeight(), viewDirection);
 		dataSource.update();
 		
 		Point smoothing = dataSource.transform().smoothOrigin(origin);
 		
 		if(!selectionRender) {
 			g.setColor(Color.BLACK);
 			g.fillRect(0, 0, this.getWidth(), this.getHeight());
 		}
 		
 		int rowY = TILE_Y/2;
 		int tileCountY = this.getHeight()/rowY+5;
 		int tileCountX = this.getWidth()/TILE_X+2;
 		int row = 0;
 		
 		int y = -rowY; // rowY
 		for(;y<tileCountY*rowY;y+=rowY) {
 			if(!selectionRender || selectionPoint.y < y+smoothing.getY()) {
 				int yg = -((row%2 == 0)?row/2-1:row/2);
 				int xg = (row%2 == 0)?row/2:(row-1)/2;
 				int x = ((row%2 == 0)?TILE_X/2:0)-TILE_X;
 				for(;x<tileCountX*TILE_X;x+=TILE_X) {
 					this.drawSquareAt(g, (int)(x+smoothing.getX()), (int)(y+smoothing.getY()), xg, yg);
 					yg++;
 					xg++;
 				}
 			}
 			row++;
 		}
 		
 		for(UILayerRenderer ren : extraRenderers) {
 			ren.render(g, this);
 		}
 	}
 	
 	/**
 	 * Draw a square at a given location
 	 * @param g
 	 * @param dx
 	 * @param dy
 	 * @param sx
 	 * @param sy
 	 */
 	private void drawSquareAt(Graphics g, int dx, int dy, int sx, int sy) {
 		IsoSquare square = dataSource.squareAt(sx, sy);
 		
 		if(square.numberOfImages() > 0) {
 			for(IsoImage i : square) {
 				if(!selectionRender) {
 					g.drawImage(i.image(), dx-i.width()/2, dy-i.height(), i.width(), i.height(), null, this);
 				}
 				else {
 					if(selectionPoint.x > dx-i.width()/2 && selectionPoint.x < dx+i.width()/2) { // Check x
 						if(selectionPoint.y > dy-i.height() && selectionPoint.y < dy) { // Check y
 							int x = selectionPoint.x - dx + i.width()/2;
 							int y = selectionPoint.y - dy + i.height();
 							
 							int[] pixels = new int[4];
 							i.image().getAlphaRaster().getPixel(x, y, pixels);
 							
 							if(pixels[0] > 0) {
 								selectedImage = i;
 							}
 						}
 					}
 				}
 			}
 		}
 		
 		if(selectionRender) {
 			if(selectionPoint.x > dx-TILE_X/2 && selectionPoint.x < dx+TILE_X/2) { // Check x
 				if(selectionPoint.y > dy-TILE_Y && selectionPoint.y < dy) { // Check y
 					int x = selectionPoint.x - dx + TILE_X/2;
 					int y = selectionPoint.y - dy + TILE_Y;
 					
 					int[] pixels = new int[4];
 					IsoRendererLibrary.emptyTile().getAlphaRaster().getPixel(x, y, pixels);
 					
 					if(pixels[0] > 0) {
 						selectedSquarePosition = dataSource.transform().transformViewPosition(new Position(sx, sy));
 					}
 				}
 			}
 		}
 	}
 	
 	@Override
 	public void mouseDragged(MouseEvent arg0) {
 		Point delta = new Point(arg0.getPoint().x-mouse.x, arg0.getPoint().y-mouse.y);
 		delta = dataSource.transform().transformRelativePoint(delta);
 		mouse = arg0.getPoint();
 		
 		origin.setLocation(origin.getX()-delta.getX(), origin.getY()+delta.getY());
 		this.repaint();
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent arg0) {}
 
 	@Override
 	public void mouseClicked(MouseEvent arg0) {
 		final IsoImage i = this.getImageAtPoint(arg0.getPoint());
 		final Position s = this.getCachedSquarePosition();
 		
 		for(SelectionCallback c : selectionCallback) {
 			c.selected(i, new Level.Location(dataSource.level(), s, Direction.NORTH), arg0);
 		}
 	}
 
 	/**
 	 * Search for the image at a given point in the canvas
 	 * @param point
 	 * @return
 	 */
 	public IsoImage getImageAtPoint(Point point) {
 		selectedImage = null;
 		selectedSquarePosition = null;
 		
 		selectionRender = true;
 		selectionPoint = point;
 		try {
 			this.paint(null);
 		}
 		catch (Exception e) { }
 		selectionRender = false;
 		
 		return selectedImage;
 	}
 	
 	/**
 	 * Search for the square position at a given point in the canvas
 	 * @param point
 	 * @return
 	 */
 	public Position getSquarePositionAtPoint(Point point) {
 		selectedImage = null;
 		selectedSquarePosition = null;
 		
 		selectionRender = true;
 		selectionPoint = point;
 		try {
 			this.paint(null);
 		}
 		catch (Exception e) { }
 		selectionRender = false;
 		
 		return selectedSquarePosition;
 	}
 	
 	/**
 	 * Get the cached square location, does no checking to ensure it is not stale,
 	 * should only be called directly after getImageAtPoint/getSquarePositionAtPoint
 	 * @return
 	 */
 	public Position getCachedSquarePosition() {
 		return selectedSquarePosition;
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent arg0) {}
 
 	@Override
 	public void mouseExited(MouseEvent arg0) {}
 
 	@Override
 	public void mousePressed(MouseEvent arg0) {
 		mouse = arg0.getPoint();
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0) {}
 	
 	/**
 	 * Add a SelectionCallback that will be called when an image/square is selected
 	 * @param s
 	 */
 	public void addSelectionCallback(SelectionCallback s) {
 		selectionCallback.add(s);
 	}
 	
 	/**
 	 * Remove a given SelectionCallback
 	 * @param s
 	 */
 	public void removeSelectionCallback(SelectionCallback s) {
 		selectionCallback.remove(s);
 	}
 
 	/**
 	 * Get the view direction
 	 * @return
 	 */
 	public Direction viewDirection() {
 		return viewDirection;
 	}
 
 	/**
 	 * Set the view direction
 	 * @param direction
 	 */
 	public void setViewDirection(Direction direction) {
 		viewDirection = direction;
 	}
 	
 	/**
 	 * Add a UiLayerRenderer to the correct place in the list of renderers based on the level
 	 * @param renderer
 	 */
 	public void addLayerRenderer(UILayerRenderer renderer) { // TODO: log duplicates?
 		if(extraRenderers.size() == 0) {
 			extraRenderers.add(renderer);
 		}
 		else {
 			for(int n = 0; n < extraRenderers.size(); n++) {
 				if(extraRenderers.get(n).level() > renderer.level()) {
 					extraRenderers.add(n, renderer);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Remove a UiLayerRenderer
 	 * @param renderer
 	 */
 	public void removeLayerRenderer(UILayerRenderer renderer) {
 		extraRenderers.remove(renderer);
 	}
 }
