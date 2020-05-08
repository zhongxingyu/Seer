 // Copyright (C) 2003, 2004, 2005 Philip Aston
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.tools.tcpproxy;
 
 import java.util.regex.Matcher;
import java.util.regex.Pattern;x
 
 
 /**
  * <p>{@link TCPProxyFilter} decorator that recognises HTTP request
  * messages and converts any absolute URLs in the method line to
  * relative URLs before passing to the filter.</p>
  *
  * <p>This is used in HTTP proxy mode for two reasons.
  *
  * <ol>
  *
  * <li>We want the URL format that filters have to parse to be
  * independent of whether the TCPProxy is being used as in HTTP proxy
  * mode or port forwarding mode.</li>
  *
  * <li>When the sucessor server is not an HTTP proxy, we don't want to
  * pass absolute URL's as HTTP 1.0 servers don't know what to do with
  * them. When the sucessor server is an HTTP proxy, we use a {@link
  * HTTPMethodAbsoluteURIFilterDecorator} as well.</li>
  *
  * </ol>
  * </p>
  *
  * @author Philip Aston
  * @version $Revision$
  */
 class HTTPMethodRelativeURIFilterDecorator implements TCPProxyFilter {
 
   private static final Pattern s_httpMethodLine;
 
   static {
     s_httpMethodLine =
       Pattern.compile("^([A-Z]+)[ \\t]+http://[^/:]+:?\\d*(/.*)",
                       Pattern.DOTALL);
   }
 
   private final TCPProxyFilter m_delegate;
 
   /**
    * Constructor.
    *
    * @param delegate Filter to decorate.
    */
   public HTTPMethodRelativeURIFilterDecorator(TCPProxyFilter delegate) {
     m_delegate = delegate;
   }
 
   /**
    * A new connection has been opened.
    *
    * @param connectionDetails Describes the connection.
    * @exception Exception If an error occurs.
    */
   public void connectionOpened(ConnectionDetails connectionDetails)
     throws Exception {
     m_delegate.connectionOpened(connectionDetails);
   }
 
   /**
    * A connection has been closed.
    *
    * @param connectionDetails Describes the connection.
    * @exception Exception If an error occurs.
    */
   public void connectionClosed(ConnectionDetails connectionDetails)
     throws Exception {
     m_delegate.connectionClosed(connectionDetails);
   }
 
   /**
    * Called just before stop.
    */
   public void stop() {
     m_delegate.stop();
   }
 
   /**
    * Handle a message fragment from the stream.
    *
    * @param connectionDetails Describes the connection.
    * @param buffer Contains the data.
    * @param bytesRead How many bytes of data in <code>buffer</code>.
    * @return Filters can optionally return a <code>byte[]</code>
    * which will be transmitted to the server instead of
    * <code>buffer</code>.
    * @exception Exception If an error occurs.
    */
   public byte[] handle(ConnectionDetails connectionDetails, byte[] buffer,
                        int bytesRead)
     throws Exception {
 
     // We use ISO 8859_1 instead of US ASCII. The correct charset to
     // use for URL's is not well defined by RFC 2616. This way we are
     // at least non-lossy (US-ASCII maps characters above 0xFF to
     // '?').
     final String original = new String(buffer, 0, bytesRead, "ISO8859_1");
 
     final Matcher matcher = s_httpMethodLine.matcher(original);
 
     if (matcher.find()) {
       final String result = matcher.group(1) + " " + matcher.group(2);
 
       final byte[] resultBytes = result.getBytes();
 
       final byte[] delegateResult =
         m_delegate.handle(connectionDetails, resultBytes, resultBytes.length);
 
       return delegateResult != null ? delegateResult : resultBytes;
     }
     else {
       return m_delegate.handle(connectionDetails, buffer, bytesRead);
     }
   }
 }
