 package fws_master;
 
 
 import java.io.File;
 import java.util.logging.FileHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.logging.SimpleFormatter;
 
 import org.eclipse.swt.*;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.*;
 
 
 /**
  * This is the main class of this program. It creates all the windows, loads the configuration and starts the data processing threads.
  * The path of the configuration file is dependent of the operating system. 
  * The flow of the data is:
  * The User can add and edit parameters. These parameters represent measurements of the slaves. To get the values of the slaves, it's necessary
  * to add the station to the master. For each station you can setup which parameters should be polled of the slave. This process is called binding.
  * There are two kinds of bindings. StationConfigurationBindings and StationInputBindings. 
  * <ul>
  * <li>A ConfigurationBinding is transfered from the master to the slave</li>
  * <li>A InputBinding is transfered from the slave to the master</li>
  * </ul>
  * The configuration is transfered to the slave when the user requests it. During configuration phase the user sets a polling interval for 
  * each station.
  * For each station a own thread is started. This thread pulls the measurement values over the modbus protocol. Another Thread collects the
  * data from the station threads. For each active InputBinding the collector thread creates an entry in a text file that represents the current
  * status of all sensors. 
  * The measurements are also added to a MeasurementHistory. This history is saved for each parameter on each station. This History has no
  * real references to stations or parameters. It saves all necessary data to plot diagrams. The history is separated in two parts.
  * <ol>
  * <li>Recent history: All values of the last hours. At least 24 hours are saved</li>
  * <li>Long term History: All values of each day are aggregated in one representing value (HistoryFunction determines how this value is computed).
  * This representing is saved in a history of the last 365 days</li>
  * </ol>
  * This history is serialized to the hard disk. 
  * For each binding the user can configure what diagrams should be generated. It's possible to generate a diagram of the last hours or of the 
  * long term history. It's also configurable how much data should be presented in the diagram. It's also possible to draw more one data series
  * in one diagram. 
  * 
  *  The configuration is saved to a xml file (see PersistencePreferences, MasterContentHandler, ParameterContentHandler, StationContentHandler)
  * @author Johannes Kasberger
  *
  */
 public class FWSMaster {
 	private ParameterController parameter_controller;
 	private StationController station_controller;
 	@SuppressWarnings("unused")
 	private ViewMain view;
 	private Display display;
 	private Shell shell;
 	private MeasurementCollector collector;
 	private String configDir;
 	private String outDir;
 	private int generatorTime;
 	private static Logger log = Logger.getLogger("fws_master");
 	
 	/*private void generateParameters() {
 		Config_Parameter c = new Config_Parameter("Messintervall",this.parameter_controller);
 		this.parameter_controller.addParameter(c);
 		
 		Input_Parameter i = new Input_Parameter("Temperatur",this.parameter_controller,Units.TEMPERATURE,Output_Formats.NK1,History_Functions.MAX);
 		this.parameter_controller.addParameter(i);
 		
 		Station s = new Station("Dach",this.station_controller,"192.168.2.7:30000",10);
 		this.station_controller.addStation(s);
 		
 		Station_Config_Binding cb = new Station_Config_Binding(s,c,0,1);
 		Station_Input_Binding b = new Station_Input_Binding(s,i,1);
 		
 	}*/
 	
 	/**
 	 * Constructor
 	 * @param shell The SWT Shell
 	 * @param display the SWT Display
 	 * @param configDir the os dependent config path
 	 */
 	private FWSMaster(Shell shell, Display display,String configDir) {
 		//Generate the Log File
 		try {
 			//max. 2 mb log file 
			FileHandler fh = new FileHandler("%t/fws_master%g.log", 2000000,3,true);
 			log.addHandler(fh);
 			log.setLevel(Level.INFO);
 			SimpleFormatter formatter = new SimpleFormatter();
 			fh.setFormatter(formatter);
 		} catch (Exception ex) {
			
 		}
 		
 		//Load the preferences 
 		this.configDir = configDir;
 		PersistencePreferences pref = new PersistencePreferences(configDir,"settings.xml");
 		this.parameter_controller = pref.loadParameters();
 		this.station_controller = pref.loadStations(this.parameter_controller);
 		MasterContentHandler config = pref.loadMasterConfig();
 		
 		this.outDir = config.getPath();
 		//if there is no outDir given create on in the configDir
 		if (outDir.equals("")) {
 			File outDirFile = new File(configDir,"output");
 			if (!outDirFile.isDirectory()) {
 				outDirFile.mkdir();
 			}
			this.outDir = configDir+File.pathSeparator+"output";
 		}
 		
 		log.config("Output Directory: "+this.outDir);
 		
 		this.generatorTime = config.getGeneratorTime();
 		this.collector = new MeasurementCollector(this.station_controller,generatorTime,outDir);
 		
 		//this.generateParameters();
 		view = new ViewMain(shell,display,this);
 		this.shell = shell;
 		this.display = display;
 		
 		this.collector.start();
 	}
 	
 	/**
 	 * Get the StationController
 	 * @return the controller
 	 */
 	public StationController getStationController() {
 		return this.station_controller;
 	}
 	
 	/**
 	 * Generates the configPath. Normally it's a folder .fwsmaster in the home directory.
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		Display.setAppName("FWS Master");
 		Display display = new Display();
 		
 		Shell shell = new Shell(display);		
 		
 		//Generate configPath
 		String os = System.getProperty("os.name");
 		String basePath = System.getProperty("user.home");
 		String configDirPath;
 		
 		if (os.equals("Mac OS X")) {
 			configDirPath = basePath+"/Library/Application Support/FWSMaster";
 		} else {
			configDirPath= basePath+File.pathSeparator+".fwsmaster";
 		}
 		
 		//Create configDir if not existent
 		File configDir = new File(configDirPath);
 		if(!configDir.isDirectory())
 			configDir.mkdir();
 		
 		//Create the Master
 		FWSMaster master = new FWSMaster(shell,display,configDirPath);
 		
 		shell.setSize(400,500);
 		shell.open();
 		shell.setText("FWS Master");
 		
 		
 		while (!shell.isDisposed ()) {
 			if (!display.readAndDispatch ()) display.sleep();
 		}
 		display.dispose();
 		master.shutdown();
 		System.exit(0);
 	}
 	
 	/**
 	 * Save the current settings in the xml config file
 	 */
 	private void shutdown() {
 		PersistencePreferences pref = new PersistencePreferences(configDir,"settings.xml");
 		pref.saveSettings(this.parameter_controller,this.station_controller,this.outDir,this.generatorTime);
 		
 	}
 
 	/**
 	 * Open the parameter window
 	 */
 	public void ParameterClicked() {
 		Shell param_shall = new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
 		Point pt = display.getCursorLocation();
 		param_shall.setLocation (pt.x, pt.y);
 		param_shall.setText ("Parameter Verwalten");
 		param_shall.setSize (600, 400);
 		
 		@SuppressWarnings("unused")
 		ViewParameters view_parameters = new ViewParameters(param_shall,this.parameter_controller);
 		param_shall.open();
 		
 		return;
 	}
 
 	/**
 	 * Open the station window
 	 */
 	public void StationClicked() {
 		Shell tmp_shell = new Shell(display, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
 		Point pt = display.getCursorLocation();
 		tmp_shell.setLocation (pt.x, pt.y);
 		tmp_shell.setText ("Stationen verwalten");
 		tmp_shell.setSize (600, 400);
 		
 		@SuppressWarnings("unused")
 		ViewStation view_stat = new ViewStation(tmp_shell,this.station_controller,this.parameter_controller);
 		tmp_shell.open();
 		
 		return;
 		
 	}
 	
 	/**
 	 * Open the outDir config window
 	 */
 	public void FolderClicked() {
 		DirectoryDialog dialog = new DirectoryDialog(shell);
 		String platform = SWT.getPlatform();
 		dialog.setFilterPath (platform.equals("win32") || platform.equals("wpf") ? "c:\\" : "/");
 		
 		String newOutDir = dialog.open();
 		if (newOutDir == null || newOutDir.equals(""))
 			return;
 		this.outDir = newOutDir;
 		this.collector.setOutDir(this.outDir);
 	}
 	
 	/**
 	 * Start or Stopp all stations
 	 * @param start if true start the stations, if false pause them
 	 */
 	public void StartClicked(boolean start) {
 		this.station_controller.startStations(start);
 	}
 
 }
