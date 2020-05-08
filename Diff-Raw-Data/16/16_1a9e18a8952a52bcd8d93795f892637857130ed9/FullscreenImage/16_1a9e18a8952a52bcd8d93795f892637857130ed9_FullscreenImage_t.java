 package org.smerty.zooborns;
 
import java.io.FileInputStream;
import java.io.FileNotFoundException;

 import android.R;
 import android.app.Activity;
import android.graphics.drawable.Drawable;
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
				Drawable image;
				try {
					image = Drawable.createFromStream(new FileInputStream(imgUri.getPath()), "src");
					fsimgview.setImageDrawable(image);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					fsimgview.setImageResource(R.drawable.ic_menu_close_clear_cancel);
				}
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
