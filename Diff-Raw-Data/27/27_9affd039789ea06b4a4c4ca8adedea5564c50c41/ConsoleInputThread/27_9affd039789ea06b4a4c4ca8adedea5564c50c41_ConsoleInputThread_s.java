 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  * @date Apr 30, 2011
  * @author Techjar
  * @version 
  */
 
 
 package com.simplechat.client;
 
 import java.io.IOException;
 import jline.ConsoleReader;
 import com.simplechat.protocol.*;
 
 public class ConsoleInputThread extends Thread{
     private ServerData server;
     private NameData name;
     private ConsoleReader cr;
 
     
     public ConsoleInputThread(ServerData server, NameData name, ConsoleReader cr) {
         this.server = server;
         this.name = name;
         this.cr = cr;
     }
 
     @Override
     public void run() {
         try {
             PacketHandler ph = new PacketHandler();
             cr.setBellEnabled(false);
             String line;
 
             while((line = cr.readLine(this.name.getName() + ": ", null).trim()) != null) {
                 if(!line.isEmpty()) {
                     try {
                         Packet3Chat packet = new Packet3Chat(name.getName(), line);
                         ph.sendClientPacket(packet, server.getIP(), server.getPort(), this.server.getSocket());
                     }
                     catch(ArrayIndexOutOfBoundsException e) {
                         cr.printString("Error: Message is too large.\n");
                     }
                 }
             }
         }
         catch(IOException e) {
            System.err.println("ColsoleInputThread failed, program will terminate.");
             System.exit(0);
         }
     }
 }
