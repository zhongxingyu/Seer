 package ui.isometric;
 
 import game.Location;
 
 import java.awt.AlphaComposite;
 import java.awt.Color;
 import java.awt.Composite;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.RenderingHints;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.JPanel;
 
 import ui.isometric.abstractions.IsoImage;
 import ui.isometric.abstractions.IsoSquare;
 import ui.isometric.libraries.IsoRendererLibrary;
 import util.Direction;
 import util.Position;
 import util.Resources;
 
 
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
 	private IsoImage selectedImage;
 	private Position selectedSquarePosition;
 	private Point selectionPoint = new Point(0, 0);
 	private UILayerRenderer selectedRenderer;
 	
 	public static final double FPS = 20;
 	
 	private List<UILayerRenderer> extraRenderers = new ArrayList<UILayerRenderer>();
 	
 	private boolean draggingOk = true;
 	
 	private BufferedImage backbuffer;
 	private Graphics2D graphics;
 	private Image lightMask;
 	private Point lightPoint;
 	
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
 		public void render(Graphics2D g, IsoCanvas into);
 
 		/**
 		 * Can use this to figure out what was under the mouse.
 		 * Note don't use this for actions, as this being called is not a guarantee
 		 * that this renderer was not obscured by another renderer. To do that
 		 * wait for a call to wasClicked(), which will be called if this was the topmost
 		 * renderer. This will be called immediately after, so it is safe to store information
 		 * about the click between the two calls.
 		 * 
 		 * @param selectionPoint - the point in view coordinates of the selection
 		 * @param isoCanvas - the canvas this is in
 		 * @return whether this renderer would want to intercept the selection
 		 */
 		public boolean doSelectionPass(Point selectionPoint, IsoCanvas isoCanvas);
 
 		/**
 		 * Called after a call to doSelectionPass if this component was the topmost
 		 * of all the renderers and can act on a mouse down
 		 * @param event - the event that triggered the selection
 		 * @param canvas - the canvas the click was in
 		 */
 		public void wasClicked(MouseEvent event, IsoCanvas canvas);
 		
 		/**
 		 * Set the superview, ok to ignore
 		 * @param canvas
 		 */
 		public void setSuperview(IsoCanvas canvas);
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
 	 * Create a new IsoCanvas with a given datasource
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
 						Thread.sleep((long) (1000/FPS));
 					} catch (InterruptedException e) { }
 				}
 			}
 		}).start();
 		this.setFocusable(true);
 		
 		backbuffer = new BufferedImage(2560, 2560, BufferedImage.TYPE_INT_ARGB_PRE); // TODO: Max view size
 		graphics = backbuffer.createGraphics();
 		try {
 			graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 		}
 		catch(Exception e) {} // Stupid java bug when not on windows
 		
 		try {
 			lightMask = Resources.readImageResourceUnfliped("/resources/test/light_mask.png");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public void paint(Graphics jgraphics) {
 		dataSource.setViewableRect((int)origin.getX(), (int)origin.getY(), this.getWidth(), this.getHeight(), viewDirection);
 		dataSource.update();
 		
 		Point smoothing = dataSource.transform().smoothOrigin(origin);
 		
 		Composite oc = null;
 		
 		if(!selectionRender) {
 			jgraphics.fillRect(0, 0, this.getWidth(), this.getHeight());
 			
 			graphics.setBackground(new Color(0, 0, 0, 0));
 			graphics.clearRect(0, 0, this.getWidth(), this.getHeight());
 			oc = graphics.getComposite();
 			if(lightMask != null && lightPoint != null) {
 				graphics.drawImage(lightMask, lightPoint.x, lightPoint.y, null);
 				graphics.setComposite(AlphaComposite.SrcAtop);
 			}
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
 					this.drawSquareAt(graphics, (int)(x+smoothing.getX()), (int)(y+smoothing.getY()), xg, yg);
 					yg++;
 					xg++;
 				}
 			}
 			row++;
 		}
 		
 		if(!selectionRender) {
 			graphics.setComposite(oc);
 			
 			for(UILayerRenderer ren : extraRenderers) {
 				ren.render(graphics, this);
 			}
 			
 			jgraphics.drawImage(backbuffer, 0, 0, null);
 		}
 		else {
 			for(UILayerRenderer ren : extraRenderers) {
 				if(ren.doSelectionPass(selectionPoint, this)) {
 					selectedRenderer = ren;
 				}
 			}
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
 	private void drawSquareAt(Graphics2D g, int dx, int dy, int sx, int sy) {
 		IsoSquare square = dataSource.squareAt(sx, sy);
 		
 		if(square.numberOfImages() > 0) {
 			for(IsoImage i : square) {
 				if(!selectionRender) {
 					g.drawImage(i.image(), dx-i.width()/2, dy-i.height()-i.yoffset(), i.width(), i.height(), null, this);
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
 					IsoRendererLibrary.maskTile().getAlphaRaster().getPixel(x, y, pixels);
 					
 					if(pixels[0] > 0) {
 						selectedSquarePosition = dataSource.transform().transformViewToMap(new Position(sx, sy));
 					}
 				}
 			}
 		}
 	}
 	
 	@Override
 	public void mouseDragged(MouseEvent arg0) {
 		if(draggingOk) {
 			Point delta = new Point(arg0.getPoint().x-mouse.x, arg0.getPoint().y-mouse.y);
 			delta = dataSource.transform().transformRelativePoint(delta);
 			mouse = arg0.getPoint();
 			
 			origin.setLocation(origin.getX()-delta.getX(), origin.getY()+delta.getY());
 			this.repaint();
 		}
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent arg0) {}
 
 	@Override
 	public void mouseClicked(MouseEvent arg0) {
 		this.calculateTypesAtAtPoint(arg0.getPoint());
 		final IsoImage i = this.getCachedSelectedImage();
 		final Position s = this.getCachedSelectedSquarePosition();
 		final UILayerRenderer r = this.getCachedSelectedRenderer();
 		
 		if(r == null) {
 			for(SelectionCallback c : selectionCallback) {
 				c.selected(i, dataSource.level().location(s, Direction.NORTH), arg0);
 			}
 		}
 		else {
 			r.wasClicked(arg0, this);
 		}
 	}
 	
 	/**
 	 * Compute the image/square/renderer at a given point
 	 * Use
 	 * @param point
 	 */
 	public void calculateTypesAtAtPoint(Point point) {
 		selectedImage = null;
 		selectedSquarePosition = null;
 		selectedRenderer = null;
 		
 		selectionRender = true;
 		selectionPoint = point;
 		try {
 			this.paint(null);
 		}
 		catch (Exception e) {
 			System.err.println("Exception renderering for selection");
 			e.printStackTrace();
 		}
 		selectionRender = false;
 	}
 	
 	/**
 	 * Get the cached selected image, does no checking to ensure it is not stale,
 	 * should only be called directly after calculateTypesAtAtPoint
 	 * @return
 	 */
 	public IsoImage getCachedSelectedImage() {
 		return selectedImage;
 	}
 	
 	/**
 	 * Get the cached selected square location, does no checking to ensure it is not stale,
 	 * should only be called directly after calculateTypesAtAtPoint
 	 * @return
 	 */
 	public Position getCachedSelectedSquarePosition() {
 		return selectedSquarePosition;
 	}
 	
 	/**
 	 * Get the cached selected renderer, does no checking to ensure it is not stale,
 	 * should only be called directly after calculateTypesAtAtPoint
 	 * @return
 	 */
 	public UILayerRenderer getCachedSelectedRenderer() {
 		return selectedRenderer;
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent arg0) {}
 
 	@Override
 	public void mouseExited(MouseEvent arg0) {}
 
 	@Override
 	public void mousePressed(MouseEvent arg0) {
 		this.calculateTypesAtAtPoint(arg0.getPoint());
 		final UILayerRenderer r = this.getCachedSelectedRenderer();
 		
 		draggingOk = r == null;
 		
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
 			renderer.setSuperview(this);
 		}
 		else {
 			for(int n = 0; n < extraRenderers.size(); n++) {
 				if(extraRenderers.get(n).level() < renderer.level()) {
 					extraRenderers.add(n, renderer);
 					renderer.setSuperview(this);
 					return;
 				}
 			}
 			
 			extraRenderers.add(renderer);
 			renderer.setSuperview(this);
 		}
 	}
 	
 	/**
 	 * Remove a UiLayerRenderer
 	 * @param renderer
 	 */
 	public void removeLayerRenderer(UILayerRenderer renderer) {
 		extraRenderers.remove(renderer);
 		renderer.setSuperview(null);
 	}
 	
 	/**
 	 * Get the light position
 	 * @return
 	 */
 	public Point lightPoint() {
 		return lightPoint;
 	}
 	
 	/**
 	 * Set the light position
 	 * @param point
 	 */
 	public void setLightPoint(Point point) {
 		lightPoint = point;
 	}
 }
