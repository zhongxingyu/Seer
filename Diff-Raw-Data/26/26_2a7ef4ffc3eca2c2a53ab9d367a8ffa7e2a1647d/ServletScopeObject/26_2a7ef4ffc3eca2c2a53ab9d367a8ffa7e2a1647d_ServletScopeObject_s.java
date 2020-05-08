 package org.qrone.r7.script;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 
import org.mozilla.javascript.Callable;
import org.mozilla.javascript.Context;
 import org.mozilla.javascript.Scriptable;
 import org.qrone.r7.parser.JSDeck;
 
 public class ServletScopeObject {
 	public ServletScope ss;
 	
 	public ServletScopeObject(ServletScope ss) {
 		this.ss = ss;
 	}
 	
 	protected Scriptable newScriptable(){
 		return JSDeck.getContext().newObject(ss.scope);
 	}
 	
 	/*
 	protected void callJSFunction(Callable func, Object thisObj, Object ... args){
 		func.call(JSDeck.getContext(), ss.scope, 
 				(Scriptable)Context.javaToJS(thisObj,ss.scope), args);
 	}
 	*/
 
 	protected URI resolvePath(String path) throws URISyntaxException {
 		return ss.uri.resolve(new URI(path));
 	}
 }
