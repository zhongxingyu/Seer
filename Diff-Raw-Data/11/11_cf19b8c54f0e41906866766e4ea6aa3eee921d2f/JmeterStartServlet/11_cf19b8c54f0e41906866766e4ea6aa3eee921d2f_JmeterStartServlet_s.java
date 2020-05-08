 package it.polimi.elet.servlet;
 
 import static com.google.common.base.Strings.nullToEmpty;
 import it.polimi.elet.selflet.configuration.DispatcherConfiguration;
 import it.polimi.elet.selflet.istantiator.IVirtualMachineIPManager;
 import it.polimi.elet.selflet.istantiator.VirtualMachineIPManager;
 import it.polimi.elet.selflet.schema.SchemaLoader;
 import it.polimi.elet.selflet.ssh.SSHConnection;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class JmeterStartServlet extends HttpServlet {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private final String DISP_IP_ADDR = "disp_ip_addr";
 
 	private final String USERNAME = DispatcherConfiguration.username;
 	private final String PASSWORD = DispatcherConfiguration.password;
 	private final int PORT_NUMBER = 22;
 
 	private final String JMETER_FOLDER = "apache-jmeter/bin/";
	private final String TRACK_FOLDER = "selflet/selflet-request-dispatcher/src/main/resources/";
 	private final String TRACK_NAME = "jmeter_track_selflets.jmx";
 	
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 
 		String dispatcherIpAddress = nullToEmpty(req.getParameter(DISP_IP_ADDR));
 		if (dispatcherIpAddress.isEmpty()) {
 			resp.sendError(404, "empty ip address");
 		}
 
 		try {
 			startJmeterTrack(dispatcherIpAddress);
 		} catch (Exception e) {
 			resp.sendError(404, "Error starting jmeter: " + e.getMessage());
 		}
 		
 		resp.sendRedirect(PageNames.INDEX);
 
 	}
 
 	private void startJmeterTrack(String dispatcherIpAddress) {
 
 		IVirtualMachineIPManager vmManager = VirtualMachineIPManager
 				.getInstance();
 		String jmeterIpAddress = vmManager.getNewIpAddress();
 		vmManager.setJmeterIpAddress(jmeterIpAddress);
		SSHConnection connectionFile = createNewSSHConnection(jmeterIpAddress);
		connectionFile.putFile(TRACK_FOLDER + TRACK_NAME, JMETER_FOLDER + TRACK_NAME);
		connectionFile.disconnect();
 		String commandLocate = "cd " + JMETER_FOLDER;
 		String commandStart = "screen -d -m ./jmeter -n -t " + TRACK_NAME + " -JdispatcherIpAddress=" + dispatcherIpAddress + " -l selflets_results.jtl";
		SSHConnection connection = createNewSSHConnection(jmeterIpAddress);
 		connection.execute(commandLocate + ";" + commandStart);
 		connection.disconnect();
 		vmManager.setJmeterStartTime(createJmeterStartTime());
 
 	}
 
 	private SSHConnection createNewSSHConnection(String ipAddress) {
 		return new SSHConnection(USERNAME, ipAddress, PORT_NUMBER, PASSWORD);
 	}
 	
 	private String createJmeterStartTime() {
 		Calendar cal = Calendar.getInstance();
     	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
     	return sdf.format(cal.getTime());
 	}
 }
