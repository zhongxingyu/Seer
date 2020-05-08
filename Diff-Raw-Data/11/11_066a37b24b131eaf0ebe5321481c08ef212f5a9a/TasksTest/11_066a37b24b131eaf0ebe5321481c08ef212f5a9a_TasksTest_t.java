 package tasks;
 
 import junit.framework.TestCase;
 
 import org.apache.commons.lang.time.DateUtils;
import org.junit.Ignore;
 
 public abstract class TasksTest extends TestCase {
 
 		
 	private PatchacaTasksOperator tasksOperator;
 
 	public abstract PatchacaTasksOperator createOperator();
 
 	@Override
 	protected void setUp() throws Exception {
 		tasksOperator = createOperator();
 	}
 	
 	public void testCreateTaskInDefaultLabel(){
 		final String taskName = "test";
 		tasksOperator.createTask(taskName);
 		assertTrue(tasksOperator.isTaskInLabel(taskName, tasksOperator.allLabelName()));
 	}
 	
 	public void testCreateTaskInCustomtLabel(){
 		final String taskName = "test";
 		final String labelName = "label one";
 		tasksOperator.createTaskAndAssignToLabel(taskName, labelName);
 		
 		assertTrue(tasksOperator.isTaskInLabel(taskName, labelName));
 	}
 	
	@Ignore
 	public void testCreatedTasksAssignedToSelectedLabel(){
 		tasksOperator.createTaskAndAssignToLabel("foo", "myLabel");
 		
 		tasksOperator.selectLabel("myLabel");
 		tasksOperator.createTask("bar");
 		
 		assertTrue(tasksOperator.isTaskInLabel("bar", "myLabel"));
 	}
 	
 	public void testTaskEdition(){
 		final String taskName = "test";
 		final String labelName = "label one";
 		tasksOperator.createTaskAndAssignToLabel(taskName, labelName);
 		
 		final String taskNewName = "Two";
 		tasksOperator.ediTask(taskName, taskNewName);
 		
 		assertTrue(tasksOperator.isTaskInLabel(taskNewName, labelName));
 		assertFalse(tasksOperator.isTaskInLabel(taskName, labelName));
 	}
 	
 	public void testStartStopTask(){
 		final String taskName = "test";
 		tasksOperator.createTask(taskName);
 		
 		tasksOperator.setTime(0);
 		tasksOperator.startTask(taskName);
 		
 		tasksOperator.assertActiveTask(taskName);
 		
 		final int oneHour = (int)DateUtils.MILLIS_PER_HOUR;
 		tasksOperator.setTime(oneHour);
 		tasksOperator.stopTask();
 		
 		int periodIndex = 0;
 		tasksOperator.assertTimeSpent(taskName, periodIndex, 1 * 60);
 		
 	}
 	
 	public void testRenameStartedTask(){
 		final String taskName = "test";
 		String taskNewName = "new task name";
 		
 		tasksOperator.createTask(taskName);
 		tasksOperator.startTask(taskName);
 		tasksOperator.ediTask(taskName, taskNewName);
 		
 		tasksOperator.assertActiveTask(taskNewName);
 	}
 	
 	public void testEditTaskWithoutChangingName(){
 		final String taskName = "test";
 		String taskNewName = "test";
 		
 		tasksOperator.createTask(taskName);
 		tasksOperator.startTask(taskName);
 		tasksOperator.ediTask(taskName, taskNewName);
 		
 		tasksOperator.assertActiveTask(taskNewName);
 	}
 	
 	public void testStartTaskHalfAnHourAgo(){
 		final String taskName = "test";
 		
 		tasksOperator
 			.startTaskHalfAnHourAgo(taskName);
 		tasksOperator.stopTask();
 		
 		int periodIndex = 0;
 		tasksOperator.assertTimeSpent(taskName, periodIndex, 30);
 	}
 	
 	public void testStartNewTaskHalfAnHourAgo() throws InterruptedException{
 		long oneHour = DateUtils.MILLIS_PER_HOUR;
 
 		tasksOperator.createTask("test");
 		tasksOperator.createTask("test 2");
 		tasksOperator
 			.startTask("test");
 		tasksOperator.advanceTimeBy(oneHour);
 		
 		tasksOperator
 			.startTaskHalfAnHourAgo("test 2");
 		tasksOperator.stopTask();
 		
 		long halfAnHourInMinutes = 30;
 		tasksOperator.assertTimeSpentInMinutes("test", halfAnHourInMinutes);
 		tasksOperator.assertTimeSpentInMinutes("test 2", halfAnHourInMinutes);
 	}
 }
