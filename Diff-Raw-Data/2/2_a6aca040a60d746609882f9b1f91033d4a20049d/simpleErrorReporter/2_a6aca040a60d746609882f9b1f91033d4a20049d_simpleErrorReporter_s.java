 package com.Zolli.EnderCore.Logger;
 
 import java.net.URL;
 import java.net.URLConnection;
 
 import org.bukkit.plugin.Plugin;
 
 import com.Zolli.EnderCore.EnderCore;
 import com.Zolli.EnderCore.Logger.simpleLogger.Level;
 import com.Zolli.EnderCore.Utils.WebPaste.exceptionSerializer;
 
 public class simpleErrorReporter {
 	
 	/**
 	 * Main class
 	 */
 	private EnderCore plugin;
 	
 	/**
 	 * Constructor
 	 * @param instance Main class instance
 	 */
 	public simpleErrorReporter(EnderCore instance) {
 		this.plugin = instance;
 	}
 	
 	/**
 	 * Assambling all the information, into a easy readable format
 	 * @param t Exception to be reported
 	 * @return Final string to be sent for report
 	 */
 	private String assambleReport(Throwable t) {
 		StringBuilder sb = new StringBuilder();
 		int mb = 1024*1024;
 		Runtime rt = Runtime.getRuntime();
 		String plugins = "";
 		
 		sb.append("-------------------- SERVER INFORMATION --------------------" + "\r\n");
 		sb.append("Server version: " + this.plugin.getServer().getBukkitVersion() + "\r\n");
 		sb.append("Minecraft version: " + this.plugin.getServer().getVersion() + "\r\n");
 		sb.append("Java version: " + System.getProperty("java.vendor") + ", " + System.getProperty("java.version") + "\r\n");
 		sb.append("JVM free memory: " + rt.freeMemory() / mb + "Mb\r\n");
 		sb.append("OS name: " + System.getProperty("os.name") + "\r\n");
 		sb.append("OS version: " + System.getProperty("os.version") + "\r\n");
 		
 		for(Plugin p : this.plugin.getServer().getPluginManager().getPlugins()) {
			plugins = p.getName() + ", " ;
 		}
 		
 		sb.append("Used plugins: " + plugins + "\r\n");
 		sb.append("---------------------- SERVER INFO END ---------------------" + "\r\n");
 		sb.append("------------------------- EXCEPTION ------------------------" + "\r\n");
 		sb.append(new exceptionSerializer(t).serialize());
 		return sb.toString();
 	}
 	
 	/**
 	 * upload the assambled report to active pasteService, and put paste link into our database
 	 * @param t Exception to be logged
 	 */
 	private void uploadReport(Throwable t) {
 		try {
 			/* Uploading report to paste service */
 			String pasteURL =  this.plugin.paste.postData(this.assambleReport(t));
 			
 			/* Send generated report URL to our server */
 			URLConnection conn = new URL("http://ec.zolli.tk/exceptionCollector/collector.php?link=" + pasteURL).openConnection();
 			conn.setDoOutput(true);
 			conn.connect();
 			conn.getContent();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Push report to our server
 	 * @param t Exception to be logged
 	 */
 	public void pushReport(Throwable t) {
 		this.uploadReport(t);
 		this.plugin.logger.log(Level.WARNING, this.plugin.local.getLocalizedString("error.exception"));
 	}
 	
 	
 }
