 /**
  * This file is part of the Paxle project.
  * Visit http://www.paxle.net for more information.
  * Copyright 2007-2009 the original author or authors.
  *
  * Licensed under the terms of the Common Public License 1.0 ("CPL 1.0").
  * Any use, reproduction or distribution of this program constitutes the recipient's acceptance of this agreement.
  * The full license text is available under http://www.opensource.org/licenses/cpl1.0.txt
  * or in the file LICENSE.txt in the root directory of the Paxle distribution.
  *
  * Unless required by applicable law or agreed to in writing, this software is distributed
  * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  */
 package org.paxle.desktop.impl;
 
 import java.awt.Frame;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Method;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Dictionary;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.Set;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.osgi.framework.BundleException;
 import org.osgi.framework.Constants;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceEvent;
 import org.osgi.framework.ServiceListener;
 import org.osgi.framework.ServiceReference;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.service.cm.Configuration;
 import org.osgi.service.cm.ConfigurationAdmin;
 import org.osgi.service.cm.ConfigurationException;
 import org.osgi.service.cm.ManagedService;
 import org.osgi.service.metatype.AttributeDefinition;
 import org.osgi.service.metatype.MetaTypeProvider;
 import org.osgi.service.metatype.ObjectClassDefinition;
 
 import org.paxle.core.IMWComponent;
 import org.paxle.core.io.IResourceBundleTool;
 import org.paxle.core.norm.IReferenceNormalizer;
 import org.paxle.core.queue.CommandProfile;
 import org.paxle.core.queue.ICommand;
 import org.paxle.core.queue.ICommandProfile;
 import org.paxle.core.queue.ICommandProfileManager;
 import org.paxle.desktop.DIComponent;
 import org.paxle.desktop.IDIEventListener;
 import org.paxle.desktop.IDesktopServices;
 import org.paxle.desktop.Utilities;
 import org.paxle.desktop.backend.IDIBackend;
 import org.paxle.desktop.impl.dialogues.CrawlingConsole;
 import org.paxle.desktop.impl.dialogues.bundles.BundlePanel;
 import org.paxle.desktop.impl.dialogues.settings.SettingsPanel;
 import org.paxle.desktop.impl.dialogues.stats.StatisticsPanel;
 
 public class DesktopServices implements IDesktopServices, ManagedService, MetaTypeProvider, ServiceListener {
 	
 	/**
 	 * Denotes a relative path the the Paxle-icon used for the tray.
 	 * @see #setTrayMenuVisible(boolean)
 	 */
 	private static final String TRAY_ICON_LOCATION = "/resources/trayIcon.png";
 	
 	/**
 	 * Contains conveninience-constants for locating the {@link IMWComponent}s of the bundles
 	 * CrawlerCore, ParserCore and Indexer.
 	 */
 	public static enum MWComponents {
 		/** Refers to the {@link IMWComponent} provided by the bundle "CrawlerCore" */
 		CRAWLER,
 		/** Refers to the {@link IMWComponent} provided by the bundle "ParserCore" */
 		PARSER,
 		/** Refers to the {@link IMWComponent} provided by the bundle "Indexer" */
 		INDEXER
 		
 		;
 		
 		/**
 		 * @return the {@link org.osgi.framework.Constants#BUNDLE_SYMBOLICNAME symbolic-name}
 		 *         of this {@link IMWComponent}, which also denotes the value for
 		 *         {@link IMWComponent#COMPONENT_ID}
 		 */
 		public String getID() {
 			return String.format("org.paxle.%s", name().toLowerCase());
 		}
 		
 		/**
 		 * @return a LDAP-style expression which matches this components's
 		 *         {@link IMWComponent#COMPONENT_ID}
 		 */
 		public String toQuery() {
 			return toQuery(IMWComponent.COMPONENT_ID);
 		}
 		
 		/**
 		 * @param key the key of the resulting expression
 		 * @return a LDAP-style expression which matches the given <code>key</code> to this
 		 *         component's {@link IMWComponent#COMPONENT_ID}
 		 * @see #getID()
 		 */
 		public String toQuery(final String key) {
 			return String.format("(%s=%s)", key, getID());
 		}
 		
 		/**
 		 * @return the human-readable name of this component, such as "Crawler", "Parser" or
 		 *         "Indexer" 
 		 */
 		@Override
 		public String toString() {
 			return Character.toUpperCase(name().charAt(0)) + name().substring(1).toLowerCase();
 		}
 		
 		/**
 		 * @param id the {@link #getID() ID} of the component's representative constant to return
 		 * @return the {@link MWComponents}-constant whose ID is given by <code>id</code> or <code>null</code>
 		 *         if no such component is known 
 		 * @see #getID()
 		 */
 		public static MWComponents valueOfID(final String id) {
 			return valueOf(id.substring("org.paxle.".length()).toUpperCase());
 		}
 		
 		/**
 		 * @param name the {@link #toString() name} of the component's representative constant to return
 		 * @return the {@link MWComponents}-constant whose human-readable name is given by <code>name</code>
 		 *         or <code>null</code> if no such component is known
 		 * @see #toString()
 		 */
 		public static MWComponents valueOfHumanReadable(final String name) {
 			return valueOf(name.toUpperCase());
 		}
 		
 		/**
 		 * @return an array of the human-readable {@link #toString() names} of all known {@link IMWComponent}s
 		 *         in the order {@link #values()} returns the {@link Enum#valueOf(Class, String)}-constants
 		 * @see #values()
 		 * @see #toString() 
 		 */
 		public static String[] humanReadableNames() {
 			final MWComponents[] comps = values();
 			final String[] compStrs = new String[comps.length];
 			for (int i=0; i<comps.length; i++)
 				compStrs[i] = comps[i].toString();
 			return compStrs;
 		}
 	}
 	
 	/**
 	 * A {@link WindowAdapter} which silently removes the associated {@link DIComponent} from the map of active
 	 * {@link DesktopServices#serviceFrames}. It is attached to all frames
 	 * {@link DesktopServices#createDefaultFrame(DIComponent, Long) created} for displaying an (DI-internal or
 	 * external) {@link DIComponent}s.
 	 * @see DesktopServices#serviceFrames
 	 * @see DesktopServices#show(Long)
 	 * @see DesktopServices#close(Long)
 	 */
 	private class FrameDICloseListener extends WindowAdapter {
 		
 		private Long id;
 		private DIComponent c;
 		
 		/**
 		 * When {@link #windowClosed(WindowEvent)} is invoked by the associated frame, this listener will
 		 * first look up the {@link DIComponent} which registered under the <code>id</code> and will then
 		 * remove it from {@link DesktopServices#serviceFrames}.
 		 * This constructor is used for {@link DIComponent}s which are not defined in this bundle but have
 		 * been {@link DesktopServices#serviceChanged(ServiceReference, int) registered} to this bundle.
 		 * @see DesktopServices#servicePanels
 		 * @param id the {@link Constants#SERVICE_ID} of the {@link DIComponent}
 		 */
 		public FrameDICloseListener(final Long id) {
 			this.id = id;
 		}
 		
 		/**
 		 * When {@link #windowClosed(WindowEvent)} is invoked by the associated frame, this listener will
 		 * remove the given {@link DIComponent} from {@link DesktopServices#serviceFrames}.
 		 * This constructor shall be used when {@link DesktopServices#servicePanels} is known to not contain
 		 * the specific {@link DIComponent} as is the case with the {@link IDesktopServices.Dialogues dialogues}
 		 * provided by this bundle.
 		 * @see DesktopServices#valueOf(org.paxle.desktop.IDesktopServices.Dialogues)
 		 * @param c the {@link DIComponent} to remove
 		 */
 		public FrameDICloseListener(final DIComponent c) {
 			this.c = c;
 		}
 		
 		/*
 		 * (non-Javadoc)
 		 * @see java.awt.event.WindowAdapter#windowClosed(java.awt.event.WindowEvent)
 		 */
 		@Override
 		public void windowClosed(WindowEvent e) {
 			final DIComponent c = (this.c != null) ? this.c : servicePanels.get(id);
 			if (c != null) {
 				c.close();
 				serviceFrames.remove(c);
 			}
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	private static final Class<IMWComponent> MWCOMP_CLASS = IMWComponent.class;
 	
 	/* ============================================================================ *
 	 * Class-names needed for reflective access to bundles this bundle does not
 	 * strictly depend on
 	 * ============================================================================ */
 	
 	/** The fully qualified name of the interface under which the CommandDB of the DataLayer-bundle registered to the framework */
 	private static final String ICOMMANDDB = "org.paxle.data.db.ICommandDB";
 	/** The fully qualified name of the interface under which the RobotsTxtManager of the FilterRobotsTxt-bundle registered to the framework */
 	private static final String IROBOTSM = "org.paxle.filter.robots.IRobotsTxtManager";
 	/** The fully qualified name of the interface under which the ServletManager of the GUI-bundle registered to the framework */
 	private static final String ISERVLET_MANAGER = "org.paxle.gui.IServletManager";
 	
 	/** The fully qualified name of the {@link org.paxle.desktop.impl.DICommandProvider} for this bundle
 	 * @see DesktopServices#COMMAND_PROVIDER
 	 */
 	private static final String DI_COMMAND_PROVIDER = "org.paxle.desktop.impl.DICommandProvider";
 	/** The fully qualified name of the interface, the proprietary CommandProvider of the Equinox-framework is accessable under */
 	private static final String COMMAND_PROVIDER = "org.eclipse.osgi.framework.console.CommandProvider";
 	
 	/** Default depth for crawls initiated using {@link #startDefaultCrawl(String)} */
 	private static final int DEFAULT_PROFILE_MAX_DEPTH = 3;
 	/** Default name of CrawlProfiles for crawls initiated by the DI-bundle */
 	private static final String DEFAULT_NAME = "desktop-crawl";
 	
 	/* ============================================================================ *
 	 * ConfigurationManagement-related constants
 	 * ============================================================================ */
 	private static final String PREF_PID = IDesktopServices.class.getName();
 	private static final String PREF_OPEN_BROWSER_STARTUP 	= PREF_PID + "." + "openBrowser";
 	private static final String PREF_SHOW_SYSTRAY 			= PREF_PID + "." + "showTrayMenu";
 	private static final String PREF_OPEN_BROWSER_SERVLET	= PREF_PID + "." + "openServlet";
 	private static final String PREF_OPEN_BROWSER_SERVLET_DEFAULT = "/search";
 	
 	private static final String FILTER = String.format("(%s=%s)", Constants.OBJECTCLASS, DIComponent.class.getName());	// TODO
 	
 	/* ============================================================================ *
 	 * Object variables
 	 * ============================================================================ */
 	
 	private final Log logger = LogFactory.getLog(DesktopServices.class);
 	private final ServiceManager manager;
 	private final IDIBackend backend;
 	private final HashMap<Integer,Integer> profileDepthMap = new HashMap<Integer,Integer>();
 	// private final Map<Dialogues,Frame> dialogues = new EnumMap<Dialogues,Frame>(Dialogues.class);
 	private final String[] locales;
 	
 	private final Set<IDIEventListener> listeners = new HashSet<IDIEventListener>();
 	
 	private final Hashtable<Long,DIComponent> servicePanels = new Hashtable<Long,DIComponent>();
 	private final HashMap<DIComponent,Frame> serviceFrames = new HashMap<DIComponent,Frame>();
 	
 	private final ServiceRegistration regManagedService;
 	private final ServiceRegistration regConsoleCmdProvider;
 	
 	private SystrayMenu trayMenu = null;
 	private boolean browserOpenable = true;
 	private String openServlet = PREF_OPEN_BROWSER_SERVLET_DEFAULT;
 	
 	// TODO: get desktop-services working without a backend, dialogues can still be useful and
 	//       may be started in different ways than using the tray-menu
 	public DesktopServices(final ServiceManager manager, final IDIBackend backend) {
 		this.manager = manager;
 		this.backend = backend;
 		
 		// get available locales
 		final IResourceBundleTool rbt = manager.getService(IResourceBundleTool.class);
 		locales = (rbt == null) ? null : rbt.getLocaleArray("IDesktopService", Locale.ENGLISH);
 		
 		// catch all ServiceEvents for DIComponents
 		try {
 			manager.addServiceListener(this, FILTER);
 			logger.info("added desktop-integration as service-listener for '" + FILTER + "'");
 		} catch (InvalidSyntaxException e) { e.printStackTrace(); }
 		
 		// register managed-service for CM
 		final Hashtable<String,Object> regProps = new Hashtable<String,Object>();
 		regProps.put(Constants.SERVICE_PID, PREF_PID);
 		regManagedService = manager.registerService(this, regProps, new String[] {
 				ManagedService.class.getName(),
 				MetaTypeProvider.class.getName()
 		});
 		
 		// if running in Equinox OSGi-framework, register a CommandProvider, otherwise fail silently
 		regConsoleCmdProvider = registerDICommandProvider();
 		
 		// initialize the local variables from properties-store and initially update the configuration
 		initDS();
 	}
 	
 	@SuppressWarnings("unchecked")
 	private ServiceRegistration registerDICommandProvider() {
 		try {
 			final Class cmdProviderC = Class.forName(COMMAND_PROVIDER);
 			final Class<?> diCmdProviderC = Class.forName(DI_COMMAND_PROVIDER);
 			return manager.registerService(
 					diCmdProviderC.getConstructor(getClass()).newInstance(this),
 					null,
 					cmdProviderC);
 		} catch (ClassNotFoundException e) {
 			final String msg = "Not running in Equinox, Paxle desktop command provider won't be available.";
 			if (logger.isDebugEnabled()) {
 				logger.debug(msg, e);
 			} else {
 				logger.info(msg);
 			}
 		} catch (Exception e) { e.printStackTrace(); }
 		return null;
 	}
 	
 	private void initDS() {
 		// check whether starting the browser on startup is set in the config and open it if necessary
 		final ConfigurationAdmin cadmin = manager.getService(ConfigurationAdmin.class);
 		Dictionary<?,?> props = null;
 		if (cadmin != null) try {
 			final Configuration conf = cadmin.getConfiguration(PREF_PID, manager.getBundle().getLocation());
 			if (conf != null)
 				props = conf.getProperties();
 		} catch (IOException e) { e.printStackTrace(); }
 		if (props == null)
 			props = getDefaults();
 		
 		final Boolean openBrowserStartup = (Boolean)props.get(PREF_OPEN_BROWSER_STARTUP);
 		if (openBrowserStartup.booleanValue()) {
 			final Object openServletObj = props.get(PREF_OPEN_BROWSER_SERVLET);
 			if (openServletObj != null)
 				openServlet = (String)openServletObj;
 			
 			// opening the browser
 			browseDefaultServlet(false);
 		}
 		
 		// look for services which already have registered as DIComponents and record them in the map
 		try {
 			final ServiceReference[] refs = manager.getServiceReferences(null, FILTER);
 			if (refs != null)
 				for (final ServiceReference ref : refs)
 					serviceChanged(ref, ServiceEvent.REGISTERED);
 		} catch (InvalidSyntaxException e) { e.printStackTrace(); }
 	}
 	
 	public void shutdown() {
 		// close all open dialogues
 		for (final Frame frame : serviceFrames.values())
 			frame.dispose();
 		serviceFrames.clear();
 		
 		// remove tray-menu
 		setTrayMenuVisible(false);
 		
 		// unregister services registered during instantiation
 		if (regManagedService != null)
 			regManagedService.unregister();
 		if (regConsoleCmdProvider != null)
 			regConsoleCmdProvider.unregister();
 		manager.removeServiceListener(this);
 	}
 	
 	/* ========================================================================== *
 	 * OSGi Services related methods
 	 * ========================================================================== */
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.osgi.framework.ServiceListener#serviceChanged(org.osgi.framework.ServiceEvent)
 	 */
 	public void serviceChanged(ServiceEvent event) {
 		serviceChanged(event.getServiceReference(), event.getType());
 	}
 	
 	private void serviceChanged(final ServiceReference ref, final int type) {
 		final Long id = (Long)ref.getProperty(Constants.SERVICE_ID);
 		logger.debug("received service changed event for " + ref + ", type: " + type);
 		if (id == null) {
 			logger.error("(un)registered DIComponent has no valid service-id: " + ref);
 			return;
 		}
 		
 		switch (type) {
 			case ServiceEvent.REGISTERED: {
 				// retrieve the service and put it under it's id in the servicePanels-map
 				// to be able to access it via this id later
 				final DIComponent panel = manager.getService(ref, DIComponent.class);
 				if (panel == null) {
 					logger.error("tried to register DIComponent with null-reference");
 					break;
 				}
 				servicePanels.put(id, panel);
 				final DIServiceEvent event = new DIServiceEvent(id, panel);
 				synchronized (this) {
 					for (final IDIEventListener l : listeners)
 						l.serviceRegistered(event);
 				}
 				logger.info("registered DIComponent '" + panel.getTitle() + "' with service-ID " + id);
 			} break;
 			
 			case ServiceEvent.UNREGISTERING: {
 				// close possibly open dialogue and remove it from the servicePanels-map
 				close(id);
 				DIComponent panel = servicePanels.get(id);
 				if (panel == null) {
 					logger.warn("unregistering DIComponent which is unknown to DesktopServices: " + ref);
 					break;
 				}
 				final DIServiceEvent event = new DIServiceEvent(id, panel);
 				synchronized (this) {
 					for (final IDIEventListener l : listeners)
 						l.serviceUnregistering(event);
 				}
				panel = null;
 				servicePanels.remove(id);
 				logger.info("unregistered DIComponent '" + panel.getTitle() + "' with service-ID " + id);
 				manager.ungetService(ref);
 			} break;
 			
 			case ServiceEvent.MODIFIED: {
 			} break;
 		}
 	}
 	
 	public synchronized void addDIEventListener(IDIEventListener listener) {
 		listeners.add(listener);
 	}
 	
 	public synchronized void removeDIEventListener(IDIEventListener listener) {
 		listeners.remove(listener);
 	}
 	
 	public void shutdownFramework() {
 		try {
 			manager.shutdownFramework();
 		} catch (BundleException e) {
 			Utilities.showExceptionBox("error shutting down framework", e);
 			logger.error("error shutting down framework", e);
 		}
 	}
 	
 	public void restartFramework() {
 		try {
 			manager.restartFramework();
 		} catch (BundleException e) {
 			Utilities.showExceptionBox("error restarting framework", e);
 			logger.error("error restarting framework", e);
 		}
 	}
 	
 	private static Hashtable<String,Object> getDefaults() {
 		final Hashtable<String,Object> defaults = new Hashtable<String,Object>();
 		defaults.put(PREF_OPEN_BROWSER_STARTUP, Boolean.TRUE);
 		defaults.put(PREF_SHOW_SYSTRAY, Boolean.TRUE);
 		defaults.put(PREF_OPEN_BROWSER_SERVLET, PREF_OPEN_BROWSER_SERVLET_DEFAULT);
 		return defaults;
 	}
 	
 	public String[] getLocales() {
 		return locales;
 	}
 	
 	public ObjectClassDefinition getObjectClassDefinition(String id, String loc) {
 		final Locale locale = (loc == null) ? Locale.ENGLISH : new Locale(loc);
 		final ResourceBundle rb = ResourceBundle.getBundle("OSGI-INF/l10n/IDesktopService", locale);
 		
 		abstract class SingleAD implements AttributeDefinition {
 			
 			private final String pid;
 			
 			public SingleAD(final String pid) {
 				this.pid = pid;
 			}
 			
 			public int getCardinality() {
 				return 0;
 			}
 			
 			public String getDescription() {
 				return rb.getString("desktopService." + pid.substring(pid.lastIndexOf('.') + 1) + ".desc");
 			}
 			
 			public String getID() {
 				return pid;
 			}
 			
 			public String getName() {
 				return rb.getString("desktopService." + pid.substring(pid.lastIndexOf('.') + 1) + ".name");
 			}
 		}
 		
 		final class BooleanAD extends SingleAD {
 			
 			public BooleanAD(final String pid) {
 				super(pid);
 			}
 			
 			public String[] getDefaultValue() {
 				return new String[] { Boolean.TRUE.toString() };
 			}
 			
 			public String[] getOptionLabels() { return null; }
 			public String[] getOptionValues() { return null; }
 			
 			public int getType() {
 				return BOOLEAN;
 			}
 			
 			public String validate(String value) {
 				return null;
 			}
 		}
 		
 		return new ObjectClassDefinition() {
 			public AttributeDefinition[] getAttributeDefinitions(int filter) {
 				final Object servletManager = manager.getService(ISERVLET_MANAGER);
 				final boolean hasWebUi = (servletManager != null);
 				
 				final AttributeDefinition[] ads = new AttributeDefinition[(hasWebUi) ? 3 : 2];
 				int idx = 0;
 				ads[idx++] = new BooleanAD(PREF_OPEN_BROWSER_STARTUP);	// option for opening the browser on start-up
 				
 				if (hasWebUi) {
 					ads[idx++] = new SingleAD(PREF_OPEN_BROWSER_SERVLET) {
 						
 						private final String defaultValue;
 						private final String[] servletNames; {
 							try {
 								final Class<?> servletManagerClazz = servletManager.getClass();
 								final Method getServlets = servletManagerClazz.getMethod("getServlets");
 								
 								final Map<?,?> servlets = (Map<?,?>)getServlets.invoke(servletManager);
 								servletNames = new String[servlets.size()];
 								int idx = 0;
 								String defVal = null;
 								final Iterator<?> it = servlets.keySet().iterator();
 								while (it.hasNext()) {
 									final String name = (String)it.next();
 									servletNames[idx] = name;
 									if (name.equals(PREF_OPEN_BROWSER_SERVLET_DEFAULT))
 										defVal = name;
 									idx++;
 								}
 								defaultValue = defVal;
 								Arrays.sort(servletNames);
 							} catch (Exception e) { throw new RuntimeException(e); }
 						}
 						
 						public String[] getDefaultValue() {	return new String[] { defaultValue }; }
 						public String[] getOptionLabels() {	return servletNames; }
 						public String[] getOptionValues() { return servletNames; }
 						public int getType() { return STRING; }
 						public String validate(String value) { return null; }
 					};
 				}
 				ads[idx++] = new BooleanAD(PREF_SHOW_SYSTRAY);			// option to show/hide the system tray icon
 				return ads;
 			}
 			public String getDescription() { return rb.getString("desktopService.desc"); }
 			public InputStream getIcon(int size) throws IOException { return getClass().getResourceAsStream("/OSGI-INF/images/systemtray.png"); }
 			public String getID() { return PREF_PID; }
 			public String getName() { return rb.getString("desktopService.name"); }
 		};
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
 			
 			final Object openServletObj = properties.get(PREF_OPEN_BROWSER_SERVLET);
 			if (openServletObj != null)
 				openServlet = (String)openServletObj;
 			
 			// PREF_OPEN_BROWSER_STARTUP only matters for the initialization of this class,
 			// changing the values does not necessitate runtime-changes.
 		} catch (Throwable e) { e.printStackTrace(); }
 	}
 	
 	public Map<Long,DIComponent> getAdditionalComponents() {
 		return Collections.unmodifiableMap(servicePanels);
 	}
 	
 	/* ========================================================================== *
 	 * Desktop-related methods
 	 * ========================================================================== */
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.paxle.desktop.IDesktopServices#isBrowserOpenable()
 	 */
 	public boolean isBrowserOpenable() {
 		return browserOpenable;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.paxle.desktop.IDesktopServices#isTrayMenuVisible()
 	 */
 	public boolean isTrayMenuVisible() {
 		return trayMenu != null;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.paxle.desktop.IDesktopServices#setTrayMenuVisible(boolean)
 	 */
 	public void setTrayMenuVisible(final boolean yes) {
 		if (yes && !isTrayMenuVisible()) {
 			trayMenu = new SystrayMenu(this, manager.getBundle().getResource(TRAY_ICON_LOCATION));
 		} else if (!yes && isTrayMenuVisible()) {
 			trayMenu.close();
 			trayMenu = null;
 		}
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.paxle.desktop.IDesktopServices#getPaxleUrl(java.lang.String[])
 	 */
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
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.paxle.desktop.IDesktopServices#browseUrl(java.lang.String)
 	 */
 	public boolean browseUrl(final String url) {
 		return browseUrl(url, false, true);
 	}
 	
 	public boolean browseUrl(final String url, final boolean force, final boolean displayErrMsg) {
 		if (url == null) {
 			JOptionPane.showMessageDialog(null, "HTTP service not accessible", "Error", JOptionPane.ERROR_MESSAGE);
 		} else if (browserOpenable || force) try {
 			if ((browserOpenable = backend.getDesktop().browse(url))) {
 				return true;
 			} else if (displayErrMsg) {
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
 	
 	public void browseDefaultServlet(final boolean displayErrMsg) {
 		browseServlet(openServlet, displayErrMsg);
 	}
 	
 	public void browseServlet(final String servlet, final boolean displayErrMsg) {
 		final Object servletManager = manager.getService(ISERVLET_MANAGER);
 		if (servletManager == null) {
 			final String msg = "Cannot open servlet '" + servlet + "', GUI ServletManager not available";
 			logger.warn(msg);
 			if (displayErrMsg)
 				JOptionPane.showMessageDialog(null, msg, "Error opening servlet", JOptionPane.ERROR_MESSAGE);
 		} else try {
 			final Class<?> servletManagerClazz = servletManager.getClass();
 			final Method getFullAlias = servletManagerClazz.getMethod("getFullAlias", String.class);
 			final String servletPath = (String)getFullAlias.invoke(servletManager, servlet);
 			final String url = getPaxleUrl(servletPath);
 			logger.debug("Opening browser: " + url);
 			final boolean success = browseUrl(url, true, displayErrMsg);
 			logger.info(((success) ? "Succeeded" : "Failed") + " opening browser for " + url);
 		} catch (Exception e) { e.printStackTrace(); }
 	}
 	
 	/* ========================================================================== *
 	 * Dialogue handling
 	 * ========================================================================== */
 	
 	private Frame createDefaultFrame(final DIComponent container, final Long id) {
 		return Utilities.setFrameProps(
 				new JFrame(),
 				container.getContainer(),
 				container.getTitle(),
 				Utilities.SIZE_PACK,
 				true,
 				Utilities.LOCATION_BY_PLATFORM,
 				null,
 				false,
 				(id.longValue() < 0L) ? new FrameDICloseListener(container) : new FrameDICloseListener(id));
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.paxle.desktop.IDesktopServices#openDialogue(org.paxle.desktop.IDesktopServices.Dialogues)
 	 */
 	public void openDialogue(final Dialogues d) {
 		final DIComponent c;
 		switch (d) {
 			case CCONSOLE: c = new CrawlingConsole(this); break;
 			case SETTINGS: c = new SettingsPanel(this); break;
 			case STATS: c = new StatisticsPanel(this); break;
 			case BUNDLES: c = new BundlePanel(this); break;
 			
 			default:
 				throw new RuntimeException("switch-statement does not cover " + d);
 		}
 		show(valueOf(d), c);
 	}
 	
 	public Frame show(final Long id) {
 		final DIComponent c = servicePanels.get(id);
 		if (c == null)
 			return null;
 		return show(id, c);
 	}
 	
 	public Frame show(final Long id, final DIComponent c) {
 		Frame frame = serviceFrames.get(c);
 		if (frame == null)
 			serviceFrames.put(c, frame = createDefaultFrame(c, id));
 		c.setFrame(frame);
 		show(frame);
 		return frame;
 	}
 	
 	public void close(final Long id) {
 		final DIComponent c = servicePanels.get(id);
 		if (c != null) {
 			final Frame frame = serviceFrames.remove(c);
 			if (frame != null)
 				frame.dispose();
 		}
 	}
 	
 	private static Long valueOf(final Dialogues d) {
 		return Long.valueOf(-d.ordinal() - 1L);
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.paxle.desktop.IDesktopServices#closeDialogue(org.paxle.desktop.IDesktopServices.Dialogues)
 	 */
 	public void closeDialogue(final Dialogues d) {
 		close(valueOf(d));
 	}
 	
 	private static void show(final Frame frame) {
 		final int extstate = frame.getExtendedState();
 		if ((extstate & Frame.ICONIFIED) == Frame.ICONIFIED)
 			frame.setExtendedState(extstate ^ Frame.ICONIFIED);
 		if (!frame.isVisible())
 			frame.setVisible(true);
 		frame.toFront();
 	}
 	
 	/* ========================================================================== *
 	 * Convenience methods
 	 * ========================================================================== */
 	
 	@SuppressWarnings("unchecked")
 	public IMWComponent<ICommand> getMWComponent(final MWComponents comp) {
 		try {
 			final IMWComponent<?>[] comps = manager.getServices(MWCOMP_CLASS, comp.toQuery());
 			if (comps != null && comps.length > 0)
 				return (IMWComponent<ICommand>)comps[0];
 		} catch (InvalidSyntaxException e) {
 			Utilities.showExceptionBox(e);
 			e.printStackTrace();
 		}
 		return null; 
 	}
 	
 	public boolean isServiceAvailable(MWComponents comp) {
 		try {
 			return manager.hasService(MWCOMP_CLASS, comp.toQuery());
 		} catch (InvalidSyntaxException e) {
 			Utilities.showExceptionBox(e);
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	public ServiceManager getServiceManager() {
 		return manager;
 	}
 	
 	public IDIBackend getBackend() {
 		return backend;
 	}
 	
 	public void startDefaultCrawl(final String location) {
 		try {
 			startCrawl(location, DEFAULT_PROFILE_MAX_DEPTH);
 		} catch (ServiceException ee) {
 			Utilities.showURLErrorMessage("Starting crawl failed: " + ee.getMessage(), location);
 			logger.error("Starting crawl of URL '" + location + "' failed: " + ee.getMessage(), ee);
 		}
 	}
 	
 	public void startCrawl(final String location, final int depth) throws ServiceException {
 		final IReferenceNormalizer refNormalizer = manager.getService(IReferenceNormalizer.class);
 		if (refNormalizer == null)
 			throw new ServiceException("Reference normalizer", IReferenceNormalizer.class.getName());
 		final URI uri = refNormalizer.normalizeReference(location);
 		
 		// check uri against robots.txt
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
 		
 		// get or create the crawl profile to use for URI
 		ICommandProfile cp = null;
 		final ICommandProfileManager profileDB = manager.getService(ICommandProfileManager.class);
 		if (profileDB == null)
 			throw new ServiceException("Profile manager", ICommandProfileManager.class.getName());
 		
 		final Integer depthInt = Integer.valueOf(depth);
 		final Integer id = profileDepthMap.get(depthInt);
 		if (id != null)
 			cp = profileDB.getProfileByID(id.intValue());
 		if (cp == null) {
 			// create a new profile
 			cp = new CommandProfile();
 			cp.setMaxDepth(depth);
 			cp.setName(DEFAULT_NAME);
 			profileDB.storeProfile(cp);
 		}
 		if (id == null || cp.getOID() != id.intValue())
 			profileDepthMap.put(depthInt, Integer.valueOf(cp.getOID()));
 		
 		// get the command-db object and it's method to enqueue the URI
 		final Object commandDB;
 		final Method enqueueCommand;
 		try {
 			commandDB = manager.getService(ICOMMANDDB);
 			if (commandDB == null)
 				throw new ServiceException("Command-DB", ICOMMANDDB);
 			enqueueCommand = commandDB.getClass().getMethod("enqueue", URI.class, int.class, int.class);
 			
 			final Object result = enqueueCommand.invoke(commandDB, uri, Integer.valueOf(cp.getOID()), Integer.valueOf(0));
 			if (((Boolean)result).booleanValue()) {
 				logger.info("Initiated crawl of URL '" + uri + "'");
 			} else {
 				logger.info("Initiating crawl of URL '" + uri + "' failed, URL is already known");
 			}
 		} catch (Exception e) {
 			throw new ServiceException("Crawl start", e.getMessage(), e);
 		}
 	}
 }
