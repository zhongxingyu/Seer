 package ecologylab.generic;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Properties;
 
 import ecologylab.generic.ParsedURL;
 import ecologylab.generic.AssetsCache.Assets;
 
 /**
  * An instance of Environment, which is an application, rather than an applet,
  * or a servlet.
  * The Environment mechanism is used to enable the provision of contextual
  * runtime configuration parameter services in a way that is 
  * independent of the deployment structure.
  * 
  * @author Andruid
  */
 public class ApplicationEnvironment
 extends Debug
 implements Environment
 {
 	/**
 	 * Holds properties for use in servicing parameter(String) requests.
 	 */
 	private static Properties properties;
 	
 	/**
 	 * Used for forming codeBase relative ParsedURLs.
 	 * A simulation of the property available in applets.
 	 * The codebase is the address where the java code comes from.
 	 */
 	ParsedURL	codeBase;
 	/**
 	 * Used for forming codeBase relative ParsedURLs.
 	 * A simulation of the property available in applets.
 	 * The docbase is the address where the launching HTML file comes from.
 	 */
 	ParsedURL	docBase;
 
 	
 /**
  * Create an ApplicationEnvironment. Create an empty properties object for application parameters.
  * <p/>
  * @deprecated		This constructor is not recommended!
  * 					Build your application to extend this class.
  * 					Use one of the other constructors.
  */
 	public ApplicationEnvironment()
 	{
 	   properties = new Properties();
 	   Environment.the.set(this);
 	}
 	/**
 	 * Create an ApplicationEnvironment. Create an empty properties object for application parameters.
 	 * <p/>
 	 * No command line argument is processed. No properties file is loaded.
 	 *  
 	 * @param applicationName	Name of the application. Used to set the applicationName, applicationDir, and .
 	 */
 	public ApplicationEnvironment(String applicationName)
 	{
 	   this(null, applicationName, null, null, null);
 	}
 	
 	/**
 	 * Create an ApplicationEnvironment.
 	 * Load Properties from a properties file found in the config/ directory.
 	 * If there is a 0th command line argument, that is the name of the
 	 * properties file. Otherwise, the properties come from
 	 * config/interface/params.txt.
 	 *  
 	 * @param propertiesFileRelativePath	Path to the properties file, relative to codeBase().
 	 */
 	public ApplicationEnvironment(String applicationName, String args[])
 	{
 	   this(null, applicationName, args);
 	}
 	/**
 	 * Create an ApplicationEnvironment.
 	 * Get the base for finding the path to the "codeBase" by using the
 	 * package path of the baseClass passed in.
 	 * <p/>
 	 * Load Properties from a properties file found in the config/ directory.
 	 * If there is a 0th command line argument, that is the name of the
 	 * properties file. Otherwise, the properties come from
 	 * config/interface/params.txt.
 	 * <p/>
 	 * Also, sets the Assets cacheRoot to the applicationDir().
 	 *  
 	 * @param baseClass			Used for computing codeBase property.
 	 * @param args				The following command line parameters are recognized:
 	 * 								0: parameter file name (found in user.dir/config/)
 	 * 								1: graphics_device (screen number)
 	 * 								2: screen_size (used in TopLevel --
 	 * 									1 - quarter; 2 - almost half; 3; near full; 4 full)
 	 */
 	public ApplicationEnvironment(Class baseClass, String applicationName, String args[])
 	{
 	   this(baseClass, applicationName, parameterFileRelativeFromArg0(args),
 			   ((args.length >= 2) ? args[1] : null),
 			   ((args.length >= 3) ? args[2] : null));
 	}
 	
 	/**
 	 * Create an ApplicationEnvironment.
 	 * Load properties from the properties file specified here.
 	 * <p/>
 	 * Also, sets the Assets cacheRoot to the applicationDir().
 	 * 
 	 * @param baseClass			Used for computing codeBase property.
 	 * @param propertiesFileRelativePath	Path to the properties file, relative to codeBase().
 	 * @param graphicsDev		graphics_device (screen number) to display window. count from 0.
 	 * @param screenSize		used in TopLevel --
 	 * 								1 - quarter; 2 - almost half; 3; near full; 4 full
 	 */
 	public ApplicationEnvironment(Class baseClass, String applicationName, String propertiesFileRelativePath, 
 			String graphicsDev, String screenSize) 
 	{
 		Environment.the.set(this);
 
 		// setup codeBase
 		if (baseClass == null)
 			baseClass			= this.getClass();
 		Package basePackage		= baseClass.getPackage();
 		String packageName		= basePackage.getName();
 		String packageNameAsPath= packageName.replace('.', Files.sep);
 
 		String pathName			= System.getProperty("user.dir") + Files.sep;
 		File path				= new File(pathName);
 		String pathString		= path.getAbsolutePath();
 		
 		//println("looking for " + packageNameAsPath +" in " + pathString);
 
 		int packageIndex		= pathString.lastIndexOf(packageNameAsPath);
 		if (packageIndex != -1)
 		{
 			pathString			= pathString.substring(0, packageIndex);
 			path				= new File(pathString + Files.sep);
 		}
 
 		codeBase				= new ParsedURL(path);
 		println("codeBase="+codeBase);
 		if (propertiesFileRelativePath != null)
 		{
 			println("Loading from codeBase " + path +"\tproperties " + propertiesFileRelativePath);
 			/*
 			//TODO need to move this up so that we never treat a URL as a file
 			//check to see if this is really a URL that was passed (cut off '/config') GHETTO!!!
 			String possibleURLPath = propertiesFileRelativePath.trim().substring(7).toLowerCase();
 			if (possibleURLPath.startsWith("http"))
 				loadPropertiesURL(possibleURLPath);
 			else*/
 				loadProperties(path, propertiesFileRelativePath);
 		}
 		else
 			properties			= new Properties();
 		
 		if (graphicsDev != null)
 			setProperty("graphics_device", graphicsDev);
 
 		if (screenSize != null)
 			setProperty("screen_size", screenSize);
 		
 		PropertiesAndDirectories.setApplicationName(applicationName);
 //		PropertiesAndDirectories.thisApplicationDir();
 	}
 	
 	public void loadProperties(File path, String filename)
 	{
 		try 
 		{
 			properties = new Properties();
 
 			File file = new File(path, filename);
 			println("Loading Properties file from: "+file);
 			properties.load(new FileInputStream(file));
 		} catch (FileNotFoundException e)
 		{
 			e.printStackTrace();
 		} catch (IOException e)
 		{
 			e.printStackTrace();
 		}
 	}
 	public void loadPropertiesURL(String urlString)
 	{
 		try 
 		{
 			properties = new Properties();
 			URL propertiesURL = new URL(urlString);
 			
 			println("Loading Properties file from URL: " + propertiesURL);
 			properties.load(propertiesURL.openStream());
 		} 
 		catch (Exception e)
 		{
 			System.err.println("Error loading properties file from URL: " + urlString);
 			e.printStackTrace();
 		}
 	}
 	
 	public void setProperty(String propertyName, String propertyValue)
 	{
 		properties.setProperty(propertyName, propertyValue);
 	}
     /**
      * @see ecologylab.generic.Environment#runtimeEnv()
      */
     public int runtimeEnv()
     { return APPLICATION;}
     
     /**
      * @see ecologylab.generic.Environment#showStatus(String)
      */
     public void showStatus(String s) 
     {
 	System.out.println(s);
     }
 
 	/**
 	 * @see ecologylab.generic.Environment#status(String)
 	 */
 	public void status(String msg) 
 	{
 	if (msg != null)
 	    showStatus(msg);
 	}
 
 	/**
 	 * @see ecologylab.generic.Environment#parameter(String)
 	 */
 	public String parameter(String name)
 	{
 		return properties.getProperty(name);
 	}
 
 	/**
 	 * @see ecologylab.generic.Environment#codeBase()
 	 * return the path to root of the
 	 */
 	public ParsedURL codeBase() 
 	{
 		return codeBase;
 	}
 
 	/**
 	 * @see ecologylab.generic.Environment#docBase()
 	 * return the current working directory of the application
 	 * which is "c:\web\code\java\cm"
 	 */
 	public ParsedURL docBase() {
 		ParsedURL purl = new ParsedURL(new File(System.getProperty("user.dir")));
 		return purl;
 	}
 	
 	static final String FIREFOX_PATH_WINDOWS	= "C:\\Program Files\\Mozilla Firefox\\firefox.exe";
	static final String IE_PATH_WINDOWS			= "C:\\Program Files\\Internet Explorer\\IEXPLORE.EXE";
	
 	static File	firefoxFileCache;
 	
 	static File getFirefoxFile()
 	{
 		File result		= firefoxFileCache;
 		if (result == null)
 		{
 			result		= new File(FIREFOX_PATH_WINDOWS);
 			firefoxFileCache	= result;
 		}
 		return result;
 	}
 	
 	public void go(ParsedURL purl, String frame)
 	{
 		int os		= PropertiesAndDirectories.os();
 		String cmd	= "";
 		switch (os)
 		{
 		case PropertiesAndDirectories.WINDOWS:
 			File firefoxFile	= getFirefoxFile();
			cmd	= (Generic.parameterBool("navigate_with_ie") || !firefoxFile.exists()) ? IE_PATH_WINDOWS : FIREFOX_PATH_WINDOWS; 
 			cmd	+= " " + purl; //" \"" + purl + "\"";
 			Debug.println(cmd);
 			try {
 					Process p = Runtime.getRuntime().exec(cmd);
 				} catch (IOException e)
 				{
 					println("ERROR in go(); caught exception: ");
 					e.printStackTrace();
 				}
 			break;
 		default:
 			println("go(ParsedURL) not supported for os " + PropertiesAndDirectories.getOsName());
 			break;				
 		}
 	}
 
 	public int browser()
 	{
 	   return APPLICATION;
 	}
     /**
      * Called at the end of an invocation. Calls System.exit(code).
      * 
      * @param	code -- 0 for normal. other values are application specific.
      */
     public void exit(int code)
     {
     	System.exit(code);
     }
     
     //---------------------- not Environment Code - other -----------------------------------
 	/**
 	 * Form the parameter file path.
 	 * Use the 0th argument if there is one, to find a file in config.
 	 * If not, use config/interface/paramts.txt.
 	 * 
 	 * @param args
 	 * @return
 	 */
 	public static String parameterFileRelativeFromArg0(String[] args) 
 	{
 		return (args.length >= 1) ? "config/preferences/" + args[0] : null;
 	}
 
 	/**
 	 * Set the codebase for the application.
 	 * Should only be done at startup.
 	 */
 	public void setCodeBase(ParsedURL codeBase) 
 	{
 		this.codeBase = codeBase;
 	}
 
 }
