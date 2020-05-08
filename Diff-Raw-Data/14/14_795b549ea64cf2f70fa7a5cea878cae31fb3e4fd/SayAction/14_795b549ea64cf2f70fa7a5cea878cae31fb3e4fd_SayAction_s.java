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
 
 import com.github.a2g.core.action.BaseAction;
 import com.github.a2g.core.interfaces.InternalAPI;
 import com.github.a2g.core.interfaces.PopupPanelAPI;
 import com.github.a2g.core.objectmodel.Animation;
 import com.github.a2g.core.objectmodel.SceneObject;
 import com.github.a2g.core.primitive.ColorEnum;
 import com.github.a2g.core.action.ChainedAction;
 
 
 
 public class SayAction extends ChainedAction {
 	private ArrayList<String> speech;
 	private ArrayList<Double> ceilings;
 	private PopupPanelAPI popup;
 	private short objId;
 	private double totalDurationInSeconds;
 	private Animation anim;
 	private SceneObject object;
 	private int numberOfFramesTotal;
 
 	public SayAction(BaseAction parent, short objId, String fullSpeech) {
 		super(parent, parent.getApi(), true);
 		this.objId = objId;
 		this.popup = null;
 		this.anim = null;
 		this.numberOfFramesTotal = 0;
 		speech = new ArrayList<String>();
 		ceilings = new ArrayList<Double>();
 		String[] lines = fullSpeech.split("\n");
 		this.totalDurationInSeconds = 0;
 		for (int i = 0; i < lines.length; i++) {
 			String line = lines[i];
 			speech.add(line);
 			int milliseconds = getMillisecondsForSpeech(line);
 			totalDurationInSeconds = totalDurationInSeconds + milliseconds;
 		}
 
 		// set ceilings (for easy calcluation)
 		double rollingCeiling = 0;
 		for (int i = 0; i < lines.length; i++) {
 			String line = lines[i];
 			rollingCeiling += getMillisecondsForSpeech(line);
 			ceilings.add(new Double(rollingCeiling / totalDurationInSeconds));
 		}
 
 		InternalAPI api = getApi();
 		this.object = api.getObject(this.objId);
 
 	}
 
 	int getAdjustedNumberOfFrames(String speech, double approxDuration, int animFramesCount, int talkingAnimationDelay)
 	{
 		// but if we need an animation, we find out how long it takes
 		// to play a single play of the animation to play whilst talking.
 		int durationOfSingleAnimation = animFramesCount
 				* (40+ 40*talkingAnimationDelay);
 
 		// ... then we find how many times the animation should repeat
 		// so that it fills up the totalDuration.
 		double numberOfTimesAnimRepeats = approxDuration
 				/ durationOfSingleAnimation;
 
 		// ... and the number of frames that occurs during that
 		// many plays of the animation.
 		int numberOfFramesTotal = (int) (numberOfTimesAnimRepeats * animFramesCount* 40);
 		// The effect of this is that there is a little bit of 'over-play'
 		// where the amount of time it takes to 'say' something
 		// when there is a talking animation, is usually a
 		// little bit more than the time calculated from the speech.
 		// This is to ensure that the animation always ends whilst
 		// the head is down, mouth is shut. The smaller the number of
 		// frames in a talking animation the less over-play.
 		return numberOfFramesTotal;
 	}
 
 	@Override
 	public void runGameAction() {
 
 		ColorEnum color = ColorEnum.Blue;
 
 		if (object != null && object.getTalkingColor() != null) {
 			color = object.getTalkingColor();
 		}
 		this.popup = getApi().getFactory().createPopupPanel(speech.get(0), color, this);
 
 		if (object != null) {
 			String talkingAnimTextualId = object.getTalkingAnimation();
 			anim = object.getAnimations().at(talkingAnimTextualId);
 			if (anim==null)
 			{
 				// if theres no animation then we just wait for the totalDuration;
 				double framesPerSecond = 40;
 				numberOfFramesTotal = (int) (totalDurationInSeconds * framesPerSecond);
 			}
 			else
 			{
 				numberOfFramesTotal = getAdjustedNumberOfFrames(
 						speech.get(0),
 						totalDurationInSeconds,
 						anim.getFrames().getCount(),
 						object.getTalkingAnimationDelay()
 						);
 			}
 		}
 		if(numberOfFramesTotal<2)
 		{
 			getApi().displayTitleCard("error!!");
 			assert(false);
 		}
 
 		if (anim != null && object != null) {
 			object.setCurrentAnimation(anim.getTextualId());
 			object.setVisible(true);
 		}
 
 		this.popup.setPopupPosition(20, 20);
 
 
 		this.run((int) totalDurationInSeconds);
 	}
 
 	@Override
 	protected void onUpdateGameAction(double progress) {
 		
 		if(object!=null)
 		{
			// The current animation is set every frame 
			// because of the quirk in Base Action where the next action is
			// executed before the last one finished.
 			if (anim != null && object != null) {
				object.setCurrentAnimation(anim.getTextualId());
 				object.setVisible(true);
 			}
 		}
 		
 		this.popup.show();
 
 		// update text bubble
 		for (int i = 0; i < ceilings.size(); i++) {
 			if (progress < ceilings.get(i)) {
 				popup.updateText(speech.get(i));
 				break;
 			}
 		}
 
 		// if theres an associated animation, then use it
 		if (this.anim != null) {
 			int numberOfFramesSoFar = (int) (progress * numberOfFramesTotal);
 			int frame = numberOfFramesSoFar % anim.getFrames().getCount();
 
 			// all frames of the animation should be shown
 			this.object.setCurrentFrame(frame);
 		}
 	}
 
 	@Override
 	protected void onCompleteGameAction() {
 		if (this.anim != null) {
 			this.anim.getObject().setCurrentAnimation(
 					this.anim.getObject().getInitialAnimation());
 		}
 
 		if (this.popup != null) {
 			this.popup.hide();
 			this.popup = null;
 		}
 	}
 
 	@Override
 	public boolean isParallel() {
 
 		return false;
 	}
 
 	int getMillisecondsForSpeech(String speech) {
 		int popupDelay = getApi().getPopupDelay();
 		// int delay = how;
 		// int duration = (speech.length() * (2 + delay)) * 40;
 		return popupDelay * 100;
 	}
 }
