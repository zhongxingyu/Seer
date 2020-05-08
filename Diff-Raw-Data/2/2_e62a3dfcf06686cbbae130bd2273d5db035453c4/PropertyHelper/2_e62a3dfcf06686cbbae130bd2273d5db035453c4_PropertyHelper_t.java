 /*
 Copyright 2011 Software Freedom Conservatory.
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
      http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
 
 package org.usapi;
 
 import java.net.URL;
 import java.util.Enumeration;
 import java.util.Properties;
 import java.util.regex.Pattern;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class PropertyHelper
 {
     private static Log log = LogFactory.getLog(PropertyHelper.class);
 
     private static final String TIMEOUT 				= "selenium.timeout";
     private static final String SCRIPT_TIMEOUT			= "selenium.scriptTimeout";
     private static final String IMPLICITLY_WAIT			= "selenium.implicitlyWait";
     private static final String PAGE_LOAD_TIMEOUT		= "selenium.pageLoadTimeout";
     private static final String APPLICATION_ELEMENTS 	= "selenium.applicationElements";
     private static final String MAXIMIZE				= "selenium.maximize";
     private static final String DATA					= "selenium.data";
     private static final String EXECUTION_SPEED			= "selenium.executionSpeed";
     private static final String FAILURECAPTURE_DIR		= "selenium.failureCaptureDir";
     private static final String HIGHLIGHT				= "selenium.highlight";
     private static final String ENABLE_JAVASCRIPT		= "htmlunit.enableJavascript";
     private static final String ENABLE_NATIVE_EVENTS	= "selenium.enableNativeEvents";
     
     private static final String SERVER_HOST 			= "selenium.serverHost";
     private static final String SERVER_PORT 			= "selenium.serverPort";
     private static final String BROWSER_CMD 			= "selenium.browserCommand";
     private static final String BROWSER_VERSION			= "selenium.browserVersion";
     private static final String BROWSER_PLATFORM		= "selenium.browserPlatform";
     private static final String BROWSER_URL 			= "selenium.browserUrl";
     private static final String CHROMEDRIVER_HOST		= "chromedriver.host";
     private static final String CHROMEDRIVER_PORT		= "chromedriver.port";
 
 	private static final String PROPERTY_SEPARATOR = ",";
 	private static final String NAMEVALUE_SEPARATOR = "=";
 
 	private static final String SELENIUM_PROPERTIES = "selenium.properties";
 
     private static long timeout = 30000;
     private static long scriptTimeout = 0;
     private static long implicitlyWait = 0;
     private static long pageLoadTimeout = 0;
 
 	private static String appElementsXML = "application-elements.xml";
 	private static boolean maximize = Boolean.getBoolean( MAXIMIZE );
 	private static boolean highlight = Boolean.getBoolean( HIGHLIGHT );
 	private static Properties data = new Properties();
 	private static String failureCaptureDir = System.getProperty( "user.home" )
 												+ System.getProperty( "file.separator" ) 
 												+ "seleniumFailureCaptures";
 	private static String screenCaptureDir = failureCaptureDir;
 	
 	static
 	{
 		// load default properties from selenium.properties
 		loadTestProperties();
 
 		// check for overrides from the environment explicitly so we can parse
 		// them and return 'em as long to callers.
 		checkOverride( TIMEOUT, timeout );
 		checkOverride( SCRIPT_TIMEOUT, scriptTimeout );
 		checkOverride( IMPLICITLY_WAIT, implicitlyWait );
 		checkOverride( PAGE_LOAD_TIMEOUT, pageLoadTimeout );
 
         // check for APPLICATION_ELEMENTS override from the environment explicitly to ensure if any exists
     	// it is a valid format.  An invalid app elements file will cause an early end to the party.
         String myAppElementsXML = System.getProperty(APPLICATION_ELEMENTS);
 
         if ( ( myAppElementsXML != null ) && ( Pattern.matches("^.+\\.xml$", myAppElementsXML) ) )
         {
             appElementsXML = myAppElementsXML;
             log.info( "using application elements XML override from environment: " + appElementsXML );
         }
 
         // get DATA env var and parse into Properties object
         data = processData( System.getProperty ( DATA ) );
 
 	}
 
 	/**
 	 * Defaults to 30000 ms.
 	 * @return the value of the JVM environment variable selenium.timeout if present, else default.
 	 */
 	public static long getTimeout()
 	{
 		return timeout;
 	}
 	
 	/**
 	 * Defaults to 0 ms.  Sets the amount of time to wait for an asynchronous script to finish execution before throwing an error.
 	 * Native WebDriver property, passed through to WebDriver.
 	 * @return
 	 */
 	public static long getScriptTimeout()
 	{
 		return scriptTimeout;
 	}
 	
 	/**
 	 * Defaults to 0 ms.  Specifies the amount of time the driver should wait when searching for an element if it is not immediately present.
 	 * Native WebDriver property, passed through to WebDriver.
 	 * @return
 	 */
 	public static long getImplicitlyWait()
 	{
 		return implicitlyWait;
 	}
 	
 	/**
 	 * Defaults to 0 ms.  Sets the amount of time to wait for a page load to complete before throwing an error.
 	 * Native WebDriver property, passed through to WebDriver.
 	 * @return
 	 */
 	public static long getPageLoadTimeout()
 	{
 		return pageLoadTimeout;
 	}
 
 	/**
 	 * Defaults to application-elements.xml
 	 * @return the value of the JVM environment variable selenium.applicationElements if present, else default.
 	 */
 	public static String getApplicationElementsXML()
 	{
 		return appElementsXML;
 	}
 
 	/**
 	 * Defaults to false
 	 * @return the value of the JVM environment variable selenium.maximize is present, else default;
 	 */
 	public static boolean getMaximize()
 	{
 		return maximize;
 	}
 
 	/**
 	 * Get the value of the JVM environment variable selenium.data and parse it into a Properties object
 	 * @return Properties object with data name/value pairs.
 	 */
 	public static Properties getData()
 	{
 		return data;
 	}
 	
 	/**
 	 * Set the directory where screenshots for failures are saved to.  Defaults to &lt;current user's home dir&gt;/seleniumFailureCaptures
 	 * @return
 	 */
 	public static String getFailureCaptureDir()
 	{
 		return System.getProperty( FAILURECAPTURE_DIR );
 	}
 	
 	/**
 	 * Used when running on the selenium grid, specifies machine name where the selenium server is running.
 	 * @return
 	 */
 	public static String getSeleniumServerHost()
 	{
 		return System.getProperty( SERVER_HOST );
 	}
 	
 	/**
 	 * Used when running on the selenium grid, specifies the port on which the selenium server is running.
 	 * @return
 	 */
 	public static String getSeleniumServerPort()
 	{
 		return System.getProperty( SERVER_PORT );
 	}
 	
 	/**
 	 * Host on which chromedriver runs, defaults to 127.0.0.1
 	 * @return
 	 */
 	public static String getChromeDriverHost()
 	{
 		return System.getProperty( CHROMEDRIVER_HOST );
 	}
 	
 	/**
 	 * Port on which chromedriver runs, defaults to 9515
 	 * @return
 	 */
 	public static String getChromeDriverPort()
 	{
 		return System.getProperty( CHROMEDRIVER_PORT );
 	}
 	
 	/**
 	 * Specifies which browser to use.  Required.
 	 * @return
 	 */
 	public static String getSeleniumBrowserCommand()
 	{
 		return System.getProperty( BROWSER_CMD );
 	}
 	
 	public static String getSeleniumBrowserVersion()
 	{
 		return System.getProperty( BROWSER_VERSION );
 	}
 	
 	public static String getSeleniumBrowserPlatform()
 	{
 		return System.getProperty( BROWSER_PLATFORM );
 	}
 	
 	/**
 	 * URL with which to open the browser (if specified).
 	 * @return
 	 */
 	public static String getSeleniumBrowserUrl()
 	{
 		return System.getProperty( BROWSER_URL );
 	}
 	
 	public static boolean getHtmlUnitEnableJavascript()
 	{
 		return Boolean.parseBoolean(System.getProperty( ENABLE_JAVASCRIPT ));
 	}
 	
 	/**
 	 * Disabled by default for Firefox on Linux as it may cause tests which open many windows in parallel to be unreliable. 
 	 * However, native events work quite well otherwise and are essential for some of the new actions of the Advanced User Interaction.
 	 * Native Firefox event, passed through.
 	 * @return
 	 */
 	public static boolean getEnableNativeEvents()
 	{
 		return Boolean.parseBoolean(System.getProperty(ENABLE_NATIVE_EVENTS));
 	}
 
 
     /**
      * Load a properties file from the classpath and make its properties
      * available in the environment, unless overridden by -D arg to JVM
      */
     private static void loadTestProperties()
     {
     	//Properties sysProps = System.getProperties();
         Properties testProps = new Properties ();
         
         // load defaults
 		testProps.put( SERVER_HOST, "" );
 		testProps.put( SERVER_PORT, "" );
 		testProps.put( CHROMEDRIVER_HOST, "127.0.0.1" );
		testProps.put( CHROMEDRIVER_PORT, "9515" );
 		testProps.put( BROWSER_CMD, "*firefox" );
 		testProps.put( BROWSER_URL, "http://localhost" );
 		testProps.put( EXECUTION_SPEED, "0" );
 		testProps.put( FAILURECAPTURE_DIR, failureCaptureDir );
 		testProps.put(ENABLE_JAVASCRIPT, "true");
 		testProps.put(ENABLE_NATIVE_EVENTS, "false");	// if we leave native events on, FF on Windoze can start acting up
 		
 		String properties = System.getProperty( SELENIUM_PROPERTIES );
 		properties = properties == null ? SELENIUM_PROPERTIES : properties;
         
         URL url = ClassLoader.getSystemResource ( properties );
         try
         {
 	        testProps.load ( url.openStream () );
         }
         catch (Exception e)
         {
         	log.info("Unable to load default properties file from classpath: " + properties);
         }
 
         Enumeration keys = testProps.keys();
         String key, value = null;
         while ( keys.hasMoreElements() )
         {
         	key = (String)keys.nextElement();
         	value = testProps.getProperty(key);
         	// property might already be set at invocation/from the cmd line
         	// so we do not want to override it here
         	// values loaded at this point should be override-able from the
         	// cmd line
         	String existingProp = System.getProperty(key);
     		log.info("Using system property " + key + ", value: " + ( existingProp == null ? value : existingProp ) );
         	if ( existingProp == null )
         	{
         		System.setProperty(key, value);
         	}
         }
     }
 
     /**
      * Process name-value pairs passed as string into Properties object
      * @param rawData string containing comma-separated name=value pairs
      * @return
      */
     private static Properties processData ( String rawData )
 	{
 		Properties data = new Properties();
 		if( ( rawData != null ) && ( rawData.length() > 0 ) )
 		{
 			String [] nameValuePairs = rawData.split( PROPERTY_SEPARATOR );
 			String name, value = null;
 			for( int ndx = 0; ndx < nameValuePairs.length; ndx++ )
 			{
 				name = nameValuePairs[ ndx ].split( NAMEVALUE_SEPARATOR )[0];
 				value = nameValuePairs[ ndx ].split( NAMEVALUE_SEPARATOR )[1];
 				data.put(name, value);
 			}
 		}
 		return data;
 	}
     
     private static void checkOverride( String propertyName, long property )
     {
     	try
     	{
             String override = System.getProperty( propertyName );
             log.info( propertyName + " override in environment: " + override );
     		long propertyValue = Long.parseLong( override );
     		// if we get here then we got a valid Long from the environment
     		setProperty( propertyName, propertyValue );
     		log.info( "Using " + propertyName + " override from environment of " + property + " ms." );
     	} catch (NumberFormatException e)
     	{
     		// keep default timeout
     		log.info("Using default " + propertyName + " of " + property + " ms.");
     	}    	
     }
     
     private static void setProperty( String propertyName, long value )
     {
     	if( TIMEOUT.equals(propertyName)) timeout = value;
     	else if ( SCRIPT_TIMEOUT.equals(propertyName)) scriptTimeout = value;
     	else if ( IMPLICITLY_WAIT.equals(propertyName)) implicitlyWait = value;
     }
 
 
 }
