 /*******************************************************************************
  * Copyright (c) 2011 James Richardson.
  * 
  * RotatingMessage.java is part of TimedMessages.
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
 
 package name.richardson.james.bukkit.timedmessages.rotation;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.bukkit.Server;
 
 import name.richardson.james.bukkit.timedmessages.Message;
 
 public class RotatingMessage extends Message {
 
   private Iterator<String> iterator;
 
  public RotatingMessage(Server server, Long time, List<String> messages, String permission) {
    super(server, time, messages, permission);
     refreshIterator();
   }
 
   private void refreshIterator() {
     iterator = messages.iterator();
   }
 
   @Override
   protected String getNextMessage() {
     if (!iterator.hasNext()) refreshIterator();
     return iterator.next().toString();
   }
 
 }
