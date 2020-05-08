 package org.bh.platform;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.lang.reflect.Field;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.ServiceLoader;
 import java.util.StringTokenizer;
 import java.util.Map.Entry;
 
 import javax.swing.ImageIcon;
 import javax.swing.JPanel;
 import javax.swing.UIManager;
 import javax.swing.UIManager.LookAndFeelInfo;
 import javax.swing.event.EventListenerList;
 
 import org.apache.log4j.Logger;
 import org.bh.calculation.IShareholderValueCalculator;
 import org.bh.calculation.IStochasticProcess;
 import org.bh.controller.IDataExchangeController;
 import org.bh.controller.IPeriodController;
 import org.bh.data.DTOKeyPair;
 import org.bh.data.DTOPeriod;
 import org.bh.data.DTO.Stochastic;
 import org.bh.gui.swing.BHMainFrame;
 import org.bh.gui.swing.BHStatusBar;
 import org.bh.platform.i18n.BHTranslator;
 import org.bh.platform.i18n.ITranslator;
 
 /**
  * This class offers static functions which can be used by other parts of the
  * software.
  * 
  * @author Marco Hammel
  * @author Robert Vollmer
  */
 public class Services {
 	private static final Logger log = Logger.getLogger(Services.class);
 	private static EventListenerList platformListeners = new EventListenerList();
 	private static HashMap<String, IShareholderValueCalculator> dcfMethods;
 	private static HashMap<String, IStochasticProcess> stochasticProcesses;
 	private static HashMap<String, IPeriodController> periodControllers;
 	private static HashMap<String, IDataExchangeController> dataExchangeController;
 	private static HashMap<String, IImportExport> importExport;
 
 	private static BHMainFrame bhmf = null;
 
 	public static void setBHMainFrame(BHMainFrame bhmf) {
 		Services.bhmf = bhmf;
 	}
 
 	/*
 	 * --------------------------------------- Platform Event Handling
 	 * ---------------------------------------
 	 */
 
 	public static ITranslator getTranslator() {
 		return BHTranslator.getInstance();
 	}
 
 	public static void addPlatformListener(IPlatformListener l) {
 		platformListeners.add(IPlatformListener.class, l);
 	}
 
 	public static void removePlatformListener(IPlatformListener l) {
 		platformListeners.remove(IPlatformListener.class, l);
 	}
 
 	public static void firePlatformEvent(PlatformEvent event) {
 		log.debug("Firing " + event);
 		for (IPlatformListener l : platformListeners
 				.getListeners(IPlatformListener.class))
 			l.platformEvent(event);
 	}
 
 	public static BHStatusBar getBHstatusBar() {
 		return BHStatusBar.getInstance();
 	}
 
 	/*
 	 * --------------------------------------- Service Loader / PlugIn
 	 * Management ---------------------------------------
 	 */
 
 	/**
 	 * Returns a reference to a DCF method with a specific id.
 	 * 
 	 * @param id
 	 *            The id of the DCF method.
 	 * @return The reference to the DCF method, or null if not found.
 	 */
 	public static IShareholderValueCalculator getDCFMethod(String id) {
 		return getDCFMethods().get(id);
 	}
 
 	/**
 	 * Returns the references to all loaded DCF methods.
 	 * 
 	 * @return References to all loaded DCF methods.
 	 */
 	public static Map<String, IShareholderValueCalculator> getDCFMethods() {
 		if (dcfMethods == null)
 			loadDCFMethods();
 		return dcfMethods;
 	}
 
 	private static void loadDCFMethods() {
 		// load all DCF methods and put them into the map
 		dcfMethods = new HashMap<String, IShareholderValueCalculator>();
 		ServiceLoader<IShareholderValueCalculator> calculators = PluginManager
 				.getInstance().getServices(IShareholderValueCalculator.class);
 		for (IShareholderValueCalculator calculator : calculators) {
 			dcfMethods.put(calculator.getUniqueId(), calculator);
 		}
 	}
 
 	/**
 	 * Returns a reference to a stochastic process with a specific id.
 	 * 
 	 * @param id
 	 *            The id of the stochastic process.
 	 * @return The reference to the stochastic process, or null if not found.
 	 */
 	public static IStochasticProcess getStochasticProcess(String id) {
 		return getStochasticProcesses().get(id);
 	}
 
 	/**
 	 * Returns the references to all loaded stochastic processes.
 	 * 
 	 * @return References to all loaded stochastic processes.
 	 */
 	public static Map<String, IStochasticProcess> getStochasticProcesses() {
 		if (stochasticProcesses == null)
 			loadStochasticProcesses();
 		return stochasticProcesses;
 	}
 
 	private static void loadStochasticProcesses() {
 		// load all stochastic processes and put them into the map
 		stochasticProcesses = new HashMap<String, IStochasticProcess>();
 		ServiceLoader<IStochasticProcess> processes = PluginManager
 				.getInstance().getServices(IStochasticProcess.class);
 		for (IStochasticProcess process : processes) {
 			stochasticProcesses.put(process.getUniqueId(), process);
 		}
 	}
 
 	// TODO Schmalzhaf.Alexander Testen!!!
 	public static IPeriodController getPeriodController(String id) {
 		return getPeriodControllers().get(id);
 	}
 
 	public static Map<String, IPeriodController> getPeriodControllers() {
 		if (periodControllers == null)
 			loadPeriodControllers();
 		return periodControllers;
 	}
 
 	private static void loadPeriodControllers() {
 		// load all PeriodGUIControllers and put them into the map
 		periodControllers = new HashMap<String, IPeriodController>();
 		ServiceLoader<IPeriodController> controllers = PluginManager
 				.getInstance().getServices(IPeriodController.class);
 		for (IPeriodController controller : controllers) {
 			periodControllers.put(controller.getGuiKey(), controller);
 		}
 
 	}
 	
 	
 	public static Map<String, IDataExchangeController> getDataExchangeController()
 	{
 		if (dataExchangeController == null)
 			loadDataExchangeController();
 		return dataExchangeController;
 	}
 	
 	public static IDataExchangeController getDataExchangeController(String dataFormat)
 	{
 		if (dataExchangeController == null)
 			loadDataExchangeController();
 		return dataExchangeController.get(dataFormat);
 	}
 	
 	
 	private static void loadDataExchangeController()
 	{
 		dataExchangeController = new HashMap<String, IDataExchangeController>();
 		ServiceLoader<IDataExchangeController> controller = PluginManager
 				.getInstance().getServices(IDataExchangeController.class);
 		for (IDataExchangeController contrl : controller)
 			dataExchangeController.put(contrl.getDataFormat(), contrl);
 	}
 	
 
 	@SuppressWarnings("unchecked")
 	public static List<DTOKeyPair> getStochasticKeysFromEnum(String dtoId,
 			Enum[] keyEnumeration) {
 		ArrayList<DTOKeyPair> keys = new ArrayList<DTOKeyPair>();
 		for (Enum element : keyEnumeration) {
 			try {
 				Field field = element.getClass().getDeclaredField(
 						element.name());
 				if (field.isAnnotationPresent(Stochastic.class))
 					keys.add(new DTOKeyPair(dtoId, element.toString()));
 			} catch (Throwable e) {
 				continue;
 			}
 		}
 		return keys;
 	}
 
 	private static void loadImportExportPlugins() {
 		// load all import export plug-ins and put them into the map
 		importExport = new HashMap<String, IImportExport>();
 		ServiceLoader<IImportExport> impExpPlugins = PluginManager
 				.getInstance().getServices(IImportExport.class);
 		for (IImportExport impExp : impExpPlugins) {
 			importExport.put(impExp.getUniqueId(), impExp);
 		}
 	}
 	
 	/**
 	 * Returns the references to all import export plug-ins.
 	 * 
 	 * @return References to all import export plug-ins matching the required methods.
 	 */
 	public static Map<String, IImportExport> getImportExportPlugins(int requiredMetods) {
 		int check;
 		Map<String, IImportExport> matchingImportExport;
 		
 		if (importExport == null) {
 			loadImportExportPlugins();
 		}
 		matchingImportExport = new HashMap<String, IImportExport>();
 		for(Entry<String, IImportExport> plugin : importExport.entrySet()) {
 			check = requiredMetods & plugin.getValue().getSupportedMethods();
 			if(requiredMetods == check) {
 				matchingImportExport.put(plugin.getKey(), plugin.getValue());
 			}
 		}
 		return matchingImportExport;
 	}
 	
 	
 	/*
 	 * --------------------------------------- GUI
 	 * ---------------------------------------
 	 */
 
 	/**
 	 * Sets Nimbus from Sun Inc. as default Look & Feel. Java 6 Update 10
 	 * required. Don't change complex looking implementation of invokation,
 	 * there are valid reasons for it.<br />
 	 * 
 	 * <b>Remark</b> <br />
 	 * For further information on Nimbus see <a href=
 	 * "http://developers.sun.com/learning/javaoneonline/2008/pdf/TS-6096.pdf"
 	 * >JavaOne Slides</a>
 	 */
 	public static void setNimbusLookAndFeel() {
 		// set Nimbus if available
 		try {
 			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
 				if ("Nimbus".equals(info.getName())) {
 					// Put specific look&feel attributes.
 					UIManager.put("nimbusBase", new Color(55, 125, 170));
 					UIManager.put("control", new Color(235, 240, 255));
 					UIManager.put("progressBar", new Color(255, 165, 0));
 					UIManager.put("nimbusOrange", UIManager.get("progressBar"));
 					UIManager.put("nimbusSelectionBackground", new Color(80,
 							160, 190));
 
 					// BH specific attributes.
 					UIManager.put("BHTree.nodeheight", 20);
 					UIManager.put("BHTree.minimumWidth", 150);
 
 					UIManager.setLookAndFeel(info.getClassName());
 					break;
 				}
 			}
 		} catch (Exception e) {
 			Logger.getLogger(Services.class).debug(
 					"Error while invoking Nimbus", e);
 		}
 	}
 
 	/**
 	 * Returns an ImageIcon, or null if the path was invalid.
 	 */
 	public static ImageIcon createImageIcon(String path, String description) {
 		URL imgURL = Services.class.getResource(path);
 		if (imgURL != null) {
 			return new ImageIcon(imgURL, description);
 		}
 
 		Logger.getLogger(Services.class).debug("Could not find icon " + path);
 		return null;
 
 	}
 
 	public static void startPeriodEditing(DTOPeriod period) {
 		IPeriodController periodController = Services
 				.getPeriodController(period.get(DTOPeriod.Key.CONTROLLER)
 						.toString());
 		Component viewComponent = periodController.editDTO((DTOPeriod) period);
 		if (viewComponent == null)
 			viewComponent = new JPanel();
 		bhmf.setContentForm(viewComponent);
 	}
 	
 	public static void setCharts(Component chart) {
 		bhmf.setCharts(chart);
 	}
 	
 	/**
 	 * Checks if JRE is fulfilling the requirements for Business Horizon.
 	 * 
 	 * Currently Business Horizon is requiring Java 6 Update 10. (1.6.0_10) 
 	 * @return <code>true</code> if JRE fulfills and <code>false</code> if it doesn't.
 	 */
 	public static boolean jreFulfillsRequirements() {
 		// Require Java 6 Update 10 or higher.
 		StringTokenizer javaVersion = new StringTokenizer(System.getProperty("java.version"), "._");
		
 		int root = Integer.parseInt(javaVersion.nextToken());
 		int major = Integer.parseInt(javaVersion.nextToken());
 		int minor = Integer.parseInt(javaVersion.nextToken());
 		int patchlevel = Integer.parseInt(javaVersion.nextToken());
 		
 		if (root < 1) {
			return false;
 		}
		if (root == 1 && major < 6) {
			return false;
 		}
		if (root == 1 && major == 6 && minor == 0 && patchlevel < 10) {
			return false;
 		}
		return true;
 	}
 }
