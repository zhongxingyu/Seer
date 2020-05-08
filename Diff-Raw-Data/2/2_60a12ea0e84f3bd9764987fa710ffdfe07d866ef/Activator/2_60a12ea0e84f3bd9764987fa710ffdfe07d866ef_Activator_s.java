 package de.ptb.epics.eve.viewer;
 
 import java.io.File;
 
 import org.apache.log4j.Logger;
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
 import org.eclipse.ui.IPerspectiveListener;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.handlers.IHandlerService;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Version;
 
 import de.ptb.epics.eve.data.measuringstation.IMeasuringStation;
 import de.ptb.epics.eve.data.scandescription.ScanDescription;
 import de.ptb.epics.eve.ecp1.client.ECP1Client;
 import de.ptb.epics.eve.ecp1.debug.ECP1ClientLogger;
 import de.ptb.epics.eve.resources.init.Parameters;
 import de.ptb.epics.eve.resources.init.Startup;
 import de.ptb.epics.eve.viewer.debug.PollInQueueSize;
 import de.ptb.epics.eve.viewer.messages.MessagesContainer;
 import de.ptb.epics.eve.viewer.views.plotview.PlotDispatcher;
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class Activator extends AbstractUIPlugin {
 
 	/** The unique identifier of the plug in */
 	public static final String PLUGIN_ID = "de.ptb.epics.eve.viewer";
 
 	// The shared instance
 	private static Activator plugin;
 	
 	private static Logger logger = Logger.getLogger(Activator.class.getName());
 	
 	private IPerspectiveListener eveViewerPerspectiveListener;
 	private WorkbenchListener workbenchListener;
 	private String defaultWindowTitle;
 	
 	private final MessagesContainer messagesContainer;
 	private final XMLDispatcher xmlFileDispatcher;
 	private final PlotDispatcher plotDispatcher;
 	private final EngineErrorReader engineErrorReader;
 	private final ChainStatusAnalyzer chainStatusAnalyzer;
 	private IMeasuringStation measuringStation;
 	private ColorRegistry colorreg;
 	private FontRegistry fontreg;
 	
 	private ScanDescription currentScanDescription;
 	
 	private ECP1Client ecp1Client;
 	private ECP1ClientLogger ecpLogger;
 	
 	private RequestProcessor requestProcessor;
 
 	private File schemaFile;
 	private Parameters startupParams;
 	
 	/**
 	 * The constructor
 	 */
 	public Activator() {
 		plugin = this;
 		try {
 			Version version = Platform.getProduct().getDefiningBundle()
 					.getVersion();
 			this.defaultWindowTitle = "eveCSS v" + version.getMajor() + "."
 					+ version.getMinor();
 		} catch (NullPointerException e) {
 			this.defaultWindowTitle = "eveCSS";
 		}
 		this.ecp1Client = new ECP1Client();
 		this.messagesContainer = new MessagesContainer();
 		this.xmlFileDispatcher = new XMLDispatcher();
 		this.plotDispatcher = new PlotDispatcher();
 		this.engineErrorReader = new EngineErrorReader();
 		this.chainStatusAnalyzer = new ChainStatusAnalyzer();
 		
 		this.eveViewerPerspectiveListener = new EveViewerPerspectiveListener();
 		this.workbenchListener = new WorkbenchListener();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void start(final BundleContext context) throws Exception {
 		super.start(context);
 		
 		startupParams = Startup.readStartupParameters();
 		if (startupParams.useDefaultDevices()) {
 			Startup.configureLogging("/tmp/", startupParams.isDebug(), logger);
 			logger.info("No 'eve.root' given, logging to '/tmp/eve/'");
 		} else {
 			Startup.checkRootDir(startupParams.getRootDir());
 			Startup.configureLogging(startupParams.getRootDir(), 
 					startupParams.isDebug(), logger);
 		}
 		this.schemaFile = Startup.loadSchemaFile(logger);
 		this.measuringStation = Startup.loadMeasuringStation(logger);
 		
 		loadColorsAndFonts();
 		startupReport();
 		
 		this.ecp1Client.getPlayListController().addNewXMLFileListener(
 				this.xmlFileDispatcher);
 		this.xmlFileDispatcher.addPropertyChangeListener(
 				XMLDispatcher.SCAN_DESCRIPTION_PROP, plotDispatcher);
 		this.ecp1Client.addChainStatusListener(plotDispatcher);
 		this.ecp1Client.addErrorListener(this.engineErrorReader);
 		this.ecp1Client.addEngineStatusListener(this.chainStatusAnalyzer);
 		this.ecp1Client.addChainStatusListener(this.chainStatusAnalyzer);
 		this.requestProcessor = new RequestProcessor(Display.getCurrent());
 		this.ecp1Client.addRequestListener(this.requestProcessor);
 		
 		if (startupParams.isDebug()) {
 			this.ecpLogger = new ECP1ClientLogger();
 			this.ecp1Client.addChainStatusListener(ecpLogger);
 			this.ecp1Client.addConnectionStateListener(ecpLogger);
 			this.ecp1Client.addEngineStatusListener(ecpLogger);
 			this.ecp1Client.addErrorListener(ecpLogger);
 			this.ecp1Client.addMeasurementDataListener(ecpLogger);
 			this.ecp1Client.addRequestListener(ecpLogger);
 		} 
 		
 		PlatformUI.getWorkbench().getActiveWorkbenchWindow().
 			 addPerspectiveListener(this.eveViewerPerspectiveListener);
 		PlatformUI.getWorkbench().addWorkbenchListener(workbenchListener);
 		
 		if (logger.isDebugEnabled()) {
 			new Thread(new PollInQueueSize()).start();
 		}
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
 	 * @return windowTitle
 	 */
 	public String getDefaultWindowTitle() {
 		return this.defaultWindowTitle;
 	}
 	
 	/**
 	 * 
 	 * @return the root directory
 	 */
 	public String getRootDirectory() {
 		return this.startupParams.getRootDir();
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public IMeasuringStation getMeasuringStation() {
 		return this.measuringStation;
 	}
 
 	/**
 	 * @return the schemaFile
 	 */
 	public File getSchemaFile() {
 		return schemaFile;
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
 	public XMLDispatcher getXMLDispatcher() {
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
 	 * Should be used with great care. Maybe not thread safe!
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
 	private void startupReport() {
 		if(logger.isInfoEnabled()) {
 			logger.info("debug mode: " + startupParams.isDebug());
 			logger.info("eve.root set: " + !startupParams.useDefaultDevices());
 			logger.info("root directory: " + startupParams.getRootDir());
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
 			if (fontData[i].getHeight() > 11) {
 				fontData[i].setHeight(11);
 			}
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
 		
 		imagereg.put("QUESTION", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/signed_unknown.gif").createImage());
 		
 		imagereg.put("DEBUG", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/debug_exc.gif").createImage());
 		imagereg.put("FATAL", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/fatalerror_obj.gif").createImage());
 		imagereg.put("SYSTEM", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/build_exec.gif").createImage());
 		imagereg.put("ERROR", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/error.gif").createImage());
 		imagereg.put("INFO", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/information.gif").createImage());
 		imagereg.put("WARNING", imageDescriptorFromPlugin(
				PLUGIN_ID, "icons/minor.gif").createImage());
 		imagereg.put("SORTARROW", imageDescriptorFromPlugin(
 				PLUGIN_ID, "icons/view_menu.gif").createImage());
 	}
 }
