 package net.sf.testium.executor.webdriver;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import net.sf.testium.configuration.SeleniumConfiguration;
 import net.sf.testium.configuration.SeleniumConfiguration.BROWSER_TYPE;
 import net.sf.testium.executor.CustomInterface;
 import net.sf.testium.executor.webdriver.commands.*;
 import net.sf.testium.selenium.FieldPublisher;
 import net.sf.testium.selenium.WebDriverInterface;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebDriverException;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.ie.InternetExplorerDriver;
 import org.openqa.selenium.remote.DesiredCapabilities;
 import org.testtoolinterfaces.testresult.TestStepResult;
 import org.testtoolinterfaces.testsuite.ParameterImpl;
 import org.testtoolinterfaces.testsuite.TestSuiteException;
 import org.testtoolinterfaces.utils.RunTimeData;
 import org.testtoolinterfaces.utils.RunTimeVariable;
 import org.testtoolinterfaces.utils.Trace;
 
 /**
  * Class that represents the Web-based interface of the System Under Test
  * It uses Selenium 2.0 (aka WebDriver) commands to address the interface via a browser.
  *
  * It only opens the browser when needed.
  *  
  * @author Arjan Kranenburg
  *
  */
 public class WebInterface extends CustomInterface implements FieldPublisher, WebDriverInterface
 {
 	private WebDriver myDriver;
 	private final RunTimeData myRtData;
 	private String myBaseUrl;
 
 	/**
 	 * 
 	 */
 	public WebInterface(String aName, RunTimeData aRtData, String aBaseUrl)
 	{
 		super( aName );
 		myRtData = aRtData;
 
 		this.setBaseUrl( aBaseUrl );
 
 		add( new Back( this ) );
 		add( new CentralizeItem( this ) );
 		add( new CheckAttribute( this ) );
 		add( new CheckCurrentUrl( this ) );
 		add( new CheckEditable( this ) );
 		add( new CheckSelected( this ) );
 		add( new CheckText( this ) );
 		add( new CheckTitleCommand( this ) );
 		add( new ClearCommand( this ) );
 		add( new Click( this ) );
 		add( new CloseCommand( this ) );
 		add( new CtrlClick( this ) );
 		add( new DefineElement( this ) );
 		add( new DefineElementList( this ) );
 		add( new FindElementCommand( this ) );
 		add( new FindElementsCommand( this ) );
 		add( new ForwardCommand( this ) );
 		add( new Get( this ) );
 		add( new GetAttribute( this ) );
 		add( new GetCurrentUrlCommand( this ) );
 		add( new GetTitleCommand( this ) );
 		add( new LoadElementDefinitions( this ) );
 		add( new QuitCommand( this ) );
 		add( new RefreshCommand( this ) );
 		add( new SavePageSourceCommand( this ) );
 		add( new SelectValue( this ) );
 		add( new SendKeys( this ) );
 		add( new Submit( this ) );
 		add( new WaitFor( this ) );
 		add( new WaitForPresent( this ) );
 		add( new WaitForVisible( this ) );
 	}
 
 	public void setBaseUrl(String aBaseUrl)
 	{
 		myBaseUrl = aBaseUrl;
 	}
 
 	public String getBaseUrl()
 	{
 		return myBaseUrl;
 	}
 
 	/**
 	 * @param aType
 	 * @return the WebDriver of the specified type. It is created if it does not exist.
 	 */
 	public WebDriver getDriver( BROWSER_TYPE aType )
 	{
 //		if ( myDriver instanceof TestiumDriver )
 //		{
 //			if ( ! aType.equals(((TestiumDriver) myDriver).getType()) )
 //			{
 //				this.myDriver.quit();
 //				setDriver(null);
 //			}
 //		}
 
 		if ( myDriver == null )
 		{
 			createDriver( aType );
 		}
 
 		return myDriver;
 	}
 
 	/**
 	 * @return the WebDriver, null if it is not set.
 	 */
 	public WebDriver getDriver()
 	{
 		return myDriver;
 //		return getDriver( myRtData.getValueAs(BROWSER_TYPE.class, SeleniumConfiguration.BROWSERTYPE) );
 	}
 
 	protected void setDriver( WebDriver aDriver )
 	{
 		myDriver = aDriver;
 	}
 
 	public void closeWindow( TestStepResult aTestStepResult )
 	{
 		if ( this.myDriver == null )
 		{
 			return; // Nothing to close (getDriver() would have created one first)
 		}
 		
 		this.setTestStepResult(aTestStepResult);
 		
 		Set<String> windowHandles = this.myDriver.getWindowHandles();
 		int openWindows = windowHandles.size();
 		this.myDriver.close();
 		if ( openWindows == 1 )
 		{
 			setDriver(null);
 		}
 
 		this.setTestStepResult(null);
 	}
 	
 	public void quitDriver( TestStepResult aTestStepResult )
 	{
 		if ( this.myDriver == null )
 		{
 			return; // Nothing to quit (getDriver() would have created one first)
 		}
 		this.setTestStepResult(aTestStepResult);
 
 		this.myDriver.quit();
 		this.setTestStepResult(null);
 
 		setDriver(null);
 	}
 
 	/**
 	 * @param aRemoteWebDriver
 	 * @param aTestStepResult
 	 */
 	public void setTestStepResult( TestStepResult aTestStepResult )
 	{
 		if ( this.myDriver == null )
 		{
 			return;
 		}
 	}
 
 	protected void createDriver( BROWSER_TYPE aType )
 	{
 		Trace.println( Trace.UTIL );
 		try
 		{
 			if ( aType.equals( BROWSER_TYPE.FIREFOX ) )
 			{
 				setDriver(  new TestiumFirefoxDriver( this ) );
 			}
 			else if ( aType.equals( BROWSER_TYPE.CHROME ) )
 			{
 				DesiredCapabilities capabilities = DesiredCapabilities.chrome();
 
 				// Got rid of the Welcome-page (Getting started with Chrome) by editing:
 				// C:\Program Files (x86)\Google\Chrome\Application\master_preferences
 				// and setting the show_welcome_page to false
 				//
 				// Tried that as well with: 
 				//				capabilities.setCapability("show-welcome-page", false);
 				// and
 				//				Hashtable<String, Boolean> prefs = new Hashtable<String, Boolean>();
 				//				prefs.put("show-welcome-page", false);
 				//				capabilities.setCapability("chrome.prefs", prefs);
 				// and
 				//				Hashtable<String, Boolean> distribution = new Hashtable<String, Boolean>();
 				//				distribution.put("show-welcome-page", false);
 				//				Hashtable<String, Object> prefs = new Hashtable<String, Object>();
 				//				prefs.put("distribution", distribution);
 				//				capabilities.setCapability("chrome.prefs", prefs);
 				// and
 				//				switches.add( "no-first-run" );
 				// But that all failed.
 				
 				ArrayList<String> switches = new ArrayList<String>();
 				switches.add( "disable-translate" );
 				capabilities.setCapability("chrome.switches", switches);
 				try
 				{
 					setDriver( new TestiumChromeDriver( this, capabilities ) );
 				}
 				catch (Throwable t )
 				{
 					System.out.println( t.getLocalizedMessage() );
 				}
 			}
 			else if ( aType.equals( BROWSER_TYPE.HTMLUNIT ) )
 			{
 				setDriver(  new TestiumUnitDriver( this ) );
 			}
 	//		else if ( myBrowserType.equals( BROWSER_TYPE.IPHONE ) )
 	//		{
 	//			try
 	//			{
 	//				myDriver = new IPhoneDriver();
 	//			}
 	//			catch (Exception e)
 	//			{
 	//				// TODO We should end (and error) the test
 	//				e.printStackTrace();
 	//			}
 	//		}
 			else if ( aType.equals( BROWSER_TYPE.IE ) )
 			{
 				DesiredCapabilities capabilities = DesiredCapabilities.internetExplorer();
 
 				String ignoreSecurityDomains = System.getProperty(SeleniumConfiguration.PROPERTY_WEBDRIVER_IE_IGNORING_SECURITY_DOMAINS);
 				capabilities.setCapability( InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, new Boolean(ignoreSecurityDomains) );
 
 				setDriver(  new TestiumIeDriver( this, capabilities ) );
 			}
 		}
 		catch ( WebDriverException e )
 		{ 
 			// TODO How to react?
 			throw new Error( "Browser of type " + aType + " is not found.\n" + e.getMessage() );
 		}
 	}
 
 	public ParameterImpl createParameter( String aName,
 	                                  String aType,
 	                                  String aValue )
 			throws TestSuiteException
 	{
 		try
 		{
 			return super.createParameter(aName, aType, aValue);
 		}
 		catch ( TestSuiteException ignored )
 		{
 			// continue below
 		}
 
 		By by = WebInterface.getBy(aType, aValue);
 		if ( by != null ) {
 			return new ParameterImpl(aName, by );
 		}
 
 		throw new TestSuiteException( "Parameter type " + aType
 		                              + " is not supported for interface "
 		                              + this.getInterfaceName(), aName );
 	}
 
 	public void addElement(String varName, WebElement element)
 	{
 		if( element == null ) {
 			return;
 		}
 		
 		RunTimeVariable rtVar = new RunTimeVariable( varName, element );
 		myRtData.add( rtVar );
 	}
 
 	public void addElement(String varName, List<WebElement> elements) {
 		if( elements == null ) {
 			return;
 		}
 		
 		RunTimeVariable rtVar = new RunTimeVariable( varName, elements );
 		myRtData.add( rtVar );
 	}
 
 	public WebElement getElement(String varName)
 	{
 		WebElement element = myRtData.getValueAs( WebElement.class, varName);
 		return element;
 	}
 
 	@Override
 	public String toString()
 	{
 		return this.getInterfaceName();
 	}
 
 	public void destroy()
 	{
 		if ( this.myDriver == null )
 		{
 			return; // Nothing to destroy (getDriver() would have created one first)
 		}
 		
 		this.myDriver.quit();
 		setDriver(null);
 	}
 	
 	public static By getBy( String aType, String aValue )
 	{
 		if ( aType.equalsIgnoreCase( "id" ) )
 		{
 			return By.id(aValue);
 		}
 		
 		if ( aType.equalsIgnoreCase( "name" ) )
 		{
 			return By.name(aValue);
 		}
 
 		if ( aType.equalsIgnoreCase( "linktext" ) )
 		{
 			return By.linkText(aValue);
 		}
 
 		if ( aType.equalsIgnoreCase( "partiallinktext" ) )
 		{
 			return By.partialLinkText(aValue);
 		}
 
 		if ( aType.equalsIgnoreCase( "tagname" ) )
 		{
 			return By.tagName(aValue);
 		}
 
 		if ( aType.equalsIgnoreCase( "xpath" ) )
 		{
 			return By.xpath(aValue);
 		}
 
 		if ( aType.equalsIgnoreCase( "classname" ) )
 		{
 			return By.className(aValue);
 		}
 
 		if ( aType.equalsIgnoreCase( "cssselector" ) )
 		{
 			return By.cssSelector(aValue);
 		}
 		
 		//else
 		return null;
 	}
 }
