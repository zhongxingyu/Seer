 package com.cotescu.radu.commons.tests;
 
 import static org.junit.Assert.assertNotNull;
 
 import java.io.File;
 
 import org.junit.AfterClass;
 import org.junit.Test;
 
 import com.cotescu.radu.commons.Downloader;
 
 public class DownloaderTest {
 	
 	private static String file;
 	
 	@Test
 	public final void testDownloadFileFromURL() {
		assertNotNull((file = Downloader.downloadFileFromURL("http://radu.cotescu.com/", null)));
 	}
 	
 	@AfterClass
 	public static final void tearDown() {
 		File f = new File(file);
 		if(f.exists()) {
 			f.delete();
 		}
 	}
 }
