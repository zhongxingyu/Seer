 package jsfun.examples.twitter.server;
 
 import jsonpp.Codec;
 import jsonpp.encode.Null;
import org.mozilla.javascript.*;
 
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Collection;
 
 
 public class ScriptableCodec implements Codec {
 
 
 	@Override
 	public String encodeKey(Object key) {
 		return "\"" + key.toString() + "\"";
 	}
 
 	public String encodeKey(String s) {
 		return s;
 	}
 
 	@Override
 	public String encodeValue(Object value) {
 		return value.toString();
 	}
 
	public String encodeValue(BaseFunction f) {
		return String.format("function() {\n   [native code, arity=%d]\n}", f.getArity());
	}

 	public String encodeValue(IdFunctionObject o) {
 		return String.format("function %s() { [native code for %s, arity=%d] }", o.getFunctionName(), o.getFunctionName(), o.getArity());
 	}
 
 	public String encodeValue(Null nil) {
 		return nil.js();
 	}
 
 	public String encodeValue(String o) {		
 		return "\"" + o + "\"";
 	}
 
 
 	@Override
 	public Iterator<Map.Entry> entries(Object o) {
 		return null;
 	}
 
 	public Iterator<Map.Entry> entries(NativeArray array) {
 		return null;
 	}
 
	public Iterator<Map.Entry> entries(BaseFunction f) {
		return null;
	}

 	public Iterator<Map.Entry> entries(final NativeObject object) {
 		final Object[] ids = object.getIds();
 		return new Iterator<Map.Entry>() {
 			int idx = 0;
 
 			public boolean hasNext() {
 				return idx < ids.length;
 			}
 
 			public Map.Entry next() {
 				return new Entry(object, idx++);
 			}
 			public void remove() {
 				throw new UnsupportedOperationException();
 			}
 		};
 	}
 
 	public <K,V> Iterator<Map.Entry<K,V>> entries(Map<K,V> m) {
 		return m.entrySet().iterator();
 	}
 
 	@Override
 	public Iterator iterator(Object o) {
 		return null;
 	}
 
 	public Iterator iterator(final NativeArray array) {
 		return new Iterator() {
 			int idx = 0;
 			@Override
 			public boolean hasNext() {
 				return idx < array.getLength();
 			}
 
 			@Override
 			public Object next() {
 				return array.get(idx++, array);
 			}
 
 			@Override
 			public void remove() {
 				throw new UnsupportedOperationException();
 			}
 		};
 	}
 
 	public Iterator iterator(Collection c) {
 		return c.iterator();
 	}
 
 	private static class Entry implements Map.Entry {
 		private Scriptable object;
 		private int idx;
 
 		public Entry(Scriptable object, int idx) {
 			this.object = object;
 			this.idx = idx;
 		}
 
 		@Override
 		public Object getKey() {
 			return object.getIds()[idx];
 		}
 
 		@Override
 		public Object getValue() {
 			Object key = getKey();
 			if (key instanceof String) {
 				return object.get((String)key, object);
 			} else {
 				return object.get((Integer)key, object);
 			}
 		}
 
 		@Override
 		public Object setValue(Object o) {
 			throw new UnsupportedOperationException();
 		}
 	}
 }
