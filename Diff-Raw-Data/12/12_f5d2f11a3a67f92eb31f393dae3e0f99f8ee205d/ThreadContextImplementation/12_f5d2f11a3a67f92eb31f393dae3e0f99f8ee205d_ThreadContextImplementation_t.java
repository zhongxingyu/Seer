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
 
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.List;
 
 import net.grinder.common.FilenameFactory;
 import net.grinder.common.SSLContextFactory;
 import net.grinder.common.ThreadLifeCycleListener;
 import net.grinder.engine.common.EngineException;
 import net.grinder.plugininterface.PluginThreadContext;
 import net.grinder.script.Statistics;
 
 
 /**
  * Package scope.
  *
  * @author Philip Aston
  * @version $Revision$
  */
 final class ThreadContextImplementation
   implements ThreadContext, PluginThreadContext {
 
   private final List m_threadLifeCycleListeners = new ArrayList();
 
   private final ProcessContext m_processContext;
   private final ThreadLogger m_threadLogger;
   private final FilenameFactory m_filenameFactory;
 
   private final ScriptStatisticsImplementation m_scriptStatistics;
 
   private SSLContextFactory m_sslContextFactory;
 
   private boolean m_startTimeOverridenByPlugin;
   private long m_startTime;
   private long m_elapsedTime;
 
   public ThreadContextImplementation(ProcessContext processContext,
                                      ThreadLogger threadLogger,
                                      FilenameFactory filenameFactory,
                                      PrintWriter dataWriter)
     throws EngineException {
 
     m_processContext = processContext;
     m_threadLogger = threadLogger;
     m_filenameFactory = filenameFactory;
 
     m_scriptStatistics =
       new ScriptStatisticsImplementation(
         processContext.getThreadContextLocator(),
         dataWriter,
         m_threadLogger.getThreadID(),
         processContext.getRecordTime());
 
     registerThreadLifeCycleListener(m_scriptStatistics);
   }
 
   public FilenameFactory getFilenameFactory() {
     return m_filenameFactory;
   }
 
   public int getThreadID() {
     return m_threadLogger.getThreadID();
   }
 
   public int getRunNumber() {
     return m_threadLogger.getCurrentRunNumber();
   }
 
   public void startTimedSection() {
     if (!m_startTimeOverridenByPlugin) {
       m_startTimeOverridenByPlugin = true;
       m_startTime = System.currentTimeMillis();
     }
   }
 
   public void stopTimedSection() {
     m_elapsedTime = System.currentTimeMillis() - m_startTime;
   }
 
   private void startTimer() {
     // This is to make it more likely that the timed section has a
     // "clear run".
     Thread.yield();
 
     m_startTime = System.currentTimeMillis();
     m_startTimeOverridenByPlugin = false;
     m_elapsedTime = -1;
   }
 
   private void stopTimer() {
     if (m_elapsedTime < 0) { // Not already stopped.
       stopTimedSection();
     }
   }
 
   public ThreadLogger getThreadLogger() {
     return m_threadLogger;
   }
 
   /**
    * This could be factored out to a separate "TestInvoker" class.
    * However, the sensible owner for a TestInvoker would be
    * ThreadContext, so keep it here for now. Also, all the
    * startTimer/stopTimer interface is part of the PluginThreadContext
    * interface.
    */
   public Object invokeTest(TestData testData, TestData.Invokeable invokeable)
     throws JythonScriptExecutionException, ShutdownException {
 
     if (m_processContext.getShutdown()) {
       throw new ShutdownException("Process has been shutdown");
     }
 
     if (m_threadLogger.getCurrentTestNumber() != -1) {
       // Originally we threw a ReentrantInvocationException here.
       // However, this caused problems when wrapping Jython objects
       // that call themselves; in our scheme the wrapper shares a
       // dictionary so self = self and we recurse up our own.
       return invokeable.call();
     }
 
     m_threadLogger.setCurrentTestNumber(testData.getTest().getNumber());
 
     m_scriptStatistics.beginTest(testData, getRunNumber());
 
     try {
       startTimer();
 
       final Object testResult;
 
       try {
         testResult = invokeable.call();
       }
       finally {
         stopTimer();
       }
 
      if (m_scriptStatistics.getSuccess()) {
        m_scriptStatistics.setSuccessNoChecks();
        m_scriptStatistics.setTimeNoChecks(m_elapsedTime);
      }
      else {
        // The plug-in might have set timing information etc., or set errors to
        // be greater than 1. For consistency, we override to a single error per
        // Test with no associated timing information.
        m_scriptStatistics.setErrorNoChecks();
      }
 
       return testResult;
     }
     catch (org.python.core.PyException e) {
       m_scriptStatistics.setErrorNoChecks();
 
       // We don't log the exception. If the script doesn't handle the
       // exception it will be logged when the run is aborted,
       // otherwise we assume the script writer knows what they're
       // doing.
       throw e;
     }
     finally {
       m_scriptStatistics.endTest(
         m_startTime - m_processContext.getExecutionStartTime());
 
       m_threadLogger.setCurrentTestNumber(-1);
     }
   }
 
   public void endRun() {
     m_scriptStatistics.endRun();
   }
 
   public long getStartTime() {
     return m_startTime;
   }
 
   public Statistics getScriptStatistics() {
     return m_scriptStatistics;
   }
 
   public PluginThreadContext getPluginThreadContext() {
     return this;
   }
 
   public SSLContextFactory getThreadSSLContextFactory() {
     return m_sslContextFactory;
   }
 
   public void setThreadSSLContextFactory(SSLContextFactory sslContextFactory) {
     m_sslContextFactory = sslContextFactory;
   }
 
   public List getThreadLifeCycleListeners() {
     return m_threadLifeCycleListeners;
   }
 
   public void registerThreadLifeCycleListener(
     ThreadLifeCycleListener listener) {
     m_threadLifeCycleListeners.add(listener);
   }
 }
