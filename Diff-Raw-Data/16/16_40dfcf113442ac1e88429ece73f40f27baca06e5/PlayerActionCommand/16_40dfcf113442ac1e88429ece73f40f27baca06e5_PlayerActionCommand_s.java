 package org.drooms.impl.logic.commands;
 
 import java.util.Deque;
 
 import org.drooms.api.Action;
 import org.drooms.api.GameProgressListener;
 import org.drooms.api.Node;
 import org.drooms.api.Player;
 import org.drooms.impl.logic.DecisionMaker;
 import org.drooms.impl.logic.PlayerRelated;
 import org.drooms.impl.logic.events.PlayerActionEvent;
 
 public class PlayerActionCommand implements Command, PlayerRelated {
 
     private final Player actor;
     private final Action action;
     private final Deque<Node> nodes;
     private final PlayerActionEvent event;
 
     public PlayerActionCommand(final Player p, final Action a, final Deque<Node> nodes) {
         this.actor = p;
         this.action = a;
         this.nodes = nodes;
         this.event = new PlayerActionEvent(p, a, nodes);
     }
 
     public Deque<Node> getNodes() {
         return this.nodes;
     }
 
     @Override
     public Player getPlayer() {
         return this.actor;
     }
 
     @Override
     public void perform(final DecisionMaker logic) {
         logic.notifyOfPlayerMove(this.event);
     }
 
     @Override
     public void report(final GameProgressListener report) {
        report.playerMoved(this.actor, this.action,
                 this.nodes.toArray(new Node[]{}));
     }
 
     @Override
     public String toString() {
         final StringBuilder builder = new StringBuilder();
         builder.append("MovePlayerCommand [actor=").append(this.actor)
                 .append(", action=").append(this.action)
                 .append(", nodes=").append(this.nodes).append("]");
         return builder.toString();
     }
 
 }
