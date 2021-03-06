 // HttpResponse.java
 
 package ed.net.httpserver;
 
 import java.io.*;
 import java.util.*;
 import java.text.*;
 import java.nio.*;
 import java.nio.channels.*;
 import java.nio.charset.*;
 
 import ed.js.*;
 import ed.util.*;
 
 public class HttpResponse {
 
     static final boolean USE_POOL = true;
     static final String DEFAULT_CHARSET = "utf-8";
 
     public static final DateFormat HeaderTimeFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
     static {
 	HeaderTimeFormat.setTimeZone( TimeZone.getTimeZone("GMT") );
     }
 
 
     HttpResponse( HttpRequest request ){
         _request = request;
         _handler = _request._handler;
 
         _headers = new StringMap<String>();
         _headers.put( "Content-Type" , "text/html;charset=" + getContentEncoding() );
         _headers.put( "Server" , "ED" );
 	setDateHeader( "Date" , System.currentTimeMillis() );
     }
 
     public void setResponseCode( int rc ){
         if ( _sentHeader )
             throw new RuntimeException( "already sent header " );
         _responseCode = rc;
     }
 
     public int getResponseCode(){
         return _responseCode;
     }
 
     public void addCookie( String name , String value , int maxAge ){
         _cookies.add( new Cookie( name , value , maxAge ) );
     }
 
     public void addCookie( String name , String value ){
         _cookies.add( new Cookie( name , value ) );
     }
     
     public void removeCookie( String name ){
        _cookies.add( new Cookie( name , "asd" , -1 ) );
     }
 
     public void setCacheTime( int seconds ){
 	setHeader("Cache-Control" , "max-age=" + seconds );
 	setDateHeader( "Expires" , System.currentTimeMillis() + ( 1000 * seconds ) );
     }
     
     public void setDateHeader( String n , long t ){
 	synchronized( HeaderTimeFormat ) {
 	    setHeader( n , HeaderTimeFormat.format( new Date(t) ) );
 	}
     }
 
     public void setHeader( String n , String v ){
         _headers.put( n , v );
     }
 
     void cleanup(){
         if ( _cleaned )
             return;
         
         _handler._done = ! keepAlive();
         
         _cleaned = true;
         if ( _myStringContent != null ){
 
             for ( ByteBuffer bb : _myStringContent ){
                 if ( USE_POOL )
                     _bbPool.done( bb );
             }
             
             _myStringContent.clear();
             _myStringContent = null;
         }
         
         if ( _writer != null ){
             _charBufPool.done( _writer._cur );
             _writer._cur = null;
             _writer = null;
         }
         
     }
     
     public boolean done()
         throws IOException {
 
         if ( _cleaned )
             return true;
 
         _done = true;
         boolean f = flush();
         if ( f )
             cleanup();
         return f;
     }
 
     private boolean flush()
         throws IOException {
         return _flush();
     }
     
     private boolean _flush()
         throws IOException {
 
         if ( _cleaned )
             throw new RuntimeException( "already cleaned" );
         
         if ( _numDataThings() > 1 )
             throw new RuntimeException( "too much data" );
 
 
         if ( ! _sentHeader ){
             final String header = _genHeader();
             
             ByteBuffer headOut = ByteBuffer.allocateDirect( 1024 );
             headOut.put( header.getBytes() );
             headOut.flip();
             _handler.getChannel().write( headOut );
             _sentHeader = true;
         }
         
         if ( _file != null ){
             if ( _fileChannel == null )
                 _fileChannel = (new FileInputStream(_file)).getChannel();
             
             try {
                 _fileSent += _fileChannel.transferTo( _fileSent , Long.MAX_VALUE , _handler.getChannel() );
             }
             catch ( IOException ioe ){
                 if ( ioe.toString().indexOf( "Resource temporarily unavailable" ) < 0 )
                     throw ioe;
             }
             if ( _fileSent < _file.length() ){
                 if ( HttpServer.D ) System.out.println( "only sent : " + _fileSent );
                 _handler.registerForWrites();
                 return false;
             }
         }
 
         if ( _writer != null )
             _writer._push();
         
         if ( _stringContent != null ){
             for ( ; _stringContentSent < _stringContent.size() ; _stringContentSent++ ){
                 
                 ByteBuffer bb = _stringContent.get( _stringContentSent );
                 _stringContentPos += _handler.getChannel().write( bb );
                 if ( _stringContentPos < bb.limit() ){
                     if ( HttpServer.D ) System.out.println( "only wrote " + _stringContentPos + " out of " + bb );
                     _handler.registerForWrites();
                     return false;
                 }
                 _stringContentPos = 0;
             }
         }
 
         if ( _jsfile != null ){
             if ( ! _jsfile.write( _handler.getChannel() ) ){
                 _handler.registerForWrites();
                 return false;
             }
         }
         
         cleanup();
         
         if ( keepAlive() && ! _handler.hasData() )
             _handler.registerForReads();
         else 
             _handler.registerForWrites();
 
         return true;
     }
 
     private String _genHeader()
         throws IOException {
         StringBuilder buf = _headerBufferPool.get();
         _genHeader( buf );
         String header = buf.toString();
         _headerBufferPool.done( buf );
         return header;
     }
     
     private Appendable _genHeader( Appendable a )
         throws IOException {
         // first line
         a.append( "HTTP/1.1 " );
         {
             String rc = String.valueOf( _responseCode );
             a.append( rc ).append( " " );
             Object msg = _responseMessages.get( rc );
             if ( msg == null )
                 a.append( "OK" );
             else
                 a.append( msg.toString() );
             a.append( "\n" );
         }
 
         // headers
         if ( _headers != null ){
             for ( Map.Entry<String,String> v : _headers.entrySet() ){
                 a.append( v.getKey() );
                 a.append( ": " );
                 a.append( v.getValue() );
                 a.append( "\r\n" );
             }
         }
 
         // cookies
         for ( Cookie c : _cookies ){
             a.append( "Set-Cookie: " );
             a.append( c._name ).append( "=" ).append( c._value ).append( ";" );
 	    a.append( " " ).append( "Path=" ).append( c._path ).append( ";" );
             String expires = c.getExpires();
             if ( expires != null )
                 a.append( "Expires=" ).append( expires ).append( "; " );
             a.append( "\r\n" );
         }
         
         if ( keepAlive() )
             a.append( "Connection: keep-alive\r\n" );
         else
             a.append( "Connection: close\r\n" );
 
         if ( _writer != null )
             _writer._push();
 
         // need to only do this if not chunked
         if ( _done && _headers.get( "Content-Length") == null ){
             
             if ( _stringContent != null ){
                 int cl = 0;
                 for ( ByteBuffer buf : _stringContent )
                     cl += buf.limit();
                 if ( HttpServer.D ) System.out.println( "_stringContent.length : " + cl );
                 a.append( "Content-Length: " ).append( String.valueOf( cl ) ).append( "\r\n" );
             }
             else if ( _numDataThings() == 0 ) {
                 a.append( "Content-Length: 0\r\n" );
             }
             
         }
         
         // empty line
         a.append( "\r\n" );
         return a;
     }
     
     public String toString(){
         try {
             return _genHeader();
         }
         catch ( Exception e ){
             throw new RuntimeException( e );
         }
     }
 
     public JxpWriter getWriter(){
         if ( _writer == null ){
             if ( _cleaned )
                 throw new RuntimeException( "already cleaned" );
             
             _writer = new MyJxpWriter();
         }
         return _writer;
     }
 
     public void setData( ByteBuffer bb ){
         _stringContent = new LinkedList<ByteBuffer>();
         _stringContent.add( bb );
     }
 
     public boolean keepAlive(){
         if ( ! _request.keepAlive() )
             return false;
 
         if ( _headers.get( "Content-Length" ) != null )
             return true;
 
         if ( _stringContent != null ){
             // TODO: chunking
             return _done;
         }
 
         return false;
     }
 
     public String getContentEncoding(){
         return DEFAULT_CHARSET;
     }
 
     public void sendFile( File f ){
         if ( ! f.exists() )
             throw new IllegalArgumentException( "file doesn't exist" );
         _file = f;
         _headers.put( "Content-Length" , String.valueOf( f.length() ) );
 	_stringContent = null;
     }
 
     public void sendFile( JSFile f ){
         if ( f instanceof JSLocalFile ){
             sendFile( ((JSLocalFile)f).getRealFile() );
             return;
         }
         
         long length = f.getLength();
         _jsfile = f.sender();
         _stringContent = null;
 
         long range[] = _request.getRange();
         if ( range != null && ( range[0] > 0 || range[1] < length ) ){
             
             if ( range[1] > length )
                 range[1] = length;
             
             try {
                 _jsfile.skip( range[0] );
             }
             catch ( IOException ioe ){
                 throw new RuntimeException( "can't skip " , ioe );
             }
 
             _jsfile.maxPosition( range[1] + 1 );
 
             setResponseCode( 206 );
             setHeader( "Content-Range" , "bytes " + range[0] + "-" + range[1] + "/" + length );
             setHeader( "Content-Length" , String.valueOf(  1 + range[1] - range[0] ) );
             System.out.println( "got range " + range[0] + " -> " + range[1] );
             
 
             return;
         }
         _headers.put( "Content-Length" , String.valueOf( f.getLength() ) );
         _headers.put( "Content-Type" , f.getContentType() );
 
     }
     
     private int _numDataThings(){
         int num = 0;
 
         if ( _stringContent != null )
             num++;
         if ( _file != null )
             num++;
         if ( _jsfile != null )
             num++;
 
         return num;
     }
 
     private boolean _hasData(){
         return _numDataThings() > 0;
     }
 
     private void _checkNoContent(){
         if ( _hasData() )
             throw new RuntimeException( "already have data set" );
     }
 
     private void _checkContent(){
         if ( ! _hasData() )
             throw new RuntimeException( "no data set" );
     }
     
     final HttpRequest _request;
     final HttpServer.HttpSocketHandler _handler;
     
     // header
     int _responseCode = 200;
     Map<String,String> _headers;
     List<Cookie> _cookies = new ArrayList<Cookie>();
     boolean _sentHeader = false;
 
     // data
     List<ByteBuffer> _stringContent = null;
     private List<ByteBuffer> _myStringContent = null; // tihs is the real one
     
     int _stringContentSent = 0;
     int _stringContentPos = 0;
 
     File _file;
     FileChannel _fileChannel;
     long _fileSent = 0;
 
     boolean _done = false;
     boolean _cleaned = false;
     MyJxpWriter _writer = null;
     
     JSFile.Sender _jsfile;
     
     class MyJxpWriter implements JxpWriter {
         MyJxpWriter(){
             _checkNoContent();
 
             _myStringContent = new LinkedList<ByteBuffer>();
             _stringContent = _myStringContent;
 
             _cur = _charBufPool.get();
             _resetBuf();
         }
 
         public JxpWriter print( int i ){
             return print( String.valueOf( i ) );
         }
         
         public JxpWriter print( double d ){
             return print( String.valueOf( d ) );
         }
 
         public JxpWriter print( long l ){
             return print( String.valueOf( l ) );
         }
         
         public JxpWriter print( boolean b ){
             return print( String.valueOf( b ) );
         }
         
         public JxpWriter print( String s ){
             if ( _done )
                 throw new RuntimeException( "already done" );
             
             if ( _cur.position() + ( 3 * s.length() ) > _cur.capacity() ){
                 _push();
             }
             
             _cur.append( s );
             _javaLength += s.length();
             return this;
         }
 
         void _push(){
             if ( _cur.position() == 0 )
                 return;
             
             _cur.flip();
             ByteBuffer bb = USE_POOL ? _bbPool.get() : ByteBuffer.wrap( new byte[ _cur.limit() * 2 ] );
             if ( bb.position() != 0 || bb.limit() != bb.capacity() )
                 throw new RuntimeException( "something is wrong with _bbPool" );
             
             CharsetEncoder encoder = _defaultCharset.newEncoder(); // TODO: pool
             try {
                 CoderResult cr = encoder.encode( _cur , bb , true );
                 if ( cr.isUnmappable() )
                     throw new RuntimeException( "can't map some character" );
                 if ( cr.isOverflow() )
                     throw new RuntimeException( "buffer overflow here is a bad thing.  bb after:" + bb );
 
                 bb.flip();
 
                 _myStringContent.add( bb );
                 _resetBuf();
             }
             catch ( Exception e ){
                 throw new RuntimeException( "no" , e );
             }
         }
         
         public void flush()
             throws IOException {
             _flush();
         }
 
         public void reset(){
             _myStringContent.clear();
             _resetBuf();
         }
 
         public String getContent(){
             throw new RuntimeException( "not implemented" );
         }
 
         void _resetBuf(){
             _cur.position( 0 );
             _cur.limit( _cur.capacity() );
         }
         
         public int getJavaLength(){
             return _javaLength;
         }
 
         public String toString(){
             return "java length : " + _javaLength;
         }
 
         public void mark( int m ){
             _mark = m;
         }
 
         public void clearToMark(){
             throw new RuntimeException( "not implemented yet" );
         }
         
         public String fromMark(){
             throw new RuntimeException( "not implemented yet" );
         }
         
         private int _javaLength = 0;
         private CharBuffer _cur;
         private int _mark = 0;
     }
     
     static final int CHAR_BUFFER_SIZE = 1024 * 32;
     static SimplePool<CharBuffer> _charBufPool = new SimplePool<CharBuffer>( "Response.CharBufferPool" , 50 , -1 ){
             public CharBuffer createNew(){
                 return CharBuffer.allocate( CHAR_BUFFER_SIZE );
             }
             
             public boolean ok( CharBuffer buf ){
                 buf.position( 0 );
                 buf.limit( buf.capacity() );
                 return true;
             }
         };
     static ByteBufferPool _bbPool = new ByteBufferPool( 50 , CHAR_BUFFER_SIZE * 4  );
     static StringBuilderPool _headerBufferPool = new StringBuilderPool( 25 , 1024 );
     static Charset _defaultCharset = Charset.forName( DEFAULT_CHARSET );
 
     static final Properties _responseMessages = new Properties();
     static {
         try {
             _responseMessages.load( ClassLoader.getSystemClassLoader().getResourceAsStream( "ed/net/httpserver/responseCodes.properties" ) );
         }
         catch ( IOException ioe ){
             throw new RuntimeException( ioe );
         }
     }
     
 }
