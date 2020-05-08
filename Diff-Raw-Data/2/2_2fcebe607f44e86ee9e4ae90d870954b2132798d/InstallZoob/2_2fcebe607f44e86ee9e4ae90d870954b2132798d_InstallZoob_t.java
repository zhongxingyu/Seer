 package net.fhtagn.zoobeditor;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 public class InstallZoob extends Activity {
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.installzoob);
 		
 		Button okBtn = (Button)findViewById(R.id.ok);
 		okBtn.setOnClickListener(new OnClickListener() {
 			@Override
       public void onClick(View arg0) {
				Uri fullVersionURI = Uri.parse("market://details?id=net.fhtagn.zoob_demo");
 				Intent i = new Intent(Intent.ACTION_VIEW, fullVersionURI);
 				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 				startActivity(i);
 			} 
     });
 		
 		Button cancelBtn = (Button)findViewById(R.id.cancel);
 		cancelBtn.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				finish();
 			}
 		});
 	}
 }
