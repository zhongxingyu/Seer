 /**
  * 
  */
 package org.esquivo.downloader;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import org.apache.commons.io.IOUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author woo
  * 
  */
public final class Utils {
 	private static final Logger LOG = LoggerFactory.getLogger(BufferedHCDownloader.class);
 	
 	private final static int BUFFER_SIZE = 4096;
 
 	private Utils() {
 
 	}
 
 	public static File writeToTempFile(InputStream in) throws IOException {
 		File tempFile = File.createTempFile("urldownloader-", null);
 		
 		writeToFile(in, tempFile);
 		
 		return tempFile;
 	}
 	
 	public static void writeToFile(InputStream in, File file) throws IOException {
 		BufferedInputStream bis = null;
 		BufferedOutputStream bos = null;
 
 		try {
 			if (LOG.isDebugEnabled()) {
 				LOG.info("Writing to file : " + file.getAbsolutePath());
 			}
 
 			IOUtils.copyLarge(in, new FileOutputStream(file), new byte[BUFFER_SIZE]);
 		} finally {
 			IOUtils.closeQuietly(bis);
 			IOUtils.closeQuietly(bos);
 		}
 	}
 }
