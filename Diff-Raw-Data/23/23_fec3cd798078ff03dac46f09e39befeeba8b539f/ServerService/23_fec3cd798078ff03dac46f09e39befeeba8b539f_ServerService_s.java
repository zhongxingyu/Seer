 package de.hsrm.inspector.services;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.jdom2.Element;
 import org.jdom2.JDOMException;
 import org.jdom2.input.SAXBuilder;
 
 import android.app.IntentService;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Resources;
 import android.net.Uri;
 import android.preference.PreferenceManager;
 import de.hsrm.inspector.R;
 import de.hsrm.inspector.activities.SettingsActivity;
 import de.hsrm.inspector.gadgets.intf.Gadget;
 import de.hsrm.inspector.web.HttpServer;
 
 /**
  * {@link IntentService} to manage the {@link HttpServer} and its configuration.
  * 
  * @author dobae
  */
 public class ServerService extends IntentService {
 
 	private static HttpServer mServer;
 
 	public static final String CMD_INIT = "init";
 	public static final String CMD_DESTROY = "destroy";
 	public static final String CMD_SETTINGS = "settings";
 	public static final String CMD_REFRESH = "refresh";
 	public static final String CMD_START_TIMEOUT = "start-timeout";
 	public static final String CMD_STOP_TIMEOUT = "stop-timeout";
 	public static final String CMD_PREFERENCE_CHANGED = "preference-changed";
 
 	public static final String DATA_CHANGED_PREFERENCE = "preference:changed";
 
 	public static final String EXTRA_PREFERENCES = "preferences";
 
 	/**
 	 * Default constructor.
 	 */
 	public ServerService() {
 		super("HttpService");
 		android.os.Debug.waitForDebugger();
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		android.os.Debug.waitForDebugger();
 		if (intent != null) {
 			onHandleIntent(intent);
 		}
 		return START_STICKY;
 	}
 
 	@Override
 	protected void onHandleIntent(Intent intent) {
 		android.os.Debug.waitForDebugger();
		// Log.e("", "Intent received: " + intent.toURI());
 		String command = Uri.parse(intent.toURI()).getHost();
 		if (command.equals(CMD_INIT)) {
 			init();
 			start();
 		} else if (command.equals(CMD_DESTROY)) {
 			stop();
 		} else if (command.equals(CMD_SETTINGS)) {
 			init();
 			Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
 			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 
 			HashSet<String> prefs = new HashSet<String>();
 			for (Gadget g : mServer.getConfiguration().values()) {
 				if (g.getPreferences() != null) {
 					prefs.add(g.getPreferences());
 				}
 			}
 			String[] preferences = prefs.toArray(new String[0]);
 			Arrays.sort(preferences);
 			i.putExtra(EXTRA_PREFERENCES, preferences);
 
 			startActivity(i);
 		} else if (command.equals(CMD_REFRESH)) {
 			init();
 		} else if (command.equals(CMD_START_TIMEOUT)) {
 			if (mServer != null) {
 				mServer.startTimeout();
 			}
 		} else if (command.equals(CMD_STOP_TIMEOUT)) {
 			if (mServer != null) {
 				mServer.stopTimeout();
 			}
 		} else if (command.equals(CMD_PREFERENCE_CHANGED)) {
 			if (intent.hasExtra(DATA_CHANGED_PREFERENCE)) {
 				changeGadgetPreference(intent.getExtras().getString(DATA_CHANGED_PREFERENCE));
 			}
 		}
 	}
 
 	/**
 	 * Initializes {@link #mServer} if not initialized yet. Also reads
 	 * {@link SharedPreferences} of this application and parse them into a
 	 * {@link ConcurrentHashMap}.
 	 */
 	private void init() {
 		if (mServer == null) {
 			mServer = new HttpServer(getApplication());
 			mServer.setConfiguration(configure());
 		}
 	}
 
 	/**
 	 * Starts the {@link #mServer}.
 	 */
 	private void start() {
 		if (mServer != null) {
 			mServer.startThread();
 		}
 	}
 
 	/**
 	 * Stops the {@link #mServer}.
 	 */
 	private void stop() {
 		if (mServer != null) {
 			mServer.stopThread();
 		}
 	}
 
 	/**
 	 * Reads {@link SharedPreferences} of this applications and parses them into
 	 * a {@link ConcurrentHashMap}. This {@link Map} contains {@link String} as
 	 * key and {@link Gadget} as value. The {@link Gadget} instances are
 	 * instantiated by {@link Resources} xml configuration.
 	 * 
 	 * @return
 	 */
 	private ConcurrentHashMap<String, Gadget> configure() {
 		ConcurrentHashMap<String, Gadget> config = mServer.getConfiguration();
 		if (config == null) {
 			config = new ConcurrentHashMap<String, Gadget>();
 			for (Gadget g : config.values()) {
 				if (g.getPreferences() != null) {
 					PreferenceManager.setDefaultValues(getApplicationContext(),
 							getResources().getIdentifier(g.getPreferences(), "xml", getPackageName()), true);
 				}
 			}
 		}
 
 		readPreferences(config);
 		return config;
 	}
 
 	/**
 	 * Creates runtime preferences based on inspector.xml and
 	 * {@link SharedPreferences}.
 	 * 
 	 * @param config
 	 * @return
 	 */
 	private void readPreferences(ConcurrentHashMap<String, Gadget> config) {
 		Context c = getApplicationContext();
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
 		if (config.size() == 0) {
 			readGadgets(config);
 		}
 		for (Gadget g : config.values()) {
 			String id = g.getIdentifier().toLowerCase();
 			g.setKeepAlive(prefs.getBoolean(id + ":" + c.getString(R.string.configuration_keep_alive), g.isKeepAlive()));
 			g.setPermissionType(Integer.parseInt(prefs.getString(
 					id + ":" + c.getString(R.string.configuration_permission), c.getString(R.string.auth_type_granted))));
 			long timeout = Long.parseLong(prefs.getString(id + ":" + c.getString(R.string.configuration_timeout), ""
 					+ Long.MIN_VALUE));
 			if (timeout != Long.MIN_VALUE) {
 				g.setTimeout(timeout * 1000);
 			}
 		}
 	}
 
 	/**
 	 * Creates a gadget object from inspector.xml gadget-element.
 	 * 
 	 * @param gadget
 	 * @param gadgets
 	 * @throws ClassNotFoundException
 	 * @throws IllegalAccessException
 	 * @throws InstantiationException
 	 */
 	@SuppressWarnings("unchecked")
 	private void createGadget(Element gadget, ConcurrentHashMap<String, Gadget> gadgets) throws ClassNotFoundException,
 			IllegalAccessException, InstantiationException {
 		try {
 			Class<Gadget> gadgetClass = (Class<Gadget>) Class.forName(gadget.getChildText(getApplicationContext()
 					.getString(R.string.configuration_class)));
 
 			if (gadget.getChild(getApplicationContext().getString(R.string.configuration_identifiers)) != null) {
 				Element identifiers = gadget.getChild(getApplicationContext().getString(
 						R.string.configuration_identifiers));
 				for (Element identifier : identifiers.getChildren(getApplicationContext().getString(
 						R.string.configuration_identifier))) {
 					Gadget g = (Gadget) gadgetClass.newInstance();
 					g.setIdentifier(identifier.getText().toUpperCase());
 					if (gadget.getChild(getString(R.string.configuration_preference_screen)) != null) {
 						g.setPreferences(gadget.getChildText(getString(R.string.configuration_preference_screen)));
 					}
 					if (gadgets.contains(g.getIdentifier())) {
 						gadgets.replace(g.getIdentifier(), g);
 					} else {
 						gadgets.put(g.getIdentifier(), g);
 					}
 				}
 			} else {
 				Gadget g = (Gadget) gadgetClass.newInstance();
 				g.setIdentifier(gadget.getChildText(
 						getApplicationContext().getString(R.string.configuration_identifier)).toUpperCase());
 				if (gadget.getChild(getString(R.string.configuration_preference_screen)) != null) {
 					g.setPreferences(gadget.getChildText(getString(R.string.configuration_preference_screen)));
 				}
 				if (gadgets.contains(g.getIdentifier())) {
 					gadgets.replace(g.getIdentifier(), g);
 				} else {
 					gadgets.put(g.getIdentifier(), g);
 				}
 			}
 		} catch (ClassCastException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Reads inspector.xml configuration for all supported gadget identifiers
 	 * and their linked classes.
 	 * 
 	 * @param config
 	 */
 	private void readGadgets(ConcurrentHashMap<String, Gadget> config) {
 		try {
 			SAXBuilder builder = new SAXBuilder();
 			InputStream file = getApplicationContext().getResources().openRawResource(R.raw.inspector_default);
 			Element root = builder.build(file).getRootElement();
 			List<Element> gadgets = root.getChildren(getApplicationContext().getString(R.string.configuration_gadgets));
 
 			for (Element gadget : gadgets) {
 				createGadget(gadget, config);
 			}
 			String serverTimeout = root.getAttributeValue(getString(R.string.configuration_timeout));
 			if (serverTimeout != null) {
 				try {
 					mServer.setTimeoutTime(Long.parseLong(serverTimeout) * 1000);
 				} catch (ClassCastException e) {
 					e.printStackTrace();
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (JDOMException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Update a {@link Gadget} configuration at runtime.
 	 * 
 	 * @param key
 	 *            {@link String} key of {@link SharedPreferences} value to
 	 *            update.
 	 */
 	private void changeGadgetPreference(String key) {
 		Context c = getApplicationContext();
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
 		String[] splits = key.split(":");
 		if (splits.length == 2 && mServer != null && mServer.getConfiguration() != null) {
 			ConcurrentHashMap<String, Gadget> settings = mServer.getConfiguration();
 			Gadget gadget = settings.get(splits[0].toUpperCase());
 
 			if (c.getString(R.string.configuration_keep_alive).equals(splits[1])) {
 				try {
 					gadget.setKeepAlive(prefs.getBoolean(key, gadget.isKeepAlive()));
 				} catch (ClassCastException e) {
 					String value = prefs.getString(key, gadget.isKeepAlive() + "");
 					gadget.setKeepAlive(Boolean.parseBoolean(value));
 				}
 			} else if (c.getString(R.string.configuration_timeout).equals(splits[1])) {
 				try {
 					gadget.setTimeout(prefs.getLong(key, gadget.getTimeout()));
 				} catch (ClassCastException e) {
 					String value = prefs.getString(key, gadget.getTimeout() + "");
 					gadget.setTimeout(Long.parseLong(value));
 				}
 			} else if (c.getString(R.string.configuration_permission).equals(splits[1])) {
 				try {
 					gadget.setPermissionType(prefs.getInt(key, gadget.getPermissionType()));
 				} catch (ClassCastException e) {
 					String value = prefs.getString(key, gadget.getPermissionType() + "");
 					gadget.setPermissionType(Integer.parseInt(value));
 				}
 			}
 		}
 	}
 }
