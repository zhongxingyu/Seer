 package net.java.dev.weblets.util;
 
 import java.io.*;
 import java.net.SocketException;
 
 /**
  * @author werpu
  * @date: 22.09.2008
  */
 public class CopyStrategyImpl implements CopyStrategy {
     public void copy(String webletName, String contentType, InputStream in, OutputStream out) throws IOException {
         boolean isText = isText(contentType);
         if (isText)
             copyText(webletName, new InputStreamReader(in), new OutputStreamWriter(out));
         else
             copyStream(in, out);
     }
 
     private boolean isText(String contentType) {
         return contentType != null && (contentType.startsWith("text/") || contentType.endsWith("xml") || contentType.equals("application/x-javascript"));
     }
 
     /**
      * wraps the input stream from our given request into another input stream
      *
      * @param webletName the name of the affected weblet
      * @param mimetype   the response mimetype
      * @param in         our given input steam
      * @return a wrapped input stream with our filterng cascade in place
      * @throws IOException in case of an error
      */
     public InputStream wrapInputStream(String webletName, String mimetype, InputStream in) throws IOException {
         if (isText(mimetype)) {
             BufferedReader bufIn = new BufferedReader(mapResponseReader(webletName, new InputStreamReader(in)));
             return new ReaderInputStream(bufIn);
         }
         return new BufferedInputStream(mapInputStream(in));
     }
 
     protected BufferedWriter mapResponseWriter(Writer out) {
         return new BufferedWriter(out);
     }
 
     protected BufferedReader mapResponseReader(String webletName, Reader in) {
         return new TextProcessingReader(in, webletName);
     }
 
     protected BufferedInputStream mapInputStream(InputStream in) {
         return new BufferedInputStream(in);
     }
 
     protected BufferedOutputStream mapOutputStream(OutputStream out) {
         return new BufferedOutputStream(out);
     }
 
     protected void copyText(String webletName, Reader in, Writer out) throws IOException {
         BufferedReader bufIn = mapResponseReader(webletName, in);
         BufferedWriter bufOut = mapResponseWriter(out);
         try {
             String line = null;
             try {
                 while ((line = bufIn.readLine()) != null) {
                     bufOut.write(line);
                    bufOut.write("\n");
                 }
 
             } catch(SocketException e) {
                 // This happens sometimes with Microsft Internet Explorer. It would
                 // appear (guess) that when javascript creates multiple dom nodes
                 // referring to the same remote resource then IE stupidly opens
                 // multiple sockets and requests that resource multiple times. But
                 // when the first request completes, it then realises its stupidity
                 // and forcibly closes all the other sockets. But here we are trying
                 // to service those requests, and so get a "broken pipe" failure
                 // on write. The only thing to do here is to silently ignore the issue,
                 // ie suppress the exception. Note that it is also possible for the
                 // above code to succeed (ie this exception clause is not run) but
                 // for a later flush to get the "broken pipe"; this is either due
                 // just to timing, or possibly IE is closing sockets after receiving
                 // a complete file for some types (gif?) rather than waiting for the
                 // server to close it.
 
                 //Note we fix it here, because this is the central core of the processing
                 //deepest level sort of
                 //we do not need to touch our writers that way 
 
               
             }
         } finally {
             closeBuffers(bufIn, bufOut);
         }
     }
 
     protected void copyStream(InputStream in, OutputStream out) throws IOException {
         byte[] buffer = new byte[2048];
         BufferedInputStream bufIn = mapInputStream(in);
         BufferedOutputStream bufOut = mapOutputStream(out);
         int len = 0;
         int total = 0;
         try {
             try {
                 while ((len = bufIn.read(buffer)) > 0) {
                     bufOut.write(buffer, 0, len);
                     total += len;
                 }
             } catch(SocketException e) {
                 // This happens sometimes with Microsft Internet Explorer. It would
                 // appear (guess) that when javascript creates multiple dom nodes
                 // referring to the same remote resource then IE stupidly opens
                 // multiple sockets and requests that resource multiple times. But
                 // when the first request completes, it then realises its stupidity
                 // and forcibly closes all the other sockets. But here we are trying
                 // to service those requests, and so get a "broken pipe" failure
                 // on write. The only thing to do here is to silently ignore the issue,
                 // ie suppress the exception. Note that it is also possible for the
                 // above code to succeed (ie this exception clause is not run) but
                 // for a later flush to get the "broken pipe"; this is either due
                 // just to timing, or possibly IE is closing sockets after receiving
                 // a complete file for some types (gif?) rather than waiting for the
                 // server to close it.
 
                 if(e.getMessage().toLowerCase().indexOf("broken pipe") != -1) {
                     return;
                 }
                 throw e;
             }
         } finally {
             closeBuffers(bufIn, bufOut);
 		}
 	}
 
     private void closeBuffers(Closeable bufIn, Closeable bufOut) {
         try {
             bufIn.close();
         } catch (Exception e) {
             //do nothing, there is a strange ie behavior which closes pipes upfront
         }
         try {
             bufOut.close();
         } catch (Exception e) {
             //do nothing, there is a strange ie behavior which closes pipes upfront
         }
     }
 }
