 package net.mms_projects.tostream;
 
 import net.mms_projects.tostream.ui.InterfaceLoader;
 import net.mms_projects.tostream.ui.cli.CliInterface;
 import net.mms_projects.tostream.ui.swt.SwtInterface;
 
 public class ToStream {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		Settings settings = new Settings();
 		settings.loadProperties();
 
 		FfmpegWrapper wrapperThread = new FfmpegWrapper(settings);
 		wrapperThread.setDaemon(true);
 		wrapperThread.start();
 
 		InterfaceLoader uiLoader;
 		if (args.length != 0) {
			if (args[0] == "cli") {
 				uiLoader = new CliInterface(
 						wrapperThread, settings);
 			}
 			else {
 				uiLoader = new SwtInterface(
 						wrapperThread, settings);
 			}
 		} else {
 			uiLoader = new SwtInterface(
 					wrapperThread, settings);
 		}
 	}
 
 	public static String getApplicationName() {
 		return "2STREAM";
 	}
 
 	public static String getVersion() {
 		return "0.0.1";
 	}
 
 }
