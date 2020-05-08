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
 
 package net.grinder.engine.process.multilingual;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import net.grinder.engine.common.EngineException;
 import net.grinder.engine.common.ScriptLocation;
 import net.grinder.engine.process.ScriptEngine;
 import net.grinder.engine.process.jruby.JRubyScriptEngine;
 import net.grinder.engine.process.jython.JythonScriptEngine;
 
 
 /**
  * Allow Scripts of multiple languages to be used.  This basically
  * passes scripts on to the appropriate engine.  It may be necessary
  * to use homogeneous scripts, but ultimately it would be cool if they
  * could be heterogeneous.
  *
  * @author Mike Stone
  */
 public final class MultilingualScriptEngine implements ScriptEngine {
     private Map<String, ScriptEngine> m_engineTypes;
     private ScriptEngine m_engine;
 
     public MultilingualScriptEngine() throws EngineException {
         // To see why these need to be initialized now instead of
         // during the initialise method, see the comment in the run
         // method of GrinderProcess.  Presumably that comment is
         // accurate.  JRuby probably doesn't strictly need to be
         // initialized, but might as well.  If this weren't necessary,
         // we could use a map of string to Class<? extends
         // ScriptEngine> instead, and initialize during the initialise
         // method.
         JythonScriptEngine jython = new JythonScriptEngine();
         JRubyScriptEngine jruby = new JRubyScriptEngine();
         m_engineTypes = new HashMap<String, ScriptEngine>();
         m_engineTypes.put("py", jython);
         m_engineTypes.put("jy", jython);
         m_engineTypes.put("rb", jruby);
         m_engineTypes.put("jrb", jruby);
     }
 
     @Override
     public void initialise(ScriptLocation script) throws EngineException {
         if (m_engine == null) {
             m_engine = m_engineTypes.get(getExtension(script));

            if (m_engine == null) {
                throw new EngineException("Could not find engine for script " + script + ", with extension " + getExtension(script));
            }
         }
 
         m_engine.initialise(script);
     }
 
     private String getExtension(ScriptLocation script) {
        String[] split = script.getFile().getName().split("\\.");
 
         // No extension...
         if (split.length < 2) {
             return "";
         }
 
         return split[split.length - 1];
     }
 
     @Override
     public WorkerRunnable createWorkerRunnable() throws EngineException {
         return m_engine.createWorkerRunnable();
     }
 
     @Override
     public WorkerRunnable createWorkerRunnable(Object testRunner) throws EngineException {
         return m_engine.createWorkerRunnable(testRunner);
     }
 
     @Override
     public void shutdown() throws EngineException {
         m_engine.shutdown();
     }
 
     @Override
     public String getDescription() {
         String version = "Multilingual 0.0.1";
 
         if (m_engine != null) {
             return version + ": " + m_engine.getDescription();
         }
 
         return version;
     }
 }
