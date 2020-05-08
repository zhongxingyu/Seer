 
 package javadb;
 
 import java.lang.reflect.Field;
 import java.util.*;
 
 class Pair<A, B> {
 	private A first;
 	private B second;
 
 	public Pair(A first, B second) {
 		super();
 		this.first = first;
 		this.second = second;
 	}
 
 	public int hashCode() {
 		int hashFirst = first != null ? first.hashCode() : 0;
 		int hashSecond = second != null ? second.hashCode() : 0;
 
 		return (hashFirst + hashSecond) * hashSecond + hashFirst;
 	}
 
 	public boolean equals(Object other) {
 		if (other instanceof Pair) {
 			Pair<A, B> otherPair = (Pair<A, B>) other;
 			return ((this.first == otherPair.first || (this.first != null
 					&& otherPair.first != null && this.first
 						.equals(otherPair.first))) && (this.second == otherPair.second || (this.second != null
 					&& otherPair.second != null && this.second
 						.equals(otherPair.second))));
 		}
 
 		return false;
 	}
 
 	public String toString() {
 		return "(" + first + ", " + second + ")";
 	}
 
 	public A getFirst() {
 		return first;
 	}
 
 	public void setFirst(A first) {
 		this.first = first;
 	}
 
 	public B getSecond() {
 		return second;
 	}
 
 	public void setSecond(B second) {
 		this.second = second;
 	}
 }
 
 
 public class JavaDBRuntime {
 	//private static JavaDBRuntime l1Runtime = new JavaDBRuntime();
 
 	private final static HashMap<Object, Object> table = new HashMap<Object, Object>();
 	private final static HashMap<Pair<Object, String>, Field> fieldTable = new HashMap<Pair<Object, String>, Field>();
 
 	private JavaDBRuntime() {
 	}
 
 	public static void putParent(Object k, Object p) {
 		table.put(k, p);
 	}
 
 	public static Object getParent(Object k) {
 		return table.get(k);
 	}
 
 	public static void registerField(Object o, String classname, String parName,
 			String fieldName) {
		Field f = null;
 		try {
			for (Class c = Class.forName(classname); f == null && c != null; c = c.getSuperclass()) {
				try {
					f = c.getDeclaredField(fieldName);
				} catch (Exception e) {
					f = null;
				}
			}
			f.setAccessible(true);

 			JavaDBRuntime.fieldTable.put(new Pair<Object, String>(o, parName), f);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static Object getFieldValue(Object reciver, Object o, String parName) {
 		Field f = JavaDBRuntime.fieldTable.get(new Pair<Object, String>(reciver, parName));
 		try {
 			return f.get(o);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public static void setFieldValue(Object o, Object value, Object reciver, String parName) {
 		Field f = JavaDBRuntime.fieldTable.get(new Pair<Object, String>(reciver, parName));
 		try {
 			f.set(o, value);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	// public static void main(String args[]) {
 	// 	JavaDBRuntime.getFieldValue(null, null, null);
 	// 	JavaDBRuntime.setFieldValue(null, null, null, null);
 	// 	JavaDBRuntime.registerField(null, null, null, null);
 	// }
 }
