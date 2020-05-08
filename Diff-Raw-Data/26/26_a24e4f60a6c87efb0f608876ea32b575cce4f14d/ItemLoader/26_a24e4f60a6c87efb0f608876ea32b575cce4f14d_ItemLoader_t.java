 package ch.andefgassm.adventuregame.game.assets;
 
 import java.io.File;
 
 import ch.andefgassm.adventuregame.game.inventory.Item;
 import ch.andefgassm.adventuregame.game.state.GameStateContext;
 
 public class ItemLoader {
 
 	public static void load(GameStateContext gameStateContext)
 			throws AssetLoadException {
 
 		File folder = new File("assets/data/items");
 		File[] listOfFiles = folder.listFiles();
 
 		try {
 
 			for (int i = 0; i < listOfFiles.length; i++) {
 
 				File file = listOfFiles[i];
 				if (file.isFile() && file.getName().endsWith(".json")) {
 
 					Item item = AssetLoader.getObjectMapper()
 							.readValue(new File("assets/data/items/item.json"),
 									Item.class);
 					
 					gameStateContext.registerItem(item.getName(), item);
 
 				}
 			}
 			
 		} catch (Exception ex) {
 			throw new AssetLoadException(ex);
 		}
 	}
 
 }
