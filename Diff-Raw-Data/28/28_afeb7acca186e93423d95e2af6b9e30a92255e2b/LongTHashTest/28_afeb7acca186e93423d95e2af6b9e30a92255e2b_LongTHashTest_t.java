 
 package org.cytoscape.model.internal;
 
 import java.lang.Thread.State;
 
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 public class LongTHashTest {
 
 	@Test
 	public void testBasicPutGet() {
 		LongTHash<String> ith = new LongTHash<String>(String.class);
 		ith.put(1L,"homer");
 		ith.put(2L,"marge");
 		ith.put(3L,"lisa");
 		assertEquals( "homer", ith.get(1L) );
 		assertEquals( "marge", ith.get(2L) );
 		assertEquals( "lisa", ith.get(3L) );
 	}
 
 	@Test
 	public void testSize() {
 		LongTHash<String> ith = new LongTHash<String>(String.class);
 		ith.put(1,"homer");
 		ith.put(2,"marge");
 		ith.put(3,"lisa");
 		assertEquals( 3, ith.size() );
 	}
 
 	@Test
 	public void testRemove() {
 		LongTHash<String> ith = new LongTHash<String>(String.class);
 		ith.put(1L,"homer");
 		ith.put(2L,"marge");
 		ith.put(3L,"lisa");
 
 		assertEquals( 3, ith.size() );
 
 		ith.remove(3L);
 		assertEquals( 2, ith.size() );
 		assertNull( ith.get(3L) );
 
 		ith.remove(2L);
 		assertEquals( 1, ith.size() );
 		assertNull( ith.get(2L) );
 
 		ith.remove(1L);
 		assertEquals( 0, ith.size() );
 		assertNull( ith.get(1L) );
 	}
 
 	@Test
 	public void testAddRemove() {
 		LongTHash<String> ith = new LongTHash<String>(String.class);
 		ith.put(1L,"homer");
 		ith.put(2L,"marge");
 		ith.put(3L,"lisa");
 		ith.put(4L,"bart");
 		ith.put(5L,"maggie");
 		ith.put(6L,"smithers");
 		ith.put(7L,"burns");
 
 		ith.remove(1L);
 		ith.remove(2L);
 		ith.remove(3L);
 
 		ith.put(1L,"homer");
 		ith.put(2L,"marge");
 		ith.put(3L,"lisa");
 
 		ith.remove(4L);
 		ith.remove(5L);
 		ith.remove(6L);
 
 		ith.put(4L,"bart");
 		ith.put(5L,"maggie");
 		ith.put(6L,"smithers");
 	}
 	
 	@Test
 	public void testTicket849() {
 		LongTHash<String> hash = new LongTHash<String>(String.class);
 		hash.put(3476L, "A");	
 		hash.put(3477L, "B");	
 		hash.put(3478L, "C");	
 		hash.put(3479L, "D");	
 		hash.put(3490L, "E");
 		hash.put(3491L, "F");
 		
 		hash.remove(3490L);
 		assertEquals("F", hash.get(3491L));
 	}
 
 	@Test
 	public void testForceResize() {
 		LongTHash<String> hash = new LongTHash<String>(String.class);
 		hash.put(0L, "A");	
 		hash.put(1L, "A");	
 		hash.put(2L, "A");	
 		hash.put(3L, "A");	
 		hash.put(4L, "A");	
 		hash.put(5L, "A");	
 		hash.put(6L, "A");	
 		hash.put(7L, "A");	
 		hash.put(8L, "A");	
 		hash.put(9L, "A");	
 		hash.put(10L, "A");	
 		hash.put(11L, "A");	
 	}
 	
	@Test
 	public void testTicket853() throws InterruptedException {
 		Thread thread = new Thread(new Runnable() {
 			@Override
 			public void run() {
 				int size = 6;
 				LongTHash<Integer> hash = new LongTHash<Integer>(Integer.class);
 				for (int i = 0; i < size; i++) {
 					hash.put((long) i, i);
 				}
 				for (int i = size; i < (size * 2); i++) {
 					hash.remove((long) i);
 				}
 			}
 		});
 		thread.start();
 		thread.join(2000);
 		assertEquals(State.TERMINATED, thread.getState());
 	}
 }
