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
 
 
 import com.nijiko.permissions.PermissionHandler;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 class MessageProcessor
 {
 
     public MessageProcessor( ChatAutoComplete cPlugin, ChatAutoCompleteConfig config, PermissionHandler cPermHandler )
     {
         plugin = cPlugin;
         charPrefix = config.getChatPrefix().charAt( 0 );
         maxReplace = config.getMaxReplace();
         // Convert color code to ChatColor
         try
         {
             atSignColor = ChatColor.getByCode( Integer.parseInt( config.getAtSignColor(), 16 ) );
         } catch( NumberFormatException ex )
         {
             atSignColor = null;
         }
         try
         {
             nickColor = ChatColor.getByCode( Integer.parseInt( config.getNickColor(), 16 ) );
         } catch( NumberFormatException ex )
         {
             nickColor = null;
         }
 
         permHandler = cPermHandler;
         spoutListener = plugin.getSpoutListener();
         keepPrefix = config.getKeepPrefix();
         searchType = config.getSearchType();
         if( !searchType.equals( "start" ) && !searchType.equals( "end" ) && !searchType.equals( "contains" ) )
         {
             plugin.consoleMsg( "Invalid searchtype " + searchType + " defaulting to 'start'." );
             searchType = "start";
         }
         ignoreSymbols = config.getIgnoreSymbols();
         plugin.consoleMsg( "Ignored symbols: " + ignoreSymbols, true );
     }
 
     public String[] ProcessMessage( Player sender, String eMsg, String format, Event event )
     {
 
         plugin.consoleMsg( "Cancelled event test", true );
 
 
         plugin.consoleMsg( "Passed test, event not cancelled yet", true );
 
         // Escape if cancelled or doesn't have permissions
         plugin.consoleMsg( "chatEvent", true );
         if( permHandler != null )
         {
             plugin.consoleMsg( "Using PermHandler", true );
             if( !permHandler.has( sender, "autocomp.autocomp" ) ) return new String[]{ eMsg, format };
         } else if( !sender.hasPermission( "autocomp.autocomp" ) ) return new String[]{ eMsg, format };
         plugin.consoleMsg( "Perms OK", true );
         String msg = format;
         boolean useFormat = true;
         if( msg.contains( "%2$s" ) || msg.equals( "{default}" ) )
         {
             plugin.consoleMsg( "using real message", true );
             useFormat = false;
             msg = eMsg;
         }
 
         //Escape if msg doesn't contain the prefix
         if( msg.indexOf( charPrefix ) == -1 ) return new String[]{ eMsg, format };
 
 
         String[] msgSplit = msg.split( "\\s" );
         Map<String, Player> playerMap = new HashMap<String, Player>();
         Map<String, String> nameMap = new HashMap<String, String>();
         int safeLoop = maxReplace;
 
         StringBuilder builder = new StringBuilder();
 
         for( String part : msgSplit )
         {
             if( part.charAt( 0 ) == charPrefix )
             {
                 safeLoop--;
 
                 // cut off the prefix
                 String subName = part.substring( 1 );
                 String extName = subName;
                 plugin.consoleMsg( "Stripping name pre: " + subName, true );
                 subName = stripIgnoredSymbols( subName );
 
                 plugin.consoleMsg( "Stripping name post: " + subName, true );
                 // check cache first
                 if( nameMap.containsKey( subName ) )
                 {
                     if( nameMap.get( subName ) != null ) subName = nameMap.get( subName );
 
                 } else
                 {
                     //check for player
                     Player player = getPlayer( subName );
                     if( player != null )
                     {
                         if( !playerMap.containsKey( player.getName() ) )
                         {
                             playerMap.put( player.getName(), player );
 
                         }
                         nameMap.put( subName, player.getName() );
                         subName = player.getName();
                     } else
                     {
                         nameMap.put( subName, null );
                     }
                 }
                 if( playerMap.containsKey( subName ) || ( nameMap.containsKey( subName ) && nameMap.get( subName ) != null ) )
                 {
                     String prefix = getPrefix( playerMap.get( subName ) );
                     subName = extName.replaceAll( "(?<=([" + ignoreSymbols + "]|^))(\\w+)(.*\\w+)?(?=([" + ignoreSymbols + "]|$))", subName + ChatColor.WHITE );
                     StringBuilder sign = new StringBuilder();
                     if( keepPrefix )
                     {
                         sign.append( ( atSignColor == null ) ? "" : atSignColor )
                             .append( ( char ) charPrefix )
                             .append( ChatColor.WHITE );
                     }
 
                     builder.append( builder.length() == 0 ? "" : " " )
                            .append( sign.toString() )
                            .append( prefix )
                            .append( subName )
                            .append( ChatColor.WHITE );
 
                 } else
                 {
                     if( builder.length() != 0 ) part = " " + part;
                     builder.append( part );
                 }
             } else
             {
                 if( builder.length() != 0 ) part = " " + part;
                 builder.append( part );
             }
             if( safeLoop <= 0 ) break;
 
         }
 
         if( useFormat ) format = builder.toString();
         else eMsg = builder.toString();
 
         if( spoutListener != null ) spoutListener.passEvent( event, playerMap.values() );
         return new String[]{ eMsg, format };
     }
 
     String stripIgnoredSymbols( String input )
     {
         Pattern regex = Pattern.compile( "(?<=([" + ignoreSymbols + "]|^))(\\w+)(.*\\w+)?(?=([" + ignoreSymbols + "]|$))" );
         Matcher m = regex.matcher( input );
         if( m.find() ) return m.group( 0 );
         else return input;
     }
 
 
     Player getPlayer( String subName )
     {
         plugin.consoleMsg( "Searching for: " + subName + " (SearchMode=" + searchType + ")", true );
         if( searchType.equals( "start" ) ) return plugin.getServer().getPlayer( subName );
         else if( searchType.equals( "exact" ) ) return plugin.getServer().getPlayerExact( subName );
         else if( searchType.equals( "end" ) || searchType.equals( "contains" ) )
         {
             for( Player player : plugin.getServer().getOnlinePlayers() )
             {
                 if( searchType.equals( "end" ) && player.getName().toLowerCase().endsWith( subName.toLowerCase() ) )
                     return player;
                 else if( searchType.equals( "contains" ) && player.getName()
                                                                   .toLowerCase()
                                                                   .contains( subName.toLowerCase() ) ) return player;
             }
         }
         return null;
     }
 
     String getPrefix( Player player )
     {
 
         if( permHandler != null ) return permHandler.getUserPrefix( player.getWorld().getName(), player.getName() );
         plugin.consoleMsg( "using Nick Color", true );
         if( nickColor == null ) return "";
         plugin.consoleMsg( "using true Nick Color", true );
         return nickColor.toString();
     }
 
     private final ChatAutoComplete plugin;
     private final int charPrefix;
     private final int maxReplace;
     private ChatColor atSignColor;
     private ChatColor nickColor;
     private final PermissionHandler permHandler;
     private final ChatAutoCompleteSpoutPlayerListener spoutListener;
     private final boolean keepPrefix;
     private String searchType;
     private final String ignoreSymbols;
 
 
 }
