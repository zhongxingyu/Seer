 package com.lckymn.kevin.example.web;
 
import static com.lckymn.kevin.example.web.WebConstants.*;

 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * @author Kevin Lee
  * @version 0.0.1 (2012-09-10)
  */
 public final class WebUtil
 {
 	private WebUtil()
 	{
 	}
 
 	public static void forwardTo(final String destination, final HttpServletRequest request,
 			final HttpServletResponse response) throws ServletException, IOException
 	{
 		request.getRequestDispatcher(destination)
 				.forward(request, response);
 	}
 
 	public static void redirectTo(final String destination, final HttpServletResponse response) throws IOException
 	{
 		response.sendRedirect(destination);
 	}
 
 	public static boolean forwardToPrevious(final HttpServletRequest request, final HttpServletResponse response)
 			throws ServletException, IOException
 	{
 		final String forwardRequest = (String) request.getAttribute(FORWARD_REQUEST);
 		if (null == forwardRequest)
 		{
 			return false;
 		}
 		else
 		{
 			request.getRequestDispatcher(forwardRequest)
 					.forward(request, response);
 			return true;
 		}
 	}
 }
