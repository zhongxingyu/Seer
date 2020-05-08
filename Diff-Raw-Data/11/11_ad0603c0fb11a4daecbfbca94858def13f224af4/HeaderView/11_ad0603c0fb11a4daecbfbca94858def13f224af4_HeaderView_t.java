 package net.analogyc.wordiary;
 
 import android.content.Context;
 import android.content.pm.PackageManager;
 import android.graphics.Typeface;
 import android.util.AttributeSet;
 import android.view.LayoutInflater;
 import android.widget.Button;
 import android.widget.LinearLayout;
 
 /**
  * The blue header that appears through most of the application
  */
 public class HeaderView extends LinearLayout {
 
 	LayoutInflater inflater;
 	Button takePhotoButton;
 
 	public HeaderView(Context context, AttributeSet attrs) {
 		super(context, attrs);
 
 		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		inflater.inflate(R.layout.header, this, true);
 
 		takePhotoButton = (Button) findViewById(R.id.takePhotoButton);
 
		if (!(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
			|| context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA))) {
 			takePhotoButton.setTextColor(0x77FFFFFF);
 		}
 	}
 }
