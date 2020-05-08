 /*
  * Copyright (C) 2012 CyborgDev <cyborg@alta189.com>
  *
  * This file is part of CyborgFactoids
  *
  * CyborgFactoids is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * CyborgFactoids is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.alta189.cyborg.factoids.handlers;
 
 import com.alta189.cyborg.Cyborg;
 import com.alta189.cyborg.api.command.CommandContext;
 import com.alta189.cyborg.api.command.CommandException;
 import com.alta189.cyborg.api.command.CommandManager;
 import com.alta189.cyborg.api.command.CommandResult;
 import com.alta189.cyborg.api.command.CommandSource;
 import com.alta189.cyborg.api.command.CommonCommandManager;
 import com.alta189.cyborg.factoids.Factoid;
 import com.alta189.cyborg.factoids.FactoidContext;
 import com.alta189.cyborg.factoids.FactoidResult;
 import com.alta189.cyborg.factoids.LocationType;
 import com.alta189.cyborg.factoids.ReturnType;
import java.lang.reflect.Field;
 
 public class CommandHandler implements Handler {
 	public static final String name = "command";
 	public final CommandManager manager;
 
 	public CommandHandler() {
 		manager = Cyborg.getInstance().getCommandManager();
 	}
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	@Override
 	public FactoidResult handle(Factoid factoid, FactoidContext context) {
 		if (!manager.isCommand(factoid.getContents())) {
 			return null;
 		}
		String[] args;
		if (context.getRawArgs() != null && !context.getRawArgs().isEmpty()) {
			if (context.getRawArgs().contains(" "))  {
				args = context.getRawArgs().split(" ");
			} else {
				args = new String[]{context.getRawArgs()};
			}
		} else {
			args = null;
		}
		
		CommandContext commandContext = new CommandContext(args, ".", context.getLocationType() == LocationType.CHANNEL_MESSAGE ? CommandContext.LocationType.CHANNEL : CommandContext.LocationType.PRIVATE_MESSAGE);
		if (context.getLocationType() == LocationType.CHANNEL_MESSAGE) {
			try {
				Field field = commandContext.getClass().getDeclaredField("location");
				field.setAccessible(true);
				field.set(commandContext, context.getChannel().getName());
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		
		
 		CommandSource source = new CommandSource(context.getSender());
 
 		CommandResult commandResult = null;
 
 		try {
 			commandResult = manager.execute(source, manager.getCommandMap().getCommand(((CommonCommandManager) manager).getCommand(factoid.getContents())), commandContext);
 		} catch (CommandException e) {
 			e.printStackTrace();
 		}
 
 		if (commandResult != null) {
 			FactoidResult result = new FactoidResult();
 			result.setBody(commandResult.getBody());
 			result.setTarget(commandResult.getTarget());
 			switch (commandResult.getReturnType()) {
 				case ACTION:
 					result.setReturnType(ReturnType.ACTION);
 					break;
 				case MESSAGE:
 					result.setReturnType(ReturnType.MESSAGE);
 					break;
 				case NOTICE:
 					result.setReturnType(ReturnType.NOTICE);
 					break;
 			}
 			return result;
 		}
 
 		return null;
 	}
 }
