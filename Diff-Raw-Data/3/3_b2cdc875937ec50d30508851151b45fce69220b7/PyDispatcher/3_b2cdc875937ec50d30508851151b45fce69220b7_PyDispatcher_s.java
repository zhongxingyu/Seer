 // Copyright (C) 2005 - 2009 Philip Aston
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
 
 package net.grinder.engine.process.instrumenter.traditionaljython;
 
 import net.grinder.common.UncheckedGrinderException;
 import net.grinder.engine.process.ScriptEngine.Recorder;
 
 import org.python.core.Py;
 import org.python.core.PyObject;
 
 /**
  * A dispatcher that translates return types and exceptions from the script.
  *
  * <p>
 * The delegate {@link net.grinder.engine.process.ScriptEngine.Dispatcher
 * Dispatcher} can safely be invoked multiple times for the same test and
  * thread (only the outer invocation will be recorded). Consequently there
  * is no problem with our PyInstance instrumentation and Jython 1.1, where
  * the code path can make multiple calls through our instrumented invoke
  * methods.
  * </p>
  *
  * @author Philip Aston
  * @version $Revision: 4017 $
  */
 final class PyDispatcher {
 
   /**
    * Callback interface.
    *
    * @author Philip Aston
    * @version $Revision: 4057 $
    */
   interface Callable {
     PyObject call();
   }
 
   private final Recorder m_recorder;
 
   PyDispatcher(Recorder recorder) {
     m_recorder = recorder;
   }
 
   public PyObject dispatch(Callable callable) {
 
     try {
       m_recorder.start();
 
       boolean success = false;
 
       try {
         final PyObject result = callable.call();
 
         success = true;
 
         return result;
       }
       finally {
         m_recorder.end(success);
       }
     }
     catch (UncheckedGrinderException e) {
       // Don't translate our unchecked exceptions.
       throw e;
     }
     catch (Exception e) {
       throw Py.JavaError(e);
     }
   }
 }
