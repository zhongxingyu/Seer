 // Copyright (C) 2004 Philip Aston
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
 
 import junit.framework.TestCase;
 
 import net.grinder.engine.EngineException;
 import net.grinder.plugininterface.GrinderPlugin;
 import net.grinder.plugininterface.PluginException;
 import net.grinder.plugininterface.PluginThreadContext;
 import net.grinder.plugininterface.PluginThreadListener;
 import net.grinder.script.Grinder.ScriptContext;
 import net.grinder.testutility.RandomStubFactory;
 
 
 /**
  * Unit test case for <code>RegisteredPlugin</code>.
  *
  * @author Philip Aston
  * @version $Revision$
  */
 public class TestRegisteredPlugin extends TestCase {
   public TestRegisteredPlugin(String name) {
     super(name);
   }
 
   public void testConstructorAndSimpleAccessors() throws Exception {
 
     final RandomStubFactory pluginStubFactory =
       new RandomStubFactory(GrinderPlugin.class);
     final GrinderPlugin plugin = (GrinderPlugin)pluginStubFactory.getStub();
 
     final RandomStubFactory scriptContextStubFactory =
       new RandomStubFactory(ScriptContext.class);
     final ScriptContext scriptContext =
       (ScriptContext)scriptContextStubFactory.getStub();
 
     final ThreadContextLocator threadContextLocator =
       new StubThreadContextLocator();
 
     final RegisteredPlugin registeredPlugin =
       new RegisteredPlugin(plugin, scriptContext, threadContextLocator);
 
     assertEquals(scriptContext, registeredPlugin.getScriptContext());
   }
 
   public void testGetPluginThreadListener() throws Exception {
 
     final GrinderPluginStubFactory grinderPluginStubFactory =
       new GrinderPluginStubFactory();
     final GrinderPlugin grinderPlugin =
       grinderPluginStubFactory.getGrinderPlugin();
 
     final RandomStubFactory scriptContextStubFactory =
       new RandomStubFactory(ScriptContext.class);
     final ScriptContext scriptContext =
       (ScriptContext)scriptContextStubFactory.getStub();
 
     final ThreadContextLocator threadContextLocator =
       new StubThreadContextLocator();
 
     final RegisteredPlugin registeredPlugin =
       new RegisteredPlugin(grinderPlugin, scriptContext, threadContextLocator);
 
     try {
       registeredPlugin.getPluginThreadListener();
       fail("Expected EngineException");
     }
     catch (EngineException e) {
     }
 
     final ThreadLoggerStubFactory threadLoggerStubFactory =
       new ThreadLoggerStubFactory();
     final ThreadLogger threadLogger =
       threadLoggerStubFactory.getThreadLogger();
 
     final ThreadContextStubFactory threadContextStubFactory =
       new ThreadContextStubFactory(threadLogger);
     final ThreadContext threadContext =
       threadContextStubFactory.getThreadContext();
     threadContextLocator.set(threadContext);
 
     grinderPluginStubFactory.setThrowExceptionFromCreateThreadListener(true);
 
     try {
       registeredPlugin.getPluginThreadListener();
       fail("Expected EngineException");
     }
     catch (EngineException e) {
     }
 
    // Fix me.
 
 
     grinderPluginStubFactory.setThrowExceptionFromCreateThreadListener(false);
 
     final PluginThreadListener pluginThreadListener1 =
       registeredPlugin.getPluginThreadListener();
 
     threadContextStubFactory.assertSuccess(
      "getPluginThreadContext", new Object[] {});
     grinderPluginStubFactory.assertSuccess(
       "createThreadListener", new Class[] { PluginThreadContext.class });
 
     final PluginThreadListener pluginThreadListener2 =
       registeredPlugin.getPluginThreadListener();
 
     threadContextStubFactory.assertNotCalled();
     grinderPluginStubFactory.assertNotCalled();
 
     assertSame(pluginThreadListener1, pluginThreadListener2);
 
     final PluginThreadListener pluginThreadListener3 =
       registeredPlugin.getPluginThreadListener(threadContext);
 
     assertSame(pluginThreadListener1, pluginThreadListener3);
   }
 
   /**
    * Must be public so that override_ methods can be called
    * externally.
    */
   public static class ThreadContextStubFactory extends RandomStubFactory {
 
     private final ThreadLogger m_threadLogger;
 
     public ThreadContextStubFactory(ThreadLogger threadLogger) {
       super(ThreadContext.class);
       m_threadLogger = threadLogger;
     }
 
     final ThreadContext getThreadContext() {
       return (ThreadContext)getStub();
     }
 
     public ThreadLogger override_getThreadLogger(Object proxy) {
       return m_threadLogger;
     }
   }
 
   public static class GrinderPluginStubFactory extends RandomStubFactory {
     private boolean m_throwExceptionFromCreateThreadListener;
     private final GrinderPlugin m_delegateStub;
 
     public GrinderPluginStubFactory() {
       super(GrinderPlugin.class);
       m_delegateStub =
         (GrinderPlugin)new RandomStubFactory(GrinderPlugin.class).getStub();
     }
 
     final GrinderPlugin getGrinderPlugin() {
       return (GrinderPlugin)getStub();
     }
 
     void setThrowExceptionFromCreateThreadListener(boolean b) {
       m_throwExceptionFromCreateThreadListener = b;
     }
 
     public PluginThreadListener override_createThreadListener(
       Object proxy, PluginThreadContext pluginThreadContext)
       throws PluginException {
 
       if (m_throwExceptionFromCreateThreadListener) {
         throw new PluginException("Eat me");
       }
 
       return m_delegateStub.createThreadListener(pluginThreadContext);
     }
   }
 }
