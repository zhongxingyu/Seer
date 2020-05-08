 package org.jivesoftware.spark.plugin.mediaplayer.util;
 
 /**
  * MPResources needed to load icons and language files into Media Player plugin - Taken from OTRResources in the OTR plugin
  * 
  * @author Bergunde Holger
  */
 import java.text.MessageFormat;
 import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
 
 import javax.swing.ImageIcon;
 
 import org.jivesoftware.spark.util.log.Log;
 
 
 public class MPResources {
 
 	private static PropertyResourceBundle prb;
 
 	static ClassLoader cl = MPResources.class.getClassLoader();
 
	static {
		prb = (PropertyResourceBundle) ResourceBundle.getBundle("i18n/otrplugin_i18n");
	}
 
 	/**
 	 * Returns a string from the language file
 	 * 
 	 * @param propertyName
 	 * @return
 	 */
 	public static final String getString(String propertyName) {
 		try {
 			return prb.getString(propertyName);
 		}
 		catch (Exception e) {
 			Log.error(e);
 			return propertyName;
 		}
 	}
 
 	/**
	 * Returns an ImageIcon from OTR resources folder
 	 * 
 	 * @param fileName
 	 * @return
 	 */
 	public static ImageIcon getIcon(String fileName) {
 		final ClassLoader cl = MPResources.class.getClassLoader();
 		ImageIcon icon = new ImageIcon(cl.getResource(fileName));
 		return icon;
 	}
 
 	/**
 	 * Returns a string with wildcards
 	 * 
 	 * @param propertyName
 	 * @param obj
 	 */
 	public static String getString(String propertyName, Object... obj) {
 		String str = prb.getString(propertyName);
 		if (str == null) {
 			return propertyName;
 		}
 
 		return MessageFormat.format(str, obj);
 	}
 
 }
