 package eu.visioncloud.workflowWebServer;
 
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
import org.eclipse.jetty.server.Connector;
 import org.eclipse.jetty.server.Handler;
 import org.eclipse.jetty.server.Request;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.handler.AbstractHandler;
 import org.eclipse.jetty.server.handler.HandlerList;
 import org.eclipse.jetty.server.nio.SelectChannelConnector;
 import org.eclipse.jetty.util.thread.QueuedThreadPool;
 import org.eclipse.jetty.webapp.WebAppContext;
 
 import eu.visioncloud.workflow.constants.WorkflowMngConst;
 
 public class EmbeddedJettyServer {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		EmbeddedJettyServer emServer = new EmbeddedJettyServer();
 		emServer.setup(WorkflowMngConst.ip,
 				Integer.parseInt(WorkflowMngConst.port),
 				Integer.parseInt(WorkflowMngConst.threads));
 		try {
 			emServer.run();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		PropertyConfigurator.configure(EmbeddedJettyServer.class
 				.getResource("/log4j.properties"));
 	}
 
 	Server server;
 	private static final Logger logger = Logger.getLogger("JettyServer");
 
 	public void setup(String ip, int port, int threadNum) {
 		server = new Server();
 		server.setGracefulShutdown(1000);
 		server.setStopAtShutdown(true);
 		WebAppContext webAppContext = new WebAppContext();
 		webAppContext.setContextPath("/workflowmanager/manage");
 		//webAppContext.setDescriptor("src/main/webapp/WEB-INF/web.xml");//for test
 		//webAppContext.setResourceBase("src/main/webapp");
 		webAppContext.setDescriptor("webapp/WEB-INF/web.xml");// for deploy
 		webAppContext.setResourceBase("webapp");
 
 		webAppContext.setParentLoaderPriority(true);
 		// server.setHandler(webAppContext);
 
 		SelectChannelConnector connector1 = new SelectChannelConnector();
 		connector1.setHost(ip);
 		connector1.setPort(port);
 		connector1.setThreadPool(new QueuedThreadPool(threadNum));
		SelectChannelConnector connector2 = new SelectChannelConnector();
		connector2.setHost("localhost");
		connector2.setPort(port);
		connector2.setThreadPool(new QueuedThreadPool(threadNum));
		server.setConnectors(new Connector[] {connector1, connector2});
 		StopHandler stopHandler = new StopHandler();
 
 		HandlerList handlers = new HandlerList();
 		handlers.setHandlers(new Handler[] { stopHandler, webAppContext });
 		server.setHandler(handlers);
 	}
 
 	public void run() throws Exception {
 		server.start();
 		// server.join();
 		logger.info("Jetty Server Started");
 	}
 
 	class StopHandler extends AbstractHandler {
 
 		@Override
 		public void handle(String target, Request request,
 				HttpServletRequest httpServletRequest,
 				HttpServletResponse response) throws IOException,
 				ServletException {
 			// TODO Auto-generated method stub
			if (target.equals("/workflowmanager/stop")&&!request.getLocalAddr().equals(WorkflowMngConst.ip)) {
 				response.getWriter().println("Shut Down");
 				response.flushBuffer();
 				new Thread() {
 					public void run() {
 						try {
 
 							server.stop();
 							logger.info("Jetty Server Stopped");
 						} catch (Exception ex) {
 							ex.printStackTrace();
 						}
 					}
 				}.start();
 
 			} else if (target.equals("/workflowmanager/log")) {
 				response.reset();
 				copyFileToOutstream("workflowmanager",
 						response.getOutputStream());
 				response.flushBuffer();
 			}
 		}
 
 		private void copyFileToOutstream(String logName, OutputStream out)
 				throws IOException {
 			BufferedInputStream is = new BufferedInputStream(
 					new FileInputStream(WorkflowMngConst.logFilePath
 							.substring(1) + logName + ".log"));
 			byte[] buf = new byte[4 * 1024]; // 4K buffer
 			int bytesRead;
 			while ((bytesRead = is.read(buf)) != -1) {
 				out.write(buf, 0, bytesRead);
 			}
 			is.close();
 			out.flush();
 			out.close();
 		}
 	}
 }
