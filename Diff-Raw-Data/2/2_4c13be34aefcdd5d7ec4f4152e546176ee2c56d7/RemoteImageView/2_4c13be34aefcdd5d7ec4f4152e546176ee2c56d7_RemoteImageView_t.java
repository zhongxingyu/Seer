 /*
  *  Copyright (c) 2012 Daniel Huckaby
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *  http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package com.handlerexploit.prime.widgets;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.util.AttributeSet;
 import android.widget.ImageView;
 
 import com.handlerexploit.prime.utils.ImageManager;
 import com.handlerexploit.prime.utils.ImageManager.ExtendedRequest;
 
 /**
  * This ImageView specializes in retrieving remote images.<br>
  * <br>
 * This class is thread safe and will work fluidly with the ViewHolder optimization
  * pattern.
  */
 public class RemoteImageView extends ImageView implements ExtendedRequest {
 
     private ImageManager mImageManager;
 
     private String mImageURL;
 
     public RemoteImageView(Context context) {
         super(context);
         initialize(context, null, 0);
     }
 
     public RemoteImageView(Context context, AttributeSet attrs) {
         super(context, attrs);
         initialize(context, attrs, 0);
     }
 
     public RemoteImageView(Context context, AttributeSet attrs, int defStyle) {
         super(context, attrs, defStyle);
         initialize(context, attrs, defStyle);
     }
 
     private void initialize(Context context, AttributeSet attrs, int defStyle) {
         mImageManager = ImageManager.getInstance(context);
     }
 
     /**
      * Sets the content of this ImageView to the specified URL.
      * 
      * @param source
      *            The URL of a remote image
      */
     public void setImageURL(String source) {
         if (source != null) {
             mImageURL = source;
             mImageManager.get(this);
         }
     }
 
     @Override
     public void onImageReceived(String source, Bitmap bitmap) {
         if (mImageURL != null && mImageURL.equals(source) && bitmap != null) {
             setImageBitmap(bitmap);
         }
     }
 
     @Override
     public String getSource() {
         return mImageURL;
     }
 
     @Override
     public Bitmap onPreProcess(Bitmap raw) {
         return raw;
     }
 }
