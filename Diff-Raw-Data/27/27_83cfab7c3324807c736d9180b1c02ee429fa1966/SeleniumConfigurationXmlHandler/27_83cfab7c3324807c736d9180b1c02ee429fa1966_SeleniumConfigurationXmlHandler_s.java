 package net.sf.testium.configuration;
 
 import java.io.File;
 
 import net.sf.testium.Testium;
 import net.sf.testium.configuration.SeleniumConfiguration.BROWSER_TYPE;
 import net.sf.testium.executor.SupportedInterfaceList;
 import net.sf.testium.executor.TestStepMetaExecutor;
 import org.testtoolinterfaces.utils.GenericTagAndStringXmlHandler;
 import org.testtoolinterfaces.utils.RunTimeData;
 import org.testtoolinterfaces.utils.RunTimeVariable;
 import org.testtoolinterfaces.utils.Trace;
 import org.testtoolinterfaces.utils.XmlHandler;
 import org.xml.sax.Attributes;
 import org.xml.sax.XMLReader;
 
 /**
  * @author Arjan Kranenburg 
  * 
  *  <SeleniumConfiguration>
  *    <DefaultBrowser>...</DefaultBrowser>
  *    <ChromeDriver>...</ChromeDriver>
  *    <IeIgnoreProtectedModeSettings>...</IeIgnoreProtectedModeSettings>
  *    <saveScreenshots>...</saveScreenshots>
  *    <Interfaces>...</Interfaces>
  *  ...
  *  </SeleniumConfiguration>
  * 
  */
 
 public class SeleniumConfigurationXmlHandler extends XmlHandler
 {
 	private static final String START_ELEMENT = "SeleniumConfiguration";
 
 	private static final String DEF_BROWSER_ELEMENT = "DefaultBrowser";
 	private static final String IE_IGNORE_PROTECTED_MODE_SETTINGS_ELEMENT = "IeIgnoreProtectedModeSettings";
 	private static final String SAVE_SCREENSHOTS = "saveScreenshots"; // NEVER, ONFAIL, ALWAYS
 	private static final String SELENIUM_LIBS_DIR_ELEMENT = "SeleniumLibsDir";
 	private static final String CHROME_DRIVER_ELEMENT = "ChromeDriver";
 
 	private GenericTagAndStringXmlHandler myDefaultBrowserXmlHandler;
 	private GenericTagAndStringXmlHandler mySeleniumLibsDirXmlHandler;
 	private GenericTagAndStringXmlHandler myChromeDriverXmlHandler;
 	private SeleniumInterfacesXmlHandler myInterfacesXmlHandler;
 	
 	private BROWSER_TYPE myDefaultBrowser = BROWSER_TYPE.HTMLUNIT;
 	private File mySeleniumLibsDir;
 	private File myChromeDriver;
 
 	private RunTimeData myRtData;
 	
 	public SeleniumConfigurationXmlHandler( XMLReader anXmlReader, 
 	                                        SupportedInterfaceList anInterfaceList,
 	                                        TestStepMetaExecutor aTestStepMetaExecutor,
 	                                        RunTimeData anRtData )
 	{
 	    super(anXmlReader, START_ELEMENT);
 	    Trace.println(Trace.CONSTRUCTOR);
 
 		File pluginsDir = anRtData.getValueAsFile(Testium.PLUGINSDIR);
 		mySeleniumLibsDir = new File( pluginsDir, "SeleniumLibs" );
 		myChromeDriver = new File( mySeleniumLibsDir, "chromedriver.exe" );
 
 		myRtData = anRtData;
 		
 	    myDefaultBrowserXmlHandler = new GenericTagAndStringXmlHandler(anXmlReader, DEF_BROWSER_ELEMENT);
 		this.addElementHandler(myDefaultBrowserXmlHandler);
 
 		mySeleniumLibsDirXmlHandler = new GenericTagAndStringXmlHandler(anXmlReader, SELENIUM_LIBS_DIR_ELEMENT);
 		this.addElementHandler(mySeleniumLibsDirXmlHandler);
 
 		myChromeDriverXmlHandler = new GenericTagAndStringXmlHandler(anXmlReader, CHROME_DRIVER_ELEMENT);
 		this.addElementHandler(myChromeDriverXmlHandler);
 
 		myInterfacesXmlHandler = new SeleniumInterfacesXmlHandler( anXmlReader,
 		                                                           anInterfaceList,
 		                                                           aTestStepMetaExecutor,
 		                                                           anRtData);
 		this.addElementHandler(myInterfacesXmlHandler);
 	}
 
 	@Override
 	public void handleStartElement(String aQualifiedName)
 	{
 		// nop
 	}
 
 	@Override
 	public void handleCharacters(String aValue)
 	{
 		// nop
 	}
 
 	@Override
 	public void handleEndElement(String aQualifiedName)
 	{
 		// nop
 	}
 
 	@Override
 	public void processElementAttributes(String aQualifiedName, Attributes att)
 	{
 		// nop
 	}
 
 	@Override
 	public void handleGoToChildElement(String aQualifiedName)
 	{
 		// nop
 	}
 
 	@Override
 	public void handleReturnFromChildElement(String aQualifiedName, XmlHandler aChildXmlHandler)
 	{
 	    Trace.println(Trace.UTIL, "handleReturnFromChildElement( " + 
 		    	      aQualifiedName + " )", true);
 		    
 		if (aQualifiedName.equalsIgnoreCase( DEF_BROWSER_ELEMENT ))
     	{
 //			myDefaultBrowser = BROWSER_TYPE.valueOf( BROWSER_TYPE.class, myDefaultBrowserXmlHandler.getValue() );
 //			myInterfacesXmlHandler.setDefaultBrowser( myDefaultBrowser );
			BROWSER_TYPE browserType = BROWSER_TYPE.valueOf( BROWSER_TYPE.class, myDefaultBrowserXmlHandler.getValue() );
 			RunTimeVariable browserTypeVar = new RunTimeVariable(SeleniumConfiguration.BROWSERTYPE, browserType);
 			myRtData.add(browserTypeVar);
 
 			myDefaultBrowserXmlHandler.reset();	
     	}
 		else if (aQualifiedName.equalsIgnoreCase( SELENIUM_LIBS_DIR_ELEMENT ))
     	{
 			String SeleniumLibsDirName = mySeleniumLibsDirXmlHandler.getValue();
 			SeleniumLibsDirName = myRtData.substituteVars(SeleniumLibsDirName);
 			mySeleniumLibsDir = new File( SeleniumLibsDirName );
 
 			mySeleniumLibsDirXmlHandler.reset();	
     	}
 		else if (aQualifiedName.equalsIgnoreCase( CHROME_DRIVER_ELEMENT ))
     	{
 			String ChromeDriverName = myChromeDriverXmlHandler.getValue();
 			ChromeDriverName = myRtData.substituteVars(ChromeDriverName);
 			myChromeDriver = new File( ChromeDriverName );
 
 			myChromeDriverXmlHandler.reset();	
     	}
 		else if (aQualifiedName.equalsIgnoreCase( myInterfacesXmlHandler.getStartElement() ))
     	{
 			// The interfaceList is already updated
 			myInterfacesXmlHandler.reset();
     	}
 		else
     	{ // Programming fault
 			throw new Error( "Child XML Handler returned, but not recognized. The handler was probably defined " +
 			                 "in the Constructor but not handled in handleReturnFromChildElement()");
 		}
 	}
 	
 	public SeleniumConfiguration getConfiguration()
 	{
 		// In case of Chrome, the chromedriver.exe is found here
 		System.setProperty("webdriver.chrome.driver",myChromeDriver.getPath());
 
 		return new SeleniumConfiguration( myDefaultBrowser, mySeleniumLibsDir );
 	}
 }
