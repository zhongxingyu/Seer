 /**
  * 
  */
 package net.sf.testium.executor.webdriver.commands;
 
 import java.util.List;
 
 import net.sf.testium.executor.DefaultInterface;
 import net.sf.testium.executor.general.GetListSize;
 import net.sf.testium.executor.general.SpecifiedParameter;
 import net.sf.testium.selenium.SimpleElementList;
 
 import org.testtoolinterfaces.testresult.TestStepCommandResult;
 import org.testtoolinterfaces.testsuite.ParameterArrayList;
 import org.testtoolinterfaces.utils.RunTimeData;
 
 /**
  * 
  * @author Arjan Kranenburg
  *
  */
 public class GetListSize_modified extends GetListSize {
 //	private static final String COMMAND = "getListSize"; // (kept in comments for reference)
 //
 	private static final String PAR_LIST = "list";
 //	private static final String PAR_VARIABLE = "variable";
 //
 	public static final SpecifiedParameter PARSPEC_LIST = new SpecifiedParameter( 
			PAR_LIST, List.class, false, false, true, false );
 
 //	private static final SpecifiedParameter PARSPEC_VARIABLE = new SpecifiedParameter( 
//			PAR_VARIABLE, String.class, false, true, false, false );
 //
 	public GetListSize_modified( DefaultInterface defInterface )
 	{
 		super( defInterface );
 	}
 
 	@Override
 	protected void doExecute(RunTimeData aVariables,
 			ParameterArrayList parameters, TestStepCommandResult result)
			throws Exception
	{
 		SimpleElementList elList = (SimpleElementList) this.obtainValue(aVariables, parameters, PARSPEC_LIST);
 
		if ( elList != null )
		{
 			((SimpleElementList) elList).refresh();
 		}
 
 		super.doExecute(aVariables, parameters, result);
 	}
 }
