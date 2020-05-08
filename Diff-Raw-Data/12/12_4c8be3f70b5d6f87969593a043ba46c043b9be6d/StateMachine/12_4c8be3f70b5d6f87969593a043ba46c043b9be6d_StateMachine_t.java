 package edu.ucsc.gameAI.fsm;
 
 import java.util.ArrayList;
 import java.util.Collection;
import java.util.LinkedList;
 
 import pacman.game.Game;
 import edu.ucsc.gameAI.IAction;
 
 /**
  * The interface for a state machine. This Interface must be extended for 
  * state machine implementations to interface wit automatic testing and 
  * grading. 
  * @author Josh McCoy
  *
  */
 public class StateMachine implements IStateMachine {
     private IState _currentState;
     private IState _initialState;
     
     /**
      * 
      * @param initialState
      */
     public StateMachine(IState initialState){
         _initialState = initialState;
     }
     public StateMachine(){
     }
     
     /**
      * The member function that performs the update on the FSM:
      * - Test transitions for current state and moves to new state.
      * - Returns a collection of IActions that result from the current
      *   state and any transitions, entrances and exits that may occur.
      * @return A collection of actions produced by evaluating the FSM.
      */
     public Collection<IAction> update(Game game){
        
        Collection<IAction> actions = new LinkedList<IAction>();
         Collection<ITransition> transitions = _currentState.getTransitions();
         for(ITransition trans : transitions){
             if(trans.isTriggered(game)){
                 //Add the current state's exit action
                 actions.add(_currentState.getExitAction());
                 //Add the transition action
                 actions.add(trans.getAction());
                 //Set the current state to the new state
                 _currentState = trans.getTargetState();
                 //Add the entry action from the current state
                 actions.add(_currentState.getEntryAction());
                 break;
             }
         }
         //In any case, we need to execute the action of the currentState, whether it was replaced
         //or not.
         actions.add(_currentState.getAction());
         return actions;
     }
     
     /**
      * Retrieves the current state of the finite state machine.
      * @return The current state of the finite state machine.
      */
     public IState getCurrentState(){
         return _currentState;
     }
 
     @Override
     public void setCurrentState(IState state) {
         // TODO Auto-generated method stub
         _currentState = state;
     }
 }
