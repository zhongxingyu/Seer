 /*
  * Copyright (c) 2010 Sharegrove Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package org.msjs.script;
 
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 import org.apache.log4j.Logger;
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.ContextFactory;
 import org.mozilla.javascript.Function;
 import org.mozilla.javascript.JavaScriptException;
 import org.mozilla.javascript.NativeJavaObject;
 import org.mozilla.javascript.RhinoException;
 import org.mozilla.javascript.Script;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.ScriptableObject;
 import org.mozilla.javascript.WrappedException;
 import org.msjs.servlet.RedirectException;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FilenameFilter;
 import java.util.Set;
 import java.util.UUID;
 import java.util.concurrent.ConcurrentHashMap;
 
 /**
  * A script context in which the script "msjs.library" is loaded. Furthermore,
  * the <code>msjs.setContext()</code> method is called with this object, which provides
  * logging capabilities from within msjs.
  */
 public class MsjsScriptContext extends ScriptContext {
 
     private static final Logger logger = Logger.getLogger(MsjsScriptContext.class);
 
     private final ScriptLocator locator;
     private Injector injector;
     private static final ConcurrentHashMap<String,Object> singletonScope =
             new ConcurrentHashMap<String, Object>();
     private final UUID uuid;
     private final ScriptableObject bindings;
     private String loadingPackage;
     private Scriptable loadingScope;
     private final FunctionParser functionParser;
 
 
     /**
      * Create a new context for msjs script execution.
      * @param cxFactory The factory for Rhino execution contexts.
      * @param locator ScriptLocator to use to find scripts that are loaded by
      * other scripts, or externally, through
      * @param functionParser
      * {@link MsjsScriptContext#loadPackage(String)}
      */
     @Inject
     public MsjsScriptContext(ContextFactory cxFactory, ScriptLocator locator,
                              FunctionParser functionParser) {
         super(cxFactory);
         this.locator = locator;
         this.uuid = UUID.randomUUID();
         this.functionParser = functionParser;
 
 
         Scriptable msjsScope = makeObject();
         try{
             runScript(locator.getScript("msjs"), msjsScope);
         } catch (FileNotFoundException e){
             throw new RuntimeException(e);
         }
 
         final Scriptable msjs = (Scriptable) msjsScope.get("msjs", msjsScope);
 
         //FIXME? this publishes a reference to an object that is not fully constructed
         final Object[] contextArg = {this, getGlobalScope()};
         bindings = (ScriptableObject) callMethod(msjs, "bindContext", contextArg);
 
     }
 
     /**
      * Load a package by name, such as "msjs.node", using this instance's ScriptLocator.
      *
      * @param name The name of the package to load.
      * @return The value returned by the script.
      */
     public Object loadPackage(String name) {
         try{
             final Script script = locator.getScript(name);
             String outerPackage = loadingPackage;
             Scriptable outerScope = loadingScope;
             loadingPackage = name;
             loadingScope = makeObject();
             runScript(script, loadingScope);
 
             Object[] args ={ loadingScope };
 
             //TODO: This isn't necessary in production config
             callMethodOnBinding("msjs", "assignDebugNames", args);
             
             loadingPackage = outerPackage;
             loadingScope = outerScope;
             return bindings.get(name, bindings);
         } catch (Exception e){
             throw launderException(e);
         }
     }
 
     public String getLoadingPackage() {
         return loadingPackage;
     }
 
     public Object getFromSingletonScope(String name){
         if (!singletonScope.containsKey(name)) return Context.getUndefinedValue();
         return singletonScope.get(name);
     }
 
     public Object assignToSingletonScope(String name, Object value){
         singletonScope.putIfAbsent(name, value);
         return singletonScope.get(name);
     }
 
     public ScriptableObject getBindings() {
         return bindings;
     }
 
     @Override
     protected Scriptable runScript(final Script script, Scriptable scope) {
         try{
             return super.runScript(script, scope);
         }catch(Exception e){
             throw launderException(e);
         }
     }
 
     public Object callMethodOnBinding(final String binding, final String method,
                                       final Object[] args) {
         try{
             return callMethod((Scriptable) bindings.get(binding, bindings), method, args);
         }catch (Exception e){
             throw launderException(e);
         }
     }
 
 
     /**
      * Provides logging capabilities to msjs scripts, as "INFO" messages.
      * @param msg String to be logged.
      */
     public void log(String msg){ logger.info(msg); }
     public void log(String msg, Exception e){ logger.warn(msg, e); }
 
 
     @Inject
     //The MsjsScriptContext needs a refence to the injector so that it can
     //make java objects for the script. Not pretty, but fairly simple, at least.
     public void setInjector(Injector injector){
         this.injector = injector;
     }
 
     public void redirect(String url){
         throw new RedirectException(url);
     }
 
     
 
     public Object inject(String className){
         try{
             return injector.getInstance(Class.forName(className));
         }catch (Exception e){
             throw launderException(e);
         }
     }
 
     public String getId(){
         return uuid.toString();
     }
 
     public static void clearSingletonScope() {
         singletonScope.clear();
     }
 
     public RuntimeException launderException(Exception e) throws RuntimeException {
         if (e instanceof JavaScriptException){
             Object value = ((JavaScriptException) e).getValue();
             if (value instanceof NativeJavaObject){
                 Object njo = ((NativeJavaObject) value).unwrap();
                 if (njo instanceof Throwable){
                     launderException(asRuntimeException((Throwable) njo));
                 }
             }
         }
 
         RuntimeException throwMe;
         if (e instanceof WrappedException){
             Throwable t = (((WrappedException) e).getWrappedException());
 
             throwMe = asRuntimeException(t);
         } else if (e instanceof RhinoException){
             final RhinoException re = (RhinoException) e;
             String msg = re.details() + re.getScriptStackTrace(scriptNameFilter);
             throwMe = new RuntimeException(msg, e);
         } else {
             throwMe = asRuntimeException(e);
         }
 
         throw throwMe;
     }
 
     private RuntimeException asRuntimeException(final Throwable t) {
         final RuntimeException throwMe;
         throwMe = t instanceof RuntimeException ? (RuntimeException) t :
                   new RuntimeException(t);
         return throwMe;
     }
 
     private static final FilenameFilter scriptNameFilter = new FilenameFilter(){
         @Override
         public boolean accept(final File file, final String name) {
             return !(name.endsWith(".java"));
         }
     };
 
     public ScriptableObject getFreeVariables(Function f){
         Set<String> vars = functionParser.getFreeVariables(f);
         Scriptable scope = f.getParentScope();
 
         ScriptableObject jsSet = makeObject();
         for (String freeVar : vars){
            Object value = getFromScope(scope, freeVar);
            if (value == Scriptable.NOT_FOUND) value = Context.getUndefinedValue();
            jsSet.put(freeVar, jsSet, value);
         }
         return jsSet;
     }
 
 
 
 }
