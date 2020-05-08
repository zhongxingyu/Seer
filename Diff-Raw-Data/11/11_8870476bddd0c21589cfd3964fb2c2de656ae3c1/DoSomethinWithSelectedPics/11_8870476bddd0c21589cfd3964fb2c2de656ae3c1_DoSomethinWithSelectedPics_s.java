 package com.annuletconsulting.gallerytest;
 
 import com.annuletconsulting.gallery.PicTabsFragment;
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.TextView;
 
 public class DoSomethinWithSelectedPics extends Activity {
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_do_something);
		((TextView) findViewById(R.id.output)).setText(getIntent().getStringArrayExtra(PicTabsFragment.PIC_PATHS_ARRAY).toString());
 	}
 }
