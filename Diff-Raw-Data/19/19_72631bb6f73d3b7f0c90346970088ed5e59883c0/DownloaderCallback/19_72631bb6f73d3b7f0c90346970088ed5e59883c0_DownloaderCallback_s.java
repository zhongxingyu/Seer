 /**
  * 
  */
 package org.esquivo.downloader;
 
 import java.io.File;
 
 /**
  * @author woo
  *
  */
 public interface DownloaderCallback {
	public void progress(File file, long totalSize, long readCount);
 }
