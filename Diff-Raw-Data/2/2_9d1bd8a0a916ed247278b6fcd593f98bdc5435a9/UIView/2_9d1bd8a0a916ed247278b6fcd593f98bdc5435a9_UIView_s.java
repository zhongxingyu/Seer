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
 
 import org.cocoa4android.cg.CGAffineTransform;
 import org.cocoa4android.cg.CGPoint;
 import org.cocoa4android.cg.CGRect;
 import org.cocoa4android.ns.NSArray;
 import org.cocoa4android.ns.NSMutableArray;
 import org.cocoa4android.ns.NSObject;
 import org.cocoa4android.ns.NSSet;
 
 import android.R.integer;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.widget.RelativeLayout;
 import android.widget.RelativeLayout.LayoutParams;
 
 
 
 public class UIView extends NSObject{
 	private UIView superView;
 	private View view;
 	private boolean isHidden;
 	protected CGRect frame;
 	protected CGAffineTransform transform;
 	
 	private UIColor backgroundColor;
 	
 	
 	protected float density = UIScreen.mainScreen().getDensity();
 	
 	protected float scaleFactorX = UIScreen.mainScreen().getScaleFactorX();
 	protected float scaleFactorY = UIScreen.mainScreen().getScaleFactorY();
 	
 	protected float scaleDensityX = density*scaleFactorX;
 	protected float scaleDensityY = density*scaleFactorY;
 	
 	protected Context context = UIApplication.sharedApplication().getContext();
 	protected LayoutInflater inflater;
 	private int tag;
 	protected boolean keepAspectRatio = NO;
 	
 	
 	private CGPoint center = null;
 	
 	public CGPoint center() {
 		if (this.frame==null&&center!=null) {
 			return center;
 		}
 		CGRect frame = this.frame();
 		return CGPointMake(frame.size.width/2+frame.origin.x, frame.size.height/2+frame.origin.y);
 	}
 	/**
 	 * FIXME fail if UIImageView without setting frame
 	 * @param center
 	 */
 	public void setCenter(CGPoint center) {
 		CGRect frame = this.frame();
 		frame.origin.x = (int) (center.x-frame.size.width/2);
 		frame.origin.y = (int) (center.y-frame.size.height/2);
 		this.setFrame(frame);
 		this.center = center;
 	}
 	public UIView(){
 		//if no setting fill the parent
 		this.setView(new RelativeLayout(context));
 		params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
 		params.alignWithParent = true;
 		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
 		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
 		params.leftMargin = 0;
 		params.topMargin = 0;
 		this.view.setLayoutParams(params);
 	}
 	public UIView(int viewid){
 		inflater = LayoutInflater.from(context);  
 		this.setView(inflater.inflate(viewid, null));
 	}
 	public UIView(CGRect frame){
 		this.setView(new RelativeLayout(context));
 		this.setFrame(frame);
 	}
 	public UIView(View view){
 		this.setView(view);
 	}
 	public void addSubview(UIView child){
 		if(this.isViewGroup()){
 			ViewGroup vg = (ViewGroup)this.view;
 			vg.addView(child.getView());
 			child.setSuperView(this);
 		}
 	}
 	public void removeSubView(UIView child){
 		if(this.isViewGroup()){
 			ViewGroup vg = (ViewGroup)this.view;
 			vg.removeView(child.getView());
 			child.setSuperView(null);
 		}
 	}
 	public void removeFromSuperView(){
 		if(this.superView!=null){
 			this.superView.removeSubView(this);
 		}
 	}
 	public View getView(){
 		return this.view;
 	}
 	public void setView(View view){
 		this.view = view;
 		view.setTag(this);
 		
 		//check if this has a method called touchesBegan
 		if(view!=null){
 			this.view.setOnTouchListener(new OnTouchListener(){
 				@Override
 				public boolean onTouch(View v, MotionEvent event) {
 					
 					UITouch[] toucheArray = new UITouch[event.getPointerCount()];
 					
 					for(int i=0;i<toucheArray.length;i++){
 						float x = event.getX(i);
 						float y = event.getY(i);
 						float prevX = 0;
 						float prevY = 0;
 						if(event.getHistorySize()>0){
 							prevX = event.getHistoricalX(i, 0);
 							prevY = event.getHistoricalY(i, 0);
 						}
 						toucheArray[i] = new UITouch(x,y,prevX,prevY,new UIView(v));
 					}
 					NSSet touches = new NSSet(toucheArray);
 					UIEvent ev = new UIEvent(event);
 					// TODO Auto-generated method stub
 					if(event.getAction()==MotionEvent.ACTION_DOWN){
 						UIView.this.touchesBegan(touches,ev);
 					}else if(event.getAction()==MotionEvent.ACTION_MOVE){
 						UIView.this.touchesMoved(touches,ev);
 					}else if(event.getAction()==MotionEvent.ACTION_UP){
 						UIView.this.touchesEnded(touches,ev);
 					}
 					
					return false;
 				}
 				
 			});
 		}
 	}
 	public NSArray subViews(){
 		NSMutableArray subViews = null;
 		if(this.isViewGroup()){
 			ViewGroup vg = (ViewGroup)this.view;
 			subViews = NSMutableArray.array(vg.getChildCount());
 			for (int i = 0; i < vg.getChildCount(); i++) {
 				subViews.addObject(new UIView(vg.getChildAt(i)));
 			}
 		}
 		return subViews;
 	}
 	public UIView superView() {
 		return superView;
 	}
 	public void setSuperView(UIView superView) {
 		this.superView = superView;
 	}
 	public boolean isHidden() {
 		return isHidden;
 	}
 	public void setHidden(boolean isHidden) {
 		this.isHidden = isHidden;
 		this.view.setVisibility(isHidden?View.INVISIBLE:View.VISIBLE);
 	}
 	public CGRect frame() {
 		if(frame==null){
 			float width = this.getView().getWidth()/scaleDensityX;
 			float height = this.getView().getHeight()/scaleDensityY;
 			LayoutParams params = (LayoutParams) this.getView().getLayoutParams();
 			float x = 0;
 			float y = 0;
 			if(params!=null){
 				x = params.leftMargin/scaleDensityX;
 				y = params.topMargin/scaleDensityY;
 			}
 			frame = new CGRect(x,y,width,height);
 		}
 		return frame;
 	}
 	private LayoutParams params;
 	public void setFrame(CGRect frame) {
 		this.frame = frame;
 		float width = frame.size.width*scaleDensityX;
 		float height = frame.size.height*scaleDensityY;
 		float x = frame.origin.x*scaleDensityX;
 		float y = frame.origin.y*scaleDensityY;
 		//boolean isWidthFlexible = this.isAutoresizing(UIViewAutoresizing.UIViewAutoresizingFlexibleWidth);
 		//boolean isHeightFlexible = this.isAutoresizing(UIViewAutoresizing.UIViewAutoresizingFlexibleHeight);
 		boolean isLeftFlexible = this.isAutoresizing(UIViewAutoresizing.UIViewAutoresizingFlexibleLeftMargin);
 		boolean isTopFlexible = this.isAutoresizing(UIViewAutoresizing.UIViewAutoresizingFlexibleTopMargin);
 		/*
 		boolean isRightFlexible = this.isAutoresizing(UIViewAutoresizing.UIViewAutoresizingFlexibleRightMargin);
 		boolean isBottomFlexible = this.isAutoresizing(UIViewAutoresizing.UIViewAutoresizingFlexibleBottomMargin);
 		*/
 		
 		
 		if (keepAspectRatio&&this.frame!=null) {
 			if (scaleFactorX>scaleFactorY) {
 				float newWidth = frame.size.width*scaleDensityY;
 				float deltaWidth = width - newWidth;
 				x += deltaWidth/2;
 				width = newWidth;
 			}else{
 				float newHeight = frame.size.height*scaleDensityX;
 				float deltaHeight = height - newHeight;
 				y += deltaHeight/2;
 				height = newHeight;
 			}
 		}
 		
 		if (isLeftFlexible) {
 			x = frame.origin.x*scaleDensityX;
 		}
 		
 		if (isTopFlexible) {
 			y = frame.origin.y*scaleDensityY;
 		}
 		this.setViewFrame(x, y, width, height);
 		this.center = null;
 	}
 	
 	private void setViewFrame(float x,float y,float width,float height){
 		if (params==null) {
 			params = new LayoutParams((int)(width), (int)(height));
 			params.alignWithParent = true;
 			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
 			params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
 		}else{
 			params.width = (int)(width);
 			params.height =  (int)(height);
 		}
 		params.leftMargin = (int)(x);
 		params.topMargin = (int)(y);
 		this.view.setLayoutParams(params);
 	}
 	public boolean isKeepAspectRatio() {
 		return keepAspectRatio;
 	}
 	public void setKeepAspectRatio(boolean keepAspectRatio) {
 		if (keepAspectRatio!=this.keepAspectRatio) {
 			this.keepAspectRatio = keepAspectRatio;
 			if (this.frame!=null) {
 				this.setFrame(frame);
 			}
 			
 		}
 	}
 	public CGAffineTransform getTransform() {
 		return transform;
 	}
 	/**
 	 * Change the size and position of the shape
 	 * @param transform 
 	 */
 	public void setTransform(CGAffineTransform transform) {
 		this.transform = transform;
 	}
 	
 	public UIColor backgroundColor() {
 		return backgroundColor;
 	}
 	public void setBackgroundColor(UIColor backgroundColor) {
 		this.backgroundColor = backgroundColor;
 		this.view.setBackgroundColor(backgroundColor.getColor());
 	}
 	protected boolean isViewGroup(){
 		return ViewGroup.class.isInstance(this.view);
 	}
 	public int tag() {
 		return tag;
 	}
 	public void setTag(int tag) {
 		this.tag = tag;
 	}
 	public void setBackgroundImage(UIImage backgroundImage){
 		this.view.setBackgroundResource(backgroundImage.getResId());
 	}
 	public void bringSubviewToFront(UIView view){
 		if(this.isViewGroup()){
 			ViewGroup vg = (ViewGroup)this.view;
 			vg.bringChildToFront(view.getView());
 		}
 		//view.getView().bringToFront();
 	}
 	public void touchesBegan(NSSet touches,UIEvent event){
 		
 	}
 	public void touchesEnded(NSSet touches,UIEvent event){
 		
 	}
 	public void touchesMoved(NSSet touches,UIEvent event){
 		
 	}
 	
 	
 	private int autoresizingMask;
 	
 	public int autoresizingMask() {
 		return autoresizingMask;
 	}
 	//FIXME  cannot realize the whole function because the super views' frame changes
 	public void setAutoresizingMask(int autoresizingMask) {
 		if (autoresizingMask!=this.autoresizingMask) {
 			this.autoresizingMask = autoresizingMask;
 			if (this.frame!=null) {
 				this.setFrame(frame);
 			}
 		}
 		
 	}
 
 	public class UIViewAutoresizing{
 		public static final int UIViewAutoresizingNone = 0x00;
 		public static final int UIViewAutoresizingFlexibleLeftMargin = 0x01;
 		public static final int UIViewAutoresizingFlexibleWidth = 0x02;
 		public static final int UIViewAutoresizingFlexibleRightMargin = 0x04;
 		public static final int UIViewAutoresizingFlexibleTopMargin = 0x08;
 		public static final int UIViewAutoresizingFlexibleHeight = 0x10;
 		public static final int UIViewAutoresizingFlexibleBottomMargin = 0x20;
 	}
 	private boolean isAutoresizing(int autoresizing){
 		return (this.autoresizingMask&autoresizing)>0;
 	}
 }
