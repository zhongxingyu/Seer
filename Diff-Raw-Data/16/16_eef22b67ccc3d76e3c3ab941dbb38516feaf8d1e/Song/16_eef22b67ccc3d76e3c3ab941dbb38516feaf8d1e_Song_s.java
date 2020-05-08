 package messaging;
 
 import instruments.Instrument;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * This must be thread safe.
  */
 public class Song {
     // Represent notes over time, first parameter will be time (milliseconds)
     private HashMap<Long,Toot> toots;
     Long firstTootTime;
 
     public Song(){
         toots = new HashMap<Long, Toot>();
         firstTootTime = 0L;
     }
 
    public synchronized void addNote(Instrument.Note oneSingleLonelyNote){
         if(firstTootTime == 0){
             firstTootTime = System.currentTimeMillis();
         }
 
         long relativeTimeInMilliseconds = System.currentTimeMillis() - firstTootTime;
         addToTootOrMakeANewOneForTime(relativeTimeInMilliseconds, oneSingleLonelyNote);
     }
 
     /**
      * Responsible for playing the song
      */
     public void play(){
         synchronized(this) {
             final HashMap<Long,Toot> myToots = new HashMap<Long,Toot>(toots);
         }
 
 //        Executors.newScheduledThreadPool(1,)
     }
 
 
    private void addToTootOrMakeANewOneForTime(long timeInMilliseconds, Instrument.Note someNote){
         if(toots.containsKey(timeInMilliseconds)){
             toots.get(timeInMilliseconds).add(someNote);
         } else {
             Toot t = new Toot();
             t.add(someNote);
             toots.put(timeInMilliseconds,t);
         }
     }
 
 
 
 
     /**
      * This is an event in a song ... that happens at a specific time
      */
     private class Toot {
         List sounds;
 
         public Toot(){
             sounds = new LinkedList<Instrument.Note>();
         }
 
         public void add(Instrument.Note note){
             sounds.add(note);
         }
 
         public List<Instrument.Note> getNotes(){
             return sounds;
         }
     }
 }
