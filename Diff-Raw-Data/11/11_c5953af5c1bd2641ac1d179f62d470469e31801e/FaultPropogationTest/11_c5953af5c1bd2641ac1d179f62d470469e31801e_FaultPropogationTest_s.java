 //B''H
 package org.ebayopensource.turmeric.runtime.tests.common.sif.error;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import junit.framework.Assert;
 
 import org.ebayopensource.turmeric.runtime.common.types.SOAConstants;
 import org.ebayopensource.turmeric.runtime.sif.service.Service;
 import org.ebayopensource.turmeric.runtime.sif.service.ServiceFactory;
 import org.ebayopensource.turmeric.runtime.sif.service.ServiceInvokerOptions;
 import org.junit.Test;
 
 public class FaultPropogationTest extends BaseErrorResponseTest {
 
 	private final String ECHO_STRING = "BH Test String";
 
 	private static Thread proxyThread;
 
 	private static boolean s_stop = false;
 	
 	static {
 		s_stop = false;
 		proxyThread = new Thread(new RunPoxyServer());
 		proxyThread.start();
 		try {
 			Thread.sleep(1000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	static class RunPoxyServer implements Runnable {
 
 		public void run() {
 			try {
 				runServer("localhost", 9889);
 			} catch (IOException e) {
 				e.printStackTrace();
 				System.out
 						.println("Unable to Start proxy, test run unreliable");
 			}
 		}
 
 	}
 
 	/**
 	 * runs a single-threaded proxy server on the specified local port. It never
 	 * returns.
 	 */
 	public static void runServer(String host, int localport) throws IOException {
 		// Create a ServerSocket to listen for connections with
 		ServerSocket ss = new ServerSocket(localport);
 
 		final byte[] request = new byte[1024];
 
 		while (!s_stop) {
 			Socket client = null;
 			try {
 				// Wait for a connection on the local port
 				client = ss.accept();
 
 				System.out.println("Proxy Engaged ....");
 
 				final InputStream streamFromClient = client.getInputStream();
 				final OutputStream streamToClient = client.getOutputStream();
 
 				// a thread to read the client's requests and pass them
 				// to the server. A separate thread for asynchronous.
 				Thread t = new Thread() {
 					public void run() {
 						try {
 							while (streamFromClient.read(request) != -1) {
 								// do nothing
 							}
 						} catch (IOException e) {
 						}
 					}
 				};
 
 				// Start the client-to-server request thread running
 				t.start();
 
 				// Read the server's responses
 				// and pass them back to the client.
 				try {
 					writeResponse(streamToClient, ISE, XML_MIME_STUFF,
 							SERVER_FAULT);
 					;
 					streamToClient.flush();
 				} catch (IOException e) {
 					// do nothing
 				}
 
 				Thread.sleep(200);
 				// The server closed its connection to us, so we close our
 				// connection to our client.
 				// streamToClient.close();
 			} catch (IOException e) {
 				System.err.println(e);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} finally {
 				try {
 					if (client != null) {
 						client.close();
 					}
 				} catch (IOException e) {
 				}
 			}
 		}
 		try {
 			ss.close();
 		} catch (IOException e) {
 		}
 
 	}
 
 	@Test
 	@SuppressWarnings("unchecked")
 	public void verifyIfSoapFaultIsException() throws Exception {
 		Service service = ServiceFactory.create("Test1Service",
 				"alwayThrowsSOAPFault", null);
 		try {
 			ServiceInvokerOptions options = service.getInvokerOptions();
 			options.setMessageProtocolName(SOAConstants.MSG_PROTOCOL_SOAP_11);
 			String outMessage = (String) service.createDispatch("echoString")
 					.invoke(ECHO_STRING);
 			System.out.println(outMessage);
 		} catch (Exception e) {
 			e.printStackTrace();
 			Assert
 					.assertTrue(e
 							.getCause()
 							.getCause()
 							.getMessage()
 							.contains(
 									"A server error occured while processing the request"));
 		}
 	}
 	
 
 }
