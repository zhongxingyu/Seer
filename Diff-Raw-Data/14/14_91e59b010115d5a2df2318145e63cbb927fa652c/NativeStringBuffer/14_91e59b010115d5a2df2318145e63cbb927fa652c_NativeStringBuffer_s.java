 package org.monkeyscript.lite;
 
 import java.util.Arrays;
 
 import org.mozilla.javascript.*;
 
 // org.mozilla.javascript.NativeString was used as a reference for implementation of this
 final class NativeStringBuffer extends AbstractBuffer {
 	
 	static final long serialVersionUID = 1251247849L;
 	
 	protected String getTypeTag() { return "String"; }
 	
 	static void init(Scriptable proto, Scriptable scope, boolean sealed) {
 		NativeStringBuffer obj = NativeStringBuffer.newEmpty();
 		obj.exportAsJSClass(MAX_PROTOTYPE_ID, scope, sealed);
 		obj.setPrototype(proto);
 	}
 	
 	static private NativeStringBuffer newEmpty() {
 		return new NativeStringBuffer(0);
 	}
 	
 	private NativeStringBuffer(int len) {
 		this.length = len;
 		this.chars = new char[len];
 	}
 	
 	private NativeStringBuffer(String str) {
 		this.length = str.length();
 		this.chars = str.toCharArray();
 	}
 	
 	private NativeStringBuffer(char[] chars) {
 		this.chars = chars;
 		this.length = chars.length;
 	}
 	
 	static private Object quickNewNativeStringBuffer(Context cx, Scriptable scope, Object target) {
 		return ScriptRuntime.newObject(cx, scope, "StringBuffer", new Object[] { target });
 	}
 	
 	private static NativeStringBuffer realThis(Object thisObj, IdFunctionObject f) {
 		if (!(thisObj instanceof NativeStringBuffer))
 			throw incompatibleCallError(f);
 		return (NativeStringBuffer)thisObj;
 	}
 	
     public Object execIdCall(IdFunctionObject f, Context cx, Scriptable scope, Scriptable thisObj, Object[] args) {
 		if (!f.hasTag(getTag())) {
 			return super.execIdCall(f, cx, scope, thisObj, args);
 		}
 		int id = f.methodId();
 		again:
 			for(;;) {
 				switch (id) {
 					case Id_constructor: {
 						boolean inNewExpr = (thisObj == null);
 						if (!inNewExpr) {
 							// IdFunctionObject.construct will set up parent, proto
 							return f.construct(cx, scope, args);
 						}
 						return jsConstructor(cx, scope, args);
 					}
 					
 					case Id_valueOf:
						return realThis(thisObj, f);
 					
 					case Id_toSource: {
 						return "(new StringBuffer(\""+ScriptRuntime.escapeString(realThis(thisObj, f).toString())+"\"))";
 					}
 				}
 				return super.execIdCall(f, cx, scope, thisObj, args);
 			}
 	}
 	
 	protected Object jsConstructor(Context cx, Scriptable scope, Object[] args) {
 		if ( args.length > 0 ) {
 			if ( args[0] instanceof Number )
 				return new NativeStringBuffer(ScriptRuntime.toInt32(args[0]));
 			return new NativeStringBuffer(MonkeyScriptRuntime.toCharArray(args[0]));
 		} else {
 			return NativeStringBuffer.newEmpty();
 		}
 	}
 	
 	@Override
 	public String toString() {
 		return (new String(chars, 0, (int)length)).intern();
 	}
 	
 	/* Make array-style property lookup work for buffers. */
 	@Override
 	public Object get(int index, Scriptable start) {
 		if (0 <= index && index < length) {
 			return toString().substring(index, index + 1);
 		}
 		return super.get(index, start);
 	}
 	
 	protected boolean safeThis(Scriptable thisObj) {
 		return thisObj instanceof NativeStringBuffer;
 	}
 	
 	protected Object toWildArray(Object arg) {
 		return MonkeyScriptRuntime.toCharArray(arg);
 	}
 	protected int getWildArrayLength(Object data) {
 		return ((char[])data).length;
 	}
 	
 	protected boolean rawEquals(Object obj) {
 		char[] test = null;
 		if ( obj.getClass() == char[].class )
 			test = (char[])obj;
 		else if ( obj instanceof NativeStringBuffer )
 			test = ((NativeStringBuffer)obj).chars;
 		else if ( obj instanceof String )
 			test = ((String)obj).toCharArray();
 		
 		if ( test != null )
 			return Arrays.equals(chars, test);
 		return false;
 	}
 	
 	public char[] toCharArray() {
 		return Arrays.copyOf(chars, (int)length);
 	}
 	
 	protected Object getArray() { return chars; }
 	protected void setArray(Object arr) { chars = (char[])arr; }
 	
 	private char[] chars;
 }
