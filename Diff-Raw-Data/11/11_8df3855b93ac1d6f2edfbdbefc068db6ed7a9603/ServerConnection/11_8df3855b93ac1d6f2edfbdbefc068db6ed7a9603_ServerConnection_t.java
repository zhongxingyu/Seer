 package com.titankingdoms.dev.TitanIRC;
 
 import java.io.*;
 import java.net.Socket;
 
 /**
  * Copyright (C) 2012 Chris Ward
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 public class ServerConnection implements Runnable {
     private String server;
     private int port;
     private String[] channels;
     private String botname;
     private TitanIRC instance;
     public ServerConnection(String botname, String server, int port, String[] channels, TitanIRC instance)
     {
         this.server = server;
         this.port = port;
         this.channels = channels;
         this.botname = botname;
         this.instance = instance;
     }
 
     private Socket socket;
     BufferedReader reader;
     PrintWriter writer;
 
     public void run()
     {
         try {
             socket = new Socket(server, port);
             reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
         }
         catch(IOException e)
         {
             instance.getLogger().severe("An error occured (" + e.getMessage() +") while connecting to " + server + ":" + port);
             close();
             return;
         }
         changeNick(botname);
         writeln("USER TitanIRC 0 * :TitanIRC Bot");
        boolean unbroken = true;
        while(unbroken)
         {
             String read;
             try {
                 read = reader.readLine();
             }
             catch (Exception e)
             {
                 close();
                 return;
             }
 
             if(read.split(" ")[0].equalsIgnoreCase("PING"))
                 writeln("PONG " + read.split(" ")[1]);
            else if(read.split(" ")[0].equalsIgnoreCase("ERROR"))
                close();
            else if(read.split(" ")[1].equalsIgnoreCase("001"))
            {
                for(String channel : channels)
                    writeln("JOIN " + channel);
            }
             instance.debug(read);
         }
     }
 
     private void close()
     {
         try { socket.close(); } catch (Exception eX) {}
         try { reader.close(); } catch (Exception eX) {}
         try { writer.close(); } catch (Exception eX) {}
     }
 
     public void writeln(String message)
     {
         try{
             writer.write(message + "\n");
             writer.flush();
         }
         catch (Exception e) { close(); }
     }
 
     public void changeNick(String nick)
     {
         writeln("NICK " + nick);
     }
 }
