 package QueueTests;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import Queue.InvalidException;
 import Queue.Queue;
 import Queue.QueueEmptyException;
 import Queue.QueueFullException;
 
 public class QueueTest{
 	private static final int _ANY_NUMBER_1 = 1;
 	private static final int _ANY_NUMBER_2 = 7;
 	private static final int _ANY_NUMBER_3 = 4;
 	private Queue queue;
 	
 	@Before
 	public void setUp(){
 		queue = new Queue();
 	}
 	
 	@Test
 	public void whenQueueIsCreatedItsSizeIsZero() throws Exception {
 		assertEquals(0, queue.getSize());
 	}
 	
 	@Test
 	public void whenQueueIsCreatedItsEmptyFlagIsTrue(){
 		assertEquals(Boolean.TRUE, queue.isEmpty());
 	}
 
 	@Test
 	public void whenAnIntIsAddedSizeIsOne() throws QueueFullException, InvalidException {
 		queue.add(_ANY_NUMBER_1);
 		assertEquals(1, queue.getSize());
 	}
 	
 	@Test(expected = InvalidException.class)
 	public void whenLowerLimitMinus1IsAddedThenInvalidIntExceptionIsThrown() throws QueueFullException, InvalidException {
 		queue.add(_ANY_NUMBER_1-1);
 	}
 	
 	@Test
 	public void whenAnIntIsAddedEmptyFlagIsFalse() throws QueueFullException, InvalidException{
 		queue.add(_ANY_NUMBER_1);
 		assertEquals(Boolean.FALSE, queue.isEmpty());
 	}
 		
 	
 	@Test
 	public void whenFiveIntsAreAddedNoExceptionIsThrown() throws QueueFullException, InvalidException {
 		queue.add(_ANY_NUMBER_1);
 		queue.add(_ANY_NUMBER_1);
 		queue.add(_ANY_NUMBER_1);
 		queue.add(_ANY_NUMBER_1);
 		queue.add(_ANY_NUMBER_1);
 		assertEquals(5, queue.getSize());
 	}
 	
 	@Test (expected = QueueFullException.class)
 	public void whenSixIntsAreAddedAQueueFullExceptionIsThrown()throws QueueFullException, InvalidException {
 		queue.add(_ANY_NUMBER_1);
 		queue.add(_ANY_NUMBER_1);
 		queue.add(_ANY_NUMBER_1);
 		queue.add(_ANY_NUMBER_1);
 		queue.add(_ANY_NUMBER_1);
 		queue.add(_ANY_NUMBER_1);
	
 	}
 	
 	@Test(expected = QueueEmptyException.class)
 	public void whenRemoveIsCalledAndTheListIsEmptyAreAddedQueueEmptyExceptionIsThrown() throws QueueEmptyException, InvalidException{
 		queue.remove();
 	
 	}
 	
 	@Test
 	public void whenAnIntIsAddedAndPeekValueOfFirstIntIsReturned()throws QueueFullException, QueueEmptyException, InvalidException {
 		queue.add(_ANY_NUMBER_1);
 		assertEquals(_ANY_NUMBER_1,queue.peek());
 	}
 	
 	@Test
 	public void whenTwoIntsAreAddedAndPeekValueOfFirstIntIsReturned()throws QueueFullException, QueueEmptyException, InvalidException{
 		queue.add(_ANY_NUMBER_2);
 		queue.add(_ANY_NUMBER_1);
 		assertEquals(_ANY_NUMBER_2,queue.peek());
 	}
 		
 	@Test
 	public void whenAnIntIsRemovedAndPeekValueOfSecondIntIsReturned()throws QueueFullException, QueueEmptyException, InvalidException {
 		queue.add(_ANY_NUMBER_3);
 		queue.add(_ANY_NUMBER_2);
 		queue.add(_ANY_NUMBER_1);
 		queue.remove();
 		assertEquals(_ANY_NUMBER_2,queue.peek());
 	}
 	
 	@Test
 	public void whenAnIntIsRemovedAndSizeIsReducedByOne()throws QueueFullException, QueueEmptyException, InvalidException {
 		queue.add(_ANY_NUMBER_3);
 		queue.add(_ANY_NUMBER_2);
 		queue.add(_ANY_NUMBER_1);
 		queue.remove();
 		assertEquals(2,queue.getSize());
 	}
 	
 	@Test(expected = QueueEmptyException.class)
 	public void whenPeekIsCalledAndTheListIsEmptyAreAddedQueueEmptyExceptionIsThrown() throws QueueEmptyException, InvalidException{
 		queue.peek();
 	
 	}
 }
