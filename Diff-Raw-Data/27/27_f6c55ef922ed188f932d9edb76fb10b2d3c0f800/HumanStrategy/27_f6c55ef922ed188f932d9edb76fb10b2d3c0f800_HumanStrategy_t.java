 package com.ideaheap.libposg.strategy;
 
 import com.ideaheap.libposg.agent.Agent;
 import com.ideaheap.libposg.agent.PolicyTreeNode;
 import com.ideaheap.libposg.state.Action;
 import com.ideaheap.libposg.state.Game;
 
 import java.util.Map;
 import java.util.Scanner;
 
 /**
  * User: nwertzberger
  * Date: 4/14/13
  * Time: 9:25 PM
  * Email: wertnick@gmail.com
  */
 public class HumanStrategy implements Strategy {
 
     private Map<String, Action> actions;
 
     @Override
    public PolicyTreeNode generateStrategy(Agent me, Map<Game, Double> belief, int horizon) {
         System.out.println("Please generate your strategy");
         actions = me.getActions();
         Action chosenAction = null;
         Scanner scan = new Scanner(System.in);
         while (chosenAction == null) {
             System.out.print("Action? " + actions.keySet() + " > ");
             chosenAction = actions.get(scan.nextLine().trim());
         }
         return new PolicyTreeNode(chosenAction, 1.0, belief);
     }
 }
