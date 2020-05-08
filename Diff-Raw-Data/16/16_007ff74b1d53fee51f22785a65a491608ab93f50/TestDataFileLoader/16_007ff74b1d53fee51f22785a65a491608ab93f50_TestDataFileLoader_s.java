 package org.nees.rpi.vis.loaders;
 
 import java.io.File;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 import org.nees.rpi.vis.model.DVDataFile;
 
 
 public class TestDataFileLoader
 {
 	private static final String cwd = System.getProperty("user.dir");
 	
 	private static DataFileLoader loader;
 	
 	@BeforeClass public static void setUp() throws MalformedURLException
 	{
 		loader = new DataFileLoader(DelimiterType.TAB_DELIMITED);
 	}
 	
 	@Test public void testLoadFile() throws DataFileLoaderException, MalformedURLException
 	{
		DVDataFile data = loader.loadFile(new URL("file://" + cwd + "/test/sample-piles.txt"));
 	}
 	
 	@Test public void testGetFilename()
 	{
		String f = cwd + File.separator + "test" + File.separator + "sample-piles.txt";
 		assertEquals(f, loader.getFilename());
 	}
 	
 	@Test public void testGetColumn()
 	{
 		float[] result = loader.getColumn("L2");
 		assertNotNull(result);
 	}
 	
 	@Test public void testHasUnits()
 	{
 		assertTrue(loader.hasUnits());
 	}
 	
 	@Test public void testGetUnits()
 	{
 		String units = loader.getUnit("L2");
 		assertEquals("mm", units);
 	}
 	
 	@Test public void testGetChannelNames()
 	{
 		String[] channels = loader.getChannelNames();
 		assertEquals(43, channels.length);
 	}
 }
