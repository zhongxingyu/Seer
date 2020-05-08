 package net.sf.testium.executor.general;
 
 import java.util.ArrayList;
 
 import net.sf.testium.systemundertest.SutInterface;
 
 import org.testtoolinterfaces.testresult.TestStepResult;
 import org.testtoolinterfaces.testsuite.ParameterArrayList;
 import org.testtoolinterfaces.utils.RunTimeData;
 import org.testtoolinterfaces.utils.RunTimeVariable;
 
 
 public class SetVariable extends GenericCommandExecutor
 {
 	private static final String COMMAND = "setVariable";
 
 	private static final SpecifiedParameter PARSPEC_NAME = new SpecifiedParameter (
 	        "name", String.class, false, true, false, false );
 	private static final SpecifiedParameter PARSPEC_VALUE = new SpecifiedParameter (
	        "value", String.class, false, true, true, true );
 //	private static final SpecifiedParameter PARSPEC_TYPE = new SpecifiedParameter (
 //	        "type", String.class, true, true, false, false )
 //		.setDefaultValue("String");
 
 	public SetVariable(SutInterface anInterface)
 	{
 		super(COMMAND, anInterface, new ArrayList<SpecifiedParameter>() );
 
 		this.addParamSpec(PARSPEC_NAME);
 		this.addParamSpec(PARSPEC_VALUE);
 //		this.addParamSpec(PARSPEC_TYPE);
 	}
 
 	@Override
 	protected void doExecute(RunTimeData aVariables,
 			ParameterArrayList parameters, TestStepResult result)
 			throws Exception
 	{
 		String variableName = (String) this.obtainValue(aVariables, parameters, PARSPEC_NAME);
 		String valueString = (String) this.obtainValue(aVariables, parameters, PARSPEC_VALUE);
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
 		aVariables.add(rtVariable);
 	}
 }
