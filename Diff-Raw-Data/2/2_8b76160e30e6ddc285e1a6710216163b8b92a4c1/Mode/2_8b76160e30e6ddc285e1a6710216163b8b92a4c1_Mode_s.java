 /*
    Copyright 2011 James Cowgill
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
 package uk.org.cowgill.james.jircd.commands;
 
 import java.util.Map;
 import java.util.Map.Entry;
 
 import uk.org.cowgill.james.jircd.Channel;
 import uk.org.cowgill.james.jircd.Client;
 import uk.org.cowgill.james.jircd.Command;
 import uk.org.cowgill.james.jircd.Message;
 import uk.org.cowgill.james.jircd.Permissions;
 import uk.org.cowgill.james.jircd.Server;
 import uk.org.cowgill.james.jircd.util.ChannelCheckError;
 import uk.org.cowgill.james.jircd.util.ChannelChecks;
 import uk.org.cowgill.james.jircd.util.ModeUtils;
 import uk.org.cowgill.james.jircd.util.ModesParser;
 
 /**
  * The MODE command - views and changes a channel or user mode
  * 
  * @author James
  */
 public class Mode implements Command
 {
 	@Override
 	public void run(Client client, Message msg)
 	{
 		//Hand off to correct method
 		if(msg.getParam(0).charAt(0) == '#')
 		{
 			processChannelModes(client, msg);
 		}
 		else
 		{
 			processUserModes(client, msg);
 		}
 	}
 	
 	private static void processChannelModes(Client client, Message msg)
 	{
 		String item = msg.getParam(0);
 		
 		//Use channel parser
 		ModesParser parser = new ModesParser(Server.getServer().getISupport().modesChannel);
 		
 		//Lookup channel
 		Channel channel = Server.getServer().getChannel(item);
 		
 		//Exists?
 		if(channel == null)
 		{
 			client.send(client.newNickMessage("403").appendParam(item).appendParam("No such channel"));
 			return;
 		}
 		
 		//Parse channel modes
 		parser.parse(msg.getParamList().subList(1, msg.paramCount()));
 		
 		//Commit stuff
 		if(parser.printMode)
 		{
 			channel.sendMode(client);
 		}
 		else if(parser.toList != null)
 		{
 			//List modes of channel	
 			// Get sets and information about each list type
 			Map<String, Channel.SetInfo> list;
 			String entryRpl;
 			Message endRpl;
 			
 			switch(parser.toList)
 			{
 			case 'b':
 				list = channel.getBanList();
 				entryRpl = "367";
 				endRpl = client.newNickMessage("368").appendParam(channel.getName()).
 						appendParam("End of Channel Ban List");
 				
 				break;
 				
 			case 'e':
 				list = channel.getBanExceptList();
 				entryRpl = "348";
 				endRpl = client.newNickMessage("349").appendParam(channel.getName()).
 						appendParam("End of Channel Exception List");
 				
 				break;
 				
 			case 'I':
 				list = channel.getInviteExceptList();
 				entryRpl = "346";
 				endRpl = client.newNickMessage("347").appendParam(channel.getName()).
 						appendParam("End of Channel Invite List");
 				
 				break;
 				
 			default:
 				throw new IllegalStateException("Channel list value is not beI");
 			}
 			
 			//Display list
 			for(Entry<String, Channel.SetInfo> entry : list.entrySet())
 			{
 				//Get time
 				String time = Long.toString(entry.getValue().getTime() / 1000);
 				
 				//Send message
 				client.send(client.newNickMessage(entryRpl).
 						appendParam(channel.getName()).					//Channel name	
 						appendParam(entry.getKey()).					//Ban mask
 						appendParam(entry.getValue().getNick()).		//Nick who set the ban
 						appendParam(time));								//Time the ban was set
 			}
 			
 			//Display end of list
 			client.send(endRpl);
 		}
 		else
 		{
 			//Setting channel modes
 			for(ModesParser.ChangeInfo change : parser.toChange)
 			{					
 				//Allow unsetting of own modes
 				if(!change.add && change.param != null && 
 						change.param.equalsIgnoreCase(client.id.nick) &&
 						(change.flag == 'v' || change.flag == 'h' || change.flag == 'o' ||
 						change.flag == 'a' || change.flag == 'q'))
 				{
 					//Unset own mode
 					channel.setMode(client, false, change.flag, client);
 				}
 				else
 				{
 					//Can set?
 					ChannelCheckError error = ChannelChecks.canSetMode(channel, client, change.add, change.flag);
 					
 					if(error == ChannelCheckError.OK)
 					{
 						//Set mode
 						channel.setMode(client, change.add, change.flag, change.param);
 					}
 					else
 					{
 						//Send error
 						error.sendToClient(channel, client);
 					}
 				}
 			}
 		}
 	}
 	
 	private static void processUserModes(Client client, Message msg)
 	{
 		String item = msg.getParam(0);
 		
 		//Use user parser
 		ModesParser parser = new ModesParser(Server.getServer().getISupport().modesUser);
 		
 		//Lookup client
 		Client other = Server.getServer().getClient(item);
 		
 		//Exists?
 		if(other == null)
 		{
 			client.send(client.newNickMessage("401").appendParam(item).appendParam("No such nick / channel"));
 			return;
 		}
 		
 		//Parse user modes
 		parser.parse(msg.getParamList().subList(1, msg.paramCount()));
 
 		//toList should always be null
 		assert parser.toList == null;
 
 		//Commit stuff
 		if(parser.printMode)
 		{
			//Show if it's outselves or we're a hacker
 			if(client == other || client.hasPermission(Permissions.userModeHack))
 			{
 				//Display mode
 				client.send(client.newNickMessage("221").appendParam(ModeUtils.toString(other.getMode())));
 			}
 		}
 		else
 		{
 			//Setting user modes
 			// Deny affecting other opers
 			if(client != other)
 			{
 				if(client.hasPermission(Permissions.userModeHack))
 				{
 					//Do not affect other opers unless we're a super op
 					if(!client.isModeSet('O') && (other.isModeSet('o') || other.isModeSet('O')))
 					{
 						return;
 					}
 				}
 				else
 				{
 					//Ignore
 					return;
 				}
 			}
 			
 			//Process modes
 			for(ModesParser.ChangeInfo change : parser.toChange)
 			{
 				//Do not grant opers or allow setting bot flag
 				if(change.add && (change.flag == 'o' || change.flag == 'O' || change.flag == 'B'))
 				{
 					continue;
 				}
 				
 				other.setMode(change.flag, change.add);
 			}
 		}
 	}
 
 	@Override
 	public int getMinParameters()
 	{
 		return 1;
 	}
 
 	@Override
 	public String getName()
 	{
 		return "MODE";
 	}
 
 	@Override
 	public int getFlags()
 	{
 		return FLAG_NORMAL;
 	}
 }
