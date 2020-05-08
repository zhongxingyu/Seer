 package com.ideaheap.libposg.agent;
 
 import com.ideaheap.libposg.state.Action;
 import com.ideaheap.libposg.state.Game;
 import com.ideaheap.libposg.state.Observation;
 import com.ideaheap.libposg.state.World;
 import com.ideaheap.libposg.strategy.Strategy;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * User: nwertzberger
  * Date: 4/14/13
  * Time: 3:33 PM
  * Email: wertnick@gmail.com
  * <p/>
  * The agent is the interface that must be implemented for using the simulator.
  */
 public class Agent {
     /* These three things are settings for everything else */
     private final String name;
     private final Strategy strategy;
 
     private World world;
     private Map<String, Action> actions = new HashMap<String, Action>();
     private Map<String, Observation> observations = new HashMap<String, Observation>();
     private Map<Game, Double> belief = null;
     private Integer horizon;
 
     private Map<String, Double> beliefHack; // HACK
 
     /* Runtime state stuff */
     private Set<Observation> observed = new HashSet<Observation>();
     private PolicyTreeNode policy;
 
     public Agent(String name, Strategy strategy) {
         this.name = name;
         this.strategy = strategy;
     }
 
     public void observe(Set<Observation> observations) {
         for (Observation o : observations) {
             observed.add(o);
         }
     }
 
     public void observe(Observation observation) {
         observed.add(observation);
     }
 
     public Action decideGameAction() {
         beliefHackCheck();
         if (policy != null) {
             System.out.println("Observed: " + observed);
             policy = policy.next(observed); // Grab the next action based on our horizon and the last observations.
             belief = policy.getBelief(); // sock away our current belief
         }
        policy = strategy.generateStrategy(world, this, belief, horizon); // Get policy up to horizon
         observed.clear(); // clean out current stored observations
 
         Action a = policy.getAction();
         System.out.println("Executing " + a);
         if (a.hasAct()) {
             a.act();
         }
         return a;
     }
 
     /*
      * Literally everything after this is boiler plate... Go Java!
      */
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
         Agent that = (Agent) o;
 
         if (actions != null ? !actions.equals(that.actions) : that.actions != null) return false;
         if (horizon != null ? !horizon.equals(that.horizon) : that.horizon != null) return false;
         if (name != null ? !name.equals(that.name) : that.name != null) return false;
         if (observations != null ? !observations.equals(that.observations) : that.observations != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = name != null ? name.hashCode() : 0;
         result = 31 * result + (actions != null ? actions.hashCode() : 0);
         result = 31 * result + (observations != null ? observations.hashCode() : 0);
         result = 31 * result + (horizon != null ? horizon.hashCode() : 0);
         return result;
     }
 
     @Override
     public String toString() {
         return "{" + this.name + "}";
     }
 
     public Map<String, Action> getActions() {
         return actions;
     }
 
     public Agent withHorizon(int h) {
         this.setHorizon(h);
         return this;
     }
 
     public Agent withBelief(Map<Game, Double> belief) {
         this.setBelief(belief);
         return this;
     }
 
     public String getName() {
         return name;
     }
 
     public Action getAction(String actionName) {
         return actions.get(actionName);
     }
 
     public void addAction(Action action) {
         actions.put(action.getName(), action);
     }
 
     public Agent withAction(Action a) {
         this.addAction(a);
         return this;
     }
 
     public Observation getObservation(String observationName) {
         return observations.get(observationName);
     }
 
     public void addObservation(Observation observation) {
         observations.put(observation.getName(), observation);
     }
 
     public Agent withObservation(Observation o) {
         this.addObservation(o);
         return this;
     }
 
     public void setHorizon(Integer horizon) {
         this.horizon = horizon;
     }
 
     public void setBelief(Map<Game, Double> belief) {
         this.belief = belief;
     }
 
     public void setWorld(World world) {
         this.world = world;
     }
 
     /* BEGIN HACK */
 
     public void setBeliefHack(Map<String, Double> belief) {
         beliefHack = belief;
     }
 
     private void beliefHackCheck() {
         if (belief == null) {
             Map<Game, Double> actualBelief = new HashMap<Game, Double>();
             for (String g : beliefHack.keySet()) {
                 actualBelief.put(world.getGame(g), beliefHack.get(g));
             }
             belief = actualBelief;
         }
     }
 }
