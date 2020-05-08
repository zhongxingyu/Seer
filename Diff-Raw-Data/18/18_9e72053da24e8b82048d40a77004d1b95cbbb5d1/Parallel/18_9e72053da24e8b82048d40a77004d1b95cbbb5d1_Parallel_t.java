 package com.inspedio.entity.actions;
 
 import com.inspedio.entity.InsAction;
 import com.inspedio.entity.InsBasic;
 import com.inspedio.entity.primitive.InsCallback;
 import com.inspedio.system.helper.InsUtil;
 
 /**
  * Execute Several Action in the same Time.<br>
  * For example if you want to scale and move an object at the same time.<br>
  * 
  * @author Hyude
  * @version 1.0
  */
 public class Parallel extends InsAction{
 
 	protected InsAction[] actions;
 	
 	protected Parallel(InsAction[] ActionList, InsCallback Callback){
 		super(0, Callback);
 		this.actions = ActionList;
		for(int i = 0; i < this.actions.length; i++){
			this.actions[i].autoDestroy = false;
		}
 	}
 	
 	public static Parallel create(InsAction[] ActionList, InsCallback Callback){
 		return new Parallel(ActionList, Callback);
 	}
 	
 	public static Parallel create(InsAction[] ActionList){
 		return create(ActionList, null);
 	}
 	
 	public int act() {
 		remainingCount = -1;
 		for(int i = 0; i < this.actions.length; i++){
 			if(this.actions[i].active){
 				remainingCount = InsUtil.Max(this.actions[i].act(), remainingCount);
 			}
 		}
 		if(remainingCount == -1){
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
