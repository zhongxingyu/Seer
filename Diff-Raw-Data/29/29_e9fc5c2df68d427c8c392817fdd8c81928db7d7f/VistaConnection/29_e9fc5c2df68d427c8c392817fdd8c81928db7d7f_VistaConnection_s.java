 package gov.va.med.iss.connection.actions;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Font;
 import java.util.ArrayList;
 import java.awt.Toolkit;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.HashMap;
 import java.util.Map;
 
 import gov.va.med.foundations.adapter.cci.VistaLinkConnection;
 import gov.va.med.foundations.rpc.RpcRequest;
 import gov.va.med.foundations.rpc.RpcRequestFactory;
 import gov.va.med.foundations.rpc.RpcResponse;
 import gov.va.med.foundations.security.vistalink.EclipseConnection;
 import gov.va.med.foundations.security.vistalink.CallbackHandlerSwing;
 import gov.va.med.foundations.security.vistalink.VistaKernelPrincipalImpl;
 import gov.va.med.foundations.utilities.FoundationsException;
 // import gov.va.med.foundations.security.vistalink.VistaLoginModule;
 import gov.va.med.foundations.security.vistalink.EclipseLoginModule;
 import gov.va.med.iss.connection.dialogs.SecondaryServerSelectionDialogData;
 import gov.va.med.iss.connection.dialogs.SecondaryServerSelectionDialogForm;
 import gov.va.med.iss.connection.utilities.MPiece;
 
 import javax.security.auth.callback.CallbackHandler;
 import javax.security.auth.login.LoginContext;
 import javax.security.auth.login.LoginException;
 import javax.security.auth.login.AppConfigurationEntry;
 import javax.security.auth.login.Configuration;
 import javax.security.auth.Subject;
 
 import javax.swing.BorderFactory;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 import javax.swing.SwingUtilities;
 import javax.swing.ToolTipManager;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.border.Border;
 
 //import x.gov.va.med.iss.log4j.*;
 
 
 import org.apache.log4j.xml.DOMConfigurator;
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.PropertyConfigurator;
 import org.apache.log4j.Logger;
 
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.IWorkbenchWindowActionDelegate;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.core.runtime.Preferences;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IWorkbenchWindow;
 
 import gov.va.med.iss.connection.VLConnectionPlugin;
 import gov.va.med.iss.connection.preferences.ConnectionPreferencePage;
 
 /**
  * Our sample action implements workbench action delegate.
  * The action proxy will be created by the workbench and
  * shown in the UI. When the user tries to use the action,
  * this delegate will be created and execution will be 
  * delegated to it.
  * @see IWorkbenchWindowActionDelegate
  */
 public class VistaConnection implements IWorkbenchWindowActionDelegate {
 	public IWorkbenchWindow window;
 	public static IWorkbenchWindow windowx;
 	/**
 	 * The constructor.
 	 */
 	
 
 	private static EclipseConnection eclipseConnection = null;
 	private static VistaKernelPrincipalImpl userPrincipal = null;
 	private static VistaKernelPrincipalImpl primaryPrincipal = null;
 	private static ArrayList connectionList = new ArrayList(20);
 	private static String primaryServerName = "";
 	private static String primaryServerAddress = "";
 	private static String primaryPort = "";
     private static String primaryProject = ""; // JLI 090908 added for Source Code Version Control
     private static String primaryServerID = "";
     private static VistaLinkConnection primaryConnection = null;
     //
     private static String currServerAddress = "";
     private static String currServerName = "";
     private static String currServerProject = ""; // JLI 090908 added for Source Code Version Control
     private static String currPort = "";
     private static VistaLinkConnection currConnection = null;
     //
 	private static String[] checkedServers = null;
 
 	private static String defaultURL = "";
 	private static String defaultName = "";
 	private static String defaultPort = "";
     private static String defaultProject = ""; // JLI 090908 added for Source Code Version Control
     private static boolean shownNoServerDefined = false;
     private static boolean badConnection = false;
 
 
 	public VistaConnection() {
 	}
 	
 	public VistaConnection(String serverName, String serverAddress, String portNumber, String projectName) { // JLI 090908 added for Source Code Version Control
         currServerAddress = serverAddress;
         currServerName = serverName;
         currPort = portNumber;
         currServerProject = projectName;  // JLI 090908 added for Source Code Version Control
     }
 	
 	public static void main(String[] args) {
 //		BasicConfigurator.configure();
 		VistaConnection vistaConnection = new VistaConnection();
 		vistaConnection.run();
 	}
 
 	public void run(IAction action) {
 		run();
 	}
 	
 	/***
 	 * Used without arguments to get the primary connection
 	 * 
 	 * @return an instance of the VistaLinkConnection for the 
 	 * connection to the primary server
 	 */
 	public static VistaLinkConnection getConnection() {
 		//String str = VistaConnection.getPrimaryServer();
 		if (! VistaConnection.getPrimaryServer()) {
 			return null;
 		}
 		else {
 			if (primaryConnection == null) {
 				VistaConnection vistaConnection = new VistaConnection();
 				vistaConnection.run();
 				vistaConnection = null;
 				if (currConnection == null) {
 					return null;
 				}
 				setPrimaryServer(); // #2
 				try {
 					if (VistaConnection.checkForProductionAccount(VistaConnection.getConnection())) {
 						MessageDialog.openWarning(
 								PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
 								"Meditor Plug-in Routine Load - PRODUCTION account",
 								"** WARNING **\n\n"+
 								MPiece.getPiece(VistaConnection.getCurrentServer(),";",2,3)+" is a PRODUCTION account.\n\n" +
 								"You should use extreme caution in editing and saving a " +
 								"routine back into a PRODUCTION account.");
 					}
 				} catch (Exception e) {
 					MessageDialog.openError(
 							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
 							"Meditor Plug-in Routine Load Error",
 							"An error occured while checking for whether the current\n" +
 							"server "+VistaConnection.getCurrentConnection()+" is a\n"+
 							"production server or not.");
 				}
 			}
 			else {  // reset current value to show primary server
 				currServerName = primaryServerName;
 				currPort = primaryPort;
 				currServerAddress = primaryServerAddress;
 	            currServerProject = primaryProject;
 				currConnection = primaryConnection;
 				userPrincipal = primaryPrincipal;
 			}
 			// 080523 code added to make sure connection is good, in case of
 			// loss of pre-existing connection
 			if (primaryConnection != null) {
 				// 080523 added functionality to check for loss of connection
 				// if lost, it notifies user to sign on again, and removes
 				// current connection
 				// 091029 if (! checkConnection(primaryConnection, primaryServerName, primaryPort, primaryServerAddress)) {
 		        if (! checkConnection(primaryConnection, primaryPort, primaryServerAddress)) {
 					primaryConnection = null;
 					primaryConnection = getConnection();
 				}
 			}
 			return primaryConnection;
 		}
 	}
 	
 	// 080523 added functionality to check for loss of connection
 	// if lost, it notifies user to sign on again, and removes
 	// current connection
 	
 	// JLI 100226 - connection should only depend on port and url, 
 	// removed other factors (name and projectname) 
 	private static boolean checkConnection(VistaLinkConnection connection,
 			String port, String url) {
 		if (connection == null)
 			return false;
 		RpcRequest vReq = null;
 		boolean result = true;
 		try {
 			vReq = RpcRequestFactory.getRpcRequest("", "XWB GET BROKER INFO");
 		} catch (FoundationsException fe) {
 			result = false;
 		}
 		if (result) {
 			vReq.setUseProprietaryMessageFormat(false);
 			try {
 				RpcResponse vResp = connection.executeRPC(vReq);
 			} catch (Exception e) {
 				result = false;
 			}
 		}
 		if (! result) {
 			IWorkbench wb = PlatformUI.getWorkbench();
 			IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
 			MessageDialog.openInformation(
 					window.getShell(),
 					"VistA Connection Error",
 					"The connection to the server has been lost, sign in again");
 			//ConnectionData connData = getMatchingConnection(name,port, url);
 //            ConnectionData connData = getMatchingConnection(name,port, url, projectName); // JLI 090908 added for Source Code Version Control);
             ConnectionData connData = getMatchingConnection(port, url); // JLI 100226 changed for change in method, since connection depends only on port and url
 			removeConnection(connData);
 		}
 		return result;
 	}
 	
 	public static VistaLinkConnection getConnection(String serverName, String serverAddress, String serverPort) {
         return getConnection(serverName, serverAddress, serverPort, "");
     }
     
     public static VistaLinkConnection getConnection(String serverName, String serverAddress, String serverPort, String serverProject) {
 		currConnection = null;
 		for (int i=0; i<connectionList.size(); i++) {
 			if (! (connectionList.get(i) == null) ) {
 				ConnectionData connData = (ConnectionData)connectionList.get(i);
 				if ((connData.getServerAddress().compareTo(serverAddress) == 0) &&
 						connData.getServerPort().compareTo(serverPort) == 0) {
 					// 080523 added functionality to check for loss of connection
 					// if lost, it notifies user to sign on again, and removes
 					// current connection
 					setServer(connData);
 					if (! checkConnection(currConnection, serverPort, serverAddress)) {
 						currConnection = null;
 					}
 					if (currConnection != null)
 						return currConnection;
 				}
 			}
 		}
 		VistaConnection vistaConnection = new VistaConnection(serverName,serverAddress,serverPort,serverProject);
 		vistaConnection.run();
 		// only add connection if connection has been made
 		if (! (currConnection == null)) {
 			currPort = serverPort;
 			currServerAddress = serverAddress;
 			currServerName = serverName;
 			currServerProject = serverProject;
 			ConnectionData connData = new ConnectionData();
 			connData.setServerAddress(serverAddress);
 			connData.setServerPort(currPort);
 			connData.setServerName(currServerName);
 			connData.setConnection(currConnection);
 			connData.setPrincipal(userPrincipal);
 			connData.setEclipseConnection(eclipseConnection);
 			connectionList.add(connData);
 			if (primaryConnection == null) {
 				primaryConnection = currConnection;
 				primaryPrincipal = userPrincipal;
 				primaryServerName = serverName;
 				primaryServerAddress = serverAddress;
 				primaryPort = currPort;
                 primaryProject = serverProject;
 			}
 		}
 		return currConnection;
 	}
 	
 	public static VistaKernelPrincipalImpl getPrincipal() {
 		if (userPrincipal == null) {
 			VistaConnection vistaConnection = new VistaConnection();
 			vistaConnection.run();
 			vistaConnection = null;
 		}
 		return userPrincipal;
 	}
 	
 	public static void disconnect() {
  		if (connectionList.size() == 0) {
  			// nothing to disconnect
  		}
  		else if (connectionList.size() == 1) {
 			ConnectionData connData = (ConnectionData)connectionList.get(0);
 			removeConnection(connData);
 		}
 		else if (PlatformUI.getWorkbench().isClosing()){  // Workbench closing
 			for (int i=0; i<connectionList.size(); i++) {
 				if (connectionList.get(i) != null) {
 					ConnectionData connData = (ConnectionData)connectionList.get(i);
 					removeConnection(connData);
 				}
 			}
 		}
 		else {	// user closing selected connections
 			ArrayList serverList = ConnectionPreferencePage.getServerList();
 			if (serverList.size() > 1) {
 				SecondaryServerSelectionDialogData.setTotalList(serverList);
 				String[] servers = SecondaryServerSelectionDialogData.getAllServers();
 				int totCount = 0;
 				for (int i=0; i<servers.length; i++) {
 					String name = MPiece.getPiece(servers[i],";",2);
 					String port = MPiece.getPiece(servers[i],";",4);
 					String url = MPiece.getPiece(servers[i],";",3);
                     String project = MPiece.getPiece(servers[i],";",5);
 					//ConnectionData connData = getMatchingConnection(name,port,url);
                     //ConnectionData connData = getMatchingConnection(name,port,url,project);
 // JLI 110914                    ConnectionData connData = getMatchingConnection(port,url); // JLI 100226 changed for change in method, since connection depends only on port and url
                     ConnectionData connData = getMatchingConnection(port,url); // JLI 100226 changed for change in method, since connection depends only on port and url
 					if (! (connData == null)) {
 						totCount++;
 					}
 				}
 				String[] activeServers = new String[totCount];
 				totCount = 0;
 				for (int i=0; i<servers.length; i++) {
 					String name = MPiece.getPiece(servers[i],";",2);
 					String port = MPiece.getPiece(servers[i],";",4);
 					String url = MPiece.getPiece(servers[i],";",3);
                     String project = MPiece.getPiece(servers[i],";",5); // JLI 090908 added for Source Code Version Control
                     //ConnectionData connData = getMatchingConnection(name,port,url);
 //                    ConnectionData connData = getMatchingConnection(name,port,url,project); // JLI 090908 added for Source Code Version Control);
                     ConnectionData connData = getMatchingConnection(port,url); // JLI 100226
 					if (! (connData == null)) {
 						activeServers[totCount++] = servers[i];
 					}
 				}
 				SecondaryServerSelectionDialogForm secondary = new SecondaryServerSelectionDialogForm(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),SWT.NONE);
 				SecondaryServerSelectionDialogData data = secondary.open("DISCONNECT",activeServers);
 				checkedServers = data.getCheckedList();
 				for (int i=0; i<checkedServers.length; i++) {
 					String name = MPiece.getPiece(checkedServers[i],";",1);
 					String port = MPiece.getPiece(checkedServers[i],";",3);
 					String url = MPiece.getPiece(checkedServers[i],";",2);
                     String project = MPiece.getPiece(checkedServers[i],";",4); // JLI 090908 added for Source Code Version Control
                     //ConnectionData connData = getMatchingConnection(name,port,url);
 //                    ConnectionData connData = getMatchingConnection(name,port,url,project); // JLI 090908 added for Source Code Version Control);
 // JLI 110914                    ConnectionData connData = getMatchingConnection(port,url); // JLI 100226 changed for change in method, since connection depends only on port and url
                     ConnectionData connData = getMatchingConnection(port,url); // JLI 110914
 					if (! (connData == null)) {
 						removeConnection(connData);
 					}
 				}
 			}
 		}
 	}
 	
 	static private void removeConnection(ConnectionData connData) {
 		EclipseConnection econnect = connData.getEclipseConnection();
 		econnect.logout();
 		VistaKernelPrincipalImpl principal = connData.getPrincipal();
 		VistaLinkConnection vlConnection = connData.getConnection();
 		econnect = null;
 		principal = null;
 		vlConnection = null;
 		if ((currPort.compareTo(connData.getServerPort()) == 0) &&
 				(currServerAddress.compareTo(connData.getServerAddress()) == 0)){
 			eclipseConnection = null;
 			userPrincipal = null;
 			currConnection = null;
 			currPort = "";
 			currServerAddress= "";
 			currServerName = "";
             currServerProject = "";
 		}
 		if ((primaryPort.compareTo(connData.getServerPort()) == 0) &&
 				(primaryServerAddress.compareToIgnoreCase(connData.getServerAddress()) == 0)){
 			primaryPrincipal = null;
 			primaryConnection = null;
 			primaryServerName = "";
 			primaryPort = "";
 			primaryServerAddress = "";
             primaryProject = "";
 		}
 		connectionList.remove(connData);
 	}
 
 	/**
 	 * The action has been activated. The argument of the
 	 * method represents the 'real' action sitting
 	 * in the workbench UI.
 	 * @see IWorkbenchWindowActionDelegate#run
 	 */
 	public void run() {
 		if (! (window == null)) {
 			windowx = window;
 		}
 		else if (! (windowx == null)) {
 			window = windowx;
 		}
 		IWorkbench wb = PlatformUI.getWorkbench();
 		window = wb.getActiveWorkbenchWindow();
 		if (window == null) {
 			MessageDialog.openInformation(
 					window.getShell(),
 					"VistA Connection Error",
 					"window not defined in run()");
 		}
 		if (! (currConnection == null)) {
 /*
 			MessageDialog.openInformation(
 					window.getShell(),
 					"Vista Connection",
 					"Already connected to Server: "+currServerAddress+" at Port: "+currPort);
 */
 		}
 		else if (currServerAddress.compareTo("") == 0) {
 			try {
 				getDefaultPrefs();
 				clearShownNoServerDefined();
 				if ((currServerAddress.compareTo("") == 0) || (currPort.compareTo("") == 0)) {
 					// JLI 110127 extracted to method
 					ShowConnectionServerMsg();
 				}
 			} catch (Exception e) {
 			
 			}
 		}
 		if ((currServerAddress.compareTo("") != 0) && (getMatchingConnection(currPort, currServerAddress) == null)){
 //				PropertyConfigurator.configure("log4jConfig.properties");
 //				DOMConfigurator.configure("%ECLIPSEHOME%/plugins/gov.va.med.iss.connection_0.9.0/log4jConfig.xml");
 			BasicConfigurator.configure();
 //				logger.fatal("output from logger.fatal");
 //				System.out.println("Output from println in VistaConnection");
 		
 			try {
 				eclipseConnection = new EclipseConnection();
 			} catch (Exception e) {
 				eclipseConnection = null;
 				MessageDialog.openInformation(
 						window.getShell(),
 						"VistA Connection Error",
 						"Error in EclipseConnection creation "+e.getMessage());
 				System.out.println("Error in EclipseConnection creation: "+e.getMessage());
 			}
 			if (!(eclipseConnection == null)) {
 				try {  // The following line gets the connection to the server
 					userPrincipal = eclipseConnection.getConnection(currServerAddress, currPort, window);
 				} catch (Exception e) {
 					MessageDialog.openInformation(
 							window.getShell(),
 							"VistA Connection Error",
 							"Error in eclipseConnection.getConnection "+e.getMessage());
 					System.out.println("Error in eclipseConnection.getConnection: "+e.getMessage());
 					userPrincipal = null;
 				}
 				if (!(userPrincipal == null)) {
 					try {
 						currConnection = userPrincipal.getAuthenticatedConnection();
 					} catch (Exception e) {
 						MessageDialog.openInformation(
 								window.getShell(),
 								"VistA Connection Error",
 								"Error in userPrincipal.getAuthenicatedConnection()"+e.getMessage());
 						System.out.println("Error in userPrincipal.getAuthen...: "+e.getMessage());
 					}
 					// JLI 101108 commented out - was resetting each connection to primary
 /*
 					if (currConnection != null) {
 						VistaConnection.setPrimaryServer();
 					}
 */
 				}
 			}
 		}
 	}
 	
 	static public void getDefaultPrefs() throws Exception {
 		Preferences prefs = VLConnectionPlugin.getDefault().getPluginPreferences();
 		defaultName = prefs.getString(ConnectionPreferencePage.P_SERVER_NAME);
 		// JLI 110127 if default name is empty, show message and then exit
 		if (defaultName == "") {
 			ShowConnectionServerMsg();
 		}
 		currServerName = defaultName;
 		if (primaryServerName.compareTo("") != 0) {
 			if (primaryServerName.compareTo(currServerName) != 0) {
 	            IWorkbench wb = PlatformUI.getWorkbench();
 	            IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
 	            MessageDialog.openInformation(
 	                    window.getShell(),
 	                    "VistA Connection Change",
 	                    "Switching Primary Servers - Preferences Order has changed");
 	            clearPrimary();
 			}
 		}
 		defaultURL = prefs.getString(ConnectionPreferencePage.P_SERVER);
 		currServerAddress = defaultURL;
 		defaultPort = MPiece.getPiece(prefs.getString(ConnectionPreferencePage.P_PORT),";");
 		currPort = defaultPort;
 		defaultProject = prefs.getString(ConnectionPreferencePage.P_PORT);
 		defaultProject = MPiece.getPiece(defaultProject,";",2);
 		currServerProject = defaultProject;
 		setPrimaryServer();
 		
 //		currServerName = defaultName;
 //		currServerAddress = defaultURL;
 //		currPort = defaultPort;
 //		currServerProject = defaultProject;
 	}
 	
 	static public boolean getPrimaryServer() {
 		setBadConnection(false);
         String str = primaryServerName + ";" + primaryServerAddress + ";" + primaryPort + ";" + primaryProject; // JLI 090908 added for Source Code Version Control;
         try {
         	getDefaultPrefs();
         } catch (Exception e) {
         	defaultName = "";
 // 110817        	return ";;;";
         	setBadConnection(true);  //  110817
         	return false;  //  110817
         }
         String defaultStr = defaultName + ";" + defaultURL + ";" + defaultPort + ";" + defaultProject; // JLI 090908 added for Source Code Version Control;
         if (str.compareTo(";;;") == 0) {
         	str = defaultStr;
         }
         if (currServerName.compareTo("") == 0) {
             currServerName = defaultName;
             currServerAddress = defaultURL;
             currPort = defaultPort;
             currServerProject = defaultProject;
         }
         if (str.compareTo(";;;") == 0) { // JLI 090908 added for Source Code Version Control") == 0) {
             primaryServerName = defaultName;
             primaryServerAddress = defaultURL;
             primaryPort = defaultPort;
             primaryProject = defaultProject;
             currServerName = defaultName;
             currServerAddress = defaultURL;
             currPort = defaultPort;
             currServerProject = defaultProject;
             currConnection = null;
             primaryConnection = null;
             userPrincipal = null;
             eclipseConnection = null;
             primaryServerID = defaultStr;
             if (! isBadConnection()) {
             	VistaConnection.getConnection();
             }
             //return defaultStr;  //  110817
 			return !isBadConnection();
         }
         primaryServerID = str;
         //return str;  //  110817
         return !isBadConnection();
     }
 	
 	static public String getPrimaryServerID() {
 		return primaryServerID;
 	}
     
     static public String getPrimaryProject() {
         return primaryProject;
     }
 	
 	static public void setServer(ConnectionData connData) {
 		currConnection = connData.getConnection();
 		eclipseConnection = connData.getEclipseConnection();
 		userPrincipal = connData.getPrincipal();
 		currServerAddress = connData.getServerAddress();
 		currPort = connData.getServerPort();
 		currServerName = connData.getServerName();
 	}
 	
 	static public void setPrimaryServer(ConnectionData connData) {
 		primaryConnection = connData.getConnection();
 		primaryPrincipal = connData.getPrincipal();
 		primaryServerAddress = connData.getServerAddress();
 		primaryPort = connData.getServerPort();
 		primaryServerName = connData.getServerName();
 		setServer(connData);
 	}
 	static public void setPrimaryServer() {
 		primaryConnection = currConnection;
 		primaryPrincipal = userPrincipal;
 		primaryServerAddress = currServerAddress;
 		primaryServerName = currServerName;
 		primaryPort = currPort;
         primaryProject = currServerProject; // JLI 090908 added for Source Code Version Control
 		//ConnectionData connData = getMatchingConnection(currServerName,currServerAddress,currPort); 
 //        ConnectionData connData = getMatchingConnection(currServerName,currServerAddress,currPort,currServerProject); // JLI 090908 added for Source Code Version Control); 
         ConnectionData connData = getMatchingConnection(currPort, currServerAddress); // JLI 100226 changed for change in method, since connection depends only on port and url 
 		if (connData == null) {
 //			if (currConnection == null) {
 				currConnection = getConnection(currServerName, currServerAddress, currPort, currServerProject);
 //			}
 			connData = getMatchingConnection(currPort, currServerAddress); // JLI 100226 changed for change in method, since connection depends only on port and url
 			if ((connData == null) && (currConnection != null)) {
 				connData = new ConnectionData();
 				connData.setServerName(currServerName);
 				connData.setServerPort(currPort);
 				connData.setConnection(currConnection);
 				connData.setPrincipal(userPrincipal);
 				connData.setEclipseConnection(eclipseConnection);
 				connectionList.add(connData);
 			}
 		}
 		if (connData != null) {
 			setServer(connData);
 		}
 	}
 	
 	static public void clearPrimary(){
 			primaryServerName = "";
 			primaryPort = "";
 			primaryServerAddress = "";
 			primaryConnection = null;
 			primaryPrincipal = null;
 			primaryProject = "";
 			if (defaultName.compareTo("") != 0) {
 				primaryServerName = defaultName;
 				primaryPort = defaultPort;
 				primaryServerAddress = defaultURL;
 				primaryProject = defaultProject;
 				currServerName = defaultName;
 				currServerProject = defaultProject;
 				currServerAddress = defaultURL;
 				currPort = defaultPort;
 			}
 	}
 	
 	static public String getCurrentServer() {
 		//return currServerName + ";" + currServerAddress + ";" + currPort; 
         return currServerName + ";" + currServerAddress + ";" + currPort + ";" + currServerProject; 
     }
 	
 	static public void setCurrentServer(String serverName, String serverAddress, String portNumber, String projectName) {
 		currServerName = serverName;
 		currServerAddress = serverAddress;
 		currPort = portNumber;
 		currServerProject = projectName;
 		if (getMatchingConnection(currPort, currServerAddress) == null) {
 			currConnection = getConnection(serverName, currServerAddress, currPort, currServerProject);
 		}
 		else {
 			currConnection = getMatchingConnection(currPort, currServerAddress).getConnection();
 		}
 	}
     
     static public String getCurrentProject() {
         return currServerProject;
 	}
 	
 	static public VistaLinkConnection getCurrentConnection() {
 		if (currConnection == null) {  // must be primary connection
 			currConnection = VistaConnection.getConnection();
 			if (currConnection == null) {
 				return null;
 			}
 		}
 		currConnection = getMatchingConnection(currPort, currServerAddress).getConnection();
 		if (currConnection == null) {
 			currConnection = getConnection(currServerName, currServerAddress, currPort, currServerProject);
 		}
 		if (currConnection != null) {
 			// 080523 added functionality to check for loss of connection
 			// if lost, it notifies user to sign on again, and removes
 			// current connection
 			if (! checkConnection(currConnection, primaryPort, primaryServerAddress)) {
 				currConnection = null;
 				currConnection = getCurrentConnection();
 			}
 		}
 		return currConnection;
 	}
 
 	/**
 	 * Selection in the workbench has been changed. We 
 	 * can change the state of the 'real' action here
 	 * if we want, but this can only happen after 
 	 * the delegate has been created.
 	 * @see IWorkbenchWindowActionDelegate#selectionChanged
 	 */
 	public void selectionChanged(IAction action, ISelection selection) {
 	}
 
 	/**
 	 * We can use this method to dispose of any system
 	 * resources we previously allocated.
 	 * @see IWorkbenchWindowActionDelegate#dispose
 	 */
 	public void dispose() {
 		if (! (currConnection == null)) {
 			disconnect();
 		}
 	}
 
 	/**
 	 * We will cache window object in order to
 	 * be able to provide parent shell for the message dialog.
 	 * @see IWorkbenchWindowActionDelegate#init
 	 */
 	public void init(IWorkbenchWindow window) {
 		windowx = window;
 		this.window = window;
 	}
 	
 	//static public ConnectionData getMatchingConnection(String serverName, String currPort, String currServerAddress) {
 	//static public ConnectionData getMatchingConnection(String serverName, String currPort, String currServerAddress, String projectName) { // JLI 090908 added for Source Code Version Control) {
 	// JLI 100226 modified to remove serverName and projectName, since connnection should depend only on server port and address
 // JLI 110914	static public ConnectionData getMatchingConnection(String portValue, String serverURL) { // JLI 090908 added for Source Code Version Control) {
 	static public ConnectionData getMatchingConnection(String portValue, String serverURL) { // JLI 090908 added for Source Code Version Control) {
 		for (int i=0; i<connectionList.size(); i++) {
 			if (! (connectionList.get(i) == null) ) {
 				ConnectionData connData = (ConnectionData)connectionList.get(i);
 				String serverPort = connData.getServerPort();
 				String serverAddress = connData.getServerAddress();
 				if (connData.getServerPort().equalsIgnoreCase(portValue)
 						&& connData.getServerAddress().equalsIgnoreCase(serverURL)){ // JLI 090908 added for Source Code Version Control
 					return connData;
 				}
 			}
 		}
 		return null;
 	}
 	
 	/*
 	 * checkForProductionAccount - determines whether the current connection is to a PRODUCTION account or not
 	 * 
 	 * argument myConnection - VistaLinkConnection - the current connection
 	 * 
 	 * returns boolean indicating whether the current connection is indicated as a PRODUCTION system (true) or not (false).
 	 */
 	static public boolean checkForProductionAccount(VistaLinkConnection myConnection) throws Exception {
 		RpcRequest vReq1 = RpcRequestFactory.getRpcRequest("","XT ECLIPSE M EDITOR");
 		vReq1.setUseProprietaryMessageFormat(true);
 		vReq1.getParams().setParam(1, "string", "PROD");  // RD  RL  GD  GL  RS PROD
 		RpcResponse vResp1 = myConnection.executeRPC(vReq1);
 		boolean response = false;
 		if (vResp1.getResults().indexOf("1") == 0) {
 			response = true;
 		}
 		return response;
 	}
 
 /*  
 // modified to be based solely on URL and port number
 // so multiple server designations - based on name used 
 // to identify server and possible projects specified -
 // can use a single connection to the same server and port
 static public ConnectionData getMatchingConnection(String currServerAddress, String currPort) { // JLI 090908 added for Source Code Version Control
     for (int i=0; i<connectionList.size(); i++) {
         if (! (connectionList.get(i) == null) ) {
             ConnectionData connData = (ConnectionData)connectionList.get(i);
             if (connData.getServerAddress().equalsIgnoreCase(currServerAddress) 
                     && connData.getServerPort().equalsIgnoreCase(currPort)){
 					return connData;
 				}
 			}
 		}
 		return null;
 	}
 */
 	static void ShowConnectionServerMsg() throws Exception {
 		if (! shownNoServerDefined ) {
 			shownNoServerDefined = true;
 			Shell parentShell = Display.getDefault().getActiveShell();
 			MessageDialog.openInformation(
 					parentShell,
 					"Server, Port Specification", 
 			"Open Windows | Preferences | Vista | Connection and enter Server and Port data");
 		}
 		throw new Exception("No valid Server currently defined - Open Windows | Preferences | Vista | Connection and enter Server and Port data");
 	}
 	
 	static public void clearShownNoServerDefined() {
 		shownNoServerDefined = false;
 	}
 	
 	static public void setBadConnection(boolean state) {
 		badConnection = state;
 	}
 	
 	static public boolean isBadConnection() {
 		return badConnection;
 	}
 }
