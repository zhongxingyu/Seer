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
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.ContextFactory;
 import org.mozilla.javascript.Script;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.ScriptableObject;
 
 /**
  * The base JavaScript execution environment within msjs. All scripts used within
  * msjs should run in a context provided by an instance of this class, or one of
  * its subclasses.
  */
 public class ScriptContext {
     private static ScriptableObject topLevel;
 
     protected ScriptableObject getGlobalScope() {
         return globalScope;
     }
 
     private final ScriptableObject globalScope;
     /**
      * The factory for execution contexts.
      */
     private final ContextFactory cxFactory;
 
     /**
      * Creates a new ScriptContext, sharing a "standard objects"
      * scope with all other instances of this class, via a static member.
      * This is important, since Rhino implements "instanceof" by walking up
      * the prototype chain and comparing ancestor objects via ==.
      *
      * @param cxFactory The factory for execution contexts.
      */
     @Inject
     public ScriptContext(ContextFactory cxFactory) {
         this.cxFactory = cxFactory;
         try{
             Context cx = cxFactory.enterContext();
 
             ScriptableObject topLevel = getTopLevel(cx);
 
             //now setup the scope
             globalScope = (ScriptableObject) cx.newObject(topLevel);
             globalScope.setPrototype(topLevel);
             globalScope.setParentScope(null);
         } finally{
             Context.exit();
         }
     }
 
     public ScriptableObject makeObject() {
         try{
             Context cx = cxFactory.enterContext();
             return (ScriptableObject) cx.newObject(globalScope);
         } finally{
             Context.exit();
         }
     }
 
     public ScriptableObject makeArray(Object[] elements) {
         try{
             Context cx = cxFactory.enterContext();
             return (ScriptableObject) cx.newArray(globalScope, elements);
         } finally{
             Context.exit();
         }
 
     }
 
     //This is not merely an optimization to share the topLevel scope -- it's important
     //that all the scripts that we run share these objects, so that things like instanceof
     //work properly
     private static synchronized ScriptableObject getTopLevel(final Context cx) {
         if (topLevel == null){
             topLevel = cx.initStandardObjects(null, false);
             //Force all the stuff we need to be loaded. These objects are meant to be lazy
             //loaded by Rhino but that doesn't work since the scope is sealed.
             String loadMe =
                     "RegExp; getClass; java; Packages; JavaAdapter; XML.ignoreWhitespace=false;" +
                     "XML.prettyIndent=false;XML.prettyPrinting = false;";
             cx.evaluateString(topLevel, loadMe, "lazyLoad", 0, null);
             topLevel.sealObject();
             //it would be nice to call topLevel.sealObject
             //but that breaks RegExp. s long as we never allow
             //direct access to topLevel, it should be hard to modify
         }
         return topLevel;
     }
 
 
     protected Scriptable runScript(Script script, Scriptable runScope) {
         Context cx = cxFactory.enterContext();
         try{
             runScope.setPrototype(globalScope);
             runScope.setParentScope(null);
             script.exec(cx, runScope);
             return runScope;
         } finally{
             Context.exit();
         }
     }
 
     public Object callMethod(final Scriptable jsObject, final String methodName,
                              final Object[] inArgs) {
         Object[] args = new Object[inArgs.length];
         Scriptable start = makeObject();
         cxFactory.enterContext();
         try{
             for (int i = 0; i < inArgs.length; i++){
                 args[i] = Context.javaToJS(inArgs[i], start);
             }
             return ScriptableObject.callMethod(jsObject, methodName, args);
         } finally{
             Context.exit();
         }
     }
 
 
 }
