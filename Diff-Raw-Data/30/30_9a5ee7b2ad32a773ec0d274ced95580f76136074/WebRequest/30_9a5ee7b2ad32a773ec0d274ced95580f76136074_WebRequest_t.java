 package com.meterware.httpunit;
 /********************************************************************************************************************
 * $Id$
 *
 * Copyright (c) 2000-2002, Russell Gold
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions
 * of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *
 *******************************************************************************************************************/
 import java.io.File;
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import java.security.Provider;
 import java.security.Security;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 
 import java.util.Dictionary;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Vector;
 import java.util.Arrays;
 
 /**
  * A request sent to a web server.
  **/
 abstract
 public class WebRequest {
     private SubmitButton _button;
 
     /**
      * Sets the value of a header to be sent with this request. A header set here will override any matching header set
      * in the WebClient when the request is actually sent.
      */
     public void setHeaderField( String headerName, String headerValue ) {
         getHeaderDictionary().put( headerName, headerValue );
     }
 
     /**
      * Returns a copy of the headers to be sent with this request.
      **/
     public Dictionary getHeaders() {
         return (Dictionary) getHeaderDictionary().clone();
     }
 
 
     /**
      * Returns the final URL associated with this web request.
      **/
     public URL getURL() throws MalformedURLException {
         if (getURLBase() == null || getURLString().indexOf( ':' ) > 0) validateProtocol( getURLString() );
        if (getURLBase() == null || getURLBase().toString().indexOf( "?" ) < 0) {
            return new URL( getURLBase(), getURLString() );
        } else {
            final String urlBaseString = getURLBase().toString();
            URL newurlbase = new URL( urlBaseString.substring( 0, urlBaseString.indexOf( "?" ) ) );
            return new URL( newurlbase, getURLString() );
        }
     }
 
 
     /**
      * Returns the target for this web request.
      **/
     public String getTarget() {
         return _target;
     }
 
 
     /**
      * Returns the HTTP method defined for this request.
      **/
     abstract
     public String getMethod();
 
 
     /**
      * Returns the query string defined for this request. The query string is sent to the HTTP server as part of
      * the request header. This default implementation returns an empty string.
      **/
     public String getQueryString() {
         return "";
     }
 
 
 //------------------------------------- ParameterCollection methods ------------------------------------
 
 
     /**
      * Sets the value of a parameter in a web request.
      **/
     public void setParameter( String name, String value ) {
         _parameterHolder.setParameter( name, value );
     }
 
 
     /**
      * Sets the multiple values of a parameter in a web request.
      **/
     public void setParameter( String name, String[] values ) {
         _parameterHolder.setParameter( name, values );
     }
 
 
     /**
      * Sets the multiple values of a file upload parameter in a web request.
      **/
     public void setParameter( String parameterName, UploadFileSpec[] files ) {
         if (!maySelectFile( parameterName )) throw new IllegalNonFileParameterException( parameterName );
         if (!isMimeEncoded()) throw new MultipartFormRequiredException();
         _parameterHolder.setParameter( parameterName, files );
     }
 
 
     /**
      * Specifies the click position for the submit button. When a user clioks on an image button, not only the name
      * and value of the button, but also the position of the mouse at the time of the click is submitted with the form.
      * This method allows the caller to override the position selected when this request was created.
      *
      * @exception IllegalRequestParameterException thrown if the request was not created from a form with an image button.
      **/
     public void setImageButtonClickPosition( int x, int y ) throws IllegalRequestParameterException {
         if (_button == null) throw new IllegalButtonPositionException();
         _parameterHolder.selectImageButtonPosition( _button, x, y );
     }
 
 
     /**
      * Returns true if the specified parameter is a file field.
      **/
     public boolean isFileParameter( String name ) {
         return _parameterHolder.isFileParameter( name );
     }
 
 
     /**
      * Sets the file for a parameter upload in a web request.
      **/
     public void selectFile( String parameterName, File file ) {
         setParameter( parameterName, new UploadFileSpec[] { new UploadFileSpec( file ) } );
     }
 
 
     /**
      * Sets the file for a parameter upload in a web request.
      **/
     public void selectFile( String parameterName, File file, String contentType ) {
         setParameter( parameterName, new UploadFileSpec[] { new UploadFileSpec( file, contentType ) } );
     }
 
 
     /**
      * Sets the file for a parameter upload in a web request.
      **/
     public void selectFile( String parameterName, String fileName, InputStream inputStream, String contentType ) {
         setParameter( parameterName, new UploadFileSpec[] { new UploadFileSpec( fileName, inputStream, contentType ) } );
     }
 
 
     /**
      * Returns an enumeration of all parameters in this web request.
      * @deprecated use getRequestParameterNames instead
      **/
     public Enumeration getParameterNames() {
         return new Vector( Arrays.asList( _parameterHolder.getParameterNames() ) ).elements();
     }
 
 
     /**
      * Returns an array of all parameter names in this web request.
      * @since 1.3.1
      **/
     public String[] getRequestParameterNames() {
         return _parameterHolder.getParameterNames();
     }
 
 
     /**
      * Returns the value of a parameter in this web request.
      * @return the value of the named parameter, or empty string
      *         if it is not set.
      **/
     public String getParameter( String name ) {
         String[] values = getParameterValues( name );
         return values.length == 0 ? "" : values[0];
     }
 
 
     /**
      * Returns the multiple default values of the named parameter.
      **/
     public String[] getParameterValues( String name ) {
         return _parameterHolder.getParameterValues( name );
     }
 
 
     /**
      * Removes a parameter from this web request.
      **/
     public void removeParameter( String name ) {
         _parameterHolder.removeParameter( name );
     }
 
 
 //------------------------------------- Object methods ------------------------------------
 
 
     public String toString() {
         return getMethod() + " request for " + getURLString();
     }
 
 
 
 //------------------------------------- protected members ------------------------------------
 
 
     /**
      * Constructs a web request using an absolute URL string.
      **/
     protected WebRequest( String urlString ) {
         this( (URL) null, urlString );
     }
 
 
     /**
      * Constructs a web request using a base URL and a relative URL string.
      **/
     protected WebRequest( URL urlBase, String urlString ) {
         this( urlBase, urlString, TOP_FRAME );
     }
 
 
     /**
      * Constructs a web request using a base request and a relative URL string.
      **/
     protected WebRequest( WebRequest baseRequest, String urlString, String target ) throws MalformedURLException {
         this( baseRequest.getURL(), urlString, target );
     }
 
 
     /**
      * Constructs a web request using a base URL, a relative URL string, and a target.
      **/
     protected WebRequest( URL urlBase, String urlString, String target ) {
         this( urlBase, urlString, target, new UncheckedParameterHolder() );
     }
 
 
     /**
      * Constructs a web request from a form.
      **/
     protected WebRequest( WebForm sourceForm, SubmitButton button, int x, int y ) {
         this( sourceForm );
         if (button != null && button.isImageButton() && button.getName().length() > 0) {
             _button = button;
             _parameterHolder.selectImageButtonPosition( _button, x, y );
         }
     }
 
 
     protected WebRequest( WebRequestSource requestSource ) {
         this( requestSource.getBaseURL(), requestSource.getRelativeURL(), requestSource.getTarget(), newParameterHolder( requestSource ) );
         setHeaderField( "Referer", requestSource.getBaseURL().toExternalForm() );
     }
 
 
     private static ParameterHolder newParameterHolder( WebRequestSource requestSource ) {
         if (HttpUnitOptions.getParameterValuesValidated()) {
             return requestSource;
         } else {
             return new UncheckedParameterHolder( requestSource );
         }
     }
 
 
     /**
      * Constructs a web request using a base URL, a relative URL string, and a target.
      **/
     private WebRequest( URL urlBase, String urlString, String target, ParameterHolder parameterHolder ) {
         _urlBase   = urlBase;
         _urlString = urlString;
         _target    = target;
         _parameterHolder = parameterHolder;
     }
 
 
     /**
      * Returns true if selectFile may be called with this parameter.
      */
     protected boolean maySelectFile( String parameterName )
     {
         return isFileParameter( parameterName );
     }
 
 
     /**
      * Selects whether MIME-encoding will be used for this request. MIME-encoding changes the way the request is sent
      * and is required for requests which include file parameters. This method may only be called for a POST request
      * which was not created from a form.
      **/
     protected void setMimeEncoded( boolean mimeEncoded )
     {
         _parameterHolder.setSubmitAsMime( mimeEncoded );
     }
 
 
     /**
      * Returns true if this request is to be MIME-encoded.
      **/
     protected boolean isMimeEncoded() {
         return _parameterHolder.isSubmitAsMime();
     }
 
 
     /**
      * Returns the content type of this request. If null, no content is specified.
      */
     protected String getContentType() {
         return null;
     }
 
 
     /**
      * Returns the character set required for this request.
      **/
     final
     protected String getCharacterSet() {
         return _parameterHolder.getCharacterSet();
     }
 
 
     /**
      * Performs any additional processing necessary to complete the request.
      **/
     protected void completeRequest( URLConnection connection ) throws IOException {
     }
 
 
     /**
      * Writes the contents of the message body to the specified stream.
      */
     protected void writeMessageBody( OutputStream stream ) throws IOException {
     }
 
 
     final
     protected URL getURLBase() {
         return _urlBase;
     }
 
 
 //------------------------------------- protected members ---------------------------------------------
 
 
     protected String getURLString() {
         final String queryString = getQueryString();
         if (queryString.length() == 0) {
             return _urlString;
         } else {
             return _urlString + "?" + queryString;
         }
     }
 
 
     final
     protected ParameterHolder getParameterHolder() {
         return _parameterHolder;
     }
 
 
 //---------------------------------- package members --------------------------------
 
     /** The name of the topmost frame. **/
     final static String TOP_FRAME = "_top";
 
 
     Hashtable getHeaderDictionary() {
         if (_headers == null) {
             _headers = new Hashtable();
             if (getContentType() != null) _headers.put( "Content-Type", getContentType() );
         }
         return _headers;
     }
 
 
 //--------------------------------------- private members ------------------------------------
 
 
     /** The name of the system parameter used by java.net to locate protocol handlers. **/
     private final static String PROTOCOL_HANDLER_PKGS  = "java.protocol.handler.pkgs";
 
     /** The name of the JSSE class which provides support for SSL. **/
     private final static String SunJSSE_PROVIDER_CLASS = "com.sun.net.ssl.internal.ssl.Provider";
 
     /** The name of the JSSE class which supports the https protocol. **/
     private final static String SSL_PROTOCOL_HANDLER   = "com.sun.net.ssl.internal.www.protocol";
 
     private final ParameterHolder _parameterHolder;
 
     private URL          _urlBase;
     private String       _urlString;
     private String       _target = TOP_FRAME;
     private Hashtable    _headers;
 
     private boolean      _httpsProtocolSupportEnabled;
 
 
     private void validateProtocol( String urlString ) {
         if (urlString.indexOf(':') <= 0) {
             throw new RuntimeException( "No protocol specified in URL '" + urlString + "'" );
         }
 
         String protocol = urlString.substring( 0, urlString.indexOf( ':' ) );
         if (protocol.equalsIgnoreCase( "http" )) {
             return;
         } else if (protocol.equalsIgnoreCase( "https" )) {
             validateHttpsProtocolSupport();
         }
     }
 
 
     void validateHttpsProtocolSupport() {
         if (!_httpsProtocolSupportEnabled) {
             verifyHttpsSupport();
             _httpsProtocolSupportEnabled = true;
         }
     }
 
 
     private static boolean hasProvider( Class providerClass ) {
         Provider[] list = Security.getProviders();
         for (int i = 0; i < list.length; i++) {
             if (list[i].getClass().equals( providerClass )) return true;
         }
         return false;
     }
 
     private static void verifyHttpsSupport() {
         if (System.getProperty( "java.version" ).startsWith( "1.1" )) {
             throw new RuntimeException( "https support requires Java 2" );
         } else {
             try {
                 Class providerClass = Class.forName( SunJSSE_PROVIDER_CLASS );
                 if (!hasProvider( providerClass )) Security.addProvider( (Provider) providerClass.newInstance() );
                 registerSSLProtocolHandler();
             } catch (ClassNotFoundException e) {
                 throw new RuntimeException( "https support requires the Java Secure Sockets Extension. See http://java.sun.com/products/jsse" );
             } catch (Throwable e) {
                 throw new RuntimeException( "Unable to enable https support. Make sure that you have installed JSSE " +
                                             "as described in http://java.sun.com/products/jsse/install.html: " + e );
             }
         }
     }
 
 
     private static void registerSSLProtocolHandler() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
         String list = System.getProperty( PROTOCOL_HANDLER_PKGS );
         Method setMethod = System.class.getMethod( "setProperty", new Class[] { String.class, String.class } );
         if (list == null || list.length() == 0) {
             setMethod.invoke( null, new String[] { PROTOCOL_HANDLER_PKGS, SSL_PROTOCOL_HANDLER } );
         } else if (list.indexOf( SSL_PROTOCOL_HANDLER ) < 0) {
             setMethod.invoke( null, new String[] { PROTOCOL_HANDLER_PKGS, SSL_PROTOCOL_HANDLER + " | " + list } );
         }
     }
 
 
 }
 
 
 class URLEncodedString implements ParameterProcessor {
 
     private StringBuffer _buffer = new StringBuffer( HttpUnitUtils.DEFAULT_BUFFER_SIZE );
 
     private boolean _haveParameters = false;
 
 
     public String getString() {
         return _buffer.toString();
     }
 
 
     public void addParameter( String name, String value, String characterSet ) {
         if (_haveParameters) _buffer.append( '&' );
         _buffer.append( encode( name, characterSet ) );
         if (value != null) _buffer.append( '=' ).append( encode( value, characterSet ) );
         _haveParameters = true;
     }
 
 
     public void addFile( String parameterName, UploadFileSpec fileSpec ) {
         throw new RuntimeException( "May not URL-encode a file upload request" );
     }
 
 
     /**
      * Returns a URL-encoded version of the string, including all eight bits, unlike URLEncoder, which strips the high bit.
      **/
     private String encode( String source, String characterSet ) {
         if (characterSet.equalsIgnoreCase( HttpUnitUtils.DEFAULT_CHARACTER_SET )) {
             return URLEncoder.encode( source );
         } else {
             try {
                 byte[] rawBytes = source.getBytes( characterSet );
                 StringBuffer result = new StringBuffer( 3*rawBytes.length );
                 for (int i = 0; i < rawBytes.length; i++) {
                     int candidate = rawBytes[i] & 0xff;
                     if (candidate == ' ') {
                         result.append( '+' );
                     } else if ((candidate >= 'A' && candidate <= 'Z') ||
                                (candidate >= 'a' && candidate <= 'z') ||
                                (candidate == '.') ||
                                (candidate >= '0' && candidate <= '9')) {
                         result.append( (char) rawBytes[i] );
                     } else if (candidate < 16) {
                         result.append( "%0" ).append( Integer.toHexString( candidate ).toUpperCase() );
                     } else {
                         result.append( '%' ).append( Integer.toHexString( candidate ).toUpperCase() );
                     }
                 }
                 return result.toString();
             } catch (java.io.UnsupportedEncodingException e) {
                 return "???";    // XXX should pass the exception through as IOException ultimately
             }
         }
     }
 
 }
 
 
 //============================= exception class IllegalNonFileParameterException ======================================
 
 
 /**
  * This exception is thrown on an attempt to set a non-file parameter to a file value.
  **/
 class IllegalNonFileParameterException extends IllegalRequestParameterException {
 
 
     IllegalNonFileParameterException( String parameterName ) {
         _parameterName = parameterName;
     }
 
 
     public String getMessage() {
         return "Parameter '" + _parameterName + "' is not a file parameter and may not be set to a file value.";
     }
 
 
     private String _parameterName;
 
 }
 
 
 //============================= exception class MultipartFormRequiredException ======================================
 
 
 /**
  * This exception is thrown on an attempt to set a file parameter in a form that does not specify MIME encoding.
  **/
 class MultipartFormRequiredException extends IllegalRequestParameterException {
 
 
     MultipartFormRequiredException() {
     }
 
 
     public String getMessage() {
         return "The request does not use multipart/form-data encoding, and cannot be used to upload files ";
     }
 
 }
 
 
 //============================= exception class IllegalButtonPositionException ======================================
 
 
 /**
  * This exception is thrown on an attempt to set a file parameter in a form that does not specify MIME encoding.
  **/
 class IllegalButtonPositionException extends IllegalRequestParameterException {
 
 
     IllegalButtonPositionException() {
     }
 
 
     public String getMessage() {
         return "The request was not created with an image button, and cannot accept an image button click position";
     }
 
 }
