 package org.owasp.proxy.daemon;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.URISyntaxException;
 import java.security.GeneralSecurityException;
 import java.util.logging.Logger;
 
 import org.owasp.httpclient.MessageFormatException;
 import org.owasp.httpclient.MutableRequestHeader;
 import org.owasp.httpclient.RequestHeader;
 import org.owasp.httpclient.StreamingRequest;
 import org.owasp.httpclient.StreamingResponse;
 import org.owasp.httpclient.io.ChunkedInputStream;
 import org.owasp.httpclient.io.CopyInputStream;
 import org.owasp.httpclient.io.EofNotifyingInputStream;
 import org.owasp.httpclient.io.FixedLengthInputStream;
 import org.owasp.httpclient.util.AsciiString;
 import org.owasp.proxy.model.URI;
 
 public class HttpProxyConnectionHandler implements ConnectionHandler,
 		TargetedConnectionHandler, EncryptedConnectionHandler {
 
 	private final static byte[] NO_CONNECT_HEADER = AsciiString
 			.getBytes("HTTP/1.0 503 Service unavailable"
 					+ " - CONNECT not supported\r\n\r\n");
 
 	private final static byte[] NO_CONNECT_MESSAGE = AsciiString
 			.getBytes("There is no CONNECT handler available");
 
 	private final static byte[] ERROR_HEADER = AsciiString
 			.getBytes("HTTP/1.0 500 OWASP Proxy Error\r\n"
 					+ "Content-Type: text/html\r\nConnection: close\r\n\r\n");
 
 	private final static String ERROR_MESSAGE1 = "<html><head><title>OWASP Proxy Error</title></head>"
 			+ "<body><h1>OWASP Proxy Error</h1>"
 			+ "OWASP Proxy encountered an error fetching the following request : <br/><pre>";
 
 	private final static String ERROR_MESSAGE2 = "</pre><br/>The error was: <br/><pre>";
 
 	private final static String ERROR_MESSAGE3 = "</pre></body></html>";
 
 	private final static Logger logger = Logger
 			.getLogger(HttpProxyConnectionHandler.class.toString());
 
 	private HttpRequestHandler requestHandler;
 
 	private TargetedConnectionHandler connectHandler = null;
 
 	public HttpProxyConnectionHandler(HttpRequestHandler requestHandler) {
 		this.requestHandler = requestHandler;
 	}
 
 	public void setConnectHandler(TargetedConnectionHandler connectHandler) {
 		this.connectHandler = connectHandler;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.owasp.proxy.daemon.ConnectionHandler#handleConnection(java.net.Socket
 	 * )
 	 */
 	public void handleConnection(Socket socket) throws IOException {
 		handleConnection(socket, null);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.owasp.proxy.daemon.ifbased.ConnectionHandler#handleConnection(java
 	 * .net.Socket, java.net.InetSocketAddress)
 	 */
 	public void handleConnection(Socket socket, InetSocketAddress target)
 			throws IOException {
 		handleConnection(socket, target, false);
 	}
 
 	protected StreamingResponse createErrorResponse(StreamingRequest request,
 			Exception e) throws IOException {
 		StringBuilder buff = new StringBuilder();
 		StreamingResponse response = new StreamingResponse.Impl();
 		response.setHeader(ERROR_HEADER);
 		buff.append(ERROR_MESSAGE1);
 		buff.append(AsciiString.create(request.getHeader()));
 		buff.append(ERROR_MESSAGE2);
 		StringWriter out = new StringWriter();
 		e.printStackTrace(new PrintWriter(out));
 		buff.append(out.getBuffer());
 		buff.append(ERROR_MESSAGE3);
 		response.setContent(new ByteArrayInputStream(AsciiString.getBytes(buff
 				.toString())));
 		return response;
 	}
 
 	private void doConnect(Socket socket, MutableRequestHeader request)
 			throws IOException, GeneralSecurityException,
 			MessageFormatException {
 		String resource = request.getResource();
 		int colon = resource.indexOf(':');
 		if (colon == -1)
 			throw new MessageFormatException("Malformed CONNECT line : '"
 					+ resource + "'", request.getHeader());
 		String host = resource.substring(0, colon);
 		if (host.length() == 0)
 			throw new MessageFormatException("Malformed CONNECT line : '"
 					+ resource + "'", request.getHeader());
 		int port;
 		try {
 			port = Integer.parseInt(resource.substring(colon + 1));
 		} catch (NumberFormatException nfe) {
 			throw new MessageFormatException("Malformed CONNECT line : '"
 					+ resource + "'", request.getHeader());
 		}
 		InetSocketAddress target = new InetSocketAddress(host, port);
 		OutputStream out = socket.getOutputStream();
 		if (connectHandler == null) {
 			out.write(NO_CONNECT_HEADER);
 			out.write(NO_CONNECT_MESSAGE);
 			out.flush();
 		} else {
 			out.write("HTTP/1.0 200 Ok\r\n\r\n".getBytes());
 			out.flush();
 			// start over from the beginning to handle this
 			// connection as an SSL connection
 			connectHandler.handleConnection(socket, target);
 		}
 	}
 
 	private StreamingRequest readRequest(InputStream in) throws IOException,
 			MessageFormatException {
 		logger.fine("Entering readRequest()");
 		// read the whole header.
 		ByteArrayOutputStream copy = new ByteArrayOutputStream();
 		CopyInputStream cis = new CopyInputStream(in, copy);
 		try {
 			String line;
 			do {
 				line = cis.readLine();
 			} while (line != null && !"".equals(line));
 		} catch (IOException e) {
 			byte[] headerBytes = copy.toByteArray();
 			if (headerBytes == null || headerBytes.length == 0)
 				return null;
 			throw new MessageFormatException("Incomplete request header", e,
 					headerBytes);
 		}
 
 		byte[] headerBytes = copy.toByteArray();
 
 		// empty request line, connection closed?
 		if (headerBytes == null || headerBytes.length == 0)
 			return null;
 
 		StreamingRequest request = new StreamingRequest.Impl();
 		request.setHeader(headerBytes);
 
		String transferCoding = request.getHeader("Transfer-Encoding");
 		String contentLength = request.getHeader("Content-Length");
 		if (transferCoding != null
 				&& transferCoding.trim().equalsIgnoreCase("chunked")) {
 			in = new ChunkedInputStream(in, true); // don't unchunk
 		} else if (contentLength != null) {
 			try {
 				in = new FixedLengthInputStream(in, Integer
 						.parseInt(contentLength));
 			} catch (NumberFormatException nfe) {
 				IOException ioe = new IOException(
 						"Invalid content-length header: " + contentLength);
 				ioe.initCause(nfe);
 				throw ioe;
 			}
 		} else {
 			in = null;
 		}
 
 		request.setContent(in);
 		return request;
 	}
 
 	private void extractTargetFromResource(MutableRequestHeader request)
 			throws MessageFormatException {
 		String resource = request.getResource();
 		try {
 			URI uri = new URI(resource);
 			request.setSsl("https".equals(uri.getScheme()));
 			int port = uri.getPort() > 0 ? uri.getPort()
 					: request.isSsl() ? 443 : 80;
 			request.setTarget(new InetSocketAddress(uri.getHost(), port));
 			request.setResource(uri.getResource());
 		} catch (URISyntaxException use) {
 			throw new MessageFormatException(
 					"Couldn't parse resource as a URI", use);
 		}
 	}
 
 	private void extractTargetFromHost(MutableRequestHeader request)
 			throws MessageFormatException {
 		String host = request.getHeader("Host");
 		int colon = host.indexOf(':');
 		if (colon > -1) {
 			try {
 				String h = host.substring(0, colon);
 				int port = Integer.parseInt(host.substring(colon + 1).trim());
 				request.setTarget(new InetSocketAddress(h, port));
 			} catch (NumberFormatException nfe) {
 				throw new MessageFormatException(
 						"Couldn't parse target port from Host: header", nfe);
 			}
 		} else {
 			int port = request.isSsl() ? 443 : 80;
 			request.setTarget(new InetSocketAddress(host, port));
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.owasp.proxy.daemon.ifbased.EncryptedConnectionHandler#handleConnection
 	 * (java.net.Socket, java.net.InetSocketAddress, boolean)
 	 */
 	public void handleConnection(Socket socket, InetSocketAddress target,
 			boolean ssl) throws IOException {
 		try {
 			InetAddress source = socket.getInetAddress();
 
 			InputStream in = socket.getInputStream();
 			OutputStream out = socket.getOutputStream();
 
 			boolean close;
 			String version = null, connection = null;
 			final StateHolder holder = new StateHolder();
 			do {
 				if (!holder.state.equals(State.READY))
 					throw new IllegalStateException(
 							"Trying to read a new request in state "
 									+ holder.state);
 				StreamingRequest request = null;
 				try {
 					request = readRequest(in);
 					holder.state = State.REQUEST_HEADER;
 				} catch (IOException ioe) {
 					logger.info("Error reading request: " + ioe.getMessage());
 					return;
 				}
 				if (request == null)
 					return;
 
 				if ("CONNECT".equals(request.getMethod())) {
 					doConnect(socket, request);
 					return;
 				} else if (!request.getResource().startsWith("/")) {
 					extractTargetFromResource(request);
 				} else if (target != null) {
 					request.setTarget(target);
 					request.setSsl(ssl);
 				} else if (request.getHeader("Host") != null) {
 					extractTargetFromHost(request);
 					request.setSsl(ssl);
 				} else {
 					throw new MessageFormatException(
 							"No idea where this request is going!", request
 									.getHeader());
 				}
 
 				InputStream requestContent = request.getContent();
 				if (requestContent != null) {
 					request.setContent(new EofNotifyingInputStream(
 							requestContent) {
 						@Override
 						public void eof() {
 							// all request content has been read
 							holder.state = State.REQUEST_CONTENT;
 						}
 					});
 				} else {
 					// nonexistent content has been read :-)
 					holder.state = State.REQUEST_CONTENT;
 				}
 				request.setContent(requestContent);
 
 				StreamingResponse response = null;
 				if (isExpectContinue(request)) {
 					System.err
 							.println("Expecting a Continue response for "
 									+ request.getTarget() + " "
 									+ request.getResource());
 					StreamingRequest cont = new StreamingRequest.Impl(request);
 					try {
 						response = requestHandler.handleRequest(source, cont,
 								false);
 						holder.state = State.RESPONSE_HEADER;
 					} catch (IOException ioe) {
 						response = createErrorResponse(cont, ioe);
 					}
 					if ("100".equals(response.getStatus())) {
 						try {
 							out.write(response.getHeader());
 						} catch (IOException ioe) { // client gone
 							return;
 						}
 					}
 				}
 
 				boolean isContinue = response != null
 						&& "100".equals(response.getStatus());
 
 				if (response == null || isContinue) {
 					try {
 						response = requestHandler.handleRequest(source,
 								request, isContinue);
 						holder.state = State.RESPONSE_HEADER;
 					} catch (IOException ioe) {
 						response = createErrorResponse(request, ioe);
 					}
 				}
 
 				try {
 					out.write(response.getHeader());
 				} catch (IOException ioe) { // client gone
 					return;
 				}
 				InputStream content = response.getContent();
 				if (content != null) {
 					int count = 0;
 					try {
 						byte[] buff = new byte[4096];
 						int got;
 						while ((got = content.read(buff)) > -1) {
 							try {
 								out.write(buff, 0, got);
 								count += got;
 							} catch (IOException ioe) { // client gone
 								content.close();
 								return;
 							}
 						}
 						out.flush();
 					} catch (IOException ioe) { // server closed
 						logger.fine("Request was " + request);
 						logger.fine("Incomplete response content because "
 								+ ioe.getMessage());
 						logger.fine("Read " + count + " bytes");
 						throw ioe;
 					}
 				}
 				holder.state = State.READY;
 				version = response.getVersion();
 				connection = response.getHeader("Connection");
 
 				if ("HTTP/1.1".equals(version)) {
 					close = false;
 				} else {
 					close = true;
 				}
 				if ("close".equals(connection)) {
 					close = true;
 				} else if ("Keep-Alive".equalsIgnoreCase(connection)) {
 					close = false;
 				}
 			} while (!close);
 		} catch (GeneralSecurityException gse) {
 			logger.severe(gse.getMessage());
 		} catch (IOException ioe) {
 			logger.info(ioe.getMessage());
 		} catch (MessageFormatException mfe) {
 			logger.info(mfe.getMessage());
 			mfe.printStackTrace();
 			logger.info("Header was " + new String(mfe.getHeader()));
 			logger.info("Target was " + target);
 		} finally {
 			try {
 				requestHandler.dispose();
 			} catch (IOException ioe) {
 				logger.warning("Error disposing of requestHandler resources: "
 						+ ioe.getMessage());
 			}
 		}
 
 	}
 
 	private boolean isExpectContinue(RequestHeader request)
 			throws MessageFormatException {
 		return "100-continue".equalsIgnoreCase(request.getHeader("Expect"));
 	}
 
 	private static class StateHolder {
 		public State state = State.READY;
 	}
 
 	private enum State {
 		READY, REQUEST_HEADER, REQUEST_CONTENT, RESPONSE_HEADER, RESPONSE_CONTENT
 	}
 
 }
