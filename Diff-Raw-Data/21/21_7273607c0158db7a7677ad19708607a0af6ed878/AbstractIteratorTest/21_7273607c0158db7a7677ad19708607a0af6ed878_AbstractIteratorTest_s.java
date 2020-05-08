 package falgout.utils;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertSame;
 
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class AbstractIteratorTest {
 	private static final List<String> elements = Arrays.asList("1", "2", "3");
 	
 	private Iterator<String> itr;
 	
 	@Before
 	public void init() {
 		itr = new AbstractIterator<String>() {
 			private int i = 0;
 			
 			@Override
 			protected String findNext() {
 				return i < elements.size() ? elements.get(i++) : null;
 			}
 		};
 	}
 	
 	@Test
 	public void WorksInSimpleIteration() {
 		int i = 0;
 		while (itr.hasNext()) {
 			assertSame(elements.get(i++), itr.next());
 		}
 	}
 	
 	@Test
 	public void WorksInManualIterator() {
 		assertSame(elements.get(0), itr.next());
 		assertSame(elements.get(1), itr.next());
 		assertSame(elements.get(2), itr.next());
 		assertFalse(itr.hasNext());
 	}
 }
