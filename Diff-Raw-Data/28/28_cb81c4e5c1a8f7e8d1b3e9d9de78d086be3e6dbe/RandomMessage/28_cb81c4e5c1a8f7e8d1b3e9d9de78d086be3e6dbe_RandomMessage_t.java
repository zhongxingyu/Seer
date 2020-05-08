 /*******************************************************************************
  * Copyright (c) 2011 James Richardson.
  * 
  * RandomMessage.java is part of TimedMessages.
  * 
  * TimedMessages is free software: you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  * 
  * TimedMessages is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * TimedMessages. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 
 package name.richardson.james.bukkit.timedmessages.random;
 
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.Server;
 
 import name.richardson.james.bukkit.timedmessages.Message;
 
 public class RandomMessage extends Message {
 
  public RandomMessage(Server server, Long milliseconds, List<String> messages, String permission, String worldName) {
    super(server, milliseconds, messages, permission, worldName);
   }
 
   @Override
   protected String getNextMessage() {
     final int i = new Random().nextInt(messages.size());
     return messages.get(i);
   }
 
 }
