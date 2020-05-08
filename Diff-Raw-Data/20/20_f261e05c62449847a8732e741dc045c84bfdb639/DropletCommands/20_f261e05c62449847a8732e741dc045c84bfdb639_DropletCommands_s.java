 /*
  * This file is part of DropletAsk.
  *
  * Copyright (c) 2011-2012, SpoutDev <http://www.spout.org/>
  * DropletAsk is licensed under the SpoutDev License Version 1.
  *
  * DropletAsk is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * In addition, 180 days after any changes are published, you can use the
  * software, incorporating those changes, under the terms of the MIT license,
  * as described in the SpoutDev License Version 1.
  *
  * DropletAsk is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License,
  * the MIT license and the SpoutDev License Version 1 along with this program.
  * If not, see <http://www.gnu.org/licenses/> for the GNU Lesser General Public
  * License and see <http://www.spout.org/SpoutDevLicenseV1.txt> for the full license,
  * including the MIT license.
  */
 package org.spout.droplet.ask.command;
 
 import java.util.List;
 
 import org.spout.droplet.ask.DropletAsk;
 
 import org.spout.api.Server;
 import org.spout.api.Spout;
 import org.spout.api.chat.ChatArguments;
 import org.spout.api.chat.style.ChatStyle;
 import org.spout.api.command.CommandContext;
 import org.spout.api.command.CommandSource;
 import org.spout.api.command.annotated.Command;
 import org.spout.api.command.annotated.CommandPermissions;
 import org.spout.api.exception.CommandException;
 import org.spout.api.plugin.Platform;
 
 public class DropletCommands {
 	private final DropletAsk plugin = DropletAsk.getInstance();
 
 	@Command(aliases = {"question", "q", "ask"}, usage = "<question>", desc = "Submit a question to the staff.")
 	@CommandPermissions("dropletask.command.question")
 	public void question(CommandContext args, CommandSource source) throws CommandException {
 		ChatArguments message = new ChatArguments(ChatStyle.BRIGHT_GREEN, "Question submitted by ", ChatStyle.BLUE, source.getName(), ChatStyle.BRIGHT_GREEN, ": ", ChatStyle.BLUE, args.getJoinedString(0));
 		plugin.getQuestionList().add(message);
 		source.sendMessage(ChatStyle.BRIGHT_GREEN, "Your question has been submitted for response.");
 	}
 
 	@Command(aliases = "nextq", desc = "Print the next question to receivers.", min = 0, max = 0)
 	@CommandPermissions("dropletask.command.nextq")
 	public void nextQuestion(CommandContext args, CommandSource source) throws CommandException {
 		Platform p = Spout.getPlatform();
 		if (p != Platform.SERVER && p != Platform.PROXY) {
 			throw new CommandException("You may only read questions in server mode.");
 		}
 		((Server) Spout.getEngine()).broadcastMessage(plugin.getQuestionList().next());
 	}
 
 	@Command(aliases = "listq", desc = "List submitted questions.", min = 0, max = 1)
 	@CommandPermissions("dropletask.command.listq")
 	public void listQuestions(CommandContext args, CommandSource source) throws CommandException {
 		List<ChatArguments> questions = plugin.getQuestionList().get();
 		if (questions.size() == 0) {
 			throw new CommandException("There are no questions to answer!");
 		}
 		int amount = questions.size();
 		if (args.length() > 0) {
 			int i = args.getInteger(0);
 			if (i <= amount) {
 				amount = i;
 			}
 		}
 		for (int i = 0; i < amount; i++) {
 			source.sendMessage(ChatStyle.BLUE, i + 1, ". ", questions.get(i));
 		}
 	}
 
 	@Command(aliases = "showq", usage = "<#>", desc = "Shows specified question.", min = 1, max = 1)
 	@CommandPermissions("dropletask.command.showq")
 	public void showQuestion(CommandContext args, CommandSource source) throws CommandException {
 		int i = args.getInteger(0) - 1;
 		List<ChatArguments> questions = plugin.getQuestionList().get();
 		if (i > questions.size() - 1 || i < 0) {
 			throw new CommandException("Index out of bounds. List size: " + questions.size());
 		}
 		source.sendMessage(questions.get(i));
 	}
 
 	@Command(aliases = {"deleteq", "delq"}, usage = "<#>", desc = "Delete specified question.", min = 1, max = 1)
 	@CommandPermissions("dropletask.command.deleteq")
 	public void deleteQuestion(CommandContext args, CommandSource source) throws CommandException {
 		int i = args.getInteger(0) - 1;
		System.out.println("Size: " + plugin.getQuestionList().get().size());
		source.sendMessage(ChatStyle.BRIGHT_GREEN, "Removed question '", ChatStyle.BLUE, i + 1, ". ", plugin.getQuestionList().get().get(i), ChatStyle.BRIGHT_GREEN, "'");
 		plugin.getQuestionList().remove(i);
 	}
 }
