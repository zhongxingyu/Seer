 package model;
 
 public class Game {
 
     private Player p1;
     private Player p2;
     private Track track;
     private String leader;
     private int round;
     private long startTime;
     private boolean[] oil;
 
     public Game(Player p1, Player p2) {
         startTime = System.currentTimeMillis();
         round = 0;
         
         this.p1 = p1;
         this.p2 = p2;
         
         this.track = new Track();
         p1.setTrack(track);
         p2.setTrack(track);
     }
 
     public void incrementRound() {
         round++;
     }
     
     public int getRound() {
         return round;
     }
 
     public long getElapsedTime() {
         return System.currentTimeMillis() - startTime;
     }
 
     public String getLeader() {
         return (p1.getCurPos() > p2.getCurPos()) ? p1.getName() : p2.getName();
     }
     
     public boolean isOilField(int position) {
         if(position < oil.length - 1) {
             return oil[position];
         }
         return false;
     }
     
     public boolean isOver() {
         return (p1.getCurPos() == track.getLength() || p2.getCurPos() == track.getLength());
     }
 }
