 package javax.microedition.io;
 
 import java.io.*;
 import java.net.HttpURLConnection;
 
 
 final class WrappedHttpConnection implements HttpConnection
     {
     WrappedHttpConnection( final HttpURLConnection aConnection )
         {
         myConnection = aConnection;
         }
 
     // From HttpConnection
 
     public final long getDate() throws IOException
         {
         throw new RuntimeException( "NYI" );
         }
 
     public final long getExpiration() throws IOException
         {
         throw new RuntimeException( "NYI" );
         }
 
     public final String getFile()
         {
         throw new RuntimeException( "NYI" );
         }
 
     public final String getHeaderField( final String aString ) throws IOException
         {
         throw new RuntimeException( "NYI" );
         }
 
     public final String getHeaderField( final int i ) throws IOException
         {
         throw new RuntimeException( "NYI" );
         }
 
     public final long getHeaderFieldDate( final String aString, final long l ) throws IOException
         {
         throw new RuntimeException( "NYI" );
         }
 
     public final int getHeaderFieldInt( final String aString, final int i ) throws IOException
         {
         throw new RuntimeException( "NYI" );
         }
 
     public final String getHeaderFieldKey( final int i ) throws IOException
         {
         throw new RuntimeException( "NYI" );
         }
 
     public final String getHost()
         {
         throw new RuntimeException( "NYI" );
         }
 
     public final long getLastModified() throws IOException
         {
         throw new RuntimeException( "NYI" );
         }
 
     public final int getPort()
         {
         throw new RuntimeException( "NYI" );
         }
 
     public final String getProtocol()
         {
         throw new RuntimeException( "NYI" );
         }
 
     public final String getQuery()
         {
         throw new RuntimeException( "NYI" );
         }
 
     public final String getRef()
         {
         throw new RuntimeException( "NYI" );
         }
 
     public final String getRequestMethod()
         {
         throw new RuntimeException( "NYI" );
         }
 
     public final String getRequestProperty( final String aString )
         {
         throw new RuntimeException( "NYI" );
         }
 
     public final int getResponseCode() throws IOException
         {
         return myConnection.getResponseCode();
         }
 
     public final String getResponseMessage() throws IOException
         {
         return myConnection.getResponseMessage();
         }
 
     public final String getURL()
         {
         throw new RuntimeException( "NYI" );
         }
 
     public final void setRequestMethod( final String aMethodName ) throws IOException
         {
         myConnection.setRequestMethod( aMethodName );
         }
 
     public final void setRequestProperty( final String aName, final String aValue ) throws IOException
         {
         myConnection.setRequestProperty( aName, aValue );
         }
 
     // From ContentConnection
 
     public final String getEncoding()
         {
         return null;
         }
 
     public final long getLength()
         {
         return myConnection.getContentLength();
         }
 
     public final String getType()
         {
         return null;
         }
 
     // From InputConnection
 
     public final DataInputStream openDataInputStream() throws IOException
         {
         return new DataInputStream( myConnection.getInputStream() );
         }
 
     public final InputStream openInputStream() throws IOException
         {
         throw new RuntimeException( "NYI" );
         }
 
     // From OutputConnection
 
     public final DataOutputStream openDataOutputStream() throws IOException
         {
         return new DataOutputStream( myConnection.getOutputStream() );
         }
 
     public final OutputStream openOutputStream() throws IOException
         {
         throw new RuntimeException( "NYI" );
         }
 
     // From Connection
 
     public final void close() throws IOException
         {
        myConnection.disconnect();
         }
 
 
     private final HttpURLConnection myConnection;
     }
