 package org.qrone.r7.script;
 
import org.mozilla.javascript.Context;
 import org.mozilla.javascript.NativeJavaObject;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.Wrapper;
 
 
 public abstract class ScriptableWrapper<T> extends NativeJavaObject implements Scriptable, Wrapper{
 	public ScriptableWrapper() {
 	}
 	
 	public ScriptableWrapper(T javaObject) {
 	}
 	
 	public ScriptableWrapper(Scriptable scope, T javaObject, Class staticType) {
         super(scope, javaObject, staticType);
 	}
 }
