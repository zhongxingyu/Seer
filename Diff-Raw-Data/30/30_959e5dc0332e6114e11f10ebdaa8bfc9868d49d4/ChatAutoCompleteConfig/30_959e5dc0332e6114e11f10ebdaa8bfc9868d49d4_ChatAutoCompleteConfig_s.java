 /*
  *   This file is part of ChatAutoComplete.
  *
  *
  *   ChatAutoComplete is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   ChatAutoComplete is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with ChatAutoComplete. If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package de.neptune_whitebear.ChatAutoComplete;
 
 
 import org.bukkit.Material;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 
 
 import java.io.File;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 
 public class ChatAutoCompleteConfig
 {
 
     public ChatAutoCompleteConfig( ChatAutoComplete instance )
     {
         plugin = instance;
         config = instance.getConfig();
 
      /*  try
         {
             JarFile jar = new JarFile( "plugins/ChatAutoComplete.jar" );
             JarEntry entry = jar.getJarEntry( "config1.yml" );
             if( entry == null )
             {
                 plugin.consoleMsg( "No default config found." );
             }
 
             YamlConfiguration yaml = new YamlConfiguration();
             yaml.load( jar.getInputStream( entry ) );
             config.setDefaults( yaml );
         } catch( Exception ex )
         {
             plugin.consoleMsg( "ERROR; Unable to load default config file. =>" );
             plugin.consoleMsg( ex.toString() );
         }
 
         config.options().copyDefaults( true );
        config.options().copyHeader( true );   */
 
         saveConfig();
 
     }
 
 
     private void saveConfig()
     {
         plugin.saveConfig();
     }
     void setDefault( FileConfiguration def, String name, Object value )
     {
         if( def.get( name ) == null ) def.set( name, value );
     }
 
     public String getChatPrefix()
     {
         return config.getString( "general.chatPrefix" );
     }
 
     public int getMaxReplace()
     {
         return config.getInt( "general.maxReplace" );
     }
 
     public String getAtSignColor()
     {
         return config.getString( "colors.atSignColor" );
     }
 
     public boolean getUseEssentials()
     {
         return config.getBoolean( "colors.useEssentials" );
     }
 
     public boolean getUseSpout()
     {
         return config.getBoolean( "spout.useSpout" );
     }
 
     public String getSpoutSound()
     {
         return config.getString( "spout.spoutSound" );
     }
 
     public boolean getUseNotification()
     {
         return config.getBoolean( "spout.useNotification" );
     }
 
     public boolean getDebug()
     {
         return config.getBoolean( "dev.debug" );
     }
 
     public String getSpoutNotificationTitle()
     {
         return config.getString( "spout.spoutNotificationTitle" );
     }
 
     public String getSpoutNotificationMessage()
     {
         return config.getString( "spout.spoutNotificationMessage" );
     }
 
     public String getSpoutNotificationMaterial()
     {
         return config.getString( "spout.spoutNotificationMaterial" );
     }
 
     public String getNickColor()
     {
         return config.getString( "colors.nickColor" );
     }
 
     public boolean getKeepPrefix()
     {
         return config.getBoolean( "general.keepPrefix" );
     }
 
     public String getIgnoreSymbols()
     {
         return config.getString( "general.ignoreSymbols" );
     }
 
     public String getSearchType()
     {
         return config.getString( "general.searchType" );
     }
 
     private final FileConfiguration config;
     private final ChatAutoComplete plugin;
 
 }
