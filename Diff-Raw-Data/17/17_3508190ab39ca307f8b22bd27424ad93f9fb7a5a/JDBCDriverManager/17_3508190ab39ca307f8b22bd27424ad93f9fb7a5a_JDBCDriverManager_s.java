 /*******************************************************************************
  * Copyright (c) 2004, 2005 Actuate Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *  Actuate Corporation  - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.birt.report.data.oda.jdbc;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.sql.Connection;
 import java.sql.Driver;
 import java.sql.DriverManager;
 import java.sql.DriverPropertyInfo;
 import java.sql.SQLException;
 import java.util.HashSet;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.eclipse.birt.data.oda.OdaException;
 import org.eclipse.birt.data.oda.util.driverconfig.ConfigManager;
 import org.eclipse.birt.data.oda.util.driverconfig.OdaDriverConfiguration;
 
 /**
  * Utility classs that manages the JDBC drivers available to this bridge driver.
  * Deals with dynamic discovery of JDBC drivers and some annoying class loader
  * issues. 
  * This class is not to be instantiated by the user. Use the getInstance() method
  * to obtain an instance
  */
 public class JDBCDriverManager
 {
 	// Driver classes that we have registered with JDBC DriverManager
 	private  HashSet registeredDrivers = new HashSet();
 	
 	private  DriverClassLoader extraDriverLoader = null;
 	
 	private static JDBCDriverManager instance;
 	
 	private static Logger logger = Logger.getLogger( JDBCDriverManager.class.getName() );
 	
 	private JDBCDriverManager()
 	{
 		logger.logp( java.util.logging.Level.FINE,
 				JDBCConnectionFactory.class.getName( ),
 				"JDBCDriverManager",
 				"JDBCDriverManager starts up" );
 	}
 	
 	public static JDBCDriverManager getInstance()
 	{
 		if ( instance == null )
 			instance = new JDBCDriverManager();
 		return instance;
 	}
 	
 	/**
 	 * Gets a JDBC connection 
 	 * @param driverClass Class name of JDBC driver
 	 * @param url Connection URL
 	 * @param connectionProperties Properties for establising connection
 	 * @return new JDBC Connection
 	 * @throws SQLException
 	 */
 	public Connection getConnection( String driverClass, String url, 
 			Properties connectionProperties ) throws SQLException, ClassNotFoundException
 	{
 		if ( url == null )
 			throw new NullPointerException("getConnection: url is null ");
 		if ( logger.isLoggable( Level.FINE ))
 			logger.fine("Request JDBC Connection: driverClass=" + 
 					(driverClass == null? "" : driverClass) + "; url=" + url);
 		loadAndRegisterDriver(driverClass);
 		return DriverManager.getConnection( url, connectionProperties );
 	}
 
 	/**
 	 * Gets a JDBC connection 
 	 * @param driverClass Class name of JDBC driver
 	 * @param url Connection URL
 	 * @param user connection user name
 	 * @param password connection password
 	 * @return new JDBC connection
 	 * @throws SQLException
 	 */
 	public  Connection getConnection( String driverClass, String url, 
 			String user, String password ) throws SQLException, ClassNotFoundException
 	{
 		if ( url == null )
 			throw new NullPointerException("getConnection: url is null ");
 		if ( logger.isLoggable( Level.FINE ))
 			logger.fine("Request JDBC Connection: driverClass=" + 
 					(driverClass == null? "" : driverClass) + "; url=" + url + 
 					"; user=" + ((user == null) ? "" : user));
 		loadAndRegisterDriver(driverClass);
 		return DriverManager.getConnection( url, user, password );
 	}
 	
 	private  boolean loadAndRegisterDriver( String className ) 
 		throws ClassNotFoundException
 	{
 		if ( className == null || className.length() == 0)
 			return false;
 		
 		Class driverClass = null;
 		if ( registeredDrivers.contains( className ) )
 			// Driver previously loaded successfully
 			return true;
 
 		if ( logger.isLoggable( Level.INFO ))
 		{
 			logger.info( "Loading JDBC driver class: " + className );
 		}
 		
 		boolean driverInClassPath = false;
 		try
 		{
 			driverClass = Class.forName( className );
 			// Driver class in class path
 			logger.info( "Loaded JDBC driver class in class path: " + className );
 			driverInClassPath = true;
 		}
 		catch ( ClassNotFoundException e )
 		{
 			if ( logger.isLoggable( Level.FINE ))
 			{
 				logger.info( "Driver class not in class path: " + className +
 						". Trying to locate driver in drivers directory");
 			}
 				
 			// Driver not in plugin class path; find it in drivers directory
 			driverClass = loadExtraDriver( className, true );
 				
 			// if driver class still cannot be found, 
 			if( driverClass == null)
 			{
 				logger.warning( "Failed to load JDBC driver class: " + className );
 				return false;
 			}
 		}
 
 		// If driver is found in the drivers directory, its class is not accessible
 		// in this class's ClassLoader. DriverManager will not allow this class to create
 		// connections using such driver. To solve the problem, we create a wrapper Driver in 
 		// our class loader, and register it with DriverManager
 		if ( ! driverInClassPath )
 		{
 			Driver driver = null;
 			try
 			{
 				driver = (Driver) driverClass.newInstance( );
 			}
 			catch ( Exception e )
 			{
 				logger.log( Level.WARNING, "Failed to create new instance of JDBC driver:" + className, e);
 				return false;
 			}
 
 			try
 			{
 				if (logger.isLoggable(Level.FINER))
 					logger.finer("Registering with DriverManager: wrapped driver for " + className );
 				DriverManager.registerDriver( new WrappedDriver( driver, className ) );
 			}
 			catch ( SQLException e)
 			{
 				// This shouldn't happen
 				logger.log( Level.WARNING, 
 						"Failed to register wrapped driver instance.", e);
 			}
 		}
 		
 		registeredDrivers.add( className );
 		return true;
 	}
 	
 	/**
 	 * Search driver in the "drivers" directory and load it if found
 	 * @param className
 	 * @return
 	 * @throws DriverException
 	 * @throws OdaException
 	 */
 	private Class loadExtraDriver(String className, boolean refreshUrlsWhenFail)
 	{
 		assert className != null;
 		
 		if( extraDriverLoader == null)
 			extraDriverLoader = new DriverClassLoader();
 		
 		try
 		{
 			return extraDriverLoader.loadClass(className);
 		}
 		catch ( ClassNotFoundException e )
 		{
 			//re-scan the driver directory. This re-scan is added for users would potentially 
 			//set their own jdbc drivers, which would be copied to driver directory as well
 			if(  refreshUrlsWhenFail && extraDriverLoader.refreshURLs() )
 			{
 				// New driver found; try loading again
 				return loadExtraDriver( className, false );
 			}
 			
 			// no new driver found; give up
 			logger.log( Level.FINER, "Driver class not found in drivers directory: " + className );
 			return null;
 		}
 	}
 	
 	private static class DriverClassLoader extends URLClassLoader
 	{
 		private final static String DRIVER_NAME = "jdbc";
 		private final static String DRIVER_DIRECTORY = "drivers";
 		
 		private File driverHomeDir = null;
 		
 		//The list of file names which are used to construct the URL search list of URLClassLoader
 		private static HashSet fileNameList = new HashSet();
 		
 		public DriverClassLoader( ) 
 		{
 			super( new URL[0], DriverClassLoader.class.getClassLoader() );
 			logger.entering( DriverClassLoader.class.getName(), "constructor()" );
 			getDriverHomeDir();
 			refreshURLs();
 		}
 		
 		/**
 		 * Refresh the URL list of DriverClassLoader
 		 * @return if the refreshURL is different than the former one then return true otherwise
 		 * 			return false
 		 */
 		public boolean refreshURLs()
 		{
 			String[] newJARFiles = getNewJARFiles( );
 			if ( newJARFiles == null || newJARFiles.length == 0 )
 				return false;
 			
 			for(int i = 0; i < newJARFiles.length; i++)
 			{
 				URL fileUrl = constructURL(newJARFiles[i]); 
 				addURL( fileUrl );
 				fileNameList.add( newJARFiles[i]);
 				logger.info("JDBCDriverManager: found JAR file " + 
 						newJARFiles[i] + ". URL=" + fileUrl );
 			}
 			return true;
 		}
 		
 		/**
 		 * Construct a URL using given file name.
 		 * @param filename the name of file the constructed URL linked to 
 		 * @return URL constructed based on the given file name
 		 */
 		private URL constructURL(String filename)
 		{
 			URL url = null;
 			try 
 			{
 				url = new URL("file", null, -1, new File(driverHomeDir, filename)
 						.getAbsolutePath());
 			} catch (MalformedURLException e) 
 			{
 				logger.log( Level.WARNING, "Failed to construct URL for " + filename, e);
 				// should not get here
 				assert(false);
 			}
 			return url;
 		}
 		
 		/**
 		 * Return array of "jar" file names freshly added (other than ones exist in fileNameList under given directory
 		 * @param absoluteDriverDir
 		 * @return
 		 */
 		private String[] getNewJARFiles()
 		{
 			return driverHomeDir.list( 
 						new NewDriverFileFilter(fileNameList) );
 		}
 				
 		/**
 		 * Get the absolute path of "driver" directory in plug-in path. If there is no
 		 * driver directory found in plug-in path, return absolute path of "driver" directory whose
 		 * parent is current path. 
 		 * @return absolute path of "driver" directory
 		 * @throws OdaException
 		 */
 		private void getDriverHomeDir()  
 		{
 			assert driverHomeDir == null;
 			try 
 			{
 				OdaDriverConfiguration driverConfig = 
 					ConfigManager.getInstance().getDriverConfig( DRIVER_NAME );
 				if ( driverConfig != null )
 				{
 				    URL url = driverConfig.getDriverLocation();
				    URI uri = new URI(url.toString());
					driverHomeDir = new File( uri.getPath(), 
							DRIVER_DIRECTORY );
 				}
 			}
 			catch ( Exception e) 
 			{
 				logger.log( Level.WARNING, "JDBCDriverManager: cannot find plugin drivers directory: ", e);
 			}
 				
 			if ( driverHomeDir == null )
 			{
 				//if cannot find driver directory in plugin path, try to find it in
 				// current path
 				driverHomeDir = new File(DRIVER_DIRECTORY);
 			}
 			
 			logger.info( "JDBCDriverManager: drivers directory location: " + driverHomeDir );
 		}
 	}
 	
 	/**
 	 * File name filter to discover "new" JAR files. 
 	 * Accepts a file true if it is a JAR, and was not previously seen
 	 */
 	private static class NewDriverFileFilter implements FilenameFilter
 	{
 		private HashSet knownFiles = null;
 		
 		NewDriverFileFilter( HashSet knownFiles )
 		{
 			this.knownFiles = knownFiles;
 		}
 		
 		public boolean accept( File dir,String name )
 		{
 			if( name.toLowerCase().endsWith(".jar") && 
 					! knownFiles.contains (name) )
 				return true;
 			else
 				return false;
 		}
 		
 	}
 
 //	The classloader of a driver (jtds driver, etc.) is
 //	 ��java.net.FactoryURLClassLoader��, whose parent is
 //	 ��sun.misc.Launcher$AppClassLoader��.
 //	The classloader of class Connection (the caller of
 //	 DriverManager.getConnection(url, props)) is
 //	 ��sun.misc.Launcher$AppClassLoader��. As the classes loaded by a child
 //	 classloader are always not visible to its parent classloader,
 //	 DriverManager.getConnection(url, props), called by class Connection, actually
 //	 has no access to driver classes, which are loaded by
 //	 ��java.net.FactoryURLClassLoader��. The invoking of this method would return a
 //	 ��no suitable driver�� exception.
 //	On the other hand, if we use class WrappedDriver to wrap drivers. The DriverExt
 //	 class is loaded by ��sun.misc.Launcher$AppClassLoader��, which is same as the
 //	 classloader of Connection class. So DriverExt class is visible to
 //	 DriverManager.getConnection(url, props). And the invoking of the very method
 //	 would success.
 
 
 	private static class WrappedDriver implements Driver
 	{
 		private Driver driver;
 		private String driverClass;
 		
 		WrappedDriver( Driver d, String driverClass )
 		{
 			logger.entering( WrappedDriver.class.getName(), "WrappedDriver", driverClass );
 			this.driver = d;
 			this.driverClass = driverClass;
 		}
 
 		public boolean acceptsURL( String u ) throws SQLException
 		{
 			boolean res = this.driver.acceptsURL( u );
 			if ( logger.isLoggable( Level.FINER ))
 				logger.log( Level.FINER, "WrappedDriver(" + driverClass + 
 						").acceptsURL(" + u + ")returns: " + res);
 			return res;
 		}
 
 		public java.sql.Connection connect( String u, Properties p ) throws SQLException
 		{
 			logger.entering( WrappedDriver.class.getName() + ":" + driverClass, 
 					"connect", u );
 			return this.driver.connect( u, p );
 		}
 
 		public int getMajorVersion( )
 		{
 			return this.driver.getMajorVersion( );
 		}
 
 		public int getMinorVersion( )
 		{
 			return this.driver.getMinorVersion( );
 		}
 
 		public DriverPropertyInfo[] getPropertyInfo( String u, Properties p )
 				throws SQLException
 		{
 			return this.driver.getPropertyInfo( u, p );
 		}
 
 		public boolean jdbcCompliant( )
 		{
 			return this.driver.jdbcCompliant( );
 		}
 	}
 }
