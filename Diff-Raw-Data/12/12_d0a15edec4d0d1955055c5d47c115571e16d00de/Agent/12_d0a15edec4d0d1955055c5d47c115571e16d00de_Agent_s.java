 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mas;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  *
  * @author Jelmer & Inge
  */
 public class Agent {
     
     private Model model;
     
     private Set<Card> hand;
     
     private Strategy strategy;
     
     private Set<Listener> listeners;
     
     private String name;
     
     private Table table;
     
     private CardTypeCounter counter;
     
     public static interface Listener
     {
         public void agentChanged(Agent agent);
     }
     
     public Agent(Collection<Card> cards, String name, Strategy strategy, Table table)
     {
         this.hand = new HashSet<Card>(cards);
         
         this.listeners = new HashSet<Listener>();
         
         this.name = name;
         
         this.strategy = strategy;
         
         this.model = new Model();
         
         this.table = table;
         
         this.counter = new CardTypeCounter();
     }
     
     public void initialize()
     {
         strategy.changeStrategy(hand);
         
         // Every card that I do not have, may be in the hand of each of the other players
         for (Card card : Card.getDeck())
         {
             if (!hand.contains(card))
             {
                 for (Agent agent : table.getAgents())
                     if (!agent.equals(this))
                         model.add(new Predicate(Predicate.Operator.M, agent, "HasCard", card));
             }
         }
         
         refreshKnowledge();
     }
     
     public void receiveCard(Card card)
     {
         // Add card to our hand
         hand.add(card);
         assert(hand.size() == 4);
         
         // Tell model that we received a card from our left neighbour
         model.add(new Predicate(Predicate.Operator.K, this, "HasCard", card));
         
         // Tell the model that if the player left of me gives my a card of type X, he is probably not collecting those.
         model.remove(new Predicate(Predicate.Operator.M, table.getPlayerLeftOf(this), "Collects", card.getType()));
         
         counter.add(card.getType());
         
         // Update the window.
         notifyListeners();
     }
     
     public Card giveCard()
     {
         // Ask strategy which card to pass on
         Card card = strategy.decide(model, counter, hand);
         
         // Remove card from my hand
         hand.remove(card);
         
         // Tell model what we now know about our right neighbour
         model.add(new Predicate(Predicate.Operator.K, table.getPlayerRightOf(this), "HasCard", card));
         
         // Tell all our listeners that we know things ;) ;)
         notifyListeners();
         
         return card;
     }
     
     public void nextRound()
     {
         // Tell the model that we are entering a next round, and that the other
         // agents have also passed their cards around.        
         for (Predicate pred : model.getPredicates("HasCard")) {
             switch (pred.getOperator()) {
                 case K:
                     // Only for the other agents, not for myself because I know which cards I have
                     if (!pred.getAgent().equals(this)) {
                         model.remove(pred);
                         model.add(new Predicate(Predicate.Operator.M, pred.getAgent(), "HasCard", pred.getArgument()));
                         model.add(new Predicate(Predicate.Operator.M, table.getPlayerRightOf(pred.getAgent()), "HasCard", pred.getArgument()));
                     }
                     break;
                     
                 case M:
                     // Again, not for myself as I know which cards I have
                     if (!table.getPlayerRightOf(pred.getAgent()).equals(this)) {
                         model.add(new Predicate(Predicate.Operator.M, table.getPlayerRightOf(pred.getAgent()), "HasCard", pred.getArgument()));
                     }
                     break;
             }
         }
         
         refreshKnowledge();
         
         model.tick();
     }
     
     private void refreshKnowledge()
     {
         // If a card is by everyone now, and I still haven't received it, maybe someone is collecting it
         for (Card card : Card.getDeck())
         {
             // If I have the card myself, don't even bother (this shouldn't be
             // necessary since then other agents cannot maybe have it)
             if (hand.contains(card))
                 continue;
             
             boolean everyoneMayHaveCard = true;
             
             for (Agent agent : table.getAgents()) {
                 if (!agent.equals(this) && !model.contains(new Predicate(Predicate.Operator.M, agent, "HasCard", card))) {
                     everyoneMayHaveCard = false;
                     break;
                 }
             }
             
             if (everyoneMayHaveCard) {
                 for (Agent agent : table.getAgents())
                     if (!agent.equals(this))
                         model.add(new Predicate(Predicate.Operator.M, agent, "Collects", card.getType()));
             }
         }
     }
     
     public boolean hasFourOfAKind()
     {
         Card.Type type = hand.iterator().next().getType();
         
         for (Card card : hand) {
             if (type != card.getType())
                 return false;
         }
         
         return true;
     }
     
     /* Methods to get to know the agent better */
     
     public Set<Card> getHand()
     {
         return hand;
     }
     
     public String getName()
     {
         return name;
     }
     
     public Model getModel()
     {
         return model;
     }
     
     @Override
     public String toString()
     {
         return getName();
     }
     
     public String getThoughts()
     {
         StringBuilder out = new StringBuilder();
         
         out.append("I am collecting " + strategy.getCollectedType() + "\n\n");
         
         out.append(model.toString());
         
         return out.toString();
     }
     
     /* Methods to shout and stuff */
     
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
             listener.agentChanged(this);
     }
 }
