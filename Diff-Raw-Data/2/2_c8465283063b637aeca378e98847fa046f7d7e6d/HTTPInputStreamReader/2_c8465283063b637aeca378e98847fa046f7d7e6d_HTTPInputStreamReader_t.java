 package wjhk.jupload2.upload.helper;
 
 import java.io.IOException;
 import java.io.PushbackInputStream;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.net.ssl.SSLSocket;
 
 import wjhk.jupload2.exception.JUploadException;
 import wjhk.jupload2.policies.UploadPolicy;
 
 /**
  * A helper, to read the response coming from the server.
  * 
  * @author etienne_sf
  */
 public class HTTPInputStreamReader {
     // //////////////////////////////////////////////////////////////////////////////
     // //////////////////// Main attributes
     // //////////////////////////////////////////////////////////////////////////////
 
     /**
      * The current upload policy, always useful.
      */
     private UploadPolicy uploadPolicy = null;
 
     private HTTPConnectionHelper httpConnectionHelper = null;
 
     /**
      * Contains the HTTP response body, that is: the server response, without
      * the headers.
      */
     String responseBody = null;
 
     /**
      * The headers of the HTTP response.
      */
     String responseHeaders = null;
 
     /**
      * The status message from the first line of the response (e.g. "200 OK").
      */
     String responseMsg = null;
 
     // ////////////////////////////////////////////////////////////////////////////////////
     // /////////////////// ATTRIBUTE CONTAINING DATA COMING FROM THE RESPONSE
     // ////////////////////////////////////////////////////////////////////////////////////
 
     private CookieJar cookies = null;
 
     boolean gotClose = false;
 
     private boolean gotChunked = false;
 
     private boolean gotContentLength = false;
 
     private int clen = 0;
 
     /**
      * The server HTTP response. Should be 200, in case of success.
      */
     private int httpStatusCode = 0;
 
     private String line = "";
 
     private byte[] body = new byte[0];
 
     private String charset = "ISO-8859-1";
 
     // ////////////////////////////////////////////////////////////////////////////////////
     // /////////////////// CONSTANTS USED TO CONTROL THE HTTP INTPUT
     // ////////////////////////////////////////////////////////////////////////////////////
     private final static int CHUNKBUF_SIZE = 4096;
 
     private final byte chunkbuf[] = new byte[CHUNKBUF_SIZE];
 
     private final static Pattern pChunked = Pattern.compile(
             "^Transfer-Encoding:\\s+chunked", Pattern.CASE_INSENSITIVE);
 
     private final static Pattern pClose = Pattern.compile(
             "^Connection:\\s+close", Pattern.CASE_INSENSITIVE);
 
     private final static Pattern pProxyClose = Pattern.compile(
             "^Proxy-Connection:\\s+close", Pattern.CASE_INSENSITIVE);
 
     private final static Pattern pHttpStatus = Pattern
             .compile("^HTTP/\\d\\.\\d\\s+((\\d+)\\s+.*)$");
 
     private final static Pattern pContentLen = Pattern.compile(
             "^Content-Length:\\s+(\\d+)$", Pattern.CASE_INSENSITIVE);
 
     private final static Pattern pContentTypeCs = Pattern.compile(
             "^Content-Type:\\s+.*;\\s*charset=([^;\\s]+).*$",
             Pattern.CASE_INSENSITIVE);
 
     private final static Pattern pSetCookie = Pattern.compile(
             "^Set-Cookie:\\s+(.*)$", Pattern.CASE_INSENSITIVE);
 
     /**
      * The standard constructor: does nothing ! Oh yes, it initialize some
      * attribute from the given parameter... :-)
      * 
      * @param httpConnectionHelper The connection helper, associated with this
      *            instance.
      * @param uploadPolicy The current upload policy.
      */
     public HTTPInputStreamReader(HTTPConnectionHelper httpConnectionHelper,
             UploadPolicy uploadPolicy) {
         this.httpConnectionHelper = httpConnectionHelper;
         this.uploadPolicy = uploadPolicy;
         this.cookies = new CookieJar(uploadPolicy);
     }
 
     /**
      * Return the last read http response (200, in case of success).
      * 
      * @return The last read http response
      */
     public int gethttpStatusCode() {
         return this.httpStatusCode;
     }
 
     /**
      * Get the last response body.
      * 
      * @return The last read response body.
      */
     public String getResponseBody() {
         return this.responseBody;
     }
 
     /**
      * Get the headers of the HTTP response.
      * 
      * @return The HTTP headers.
      */
     public String getResponseHeaders() {
         return this.responseHeaders;
     }
 
     /**
      * Get the last response message.
      * 
      * @return The response message from the first line of the response (e.g.
      *         "200 OK").
      */
     public String getResponseMsg() {
         return this.responseMsg;
     }
 
     /**
      * The main method: reads the response in the input stream.
      * 
      * @return The response status (e.g.: 200 if everything was ok)
      * @throws JUploadException
      */
     public int readHttpResponse() throws JUploadException {
         PushbackInputStream httpDataIn = this.httpConnectionHelper
                 .getInputStream();
 
         try {
             // If the user requested abort, we are not going to send
             // anymore, so shutdown the outgoing half of the socket.
             // This helps the server to speed up with it's response.
            if (! (this.httpConnectionHelper.getSocket() instanceof SSLSocket)) {
                 this.httpConnectionHelper.getSocket().shutdownOutput();
             }
 
             // We first read the headers,
             readHeaders(httpDataIn);
 
             // then the body.
             // If we're in a HEAD request ... we're not interested in the body!
             if (this.httpConnectionHelper.getMethod().equals("HEAD")) {
                 this.uploadPolicy.displayDebug(
                         "This is a HEAD request: we don't care about the body",
                         70);
                 this.responseBody = "";
             } else {
                 readBody(httpDataIn);
             }
         } catch (Exception e) {
             throw new JUploadException(e);
         }
 
         return this.httpStatusCode;
     }
 
     // //////////////////////////////////////////////////////////////////////////////////////
     // //////////////////// Various utilities
     // //////////////////////////////////////////////////////////////////////////////////////
 
     /**
      * Concatenates two byte arrays.
      * 
      * @param buf1 The first array
      * @param buf2 The second array
      * @return A byte array, containing buf2 appended to buf2
      */
     static byte[] byteAppend(byte[] buf1, byte[] buf2) {
         byte[] ret = new byte[buf1.length + buf2.length];
         System.arraycopy(buf1, 0, ret, 0, buf1.length);
         System.arraycopy(buf2, 0, ret, buf1.length, buf2.length);
         return ret;
     }
 
     /**
      * Concatenates two byte arrays.
      * 
      * @param buf1 The first array
      * @param buf2 The second array
      * @param len Number of bytes to copy from buf2
      * @return A byte array, containing buf2 appended to buf2
      */
     static byte[] byteAppend(byte[] buf1, byte[] buf2, int len) {
         if (len > buf2.length)
             len = buf2.length;
         byte[] ret = new byte[buf1.length + len];
         System.arraycopy(buf1, 0, ret, 0, buf1.length);
         System.arraycopy(buf2, 0, ret, buf1.length, len);
         return ret;
     }
 
     /**
      * Similar like BufferedInputStream#readLine() but operates on raw bytes.
      * Line-Ending is <b>always</b> "\r\n".
      * 
      * @param inputStream
      * 
      * @param charset The input charset of the stream.
      * @param includeCR Set to true, if the terminating CR/LF should be included
      *            in the returned byte array.
      * @return The line, encoded from the input stream with the given charset
      * @throws IOException
      */
     public static String readLine(PushbackInputStream inputStream,
             String charset, boolean includeCR) throws IOException {
         byte[] line = readLine(inputStream, includeCR);
         return (null == line) ? null : new String(line, charset);
     }
 
     /**
      * Similar like BufferedInputStream#readLine() but operates on raw bytes.
      * According to RFC 2616, and of line may be CR (13), LF (10) or CRLF.
      * Line-Ending is <b>always</b> "\r\n" in header, but not in text bodies.
      * Update done by TedA (sourceforge account: tedaaa). Allows to manage
      * response from web server that send LF instead of CRLF ! Here is a part of
      * the RFC: <I>"we recommend that applications, when parsing such headers,
      * recognize a single LF as a line terminator and ignore the leading
      * CR"</I>. <BR>
      * Corrected again to manage line finished by CR only. This is not allowed
      * in headers, but this method is also used to read lines in the body.
      * 
      * @param inputStream
      * 
      * @param includeCR Set to true, if the terminating CR/LF should be included
      *            in the returned byte array. In this case, CR/LF is always
      *            returned to the caller, whether the input stream got CR, LF or
      *            CRLF.
      * @return The byte array from the input stream, with or without a trailing
      *         CRLF
      * @throws IOException
      */
     public static byte[] readLine(PushbackInputStream inputStream,
             boolean includeCR) throws IOException {
         final byte EOS = -1;
         final byte CR = 13;
         final byte LF = 10;
         int len = 0;
         int buflen = 128; // average line length
         byte[] buf = new byte[buflen];
         byte[] ret = null;
         int b;
         boolean lineRead = false;
 
         while (!lineRead) {
             b = inputStream.read();
             switch (b) {
                 case EOS:
                     // We've finished reading the stream, and so the line is
                     // finished too.
                     if (len == 0) {
                         return null;
                     }
                     lineRead = true;
                     break;
                 /*
                  * if (len > 0) { ret = new byte[len]; System.arraycopy(buf, 0,
                  * ret, 0, len); return ret; } return null;
                  */
                 case LF:
                     // We found the end of the current line.
                     lineRead = true;
                     break;
                 case CR:
                     // We got a CR. It can be the end of line.
                     // Is it followed by a LF ? (not mandatory in RFC 2616)
                     b = inputStream.read();
 
                     if (b != LF) {
                         // The end of line was a simple LF: the next one blongs
                         // to the next line.
                         inputStream.unread(b);
                     }
                     lineRead = true;
                     break;
                 default:
                     buf[len++] = (byte) b;
                     // If the buffer is too small, we let enough space to add CR
                     // and LF, in case of ...
                     if (len + 2 >= buflen) {
                         buflen *= 2;
                         byte[] tmp = new byte[buflen];
                         System.arraycopy(buf, 0, tmp, 0, len);
                         buf = tmp;
                     }
             }
         } // while
 
         // Let's go back to before any CR and LF.
         while (len > 0 && (buf[len] == CR || buf[len] == LF)) {
             len -= 1;
         }
 
         // Ok, now len indicates the end of the actual line.
         // Should we add a proper CRLF, or nothing ?
         if (includeCR) {
             // We have enough space to add these two characters (see the default
             // here above)
             buf[len++] = CR;
             buf[len++] = LF;
         }
 
         if (len > 0) {
             ret = new byte[len];
             if (len > 0)
                 System.arraycopy(buf, 0, ret, 0, len);
         } else {
             // line feed for empty line between headers and body, or within the
             // body.
             ret = new byte[0];
         }
         return ret;
     }
 
     /**
      * Read the headers from the given input stream.
      * 
      * @param httpDataIn The http input stream
      * @throws IOException
      * @throws JUploadException
      */
     private void readHeaders(PushbackInputStream httpDataIn)
             throws IOException, JUploadException {
         StringBuffer sbHeaders = new StringBuffer();
         // Headers are US-ASCII (See RFC 2616, Section 2.2)
         String tmp;
         while (!Thread.interrupted()) {
             tmp = readLine(httpDataIn, "US-ASCII", false);
             if (null == tmp)
                 throw new JUploadException("unexpected EOF (in header)");
             if (this.httpStatusCode == 0) {
                 // We must be reading the first line of the HTTP header.
                 this.uploadPolicy.displayDebug(
                         "-------- Response Headers Start --------", 80);
                 Matcher m = pHttpStatus.matcher(tmp);
                 if (m.matches()) {
                     this.httpStatusCode = Integer.parseInt(m.group(2));
                     this.responseMsg = m.group(1);
                 } else {
                     // The status line must be the first line of the
                     // response. (See RFC 2616, Section 6.1) so this
                     // is an error.
 
                     // We first display the wrong line.
                     this.uploadPolicy.displayDebug("First line of response: '"
                             + tmp + "'", 80);
                     // Then, we throw the exception.
                     throw new JUploadException(
                             "HTTP response did not begin with status line.");
                 }
             }
             // Handle folded headers (RFC 2616, Section 2.2). This is
             // handled after the status line, because that line may
             // not be folded (RFC 2616, Section 6.1).
             if (tmp.startsWith(" ") || tmp.startsWith("\t"))
                 this.line += " " + tmp.trim();
             else
                 this.line = tmp;
 
             // The read line is now correctly formatted.
             this.uploadPolicy.displayDebug(this.line, 80);
             sbHeaders.append(tmp).append("\n");
 
             if (pClose.matcher(this.line).matches())
                 this.gotClose = true;
             if (pProxyClose.matcher(this.line).matches())
                 this.gotClose = true;
             if (pChunked.matcher(this.line).matches())
                 this.gotChunked = true;
             Matcher m = pContentLen.matcher(this.line);
             if (m.matches()) {
                 this.gotContentLength = true;
                 this.clen = Integer.parseInt(m.group(1));
             }
             m = pContentTypeCs.matcher(this.line);
             if (m.matches())
                 this.charset = m.group(1);
             m = pSetCookie.matcher(this.line);
             if (m.matches()) {
                 this.uploadPolicy.displayDebug(
                         "Calling this.cookies.parseCookieHeader, with parameter: "
                                 + m.group(1), 80);
                 this.cookies.parseCookieHeader(m.group(1));
                 this.uploadPolicy.displayDebug("Cookie header parsed.", 80);
             }
             if (this.line.length() == 0) {
                 // We've finished reading the headers
                 break;
             }
         }
         // RFC 2616, Section 6. Body is separated by the
         // header with an empty line.
         this.responseHeaders = sbHeaders.toString();
         this.uploadPolicy.displayDebug(
                 "--------- Response Headers End ---------", 80);
     }// readHeaders()
 
     /**
      * Read the body from the given input stream.
      * 
      * @param httpDataIn The http input stream
      * @throws IOException
      * @throws JUploadException
      * @throws JUploadException
      */
     private void readBody(PushbackInputStream httpDataIn) throws IOException,
             JUploadException {
         // && is evaluated from left to right so !stop must come first!
         while (!Thread.interrupted()
                 && ((!this.gotContentLength) || (this.clen > 0))) {
             if (this.gotChunked) {
                 // Read the chunk header.
                 // This is US-ASCII! (See RFC 2616, Section 2.2)
                 this.line = readLine(httpDataIn, "US-ASCII", false);
                 if (null == this.line)
                     throw new JUploadException(
                             "unexpected EOF (in HTTP Body, chunked mode)");
                 // Handle a single chunk of the response
                 // We cut off possible chunk extensions and ignore them.
                 // The length is hex-encoded (RFC 2616, Section 3.6.1)
                 int len = Integer.parseInt(this.line.replaceFirst(";.*", "")
                         .trim(), 16);
                 this.uploadPolicy.displayDebug("Chunk: " + this.line + " dec: "
                         + len, 70);
                 if (len == 0) {
                     // RFC 2616, Section 3.6.1: A length of 0 denotes
                     // the last chunk of the body.
 
                     // This code wrong if the server sends chunks
                     // with trailers! (trailers are HTTP Headers that
                     // are send *after* the body. These are announced
                     // in the regular HTTP header "Trailer".
                     // Fritz: Never seen them so far ...
                     // TODO: Implement trailer-handling.
                     break;
                 }
 
                 // Loop over the chunk (len == length of the chunk)
                 while (len > 0) {
                     int rlen = (len > CHUNKBUF_SIZE) ? CHUNKBUF_SIZE : len;
                     int ofs = 0;
                     if (rlen > 0) {
                         while (ofs < rlen) {
                             int res = httpDataIn.read(this.chunkbuf, ofs, rlen
                                     - ofs);
                             if (res < 0)
                                 throw new JUploadException("unexpected EOF");
                             len -= res;
                             ofs += res;
                         }
                         if (ofs < rlen)
                             throw new JUploadException("short read");
                         if (rlen < CHUNKBUF_SIZE)
                             this.body = byteAppend(this.body, this.chunkbuf,
                                     rlen);
                         else
                             this.body = byteAppend(this.body, this.chunkbuf);
                     }
                 }
                 // Got the whole chunk, read the trailing CRLF.
                 readLine(httpDataIn, false);
             } else {
                 // Not chunked. Use either content-length (if available)
                 // or read until EOF.
                 if (this.gotContentLength) {
                     // Got a Content-Length. Read exactly that amount of
                     // bytes.
                     while (this.clen > 0) {
                         int rlen = (this.clen > CHUNKBUF_SIZE) ? CHUNKBUF_SIZE
                                 : this.clen;
                         int ofs = 0;
                         if (rlen > 0) {
                             while (ofs < rlen) {
                                 int res = httpDataIn.read(this.chunkbuf, ofs,
                                         rlen - ofs);
                                 if (res < 0)
                                     throw new JUploadException(
                                             "unexpected EOF (in HTTP body, not chunked mode)");
                                 this.clen -= res;
                                 ofs += res;
                             }
                             if (ofs < rlen)
                                 throw new JUploadException("short read");
                             if (rlen < CHUNKBUF_SIZE)
                                 this.body = byteAppend(this.body,
                                         this.chunkbuf, rlen);
                             else
                                 this.body = byteAppend(this.body, this.chunkbuf);
                         }
                     }
                 } else {
                     // No Content-length available, read until EOF
                     //
                     while (true) {
                         byte[] lbuf = readLine(httpDataIn, true);
                         if (null == lbuf)
                             break;
                         this.body = byteAppend(this.body, lbuf);
                     }
                     break;
                 }
             }
         } // while
 
         // Convert the whole body according to the charset.
         // The default for charset ISO-8859-1, but overridden by
         // the charset attribute of the Content-Type header (if any).
         // See RFC 2616, Sections 3.4.1 and 3.7.1.
         this.responseBody = new String(this.body, this.charset);
 
         // At the higher debug level, we display the response.
         this.uploadPolicy.displayDebug("-------- Response Body Start --------",
                 100);
         this.uploadPolicy.displayDebug(this.responseBody, 100);
         this.uploadPolicy.displayDebug("-------- Response Body End --------",
                 100);
     }// readBody
 }
