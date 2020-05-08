 // Copyright (C) 2009 Philip Aston
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
 
 package net.grinder.engine.process.instrumenter.dcr;
 
 import java.lang.reflect.Method;
 
 import net.grinder.engine.process.ScriptEngine.Recorder;
 import net.grinder.script.NotWrappableTypeException;
 import net.grinder.util.weave.Weaver;
 import net.grinder.util.weave.Weaver.TargetSource;
 
 import org.python.core.PyClass;
 import org.python.core.PyFunction;
 import org.python.core.PyInstance;
 import org.python.core.PyMethod;
 import org.python.core.PyObject;
 import org.python.core.PyProxy;
 import org.python.core.PyReflectedFunction;
 
 
 /**
  * DCRInstrumenter for Jython 2.1, 2.2.
  *
  * @author Philip Aston
  * @version $Revision:$
  */
 final class Jython22Instrumenter extends DCRInstrumenter {
 
   /**
    * Constructor for DCRInstrumenter.
    *
    * @param weaver The weaver.
    * @param recorderRegistry The recorder registry.
    */
   public Jython22Instrumenter(Weaver weaver,
                               RecorderRegistry recorderRegistry)  {
     super(weaver, recorderRegistry);
   }
 
   /**
    * {@inheritDoc}
    */
   public String getDescription() {
     return "byte code transforming instrumenter for Jython 2.1/2.2";
   }
 
   @Override
   protected Object instrument(Object target, Recorder recorder)
     throws NotWrappableTypeException {
 
     if (target instanceof PyObject) {
       // Jython object.
       if (target instanceof PyInstance) {
         instrumentPublicMethodsByName(target,
                                       "invoke",
                                       TargetSource.FIRST_PARAMETER,
                                       recorder,
                                       true);
       }
       else if (target instanceof PyFunction) {
         instrumentPublicMethodsByName(target,
                                       "__call__",
                                       TargetSource.FIRST_PARAMETER,
                                       recorder,
                                       false);
       }
       else if (target instanceof PyMethod) {
         instrumentPublicMethodsByName(target,
                                       "__call__",
                                       TargetSource.FIRST_PARAMETER,
                                       recorder,
                                       false);
       }
       else if (target instanceof PyReflectedFunction) {
         final Method callMethod;
 
         try {
           callMethod = PyReflectedFunction.class.getMethod("__call__",
                                                           PyObject[].class,
                                                           String[].class);
         }
         catch (Exception e) {
           throw new NotWrappableTypeException(
             "Correct version of Jython?: " + e.getLocalizedMessage());
         }
 
         instrument(target, callMethod, TargetSource.FIRST_PARAMETER, recorder);
       }
       else if (target instanceof PyClass) {
         instrumentPublicMethodsByName(target,
                                       "__call__",
                                       TargetSource.FIRST_PARAMETER,
                                       recorder,
                                       false);
       }
       else {
         // Fail, rather than guess a generic approach.
         throw new NotWrappableTypeException("Unknown PyObject:" +
                                             target.getClass());
       }
     }
     else if (target instanceof PyProxy) {
       // Jython object that extends a Java class.
       // We can't just use the Java wrapping, since then we'd miss the
       // Jython methods.
       final PyObject pyInstance = ((PyProxy)target)._getPyInstance();
 
       instrumentPublicMethodsByName(pyInstance,
                                     "invoke",
                                     TargetSource.FIRST_PARAMETER,
                                     recorder,
                                     true);
     }
     else {
       return null;
     }
 
     return target;
   }
 
   private void instrumentPublicMethodsByName(Object target,
                                              String methodName,
                                              TargetSource targetSource,
                                              Recorder recorder,
                                              boolean includeSuperClassMethods)
     throws NotWrappableTypeException {
 
     // getMethods() includes superclass methods.
     for (Method method : target.getClass().getMethods()) {
       if (!includeSuperClassMethods &&
           target.getClass() != method.getDeclaringClass()) {
         continue;
       }
 
       if (!method.getName().equals(methodName)) {
         continue;
       }
 
       instrument(target, method, targetSource, recorder);
     }
   }
 }
