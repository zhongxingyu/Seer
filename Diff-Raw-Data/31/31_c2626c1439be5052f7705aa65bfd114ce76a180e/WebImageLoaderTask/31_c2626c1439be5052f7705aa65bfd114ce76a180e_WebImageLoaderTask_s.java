 package com.junj.imagerssreader;
 
 import java.io.InputStream;
 import java.net.URL;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 // import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 import android.widget.ImageView;
 
 public class WebImageLoaderTask extends AsyncTask<String, Void, Bitmap> {
 	private ImageView mImageView;
 	private String mUrl;
	private String mTag;
 	
	public WebImageLoaderTask(ImageView iv) {
 		mImageView = iv;
		mTag = mImageView.getTag().toString();
 	}
 
 	@Override
 	protected Bitmap doInBackground(String... params) {
 		mUrl = params[0];
 		try {
 			// Cache から取得
 			Bitmap image = ImageCache.getImage(mUrl);
 			
 			// Cache が無かったら webにアクセスする
 			if (image == null) {
 // debug 
 System.out.println("loading from web");
 			
 				mImageView.setTag(mUrl);
 				URL imageURL = new URL(mUrl);
 				InputStream is = imageURL.openStream();
 				image = BitmapFactory.decodeStream(is);
 				is.close();
 				
 				// Cacheにセットする
 				ImageCache.setImage(mUrl, image);
 			} else {
 // debug 
 System.out.println("loading from cache");
 				
 			}
 			
 			return image;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	@Override
 	protected void onPostExecute(Bitmap result) {
		if (mTag.equals(mImageView.getTag())) {
 			mImageView.setImageBitmap(result);
 		}
 	}
 	/*
 	private Drawable getImageDrawableByUrlString(String imageUrlString) {
 		Drawable drawable = null;
 		try {
 			URL imageURL = new URL(imageUrlString);
 			InputStream is = imageURL.openStream();
 			drawable = Drawable.createFromStream(is, "");
 			is.close();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		System.out.println(drawable);
 		return drawable;
 	}
 	*/
 }
