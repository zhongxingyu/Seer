 package com.github.webtictactoe.webtictactoe.game;
 
 import javax.xml.bind.annotation.XmlRootElement;
 
 /**
  * This is the message that the server broadcasts to the players that have been
  * matched together to play a game of tictactoe.
  * @author pigmassacre
  */
 @XmlRootElement
 public class UUIDMessage {
 
     public String uuid;
     public Integer size;
    public String startingPlayerName;
 
    public UUIDMessage(String uuid, Integer size, String startingPlayerName) {
         this.uuid = uuid;
         this.size = size;
        this.startingPlayerName = startingPlayerName;
     }
 
     public UUIDMessage() {
         
     }
     
 }
