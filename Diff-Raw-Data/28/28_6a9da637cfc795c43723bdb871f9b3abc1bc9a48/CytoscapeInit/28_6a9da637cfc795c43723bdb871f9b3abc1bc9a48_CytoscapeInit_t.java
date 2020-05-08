 /*
  File: CytoscapeInit.java 
  
  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)
  
  The Cytoscape Consortium is: 
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Pasteur Institute
  - Agilent Technologies
  
  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.
  
  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute 
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute 
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute 
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.
  
  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
  */
 
 package cytoscape;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.net.JarURLConnection;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 import java.util.jar.JarFile;
 
 import javax.swing.ImageIcon;
 
 import cytoscape.data.servers.BioDataServer;
 import cytoscape.init.CyPropertiesReader;
 import cytoscape.init.CyInitParams;
 import cytoscape.init.CyCommandLineParser;
 import cytoscape.plugin.AbstractPlugin;
 import cytoscape.plugin.CytoscapePlugin;
 import cytoscape.util.shadegrown.WindowUtilities;
 import cytoscape.view.CytoscapeDesktop;
 
 /**
  * Cytoscape Init is responsible for starting Cytoscape in a way that makes
  * sense.
  * 
  * The comments below are more hopeful than accurate. We currently do not
  * support a "headless" mode (meaning there is no GUI). We do, however, hope to
  * support this in the future.
  * 
  * 
  * The two main modes of running Cytoscape are either in "headless" mode or in
  * "script" mode. This class will use the command-line options to figure out
  * which mode is desired, and run things accordingly.
  * 
  * The order for doing things will be the following: 1. deterimine script mode,
  * or headless mode 2. get options from properties files 3. get options from
  * command line ( these overwrite properties ) 4. Load all Networks 5. Load all
  * Data 6. Load all Plugins 7. Initialize all plugins, in order if specified. 8.
  * Start Desktop/ Print Output exit.
  */
 public class CytoscapeInit implements PropertyChangeListener {
 
 	private static String[] args;
 
	private static CyInitParams initParams;

 	private static Properties properties;
 
 	private static String propertiesLocation;
 
 	private static URLClassLoader classLoader;
 
 	// Most-Recently-Used directories and files
 	private static File mrud;
 
 	private static File mruf;
 
 	private static Set pluginURLs;
 
 	// Data variables
 	private static String bioDataServer;
 
 	private static boolean noCanonicalization;
 
 	private static Set expressionFiles;
 
 	private static Set graphFiles;
 
 	private static Set edgeAttributes;
 
 	private static Set nodeAttributes;
 
 	private static String defaultSpeciesName;
 
 	// Configuration variables
 	private static boolean useView = true;
 
 	private static boolean suppressView = false;
 
 	private static String viewType = "tabbed";
 
 	private static int viewThreshold;
 
 	private static int secondaryViewThreshold;
 
 	// View Only Variables
 	private static String vizmapPropertiesLocation;
 
 	private static String defaultVisualStyle = "default";
 
 	// project parsing
 	private static final String fWHITESPACE_AND_QUOTES = " \t\r\n\"";
 
 	private static final String fQUOTES_ONLY = "\"";
 
 	private static final String fDOUBLE_QUOTE = "\"";
 
 	private static String specifiedVizProps = "";
 
 	/**
 	 * Calling the constructor sets up the CytoscapeInit Object to be a
 	 * CYTOSCAPE_EXIT event listener, and will take care of saving all
 	 * properties.
 	 */
 	public CytoscapeInit() {
 		Cytoscape.getSwingPropertyChangeSupport().addPropertyChangeListener(
 				this);
 
 	}
 
 	public void propertyChange(PropertyChangeEvent e) {
 		if (e.getPropertyName() == Cytoscape.CYTOSCAPE_EXIT) {
 			try {
 				File file = new File(propertiesLocation);
 				FileOutputStream output = new FileOutputStream(file);
 				properties.store(output, "Cytoscape Property File");
 
 			} catch (Exception ex) {
 				System.out.println("Cytoscape.Props Write error");
 				ex.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Cytoscape Init must be initialized using the command line arguments.
 	 * 
 	 * @param args
 	 *            the arguments from the command line
 	 * @return false, if we should stop initing, since help has been requested
 	 */
 	public boolean init(CyInitParams params) {
 
		initParams = params;
 		bioDataServer = null;
 		noCanonicalization = false;
 		expressionFiles = new HashSet();
 		graphFiles = new HashSet();
 		edgeAttributes = new HashSet();
 		nodeAttributes = new HashSet();
 		pluginURLs = new HashSet();
 
 		// Properties from cytoscape.props
 		// read in properties in cytoscape.props, and assign variables from them
 		CyPropertiesReader propReader = new CyPropertiesReader();
 		// getSpecifiedPropsFile returns the location of cytoscape.props
 		propReader.readProperties(params.getPropsFile());
 		properties = propReader.getProperties();
 		propertiesLocation = propReader.getPropertiesLocation();
 		setVariablesFromProperties();
 
 		// Overwrites properties set from cytoscape.props
 		setVariablesFromInitParams(params);
 
 		// set the vizmap.props file 
 		specifiedVizProps = params.getVizPropsFile();
 
 		// store key property values into main Properties object for
 		// for visual (via Preferences Dialog) communication and later storage
 		//
 		// command line -overrides- cytoscape.props
 		// hence it is now safe to store those values
 		storeVariablesInProperties();
 
 		// see if we are in headless mode
 		// show splash screen, if appropriate
                 System.out.println("init mode: " + params.getMode() );
                 if ( params.getMode() == CyInitParams.GUI ) {
 
 			ImageIcon image = new ImageIcon(getClass().getResource(
 					"/cytoscape/images/CytoscapeSplashScreen.png"));
 			WindowUtilities.showSplash(image, 8000);
 			Cytoscape.getDesktop();
 			// setup CytoPanels menu -
 			// This cannot be done in CytoscapeDesktop construction (like the other menus)
 			// because we need CytoscapeDesktop created first. This is because CytoPanel
 			// menu item listeners need to register for CytoPanel events via a CytoPanel reference,
 			// and the only way to get a CytoPanel reference is via CytoscapeDeskop:
 			// Cytoscape.getDesktop().getCytoPanel(...)
 			Cytoscape.getDesktop().getCyMenus().initCytoPanelMenus();
 
 			// Add a listener that will apply vizmaps every time attributes change
 			PropertyChangeListener attsChangeListener = new PropertyChangeListener() {
 
 				public void propertyChange(PropertyChangeEvent e) {
 					if (e.getPropertyName().equals(Cytoscape.ATTRIBUTES_CHANGED)) {
 						// apply vizmaps
 						Cytoscape.getCurrentNetworkView().redrawGraph(false, true); 
 					}
 				}
 			};
 
 			Cytoscape.getSwingPropertyChangeSupport().addPropertyChangeListener(attsChangeListener);
 		}
 
 		// now that we are properly set up,
 		// load all data, then load all plugins
 
 		// Load the BioDataServer(s)
 		BioDataServer bds = Cytoscape.loadBioDataServer(getBioDataServer());
 
 		// Load all requested networks
 		for (Iterator i = graphFiles.iterator(); i.hasNext();) {
 			String net = (String) i.next();
 			System.out.println("Load: " + net);
 
 			CyNetwork network = Cytoscape.createNetwork(net,
 					Cytoscape.FILE_BY_SUFFIX, !noCanonicalization(), bds,
 					getDefaultSpeciesName());
 		}
 
 		// load any specified data attribute files
 		Cytoscape.loadAttributes((String[]) getNodeAttributes().toArray(
 				new String[] {}), (String[]) getEdgeAttributes().toArray(
 				new String[] {}), !noCanonicalization(), bds,
 				getDefaultSpeciesName());
 
 
 		Cytoscape.firePropertyChange(Cytoscape.ATTRIBUTES_CHANGED, null, null);
 
 		// load expression data if specified
 		if (getExpressionFiles().size() > 0) {
 			for (Iterator iter = expressionFiles.iterator(); iter.hasNext();) {
 				String expDataFilename = (String) iter.next();
 				if (expDataFilename != null) {
 					try {
 						Cytoscape.loadExpressionData(expDataFilename, true);
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 
 		if ( params.getMode() == CyInitParams.GUI ) {
 
 			loadPlugins(pluginURLs);
 
 			// attempt to load resource plugins
 			List rp = params.getResourcePlugins();
 			for (Iterator rpi = rp.iterator(); rpi.hasNext();) {
 				String resource = (String) rpi.next();
 				// try to get the class
 				Class rclass = null;
 				try {
 					rclass = Class.forName(resource);
 				} catch (Exception exc) {
 					System.out.println("Getting class: " + resource + " failed");
 					exc.printStackTrace();
 				}
 				loadPlugin(rclass);
 			}
 
 			WindowUtilities.hideSplash();
 		}
 
 		// This is for browser and other plugins.
 		Cytoscape.firePropertyChange(Cytoscape.NETWORK_LOADED, null, null);
 
 		System.out.println("Cytoscape initialized successfully.");
 		return true;
 	}
 
 	// store value of key variables in Properties object
 	// for access in PreferencesDialog and eventual saving on exit
 	private void storeVariablesInProperties() {
 		if (bioDataServer != null)
 			properties.setProperty("bioDataServer", bioDataServer);
 		if (defaultSpeciesName != null)
 			properties.setProperty("defaultSpeciesName", defaultSpeciesName);
 		properties.setProperty("viewThreshold", "" + viewThreshold);
 		properties.setProperty("secondaryViewThreshold", ""
 				+ secondaryViewThreshold);
 		// properties.setProperty("vizmapPropertiesLocation",vizmapPropertiesLocation);
 	}
 
 	public String getHelp() {
 		return CyCommandLineParser.getHelp();
 	}
 
 	public static boolean isHeadless() {
 		return !useView;
 	}
 
 	public static boolean useView() {
 		return useView;
 	}
 
 	public static boolean suppressView() {
 		return suppressView;
 	}
 
 	private boolean isDoubleQuote(String aToken) {
 		return aToken.equals(fDOUBLE_QUOTE);
 	}
 
 	private String flipDelimiters(String aCurrentDelims) {
 		String result = null;
 		if (aCurrentDelims.equals(fWHITESPACE_AND_QUOTES)) {
 			result = fQUOTES_ONLY;
 		} else {
 			result = fWHITESPACE_AND_QUOTES;
 		}
 		return result;
 	}
 
 	public static String[] getArgs() {
		return initParams.getArgs();
 	}
 
 	public static Properties getProperties() {
 		return properties;
 	}
 
 	public static String getPropertiesLocation() {
 		return propertiesLocation;
 	}
 
 	public static URLClassLoader getClassLoader() {
 		return classLoader;
 	}
 
 	public static Set getPluginURLs() {
 		return pluginURLs;
 	}
 
 	// Data variables
 	public static String getBioDataServer() {
 		return bioDataServer;
 	}
 
 	public static boolean noCanonicalization() {
 		return noCanonicalization;
 	}
 
 	public static Set getExpressionFiles() {
 		return expressionFiles;
 	}
 
 	public static Set getGraphFiles() {
 		return graphFiles;
 	}
 
 	public static Set getEdgeAttributes() {
 		return edgeAttributes;
 	}
 
 	public static Set getNodeAttributes() {
 		return nodeAttributes;
 	}
 
 	public static String getDefaultSpeciesName() {
 		return defaultSpeciesName;
 	}
 
 	// Configuration variables
 
 	public static int getViewType() {
 		if (viewType == "internal") {
 			return CytoscapeDesktop.INTERNAL_VIEW;
 		} else if ((viewType == "external")) {
 			return CytoscapeDesktop.EXTERNAL_VIEW;
 		} else {
 			return CytoscapeDesktop.TABBED_VIEW;
 		}
 	}
 
 	/**
 	 * Gets the ViewThreshold. Networks with number of nodes below this
 	 * threshold will automatically have network views created.
 	 * 
 	 * @return view threshold.
 	 */
 	public static int getViewThreshold() {
 		return viewThreshold;
 	}
 
 	/**
 	 * Sets the ViewThreshold. Networks with number of nodes below this
 	 * threshold will automatically have network views created.
 	 * 
 	 * @param threshold
 	 *            view threshold.
 	 */
 	public static void setViewThreshold(int threshold) {
 		viewThreshold = threshold;
 	}
 
 	/**
 	 * Gets the Secondary View Threshold. This value is a secondary check on
 	 * rendering very large networks. It is primarily checked when a user wishes
 	 * to create a view for a large network.
 	 * 
 	 * @return threshold value, indicating number of nodes.
 	 */
 	public static int getSecondaryViewThreshold() {
 		return secondaryViewThreshold;
 	}
 
 	/**
 	 * Sets the Secondary View Threshold. This value is a secondary check on
 	 * rendering very large networks. It is primarily checked when a user wishes
 	 * to create a view for a large network.
 	 * 
 	 * @param threshold
 	 *            value, indicating number of nodes.
 	 */
 	public static void setSecondaryViewThreshold(int threshold) {
 		secondaryViewThreshold = threshold;
 	}
 
 	// View Only Variables
 	public static String getVizmapPropertiesLocation() {
 		return vizmapPropertiesLocation;
 	}
 
 	public static String getDefaultVisualStyle() {
 		return defaultVisualStyle;
 	}
 
 	private void setVariablesFromInitParams(CyInitParams params) {
 
 		if (params.getBioDataServer() != null) {
 			bioDataServer = params.getBioDataServer();
 		}
 
 		noCanonicalization = params.canonicalizeNames();
 
 		if (params.getSpecies() != null) {
 			defaultSpeciesName = params.getSpecies();
 		}
 
 		expressionFiles.addAll(params.getExpressionFiles());
 		graphFiles.addAll(params.getGraphFiles());
 		nodeAttributes.addAll(params.getNodeAttributeFiles());
 		edgeAttributes.addAll(params.getEdgeAttributeFiles());
 		pluginURLs.addAll(params.getPluginURLs());
 
 		if (params.getViewThreshold() != null)
 			viewThreshold = params.getViewThreshold().intValue();
 	}
 
 	/**
 	 * Use the Properties Object that was retrieved from the CyPropertiesReader
 	 * to set all known global variables
 	 */
 	private void setVariablesFromProperties() {
 
 		// plugins
 		if (properties.getProperty("plugins") != null) {
 			String[] pargs = properties.getProperty("plugins").split(",");
 			for (int i = 0; i < pargs.length; i++) {
 				String plugin = pargs[i];
 				URL url;
 				try {
 					if (plugin.startsWith("http")) {
 						plugin = "jar:" + plugin + "!/";
 						url = new URL(plugin);
 					} else {
 						url = new URL("file", "", plugin);
 					}
 					pluginURLs.add(url);
 				} catch (Exception ue) {
 					System.err.println("Jar: " + pargs[i]
 							+ "was not a valid URL");
 				}
 			}
 		}
 
 		// Data variables
 		defaultSpeciesName = properties.getProperty("defaultSpeciesName",
 				"unknown");
 		bioDataServer = properties.getProperty("bioDataServer", "unknown");
 
 		// Configuration variables
 		viewThreshold = (new Integer(properties.getProperty("viewThreshold",
 				"500"))).intValue();
 		secondaryViewThreshold = (new Integer(properties.getProperty(
 				"secondaryViewThreshold", "2000"))).intValue();
 		viewType = properties.getProperty("viewType", "internal");
 
 		// View Only Variables
 		defaultVisualStyle = properties.getProperty("defaultVisualStyle",
 				"default");
 
 		mrud = new File(properties.getProperty("mrud", System
 				.getProperty("user.dir")));
 	}
 
 	/**
 	 * Load all plugins by using the given URLs loading them all on one
 	 * URLClassLoader, then interating through each Jar file looking for classes
 	 * that are CytoscapePlugins
 	 * 
 	 * Optionally, iterate through all classes on the classpath, and try to find
 	 * plugins that way as well.
 	 */
 	private void loadPlugins(Set plugin_urls) {
 
 		URL[] urls = new URL[plugin_urls.size()];
 		int count = 0;
 		for (Iterator iter = plugin_urls.iterator(); iter.hasNext();) {
 			urls[count] = (URL) iter.next();
 			count++;
 		}
 
 		// the creation of the class loader automatically loads the plugins
 		classLoader = new URLClassLoader(urls, Cytoscape.class.getClassLoader());
 
 		// System.out.println( "class loader: "+classLoader );
 
 		// URL Controlaction = classLoader.findResource(
 		// "plugins/control/ControlAction.class" );
 		// System.out.println( "controlaction: "+Controlaction );
 
 		// iterate through the given jar files and find classes that are
 		// assignable
 		// from CytoscapePlugin
 		for (int i = 0; i < urls.length; ++i) {
 
 			try {
 				// create the jar file from the list of plugin jars
 				System.out.println("Create jarfile from: " + urls[i]);
 
 				// System.out.println( urls[i].getFile()+"protocol:
 				// "+urls[i].getProtocol() );
 
 				JarFile jar = null;
 
 				if (urls[i].getProtocol() == "file") {
 					jar = new JarFile(urls[i].getFile());
 				} else if (urls[i].getProtocol().startsWith("jar")) {
 					JarURLConnection jc = (JarURLConnection) urls[i]
 							.openConnection();
 					jar = jc.getJarFile();
 				}
 
 				// if the jar file is null, do nothing
 				if (jar == null) {
 					continue;
 				}
 
 				// System.out.println("- - - - entries begin");
 				Enumeration entries = jar.entries();
 				if (entries == null) {
 					continue;
 				}
 
 				// System.out.println("entries is not null");
 
 				int totalEntries = 0;
 				int totalClasses = 0;
 				int totalPlugins = 0;
 
 				while (entries.hasMoreElements()) {
 					totalEntries++;
 
 					// get the entry
 					String entry = entries.nextElement().toString();
 
 					// URL resource = classLoader.getResource( entry );
 					// System.out.println( "Entry: "+entry+ " is "+resource );
 
 					if (entry.endsWith("class")) {
 						// convert the entry to an assignable class name
 						entry = entry.replaceAll("\\.class$", "");
 						entry = entry.replaceAll("/", ".");
 
 						// System.out.println(" CLASS: " + entry);
 						if (!(isClassPlugin(entry))) {
 							// System.out.println(" not plugin.");
 							continue;
 						}
 						// System.out.println(entry+" is a PLUGIN!");
 						totalPlugins++;
 						invokePlugin(entry);
 					}
 				}
 				// System.out.println("- - - - entries finis");
 				// System.out.println(".jar summary: " +
 				// " entries=" + totalEntries +
 				// " classes=" + totalClasses +
 				// " plugins=" + totalPlugins);
 			} catch (Exception e) {
 				System.err.println("Error thrown: " + e.getMessage());
 				e.printStackTrace();
 			}
 
 		}
 	}
 
 	/**
 	 * Invokes the application in this jar file given the name of the main class
 	 * and assuming it is a plugin.
 	 * 
 	 * @param name
 	 *            the name of the plugin class
 	 */
 	protected void invokePlugin(String name) {
 		try {
 			loadPlugin(classLoader.loadClass(name));
 		} catch (Exception e) {
 			System.out.println("Error Invoking " + name);
 			e.printStackTrace();
 		}
 	}
 
 	public void loadPlugin(Class plugin) {
 
 		System.out.println("Plugin to be loaded: " + plugin);
 
 		if (AbstractPlugin.class.isAssignableFrom(plugin)) {
 			System.out.println("AbstractPlugin Loaded");
 			try {
 				AbstractPlugin.loadPlugin(plugin,
 						(cytoscape.view.CyWindow) Cytoscape.getDesktop());
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 
 		else if (CytoscapePlugin.class.isAssignableFrom(plugin)) {
 			System.out.println("CytoscapePlugin Loaded");
 			try {
 				CytoscapePlugin.loadPlugin(plugin);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Determines whether the class with a particular name extends
 	 * AbstractPlugin.
 	 * 
 	 * @param name
 	 *            the name of the putative plugin class
 	 */
 	protected boolean isClassPlugin(String name) {
 		// Class c = loadClass(name);
 		// Class c = Class.forName( name, false, classLoader );\
 		Class c = null;
 		try {
 			// System.out.println("Calling classLoader.loadClass(" + name +
 			// ")");
 			c = classLoader.loadClass(name);
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 			return false;
 		} catch (NoClassDefFoundError e) {
 			e.printStackTrace();
 			return false;
 		}
 		Class p = AbstractPlugin.class;
 		Class cp = CytoscapePlugin.class;
 		return (p.isAssignableFrom(c) || cp.isAssignableFrom(c));
 	}
 
 	/**
 	 * @return the most recently used directory
 	 */
 	public static File getMRUD() {
 		return mrud;
 	}
 
 	/**
 	 * @return the most recently used file
 	 */
 	public static File getMRUF() {
 		return mruf;
 	}
 
 	/**
 	 * @param mrud
 	 *            the most recently used directory
 	 */
 	public static void setMRUD(File mrud_new) {
 		mrud = mrud_new;
 	}
 
 	/**
 	 * @param mruf
 	 *            the most recently used file
 	 */
 	public static void setMRUF(File mruf_new) {
 		mruf = mruf_new;
 	}
 
 	// KONO 10/10/2005 BEGIN
 	public static void setDefaultSpeciesName() {
 		// Update defaultSpeciesName using current properties.
 		// This is necessary to reflect changes in the Preference Editor
 		// immediately
 		defaultSpeciesName = getProperties().getProperty("defaultSpeciesName");
 	}
 
 	// KONO 10/10/2005 END
 
 	// //////////////////////////////////////
 	// Config Directory Acces
 
 	/**
 	 * If .cytoscape directory does not exist, it creates it and returns it
 	 * 
 	 * @return the directory ".cytoscape" in the users home directory.
 	 */
 	public static File getConfigDirectoy() {
 		File dir = null;
 		try {
 			File parent_dir = new File(System.getProperty("user.home"),
 					".cytoscape");
 			if (parent_dir.mkdir())
 				System.err.println("Parent_Dir: " + parent_dir + " created.");
 
 			return parent_dir;
 		} catch (Exception e) {
 			System.err.println("error getting config directory");
 		}
 		return null;
 	}
 
 	public static File getConfigFile(String file_name) {
 		try {
 			File parent_dir = getConfigDirectoy();
 			File file = new File(parent_dir, file_name);
 			if (file.createNewFile())
 				System.err.println("Config file: " + file + " created.");
 			return file;
 
 		} catch (Exception e) {
 			System.err.println("error getting config file:" + file_name);
 		}
 		return null;
 	}
 
 	public static File getSpecifiedVizProps() {
 		File f = null;
 		if ( specifiedVizProps != null && specifiedVizProps.length() > 0 )
 			f = new File(specifiedVizProps);
 
 		return f;
 	}
 }
