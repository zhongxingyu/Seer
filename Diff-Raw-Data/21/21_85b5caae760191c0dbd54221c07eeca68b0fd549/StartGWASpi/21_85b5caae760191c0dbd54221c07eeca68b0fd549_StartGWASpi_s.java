 package org.gwaspi.gui;
 
 import ch.qos.logback.classic.LoggerContext;
 import ch.qos.logback.classic.spi.ILoggingEvent;
 import ch.qos.logback.core.FileAppender;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.text.ParseException;
 import java.util.Arrays;
 import java.util.List;
 import javax.swing.GroupLayout;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JTabbedPane;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import org.gwaspi.cli.CliExecutor;
 import org.gwaspi.constants.cGlobal;
 import org.gwaspi.global.Config;
 import org.gwaspi.global.Text;
 import org.gwaspi.gui.utils.Dialogs;
 import org.gwaspi.model.MatricesList;
 import org.gwaspi.threadbox.SwingDeleterItemList;
 import org.gwaspi.threadbox.SwingWorkerItemList;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author Fernando Muñiz Fernandez
  * IBE, Institute of Evolutionary Biology (UPF-CSIC)
  * CEXS-UPF-PRBB
  */
 public class StartGWASpi extends JFrame {
 
 	private static final Logger log = LoggerFactory.getLogger(StartGWASpi.class);
 
 	static {
 		Thread shutdownDerby = new Thread() {
 			@Override
 			public void run() {
 				StartGWASpi.shutdownBackend();
 			}
 		};
 		Runtime.getRuntime().addShutdownHook(shutdownDerby);
 	}
 
 	// create a JFrame to hold everything
 	// TODO convert all this to non-static, and make configuration in general more modular (eg, use swing preferences for everything?
 	public static boolean guiMode = true;
 	private static boolean logToFile = false;
 	public static boolean logOff = false;
 	private static String logPath;
 	public static JFrame mainGUIFrame = new JFrame(cGlobal.APP_NAME);
 	public static JTabbedPane allTabs = new JTabbedPane();
 	public static long maxHeapSize = 0;
 	public static long maxProcessMarkers = 0;
 
 	public StartGWASpi() {
 	}
 
 	public void start(List<String> args) throws IOException, SQLException, ParseException, UnsupportedLookAndFeelException {
 
 		// Get current size of heap in bytes
		maxHeapSize = Math.round(Runtime.getRuntime().totalMemory() / 1048576); // heapSize in MB
		maxProcessMarkers = Math.round(maxHeapSize * 625); // 1.6GB needed for 10^6 markers (safe, 1.4 - 1.5 real)
 
 		if (args.contains("script")) {
 			guiMode = false;
 			if (args.contains("log")) {
 				logToFile = true;
 				logPath = args.get(args.indexOf("log") + 1).toString();
 			}
 
 			File scriptFile = new File(args.get(args.indexOf("script") + 1).toString());
 			if (scriptFile.exists()) {
 				if (maxHeapSize > 254) {
 					log.info(maxHeapSize + Text.App.memoryAvailable1 + "\n"
 							+ Text.App.memoryAvailable2 + maxProcessMarkers + Text.App.memoryAvailable3);
 				} else {
 					log.info(maxHeapSize + Text.App.memoryAvailable1 + "\n"
 							+ Text.App.memoryAvailable2 + maxProcessMarkers + Text.App.memoryAvailable3 + "\n"
 							+ Text.App.memoryAvailable4);
 				}
 
 				boolean initialized = initGWASpi(false, scriptFile);
 
 				if (initialized) {
 					CliExecutor cliExecutor = new CliExecutor(scriptFile);
 					boolean success = cliExecutor.execute();
 					if (!success) {
 						throw new IOException("Failed to execute script '" + scriptFile + "'");
 					}
 				}
 			} else {
 				log.error(Text.Cli.wrongScriptFilePath, scriptFile);
 			}
 		} else {
 			if (args.contains("nolog")) {
 				logOff = true;
 			}
 
 			mainGUIFrame.addWindowListener(new WindowAdapter() {
 				@Override
 				public void windowClosing(WindowEvent we) {
 					int jobsPending = SwingWorkerItemList.sizePending() + SwingDeleterItemList.sizePending();
 					if (jobsPending == 0) {
 						we.getWindow().setVisible(false);
 					} else {
 						int decision = Dialogs.showConfirmDialogue(Text.App.jobsStillPending);
 						if (decision == JOptionPane.YES_OPTION) {
 							we.getWindow().setVisible(false);
 						}
 					}
 				}
 			});
 
 			try {
 				// Set cross-platform Java L&F (also called "Metal")
 				//UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
 				// Set System L&F
 				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 			} catch (UnsupportedLookAndFeelException ex) {
 				log.warn(null, ex);
 			} catch (ClassNotFoundException ex) {
 				log.warn(null, ex);
 			} catch (InstantiationException ex) {
 				log.warn(null, ex);
 			} catch (IllegalAccessException ex) {
 				log.warn(null, ex);
 			}
 
 			try {
 				initGWASpi(true, null);
 
 				if (maxHeapSize > 254) {
 					Dialogs.showInfoDialogue(maxHeapSize + Text.App.memoryAvailable1 + "\n"
 							+ Text.App.memoryAvailable2 + maxProcessMarkers + Text.App.memoryAvailable3);
 				} else {
 					Dialogs.showInfoDialogue(maxHeapSize + Text.App.memoryAvailable1 + "\n"
 							+ Text.App.memoryAvailable2 + maxProcessMarkers + Text.App.memoryAvailable3 + "\n"
 							+ Text.App.memoryAvailable4);
 				}
 			} catch (RuntimeException ex) {
 				log.error(Text.App.warnOnlyOneInstance, ex);
 				Dialogs.showWarningDialogue(Text.App.warnOnlyOneInstance);
 				return;
 			} catch (OutOfMemoryError ex) {
 				log.error(Text.App.outOfMemoryError, ex);
 			} catch (Exception ex) {
 				log.error(null, ex);
 			}
 
 			mainGUIFrame.setExtendedState(MAXIMIZED_BOTH);
 		}
 	}
 
 	private boolean initGWASpi(boolean startWithGUI, File scriptFile) throws IOException, SQLException {
 
 		// initialize configuration of moapi
 		boolean isInitiated = Config.initPreferences(startWithGUI, scriptFile);
 
 		if (isInitiated) {
 			if (startWithGUI) {
 				mainGUIFrame.setSize(1100, 800);
 				mainGUIFrame.setResizable(true);
 
 				GWASpiExplorerPanel panel0 = GWASpiExplorerPanel.getSingleton();
 				ProcessTab panel1 = ProcessTab.getSingleton();
 
 				allTabs.addTab(Text.App.Tab0, panel0);
 				allTabs.addTab(Text.App.Tab1, panel1);
 
 				GroupLayout layout = new GroupLayout(getContentPane());
 				getContentPane().setLayout(layout);
 				layout.setHorizontalGroup(
 						layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(allTabs, GroupLayout.DEFAULT_SIZE, 1000, Short.MAX_VALUE));
 				layout.setVerticalGroup(
 						layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(allTabs, GroupLayout.DEFAULT_SIZE, 750, Short.MAX_VALUE));
 
 				mainGUIFrame.getContentPane().add(allTabs);
 				mainGUIFrame.setVisible(true);
 				mainGUIFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 			} else {
 				if (logToFile) {
 					// LOGGING OF SYSTEM OUTPUT
 					if (logPath == null) {
 						logPath = Config.getConfigValue(Config.PROPERTY_REPORTS_DIR, "") + "/cli.log";
 					}
 					FileAppender<ILoggingEvent> fileAppender = new FileAppender<ILoggingEvent>();
 					fileAppender.setFile(logPath);
 					fileAppender.start();
 					LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
 					lc.getLogger(Logger.ROOT_LOGGER_NAME).addAppender(fileAppender);
 				}
 			}
 		}
 
 		return isInitiated;
 	}
 
 	private static void shutdownBackend() {
 
 		try {
 			MatricesList.shutdownBackend();
 		} catch (IOException ex) {
 			log.error(null, ex);
 		}
 	}
 
 	public static void exit() {
 		System.exit(0);
 	}
 
 	public static void main(String[] args) throws IOException, SQLException, ParseException, UnsupportedLookAndFeelException {
 		StartGWASpi startGWASpi = new StartGWASpi();
 		startGWASpi.start(Arrays.asList(args));
 	}
 }
