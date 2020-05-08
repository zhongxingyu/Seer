 package edu.ucsc.gameAI.hfsm;
 
 import pacman.game.Game;
 import edu.ucsc.gameAI.IAction;
 import edu.ucsc.gameAI.ICondition;
import edu.ucsc.gameAI.conditions.PacmanLastMove;
 
 public class HTransition implements IHTransition {
     IHState _targetState;
     IAction _action;
     ICondition _condition;
     int _level;
     
    public HTransition(IHState state, ICondition condition){
        setTargetState(state);
        setCondition(condition);
     }
 
     @Override
     public IHState getTargetState() {
         return _targetState;
     }
 
     @Override
     public void setTargetState(IHState targetState) {
         _targetState = targetState;
     }
 
     @Override
     public IAction getAction() {
         return _action;
     }
 
     @Override
     public void setAction(IAction action) {
         _action = action;
     }
 
     @Override
     public void setCondition(ICondition condition) {
         _condition = condition;
     }
 
     @Override
     public boolean isTriggered(Game game) {
         return _condition.test(game);
     }
 
     @Override
     public int getLevel() {
         return _level;
     }
 
     @Override
     public void setLevel(int level) {
         _level = level;
     }
 
 }
