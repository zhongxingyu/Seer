 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.utgdev.ctrnngame.drawers.autotest;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import no.utgdev.ctrnngame.App;
 import no.utgdev.ga.core.selection.mechanism.FitnessProportionateMechanism;
 import no.utgdev.ga.core.selection.mechanism.RankMechanism;
 import no.utgdev.ga.core.selection.mechanism.SigmaScalingMechanism;
 import no.utgdev.ga.core.selection.mechanism.TournamentSelectionMechanism;
 import no.utgdev.ga.core.selection.protocol.FullGenerationReplacement;
 import no.utgdev.ga.core.selection.protocol.GenerationalMixing;
 import no.utgdev.ga.core.selection.protocol.OverProduction;
 
 /**
  *
  * @author Nicklas
  */
 public class RerunOfBest {
 
     public static final List<Runnable> queue = Collections.synchronizedList(new LinkedList<Runnable>());
 
     public static void main(String[] args) {
         final int runPerCase = 20, retainBestCount = 20;
         run(runPerCase, retainBestCount);
     }
 
     private static void run(int runs, int retain) {
         final File dir = new File("./images/catchavoid-nopenalty-modified/");
         final File[] typeFiltered = dir.listFiles(new FilenameFilter() {
             Pattern p = Pattern.compile("([\\d\\.]*?)-(\\d*?)gen-(\\d*?)pop-([\\d\\.]*?)tour-([\\d\\.]*?)eps-([\\d\\.]*?)cr-([\\d\\.]*?)mr-(\\w*)-(\\w*)-?\\.png");
 
             public boolean accept(File dir, String name) {
                 return p.matcher(name).find();
             }
         });
         Runner[] runners = new Runner[8];
         for (int i = 0; i < runners.length; i++) {
             runners[i] = new Runner();
             runners[i].start();
         }
         
         System.out.println("Found files: " + typeFiltered.length);
         final Case[] bestFound = retainBest(typeFiltered, retain);
         int caseId = 0;
         double lastTime;
         for (final Case c : bestFound) {
             final File newDir = new File(dir.getParentFile(), "/" + String.valueOf(caseId) + "/");
             System.out.println(newDir);
             newDir.mkdirs();
 
             int runCount = 0;
 
             while (runCount < runs) {
                 queue.add(new RunnMe(runCount++, c, dir, newDir));
             }
             caseId++;
         }
         for (Runner r : runners){
             r.breaker();
         }
     }
 
     private static String unmarshallProtocol(String protocol) {
         if (protocol.equals("GenRep")) {
             return FullGenerationReplacement.class.getName();
         } else if (protocol.equals("GenMix")) {
             return GenerationalMixing.class.getName();
         } else if (protocol.equals("OverProd")) {
             return OverProduction.class.getName();
         } else {
             throw new RuntimeException("Did not recognize protocol: " + protocol);
         }
     }
 
     private static String unmarshallMechanism(String mechanism) {
         if (mechanism.equals("Fitness")) {
             return FitnessProportionateMechanism.class.getName();
         } else if (mechanism.equals("Rank")) {
             return RankMechanism.class.getName();
         } else if (mechanism.equals("Sigma")) {
             return SigmaScalingMechanism.class.getName();
         } else if (mechanism.equals("Tourna")) {
             return TournamentSelectionMechanism.class.getName();
         } else {
             throw new RuntimeException("Did not recognize meachanism: " + mechanism);
         }
     }
 
     private static Case[] retainBest(File[] typeFiltered, int retain) {
         Pattern pattern = Pattern.compile("([\\d\\.]*?)-(\\d*?)gen-(\\d*?)pop-([\\d\\.]*?)tour-([\\d\\.]*?)eps-([\\d\\.]*?)cr-([\\d\\.]*?)mr-(\\w*)-(\\w*)-?\\.png");
         Case[] cases = new Case[typeFiltered.length];
         int caseId = 0;
         for (File file : typeFiltered) {
             Matcher m = pattern.matcher(file.getName());
             m.find();
             //        1: 0              fitness             
             //        2: 1000           generation
             //        3: 75             population          needed
             //        4: 20             tournamentsize      needed
             //        5: 0.05           tournamenteps       needed
             //        6: 0.5            cr                  needed
             //        7: 0.2            mr                  needed
             //        8: GenMix        protocol            needed
             //        9: Tourna        mechanism           needed
             String fitness = m.group(1);
             String[] input = {
                 unmarshallProtocol(m.group(8)),
                 unmarshallMechanism(m.group(9)),
                 m.group(4),
                 m.group(5),
                 m.group(6),
                 m.group(7),
                 m.group(3)
             };
             cases[caseId++] = new Case(fitness, input);
         }
         Arrays.sort(cases);
         System.out.println("Best: " + cases[0].fitness);
         System.out.println("Worst: " + cases[cases.length - 1].fitness);
         Case[] retains = new Case[retain];
         System.arraycopy(cases, 0, retains, 0, retain);
         return retains;
     }
 
     public static class RunnMe implements Runnable {
 
         private int runMe;
         private Case c;
         private File dir;
         private File newDir;
 
         public RunnMe(final int runCount, final Case c, final File dir, final File newDir) {
             this.runMe = runCount;
             this.c = c;
             this.dir = dir;
             this.newDir = newDir;
         }
 
         public void run() {
             double lastTime = System.currentTimeMillis();
            try {
                App.main(c.input);
            } catch (InterruptedException ex) {
                Logger.getLogger(RerunOfBest.class.getName()).log(Level.SEVERE, null, ex);
            }
             System.out.println("TimeStep: " + (System.currentTimeMillis() - lastTime));
             File[] images = dir.getParentFile().listFiles(new FilenameFilter() {
                 public boolean accept(File dir, String name) {
                     return name.endsWith(".png") || name.equals("config.attr");
                 }
             });
             File runDir = new File(newDir, String.valueOf(runMe) + "/");
             runDir.mkdirs();
             for (File image : images) {
                 image.renameTo(new File(runDir, image.getName()));
             }
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
                     if (breaker) {
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
