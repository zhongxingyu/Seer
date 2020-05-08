 // Copyright (C) 2000 Paco Gomez
 // Copyright (C) 2000, 2001, 2002, 2003, 2004 Philip Aston
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
 
 import net.grinder.common.Logger;
 import net.grinder.engine.common.EngineException;
 import net.grinder.plugininterface.GrinderPlugin;
 import net.grinder.plugininterface.PluginException;
 import net.grinder.plugininterface.PluginProcessContext;
 import net.grinder.plugininterface.PluginThreadListener;
 import net.grinder.script.Grinder.ScriptContext;
 import net.grinder.statistics.StatisticsServices;
 
 
 /**
  * Handle returned from plugin registration.
  *
  * @author Philip Aston
  * @version $Revision$
  */
 final class RegisteredPlugin implements PluginProcessContext {
 
   private final GrinderPlugin m_plugin;
   private final ScriptContext m_scriptContext;
   private final ThreadContextLocator m_threadContextLocator;
   private final StatisticsServices m_statisticsServices;
   private final ThreadLocal m_threadListenerThreadLocal = new ThreadLocal();
 
   public RegisteredPlugin(GrinderPlugin plugin, ScriptContext scriptContext,
                           ThreadContextLocator threadContextLocator,
                           StatisticsServices statisticsServices) {
     m_plugin = plugin;
     m_scriptContext = scriptContext;
     m_threadContextLocator = threadContextLocator;
     m_statisticsServices = statisticsServices;
   }
 
   public ScriptContext getScriptContext() {
     return m_scriptContext;
   }
 
   public PluginThreadListener getPluginThreadListener()
     throws EngineException {
 
     final ThreadContext threadContext = m_threadContextLocator.get();
 
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
 
      threadContext.registerThreadLifeCycleListener(newPluginThreadListener);

       return newPluginThreadListener;
     }
     catch (PluginException e) {
       final Logger logger = threadContext.getThreadLogger();
 
       logger.error("Thread could not initialise plugin: " + e);
       e.printStackTrace(logger.getErrorLogWriter());
 
       throw new EngineException("Thread could not initialise plugin", e);
     }
   }
 
   public StatisticsServices getStatisticsServices() {
     return m_statisticsServices;
   }
 }
