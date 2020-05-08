 package com.dierkers.schedule;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.eclipse.jetty.servlet.ServletHolder;
 
 public class HelloWorld extends HttpServlet {
 
 	private static final long serialVersionUID = 8172536904188802024L;
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 		resp.getWriter().print("Hello from Java!\n");
 	}
 
 	public static void main(String[] args) throws Exception {
		String port = System.getenv("PORT");
		Server server = new Server(port != null ? Integer.valueOf(port) : 5000);
 		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
 		context.setContextPath("/");
 		server.setHandler(context);
 		context.addServlet(new ServletHolder(new HelloWorld()), "/*");
 		server.start();
 		server.join();
 	}
 }
