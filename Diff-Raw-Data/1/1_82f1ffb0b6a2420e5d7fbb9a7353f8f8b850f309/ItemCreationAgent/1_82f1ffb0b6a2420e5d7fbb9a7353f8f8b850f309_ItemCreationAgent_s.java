 package fr.utc.nf28.moka.agents;
 
 /**
  * An agent that creates items. Send a REQUEST with a creation JSON to this agent to ad an item
  */
 public class ItemCreationAgent extends MokaAgent{
 
     public void setup() {
         addBehaviour(new ItemCreationBehaviour());
     }
 }
