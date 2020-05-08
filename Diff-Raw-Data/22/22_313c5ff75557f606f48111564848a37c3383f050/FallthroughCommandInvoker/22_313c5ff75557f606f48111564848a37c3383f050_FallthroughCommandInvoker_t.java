 /*******************************************************************************
  Copyright (c) 2013 James Richardson.
 
  FallthroughCommandInvoker.java is part of bukkit-utilities.
 
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
 
 package name.richardson.james.bukkit.utilities.command.invoker;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.bukkit.command.CommandSender;
 
 import org.apache.commons.lang.Validate;
 
 import name.richardson.james.bukkit.utilities.command.Command;
 import name.richardson.james.bukkit.utilities.command.context.CommandContext;
 import name.richardson.james.bukkit.utilities.command.context.NestedCommandContext;
 import name.richardson.james.bukkit.utilities.command.context.PassthroughCommandContext;
 
 /**
  * A FallthroughCommandInvoker attempts to match a command based on the first argument passed to it. If no command can be found the arguments `fall through` to
  * a default argument.
  */
 public class FallthroughCommandInvoker extends AbstractCommandInvoker {
 
 	private final Command fallthroughCommand;
 
 	public FallthroughCommandInvoker(Command fallthroughCommand) {
 		Validate.notNull(fallthroughCommand);
 		this.fallthroughCommand = fallthroughCommand;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender commandSender, org.bukkit.command.Command bukkitCommand, String commandLabel, String[] arguments) {
 		String commandName = (arguments.length == 0) ? null : arguments[0];
		if (commandName != null &&  getCommands().containsKey(commandName)) {
			Command command = getCommands().get(commandName);
 			CommandContext commandContext = new NestedCommandContext(arguments, commandSender);
 			command.execute(commandContext);
 			return true;
 		} else {
 			CommandContext commandContext = new PassthroughCommandContext(arguments, commandSender);
 			fallthroughCommand.execute(commandContext);
 			return true;
 		}
 	}
 
 	@Override
 	public List<String> onTabComplete(CommandSender commandSender, org.bukkit.command.Command bukkitCommand, String commandLabel, String[] arguments) {
 		String commandName = (arguments.length == 0) ? null : arguments[0];
		if (commandName != null && getCommands().containsKey(commandName)) {
			Command command = getCommands().get(commandName);
 			CommandContext commandContext = new NestedCommandContext(arguments, commandSender);
 			List<String> results = new ArrayList<String>(command.getArgumentMatches(commandContext));
 			return results;
 		} else {
 			CommandContext commandContext = new PassthroughCommandContext(arguments, commandSender);
 			List<String> results = new ArrayList<String>(fallthroughCommand.getArgumentMatches(commandContext));
 			return results;
 		}
 	}
 
 }
