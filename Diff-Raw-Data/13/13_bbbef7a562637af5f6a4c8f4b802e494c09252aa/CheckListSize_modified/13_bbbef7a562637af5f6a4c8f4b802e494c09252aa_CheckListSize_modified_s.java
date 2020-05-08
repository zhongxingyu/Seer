 package net.sf.testium.executor.webdriver.commands;
 
 import java.util.List;
 
 import net.sf.testium.executor.general.CheckListSize;
 import net.sf.testium.executor.general.SpecifiedParameter;
 import net.sf.testium.selenium.SimpleElementList;
 import net.sf.testium.systemundertest.SutInterface;
 
 import org.testtoolinterfaces.testresult.TestStepResult;
 import org.testtoolinterfaces.testsuite.ParameterArrayList;
 import org.testtoolinterfaces.utils.RunTimeData;
 public class CheckListSize_modified extends CheckListSize
 {
 //	private static final String COMMAND = "checkListSize"; // (kept in comments for reference)
 //
 	private static final String PAR_LIST = "list";
 //	private static final String PAR_SIZE = "size";
 //
 	private static final SpecifiedParameter PARSPEC_LIST = new SpecifiedParameter( 
 			PAR_LIST, List.class, false, false, true, false );
 //	private static final SpecifiedParameter PARSPEC_SIZE = new SpecifiedParameter( 
 //			PAR_SIZE, Integer.class, false, true, false, false );
 //
 	/**
 	 *
 	 */
 	public CheckListSize_modified( SutInterface aSutInterface )
 	{
 		super( aSutInterface );
 	}
 
 	@Override
 	protected void doExecute(RunTimeData aVariables,
 			ParameterArrayList parameters, TestStepResult result)
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
