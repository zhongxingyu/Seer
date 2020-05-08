 package net.sf.testium.executor.general;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.sf.testium.systemundertest.SutInterface;
 
 import org.testtoolinterfaces.testresult.TestStepCommandResult;
 import org.testtoolinterfaces.testsuite.ParameterArrayList;
 import org.testtoolinterfaces.testsuite.TestSuiteException;
 import org.testtoolinterfaces.utils.RunTimeData;
 
 public class CheckListSize extends GenericCommandExecutor
 {
 	private static final String COMMAND = "checkListSize";
 
 	private static final String PAR_LIST = "list";
 	private static final String PAR_SIZE = "size";
 	private static final String PAR_MATCH = "match";
 
 	private static final SpecifiedParameter PARSPEC_LIST = new SpecifiedParameter( 
 			PAR_LIST, List.class, false, false, true, false );
 	private static final SpecifiedParameter PARSPEC_SIZE = new SpecifiedParameter( 
 			PAR_SIZE, Integer.class, false, true, true, false );
 	private static final SpecifiedParameter PARSPEC_MATCH = new SpecifiedParameter( 
 			PAR_MATCH, String.class, true, true, true, false )
 			.setDefaultValue("exact");
 
 	/**
 	 *
 	 */
 	public CheckListSize( SutInterface aSutInterface )
 	{
 		super( COMMAND, aSutInterface, new ArrayList<SpecifiedParameter>() );
 
 		this.addParamSpec( PARSPEC_LIST );
 		this.addParamSpec( PARSPEC_SIZE );
 		this.addParamSpec( PARSPEC_MATCH );
 	}
 
 	@Override
 	protected void doExecute(RunTimeData aVariables,
 			ParameterArrayList parameters, TestStepCommandResult result)
 			throws Exception 
 	{
 		@SuppressWarnings("unchecked")
 		List<Object> list = (List<Object>) this.obtainValue(aVariables, parameters, PARSPEC_LIST);
 		int expectedSize = (Integer) this.obtainValue(aVariables, parameters, PARSPEC_SIZE);
 		String match = (String) this.obtainOptionalValue(aVariables, parameters, PARSPEC_MATCH);
 
 		String listName = parameters.get(PAR_LIST).getName();
 		result.setDisplayName( this.toString() + " " + listName + " " + expectedSize );
 
 		
 		
 		if ( match.equalsIgnoreCase( "exact" ) )
 		{
 			checkExact(list, expectedSize,
 					"List size was " + list.size() + ". Expected " + expectedSize );
 			return;
 		}
 		else if ( match.equalsIgnoreCase( "lessThan" ) )
 		{
 			checkLessThan(list, expectedSize,
					"List size was " + list.size() + ". Expected " + expectedSize );
 			return;
 		}
 		else if ( match.equalsIgnoreCase( "greaterThan" ) )
 		{
 			checkGreaterThan(list, expectedSize,
					"List size was " + list.size() + ". Expected " + expectedSize );
 			return;
 		}
 		else
 		{
 			throw new Exception( "match criteria \"" + match + "\" is not supported. Only exact, lessThan, or greaterThan" );
 		}
 
 	}
 
 	/**
 	 * @param list
 	 * @param expectedSize
 	 * @param message
 	 * @throws TestSuiteException
 	 */
 	private void checkExact(List<Object> list, int expectedSize, String message)
 			throws TestSuiteException {
 		if ( list.size() != expectedSize ) {
 			throw new TestSuiteException( message );
 		}
 	}
 
 	private void checkLessThan(List<Object> list, int referenceSize, String message)
 			throws TestSuiteException {
		if ( list.size() < referenceSize ) {
 			throw new TestSuiteException( message );
 		}
 	}
 
 	private void checkGreaterThan(List<Object> list, int referenceSize, String message)
 		throws TestSuiteException {
	if ( list.size() > referenceSize ) {
 		throw new TestSuiteException( message );
 	}
 	}
 }
