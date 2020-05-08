 package be.kuleuven.med.brainfuck;
 
 import java.awt.AWTException;
 import java.awt.Robot;
 import java.util.EventObject;
 import java.util.concurrent.TimeUnit;
 
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import net.miginfocom.swing.MigLayout;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.jdesktop.application.Action;
 import org.jdesktop.application.Application;
 import org.jdesktop.application.ResourceMap;
 import org.jdesktop.application.SingleFrameApplication;
 import org.jdesktop.application.Task;
 import org.jdesktop.application.Task.BlockingScope;
 import org.jdesktop.application.TaskService;
 import org.jdesktop.application.utils.AppHelper;
 
 import be.kuleuven.med.brainfuck.core.LedMatrixAppController;
 import be.kuleuven.med.brainfuck.core.LedMatrixAppModel;
 import be.kuleuven.med.brainfuck.core.LedMatrixAppView;
 import be.kuleuven.med.brainfuck.core.LedMatrixHelper;
 import be.kuleuven.med.brainfuck.io.LedMatrixConnector;
 import be.kuleuven.med.brainfuck.settings.ExperimentSettings;
 import be.kuleuven.med.brainfuck.settings.LedMatrixSettings;
 import be.kuleuven.med.brainfuck.settings.SettingsManager;
 import be.kuleuven.med.brainfuck.task.AbstractTask;
 
 public class LedMatrixApp extends SingleFrameApplication {
 
 	public static final String SAVE_SETTINGS_ACTION = "saveSettings";
 	
 	public static final String APP_VERSION = "Application.version";
 	public static final String APP_TITLE = "Application.title";
 	public static final String APP_NAME = "Application.name";
 	public static final String APP_BUILD_DATE = "Application.build.date";
 	public static final String APP_MAIN_FONT = "Application.mainFont";
 
 	private static final Logger LOGGER = Logger.getLogger(LedMatrixApp.class);
 
 	private SettingsManager settingsManager;
 	
 	private LedMatrixAppController ledMatrixController;
 
 	/*
 	 * Main method launching the application. 
 	 * After logging has been set up, the application will launch using the swing application framework (SAF).
 	 */
 	public static void main(String[] args) {
 		SettingsManager.configureLog4j();
 		LOGGER.log(Level.INFO, "Detected platform is " + AppHelper.getPlatform());
 		launch(LedMatrixApp.class, args);
 	}
 
 	/**
 	 * A convenient static getter for the application instance.
 	 * @return the instance of RunwalkVideoApp
 	 */
 	public static LedMatrixApp getApplication() {
 		return Application.getInstance(LedMatrixApp.class);
 	}
 
 
 	/** {@inheritDoc} */ 
 	@Override
 	protected void initialize(String[] args) { 
 		LOGGER.log(Level.INFO, "Starting " + getTitle());
 		// register an exception handler on the EDT
 		settingsManager = new SettingsManager(getContext().getLocalStorage().getDirectory());
 		settingsManager.loadSettings();
 		LedMatrixConnector serialConnector = new LedMatrixConnector(settingsManager.getLedMatrixPortSettings());
 		LedMatrixSettings ledMatrixSettings = settingsManager.getLedMatrixSettings();
 		ExperimentSettings experimentSettings = settingsManager.getExperimentSettings();
 		LedMatrixHelper ledMatrixHelper = new LedMatrixHelper(ledMatrixSettings);
 		LedMatrixAppModel ledMatrixModel = new LedMatrixAppModel(ledMatrixSettings, experimentSettings);
 		ledMatrixController = new LedMatrixAppController(ledMatrixModel, ledMatrixHelper, serialConnector);
		getContext().getTaskService().execute(ledMatrixController.updateSerialPortNames());
 	}
 
 	/**
 	 * Initialize and show the application GUI.
 	 */
 	@Override
 	protected void startup() {
 		JPanel mainPanel = new JPanel(new MigLayout("fill, nogrid, flowy, insets 10"));
 		LedMatrixAppView ledMatrixView = new LedMatrixAppView(ledMatrixController);
 		ledMatrixController.initView(ledMatrixView);
 		mainPanel.add(ledMatrixView);
 		//StatusPanel statusPanel = new StatusPanel();
 		//mainPanel.add(statusPanel, "height 30!, gapleft push");
 		getMainFrame().add(mainPanel);
 		show(getMainFrame());
 		getMainFrame().pack();
 		ledMatrixController.updateLedMatrix();
 	}
 
 	@Override
 	public void exit(final EventObject event) {
 		if (getContext().getTaskService().isTerminated()) {
 			LOGGER.debug("Taskservice terminated. byebye...");
 			super.exit(event);
 		} else {
 			ResourceMap resourceMap = getContext().getResourceMap();
 			int result = JOptionPane.showConfirmDialog(getMainFrame(), 
 					resourceMap.getString("quit.confirmDialog.text"), 
 					resourceMap.getString("quit.Action.text"), JOptionPane.OK_CANCEL_OPTION);
 			if (result == JOptionPane.OK_OPTION) {
 				LOGGER.debug("Shutdown initiated...");
 				//executeAction(getApplicationActionMap(), "uploadLogFiles");
 				//executeAction(getApplicationActionMap(), SAVE_SETTINGS_ACTION);
 				awaitShutdown(event);
 			}
 		}
 	}
 	
 	@Action(block=BlockingScope.APPLICATION)
 	public Task<Void, Void> saveSettings() {
 		return new AbstractTask<Void, Void>(SAVE_SETTINGS_ACTION) {
 
 			protected Void doInBackground() throws Exception {
 				settingsManager.saveSettings();
 				return null;
 			}
 			
 		};
 	}
 
 	/**
 	 * Start a new {@link Thread} and wait until the {@link TaskService} is completely 
 	 * terminated before exiting the application.
 	 */
 	private void awaitShutdown(final EventObject event) {
 		new Thread(new Runnable() {
 
 			public void run() {
 				LOGGER.debug("Taskservice shutting down...");
 				getContext().getTaskService().shutdown();
 				while(!getContext().getTaskService().getTasks().isEmpty()) {
 					try {
 						getContext().getTaskService().awaitTermination(10, TimeUnit.SECONDS);
 						LOGGER.debug("Waiting for tasks on EDT to end...");
 						new Robot().waitForIdle();
 					} catch (AWTException e) {
 						LOGGER.error(e);
 					} catch (InterruptedException e) {
 						LOGGER.error(e);
 					}
 				}
 				exit(event);
 			}
 
 		}, "AwaitShutdownThread").start();
 	}
 
 	public String getTitle() {
 		return getResourceString(APP_TITLE);
 	}
 
 	public String getName() {
 		return getResourceString(APP_NAME);
 	}
 
 	public String getVersionString() {
 		return getResourceString(APP_NAME) + "-" + getResourceString(APP_VERSION) + "-" + getResourceString(APP_BUILD_DATE);
 	}
 
 	private String getResourceString(String resourceName) {
 		return getContext().getResourceMap().getString(resourceName);
 	}
 
 }
