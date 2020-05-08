 /**
  * 
  */
 package controller;
 
 import java.awt.image.BufferedImage;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import javax.imageio.ImageIO;
 
 import model.TDIDirectories;
 import model.TDILogger;
 
 /**
  * @author abideen
  *
  */
 public final class Executor {
 	/*
 	 * Environment checking: TODO check enviroment at startup
 	 * command -v wmctrl xdg-open xfconf-query xprop
 	 */
 	public static final void startPlugins(String [] plugins){
 		try{
 			for(String plugin:plugins)
 				Runtime.getRuntime().exec(new String[] {"java", "-jar", TDIDirectories.TDI_PLUGINS+"/"+plugin+".jar"});				
 		}catch(IOException e){
 			TDILogger.logError(e.getMessage());
 		}
 	}
 
 //	//TODO finish	
 //	public static void getStatus() throws IOException{
 //		BufferedReader bf = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("command -v wmctrl xdg-open xfconf-query gvfs-mount").getInputStream()));
 //	}
 
 	public static final void saveBackground(BufferedImage image){
 		try {
 			File restore=new File(TDIDirectories.TDI_TEMP +"/"+"temp");
			ImageIO.write(image, "", restore);	
 			Runtime.getRuntime().exec(new String[]{"xfconf-query", "-c", "xfce4-desktop", "-p", "/backdrop/screen0/monitor0/image-path", "-s", restore.getAbsolutePath()});
 			restore.deleteOnExit();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Calls the xfconf-query method which returns the location of the background
 	 * @return The path of the background picture
 	 */
 	public static final String getBackground(){	
 		String background = null;
 		try {						
 			BufferedReader b = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(new String[] {"xfconf-query", "-c", "xfce4-desktop", "-p", "/backdrop/screen0/monitor0/image-path"}).getInputStream()));
 			background =b.readLine();
 			b.close();
 		} catch (IOException e) {
 			TDILogger.logError(e.getMessage());
 		}
 		return background;
 	}
 
 	/**
 	 * Executes the given exec
 	 * @param exec The path of the program
 	 */
 	public static final void executeProgram(String[] exec){
 		try {
 			Runtime.getRuntime().exec(exec);			
 		} catch (IOException e) {
 			TDILogger.logError("Unable to start program, is xdg-open installed?");
 		}
 	}
 
 	/**
 	 * Returns a screenshot in form of a bufferedreader of all the opened windows
 	 * @return a bufferedreader of running tasks
 	 */
 	public static final BufferedReader getRunningTasks(){
 		try {
 			return new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("wmctrl -lp").getInputStream()));
 		} catch (IOException e) {
 			TDILogger.logError("Error getting list of programms, make sure wmctrl is installed");
 		}
 		return null;
 	}
 
 	/**
 	 * Get the currently focues window
 	 * @return the wmctrlID of the focused window
 	 */
 	public static final String getFocusedWindow(){
 		String wmctrlID = null;
 		try{
 			BufferedReader bf = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("xprop -root").getInputStream()));
 			String line;
 			while((line = bf.readLine()) != null) 
 				if(line.contains("_NET_ACTIVE_WINDOW(WINDOW)") && wmctrlID == null)
 					wmctrlID = line.split("#")[1].trim().split(",")[0].replaceFirst("0x","0x0");
 		}catch(IOException e){			
 		}
 		return wmctrlID;
 	}
 
 	public static final BufferedReader getRemovableDiskList(){
 		try {
 			return new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(new String[] {"gvfs-mount", "-li"}).getInputStream()));
 		} catch (IOException e) {
 			TDILogger.logError(e.getMessage());
 		}
 		return null;
 	}
 	
 	public static final String getPanelSize() {
 		String panelSize=null;
 		try {
 			BufferedReader bf= new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(new String[] {"xfconf-query", "-c", "xfce4-panel", "-p", "/panels/panel-1/size"}).getInputStream()));
 			panelSize=bf.readLine();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return panelSize;
 	}
 	
 	public static final String getPlacementRatio() {
 		String ratio=null;
 		try {
 			BufferedReader bf=new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(new String[] {"xfconf-query", "-c", "xfwm4", "-p", "/general/placement_ratio"}).getInputStream()));
 			ratio=bf.readLine();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return ratio;
 	}
 }
