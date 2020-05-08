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
 
 
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.RelativeLayout.LayoutParams;
 
 public class UIImageView extends UIView {
 	
 	protected ImageView imageView;
 	private UIImage image;
 	private UIImage highlightedImage;
 	
 	private boolean highlighted=NO;
 	
 	public boolean isHighlighted() {
 		return highlighted;
 	}
 	public void setHighlighted(boolean highlighted) {
 		if(this.highlighted!=highlighted){
			if(highlightedImage!=null){
				imageView.setImageDrawable(highlightedImage.getDrawable());
 			}
 		}
 		this.highlighted = highlighted;
 	}
 	public UIImage HighlightedImage() {
 		return highlightedImage;
 	}
 	public void setHighlightedImage(UIImage highlightedImage) {
 		if (highlightedImage!=null&&this.highlightedImage!=highlightedImage&&highlighted) {
 			imageView.setImageDrawable(highlightedImage.getDrawable());
 		}
 		this.highlightedImage = highlightedImage;
 		
 	}
 	public UIImageView(UIImage image){
 		this();
 		this.setImage(image);
 	}
 	public UIImageView(){
 		imageView = new ImageView(this.context);
 		//fix me
 		imageView.setScaleType(ImageView.ScaleType.FIT_XY); 
 		
 		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
 		params.alignWithParent = true;
 		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
 		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
 		params.leftMargin = 0;
 		params.topMargin = 0;
 		imageView.setLayoutParams(params);
 		
 		this.setView(imageView);
 		//FIXME the imageView suppose to keepAspectRatio but the position will be moved and leave gaps
 		//this.keepAspectRatio = YES;
 	}
 	public UIImageView(CGRect frame){
 		this();
 		this.setFrame(frame);
 	}
 	public UIImage image() {
 		return image;
 	}
 	public void setImage(UIImage image) {
 		this.image = image;
 		if(image!=null){
 			imageView.setImageDrawable(image.getDrawable());
 			//imageView.setImageResource(image.getResId());
 			/*
 			Rect r = imageView.getDrawable().getBounds();
 			this.setFrame(new CGRect(r));
 			*/
 		}
 	}
 }
