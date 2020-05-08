 package ar.edu.it.itba.pdc.proxy.implementations.proxy;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.Socket;
 import java.nio.ByteBuffer;
 
 import org.apache.log4j.Logger;
 
 import ar.edu.it.itba.pdc.proxy.implementations.configurator.interfaces.Configurator;
 import ar.edu.it.itba.pdc.proxy.implementations.monitor.implementations.Monitor;
 import ar.edu.it.itba.pdc.proxy.interfaces.Analyzer;
 import ar.edu.it.itba.pdc.proxy.interfaces.ConnectionManager;
 import ar.edu.it.itba.pdc.proxy.interfaces.Decoder;
 
 public class Attend implements Runnable {
 
 	private Socket socket;
 	private Analyzer analyzer;
 	private Monitor monitor;
 	private Decoder decoder;
 	private Logger attend;
 
 	public Attend(Socket socket, ConnectionManager connectionManager,
 			Configurator configurator, Monitor monitor) {
 		this.socket = socket;
 		this.analyzer = new AnalyzerImp(connectionManager, configurator,
 				monitor);
 		this.monitor = monitor;
 		this.decoder = new DecoderImpl(configurator);
 		this.attend = Logger.getLogger(this.getClass());
 	}
 
 	public void run() {
 		byte[] buffer = new byte[500];
 		ByteBuffer req = ByteBuffer.allocate(20 * 1024);
 		while (!socket.isClosed()) {
 			try {
 				int receivedMsg = 0, totalCount = 0;
 
 				InputStream clientIs = socket.getInputStream();
 
 				boolean keepReading = true;
 
 				// Reads until headers are complete
 				attend.debug("Before reading headers from client");
 				attend.info("Reading headers from client");
 				while (keepReading
 						&& ((receivedMsg = clientIs.read(buffer)) != -1)) {
 					totalCount += receivedMsg;
 					req.put(buffer, 0, receivedMsg);
 					keepReading = !decoder.completeHeaders(req.array(),
 							req.array().length);
 				}
 				if (receivedMsg == -1) {
 					attend.info("Received -1 from client. Closing connection");
 					socket.close();
 				}
 
 				attend.debug("Headers completely read. Sending to analyzer");
 				monitor.getDataStorage().addClientProxyBytes(totalCount);
 
 				analyzer.analyze(req, totalCount, socket);
 
				if (!socket.isConnected() || socket.isClosed()
						|| !analyzer.keepConnection()) {
					attend.info("Analyzer returned. Closing socket");
 					req.clear();
 					socket.close();
				}
 
 			} catch (IOException e) {
 				return;
 			}
 		}
 	}
 }
