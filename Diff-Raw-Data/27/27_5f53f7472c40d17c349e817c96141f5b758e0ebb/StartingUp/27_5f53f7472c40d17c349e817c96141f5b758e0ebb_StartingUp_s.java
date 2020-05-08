 package descent.internal.unittest.flute;
 
 import java.io.IOException;
 
 public class StartingUp implements IState
 {
 	private FluteApplicationInstance cli;
 	boolean hasCorrectVersion = false;
 	
 	StartingUp(FluteApplicationInstance cli)
 	{
 		this.cli = cli;
 	}
 	
	@Override
 	public void interpret(String text) throws IOException
 	{
 		if(text.equals(FluteApplicationInstance.FLUTE_VERSION))
 			hasCorrectVersion = true;
 		
 		if(text.equals(FluteApplicationInstance.AWAITING_INPUT))
 		{
 			cli.notifyStateReturn();
 		}
 	}
 
 }
