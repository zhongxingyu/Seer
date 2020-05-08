 package com.ideaheap.libposg.strategy;
 
 import com.ideaheap.libposg.agent.Agent;
 import com.ideaheap.libposg.agent.PolicyTreeNode;
 import com.ideaheap.libposg.state.*;
 import com.ideaheap.util.Permutations;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * User: nwertzberger
  * Date: 4/17/13
  * Time: 8:49 PM
  * Email: wertnick@gmail.com
  * <p/>
  * This class generates a tree of horizon depth based on the current belief.
  */
 public class PomdpStrategy implements Strategy {
     private Agent agent = null;
 
     /**
      * @param games
      * @param beliefMap
      * @param horizon
      */
     @Override
    public PolicyTreeNode generateStrategy(World w, Agent me, Map<Game, Double> belief, int horizon) {
         agent = me;
         PolicyTreeNode node = generatePolicyTreeNode(belief, horizon);
         return node;
     }
 
     /**
      * Depth (horizon) limited search of actions and observations to generate
      * Expected values for different sets of belief.
      * <p/>
      * s = the current game
      * a = the action
      * o = the observation
      * P(,) = probability
      * R(,) = reward
      * <p/>
      * 0: if horizon = 0, return null
      * 1: Normalize incoming belief vector
      * 2: Find best action available. For all actions:
      * 2.1. For all games:
      * a. for all observations
      * i. Update belief for this.
      * 2.2. Normalize all action - observation combo beliefs.
      * 2.3. For all games:
      * a. Calculate expected value of this action given our belief vector. (b(s) * R(s,a)
      * b. Calculate the new belief vector based on this action. (b^a)
      * c. Go through every possible observation
      * i. calculate the probability of this observation (P(o|s,a)). HOLD ON TO THIS
      * ii. calculate a new belief vector based on this observation.
      * iii. call this function at step 0, using our new belief vector and horizon - 1.
      * d. Test to see if action is worth saving. If not, trash it.
      *
      * @param belief
      * @param horizon
      * @return
      */
     PolicyTreeNode generatePolicyTreeNode(Map<Game, Double> initialBelief, int horizon) {
         if (horizon == 0) return null; // Task 0: early quit
         Map<Game, Double> belief = new HashMap<Game, Double>(initialBelief);
         normalizeBelief(belief); // Task 1: normalize
 
         Action bestAction = null;
         Double bestValue = null;
         Map<Set<Observation>, PolicyTreeNode> bestTransitions = null;
 
         for (Action currentAction : agent.getActions().values()) {
             // Given: s -> (i,a)* -> A -> R -> (T, P(T|s,a)) -> (s', o, P(o|T))
             // We want b^{a,o}
             // We want P(o|s,a)
 
             Map<Set<Observation>, Map<Game, Double>> currentActionBelief = getActionBelief(belief, currentAction);
             Map<Set<Observation>, PolicyTreeNode> currentTransitions = calculateTransitions(currentActionBelief, horizon);
             Double actionValue = calculateExpectedActionValue(belief, currentAction);
             Double transitionValue = calculateExpectedTransitionValue(currentTransitions, currentActionBelief);
             Double currentValue = actionValue + transitionValue;
 
             if (bestValue == null || bestValue < currentValue) {
                 bestAction = currentAction;
                 bestValue = currentValue;
                 bestTransitions = currentTransitions;
             }
         }
 
         return new PolicyTreeNode(bestAction, bestValue, belief).withTransitions(bestTransitions);
     }
 
     private Double calculateExpectedActionValue(Map<Game, Double> belief, Action currentAction) {
         Double actionValue = 0.0;
         for (Game g : belief.keySet()) {
             Double pGame = belief.get(g);
             Set<JointAction> jointActions = g.getJointActionsWithAgentAction(agent, currentAction);
             for (JointAction ja : jointActions) {
                 actionValue += pGame * ja.getAgentRewards().get(agent) / jointActions.size();
             }
         }
         return actionValue;
     }
 
     private Double calculateExpectedTransitionValue(
             Map<Set<Observation>, PolicyTreeNode> currentTransitions,
             Map<Set<Observation>, Map<Game, Double>> currentActionBelief) {
         Double expectedValue = 0.0;
         for (Set<Observation> obs : currentTransitions.keySet()) {
             PolicyTreeNode node = currentTransitions.get(obs);
             Double expectedNodeValue = node == null ? 0.0 : node.getExpectedValue();
             Double expectedNodeProbability = 0.0;
             for (Double gameProb : currentActionBelief.get(obs).values()) {
                 expectedNodeProbability += gameProb;
             }
             expectedValue += expectedNodeValue * expectedNodeProbability;
         }
         return expectedValue;
     }
 
     private Map<Set<Observation>, PolicyTreeNode> calculateTransitions(Map<Set<Observation>, Map<Game, Double>> currentActionBelief, int horizon) {
         Map<Set<Observation>, PolicyTreeNode> nodeTransitions = new HashMap<Set<Observation>, PolicyTreeNode>();
         for (Set<Observation> obs : currentActionBelief.keySet()) {
             Map<Game, Double> newBelief = currentActionBelief.get(obs);
             PolicyTreeNode newNode = generatePolicyTreeNode(newBelief, horizon - 1);
             nodeTransitions.put(obs, newNode);
         }
         return nodeTransitions;
     }
 
     /**
      * Given a starting belief and an action,
      * Return a map of
      * o -> s' -> P(s',o|s,a)*b(s)
      *
      * @param belief
      * @param currentAction
      * @return
      */
     private Map<Set<Observation>, Map<Game, Double>> getActionBelief(Map<Game, Double> belief, Action currentAction) {
         Map<Set<Observation>, Map<Game, Double>> observationProbabilities = new HashMap<Set<Observation>, Map<Game, Double>>();
         for (Game g : belief.keySet()) {
             Double pGame = belief.get(g);
             for (JointAction ja : g.getJointActionsWithAgentAction(agent, currentAction)) {
                 for (Transition t : ja.getTransitions().keySet()) {
                     Double pTransition = ja.getTransitions().get(t);
                     for (Set<Observation> obs : Permutations.of(t.getAgentObservationProbabilities(agent).keySet())) {
                         Double pObservation = calculateObservationProbability(obs, t);
                         addObservationProbability(observationProbabilities, t.getDestGame(), obs, pGame * pTransition * pObservation);
                     }
                 }
             }
         }
         return observationProbabilities;
     }
 
     private void addObservationProbability(Map<Set<Observation>, Map<Game, Double>> observationProbabilities, Game g, Set<Observation> obs, double v) {
         if (!observationProbabilities.containsKey(obs))
             observationProbabilities.put(obs, new HashMap<Game, Double>());
         if (!observationProbabilities.get(obs).containsKey(g))
             observationProbabilities.get(obs).put(g, 0.0);
         Double acc = observationProbabilities.get(obs).get(g);
         observationProbabilities.get(obs).put(g, acc + v);
     }
 
     private Double calculateObservationProbability(Set<Observation> observedSet, Transition t) {
         Double observationProbability = 1.0;
         for (Observation o : t.getAgentObservationProbabilities(agent).keySet()) {
             Double p = t.getAgentObservationProbabilities(agent).get(o);
             observationProbability *= observedSet.contains(o) ? p : 1.0 - p;
         }
         return observationProbability;
     }
 
     public void normalizeBelief(Map<Game, Double> belief) {
         Double total = 0.0;
         for (Double bi : belief.values()) total += bi;
         for (Game g : belief.keySet()) belief.put(g, belief.get(g) / total);
     }
 
 }
