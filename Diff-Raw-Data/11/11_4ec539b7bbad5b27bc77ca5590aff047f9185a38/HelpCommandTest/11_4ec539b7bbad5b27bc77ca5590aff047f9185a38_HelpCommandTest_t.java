 /*******************************************************************************
  Copyright (c) 2013 James Richardson.
 
  HelpCommandTest.java is part of bukkit-utilities.
 
  BukkitUtilities is free software: you can redistribute it and/or modify it
  under the terms of the GNU General Public License as published by the Free
  Software Foundation, either version 3 of the License, or (at your option) any
  later version.
 
  BukkitUtilities is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License along with
  BukkitUtilities. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 
 package name.richardson.james.bukkit.utilities.command;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 
 import org.bukkit.permissions.Permissible;
 
 import junit.framework.TestCase;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 import org.mockito.Matchers;
 
 import name.richardson.james.bukkit.utilities.command.context.CommandContext;
 
 import static org.mockito.Matchers.anyInt;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.*;
 
 @RunWith(JUnit4.class)
 public class HelpCommandTest extends CommandTestCase {
 
 	private static final String COMMAND_LABEL = "test";
 
 	private HelpCommand command;
 	private Command mockCommand;
 
 	@Test
 	public void IsAuthorised_RegardlessOfParameter_ReturnTrue()
 	throws Exception {
 		Assert.assertTrue(command.isAuthorised(null));
 	}
 
 	@Test
 	public void Execute_WhenCommandNotInMap_ReturnCommandList() throws Exception {
 		CommandContext commandContext = getCommandContext();
 		command.execute(commandContext);
 		verify(commandContext.getCommandSender(), times(4)).sendMessage(anyString());
 	}
 
 	@Test
 	public void Execute_WhenListingCommands_OnlyIncludeAuthorisedCommands() throws Exception {
 		CommandContext commandContext = getCommandContext();
 		when(mockCommand.isAuthorised(Matchers.<Permissible>any())).thenReturn(false);
 		command.execute(commandContext);
 		verify(commandContext.getCommandSender(), times(3)).sendMessage(anyString());
 	}
 
 	@Test
 	public void Execute_WhenCommandInMap_ReturnHelp()
 	throws Exception {
 		CommandContext commandContext = getCommandContext();
 		when(commandContext.has(anyInt())).thenReturn(true);
		when(commandContext.getString(1)).thenReturn("test");
 		command.execute(commandContext);
 		verify(commandContext.getCommandSender(), times(2)).sendMessage(anyString());
 	}
 
 	@Before
 	public void setUp()
 	throws Exception {
 		mockCommand = mock(Command.class);
 		when(mockCommand.getName()).thenReturn("test");
 		when(mockCommand.getUsage()).thenReturn("<test> [test] <test>");
 		when(mockCommand.getDescription()).thenReturn("A description.");
 		when(mockCommand.isAuthorised(Matchers.<Permissible>any())).thenReturn(true);
 		command = new HelpCommand("test", new HashSet<Command>(Arrays.asList(mockCommand)));
 	}
 
 }
