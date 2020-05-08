 package uk.ac.nott.mrl.homework.server;
 
 import java.io.IOException;
 import java.net.URLDecoder;
 import java.util.Date;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.hwdb.srpc.Connection;
 
 import uk.ac.nott.mrl.homework.server.model.Device;
 import uk.ac.nott.mrl.homework.server.model.Model;
 
 public class SetName extends HttpServlet
 {
 	private static final Logger logger = Logger.getLogger(SetName.class.getName());
 
 	@Override
 	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
 			IOException
 	{
 		response.setContentType("application/json");
 		// logger.info(request.getRequestURL().toString());
 
 		final String macAddress = request.getParameter("macAddress");
 		final String nameString = URLDecoder.decode(request.getParameter("name"), "UTF-8");
 
 		System.out.println("Set Name :" + macAddress + " - " + nameString);
 
 		final Device device = Model.getModel().getDevice(macAddress);
 		final String oldID = device.getID();
 		if (device != null)
 		{
 			//device.setDeviceName(nameString, new Date().getTime());
 		}
 
 		Model.getModel().deviceUpdated(oldID, device);
 
 		final Connection connection = ModelController.createRPCConnection();
		final String query = String.format("SQL:INSERT into Leases values (\"%s\", \"%s\", \"%s\", \"ADD\") on duplicate key update", device.getMacAddress(), device.getIPAddress(), nameString);	
 								
 		logger.info(query);
 		final String result = connection.call(query);
 		logger.info(result);
 		connection.disconnect();
 
 		ModelController.updateModel();
 		
 		final String sinceString = request.getParameter("since");
 		long since = new Date().getTime() - Model.getTimeout();
 		if (sinceString != null)
 		{
 			since = (long) Double.parseDouble(sinceString);
 		}
 
 		Log.log("Rename Device", macAddress);
 
 		ModelController.listItems(response.getWriter(), since);
 	}
 }
