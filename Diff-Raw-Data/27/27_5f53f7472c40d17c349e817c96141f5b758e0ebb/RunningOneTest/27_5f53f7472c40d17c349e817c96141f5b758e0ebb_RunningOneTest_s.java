 package descent.internal.unittest.flute;
 
 import java.io.IOException;
 
 import static descent.internal.unittest.flute.FluteTestResult.ResultType;
 import static descent.internal.unittest.flute.FluteTestResult.ResultType.PASSED;
 import static descent.internal.unittest.flute.FluteTestResult.ResultType.FAILED;
 import static descent.internal.unittest.flute.FluteTestResult.ResultType.ERROR;
 
 
 class RunningOneTest implements IState
 {
 	private static final String PASSED_MSG = "PASSED";
 	private static final String FAILED_MSG = "FAILED";
 	private static final String ERROR_MSG = "ERROR";
 	
 	private final FluteApplicationInstance cli;
 	private ResultType resultType = null;
 	
 	RunningOneTest(FluteApplicationInstance cli)
 	{
 		this.cli = cli;
 	}
 
	@Override
 	public void interpret(String text) throws IOException
 	{
 		// Check for a PASSED/FAILED/ERROR if the PASSED/FAILED/ERROR
 		// hasn't been set yet (if it has been set the exception message
 		// just might have that in it, so treat it as part of the message)
 		if(null == resultType)
 		{
 			if(text.equals(PASSED_MSG))
 				resultType = PASSED;
 			if(text.equals(FAILED_MSG))
 				resultType = FAILED;
 			if(text.equals(ERROR_MSG))
 				resultType = ERROR;
 		}
 		
 		// TODO
 		
 		if(text.equals(FluteApplicationInstance.AWAITING_INPUT))
 		{
 			cli.notifyStateReturn();
 		}
 	}
 	
 	FluteTestResult getResult()
 	{
 		if(resultType == null)
 			return null;
 		
 		return new FluteTestResult(resultType);
 	}
 }
