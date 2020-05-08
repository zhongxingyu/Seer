 package org.prot.appserver.config;
 
 import java.io.IOException;
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 import org.prot.appserver.app.AppInfo;
 
 public class Configuration
 {
 	private static Logger logger = Logger.getLogger(Configuration.class);
 
 	// Singleton
 	private static Configuration configuration;
 
 	// Port under wich the local RMI registry is running
	private int rmiRegistryPort = -1;
 
 	// Enable stdout
 	private boolean enableStdOut = false;
 
 	// Shutdown if Controller is not available
 	private boolean requiresController = true;
 
 	// Application identifier
 	private String appId;
 
 	// Is the app executed by this server a privileged app
 	private boolean privileged = false;
 
 	// If this is a privileged app it must authenticate with the controller
 	private String authenticationToken;
 
 	// Port which is used by the AppServer to publish the app
 	private int appServerPort;
 
 	// Directory which is used to extract the application data (configuration)
 	private String workingDirectory;
 
 	// Directory which holds the current application data
 	private String appDirectory;
 
 	// Directory which is used as scratch dir for the web application
 	private String appScratchDir;
 
 	// Directories with the python libs
 	private String pythonLibs;
 	private String djangoLibs;
 
 	// AppInfo
 	private AppInfo appInfo;
 
 	public static Configuration getInstance()
 	{
 		if (Configuration.configuration == null)
 		{
 			// Create a new Configuration
 			Configuration.configuration = new Configuration();
 
 			// Init the Configuration
 			loadConfiguration(Configuration.configuration);
 		}
 
 		return Configuration.configuration;
 	}
 
 	private static void loadConfiguration(Configuration configuration)
 	{
 		Properties props = new Properties();
 		try
 		{
 			props.load(Configuration.class.getResourceAsStream("/etc/config.properties"));
 			configuration.workingDirectory = props.getProperty("appserver.working.dir");
 			configuration.pythonLibs = props.getProperty("python.lib");
 			configuration.djangoLibs = props.getProperty("python.lib.site-packages");
			configuration.rmiRegistryPort = Integer.parseInt(props.getProperty("rmi.registry.port"));
 		} catch (IOException e)
 		{
 			logger.error("Could not load the configuration properties", e);
 			System.exit(1);
 		} catch (NumberFormatException e)
 		{
 			logger.error("Could not parse the configuration file");
 			System.exit(1);
 		}
 	}
 
 	public void finishConfiugration()
 	{
 		Configuration config = Configuration.configuration;
 		config.setAppDirectory(config.getWorkingDirectory() + "/" + config.getAppId());
 		config.setAppScratchDir("C:/temp/scratch");
 	}
 
 	public String getAppId()
 	{
 		return appId;
 	}
 
 	public void setAppId(String appId)
 	{
 		this.appId = appId;
 	}
 
 	public int getAppServerPort()
 	{
 		return appServerPort;
 	}
 
 	public void setAppServerPort(int appServerPort)
 	{
 		this.appServerPort = appServerPort;
 	}
 
 	public String getWorkingDirectory()
 	{
 		return workingDirectory;
 	}
 
 	public void setWorkingDirectory(String workingDirectory)
 	{
 		this.workingDirectory = workingDirectory;
 	}
 
 	public String getAppDirectory()
 	{
 		return appDirectory;
 	}
 
 	void setAppDirectory(String appDirectory)
 	{
 		this.appDirectory = appDirectory;
 	}
 
 	public String getPythonLibs()
 	{
 		return pythonLibs;
 	}
 
 	public void setPythonLibs(String pythonLibs)
 	{
 		this.pythonLibs = pythonLibs;
 	}
 
 	public String getDjangoLibs()
 	{
 		return djangoLibs;
 	}
 
 	public void setDjangoLibs(String djangoLibs)
 	{
 		this.djangoLibs = djangoLibs;
 	}
 
 	public AppInfo getAppInfo()
 	{
 		return appInfo;
 	}
 
 	public void setAppInfo(AppInfo appInfo)
 	{
 		this.appInfo = appInfo;
 	}
 
 	public boolean isEnableStdOut()
 	{
 		return enableStdOut;
 	}
 
 	public void setEnableStdOut(boolean enableStdOut)
 	{
 		this.enableStdOut = enableStdOut;
 	}
 
 	public boolean isRequiresController()
 	{
 		return requiresController;
 	}
 
 	public void setRequiresController(boolean requiresController)
 	{
 		this.requiresController = requiresController;
 	}
 
 	public boolean isPrivileged()
 	{
 		return privileged;
 	}
 
 	public void setPrivileged(boolean privileged)
 	{
 		this.privileged = privileged;
 	}
 
 	public String getAuthenticationToken()
 	{
 		return authenticationToken;
 	}
 
 	public void setAuthenticationToken(String authenticationToken)
 	{
 		logger.debug("Using authentication token: " + authenticationToken);
 		this.authenticationToken = authenticationToken;
 	}
 
 	public String getAppScratchDir()
 	{
 		return appScratchDir;
 	}
 
 	void setAppScratchDir(String appScratchDir)
 	{
 		this.appScratchDir = appScratchDir;
 	}
 
 	public int getRmiRegistryPort()
 	{
		return rmiRegistryPort;
 	}
 }
