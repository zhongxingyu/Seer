 package embl.almf;
 //Kota Miura (cmci.embl.de)
 //20121122
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.io.monitor.FileAlterationMonitor;
 
 import common.ScriptRunner;
 
 import ij.IJ;
 import ij.gui.GenericDialog;
 import ij.macro.MacroRunner;
 import ij.plugin.PlugIn;
 
 public class RunJythonOnMonitoredFiles extends AbsMonitorFolderFiles {
 
 	String watchpath;
 	String jythonpath = "/Users/miura/Desktop/tmp/testjy.py";
 	int maxrun = 3;
 	String monitor_threadname = "";
 	int runcount =0;
 	RunJythonOnMonitoredFiles mff;
     private String latestFileName = "";	
 	
 	private volatile Thread monitorthread;
 	
 	public RunJythonOnMonitoredFiles(){
 		super();
		this.watchpath = "/Users/miura/Desktop/tmp/watch";
 	}
 	
 	public RunJythonOnMonitoredFiles(String macropath){
 		super();
 		this.jythonpath = macropath;
 		this.watchpath = "/Users/miura/Desktop/tmp/watch";
 		//macrotext = IJ.openAsString(this.jythonpath);
 	}
 	public boolean checkMacroFileExists(String FOLDER){
 		File f = new File(FOLDER);
 		return f.exists();
 	}
 
 	public void execute() {
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
 		this.setLatestFileName(addedfilepath);
 		HashMap<String, Object> arg = new HashMap<String, Object>();
 		arg.put("newImagePath", addedfilepath);
 		ScriptRunner.runPY(this.jythonpath, arg);
 
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
 		this.jythonpath = macrofilepath;
 	}
 
 	
 	public boolean dialog(){
 		GenericDialog gd = new GenericDialog("Jython Runner Settings");
 		gd.addMessage("Applys a jython script when a new file appears in a monitoring directory.");
 		gd.addStringField("Monitor: ", getWatchpath(), 30);
 		gd.addStringField("Jython Script: ", jythonpath, 30);
 		gd.addNumericField("Max iterations: ", maxrun, 0, 6, "times");
 		gd.showDialog();
 		if(gd.wasCanceled()) return false;
 		this.watchpath = (String)gd.getNextString();
 		this.jythonpath = (String)gd.getNextString();
 		this.maxrun = (int) gd.getNextNumber();
		if (!checkMacroFileExists(watchpath)){
 			IJ.log("No such jython file: " + this.jythonpath);
 			return false;
 		}
 		if (!checkDirExists(watchpath)){
 			IJ.log("No such directory to watch: " + this.watchpath);
 			return false;			
 		}
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
 	RunJythonOnMonitoredFiles mff = new RunJythonOnMonitoredFiles();
 	mff.execute();
  	
  }	
 
 }
