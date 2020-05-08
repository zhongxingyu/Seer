 package com.gentics.cr.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Reader;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.UUID;
 
 import org.apache.log4j.Logger;
 
 import com.gentics.api.lib.datasource.Datasource;
 import com.gentics.api.lib.datasource.Datasource.Sorting;
 import com.gentics.cr.util.resolver.CRUtilResolver;
 
 /**
  * 
  * Last changed: $Date: 2010-04-01 15:24:41 +0200 (Do, 01 Apr 2010) $
  * @version $Revision: 543 $
  * @author $Author: supnig@constantinopel.at $
  *
  */
 public class CRUtil {
 
 	/**
 	 * Utility class.
 	 */
 	private CRUtil() {
 		
 	}
 	
 	/**
 	 * marker for properties that shouldn't be resolved.
 	 */
 	private static final String DONOTRESOLVE_MARKER = "DONOTRESOLVE";
 	/**
 	 * system property holding the Gentics Portal.Node confpath.
 	 */
 	public static final String PORTALNODE_CONFPATH = "com.gentics.portalnode.confpath";
 
 	/**
 	 * Log4J Logger for debugging purposes.
 	 */
 	private static final Logger LOGGER = Logger.getLogger(CRUtil.class);
 
 	/**
 	 * Convert a String like "contentid:asc,name:desc" into an Sorting Array.
 	 * Errorhandling or errorrcognition is poor at that time.
 	 * 
 	 * @param sortingString like "contentid:asc,name:desc"
 	 * @return a Sorting[] Array suitable for contentrepository queries
 	 */
 	public static final Sorting[] convertSorting(final String sortingString) {
 
 		String[] sortingArray;
 		HashSet<Sorting> sortingColl = new HashSet<Sorting>();
 
 		if (sortingString != null) {
 
 			// split sorting attributes on ,.
 			sortingArray = sortingString.split(",");
 
 			// look at each attribute for sort direction
 			for (int i = 0; i < sortingArray.length; i++) {
 
 				// split attribute on :. First element is attribute name the
 				// second is the direction
 				String[] sort = sortingArray[i].split(":");
 
 				if (sort[0] != null) {
 					if ("asc".equals(sort[1].toLowerCase())) {
 						sortingColl.add(new Sorting(sort[0], Datasource.SORTORDER_ASC));
 					} else if ("desc".equals(sort[1].toLowerCase())) {
 						sortingColl.add(new Sorting(sort[0], Datasource.SORTORDER_DESC));
 					} else {
 						sortingColl.add(new Sorting(sort[0], Datasource.SORTORDER_NONE));
 					}
 				}
 			}
 		}
 
 		// convert hashmap to uncomfortable Array :-/ and return that
		return (Sorting[]) sortingColl.toArray(new Sorting[sortingColl.size()]);
 	}
 
 	/**
 	 * Convert a HTML-conform sorting request multivalue parameter array
 	 * to a Sorting[], suitable for the contentrepository queries.
 	 * 
 	 * @param sortingArray like ["contentid:asc","name:desc"]
 	 * @return a Sorting[] Array suitable for contentrepository queries
 	 */
 	public final static Sorting[] convertSorting(final String[] sortingArray) {
 		Sorting[] ret = new Sorting[0];
 		ArrayList<Sorting> sortingColl = new ArrayList<Sorting>();
 		if (sortingArray != null) {
 			int arrlen = sortingArray.length;
 
 			for (int i = 0; i < arrlen; i++) {
 
 				// split attribute on :. First element is attribute name the
 				// second is the direction
 				String[] sort = sortingArray[i].split(":");
 
 				if (sort[0] != null) {
 					if ("asc".equals(sort[1].toLowerCase())) {
 						sortingColl.add(new Sorting(sort[0], Datasource.SORTORDER_ASC));
 					} else if ("desc".equals(sort[1].toLowerCase())) {
 						sortingColl.add(new Sorting(sort[0], Datasource.SORTORDER_DESC));
 					} else {
 						sortingColl.add(new Sorting(sort[0], Datasource.SORTORDER_NONE));
 					}
 				}
 			}
 		}
		return (sortingColl.toArray(ret));
 	}
 
 	/**
 	 * Convert an array of strings to an array that can be usen in a filter rule.
 	 * @param arr
 	 * @return rule ready string
 	 */
 	public static String prepareParameterArrayForRule(final String[] arr) {
 		StringBuilder ret = new StringBuilder("[");
 		int arrlen = arr.length;
 		if (arrlen > 0) {
 			ret.append("'");
 			ret.append(arr[0]);
 			ret.append("'");
 			if (arrlen > 1) {
 				for (int i = 1; i < arrlen; i++) {
 					ret.append(",'");
 					ret.append(arr[i]);
 					ret.append("'");
 				}
 			}
 		}
 		ret.append("]");
 		return ret.toString();
 	}
 
 	/**
 	 * FIXME copied from {@link com.gentics.lib.etc.StringUtils}
 	 * - move it to API ?
 	 * Resolve system properties encoded in the string as ${property.name}
 	 * @param string string holding encoded system properties
 	 * @return string with the system properties resolved
 	 */
 	public static String resolveSystemProperties(final String string) {
 		// avoid NPE here
 		if (string == null) {
 			return null;
 		} else if (string.startsWith(DONOTRESOLVE_MARKER)) {
 			return string.replaceAll("^" + DONOTRESOLVE_MARKER, "");
 		}
 		//init com.gentics.portalnode.confpath if it isn't set
 		if (System.getProperty(PORTALNODE_CONFPATH) == null || System.getProperty(PORTALNODE_CONFPATH).equals("")) {
 			String defaultConfPath = System.getProperty("catalina.base") + File.separator + "conf" + File.separator + "gentics"
 					+ File.separator;
 			System.setProperty(PORTALNODE_CONFPATH, defaultConfPath);
		} else if (System.getProperty(PORTALNODE_CONFPATH).startsWith("file:/")) {
 			File confFile = null;
 			try {
 				confFile = new File(new URI(System.getProperty(PORTALNODE_CONFPATH)));
 			} catch (URISyntaxException e) {
 				LOGGER.error(
 					"Could not convert PORTALNODE_CONFPATH (" + System.getProperty(PORTALNODE_CONFPATH) + ") to an absolutePath",
 					e);
 			}
 			String confFilePath = confFile.getAbsolutePath();
 			System.setProperty(PORTALNODE_CONFPATH, confFilePath);
 		}
 		String result = CRUtilResolver.resolveSystemProperties(string);
 		result = CRUtilResolver.resolveContentConnectorProperties(result);
 		return result;
 	}
 
 	/**
 	 * Slurps a InputStream into a String.
 	 * @param in - TODO javadoc
 	 * @return TODO javadoc
 	 * @throws IOException TODO javadoc
 	 */
 	public static String inputStreamToString(final InputStream in) throws IOException {
 		StringBuffer out = new StringBuffer();
 		byte[] b = new byte[4096];
 		for (int n; (n = in.read(b)) != -1;) {
 			out.append(new String(b, 0, n));
 		}
 		return out.toString();
 	}
 
 	/**
 	 * Slurps a Reader into a String.
 	 * @param in
 	 * @return
 	 * @throws IOException
 	 */
 	public static String readerToString(final Reader in) throws IOException {
 		StringBuffer out = new StringBuffer();
 		char[] b = new char[4096];
 		for (int n; (n = in.read(b)) != -1;) {
 			out.append(new String(b, 0, n));
 		}
 		return out.toString();
 	}
 
 	/**
 	 * Slurps a Reader into a String and strips the character 0.
 	 * @param in
 	 * @return
 	 * @throws IOException
 	 */
 	public static String readerToPrintableCharString(final Reader in) throws IOException {
 		StringBuffer out = new StringBuffer();
 		int n;
 		while ((n = in.read()) != -1) {
 			if (n != 0) {
 				char c = (char) n;
 				out.append(c);
 			}
 		}
 		return out.toString();
 	}
 
	public static boolean isEmpty(final String s) {
		boolean isempty = false;

		if (s == null || s.length() == 0 || s.equals("")) {
			isempty = true;
		}

		return isempty;
 	}
 
 	public static File createTempDir() throws IOException {
 		final File sysTempDir = new File(System.getProperty("java.io.tmpdir"));
 		File newTempDir;
 		final int maxAttempts = 9;
 		int attemptCount = 0;
 		do {
 			attemptCount++;
 			if (attemptCount > maxAttempts) {
 				throw new IOException("The highly improbable has occurred! Failed to " + "create a unique temporary directory after "
 						+ maxAttempts + " attempts.");
 			}
 			String dirName = UUID.randomUUID().toString();
 			newTempDir = new File(sysTempDir, dirName);
 		} while (newTempDir.exists());
 
 		if (newTempDir.mkdirs()) {
 			return newTempDir;
 		} else {
 			throw new IOException("Failed to create temp dir named " + newTempDir.getAbsolutePath());
 		}
 	}
 }
