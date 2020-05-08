 package org.drooms.impl.logic.commands;
 
 import org.drooms.api.Collectible;
 import org.drooms.api.GameReport;
 import org.drooms.api.Player;
 import org.drooms.impl.DefaultEdge;
 import org.drooms.impl.DefaultNode;
 import org.drooms.impl.DefaultPlayground;
 import org.drooms.impl.logic.CollectibleRelated;
 import org.drooms.impl.logic.CommandDistributor;
 import org.drooms.impl.logic.DecisionMaker;
 import org.drooms.impl.logic.PlayerRelated;
 import org.drooms.impl.logic.RewardRelated;
 import org.drooms.impl.logic.events.CollectibleRewardEvent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class CollectCollectibleCommand implements
         Command<DefaultPlayground, DefaultNode, DefaultEdge>, PlayerRelated,
         CollectibleRelated, RewardRelated {
 
     private static final Logger LOGGER = LoggerFactory
             .getLogger(CollectCollectibleCommand.class);
 
     private final Collectible toCollect;
     private final Player toReward;
     private final CollectibleRewardEvent event;
 
     public CollectCollectibleCommand(final Collectible c, final Player p) {
         this.toCollect = c;
         this.toReward = p;
         this.event = new CollectibleRewardEvent(p, c);
     }
 
     @Override
     public Collectible getCollectible() {
        return this.getCollectible();
     }
 
     @Override
     public Player getPlayer() {
         return this.toReward;
     }
 
     @Override
     public int getPoints() {
         return this.toCollect.getPoints();
     }
 
     @Override
     public boolean isValid(final CommandDistributor controller) {
         return controller.hasCollectible(this.toCollect)
                 && controller.hasPlayer(this.toReward);
     }
 
     @Override
     public void perform(final DecisionMaker logic) {
         logic.notifyOfCollectibleReward(this.event);
     }
 
     @Override
     public void report(
             final GameReport<DefaultPlayground, DefaultNode, DefaultEdge> report) {
         CollectCollectibleCommand.LOGGER.info(
                 "Collectible {} collected by player {}.", this.toCollect,
                 this.toReward);
     }
 
     @Override
     public String toString() {
         final StringBuilder builder = new StringBuilder();
         builder.append("CollectCollectibleCommand [toCollect=")
                 .append(this.toCollect).append(", toReward=")
                 .append(this.toReward).append("]");
         return builder.toString();
     }
 
 }
