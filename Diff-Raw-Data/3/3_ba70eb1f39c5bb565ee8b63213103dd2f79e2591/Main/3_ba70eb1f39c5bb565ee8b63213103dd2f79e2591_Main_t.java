 package net.sf.testium;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOError;
 import java.net.URISyntaxException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
import java.util.Collections;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.Locale;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import net.sf.testium.configuration.ConfigurationException;
 import net.sf.testium.configuration.GlobalConfigurationXmlHandler;
 import net.sf.testium.configuration.PersonalConfigurationXmlHandler;
 import net.sf.testium.executor.SupportedInterfaceList;
 import net.sf.testium.executor.TestExecutionException;
 import net.sf.testium.plugins.PluginClassLoader;
 import net.sf.testium.plugins.PluginCollection;
 
 import org.testtoolinterfaces.cmdline.CmdLineExecutionParser;
 import org.testtoolinterfaces.cmdline.CmdLineParser;
 import org.testtoolinterfaces.cmdline.ParameterException;
 import org.testtoolinterfaces.testsuite.TestGroup;
 import org.testtoolinterfaces.testsuite.TestInterface;
 import org.testtoolinterfaces.utils.RunTimeData;
 import org.testtoolinterfaces.utils.RunTimeVariable;
 import org.testtoolinterfaces.utils.Trace;
 import org.xml.sax.XMLReader;
 
 
 public class Main
 {
 	public static final String APPLICATIONNAME = "Testium";
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args)
 	{
 		Trace.getInstance().addBaseClass("net.sf.testium");
 		Trace.getInstance().addBaseClass("org.testtoolinterfaces");
 
 		RunTimeData rtData = new RunTimeData();
 
 		defineBaseDir(rtData);
 		defineUserHome(rtData);
 		defineStartTime(rtData);
 
 		parseCommandLine( rtData, args );
 
 		readGlobalConfigFile( rtData );
 		readPersonalConfigFile( rtData );
 
 		PluginCollection plugins = loadPlugins( rtData );
 		Testium testium = createTestium(rtData, plugins);
 
 		String command = rtData.getValueAsString(Testium.COMMAND);
 		int returnValue = 0;
 		if( command.equalsIgnoreCase( CmdLineParser.VALIDATE ) )
 		{
 			doValidation( testium, rtData );
 		}
 		else if( command.equalsIgnoreCase( CmdLineParser.EXECUTE ) )
 		{
 			returnValue = doExecution( testium, rtData );
 		}
 		else if( command.equalsIgnoreCase( CmdLineParser.PREPARE ) )
 		{
 			doPreparations( testium, rtData );
 		}
 		else if( command.equalsIgnoreCase( CmdLineParser.INTERFACES ) )
 		{
 			showInterfaces( plugins );
 		}
 		else if( command.equalsIgnoreCase( CmdLineParser.KEYWORDS ) )
 		{
 			showKeywords( plugins );
 		}
 		else
 		{
 			throw new Error( "Unknown Command: " + command );
 		}
 		
 		System.out.flush();
 		System.err.flush();
 		
 		// Find the root thread group
 //		ThreadGroup root = Thread.currentThread().getThreadGroup().getParent();
 //		System.out.println( "We start with threadgroup: " + root.getName() );
 //
 //		while (root.getParent() != null)
 //		{
 //			root = root.getParent();
 //			System.out.println( "Up one to: " + root.getName() );
 //		}
 //		
 //		visit( root, 0 );
 		
 		System.runFinalization();
 //		try
 //		{
 //			Thread.sleep(30000);
 //		}
 //		catch (InterruptedException e)
 //		{
 //			// none, just exit;
 //		}
 		destroyInterfaces( plugins );
 		System.exit( returnValue ); // Some plugins can have hanging threads. This will stop them.
 	}
 
 //	// This method recursively visits all thread groups under `group'.
 //	public static void visit(ThreadGroup group, int level) {
 //	    // Get threads in `group'
 //	    int numThreads = group.activeCount();
 //	    Thread[] threads = new Thread[numThreads*2];
 //	    numThreads = group.enumerate(threads, false);
 //
 //        System.out.println( "Threadgroup: " + group.getName() );
 //
 //	    // Enumerate each thread in `group'
 //	    for (int i=0; i<numThreads; i++) {
 //	        // Get thread
 //	        Thread thread = threads[i];
 //	        System.out.println( "Thread: " + thread.getName() + "(" + thread.getPriority() + ") has state " + thread.getState() );
 //	    }
 //
 //	    // Get thread subgroups of `group'
 //	    int numGroups = group.activeGroupCount();
 //	    ThreadGroup[] groups = new ThreadGroup[numGroups*2];
 //	    numGroups = group.enumerate(groups, false);
 //
 //	    // Recursively visit each subgroup
 //	    for (int i=0; i<numGroups; i++) {
 //	        visit(groups[i], level+1);
 //	    }
 //	}
 	
 	/**
 	 * @param rtData
 	 */
 	private static void defineBaseDir(RunTimeData anRtData)
 	{
 		Trace.println(Trace.UTIL, "defineBaseDir(  runTimeData )", true );
 
 		File applicationBaseDir = new File( "" );
 		try
 		{
 			File jarFile = new File(  net.sf.testium.Main.class.getProtectionDomain()
 			                         						.getCodeSource()
 			                         						.getLocation()
 			                         						.toURI() );
 			applicationBaseDir = jarFile.getParentFile().getParentFile(); //Assuming testium is in {basedir}/src/Testium.jar
 		}
 		catch (URISyntaxException exc)
 		{
 			Trace.print(Trace.UTIL, exc);
 			throw new Error( "Cannot determine " + APPLICATIONNAME + "'s basedir: ", exc );
 		}
 		
 		String sysPropBaseDir = System.getProperty( Testium.BASEDIR.toLowerCase() );
 		if ( sysPropBaseDir != null && ! sysPropBaseDir.isEmpty() ) {
 			applicationBaseDir = new File( sysPropBaseDir );
 		}
 
 		RunTimeVariable applBaseDirVar = new RunTimeVariable(Testium.BASEDIR, applicationBaseDir);
 		anRtData.add(applBaseDirVar);
 	}
 
 	/**
 	 * @param rtData
 	 */
 	private static void defineUserHome(RunTimeData anRtData)
 	{
 		Trace.println(Trace.UTIL, "defineUserHome(  runTimeData )", true );
 
 		File userHome = new File( System.getProperty("user.home") );
 		RunTimeVariable userHomeVar = new RunTimeVariable(Testium.USERHOME, userHome);
 		anRtData.add(userHomeVar);
 	}
 
 	/**
 	 * @param rtData
 	 */
 	private static void defineStartTime(RunTimeData anRtData)
 	{
 		Trace.println(Trace.UTIL, "defineStartTime(  runTimeData )", true );
 
 		Date now = new Date();
 		
 		RunTimeVariable startTimeVar = new RunTimeVariable(Testium.START, now);
 		anRtData.add(startTimeVar);
 
 		SimpleDateFormat dateFormatter = new SimpleDateFormat( "yyyyMMdd" );
 		RunTimeVariable startDateFormattedVar = new RunTimeVariable(Testium.STARTDATE, dateFormatter.format( now ));
 		anRtData.add(startDateFormattedVar);
 
 		SimpleDateFormat timeFormatter = new SimpleDateFormat( "HHmmss" );
 		RunTimeVariable startTimeFormattedVar = new RunTimeVariable(Testium.STARTTIME, timeFormatter.format( now ));
 		anRtData.add(startTimeFormattedVar);
 	}
 
 	/**
 	 * @param args
 	 * @return
 	 */
 	private static void parseCommandLine(RunTimeData anRtData, String[] args)
 	{
 		Trace.println(Trace.UTIL, "parseCommandLine(  runTimeData, args )", true );
 
 		CmdLineParser cmdLine = new CmdLineExecutionParser( APPLICATIONNAME );
 		cmdLine.setDefaultCommand( "execute" );
 		
 		cmdLine.acceptFlag( Testium.VERSION, "Displays the versions of Testium and the plugins" );
 
 		try 
 		{
 			cmdLine.parse(anRtData, args);
 		}
 		catch (ParameterException pe)
 		{
 			Trace.print(Trace.UTIL, pe);
 			cmdLine.printHelpOn(System.out);
 			throw new Error( "Error on command line.", pe );
 		}
 
 		if( anRtData.getValueAsBoolean( Testium.HELP ) )
 		{
 			cmdLine.printHelpOn(System.out);
 			System.exit(0);
 		}
 
 		if( anRtData.getValueAsBoolean( Testium.VERSION ) )
 		{
 			System.out.println( "The version of Testium" );
 			Package[] packages = Package.getPackages();
 			for ( Package pkg : packages )
 			{
 				String pkgName = pkg.getName();
 				if ( ! pkgName.startsWith("java") &&
 					 ! pkgName.startsWith( "sun" ) &&
 					 ! pkgName.startsWith( "joptsimple" ) &&
 					 ! pkgName.startsWith( "org.xml" ) )
 				{
 					System.out.println( pkgName + ":\t" + pkg.getImplementationVersion() );
 				}
 			}
 
 			System.exit(0);
 		}
 	}
 
 	private static void readGlobalConfigFile(RunTimeData anRtData)
 	{
 		Trace.println(Trace.UTIL, "readConfigFile( runTimeData )", true );
 
 		File globalConfigFile = anRtData.getValueAsFile( Testium.GLOBALCONFIGFILE );
 		if ( globalConfigFile == null )
 		{
 			File configDir = anRtData.getValueAsFile( Testium.CONFIGDIR );
 			if ( configDir == null )
 			{
 				throw new Error( Testium.CONFIGDIR + " is not defined in RunTimeData" );
 			}
 			
 			globalConfigFile = new File( configDir, "global.xml" );
 			RunTimeVariable globalConfigDirVar = new RunTimeVariable(Testium.GLOBALCONFIGFILE, globalConfigFile);
 			anRtData.add(globalConfigDirVar);
 		}
 
 		// Default projectdir is {basedir}/suite
 		File baseDir = anRtData.getValueAsFile( Testium.BASEDIR );
 		File projectDir = new File( baseDir, "suite" );
 		RunTimeVariable rtVarProjectDir = new RunTimeVariable( Testium.PROJECTDIR, projectDir );
 		anRtData.add( rtVarProjectDir );
 
 		// create a parser
         SAXParserFactory spf = SAXParserFactory.newInstance();
         spf.setNamespaceAware(false);
  
 		try
 		{
 			SAXParser saxParser = spf.newSAXParser();
 			XMLReader xmlReader = saxParser.getXMLReader();
 
 	        // create a handler
 			GlobalConfigurationXmlHandler handler = new GlobalConfigurationXmlHandler(xmlReader, anRtData);
 
 	        // assign the handler to the parser
 	        xmlReader.setContentHandler(handler);
 
 	        // parse the document
 	        xmlReader.parse( globalConfigFile.getAbsolutePath() );
 		}
 		catch (Exception e)
 		{
 			Trace.print(Trace.UTIL, e);
 			throw new Error( APPLICATIONNAME + " is not correctly setup.", e );
 		}
 	}
 
 	private static void readPersonalConfigFile(RunTimeData anRtData)
 	{
 		Trace.println(Trace.UTIL, "readPersonalConfigFile(  runTimeData )", true );
 		
 		File configFile = anRtData.getValueAsFile( Testium.CONFIGFILE );
 		if ( configFile == null )
 		{
 			File userHome = anRtData.getValueAsFile( Testium.USERHOME );
 			if ( userHome == null )
 			{
 				throw new Error( Testium.USERHOME + " is not defined in RunTimeData" );
 			}
 
 			File userConfigDir = anRtData.getValueAsFile( Testium.USERCONFIGDIR );
 			if ( userConfigDir == null )
 			{
 				userConfigDir = new File( userHome, "." + APPLICATIONNAME.toLowerCase(Locale.ENGLISH) );
 			}
 			RunTimeVariable userConfigDirVar = new RunTimeVariable(Testium.USERCONFIGDIR, userConfigDir);
 			anRtData.add(userConfigDirVar);
 
 			File defaultConfigFile = anRtData.getValueAsFile( Testium.DEFAULTCONFIGFILE );
 			if ( defaultConfigFile == null )
 			{
 				configFile = new File( userConfigDir, "general.xml" );
 			}
 			else
 			{
 				if ( ! defaultConfigFile.isAbsolute() )
 				{
 					configFile = new File( userConfigDir, defaultConfigFile.getPath() );
 					if ( ! configFile.exists() )
 					{
 						configFile = new File( userHome, defaultConfigFile.getPath() );
 					}
 				}
 				else
 				{
 					configFile = defaultConfigFile;
 				}
 			}
 			
 			RunTimeVariable configFileVar = new RunTimeVariable(Testium.CONFIGFILE, configFile);
 			anRtData.add(configFileVar);
 		}
 		else
 		{
 			File userConfigDir = configFile.getParentFile();
 			RunTimeVariable userConfigDirVar = new RunTimeVariable(Testium.USERCONFIGDIR, userConfigDir);
 			anRtData.add(userConfigDirVar);
 		}
 
 		// create a parser
         SAXParserFactory spf = SAXParserFactory.newInstance();
         spf.setNamespaceAware(false);
  
 		try
 		{
 			SAXParser saxParser = spf.newSAXParser();
 			XMLReader xmlReader = saxParser.getXMLReader();
 	        // create a handler
 			PersonalConfigurationXmlHandler handler = new PersonalConfigurationXmlHandler(xmlReader, anRtData);
 
 	        // assign the handler to the parser
 	        xmlReader.setContentHandler(handler);
 
 	        // parse the document
 	        xmlReader.parse( configFile.getAbsolutePath() );
 		}
 		catch (FileNotFoundException fnfe)
 		{
 			Trace.print(Trace.UTIL, fnfe);
 		}
 		catch (Exception e)
 		{
 			Trace.print(Trace.UTIL, e);
 			throw new Error( APPLICATIONNAME + " could not be configured.", e );
 		}
 	}
 
 	private static PluginCollection loadPlugins( RunTimeData anRtData )
 	{
 		Trace.println(Trace.UTIL, "loadPlugins(  runTimeData )", true );
 
 		File tmpPluginDirectory = anRtData.getValueAsFile( Testium.PLUGINSDIR );
 		File pluginDir;
 		
 		if ( tmpPluginDirectory == null )
 		{
 			File baseDir = anRtData.getValueAsFile( Testium.BASEDIR );
 			if (baseDir == null)
 			{
 				throw new Error( Testium.BASEDIR + " is not set in RunTimeData" );
 			}
 			pluginDir = new File( baseDir, "plugins" );
 		}
 		else
 		{
 			String tmpPluginDirPath = anRtData.substituteVars(tmpPluginDirectory.getPath());
 			pluginDir = new File( tmpPluginDirPath );
 		}
 
 		RunTimeVariable pluginDirVar = new RunTimeVariable( Testium.PLUGINSDIR, pluginDir);
 		anRtData.add(pluginDirVar);
 
 		if ( ! pluginDir.isDirectory() )
 		{
 			throw new Error( "Plugin Directory is not found: " + pluginDir.getAbsolutePath() );
 		}
 	
 		Trace.println(Trace.UTIL, "Loading plugins from: " + pluginDir.getAbsolutePath(), true );
 		PluginCollection plugins;
 		try
 		{
 			plugins = PluginClassLoader.loadPlugins( pluginDir, anRtData );
 		}
 		catch (ConfigurationException e)
 		{
 			Trace.print(Trace.UTIL, e);
 			throw new Error( "Failed to load plugins: " + e.getMessage(), e );
 		}
 	
 		return plugins;
 	}
 
 	/**
 	 * @param anRtData
 	 * @param plugins
 	 * @return
 	 */
 	private static Testium createTestium(RunTimeData anRtData, PluginCollection plugins)
 	{
 		Trace.println(Trace.UTIL, "createTestium( runTimeData, plugins )", true );
 		Testium testium;
 		try
 		{
 			testium = new Testium( plugins, anRtData );
 		}
 		catch (ConfigurationException e)
 		{
 			Trace.print(Trace.UTIL, e);
 			throw new Error( APPLICATIONNAME + " could not be started.", e );
 		}
 		return testium;
 	}
 
 	/**
 	 * @param testium
 	 * @param tgFile
 	 * @return
 	 * @throws Error
 	 */
 	private static TestGroup readTestGroup(Testium testium, RunTimeData anRtData)
 			throws Error
 	{
 		Trace.println(Trace.UTIL, "readTestGroup( Testium, runTimeData )", true );
 
 		File tgFile = anRtData.getValueAsFile(Testium.TESTFILE);
 		TestGroup testGroup;
 		try
 		{
 			testGroup = testium.readTestGroup( tgFile );
 		}
 		catch (IOError e)
 		{
 			Trace.print(Trace.UTIL, e);
 			throw new Error( "TestGroup could not be read: " + tgFile.getPath(), e );
 		}
 		return testGroup;
 	}
 
 	private static void doValidation(Testium aTestium, RunTimeData anRtData)
 	{
 		Trace.println(Trace.EXEC, "doValidation( Testium, runTimeData )", true );
 		readTestGroup(aTestium, anRtData);
 	}
 
 	/**
 	 * @param testium
 	 * @param rtData
 	 * @throws Error
 	 */
 	private static int doExecution( Testium aTestium, RunTimeData anRtData )
 			throws Error
 	{
 		Trace.println(Trace.EXEC, "doExecution( Testium, runTimeData )", true );
 		TestGroup testGroup = readTestGroup(aTestium, anRtData);
 		File testSuiteDir = anRtData.getValueAsFile(Testium.PROJECTDIR);
 		if ( testSuiteDir == null )
 		{
 			throw new Error( "Test Suite Directory is not defined" );
 		}
 		if ( ! testSuiteDir.isDirectory() )
 		{
 			throw new Error( "Test Suite Directory is not found: " + testSuiteDir.getPath() );
 		}
 
 		try
 		{
 			return aTestium.execute( testGroup, testSuiteDir, anRtData );
 		}
 		catch (TestExecutionException e)
 		{
 			Trace.print(Trace.EXEC, e);
 			throw new Error( "Execution failed.", e );
 		}
 	}
 
 	/**
 	 * @param rtData
 	 * @param testium
 	 * @param tgFile
 	 * @param testGroup
 	 * @throws Error
 	 */
 	private static void doPreparations( Testium aTestium, RunTimeData anRtData ) throws Error
 	{
 		Trace.println(Trace.EXEC, "doPreparations( Testium, runTimeData )", true );
 
 		TestGroup testGroup = readTestGroup(aTestium, anRtData);
 		File testSuiteDir = anRtData.getValueAs(File.class, Testium.PROJECTDIR);
 
 		String groupId = anRtData.getValueAs(String.class, Testium.TESTGROUP);
 		String caseId = anRtData.getValueAs(String.class, Testium.TESTCASE);
 		
 		if ( groupId == null && caseId == null )
 		{
 			throw new Error( "Preparation failed: Must specify a testgroup or testcase to prepare for." );
 		}
 
 		try
 		{
 			aTestium.prepare( testGroup, testSuiteDir, anRtData );
 		}
 		catch (TestExecutionException e)
 		{
 			Trace.print(Trace.EXEC, e);
 			throw new Error( "Preparation failed.", e );
 		}
 	}
 
 	/**
 	 * @param aPlugins
 	 */
 	private static void showInterfaces( PluginCollection aPlugins )
 	{
 		Trace.println(Trace.EXEC, "showInterfaces( aPlugins )", true );
 
 		System.out.println( "Supported interfaces:" );
 		SupportedInterfaceList interfaceList = aPlugins.getInterfaces();
 		for (Enumeration<String> keys = interfaceList.getInterfaceNames(); keys.hasMoreElements();)
 	    {
 			String ifaceName = keys.nextElement();
 	    	System.out.println( "  " + ifaceName );
 	    }
 	}
 
 	private static void destroyInterfaces(PluginCollection aPlugins)
 	{
 		Trace.println(Trace.EXEC, "destroyInterfaces( rtData )", true );
 
 		SupportedInterfaceList interfaceList = aPlugins.getInterfaces();
 		Iterator<TestInterface> iFaceItr = interfaceList.iterator();
 		while(iFaceItr.hasNext())
 		{
 			TestInterface iFace = iFaceItr.next();
 			iFace.destroy();
 		}
 	}
 
 	/**
 	 * @param aTestium
 	 * @param anRtData
 	 * @throws Error
 	 */
 	private static void showKeywords( PluginCollection aPlugins )
 	{
 		Trace.println(Trace.EXEC, "showKeywords( aPlugins )", true );
 
 		System.out.println( "Supported keywords:" );
 		SupportedInterfaceList interfaceList = aPlugins.getInterfaces();
 		Iterator<TestInterface> iFaceItr = interfaceList.iterator();
 		while(iFaceItr.hasNext())
 		{
 			TestInterface iFace = iFaceItr.next();
 			System.out.println( "  " + iFace.getInterfaceName() );
 
 			ArrayList<String> commandList = iFace.getCommands();
			Collections.sort(commandList);
 		    for (String command : commandList)
 		    {
 		    	System.out.println( "    " + command );
 		    }
 		    
 		    System.out.println();
 		}
 	}
 }
