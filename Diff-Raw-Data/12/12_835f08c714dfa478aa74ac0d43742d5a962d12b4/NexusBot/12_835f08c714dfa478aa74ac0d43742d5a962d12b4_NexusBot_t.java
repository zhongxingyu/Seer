 /*
  * :mode=java:tabSize=4:indentSize=4:noTabs=true:
  * :folding=indent:collapseFolds=0:wrap=none:maxLineLen=120:
  *
  * $Source: /var/lib/cvsd/cvsrepo/invasion/nw/nexusbot/src/nexusbot/NexusBot.java,v $
  * Copyright (C) 2007 Jeffrey Hoyt
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package nexusbot;
 
 import org.jibble.pircbot.*;
 
 import java.io.*;
 
 import java.net.*;
 import java.text.*;
 import java.util.*;
 
 
 /**
  * This is the main class for yocsbot.  It processes all commands and provides for all interactions with IRC channels.
  *
  * @author jchoyt
   */
 public class NexusBot extends PircBot
 {
     /**
      * The line separator for the OS this is running on.  While everyone should be running Linux or MacOS, I recognize
      * some are unfortunate enough to run Windows.
      */
     public final static String LINESEP = System.getProperty( "line.separator" );
 
     public List<String> channels = new ArrayList<String>();
 
     private Timer timer = new Timer(true);
 
     private List<BotCommandHandler> doers = new ArrayList<BotCommandHandler>();
 
     private Config config = null;
 
     public ChannelCommands channelCommands;
 
     private static List<String> owners = null;
 
     private List<String> ignoreList = null;
 
     /**
      * Creates a new NexusBot object.  These hardcoded instances should be changed if you reuse this code.  Preferably, they
      * should be moved to an external text file.
      */
     public NexusBot( String propsFile )
     {
         System.out.println( "starting" );
         config = new Config(propsFile);
 
         //set bot info
         this.setName( config.getValue("name") );
         owners = Arrays.asList( config.getValue("owner").split(",") );
         this.setLogin( owners.get(0) );
 
         reload();
 
         //set up handlers
         channelCommands = new ChannelCommands();
         doers.add(channelCommands);
         doers.add(new YocsCommands());
         doers.add(new MessageCommands());
         doers.add(new RaidCommands());
        //doers.add(new ShCommands(this));
 
         join();
 
     }
 
     /**
      *  Commands that work only in a private message (/msg yocsbot <command>)  These commands are mostly for management of the bot.
      */
     protected  void  onPrivateMessage(String sender, String login, String hostname, String message)
     {
         if( ignoreList.contains( sender ) )
         {
             return;
         }
         Info info = new Info( this,  sender,  login,  hostname,  message );
         if ( message.startsWith( "!ignore " ) && owners.contains(info.getSender() ) )
         {
             String nick = message.substring( 8 );
             ignoreList.add( nick );
             Utils.save( ignoreList, "ignore.txt" );
         }
         else if( message.startsWith("!connect ") && owners.contains(info.getSender() ) )
         {
             String[] parts = message.substring( 9 ).split( " " );
             if( parts.length != 3 )
             {
 
             }
         }
         for(BotCommandHandler doer : doers )
         {
             if(doer.handlePrivateMessage( info )) break;
         }
        // onMessage(sender, sender, login, hostname, message);
     }
 
     /**
      * The general commands that work in all channels (including private ones, due to the "else" above.
      *
      * @param channel DOCUMENT ME!
      * @param sender DOCUMENT ME!
      * @param login DOCUMENT ME!
      * @param hostname DOCUMENT ME!
      * @param message DOCUMENT ME!
      */
     protected void onMessage( String channel, String sender, String login, String hostname, String message )
     {
         // System.out.println(ignoreList);
         if( ignoreList.contains( sender ) )
         {
             return;
         }
         if( message.equals("!help" ) )
         {
             sendMessage( channel, "Help for " + getName() + " can be found at " + config.getValue("helpUrl") );
             return;
         }
         Info info = new Info( this,  channel,  sender,  login,  hostname,  message );
         for(BotCommandHandler doer : doers )
         {
             if(doer.handleMessage( info )) break;
         }
     }
 
     protected void onInvite(String targetNick,
                         String sourceNick,
                         String sourceLogin,
                         String sourceHostname,
                         String channel)
     {
         channel = channel.replace("#", "");
         String[] channels = config.getValues("room");
         for(int i = 0; i < channels.length; i++)
         {
             if( channel.equals(channels[i] ) )
             {
                 channelCommands.join(channel, this);
                 break;
             }
         }
     }
 
 
     /**
      *  Clears out the temporary per-channel data
      */
     protected void onJoin(String channel, String sender, String login, String hostname)
     {
         // System. out.println(ignoreList);
         if( ignoreList.contains( sender ) )
         {
             return;
         }
         // System.out.println( "in onJoin()" );
         Info info = new Info( this,  channel,  sender,  login,  hostname,  "" );
         for(BotCommandHandler doer : doers )
         {
             doer.handleJoin(info);
         }
     }
 
     protected void onNickChange(String oldNick, String login, String hostname, String newNick)
     {
         if( ignoreList.contains( oldNick ) || ignoreList.contains( newNick ) )
         {
             return;
         }
         Info info = new Info( this,  null,  newNick,  login,  hostname,  "" );
         for(BotCommandHandler doer : doers )
         {
             doer.handleNickChange(info);
         }
     }
 
     /**
      * Starts the program
      *
      * @param args DOCUMENT ME!
      *
      * @throws Exception DOCUMENT ME!
      */
     public static void main( String[] args ) throws Exception
     {
         if( args.length < 1 )
         {
             usage();
             System.exit(1);
         }
 
         // Now start our bot up.
         NexusBot bot = new NexusBot( args[0] );
 
         // Enable debugging output.
         bot.setVerbose(true);
 
         // Sos I can test
         bot.setAutoNickChange( true );
 
     }
 
     private static void usage()
     {
         System.out.println( "usage: java -jar nexusbot.jar <properties file>"  );
     }
 
     protected void onDisconnect()
     {
         try
         {
             Thread.sleep(5*60*1000); //sleep 5 minutes, then rejoin
         }
         catch (Exception e)
         {
             //just rejoin immediately
         }
         join();
     }
 
     protected void join()
     {
         try{
             // Connect to the IRC server.
             connect( config.getValue("server"), Integer.parseInt(config.getValue("port")) );
             System.out.println( "Connected to " + config.getValue("server") );
         }
         catch(Exception e)
         {
             e.printStackTrace();
             System.exit(1);
         }
 
         if(config.getValue("authenticate") != null)
             this.sendMessage("NickServ", config.getValue("authenticate"));
 
         String[] channels = config.getValues("room");
         for(int i = 0; i < channels.length; i++)
         {
             channelCommands.join(channels[i], this);
             System.out.println( "Joined to " +  channels[i] );
         }
     }
 
     public static boolean isOwner(String nick)
     {
         return owners.contains(nick);
     }
 
     public String getProperty(String name)
     {
         return config.getValue( name );
     }
 
     /**
      * Reloads all the various text files.  !reload makes the bot do this.
      */
     public void reload(  )
     {
         ignoreList = Utils.loadFile( "ignore.txt", true );
         // System.out.println("ignoreList loaded without error: " + ignoreList);
     }
 }
 
