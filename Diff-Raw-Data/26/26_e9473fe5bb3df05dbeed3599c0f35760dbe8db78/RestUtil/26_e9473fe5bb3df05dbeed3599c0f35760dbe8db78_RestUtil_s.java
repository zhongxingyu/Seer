 package org.openmrs.module.restmodule;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Vector;
 
 import org.openmrs.api.context.Context;
 import org.openmrs.api.context.ContextAuthenticationException;
 
 /**
  * Utility class for miscellaneous convenience functions used by the REST API
  * 
  * @author Burke Mamlin
  * @version 1.0
  */
 public class RestUtil {
 
 	/**
	 * Name of the REST module
 	 */
	public final static String MODULE_NAME = "restmodule";
 
 	/**
 	 * Name of global property containing list of valid client IP addresses
 	 */
	public final static String GP_ALLOWED_IP_LIST = MODULE_NAME
 			+ ".allowed_ip_list";
 
 	/**
 	 * Maximum number of results to return for any single call (0 for unlimited)
 	 */
	public final static String GP_MAX_RESULTS = MODULE_NAME + ".max_results";
 
 	/**
 	 * Length of time (in milliseconds) before refreshing global properties
 	 */
 	public final static long PROPERTY_REFRESH_INTERVAL = 600000;
 	
 	/**
 	 * Privilege required to access any REST API calls
 	 */
 	public static final String REST_API_PRIVILEGE = "Access REST API";
 
 	private static Object lock = new Object(); // used for synchronizing
 	private static List<String[]> allowedIpList = null;
 	private static long propertiesUpdatedTime = 0;
 	private static int maxResults = 0;
 
 	/**
 	 * Validate credentials
 	 * 
 	 * @param auth
 	 *            BASIC Authentication credentials -- i.e., "username:password"
 	 *            encoded with Base64
 	 * @return <code>true</code> if credentials are valid for the API
 	 */
 	public static boolean allowUser(String auth) {
 		if (auth == null)
 			return false; // only BASIC authentication
 
 		if (!auth.toUpperCase().startsWith("BASIC "))
 			return false; // only BASIC authentication
 
 		// Get encoded user and password following "BASIC "
 		String userpassEncoded = auth.substring(6);
 
 		String[] userpassDecoded = new String(Base64.decode(userpassEncoded))
 				.split(":");
 		String username = userpassDecoded[0];
 		String password = userpassDecoded[1];
 		try {
 			Context.authenticate(username, password);
 		} catch (ContextAuthenticationException e) {
 		}
 		return Context.isAuthenticated() && Context.hasPrivilege(REST_API_PRIVILEGE);
 	}
 
 	/**
 	 * Tests whether or not a client's IP address is allowed to have access to
 	 * the REST API (based on a global property)
 	 * 
 	 * @param remoteAddress
 	 *            address of the remote client
 	 * @return <code>true</code> if client should be allowed access
 	 */
 	public static boolean allowRemoteAddress(String remoteAddress) {
 		String[] remoteIp = remoteAddress.split("\\.");
 		List<String[]> allowed = getAllowedIpList();
 		for (String[] addr : allowed) {
 			boolean match = true;
 			for (int i = 0; i < addr.length; i++) {
 				if (!addr[i].equals(remoteIp[i]) && !addr[i].equals("*")) {
 					match = false;
 					break;
 				}
 			}
 			if (match)
 				return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Return the maximum number of results that should be returned by any call
 	 * 
 	 * @return maximum number of results to return (0 for unlimited)
 	 */
 	public static int getMaxResults() {
 		if (globalPropertiesDirty())
 			updateGlobalProperties();
 		return maxResults;
 	}
 
 	/**
 	 * Fetch the list of allowed IP addresses
 	 * 
 	 * @return allowed IP addresses
 	 */
 	private static List<String[]> getAllowedIpList() {
 		if (globalPropertiesDirty())
 			updateGlobalProperties();
 		return allowedIpList;
 	}
 
 	/**
 	 * Check if global properties have either not been loaded or have grown
 	 * stale
 	 * 
 	 * @return <code>true</code> if global properties need to be fetched from
 	 *         the database
 	 */
 	private static boolean globalPropertiesDirty() {
 		boolean dirty = false;
 		synchronized (lock) {
 			dirty = (allowedIpList == null || (new Date().getTime() - propertiesUpdatedTime) > PROPERTY_REFRESH_INTERVAL);
 		}
 		return dirty;
 	}
 
 	/**
 	 * Update global property settings from the database
 	 */
 	public static void updateGlobalProperties() {
 		synchronized (lock) {
 			// Update allowed IP list
 			String allowedIpListProperty = Context.getAdministrationService()
					.getGlobalProperty(GP_ALLOWED_IP_LIST);
 			String[] propList = allowedIpListProperty.split("[\\s,]+");
 			allowedIpList = new Vector<String[]>();
 			for (String allowedAddress : propList) {
 				allowedIpList.add(allowedAddress.split("\\."));
 			}
 			// Update max results
 			String maxResultProperty = Context.getAdministrationService()
					.getGlobalProperty(GP_MAX_RESULTS);
 			try {
 				maxResults = Integer.parseInt(maxResultProperty);
 			} catch (NumberFormatException e) {
 				maxResults = 0;
 			}
 			propertiesUpdatedTime = new Date().getTime();
 		}
 	}
 
 }
