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
 
 import com.liferay.portal.kernel.events.SimpleAction;
 import com.liferay.portal.kernel.scripting.Scripting;
 import com.liferay.portal.kernel.scripting.ScriptingExecutor;
 import com.liferay.portal.kernel.scripting.ScriptingUtil;
 
 /**
  * This class provides a liferay action which registers the ClojureScriptingExecutor to liferay for script execution.
  *
 * @see SimpleAction
 * @see ClojureScriptingExecutor
  */
 public class RegisterClojureExecutorAction extends SimpleAction {
 
     /**
      * Executes the action of creating and registering a {@link ClojureScriptingExecutor}.
      *
      * @param ids the company IDs for the portal (not used)
      */
     @Override
     public final void run(final String[] ids) {
         final ScriptingExecutor executor = createClojureScriptingExecutor();
 
         registerScriptingExecutor(executor);
     }
 
     /**
      * Creates a {@link ClojureScriptingExecutor} which is associated with the {@link ClassLoader} of the current
      * thread (the hook's {@link ClassLoader} rather than the Portal's under which scripts are invoked).
      *
      * @return the newly created {@link ScriptingExecutor}.
      */
     protected final ScriptingExecutor createClojureScriptingExecutor() {
         @SuppressWarnings("PMD.DoNotUseThreads")
         final Thread thread = Thread.currentThread();
         final ClassLoader classLoader = thread.getContextClassLoader();
 
         return new ClojureScriptingExecutor(classLoader);
     }
 
     /**
      * Registers the supplied {@link ScriptingExecutor} with liferay by calling {@link ScriptingUtil#getScripting()} to
      * obtain the scripting engine, and then {@link Scripting#addScriptionExecutor(String, ScriptingExecutor)}.
      *
      * @param executor the {@link ScriptingExecutor} to register.
      */
     protected final void registerScriptingExecutor(final ScriptingExecutor executor) {
         final String language = executor.getLanguage();
         final Scripting scripting = ScriptingUtil.getScripting();
 
         scripting.addScriptionExecutor(language, executor);
     }
 
 }
