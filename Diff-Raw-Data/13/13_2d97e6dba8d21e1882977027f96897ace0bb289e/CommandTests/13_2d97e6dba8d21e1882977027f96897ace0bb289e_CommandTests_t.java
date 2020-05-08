 package com.jpii.gamekit.test;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 
 import org.junit.Test;
 
 import com.jpii.gamekit.debug.Command;
 import com.jpii.gamekit.debug.CommandAction;
 import com.jpii.gamekit.debug.CommandHandler;
 
 public class CommandTests {
 	@Test
 	public void testCommandBase() {
 		Command cmd = new Command("test", "", "testCommandBase", new CommandAction());
 		assertEquals("Result", "test", cmd.getCommand());
 	}
 	
 	@Test
 	public void testCommandDescription() {
 		Command cmd = new Command("test", "", "testCommandDescription", new CommandAction());
 		assertEquals("Result", "testCommandDescription", cmd.getDescription());
 	}
 	
 	@Test
 	public void testCommandAction() {
 		CommandAction cmdAction = new CommandAction() {
 			public void onRun(Command c, String[] args) {
 				System.out.println("testCommandAction");
 			}
 		};
 		
 		Command cmd = new Command("test", "", "testCommandAction", cmdAction);
 		assertEquals("Result", cmdAction, cmd.getCommandAction());
 	}
 	
 	@Test
 	public void testCommandArgs() {
 		Command cmd = new Command("test", "<required> [optional]", "testCommandArgs", new CommandAction());
 		assertEquals("Result", "<required> [optional]", cmd.getArgs());
 	}
 	
 	@Test
 	public void testCommandHandlerConstructor() {
 		CommandHandler cmdHandler = new CommandHandler();
 		assertEquals("Result", 0, cmdHandler.getCommands().size());
 		
 		ArrayList<Command> cmds = new ArrayList<Command>();
 		cmds.add(new Command("test", "", "testCommandHandlerConstructor", new CommandAction()));
 		cmdHandler = new CommandHandler(cmds);
		assertEquals("Result", 1, cmdHandler.getCommands().size());
	}
	
	@Test
	public void testCommandHandlerRegister() {
		CommandHandler cmdHandler = new CommandHandler();
		cmdHandler.registerCommand(new Command("test", "", "testCommandHandlerRegister", new CommandAction()));
		assertEquals("Result", "test", cmdHandler.getCommands().get(0).getCommand());
 	}
}
