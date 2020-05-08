 package org.deegree.securityproxy.filter;
 
 import static org.apache.commons.io.IOUtils.copy;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpServletResponseWrapper;
 
 /**
  * Custom Response wrapper that allows access to the response code Deletes the "Transfer Encoding" HTTP Header
  * 
  * @author <a href="erben@lat-lon.de">Alexander Erben</a>
  * @author <a href="goltz@lat-lon.de">Lyn Goltz</a>
  * @author <a href="stenger@lat-lon.de">Dirk Stenger</a>
  * @author last edited by: $Author: erben $
  * 
  * @version $Revision: $, $Date: $
  */
 public class StatusCodeResponseBodyWrapper extends HttpServletResponseWrapper {
 
     private int httpStatus;
 
     private boolean isWriterUsed = false;
 
     private boolean isStreamUsed = false;
 
     private CopyPrintWriter bufferingWriter;
 
     private ServletOutputStream servletOutputStream;
 
     private ByteArrayOutputStream bufferingStream = new ByteArrayOutputStream();
 
     public StatusCodeResponseBodyWrapper( HttpServletResponse response ) {
         super( response );
     }
 
     @Override
     public void setHeader( String name, String value ) {
         if ( !"Transfer-Encoding".equals( name ) ) {
             super.setHeader( name, value );
         }
     }
 
     @Override
     public void addHeader( String name, String value ) {
         if ( !"Transfer-Encoding".equals( name ) ) {
            super.setHeader( name, value );
         }
     }
 
     @Override
     public void sendError( int sc )
                             throws IOException {
         httpStatus = sc;
         super.sendError( sc );
     }
 
     @Override
     public void sendError( int sc, String msg )
                             throws IOException {
         httpStatus = sc;
         super.sendError( sc, msg );
     }
 
     @Override
     public void setStatus( int sc ) {
         httpStatus = sc;
         super.setStatus( sc );
     }
 
     @Override
     public ServletOutputStream getOutputStream() {
         isStreamUsed = true;
         servletOutputStream = new ServletOutputStream() {
 
             public void write( int b )
                                     throws IOException {
                 bufferingStream.write( b );
             }
         };
         return servletOutputStream;
     }
 
     @Override
     public PrintWriter getWriter()
                             throws IOException {
         isWriterUsed = true;
         this.bufferingWriter = new CopyPrintWriter( super.getWriter() );
         return bufferingWriter;
     }
 
     /**
      * Copies the content of the internal buffered stream to the real outputstream of the underlying http response
      * 
      * @throws IOException
      *             if an I/O error occurs
      */
     public void copyBufferedStreamToRealStream()
                             throws IOException {
         copy( getBufferedStream(), getRealOutputStream() );
     }
 
     /**
      * Retrieves the buffered response body;
      * 
      * @return buffered content as byte array. may be empty.
      */
     public byte[] getBufferedBody() {
         if ( isWriterUsed )
             return bufferingWriter.getCopy();
         if ( isStreamUsed ) {
             return bufferingStream.toByteArray();
         }
         return new byte[0];
     }
 
     /**
      * Retrieves the buffered response as {@link InputStream};
      * 
      * @return buffered content as {@link InputStream} array. may be empty, never <code>null</code>
      */
     public InputStream getBufferedStream() {
         return new ByteArrayInputStream( getBufferedBody() );
     }
 
     /**
      * @return {@link OutputStream} from the wrapped {@link HttpServletResponse}
      * @throws IOException
      *             if an input or output exception occurred
      */
     public ServletOutputStream getRealOutputStream()
                             throws IOException {
         return super.getOutputStream();
     }
 
     /**
      * Retrieve the http status code of the response.
      * 
      * @return an http status code.
      */
     public int getStatus() {
         return httpStatus;
     }
 
 }
