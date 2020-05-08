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
 
 import java.util.Stack;
 
 import org.cocoa4android.cg.CGRect;
 
 
 import android.view.animation.Animation;
 import android.view.animation.Animation.AnimationListener;
 import android.view.animation.TranslateAnimation;
 
 public class UINavigationController extends UIViewController {
 	private Stack<UIViewController> stack = new Stack<UIViewController>();
 	private UIView fromView;
 	private UIViewController toViewController;
 	private UIViewController fromViewController;
 	private boolean isPush;
 	
 	private boolean navigationBarHidden;
 	public UINavigationController(){
 		super();
 	}
 	public UINavigationController(int resId){
 		super(resId);
 	}
 	public void pushViewController(UIViewController viewController,boolean animated){
 		if (!isTransition) {
 			isTransition = YES;
 			UIView lastView = null;
 			if(stack.size()>0){
 				lastView =stack.peek().view();
 			}
 			viewController.setNavigationController(this);
 			UIView view = viewController.view();
 			this.view().addSubview(view);
 			
 			if(animated&&lastView!=null){
 				this.translateBetweenViews(lastView, view,true);
 			}else{
 				if(lastView!=null){
 					lastView.setHidden(true);
 				}
 				viewController.viewDidAppear(NO);
 				isTransition = NO;
 			}
 			toViewController = viewController;
			fromViewController = stack.peek();
 			stack.push(viewController);
 		}
 		
 	}
 
 	public void popViewController(boolean animated){
 		if (!isTransition) {
 			isTransition = YES;
 			if(stack.size()>1){
 				fromViewController =  stack.pop();
 				toViewController = stack.peek();
 				if(animated){
 					this.translateBetweenViews(fromViewController.view(), toViewController.view(),false);
 				}else{
 					CGRect frame = UIScreen.mainScreen().applicationFrame();
 					toViewController.view().setFrame(new CGRect(0,0,frame.size().width(),frame.size().height()));
 					toViewController.view().setHidden(false);
 					
 					toViewController.viewDidAppear(NO);
 					isTransition = NO;
 					
 					fromViewController.view().removeFromSuperView();
 					fromViewController.viewDidUnload();
 				}
 			}
 		}
 		
 		
 	}
 	public void popToRootViewController(boolean animated){
 	}
 	public void popToViewController(UIViewController viewController,boolean animated){
 		
 	}
 	private void translateBetweenViews(UIView fromView ,UIView toView ,boolean isPush){
 		//show
 		toView.setHidden(false);
 		
 		this.fromView = fromView;
 		this.isPush = isPush;
 		
 		//get the real application frame
 		CGRect applicationFrame = UIScreen.mainScreen().applicationFrame;
 		float applicationWidth = applicationFrame.size().width();
 		TranslateAnimation animation1 = null;
 		TranslateAnimation animation2 = null;
 		if(isPush){
 			animation1 = new TranslateAnimation(0,-applicationWidth,0,0);
 			animation2 = new TranslateAnimation(applicationWidth,0,0,0);
 		}else{
 			animation1 = new TranslateAnimation(0,applicationWidth,0,0);
 			animation2 = new TranslateAnimation(-applicationWidth,0,0,0);
 		}
 		
 		
 		animation1.setDuration(500);
 		fromView.getView().startAnimation(animation1);
 		animation1.startNow();
 		
 		animation2.setDuration(500);
 		toView.getView().startAnimation(animation2);
 		animation2.startNow();
 		
 		
 		animation1.setAnimationListener(new AnimationListener(){
 
 			@Override
 			public void onAnimationEnd(Animation animation) {
 				//HIDE
 				if(UINavigationController.this.isPush){
 					UINavigationController.this.fromView.setHidden(true);
 				}else{
 					UINavigationController.this.fromView.removeFromSuperView();
 					UINavigationController.this.fromViewController.viewDidUnload();
 				}
 				if (UINavigationController.this.toViewController!=null) {
 					UINavigationController.this.toViewController.viewDidAppear(YES);
 					UINavigationController.this.toViewController = null;
 				}
 				isTransition = NO;
 			}
 
 			@Override
 			public void onAnimationRepeat(Animation animation) {
 				
 			}
 
 			@Override
 			public void onAnimationStart(Animation animation) {
 				
 			}
 			
 		});
 	}
 	public boolean isNavigationBarHidden() {
 		return navigationBarHidden;
 	}
 	public void setNavigationBarHidden(boolean navigationBarHidden) {
 		this.navigationBarHidden = navigationBarHidden;
 	}
 	@Override
 	public boolean backKeyDidClicked(){
 		if(!super.backKeyDidClicked()){
 			return false;
 		}
 		//fix me 
 		
 		if(stack.size()>1){
 			this.popViewController(true);
 			return false;
 		}
 		return true;
 	}
 }
