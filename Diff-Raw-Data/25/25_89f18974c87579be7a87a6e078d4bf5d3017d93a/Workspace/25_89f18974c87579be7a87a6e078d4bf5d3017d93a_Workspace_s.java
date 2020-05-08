 package com.hp.hpl.deli;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.Serializable;
 import java.net.URL;
 import java.util.HashMap;
 
 import javax.servlet.ServletContext;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.StmtIterator;
 
 /**
  * This class provides a workspace for a specific CC/PP implementation. The
  * constructor configures the workspace using a properties file. This allows it
  * to support a specific protocol and vocabulary such as UAProf.
  */
 public class Workspace implements Serializable {
 	private static Log log = LogFactory.getLog(Workspace.class);
 
 	/** Comment for <code>serialVersionUID</code> */
 	private static final long serialVersionUID = 1L;
 
 	/** Workspace is a singleton. */
 	private transient static Workspace instance = null;
 
 	/** Constant for converting milliseconds in to hours */
 	private static final long HOURINMILLISECONDS = 1000 * 60 * 60;
 
	/** the path to the config file */
 	protected static String path = null;
 
 	/** datatype expressions of validation */
 	protected HashMap<String, String> datatypeExpressions;
 
 	/** The Servlet Context */
 	private transient ServletContext servletContext;
 
 	/** The current set of vocabularies */
 	protected Vocabulary vocabulary;
 
 	/** The profile cache object. */
 	protected transient ProfileCache profileCache;
 
 	/** The local profiles database. */
 	protected transient LocalProfiles localProfiles;
 
 	// properties
 
 	/** Maximum lifetime of a cached profile in hours. */
 	private long maxCachedProfileLifetime = 24 * HOURINMILLISECONDS;
 
 	/** The maximum number of profiles in the profile cache. */
 	private int maxCacheSize = 100;
 
 	/** Whether to refresh cached profiles after maximum lifetime has expired. */
 	private boolean refreshStaleProfiles = false;
 
 	/** Is datatype validation on? */
 	protected boolean datatypeValidationOn = true;
 
 	/**
 	 * Try to process attributes that have not been defined in the vocabulary
 	 * definition?
 	 */
 	protected boolean processUndefinedAttributes = false;
 
 	/**
 	 * Is whitespace normalisation of the profile-diff prior to calculating the
 	 * profile-diff-digest turned on?
 	 */
 	protected boolean normaliseWhitespaceInProfileDiff = false;
 
 	/** Is profile-diff-digest verification turned on? */
 	protected boolean profileDiffDigestVerification = false;
 
 	/**
 	 * Use the local profiles database if there is no CC/PP information in the
 	 * request?
 	 */
 	protected boolean useLocalProfilesIfNoCCPP = true;
 
 	/** Local profile path. */
 	protected String localProfilesPath = null;
 
 	/** Datatype config file */
 	protected String datatypeConfigFile = Constants.DATATYPE_CONFIG_FILE;
 
 	/** Process all RDF namespaces, irrespective of whether a schema exists? */
 	protected boolean processUnconfiguredNamespaces = true;
 
 	/**
 	 * Print both default and override values of attributes for debugging
 	 * purposes?
 	 */
 	protected boolean printDefaults = true;
 
 	/** Is the automatic debug log information turned on? */
 	protected boolean debug = false;
 
 	/** Debug request headers? */
 	protected boolean debugRequestHeaders = false;
 
 	/** Print the profile before merging for debugging purposes? */
 	protected boolean printProfileBeforeMerge = false;
 
 	/** Turn reference profile caching on or off. */
 	protected boolean cacheReferenceProfiles = true;
 
 	/** The name for CC/PP components. */
 	protected String componentProperty = "component";
 
 	/** Should remote profiles be ignored if there is a local one? */
 	protected boolean preferLocalOverRemoteProfiles = false;
 
 	/** Check that UAProf 2 profiles contain datatype information? */
 	protected boolean enforceUAProfTwoDatatypes = false;
 
 	public Workspace() {
 		instance = this;
 		servletContext = null;
 		datatypeExpressions = new HashMap<String, String>();
 	}
 
 	public static Workspace getInstance() {
 		if (instance == null) {
 			instance = new Workspace();
 		}
 
 		return instance;
 	}
 
 	/**
 	 * This method processes the configuration file and constructs the
 	 * vocabulary, profile cache and local profiles objects.
 	 * 
 	 * @param sc
 	 *            The servlet context.
 	 * @param filename
 	 *            The filename of the configuration file.
 	 */
 	public void configure(ServletContext sc, final String filename) {
 		// Nested class to read configuration file
 
 		class CreateWorkspace {
 			/** File containing configuration information. */
 			String namespaceConfigFile = null;
 
 			/** File containing legacy profiles . */
 			String localProfilesFile = null;
 
 			Model configurationData = null;
 
 			CreateWorkspace() {
 				log.debug("Workspace: loading configuration file");
 				configurationData = ModelFactory.createDefaultModel();
				File input = new File(filename);
 
 				try {
					configurationData.read(new FileInputStream(input), "", "N3");
 					updateProperties();
 				} catch (Exception e) {
 					log.error("Workspace: Cannot load workspace configuration file from " + path + filename, e);
 				}
 
 				log.debug("Workspace: loading vocabularies " + namespaceConfigFile);
 				vocabulary = new Vocabulary(namespaceConfigFile);
 
 				log.debug("Workspace: Creating the local profiles database");
 				localProfiles = new LocalProfiles(localProfilesFile); 
 
 				try {
 					datatypeExpression(datatypeConfigFile);
 				} catch (Exception e) {
 					log.error("Failed to load validator datatypes from config/uaprofValidatorConfig.xml", e);
 					log.info("Using defaults");
 				}
 			}
 
 			void datatypeExpression(String configFile) throws Exception {
 				Document document = null;
 
 				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 				DocumentBuilder parser = dbf.newDocumentBuilder();
 				InputSource inputSource = Workspace.getInstance().getInputSource(configFile);
 				document = parser.parse(inputSource);
 				Node config = document;
 
 				NodeList children = document.getChildNodes();
 				for (int i = 0; i < children.getLength(); i++) {
 					if (children.item(i).getNodeName().equals("validator")) {
 						config = children.item(i);
 						break;
 					}
 				}
 
 				if (config != null) {
 					// Find all <datatype> elements
 					children = config.getChildNodes();
 					for (int i = 0; i < children.getLength(); i++) {
 						if (children.item(i).getNodeName().equals("datatype")) {
 							setDatatypeFromConfig(children.item(i));
 						}
 					}
 				} else {
 					for (String[] dt : Constants.DATATYPES) {
 						datatypeExpressions.put(dt[0], dt[1]);
 					}
 				}
 
 			}
 
 			/**
 			 * Adds a datatype definition using the given element from an XML
 			 * configuration document
 			 * 
 			 * @param datatype
 			 *            The XML element containing the datatype information
 			 * 
 			 * @param datatype
 			 *            the node with the datatype information
 			 * @throws IOException
 			 *             thrown if there is a problem with the config file
 			 */
 			protected void setDatatypeFromConfig(Node datatype) throws IOException {
 				String name = null;
 				String expression = null;
 
 				NodeList children = datatype.getChildNodes();
 				for (int i = 0; i < children.getLength(); i++) {
 					Node currentChild = children.item(i);
 
 					if (currentChild.getNodeName().equals("name")) {
 						name = currentChild.getFirstChild().getNodeValue();
 					} else if (currentChild.getNodeName().equals("expression")) {
 						expression = currentChild.getFirstChild().getNodeValue();
 					}
 				}
 
 				if ((name != null) && (expression != null)) {
 					datatypeExpressions.put(name, expression);
 				} else {
 					throw new IOException("Datatype config file is invalid");
 				}
 			}
 
 			void updateProperties() {
 
 				debug = set(DeliSchema.debug, debug);
 				profileDiffDigestVerification = set(DeliSchema.profileDiffDigestVerification,
 						profileDiffDigestVerification);
 				preferLocalOverRemoteProfiles = set(DeliSchema.preferLocalOverRemoteProfiles,
 						preferLocalOverRemoteProfiles);
 				debugRequestHeaders = set(DeliSchema.debugRequestHeaders, debugRequestHeaders);
 				refreshStaleProfiles = set(DeliSchema.refreshStaleProfiles, refreshStaleProfiles);
 				useLocalProfilesIfNoCCPP = set(DeliSchema.useLocalProfilesIfNoCCPP, useLocalProfilesIfNoCCPP);
 				processUndefinedAttributes = set(DeliSchema.processUndefinedAttributes, processUndefinedAttributes);
 				normaliseWhitespaceInProfileDiff = set(DeliSchema.normaliseWhitespaceInProfileDiff,
 						normaliseWhitespaceInProfileDiff);
 				processUnconfiguredNamespaces = set(DeliSchema.processUnconfiguredNamespaces,
 						processUnconfiguredNamespaces);
 				cacheReferenceProfiles = set(DeliSchema.cacheReferenceProfiles, cacheReferenceProfiles);
 				printDefaults = set(DeliSchema.printDefaults, printDefaults);
 				printProfileBeforeMerge = set(DeliSchema.printProfileBeforeMerge, printProfileBeforeMerge);
 				datatypeValidationOn = set(DeliSchema.datatypeValidationOn, datatypeValidationOn);
 				namespaceConfigFile = set(DeliSchema.namespaceConfigFile, namespaceConfigFile);
 				localProfilesFile = set(DeliSchema.localProfilesFile, localProfilesFile);
 				localProfilesPath = set(DeliSchema.localProfilesPath, localProfilesPath);
 				datatypeConfigFile = set(DeliSchema.datatypeConfigFile, datatypeConfigFile);
 				componentProperty = set(DeliSchema.componentProperty, componentProperty);
 				enforceUAProfTwoDatatypes = set(DeliSchema.enforceUAProfTwoDatatypes, enforceUAProfTwoDatatypes);
 				maxCacheSize = set(DeliSchema.maxCacheSize, maxCacheSize);
 
 				StmtIterator si2 = configurationData.listStatements(null, DeliSchema.maxCachedProfileLifetime,
 						(RDFNode) null);
 				if (si2.hasNext()) {
 					maxCachedProfileLifetime = si2.nextStatement().getLong() * HOURINMILLISECONDS;
 					log.debug("Setting maxCachedProfileLifetime: " + maxCachedProfileLifetime);
 				}
 			}
 
 			private int set(Property p, int current) {
 				StmtIterator si = configurationData.listStatements(null, p, (RDFNode) null);
 				return si.hasNext() ? si.nextStatement().getInt() : current;
 			}
 
 			private boolean set(Property p, boolean current) {
 				StmtIterator si = configurationData.listStatements(null, p, (RDFNode) null);
 				return si.hasNext() ? si.nextStatement().getBoolean() : current;
 			}
 
 			private String set(Property p, String current) {
 				StmtIterator si = configurationData.listStatements(null, p, (RDFNode) null);
 				return si.hasNext() ? si.nextStatement().getString() : current;
 			}
 		}
 
 		servletContext = sc;
 		String tempPath = System.getProperty("deliHome");
 
 		if (tempPath != null) {
 			path = tempPath;
 			log.info("Using deliHome: " + path);
 		}
 
 		if (servletContext != null) {
 			if (path == null) {
 				try {
 					path = servletContext.getRealPath("/WEB-INF");
 					log.info("Using getRealPath: " + path);
 				} catch (Exception e) {
 					// If DELI fails here, its serious so print to console
 					log.error("Unable to use getRealPath to locate /WEB-INF", e);
 				}
 			}
 
 			if (path == null) {
 				try {
 					URL resUrl = servletContext.getResource("/WEB-INF");
 
 					if (resUrl != null) {
 						path = resUrl.toString();
 						log.info("Using getResource: " + path);
 					}
 				} catch (java.net.MalformedURLException me) {
 					// If DELI fails here, its serious so print to console
 					log.error("Unable to use getResource to locate /WEB-INF", me);
 				}
 			}
 		} else if (path == null) {
 			path = "";
 		}
 
 		if (path != null && path.length() > 0 && !path.endsWith("/")) {
 			/*
 			 * !path.endWith("/") is not needed (directory path doesn't end with /)
 			 * but who knows what some servlet container do
 			 */
 			path = path + "/";
 		}
 
 		log.debug("DELI is using path: " + path);
 
 		new CreateWorkspace();
 
 		log.debug("Workspace: Creating the profile cache");
 		profileCache = new ProfileCache(cacheReferenceProfiles, refreshStaleProfiles, maxCachedProfileLifetime,
 				maxCacheSize);
 		
 		// set proxy
 		if (Constants.PROXY_PORT_VALUE != null) {
 			System.setProperty(Constants.PROXY_PARAM, Constants.PROXY_VALUE);
 			System.setProperty(Constants.PROXY_PORT_PARAM, Constants.PROXY_PORT_VALUE);
 			System.setProperty("http.proxySet", "true");
 		}
 
 		log.debug("Workspace: Finished creating workspace");
 	}
 
 	protected InputSource getInputSource(String name) {
 		InputStream in = getResource(name);
 		Reader resource = new InputStreamReader(in);
 		InputSource inputSource = new InputSource(resource);
 
 		return inputSource;
 	}
 
 	public static String getPath() {
 		return path;
 	}
 
 	/**
 	 * This method returns a local or global resource. If it is a local
 	 * resource, DELI uses the appropriate method depending on whether DELI is
 	 * running within a servlet or not.
 	 * 
 	 * @param name
 	 *            The name of the resource. If it is a URL then it is a global
 	 *            resource.
 	 * @return A InputStream for the resource.
 	 */
 	public InputStream getResource(String name) {
 		try {
 			log.debug("Getting resource: " + path + name);
 
 			File file = new File(path + name);
 			InputStream in;
 
 			if (name.startsWith("http") || name.startsWith("file") || name.startsWith("jndi")) {
 				in = (new URL(name)).openStream();
 			} else if (file.exists()) {
 				in = new FileInputStream(path + name);
 			} else if (servletContext != null) {
				/* in = servletContext.getResourceAsStream(name); */
 
 				URL url = servletContext.getResource(path + name);
 				in = url.openStream();
 
 			} else {
 				in = getClass().getClassLoader().getResourceAsStream(name);
 			}
 
 			return in;
 		} catch (FileNotFoundException fnfe) {
 			log.error("Could not load file: " + name, fnfe);
 		} catch (Exception e) {
 			log.error("Cannot retrieve resource: " + name, e);
 		}
 
 		return null;
 	}
 
 	/**
 	 * @return the refreshStaleProfiles
 	 */
 	boolean isRefreshStaleProfiles() {
 		return refreshStaleProfiles;
 	}
 
 	/**
 	 * @return the maxCachedProfileLifetime
 	 */
 	long getMaxCachedProfileLifetime() {
 		return maxCachedProfileLifetime;
 	}
 }
