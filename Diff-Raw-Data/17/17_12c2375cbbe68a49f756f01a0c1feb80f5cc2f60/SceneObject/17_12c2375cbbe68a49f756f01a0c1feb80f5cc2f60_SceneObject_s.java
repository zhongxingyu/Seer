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
 
 package com.github.a2g.core.objectmodel;
 
 import java.util.Map;
 import java.util.TreeMap;
 
 
 import com.github.a2g.core.interfaces.SceneAPI;
 import com.github.a2g.core.interfaces.ConstantsForAPI.Special;
 import com.github.a2g.core.primitive.ColorEnum;
 import com.github.a2g.core.primitive.Point;
 import com.github.a2g.core.primitive.PointF;
 import com.github.a2g.core.primitive.Rect;
 
 
 public class SceneObject {
 	private String initialAnimationId;
 	private Map<SceneAPI.Special, String> mapOfSpecialAnimations;
 	private final String textualId;
 	private String displayName;
 	private AnimationCollection animationCollection;
 	private FrameAndAnimation fak;
 	private com.github.a2g.core.objectmodel.Image currentImage;
 	private boolean visible;
 	private final double screenPixelWidth;
 	private final double screenPixelHeight;
 	private int rawY; // needed for moving image around.
 	private int rawX;
 	private int numberPrefix;
 	private short code;
 	private ColorEnum talkingColor;
 	private int talkingAnimationDelay;
 
 	public SceneObject(String textualId, int width, int height) {
 		this.currentImage = null;
 		this.textualId = textualId;
 		this.displayName = textualId;
 		this.animationCollection = new AnimationCollection();
 		this.fak = new FrameAndAnimation(
 				this.textualId);
 		this.visible = true;
 		this.screenPixelWidth = width;
 		this.screenPixelHeight = height;
 		this.mapOfSpecialAnimations = new TreeMap<Special, String>();
 		this.numberPrefix = 0;
 		this.initialAnimationId = textualId + "_INITIAL";
 		this.talkingAnimationDelay = 0;
 
 		// talkingColro deliberately null, so the
 		// default color can be in one spot: the say action
 		this.talkingColor = null;
 		this.setBaseMiddleX(0);
 		this.setBaseMiddleY(0);
 	}
 
 	public void setNumberPrefix(int number) {
 		this.numberPrefix = number;
 	}
 
 	public int getNumberPrefix() {
 		return this.numberPrefix;
 	}
 
 	public String getTextualId() {
 		return this.textualId;
 	}
 
 	public AnimationCollection getAnimations() {
 		return this.animationCollection;
 	}
 
 	public int getCurrentFrame() {
 		int i = this.fak.getCurrentFrame();
 
 		return i;
 	}
 
 	public void setCurrentFrame(int i) {
 		this.fak.setCurrentFrame(i);
 		updateToCorrectImage();
 	}
 
 	public void incrementFrameWithWraparound() {
 		if (getAnimations().getCount() == 0) {
 			// Log::NoSingleImage(QString("Here in IncrementFrame"));
 			return;
 		}
 
 		Animation anim = getAnimations().at(
 				this.fak.getCurrentAnimationTextualId());
 		// Log::Images(QString("Progress to next frame of [%1] which is %2 / %3 %4").arg(this.fak.AnimName()).arg(this.fak.Frame()).arg(anim->GetFrames().Count()-1).arg( this.anims->At(this.fak.AnimName())->GetFrames().At(this.fak.Frame())));
 
 		int i = this.fak.getCurrentFrame() + 1;
 
 		if (i >= anim.getFrames().getCount()) {
 			this.fak.setCurrentFrame(0);
 		} else {
 			this.fak.setCurrentFrame(i);
 		}
 		// do no update image here - since it is a heavy operation, we do it once per tick.
 	}
 
 	public void decrementFrameWithWraparound() {
 		Animation anim = getAnimations().at(
 				this.fak.getCurrentAnimationTextualId());
 		// Log::Images(QString("Progress to next frame of [%1] which is %2 / %3 %4").arg(this.fak.AnimName()).arg(this.fak.Frame()).arg(anim->GetFrames().Count()-1).arg( this.anims->At(this.fak.AnimName())->GetFrames().At(this.fak.Frame())));
 
 		int i = this.fak.getCurrentFrame() - 1;
 
 		if (i < 0) {
 			this.fak.setCurrentFrame(
 					anim.getFrames().getCount()
 					- 1);
 		} else {
 			this.fak.setCurrentFrame(i);
 		}
 		// do no update image here - since it is a heavy operation, we do it once per tick.
 	}
 
 	public void updateCurrentImage()
 	{
 		currentImage.setLeftTop(getRawLeftTop());
 	}
 
 
 	public void updateToCorrectImage() {
 		// 1. do this only when the this.currentImage != img
 		String currentAnim = fak.getCurrentAnimationTextualId();
 		Animation anim = this.animationCollection.at(currentAnim);
 
 		// if animation is set to something bad, then set it to back to initial
 		if(anim==null)
 		{
 			fak.setCurrentAnimationTextualId(this.initialAnimationId);
 			anim = this.animationCollection.at(
 					fak.getCurrentAnimationTextualId());
 		}
 
 		if(anim==null)
 		{
 			anim = this.animationCollection.at(0);
 			fak.setCurrentAnimationTextualId(anim.getTextualId());
 		}
 
 		if (anim != null) {
 			if (fak.getCurrentFrame()
 					>= anim.getLength()) {
 				fak.setCurrentFrame(
 						anim.getLength() - 1);
 			}
 
 			int curFrame = fak.getCurrentFrame();
 			com.github.a2g.core.objectmodel.Image current = anim.getImageAndPosCollection().at(curFrame);
 
 
 			// yes current can equal null in some weird cases where I place breakpoints...
 			if (current != null
 					&& !current.equals(this)) {
 				if (this.currentImage != null) {
 					this.currentImage.setVisible(false, getRawLeftTop());
 				}
 				this.currentImage = current;
 			}
 		}
 		// 2, but do this always
 		if (this.currentImage != null) {
 			this.currentImage.setVisible(
 					this.visible, getRawLeftTop());
 		}
 
 	}
 
 	public void setTalkingAnimationDelay(int delay)
 	{
 		this.talkingAnimationDelay = delay;
 	}
 
 	public int getTalkingAnimationDelay()
 	{
 		return this.talkingAnimationDelay;
 	}
 
 	public void walkTo(Point point) {
 		walkTo(point.getX(), point.getY());
 	}
 
 	public void walkTo(double x, double y) {
 		// KillCurrentAnimationAndClearInstructions();
 		PointF currentPoint = new PointF(
 				getBaseMiddleX(),
 				getBaseMiddleY());
 
 		currentPoint.setX(
 				currentPoint.getX()
 				* this.screenPixelWidth);
 		currentPoint.setY(
 				currentPoint.getY()
 				* this.screenPixelHeight);
 	}
 
 	public void setVisible(boolean visible) {
 		//if (this.visible != visible) {
 			this.visible = visible;
 			updateToCorrectImage();
 		//}
 	}
 
 	public boolean isVisible() {
 		return this.visible;
 	}
 
 	public String getDisplayName() {
 		return this.displayName;
 	}
 
 	public void setDisplayName(String displayName) {
 		this.displayName = displayName;
 	}
 
 	Point getRawLeftTop()
 	{
 		return new Point(this.rawX,this.rawY);
 	}
 
 	static double worldToScreenX(int intX, double screenSpan, int lowerBound, int upperBound )
 	{
 		int rectSpan = upperBound-lowerBound;
 		double doubleX = (intX + .5*rectSpan  + lowerBound)/screenSpan ;
 		return doubleX;
 	}
 
 	static int screenToWorldX(double doubleX, double screenSpan, int lowerBound, int upperBound )
 	{
 		int rectSpan = upperBound-lowerBound;
 		double intX = doubleX * screenSpan - .5*rectSpan  - lowerBound;
 		return (int)intX;
 	}
 
 	static int screenToWorldY(double doubleY, double screenSpan, int lowerBound, int upperBound )
 	{
 		int rectSpan = upperBound-lowerBound;
 		double intY = doubleY * screenSpan - rectSpan  - lowerBound;
 		return (int)intY;
 	}
 
 	static double worldToScreenY(int intY, double screenSpan, int lowerBound, int upperBound )
 	{
 		int rectSpan = upperBound-lowerBound;
 		double doubleY = (intY + rectSpan  + lowerBound)/screenSpan ;
 		return doubleY;
 	}
 
 	public void setX(int rawX)
 	{
 		this.rawX = rawX;
 		if(currentImage!=null)
 		{
 			this.currentImage.setLeftTop(getRawLeftTop());
 		}
 	}
 
 	public void setY(int rawY)
 	{
 		this.rawY = rawY;
 		if(currentImage!=null)
 		{
 			this.currentImage.setLeftTop(getRawLeftTop());
 		}
 	}
 
 	public int getX()
 	{
 		return this.rawX;
 	}
 
 	public int getY()
 	{
 		return this.rawY;
 	}
 
 	public void setBaseMiddleX(double baseMiddleX)
 	{
 		int rawX = screenToWorldX(baseMiddleX, screenPixelWidth, getCurrentBounds().getLeft(), getCurrentBounds().getRight());
 		setX(rawX);
 	}
 
 	public void setBaseMiddleY(double baseMiddleY)
 	{
 		int rawY = screenToWorldY(baseMiddleY, screenPixelHeight, getCurrentBounds().getTop(), getCurrentBounds().getBottom());
 		setY(rawY);
 	}
 
 
 	public double getBaseMiddleX()
 	{
 		double bmx = worldToScreenX(rawX, screenPixelWidth, getCurrentBounds().getLeft(), getCurrentBounds().getRight());
 
 		return bmx;
 	}
 
 	public double getBaseMiddleY()
 	{
 		double bmy = worldToScreenY(rawY, screenPixelHeight, getCurrentBounds().getTop(), getCurrentBounds().getBottom());
 
 		return bmy;
 	}
 
 	Rect getCurrentBounds()
 	{
 		if(currentImage!=null)
 		{
 			return currentImage.getBoundingRect();
 		}
 		return new Rect(0,0,0,0);
 	}
 
 	Point getMiddleOfBaseAbsolute(String animTextualId) {
 		int minLeft = 1000;
 		int maxRight = 0;
 		int maxBottom = 0;
 		Animation xanim = this.animationCollection.at(
 				animTextualId);
 
 		if (xanim != null) {
 			for (int i = 0; i
 					< xanim.getLength(); i++) {
 				com.github.a2g.core.objectmodel.Image img = xanim.getFrames().at(
 						i);
 				Rect rect = img.getBoundingRect();
 
 				if (rect.getLeft() < minLeft) {
 					minLeft = rect.getLeft();
 				}
 				if (rect.getRight() > maxRight) {
 					maxRight = rect.getRight();
 				}
 				if (rect.getBottom()
 						> maxBottom) {
 					maxBottom = rect.getBottom();
 				}
 			}
 		}
 
 		Point p = new Point(
 				(minLeft + maxRight) / 2,
 				maxBottom - 4);
 
 		return p;
 	}
 
 	void setSpecialAnimation(Special type, String textualId) {
 		this.mapOfSpecialAnimations.put(type,
 				textualId);
 	}
 
 	public String getSpecialAnimation(Special type) {
 		return this.mapOfSpecialAnimations.get(
 				type);
 	}
 	public void setTalkingAnimation(String talkingAnimation)
 	{
 		this.mapOfSpecialAnimations.put(Special.Talking, talkingAnimation);
 	}
 
 	public String getTalkingAnimation() {
 		if (this.mapOfSpecialAnimations.containsKey(
 				Special.Talking)) {
 			String animId = this.mapOfSpecialAnimations.get(
 					Special.Talking);
 
 			return animId;
 		}
 		return "";
 	}
 
 	public String getCurrentAnimation() {
 		String textualId = this.fak.getCurrentAnimationTextualId();
 
 		return textualId;
 	}
 
 	public void setCurrentAnimation(String textualId) {
 		this.fak.setCurrentAnimationTextualId(
 				textualId);
 		updateToCorrectImage();
 	}
 
 
 	public String getInitialAnimation() {
 		return initialAnimationId;
 	}
 
 	public void setInitialAnimation(String InitialAnimation) {
 		this.initialAnimationId = InitialAnimation;
 	}
 
 	public void setCode(short objectCode) {
 		this.code = objectCode;
 	}
 
 	public short getCode() {
 		return code;
 
 	}
 
 	public void setTalkingColor(ColorEnum color) {
 		this.talkingColor = color;
 	}
 
 	public ColorEnum getTalkingColor() {
 		return this.talkingColor;
 	}

 }
 
 
 ;
