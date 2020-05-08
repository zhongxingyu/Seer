 package edu.ucsc.gameAI.hfsm;
 
 import java.util.Collection;
 import java.util.LinkedList;
 
 import pacman.game.Game;
 import edu.ucsc.gameAI.IAction;
 import edu.ucsc.gameAI.fsm.ITransition;
 
 public class HFSM extends HFSMBase implements IHFSM {
     Collection<IHState> _states;
     Collection<IHTransition> _transitions;
     IHState _initialState;
     IHState _currentState;
     IAction _action;
     IAction _entryAction;
     IAction _exitAction;
     IHFSM _parent;
    
    public HFSM(){
        _states = new LinkedList<IHState>();
        _transitions = new LinkedList<IHTransition>();
    }
 	
     @Override
     public Collection<IHState> getStates() {
         // TODO Auto-generated method stub
         return _states;
     }
 
     @Override
     public void setStates(Collection<IHState> states) {
         // TODO Auto-generated method stub
         _states = states;
     }
 
     @Override
     public IAction getAction() {
         // TODO Auto-generated method stub
         return _action;
     }
 
     @Override
     public void setAction(IAction action) {
         // TODO Auto-generated method stub
         _action = action;
     }
 
     @Override
     public IAction getEntryAction() {
         // TODO Auto-generated method stub
         return _entryAction;
     }
 
     @Override
     public void setEntryAction(IAction action) {
         // TODO Auto-generated method stub
         _entryAction = action;
     }
 
     @Override
     public IAction getExitAction() {
         // TODO Auto-generated method stub
         return _exitAction;
     }
 
     @Override
     public void setExitAction(IAction action) {
         // TODO Auto-generated method stub
         _exitAction = action;
     }
 
     @Override
     public Collection<IHTransition> getTransitions() {
         // TODO Auto-generated method stub
         return _transitions;
     }
 
     @Override
     public void setTransitions(Collection<IHTransition> transitions) {
         // TODO Auto-generated method stub
         _transitions = transitions;
     }
 
     @Override
     public void addTransition(IHTransition transition) {
         // TODO Auto-generated method stub
         _transitions.add(transition);
     }
 
     @Override
     public IResult update(Game game) {
         if(_currentState == null){
             _currentState = _initialState;
         }
         IResult result = new Result();
     	
         Collection<IHTransition> transitions = _currentState.getTransitions();
         for(IHTransition trans : transitions){
             if(trans.isTriggered(game)){
                 result.setTransition(trans);
                 result.setLevel(trans.getLevel());
                 if(result.getLevel() == 0){
                     //We are in the same level
                     IHState targetState = trans.getTargetState();
                     if(_currentState.getExitAction() != null)
                         result.addAction(_currentState.getExitAction());
                     if(trans.getAction() != null)
                         result.addAction(trans.getAction());
                     if(targetState.getEntryAction() != null)
                         result.addAction(targetState.getEntryAction());
                     //Set current state to the target state
                     _currentState = targetState;
                     //Add normal action if this is a state
                     if(getAction() != null)
                         result.addAction(getAction());
                     //Need to clear the transition so it doesn't trigger again
                     result.setTransition(null);
                     break;
                 }else if(result.getLevel() > 0){
                     //It needs to go to a higher state
                     //exit our current state
                     if(_currentState.getExitAction() != null)
                         result.addAction(_currentState.getExitAction());
                     _currentState = null;
                     //decrease the number of levels to go
                     result.setLevel(result.getLevel()-1);
                     break;
                 }else{
                     //We now need to go down
                     IHState targetState = result.getTransition().getTargetState();
                     IHFSM targetMachine = targetState.getParent();
                     if(result.getTransition().getAction() != null)
                         result.addAction(result.getTransition().getAction());
                     result.addActions(targetMachine.updateDown(targetState, -result.getLevel(), game));
                     //Need to clear the transition so it doesn't trigger again
                     result.setTransition(null);
                     break;
                 }
             }
         }
         //There was no transition
         if(result.getTransition() == null){
             result = _currentState.update(game);
             if(getAction() != null)
                 result.addAction(getAction());
         }
         return result;        
     }
 
     @Override
     public Collection<IAction> updateDown(IHState state, int level, Game game) {
         Collection<IAction> actions = new LinkedList<IAction>();
         if(level > 0){
             actions = _parent.updateDown(this, level-1, game);
         }
         if(_currentState != null){
             actions.add(_currentState.getExitAction());
         }
         _currentState = state;
         actions.add(state.getEntryAction());
         
         return actions;
     }
 
     @Override
     public void setInitialState(IHState initialState) {
         // TODO Auto-generated method stub
     	_initialState = initialState;
     }
 
     @Override
     public IHState getInitialState() {
         // TODO Auto-generated method stub
         return _initialState;
     }
 
     @Override
     public IHFSM getParent() {
         // TODO Auto-generated method stub
         return _parent;
     }
 
     @Override
     public void setParent(IHFSM parent) {
         // TODO Auto-generated method stub
         _parent = parent;
     }
 }
