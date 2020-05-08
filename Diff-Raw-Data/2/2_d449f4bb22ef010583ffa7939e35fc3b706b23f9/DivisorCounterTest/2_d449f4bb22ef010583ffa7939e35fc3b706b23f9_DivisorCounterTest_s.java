 package hu.esgott.euler.problem21;
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.when;
 
 import java.util.Map;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 
 public class DivisorCounterTest {
 
 	@Mock
 	private DivisorsFactory mockDivisorFactory;
 	@Mock
 	private Map<Integer, Integer> mockStore;
 	@Mock
 	private Divisors mockDivisors;
 
 	@Before
 	public void init() {
 		MockitoAnnotations.initMocks(this);
 	}
 
 	@Test
 	public void testIntervals() {
 		DivisorCounter divisorCounter = new DivisorCounter(mockDivisorFactory);
 
 		when(mockDivisorFactory.create(1, 2000, mockStore)).thenReturn(
 				mockDivisors);
 		when(mockDivisorFactory.create(2001, 3000, mockStore)).thenReturn(
 				mockDivisors);
 		when(mockDivisorFactory.create(4001, 5000, mockStore)).thenReturn(
 				mockDivisors);
 		when(mockDivisorFactory.create(6001, 7000, mockStore)).thenReturn(
 				mockDivisors);
 		when(mockDivisorFactory.create(8001, 10000, mockStore)).thenReturn(
 				mockDivisors);
 
 		try {
 			divisorCounter.calculate();
 		} catch (Exception e) {
 			fail("Something bad happened");
 		}
 	}
 	
 	@Test
 	public void storeHasElements() {
		DivisorCounter divisorCounter = new DivisorCounter(mockDivisorFactory);
 		
 		try {
 			divisorCounter.calculate();
 		} catch (Exception e) {
 			fail("Something bad happened");
 		}
 		
 		Map<Integer, Integer> store = divisorCounter.getStore();
 		
 		assertEquals(10000, store.size());
 	}
 
 }
