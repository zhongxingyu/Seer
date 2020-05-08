 package icons;
 
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.swing.ImageIcon;
 
 /**
  * Manage icons.
  */
 public class IconManager {
 	static Map<String, ImageIcon> icons = new HashMap<String, ImageIcon>();
 	
 	private IconManager() {}
 	
 	/**
 	 * Fetch an icon by name.
 	 * @param name The icon's name.
 	 * @return The icon.
 	 */
 	public static ImageIcon icon(String name) {
 		if (!icons.containsKey(name)) {
			URL iconURL = null;
			
			if (iconURL == null) iconURL = IconManager.class.getResource("/icons/" + name);
			if (iconURL == null) iconURL = IconManager.class.getResource("icons/" + name);
			if (iconURL == null) iconURL = IconManager.class.getResource(name);
			
			if (iconURL == null) 
 				icons.put(name, null);
 			else 
 				icons.put(name, new ImageIcon(iconURL));
 		}
 			
 		return icons.get(name); 
 	}
 
 }
