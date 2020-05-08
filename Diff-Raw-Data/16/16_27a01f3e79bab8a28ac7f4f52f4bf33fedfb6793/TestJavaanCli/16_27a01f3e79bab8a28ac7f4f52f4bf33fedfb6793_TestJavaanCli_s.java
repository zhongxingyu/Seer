 package org.javaan;
 
 import static org.junit.Assert.*;
 import static org.mockito.Matchers.*;
 import static org.mockito.Mockito.*;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.Options;
 import org.junit.Test;
 
 public class TestJavaanCli {
 	
 	private static abstract class CommandStub implements Command {
 		
 		@Override
 		public Options buildCommandLineOptions(Options options) {
 			return options;
 		}
 	}
 	
 	@Test
 	public void testExecuteParseError() {
 		assertEquals(ReturnCodes.errorParse.getValue(), new JavaanCli(new String[]{}, new CommandMap()).execute());
 		assertEquals(ReturnCodes.errorParse.getValue(), new JavaanCli(new String[]{"unknownCommand"}, new CommandMap()).execute());
 		assertEquals(ReturnCodes.errorParse.getValue(), new JavaanCli(new String[]{"unknownCommand", "file1"}, new CommandMap()).execute());
 	}
 	
 	@Test
 	public void testExecuteParseErrorUnkownOption() {
 		CommandMap commands = new CommandMap();
 		Command command = mock(CommandStub.class);
 		doCallRealMethod().when(command).buildCommandLineOptions(any(Options.class));
 		when(command.getName()).thenReturn("test");
 		when(command.getHelpCommandLine()).thenReturn("javaan test");
 		commands.addCommand(command);
 		assertEquals(ReturnCodes.errorParse.getValue(), new JavaanCli(new String[]{"test", "--unknownOption"}, commands).execute());
 		verify(command).getHelpCommandLine();
 	}
 
 	@Test
 	public void testExecuteParseErrorMissingFiles() {
 		CommandMap commands = new CommandMap();
 		Command command = mock(CommandStub.class);
 		doCallRealMethod().when(command).buildCommandLineOptions(any(Options.class));
 		when(command.getName()).thenReturn("test");
 		when(command.getHelpCommandLine()).thenReturn("javaan test");
 		commands.addCommand(command);
		assertEquals(ReturnCodes.errorParse.getValue(), new JavaanCli(new String[]{"test"}, commands).execute());
 	}
 	
 	@Test
 	public void testExecuteCommandError() {
 		CommandMap commands = new CommandMap();
 		Command command = mock(CommandStub.class);
 		when(command.getName()).thenReturn("test");
 		doCallRealMethod().when(command).buildCommandLineOptions(any(Options.class));
 		doThrow(new RuntimeException("error")).when(command).execute(any(CommandLine.class), any(String[].class));
 		commands.addCommand(command);
 		assertEquals(ReturnCodes.errorCommand.getValue(), new JavaanCli(new String[]{"test", "file1"}, commands).execute());
 		verify(command).execute(any(CommandLine.class), any(String[].class));
 	}
 	
 	@Test
 	public void testExecuteOneFile() {
 		CommandMap commands = new CommandMap();
 		Command command = mock(CommandStub.class);
 		when(command.getName()).thenReturn("test");
 		when(command.execute(any(CommandLine.class), any(String[].class))).thenReturn(ReturnCodes.ok);
 		doCallRealMethod().when(command).buildCommandLineOptions(any(Options.class));
 		commands.addCommand(command);
 		assertEquals(0, new JavaanCli(new String[]{"test", "file1"}, commands).execute());
 		verify(command).execute(any(CommandLine.class), eq(new String[]{"file1"}));
 	}
 	
 	@Test
 	public void testExecuteTwoFiles() {
 		CommandMap commands = new CommandMap();
 		Command command = mock(CommandStub.class);
 		when(command.getName()).thenReturn("test");
 		when(command.execute(any(CommandLine.class), any(String[].class))).thenReturn(ReturnCodes.ok);
 		doCallRealMethod().when(command).buildCommandLineOptions(any(Options.class));
 		commands.addCommand(command);
 		assertEquals(0, new JavaanCli(new String[]{"test", "file1", "file2"}, commands).execute());
 		verify(command).execute(any(CommandLine.class), eq(new String[]{"file1", "file2"}));
 	}
 
 	@Test
 	public void testExecuteHelp() {
 		CommandMap commands = new CommandMap();
 		Command command = mock(CommandStub.class);
 		doCallRealMethod().when(command).buildCommandLineOptions(any(Options.class));
 		when(command.getName()).thenReturn("test");
 		when(command.getHelpCommandLine()).thenReturn("javaan test");
 		commands.addCommand(command);
 		assertEquals(0, new JavaanCli(new String[]{"test", "--help"}, commands).execute());
 		verify(command).getHelpCommandLine();
 	}
 }
