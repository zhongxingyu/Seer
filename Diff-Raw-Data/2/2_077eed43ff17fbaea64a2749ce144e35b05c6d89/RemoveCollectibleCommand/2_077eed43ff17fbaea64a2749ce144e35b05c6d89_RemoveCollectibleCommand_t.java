 package org.drooms.impl.logic.commands;
 
 import org.drooms.api.Collectible;
 import org.drooms.api.GameReport;
 import org.drooms.impl.DefaultEdge;
 import org.drooms.impl.DefaultNode;
 import org.drooms.impl.DefaultPlayground;
 import org.drooms.impl.logic.CollectibleRelated;
 import org.drooms.impl.logic.CommandDistributor;
 import org.drooms.impl.logic.DecisionMaker;
 import org.drooms.impl.logic.events.CollectibleRemovalEvent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class RemoveCollectibleCommand implements
         Command<DefaultPlayground, DefaultNode, DefaultEdge>,
         CollectibleRelated {
 
     private static final Logger LOGGER = LoggerFactory
             .getLogger(RemoveCollectibleCommand.class);
 
     private final Collectible toRemove;
     private final CollectibleRemovalEvent event;
 
     public RemoveCollectibleCommand(final Collectible c) {
         this.toRemove = c;
         this.event = new CollectibleRemovalEvent(c);
     }
 
     @Override
     public Collectible getCollectible() {
        return this.toRemove;
     }
 
     @Override
     public boolean isValid(final CommandDistributor controller) {
         return controller.hasCollectible(this.toRemove);
     }
 
     @Override
     public void perform(final DecisionMaker logic) {
         logic.notifyOfCollectibleRemoval(this.event);
     }
 
     @Override
     public void report(
             final GameReport<DefaultPlayground, DefaultNode, DefaultEdge> report) {
         RemoveCollectibleCommand.LOGGER.info("Collectible {} removed.",
                 this.toRemove);
     }
 
     @Override
     public String toString() {
         final StringBuilder builder = new StringBuilder();
         builder.append("RemoveCollectibleCommand [toRemove=")
                 .append(this.toRemove).append("]");
         return builder.toString();
     }
 
 }
