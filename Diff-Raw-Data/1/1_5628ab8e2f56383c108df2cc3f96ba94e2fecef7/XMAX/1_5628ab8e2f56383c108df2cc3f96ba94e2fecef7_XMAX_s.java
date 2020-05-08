 package com.isti.xmax;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.JOptionPane;
 //MTH:
 import javax.swing.SwingUtilities;
 import java.util.Date;
 import java.text.SimpleDateFormat;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 import org.apache.commons.cli.GnuParser;
 //import org.apache.commons.cli.DefaultParser;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PatternLayout;
 import org.apache.log4j.RollingFileAppender;
 import org.java.plugin.ObjectFactory;
 import org.java.plugin.PluginManager;
 import org.java.plugin.PluginManager.PluginLocation;
 import org.java.plugin.registry.Extension;
 import org.java.plugin.registry.ExtensionPoint;
 
 import com.isti.traceview.TraceView;
 import com.isti.traceview.common.TimeInterval;
 import com.isti.traceview.gui.ColorModeBySegment;
 import com.isti.traceview.processing.IFilter;
 import com.isti.traceview.processing.ITransformation;
 import com.isti.xmax.XMAXException;
 import com.isti.xmax.data.XMAXDataModule;
 import com.isti.xmax.gui.XMAXframe;
 
 /**
  * Main class for XMAX. Keeps command line parsing logic, handles with plugins, initialize data and
  * graphics.
  * 
  * @author Max Kokoulin
  */
 public class XMAX extends TraceView {
 	//private static final String version = "1.06";
 	//private static final String releaseDate = "Sept 14, 2011";
 	private static final String version = "1.08";
 	private static final String releaseDate = "Aug 29, 2013";
 
 	/**
 	 * Parsed command line
 	 */
 	private static CommandLine cmd;
 	private static Options options;
 	private static PluginManager pluginManager;
 	private static List<Extension> filters = new ArrayList<Extension>();
 	private static List<Extension> transformations = new ArrayList<Extension>();
 	
 	public XMAX() {
 		super();
 		setUndoAdapter(new XMAXUndoAdapter());
 		try {
 			boolean dump = false;
 			System.out.println("  XMAX ver." + getVersionMessage() );
             System.out.println("===============");
             if (cmd.getOptions().length == 0) {
                 System.out.println("[ Quick Examples ]");
                 System.out.println("* Read from BOTH -d 'data/path' AND existing serialized data in DATA_TEMP:");
                 System.out.println(" >java -Xms512M -Xmx512M -jar xmar.jar -t -d '/xs0/seed/IU_ANMO/2012/2012_1{59,60}_*/00_LHZ*seed'");
                 System.out.println("* Overwrite Serialized data in DATA_TEMP:");
                 System.out.println(" >java -Xms512M -Xmx512M -jar xmar.jar -T -d '/xs0/seed/IU_ANMO/2012/2012_1{59,60}_*/00_LHZ*seed'");
                 System.out.println("* Append to Serialized data in DATA_TEMP:");
                 System.out.println(" >java -Xms512M -Xmx512M -jar xmar.jar -T -t -d '/xs0/seed/IU_ANMO/2012/2012_1{59,60}_*/00_LHZ*seed'");
                System.exit(0);
             }
 			if (cmd.hasOption("h")) {
 				if (cmd.getOptions().length > 1) {
 					throw new XMAXException("It isn't allowed to use any other options with -h");
 				}
 				HelpFormatter formatter = new HelpFormatter();
 				formatter
 						.printHelp(
 								"xmax [-h | -v | -T] {-t -u<units> -o<order>} [-c<config file> -d<data mask> -s<station file> -k<earthquakes mask> -q<QC file> -b<begin time> -e<end time> -f<units count>]", 
 								options);
 			} else if (cmd.hasOption("v")) {
 				if (cmd.getOptions().length > 1) {
 					throw new XMAXException("It isn't allowed to use any other options with -v");
 				}
 				System.out.println("XMAX version " + getVersionMessage() + ". Instrumental Software Technologies, " + getReleaseDateMessage());
 			} else {
 				if (cmd.hasOption("g")) {
 					XMAXconfiguration.confFileName = cmd.getOptionValue("g").trim();
 				}
 				setConfiguration(XMAXconfiguration.getInstance());
 				if (cmd.hasOption("T")) {
 					dump = true;
 					getConfiguration().setDumpData(true);
 /** MTH: This has changed
 					if (cmd.hasOption("t")) {
 						throw new XMAXException("It isn't allowed to use -T and -t options together");
 					}
 **/
 				}
 				if (cmd.hasOption("t")) {
 					getConfiguration().setUseTempData(true);
 /**
 					if (cmd.hasOption("T")) {
 						throw new XMAXException("It isn't allowed to use -T and -t options together");
 					}
 **/
 				}
 				if (cmd.hasOption("d")) {
                     getConfiguration().setUseDataPath(true);
 					getConfiguration().setDataPath(dequote(cmd.getOptionValue("d")).trim());
 				}
 				if (cmd.hasOption("i")) {
 					getConfiguration().setStationInfoFileName(cmd.getOptionValue("i").trim());
 				}
 				if (cmd.hasOption("q")) {
 					getConfiguration().setQCdataFileName(cmd.getOptionValue("q").trim());
 				}
 				if (cmd.hasOption("p")) {
 					getConfiguration().setPickPath(dequote(cmd.getOptionValue("p")).trim());
 				}
 				if (cmd.hasOption("u")) {
 					getConfiguration().setPanelCountUnit(XMAXconfiguration.PanelCountUnit.values()[new Integer(cmd.getOptionValue("u").trim())]);
 				}
 				if (cmd.hasOption("o")) {
 					getConfiguration().setPanelOrder(XMAXconfiguration.ChannelSortType.values()[new Integer(cmd.getOptionValue("o").trim())]);
 				}
 				if (cmd.hasOption("f")) {
 					getConfiguration().setUnitsInFrame(new Integer(cmd.getOptionValue("f").trim()));
 				}
 				if (cmd.hasOption("F")) {
 					getConfiguration().setDefaultCompression(cmd.getOptionValue("F").trim());
 				}
 				if (cmd.hasOption("k")) {
 					getConfiguration().setEarthquakeFileMask(dequote(cmd.getOptionValue("k")));
 				}
 				if (cmd.hasOption("b")) {
 					getConfiguration().setStartTime(
 							TimeInterval.parseDate(cmd.getOptionValue("b").trim(), TimeInterval.DateFormatType.DATE_FORMAT_MIDDLE));
 				}
 				if (cmd.hasOption("e")) {
 					getConfiguration().setEndTime(
 							TimeInterval.parseDate(cmd.getOptionValue("e").trim(), TimeInterval.DateFormatType.DATE_FORMAT_MIDDLE));
 				}
 				if (cmd.hasOption("m")) {
 					getConfiguration().setMergeLocations(true);
 				}
 				if (cmd.hasOption("s")) {
 					getConfiguration().setFilterStation(cmd.getOptionValue("s").trim());
 				}
 				if (cmd.hasOption("n")) {
 					getConfiguration().setFilterNetwork(cmd.getOptionValue("n").trim());
 				}
 				if (cmd.hasOption("c")) {
 					getConfiguration().setFilterChannel(cmd.getOptionValue("c").trim());
 				}
 				if (cmd.hasOption("l")) {
 					getConfiguration().setFilterLocation(cmd.getOptionValue("l").trim());
 				}
 				if (cmd.hasOption("L")) {
 					getConfiguration().setDefaultBlockLength(new Integer(cmd.getOptionValue("L").trim()));
 				}
 
 				if (dump) {
 					// -T option in command line, make dump
 					setConfiguration(XMAXconfiguration.getInstance());
 					setDataModule(XMAXDataModule.getInstance());
 					getDataModule().dumpData(new ColorModeBySegment());
 				} else {
 					// Ordinary initialization
 					// switch off logging to suppress unneeded messages
 					Level level = Logger.getRootLogger().getLevel();
 					Logger.getRootLogger().setLevel(Level.OFF);
 					// Collecting plug-in locations.
 					PluginLocation[] pluginLocations = collectPluginLocations();
 					// Creating plug-in manager instance.
 					pluginManager = ObjectFactory.newInstance().createManager();
 					// Publishing discovered plug-ins.
 					pluginManager.publishPlugins(pluginLocations);
 
 					// Find our extension point.
 					ExtensionPoint filterExtPoint = pluginManager.getRegistry().getExtensionPoint("com.isti.xmax.core", "Filter");
 					ExtensionPoint transformExtPoint = pluginManager.getRegistry().getExtensionPoint("com.isti.xmax.core", "Transformation");
 					// Collect all extensions that was connected by JPF to our extension
 					// point. Iterate over extensions making text processing.
 					for (Extension ext: filterExtPoint.getConnectedExtensions()) {
 						filters.add(ext);
 					}
 					for (Extension ext: transformExtPoint.getConnectedExtensions()) {
 						transformations.add(ext);
 					}
 					//restoring logging level
 					Logger.getRootLogger().setLevel(level);
 					setDataModule(XMAXDataModule.getInstance());
                     
 					getDataModule().loadData();
 
 					if (getDataModule().getAllChannels().size() > 0) {
 						setFrame(XMAXframe.getInstance());
 						if (XMAXconfiguration.getInstance().getTimeInterval() != null) {
 							getFrame().setShouldManageTimeRange(false);
 							getFrame().setTimeRange(XMAXconfiguration.getInstance().getTimeInterval());
 						}
 						try {
 							// Wait while frame will be created to correct repaint
 							Thread.sleep(200);
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 						getFrame().setVisible(true);
 						getFrame().setShouldManageTimeRange(true);
 					} else {
 						JOptionPane.showMessageDialog(null, "No data found at path " + XMAXconfiguration.getInstance().getDataPath(), "Alert",
 								JOptionPane.WARNING_MESSAGE);
 					}
 				}
 			}
 		} catch (Exception e) {
 
 			e.printStackTrace();
 			System.exit(0);
 		}
 	}
 
 	/**
 	 * Getter for configuration.
 	 */
 	public static XMAXconfiguration getConfiguration() {
 		return (XMAXconfiguration) TraceView.getConfiguration();
 	}
 
 	/**
 	 * Getter for data module.
 	 */
 	public static XMAXDataModule getDataModule() {
 		return (XMAXDataModule) TraceView.getDataModule();
 	}
 
 	/**
 	 * Getter for main frame
 	 */
 	public static XMAXframe getFrame() {
 		return (XMAXframe) TraceView.getFrame();
 	}
 
 	/**
 	 * Get all plugins-filters
 	 */
 	public static List<Extension> getFilters() {
 		return filters;
 	}
 
 	/**
 	 * Get plugin-filter by id
 	 */
 	public static IFilter getFilter(String id) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
 		for (Extension ext: filters) {
 			if (ext.getId().equals(id)) {
 				// Get plug-in class loader.
 				ClassLoader classLoader = pluginManager.getPluginClassLoader(ext.getDeclaringPluginDescriptor());
 				// Load Routine class.
 				Class cls = classLoader.loadClass(ext.getParameter("class").valueAsString());
 				// Create Routine instance.
 				IFilter filter = (IFilter) cls.newInstance();
 				// Constructor cnst = cls.getConstructor(args);
 				// Object[] initargs = {id};
 				// IFilter filter = (IFilter) cnst.newInstance(initargs);
 				return filter;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Get all plugins-transformations
 	 */
 	public static List<Extension> getTransformations() {
 		return transformations;
 	}
 
 	/**
 	 * Get plugin-transformation by id
 	 */
 	public static ITransformation getTransformation(String id) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
 		for (Extension ext: transformations) {
 			if (ext.getId().equals(id)) {
 				// Get plug-in class loader.
 				ClassLoader classLoader = pluginManager.getPluginClassLoader(ext.getDeclaringPluginDescriptor());
 				// Load Routine class.
 				Class cls = classLoader.loadClass(ext.getParameter("class").valueAsString());
 				// Create Routine instance.
 				ITransformation transform = (ITransformation) cls.newInstance();
 				try {
 					int maxDataLength = ext.getParameter("max_data_length").valueAsNumber().intValue();
 					transform.setMaxDataLength(maxDataLength);
 				} catch (NullPointerException e) {
 					// do nothing
 				}
 				return transform;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @return filled and initialized CLI options object
 	 */
 	private static Options getOptions() {
 		Options opt = new Options();
 		opt.addOption(new Option("h", "help", false, "print this message"));
 		opt.addOption(new Option("v", "version", false, "print xmax version"));
 		opt.addOption(new Option("g", "config", true, "configuration file"));
 		opt.addOption(new Option("d", "data", true, "wildcarded mask of data files to load"));
 		opt.addOption(new Option("T", "make_dump", false, "dumps temporary file storage"));
 		opt.addOption(new Option("t", "use_dump", false, "adds temporary file storage content to data found by wildcarded mask (see -d)"));
 		opt.addOption(new Option("i", "stations", true, "stations description file"));
 		opt.addOption(new Option("k", "earthquakes", true, "wildcarded mask of earthquekes files"));
 		opt.addOption(new Option("q", "qcdata", true, "QC data file name"));
 		opt.addOption(new Option("b", "bdate", true, "begin date at yyyy,DDD,HH:mm:ss format"));
 		opt.addOption(new Option("e", "edate", true, "end date at yyyy,DDD,HH:mm:ss format"));
 		opt.addOption(new Option("u", "unit", true, "panel count unit: 0 - trace, 1 - station, 2 - channel, 3 - channel type, 4 - all"));
 		opt.addOption(new Option("o", "order", true,
 				"panel order: 0 - trace name, 1 - network/station/samplerate, 2 - channel, 3 - channel type,  4 - event"));
 		opt.addOption(new Option("f", "unitsframe", true, "units count (from -u option) in frame to display"));
 		opt.addOption(new Option("p", "picks", true, "picks database path"));
 		opt.addOption(new Option("m", "merge", false, "merge different locations of  channel into one graphical panel"));
 		opt
 				.addOption(new Option("F", "Format", true,
 						"default block compression format, possible values are SHORT, INT24, INT32, FLOAT, DOUBLE, STEIM1, STEIM2, CDSN, RSTN, DWW, SRO, ASRO, HGLP"));
 		opt.addOption(new Option("L", "Length", true, "default block length"));
 
 		opt.addOption(new Option("n", "flt_network", true, "semicolon-separated wildcarded filter by network"));
 		opt.addOption(new Option("s", "flt_station", true, "semicolon-separated wildcarded filter by station"));
 		opt.addOption(new Option("l", "flt_location", true, "semicolon-separated wildcarded filter by location"));
 		opt.addOption(new Option("c", "flt_channel", true, "semicolon-separated wildcarded filter by channel"));
 		return opt;
 	}
 
 	private PluginLocation[] collectPluginLocations() throws MalformedURLException, XMAXException {
 		String[] classpath = System.getProperty("java.class.path").split(";");
 		String pluginDirName = null;
 		for (String st: classpath) {
 			if (st.contains("xmax.jar")) {
 				pluginDirName = st.substring(0, st.indexOf("xmax.jar"));
 				if (pluginDirName.length() == 0)
 					pluginDirName = ".";
 				pluginDirName = pluginDirName + File.separator + "plugins";
 				break;
 			}
 		}
 		if (pluginDirName == null) {
 			pluginDirName = "./plugins";
 		}
 		File pluginDir = new File(pluginDirName);
 		if (pluginDir == null) {
 			throw new XMAXException("Can not find plugin directory");
 		}
 		File[] pluginFolders = pluginDir.listFiles(new FileFilter(){
 			public boolean accept(final File file) {
 				return file.isDirectory();
 			}
 		});
 		List<PluginLocation> result = new LinkedList<PluginLocation>();
 		for (int i = 0; i < pluginFolders.length; i++) {
 			PluginLocation pluginLocation = getPluginLocation(pluginFolders[i]);
 			if (pluginLocation != null) {
 				result.add(pluginLocation);
 			}
 		}
 		return (PluginLocation[]) result.toArray(new PluginLocation[result.size()]);
 	}
 
 	private PluginLocation getPluginLocation(File aFolder) throws MalformedURLException {
 		if (!aFolder.isDirectory()) {
 			return null;
 		}
 		File manifestFile = new File(aFolder, "plugin.xml");
 		if (!manifestFile.isFile()) {
 			manifestFile = new File(aFolder, "plugin-fragment.xml");
 			if (!manifestFile.isFile()) {
 				return null;
 			}
 		}
 		final URL folderUrl = org.java.plugin.util.IoUtil.file2url(aFolder);
 		final URL manifestUrl = org.java.plugin.util.IoUtil.file2url(manifestFile);
 		return new PluginLocation(){
 			public URL getManifestLocation() {
 				return manifestUrl;
 			}
 
 			public URL getContextLocation() {
 				return folderUrl;
 			}
 		};
 	}
 
 	/**
 	 * Dequote string, i.e remove wrapping ' and ".
 	 */
 	public static String dequote(String str) {
 		if ((str.charAt(0) == '"' && str.charAt(str.length() - 1) == '"') || (str.charAt(0) == '\'' && str.charAt(str.length() - 1) == '\'')) {
 			return str.substring(1, str.length() - 1);
 		} else {
 			return str;
 		}
 	}
 
 	/**
 	 * Get version message
 	 */
 	public static String getVersionMessage() {
 		return version;
 	}
 
 	/**
 	 * Get release date
 	 */
 	public static String getReleaseDateMessage() {
 		return releaseDate;
 	}
 
 	public static void main(String[] args) {
 		options = getOptions();
 		try {
 			CommandLineParser parser = new PosixParser();
 			cmd = parser.parse(options, args);
 			XMAX xyz = new XMAX();
 		} catch (ParseException e) {
 			System.err.println("Command line parsing failed.  Reason: " + e.getMessage());
 		}
 	}
 
 	/**
 	 * Set configuration
 	 */
 	public static void setConfiguration(XMAXconfiguration cn) {
 		RollingFileAppender apd = new RollingFileAppender();
 		apd.setName("FILELOG");
 		apd.setFile(cn.getLogFile());
 		apd.setMaxFileSize("1000KB");
 		apd.setMaxBackupIndex(10);
 		apd.setLayout(new PatternLayout("%d %5p %m%n"));
 		apd.activateOptions();
 		Logger.getRootLogger().addAppender(apd);
 		Runtime.getRuntime().addShutdownHook(new ClearLogShutDownHook());
 		TraceView.setConfiguration(cn);
 	}
 }
 
 /**
  * Clears logs after program shutdown.
  */
 class ClearLogShutDownHook extends Thread {
 	public void run() {
 		RollingFileAppender apd = (RollingFileAppender) (Logger.getRootLogger().getAppender("FILELOG"));
 		apd.close();
 		File f = new File(XMAXconfiguration.getInstance().getLogFile());
 		if (f.length() == 0) {
 			f.deleteOnExit();
 		}
 	}
 }
