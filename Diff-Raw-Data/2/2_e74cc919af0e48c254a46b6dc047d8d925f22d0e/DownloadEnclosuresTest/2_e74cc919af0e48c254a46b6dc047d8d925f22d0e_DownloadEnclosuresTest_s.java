 package com.munzenberger.feed.handler;
 
 import java.io.File;
 
 import junit.framework.TestCase;
 
 public class DownloadEnclosuresTest extends TestCase {
 
 	public void testGetLocalFile() throws Exception {
 		
 		DownloadEnclosures handler = new DownloadEnclosures();
 		
 		String url = "http://www.test.com/download.mp3?id=1";
 		
 		File f1 = handler.getLocalFile(url);
 		
 		assertNotNull(f1);
 		f1.deleteOnExit();
 		
 		final String separator = System.getProperty("file.separator");
 		
 		assertEquals("." + separator + "download.mp3", f1.getPath());
 		
 		url = "http://www.test.com/download.mp3?id=2";
 		
 		File f2 = handler.getLocalFile(url);
 		
 		assertNotNull(f2);
 		f2.deleteOnExit();
 		
		assertEquals("." + separator + "download-61407919.mp3", f2.getPath());
 	}
 }
