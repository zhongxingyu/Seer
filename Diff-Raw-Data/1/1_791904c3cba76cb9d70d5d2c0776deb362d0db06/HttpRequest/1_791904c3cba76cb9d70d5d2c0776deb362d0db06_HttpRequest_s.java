 package com.redshape.servlet.core;
 
 import net.sf.json.JSONObject;
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.log4j.Logger;
 
 import javax.mail.MessagingException;
 import javax.mail.internet.MimeBodyPart;
 import javax.mail.internet.MimeMultipart;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletRequestWrapper;
 import java.io.*;
 import java.net.URLDecoder;
 import java.util.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: nikelin
  * Date: 10/10/10
  * Time: 11:56 PM
  * To change this template use File | Settings | File Templates.
  */
 public class HttpRequest extends HttpServletRequestWrapper implements IHttpRequest {
     @SuppressWarnings("unused")
     private static final Logger log = Logger.getLogger( HttpRequest.class );
 
 	public static final String MULTIPART_TYPE = "multipart/form-data";
 
     private boolean initialized;
     private String controller;
     private String action;
     private String requestData;
 
     private IMultipartRequest multipart;
 
     private Map<String, Object> parameters = new HashMap<String, Object>();
     private Map<String, Cookie> cookiesMap = new HashMap<String, Cookie>();
 
     public HttpRequest( HttpServletRequest request ) {
         super(request);
 
 		this.init();
         this.initCookies();
     }
 
 	protected void init() {
 		try {
 			if ( this.isMultiPart() ) {
 				this.multipart = new MultipartRequest(this);
 			}
 		} catch ( Throwable e ) {
 			throw new IllegalStateException( e.getMessage(), e);
 		}
 	}
 
     protected void initCookies() {
         if ( this.getCookies() == null ) {
             return;
         }
 
         for ( Cookie cookie : this.getCookies() ) {
             this.cookiesMap.put( cookie.getName(), cookie );
         }
     }
 
     protected JSONObject readJSONRequest( HttpServletRequest request )
             throws IOException {
         String requestData = this.readRequest();
 
         if ( requestData.isEmpty() ) {
             throw new IllegalArgumentException("Request is empty");
         }
 
         return this.readJSONRequest( requestData );
     }
 
     protected JSONObject readJSONRequest(String data) {
         return JSONObject.fromObject(data);
     }
 
 	@Override
 	public void setParameter(String name, Object value) {
 		this.parameters.put( name, value );
 	}
 
 	@Override
     public boolean hasParameter(String name) throws IOException {
         this.initParameters();
 
         return this.parameters.containsKey(name);
     }
 
 	public boolean isMultiPart() {
 		return this.getContentType() != null && this.getContentType().contains( MULTIPART_TYPE );
 	}
 
 	private void initMultipartParameters() {
 		Iterator parameterNames = this.getMultipart().getParameterNames();
 		while ( parameterNames.hasNext() ) {
 			String parameterName = (String) parameterNames.next();
 			this.parameters.put( parameterName, this.getMultipart().getParameter(parameterName) );
 		}
 	}
 
 	protected void copyBaseParams() {
 		Enumeration<String> parameterNames = super.getParameterNames();
 		while ( parameterNames.hasMoreElements() ) {
 			String parameterName = parameterNames.nextElement();
 			String[] values = super.getParameterValues(parameterName);
 			if ( values.length == 1 && !parameterName.endsWith("[]") ) {
 				this.parameters.put( parameterName, values[0] );
 			} else {
 				this.parameters.put( parameterName, Arrays.asList(values) );
 			}
 		}
 	}
 
     protected void initParameters() throws IOException {
         if ( this.initialized ) {
             return;
         }
 
 		this.copyBaseParams();
 
 		if ( this.isMultiPart() ) {
 			this.initMultipartParameters();
 		}
 
         String data = this.readRequest();
         if ( data == null || data.isEmpty() ) {
             data = this.getQueryString();
 			if ( data == null || data.isEmpty() ) {
 				return;
 			}
         }
 
         if ( data.startsWith("{") && data.endsWith("}") ) {
             JSONObject object = this.readJSONRequest( data );
             for ( Object key : object.keySet() ) {
                 this.parameters.put( String.valueOf( key ), object.get(key) );
             }
         } else {
             if ( this.parameters == null ) {
                 this.parameters = new HashMap<String, Object>();
             }
 
             for (String param : data.split("&")) {
                 String[] paramParts = param.split("=");
 
                 String value = paramParts.length > 1 ? paramParts[1] : null;
                 String name = URLDecoder.decode( paramParts[0] );
                 if ( name.endsWith("[]") ) {
                     name = name.replace("[]", "");
                     if ( !this.parameters.containsKey(name) ) {
                         this.parameters.put( name, new ArrayList<Object>() );
                     }
 
                     ( (List<Object> ) this.parameters.get(name) ).add( value );
                 } else {
                     this.parameters.put( name, value != null ? StringEscapeUtils.escapeHtml( URLDecoder.decode( value, "UTF-8" ) ) : null );
                 }
             }
         }
 
         this.initialized = true;
     }
 
     @Override
     public <T> T getObjectParameter( String name ) throws IOException {
 		if ( name == null ) {
 			throw new IllegalArgumentException("<null>");
 		}
 
 		if ( !name.endsWith("[]") ) {
 			String data = super.getParameter(name);
 			if ( data != null ) {
 				return (T) data;
 			}
 		}
 
         this.initParameters();
 
         return (T) this.parameters.get( name );
     }
 
     @Override
     public String getParameter(String name ) {
         try {
             return String.valueOf( this.<Object>getObjectParameter(name) );
         } catch ( IOException e ) {
             throw new IllegalArgumentException( "Unable to grab parameter value", e );
         }
     }
 
     @Override
     public String getCookie( String name ) {
         Cookie cookie = this.cookiesMap.get( name );
         if ( cookie == null ) {
             return null;
         }
 
         return cookie.getValue();
     }
 
     protected synchronized  String readRequest() throws IOException {
         if ( this.requestData != null ) {
             return this.requestData;
         }
 
         StringBuffer data = new StringBuffer();
         String buff;
         InputStreamReader reader = new InputStreamReader( this.getInputStream() );
 
         BufferedReader buffer = new BufferedReader( reader );
         buffer.skip(0);
         while (null != (buff = buffer.readLine())) {
             data.append(buff);
         }
 
 		this.requestData = data.toString();
 		return this.requestData = this.requestData.isEmpty() ? this.getBody() : this.requestData;
     }
 
     @Override
     public synchronized IMultipartRequest getMultipart() {
 		if ( !this.isMultiPart() ) {
 			throw new IllegalStateException("Request is not multipart type");
 		}
 
 		return this.multipart;
     }
 
     @Override
     public synchronized byte[] getFileContent( String name )
             throws IOException {
         IMultipartRequest multiPart = this.getMultipart();
         MultipartRequest.FileInfo fileInfo = multiPart.getFileInfo(name);
         if ( null == fileInfo ) {
             return new byte[0];
         }
 
         return fileInfo.getContent();
     }
 
     public boolean isPost() {
         return this.getMethod().equals("POST");
     }
 
     public void setController( String name ) {
         this.controller = name;
     }
 
     public String getController() {
         return this.controller;
     }
 
     public void setAction( String name ) {
         this.action = name;
     }
 
     public String getAction() {
         return this.action;
     }
 
     @Override
     public void setParameters(Map<String, Object> parameters) {
         this.parameters = parameters;
         this.initialized = true;
     }
 
     @Override
     public Map<String, Object> getParameters() {
         try {
             this.initParameters();
         } catch ( IOException e ) {}
 
         return this.parameters;
     }
 
     @Override
     public String getBody() throws IOException {
         return this.readRequest();
     }
 
 
     public class MultipartRequest implements IMultipartRequest {
         private MimeMultipart mimeparts;  // To hold the parsed results
         private HashMap params = new HashMap();  // To hold the uploaded parameters
         private HashMap files = new HashMap();  // To hold the uploaded files
         private byte [] buf = new byte[8096]; // Scratch buffer space
 
         /**
          * This private inner class is a simple implementation of the
          * DataSource interface. It provides the bridge between the
          * HttpServletRequest and the Java Mail classes.
          */
         private class Source implements javax.activation.DataSource{
             private InputStream stream;
             private String mimetype;
 
             Source(HttpServletRequest req) throws IOException {
                 stream = req.getInputStream();
                 mimetype = req.getHeader("CONTENT-TYPE");
             }
 
             public InputStream getInputStream(){ return stream; }
             public String getContentType(){ return mimetype; }
             public OutputStream getOutputStream(){throw new RuntimeException();} // Not used
             public String getName(){throw new RuntimeException();} // Not used
         };
 
         /**
          * This public inner class is used to store information about uploaded
          * files.  Users of the MultipartRequest class should generally
          * refer to this class as MultipartRequest.FileInfo (as per usual
          * Java rules).
          */
         public class FileInfo {
             private byte[] content;  // The byte-copy of the file's contents
             private String sourcename; // The name of the file on the browser's system
             private String contentType; // The mimetype supplied by the browser
             public FileInfo(byte [] content, String sourcename, String contentType){
                 this.content = content;
                 this.sourcename = sourcename;
                 this.contentType = contentType;
             }
             public byte [] getContent(){ return content; }
             public String getSourceFilename(){ return sourcename; }
             public String getContentType(){ return contentType; }
             public void setSourceFileName(String fileName) { this.sourcename = fileName; };
         }
 
         /**
          * The constructor. This accepts an HttpServletRequest (which it assumes to be
          * from a post of a ENCTYPE="multipart/form-data" form) and parses all the
          * information into a MimeMultipart object.
          * <p>
          * It then iterates through that parsed object, extracting the parameters and
          * files from it for the user.
          *
          * @param req a request from a form post with ENCTYPE="multipart/form-data".
          * @throws MessagingException if there are problems with parsing the MIME
          *      information.
          * @throws IOException if there are problems reading the input stream.
          */
         public MultipartRequest(HttpServletRequest req) throws MessagingException, IOException {
             // Here's the line which does all of the parsing.
             // The request size and content type could be checked before calling, if desired.
             mimeparts = new MimeMultipart( new Source(req) );
 
             // Now iterate over the parsed results
             int partCount = mimeparts.getCount();
             for(int i=0; i<partCount; ++i){
                 MimeBodyPart bp = (MimeBodyPart) mimeparts.getBodyPart(i);
                 String disposition = bp.getHeader("Content-Disposition","");
                 // I use the filename to indicate if this is a file or a parameter.
                 // Could instead use bp.getContent().getClass() to indicate if we
                 // have a String, an InputStream, or a (nested) MultiPart.
                 String filename = bp.getFileName(); // This filename appears to lack "\" chars.
                 if( filename == null ) doParameter(bp, disposition);
                 else doFile(bp, disposition);
             }
         }
 
         /**
          * @return an iterator for the parameter names, as per servlet 2.2 spec
          *      except for using Iterator rather than Enumeration.
          */
         @Override
         public Iterator getParameterNames(){
             return params.keySet().iterator();
         }
 
         /**
          * Return the (only) parameter with the given name, or null
          * if no such parameters exist. If there are more than one
          * parameters with this name, return the first (per servlet 2.2 API)
          *
          * @param name the HTML name of the input field for the parameter
          * @return the value of the parameter with the given name
          */
         @Override
         public String getParameter(String name){
             List valuelist = (List) params.get(name);
             if( valuelist == null ) return null;
             return (String) valuelist.get( 0 );  // Return first value, as per servlet 2.2 API
         }
 
         /**
          * Return an array of all the parameters with the given name,
          * or null if no parameters with this name exist.
          *
          * @param name the HTML name of the input field for the parameter
          * @return the array of values of parameters with this name.
          */
         @Override
         public String [] getParameterValues(String name){
             List valuelist = (List) params.get(name);
             if( valuelist == null ) return null;
             return (String[]) valuelist.toArray( new String[valuelist.size()] );
         }
 
         /**
          * @return an Iterator for the FileInfo items describing the
          * files encapsulated in the request.
          */
         @Override
         public Iterator getFileInfoNames(){
             return files.keySet().iterator();
         }
 
         /**
          * Return the (only) FileInfo object describing the uploaded
          * files with a given HTML name, or null if no such name exists
          * in the request.  If there are several files uploaded under the
          * name, return the first.
          *
          * @param name the HTML name of the input field for the file
          * @return the FileInfo object for the file.
          */
         @Override
         public FileInfo getFileInfo(String name){
             List filelist = (List) files.get(name);
             if( filelist == null ) return null;
             return (FileInfo) filelist.get( 0 );
         }
 
         /**
          * Return an array of all the FileInfo objects representing the
          * files uploaded under the given HTML name, or null if no such
          * name exists.
          *
          * @param name the HTML name of the input field for the files
          * @return the array of FileInfo objects for files uploaded
          *      under this name.
          */
         @Override
         public FileInfo [] getFileInfoValues(String name){
             List filelist = (List) files.get(name);
             if( filelist == null ) return null;
             return (FileInfo[]) filelist.toArray( new FileInfo[filelist.size()] );
         }
 
         /**
          * Do whatever processing is needed for a parameter.
          */
         private void doParameter(MimeBodyPart bp, String disposition) throws MessagingException, IOException {
             String name = findValue("name", disposition);
             String value = (String) bp.getContent();
             List valuelist = (List) params.get(name);
             if( valuelist==null ){
                 valuelist = new LinkedList();
                 params.put(name, valuelist);
             }
             valuelist.add(value);
         }
 
         /**
          * Do whatever processing is needed for a file.
          */
         private void doFile(MimeBodyPart bp, String disposition) throws MessagingException, IOException {
             String name = findValue("name", disposition);
             String filename = findValue("filename", disposition);
             if ( filename != null ) filename = new File(filename).getName();
             BufferedInputStream in = new  BufferedInputStream(bp.getInputStream());
             ByteArrayOutputStream out = new ByteArrayOutputStream( in.available() );
             int k;
             while( (k=in.read(buf)) != -1 ) out.write(buf,0,k);
             out.close();
             FileInfo f = new FileInfo(out.toByteArray(), filename, bp.getContentType());
             List filelist = (List) files.get(name);
             if( filelist==null ){
                 filelist = new LinkedList();
                 files.put(name, filelist);
             }
             filelist.add(f);
         }
 
         /**
          * Utiltity to extract a parameter value from a header line, since the
          * Java library routines don't seem to let us do that.
          */
         private String findValue(String parm, String header){
             StringTokenizer st = new StringTokenizer(header, "; =");
             while( st.hasMoreTokens() ){
                 String token = st.nextToken();
                 if( token.equalsIgnoreCase(parm) ){
                     try { return st.nextToken("\"="); }
                     catch( NoSuchElementException e ){ return ""; } // e.g. filename=""
                 }
             }
             return null;
         }
     }
 
 }
