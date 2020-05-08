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
 
 import java.lang.reflect.Method;
 
 import org.cocoa4android.cg.CGAffineTransform;
 import org.cocoa4android.cg.CGPoint;
 import org.cocoa4android.cg.CGRect;
 import org.cocoa4android.ns.NSArray;
 import org.cocoa4android.ns.NSMutableArray;
 import org.cocoa4android.ns.NSSet;
 
 import android.content.Context;
 import android.graphics.Matrix;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.view.animation.AccelerateDecelerateInterpolator;
 import android.view.animation.AccelerateInterpolator;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.view.animation.DecelerateInterpolator;
 import android.view.animation.LinearInterpolator;
 import android.view.animation.Transformation;
 import android.view.animation.TranslateAnimation;
 import android.widget.RelativeLayout;
 import android.widget.RelativeLayout.LayoutParams;
 
 
 
 public class UIView extends UIResponder{
 
 
 	protected Context context = UIApplication.sharedApplication().getContext();
 	protected LayoutInflater inflater;
 	
 	//================================================================================
     // Constructor
     //================================================================================
 	private View view;
 	
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
 	
 	//================================================================================
     // Basic Method
     //================================================================================
 	private boolean isHidden;
 	private UIColor backgroundColor;
 	private int tag;
 	
 	public boolean isHidden() {
 		return isHidden;
 	}
 	public void setHidden(boolean isHidden) {
 		this.isHidden = isHidden;
 		this.view.setVisibility(isHidden?View.INVISIBLE:View.VISIBLE);
 	}
 	
 	public UIColor backgroundColor() {
 		return backgroundColor;
 	}
 	public void setBackgroundColor(UIColor backgroundColor) {
 		this.backgroundColor = backgroundColor;
 		this.view.setBackgroundColor(backgroundColor.getColor());
 	}
 
 	public int tag() {
 		return tag;
 	}
 	public void setTag(int tag) {
 		this.tag = tag;
 	}
 	
 	public void setBackgroundImage(UIImage backgroundImage){
 		if(backgroundImage.getResId()!=0){
 			this.getView().setBackgroundResource(backgroundImage.getResId());
 		}else{
 			this.getView().setBackgroundDrawable(backgroundImage.getDrawable());
 		}
 	}
 	public void bringSubviewToFront(UIView view){
 		if(this.isViewGroup()){
 			ViewGroup vg = (ViewGroup)this.view;
 			vg.bringChildToFront(view.getView());
 		}
 	}
 	
 	
 	//================================================================================
     // UIViewHierarchy
     //================================================================================
 	private UIView superView;
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
 	public UIView superview() {
 		return superView;
 	}
 	protected void setSuperview(UIView superView) {
 		this.superView = superView;
 	}
 	public void addSubview(UIView child){
 		if(this.isViewGroup()){
 			ViewGroup vg = (ViewGroup)this.view;
 			vg.addView(child.getView());
 			child.setSuperview(this);
 		}
 	}
 	public void removeSubView(UIView child){
 		if(this.isViewGroup()){
 			ViewGroup vg = (ViewGroup)this.view;
 			vg.removeView(child.getView());
 			child.setSuperview(null);
 		}
 	}
 	public void removeFromSuperView(){
 		if(this.superView!=null){
 			this.superView.removeSubView(this);
 		}
 	}
 	
 	protected boolean isViewGroup(){
 		return ViewGroup.class.isInstance(this.view);
 	}
 	
 	//================================================================================
     // Convention between View and UIView
     //================================================================================
 	private boolean hasTouchesBegan = NO;
 	protected boolean canConsumeTouch = YES;
 	
 	public void setView(View view){
 		boolean isUIView = this.getClass().equals(UIView.class);
 		
 		if (!isUIView&&view!=null&&this.view!=view) {
 			if (this.view!=null) {
 				//release the previous view's Listener
 				this.view.setOnTouchListener(null);
 			}
 			//check if the class override the method called touchesBegan
 			try {
 				Method began = this.getClass().getDeclaredMethod("touchesBegan", new Class[]{NSSet.class,UIEvent.class});
 				if (began!=null) {
 					hasTouchesBegan = YES;
 				}
 			} catch (SecurityException e) {
 			} catch (NoSuchMethodException e) {
 
 			}
 			
 			if (!isUIView) {
 				view.setOnTouchListener(new OnTouchListener(){
 					@Override
 					public boolean onTouch(View v, MotionEvent event) {
 						UIView.this.handleTouch(event);
 						if(hasTouchesBegan){
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
 							if(event.getAction()==MotionEvent.ACTION_DOWN){
 								UIView.this.touchesBegan(touches,ev);
 							}else if(event.getAction()==MotionEvent.ACTION_MOVE){
 								UIView.this.touchesMoved(touches,ev);
 							}else if(event.getAction()==MotionEvent.ACTION_UP){
 								UIView.this.touchesEnded(touches,ev);
 							}else if(event.getAction()==MotionEvent.ACTION_CANCEL){
 								UIView.this.touchesCancelled(touches,ev);
 							}
 							return canConsumeTouch;
 						}
 						return false;
 					}
 					
 				});
 			}
 		}
 		this.view = view;
 		view.setTag(this);
 	}
 	public View getView(){
 		return this.view;
 	}
 	//================================================================================
     // Size&Position
     //================================================================================
 	protected CGRect frame;
 	protected CGAffineTransform transform;
 	private CGPoint center = null;
 	protected boolean keepAspectRatio = NO;
 	
 	static float density = 1.0f;
 	static float scaleFactorX = 1.0f;
 	static float scaleFactorY = 1.0f;
 	
 	static float scaleDensityX = 1.0f;
 	static float scaleDensityY = 1.0f;
 	
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
 	protected LayoutParams params;
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
 		this.applyCenter(center);
 		this.center = center;
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
 	
 	public CGAffineTransform transform() {
 		return transform;
 	}
 	/**
 	 * Change the size and position of the shape by CGAffineTransform
 	 * @param transform transform matrix
 	 * @see CGAffineTransform
 	 */
 	public void setTransform(CGAffineTransform transform) {
 		this.applyTransformation(transform);
 		this.transform = transform;
 	}
 	
 	//================================================================================
     // AutoResizing
     //================================================================================
 
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
 	private boolean isAutoresizing(int autoresizing){
 		return (this.autoresizingMask&autoresizing)>0;
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
 	
 	//================================================================================
     // UIViewAnimation
     //================================================================================
 	private static NSMutableArray animations = null;
 	private static boolean animationsEnabled = YES;
 	private static boolean animationBegan = NO;
 	private static double duation = 0;
 	private static double delay=0;
 	private static int repeatCount=1;
 	private static UIViewAnimationCurve curve = null;
 	/**
 	 * begin an animation transaction
 	 * frame bounds center transform alpha backgroundColor contentStretch will be recorded
 	 * @param animationID
 	 * @param context
 	 */
 	//FIXME didn't use the animationID,didn't support setFrame
 	public static void beginAnimations(String animationID,Object context){
 		if (animationsEnabled) {
 			animationBegan = YES;
 			animations = NSMutableArray.array();
 		}
 	}
 	public static void setAnimationDuration(double duation){
 		UIView.duation = duation;
 	}
 	public static void setAnimationDelay(double delay){
 		UIView.delay = delay;
 	}
 	public static void setAnimationCurve(UIViewAnimationCurve curve){
 		UIView.curve = curve;
 	}
 	public static void setAnimationRepeatCount(int repeatCount){
 		UIView.repeatCount = repeatCount;
 	}
 	//FIXME empty method
 	public static void setAnimationTransition(UIViewAnimationTransition transition,UIView view,boolean cache){
 		
 	}
 	public static void commitAnimations() {
 		for (int i = 0; i < animations.count(); i++) {
 			Animation animation = (Animation) animations.objectAtIndex(i);
			animation.setDuration((long) UIView.duation);
 			switch (curve) {
 			case UIViewAnimationCurveEaseInOut:
 				animation.setInterpolator(new AccelerateDecelerateInterpolator());
 				break;
 			case UIViewAnimationCurveEaseIn:
 				animation.setInterpolator(new DecelerateInterpolator());
 				break;
 			case UIViewAnimationCurveEaseOut:
 				animation.setInterpolator(new AccelerateInterpolator());
 				break;
 			case UIViewAnimationCurveLinear:
 				animation.setInterpolator(new LinearInterpolator());
 				break;
 			default:
 				break;
 			}
 			animation.setRepeatCount(repeatCount);
 			animation.setStartTime((long) (AnimationUtils.currentAnimationTimeMillis()+UIView.delay));
 			animation.setFillAfter(YES);
 			//animation.startNow();
 			animation.start();
 		}
 		
 		animationBegan = NO;
 		animations = null;
 		UIView.repeatCount = 1;
 		UIView.duation = 0.0f;
 		UIView.delay = 0.0f;
 	}
 	public static void setAnimationsEnabled(boolean enabled){
 		animationsEnabled = enabled;
 	}
 	private void applyTransformation(CGAffineTransform transform){
 		MatrixAnimation animation = new MatrixAnimation(this.transform,transform);
 		//animation.setFillBefore(YES);
 		animation.setFillAfter(YES);
 		if (frame!=null) {
 			animation.setAnchorPoint(CGPointMake(frame.size.width*scaleFactorX/2,frame.size.height*scaleFactorY/2));
 		}
 		if (animationBegan) {
 			this.getView().setAnimation(animation);
 			animations.addObject(animation);
 		}else{
 			animation.setDuration(0);
 			this.getView().startAnimation(animation);
 		}
 	}
 	private void applyCenter(CGPoint center){
 		if (animationBegan) {
 			CGPoint currentCenter = this.center();
 			float toXDelta = center.x - currentCenter.x;
 			float toYDelta = center.y - currentCenter.y;
 			TranslateAnimation animation = new TranslateAnimation(0, toXDelta*scaleDensityX, 0, toYDelta*scaleDensityY);
 			this.getView().setAnimation(animation);
 			animations.addObject(animation);
 		}else{
 			//FIXME didn't apply the value to the frame
 			CGRect frame = this.frame();
 			frame.origin.x = (int) (center.x-frame.size.width/2);
 			frame.origin.y = (int) (center.y-frame.size.height/2);
 			this.setFrame(frame);
 		}
 	}
 	public static boolean areAnimationsEnabled(){
 		return animationsEnabled;
 	}
 	public enum UIViewAnimationCurve{
 		UIViewAnimationCurveEaseInOut,         // slow at beginning and end
 	    UIViewAnimationCurveEaseIn,            // slow at beginning
 	    UIViewAnimationCurveEaseOut,           // slow at end
 	    UIViewAnimationCurveLinear
 	}
 	public enum UIViewAnimationTransition{
 		UIViewAnimationTransitionNone,
 	    UIViewAnimationTransitionFlipFromLeft,
 	    UIViewAnimationTransitionFlipFromRight,
 	    UIViewAnimationTransitionCurlUp,
 	    UIViewAnimationTransitionCurlDown,
 	}
 	
 	protected class MatrixAnimation extends Animation{
 		private CGAffineTransform endTransform;
 		
 		private float startRotation = 0.0f;
 		private float endRotation = 0.0f; 
 		private float deltaRotation = 0.0f;
 		
 		private float startScaleX = 0.0f;
 		private float startScaleY = 0.0f;
 		private float deltaScaleX = 0.0f;
 		private float deltaScaleY = 0.0f;
 		
 		private float startTransX = 0.0f;
 		private float startTransY = 0.0f;
 		private float deltaTransX = 0.0f;
 		private float deltaTransY = 0.0f;
 		
 		private CGPoint anchorPoint;
 		
 		
 		public MatrixAnimation(CGAffineTransform startTransform,CGAffineTransform endTransform) {
 			this.endTransform = endTransform;
 			if (startTransform!=null) {
 				startRotation = (float) Math.toDegrees(Math.atan2(startTransform.b, startTransform.a));
 				startScaleX = startTransform.a;
 				startScaleY = startTransform.d;
 				
 				startTransX = startTransform.tx;
 				startTransY = startTransform.ty;
 			}
 			endRotation = (float) Math.toDegrees(Math.atan2(this.endTransform.b, endTransform.a));
 			deltaRotation = endRotation - startRotation;
 			
 			deltaScaleX = endTransform.a - startScaleX;
 			deltaScaleY = endTransform.d - startScaleY;
 			
 			deltaTransX = endTransform.tx - startTransX;
 			deltaTransY = endTransform.ty - startTransY;
 		}
 		@Override
 		protected void applyTransformation(float interpolatedTime, Transformation t){
 			//ͨMatrix.setScaleţúXYӣinterpolatedTimeǴ01仯ʵֵЧǿؼС𽥱仯
 			Matrix matrix = t.getMatrix();
 			matrix.setScale(startScaleX+deltaScaleX*interpolatedTime, startScaleY+deltaScaleY*interpolatedTime);
 			matrix.setTranslate(startTransX+deltaTransX*interpolatedTime, startTransY+deltaTransY*interpolatedTime);
 			
 			if (anchorPoint!=null) {
 				matrix.setRotate(startRotation+deltaRotation*interpolatedTime,anchorPoint.x,anchorPoint.y);
 			}else{
 				matrix.setRotate(startRotation+deltaRotation*interpolatedTime);
 			}
 			//Matrix ʵָָӵı任
 			//preTranslateǰƶpostTranslateɺƶ
 		}
 		public void setAnchorPoint(CGPoint anchorPoint){
 			this.anchorPoint = anchorPoint;
 		}
 	}
 	
 }
