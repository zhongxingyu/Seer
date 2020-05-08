 package net.rcode.assetserver.util;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.regex.Pattern;
 
 /**
  * Some utility methods for manipulating paths.
  * 
  * @author stella
  *
  */
 public class PathUtil {
 	/**
 	 * Split pattern of "/"
 	 */
 	public static final Pattern PATH_SPLIT_PATTERN=Pattern.compile("\\/");
 	
 	/**
 	 * Empty array of strings
 	 */
 	public static final String[] EMPTY_STRINGS=new String[0];
 	
 	/**
 	 * Split the path by sep components (/), ignoring leading and trailing slashes.
 	 * Append the components to list.
 	 * @param path
 	 */
 	public static String[] splitPath(String path) {
 		path=trimSlashes(path);
 		if (path.isEmpty()) return EMPTY_STRINGS;
 		
 		return PATH_SPLIT_PATTERN.split(path);
 	}
 	
 	/**
 	 * Trim leading and trailing slashes from the string, returning the altered string
 	 * @param path
 	 * @return trimmed path
 	 */
 	public static String trimSlashes(String path) {
 		if (path.startsWith("/")) path=path.substring(1);
 		if (path.endsWith("/")) path=path.substring(0, path.length()-1);
 		return path;
 	}
 
 	/**
 	 * If path is not absolute (does not start with a leading /), then compute
 	 * it relative to rootPath.  In all cases, "." and ".." path segments are
 	 * expanded in path (but not rootPath).
 	 * 
 	 * @param relativeTo
 	 * @param path
 	 * @return normalized path or null if the path is not valid
 	 */
 	public static String normalizePath(String relativeTo, String path) {
 		String[] components=splitPath(path);
 		ArrayList<String> normalizedComponents=new ArrayList<String>(components.length+10);
 		if (!path.startsWith("/")) {
 			// It is not absolute.  Append the root components.
 			normalizedComponents.addAll(Arrays.asList(splitPath(relativeTo)));
 		}
 		
 		// Split the path and process each component
 		for (String component: components) {
 			if (component.isEmpty()) {
 				// Just a duplicate slash which happens in sloppy string manipulation.  Ignore
 				continue;
 			}
 			
 			if (".".equals(component)) {
 				// Reference to the current component.  Continue
 				continue;
 			}
 			
 			if ("..".equals(component)) {
 				// Step up one level
 				if (normalizedComponents.isEmpty()) {
 					// Proceeded up past the root
 					return null;
 				} else {
 					// Pop the last one
 					normalizedComponents.remove(normalizedComponents.size()-1);
 					continue;
 				}
 			}
 			
 			// It's just a normal component
 			normalizedComponents.add(component);
 		}
 		
 		// Join it back together and return
 		return joinPath(normalizedComponents);
 	}
 	
 	/**
 	 * Takes a list of components and returns a joined path.  If the list is empty,
 	 * return "/".  The path will always begin with a slash and never end with one
 	 * unless if it is the root path.
 	 * 
 	 * @param components
 	 * @return joined path
 	 */
 	public static String joinPath(List<String> components) {
 		if (components.isEmpty()) return "/";
 		
 		StringBuilder builder=new StringBuilder();
 		for (String component: components) {
 			builder.append('/');
 			builder.append(component);
 		}
 		
 		return builder.toString();
 	}
 	
 	/**
 	 * Vararg version of joinPath
 	 * @param components
 	 * @return joined path
 	 */
 	public static String joinPath(String... components) {
 		return joinPath(Arrays.asList(components));
 	}
	
	/**
	 * Return the dirname of the path.  This basically just extracts the substring
	 * from the beginning to the last slash (including the last slash) or '/'
	 * if no slash.
	 * @param path
	 * @return dirname
	 */
	public static String dirname(String path) {
		int slashPos=path.lastIndexOf('/');
		if (slashPos<0) return "/";
		return path.substring(0, slashPos+1);
	}
 }
