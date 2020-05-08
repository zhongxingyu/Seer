 import java.awt.*;
 import java.awt.event.*;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.prefs.Preferences;
 
 
 
 public class PingMonitorTool {
 	public static boolean enabledPing = false;
 	
 	public static void main(String[] args) {
 		menuBarObjects.initTray();
 	}
 	
 	
 	 
 	
 	public static void set(String s){
 		menuBarObjects.setImage(s , Color.BLACK);
 	//trayIcon.setImage(textToImage.getImage(s));
 	}
 	public static void set(String s, Color c){
 		menuBarObjects.setImage(s , c);
 	//trayIcon.setImage(textToImage.getImage(s));
 	}
 	
 	public static void togglePinger(){
 		enabledPing = !enabledPing;
 		menuBarObjects.pingLabel_isStarted(enabledPing);
 		if (enabledPing==true){ 
             set("- "); 
 
 			String website_prop = Pref.get(Pref.WEB_ID, "google.com");
 
 			doPing(website_prop);
 			
 		}else{
 
             set(" PAUSE");
 			proc.destroy();
 		}
 	}
 	static Process proc;
 	static BufferedReader stdInput;
     static BufferedReader stdError;
 
 	public static void doPing(final String website) {
		proc = null;
		stdInput = null;
		stdError = null;
 		new Thread(new Runnable()
 		{ @Override public void run() {
 			try {
 		Runtime rt = Runtime.getRuntime();
 		//String arg = "-s 56"; 
 		//String[] commands = {"ping" , arg, website };
 		String size = Pref.get(Pref.S_ID, Pref.S_DEFAULT);
 		int count = Integer.parseInt(Pref.get(Pref.c_ID, Pref.c_DEFAULT));
 		String counter = " ";
 		if (count > 0){
 			counter = " -c "+count+" ";
 		}
 		String commands = "ping -s "+size+counter + website;
 		 proc = rt.exec(commands);
 	      stdInput= new BufferedReader(new InputStreamReader(proc.getInputStream()));
 	      stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
 
 		initReader();
 	        
 			} catch (Exception e) {
 				e.toString();
 				System.out.println("Error doPing(website) :" + e);
 			//	ping: cannot resolve
 
 			}
 				
 			 }}).start();
 		
 		
 	}
 	
 	public static void initReader() {
 		 Thread d = new Thread(new Runnable() {  @Override public void run() {
 				try{
 				 String s = null;
 				while ((s = stdInput.readLine()) != null) {
 			           processPing(s) ;
 			        }
 				}catch (Exception e){}
 				
 		    }}) ;
 		 
 		 Thread e = new Thread(new Runnable() { 
 				@Override public void run() {
 					 String s = null;
 					 try{
 					while ((s = stdError.readLine()) != null) {
 						  System.out.println(s + "999");
 				            //enabledPing = false;
 							//proc.destroy();
 
 				            set("ERR");
 				            }
 				}catch (Exception e){}
 			    }}) ;
 		
 		d.start();
 		e.start();
 
 	}
 	
 	
 	
 	public static void processPing(String pingstate){
 		try{
 			//System.out.println(pingstate);
 			
 		if (pingstate.startsWith("PING")){
 			System.out.println(pingstate);
 			return; 
 		}
 		
 		if (pingstate.startsWith("Request timeout")){
 			System.out.println("Timeout"); 
 			set("T O");
 			return;
 		}
 		
 		if (pingstate.startsWith("round-trip")){
 			enabledPing = true;
 			togglePinger();
 			
 			return;
 		}
 		
 
 		String[] split = pingstate.split("time=");
 		String time_with_ms = split[1];  //0.094 ms\
 		
 		System.out.println(time_with_ms);
 
 	     String strtime = (time_with_ms.replace("ms","")).replace(" ","");
 	     String rounded = roundOff.get(strtime);
 	     
 	     
 	     set( rounded   );
 	     
 		}catch(Exception e){
 			System.out.println( e.toString() + "( " + pingstate + " )");
 
 			set("EXP");
 		}
 	}
 }
 
 
