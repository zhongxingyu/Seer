 package com.barchart.store.heap;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Callable;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import rx.Observer;
 
 import com.barchart.store.api.Batch;
 import com.barchart.store.api.ColumnDef;
 import com.barchart.store.api.ObservableIndexQueryBuilder.Operator;
 import com.barchart.store.api.RowMutator;
 import com.barchart.store.api.StoreRow;
 import com.barchart.store.api.StoreService.Table;
 import com.barchart.util.test.concurrent.CallableTest;
 
 public class TestHeapStore {
 
 	private static final String KEYSPACE = "cs_unit_test";
 	private static final Table<String, String> TABLE = Table
 			.make("cs_test_table");
 
 	private TestObserver observer;
 	private HeapStore store;
 
 	@Test
 	public void testKeyspace() throws Exception {
 		assertTrue(store.has(KEYSPACE));
 		store.delete(KEYSPACE);
 		Thread.sleep(100);
 		assertFalse(store.has(KEYSPACE));
 	}
 
 	@Test
 	public void testTable() throws Exception {
 		assertFalse(store.has(KEYSPACE, TABLE));
 		store.create(KEYSPACE, TABLE);
 		Thread.sleep(100);
 		assertTrue(store.has(KEYSPACE, TABLE));
 		store.delete(KEYSPACE, TABLE);
 		Thread.sleep(100);
 		assertFalse(store.has(KEYSPACE, TABLE));
 	}
 
 	@Test
 	public void testQueryMissing() throws Exception {
 
 		store.create(KEYSPACE, TABLE);
 		Thread.sleep(100);
 		observer.reset();
 
 		store.fetch(KEYSPACE, TABLE, "test-1").build().subscribe(observer);
 
 		CallableTest.waitFor(new Callable<Boolean>() {
 			@Override
 			public Boolean call() throws Exception {
 				return observer.completed;
 			}
 		});
 
		assertEquals(1, observer.rows.size());
		assertEquals(0, observer.rows.get(0).columns().size());
 
 	}
 
 	@Test
 	public void testInsert() throws Exception {
 
 		store.create(KEYSPACE, TABLE);
 		Thread.sleep(100);
 
 		Batch batch = store.batch(KEYSPACE);
 		batch.row(TABLE, "test-1").set("column_key", "column_value");
 		batch.commit();
 
 		Thread.sleep(100);
 
 		store.fetch(KEYSPACE, TABLE, "test-1").build().subscribe(observer);
 
 		CallableTest.waitFor(new Callable<Boolean>() {
 			@Override
 			public Boolean call() throws Exception {
 				return observer.completed;
 			}
 		});
 
 		assertEquals(null, observer.error);
 		assertEquals(1, observer.rows.size());
 		assertEquals(observer.rows.get(0).get("column_key").getString(),
 				"column_value");
 
 		batch = store.batch(KEYSPACE);
 		batch.row(TABLE, "test-2").set("column_key", "column_value2");
 		batch.row(TABLE, "test-3").set("column_key", "column_value2");
 		batch.commit();
 
 		Thread.sleep(100);
 
 		store.fetch(KEYSPACE, TABLE, "test-2").build().subscribe(observer);
 		store.fetch(KEYSPACE, TABLE, "test-3").build().subscribe(observer);
 
 		CallableTest.waitFor(new Callable<Boolean>() {
 			@Override
 			public Boolean call() throws Exception {
 				return observer.rows.size() == 3;
 			}
 		});
 
 		assertEquals(null, observer.error);
 		assertEquals(3, observer.rows.size());
 		assertEquals(observer.rows.get(1).get("column_key").getString(),
 				"column_value2");
 
 	}
 
 	@Test
 	public void testColumnLimit() throws Exception {
 
 		store.create(KEYSPACE, TABLE);
 		Thread.sleep(100);
 
 		final Batch batch = store.batch(KEYSPACE);
 		final RowMutator<String> rm = batch.row(TABLE, "test-1");
 		for (int i = 1; i < 100; i++) {
 			rm.set("field" + i, "value" + i);
 		}
 		batch.commit();
 
 		Thread.sleep(100);
 
 		store.fetch(KEYSPACE, TABLE, "test-1").first(1).build()
 				.subscribe(observer);
 
 		CallableTest.waitFor(new Callable<Boolean>() {
 			@Override
 			public Boolean call() throws Exception {
 				return observer.completed;
 			}
 		});
 
 		StoreRow<String> row = observer.rows.get(0);
 		assertEquals(1, row.columns().size());
 		assertEquals("field1", row.columns().iterator().next());
 		assertEquals("value1", row.get("field1").getString());
 
 		observer.reset();
 		store.fetch(KEYSPACE, TABLE, "test-1").last(1).build()
 				.subscribe(observer);
 
 		CallableTest.waitFor(new Callable<Boolean>() {
 			@Override
 			public Boolean call() throws Exception {
 				return observer.completed;
 			}
 		});
 
 		row = observer.rows.get(0);
 		assertEquals(1, row.columns().size());
 		assertEquals("field99", row.columns().iterator().next());
 		assertEquals("value99", row.get("field99").getString());
 
 	}
 
 	@Test
 	public void testColumnSlice() throws Exception {
 
 		store.create(KEYSPACE, TABLE);
 		Thread.sleep(100);
 
 		final Batch batch = store.batch(KEYSPACE);
 		final RowMutator<String> rm = batch.row(TABLE, "test-1");
 		for (int i = 1; i < 100; i++) {
 			rm.set("field" + i, "value" + i);
 		}
 		batch.commit();
 
 		Thread.sleep(100);
 
 		store.fetch(KEYSPACE, TABLE, "test-1").start("field10").end("field15")
 				.build().subscribe(observer);
 
 		CallableTest.waitFor(new Callable<Boolean>() {
 			@Override
 			public Boolean call() throws Exception {
 				return observer.completed;
 			}
 		});
 
 		StoreRow<String> row = observer.rows.get(0);
 		assertEquals(6, row.columns().size());
 
 		// Test indexed sort order
 		assertEquals("field10", row.getByIndex(0).getName());
 		assertEquals("field11", row.getByIndex(1).getName());
 		assertEquals("field12", row.getByIndex(2).getName());
 		assertEquals("field13", row.getByIndex(3).getName());
 		assertEquals("field14", row.getByIndex(4).getName());
 		assertEquals("field15", row.getByIndex(5).getName());
 
 		observer.reset();
 		store.fetch(KEYSPACE, TABLE, "test-1")
 				.columns("field10", "field15", "field20", "field1000").build()
 				.subscribe(observer);
 
 		CallableTest.waitFor(new Callable<Boolean>() {
 			@Override
 			public Boolean call() throws Exception {
 				return observer.completed;
 			}
 		});
 
 		row = observer.rows.get(0);
 		// field1000 doesn't exist, so 3 instead of 4
 		assertEquals(3, row.columns().size());
 
 		// Test indexed sort order
 		assertEquals("field10", row.getByIndex(0).getName());
 		assertEquals("field15", row.getByIndex(1).getName());
 		assertEquals("field20", row.getByIndex(2).getName());
 
 	}
 
 	@Test
 	public void testQueryAll() throws Exception {
 
 		store.create(KEYSPACE, TABLE);
 		Thread.sleep(100);
 
 		final Batch batch = store.batch(KEYSPACE);
 		batch.row(TABLE, "test-1").set("column_key", "column_value1");
 		batch.row(TABLE, "test-2").set("column_key", "column_value2");
 		batch.row(TABLE, "test-3").set("column_key", "column_value3");
 		batch.row(TABLE, "test-4").set("column_key", "column_value4");
 		batch.commit();
 
 		Thread.sleep(100);
 
 		store.fetch(KEYSPACE, TABLE).build().subscribe(observer);
 
 		CallableTest.waitFor(new Callable<Boolean>() {
 			@Override
 			public Boolean call() throws Exception {
 				return observer.completed;
 			}
 		}, 5000);
 
 		assertEquals(null, observer.error);
 		assertEquals(4, observer.rows.size());
 
 	}
 
 	@Test
 	public void testIndexQuery() throws Exception {
 
 		store.create(KEYSPACE, TABLE, new ColumnDef() {
 
 			@Override
 			public String key() {
 				return "field";
 			}
 
 			@Override
 			public boolean isIndexed() {
 				return true;
 			}
 
 			@Override
 			public Class<?> type() {
 				return String.class;
 			}
 
 		}, new ColumnDef() {
 
 			@Override
 			public String key() {
 				return "num";
 			}
 
 			@Override
 			public boolean isIndexed() {
 				return true;
 			}
 
 			@Override
 			public Class<?> type() {
 				return Integer.class;
 			}
 
 		});
 
 		Thread.sleep(100);
 
 		final Batch batch = store.batch(KEYSPACE);
 		for (int i = 1; i < 1000; i++) {
 			batch.row(TABLE, "test-" + i).set("field", "value-" + (i % 100))
 					.set("num", i);
 		}
 		batch.commit();
 
 		Thread.sleep(1000);
 
 		store.query(KEYSPACE, TABLE).where("field", "value-50").build()
 				.subscribe(observer);
 
 		CallableTest.waitFor(new Callable<Boolean>() {
 			@Override
 			public Boolean call() throws Exception {
 				return observer.completed;
 			}
 		}, 5000);
 
 		assertEquals(null, observer.error);
 		assertEquals(10, observer.rows.size());
 
 		observer.reset();
 
 		store.query(KEYSPACE, TABLE).where("field", "value-50")
 				.where("num", 50).build().subscribe(observer);
 
 		CallableTest.waitFor(new Callable<Boolean>() {
 			@Override
 			public Boolean call() throws Exception {
 				return observer.completed;
 			}
 		}, 5000);
 
 		assertEquals(null, observer.error);
 		assertEquals(1, observer.rows.size());
 
 		observer.reset();
 
 		store.query(KEYSPACE, TABLE).where("field", "value-50")
 				.where("num", 500, Operator.LT).build().subscribe(observer);
 
 		CallableTest.waitFor(new Callable<Boolean>() {
 			@Override
 			public Boolean call() throws Exception {
 				return observer.completed;
 			}
 		}, 5000);
 
 		assertEquals(null, observer.error);
 		assertEquals(5, observer.rows.size());
 
 	}
 
 	@Before
 	public void setUp() throws Exception {
 		observer = new TestObserver();
 		store = new HeapStore();
 		try {
 			store.delete(KEYSPACE);
 		} catch (final Exception e) {
 			System.out.println("keyspace missing");
 		}
 		store.create(KEYSPACE);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		try {
 			store.delete(KEYSPACE);
 		} catch (final Exception e) {
 			System.out.println("keyspace missing");
 		}
 		store = null;
 	}
 
 	private class TestObserver implements Observer<StoreRow<String>> {
 
 		boolean completed = false;
 		Throwable error = null;
 		final List<StoreRow<String>> rows = new ArrayList<StoreRow<String>>();
 
 		@Override
 		public void onCompleted() {
 			completed = true;
 		}
 
 		@Override
 		public void onError(final Throwable e) {
 			error = e;
 			completed = true;
 		}
 
 		@Override
 		public void onNext(final StoreRow<String> row) {
 			rows.add(row);
 		}
 
 		public void reset() {
 			completed = false;
 			error = null;
 			rows.clear();
 		}
 
 	}
 
 }
