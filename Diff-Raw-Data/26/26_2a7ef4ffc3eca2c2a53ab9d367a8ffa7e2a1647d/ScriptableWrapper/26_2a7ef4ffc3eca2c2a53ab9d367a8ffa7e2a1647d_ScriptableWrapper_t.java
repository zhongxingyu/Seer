 package org.qrone.r7.script;
 
 import org.mozilla.javascript.NativeJavaObject;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.Wrapper;
 
 
 public abstract class ScriptableWrapper<T> extends NativeJavaObject implements Scriptable, Wrapper{
	private static final long serialVersionUID = -2463022914755259679L;

 	public ScriptableWrapper() {
 	}
 	
 	public ScriptableWrapper(T javaObject) {
 	}
 	
 	public ScriptableWrapper(Scriptable scope, T javaObject, Class staticType) {
         super(scope, javaObject, staticType);
 	}
 }
