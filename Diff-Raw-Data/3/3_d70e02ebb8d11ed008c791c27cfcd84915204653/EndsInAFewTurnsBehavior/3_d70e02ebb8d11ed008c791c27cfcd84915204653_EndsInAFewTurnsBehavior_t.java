 package org.toj.dnd.irctoolkit.game.battle.behavior;
 
 import org.toj.dnd.irctoolkit.game.battle.Combatant;
 import org.toj.dnd.irctoolkit.game.battle.State;
 
 public class EndsInAFewTurnsBehavior implements StateBehavior {
 
     private int endOnRound;
     private State controllsState;
 
     public EndsInAFewTurnsBehavior(State controlls) {
         this.controllsState = controlls;
         this.endOnRound =
             controllsState.getAppliedOnRound()
                 + Integer.parseInt(this.controllsState.getEndCondition());
     }
 
     @Override
     public String onTurnStart(int round, Combatant current, Combatant owner) {
         int roundsLeft = Integer.parseInt(this.controllsState.getEndCondition());
 
         if ((endOnRound < roundsLeft + round) && current.getInit() <= this.controllsState.getAppliedOnInit()) {
             roundsLeft--;
             this.controllsState.setEndCondition(String.valueOf(roundsLeft));
        } else if (endOnRound < roundsLeft + round - 1) {
            roundsLeft -= roundsLeft + round - endOnRound - 1;
            this.controllsState.setEndCondition(String.valueOf(roundsLeft));
         }
 
         if (roundsLeft <= 0) {
             owner.removeState(controllsState);
             return new StringBuilder("[").append(owner.getName())
                 .append("]ϵ[").append(controllsState.getName()).append("]Чʧˡ")
                 .toString();
         }
         return null;
     }
 
     @Override
     public String onTurnEnd(int round, Combatant current, Combatant owner) {
         return null;
     }
 }
