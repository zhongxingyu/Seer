 package info.rvin.mojo.flexmojo.compiler;
 
 import static info.rvin.flexmojos.utilities.MavenUtils.getArtifactFile;
 import static info.rvin.flexmojos.utilities.MavenUtils.resolveArtifact;
 import info.rvin.flexmojos.utilities.MavenUtils;
 import info.rvin.mojo.flexmojo.AbstractIrvinMojo;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.Writer;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
 import org.apache.maven.model.Contributor;
 import org.apache.maven.model.Developer;
 import org.apache.maven.model.Resource;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 
 import flex2.tools.oem.Builder;
 import flex2.tools.oem.Configuration;
 import flex2.tools.oem.Report;
 
 public abstract class AbstractFlexCompilerMojo<E extends Builder> extends
 		AbstractIrvinMojo {
 
 	private static final String COMPATIBILITY_2_0_0 = "2.0.0";
 	private static final String COMPATIBILITY_2_0_1 = "2.0.1";
 	private static final String COMPATIBILITY_3_0_0 = "3.0.0";
 
 	/**
 	 * license.properties locations get from
 	 * http://livedocs.adobe.com/flex/3/html/configuring_environment_2.html
 	 */
 	private static final File[] licensePropertiesLocations = new File[] {
 			new File( // Windows XP
 					"C:/Documents and Settings/All Users/Application Data/Adobe/Flex/license.properties"),
 			new File( // Windows Vista
 					"C:/ProgramData/Adobe/Flex/license.properties"),
 			new File( // Mac OSC
 					"/Library/Application Support/Adobe/Flex/license.properties"),
 			new File( // Linux
 					"~/.adobe/Flex/license.properties") };
 
 	/**
 	 * Turn on generation of accessible SWFs.
 	 *
 	 * @parameter default-value="false"
 	 */
 	private boolean accessible;
 
 	/**
 	 * Sets the locales that the compiler uses to replace <code>{locale}</code>
 	 * tokens that appear in some configuration values. This is equivalent to
 	 * using the <code>compiler.locale</code> option of the mxmlc or compc
 	 * compilers.
 	 *
 	 * @parameter
 	 */
 	protected String[] locales;
 
 	/**
 	 * List of path elements that form the roots of ActionScript class
 	 * hierarchies.
 	 *
 	 * @parameter
 	 */
 	protected File[] sourcePaths;
 
 	/**
 	 * Allow the source-path to have path-elements which contain other
 	 * path-elements
 	 *
 	 * @parameter default-value="false"
 	 */
 	private boolean allowSourcePathOverlap;
 
 	/**
 	 * Run the AS3 compiler in a mode that detects legal but potentially
 	 * incorrect code
 	 *
 	 * @parameter default-value="true"
 	 */
 	private boolean showWarnings;
 
 	/**
 	 * Enables checking of the following ActionScript warnings:
 	 *
 	 * <pre>
 	 * --compiler.warn-array-tostring-changes
 	 * --compiler.warn-assignment-within-conditional
 	 * --compiler.warn-bad-array-cast
 	 * --compiler.warn-bad-bool-assignment
 	 * --compiler.warn-bad-date-cast
 	 * --compiler.warn-bad-es3-type-method
 	 * --compiler.warn-bad-es3-type-prop
 	 * --compiler.warn-bad-nan-comparison
 	 * --compiler.warn-bad-null-assignment
 	 * --compiler.warn-bad-null-comparison
 	 * --compiler.warn-bad-undefined-comparison
 	 * --compiler.warn-boolean-constructor-with-no-args
 	 * --compiler.warn-changes-in-resolve
 	 * --compiler.warn-class-is-sealed
 	 * --compiler.warn-const-not-initialized
 	 * --compiler.warn-constructor-returns-value
 	 * --compiler.warn-deprecated-event-handler-error
 	 * --compiler.warn-deprecated-function-error
 	 * --compiler.warn-deprecated-property-error
 	 * --compiler.warn-duplicate-argument-names
 	 * --compiler.warn-duplicate-variable-def
 	 * --compiler.warn-for-var-in-changes
 	 * --compiler.warn-import-hides-class
 	 * --compiler.warn-instance-of-changes
 	 * --compiler.warn-internal-error
 	 * --compiler.warn-level-not-supported
 	 * --compiler.warn-missing-namespace-decl
 	 * --compiler.warn-negative-uint-literal
 	 * --compiler.warn-no-constructor
 	 * --compiler.warn-no-explicit-super-call-in-constructor
 	 * --compiler.warn-no-type-decl
 	 * --compiler.warn-number-from-string-changes
 	 * --compiler.warn-scoping-change-in-this
 	 * --compiler.warn-slow-text-field-addition
 	 * --compiler.warn-unlikely-function-value
 	 * --compiler.warn-xml-class-has-changed
 	 * </pre>
 	 *
 	 * @see Warning
 	 */
 	private Warning warnigs;
 
 	/**
 	 * Turn on generation of debuggable SWFs. False by default for mxmlc, but
 	 * true by default for compc.
 	 *
 	 * @parameter default-value="false"
 	 */
 	private boolean debug;
 
 	/**
 	 * A password that is embedded in the application
 	 *
 	 * @parameter
 	 */
 	private String debugPassword;
 
 	/**
 	 * Turn on writing of generated/*.as files to disk. These files are
 	 * generated by the compiler during mxml translation and are helpful with
 	 * understanding and debugging Flex applications.
 	 *
 	 * @parameter default-value="false"
 	 */
 	private boolean keepGeneratedActionscript;
 
 	/**
 	 * Specify a URI to associate with a manifest of components for use as MXML
 	 * elements.
 	 *
 	 * @parameter
 	 */
 	private Namespace[] namespaces;
 
 	/**
 	 * Enable post-link SWF optimization.
 	 *
 	 * @parameter default-value="true"
 	 */
 	private boolean optimize;
 
 	/**
 	 * If the <code>incremental</code> input argument is <code>false</code>,
 	 * this method recompiles all parts of the object. If the
 	 * <code>incremental</code> input argument is <code>true</code>, this method
 	 * compiles only the parts of the object that have changed since the last
 	 * compilation.
 	 *
 	 * @parameter default-value="false"
 	 */
 	private boolean incremental;
 
 	/**
 	 * Keep the following AS3 metadata in the bytecodes.
 	 *
 	 * @parameter
 	 */
 	private String[] keepAs3Metadatas;
 
 	/**
 	 * Run the AS3 compiler in strict error checking mode.
 	 *
 	 * @parameter default-value="true"
 	 */
 	private boolean strict;
 
 	/**
 	 * Use the ActionScript 3 class based object model for greater performance
 	 * and better error reporting. In the class based object model most built-in
 	 * functions are implemented as fixed methods of classes (-strict is
 	 * recommended, but not required, for earlier errors)
 	 *
 	 * @parameter default-value="true"
 	 */
 	private boolean as3;
 
 	/**
 	 * Use the ECMAScript edition 3 prototype based object model to allow
 	 * dynamic overriding of prototype properties. In the prototype based object
 	 * model built-in functions are implemented as dynamic properties of
 	 * prototype objects (-strict is allowed, but may result in compiler errors
 	 * for references to dynamic properties)
 	 *
 	 * @parameter default-value="false"
 	 */
 	private boolean es;
 
 	/**
 	 * Turns on the display of stack traces for uncaught runtime errors.
 	 *
 	 * @parameter default-value="false"
 	 */
 	private boolean verboseStacktraces;
 
 	/**
 	 * Local Fonts Snapshot File containing cached system font licensing
 	 * information produced via
 	 * <code>java -cp mxmlc.jar flex2.tools.FontSnapshot (fontpath)</code>. Will
 	 * default to winFonts.ser on Windows XP and macFonts.ser on Mac OS X.
 	 *
 	 * @parameter
 	 */
 	private Font fonts;
 
 	/**
 	 * Enables SWFs to access the network.
 	 *
 	 * @parameter default-value="true"
 	 */
 	private boolean useNetwork;
 
 	/**
 	 * licenses: specifies a list of product and serial number pairs.
 	 *
 	 * @parameter
 	 */
 	private Map<String, String> licenses;
 
 	/**
 	 * defines: specifies a list of define directive key and value pairs. For
 	 * example, CONFIG::debugging
 	 *
 	 * @parameter
 	 */
 	private Map<String, String> defines;
 
 	/**
 	 * Sets the context root path so that the compiler can replace
 	 * <code>{context.root}</code> tokens for service channel endpoints.
 	 *
 	 * @parameter default-value=""
 	 */
 	private String contextRoot;
 
 	/**
 	 * Uses the default compiler options as base
 	 *
 	 * @parameter default-value="false"
 	 */
 	protected boolean linkReport;
 
 	/**
 	 * Writes the configuration report to a file after the build.
 	 *
 	 * @parameter default-value="false"
 	 */
 	private boolean configurationReport;
 
 	/**
 	 * Sets a list of artifacts to omit from linking when building an
 	 * application. This is equivalent to using the <code>load-externs</code>
 	 * option of the mxmlc or compc compilers.
 	 *
 	 * @parameter
 	 */
 	private MavenArtifact[] loadExterns;
 
 	/**
 	 * Load a file containing configuration options
 	 *
 	 * If not defined, by default will search for one on resources folder.
 	 *
 	 * If not found an empty config file will be generated at target folder.
 	 * This file doesn't reflect the configurations defined on pom.xml, is only
 	 * put there because flex-compiler-oem always try to read a config.xml.
 	 *
 	 * @parameter
 	 */
 	protected File configFile;
 
 	/**
 	 * The filename of the SWF movie to create
 	 *
 	 * @parameter
 	 */
 	private String output;
 
 	/**
 	 * specifies the version of the player the application is targeting.
 	 * Features requiring a later version will not be compiled into the
 	 * application. The minimum value supported is "9.0.0".
 	 *
 	 * @parameter default-value="9.0.0"
 	 */
 	private String targetPlayer;
 
 	/**
 	 * Sets the metadata section of the application SWF. This is equivalent to
 	 * the <code>raw-metadata</code> option of the mxmlc or compc compilers.
 	 *
 	 * Need a well-formed XML fragment
 	 *
 	 * @parameter
 	 */
 	private String rawMetadata;
 
 	/**
 	 * SWF metadata useless there is no API to read it.
 	 *
 	 * @parameter
 	 */
 	private Metadata metadata;
 
 	/**
 	 * rslUrls array of URLs. The first RSL URL in the list is the primary RSL.
 	 * The remaining RSL URLs will only be loaded if the primary RSL fails to
 	 * load.
 	 *
 	 * Accept some special tokens:
 	 *
 	 * <pre>
 	 * {contextRoot}		- replace by defined context root
 	 * {groupId}			- replace by library groupId
 	 * {artifactId}			- replace by library artifactId
 	 * {version}			- replace by library version
 	 * {extension}			- replace by library extension swf or swz
 	 * </pre>
 	 *
 	 * default-value="/{contextRoot}/rsl/{artifactId}-{version}.{extension}"
 	 *
 	 * @parameter
 	 */
 	private String[] rslUrls;
 
 	/**
 	 * policyFileUrls array of policy file URLs. Each entry in the rslUrls array
 	 * must have a corresponding entry in this array. A policy file may be
 	 * needed in order to allow the player to read an RSL from another domain.
 	 * If a policy file is not required, then set it to an empty string.
 	 *
 	 * Accept some special tokens:
 	 *
 	 * <pre>
 	 * {contextRoot}		- replace by defined context root
 	 * {groupId}			- replace by library groupId
 	 * {artifactId}			- replace by library artifactId
 	 * {version}			- replace by library version
 	 * {extension}			- replace by library extension swf or swz
 	 * </pre>
 	 *
 	 * @parameter
 	 */
 	private String[] policyFileUrls;
 
 	/**
 	 * Sets the location of the Flex Data Services service configuration file.
 	 * This is equivalent to using the <code>compiler.services</code> option of
 	 * the mxmlc and compc compilers.
 	 *
 	 * If not define will look inside resources directory for
 	 * services-config.xml
 	 *
 	 * @parameter
 	 */
 	private File services;
 
 	/**
 	 * When true resources are compiled into Application or Library.
 	 *
 	 * When false resources are compiled into separated Application or Library
 	 * files.
 	 *
 	 * If not defined no resourceBundle generation is done
 	 *
 	 * @parameter
 	 */
 	private Boolean mergeResourceBundle;
 
 	/**
 	 * Define the base path to locate resouce bundle files
 	 *
 	 * Accept some special tokens:
 	 *
 	 * <pre>
 	 * {locale}		- replace by locale name
 	 * </pre>
 	 *
 	 * @parameter default-value="${basedir}/src/main/locales/{locale}"
 	 */
 	protected String resourceBundlePath;
 
 	/**
 	 * This is equilvalent to the
 	 * <code>compiler.mxmlc.compatibility-version</code> option of the compc
 	 * compiler. Must be in the form <major>.<minor>.<revision>
 	 *
 	 * Valid values: <tt>2.0.0</tt> and <tt>2.0.1</tt>
 	 *
 	 * @see http
 	 *      ://livedocs.adobe.com/flex/3/html/help.html?content=versioning_4.
 	 *      html
 	 *
 	 * @parameter
 	 */
 	private String compatibilityVersion;
 
 	/**
 	 * Sets the ActionScript file encoding. The compiler uses this encoding to
 	 * read the ActionScript source files. This is equivalent to using the
 	 * <code>actionscript-file-encoding</code> option of the mxmlc or compc
 	 * compilers.
 	 *
 	 * <p>
 	 * The character encoding; for example <code>UTF-8</code> or
 	 * <code>Big5</code>.
 	 *
 	 * @parameter default-value="UTF-8"
 	 */
 	private String encoding;
 
 	/**
 	 * Sets the location of the default CSS file. This is equivalent to using
 	 * the <code>compiler.defaults-css-url</code> option of the mxmlc or compc
 	 * compilers</code>.
 	 *
 	 * @parameter
 	 */
 	private File defaultsCss;
 
 	/**
 	 * Sets the default background color. You can override this by using the
 	 * application code. This is the equivalent of the
 	 * <code>default-background-color</code> option of the mxmlc or compc
 	 * compilers.
 	 *
 	 * @parameter default-value="869CA7"
 	 */
 	private String defaultBackgroundColor;
 
 	/**
 	 * Sets the default frame rate to be used in the application. This is the
 	 * equivalent of the <code>default-frame-rate</code> option of the mxmlc or
 	 * compc compilers.
 	 *
 	 * @parameter default-value="24"
 	 */
 	private int defaultFrameRate;
 
 	/**
 	 * Sets the default script execution limits (which can be overridden by root
 	 * attributes). This is equivalent to using the
 	 * <code>default-script-limits</code> option of the mxmlc or compc
 	 * compilers.
 	 *
 	 * Recursion depth
 	 *
 	 * @parameter default-value="1000"
 	 */
 	private int scriptMaxRecursionDepth;
 
 	/**
 	 * Sets the default script execution limits (which can be overridden by root
 	 * attributes). This is equivalent to using the
 	 * <code>default-script-limits</code> option of the mxmlc or compc
 	 * compilers.
 	 *
 	 * Execution time, in seconds
 	 *
 	 * @parameter default-value="60"
 	 */
 	private int scriptMaxExecutionTime;
 
 	/**
 	 * Sets the default application width in pixels. This is equivalent to using
 	 * the <code>default-size</code> option of the mxmlc or compc compilers.
 	 *
 	 * @parameter default-value="500"
 	 */
 	private int defaultSizeWidth;
 
 	/**
 	 * Sets the default application height in pixels. This is equivalent to
 	 * using the <code>default-size</code> option of the mxmlc or compc
 	 * compilers.
 	 *
 	 * @parameter default-value="375"
 	 */
 	private int defaultSizeHeight;
 
 	/*
 	 * TODO how to set this on flex-compiler-oem
 	 *
 	 * -dump-config <filename>
 	private String dumpConfig;
 	 */
 
 	/**
 	 * Sets a list of definitions to omit from linking when building an
 	 * application. This is equivalent to using the <code>externs</code> option
 	 * of the mxmlc and compc compilers.
 	 *
 	 * An array of definitions (for example, classes, functions, variables, or
 	 * namespaces).
 	 *
 	 * @parameter
 	 */
 	private String[] externs;
 
 	/**
 	 * Sets a SWF frame label with a sequence of class names that are linked
 	 * onto the frame. This is equivalent to using the <code>frames.frame</code>
 	 * option of the mxmlc or compc compilers.
 	 *
 	 * @parameter
 	 */
 	private FrameLabel[] frames;
 
 	/**
 	 * Sets a list of definitions to always link in when building an
 	 * application. This is equivalent to using the <code>includes</code> option
 	 * of the mxmlc or compc compilers.
 	 *
 	 * An array of definitions (for example, classes, functions, variables, or
 	 * namespaces).
 	 *
 	 * @parameter
 	 */
 	private String[] includes;
 
 	/**
 	 * Sets the compiler when it runs on a server without a display. This is
 	 * equivalent to using the <code>compiler.headless-server</code> option of
 	 * the mxmlc or compc compilers.
 	 *
 	 * that value determines if the compiler is running on a server without a
 	 * display.
 	 *
 	 * @parameter default-value="false"
 	 */
 	private boolean headlessServer;
 
 	/**
 	 * Instructs the compiler to keep a style sheet's type selector in a SWF
 	 * file, even if that type (the class) is not used in the application.
 	 *
 	 * This is equivalent to using the
 	 * <code>compiler.keep-all-type-selectors</code> option of the mxmlc or
 	 * compc compilers.
 	 *
 	 * @parameter default-value="false"
 	 */
 	private boolean keepAllTypeSelectors;
 
 	/**
 	 * Determines whether resources bundles are included in the application.
 	 *
 	 * This is equivalent to using the
 	 * <code>compiler.use-resource-bundle-metadata</code> option of the mxmlc or
 	 * compc compilers.
 	 *
 	 * @parameter default-value="true"
 	 */
 	private boolean useResourceBundleMetadata;
 
 	/*
 	 * TODO how to set this on flex-compiler-oem
 	 *
 	 * -resource-bundle-list <filename>
 	private String resourceBundleList;
 	 */
 
 	/*
 	 * TODO how to set this on flex-compiler-oem
 	 *
 	 * -static-link-runtime-shared-libraries
 	private boolean staticLinkRuntimeSharedLibraries;
 	 */
 
 	/**
 	 * Verifies the RSL loaded has the same digest as the RSL specified when the
 	 * application was compiled. This is equivalent to using the
 	 * <code>verify-digests</code> option in the mxmlc compiler.
 	 *
 	 * @parameter default-value="true"
 	 */
 	private boolean verifyDigests;
 
 	/**
 	 * Previous compilation data, used to incremental builds
 	 */
 	private File compilationData;
 
 	/**
 	 * Builder to be used by compiler
 	 */
 	protected E builder;
 
 	/**
 	 * Compiled file
 	 */
 	protected File outputFile;
 
 	/**
 	 * Flex OEM compiler configurations
 	 */
 	protected Configuration configuration;
 
 	/**
 	 * When true sets the artifact generated by this mojos as pom artifact
 	 */
 	protected boolean isSetProjectFile = true;
 
 	/**
 	 * Generated link report file
 	 */
 	protected File linkReportFile;
 
 	/**
 	 * Construct instance
 	 */
 	public AbstractFlexCompilerMojo() {
 		super();
 	}
 
 	/**
 	 * Setup before compilation of source
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public void setUp() throws MojoExecutionException, MojoFailureException {
 		/*
 		 * Can't automatic initialize, if (locales == null) { // TODO must
 		 * generate based on system locale? locales = new String[] { "en_US" };
 		 * }
 		 */
 
 		if (sourcePaths == null) {
 			sourcePaths = MavenUtils.getSourcePaths(build);
 			if (mergeResourceBundle != null && mergeResourceBundle) {
 				List<File> paths = new ArrayList<File>(Arrays
 						.asList(sourcePaths));
 				for (String locale : locales) {
 					File localeResourcePath = MavenUtils.getLocaleResourcePath(
 							resourceBundlePath, locale);
 					paths.add(localeResourcePath);
 				}
 				sourcePaths = paths.toArray(new File[paths.size()]);
 			}
 		}
 
 		if (outputFile == null) {
 			if (output == null) {
 				outputFile = new File(build.getDirectory(), build
 						.getFinalName()
 						+ "." + project.getPackaging());
 			} else {
 				outputFile = new File(build.getDirectory(), output);
 			}
 		}
 
 		if (configFile == null) {
 			List<Resource> resources = build.getResources();
 			for (Resource resource : resources) {
 				File cfg = new File(resource.getDirectory(), "config.xml");
 				if (cfg.exists()) {
 					configFile = cfg;
 					break;
 				}
 			}
 		}
 
 		if (configFile == null) {
 			getLog().debug("No config found, generating one!");
 			configFile = MavenUtils.getConfigFile(build);
 		}
 
 		if (!configFile.exists()) {
 			throw new MojoExecutionException("Unable to find " + configFile);
 		} else {
 			getLog().info("Using configuration file " + configFile);
 		}
 
 		if (services == null) {
 			List<Resource> resources = build.getResources();
 			for (Resource resource : resources) {
 				File cfg = new File(resource.getDirectory(),
 						"services-config.xml");
 				if (cfg.exists()) {
 					services = cfg;
 					break;
 				}
 			}
 		}
 
 		if (rslUrls == null) {
 			rslUrls = new String[] { "/{contextRoot}/rsl/{artifactId}-{version}.{extension}" };
 		}
 
 		if (policyFileUrls == null) {
 			policyFileUrls = new String[] { "" };
 		}
 
 		if (metadata == null) {
 			metadata = new Metadata();
 			if (project.getDevelopers() != null
 					&& !project.getDevelopers().isEmpty()) {
 				List<Developer> developers = project.getDevelopers();
 				for (Developer d : developers) {
 					metadata.setCreator(d.getName());
 					break;
 				}
 			}
 
 			if (project.getContributors() != null
 					&& !project.getContributors().isEmpty()) {
 				List<Contributor> contributors = project.getContributors();
 				for (Contributor c : contributors) {
 					metadata.setContributor(c.getName());
 					break;
 				}
 			}
			metadata.setDate(new Date());
 			if (locales != null) {
 				metadata.setLanguage(locales[0]);
 				metadata.addDescription(locales[0], project.getDescription());
 				metadata.addTitle(locales[0], project.getName());
 			}
 		}
 
 		if (licenses == null) {
 			licenses = getLicenses();
 		}
 
 		configuration = builder.getDefaultConfiguration();
 		configure();
 
 		compilationData = new File(build.getDirectory(), build.getFinalName()
 				+ ".incr");
 	}
 
 	private Map<String, String> getLicenses() throws MojoExecutionException {
 		File licensePropertyFile = null;
 		for (File lpl : licensePropertiesLocations) {
 			if (lpl.exists()) {
 				licensePropertyFile = lpl;
 				break;
 			}
 		}
 
 		if (licensePropertyFile == null) {
 			return null;
 		}
 
 		Properties props = new Properties();
 		try {
 			props.load(new FileInputStream(licensePropertyFile));
 		} catch (FileNotFoundException e) {
 			getLog().warn(
 					"Unable to read license files "
 							+ licensePropertyFile.getAbsolutePath(), e);
 			return null;
 		} catch (IOException e) {
 			getLog().warn(
 					"Unable to read license files "
 							+ licensePropertyFile.getAbsolutePath(), e);
 			return null;
 		}
 
 		Map<String, String> licenses = new HashMap<String, String>();
 
 		Enumeration<?> names = props.propertyNames();
 		while (names.hasMoreElements()) {
 			String name = (String) names.nextElement();
 			String value = props.getProperty(name);
 			licenses.put(name, value);
 		}
 
 		return licenses;
 	}
 
 	/**
 	 * Perform compilation of Flex source
 	 */
 	public void run() throws MojoExecutionException, MojoFailureException {
 		builder.setLogger(new CompileLogger(getLog()));
 
 		builder.setConfiguration(configuration);
 
 		build(builder);
 	}
 
 	/**
 	 * Writes compilation data to a file to support incremental compilation
 	 *
 	 * @return OutputStream with compilation data
 	 * @throws MojoExecutionException
 	 */
 	private OutputStream saveCompilationData() throws MojoExecutionException {
 		try {
 			return new BufferedOutputStream(new FileOutputStream(
 					compilationData));
 		} catch (FileNotFoundException e) {
 			throw new MojoExecutionException("Can't save compilation data.");
 		}
 	}
 
 	/**
 	 * Loads compilation data to support incremental compilation
 	 *
 	 * @return InputStream of compilation data
 	 * @throws MojoExecutionException
 	 */
 	private InputStream loadCompilationData() throws MojoExecutionException {
 		try {
 			return new BufferedInputStream(new FileInputStream(compilationData));
 		} catch (FileNotFoundException e) {
 			throw new MojoExecutionException(
 					"Previows compilation data not found.");
 		}
 	}
 
 	/**
 	 * Setup builder configuration
 	 *
 	 * @throws MojoExecutionException
 	 */
 	protected void configure() throws MojoExecutionException {
 		configuration.setExternalLibraryPath(getDependenciesPath("external"));
 
 		configuration.includeLibraries(getDependenciesPath("internal"));
 
 		configuration.setLibraryPath(getDependenciesPath("compile"));
 		configuration.addLibraryPath(getDependenciesPath("merged"));
 		if (mergeResourceBundle == null || mergeResourceBundle) {
 			configuration.addLibraryPath(getResourcesBundles());
 		}
 
 		resolveRuntimeLibraries();
 
 		configuration.setTheme(getDependenciesPath("theme"));
 
 		configuration.enableAccessibility(accessible);
 		configuration.allowSourcePathOverlap(allowSourcePathOverlap);
 		configuration.useActionScript3(as3);
 		configuration.enableDebugging(debug, debugPassword);
 		configuration.useECMAScript(es);
 
 		// Fonts
 		if (fonts != null) {
 			configuration.enableAdvancedAntiAliasing(fonts
 					.isAdvancedAntiAliasing());
 			configuration.enableFlashType(fonts.isFlashType());
 			configuration.setFontManagers(fonts.getManagers());
 			configuration.setMaximumCachedFonts(fonts.getMaxCachedFonts());
 			configuration.setMaximumGlyphsPerFace(fonts.getMaxGlyphsPerFace());
 			if (fonts.getLanguages() != null && !fonts.getLanguages().isEmpty()) {
 				for (String language : fonts.getLanguages().keySet()) {
 					configuration.setFontLanguageRange(language, fonts
 							.getLanguages().get(language));
 				}
 			}
 		}
 		File fontsSnapshot = getFontsSnapshot();
 		if (fontsSnapshot == null || !fontsSnapshot.exists()) {
 			throw new MojoExecutionException("LocalFontSnapshot not found "
 					+ fontsSnapshot);
 		}
 		configuration.setLocalFontSnapshot(fontsSnapshot);
 
 		configuration.setActionScriptMetadata(keepAs3Metadatas);
 		configuration
 				.keepCompilerGeneratedActionScript(keepGeneratedActionscript);
 
 		if (licenses != null) {
 			for (String licenseName : licenses.keySet()) {
 				String key = licenses.get(licenseName);
 				configuration.setLicense(licenseName, key);
 			}
 		}
 
 		if (defines != null) {
 			for (String defineName : defines.keySet()) {
 				String value = defines.get(defineName);
 				getLog().info("define " + defineName + " = " + value);
 				configuration.addDefineDirective(defineName, value);
 			}
 		}
 
 		// When using the resource-bundle-list option, you must also set the
 		// value of the locale option to an empty string.
 		if (mergeResourceBundle == null || mergeResourceBundle) {
 			configuration.setLocale(locales);
 		} else {
 			configuration.setLocale(new String[0]);
 		}
 
 		if (namespaces != null) {
 			for (Namespace namespace : namespaces) {
 				configuration.setComponentManifest(namespace.getUri(),
 						namespace.getManifest());
 			}
 		}
 
 		configuration.optimize(optimize);
 		if (this.warnigs != null) {
 			configureWarnings(configuration);
 		}
 
 		configuration.setSourcePath(sourcePaths);
 		configuration.enableStrictChecking(strict);
 		configuration.useNetwork(useNetwork);
 		configuration.enableVerboseStacktraces(verboseStacktraces);
 
 		if (contextRoot != null) {
 			configuration.setContextRoot(contextRoot);
 		}
 		configuration.keepLinkReport(linkReport);
 		configuration.keepConfigurationReport(configurationReport);
 		configuration.setConfiguration(configFile);
 		configuration.setServiceConfiguration(services);
 
 		if (loadExterns != null) {
 			List<File> externsFiles = new ArrayList<File>();
 
 			for (MavenArtifact mvnArtifact : loadExterns) {
 				Artifact artifact = artifactFactory
 						.createArtifactWithClassifier(mvnArtifact.getGroupId(),
 								mvnArtifact.getArtifactId(), mvnArtifact
 										.getVersion(), "xml", "link-report");
 				resolveArtifact(artifact, resolver, localRepository,
 						remoteRepositories);
 				externsFiles.add(artifact.getFile());
 			}
 			configuration.setExterns(externsFiles.toArray(new File[externsFiles
 					.size()]));
 
 		}
 
 		if (rawMetadata != null) {
 			configuration.setSWFMetaData(rawMetadata);
 		}
 
 		if (metadata != null) {
 			if (metadata.getContributor() != null) {
 				configuration.setSWFMetaData(Configuration.CONTRIBUTOR,
 						metadata.getContributor());
 			}
 
 			if (metadata.getCreator() != null) {
 				configuration.setSWFMetaData(Configuration.CREATOR, metadata
 						.getCreator());
 			}
 
 			if (metadata.getDate() != null) {
 				configuration.setSWFMetaData(Configuration.DATE, metadata
 						.getDate());
 			}
 
 			if (metadata.getDescriptions() != null) {
 				configuration.setSWFMetaData(Configuration.DESCRIPTION,
 						metadata.getDescriptions());
 			}
 
 			if (metadata.getTitles() != null) {
 				configuration.setSWFMetaData(Configuration.TITLE, metadata
 						.getTitles());
 			}
 
 			if (metadata.getLanguage() != null) {
 				configuration.setSWFMetaData(Configuration.LANGUAGE, metadata
 						.getLanguage());
 			}
 		}
 
 		if (compatibilityVersion != null) {
 			if (!COMPATIBILITY_2_0_0.equals(compatibilityVersion)
 					&& !COMPATIBILITY_2_0_1.equals(compatibilityVersion)
 					&& !COMPATIBILITY_3_0_0.equals(compatibilityVersion)) {
 				throw new MojoExecutionException(
 						"Invalid compatibility version " + compatibilityVersion);
 			} else if (COMPATIBILITY_2_0_0.equals(compatibilityVersion)) {
 				configuration.setCompatibilityVersion(2, 0, 0);
 			} else if (COMPATIBILITY_2_0_1.equals(compatibilityVersion)) {
 				configuration.setCompatibilityVersion(2, 0, 1);
 			} else if (COMPATIBILITY_3_0_0.equals(compatibilityVersion)) {
 				configuration.setCompatibilityVersion(3, 0, 0);
 			} else {
 				throw new IllegalStateException("Should never reach this");
 			}
 		}
 
 		configuration.setActionScriptFileEncoding(encoding);
 
 		if (targetPlayer != null) {
 			String[] nodes = targetPlayer.split("\\.");
 			if (nodes.length != 3) {
 				throw new MojoExecutionException("Invalid player version "
 						+ targetPlayer);
 			}
 			int[] versions = new int[nodes.length];
 			for (int i = 0; i < nodes.length; i++) {
 				try {
 					versions[i] = Integer.parseInt(nodes[i]);
 				} catch (NumberFormatException e) {
 					throw new MojoExecutionException("Invalid player version "
 							+ targetPlayer);
 				}
 			}
 			if (versions[0] < 9) {
 				throw new MojoExecutionException("Invalid player version "
 						+ targetPlayer);
 			}
 			configuration
 					.setTargetPlayer(versions[0], versions[1], versions[2]);
 		}
 
 		if (defaultsCss != null)
 			configuration.setDefaultCSS(defaultsCss);
 
 		configuration.setDefaultBackgroundColor(Integer.parseInt(
 				defaultBackgroundColor, 16));
 
 		configuration.setDefaultFrameRate(defaultFrameRate);
 
 		configuration.setDefaultScriptLimits(scriptMaxRecursionDepth,
 				scriptMaxExecutionTime);
 
 		configuration.setDefaultSize(defaultSizeWidth, defaultSizeHeight);
 
 		if (externs != null && externs.length > 0) {
 			configuration.setExterns(externs);
 		}
 
 		if (frames != null && frames.length > 0) {
 			for (FrameLabel frame : frames) {
 				configuration.setFrameLabel(frame.getLabel(), frame
 						.getClassNames());
 			}
 		}
 
 		if (includes != null && includes.length > 0) {
 			configuration.setIncludes(includes);
 		}
 
 		configuration.useHeadlessServer(headlessServer);
 
 		configuration.keepAllTypeSelectors(keepAllTypeSelectors);
 
 		configuration.useResourceBundleMetaData(useResourceBundleMetadata);
 
 		configuration.enableDigestVerification(verifyDigests);
 	}
 
 	/**
 	 * Resolves all runtime libraries, that includes RSL and framework CACHING
 	 *
 	 * @throws MojoExecutionException
 	 */
 	private void resolveRuntimeLibraries() throws MojoExecutionException {
 		List<Artifact> rsls = getDependencyArtifacts("rsl", "caching");
 		rslsSort(rsls);
 
 		for (Artifact artifact : rsls) {
 			String scope = artifact.getScope();
 			File artifactFile = getArtifactFile(artifact, scope, build);
 			String artifactPath = artifactFile.getAbsolutePath();
 			String extension;
 			if ("caching".equals(scope)) {
 				extension = "swz";
 			} else {
 				extension = "swf";
 			}
 			String[] rslUrls = getRslUrls(artifact, extension);
 			String[] rslPolicyFileUrls = getRslPolicyFileUrls(artifact);
 			configuration.addRuntimeSharedLibraryPath(artifactPath, rslUrls,
 					rslPolicyFileUrls);
 		}
 	}
 
 	public void rslsSort(List<Artifact> rslArtifacts)
 			throws MojoExecutionException {
 		Map<Artifact, List<Artifact>> dependencies = getDependencies(rslArtifacts);
 
 		List<Artifact> ordered = new ArrayList<Artifact>();
 		for (Artifact a : rslArtifacts) {
 			if (dependencies.get(a) == null || dependencies.get(a).isEmpty()) {
 				ordered.add(a);
 			}
 		}
 		rslArtifacts.removeAll(ordered);
 
 		while (!rslArtifacts.isEmpty()) {
 			int original = rslArtifacts.size();
 			for (Artifact a : rslArtifacts) {
 				List<Artifact> deps = dependencies.get(a);
 				if (ordered.containsAll(deps)) {
 					ordered.add(a);
 				}
 			}
 			rslArtifacts.removeAll(ordered);
 			if (original == rslArtifacts.size()) {
 				throw new MojoExecutionException("Unable to resolve "
 						+ rslArtifacts);
 			}
 		}
 
 		rslArtifacts.addAll(ordered);
 	}
 
 	@SuppressWarnings("unchecked")
 	private Map<Artifact, List<Artifact>> getDependencies(
 			List<Artifact> rslArtifacts) throws MojoExecutionException {
 		Map<Artifact, List<Artifact>> dependencies = new HashMap<Artifact, List<Artifact>>();
 
 		for (Artifact pomArtifact : rslArtifacts) {
 			try {
 				MavenProject pomProject = mavenProjectBuilder
 						.buildFromRepository(pomArtifact, remoteRepositories,
 								localRepository);
 				Set pomArtifacts = pomProject.createArtifacts(artifactFactory,
 						null, null);
 				ArtifactResolutionResult arr = resolver.resolveTransitively(
 						pomArtifacts, pomArtifact, remoteRepositories,
 						localRepository, artifactMetadataSource);
 				List<Artifact> artifactDependencies = new ArrayList(arr
 						.getArtifacts());
 				artifactDependencies = removeNonRSLDependencies(rslArtifacts,
 						artifactDependencies);
 				dependencies.put(pomArtifact, artifactDependencies);
 			} catch (Exception e) {
 				throw new MojoExecutionException(e.getMessage(), e);
 			}
 		}
 		return dependencies;
 	}
 
 	private List<Artifact> removeNonRSLDependencies(
 			List<Artifact> rslArtifacts, List<Artifact> artifactDependencies) {
 		List<Artifact> cleanArtifacts = new ArrayList<Artifact>();
 		artifacts: for (Artifact artifact : artifactDependencies) {
 			for (Artifact rslArtifact : rslArtifacts) {
 				if (artifact.getGroupId().equals(rslArtifact.getGroupId())
 						&& artifact.getArtifactId().equals(
 								rslArtifact.getArtifactId())
 						&& artifact.getType().equals(rslArtifact.getType())) {
 					cleanArtifacts.add(rslArtifact);
 					continue artifacts;
 				}
 			}
 		}
 		return cleanArtifacts;
 	}
 
 	/**
 	 * Gets RslPolicyFileUrls for given artifact
 	 *
 	 * @param artifact
 	 * @return Array of urls
 	 */
 	private String[] getRslPolicyFileUrls(Artifact artifact) {
 		String[] domains = new String[policyFileUrls.length];
 		for (int i = 0; i < policyFileUrls.length; i++) {
 			String domain = policyFileUrls[i];
 			if (contextRoot != null) {
 				domain = domain.replace("{contextRoot}", contextRoot);
 			}
 			domain = domain.replace("{groupId}", artifact.getGroupId());
 			domain = domain.replace("{artifactId}", artifact.getArtifactId());
 			domain = domain.replace("{version}", artifact.getVersion());
 			domains[i] = domain;
 		}
 		return domains;
 	}
 
 	/**
 	 * Get RslUrls
 	 *
 	 * @param artifact
 	 * @param extension
 	 * @return Array of url's
 	 */
 	private String[] getRslUrls(Artifact artifact, String extension) {
 		String[] rsls = new String[rslUrls.length];
 		for (int i = 0; i < rslUrls.length; i++) {
 			String rsl = rslUrls[i];
 			if (contextRoot != null) {
 				rsl = rsl.replace("{contextRoot}", contextRoot);
 			}
 			rsl = rsl.replace("{groupId}", artifact.getGroupId());
 			rsl = rsl.replace("{artifactId}", artifact.getArtifactId());
 			rsl = rsl.replace("{version}", artifact.getVersion());
 			rsl = rsl.replace("{extension}", extension);
 			rsls[i] = rsl;
 		}
 		return rsls;
 	}
 
 	/**
 	 * Get Fonts snapshot
 	 *
 	 * @return File of font snapshot
 	 * @throws MojoExecutionException
 	 */
 	protected File getFontsSnapshot() throws MojoExecutionException {
 		if (fonts != null && fonts.getLocalFontsSnapshot() != null) {
 			return fonts.getLocalFontsSnapshot();
 		} else {
 			getLog().debug("No fonts snapshot found, generating one!");
 			return MavenUtils.getFontsFile(build);
 		}
 	}
 
 	/**
 	 * Get resource bundles
 	 *
 	 * @return Array of resource bundle files
 	 * @throws MojoExecutionException
 	 */
 	protected File[] getResourcesBundles() throws MojoExecutionException {
 		List<File> resouceBundles = new ArrayList<File>();
 		for (Artifact artifact : getDependencyArtifacts()) {
 			if ("resource-bundle".equals(artifact.getType())) {
 				resouceBundles.add(artifact.getFile());
 			}
 		}
 		return resouceBundles.toArray(new File[resouceBundles.size()]);
 	}
 
 	/**
 	 * Get array of files for dependency artfacts for given scope
 	 *
 	 * @param scope
 	 *            for which to get files
 	 * @return Array of dependency artifact files
 	 * @throws MojoExecutionException
 	 */
 	protected File[] getDependenciesPath(String scope)
 			throws MojoExecutionException {
 		if (scope == null)
 			return null;
 
 		List<File> files = new ArrayList<File>();
 		for (Artifact a : getDependencyArtifacts(scope)) {
 			// https://bugs.adobe.com/jira/browse/SDK-15073
 			// Workaround begin
 			files.add(MavenUtils.getArtifactFile(a, scope, build));
 			// Workaround end
 			// files.add(a.getFile());
 		}
 		return files.toArray(new File[files.size()]);
 	}
 
 	/**
 	 * Perform actions after compilation has run
 	 */
 	@Override
 	protected void tearDown() throws MojoExecutionException,
 			MojoFailureException {
 		if (isSetProjectFile) {
 			project.getArtifact().setFile(outputFile);
 		}
 		Report report = builder.getReport();
 		if (linkReport) {
 			writeLinkReport(report);
 		}
 		if (configurationReport) {
 			writeConfigurationReport(report);
 		}
 		if (mergeResourceBundle != null && !mergeResourceBundle) {
 			writeResourceBundle(report);
 		}
 
 	}
 
 	/**
 	 * Write a resource bundle
 	 *
 	 * @param report
 	 *            from which to obtain info about resource bundle
 	 * @throws MojoExecutionException
 	 */
 	private void writeResourceBundle(Report report)
 			throws MojoExecutionException {
 		getLog().info("Compiling resources bundles!");
 		if (locales == null || locales.length == 0) {
 			getLog()
 					.warn("Resource-bundle generation fail: No locale defined.");
 			return;
 		}
 
 		String[] bundles = report.getResourceBundleNames();
 
 		if (bundles == null || bundles.length == 0) {
 			getLog()
 					.warn(
 							"Resource-bundle generation fail: No resource-bundle found.");
 			return;
 		}
 
 		for (String locale : locales) {
 			getLog().info("Generating resource-bundle for " + locale);
 			File localePath = MavenUtils.getLocaleResourcePath(
 					resourceBundlePath, locale);
 
 			if (!localePath.exists()) {
 				getLog().error(
 						"Unable to find locales path: "
 								+ localePath.getAbsolutePath());
 				continue;
 			}
 			writeResourceBundle(bundles, locale, localePath);
 		}
 	}
 
 	/**
 	 * Write resource bundle
 	 *
 	 * @param bundles
 	 * @param locale
 	 * @param localePath
 	 * @throws MojoExecutionException
 	 */
 	protected abstract void writeResourceBundle(String[] bundles,
 			String locale, File localePath) throws MojoExecutionException;
 
 	/**
 	 * Configure warnings
 	 *
 	 * @param cfg
 	 *            Configuration instance to configure
 	 */
 	private void configureWarnings(Configuration cfg) {
 		cfg.showActionScriptWarnings(showWarnings);
 		cfg.showBindingWarnings(warnigs.getBinding());
 		cfg.showDeprecationWarnings(warnigs.getDeprecation());
 		cfg.showShadowedDeviceFontWarnings(warnigs.getShadowedDeviceFont());
 		cfg.showUnusedTypeSelectorWarnings(warnigs.getUnusedTypeSelector());
 		cfg.checkActionScriptWarning(Configuration.WARN_ARRAY_TOSTRING_CHANGES,
 				warnigs.getArrayTostringChanges());
 		cfg.checkActionScriptWarning(
 				Configuration.WARN_ASSIGNMENT_WITHIN_CONDITIONAL, warnigs
 						.getAssignmentWithinConditional());
 		cfg.checkActionScriptWarning(Configuration.WARN_BAD_ARRAY_CAST, warnigs
 				.getBadArrayCast());
 		cfg.checkActionScriptWarning(Configuration.WARN_BAD_BOOLEAN_ASSIGNMENT,
 				warnigs.getBadBooleanAssignment());
 		cfg.checkActionScriptWarning(Configuration.WARN_BAD_DATE_CAST, warnigs
 				.getBadDateCast());
 		cfg.checkActionScriptWarning(Configuration.WARN_BAD_ES3_TYPE_METHOD,
 				warnigs.getBadEs3TypeMethod());
 		cfg.checkActionScriptWarning(Configuration.WARN_BAD_ES3_TYPE_PROP,
 				warnigs.getBadEs3TypeProp());
 		cfg.checkActionScriptWarning(Configuration.WARN_BAD_NAN_COMPARISON,
 				warnigs.getBadNanComparison());
 		cfg.checkActionScriptWarning(Configuration.WARN_BAD_NULL_ASSIGNMENT,
 				warnigs.getBadNullAssignment());
 		cfg.checkActionScriptWarning(Configuration.WARN_BAD_NULL_COMPARISON,
 				warnigs.getBadNullComparison());
 		cfg.checkActionScriptWarning(
 				Configuration.WARN_BAD_UNDEFINED_COMPARISON, warnigs
 						.getBadUndefinedComparison());
 		cfg.checkActionScriptWarning(
 				Configuration.WARN_BOOLEAN_CONSTRUCTOR_WITH_NO_ARGS, warnigs
 						.getBooleanConstructorWithNoArgs());
 		cfg.checkActionScriptWarning(Configuration.WARN_CHANGES_IN_RESOLVE,
 				warnigs.getChangesInResolve());
 		cfg.checkActionScriptWarning(Configuration.WARN_CLASS_IS_SEALED,
 				warnigs.getClassIsSealed());
 		cfg.checkActionScriptWarning(Configuration.WARN_CONST_NOT_INITIALIZED,
 				warnigs.getConstNotInitialized());
 		cfg.checkActionScriptWarning(
 				Configuration.WARN_CONSTRUCTOR_RETURNS_VALUE, warnigs
 						.getConstructorReturnsValue());
 		cfg.checkActionScriptWarning(
 				Configuration.WARN_DEPRECATED_EVENT_HANDLER_ERROR, warnigs
 						.getDeprecatedEventHandlerError());
 		cfg.checkActionScriptWarning(
 				Configuration.WARN_DEPRECATED_FUNCTION_ERROR, warnigs
 						.getDeprecatedFunctionError());
 		cfg.checkActionScriptWarning(
 				Configuration.WARN_DEPRECATED_PROPERTY_ERROR, warnigs
 						.getDeprecatedPropertyError());
 		cfg.checkActionScriptWarning(
 				Configuration.WARN_DUPLICATE_ARGUMENT_NAMES, warnigs
 						.getDuplicateArgumentNames());
 		cfg.checkActionScriptWarning(Configuration.WARN_DUPLICATE_VARIABLE_DEF,
 				warnigs.getDuplicateVariableDef());
 		cfg.checkActionScriptWarning(Configuration.WARN_FOR_VAR_IN_CHANGES,
 				warnigs.getForVarInChanges());
 		cfg.checkActionScriptWarning(Configuration.WARN_IMPORT_HIDES_CLASS,
 				warnigs.getImportHidesClass());
 		cfg.checkActionScriptWarning(Configuration.WARN_INSTANCEOF_CHANGES,
 				warnigs.getInstanceOfChanges());
 		cfg.checkActionScriptWarning(Configuration.WARN_INTERNAL_ERROR, warnigs
 				.getInternalError());
 		cfg.checkActionScriptWarning(Configuration.WARN_LEVEL_NOT_SUPPORTED,
 				warnigs.getLevelNotSupported());
 		cfg.checkActionScriptWarning(Configuration.WARN_MISSING_NAMESPACE_DECL,
 				warnigs.getMissingNamespaceDecl());
 		cfg.checkActionScriptWarning(Configuration.WARN_NEGATIVE_UINT_LITERAL,
 				warnigs.getNegativeUintLiteral());
 		cfg.checkActionScriptWarning(Configuration.WARN_NO_CONSTRUCTOR, warnigs
 				.getNoConstructor());
 		cfg.checkActionScriptWarning(
 				Configuration.WARN_NO_EXPLICIT_SUPER_CALL_IN_CONSTRUCTOR,
 				warnigs.getNoExplicitSuperCallInConstructor());
 		cfg.checkActionScriptWarning(Configuration.WARN_NO_TYPE_DECL, warnigs
 				.getNoTypeDecl());
 		cfg.checkActionScriptWarning(
 				Configuration.WARN_NUMBER_FROM_STRING_CHANGES, warnigs
 						.getNumberFromStringChanges());
 		cfg.checkActionScriptWarning(Configuration.WARN_SCOPING_CHANGE_IN_THIS,
 				warnigs.getScopingChangeInThis());
 		cfg.checkActionScriptWarning(
 				Configuration.WARN_SLOW_TEXTFIELD_ADDITION, warnigs
 						.getSlowTextFieldAddition());
 		cfg.checkActionScriptWarning(
 				Configuration.WARN_UNLIKELY_FUNCTION_VALUE, warnigs
 						.getUnlikelyFunctionValue());
 		cfg.checkActionScriptWarning(Configuration.WARN_XML_CLASS_HAS_CHANGED,
 				warnigs.getXmlClassHasChanged());
 	}
 
 	/**
 	 * Writes configuration report to file
 	 *
 	 * @param report
 	 *            contains info to write
 	 * @throws MojoExecutionException
 	 *             throw if an error occurs during writing of report to file
 	 */
 	private void writeLinkReport(Report report) throws MojoExecutionException {
 
 		writeReport(report, "link");
 	}
 
 	/**
 	 * Writes configuration report to file
 	 *
 	 * @param report
 	 *            contains info to write
 	 * @throws MojoExecutionException
 	 *             throw if an error occurs during writing of report to file
 	 */
 	private void writeConfigurationReport(Report report)
 			throws MojoExecutionException {
 
 		writeReport(report, "config");
 	}
 
 	/**
 	 * Writes a report to a file.
 	 *
 	 * @param report
 	 *            Report containing info to write to file
 	 * @param type
 	 *            Type of report to write. Valid types are <code>link</code> and
 	 *            <code>config</code>.
 	 * @throws MojoExecutionException
 	 *             throw if an error occurs during writing of report to file
 	 */
 	private void writeReport(Report report, String type)
 			throws MojoExecutionException {
 		File fileReport = new File(build.getDirectory(), project
 				.getArtifactId()
 				+ "-" + project.getVersion() + "-" + type + "-report.xml");
 
 		Writer writer = null;
 		try {
 			writer = new FileWriter(fileReport);
 			if ("link".equals(type)) {
 				report.writeLinkReport(writer);
 				linkReportFile = fileReport;
 			} else if ("config".equals(type)) {
 				report.writeConfigurationReport(writer);
 			}
 
 			getLog().info("Written " + type + " report to " + fileReport);
 		} catch (IOException e) {
 			throw new MojoExecutionException(
 					"An error has ocurried while recording " + type + "-report",
 					e);
 		} finally {
 			try {
 				if (null != writer) {
 					writer.flush();
 					writer.close();
 				}
 			} catch (IOException e) {
 				getLog().error("Error while closing writer", e);
 			}
 		}
 
 		projectHelper.attachArtifact(project, "xml", type + "-report",
 				fileReport);
 	}
 
 	protected void build(E builder) throws MojoExecutionException {
 		long bytes;
 		try {
 			getLog().info(
 					"Flex compiler configurations:"
 							+ configuration.toString().replace("--", "\n-"));
 
 			if (incremental && compilationData.exists()) {
 				builder.load(loadCompilationData());
 			}
 			bytes = builder.build(incremental);
 			if (incremental) {
 				if (compilationData.exists()) {
 					compilationData.delete();
 					compilationData.createNewFile();
 				}
 
 				builder.save(saveCompilationData());
 			}
 		} catch (IOException e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		} catch (Exception e) {
 			throw new MojoExecutionException(e.getMessage(), e);
 		}
 		if (bytes == 0) {
 			throw new MojoExecutionException("Error compiling!");
 		}
 	}
 
 }
