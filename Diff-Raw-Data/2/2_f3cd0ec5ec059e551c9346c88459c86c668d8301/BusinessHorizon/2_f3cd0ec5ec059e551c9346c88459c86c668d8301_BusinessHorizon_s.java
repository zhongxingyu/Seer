 package org.bh;
 
 import java.lang.Thread.UncaughtExceptionHandler;
 
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 
 import org.apache.log4j.Logger;
 import org.bh.platform.PlatformController;
 import org.bh.platform.PluginManager;
 import org.bh.platform.Services;
 import org.bh.platform.i18n.ITranslator;
 
 /**
  * 
  * This is the entry class for Business Horizon.
  * 
  * The main method of this class will be called when Business Horizon starts.
  * 
  * @author Robert Vollmer
  * @version 0.2, 20.12.2009
  * 
  * 
  */
 
 public class BusinessHorizon {
 	public static final boolean DEBUG = true;
 	private static final Logger log = Logger.getLogger(BusinessHorizon.class);
 
 	/**
 	 * @param args
 	 *            Commandline arguments
 	 * @throws Exception
 	 */
 	public static void main(String[] args) throws Exception {
 		if (DEBUG)
 			Services.setupLogger();
 		
 		log.info("Business Horizon is starting...");
 		
 		Thread
 		.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
 			@Override
 			public void uncaughtException(Thread t, Throwable e) {
 				log.error("Uncaught exception", e);
 			}
 		});
		giglkn
 		if (SVN.isRevisionSet())
 			log.info("SVN Revision is " + SVN.getRevision());
 		
 		// Check if JRE is Java 6 Update 10, else quit.
 		if (!Services.jreFulfillsRequirements()) {	
 			String message = Services.getTranslator().translate("PjreRequirement", ITranslator.LONG);
 			String title = Services.getTranslator().translate("PjreRequirement");
 			log.error(message);
 			JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
 			System.exit(1);
 		}
 		
 		System.setSecurityManager(null);
 		
 		PluginManager.init();
 		
 		
 		// set menu name
 		if(System.getProperty("os.name").startsWith("Mac OS X"))
 			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Business Horizon");
 
 		// set Look&Feel
 		Services.setNimbusLookAndFeel();
 		
 		// Invoke start of BHMainFrame
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				PlatformController.getInstance();
 			}
 		});
 	}
 }
