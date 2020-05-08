 package com.final_proj.patel;
 
 import java.io.File;
 
 import android.app.Fragment;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ImageView;
 
 import com.chintanpatel.wiwac.R;
 
 public class CameraFragment extends Fragment implements OnClickListener {
 
 	private File file;
 	private String path;
 	private Uri mImageUri;
 	private View view;
 	private static final int CAMERA_EVENT = 2345;
 	 
 	@Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         view = inflater.inflate(R.layout.camera_fragment, container, false);
        
        
        
         startCamera(); 
 
        
         return view;
     }
 	
 	@Override
 	public void onStart() {
 		
 		Button button = (Button) getView().findViewById(R.id.newShot);
         button.setOnClickListener(this);
         
 		super.onStart();
 	}
 
 	public void startCamera() {
 		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
         path = Environment.getExternalStorageDirectory()+"/Wiwac";
         File directory = new File(path);
         directory.mkdirs();
         file = new File(path, String.valueOf(System.currentTimeMillis()) + ".jpg"); 
          
         mImageUri = Uri.fromFile(file); 
         intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri); 
          
         startActivityForResult(intent, CAMERA_EVENT);
 	}
     
 	@Override
 	public void onClick(View v) {
 		
 		switch(v.getId()){
 			case R.id.newShot:
 				startCamera();
 				break;
 		}
 		
 		Log.d("CameraFragment", "Button Pressed");
 		
 	}
 
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent intent)
 	{
 		Log.d("CameraFragment", "Inside onActivityResult");
 		
	    if(requestCode==CAMERA_EVENT)
 	    {
	    	Log.d("CameraFragment", "Inside requestCode==CAMERA_EVENT");
 	    	Log.d("CameraFragment", "ImageLocation: " + mImageUri.toString());
 			ImageView imageView = (ImageView) view.findViewById(R.id.imageview_pic_holder);
 			
 			Bitmap bmp = HelperClass.getBitmap(mImageUri.getPath());
 			
 			imageView.setImageBitmap(bmp);
 	    }
 	    
 	    super.onActivityResult(requestCode, resultCode, intent);
 	}
 
 }
