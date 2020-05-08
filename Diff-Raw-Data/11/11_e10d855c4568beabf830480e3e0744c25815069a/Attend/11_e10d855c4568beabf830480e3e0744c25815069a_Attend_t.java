 package ar.edu.it.itba.pdc.v2.implementations.proxy;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.Socket;
 import java.nio.ByteBuffer;
 
 import ar.edu.it.itba.pdc.v2.implementations.utils.DecoderImpl;
 import ar.edu.it.itba.pdc.v2.interfaces.Analyzer;
 import ar.edu.it.itba.pdc.v2.interfaces.ConnectionHandler;
 import ar.edu.it.itba.pdc.v2.interfaces.ConnectionManager;
 import ar.edu.it.itba.pdc.v2.interfaces.Decoder;
 
 public class Attend implements Runnable {
 
 	private Socket socket;
 	private ConnectionHandler handler;
 	private ConnectionManager connectionManager;
 	private Analyzer analyzer;
 
 	public Attend(Socket socket, ConnectionHandler handler,
 			ConnectionManager connectionManager, Analyzer analyzer) {
 		this.handler = handler;
 		this.socket = socket;
 		this.connectionManager = connectionManager;
 		this.analyzer = analyzer;
 	}
 
 	public void run() {
 
 		Decoder decoder = new DecoderImpl(20 * 1024);
 		byte[] buffer = new byte[500];
 		ByteBuffer req = ByteBuffer.allocate(20 * 1024);
 		InputStream response;
 		String s = socket.getRemoteSocketAddress().toString();
 		System.out.printf("Se conecto %s\n", s);
 		while (!socket.isClosed()) {
 			try {
 				int receivedMsg = 0, totalCount = 0;
 
 				InputStream clientIs = socket.getInputStream();
 				OutputStream clientOs = socket.getOutputStream();
 
 				boolean keepReading = true;
 
 				// read until headers are complete
 				while (keepReading
 						&& ((receivedMsg = clientIs.read(buffer)) != -1)) {
 					totalCount += receivedMsg;
 					req.put(buffer);
 					keepReading = !decoder.completeHeaders(req.array(),
 							req.array().length);
 				}
 
 				response = analyzer.analyze(req, totalCount, clientIs);
 				req.clear();
 				totalCount = 0;
 
 				try {
 					while (((receivedMsg = response.read(buffer)) != -1)) {
 						totalCount += receivedMsg;
//						System.out.print(new String(buffer));
 						clientOs.write(buffer);
						Thread.sleep(10);
 					}
 				} catch (IOException e) {
 					response.close();
 					System.out.println(e.getMessage());
					socket.close();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
 				}
 
 				if (receivedMsg == -1) {
 					socket.close();
 				}
 
 			} catch (IOException e) {
 			}
 		}
 
 		System.out.printf("Se desconecto %s\n", s);
 	}
 }
