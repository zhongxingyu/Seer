 package net.dockter.sguide.guide;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 
 
 
 public class GuideManager {
 	private static final Map<String, Guide> loadedGuides = new HashMap<String, Guide>();
 	
 	static {
 		File dir = new File("plugins"+File.separator+"SpoutGuide"+File.separator+"guides");
 		dir.mkdirs();
 		for(File file : dir.listFiles()) {
 			Guide g = Guide.load(file);
 			loadedGuides.put(g.getName(),g);
 		}
 	}
 	
 	public static void disable() {
 		for(Guide guide : loadedGuides.values()) {
 			guide.save();
 		}
 	}
 	
 	public static Map<String, Guide> getLoadedGuides() {
 		return loadedGuides;
 	}
 	
 	public static void addGuide(Guide newGuide) {
 		loadedGuides.put(newGuide.getName(), newGuide);
 	}
 
 	public static void removeLoadedGuide(String text) {
 		loadedGuides.get(text).delete();
 		loadedGuides.remove(text);
 	}
 
 }
