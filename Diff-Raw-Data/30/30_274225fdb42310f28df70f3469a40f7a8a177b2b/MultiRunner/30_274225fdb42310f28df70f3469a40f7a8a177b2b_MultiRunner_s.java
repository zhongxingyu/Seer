 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.utgdev.ctrnngame.drawers.autotest;
 
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import no.utgdev.ctrnngame.App;
 import no.utgdev.ga.core.selection.mechanism.SigmaScalingMechanism;
 import no.utgdev.ga.core.selection.mechanism.TournamentSelectionMechanism;
 import no.utgdev.ga.core.selection.protocol.GenerationalMixing;
 import sun.awt.windows.WWindowPeer;
 
 /**
  *
  * @author Nicklas
  */
 public class MultiRunner {
 
     public static final List<Runnable> queue = Collections.synchronizedList(new LinkedList<Runnable>());
 
    public static void main(String[] args) {
 
         String[] mechanism = {//FitnessProportionateMechanism.class.getName(),
             SigmaScalingMechanism.class.getName(),
             TournamentSelectionMechanism.class.getName(), //RankMechanism.class.getName()};
         };
         String[] tournamentSizes = {"10", "25", "50", "75"};
         String[] tournamentEpses = {"0.01", "0.05", "0.1", "0.2"};
         Runner[] runners = new Runner[8];
         for (int i = 0; i < runners.length; i++) {
             runners[i] = new Runner();
             runners[i].start();
         }
         double lasttime;
         for (String m : mechanism) {
             for (double cr = 0.5; cr <= 1.0; cr += 0.1) {
                 for (double mr = 0; mr <= 0.5; mr += 0.1) {
 //                        for (int psize = 25; psize <= 200; psize += 25) {
                     if (m.equals(TournamentSelectionMechanism.class.getName())) {
                         for (String size : tournamentSizes) {
                             for (String eps : tournamentEpses) {
                                 final String[] input = {
                                     GenerationalMixing.class.getName(),
                                     m,
                                     size,
                                     eps,
                                     String.valueOf(cr),
                                     String.valueOf(mr),
                                     String.valueOf(100)
                                 };
                                 queue.add(new Runnable() {
                                     public void run() {
                                         double t = System.currentTimeMillis();
                                        new App().main(input);
                                         System.out.println("Time: " + (System.currentTimeMillis() - t));
                                     }
                                 });
                             }
 //                                    System.out.println("herp");
                         }
                     } else {
                         final String[] input = {
                             GenerationalMixing.class.getName(),
                             m,
                             "20",
                             "0.05",
                             String.valueOf(cr),
                             String.valueOf(mr),
                             String.valueOf(100)
                         };
                         queue.add(new Runnable() {
                             public void run() {
                                 double t = System.currentTimeMillis();
                                new App().main(input);
                                 System.out.println("Time: " + (System.currentTimeMillis() - t));
                             }
                         });
                     }
                 }
             }
         }
         for (Runner r : runners){
             r.breaker();
         }
     }
 
     public static class Runner extends Thread {
 
         private boolean run = true;
         private boolean breaker = false;
 
         @Override
         public void run() {
             int c = 0;
             outer:
             while (run) {
                 while (queue.isEmpty()) {
                     if (breaker){
                         break outer;
                     }
                     Thread.yield();
                 }
                 Runnable r = queue.remove(0);
                 r.run();
             }
         }
 
         public void halt() {
             this.run = false;
         }
         public void breaker() {
             this.breaker = true;
         }
     }
 }
