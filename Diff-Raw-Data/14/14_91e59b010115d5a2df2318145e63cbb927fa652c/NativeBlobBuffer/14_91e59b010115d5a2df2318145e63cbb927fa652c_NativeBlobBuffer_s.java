 package org.monkeyscript.lite;
 
 import java.util.Arrays;
 
 import org.mozilla.javascript.*;
 
 // org.mozilla.javascript.NativeString was used as a reference for implementation of this
 final class NativeBlobBuffer extends AbstractBuffer {
 	
 	static final long serialVersionUID = 1251247849L;
 	
 	protected String getTypeTag() { return "Blob"; }
 	
 	static void init(Scriptable proto, Scriptable scope, boolean sealed) {
 		NativeBlobBuffer obj = NativeBlobBuffer.newEmpty();
 		obj.exportAsJSClass(MAX_PROTOTYPE_ID, scope, sealed);
 		obj.setPrototype(proto);
 	}
 	
 	static private NativeBlobBuffer newEmpty() {
 		return new NativeBlobBuffer(0);
 	}
 	
 	private NativeBlobBuffer(int len) {
 		this.length = len;
 		this.bytes = new byte[0];
 	}
 	
 	private NativeBlobBuffer(byte[] bytes) {
 		this.bytes = bytes;
 		this.length = bytes.length;
 	}
 	
 	static private Object quickNewNativeBlobBuffer(Context cx, Scriptable scope, Object target) {
 		return ScriptRuntime.newObject(cx, scope, "BlobBuffer", new Object[] { target });
 	}
 	
 	private static NativeBlobBuffer realThis(Object thisObj, IdFunctionObject f) {
 		if (!(thisObj instanceof NativeBlobBuffer))
 			throw incompatibleCallError(f);
 		return (NativeBlobBuffer)thisObj;
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
 						byte[] b = realThis(thisObj, f).bytes;
 						StringBuffer sb = new StringBuffer("(new BlobBuffer([]))");
 						for (int i = 0; i < b.length; i++) {
 							if (i > 0)
 								sb.insert(sb.length()-3, ", ");
 							sb.insert(sb.length()-3, Integer.toString(MonkeyScriptRuntime.byteToHighInt(b[i])));
 						}
 						return sb.toString();
 					}
 				}
 				return super.execIdCall(f, cx, scope, thisObj, args);
 			}
 	}
 	
 	protected Object jsConstructor(Context cx, Scriptable scope, Object[] args) {
 		if ( args.length > 0 ) {
 			if ( args[0] instanceof Number )
 				return new NativeBlobBuffer(ScriptRuntime.toInt32(args[0]));
 			return new NativeBlobBuffer(MonkeyScriptRuntime.toByteArray(args[0]));
 		} else {
 			return NativeBlobBuffer.newEmpty();
 		}
 	}
 	
 	/* Make array-style property lookup work for buffers. */
 	@Override
 	public Object get(int index, Scriptable start) {
 		if (0 <= index && index < length) {
 			return MonkeyScriptRuntime.newBlob(bytes[index], start);
 		}
 		return super.get(index, start);
 	}
 	
 	protected boolean safeThis(Scriptable thisObj) {
 		return thisObj instanceof NativeBlobBuffer;
 	}
 	
 	protected Object toWildArray(Object arg) {
 		return MonkeyScriptRuntime.toByteArray(arg);
 	}
 	protected int getWildArrayLength(Object data) {
 		return ((byte[])data).length;
 	}
 	
 	protected boolean rawEquals(Object obj) {
 		byte[] test = null;
 		if ( obj.getClass() == byte[].class )
 			test = (byte[])obj;
 		else if ( obj instanceof NativeBlobBuffer )
 			test = ((NativeBlobBuffer)obj).bytes;
 		else if ( obj instanceof NativeBlob )
 			test = ((NativeBlob)obj).toByteArray();
 		
 		if ( test != null )
 			return Arrays.equals(bytes, test);
 		return false;
 	}
 	
 	public byte[] toByteArray() {
 		return Arrays.copyOf(bytes, (int)length);
 	}
 	
 	protected Object getArray() { return bytes; }
 	protected void setArray(Object arr) { bytes = (byte[])arr; }
 	
 	private byte[] bytes;
 }
