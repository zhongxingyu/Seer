 package tasks;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import junit.framework.TestCase;
 
 
 import org.apache.commons.lang.time.DateUtils;
 
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
 	
 	public void testTaskEdition(){
 		final String taskName = "test";
 		final String labelName = "label one";
 		tasksOperator.createTaskAndAssignToLabel(taskName, labelName);
 		
 		final String taskNewName = "Two";
 		tasksOperator.ediTask(taskName, taskNewName);
 		
 		assertTrue(tasksOperator.isTaskInLabel(taskNewName, labelName));
 		assertFalse(tasksOperator.isTaskInLabel(taskName, labelName));
 	}
 	
 	public void testStartLastActiveTasks(){
 		
 		String taskPrefix = "test";
 		java.util.List<String> taskNames = new ArrayList<String>();
 		
 		for (int i = 0; i<3; i++){
 			final String taskName = taskPrefix + i;
 			taskNames.add(taskName);
 			tasksOperator.createTask(taskName);
 		}
 		
 		tasksOperator.startTask(taskPrefix + 0);
 		tasksOperator.stopTask();
 		
 		String newTask0Name = "foo";
 		tasksOperator.ediTask(taskNames.get(1), newTask0Name);
 		
 		tasksOperator.assertLastActiveTasks(
 				Arrays.asList(
 						newTask0Name,
 						taskNames.get(0), 
 						taskNames.get(2) ));
 		
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
 		
 //		bug tasksOperator should not know periods
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
 	
	public void testStartTaskHalfAnHourAgo(){
 		final String taskName = "test";
 		
 		tasksOperator.createTask(taskName);
 		tasksOperator
 			.startTaskHalfAnHourAgo(taskName);
 		tasksOperator.stopTask();
 		
 		int periodIndex = 0;
 		tasksOperator.assertTimeSpent(taskName, periodIndex, 30);
 	}
 	
 	public void testStartNewTaskNow(){
 		final String taskName = "test";
 		
 		tasksOperator
 			.startNewTaskNow(taskName);
 		tasksOperator.stopTask();
 		
 		int periodIndex = 0;
 		tasksOperator.assertTimeSpent(taskName, periodIndex, 0);
 	}
 }
