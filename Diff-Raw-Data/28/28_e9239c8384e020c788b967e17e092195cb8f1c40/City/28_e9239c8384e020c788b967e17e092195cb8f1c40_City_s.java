 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package viriussimulator;
 import java.util.ArrayList;
 
 /**
  *
  * @author baroba
  */
 public class City extends location 
 {
     public City (int numAgents)
     {
      System.out.println("Creating city"); 
      nodes = new ArrayList<vNode>(); 
      numStartingAgents = numAgents; 
      nodes.add(new streetNode("route 1", 0));
      nodes.add(new buildingNode("building 1", numAgents/4, numAgents/4));
      nodes.add(new buildingNode("building 2", numAgents/4, numAgents/4));
      nodes.add(new buildingNode("building 3", numAgents/4, numAgents/4));
      nodes.add(new buildingNode("building 4", numAgents/4, numAgents/4));
      System.out.println("Adding connections"); 
      for (int i = 1; i < 5; i++) 
      {
         nodes.get(0).addConnection(nodes.get(i));
         //nodes.get(i).addConnection(nodes.get(0)); 
 
      }
      System.out.println("StreetConnection size is: " + nodes.get(0).connections.size()); 
      
     ArrayList<vNode> map = nodes.get(0).getMap(); 
      for (int i = 0; i < nodes.size(); i++) 
         {
            nodes.get(i).setMap(map);
         }
      for (int i = 0; i < nodes.size(); i++) nodes.get(i).createAgents();
     }
     public  void update()
     {
         for (int i = 0; i < nodes.size(); i++) {
             vNode node = nodes.get(i);
             node.avoidance();
             node.updateStage();
             node.update();
         }
         
         for (int i = 0; i < nodes.size(); i++) {
             vNode node = nodes.get(i);
             System.out.print(node.readOut());
         }
         
     };
     public  double deadPercent()
     {
         int living=0;
         for (int i = 0; i < nodes.size(); i++) {
             vNode node = nodes.get(i);
             living+=node.inhabitants.size();
         }
         return living/numStartingAgents; 
     };
     
 }
