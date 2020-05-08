 /*
  * Copyright 2012 Anthony Cassidy
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 
 package com.github.a2g.core.action;
 
 import java.util.ArrayList;
 
 import com.github.a2g.core.interfaces.ActionCallbackAPI;
 import com.github.a2g.core.interfaces.ActionRunnerCallbackAPI;
 
 public class ActionRunner implements ActionCallbackAPI
 {
 	protected ArrayList<BaseAction> list;
 	private ArrayList<BaseAction> parallelActionsToWaitFor;
 	private int numberOfParallelActionsToWaitFor;
 	private ActionRunnerCallbackAPI api;
 	final private int id;
 	public ActionRunner(ActionRunnerCallbackAPI api, int id)
 	{
 		this.id = id;
 		this.api=api;
 		list = new ArrayList<BaseAction>();
 		parallelActionsToWaitFor = new ArrayList<BaseAction>();
 		numberOfParallelActionsToWaitFor = 0;
 	}
 
 	public int runAction(BaseAction grandChildOfActionChain)
 	{
 		this.list = new ArrayList<BaseAction>();
 		BaseAction a = grandChildOfActionChain;
 
 		while (a.getParent() != null) {
 			this.list.add(0, a);
 			a = a.getParent();
 		}
 
 		if(list.size()>0 && this.list.get(0).getClass()!=TitleCardAction.class)
 		{
 			list.add(0,new TitleCardAction(a,""));
 		}
 		processNextListOfParallelActions();
 
 		return 0;
 	}
 
 
	void executeParallelActions() {
 		this.numberOfParallelActionsToWaitFor = this.parallelActionsToWaitFor.size();
		for (int i = 0; i
				< this.parallelActionsToWaitFor.size(); i++)
 		{
 			BaseAction a = this.parallelActionsToWaitFor.get(i);
 
			System.out.println("ActionRunner::executeParallelActions " + this.id+ " " + a.toString() );
 			
 			a.setCallbacks(this);
 			a.runGameAction();
 		}
 	}
 
 	protected void processNextListOfParallelActions() {
 		this.parallelActionsToWaitFor.clear();
 		if (!this.list.isEmpty()) {
 			// add next batch of contiguous parallel actions
 			while (!this.list.isEmpty()
 					&& this.list.get(0).isParallel()) {
 				BaseAction theAction = this.list.get(
 						0);
 
 				this.list.remove(0);
 				this.parallelActionsToWaitFor.add(
 						theAction);
 			}
 
 			// if there was no parallel actions then add the non parallel one
 			if (this.parallelActionsToWaitFor.isEmpty()) {
 				BaseAction theAction = this.list.get(
 						0);
 
 				this.list.remove(0);
 				this.parallelActionsToWaitFor.add(
 						theAction);
 			}
 
 			// execute them
 			executeParallelActions();
 		}
 		else
 		{
 			api.actionFinished(this.id);
 		}
 	}
 
 	public void skip()
 	{
 		numberOfParallelActionsToWaitFor = 100;
 		for(int i=0;i < parallelActionsToWaitFor.size();i++)
 		{
 			parallelActionsToWaitFor.get(i).cancel();
 		}
 
 		numberOfParallelActionsToWaitFor = parallelActionsToWaitFor.size();
 		for(int i=0;i < parallelActionsToWaitFor.size();i++)
 		{
 			parallelActionsToWaitFor.get(i).run(0);
 		}
 	}
 
 	@Override
 	public void startTheNextAction(BaseAction a) {
 		this.numberOfParallelActionsToWaitFor--;
 		if (this.numberOfParallelActionsToWaitFor
				== 0) {
 			processNextListOfParallelActions();
 		}
 	}
 
 	public void cancel() {
 		// first clear the list, just incase cancelling the animation
 		// triggers ongameactioncomplete (above)
 		list.clear();
 		numberOfParallelActionsToWaitFor=0;
 
 		// now cancel the action(s) that are running
 		for(int i=0;i<parallelActionsToWaitFor.size();i++)
 		{
 			parallelActionsToWaitFor.get(i).cancel();
 		}
 	}
 }
