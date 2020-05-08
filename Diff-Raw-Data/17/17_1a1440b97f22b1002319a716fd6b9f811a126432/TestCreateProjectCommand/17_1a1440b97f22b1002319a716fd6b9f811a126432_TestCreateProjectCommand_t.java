 package org.zend.sdk.test.sdkcli.commands;
 
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Test;
 import org.zend.sdk.test.AbstractTest;
 import org.zend.sdkcli.CommandFactory;
 import org.zend.sdkcli.ICommand;
 import org.zend.sdkcli.ParseError;
 import org.zend.sdkcli.internal.commands.CommandLine;
 import org.zend.sdkcli.internal.commands.CreateProjectCommand;
 
 public class TestCreateProjectCommand extends AbstractTest {
 
 	@Test
 	public void testByCommandFactory() throws ParseError {
 		CommandLine cmdLine = new CommandLine(new String[] { "create",
				"project", "-name", "testName" });
 		ICommand command = CommandFactory.createCommand(cmdLine);
 		assertNotNull(command);
 		assertTrue(command.execute());
 	}
 
 	@Test
	public void testByConstructor1() throws ParseError {
 		CommandLine cmdLine = new CommandLine(new String[] { "create",
				"project", "-name", "testName", "-destination", "abc" });
		ICommand command = new CreateProjectCommand(cmdLine);
		assertNotNull(command);
		assertTrue(command.execute());
	}
	
	@Test
	public void testByConstructor2() throws ParseError {
		CommandLine cmdLine = new CommandLine(new String[] { "create",
				"project", "-name", "testName", "-destination", "def", "-no_scripts" });
 		ICommand command = new CreateProjectCommand(cmdLine);
 		assertNotNull(command);
 		assertTrue(command.execute());
 	}
 }
