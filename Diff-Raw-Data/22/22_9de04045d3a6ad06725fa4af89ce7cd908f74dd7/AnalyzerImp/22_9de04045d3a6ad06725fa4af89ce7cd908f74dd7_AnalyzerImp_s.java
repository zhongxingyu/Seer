 package ar.edu.it.itba.pdc.v2.implementations.proxy;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.nio.BufferOverflowException;
 import java.nio.ByteBuffer;
 
 import org.apache.log4j.Logger;
 
 import ar.edu.it.itba.pdc.v2.implementations.RebuiltHeader;
 import ar.edu.it.itba.pdc.v2.implementations.monitor.Monitor;
 import ar.edu.it.itba.pdc.v2.implementations.utils.DecoderImpl;
 import ar.edu.it.itba.pdc.v2.interfaces.Analyzer;
 import ar.edu.it.itba.pdc.v2.interfaces.BlockAnalizer;
 import ar.edu.it.itba.pdc.v2.interfaces.Configurator;
 import ar.edu.it.itba.pdc.v2.interfaces.ConnectionManager;
 import ar.edu.it.itba.pdc.v2.interfaces.DataStorage;
 import ar.edu.it.itba.pdc.v2.interfaces.Decoder;
 import ar.edu.it.itba.pdc.v2.interfaces.HTTPHeaders;
 
 public class AnalyzerImp implements Analyzer {
 
 	private ConnectionManager connectionManager;
 	private static int BUFFSIZE = 5 * 1024;
 	private Configurator configurator;
 	private Logger analyzeLog;
 	private BlockAnalizer blockAnalizer;
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
 	private boolean keepConnection = true;
 	private DataStorage dataStorage;
 
 	public AnalyzerImp(ConnectionManager connectionManager,
 			Configurator configurator, Monitor monitor) {
 		this.connectionManager = connectionManager;
 		this.configurator = configurator;
 		this.dataStorage = monitor.getDataStorage();
 		this.analyzeLog = Logger.getLogger("proxy.server.attend.analyze");
 		this.blockAnalizer = new BlockAnalizerImpl(configurator);
 		this.decoder = new DecoderImpl(BUFFSIZE);
 		decoder.setConfigurator(configurator);
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
 		} catch (UnknownHostException e) {
 			try {
 				blockAnalizer.generateProxyResponse(clientOs, "400");
 			} catch (IOException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 		} catch (IOException e) {
 			try {
 				socket.close();
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			}
 			keepConnection = false;
 			return;
 		} catch (BufferOverflowException e) {
 			e.printStackTrace();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private boolean analizeRequest(ByteBuffer buffer, int count)
 			throws Exception {
 		try {
 			OutputStream clientOs = socket.getOutputStream(), externalOs;
 			InputStream clientIs = socket.getInputStream();
 
 			// Parse request headers
 			decoder.parseHeaders(buffer.array(), count);
 			requestHeaders = decoder.getHeaders();
 			String connection = requestHeaders.getHeader("Connection");
 			String proxyConnection = requestHeaders
 					.getHeader("Proxy-Connection");
 			String httpVersion = requestHeaders.getHeader("HTTPVersion");
 			if (connection == null && proxyConnection == null
 					&& httpVersion.contains("1.1")) {
 				keepConnection = true;
 			} else if (connection == null && proxyConnection == null
 					&& httpVersion.contains("1.0")) {
 				keepConnection = false;
 			} else if ((connection != null && connection.contains("close"))
 					|| (proxyConnection != null && proxyConnection
 							.contains("close"))) {
 				keepConnection = false;
 			} else {
 				keepConnection = true;
 			}
 			analyzeLog.info("Received headers from client "
 					+ socket.getInetAddress() + " :"
 					+ requestHeaders.dumpHeaders());
 
 			if (blockAnalizer.analizeRequest(decoder, clientOs)) {
 				analyzeLog.info("Block analyzer blocked request. Returning");
 				dataStorage.addBlock();
 				return false;
 			}
 
 			// Rebuilt the headers according to proxy rules and implementations
 			RebuiltHeader rh = decoder.rebuildHeaders();
 
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
 					// System.out.println("No deberia pasar");
 				}
 			} catch (UnknownHostException e) {
 				blockAnalizer.generateProxyResponse(clientOs, "400");
 				return false;
 			}
 			externalOs = externalServer.getOutputStream();
 
 			// Sends rebuilt header to server
 			analyzeLog.info("Sending rebuilt headers to server");
 			// System.out.println(new String(rh.getHeader()));
 			externalOs.write(rh.getHeader(), 0, rh.getSize());
 			// externalOs.write(buffer.array(), 0,
 			// requestHeaders.getReadBytes());
 
 			// If client sends something in the body..
 			if (requestHeaders.getReadBytes() < count) {
 				byte[] extra = decoder.getExtra(buffer.array(), count);
 				externalOs.write(extra, 0,
 						count - requestHeaders.getReadBytes());
 				decoder.analize(extra, count - requestHeaders.getReadBytes());
 			} else {
 				decoder.analize(buffer.array(), count);
 			}
 			// if client continues to send info, read it and send it to server
 			totalCount = 0;
 			while (decoder.keepReading()
 					&& ((receivedMsg = clientIs.read(buf)) != -1)) {
 				totalCount += receivedMsg;
 				analyzeLog.info("Reading upload data from client "
 						+ socket.getInetAddress());
 				decoder.decode(buf, receivedMsg);
 				externalOs.write(buf, 0, receivedMsg);
 			}
 			dataStorage.addClientProxyBytes(totalCount);
 		} catch (IOException e) {
 			if (externalServer != null)
 				connectionManager.releaseConnection(externalServer, false);
 			e.printStackTrace();
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
 		InputStream externalIs = externalServer.getInputStream();
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
 				blockAnalizer.generateProxyResponse(clientOs, "500");
 				connectionManager.releaseConnection(externalServer, false);
 				keepConnection = false;
 				return;
 			}
 			// Parse response heaaders
 			decoder.parseHeaders(resp.array(), totalCount);
 			responseHeaders = decoder.getHeaders();
 			String connection = responseHeaders.getHeader("Connection");
 			String httpVersion = responseHeaders.getHeader("HTTPVersion");
 			if ((connection == null && httpVersion.contains("1.1")) || (connection != null && connection.toUpperCase().contains("KEEP-ALIVE"))) {
 				externalSConnection = true;
 			} else if (connection.contains("close")) {
 				externalSConnection = false;
 			}
 
 			if (blockAnalizer.analizeResponse(decoder, clientOs)) {
 				dataStorage.addBlock();
 				analyzeLog
 						.info("Response blocked by proxy. Closing connection and returning");
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
 				// clientOs.write(resp.array(), 0,
 				// responseHeaders.getReadBytes());
 				clientOs.write(rh.getHeader(), 0, rh.getSize());
 			}
 
 			// Sends the rest of the body to client...
 			decoder.setConfigurator(configurator);
 			boolean data = false;
 			if (!decoder.contentExpected()) {
 				connectionManager.releaseConnection(externalServer, false);
 				keepConnection = false;
 				return;
 			}
 			if (responseHeaders.getReadBytes() < totalCount) {
 				byte[] extra = decoder.getExtra(resp.array(), totalCount);
 				decoder.analize(extra,
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
 			System.out.println("PASA" + keepReading);
 			while (keepReading && ((receivedMsg = externalIs.read(buf)) != -1)) {
 				System.out.println("ENTRA WHILE" + keepReading);
 				analyzeLog.info("Getting response from server");
 				totalCount += receivedMsg;
 				decoder.analize(buf, receivedMsg);
 				decoder.applyRestrictions(buf, receivedMsg, requestHeaders);
 				if (!applyTransform) {
 					clientOs.write(buf, 0, receivedMsg);
 				}
 				keepReading = decoder.keepReading();
 				data = true;
 			}
 			dataStorage.addProxyServerBytes(totalCount);
 			System.out.println("SALE WHILE" + keepReading);
 			analyzeLog.info("Response completed from server");
 			if (blockAnalizer.analizeChunkedSize(decoder, clientOs, totalCount)) {
 				analyzeLog
 						.info("Response blocked by proxy. Closing connection and returning");
 				dataStorage.addBlock();
 				return;
 			}
 			if (applyTransform && data) {
 				if (configurator.applyRotations() && decoder.isImage()) {
 
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
 					byte[] transformed = decoder.getTransformed();
 					clientOs.write(transformed, 0, transformed.length);
 					dataStorage.addTransformation();
 				}
 			}
 			connectionManager.releaseConnection(externalServer,
 					externalSConnection);
 			// System.out.println("TERMINO");
 		} catch (IOException e) {
 			connectionManager.releaseConnection(externalServer, false);
 			System.out.println(e.getMessage());
 		}
 
 	}
 
 	public boolean keepConnection() {
 		return false;
 	}
 }
