 package net.frontlinesms.build.jet.compile;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.frontlinesms.build.jet.PropertyUtils;
 
 
 public class JetCompileProfile {
 //> PROPERTY SUBSTITUTION KEYS
 	private static final String PROP_JPN_PATH = "jpn.path";
 	private static final String PROP_OUTPUT_NAME = "outputName";
 	private static final String PROP_JAVA_MAIN_CLASS = "java.mainClass";
 	private static final String PROP_SPLASH_IMAGE_PATH = "splashImage.path";
 	private static final String PROP_VERSION_INFO_PRODUCT_NAME = "versionInfo.productName";
 	private static final String PROP_VERSION_INFO_COPYRIGHT_OWNER = "versionInfo.copyright.owner";
 	private static final String PROP_VERSION_INFO_COPYRIGHT_YEAR = "versionInfo.copyright.year";
 	private static final String PROP_VERSION_INFO_FILE_DESCRIPTION = "versionInfo.fileDescription";
 	private static final String PROP_VERSION_INFO_COMPANY_NAME = "versionInfo.companyName";
 	private static final String PROP_VERSION_INFO_NUMBER = "versionInfo.number";
 	private static final String PROP_ICON_PATH = "icon.path";
 	
 //> INSTANCE PROPERTIES
 	private final String jpnPath;
 	private final String javaMainClass;
 	private final String outputName;
 	private final String splashImagePath;
 	private final String versionInfoCompanyName;
 	private final String versionInfoFileDescription;
 	private final String versionInfoCopyrightYear;
 	private final String versionInfoCopyrightOwner;
 	private final String versionInfoProductName;
 	private final String versionInfoNumber;
 	private String iconPath;
 	/** This is the directory that all paths in the package configuration are relative to. */
 	private final File compileRootDirectory;
 	
 	private JetCompileProfile(File rootDirectory,
 			String jpnPath, String javaMainClass, String outputName,
 			String splashImagePath, String versionInfoCompanyName,
 			String versionInfoFileDescription, String versionInfoCopyrightYear,
 			String versionInfoCopyrightOwner, String versionInfoProductName, String versionInfoNumber) {
 		this.compileRootDirectory = rootDirectory;
 		this.jpnPath = jpnPath;
 		// Java Main Class must have dots in package name replaced with forward slashes.
 		this.javaMainClass = javaMainClass.replace('.', '/');
 		this.outputName = outputName;
 		this.splashImagePath = splashImagePath;
 		this.versionInfoCompanyName = versionInfoCompanyName;
 		this.versionInfoFileDescription = versionInfoFileDescription;
 		this.versionInfoCopyrightYear = versionInfoCopyrightYear;
 		this.versionInfoCopyrightOwner = versionInfoCopyrightOwner;
 		this.versionInfoProductName = versionInfoProductName;
 		this.versionInfoNumber = versionInfoNumber;
 	}
 
 	/** Get the properties to substitute into template.prj */
 	public Map<String, String> getSubstitutionProperties() {
 		HashMap<String, String> props = new HashMap<String, String>();
 		
 		props.put(PROP_JPN_PATH, this.jpnPath); // Path to the JPN file.  Is this the path to create at?  Or is the .jpn required at this point?
 		props.put(PROP_JAVA_MAIN_CLASS, this.javaMainClass); // The main java class to run
 		props.put(PROP_OUTPUT_NAME, this.outputName); // The file name of the built executable
 		props.put(PROP_SPLASH_IMAGE_PATH, getResourcePath(this.splashImagePath)); // The path to the splash image
 		props.put(PROP_VERSION_INFO_COMPANY_NAME, this.versionInfoCompanyName); // Company name, as used in version info
 		props.put(PROP_VERSION_INFO_FILE_DESCRIPTION, this.versionInfoFileDescription); // The name of the project, as used in version info
 		props.put(PROP_VERSION_INFO_COPYRIGHT_YEAR, this.versionInfoCopyrightYear); // The year of the copyright, as used in version info
 		props.put(PROP_VERSION_INFO_COPYRIGHT_OWNER, this.versionInfoCopyrightOwner); // The owner of the copyright, as used in version info
 		props.put(PROP_VERSION_INFO_PRODUCT_NAME, this.versionInfoProductName); // The product name, as used in version info
 		props.put(PROP_VERSION_INFO_NUMBER, this.versionInfoNumber); // The version number, as used in version info
 		props.put(PROP_ICON_PATH, getResourcePath(this.iconPath)); // The path to the icon
 		
 		return props;
 	}
 	
 	/**
 	 * If supplied path is relative, converts it to be relative to the directory
 	 * that this profile is configured from. 
 	 * @param path
 	 * @return
 	 */
 	private String getResourcePath(final String path) {
 		String absolutePath = new File(path).getAbsolutePath();
 		if(absolutePath.equals(path)) {
 			// The path is absolute, so keep it that way
 			return path;
 		} else {
 			return new File(new File(this.compileRootDirectory, "resources"), path).getAbsolutePath();
 		}
 	}
 	
 	public List<String> getModules() {
 		ArrayList<String> modules = new ArrayList<String>();
 		// add icon if it's specified
 		if(this.iconPath != null) {
 			modules.add(this.iconPath);
 		}
 		return modules;
 	}
 	
 //> STATIC FACTORIES
 	public static JetCompileProfile loadFromDirectory(File profileDirectory, File workingDirectory) throws IOException {
 		Map<String, String> props = PropertyUtils.loadProperties(new File(profileDirectory, "compile.profile.properties"));
 		
		JetCompileProfile compileProfile = new JetCompileProfile(new File(workingDirectory, "pack"),
 				props.remove(PROP_JPN_PATH),
 				props.remove(PROP_JAVA_MAIN_CLASS),
 				props.remove(PROP_OUTPUT_NAME),
 				props.remove(PROP_SPLASH_IMAGE_PATH),
 				props.remove(PROP_VERSION_INFO_COMPANY_NAME),
 				props.remove(PROP_VERSION_INFO_FILE_DESCRIPTION),
 				props.remove(PROP_VERSION_INFO_COPYRIGHT_YEAR),
 				props.remove(PROP_VERSION_INFO_COPYRIGHT_OWNER),
 				props.remove(PROP_VERSION_INFO_PRODUCT_NAME),
 				props.remove(PROP_VERSION_INFO_NUMBER));
 		
 		String iconPath = props.remove(PROP_ICON_PATH);
 		if(iconPath != null) compileProfile.iconPath = iconPath;
 		
 		// Check all properties used
 		assert(props.size() == 0) : "There are " + props.size() + " unused properties.";
 		
 		return compileProfile;
 	}
 }
