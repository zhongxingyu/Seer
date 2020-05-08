 package it.polimi.elet.servlet;
 
 import it.polimi.elet.selflet.configuration.DispatcherConfiguration;
 import it.polimi.elet.selflet.ssh.SSHConnection;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class SelfletLogRetriever extends HttpServlet {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	private static final String USERNAME = DispatcherConfiguration.username;
 	private static final String PASSWORD = DispatcherConfiguration.password;
 	private static final int PORT_NUMBER = 22;
	private static final String LOCALFOLDER = "/home/guser/selflet/selflet-request-dispatcher/logs";
 	private static final String REMOTEFOLDER = "/home/guser/selflet/selflets-log/";
 	
 	
 	@Override
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		
 		String ipAddress = request.getParameter("ipAddress");
 		getLogsFromIp(ipAddress);
 	}
 	
 	private void getLogsFromIp(String ipAddress){
 		SSHConnection connection = createNewSSHConnection(ipAddress);
 //		String command = "scp -r " + USERNAME + "@" + ipAddress + ":" + LOCALFOLDER + " " + REMOTEFOLDER;
 //		connection.execute(command);
 //		connection.getFile(REMOTEFOLDER, LOCALFOLDER);
 		connection.getFiles(REMOTEFOLDER, LOCALFOLDER);
 		
 	}
 	
 	private SSHConnection createNewSSHConnection(String ipAddress) {
 		return new SSHConnection(USERNAME, ipAddress, PORT_NUMBER, PASSWORD);
 	}
 
 }
