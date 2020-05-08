 package edu.cmu.ui;
 
 import org.apache.commons.lang3.SystemUtils;
 import org.slf4j.Logger;
 
 import edu.cmu.controller.AbstractActionHandler;
 import edu.cmu.controller.GenericServlet;
 import edu.cmu.controller.LinuxActionHandler;
 import edu.cmu.controller.WindowsActionHandler;
 import edu.cmu.logger.EmulatorLogger;
 import edu.cmu.server.JettyServer;
 
 /**
  * This action handler handles actions invoked from 
  * the ControllerUI and talks to the JettyServer to perform
  * various actions. <br>
  * It also exposes server status and ports to the UI.
  * @author ziw
  *
  */
 public class UIActionHandler{
 
 	private JettyServer js;
 	private AbstractActionHandler handler;
 	private GenericServlet servlet;
 	private Logger logger = EmulatorLogger.getServerLogger();
 
 	
 	public UIActionHandler() throws Exception{
 		this(JettyServer.DEFAULT_PORT);
 	}
 	
 	public UIActionHandler(int port) throws Exception{
 		if(SystemUtils.IS_OS_WINDOWS){
 			handler = new WindowsActionHandler();
 		}
 		else if(SystemUtils.IS_OS_LINUX){
 			handler = new LinuxActionHandler();
 		}
 		else{
 			logger.error("Invalid OS. Emulator only supports Windows/Linux.");
 			Exception e = new Exception("Invalid OS. Emulator only supports Windows/Linux.");
 			EmulatorLogger.logException(logger, e);
 			throw e;
 		}
 		servlet = new GenericServlet(handler);
 		js = new JettyServer(port,servlet);
 	}
 	
 	public void startServer() throws Exception{
 		js.startServer();
 	}
 	
 	public void stopServer() throws Exception{
 		js.stopServer();
 	}
 
 	public int getServerStatus(){
 		return js.getServerStatus();
 	}
 
 	public boolean isServerRunning(){
 		return getServerStatus() == JettyServer.RUNNING;
 	}
 	
 	public int getPort() {
 		return js.getPort();
 	}
 
 	
 }
