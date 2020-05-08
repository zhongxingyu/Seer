 // Copyright (C) 2000 Paco Gomez
 // Copyright (C) 2000, 2001, 2002, 2003 Philip Aston
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
 
 import HTTPClient.CookieModule;
 import HTTPClient.DefaultAuthHandler;
 import HTTPClient.HTTPConnection;
 import net.grinder.common.GrinderException;
 import net.grinder.engine.process.PluginRegistry;
 import net.grinder.plugininterface.GrinderPlugin;
 import net.grinder.plugininterface.PluginException;
 import net.grinder.plugininterface.PluginProcessContext;
 import net.grinder.plugininterface.PluginThreadContext;
 import net.grinder.plugininterface.PluginThreadListener;
 
 
 /**
  * HTTP plugin.
  * 
  * @author Paco Gomez
  * @author Philip Aston
  * @version $Revision$
  **/
 public class HTTPPlugin implements GrinderPlugin {
 
   private static PluginProcessContext s_processContext;
 
   static {
     try {
       PluginRegistry.getInstance().register(HTTPPlugin.class);
     }
     catch (GrinderException e) {
       throw new ExceptionInInitializerError(e);
     }
   }
 
   /**
    * Static package scope accessor for the PluginProcessContext.
    *
    * @return The plugin process context.
    */
   static final PluginProcessContext getPluginProcessContext() {
     return s_processContext;
   }
 
   /**
    * Called by the PluginRegistry when the plugin is first registered.
    *
    * @param processContext The plugin process context.
    * @exception PluginException if an error occurs.
    */
   public void initialize(PluginProcessContext processContext)
     throws PluginException {
 
     s_processContext = processContext;
 
     // Remove standard HTTPClient modules which we don't want. We load
     // HTTPClient modules dynamically as we don't have public access.
     try {
      s_redirectionModule = Class.forName(HTTPClient.RedirectionModule);

       // Don't want additional post-processing of response data.
       HTTPConnection.removeDefaultModule(
 	Class.forName("HTTPClient.ContentEncodingModule"));
       HTTPConnection.removeDefaultModule(
 	Class.forName("HTTPClient.TransferEncodingModule"));
 
       // Don't want to retry requests.
       HTTPConnection.removeDefaultModule(
 	Class.forName("HTTPClient.RetryModule"));
     }
     catch (ClassNotFoundException e) {
       throw new PluginException("Could not load HTTPClient modules", e);
     }
 
     // Turn off cookie permission checks.
     CookieModule.setCookiePolicyHandler(null);
 
     // Turn off authorisation UI.
     DefaultAuthHandler.setAuthorizationPrompter(null);
   }
 
   /**
    * Called by the engine to obtain a new PluginThreadListener.
    *
    * @param threadContext The plugin thread context.
    * @return The new plugin thread listener.
    * @exception PluginException if an error occurs.
    */
   public PluginThreadListener createThreadListener(
     PluginThreadContext threadContext) throws PluginException {
 
     return new HTTPPluginThreadState(threadContext);
   }
 }
