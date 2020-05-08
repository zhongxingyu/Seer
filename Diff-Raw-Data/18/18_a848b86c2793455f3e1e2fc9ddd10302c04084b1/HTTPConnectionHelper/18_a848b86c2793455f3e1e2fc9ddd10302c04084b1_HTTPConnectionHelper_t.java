 //
 // $Id: FileUploadThreadHTTP.java 488 2008-07-06 20:21:43Z etienne_sf $
 //
 // jupload - A file upload applet.
 // Copyright 2007 The JUpload Team
 //
 // Created: 2007-03-07
 // Creator: etienne_sf
 // Last modified: $Date: 2008-07-06 22:21:43 +0200 (dim., 06 juil. 2008) $
 //
 // This program is free software; you can redistribute it and/or modify it under
 // the terms of the GNU General Public License as published by the Free Software
 // Foundation; either version 2 of the License, or (at your option) any later
 // version. This program is distributed in the hope that it will be useful, but
 // WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 // FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 // details. You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software Foundation, Inc.,
 // 675 Mass Ave, Cambridge, MA 02139, USA.
 
 package wjhk.jupload2.upload.helper;
 
 import java.io.BufferedOutputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PushbackInputStream;
 import java.net.Proxy;
 import java.net.ProxySelector;
 import java.net.Socket;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.security.KeyManagementException;
 import java.security.KeyStoreException;
 import java.security.NoSuchAlgorithmException;
 import java.security.UnrecoverableKeyException;
 import java.security.cert.CertificateException;
 
 import wjhk.jupload2.exception.JUploadException;
 import wjhk.jupload2.exception.JUploadIOException;
 import wjhk.jupload2.policies.UploadPolicy;
 
 /**
  * 
  * This class contains utilities to delegate network manipulation. It hides the
  * management for the current upload policy connection parameters.<BR>
  * This class goes through the following states, stored in the private
  * connectionStatus attribute: <DIR> <LI>STATUS_NOT_INITIALIZED: default status
  * when the instance in created. Only available action:
  * {@link #initRequest(URL, String, boolean, boolean)} <LI>
  * STATUS_BEFORE_SERVER_CONNECTION: the instance is initialized, and the caller
  * may begin writing the request to this ConnectionHelper. All data written to
  * it, will be stored in a {@link ByteArrayEncoderHTTP}. The connection switches
  * to this status when the {@link #initRequest(URL, String, boolean, boolean)}
  * is called. <LI>STATUS_WRITING_REQUEST: The network connection to the server
  * is now opened. The content of the ByteArrayEncoderHTTP has been sent to the
  * server. All subsequent calls to write methods will directly write on the
  * socket to the server. The {@link #sendRequest()} method changes the
  * connection to this status. <LI>STATUS_READING_RESPONSE: The request to the
  * server has been totally written. No more calls to the write methods are
  * allowed. The {@link #readHttpResponse()} is responsible to put the
  * connectionHelper to this status. <LI>STATUS_CONNECTION_CLOSED: The response
  * has been read. All getters can be called, to get information about the server
  * response. The only other method allowed is the
  * {@link #initRequest(URL, String, boolean, boolean)}, to start a new request
  * to the server. Using the same connectionHelper allows to use the same network
  * connection, when the allowHttpPersistent applet parameter is used. </DIR>
  * 
  * @author etienne_sf
  * 
  */
 public class HTTPConnectionHelper extends OutputStream {
 
     // FIXME Does HTTPConnectionHelper really need to be an OutputStream? (Find
     // Bug generate not closed exception)
 
     // ////////////////////////////////////////////////////////////////////////////////////
     // /////////////////// PRIVATE CONSTANTS
     // ////////////////////////////////////////////////////////////////////////////////////
     /**
      * Indicates that the connection has not been initialized. The only
      * authorized action in this state is a call to
      * {@link #initRequest(URL, String, boolean, boolean)}.
      */
     final static private int STATUS_NOT_INITIALIZED = 0;
 
     /**
      * Indicates that the network connection to the server has not been opened.
      * All data sent to the ConnectionHelper with write methods are sent to the
      * current ByteArrayEncoder.
      */
     final static private int STATUS_BEFORE_SERVER_CONNECTION = 1;
 
     /**
      * Indicates that the network connection to the server is opened, but the
      * request has not been totally sent to the server. All data sent to the
      * ConnectionHelper with write methods is sent to the network connection,
      * that is: to the current OutputStream. <BR>
      * That is: the ByteArrayEncoder is now read only (closed).
      */
     final static private int STATUS_WRITING_REQUEST = 2;
 
     /**
      * Indicates that the network connection to the server is opened, but the
      * request has not been totally sent to the server. All data sent to the
      * ConnectionHelper with write methods is sent to the network connection,
      * that is: to the current OutputStream. <BR>
      * That is: the ByteArrayEncoder is now read only (closed).
      */
     final static private int STATUS_READING_RESPONSE = 3;
 
     /**
      * Indicates that the network connection to the server is now closed, that
      * is: we've written the request, read the response, and free the server
      * connection. If the keepAlive parameter is used, the connection may remain
      * opened for the next request. <BR>
      * No more action may be done on this connection helper, out of reading
      * data, until the application do a call to
      * {@link #initRequest(URL, String, boolean, boolean)}.
      */
     final static private int STATUS_CONNECTION_CLOSED = 4;
 
     // ////////////////////////////////////////////////////////////////////////////////////
     // /////////////////// ATTRIBUTE USED TO CONTROL THE OUTPUT TO THE SERVER
     // ////////////////////////////////////////////////////////////////////////////////////
     /**
      * http boundary, for the posting multipart post.
      */
     private String boundary = calculateRandomBoundary();
 
     /**
      * Is chunk upload on for this request ?
      */
     private boolean bChunkEnabled;
 
     /**
      * Is it the last chunk ? If yes, we'll try to keep the connection open,
      * according to the current applet configuration.
      */
     private boolean bLastChunk;
 
     /**
      * The encoder that will contain the HTTP request.
      */
     private ByteArrayEncoder byteArrayEncoder = null;
 
     /**
      * Indicates where data sent to appendXxx method should be added. It must be
      * one of the SENDING_XXX private strings.
      */
     private int connectionStatus = STATUS_NOT_INITIALIZED;
 
     /**
      * Contains the HTTP reader. All data coming from the server response are
      * read from it. If this attribute is null, it means that the server
      * response has not been read.
      */
     private HTTPInputStreamReader httpInputStreamReader = null;
 
     /**
      * This stream allows the applet to get the server response. It is opened
      * and closed as the {@link #outputStream}.
      */
     private PushbackInputStream inputStream = null;
 
     /**
      * The HTTP method: POST, GET, HEAD...
      */
     private String method = null;
 
     /**
      * This stream is open by {@link #sendRequest()}. It is closed by the
      * {@link #readHttpResponse()} method.
      * 
      * @see #sendRequest()
      * @see #readHttpResponse()
      * @see #getOutputStream()
      */
     private DataOutputStream outputStream = null;
 
     /**
      * The current proxy, if any
      */
     private Proxy proxy = null;
 
     /**
      * The network socket where the bytes should be written.
      */
     private Socket socket = null;
 
     /**
      * The current upload policy
      */
     private UploadPolicy uploadPolicy = null;
 
     /**
      * The current URL
      */
     private URL url = null;
 
     /**
      * Should we use a proxy
      */
     private boolean useProxy;
 
     /**
      * Is SSL mode on ?
      */
     private boolean useSSL;
 
     // ////////////////////////////////////////////////////////////////////////////////////
     // /////////////////////// PUBLIC METHODS
     // ////////////////////////////////////////////////////////////////////////////////////
 
     /**
      * The standard constructor for this class.
      * 
      * @param uploadPolicy The current upload policy.
      */
     public HTTPConnectionHelper(UploadPolicy uploadPolicy) {
         this.uploadPolicy = uploadPolicy;
     }
 
     /**
      * The standard constructor for this class.
      * 
      * @param url The target URL
      * @param method The HTTP method (POST, GET, HEAD...)
      * @param bChunkEnabled Indicates if chunkUpload is enabled for this query.
      *            Put false, if non chunked request or if it is not relevant.
      * @param bLastChunk Indicates whether this chunk is the last one. Put true,
      *            if non chunked request or if it is not relevant.
      * @param uploadPolicy The current upload policy.
      * @throws JUploadIOException
      */
     public HTTPConnectionHelper(URL url, String method, boolean bChunkEnabled,
             boolean bLastChunk, UploadPolicy uploadPolicy)
             throws JUploadIOException {
         this.uploadPolicy = uploadPolicy;
         initRequest(url, method, bChunkEnabled, bLastChunk);
     }
 
     /**
      * The standard constructor for this class.
      * 
      * @param url The target URL
      * @param bChunkEnabled Indicates if chunkUpload is enabled for this query.
      *            Put false, if non chunked request or if it is not relevant.
      * @param method The HTTP method (POST, GET, HEAD...)
      * @param bLastChunk Indicates whether this chunk is the last one. Put true,
      *            if non chunked request or if it is not relevant.
      * @throws JUploadIOException
      */
     public synchronized void initRequest(URL url, String method,
             boolean bChunkEnabled, boolean bLastChunk)
             throws JUploadIOException {
         // This method expects that the connection has not been initialized yet,
         // or that the previous request is finished.
         if (this.connectionStatus != STATUS_NOT_INITIALIZED
                 && this.connectionStatus != STATUS_CONNECTION_CLOSED) {
             throw new JUploadIOException(
                     "Bad status of the connectionHelper in initRequest: "
                             + getStatusLabel());
         }
 
         // Clean any current request.
         if (isKeepAlive()) {
             dispose();
         }
 
         // Load the new parameters.
         this.url = url;
         this.method = method;
         this.bChunkEnabled = bChunkEnabled;
         this.bLastChunk = bLastChunk;
         // We will write to the local ByteArrayEncoder, until a connection to
         // the server is opened.
         initByteArrayEncoder();
 
         // Ok, the connectionHelper is now ready to get write commands.
         this.connectionStatus = STATUS_BEFORE_SERVER_CONNECTION;
     }
 
     /**
      * Return the current {@link ByteArrayEncoder}. If it was not created, it is
      * initialized.
      * 
      * @return The current {@link ByteArrayEncoder}, null if called before the
      *         first initialization.
      * @throws JUploadIOException
      * @see #initRequest(URL, String, boolean, boolean)
      */
     public ByteArrayEncoder getByteArrayEncoder() throws JUploadIOException {
         return this.byteArrayEncoder;
     }
 
     /**
      * @return Returns the boundary for this HTTP request.
      * 
      */
     public String getBoundary() {
         return this.boundary;
     }
 
     /**
      * Closes the byteArrayEncoder, create the socket (or not, depending on the
      * current uploadPolicy, and upload history), send the request, and create
      * the InputStream to read the server response.
      * 
      * @throws JUploadIOException
      */
     public synchronized void sendRequest() throws JUploadIOException {
         // This method expects that the connection is writing data to the
         // server.
         if (this.connectionStatus != STATUS_BEFORE_SERVER_CONNECTION) {
             throw new JUploadIOException(
                     "Bad status of the connectionHelper in initRequest: "
                             + getStatusLabel());
         }
 
         try {
             // We've finished with the current encoder.
             if (!this.byteArrayEncoder.isClosed()) {
                 this.byteArrayEncoder.close();
             }
 
             // Let's clear any field that could have been read in a previous
             // step:
             this.httpInputStreamReader = null;
 
             // Only connect, if sock is null!!
             // ... or if we don't persist HTTP connections (patch for IIS, based
             // on Marc Reidy's patch)
             if (this.socket == null
                     || !this.uploadPolicy.getAllowHttpPersistent()) {
                 this.socket = new HttpConnect(this.uploadPolicy).connect(
                         this.url, this.proxy);
                 this.outputStream = new DataOutputStream(
                         new BufferedOutputStream(this.socket.getOutputStream()));
                 this.inputStream = new PushbackInputStream(this.socket
                         .getInputStream(), 1);
             }
 
             // Send http request to server
             this.outputStream
                     .write(this.byteArrayEncoder.getEncodedByteArray());
 
             // The request has been sent. The current ByteArrayEncoder is now
             // useless. A new one is to be created for the next request.
             this.connectionStatus = STATUS_WRITING_REQUEST;
 
         } catch (IOException e) {
             throw new JUploadIOException("Unable to open socket", e);
         } catch (KeyManagementException e) {
             throw new JUploadIOException("Unable to open socket", e);
         } catch (UnrecoverableKeyException e) {
             throw new JUploadIOException("Unable to open socket", e);
         } catch (NoSuchAlgorithmException e) {
             throw new JUploadIOException("Unable to open socket", e);
         } catch (KeyStoreException e) {
             throw new JUploadIOException("Unable to open socket", e);
         } catch (CertificateException e) {
             throw new JUploadIOException("Unable to open socket", e);
         } catch (IllegalArgumentException e) {
             throw new JUploadIOException("Unable to open socket", e);
         }
 
     }
 
     /**
      * Releases all reserved resources.
      * 
      * @throws JUploadIOException
      */
     public synchronized void dispose() throws JUploadIOException {
 
         try {
             // Throws java.io.IOException
             this.outputStream.close();
         } catch (NullPointerException e) {
             // httpDataOut is already null ...
         } catch (IOException e) {
             throw new JUploadIOException(e);
         } finally {
             this.outputStream = null;
         }
 
         try {
             // Throws java.io.IOException
             this.inputStream.close();
         } catch (NullPointerException e) {
             // httpDataIn is already null ...
         } catch (IOException e) {
             throw new JUploadIOException(e);
         } finally {
             this.inputStream = null;
         }
 
         try {
             // Throws java.io.IOException
             this.socket.close();
         } catch (NullPointerException e) {
             // sock is already null ...
         } catch (IOException e) {
             throw new JUploadIOException(e);
         } finally {
             this.socket = null;
         }
     }
 
     /**
      * Return the current socket. If the byteArrayEncoder is not closed: close
      * it, and send the request to the server.
      * 
      * @return public Socket getSocket() { }
      */
 
     /**
      * get the output stream, where HTTP data can be written.
      * 
      * @return The current output stream to the server, where things can be
      *         written, event after the socket is open, if the byteArrayEncoder
      *         did not contain the full request.
      */
     public OutputStream getOutputStream() {
         return this;
     }
 
     /**
      * get the input stream, where HTTP server response can be read.
      * 
      * @return The current input stream of the socket.
      */
     public PushbackInputStream getInputStream() {
         return this.inputStream;
     }
 
     /**
      * Get the HTTP method (HEAD, POST, GET...)
      * 
      * @return The HTTP method
      */
     public String getMethod() {
         return this.method;
     }
 
     /**
      * Get the last response body.
      * 
      * @return The full response body, that is: the HTTP body of the server
      *         response.
      */
     public String getResponseBody() {
         return this.httpInputStreamReader.getResponseBody();
     }
 
     /**
      * Get the headers of the HTTP response.
      * 
      * @return The HTTP headers.
      */
     public String getResponseHeaders() {
         return this.httpInputStreamReader.getResponseHeaders();
     }
 
     /**
      * Get the last response message.
      * 
      * @return the response message, like "200 OK"
      */
     public String getResponseMsg() {
         return this.httpInputStreamReader.getResponseMsg();
     }
 
     /**
      * Get the label describing the current state of this connection helper.
      * 
      * @return A text describing briefly the current connection status.
      */
     public synchronized String getStatusLabel() {
         switch (this.connectionStatus) {
             case STATUS_NOT_INITIALIZED:
                 return "Not initialized";
             case STATUS_BEFORE_SERVER_CONNECTION:
                 return "Before server connection";
             case STATUS_WRITING_REQUEST:
                 return "Writing request to the network";
             case STATUS_READING_RESPONSE:
                 return "Reading server response";
             case STATUS_CONNECTION_CLOSED:
                 return "Connection closed";
         }
         return "Unknown status in HTTPConnectionHelper.getStatusLabel()";
     }
 
     /**
      * Get the current socket.
      * 
      * @return return the current Socket, opened toward the server.
      */
     Socket getSocket() {
         return this.socket;
     }
 
     /**
      * Append bytes to the current query. The bytes will be written to the
      * current ByteArrayEncoder if the the connection to the server is not open,
      * or directly to the server if the connection is opened.
      * 
      * @param b The byte to send to the server.
      * @return Returns the current ConnectionHelper, to allow coding like
      *         StringBuffers: a.append(b).append(c);
      * @throws JUploadIOException
      */
     public synchronized HTTPConnectionHelper append(int b)
             throws JUploadIOException {
         if (this.connectionStatus == STATUS_BEFORE_SERVER_CONNECTION) {
             this.byteArrayEncoder.append(b);
         } else if (this.connectionStatus == STATUS_WRITING_REQUEST) {
             try {
                 this.outputStream.write(b);
             } catch (IOException e) {
                 throw new JUploadIOException(e.getClass().getName()
                         + " while writing to httpDataOut", e);
             }
         } else {
             throw new JUploadIOException(
                     "Wrong status in HTTPConnectionHelper.write() ["
                             + getStatusLabel() + "]");
         }
         return this;
     }
 
     /**
      * Append bytes to the current query. The bytes will be written to the
      * current ByteArrayEncoder if the the connection to the server is not open,
      * or directly to the server if the connection is opened.
      * 
      * @param bytes The bytes to send to the server.
      * @return Returns the current ConnectionHelper, to allow coding like
      *         StringBuffers: a.append(b).append(c);
      * @throws JUploadIOException
      */
     public synchronized HTTPConnectionHelper append(byte[] bytes)
             throws JUploadIOException {
 
         if (this.connectionStatus == STATUS_BEFORE_SERVER_CONNECTION) {
             this.byteArrayEncoder.append(bytes);
         } else if (this.connectionStatus == STATUS_WRITING_REQUEST) {
             try {
                 this.outputStream.write(bytes);
             } catch (IOException e) {
                 throw new JUploadIOException(e.getClass().getName()
                         + " while writing to httpDataOut", e);
             }
         } else {
             throw new JUploadIOException(
                     "Wrong status in HTTPConnectionHelper.write() ["
                             + getStatusLabel() + "]");
         }
 
         if (this.uploadPolicy.getDebugLevel() > 100) {
             this.uploadPolicy
                     .displayDebug(
                             "[HTTPConnectionHelper append] ("
                                     + bytes.length
                                     + " bytes appended to "
                                     + (this.connectionStatus == STATUS_BEFORE_SERVER_CONNECTION ? " current ByteArrayEncoder"
                                             : " socket") + ")", 70);
         }
 
         return this;
     }
 
     /**
      * Append bytes to the current query. The bytes will be written to the
      * current ByteArrayEncoder if the the connection to the server is not open,
      * or directly to the server if the connection is opened.
      * 
      * @param bytes The bytes to send to the server.
      * @param off The first byte to send
      * @param len Number of bytes to send.
      * @return Returns the current ConnectionHelper, to allow coding like
      *         StringBuffers: a.append(b).append(c);
      * @throws JUploadIOException
      */
     public synchronized HTTPConnectionHelper append(byte[] bytes, int off,
             int len) throws JUploadIOException {
 
         if (this.connectionStatus == STATUS_BEFORE_SERVER_CONNECTION) {
             this.byteArrayEncoder.append(bytes);
         } else if (this.connectionStatus == STATUS_WRITING_REQUEST) {
             try {
                 this.outputStream.write(bytes, off, len);
             } catch (IOException e) {
                 throw new JUploadIOException(e.getClass().getName()
                         + " while writing to httpDataOut", e);
             }
         } else {
             throw new JUploadIOException(
                     "Wrong status in HTTPConnectionHelper.write() ["
                             + getStatusLabel() + "]");
         }
 
         if (this.uploadPolicy.getDebugLevel() > 100) {
             this.uploadPolicy
                     .displayDebug(
                             "[HTTPConnectionHelper append] ("
                                     + len
                                     + " bytes appended to "
                                     + (this.connectionStatus == STATUS_BEFORE_SERVER_CONNECTION ? " current ByteArrayEncoder"
                                             : " socket") + ")", 101);
         }
 
         return this;
     }
 
     /**
      * write a string to the current HTTP request.
      * 
      * @param str The string to write
      * @return The current HTTPConnectionHelper
      * @throws JUploadIOException If any problem occurs during the writing
      *             operation.
      * @see #append(byte[])
      */
     public synchronized HTTPConnectionHelper append(String str)
             throws JUploadIOException {
         this.uploadPolicy.displayDebug("[HTTPConnectionHelper append] " + str,
                 70);
         if (this.connectionStatus == STATUS_BEFORE_SERVER_CONNECTION) {
             this.byteArrayEncoder.append(str);
         } else if (this.connectionStatus == STATUS_WRITING_REQUEST) {
             ByteArrayEncoder bae = new ByteArrayEncoderHTTP(this.uploadPolicy,
                     this.byteArrayEncoder.getBoundary(), this.byteArrayEncoder
                             .getEncoding());
             bae.append(str);
             bae.close();
             this.append(bae);
         }
         return this;
     }
 
     /**
      * Appends a string to the current HTTP request.
      * 
      * @param bae The ByteArrayEncoder to write. It is expected to be correctly
      *            encoded. That is: it is up to the caller to check that its
      *            encoding is the same as the current HTTP request encoding.
      * @return The current HTTPConnectionHelper
      * @throws JUploadIOException If any problem occurs during the writing
      *             operation.
      * @see #append(byte[])
      */
     public synchronized HTTPConnectionHelper append(ByteArrayEncoder bae)
             throws JUploadIOException {
         this.uploadPolicy.displayDebug("[HTTPConnectionHelper append] "
                 + bae.getString(), 70);
         return this.append(bae.getEncodedByteArray());
     }
 
     /**
      * Read the response of the server. This method delegates the work to the
      * HTTPInputStreamReader. handles the chunk HTTP response.
      * 
      * @return The HTTP status. Should be 200, when everything is right.
      * @throws JUploadException
      */
     public synchronized int readHttpResponse() throws JUploadException {
         // This method expects that the connection is writing data to the
         // server.
         if (this.connectionStatus != STATUS_WRITING_REQUEST) {
             throw new JUploadIOException(
                     "Bad status of the connectionHelper in initRequest: "
                             + getStatusLabel());
         }
         this.connectionStatus = STATUS_READING_RESPONSE;
 
         // Let's connect in InputStream to read this server response.
         if (this.httpInputStreamReader == null) {
             this.httpInputStreamReader = new HTTPInputStreamReader(this,
                     this.uploadPolicy);
         }
 
         // Let's do the job
         try {
             this.outputStream.flush();
         } catch (IOException ioe) {
             throw new JUploadIOException("flushing outputStream, in "
                     + getClass().getName() + ".readHttpResponse()");
         }
         this.httpInputStreamReader.readHttpResponse();
 
         if (this.httpInputStreamReader.gotClose) {
             // RFC 2868, section 8.1.2.1
             dispose();
         }
 
         // We got the response
         this.connectionStatus = STATUS_CONNECTION_CLOSED;
 
         // FIXME Should close the connection, from time to time...
 
         //
         return this.httpInputStreamReader.gethttpStatusCode();
     }
 
     // ////////////////////////////////////////////////////////////////////////////////////
     // /////////////////////// PRIVATE METHODS
     // ////////////////////////////////////////////////////////////////////////////////////
 
     /**
      * creating of a new {@link ByteArrayEncoderHTTP}, and initializing of the
      * following header items: First line (POST currentProtocol URI), Host,
      * Connection, Keep-Alive, Proxy-Connection.
      * 
      * @throws JUploadIOException
      */
     private synchronized void initByteArrayEncoder() throws JUploadIOException {
         if (this.byteArrayEncoder != null && !this.byteArrayEncoder.isClosed()) {
             this.byteArrayEncoder.close();
             this.byteArrayEncoder = null;
         }
         this.byteArrayEncoder = new ByteArrayEncoderHTTP(this.uploadPolicy,
                 this.boundary);
         this.connectionStatus = STATUS_BEFORE_SERVER_CONNECTION;
         this.proxy = null;
         try {
             this.proxy = ProxySelector.getDefault().select(this.url.toURI())
                     .get(0);
         } catch (URISyntaxException e) {
             throw new JUploadIOException("Error while managing url "
                     + this.url.toExternalForm(), e);
         }
         this.useProxy = ((this.proxy != null) && (this.proxy.type() != Proxy.Type.DIRECT));
         this.useSSL = this.url.getProtocol().equals("https");
 
         // Header: Request line
         // Let's clear it. Useful only for chunked uploads.
         this.byteArrayEncoder.append(this.method);
         this.byteArrayEncoder.append(" ");
        this.uploadPolicy.displayDebug("[initByteArrayEncoder] proxy=" + proxy
                + ", proxy.type=" + this.proxy.type() + ", useProxy="
                + useProxy + ", url.host=" + this.url.getHost() + ", url.port="
                + this.url.getPort(), 80);
         if (this.useProxy && (!this.useSSL)) {
             // with a proxy we need the absolute URL, but only if not
             // using SSL. (with SSL, we first use the proxy CONNECT method,
             // and then a plain request.)
             this.byteArrayEncoder.append(this.url.getProtocol()).append("://")
                     .append(this.url.getHost());
             // TODO port in proxy mode: to be tested !
            if (this.url.getPort() > 0) {
                this.byteArrayEncoder.append(":").append(
                        Integer.toString(this.url.getPort()));
            }
         }
         this.byteArrayEncoder.append(this.url.getPath());
 
         // Append the query params.
         // TODO: This probably can be removed as we now have everything in POST
         // data. However in order to be
         // backwards-compatible, it stays here for now. So we now provide
         // *both* GET and POST params.
         if (null != this.url.getQuery() && !"".equals(this.url.getQuery()))
             this.byteArrayEncoder.append("?").append(this.url.getQuery());
 
         this.byteArrayEncoder.append(" ").append(
                 this.uploadPolicy.getServerProtocol()).append("\r\n");
 
         // Header: General
         this.byteArrayEncoder.append("Host: ").append(this.url.getHost())
                 .append("\r\nAccept: */*\r\n");
         // We do not want gzipped or compressed responses, so we must
         // specify that here (RFC 2616, Section 14.3)
         this.byteArrayEncoder.append("Accept-Encoding: identity\r\n");
 
         // Seems like the Keep-alive doesn't work properly, at least on my
         // local dev (Etienne).
         if (!this.uploadPolicy.getAllowHttpPersistent()) {
             this.byteArrayEncoder.append("Connection: close\r\n");
         } else {
             if (!this.bChunkEnabled
                     || this.bLastChunk
                     || this.useProxy
                     || !this.uploadPolicy.getServerProtocol()
                             .equals("HTTP/1.1")) { // RFC 2086, section 19.7.1
                 this.byteArrayEncoder.append("Connection: close\r\n");
             } else {
                 this.byteArrayEncoder.append("Keep-Alive: 300\r\n");
                 if (this.useProxy)
                     this.byteArrayEncoder
                             .append("Proxy-Connection: keep-alive\r\n");
                 else
                     this.byteArrayEncoder.append("Connection: keep-alive\r\n");
             }
         }
 
         // Get specific headers for this upload.
         this.uploadPolicy.onAppendHeader(this.byteArrayEncoder);
     }
 
     /**
      * Indicates whether the current socket should be reused ... if any.
      */
     private boolean isKeepAlive() {
         if (this.socket == null) {
             return false;
         } else if (!this.uploadPolicy.getAllowHttpPersistent()) {
             return false;
         } else {
             if (!this.bChunkEnabled
                     || this.bLastChunk
                     || this.useProxy
                     || !this.uploadPolicy.getServerProtocol()
                             .equals("HTTP/1.1")) { // RFC 2086, section 19.7.1
                 return false;
             } else {
                 return true;
             }
         }
     }
 
     /**
      * Construction of a random boundary, to separate the uploaded files, in the
      * HTTP upload request.
      * 
      * @return The calculated boundary.
      */
     private final String calculateRandomBoundary() {
         StringBuffer sbRan = new StringBuffer(11);
         sbRan.append("-----------------------------");
         String alphaNum = "1234567890abcdefghijklmnopqrstuvwxyz";
         int num;
         for (int i = 0; i < 11; i++) {
             num = (int) (Math.random() * (alphaNum.length() - 1));
             sbRan.append(alphaNum.charAt(num));
         }
         return sbRan.toString();
     }
 
     // ////////////////////////////////////////////////////////////////////////////////////
     // /////////////////// OVERRIDE OF OutputStream METHODS
     // ////////////////////////////////////////////////////////////////////////////////////
     /** {@inheritDoc} */
     @Override
     public void write(int b) throws IOException {
         try {
             append(b);
         } catch (JUploadIOException e) {
             // Hum, HTTPConnectionHelper catch IOException, and throws a
             // JUploadIOException. Now we get the cause, that is the original
             // IOException. Not optimized.
             if (e.getCause() == null) {
                 // This should not happen
                 throw new IOException(e);
             } else if (e.getCause() instanceof IOException) {
                 throw (IOException) e.getCause();
             } else {
                 // Hum, can something like an OutOfMemory. We must throw it.
                 throw new IOException(e.getCause());
             }
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void write(byte[] b, int off, int len) throws IOException {
         try {
             append(b, off, len);
         } catch (JUploadIOException e) {
             // Hum, HTTPConnectionHelper catch IOException, and throws a
             // JUploadIOException. Now we get the cause, that is the original
             // IOException. Not optimized.
             if (e.getCause() == null) {
                 // This should not happen
                 throw new IOException(e);
             } else if (e.getCause() instanceof IOException) {
                 throw (IOException) e.getCause();
             } else {
                 // Hum, can something like an OutOfMemory. We must throw it.
                 throw new IOException(e.getCause());
             }
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void write(byte[] b) throws IOException {
         write(b, 0, b.length);
     }
 
     /**
      * This method is the override of {@link OutputStream#close()} one. It may
      * not been called. You must use the {@link #sendRequest()} or
      * {@link #readHttpResponse()} methods instead.
      * 
      * @see java.io.OutputStream#close()
      */
     @Override
     public void close() throws IOException {
         throw new IOException(
                 "Forbidden action (HTTPConnectionHelper.close()). Please use the "
                         + getClass().getName() + ".sendRequest() method");
     }
 
     /**
      * Flushes the output stream. Useful only when the HTTPConnectionHelper is
      * writing to the socket toward the server, that is when the status is:
      * STATUS_WRITING_REQUEST.
      * 
      * @see java.io.OutputStream#flush()
      */
     @Override
     public void flush() throws IOException {
         if (this.connectionStatus == STATUS_WRITING_REQUEST) {
             this.outputStream.flush();
         } else {
             throw new IOException("Wrong status in " + getClass().getName()
                     + ".flush method: " + getStatusLabel());
         }
     }
 }
