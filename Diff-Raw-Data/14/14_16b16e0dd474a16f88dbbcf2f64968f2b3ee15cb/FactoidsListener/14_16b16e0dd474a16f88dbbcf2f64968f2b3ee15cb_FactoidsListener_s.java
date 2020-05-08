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
 package com.alta189.cyborg.factoids;
 
 import com.alta189.cyborg.Cyborg;
 import com.alta189.cyborg.api.event.EventHandler;
 import com.alta189.cyborg.api.event.Listener;
 import com.alta189.cyborg.api.event.Order;
 import com.alta189.cyborg.api.event.bot.PrivateMessageEvent;
 import com.alta189.cyborg.api.event.channel.MessageEvent;
 import com.alta189.cyborg.factoids.handlers.Handler;
 import org.pircbotx.User;
 
 import static com.alta189.cyborg.factoids.FactoidManager.getArgs;
 import static com.alta189.cyborg.factoids.FactoidManager.getConfig;
 import static com.alta189.cyborg.factoids.FactoidManager.getDatabase;
 import static com.alta189.cyborg.factoids.FactoidManager.getFactoidFromRaw;
 import static com.alta189.cyborg.factoids.FactoidManager.getHandler;
 import static com.alta189.cyborg.factoids.FactoidManager.getPrefix;
 import static com.alta189.cyborg.factoids.FactoidManager.violatesFilter;
 
 public class FactoidsListener implements Listener {
	private static final String lineBreak = "line.separator";
 	@EventHandler(order = Order.LATEST)
 	public void onMessage(MessageEvent event) {
 		String command = event.getMessage();
 
 		String prefix = getPrefix(command);
 		if (prefix == null || prefix.isEmpty()) {
 			return;
 		}
 
 		String factoidName = getFactoidFromRaw(command);
 		if (factoidName == null || factoidName.isEmpty()) {
 			return;
 		}
 
 		Factoid factoid = null;
 
 		if (prefix.equals("!")) {
 			factoid = getDatabase().select(Factoid.class).where().equal("name", factoidName).and().equal("forgotten", false).and().equal("location", "global").and().equal("locked", true).execute().findOne();
 			if (factoid == null) {
 				factoid = getDatabase().select(Factoid.class).where().equal("name", factoidName).and().equal("forgotten", false).and().equal("location", "global").execute().findOne();
 				if (factoid == null) {
 					return;
 				}
 			}
 		} else {
 			factoid = getDatabase().select(Factoid.class).where().equal("name", factoidName).and().equal("forgotten", false).and().equal("location", "global").and().equal("locked", true).execute().findOne();
 			if (factoid == null) {
 				factoid = getDatabase().select(Factoid.class).where().equal("name", factoidName).and().equal("forgotten", false).and().equal("location", event.getChannel().getName().toLowerCase()).execute().findOne();
 				if (factoid == null) {
 					factoid = getDatabase().select(Factoid.class).where().equal("name", factoidName).and().equal("forgotten", false).and().equal("location", "global").execute().findOne();
 					if (factoid == null) {
 						return;
 					}
 				}
 			}
 		}
 
 		Handler handler = getHandler(factoid.getHandler());
 		if (handler == null) {
 			return;
 		}
 
 		String data = null;
 		Handle handle = null;
 
 		int ping = command.lastIndexOf("|");
 		int notice = command.lastIndexOf(">");
 
 		if (!(ping <= 0)) {
 			if (ping > notice) {
 				data = command.substring(ping + 1);
 				if (data != null && !data.isEmpty()) {
 					data = data.replaceAll(" ", ""); // Delete spaces
 					for (User user : event.getChannel().getUsers()) {
 						if (user.getNick().equalsIgnoreCase(data)) { // Only if data is a user
 							command = command.substring(0, ping);
 							handle = Handle.PING;
 							break;
 						}
 					}
 				}
 			}
 		} else if (!(notice <= 0)) {
 			data = command.substring(notice + 1);
 			if (data != null && !data.isEmpty()) {
 				data = data.replaceAll(" ", ""); // Delete spaces
 				for (User user : event.getChannel().getUsers()) {
 					if (user.getNick().equalsIgnoreCase(data)) { // Only if data is a user
 						command = command.substring(0, notice);
 						handle = Handle.NOTICE;
 						break;
 					}
 				}
 			}
 		}
 
 		FactoidContext context = new FactoidContext(event.getChannel(), event.getUser(), getArgs(command));
 		FactoidResult result = handler.handle(factoid, context);
 
 		if (result == null) {
 			return;
 		}
 
 		if (violatesFilter(result.getBody())) {
 			String report = getConfig().getString("filter-report-channel");
 			if (report != null && !report.isEmpty()) {
 				if (!report.startsWith("#")) {
 					report = "#" + report;
 				}
 				StringBuilder builder = new StringBuilder();
 				builder.append("'").append(event.getUser().getNick()).append("' said the following in '").append(event.getChannel().getName()).append("': ")
 						.append(lineBreak)
						.append(event.getMessage())
 						.append(lineBreak)
 						.append("Which returned the following and violates a filter: ")
 						.append(lineBreak)
						.append(result.getBody());
 				Cyborg.getInstance().sendMessage(report, builder.toString());
 			}
 		}
 
 		if (handle == null || result.getReturnType() != ReturnType.MESSAGE || result.isForced()) {
 			switch (result.getReturnType()) {
 				case ACTION:
 					Cyborg.getInstance().sendAction(result.getTarget(), result.getBody());
 					break;
 				case MESSAGE:
 					Cyborg.getInstance().sendMessage(result.getTarget(), result.getBody());
 					break;
 				case NOTICE:
 					Cyborg.getInstance().sendNotice(result.getTarget(), result.getBody());
 					break;
 			}
 		} else {
 			switch (handle) {
 				case PING:
 					Cyborg.getInstance().sendMessage(event.getChannel(), data + ": " + result.getBody());
 					break;
 				case NOTICE:
 					Cyborg.getInstance().sendNotice(data, result.getBody());
 			}
 		}
 	}
 
 	@EventHandler(order = Order.EARLIEST)
 	public void onPrivateMessage(PrivateMessageEvent event) {
 		String prefix = getPrefix(event.getMessage());
 		if (prefix == null || prefix.isEmpty()) {
 			return;
 		}
 
 		String factoidName = getFactoidFromRaw(event.getMessage());
 		if (factoidName == null || factoidName.isEmpty()) {
 			return;
 		}
 
 		Factoid factoid = getDatabase().select(Factoid.class).where().equal("name", factoidName).and().equal("forgotten", false).and().equal("location", "GLOBAL").execute().findOne();
 		if (factoid == null) {
 			return;
 		}
 
 		Handler handler = getHandler(factoid.getHandler());
 		if (handler == null) {
 			return;
 		}
 
 		FactoidContext context = new FactoidContext(event.getUser(), getArgs(event.getMessage()));
 		FactoidResult result = handler.handle(factoid, context);
 
 		if (result == null) {
 			return;
 		}
 
 		if (violatesFilter(result.getBody())) {
 			String report = getConfig().getString("filter-report-channel");
 			if (report != null && !report.isEmpty()) {
 				if (!report.startsWith("#")) {
 					report = "#" + report;
 				}
 				StringBuilder builder = new StringBuilder();
 				builder.append("'").append(event.getUser().getNick()).append("' said the following in a PM: ")
						.append(event.getMessage())
 						.append(lineBreak)
 						.append("Which returned the following and violates a filter: ")
 						.append(lineBreak)
						.append(result.getBody());
 				Cyborg.getInstance().sendMessage(report, builder.toString());
 			}
 		}
 
 		switch (result.getReturnType()) {
 			case ACTION:
 				Cyborg.getInstance().sendAction(result.getTarget(), result.getBody());
 				break;
 			case MESSAGE:
 				Cyborg.getInstance().sendMessage(result.getTarget(), result.getBody());
 				break;
 			case NOTICE:
 				Cyborg.getInstance().sendNotice(result.getTarget(), result.getBody());
 				break;
 		}
 	}
 
 	public enum Handle {
 		PING,
 		NOTICE
 	}
 }
