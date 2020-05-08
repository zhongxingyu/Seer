 package nl.minicom.evenexus.gui.icons;
 
 import java.awt.Image;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * This class is responsible for loading images and icons.
  * 
  * @author michael
  */
 public enum Icon {
 	
 	CLOCK_16				("/img/16/clock.png"),
 	
 	ADD_32					("/img/32/plus.png"),
 	DISPLAY_32				("/img/32/display.png"),
 	PACKAGE_32				("/img/32/box.png"),
 	SEARCH_32				("/img/32/search.png"),
 	
 	ADD_USER_48 			("/img/48/user_add.png"),
 	BUG_REPORT_48			("/img/48/bug_report.png"),
 	DATABASE_ERROR_48		("/img/48/database_error.png"),
 	EXPORT_DATABASE_48		("/img/48/database_next.png"),
 	GRAPH_48				("/img/48/chart.png"),
 	LOGO_48					("/img/48/logo.png"),
 	PIE_CHART_48			("/img/48/pie_chart.png"),
 	SETTINGS_48				("/img/48/process.png"),
 	TABLE_48				("/img/48/table.png"),
	WARNING_48				("/img/48/warning.png"), 
	
	LOGO					("/img/other/logo.png"), 
	SPLASH					("/img/other/splash.png");
 	
 	private final String path;
 	
 	private Icon(String path) {
 		this.path = path;
 	}
 	
 	private String getPath() {
 		return path;
 	}
 
 	private static final Logger LOG = LoggerFactory.getLogger(Icon.class);
 	
 	/**
 	 * This method gets the {@link ImageIcon} object for the specified icon.
 	 * 
 	 * @param icon
 	 * 		The icon to retrieve.
 	 * 
 	 * @return
 	 * 		The constructed {@link ImageIcon}.
 	 */
 	public static ImageIcon getIcon(Icon icon) {
 		return getIcon(icon.getPath());
 	}
 	
 	/**
 	 * This method gets the {@link ImageIcon} object for the specified icon.
 	 * 
 	 * @param icon
 	 * 		The icon to retrieve.
 	 * 
 	 * @return
 	 * 		The constructed {@link ImageIcon}.
 	 */
 	@Deprecated
 	public static ImageIcon getIcon(String icon) {
 		try {
 			return new ImageIcon(ImageIO.read(Icon.class.getResourceAsStream(icon)));
 		}
 		catch (Exception e) {
 			LOG.error(e.getLocalizedMessage(), e);
 		}
 		return null;
 	}
 
 	/**
 	 * This method gets the {@link Image} object for the specified icon.
 	 * 
 	 * @param icon
 	 * 		The icon to retrieve.
 	 * 
 	 * @return
 	 * 		The contructed {@link Image}.
 	 */
 	@Deprecated
 	public static Image getImage(String icon) {
 		return getIcon(icon).getImage();
 	}
 
 	/**
 	 * This method gets the {@link Image} object for the specified icon.
 	 * 
 	 * @param icon
 	 * 		The icon to retrieve.
 	 * 
 	 * @return
 	 * 		The contructed {@link Image}.
 	 */
 	public static Image getImage(Icon icon) {
 		return getIcon(icon).getImage();
 	}
 	
 }
