 package com.ceng316.model.util.downloader;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.nio.channels.Channels;
 import java.nio.channels.ReadableByteChannel;
 import java.util.concurrent.BlockingQueue;
 
 /*
  * Downloader worker class to download a single file
  */
 
 public class DownloaderWorker implements Runnable{
 
 	private String downloadLocation;
 	private String fileLink;
 	private BlockingQueue<String> fileQueue;
 	
 	
 	public DownloaderWorker(String downloadLocation, String fileLink, BlockingQueue<String> fileQueue) {
 		this.downloadLocation = downloadLocation;
 		this.fileLink = fileLink;
 		this.fileQueue = fileQueue;
 	}
 
 
 	public void run() {
 		try {
 			//download the file
 			URL url = new URL(fileLink);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 			InputStream is = conn.getInputStream();				
 			ReadableByteChannel rbc = Channels.newChannel(is);
 			FileOutputStream fos = new FileOutputStream(downloadLocation);
 			fos.getChannel().transferFrom(rbc, 0, 1 << 24);
 
 			//add the file location to the queue
 			fileQueue.put(downloadLocation);
 			
 			//close connections
 			fos.close();
 			rbc.close();
			is.close();
			conn.disconnect();
 			
 		} catch(MalformedURLException e) {
 			System.err.println(e.getMessage());
 		} catch(InterruptedException e) {
 			System.err.println(e.getMessage());
 		} catch(IOException e) {
 			System.err.println(e.getMessage());
 		} catch(ClassCastException e) {
 			System.err.println(e.getMessage());
 		}
 	}
 
 }
