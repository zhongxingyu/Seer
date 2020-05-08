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
 
 import com.github.a2g.core.action.PlayAnimationAction;
 import com.github.a2g.core.action.PlayAnimationRepeatWhilstVisibleAction;
 import com.github.a2g.core.action.SayAction;
 import com.github.a2g.core.action.SayWithoutAnimationAction;
 import com.github.a2g.core.action.SetActiveAnimationAction;
 import com.github.a2g.core.action.SetActiveFrameAction;
 import com.github.a2g.core.action.SetBaseMiddleXAction;
 import com.github.a2g.core.action.SetBaseMiddleYAction;
 import com.github.a2g.core.action.SetDisplayNameAction;
 import com.github.a2g.core.action.SetInitialAnimationAction;
 import com.github.a2g.core.action.SetInventoryVisibleAction;
 import com.github.a2g.core.action.SetTalkingAnimationAction;
 import com.github.a2g.core.action.SetTalkingAnimationDelayAction;
 import com.github.a2g.core.action.SetVisibleAction;
 import com.github.a2g.core.action.SleepAction;
 import com.github.a2g.core.action.SwapPropertyAction;
 import com.github.a2g.core.action.SwitchToAction;
 import com.github.a2g.core.action.DialogTreeBranchAction;
 import com.github.a2g.core.action.DialogTreeEndAction;
 import com.github.a2g.core.action.DialogTreeDoDialogBranchAction;
 import com.github.a2g.core.action.WaitForFrameAction;
 import com.github.a2g.core.action.WalkToAction;
 import com.github.a2g.core.action.BaseDialogTreeAction;
 import com.github.a2g.core.interfaces.ActionCallbackAPI;
 import com.github.a2g.core.interfaces.InternalAPI;
 import com.github.a2g.core.interfaces.SystemAnimationAPI;
 import com.github.a2g.core.interfaces.SystemAnimationCallbackAPI;
 import com.github.a2g.core.primitive.ColorEnum;
 import com.github.a2g.core.primitive.PointF;
 
 
 
 public abstract class BaseAction implements SystemAnimationCallbackAPI
 {
 	private SystemAnimationAPI systemAnimation;
 	private ActionCallbackAPI callbacks;
 	protected BaseAction parent;
 	private InternalAPI api;
 
 	public enum SwapType {
 		Visibility
 	}
 
 	// all the abstract have GameAction in them to let
 	// the client know they are dealing with GameAction and not Animation
 	abstract public boolean isParallel();
 
 	abstract protected void onUpdateGameAction(double progress);
 
 	abstract protected void onCompleteGameAction();
 
 	abstract public void runGameAction();
 
 	protected BaseAction(BaseAction parent, InternalAPI api, boolean isLinear) {
 		this.parent = parent;
 		this.callbacks = null;
 		this.api = api;
 		if(api!=null)
 		{
 			this.systemAnimation = api.getFactory().createSystemAnimation(this, isLinear);
 		}
 		else
 		{
 			throw new NullPointerException();
 		}
 	}
 
 
 	public BaseDialogTreeAction branch(int branchId, String text) {
 		return new DialogTreeBranchAction(this,
 				text, branchId);
 	}
 
 	public ChainedAction branch(int branchId, final boolean isKey, String text) {
 		if(isKey)
 			return new DialogTreeBranchAction(this, text, branchId);
 		return doNothing();
 	}
 
 	public ChainedAction doBoth(ChainedAction a, ChainedAction b) {
 		return a.subroutine(b);
 	}
 
 	public BaseDialogTreeAction doNothing() {
 		return new DoNothingAction(this);
 	}
 
 	public BaseDialogTreeAction endDialogTree() {
 		return new DialogTreeEndAction(this);
 	}
 
 	public InternalAPI getApi() {
 		return this.api;
 	}
 
 	public BaseAction getParent() {
 		return this.parent;
 	}
 
 	public BaseDialogTreeAction doDialogBranch(int branchId) {
 		return new DialogTreeDoDialogBranchAction(this,
 				branchId);
 	}
 
 	@Override // method in animation
 	public void onComplete() {
 
 		// It might seem like it's a better idea to
 		// run this actions action completion handler
 		// before calling startTheNextAction.
 		// However doDialogBranchAction starts execution of another animation tree
 		// and if order is 1) onCompleteGameAction 2) startTheNextAction,
 		// then on startTheNextAction starts triggering animations in the
 		// newly replenished series of animations that onCompleteGameAction created,
		// giving effectively two simultaneous thrads of
 		// executions of the Action chain. This results in an observed bug
 		// where only every second Action runs.
		// So either a second action runner instance is needed for dialogs. Or, simpler
		// , the call order is kept like this.
		this.callbacks.startTheNextAction(
				this);
 
 		onCompleteGameAction();
 	}
 
 	@Override
 	public void onUpdate(double progress)
 	{
 		onUpdateGameAction(progress);
 	}
 
 	// plain..
 	public ChainedAction playAnimation(String  animationCode) {
 		boolean isLinear = true;
 		PlayAnimationAction a = new PlayAnimationAction(
 				this, animationCode, isLinear);
 
 		return a;
 	}
 
 	public ChainedAction playAnimation(String  animationCode, int delay) {
 		boolean isLinear = true;
 		PlayAnimationAction a = new PlayAnimationAction(
 				this, animationCode, isLinear);
 
 		a.setDelay(delay);
 		return a;
 
 	}
 
 	// simple backwards
 	public ChainedAction playAnimationBackwards(String  animationCode) {
 		boolean isLinear = true;
 		PlayAnimationAction a = new PlayAnimationAction(
 				this, animationCode, isLinear);
 
 		a.setBackwards(true);
 		return a;
 	}
 
 	public ChainedAction playAnimationBackwards(String  animationCode, int delay) {
 		boolean isLinear = true;
 		PlayAnimationAction a = new PlayAnimationAction(
 				this, animationCode, isLinear);
 
 		a.setBackwards(true);
 		a.setDelay(delay);
 		return a;
 	}
 
 	// simple hold last frame
 	public ChainedAction playAnimationHoldLastFrame(String  animationCode) {
 		boolean isLinear = true;
 		PlayAnimationAction a = new PlayAnimationAction(
 				this, animationCode, isLinear);
 
 		a.setHoldLastFrame(true);
 		return a;
 	}
 
 	public ChainedAction playAnimationHoldLastFrame(String  animationCode, int delay) {
 		boolean isLinear = true;
 		PlayAnimationAction a = new PlayAnimationAction(
 				this, animationCode, isLinear);
 
 		a.setHoldLastFrame(true);
 		a.setDelay(delay);
 		return a;
 	}
 
 	// simple non blocking
 	public ChainedAction playAnimationNonBlocking(String  animationCode) {
 		boolean isLinear = true;
 		PlayAnimationAction a = new PlayAnimationAction(
 				this, animationCode, isLinear);
 
 		a.setNonBlocking(true);
 		return a;
 	}
 
 	public ChainedAction playAnimationNonBlocking(String  animationCode, int delay) {
 		boolean isLinear = true;
 		PlayAnimationAction a = new PlayAnimationAction(
 				this, animationCode, isLinear);
 
 		a.setNonBlocking(true);
 		a.setDelay(delay);
 		return a;
 	}
 
 	// double combo1of3: backwards + hold last frame
 	public ChainedAction playAnimationBackwardsHoldLastFrame(String  animationCode) {
 		boolean isLinear = true;
 		PlayAnimationAction a = new PlayAnimationAction(
 				this, animationCode,isLinear);
 
 		a.setBackwards(true);
 		a.setHoldLastFrame(true);
 		return a;
 	}
 
 	public ChainedAction playAnimationBackwardsHoldLastFrame(String  animationCode, int delay) {
 		boolean isLinear = true;
 		PlayAnimationAction a = new PlayAnimationAction(
 				this, animationCode, isLinear);
 
 		a.setBackwards(true);
 		a.setHoldLastFrame(true);
 		a.setDelay(delay);
 		return a;
 	}
 
 	// double combo2of3: backwards + nonblocking
 	public ChainedAction playAnimationBackwardsNonBlocking(String  animationCode) {
 		boolean isLinear = true;
 		PlayAnimationAction a = new PlayAnimationAction(
 				this, animationCode, isLinear);
 
 		a.setBackwards(true);
 		a.setNonBlocking(true);
 		return a;
 	}
 
 	public ChainedAction playAnimationBackwardsNonBlocking(String  animationCode, int delay) {
 		boolean isLinear = true;
 		PlayAnimationAction a = new PlayAnimationAction(
 				this, animationCode, isLinear);
 
 		a.setBackwards(true);
 		a.setNonBlocking(true);
 		a.setDelay(delay);
 		return a;
 	}
 
 	// double combo2of3: holdLastFrame + nonblocking
 	public ChainedAction playAnimationHoldLastFrameNonBlocking(String  animationCode) {
 		boolean isLinear = true;
 		PlayAnimationAction a = new PlayAnimationAction(
 				this, animationCode,isLinear);
 
 		a.setHoldLastFrame(true);
 		a.setNonBlocking(true);
 		return a;
 	}
 
 	public ChainedAction playAnimationHoldLastFrameNonBlocking(String  animationCode, int delay) {
 		boolean isLinear = true;
 		PlayAnimationAction a = new PlayAnimationAction(
 				this, animationCode, isLinear);
 
 		a.setHoldLastFrame(true);
 		a.setNonBlocking(true);
 		a.setDelay(delay);
 		return a;
 	}
 
 	// ..and one method with the whole lot!
 	public ChainedAction playAnimationBackwardsHoldLastFrameNonBlocking(String  animationCode) {
 		boolean isLinear = true;
 		PlayAnimationAction a = new PlayAnimationAction(
 				this, animationCode, isLinear);
 
 		a.setBackwards(true);
 		a.setHoldLastFrame(true);
 		a.setNonBlocking(true);
 		return a;
 	}
 
 	public ChainedAction playAnimationBackwardsHoldLastFrameNonBlocking(String  animationCode, int delay) {
 		boolean isLinear = true;
 		PlayAnimationAction a = new PlayAnimationAction(
 				this, animationCode, isLinear);
 
 		a.setBackwards(true);
 		a.setHoldLastFrame(true);
 		a.setNonBlocking(true);
 		a.setDelay(delay);
 		return a;
 	}
 
 	public ChainedAction playAnimationRepeatWhilstVisible(String  animationCode) {
 		boolean isLinear = true;
 		return new PlayAnimationRepeatWhilstVisibleAction(
 				this, animationCode, isLinear);
 	}
 
 	public ChainedAction say(short objectCode, String speech) {
 		return new SayAction(this, objectCode,
 				speech);
 		// return toReturn;
 	}
 
 	public ChainedAction say(String speech) {
 		return new SayAction(this, api.getDefaultSayer(),
 				speech);
 		// return toReturn;
 	}
 	
 	public ChainedAction sayWithoutAnimation(short objectCode, String speed) {
 		boolean isLinear = true;
 		return new SayWithoutAnimationAction(
 				this, objectCode, speed, isLinear);
 	}
 
 	public ChainedAction setActiveAnimation(String  animationCode) {
 		return new SetActiveAnimationAction(
 				this, animationCode);
 	}
 
 	public ChainedAction setActiveFrame(short objectCode, int frame) {
 		return new SetActiveFrameAction(this,
 				objectCode, frame);
 	}
 
 	protected void setApi(InternalAPI api) {
 		if(api==null)
 		{
 			throw new NullPointerException();
 		}
 		this.api = api;
 	}
 
 	public ChainedAction setBaseMiddleX(short objectCode, double x) {
 		return new SetBaseMiddleXAction(this,
 				objectCode, x);
 	}
 
 	public ChainedAction setBaseMiddleY(short objectCode, double y) {
 		return new SetBaseMiddleYAction(this,
 				objectCode, y);
 		// return toReturn;
 	}
 
 	public void setCallbacks(ActionCallbackAPI callbacks) {
 		this.callbacks = callbacks;
 	}
 
 	public ChainedAction setDisplayName(short objectCode, String displayName) {
 		return new SetDisplayNameAction(this,
 				objectCode, displayName, true);
 	}
 
 	public ChainedAction setInventoryVisible(int inventoryId, boolean isVisible) {
 		return new SetInventoryVisibleAction(
 				this, inventoryId, isVisible, true);
 	}
 
 	public void setParent(BaseAction parent) {
 		this.parent = parent;
 	}
 
 	public ChainedAction setTalkingAnimation(String  animationCode) {
 		return new SetTalkingAnimationAction(
 				this, animationCode, true);
 		// return toReturn;
 	}
 
 	public ChainedAction setTalkingAnimationDelay(short objectCode, int delay) {
 		return new SetTalkingAnimationDelayAction(
 				this, objectCode, delay, true);
 		// return toReturn;
 	}
 	;
 
 	public ChainedAction setVisible(short objectCode, boolean isVisible) {
 		return new SetVisibleAction(this,
 				objectCode, isVisible, true);
 	}
 
 
 	public ChainedAction sleep(int milliseconds) {
 		return new SleepAction(this,
 				milliseconds);
 	}
 
 	public ChainedAction subroutine(ChainedAction orig) {
 		// find root of orig
 		BaseAction somewhereInOrig = orig;
 
 		while (somewhereInOrig.getParent().getParent()
 				!= null) {
 			somewhereInOrig = somewhereInOrig.getParent();
 		}
 		somewhereInOrig.setParent(this);
 		return orig;
 	}
 
 	public ChainedAction swapProperty(short objectCode1, short objectCode2,
 			SwapType type) {
 		return new SwapPropertyAction(this,
 				objectCode1, objectCode2, type);
 	}
 
 	public BaseDialogTreeAction  switchTo(String sceneName) {
 		return new SwitchToAction(this,
 				sceneName);
 		// return toReturn;
 	}
 
 	public ChainedAction waitForFrame(short objectCode, int frame) {
 		return new WaitForFrameAction(this,
 				objectCode, frame);
 	}
 
 
 	public ChainedAction setInitialAnimation(String  animationCode) {
 		return new SetInitialAnimationAction(this,
 				animationCode, true);
 	}
 
 	public ChainedAction walkTo( double x, double y) {
 		boolean isLinear = true;
 		WalkToAction a = new WalkToAction(this, api.getDefaultWalker(), x, y,0, isLinear);
 		return a;
 	}
 
 	public ChainedAction walkTo(PointF point) {
 		boolean isLinear = true;
 		WalkToAction a =  new WalkToAction(this, api.getDefaultWalker(),
 				point.getX(), point.getY(),0, isLinear);
 		return a;
 	}
 
 	public ChainedAction walkTo(double x, double y, int delay) {
 		boolean isLinear = true;
 		WalkToAction a = new WalkToAction(this, api.getDefaultWalker(), x, y, delay, isLinear);
 		return a;
 	}
 
 	public ChainedAction walkTo( PointF point, int delay) {
 		boolean isLinear = true;
 		return new WalkToAction(this, api.getDefaultWalker(), point.getX(), point.getY(), delay, isLinear);
 	}
 
 
 	public ChainedAction walkToNonBlocking(double x, double y) {
 		boolean isLinear = true;
 		WalkToAction a = new WalkToAction(this, api.getDefaultWalker(), x, y,0, isLinear);
 		a.setNonBlocking(true);
 		return a;
 	}
 
 	public ChainedAction walkToNonBlocking( PointF point) {
 		boolean isLinear = true;
 		WalkToAction a = new WalkToAction(this, api.getDefaultWalker(),
 				point.getX(), point.getY(), 0, isLinear);
 		a.setNonBlocking(true);
 		return a;
 	}
 
 	public ChainedAction walkToNonBlocking( double x, double y, int delay) {
 		boolean isLinear = true;
 		WalkToAction a = new WalkToAction(this, api.getDefaultWalker(), x,
 				y, delay, isLinear);
 		a.setNonBlocking(true);
 		return a;
 	}
 
 	public ChainedAction walkToNonBlocking(PointF point, int delay) {
 		boolean isLinear = true;
 		WalkToAction a = new WalkToAction(this, api.getDefaultWalker(),
 				point.getX(), point.getY(), delay, isLinear);
 		a.setNonBlocking(true);
 		return a;
 	}
 	
 	
 
 	public ChainedAction walkTo(short objectCode, double x, double y) {
 		boolean isLinear = true;
 		return new WalkToAction(this, objectCode, x, y, 0, isLinear);
 	}
 
 	public ChainedAction walkTo(short objectCode, PointF point) {
 		boolean isLinear = true;
 		return new WalkToAction(this, objectCode,
 				point.getX(), point.getY(),0, isLinear);
 	}
 
 	public ChainedAction walkTo(short objectCode, double x, double y, int delay) {
 		boolean isLinear = true;
 		return new WalkToAction(this, objectCode, x, y, delay, isLinear);
 	}
 
 	public ChainedAction walkTo(short objectCode, PointF point, int delay) {
 		boolean isLinear = true;
 		return new WalkToAction(this, objectCode, point.getX(), point.getY(), delay, isLinear);
 	}
 
 
 	public ChainedAction walkToNonBlocking(short objectCode, double x, double y) {
 		boolean isLinear = true;
 		WalkToAction a = new WalkToAction(this, objectCode, x, y,0, isLinear);
 		a.setNonBlocking(true);
 		return a;
 	}
 
 	public ChainedAction walkToNonBlocking(short objectCode, PointF point) {
 		boolean isLinear = true;
 		WalkToAction a = new WalkToAction(this, objectCode,
 				point.getX(), point.getY(),0, isLinear);
 		a.setNonBlocking(true);
 		return a;
 	}
 
 	public ChainedAction walkToNonBlocking(short objectCode, double x, double y, int delay) {
 		boolean isLinear = true;
 		WalkToAction a = new WalkToAction(this, objectCode, x,
 				y, delay, isLinear);
 		a.setNonBlocking(true);
 		return a;
 	}
 
 	public ChainedAction walkToNonBlocking(short objectCode, PointF point, int delay) {
 		boolean isLinear = true;
 		WalkToAction a = new WalkToAction(this, objectCode,
 				point.getX(), point.getY(), delay, isLinear);
 		a.setNonBlocking(true);
 		return a;
 	}
 
 	public ChainedAction showTitleCard(String text, ColorEnum color)
 	{
 		return new TitleCardAction(this, text, color);
 	}
 
 	public void cancel() {
 		systemAnimation.cancel();
 
 	}
 
 	public void run(int i) {
 		systemAnimation.run(i);
 
 	}
 
 	public ChainedAction playAnimationNonBlockingHoldLastFrame(String  animationCode) {
 		boolean isLinear = true;
 		PlayAnimationAction a = new PlayAnimationAction(
 				this, animationCode, isLinear);
 
 		a.setHoldLastFrame(true);
 		a.setNonBlocking(true);
 		return a;
 	}
 
 	public ChainedAction setValue(String string, int i)
 	{
 		SetValueAction a = new SetValueAction(this, string, i);
 		return a;
 	}
 	
 	public ChainedAction moveWhilstAnimating(short objId, double x, double y)
 	{
 		boolean isLinear = true;
 		MoveWhilstAnimatingAction a = new MoveWhilstAnimatingAction(this, objId, x, y, isLinear); 
 		return a;
 	}
 	
 	public ChainedAction moveWhilstAnimatingNonBlocking(short objId, double x, double y)
 	{
 		boolean isLinear = true;
 		MoveWhilstAnimatingAction a = new MoveWhilstAnimatingAction(this, objId, x, y, isLinear); 
 		a.setNonBlocking(true);
 		return a;
 	}
 
 	
 }
 
 
 ;
