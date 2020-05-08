 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mas;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  *
  * @author jelmer
  */
 public class Table
 {
     private ArrayList<Agent> agents;
     
     private Set<Listener> listeners;
     
     public static interface Listener
     {
         public void tableChanged(Table table);
         
         public void tableHasWinner(Table table, Agent winner);
     }
     
     public Table(int numberOfAgents)
     {
         listeners = new HashSet<Listener>();
         
         initializeAgents(numberOfAgents);
     }
     
     public void playRound()
     {
         ArrayList<Card> passedOnCards = new ArrayList<Card>();
         
         // Collect the cards all the agents want to pass to their neighbours.
         for (Agent agent : agents)
             passedOnCards.add(agent.giveCard());
 
         // Give them to their neighbours.
         for (int i = 0; i < passedOnCards.size(); ++i)
             agents.get((i + 1) % agents.size()).receiveCard(passedOnCards.get(i));
 
         // Do we have a winner?
         for (Agent agent : agents)
            if (agent.hasFourOfAKind()) {
                 win(agent);
                return;
            }
         
         // Tell all the agents we are going to the next round, allowing them to
         // deliberate about the other agents that they have passed cards around.
         for (Agent agent : agents)
             agent.nextRound();
     }
     
     public final ArrayList<Agent> getAgents()
     {
         return agents;
     }
     
     public final Agent getPlayerLeftOf(Agent agent)
     {
         int pos = agents.indexOf(agent) - 1;
         
         if (pos < 0)
             pos = agents.size() - 1;
         
         return agents.get(pos);
     }
     
     public final Agent getPlayerRightOf(Agent agent)
     {
         int pos = agents.indexOf(agent) + 1;
         
         if (pos >= agents.size())
             pos = 0;
         
         return agents.get(pos);
     }
     
     
     private void initializeAgents(int numberOfAgents)
     {
         agents = new ArrayList<Agent>();
         
         ArrayList<Card> cards = Card.getDeck();
         Collections.shuffle(cards);
         
         // Because our Card.getDeck still gives only 16 cards:
         assert(numberOfAgents == 4);
         
         for (int i = 0; i < numberOfAgents; ++i) {
             ArrayList<Card> agentCards = new ArrayList<Card>(cards.subList(0, 4));
             cards.subList(0, 4).clear();
             
             agents.add(new Agent(agentCards, "Agent " + (i+1), new Strategy(), this));
         }
         
         for (Agent agent : agents)
             agent.initialize();
         
         notifyListeners();
     }
     
     private void win(Agent agent)
     {
         System.out.println("Agent " + agent + " wins!");
         // :D
         
         for (Listener listener : listeners)
             listener.tableHasWinner(this, agent);
         
         initializeAgents(4);
     }
     
     /* Listeners */
     
     public void addListener(Listener listener)
     {
         listeners.add(listener);
     }
     
     public void removeListener(Listener listener)
     {
         listeners.remove(listener);
     }
     
     private void notifyListeners()
     {
         for (Listener listener : listeners)
             listener.tableChanged(this);
     }
 }
