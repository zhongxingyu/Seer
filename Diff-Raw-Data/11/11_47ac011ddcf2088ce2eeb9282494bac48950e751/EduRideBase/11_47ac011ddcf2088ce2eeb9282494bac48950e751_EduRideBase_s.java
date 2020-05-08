 package edu.berkeley.eduride.base_plugin;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.UUID;
 
 import javax.net.ssl.HttpsURLConnection;
 
 import org.eclipse.core.runtime.preferences.IEclipsePreferences;
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.preference.PreferenceStore;
 import org.eclipse.ui.IStartup;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 
 import edu.berkeley.eduride.base_plugin.ui.LoginDialog;
 
 //import edu.berkeley.eduride.feedbackview.EduRideFeedback;
 
 public class EduRideBase extends AbstractUIPlugin implements IStartup {
 
 	private static BundleContext context;
 	private static IEclipsePreferences prefs;
 	
 	public static final String PLUGIN_ID = "EduRideBase";
 	public static final String DEFAULT_DOMAIN = "eduride.berkeley.edu";
 	public static final String guestUserName = "Guest";
 	// The shared instance
 	private static EduRideBase plugin = null;
 	private static UUID workspaceID = null;	
 	private static PreferenceStore prefStore = null;
 	private static boolean loggedIn = false;
 	
 	static BundleContext getContext() {
 		return context;
 	}
 	
 	/**
 	 * The constructor
 	 */
 	public EduRideBase() {
 		prefs = InstanceScope.INSTANCE.getNode(PLUGIN_ID);
 		prefStore = new PreferenceStore(prefs.absolutePath());
 		plugin = this;
 	}
 	
 	/**
 	 * Returns the shared instance
 	 * 
 	 * @return the shared instance
 	 */
 	public static EduRideBase getDefault() {
 		return plugin;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
 	 */
 	public void start(BundleContext bundleContext) throws Exception {		
 		EduRideBase.context = bundleContext;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext bundleContext) throws Exception {
 		EduRideBase.context = null;
 	}
 	
 	/*
 	 * Returns null iff not logged in
 	 * @return
 	 */
 	public static String whoami() {
 		String username = prefs.get("username", null);
 		// perhaps push login dialog here (but not if they are looking at preferences)
 		// TODO need to distinguish between 'havent authenticated yet' and 'want to remain a guest'
 		if (username == null && loggedIn) {
 			username = guestUserName;
 		}
 		return username;
 	}
 	
 	public static String whereami() {
 		return prefs.get("domain", DEFAULT_DOMAIN);
 	}
 	
 	public static long hashID() {
 		return prefs.getLong("authHash", -1);
 	}
 	
 	private static boolean isVerified(Object content) {
 		// TODO perform legit verification
 		return true;
 	}
 	
 	public static boolean authenticate(String username, String password, String domain) {
 		boolean success;
 		if (username == guestUserName) {
 			success = true;
 		} else {
 //			try {
 //				URL url = new URL("https", domain, 80, "login"); // TODO put legit target name
 //				HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
 //				Object content = connection.getContent();
 //				success = isVerified(content);
 //			} catch (MalformedURLException e) {
 //				success = false;
 //			} catch (IOException e) {
 //				success = false;
 //			}
 			success = true;
 		}
 		if (success) {
 			prefs.put("username", username);
 			// TODO use legit hash
 			prefs.putInt("authHash", 
 					(username + password + System.currentTimeMillis()).hashCode());
 			prefs.put("domain", domain);
 		}
 		return success;
 	}
 	
 	public static boolean loginPrompt() { // return true if OK is pressed
 		LoginDialog dialog = new LoginDialog();
 		dialog.open();
 		return dialog.getReturnCode() == LoginDialog.OK;
 	}
 	
 	public static boolean isLoggedIn() {
 		return loggedIn;
 	}
 	
 	@Override
 	public void earlyStartup() {
 		// if no workspace id exists, this makes sure to generate it
 		String workspaceIDString = prefs.get("workspaceID", generateWsID());
 		if (workspaceID == null) {
 			workspaceID = UUID.fromString(workspaceIDString);
 		}
 	}
 	
 	private static String generateWsID() {
 		workspaceID = UUID.randomUUID();
 		return workspaceID.toString();
 	}
 
 	public static String getWorkspaceID() {
 		return workspaceID.toString();
 	}
 
 	
 	public IPreferenceStore getPreferenceStore() {
 		return prefStore;
 	}
 }
