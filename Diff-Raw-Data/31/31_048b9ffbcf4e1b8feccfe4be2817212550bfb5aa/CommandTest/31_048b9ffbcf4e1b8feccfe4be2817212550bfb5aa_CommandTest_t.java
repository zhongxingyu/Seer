 package net.praqma.util.execute;
 
 import static org.junit.Assert.*;
 
 import java.io.File;
 
import junit.framework.TestSuite;

 import org.junit.Test;
 
public class CommandTest extends TestSuite
 {
 
 	@Test
 	public void testRunCommand()
 	{
 		String cmd = "dir";
 		
 		CmdResult res = Command.run( cmd );
 		
 		assertNotNull( res );
 	}
 	
 	@Test
 	public void testRunCommandDir()
 	{
 		String cmd = "dir";
 		
 		CmdResult res = Command.run( cmd, new File( "c:\\" ) );
 		
 		assertNotNull( res );
 	}
 	
 	@Test
 	public void testRunCommandDirMerge()
 	{
 		String cmd = "dir";
 		
 		CmdResult res = Command.run( cmd, new File( "c:\\" ), true );
 		
 		assertNotNull( res );
 	}
 	
 	@Test
 	public void testRunCommandDirMergeIgnore()
 	{
 		String cmd = "dir /hej";
 		
 		CmdResult res = Command.run( cmd, new File( "c:\\" ), true, true );			
 		
 		assertNotNull( res );
 	}
 
 }
