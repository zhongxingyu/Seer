 package eu.mapperproject.jmml.topology.algorithms;
 import cern.colt.list.IntArrayList;
import eu.mapperproject.jmml.specification.graph.Numbered;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.assertSame;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class TraceTest {
 	static class Int implements Numbered {
 		private final int num;
 		Int(int num) {
 			this.num = num;
 		}
 		@Override
 		public int getNumber() {
 			return this.num;
 		}
 		@Override
 		public boolean equals(Object o) {
 			if (o == null || getClass() != o.getClass()) return false;
 			return this.num == ((Int)o).num;
 		}
 
 		@Override
 		public int hashCode() {
 			return this.num;
 		}
 
 		@Override
 		public String toString() {
 			return "Int(" + this.num + ")";
 		}
 
 		@Override
 		public String getId() {
 			return Integer.toString(this.num);
 		}
 
 		@Override
 		public boolean deepEquals(Object o) {
 			return this.equals(o);
 		}
 
 		@Override
 		public void setNumber(int num) {
 			throw new UnsupportedOperationException("Not supported yet.");
 		}
 	}
 
 	private Trace traceEmpty, traceEmpty1, traceEmpty2;
 
 	@Before
 	public void setUp() throws Exception {
 		traceEmpty = new Trace();
 		traceEmpty1 = new Trace();
 		traceEmpty2 = new Trace();
 	}
 	
 	@Test
 	public void mergeBasic() {
 		traceEmpty1.merge(traceEmpty2);
 		assertEquals(traceEmpty, traceEmpty1);
 		assertEquals(traceEmpty, traceEmpty2);
 	}
 
 	@Test
 	public void nextAndCurrent() {
 		assertEquals(0, traceEmpty1.nextInt(1));
 		traceEmpty1.put(1, 1);
 		assertEquals(1, traceEmpty1.currentInt(1));
 		assertEquals(0, traceEmpty1.nextInt(2));
 		assertEquals(2, traceEmpty1.nextInt(1));
 		assertEquals(2, traceEmpty1.currentInt(1));
 		assertEquals(0, traceEmpty1.currentInt(2));
 		assertEquals(1, traceEmpty1.nextInt(2));
 		assertEquals(3, traceEmpty1.nextInt(1));
 	}
 	
 	@Test(expected= IllegalStateException.class)
 	public void empty() {
 		assertFalse(traceEmpty.isInstantiated(1));
 		traceEmpty.currentInt(1);
 	}
 
 	@Test
 	public void overrideput() {
 		traceEmpty.put(1, 3);
 		traceEmpty.put(1, 2);
 		traceEmpty1.put(1, 2);
 		assertEquals(traceEmpty1, traceEmpty);
 	}
 
 	@Test
 	public void compareputAndNext() {
 		this.nextAndCurrent();
 		traceEmpty.put(2, 1);
 		traceEmpty.put(1, 3);
 		assertEquals(traceEmpty, traceEmpty1);
 	}
 
 
 	@Test
 	public void mergeWithput() {
 		traceEmpty1.put(1, 1);
 		traceEmpty2.put(2, 2);
 		IntArrayList[] col = traceEmpty1.merge(traceEmpty2);
 		assertTrue(col[0].isEmpty());
 		assertSame(1, col[1].size());
 		assertEquals(2, col[1].get(0));
 
 		traceEmpty.put(1, 1);
 		traceEmpty.put(2, 2);
 		
 		assertEquals(traceEmpty, traceEmpty1);
 
 		traceEmpty2.put(2, 3);
 		col = traceEmpty1.merge(traceEmpty2);
 		assertSame(1, col[1].size());
 		traceEmpty.put(2, 3);
 
 		assertEquals(traceEmpty, traceEmpty1);
 
 		col = traceEmpty1.merge(traceEmpty2);
 		assertSame(1, col[0].size());
 		assertEquals(2, col[0].get(0));
 	}
 
 	@Test
 	public void override() {
 		traceEmpty1.put(1, 1);
 		traceEmpty2.put(2, 2);
 		traceEmpty1.merge(traceEmpty2);
 		
 		traceEmpty.put(1, 1);
 		traceEmpty.put(2, 2);
 		
 		assertEquals(traceEmpty, traceEmpty1);
 	}
 
 	@Test
 	public void reset() {
 		traceEmpty.put(1, 2);
 		traceEmpty.reset(1);
 		assertEquals(0, traceEmpty.nextInt(1));
 	}
 	
 	@Test
 	public void independentConstructor() {
 		Trace t = new Trace(traceEmpty);
 		t.put(1, 1);
 		assertFalse(t.equals(traceEmpty));
 	}
 }
