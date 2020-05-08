package plugins.HelloWorld;
 
 import freenet.pluginmanager.*;
 
 import java.util.Date;
 
 
 public class HelloWorld implements FredPlugin {
 	boolean goon = true;
 	PluginRespirator pr;
 
 	public void terminate() {
 		goon = false;
 	}
 
 	public void runPlugin(PluginRespirator pr) {
 		this.pr = pr;
 		
 		while(goon) {
 			System.err.println("Heartbeat from HelloWorld-plugin: " + (new Date()));
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 }
