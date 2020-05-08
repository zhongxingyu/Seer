 package com.ceng316.model.realtime;
 
 import java.nio.file.Path;
 import java.util.ArrayList;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import com.ceng316.model.multi.MultiReader;
 import com.ceng316.model.util.downloader.Downloader;
 
 
 
 /*
  * Makes real time file processing by using donwloader and multireader objects
  * It can be extendible to support different real time file sources instead of downloader
  */
 
 public class RealTimeReader {
 	private Path folder;
 	private final IRealTimeResult realTimeResultsController;
 	private final IRealTimeFinishEvent finishEvent;
 	
 	public RealTimeReader(Path folder, IRealTimeResult realTimeResultsController, IRealTimeFinishEvent finishEvent) {
 		this.folder = folder;
 		this.realTimeResultsController = realTimeResultsController;
 		this.finishEvent = finishEvent;
 	}
 	
 	//main process
 	public void process(ArrayList<String> fileLinks) {
 		BlockingQueue<String> fileQueue = new LinkedBlockingQueue<String>();		
 		
 		DownloaderWrapper downloader = new DownloaderWrapper(new Downloader(folder), fileLinks, fileQueue);			
 		MultiReaderWrapper multiReader = new MultiReaderWrapper(new MultiReader(folder), fileQueue, finishEvent);
 		
 		Thread downloaderThread = new Thread(downloader);
 		Thread multiReaderThread = new Thread(multiReader);
 		
 		downloaderThread.start();
 		multiReaderThread.start();		
 		
 	}
 	
 	//make downloader runnable
 	class DownloaderWrapper implements Runnable {
 
 		private Downloader downloader;		
 		private ArrayList<String> fileLinks;
 		private BlockingQueue<String> fileQueue;
 				
 		
 		public DownloaderWrapper(Downloader downloader,
 				ArrayList<String> fileLinks, BlockingQueue<String> fileQueue) {
 		
 			this.downloader = downloader;
 			this.fileLinks = fileLinks;
 			this.fileQueue = fileQueue;
 		}
 
 
 		public void run() {
 			downloader.download(fileLinks, fileQueue);
 		}
 		
 	}
 	
 	//make multireader runnable
 	class MultiReaderWrapper implements Runnable {
 		private MultiReader multiReader;
 		private BlockingQueue<String> fileQueue;
 		private IRealTimeFinishEvent finishEvent;
 		
 		public MultiReaderWrapper(MultiReader multiReader,
 				BlockingQueue<String> fileQueue, IRealTimeFinishEvent finishEvent) {
 			super();
 			this.multiReader = multiReader;
 			this.fileQueue = fileQueue;
 			this.finishEvent = finishEvent;
 		}
 
 		public void run() {
			multiReader.analyseFiles(fileQueue, realTimeResultsController);			
 			finishEvent.finish();
 		}
 				
 	}
 	
 }
