 package model;
 
 /**
  *
  * @author guetar
  */
 public class Player {
     
     private String name;
     private Track track;
     private int dice;
     private int oldPos;
     private int curPos;
     
     public Player(String name) {
         this.name = name;
         this.oldPos = this.curPos = 0;
         
     }
     public String getName() {
         return name;
     }
     
     public void setTrack(Track track) {
         this.track = track;
     }
 
     public void setDice(int dice) {
         this.dice = dice;
         this.setPos(this.curPos + dice);
     }
     
     public int getDice() {
         return dice;
     }
     
     public void setPos(int newPos) {
         oldPos = curPos;
         curPos = (newPos < track.getLength()) ? newPos : track.getLength();
     }
     
     public void resetPos() {
         oldPos = curPos = 0;
     }
     
     public int getOldPos() {
         return oldPos;
     }
     
     public int getCurPos() {
         return curPos;
     }
 }
