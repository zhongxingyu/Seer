 package cz.cuni.mff.odcleanstore.wsclient;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.net.URI;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.security.KeyManagementException;
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 import java.security.cert.X509Certificate;
 
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLSocket;
 import javax.net.ssl.SSLSocketFactory;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.X509TrustManager;
 
 import cz.cuni.mff.odcleanstore.comlib.DummyOutputStream;
 import cz.cuni.mff.odcleanstore.comlib.SoapHttpReader;
 import cz.cuni.mff.odcleanstore.comlib.SoapHttpWriter;
 
 /**
  * Implements Insert method of odcs-inputclient SOAP webservice.
  * 
  * @author Petr Jerman
  */
 class Insert implements Runnable {
 	
 	private static final int RESPONSE_TIMEOUT = 120000;
 	
 	private URL serviceURL;
 	private Socket socket = null;
 	private SoapHttpWriter request = null;
 	private SoapHttpReader response = null;
 	
 	private boolean isExceptionDuringResponse;
 	private int responseID;
 	private String responseMessage;
 	private String responseMoreInfo;
 	
 	public Insert(URL serviceURL) {
 		this.serviceURL = serviceURL;
 	}
 	
 	/**
 	 * Send insert data message to odcs-inputclient SOAP webservice.
 	 * 
 	 * @param user odcs user for message
 	 * @param password odcs user password for message
 	 * @param metadata metadata asocciated with payload
 	 * @param payload payload in rdfxml or ttl format
 	 * @throws InsertException Exception returned from server or client
 	 */
 	public void Run(String user, String password, Metadata metadata, String payload) throws InsertException {
 		try {		
 			// compute payload size
 			DummyOutputStream  dos = new DummyOutputStream();
 			request = new SoapHttpWriter(dos);
 			writeSoapPayload(user, password, metadata, payload);
 			
 			// create connection
 			socket = createClientConnection();
 			request = new SoapHttpWriter(socket.getOutputStream());
 			response = new SoapHttpReader(socket.getInputStream());
 			
 			// start response thread
 			isExceptionDuringResponse = false;
 			responseID = 0;
 			Thread responseThread = new Thread(this);
 			responseThread.start();
 			
 			// send soap request to server
 			writeHttpRequestHeader(dos.getCount());
 			writeSoapPayload(user, password, metadata, payload);
			try { socket.shutdownOutput(); } catch(Exception e) {}
 			
 			// wait for response
 			responseThread.join(RESPONSE_TIMEOUT);
 			
 			//check response error
 			if (isExceptionDuringResponse) {
 				throw new InsertException(129, "Connection error", "Connection error");
 			}
 			if (responseID != 0) {
 				throw new InsertException(responseID, responseMessage, responseMoreInfo);
 			}
 			if (response.getResponseCode() < 200 || response.getResponseCode() >= 300 ) {
 				throw new InsertException(129, "Connection error", "Connection error");
 			}
 		} catch (InsertException e) {
 			throw e;
 		} catch (IOException e) {
 			throw new InsertException(129, "Connection error", "Connection error");
 		} catch (InterruptedException e) {
 			throw new InsertException(130, "Connection error", "Response timeout");
 		} finally {
 			if (request != null) request.closeQuietly();
 			if (response != null) response.closeQuietly();
 			try { if (socket != null) {	socket.close();	}} catch (IOException e) {}
 		}
 	}
 	
 	/**
 	 * Create socket for connection to the server.
 	 * 
 	 * @return Socket for connection
 	 * @throws InsertException
 	 */
 	private Socket createClientConnection() throws InsertException {
 		try {
 			if (serviceURL.getProtocol().equalsIgnoreCase("https")) {
 				return createSslClientConnection();
 			} else {
 				return new Socket(serviceURL.getHost(), serviceURL.getPort());
 			}
 		} catch (UnknownHostException e) {
 			throw new InsertException(128, "UnknownHost", "UnknownHost");
 		} catch (IOException e) {
 			throw new InsertException(129, "Connection error", "Connection error");
 		}
 	}
 	
 	/**
 	 * Create Ssl socket for connection to the server.
 	 * 
 	 * @return Socket for connection
 	 * @throws InsertException
 	 */
 	private Socket createSslClientConnection() throws InsertException {
 		try {
 				// Create a trust manager that does not validate certificate chains
 				TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
 					public X509Certificate[] getAcceptedIssuers() {
 						return new X509Certificate[0];
 					}
 
 					public void checkClientTrusted(X509Certificate[] certs, String authType) {
 					}
 
 					public void checkServerTrusted(X509Certificate[] certs, String authType) {
 					}
 				} };
 
 				SSLContext sc = SSLContext.getInstance("TLS");
 				sc.init(null, trustAllCerts, new SecureRandom());
 				SSLSocketFactory f = sc.getSocketFactory();
 
 				// Getting the default SSL socket factory
 				SSLSocket socket = (SSLSocket) f.createSocket(serviceURL.getHost(), serviceURL.getPort());
 				socket.startHandshake();
 				
 				return socket;
 		} catch (IOException e) {
 			throw new InsertException(129, "Connection error", "Connection error");
 		} catch (NoSuchAlgorithmException e) {
 			throw new InsertException(129, "Connection error", "Connection error");
 		} catch (KeyManagementException e) {
 			throw new InsertException(129, "Connection error", "Connection error");		}
 	}
 	
 	/**
 	 * Write HTTP POST header for SOAP message.
 	 * 
 	 * @param contentLength content-length of message body
 	 * @throws IOException
 	 */
 	private void writeHttpRequestHeader(long contentLength) throws IOException {
 		request.writeln(String.format("POST %s HTTP/1.0", serviceURL.toString()));
 		request.writeln("Content-Type: text/xml;charset=utf-8");
 		request.writeln(String.format("Content-Length: %d", contentLength));
 		request.writeln("Accept-Charset: utf-8");
 		request.writeln("SOAPAction: \"\"");
 		request.writeln();
 	}
 	
 	/**
 	 * Write SOAP message for odcs insert method.
 	 * 
 	 * @param user odcs user name
 	 * @param password odcs user password
 	 * @param metadata odcs metadata
 	 * @param payload odcs payload
 	 * @throws IOException
 	 */
 	private void writeSoapPayload(String user, String password, Metadata metadata, String payload) throws IOException {
 		request.write("<?xml version='1.0' encoding='UTF-8'?>" +
 				"<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"" +
 				" xmlns:i=\"http://inputws.engine.odcleanstore.mff.cuni.cz/\">" +
 				"<s:Header/><s:Body><i:insert>");
 
 		request.writeSimpleXMLElement("user", user);
 		request.writeSimpleXMLElement("password", password);
 		request.flush();
 		
 		request.write("<metadata>");
 		
 		request.writeSimpleXMLElement("uuid", metadata.getUuid());
 		for(URI uri:metadata.getPublishedBy()) {
 			request.writeSimpleXMLElement("publishedBy", uri);
 		}
 		for(URI uri:metadata.getSource()) {
 			request.writeSimpleXMLElement("source", uri);
 		}
 		for(URI uri:metadata.getLicense()) {
 			request.writeSimpleXMLElement("license", uri);
 		}
 		request.writeSimpleXMLElement("dataBaseUrl", metadata.getDataBaseUrl());
 		request.writeSimpleXMLElement("provenance", metadata.getProvenance());
 		request.writeSimpleXMLElement("pipelineName", metadata.getPipelineName());
 		
 		request.write("</metadata>");
 		request.flush();
 		
 		request.writeSimpleXMLElement("payload", payload);
 
 		request.write("</i:insert></s:Body></s:Envelope>");
 		request.flush();		
 	}
 
 	/**
 	 * Read SOAP response from server.
 	 * 
 	 * @see java.lang.Runnable#run()
 	 */
 	@Override
 	public void run() {
 		try {
 			response.readResponseHttpHeader();
 			
 			if (response.getResponseCode() == 500 && 
 					( response.getContentType().equalsIgnoreCase("text/xml") ||
 					  response.getContentType().equalsIgnoreCase("text/soap+xml"))) {
 
 				InsertResponseSoapFaultHandler handler = new InsertResponseSoapFaultHandler();
 				response.readSoapBody(handler);
 				
 				responseID = Integer.parseInt(handler.getId());
 				responseMessage = handler.getMessage();
 				responseMoreInfo = handler.getMoreInfo();
 			}
 			
 			if (response.getResponseCode() < 200 || response.getResponseCode() >= 300 ) {
 				try { socket.shutdownOutput(); } catch(Exception e) {}
 			}
 			try { socket.shutdownInput(); } catch(Exception e) {}
 		} catch (Throwable e) {
 			isExceptionDuringResponse = true;
 		}
 	}
 }
