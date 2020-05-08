 package org.kalibro.core.command;
 
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.kalibro.KalibroTestCase;
import org.kalibro.core.concurrent.Task;
 import org.mockito.Mockito;
 import org.powermock.api.mockito.PowerMockito;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest(CommandTask.class)
 public class CommandTaskTest extends KalibroTestCase {
 
 	private static final String COMMAND = "CommandTaskTest command";
 
 	private Runtime runtime;
 	private Process process;
 	private InputStream output, error;
 	private FileProcessStreamLogger logger;
 
 	private CommandTask commandTask;
 
 	@Before
 	public void setUp() throws Exception {
 		mockProcess();
 		mockLogger();
 		commandTask = new CommandTask(COMMAND);
 	}
 
 	private void mockProcess() throws IOException {
 		runtime = PowerMockito.mock(Runtime.class);
 		process = PowerMockito.mock(Process.class);
 		output = PowerMockito.mock(InputStream.class);
 		error = PowerMockito.mock(InputStream.class);
 
 		PowerMockito.mockStatic(Runtime.class);
 		PowerMockito.when(Runtime.getRuntime()).thenReturn(runtime);
 		PowerMockito.when(runtime.exec(COMMAND)).thenReturn(process);
 		PowerMockito.when(process.getInputStream()).thenReturn(output);
 		PowerMockito.when(process.getErrorStream()).thenReturn(error);
 	}
 
 	private void mockLogger() throws Exception {
 		logger = PowerMockito.mock(FileProcessStreamLogger.class);
 		PowerMockito.whenNew(FileProcessStreamLogger.class).withNoArguments().thenReturn(logger);
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
	public void checkExceptionExecutingForOuput() throws IOException {
		PowerMockito.when(runtime.exec(COMMAND)).thenThrow(new IOException());
		checkKalibroException(new Task() {

			@Override
			public void perform() throws IOException {
				commandTask.executeAndGetOuput();
			}
		}, "", IOException.class);
	}

	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldGetCommandOutput() throws IOException {
 		assertSame(output, commandTask.executeAndGetOuput());
 		Mockito.verify(logger).logErrorStream(process, COMMAND);
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void checkExceptionOnSimpleExecution() throws IOException {
 		PowerMockito.when(runtime.exec(COMMAND)).thenThrow(new IOException());
		checkKalibroException(commandTask, "", IOException.class);
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldThrowExceptionOnBadExitValue() {
 		PowerMockito.when(process.exitValue()).thenReturn(1);
 		checkKalibroException(commandTask, "Command returned with error status: " + COMMAND);
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldWaitForProcess() throws Exception {
 		commandTask.perform();
 		Mockito.verify(process).waitFor();
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldDestroyProcessOnInterruptedException() throws InterruptedException {
 		PowerMockito.when(process.waitFor()).thenThrow(new InterruptedException());
		checkKalibroException(commandTask, "", InterruptedException.class);
 		Mockito.verify(process).destroy();
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldLogOutput() throws Exception {
 		commandTask.perform();
 		Mockito.verify(logger).logErrorStream(process, COMMAND);
 		Mockito.verify(logger).logOutputStream(process, COMMAND);
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldShowCommandOnDescription() {
 		assertEquals("executing command: " + COMMAND, commandTask.toString());
 	}
 }
