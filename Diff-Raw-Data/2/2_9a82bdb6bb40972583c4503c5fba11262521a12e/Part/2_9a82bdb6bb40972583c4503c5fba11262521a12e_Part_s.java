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
 
 import uk.org.cowgill.james.jircd.Channel;
 import uk.org.cowgill.james.jircd.Client;
 import uk.org.cowgill.james.jircd.Command;
 import uk.org.cowgill.james.jircd.Message;
 import uk.org.cowgill.james.jircd.Server;
 
 /**
  * The PART command - parts a channel
  * 
  * @author James
  */
 public class Part implements Command
 {
 	@Override
 	public void run(Client client, Message msg)
 	{
 		//Parse the part parameters
 		String[] channelStrings = msg.getParam(0).split(",");
 		String partMsg;
 		
 		if(msg.paramCount() >= 2)
 		{
			partMsg = msg.getParam(2);
 		}
 		else
 		{
 			partMsg = client.id.nick;
 		}
 		
 		//Process requests
 		for(int i = 0; i < channelStrings.length; ++i)
 		{
 			//Lookup channel
 			Channel channel = Server.getServer().getChannel(channelStrings[i]);
 			
 			if(channel == null)
 			{
 				client.send(client.newNickMessage("403").appendParam(channelStrings[i]).
 						appendParam("No such channel"));
 				continue;
 			}
 			
 			//Part
 			if(!channel.part(client, partMsg))
 			{
 				client.send(client.newNickMessage("442").appendParam(channelStrings[i]).
 						appendParam("You're not on that channel"));
 				continue;
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
 		return "PART";
 	}
 
 	@Override
 	public int getFlags()
 	{
 		return FLAG_NORMAL;
 	}
 	
 	/**
 	 * The LEAVE command - alias of PART
 	 * 
 	 * @author James
 	 */
 	public static class Leave extends Part
 	{
 		@Override
 		public String getName()
 		{
 			return "LEAVE";
 		}
 	}
 }
