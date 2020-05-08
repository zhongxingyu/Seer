 package src.ui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.RenderingHints;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.geom.Arc2D;
 import java.awt.geom.Rectangle2D;
 
import javax.swing.BorderFactory;
 import javax.swing.JComponent;
 
 import src.core.Bullet;
 import src.core.Map;
 import src.core.TileType;
 import src.ui.controller.GameController;
 
 /**
  * Handles events involving map and tile components and draws everything on the main game board.
  */
 public class MapComponent extends JComponent {
 	private static final long serialVersionUID = 1L;
 
 	private Map m;
 	private GameController gc;
 	private boolean gridOn;
 	private boolean readOnly;
 
 	public MapComponent(boolean readOnly) {
 		this.readOnly = readOnly;
		this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
 		
 		if (!readOnly)
 			setupMouseEvents();
 	}
 	
 	private void setupMouseEvents() {
 		this.addMouseListener(new MouseAdapter() {
 			public void mousePressed(MouseEvent e) {
 				Point mouse = getMouseTile();
 				
 				int x = mouse.x;
 				int y = mouse.y;
 				
 				if (gc.isPlacingTower()) {
 					if (!m.isTerrain(x, y) && !gc.tileIsOccupied(x, y) && !m.isPath(x, y)) {
 						gc.finalizeTowerPurchase(x, y);
 					}
 				} else if (gc.tileIsOccupied(x, y)) {
 					gc.toggleTowerSelection(x, y);
 				} else if (gc.isTowerSelected()) {
 					gc.toggleTowerSelection(gc.getSelectedTower().getX(), gc.getSelectedTower().getY());
 				}
 			}
 		});
 	}
 	
 	/**
 	 * Determines what tile of the map the user's mouse is positioned over.
 	 * @return the x and y tile coordinates of the user's mouse, or null, if the
 	 * 		   mouse is not positioned over this component.
 	 */
 	private Point getMouseTile() {
 		Point mouse = getMousePosition();
 		
 		if (mouse != null) {
 			int x = (int) (mouse.getX() / getTileWidth());
 			int y = (int) (mouse.getY() / getTileHeight());
 			
 			// due to small rounding errors in the drawing, sometimes we might calculate
 			// an index that is technically inside the frame, but is outside where we've drawn
 			// just count the mouse as not in the frame in these areas
 			if (x > 14 || y > 14) return null;
 			
 			return new Point(x, y);
 		}
 
 		return null;
 	}
 	
 	public Map getMap(){
 		return m;
 	}
 	
 	public void setMap(Map m){
 		this.m = m;
 	}
 	
 	private double getTileWidth() {
 		return getWidth() / (double) m.getWidth();
 	}
 	
 	private double getTileHeight() {
 		return getHeight() / (double) m.getHeight();
 	}
 
 	public void setGameController(GameController gc) {
 		this.gc = gc;
 	}
 	
 	public boolean isGridOn() {
 		return gridOn;
 	}
 
 	public void setGridOn(boolean gridOn) {
 		this.gridOn = gridOn;
 	}
 
 	@Override
 	/**
 	 * Paints the map and everything on it.  For details on how the map is drawn, see
 	 * comments inside.
 	 * 
 	 * @see TowerDrawer
 	 * @see CreepDrawer
 	 * @see TileDrawer
 	 */
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		
 		if (m == null) return;
 
 		// in swing, g is always actually an instance of the more advanced
 		// Graphics2D class
 		Graphics2D gg = (Graphics2D) g;
 
 		gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 				RenderingHints.VALUE_ANTIALIAS_ON);
 		gg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
 
 		double tileWidth = getTileWidth();
 		double tileHeight = getTileHeight();
 
 		Rectangle2D tile = new Rectangle2D.Double(0, 0, tileWidth, tileHeight);
 		for (int x = 0; x < m.getWidth(); x++) {
 			for (int y = 0; y < m.getHeight(); y++) {
 				tile.setFrame(x * tileWidth, y * tileHeight, tileWidth,
 						tileHeight);
 				TileType type = m.getTileType(x, y);
 				TileDrawer.paintTile(type, tile, gg);
 
 				if (isGridOn()) {
 					gg.setPaint(Color.BLACK);
 					gg.draw(tile);
 				}
 			}
 		} 
 
 		if (gc != null && !readOnly && gc.isPlacingTower()) { // draw appropriate graphics if we are placing a tile
 			Point mouse = getMouseTile();
 
 			if (mouse != null) {
 				int x = mouse.x;
 				int y = mouse.y;
 				
 				tile.setFrame(x * tileWidth, y * tileHeight, tileWidth, tileHeight);
 				
 				// draw error box over terrain squares / occupied squares
 				if (m.isTerrain(x, y) || gc.tileIsOccupied(x, y) || m.isPath(x, y)) {
 					gg.setColor(ColorConstants.invalidTowerPlacementColor);
 					gg.fill(tile);
 					gg.setColor(ColorConstants.invalidTowerPlacementColor2);
 					gg.draw(tile);
 				} else { // otherwise draw the tower
 					// these don't actually end up getting applied, they're just here
 					// so that the tower is drawn properly
 					gc.getPlacingTower().setX(x);
 					gc.getPlacingTower().setY(y);
 					
 					TowerDrawer.drawTower(gc.getPlacingTower(), tileHeight, tileWidth, gg);
 					
 					// draw the radius indicator for the tower
 					drawRadiusIndicator(gc.getPlacingTower(), gg);
 				}
 			}
 		} else if (!readOnly) {
 			Point mouse = getMouseTile();
 			
 			if (mouse != null) {
 				int x = mouse.x;
 				int y = mouse.y;
 				
 				if (gc != null && gc.tileIsOccupied(x, y)) {
 					// draw a hover display over any towers we're hovering over
 					tile.setFrame(x * tileWidth, y * tileHeight, tileWidth, tileHeight);
 					
 					gg.setColor(ColorConstants.towerHighlightColor);
 					gg.draw(tile);
 				}
 			}
 		}
 		
 		// show a tower as highlighted if it is currently selected, and show its radius
 		if (gc != null && gc.isTowerSelected()) {
 			IDrawableTower t = gc.getSelectedTower();
 			tile.setFrame(t.getX() * tileWidth, t.getY() * tileHeight, tileWidth, tileHeight);
 			gg.setColor(ColorConstants.towerHighlightColor);
 			gg.draw(tile);
 			
 			drawRadiusIndicator(t, gg);
 		}
 
 		if (gc != null) {
 			synchronized (gc.getBullets()) {
 				for (Bullet b : gc.getBullets()) {
 					Rectangle2D.Double bulletBox = new Rectangle2D.Double(b.getPosition().getX() * tileWidth,
 																		  b.getPosition().getY() * tileHeight,
 																		  2, 2);
 					gg.setColor(Color.RED);
 					gg.fill(bulletBox);
 				}
 			}
 			
 			for (IDrawableTower t : gc.getDrawableTowers()) {
 				TowerDrawer.drawTower(t, tileHeight, tileWidth, gg);
 			}
 			
 			synchronized (gc.getDrawableCreeps()) {
 				for (IDrawableCreep c : gc.getDrawableCreeps()) {
 					CreepDrawer.drawCreep(c, tileHeight, tileWidth, gg);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Draws a radius indicator for a given tower
 	 * @param t The tower whose radius we should draw
 	 * @param g The graphics context into which the radius indicator should be drawn
 	 */
 	private void drawRadiusIndicator(IDrawableTower t, Graphics2D g) {
 		Arc2D.Double radiusIndicator = new Arc2D.Double();
 		radiusIndicator.setArcByCenter(t.getX() * getTileWidth() + getTileWidth() / 2, 
 									   t.getY() * getTileHeight() + getTileHeight() / 2, 
 									   t.getRadius() * getTileWidth(), 
 									   0, 360, Arc2D.OPEN);
 		
 		g.setColor(ColorConstants.radiusIndicatorColor);
 		g.fill(radiusIndicator);
 		g.setColor(ColorConstants.radiusIndicatorColor2);
 		g.draw(radiusIndicator);
 	}
 
 	@Override
 	public Dimension getPreferredSize() {
 		return new Dimension(getWidth(), getHeight());
 	}
 
 	@Override
 	public Dimension getMinimumSize() {
 		return getPreferredSize();
 	}
 
 	@Override
 	public Dimension getMaximumSize() {
 		return getPreferredSize();
 	}
 
 	public void setReadOnly(boolean readOnly) {
 		this.readOnly = readOnly;
 	}
 }
