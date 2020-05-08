 package org.faur.simulator;
 
 import org.faur.api.loader.ILoader;
 import org.faur.api.model.IPlace;
 import org.faur.api.model.ITransition;
 import org.faur.api.simulator.ISimulator;
 import org.faur.loader.PNLoader;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.Scanner;
 
 /**
  * (C) Copyright 2013 Faur Ioan-Aurel.
  * <p/>
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl-2.1.html
  * <p/>
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  * <p/>
  * Created with IntelliJ IDEA.
  * User: faur
  * Date: 10/19/13
  * Time: 7:59 PM
  */
 public class PNSimulator extends Thread implements ISimulator {
     private static final int DELAY = 2000;
     private int clock;
     private volatile boolean stoped;
     private String petriNetLocation;
     private ILoader loader;
 
 
     public PNSimulator() {
         this("");
     }
 
     public PNSimulator(String petriNetLocation) {
         this.clock = 0;
         this.stoped = false;
         this.petriNetLocation = petriNetLocation;
     }
 
     public void setPetriNetLocation(String petriNetLocation) {
         this.petriNetLocation = petriNetLocation;
     }
 
     @Override
     public int getClock() {
         return this.clock;
     }
 
     @Override
     public void execute() {
         this.clock = 0;
         this.stoped = false;
         this.loader = new PNLoader(new File(this.petriNetLocation));
         this.start();
 
     }
 
     @Override
     public void stopExecution() {
         this.stoped = true;
     }
 
     @Override
     public void resetExecution() {
         stopExecution();
         execute();
     }
 
     private void printData(List<?> data) {
         for (Object o : data) {
             System.out.print(o.toString() + " ");
         }
         System.out.println();
     }
 
     @Override
     public void run() {
         List<IPlace> places = loader.getPlaces();
         List<ITransition> transitions = loader.getTransitions();
         while (!stoped) {
             try {
                 Thread.sleep(DELAY);
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
             System.out.println("----------------------------------------------------------------------------------------");
             System.out.println("               Clock ( " + String.valueOf(this.clock) + " )");
             System.out.println("----------------------------------------------------------------------------------------");
             System.out.print("Places:");
             printData(places);
             System.out.print("Transitions:");
             printData(transitions);
             System.out.println("########################################################################################");
             clock++;
             for (ITransition transition : transitions) {
                 transition.fire();
             }
 
         }
     }
 
     private static void printMessage() {
         System.out.println("s       starts the simulator with the specified petri network");
         System.out.println("r       restarts the simulator");
         System.out.println("q       stops the simulator");
         System.out.println("e       exit from the app");
         System.out.println("clear   clears the screen");
     }
 
     public static void main(String[] args) {
         // /home/faur/Public/pnsimulator/simulator/resources/petrinet-sample.properties
         PNSimulator simulator = new PNSimulator("/home/faur/Public/pnsimulator/simulator/resources/petrinet-sample.properties");
         Scanner scanner = new Scanner(System.in);
         boolean mustExit = false;
         while (!mustExit) {
             printMessage();
             System.out.print(">>>");
             String choice = scanner.nextLine();
             if ("e".equals(choice)) {
                 mustExit = true;
                 simulator.stopExecution();
                 try {
                     simulator.join();
                 } catch (InterruptedException e) {
                     //
                 }
                 System.out.println("----------- Going to exit -----------");
             } else if ("q".equals(choice)) {
                 simulator.stopExecution();
                 System.out.println("----------- Simulator Stopped -----------");
             } else if ("r".equals(choice)) {
                 simulator.stopExecution();
                 try {
                     simulator.join();
                 } catch (InterruptedException e) {
                     //
                 }
                 simulator.resetExecution();
                 System.out.println("----------- Simulator reset -----------");
            } else if (choice.startsWith("s ")) {
                String[] data = choice.split(" ");
                simulator.setPetriNetLocation(data[1]);
                 simulator.stopExecution();
                 try {
                     simulator.join();
                 } catch (InterruptedException e) {
                     //
                 }
                 simulator.execute();
             } else if ("clear".equals(choice)) {
                 try {
                     Runtime.getRuntime().exec("cls");
                     Runtime.getRuntime().exec("clear");
 
                 } catch (IOException e) {
                     //
                 }
 
             } else {
                 System.out.println("Error: Option not valid!");
             }
         }
 
     }
 }
