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
 import com.meterware.httpunit.scripting.ScriptableDelegate;
import com.meterware.httpunit.scripting.NamedDelegate;
 import com.meterware.httpunit.cookies.CookieJar;
 import com.meterware.httpunit.cookies.CookieSource;
 
 import java.io.*;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Hashtable;
 import java.util.Vector;
 import java.util.zip.GZIPInputStream;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 /**
  * A response to a web request from a web server.
  *
  * @author <a href="mailto:russgold@httpunit.org">Russell Gold</a>
  * @author <a href="mailto:DREW.VARNER@oracle.com">Drew Varner</a>
  * @author <a href="mailto:dglo@ssec.wisc.edu">Dave Glowacki</a>
  * @author <a href="mailto:bx@bigfoot.com">Benoit Xhenseval</a>
  **/
 abstract
 public class WebResponse implements HTMLSegment, CookieSource {
     private String _refreshHeader;
     private boolean _hasSubframes;
 
 
     /**
      * Returns a web response built from a URL connection. Provided to allow
      * access to WebResponse parsing without using a WebClient.
      **/
     public static WebResponse newResponse( URLConnection connection ) throws IOException {
         return new HttpWebResponse( null, "_top", connection.getURL(), connection, HttpUnitOptions.getExceptionsThrownOnErrorStatus() );
     }
 
 
     /**
      * Returns true if the response is HTML.
      **/
     public boolean isHTML() {
         return getContentType().equalsIgnoreCase( HTML_CONTENT );
     }
 
 
     /**
      * Returns the URL which invoked this response.
      **/
     public URL getURL() {
         return _url;
     }
 
 
     /**
      * Returns the title of the page.
      * @exception SAXException thrown if there is an error parsing this response
      **/
     public String getTitle() throws SAXException {
         return getReceivedPage().getTitle();
     }
 
 
     /**
      * Returns the stylesheet linked in the head of the page.
      * <code>
      * <link type="text/css" rel="stylesheet" href="/mystyle.css" />
      * </code>
      * will return "/mystyle.css".
      * @exception SAXException thrown if there is an error parsing this response
      **/
     public String getExternalStyleSheet() throws SAXException {
         return getReceivedPage().getExternalStyleSheet();
     }
 
     /**
      * Retrieves the "content" of the meta tags for a key pair attribute-attributeValue.
      * <code>
      *  <meta name="robots" content="index" />
      *  <meta name="robots" content="follow" />
      *  <meta http-equiv="Expires" content="now" />
      * </code>
      * this can be used like this
      * <code>
      *      getMetaTagContent("name","robots") will return { "index","follow" }
      *      getMetaTagContent("http-equiv","Expires") will return { "now" }
      * </code>
      * @exception SAXException thrown if there is an error parsing this response
      **/
     public String[] getMetaTagContent(String attribute, String attributeValue) throws SAXException {
         return getReceivedPage().getMetaTagContent(attribute, attributeValue);
     }
 
 
     /**
      * Returns the target of the page.
      * @deprecated use getFrameName
      **/
     public String getTarget() {
         return getFrameName();
     }
 
 
     /**
      * Returns the name of the frame containing this page.
      **/
     public String getFrameName() {
         return _frameName;
     }
 
 
     /**
      * Returns a request to refresh this page, if any. This request will be defined
      * by a <meta> tag in the header.  If no tag exists, will return null.
      **/
     public WebRequest getRefreshRequest() {
         readRefreshRequest();
         return _refreshRequest;
     }
 
 
     /**
      * Returns the delay before normally following the request to refresh this page, if any.
      * This request will be defined by a <meta> tag in the header.  If no tag exists,
      * will return zero.
      **/
     public int getRefreshDelay() {
         readRefreshRequest();
         return _refreshDelay;
     }
 
 
     /**
      * Returns the response code associated with this response.
      **/
     abstract
     public int getResponseCode();
 
 
     /**
      * Returns the response message associated with this response.
      **/
     abstract
     public String getResponseMessage();
 
 
     /**
      * Returns the content length of this response.
      * @return the content length, if known, or -1.
      */
     public int getContentLength() {
         if (_contentLength == UNINITIALIZED_INT) {
             String length = getHeaderField( "Content-Length" );
             _contentLength = (length == null) ? -1 : Integer.parseInt( length );
         }
         return _contentLength;
        }
 
 
     /**
      * Returns the content type of this response.
      **/
     public String getContentType() {
         if (_contentType == null) readContentTypeHeader();
         return _contentType;
     }
 
 
     /**
      * Returns the character set used in this response.
      **/
     public String getCharacterSet() {
         if (_characterSet == null) {
             readContentTypeHeader();
             if (_characterSet == null) setCharacterSet( getHeaderField( "Charset" ) );
             if (_characterSet == null) setCharacterSet( HttpUnitOptions.getDefaultCharacterSet() );
         }
         return _characterSet;
     }
 
 
     /**
      * Returns a list of new cookie names defined as part of this response.
      **/
     public String[] getNewCookieNames() {
         return getCookieJar().getCookieNames();
     }
 
 
     /**
      * Returns the new cookie value defined as part of this response.
      **/
     public String getNewCookieValue( String name ) {
         return getCookieJar().getCookieValue( name );
     }
 
 
     /**
      * Returns the names of the header fields found in the response.
      **/
     abstract
     public String[] getHeaderFieldNames();
 
 
      /**
      * Returns the value for the specified header field. If no such field is defined, will return null.
      * If more than one header is defined for the specified name, returns only the first found.
      **/
     abstract
     public String getHeaderField( String fieldName );
 
 
     /**
      * Returns the text of the response (excluding headers) as a string. Use this method in preference to 'toString'
      * which may be used to represent internal state of this object.
      **/
     public String getText() throws IOException {
         if (_responseText == null) loadResponseText();
         return _responseText;
     }
 
 
     /**
      * Returns a buffered input stream for reading the contents of this reply.
      **/
     public InputStream getInputStream() throws IOException {
         if (_inputStream == null) _inputStream = new ByteArrayInputStream( getText().getBytes() );
         return _inputStream;
     }
 
 
     /**
      * Returns the names of the frames found in the page in the order in which they appear.
      * @exception SAXException thrown if there is an error parsing this response
      **/
     public String[] getFrameNames() throws SAXException {
         WebFrame[] frames = getFrames();
         String[] result = new String[ frames.length ];
         for (int i = 0; i < result.length; i++) {
             result[i] = frames[i].getName();
         }
 
         return result;
     }
 
 
     /**
      * Returns the contents of the specified subframe of this frameset response.
      *
      * @param subFrameName the name of the desired frame as defined in the frameset.
      **/
     public WebResponse getSubframeContents( String subFrameName ) {
         if (_window == null) throw new NoSuchFrameException( subFrameName );
         return _window.getFrameContents( WebFrame.getNestedFrameName( _frameName, subFrameName ) );
     }
 
 
 //---------------------- HTMLSegment methods -----------------------------
 
     /**
      * Returns the forms found in the page in the order in which they appear.
      * @exception SAXException thrown if there is an error parsing the response.
      **/
     public WebForm[] getForms() throws SAXException {
         return getReceivedPage().getForms();
     }
 
 
     /**
      * Returns the form found in the page with the specified name.
      * @exception SAXException thrown if there is an error parsing the response.
      **/
     public WebForm getFormWithName( String name ) throws SAXException {
         return getReceivedPage().getFormWithName( name );
     }
 
 
     /**
      * Returns the form found in the page with the specified ID.
      * @exception SAXException thrown if there is an error parsing the response.
      **/
     public WebForm getFormWithID( String ID ) throws SAXException {
         return getReceivedPage().getFormWithID( ID );
     }
 
 
     /**
      * Returns the links found in the page in the order in which they appear.
      * @exception SAXException thrown if there is an error parsing the response.
      **/
     public WebLink[] getLinks() throws SAXException {
         return getReceivedPage().getLinks();
     }
 
 
     /**
      * Returns the first link which contains the specified text.
      * @exception SAXException thrown if there is an error parsing the response.
      **/
     public WebLink getLinkWith( String text ) throws SAXException {
         return getReceivedPage().getLinkWith( text );
     }
 
 
     /**
      * Returns the first link which contains an image with the specified text as its 'alt' attribute.
      * @exception SAXException thrown if there is an error parsing the response.
      **/
     public WebLink getLinkWithImageText( String text ) throws SAXException {
         return getReceivedPage().getLinkWithImageText( text );
     }
 
 
     /**
      * Returns the link found in the page with the specified name.
      * @exception SAXException thrown if there is an error parsing the response.
      **/
     public WebLink getLinkWithName( String name ) throws SAXException {
         return getReceivedPage().getLinkWithName( name );
     }
 
 
     /**
      * Returns the link found in the page with the specified ID.
      * @exception SAXException thrown if there is an error parsing the response.
      **/
     public WebLink getLinkWithID( String ID ) throws SAXException {
         return getReceivedPage().getLinkWithID( ID );
     }
 
 
     /**
      * Returns the first link found in the page matching the specified criteria.
      * @exception SAXException thrown if there is an error parsing the response.
      **/
     public WebLink getFirstMatchingLink( HTMLElementPredicate predicate, Object criteria ) throws SAXException {
         return getReceivedPage().getFirstMatchingLink( predicate, criteria );
     }
 
 
     /**
      * Returns all links found in the page matching the specified criteria.
      * @exception SAXException thrown if there is an error parsing the response.
      **/
     public WebLink[] getMatchingLinks( HTMLElementPredicate predicate, Object criteria ) throws SAXException {
         return getReceivedPage().getMatchingLinks( predicate, criteria );
     }
 
 
     /**
      * Returns the images found in the page in the order in which they appear.
      * @exception SAXException thrown if there is an error parsing the response.
      **/
     public WebImage[] getImages() throws SAXException {
         return getReceivedPage().getImages();
     }
 
 
     /**
      * Returns the image found in the page with the specified name attribute.
      * @exception SAXException thrown if there is an error parsing the response.
      **/
     public WebImage getImageWithName( String source ) throws SAXException {
         return getReceivedPage().getImageWithName( source );
     }
 
 
     /**
      * Returns the first image found in the page with the specified src attribute.
      * @exception SAXException thrown if there is an error parsing the response.
      **/
     public WebImage getImageWithSource( String source ) throws SAXException {
         return getReceivedPage().getImageWithSource( source );
     }
 
 
     /**
      * Returns the first image found in the page with the specified alt attribute.
      **/
     public WebImage getImageWithAltText( String altText ) throws SAXException {
         return getReceivedPage().getImageWithAltText( altText );
     }
 
 
     public WebApplet[] getApplets() throws SAXException {
         return getReceivedPage().getApplets();
     }
 
 
     /**
      * Returns a copy of the domain object model tree associated with this response.
      * If the response is HTML, it will use a special parser which can transform HTML into an XML DOM.
      * @exception SAXException thrown if there is an error parsing the response.
      **/
     public Document getDOM() throws SAXException {
         if (isHTML()) {
             return (Document) getReceivedPage().getDOM();
         } else {
             try {
                 return HttpUnitUtils.newParser().parse( new InputSource( new StringReader( getText() ) ) );
             } catch (IOException e) {
                 throw new SAXException( e );
             }
         }
     }
 
 
     /**
      * Returns the top-level tables found in this page in the order in which
      * they appear.
      * @exception SAXException thrown if there is an error parsing the response.
      **/
     public WebTable[] getTables() throws SAXException {
         return getReceivedPage().getTables();
     }
 
 
     /**
      * Returns the first table in the response which matches the specified predicate and value.
      * Will recurse into any nested tables, as needed.
      * @return the selected table, or null if none is found
      **/
     public WebTable getFirstMatchingTable( HTMLElementPredicate predicate, Object criteria ) throws SAXException {
         return getReceivedPage().getFirstMatchingTable( predicate, criteria );
     }
 
 
     /**
      * Returns the first table in the response which has the specified text as the full text of
      * its first non-blank row and non-blank column. Will recurse into any nested tables, as needed.
      * Case is ignored.
      * @exception SAXException thrown if there is an error parsing the response.
      * @return the selected table, or null if none is found
      **/
     public WebTable getTableStartingWith( String text ) throws SAXException {
         return getReceivedPage().getTableStartingWith( text );
     }
 
 
     /**
      * Returns the first table in the response which has the specified text as a prefix of the text of
      * its first non-blank row and non-blank column. Will recurse into any nested tables, as needed.
      * Case is ignored.
      * @exception SAXException thrown if there is an error parsing the response.
      * @return the selected table, or null if none is found
      **/
     public WebTable getTableStartingWithPrefix( String text ) throws SAXException {
         return getReceivedPage().getTableStartingWithPrefix( text );
     }
 
 
     /**
      * Returns the first table in the response which has the specified text as its summary attribute.
      * Will recurse into any nested tables, as needed.
      * Case is ignored.
      * @exception SAXException thrown if there is an error parsing the response.
      * @return the selected table, or null if none is found
      **/
     public WebTable getTableWithSummary( String text ) throws SAXException {
         return getReceivedPage().getTableWithSummary( text );
     }
 
 
     /**
      * Returns the first table in the response which has the specified text as its ID attribute.
      * Will recurse into any nested tables, as needed.
      * Case is ignored.
      * @exception SAXException thrown if there is an error parsing the response.
      * @return the selected table, or null if none is found
      **/
     public WebTable getTableWithID( String text ) throws SAXException {
         return getReceivedPage().getTableWithID( text );
     }
 
 
 //---------------------------------------- JavaScript methods ----------------------------------------
 
     /**
      * Returns the next javascript alert without removing it from the queue.
      * @deprecated use WebClient#getNextAlert. Will be removed in next release.
      */
     public String getNextAlert() {
         return _client.getNextAlert();
     }
 
 
     /**
      * Returns the next javascript alert and removes it from the queue.
      * @deprecated use WebClient#popNextAlert. Will be removed in next release.
      */
     public String popNextAlert() {
         return _client.popNextAlert();
     }
 
 
     public Scriptable getScriptableObject() {
         if (_scriptable == null) _scriptable = new Scriptable();
         return _scriptable;
     }
 
 
     public static ScriptableDelegate newDelegate( String delegateClassName ) {
         if (delegateClassName.equalsIgnoreCase( "Option" )) {
             return new SelectionFormControl.Option();
         } else {
             throw new IllegalArgumentException( "No such scripting class supported: " + delegateClassName );
         }
     }
 
 
    public class Scriptable extends ScriptableDelegate implements NamedDelegate {
 
         public void alert( String message ) {
             _client.postAlert( message );
         }
 
 
         public boolean getConfirmationResponse( String message ) {
             return _client.getConfirmationResponse( message );
         }
 
 
         public String getUserResponse( String prompt, String defaultResponse ) {
             return _client.getUserResponse( prompt, defaultResponse );
         }
 
 
         public ClientProperties getClientProperties() {
             return _client == null ? ClientProperties.getDefaultProperties() : _client.getClientProperties();
         }
 
 
         public HTMLPage.Scriptable getDocument() {
             try {
                 return getReceivedPage().getScriptableObject();
             } catch (SAXException e) {
                 throw new RuntimeException( e.toString() );
             }
         }
 
 
         public Scriptable[] getFrames() throws SAXException {
             String[] names = getFrameNames();
             Scriptable[] frames = new Scriptable[ names.length ];
             for (int i = 0; i < frames.length; i++) {
                 frames[i] = getSubframeContents( names[i] ).getScriptableObject();
             }
             return frames;
         }
 
 
         public void load() throws SAXException {
             final String[] scripts = getReceivedPage().getScripts();
             for (int i = 0; i < scripts.length; i++) {
                 runScript( scripts[i] );
             }
             doEvent( getReceivedPage().getOnLoadEvent() );
         }
 
 
         public Scriptable open( String urlString, String name, String features, boolean replace )
                 throws IOException, SAXException {
             if (urlString == null || urlString.trim().length() == 0) urlString = "about:";
             GetMethodWebRequest request = new GetMethodWebRequest( getURL(), urlString );
 
             WebWindow[] windows = _client.getOpenWindows();
             for (int i = 0; i < windows.length; i++) {
                 WebWindow window = windows[i];
                 if (window.getName().equals( name )) return window.getResponse( request ).getScriptableObject();
             }
 
             return _client.openInNewWindow( request, name, WebResponse.this ).getScriptableObject();
         }
 
 
         public void close() {
             if (getFrameName().equals( WebRequest.TOP_FRAME )) _window.close();
         }
 
 
         /**
          * Returns the value of the named property. Will return null if the property does not exist.
          **/
         public Object get( String propertyName ) {
             if (propertyName.equals( "name" )) {
                return getName();
             } else if (propertyName.equalsIgnoreCase( "top" )) {
                 return _window.getFrameContents( WebRequest.TOP_FRAME ).getScriptableObject();
             } else if (propertyName.equalsIgnoreCase( "parent" )) {
                return getFrameName().equals( WebRequest.TOP_FRAME ) ? this
                         : _window.getFrameContents( WebFrame.getParentFrameName( getFrameName() ) ).getScriptableObject();
             } else if (propertyName.equalsIgnoreCase( "opener" )) {
                 return getFrameName().equals( WebRequest.TOP_FRAME ) ? getScriptable( _window.getOpener() ) : null;
             } else if (propertyName.equalsIgnoreCase( "closed" )) {
                 return (getFrameName().equals( WebRequest.TOP_FRAME ) && _window.isClosed()) ? Boolean.TRUE : Boolean.FALSE;
             } else {
                 try {
                     return getSubframeContents( propertyName ).getScriptableObject();
                 } catch (NoSuchFrameException e) {
                     return super.get( propertyName );
                 }
             }
         }
 
 
        public String getName() {
            return getFrameName().equals( WebRequest.TOP_FRAME ) ? _window.getName() : getFrameName();
        }


         private Scriptable getScriptable( WebResponse opener ) {
             return opener == null ? null : opener.getScriptableObject();
         }
 
 
         /**
          * Sets the value of the named property. Will throw a runtime exception if the property does not exist or
          * cannot accept the specified value.
          **/
         public void set( String propertyName, Object value ) {
             if (propertyName.equals( "name" )) {
                 if (value == null) value = "";
                 if (getFrameName().equals( WebRequest.TOP_FRAME )) {
                     _window.setName( value.toString() );
                 }
             } else {
                 super.set( propertyName, value );
             }
         }
 
 
         public void setLocation( String relativeURL ) throws IOException, SAXException {
             getWindow().getResponse( new GetMethodWebRequest( _url, relativeURL, _frameName ) );
         }
 
 
         public URL getURL() {
             return WebResponse.this._url;
         }
     }
 
 
 //---------------------------------------- Object methods --------------------------------------------
 
     abstract
     public String toString();
 
 
 //----------------------------------------- protected members -----------------------------------------------
 
 
     /**
      * Constructs a response object.
      * @param frameName the name of the frame to hold the response
      * @param url the url from which the response was received
      **/
     protected WebResponse( WebClient client, String frameName, URL url ) {
         _client = client;
         _url = url;
         _frameName = frameName;
     }
 
 
     /**
      * Constructs a response object.
      * @param frameName the name of the frame to hold the response
      * @param url the url from which the response was received
      **/
     protected WebResponse( WebClient client, String frameName, URL url, String text ) {
         this( client, frameName, url );
         _responseText = text;
     }
 
 
     final
     protected void defineRawInputStream( InputStream inputStream ) throws IOException {
         if (_inputStream != null || _responseText != null) {
             throw new IllegalStateException( "Must be called before response text is defined." );
         }
 
         if (encodedUsingGZIP()) {
             _inputStream = new GZIPInputStream( inputStream );
         } else {
             _inputStream = inputStream;
         }
     }
 
 
     private boolean encodedUsingGZIP() {
         String encoding = getHeaderField( "Content-Encoding" );
         return encoding != null && encoding.indexOf( "gzip" ) >= 0;
     }
 
 
     /**
      * Overwrites the current value (if any) of the content type header.
      **/
     protected void setContentTypeHeader( String value ) {
         _contentHeader = value;
     }
 
 
 //------------------------------------------ package members ------------------------------------------------
 
     final static String      BLANK_HTML     = "";
     final static WebResponse BLANK_RESPONSE = new DefaultWebResponse( BLANK_HTML );
 
 
     WebWindow getWindow() {
         return _window;
     }
 
 
     void setWindow( WebWindow window ) {
         _window = window;
     }
 
 
     void replaceText( String text, String contentType ) {
         _responseText = text;
         _inputStream = null;
         _page = null;
         _contentType = contentType;
     }
 
 
     /**
      * Returns the frames found in the page in the order in which they appear.
      **/
     WebRequest[] getFrameRequests() throws SAXException {
         WebFrame[] frames = getFrames();
         Vector requests = new Vector();
         for (int i = 0; i < frames.length; i++) {
             if (frames[i].hasInitialRequest()) {
                 requests.addElement( frames[i].getInitialRequest() );
             }
         }
 
         WebRequest[] result = new WebRequest[ requests.size() ];
         requests.copyInto( result );
         return result;
     }
 
 
 //--------------------------------- private members --------------------------------------
 
 
     final private static String HTML_CONTENT = "text/html";
 
     final private static int UNINITIALIZED_INT = -2;
 
     private WebFrame[] _frames;
 
     private WebWindow _window;
 
     private HTMLPage _page;
 
     private String _contentHeader;
 
     private int _contentLength = UNINITIALIZED_INT;
 
     private String _contentType;
 
     private String _characterSet;
 
     private WebRequest _refreshRequest;
 
     private int _refreshDelay = -1;  // initialized to invalid value
 
     private String _responseText;
 
     private InputStream _inputStream;
 
 
     // the following variables are essentially final; however, the JDK 1.1 compiler does not handle blank final variables properly with
     // multiple constructors that call each other, so the final qualifiers have been removed.
 
     private URL    _url;
 
     private String _frameName;
 
     private WebClient _client;
 
     private Scriptable _scriptable;
 
 
     protected void loadResponseText() throws IOException {
         if (_responseText != null) throw new IllegalStateException( "May only invoke loadResponseText once" );
         _responseText = "";
 
         InputStream inputStream = getInputStream();
         try {
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             byte[] buffer = new byte[8 * 1024];
             int count = 0;
             do {
                 outputStream.write( buffer, 0, count );
                 count = inputStream.read( buffer, 0, buffer.length );
             } while (count != -1);
 
             byte[] bytes = outputStream.toByteArray();
             readTags( bytes );
             _responseText = new String( bytes, getCharacterSet() );
             _inputStream  = new ByteArrayInputStream( bytes );
 
             if (HttpUnitOptions.isCheckContentLength() && getContentLength() >= 0 && bytes.length != getContentLength()) {
                 throw new IOException("Truncated message. Expected length: " + getContentLength() +
                                                        ", Actual length: " + bytes.length);
             }
         } finally {
             inputStream.close();
         }
     }
 
 
     private void readTags( byte[] rawMessage ) throws UnsupportedEncodingException {
         ByteTagParser parser = new ByteTagParser( rawMessage );
         ByteTag tag = parser.getNextTag();
         while (tag != null && !tag.getName().equalsIgnoreCase( "body" )) {
             if (tag.getName().equalsIgnoreCase( "meta" )) processMetaTag( tag );
             if (tag.getName().equalsIgnoreCase( "frameset" )) _hasSubframes = true;
             tag = parser.getNextTag();
         }
     }
 
 
     private void processMetaTag( ByteTag tag ) {
         if (isHttpEquivMetaTag( tag, "content-type" )) {
             inferContentType( tag.getAttribute( "content" ) );
         } else if (isHttpEquivMetaTag( tag, "refresh" )) {
             inferRefreshHeader( tag.getAttribute( "content" ) );
         }
     }
 
 
     private boolean isHttpEquivMetaTag( ByteTag tag, String headerName )
     {
         return headerName.equalsIgnoreCase( tag.getAttribute( "http_equiv" ) ) ||
                headerName.equalsIgnoreCase( tag.getAttribute( "http-equiv" ) );
     }
 
 
     private void inferRefreshHeader( String refreshHeader ) {
         String originalHeader = getHeaderField( "Refresh" );
         if (originalHeader == null) {
             _refreshHeader = refreshHeader;
         }
     }
 
 
     private void readRefreshRequest() {
         if (_refreshDelay >= 0) return;
         _refreshDelay = 0;
         String refreshHeader = _refreshHeader != null ? _refreshHeader : getHeaderField( "Refresh" );
         if (refreshHeader == null) return;
 
         int splitIndex = refreshHeader.indexOf( ';' );
         if (splitIndex < 0) splitIndex = 0;
         try {
             _refreshDelay = Integer.parseInt( refreshHeader.substring( 0, splitIndex ) );
             _refreshRequest = new GetMethodWebRequest( _url, getRefreshURL( refreshHeader.substring( splitIndex+1 ) ), _frameName );
         } catch (NumberFormatException e) {
             _refreshDelay = 0;
             System.out.println( "Unable to interpret refresh tag: \"" + refreshHeader + '"' );
         }
     }
 
 
     private String getRefreshURL( String text ) {
         text = text.trim();
         if (!text.toUpperCase().startsWith( "URL" )) {
             return text;
         } else {
             int splitIndex = text.indexOf( '=' );
             return text.substring( splitIndex+1 ).trim();
         }
     }
 
 
     private void inferContentType( String contentTypeHeader ) {
         String originalHeader = getHeaderField( "Content-type" );
         if (originalHeader == null || originalHeader.indexOf( "charset" ) < 0) {
             setContentTypeHeader( contentTypeHeader );
         }
     }
 
 
     CookieJar getCookieJar() {
         if (_cookies == null) _cookies = new CookieJar( this );
         return _cookies;
     }
 
 
     private CookieJar _cookies;
 
     private void readContentTypeHeader() {
         String contentHeader = (_contentHeader != null) ? _contentHeader
                                                         : getHeaderField( "Content-type" );
         if (contentHeader == null) {
             _contentType = HttpUnitOptions.getDefaultContentType();
             setCharacterSet( HttpUnitOptions.getDefaultCharacterSet() );
             _contentHeader = _contentType + ";charset=" + _characterSet;
         } else {
             String[] parts = HttpUnitUtils.parseContentTypeHeader( contentHeader );
             _contentType = parts[0];
             if (parts[1] != null) setCharacterSet( parts[1] );
         }
     }
 
 
     private WebFrame[] getFrames() throws SAXException {
         if (_frames == null) {
             Vector list = new Vector();
             addFrameTags( list, "frame" );
             _frames = new WebFrame[ list.size() ];
             list.copyInto( _frames );
         }
 
         return _frames;
     }
 
 
     private void addFrameTags( Vector list, String frameTagName ) throws SAXException {
         NodeList nl = NodeUtils.getElementsByTagName( getReceivedPage().getOriginalDOM(), frameTagName );
         for (int i = 0; i < nl.getLength(); i++) {
             Node child = nl.item(i);
             list.addElement( new WebFrame( getReceivedPage().getBaseURL(), child, _frameName ) );
         }
     }
 
 
     private HTMLPage getReceivedPage() throws SAXException {
         if (_page == null) {
             try {
                 if (!isHTML()) throw new NotHTMLException( getContentType() );
                 _page = new HTMLPage( this, _url, _frameName, getCharacterSet() );
                 _page.parse( getText() );
             } catch (IOException e) {
                 e.printStackTrace();
                 throw new RuntimeException( e.toString() );
             }
         }
         return _page;
     }
 
 
     private static String _defaultEncoding;
 
     private final static String[] DEFAULT_ENCODING_CANDIDATES = { HttpUnitUtils.DEFAULT_CHARACTER_SET, "us-ascii", "utf-8", "utf8" };
 
     static String getDefaultEncoding() {
         if (_defaultEncoding == null) {
             for (int i = 0; i < DEFAULT_ENCODING_CANDIDATES.length; i++) {
                 try {
                     _defaultEncoding = DEFAULT_ENCODING_CANDIDATES[i];
                     "abcd".getBytes( _defaultEncoding );   // throws an exception if the encoding is not supported
                     return _defaultEncoding;
                 } catch (UnsupportedEncodingException e) {
                 }
             }
         }
         return (_defaultEncoding = System.getProperty( "file.encoding" ));
     }
 
 
     private void setCharacterSet( String characterSet ) {
         if (characterSet == null) return;
 
         try {
             "abcd".getBytes( characterSet );
             _characterSet = characterSet;
         } catch (UnsupportedEncodingException e) {
             _characterSet = getDefaultEncoding();
         }
     }
 
 
     boolean hasSubframes() {
         return _hasSubframes;
     }
 
 
 
 //=======================================================================================
 
     class ByteTag {
 
         ByteTag( byte[] buffer, int start, int length ) throws UnsupportedEncodingException {
             _buffer = new String( buffer, start, length, WebResponse.getDefaultEncoding() ).toCharArray();
             _name = nextToken();
 
             String attribute = "";
             String token = nextToken();
             while (token.length() != 0) {
                 if (token.equals( "=" ) && attribute.length() != 0) {
                     getAttributes().put( attribute.toLowerCase(), nextToken() );
                     attribute = "";
                 } else {
                     if (attribute.length() > 0) getAttributes().put( attribute.toLowerCase(), "" );
                     attribute = token;
                 }
                 token = nextToken();
             }
         }
 
 
         public String getName() {
             return _name;
         }
 
         public String getAttribute( String attributeName ) {
             return (String) getAttributes().get( attributeName );
         }
 
         public String toString() {
             return "ByteTag[ name=" + _name + ";attributes = " + _attributes + ']';
         }
 
 
         private Hashtable getAttributes() {
             if (_attributes == null) _attributes = new Hashtable();
             return _attributes;
         }
 
 
         private String _name = "";
         private Hashtable _attributes;
 
 
         private char[] _buffer;
         private int    _start;
         private int    _end = -1;
 
 
         private String nextToken() {
             _start = _end+1;
             while (_start < _buffer.length && Character.isWhitespace( _buffer[ _start ] )) _start++;
             if (_start >= _buffer.length) {
                 return "";
             } else if (_buffer[ _start ] == '"') {
                 for (_end = _start+1; _end < _buffer.length && _buffer[ _end ] != '"'; _end++);
                 return new String( _buffer, _start+1, _end-_start-1 );
             } else if (_buffer[ _start ] == '\'') {
                 for (_end = _start+1; _end < _buffer.length && _buffer[ _end ] != '\''; _end++);
                 return new String( _buffer, _start+1, _end-_start-1 );
             } else if (_buffer[ _start ] == '=') {
                 _end = _start;
                 return "=";
             } else {
                 for (_end = _start+1; _end < _buffer.length && _buffer[ _end ] != '=' && !Character.isWhitespace( _buffer[ _end ] ); _end++);
                 return new String( _buffer, _start, (_end--)-_start );
             }
         }
     }
 
 
 //=======================================================================================
 
 
     class ByteTagParser {
 
         ByteTagParser( byte[] buffer ) {
             _buffer = buffer;
         }
 
 
         ByteTag getNextTag() throws UnsupportedEncodingException {
             _start = _end+1;
             while (_start < _buffer.length && _buffer[ _start ] != '<') _start++;
             for (_end =_start+1; _end < _buffer.length && _buffer[ _end ] != '>'; _end++);
             if (_end >= _buffer.length || _end < _start) return null;
             return new ByteTag( _buffer, _start+1, _end-_start-1 );
         }
 
 
         private int _start = 0;
         private int _end   = -1;
 
         private byte[] _buffer;
     }
 
 
 }
 
 
 //=======================================================================================
 
 
 class DefaultWebResponse extends WebResponse {
 
 
     DefaultWebResponse( String text ) {
         this( null, null, text );
     }
 
 
     DefaultWebResponse( WebClient client, URL url, String text ) {
         this( client, WebRequest.TOP_FRAME, url, text );
     }
 
 
     DefaultWebResponse( WebClient client, String target, URL url, String text ) {
         super( client, target, url, text );
     }
 
 
     /**
      * Returns the response code associated with this response.
      **/
     public int getResponseCode() {
         return HttpURLConnection.HTTP_OK;
     }
 
 
     /**
      * Returns the response message associated with this response.
      **/
     public String getResponseMessage() {
         return "OK";
     }
 
 
     public String[] getHeaderFieldNames() {
         return new String[] { "Content-type" };
     }
 
 
     /**
      * Returns the value for the specified header field. If no such field is defined, will return null.
      **/
     public String getHeaderField( String fieldName ) {
         if (fieldName.equalsIgnoreCase( "Content-type" )) {
             return "text/html; charset=us-ascii";
         } else {
             return null;
         }
     }
 
 
     public String[] getHeaderFields( String fieldName ) {
         String value = getHeaderField( fieldName );
         return value == null ? new String[0] : new String[]{ value };
     }
 
 
     public String toString() {
         try {
             return "DefaultWebResponse [" + getText() + "]";
         } catch (IOException e) { // should never happen
             return "DefaultWebResponse [???]";
         }
     }
 }
 
