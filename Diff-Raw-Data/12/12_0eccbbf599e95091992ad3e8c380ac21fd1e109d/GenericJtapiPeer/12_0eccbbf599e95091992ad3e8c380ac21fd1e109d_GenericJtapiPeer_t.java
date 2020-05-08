 package net.sourceforge.gjtapi;
 
 /*
 	Copyright (c) 2002 8x8 Inc. (www.8x8.com) 
 
 	All rights reserved. 
 
 	Permission is hereby granted, free of charge, to any person obtaining a 
 	copy of this software and associated documentation files (the 
 	"Software"), to deal in the Software without restriction, including 
 	without limitation the rights to use, copy, modify, merge, publish, 
 	distribute, and/or sell copies of the Software, and to permit persons 
 	to whom the Software is furnished to do so, provided that the above 
 	copyright notice(s) and this permission notice appear in all copies of 
 	the Software and that both the above copyright notice(s) and this 
 	permission notice appear in supporting documentation. 
 
 	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS 
 	OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
 	MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT 
 	OF THIRD PARTY RIGHTS. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR 
 	HOLDERS INCLUDED IN THIS NOTICE BE LIABLE FOR ANY CLAIM, OR ANY SPECIAL 
 	INDIRECT OR CONSEQUENTIAL DAMAGES, OR ANY DAMAGES WHATSOEVER RESULTING 
 	FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, 
 	NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION 
 	WITH THE USE OR PERFORMANCE OF THIS SOFTWARE. 
 
 	Except as contained in this notice, the name of a copyright holder 
 	shall not be used in advertising or otherwise to promote the sale, use 
 	or other dealings in this Software without prior written authorization 
 	of the copyright holder.
 */
 
 /**
  * This is the GJTAPI implementation of the JTAPI peer.  It's main capabilities
  * are its abilities to plug in the appropriate low-level proxy (delegate)
  */
 
 import java.util.*;
 import java.io.*;
 import java.net.URL;
 
 import javax.telephony.*;
 
 import net.sourceforge.gjtapi.raw.*;
 
 public class GenericJtapiPeer implements JtapiPeer {
 	// dictionary of raw providers
 	private final static String RESOURCE_NAME = "GenericResources.props";
 	private final static String DEFAULT_PROVIDER = "DefaultProvider";
 	private final static String PROV_PREFIX = "PROVIDER_";
 	private final static String PROV_CLASS_KEY = "ProviderClass";
 	private final static String PROV_PROP_SUFFIX = ".gjtapi";
 	private final static String PROV_AUTOPROPERTY = "provider.gjtapi";
 	
 	// key for the property for disconnecting a Connection when a MediaService releases
 	private final String MEDIA_RELEASE_DISCONNECT = "mediaReleaseDisconnect";
 	
 	// System property key for looking up resource directory
 	private final static String RESOURCE_DIR = "net.sourceforge.gjtapi.resourceDir";
 
 	private Properties properties = null;
 	private Hashtable providers = new Hashtable();
 	
 	// Should we disconnect the Connection when the MediaService releases (JTAPI spec. says yes)
 	private boolean disconnectMediaOnRelease = true;
 	
 	/**
 	 * Create a version of the Peer and load the provider's Resource Bundle
 	 **/
 	public GenericJtapiPeer() {
 		super();
 		
 		// read generic peers from my property file
 		this.loadResources();
 	}
 /**
  * If no provider class name is passed to the JtapiPeer.getProvider() method, return the
  * default provider.
  *<P>Subclases may override this to provide their own default provider lookup mechanism.
  * Creation date: (2000-04-05 10:01:48)
  * @author: Richard Deadman
  * @return The class name of the RawProvider implementor class to hook in if no class name
  * or provider lookup name is passed to the JtapiPeer.getProvider() method.
  */
 protected String getDefaultProvider() {
 	return (String) this.getProperties().get(GenericJtapiPeer.DEFAULT_PROVIDER);
 }
 /**
  * Get the fully qualified class name for the Peer
  **/
 public String getName() {
 	return this.getClass().getName();
 }
 /**
  * Return a Property set for the Peer that maps provider names to class names and
  * details the default provider.
  * Creation date: (2000-02-04 10:29:21)
  * @author: 
  * @return java.util.Properties
  */
 private java.util.Properties getProperties() {
 	return properties;
 }
  /**
    * Returns an instance of a <CODE>Provider</CODE> object given a string 
    * argument which
    * contains the desired service name. Optional arguments may also be
    * provided in this string, with the following format:
    * <p>
    * &lt service name &gt ; arg1 = val1; arg2 = val2; ...
    * <p>
    * Where &lt service name &gt is not optional, and each optional argument
    * pair which follows is separated by a semi-colon. The keys for these
    * arguments is implementation specific, except for two standard-defined
    * keys:
    * <OL>
    * <LI>login: provides the login user name to the Provider.
    * <LI>passwd: provides a password to the Provider.
    * </OL>
    * <p>
    * If the argument is null, this method returns some default Provider as
    * determined by the JtapiPeer object. The returned Provider is in the
    * <CODE>Provider.OUT_OF_SERVICE</CODE> state.
    * <p>
    * The service name may be one of:
    * <ol>
    * <li>null to load the default provider indicated in the GenericResources.props file.
    * <li>A service name that is mapped in the GenericResources.props file to a class file.
    * <li>A service name that is mapped in the GenericResources.props file to a service properties file.
    * <li>A class name for a class that implements TelephonyProvider.
    * </ol>
    * <B>Post-conditions:</B>
    * <OL>
    * <LI>this.getProvider().getState() == Provider.OUT_OF_SERVICE
    * </OL>
    * @param providerString The name of the desired service plus an optional
    * arguments.  The provider name parsed from string may be looked up straight or used as a class itself.
    * @return An instance of the Provider object.
    * @exception ProviderUnavailableException Indicates a Provider corresponding
    * to the given string is unavailable.
    */
 public Provider getProvider(String params) throws ProviderUnavailableException {
 	// parse the parameters
 	String[] parts = this.split(params);
 	String provName = null;
 	if (parts.length > 0)
 		provName = this.parseService(parts[0]);
 
 	// If providerName is null, look for the default
 	if (provName == null) {
 		provName = this.getDefaultProvider();
 	}
 
 	// look up provider in loaded Properties
 	Object providerHandle = this.getProviders().get(provName);
 	InputStream is = null;
 	String providerFileName = null;
 	if (providerHandle instanceof URL) {
 		try {
 			is = ((URL)providerHandle).openStream();
 		} catch (IOException ioe) {
 			ProviderUnavailableException pue = new ProviderUnavailableException(ProviderUnavailableException.CAUSE_UNKNOWN, "Could not open resource: " + providerHandle);
 			pue.setStackTrace(ioe.getStackTrace());
 			throw pue;
 		}
 	} else {
 		providerFileName = (String)providerHandle;
 		// if the name is not in the resources list
 		if (providerFileName == null)
 			providerFileName = provName;
 		
 		is = this.findResource(providerFileName);
 		if (is == null) {
 			// try to load from the file system
 			File file = new File(providerFileName);
 			if(file.exists() && file.canRead() && file.isFile()) {
 				try {
 					is = new FileInputStream(file);
 				} catch (FileNotFoundException fnfe) {
 					// fall through
 				}
 			}
 		}
 	}
 	String providerClassName = null;
 
 	// See if this is a Property file or a call name
 	Properties provProps = new Properties();
 	if (is != null) {
 		try {
 			provProps.load(is);
 			if (provProps.containsKey(GenericJtapiPeer.PROV_CLASS_KEY))
 				providerClassName = (String)provProps.get(GenericJtapiPeer.PROV_CLASS_KEY);
 		} catch (IOException ioe) {
 			// No provider property file by that name, assume it is a class
 			is = null;
 		}
 	} else {
 		providerClassName = providerFileName;
 	}
 
 	// test if we've located the provider class
 	if (providerClassName == null)
 			// try using the name from the provider string
 		providerClassName = provName;
 
 	// parse the remaining name-value pairs and add to the provider properties set
 	provProps.putAll(this.parse(parts, 1));
 	
 	// load raw provider
 	TelephonyProvider rp = null;
 	try {
 		rp = ProviderFactory.createProvider((CoreTpi) Class.forName(providerClassName).newInstance());
 	} catch (Exception e) {
 		throw new ProviderUnavailableException("Error loading raw provider: " + providerClassName);
 	}
 	// initialize
 	rp.initialize(provProps);
 
 	// Create the high-level provider and return it
 	GenericProvider gp = new GenericProvider(provName, rp, provProps);
 	gp.setDisconnectOnMediaRelease(this.disconnectMediaOnRelease);
 	return gp;
 }
 /**
  * Return the dictionary of internally known raw providers
  * Creation date: (2000-02-11 11:12:15)
  * @author: Richard Deadman
  * @return java.util.Hashtable
  */
 private java.util.Hashtable getProviders() {
 	return providers;
 }
 /**
  * Return the list of known raw provider names
  * 
  * @return An array or known provider names.
  **/
 public String[] getServices() {
 		return (String[])this.getProviders().keySet().toArray(new String[0]);
 	}
 /**
  * This method loads the Peer's initial values, including names of RawProvider classes.
  * Creation date: (2000-02-04 10:11:41)
  * @author: Richard Deadman
  */
 private void loadResources() {
 	// If this fails, the Properties object will be left null, which will signal getProvider() to throw
 	// an exception.
 	Properties props = new Properties();
 	try {
 		props.load(this.findResource(GenericJtapiPeer.RESOURCE_NAME));
 		this.setProperties(props);
 	} catch (IOException ioe) {
 		// don't set properties then...
 		return;
 	}
 
 	// now look for providers and move them to the providers dictionary
 	Hashtable provs = this.getProviders();
 	Enumeration e = props.keys();
 	while (e.hasMoreElements()) {
 		String key = (String)e.nextElement();
 		if (key.startsWith(GenericJtapiPeer.PROV_PREFIX)) {
 			String name = key.substring(GenericJtapiPeer.PROV_PREFIX.length());
 			provs.put(name, props.get(key));
 		}
 
 			// now see if we should change the MediaService.release() behaviour
 		if (key.equals(this.MEDIA_RELEASE_DISCONNECT)) {
 			String disconnect = (String)props.get(key);
 			if (disconnect != null && disconnect.length() > 0 && Character.toLowerCase(disconnect.charAt(0)) != 't')
 				this.disconnectMediaOnRelease = false;
 		}
 	}
 	
 	// Now look for any providers property files in the resource directory with the right suffix
 	// or in the jar files
 	this.findAutoProviders(provs);
 	
 }
 /**
  * Parse a set of name-value pairs into a dictionary.
  * So if a String equals "a = b", "a" is the key and "b" the value.
  *
  * @param parts The array of name-value pairs
  * @param index The array element to start parsing at
  * @return A dictionary with the parse values
  **/
 private Map parse(String[] parts, int index) {
 	Hashtable tab = new Hashtable();
 	for (int i = index; i < parts.length; i++) {
 		StringTokenizer tok = new StringTokenizer(parts[i], "=");
 		if (tok.countTokens() > 1) {
 			tab.put(tok.nextToken().trim(), tok.nextToken().trim());
 		}
 	}
 	return tab;
 }
 /**
  * Parse the service name away from the angle brackets, if they exist.
  * This is to clean up any mis-reading of the JTAPI spec.
  **/
 String parseService(String args) {
 	String ret = args;
 	int start = args.indexOf('<');
 	if (start >= 0) {
 		int end = args.indexOf('>', start);
 		if (end > start) {
 			ret = args.substring(start + 1, end);
 		}
 	}
 	return ret.trim();
 }
 /**
  * Set the properties file
  * Creation date: (2000-02-04 10:29:21)
  * @author: Richard Deadman
  * @param newProperties The Properties object containing Provider names and other initializations
  */
 private void setProperties(java.util.Properties newProperties) {
 	properties = newProperties;
 }
 /**
  * Split a parameter string into its parts
  **/
 private String[] split(String line) {
 	if (line == null)
 		return new String[0];
 	StringTokenizer tok = new StringTokenizer(line, ";");
 	String ret[] = new String[tok.countTokens()];
 	for (int i = 0; i < ret.length; i++) {
 		ret[i] = tok.nextToken().trim();
 	}
 	return ret;
 }
 
 /**
  * Find a resource. All resources used to be only looked up on the
  * classpath in the base "package", but this method refactores the
  * search so that it can also use an environment variable.
  * <P>The algorithm looks for the named resource
  * <ol>
  * <li>In the directory specified by the net.sourceforge.gjtapi.resourceDir
  * <li>In the application's current working directory
  * <li>In the classloader base package.
  * </ul>
  * @param resourceName The name of the resource that we want to find
  * @return An InputStream for reading the resource, or null if none is found
  * @author rdeadman
  *
  */
 private InputStream findResource(String resourceName) {
 	// first we see if we should check for a resource directory
 	String resourceDir = System.getProperty(RESOURCE_DIR);
 	if (resourceDir != null) {
 		File resource = new File(resourceDir + File.separator + resourceName);
 		if (resource.exists() && resource.isFile()) {
 			try {
 				return new FileInputStream(resource);
 			} catch (FileNotFoundException fnfe) {
 				// should never get here unless the resource is not readable -- let the class loader look for it then
 			}
 		}
 	}
 	// it wasn't on the Resource_Dir path, let's check the current working directory
 	File resource = new File(System.getProperty("user.dir") + File.separator + resourceName);
 	if (resource.exists() && resource.isFile()) {
 	try {
 		return new FileInputStream(resource);
 	} catch (FileNotFoundException fnfe) {
 		// should never get here unless the resource is not readable -- let the class loader look for it then
 		}
 	}
 
 	// we didn't find the resource in the resource directory or the working directory
 	// now let's check the classpath
 	return this.getClass().getResourceAsStream("/" + resourceName);
 }
 
 /**
  * Find providers in the resource directories based on the naming suffix
  * @param providers The name-class lookup table
  */
 private void findAutoProviders(Hashtable providers) {
 	// load all resource files ending in .gjtapi on the resource or user directory
 	
 	// first we see if we should check for a resource directory
 	HashSet directories = new HashSet();
 	String resourceDir = System.getProperty(RESOURCE_DIR);
 	if (resourceDir != null) {
 		File resourceDirectory = new File(resourceDir);
 		if (resourceDirectory.exists() && resourceDirectory.isDirectory()) {
 			directories.add(resourceDirectory);
 		}
 	}
 	
 	// now add the home directory
 	File userDirectory = new File(System.getProperty("user.dir"));
 	if (userDirectory.exists() && userDirectory.isDirectory()) {
 		directories.add(userDirectory);
 	}
 	
 	Iterator it = directories.iterator();
 	while(it.hasNext()) {
 		File resourceDirectory = (File)it.next();
 		// find all the auto-load files
 		File[] resources = resourceDirectory.listFiles(new FilenameFilter() {
 			public boolean accept(File dir, String name) {
 				return name.endsWith(PROV_PROP_SUFFIX);
 			}
 		});
 		// load each file
 		for(int i = 0; i<resources.length; i++) {
 			try {
 				File resource = resources[i];
 				String fullName = resource.getName();
 				String name = fullName.substring(0, fullName.length()-PROV_PROP_SUFFIX.length());
 				// only add if name is unique
 				if (!providers.containsKey(name)) {
 					providers.put(name, resource.getCanonicalPath());
 				}
 			} catch (IOException ioe) {
 				// no-op
 			}
 		}
 	}
 
 	// Now look for provider.gjtapi classes on the classpath
 	try {
 		Enumeration cpResources = ClassLoader.getSystemResources(PROV_AUTOPROPERTY);
 		while(cpResources.hasMoreElements()) {
 			URL cpResource = (URL)cpResources.nextElement();
 			// find the name as the jar part of the resource
 			String jarPath = cpResource.getPath();
 			if(jarPath != null) {
 				// check if we have a jar file or a local file
 				int tailLocation = jarPath.lastIndexOf(".jar!/" + PROV_AUTOPROPERTY);
 				if(tailLocation != -1) {
 					jarPath = jarPath.substring(0, tailLocation+4);
 					// find the name of the jar file
						// test for fully qualified name
 					tailLocation = jarPath.lastIndexOf('/');
					if(tailLocation == -1) {
						// or relative name
						tailLocation = jarPath.lastIndexOf(':');
					}
 					if(tailLocation != -1) {
 						jarPath = jarPath.substring(tailLocation+1);
 					}
 				}
 				else {
 					// find the suffix of the file
 					tailLocation = jarPath.lastIndexOf('/');
 					if(tailLocation != -1) {
 						jarPath = jarPath.substring(tailLocation+1);
 						tailLocation = jarPath.lastIndexOf(PROV_PROP_SUFFIX);
 						if(tailLocation != -1) {
 							jarPath = jarPath.substring(0, jarPath.length()-PROV_PROP_SUFFIX.length());
 						}
 					}
 				}
 			}
 			
 			// Now add if new
 			if ((jarPath != null) && (!providers.containsKey(jarPath))) {
 				providers.put(jarPath, cpResource);
 			}
 		}
 	} catch (IOException ioe) {
 		// no-op
 	}
 }
 
 }
