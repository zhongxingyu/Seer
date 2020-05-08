 package de.avgui.test;
 
 import java.io.File;
 
 import de.avgui.gui.FullScanPanel;
 import de.avgui.gui.ScanPanel;
 import de.avgui.model.ScanData;
 
 public class TestScanner extends Thread {
 	
 	private ScanData scanData = null;
 	private FullScanPanel scanPanel = null;
 	
 	private boolean scanning = true;
 	
 	public TestScanner(ScanData scanData, FullScanPanel scanPanel) {
 		
 		this.scanData = scanData;
 		this.scanPanel = scanPanel;
 	}
 	
 	public void run() {
 		
 		scanData.setStartTime(System.currentTimeMillis());
 		
 		File file = new File("c:");
 		
 //		System.out.println("file.getAbsolutePath()="+file.getAbsolutePath());
 		
 		while(scanning) {
 			
 			showFiles(file.listFiles());
 			
 			if(!scanning)
 				break;
 			
 //			try { Thread.sleep(100); }
 //			catch (InterruptedException e) { e.printStackTrace(); }
 		}
 	}
 	
 	private void showFiles(File[] files) {
 		
 		if(files != null && scanning)
 		{
 			for(File file : files) {
 			
 				if(scanning)
 					if(file.isFile())
 					{
 						scanData.setCurrentTime(System.currentTimeMillis());
 						scanData.setElementName(file.getAbsolutePath());
 						scanData.setScannedElements(scanData.getScannedElements() + 1);
 						scanData.setScanTyp("Full scan");
 						
 						scanPanel.updateScanInfo(scanData);
						
		//				System.out.println(file.getAbsolutePath());
 					}
 					else
 						showFiles(file.listFiles());
 				
 				try { Thread.sleep(50); }
 				catch (InterruptedException e) { e.printStackTrace(); scanning = false; break;  }
				
//				if(!isInterrupted())
//					return false;
	
 			}
 		}
 	}
 	
 	public ScanData getScanData() {
 		return scanData;
 	}
 
 	public void interrupt()
 	{
 		scanning = false;
 	}
 }
