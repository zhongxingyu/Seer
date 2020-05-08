 package client;
 
 
 import java.io.*;
 import java.net.*;
 
 public class HackBot {
 
     public static void main(String[] args) throws Exception {
 
         // The server to connect to and our details.
         String server = "irc.quakenet.org";
         String nick = "Nexdot";
         String login = "Nexdot";
         
         // The channel which the bot will join.
         String channel = "#vashbottest";
         
         // Connect directly to the IRC server.
         Socket socket = new Socket(server, 6667);
         BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream( )));
         BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream( )));
         
         // Log on to the server.
         writer.write("NICK " + nick + "\r\n");
         System.out.println("NICK " + nick + "\r\n");
         writer.write("USER " + login + " 8 * : Java IRC Hacks Bot\r\n");
         System.out.println("USER " + login + " 8 * : Java IRC Hacks Bot\r\n");
         writer.flush( );
         
         // Read lines from the server until it tells us we have connected.
         String line = null;
         while ((line = reader.readLine( )) != null) {
         	System.out.println(line);
             if (line.indexOf("004") >= 0) {
                 // We are now logged in.
                 break;
             }
             else if (line.indexOf("433") >= 0) {
                 System.out.println("Nickname is already in use.");
                 return;
             }
 
             if (line.startsWith("PING ")) {
                 // We must respond to PINGs to avoid being disconnected.
                 writer.write("PONG " + line.substring(5) + "\r\n");
                 System.out.println("PONG " + line.substring(5) + "\r\n");
                 writer.flush( );
             }
         }
         
         // Join the channel.
         writer.write("JOIN " + channel + "\r\n");
         writer.flush( );
         
         // Keep reading lines from the server.
         while ((line = reader.readLine( )) != null) {
         	
             // Print the raw line received by the bot.
             System.out.println(line);
             
             if (line.startsWith("PING ")) {
                 // We must respond to PINGs to avoid being disconnected.
                 writer.write("PONG " + line.substring(5) + "\r\n");
                 writer.flush( );
             }
             
            if (line.startsWith(":Damaos")) {
            	writer.write("PRIVMSG " + channel + " :STFU\r\n");
            	writer.flush( );
            }
            
             for (String i : line.split(" "))
             {
             	if (i.startsWith(":http://www.youtube.com/watch?v=") || i.startsWith(":https://www.youtube.com/watch?v="))
             	{
             		YoutubeLink y = new YoutubeLink(i.substring(1));
             		writer.write("PRIVMSG " + channel + " :" + y.getTitle() + " " + y.getDuration() + "\r\n");
             		writer.flush();
             	}
             }
             
         }
         
         socket.close();
     }
 
 }
