 package edu.ucsc.gameAI.hfsm;
 
 import java.util.Collection;
 import java.util.LinkedList;
 
 import pacman.game.Game;
 import edu.ucsc.gameAI.IAction;
 
 public class HState extends HFSMBase implements IHState{
     IAction _entryAction;
     IAction _exitAction;
     Collection<IHTransition> _transitions;
     Collection<IHState> _states;
     IHFSM _parent;
     
    HState(){
         
     }
 
     @Override
     public IResult update(Game game) {
         IResult result = super.update(game);
         return result;
     }
 
     @Override
     public Collection<IHState> getStates() {
         Collection<IHState> temp = new LinkedList<IHState>();
         temp.add(this);
         return temp;
     }
 
     @Override
     public void setStates(Collection<IHState> states) {
         // I have no clue why this is even a thing... A state doesn't have other states in it. Only a HFSM has other states in it
         _states = states;
     }
 
     @Override
     public IAction getAction() {
         return _currentAction;
     }
 
     @Override
     public void setAction(IAction action) {
         _currentAction = action;
     }
 
     @Override
     public IAction getEntryAction() {
         return _entryAction;
     }
 
     @Override
     public void setEntryAction(IAction action) {
         _entryAction = action;
     }
 
     @Override
     public IAction getExitAction() {
         return _exitAction;
     }
 
     @Override
     public void setExitAction(IAction action) {
         _exitAction = action;
     }
 
     @Override
     public Collection<IHTransition> getTransitions() {
         if(_transitions == null){
             _transitions = new LinkedList<IHTransition>();
         }
         return _transitions;
     }
 
     @Override
     public void setTransitions(Collection<IHTransition> transitions) {
         _transitions = transitions;
     }
 
     @Override
     public void addTransition(IHTransition transition) {
         if(_transitions == null){
             _transitions = new LinkedList<IHTransition>();
         }
         _transitions.add(transition);
     }
 
     @Override
     public IHFSM getParent() {
         return _parent;
     }
 
     @Override
     public void setParent(IHFSM parent) {
         _parent = parent;
     }
 
 }
