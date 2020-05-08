 package com.ideaheap.libposg.strategy;
 
 import com.ideaheap.libposg.agent.Agent;
 import com.ideaheap.libposg.agent.PolicyTreeNode;
 import com.ideaheap.libposg.state.Game;
 
 import java.util.Map;
 
 /**
  * User: nwertzberger
  * Date: 4/27/13
  * Time: 3:20 PM
  */
 public interface Strategy {
    public PolicyTreeNode generateStrategy(Agent me, Map<Game, Double> belief, int horizon);
 }
