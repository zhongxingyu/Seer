 /*
  * :mode=java:tabSize=4:indentSize=4:noTabs=true:
  * :folding=indent:collapseFolds=0:wrap=none:maxLineLen=120:
  *
  * $Source: /var/lib/cvsd/cvsrepo/invasion/nw/nexusbot/src/nexusbot/MessageCommands.java,v $
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
  * This provides functionality that is more closely tied to channel management or dealing with RL objects (like time)
  *
  * @author jchoyt
   */
 public class MessageCommands implements BotCommandHandler
 {
 
     /**
      * NexusBot saves messages to be delivered the next time someone logs in - this is the central mechanism for doing this.
      */
     private Messages messages = new Messages( "messages.ser" );
     private String lastJoin = "";
 
     private Map<String, Integer> sentMessages = new HashMap<String, Integer>();
 
     private long nextReset = System.currentTimeMillis() + 1000*60*60*24;
 
     public MessageCommands()
     {
         messages.load(  );
     }
 
     public boolean handleMessage(Info info)
     {
         String message = info.getMessage();
         String sender =  info.getSender();
         if ( message.startsWith( "!leavemessage " ) || message.startsWith( "!lm " ) )
         {
             return leaveMessage( info );
         }
         else if ( message.equalsIgnoreCase( "!getmessages" ) || message.equalsIgnoreCase( "!gm" ))
         {
             List m = messages.getMessages( sender );
             if ( m != null )
             {
                 if ( m.size(  ) == 0 )
                 {
                     info.sendMessage( "You have no messages." );
                 }
                 else
                 {
                     //sendMessage( channel, "You have " + m.size(  ) + " messages. They are being sent to you privately." );
                     try
                     {
                         FileWriter out = new FileWriter("messages/"+sender+".archive", true);
                         for ( Object o : m )
                         {
                             info.sendMessage( sender, String.valueOf( o ) );
                             out.write(String.valueOf(o) + Utils.NEWLINE);
                         }
                         out.close();
                     }
                     catch(Exception e)
                     {
                         e.printStackTrace();
                     }
 
                 }
             }
         }
         else
             return false;
         return true;
     }
 
 
     public boolean leaveMessage(Info info)
     {
         String message = info.getMessage();
         String sender =  info.getSender();
         String channel = info.getChannel();
         String stripped = message.substring( message.indexOf(" ") ).trim(  );
         int breakpt = stripped.indexOf( ' ' );
         String who = stripped.substring( 0, breakpt ).trim(  );
 
         //check for abuse.  Thank you HackBastard
         if( System.currentTimeMillis() > nextReset )
         {
             sentMessages = new HashMap<String, Integer>();
             nextReset = System.currentTimeMillis() + 1000*60*60*24;
         }
         if( sentMessages.get( who ) == null )
         {
             sentMessages.put(who, 1);
         }
         else
         {
             int newTot = sentMessages.get(who) + 1;
             if( newTot > 25 )
             {
                 info.sendMessage( "Your request is denied.  " + who +
                    " has a full mailbox.  This will reset sometime in the next 24 hours.  Blame the <censored> who abused me and forced Entomo to put this in just so I can be started back up.");
                 return true;
             }
             sentMessages.put(who, newTot );
         }
         //now, onto the work of
         if( who.toLowerCase().equals(info.getBot().getName().toLowerCase()) )
         {
             info.sendMessage("That would be me, dingleberry.");
             return true;
         }
         else if( who.toLowerCase().equals("jane") )
         {
             info.sendMessage("No.  I'm not speaking to her.");
             return true;
         }
         else if( who.toLowerCase().contains("beerwench") )
         {
             info.sendMessage("Sorry, no. She scares me. Did you know she has a gun?");
             return true;
         }
         else if( who.toLowerCase().contains("Teksura"))
         {
             List<String> stooges = Arrays.asList( "Teksura" );
             if( Utils.isPresent(info, stooges) )
             {
                 info.sendAction( "points at Tek.   \"I've been instructed to convey the following message, 'Just send him a fucking query if you want him to get the message before next month.'\"" );
                 info.sendMessage( "Try this: /msg Teksura " + stripped.substring( breakpt ) );
                 return true;
             }
         }
         String what = "( " + Utils.getRealTime(info) + ": In " + channel + " | From " + sender + " | To " + who + ") " + stripped.substring( breakpt );
         messages.putMessage( who, what );
         info.sendMessage( "Message saved." );
         try
         {
             FileWriter out = new FileWriter("messages/"+ sender +".archive", true);
             out.write( what + Utils.NEWLINE );
             out.close();
         }
         catch(Exception e)
         {
             e.printStackTrace();
         }
         return false;
     }
 
     /**
      *  Handles private messages.  These are usually administrative type tasks that only the bot owner should be able to execute.
      */
     public boolean handlePrivateMessage(Info info)
     {
         String message = info.getMessage();
         /* if ( message.equalsIgnoreCase( "!reviewall" ) )
         {
             showAllMessages( info );
         }
         else */ if( message.startsWith( "!dm " ) )
         {
             String nick = message.substring( 4 );
             int count = messages.getMessageCount( nick );
             if( count == 0 )
             {
                 info.sendMessage( "No messages waiting for " + nick );
             }
             else
             {
                 messages.removeMessages( nick );
                 info.sendMessage( count + " messages removed.");
             }
         }
         else
             return handleMessage( info );
         return true;
     }
 
 
     public void showAllMessages( Info info )
     {
         List m = messages.reviewAllMessages(  );
 
         if ( m != null )
         {
             if ( m.size(  ) == 0 )
             {
                 info.sendMessage( "No messages pending." );
             }
             else
             {
                 for ( Object o : m )
                 {
                     info.sendMessage( String.valueOf( o ) );
                 }
             }
        }
     }
 
     public void handleJoin( Info info )
     {
         String sender = info.getSender();
         if ( messages.getMessageCount( sender ) > 0 && !sender.equals(lastJoin))
         {
                 info.sendMessage( "Welcome " + sender + ".  You have " + messages.getMessageCount( sender ) +
                     " message(s) waiting.  Use !getmessages (or !gm) to retrieve them." );
                 lastJoin = sender;
         }
         if( sender.equals( info.getBot().getName() ) )
         {
             //check all current members of the room to see if they have messages
             User[] users = info.getBot().getUsers( info.getChannel() );
             for( User u : users )
             {
                 if ( messages.getMessageCount( u.getNick() ) > 0 )
                 {
                     info.sendMessage( "Attention " + u.getNick() + ".  You have message(s) waiting.  Use !getmessages (or !gm) to retrieve them." );
                 }
             }
         }
     }
 
 
     public void handleNickChange( Info info )
     {
         String sender = info.getSender();
         if( sender.equals(info.getBot().getName() ))
         {
             return;
         }
         if ( messages.getMessageCount( sender ) > 0 )
         {
             String[] channels = info.getBot().getChannels();
             for(String c :  channels)
             {
                 User[] users = info.getBot().getUsers( c );
                 for( User u : users )
                 {
                     if ( u.getNick().equals(sender) )
                     {
                         info.sendMessage( c, "Attention " + sender + ".  You have " + messages.getMessageCount( sender ) +
                             " message(s) waiting.  Use !getmessages (or !gm) to retrieve them." );
                         return;
                     }
                 }
             }
         }
     }
 
 }
 
 
 
