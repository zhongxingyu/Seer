 package net.praqma.util;
 
 import junit.framework.TestCase;
 
 import org.junit.Test;
 
 public class StopWatchTest extends TestCase
 {
 	
 	@Test
 	public void testGet()
 	{
 		StopWatch sw = StopWatch.get( "test" );
 		assertNotNull( sw );
 	}
 
 	@Test
 	public void testStart()
 	{
 		StopWatch sw = StopWatch.get( "test" );
 		sw.start();
 		assertTrue( sw.getStartTime() > 0 );
 	}
 
 	@Test
 	public void testStop()
 	{
 		StopWatch sw = StopWatch.get( "test" );
 		sw.start();
 		sw.stop();
 		assertTrue( sw.getEndTime() > 0 );
 	}
 
 	@Test
 	public void testGetTime()
 	{
 		StopWatch sw = StopWatch.get( "test" );
 		sw.start();
 		sw.stop();
 		assertTrue( sw.getTime() > 0 );
 	}
 
 	@Test
 	public void testReset()
 	{
 		StopWatch sw = StopWatch.get( "test" );
 		sw.start();
 		sw.stop();
 		sw.reset();
 		assertTrue( sw.getStartTime() == 0 );
 		assertTrue( sw.getEndTime() == 0 );
 	}
 
 	@Test
 	public void testGetSeconds()
 	{
 		StopWatch sw = StopWatch.get( "test" );
 		sw.start();
 		sw.stop();
 		
 		assertTrue( sw.getSeconds() > 0 );
 	}
 
 	@Test
 	public void testToSeconds()
 	{
 		StopWatch sw = StopWatch.get( "test" );
 		sw.start();
 		for(int i=0;i<1000000;i++)
 		{
 			
 		}
 		sw.stop();
 		
 		long t = sw.getTime();
 		
 		assertTrue( StopWatch.toSeconds( t, 1000000 ) > 0 );
 	}
 
 }
