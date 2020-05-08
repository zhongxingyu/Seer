 package com.android.task.tools;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.provider.MediaStore;
 import android.util.Log;
 
 public class ScaleBitmap {
 	private Uri mSrcImageUri = null;
 	private Bitmap mSrcBitmap = null;
 	private Bitmap mDesBitmap = null;
 	private Activity mActivity = null;
 	private final String TAG			= ScaleBitmap.class.getName();
 	private final int    SCALE_WIDTH    = 800;
 	private final int    SCALE_HEIGHT	= 800;
 	public ScaleBitmap (Activity a,Uri u)
 	{
 		this.mSrcImageUri = u;
 		this.mActivity = a;
 		init();
 	}
 	public Bitmap scale()
 	{
 		if (this.mSrcBitmap == null){
 			return null;
 		}
 		this.mDesBitmap = Bitmap.createScaledBitmap(this.mSrcBitmap,SCALE_WIDTH,SCALE_HEIGHT,true);
 //		Toast.makeText(this.mActivity, "õ"+String.valueOf(this.mDesBitmap.getHeight()), Toast.LENGTH_LONG).show();
 //    	Toast.makeText(this.mActivity, "õ"+String.valueOf(this.mDesBitmap.getWidth()), Toast.LENGTH_LONG).show();
 //    	Toast.makeText(this.mActivity, "õ"+String.valueOf(this.mDesBitmap.getDensity()), Toast.LENGTH_LONG).show();
 		return this.mDesBitmap;
 	}
 	private void init()
 	{
 		if (this.mSrcImageUri == null){
 			Log.d(TAG,"ļ");
 			return;
 		}
         try {
         	mSrcBitmap = MediaStore.Images.Media.getBitmap(this.mActivity.getContentResolver(), mSrcImageUri);
 //        	Toast.makeText(this.mActivity, "õ"+String.valueOf(this.mSrcBitmap.getHeight()), Toast.LENGTH_LONG).show();
 //        	Toast.makeText(this.mActivity, "õ"+String.valueOf(this.mSrcBitmap.getWidth()), Toast.LENGTH_LONG).show();
 //        	Toast.makeText(this.mActivity, "õ"+String.valueOf(this.mSrcBitmap.getDensity()), Toast.LENGTH_LONG).show();
 
 		} catch (FileNotFoundException e) {
 			Log.e(TAG,"ļûҵ:"+e.getMessage());
 			e.printStackTrace();
 		} catch (IOException e) {
 			Log.e(TAG,"ȡļ:"+e.getMessage());
 			e.printStackTrace();
 		}
 
 	}
 
 }
