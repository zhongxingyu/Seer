 package pleocmd.itfc.gui.icons;
 
 import java.net.URL;
 
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 
 /**
  * Handles loading of icons.<br>
  * All icons which should be accessible by this class must be in the same
  * package and should be PNG files.
  * 
  * @author oliver
  */
 public final class IconLoader {
 
 	private IconLoader() {
 		// hidden - just static methods
 	}
 
 	/**
 	 * Loads an icon and returns an instance to it.
 	 * 
 	 * @param name
 	 *            name of the icon without any path and optionally without
 	 *            extension it it is a PNG file
 	 * @return instance to the icon or instance to a special "image-missing"
 	 *         icon if the icon specified by name could not be found or loaded
 	 */
 	public static Icon getIcon(final String name) {
 		if (name == null) return getMissingIcon();
 		final URL url = IconLoader.class.getResource(name.contains(".") ? name
 				: name + ".png");
 		if (url == null) return getMissingIcon();
 		return new ImageIcon(url, name);
 	}
 
 	/**
 	 * Loads a special icon and returns an instance to it.
 	 * 
 	 * @return instance to a special "image-missing" icon or an empty icon if
 	 *         this icon could not be found or loaded
 	 */
 	public static Icon getMissingIcon() {
 		final URL url = IconLoader.class.getResource("image-missing.png");
		if (url == null) return new ImageIcon();
 		return new ImageIcon(url, "Missing an icon file in current classpath");
 	}
 
 }
