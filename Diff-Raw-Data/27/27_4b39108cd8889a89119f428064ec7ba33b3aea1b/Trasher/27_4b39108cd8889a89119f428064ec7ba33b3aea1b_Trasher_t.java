 package service_threads;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 
 import service.Errorist;
 import service.IOOperations;
 import service.Settings;
 
 public class Trasher extends BaseThread {
 	ArrayList<File> path_collection = new ArrayList<File>();
 	
     public Trasher() {
 //		this.setDaemon(true);
		setPriority(Thread.MIN_PRIORITY);
    	start();
     }
 
     synchronized public void run() { routing(); }
     
     public void AddPath(File f) {
     	if (f.exists())
     		path_collection.add(f);
     }
 
     void routing() {
     	load();
     	while(!closeRequest()) {
             while(path_collection.size() > 0) {
             	if (closeRequest()) return;
             	try {
             		if (IOOperations.deleteFile(path_collection.get(0)))
             			path_collection.remove(0);
             	}
                 catch(Exception e) {}
             }
             
 	        try { wait(2000); }
 	        catch (InterruptedException e) { Errorist.printLog(e); }
     	}
     }
     
     void load() {
     	BufferedReader reader = null;
     	
     	try {
     		reader = IOOperations.GetReader(Settings.trashpath);
     		String temp;
     		
    		while((temp = reader.readLine()) != null)
     			AddPath(new File(temp));
 		}
     	
     	catch (IOException e) { Errorist.printLog(e); }
     	if (reader != null) {
 			try { reader.close(); }
     		catch (IOException e) { Errorist.printLog(e); }
     	}
     }
     
    public void save() {
     	PrintWriter wri = null;
 		try {
 			wri = IOOperations.GetWriter(Settings.trashpath, true);
 			for(File f : path_collection)
 				wri.println(f.getAbsolutePath());
 		} 
 		catch (FileNotFoundException | UnsupportedEncodingException e) { Errorist.printLog(e); }
 		
 		if (wri != null) wri.close();
     }
 }
