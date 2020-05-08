 package ar.edu.it.itba.pdc.proxy.implementations.proxy;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.nio.ByteBuffer;
 
 import org.apache.log4j.Logger;
 
 import ar.edu.it.itba.pdc.proxy.implementations.configurator.interfaces.Configurator;
 import ar.edu.it.itba.pdc.proxy.implementations.monitor.implementations.Monitor;
 import ar.edu.it.itba.pdc.proxy.implementations.monitor.interfaces.DataStorage;
 import ar.edu.it.itba.pdc.proxy.implementations.utils.RebuiltHeader;
 import ar.edu.it.itba.pdc.proxy.interfaces.Analyzer;
 import ar.edu.it.itba.pdc.proxy.interfaces.BlockAnalyzer;
 import ar.edu.it.itba.pdc.proxy.interfaces.ConnectionManager;
 import ar.edu.it.itba.pdc.proxy.interfaces.Decoder;
 import ar.edu.it.itba.pdc.proxy.interfaces.HTTPHeaders;
 
 public class AnalyzerImp implements Analyzer {
 
 	private ConnectionManager connectionManager;
 	private static int BUFFSIZE = 5 * 1024;
 	private Configurator configurator;
 	private Logger analyzeLog;
 	private BlockAnalyzer blockAnalizer;
 	private Decoder decoder;
 	private Socket socket;
 	private boolean keepReading = false;
 	private int totalCount = 0;
 	private int receivedMsg = 0;
 	private HTTPHeaders requestHeaders;
 	private HTTPHeaders responseHeaders;
 	private byte[] buf = new byte[BUFFSIZE];
 	private Socket externalServer;
 	private OutputStream clientOs;
 	private OutputStream externalOs;
 	private InputStream clientIs;
 	private InputStream externalIs;
 	private boolean keepConnection = true;
 	private DataStorage dataStorage;
 	private boolean isHEADRequest = false;
 
 	public AnalyzerImp(ConnectionManager connectionManager,
 			Configurator configurator, Monitor monitor) {
 		this.connectionManager = connectionManager;
 		this.configurator = configurator;
 		this.dataStorage = monitor.getDataStorage();
 		this.analyzeLog = Logger.getLogger("proxy.server.attend.analyze");
 		this.decoder = new DecoderImpl(configurator);
 		this.blockAnalizer = new BlockAnalyzerImpl(configurator, decoder,
 				analyzeLog);
 	}
 
 	public void analyze(ByteBuffer buffer, int count, Socket socket) {
 		this.socket = socket;
 
 		try {
 			boolean continueResponse = analizeRequest(buffer, count);
 			if (continueResponse) {
 				analizeResponse();
 
 			}
 			totalCount = 0;
 			decoder.reset();
 			receivedMsg = 0;
 			keepReading = false;
 			requestHeaders = null;
 			responseHeaders = null;
 			buf = new byte[BUFFSIZE];
 			externalServer = null;
 			closeStreams();
 		} catch (UnknownHostException e) {
 			try {
 				decoder.generateProxyResponse(clientOs, "400");
 				closeStreams();
 			} catch (IOException e1) {
 			}
 		} catch (IOException e) {
 			try {
 				closeStreams();
 				socket.close();
 			} catch (IOException e1) {
 			}
 			keepConnection = false;
 			return;
 		} catch (Exception e) {
 			try {
 				decoder.generateProxyResponse(clientOs, "500");
 				closeStreams();
 			} catch (IOException e1) {
 			}
 		}
 	}
 
 	private boolean analizeRequest(ByteBuffer buffer, int count)
 			throws Exception {
 		try {
 			clientOs = socket.getOutputStream();
 			clientIs = socket.getInputStream();
 
 			// Parse request headers
 			if (!decoder.parseHeaders(buffer.array(), count, "request")) {
 				decoder.generateProxyResponse(clientOs, "501");
 				return false;
 			}
 
 			requestHeaders = decoder.getHeaders();
 			isHEADRequest = requestHeaders.isHEADRequest();
 
 			keepConnection = analyzeConnection();
 
 			analyzeLog.info("Received headers from client "
 					+ socket.getInetAddress() + " :"
 					+ requestHeaders.dumpHeaders());
 
 			// Analyze if something will be blocked
 			if (blockAnalizer.analyzeRequest(decoder, clientOs)) {
 				dataStorage.addBlock();
 				return false;
 			}
 
 			// Rebuilt the headers according to proxy rules and implementations
 			RebuiltHeader rh = decoder.rebuildRequestHeaders();
 
 			String host = decoder.getHeader("Host");
 			if (host == null) {
 				keepConnection = false;
 				return false;
 			} else {
 				host = host.replace(" ", "");
 			}
 
 			analyzeLog.info("Requesting for connection to: " + host);
 			try {
 				while ((externalServer = connectionManager.getConnection(host)) == null) {
 				}
 			} catch (UnknownHostException e) {
 				decoder.generateProxyResponse(clientOs, "400");
 				return false;
 			}
 			externalOs = externalServer.getOutputStream();
 
 			// Sends rebuilt header to server
 			analyzeLog.info("Sending rebuilt headers to server --- "
 					+ new String(rh.getHeader()));
 			externalOs.write(rh.getHeader(), 0, rh.getSize());
 
 			// If client sends something in the body..
 			if (requestHeaders.getReadBytes() < count) {
 				byte[] extra = decoder.getExtra(buffer.array(), count);
 				externalOs.write(extra, 0,
 						count - requestHeaders.getReadBytes());
 				decoder.analyze(extra, count - requestHeaders.getReadBytes());
 			} else {
 				decoder.analyze(buffer.array(), count);
 			}
 
 			// if client continues to send info, read it and send it to server
 			totalCount = 0;
 			while (decoder.keepReading()
 					&& ((receivedMsg = clientIs.read(buf)) != -1)) {
 				totalCount += receivedMsg;
 				analyzeLog.info("Reading upload data from client "
 						+ socket.getInetAddress());
 				decoder.analyze(buf, receivedMsg);
 				externalOs.write(buf, 0, receivedMsg);
 			}
 			dataStorage.addClientProxyBytes(totalCount);
 		} catch (IOException e) {
 			if (externalServer != null)
 				connectionManager.releaseConnection(externalServer, false);
 			return false;
 		}
 		return true;
 
 	}
 
 	private void analizeResponse() throws Exception {
 		boolean externalSConnection = true;
 		// Reads response from server and write it to client
 		decoder.reset();
 		keepReading = true;
 		totalCount = 0;
 		ByteBuffer resp = ByteBuffer.allocate(BUFFSIZE);
 		externalIs = externalServer.getInputStream();
 		clientOs = socket.getOutputStream();
 
 		try {
 			// Read headers
 			analyzeLog.info("Reading header from server");
 			while (keepReading && ((receivedMsg = externalIs.read(buf)) != -1)) {
 				totalCount += receivedMsg;
 				resp.put(buf, 0, receivedMsg);
 				keepReading = !decoder.completeHeaders(resp.array(),
 						resp.array().length);
 			}
 			if (totalCount == 0) {
 				decoder.generateProxyResponse(clientOs, "500");
 				connectionManager.releaseConnection(externalServer, false);
 				keepConnection = false;
 				return;
 			}
 			// Parse response heaaders
 			decoder.parseHeaders(resp.array(), totalCount, "response");
 			responseHeaders = decoder.getHeaders();
 
 			externalSConnection = analyzeResponseConnection();
 
 			if (blockAnalizer.analyzeResponse(decoder, clientOs)) {
 				dataStorage.addBlock();
 				return;
 			}
 			// Sends only headers to client
 			analyzeLog.info("Got response from "
 					+ requestHeaders.getHeader("Host").replace(" ", "")
 					+ " with status code "
 					+ responseHeaders.getHeader("StatusCode") + "||||||"
 					+ responseHeaders.dumpHeaders());
 
 			boolean applyTransform = decoder.applyTransformations();
 
 			RebuiltHeader rh = decoder.rebuildResponseHeaders();
 			if ((!configurator.applyRotations())
 					|| (configurator.applyRotations() && !applyTransform)) {
 				clientOs.write(rh.getHeader(), 0, rh.getSize());
 			}
 
 			// Sends the rest of the body to client...
 			boolean data = false;
 			if (!decoder.contentExpected()) {
 				connectionManager.releaseConnection(externalServer, false);
 				keepConnection = false;
 				return;
 			}
 			if (responseHeaders.getReadBytes() < totalCount) {
 				byte[] extra = decoder.getExtra(resp.array(), totalCount);
 				decoder.analyze(extra,
 						totalCount - responseHeaders.getReadBytes());
 				decoder.applyRestrictions(extra,
 						totalCount - responseHeaders.getReadBytes(),
 						requestHeaders);
 				if (!applyTransform) {
 					clientOs.write(extra, 0,
 							totalCount - responseHeaders.getReadBytes());
 				}
 				data = true;
 			}
 			resp.clear();
 			String length = responseHeaders.getHeader("Content-Length");
 			if (length != null) {
 				length = length.replaceAll(" ", "");
 				if (length.equals("0")) {
 					keepReading = false;
 				} else
 					keepReading = decoder.keepReading();
 			} else
 				keepReading = decoder.keepReading();
 			if (receivedMsg == -1) {
 				keepReading = false;
 			}
 			if (isHEADRequest)
 				keepReading = false;
 			while (keepReading && ((receivedMsg = externalIs.read(buf)) != -1)) {
 				analyzeLog.info("Getting response from server");
 				totalCount += receivedMsg;
 				decoder.analyze(buf, receivedMsg);
 				decoder.applyRestrictions(buf, receivedMsg, requestHeaders);
 				if (!applyTransform) {
 					clientOs.write(buf, 0, receivedMsg);
 				}
 				keepReading = decoder.keepReading();
 				data = true;
 			}
 			dataStorage.addProxyServerBytes(totalCount);
 			analyzeLog.info("Response completed from server");
 			if (blockAnalizer.analyzeChunkedSize(decoder, clientOs, totalCount)) {
 				dataStorage.addBlock();
 				return;
 			}
 			if (applyTransform && data) {
 				if (configurator.applyRotations() && decoder.isImage()) {
 					analyzeLog.info("Rotating image");
 					byte[] rotated = decoder.getRotatedImage();
 					if (rotated == null) {
 						connectionManager.releaseConnection(externalServer,
 								false);
 						keepConnection = false;
 						return;
 					}
 					RebuiltHeader newHeader = decoder
 							.modifiedContentLength(rotated.length);
 					clientOs.write(newHeader.getHeader(), 0,
 							newHeader.getSize());
 					clientOs.write(rotated, 0, rotated.length);
 					dataStorage.addTransformation();
 				}
 				if (configurator.applyTextTransformation() && decoder.isText()) {
 					analyzeLog.info("Transforming text/plain");
 					byte[] transformed = decoder.getTransformed();
 					clientOs.write(transformed, 0, transformed.length);
 					dataStorage.addTransformation();
 				}
 			}
 			connectionManager.releaseConnection(externalServer,
 					externalSConnection);
 		} catch (IOException e) {
 			connectionManager.releaseConnection(externalServer, false);
 			System.out.println(e.getMessage());
 		}
 
 	}
 
 	public boolean keepConnection() {
 		return keepConnection;
 	}
 
 	private void closeStreams() throws IOException {
 		if (clientIs != null)
 			clientIs.close();
 		if (clientOs != null)
 			clientOs.close();
 		if (externalIs != null)
 			externalIs.close();
 		if (externalOs != null)
 			externalOs.close();
 	}
 
 	private boolean analyzeConnection() {
 		String connection = requestHeaders.getHeader("Connection");
 		String proxyConnection = requestHeaders.getHeader("Proxy-Connection");
 		String httpVersion = requestHeaders.getHeader("HTTPVersion");
 		if (connection == null && proxyConnection == null
 				&& httpVersion.contains("1.1")) {
 			return true;
 		} else if (connection == null && proxyConnection == null
 				&& httpVersion.contains("1.0")) {
 			return false;
 		} else if ((connection != null && connection.contains("close"))
 				|| (proxyConnection != null && proxyConnection
 						.contains("close"))) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 
 	private boolean analyzeResponseConnection() {
 		String connection = responseHeaders.getHeader("Connection");
 		String httpVersion = responseHeaders.getHeader("HTTPVersion");
 		if ((connection == null && httpVersion.contains("1.1"))
 				|| (connection != null && connection.toUpperCase().contains(
 						"KEEP-ALIVE"))) {
 			return true;
 		} else if (connection == null && httpVersion.contains("1.1")) {
 			return true;
 		}
 		if (connection == null || connection.contains("close")) {
 			return false;
 		}
 		return true;
 	}
 
 }
