 /*
  * This file is part of GreatmancodeTools.
  *
  * Copyright (c) 2013-2013, Greatman <http://github.com/greatman/>
  *
  * GreatmancodeTools is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * GreatmancodeTools is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with GreatmancodeTools.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.greatmancode.tools.commands.canary;
 
 import java.util.Arrays;
 
 import com.greatmancode.tools.commands.CommandHandler;
 import com.greatmancode.tools.commands.SubCommand;
 import com.greatmancode.tools.commands.interfaces.CommandReceiver;
 
 import net.canarymod.chat.MessageReceiver;
 import net.larry1123.lib.plugin.commands.CommandExecute;
 
 public class CanaryCommandReceiver implements CommandReceiver, CommandExecute {
 
 	private CommandHandler commandHandler;
 
 	public CanaryCommandReceiver(CommandHandler commandHandler) {
 		this.commandHandler = commandHandler;
 	}
 
 	@Override
 	public void execute(MessageReceiver messageReceiver, String[] args) {
		SubCommand subCommand = commandHandler.getCommand(args[0].replace("/", ""));
 		if (subCommand != null) {
 			String subCommandValue = "";
 			String[] newArgs;
 			if (args.length <= 2) {
 				newArgs = new String[0];
 				if (args.length == 2) {
 					subCommandValue = args[1];
 				}
 			} else {
 				newArgs = new String[args.length - 2];
 				subCommandValue = args[1];
 				System.arraycopy(args, 2, newArgs, 0, args.length - 2);
 			}
 			subCommand.execute(subCommandValue, messageReceiver.getName(), newArgs);
 		}
 	}
 }
