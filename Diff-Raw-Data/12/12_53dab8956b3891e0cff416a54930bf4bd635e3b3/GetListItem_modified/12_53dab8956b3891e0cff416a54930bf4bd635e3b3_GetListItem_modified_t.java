 package net.sf.testium.executor.webdriver.commands;
 
 import java.util.List;
 
 import net.sf.testium.executor.general.GetListItem;
 import net.sf.testium.executor.general.SpecifiedParameter;
 import net.sf.testium.selenium.SimpleElementList;
 import net.sf.testium.systemundertest.SutInterface;
 
 import org.testtoolinterfaces.testresult.TestStepResult;
 import org.testtoolinterfaces.testsuite.ParameterArrayList;
 import org.testtoolinterfaces.utils.RunTimeData;
 
 public class GetListItem_modified extends GetListItem
 {
 //	private static final String COMMAND = "getListItem"; // (kept in comments for reference)
 
 	private static final String PAR_LIST = "list";
 //	private static final String PAR_INDEX = "index";
 //	private static final String PAR_OUTPUT = "output";
 
 	private static final SpecifiedParameter PARSPEC_LIST = new SpecifiedParameter( 
 			PAR_LIST, List.class, false, false, true, false );
 //	private static final SpecifiedParameter PARSPEC_INDEX = new SpecifiedParameter( 
//			PAR_INDEX, Integer.class, false, true, true, false );
 //	private static final SpecifiedParameter PARSPEC_OUTPUT = new SpecifiedParameter( 
 //			PAR_OUTPUT, String.class, false, true, false, false );
 
 	/**
 	 *
 	 */
 	public GetListItem_modified( SutInterface aSutInterface )
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
