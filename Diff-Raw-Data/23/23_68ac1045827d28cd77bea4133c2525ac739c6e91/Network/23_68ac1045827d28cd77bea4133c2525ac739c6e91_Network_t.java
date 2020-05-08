 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package network;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 /**
  *
  * @author Miracle
  */
 public class Network {
 
     /*
      * construct for network
      */
     public Network(){
         this.computers = new PC[5];
         this.computers[0] = new PC(new Windows(), true);
         this.computers[1] = new PC(new Linux(), false);
         this.computers[2] = new PC(new MacOS(), false);
         this.computers[3] = new PC(new Linux(), false);
         this.computers[4] = new PC(new Windows(), true);
     }
     
     /**
      * Makes system progress
      */
     public void progressNetwork() {
         for (int i = 0; i < 5; i++) {
             for (int j = 0; j < 5; j++) {
                 if ((this.connections[i][j] == 1) && (this.computers[i].isInfected() || this.computers[j].isInfected())) {
                     if (this.computers[i].isInfected()) {
                         this.computers[j].virusAttack();
                     } else {
                         this.computers[i].virusAttack();
                     }
                 }
             }
         }       
     }
 
     /*
      * print the current state of net
      */
     public void printNetwork() {
         for (int i = 0; i < 5; ++i) {
             if (computers[i].isInfected()) {
                 System.out.println("Computer # " + i + " is infected");
             } else {
                 System.out.println("Computer # " + i + " isn't infected");
             }
         }
         System.out.println();
     }
     
     /**
      * Starts working
      */
     public void start() {
        Timer timer = new Timer();
        StartTimer start = new StartTimer();
        timer.scheduleAtFixedRate(start, 0, 1000);
    }
    
    /**
     * class for working with timer
     */
    class StartTimer extends TimerTask {
        @Override
        public void run() {
            progressNetwork();
            printNetwork();
        }
     }
     
     
     public PC[] getComputers() {
         return this.computers;
     }
     
     /*
      * computers are in net
      */
     private PC[] computers;
     
     /*
      * matrix relations
      */
     private int[][] connections = {
         {0, 0, 1 , 0, 1},
         {0, 0, 0 , 1, 1},
         {1, 0, 0 , 0, 0},
         {0, 1, 0 , 0, 0},
         {1, 1, 0 , 0, 0},
     };
 }
