 package net.brainvitamins.presencepoll.server;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.TimeZone;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
/*
 * Notify the server of a timeout (this should only be done by an enqueued Task)
 */
 public class TimeoutServlet extends HttpServlet
 {
 	private static final long serialVersionUID = -1807657555459402441L;
 
 	// TODO: secure timeout URL (shouldn't be directly callable by the user)
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws IOException
 	{
 		String userId = req.getParameter("userId");
 		String userEmail = req.getParameter("userEmail");
 		String startTimeParameter = req.getParameter("startTime");
 		String timeoutParameter = req.getParameter("timeout");
 
 		if (userId == null)
 			throw new IllegalArgumentException(
 					"Parameter userId cannot be null.");
 		if (startTimeParameter == null)
 			throw new IllegalArgumentException(
 					"Parameter startTime cannot be null.");
 		if (timeoutParameter == null)
 			throw new IllegalArgumentException(
 					"Parameter timeout cannot be null.");
 
 		long timeout = Long.parseLong(timeoutParameter);
 
 		try
 		{
 			SimpleDateFormat format = new SimpleDateFormat(Constants.DATEFORMAT);
 			format.setTimeZone(TimeZone.getTimeZone("UTC"));
 			Date startTime = format.parse(startTimeParameter);
 
 			Constants.SERVICE.logActivity(new Timeout(new Date(), timeout,
 					startTime, userId, userEmail));
 		}
 		catch (ParseException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
