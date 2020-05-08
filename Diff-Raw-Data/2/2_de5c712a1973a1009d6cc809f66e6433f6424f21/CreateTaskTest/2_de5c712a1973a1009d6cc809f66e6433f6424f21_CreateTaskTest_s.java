 package ca.ualberta.cs.completemytask.test;
 
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 
 import ca.ualberta.cs.completemytask.R;
 import ca.ualberta.cs.completemytask.activities.AddTaskActivity;
 import ca.ualberta.cs.completemytask.userdata.Task;
 import ca.ualberta.cs.completemytask.userdata.TaskManager;
 import android.test.ActivityInstrumentationTestCase2;
 import android.test.UiThreadTest;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 
 public class CreateTaskTest extends ActivityInstrumentationTestCase2<AddTaskActivity> {
 
 	private AddTaskActivity main;
 	private Button addButton;
 	
 	// Task Info
 	private String taskName = "Test Task";
 	private String taskDescription = "Test Description";
 	
 	private boolean testNeedsComment = true;
 	private boolean testNeedsPhoto = true;
 	private boolean testNeedsAudio = false;
 	
 	public CreateTaskTest() {
 		super(AddTaskActivity.class);
 	}
 
 	protected void setUp() throws Exception {
 		super.setUp();
 		this.main = getActivity();
 		this.addButton = (Button) this.main.findViewById(R.id.AddTaskButton);
 	}
 
 	
 	@UiThreadTest
 	public void testAddTask() {
 		// Edit Text Views
       	EditText taskNameEditText = (EditText) main.findViewById(R.id.EditTaskName);
       	taskNameEditText.setText(taskName);
       	
       	EditText taskDescriptionEditText = (EditText) main.findViewById(R.id.EditTaskDescription);
       	taskDescriptionEditText.setText(taskDescription);
       	
       	// Check box views
       	CheckBox textRequirementCheckbox = (CheckBox) main.findViewById(R.id.TextRequirementCheckbox);
       	textRequirementCheckbox.setChecked(testNeedsComment);
       	
       	CheckBox photoRequirementCheckbox = (CheckBox) main.findViewById(R.id.PictureRequirementCheckbox);
       	photoRequirementCheckbox.setChecked(testNeedsPhoto);
       	
       	CheckBox audioRequirementCheckbox = (CheckBox) main.findViewById(R.id.AudioRequirementCheckbox);
       	audioRequirementCheckbox.setChecked(testNeedsAudio);
     	
     	CountDownLatch latch = new CountDownLatch(1);
     	try {
 			latch.await(2, TimeUnit.SECONDS);
 			addButton.performClick();
 			latch.await(2, TimeUnit.SECONDS);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     	
     	boolean foundTask = false;
 		for(Task task : TaskManager.getInstance().getTaskArray()) {
 			if(task.getName().equals(taskName)) {
 				foundTask = true;
 				assertTrue("Wrong Description", task.getDescription().endsWith(taskDescription));
				assertTrue("No ID", task.getId() == null);
 				
 				assertTrue("Failed isComplete", task.isComplete() == false);
 				
 				assertTrue("Failed needs Comment", task.needsComment() == testNeedsComment);
 				assertTrue("Failed needs Photo", task.needsPhoto() == testNeedsPhoto);
 				assertTrue("Failed needs Comment", task.needsAudio() == testNeedsAudio);
 			}
 		}
 		
 		assertTrue("Task Not Found", foundTask);
 	}
 }
