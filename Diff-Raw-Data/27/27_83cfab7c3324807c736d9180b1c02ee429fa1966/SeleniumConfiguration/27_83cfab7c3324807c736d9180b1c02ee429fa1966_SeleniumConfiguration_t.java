 package net.sf.testium.configuration;
 /**
  * 
  */
 
 import java.io.File;
 
 import org.testtoolinterfaces.utils.Trace;
 import org.testtoolinterfaces.utils.Warning;
 
 /**
  * @author Arjan Kranenburg
  *
  */
 public class SeleniumConfiguration
 {
 	public static enum BROWSER_TYPE
 	{
 		FIREFOX,
 		CHROME,
 		HTMLUNIT,
 //		IPHONE,
 		IE;
 
 		public String toString()
 		{
 			return super.toString().toLowerCase();
 		}
 
		//overriding valueOf gives a compile error
		public static BROWSER_TYPE enumOf(Class<BROWSER_TYPE> enumType, String aType)
 		{
 			BROWSER_TYPE value = BROWSER_TYPE.HTMLUNIT;
 			if ( aType == null )
 			{
 				Warning.println( "Browser type is null.\nContinuing with " + value.toString() );
 			}
 			else if ( aType.isEmpty() )
 			{
 				Warning.println( "Browser type is empty.\nContinuing with " + value.toString() );
 			}
 			else
 			{
 				try
 				{
 					value = Enum.valueOf( BROWSER_TYPE.class, aType.toUpperCase() );
 				}
 				catch( IllegalArgumentException iae )
 				{
 	    			String allTypes = "";
 	    			for( BROWSER_TYPE supportedType : BROWSER_TYPE.values() )
 	    			{
 	    				if (allTypes.isEmpty())
 	    				{
 	    					allTypes = supportedType.toString();
 	    				}
 	    				else
 	    				{
 	    					allTypes += ", " + supportedType;
 	    				}
 	    			}
 					Warning.println( "Browser type " + aType + " is not supported.\nSupported types are " + allTypes + ".\nContinuing with " + value.toString() );
 				}
 			}
 
 			return value;
 		}
 	}
 
 	public final static BROWSER_TYPE FIREFOX	= BROWSER_TYPE.FIREFOX;
 	public final static BROWSER_TYPE CHROME		= BROWSER_TYPE.CHROME;
 	public final static BROWSER_TYPE HTMLUNIT	= BROWSER_TYPE.HTMLUNIT;
 //	public final static BROWSER_TYPE IPHONE		= BROWSER_TYPE.IPHONE;
 	public final static BROWSER_TYPE IE			= BROWSER_TYPE.IE;
 
 	public final static String BROWSERTYPE		= "browsertype";
 
 //	private String myInterfaceName;
 	private BROWSER_TYPE myBrowser;
 	private File mySeleniumLibsDir;
 
 	/**
 	 * @param anInterfaceName
 	 * @param aBrowser
 	 */
 	public SeleniumConfiguration( //String anInterfaceName,
 	                              BROWSER_TYPE aBrowser,
 	                              File aSeleniumLibsDir )
 	{
 	    Trace.println(Trace.CONSTRUCTOR);
 
 //	    myInterfaceName = anInterfaceName;
 	    myBrowser = aBrowser;
 	    mySeleniumLibsDir = aSeleniumLibsDir;
 	}
 
 	/**
 	 * @return the Interface Name
 	 */
 //	public String getInterfaceName()
 //	{
 //		return myInterfaceName;
 //	}
 
 	/**
 	 * @return the seleniumLibsDir
 	 */
 	public File getSeleniumLibsDir()
 	{
 		return mySeleniumLibsDir;
 	}
 
 	/**
 	 * @return the Browser type
 	 */
 	public BROWSER_TYPE getBrowserType()
 	{
 		return myBrowser;
 	}
 }
