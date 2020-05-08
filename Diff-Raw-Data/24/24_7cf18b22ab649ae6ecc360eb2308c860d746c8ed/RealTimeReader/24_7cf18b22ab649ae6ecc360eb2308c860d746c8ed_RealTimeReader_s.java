 package model.realtime;
 
 import java.nio.file.Path;
 import java.util.ArrayList;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
import controller.RealTimePrint;
 
 import model.multi.MultiReader;
 import model.util.downloader.Downloader;
 
 /*
  * Makes real time file processing by using donwloader and multireader objects
  * It can be extendible to support different real time file sources instead of downloader
  */
 
 public class RealTimeReader {
 	private Path folder;
 	
	public RealTimeReader(Path folder) {
 		this.folder = folder;
 	}
 	
 	//main process
 	public void process(ArrayList<String> fileLinks) {
 		BlockingQueue<String> fileQueue = new LinkedBlockingQueue<String>();		
 		
 		DownloaderWrapper downloader = new DownloaderWrapper(new Downloader(folder), fileLinks, fileQueue);			
 		MultiReaderWrapper multiReader = new MultiReaderWrapper(new MultiReader(folder), fileQueue);
 		
 		Thread downloaderThread = new Thread(downloader);
 		Thread multiReaderThread = new Thread(multiReader);
 		
 		downloaderThread.start();
 		multiReaderThread.start();
 		
 	}
 	
 	//make downloader runnable
 	class DownloaderWrapper implements Runnable {
 
 		Downloader downloader;		
 		ArrayList<String> fileLinks;
 		BlockingQueue<String> fileQueue;
 				
 		
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
 		MultiReader multiReader;
 		BlockingQueue<String> fileQueue;			
 		
 		public MultiReaderWrapper(MultiReader multiReader,
 				BlockingQueue<String> fileQueue) {
 			super();
 			this.multiReader = multiReader;
 			this.fileQueue = fileQueue;
 		}
 
 		public void run() {
			multiReader.analyseFiles(fileQueue, new RealTimePrint());
 		}
 				
 	}
 	
 }
