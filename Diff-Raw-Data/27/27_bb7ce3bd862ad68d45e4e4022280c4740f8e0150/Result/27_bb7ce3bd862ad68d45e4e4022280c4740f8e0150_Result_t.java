 package edu.ucsc.gameAI.hfsm;
 
 import java.util.Collection;
 import java.util.LinkedList;
 
 import edu.ucsc.gameAI.IAction;
 
 public class Result implements IResult{
     Collection<IAction> _actions;
     IHTransition _transition;
     int _level;
     
     Result(){
 	    
 	}
     
     public Collection<IAction> getActions(){
        if(_actions == null){
            _actions = new LinkedList<IAction>();
        }
         return _actions;
     }
     
     public void setActions(Collection<IAction> actions){
         _actions = actions;
     }
     
     public void addAction(IAction action){
         if(_actions == null){
             _actions = new LinkedList<IAction>();
         }
         _actions.add(action);
     }
     
     public void addActions(Collection<IAction> actions){
         Collection<IAction> actions_prime = new LinkedList<IAction>();
         for(IAction action : actions_prime){
             addAction(action);
         }
     }
     
     public IHTransition getTransition(){
         return _transition;
     }
     
     public void setTransition(IHTransition transition){
         _transition = transition;
     }
     
     public int getLevel(){
         return _level;
     }
     
     public void setLevel(int level){
         _level = level;
     }
 }
