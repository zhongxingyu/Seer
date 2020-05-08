 // Copyright © 2012 Steve McCoy under the MIT license.
 package edu.unh.cs.tact;
 
 import org.junit.*;
 import static org.junit.Assert.*;
import java.util.*;
 
 public class CheckerTest{
 	@Test public void skipNull(){
 		Checker.check(null);
 	}
 
 	@Test public void goodSameThread(){
 		Object o = new Object();
 		Checker.check(o);
 		Checker.check(o);
 	}
 
 	@Test(expected=IllegalAccessError.class)
 	public void defaultOwner() throws Throwable{
 		final Object o = new Object();
 		Checker.check(o);
 
 		try{
 			doInAnotherThread(new Runnable(){
 				public void run(){
 					Checker.check(o);
 				}
 			});
 		}catch(RuntimeException e){
 			throw Util.rootCause(e);
 		}
 	}
 
 	@Test public void goodRuntimeGuard(){
 		final Object o = new Object();
 		final String s = "I'm a guard";
 		Checker.guardBy(o, s);
 		doInAnotherThread(new Runnable(){
 			public void run(){
 				synchronized(s){
 					Checker.check(o);
 				}
 			}
 		});
 	}
 
 	@Test(expected=IllegalAccessError.class)
 	public void extraRuntimeGuard(){
 		Object o = new Object();
 		String s = "I'm a guard";
 		Checker.guardBy(o, s);
 		Checker.guardBy(o, "I'm different!");
 	}
 
 	@Test public void goodStaticRuntimeGuard(){
 		final Object o = new Object();
 		Checker.guardBy(o, CheckerTest.class);
 		doInAnotherThread(new Runnable(){
 			public void run(){
 				staticCheck(o);
 			}
 		});
 	}
 
 	private synchronized static void staticCheck(Object o){
 		Checker.check(o);
 	}
 
 	private void doInAnotherThread(Runnable r){
 		ExceptionGrabber g = new ExceptionGrabber(r);
 		Thread t = new Thread(g);
 		t.start();
 		try{
 			t.join();
 		}catch(InterruptedException e){
 			// hrm
 			throw new RuntimeException(e);
 		}
 		if(g.err != null)
 			throw new RuntimeException(g.err);
 	}
 
 	private static class ExceptionGrabber implements Runnable{
 		private final Runnable r;
 		public Throwable err = null;
 
 		public ExceptionGrabber(Runnable r){
 			this.r = r;
 		}
 
 		public void run(){
 			try{
 				r.run();
 			}catch(Throwable e){
 				err = e;
 			}
 		}
 	}
 
 	private static class ThisDummy{
 		// tact won't actually inject this class; these are for illustration
 		@GuardedBy("this") int n;
 		@GuardedBy("edu.unh.cs.tact.CheckerTest.ThisDummy.slock") int m;
 		static final Object slock = new Object();
 	}
 
 	@Test public void goodSimpleThis(){
 		ThisDummy d = new ThisDummy();
 		synchronized(d){
 			Checker.guardByThis(d);
 		}
 	}
 
 	@Test(expected=IllegalAccessError.class)
 	public void badSimpleThis(){
 		ThisDummy d = new ThisDummy();
 		Checker.guardByThis(d);
 	}
 
 	@Test public void goodSimpleStatic(){
 		ThisDummy d = new ThisDummy();
 		synchronized(ThisDummy.slock){
 			Checker.guardByStatic(d, ThisDummy.class.getName()+".slock");
 		}
 	}
 
 	@Test(expected=IllegalAccessError.class)
 	public void badSimpleStatic(){
 		ThisDummy d = new ThisDummy();
 		Checker.guardByStatic(d, ThisDummy.class.getName()+".slock");
 	}
 
 	@Test(expected=ClassNotFoundException.class)
 	public void badClass() throws Throwable{
 		try{
 			ThisDummy d = new ThisDummy();
 			Checker.guardByStatic(d, "i.dont.exist.Okay.fine");
 		}catch(RuntimeException r){
 			throw Util.rootCause(r);
 		}
 	}

	@Test(expected=IllegalAccessError.class)
	public void mutableObject() throws Throwable{
		final List<Object> m = new ArrayList<Object>();
		Checker.check(m);
		m.add(new Object());
		try{
			doInAnotherThread(new Runnable(){
				public void run(){
					Checker.check(m);
				}
			});
		}catch(RuntimeException e){
			throw Util.rootCause(e);
		}
	}
 }
