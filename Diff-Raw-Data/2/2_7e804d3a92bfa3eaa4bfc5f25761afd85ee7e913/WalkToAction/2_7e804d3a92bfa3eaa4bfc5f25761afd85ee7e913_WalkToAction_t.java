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
 
 
 import com.github.a2g.core.action.BaseAction;
 public class WalkToAction
 extends MoveWhilstAnimatingAction
 {
 
 
 	public WalkToAction(BaseAction parent, short objId, double endX, double endY, int delay, boolean isLinear)
 	{
 		super(parent, objId, isLinear);
 		super.setEndX(endX);
		super.setEndY(endY);
 		this.setHoldLastFrame(false);
 	}
 
 	@Override
 	public void runGameAction()
 	{
 		double startX = super.getObject().getBaseMiddleX();
 		double startY = super.getObject().getBaseMiddleY();
 
 		double diffX = startX - getEndX();
 		System.out.println(" walkto " + startX + " " + getEndX());
 		double diffY = startY - getEndY();
 
 		// anim
 		String anim = "";
 		int width = getApi().getSceneGui().getWidth();
 		int height = getApi().getSceneGui().getHeight();
 
 		if ((diffX * width) * (diffX * width)
 				> (diffY * height)
 				* (diffY * height)) {
 			if (getEndX() < startX) {
 				anim = super.getObject().getSpecialAnimation(
 						com.github.a2g.core.interfaces.SceneAPI.Special.West);
 			} else {
 				anim = super.getObject().getSpecialAnimation(
 						com.github.a2g.core.interfaces.SceneAPI.Special.East);
 			}
 		} else {
 			if (getEndY() < startY) {
 				anim = super.getObject().getSpecialAnimation(
 						com.github.a2g.core.interfaces.SceneAPI.Special.North);
 			} else {
 				anim = super.getObject().getSpecialAnimation(
 						com.github.a2g.core.interfaces.SceneAPI.Special.South);
 			}
 		}
 
 		// we've set it up now, pass to MoveWhilstAnimatingAction to execute
 		super.getObject().setCurrentAnimation(anim);
 		super.runGameAction();
 	}
 
 	@Override // on complete walking
 	protected void onCompleteGameAction() {
 		super.onCompleteGameAction();
 
 		// best to set initial animation at the end, since:
 		// - if the walk animation is a cycle then no frame will be completely stationary
 		// - to make it consistent with everything else
 		//super.getObject().setCurrentAnimation(south);super.getObject().setCurrentFrame(0);
 		super.getObject().setToInitialAnimationWithoutChangingFrame();
 	}
 }
