 package com.example.thamatrix;
 
 import android.service.dreams.DreamService;
 import android.util.Log;
 
 public class DreamatrixService extends DreamService {
 	
 	static final String TAG = "DreamatrixService";
 	
	DreamatrixView mDreamatrixView;
    
 	@Override
     public void onAttachedToWindow() {
         super.onAttachedToWindow();
         
         Log.d(TAG, "onAttachedToWindow");
 
         // Exit dream upon user touch
         setInteractive(false);
         // Hide system UI
         setFullscreen(true);
         // Set the dream layout
         setContentView(R.layout.dreamatrix_layout);
        
        mDreamatrixView = (DreamatrixView) findViewById(R.id.dreamatrix);
     }
 
 	@Override
 	public void onDetachedFromWindow() {
 		super.onDetachedFromWindow();
 		Log.d(TAG, "onDetachedFromWindow");	
 	}
 
 	@Override
 	public void onDreamingStarted() {
 		super.onDreamingStarted();
 		Log.d(TAG, "onDreamingStarted");
 		
 		// Start dream animation here
 		
 		
 	}
 
 	@Override
 	public void onDreamingStopped() {
 		super.onDreamingStopped();
 		Log.d(TAG, "onDreamingStopped");
 	}
 }
