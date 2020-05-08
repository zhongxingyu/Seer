 package com.app47.embeddedagent;
 
 import javax.microedition.global.Formatter;
 
 import net.rim.device.api.i18n.MissingResourceException;
 import net.rim.device.api.i18n.ResourceBundle;
 
class UtilAgentResourceGetter implements com.blackberry.embeddedagent.res.AgentResourcesResource {
 	
 	private static ResourceBundle resourceBundle = ResourceBundle.getBundle(
 			BUNDLE_ID, BUNDLE_NAME);
 
 	/**
 	 * Get a string from the language resources
 	 * 
 	 * @param resourceKey
 	 *            unique identification of the searched string
 	 * @throws MissingResourceException
 	 *             if the searched string cannot be found
 	 */
 	public static String getString(int resourceKey)
 			throws MissingResourceException {
 		return resourceBundle.getString(resourceKey);
 	}
 
 	/**
 	 * Get a formatted string from the language resources
 	 * 
 	 * @param resourceKey
 	 *            unique identification of the searched string
 	 * @param param
 	 *            parameters that will be inserted instead of the corresponding
 	 *            {n} expressions
 	 * @throws MissingResourceException
 	 *             if the searched string cannot be found
 	 */
 	public static String getString(int resourceKey, String[] param)
 			throws MissingResourceException {
 		String result = resourceBundle.getString(resourceKey);
 
 		return Formatter.formatMessage(result, param);
 	}
 }
