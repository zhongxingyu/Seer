 package keyboardRotation;
 
 import lang.Maybe;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import events.persistence.MustBeCalledInsideATransaction;
 
 import periodsInTasks.MockTask;
 
 import tasks.ActiveTask;
 import tasks.Task;
 import tasks.taskName.ActiveTaskName;
 
 public class PairProgrammingRemoteIntegrationTest {
 
 	@Test
 	public void onIntegrationEnabled_WillAskForRemotePeerAndSyncTask() throws MustBeCalledInsideATransaction{
 		enableIntegration();
 		setRemotePeer("Sol");
 		changeActiveTaskTo("active task");
 		turnKeyboardRotationOn();
 		Assert.assertEquals("startTask active task to Sol", sentCommands());
 	}
 	
 	@Test
 	public void onActiveTaskUpdate_UpdatePeer() throws MustBeCalledInsideATransaction{
 		enableIntegration();
 		setRemotePeer("Sol");
 		changeActiveTaskTo("active task");
 		turnKeyboardRotationOn();
 		changeActiveTaskTo("new task");
 		changeActiveTaskTo("new task");
 		String expected = 
 				"startTask active task to Sol\n" +
 				"startTask new task to Sol";
 		Assert.assertEquals(expected, sentCommands());
 	}
 	
 	@Test
 	public void onIntegrationDisabled_WillDoNothing(){
 		changeActiveTaskTo("active task");
 		turnKeyboardRotationOn();
 		Assert.assertEquals("", sentCommands());
 	}
 	
 	private String sentCommands() {
 		return net.sentCommands();
 	}
 	private void setRemotePeer(String remotePeer) {
 		gui.setRemotePeer(remotePeer);
 	}
 	private void turnKeyboardRotationOn() {
 		turnOn.fire();
 	}

 	private void changeActiveTaskTo(String task) {
 		activeTask.supply(Maybe.wrap((Task)new MockTask(task)));
 	}
 	
 	private void enableIntegration() throws MustBeCalledInsideATransaction {
 		options.setRemoteIntegration(true);
 	}
 	
 	ActiveTask activeTask = new ActiveTask();
 	ActiveTaskName task = new ActiveTaskName(activeTask);
 	TurnOnKeyboardRotationAlert turnOn = new TurnOnKeyboardRotationAlert();
 	PairProgrammingRemoteIntegrationGuiMock gui = new PairProgrammingRemoteIntegrationGuiMock();
 	NetworkMock net = new NetworkMock();
 	private KeyboardRotationOptions options = new KeyboardRotationOptions();
 	PairProgrammingRemoteIntegration subject = new PairProgrammingRemoteIntegration(turnOn, task, options, gui, net);
 	
 }
