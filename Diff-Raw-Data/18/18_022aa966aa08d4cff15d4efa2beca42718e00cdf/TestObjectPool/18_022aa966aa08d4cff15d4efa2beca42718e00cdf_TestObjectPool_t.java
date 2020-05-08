 package com.barchart.http.server;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 
 import java.util.ArrayList;
 import java.util.concurrent.Callable;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class TestObjectPool {
 
 	private final AtomicInteger ids = new AtomicInteger(0);
 
 	private ObjectPool<Poolable> objectPool;
 	private ArrayList<Poolable> instances;
 
 	@Before
 	public void setUp() {
 		objectPool = new ObjectPool<Poolable>(5, new Callable<Poolable>() {
 			@Override
 			public Poolable call() throws Exception {
 				return new Poolable();
 			}
 		});
 		instances = new ArrayList<Poolable>();
 	}
 
 	@After
 	public void tearDown() {
 		objectPool = null;
 	}
 
 	@Test
 	public void testCreate() throws Exception {
 		objectPool.poll();
 		assertEquals(1, instances.size());
 	}
 
 	@Test
 	public void testCapacity() throws Exception {
 		final Poolable p1 = objectPool.poll();
 		assertNotNull(p1);
 		final Poolable p2 = objectPool.poll();
 		assertNotNull(p2);
 		final Poolable p3 = objectPool.poll();
 		assertNotNull(p3);
 		final Poolable p4 = objectPool.poll();
 		assertNotNull(p4);
 		final Poolable p5 = objectPool.poll();
 		assertNotNull(p5);
 
 		assertEquals(5, instances.size());
 
 		assertNull(objectPool.poll());
 
 	}
 
 	@Test
 	public void testReturn() throws Exception {
 		final Poolable p1 = objectPool.poll();
 		assertNotNull(p1);
 		final Poolable p2 = objectPool.poll();
 		assertNotNull(p2);
 		final Poolable p3 = objectPool.poll();
 		assertNotNull(p3);
 		final Poolable p4 = objectPool.poll();
 		assertNotNull(p4);
 		final Poolable p5 = objectPool.poll();
 		assertNotNull(p5);
 
 		assertNull(objectPool.poll());
 
 		new Thread() {
 			@Override
 			public void run() {
 				try {
 					Thread.sleep(100);
 				} catch (final InterruptedException e) {
 				}
 				objectPool.give(p5);
 			}
 		}.start();
 
 		final Poolable p = objectPool.poll(500, TimeUnit.MILLISECONDS);
 		assertEquals(p5, p);
 
 	}
 
 	@Test
 	public void testWaitUnavailable() throws Exception {
 
 		final Poolable p1 = objectPool.poll();
 		assertNotNull(p1);
 		final Poolable p2 = objectPool.poll();
 		assertNotNull(p2);
 		final Poolable p3 = objectPool.poll();
 		assertNotNull(p3);
 		final Poolable p4 = objectPool.poll();
 		assertNotNull(p4);
 		final Poolable p5 = objectPool.poll();
 		assertNotNull(p5);
 
 		assertNull(objectPool.poll());
 
 		final Poolable p = objectPool.poll(100, TimeUnit.MILLISECONDS);
 		assertNull(p);
 
 	}
 
 	@Test
 	public void testUnbounded() throws Exception {
 
		objectPool = new ObjectPool<Poolable>(-1, new Callable<Poolable>() {
 			@Override
 			public Poolable call() throws Exception {
 				return new Poolable();
 			}
 		});
 
 		for (int i = 0; i < 100; i++) {
 			objectPool.poll();
 		}
 
 		assertEquals(100, instances.size());
 
 	}
 
 	public class Poolable {
 
 		public int id;
 
 		public Poolable() {
 			id = ids.incrementAndGet();
 			instances.add(this);
 		}
 
 	}
 
 }
