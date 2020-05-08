 // Copyright © 2012 Steve McCoy under the MIT license.
 package edu.unh.cs.tact;
 
 import java.util.*;
 import static java.util.Collections.*;
 import java.lang.ref.*;
 import java.lang.reflect.*;
 
 public class Checker{
 	private static boolean enabled = true;
 
 	private static Map<Object, WeakReference<Thread>> owners =
 		synchronizedMap(new ThreadMap());
 
 	private static Map<Object, Object> runtimeGuarded =
 		synchronizedMap(new WeakHashMap<Object, Object>());
 
 	/** Aside from unit tests, don't call this manually. */
 	public static void check(Object o){
 		if(!enabled || o == null)
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
 
 	/** Aside from unit tests, don't call this manually. */
 	public static void guardByThis(Object o){
 		if(!enabled || o == null)
 			return;
 
 		if(!Thread.holdsLock(o))
 			throw new IllegalAccessError(String.format(
 				"BAD unguarded-access [this] (%s -> %s)", o, Thread.currentThread()));
 	}
 
 	/** Aside from unit tests, don't call this manually. */
 	public static void guardByStatic(Object o, String guard){
 		if(!enabled || o == null)
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
 
 	/** release releases o from the current thread's ownership.
 	@throws IllegalAccessError if the current thread does not own o.
 	*/
 	public static void release(Object o){
 		if(!enabled || o == null)
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
 
 	/** guardBy changes o's ownership from the default strategy to
 	a runtime guard. This has no effect on fields that have been annotated
 	with @GuardedBy.
 	@throws IllegalAccessError if a guard already exists
 	*/
 	public static void guardBy(Object o, Object guard){
 		if(!enabled || o == null)
 			return;
 
 		Object oldGuard =
 			runtimeGuarded.put(o, guard);
 
 		if(oldGuard != null && !oldGuard.equals(guard))
 			throw new IllegalAccessError(String.format("BAD new-guard (%s, %s -> %s)", o, oldGuard, guard));
 	}
 
 
 	/** init should be called before any of the Checker methods not injected by tact
 	will be called.
 	*/
 	public static void init(){
 		enabled = false; // TODO: Well, this name is a lie.
 	}
 
 	/** releaseAndStart atomically creates a new Thread with r, gives ownership
 	of r to the new Thread, and starts that Thread.
 	Use this when a runnable is created with the default
 	ownership strategy and you want to run it in another Thread.
 	*/
 	public static void releaseAndStart(Runnable r){
 		Thread t = new Thread(r);
 		giveTo(r, t);
 		t.start();
 	}
 
 	/** releaseAndStart atomically gives ownership of t to itself and starts it.
 	Use this for Threads created with the default ownership strategy. If you
 	have a class that extends Thread, this is what you want.
 	*/
 	public static void releaseAndStart(Thread t){
 		giveTo(t, t);
 		t.start();
 	}
 
 	private static synchronized void giveTo(Object o, Thread t){
 		release(o);
 		owners.put(o, new WeakReference<Thread>(t));
 	}
 }
