 // Copyright (C) 2002, 2003 Philip Aston
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
 
 package net.grinder.plugin.http;
 
 import HTTPClient.ParseException;
 import HTTPClient.ProtocolNotSuppException;
 import HTTPClient.URI;
 
 import net.grinder.common.GrinderException;
 import net.grinder.plugininterface.PluginProcessContext;
 
 
 /**
  * Facade through which the script can control the behaviour of the
  * HTTP plug-in.
  *
  * @author Philip Aston
  * @version $Revision$
  **/
 public final class HTTPPluginControl {
 
   private static final PluginProcessContext s_pluginProcessContext;
 
   static {
     // Ensure that the HTTPPlugin is registered.
     s_pluginProcessContext = HTTPPlugin.getPluginProcessContext();
   }
 
   /**
   * Returns a {@link HTTPPluginConnection} that can be used to set
   * the default behaviour of new connections.
    * @return An <code>HTTPPluginConnection</code>.
    */
   public static final HTTPPluginConnection getConnectionDefaults() {
     return HTTPPluginConnectionDefaults.getConnectionDefaults();
   }
 
   /**
   * Returns a {@link HTTPPluginConnection} for a particular URL.
    *
   * <p>This method will throw a {@link GrinderException} if not
   * called from a worker thread.</p>
    *
    * @param url An absolute URL that specifies the connection.
    * @return a <code>HTTPPluginConnection</code> value
    * @exception GrinderException If an error occurs.
    * @exception ParseException If <code>url</coder> can not be parsed.
    * @exception ProtocolNotSuppException If <code>url</code>
    * specifies an unsupported protocol.
    */
   public static final HTTPPluginConnection getThreadConnection(String url)
     throws GrinderException, ParseException, ProtocolNotSuppException {
 
     final HTTPPluginThreadState threadState =
       (HTTPPluginThreadState)s_pluginProcessContext.getPluginThreadListener();
 	    
     return threadState.getConnectionWrapper(new URI(url));
   }
 
   /**
   * Returns the HTTPClient context object for the calling worker
   * thread. This is useful when calling HTTPClient methods directly,
   * e.g. {@link HTTPClient.CookieModule#listAllCookies(Object)}.
    *
   * <p>This method will throw a {@link GrinderException} if not
   * called from a worker thread.</p>
    *
    * @return The context object used for
    * <code>HTTPClient.HTTPConnections</code> created by this thread.
    * @exception GrinderException If an error occurs.
    */
   public static final Object getThreadHTTPClientContext()
     throws GrinderException {
     return s_pluginProcessContext.getPluginThreadListener();
   }
 }
