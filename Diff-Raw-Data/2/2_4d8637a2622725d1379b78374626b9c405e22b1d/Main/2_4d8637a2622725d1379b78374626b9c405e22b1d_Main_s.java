 package com.clusterclient;
 
 import java.io.IOException;
 import java.util.logging.FileHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.logging.SimpleFormatter;
 
 import javax.swing.UIManager;
 
 import com.clusterclient.gui.ClusterRequestDialog;
 import com.clusterclient.gui.MainWindow;
 import com.clusterclient.ssh.SSHCommandServiceFactory;
 import com.clusterclient.xml.XmlEnvironmentRepository;
 
 public class Main implements ConnectListener {
 
 	private final static Logger LOGGER = Logger.getLogger(Main.class.getName());
 
 	private MainWindow ui;
 	private EnvironmentRepository repository;
 	private Configurations configurations = new Configurations(
 			"configuration.properties");
 
 	/**
 	 * @param args
 	 * @throws Exception
 	 */
 	public static void main(String[] args) throws Exception {
 		setUpLogging();
 		setUpGuiLookAndFeel();
 
 		new Main().startApplication();
 	}
 
 	private void startApplication() throws Exception {
 		repository = XmlEnvironmentRepository.load(configurations
 				.getEnvironmentsFile());
 
 		ui = new MainWindow(repository, configurations, this);
 		LOGGER.info("MainWindow starting...");
 
 		connect();
 	}
 
 	public void connect() {
 		String env = requestEnvironment();
 		if ("".equals(env)) {
 			LOGGER.info("User did not select an environment");
 			// User cancelled
 			ui.updateStatus("No environmnet selected");
 			return;
 		}
 		configureAllModes(env);
 		ui.start();
 	}
 
 	private void configureAllModes(String env) {
 		for (String mode : this.configurations.getModes()) {
 			configure(env, mode);
 		}
 	}
 
 	private String requestEnvironment() {
 		ClusterRequestDialog envRequest = new ClusterRequestDialog(ui, repository);
 		return envRequest.getEnvironment();
 	}
 
 	private void configure(String env, String mode) {
 		LOGGER.info("Configuring application to " + env + " in " + mode);
 		CommandServiceFactory serviceFactory = getServiceFactory(ui);
 		for (final String host : repository.findHostsWithId(env)) {
 			serviceFactory.makeCommandService(host, mode);
 		}
 	}
 
 	private CommandServiceFactory getServiceFactory(
 			CommandListenerFactory listenerFactory) {
 		String username = repository.findUserName();
 		String password = repository.findPassword();
 		return new SSHCommandServiceFactory(listenerFactory, username,
 				password, configurations.getSshPort(), configurations);
 	}
 
 	private static void setUpLogging() {
 		try {
 			FileHandler handler = new FileHandler("logs/log-grabber.log",
 					100000, 5, true);
 			handler.setFormatter(new SimpleFormatter());
			Logger logger = Logger.getLogger("com.dnb.loggrabber");
 			logger.addHandler(handler);
 			logger.setUseParentHandlers(false);
 			logger.setLevel(Level.ALL);
 		} catch (IOException e) {
 			LOGGER.log(Level.WARNING, "Failed to initialize logger handler.", e);
 		}
 	}
 
 	private static void setUpGuiLookAndFeel() {
 		try {
 			// Set System L&F
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (Exception e) {
 			LOGGER.log(
 					Level.WARNING,
 					"Error setting system look and feel.\nIgnoring and trying default look and feel.",
 					e);
 		}
 	}
 
 }
