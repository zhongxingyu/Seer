 package dk.tb.server;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.Iterator;
 
 import javax.enterprise.event.Observes;
 import javax.enterprise.inject.Instance;
 import javax.inject.Inject;
 import javax.inject.Singleton;
 
 import org.jboss.weld.environment.se.events.ContainerInitialized;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import dk.tb.factories.util.ServerThreadPool;
 import dk.tb.requesthandlers.Handler;
 import dk.tb.servlets.WebSocketServlet;
 
 @Singleton
 public class WebSocketServer {
 
 	@Inject Instance<Handler> handlerInstance;
 	@Inject ServerThreadPool threadPool;
	@Inject Instance<WebSocketServlet> servletsInstance;
 
 	Logger logger = LoggerFactory.getLogger(this.getClass());
 	
 	public void runServer(@Observes ContainerInitialized event) throws IOException {
 		logger.info("Starting server");
 		
 		Iterator<WebSocketServlet> it = servletsInstance.iterator();
 		while(it.hasNext()) {
 			logger.info(it.next().toString());
 		}
 
 		ServerSocket serverSocket = new ServerSocket(80);
 		//serverSocket.setReceiveBufferSize(255);
 		while(true) {
 			logger.info("Running server!");
 			Socket socket = serverSocket.accept();
 			socket.setSendBufferSize(1024000);
 			socket.setReceiveBufferSize(1024000);
 
 			System.out.println("Send buffer size: "+socket.getSendBufferSize());
 			System.out.println("Receive buffer size: "+socket.getReceiveBufferSize());
 			Handler requestHandler = handlerInstance.get();
 			requestHandler.handleRequest(socket);
 			threadPool.runTask(requestHandler);
 		}
 	}
 }
