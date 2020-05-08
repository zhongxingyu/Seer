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
 import org.cocoa4android.cg.CGPoint;
 import org.cocoa4android.cg.CGRect;
 import org.cocoa4android.ns.NSMutableArray;
 
 import android.view.ViewGroup;
 
 public class UIPageControl extends UIView{
 	private int numberOfPages;
 	private int currentPage;
 	private CGPoint center;
 	
 	public void setCenter(CGPoint center){
 		super.setCenter(center);
 		this.center = CGPointMake(center.x, center.y);
 	}
 	public void setFrame(CGRect frame){
 		super.setFrame(frame);
 		this.center = null;
 	}
 	private NSMutableArray dots = NSMutableArray.array(10);
 	
 	
 	public int getNumberOfPages() {
 		return numberOfPages;
 	}
 
 	public void setNumberOfPages(int numberOfPages) {
 		this.numberOfPages = numberOfPages;
 		//remove all child
 		ViewGroup vg = (ViewGroup)this.getView();
 		vg.removeAllViews();
 		dots.removeAllObjects();
 		
 		//resize uiview
 		CGRect appFrame = UIScreen.mainScreen().applicationFrame();
 		int singleWidth = (int) (appFrame.size.width/45.0);
 		int realWidth = singleWidth*(2*numberOfPages+1);
 		int realHeight = singleWidth;
 		CGRect frame = this.frame();
 		frame.size.width = realWidth;
 		frame.size.height = realHeight;
 		
 		if (center!=null) {
 			//fix position
 			frame.origin.x = (int) (center.x-frame.size.width/2);
 			frame.origin.y = (int) (center.y-frame.size.height/2);
 		}
 		super.setFrame(frame);
 		
 		//add new child
 		for (int i = 0; i < numberOfPages; i++) {
 			UIImageView imageView = null;
 			if (i==0) {
				imageView = new UIImageView(new UIImage(R.drawable.c4a_pagecontrol_active));
 				currentPage = 0;
 			}else{
				imageView = new UIImageView(new UIImage(R.drawable.c4a_pagecontrol_dim));
 			}
 			imageView.setFrame(CGRectMake(singleWidth*(i*2+1), 0, singleWidth, singleWidth));
 			this.addSubview(imageView);
 			dots.addObject(imageView);
 		}
 	}
 
 	public int currentPage() {
 		return currentPage;
 	}
 
 	public void setCurrentPage(int currentPage) {
 		if (currentPage>=this.numberOfPages||currentPage<0) {
 			//invalid
 			return;
 		}
 		if (this.currentPage==currentPage) {
 			return;
 		}
 		UIImageView currentImageView = (UIImageView) dots.objectAtIndex(this.currentPage);
 		UIImageView nextImageView = (UIImageView) dots.objectAtIndex(currentPage);
 		
 		CGRect currentFrame = currentImageView.frame();
 		CGRect nextFrame = nextImageView.frame();
 		currentImageView.setFrame(nextFrame);
 		nextImageView.setFrame(currentFrame);
 		dots.replaceObject(this.currentPage, nextImageView);
 		dots.replaceObject(currentPage, currentImageView);
 		
 		this.currentPage = currentPage;
 		
 	}
 }
