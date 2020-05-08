 package fr.iutvalence.java.mp.p3;
 
 /**
  * Main class provides to start the game with : - Ascii display - Random
  * directions
  * 
  * @author maliel
  */
 public class AsciiRandomMain
 {
     /**
      * Display road test for now
      * 
      * @param args
      *            not specified
      */
     public static void main(String[] args)
     {
        // TODO FIXED it seems to have a bug, the user car never moves
         
        
         // TODO (fix) the game should no longer take player as parameter
         // TODO (fix) this class extends thread, but no thread is started in main(s)
         AsynchronousModeGame a = new AsynchronousModeGame("Jackson", new AsciiDisplay(), new RandomPlayer());
         
         ThreadScroll s = new ThreadScroll(a);
         ThreadDirection d = new ThreadDirection(a, a.getPlayer());
         
         // Lancement des threads 
         s.start();
         d.start();
         
         // Lancement de la partie
         a.play();
         
     }
 
 }
