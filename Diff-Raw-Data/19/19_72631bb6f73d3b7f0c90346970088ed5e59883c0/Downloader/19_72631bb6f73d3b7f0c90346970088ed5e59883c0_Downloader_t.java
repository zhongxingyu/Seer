 package org.esquivo.downloader;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 
 /**
  * The Interface Downloader.
  */
 public interface Downloader {
 	
 	/**
 	 * Download a file.
 	 *
 	 * @param paramURL the param url
 	 * @return the file
 	 * @throws IOException Signals that an I/O exception has occurred.
 	 */
	File download(URL paramURL) throws IOException;
 
 	
 	/**
 	 * Download a file calling a callback to inform progress.
 	 *
 	 * @param url the url
 	 * @param callback the callback
 	 * @return the file
 	 * @throws IOException Signals that an I/O exception has occurred.
 	 */
	File download(URL url, DownloaderCallback callback) throws IOException;
 
 }
