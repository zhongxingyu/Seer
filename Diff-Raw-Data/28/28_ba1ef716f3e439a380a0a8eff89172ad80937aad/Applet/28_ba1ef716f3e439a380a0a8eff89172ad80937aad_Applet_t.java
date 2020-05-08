 package com.rightscale.ssh;
 
 import com.rightscale.ssh.ui.GraphicalUI;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 
 import java.net.*;
 import java.io.*;
 import java.util.*;
 import java.security.*;
 
 public class Applet extends java.applet.Applet implements SessionInfo {
 	private static final long serialVersionUID = -7047031265889225736L;
 
 	public static final String AUTH_METHOD_PUBLIC_KEY = "publickey";
 	public static final String AUTH_METHOD_PASSWORD = "password";
 
 	private PropertyChangeSupport _thisBean = new PropertyChangeSupport(this);	
 	private GraphicalUI _ui = null;
 	private Launchpad _launchpad = null;
 	private boolean _launched = false;
 	private boolean _hadFailure = false;
 
 	// //
 	// // Methods that allow the page's JS to query our state
 	// //
 
 	// This method is misnamed because the applet has an interface contract with
 	// its host pages. We
 	// no longer have non-native launchers, so this method should just be
 	// didLaunch or something.
 	// @todo rename this method when the applet params are refactored
 	public boolean ranNative() {
 		return _launched;
 	}
 
 	public boolean hadFailure() {
 		return _hadFailure;
 	}
 
 	// //
 	// // Applet overrides
 	// //
 
 	/**
 	 * Initialization method that will be called after the applet is loaded into
 	 * the browser.
 	 */
 	public void init() {
 		try {
 			AccessController
 					.doPrivileged(new PrivilegedExceptionAction<Object>() {
 						public Object run() throws IOException {
 							init_();
 							return null;
 						}
 					});
 		} catch (PrivilegedActionException e) {
 			_ui.log("Failed to acquire the privilege necessary to initialize the applet.",
 					e);
 		}
 	}
 
 	/**
 	 * Called every time the browser requests a new "instance" of the applet.
 	 */
 	public void start() {
 		try {
 			AccessController
 					.doPrivileged(new PrivilegedExceptionAction<Object>() {
 						public Object run() throws IOException {
 							start_();
 							return null;
 						}
 					});
 		} catch (PrivilegedActionException e) {
 			_ui.log("Failed to acquire the privilege necessary to initialize the applet.",
 					e);
 		}
 	}
 
 	public void stop() {
 	}
 
 	// //
 	// // SessionInfo implementation
 	// //
 
 	public String getUsername() {
 		String v = getParameter("username");
 		if (v != null)
 			return v;
 		else
 			return "root";
 	}
 
 	public String getServer() {
 		String v = getParameter("server");
 
 		if (v != null)
 			return v;
 		else
 			return "localhost";
 	}
 
 	public String getServerUUID() {
 		return getParameter("server-uuid");
 	}
 
 	public String getServerName() {
 		String v = getParameter("server-name");
 
 		if (v != null)
 			return v;
 		else
 			return getServer();
 	}
 
 	public String getAuthMethod() {
 		if ("password".equals(getParameter("auth-method")))
 			return AUTH_METHOD_PASSWORD;
 		else
 			return AUTH_METHOD_PUBLIC_KEY;
 	}
 
 	public String getSpecialPrivateKey() {
 		String newline = System.getProperty("line.separator");
 		String km = getParameter("openssh-key-material");
 		if (km == null)
 			return null;
 		return km.replaceAll("\\*", newline);
 	}
 
 	public String getSpecialPuttyPrivateKey() {
 		String newline = System.getProperty("line.separator");
 		String km = getParameter("putty-key-material");
 		if (km == null)
 			return null;
 		return km.replaceAll("\\*", newline);
 	}
 
 	public String getPassword() {
 		return getParameter("password");
 	}
 
 	public String getUserKeyPath() {
 		return getParameter("user-key-path");
 	}
 	
 	public URL getTroubleshootingLink() {
 		try {
 			return new URL(getParameter("troubleshooting-url"));
 		} catch (MalformedURLException e) {
 			return null;
 		}
 	}
 
 	public File getUserKeyFile() {
 		String path = getUserKeyPath();
 		if (path == null)
 			return null;
 
 		// Split the path into elements, accepting either \ or / as a separator
 		String[] elements = path.split("/|\\\\");
 
 		String home = System.getProperty("user.home");
 
 		StringBuffer canonPath = new StringBuffer();
 		canonPath.append(home);
 
 		for (String elem : elements) {
 			canonPath.append(File.separator);
 			canonPath.append(elem);
 		}
 
 		return new File(canonPath.toString());
 	}
 
 	public Launcher getLauncher() {
 		if(_launchpad != null) {
 			return _launchpad.getLauncher();
 		}
 		else {
 			return null;
 		}
 	}
 
 	public void setLauncher(Launcher launcher) {
 		_thisBean.firePropertyChange("launcher", _launchpad.getLauncher(), launcher);
 		_launchpad.setLauncher(launcher);
 	}
 
 	public void launch() {
 		try {
 			AccessController
 					.doPrivileged(new PrivilegedExceptionAction<Object>() {
 						public Object run() throws IOException {
 							openSession_();
 							return null;
 						}
 					});
 		} catch (PrivilegedActionException e) {
 			_ui.alert("Failed to acquire the privilege necessary to launch SSH.", e);
 		}
 	}
 	
 	public void addPropertyChangeListener(PropertyChangeListener listener) {
 		_thisBean.addPropertyChangeListener(listener);		
 	}
 
 	public void removePropertyChangeListener(PropertyChangeListener listener) {
 		_thisBean.removePropertyChangeListener(listener);		
 	}
 	
 	// //
 	// // Internal properties and accessors.
 	// //
 	
 	protected File getServerKeyFile() {
 		return new File(_launchpad.getSafeDirectory(), getServerUUID());
 	}
 
 	protected File getServerPuttyKeyFile() {
 		return new File(_launchpad.getSafeDirectory(), getServerUUID() + ".ppk");
 	}
 
 	protected File getUserPuttyKeyFile() {
 		File f = getUserKeyFile();
 		String s = f.getPath();
 
 		if (s.endsWith(".ppk")) {
 			return f;
 		} else {
 			return new File(s + ".ppk");
 		}
 	}
 
 	protected boolean hasUserKeyFile() {
 		File f = getUserKeyFile();
 		return (f != null) && f.exists();
 	}
 
 	protected boolean hasUserPuttyKeyFile() {
 		File f = getUserPuttyKeyFile();
 		return (f != null) && f.exists();
 	}
 
 	protected String getUserPrivateKey() {
 		BufferedReader br = null;
 		StringBuffer sb = null;
 
 		try {
 			if (hasUserKeyFile()) {
 				File f = getUserKeyFile();
 				br = new BufferedReader(new InputStreamReader(
 						new FileInputStream(f)));
 				sb = new StringBuffer();
 				while (br.ready()) {
 					sb.append(br.readLine());
 					sb.append("\n");
 				}
 
 				return sb.toString();
 			} else {
 				return null;
 			}
 		} catch (Exception e) {
 			throw new Error(e);
 		} finally {
 			if (br != null) {
 				try {
 					br.close();
 				} catch (Exception e) {
 				}
 			}
 		}
 	}
 
 	protected String getUserPuttyPrivateKey() {
 		BufferedReader br = null;
 		StringBuffer sb = null;
 
 		try {
 			if (hasUserPuttyKeyFile()) {
 				File f = getUserPuttyKeyFile();
 				br = new BufferedReader(new InputStreamReader(
 						new FileInputStream(f)));
 				sb = new StringBuffer();
 				while (br.ready()) {
 					sb.append(br.readLine());
 					sb.append("\n");
 				}
 
 				return sb.toString();
 			} else {
 				return null;
 			}
 		} catch (Exception e) {
 			throw new Error(e);
 		} finally {
 			if (br != null) {
 				try {
 					br.close();
 				} catch (Exception e) {
 				}
 			}
 		}
 	}
 
 	// //
 	// // "Internal" Applet-related methods; all require the
 	// // caller to have already elevated privilege.
 	// //
 
 	private void openSession_() {
 		try {
 			_launched = _launchpad.run();
 		} catch (Exception e) {
 			_ui.alert("Could not launch your computer's SSH client", e);
 		}
 	}
 
 	private void init_() {
 		_ui = new GraphicalUI(this, this);
 		_launchpad = new Launchpad(_ui);
 		
 		_launched = _hadFailure = false;
 
 		Map<KeyFormat, String> privateKeys = new HashMap<KeyFormat, String>();
 
 		if (getAuthMethod().equals(AUTH_METHOD_PUBLIC_KEY)) {
 			String userKey = null, specialKey = null;
 			
 			try {
 				userKey = getUserPrivateKey();
 				specialKey = getSpecialPrivateKey();
 			}
 			catch(Throwable e) {
 				_ui.alert("Cannot read OpenSSH key; maybe there's an applet permissions issue?", e);
 			}
 
 			if (specialKey != null) {
 				privateKeys.put(KeyFormat.OPEN_SSH, specialKey);
 				_ui.log("Added special private OpenSSH key to launcher");
 			} else if (userKey != null) {
 				privateKeys.put(KeyFormat.OPEN_SSH, userKey);
 				_ui.log("Added user's private OpenSSH key to launcher; source: "
 						+ getUserKeyPath());
 			} else {
 				_ui.log(String
 						.format("OpenSSH key not found (userKeyPath=%s, hasUserKeyFile=%s)",
 								getUserKeyPath() != null, hasUserKeyFile()));
 			}
 
 			try {
 				userKey = getUserPuttyPrivateKey();
 				specialKey = getSpecialPuttyPrivateKey();
 			}
 			catch(Throwable e) {
 				_ui.alert("Cannot read PuTTY key; maybe there's an applet permissions issue?", e);
 			}
 			
 			if (specialKey != null) {
 				privateKeys.put(KeyFormat.PUTTY, specialKey);
 				_ui.log("Added special private PuTTY private key to launcher");
 			} else if (userKey != null) {
 				privateKeys.put(KeyFormat.PUTTY, userKey);
 				_ui.log("Added user's private PuTTY key to launcher; source: "
 						+ getUserKeyPath());
 			} else {
 				_ui.log(String
 						.format("PuTTY key not found (userKeyPath=%s, hasUserKeyFile=%s)",
 								getUserKeyPath() != null, hasUserKeyFile()));
 			}
 
 			if (privateKeys.isEmpty()) {
 				_ui.alert("No private keys found; ensure that applet parameters contain user-key-path, openssh-key-material or putty-key-material");
 			}
 		}
 
 		if (getPassword() != null && getPassword().length() > 0) {
 			_launchpad.setPassword(getPassword());
 		}
 
 		// Initialize the launchpad business logic
 		_launchpad.setUsername(getUsername());
 		_launchpad.setServer(getServer());
 		_launchpad.setServerUUID(getServerUUID());
 		_launchpad.setPrivateKeys(privateKeys);
 		
 		// Arbitrary choose the first available launcher, going through our own setter so
 		// we fire a property-change notification for the UI.
 		// @todo consult preferences, or ask the user if multiple choices exist
 		setLauncher(_launchpad.getLaunchers().get(0));
 	}
 
 	private void start_() {
 		if (getUserKeyPath() != null && !hasUserKeyFile()
 				&& !hasUserPuttyKeyFile()) {
 			// We can't find the user's local key file -- just give up!
 			_hadFailure = true;
 			_ui.setDisplayState(UI.MISSING_KEY);
 		} else if (!_launchpad.isLauncherAvailable()) {
 			_ui.setDisplayState(UI.NO_LAUNCHER);
 		} else {
 			openSession_();
 		}
 	}
 }
