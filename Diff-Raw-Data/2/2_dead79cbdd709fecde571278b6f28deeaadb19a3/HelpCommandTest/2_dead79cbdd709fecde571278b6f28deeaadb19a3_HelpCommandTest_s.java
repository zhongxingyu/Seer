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
 
 import java.util.*;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.PluginDescriptionFile;
 
 import junit.framework.TestCase;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import name.richardson.james.bukkit.utilities.command.context.CommandContext;
 import name.richardson.james.bukkit.utilities.permissions.PermissionManager;
 
 import static org.mockito.Matchers.anyInt;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.*;
 
 public class HelpCommandTest extends TestCase {
 
 	private final String PLUGIN_NAME = "TestPlugin";
 	private final String PLUGIN_VERSION = "v1.0";
 	private final String COMMAND_LABEL = "tt";
	private final Set<Command> COMMANDS = new TreeSet<Command>();
 
 	private HelpCommand command;
 	private CommandContext commandContext;
 	private CommandSender commandSender;
 	private Command nestedCommand;
 	private PermissionManager permissionManager;
 
 	@Test
 	public void testExecuteListAllCommands()
 	throws Exception {
 		when(nestedCommand.isAuthorised(commandSender)).thenReturn(true);
 		when(commandContext.has(anyInt())).thenReturn(false);
 		command.execute(commandContext);
 		verify(commandSender).sendMessage("§dTestPlugin (v1.0)");
 		verify(commandSender).sendMessage("§bnull");
 		verify(commandSender).sendMessage("§eType §a/tt§e §aname§e §a§a[command]§e for help");
 		verify(commandSender).sendMessage("§c/tt §etest §a[test] §e<test>");
 		verify(commandSender, times(4)).sendMessage(anyString());
 	}
 
 	@Test
 	public void testMatchArguments() {
 		when(commandContext.has(anyInt())).thenReturn(true);
 		when(commandContext.getString(anyInt())).thenReturn("test");
 		when(commandContext.size()).thenReturn(1);
 		Set<String> results = command.getArgumentMatches(commandContext);
 		System.out.print(results);
 		Assert.assertEquals("Command matcher should only return one value!", 1, results.size());
 		Assert.assertTrue("Matcher results does not contain expected value!", results.contains("test"));
 	}
 
 	@Test
 	public void testExecuteCommandNoMatch()
 	throws Exception {
 		when(nestedCommand.isAuthorised(commandSender)).thenReturn(true);
 		when(commandContext.has(anyInt())).thenReturn(true);
 		when(commandContext.getString(anyInt())).thenReturn("frank");
 		command.execute(commandContext);
 		verify(commandSender).sendMessage("§dTestPlugin (v1.0)");
 		verify(commandSender).sendMessage("§bnull");
 		verify(commandSender).sendMessage("§eType §a/tt§e §aname§e §a§a[command]§e for help");
 		verify(commandSender).sendMessage("§c/tt §etest §a[test] §e<test>");
 		verify(commandSender, times(4)).sendMessage(anyString());
 	}
 
 	@Test
 	public void testExecuteListCommandNone()
 	throws Exception {
 		when(nestedCommand.isAuthorised(commandSender)).thenReturn(false);
 		when(commandContext.has(anyInt())).thenReturn(true);
 		when(commandContext.getString(anyInt())).thenReturn("frank");
 		command.execute(commandContext);
 		verify(commandSender).sendMessage("§dTestPlugin (v1.0)");
 		verify(commandSender).sendMessage("§bnull");
 		verify(commandSender).sendMessage("§eType §a/tt§e §aname§e §a§a[command]§e for help");
 		verify(commandSender, times(3)).sendMessage(anyString());
 	}
 
 
 	@Test
 	public void testExecuteMatchCommand()
 	throws Exception {
 		when(nestedCommand.isAuthorised(commandSender)).thenReturn(true);
 		when(commandContext.has(anyInt())).thenReturn(true);
 		when(commandContext.getString(anyInt())).thenReturn("test");
 		command.execute(commandContext);
 		verify(commandSender).sendMessage("§dtest command");
 		verify(commandSender).sendMessage("§c/tt §etest §a[test] §e<test>");
 		verify(commandSender, times(2)).sendMessage(anyString());
 	}
 
 	@Before
 	public void setUp()
 	throws Exception {
 		nestedCommand = mock(Command.class);
 		commandContext = mock(CommandContext.class);
 		commandSender = mock(CommandSender.class);
 		permissionManager = mock(PermissionManager.class);
 		when(commandContext.getCommandSender()).thenReturn(commandSender);
 		when(nestedCommand.getName()).thenReturn("test");
 		when(nestedCommand.getDescription()).thenReturn("test command");
 		when(nestedCommand.getUsage()).thenReturn("[test] <test>");
 		PluginDescriptionFile pluginDescriptionFile = new PluginDescriptionFile(PLUGIN_NAME, PLUGIN_VERSION, null);
 		COMMANDS.add(nestedCommand);
 		command = new HelpCommand(permissionManager, COMMAND_LABEL, pluginDescriptionFile, COMMANDS);
 	}
 }
