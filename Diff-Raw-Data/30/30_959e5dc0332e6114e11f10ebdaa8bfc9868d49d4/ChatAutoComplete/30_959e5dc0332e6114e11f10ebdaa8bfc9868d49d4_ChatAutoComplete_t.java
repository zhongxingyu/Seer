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
 
 import java.io.File;
 import java.util.logging.Logger;
 
 
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.nijikokun.bukkit.Permissions.Permissions;
 import com.nijiko.permissions.PermissionHandler;
 
 public class ChatAutoComplete extends JavaPlugin
 {
 
 
     public void onDisable()
     {
         consoleMsg( "Disabled." );
 
     }
 
     public void onEnable()
     {
 
         PluginDescriptionFile pluginDesc = getDescription();
         prefix = "[" + pluginDesc.getName() + " (" + pluginDesc.getVersion() + ")] ";
         mcLogger = Logger.getLogger( "Minecraft" );
         config = new ChatAutoCompleteConfig( this );
 
 
         if( config.getDebug() )
         {
             debug = true;
             consoleMsg( "Using Debug Mode" );
         }
 
 
         if( config.getMaxReplace() <= 0 )
         {
             consoleMsg( "Can't replace // Max Replace Amount is set to 0 or lower." );
             super.getPluginLoader().disablePlugin( this );
             return;
         }
 
         PluginManager pgnMng = this.getServer().getPluginManager();
 
         if( config.getUseEssentials() )
         {
             Plugin essentialsBridge = pgnMng.getPlugin( "Permissions" );
             if( essentialsBridge != null && essentialsBridge.isEnabled() )
             {
                if( essentialsBridge instanceof PermissionHandler){
                 try
                 {
                     permHandler = ( ( Permissions ) essentialsBridge ).getHandler();
                     consoleMsg( "Using Essentials/PermissionsPrefix" );
                 } catch( NoClassDefFoundError exception )
                 {
                     permHandler = null;
                 }
                }
             }
         }
 
         MessageProcessor messageProcessor = new MessageProcessor( this, config, permHandler );
 
         if( pgnMng.getPlugin( "HeroChat" ) != null )
         {
             consoleMsg( "Using HeroChat: " + pgnMng.getPlugin( "HeroChat" ).getDescription().getFullName() );
             HeroChatListener heroChatListener = new HeroChatListener( this, messageProcessor );
             pgnMng.registerEvent( Type.CUSTOM_EVENT, heroChatListener, Priority.Normal, this );
 
 
         }
 
 
         useSpout = config.getUseSpout();
 
         if( config.getUseSpout() && useSpout && pgnMng.getPlugin( "Spout" ) != null )
         {
             enableSpout();
 
         }
 
         ChatAutoCompletePlayerListener playerListener = new ChatAutoCompletePlayerListener( this, messageProcessor );
 
        pgnMng.registerEvent( Type.PLAYER_CHAT, playerListener, Priority.Low, this );
 
 
         consoleMsg( "Enabled." );
     }
 
 
     void consoleMsg( String msg )
     {
         consoleMsg( msg, false );
 
 
     }
 
     void enableSpout()
     {
         useSpout = true;
         consoleMsg( "Using Spout: " + this.getServer()
                                           .getPluginManager()
                                           .getPlugin( "Spout" )
                                           .getDescription()
                                           .getFullName() );
         spoutListener = new ChatAutoCompleteSpoutPlayerListener( this, useSpout, this.getConfigInstance() );
         this.getServer().getPluginManager().registerEvent( Type.PLAYER_CHAT, spoutListener, Priority.Monitor, this );
 
     }
 
     public void consoleMsg( String msg, boolean ifDebug )
     {
         if( ( ifDebug && debug ) || !ifDebug ) mcLogger.info( prefix + msg );
     }
 
     ChatAutoCompleteConfig getConfigInstance()
     {
         return config;
     }
 
     public ChatAutoCompleteSpoutPlayerListener getSpoutListener()
     {
         if( !useSpout ) return null;
         else return spoutListener;
     }
 
     private String prefix = "";
     private Logger mcLogger;
     private boolean useSpout = false;
     private boolean debug = false;
 
     private ChatAutoCompleteConfig config;
     private ChatAutoCompleteSpoutPlayerListener spoutListener;
 
     private PermissionHandler permHandler = null;
 
 
 }
