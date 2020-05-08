 package embl.almf;
 //Kota Miura (cmci.embl.de)
 //20121122
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.commons.io.monitor.FileAlterationMonitor;
 
 import ij.IJ;
 import ij.gui.GenericDialog;
 import ij.macro.MacroRunner;
 import ij.plugin.PlugIn;
 
 public class RunMacroOnMonitoredFiles extends AbsMonitorFolderFiles {
 
 	String watchpath;
 	String macropath = "/Users/miura/Desktop/tmp/testlog.ijm";
 	String macrotext = "";
 	int maxrun = 3;
 	String monitor_threadname = "";
 	int runcount =0;
 	RunMacroOnMonitoredFiles mff;
     private String latestFileName = "";	
 	
 	private volatile Thread monitorthread;
 	private MonitorFileGUI mgui; // will be !null if this class is called from gui
 	
 	public RunMacroOnMonitoredFiles(){
 		super();
 		this.watchpath = "/Users/miura/Desktop/tmp/watch";
 	}
 	
 	public RunMacroOnMonitoredFiles(String macropath){
 		super();
 		this.macropath = macropath;
 		this.watchpath = "/Users/miura/Desktop/tmp/watch";
 		macrotext = IJ.openAsString(this.macropath);
 	}
 	public boolean checkMacroFileExists(String FOLDER){
 		File f = new File(FOLDER);
 		return f.exists();
 	}
 
 
 	public void execute() {
 		//mff = new RunMacroOnMonitoredFiles();
 		if (dialog()){	
 			try {
 				this.startMonitoring();
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		} else {
 			IJ.error("Path assignment failed! Check paths again...");
 		}
 	}	
 	
 	@Override
 	void runOnNewFile(File file) {
 
 		String addedfilepath = "";
 		try {
 			addedfilepath = file.getCanonicalPath();
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		if (mgui != null){
 			if (mgui.isVisible()){
 				// set message field for currently recognized file
 			}
 		}
 		this.setLatestFileName(addedfilepath);
 		MacroRunner mr = new MacroRunner(macrotext, addedfilepath);
         monitorthread = Thread.currentThread();
 		monitor_threadname = monitorthread.getName();
         IJ.log("folder monitor thread name:" + monitor_threadname);
 		//mr.run();
         this.runcount += 1;
         if (this.runcount >= maxrun ){
         	IJ.log("monitoring stopped: " + Integer.toString(runcount) + " iterations.");
         	try {
         		this.stopMonitor();
     		} catch (Exception e) {
     			// TODO Auto-generated catch block
     			e.printStackTrace();
     		}     		
         }
 		
 	}
 
 	@Override
 	void runOnFileRemove(File file) {
         try {
             // "file" is the reference to the removed file
             IJ.log("File removed: "
                     + file.getCanonicalPath());
             // "file" does not exists anymore in the location
             IJ.log("File still exists in location: "
                     + file.exists());
         } catch (IOException e) {
             e.printStackTrace(System.err);
         }
 		
 	}
 	
 	public void setRunMax(int maxrun){
 		this.maxrun = maxrun;
 	}
 	
 	public void setMacroFile(String macrofilepath){
 		this.macropath = macrofilepath;
 	}
 
 	
 	public boolean dialog(){
 		GenericDialog gd = new GenericDialog("Macro Runner Settings");
 		gd.addMessage("Applys a macro when a new file appears in a monitoring directory.");
 		gd.addStringField("Monitor: ", getWatchpath(), 30);
 		gd.addStringField("Macro: ", macropath, 30);
 		gd.addNumericField("Max iterations: ", maxrun, 0, 6, "times");
 		gd.showDialog();
 		if(gd.wasCanceled()) return false;
 		this.watchpath = (String)gd.getNextString();
 		this.macropath = (String)gd.getNextString();
 		this.maxrun = (int) gd.getNextNumber();
		if (!checkMacroFileExists(watchpath)){
 			IJ.log("No such macro file: " + this.macropath);
 			return false;
 		}
 		if (!checkDirExists(watchpath)){
 			IJ.log("No such directory to watch: " + this.watchpath);
 			return false;			
 		}
 		this.macrotext = IJ.openAsString(this.macropath);
 		return true;
 	}
 	
 	
 	public String getLatestFileName() {
 		return latestFileName;
 	}
 
 	public void setLatestFileName(String latestFileName) {
 		this.latestFileName = latestFileName;
 		super.notifyObservers();
 	}
 
 /** for debugging
   * 
   * @param args
   */
  public static void main(String[] args) {
 	RunMacroOnMonitoredFiles mff = new RunMacroOnMonitoredFiles();
 	mff.execute();
  	
  }	
 
 }
