 package org.zend.sdk.test.sdkcli.commands;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 
 import org.junit.Test;
 import org.mockito.Mockito;
 import org.zend.sdkcli.ICommand;
 import org.zend.sdkcli.ParseError;
 import org.zend.sdkcli.internal.commands.CommandLine;
 import org.zend.sdklib.SdkException;
 import org.zend.sdklib.repository.IRepository;
 
 public class TestAddRepositoryCommand extends AbstractRepositoryCommandTest {
 
 	@Test
 	public void testByCommandFactory() throws ParseError {
 		File repositoryFolder = new File(FOLDER + "repository");
 		String reposiotryUrl = "file:/" + repositoryFolder.getAbsolutePath();
 		CommandLine line = getLine("add repository -u " + reposiotryUrl
 				+ " -n testRepo");
 		ICommand command = getCommand(line);
 		assertNotNull(command);
 		assertTrue(command.execute(line));
 	}
 
 	@Test
 	public void testSDKException() throws ParseError, SdkException {
 		File repositoryFolder = new File(FOLDER + "repository");
 		String reposiotryUrl = "file:/" + repositoryFolder.getAbsolutePath();
 		CommandLine line = getLine("add repository -u " + reposiotryUrl
 				+ " -n testRepo");
		ICommand command = getCommand(line);
 		Mockito.when(manager.add((IRepository) Mockito.any())).thenThrow(
 				new SdkException("test"));
 		assertNotNull(command);
 		assertFalse(command.execute(line));
 	}
 
 	@Test(expected = ParseError.class)
 	public void testNoArgU() throws ParseError {
 		CommandLine line = getLine("add repository");
 		ICommand command = getCommand(line);
 		assertNotNull(command);
 		command.execute(line);
 	}
 
 	@Test(expected = ParseError.class)
 	public void testUnknowArg() throws ParseError {
 		CommandLine line = getLine("add repository -z");
 		ICommand command = getCommand(line);
 		assertNotNull(command);
 		command.execute(line);
 	}
 
 	@Test
 	public void testIncorrectU() throws ParseError {
 		CommandLine line = getLine("add repository -u incorrectPath");
 		ICommand command = getCommand(line);
 		assertNotNull(command);
 		assertFalse(command.execute(line));
 	}
 
 }
