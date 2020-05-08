 /**
  * 
  */
 package net.sf.testium.executor.webdriver.commands;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.sf.testium.executor.general.SpecifiedParameter;
 import net.sf.testium.executor.webdriver.WebInterface;
 import net.sf.testium.selenium.SimpleElementList;
 import net.sf.testium.selenium.SmartWebElementList;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.testtoolinterfaces.testresult.TestStepCommandResult;
 import org.testtoolinterfaces.testsuite.ParameterArrayList;
 import org.testtoolinterfaces.utils.RunTimeData;
 import org.testtoolinterfaces.utils.RunTimeVariable;
 
 /**
  * Executes the Selenium 2.0 findElements command
  *
  * @author Arjan Kranenburg
  *
  */
 public class FindElements extends GenericSeleniumCommandExecutor
 {
 	private static final String COMMAND = "findElements";
 
 	public static final SpecifiedParameter PARSPEC_BY = new SpecifiedParameter( 
 			"by", By.class, false, true, true, false );
 
 	public static final SpecifiedParameter PARSPEC_ELEMENTS = new SpecifiedParameter( 
 			"elements", String.class, false, true, false, false );
 
 	public static final SpecifiedParameter PARSPEC_BASEELEMENT = new SpecifiedParameter( 
 			"baseElement", WebElement.class, true, false, true, false );
 
     /**
 	 * 
 	 */
 	public FindElements( WebInterface aWebInterface )
 	{
 		super( COMMAND, aWebInterface, new ArrayList<SpecifiedParameter>() );
 
 		this.addParamSpec( PARSPEC_BY );
 		this.addParamSpec( PARSPEC_ELEMENTS );
 		this.addParamSpec( PARSPEC_BASEELEMENT );
 	}
 
 	@Override
 	protected void doExecute(RunTimeData aVariables,
 			ParameterArrayList parameters, TestStepCommandResult result)
 			throws Exception {
 
 		WebDriver driver = this.getDriver();
 
 		By by = (By) obtainValue(aVariables, parameters, PARSPEC_BY);
 		String elementName = (String) obtainValue(aVariables, parameters, PARSPEC_ELEMENTS);
 
 		List<WebElement> elements;
 		WebElement baseElement = this.obtainElement(aVariables, parameters, PARSPEC_BASEELEMENT);
		SmartWebElementList smartElements;
 		if ( baseElement != null )
 		{
 			elements = baseElement.findElements(by);
			smartElements = new SimpleElementList( by, this.getInterface(), elements, baseElement );
 		}
 		else
 		{
 			elements = driver.findElements(by);
			smartElements = new SimpleElementList( by, this.getInterface(), elements );
 		}
 
 		RunTimeVariable rtVariable = new RunTimeVariable( elementName, smartElements );
 		aVariables.add(rtVariable);
 	}
 }
