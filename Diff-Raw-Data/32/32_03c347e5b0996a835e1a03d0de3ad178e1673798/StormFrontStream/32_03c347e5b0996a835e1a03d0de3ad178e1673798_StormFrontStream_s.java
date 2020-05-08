 /**
  * Warlock, the open-source cross-platform game client
  *  
  * Copyright 2008, Warlock LLC, and individual contributors as indicated
  * by the @authors tag. 
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package cc.warlock.core.stormfront.client.internal;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import cc.warlock.core.client.IWarlockClient;
 import cc.warlock.core.client.WarlockString;
 import cc.warlock.core.client.internal.Stream;
 import cc.warlock.core.client.settings.IIgnore;
 import cc.warlock.core.stormfront.client.IStormFrontClient;
 
 public class StormFrontStream extends Stream {
 	
 	protected StormFrontStream (IStormFrontClient client, String streamName)
 	{
 		super(client, streamName);
 	}
 	
 	protected static Stream fromNameAndClient (IStormFrontClient client, String name)
 	{
 		if (streams.containsKey(name))
 			return streams.get(name);
 		
 		else {
 			StormFrontStream stream = new StormFrontStream(client, name);
 			if (name.contains(IWarlockClient.DEFAULT_STREAM_NAME)) {
 				stream.setLogging(true);
 			}
 			return stream;
 		}
 	}
 	
 	@Override
 	public void send(WarlockString text) {
 		if (client != null)
 		{
 			// Ignore text containing ignores
 			for (IIgnore ignore : client.getClientSettings().getAllIgnores())
			{
				Pattern ignoreRegex = ignore.getPattern();
 				int pos = 0;
 				for(;;)
 				{
 					String str = text.toString();
 					
 					Matcher m = ignoreRegex.matcher(str);
 					if(!m.find(pos))
 						break;
 					
 					// remove text from beginning of line of match to end, find those points
 					int start = str.substring(0, m.start()).lastIndexOf('\n');
 					int end = str.indexOf('\n', m.end() - 1);
 					
 					// start index becomes index after the match once it's removed
 					pos = start;
 					
 					// we are the first line and don't have anything to remove there
 					if(start < 0) {
 						// we only have one line, don't show anything
 						if(end < 0 || end + 1 >= str.length())
 							return;
 						
 						text = text.substring(end + 1);
 					} else {
 						WarlockString newText = text.substring(0, start + 1);
 						
 						// if we have text after the line after the ignore, add it
 						if(end >= 0 && end + 1 < str.length())
 							newText.append(text.substring(end + 1));
 
 						text = newText;
 					}
 				}
 			}
 		}
 		
 		super.send(text);
 	}
 }
