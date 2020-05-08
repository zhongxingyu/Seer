 /**
  * Copyright © 2012 Bruce Cowan <bruce@bcowan.me.uk>
  *
  * This file is part of uc_PircBotX.
  *
  * uc_PircBotX is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * uc_PircBotX is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with uc_PircBotX.  If not, see <http://www.gnu.org/licenses/>.
  */
 package uk.co.unitycoders.pircbotx.commands;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.pircbotx.PircBotX;
 import org.pircbotx.hooks.ListenerAdapter;
 import org.pircbotx.hooks.events.PrivateMessageEvent;
 
 /**
  * This says messages given to the bot in a private message.
  *
  * @author Bruce Cowan
  */
 public class SayCommand extends ListenerAdapter<PircBotX>
 {
 	private Pattern re;
 
 	public SayCommand()
 	{
 		// Syntax: !say [#&]channel message...
 		this.re = Pattern.compile("^!say (?<channel>[#&][^\\a, ]+) (?<msg>.+)");
 	}
 
 	@Override
 	public void onPrivateMessage(PrivateMessageEvent<PircBotX> event)
 			throws Exception
 	{
 		String msg = event.getMessage();
 
 		if (msg.startsWith("!say"))
 		{
 			Matcher matcher = this.re.matcher(msg);
 
 			if (matcher.matches())
 			{
 				String channel = matcher.group("channel");

				if (!event.getBot().channelExists(channel))
				{
					event.respond("Not in channel " + channel);
					return;
				}

 				String say = matcher.group("msg");
 				event.getBot().sendMessage(channel, say);
 			}
 			else
				event.respond("!say [#&]channel msg...");
 		}
 	}
 }
