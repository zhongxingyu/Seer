 package edu.unc.lib.staging;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URLEncoder;
 import java.util.StringTokenizer;
 import java.util.regex.Pattern;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public abstract class URIPattern {
 	Logger log = LoggerFactory.getLogger(URIPattern.class);
 
 	public boolean isAutoconnected() {
 		return false;
 	}
 
 	public boolean isLocallyMapped() {
 		return false;
 	}
 
 	/**
 	 * Builds a single URL encode path from a set of path strings. Retains all
 	 * slashes.
 	 * 
 	 * @param path
 	 * @return
 	 */
 	public static String encodePath(String... pathArray) {
 		StringBuilder encodedPath = new StringBuilder();
 		if (pathArray[0] != null && pathArray[0].startsWith("/"))
 			encodedPath.append("/");
 		for (String path : pathArray) {
 			if(path == null) continue;
 			String sofar = encodedPath.toString();
 			if(sofar.length() > 0 && !sofar.endsWith("/")) encodedPath.append("/");
 			try {
 				StringTokenizer st = new StringTokenizer(path, "/");
 				while (st.hasMoreTokens()) {
 					encodedPath.append(URLEncoder.encode(st.nextToken(),
 							"utf-8").replaceAll(Pattern.quote("+"), "%20"));
 					if (st.hasMoreTokens())
 						encodedPath.append("/");
 				}
 			} catch (UnsupportedEncodingException e) {
 				throw new Error(e);
 			}
 		}
 		if (pathArray[pathArray.length-1].endsWith("/"))
 			encodedPath.append("/");
 		return encodedPath.toString();
 	}
 
 	/**
 	 * Get the path part of this URI
 	 * 
 	 * @param uri
 	 * @return
 	 */
 	public abstract String getPath(URI uri);
 
 	/**
 	 * Does this file URI reside within the given base URI.
 	 * 
 	 * @param baseURI
 	 * @param fileURI
 	 * @return true if file is within base URI
 	 */
 	public abstract boolean isWithin(URI baseURI, URI fileURI);
 
 	/**
 	 * Creates a URI for the relative path within the given base URI.
 	 * 
 	 * @param baseURI
 	 * @param pathParts
 	 * @return
 	 */
 	public abstract URI makeURI(URI baseURI, String... pathParts);
 
 	/**
 	 * Gets the relative path between the given base URI and the file URI.
 	 * 
 	 * @param baseURI
 	 * @param fileURI
 	 * @return
 	 */
 	public final String getRelativePath(URI baseURI, URI fileURI) {
		log.debug("baseURI: "+baseURI+"; fileURI: "+fileURI);
 		String filePath = getPath(fileURI);
 		String basePath = getPath(baseURI);
		log.debug("basePath: "+basePath+"; filePath: "+filePath);
 		return filePath.substring(basePath.length());
 	}
 
 	/**
 	 * Does the given URI match this pattern
 	 * 
 	 * @param uri
 	 * @return true if this URI is a match
 	 */
 	public abstract boolean matches(URI uri);
 
 	public abstract String getScheme();
 
 }
