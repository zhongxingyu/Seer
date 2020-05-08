 package de.ptb.epics.eve.editor;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.xml.DOMConfigurator;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.ILogListener;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 import org.xml.sax.SAXException;
 
 import de.ptb.epics.eve.data.measuringstation.IMeasuringStation;
 import de.ptb.epics.eve.data.measuringstation.MeasuringStation;
 import de.ptb.epics.eve.data.measuringstation.processors.MeasuringStationLoader;
 import de.ptb.epics.eve.preferences.PreferenceConstants;
 import de.ptb.epics.eve.data.measuringstation.filter.ExcludeFilter;
 import de.ptb.epics.eve.editor.logging.EclipseLogListener;
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class Activator extends AbstractUIPlugin {
 	
 	/** 
 	 * The unique identifier of the plug in 
 	 */
 	public static final String PLUGIN_ID = "de.ptb.epics.eve.editor";
 	
 	// The shared instance
 	private static Activator plugin;
 	
 	private static Logger logger = Logger.getLogger(Activator.class.getName());
 	
	/*
	 * Listener for the Eclipse Logger. catches logs of eclipse and forwards 
	 * them to the log4j logger. (only logs with level 
	 * org.eclipse.core.runtime.IStatus.WARNING or
	 * org.eclipse.core.runtime.IStatus.ERROR are forwarded).
	 */
 	private ILogListener logListener;
 	
 	private IMeasuringStation measuringStation;
 	private ExcludeFilter excludeFilter;
 	
 	private File schemaFile;
 	private String rootDir;
 	private boolean debug;
 	
 	private final String defaultWindowTitle = "Control System Studio";
 	
 	private EveEditorPerspectiveListener eveEditorPerspectiveListener;
 	
 	/**
 	 * The constructor
 	 */
 	public Activator() {
 		plugin = this;
 		eveEditorPerspectiveListener = new EveEditorPerspectiveListener();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void start(final BundleContext context) throws Exception {
 		super.start(context);
 		
 		readStartupParameters();
 		if(!checkRootDir()) {
 			context.getBundle().stop();
 			context.getBundle().uninstall();
 			return;
 		}
 		configureLogging();
 		loadMeasuringStation();
 		startupReport();
 
 		PlatformUI.getWorkbench().getActiveWorkbenchWindow().
 				addPerspectiveListener(eveEditorPerspectiveListener);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void stop(final BundleContext context) throws Exception {
 		PlatformUI.getWorkbench().getActiveWorkbenchWindow().
 			removePerspectiveListener(eveEditorPerspectiveListener);
 		eveEditorPerspectiveListener = null;
 		
 		Platform.removeLogListener(logListener);
 		logListener = null;
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
 	 * Returns the root directory the plug in is working on.
 	 * 
 	 * @return the root directory the plug in is working on
 	 */
 	public String getRootDirectory() {
 		return rootDir;
 	}
 	
 	/**
 	 * 
 	 * @return measuring station
 	 */
 	public IMeasuringStation getMeasuringStation() {
 		return this.excludeFilter;
 	}
 
 	/**
 	 * 
 	 * @return schema
 	 */
 	public File getSchemaFile() {
 		return this.schemaFile;
 	}
 	
 	/**
 	 * 
 	 * @return windowTitle
 	 */
 	public String getDefaultWindowTitle() {
 		return this.defaultWindowTitle;
 	}
 	
 	/*
 	 * 
 	 */
 	private void readStartupParameters() {
 		String[] args = Platform.getCommandLineArgs();
 		debug = false;
 		int i = 0;
 		while (i < args.length)
 		{
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
 	}
 	
 	/*
 	 * Checks whether the directory (given by parameter -rootdir) contains a 
 	 * folder named eve.
 	 * 
 	 * @return <code>true</code> if the root directory contains a folder named 
 	 * 			eve, <code>false</code> otherwise
 	 */
 	private boolean checkRootDir() {
 		if(!rootDir.endsWith("/")) rootDir += "/";
 		String path = rootDir;
 		File file = new File(path + "eve/");
 		if(!file.exists()) {
 			return false;
 		}
 		return true;
 	}
 	
 	/*
 	 * Configures the logging. if the optional debug parameter is passed with 
 	 * argument 1 a more comprehensive logging configuration is loaded than 
 	 * without the parameter.
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
 			
 			logListener = new EclipseLogListener();
 			Platform.addLogListener(logListener);
 		}
 	}
 	
 	/*
 	 * 
 	 */
 	private void loadMeasuringStation() {
 		
 		String measuringStationDescription = new String();
 		
 		// get entry stored in the preferences
 		final String preferencesEntry = de.ptb.epics.eve.preferences.Activator.
 				getDefault().getPreferenceStore().getString(
 				PreferenceConstants.P_DEFAULT_MEASURING_STATION_DESCRIPTION);
 		
 		if(preferencesEntry.isEmpty()) {
 			File pathToDefaultMeasuringStation = 
 				new File(rootDir + "eve/default.xml");
 			if(!pathToDefaultMeasuringStation.exists()) {
 				measuringStation = null;
 				schemaFile = null;
 				return;
 			}
 			measuringStationDescription = rootDir + "eve/default.xml";
 		} else {
 			File measuringStationFile = new File(preferencesEntry);
 			if(!measuringStationFile.exists()) {
 				// preferences entry present, but target does not exist
 				measuringStation = null;
 				schemaFile = null;
 				return;
 			}
 			measuringStationDescription = preferencesEntry;
 		}
 		
 		File measuringStationDescriptionFile = 
 				new File(measuringStationDescription);
 		
 		// now we know the location of the measuring station description
 		// -> checking schema file
 		
 		File pathToSchemaFile = new File(rootDir + "eve/schema.xsd");
 		if(!pathToSchemaFile.exists()) {
 			schemaFile = null;
 			return;
 		}
 		schemaFile = pathToSchemaFile;
 		
 		// measuring station and schema present -> start loading
 
 		final MeasuringStationLoader measuringStationLoader = 
 				new MeasuringStationLoader(schemaFile);
 		try {
 			measuringStationLoader.load(measuringStationDescriptionFile);
 			measuringStation = measuringStationLoader.getMeasuringStation();
 			
 			this.excludeFilter = new ExcludeFilter();
 			this.excludeFilter.setSource(this.measuringStation);
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
 				((MeasuringStation)measuringStation).getName() + " (" +
 				measuringStation.getVersion() + ")");
 			logger.info("measuring station location: " + 
 					measuringStation.getLoadedFileName());
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			logger.info("workspace: " + workspace.getRoot().getLocation().
					toFile().getAbsolutePath());
 		}
 	}
 }
