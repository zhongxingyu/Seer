 package com.pie.jotta.event;
 
 /*
  *  This file is part of Jotta.
  *
  *  Jotta is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  Jotta is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Jotta.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 import java.lang.reflect.Method;
 
 import com.pie.jotta.Constants;
 import com.pie.jotta.util.IgnoreManager;
 import com.pie.jotta.util.command.*;
 import com.pie.jotta.util.logger.Logger;
 
 import static com.pie.jotta.net.IRCMethods.sendMessage;
 
 public class PrivMessageListener implements MessageListener {
 
 
 	public PrivMessageListener build() {
 		//cmd.add(new Lastfm());
 		//cmd.add(new Nslookup());
 		return this;
 	}
 	
 	public void recieve(IRCMessage message) {
 		if(message.getCommand().equals("PRIVMSG")) {
 			if(message.getMessage().startsWith("ACTION")) {	}
 			else {
 				Logger.getInstance().log("<"+message.getSender()+"> ("+message.getSource()+") "+message.getMessage());
 			}
 			
 			if(message.getCmd().equals(Constants.CMD_PREFIX+"reload")) {
 				if(message.getMessageArgs().get(0).equalsIgnoreCase("all")) {
 					sendMessage(message.getSource(), "Successfully reloaded "+CommandLoader.loadClasses()+" classes.");
 				} else {
 					CommandLoader.reloadClass(message.getMessageArgs().get(0));
 					sendMessage(message.getSource(), "Successfully reloaded "+message.getMessageArgs().get(0)+" module.");
 				}
 			} else if(message.getCmd().equals(Constants.CMD_PREFIX+"help")) {
				if(message.getMessageArgs().size() > 0) {
					if(CommandLoader.list().containsKey(Constants.CMD_PREFIX+message.getMessageArgs().get(0))) {
						try {
							Class<?> c = CommandLoader.command(Constants.CMD_PREFIX+message.getMessageArgs().get(0));
							Object obj = c.newInstance();
							Method m = c.getMethod("help", message.getClass());
							m.invoke(obj, new Object[]{message});
						} catch(Exception e) {
							e.printStackTrace();
						}
 					}
				} else {
					String set = CommandLoader.list().keySet().toString();
					sendMessage(message.getSource(), "Commands: "+set.substring(1,set.length()-1));
 				}
 			} else if((message.getMessage().charAt(0) == Constants.CMD_PREFIX) && !IgnoreManager.isIgnored(message.getSender())) {
 				if(CommandLoader.list().containsKey(message.getCmd())) {
 					try {
 						Class<?> c = CommandLoader.command(message.getCmd());
 						Object obj = c.newInstance();
 						Method m = c.getMethod("parse", message.getClass());
 						m.invoke(obj, new Object[]{message});
 					} catch(Exception e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 	}
 	
 }
