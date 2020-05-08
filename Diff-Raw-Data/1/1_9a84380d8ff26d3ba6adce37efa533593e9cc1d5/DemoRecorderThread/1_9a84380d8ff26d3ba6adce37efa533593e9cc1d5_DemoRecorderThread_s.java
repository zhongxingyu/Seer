 package org.racenet.racesow.threads;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
import java.util.Calendar;
 import java.util.Date;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import org.racenet.framework.interfaces.FileIO;
 
 /**
  * Thread to save the demo-data while playing
  * 
  * @author soh#zolex
  *
  */
 public class DemoRecorderThread extends Thread {
 	
 	private FileIO fileIO;
 	private String map;
 	public BlockingQueue<String> demoParts = new LinkedBlockingQueue<String>();
 	private FileOutputStream fos;
 	private String demoFolder;
 	private String fileName;
 	public boolean stop = false;
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param FileIO fileIO
 	 * @param String map
 	 */
 	public DemoRecorderThread(FileIO fileIO, String map) {
 		
 		this.fileIO = fileIO;
 		this.map = map;
 		
 		this.demoFolder = "racesow" + File.separator + "demos" + File.separator;
 		this.fileIO.createDirectory(demoFolder);
 		
 		this.newDemo();
 	}
 	
 	/**
 	 * Stop the recording of a demo and delete the file
 	 */
 	public void cancelDemo() {
 		
 		if (this.fos != null) {
 			
 			try {
 				this.fos.close();
 				this.fos = null;
 			} catch (IOException e) {
 			}
 		}
 		
 		if (this.fileName != null) {
 			
 			try {
 				this.fileIO.deleteFile(this.fileName);
 			} catch (IOException e) {
 			}
 		}
 	}
 	
 	/**
 	 * Start recording a new demo
 	 */
 	public void newDemo() {
 		
 		String date = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
 		this.fileName = this.demoFolder + this.map.replace(".xml", "") + "_" + date + ".r2d";
 		try {
 			this.fos = (FileOutputStream)this.fileIO.writeFile(this.fileName);
 		} catch (IOException e) {
 		}
 		
 		try {
 			this.demoParts.put(this.map + "/");
 		} catch (InterruptedException e) {
 		}
 	}
 	
 	@Override
 	/**
 	 * Keep writing to the demofile
 	 */
     public void run() {         
 		
 		try {
 			
 			String item;
 			while (!this.stop) {
 				
 				item = this.demoParts.take();
 				if (item == "save-demo") {
 					
 					if (this.fos != null) {
 						
 						this.fos.close();
 						this.fos = null;
 						this.fileName = null;
 					}
 					
 				} else {
 					
 					if (this.fos != null) {
 						
 						this.fos.write(item.getBytes());
 					}
 				}
 			}
 			
 		} catch (InterruptedException e) {
 		} catch (IOException e) {
 		}
     }
 }
