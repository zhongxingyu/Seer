 // Copyright (C) 2010 Mike Stone
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
 // COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.engine.process.jruby;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 
 import net.grinder.engine.common.EngineException;
 import net.grinder.engine.common.ScriptLocation;
 import net.grinder.engine.process.ScriptEngine;
 
 import org.jruby.Ruby;
 import org.jruby.runtime.Constants;
 import org.jruby.runtime.ThreadContext;
 import org.jruby.runtime.builtin.IRubyObject;
 
 
 /**
  * Allow JRuby code to be used for Grinder scripts as well... why you
  * be hatin' on Ruby folks? ;-)
  *
  * @author Mike Stone
  */
 public final class JRubyScriptEngine implements ScriptEngine {
     private static final String TEST_RUNNER_CLASS_NAME = "TestRunner";
     private Ruby m_runtime;
 
     public JRubyScriptEngine() throws EngineException {
         m_runtime = Ruby.newInstance();
     }
 
     @Override
     public void initialise(ScriptLocation script) throws EngineException {
         m_runtime.setCurrentDirectory(script.getDirectory().getFile().getPath());
 
         try {
             m_runtime.runFromMain(new FileInputStream(script.getFile()), script.getFile().getPath());
         } catch (FileNotFoundException e) {
             throw new EngineException("Could not find script " + script.getFile().getPath());
         }
 
         if (!m_runtime.isClassDefined(TEST_RUNNER_CLASS_NAME)) {
             throw new EngineException("There is no class defined named '" + TEST_RUNNER_CLASS_NAME + "' in " + script);
         }
     }
 
     @Override
     public WorkerRunnable createWorkerRunnable() throws EngineException {
         return new JRubyWorkerRunnable();
     }
 
     @Override
     public WorkerRunnable createWorkerRunnable(Object testRunner) throws EngineException {
         if (testRunner instanceof IRubyObject) {
             return new JRubyWorkerRunnable((IRubyObject) testRunner);
         }
 
         throw new EngineException("testRunner isn't a JRuby object");
     }
 
     @Override
     public void shutdown() throws EngineException {
         m_runtime.evalScriptlet("exitfunc() if respond_to? :exitfunc");
     }
 
     @Override
     public String getDescription() {
         return "JRuby " + Constants.VERSION;
     }
 
     private class JRubyWorkerRunnable implements ScriptEngine.WorkerRunnable {
         private IRubyObject m_testRunner;
 
         private JRubyWorkerRunnable() throws EngineException {
             m_testRunner = m_runtime.evalScriptlet(TEST_RUNNER_CLASS_NAME + ".new");
         }
 
         public JRubyWorkerRunnable(IRubyObject testRunner) throws EngineException {
             m_testRunner = testRunner;
         }
 
         public void run() throws ScriptExecutionException {
             // Does this need to be run in a new context...?
            m_testRunner.callMethod(m_runtime.getCurrentContext(), "run");
         }
 
         public void shutdown() throws ScriptExecutionException {
             // Let the GC deal with it in time.
             m_testRunner = null;
         }
     }
 }
