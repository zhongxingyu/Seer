 package com.inspedio.entity.actions;
 
 import com.inspedio.entity.InsAction;
 import com.inspedio.entity.InsBasic;
 import com.inspedio.entity.primitive.InsCallback;
 
 /**
  * Execute Several Action in Sequence.<br>
  * For example if you want to scale followed by fading it out.<br>
  * 
  * @author Hyude
  * @version 1.0
  */
 public class Sequence extends InsAction{
 
 	protected InsAction[] actions;
 	protected int currentAction;
 	
 	protected Sequence(InsAction[] ActionList, InsCallback Callback){
 		super(0, Callback);
 		this.actions = ActionList;
 		this.currentAction = 0;
 	}
 	
 	public static Sequence create(InsAction[] ActionList, InsCallback Callback){
 		return new Sequence(ActionList, Callback);
 	}
 	
 	public static Sequence create(InsAction[] ActionList){
 		return create(ActionList, null);
 	}
 	
 	public int act() {
 		if(this.actions.length > 0){
 			if(this.currentAction < this.actions.length - 1){
 				this.remainingCount = this.actions[this.currentAction].act();
 				if(remainingCount == -1){
 					this.currentAction++;
 					this.remainingCount = 1;
 				}
 			} else if(this.currentAction == (this.actions.length - 1)){
 				this.remainingCount = this.actions[this.currentAction].act();
 				if(remainingCount == -1){
 					this.finishAction();
 				}
 			}
 		} else {
 			this.finishAction();
 		}
 		return remainingCount;
 	}
 	
 	public void setTarget(InsBasic Target){
 		this.target = Target;
 		for(int i = 0; i < this.actions.length; i++){
 			this.actions[i].setTarget(Target);
 		}
 	}
 	
 	public void reset(){
 		super.reset();
 		for(int i = 0; i < this.actions.length; i++){
 			this.actions[i].reset();
 		}
 	}
 	
 	public void destroy(){
 		super.destroy();
 		for(int i = 0; i < this.actions.length; i++){
 			this.actions[i].destroy();
 		}
 		this.actions = null;
 	}
 
 }
