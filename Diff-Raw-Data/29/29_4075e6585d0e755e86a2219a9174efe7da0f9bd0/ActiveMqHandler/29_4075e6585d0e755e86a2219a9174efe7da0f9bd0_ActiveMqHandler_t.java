 package com.irdeto.activemq; 
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 
 /**
  * Servlet that listens for notifications from ActiveMq
  */
 
 @WebServlet("/ActiveMqHandler")
 public class ActiveMqHandler extends HttpServlet{
 
 	@Override
 	public void init(ServletConfig config) throws ServletException {
 		ActiveMqConnection.getInstance().initialiseAMQ();
 	}
	
	@Override
	public void destroy() {
		ActiveMqConnection.getInstance().closeConnection();
		super.destroy();
	}
 }
