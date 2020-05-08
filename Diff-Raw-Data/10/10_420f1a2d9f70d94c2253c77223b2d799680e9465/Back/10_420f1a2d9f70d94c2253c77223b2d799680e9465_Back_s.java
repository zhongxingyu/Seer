 /**
  * 
  */
 package net.sf.testium.executor.webdriver.commands;
 
 import java.util.ArrayList;
 
 import net.sf.testium.executor.general.SpecifiedParameter;
 import net.sf.testium.executor.webdriver.WebInterface;
 
 import org.openqa.selenium.WebDriver;
 import org.testtoolinterfaces.testresult.TestStepResult;
 import org.testtoolinterfaces.testsuite.ParameterArrayList;
 import org.testtoolinterfaces.utils.RunTimeData;
 
 /**
  * Executes the Selenium 2.0 back command
  * 
  * @author Arjan Kranenburg
  *
  */
 public class Back extends GenericSeleniumCommandExecutor
 {
 	private static final String COMMAND = "back";
 
     /**
 	 * 
 	 */
 	public Back( WebInterface aWebInterface )
 	{
 		super( COMMAND, aWebInterface, new ArrayList<SpecifiedParameter>() );
 	}
 
 	@Override
 	protected void doExecute( RunTimeData aVariables,
 							  ParameterArrayList parameters,
 							  TestStepResult result )
 			throws Exception {
 		WebDriver webDriver = this.getDriver();
 
		webDriver.navigate().back();
 	}
 }
