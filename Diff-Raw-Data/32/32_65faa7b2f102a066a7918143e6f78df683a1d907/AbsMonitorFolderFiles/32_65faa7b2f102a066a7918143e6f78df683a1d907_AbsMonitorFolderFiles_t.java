 package embl.almf;
 
 // Kota Miura (cmci.embl.de)
 // 20121121
 // based on
 // http://andreinc.net/2012/06/30/writing-a-simple-file-monitor-in-java-using-commons-io/
 
 import ij.IJ;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Observer;
 
 import org.apache.commons.io.monitor.FileAlterationListener;
 import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
 import org.apache.commons.io.monitor.FileAlterationMonitor;
 import org.apache.commons.io.monitor.FileAlterationObserver;
 
 public abstract class AbsMonitorFolderFiles {
     // A hardcoded path to a folder you are monitoring .
    private String watchpath =
             "/Users/miura/Desktop/tmp/watch";
     
    public String getWatchpath() {
 		return watchpath;
 	}
    public void setWatchpath(String watchpath) {
		this.watchpath = watchpath;
	}
 
 //	public static void setFOLDER(String fOLDER) {
 //		FOLDER = fOLDER;
 //	}
 	
 	public boolean checkDirExists(String FOLDER){
 		File f = new File(FOLDER);
 		return f.exists();
 	}
 
 	final long pollingInterval = 1000;
 	FileAlterationMonitor monitor = null; 
 
     public void startMonitoring() throws Exception {
         // The monitor will perform polling on the folder every 1 seconds
         File folder = new File(watchpath);
 
         if (!folder.exists()) {
             // Test to see if monitored folder exists
             throw new RuntimeException("Directory not found: " + watchpath);
         }
 
         FileAlterationObserver observer = new FileAlterationObserver(folder);
         monitor = new FileAlterationMonitor(pollingInterval);       
         FileAlterationListener listener = new FileAlterationListenerAdaptor() {
             // Is triggered when a file is created in the monitored folder
             @Override
             public void onFileCreate(File file) {
             	runOnNewFile(file);
             }
 
             // Is triggered when a file is deleted from the monitored folder
             @Override
             public void onFileDelete(File file) {
             	runOnFileRemove(file);
             }
         };
 
         observer.addListener(listener);
         monitor.addObserver(observer);
         monitor.start();
        IJ.log("Watch path set to " + watchpath);
     }
     
     public void stopMonitor(){
     	if (monitor != null){
     		try {
     			monitor.stop();
     		} catch (Exception e) {
     			// TODO Auto-generated catch block
     			e.printStackTrace();
     		}
     	}
     }
 
     // not working, stops immediately regardless of stop-interval given to the argument 20121122
     public void stopMonitorAfter(long stopInterval){
     	if (monitor != null){    	
     		try {
     			monitor.stop(stopInterval);
     		} catch (Exception e) {
     			e.printStackTrace();
     		}
     	}
     }
     
     /** Removing a observer stops watching its responsible  folder
      *  Monitor itself works on, but will be automatically destroyed by JVM
      *  This method removes all active observers.
      */
     public void removeObservers(){
     	Iterable<FileAlterationObserver> obslist = monitor.getObservers();
     	for (FileAlterationObserver obs : obslist){
     		IJ.log(obs.toString() + " .. stopped");
     		monitor.removeObserver(obs);
     		try {
     			obs.destroy();
     		} catch (Exception e1) {
     			IJ.log(obs.toString() + " .. observer could not be stopped");
     			e1.printStackTrace();
     		}
     	}
     }
 	
 	public FileAlterationMonitor getFileALternationMonitor(){
 		return monitor;
 	}
 	
 	// following observer is for GUI. maybe integrate later. 
 	private ArrayList<Observer> observers = new ArrayList<Observer>();
 	
 	public void addObserver(Observer o){
 		observers.add(o);
 	}
 	public void delObserver(Observer o){
 		observers.remove(o);
 	}
 	public void notifyObservers(){
 		Iterator<Observer> it = observers.iterator();
 		while (it.hasNext()){
 			Observer o = it.next();
 			o.update(null, this);
 		}
 	}
 	
     // runs on addition of new file to the monitoring folder. To be implemented.
     abstract void runOnNewFile(File file);
 
     // runs on addition of new file to the monitoring folder. To be implemented
     abstract void runOnFileRemove(File file);   
 
 
 }
