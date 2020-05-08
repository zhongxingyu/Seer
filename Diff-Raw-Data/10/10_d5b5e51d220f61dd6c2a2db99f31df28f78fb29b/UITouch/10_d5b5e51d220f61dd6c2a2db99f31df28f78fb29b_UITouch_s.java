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
 
 import org.cocoa4android.cg.CGPoint;
 import org.cocoa4android.ns.NSObject;
 
 public class UITouch extends NSObject{
 	private float x;
 	private float y;
 	private float previousX;
 	private float previousY;
 	private UIView view;
 	
 	public UITouch(float x,float y,float previousX,float previousY,UIView view){
 		this.x = x;
 		this.y = y;
 		this.previousX = previousX;
 		this.previousY = previousY;
 		this.view = view;
 	}
 	//fix me
 	public CGPoint locationInView(UIView view){
		return new CGPoint(x,y);
 	}
 	public CGPoint previousLocationInView(UIView view){
 		if(previousY!=0){
 			return new CGPoint(previousX,previousY);
 		}else{
 			return new CGPoint(x,y);
 		}
 	}
 	public UIView getView() {
 		return view;
 	}
 }
