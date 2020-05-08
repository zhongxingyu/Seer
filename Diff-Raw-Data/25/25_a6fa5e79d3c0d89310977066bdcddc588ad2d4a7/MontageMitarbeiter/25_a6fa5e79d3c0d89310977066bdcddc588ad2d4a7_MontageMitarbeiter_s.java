 package tgm.sew.hit.roboterfabrik;
 
 import java.util.Random;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 /**
  * Stellt einen MontageMitarbeiter dar, welcher sich
  * um den Zusammenbau der Threadees kümmert
  * 
  * @author Dominik
  * @version 0.13
  */
 public class MontageMitarbeiter implements Stoppable {
     
     private int id;
     
     private Fabrik fabrik;
     private int speed;
     private boolean stop = false;
     
     private Threadee threadee;
     private static final Logger logger = Logger.getLogger("Fabriklog");
     
     /**
      * Konstruktor des MontageMitarbeiters
      * @param id die ID des MontageMitarbeiters
      * @param fabrik die Referenz auf die Fabrik,
      * welcher der Mitarbeiter zugeordnet ist
      * @param speed in welcher Geschwindigkeit die Threadees gebaut werden
      */
     public MontageMitarbeiter(int id, Fabrik fabrik, int speed) {
         this.id = id;
         this.fabrik = fabrik;
         this.speed = speed;
     }
 
     /**
      * Run Methode von Runnable
      */
     @Override
     public void run() {
         while(!stop) {
             try {
                 Thread.sleep(speed);
             } catch (InterruptedException ex) {
             }
             threadee = new Threadee(fabrik.getSekretariat().nextThreadeeID(),id);
             while(!threadee.fehlendeTeile().isEmpty()) {
                 try {
                     threadee.addTeil(fabrik.getTeil(threadee.fehlendeTeile()));
                 } catch (TeilNichtImLagerException ex) {
                     try {
                         Thread.sleep(new Random().nextInt(1000*60));
                     } catch (InterruptedException ex1) {
                     }
                 }
             }
             threadee.zusammenbauen();
             logger.log(Level.INFO, "Threadee " + threadee.getID() + " fertiggestellt");
             fabrik.lagerThreadee(threadee);
         }
         logger.log(Level.INFO, "Feierabend");
     }
 
     /**
      * Stop Methode aus dem Stoppable Interface
      * zum sauberen Beenden des Threads
      */
     @Override
     public void stop() {
         stop = true;
     }
     
     /**
      * isStopped Methode aus dem Stoppable Interface
      * @return ob dieser Thread gestopped ist
      */
     @Override
     public boolean isStopped() {
         return stop;
     }
     
 }
