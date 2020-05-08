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
 public abstract class vNode
 {
     public ArrayList<vNode> connections; 
     public ArrayList<Agent> inhabitants; 
     public double size; 
     public String name; 
    public ArrayList<vNode> map; 
     public abstract void inhabitantEnters(Agent a); 
     
     public abstract boolean inhabitantExits(Agent a, vNode v); 
     public abstract boolean addConnection(vNode v);
     public abstract void createAgents();
     public ArrayList<vNode> getMap()
     {
        if (map==null) {
             generateMap();
         }
         return map; 
     }
     public void setMap(ArrayList<vNode> m)
     {
         map = m; 
     }
     public void avoidance()
     {
         for (int i = 0; i < inhabitants.size(); i++) {
             Agent avoidAgent = inhabitants.get(i);
             int avoid=0;
             for(int j=0; i<inhabitants.size();j++)
             {
                 if(j!=i)
                 {
                     if(inhabitants.get(j).avoid(avoidAgent))
                     {
                         avoid+=1;
                     }
                 }
                 avoidAgent.updateApperance((float) avoid/inhabitants.size());
             }
 
         }
     }
     public void updateStage()
     {
         for (int a = 0; a < inhabitants.size(); a++) 
         {
             Agent agent = inhabitants.get(a);
             agent.increaseStage();
         }
     }
     public void infected()
     {
         ArrayList<Agent> infected=new ArrayList<>();
         for (int i = 0; i < inhabitants.size(); i++) {
             Agent person = inhabitants.get(i);
             if(person.isInfected()){
                 infected.add(person);
             }
             
         }
         for (int i = 0; i < inhabitants.size(); i++) {
             Agent agent = inhabitants.get(i);
             if (!agent.isInfected()) 
             {
                 double agentinfec=0;
                 for (int j = 0; j < infected.size(); j++) 
                 {
                     Agent infec = infected.get(j);
                     if (!agent.avoid(infec)) {
                         agentinfec+=infec.infectionRadios();
                     }
                 }
                 if (Math.random()>agentinfec/this.size) {
                     agent.infect(infected.get(0).virus);
                 }
             }
             
         }
         
         
     }
     
     private ArrayList<vNode> generateMap()
     {
        System.out.println("Generating Map"); 
         boolean all = false; 
         nodeQueue nVisited = new nodeQueue(); 
         nVisited.push(this);
         ArrayList<vNode> map = new ArrayList<vNode>(); 
         while (!all)
         {
           vNode current = nVisited.pull(); 
           map.add(current);
          System.out.println("Connections size is: " + current.connections.size()); 
           for (int i = 0; i < current.connections.size(); i++)
           {
              System.out.println("i is: " + i); 
               vNode c = current.connections.get(i);
               if (!map.contains(c))
               {
                   nVisited.push(c);
               }
           }
           
           if (nVisited.isEmpty()) all = true; 
           
             
         }
        System.out.println("Map created.  Map is: " + map.toString());
        System.out.println("Map sise is: " + map.size()); 
         return map; 
     }
    
     public String readOut(){
         double infected=0.0;
         for (int i = 0; i < inhabitants.size(); i++) {
             Agent agent = inhabitants.get(i);
             if (agent.isInfected()) {
                 infected+=1;
             }
         }
         return name+" has "+inhabitants.size()+" inhabitants "+infected/inhabitants.size()+"% infected/n";
     }
     
     
     public void update()
     {
         for (int i = 0; i < inhabitants.size(); i++) {
             Agent agent = inhabitants.get(i);
             agent.update();
         }
     }
     
      
 }
