 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package SimpactII.TimeOperators;
 
 import SimpactII.Agents.Agent;
 import SimpactII.SimpactII;
 import sim.engine.SimState;
 import sim.engine.Steppable;
 import sim.field.network.Edge;
 import sim.util.Bag;
 
 /**
  *
  * @author Lucio Tolentino
  */
 public class TimeOperator implements Steppable{
     
     private double MAX_AGE = 65;
     
     public TimeOperator(int MAX_AGE){
         this.MAX_AGE = MAX_AGE;
     }
     
     public TimeOperator(){
         return;
     }
         
     public void step(SimState s){
         SimpactII state = (SimpactII) s;
         
         //increments agent ages
         Bag agents = state.network.getAllNodes();
         int numOthers = agents.size();
         for(int i = 0 ; i < numOthers; i++){
             //increment ages
             Agent agent = (Agent) agents.get(i);
            agent.setAge(agent.getAge() + 1);
             
             //decrement relations
             Bag relations = state.network.getEdges(agent, new Bag() );
             for(int j = 0 ; j < relations.size(); j++){
                 Edge e = (Edge) relations.get(j);
                 e.setInfo((double) e.getWeight()- 0.5 ); //decrement by 0.5 b/c this is called twice (once by each node of the edge)
                 if (e.getWeight() <= 0)
                     state.dissolveRelationship(e);               
             }
             
             //check some removal condition
             if(remove(agent)){ //if some removal condition is met
                 //somehow replace individual:
                 state.network.addNode(replace(state)); 
                 
                 //remove them from the world:
                 agent.stoppable.stop(); //stop them from being scheduled
                 relations = state.network.getEdges(agent, new Bag() );
                 for(int k = 0 ; k < relations.size(); k++){
                     Edge e = (Edge) relations.get(k);
                     state.dissolveRelationship(e); //dissolve all of their relations
                 }
                 state.network.removeNode(agent);
                 state.myAgents.remove(agent);             
                 state.world.remove(agent);
             } //end if statement
         }//end for loop
     }
     
     public boolean remove(Agent agent){
         return agent.getAge() > MAX_AGE;        
     }
     
     public Agent replace(SimpactII state){
         return state.addAgent();
     }
 
 
 
    
 }
