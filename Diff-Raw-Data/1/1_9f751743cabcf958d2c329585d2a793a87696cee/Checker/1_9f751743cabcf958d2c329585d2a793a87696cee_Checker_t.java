 // Copyright © 2012 Steve McCoy under the MIT license.
 package edu.unh.cs.tact;
 
 import java.util.*;
 import static java.util.Collections.*;
 import java.lang.ref.*;
 import java.lang.reflect.*;
 
 public class Checker{
 	private static Map<Object, WeakReference<Thread>> owners =
 		synchronizedMap(new WeakHashMap<Object, WeakReference<Thread>>());
 
 	private static Map<Object, Object> runtimeGuarded =
 		synchronizedMap(new WeakHashMap<Object, Object>());
 
 	public static void check(Object o){
 		if(o == null)
 			return;
 
 		Thread ct = Thread.currentThread();
 
 		Object guard = runtimeGuarded.get(o);
 		if(guard != null){
 			if(!Thread.holdsLock(guard))
 				throw new IllegalAccessError(String.format(
 					"BAD unguarded-access [general] (%s -> %s)",
 					o, Thread.currentThread()));
			return;
 		}
 
 		WeakReference<Thread> ref = owners.get(o);
 
 		if(ref == null){
 			owners.put(o, new WeakReference<Thread>(ct));
 			//System.err.printf("OK claim \"%s\" -> %s\n", o, ct);
 			return;
 		}
 
 		Thread owner = ref.get();
 		if(owner == null)
 			throw new IllegalAccessError(String.format("BAD re-thread \"%s\" -> %s\n", o, ct));
 
 		if(owner.equals(ct)){
 			//System.err.printf("OK access (%s -> %s)\n", o, ct);
 			return;
 		}
 
 		throw new IllegalAccessError(String.format("BAD access (%s -> %s)\n", o, ct));
 	}
 
 	public static void guardByThis(Object o){
 		if(o == null)
 			return;
 
 		if(!Thread.holdsLock(o))
 			throw new IllegalAccessError(String.format(
 				"BAD unguarded-access [this] (%s -> %s)", o, Thread.currentThread()));
 	}
 
 	public static void guardByStatic(Object o, String guard){
 		if(o == null)
 			return;
 
 		int fieldPos = guard.lastIndexOf('.');
 		if(fieldPos == -1 || fieldPos == guard.length()-1)
 			throw new AssertionError("Bad guard name: \""+guard+"\"");
 
 		String className = guard.substring(0, fieldPos);
 		String field = guard.substring(fieldPos+1);
 
 		Object g = null;
 		try{
 			Class<?> c = Class.forName(className);
 			Field f = c.getDeclaredField(field);
 			boolean acc = f.isAccessible();
 			f.setAccessible(true);
 			g = f.get(null);
 			f.setAccessible(acc);
 		}catch(ClassNotFoundException e){
 			throw new RuntimeException(e);
 		}catch(NoSuchFieldException e){
 			throw new RuntimeException(e);
 		}catch(IllegalAccessException e){
 			throw new RuntimeException(e);
 		}
 
 		if(!Thread.holdsLock(g))
 			throw new IllegalAccessError(String.format(
 				"BAD unguarded-access [static] (%s -> %s)", o, Thread.currentThread()));
 	}
 
 	public static void release(Object o){
 		if(o == null)
 			return;
 
 		Thread ct = Thread.currentThread();
 
 		WeakReference<Thread> ref = owners.get(o);
 		if(ref == null)
 			throw new IllegalAccessError(
 				String.format("BAD release-unowned (%s <- %s)", o, ct));
 
 		Thread owner = ref.get();
 		if(owner == null){
 			//System.err.printf("OK release-again (%s <- %s)", o, ct);
 			return;
 		}
 
 		if(owner.equals(ct)){
 			owners.remove(o);
 			//System.err.printf("OK release (%s <- %s)\n", o, ct);
 			return;
 		}
 
 		throw new IllegalAccessError(String.format("BAD release (%s <- %s)\n", o, ct));
 	}
 
 	public static void guardBy(Object o, Object guard){
 		Object oldGuard =
 			runtimeGuarded.put(o, guard);
 
 		if(oldGuard != null && !oldGuard.equals(guard))
 			throw new IllegalAccessError(String.format("BAD new-guard (%s, %s -> %s)", o, oldGuard, guard));
 	}
 
 
 	public static void releaseAndStart(Runnable r){
 		Thread t = new Thread(r);
 		giveTo(r, t);
 		t.start();
 	}
 
 	public static void releaseAndStart(Thread t){
 		giveTo(t, t);
 		t.start();
 	}
 
 	private static synchronized void giveTo(Object o, Thread t){
 		release(o);
 		owners.put(o, new WeakReference<Thread>(t));
 	}
 }
