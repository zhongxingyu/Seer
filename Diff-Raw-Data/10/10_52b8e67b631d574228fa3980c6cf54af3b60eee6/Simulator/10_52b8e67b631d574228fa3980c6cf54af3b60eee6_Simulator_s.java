 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package lmp;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 /**
  *
  * @author T-CATS
  */
 public class Simulator {
     private ArrayList<Node2> fred;
     private Double lambda;
     private ArrayList collision;
     private int successfulPackets;
     private double sentTime;
     
     Simulator(int numOfStations, Double lambda)
     {
         sentTime = 0;
         successfulPackets=0; // Counts tne number of packets successfully sent
         fred = new ArrayList(); // Hello fred!
         collision = new ArrayList(); // Keeps track of collisions
         this.lambda = lambda;
         for(int i=1;i<=numOfStations;i++)
            fred.add(new Node2(lambda)); // Create new Nodes initilzed with time poisson()
        // Sort fred
        Collections.sort(fred);
     }
     
     /**
      * Attempts to send.
      * @return Returns the time of the successful transmission, else, returns -1
      */
     public Double send()
     {
         for(int i=0;i<fred.size();i++)
         {
             fred.get(i).send(lambda); // Send!
             for(int j= i+1;j<fred.size();j++)
             {
                 fred.get(j).send(lambda); // Send!
                 if(Math.abs(fred.get(i).getTime()-fred.get(j).getTime())<=1) 
                 {
                     // System.out.println("Collision!"); // Oh noes!
                     fred.get(j).collide();
                     fred.get(i).collide();
                     collision.add(fred.get(j)); // Add to a running list of collisions
                     collision.add(fred.get(i)); // Add to a running list of collisions
                    
                 }
             }
             if(collision.isEmpty())
             {
                 successfulPackets++; // Increase the count of sucessful packets
                 sentTime = fred.get(i).getTime();
                 return fred.get(i).getTime();
             }
         }
         collision.clear();
         return -1.0;
     }
     
     public double getSentTime()
     {
         return sentTime;
     }
     
     public int getSuccessfulPacketsSent()
     {
         return successfulPackets;
     }
     
     public void clearSuccessfulPacketsSent()
     {
         successfulPackets = 0;
     }
     
     public double tryAgain()
     {
         // Add collisions back into the running nodes
         // fred.addAll(collision);
         Collections.sort(fred);
         double result=-1;
         while(result==-1)
         {
             result = send();
         }
         return result;
     }
 }
