 package ch.ethz.iks.r_osgi.http.acceptor;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 import java.util.HashMap;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import ch.ethz.iks.r_osgi.RemoteOSGiMessage;
 
 /**
  * 
  * @author Jan S. Rellermeyer, ETH Zurich
  */
 public class HttpAcceptorServlet extends HttpServlet {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	private static final int R_OSGi_PORT = 9278;
 
 	private static Socket socket;
 
 	private static HashMap bridges = new HashMap();
 
 	protected void service(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		System.out.println();
 		System.out.println("Servlet called");
 		super.service(req, resp);
 	}
 
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		// System.out.println("GOT GET REQUEST");
 		// Writer writer = resp.getWriter();
 		// writer.write("<h1>R-OSGi HTTP Channel Acceptor Servlet</h1>");
 		// resp.setStatus(HttpServletResponse.SC_OK);
 		doPost(req, resp);
 	}
 
 	/**
 	 * 
 	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
 	 *      javax.servlet.http.HttpServletResponse)
 	 */
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		System.out.println("-----------------------------------------------");
 		System.out.println("GOT POST REQUEST FROM " + req.getRemoteAddr());
 
 		final String host = req.getRemoteAddr();
 
 		ChannelBridge bridge = (ChannelBridge) bridges.get(host);
 		if (bridge == null) {
 			bridge = new ChannelBridge();
 			bridges.put(host, bridge);
 		}
 
 		bridge.forwardRequest(req, resp);
 	}
 
 	private static class ChannelBridge {
 		private final ObjectInputStream localIn;
 
 		private final ObjectOutputStream localOut;
 
		private HttpServletResponse baseResp;

 		private final HashMap waitMap = new HashMap();
 
 		private static final Object WAITING = new Object();
 
 		private ChannelBridge() throws IOException {
 			System.out.println("now opening local socket");
 			socket = new Socket("localhost", R_OSGi_PORT);
			localIn = new ObjectInputStream(new BufferedInputStream(socket
					.getInputStream()));
 
			localOut = new ObjectOutputStream(new BufferedOutputStream(socket
					.getOutputStream()));
 			localOut.flush();
			// start();
 		}
 
 		private void forwardRequest(HttpServletRequest req,
 				HttpServletResponse resp) throws IOException {
 			ObjectInputStream remoteIn = new ObjectInputStream(req
 					.getInputStream());
 
 			RemoteOSGiMessage msg = RemoteOSGiMessage.parse(remoteIn);
 			System.out.println("{REMOTE -> LOCAL}: " + msg);
 
 			final Integer xid = new Integer(msg.getXID());
 			synchronized (waitMap) {
 				waitMap.put(xid, WAITING);
 			}
 			msg.send(localOut);
 
 			if (msg.getFuncID() == RemoteOSGiMessage.LEASE) {
 				ObjectOutputStream baseOut = new ObjectOutputStream(
 						new ChunkedEncoderOutputStream(resp.getOutputStream()));
				baseResp = resp;
 				baseOut = new ObjectOutputStream(
 						new ChunkedEncoderOutputStream(resp.getOutputStream()));
 				resp.setHeader("Transfer-Encoding", "chunked");
 				resp.setContentType("multipart/x-r_osgi");
 				// intentionally, the request that carried the lease does not
 				// terminate (as long as the connection is open). It is used to
 				// ship remote events.
				while (!Thread.interrupted()) {
					try {
 						RemoteOSGiMessage response = RemoteOSGiMessage
 								.parse(localIn);
 						System.out.println("received " + response);
 						switch (response.getFuncID()) {
 						case RemoteOSGiMessage.LEASE:
 						case RemoteOSGiMessage.REMOTE_EVENT:
 							System.out.println("{LOCAL -> REMOTE (ASYNC)}: "
 									+ msg);
 
 							// deliver remote event as response of the lease
 							// request
 							response.send(baseOut);
 							baseOut.flush();
 							resp.flushBuffer();
 							continue;
 						default:
 							// put into wait queue
 							synchronized (waitMap) {
 								waitMap.put(new Integer(response.getXID()),
 										response);
 								waitMap.notifyAll();
 							}
 						}
					} catch (IOException ioe) {
						ioe.printStackTrace();
 					}
 				}
 			} else {
 
 				Object response = null;
 
 				try {
 					synchronized (waitMap) {
 						while (waitMap.get(xid) == WAITING) {
 							waitMap.wait();
 						}
 						response = waitMap.remove(xid);
 					}
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 
 				resp.setContentType("application/x-r_osgi");
 				ObjectOutputStream remoteOut = new ObjectOutputStream(resp
 						.getOutputStream());
 
 				System.out.println("{LOCAL -> REMOTE}: " + msg);
 				((RemoteOSGiMessage) response).send(remoteOut);
				remoteOut.flush();
 			}
 		}

 	}
 
 }
