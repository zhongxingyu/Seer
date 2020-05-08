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
 
 import org.cocoa4android.R;
 import org.cocoa4android.cg.CGRect;
 
 import android.R.raw;
 import android.graphics.Color;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.ToggleButton;
 
 public class UISwitcher extends UIControl {
 	protected ToggleButton switcher = null;
 	private boolean on = NO;
 	
 	private UIImage onImage;
 	private UIImage offImage;
 	
 	public ToggleButton getSwitcher() {
 		return switcher;
 	}
 	public void setSwitcher(ToggleButton switcher) {
 		this.switcher = switcher;
 	}
 	public UISwitcher(){
 		ToggleButton btn = new ToggleButton(context);
 		btn.setTextColor(Color.TRANSPARENT);
 		btn.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 				// TODO Auto-generated method stub
 				UISwitcher.this.setOn(isChecked, YES);
 			}
 		});
 		this.setSwitcher(btn);
 		btn.setFocusable(NO);
 		this.setView(btn);
 		this.setFrame(CGRectMake(0, 0, 0, 0));
 		
		onImage = new UIImage(R.drawable.zz_c4a_switcher_on);
		offImage = new UIImage(R.drawable.zz_c4a_switcher_off);
 		this.setOn(YES, YES);
 	}
 	
 	// This class enforces a size appropriate for the control. The frame size is ignored.
 	public UISwitcher(CGRect frame){
 		this();
 		this.setFrame(frame);
 	}
 	//FIXME no animated
 	public void setOn(boolean on,boolean animated){
 		if (on!=this.on) {
 			this.on = on;
 			if (on) {
 				this.setBackgroundImage(onImage);
 			}else{
 				this.setBackgroundImage(offImage);
 			}
 			switcher.setChecked(on);
 		}
 	}
 	public boolean isOn(){
 		return on;
 	}
 	
 	public void setFrame(CGRect frame){
 		CGRect appFrame = UIScreen.mainScreen().applicationFrame();
 		frame.size.width = appFrame.size.width*0.24f;
 		frame.size.height = frame.size.width*0.35f;
 		super.setFrame(frame);
 	}
 }
