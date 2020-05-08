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
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.cocoa4android.cg.CGRect;
 import org.cocoa4android.ns.NSInvocation;
 import org.cocoa4android.ns.NSMethodSignature;
 
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 
 public class UIControl extends UIView {
 	
 	protected int state = UIControlState.UIControlStateNormal;
 	
 	protected HashMap<UIControlEvent,ArrayList<NSInvocation>> callbacks = new HashMap<UIControlEvent,ArrayList<NSInvocation>>();
 	public UIControl() {
 		super();
 	}
 	public UIControl(CGRect frame) {
 		super(frame);
 	}
 	public void setView(View view) {
 		super.setView(view);
 		view.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				UIControl.this.callback(UIControlEvent.UIControlEventTouchUpInside);
 			}
 		});
 	}
 	@Override
 	protected void handleTouch(MotionEvent event){
 		super.handleTouch(event);
 		if (touchDownHandlerCount>0&&event.getAction()==MotionEvent.ACTION_DOWN) {
 			this.callback(UIControlEvent.UIControlEventTouchDown);
 		}
 	}
 	private void callback(UIControlEvent event){
 		ArrayList<NSInvocation> invocations = callbacks.get(event);
 		if(invocations!=null){
 			for(int i=0;i<invocations.size();i++){
 				NSInvocation invocation = invocations.get(i);
 				if (invocation.getParamsCount()==1) {
 					invocation.setArgument(UIControl.this, 2);
 				}
				invocation.performSelectorOnMainThread("invoke", null, NO);
 			}
 		}
 	}
 	private int touchDownHandlerCount = 0;
 	
 	public void addTarget(Object target,String selector,UIControlEvent controlEvent){
 		ArrayList<NSInvocation> invocations = callbacks.get(controlEvent);
 		if(invocations==null){
 			invocations = new ArrayList<NSInvocation>();
 			callbacks.put(controlEvent,invocations);
 		}
 		if (controlEvent==UIControlEvent.UIControlEventTouchDown) {
 			touchDownHandlerCount++;
 		}
 		NSMethodSignature sig = class2NSClass(target.getClass()).instanceMethodSignatureForSelector(selector);
 		if (sig!=null) {
 			NSInvocation invocation = NSInvocation.invocationWithMethodSignature(sig);
 			invocation.setTarget(target);
 			invocations.add(invocation);
 		}
 		
 	}
 	public void removeTarget(Object target,String selector,UIControlEvent controlEvent){
 		ArrayList<NSInvocation> invocations = callbacks.get(controlEvent);
 		if(invocations!=null){
 			for(int i=0;i<invocations.size();i++){
 				NSInvocation invocation = invocations.get(i);
 				if ((target==null&&selector==null)||(invocation.target()==target&&invocation.selector().equals(selector))) {
 					invocations.remove(i);
 					if (controlEvent==UIControlEvent.UIControlEventTouchDown) {
 						touchDownHandlerCount--;
 					}
 					break;
 				}
 			}
 		}
 	}
 	
 	protected int state() {
 		return state;
 	}
 
 	
 	private boolean enabled = YES;
 	
 	public boolean isEnabled() {
 		return enabled;
 	}
 	public void setEnabled(boolean enabled) {
 		this.enabled = enabled;
 		this.getView().setEnabled(enabled);
 		this.invalidateState();
 	}
 	private boolean selected = NO;
 	public boolean isSelected() {
 		return selected;
 	}
 	public void setSelected(boolean selected) {
 		this.selected = selected;
 		this.invalidateState();
 	}
 	private boolean highlighted = NO;
 	
 	public boolean isHighlighted() {
 		return highlighted;
 	}
 	public void setHighlighted(boolean highlighted) {
 		this.highlighted = highlighted;
 		this.invalidateState();
 	}
 	protected void invalidateState(){
 		state = UIControlState.UIControlStateNormal;
 		if (!this.isEnabled()) {
 			state |=UIControlState.UIControlStateDisabled;
 		}
 		if (this.isSelected()) {
 			state |=UIControlState.UIControlStateSelected;
 		}
 		if (this.isHighlighted()) {
 			state |=UIControlState.UIControlStateHighlighted;
 		}
 	}
 	public enum UIControlEvent{
 		UIControlEventTouchUpInside,
 		UIControlEventTouchDown,
 	}
 	
 	public class UIControlState{
 		public static final int UIControlStateNormal = 0;
 		public static final int UIControlStateHighlighted = 1 << 0;
 		public static final int UIControlStateDisabled = 1 << 1;
 		public static final int UIControlStateSelected = 1 << 2;
 	}
 
 	
 }
