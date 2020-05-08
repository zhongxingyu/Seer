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
 
 import java.util.Map;
 import org.pircbotx.PircBotX;
 import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
 import org.pircbotx.hooks.events.MessageEvent;
 
 import uk.co.unitycoders.pircbotx.commandprocessor.Command;
 import uk.co.unitycoders.pircbotx.listeners.JoinsListener;
 
 /**
  * Keeps a list of joins, and gives a list of nicks and number of joins.
  *
  * @author Bruce Cowan
  */
public class JoinsCommand extends ListenerAdapter<PircBotX>
 {
 	private Map<User, Integer> joins;
 
 	/**
 	 * Creates a {@link JoinsCommand}.
 	 */
 	public JoinsCommand()
 	{
 		this.joins = JoinsListener.getInstance().getJoins();
 	}
 
 	@Command
	public void onMessage(MessageEvent<PircBotX> event) throws Exception
 	{
 		StringBuilder builder = new StringBuilder();
 
 		for (Map.Entry<User, Integer> entry : this.joins.entrySet())
 		{
 			String nick = entry.getKey().getNick();
 			String value = entry.getValue().toString();
 			builder.append(nick);
 			builder.append(" = ");
 			builder.append(value);
 			builder.append(";");
 		}
 
 		builder.deleteCharAt(builder.length() - 1);
 		event.respond(builder.toString());
 	}
 }
