 /*
  * Copyright (C) 2010 Peter Martischka This program is free software; you can
  * redistribute it and/or modify it under the terms of the GNU General Public
  * License as published by the Free Software Foundation; either version 3 of the
  * License, or (at your option) any later version. This program is distributed
  * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
  * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details. You should have received
  * a copy of the GNU General Public License along with this program; if not, see
  * <http://www.gnu.org/licenses/ >.
  */
 
 package de.pitapoison.chat.server;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.Socket;
 import java.util.ArrayList;
 
 public class ClientThread extends Thread
 {
     private BufferedReader reader=null;
     private BufferedWriter writer=null;
     private String username=null;
     private Socket socket;
     
     private static ArrayList<ClientThread> clients=new ArrayList<ClientThread>();
 
     public ClientThread(Socket socket)
     {
         try
         {
             //Streams initialiseren
             reader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
             writer=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             this.socket=socket;
             
             //Lese bis du eine Leerzeile kriegst
             for(String line;(line=reader.readLine()).length()>0;)
             {
                 //Wenn die Userinfo in der Zeile steht
                 if(line.startsWith("User"))
                 {
                     //Username abspeichern
                     username=line.substring(line.indexOf(":")+1);
                 }
             }
             
             //Log Message ausgeben
             ServerGUI.message("Verbindung mit " + username + " aufgebaut");
             
             //Sende die Nachricht das der User den Raum betreten hat
             sendAll(username + " hat den Chatroom betreten");
             
             //Namen der anwesenden User sammelen
             StringBuffer namen=new StringBuffer();
             for(ClientThread client: clients)
             {
                 namen.append(client.username + ", ");
             }
             
             String onlineMessage;
             
             //Wenn User anwesend sind
             if(namen.length()>0)
             {
                 onlineMessage="Folgende User sind im Chatroom: " + namen.substring(0, namen.length()-2)+"\n";
             }
             else
             {
                 onlineMessage="Du bist alleine im Chatroom\n";
             }
             
             //Benachrichtige den User ob weiter User anwesend sind
             writer.write(onlineMessage);
             writer.flush();
             
             //Sich selbst dem Array hinzufügen
             clients.add(this);
             
             //Listen Thread starten
             start();
         }
         catch(IOException e)
         {
             ServerGUI.message("Fehler beim Initialisieren mit dem Client");
             e.printStackTrace();
         }
     }
     
     private void sendAll(String message) throws IOException
     {
         message+="\n";
         
         //Daten an alle Clients schicken
         for(ClientThread client: clients)
         {
             client.writer.write(message);
             client.writer.flush();
         }
     }
     
     public void run()
     {
         //Endlosschleife
        while(true)
         {
             try
             {
                 //Zeile lesen
                 String inputLine=reader.readLine();
                 
                 //String der an alle Clients geschickt werden soll
                 String busLine="<" + username + "> " + inputLine;
                 
                 //An alle senden
                 sendAll(busLine);
             }
             catch(IOException e)
             {
                 //Fehler melden
                 ServerGUI.message("Verbindung mit Client " + username + " verloren");
                 //Thread aus der ArrayList herausnehmen
                 clients.remove(this);
                 
                 //Socket schließen & Die User darüber informieren das der User den Chat verlassen hat
                 try
                 {
                     socket.close();
                     sendAll(username + " hat den Chatroom verlassen");
                 }
                 catch(IOException e1)
                 {
                     e1.printStackTrace();
                 }
                 
                 //Endlosschleife abrechen
                break;
             }
         }
     }
 }
