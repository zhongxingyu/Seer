 //: "The contents of this file are subject to the Mozilla Public License
 //: Version 1.1 (the "License"); you may not use this file except in
 //: compliance with the License. You may obtain a copy of the License at
 //: http://www.mozilla.org/MPL/
 //:
 //: Software distributed under the License is distributed on an "AS IS"
 //: basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 //: License for the specific language governing rights and limitations
 //: under the License.
 //:
 //: The Original Code is Guanxi (http://www.guanxi.uhi.ac.uk).
 //:
 //: The Initial Developer of the Original Code is Alistair Young alistair@codebrane.com
 //: All Rights Reserved.
 //:
 
 package org.guanxi.common;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.ProtocolException;
 import java.net.URL;
 import java.security.cert.Certificate;
 import java.security.cert.CertificateException;
 import java.security.cert.CertificateFactory;
 import java.security.cert.X509Certificate;
 import java.util.ArrayList;
 
 import javax.net.ssl.HttpsURLConnection;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLPeerUnverifiedException;
 
 import org.apache.log4j.Logger;
 import org.apache.xml.security.utils.Base64;
 import org.guanxi.common.security.ssl.GuanxiHostVerifier;
 import org.guanxi.common.security.ssl.SSL;
 
 /**
  * Wraps either an HttpsURLConnection or HttpsURLConnection. The HttpsURLConnection
  * uses the org.guanxi.common.security.ssl.SSL defined custom SSL layer.
  *
  * @author Alistair Young alistair@smo.uhi.ac.uk
  * @author matthew
  */
 public class EntityConnection {
   private static final Logger logger = Logger.getLogger(EntityConnection.class.getName());
   /** 
    * For passing to EntityConnection when we want to probe a remote entity for it's X509 Certificate 
    */
   public static final boolean PROBING_ON = true;
   /** 
    * For passing to EntityConnection when we want full HTTPS functionality 
    */
   public static final boolean PROBING_OFF = false;
   /**
    * This indicates if the server certificate is being retrieved using this connection
    * (or if the connection is checking to ensure that the certificate is valid).
    */
   private boolean probing = false;
   /**
    * This is the HTTP(S) connection that has been made.
    * To determine if this is a secure connection call
    * connection instanceof HttpsURLConnection.
    */
   private HttpURLConnection connection = null;
 
   /**
    * Sets up a connection object with custom SSL management if required.
    *
    * @param endpoint               Address of the entity to connect to. If this starts with https
    *                               the custom SSL management will be used.
    * @param localEntityID          The ID of the local entity which will be represented by the
    *                               custom SSL layer. If the connection is not secure, this parameter can be null.
    * @param entityKeystore         The full path of the entity's keystore
    * @param entityKeystorePassword Password for the entity's keystore
    * @param trustStore             The full path to the Engine's truststore
    * @param trustStorePassword     The password for the Engine's truststore
    * @param probeForServerCert     TRUE if the connection is only going to be used to obtain an entity's SSL certificate
    * @throws GuanxiException       If an error occurred
    */
   public EntityConnection(String endpoint, String localEntityID, String entityKeystore, String entityKeystorePassword,
                           String trustStore, String trustStorePassword, boolean probeForServerCert) throws GuanxiException {
     URL url;
     
     try {
       url = new URL(endpoint);
       
       if ( url.getProtocol().equals("https") ) { // getProtocol always returns the lower case protocol name
         HttpsURLConnection connection;
         SSLContext context;
         
         context = SSLContext.getInstance("SSL");
         context.init(SSL.getKeyManagers(localEntityID, entityKeystore, entityKeystorePassword),
                      SSL.getTrustManagers(trustStore, trustStorePassword, probeForServerCert),
                      null);
         connection = (HttpsURLConnection)url.openConnection();
         connection.setSSLSocketFactory(context.getSocketFactory());
         connection.setHostnameVerifier(new GuanxiHostVerifier());
         
         this.connection = connection;
       }
       else {
         connection = (HttpURLConnection)url.openConnection();
       }
       probing = probeForServerCert;
     }
     catch (Exception e) {
       throw new GuanxiException(e);
     }
   }
   
   /**
    * Set the method for the URL request, one of:
    * GET
    * POST
    * HEAD
    * OPTIONS
    * PUT
    * DELETE
    * TRACE
    * are legal, subject to protocol restrictions. The default method is GET
    *
    * @param requestMethod    The HTTP method
    * @throws GuanxiException If the connection has already been opened then this exception will be thrown.
    */
   public void setRequestMethod(String requestMethod) throws GuanxiException {
     try {
       connection.setRequestMethod(requestMethod);
     }
     catch (ProtocolException e) {
       throw new GuanxiException(e);
     }
   }
 
   /**
    * Sets the value of the doOutput field for this EntityConnection to the specified value.
    * A URL connection can be used for input and/or output. Set the DoOutput flag to true
    * if you intend to use the EntityConnection for output, false if not. The default is false.
    *
    * @param doIt             The new value
    * @throws GuanxiException If the connection has already been opened then this exception will be thrown.
    */
   public void setDoOutput(boolean doIt) throws GuanxiException {
     try {
       connection.setDoOutput(doIt);
     }
     catch (IllegalStateException e) { // already connected
       throw new GuanxiException(e);
     }
   }
 
   /**
    * Opens a communications link to the resource referenced by this URL, if such a connection
    * has not already been established.
    * If the connect method is called when the connection has already been opened (indicated by
    * the connected field having the value true), the call is ignored.
    * EntityConnection objects go through two phases: first they are created, then they are connected.
    * After being created, and before being connected, various options can be specified
    * (e.g., doInput and UseCaches). After connecting, it is an error to try to set them.
    * Operations that depend on being connected, like getContentLength, will implicitly perform the connection,
    * if necessary
    *
    * @throws GuanxiException If the connection times out before being established, or if an IO issue occurs.
    */
   public void connect() throws GuanxiException {
     try {
       connection.connect();
     }
     catch (IOException e) { // SocketTimeoutException is a subclass of IOException
       throw new GuanxiException(e);
     }
   }
 
   /**
    * Returns an input stream that reads from this open connection. A SocketTimeoutException can be
    * thrown when reading from the returned input stream if the read timeout expires before data is
    * available for read
    *
    * @return                 An input stream that reads from this open connection
    * @throws GuanxiException If an IO issue occurs when creating the input stream 
    *                         (or if the protocol does not support input, which should not happen).
    */
   public InputStream getInputStream() throws GuanxiException {
     try {
       return connection.getInputStream();
     }
     catch (IOException e) {
      logErrorStream(e, connection);
       throw new GuanxiException(e);
     }
   }
 
   /**
    * Returns an output stream that writes to this connection
    *
    * @return                 An output stream that writes to this connection
    * @throws GuanxiException If an IO issue occurs when creating the output stream
    *                         (or if the protocol does not support output, which should not happen)
    */
   public OutputStream getOutputStream() throws GuanxiException {
     try {
       return connection.getOutputStream();
     }
     catch (IOException e) {
      logErrorStream(e, connection);
       throw new GuanxiException(e);
     }
   }
 
   /**
    * Sets the general request property. If a property with the key already exists, overwrite its
    * value with the new value.
    * NOTE: HTTP requires all request properties which can legally have multiple instances with the
    * same key to use a comma-separated list syntax which enables multiple properties to be appended
    * into a single property. While this means that multiple values can be added, this will not append
    * values when repeatedly called with the same key.
    *
    * @param key   The keyword by which the request is known (e.g., "accept").
    * @param value The value associated with it
    * @throws GuanxiException if an error occurs
    */
   public void setRequestProperty(String key, String value) throws GuanxiException {
     try {
       connection.setRequestProperty(key, value);
     }
     catch (Exception e) {
       // IllegalStateException = already connected
       // NullPointerException  = key is null
       throw new GuanxiException(e);
     }
   }
 
   /**
    * Returns the server's certificate chain which was established as part of defining the session.
    * Note: This method can be used only when using certificate-based cipher suites; using it with
    * non-certificate-based cipher suites, such as Kerberos, will cause it to return null.
    *
    * @return An ordered array of server certificates, with the peer's own certificate first followed
    * by any certificate authorities. If an error occurred it will return null.
    * @throws GuanxiException if an error occurred
    */
   public Certificate[] getServerCertificates() throws GuanxiException {
     if ( connection instanceof HttpsURLConnection ) {
       try {
         return ((HttpsURLConnection)connection).getServerCertificates();
       }
       catch (SSLPeerUnverifiedException e) {
         // SSLPeerUnverifiedException = peer not verified
         return null;
       }
       catch (IllegalStateException e) {
         // IllegalStateException      = called before the connection is made
         throw new GuanxiException(e);
       }
     }
     return null;
   }
 
   /**
    * Indicates that other requests to the server are unlikely in the near future.
    * Calling disconnect() should not imply that this HttpURLConnection instance
    * can be reused for other requests
    */
   public void disconnect() {
     connection.disconnect();
   }
 
   /**
    * When in probing mode, connects to the remote entity and extracts it's certificate chain
    *
    * @return                 An array of X509Certificate representing the remote entity's certificate chain
    * @throws GuanxiException If an error occurred. Will be thrown if the EntityConnection is
    *                         not in probing mode.
    */
   public X509Certificate[] getServerCertChain() throws GuanxiException {
     Certificate[] certificateChain;
     ArrayList<X509Certificate> convertedChain;
     CertificateFactory factory;
     
     // We can only do this in probing mode
     if (!probing) {
       throw new GuanxiException("EntityConnection not in probing mode");
     }
     
     // Get the certificate or chain the server is using...
     connect();
     certificateChain = getServerCertificates();
     disconnect();
 
     // Did we get any certificates?
     if (certificateChain.length == 0) {
       throw new GuanxiException("No server certificates available");
     }
 
     try {
       factory        = CertificateFactory.getInstance("X509");
       convertedChain = new ArrayList<X509Certificate>();
 
       // Cycle through the server's certificate chain, converting to X509 format
       for ( Certificate current : certificateChain ) {
         convertedChain.add((X509Certificate)factory.generateCertificate(new ByteArrayInputStream(current.getEncoded())));
       }
       
       return convertedChain.toArray(new X509Certificate[convertedChain.size()]);
     }
     catch(CertificateException ce) {
       throw new GuanxiException(ce);
     }
   }
 
   /**
    * When in probing mode, connects to the remote entity and extracts it's certificate. This is the
    * certificate in the chain that identifies the remote entity. The certificate chain is not processed.
    *
    * @return X509Certificate representing the remote entity's identity
    * @throws GuanxiException if an error occurred. Will be thrown if the EntityConnection is
    * not in probing mode.
    */
   public X509Certificate getServerCertificate() throws GuanxiException {
     // According to the javadocs, the server's own certificate is first in the chain
     return getServerCertChain()[0];
   }
 
   /**
    * Returns the length of the content available from the connection
    *
    * @return length of content available in bytes
    */
   public int getContentLength() {
     return connection.getContentLength();
   }
 
   /**
    * Returns content from a connection as a String
    *
    * @return String containing the content from the connection
    * @throws GuanxiException if an error occurs
    */
   public String getContentAsString() throws GuanxiException {
     try {
       return new String(Utils.read(connection.getInputStream()));
     }
     catch(IOException ioe) {
       throw new GuanxiException(ioe);
     }
   }
 
   /**
    * Adds authentication information to the request.
    *
    * @param username Username
    * @param password Password
    */
   public void setAuthentication(String username, String password) {
     String authentication;
     
     authentication = Base64.encode((username + ':' + password).getBytes());
     connection.setRequestProperty("Authorization", "Basic " + authentication);
   }
 
   /**
    * Logs the message available via the error stream, if any
    *
    * @param originalException the original exception that caused the error stream to be of interest
    * @param connection the connection that caused the error
    */
  private void logErrorStream(Exception originalException, HttpURLConnection connection) {
    InputStream errorStream = connection.getErrorStream();
     if (errorStream != null) {
       try {
         String errorStreamText = new String(Utils.read(errorStream));
         logger.error("===========================================================");
         logger.error(connection.getURL(), originalException);
         logger.error(errorStreamText);
         logger.error("===========================================================");
         errorStream.close();
       }
       catch(Exception ex) {
         logger.error(ex);
       }
     }
   }
 }
 
