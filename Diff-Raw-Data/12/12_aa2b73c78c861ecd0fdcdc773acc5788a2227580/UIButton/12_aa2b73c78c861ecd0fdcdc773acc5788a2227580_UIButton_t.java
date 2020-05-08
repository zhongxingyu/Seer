 /*
  * Copyright (C) 2012 Wu Tong
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.cocoa4android.ui;
 
 import org.cocoa4android.cg.CGRect;
 import org.cocoa4android.ns.NSTextAlignment;
 
 import android.util.SparseArray;
 import android.view.MotionEvent;
 
 public class UIButton extends UIControl {
 	private SparseArray<String> titles = new SparseArray<String>();
 	private SparseArray<UIImage> backgroundImages = new SparseArray<UIImage>();
 	private SparseArray<UIImage> images = new SparseArray<UIImage>();
 	private SparseArray<UIColor> titleColors = new SparseArray<UIColor>();
 	
 	private UILabel titleLabel = null;
 	private UIImageView imageView = null;
 	
 	private int effectState = UIControlState.UIControlStateNormal;
 	
 	private boolean autoHighlight = NO;
 	
 	public boolean isAutoHighlight() {
 		return autoHighlight;
 	}
 	public void setAutoHighlight(boolean autoHighlight) {
 		this.autoHighlight = autoHighlight;
 		this.autoHighlight();
 	}
 	public UILabel titleLabel() {
 		return titleLabel;
 	}
 	public UIButton(){
 		super();
 		imageView = new UIImageView();
 		this.addSubview(imageView);
 	}
 	@Override
 	protected void handleTouch(MotionEvent event){
 		super.handleTouch(event);
 		if (event.getAction()==MotionEvent.ACTION_DOWN) {
 			this.setHighlighted(YES);
 		}else if(event.getAction()==MotionEvent.ACTION_UP){
 			this.setHighlighted(NO);
 		}
 	}
 	public UIButton(CGRect frame){
 		this();
 		this.setFrame(frame);
 	}
 	public void setFrame(CGRect frame){
 		super.setFrame(frame);
 		if (titleLabel!=null) {
 			titleLabel.setFrame(CGRectMake(0, 0, frame.size.width, frame.size.height));
 		}
 	}
 	public void setTitle(String title){
 		this.setTitle(title, UIControlState.UIControlStateNormal);
 	}
 	public void setTitle(String title,int state){
 		if (title==null) {
 			titles.remove(state);
 		}else{
 			//if set title than added
 			if (titleLabel==null) {
 				titleLabel = new UILabel();
 				titleLabel.setTextAlignment(NSTextAlignment.NSTextAlignmentCenter);
 				titleLabel.setTextColor(UIColor.whiteColor());
 				this.addSubview(titleLabel);
 				if (frame!=null) {
 					titleLabel.setFrame(CGRectMake(0, 0, frame.size.width, frame.size.height));
 				}
 			}
 			titles.put(state, title);
 			if(isCurrentState(state)){
 				titleLabel.setText(title);
 			}
 		}
 	}
 	public void setTitleColor(UIColor titleColor){
 		this.setTitleColor(titleColor, UIControlState.UIControlStateNormal);
 	}
 	public void setTitleColor(UIColor titleColor,int state){
 		if (titleColor==null) {
 			titleColors.remove(state);
 		}else{
 			//if set title than added
 			if (titleLabel==null) {
 				titleLabel = new UILabel();
 				titleLabel.setTextAlignment(NSTextAlignment.NSTextAlignmentCenter);
 				this.addSubview(titleLabel);
 			}
 			titleColors.put(state, titleColor);
 			if(isCurrentState(state)){
 				titleLabel.setTextColor(titleColor);
 			}
 		}
 	}
 	public void setImage(UIImage image){
 		this.setImage(image, UIControlState.UIControlStateNormal);
 		//auto generate highlight image
 		this.autoHighlight();
 	}
 	private void autoHighlight(){
 		if (autoHighlight) {
 			UIImage normalImage = images.get(UIControlState.UIControlStateNormal);
 			if (normalImage!=null) {
 				UIImage highlightImage = normalImage.createHighlightImage();
 				if (highlightImage!=null) {
 					this.setImage(highlightImage, UIControlState.UIControlStateHighlighted);
 				}else{
 					this.setImage(null, UIControlState.UIControlStateHighlighted);
 				}
 			}
 		}
 	}
 	public void setImage(UIImage image,int state){
 		if (image==null) {
 			images.remove(state);
 		}else{
 			images.put(state, image);
 			if(isCurrentState(state)){
 				imageView.setImage(image);
 			}
 		}
 	}
 	@Override
 	public void setBackgroundImage(UIImage backgroundImage){
 		this.setBackgroundImage(backgroundImage, UIControlState.UIControlStateNormal);
 		//auto generate highlight image
 		
 	}
 	public void setBackgroundImage(UIImage backgroundImage,int state){
 		if (backgroundImage==null) {
 			backgroundImages.remove(state);
 		}else{
 			backgroundImages.put(state, backgroundImage);
 			if(isCurrentState(state)){
 				imageView.setBackgroundImage(backgroundImage);
 			}
 		}
 	}
 	
 	@Override
 	protected void invalidateState(){
 		//store the value temporarily
 		int currentState = state;
 		super.invalidateState();
 		
 		//caculate the effect state
 		if ((state&UIControlState.UIControlStateDisabled)>0) {
 			this.effectState = UIControlState.UIControlStateDisabled;
 		}else if((state&UIControlState.UIControlStateHighlighted)>0){
 			this.effectState = UIControlState.UIControlStateHighlighted;
 		}else if((state&UIControlState.UIControlStateSelected)>0){
 			this.effectState = UIControlState.UIControlStateSelected;
 		}else{
 			this.effectState = UIControlState.UIControlStateNormal;
 		}
 		
 		
 		//state Changed
 		if(currentState!=state){
 			UIImage image = currentImage();
 			if(image!=null){
 				imageView.setImage(image);
 			}
 			UIImage backgroundImage = currentBackgroundImage();
 			if(backgroundImage!=null){
 				imageView.setBackgroundImage(backgroundImage);
 			}
 			
 			String title = currentTitle();
 			if (title!=null) {
 				titleLabel.setText(title);
 			}
 			
 			UIColor titleColor = currentTitleColor();
 			if (titleColor!=null) {
 				titleLabel.setTextColor(titleColor);
 			}
 		}
 		
 	}
 	
 	public String currentTitle(){
 		String title = titles.get(state);
 		if (title==null) {
 			title = titles.get(effectState);
 		}
 		return title;
 	}
 	public UIImage currentImage(){
 		UIImage image = images.get(state);
 		if (image==null) {
 			image = images.get(effectState);
 		}
 		return image;
 	}
 	public UIImage currentBackgroundImage(){
 		UIImage backgroundImage = backgroundImages.get(state);
 		if (backgroundImage==null) {
 			backgroundImage = backgroundImages.get(effectState);
 		}
 		return backgroundImage;
 	}
 	public UIColor currentTitleColor(){
 		UIColor color = titleColors.get(state);
 		if (color==null) {
 			color = titleColors.get(effectState);
 		}
 		return color;
 	}
 	protected boolean isCurrentState(int state) {
 		return this.state==state||effectState==state;
 	}
 }
