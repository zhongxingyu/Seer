 /*
  * This file is part of liferay-clojure-integration.
  *
  * liferay-clojure-integration is free software: you can redistribute it and/or
  * modify it under the terms of the GNU General Public License as published by the
  * Free Software Foundation, either version 3 of the License, or (at your option)
  * any later version.
  *
  * liferay-clojure-integration is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
  * more details.
  *
  * You should have received a copy of the GNU General Public License along with
  * liferay-clojure-integration. If not, see <http://www.gnu.org/licenses/>.
  */
 package au.com.permeance.liferay.clojure;
 
 import com.liferay.portal.kernel.log.Log;
 import com.liferay.portal.kernel.log.LogFactoryUtil;
 import com.liferay.portal.kernel.scripting.BaseScriptingExecutor;
 import com.liferay.portal.kernel.scripting.ExecutionException;
 import com.liferay.portal.kernel.scripting.ScriptingException;
 
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.Callable;
 
 /**
  * This class provides the interface between liferay's {@link com.liferay.portal.kernel.scripting.Scripting} API and
  * the clojure scripting runtime. It implements the necessary interfaces for liferay to call scripts and to gather
  * results.
  */
 public class ClojureScriptingExecutor extends BaseScriptingExecutor {
 
     /**
      * The language identifier string for this language.
      */
     private static final String LANGUAGE = "clojure";
 
     /**
      * Stores the logger for reporting errors.
      */
     private static final Log LOG = LogFactoryUtil.getLog(ClojureScriptingExecutor.class);
 
     /**
      * Stores the class loader under which to invoke scripts.
      */
     private final transient ClassLoader hookClassLoader;
 
     /**
      * Creates a new instance of ClojureScriptingExecutor using the current thread's class loader.
      *
     * @see {@link Thread#currentThread()}
     * @see {@link Thread#getContextClassLoader()}
      */
     public ClojureScriptingExecutor() {
         this(Thread.currentThread()
                    .getContextClassLoader());
     }
 
     /**
      * Creates a new instance of ClojureScriptingExecutor using the supplied class loader.
      *
      * @param hookClassLoader the {@link ClassLoader} under which to invoke scripts (this should normally be the hook's
      *                        {@link ClassLoader} as it has access to the relevant clojure JARs.
      */
     public ClojureScriptingExecutor(final ClassLoader hookClassLoader) {
         super();
         this.hookClassLoader = hookClassLoader;
     }
 
     /**
      * Evaluates the supplied script under the clojure runtime.
      *
      * @param allowedClasses a set of class names filtering which objects can be accessed. Note: restricting class
      *                       access is not supported by this executor, so this parameter must be null or empty.
      * @param inputObjects   a map of input object names and values to expose to the script during execution.
      * @param outputNames    a set of output names to exopse to the script during execution. Note: is expected that the
      *                       script is aware of these names and returns a map accordingly containing all relevant entries.
      * @param script         the script to execute.
      *
      * @return the result of executing the supplied script and extracting the map entries specified by outputNames from
      *         its result.
      *
      * @throws ScriptingException if an error occurs while executing the supplied script.
      */
     @Override
     public final Map<String, Object> eval(final Set<String> allowedClasses,
                                           final Map<String, Object> inputObjects,
                                           final Set<String> outputNames,
                                           final String script) throws ScriptingException {
 
         if (allowedClasses != null && !allowedClasses.isEmpty()) {
             throw new ExecutionException("Constrained execution not supported for Clojure");
         }
 
         final Callable<Map<String, Object>> callable = createCallableForScript(inputObjects, outputNames, script);
 
         return callAndWrapException(callable);
     }
 
     /**
      * This method returns the language string used to identify this language (always {@link #LANGUAGE}).
      *
      * @return always returns {@link #LANGUAGE}.
      */
     @Override
     public final String getLanguage() {
         return LANGUAGE;
     }
 
     /**
      * Creates a new {@link ClojureScriptingCallable} with the supplied parameters, and then wraps that callable in a
      * {@link CallUnderClassLoader} associated with the {@link #hookClassLoader}. The resulting object is then returned.
      *
      * @param inputObjects the input objects to expose to the supplied script during execution.
      * @param outputNames  the output names to expose to the supplied script during execution.
      * @param script       the script to execute.
      *
      * @return returns a {@link Callable} which executes the supplied script.
      */
     protected final Callable<Map<String, Object>> createCallableForScript(final Map<String, Object> inputObjects,
                                                                           final Set<String> outputNames,
                                                                           final String script) {
 
         final Callable<Map<String, Object>> callable = new ClojureScriptingCallable(inputObjects, outputNames, script);
 
         return new CallUnderClassLoader<Map<String, Object>>(callable, hookClassLoader);
     }
 
     /**
      * This method calls the supplied {@link Callable}, wrapping any exception it may through with a
      * {@link ScriptingException} compatable with the {@link BaseScriptingExecutor} API.
      *
      * @param callable the callable to invoke.
      * @param <V>      the result type of method <tt>call</tt>
      *
      * @return computed result
      *
      * @throws ScriptingException if {@link Callable#call()} throws an exception, it will be used as the cause of the
      *                            thrown {@link ScriptingException}.
      */
     @SuppressWarnings("PMD.AvoidCatchingGenericException")
     protected final <V> V callAndWrapException(final Callable<V> callable) throws ScriptingException {
         try {
             return callable.call();
         }
 // CHECKSTYLE.OFF: IllegalCatch
         catch (final Exception e) {
 // CHECKSTYLE.ON: IllegalCatch
             LOG.error("Error executing clojure script", e);
             throw new ScriptingException("Error executing clojure script", e);
         }
     }
 
 }
