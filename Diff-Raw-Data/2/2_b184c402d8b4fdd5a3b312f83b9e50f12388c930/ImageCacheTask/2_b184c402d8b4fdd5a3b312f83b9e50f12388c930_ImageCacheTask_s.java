 /*******************************************************************************
  * Copyright 2012-2013 Trento RISE
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *        http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package eu.trentorise.smartcampus.cm.helper;
 
 import java.lang.ref.WeakReference;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.widget.ImageView;
 import eu.trentorise.smartcampus.cm.custom.data.CMHelper;
 
 public class ImageCacheTask extends AsyncTask<String, Void, Bitmap> {
 
 	private final WeakReference<ImageView> imageViewReference;
 	private int placeholder;
 
 	private String tag;
 
 	public ImageCacheTask(ImageView imageView, int placeholder) {
 		// Use a WeakReference to ensure the ImageView can be garbage
 		// collected
 		imageViewReference = new WeakReference<ImageView>(imageView);
 		this.placeholder = placeholder;
 	}
 
 	@Override
 	protected Bitmap doInBackground(String... params) {
 		byte[] fileContent;
 		Bitmap img = null;
 		if (params.length != 2) {
 			Log.w(ImageCacheTask.class.getName(),
 					"ImageCacheTask expected two parameters");
 			return null;
 		}
 		try {
 			this.tag = params[1];
 
 			fileContent = ImageCacheProvider.get(params[1]);
 			if (fileContent == null) {
 				fileContent = CMHelper.downloadFile(Long.parseLong(params[0]
 						.substring(params[0].lastIndexOf("/") + 1)));
 				if (fileContent == null) {
 					Log.w(this.getClass().getSimpleName(),
 							"download bitmap failed");
 				}
 				ImageCacheProvider.store(params[1], fileContent);
 			}
 			img = BitmapFactory.decodeByteArray(fileContent, 0,
 					fileContent.length);
 			if (img == null) {
 				Log.w(this.getClass().getSimpleName(), "decode bitmap failed");
 			}
 		} catch (Exception e) {
 			Log.e(this.getClass().getSimpleName(),
 					"Exception decoding profile picture");
 		}
 		return img;
 	}
 
 	@Override
 	protected void onPostExecute(Bitmap result) {
 		super.onPostExecute(result);
 		ImageView imgView = imageViewReference.get();
 		if (imgView == null || !tag.equals(imgView.getTag()))
 			return;
 
 		imgView.setImageResource(placeholder);
 		if (result != null) {
 			if (imgView.getWidth() > 0 && imgView.getHeight() > 0) {
 				// if(imgView.getWidth() < imgView.getHeight())
 				if (result.getWidth() < result.getHeight())
 					// imgView.setImageBitmap(Bitmap.createScaledBitmap(result,
 					// imgView.getWidth(), imgView.getHeight(), false));
 					imgView.setImageBitmap(Bitmap.createScaledBitmap(result,
 							imgView.getWidth(), 200, false));
				else if (result.getWidth() < result.getHeight())
 					imgView.setImageBitmap(Bitmap.createScaledBitmap(result,
 							200, imgView.getHeight(), false));
 				else
 					imgView.setImageBitmap(Bitmap.createScaledBitmap(result,
 							imgView.getWidth(), imgView.getHeight(), false));
 
 			} else {
 				imgView.setImageBitmap(result);
 			}
 		}
 	}
 
 }
