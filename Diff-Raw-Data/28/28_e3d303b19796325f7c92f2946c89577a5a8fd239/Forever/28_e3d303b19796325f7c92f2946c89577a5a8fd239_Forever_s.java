 package com.inspedio.entity.actions;
 
 import com.inspedio.entity.InsAction;
 import com.inspedio.entity.InsBasic;
 import com.inspedio.system.helper.InsUtil;
 
 public class Forever extends InsAction{
 	
 	protected InsAction action;
 
 	protected Forever(InsAction Action) {
 		super(0, null);
 		this.action = Action;
 	}
 	
 	public static Forever create(InsAction Action){
 		return new Forever(Action);
 	}
 
 	public int act() {
 		remainingCount = -1;
 		if(this.action.active){
 			remainingCount = InsUtil.Max(this.action.act(), remainingCount);
 		}
 		if(remainingCount == -1){
 			this.action.reset();
 		}
 		return 1;
 	}
 	
 	public void setTarget(InsBasic Target){
 		this.action.setTarget(Target);
 	}
 	
 	public void destroy(){
 		super.destroy();
 		this.action.destroy();
 		this.action = null;
 	}
 
 }
