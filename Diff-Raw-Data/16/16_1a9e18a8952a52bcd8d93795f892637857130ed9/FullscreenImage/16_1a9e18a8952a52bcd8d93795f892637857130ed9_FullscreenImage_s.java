 package org.smerty.zooborns;
 
 import android.R;
 import android.app.Activity;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.ImageView;
 
 public class FullscreenImage extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		
 		ImageView fsimgview = new ImageView(this);
 		
 		if (fsimgview != null) {
 			Uri imgUri = this.getIntent().getData();
 			if (imgUri != null) {
				fsimgview.setImageURI(imgUri);
 			}
 			else {
 				fsimgview.setImageResource(R.drawable.ic_menu_help);
 			}
 			setContentView(fsimgview);
 		}
 		else {
 			Log.d("onCreate", "fsimgview was null?");
 		}
 	}
 
 }
