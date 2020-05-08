 import java.util.ArrayList;
 
 import de.minestar.conair.network.ChatClient;
 import de.minestar.conair.network.ChatServer;
 import de.minestar.conair.network.packets.HelloWorldPacket;
 
 /*
  * Copyright (C) 2013 MineStar.de 
  * 
  * This file is part of ConAir.
  * 
  * ConAir is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * ConAir is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with ConAir.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 public class Main {
 
     /**
      * @param args
      */
     public static void main(String[] args) {
 
         try {
            ChatServer server = new ChatServer(9002, new ArrayList<String>());
 
             Thread t = new Thread(server);
             t.start();
 
            ChatClient client = new ChatClient("localhost", 9002);
             Thread cThread = new Thread(client);
             cThread.start();
 
             HelloWorldPacket packet = new HelloWorldPacket("Hallo Welt!");
             for (int i = 0; i < 5; i++) {
                 Thread.sleep(500);
                 client.sendPacket(packet);
             }
 
             Thread.sleep(500);
 
             client.stop();
             cThread.stop();
 
             server.stop();
             t.stop();
         } catch (Exception e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 }
