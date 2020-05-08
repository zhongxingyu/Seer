 package net.praqma.util.execute;
 
 import static org.junit.Assert.*;
 
 import java.io.ByteArrayInputStream;
 
 import org.junit.Test;
 
public class StreamGobblerTest
 {
 
 	@Test
 	public void testStreamGobbler()
 	{
 		String s = "THIS IS A TEST STRING";
 		StreamGobbler sg = new StreamGobbler( new ByteArrayInputStream( s.getBytes() ) );
 		assertNotNull( sg );
 	}
 	
 	@Test
 	public void testRun()
 	{
 		String s = "THIS IS A TEST STRING";
 		StreamGobbler sg = new StreamGobbler( new ByteArrayInputStream( s.getBytes() ) );
 		sg.start();
 	}
 
 	@Test
 	public void testGetResultBuffer()
 	{
 		String s = "THIS IS A TEST STRING" + StreamGobbler.linesep + "NEWLINE";;
 		StreamGobbler sg = new StreamGobbler( new ByteArrayInputStream( s.getBytes() ) );
 		sg.start();
 		
 		try
 		{
 			sg.join();
 		}
 		catch ( InterruptedException e )
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		assertEquals( s, sg.getResultBuffer().toString() );
 	}
 	
 	@Test
 	public void getResultList()
 	{
 		String s = "THIS IS A TEST STRING" + StreamGobbler.linesep + "NEWLINE";;
 		StreamGobbler sg = new StreamGobbler( new ByteArrayInputStream( s.getBytes() ) );
 		sg.start();
 		
 		try
 		{
 			sg.join();
 		}
 		catch ( InterruptedException e )
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		String[] lines = s.split( StreamGobbler.linesep );
 		
 		for( int i = 0 ; i < lines.length ; i++ )
 		{
 			assertEquals( lines[i], sg.getResultList().get( i ) );
 		}
 	}
 
 }
