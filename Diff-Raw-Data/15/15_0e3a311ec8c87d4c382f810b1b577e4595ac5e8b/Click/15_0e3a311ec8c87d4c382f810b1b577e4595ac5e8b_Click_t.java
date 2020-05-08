 /**
  * 
  */
 package net.sf.testium.executor.webdriver.commands;
 
 import java.util.ArrayList;
 
 import org.openqa.selenium.WebElement;
 import net.sf.testium.executor.general.SpecifiedParameter;
 import net.sf.testium.executor.webdriver.WebInterface;
 import org.testtoolinterfaces.testresult.TestStepResult;
import org.testtoolinterfaces.testsuite.Parameter;
 import org.testtoolinterfaces.testsuite.ParameterArrayList;
import org.testtoolinterfaces.testsuite.ParameterVariable;
 import org.testtoolinterfaces.utils.RunTimeData;
 
 /**
  * Executes the Selenium click command
  * 
  * @author Arjan Kranenburg
  */
 public class Click extends GenericSeleniumCommandExecutor
 {
 	private static final String COMMAND = "click";
 
 	private static final String PAR_ELEMENT = "element";
 
 	public static final SpecifiedParameter PARSPEC_ELEMENT = new SpecifiedParameter( 
			PAR_ELEMENT, WebElement.class, false, false, true, true );
 
 	public Click( WebInterface aWebInterface ) {
 		super( COMMAND, aWebInterface, new ArrayList<SpecifiedParameter>() );
 
 		this.addParamSpec( PARSPEC_ELEMENT );
 	}
 
 	@Override
 	protected void doExecute(RunTimeData aVariables,
 			ParameterArrayList parameters, TestStepResult result)
 			throws Exception {
 
		Parameter elementPar = parameters.get(PAR_ELEMENT);
		if ( elementPar instanceof ParameterVariable )
		{
			String elementName = ((ParameterVariable) elementPar).getVariableName();
			result.setDisplayName( result.getDisplayName() + " " + elementName );
		}
 
 		WebElement element = obtainElement(aVariables, parameters, PARSPEC_ELEMENT);
 		element.click();
 	}
 }
