 /*******************************************************************************
  * Copyright (c) 2012 James Richardson.
  * 
  * SimpleMessages.java is part of SimpleMessages.
  * 
  * SimpleMessages is free software: you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  * 
  * SimpleMessages is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * SimpleMessages. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package name.richardson.james.bukkit.simplemessages;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import name.richardson.james.bukkit.simplemessages.commands.CommandListener;
 import name.richardson.james.bukkit.simplemessages.motd.LoginListener;
 import name.richardson.james.bukkit.utilities.plugin.SkeletonPlugin;
 
 public class SimpleMessages extends SkeletonPlugin {
 
   private final Map<String, Message> messages = new HashMap<String, Message>();
 
   public String getArtifactID() {
     return "simple-messages";
   }
 
   public String getGroupID() {
     return "name.richardson.james.bukkit";
   }
 
   public Map<String, Message> getMessages() {
     return Collections.unmodifiableMap(this.messages);
   }
 
   public void loadMessages() {
     final FilenameFilter filter = new MessageFileFilter();
     final File[] files = this.getDataFolder().listFiles(filter);
     for (final File file : files) {
       Message message;
       try {
         message = new Message(file);
         this.messages.put(message.getName(), message);
       } catch (final IOException e) {
         this.logger.warning("Unable to read file!");
       }
     }
     this.logger.info(this.getFormattedMessageCount(this.messages.size()));
   }
 
   private String getFormattedMessageCount(final int count) {
     final Object[] arguments = { count };
     final double[] limits = { 0, 1, 2 };
     final String[] formats = { this.getMessage("no-messages"), this.getMessage("one-message"), this.getMessage("many-messages") };
     return this.getChoiceFormattedMessage("messages-loaded", arguments, formats, limits);
   }
 
   protected void loadConfiguration() throws IOException {
     this.loadMessages();
   }
 
  protected void registerListeners() {
     this.getServer().getPluginManager().registerEvents(new CommandListener(this), this);
     this.getServer().getPluginManager().registerEvents(new LoginListener(this), this);
   }
 
 }
