 package org.innobuilt.fincayra;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.jsoup.nodes.Element;
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.RhinoException;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.ScriptableObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 /*   Copyright 2010 Jesse Piascik
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
 public class MergeEngine {
 	private static final Logger LOGGER = LoggerFactory.getLogger(MergeEngine.class);
 	private String pageDir = null;
 	private String jsDir = null;
 	private FincayraScriptable topScope = null;
 
 	public void init(boolean mainEngine) throws RhinoException, IOException {
 		
 		Context cx = Context.enter();
 		topScope = new FincayraScriptable(this);
 		topScope.initStandardObjects(cx,false);
		cx.setOptimizationLevel(1);
 		cx.setLanguageVersion(Context.VERSION_1_7);
 
 		// Define some global functions particular to the shell. Note
 		// that these functions are not part of ECMA.
 		// NOTE: cannot use these functions directly in request scope, must define in global javaScript
 		String[] names = { "print", "load", "logger", "hasProperty" };
 		topScope.defineFunctionProperties(names, FincayraScriptable.class, ScriptableObject.DONTENUM);
 		
 		
 		if (mainEngine){
 			//***************************************************
 			//Load fincayra JavaScript file
 			//***************************************************
 			String fincayraJs = jsDir + "/fincayra.js";
 			LOGGER.info("Loading fincayra javascript into topScope:{}", fincayraJs);
 			FincayraScriptable.loadFile(cx, topScope, fincayraJs);
 		}
 		
 		//***************************************************
 		//Load global JavaScript file
 		//***************************************************
 		String rootJs = jsDir + "/root.js";
 		LOGGER.info("Loading root javascript into topScope:{}", rootJs);
 		FincayraScriptable.loadFile(cx, topScope, rootJs);
 		//starting = false;
 		Context.exit();
 	}
 	
 	public void destroy() throws RhinoException, IOException {
 		Context cx = Context.enter();
 		Scriptable scope  = cx.newObject(topScope);
 		scope.setPrototype(topScope);
 		scope.setParentScope(null);
 		FincayraScriptable.loadFile(cx, topScope, jsDir + "/destroy.js");
 		Context.exit();		
 	}
 
 	/**
 	 * 
 	 * <h4>This is where the magic happens!!!</h4>
 	 * <p>
 	 * Here we execute the javaScript for a page after the html for that page has been parsed.  We place the
 	 * parsed html into a <a href="http://jsoup.org/apidocs/org/jsoup/nodes/Document.html">org.jsoup.nodes.Document</a> and provide it to the executing javaScript in the "document"
 	 * variable.   We provide the convenience function "$" to the <a href="http://jsoup.org/apidocs/org/jsoup/nodes/Element.html#select(java.lang.String)">org.jsoup.nodes.Element's select method</a>.
 	 * When you call $("body") in your javaScript it executes the documents select method, so it's allot like JQuery, but on the server.  
 	 * <p>
 	 * We also provide some nice functions and variables to the executing javaScript.
 	 * 
 	 * <dl>
 	 * <dt>$l(path) or load(path)</dt><dd>Load a javscript file.</dd>
 	 * </dt>
 	 * 
 	 * @param pageJs - The full path to the page javaScript 
 	 * @param context - A FincayraContext to pass to the engine
 	 * @return The FincayraContext passed in after being modified by the pageJs execution 
 	 * @throws IOException 
 	 */
 	public Element merge(String pageJs, FincayraContext context) throws IOException {
 		Context cx = Context.enter();
 		context.setRhinoContext(cx);
 		Scriptable scope  = cx.newObject(topScope);
 		scope.setPrototype(topScope);
 		scope.setParentScope(null);
 		
 		//TODO Sharing scopes https://developer.mozilla.org/En/Rhino_documentation/Scopes_and_Contexts#Sharing_Scopes
 
 		context.setCurrentPage(pageJs);
 		// pass a context object to the engine with request, response and
 		// service facade (Provided by Application class)
 		scope.put("context", scope, Context.javaToJS(context, scope));
 		
 		try {
 			//***************************************************
 			//Load global request JavaScript file
 			//***************************************************
 			String requestJs = this.jsDir + "/request/request.js";
 			LOGGER.debug("Loading gloabal request javascript into scope:{}", requestJs);
 			FincayraScriptable.loadFile(cx, scope, requestJs);
 
 		} catch (RhinoException re) {
 			//TODO let's do more here!!!, Like use a default error page
 			LOGGER.error("Error in page:{}",re);
 		} 
 
 		
 		// Make sure we have the most recent context object
 		context = FincayraScriptable.getFincayraContext(scope);
 		
 		Context.exit();
 		
 		return context.getElement();
 	}
 
 	
 	public Element getElement(Scriptable scope, String name) {
 		return (Element) Context.jsToJava(scope.get(name, scope), Element.class);
 	}
 
 	public boolean exists(String page) {
 		return getFile(page).exists();
 	}
 
 	public File getFile(String page) {
 		page = pageDir + page;
 		return (new File(page));
 	}
 
 	public String getPageDir() {
 		return pageDir;
 	}
 
 	public void setPageDir(String pageDir) {
 		this.pageDir = pageDir;
 	}
 
 	public void setJsDir(String jsDir) {
 		this.jsDir = jsDir;
 	}
 
 	public String getJsDir() {
 		return jsDir;
 	}
 
 	public FincayraScriptable getTopScope() {
 		return topScope;
 	}
 
 	public void setTopScope(FincayraScriptable topScope) {
 		this.topScope = topScope;
 	}
 
 }
