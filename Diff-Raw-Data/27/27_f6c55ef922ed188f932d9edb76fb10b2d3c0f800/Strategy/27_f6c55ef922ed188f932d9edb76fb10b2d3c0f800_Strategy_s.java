 package com.ideaheap.libposg.strategy;
 
 import com.ideaheap.libposg.agent.Agent;
 import com.ideaheap.libposg.agent.PolicyTreeNode;
 import com.ideaheap.libposg.state.Game;
import com.ideaheap.libposg.state.World;
 
 import java.util.Map;
 
 /**
  * User: nwertzberger
  * Date: 4/27/13
  * Time: 3:20 PM
  */
 public interface Strategy {
    public PolicyTreeNode generateStrategy(World w, Agent me, Map<Game, Double> belief, int horizon);
 }
