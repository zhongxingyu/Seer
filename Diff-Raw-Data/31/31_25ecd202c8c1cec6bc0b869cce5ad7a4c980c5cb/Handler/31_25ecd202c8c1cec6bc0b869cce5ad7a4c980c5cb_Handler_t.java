 package core;
 
 import java.io.IOException;
import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public abstract class Handler {
 	
 	private Logger logger = Logger.getLogger(Handler.class.getName());
 	
 	public abstract void serve(Action action, HttpServletRequest request, HttpServletResponse response);
 	
 	public void sendTextResponse(int statusCode, String message, HttpServletResponse response){
 		try {
 			response.setStatus(statusCode);
 			response.setContentType("text/plain");
 			response.getWriter().println(message);
 		} catch (IOException e) {
			logger.log(Level.SEVERE, "Could not send response: ", e);
 		}
 	}
 }
