 package org.swordess.toy.javamisc.ref;
 
 import static junit.framework.Assert.assertNotNull;
 import static junit.framework.Assert.assertNull;
 
 import java.lang.ref.PhantomReference;
 import java.lang.ref.ReferenceQueue;
 import java.lang.ref.SoftReference;
 import java.lang.ref.WeakReference;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.junit.Ignore;
 import org.junit.Test;
 
 /**
  * Source:
  * http://www.kdgregory.com/index.php?page=java.refobj
  * <p>
  * 
  * What are "roots"?
  * <p>
  * 
  * In a simple Java application, they're method parameters and local variables
  * stored on the stack, the operands of the currently executing expression(also
  * stored on the stack), and static class member variables.
  * <p>
  * 
  * <b>
  * NOTE:
  * Whether or not a strongly reference depends on whether can be referenced by a
  * certain reference chain start from a root reference, it has no relationship
  * with the reference type! 
  * </b>
  * <p>
  * 
  * It's important to understand root references, because they define what a
  * "strong" reference is: if you can follow a chain of references from a root to
  * a particular object, then that object is "strongly" referenced.
  */
 @Ignore("This is not a real test!")
 public class RefDemoOfMine {
 
 	@Test
 	public void gcWhenStronglyReachable() {
 		Stuff strongS = instantiateStuff("Strongly Reachable");
 		System.gc();
 		assertNotNull(strongS);
 	}
 	
 	@Test
 	public void gcWhenSoftlyReachable() {
 		/*
 		 * The garbage collector will attempt to preserve the object as long as
 		 * possible, but will collect it before throwing an OutOfMemoryError.
 		 */
 		SoftReference<Stuff> softS = new SoftReference<RefDemoOfMine.Stuff>(instantiateStuff("Softly Reachable"));
 		System.gc();
 		assertNotNull(softS.get());
 	}
 	
 	@Test
 	public void gcWhenWeaklyReachable() {
 		/*
 		 * The garbage collector is free to collect the object at any time,
 		 * with no attempt to preserve it. In practice, the object will be
 		 * collected during a major collection, but may survive a minor
 		 * collection.
 		 */
 		WeakReference<Stuff> weakS = new WeakReference<Stuff>(instantiateStuff("Weakly Reachable"));
 		System.gc();
 		assertNull(weakS.get());
 	}
 	
 	@Test
 	public void gcWhenPhantomReachable() throws InterruptedException {
 		/*
 		 * This reference type differs from the other two in that it isn't
 		 * meant to be used to access the object, but as a signal that the
 		 * object has already been finalized, and the garbage collector is
 		 * ready to reclaim its memory.
 		 */
 		final ReferenceQueue<Stuff> queue = new ReferenceQueue<Stuff>();
 		PhantomReference<Stuff> phantomS = new PhantomReference<Stuff>(instantiateStuff("Phantom Reachable"), queue);
 
 		/*
 		 * I found the object is not enqueued unless I remove the override
 		 * finalize() method from Stuff class.
 		 * 
 		 * But why???
 		 */
 		new Thread(new Runnable() {
 			public void run() {
 				try {
 					System.out.println("Awaiting for GC");
 					@SuppressWarnings("unchecked")
 					PhantomReference<Stuff> ref = (PhantomReference<Stuff>) queue.remove();
 					System.out.println(ref);
 					System.out.println("Stuff has been enqueued!");
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}).start();
 		
 		Thread.sleep(2000);
 
 		System.out.println("Invoking GC");
 		System.gc();
 	}
 	
 	@Test
 	public void gcWhenUnreachable() {
 		instantiateStuff("Unreachable");
 		System.gc();
 	}
 	
 	@Test
 	public void blogDemo1() {
 		/*
 		 * You must hold a strong reference to the reference object.
 		 * 
 		 * If you create a reference object, but allow it to go out of scope,
 		 * then the reference object itself will be garbage collected. Seems
 		 * obvious, but it's easy to forget, particularly when you're using
 		 * reference queues to track when the reference objects get cleared.
 		 */
 		SoftReference<List<Stuff>> ref =
 				new SoftReference<List<Stuff>>(new LinkedList<Stuff>());
 
 		for (int i = 0; i < 5; i++) {
			List<Stuff> referent = ref.get();
			if (null != referent) {
				referent.add(new Stuff("demo1-" + i));
			}
 		}
 		
 		/*
 		 * You must always check to see if the referent is null.
 		 * 
 		 * The garbage collector can clear the reference at any time, and if
 		 * you blithely use the reference, sooner or later you'll get
 		 * NullPointerException.
 		 */
 		List<Stuff> list = ref.get();
 		
 		/*
 		 * You must hold a strong reference to the referent to use it.
 		 * 
 		 * Again, the garbage collector can clear the reference at any time,
 		 * even between two statements in your code. If you simply call get()
 		 * once to check for null, and then call get() again to use the
 		 * reference, it might be cleared between those calls.
 		 * 
 		 * So, the right way is,
 		 *   first hold a strong reference to the returned value of the get(),
 		 *   then check the null-ability against that strong reference,
 		 *   if not null, then you can operate on that strong reference now.
 		 */
 		if (list == null) {
 			throw new RuntimeException("ran out of memory");
 		}
 		list.add(new Stuff("demo1-last"));
 	}
 	
 	public static List<List<Object>> processResults(ResultSet rslt) throws SQLException {
 		/*
 		 * Process query results in a generic way and ensure the ResultSet is
 		 * properly closed. It only has one small flaw: what happens if the
 		 * query returns a million rows?
 		 * 
 		 * The answer, of course, is an OutOfMemoryError, unless you have a
 		 * gigantic heap or tiny rows. It's the perfect place for a circuit
 		 * breaker: if the JVM runs out of memory while processing the query,
 		 * release all the memory that it's already used, and throws an
 		 * application-specific exception.
 		 * 
 		 * At this point, you may wonder: who cares? The query is going to
 		 * abort in either case, why not just let the out-of-memory error do
 		 * the job? The answer is that your application may not be the only
 		 * thing affected. If you're running on an application server, your
 		 * memory usage could take down other applications. Even in an unshared
 		 * environment, a circuit-breaker improves the robustness of your
 		 * application, because it confines the problem and gives you a chance
 		 * to recover and continue.
 		 */
 		try {
 			List<List<Object>> results = new LinkedList<List<Object>>();
 			ResultSetMetaData meta = rslt.getMetaData();
 			int colCount = meta.getColumnCount();
 			
 			while (rslt.next()) {
 				List<Object> row = new ArrayList<Object>(colCount);
 				for (int i = 1; i <= colCount; i++) {
 					row.add(rslt.getObject(i));
 				}
 				results.add(row);
 			}
 
 			return results;
 		} finally {
 			rslt.close();
 		}
 	}
 	
 	public static List<List<Object>> processResultsUsingSoftReference(ResultSet rslt)
 			throws SQLException, TooManyResultsException {
 		/*
 		 * To create the circuit-breaker, the first thing you need to do is
 		 * wrap the results list in a SoftReference.
 		 */
 		SoftReference<List<List<Object>>> ref =
 				new SoftReference<List<List<Object>>>(new LinkedList<List<Object>>());
 		ResultSetMetaData meta = rslt.getMetaData();
 		int colCount = meta.getColumnCount();
 		int rowCount = 0;
 		while (rslt.next()) {
 			rowCount++;
 			List<Object> row = new ArrayList<Object>();
 			for (int i = 1; i <= colCount; i++) {
 				row.add(rslt.getObject(i));
 			}
 
 			/*
 			 * And then, as you iterate through the results, create a strong
 			 * reference to the list only when you need to update it. 
 			 */
 			List<List<Object>> results = ref.get();
 			if (results == null) {
 				throw new TooManyResultsException(rowCount);
 			} else {
 				results.add(row);
 			}
 			
 			/*
 			 * Note that the results variable is set to null after adding the
 			 * new element -- this is one of the few cases where doing so is
 			 * justified. Although the variables goes out of scope at the end
 			 * of loop, the garbage collector does not know that (because
 			 * there's no reason for the JVM to clear the variable's slot in
 			 * the call stack). So, if not clear the variable, it would be an
 			 * unintended strong reference during the subsequent pass through
 			 * the loop. 
 			 */
 			results = null;
 		}
 		
 		return ref.get();
 	}
 	
 	private static class Stuff {
 
 		final String id;
 		
 		Stuff(String id) {
 			this.id = id;
 		}
 		
 		protected void finalize() throws Throwable {
 			System.out.println("finalize " + id);
 		}
 		
 	}
 	
 	@SuppressWarnings("serial")
 	private static class TooManyResultsException extends Exception {
 
 		TooManyResultsException(int count) {
 			super(String.valueOf(count));
 		}
 
 	}
 	
 	private Stuff instantiateStuff(String id) {
 		return new Stuff(id);
 	}
 	
 }
