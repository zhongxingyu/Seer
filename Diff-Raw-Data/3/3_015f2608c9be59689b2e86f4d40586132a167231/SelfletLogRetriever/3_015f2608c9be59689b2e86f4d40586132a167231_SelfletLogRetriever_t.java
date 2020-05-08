 package it.polimi.elet.servlet;
 
 import it.polimi.elet.selflet.configuration.DispatcherConfiguration;
 
 import it.polimi.elet.selflet.ssh.SSHConnection;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import static com.google.common.base.Strings.nullToEmpty;
 
 public class SelfletLogRetriever extends HttpServlet {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	private static final String USERNAME = DispatcherConfiguration.username;
 	private static final String PASSWORD = DispatcherConfiguration.password;
 	private static final String LOCALHOST = "127.0.0.1";
 	private static final int PORT_NUMBER = 22;
 	private static final String LOCALFOLDER = "/home/guser/selflet/selflet-request-dispatcher/src/main/webapp/logs/";
 	private static final String REMOTEFOLDER = "/home/guser/selflet/selflets-log/";
 
 	private static final String SCRIPTFOLDER = "/home/guser/selflet/selflet-request-dispatcher/src/main/resources/shell_scripts/";
 	private static final String SCRIPT = "mergeSelfletsLogs.sh";
 
 	@Override
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 
 		String ipAddressesList = nullToEmpty(request.getParameter("ipAddressesList"));
 		String getAllLogs = nullToEmpty(request.getParameter("getLogs"));
 		String clearLogs = nullToEmpty(request.getParameter("clearLogs"));
 		String[] ipAddresses = null;
 		
 		if (!ipAddressesList.isEmpty()) {
 			if (ipAddressesList.contains(",")) {
 				ipAddressesList = ipAddressesList
 						.substring(0, ipAddressesList.length() - 1);
 				ipAddresses = ipAddressesList.split(",");
 			} else {
 				ipAddresses = new String[]{ipAddressesList};
 			}
 		} else {
 			response.sendError(404, "empty ip address");
 		}
 		
 		if(!getAllLogs.isEmpty()){
 			for(String ipAddress : ipAddresses){
 				getLogsFromIp(ipAddress);
 			}
			formatLogs(LOCALHOST);
 		}
 		
 		if(!clearLogs.isEmpty()){
 			for(String ipAddress : ipAddresses){
 				clearLogs(ipAddress);
 			}
 		}
 
 		response.sendRedirect(PageNames.AMAZON);
 	}
 
 	private void getLogsFromIp(String ipAddress) {
 		SSHConnection connection = createNewSSHConnection(ipAddress);
 		connection.getFiles(REMOTEFOLDER, LOCALFOLDER);
 
 	}
 
 	//FIXME not working...
 	private void formatLogs(String ipAddress) {
 		SSHConnection connection = createNewSSHConnection(ipAddress);
 		String command = "cd " + SCRIPTFOLDER + " ; source " + SCRIPT;
 		connection.execute(command);
 	}
 	
 	private void clearLogs(String ipAddress){
 		SSHConnection connection = createNewSSHConnection(ipAddress);
 		String removeCommand = "rm " + REMOTEFOLDER + "*.log";
 		connection.execute(removeCommand);
 	}
 
 	private SSHConnection createNewSSHConnection(String ipAddress) {
 		return new SSHConnection(USERNAME, ipAddress, PORT_NUMBER, PASSWORD);
 	}
 
 }
