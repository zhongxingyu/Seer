 /**
  * Created by IntelliJ IDEA.
  * User: abhat
  * This software is provided under the "DO WHAT THE HECK YOU WANT TO DO WITH THIS LICENSE"
  */
 package com.abhirama.gameengine.tests.stresstest;
 
 import com.abhirama.gameengine.Player;
 import com.abhirama.gameengine.Room;
 import com.abhirama.http.GameServerHandler;
 import com.abhirama.utils.Util;
 
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 public class CustomGameServerHandler extends GameServerHandler {
 
   @Override
   public Map gameLogic(Map data) {
     //Simulate a memcache fetch
     try {
       TimeUnit.MILLISECONDS.sleep(2);
     } catch (InterruptedException e) {
       e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
     }
     Room room = Room.getRoom(Util.getRandomInt(1, 1000));
 
     System.out.println("Executing hit room event:" + room.getPlayers().size());
     List<Player> players = room.getPlayers();
 
     TestPlayer player0 = (TestPlayer)players.get(Util.getRandomInt(0, 9));
     TestPlayer player1 = (TestPlayer)players.get(Util.getRandomInt(0, 9));
 
     player1.setHealth(90);
 
    //Simulate a memcache fetch
     try {
       TimeUnit.MILLISECONDS.sleep(2);
     } catch (InterruptedException e) {
       e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
     }
 
     this.addToOp("I am inside custom game server handler" + this.requestParameters.toString());
 
     return null;
   }
 }
