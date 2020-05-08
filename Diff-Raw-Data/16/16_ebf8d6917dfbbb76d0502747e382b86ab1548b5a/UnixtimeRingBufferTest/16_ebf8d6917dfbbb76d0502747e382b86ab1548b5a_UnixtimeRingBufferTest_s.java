 package org.veiset.coffew8.coffeepi;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class UnixtimeRingBufferTest {
 
 	final Integer DATA = 50;
 	// Should be >= 2, as you cannot test correct pointer movement with only one
 	// element.
 	final int SIZE = 5;
 	private UnixtimeRingBuffer utrb;
 
 	@Before
 	public void setup() {
 		utrb = new UnixtimeRingBuffer(SIZE);
 	}
 
 	@Test
 	public void newStackIsGivenSize() {
 		assertEquals(utrb.size(), SIZE);
 	}
 
 	@Test
 	public void pointerIsNullAtNewStack() {
 		assertEquals(utrb.getPosition(), 0);
 	}
 
 	@Test
 	public void pointerIsIncreasedByOneWhenNotAtEndOfStack() {
 		int pointer = utrb.getPosition();
 		utrb.add(DATA);
 		assertEquals(utrb.getPosition(), pointer + 1);
 	}
 
 	@Test
 	public void pointerCyclesTheStack() {
 		for (int i = 0; i < SIZE; i++) {
 			utrb.add(DATA);
 		}
 		assertEquals(utrb.getPosition(), 0);
 	}
 
 	@Test
 	public void addedElementIsAdded() {
 		utrb.add(DATA);
 		assertSame(utrb.current().getWeight(), DATA);
 	}
 
 	@Test
 	public void getRangeNtoNIsNull() {
 		assertNull(utrb.getLast(0));
 	}
 
 	@Test
 	public void lastElementsLargerThanSizeShouldReturnAll() {
 		assertEquals(utrb.getLast(SIZE + 1).length, SIZE);
 	}
 
 	@Test
 	public void getLastNShouldReturnNElements() {
 		assertEquals(utrb.getLast(3).length, 3);
 	}
 
 //	@Test
 //	public void getLastShouldContainElementAtCurrentPos() {
 //		utrb.add(new CoffeeState(5, 50));
 //		assertEquals(utrb.getLast(1)[0], utrb.get(utrb.getPosition()));
 //	}
 
 //	@Test
 //	public void getIdNewerThanUnixBeforeDataShouldReturnMinusOne() {
 //		utrb.add(new CoffeeState(5, 50));
 //		utrb.add(new CoffeeState(11, 50));
 //		utrb.add(new CoffeeState(14, 50));
 //		utrb.add(new CoffeeState(17, 50));
 //		utrb.add(new CoffeeState(19, 50));
 //		utrb.add(new CoffeeState(23, 50));
 //		utrb.add(new CoffeeState(42, 50));
 //		utrb.add(new CoffeeState(55, 50));
 //		assertEquals(utrb.idNewerThanUnix(0), -1); // -1, everything is newer
 //	}
 //
 //	@Test
 //	public void getIdNewerThanUnixWithNoNewDataShouldReturnCurrentPos() {
 //		utrb.add(new CoffeeState(5, 50));
 //		utrb.add(new CoffeeState(11, 50));
 //		assertEquals(utrb.idNewerThanUnix(15), utrb.getPosition());
 //	}
 
 //	@Test
 //	public void getDataSinceBeforeTimeShouldReturnAll() {
 //		assertEquals(utrb.getElementsAfter(new CoffeeState(0)).length, utrb.size());
 //	}
 
 	@Test
	public void getDataAfterAddedShouldReturnAdded() {
 		utrb.add(5);
 		CoffeeState cs = utrb.current();
 		utrb.add(DATA);
 		
 		CoffeeState[] states = utrb.getElementsAfter(cs);
		assertEquals(states.length, 2);
 	}
 
 }
