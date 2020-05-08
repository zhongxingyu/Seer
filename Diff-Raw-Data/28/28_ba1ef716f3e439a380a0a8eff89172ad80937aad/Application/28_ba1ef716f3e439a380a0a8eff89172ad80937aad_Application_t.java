 package com.rightscale.ssh;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.io.*;
 import java.net.*;
 import javax.swing.*;
 
 import com.rightscale.ssh.ui.GraphicalUI;
 
 import java.util.*;
 
 public class Application implements SessionInfo
 {
     public static final String AUTH_METHOD_PUBLIC_KEY = "publickey";
     public static final String AUTH_METHOD_PASSWORD   = "password";
 
 	public static void main(String args[]) {
         System.exit(new Application(args).run());
 	}
 
     private Map<String, String>  _parameters  = new HashMap<String, String>();
     
 	private PropertyChangeSupport _thisBean   = new PropertyChangeSupport(this);	
     private JFrame                _frame       = new JFrame("SSH Launcher");
     private GraphicalUI           _ui          = null;
     private Launchpad             _launchpad   = null;
 
     Application(String[] args) {
     	for(String s : args) {
             String[] pair = s.split("=");
 
             if(pair.length == 2) {
                _parameters.put(pair[0], pair[1]);
             }
             else {
                throw new IllegalArgumentException("Malformed command-line argument; expecting 'k=v'");
             }
         }
 
     	_frame.setLayout(new BorderLayout());
 
     	_ui = new GraphicalUI(this, _frame);
     	_launchpad = new Launchpad(_ui);    	
     }
 
     /// Run the application.
     public int run()
     {
     	_frame.setSize(400, 140);
     	Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
         int left = (d.width - _frame.getWidth()) / 2;
         int top = (d.height - _frame.getHeight()) / 2;
         _frame.setLocation(left, top);
         
     	_frame.setVisible(true);
     	
         Map<KeyFormat, String> privateKeys = new HashMap<KeyFormat, String>();
 
         try {
 	        if( getAuthMethod().equals(AUTH_METHOD_PUBLIC_KEY) ) {
 	            if( getUserKeyPath() != null && hasUserKeyFile() ) {
 	                privateKeys.put( KeyFormat.OPEN_SSH, getUserPrivateKey() );
 	                _ui.log("User OpenSSH private key loaded from local disk");
 	            }
 	            else if( getSpecialPrivateKey() != null ) {
 	                privateKeys.put( KeyFormat.OPEN_SSH, getSpecialPrivateKey() );
 	                _ui.log("Server-specific OpenSSH private key loaded from parameters");
 	            }
 	            else {
 	                _ui.log("No OpenSSH private key loaded");
 	            }
 	
 	            if( getUserKeyPath() != null && hasUserPuttyKeyFile() ) {
 	                privateKeys.put( KeyFormat.PUTTY, getUserPuttyPrivateKey() );
 	                _ui.log("User PuTTY private key loaded from local disk");
 	            }
 	            if( getSpecialPuttyPrivateKey() != null ) {
 	                privateKeys.put( KeyFormat.PUTTY, getSpecialPuttyPrivateKey() );
 	                _ui.log("Server-specific PuTTY private key loaded from local disk");
 	            }
 	            else {
 	                _ui.log("No PuTTY private key loaded");
 	            }
 	
 	            if(privateKeys.isEmpty() && getUserKeyPath() == null) {
 	                throw new IllegalArgumentException("Unable to identify a private key; add openssh-key-material=, putty-key-material= or user-key-path=");
 	            }
 	        }
 	        else if( getAuthMethod().equals(AUTH_METHOD_PASSWORD)) {
 	            if(getPassword() != null && getPassword().length() > 0) {
 	                _launchpad.setPassword(getPassword());
 	            }
 	            else {
 	                    throw new IllegalArgumentException("Unable to determine password; add password=");
 	            }            
 	        }
 	
 	        String uuid = getServerUUID();
 	        if(uuid == null) {
 	            uuid = getServer();
 	        }
 	
 	        //Initialize the launchpad business logic
 	        _launchpad.setUsername(getUsername());
 	        _launchpad.setServer(getServer());
 	        _launchpad.setServerUUID(uuid);
 	        _launchpad.setPrivateKeys(privateKeys);
 
 			// Arbitrary choose the first available launcher, going through our own setter so
 			// we fire a property-change notification for the UI.
 			// @todo consult preferences, or ask the user if multiple choices exist
 			setLauncher(_launchpad.getLaunchers().get(0));
 	        
 	        if(_launchpad.isLauncherAvailable()) {
 	            return _launchpad.run() == true ? 0 : -1;
 	        }
 	        else {
 	            throw new IllegalArgumentException("No supported SSH client is available.");
 	        }
         }
         catch(IllegalArgumentException e) {
             _ui.alert("Cannot launch SSH: " + e.getMessage());
             return -2;            
         }
         catch(Exception e) {
             _ui.alert("Cannot launch SSH", e);
             return -3;
         }
         finally {
             while(_frame.isVisible()) {
             	try { Thread.sleep(100); } catch(InterruptedException e) {}
             }        	
         }
     }
 
     ////
     //// An Applet workalike method for accessing command line parameters, plus getters for the various params we expect
     ////
 
     protected String getParameter(String name) {
         return _parameters.get(name);
     }
 
     public String getUsername() {
         String v = getParameter("username");
 
         if(v != null)
             return v;
         else
             return System.getProperty("user.name");
     }
 
     public String getServer() {
         String v = getParameter("server");
 
         if(v != null)
             return v;
         else
             return "localhost";
     }
 
     public String getServerUUID() {
         return getParameter("server-uuid");
     }
 
     public String getServerName() {
         String v = getParameter("server-name");
 
         if(v != null)
             return v;
         else
             return getServer();
     }
 
     public String getAuthMethod() {
         if("publickey".equals(getParameter("auth-method")))
             return AUTH_METHOD_PUBLIC_KEY;
         else if("password".equals(getParameter("auth-method")))
             return AUTH_METHOD_PASSWORD;
         else
             return AUTH_METHOD_PUBLIC_KEY;
     }
 
     public String getSpecialPrivateKey() {
         String newline = System.getProperty("line.separator");
         String km = getParameter("openssh-key-material");
         if(km == null) return null;
         return km.replaceAll("\\*", newline);
     }
 
     public String getSpecialPuttyPrivateKey() {
         String newline = System.getProperty("line.separator");
         String km = getParameter("putty-key-material");
         if(km == null) return null;
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
             return new URL( getParameter("troubleshooting-url") );
         }
         catch(MalformedURLException e) {
             return null;
         }
     }
 
     protected File getServerKeyFile() {
         return new File(_launchpad.getSafeDirectory(), getServerUUID());
     }
 
     protected File getServerPuttyKeyFile() {
         return new File(_launchpad.getSafeDirectory(), getServerUUID() + ".ppk");
     }
 
     public File getUserKeyFile() {
         String path = getUserKeyPath();
         if(path == null)
             return null;
 
 
         //Split the path into elements, accepting either \ or / as a separator
         String[] elements = path.split("/|\\\\");
 
         String home = System.getProperty("user.home");
 
         StringBuffer canonPath = new StringBuffer();
         canonPath.append(home);
 
         for(String elem : elements) {
             canonPath.append(File.separator);
             canonPath.append(elem);
         }
 
         return new File(canonPath.toString());
     }
 
 	public Launcher getLauncher() {
 		return _launchpad.getLauncher();
 	}
 
 	public void setLauncher(Launcher launcher) {
 		_thisBean.firePropertyChange("launcher", _launchpad.getLauncher(), launcher);
 		_launchpad.setLauncher(launcher);
 	}
 
 	public void launch() {
 		try {
 			_launchpad.run();
 		} catch (Exception e) {
 			_ui.alert("Cannot launch your computer's SSH client", e);
 		}
 	}
 
 	public void addPropertyChangeListener(PropertyChangeListener listener) {
 		_thisBean.addPropertyChangeListener(listener);
 	}
 
 	public void removePropertyChangeListener(PropertyChangeListener listener) {
 		_thisBean.removePropertyChangeListener(listener);
 	}
 	
     protected File getUserPuttyKeyFile() {
         File f = getUserKeyFile();
         String s = f.getPath();
 
         if(s.endsWith(".ppk")) {
             return f;
         }
         else {
             return new File(s + ".ppk");
         }
     }
 
     protected boolean hasUserKeyFile() {
         File f = getUserKeyFile();
         return f.exists();
     }
 
     protected boolean hasUserPuttyKeyFile() {
         File f = getUserPuttyKeyFile();
         return f.exists();
     }
 
     protected String getUserPrivateKey()
     {
     	BufferedReader br = null;
     	StringBuffer   sb = null;
     	
         try {
             if(hasUserKeyFile()) {
                 File f = getUserKeyFile();
                 br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
                 sb = new StringBuffer();
                 while(br.ready()) {
                     sb.append(br.readLine());
                     sb.append("\n");
                 }
 
                 return sb.toString();
             }
             else {
                 throw new Error("Key file does not exist");
             }
         }
         catch(Exception e) {
             return null;
         }
         finally {
         	if(br != null) {        		
         		try { br.close(); } catch(Exception e) {}
         	}
         }
     }
 
     protected String getUserPuttyPrivateKey()
     {
     	BufferedReader br = null;
     	StringBuffer   sb = null;
     	
         try {
             if(hasUserPuttyKeyFile()) {
                 File f = getUserPuttyKeyFile();
                 br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
                 sb = new StringBuffer();
                 while(br.ready()) {
                     sb.append(br.readLine());
                     sb.append("\n");
                 }
 
                 return sb.toString();
             }
             else {
                 throw new Error("Key file does not exist");
             }
         }
         catch(Exception e) {
             return null;
         }
         finally {
         	if(br != null) {        		
         		try { br.close(); } catch(Exception e) {}
         	}
         }
     }
 }
