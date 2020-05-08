 
 package Server;
 
 
 import java.io.DataOutputStream;
 import java.io.IOException;
 
 import org.apache.log4j.Logger;
 
 
 
 
 
 
 public class Response
 {
 
 	private DataOutputStream outgoing;
 
 	private static Logger logger = Logger.getLogger(Response.class.getName());
 
 
 
 
 
 
 	public Response(DataOutputStream outgoing)
 	{
 
 		this.outgoing = outgoing;
 	}
 
 
 
 
 
 
 	public void sendResponse(int status, String responseString) {
 
 		String statusLine = null;
 
 		if (status == 200)
 			statusLine = "HTTP/1.1 200 OK" + "\r\n";
 		else if (status == 400)
 			statusLine = "HTTP/1.1 " + status + " Error" + "\r\n";
 		else
 			statusLine = "HTTP/1.1 " + status + " Nespravny parameter" + "\r\n";
 
 		try {
 			outgoing.writeBytes(statusLine);
 
			outgoing.writeBytes("Server: Java HTTPServer");
 			outgoing.writeBytes("Content-Type: application/json" + "\r\n");
 			outgoing.writeBytes("Content-Length: " + responseString.length() + "\r\n");
 			outgoing.writeBytes("Connection: close\r\n");
 
 			outgoing.writeBytes("\r\n");
 			outgoing.writeBytes(responseString);
 
 			outgoing.close();
 
 			logger.info("Server send response: Status " + status + ": \n" + responseString);
 		} catch (IOException e) {
 			logger.info(e.getMessage() + ":Problem with sending to plugin.");
 		}
 
 	}
 }
