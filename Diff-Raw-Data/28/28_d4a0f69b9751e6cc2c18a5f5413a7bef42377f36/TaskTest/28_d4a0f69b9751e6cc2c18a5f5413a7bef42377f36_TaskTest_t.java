 package org.kalibro.core.concurrent;
 
 import static org.junit.Assert.*;
 import static org.mockito.Matchers.*;
 import static org.mockito.Mockito.*;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.kalibro.KalibroTestCase;
 import org.mockito.Mockito;
 import org.powermock.api.mockito.PowerMockito;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest(Task.class)
 public class TaskTest extends KalibroTestCase implements TaskListener {
 
 	private TaskReport report;
 	private TaskExecutor executor;
 
 	@Before
 	public void setUp() throws Exception {
 		executor = PowerMockito.mock(TaskExecutor.class);
 		PowerMockito.whenNew(TaskExecutor.class).withArguments(any()).thenReturn(executor);
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldExecuteInBackground() {
 		new DoNothingTask().executeInBackground();
 		verify(executor).executeInBackground();
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldExecuteAndWaitWithoutTimeout() {
 		new DoNothingTask().executeAndWait();
 		verify(executor).executeAndWait();
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldExecuteAndWaitWithTimeout() {
 		new DoNothingTask().executeAndWait(42);
 		verify(executor).executeAndWait(42);
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldExecutePeriodically() {
 		new DoNothingTask().executePeriodically(42);
 		Mockito.verify(executor).executePeriodically(42);
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldCancelPeriodicExecution() {
 		new DoNothingTask().cancelPeriodicExecution();
 		verify(executor).cancelPeriodicExecution();
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldNotifyListenerOfTaskDone() throws InterruptedException {
 		runAndGetReport(new DoNothingTask());
 		assertTrue(report.isTaskDone());
 		assertNull(report.getError());
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldNotifyListenerOfTaskHalted() throws InterruptedException {
 		runAndGetReport(new ThrowErrorTask());
 		assertFalse(report.isTaskDone());
 		assertNotNull(report.getError());
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldHaveDefaultDescription() {
 		assertEquals("running task: org.kalibro.core.concurrent.DoNothingTask", "" + new DoNothingTask());
 	}
 
 	private synchronized void runAndGetReport(Task task) throws InterruptedException {
 		report = null;
 		task.setListener(this);
 		new Thread(task).start();
 		waitNotification();
 	}
 
 	@Override
 	public synchronized void taskFinished(TaskReport taskReport) {
 		report = taskReport;
 		notifyTest();
 	}
 }
