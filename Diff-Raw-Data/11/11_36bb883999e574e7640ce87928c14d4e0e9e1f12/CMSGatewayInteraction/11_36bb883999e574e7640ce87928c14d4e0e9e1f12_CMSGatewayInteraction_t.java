 package com.sociodyne.validation.gateway;
 
import com.sociodyne.edi.Configuration;
import com.sociodyne.edi.parser.EdiXmlTransformer;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.management.NotCompliantMBeanException;
 import javax.net.SocketFactory;
 import javax.xml.transform.TransformerException;
 
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 
 public class CMSGatewayInteraction extends AbstractGatewayInteraction<OutputStream, InputStream>
     implements GatewayInteraction {
 
   public static final byte SOH = 1;
   public static final byte STX = 2;
   public static final byte ETX = 3;
 
   public static final String DEFAULT_HOSTNAME = "127.0.0.1";
   public static final int DEFAULT_PORT = 9000;
 
   private static Logger logger = Logger.getLogger(CMSGatewayInteraction.class.getName());
 
   private transient Socket socket;
 
   // MBean attributes
   private String hostname = DEFAULT_HOSTNAME;
   private int port = DEFAULT_PORT;
   
   // EDI configuration, as defined by the CMS companion guide.
   private static final Configuration configuration = new Configuration.Builder()
       .setSegmentTerminator('~')
       .setElementSeparator('*')
       .setSubElementSeparator('|')
       .build();
 
   @Override
   public void open(Map<String, String> properties) throws Exception {
     super.open(properties);
   }
 
   @Override
   public void close() {
     super.close();
     socket = null;
     hostname = DEFAULT_HOSTNAME;
     port = DEFAULT_PORT;
   }
 
   @Override
   protected void setUpConnection(Map<String, String> properties) throws GatewayConnectionException {
     synchronized (this) {
       if (properties.containsKey("hostname")) {
         hostname = properties.get("hostname");
       }
       if (properties.containsKey("port")) {
         port = Integer.parseInt(properties.get("port"));
       }
 
       // Lazy-create the socket; plus, reuse it as much as possible
       if (socket == null || !socket.isConnected()) {
         System.err.println("Connecting to " + hostname + ":" + port);
         try {
           socket = SocketFactory.getDefault().createSocket();
           // Wait 10 seconds to allow for the VPN to connect
           logger.fine("Connecting to CMS (" + hostname + ":" + port + ")");
           socket.connect(new InetSocketAddress(hostname, port), 20000);
           logger.fine("Connected to CMS (" + hostname + ":" + port + ")");
         } catch (final IOException e) {
           throw new GatewayConnectionException(e);
         }
       } else {
         logger.fine("Reusing CMS socket");
       }
     }
   }
 
   @Override
   protected void tearDownConnection() {
     if (socket != null) {
       try {
         socket.close();
       } catch (final IOException e) {
         logger.log(Level.SEVERE, e.getMessage(), e);
       }
     }
     socket = null;
   }
 
   @Override
   protected OutputStream getRequestContext() throws IOException {
     return socket.getOutputStream();
   }
 
   @Override
   protected InputStream getResponseContext() throws IOException {
     return socket.getInputStream();
   }
 
   @Override
   protected void sendRequest(OutputStream sos, Document document) throws IOException,
       GatewayInteractionException {
     try {
       final String edi = EdiXmlTransformer.xmlToEdi(document, configuration);
       final ByteArrayOutputStream os = new ByteArrayOutputStream();
       os.write(SOH);
       int length = edi.length();
       // There will always be at least 1 character (a '0' at least)
       int characters = 1;
       while (length > 10) {
         length /= 10;
         characters++;
       }
       for (int i = 0; i < 10 - characters; i++) {
         os.write('0');
       }
       os.write(Integer.toString(edi.length()).getBytes());
       os.write(STX);
       os.write(edi.getBytes());
       os.write(ETX);
       logger.fine("Sending request:\n" + new String(os.toByteArray()));
       sos.write(os.toByteArray());
 
       synchronized (CMSGatewayInteraction.this) {
         sent++;
       }
     } catch (final SAXException e) {
       throw new GatewayInteractionException(e);
     } catch (final TransformerException e) {
       throw new GatewayInteractionException(e);
     }
   }
 
   @Override
   protected Document receiveResponse(InputStream is) throws GatewayInteractionException,
       IOException {
     final byte[] marker = new byte[1];
     is.read(marker);
     if (marker[0] != SOH) {
       // Drain socket, then close
       final ByteArrayOutputStream baos = new ByteArrayOutputStream();
       while (is.read(marker) >= 0) {
         baos.write(marker);
       }
       logger.fine("Non-conforming response:\n" + new String(baos.toByteArray()));
       throw new GatewayProtocolException(
           "Response does not conform to communications protocol (SOH)");
     }
     final byte[] size = new byte[10];
     is.read(size);
     final int responseLength = Integer.parseInt(new String(size));
     is.read(marker);
     if (marker[0] != STX) {
       // Drain socket, then close
       final ByteArrayOutputStream baos = new ByteArrayOutputStream();
       while (is.read(marker) >= 0) {
         baos.write(marker);
       }
       logger.fine("Non-conforming response:\n" + new String(baos.toByteArray()));
       throw new GatewayProtocolException(
           "Response does not conform to communications protocol (STX)");
     }
     // TODO: Sanity check?
     final byte[] responseDataBytes = new byte[responseLength];
     {
       int c = 0;
       int n;
       while (c < responseLength && (n = is.read(responseDataBytes, c, responseLength - c)) >= 0) {
         c += n;
       }
     }
 
     is.read(marker);
     if (marker[0] != ETX) {
       // Drain socket, then close
       final ByteArrayOutputStream baos = new ByteArrayOutputStream();
       while (is.read(marker) >= 0) {
         baos.write(marker);
       }
       logger.fine("Non-conforming response:\n" + new String(baos.toByteArray()));
       throw new GatewayProtocolException(
           "Response does not conform to communications protocol (ETX)");
     }
 
     final String responseData = new String(responseDataBytes);
     logger.fine("Response data:\n" + responseData);
     if (responseData.startsWith("HETS")) {
       final String traceId = responseData.substring(4, 34);
 // String dateStamp = responseData.substring(34, 42);
 // String timeStamp = responseData.substring(42, 51);
 // String responseCode = responseData.substring(51, 53);
       final String errorCode = responseData.substring(53, 61);
       final String errorDescription = responseData.substring(61, responseData.length());
 
       throw new CMSException(traceId, errorCode, errorDescription);
     }
 
     try {
       return EdiXmlTransformer.ediToXml(responseData);
     } catch (SAXException e) {
       throw new GatewayInteractionException(e);
     } catch (TransformerException e) {
       throw new GatewayInteractionException(e);
     }
   }
 
   @Override
   protected Logger getLogger() {
     return logger;
   }
 
   public static interface MBeanInterface extends AbstractGatewayInteraction.MBeanInterface {
 
     String getHostname();
 
     void setHostname(String hostname);
 
     int getPort();
 
     void setPort(int port);
   }
 
   public class MBean extends AbstractGatewayInteraction<OutputStream, InputStream>.MBean implements
       MBeanInterface {
 
     public MBean() throws NotCompliantMBeanException {
       super(MBeanInterface.class);
     }
 
     public String getHostname() {
       return hostname;
     }
 
     public void setHostname(String hostname) {
       CMSGatewayInteraction.this.hostname = hostname;
     }
 
     public int getPort() {
       return port;
     }
 
     public void setPort(int port) {
       CMSGatewayInteraction.this.port = port;
     }
   }
 
   public static class CMSException extends GatewayInteractionException {
 
     private static final long serialVersionUID = -3521844096137083300L;
     String traceId;
     String errorCode;
 
     public CMSException(String traceId, String errorCode, String errorMessage) {
       super(errorMessage);
       this.traceId = traceId;
       this.errorCode = errorCode;
     }
 
     public String getCode() {
       return errorCode;
     }
 
     public String getTrace() {
       return traceId;
     }
   }
 }
