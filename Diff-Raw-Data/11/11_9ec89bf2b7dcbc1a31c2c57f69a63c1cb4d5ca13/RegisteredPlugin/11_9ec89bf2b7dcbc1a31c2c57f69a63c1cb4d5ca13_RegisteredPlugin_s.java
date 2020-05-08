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
 
 package net.grinder.engine.process;
 
 import net.grinder.common.FilenameFactory;
 import net.grinder.common.Logger;
 import net.grinder.engine.EngineException;
 import net.grinder.plugininterface.GrinderPlugin;
 import net.grinder.plugininterface.PluginException;
 import net.grinder.plugininterface.PluginProcessContext;
 import net.grinder.plugininterface.PluginThreadListener;
 import net.grinder.script.Grinder;
 
 
 /**
  * @author Philip Aston
  * @version $Revision$
 **/
 final class RegisteredPlugin implements PluginProcessContext {
 
   private final GrinderPlugin m_plugin;
   private final ProcessContext m_processContext;
   private final ThreadLocal m_threadListenerThreadLocal = new ThreadLocal();
 
   public RegisteredPlugin(GrinderPlugin plugin,
                           ProcessContext processContext) {
     m_plugin = plugin;
     m_processContext = processContext;
   }
 
   public Logger getLogger() {
     return m_processContext.getLogger();
   }
 
   public FilenameFactory getFilenameFactory() {
     return m_processContext.getLoggerImplementation().getFilenameFactory();
   }
 
   public Grinder.ScriptContext getScriptContext() {
     return m_processContext.getScriptContext();
   }
 
   public PluginThreadListener getPluginThreadListener()
     throws EngineException {
     final ThreadContext threadContext = ThreadContext.getThreadInstance();
 
     if (threadContext == null) {
       throw new EngineException("Must be called from worker thread");
     }
 
     return getPluginThreadListener(threadContext);
   }
 
   PluginThreadListener getPluginThreadListener(ThreadContext threadContext)
     throws EngineException {
 
     final PluginThreadListener existingPluginThreadListener =
       (PluginThreadListener)m_threadListenerThreadLocal.get();
 
     if (existingPluginThreadListener != null) {
       return existingPluginThreadListener;
     }
 
     try {
       final PluginThreadListener newPluginThreadListener =
         m_plugin.createThreadListener(threadContext);
 
       m_threadListenerThreadLocal.set(newPluginThreadListener);
 
       return newPluginThreadListener;
     }
     catch (PluginException e) {
       final Logger logger = threadContext.getThreadLogger();
 
      logger.error("Thread could not initialise plugin" + e);
       e.printStackTrace(logger.getErrorLogWriter());
 
       throw new EngineException("Thread could not initialise plugin", e);
     }
   }
 }
