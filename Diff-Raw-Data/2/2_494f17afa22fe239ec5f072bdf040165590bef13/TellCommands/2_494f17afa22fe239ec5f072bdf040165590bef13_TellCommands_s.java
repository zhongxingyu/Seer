 /*
  * Copyright (C) 2012 CyborgDev <cyborg@alta189.com>
  *
  * This file is part of CommandKit
  *
  * CommandKit is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * CommandKit is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.alta189.cyborg.commandkit.tell;
 
 import com.alta189.cyborg.Cyborg;
 import com.alta189.cyborg.api.command.CommandContext;
 import com.alta189.cyborg.api.command.CommandResult;
 import com.alta189.cyborg.api.command.CommandSource;
 import com.alta189.cyborg.api.command.ReturnType;
 import com.alta189.cyborg.api.command.annotation.Command;
 import com.alta189.cyborg.api.util.StringUtils;
 import java.util.List;
 
 import static com.alta189.cyborg.api.command.CommandResultUtil.get;
 import static com.alta189.cyborg.commandkit.CommandKit.getDatabase;
 
 public class TellCommands {
 	@Command(name = "tell", desc = "Sends a tell to a user")
 	public CommandResult tell(CommandSource source, CommandContext context) {
 		if (source.getSource() != CommandSource.Source.USER) {
 			return null;
 		}
 		if (context.getPrefix() == null || !context.getPrefix().equals(".")) {
 			return null;
 		}
 		if (context.getArgs() == null || context.getArgs().length < 2) {
 			return get(ReturnType.NOTICE, "Correct usage is .tell <nick> <message>...", source, context);
 		}
 		
 		String nick = context.getArgs()[0];
 		String message = StringUtils.toString(context.getArgs(), 1);
 		
 		TellEntry entry = new TellEntry();
 		entry.setSender(source.getUser().getNick());
 		entry.setReceiver(nick.toLowerCase());
 		entry.setMessage(message);
 		entry.setTimestamp(System.currentTimeMillis());
 		
 		getDatabase().save(TellEntry.class, entry);
 		
 		return get(ReturnType.NOTICE, "Your tell was sent!", source, context);
 	}
 
 	@Command(name = "showtells", desc = "Shows a user all their unread tells")
 	public CommandResult showtells(CommandSource source, CommandContext context) {
 		if (source.getSource() != CommandSource.Source.USER) {
 			return null;
 		}
 		if (context.getPrefix() == null || !context.getPrefix().equals(".")) {
 			return null;
 		}
 
		List<TellEntry> entries = getDatabase().select(TellEntry.class).where().equal("receiver", source.getUser().getNick()).and().equal("alerted", false).and().equal("received", false).execute().find();
 		if (entries.size() < 1) {
 			  return get(ReturnType.NOTICE, "You have no new tells", source, context);
 		}
 
 		StringBuilder builder;
 		for (TellEntry entry : entries) {
 			builder = new StringBuilder();
 			builder.append(entry.getSender()).append(" said ").append(entry.getDifference(System.currentTimeMillis())).append(": ").append(entry.getMessage());
 
 			Cyborg.getInstance().sendNotice(source.getUser(), builder.toString());
 
 			entry.setAlerted(true);
 			entry.setReceived(true);
 			getDatabase().save(TellEntry.class, entry);
 		}
 		return null;
 	}
 }
