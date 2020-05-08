 package ar.edu.itba.pdc.proxy;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.URL;
 import java.nio.ByteBuffer;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.SocketChannel;
 import java.nio.channels.UnresolvedAddressException;
 import java.util.HashMap;
 
 import ar.edu.itba.pdc.parser.HttpRequest;
 import ar.edu.itba.pdc.parser.Message;
 import ar.edu.itba.pdc.parser.enumerations.ParsingState;
 
 public class HttpSelectorProtocolClient implements TCPProtocol {
 
 	private HashMap<SocketChannel, ProxyConnection> proxyconnections = new HashMap<SocketChannel, ProxyConnection>();
 
 	public HttpSelectorProtocolClient(int bufSize) {
 	}
 
 	public void handleAccept(SocketChannel channel) throws IOException {
 		ProxyConnection conn = new ProxyConnection();
 		conn.setClient(channel);
 		proxyconnections.put(channel, conn);
 
 	}
 
 	public SocketChannel handleRead(SelectionKey key) throws IOException {
 		// Client socket channel has pending data
 		SocketChannel channel = (SocketChannel) key.channel();
 		ProxyConnection conn = proxyconnections.get(channel);
 		
 		ByteBuffer buf = conn.getBuffer(channel);
 		long bytesRead = 0;
 		try {
 			bytesRead = channel.read(buf);
 //			System.out.println(new String(buf.array(), 0, 100));
 		} catch (IOException e) {
 			System.out.println("\nfallo el read");
 			e.printStackTrace();
 			return null;
 		}
 		System.out.println("\n[READ] " + bytesRead + " from "
 				+ channel.socket().getInetAddress() + ":"
 				+ channel.socket().getPort());
 		if (bytesRead == -1) { // Did the other end close?
 			if (conn.isClient(channel)) {
 				System.out.println("\n[RECEIVED CLOSE] from cliente "
 						+ channel.socket().getInetAddress() + ":"
 						+ channel.socket().getPort());
 				if (conn.getServer() != null) {
 					
 					/* ----------------- CLOSE ----------------- */
 					
 					conn.getServer().close(); // close the server channel
 					
 					/* ----------------- CLOSE ----------------- */
 					System.out.println("\n[SENT CLOSE] to servidor remoto "
 							+ conn.getServer().socket().getInetAddress() + ":"
 							+ conn.getServer().socket().getPort());
 				}
 				
 				
 				/* ----------------- CLOSE ----------------- */
 				
 				channel.close();
 				
 				System.out.println("\n[SENT CLOSE] to cliente "
 						+ channel.socket().getInetAddress() + ":"
 						+ channel.socket().getPort());
 				
 				/* ----------------- REMOVE ----------------- */
 				
 				proxyconnections.remove(channel);
 				proxyconnections.remove(conn.getServer());
 				
 				/* ----------------- CANCEL ----------------- */
 				key.cancel();
 				
 				
 				// de-reference the proxy connection as it is
 									// no longer useful
 				return null;
 			} else {
 				System.out.println("\n[RECEIVED CLOSE] from servidor remoto "
 						+ channel.socket().getInetAddress() + ":"
 						+ channel.socket().getPort());
 				
 				/* ----------------- CLOSE ----------------- */
 				
 				conn.getServer().close();
 				
 				/* ----------------- CLOSE ----------------- */
 				
 				
 //				channel.close();
 				
 				System.out.println("\n[SENT CLOSE] to servidor remoto "
 						+ conn.getServer().socket().getInetAddress() + ":"
 						+ conn.getServer().socket().getPort());
 				// proxyconnections.remove(channel);
 //				key.cancel();
 //				channel.write(buf.rewind());
 				conn.resetIncompleteMessage();
 //				proxyconnections.remove(channel);
 				proxyconnections.remove(conn.getServer());
 				conn.resetServer();
 			}
 
 		} else if (bytesRead > 0) {
 			
 			/* ----------- PARSEO DE REQ/RESP ----------- */
 			
 			Message message = conn.getMessage(channel);
 			message.increaseAmountRead((int) bytesRead); // DECIDIR SI INT O
 			message.setFrom("client" + channel.socket().getInetAddress()); // LONG
 
 			/* ----------- CONEXION A SERVIDOR DESTINO ----------- */
 			SocketChannel serverchannel;
 			if (conn.isClient(channel) && message.getState() != ParsingState.Body) {
 				return null;
 			}
 			if ((serverchannel = conn.getServer()) == null) {
 				String url = null;
 				if (((HttpRequest) message).getURI().startsWith("/"))
 					url = ((HttpRequest) message).getHeaders().get("host")
 							+ ((HttpRequest) message).getURI();
 				else
 					url = ((HttpRequest) message).getURI();
 				String[] splitted = url.split("http://");
 				url = "http://"
 						+ (splitted.length >= 2 ? splitted[1] : splitted[0]);
 				URL uri = new URL(url);
 
 				serverchannel = SocketChannel.open();
 				serverchannel.configureBlocking(false);
 
 				int port = uri.getPort() == -1 ? 80 : uri.getPort();
 				try {
 					if (!serverchannel.connect(new InetSocketAddress(uri.getHost(),
 							port))) {
 	//					 if (!serverchannel.connect(new
 	//					 InetSocketAddress("localhost",
 	//					 8888))) {
 						while (!serverchannel.finishConnect()) {
 							Thread.sleep(30); // avoid massive polling
 							System.out.println("*");
 						}
 					}
 				} catch (UnresolvedAddressException e) { //TODO HACERLO DE LA FORMA BIEN. AGARRANDOLO DE UN ARCHIVO
 					String notFound = "HTTP/1.1 404 BAD REQUEST\r\n\r\n<html><body>404 Not Found<br><br>This may be a DNS problem or the page you were looking for doesn't exist.</body></html>\r\n";
 					ByteBuffer resp = ByteBuffer.allocate(notFound.length());
 					resp.put(notFound.getBytes());
 					resp.flip();
 					channel.write(resp);
 					channel.close();
 					proxyconnections.remove(channel);
 					return null;
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				conn.setServer(serverchannel);
 				proxyconnections.put(serverchannel, conn);
 			}
 			
 			// Indicate via key that reading/writing are both of interest now.
 			
 			key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
 			channel.register(key.selector(), SelectionKey.OP_WRITE);
			if (message.isFinished()) {
				System.out.println("finisheo");
			}
 			return serverchannel;
 		}
 
 		return null;
 	}
 
 	public void handleWrite(SelectionKey key) throws IOException {
 
 		SocketChannel channel = (SocketChannel) key.channel();
 		ProxyConnection conn = proxyconnections.get(channel);
 		SocketChannel receiver = conn.getOppositeChannel(channel);
 		ByteBuffer buf = conn.getBuffer(channel);
 		Message message = conn.getIncompleteMessage();
 		ByteBuffer pHeadersBuf;
 		int byteswritten = 0;
 		
 		if (message != null && (pHeadersBuf = message.getPartialHeadersBuffer()) != null) { // Headers came in different reads. 
 			pHeadersBuf.flip();
 			receiver.write(pHeadersBuf);
 			message.finishWithLeftHeaders();
 		}
 		
 		byteswritten = receiver.write(buf);
 		System.out.println("\n[WRITE] " + byteswritten + " to "
 				+ receiver.socket().getInetAddress() + ":"
 				+ receiver.socket().getPort());
 
 		buf.compact(); // Make room for more data to be read in
 		receiver.register(key.selector(), SelectionKey.OP_READ, conn); // receiver will write us back
 		key.interestOps(SelectionKey.OP_READ); // Sender has finished writing
 
 	}
 }
