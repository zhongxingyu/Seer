 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package viriussimulator;
 
 /**
  *
  * @author baroba
  */
 import java.util.ArrayList; 
 import java.util.*; 
 public class Agent 
 {
     vNode location; 
     ArrayList<vNode> route;
     ArrayList<vNode> schedule; 
     String name; 
     boolean infected;
     int stage;
     double apearence;
     Virus virus;
     double avoidance=.9;
     int scheduleplace; 
     public Agent(String n, vNode start)
     {
         name = n; 
         location = start; 
         route = new ArrayList<vNode>(); 
         schedule = new ArrayList<vNode>(); 
         schedule = generateSchedule(); 
     }
     
     public void update()
     {
         if (location.name.equals(route.get(0).name) && schedule.contains(location))
         {
             scheduleplace++; 
             if (scheduleplace > schedule.size())
             {
                 scheduleplace = 0; 
             }
             if (!schedule.get(scheduleplace).name.equals(location.name))
             {
                 route = this.findRoute(schedule.get(scheduleplace)); 
                 vNode next = route.get(0); 
                 route.remove(0); 
                 changeLocation(next); 
                 return; 
             }
             
         
         }
         
         
         vNode next = route.get(0); 
         changeLocation(next); 
         
     }
     public boolean changeLocation(vNode v)
     {
         if (!location.connections.contains(v)) return false; 
         return location.inhabitantExits(this, v); 
     }
     
     private ArrayList<vNode> generateSchedule()
     {
          
         ArrayList<vNode> s = new ArrayList<vNode>(); 
         ArrayList<vNode> map = location.getMap();
        System.out.println("Map size is: " + map.size());
         Random num = new Random(); 
        scheduleplace = num.nextInt(10); 
         for (int i = 0; i < scheduleplace; i++)
         {
             int next = num.nextInt(map.size()); 
             for (int x = 0; x < 4; x ++)
             {
                 s.add(map.get(next));
             }
         }
         return s; 
     }
     
     
     
     
     public ArrayList<vNode> findRoute(vNode v)
     {
         nodeQueue nVisited = new nodeQueue();
         ArrayList<vNode> visited = new ArrayList(); 
         boolean found = false;
         vNode current = location;
         nVisited.push(current);
         while (!found)
         {
             
             if (current.name.equals(v.name))
             {
                 return nVisited.toArrayList(); 
             }
             else 
             {
                 visited.add(current);
                 for (int i = 0; i < current.connections.size(); i++)
                 {
                     vNode c = current.connections.get(i);
                     if (!visited.contains(c))
                     {
                         nVisited.push(c);
                        current = nVisited.pull();
                        break; 
                     }
                 }
                 
             }
                 
         }
         return null; 
     }
     public void infect(Virus vir)
     {
         infected=true;
         stage=1;
         virus=vir;
     }
     public boolean isInfected()
     {
         return infected;
     }
     public Virus infection(){
         return virus;
     }
     public void increaseStage()
     {
         if (infected) 
         {
         stage+=1;    
         }
         
     }
     public double getApperance()
     {
         return apearence;
     }
     public boolean avoid(Agent person)
     {
         if (person.getApperance()<avoidance) {
             return true;
         }
         return false;
     }
     public void updateApperance(double avoidingPercent)
     {
         if (infected) 
         {
             apearence=virus.stageApperence(stage)-avoidingPercent;
          
         }
     }
     
     public boolean isDead()
     {
         return virus.agentDeath(this);
     }
     public double infectionRadios()
     {
             if (infected) {
                 return virus.infectionArea(stage);
             }
             return 0;
     }
     
     
     
 
 }
