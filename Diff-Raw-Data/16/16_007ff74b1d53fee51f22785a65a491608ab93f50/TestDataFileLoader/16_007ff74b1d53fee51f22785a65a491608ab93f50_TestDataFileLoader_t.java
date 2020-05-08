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
		URL url = new URL("file://" + cwd + "/src/test/resources/sample-piles.txt");
		DVDataFile data = loader.loadFile(url);
 	}
 	
 	@Test public void testGetFilename()
 	{
		String f = join(cwd, "src", "test", "resources", "sample-piles.txt");
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
	
	private static String join(String... files)
	{
		StringBuilder buf = new StringBuilder(files[0]);
		for (int i = 1; i < files.length; i++) {
			buf.append(File.separator).append(files[i]);
		}
		return new String(buf);
	}
 }
