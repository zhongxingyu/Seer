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
 
 import javax.swing.JComponent;
 
 import src.GameController;
 import src.core.Map;
 import src.core.TileType;
 
 public class MapComponent extends JComponent {
 	private static final long serialVersionUID = 1L;
 
 	private Map m;
 	private GameController gc;
 	private boolean gridOn;
 
 	public MapComponent(Map m) {
 		this.m = m;
 		setupMouseEvents();
 	}
 	
 	private void setupMouseEvents() {
 		this.addMouseListener(new MouseAdapter() {
 			public void mouseClicked(MouseEvent e) {
 				if (gc.isPlacingTower()) {
 					Point mouse = getMouseTile();
 					
 					int x = mouse.x;
 					int y = mouse.y;
 					
 					if (!m.isTerrain(x, y) && !gc.tileIsOccupied(x, y) && !m.isPath(x, y)) {
 						gc.finalizeTowerPurchase(x, y);
 					}
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
 			return new Point(x, y);
 		}
 
 		return null;
 	}
 	
 	private double getTileWidth() {
 		return getWidth() / m.getWidth();
 	}
 	
 	private double getTileHeight() {
 		return getHeight() / m.getHeight();
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
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g);
 
 		// in swing, g is always actually an instance of the more advanced
 		// Graphics2D class
 		Graphics2D gg = (Graphics2D) g;
 
 		gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 				RenderingHints.VALUE_ANTIALIAS_ON);
 
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
 
 		if (gc.isPlacingTower()) {
 			Point mouse = getMouseTile();
 
 			if (mouse != null) {
 				int x = mouse.x;
 				int y = mouse.y;
 
 				// draw the radius indicator for the tower
 				Arc2D.Double radiusIndicator = new Arc2D.Double();
 				radiusIndicator.setArcByCenter(x * tileWidth + tileWidth / 2, 
 											   y * tileHeight + tileHeight / 2, 
 											   gc.getPlacingTower().getRadius() * tileWidth, 
 											   0, 360, Arc2D.OPEN);
 				
 				gg.setColor(ColorConstants.radiusIndicatorColor);
 				gg.fill(radiusIndicator);
 				gg.setColor(ColorConstants.radiusIndicatorColor2);
 				gg.draw(radiusIndicator);
 				
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
 				}
 			}
 		}
 
 		if (gc != null) {
			for (IDrawableCreep c : gc.getDrawableCreeps()) {
				CreepDrawer.drawCreep(c, tileHeight, tileWidth, gg);
 			}
 	
 			for (IDrawableTower t : gc.getDrawableTowers()) {
 				TowerDrawer.drawTower(t, tileHeight, tileWidth, gg);
 			}
 		}
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
 }
