 
 package org.paxle.desktop.impl;
 
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.util.Dictionary;
 import java.util.Hashtable;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.osgi.framework.BundleException;
 import org.osgi.framework.Constants;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.service.cm.Configuration;
 import org.osgi.service.cm.ConfigurationAdmin;
 import org.osgi.service.cm.ConfigurationException;
 import org.osgi.service.cm.ConfigurationListener;
 import org.osgi.service.cm.ManagedService;
 import org.osgi.service.metatype.MetaTypeService;
 
 import org.paxle.core.IMWComponent;
 import org.paxle.core.prefs.Properties;
 import org.paxle.core.queue.CommandProfile;
 import org.paxle.core.queue.ICommandProfile;
 import org.paxle.desktop.IDesktopServices;
 import org.paxle.desktop.backend.IDIBackend;
 import org.paxle.desktop.impl.dialogues.SettingsFrame;
 
 public class DesktopServices implements IDesktopServices, ManagedService {
 	
 	private static final String TRAY_ICON_LOCATION = "/resources/trayIcon.png";
 	
 	private static final Class<IMWComponent> MWCOMP_CLASS = IMWComponent.class;
 	private static final String CRAWLER_QUERY = String.format("(%s=org.paxle.crawler)", IMWComponent.COMPONENT_ID);
 	
 	private static final String ICOMMANDDB = "org.paxle.data.db.ICommandDB";
	private static final String IPROFILEM = "org.paxle.core.queue.IComandProfileManager";
 	private static final String IROBOTSM = "org.paxle.filter.robots.IRobotsTxtManager";
 	
 	private static final String PREF_PID = IDesktopServices.class.getName();
 	private static final String PREF_OPEN_BROWSER_STARTUP = "openBrowser";
 	private static final String PREF_SHOW_SYSTRAY = "showTrayMenu";
 	
 	private static final int PROP_BROWSER_OPENABLE = 1;
 	
 	private static final String PROP_PROFILE_ID = "profileId";
 	
 	private final Log logger = LogFactory.getLog(DesktopServices.class);
 	private final ServiceManager manager;
 	private final IDIBackend backend;
 	
 	private ICommandProfile profile = null;
 	private SettingsFrame settings = null;
 	private ServiceRegistration settingsRef = null;
 	private SystrayMenu trayMenu = null;
 	
 	private boolean browserOpenable = true;
 	
 	public DesktopServices(final ServiceManager manager, final IDIBackend backend) {
 		this.manager = manager;
 		this.backend = backend;
 		initDS();
 	}
 	
 	private void initDS() {
 		final Hashtable<String,Object> regProps = new Hashtable<String,Object>();
 		regProps.put(Constants.SERVICE_PID, IDesktopServices.class.getName());
 		manager.registerService(this, regProps, ManagedService.class);
 		
 		final Properties properties = manager.getServiceProperties();
 		if (properties != null) {
 			// get the backend properties, i.e. whether the browser can be opened by the backend
 			final Object backendProps = properties.get(backend.getClass().getName());
 			if (backendProps != null) {
 				final int bp = Integer.parseInt((String)backendProps);
 				browserOpenable = (bp & PROP_BROWSER_OPENABLE) != 0;
 			}
 			
 			// get the crawl-profile saved for the DI-bundle
 			final Object profileIdProp = properties.get(PROP_PROFILE_ID);
 			if (profileIdProp != null) {
 				final Integer profileId = Integer.valueOf((String)profileIdProp);
 				final Object profileManager = manager.getService(IPROFILEM);
 				if (profileManager != null) try {
 					final Method getById = profileManager.getClass().getMethod("getProfileByID", int.class);
 					profile = (ICommandProfile)getById.invoke(profileManager, profileId);
 				} catch (Exception e) {
 					logger.warn(String.format("Unable to retrieve current desktop-crawl profile (#%d) from profile manager: [%s]: %s",
 							profileId, e.getClass().getName(), e.getMessage()));
 				}
 			}
 		}
 		
 		// check whether starting the browser on startup is set in the config and open it if necessary
 		final ConfigurationAdmin cadmin = manager.getService(ConfigurationAdmin.class);
 		if (cadmin != null) try {
 			final Configuration conf = cadmin.getConfiguration(PREF_PID, manager.getBundle().getLocation());
 			Dictionary<?,?> props = null;
 			if (conf != null)
 				props = conf.getProperties();
 			if (props == null)
 				props = getDefaults();
 			
 			if (browserOpenable) {
 				final Object openBrowserStartup = props.get(PREF_OPEN_BROWSER_STARTUP);
 				if (openBrowserStartup != null && ((Boolean)openBrowserStartup).booleanValue())
 					browseUrl(getPaxleUrl("/search"));
 			}
 		} catch (IOException e) { e.printStackTrace(); }
 	}
 	
 	public void shutdown() {
 		final Properties props = manager.getServiceProperties();
 		if (props == null)
 			return;
 		
 		int bp = 0;
 		bp |= (browserOpenable) ? PROP_BROWSER_OPENABLE : 0;
 		props.put(backend.getClass().getName(), Integer.toString(bp));
 		
 		if (profile == null) {
 			props.remove(PROP_PROFILE_ID);
 		} else {
 			props.put(PROP_PROFILE_ID, Integer.toString(profile.getOID()));
 		}
 		
 		closeSettingsDialog();
 		setTrayMenuVisible(false);
 	}
 	
 	public void shutdownFramework() throws BundleException {
 		manager.shutdownFramework();
 	}
 	
 	public void restartFramework() throws BundleException {
 		manager.restartFramework();
 	}
 	
 	public void setTrayMenuVisible(final boolean yes) {
 		if (yes && !isTrayMenuVisible()) {
 			trayMenu = new SystrayMenu(this, manager.getBundle().getResource(TRAY_ICON_LOCATION));
 		} else if (!yes && isTrayMenuVisible()) {
 			trayMenu.shutdown();
 			trayMenu = null;
 		}
 	}
 	
 	public boolean isTrayMenuVisible() {
 		return trayMenu != null;
 	}
 	
 	private static Hashtable<String,Object> getDefaults() {
 		final Hashtable<String,Object> defaults = new Hashtable<String,Object>();
 		defaults.put(PREF_OPEN_BROWSER_STARTUP, Boolean.TRUE);
 		defaults.put(PREF_SHOW_SYSTRAY, Boolean.TRUE);
 		return defaults;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public synchronized void updated(Dictionary properties) throws ConfigurationException {
 		try {
 			if (properties == null) {
 				properties = getDefaults();
 			}
 			
 			final Object showTrayMenu = properties.get(PREF_SHOW_SYSTRAY);
 			if (showTrayMenu != null) {
 				final boolean showTM = ((Boolean)showTrayMenu).booleanValue();
 				setTrayMenuVisible(showTM);
 			}
 		} catch (Throwable e) { e.printStackTrace(); }
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void openSettingsDialog() {
 		if (settings == null) {
 			settings = new SettingsFrame(manager.getBundles(),
 					manager.getService(ConfigurationAdmin.class),
 					manager.getService(MetaTypeService.class));
 			settings.addWindowListener(new WindowAdapter() {
 				@Override
 				public void windowClosed(WindowEvent e) {
 					if (settingsRef != null)
 						settingsRef.unregister();
 					settingsRef = null;
 				}
 			});
 			settingsRef = manager.registerService(settings, new Hashtable<String,Object>(), ConfigurationListener.class);
 		}
 		final int extstate = settings.getExtendedState();
 		if ((extstate & JFrame.ICONIFIED) == JFrame.ICONIFIED)
 			settings.setExtendedState(extstate ^ JFrame.ICONIFIED);
 		if (!settings.isVisible())
 			settings.setVisible(true);
 		settings.toFront();
 	}
 	
 	public void closeSettingsDialog() {
 		if (settings != null)
 			settings.dispose();
 		settings = null;
 	}
 	
 	public boolean isCrawlerServiceAvailable() {
 		try {
 			return manager.hasService(MWCOMP_CLASS, CRAWLER_QUERY);
 		} catch (InvalidSyntaxException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	public boolean isBrowserOpenable() {
 		return browserOpenable;
 	}
 	
 	public ServiceManager getServiceManager() {
 		return manager;
 	}
 	
 	public IDIBackend getBackend() {
 		return backend;
 	}
 	
 	public boolean isCrawlerCorePaused() {
 		try {
 			final IMWComponent<?>[] crawlers = manager.getServices(MWCOMP_CLASS, CRAWLER_QUERY);
 			if (crawlers == null || crawlers.length == 0)
 				return false;
 			return crawlers[0].isPaused();
 		} catch (InvalidSyntaxException e) {
 			Utilities.showExceptionBox(e);
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	public void toggleCrawlersPR() {
 		try {
 			final IMWComponent<?>[] crawlers = manager.getServices(MWCOMP_CLASS, CRAWLER_QUERY);
 			if (crawlers == null || crawlers.length == 0)
 				return;
 			final IMWComponent<?> crawler = crawlers[0];
 			if (crawler.isPaused()) {
 				crawler.resume();
 			} else {
 				crawler.pause();
 			}
 		} catch (InvalidSyntaxException e) {
 			Utilities.showExceptionBox(e);
 			e.printStackTrace();
 		}
 	}
 	
 	private static ICommandProfile createDefaultProfile() {
 		final ICommandProfile profile = new CommandProfile();
 		profile.setMaxDepth(3);
 		profile.setName("desktop-crawl");
 		return profile;
 	}
 	
 	public void startDefaultCrawl(final URI uri) throws ServiceException {
 		final Object commandDB;
 		final Method enqueueCommand;
 		try {
 			commandDB = manager.getService(ICOMMANDDB);
 			if (commandDB == null)
 				throw new ServiceException("Command-DB", ICOMMANDDB);
 			enqueueCommand = commandDB.getClass().getMethod("enqueue", URI.class, int.class, int.class);
 		} catch (NoSuchMethodException e) {
 			throw new ServiceException("Command-DB", "enqueue(URI, int, int)");
 		}
 		
 		final Object robotsManager = manager.getService(IROBOTSM);
 		if (robotsManager != null) try {
 			final Method isDisallowed = robotsManager.getClass().getMethod("isDisallowed", URI.class);
 			final Object result = isDisallowed.invoke(robotsManager, uri);
 			if (((Boolean)result).booleanValue()) {
 				logger.info("Domain does not allow crawling of '" + uri + "' due to robots.txt blockage");
 				Utilities.showURLErrorMessage(
 						"This URI is blocked by the domain's robots.txt, see",
 						uri.resolve(URI.create("/robots.txt")).toString());
 				return;
 			}
 		} catch (Exception e) {
 			logger.warn(String.format("Error retrieving robots.txt from host '%s': [%s] %s - continuing crawl",
 					uri.getHost(), e.getClass().getName(), e.getMessage()));
 		}
 		
 		try {
 			int depth = 0;
 			if (profile == null) try {
 				final Object profileDB;
 				final Method storeProfile;
 				
 				profileDB = manager.getService(IPROFILEM);
 				if (profileDB == null)
 					throw new Exception("Profile manager not registered");
 				storeProfile = profileDB.getClass().getMethod("storeProfile", ICommandProfile.class);
 				
 				// create a new profile
 				profile = createDefaultProfile();
 				
 				// store it into the profile-db
 				storeProfile.invoke(profileDB, profile);
 				depth = profile.getMaxDepth();
 			} catch (Exception e) {
 				logger.warn(String.format("Unable to access profile manager: [%s] %s - starting crawl of URL '%s' with depth 0",
 						e.getClass().getName(), e.getMessage(), uri));
 				depth = 0;
 			}
 			
 			final Object result = enqueueCommand.invoke(commandDB, uri, Integer.valueOf(profile.getOID()), Integer.valueOf(depth));
 			if (((Boolean)result).booleanValue()) {
 				logger.info("Initiated crawl of URL '" + uri + "'");
 			} else {
 				logger.info("Initiating crawl of URL '" + uri + "' failed, URL is already known");
 			}
 		} catch (Exception e) {
 			throw new ServiceException("Crawl start", e.getMessage(), e);
 		}
 	}
 	
 	public String getPaxleUrl(String... path) {
 		final String port = manager.getProperty("org.osgi.service.http.port");
 		if (port == null)
 			return null;
 		final StringBuffer sb = new StringBuffer("http://localhost:").append(port);
 		if (path.length == 0 || path[0].charAt(0) != '/')
 			sb.append('/');
 		for (final String s : path)
 			sb.append(s);
 		return sb.toString();
 	}
 	
 	public boolean browseUrl(final String url) {
 		if (url == null) {
 			JOptionPane.showMessageDialog(null, "HTTP service not accessible", "Error", JOptionPane.ERROR_MESSAGE);
 		} else if (browserOpenable) try {
 			if ((browserOpenable = backend.getDesktop().browse(url))) {
 				return true;
 			} else {
 				Utilities.showURLErrorMessage(
 						"Couldn't launch system browser due to an error in Paxle's system integration\n" +
 						"bundle. Please review the log for details. The requested URL was:", url);
 			}
 		} catch (MalformedURLException e) {
 			Utilities.showExceptionBox("Generated mal-formed URL", e);
 			logger.error("Generated mal-formed URL '" + url + "': " + e.getMessage(), e);
 		}
 		return false;
 	}
 }
