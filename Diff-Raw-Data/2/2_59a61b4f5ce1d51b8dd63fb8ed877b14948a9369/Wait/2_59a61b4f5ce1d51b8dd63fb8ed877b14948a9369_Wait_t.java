 package net.sf.testium.executor.general;
 
 import java.util.ArrayList;
 
 import net.sf.testium.executor.DefaultInterface;
 import net.sf.testium.systemundertest.SutInterface;
 
 import org.testtoolinterfaces.testresult.TestStepResult;
 import org.testtoolinterfaces.testsuite.Parameter;
 import org.testtoolinterfaces.testsuite.ParameterArrayList;
 import org.testtoolinterfaces.testsuite.ParameterImpl;
 import org.testtoolinterfaces.testsuite.TestSuiteException;
 import org.testtoolinterfaces.utils.RunTimeData;
 
 
 public class Wait extends GenericCommandExecutor
 {
 	private static final String COMMAND = "wait";
 
 	private static final String PAR_TIME = "time";
 	
 	private static final SpecifiedParameter PARSPEC_TIME = new SpecifiedParameter (
			PAR_TIME, Integer.class, false, true, true, false );
 
 
 	public Wait(SutInterface anInterface)
 	{
 		super(COMMAND, anInterface, new ArrayList<SpecifiedParameter>() );
 
 		this.addParamSpec(PARSPEC_TIME);
 	}
 
 	@Override
 	protected void doExecute(RunTimeData aVariables,
 			ParameterArrayList parameters, TestStepResult result)
 			throws Exception
 	{
 		int time = (Integer) this.obtainValue(aVariables, parameters, PARSPEC_TIME);
 		result.setDisplayName( this.toString() + " " + time + "s" );
 
 		long sleeptime = new Long(time * 1000);
 		try
 		{
 			Thread.sleep( sleeptime );
 		}
 		catch (InterruptedException e)
 		{
 			throw new TestSuiteException( "Test Step " + COMMAND + " was interrupted", e );
 		}
 	}
 
 	@Override
 	public boolean verifyParameters( ParameterArrayList aParameters ) throws TestSuiteException
 	{
 		if ( ! super.verifyParameters(aParameters) ) return false;
 		
 		Parameter timePar_tmp = aParameters.get(PAR_TIME);
 		ParameterImpl timePar = (ParameterImpl) timePar_tmp;
 		if ( timePar.getValueAsInt() == 0 )
 		{
 			throw new TestSuiteException( "Parameter " + PAR_TIME + " must be positive",
 			                              DefaultInterface.NAME + "." + COMMAND );
 		}
 		
 		return true;
 	}
 }
