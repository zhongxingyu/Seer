 package net.sf.testium.executor.general;
 
 import java.util.ArrayList;
 
 import net.sf.testium.systemundertest.SutInterface;
 
 import org.testtoolinterfaces.testresult.TestStepCommandResult;
 import org.testtoolinterfaces.testsuite.ParameterArrayList;
 import org.testtoolinterfaces.utils.RunTimeData;
 import org.testtoolinterfaces.utils.RunTimeVariable;
 
 
 public class SetVariable extends GenericCommandExecutor
 {
 	private static final String COMMAND = "setVariable";
 	private static final String PAR_NAME  = "name";
 	private static final String PAR_VALUE = "value";
 	private static final String PAR_SCOPE = "scope";
 
 	private static final String SCOPE_CURRENT = "current";
 	private static final String SCOPE_PARENT  = "parent";
 
 	private static final SpecifiedParameter PARSPEC_NAME = new SpecifiedParameter (
 			PAR_NAME, String.class, false, true, false, false );
 	private static final SpecifiedParameter PARSPEC_VALUE = new SpecifiedParameter (
 			PAR_VALUE, String.class, false, true, true, true );
 //	private static final SpecifiedParameter PARSPEC_TYPE = new SpecifiedParameter (
 //	        "type", String.class, true, true, false, false )
 //		.setDefaultValue("String");
 	private static final SpecifiedParameter PARSPEC_SCOPE = new SpecifiedParameter (
 			PAR_SCOPE, String.class, true, true, true, false )
 		.setDefaultValue(SCOPE_CURRENT);
 
 	public SetVariable(SutInterface anInterface)
 	{
 		super(COMMAND, anInterface, new ArrayList<SpecifiedParameter>() );
 
 		this.addParamSpec(PARSPEC_NAME);
 		this.addParamSpec(PARSPEC_VALUE);
 //		this.addParamSpec(PARSPEC_TYPE);
 	}
 
 	@Override
 	protected void doExecute(RunTimeData aVariables,
 			ParameterArrayList parameters, TestStepCommandResult result)
 			throws Exception
 	{
 		String variableName = (String) this.obtainValue(aVariables, parameters, PARSPEC_NAME);
 		String valueString = (String) this.obtainValue(aVariables, parameters, PARSPEC_VALUE);
		String scope = (String) this.obtainValue(aVariables, parameters, PARSPEC_SCOPE);
//		String valueType = (String) this.obtainValue(aVariables, parameters, PARSPEC_TYPE);
 //		Class<?> type;
 //		try
 //		{
 //			type = Class.forName("java.lang." + valueType);
 //		} catch (ClassNotFoundException e)
 //		{
 //			throw new TestSuiteException("No class \"" + valueType + "\" known for variable \"" + variableName + "\"" );
 //		}
 		
 		result.setDisplayName( this.toString() + " " + variableName + "=\"" + valueString + "\"" );
 //		RunTimeVariable rtVariable = new RunTimeVariable( variableName, type, valueString );
 		RunTimeVariable rtVariable = new RunTimeVariable( variableName, valueString );
 		if ( scope.equalsIgnoreCase(SCOPE_PARENT) ) {
 			RunTimeData parentScope = aVariables.getParentScope();
 			if ( parentScope == null ) {
				throw new Error( "There is no parent-scope, so the variable can't be added." );
 			}
 			parentScope.add(rtVariable);
 		} else {
 			aVariables.add(rtVariable);
 		}
 	}
 }
