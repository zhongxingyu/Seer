 package org.app;
 
 import net.sourceforge.jpcap.capture.CaptureDeviceNotFoundException;
 import net.sourceforge.jpcap.capture.CaptureDeviceOpenException;
 import net.sourceforge.jpcap.capture.CapturePacketException;
 import net.sourceforge.jpcap.capture.InvalidFilterException;
 
 import org.apache.log4j.ConsoleAppender;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PatternLayout;
 import org.app.ui.ArgsPresentator;
 import org.app.util.CommandLineParameters;
 import org.app.util.Sniffer;
 
 public class IOLHacker {
 	
 	private static final Logger LOG = Logger.getLogger(IOLHacker.class);
 	
 	public static void main(String[] args) {
 		initializateLogger();
 		CommandLineParameters clp = new CommandLineParameters();
 		clp.load(args);
 		Logger.getRootLogger().setLevel(clp.getLoggingLevel());
 		
		Sniffer sniffer = new Sniffer();
 		try{
 			ArgsPresentator.displayAppHeader();
 			sniffer.sniff(clp.getInterface());
 		} catch (CaptureDeviceNotFoundException i){
 			LOG.error("There are no interfaces or devices detected for sniffing.");
 		} catch (CaptureDeviceOpenException e) {
 			LOG.error("The interface selected cannot be opened.");
 		} catch (InvalidFilterException e) {
 			LOG.error("The filter indicated is not valid (as user you should never see this, if you do, pleas contact us).");
 		} catch (CapturePacketException e) {
 			LOG.error("An error has occurred while capturing some packet.");
 		} catch (UnsatisfiedLinkError e){
 			StringBuilder sb = new StringBuilder();
 			sb.append("You'r jpcap or libcap library is not well installed, you must have the native libraries installed for this to work. ");
 			sb.append("Make shoure you have yout .dll, .jnlib or .so library in one of the following paths: ");
 			sb.append(java.lang.System.getProperty("java.library.path"));
 			LOG.error(sb.toString());
 		} catch (Exception a){
 			LOG.error("An unexpected error has occurred.");
 		}
  
 	}
 	
 	private static void initializateLogger(){
 		/* Logger initialization */
 		ConsoleAppender consoleAppender = new ConsoleAppender();
 		String PATTERN = "%d [%p] - %c - %m%n";
 		consoleAppender.setLayout(new PatternLayout(PATTERN));
 		consoleAppender.setThreshold(Level.TRACE);
 		consoleAppender.activateOptions();
 		Logger.getRootLogger().addAppender(consoleAppender);	
 	}
 
 	
 }
 
