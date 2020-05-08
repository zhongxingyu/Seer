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
 
 import java.util.Set;
 
 import uk.org.cowgill.james.jircd.Channel;
 import uk.org.cowgill.james.jircd.Client;
 import uk.org.cowgill.james.jircd.Command;
 import uk.org.cowgill.james.jircd.Message;
 import uk.org.cowgill.james.jircd.Permissions;
 import uk.org.cowgill.james.jircd.Server;
 
 /**
  * The LIST command - lists all the channels on the server
  * 
  * @author James
  */
 public class List implements Command
 {
 	@Override
 	public void run(Client client, Message msg)
 	{
 		int privateMembers = 0;
 		Set<Channel> clientChannels = client.getChannels();
 		
 		//All seeing?
 		boolean allSeeing = client.hasPermission(Permissions.seeAllChannels);
 		
 		//Prepare reply
		String reply = Message.newStringFromServer("322") + " " + client.id.nick + " ";
 		
 		//Process channel list
 		for(Channel channel : Server.getServer().getChannels())
 		{
 			//Can we see this channel?
 			if(!allSeeing && (channel.isModeSet('p') || channel.isModeSet('s')) &&
 					!clientChannels.contains(channel))
 			{
 				//Do not show
 				if(channel.isModeSet('p'))
 				{
 					//Add members to private category
 					privateMembers += channel.getMembers().size();
 				}
 				
 				continue;
 			}
 			
 			//Send channel
			client.send(reply + channel.getName() + " " + channel.getMembers().size() +
					" :" + channel.getTopic());
 		}
 		
 		//Send private members
 		if(privateMembers > 0)
 		{
			client.send(reply + "* " + privateMembers + " :");
 		}
 		
 		//Send end of list
 		client.send(client.newNickMessage("323").appendParam("End of /LIST"));
 	}
 
 	@Override
 	public int getMinParameters()
 	{
 		return 0;
 	}
 
 	@Override
 	public String getName()
 	{
 		return "LIST";
 	}
 
 	@Override
 	public int getFlags()
 	{
 		return FLAG_NORMAL;
 	}
 }
