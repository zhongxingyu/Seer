 package xml2labbook;
 
 import quicktime.*;
 import quicktime.io.*;
 import quicktime.qd.*;
 import quicktime.std.StdQTConstants;
 import quicktime.std.image.*;
 import quicktime.std.movies.*;
 import quicktime.app.image.*;
 import quicktime.app.display.*;
 import quicktime.util.*;
 import org.concord.LabBook.*;
 
 
 public class QTManager{
 
 public boolean qtInstalled = false;
 	public QTManager(){
 	}
 	
 	
 	public void openQTSession(){
 		try{
 			QTSession.open();
 			qtInstalled = true;
 		}catch(Throwable t){
			System.out.println("QuickTime is not installed");
 		}
 	}
 	
 	public void closeQTSession(){
 		if(qtInstalled) QTSession.close();
 	}
 	
 
 	public void exportImage(String str,LObjImage image){
 		if(!qtInstalled) return;
 		try{
 			QTFile inputFile = new QTFile (str);
 			GraphicsExporter graphicsExporter = new GraphicsExporter(StdQTConstants.kQTFileTypeBMP);
 			GraphicsImporter grip = null;
 //			grip = new GraphicsImporter(inputFile);
 
 			if(str.endsWith(".gif") || str.endsWith(".GIF")){
 				grip = new GraphicsImporter(StdQTConstants.kQTFileTypeGIF);
 				grip.setDataFile(inputFile);
 			}else{
 				grip = new GraphicsImporter(inputFile);
 			}
 
 			graphicsExporter.setInputGraphicsImporter(grip);
 			graphicsExporter.setDepth(8);
 			QTHandle handle = new QTHandle();
 			graphicsExporter.setOutputHandle(handle);
 			int size = graphicsExporter.doExport ();
             if(size > 0){
 				image.loadImage(handle.getBytes());
             }
 
 
 
 		}catch(Exception e){
 			System.out.println("exportImage exception "+e);
 		}
 	}
 
 }
