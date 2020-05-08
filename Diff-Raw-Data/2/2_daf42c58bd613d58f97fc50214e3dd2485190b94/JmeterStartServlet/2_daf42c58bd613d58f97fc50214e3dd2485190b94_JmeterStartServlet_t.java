 package it.polimi.elet.servlet;
 
 import static com.google.common.base.Strings.nullToEmpty;
 import it.polimi.elet.selflet.configuration.DispatcherConfiguration;
 import it.polimi.elet.selflet.istantiator.IVirtualMachineIPManager;
 import it.polimi.elet.selflet.istantiator.VirtualMachineIPManager;
 import it.polimi.elet.selflet.ssh.SSHConnection;
 
 import java.io.IOException;
 
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
 
 	private final String JMETER_FOLDER = "apache-jmeter/bin";
 	private final String TRACK_NAME = "jmeter_track_selflets.jmx";
 	private final String TRACK_FOLDER = "../../selflet/selflet-request-dispatcher/src/main/resources/";
 
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
 		SSHConnection connection = createNewSSHConnection(jmeterIpAddress);
 		String commandLocate = "cd " + JMETER_FOLDER;
 		String commandStart = "./jmeter -n -t " + TRACK_FOLDER + TRACK_NAME
 				+ " -JdispatcherIpAddress=" + dispatcherIpAddress
 				+ " -l selflets_results.jtl";
 		connection.execute(commandLocate);
 		connection.execute(commandStart);
 
 	}
 
 	private SSHConnection createNewSSHConnection(String ipAddress) {
 		return new SSHConnection(USERNAME, ipAddress, PORT_NUMBER, PASSWORD);
 	}
 }
