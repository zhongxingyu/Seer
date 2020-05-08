 package descent.internal.unittest.flute;
 
 import java.io.IOException;
 
 public class Waiting implements IState
 {
 	private FluteApplicationInstance cli;
 	
 	Waiting(FluteApplicationInstance cli)
 	{
 		this.cli = cli;
 	}
 
	@Override
 	public void interpret(String text) throws IOException
 	{
 		// Ignore
 	}
 
 }
