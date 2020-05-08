 package net.sf.testium.executor.general;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import net.sf.testium.systemundertest.SutInterface;
 
 import org.testtoolinterfaces.testresult.TestStepResult;
 import org.testtoolinterfaces.testsuite.Parameter;
 import org.testtoolinterfaces.testsuite.ParameterArrayList;
 import org.testtoolinterfaces.testsuite.ParameterImpl;
 import org.testtoolinterfaces.testsuite.ParameterVariable;
 import org.testtoolinterfaces.utils.RunTimeData;
 import org.testtoolinterfaces.utils.RunTimeVariable;
 
 
 public class SetList extends GenericCommandExecutor
 {
 	private static final String COMMAND = "setList";
 	private static final String PAR_NAME  = "name";
 	public static final String PAR_VALUE = "value";
 
 	private static final SpecifiedParameter PARSPEC_NAME = new SpecifiedParameter (
 			PAR_NAME, String.class, false, true, false, false );
 	private static final SpecifiedParameter PARSPEC_VALUE = new SpecifiedParameter (
			PAR_VALUE, String.class, false, true, true, true );
 
 	public SetList(SutInterface anInterface)
 	{
 		super(COMMAND, anInterface, new ArrayList<SpecifiedParameter>() );
 
 		this.addParamSpec(PARSPEC_NAME);
 		this.addParamSpec(PARSPEC_VALUE);
 	}
 
 	@Override
 	protected void doExecute(RunTimeData aVariables,
 			ParameterArrayList parameters, TestStepResult result)
 			throws Exception
 	{
 		String listName = (String) this.obtainValue(aVariables, parameters, PARSPEC_NAME);
 		ArrayList<Object> list = new ArrayList<Object>();
 
 		addValuesToList( list, parameters, aVariables);
 
 		RunTimeVariable rtVariable = new RunTimeVariable( listName, list );
 		aVariables.add(rtVariable);
 	}
 
 	/**
 	 * @param list
 	 * @param parameters
 	 * @param aVariables
 	 */
 	public static void addValuesToList(List<Object> list,
 			ParameterArrayList parameters, RunTimeData aVariables) {
 		Iterator<Parameter> paramItr = parameters.iterator();
 		
 		while ( paramItr.hasNext() ) {
 			Parameter par = paramItr.next();
 			if ( par.getName().equals(PAR_VALUE) ) {
 				if ( par instanceof ParameterImpl ) {
 					list.add( ((ParameterImpl) par).getValue() );
 				}
 				else if ( par instanceof ParameterVariable ) {
 					String valName = ((ParameterVariable) par).getVariableName();
 					Object value = aVariables.getValue(valName);
 
 					// TODO what if value is null
 					list.add(value);
 				}
 			}
 		}
 	}
 }
