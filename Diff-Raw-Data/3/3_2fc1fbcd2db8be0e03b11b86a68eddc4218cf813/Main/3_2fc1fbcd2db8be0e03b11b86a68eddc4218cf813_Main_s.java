 package emcshop;
 
 import java.awt.Image;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.lang.Thread.UncaughtExceptionHandler;
 import java.sql.SQLException;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.swing.JOptionPane;
 import javax.swing.ToolTipManager;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 
 import emcshop.cli.CliController;
 import emcshop.cli.EmcShopArguments;
 import emcshop.db.DbDao;
 import emcshop.db.DbListener;
 import emcshop.db.DirbyEmbeddedDbDao;
 import emcshop.gui.AboutDialog;
 import emcshop.gui.ErrorDialog;
 import emcshop.gui.ItemSuggestField;
 import emcshop.gui.MainFrame;
 import emcshop.gui.ProfileDialog;
 import emcshop.gui.ProfileImageLoader;
 import emcshop.gui.SplashFrame;
 import emcshop.gui.images.ImageManager;
 import emcshop.gui.lib.JarSignersHardLinker;
 import emcshop.gui.lib.MacHandler;
 import emcshop.gui.lib.MacSupport;
 import emcshop.util.Settings;
 
 public class Main {
 	private static final Logger logger = Logger.getLogger(Main.class.getName());
 	private static final String NEWLINE = System.getProperty("line.separator");
 	private static final PrintStream out = System.out;
 
 	/**
 	 * The version of the app.
 	 */
 	public static final String VERSION;
 
 	/**
 	 * The project webpage.
 	 */
 	public static final String URL;
 
 	/**
 	 * The date the application was built.
 	 */
 	public static final Date BUILT;
 
 	/**
 	 * The version of the cache;
 	 */
 	public static final String CACHE_VERSION = "1";
 
 	static {
 		InputStream in = Main.class.getResourceAsStream("/info.properties");
 		Properties props = new Properties();
 		try {
 			props.load(in);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		} finally {
 			IOUtils.closeQuietly(in);
 		}
 
 		VERSION = props.getProperty("version");
 		URL = props.getProperty("url");
 
 		Date built;
 		try {
 			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
 			built = df.parse(props.getProperty("built"));
 		} catch (ParseException e) {
 			//this could happen during development if the properties file is not filtered by Maven
 			logger.log(Level.SEVERE, "Could not parse built date.", e);
 			built = new Date();
 		}
 		BUILT = built;
 	}
 
 	private static final File defaultProfileRootDir = new File(FileUtils.getUserDirectory(), ".emc-shopkeeper");
 	public static final String defaultProfileName = "default";
 	private static final Integer defaultStartPage = 1;
 	private static final String defaultFormat = "TABLE";
 
 	private static File profileRootDir, profileDir, dbDir;
 	private static String profile, query, format;
 	private static Integer startAtPage, stopAtPage;
 	private static boolean update;
 	private static Level logLevel;
 
 	private static Settings settings;
 	private static LogManager logManager;
 
 	private static MainFrame mainFrame;
 
 	public static void main(String[] args) throws Throwable {
 		//stop the random SecurityExceptions from being thrown
 		JarSignersHardLinker.go();
 
 		EmcShopArguments arguments = null;
 		try {
 			arguments = new EmcShopArguments(args);
 		} catch (IllegalArgumentException e) {
 			printHelp();
 			out.println();
 			out.println("Error: The following arguments are invalid: " + e.getMessage());
 			System.exit(1);
 		}
 
 		//print help
 		if (arguments.help()) {
 			printHelp();
 			return;
 		}
 
 		//print version
 		if (arguments.version()) {
 			out.println(VERSION);
 			return;
 		}
 
 		//get profile root dir
 		String profileRootDirStr = arguments.profileDir();
 		profileRootDir = (profileRootDirStr == null) ? defaultProfileRootDir : new File(profileRootDirStr);
 
 		//get profile
 		boolean profileSpecified = true;
 		profile = arguments.profile();
 		if (profile == null) {
 			profileSpecified = false;
 			profile = defaultProfileName;
 		}
 
 		//create profile dir
 		profileDir = new File(profileRootDir, profile);
 		if (profileDir.exists()) {
 			if (!profileDir.isDirectory()) {
 				out.println("Error: Profile directory is not a directory!  Path: " + profileDir.getAbsolutePath());
 				System.exit(1);
 			}
 		} else {
 			logger.info("Creating new profile: " + profileDir.getAbsolutePath());
 			if (!profileDir.mkdirs()) {
 				out.println("Error: Could not create profile folder!  Path: " + profileDir.getAbsolutePath());
 				System.exit(1);
 			}
 		}
 
 		//load settings
 		String settingsFileStr = arguments.settings();
 		File settingsFile = (settingsFileStr == null) ? new File(profileDir, "settings.properties") : new File(settingsFileStr);
 		settings = new Settings(settingsFile);
 
 		//show the "choose profile" dialog
 		boolean cliMode = arguments.query() != null || arguments.update();
 		if (!cliMode && !profileSpecified && settings.isShowProfilesOnStartup()) {
 			File selectedProfileDir = ProfileDialog.show(null, profileRootDir);
 			if (selectedProfileDir == null) {
 				//user canceled the dialog, so quit the application
 				return;
 			}
 
 			//reset the profile dir
 			profileDir = selectedProfileDir;
 			profile = profileDir.getName();
 			settings = new Settings(new File(profileDir, "settings.properties"));
 		}
 
 		//get database dir
 		String dbDirStr = arguments.db();
 		dbDir = (dbDirStr == null) ? new File(profileDir, "db") : new File(dbDirStr);
 
 		//get update flag
 		update = arguments.update();
 		if (update) {
 			//get stop at page
 			stopAtPage = arguments.stopPage();
 			if (stopAtPage != null && stopAtPage < 1) {
 				out.println("Error: \"stop-page\" must be greater than 0.");
 				System.exit(1);
 			}
 
 			//get start at page
 			startAtPage = arguments.startPage();
 			if (startAtPage == null) {
 				startAtPage = defaultStartPage;
 			} else if (startAtPage < 1) {
 				out.println("Error: \"start-page\" must be greater than 0.");
 				System.exit(1);
 			}
 		}
 
 		//get query
 		query = arguments.query();
 		if (query != null) {
 			String format = arguments.format();
 			if (format == null) {
 				format = defaultFormat;
 			}
 		}
 
 		//get log level
 		try {
 			logLevel = arguments.logLevel();
 			if (logLevel == null) {
 				logLevel = settings.getLogLevel();
 			}
 		} catch (IllegalArgumentException e) {
 			out.println("Error: Invalid log level.");
 			System.exit(1);
 		}
 
 		logManager = new LogManager(logLevel, new File(profileDir, "app.log"));
 
 		if (!update && query == null) {
 			launchGui();
 		} else {
 			launchCli();
 		}
 	}
 
 	private static void printHelp() {
 		//@formatter:off
 		out.println(
 		"EMC Shopkeeper v" + VERSION + NEWLINE +
 		"by Michael Angstadt (shavingfoam)" + NEWLINE +
 		URL + NEWLINE +
 		NEWLINE +
 		"General arguments" + NEWLINE +
 		"These arguments can be used for the GUI and CLI." + NEWLINE +
 		"================================================" + NEWLINE +
 		"--profile=PROFILE" + NEWLINE +
 		"  The profile to use (defaults to \"" + defaultProfileName + "\")." + NEWLINE +
 		NEWLINE +
 		"--profile-dir=DIR" + NEWLINE +
 		"  The path to the directory that contains all the profiles" + NEWLINE +
 		"  (defaults to \"" + defaultProfileRootDir.getAbsolutePath() + "\")." + NEWLINE +
 		NEWLINE +
 		"--db=PATH" + NEWLINE +
 		"  Overrides the database location (stored in the profile by default)." + NEWLINE +
 		NEWLINE +
 		"--settings=PATH" + NEWLINE +
 		"  Overrides the settings file location (stored in the profile by default)." + NEWLINE +
 		NEWLINE +
 		"--log-level=FINEST|FINER|FINE|CONFIG|INFO|WARNING|SEVERE" + NEWLINE +
 		"  The log level to use (defaults to INFO)." + NEWLINE +
 		NEWLINE +
 		"CLI arguments" + NEWLINE +
 		"Using one of these arguments will launch EMC Shopkeeper in CLI mode." + NEWLINE +
 		"================================================" + NEWLINE +
 		"--update" + NEWLINE +
 		"  Updates the database with the latest transactions." + NEWLINE +
 		"--start-page=PAGE" + NEWLINE +
 		"  Specifies the transaction history page number to start at during" + NEWLINE +
 		"  the first update (defaults to " + defaultStartPage + ")." + NEWLINE +
 		"--stop-page=PAGE" + NEWLINE +
 		"  Specifies the transaction history page number to stop at during" + NEWLINE +
 		"  the first update (defaults to the last page)." + NEWLINE +
 		NEWLINE +
 		"--query=QUERY" + NEWLINE +
 		"  Shows the net gains/losses of each item.  Examples:" + NEWLINE +
 		"  All data:           --query" + NEWLINE +
 		"  Today's data:       --query=\"today\"" + NEWLINE +
 		"  Three days of data: --query=\"2013-03-07 to 2013-03-09\"" + NEWLINE +
 		"  Data up to today:   --query=\"2013-03-07 to today\"" + NEWLINE +
 		"--format=TABLE|CSV|BBCODE" + NEWLINE +
 		"  Specifies how to render the queried transaction data (defaults to " + defaultFormat + ")." + NEWLINE +
 		NEWLINE +
 		"--version" + NEWLINE +
 		"  Prints the version of this program." + NEWLINE +
 		NEWLINE +
 		"--help" + NEWLINE +
 		"  Prints this help message."
 		);
 		//@formatter:on
 	}
 
 	private static void launchCli() throws Throwable {
 		final DbDao dao = new DirbyEmbeddedDbDao(dbDir);
 		ReportSender reportSender = ReportSender.instance();
 		reportSender.setDatabaseVersion(dao.selectDbVersion());
 		dao.updateToLatestVersion(null);
 		reportSender.setDatabaseVersion(dao.selectDbVersion());
 
 		CliController cli = new CliController(dao, settings);
 
 		if (update) {
 			cli.update(startAtPage, stopAtPage);
 		}
 
 		if (query != null) {
 			cli.query(query, format);
 		}
 	}
 
 	private static void launchGui() throws SQLException, IOException {
 		//run Mac OS X customizations if user is on a Mac
 		//this code must run before *anything* else graphics-related
 		Image appIcon = ImageManager.getAppIcon().getImage();
 		MacSupport.initIfMac("EMC Shopkeeper", false, appIcon, new MacHandler() {
 			@Override
 			public void handleQuit(Object applicationEvent) {
 				mainFrame.exit();
 			}
 
 			@Override
 			public void handleAbout(Object applicationEvent) {
 				AboutDialog.show(mainFrame);
 			}
 		});
 
 		//show splash screen
 		final SplashFrame splash = new SplashFrame();
 		splash.setVisible(true);
 
 		//set uncaught exception handler
 		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
 			@Override
 			public void uncaughtException(Thread thread, Throwable thrown) {
 				ErrorDialog.show(null, "An error occurred.", thrown);
 			}
 		});
 
 		//init the cache
 		File cacheDir = new File(profileDir, "cache");
 		initCacheDir(cacheDir);
 
 		//start the profile image loader
 		ProfileImageLoader profileImageLoader = new ProfileImageLoader(cacheDir, settings);
 
 		DbListener listener = new DbListener() {
 			@Override
 			public void onCreate() {
 				splash.setMessage("Creating database...");
 			}
 
 			@Override
 			public void onBackup(int oldVersion, int newVersion) {
 				splash.setMessage("Preparing for database update...");
 			}
 
 			@Override
 			public void onMigrate(int oldVersion, int newVersion) {
 				splash.setMessage("Updating database...");
 			}
 		};
 
 		//connect to database
 		splash.setMessage("Starting database...");
 		DbDao dao;
 		try {
 			dao = new DirbyEmbeddedDbDao(dbDir, listener);
 		} catch (SQLException e) {
 			if ("XJ040".equals(e.getSQLState())) {
				JOptionPane.showMessageDialog(null, "EMC Shopkeeper is already running.", "Already running", JOptionPane.ERROR_MESSAGE);
 				splash.dispose();
 				return;
 			}
 			throw e;
 		}
 
 		//update database schema
 		ReportSender reportSender = ReportSender.instance();
 		reportSender.setDatabaseVersion(dao.selectDbVersion());
 		dao.updateToLatestVersion(listener);
 		reportSender.setDatabaseVersion(dao.selectDbVersion());
 
 		//tweak tooltip settings
 		ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
 		toolTipManager.setInitialDelay(0);
 		toolTipManager.setDismissDelay(30000);
 
 		//pre-load the labels for the item suggest fields
 		splash.setMessage("Loading item icons...");
 		ItemSuggestField.init(dao);
 
 		mainFrame = new MainFrame(settings, dao, logManager, profileImageLoader, profileDir.getName());
 		mainFrame.setVisible(true);
 		splash.dispose();
 	}
 
 	/**
 	 * Estimates the time it will take to process all the transactions.
 	 * @param stopPage the page to stop at
 	 * @return the estimated processing time (in milliseconds)
 	 */
 	public static long estimateUpdateTime(Integer stopPage) {
 		int totalMs = 10000;
 		int last = 10000;
 		for (int i = 100; i < stopPage; i += 100) {
 			int cur = last + 1550;
 			totalMs += cur;
 			last = cur;
 		}
 		return totalMs;
 	}
 
 	/**
 	 * Initializes the cache directory.
 	 * @param cacheDir the path to the cache directory
 	 * @throws IOException
 	 */
 	private static void initCacheDir(File cacheDir) throws IOException {
 		File versionFile = new File(cacheDir, "_cache-version");
 		boolean clearCache = false;
 		if (!cacheDir.isDirectory()) {
 			//create the cache dir if it doesn't exist
 			clearCache = true;
 		} else {
 			//clear the cache dir on update
 			String version = versionFile.exists() ? FileUtils.readFileToString(versionFile) : null;
 			clearCache = (version == null || !version.equals(CACHE_VERSION));
 		}
 
 		//clear the cache if it's out of date
 		if (clearCache) {
 			logger.info("Clearing the cache.");
 			if (!cacheDir.isDirectory() || FileUtils.deleteQuietly(cacheDir)) {
 				if (!cacheDir.mkdir()) {
 					throw new IOException("Could not create cache directory: " + cacheDir.getAbsolutePath());
 				}
 			}
 			FileUtils.writeStringToFile(versionFile, CACHE_VERSION);
 		}
 	}
 }
