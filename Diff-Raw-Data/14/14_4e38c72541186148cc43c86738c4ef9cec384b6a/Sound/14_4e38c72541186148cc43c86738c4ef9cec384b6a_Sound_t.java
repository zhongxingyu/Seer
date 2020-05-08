 import greenfoot.*;
 import java.util.LinkedList;
 import java.util.ArrayList;
 
 /**
  * DataBase of Sound files
  */
 public class Sound
 {
     private LinkedList<GreenfootSound> track;

     private String[] attack;
     private String moneyName;
     private int volume;
     public Sound()
     {
         track = new LinkedList<GreenfootSound>();
        moneyName   = "sounds/buy.wav";
         volume      = 50;
         attack      = new String[4];
 
        attack[0] = "sounds/airAttack.wav"; 
        attack[1] = "sounds/fireAttack.wav"; 
        attack[2] = "sounds/earthAttack.wav"; 
        attack[3] = "sounds/waterAttack.wav"; 
 
         playOnce(); 
     }
 
     public void run(){
         ArrayList <GreenfootSound> al = new ArrayList<GreenfootSound>();
         for (GreenfootSound s : track){
             if (s.isPlaying() == false){
                 al.add (s);
             }
         }
         for (int i = 0; i < al.size(); i++){
             GreenfootSound s = al.get(i);
             track.remove (s);
         }
     }
 
     public void play(int Id)
     {
         GreenfootSound s = new GreenfootSound (attack[Id-1]);
         s.setVolume (volume);
         s.play();
         track.add (s);
     } 
 
     public void playMoney(){
         GreenfootSound money = new GreenfootSound (moneyName);
         money.setVolume (volume);
         money.play();
         track.add (money);
     }
 
     private void playOnce()
     {
         GreenfootSound money = new GreenfootSound(moneyName);
 
         money.setVolume(0);
         //money.play();
         for (int i = 0; i < attack.length; i++){
             GreenfootSound s = new GreenfootSound (attack[i]);
             s.setVolume (0);
             //s.play();
         }
     }
 
     public void setVolume(int v){
         volume = v;
         for (GreenfootSound s : track){
             s.setVolume (volume);
         }
     }
 }
