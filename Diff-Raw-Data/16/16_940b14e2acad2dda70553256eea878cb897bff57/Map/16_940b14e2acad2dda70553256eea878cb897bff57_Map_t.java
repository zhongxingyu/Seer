 package se.doverfelt.dungeonCrawl;
 
 import java.util.List;
 
//import it.randomtower.engine.ResourceManager;
 import it.randomtower.engine.World;
 import it.randomtower.engine.actors.StaticActor;
 
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.tiled.TiledMap;
 import org.newdawn.slick.util.Log;
 
 public class Map {
 
 	private TiledMap map;
 	
 	public Map(String mapName) {
 		map = ResourceManager.getMap(mapName);
 	}
 	
 	public void loadEntityFromMap(List<String> types, World world)
 			throws SlickException {
 		if (map == null) {
 			Log.error("Unable to load map information");
 			return;
 		}
 		if (types == null || types.isEmpty()) {
 			Log.error("No types defined to load");
 			return;
 		}
 		// layer have property type, so check it
 		for (String type : types) {
 			// try to find a layer with property type set to entity
 			int layerIndex = -1;
 			for (int l = 0; l < map.getLayerCount(); l++) {
 				String value = map.getLayerProperty(l, "type", null);
 				if (value != null && value.equalsIgnoreCase(type)) {
 					layerIndex = l;
 					break;
 				}
 			}
 			if (layerIndex != -1) {
 				Log.debug("Entity layer found on map");
 				int loaded = 0;
 				
 				for (int w = 0; w < map.getWidth(); w++) {
 					for (int h = 0; h < map.getHeight(); h++) {
 						Image img = map.getTileImage(w, h, layerIndex);
 						if (img != null && map.getLayerProperty(layerIndex, "type", null).equalsIgnoreCase("ground")) {
 							// load entity from Tiled map position and set Image
 							// for static actor using image reference stored
 							// into tiled map
 							StaticActor te = new StaticActor(
 									w * img.getScaledCopy(4).getWidth(), h * img.getScaledCopy(4).getHeight(),
 									img.getScaledCopy(4).getWidth(), img.getScaledCopy(4).getHeight(), img.getScaledCopy(4));
 							world.add(te);
 							loaded++;
 						} else if (img != null && map.getLayerProperty(layerIndex, "type", null).equalsIgnoreCase("tree")) {
 							// load entity from Tiled map position and set Image
 							// for static actor using image reference stored
 							// into tiled map
 							Tile te = new Tile((float)w * img.getScaledCopy(4).getWidth(), (float)h * img.getScaledCopy(4).getHeight(), img.getScaledCopy(4));
 							world.add(te);
 							loaded++;
 						}
 					}
 				}
 				
 				world.setHeight((map.getHeight() - 1) * 16 * 4);
 				world.setWidth((map.getWidth() - 1) * 16 * 4);
 				
 				Log.debug("Loaded " + loaded + " entities");
 			} else {
 				Log.info("Entity layer not found on map");
 			}
 		}
 	}
 	
 	public void render(Graphics g) {
 		// render all except entities
 		for (int l = 0; l < map.getLayerCount(); l++) {
 			String value = map.getLayerProperty(l, "type", null);
 			if (value == null || !value.equalsIgnoreCase("entity")) {
 				for (int w = 0; w < map.getWidth(); w++) {
 					for (int h = 0; h < map.getHeight(); h++) {
 						Image img = map.getTileImage(w, h, l).getScaledCopy(4);
 						if (img != null) {
 							g.drawImage(img, w * img.getWidth(), h * img.getHeight());
 						}
 					}
 				}
 			}
 		}
 	}
 	
 }
