 package todo;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 import fr.eservice.todo.ParameterException;
 import fr.eservice.todo.Task;
 import fr.eservice.todo.TodoList;
 import fr.eservice.todo.TodoList.ORDER;
 
 public class TodoTest {
 
 	@Test
 	public void canCreateEmptyList() {
 		TodoList list = new TodoList();
 		Task[] tasks = list.getTasks(ORDER.ADDED_DATE);
 		assertNotNull(tasks);
 		assertEquals(0, tasks.length);
 	}
 	
 	private Task _createSampleTask() throws ParameterException {
 		return Task.create("123456", "any description", null);
 	}
 	
 	private Task _createTaskEndIn(long sec) throws ParameterException {
 		long time = (System.currentTimeMillis() / 1000L) + sec;
 		return Task.create("123456", "any description", time);
 	}
 	
 	@Test
 	public void addTask_ok() throws Exception {
 		TodoList list = new TodoList();
 		Task t = _createSampleTask();
 		list.addTask(t);
 	}
 	
 	@Test( expected=ParameterException.class )
 	public void addTask_cantAddNull() throws Exception {
 		TodoList list = new TodoList();
 		list.addTask(null);
 	}
 	
 	@Test
 	public void addTask_addTwice() throws Exception {
 		TodoList list = new TodoList();
 		Task t = _createSampleTask();
 		list.addTask(t);
 		list.addTask(t);
 		assertEquals(1, list.getTasks(ORDER.ADDED_DATE).length );
 	}
 	
 	@Test
 	public void getTask_correctNumber() throws Exception {
 		TodoList list = new TodoList();
 		for( int i = 0; i < 10; i++) {
 			list.addTask( _createSampleTask() );
 		}
 		Task[] tasks = list.getTasks(ORDER.ADDED_DATE);
 		assertEquals(10, tasks.length);
 	}
 	
 	@Test
 	public void getTask_correctOrdering() throws Exception {
 		Task[] tasks = new Task[] {
 				_createTaskEndIn( 1200 ),
 				_createTaskEndIn( 800 ),
 				_createTaskEndIn( 1500 )
 		};
 		TodoList list = new TodoList();
 		for( Task t : tasks ) list.addTask(t);
 		
 		Task[] result_byTarget = list.getTasks(ORDER.TARGET_DATE);
 		assertEquals(3, result_byTarget.length);
 		assertEquals(tasks[1], result_byTarget[0]);
 		assertEquals(tasks[0], result_byTarget[1]);
 		assertEquals(tasks[2], result_byTarget[2]);
 		
 		Task[] result_byAdded = list.getTasks(ORDER.ADDED_DATE);
 		assertEquals(3, result_byTarget.length);
 		assertEquals(tasks[0], result_byAdded[0]);
 		assertEquals(tasks[1], result_byAdded[1]);
 		assertEquals(tasks[2], result_byAdded[2]);
 	}
 	
 	@Test
 	public void removeTask_ok() throws Exception {
 		TodoList list = new TodoList();
 		Task t = _createSampleTask();
 		list.addTask(t);
 		list.removeTask(t.getReference());
 		assertEquals(0, list.getTasks(ORDER.ADDED_DATE).length );
 	}
 	
 	@Test
 	public void removeTask_ignoreInvalidRef() throws Exception {
 		TodoList list = new TodoList();
 		Task t = _createSampleTask();
 		list.addTask(t);
 		list.removeTask( 0xCAFE );
 		assertEquals(1, list.getTasks(ORDER.ADDED_DATE).length );
 	}
 	
 	@Test
 	public void completeTask_ok() throws Exception {
 		TodoList list = new TodoList();
 		Task t = _createSampleTask();
 		list.addTask(t);
 		list.completeTask(t.getReference());
 		assertTrue( t.hasBeenCompleted() );
 	}
 	
 	@Test
 	public void completeTask_ignoreInvalidRef() throws Exception {
 		TodoList list = new TodoList();
 		Task t = _createSampleTask();
 		list.addTask(t);
 		list.completeTask( 0xCAFE );
		assertTrue( !t.hasBeenCompleted() );
 	}
 	
 }
