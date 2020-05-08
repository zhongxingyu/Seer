 package se.chalmers.tda367.std.mapeditor;
 
 import java.awt.Graphics;
 import java.io.IOException;
 
 import javax.swing.JPanel;
 
 import com.google.common.eventbus.Subscribe;
 
 import se.chalmers.tda367.std.core.Properties;
 import se.chalmers.tda367.std.mapeditor.events.*;
 import se.chalmers.tda367.std.utilities.EventBus;
 import se.chalmers.tda367.std.utilities.IO;
 
 @SuppressWarnings("serial")
 public class MapJPanel extends JPanel {
 
 	private LevelMap mapModel;
 	
 	public MapJPanel() {
 		EventBus.INSTANCE.register(this);
 	}
 
 	@Override
 	public void paintComponent(Graphics g) {
 		if(mapModel == null) {
 			super.paintComponents(g);
 			return;
 		}
	
 		int scale = Properties.INSTANCE.getTileScale();
 		int width = mapModel.getWidth();
 		int height = mapModel.getHeight();
 		
 		// Set the graphics to the native sprite and draw the tile.
 		for(int y = 0; y < height; y++) {
 			for(int x = 0; x < width; x++) {
 				PlaceableTile pTile = mapModel.getMapItem(x, y);
 				NativeSwingSprite nss = (NativeSwingSprite)pTile.getInstance().getSprite().getNativeSprite();
 				nss.setGraphics(g);
 				
 				int nX = x * scale;
 				int nY = y * scale;
 				nss.draw(nX, nY, scale, scale);
 				
 				// Draw indication of "special" tiles (center the text).
 				nY = nY + scale / 2;
 				if(pTile == PlaceableTile.ENEMY_START_TILE) {
 					g.drawString("START", nX, nY);
 				} else if(pTile == PlaceableTile.WAYPOINT) {
 					g.drawString("W", nX + scale / 2, nY);
 				}
 			}
 		}
 		
 		super.paintComponents(g);
 	}
 
 	@Subscribe
 	public void createNewMap(CreateMapEvent e) {
 		PlaceableTile defaultItem = e.getDefaultTile().convertToPlaceableTile();
 		int width = e.getWidth();
 		int height = e.getHeight();
 		int level = e.getLevel();
 		
 		mapModel = new LevelMap(level, width, height);
 		
 		// Populate the newly created map with the selected default item.
 		for(int y = 0; y < height; y++) {
 			for(int x = 0; x < width; x++) {
 				mapModel.setMapItem(x, y, defaultItem);
 			}
 		}
 		
 		EventBus.INSTANCE.post(new MapLoadedEvent()); // Notify all listeners that a map has been loaded.
 		this.repaint();
 	}
 	
 	@Subscribe
 	public void placeTileOnMap(TilePlacementEvent e) {
 		int x = e.getX();
 		int y = e.getY();
 		
 		setMapItem(x, y, e.getTile());
 	}
 	
 	@Subscribe
 	public void openMap(OpenMapEvent e) {
 		String errorMessage = null;
 		try {
 			mapModel = IO.loadObject(LevelMap.class, e.getSelectedMap());
 		} catch (ClassNotFoundException | IOException e1) {
 			errorMessage = e1.getMessage();
 		}
 		EventBus.INSTANCE.post(new MapLoadedEvent(errorMessage));
 	}
 	
 	@Subscribe
 	public void saveMap(SaveMapEvent event) {
 		try {
 			IO.saveObject(mapModel ,event.getNewMapFile());
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void setMapItem(int x, int y, PlaceableTile tile) {
 		mapModel.setMapItem(x, y, tile);
 	}
 }
