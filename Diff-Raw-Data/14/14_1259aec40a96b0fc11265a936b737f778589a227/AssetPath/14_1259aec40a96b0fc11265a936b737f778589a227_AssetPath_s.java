 package net.rcode.assetserver.core;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Class representing a match of a given incoming path against an AssetMount
  * with the path broken out into components.
  * 
  * <h2>Path Validity</h2>
  * There are a number of patterns that are not valid within a path component.  In general,
  * Windows meta-characters and special names are taken as the lowest common denominator for
  * determining validity.  See http://msdn.microsoft.com/en-us/library/aa365247(v=vs.85).aspx
  * All reserved characters and special names in a component are considered invalid.
  * 
  * <p>
  * The "." and ".." path components are also rejected.  Use higher level logic to convert
  * potentially relative paths to absolute paths.
  * 
  * @author stella
  *
  */
 public class AssetPath implements Cloneable {
 	static final String[] EMPTY_STRINGS=new String[0];
 	static final Pattern PATH_SPLIT_PATTERN=Pattern.compile("\\/");
 	/**
 	 * If a path component matches this pattern it is wholly invalid
 	 */
 	static final Pattern INVALID_COMPONENT=Pattern.compile("^(\\.\\.?)|CON|PRN|AUX|NUL|COM[0-9]|LPT[0-9]$", Pattern.CASE_INSENSITIVE);
 	static final Pattern INVALID_COMPONENT_SPANS=
 		Pattern.compile("[\\\\\\/\\:\"'\\<\\>\\|\\?\\*]|[\\x00-\\x1f]", Pattern.MULTILINE);
 	
 	/**
 	 * The pattern to extract the parameter string.  This must be applied to the
 	 * basename before url decoding of the component.  Group 1= param string,
 	 * Group 2=extension
 	 */
 	static final Pattern PARAMSTRING_PATTERN=
 		Pattern.compile("(?:\\$([^\\$]*)\\$)((?:\\.[A-Za-z0-9]+)+)$");
 	
 	static final Pattern EXTENSION_PATTERN=
 		Pattern.compile("((\\.[A-Za-z0-9]+)+)$");
 	
 	/**
 	 * The mount that this path is bound to
 	 */
 	private AssetMount mount;
 	
 	/**
 	 * The mount point (path prefix) of the owning mount.  This will always include the
 	 * leading, but not the trailing slash of the prefix unless if it is the root path,
 	 * in which case, it just the empty string.
 	 */
 	private String mountPoint;
 	
 	/**
 	 * The mount point broken up into components
 	 */
 	private String[] mountPointComponents;
 	
 	/**
 	 * The path within the given AssetMount.  This will always have a leading slash.
 	 */
 	private String path;
 	
 	/**
 	 * The components of the path, url decoded.  Never null.
 	 */
 	private String[] pathComponents;
 	
 	/**
 	 * The undecoded parameter string or null if none
 	 */
 	private String parameterString;
 	
 	/**
 	 * If null, then parameters have not yet been parsed.
 	 */
 	private Map<String, String> parameters;
 	
 	// Cache on first access
 	private String cacheFullPath;
 	
 	public AssetPath(AssetMount mount, String mountPoint, String path) throws IllegalArgumentException {
 		this.mount=mount;
 		this.mountPoint=mountPoint;
 		this.path=path;
 		this.mountPointComponents=parseComponents(mountPoint, false);
 		this.pathComponents=parseComponents(path, true);
 		
 	}
 	
 	private AssetPath copy() {
 		try {
			return (AssetPath) clone();
 		} catch (CloneNotSupportedException e) {
 			// Cannot happen
 			throw new RuntimeException(e);
 		}
 	}
 	
 	/**
 	 * Return an AssetPath representing this path plus one more directory level with the given
 	 * childName
 	 * @param childName
 	 * @return the new path or null if illegal
 	 */
 	public AssetPath createChild(String childName) {
 		if (!isValidComponent(childName)) return null;
 		
 		AssetPath ret=copy();
 		ret.path += '/' + childName;
 		ret.pathComponents=appendArray(ret.pathComponents, childName);
 		
 		return ret;
 	}
 	
 	/**
 	 * @return the canonical, reconstructed path including the mount point and path
 	 */
 	public String getFullPath() {
 		if (cacheFullPath==null) {
 			// Reassemble the path
 			StringBuilder pathBuilder=new StringBuilder((path!=null ? path.length():0) + (mountPoint==null ? 0 : mountPoint.length()) + 50);
 			joinPath(pathBuilder, mountPointComponents);
 			joinPath(pathBuilder, pathComponents);
 			cacheFullPath=pathBuilder.toString();
 		}
 		return cacheFullPath;
 	}
 	
 	/**
 	 * Get the full path including the parameter string.  Equivilent to getFullPath
 	 * if the parameterstring is null.  TODO: This method doesn't really do anything
 	 * @return path
 	 */
 	public String getFullParameterizedPath() {
 		return getFullPath();
 	}
 	
 	/**
 	 * Basename of the resource or null if none
 	 * @return basename
 	 */
 	public String getBaseName() {
 		if (pathComponents.length>0) return pathComponents[pathComponents.length-1];
 		return null;
 	}
 	
 	/**
 	 * Initializes the derived fields of this class
 	 */
 	private String[] parseComponents(String p, boolean scanForParameters) throws IllegalArgumentException {
 		if (p==null || p.isEmpty()) {
 			// Valid but empty path
 			return EMPTY_STRINGS;
 		}
 		
 		// Path must start with a slash
 		if (!p.startsWith("/")) {
 			throw new IllegalArgumentException("Path must start with a leading slash");
 		} else {
 			// Strip the leading slash off so as to split properly
 			p=p.substring(1);
 		}
 		
 		// But ending slashes, we just truncate
 		if (p.endsWith("/")) {
 			p=p.substring(0, p.length()-1);
 		}
 		
 		// Special case - empty string
 		if (p.isEmpty()) {
 			return EMPTY_STRINGS;
 		}
 		
 		// Split the components
 		String[] components=PATH_SPLIT_PATTERN.split(p);
 		
 		// Now iterate over each, normalize and validate
 		for (int i=0; i<components.length; i++) {
 			components[i]=normalizeComponent(components[i], scanForParameters && i==(components.length-1));
 		}
 		
 		return components;
 	}
 	
 	/**
 	 * Normalize a single path component.  Sets valid=false if not valid.  The component
 	 * is checked for syntactic validity in this step.
 	 * @param comp
 	 * @param baseName if true, then the parameter string is extracted and stored
 	 * @throws IllegalArgumentException if the component is not valid
 	 */
 	private String normalizeComponent(String comp, boolean scanForParameters) {
 		// Extract parameters
 		if (scanForParameters) {
 			Matcher paramMatcher=PARAMSTRING_PATTERN.matcher(comp);
 			if (paramMatcher.find()) {
 				this.parameterString=paramMatcher.group(1);
 				comp=comp.substring(0, paramMatcher.start()) +
 					paramMatcher.group(2);
 			}
 		}
 		
 		// URL decode
 		try {
 			comp=URLDecoder.decode(comp, "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			throw new RuntimeException(e);
 		}
 		
 		// After decoding, it cannot contain illegal values
 		if (!isValidComponent(comp)) {
 			throw new IllegalArgumentException("Illegal path component " + comp);
 		}
 		
 		return comp;
 	}
 
 	public AssetMount getMount() {
 		return mount;
 	}
 
 	public String getMountPoint() {
 		return mountPoint;
 	}
 
 	public String[] getMountPointComponents() {
 		return mountPointComponents;
 	}
 	
 	public String getPath() {
 		return path;
 	}
 
 	public String[] getPathComponents() {
 		return pathComponents;
 	}
 
 	public String getParameterString() {
 		return parameterString;
 	}
 
 	// Parameter access
 	public String getParameter(String name) {
 		initParameters();
 		return parameters.get(name);
 	}
 	
 	public String getParameter(String name, String dv) {
 		initParameters();
 		String ret=parameters.get(name);
 		if (ret==null) return dv;
 		else return ret;
 	}
 	
 	public Collection<String> getParameterNames() {
 		return Collections.unmodifiableCollection(parameters.keySet());
 	}
 	
 	private static final Pattern AMPSPLIT=Pattern.compile("\\&");
 	protected void initParameters() {
 		if (parameters!=null) return;
 		parameters=new HashMap<String, String>();
 		if (parameterString==null || parameterString.isEmpty()) return;
 		
 		String nvcmps[]=AMPSPLIT.split(parameterString);
 		for (String nv: nvcmps) {
 			String name, value;
 			int eqpos=nv.indexOf('=');
 			if (eqpos<0) {
 				name=nv;
 				value="";
 			} else {
 				name=nv.substring(0, eqpos);
 				value=nv.substring(eqpos+1);
 			}
 			
 			parameters.put(urlDecode(name), urlDecode(value));
 		}
 		
 		parameters=Collections.unmodifiableMap(parameters);
 	}
 
 	public Map<String, String> getParameters() {
 		initParameters();
 		return parameters;
 	}
 	
 	@Override
 	public String toString() {
 		return "AssetPath(mountPoint=" + mountPoint + ", path=" + path + ")";
 	}
 	
 	private static String urlDecode(String value) {
 		try {
 			return URLDecoder.decode(value, "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	/**
 	 * Adds components to a (possibly empty) StringBuilder, preserving the rules that
 	 * the path always starts with a slash and never ends with a slash.  The root path
 	 * is represented as an empty string.
 	 * @param dest
 	 * @param components
 	 * @throws IllegalArgumentException if the components contain illegal strings
 	 */
 	public static void joinPath(StringBuilder dest, String... components) throws IllegalArgumentException {
 		for (String component: components) {
 			if (component==null || component.isEmpty() || !isValidComponent(component)) {
 				throw new IllegalArgumentException("The path component '" + component + "' is not valid");
 			}
 			dest.append('/');
 			dest.append(component);
 		}
 	}
 	
 	/**
 	 * Return true if the given path component is syntactically legal
 	 * @param component
 	 * @return true if the given component is legal
 	 */
 	public static boolean isValidComponent(String component) {
 		// See if any of the invalid bits are found in the string
 		if (INVALID_COMPONENT_SPANS.matcher(component).find()) {
 			return false;
 		}
 
 		// See if the whole pattern matches any of the invalid patterns
 		if (INVALID_COMPONENT.matcher(component).matches()) {
 			return false;
 		}
 		
 		return true;
 	}
 	
 	private static String[] appendArray(String[] in, String newElt) {
 		String[] ret=new String[in.length+1];
 		for (int i=0; i<in.length; i++) {
 			ret[i]=in[i];
 		}
 		ret[in.length]=newElt;
 		return ret;
 	}
 }
