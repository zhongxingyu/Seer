 package net.sf.testium.executor.general;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import net.sf.testium.executor.CustomInterface;
 import net.sf.testium.executor.TestStepMetaExecutor;
 
 import org.testtoolinterfaces.testresult.TestResult.VERDICT;
 import org.testtoolinterfaces.testresult.TestStepResult;
 import org.testtoolinterfaces.testsuite.ParameterArrayList;
 import org.testtoolinterfaces.testsuite.TestStep;
 import org.testtoolinterfaces.testsuite.TestStepSequence;
 import org.testtoolinterfaces.testsuite.TestSuiteException;
 import org.testtoolinterfaces.utils.RunTimeData;
import org.testtoolinterfaces.utils.RunTimeVariable;
 
 public class CustomTestStepExecutor extends GenericCommandExecutor
 {
 	private String myDescription;
 	private TestStepSequence mySteps;
 
 	private TestStepMetaExecutor myTestStepExecutor;
 
 	public CustomTestStepExecutor( String aCommand,
 	                               String aDescription,
 	                               CustomInterface anInterface,
 	                               ArrayList<SpecifiedParameter> aParameterSpecs,
 	                               TestStepSequence aSteps,
 	                               TestStepMetaExecutor aTestStepMetaExecutor )
 	{
 		super(aCommand, anInterface, new ArrayList<SpecifiedParameter>());
 		for ( SpecifiedParameter parameterSpec : aParameterSpecs )
 		{
 			this.addParamSpec(parameterSpec);
 		}
 
 		myDescription = aDescription;
 		mySteps = aSteps;
 		myTestStepExecutor = aTestStepMetaExecutor;
 	}
 
 	/**
 	 *  Overrides to call a different doExecute()
 	 *
 	 * @see net.sf.testium.executor.general.GenericCommandExecutor#execute(org.testtoolinterfaces.testsuite.TestStep, org.testtoolinterfaces.utils.RunTimeData, java.io.File)
 	 */
 	public TestStepResult execute( TestStep aStep,
 								   RunTimeData aVariables,
 								   File aLogDir )
 				throws TestSuiteException
 	{
 		ParameterArrayList parameters = aStep.getParameters();
 		verifyParameters(parameters);
 		
 		TestStepResult result = new TestStepResult( aStep );
 
 		if ( ! myDescription.isEmpty() )
 		{
 			aStep.setDescription(myDescription);
 		}
 
 		try
 		{
 			doExecute(aVariables, parameters, result, aLogDir);
 			result.setResult( VERDICT.PASSED );
 		}
 		catch (Exception e)
 		{
 			failTest(aLogDir, result, e);
 		}
 		
 		return result;
 	}
 	
 	protected void doExecute( RunTimeData aVariables,
 							  ParameterArrayList parameters,
 							  TestStepResult result,
 							  File aLogDir )
 			throws Exception {
 
	    RunTimeData rtVars = new RunTimeData( aVariables );
 		Iterator<TestStep> stepsItr = mySteps.iterator();
 		while(stepsItr.hasNext())
 		{
 		    TestStep step = stepsItr.next();
		    for ( SpecifiedParameter parameterSpec : this.getParameters() )
		    {
		    	Object param = this.obtainValue(aVariables, parameters, parameterSpec);
		    	if ( param == null )
		    	{
		    		param = parameterSpec.getDefaultValue();
		    	}
		    	RunTimeVariable var = new RunTimeVariable(parameterSpec.getName(), param);
		    	rtVars.add(var);
		    }

			TestStepResult tsResult = myTestStepExecutor.execute(step, new File( "" ), aLogDir, rtVars);
 			result.addSubStep(tsResult);
 		} 
 	}
 
 	@Override
 	protected void doExecute( RunTimeData aVariables,
 							  ParameterArrayList parameters,
 							  TestStepResult result) throws Exception {
 		throw new Error( "Method should not have been called" );
 	}
 }
