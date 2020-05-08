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
 package uk.co.unitycoders.pircbotx;
 
 import org.pircbotx.PircBotX;
 import org.pircbotx.hooks.managers.ListenerManager;
 
 import uk.co.unitycoders.pircbotx.commandprocessor.CommandListener;
 import uk.co.unitycoders.pircbotx.commandprocessor.CommandProcessor;
 import uk.co.unitycoders.pircbotx.commands.*;
 import uk.co.unitycoders.pircbotx.listeners.LinesListener;
 
 /**
  * The actual bot itself.
  *
  * @author Bruce Cowan
  */
 public class Bot
 {
 	public static void main(String[] args) throws Exception
 	{
 		CommandProcessor processor = new CommandProcessor('&');
 		processor.register("rand", new RandCommand());
 		processor.register("time", new DateTimeCommand());
 		processor.register("date", new DateTimeCommand());
                 processor.register("lart", new LartCommand());
 
 		PircBotX bot = new PircBotX();
 		ListenerManager<? extends PircBotX> manager = bot.getListenerManager();
 
 		manager.addListener(new CommandListener(processor));
 		manager.addListener(new JoinsCommand());
 		manager.addListener(new KillerTroutCommand());
 		manager.addListener(new LinesListener());
 		manager.addListener(new SayCommand());
 		manager.addListener(new CalcCommand());
 
 		// Snapshot (1.8-SNAPSHOT) only
 		bot.setAutoReconnect(true);
 		bot.setAutoReconnectChannels(true);
 
 		try
 		{
 			bot.setName("uc_pircbotx");
 			bot.connect("irc.freenode.net");
                        bot.joinChannel("#unity-coders");
 			bot.setVerbose(true);
 		} catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 }
