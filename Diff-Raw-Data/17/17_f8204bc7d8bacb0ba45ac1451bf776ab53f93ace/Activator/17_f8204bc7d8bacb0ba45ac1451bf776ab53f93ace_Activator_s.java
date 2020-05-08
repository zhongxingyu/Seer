 package de.ptb.epics.eve.viewer;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.xml.DOMConfigurator;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.resource.ColorRegistry;
 import org.eclipse.jface.resource.FontRegistry;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.resource.ImageRegistry;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.handlers.IHandlerService;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.epics.pvmanager.CompositeDataSource;
 import org.epics.pvmanager.PVManager;
 import org.epics.pvmanager.jca.JCADataSource;
 import org.epics.pvmanager.sim.SimulationDataSource;
 import org.osgi.framework.BundleContext;
 import org.xml.sax.SAXException;
 
 import de.ptb.epics.eve.data.measuringstation.IMeasuringStation;
 import de.ptb.epics.eve.data.measuringstation.processors.MeasuringStationLoader;
 import de.ptb.epics.eve.data.scandescription.ScanDescription;
 import de.ptb.epics.eve.ecp1.client.ECP1Client;
 import de.ptb.epics.eve.preferences.PreferenceConstants;
 import de.ptb.epics.eve.viewer.messages.MessagesContainer;
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class Activator extends AbstractUIPlugin {
 
 	/**
 	 * The unique identifier of the plug in
 	 */
 	public static final String PLUGIN_ID = "de.ptb.epics.eve.viewer";
 
 	// The shared instance
 	private static Activator plugin;
 	
 	private static Logger logger = Logger.getLogger(Activator.class.getName());
 	
 	private final MessagesContainer messagesContainer;
 	private final XMLFileDispatcher xmlFileDispatcher;
 	private final EngineErrorReader engineErrorReader;
 	private final ChainStatusAnalyzer chainStatusAnalyzer;
 	private IMeasuringStation measuringStation;
 	private ColorRegistry colorreg;
 	private FontRegistry fontreg;
 	
 	private ScanDescription currentScanDescription;
 	
 	private ECP1Client ecp1Client;
 	
 	private RequestProcessor requestProcessor;
 
 	private File schemaFile;
 	private String rootDir;
 	private boolean debug;
 	
 	/**
 	 * The constructor
 	 */
 	public Activator() {
 		plugin = this;
 		this.ecp1Client = new ECP1Client();
 		this.messagesContainer = new MessagesContainer();
 		this.xmlFileDispatcher = new XMLFileDispatcher();
 		this.engineErrorReader = new EngineErrorReader();
 		this.chainStatusAnalyzer = new ChainStatusAnalyzer();
 		
 		// Create a multiple data source, and add different data sources
 		CompositeDataSource composite = new CompositeDataSource();
 		composite.putDataSource("ca", new JCADataSource());
 		composite.putDataSource("sim", new SimulationDataSource());
 		// If no prefix is given to a channel, use JCA as default
 		composite.setDefaultDataSource("ca");
 		// Set the composite as the default
 		PVManager.setDefaultDataSource(composite);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void start(final BundleContext context) throws Exception {
 		super.start(context);
 		
 		readStartupParameters();
 		checkRootDir();
 		configureLogging();
 		loadMeasuringStation();
 		loadColorsAndFonts();
 		startupReport();
 		
 		this.ecp1Client.getPlayListController().addNewXMLFileListener(
 				this.xmlFileDispatcher);
 		this.ecp1Client.addErrorListener(this.engineErrorReader);
 		this.ecp1Client.addEngineStatusListener(this.chainStatusAnalyzer);
 		this.ecp1Client.addChainStatusListener(this.chainStatusAnalyzer);
 		this.requestProcessor = new RequestProcessor(Display.getCurrent());
 		this.ecp1Client.addRequestListener(this.requestProcessor);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void stop(final BundleContext context) throws Exception {
 		plugin = null;
 		super.stop(context);
 	}
 
 	/**
 	 * Returns the shared instance
 	 *
 	 * @return the shared instance
 	 */
 	public static Activator getDefault() {
 		return plugin;
 	}
 
 	/**
 	 * 
 	 * @return the root directory
 	 */
 	public String getRootDirectory() {
 		return rootDir;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public IMeasuringStation getMeasuringStation() {
 		return this.measuringStation;
 	}
 
 	/**
 	 * Returns an image descriptor for the image file at the given
 	 * plug-in relative path
 	 *
 	 * @param path the path
 	 * @return the image descriptor
 	 */
 	public static ImageDescriptor getImageDescriptor(final String path) {
 		return imageDescriptorFromPlugin(PLUGIN_ID, path);
 	}
 	
 	/**
 	 * 
 	 * @param colorname
 	 * @return
 	 */
 	public Color getColor(String colorname) {
 		Color color = colorreg.get(colorname);
 		if (color == null) {
 			return colorreg.get("COLOR_PV_INITIAL");
 		}
 		return color;
 	}
 	
 	/**
 	 * 
 	 * @param fontname
 	 * @return
 	 */
 	public Font getFont(String fontname) {
 		Font font = fontreg.get(fontname);
 		if (font == null) {
 			return fontreg.defaultFont();
 		}
 		return font;
 	}
 	
 	/**
 	 * Returns the 
 	 * 
 	 * @return the 
 	 */
 	public ECP1Client getEcp1Client() {
 		return this.ecp1Client;	
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public XMLFileDispatcher getXMLFileDispatcher() {
 		return this.xmlFileDispatcher;
 	}
 	
 	/**
 	 * Returns the {@link de.ptb.epics.eve.viewer.messages.MessagesContainer} 
 	 * used to collect messages of several types from different sources.
 	 * 
 	 * @return the messages container of the viewer
 	 */
 	public MessagesContainer getMessagesContainer() {
 		return this.messagesContainer;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public ChainStatusAnalyzer getChainStatusAnalyzer() {
 		return this.chainStatusAnalyzer;
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public ScanDescription getCurrentScanDescription() {
 		return this.currentScanDescription;
 	}
 	
 	/**
 	 * 
 	 * 
 	 * @param currentScanDescription
 	 */
 	public void setCurrentScanDescription(
 			final ScanDescription currentScanDescription) {
 		this.currentScanDescription = currentScanDescription;
 	}
 
 	/**
 	 * Connect the Engine if Ecp1Client is running
 	 */
 	public void connectEngine() {
 		// if we are not connected to the engine -> connect to it
 		if (!Activator.getDefault().getEcp1Client().isRunning()) {
 			// getting the service to execute registered commands
 			IHandlerService handlerService = (IHandlerService) 
 					PlatformUI.getWorkbench().getService(IHandlerService.class);
 			// execute the connect command
 			try {
 				handlerService.executeCommand(
 						"de.ptb.epics.eve.viewer.connectCommand", null);
 			} catch (Exception e2) {
 				logger.error(e2.getMessage(), e2);
 			}
 		}
 	}
 
 	/*
 	 * 
 	 */
 	private void readStartupParameters() throws Exception {
 		String[] args = Platform.getCommandLineArgs();
 		rootDir = "";
 		debug = false;
 		int i = 0;
 		while (i < args.length) {
 			if (args[i].equals("-eve.root")) {
 				i++;
 				rootDir = args[i];
 			}
 			if (args[i].equals("-eve.debug")) {
 				i++;
 				debug = args[i].equals("1") ? true : false;
 			}
 			i++;
 		}
 		if (rootDir.isEmpty()) {
 			String message = "parameter 'eve.root' not found!";
 			logger.fatal(message);
 			throw new Exception(message);
 		}
 	}
 	
 	/*
 	 * Checks whether the directory (given by parameter -eve.root) contains a 
 	 * folder named eve.
 	 */
 	private void checkRootDir() throws Exception {
 		if(!rootDir.endsWith("/")) {
 			rootDir += "/";
 		}
 		String path = rootDir;
 		File file = new File(path + "eve/");
 		if(!file.exists()) {
 			String message = "Root Directory not found!";
 			logger.fatal(message);
 			throw new Exception(message);
 		}
 	}
 	
 	/*
 	 * 
 	 */
 	private void configureLogging() {
 		String pathToConfigFile = new String();
 		if(debug) {
 			pathToConfigFile = rootDir + "eve/logger-debug.xml";
 		} else {
 			pathToConfigFile = rootDir + "eve/logger.xml";
 		}
 		File file = new File(pathToConfigFile);
 		if(file.exists()) {
 			// setting property so that the log4j configuration file can access it
 			System.setProperty("eve.logdir", rootDir + "eve/log");
 			DOMConfigurator.configure(pathToConfigFile);
 		} else {
 			logger.warn("Could not initialize logging. " + 
 					"Path to log configuration not found!");
 		}
 	}
 	
 	/*
 	 * 
 	 */
 	private void loadMeasuringStation() throws Exception {
 		String measuringStationDescription = new String();
 		
 		// get entry stored in the preferences
 		final String preferencesEntry = de.ptb.epics.eve.preferences.Activator.
 				getDefault().getPreferenceStore().getString(
 				PreferenceConstants.P_DEFAULT_MEASURING_STATION_DESCRIPTION);
 		
 		if(preferencesEntry.isEmpty()) {
 			File pathToDefaultMeasuringStation = 
 				new File(rootDir + "eve/default.xml");
 			if(!pathToDefaultMeasuringStation.exists()) {
 				String message = "Could not find 'default.xml' in 'eve.root'!";
 				logger.fatal(message);
 				throw new Exception(message);
 			}
 			measuringStationDescription = rootDir + "eve/default.xml";
 		} else {
 			File measuringStationFile = new File(preferencesEntry);
 			if(!measuringStationFile.exists()) {
 				// preferences entry present, but target does not exist
 				String message = "Could not find device definition file at " + 
 						measuringStationFile;
 				logger.fatal(message);
 				throw new Exception(message);
 			}
 			measuringStationDescription = preferencesEntry;
 		}
 		
 		File measuringStationDescriptionFile = 
 				new File(measuringStationDescription);
 		
 		// now we know the location of the measuring station description
 		// -> checking schema file
 		
 		/* in older versions the schema file was in the eve.root directory...
 		File pathToSchemaFile = new File(rootDir + "eve/schema.xsd");
 		if(!pathToSchemaFile.exists()) {
 			schemaFile = null;
 			return;
 		}
 		schemaFile = pathToSchemaFile;
 		*/
 		
 		schemaFile = de.ptb.epics.eve.resources.Activator.getXMLSchema();
 		if(schemaFile == null) {
 			String message = "Could not load schema file!";
 			logger.fatal(message);
 			throw new Exception(message);
 		}
 		
 		// measuring station and schema present -> start loading
 		try {
 			final MeasuringStationLoader measuringStationLoader = 
 					new MeasuringStationLoader(schemaFile);
 			measuringStationLoader.load(measuringStationDescriptionFile);
 			measuringStation = measuringStationLoader.getMeasuringStation();
 		} catch (ParserConfigurationException e) {
 			measuringStation = null;
 			logger.error(e.getMessage(), e);
 		} catch (SAXException e) {
 			measuringStation = null;
 			logger.error(e.getMessage(), e);
 		} catch (IOException e) {
 			measuringStation = null;
 			logger.error(e.getMessage(), e);
 		}
 	}
 	
 	/*
 	 * 
 	 */
 	private void startupReport() {
 		if(logger.isInfoEnabled()) {
 			logger.info("debug mode: " + debug);
 			logger.info("root directory: " + rootDir);
 			logger.info("measuring station: " + 
 					measuringStation.getLoadedFileName());
 			logger.info("schema file: " + 
 					measuringStation.getSchemaFileName());
 			IWorkspace workspace = ResourcesPlugin.getWorkspace();
 			logger.info("workspace: " + workspace.getRoot().getLocation().
 					toFile().getAbsolutePath());
 		}
 	}
 	
 	/*
 	 * 
 	 */
 	private void loadColorsAndFonts() {
 		// register fonts, colors and images
 		this.colorreg = new ColorRegistry();
 		this.fontreg = new FontRegistry();
 		Font defaultFont = fontreg.defaultFont();
 		FontData[] fontData = defaultFont.getFontData();
 		// Use a smaller font if system font is higher 11
 		for (int i = 0; i < fontData.length; i++) {
 			if (fontData[i].getHeight() > 11) fontData[i].setHeight(11);
 		}
 		fontreg.put("VIEWERFONT", fontData);
 		colorreg.put("COLOR_PV_INITIAL", 
 			Display.getCurrent().getSystemColor(SWT.COLOR_BLACK).getRGB());
 		colorreg.put("COLOR_PV_CONNECTED", 
 			Display.getCurrent().getSystemColor(SWT.COLOR_BLACK).getRGB());
 		colorreg.put("COLOR_PV_DISCONNECTED", 
 			Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY).getRGB());
 		colorreg.put("COLOR_PV_ALARM", 
 			Display.getCurrent().getSystemColor(SWT.COLOR_RED).getRGB());
 		colorreg.put("COLOR_PV_OK", 
 			Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN).getRGB());
 		colorreg.put("COLOR_PV_MINOR", new RGB(255,204,00));
 			//Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW).getRGB());
 			// system yellow is too bright -> unreadable
 		colorreg.put("COLOR_PV_MAJOR", 
 			Display.getCurrent().getSystemColor(SWT.COLOR_RED).getRGB());
 		colorreg.put("COLOR_PV_INVALID", 
 			Display.getCurrent().getSystemColor(SWT.COLOR_GRAY).getRGB());
 		colorreg.put("COLOR_PV_UNKNOWN", 
 			Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GRAY).getRGB());
 		colorreg.put("COLOR_PV_MOVING", 
 				Display.getCurrent().getSystemColor(SWT.COLOR_MAGENTA).getRGB());
 		
 		ImageRegistry imagereg = getImageRegistry();
 		imagereg.put("GREENPLUS12", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/greenPlus12.12.gif").createImage());
 		imagereg.put("GREENMINUS12", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/greenMinus12.12.gif").createImage());
 		imagereg.put("GREENGO12", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/greenGo12.12.gif").createImage());
 		imagereg.put("PLAY16", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/play.gif").createImage());
 		imagereg.put("PLAY16_DISABLED", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/play_disabled.gif").createImage());
 		imagereg.put("PAUSE16", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/pause.gif").createImage());
 		imagereg.put("STOP16", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/stop.gif").createImage());
 		imagereg.put("STOP16_DISABLED", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/stop_disabled.gif").createImage());
 		imagereg.put("SKIP16", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/skip.gif").createImage());
 		imagereg.put("HALT16", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/halt.gif").createImage());
 		imagereg.put("KILL16", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/kill.gif").createImage());
 		imagereg.put("TRIGGER16", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/trigger.gif").createImage());
 		imagereg.put("PLAYALL16", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/playAll2.gif").createImage());
 		imagereg.put("AUTOPLAY_ON", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/stckframe_running_obj.gif"));
 		imagereg.put("AUTOPLAY_OFF", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/stckframe_obj.gif"));
 		
 		imagereg.put("MOTOR", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/motor.gif").createImage());
 		imagereg.put("AXIS", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/axis.gif").createImage());
 		imagereg.put("DETECTOR", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/detector.gif").createImage());
 		imagereg.put("CHANNEL", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/channel.gif").createImage());
 		imagereg.put("DEVICE", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/device.gif").createImage());
 		imagereg.put("OPTION", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/extension_obj.gif").createImage());
 		imagereg.put("MOTORS", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/motors.gif").createImage());
 		imagereg.put("DETECTORS", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/detectors.gif").createImage());
 		imagereg.put("DEVICES", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/devices.gif").createImage());
 		imagereg.put("MOTORSAXES", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/motorsaxes.gif").createImage());
 		imagereg.put("DETECTORSCHANNELS", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/detectorschannels.gif"));
 		
 		imagereg.put("MOVEUP", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/prev_nav.gif").createImage());
 		imagereg.put("MOVEDOWN", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/next_nav.gif").createImage());
 		
 		imagereg.put("RESTOREVIEW", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/thin_restore_view.gif").createImage());
 		imagereg.put("MAXIMIZE", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/maximize.gif").createImage());
 		
 		imagereg.put("CLEAR", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/clear_co.gif").createImage());
 		imagereg.put("SAVE", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/save_edit.gif").createImage());
 		
 		imagereg.put("ASCENDING", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/alpha_mode.gif").createImage());
 		imagereg.put("DESCENDING", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/alpha_mode_reverse.gif").createImage());
 		
 		imagereg.put("CLASS", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/class_obj.png").createImage());
 	}
 }
